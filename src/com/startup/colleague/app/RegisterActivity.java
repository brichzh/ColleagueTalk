package com.startup.colleague.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.startup.colleague.R;
import com.startup.colleague.net.API;
import com.startup.colleague.net.APIURL;
import com.startup.colleague.storage.Storage;
import com.startup.colleague.util.ActivityManager;
import com.startup.colleague.util.Md5;
import com.startup.colleague.util.MyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;


public class RegisterActivity extends Activity {
	private API api;
	private TextView username, password, password2;
	private Storage storage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		api = new API(this);
		storage = new Storage(getApplicationContext(), "account");
		setContentView(R.layout.account_register);
		
		getWindow().setBackgroundDrawable(CSApplication.getInstance().getBackground());
		
		username = (TextView) findViewById(R.id.username);
		password = (TextView) findViewById(R.id.password);
		password2 = (TextView) findViewById(R.id.password2);
		
		findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String user = username.getText().toString();
				String pwd = password.getText().toString();
				if (user.length() < 6) {
					// Toast.makeText(getApplicationContext(), "密码太短",
					// Toast.LENGTH_SHORT).show();
					MyToast.show(getBaseContext(), "用户名太短, 至少6位");
					return;
				}
				if (pwd.length() < 6) {
					// Toast.makeText(getApplicationContext(), "密码太短",
					// Toast.LENGTH_SHORT).show();
					MyToast.show(getBaseContext(), "密码太短, 至少6位");
					return;
				}
				if (pwd.equals(password2.getText().toString())) {
					HashMap<String, String> params = new HashMap<String, String>();
					
					params.put("username", user);
					params.put("password", Md5.getMD5(pwd));
					params.put("email", storage.get("email"));
					api.post(APIURL.POST_REGISTER, params, new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							// MyToast.show(getApplicationContext(), response);
							
							try {
								JSONObject res = new JSONObject(response);
								JSONObject resp = res.getJSONObject("baseResp");
								
								if (resp.getInt("status") != 0){
									MyToast.show(getApplicationContext(), resp.getString("msg"));
									return;
								} 
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							if (api.getBaseHttp().getCookie("auth") == null) {
								MyToast.show(getApplicationContext(), "注册失败");
								return;
							}
							Log.w("URL-auth", api.getBaseHttp().getCookie("auth").getValue());
							storage.put("auth", api.getBaseHttp().getCookie("auth").getValue());
							storage.put("domain", api.getBaseHttp().getCookie("auth").getDomain());
							try {
								String company = URLDecoder.decode(api.getBaseHttp().getCookie("companyName").getValue(), "utf-8");
								storage.put("company", company);
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							storage.put("user", response);
							MyToast.show(getApplicationContext(), "注册成功");
							Intent intent = new Intent(RegisterActivity.this, NoteMain.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							ActivityManager.getActivityManager().popAllActivityFromStack();
							finish();
//							RegisterActivity.this.setResult(StartActivity.OPEN_NOTE_MAIN);
//							 
//							RegisterActivity.this.finish();
						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							MyToast.show(RegisterActivity.this, error.getMessage());
						}
					});
				} else {
					MyToast.show(getBaseContext(), "密码不一致");
				}
			}
		});

	} 
}
