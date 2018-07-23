package com.e_trans.xxappstore.downloadservice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.e_trans.xxappstore.XxCustomApplication;
import com.e_trans.xxappstore.constant.Constant;
import com.e_trans.xxappstore.db.DBHelper;
import com.e_trans.xxappstore.entity.AppListEntity;
import com.e_trans.xxappstore.entity.RegisterResultEntity;
import com.e_trans.xxappstore.request.UrlManager;
import com.e_trans.xxappstore.request.VolleyInterface;
import com.e_trans.xxappstore.request.VolleyRequest;
import com.e_trans.xxappstore.utils.MD5Util;
import com.e_trans.xxdownloadaidl.DownloadInfo;
import com.e_trans.xxdownloadaidl.IDownloadListener;
import com.e_trans.xxdownloadaidl.IDownloadService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.etrans.etsdk.config.EtSDK;
import cn.com.etrans.etsdk.utils.HardVersionInfoManager;

public class DownloadService extends Service {

    private String TAG = "DownloadService";
    private static XxCustomApplication application;
    private Map<String, String> registerParams = new HashMap<String, String>();
    private Map<String, String> appListParams = new HashMap<String, String>();
    private Map<String, String> appUpdateParams = new HashMap<String, String>();
    private AppListEntity appListEntity = null;
    private AppListEntity appUpdateEntity = null;
    private String appIds = "";//所有需要强制更新的id拼在一起用，隔开
    private Map<String, String> downloadParams = new HashMap<String, String>();
    private RegisterResultEntity registerResultEntity = null;
    //    private Map<String, DownLoader> downLoaders = new HashMap<String, DownLoader>();//下载器的Map KEY是fileId
    private IDownloadListener iDownloadListener;
    private int fileSize;
    private Intent mIntent = null;
    private boolean isCheckUpdate = false;
    private boolean isStartWait = true;
    private int fileIdTmp;
    private String fileNameTmp;
    private boolean isFirst = true;
    //    private boolean isClearWait = true;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private int outTimes = 0;
    boolean isNormal = false;
    private Handler timeHandler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (isNetworkAvailable(DownloadService.this)) {
                timeHandler.removeCallbacks(runnable);
                checkUpdate();
            } else {
                timeHandler.postDelayed(this, 2000);
            }
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1://一边下载进度条一边上消息，去更新
                    try {
                        if (msg.obj != null) {
                            if (msg.arg1 == 0 && msg.arg2 > 0) {
                                outTimes = 0;
                            }
                            if (iDownloadListener != null) {
                                iDownloadListener.onUpdateProgress(msg.arg2, (String) msg.obj);
                            }
                            if (msg.arg2 == msg.arg1) {//下载完毕
                                if (application != null) {
                                    for (int i = 0; i < application.downloadList.size(); i++) {
                                        if (Integer.parseInt((String) msg.obj) == application.downloadList.
                                                get(i).fileId) {
                                            fileIdTmp = application.downloadList.get(i).fileId;
                                            fileNameTmp = application.downloadList.get(i).appName;
                                            if (application.downLoaders != null) {
                                                DownLoader loader = application.downLoaders.get((String) msg.obj);
                                                if (loader != null)
                                                    loader.deleteInfoByFileId((String) msg.obj);//在数据库中把对应这个任务的下载信息删除
                                            }
                                            DownLoader loader = application.downLoaders.remove((String) msg.obj);//把这个任务的下载器移除
                                            boolean isComplete = isFileComplete(Constant.appFilePath + application.downloadList.
                                                    get(i).appName + ".apk", application.downloadList.
                                                    get(i).md5);
                                            if (isComplete) {
                                                if (loader != null)
                                                    loader.saveDownloadHistory(application.downloadList.get(i));
                                                // 安装apk并发送广播
                                                insatllApp(application.downloadList.get(i).fileId, application.downloadList.get(i).appName);
                                            } else {
                                                File localFile = new File(Constant.appFilePath + application.downloadList.get(i).appName + ".apk");
                                                if (localFile.exists()) {
                                                    localFile.delete();
                                                }
                                                mIntent = new Intent("fileFalse");
                                                mIntent.putExtra("fileId", application.downloadList.get(i).fileId);
                                                sendBroadcast(mIntent);
                                                Toast.makeText(DownloadService.this, application.downloadList.get(i).appName + "下载失败，请重新下载", Toast.LENGTH_SHORT).show();
                                            }

                                            //当前下载完成开启等待任务
                                            startWaitTask();
                                            if (loader != null)
                                                loader.closeDB();
                                            application.downloadList.remove(i);
                                            if (iDownloadListener != null)
                                                iDownloadListener.onDownloadFinish();//通知activity更新UI
                                        }
                                    }
                                }
                            }
                            Log.i("eee", "   completeSize=" + msg.arg2 + "   fileId=" + (String) msg.obj.toString());
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Log.i("eee", "RemoteException: " + e.toString());
                    }
                    break;
                case 2://下载出错时，开启等待任务
                    if (!hasDownloadingTask()) {
                        startWaitTask();
                    }
                    mIntent = new Intent("downloadfailed");
                    mIntent.putExtra("fileId", msg.arg1);
                    sendBroadcast(mIntent);
                    break;
                case 3://网络异常，暂停全部任务
                    String tableExistsSql = "select count(*) as c from sqlite_master where type ='table' and name ='netcut_info'";
                    Cursor cursor = db.rawQuery(tableExistsSql, null);
                    if (cursor.moveToNext()) {
                        int count = cursor.getInt(0);
                        if (count > 0) {//表存在
                            Cursor cutnetCursor = db.rawQuery("select * from netcut_info", null);
                            if (!cutnetCursor.moveToNext()) {
                                if (application.downLoaders != null && application.downLoaders.size() > 0) {
                                    for (DownLoader loader : application.downLoaders.values()) {
                                        String saveSql = "insert into netcut_info(file_id,localUrl,thread_count,downloader_state) values (?,?,?,?)";
                                        Object[] bindArgs = {loader.fileId, loader.localFile, loader.threadCount, loader.getState()};
                                        db.execSQL(saveSql, bindArgs);
                                        loader.pause();
                                    }
                                    try {
                                        if (iDownloadListener != null)
                                            iDownloadListener.onDownloadStateChange();
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
//                            if (application.waitList != null)
//                                application.waitList.clear();
                                } else {
                                    Cursor downloaderCursor = db.rawQuery("select * from downloader_info", null);
                                    if (downloaderCursor.moveToNext()) {
                                        downloaderCursor.close();
                                        db.execSQL("insert into netcut_info select * from downloader_info");
                                    } else {//不知道要不要处理先放着
                                    }
                                }
                            }
                            try {
                                if (iDownloadListener != null)
                                    iDownloadListener.onDownloadStateChange();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    break;
                case 4://恢复网络
                    if (application.isDoRecoverNet) {
                        getDownloaderInfoFromDB(true);
                        if (isAllWait()) {
                            startWaitTask();
                        }
                    }
                    break;
                case 5://网络超时
                    if (msg.obj != null) {
                        String key = msg.obj.toString();
                        if (isNetworkAvailable(DownloadService.this)) {
                            if (!TextUtils.isEmpty(key)) {
                                DownLoader loadder = application.downLoaders.get(key);
                                if (loadder != null && loadder.state == 2) {
                                    loadder.state = 1;
                                    JsonObject object = new JsonObject();
                                    object.addProperty("fileId", Integer.parseInt(key));
                                    object.addProperty("ID", Constant.deviceId);
                                    object.addProperty("vin", Constant.vin);
                                    downloadParams.put("token", Constant.token);
                                    downloadParams.put("data", object.toString());
                                    int filesize = 0;
                                    for (AppListEntity.AppInfo appInfo : application.downloadList) {
                                        if (appInfo.fileId == Integer.parseInt(key)) {
                                            filesize = appInfo.fileSize;
                                            break;
                                        }
                                    }
                                    DownloadInfo info = loadder.getDownloadInfoByFileId(downloadParams, filesize);
                                    if (outTimes == 3) {
                                        Toast.makeText(DownloadService.this, "服务器繁忙，请稍候再试", Toast.LENGTH_SHORT).show();
                                        outTimes = 0;
                                        loadder.pause();
                                        application.downLoaders.put(key, loadder);
                                        if (isNoDownloading()) {
                                            startWaitTask();
                                            try {
                                                if (iDownloadListener != null)
                                                    iDownloadListener.onDownloadStateChange();
                                            } catch (RemoteException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        loadder.downloadByPost(downloadParams);
                                        outTimes++;
                                    }
                                }
                            }
                        } else {
                            Message beyondMsg = Message.obtain();
                            beyondMsg.what = 3;
                            mHandler.sendMessage(beyondMsg);
                        }
                    }
                    break;
            }
        }

    };

    public DownloadService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPre = getSharedPreferences("config",
                MODE_PRIVATE);
        isNormal = sharedPre.getBoolean("isNormal", false);

        File cacheFile = new File("/sdcard/ZhongTai/ApkDownload");
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }
        application = (XxCustomApplication) getApplication();
//        getDeviceInfo();
//        JsonObject object = new JsonObject();
//        object.addProperty("ID", Constant.deviceId);
//        object.addProperty("supplierCode", Constant.supplierCode);
//        object.addProperty("vin", Constant.vin);
//        registerParams.put("data", object.toString());
//        getRegisterData(registerParams);
        File markFile = new File(Environment.getExternalStorageDirectory().getPath() + "/mark.txt");
        if (markFile.exists()) { //切换到测试服务器
            Constant.vin = "LJU70W1Z7FG075386";//先写死一个测试用
            UrlManager.HOST = "http://58.58.205.23:5152/api/v1/";
            UrlManager.DOWNLOADHOST = "http://58.58.205.23:5152/file/v1/";
            Log.i(TAG, "onCreate: mark.txt存在");
        } else {//切换正式服务器
            Log.i(TAG, "onCreate: mark.txt不存在");
            try {
                Constant.vin = EtSDK.getInstance(this).getCanManager().getVehicleVINNumValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            UrlManager.HOST = "http://vis.evcar.com:5151/v1/";
            UrlManager.DOWNLOADHOST = "http://vis.evcar.com:5151/v1/";

            //test
//        Constant.sysVersion = "V174";//先写死一个测试用
//        Constant.sysModel = "YC-DD2000-V7";//先写死一个测试用

            //旧版
//            Constant.vin = CANManager.get(this).getVehicleVINNumValue();
//            UrlManager.HOST = "http://vis.evcar.com:5151/v1/";
//            UrlManager.DOWNLOADHOST = "http://vis.evcar.com:5151/v1/";
        }
        registerReceiver();
        dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();
//        getDownloaderInfoFromDB();
//        checkUpdate();
    }

    /**
     * 获取车机信息
     */
    private void getDeviceInfo() {
        Constant.deviceId = HardVersionInfoManager.GetHardSeralNum().replaceAll("\n", "");
        Constant.sysVersion = SystemProperties.get("ro.build.version");//.substring(Constant.sysVersion.indexOf("RV"));
        Constant.sysModel = SystemProperties.get("ro.product.model").replaceAll(" ", "");
//        Constant.sysVersion = "V174";//先写死一个测试用
//        Constant.sysModel = "YC-DD2000-V7";//写死一个测试用
    }

    /**
     * 车机注册获取令牌
     *
     * @param params
     */
    private void getRegisterData(Map<String, String> params) {
        VolleyRequest.RequestPost(DownloadService.this, UrlManager.getRegisterUrl(),
                UrlManager.TAG, params, new VolleyInterface(DownloadService.this,
                        VolleyInterface.mListener,
                        VolleyInterface.mErrorListener) {
                    @Override
                    public void onSuccessfullyListener(String result) {
                        registerResultEntity = new Gson().fromJson(result.toString(),
                                RegisterResultEntity.class);
                        if (registerResultEntity != null) {
                            if (registerResultEntity.state == 1) {
                                Constant.token = registerResultEntity.data.token;
                            } else if (registerResultEntity.state == 0) {
                                Log.e(TAG, "errCode:" + registerResultEntity.err.errCode +
                                        "====errMsg：" + registerResultEntity.err.errMsg);
                            }
                        }
                    }

                    @Override
                    public void onErrorListener(VolleyError error) {
                        Log.e(TAG, "VolleyError:" + error.toString());
                    }
                });
    }

    @Override
    public IBinder onBind(Intent intent) {
        isFirst = true;
        if (isFirst) {
            getDownloaderInfoFromDB(true);
            isFirst = false;
        }
        return downloadBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        isFirst = true;
        if (isFirst) {
            getDownloaderInfoFromDB(true);
            isFirst = false;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            Bundle bundle = (Bundle) intent.getExtras();
            if (bundle != null) {
                isCheckUpdate = bundle.getBoolean("isCheckUpdate");
                if (isCheckUpdate) {
                    //检查更新
                    timeHandler.postDelayed(runnable, 2000);
                }
            }
        }
        return START_FLAG_REDELIVERY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("TAG", "onDestroy------->");
    }

    IDownloadService.Stub downloadBinder = new IDownloadService.Stub() {
        @Override
        public void start(String fileId, String fileName) throws RemoteException {
            int threadCount = 1;//启动三个线程开始下载
            DownLoader downLoader = application.downLoaders.get(fileId);
            if (downLoader == null) {//如果下载器是空的，表示第一次下载或者下载已完成
                Log.v("TAG", "startDownload------->downLoader==null");
                downLoader = new DownLoader(UrlManager.getDownloadUrl(), fileId, Constant.appFilePath + fileName + ".apk",
                        threadCount, DownloadService.this, mHandler, application); //开始一次下载，初始化一个下载器
                if (application.downLoaders != null) {
                    for (String key : application.downLoaders.keySet()) {
                        if (application.downLoaders.get(key).isDownloading()) {
                            downLoader.state = 4;//置为等待状态
                            if (!application.waitList.contains(fileId)) {
                                application.waitList.add(fileId);//将fileid添加到等待列表
                            }
                        }
                    }
                }
                application.downLoaders.put(fileId, downLoader);//把下载器和标识这个下载器的FileId放进Map
            } else {
                if (application.downLoaders != null) {
                    for (String key : application.downLoaders.keySet()) {
                        if (application.downLoaders.get(key).isDownloading() && application.downLoaders.get(key).state != -1 && application.downLoaders.get(key).fileId != downLoader.fileId) {
                            downLoader.state = 4;//置为等待状态
                            if (!application.waitList.contains(fileId)) {
                                application.waitList.add(fileId);//将fileid添加到等待列表
                            }
                        }
                    }
                }
            }
            if (downLoader.isDownloading() || downLoader.state == -1) {//如果是正在下载，点击下载按钮
                return;
            }
            JsonObject object = new JsonObject();
            object.addProperty("fileId", Integer.parseInt(fileId));
            object.addProperty("ID", Constant.deviceId);
            object.addProperty("vin", Constant.vin);
            downloadParams.put("token", Constant.token);
            downloadParams.put("data", object.toString());
            int filesize = 0;
            for (AppListEntity.AppInfo appInfo : application.downloadList) {
                if (appInfo.fileId == Integer.parseInt(fileId)) {
                    filesize = appInfo.fileSize;
                    break;
                }
            }
            DownloadInfo info = downLoader.getDownloadInfoByFileId(downloadParams, filesize);
//            if (info != null)
//                fileSize = info.getFileSize();
//            showProcessBar(view, info, urlString);

            downLoader.downloadByPost(downloadParams);
            if (iDownloadListener != null)
                iDownloadListener.onDownloadStateChange();
        }

        @Override
        public void pause(String fileId) throws RemoteException {
            application.downLoaders.get(fileId).pause();
            if (iDownloadListener != null)
                iDownloadListener.onDownloadStateChange();
            if (!hasDownloadingTask()) {
                startWaitTask();
            }
        }

        @Override
        public void remove(String fileId) throws RemoteException {
            DownLoader downLoader = application.downLoaders.get(fileId);
            if (downLoader != null) {
                if (downLoader.getState() == 4) {//删除的是等待的任务
                    for (int i = 0; i < application.waitList.size(); i++) {
                        if (application.waitList.get(i).equals(fileId)) {
                            application.waitList.remove(i);
                        }
                    }
                }
                downLoader.deleteInfoByFileId(fileId);//在数据库中把对应这个任务的下载信息删除
                DownLoader loader = application.downLoaders.remove(fileId);//把这个任务的下载器移除
                loader.closeDB();

                String sql = "select count(*) from netcut_info where file_id=?";
                Cursor cursor = db.rawQuery(sql, new String[]{fileId});
                cursor.moveToFirst();
                int count = cursor.getInt(0);
                cursor.close();
                if (count != 0) {
                    db.delete("netcut_info", "file_id=?", new String[]{fileId});
                }
            } else {
                DownLoader tmpDownLoader = new DownLoader(UrlManager.getDownloadUrl(), fileId, Constant.appFilePath + fileId + ".apk",
                        1, DownloadService.this, mHandler, application);
                tmpDownLoader.deleteInfoByFileId(fileId);
                tmpDownLoader = null;
            }
            if (!hasDownloadingTask()) {
                if (isNetworkAvailable(DownloadService.this)) {
                    startWaitTask();
                }
            }
        }

        @Override
        public int getDownloaderState(String fileId) throws RemoteException {
            return (application != null && application.downLoaders != null && application.downLoaders.size() > 0 && fileId != null && application.downLoaders.get(fileId) != null) ? application.downLoaders.get(fileId).getState() : -100;
        }

        @Override
        public boolean isDownloading(String fileId) throws RemoteException {
            return (application != null && application.downLoaders != null && application.downLoaders.size() > 0 && fileId != null && application.downLoaders.get(fileId) != null) ? application.downLoaders.get(fileId).isDownloading() : false;
        }

        @Override
        public void setDownloadListener(IDownloadListener iDownloadListener) throws RemoteException {
            DownloadService.this.iDownloadListener = iDownloadListener;
        }

    };

    /**
     * 静默安装
     *
     * @param filePath
     * @return
     */
    public int installAppSilence(String filePath) {
        /*File file = new File(filePath);
        if (!file.exists())
            return -1;
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.println("chmod 777 " + filePath);
            PrintWriter.println("export LD_LIBRARY_PATH=/vendor/lib:/system/lib");
            PrintWriter.println("pm install -r " + filePath);
//          PrintWriter.println("exit");
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return -1;*/
        File file = new File(filePath);
        if (filePath == null || filePath.length() == 0
                || (file = new File(filePath)) == null || file.length() <= 0
                || !file.exists() || !file.isFile()) {
            return 1;
        }

        String[] args = {"pm", "install", "-r", filePath};
        ProcessBuilder processBuilder = new ProcessBuilder(args);

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result = 0;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            String s;

            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }

            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
            Log.i("installAppSilence", "errorMsg: " + errorMsg.toString());
        } catch (IOException e) {
            e.printStackTrace();
            result = 2;
        } catch (Exception e) {
            e.printStackTrace();
            result = 2;
        } finally {
            try {
                if (successMsg.toString() == null || !successMsg.toString().contains("Success")) {
                    result = 3;
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }

    private void startWaitTask() {
        if (application.waitList == null || application.waitList.size() == 0)
            return;
        DownLoader downLoader = application.downLoaders.get(application.waitList.get(0));//获取等待列表的第一个任务
        if (downLoader == null)
            return;
        downLoader.state = 1;
        JsonObject object = new JsonObject();
        object.addProperty("fileId", Integer.parseInt(application.waitList.get(0)));
        object.addProperty("ID", Constant.deviceId);
        object.addProperty("vin", Constant.vin);
        downloadParams.put("token", Constant.token);
        downloadParams.put("data", object.toString());
        int filesize = 0;
        for (AppListEntity.AppInfo appInfo : application.downloadList) {
            if (appInfo.fileId == Integer.parseInt(application.waitList.get(0))) {
                filesize = appInfo.fileSize;
                break;
            }
        }
        application.waitList.remove(0);

        DownloadInfo info = downLoader.getDownloadInfoByFileId(downloadParams, filesize);
//        if (info != null)
//            fileSize = info.getFileSize();
        downLoader.downloadByPost(downloadParams);
        try {
            if (iDownloadListener != null)
                iDownloadListener.onDownloadStateChange();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void insatllApp(final int fileId, final String fileName) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                mIntent = new Intent("installingApp");
                mIntent.putExtra("fileId", fileId);
                sendBroadcast(mIntent);
                int result = installAppSilence(Constant.appFilePath + fileName + ".apk");
                return result;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                if (integer == 0) {
                    mIntent = new Intent("installSuccess");
                    mIntent.putExtra("fileId", fileId);
                    sendBroadcast(mIntent);
                } else {
                    mIntent = new Intent("installFailed");
                    mIntent.putExtra("fileId", fileId);
                    sendBroadcast(mIntent);
                }
            }
        }.execute();
    }

    private boolean hasDownloadingTask() {
        if (application.downLoaders != null) {
            for (DownLoader loader : application.downLoaders.values()) {
                if (loader.state == 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getDownloaderInfoFromDB(boolean isDelete) {
//        if (isClearWait) {
//            if (application.waitList != null)
//                application.waitList.clear();
//            isClearWait = false;
//        }
        if (application.downloadList == null || application.downloadList.size() == 0) {
            isStartWait = false;//开机时不能去启动等待任务
            DBHelper dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "select * from download_info";
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    AppListEntity.AppInfo downloadInfo = new AppListEntity().new AppInfo();
                    downloadInfo.fileId = cursor.getInt(cursor.getColumnIndex("file_id"));
                    downloadInfo.fileSize = cursor.getInt(cursor.getColumnIndex("end_pos")) + 1;
                    downloadInfo.completeSize = cursor.getInt(cursor.getColumnIndex("compelete_size"));
                    downloadInfo.iconFileId = cursor.getInt(cursor.getColumnIndex("icon_id"));
                    downloadInfo.appName = cursor.getString(cursor.getColumnIndex("file_name"));
                    downloadInfo.appState = cursor.getInt(cursor.getColumnIndex("file_state"));
                    downloadInfo.packName = cursor.getString(cursor.getColumnIndex("package_name"));
                    downloadInfo.md5 = cursor.getString(cursor.getColumnIndex("app_md5"));
                    boolean isHasTask = false;
                    for (AppListEntity.AppInfo appInfo : application.downloadList) {
                        if (appInfo.fileId == downloadInfo.fileId) {
                            isHasTask = true;
                        }
                    }
                    if (!isHasTask) {
                        application.downloadList.add(downloadInfo);
                    }
                }
                cursor.close();
            }
        }
//        if (application.downLoaders == null || application.downLoaders.size() == 0) {
        String tableExistsSql = "select count(*) as c from sqlite_master where type ='table' and name ='netcut_info'";
        Cursor existsCursor = db.rawQuery(tableExistsSql, null);
        Cursor tableHasDataCursor = null;
        if (existsCursor != null) {
            if (existsCursor.moveToNext()) {
                int count = existsCursor.getInt(0);
                if (count > 0) {//表存在
                    String tableHasDataSql = "select * from netcut_info";
                    tableHasDataCursor = db.rawQuery(tableHasDataSql, null);
                }
            }
            existsCursor.close();
        }
        String sql = "";
        if (tableHasDataCursor != null) {
            if (tableHasDataCursor.moveToNext()) {
                tableHasDataCursor.close();
                sql = "select * from netcut_info";
            } else {
                tableHasDataCursor.close();
                sql = "select * from downloader_info";
            }
        }
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                final String fileId = cursor.getString(cursor.getColumnIndex("file_id"));
                final DownLoader downLoader = new DownLoader(UrlManager.getDownloadUrl(), fileId,
                        cursor.getString(cursor.getColumnIndex("localUrl")), cursor.getInt(cursor.getColumnIndex("thread_count")),
                        DownloadService.this, mHandler, application);
                downLoader.state = cursor.getInt(cursor.getColumnIndex("downloader_state"));
                application.downLoaders.put(fileId, downLoader);
                if (downLoader.getState() == 4) {
                    if (application.waitList.size() == 0) {
                        application.waitList.add(fileId);//将fileid添加到等待列表
                    } else {
                        if (!application.waitList.contains(fileId)) {
                            application.waitList.add(fileId);//将fileid添加到等待列表
                        }
                    }
                } else if (downLoader.getState() == 2) {
                    int filesize = 0;
                    for (AppListEntity.AppInfo appInfo : application.downloadList) {
                        if (appInfo.fileId == Integer.parseInt(fileId)) {
                            filesize = appInfo.fileSize;
                            break;
                        }
                    }
                    DownloadInfo info = downLoader.getDownloadInfoByFileId(downloadParams, filesize);

                    if (isNetworkAvailable(DownloadService.this)) {
                        downLoader.state = 3;
                        if (!TextUtils.isEmpty(Constant.token)) {
                            for (String key : application.downLoaders.keySet()) {
                                if (application.downLoaders.get(key).isDownloading() && application.downLoaders.get(key).fileId != downLoader.fileId) {
                                    downLoader.state = 4;//置为等待状态
                                    if (!application.waitList.contains(fileId)) {
                                        application.waitList.add(fileId);//将fileid添加到等待列表
                                    }
                                    break;
                                }
                            }

                            JsonObject object = new JsonObject();
                            object.addProperty("fileId", Integer.parseInt(fileId));
                            object.addProperty("ID", Constant.deviceId);
                            object.addProperty("vin", Constant.vin);
                            downloadParams.put("token", Constant.token);
                            downloadParams.put("data", object.toString());
                            downLoader.downloadByPost(downloadParams);
                            try {
                                if (iDownloadListener != null)
                                    iDownloadListener.onDownloadStateChange();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        } else {
                            new AsyncTask<Void, Void, String>() {
                                @Override
                                protected void onPostExecute(String result) {
                                    registerResultEntity = new Gson().fromJson(result,
                                            RegisterResultEntity.class);
                                    if (registerResultEntity != null) {
                                        if (registerResultEntity.state == 1) {
                                            Constant.token = registerResultEntity.data.token;

                                            for (String key : application.downLoaders.keySet()) {
                                                if (application.downLoaders.get(key).isDownloading()) {
                                                    downLoader.state = 4;//置为等待状态
                                                    if (!application.waitList.contains(fileId)) {
                                                        application.waitList.add(fileId);//将fileid添加到等待列表
                                                    }
                                                    break;
                                                }
                                            }
                                            JsonObject object = new JsonObject();
                                            object.addProperty("fileId", Integer.parseInt(fileId));
                                            object.addProperty("ID", Constant.deviceId);
                                            object.addProperty("vin", Constant.vin);
                                            downloadParams.put("token", Constant.token);
                                            downloadParams.put("data", object.toString());
                                            downLoader.downloadByPost(downloadParams);
                                            try {
                                                if (iDownloadListener != null)
                                                    iDownloadListener.onDownloadStateChange();
                                            } catch (RemoteException e) {
                                                e.printStackTrace();
                                            }

                                        } else if (registerResultEntity.state == 0) {
                                            Log.e(TAG, "errCode:" + registerResultEntity.err.errCode +
                                                    "====errMsg：" + registerResultEntity.err.errMsg);
                                        }
                                    }
                                }

                                @Override
                                protected String doInBackground(Void... voids) {
                                    String result = "";
                                    getDeviceInfo();
                                    JsonObject object = new JsonObject();
                                    object.addProperty("ID", Constant.deviceId);
                                    object.addProperty("supplierCode", Constant.supplierCode);
                                    object.addProperty("vin", Constant.vin);
                                    try {
                                        HttpClient registerClient = new DefaultHttpClient();
                                        HttpPost httpPost = new HttpPost(UrlManager.getRegisterUrl());
                                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                        nameValuePairs.add(new BasicNameValuePair("data", object.toString()));
                                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                                        HttpResponse response = registerClient.execute(httpPost);
                                        HttpEntity entity = response.getEntity();
                                        result = EntityUtils.toString(entity, "utf-8");
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        return null;
                                    }
                                    return result;
                                }
                            }.execute();
                        }
                    } else {
                        Message msg = Message.obtain();
                        msg.what = 3;
                        mHandler.sendMessage(msg);
                    }
                }
            }
            cursor.close();
        }
        if (isStartWait) {
            if (!hasDownloading()) {
                startWaitTask();
            }
        }
        if (isDelete) {
            db.delete("downloader_info", null, null);
            db.delete("netcut_info", null, null);
        }
//        } else {
//            for (String key : application.downLoaders.keySet()) {
//                if (application.downLoaders.get(key).state == 3) {//遍历等待状态的下载器，然后比对数据库，如果数据库是下载态就开启下载
//                    DBHelper dbHelper = new DBHelper(this);
//                    SQLiteDatabase db = dbHelper.getReadableDatabase();
//                    String sql = "select * from downloader_info";
////                    String sql = "select downloader_state from downloader_info where file_id=?";
//                    Cursor cursor = db.rawQuery(sql, null);
////                    Cursor cursor = db.rawQuery(sql, new String[]{key});
//                    while (cursor.moveToNext()) {
//                        String fileId = cursor.getString(cursor.getColumnIndex("file_id"));
//                        int state = cursor.getInt(cursor.getColumnIndex("downloader_state"));
//                        if (fileId.equals(key) && state == 2) {
//                            DownLoader downLoader = new DownLoader(UrlManager.getDownloadUrl(), fileId,
//                                    cursor.getString(cursor.getColumnIndex("localUrl")), cursor.getInt(cursor.getColumnIndex("thread_count")),
//                                    DownloadService.this, mHandler, application);
//                            application.downLoaders.put(fileId, downLoader);
//                            downLoader.state = 3;
//
//                            JsonObject object = new JsonObject();
//                            object.addProperty("fileId", Integer.parseInt(key));
//                            object.addProperty("ID", Constant.deviceId);
//                            object.addProperty("vin", Constant.vin);
//                            downloadParams.put("token", Constant.token);
//                            downloadParams.put("data", object.toString());
//                            int filesize = 0;
//                            for (AppListEntity.AppInfo appInfo : application.downloadList) {
//                                if (appInfo.fileId == Integer.parseInt(key)) {
//                                    filesize = appInfo.fileSize;
//                                    break;
//                                }
//                            }
//                            DownloadInfo info = downLoader.getDownloadInfoByFileId(downloadParams, filesize);
//
//                            downLoader.downloadByPost(downloadParams);
//                            try {
//                                if (iDownloadListener != null)
//                                    iDownloadListener.onDownloadStateChange();
//                            } catch (RemoteException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                    cursor.close();
//                }
//            }
//        }
    }

    private void checkUpdate() {
        Log.d("check", "获取token");
//        //先注册，然后获取所有app列表
        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPostExecute(String result) {
                if (result == null) {
                    return;
                }
                try {
                    registerResultEntity = new Gson().fromJson(result,
                            RegisterResultEntity.class);
                    if (registerResultEntity != null) {
                        if (registerResultEntity.state == 1) {
                            Constant.token = registerResultEntity.data.token;
//                        getDownloaderInfoFromDB(false);
                            getAppListData();
                        } else if (registerResultEntity.state == 0) {
                            Log.e(TAG, "errCode:" + registerResultEntity.err.errCode +
                                    "====errMsg：" + registerResultEntity.err.errMsg);
                        }
                    }
                } catch (Exception e) {
//                    Toast.makeText(DownloadService.this , "服务器返回数据错误"  , Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            protected String doInBackground(Void... voids) {
                String result = "";
                getDeviceInfo();
                JsonObject object = new JsonObject();
                object.addProperty("ID", Constant.deviceId);
                object.addProperty("supplierCode", Constant.supplierCode);
                object.addProperty("vin", Constant.vin);
                try {
                    HttpClient registerClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(UrlManager.getRegisterUrl());
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("data", object.toString()));
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                    HttpResponse response = registerClient.execute(httpPost);
                    HttpEntity entity = response.getEntity();
                    result = EntityUtils.toString(entity, "utf-8");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
                return result;
            }
        }.execute();
    }

    /**
     * 获取App列表
     */
    private void getAppListData() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPostExecute(String result) {
                appListEntity = new Gson().fromJson(result,
                        AppListEntity.class);
                if (appListEntity != null) {
                    if (appListEntity.state == 1) {
                        for (AppListEntity.AppInfo appInfo : appListEntity.data.list) {
                            if (appInfo.isForced == 1) {
                                appIds = appIds + appInfo.id + ",";
                            }
                        }
                        if (appIds != null && appIds.contains(",") && appIds.length() > 0) {
                            appIds = appIds.substring(0, appIds.length() - 1);//把最后一个，干掉
                            //调用检查更新接口
                            getUpdateInfo(appIds);
                        }
                    } else if (appListEntity.state == 0) {
                        Log.e(TAG, "errCode:" + appListEntity.err.errCode +
                                "====errMsg：" + appListEntity.err.errMsg);
                    }
                } else {

                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                String result = "";
                JsonObject object = new JsonObject();
                object.addProperty("cmd", "5002");
                object.addProperty("ID", Constant.deviceId);
                object.addProperty("vin", Constant.vin);
                object.addProperty("sysModel", Constant.sysModel.replaceAll(" ", ""));
                object.addProperty("sysVersion", Constant.sysVersion);//.substring(Constant.sysVersion.indexOf("RV")));
                object.addProperty("updateTime", "");
                object.addProperty("appName", "");
                object.addProperty("typeName", "");
                object.addProperty("isPaging", false);
                try {
                    HttpClient registerClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(UrlManager.getAppUrl());
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("token", Constant.token));
                    nameValuePairs.add(new BasicNameValuePair("data", object.toString()));
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                    HttpResponse response = registerClient.execute(httpPost);
                    HttpEntity entity = response.getEntity();
                    result = EntityUtils.toString(entity, "utf-8");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
                return result;
            }
        }.execute();
    }


    private boolean checkVersion(AppListEntity.AppInfo appInfo) {
        boolean hasUpdata = false;
        List<PackageInfo> appList = getPackageManager().getInstalledPackages(0);
        List<String> pkgList = new ArrayList<String>();
        for (PackageInfo info : appList) {
            pkgList.add(info.packageName);
            if (info.packageName.equals(appInfo.packName)) {
                String localVersion = info.versionName;
                if (localVersion.compareToIgnoreCase(appInfo.version) < 0) {
                    hasUpdata = true;
                } else {
                    hasUpdata = false;
                }
            }
        }
        return hasUpdata;
    }


    private void localInstallAppSilence(final AppListEntity.AppInfo appInfo) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                mIntent = new Intent("installingApp");
                mIntent.putExtra("fileId", appInfo.fileId);
                sendBroadcast(mIntent);
                int result = installAppSilence(Constant.appFilePath + appInfo.appName + ".apk");
                return result;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                if (integer == 0) {//安装成功
                    appInfo.appState = 3;
                    mIntent = new Intent("installSuccess");
                    mIntent.putExtra("fileId", appInfo.fileId);
                    sendBroadcast(mIntent);
                } else {//安装失败
                    appInfo.appState = 0;
                    mIntent = new Intent("installFailed");
                    mIntent.putExtra("fileId", appInfo.fileId);
                    sendBroadcast(mIntent);
                    File file = new File(Constant.appFilePath + appInfo.appName + ".apk");
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }.execute();
    }

    private void getUpdateInfo(final String ids) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPostExecute(String result) {
                appUpdateEntity = new Gson().fromJson(result,
                        AppListEntity.class);
                if (appUpdateEntity != null) {
                    if (appUpdateEntity.state == 1) {
                        Log.d("check", "拿到升级列表了，是：" + result);
                        if (!isNormal) {
                            for (AppListEntity.AppInfo appInfo : application.downloadList) {

                                if (application.downLoaders.get(appInfo.fileId + "") == null) {
                                    DownLoader failDownLoader = new DownLoader(UrlManager.getDownloadUrl(), appInfo.fileId + "", Constant.appFilePath + appInfo.appName + "" + ".apk",
                                            1, DownloadService.this, mHandler, application);
                                    failDownLoader.state = -1;
                                    application.downLoaders.put(appInfo.fileId + "", failDownLoader);
                                }

                            }
                        }
                        SharedPreferences sharedPre = getSharedPreferences("config",
                                MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPre.edit();
                        editor.putBoolean("isNormal", false);
                        editor.commit();

                        searchAndDownload();
                    } else if (appUpdateEntity.state == 0) {
                        Log.e(TAG, "errCode:" + appUpdateEntity.err.errCode +
                                "====errMsg：" + appUpdateEntity.err.errMsg);
                    }
                } else {

                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                String result = "";
                JsonObject object = new JsonObject();
                object.addProperty("cmd", "5004");
                object.addProperty("ID", Constant.deviceId);
                object.addProperty("vin", Constant.vin);
                object.addProperty("sysModel", Constant.sysModel.replaceAll(" ", ""));
                object.addProperty("sysVersion", Constant.sysVersion);//.substring(Constant.sysVersion.indexOf("RV")));
                object.addProperty("updateTime", "");
                object.addProperty("appId", ids);
                try {
                    HttpClient registerClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(UrlManager.getAppUrl());
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("token", Constant.token));
                    nameValuePairs.add(new BasicNameValuePair("data", object.toString()));
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                    HttpResponse response = registerClient.execute(httpPost);
                    HttpEntity entity = response.getEntity();
                    result = EntityUtils.toString(entity, "utf-8");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
                return result;
            }
        }.execute();
    }

    private void searchAndDownload() {
        List<AppListEntity.AppInfo> tempDownloadList = new ArrayList<AppListEntity.AppInfo>();
        List<PackageInfo> appList = getPackageManager().getInstalledPackages(0);
        List<String> pkgList = new ArrayList<String>();
        List<AppListEntity.AppInfo> updatelist = new ArrayList<AppListEntity.AppInfo>();
        for (PackageInfo info : appList) {
            pkgList.add(info.packageName);
        }
        if (appUpdateEntity != null && appUpdateEntity.data != null && appUpdateEntity.data.list != null
                && appUpdateEntity.data.list.size() > 0) {
            for (AppListEntity.AppInfo info : appUpdateEntity.data.list) {
                if (pkgList.contains(info.packName)) {
                    for (PackageInfo localInfo : appList) {
                        if (localInfo.packageName.equals(info.packName)) {
                            String localVersion = localInfo.versionName;
                            if (localVersion.compareToIgnoreCase(info.version) < 0) {
                                updatelist.add(info);
                            }
                        }
                    }
                } else {//本机没安装，还是需要更新的直接添加
                    File file = new File(Constant.appFilePath + info.appName + ".apk");
                    if (file.exists()) {//有下载包 比对下载包的版本比服务器低就升级反之则直接安装
                        PackageManager pm = getPackageManager();
                        PackageInfo packageInfo = pm.getPackageArchiveInfo(Constant.appFilePath + info.appName + ".apk", PackageManager.GET_ACTIVITIES);
                        if (packageInfo != null) {
                            String version = packageInfo.versionName;//得到本机apk包版本信息
                            if (version.compareToIgnoreCase(info.version) < 0) {
                                updatelist.add(info);
                            } else {
                                insatllApp(info.fileId, info.appName);
                            }
                        } else {
                            updatelist.add(info);
                        }
                    } else {//没有下载包直接添加
                        updatelist.add(info);
                    }
                }
            }
            for (AppListEntity.AppInfo appInfo : updatelist) {


                File localFile = new File(Constant.appFilePath + appInfo.appName + ".apk");
                if (localFile.exists() && localFile.length() > 0 &&
                        isFileComplete(localFile.getPath(),
                                appInfo.md5)) {
                    if (checkVersion(appInfo)) {
                        localInstallAppSilence(appInfo);
                        appInfo.appState = 2;
                        continue;
                    }
                }


                boolean hasThisTask = false;
                for (AppListEntity.AppInfo info : application.downloadList) {
                    if (info.fileId == appInfo.fileId) {//任务列表没有这个任务再添加
                        hasThisTask = true;
                    }
                }
                if (!hasThisTask) {
                    application.downloadList.add(appInfo);
                }
            }
            for (AppListEntity.AppInfo info : application.downloadList) {
                tempDownloadList.add(info);
            }
            for (AppListEntity.AppInfo appInfo : tempDownloadList) {
                if (application.downLoaders.get(appInfo.fileId + "") != null
                        && application.downLoaders.get(appInfo.fileId + "").state == 3) {//已有暂停任务不做处理
                } else {
                    try {
                        downloadBinder.start(appInfo.fileId + "", appInfo.appName);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            tempDownloadList.clear();//遍历application.downloadList的时候如果其他的地方删除操作会报ConcurrentModificationException所以借助临时列表
            mIntent = new Intent("refreshlist");
            sendBroadcast(mIntent);
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        ConnectionChangeReceiver myReceiver = new ConnectionChangeReceiver();
        this.registerReceiver(myReceiver, filter);
        IntentFilter trafficLevelsFilter = new IntentFilter("close_music_net");
        TrafficLevelsReceiver trafficLevelsReceiver = new TrafficLevelsReceiver();
        this.registerReceiver(trafficLevelsReceiver, trafficLevelsFilter);
    }

    public class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
                //断网
                Message msg = Message.obtain();
                msg.what = 3;
                mHandler.sendMessage(msg);
//                Toast.makeText(getApplicationContext(),"断网了",0).show();
            } else {
                if (!application.isBeyond || wifiNetInfo.isConnected()) {
                    //有网
                    Message msg = Message.obtain();
                    msg.what = 4;
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    public class TrafficLevelsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = "";
            if (intent != null) {
                action = intent.getAction();
            }
            if (action != null && !TextUtils.isEmpty(action) && action.equals("close_music_net")) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
                    application.isBeyond = true;
                    Message msg = Message.obtain();
                    msg.what = 3;
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo wifiNetInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (!application.isBeyond || wifiNetInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDownloading() {
        boolean result = false;
        for (DownLoader loader : application.downLoaders.values()) {
            if (loader != null && loader.state == 2) {
                result = true;
                return result;
            }
        }
        return result;
    }

    private boolean isAllWait() {
        boolean result = true;
        for (DownLoader loader : application.downLoaders.values()) {
            if (loader != null && loader.state != 4) {
                result = false;
                return result;
            }
        }
        return result;
    }

    private boolean isNoDownloading() {
        boolean result = true;
        for (DownLoader loader : application.downLoaders.values()) {
            if (loader != null && loader.state == 2) {
                result = false;
                return result;
            }
        }
        return result;
    }

    /**
     * 判断文件是否是完整正确可用的
     *
     * @param filePath
     * @param fileMD5
     * @return
     */
    private boolean isFileComplete(String filePath, String fileMD5) {
        boolean isComplete = false;
        String md5 = MD5Util.getFileMD5(filePath);
        if (md5 != null && fileMD5 != null && fileMD5.equals(md5)) {
            isComplete = true;
        }
        return isComplete;
    }
}
