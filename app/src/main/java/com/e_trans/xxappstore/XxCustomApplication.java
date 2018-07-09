package com.e_trans.xxappstore;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.SystemProperties;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.e_trans.xxappstore.constant.Constant;
import com.e_trans.xxappstore.db.DownloadDao;
import com.e_trans.xxappstore.downloadservice.DownLoader;
import com.e_trans.xxappstore.entity.AppListEntity;
import com.e_trans.xxappstore.request.UrlManager;
import com.e_trans.xxappstore.top.XxConfig;
import com.e_trans.xxappstore.top.XxNetManager;
import com.e_trans.xxappstore.top.XxNotificationManager;
import com.e_trans.xxappstore.top.XxOtherSettingManager;
import com.e_trans.xxappstore.top.XxSetting;
import com.e_trans.xxappstore.top.gps.XxGpsManager;
import com.e_trans.xxappstore.utils.CrashHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.com.etrans.etsdk.config.EtSDK;
import cn.com.etrans.etsdk.utils.HardVersionInfoManager;

/**
 * Created by wk on 2016/3/31 0031.
 */
public class XxCustomApplication extends Application {

    public static RequestQueue mRequestQueues;
    public List<AppListEntity.AppInfo> downloadList = Collections.synchronizedList(new ArrayList<AppListEntity.AppInfo>());//正在下载的应用集合(保证线程安全)
    public List<AppListEntity.AppInfo> historyList = new ArrayList<AppListEntity.AppInfo>();//下载历史
    public Map<String, DownLoader> downLoaders = new ConcurrentHashMap<String, DownLoader>();//下载器的Map KEY是fileId(保证线程安全)
    public List<String> waitList = new ArrayList<String>();//等待下载的任务的列表，存的是fileid
    public Map<Integer, Integer> installingAppMap = new ConcurrentHashMap<Integer, Integer>();
    public boolean isBeyond = false;
    public boolean isDoRecoverNet = true;

    @Override
    public void onCreate() {
        super.onCreate();
        //捕捉崩溃异常信息
        CrashHandlers.getInstance().init(this);
        DownloadDao.getInstance().init(this);
        CrashHandler.getInstance().init(this);
        mRequestQueues = Volley.newRequestQueue(getApplicationContext());
        registerBoradcastReceiver();

        //这些参数要放在开机自启的service里面，防止被杀掉在这也拿一次
        Constant.deviceId = HardVersionInfoManager.GetHardSeralNum().replaceAll("\n", "");//2111010030313647373000da5e4fd75369
        Constant.sysVersion = SystemProperties.get("ro.build.version");//.substring(SystemProperties.get("ro.build.version").indexOf("RV"));//YC-ZD2S-HAV100-20180619-RV1.0.0update
        Constant.sysModel = SystemProperties.get("ro.product.model").replaceAll(" ", "");//t3
//        Constant.sysVersion = "V174";//先写死一个测试用
//        Constant.sysModel = "YC-DD2000-V7";
        Constant.sdPath = Environment.getExternalStorageDirectory().getPath();
        Constant.appFilePath = Constant.sdPath + "/ZhongTai/ApkDownload/";

        File imageFile = new File(Constant.sdPath + "/ZhongTai/Image");
        if (!imageFile.exists()) {
            imageFile.mkdirs();
        }
        File cacheFile = new File(Constant.sdPath + "/ZhongTai/Cache");
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }
        File markFile = new File(Environment.getExternalStorageDirectory().getPath() + "/mark.txt");
        if (markFile.exists()) {//切换到测试服务器
            Constant.vin = "LJU70W1Z7FG075386";//先写死一个测试用
            UrlManager.HOST = "http://58.58.205.23:5152/api/v1/";
            UrlManager.DOWNLOADHOST = "http://58.58.205.23:5152/file/v1/";
        } else {//切换正式服务器
//            new Thread(){
//                @Override
//                public void run() {
//                    super.run();
//                    Constant.vin = CANManager.get(XxCustomApplication.this).getVehicleVINNumValue();//++android.jar增加部分
//                }
//            }.start();
            //test1*******************************************************8
//            Constant.vin = "LJU70W1Z7FG075386";//先写死一个测试用
//            Constant.deviceId = "2115010041574d4233520175bb4c408287";
//            Constant.supplierCode = "1440049";
//            Constant.sysVersion = "YC-ZD2S-HV100-20180320-RV2.0.95";
//            Constant.sysModel = "YC-DD2000-V7";
//            UrlManager.HOST = "http://vis.evcar.com:5151/v1/";
//            UrlManager.DOWNLOADHOST = "http://vis.evcar.com:5151/v1/";
            //test1*******************************************************8
            //test2,用新设备，*********************************************************
            //{"state":0,"err":{"errCode":"100","errMsg":"sysModel不合法"}}


            try {
                Constant.vin = EtSDK.getInstance(this).getCanManager().getVehicleVINNumValue();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(Constant.vin == ""){
                Constant.vin = "LJU70W1Z7FG075386";//先写死一个测试用
            }
//            Constant.deviceId = "2111010030313647373000da5e4fd75369";
//            Constant.supplierCode = "1440049";
//            Constant.sysVersion = "YC-ZD2S-HAV100-20180619-RV1.0.0update";
//            Constant.sysModel = "t3";//"sysModel不合法"
            Constant.sysModel = "YC-DD2000-V7";
            UrlManager.HOST = "http://vis.evcar.com:5151/v1/";
            UrlManager.DOWNLOADHOST = "http://vis.evcar.com:5151/v1/";

            //test2,用新设备，*********************************************************


            //新版
//            Constant.vin = EtSDK.getInstance(this).getCanManager().getVehicleVINNumValue();
//            UrlManager.HOST = "http://vis.evcar.com:5151/v1/";
//            UrlManager.DOWNLOADHOST = "http://vis.evcar.com:5151/v1/";


            //旧版
//            Constant.vin = CANManager.get(this).getVehicleVINNumValue();
//            UrlManager.HOST = "http://vis.evcar.com:5151/v1/";
//            UrlManager.DOWNLOADHOST = "http://vis.evcar.com:5151/v1/";
        }
        XxConfig.getInstance().init(this, "EtransUpgrade");
        XxSetting.getInstance().init(this);
        XxNetManager.getInstance().init(this);
        XxNotificationManager.getInstance().init(this);
        XxOtherSettingManager.getInstance().init(this);
        XxGpsManager.getInstance().init(this);
    }

    public static RequestQueue getHttpQueues() {
        return mRequestQueues;
    }

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
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
            if (installingAppMap != null) {
                if (action.equals("installingApp")) {
                    installingAppMap.put(fileId, 2);
                } else if (action.equals("installSuccess")) {
                    if (installingAppMap.containsKey(fileId)) {
                        installingAppMap.remove(fileId);
                    }
                } else if (action.equals("installFailed")) {
                    if (installingAppMap.containsKey(fileId)) {
                        installingAppMap.remove(fileId);
                    }
                }
            }
        }

    };
}
