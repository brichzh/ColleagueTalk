package com.startup.colleague.net;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.startup.colleague.storage.Storage;
import com.startup.colleague.util.MyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by aidi on 15-06-20.
 */
public class API  {
	private static final String TAG = "API";
	private BaseHttp baseHttp;
	private Storage storage = null;

	//可以使用单例
	public API(Context context) {
		baseHttp = new BaseHttp(context);
		storage = new Storage(context, "account");

	}
	
	public Storage getStorage() {
		return storage;
	}
	
	public BaseHttp getBaseHttp() {
		return baseHttp;
	}
	public void get(final String url, HashMap<String, String> params,
			final Response.Listener<String> listener,
			final Response.ErrorListener errorListener) {
		Log.d(TAG, "Get url="+url);
		if (!TextUtils.isEmpty(storage.get("auth"))) {
			baseHttp.addCookie("auth", storage.get("auth"),
					storage.get("domain"));
		}
		baseHttp.get(url, params, listener, errorListener);
	}
	
	public void postJson(final String url, HashMap<String, String> params,
			final Response.Listener<JSONObject> listener,
			final Response.ErrorListener errorListener) {
		if (!TextUtils.isEmpty(storage.get("auth"))) {
			baseHttp.addCookie("auth", storage.get("auth"),
					storage.get("domain"));
		}
		
		baseHttp.postJson(url, params, listener, errorListener);
	}
	public void post(final String url, HashMap<String, String> params,
			final Response.Listener<String> listener,
			final Response.ErrorListener errorListener) {
		if (!TextUtils.isEmpty(storage.get("auth"))) {
			baseHttp.addCookie("auth", storage.get("auth"),
					storage.get("domain"));
		}
		
		baseHttp.post(url, params, listener, errorListener);
	}
	public void getJson(final Activity activity, final String url,
			HashMap<String, String> params,
			final Response.Listener<JSONObject> listener,
			final Response.ErrorListener errorListener) {
		// final ProgressDialog dialog = ProgressDialog.show(activity, "提示",
		// "正在提交服务器");
		if (!TextUtils.isEmpty(storage.get("auth"))) {
			baseHttp.addCookie("auth", storage.get("auth"), storage.get("domain"));
		}
		final ProgressDialog dialog = new ProgressDialog(activity);
		dialog.setMessage("正在加载");
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		dialog.show();
		baseHttp.get(url, params, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				dialog.dismiss();
				try {
					JSONObject json = new JSONObject(response);
					JSONObject baseResp = json.getJSONObject("baseResp");
					if (baseResp.getInt("status") != 0) {
						Log.w(TAG, "Http request failed, msg = "+baseResp.getString("msg"));
						MyToast.show(activity, baseResp.getString("msg"));
						listener.onResponse(json);
						return;
					} else {
						listener.onResponse(json);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					MyToast.show(activity, "出现错误:" + response);
				}
				
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				dialog.dismiss();
				MyToast.show(activity, "请求失败:" + error.getMessage());
				if (errorListener != null)
					errorListener.onErrorResponse(error);
			}
		});
	}

}
