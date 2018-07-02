package com.e_trans.xxappstore.top;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

public class XxWifiUtils {
	private WifiManager mWifiManager;
	private Context mContext;
	private boolean bInit;
	
	public XxWifiUtils(){}
	
	public void init(Context context) {
		if(!bInit) {
			mContext = context;
			mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
			bInit = true;
		}
	}
	
	public boolean openWifi() {
		if(isWifiEnabled()) {
			return true;
		}else {
			return mWifiManager.setWifiEnabled(true);
		}
	}
	
	public boolean closeWifi() {
		if(!isWifiEnabled()) {
			return true;
		}else {
			return mWifiManager.setWifiEnabled(false);
		}
	}
	
	public boolean isWifiEnabled(){
		return mWifiManager.isWifiEnabled();
	}

	public boolean removeNetwork(int networdId) {
		boolean ret = false ;
		if(isWifiEnabled()) {
			ret = mWifiManager.removeNetwork(networdId);
			saveConfiguration();
 		}
		return ret ;
	}
	
	public boolean saveConfiguration() {
		if(isWifiEnabled()) {
			return mWifiManager.saveConfiguration();
		}
		return false;
	}
	
	public List<WifiConfiguration> getConfiguredNetworks() {
		if(isWifiEnabled()) {
			return mWifiManager.getConfiguredNetworks();
		}
		return null;
	}
	
	/**
	 * 或去当前可用的wifi列表
	 * @return  可用wifi列表集合
	 */
	public List<ScanResult> getScanResults() {
		if(isWifiEnabled()) {
			return mWifiManager.getScanResults();
		}
		return null;
	}
	
	/**
	 * 
	 * @param config
	 * @return  -1 失败
	 */
	public int addNetwork(WifiConfiguration config) {
		int flag = - 1;
		if(isWifiEnabled()) {
			flag =  mWifiManager.addNetwork(config);
		}
		return flag ;
	}
	
	/**
	 * WIFI_STATE_DISABLED,  wifi关闭
     * WIFI_STATE_DISABLING, wifi关闭中
     * WIFI_STATE_ENABLED,   wifi打开
     * WIFI_STATE_ENABLING,  wifi打开中 
     * WIFI_STATE_UNKNOWN    未知状态
	 * @return
	 */
	public int getWifiState() {
		return mWifiManager.getWifiState();
	}
	
	public WifiInfo getConnectionInfo() {
		if(isWifiEnabled()) {
			return mWifiManager.getConnectionInfo();
		}
		return null;
	}
	
	public boolean disableNetwork(int netId) {
		if(isWifiEnabled()) {
			return mWifiManager.disableNetwork(netId);
		}
		return false ;
	}
	
	public boolean disconnect() {
		if(isWifiEnabled()) {
			return mWifiManager.disconnect();
		}
		return false ;
	}
	
	public boolean startScan() {
		if(isWifiEnabled()) {
			return mWifiManager.startScan();
		}
		return false;
	}
	
	public boolean enableNetwork(int netId , boolean disableOthers) {
		if(isWifiEnabled()) {
			boolean flag = mWifiManager.enableNetwork(netId, disableOthers);
			saveConfiguration();
			return flag;
		}
		return false ;
	}
}
