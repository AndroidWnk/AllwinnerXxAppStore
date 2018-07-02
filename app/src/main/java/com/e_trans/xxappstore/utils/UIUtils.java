package com.e_trans.xxappstore.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class UIUtils {
	
	private static Toast mToast = null;
	
	public static void showToast(final Context context, final String str, final int duration) {
		if (null != context) {
			if (null != mToast) {

			} else {
				mToast = Toast.makeText(context, str, duration);
			}
			mToast.setText(str);
			mToast.show();
		}
	}

	public static void sendTitleBroadCast(Context context, String title) {
		Intent intent1 = new Intent("com.etrans.vehicle.currentapp.fixed");
		context.sendBroadcast(intent1);
		intent1.putExtra("label", title);
		context.sendBroadcast(intent1);
	}
	
}
