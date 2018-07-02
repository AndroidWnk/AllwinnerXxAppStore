package com.e_trans.xxdownloadaidl;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * 每一个下载文件的信息
 * Created by wk on 2016/4/11 0011.
 */
public class DownloadInfo implements Parcelable {
    private int fileSize;//文件大小
    private int completeSize;//已下载的大小
    private String urlString;//文件下载地址
    private String fileId;//文件在服务器的ID

    public DownloadInfo(int fildownloadinfodownloadinfoeSize, int completeSize, String urlString, String fileId) {
        super();
        this.fileSize = fileSize;
        this.completeSize = completeSize;
        this.urlString = urlString;
        this.fileId = fileId;
    }

    public DownloadInfo() {
        super();
    }

    protected DownloadInfo(Parcel in) {
        fileSize = in.readInt();
        completeSize = in.readInt();
        urlString = in.readString();
        fileId = in.readString();
    }

    public static final Creator<DownloadInfo> CREATOR = new Creator<DownloadInfo>() {
        @Override
        public DownloadInfo createFromParcel(Parcel in) {
            return new DownloadInfo(in);
        }

        @Override
        public DownloadInfo[] newArray(int size) {
            return new DownloadInfo[size];
        }
    };

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getCompleteSize() {
        return completeSize;
    }

    public void setCompleteSize(int completeSize) {
        this.completeSize = completeSize;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public String toString() {
        return "DownloadInfo [fileSize=" + fileSize + ", completeSize="
                + completeSize + ", urlString=" + urlString + ",fileId=" + fileId + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(fileSize);
        parcel.writeInt(completeSize);
        parcel.writeString(urlString);
        parcel.writeString(fileId);
    }
}
