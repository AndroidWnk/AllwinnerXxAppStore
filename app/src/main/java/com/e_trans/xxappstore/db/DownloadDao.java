package com.e_trans.xxappstore.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.e_trans.xxappstore.XxCustomApplication;
import com.e_trans.xxappstore.constant.Constant;
import com.e_trans.xxappstore.entity.AppListEntity;
import com.e_trans.xxappstore.entity.ThreadDownloadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作数据库
 * Created by wk on 2016/4/11 0011.
 */
public class DownloadDao {

    private DBHelper dbHelper;
    private Context mContext;
    private static DownloadDao mDownloadDao = new DownloadDao();

    public static DownloadDao getInstance() {
        return mDownloadDao;
    }

    private DownloadDao() {

    }

    public void init(Context context){
        dbHelper = new DBHelper(context);
        this.mContext = context;
    }

    /**
     * 判断数据库中是不是有对应这个urlString的信息
     *
     * @return
     */
    public boolean unhasInfo(String urlString) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "select count(*) from download_info where urlString=?";
        Cursor cursor = db.rawQuery(sql, new String[]{urlString});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count == 0;
    }

    /**
     * 判断数据库中是不是有对应这个fileId的信息
     *
     * @return
     */
    public boolean unhasInfoByFileId(String fileId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "select count(*) from download_info where file_id=?";
        Cursor cursor = db.rawQuery(sql, new String[]{fileId});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count == 0;
    }

    /**
     * 把线程信息保存在数据库里面
     *
     * @param infos
     */
    public void saveInfos(List<ThreadDownloadInfo> infos) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (ThreadDownloadInfo info : infos) {
            String sql = "insert into download_info(thread_id,start_pos, end_pos,compelete_size,urlString,file_id,icon_id,file_name,file_state,package_name,app_md5) values (?,?,?,?,?,?,?,?,?,?,?)";
            Object[] bindArgs =
                    {info.getThreadId(), info.getStartPos(), info.getEndPos(),
                            info.getCompleteSize(), info.getUrlString(), info.getFileId(), info.getIconId(), info.getFileName(), info.getFileState(), info.getPackageName(),info.getMd5()};
            db.execSQL(sql, bindArgs);
        }

    }

    /**
     * 暂停之后，把当前数据保存在数据库中，该方法是从数据库中查询数据
     *
     * @return
     */
    public List<ThreadDownloadInfo> getInfos(String urlString) {
        List<ThreadDownloadInfo> list = new ArrayList<ThreadDownloadInfo>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "select thread_id, start_pos, end_pos,compelete_size, urlString,file_id,icon_id,file_name,file_state,package_name,app_md5 from download_info where urlString=?";
        Cursor cursor = db.rawQuery(sql, new String[]{urlString});
        while (cursor.moveToNext()) {
            ThreadDownloadInfo info = new ThreadDownloadInfo(cursor.getInt(0),
                    cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getInt(8), cursor.getString(9), cursor.getString(10));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    /**
     * 暂停之后，把当前数据保存在数据库中，该方法是从数据库中查询数据
     *
     * @return
     */
    public List<ThreadDownloadInfo> getInfosByFileId(String fileId) {
        List<ThreadDownloadInfo> list = new ArrayList<ThreadDownloadInfo>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "select thread_id, start_pos, end_pos,compelete_size, urlString,file_id,icon_id,file_name,file_state,package_name,app_md5 from download_info where file_id=?";
        Cursor cursor = db.rawQuery(sql, new String[]{fileId});
        while (cursor.moveToNext()) {
            ThreadDownloadInfo info = new ThreadDownloadInfo(cursor.getInt(0),
                    cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getInt(8), cursor.getString(9), cursor.getString(10));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    /**
     * 把当前的数据信息 存进数据库中
     *
     * @param threadId
     * @param completeSize
     */
    public void updateInfo(int threadId, int completeSize, String urlString) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "update download_info set compelete_size=? where thread_id=? and urlString=?";
        Object[] bindArgs =
                {completeSize, threadId, urlString};
        db.execSQL(sql, bindArgs);
    }

    /**
     * 把当前的数据信息 存进数据库中
     *
     * @param threadId
     * @param completeSize
     */
    public synchronized void updateInfoByFileId(int threadId, int completeSize, String fileId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "update download_info set compelete_size=? where thread_id=? and file_id=?";
        Object[] bindArgs =
                {completeSize, threadId, fileId};
        db.execSQL(sql, bindArgs);
    }

    /**
     * 关闭数据库
     */
    public void closeDB() {
//        dbHelper.close();
    }

    /**
     * 下载完成之后，从数据库里面把这个任务的信息删除
     * 不同的任务对应不同的urlString
     *
     * @param urlString
     */
    public synchronized void deleteInfos(String urlString) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("download_info", "urlString=?", new String[]{urlString});
    }

    /**
     * 下载完成之后，从数据库里面把这个任务的信息删除
     * 不同的任务对应不同的fileId
     *
     * @param fileId
     */
    public synchronized void deleteInfosByFileId(String fileId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("download_info", "file_id=?", new String[]{fileId});
        db.delete("downloader_info", "file_id=?", new String[]{fileId});
    }

    /**
     * 保存下载历史
     *
     * @param info
     */
    public void addHistoryInfo(AppListEntity.AppInfo info) {
        XxCustomApplication application = (XxCustomApplication) mContext.getApplicationContext();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String searchSql = "select count(*) from download_histroy_info where file_id=?";
        Cursor cursor = db.rawQuery(searchSql, new String[]{info.fileId + ""});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        String sql = "";
        if (count == 0) {
            sql = "insert into download_histroy_info(file_name,localUrl,file_size,icon_id,package_name,file_id,app_md5) values (?,?,?,?,?,?,?)";
            Object[] bindArgs = {info.appName, Constant.appFilePath + info.appName + ".apk", info.fileSize, info.iconFileId, info.packName, info.fileId,info.md5};
            db.execSQL(sql, bindArgs);
        } else {
//            sql = "update download_histroy_info set file_name=? and localUrl =? and file_size =? and icon_id =? and package_name =? where file_id=?";
        }
    }
}
