package com.startup.colleague.app;

import com.startup.colleague.R;
import com.startup.colleague.net.API;
import com.startup.colleague.net.BaseHttp;
import com.startup.colleague.storage.Storage;
import com.startup.colleague.util.MyToast;
import com.startup.colleague.view.XTextView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class CompanyNotOpenActivity extends Activity{
	private static final String TAG = "CompanyNotOpenActivity";
	private BaseHttp baseHttp;
	private Storage storage;
	private String emailStr = null;
	private API api;
	private TextView email;
	private TextView tips;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		api = new API(getApplicationContext());
		setContentView(R.layout.company_unopen);
		
		getWindow().setBackgroundDrawable(CSApplication.getInstance().getBackground());
		
		baseHttp = new BaseHttp(getApplicationContext());
		storage = new Storage(getApplicationContext(), "account");
		email = (TextView) findViewById(R.id.email);
		tips = (TextView) findViewById(R.id.xtip);
		
		emailStr = storage.get("email");
		email.setText(emailStr);
		
		tips.setText(getResources().getString(R.string.company_unopen_tip));
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

	public void onClickOpenEmail(View view) {
		Intent emailItent = getPackageManager().getLaunchIntentForPackage("com.android.email");
		if (emailItent == null) {
			MyToast.show(this, "没有可用的邮件程序");
			return;
		}
		startActivity(emailItent);
	}
}
