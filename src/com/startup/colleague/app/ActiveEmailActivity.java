package com.startup.colleague.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.startup.colleague.R;
import com.startup.colleague.net.API;
import com.startup.colleague.net.APIURL;
import com.startup.colleague.storage.Storage;
import com.startup.colleague.util.ActivityManager;
import com.startup.colleague.util.MyToast;

import org.apache.http.cookie.Cookie;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class ActiveEmailActivity extends Activity {
	private static final String TAG = "ActiveEmailActivity";
	private API api;
	private TextView email;
	private Storage storage;
	private String emailText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		storage = new Storage(getApplicationContext(), "account");
		api = new API(getApplicationContext());
		setContentView(R.layout.account_start);
		email = (TextView) findViewById(R.id.email);
		getWindow().setBackgroundDrawable(CSApplication.getInstance().getBackground());
		
		findViewById(R.id.next_step).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (!Is_Valid_Email(email)) {
					MyToast.show(ActiveEmailActivity.this, "请输入正确的公司邮箱地址");
					return;
				}
				storage.put("email", emailText);
				//setResult(StartActivity.OPEN_ACTIVE);
				sendVerifyEmail();
				//finish();
//				sendVerifyEmail();
			}
		});
	}

	public void sendVerifyEmail() {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("c", "account");
		params.put("a", "active");
		params.put("email", emailText);
		
		api.getJson(ActiveEmailActivity.this, APIURL.NEWROOT, params, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject res) {
				if (res == null) {
					return;
				}
				try {
					Log.d(TAG, "response"+res.toString());
					JSONObject resp = res.getJSONObject("baseResp");
					int status = resp.getInt("status");
					if (status != 0 ){
						MyToast.show(ActiveEmailActivity.this, resp.getString("msg"));
						if (status == 1) {//咱不支持的公司邮箱
							Intent intent = new Intent(ActiveEmailActivity.this, CompanyNotOpenActivity.class);
							startActivity(intent);
							//finish();
						}
					} else {
						Intent intent = new Intent(ActiveEmailActivity.this, ActiveActivity.class);
						startActivity(intent);
						ActivityManager.getActivityManager().pushActivity2Stack(ActiveEmailActivity.this);
						//setResult(StartActivity.OPEN_ACTIVE);
						//finish();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				MyToast.show(ActiveEmailActivity.this, error.getMessage());
			}
		});
	}

	public boolean Is_Valid_Email(TextView edt) {
		if (edt.getText().toString() == null) {
			edt.setError("请输入邮箱地址！");
			emailText = null;
			return false;
		} else if (isEmailValid(edt.getText().toString()) == false) {
			edt.setError("无效的邮箱地址！");
			emailText = null;
			return false;
		} else {
			emailText = edt.getText().toString();
			return true;
		}
	}

	boolean isEmailValid(CharSequence email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}
	
	public void onNoEmailClicked(View view) {
		Intent intent = new Intent(ActiveEmailActivity.this, CompanyNotOpenActivity.class);
		startActivity(intent);
	}
}
