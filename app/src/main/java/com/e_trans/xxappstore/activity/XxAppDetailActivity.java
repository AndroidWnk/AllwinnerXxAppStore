package com.e_trans.xxappstore.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.e_trans.xxappstore.R;
import com.e_trans.xxappstore.XxCustomApplication;
import com.e_trans.xxappstore.adapter.PicturesAdapter;
import com.e_trans.xxappstore.constant.Constant;
import com.e_trans.xxappstore.entity.AppDetailsEntity;
import com.e_trans.xxappstore.entity.AppListEntity;
import com.e_trans.xxappstore.request.UrlManager;
import com.e_trans.xxappstore.request.VolleyInterface;
import com.e_trans.xxappstore.request.VolleyRequest;
import com.e_trans.xxappstore.utils.ImageLoader;
import com.e_trans.xxappstore.utils.MD5Util;
import com.e_trans.xxappstore.utils.UIUtils;
import com.e_trans.xxappstore.view.CustomGridView;
import com.e_trans.xxappstore.view.MoreTextView;
import com.e_trans.xxdownloadaidl.IDownloadService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * App详情页
 * Created by wk on 2016/3/31 0031.
 */
public class XxAppDetailActivity extends XxBaseActivity implements View.OnClickListener {
    @Bind(R.id.app_icon)
    ImageView appIcon;
    @Bind(R.id.app_name)
    TextView appName;
    @Bind(R.id.app_stars)
    RatingBar appStars;
    @Bind(R.id.app_download_counts)
    TextView appDownloadCounts;
    @Bind(R.id.app_size)
    TextView appSize;
    @Bind(R.id.app_state)
    TextView appState;
    @Bind(R.id.app_update)
    TextView appUpdate;
    @Bind(R.id.app_introduce)
    MoreTextView appIntroduce;
    @Bind(R.id.gv_img)
    CustomGridView gvImg;

    private String TAG = "XxAppDetailActivity";
    private static XxCustomApplication application;
    private Intent mIntent = null;
    private Map<String, String> params = new HashMap<String, String>();
    private AppListEntity.AppInfo appInfo = null;
    private int position = -1;
    private AppDetailsEntity appDetailsEntity = null;
    private DecimalFormat df;
    private ImageLoader imageLoader;
    private IDownloadService downBinder = null;
    private String localVersion = "";
    private PicturesAdapter picturesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_app_details;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        application = (XxCustomApplication) getApplication();
        bindService();
        registerBoradcastReceiver();
        appInfo = (AppListEntity.AppInfo) getIntent().getSerializableExtra("appInfo");
        position = getIntent().getIntExtra("position", -1);
        if (appInfo != null) {
            appName.setText(appInfo.appName);
            appDownloadCounts.setText(formateCount(appInfo.downNum));
            appSize.setText(formateAppSize(appInfo.fileSize));
            appStars.setRating(appInfo.recommendLvl);
            switch (appInfo.appState) {
                case 0:
                    File localFile = new File(Constant.appFilePath + appInfo.appName + ".apk");
                    if (localFile.exists() && localFile.length() > 0 && isFileComplete(localFile.getPath(),appInfo.md5)) {
                        PackageManager pm = getPackageManager();
                        PackageInfo packageInfo = pm.getPackageArchiveInfo(Constant.appFilePath + appInfo.appName + ".apk", PackageManager.GET_ACTIVITIES);
                        if(packageInfo != null){
                            String version=packageInfo.versionName;//得到本机apk包版本信息
                            if(version.compareToIgnoreCase(appInfo.version) < 0){
                                localFile.delete();
                                appState.setText(getString(R.string.text_Uninstalled));
                            }else{
                                appState.setText(getString(R.string.text_Install));
                            }
                        }else{
                            appState.setText(getString(R.string.text_Install));
                        }
                    } else {
                        appState.setText(getString(R.string.text_Uninstalled));
                        if(localFile.exists()){
                            localFile.delete();
                        }
                    }
                    break;
                case 1:
                    appState.setText(getString(R.string.text_Donwloading));
                    break;
                case 2:
                    appState.setText(getString(R.string.text_Installing));
                    break;
                case 3:
                    appState.setText(getString(R.string.text_Installed));
                    break;
            }
            imageLoader = new ImageLoader(XxAppDetailActivity.this, R.drawable.ic_list_img_loading);
            imageLoader.displayImage(appInfo.iconFileId + "", appIcon, true);
            initStatus("应用详情");
            getAppDetailsData();
        }
    }

    @Override
    protected void setListener() {
        appState.setOnClickListener(this);
        appUpdate.setOnClickListener(this);
        gvImg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mIntent = new Intent(XxAppDetailActivity.this, ShowPicturesActivity.class);
                mIntent.putExtra("imgs", (Serializable) appDetailsEntity.data.imgList);
                mIntent.putExtra("position", position);
                startActivity(mIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appInfo != null)
            searchAndResetAppState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindService();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                if (appInfo != null && appInfo.appState == 1) {
                    Intent intent = new Intent();
                    intent.putExtra("position", position);
                    setResult(RESULT_OK, intent);
                }
                finish();
                break;
            case R.id.app_state:
                if(appInfo != null){
                    if (appInfo.appState == 0) {//该应用未安装
                        File localFile = new File(Constant.appFilePath + appInfo.appName + ".apk");
                        if (localFile.exists() && localFile.length() > 0  && isFileComplete(localFile.getPath(),appInfo.md5)) {//有安装包，直接安装
                            mIntent = new Intent("installingApp");
                            mIntent.putExtra("fileId", appInfo.fileId);
                            sendBroadcast(mIntent);
                            appInfo.appState = 2;
                            appState.setText(getString(R.string.text_Installing));
                            //安装app
                            new AsyncTask<Void, Void, Integer>() {
                                @Override
                                protected Integer doInBackground(Void... voids) {
                                    return installAppSilence(Constant.appFilePath + appInfo.appName + ".apk");
                                }

                                @Override
                                protected void onPostExecute(Integer integer) {
                                    super.onPostExecute(integer);
                                    if (integer == 0) {//安装成功
                                        mIntent = new Intent("installSuccess");
                                        mIntent.putExtra("fileId", appInfo.fileId);
                                        sendBroadcast(mIntent);
                                        UIUtils.showToast(XxAppDetailActivity.this, getString(R.string.text_Installation_completed), 2);
                                        appInfo.appState = 3;
                                        appState.setText(getString(R.string.text_Installed));
                                    } else {//安装失败
                                        mIntent = new Intent("installFailed");
                                        mIntent.putExtra("fileId", appInfo.fileId);
                                        sendBroadcast(mIntent);
                                        UIUtils.showToast(XxAppDetailActivity.this, getString(R.string.text_Installation_failed), 2);
                                        File file = new File(Constant.appFilePath + appInfo.appName + ".apk");
                                        if(file.exists()){
                                            file.delete();
                                        }
                                        appInfo.appState = 0;
                                        appState.setText(getString(R.string.text_Uninstalled));
                                    }
                                }
                            }.execute();
                        } else {//无安装包，下载
                            if(localFile.exists()){
                                localFile.delete();
                            }
                            if (isNetworkAvailable(XxAppDetailActivity.this)) {
                                appInfo.appState = 1;
                                appState.setText(getString(R.string.text_Donwloading));
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//
//                                }
//                            }).start();
                                boolean isHasTask = false;
                                for (AppListEntity.AppInfo info : application.downloadList) {
                                    if(info.fileId == appInfo.fileId){
                                        isHasTask = true;
                                    }
                                }
                                if(!isHasTask){
                                    application.downloadList.add(appInfo);
                                }
                                try {
                                    downBinder.start(appInfo.fileId + ""
                                            , appInfo.appName);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if(application.isBeyond){
                                    UIUtils.showToast(XxAppDetailActivity.this, getString(R.string.string_tips_no_traffic), 2);
                                }else{
                                    UIUtils.showToast(XxAppDetailActivity.this, getString(R.string.string_tips_no_network), 2);
                                }
                            }
                        }
                    } else if (appInfo.appState == 3) {
                        //卸载该app
                        uninstallApp(appInfo.packName);
                    }
                }
                break;
            case R.id.app_update:
                File localFile = new File(Constant.appFilePath + appInfo.appName + ".apk");
                if (localFile.exists() && localFile.length() > 0  && isFileComplete(localFile.getPath(),appInfo.md5)) {//有安装包，直接安装
                mIntent = new Intent("installingApp");
                mIntent.putExtra("fileId", appInfo.fileId);
                sendBroadcast(mIntent);
                    appUpdate.setVisibility(View.GONE);
                appInfo.appState = 2;
                appState.setText(getString(R.string.text_Installing));
                //安装app
                new AsyncTask<Void, Void, Integer>() {
                    @Override
                    protected Integer doInBackground(Void... voids) {
                        return installAppSilence(Constant.appFilePath + appInfo.appName + ".apk");
                    }

                    @Override
                    protected void onPostExecute(Integer integer) {
                        super.onPostExecute(integer);
                        if (integer == 0) {//安装成功
                            mIntent = new Intent("installSuccess");
                            mIntent.putExtra("fileId", appInfo.fileId);
                            sendBroadcast(mIntent);
                            UIUtils.showToast(XxAppDetailActivity.this,getString(R.string.text_Installation_completed), 2);
                            appInfo.appState = 3;
                            appState.setText(getString(R.string.text_Installed));
                        } else {//安装失败
                            mIntent = new Intent("installFailed");
                            mIntent.putExtra("fileId", appInfo.fileId);
                            sendBroadcast(mIntent);
                            UIUtils.showToast(XxAppDetailActivity.this, getString(R.string.text_Installation_failed), 2);
                            File file = new File(Constant.appFilePath + appInfo.appName + ".apk");
                            if(file.exists()){
                                file.delete();
                            }
                            appInfo.appState = 0;
                            appState.setText(getString(R.string.text_Uninstalled));
                        }
                    }
                }.execute();
            }else if (isNetworkAvailable(XxAppDetailActivity.this)) {
                    if(appInfo != null){
                        appUpdate.setVisibility(View.GONE);
                        appInfo.appState = 1;
                        appState.setText(getString(R.string.text_Donwloading));
                        application.downloadList.add(appInfo);
                        try {
                            downBinder.start(appInfo.fileId + "", appInfo.appName);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    if(application.isBeyond){
                        UIUtils.showToast(XxAppDetailActivity.this, getString(R.string.string_tips_no_traffic), 2);
                    }else{
                        UIUtils.showToast(XxAppDetailActivity.this, getString(R.string.string_tips_no_network), 2);
                    }
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (appInfo != null && appInfo.appState == 1) {
                Intent intent = new Intent();
                intent.putExtra("position", position);
                setResult(RESULT_OK, intent);
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    /**
     * 获取App详情
     */
    private void getAppDetailsData() {
        JsonObject object = new JsonObject();
        object.addProperty("cmd", "5003");
        if(appInfo != null){
            object.addProperty("appId", appInfo.id);
        }

        params.put("token", Constant.token);
        params.put("data", object.toString());

        VolleyRequest.RequestPost(XxAppDetailActivity.this, UrlManager.getAppUrl(),
                UrlManager.TAG, params, new VolleyInterface(XxAppDetailActivity.this,
                        VolleyInterface.mListener,
                        VolleyInterface.mErrorListener,
                        VolleyInterface.RREQUESTS_STATE_SHOW_ONCE_DIALOG) {
                    @Override
                    public void onSuccessfullyListener(String result) {
                        appDetailsEntity = new Gson().fromJson(result.toString(),
                                AppDetailsEntity.class);
                        if (appDetailsEntity != null) {
                            if (appDetailsEntity.state == 1) {
                                appIntroduce.setText(appDetailsEntity.data.description);
                                picturesAdapter = new PicturesAdapter(XxAppDetailActivity.this, appDetailsEntity.data.imgList);
                                gvImg.setAdapter(picturesAdapter);
                            } else if (appDetailsEntity.state == 0) {
                                Log.e(TAG, "errCode:" + appDetailsEntity.err.errCode +
                                        "====errMsg:" + appDetailsEntity.err.errMsg);
                            }
                        } else {
                            UIUtils.showToast(mContext, getString(R.string.text_Server_Busy), 2);
                        }
                    }

                    @Override
                    public void onErrorListener(VolleyError error) {
                        if (error.toString().contains("NoConnectionError")) {
                            if(error.toString().contains("Network is unreachable")){
                                UIUtils.showToast(mContext,
                                        getString(R.string.string_tips_no_network), 2);
                            }else{
                                UIUtils.showToast(mContext,
                                        getString(R.string.text_Server_Busy), 2);
                            }
                        } else if (error.toString().contains("TimeoutError")) {
                            UIUtils.showToast(mContext,
                                    getString(R.string.string_tips_no_network), 2);
                        }
                    }
                });
    }

    private String formateCount(int count) {
        String counts = "";
        if (count < 10000) {
            counts = count + getString(R.string.string_downloads);
        } else if (count < 100000000) {
            counts = count / 10000 + getString(R.string.string_thousand_download);
        } else {
            String sizeMod = count % 100000000 + "";
            counts = count / 100000000 + "." + sizeMod.charAt(0) + "亿次下载";
        }
        return counts;
    }

    private String formateAppSize(int size) {
        df = new DecimalFormat("0.00");
        String sizeStr = "";
        if (size < 1024) {
            sizeStr = df.format(size) + "B";
        } else if (size < 1024 * 1024) {
            sizeStr = df.format((double) (size) / 1024) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            sizeStr = df.format((double) (size) / (1024 * 1024)) + "MB";
        } else {
            sizeStr = df.format((double) (size) / (1024 * 1024 * 1024)) + "GB";
        }
        return sizeStr;
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
            NetworkInfo mobNetInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
                return false;
            }

            if(!application.isBeyond || wifiNetInfo.isConnected()){
                return true;
            }
        }
        return false;
    }

    private void bindService() {
        Intent intent = new Intent("com.e_trans.xxappstore.service.DOWNLOADSERVICE");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    private void unBindService() {
        try {
            if(downBinder != null)
            downBinder.setDownloadListener(null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unbindService(conn);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downBinder = IDownloadService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private boolean searchAndResetAppState(AppListEntity.AppInfo appInfo) {
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


    private void searchAndResetAppState() {
        List<PackageInfo> appList = getPackageManager().getInstalledPackages(0);
        List<String> pkgList = new ArrayList<String>();
        for (PackageInfo info : appList) {
            pkgList.add(info.packageName);
            if (info.packageName.equals(appInfo.packName)) {
                localVersion = info.versionName;
                if (searchAndResetAppState(appInfo)) {
                    appUpdate.setVisibility(View.VISIBLE);
                    File localFile = new File(Constant.appFilePath + appInfo.appName + ".apk");
                    if (localFile.exists() && localFile.length() > 0 &&
                            isFileComplete(localFile.getPath(), appInfo.md5)
                            ) {
                        appUpdate.setText(getString(R.string.text_Install));
                    }else {
                        appUpdate.setText(getString(R.string.text_Update));
                    }
                } else {
                    appUpdate.setVisibility(View.GONE);
                }
            }
        }
        if(appInfo.appState == 1){
            appUpdate.setVisibility(View.GONE);
        }
        if (pkgList != null && pkgList.size() > 0) {
            if (pkgList.contains(appInfo.packName)) {
                if(appInfo.appState == 1){
                    appState.setText(getString(R.string.text_Donwloading));
                }else{
                    appInfo.appState = 3;
                    appState.setText(getString(R.string.text_Uninstalled));
                }
            }else if(appInfo.appState == 2) {
                appState.setText(getString(R.string.text_Installing));
            } else {
                if (appInfo.appState == 3) {
                    appInfo.appState = 0;
                    File localFile = new File(Constant.appFilePath + appInfo.appName + ".apk");
                    if (localFile.exists() && localFile.length() > 0) {
                        appState.setText(getString(R.string.text_Install));
                    } else {
                        appState.setText(getString(R.string.text_Uninstalled));
                    }
                    appUpdate.setVisibility(View.GONE);
                }
            }
        }
    }

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("deletetask");
        myIntentFilter.addAction("fileFalse");
        myIntentFilter.addAction("downloadfailed");
        myIntentFilter.addAction("installingApp");
        myIntentFilter.addAction("installSuccess");
        myIntentFilter.addAction("installFailed");
        // 注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            int fileId = intent.getIntExtra("fileId", -1);
            if(appInfo == null)
                return;
            if (fileId == appInfo.fileId) {
                if (action.equals("installingApp")) {
                    appInfo.appState = 2;
                } else if (action.equals("installSuccess")) {
                    UIUtils.showToast(XxAppDetailActivity.this, getString(R.string.text_Installation_completed), 2);
                    appInfo.appState = 3;
                } else if (action.equals("installFailed")) {
                    UIUtils.showToast(XxAppDetailActivity.this, getString(R.string.text_Installation_failed), 2);
                    File file = new File(Constant.appFilePath + appInfo.appName + ".apk");
                    if(file.exists()){
                        file.delete();
                    }
                    appInfo.appState = 0;
                } else if (action.equals("deletetask")) {
                    appInfo.appState = 0;
                } else if (action.equals("downloadfailed")) {
                    File localFile = new File(Constant.appFilePath + appInfo.appName + ".apk");
                    if (localFile.exists()) {
                        localFile.delete();
                    }
                    appInfo.appState = 0;
                    UIUtils.showToast(XxAppDetailActivity.this,getString(R.string.text_Server_Busy), 2);
                }else if(action.equals("fileFalse")){
                    appInfo.appState = 0;
//                    UIUtils.showToast(XxAppDetailActivity.this, appInfo.appName+"下载失败，请重新下载", 2);
                }
                switch (appInfo.appState) {
                    case 0:
                        File localFile = new File(Constant.appFilePath + appInfo.appName + ".apk");
                        if (localFile.exists() && localFile.length() > 0) {
                            appState.setText(getString(R.string.text_Install));
                        } else {
                            appState.setText(getString(R.string.text_Uninstalled));
                        }
                        break;
                    case 1:
                        appState.setText(getString(R.string.text_Donwloading));
                        break;
                    case 2:
                        appState.setText(getString(R.string.text_Installing));
                        break;
                    case 3:
                        appState.setText(getString(R.string.text_Installed));
                        break;
                }
            }
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
        if(!file.exists())
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

    /**
     * 卸载App
     *
     * @param packageName
     */
    private void uninstallApp(final String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(uninstallIntent);
    }
    /**
     * 判断文件是否是完整正确可用的
     * @param filePath
     * @param fileMD5
     * @return
     */
    private boolean isFileComplete(String filePath,String fileMD5) {
        boolean isComplete = false;
        String md5 = MD5Util.getFileMD5(filePath);
        if(md5 != null && fileMD5.equals(md5)){
            isComplete = true;
        }
        return isComplete;
    }

    @Override
    public void onUpdateUI(Message msg) {
        refUI(msg);
    }
}
