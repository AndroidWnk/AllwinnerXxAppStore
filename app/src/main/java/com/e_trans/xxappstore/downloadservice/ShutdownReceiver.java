package com.e_trans.xxappstore.downloadservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.e_trans.xxappstore.XxCustomApplication;
import com.e_trans.xxappstore.db.DBHelper;
import com.e_trans.xxappstore.entity.AppListEntity;

/**
 * Created by wk on 2016/4/28.
 */
public class ShutdownReceiver extends BroadcastReceiver {
    private XxCustomApplication application;
    @Override
    public void onReceive(Context context, Intent intent) {
        application = (XxCustomApplication) context.getApplicationContext();
        application.isDoRecoverNet = false;
        SharedPreferences sharedPre = context.getSharedPreferences("config",
                context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPre.edit();
        editor.putBoolean("isNormal", true);
        editor.commit();

        XxCustomApplication application = (XxCustomApplication) context.getApplicationContext();
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String tableExistsSql = "select count(*) as c from sqlite_master where type ='table' and name ='downloader_info'";
        Cursor cursor = db.rawQuery(tableExistsSql, null);
        if (cursor.moveToNext()) {
            int count = cursor.getInt(0);
            if (count > 0) {//表存在
                cursor.close();
                for (AppListEntity.AppInfo appInfo : application.downloadList) {
                    if(appInfo != null){
                        DownLoader loader = application.downLoaders.get(appInfo.fileId+"");
                        if(loader != null){
                            String saveSql = "insert into downloader_info(file_id,localUrl,thread_count,downloader_state) values (?,?,?,?)";
                            Object[] bindArgs = {loader.fileId, loader.localFile, loader.threadCount, loader.getState()};
                            db.execSQL(saveSql, bindArgs);
                            if (loader.isDownloading()) {
                                loader.pause();
                            }
                        }
                    }
                }
//                for (DownLoader loader : application.downLoaders.values()) {
//                    String saveSql = "insert into downloader_info(file_id,localUrl,thread_count,downloader_state) values (?,?,?,?)";
//                    Object[] bindArgs = {loader.fileId, loader.localFile, loader.threadCount, loader.getState()};
//                    db.execSQL(saveSql, bindArgs);
//                }
            }
        }
        db.close();
    }
}
