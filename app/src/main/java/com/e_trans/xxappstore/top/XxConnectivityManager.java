package com.e_trans.xxappstore.top;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

public class XxConnectivityManager {
	private ConnectivityManager mConnectMgr;
	
	public XxConnectivityManager init(Context context) {
		mConnectMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return this;
	}
	
	public boolean getConnectivityState() {
		boolean netSataus = false;
		if(mConnectMgr != null && mConnectMgr.getActiveNetworkInfo() != null ) {
			netSataus = mConnectMgr.getActiveNetworkInfo().isAvailable();// 网络状态
		}
		return netSataus;
	}
	
	public State getStateByConnType(int type) {
		return mConnectMgr.getNetworkInfo(type).getState(); 
	}
	
	public NetworkInfo getNetWorkInfo(int type) {
		return mConnectMgr.getNetworkInfo(type);
	}
}
