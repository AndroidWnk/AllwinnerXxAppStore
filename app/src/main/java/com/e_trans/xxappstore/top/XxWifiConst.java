package com.e_trans.xxappstore.top;

public class XxWifiConst {
	
	public enum XxWifiEncryDesc {
		WIFI_WPA , WIFI_WPA2 , WIFI_WPA_WPA2 , WIFI_WPS ,WIFI_WEP ,
		WIFI_WPA_WPS , WIFI_WPA2_WPS , WIFI_WEP_WPS , WIFI_WPA_WPA2_WPS ,
		WIFI_NONE , WIFI_NO_SCOPE , WIFI_SAVE_WPA , WIFI_SAVE_WPA2 ,
		WIFI_SAVE_WPA_WPA2 , WIFI_SAVE_WEP , WIFI_SAVE ,
		WIFI_VERIFYING , WIFI_VERIFY_FAILED , WIFI_GET_ADDRESS , WIFI_CONNECTED ,
	}
	public class XxWifiEncryConst {
		public static final int WPAPSK = 1;
		public static final int WPA2PSK = 2;
		public static final int WEP = 4;
		public static final int NONE = 0;
	}
	
	public class XxWifiCipherConst { 
		public static final int TKIP = 1 ;
		public static final int CCMP = 2 ;
		public static final int NONE = 0 ;
	}
	
	public class XxWifiAuthAlgorithm {
		public static final int OPEN = 1 ;
		public static final int SHARED = 2 ;
	}
	
	public class XxWifiApStatus {
		public static final int WIFI_AP_STATE_DISABLING = 10;  
		public static final int WIFI_AP_STATE_DISABLED = 11;  
		public static final int WIFI_AP_STATE_ENABLING = 12;  
		public static final int WIFI_AP_STATE_ENABLED = 13;  
		public static final int WIFI_AP_STATE_FAILED = 14;
	}
}
