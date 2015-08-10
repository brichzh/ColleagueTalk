package com.startup.colleague.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.enrique.stackblur.StackBlurManager;
import com.startup.colleague.R;
import com.startup.colleague.net.API;
import com.startup.colleague.net.APIURL;
import com.startup.colleague.storage.Storage;
import com.startup.colleague.util.ActivityManager;
import com.startup.colleague.util.Md5;
import com.startup.colleague.util.MyToast;

import org.apache.http.cookie.Cookie;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;


public class LoginActivity extends Activity {
	private static final String TAG = "LoginActivity";
	private API api;
	private Storage storage;
	private TextView username, password;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_login);
		
		getWindow().setBackgroundDrawable(CSApplication.getInstance().getBackground());
		username = (TextView) findViewById(R.id.username);
		password = (TextView) findViewById(R.id.password);
		
		username.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);
		api = new API(getApplicationContext());
		storage = new Storage(getApplicationContext(), "account");
		findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String u = username.getText().toString();
				String p = password.getText().toString();
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("username", u);
				params.put("password", Md5.getMD5(p));
				api.post(APIURL.POST_LOGIN, params, new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						try {
							JSONObject res = new JSONObject(response);
							JSONObject resp = res.getJSONObject("baseResp");
							int ret = resp.getInt("status");
							if ( ret != 0){
								MyToast.show(LoginActivity.this, resp.getString("msg"));
								return;
							}
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	
						Cookie cookie = api.getBaseHttp().getCookie("auth");
						if (cookie != null) {
							Log.w("TAG", api.getBaseHttp().getCookie("auth").getValue());
							storage.put("auth", api.getBaseHttp().getCookie("auth").getValue());
							storage.put("domain", api.getBaseHttp().getCookie("auth").getDomain());
							try {
								String company = URLDecoder.decode(api.getBaseHttp().getCookie("companyName").getValue(), "utf-8");
								storage.put("company", company);
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							SaveTokenWorker worker = new SaveTokenWorker(LoginActivity.this);
			            	worker.doSaveToken();
			            	
							storage.put("user", response);
							MyToast.show(getApplicationContext(), "登录成功");
							Intent it = new Intent(LoginActivity.this, NoteMain.class);
							startActivity(it);
							overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
							ActivityManager.getActivityManager().popAllActivityFromStack();
							finish();
						} else {
							MyToast.show(LoginActivity.this, "登陆失败");
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						MyToast.show(LoginActivity.this, error.getMessage());
					}
				});
			}
		});
	}
}
