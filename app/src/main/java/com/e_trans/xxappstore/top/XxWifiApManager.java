package com.e_trans.xxappstore.top;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class XxWifiApManager {
	
	private WifiManager mWifiManager;


	public XxWifiApManager() {
		
	}
	
	public void init(Context context) {
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	public void closeWifiAp() {
		if (isWifiApEnabled()) {
			try {
				Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
				method.setAccessible(true);

				WifiConfiguration config = (WifiConfiguration) method.invoke(mWifiManager);

				Method method2 = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
				method2.invoke(mWifiManager, config, false);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public  boolean isWifiApEnabled() {
		try {
			Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
			method.setAccessible(true);
			return (Boolean) method.invoke(mWifiManager);

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	public void startWifiAp(String apName , String password) {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		} 
		Method method1 = null;
		try {
			method1 = mWifiManager.getClass().getMethod("setWifiApEnabled",
					WifiConfiguration.class, boolean.class);
			WifiConfiguration netConfig = new WifiConfiguration();
			netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);  
			netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);                        
			netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);                        
			netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);                   
			netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);  
			netConfig.status = WifiConfiguration.Status.ENABLED; 
			netConfig.preSharedKey =  password ;// WPA-PSK密码
			netConfig.status = WifiConfiguration.Status.ENABLED;
			netConfig.SSID = apName;
			method1.invoke(mWifiManager, netConfig, true);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	//获取WIFI热点的状态：
	public int getWifiApState() {  
	    try {  
	        Method method = mWifiManager.getClass().getMethod("getWifiApState");  
	        int i = (Integer) method.invoke(mWifiManager);  
	        return i;  
	    } catch (Exception e) {  
	        return XxWifiConst.XxWifiApStatus.WIFI_AP_STATE_FAILED;
	    }  
	}

	public String getWifiApName() {
		return XxConfig.getInstance().getWifiAPName();
	}

	public void setWifiApName(String wifiapName) {
		XxConfig.getInstance().setWifiAPName(wifiapName);
	}

	public void setWifiApPwd(String wifiapPass) {
		XxConfig.getInstance().setWifiAPPass(wifiapPass);
	}

	public String getWifiApPwd() {
		return XxConfig.getInstance().getWifiApPass();
	}
}

