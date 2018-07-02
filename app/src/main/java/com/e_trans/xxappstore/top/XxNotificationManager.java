package com.e_trans.xxappstore.top;

import android.content.Context;
import android.os.Message;

import com.e_trans.xxappstore.top.gps.XxGpsManager;


public class XxNotificationManager extends XxBaseModule
									implements XxGpsManager.OnXxGpsChangeListener,
												XxNetManager.IMobileState

{
	private String mCurrentTime;
	
	private Runnable mTimeRunnable;
	
	private static XxNotificationManager mNotificationManager = new XxNotificationManager();
	
	public static XxNotificationManager getInstance()
	{		
		return mNotificationManager;			
	}
	
	@Override
	public XxBaseModule init(Context context) {
		// TODO Auto-generated method stub
		super.init(context);

		XxNetManager.getInstance().addMobileStateListener(this);
		XxGpsManager.getInstance().addOnXxGpsChangeListener(this);
		
		mTimeRunnable = new Runnable() {	
			
			@Override
			public void run() {
				
				Message.obtain(mHandler, XxMessage.MSG_NOTIFICATION_TIME).sendToTarget();
				mHandler.postDelayed(mTimeRunnable, 5000);				
			}
		};
		
		mHandler.post(mTimeRunnable);
		
		return this;
	}

//	@Override
//	public void onGpsChange(int type, GpsInfo gpsInfo) {
//
//	}

	@Override
	public void onGpsChange(boolean isEncrypt, GpsInfo gpsInfo) {

	}

	@Override
	public void onGpsStateChanged(int state) {
		Message.obtain(mHandler, XxMessage.MSG_NOTIFICATION_GPS_STATE, state, 0).sendToTarget();

	}

	@Override
	public void onMobileStateChanged(boolean connected) {


	}

	@Override
	public void onMobileSignalChanged(MobileParams params) {
		Message.obtain(mHandler, XxMessage.MSG_NOTIFICATION_MOBILE, params).sendToTarget();
	}
}
