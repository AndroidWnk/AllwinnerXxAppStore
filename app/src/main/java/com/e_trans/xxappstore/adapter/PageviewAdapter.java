package com.e_trans.xxappstore.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.e_trans.xxappstore.R;
import com.e_trans.xxappstore.constant.Constant;
import com.e_trans.xxappstore.entity.AppDetailsEntity;
import com.e_trans.xxappstore.request.UrlManager;
import com.google.gson.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wk on 2016/4/22.
 */
public class PageviewAdapter extends PagerAdapter {
    private List<View> mList;
    private AsyncImageLoader asyncImageLoader;
    private List<AppDetailsEntity.ImageInfo> imageList;
    private ImageView image;
    private Context mContext;

    public PageviewAdapter(Context mContext, List<View> list, List<AppDetailsEntity.ImageInfo> imageList) {
        this.mContext = mContext;
        this.mList = list;
        this.imageList = imageList;
        asyncImageLoader = new AsyncImageLoader();
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mList != null && mList.size() > 0 ? mList.size() : 0;
    }

    /**
     * Remove a page for the given position.
     * 滑动过后就销毁 ，销毁当前页的前一个的前一个的页！
     * instantiateItem(View container, int position)
     * This method was deprecated in API level . Use instantiateItem(ViewGroup, int)
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // TODO Auto-generated method stub
        container.removeView(mList.get(position));

    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        // TODO Auto-generated method stub
        return arg0 == arg1;
    }

    /**
     * Create the page for the given position.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        Drawable cachedImage = asyncImageLoader.loadDrawable(
                imageList.get(position).imgFileId + "", new AsyncImageLoader.ImageCallback() {

                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void imageLoaded(Drawable imageDrawable,
                                            String imageUrl) {

                        View view = mList.get(position);
                        image = ((ImageView) view.findViewById(R.id.image));
                        image.setBackground(imageDrawable);
                        container.removeView(mList.get(position));
                        container.addView(mList.get(position));
                        // adapter.notifyDataSetChanged();

                    }
                });
        View view = mList.get(position);
        image = ((ImageView) view.findViewById(R.id.image));
        if (cachedImage == null) {
            image.setBackgroundResource(R.drawable.img_video_loading);
        } else {
            image.setBackground(cachedImage);
        }

        container.removeView(mList.get(position));
        container.addView(mList.get(position));
        // adapter.notifyDataSetChanged();

        return mList.get(position);
    }

    /**
     * 异步加载图片
     */
    static class AsyncImageLoader {

        // 软引用，使用内存做临时缓存 （程序退出，或内存不够则清除软引用）
        private HashMap<String, SoftReference<Drawable>> imageCache;

        public AsyncImageLoader() {
            imageCache = new HashMap<String, SoftReference<Drawable>>();
        }

        /**
         * 定义回调接口
         */
        public interface ImageCallback {
            public void imageLoaded(Drawable imageDrawable, String imageUrl);
        }


        /**
         * 创建子线程加载图片
         * 子线程加载完图片交给handler处理（子线程不能更新ui，而handler处在主线程，可以更新ui）
         * handler又交给imageCallback，imageCallback须要自己来实现，在这里可以对回调参数进行处理
         *
         * @param imageUrl       ：须要加载的图片url
         * @param imageCallback：
         * @return
         */
        public Drawable loadDrawable(final String imageUrl,
                                     final ImageCallback imageCallback) {

            //如果缓存中存在图片  ，则首先使用缓存
            if (imageCache.containsKey(imageUrl)) {
                SoftReference<Drawable> softReference = imageCache.get(imageUrl);
                Drawable drawable = softReference.get();
                if (drawable != null) {
                    imageCallback.imageLoaded(drawable, imageUrl);//执行回调
                    return drawable;
                }
            }

            /**
             * 在主线程里执行回调，更新视图
             */
            final Handler handler = new Handler() {
                public void handleMessage(Message message) {
                    imageCallback.imageLoaded((Drawable) message.obj, imageUrl);
                }
            };


            /**
             * 创建子线程访问网络并加载图片 ，把结果交给handler处理
             */
            new Thread() {
                @Override
                public void run() {
                    Drawable drawable = loadImageFromUrl(imageUrl);
                    // 下载完的图片放到缓存里
                    imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
                    Message message = handler.obtainMessage(0, drawable);
                    handler.sendMessage(message);
                }
            }.start();

            return null;
        }


        /**
         * 下载图片  （注意HttpClient 和httpUrlConnection的区别）
         */
        public Drawable loadImageFromUrl(String url) {

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(UrlManager.getDownloadUrl());
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                JsonObject object = new JsonObject();
                object.addProperty("fileId", Integer.parseInt(url));
                object.addProperty("ID", Constant.deviceId);
                object.addProperty("vin", Constant.vin);
                nameValuePairs.add(new BasicNameValuePair("token", Constant.token));
                nameValuePairs.add(new BasicNameValuePair("data", object.toString()));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();

                Bitmap bitmap = BitmapFactory.decodeStream(is);
                if (bitmap == null) {
                    return null;
                }
                Bitmap mBitmap = null;
                int h = bitmap.getHeight();
                int w = bitmap.getWidth();
                if (h >= 600 || w >= 1024) {
                    if (h > w) {
                        mBitmap = Bitmap.createScaledBitmap(bitmap,
                                (int) (((float) w / h) * 600), 600, true);
                    } else if (h < w) {
                        mBitmap = Bitmap.createScaledBitmap(bitmap,
                                1024, (int) (((float) w / h) * 1024), true);
                    } else if (h == w) {
                        mBitmap = Bitmap.createScaledBitmap(bitmap,
                                600, 600, true);
                    }
                } else {

                }

//                Drawable d = Drawable.createFromStream(is,
//                        "src");
                Drawable d = new BitmapDrawable(mBitmap);
                is.close();
                return d;
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                System.out.println("[getNetWorkBitmap->]MalformedURLException");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("[getNetWorkBitmap->]IOException");
                e.printStackTrace();
            }
            return null;
        }

        //清除缓存
        public void clearCache() {

            if (this.imageCache.size() > 0) {

                this.imageCache.clear();
            }

        }

    }
}
