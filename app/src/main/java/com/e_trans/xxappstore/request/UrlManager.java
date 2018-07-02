package com.e_trans.xxappstore.request;

import android.util.Log;

/**
 * 访问接口集合
 * Created by wk on 2016/4/1 0001.
 */
public class UrlManager {
    public static String HOST = "http://vis.evcar.com:5151/v1/";//生产环境
    public static String DOWNLOADHOST = "http://vis.evcar.com:5151/v1/";//生产环境
//    private static String HOST = "http://58.58.205.23:5152/api/v1/";//测试环境
//    private static String DOWNLOADHOST = "http://58.58.205.23:5152/file/v1/";//测试环境
    public static String TAG = "ZD_AppStore";

    /**
     * 获取注册URL
     *
     * @return
     */
    public static String getRegisterUrl() {

        String url = HOST + "register.json";

        return url;
    }

    /**
     * 获取App相关请求URL
     *
     * @return
     */
    public static String getAppUrl() {

        String url = HOST + "app.json";
        Log.e("HOST",url);
        return url;
    }
    /**
     * 获取下载URL
     *
     * @return
     */
    public static String getDownloadUrl() {

        String url = DOWNLOADHOST + "download.json";

        return url;
    }
}

