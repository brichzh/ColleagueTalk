package com.startup.colleague.app;

import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.startup.colleague.R;
import com.startup.colleague.dao.NoteDao;
import com.startup.colleague.model.NoteModel;
import com.startup.colleague.net.API;
import com.startup.colleague.net.APIURL;
import com.startup.colleague.storage.Storage;
import com.startup.colleague.util.MyToast;
import com.startup.colleague.view.XListView;
import com.startup.colleague.view.XListView.IXListViewListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteMain extends Activity implements IXListViewListener {
	private static final String TAG = "NoteMain";
	private Context context;
	private API api;
	private XListView listView;
	private TextView notifyCnt;
	private TextView companyName;
	private TextView loadFailed;
	private Storage storage;
	private Storage accountStorage;
	private ProgressDialog progressDialog;
	private List<Map<String, Object>> noteList = new ArrayList<Map<String, Object>>();
	private List<Map<String, String>> notifyList = new ArrayList<Map<String, String>>();
	private SimpleAdapter adapter;
	private boolean requestOn = false;
	private String pageIndex = "0";
	private boolean online = false;
	
	@SuppressWarnings("unchecked")
	public <T> T $(int resid) {
		return (T) findViewById(resid);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		api = new API(getApplicationContext());
		setContentView(R.layout.note_main);

		listView = $(R.id.listview);
		notifyCnt = listView.getNotifyHeader();
		companyName = $(R.id.company);
		notifyCnt.setVisibility(View.GONE);
		loadFailed = $(R.id.loadfailed);
		loadFailed.setVisibility(View.GONE);
		
		listView.getHeadView().setVisiableHeight(120);
//		notifyCnt.setVisibility(View.VISIBLE);
		
		storage = new Storage(getApplicationContext(), "noteList");
		accountStorage = new Storage(getApplicationContext(), "account");

		context = this;
		listView.setXListViewListener(this);
		listView.setVerticalScrollBarEnabled(false);
		// SimpleAdapter adapter = new SimpleAdapter(this, getData(),
		// R.layout.note_list,
		// new String[]{"content", "bottom"},
		// new int[]{R.id.content, R.id.bottom});
		// listView.setAdapter(adapter);

		findViewById(R.id.post).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// MyToast.show(getBaseContext(), "POST");
				Intent it = new Intent(NoteMain.this, NewNoteActivity.class);
				startActivity(it);
			}
		});
		
		notifyCnt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				notifyCnt.setVisibility(View.GONE);
				listView.resetHeader();
				Intent it = new Intent(NoteMain.this, NotifyActivity.class);
				it.putExtra("notifyList", (Serializable)notifyList);
				startActivity(it);
			}
		});

		adapter = new SimpleAdapter(getBaseContext(), noteList,
				R.layout.note_list,
				new String[] { "content", "viewCnt", "commentCnt" }, new int[] {
						R.id.content, R.id.view, R.id.comment });
		listView.setAdapter(adapter);
		listView.setPullLoadEnable(true);
		listView.setPullRefreshEnable(true); 
		adapter.notifyDataSetChanged();
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				HashMap<String, Object> note = (HashMap<String, Object>) listView
						.getItemAtPosition(position);
				Intent it = new Intent(NoteMain.this, NoteActivity.class);
				it.putExtra("uid", note.get("uid").toString());
				it.putExtra("topicId", note.get("topicId").toString());
				it.putExtra("viewCnt", note.get("viewCnt").toString());
				it.putExtra("content", note.get("content").toString());
				it.putExtra("commentCnt", note.get("commentCnt").toString());
				it.putExtra("myId", note.get("myId").toString());
				startActivity(it);
			}
		});
		getOnline();
		load();
	}
	
	public void getOnline() {
		if (requestOn) {
			return;
		}
		requestOn = true;
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("c", "online");
		params.put("a", "check");
		api.get(APIURL.NEWROOT, params, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				requestOn = false;
				JSONObject json;
				try {
					json = new JSONObject(response);
					JSONObject baseResp = json.getJSONObject("baseResp");
					if (baseResp.getInt("status") != 0) {
						Log.w(TAG, "Http request failed, msg = "+baseResp.getString("msg"));
						if (baseResp.getInt("status") == 100001) {
							MyToast.show(getBaseContext(), baseResp.getString("msg"));
							api.getStorage().put("auth", "invalid");
							Intent it = new Intent(NoteMain.this, LoginActivity.class);
							it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
							startActivity(it);
							finish();
						} else {
							MyToast.show(getBaseContext(), baseResp.getString("msg"));
						}
						return;
					} else {
						online = true;
						reLoad(true);
//						MyToast.show(getBaseContext(), "登录成功！");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					requestOn = false;
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				requestOn = false;
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_note_main, menu);
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
		} else if (id == R.id.login_out) {
			api.getStorage().put("auth", "");
			finish();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "Notemain onresume");
		if (notifyCnt.getVisibility() == View.GONE) {
			Log.d(TAG, "reset listview header");
			listView.resetHeader();
		}
		reLoad(true);
	}

	private void loadNewNoteList(String response, boolean fromSvr) {
		try {
			if (response == null || response == "") {
				return;
			}
			Log.d(TAG, "fromsvr " + fromSvr + " response=" + response);
			JSONObject oldObj = null;
			JSONArray newNoteList = null;
			
			JSONObject resp = new JSONObject(response);
			JSONObject baseResp = resp.getJSONObject("baseResp");
			int status = baseResp.getInt("status");
			if (fromSvr && status != 0) {
				if (storage.get("noteList") == "") {
					loadFailed.setVisibility(View.VISIBLE);
				}				

				if (status == 100001) {//重新登陆
					MyToast.show(this, baseResp.getString("msg"));
					Intent intent = new Intent(NoteMain.this, LoginActivity.class);
					startActivity(intent);
					finish();
				} else if (status != 0) {
					MyToast.show(this, baseResp.getString("msg"));
				}
				return;
			} else if (fromSvr) {//从服务器获得新数据
				newNoteList = new JSONArray();
				if (storage.get("noteList") != "") {
					Log.d(TAG, "has cache");
					oldObj = new JSONObject(storage.get("noteList"));
					//oldNoteList = oldObj.getJSONArray("noteList");
				} else {
					Log.d(TAG, "has no cache");
					storage.put("noteList", response);
				}
				MyToast.show(this, baseResp.getString("msg"));
			}
			JSONObject res = new JSONObject(response);
			
			loadFailed.setVisibility(View.GONE);

			companyName.setText(accountStorage.get("company"));
			if (!res.getString("pageIndex").equals("99999999")) {
				pageIndex = res.getString("pageIndex");
			}
			
			if (fromSvr && res.getInt("endFlag") == 1) {
				MyToast.show(this, "已经是最新数据了");
			}
			JSONArray list = res.getJSONArray("topicList");
			Log.d(TAG, "list length = " + list.length());
			noteList.clear();
			for (int i = 0; i < list.length(); i++) {
				JSONObject note = list.getJSONObject(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("uid", note.getString("uid"));
				map.put("topicId", note.getString("topicId"));
				map.put("content", note.getString("content"));
				map.put("pageIndex", storage.get("pageIndex"));
				// map.put("bottom", "阅读 " + note.getInt("viewCnt")
				// + " 评论 " + note.getInt("commentCnt"));
				map.put("viewCnt", note.getString("viewCnt"));
				map.put("commentCnt", note.getString("commentCnt"));
				map.put("myId", note.getString("myId"));
				
				if (newNoteList != null) {
					newNoteList.put(note);
				}

				noteList.add(i, map);
			} 
			Log.d(TAG, "list length = 1" );
			adapter.notifyDataSetChanged();
			if (oldObj != null) {// 更新存储
				oldObj.remove("topicList");
				oldObj.put("topicList", newNoteList);
				storage.put("noteList", oldObj.toString());
			}
			Log.d(TAG, "list length = 2" );
			listView.stopRefresh();
			listView.stopLoadMore();
			listView.setRefreshTime("刚刚");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private void loadMoreNoteList(String response, boolean fromSvr) {
		try {
			if (response == null || response == "") {
				return;
			}
			Log.d(TAG, "fromsvr " + fromSvr + " response=" + response);
			JSONObject oldObj = null;
			JSONArray oldNoteList = null;
			
			JSONObject resp = new JSONObject(response);
			JSONObject baseResp = resp.getJSONObject("baseResp");
			int status = baseResp.getInt("status");
			if (fromSvr && status != 0) {
				if (storage.get("noteList") == "") {
					loadFailed.setVisibility(View.VISIBLE);
				}
				if (status == 100001) {//重新登陆
					MyToast.show(this, baseResp.getString("msg"));
					Intent intent = new Intent(NoteMain.this, LoginActivity.class);
					startActivity(intent);
					finish();
				} else if (status != 0) {
					MyToast.show(this, baseResp.getString("msg"));
				}
				return;
			} else if (fromSvr) {//从服务器获得新数据
				if (storage.get("noteList") != "") {
					Log.d(TAG, "has cache");
					oldObj = new JSONObject(storage.get("noteList"));
					oldNoteList = oldObj.getJSONArray("topicList");
				} else {
					Log.d(TAG, "has no cache");
					storage.put("noteList", response);
				}
			}
			JSONObject res = new JSONObject(response);
			
			loadFailed.setVisibility(View.GONE);
			companyName.setText(accountStorage.get("company"));
			
			if (!res.getString("pageIndex").equals("99999999")) {
				pageIndex = res.getString("pageIndex");
			}
			if (res.getInt("endFlag") == 1) {
				MyToast.show(this, "没有更多数据了");
			}
			
			JSONArray list = res.getJSONArray("topicList");
			Log.d(TAG, "list length" + list.length());
			if (list.length() == 0) {
				
			}
			//noteList.clear();
			for (int i = 0; i < list.length(); i++) {
				JSONObject note = list.getJSONObject(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("uid", note.getString("uid"));
				map.put("topicId", note.getString("topicId"));
				map.put("content", note.getString("content"));
				map.put("pageIndex", storage.get("pageIndex"));
				// map.put("bottom", "阅读 " + note.getInt("viewCnt")
				// + " 评论 " + note.getInt("commentCnt"));
				map.put("viewCnt", note.getString("viewCnt"));
				map.put("commentCnt", note.getString("commentCnt"));
				map.put("myId", note.getString("myId"));
				
				oldNoteList.put(note);

				noteList.add(map);
			}
			adapter.notifyDataSetChanged();
			if (oldObj != null) {// 更新存储
				oldObj.remove("topicList");
				oldObj.put("topicList", oldNoteList);
				storage.put("noteList", oldObj.toString());
			}
			listView.stopRefresh();
			listView.stopLoadMore();
			listView.setRefreshTime("刚刚");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private void loadNoteListFromSvr(String response) {
		try {
			Log.d(TAG, "Load from server!");
			
			if (response == null) {
				if (storage.get("maxNoteId") == "") {
					loadFailed.setVisibility(View.VISIBLE);
				}
				return;
			}
			JSONObject res = new JSONObject(response);
			NoteDao noteDao = new NoteDao(context);
			noteDao.open();

			loadFailed.setVisibility(View.GONE);
			if (res.has("companyName") ){
				companyName.setText(res.getString("companyName"));
				storage.put("companyName", res.getString("companyName"));
			}
			JSONArray list = res.getJSONArray("noteList");
			Log.d(TAG, "list length" + list.length());
			for (int i = 0; i < list.length(); i++) {
				JSONObject note = list.getJSONObject(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("noteId", note.getString("id"));
				map.put("content", note.getString("content"));
				// map.put("bottom", "阅读 " + note.getInt("viewCnt")
				// + " 评论 " + note.getInt("commentCnt"));
				map.put("view", note.getString("viewCnt"));
				map.put("comment", note.getString("commentCnt"));

				NoteModel noteModel = new NoteModel();
				noteModel.setId(Integer.parseInt(note.getString("id")));
				noteModel.setUserId(Integer.parseInt(note.getString("uid")));
				noteModel.setContent(note.getString("content"));
				noteModel.setCommentCnt(Integer.parseInt(note
						.getString("commentCnt")));
				noteModel.setTimeStm(note.getString("createTime"));
				noteModel.setReportCnt(Integer.parseInt(note
						.getString("reportCnt")));
				noteModel.setCompanyId(Integer.parseInt(note
						.getString("companyId")));
				noteModel
						.setViewCnt(Integer.parseInt(note.getString("viewCnt")));

				noteDao.insert(noteModel);

				noteList.add(0, map);
			}

			noteDao.close();

			Log.d(TAG, "3");
			listView.stopRefresh();
			listView.stopLoadMore();
			listView.setRefreshTime("刚刚");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void loadNoteListFromDB(List<NoteModel> notes) {
		Log.d(TAG, "Load data from db!");

		companyName.setText(storage.get("companyName"));
		Log.d(TAG, "list length" + notes.size());
		for (NoteModel noteModel : notes) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("noteId", noteModel.getId());
			map.put("content", noteModel.getContent());
			map.put("view", noteModel.getViewCnt());
			map.put("comment", noteModel.getCommentCnt());

			noteList.add(map);
		}

	}

	void load() {
		Log.d(TAG, "load topics!");
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("c", "topic");
		params.put("a", "getTopicList");
		
		try {
			Log.d(TAG, "Current pageIndex = " + pageIndex);
			int index = Integer.parseInt(pageIndex);
			if (index != 0) {
				params.put("pageIndex", pageIndex);
			}
			if (storage.get("noteList") != "") {
				loadNewNoteList(storage.get("noteList"), false);
			} else {
				loadFailed.setVisibility(View.VISIBLE);
			}
		} catch (NumberFormatException e) {
			// TODO: handle exception
			Log.d(TAG, "NOT valid number!");
		}
//		if (storage.get("endFlag") == "1") {
//			MyToast.show(this, "已经是最新的数据了");
//			return;
//		}

		if ( requestOn || !online) {
			return;
		}
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(NoteMain.this, "提示", "正在获取数据");
			progressDialog.setCancelable(true);
			progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					progressDialog.cancel();
				}
			});
		}
		requestOn = true;
		api.get(APIURL.NEWROOT, params, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				// MyToast.show(getApplicationContext(),response);
				requestOn = false;
				progressDialog.cancel();
				loadNewNoteList(response, true);
				
				getNotifyCnt();
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				MyToast.show(context, error.getMessage());
				if (progressDialog != null) {
					progressDialog.cancel();
				}
				//loadNoteList(null, true);
				requestOn = false;
			}
		});
	}

	void reLoad(final boolean isRefresh) {
		Log.d(TAG, "reLoad topics!");
		if (requestOn) {
			listView.stopLoadMore();
			listView.stopRefresh();
			return;
		}
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("c", "topic");
		params.put("a", "getTopicList");
		if (!isRefresh) {
			try {
				Log.d(TAG, "Current pageIndex = " + pageIndex);
				int index = Integer.parseInt(pageIndex);
				if (index != 0) {
					params.put("pageIndex", pageIndex);
				}
			} catch (NumberFormatException e) {
				// TODO: handle exception
				Log.d(TAG, "NOT valid number!");
			}
		}
		
//		if (storage.get("endFlag") == "1") {
//			MyToast.show(this, "已经是最新的数据了");
//			return;
//		}
		if (!online) {
			MyToast.show(this, "请检查网络后，重新刷新");
			getOnline();
			listView.stopLoadMore();
			listView.stopRefresh();
			return;
		}
		requestOn = true;
		api.get(APIURL.NEWROOT, params, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				// MyToast.show(getApplicationContext(),response);
				if (isRefresh) {
					loadNewNoteList(response, true);
				}else {
					loadMoreNoteList(response, true);
				}
				requestOn = false;
				getNotifyCnt();
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				MyToast.show(context, error.getMessage());
				requestOn = false;
				//loadNoteList(null, true);
				listView.stopLoadMore();
				listView.stopRefresh();
			}
		});
	}

	// 获取新消息数量
	public void getNotifyCnt() {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("c", "notify");
		params.put("a", "getNotify");
		api.get(APIURL.NEWROOT, params, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					Log.d(TAG, "resp json = "+response);
					JSONObject res = new JSONObject(response);
					JSONObject baseResp  = res.getJSONObject("baseResp");
					if (baseResp.getInt("status") == 100001) {//重新登陆
						MyToast.show(NoteMain.this, baseResp.getString("msg"));
						Intent intent = new Intent(NoteMain.this, LoginActivity.class);
						startActivity(intent);
						finish();
						return;
					} else if (baseResp.getInt("status") != 0){
						MyToast.show(NoteMain.this, baseResp.getString("msg"));
						return;
					}
					
					if (notifyCnt.getVisibility() == View.GONE) {
						notifyList.clear();
					}
					JSONArray list = res.getJSONArray("notifyList");
					for (int i = 0; i < list.length(); i++) {
						JSONObject notify = list.getJSONObject(i);
						Map<String, String> map = new HashMap<String, String>();
						map.put("commentContent", notify.getString("commentContent"));
						map.put("topicContent", notify.getString("topicContent"));
						map.put("topicId", notify.getString("topicId"));
						if(notify.getInt("replyRowId") != 0){
							map.put("title", notify.getString("rowId")+"F 回复 "+ notify.getString("replyRowId")+"F");
							map.put("rowId", notify.getString("rowId"));
							map.put("replyRowId", notify.getString("replyRowId"));
						} else {
							map.put("title", notify.getString("rowId")+"F");
							map.put("rowId", notify.getString("rowId"));
							map.put("replyRowId", notify.getString("replyRowId"));
						}
						
						notifyList.add(map);
					}
					
					if (notifyList.size() > 0) {
						notifyCnt.setVisibility(View.VISIBLE);
						notifyCnt.setText("有" + notifyList.size() + "条新通知");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				MyToast.show(context, error.getMessage());
			}
		});
	}

	public void onLoadFailed(View view) {
		load();
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		reLoad(true);
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		reLoad(false);
	}

	private int mBackKeyPressedTimes = 0;

	@Override
    public void onBackPressed() {
        if (mBackKeyPressedTimes == 0) {
            Toast.makeText(this, "再按一次退出程序 ", Toast.LENGTH_SHORT).show();
            mBackKeyPressedTimes = 1;
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                    	mBackKeyPressedTimes = 0;
                    }
                }
            }.start();
            return;
        }else{
            this.finish();
        }
        super.onBackPressed();
    }
}
