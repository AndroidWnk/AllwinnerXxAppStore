package com.e_trans.xxappstore.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.e_trans.xxappstore.constant.Constant;

public class CrashHandler implements UncaughtExceptionHandler {

	Context mContext;
	private UncaughtExceptionHandler defaultUEH;

	static class XxCrashHandlerHolder {
		static CrashHandler instance = new CrashHandler();
	}

	public static CrashHandler getInstance() {
		return XxCrashHandlerHolder.instance;
	}
	
	public void init(Application app){
		mContext = app;
	}

	private CrashHandler() {
		defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public void uncaughtException(Thread thread, Throwable ex) {
		File dbFile = new File(mContext.getDatabasePath("download.db").getPath());
		File dbFile2 = new File(mContext.getDatabasePath("download.db-journal").getPath());
		if(dbFile.exists()){
			dbFile.delete();
		}
		if(dbFile2.exists()){
			dbFile2.delete();
		}
		File apkFile = new File(Constant.sdPath + "/ZhongTai/ApkDownload");
		if(apkFile.exists() && apkFile.isDirectory()){
			File[] childFile = apkFile.listFiles();
			if(childFile != null && childFile.length > 0){
				for(File f : childFile){
					f.delete();
				}
			}
		}

		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		StackTraceElement[] trace = ex.getStackTrace();
		StackTraceElement[] trace2 = new StackTraceElement[trace.length + 3];
		System.arraycopy(trace, 0, trace2, 0, trace.length);
		trace2[trace.length + 0] = new StackTraceElement("Android", "MODEL",
				android.os.Build.MODEL, -1);
		trace2[trace.length + 1] = new StackTraceElement("Android", "VERSION",
				android.os.Build.VERSION.RELEASE, -1);
		trace2[trace.length + 2] = new StackTraceElement("Android",
				"FINGERPRINT", android.os.Build.FINGERPRINT, -1);

		ex.setStackTrace(trace2);
		ex.printStackTrace(printWriter);

		String stacktrace = result.toString();
		printWriter.close();
		//Log.e("XXAndroid", stacktrace);

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			log(stacktrace);
		}
		defaultUEH.uncaughtException(thread, ex);

	}

//	private void log(String string) {
//		StringBuffer path = new StringBuffer(Environment.getExternalStorageDirectory().getPath());
//		if(path.charAt(path.length() - 1) != '/')
//			path.append("/");
//		
//		path.append("brightbean/erro");
//		File file = new File(path.toString());
//		if (!file.exists()) {
//			file.mkdirs();
//		}
//		                                                            
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HHmmss");
//		dateFormat.format(new Date(System.currentTimeMillis()));
//		
//		
//		//path.append(System.currentTimeMillis() + "_erro.txt");
//
//		BufferedWriter write = null;
//		try {
//			write = new BufferedWriter(new FileWriter(path.toString()));
//			write.write(string);
//			write.flush();
//
//		} catch (IOException e) {
//		} finally {
//			if (write != null)
//				try {
//					write.close();
//				} catch (IOException e) {
//				}
//		}
//
//	}
	
	private  void log(String string) {
		StringBuffer path = new StringBuffer(Environment.getExternalStorageDirectory().getPath());
		if(path.charAt(path.length() - 1) != '/')
			path.append("/");
		
		path.append("/ZhongTai/ErrLog");
		File file = new File(path.toString());
		if (!file.exists()) {
			file.mkdirs();
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String times = dateFormat.format(new Date(System.currentTimeMillis()));
		path.append("/AppstoreErr.log");

		BufferedWriter write = null;
		try {
			write = getBufferedWriter(path.toString());
			write.write("*********************************************************************");
			write.newLine();
			write.write(times);
			write.newLine();
			write.write(string);
			write.newLine();
			write.write("***********************************************************************");
			write.flush();

		} catch (IOException e) {
		} finally {
			if (write != null)
				try {
					write.close();
				} catch (IOException e) {
				}
		}
	}
	BufferedWriter getBufferedWriter(String path) throws IOException{
		File file = new File(path);
		if(file.exists()&& file.isFile()){
			//����5M���
			if(file.length() <= 5242880){
				return new BufferedWriter(new FileWriter(path,true)); 
			}
		}
		return new BufferedWriter(new FileWriter(path));
	}
	

	
	
	
}
