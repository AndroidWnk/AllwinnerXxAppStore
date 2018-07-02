package com.e_trans.xxappstore.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.e_trans.xxappstore.R;
import com.e_trans.xxappstore.XxCustomApplication;
import com.e_trans.xxappstore.constant.Constant;
import com.e_trans.xxappstore.db.DBHelper;
import com.e_trans.xxappstore.entity.AppListEntity;
import com.e_trans.xxappstore.utils.ImageLoader;
import com.e_trans.xxappstore.utils.MD5Util;
import com.e_trans.xxappstore.utils.UIUtils;
import com.e_trans.xxdownloadaidl.IDownloadService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * app下载历史数据适配器
 *
 * @author wk
 */
public class AppHistoryListAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;
    private int mResource;
    private Context context;
    private ImageLoader imageLoader;
    private DecimalFormat df;
    private ViewHolder holder;
    public List<AppListEntity.AppInfo> historyList;
    private TextView noHistoryHint;
    private Intent mIntent = null;

    public AppHistoryListAdapter(Context context, List<AppListEntity.AppInfo> historyList, TextView noHistoryHint) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.historyList = historyList;
        this.noHistoryHint = noHistoryHint;
        this.mResource = R.layout.app_download_history_item;
        this.mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(context, R.drawable.ic_list_img_loading);
        df = new DecimalFormat("0.00");
    }

    public void setListData(List<AppListEntity.AppInfo> historyList) {
        this.historyList = historyList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return historyList != null
                && historyList.size() > 0 ? historyList
                .size() : 0;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return historyList != null
                && historyList.size() > 0 ? historyList
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
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(mResource, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (historyList != null && historyList.size() > 0) {
            if (historyList.get(position) != null) {

                imageLoader.displayImage(historyList.get(position).iconFileId + "", holder.appIcon, true);

                holder.appName.setText(historyList.get(position).appName);
                holder.fileSize.setText(formateAppSize(historyList.get(position).fileSize));
                switch (historyList.get(position).appState) {
                    case 0:
                        File localFile = new File(Constant.appFilePath + historyList.get(position).appName + ".apk");
                        if (localFile.exists() && localFile.length() > 0) {
                            PackageManager pm = context.getPackageManager();
                            PackageInfo packageInfo = pm.getPackageArchiveInfo(Constant.appFilePath + historyList.get(position).appName + ".apk", PackageManager.GET_ACTIVITIES);
                            if (packageInfo != null) {
                                String version = packageInfo.versionName;//得到本机apk包版本信息
                                if (version != null && historyList != null && historyList.get(position) != null && historyList.get(position).version != null) {
                                    if (version.compareToIgnoreCase(historyList.get(position).version) < 0) {
                                        localFile.delete();
                                        holder.appState.setText("未安装");
                                    } else {
                                        holder.appState.setText("安装");
                                    }
                                } else {
                                    holder.appState.setText("安装");
                                }
                            } else {
                                holder.appState.setText("安装");
                            }
                        } else {
                            holder.appState.setText("未安装");
                        }
                        break;
                    case 1:
                        holder.appState.setText("下载中");
                        break;
                    case 2:
                        holder.appState.setText("安装中");
                        break;
                    case 3:
                        holder.appState.setText("卸载");
                        break;
                }
                holder.appState.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (historyList.get(position).appState == 0) {//该应用未安装
                            File localFile = new File(Constant.appFilePath + historyList.get(position).appName + ".apk");
                            if (localFile.exists()) {//有安装包，直接安装
                                if (isFileComplete(localFile.getPath(), historyList.get(position).md5)) {
                                    mIntent = new Intent("installingApp");
                                    mIntent.putExtra("fileId", historyList.get(position).fileId);
                                    context.sendBroadcast(mIntent);
                                    historyList.get(position).appState = 2;
                                    notifyDataSetChanged();
                                    //安装app
                                    new AsyncTask<Void, Void, Integer>() {
                                        int fileid;
                                        String filename = "";

                                        @Override
                                        protected Integer doInBackground(Void... voids) {
                                            fileid = historyList.get(position).fileId;
                                            filename = historyList.get(position).appName;
                                            return installAppSilence(Constant.appFilePath + historyList.get(position).appName + ".apk");
                                        }

                                        @Override
                                        protected void onPostExecute(Integer integer) {
                                            super.onPostExecute(integer);
                                            if (integer == 0) {//安装成功
                                                mIntent = new Intent("installSuccess");
                                                if (historyList != null && historyList.size() > 0 && position < historyList.size()) {
                                                    mIntent.putExtra("fileId", historyList.get(position).fileId);
                                                }
                                                context.sendBroadcast(mIntent);
                                                UIUtils.showToast(context, "安装成功", 2);
                                                for (int i = 0; i < historyList.size(); i++) {
                                                    if (historyList.get(i).fileId == fileid) {
                                                        historyList.get(i).appState = 3;
                                                    }
                                                }
                                                notifyDataSetChanged();
                                            } else {//安装失败
                                                mIntent = new Intent("installFailed");
                                                mIntent.putExtra("fileId", historyList.get(position).fileId);
                                                context.sendBroadcast(mIntent);
                                                UIUtils.showToast(context, "安装失败，请重试", 2);
                                                DBHelper dbHelper = new DBHelper(context);
                                                SQLiteDatabase db = dbHelper.getReadableDatabase();
                                                db.delete("download_histroy_info", "file_id=?", new String[]{fileid + ""});
                                                File file = new File(Constant.appFilePath + filename + ".apk");
                                                if (file.exists()) {
                                                    file.delete();
                                                }
                                                for (int i = 0; i < historyList.size(); i++) {
                                                    if (historyList.get(i).fileId == fileid) {
                                                        historyList.remove(i);
                                                    }
                                                }
                                                if (historyList.size() == 0) {
                                                    noHistoryHint.setVisibility(View.VISIBLE);
                                                }
                                                notifyDataSetChanged();
                                            }
                                        }
                                    }.execute();
                                } else {
                                    if (localFile.exists()) {
                                        localFile.delete();
                                    }
                                    Toast.makeText(context, historyList.get(position).appName + "安装失败", Toast.LENGTH_SHORT).show();
                                    DBHelper dbHelper = new DBHelper(context);
                                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                                    db.delete("download_histroy_info", "file_id=?", new String[]{historyList.get(position).fileId + ""});
                                    File file = new File(Constant.appFilePath + historyList.get(position).appName + ".apk");
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                    historyList.remove(position);
                                    if (historyList.size() == 0) {
                                        noHistoryHint.setVisibility(View.VISIBLE);
                                    }
                                    notifyDataSetChanged();
                                }
                            }
                        } else if (historyList.get(position).appState == 3) {
                            //卸载该app
                            uninstallApp(historyList.get(position).packName);
                        }
                    }
                });
                holder.deleteTask.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showHintDialog(position);
                    }
                });
                if (historyList.size() == 0) {
                    noHistoryHint.setVisibility(View.VISIBLE);
                } else {
                    noHistoryHint.setVisibility(View.GONE);
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
        @Bind(R.id.file_size)
        TextView fileSize;
        @Bind(R.id.app_state)
        TextView appState;
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

    private void showHintDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("是否删除该记录？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                DBHelper dbHelper = new DBHelper(context);
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                db.delete("download_histroy_info", "file_id=?", new String[]{historyList.get(position).fileId + ""});
                File file = new File(Constant.appFilePath + historyList.get(position).appName + ".apk");
                if (file.exists()) {
                    file.delete();
                }
                historyList.remove(position);
                if (historyList.size() == 0) {
                    noHistoryHint.setVisibility(View.VISIBLE);
                }
                notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
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
     * 判断文件是否是完整正确可用的
     *
     * @param filePath
     * @param fileMD5
     * @return
     */
    private boolean isFileComplete(String filePath, String fileMD5) {
        boolean isComplete = false;
        String md5 = MD5Util.getFileMD5(filePath);
        if (md5 != null && fileMD5.equals(md5)) {
            isComplete = true;
        }
        return isComplete;
    }
}
