package com.startup.colleague.app;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.WindowCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.startup.colleague.R;
import com.startup.colleague.net.API;

import java.util.List;
import java.util.Map;

public class NotifyActivity extends Activity {
	private static final String TAG = "NotifyActivity";
	ListView listView;
	List<Map<String, String>> notifyList = null;
	SimpleAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notify);
		
		listView = (ListView) findViewById(R.id.listview);
		
		Intent it = getIntent();
		if (it != null) {
			Log.d(TAG, "intent not null");
			notifyList =  (List<Map<String, String>>) it.getSerializableExtra("notifyList");
			if (notifyList == null || notifyList.size() == 0) {
				//notifyList = new ArrayList<Map<String,String>>();
				Log.d(TAG, "no notification");
				listView.setVisibility(View.INVISIBLE);
			} else {
				adapter = new SimpleAdapter(
						getBaseContext(), notifyList,
						R.layout.notify_list, new String[] {
								"title", "commentContent", "topicContent" }, new int[] {
							R.id.row, R.id.content, R.id.quote });
				listView.setAdapter(adapter);
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent,
							View view, int position, long id) {
						if (notifyList != null && notifyList.size() > position) {
							Map<String, String> map = notifyList.get(position);
							Intent it = new Intent(NotifyActivity.this, NoteActivity.class);
							it.putExtra("fromNotify", "yes");
							it.putExtra("topicId", map.get("topicId").toString());
							// it.putExtra("row", map.get("row"));
							if (map.get("rowId") != null) {
								it.putExtra("rowId", map.get("rowId"));
							}
							it.putExtra("topicContent", map.get("topicContent"));
							notifyList.remove(position);
							
							startActivity(it);
						}
					}
				});
			}
		}

		findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "Notemain onresume");
		adapter.notifyDataSetChanged();
		if (notifyList.size() == 0) {
			finish();
		}
	}

}
