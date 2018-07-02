package com.e_trans.xxappstore.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wk on 2016/4/5 0005.
 */
public class AppDetailsEntity implements Serializable {
    public int state;//服务器返回结果状态码
    public AppDetailsResultInfo data;//成功信息
    public AppListErrorInfo err;//失败信息

    public class AppDetailsResultInfo implements Serializable {
        public String description;//应用描述
        public List<ImageInfo> imgList = new ArrayList<ImageInfo>();
    }

    public class ImageInfo implements Serializable {
        public int imgFileId;//图片ID
        public int orderNo;//排序
    }

    public class AppListErrorInfo implements Serializable {
        public String errCode;//错误码
        public String errMsg;//错误信息
    }
}
