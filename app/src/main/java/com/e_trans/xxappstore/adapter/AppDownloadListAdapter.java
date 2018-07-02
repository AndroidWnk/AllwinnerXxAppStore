package com.e_trans.xxappstore.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.e_trans.xxappstore.R;
import com.e_trans.xxappstore.XxCustomApplication;
import com.e_trans.xxappstore.constant.Constant;
import com.e_trans.xxappstore.downloadservice.DownLoader;
import com.e_trans.xxappstore.entity.AppListEntity;
import com.e_trans.xxappstore.entity.DownloadUpdateViewEntity;
import com.e_trans.xxappstore.utils.ImageLoader;
import com.e_trans.xxappstore.utils.UIUtils;
import com.e_trans.xxdownloadaidl.IDownloadService;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * app下载列表数据适配器
 *
 * @author wk
 */
public class AppDownloadListAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;
    private int mResource;
    private Context context;
    private ImageLoader imageLoader;
    private DecimalFormat df;
    //    private AppListEntity.AppInfo appInfo = null;
    private IDownloadService downBinder;
    private static XxCustomApplication application;
    //    private ViewHolder holder;
    public List<AppListEntity.AppInfo> downloadList;
    private TextView noTaskHint;
    private Map<Integer, DownloadUpdateViewEntity> updateViewMap = new HashMap<Integer, DownloadUpdateViewEntity>();

    public AppDownloadListAdapter(Context context, TextView noTaskHint, XxCustomApplication application) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.noTaskHint = noTaskHint;
        this.downloadList = application.downloadList;
        this.application = application;
        this.mResource = R.layout.app_download_item;
        this.mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(context, R.drawable.ic_list_img_loading);
        df = new DecimalFormat("0.00");
        for (int i = 0; i < downloadList.size() - 1; i++) {
            for (int j = downloadList.size() - 1; j > i; j--) {
                if (downloadList.get(j).fileId == downloadList.get(i).fileId) {
                    downloadList.remove(j);
                }
            }
        }
    }

    public void setListData(List<AppListEntity.AppInfo> downloadList) {
        this.downloadList = downloadList;
        for (int i = 0; i < downloadList.size() - 1; i++) {
            for (int j = downloadList.size() - 1; j > i; j--) {
                if (downloadList.get(j).fileId == downloadList.get(i).fileId) {
                    downloadList.remove(j);
                }
            }
        }
        if (application.downloadList.size() == 0) {
            noTaskHint.setVisibility(View.VISIBLE);
        } else {
            noTaskHint.setVisibility(View.GONE);
        }
        if(updateViewMap != null){
            updateViewMap.clear();
        }
        notifyDataSetChanged();
    }

    public void setDownBinder(IDownloadService downBinder) {
        this.downBinder = downBinder;
    }

    public DownloadUpdateViewEntity getUpdateView(int fileId) {
        return updateViewMap.get(fileId);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return downloadList != null
                && downloadList.size() > 0 ? downloadList
                .size() : 0;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return downloadList != null
                && downloadList.size() > 0 ? downloadList
                .get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
//        if (convertView == null) {
//            convertView = mLayoutInflater.inflate(mResource, null);
//            holder = new ViewHolder(convertView);
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
        convertView = mLayoutInflater.inflate(mResource, null);

        if (downloadList != null && downloadList.size() > 0 && position < downloadList.size()) {
//            appInfo = downloadList.get(position);
            if (downloadList.get(position) != null) {

                ImageView appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
                TextView appName = (TextView) convertView.findViewById(R.id.app_name);
                ProgressBar downloadProgressbar = (ProgressBar) convertView.findViewById(R.id.download_progressbar);
                TextView progressPercent = (TextView) convertView.findViewById(R.id.progress_percent);
                TextView appState = (TextView) convertView.findViewById(R.id.app_state);
                TextView fileSize = (TextView) convertView.findViewById(R.id.file_size);
                final ImageView downloadControl = (ImageView) convertView.findViewById(R.id.download_control);
                ImageView deleteTask = (ImageView) convertView.findViewById(R.id.delete_task);
                DownloadUpdateViewEntity updateViewEntity = new DownloadUpdateViewEntity();
                updateViewEntity.setProgressBar(downloadProgressbar);
                updateViewEntity.setTextView(progressPercent);
                updateViewMap.put(downloadList.get(position).fileId, updateViewEntity);

                imageLoader.displayImage(downloadList.get(position).iconFileId + "", appIcon, true);

                appName.setText(downloadList.get(position).appName);
                fileSize.setText(formateAppSize(downloadList.get(position).fileSize));
                try {
                    if (downBinder != null) {
                        if (downBinder.getDownloaderState(downloadList.get(position).fileId + "") == 2) {
                            downloadControl.setBackgroundResource(R.drawable.pause);
                        } else if (downBinder.getDownloaderState(downloadList.get(position).fileId + "") == 3) {
                            downloadControl.setBackgroundResource(R.drawable.start);
                            appState.setText(context.getString(R.string.status_pause));
                        } else if (downBinder.getDownloaderState(downloadList.get(position).fileId + "") == 4) {
                            downloadControl.setBackgroundResource(R.drawable.wait);
                            appState.setText(context.getString(R.string.status_waiting));
                        }else if (downBinder.getDownloaderState(downloadList.get(position).fileId + "") == -1){
                            appState.setText(context.getString(R.string.status_download_failure));
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                downloadControl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (downloadList != null && downloadList.size() > 0 &&
                                position < downloadList.size() && downloadList.get(position) != null) {
                            try {
                                if (downBinder != null) {
                                    if (downBinder.getDownloaderState(downloadList.get(position).fileId + "") == 4)
                                        return;
                                    if (downBinder.isDownloading(downloadList.get(position).fileId + "")) {
                                        downBinder.pause(downloadList.get(position).fileId + "");
                                        downloadControl.setBackgroundResource(R.drawable.start);
                                    } else {
                                        if (isNetworkAvailable(context)) {
                                            downBinder.start(downloadList.get(position).fileId + "",
                                                    downloadList.get(position).appName);
                                            downloadControl.setBackgroundResource(R.drawable.pause);
                                        } else {
                                            if(application.isBeyond){
                                                UIUtils.showToast(context, context.getString(R.string.string_tips_no_traffic), 2);
                                            }else{
                                                UIUtils.showToast(context, context.getString(R.string.string_tips_no_network), 2);
                                            }
                                        }
                                    }
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                deleteTask.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(downloadList != null && position < downloadList.size() && downloadList.get(position) !=null ){
                            showHintDialog(downloadList.get(position).fileId + "", downloadList.get(position).appName);
                        }
                    }
                });
                DownLoader downLoader = application.downLoaders.get(downloadList.get(position).fileId + "");
                if (downLoader != null && downLoader.isDownloading()) {
                    downloadProgressbar.setVisibility(View.VISIBLE);
                    downloadProgressbar.setMax(downloadList.get(position).fileSize);
                    appState.setVisibility(View.GONE);
                    progressPercent.setVisibility(View.VISIBLE);
                    fileSize.setVisibility(View.VISIBLE);
                } else {
                    downloadProgressbar.setVisibility(View.GONE);
                    downloadProgressbar.setMax(downloadList.get(position).fileSize);
                    appState.setVisibility(View.VISIBLE);
                    progressPercent.setVisibility(View.GONE);
                    fileSize.setVisibility(View.GONE);
                }
            }
        }
        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.app_icon)
        ImageView appIcon;
        @Bind(R.id.app_name)
        TextView appName;
        @Bind(R.id.download_progressbar)
        ProgressBar downloadProgressbar;
        @Bind(R.id.progress_percent)
        TextView progressPercent;
        @Bind(R.id.app_state)
        TextView appState;
        @Bind(R.id.file_size)
        TextView fileSize;
        @Bind(R.id.download_control)
        ImageView downloadControl;
        @Bind(R.id.delete_task)
        ImageView deleteTask;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private String formateAppSize(int size) {
        String sizeStr = "";
        if (size < 1024) {
            sizeStr = df.format(size) + "B";
        } else if (size < 1024 * 1024) {
            sizeStr = df.format((double) (size) / 1024) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            sizeStr = df.format((double) (size) / (1024 * 1024)) + "MB";
        } else {
            sizeStr = df.format((double) (size) / (1024 * 1024 * 1024)) + "GB";
        }
        return sizeStr;
    }

    private void showHintDialog(final String fileId, final String fileName) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.string_delete));
        builder.setTitle(context.getString(R.string.string_note));
        builder.setPositiveButton(context.getString(R.string.string_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (hasFileId(fileId)) {
                    try {
                        if (downBinder != null)
                            downBinder.remove(fileId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    File file = new File(Constant.appFilePath + fileName + ".apk");
                    if (file.exists()) {
                        file.delete();//已经下载的临时文件删除掉防止下次再下载造成数据重复
                    }
                    //发广播通知列表刷新状态
                    Intent mIntent = new Intent("deletetask");
                    mIntent.putExtra("fileId", Integer.parseInt(fileId));
                    context.sendBroadcast(mIntent);

                    for (int i = 0; i < downloadList.size(); i++) {
                        if (downloadList.get(i).fileId == Integer.parseInt(fileId)) {
                            downloadList.remove(i);
                        }
                    }
                    if (application.downloadList.size() == 0) {
                        noTaskHint.setVisibility(View.VISIBLE);
                    }
                }
                if(updateViewMap != null){
                    updateViewMap.clear();
                }
                notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(context.getString(R.string.string_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private boolean hasFileId(String fileid) {
        boolean hasFileId = false;
        for (AppListEntity.AppInfo info : downloadList) {
            if (info != null && info.fileId == Integer.parseInt(fileid)) {
                hasFileId = true;
                break;
            }

        }
        return hasFileId;
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo mobNetInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
                return false;
            }

            if(!application.isBeyond || wifiNetInfo.isConnected()){
                return true;
            }
        }
        return false;
    }
}
