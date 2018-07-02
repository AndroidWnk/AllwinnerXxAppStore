package com.e_trans.xxappstore.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库助手类
 * Created by wk on 2016/4/11 0011.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AppDownload.db";
    private static final int VERSION = 3;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    //    任务的ID：_id
    //    线程ID：thread_id
    //    线程下载的起始位置：start_pos
    //    这个线程下载的结束位置：end_pos
    //    这个任务已经下载的大小：compelete_size
    //    这个任务的服务器ID(post下载可能会用到)：file_id
    //    这个任务的下载地址(get下载的地址)：urlString
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table download_info(_id integer PRIMARY KEY AUTOINCREMENT, " +
                "thread_id integer,start_pos integer, end_pos integer, compelete_size integer," +
                "urlString char,file_id char,icon_id char,file_name char,file_state integer,package_name char,app_md5 char)";//用于断点续传
        String sqlForHistroy = "create table download_histroy_info(_id integer PRIMARY KEY AUTOINCREMENT, " +
                "file_id char,file_name char,localUrl char,file_size integer,icon_id integer,package_name char,app_md5 char)";//用于下载历史
        String sqlForDownloader = "create table downloader_info(_id integer PRIMARY KEY AUTOINCREMENT, " +
                "file_id char,localUrl char,thread_count integer,downloader_state integer)";//用于推出app或者关机再次打开之后恢复状态
        String sqlForNetCut = "create table netcut_info(_id integer PRIMARY KEY AUTOINCREMENT, " +
                "file_id char,localUrl char,thread_count integer,downloader_state integer)";//用于断网之后恢复网络恢复状态
        db.execSQL(sql);
        db.execSQL(sqlForHistroy);
        db.execSQL(sqlForDownloader);
        db.execSQL(sqlForNetCut);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            updateDB2To3(db);
        }
    }

    /**
     * 数据库从1升到2
     * 在download_info表里新增md5字段
     */
    private void updateDB1To2(SQLiteDatabase db) {
        String updateSQL = " ALTER TABLE download_info  ADD app_md5 char";
        db.execSQL(updateSQL);
    }
    /**
     * 数据库从2升到3
     * 在download_histroy_info表里新增md5字段
     */
    private void updateDB2To3(SQLiteDatabase db) {
        String updateSQL = " ALTER TABLE download_histroy_info  ADD app_md5 char";
        db.execSQL(updateSQL);
    }
}
