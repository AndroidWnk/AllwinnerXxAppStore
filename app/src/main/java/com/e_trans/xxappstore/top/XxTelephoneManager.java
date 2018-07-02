package com.e_trans.xxappstore.top;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class XxTelephoneManager {

	private TelephonyManager mTManager;
	private Context mContext;
	private List<PhoneStateObs> phoneStateObses;
	private boolean bCallOut =false;

	private String STRNetworkOperator[] = {"46000","46001", "46002" , "46003"};
	private int mark;
	private int type;

	private static final int CMCC = 0 ;  //移动
	private static final int CUCC = 1 ;  //联通
	private static final int CTCC = 2 ;  //电信

	private boolean bInitMarkType = false ;

	public int getServiceState() {
		return serviceState;
	}

	private boolean mbConnected;
	private int mLevel;

	public XxTelephoneManager() {
		phoneStateObses = new ArrayList<PhoneStateObs>();
	}

	public void init(Context context) {
		mTManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		mContext = context;
		mTManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		getmark();
	}

	public int getTelephoneState() {
		return mTManager.getSimState();
	}

	public int getMobileType()
	{
		type = getNetworkClass();
		return type;
	}

	public void onMobileStateChanged(boolean connected) {
		mbConnected = connected;

		for (PhoneStateObs obs : phoneStateObses) {
			obs.onMobileStateChanged(connected);
		}
	}

	public boolean getMobileConnected() {
		return mbConnected;
	}

	private void getmark()//得到当前电话卡的归属运营商
	{

		String IMSI = null;
		IMSI = mTManager.getSubscriberId();
		if(IMSI != null) {
			if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
				mark = CMCC ;
			} else if (IMSI.startsWith("46001")) {
				mark = CUCC;
			} else if (IMSI.startsWith("46003")) {
				mark = CTCC;
			} else {
				mark = CMCC ;
			}
			bInitMarkType = true;
		}
	}

	public int getMobileState() {
		return mTManager.getDataState();
	}

	/**
	 * 设置手机的移动数据
	 */
	public void setMobileData(boolean pBoolean) {
		try {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			Class ownerClass = mConnectivityManager.getClass();
			Class[] argsClass = new Class[1];
			argsClass[0] = boolean.class;
			Method method = ownerClass.getMethod("setMobileDataEnabled", argsClass);
			method.invoke(mConnectivityManager, pBoolean);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("移动数据设置错误: " + e.toString());
		}
	}

	public int getNetworkClass() {
		type = getNetworkClass(mTManager.getDataNetworkType());
		return type;
	}


	private int getNetworkClass(int networkType) {

		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if(serviceState != ServiceState.STATE_IN_SERVICE || info == null || info.getType() == 1) {
			return TelephonyManager.NETWORK_CLASS_UNKNOWN;
		}

		Log.i("getNetworkClass" , "12345677   networkType =  " + networkType);
		switch (networkType) {
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_EDGE:
			case TelephonyManager.NETWORK_TYPE_CDMA:
			case TelephonyManager.NETWORK_TYPE_1xRTT:
			case TelephonyManager.NETWORK_TYPE_IDEN:
				return TelephonyManager.NETWORK_CLASS_2_G;
			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
			case TelephonyManager.NETWORK_TYPE_EHRPD:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
				return TelephonyManager.NETWORK_CLASS_3_G;
			case TelephonyManager.NETWORK_TYPE_LTE:
				return TelephonyManager.NETWORK_CLASS_4_G;
			default:
				return TelephonyManager.NETWORK_CLASS_UNKNOWN;
		}
	}


	/**
	 * 返回手机移动数据的状态
	 *
	 * @param arg 默认填null
	 * @return true 连接 false 未连接
	 */
	public boolean getMobileDataState(Object[] arg) {

		try {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			Class ownerClass = mConnectivityManager.getClass();
			Class[] argsClass = null;
			if (arg != null) {
				argsClass = new Class[1];
				argsClass[0] = arg.getClass();
			}
			Method method = ownerClass.getMethod("getMobileDataEnabled", argsClass);
			Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);
			return isOpen;
		} catch (Exception e) {
			System.out.println("得到移动数据状态出错");
			return false;
		}

	}

	/**
	 * @param
	 * @return
	 * @author liyz
	 * @desc 更新热点名字
	 */
	public void updateWifiApName() {
		String DEVICE_ID = mTManager.getDeviceId();

		if (DEVICE_ID == null)
			return;

		if(DEVICE_ID.length() == 8) {
			bCallOut = false;
		} else {
			bCallOut = true;
		}

		if (XxConfig.getInstance().getWifiAPName().equals("mycar_wifiap")) {
			if (DEVICE_ID != null && !DEVICE_ID.equals("")) {
				if (DEVICE_ID.length() < 4) {
					return;
				}

				DEVICE_ID = DEVICE_ID.substring(DEVICE_ID.length() - 4, DEVICE_ID.length());
				String hotName = "BB_" + DEVICE_ID;
				XxConfig.getInstance().setWifiAPName(hotName);
			}
		}
	}

	public void onSimReady()
	{
		Log.i("signal", "ready");
		getmark();
	}

	public void checkDeviceId() {
		String DEVICE_ID = mTManager.getDeviceId();
		if (DEVICE_ID == null) {
			Toast.makeText(mContext, "3G模块未识别", Toast.LENGTH_SHORT).show();
			return;
		}
		if(DEVICE_ID.length() == 8) {
			bCallOut = false;
		}else {
			bCallOut = true;
		}
	}

	private final boolean call =true;

	public boolean isCanCallOut() {
		return call;
	}

	public int getLevel()
	{
		return  mLevel;
	}

	public void addlistener(PhoneStateObs listener) {
		phoneStateObses.add(listener);
	}

	private int serviceState = ServiceState.STATE_OUT_OF_SERVICE;

	public void onServiceStateChanged(ServiceState serviceState) {
		this.serviceState = serviceState.getState();

		MobileParams params = new MobileParams();
		params.level = mLevel;
		params.type = getNetworkClass();
		params.serviceState = this.serviceState;
		for (PhoneStateObs obs : phoneStateObses) {
			obs.onSignalStrengthsChanged(params);
		}

	}


	private class MyPhoneStateListener extends PhoneStateListener {
		/* Get the Signal strength from the provider, each tiome there is an update  从得到的信号强度,每个tiome供应商有更新*/

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			int signal = 0;
			int snr = 0;
			int s2,s3,s4;
			signal = signalStrength.getDbm();
			mLevel = getLevel(signal, snr);
			Log.i("updateMobileSignal" , "onSignalStrengthsChanged  level = " + mLevel + "  signal = " + signal);
			type = getNetworkClass();
			MobileParams params = new MobileParams();
			params.level = mLevel;
			params.type = type ;
			params.serviceState = serviceState;
			for (PhoneStateObs obs : phoneStateObses) {
				obs.onSignalStrengthsChanged(params);
			}

			int state = mTManager.getSimState();
			if(TelephonyManager.SIM_STATE_READY == state && mTManager.getDeviceId() != null && mTManager.getDeviceId().length() != 8) {
				bCallOut = true;
			}


		}

		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			// TODO Auto-generated method stub
			super.onServiceStateChanged(serviceState);
		}


		private int getLevel(int signal, int snr) {

			if(serviceState != ServiceState.STATE_IN_SERVICE) {
				return 0;
			}

			// TODO Auto-generated method stub
			if (mark == 2) {//电信3g信号强度的分类，可以按照ui自行划分等级
				if (signal >= -65)
					return 4;
				else if (signal >= -75)
					return 3;
				else if (signal >= -95)
					return 2;
				else if (signal >= -105)
					return 1;
				else if (signal == -120)
					return  -1;
				else
					return 0;
			}
			if (mark == 1) {//联通3g信号划分
				if (signal >= -75)
					return 4;
				else if (signal >= -80)
					return 3;
				else if (signal >= -85)
					return 2;
				else if (signal >= -95)
					return 2;
				else if (signal >= -100)
					return 1;
				else
					return 0;
			}
			if (mark == 0) {//移动信号的划分，这个不是很确定是2g还是3g
				if (signal <= 2 || signal == 99)
					return 4;
				else if (signal >= 12)
					return 3;
				else if (signal >= 10)
					return 2;
				else if (signal >= 8)
					return 2;
				else if (signal >= 5)
					return 1;
				else
					return 0;
			}

			return  -1;
		}
	}

	public interface PhoneStateObs {
		void onMobileStateChanged(boolean connected);
		void onSignalStrengthsChanged(MobileParams params);
	}
}
