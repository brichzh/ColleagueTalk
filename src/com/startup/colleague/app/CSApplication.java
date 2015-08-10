package com.startup.colleague.app;

import com.enrique.stackblur.StackBlurManager;
import com.startup.colleague.R;

import android.app.Application;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Log;

public class CSApplication extends Application {
	private static final String TAG = "MMApplication";

    private static CSApplication m_instance;
    private static Context m_context;
    private StackBlurManager _stackBlurManager;
    private BitmapDrawable background;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        m_context = getApplicationContext();
        m_instance = this;
        
		Thread thread = new Thread(runnable);
		thread.start();
    }

    public static CSApplication getInstance() {
        return m_instance;
    }

    public static Context getContext() {
        return m_context;
    }
    
    public BitmapDrawable getBackground() {
    	if (background == null) {
    		_stackBlurManager = new StackBlurManager(BitmapFactory.decodeResource(getResources(), R.drawable.background_start));
			background = new BitmapDrawable(getResources(), _stackBlurManager.process(200));
		}
    	return background;
    }
    
    Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			_stackBlurManager = new StackBlurManager(BitmapFactory.decodeResource(getResources(), R.drawable.background_start));
			background = new BitmapDrawable(getResources(), _stackBlurManager.process(200));
		}
	};
}
