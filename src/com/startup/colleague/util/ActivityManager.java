package com.startup.colleague.util;

import java.util.Stack;

import android.app.Activity;
import android.util.Log;

public class ActivityManager {  
	  
    private static Stack<Activity> activityStack;  
  
    private static ActivityManager instance;  
  
    private ActivityManager(){  
  
    }  
  
    public static ActivityManager getActivityManager(){  
  
        if(instance == null){  
  
            instance = new ActivityManager();  
  
        }  
        return instance;  
    }  
  
    public void  popActivityStack(Activity activity){        
  
        if(null != activity){  
  
            activity.finish();  
  
            activityStack.remove(activity);  
  
            activity= null;  
  
        }  
    }  
  
    public void pushActivity2Stack(Activity activity){  
  
        if(activityStack== null){  
  
            activityStack= new Stack<Activity>();  
  
        }  
  
        activityStack.add(activity);  
  
    }  
  
    public Activity getCurrentActivity(){  
  
        Activity activity = null;  
  
        if(!activityStack.isEmpty()){  
  
            activity= activityStack.lastElement();  
  
        }  
  
        return activity;  
  
    }  
  
    public void popAllActivityFromStack(){  
  
         while(true){  
        	
            Activity activity = getCurrentActivity();  
            
            if(activity == null){  
                break;  
            }  
            Log.d("ActivityManager", activity.getLocalClassName());
            popActivityStack(activity);  
        }  
  
    }  
  
}  