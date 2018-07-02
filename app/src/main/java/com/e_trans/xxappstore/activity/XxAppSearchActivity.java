package com.e_trans.xxappstore.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.e_trans.xxappstore.R;
import com.e_trans.xxappstore.XxCustomApplication;
import com.e_trans.xxappstore.adapter.AppListAdapter;
import com.e_trans.xxappstore.adapter.AppSearchAdapter;
import com.e_trans.xxappstore.constant.Constant;
import com.e_trans.xxappstore.db.DownloadDao;
import com.e_trans.xxappstore.entity.AppListEntity;
import com.e_trans.xxappstore.entity.RegisterResultEntity;
import com.e_trans.xxappstore.request.UrlManager;
import com.e_trans.xxappstore.request.VolleyInterface;
import com.e_trans.xxappstore.request.VolleyRequest;
import com.e_trans.xxappstore.utils.MD5Util;
import com.e_trans.xxappstore.utils.UIUtils;
import com.e_trans.xxappstore.view.ClearEditText;
import com.e_trans.xxappstore.view.pullableview.PullToRefreshLayout;
import com.e_trans.xxappstore.view.pullableview.PullableListView;
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
 * App搜索页
 * Created by wk on 2016/3/31 0031.
 */
public class XxAppSearchActivity extends XxBaseActivity implements View.OnClickListener, PullToRefreshLayout.OnRefreshListener {
    @Bind(R.id.back)
    ImageView back;
    @Bind(R.id.index_search)
    ClearEditText indexSearch;
    @Bind(R.id.app_list)
    PullableListView appList;
    @Bind(R.id.app_refresh_view)
    PullToRefreshLayout appRefreshView;

    private String TAG = "XxAppSearchActivity";
    private XxCustomApplication application;
    private Intent mIntent = null;
    private int pagenumber = 1;
    private Map<String, String> registerParams = new HashMap<String, String>();
    private Map<String, String> appParams = new HashMap<String, String>();
    private RegisterResultEntity registerResultEntity = null;
    private AppListEntity appListEntity = null;
    private List<AppListEntity.AppInfo> list = new ArrayList<AppListEntity.AppInfo>();
    private AppSearchAdapter appSearchAdapter;
    private String keyWords = "";//关键字
    private IDownloadService downBinder = null;
    private DownloadDao dao = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_app_search;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        registerBoradcastReceiver();
        application = (XxCustomApplication) getApplication();
        dao = DownloadDao.getInstance();
        appSearchAdapter = new AppSearchAdapter(XxAppSearchActivity.this, appListEntity, application);
        appList.setAdapter(appSearchAdapter);
        bindService();
    }

    @Override
    protected void setListener() {
        back.setOnClickListener(this);
        appRefreshView.setOnRefreshListener(this);

        indexSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_SEND) {
                    keyWords = indexSearch.getText().toString().trim();
                    if (TextUtils.isEmpty(keyWords)) {
                        UIUtils.showToast(XxAppSearchActivity.this, getString(R.string.text_enter_keyword), 2);
                        return true;
                    } else {
                        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(XxAppSearchActivity.this.getCurrentFocus().getWindowToken()
                                        , InputMethodManager.HIDE_NOT_ALWAYS);
                        pagenumber = 1;
                        list.clear();
                        getAppListData(true);
                    }
                    return true;
                }
                return false;
            }
        });
        appList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mIntent = new Intent(XxAppSearchActivity.this, XxAppDetailActivity.class);
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
            if(position != -1){
                if(appListEntity != null && appListEntity.data != null && appListEntity.data.list != null
                        && appListEntity.data.list.get(position) != null)
                appListEntity.data.list.get(position).appState = 1;
                appSearchAdapter.setListData(appListEntity);
            }
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appListEntity != null) {
            searchAndResetAppState();
            appSearchAdapter.setListData(appListEntity);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindService();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mDeleteBroadcastReceiver);
    }

    /**
     * 获取App列表
     */
    private void getAppListData(boolean isShowProgress) {
        if (isNetworkAvailable(XxAppSearchActivity.this)) {
            JsonObject object = new JsonObject();
            object.addProperty("cmd", "5002");
            object.addProperty("ID", Constant.deviceId);
            object.addProperty("vin", Constant.vin);
            object.addProperty("sysModel", Constant.sysModel.replaceAll(" ",""));
            object.addProperty("sysVersion", Constant.sysVersion);//.substring(Constant.sysVersion.indexOf("RV")));
            object.addProperty("updateTime", "");
            object.addProperty("appName", keyWords);
            object.addProperty("typeName", "");
            object.addProperty("isPaging", true);
            object.addProperty("pageNo", pagenumber);
            object.addProperty("pageSize", Constant.pagesize);

            appParams.put("token", Constant.token);
            appParams.put("data", object.toString());
            if (isShowProgress) {
                VolleyRequest.RequestPost(XxAppSearchActivity.this, UrlManager.getAppUrl(),
                        UrlManager.TAG, appParams, new VolleyInterface(XxAppSearchActivity.this,
                                VolleyInterface.mListener,
                                VolleyInterface.mErrorListener,
                                VolleyInterface.RREQUESTS_STATE_SHOW_ONCE_DIALOG) {
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
                                            if (appListEntity.data.list
                                                    .size() > 0) {
                                                list.addAll(appListEntity.data.list);
                                                appListEntity.data.list.clear();
                                                appListEntity.data.list.addAll(list);
                                                if (appListEntity != null)
                                                    searchAndResetAppState();
                                                if(appListEntity.data != null && appListEntity.data.list != null && appListEntity.data.list.size() > 0){
                                                    for (int i = 0;i < appListEntity.data.list.size(); i++) {
                                                        if(application.installingAppMap.containsKey(appListEntity.data.list.get(i).fileId)){
                                                            appListEntity.data.list.get(i).appState = 2;
                                                        }
                                                    }
                                                }
                                                appSearchAdapter.setListData(appListEntity);
                                            } else {
                                                if (appListEntity != null)
                                                    searchAndResetAppState();
                                                appSearchAdapter.setListData(appListEntity);
                                                UIUtils.showToast(mContext, getString(R.string.text_no_data), 2);
                                            }
                                        }
                                    } else if (appListEntity.state == 0) {
                                        Log.e(TAG, "errCode:" + appListEntity.err.errCode +
                                                "====errMsg:" + appListEntity.err.errMsg);
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
                                    UIUtils.showToast(mContext,getString(R.string.text_Server_Busy), 2);
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
                                if (pagenumber == 1) {
                                    appRefreshView
                                            .refreshFinish(PullToRefreshLayout.FAIL);
                                } else {
                                    appRefreshView
                                            .loadmoreFinish(PullToRefreshLayout.FAIL);
                                }
                            }
                        });
            } else {
                VolleyRequest.RequestPost(XxAppSearchActivity.this, UrlManager.getAppUrl(),
                        UrlManager.TAG, appParams, new VolleyInterface(XxAppSearchActivity.this,
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
                                            if (appListEntity.data.list
                                                    .size() > 0) {
                                                list.addAll(appListEntity.data.list);
                                                appListEntity.data.list.clear();
                                                appListEntity.data.list.addAll(list);
                                                if (appListEntity != null)
                                                    searchAndResetAppState();
                                                appSearchAdapter.setListData(appListEntity);
                                            } else {
                                                if (appListEntity != null)
                                                    searchAndResetAppState();
                                                appSearchAdapter.setListData(appListEntity);
                                                UIUtils.showToast(mContext, getString(R.string.text_no_data), 2);
                                            }
                                        }
                                    } else if (appListEntity.state == 0) {
                                        Log.e(TAG, "errCode:" + appListEntity.err.errCode +
                                                "====errMsg:" + appListEntity.err.errMsg);
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
        } else {
            UIUtils.showToast(XxAppSearchActivity.this,getString(R.string.string_tips_no_network), 2);
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
            appSearchAdapter.setDownBinder(downBinder);
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
                if(info.appState == 2) {
                    return ;
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
                if(!dao.unhasInfoByFileId(appListEntity.data.list.get(i).fileId+"")){
                    appListEntity.data.list.get(i).appState = 1;
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

    private boolean isFileComplete(String filePath,String fileMD5) {
        boolean isComplete = false;
        String md5 = MD5Util.getFileMD5(filePath);
        if(md5 != null && fileMD5.equals(md5)){
            isComplete = true;
        }
        return isComplete;
    }
    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        IntentFilter myDeleteIntentFilter = new IntentFilter();
        myIntentFilter.addAction("deletetask");
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
                        UIUtils.showToast(XxAppSearchActivity.this, getString(R.string.text_Installation_completed), 2);
                        appListEntity.data.list.get(i).appState = 3;
                    } else if (action.equals("installFailed")) {
                        UIUtils.showToast(XxAppSearchActivity.this, getString(R.string.text_Installation_failed), 2);
                        File file = new File(Constant.appFilePath + appListEntity.data.list.get(i).appName + ".apk");
                        if(file.exists()){
                            file.delete();
                        }
                        appListEntity.data.list.get(i).appState = 0;
                    } else if (action.equals("deletetask")) {
                        appListEntity.data.list.get(i).appState = 0;
                    } else if (action.equals("downloadfailed")) {
                        File localFile = new File(Constant.appFilePath + appListEntity.data.list.get(i).appName + ".apk");
                        if(localFile.exists()){
                            localFile.delete();
                        }
                        appListEntity.data.list.get(i).appState = 0;
                        UIUtils.showToast(XxAppSearchActivity.this, getString(R.string.text_Server_Busy), 2);
                    }else if(action.equals("fileFalse")){
                        appListEntity.data.list.get(i).appState = 0;
//                        UIUtils.showToast(XxAppSearchActivity.this, appListEntity.data.list.get(i).appName+"下载失败，请重新下载", 2);
                    }
                    searchAndResetAppState();
                    appSearchAdapter.setListData(appListEntity);
                }
            }
            if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                searchAndResetAppState();
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
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onUpdateUI(Message msg) {

    }
}
