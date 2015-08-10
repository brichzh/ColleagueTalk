package com.startup.colleague.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.data.JPushLocalNotification;

import com.startup.colleague.R;
import com.startup.colleague.net.API;
import com.startup.colleague.util.ActivityManager;


public class StartActivity extends Activity {
	private static final String TAG = "StartActivity";
	public final static int OPEN_NOTE_MAIN = 101;
	public final static int OPEN_REGISTER = 102;
	public final static int OPEN_LOGIN = 103;
	public final static int OPEN_ACTIVE = 104;

	private API api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		api = new API(getBaseContext());
		
		JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);     		// 初始化 JPush
        
		findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(StartActivity.this, ActiveEmailActivity.class);
				startActivity(it);
				ActivityManager.getActivityManager().pushActivity2Stack(StartActivity.this);
			}
		});
		findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(StartActivity.this, LoginActivity.class);
				startActivity(it);
				ActivityManager.getActivityManager().pushActivity2Stack(StartActivity.this);
			}
		});
		

		if (api.getStorage().get("auth").equals("invalid")) {
			Intent it = new Intent(StartActivity.this, LoginActivity.class);
			startActivity(it);
			ActivityManager.getActivityManager().pushActivity2Stack(StartActivity.this);
		} else if ( !TextUtils.isEmpty(api.getStorage().get("auth")) ){
			Intent it = new Intent(StartActivity.this, NoteMain.class);
			startActivity(it);
			finish();
		} 
//			api.getJson(StartActivity.this, APIURL.ACCOUNT, params, new Response.Listener<JSONObject>() {
//				@Override
//				public void onResponse(JSONObject response) {
//					try {
//						if (response.getInt("status") == 0) {
//							Intent it = new Intent(StartActivity.this, NoteMain.class);
//							startActivity(it);
//							finish();
//						}
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//				}
//			}, null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.w("URL", requestCode + ":" + resultCode);

		if (resultCode == OPEN_NOTE_MAIN) {
			Intent it = new Intent(StartActivity.this, NoteMain.class);
			startActivity(it);
			finish();
		}

		if (resultCode == OPEN_REGISTER) {
			Intent it = new Intent(StartActivity.this, RegisterActivity.class);
			startActivityForResult(it, OPEN_REGISTER);
		}

		if (resultCode == OPEN_LOGIN) {
			Intent it = new Intent(StartActivity.this, LoginActivity.class);
			startActivityForResult(it, OPEN_LOGIN);
		}

		if (resultCode == OPEN_ACTIVE) {
			Intent it = new Intent(StartActivity.this, ActiveActivity.class);
			startActivityForResult(it, OPEN_ACTIVE);
		}
	}
}
