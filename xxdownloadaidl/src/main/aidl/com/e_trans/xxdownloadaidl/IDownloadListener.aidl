// IDownloadListener.aidl
package com.e_trans.xxdownloadaidl;
import com.e_trans.xxdownloadaidl.DownloadInfo;
// Declare any non-default types here with import statements

interface IDownloadListener {
            //void onSetProgress(in DownloadInfo info);
            void onUpdateProgress(int length,String fileId);
            void onDownloadFinish();
            void onDownloadStateChange();
           //void onPauseProgress();
           // void onRemoveProgress();
}
