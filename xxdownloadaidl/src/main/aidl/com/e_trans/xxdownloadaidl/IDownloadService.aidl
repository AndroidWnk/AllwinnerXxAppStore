// IDownloadService.aidl
package com.e_trans.xxdownloadaidl;
import com.e_trans.xxdownloadaidl.IDownloadListener;
// Declare any non-default types here with import statements

interface IDownloadService {
void start(String fileId,String fileName);
void pause(String fileId);
void remove(String fileId);
int getDownloaderState(String fileId);
boolean isDownloading(String fileId);
void setDownloadListener(in IDownloadListener iDownloadListener);
}
