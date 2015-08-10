package com.startup.colleague.net;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aidi
 *
 */
public class BaseHttp {
	private static final String TAG = "BaseHttp";
	public final static String PARAM_ERROR = "ParamError";
	public final static String PARAM_ERROR_MESSAGE = "参数错误";
	public final static String NETWORK_ERROR = "NetworkError";
	public final static String NETWORK_ERROR_MESSAGE = "请求网络失败";
	public final static String SERVER_ERROR = "ServerError";
	public final static String SERVER_ERROR_MESSAGE = "服务器异常";
	public final static String SIGN_ERROR = "SignError";
	public final static String SIGN_ERROR_MESSAGE = "签名错误";

	public final static String utf8 = "UTF-8";

	protected RequestQueue queue = null;

	private final static int SOCKET_TIMEOUT = 30000;

	private DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient();

	public BaseHttp(Context context) {
		//HttpStack httpStack = new HttpClientStack(httpClient);
		//queue = Volley.newRequestQueue(context, httpStack);
		if ( queue == null ) {
	        final ClientConnectionManager mClientConnectionManager = mDefaultHttpClient.getConnectionManager();
	        final HttpParams mHttpParams = mDefaultHttpClient.getParams();
	        final ThreadSafeClientConnManager mThreadSafeClientConnManager = new ThreadSafeClientConnManager( mHttpParams, mClientConnectionManager.getSchemeRegistry() );

	        mDefaultHttpClient = new DefaultHttpClient( mThreadSafeClientConnManager, mHttpParams );

	        final HttpStack httpStack = new HttpClientStack( mDefaultHttpClient );

	        queue = Volley.newRequestQueue( context, httpStack );
		}
	}

	public RetryPolicy getRetryPolicy() {
		RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, 0,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		return retryPolicy;
	}

	public void addCookie(String key, String value, String domain) {
		BasicClientCookie cookie = new BasicClientCookie(key, value);
		cookie.setDomain(domain);
		cookie.setPath("/");
		mDefaultHttpClient.getCookieStore().addCookie(cookie);
	}

	public List<Cookie> getCookieList() {
		return mDefaultHttpClient.getCookieStore().getCookies();
	}

	public Cookie getCookie(String name) {
		List<Cookie> list = getCookieList();
		for (Cookie cookie : list) {
			Log.w("URL-authDump", cookie.getName() + ":" + cookie.getDomain() + "=" + cookie.getValue());
			if (cookie.getName().equals(name)) {
				return cookie;
			}
		}
		return null;
	}

	public void get(final String url, HashMap<String, String> params, final Listener<String> listener,
			final ErrorListener errorListener) {
		boolean first = true;
		StringBuilder urlBuf = new StringBuilder(url);
		for (String key : params.keySet()) {
			if (first) {
				urlBuf.append("?");
				first = false;
			} else {
				urlBuf.append("&");
			}
			urlBuf.append(key);
			urlBuf.append("=");
			try {
				urlBuf.append(URLEncoder.encode(params.get(key) + "", utf8));
			} catch (UnsupportedEncodingException e) {
				errorListener.onErrorResponse(new VolleyError(PARAM_ERROR, e));
			}
		}

		Log.w("URL-ping", urlBuf.toString());

		StringRequest stringRequest = new StringRequest(urlBuf.toString(), new Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.w("URL-res:", response);
				listener.onResponse(response);
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.w("URL-error", error.toString());
				
				if (error instanceof TimeoutError) {
					error = new VolleyError("网络请求超时");
				} else if (error instanceof NetworkError) {
					error = new VolleyError("网络请求失败、请检查网络");
				} else if (error instanceof ServerError) {
					error = new VolleyError("服务器返回异常");
				} else if (error instanceof NoConnectionError) {
					error = new VolleyError("网络连接失败");
				}
				if (errorListener != null) {
					errorListener.onErrorResponse(error);
				}
			}
		});
		stringRequest.setShouldCache(true);
		stringRequest.setRetryPolicy(getRetryPolicy());
		queue.add(stringRequest);
	}
	
	public void postJson(final String url, HashMap<String, String> params, final Response.Listener<JSONObject> listener,
			final ErrorListener errorListener) {
		Log.d("URL-post:", url);
		JSONObject jsonParams = new JSONObject(params);
		Log.d("URL-post params: ", jsonParams.toString());
		JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Method.POST, url, jsonParams, 
			new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject response) {
					Log.d("URL-res:", response.toString());
					listener.onResponse(response);
				}
			}, new Response.ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					Log.w("URL-error", error.toString());
					
					if (error instanceof TimeoutError) {
						error = new VolleyError("网络请求超时");
					} else if (error instanceof NetworkError) {
						error = new VolleyError("网络请求失败、请检查网络");
					} else if (error instanceof ServerError) {
						error = new VolleyError("服务器返回异常");
					} else if (error instanceof NoConnectionError) {
						error = new VolleyError("网络连接失败");
					}
					if (errorListener != null) {
						errorListener.onErrorResponse(error);
					}
				}
			});
		jsonRequest.setShouldCache(true);
		jsonRequest.setRetryPolicy(getRetryPolicy());
		queue.add(jsonRequest);
	}
	
	public void post(final String url, final HashMap<String, String> params, final Listener<String> listener,
			final ErrorListener errorListener) {
		Log.d("URL-post:", url);
		
		StringRequest stringRequest = new StringRequest(Method.POST, url, new Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.w("URL-res:", response);
				listener.onResponse(response);
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.w("URL-error", error.toString());
				
				if (error instanceof TimeoutError) {
					error = new VolleyError("网络请求超时");
				} else if (error instanceof NetworkError) {
					error = new VolleyError("网络请求失败、请检查网络");
				} else if (error instanceof ServerError) {
					error = new VolleyError("服务器返回异常");
				} else if (error instanceof NoConnectionError) {
					error = new VolleyError("网络连接失败");
				}
				if (errorListener != null) {
					errorListener.onErrorResponse(error);
				}
			}
		}) {
		
			@Override
		    protected Map<String, String> getParams() {
		        //在这里设置需要post的参数
				Log.d(TAG, url + " post params: "+params.toString());
	            return params;
		    }
		};
		stringRequest.setShouldCache(true);
		stringRequest.setRetryPolicy(getRetryPolicy());
		queue.add(stringRequest);
	}
}
