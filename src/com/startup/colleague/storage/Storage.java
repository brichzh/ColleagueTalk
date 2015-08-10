package com.startup.colleague.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

/**
 * @author aidi
 */
public class Storage {
	private static final boolean SHOULD_COMMIT = Integer.parseInt(Build.VERSION.SDK) < 9;

	protected SharedPreferences sharedPreferences;

	public Storage() {

	}

	public Storage(Context context, String namespace) {

		sharedPreferences = context.getApplicationContext().getSharedPreferences(namespace, Context.MODE_PRIVATE);
	}

	public String get(String key) {
		return sharedPreferences.getString(key, "");
	}

	public String get(String key, String defValue) {
		return sharedPreferences.getString(key, defValue);
	}

	public void put(String key, String value) {
		put(key, value, false);
	}

	public void put(String key, String value, boolean forceSync) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		if (forceSync || SHOULD_COMMIT) {
			editor.commit();
		} else {
			editor.apply();
		}
	}
}