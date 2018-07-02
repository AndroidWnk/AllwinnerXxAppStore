package com.e_trans.xxappstore.entity;

import java.io.Serializable;

/**
 * Created by wk on 2016/4/5 0005.
 */
public class RegisterResultEntity implements Serializable {
    public int state;//服务器返回结果状态码
    public ResultInfo data;//成功信息
    public ErrorInfo err;//失败信息

    public class ResultInfo implements Serializable {
        public String token;//令牌
    }

    public class ErrorInfo implements Serializable {
        public String errCode;//错误码
        public String errMsg;//错误信息
    }
}
