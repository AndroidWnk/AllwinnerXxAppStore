package com.e_trans.xxappstore.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import com.e_trans.xxappstore.constant.Constant;
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

public class ImageLoader {

	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	// 线程池
	ExecutorService executorService;
	int stub_id;// 当进入listview时默认的图片，可换成你自己的默认图片
	Context context;
	boolean isSpecial = false;

	public ImageLoader(Context context,int default_id) {
		this.context = context;
		this.stub_id = default_id;
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
	}

	// 最主要的方法
	public void displayImage(String url, ImageView imageView,boolean isSpecial) {
		
		this.isSpecial = isSpecial;
		
		imageViews.put(imageView, url);
		// 先从内存缓存中查找

		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null){
			if(isSpecial){
				imageView.setImageBitmap(null);
				imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}else{
				imageView.setImageBitmap(bitmap);
			}
//			imageView.setImageBitmap(bitmap);
		}else {
			// 若没有的话则开启新线程加载图片
			queuePhoto(url, imageView);
			imageView.setImageResource(stub_id);
		}
	}

	private void queuePhoto(String url, ImageView imageView) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url) {
		File f = fileCache.getFile(url);

		// 先从文件缓存中查找是否有
		Bitmap b = decodeFile(f);
		if (b != null)
			return b;

		// 最后从指定的url中下载图片
		try {
			Bitmap bitmap = null;
			Bitmap mBitmap = null;

//			URL imageUrl = new URL(url);
//			HttpURLConnection conn = (HttpURLConnection) imageUrl
//					.openConnection();
//			conn.setConnectTimeout(30000);
//			conn.setReadTimeout(30000);
//			conn.setInstanceFollowRedirects(true);

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

//			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			CopyStream(is, os);
			os.close();
			bitmap = decodeFile(f);
			mBitmap = Bitmap.createScaledBitmap(bitmap,
					dip2px(context, 100),
					dip2px(context, 100), true);
			return mBitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// decode这个图片并且按比例缩放以减少内存消耗，虚拟机对每张图片的缓存大小也是有限制的
	private Bitmap decodeFile(File f) {
		Bitmap bitmap = null;
		Bitmap mBitmap = null;
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 100;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
			mBitmap = Bitmap.createScaledBitmap(bitmap,
					dip2px(context, 100),
					dip2px(context, 100), true);
			return mBitmap;
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = getBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			// 更新的操作放在UI线程中
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	/**
	 * 防止图片错位
	 * 
	 * @param photoToLoad
	 * @return
	 */
	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// 用于在UI线程中更新界面
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null){
				if(isSpecial){
					photoToLoad.imageView.setImageBitmap(null);
					photoToLoad.imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
				}else{
					photoToLoad.imageView.setImageBitmap(bitmap);
				}
//				photoToLoad.imageView.setImageBitmap(bitmap);
			}else{
				photoToLoad.imageView.setImageResource(stub_id);
			}
		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}
	/**
	 * 将dip或dp值转换为px值，保证尺寸大小不变
	 * 
	 * @param dipValue
	 * @param scale
	 *            （DisplayMetrics类中属性density）
	 * @return
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
}