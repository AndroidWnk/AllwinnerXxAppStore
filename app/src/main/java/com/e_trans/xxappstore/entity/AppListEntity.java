package com.e_trans.xxappstore.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wk on 2016/4/5 0005.
 */
public class AppListEntity implements Serializable {
    public int state;//服务器返回结果状态码
    public AppListResultInfo data;//成功信息
    public AppListErrorInfo err;//失败信息

    public class AppListResultInfo implements Serializable {
        public int totalCount;//总数
        public int totalPage;//总页数
        public int pageNo;//当前页
        public int pageSize;//每页数量
        public List<AppInfo> list = new ArrayList<AppInfo>();//应用集合
    }

    public class AppInfo implements Serializable {
        public int id;//应用ID
        public String appName;//应用名称
        public String packName;//包名
        public int typeId;//应用类型ID
        public String typeName;//应用类型名称
        public String version;//版本
        public int fileId;//APK文件ID
        public int fileSize;//文件大小
        public int iconFileId;//icon文件ID
        public int recommendLvl;//推荐指数
        public int isForced;//是否默认升级（1是，0否）
        public int downNum;//下载次数
        public String issueTime;//发布时间，格式yyyy-MM-dd hh:mm:ss
        public String md5;//文件md5
        public String md5s;//文件标准md5
        public int appState;//0:未安装;1:下载中;2:下载成功，安装中，下载失败置回0，提示下载失败;3：安装成功（即已安装），可卸载，安装失败置回0

        public int completeSize;//已下载的长度，用于更新进度条
    }

    public class AppListErrorInfo implements Serializable {
        public String errCode;//错误码
        public String errMsg;//错误信息
    }
}
