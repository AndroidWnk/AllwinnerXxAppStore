package com.e_trans.xxappstore.entity;

import java.io.Serializable;

/**
 * 线程信息类
 * Created by wk on 2016/4/11 0011.
 */
public class ThreadDownloadInfo implements Serializable {
    private int threadId;// 开启的线程数
    private int startPos;// 该进程的起始位置
    private int endPos;// 该进程的终止位置
    private int completeSize;// 完成的进度
    private String urlString;// 当前任务的url
    private String fileId;//文件在服务器的ID
    private String iconId;//文件的icon在服务器的ID
    private String fileName;//文件的名字
    private int fileState;//文件状态
    private String packageName;//包名
    private String md5;//文件md5

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    @Override
    public String toString() {
        return "ThreadDownloadInfo [threadId=" + threadId + ", startPos="
                + startPos + ", endPos=" + endPos + ", completeSize="
                + completeSize + ", urlString=" + urlString + ",fileId=" + fileId + ",iconId=" +
                iconId + ",fileName=" + fileName + ",fileState=" + fileState + ",packageName=" + packageName + ",md5=" + md5 + "]";
    }

    public ThreadDownloadInfo(int threadId, int startPos, int endPos,
                              int completeSize, String urlString, String fileId, String iconId, String fileName, int fileState, String packageName, String md5) {
        this.threadId = threadId;
        this.startPos = startPos;
        this.endPos = endPos;
        this.completeSize = completeSize;
        this.urlString = urlString;
        this.fileId = fileId;
        this.iconId = iconId;
        this.fileName = fileName;
        this.fileState = fileState;
        this.packageName = packageName;
        this.md5 = md5;
    }

    public ThreadDownloadInfo() {
    }

    public int getCompleteSize() {
        return completeSize;
    }

    public void setCompleteSize(int completeSize) {
        this.completeSize = completeSize;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getStartPos() {
        return startPos;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public String getIconId() {
        return iconId;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileState() {
        return fileState;
    }

    public void setFileState(int fileState) {
        this.fileState = fileState;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
