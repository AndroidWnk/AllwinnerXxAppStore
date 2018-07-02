package com.e_trans.xxappstore.top;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.ServiceState;
import android.util.Log;


public class XxHardwareReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
         if (action.equals("android.vehicle.amplifier.MUTE")) {
            boolean bMute = intent.getBooleanExtra("bMute", false);
            Log.i("SystemMute", "bMute = " + bMute);
            XxOtherSettingManager.getInstance().changeVoiceStatus(bMute);

        }else if (action.equals("android.intent.action.SERVICE_STATE")) {
             Log.i("updateMobileSignal", "      TelephonyIntents.ACTION_SERVICE_STATE_CHANGED");
             ServiceState ss = ServiceState.newFromBundle(intent.getExtras());
             XxNetManager.getInstance().getTelephoneManager().onServiceStateChanged(ss);
         }

    }
}
