package com.e_trans.xxappstore.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;

public class FileCache {

    private File cacheDir;

    public FileCache(Context context) {
            // 如果有SD卡则在SD卡中建一个LazyList的目录存放缓存的图片
            // 没有SD卡就放在系统的缓存目录中
            if (Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED))
                    cacheDir = new File(
                                    Environment.getExternalStorageDirectory(),
                                    "/ZhongTai/Cache");
            else
                    cacheDir = context.getCacheDir();
            if (!cacheDir.exists())
                    cacheDir.mkdirs();
    }

    public File getFile(String url) {
            // 将url的hashCode作为缓存的文件名
            String filename = String.valueOf(url.hashCode());
            // Another possible solution
            // String filename = URLEncoder.encode(url);
            File f = new File(cacheDir, filename);
            return f;

    }

    public void clear() {
            File[] files = cacheDir.listFiles();
            if (files == null)
                    return;
            for (File f : files)
                    f.delete();
    }

}