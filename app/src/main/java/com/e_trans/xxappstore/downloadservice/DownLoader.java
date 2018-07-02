package com.e_trans.xxappstore.downloadservice;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.e_trans.xxappstore.XxCustomApplication;
import com.e_trans.xxappstore.constant.Constant;
import com.e_trans.xxappstore.db.DownloadDao;
import com.e_trans.xxappstore.entity.AppListEntity;
import com.e_trans.xxappstore.entity.ThreadDownloadInfo;
import com.e_trans.xxdownloadaidl.DownloadInfo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 下载工具类
 * Created by wk on 2016/4/11 0011.
 */
public class DownLoader {
    private String urlString;// 下载地址
    public String fileId;// 文件在服务器的ID
    public String localFile;// 保存的文件
    public int threadCount;// 开启的线程数
    private int fileSize;// 文件大小
    private Handler mHandler;// 同步进度条
    private DownloadDao dao;// 数据库操作类
    private List<ThreadDownloadInfo> infos;// 保存下载信息
    private XxCustomApplication application;
    private Context mContext;
    private Intent mIntent = null;

    // 标记下载状态
    public static final int INIT = 1; // 初始状态
    public static final int DOWNLOADING = 2;// 正在下载
    public static final int PAUSE = 3;// 暂停
    public static final int WAIT = 4;// 等待
    public int state = INIT;

    public DownLoader(String urlString, String fileId, String localFile, int threadCount,
                      Context context, Handler handler, XxCustomApplication application) {
        this.urlString = urlString;
        this.fileId = fileId;
        this.localFile = localFile;
        this.threadCount = threadCount;
        this.mContext = context;
        this.application = application;
        mHandler = handler;
        dao = DownloadDao.getInstance();
    }

    /**
     * 下载
     */
    public void downloadByGet(Map<String, String> params) {
        if (infos != null) {
            Log.v("TAG", "download()------->infos != null");
            if (state == DOWNLOADING || state == WAIT) {
                return;
            }
            state = DOWNLOADING;
            for (ThreadDownloadInfo info : infos) {
                new DownloadThread(info.getThreadId(), info.getStartPos(),
                        info.getEndPos(), info.getCompleteSize(), info.getUrlString(), "get", params).start();
            }
        }
    }

    /**
     * 下载
     */
    public void downloadByPost(Map<String, String> params) {
        if (infos != null) {
            Log.v("TAG", "download()------->infos != null");
            if (state == DOWNLOADING || state == WAIT) {
                return;
            }
            state = DOWNLOADING;
            for (ThreadDownloadInfo info : infos) {
                new DownloadThread(info.getThreadId(), info.getStartPos(),
                        info.getEndPos(), info.getCompleteSize(), info.getUrlString(), "post", params).start();
            }
        }
    }

    /**
     * 下载器是否正在下载 true： 正在下载
     */
    public boolean isDownloading() {
        return state == DOWNLOADING;
    }

    /**
     * 得到当前下载信息
     *
     * @return
     */
    public DownloadInfo getDownloadInfoByUrlString(String urlString) {
//        if (state == DOWNLOADING || state == WAIT) {
//            return null;
//        }
        if (isFirstByUrl(urlString)) {
//            init("get", null);
            String iconId = "";
            String fileName = "";
            int fileState = -1;
            String packageName = "";
            String md5 = "";
            for (AppListEntity.AppInfo appInfo : application.downloadList) {
                if (appInfo.fileId == Integer.parseInt(fileId)) {
                    iconId = appInfo.iconFileId + "";
                    fileName = appInfo.appName;
                    fileState = appInfo.appState;
                    packageName = appInfo.packName;
                    md5 = appInfo.md5;
                }
            }
            infos = new ArrayList<ThreadDownloadInfo>();
            int range = fileSize / threadCount;
            int remainder = fileSize % threadCount;
            for (int i = 0; i < threadCount - 1; i++) {
                ThreadDownloadInfo info = null;
                if (i == threadCount - 1) {
                    info = new ThreadDownloadInfo(i, i * range,
                            (i + 1) * range + remainder, 0, urlString, fileId, iconId, fileName, fileState, packageName,md5);
                } else {
                    info = new ThreadDownloadInfo(i, i * range,
                            (i + 1) * range - 1, 0, urlString, fileId, iconId, fileName, fileState, packageName,md5);
                }
                infos.add(info);
            }
            ThreadDownloadInfo info = new ThreadDownloadInfo(threadCount - 1,
                    (threadCount - 1) * range, fileSize - 1, 0, urlString, fileId, iconId, fileName, fileState, packageName,md5);
            infos.add(info);
            dao.saveInfos(infos);
            return new DownloadInfo(fileSize, 0, urlString, fileId);
        } else {
            infos = dao.getInfos(urlString);
            int size = 0;
            int completeSize = 0;
            for (ThreadDownloadInfo info : infos) {
                completeSize += info.getCompleteSize();
                size += info.getEndPos() - info.getStartPos() + 1;
            }
            return new DownloadInfo(size, completeSize, urlString, fileId);
        }
    }

    /**
     * 得到当前下载信息
     *
     * @return
     */
    public DownloadInfo getDownloadInfoByFileId(Map<String, String> params, int fileSize) {
//        if (state == DOWNLOADING || state == WAIT) {
//            return null;
//        }
        this.fileSize = fileSize;
        if (isFirstByFileId(fileId)) {
//            init("post", params);
            try {
                File file = new File(localFile);
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                RandomAccessFile rFile = new RandomAccessFile(localFile, "rwd");
//                rFile.setLength(fileSize);//注释掉这句代码是因为设置这个长度耗时较长，当文件过大时会卡住，先注释掉
                rFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String iconId = "";
            String fileName = "";
            int fileState = -1;
            String packageName = "";
            String md5 = "";
            for (AppListEntity.AppInfo appInfo : application.downloadList) {
                if (appInfo.fileId == Integer.parseInt(fileId)) {
                    iconId = appInfo.iconFileId + "";
                    fileName = appInfo.appName;
                    fileState = appInfo.appState;
                    packageName = appInfo.packName;
                    md5 = appInfo.md5;
                }
            }
            infos = new ArrayList<ThreadDownloadInfo>();
            int range = fileSize / threadCount;
            int remainder = fileSize % threadCount;
            for (int i = 0; i < threadCount - 1; i++) {
                ThreadDownloadInfo info = null;
                if (i == threadCount - 1) {
                    info = new ThreadDownloadInfo(i, i * range,
                            (i + 1) * range + remainder, 0, urlString, fileId, iconId, fileName, fileState, packageName,md5);
                } else {
                    info = new ThreadDownloadInfo(i, i * range,
                            (i + 1) * range - 1, 0, urlString, fileId, iconId, fileName, fileState, packageName,md5);
                }
                infos.add(info);
            }
            ThreadDownloadInfo info = new ThreadDownloadInfo(threadCount - 1,
                    (threadCount - 1) * range, fileSize - 1, 0, urlString, fileId, iconId, fileName, fileState, packageName,md5);
            infos.add(info);
            dao.saveInfos(infos);
            return new DownloadInfo(fileSize, 0, urlString, fileId);
        } else {
            infos = dao.getInfosByFileId(fileId);
            int size = 0;
            int completeSize = 0;
            for (ThreadDownloadInfo info : infos) {
                completeSize += info.getCompleteSize();
                size += info.getEndPos() - info.getStartPos() + 1;
            }
            return new DownloadInfo(size, completeSize, urlString, fileId);
        }
    }

    /**
     * 初始化 连接网络，准备文件的保存路径等
     *
     * @param requestType 请求方式
     * @param params      请求参数（一般post才用）
     */
    private void init(String requestType, Map<String, String> params) {
        try {
            HttpURLConnection conn = null;
            InputStream is = null;
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            if (requestType.equalsIgnoreCase("get")) {
                conn.setRequestMethod("GET");
            } else if (requestType.equalsIgnoreCase("post")) {
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                String content = "token=" + params.get("token")
                        + "&data=" + params.get("data"); //要上传的参数
//                String content = "token=" + URLEncoder.encode(params.get("token"), "UTF-8")
//                        + "&data=" + URLEncoder.encode(params.get("data"), "UTF-8");
                out.writeBytes(content); //将要上传的内容写入流中
                out.flush();
                out.close(); //刷新、关闭
            }
            if (conn.getResponseCode() != 200)
                return;
            fileSize = conn.getContentLength();
            File file = new File(localFile);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            RandomAccessFile rFile = new RandomAccessFile(localFile, "rwd");
//            rFile.setLength(fileSize);//注释掉这句代码是因为设置这个长度耗时较长，当文件过大时会卡住，先注释掉
            rFile.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是不是断点续传（即是不是第一次下载） true：第一次下载
     *
     * @param urlString
     * @return
     */
    private boolean isFirstByUrl(String urlString) {
        return dao.unhasInfo(urlString);
    }

    /**
     * 判断是不是断点续传（即是不是第一次下载） true：第一次下载
     *
     * @param fileId
     * @return
     */
    private boolean isFirstByFileId(String fileId) {
        return dao.unhasInfoByFileId(fileId);
    }

    /**
     * 暂停
     */
    public void pause() {
        state = PAUSE;
    }

    /**
     * 获取下载状态
     */
    public int getState() {
        return state;
    }

    /**
     * 下载的线程类
     *
     * @author song
     */
    private class DownloadThread extends Thread {
        private int threadId;
        private int startPos;
        private int endPos;
        private int completeSize;
        private String urlString;
        private String requestType;
        private Map<String, String> params;

        public DownloadThread(int threadId, int startPos, int endPos,
                              int completeSize, String urlString, String requestType, Map<String, String> params) {
            this.threadId = threadId;
            this.startPos = startPos;
            this.endPos = endPos;
            this.completeSize = completeSize;
            this.urlString = urlString;
            this.requestType = requestType;
            this.params = params;
        }

        @Override
        public void run() {
//            RandomAccessFile rFile = null;
//            InputStream is = null;
//            try {
//                HttpClient fileClient = new DefaultHttpClient();
//                fileClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 50000); // 请求超时
//                fileClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);// 读取超时
//                HttpResponse fileResponse = null;
//                Header header_size = new BasicHeader("Range", "bytes=" + (startPos + completeSize) + "-" + endPos); //设置下载的数据位置XX字节到XX字节
//                if (requestType.equalsIgnoreCase("get")) {
//                    HttpGet httpGet = new HttpGet(urlString);
//                    httpGet.addHeader(header_size);
//                    fileResponse = fileClient.execute(httpGet);
//                } else if (requestType.equalsIgnoreCase("post")) {
//                    HttpPost httpPost = new HttpPost(urlString);
//                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//                    if (params != null) {
//                        Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
//                        while (it.hasNext()) {
//                            Map.Entry<String, String> entry = it.next();
//                            nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
//                        }
//                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
//                        httpPost.addHeader(header_size);
//                        fileResponse = fileClient.execute(httpPost);
//                    }
//                }
//                rFile = new RandomAccessFile(localFile, "rwd");
//                rFile.seek(startPos + completeSize);
//                is = fileResponse.getEntity().getContent();
//                byte[] buffer = new byte[2048];
//                int len = -1;
//                while ((len = is.read(buffer)) != -1) {
//                    rFile.write(buffer, 0, len);
//                    completeSize += len;
//                    if (requestType.equalsIgnoreCase("get")) {
//                        dao.updateInfo(threadId, completeSize, urlString);
//                    } else if (requestType.equalsIgnoreCase("post")) {
//                        dao.updateInfoByFileId(threadId, completeSize, fileId);
//                    }
//                    Message msg = Message.obtain();
//                    msg.what = 1;
//                    msg.arg2 = len;
//                    if (requestType.equalsIgnoreCase("get")) {
//                        msg.obj = urlString;
//                    } else if (requestType.equalsIgnoreCase("post")) {
//                        msg.obj = fileId;
//                    }
//                    mHandler.sendMessage(msg);
//                    Log.v("TAG", "completeSize=" + completeSize);
//                    if (state == PAUSE) {
//                        return;
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    is.close();
//                    rFile.close();
//                    dao.closeDB();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }


            HttpURLConnection conn = null;
            RandomAccessFile rFile = null;
            InputStream is = null;
            int len = -1;
            try {
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setUseCaches(false);
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("Range", "bytes="
                        + (startPos + completeSize) + "-" + endPos);
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                //要上传的参数
//                String content = "token=" + params.get("token")
//                        + "&data=" + params.get("data");
                String content = "token=" + URLEncoder.encode(params.get("token"), "UTF-8")
                        + "&data=" + URLEncoder.encode(params.get("data"), "UTF-8");
                Log.d("aaa", "参数: " + content);
                //将要上传的内容写入流中
                out.writeBytes(content);
                out.flush();
                //刷新、关闭
                out.close();

                rFile = new RandomAccessFile(localFile, "rwd");
                rFile.seek(startPos + completeSize);
                is = conn.getInputStream();
                byte[] buffer = new byte[10240];
                while ((len = is.read(buffer)) != -1) {
                    rFile.write(buffer, 0, len);
                    completeSize += len;
                    if (requestType.equalsIgnoreCase("get")) {
                        dao.updateInfo(threadId, completeSize, urlString);
                    } else if (requestType.equalsIgnoreCase("post")) {
                        dao.updateInfoByFileId(threadId, completeSize, fileId);
                    }
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.arg2 = completeSize;
                    if (requestType.equalsIgnoreCase("get")) {
                        msg.obj = urlString;
                    } else if (requestType.equalsIgnoreCase("post")) {
                        msg.obj = fileId;
                    }
                    mHandler.sendMessage(msg);
                    Log.v("ttt", "threadID=" + getId() + "   completeSize=" + completeSize + "   mThreadId=" + threadId + "   fileId=" + fileId);
                    if (state == PAUSE) {
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("ttt", getId() + "Exception: " + e.toString()+ "   fileId=" + fileId);
                ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if(!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()){//断网就不要retry

                }else{
                    if ( e.toString().contains("SocketTimeoutException")||(e.toString().contains("SocketException") && e.toString().contains("ETIMEDOUT") && e.toString().contains("timed out")) || e.toString().contains("ConnectException")) {//网断了或者网络超时
                        Message msg = Message.obtain();
                        msg.what = 5;
                        msg.obj = fileId;
                        mHandler.sendMessage(msg);
                    }
                }
            } finally {
                Log.i("ttt", "finally: " + getId());
                try {
                    if(is != null){
                        is.close();
                    }
                    if(rFile != null){
                        rFile.close();
                    }
                    conn.disconnect();
//                    if (completeSize == 0) {//任务下载失败处理
//                        for (int i = 0; i < application.downloadList.size(); i++) {
//                            if (Integer.parseInt(fileId) == application.downloadList.
//                                    get(i).fileId) {
//                                dao.addHistoryInfo(application.downloadList.get(i));
//                                deleteInfoByFileId(fileId);//在数据库中把对应这个任务的下载信息删除
//                                DownLoader loader = application.downLoaders.get(fileId);//把这个任务的下载器移除
//                                loader.closeDB();
//                                File file = new File(Constant.appFilePath + application.downloadList.get(i).appName + ".apk");
//                                if (file.exists()) {
//                                    file.delete();
//                                }
//                                application.downLoaders.remove(fileId);
//                                application.downloadList.remove(i);
//                                //开启等待任务
//                                Message msg = Message.obtain();
//                                msg.what = 2;
//                                msg.arg1 = Integer.parseInt(fileId);
//                                mHandler.sendMessage(msg);
//                            }
//                        }
//                    }
                    dao.closeDB();
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.arg1 = fileSize;
                    msg.arg2 = completeSize;
                    if (requestType.equalsIgnoreCase("get")) {
                        msg.obj = urlString;
                    } else if (requestType.equalsIgnoreCase("post")) {
                        msg.obj = fileId;
                    }
                    mHandler.sendMessage(msg);
                    completeSize = 0;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void deleteInfo(String urlString) {
        dao.deleteInfos(urlString);
    }

    public void deleteInfoByFileId(String fileId) {
        pause();//跳出下载
        dao.deleteInfosByFileId(fileId);//删除数据库里面的信息
    }

    public void closeDB() {
        dao.closeDB();
    }

    public void saveDownloadHistory(AppListEntity.AppInfo info) {
        dao.addHistoryInfo(info);
    }

}
