package com.e_trans.xxappstore.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.e_trans.xxappstore.R;
import com.e_trans.xxappstore.XxCustomApplication;
import com.e_trans.xxappstore.adapter.AppListAdapter;
import com.e_trans.xxappstore.constant.Constant;
import com.e_trans.xxappstore.db.DBHelper;
import com.e_trans.xxappstore.db.DownloadDao;
import com.e_trans.xxappstore.downloadservice.DownLoader;
import com.e_trans.xxappstore.entity.AppListEntity;
import com.e_trans.xxappstore.entity.RegisterResultEntity;
import com.e_trans.xxappstore.request.UrlManager;
import com.e_trans.xxappstore.request.VolleyInterface;
import com.e_trans.xxappstore.request.VolleyRequest;
import com.e_trans.xxappstore.utils.MD5Util;
import com.e_trans.xxappstore.utils.UIUtils;
import com.e_trans.xxappstore.view.pullableview.PullToRefreshLayout;
import com.e_trans.xxappstore.view.pullableview.PullableGridView;
import com.e_trans.xxdownloadaidl.IDownloadListener;
import com.e_trans.xxdownloadaidl.IDownloadService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * App列表页
 * Created by wk on 2016/3/31 0031.
 */
public class XxAppListActivity extends XxBaseActivity implements View.OnClickListener, PullToRefreshLayout.OnRefreshListener {
    @Bind(R.id.back)
    ImageView back;
    @Bind(R.id.search)
    ImageView search;
    @Bind(R.id.download)
    ImageView download;
    @Bind(R.id.gv_app)
    PullableGridView gvApp;
    @Bind(R.id.app_refresh_view)
    PullToRefreshLayout appRefreshView;
    @Bind(R.id.titleTv)
    TextView titleTv;

    private String TAG = "XxAppListActivity";
    private XxCustomApplication application;
    private Intent mIntent = null;
    private int pagenumber = 1;
    private Map<String, String> registerParams = new HashMap<String, String>();
    private Map<String, String> appParams = new HashMap<String, String>();
    private RegisterResultEntity registerResultEntity = null;
    private AppListEntity appListEntity = null;
    private List<AppListEntity.AppInfo> list = new ArrayList<AppListEntity.AppInfo>();
    private AppListAdapter appListAdapter;
    private IDownloadService downBinder = null;
    private DownloadDao dao = null;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_app_list;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        registerBoradcastReceiver();
        application = (XxCustomApplication) getApplication();
        dao = DownloadDao.getInstance();
        appListAdapter = new AppListAdapter(XxAppListActivity.this, appListEntity, application);
        gvApp.setAdapter(appListAdapter);
        bindService();
//        getAppListData();
        JsonObject object = new JsonObject();
        object.addProperty("ID", Constant.deviceId);
        object.addProperty("supplierCode", Constant.supplierCode);
        object.addProperty("vin", Constant.vin);

        registerParams.put("data", object.toString());
        if (TextUtils.isEmpty(Constant.token)) {
            getRegisterData(registerParams);
        } else {
            getAppListData(true);
        }
    }

    @Override
    protected void setListener() {
        back.setOnClickListener(this);
        search.setOnClickListener(this);
        download.setOnClickListener(this);
        titleTv.setOnClickListener(this);
        appRefreshView.setOnRefreshListener(this);
        gvApp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mIntent = new Intent(XxAppListActivity.this, XxAppDetailActivity.class);
                mIntent.putExtra("appInfo", appListEntity.data.list.get(position));
                mIntent.putExtra("position", position);
                startActivityForResult(mIntent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;
        if (requestCode == 0) {
            int position = data.getIntExtra("position", -1);
            if (position != -1) {
                if (list != null && list.get(position) != null)
                    list.get(position).appState = 1;
                appListAdapter.setListData(appListEntity);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.search:
                mIntent = new Intent(XxAppListActivity.this, XxAppSearchActivity.class);
                startActivity(mIntent);
                break;
            case R.id.download:
                mIntent = new Intent(XxAppListActivity.this, XxAppDownloadActivity.class);
                startActivity(mIntent);
                break;
            case R.id.titleTv:
                Toast.makeText(XxAppListActivity.this, "v2.1.12.35375", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appListEntity != null) {
            searchAndResetAppState();
            appListAdapter.setListData(appListEntity);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindService();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mDeleteBroadcastReceiver);
        saveDownloaderInfo();
    }

    private void getRegisterData(Map<String, String> params) {
        VolleyRequest.RequestPost(XxAppListActivity.this, UrlManager.getRegisterUrl(),
                UrlManager.TAG, params, new VolleyInterface(XxAppListActivity.this,
                        VolleyInterface.mListener,
                        VolleyInterface.mErrorListener,
                        VolleyInterface.RREQUESTS_STATE_SHOW_ONCE_DIALOG) {
                    @Override
                    public void onSuccessfullyListener(String result) { //
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
                        if (TextUtils.isEmpty(Constant.token)) {
                            //重新获取Token
                        } else {
                            getAppListData(true);
                        }
                    }

                    @Override
                    public void onErrorListener(VolleyError error) {
                        if (error.toString().contains("NoConnectionError")) {
                            if (error.toString().contains("Network is unreachable")) {
                                UIUtils.showToast(mContext,
                                        getString(R.string.string_tips_no_network), 2);
                            } else {
                                UIUtils.showToast(mContext,
                                        getString(R.string.text_Server_Busy), 2);
                            }
                        } else if (error.toString().contains("TimeoutError")) {
                            UIUtils.showToast(mContext,
                                    getString(R.string.text_Server_Busy), 2);
                        }
                    }
                });
    }

    /**
     * 获取App列表
     */
    private void getAppListData(boolean isShowProgress) {
        JsonObject object = new JsonObject();
        object.addProperty("cmd", "5002");
        object.addProperty("ID", Constant.deviceId);
        object.addProperty("vin", Constant.vin);
        object.addProperty("sysModel", Constant.sysModel.replaceAll(" ", ""));
        object.addProperty("sysVersion", Constant.sysVersion);// Constant.sysVersion.substring(Constant.sysVersion.indexOf("RV"))
        object.addProperty("updateTime", "");
        object.addProperty("appName", "");
        object.addProperty("typeName", "");
        object.addProperty("isPaging", true);
        object.addProperty("pageNo", pagenumber);
        object.addProperty("pageSize", Constant.pagesize);

        appParams.put("token", Constant.token);
        appParams.put("data", object.toString());
        Log.e(TAG, appParams.toString());
        if (isShowProgress) {
            isLoading = true;
            VolleyRequest.RequestPost(XxAppListActivity.this, UrlManager.getAppUrl(),
                    UrlManager.TAG, appParams, new VolleyInterface(XxAppListActivity.this,
                            VolleyInterface.mListener,
                            VolleyInterface.mErrorListener,
                            VolleyInterface.RREQUESTS_STATE_SHOW_ONCE_DIALOG) {
                        @Override
                        public void onSuccessfullyListener(String result) {
                            Log.i(TAG,result);//{"data":{"totalPage":1,"pageNo":1,"pageSize":10,"totalCount":2,"list":[{"recommendLvl":5,"iconFileId":575,"issueTime":"2016-12-09 15:24:22","appName":"Opera","md5s":"aca44bf6038caae19e93d990332c3220","typeName":"软件","version":"12.2.0.11","isForced":1,"fileSize":3268632,"typeId":2,"id":52,"packName":"com.oupeng.mini.android","downNum":2716,"fileId":574,"md5":"21BC0665F616E6A9499327A97C8C6257"},{"recommendLvl":1,"iconFileId":2524,"issueTime":"2018-6-20 11:40:44","appName":"豆伴","md5s":"7b1478eb1c5e0e6bf8214a3a074bf2b9","typeName":"车载","version":"2.3.22","isForced":1,"fileSize":5186841,"typeId":1,"id":178,"packName":"com.xdy.douban","downNum":171,"fileId":2523,"md5":"AD0B72618E37450420DA4420C654CF95"}]},"state":1}
                            appListEntity = new Gson().fromJson(result.toString(),
                                    AppListEntity.class);
                            if (appListEntity != null) {
                                if (appListEntity.state == 1) {

                                    if (pagenumber == 1) {
                                        appRefreshView
                                                .refreshFinish(PullToRefreshLayout.SUCCEED);
                                    } else {
                                        if (appListEntity.data.list != null
                                                && appListEntity.data.list
                                                .size() > 0) {
                                            appRefreshView
                                                    .loadmoreFinish(PullToRefreshLayout.SUCCEED);
                                        } else {
                                            appRefreshView
                                                    .loadmoreFinish(PullToRefreshLayout.NOMOREDATAS);
                                        }
                                    }
                                    if (appListEntity.data != null
                                            && appListEntity.data.list != null) {
                                        list.addAll(appListEntity.data.list);
                                        appListEntity.data.list.clear();
                                        appListEntity.data.list.addAll(list);
                                        searchAndResetAppState();
                                        appListAdapter.setListData(appListEntity);
                                    }
                                    if (list == null || list.size() == 0) {
                                        UIUtils.showToast(mContext, getString(R.string.string_empty), 2);
                                    } else {
                                        for (int i = 0; i < list.size(); i++) {
                                            Log.e("AppInfo", list.get(i).iconFileId + "");
                                        }
                                    }

                                } else if (appListEntity.state == 0) {
                                    Log.e(TAG, "errCode:" + appListEntity.err.errCode +
                                            "====errMsg：" + appListEntity.err.errMsg);
                                    if (pagenumber == 1) {
                                        appRefreshView
                                                .refreshFinish(PullToRefreshLayout.FAIL);
                                    } else {
                                        appRefreshView
                                                .loadmoreFinish(PullToRefreshLayout.FAIL);
                                    }
                                }
                            } else {
                                if (pagenumber == 1) {
                                    appRefreshView
                                            .refreshFinish(PullToRefreshLayout.FAIL);
                                } else {
                                    appRefreshView
                                            .loadmoreFinish(PullToRefreshLayout.FAIL);
                                }
                                UIUtils.showToast(mContext, getString(R.string.text_Server_Busy), 2);
                            }
                            isLoading = false;
                        }

                        @Override
                        public void onErrorListener(VolleyError error) {
                            if (error.toString().contains("NoConnectionError")) {
                                if (error.toString().contains("Network is unreachable")) {
                                    UIUtils.showToast(mContext,
                                            getString(R.string.string_tips_no_network), 2);
                                } else {
                                    UIUtils.showToast(mContext,
                                            getString(R.string.text_Server_Busy), 2);
                                }
                            } else if (error.toString().contains("TimeoutError")) {
                                UIUtils.showToast(mContext,
                                        getString(R.string.string_tips_no_network), 2);
                            }
                            if (pagenumber == 1) {
                                appRefreshView
                                        .refreshFinish(PullToRefreshLayout.FAIL);
                            } else {
                                appRefreshView
                                        .loadmoreFinish(PullToRefreshLayout.FAIL);
                            }
                            isLoading = false;
                        }
                    });
        } else {
            VolleyRequest.RequestPost(XxAppListActivity.this, UrlManager.getAppUrl(),
                    UrlManager.TAG, appParams, new VolleyInterface(XxAppListActivity.this,
                            VolleyInterface.mListener,
                            VolleyInterface.mErrorListener) {
                        @Override
                        public void onSuccessfullyListener(String result) {
                            appListEntity = new Gson().fromJson(result.toString(),
                                    AppListEntity.class);
                            if (appListEntity != null) {
                                if (appListEntity.state == 1) {

                                    if (pagenumber == 1) {
                                        appRefreshView
                                                .refreshFinish(PullToRefreshLayout.SUCCEED);
                                    } else {
                                        if (appListEntity.data.list != null
                                                && appListEntity.data.list
                                                .size() > 0) {
                                            appRefreshView
                                                    .loadmoreFinish(PullToRefreshLayout.SUCCEED);
                                        } else {
                                            appRefreshView
                                                    .loadmoreFinish(PullToRefreshLayout.NOMOREDATAS);
                                        }
                                    }
                                    if (appListEntity.data != null
                                            && appListEntity.data.list != null) {
                                        list.addAll(appListEntity.data.list);
                                        appListEntity.data.list.clear();
                                        appListEntity.data.list.addAll(list);
                                        searchAndResetAppState();
                                        appListAdapter.setListData(appListEntity);
                                    }
                                    if (list == null || list.size() == 0) {
                                        UIUtils.showToast(mContext, getString(R.string.string_empty), 2);
                                    }

                                } else if (appListEntity.state == 0) {
                                    Log.e(TAG, "errCode:" + appListEntity.err.errCode +
                                            "====errMsg：" + appListEntity.err.errMsg);
                                    if (pagenumber == 1) {
                                        appRefreshView
                                                .refreshFinish(PullToRefreshLayout.FAIL);
                                    } else {
                                        appRefreshView
                                                .loadmoreFinish(PullToRefreshLayout.FAIL);
                                    }
                                }
                            } else {
                                if (pagenumber == 1) {
                                    appRefreshView
                                            .refreshFinish(PullToRefreshLayout.FAIL);
                                } else {
                                    appRefreshView
                                            .loadmoreFinish(PullToRefreshLayout.FAIL);
                                }
                                UIUtils.showToast(mContext, getString(R.string.text_Server_Busy), 2);
                            }
                        }

                        @Override
                        public void onErrorListener(VolleyError error) {
                            if (error.toString().contains("NoConnectionError")) {
                                if (error.toString().contains("Network is unreachable")) {
                                    UIUtils.showToast(mContext,
                                            getString(R.string.string_tips_no_network), 2);
                                } else {
                                    UIUtils.showToast(mContext,
                                            getString(R.string.text_Server_Busy), 2);
                                }
                            } else if (error.toString().contains("TimeoutError")) {
                                UIUtils.showToast(mContext,
                                        getString(R.string.string_tips_no_network), 2);
                            }
                            if (pagenumber == 1) {
                                appRefreshView
                                        .refreshFinish(PullToRefreshLayout.FAIL);
                            } else {
                                appRefreshView
                                        .loadmoreFinish(PullToRefreshLayout.FAIL);
                            }
                        }
                    });
        }
    }

    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        pagenumber = 1;
        list.clear();
        getAppListData(false);
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
        pagenumber++;
        getAppListData(false);
    }

    private void bindService() {
        Intent intent = new Intent("com.e_trans.xxappstore.service.DOWNLOADSERVICE");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    private void unBindService() {
        try {
            if (downBinder != null)
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
            appListAdapter.setDownBinder(downBinder);
//            if (!hasDownloading()) {
//                for (String key : application.downLoaders.keySet()) {
//                    if (application.downLoaders.get(key).state == 3) {
//                        try {
//                            downBinder.start(key
//                                    , application.downLoaders.get(key).localFile.substring(application.downLoaders.get(key).localFile.lastIndexOf("/")));
//                        } catch (RemoteException e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    }
//                }
//            }
            try {
                downBinder.setDownloadListener(new IDownloadListener() {
                    @Override
                    public void onUpdateProgress(int length, String fileId) throws RemoteException {

                    }

                    @Override
                    public void onDownloadFinish() throws RemoteException {

                    }

                    @Override
                    public void onDownloadStateChange() throws RemoteException {

                    }

                    @Override
                    public IBinder asBinder() {
                        return null;
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void searchAndResetAppState() {
        if (appListEntity == null || appListEntity.data == null || appListEntity.data.list == null)
            return;

        List<PackageInfo> appList = getPackageManager().getInstalledPackages(0);
        List<String> pkgList = new ArrayList<String>();
        for (PackageInfo info : appList) {
            pkgList.add(info.packageName);
        }
        if (pkgList != null && pkgList.size() > 0) {
            for (int i = 0; i < appListEntity.data.list.size(); i++) {
                AppListEntity.AppInfo info = appListEntity.data.list.get(i);
                if (info.appState == 2) {
                    return;
                }
                if (pkgList.contains(info.packName)) {
                    if (info.appState != 1) {
                        info.appState = 3;
                    }
                    File localFile = new File(Constant.appFilePath + appListEntity.data.list.get(i).appName + ".apk");
                    if (localFile.exists() && localFile.length() > 0 &&
                            isFileComplete(localFile.getPath(), appListEntity.data.list.get(i).md5) &&
                            searchAndResetAppState(i)) {
                        info.appState = 0;
                    }
                } else {
                    if (info.appState == 3) {
                        info.appState = 0;
                    }
//                    appListEntity.data.list.get(i).appState = 0;
                }
                if (!dao.unhasInfoByFileId(info.fileId + "")) {
                    info.appState = 1;
                }
            }
        }
    }

    private boolean searchAndResetAppState(int position) {
        boolean hasUpdata = false;
        List<PackageInfo> appList = getPackageManager().getInstalledPackages(0);
        List<String> pkgList = new ArrayList<String>();
        for (PackageInfo info : appList) {
            pkgList.add(info.packageName);
            if (info.packageName.equals(appListEntity.data.list.get(position).packName)) {
                String localVersion = info.versionName;
                if (localVersion.compareToIgnoreCase(appListEntity.data.list.get(position).version) < 0) {
                    hasUpdata = true;
                } else {
                    hasUpdata = false;
                }
            }
        }
        return hasUpdata;
    }

    private boolean isFileComplete(String filePath, String fileMD5) {
        boolean isComplete = false;
        String md5 = MD5Util.getFileMD5(filePath);
        if (md5 != null && fileMD5.equals(md5)) {
            isComplete = true;
        }
        return isComplete;
    }

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        IntentFilter myDeleteIntentFilter = new IntentFilter();
        myIntentFilter.addAction("deletetask");
        myIntentFilter.addAction("refreshlist");
        myIntentFilter.addAction("fileFalse");
        myIntentFilter.addAction("downloadfailed");
        myIntentFilter.addAction("installingApp");
        myIntentFilter.addAction("installSuccess");
        myIntentFilter.addAction("installFailed");
        myDeleteIntentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        myDeleteIntentFilter.addDataScheme("package");
        // 注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
        registerReceiver(mDeleteBroadcastReceiver, myDeleteIntentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            int fileId = intent.getIntExtra("fileId", -1);
            if (appListEntity == null || appListEntity.data == null || appListEntity.data.list == null)
                return;
            for (int i = 0; i < appListEntity.data.list.size(); i++) {
                if (fileId == appListEntity.data.list.get(i).fileId) {
                    if (action.equals("installingApp")) {
                        appListEntity.data.list.get(i).appState = 2;
                    } else if (action.equals("installSuccess")) {
                        UIUtils.showToast(XxAppListActivity.this, getString(R.string.text_Installation_completed), 2);
                        appListEntity.data.list.get(i).appState = 3;
                    } else if (action.equals("installFailed")) {
                        UIUtils.showToast(XxAppListActivity.this, getString(R.string.text_Installation_failed), 2);
                        File file = new File(Constant.appFilePath + appListEntity.data.list.get(i).appName + ".apk");
                        if (file.exists()) {
                            file.delete();
                        }
                        appListEntity.data.list.get(i).appState = 0;
                    } else if (action.equals("deletetask")) {
                        appListEntity.data.list.get(i).appState = 0;
                    } else if (action.equals("downloadfailed")) {
                        File localFile = new File(Constant.appFilePath + appListEntity.data.list.get(i).appName + ".apk");
                        if (localFile.exists()) {
                            localFile.delete();
                        }
                        appListEntity.data.list.get(i).appState = 0;
                        UIUtils.showToast(XxAppListActivity.this, getString(R.string.text_Server_Busy), 2);
                    } else if (action.equals("fileFalse")) {
                        appListEntity.data.list.get(i).appState = 0;
//                        UIUtils.showToast(XxAppListActivity.this, appListEntity.data.list.get(i).appName+"下载失败，请重新下载", 2);
                    }
                    searchAndResetAppState();
                    appListAdapter.setListData(appListEntity);
                }
                if (action.equals("refreshlist")) {
                    for (AppListEntity.AppInfo appInfo : application.downloadList) {
                        if (appInfo != null && appInfo.fileId == appListEntity.data.list.get(i).fileId) {
                            appListEntity.data.list.get(i).appState = 1;
                        }
                    }
                    searchAndResetAppState();
                    appListAdapter.setListData(appListEntity);
                }
            }
        }

    };
    private BroadcastReceiver mDeleteBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                searchAndResetAppState();
            }
        }

    };

    private void saveDownloaderInfo() {
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String tableExistsSql = "select count(*) as c from sqlite_master where type ='table' and name ='downloader_info'";
        Cursor cursor = db.rawQuery(tableExistsSql, null);
        if (cursor.moveToNext()) {
            int count = cursor.getInt(0);
            if (count > 0) {//表存在
                cursor.close();
                for (AppListEntity.AppInfo appInfo : application.downloadList) {
                    if (appInfo != null) {
                        DownLoader loader = application.downLoaders.get(appInfo.fileId + "");
                        if (loader != null) {
                            String saveSql = "insert into downloader_info(file_id,localUrl,thread_count,downloader_state) values (?,?,?,?)";
                            Object[] bindArgs = {loader.fileId, loader.localFile, loader.threadCount, loader.getState()};
                            db.execSQL(saveSql, bindArgs);
                            if (loader.isDownloading()) {
                                loader.pause();
                            }
                        }
                    }
                }
//                for (DownLoader loader : application.downLoaders.values()) {
//                    String saveSql = "insert into downloader_info(file_id,localUrl,thread_count,downloader_state) values (?,?,?,?)";
//                    Object[] bindArgs = {loader.fileId, loader.localFile, loader.threadCount, loader.getState()};
//                    db.execSQL(saveSql, bindArgs);
//                    if (loader.isDownloading()) {
//                        loader.pause();
//                    }
//                }
            }
        }
    }

    @Override
    public void onUpdateUI(Message msg) {

    }

//    private boolean hasDownloading() {
//        boolean result = false;
//        for (DownLoader loader : application.downLoaders.values()) {
//            if (loader.state == 2) {
//                result = true;
//            }
//        }
//        return result;
//    }


    @Override
    public void onBackPressed() {
        if (!isLoading)
            super.onBackPressed();
        Log.e("onBackPressed", "----------onBackPressed--------");
    }
}
