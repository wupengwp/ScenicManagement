package com.jiagu.management.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/** 
* @ClassName: Utils 
* @Description: 
* @author zz
* @date 2015骞�1鏈�15鏃� 涓嬪崍12:35:50 
* @version 1
* @company 瑗垮畨鐢查浼佷笟鏂囧寲浼犳挱鏈夐檺鍏徃  
*/ 
public class Utils {

	public static boolean isNullOrEmpty(String text) {
		if (text == null)
			return true;
		if (text.length() <= 0)
			return true;

		return false;
	}

	public static boolean isMobileNO(String mobiles) {
		Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	public static String getVersionName(Context cotenxt) {
		try {
			// 锟斤拷取packagemanager锟斤拷实锟斤拷
			PackageManager packageManager = cotenxt.getPackageManager();
			// getPackageName()锟斤拷锟姐当前锟斤拷陌锟斤拷锟斤拷锟�0锟斤拷锟斤拷锟角伙拷取锟芥本锟斤拷息
			PackageInfo packInfo = packageManager.getPackageInfo(
					cotenxt.getPackageName(), 0);
			String version = packInfo.versionName;
			return version;
		} catch (NameNotFoundException e) {
			return "";
		}
	}
}
