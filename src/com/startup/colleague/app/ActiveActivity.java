package com.startup.colleague.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.startup.colleague.R;
import com.startup.colleague.net.API;
import com.startup.colleague.net.APIURL;
import com.startup.colleague.net.BaseHttp;
import com.startup.colleague.storage.Storage;
import com.startup.colleague.util.ActivityManager;
import com.startup.colleague.util.MyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class ActiveActivity extends Activity {
	private static final String TAG = "ActiveActivity";
	private Storage storage;
	private TextView email;
	private String emailStr = null;
	private API api;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		api = new API(getApplicationContext());
		setContentView(R.layout.account_active);
		email = (TextView) findViewById(R.id.email);
		
		getWindow().setBackgroundDrawable(CSApplication.getInstance().getBackground());
		
		storage = new Storage(getApplicationContext(), "account");
		emailStr = storage.get("email");
		email.setText(emailStr);
		
		findViewById(R.id.active).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (emailStr == null) {
					Toast.makeText(ActiveActivity.this, "沒有找到有效邮件地址！", Toast.LENGTH_SHORT).show();
					return;
				}
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("c", "account");
				params.put("a", "checkActive");
				params.put("email", emailStr);
				api.getJson(ActiveActivity.this, APIURL.NEWROOT, params, new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject res) {
						JSONObject baseResp;
						try {
							baseResp = res.getJSONObject("baseResp");
							if (baseResp.getInt("status") == 0) {
								storage.put("email", emailStr);
								Intent intent = new Intent(ActiveActivity.this, RegisterActivity.class);
								startActivity(intent);
								ActivityManager.getActivityManager().pushActivity2Stack(ActiveActivity.this);
							} else {
								MyToast.show(ActiveActivity.this, baseResp.getString("msg"));
							}
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							MyToast.show(ActiveActivity.this, e.getMessage());
						}
						
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {

					}
				});
				
//				setResult(StartActivity.OPEN_REGISTER);
//				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		// noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.w("URL", requestCode + ":" + resultCode);
		setResult(resultCode);
		finish();
	}

	public void onClickOpenEmail(View view) {
		Intent emailItent = getPackageManager().getLaunchIntentForPackage("com.android.email");
		if (emailItent == null) {
			MyToast.show(this, "没有可用的邮件程序");
			return;
		}
		startActivity(emailItent);
	}
	public void onResendClick(View view) {
		Log.d(TAG, "onResendClick()");
		if (emailStr == null) {
			Toast.makeText(this, "沒有找到有效邮件地址！", Toast.LENGTH_SHORT).show();
			return;
		}
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("c", "account");
		params.put("a", "active");
		params.put("email", emailStr);
		api.getJson(ActiveActivity.this, APIURL.NEWROOT, params, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject res) {
				JSONObject baseResp;
				try {
					baseResp = res.getJSONObject("baseResp");
					if (baseResp.getInt("status") == 0) {
						Toast.makeText(ActiveActivity.this, "新的邮件已发出！", Toast.LENGTH_SHORT).show();
						storage.put("email", emailStr);
					} else {
						MyToast.show(ActiveActivity.this, baseResp.getString("msg"));
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					MyToast.show(ActiveActivity.this, e.getMessage());
				}
				
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		});
		
	}
}
