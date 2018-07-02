package com.e_trans.xxappstore.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.e_trans.xxappstore.R;
import com.e_trans.xxappstore.XxCustomApplication;
import com.e_trans.xxappstore.constant.Constant;
import com.e_trans.xxappstore.entity.AppListEntity;
import com.e_trans.xxappstore.utils.ImageLoader;
import com.e_trans.xxappstore.utils.MD5Util;
import com.e_trans.xxappstore.utils.UIUtils;
import com.e_trans.xxdownloadaidl.IDownloadService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * app列表数据适配器
 *
 * @author wk
 */
public class AppListAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;
    private int mResource;
    private AppListEntity appListEntity;
    private Context context;
    private ImageLoader imageLoader;
    private DecimalFormat df;
    private IDownloadService downBinder;
    private static XxCustomApplication application;
    private ViewHolder holder;
    private Map<Integer, View> itemViews;
    private Intent mIntent = null;

    public AppListAdapter(Context context, AppListEntity appListEntity, XxCustomApplication application) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.appListEntity = appListEntity;
        this.application = application;
        this.mResource = R.layout.app_list_item;
        this.mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(context, R.drawable.ic_list_img_loading);
        df = new DecimalFormat("0.00");
        itemViews = new HashMap<Integer, View>();
    }

    public void setListData(AppListEntity appListEntity) {
        this.appListEntity = appListEntity;
        notifyDataSetChanged();
    }

    public void setDownBinder(IDownloadService downBinder) {
        this.downBinder = downBinder;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return appListEntity != null && appListEntity.data != null
                && appListEntity.data.list != null
                && appListEntity.data.list.size() > 0 ? appListEntity.data.list
                .size() : 0;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return appListEntity != null && appListEntity.data != null
                && appListEntity.data.list != null
                && appListEntity.data.list.size() > 0 ? appListEntity.data.list
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
        if (appListEntity != null && appListEntity.data != null
                && appListEntity.data.list != null
                && appListEntity.data.list.size() > 0) {
            convertView = itemViews.get(appListEntity.data.list
                    .get(position).fileId);
        }
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(mResource, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
            if (appListEntity != null && appListEntity.data != null
                    && appListEntity.data.list != null
                    && appListEntity.data.list.size() > 0) {
                itemViews.put(appListEntity.data.list
                        .get(position).fileId, convertView);
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (appListEntity != null && appListEntity.data != null
                && appListEntity.data.list != null
                && appListEntity.data.list.size() > 0) {
            if (appListEntity.data.list.get(position) != null) {

                imageLoader.displayImage(appListEntity.data.list.get(position).iconFileId + "", holder.appIcon, true);

                holder.appName.setText(appListEntity.data.list.get(position).appName);
                holder.appDownloadCounts.setText(formateCount(appListEntity.data.list.get(position).downNum));
                holder.appSize.setText(formateAppSize(appListEntity.data.list.get(position).fileSize));
                holder.appStars.setRating(appListEntity.data.list.get(position).recommendLvl);
                switch (appListEntity.data.list.get(position).appState) {
                    case 0:
                        File localFile = new File(Constant.appFilePath + appListEntity.data.list.get(position).appName + ".apk");
                        if (localFile.exists() && localFile.length() > 0 &&
                                isFileComplete(localFile.getPath(),appListEntity.data.list.get(position).md5)) {
                            PackageManager pm = context.getPackageManager();
                            PackageInfo packageInfo = pm.getPackageArchiveInfo(Constant.appFilePath + appListEntity.data.list.get(position).appName + ".apk", PackageManager.GET_ACTIVITIES);
                            if (packageInfo != null) {
                                String version = packageInfo.versionName;//得到本机apk包版本信息
                                if (version.compareToIgnoreCase(appListEntity.data.list.get(position).version) < 0) {
                                    localFile.delete();
                                    holder.appState.setText(context.getString(R.string.text_Uninstalled));
                                } else {
                                    holder.appState.setText(context.getString(R.string.text_Install));
                                }
                            } else {
                                holder.appState.setText(context.getString(R.string.text_Install));
                            }
                        } else {
                            holder.appState.setText(context.getString(R.string.text_Uninstalled));
                            if(localFile.exists()){
                                localFile.delete();
                            }
                        }
                        break;
                    case 1:
                        holder.appState.setText(context.getString(R.string.text_Donwloading));
                        break;
                    case 2:
                        holder.appState.setText(context.getString(R.string.text_Installing));
                        break;
                    case 3:
                        if (searchAndResetAppState(position)) {
                            holder.appState.setText(context.getString(R.string.text_Update));
                        } else {
                            holder.appState.setText(context.getString(R.string.text_Installed));
                        }
                        break;
                }
                holder.appState.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (appListEntity.data.list.get(position).appState == 0) {//该应用未安装
                            File localFile = new File(Constant.appFilePath + appListEntity.data.list.get(position).appName + ".apk");
                            if (localFile.exists() && localFile.length() > 0 &&
                                    isFileComplete(localFile.getPath(),
                                            appListEntity.data.list.get(position).md5)) {//有安装包，直接安装
                                appListEntity.data.list.get(position).appState = 2;
                                notifyDataSetChanged();
                                //安装app
                                new AsyncTask<Void, Void, Integer>() {
                                    @Override
                                    protected Integer doInBackground(Void... voids) {
                                        mIntent = new Intent("installingApp");
                                        mIntent.putExtra("fileId", appListEntity.data.list.get(position).fileId);
                                        context.sendBroadcast(mIntent);
                                        int result = installAppSilence(Constant.appFilePath + appListEntity.data.list.get(position).appName + ".apk");
                                        return result;
                                    }

                                    @Override
                                    protected void onPostExecute(Integer integer) {
                                        super.onPostExecute(integer);
                                        if (integer == 0) {//安装成功
                                            mIntent = new Intent("installSuccess");
                                            mIntent.putExtra("fileId", appListEntity.data.list.get(position).fileId);
                                            context.sendBroadcast(mIntent);
                                            UIUtils.showToast(context, context.getString(R.string.text_Installation_completed), 2);
                                            appListEntity.data.list.get(position).appState = 3;
                                            notifyDataSetChanged();
                                        } else {//安装失败
                                            mIntent = new Intent("installFailed");
                                            mIntent.putExtra("fileId", appListEntity.data.list.get(position).fileId);
                                            context.sendBroadcast(mIntent);
                                            UIUtils.showToast(context, context.getString(R.string.text_Installation_failed), 2);
                                            File file = new File(Constant.appFilePath + appListEntity.data.list.get(position).appName + ".apk");
                                            if (file.exists()) {
                                                file.delete();
                                            }
                                            appListEntity.data.list.get(position).appState = 0;
                                            notifyDataSetChanged();
                                        }
                                    }
                                }.execute();
                            } else {//无安装包，下载
                                if(localFile.exists()){
                                    localFile.delete();
                                }
                                if (isNetworkAvailable(context)) {
                                    appListEntity.data.list.get(position).appState = 1;
                                    notifyDataSetChanged();
                                    boolean isHasTask = false;
                                    for (AppListEntity.AppInfo info : application.downloadList) {
                                        if (info != null && info.fileId == appListEntity.data.list.get(position).fileId) {
                                            isHasTask = true;
                                        }
                                    }
                                    if (!isHasTask) {
                                        application.downloadList.add(appListEntity.data.list.get(position));
                                    }
//                                    new Thread(new Runnable() {
//                                        @Override
//                                        public void run() {
//
//                                        }
//                                    }).start();
                                    try {
                                        if (downBinder != null)
                                            downBinder.start(appListEntity.data.list.get(position).fileId + ""
                                                    , appListEntity.data.list.get(position).appName);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    if(application.isBeyond){
                                        UIUtils.showToast(context, context.getString(R.string.string_tips_no_traffic), 2);
                                    }else{
                                        UIUtils.showToast(context, context.getString(R.string.string_tips_no_network), 2);
                                    }
                                }
                            }
                        } else if (appListEntity.data.list.get(position).appState == 3) {
                            if (!searchAndResetAppState(position)) {
                                //卸载该app
                                uninstallApp(appListEntity.data.list.get(position).packName);
                            } else {
                                if (isNetworkAvailable(context)) {
                                    appListEntity.data.list.get(position).appState = 1;
                                    notifyDataSetChanged();
                                    application.downloadList.add(appListEntity.data.list.get(position));
                                    try {
                                        if (downBinder != null)
                                            downBinder.start(appListEntity.data.list.get(position).fileId + ""
                                                    , appListEntity.data.list.get(position).appName);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    if(application.isBeyond){
                                        UIUtils.showToast(context, context.getString(R.string.string_tips_no_traffic), 2);
                                    }else{
                                        UIUtils.showToast(context, context.getString(R.string.string_tips_no_network), 2);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.app_icon)
        ImageView appIcon;
        @Bind(R.id.app_name)
        TextView appName;
        @Bind(R.id.app_download_counts)
        TextView appDownloadCounts;
        @Bind(R.id.app_size)
        TextView appSize;
        @Bind(R.id.app_stars)
        RatingBar appStars;
        @Bind(R.id.app_state)
        TextView appState;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private String formateCount(int count) {
        String counts = "";
        if (count < 10000) {
            counts = count + context.getString(R.string.string_downloads);
        } else if (count < 100000000) {
            counts = count / 10000 + context.getString(R.string.string_downloads);
        } else {
            String sizeMod = count % 100000000 + "";
            counts = count / 100000000 + "." + sizeMod.charAt(0) + "亿次下载";
        }
        return counts;
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

    private boolean searchAndResetAppState(int position) {
        boolean hasUpdata = false;
        List<PackageInfo> appList = context.getPackageManager().getInstalledPackages(0);
        List<String> pkgList = new ArrayList<String>();
        for (PackageInfo info : appList) {
            pkgList.add(info.packageName);
            if (info.packageName.equals(appListEntity.data.list.get(position).packName)) {
                String localVersion = info.versionName;
//                if (localVersion.contains(".")) {
//                    localVersion = localVersion.replaceAll("\\.", "");
//                }
//                if (appInfo.version.contains(".")) {
//                    appInfo.version = appInfo.version.replaceAll("\\.", "");
//                }
                if (localVersion.compareToIgnoreCase(appListEntity.data.list.get(position).version) < 0) {
                    hasUpdata = true;
                } else {
                    hasUpdata = false;
                }
//                if (Integer.parseInt(appInfo.version) > Integer.parseInt(localVersion)) {
//                    hasUpdata = true;
//                } else {
//                    hasUpdata = false;
//                }
            }
        }
        return hasUpdata;
    }

    /**
     * 静默安装
     *
     * @param filePath
     * @return
     */
    public int installAppSilence(String filePath) {
//        File file = new File(filePath);
//        if(!file.exists())
//            return -1;
//        PrintWriter PrintWriter = null;
//        Process process = null;
//        try {
//            process = Runtime.getRuntime().exec("su");
//            PrintWriter = new PrintWriter(process.getOutputStream());
//            PrintWriter.println("chmod 777 " + filePath);
//            PrintWriter.println("export LD_LIBRARY_PATH=/vendor/lib:/system/lib");
//            PrintWriter.println("pm install -r " + filePath);
////          PrintWriter.println("exit");
//            PrintWriter.flush();
//            PrintWriter.close();
//            int value = process.waitFor();
//            return value;
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (process != null) {
//                process.destroy();
//            }
//        }
//        return -1;
        File file = new File(filePath);
        if (filePath == null || filePath.length() == 0
                || (file = new File(filePath)) == null || file.length() <= 0
                || !file.exists() || !file.isFile()) {
            return 1;
        }
        String[] args = {"pm", "install", "-r", filePath};
        ProcessBuilder processBuilder = new ProcessBuilder(args);

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result = 0;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            String s;

            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }

            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
            Log.i("installAppSilence", "errorMsg: " + errorMsg.toString());
        } catch (IOException e) {
            e.printStackTrace();
            result = 2;
        } catch (Exception e) {
            e.printStackTrace();
            result = 2;
        } finally {
            try {
                if (successMsg.toString() == null || !successMsg.toString().contains("Success")) {
                    result = 3;
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }

    /**
     * 卸载App
     *
     * @param packageName
     */
    private void uninstallApp(final String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(uninstallIntent);
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

    /**
     * 判断文件是否是完整正确可用的
     * @param filePath
     * @param fileMD5
     * @return
     */
    private boolean isFileComplete(String filePath,String fileMD5) {
        boolean isComplete = false;
        String md5 = MD5Util.getFileMD5(filePath);
        if(md5 != null && fileMD5.equals(md5)){
            isComplete = true;
        }
        return isComplete;
    }
}
