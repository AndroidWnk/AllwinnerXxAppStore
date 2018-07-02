package com.e_trans.xxappstore.request;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.e_trans.xxappstore.XxCustomApplication;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VolleyRequest {
	public static final String TAG = "VolleyRequest";
	public static StringRequest stringRequest;
	public static JsonRequest<JSONObject> jsonRequest;

	public static void RequestGet(Context mContext, String url, String tag,
			VolleyInterface vif) {
		XxCustomApplication.getHttpQueues().cancelAll(tag);
		stringRequest = new StringRequest(url, vif.successfullyListener(),
				vif.errorListener()) {
//			@Override
//			public Map<String, String> getHeaders() throws AuthFailureError {
//				HashMap<String, String> headers = new HashMap<String, String>();
//				//设置header
//				return headers;
//			}
			@Override
			protected Response<String> parseNetworkResponse(NetworkResponse arg0) {
				// TODO Auto-generated method stub
				// System.out.println("statusCode==>" + arg0.statusCode);
				return super.parseNetworkResponse(arg0);
			}
		};
		stringRequest.setTag(tag);
		XxCustomApplication.getHttpQueues().add(stringRequest);
	}

	public static void RequestPost(Context context, String url, String tag,
			final Map<String, String> params, VolleyInterface vif) {
		XxCustomApplication.getHttpQueues().cancelAll(tag);
		stringRequest = new StringRequest(Method.POST, url,
				vif.successfullyListener(), vif.errorListener()) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				return params;
			}

//			@Override
//			public Map<String, String> getHeaders() throws AuthFailureError {
//				HashMap<String, String> headers = new HashMap<String, String>();
//				//设置header
//				return headers;
//			}

			@Override
			protected Response<String> parseNetworkResponse(NetworkResponse arg0) {
				// TODO Auto-generated method stub
				// System.out.println("statusCode==>" + arg0.statusCode);
				return super.parseNetworkResponse(arg0);
			}
		};
		stringRequest.setTag(tag);
		XxCustomApplication.getHttpQueues().add(stringRequest);
	}

	public static void RequestPostJson(Context context, String url, String tag,
			JSONObject jsonObject, VolleyInterface vif) {
		XxCustomApplication.getHttpQueues().cancelAll(tag);
		jsonRequest = new JsonObjectRequest(Method.POST, url, jsonObject,
				vif.jsonSuccessfullyListener(), vif.errorListener()) {
//			@Override
//			public Map<String, String> getHeaders() throws AuthFailureError {
//				// TODO Auto-generated method stub
//				HashMap<String, String> headers = new HashMap<String, String>();
//				//设置header
//				return headers;
//			}

			@Override
			protected Response<JSONObject> parseNetworkResponse(
					NetworkResponse arg0) {
				// TODO Auto-generated method stub
				// System.out.println("statusCode==>" + arg0.statusCode);
				return super.parseNetworkResponse(arg0);
			}
		};
		jsonRequest.setTag(tag);
		XxCustomApplication.getHttpQueues().add(jsonRequest);
	}
}
