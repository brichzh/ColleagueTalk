package com.startup.colleague.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
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
import com.startup.colleague.util.MyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class NewNoteActivity extends Activity {

	private API api;
	private Context context;
	private TextView content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_note);
		api = new API(getApplicationContext());
		content = (TextView) findViewById(R.id.content);
		context = this;
		findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = content.getText().toString();
				if (text.length() < 10) {
					MyToast.show(getBaseContext(), "内容太短");
					return;
				}
				if (text.length() > 1000) {
					MyToast.show(getBaseContext(), "内容太长，不能超过1000字");
					return;
				}

				HashMap<String, String> params = new HashMap<String, String>();
				params.put("content", text);
				
				final ProgressDialog dialog = new ProgressDialog(NewNoteActivity.this);
				dialog.setMessage("正在加载");
				dialog.setIndeterminate(true);
				dialog.setCancelable(true);
				dialog.show();
				api.post(APIURL.POST_TOPIC, params, new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						dialog.dismiss();
						JSONObject resp;
						try {
							resp = new JSONObject(response);
							JSONObject baseResp = resp.getJSONObject("baseResp");
							int ret = baseResp.getInt("status");
							if (ret == 0) {
								MyToast.show(NewNoteActivity.this, baseResp.getString("msg"));
								finish();
							} else if (ret == 100001) {//重新登陆
								Intent intent = new Intent(NewNoteActivity.this, LoginActivity.class);
								startActivity(intent);
								finish();
							} else {
								MyToast.show(NewNoteActivity.this,
										baseResp.getString("msg"));
								return;
							}
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}, null);
			}
		});

		findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (content.getText().length() == 0) {
					finish();
					return;
				}
				new AlertDialog.Builder(context)
					.setTitle("提醒")
					.setMessage("放弃本次发表内容？")
					.setPositiveButton("放弃", new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							finish();
						}
					})
					.setNegativeButton("取消", new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							
						}
					})
					.create()
					.show();
			}
		});
	}
	
	@Override
    public void onBackPressed() {
		if (content.getText().length() == 0) {
			super.onBackPressed();
			return;
		}
		new AlertDialog.Builder(context)
		.setTitle("提醒")
		.setMessage("放弃本次发表内容？")
		.setPositiveButton("放弃", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				finish();
			}
		})
		.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				return;
			}
		})
		.create()
		.show();
    }
}
