package com.startup.colleague.app;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.startup.colleague.net.API;
import com.startup.colleague.net.APIURL;
import com.startup.colleague.storage.Storage;
import com.startup.colleague.util.MyToast;

public class SaveTokenWorker {
	private static final String TAG = "SaveTokenWorker";
	private API api;
	private Storage mStorage;
	private Context mContext;
	private String mToken = null;
	
	public SaveTokenWorker(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		api = new API(context);
		mStorage = new Storage(context, "account");
	}
	
	public void doSaveToken() {
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("c", "online");
		params.put("a", "saveToken");
		if (mStorage.get("regid") == "") {
			mToken = JPushInterface.getRegistrationID(mContext);
			params.put("token", mToken);
		} else {
			params.put("token", mStorage.get("regid"));
		}
		api.get(APIURL.NEWROOT, params, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					Log.d(TAG, "response="+response);
					JSONObject res = new JSONObject(response);
					JSONObject resp = res.getJSONObject("baseResp");
					int ret = resp.getInt("status");
					if ( ret != 0){
						//MyToast.show(mContext, resp.getString("msg"));
						return;
					}
					if (mToken != null) {
						mStorage.put("regid", mToken);
					}
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				
			}
		});
	}
}
