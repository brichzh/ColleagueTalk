package com.startup.colleague.share;


import com.startup.colleague.R;
import com.startup.colleague.util.Util;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class WXShareManager implements IWXAPIEventHandler{
	
	private static final String TAG = "WXShareService";
	private static WXShareManager instance;
	// IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;
	private Context mContext;
	
	public static WXShareManager getInstance(Context context) {
		if (instance == null) {
			instance = new WXShareManager(context);
		}
		
		return instance;
	}
	
    public WXShareManager (Context context) {
    	mContext = context;
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
    	api = WXAPIFactory.createWXAPI(context, Constants.APP_ID, false);
		// 将该app注册到微信
		Log.d(TAG, "将该app注册到微信");
	    if (api.registerApp(Constants.APP_ID)) {
	    	Log.d(TAG, "注册成功");
		} else {
			Log.d(TAG, "注册失败");
		}
    }
    
    public void shareTextToWX(String content, boolean isTimelineCb) {
    	Log.d(TAG, "shareTextToWX"+content);
    	
		if (content == null || content.length() == 0) {
			return;
		}
		Log.d("", "send text");
		// 初始化一个WXTextObject对象
		WXTextObject textObj = new WXTextObject();
		textObj.text = content;

		// 用WXTextObject对象初始化一个WXMediaMessage对象
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObj;
		// 发送文本类型的消息时，title字段不起作用
		// msg.title = "Will be ignored";
		msg.description = content;

		// 构造一个Req
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
		req.message = msg;
		req.scene = isTimelineCb ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		
		// 调用api接口发送数据到微信
		api.sendReq(req);
    }
    
    public void shareH5ToWX(String url, String des, boolean isTimelineCb) {
    	Log.d(TAG, "shareH5ToWX url="+url);
    	WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = "http://www.baidu.com";
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = "来自同事说";
		msg.description = "WebPage Description";
		Bitmap thumb = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.small_icon);
		msg.thumbData = Util.bmpToByteArray(thumb, true);
		Log.d("", "msg.thumbData="+msg.thumbData.length);
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;
		req.scene = isTimelineCb ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		api.sendReq(req);
    }
    
	// 微信发送请求到第三方应用时，会回调到该方法
	@Override
	public void onReq(BaseReq req) {
		Log.d(TAG, "onReq");
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			break;
		default:
			break;
		}
	}

	// 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
	@Override
	public void onResp(BaseResp resp) {
		Log.d(TAG, "onResp");
		int result = 0;
		
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			result = R.string.errcode_success;
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = R.string.errcode_cancel;
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = R.string.errcode_deny;
			break;
		default:
			result = R.string.errcode_unknown;
			break;
		}
		
		Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
	}
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
}