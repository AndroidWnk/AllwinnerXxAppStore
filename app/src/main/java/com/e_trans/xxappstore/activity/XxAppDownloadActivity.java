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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.e_trans.xxappstore.R;
import com.e_trans.xxappstore.XxCustomApplication;
import com.e_trans.xxappstore.adapter.AppDownloadListAdapter;
import com.e_trans.xxappstore.adapter.AppHistoryListAdapter;
import com.e_trans.xxappstore.adapter.DownloadPagerAdapter;
import com.e_trans.xxappstore.constant.Constant;
import com.e_trans.xxappstore.db.DBHelper;
import com.e_trans.xxappstore.entity.AppListEntity;
import com.e_trans.xxappstore.entity.DownloadUpdateViewEntity;
import com.e_trans.xxappstore.entity.ThreadDownloadInfo;
import com.e_trans.xxappstore.transformer.TransformerFactory;
import com.e_trans.xxappstore.utils.UIUtils;
import com.e_trans.xxdownloadaidl.DownloadInfo;
import com.e_trans.xxdownloadaidl.IDownloadListener;
import com.e_trans.xxdownloadaidl.IDownloadService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * App下载页
 * Created by wk on 2016/3/31 0031.
 */
public class XxAppDownloadActivity extends XxBaseActivity implements View.OnClickListener {

    @Bind(R.id.download_point1)
    ImageView downloadPoint1;
    @Bind(R.id.download_point2)
    ImageView downloadPoint2;
    @Bind(R.id.download_viewPager)
    ViewPager downloadViewPager;

    private XxCustomApplication application;
    private DownloadPagerAdapter mDownloadPagerAdapter;
    private List<View> mDownloadViews;
    private View mDownloadStateView;
    private View mDownloadHistoryView;
    private ListView downloadListView;
    private TextView noTaskHint;
    private ListView downloadHistoryListView;
    private TextView noHistoryHint;

    private IDownloadService downBinder = null;
    private ViewPager.PageTransformer transformer;
    private AppDownloadListAdapter downloadListAdapter;
    private ProgressBar mProgressBar;
    private TextView mProgressPercent;
    private List<AppListEntity.AppInfo> historyList = new ArrayList<AppListEntity.AppInfo>();
    private AppHistoryListAdapter appHistoryListAdapter;
    private int complite_size = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1://更新下载列表的按钮状态
                    if (downloadListAdapter != null)
                        downloadListAdapter.setListData(application.downloadList);
                    break;
                case 2://下载完成
                    Log.i("downloadActivity", "mHandler==>ThreadId=" + Thread.currentThread().getId());
                    complite_size = 0;
                    mProgressBar = null;
                    mProgressPercent = null;
                    if (downloadListAdapter != null)
                        downloadListAdapter.setListData(application.downloadList);
                    if (application.downloadList.size() == 0) {
                        noTaskHint.setVisibility(View.VISIBLE);
                    }
                    historyList = getDownloadHistoryInfo();
                    searchAndResetAppState();
                    if(historyList != null && historyList.size() > 0){
                        for (int i = 0;i < historyList.size(); i++) {
                            if(application.installingAppMap.containsKey(historyList.get(i).fileId)){
                                historyList.get(i).appState = 2;
                            }
                        }
                    }
                    if (appHistoryListAdapter != null)
                        appHistoryListAdapter.setListData(historyList);
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_app_download;
    }

    @Override
    protected void initView() {
        mDownloadStateView = LayoutInflater.from(this).inflate(R.layout.viewpage_download, null);
        mDownloadHistoryView = LayoutInflater.from(this).inflate(R.layout.viewpage_download_history, null);
        downloadListView = (ListView) mDownloadStateView.findViewById(R.id.download_list);
        noTaskHint = (TextView) mDownloadStateView.findViewById(R.id.no_task_hint);
        downloadHistoryListView = (ListView) mDownloadHistoryView.findViewById(R.id.download_history_list);
        noHistoryHint = (TextView) mDownloadHistoryView.findViewById(R.id.no_history_hint);

        mDownloadViews = new ArrayList<View>();
        mDownloadViews.add(mDownloadStateView);
        mDownloadViews.add(mDownloadHistoryView);

        mDownloadPagerAdapter = new DownloadPagerAdapter(mDownloadViews);
        downloadViewPager.setAdapter(mDownloadPagerAdapter);
        downloadViewPager.setCurrentItem(0);
    }

    @Override
    protected void initData() {
        application = (XxCustomApplication) getApplication();
        bindService();
        registerBoradcastReceiver();
        if (application.downloadList.size() == 0) {
            noTaskHint.setVisibility(View.VISIBLE);
        } else {
            noTaskHint.setVisibility(View.GONE);
        }
        transformer = TransformerFactory.getTransformer(TransformerFactory.TRANSFORMER_TYPE.DEPTH);
        downloadViewPager.setPageTransformer(true, transformer);
        historyList = getDownloadHistoryInfo();
        searchAndResetAppState();
        if(historyList != null && historyList.size() > 0){
            for (int i = 0;i < historyList.size(); i++) {
                if(application.installingAppMap.containsKey(historyList.get(i).fileId)){
                    historyList.get(i).appState = 2;
                }
            }
        }
        appHistoryListAdapter = new AppHistoryListAdapter(XxAppDownloadActivity.this, historyList, noHistoryHint);
        downloadHistoryListView.setAdapter(appHistoryListAdapter);
        initStatus("下载任务");

    }

    @Override
    protected void setListener() {
        downloadViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                // TODO Auto-generated method stub
                if (arg0 == 0) {
                    downloadPoint1.setBackgroundResource(R.drawable.page_indicator_focused);
                    downloadPoint2.setBackgroundResource(R.drawable.page_indicator_unfocused);

                    initStatus(getString(R.string.title_download));
                    UIUtils.sendTitleBroadCast(XxAppDownloadActivity.this,getString(R.string.title_download));
                    if (application.downloadList.size() == 0) {
                        noTaskHint.setVisibility(View.VISIBLE);
                    } else {
                        noTaskHint.setVisibility(View.GONE);
                    }
                } else if (arg0 == 1) {
                    downloadPoint1.setBackgroundResource(R.drawable.page_indicator_unfocused);
                    downloadPoint2.setBackgroundResource(R.drawable.page_indicator_focused);
                    initStatus(getString(R.string.title_download_hostory));
                    UIUtils.sendTitleBroadCast(XxAppDownloadActivity.this,getString(R.string.title_download_hostory));
                    if (historyList.size() == 0) {
                        noHistoryHint.setVisibility(View.VISIBLE);
                    } else {
                        noHistoryHint.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }
        });
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
        historyList = getDownloadHistoryInfo();
        if (historyList.size() == 0) {
            noHistoryHint.setVisibility(View.VISIBLE);
        } else {
            noHistoryHint.setVisibility(View.GONE);
        }
        searchAndResetAppState();
        if(historyList != null && historyList.size() > 0){
            for (int i = 0;i < historyList.size(); i++) {
                if(application.installingAppMap.containsKey(historyList.get(i).fileId)){
                    historyList.get(i).appState = 2;
                }
            }
        }
        appHistoryListAdapter.setListData(historyList);
        UIUtils.sendTitleBroadCast(XxAppDownloadActivity.this,getString(R.string.title_download));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindService();
        unregisterReceiver(mBroadcastReceiver);
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
            downloadListAdapter = new AppDownloadListAdapter(XxAppDownloadActivity.this, noTaskHint, application);
            downloadListView.setAdapter(downloadListAdapter);
            downloadListAdapter.setDownBinder(downBinder);
            if (application.downloadList == null || application.downloadList.size() == 0) {
                getDownloadListFromDB();
            }else{
                for (int i = 0; i < application.downloadList.size(); i++) {
                    AppListEntity.AppInfo appInfo = application.downloadList.get(i);
                    if (appInfo.appState == 1){
                        try {
                            downBinder.start(appInfo.fileId+"",appInfo.appName);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            try {
                downBinder.setDownloadListener(new IDownloadListener() {

                    @Override
                    public void onUpdateProgress(int length, String fileId) throws RemoteException {
                        Log.i("fff", "   length=" + length + "   fileId=" + fileId);
                        complite_size = length;
//                        for (int i = 0; i < application.downloadList.size(); i++) {
//                            if (!TextUtils.isEmpty(fileId) && Integer.parseInt(fileId) == application.downloadList.get(i).fileId) {
//                                RelativeLayout layout = (RelativeLayout) downloadListView.getChildAt(i);
//                                if (layout != null) {
//                                    mProgressBar = (ProgressBar) layout.findViewById(R.id.download_progressbar);
//                                    mProgressBar.setMax(application.downloadList.get(i).fileSize);
//                                    mProgressPercent = (TextView) layout.findViewById(R.id.progress_percent);
//                                    Log.i("progress", "layout != null,找到ProgressBar了，fileId="+fileId);
//                                }else{
//                                    Log.i("progress", "layout==null了，fileId="+fileId);
//                                }
//                                break;
//                            }
//                        }
//                        if (mProgressBar != null) {
//                            Log.i("progress", "找到ProgressBar了，fileId="+fileId+"在更新");
//                            mProgressBar.setProgress(complite_size);
//                            if (complite_size == 0) {
//                                mProgressPercent.setText("0%");
//                            } else {
//                                mProgressPercent.setText((int) ((double) Math.abs(complite_size) / Math.abs(mProgressBar.getMax()) * 100) + "%");
//                            }
//                        }else{
//                            Log.i("progress", "ProgressBar==null了，fileId="+fileId);
//                        }
                        if(downloadListAdapter != null){
                            DownloadUpdateViewEntity updateView = downloadListAdapter.getUpdateView(Integer.parseInt(fileId));
                            if(updateView != null){
                                ProgressBar mProgressBar = updateView.getProgressBar();
                                TextView mTextView = updateView.getTextView();
                                if(mProgressBar != null ){
                                    mProgressBar.setProgress(complite_size);
                                }
                                if(mTextView != null ){
                                    if (complite_size == 0) {
                                        mTextView.setText("0%");
                                    }else{
                                        mTextView.setText((int) ((double) Math.abs(complite_size) / Math.abs(mProgressBar.getMax()) * 100) + "%");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onDownloadFinish() throws RemoteException {
                        Log.i("downloadActivity", "onDownloadFinish==>ThreadId=" + Thread.currentThread().getId());
                        Message msg = Message.obtain();
                        msg.what = 2;
                        mHandler.sendMessage(msg);

                    }

                    @Override
                    public void onDownloadStateChange() throws RemoteException {
                        Message msg = Message.obtain();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public IBinder asBinder() {
                        return null;
                    }
                });
                //暂停状态的任务不会继续上已下载量，需查库显示
                for (int i = 0; i < application.downloadList.size(); i++) {
                    final int pos = i;
                    if (application.downLoaders != null && application.downLoaders.size() > 0 &&
                            application.downLoaders.get(application.downloadList.get(i).fileId + "") != null &&
                            !application.downLoaders.get(application.downloadList.get(i).fileId + "").isDownloading()) {
                        DBHelper dbHelper = new DBHelper(XxAppDownloadActivity.this);
                        SQLiteDatabase db = dbHelper.getReadableDatabase();
                        String sql = "select compelete_size from download_info where file_id=?";
                        Cursor cursor = db.rawQuery(sql, new String[]{application.downloadList.get(i).fileId + ""});
                        if (cursor != null) {
                            while (cursor.moveToNext()) {
                                int compeleteSize = cursor.getInt(0);
//                                downloadListView.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        RelativeLayout layout = (RelativeLayout) downloadListView.getChildAt(pos);
//                                        if (layout != null) {
//                                            mProgressBar = (ProgressBar) layout.findViewById(R.id.download_progressbar);
//                                            mProgressPercent = (TextView) layout.findViewById(R.id.progress_percent);
//                                        }
//                                        if (mProgressBar != null) {
//                                            mProgressBar.setProgress(compeleteSize);
//                                            if (compeleteSize == 0) {
//                                                mProgressPercent.setText("0%");
//                                            } else {
//                                                mProgressPercent.setText((int) ((double) Math.abs(compeleteSize) / Math.abs(mProgressBar.getMax()) * 100) + "%");
//                                            }
//                                        }
//                                    }
//                                });
                                if(downloadListAdapter != null){
                                    DownloadUpdateViewEntity updateView = downloadListAdapter.getUpdateView(application.downloadList.get(i).fileId);
                                    if(updateView != null){
                                        ProgressBar mProgressBar = updateView.getProgressBar();
                                        TextView mTextView = updateView.getTextView();
                                        if(mProgressBar != null ){
                                            mProgressBar.setProgress(compeleteSize);
                                        }
                                        if(mTextView != null ){
                                            if (complite_size == 0) {
                                                mTextView.setText("0%");
                                            }else{
                                                mTextView.setText((int) ((double) Math.abs(compeleteSize) / Math.abs(mProgressBar.getMax()) * 100) + "%");
                                            }
                                        }
                                    }
                                }
                            }
                            cursor.close();
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private List<AppListEntity.AppInfo> getDownloadHistoryInfo() {
        DBHelper dbHelper = new DBHelper(this);
        List<AppListEntity.AppInfo> historyList = new ArrayList<AppListEntity.AppInfo>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "select * from download_histroy_info";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                AppListEntity.AppInfo historyInfo = new AppListEntity().new AppInfo();
                historyInfo.fileId = cursor.getInt(1);
                historyInfo.appName = cursor.getString(2);
                historyInfo.fileSize = cursor.getInt(4);
                historyInfo.iconFileId = cursor.getInt(5);
                historyInfo.packName = cursor.getString(6);
                historyInfo.md5 = cursor.getString(7);
                File localFile = new File(cursor.getString(3));
                if (localFile.exists()) {
                    historyList.add(historyInfo);
                } else {
                    db.delete("download_histroy_info", "localUrl=?", new String[]{cursor.getString(3)});
                }
            }
            cursor.close();
        }
        return historyList;
    }

    private void searchAndResetAppState() {
        List<PackageInfo> appList = getPackageManager().getInstalledPackages(0);
        List<String> pkgList = new ArrayList<String>();
        for (PackageInfo info : appList) {
            pkgList.add(info.packageName);
        }
        if (pkgList != null && pkgList.size() > 0) {
            for (int i = 0; i < historyList.size(); i++) {
                if (pkgList.contains(historyList.get(i).packName)) {
                    historyList.get(i).appState = 3;
                } else {
//                    historyList.get(i).appState = 0;
                }
            }
        }
    }

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("installingApp");
        myIntentFilter.addAction("installSuccess");
        myIntentFilter.addAction("installFailed");
        myIntentFilter.addAction("fileFalse");
        myIntentFilter.addAction("downloadfailed");
        // 注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            int fileId = intent.getIntExtra("fileId", -1);
            if (historyList != null) {
                for (int i = 0; i < historyList.size(); i++) {
                    if (fileId == historyList.get(i).fileId) {
                        if (action.equals("installingApp")) {
                            historyList.get(i).appState = 2;
                        } else if (action.equals("installSuccess")) {
                            UIUtils.showToast(XxAppDownloadActivity.this, getString(R.string.text_Installation_completed), 2);
                            historyList.get(i).appState = 3;
                        } else if (action.equals("installFailed")) {
                            UIUtils.showToast(XxAppDownloadActivity.this, getString(R.string.text_Installation_failed), 2);
                            File file = new File(Constant.appFilePath + historyList.get(i).appName + ".apk");
                            if (file.exists()) {
                                file.delete();
                            }
                            historyList.remove(i);
                        }
                        appHistoryListAdapter.setListData(historyList);
                    }
                }
            }
            if (action.equals("downloadfailed")) {
                downloadListAdapter.setListData(application.downloadList);
            }
        }

    };

    private void getDownloadListFromDB() {//退出app再次进入的时候需要查库
        if (application.downloadList != null)
            application.downloadList.clear();
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
                    if (appInfo != null && appInfo.fileId == downloadInfo.fileId) {
                        isHasTask = true;
                    }
                }
                if (!isHasTask) {
                    application.downloadList.add(downloadInfo);
                }
            }
            cursor.close();
        }
        if (downloadListAdapter != null)
            downloadListAdapter.setListData(application.downloadList);
        for (int i = 0; i < application.downloadList.size(); i++) {
            AppListEntity.AppInfo appInfo = application.downloadList.get(i);
            if (appInfo.appState == 1){
                try {
                    downBinder.start(appInfo.fileId+"",appInfo.appName);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onUpdateUI(Message msg) {
        refUI(msg);
    }
}
