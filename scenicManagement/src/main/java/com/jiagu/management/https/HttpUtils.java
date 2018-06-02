package com.jiagu.management.https;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/** 
* @ClassName: HttpUtils 
* @Description: 
* @author zz
* @date 2015年1月15日 下午12:35:09 
* @version 1
* @company 西安甲骨企业文化传播有限公司  
*/  
public class HttpUtils {
	public static final int RESULTCODE_OK = 200;
	private static AsyncHttpClient client = new AsyncHttpClient();  // 实例化对象

	static {
		client.setTimeout(11000);  // 设置链接超时，如果不设置，默认为10s
		// client.addHeader(arg0, arg1);
	}

	public static void get(String urlString, AsyncHttpResponseHandler res) // 用一个完整url获取一个string对象
	{
		client.get(urlString, res);

	}

	public static void get(String urlString, RequestParams params,
			AsyncHttpResponseHandler res)  // url里面带参数
	{
		client.get(urlString, params, res);
	}

	public static void get(String urlString, JsonHttpResponseHandler res) // 不带参数，获取json对象或者数组
	{
		client.get(urlString, res);
	}

	public static void get(String urlString, RequestParams params,
			JsonHttpResponseHandler res) // 带参数，获取json对象或者数组
	{
		client.get(urlString, params, res);
	}

	public static void get(String uString, BinaryHttpResponseHandler bHandler)  // 下载数据使用，会返回byte数据
	{
		client.get(uString, bHandler);
	}

	public static void post(String urlString, RequestParams params,
			JsonHttpResponseHandler res) // 带参数，获取json对象或者数组
	{
		client.post(urlString, params, res);
	}

	public static void post(String urlString, RequestParams params,
			AsyncHttpResponseHandler res) // 带参数，获取json对象或者数组
	{
		client.post(urlString, params, res);
	}

	public static AsyncHttpClient getClient() {
		return client;
	}

	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}
}