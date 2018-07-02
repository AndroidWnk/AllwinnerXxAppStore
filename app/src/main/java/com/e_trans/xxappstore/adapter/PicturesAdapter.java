package com.e_trans.xxappstore.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.e_trans.xxappstore.entity.AppDetailsEntity;
import com.e_trans.xxappstore.entity.AppListEntity;
import com.e_trans.xxappstore.utils.ImageLoader;
import com.e_trans.xxappstore.utils.UIUtils;
import com.e_trans.xxdownloadaidl.IDownloadService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 图片展示数据适配器
 *
 * @author wk
 */
public class PicturesAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;
    private int mResource;
    private List<AppDetailsEntity.ImageInfo> images;
    private Context context;
    private ImageLoader mImageLoader;
    private ViewHolder holder;

    public PicturesAdapter(Context context, List<AppDetailsEntity.ImageInfo> images) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.images = images;
        this.mResource = R.layout.pictures_item;
        this.mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageLoader = new ImageLoader(context, R.drawable.app_detail_img_loading);
    }

    public void setListData(List<AppDetailsEntity.ImageInfo> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return images != null && images.size() > 0 ? images.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return images.get(position);
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
        if (images != null && images.size() > 0) {
            mImageLoader.displayImage(images.get(position).imgFileId + "",
                    holder.ivPicture,true);
        }

        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.iv_picture)
        ImageView ivPicture;


        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
