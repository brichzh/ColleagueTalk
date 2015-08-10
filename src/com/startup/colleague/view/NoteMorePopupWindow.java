package com.startup.colleague.view;

import java.util.ArrayList;
import java.util.List;

import com.startup.colleague.R;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

public class NoteMorePopupWindow {
	private PopupWindow window;
	private ListView list;
	private Button cancel;
	private Context context;
	
	public void popAwindow(View parent, Context context, OnItemClickListener listClickListener, OnDismissListener dismissListener, boolean canRemove) {  
		this.context = context;
	    if (window == null) {  
	        LayoutInflater lay = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
	        View v = lay.inflate(R.layout.note_more, null);
	        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);  
	        Display display = wm.getDefaultDisplay(); 
	        Point winSize = new Point();
	        display.getSize(winSize);
	        //初始化listview，加载数据。
	        list = (ListView) v.findViewById (R.id.operations);
	        
	        List<String> data = new ArrayList<String>();
	        data.add("分享");
	        if (canRemove) {
	        	data.add("删除");
			}else {
				data.add("举报");
			}
	        data.add("取消");
	        list.setAdapter(new ArrayAdapter<String>(context, R.layout.morelistitem, data));

	        list.setItemsCanFocus(false);
	        list.setOnItemClickListener(listClickListener);  
	        
	        window = new PopupWindow(v, winSize.x-80, winSize.y/4+38); 
	        window.setOnDismissListener(dismissListener);
	    }  
	      
	    //设置整个popupwindow的样式。  
	    window.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.rounded_corners_view));  
	    //使窗口里面的空间显示其相应的效果，比较点击button时背景颜色改变。  
	    //如果为false点击相关的空间表面上没有反应，但事件是可以监听到的。  
	    //listview的话就没有了作用。  
	    window.setFocusable(true);//如果不设置setFocusable为true，popupwindow里面是获取不到焦点的，那么如果popupwindow里面有输入框等的话就无法输入。  
	    window.update();  
	    window.showAtLocation(parent, Gravity.BOTTOM, 0, 20);  
	}
	
	public void hidePopupWin() {
		if (window != null) {
			window.dismiss();
		}
	}
	
	
}