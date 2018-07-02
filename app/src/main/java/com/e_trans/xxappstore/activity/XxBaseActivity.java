package com.e_trans.xxappstore.activity;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.e_trans.xxappstore.R;
import com.e_trans.xxappstore.top.MobileParams;
import com.e_trans.xxappstore.top.XxBaseModule;
import com.e_trans.xxappstore.top.XxMessage;
import com.e_trans.xxappstore.top.XxNetManager;
import com.e_trans.xxappstore.top.XxNotificationManager;
import com.e_trans.xxappstore.top.XxOtherSettingManager;
import com.e_trans.xxappstore.top.XxTelephoneManager;
import com.e_trans.xxappstore.top.gps.XxGpsManager;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;

/**
 * 自定义基类
 * Created by wk on 2016/3/31 0031.
 */
public abstract class XxBaseActivity extends Activity implements XxBaseModule.IUpdateUI{

    protected LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (getLayout() != 0) {
            View view = mInflater.inflate(getLayout(), null);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(view);
            setBackground();
            registerWallpaperChangedBoradcastReceiver();
            ButterKnife.bind(this);
            initView();
            initData();
            setListener();
        }
    }

    /**
     * 初始布局
     */
    protected abstract int getLayout();

    /**
     * 初始化控件
     */
    protected abstract void initView();

    /**
     * 初始化数据
     */
    protected abstract void initData();

    /**
     * 设置监听
     */
    protected abstract void setListener();

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(changeBgBroadcastReceiver);
    }

    protected void setBackground() {
        WallpaperManager wallpaperManager = WallpaperManager
                .getInstance(getApplicationContext());
        // 获取当前壁纸
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        // 将Drawable,转成Bitmap
        Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0).setBackground(new BitmapDrawable(bm));
    }

    public void registerWallpaperChangedBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("com.link.xxlaucher.systembackground");
        myIntentFilter.addAction(Intent.ACTION_WALLPAPER_CHANGED);
//        myIntentFilter.addDataScheme("package");
        // 注册广播
        registerReceiver(changeBgBroadcastReceiver, myIntentFilter);
    }

    private BroadcastReceiver changeBgBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (action.equals("com.link.xxlaucher.systembackground") || action.equals(Intent.ACTION_WALLPAPER_CHANGED)) {
                setBackground();
            }
        }

    };

    private TextView mCurrentTime;
    private ImageView mWifiSignal;
    private ImageView mMobileState;
    private ImageView mMobileSignal;
    private ImageView mMobileType;
    private ImageView mGpsState;

    private ImageView mBlueToothState;
    private ImageView mSoundState;

    private TextView mCurrentDate;


    public void initStatus(String title) {
        mCurrentTime = (TextView) findViewById(R.id.notification_time);
        mCurrentDate = (TextView) findViewById(R.id.notification_date);
        mWifiSignal = (ImageView) findViewById(R.id.notification_wifi_signal);
        mMobileSignal = (ImageView) findViewById(R.id.notification_mobile_signal);
        mMobileType = (ImageView) findViewById(R.id.notification_mobile_type);
        mGpsState = (ImageView) findViewById(R.id.notification_gps_state);
        mBlueToothState = (ImageView) findViewById(R.id.notification_bluetooth_state);
        mSoundState = (ImageView) findViewById(R.id.notification_sound_state);
        TextView mTitle = (TextView) findViewById(R.id.tv_title);
        mTitle.setText(title);
        Button mBack = (Button) findViewById(R.id.go_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initStatusData();
    }


    public void initStatusData() {
        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm");//设置日期格式
        String time = time_format.format(new Date());
        mCurrentTime.setText(time);
        int wifiSignal = XxNetManager.getInstance().getWifiSignal();
        refreshWifiSignal(wifiSignal);
        refreshWifi(XxNetManager.getInstance().getWifiManager().getWifiStatus());
        refreshGpsState(XxGpsManager.getInstance().getGpsState());

        updateMobileSignal(XxNetManager.getInstance().getTelephoneManager().getServiceState(), XxNetManager.getInstance().getTelephoneManager().getLevel());


        refreshBlueToothState(false);
        refreshMobileData(XxNetManager.getInstance().getTelephoneManager().getServiceState());
        refreshSoundState(XxOtherSettingManager.getInstance().isMute());

        XxNotificationManager.getInstance().addUpdateUIListener(this);
        XxNetManager.getInstance().addUpdateUIListener(this);

        XxOtherSettingManager.getInstance().addUpdateUIListener(this);
    }
    public void refUI(Message msg) {
        if (msg.what == XxMessage.MSG_NOTIFICATION_TIME) {
            SimpleDateFormat time_format = new SimpleDateFormat("HH:mm");//设置日期格式
            String time = time_format.format(new Date());
            mCurrentTime.setText(time);

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日 EEEE");//设置日期格式
            String date = dateFormat.format(new Date());
            mCurrentDate.setText(date);
            XxNetManager.getInstance().getTelephoneManager().getNetworkClass();
            refreshMobileData(XxNetManager.getInstance().getTelephoneManager().getServiceState());
            updateMobileSignal(XxNetManager.getInstance().getTelephoneManager().getServiceState(), XxNetManager.getInstance().getTelephoneManager().getLevel());
            int wifiSignal = XxNetManager.getInstance().getWifiSignal();
            refreshWifiSignal(wifiSignal);
        } else if (msg.what == XxMessage.MSG_NOTIFICATION_MOBILE) {
            MobileParams params = (MobileParams) msg.obj;
            updateMobileSignal(params.serviceState, params.level);
        } else if (msg.what == XxMessage.MSG_SETTING_RE_MOBILE_DATA) {
            refreshMobileData(XxNetManager.getInstance().getTelephoneManager().getServiceState());
        } else if (msg.what == XxMessage.MSG_SETTING_RE_WIFI_AND_AP) {
            refreshWifi(msg.arg1);
        } else if (msg.what == XxMessage.MSG_NOTIFICATION_GPS_STATE) {
            refreshGpsState(msg.arg1);
        } else if (msg.what == XxMessage.MSG_BTEARPHONE_OFF) {
            refreshBlueToothState(false);
        } else if (msg.what == XxMessage.MSG_BTEARPHONE_ON) {
            refreshBlueToothState(true);
        } else if (msg.what == XxMessage.MSG_SETTING_RE_VOICE) {
            if (msg.arg1 == AudioManager.STREAM_MUSIC)
                refreshSoundState( (Boolean)msg.obj);
        }
    }

    private void refreshSoundState(boolean state) {
        if (mSoundState == null) {
            return;
        }
        if (state)
            mSoundState.setBackgroundResource(R.drawable.icon_sound_off);
        else
            mSoundState.setBackgroundResource(R.drawable.icon_sound_on);
    }

    private void refreshBlueToothState(boolean state) {
        if (mBlueToothState == null) {
            return;
        }
        if (state)
            mBlueToothState.setBackgroundResource(R.drawable.icon_bluetooth_on);
        else
            mBlueToothState.setBackgroundResource(R.drawable.icon_bluetooth_off);
    }

    private void refreshGpsState(int state) {
        if (mGpsState == null) {
            return;
        }
        switch (state) {
            case XxGpsManager.STATE_AVAILABLE: {
                mGpsState.setVisibility(View.VISIBLE);
                mGpsState.setBackgroundResource(R.drawable.icon_gps_valid);
                break;
            }
            default: {
                mGpsState.setVisibility(View.GONE);
                break;
            }
        }
    }

    private void refreshWifiSignal(int signal) {
        if (mWifiSignal == null) {
            return;
        }
        NetworkInfo netWorkInfo = XxNetManager.getInstance().getConnectivityManager().getNetWorkInfo(ConnectivityManager.TYPE_WIFI);
        if (netWorkInfo != null) {
            NetworkInfo.State state = netWorkInfo.getState();
            if (state == NetworkInfo.State.CONNECTED) {
                mWifiSignal.setVisibility(View.VISIBLE);
            } else {
                mWifiSignal.setVisibility(View.GONE);
            }
        }
        if (Math.abs(signal) > 100) {
            mWifiSignal.setImageResource(R.drawable.icon_wifi_signal_0);
        } else if (Math.abs(signal) > 80) {
            mWifiSignal.setImageResource(R.drawable.icon_wifi_signal_1);
        } else if (Math.abs(signal) > 70) {
            mWifiSignal.setImageResource(R.drawable.icon_wifi_signal_1);
        } else if (Math.abs(signal) > 60) {
            mWifiSignal.setImageResource(R.drawable.icon_wifi_signal_2);
        } else if (Math.abs(signal) > 50) {
            mWifiSignal.setImageResource(R.drawable.icon_wifi_signal_2);
        } else {
            mWifiSignal.setImageResource(R.drawable.icon_wifi_signal_3);
        }
    }

    private void refreshMobileData(int serviceState) {
        if (mMobileType == null) {
            return;
        }
        if (serviceState == ServiceState.STATE_OUT_OF_SERVICE) {
            mMobileType.setVisibility(View.GONE);
            return;
        } else {
            mMobileType.setVisibility(View.VISIBLE);
        }
        XxTelephoneManager manager = XxNetManager.getInstance().getTelephoneManager();
        int status = manager.getTelephoneState();
        if (status == TelephonyManager.SIM_STATE_READY) {
            boolean mobileDataStatus = manager.getMobileDataState(null);
            int type = manager.getMobileType();
            if (mobileDataStatus && type != TelephonyManager.NETWORK_CLASS_UNKNOWN) {

                switch (type) {
                    case TelephonyManager.NETWORK_CLASS_2_G:
                        mMobileType.setBackgroundResource(R.drawable.icon_mobile_type_2g);
                        break;
                    case TelephonyManager.NETWORK_CLASS_3_G:
                        mMobileType.setBackgroundResource(R.drawable.icon_mobile_type_3g);
                        break;
                    case TelephonyManager.NETWORK_CLASS_4_G:
                        mMobileType.setBackgroundResource(R.drawable.icon_mobile_type_4g);
                        break;
                    default:
                        break;
                }

                mMobileType.setVisibility(View.VISIBLE);
            } else {
                mMobileType.setVisibility(View.GONE);
            }
        } else {
            mMobileType.setVisibility(View.GONE);
        }
    }

    private int mLastWifiStatus = WifiManager.WIFI_STATE_UNKNOWN;

    public void refreshWifi(int wifiStatus) {
        if (mWifiSignal == null) {
            return;
        }
        switch (wifiStatus) {
            case WifiManager.WIFI_STATE_ENABLED:
                if (mLastWifiStatus == WifiManager.WIFI_STATE_UNKNOWN || mLastWifiStatus == WifiManager.WIFI_STATE_ENABLING) {
                    NetworkInfo netWorkInfo = XxNetManager.getInstance().getConnectivityManager().getNetWorkInfo(ConnectivityManager.TYPE_WIFI);
                    if (netWorkInfo != null) {
                        NetworkInfo.State state = netWorkInfo.getState();
                        if (state == NetworkInfo.State.CONNECTED) {
                            mWifiSignal.setVisibility(View.VISIBLE);
                        } else {
                            mWifiSignal.setVisibility(View.GONE);
                        }
                    }
                }
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                if (mLastWifiStatus == WifiManager.WIFI_STATE_UNKNOWN || mLastWifiStatus == WifiManager.WIFI_STATE_DISABLING) {
                    mWifiSignal.setVisibility(View.GONE);
                }
                break;
            default:
                mWifiSignal.setVisibility(View.GONE);
                break;
        }
        mLastWifiStatus = wifiStatus;
    }

    private void updateMobileSignal(int serviceState, int level) {
        if (mMobileSignal == null) {
            return;
        }
        refreshMobileData(serviceState);
        if (serviceState == ServiceState.STATE_OUT_OF_SERVICE) {
            mMobileSignal.setVisibility(View.GONE);
            return;
        } else {
            mMobileSignal.setVisibility(View.VISIBLE);
        }

        switch (level) {
            case -1: {
                mMobileSignal.setBackgroundResource(R.drawable.icon_mobile_signal_0);
                break;
            }
            case 0: {
                mMobileSignal.setBackgroundResource(R.drawable.icon_mobile_signal_0);
                break;
            }
            case 1: {
                mMobileSignal.setBackgroundResource(R.drawable.icon_mobile_signal_1);
                break;
            }
            case 2: {
                mMobileSignal.setBackgroundResource(R.drawable.icon_mobile_signal_2);
                break;
            }
            case 3: {
                mMobileSignal.setBackgroundResource(R.drawable.icon_mobile_signal_3);
                break;
            }
            case 4: {
                mMobileSignal.setBackgroundResource(R.drawable.icon_mobile_signal_4);
                break;
            }
            default:
                break;
        }
    }

}
