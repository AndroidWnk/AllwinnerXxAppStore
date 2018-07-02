package com.e_trans.xxappstore.top;




import android.content.Context;
import android.provider.Settings;

public class XxSetting
{
	private static XxSetting mSetting = new XxSetting();
	
	private Context mContext;	
	private XxWifiUtils mWifiUtils;
	private boolean bInit;
	
	
	public boolean isInit() {
		return bInit;
	}

	public static XxSetting getInstance()
	{		
		return mSetting;
	}
	
	public XxSetting init(Context context)
	{
		if(!bInit) {
			mContext = context;
			mWifiUtils = new XxWifiUtils();
			mWifiUtils.init(mContext);
			bInit = true;
		}
		
		return mSetting;
	}
	
	public void setScreenOffTimeout(int ms)
	{
		Settings.System.putInt(mContext.getContentResolver(),
				Settings.System.SCREEN_OFF_TIMEOUT, ms);
	}
	
	public XxWifiUtils getWifiUtils() {
		return mWifiUtils;
	}
}
