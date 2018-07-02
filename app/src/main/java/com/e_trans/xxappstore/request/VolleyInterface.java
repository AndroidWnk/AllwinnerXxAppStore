package com.e_trans.xxappstore.request;

import android.content.Context;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.e_trans.xxappstore.view.DialogLoadingView;

import org.json.JSONObject;

public abstract class VolleyInterface {
	public Context mContext;
	public static Listener<String> mListener;
	public static Listener<JSONObject> mJsonListener;
	public static ErrorListener mErrorListener;
	private static DialogLoadingView loadingView;
	private int RREQUESTS_STATE;
	/** 显示加载框 */
	public static final int RREQUESTS_STATE_SHOW_DIALOG = 0;
	/** 销毁加载框 */
	public static final int RREQUESTS_STATE_DISMISS_DIALOG = 1;
	/** 隐藏加载框 */
	public static final int RREQUESTS_STATE_HIDDEN_DIALOG = 2;
	/** 显示一次加载框 */
	public static final int RREQUESTS_STATE_SHOW_ONCE_DIALOG = 3;

	public VolleyInterface(Context context, Listener<String> listener,
			ErrorListener errorListener) {
		this.mContext = context;
		VolleyInterface.mListener = listener;
		VolleyInterface.mErrorListener = errorListener;
	}

	public VolleyInterface(Context context, Listener<String> listener,
			ErrorListener errorListener, int RREQUESTS_STATE) {
		this.mContext = context;
		this.RREQUESTS_STATE = RREQUESTS_STATE;
		VolleyInterface.mListener = listener;
		VolleyInterface.mErrorListener = errorListener;

		if (loadingView == null) {
			loadingView = new DialogLoadingView(mContext, null);
		}

		if (loadingView != null)
			if (RREQUESTS_STATE == RREQUESTS_STATE_SHOW_DIALOG
					|| RREQUESTS_STATE == RREQUESTS_STATE_SHOW_ONCE_DIALOG)
				if(mContext != null)
				loadingView.show();
	}

	public Listener<String> successfullyListener() {
		mListener = new Listener<String>() {
			@Override
			public void onResponse(String arg0) {
				// 弹出加载对话框
				onSuccessfullyListener(arg0);
				if (loadingView != null)
					if (RREQUESTS_STATE == RREQUESTS_STATE_DISMISS_DIALOG
							|| RREQUESTS_STATE == RREQUESTS_STATE_SHOW_ONCE_DIALOG) {
						loadingView.dismiss();
						loadingView = null;
					}
			}
		};
		return mListener;
	}

	public Listener<JSONObject> jsonSuccessfullyListener() {
		mJsonListener = new Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject arg0) {
				// 弹出加载对话框
				onSuccessfullyListener(arg0.toString());
				if (loadingView != null)
					if (RREQUESTS_STATE == RREQUESTS_STATE_DISMISS_DIALOG
							|| RREQUESTS_STATE == RREQUESTS_STATE_SHOW_ONCE_DIALOG) {
						if(mContext != null)
						loadingView.dismiss();
						loadingView = null;
					}
			}
		};
		return mJsonListener;
	}

	public ErrorListener errorListener() {
		mErrorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				onErrorListener(arg0);
				if (loadingView != null)
					if (RREQUESTS_STATE == RREQUESTS_STATE_DISMISS_DIALOG
							|| RREQUESTS_STATE == RREQUESTS_STATE_SHOW_ONCE_DIALOG) {
						if(mContext != null)
						loadingView.dismiss();
						loadingView = null;
					}
			}
		};
		return mErrorListener;
	}

	public abstract void onSuccessfullyListener(String result);

	public abstract void onErrorListener(VolleyError error);

}
