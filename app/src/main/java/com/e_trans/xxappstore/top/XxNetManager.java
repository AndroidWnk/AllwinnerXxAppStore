package com.e_trans.xxappstore.top;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * @fileName XxNetManager.java
 * @author liyz
 * @desc 网络管理类
 * @date 2015-1-30
 */
public class XxNetManager extends XxBaseModule implements XxTelephoneManager.PhoneStateObs{
	
	private XxWifiManager mWifiManager;
	private XxConnectivityManager mConnectivityManager;
	private XxTelephoneManager mTelephoneManager;
	private XxWifiApManager mWifiApManager;
	private Timer mMobileTimer;
	private boolean bWifiHadConnected = false;

	private List<IMobileState> mIMobileState;

	public static XxNetManager getInstance() {
		return XxNetManagerInstance.mNetManager;
	}

	@Override
	public void onMobileStateChanged(boolean connected) {

		for(IMobileState obs : mIMobileState) {
			obs.onMobileStateChanged(connected);
		}
	}

	@Override
	public void onSignalStrengthsChanged(MobileParams params) {
		for(IMobileState obs : mIMobileState) {
			obs.onMobileSignalChanged(params);
		}

	}

	public interface IMobileState
	{
		void onMobileStateChanged(boolean connected);
		void onMobileSignalChanged(MobileParams params);
	}

	private static class XxNetManagerInstance  {
		private static final XxNetManager mNetManager = new XxNetManager();
	}
	
	private XxNetManager() {
		mWifiManager = new XxWifiManager();
		mConnectivityManager = new XxConnectivityManager();
		mTelephoneManager = new XxTelephoneManager();
		mWifiApManager = new XxWifiApManager();

		mIMobileState = new ArrayList<IMobileState>();
	}

	public XxNetManager init(Context context) {
		super.init(context);
		mWifiManager.init(context , this);
		mConnectivityManager.init(context);
		mTelephoneManager.init(context);
		mWifiApManager.init(context);
		if(XxConfig.getInstance().isWifiApAutoOpen()) {
			String wifiApName = XxConfig.getInstance().getWifiAPName();
			String wifiApPass = XxConfig.getInstance().getWifiApPass();
			mWifiApManager.startWifiAp(wifiApName , wifiApPass);
		}
		mMobileTimer = new Timer();
		mMobileTimer.schedule(new StartMobileTimerTask(), 0 , 1000);
		
		mTelephoneManager.updateWifiApName();

		mTelephoneManager.addlistener(this);
		return this;
	}

	public void addMobileStateListener(IMobileState listener)
	{
		mIMobileState.add(listener);
	}

	public int getMobileLevel()
	{
		return mTelephoneManager.getLevel();
	}

	public int getMobileType()
	{
		return mTelephoneManager.getMobileType();
	}

	public int getWifiSignal()
	{
		return mWifiManager.getConnectionWifiStrength();
	}

	public boolean isCanCallOut() {
		return mTelephoneManager.isCanCallOut();
	}

	public void checkDeviceId() {
		mTelephoneManager.checkDeviceId();
	}
	


	public XxTelephoneManager getTelephoneManager() {
		return mTelephoneManager;
	}

	public XxWifiManager getWifiManager() {
		return mWifiManager;
	}
	
	public XxConnectivityManager getConnectivityManager() {
		return mConnectivityManager;
	}
	
	
	
	public void notifyWifiConnected() {
		if(mConnectivityManager.getStateByConnType(ConnectivityManager.TYPE_WIFI) == State.CONNECTED) {
			boolean bConnected = mConnectivityManager.getConnectivityState();
			if(bConnected) {				
				if(!bWifiHadConnected) {
//					Toast.makeText(mContext, "WiFi已连接上", Toast.LENGTH_SHORT).show();
					bWifiHadConnected = true;
					Message msg = mHandler.obtainMessage(XxMessage.MSG_SETTING_WIFILIST_TOP);
					mHandler.sendMessage(msg);
					mWifiManager.startScan();
				}
			}
		}
	}

	public void setWifiHadConnected(boolean bWifiHadConnected) {
		this.bWifiHadConnected = bWifiHadConnected;
	}

	public void respondWifiState(int wifiState) {
		if(wifiState == WifiManager.WIFI_STATE_DISABLED) {
			mWifiManager.cancelRefresh();
		}else if(wifiState == WifiManager.WIFI_STATE_ENABLED) {
			mWifiManager.startRefresh();
		}
	}
	public XxWifiApManager getWifiApManager() {
		return mWifiApManager;
	}


	private boolean bOpenOtherWifi = false;
	private boolean bHaveLongOper = false ;
	public void changeWifiStatus() {
		if(mWifiApManager.isWifiApEnabled()) {
			mWifiApManager.closeWifiAp();
			bOpenOtherWifi = true;
			bHaveLongOper = true;
			return ;
		}
		if(mWifiManager.isWifiEnabled()) {
			mWifiManager.closeWifi();
			bHaveLongOper = false ;
		}else {
			XxConfig.getInstance().setWifiAutoOpen(false);
			mWifiManager.openWifi();
			bHaveLongOper = true;
		}
	}

	boolean bWifiAutoOpen = false ;
	public void changeWifiApStatus() {
		if(mWifiManager.isWifiEnabled()) {
			bWifiAutoOpen = true;
			mWifiManager.closeWifi();
			bOpenOtherWifi = true;
			bHaveLongOper = true;
			return ;
		}
		if(mWifiApManager.isWifiApEnabled()) {
			mWifiApManager.closeWifiAp();
			if(XxConfig.getInstance().isWifiAutoOpen()) {
				changeWifiStatus();
			}
			bHaveLongOper = false ;
		}else {
			XxConfig.getInstance().setWifiAutoOpen(bWifiAutoOpen);
			bWifiAutoOpen = false;
			String wifiApName = XxNetManager.getInstance().getWifiApName();
			String wifiApPass = XxConfig.getInstance().getWifiApPass();
			mWifiApManager.startWifiAp(wifiApName , wifiApPass);
			bHaveLongOper = true;
		}
	}
	
	public void refreshUI(int refreshType , int status , int lastStatus) {
		if(refreshType == XxMessage.MSG_SETTING_RE_WIFI || refreshType == XxMessage.MSG_SETTING_RE_AP){
			Message msg = mHandler.obtainMessage(XxMessage.MSG_SETTING_RE_WIFI_AND_AP);
			if(refreshType == XxMessage.MSG_SETTING_RE_WIFI) {
				msg.arg1 = status;
				msg.arg2 = mWifiApManager.getWifiApState();
				if(bOpenOtherWifi && status == WifiManager.WIFI_STATE_DISABLED) {
					changeWifiApStatus();
					bOpenOtherWifi = false ;
				}
			}else {
				msg.arg1 = mWifiManager.getWifiStatus();
				msg.arg2 = status;
				if(status == XxWifiConst.XxWifiApStatus.WIFI_AP_STATE_ENABLED) {
					XxConfig.getInstance().setWifiApAutoOpen(true);
				}else if(status == XxWifiConst.XxWifiApStatus.WIFI_AP_STATE_DISABLED) {
					XxConfig.getInstance().setWifiApAutoOpen(false);
					if(bOpenOtherWifi) {
						changeWifiStatus();
						bOpenOtherWifi = false; 
					}
				}
				
			}
			if(status == WifiManager.WIFI_STATE_ENABLED || status == XxWifiConst.XxWifiApStatus.WIFI_AP_STATE_ENABLED) {
				bHaveLongOper = false ;
				respondWifiState(status);
				mHandler.sendMessageDelayed(msg, 1000);
			} 
			else {
				mHandler.sendMessage(msg);
				if(status == WifiManager.WIFI_STATE_DISABLED) {
					List<XxWifiInfo> wifiInfos = new ArrayList<XxWifiInfo>();
					Message msgref = mHandler.obtainMessage(
							XxMessage.MSG_SETTING_RE_WIFILIST, wifiInfos);
					mHandler.sendMessageAtFrontOfQueue(msgref);
				}
			}
		}else {
			mHandler.sendMessage(mHandler.obtainMessage(refreshType));
		}
	}
	
	public boolean isHaveLongOper() {
		return bHaveLongOper;
	}

	public void changedMobileDataStatus() {
		if(mTelephoneManager.getTelephoneState() == TelephonyManager.SIM_STATE_READY) {
			if(mTelephoneManager.getMobileDataState(null)) {
				mTelephoneManager.setMobileData(false);
			}else {
				mTelephoneManager.setMobileData(true);
			}
		}
	}
	
	private class StartMobileTimerTask extends TimerTask {
		@Override
		public void run() {
			Message msg = mHandler.obtainMessage(XxMessage.MSG_SETTING_RE_MOBILE_DATA);
			msg.arg1 = mTelephoneManager.getServiceState();
			mHandler.sendMessage(msg);
		}
		
	}


	public String getWifiApName() {
		return mWifiApManager.getWifiApName();
	}

	public void setWifiApName(String wifiapName) {
		mWifiApManager.setWifiApName(wifiapName);
	}

	public void setWifiApPwd(String wifiapPass) {
		mWifiApManager.setWifiApPwd(wifiapPass);
	}

	public String getWifiApPwd() {
		return mWifiApManager.getWifiApPwd();
	}
}
