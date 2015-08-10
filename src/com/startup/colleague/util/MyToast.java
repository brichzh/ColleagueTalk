package com.startup.colleague.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by aidi on 15-06-20.
 */
public class MyToast {

  public static Toast toast;

  public static void show(Context appContext, String message) {
    toast = Toast.makeText(appContext, message, Toast.LENGTH_SHORT);
    toast.setText(message);
    toast.setGravity(Gravity.TOP, 0, 10);
    toast.show();
  }
  public static void debug(Context appContext, String message) {
    show(appContext,message);
  }
}
