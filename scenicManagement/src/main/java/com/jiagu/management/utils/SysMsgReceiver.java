/**
 *@项目名称: 手机导游2.0
 *@Copyright: ${year} www.jiagu.com Inc. All rights reserved.
 *注意：本内容仅限于西安甲骨企业文化传播有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.jiagu.management.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class SysMsgReceiver  extends BroadcastReceiver{
    private NetworkInfo 		mNetinfo = null;
    private ConnectivityManager mConnectivityManager = null;
    
    private IntentFilter 		mFilter = null;//系统监听消息过滤
    private Activity            anchor = null;
   
    public void createReceiver(Activity activity){
    	anchor = activity;
    	
    	//系统状态通知监听设置
		mFilter = new IntentFilter(); 
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		anchor.registerReceiver(this, mFilter);
		
		//网络状态获取初始化
		mConnectivityManager = (ConnectivityManager)anchor.getSystemService(Context.CONNECTIVITY_SERVICE);
        mNetinfo = mConnectivityManager.getActiveNetworkInfo(); 
    }
    
    public void destoryReceiver(){
    	anchor.unregisterReceiver(this);
    }
	    
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		 if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                mConnectivityManager = (ConnectivityManager)anchor.getSystemService(Context.CONNECTIVITY_SERVICE);
                mNetinfo = mConnectivityManager.getActiveNetworkInfo();  
                if(mNetinfo != null && mNetinfo.isAvailable()) {
                    String name = mNetinfo.getTypeName();
                    FileTools.writeLog("scenic.txt", "当前网络名称：" + name);
                } 
		 }
	}
                
	public boolean isNetConnected(){
		if (mNetinfo != null)
		{
			if (mNetinfo.isAvailable()==true && mNetinfo.isConnected() == true)
			{
				return true;
			}
		}
		return false;
	}

}
