package com.jiagu.management.application;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.jiagu.management.models.User;
import com.jiagu.management.utils.CrashHandler;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * @ClassName: ScenicApplication
 * @Description:
 * @author zz
 * @date 2015年1月15日 下午12:33:35
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class ScenicApplication extends Application {
	
//	public static final String SEVER_PATH = "http://192.168.1.108:8080/DataCollect/";
//	public static final String SEVER_RES_PATH = "http://192.168.1.108:8080/images/";
	

	
//	public static final String SEVER_PATH = "http://1.shoujidaoyou2webcs.sinaapp.com/";	                                         
//	public static final String SEVER_RES_PATH = "http://shoujidaoyou2webcs-images.stor.sinaapp.com/";
	
	public static final String SEVER_PATH = "http://www.shoujidaoyou.cn/DataCollect/";
	public static final String SEVER_RES_PATH = "http://www.shoujidaoyou.cn/image/";
	public static final String SEVER_APK_PATH = "http://www.shoujidaoyou.cn/jgwh/";
	
	public static String RootPath;
	private static Context context;
	private static LocationClient mLocationClient;
	private static SharedPreferences mUserPreferences;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
		
		CrashHandler crashHandler = CrashHandler.getInstance();  
		crashHandler.init(context);
		
		SDKInitializer.initialize(context);
		initImageLoader();
		makeFolder();
	}

	@Override
	public void onTerminate() {
		mUserPreferences.edit().clear().commit();
		super.onTerminate();
	}

	public static SharedPreferences getUserPreferences() {
		if (mUserPreferences == null) {
			mUserPreferences = context.getSharedPreferences(User.USERINFO_KEY,
					MODE_PRIVATE);
		}
		return mUserPreferences;
	}

	public static LocationClient getLocationClient() {
		if (mLocationClient == null) {
			LocationClientOption option = new LocationClientOption();
			option.setOpenGps(true);  // 是否打开GPS
			option.setCoorType("bd09ll"); // 设置返回值的坐标类型。
			option.setScanSpan(3000); // 设置定时定位的时间间隔。单位毫秒
			option.setProdName("SM");
			option.setNeedDeviceDirect(true);
			option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy); // 高精度模式
			mLocationClient = new LocationClient(context, option); // 声明LocationClient类
		}
		return mLocationClient;
	}

	private void initImageLoader() {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
				.memoryCacheSize(2 * 1024 * 1024)
				.discCacheSize(50 * 1024 * 1024)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.discCacheFileCount(100)
				.discCache(new UnlimitedDiscCache(new File(RootPath + "cache")))
				.build();
		ImageLoader.getInstance().init(config);
	}

	private void makeFolder() {
		boolean isSuccess = false;
		RootPath = Environment.getExternalStorageDirectory().getPath() + "/ScenicManagement/";
		System.out.println(RootPath);
		
		try {
			File folder = new File(RootPath + "cache");
			if (!folder.exists()) {
				isSuccess = folder.mkdirs();
				if (true == isSuccess){
					
				}
			}
		} catch (Exception e) {
			RootPath = "mnt/cache/";
		}
	}
}
