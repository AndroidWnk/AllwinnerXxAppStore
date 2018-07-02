package com.e_trans.xxappstore.downloadservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.e_trans.xxappstore.request.UrlManager;

/**
 * 接收开机广播，开启下载服务
 * Created by wk on 2016/4/11 0011.
 */
public class BootBroadcastReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isCheckUpdate", true);

        Intent service = new Intent("com.e_trans.xxappstore.service.DOWNLOADSERVICE");
        service.putExtras(bundle);
        context.startService(service);
    }
}
