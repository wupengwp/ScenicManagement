package com.jiagu.management.activities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapDoubleClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.jiagu.management.R;
import com.jiagu.management.activities.bases.SuperActivity;
import com.jiagu.management.application.ScenicApplication;
import com.jiagu.management.https.HttpUtils;
import com.jiagu.management.models.Attract;
import com.jiagu.management.models.User;
import com.jiagu.management.utils.Constants;
import com.jiagu.management.utils.CustomDialog;
import com.jiagu.management.utils.FileTools;
import com.jiagu.management.utils.SysMsgReceiver;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


/**
 * @ClassName: BaiduMarKingActivity
 * @Description:
 * @author xujintao
 * @date 2015年1月15日 上午9:47:53
 * @version 1
 * @company 西安甲骨企业文化传播有限公司 地图点位显示
 */

public class BaiduMarKingActivity extends SuperActivity implements
	OnMapDoubleClickListener , OnClickListener, OnMarkerClickListener, OnKeyListener {
	public static final int REQUEST_FROM_BAIDUMARKINGACTIVITY = 1;
	public static final int RESULT_CODE = 200;
	public static final String MARKER_LOUCS_KEY = "MARKER_LOUCS_KEY";
	
	private static final int MAX_LEVEL 	= 19; // 百度地图最大缩放级别
	private static final int COLLECT_TYPE_SPOT = 1;//景点采集
	private static final int COLLECT_TYPE_REGION = 2;//景区采集
	private static final int COLLECT_TYPE_PATH = 3;//景区道路采集
	private static final int COLLECT_TYPE_NULL = 0;//未设置采集方式
	
	private int mCollectType = COLLECT_TYPE_NULL;//景点采集类型
	
	private ImageButton mMarkingBtn;//景点采集按钮

	private MapView mMapView;//百度地图视图
	private BaiduMap mBaiduMap;//百度地图
	private LocationClient mMarkingLocationClient;//定位监听器
	private LocationManager mLocationManager;//定义管理
	private BitmapDescriptor mSingleBitmap;//单景点标识
	private BitmapDescriptor mSingleBindBitmap;//单景点已绑定标识
	private BitmapDescriptor mMultiBitmap;//多景点标识

	private CustomDialog.EditDialog mEditDialog;
	private CustomDialog.TableDialog mTableDialog;
	private CustomDialog.GeneralDialog mGeneralDialog;
	private CustomDialog.GeneralDialog mCancelDialog;
	private CustomDialog.GeneralDialog mNextDialog;
	
	private Attract mAttract = null;
	
	private ArrayList<Overlay> mAttractOverlays = null;
	private ArrayList<Marker> mMarkers = null;
//	private ArrayList<LatLng> mPathPoints = null;
	
	
	private static final int COLLECT_STATUS_IDLE = 0;//未开始
	private static final int COLLECT_STATUS_DOING = 1;//正在收集
	private static final int COLLECT_STATUS_FINISH = 2;//完成收集
	
	private int mCollectStatus = COLLECT_STATUS_IDLE;//多点采集是否还需要采集下一个景点
	private boolean mIsAutoCollect = false;
	private boolean mIsNeedCollect = false;//是否单点采集当前景点
	private boolean mIsFirstLocation = true;//第一次定位
	
	ArrayList<MotionEvent> mEventList = null;//触屏事件记录列表
	
	private int upActivity = 0;//记录上一个activity的来源信息
	
	SysMsgReceiver mSysMsgReceiver = null;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_baidu_marking);
		
		checkGPSSetting();	
		
		findView();
		
		mSysMsgReceiver = new SysMsgReceiver();
		mSysMsgReceiver.createReceiver(this);
		
		mEventList = new ArrayList<MotionEvent>();
		
		upActivity = getIntent().getIntExtra("UpActivity", -1);
		if (upActivity != -1) {
			edit();
		}
	}
	
	private boolean isNetConnected(){
		return mSysMsgReceiver.isNetConnected();
	}
	
	private void edit() {
		Intent data = getIntent();
		int Operation = data.getIntExtra("Operation", -1);
		mAttract = (Attract) data.getSerializableExtra("Attract");
		
		if (Operation == ScenicSpotActivity.RESETINFO) {
			mGeneralDialog.show();
		} else if (Operation == ScenicSpotActivity.EDITINFO) {
			mEditDialog.setValue(mAttract.name);
			mEditDialog.show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		FileTools.writeLog("scenic.txt", "invoke onActivityResult");
		
		if (mMapView != null) {
			mMapView.invalidate();
		}
		
		if (resultCode == RESULT_CODE) {
			int Operation = data.getIntExtra("Operation", -1);
			mAttract = (Attract) data.getSerializableExtra("Attract");
			if (Operation == ScenicSpotActivity.RESETINFO) {
				mAttract.locations.clear();
				mGeneralDialog.show();
			} 
			else if (Operation == ScenicSpotActivity.EDITINFO) {
				mEditDialog.setValue(mAttract.name);
				mEditDialog.show();
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		mBaiduMap.clear();
		mMapView.onResume();
		
		if (isNetConnected() == true){
			sendGetInfoRequest();
		}
		else{
			showToast(R.string.network_error_try_again);
		}
		
		mMapView.refreshDrawableState();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mSysMsgReceiver.destoryReceiver();
		
		// 退出时销毁定位
		mMarkingLocationClient.stop();
		
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		super.onDestroy();
	}
	
	private void checkGPSSetting(){
		// 判断GPS是否正常启动
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// 返回开启GPS导航设置界面
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(intent, 0);
			return;
		}
	}
	
	private void findView() {
		initView();
		initDialog();
		initBaiduMap();
		initLocationClient();
	}
	
	private void initView(){
		mTitle = (TextView) findViewById(R.id.title_bar_text);
		mTitle.setText(User.getScenicName());
		
		mLeftBtn = (TextView) findViewById(R.id.title_left_btn);
		mLeftBtn.setVisibility(View.VISIBLE);
		mLeftBtn.setOnClickListener(this);
		
		mRightBtn = (Button) findViewById(R.id.title_right_btn);
		mRightBtn.setText(R.string.show_scenic_spot_by_list);
		mRightBtn.setVisibility(View.VISIBLE);
		mRightBtn.setOnClickListener(this);
		
		mMarkingBtn = (ImageButton) findViewById(R.id.baidu_map_marking);
		mMarkingBtn.setOnClickListener(this);
	}
	
	//初始化对话框
	private void initDialog(){
		initGeneralDialog();
		initEditDialog();
		initTableDialog();
		initCancelDialog();
		initNextDialog();
	}
	
	private void initGeneralDialog(){
		mGeneralDialog = new CustomDialog.GeneralDialog(this);
		mGeneralDialog.setMessage(getString(R.string.choose_scenic_spot_marking));
		mGeneralDialog.setOnLeftButtonClickListener(getString(R.string.marking_type_spot),R.drawable.icon_dialog_left_2, markingTypeClick);// 单点标注
		mGeneralDialog.setOnMiddleButtonClickListener(getString(R.string.marking_type_path), R.drawable.icon_dialog_right_2, markingTypeClick);//道路标注
		mGeneralDialog.setOnRightButtonClickListener(getString(R.string.marking_type_region),R.drawable.icon_dialog_right_2, markingTypeClick);// 区域标注
		//mGeneralDialog.setOnRightButtonClickListener("区域",R.drawable.icon_dialog_right_2, markingTypeClick);// 区域标注
		mGeneralDialog.setOnKeyListener(this);
		
		//设置对话框位置
		Window dialogWindow = mGeneralDialog.getWindow();
		dialogWindow.setGravity(Gravity.BOTTOM | Gravity.CENTER);
	}
	
	private void initEditDialog(){
		mEditDialog = new CustomDialog.EditDialog(this);
		mEditDialog.setOnLeftButtonClickListener(getString(R.string.dialog_ok), setNameClick);
		mEditDialog.setOnRightButtonClickListener(getString(R.string.dialog_cancel), setNameClick);
		mEditDialog.setOnKeyListener(this);
		
		//设置对话框位置
		Window dialogWindow = mEditDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM | Gravity.CENTER);
	}
	
	private void initTableDialog(){
		mTableDialog = new CustomDialog.TableDialog(this);
		mTableDialog.setOnLeftButtonClickListener(saveSingleMarkerClick);
		mTableDialog.setOnRightButtonClickListener(saveSingleMarkerClick);
		mTableDialog.setOnKeyListener(this);
		
		//设置对话框位置
		Window dialogWindow = mTableDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM | Gravity.CENTER);
	}
	
	private void initCancelDialog(){
		mCancelDialog = new CustomDialog.GeneralDialog(this);
		mCancelDialog.hideMiddleButton();
		mCancelDialog.setMessage(getString(R.string.cancel_marker_prompt));
		mCancelDialog.setOnLeftButtonClickListener(getString(R.string.dialog_ahead), null, markingCancelClick);
		mCancelDialog.setOnRightButtonClickListener(getString(R.string.dialog_cancel), null, markingCancelClick);
	}
	
	private void initNextDialog(){
		mNextDialog = new CustomDialog.GeneralDialog(this);
		mNextDialog.setMessage(getString(R.string.please_go_next));
		mNextDialog.hideMiddleButton();
		mNextDialog.setOnLeftButtonClickListener(getString(R.string.dialog_marking), null, saveMultiMarkerClick);
		mNextDialog.setOnRightButtonClickListener(getString(R.string.dialog_success), null, saveMultiMarkerClick);
		mNextDialog.setCanceledOnTouchOutside(false);
		mNextDialog.setOnKeyListener(this);
		
		//设置对话框位置
		Window dialogWindow = mNextDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM | Gravity.CENTER);
	}
	
	private void initBaiduMap(){
		mMapView = (MapView) findViewById(R.id.baidu_map);
		mMapView.showScaleControl(false);
		mMapView.showZoomControls(false);
		
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMyLocationEnabled(true);
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(MAX_LEVEL));
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.FOLLOWING, true, null));
		mBaiduMap.setOnMarkerClickListener(this);
		mBaiduMap.setOnMapDoubleClickListener(this);
		
		mSingleBitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_marker_n);
		mSingleBindBitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_marker_p);
		mMultiBitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_double_marker);
	}
	
	private void initLocationClient(){
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 是否打开GPS
		option.setCoorType("bd09ll"); // 设置返回值的坐标类型。
		option.setScanSpan(1000); // 设置定时定位的时间间隔。单位毫秒
		option.setProdName("SM");
		option.setNeedDeviceDirect(true);
		option.setIsNeedAddress(true);
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy); // 高精度模式
	
		mMarkingLocationClient = new LocationClient(getApplicationContext());
		mMarkingLocationClient.setLocOption(option);
		
		mMarkingLocationClient.start();
		mMarkingLocationClient.requestLocation();
		
		mMarkingLocationClient.registerLocationListener(locationListener);
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	// 上传新景点/修改景点
	private void sendUploadRequest() {
		FileTools.writeLog("scenic.txt", "sendUploadRequest");
		
		showProgress();
		String url = ScenicApplication.SEVER_PATH + "scenicUser/uploadNewAttrac.htm";
		RequestParams params = new RequestParams();
		params.put("scenicID", User.getScenicid());
		FileTools.writeLog("scenic.txt", "景区id:"+User.getScenicid());
		params.put("attractID", mAttract.id);
		FileTools.writeLog("scenic.txt", "景点id:"+mAttract.id);
		params.put("attractName", mAttract.name);
		FileTools.writeLog("scenic.txt", "name:"+mAttract.name);
		params.put("attractTpye", mAttract.type + "");
		FileTools.writeLog("scenic.txt", "type:"+mAttract.type + "");
		params.put("attractLocation", mAttract.getJSONLocations());
		FileTools.writeLog("scenic.txt", "JSON:"+mAttract.getJSONLocations());
		
		onPost(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(Throwable arg0, String result) {
				showToast(R.string.request_error_prompt);
				hideProgress();
				clearCollectAttract();
			}

			@Override
			public void onSuccess(int code, String result) {
				FileTools.writeLog("scenic.txt", result);
				
				if (HttpUtils.RESULTCODE_OK == code) {
					System.out.println(result);
					try {
						JSONObject root = new JSONObject(result);
						if (Constants.SUCCESS.equals(root.optString("result"))) {	
							sendGetInfoRequest();
							clearCollectAttract();
						} else {
							showToast(R.string.request_error_prompt);
						}
					} catch (JSONException e) {
						
					
						FileTools.writeLog("scenic.txt", "JSONException toString:"+e.toString());
						
						showToast(R.string.system_error_prompt);
					} finally {
						hideProgress();
					}
				} else {
					showToast(R.string.network_error_try_again);
					hideProgress();
				}
				
				mTableDialog.dismiss();
				mNextDialog.dismiss();
				mEditDialog.dismiss();
				mCancelDialog.dismiss();
			}
		});
	}
	
	private void clearCollectAttract(){
		if (null != mMarkers){
			mMarkers.clear();
			mMarkers = null;
		}
		
		mAttract = null;
		mCollectType = COLLECT_TYPE_NULL;
		mCollectStatus = COLLECT_STATUS_IDLE;
		mIsAutoCollect = false;
	}

	// 获取景区内景点的信息
	private void sendGetInfoRequest() {
		FileTools.writeLog("scenic.txt", "sendGetInfoRequest");
		
		showProgress();
		String url = ScenicApplication.SEVER_PATH + "scenicUser/getSignScenicInfo.htm";
		RequestParams params = new RequestParams();
		params.put("scenicID", User.getScenicid());
		
		onPost(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(Throwable arg0, String result) {
				showToast(R.string.get_data_error_prompt);
				hideProgress();
			}

			@Override
			public void onSuccess(int code, String result) {
				
				FileTools.writeLog("scenic.txt", result);
				
				if (HttpUtils.RESULTCODE_OK == code) {
					mBaiduMap.clear();
					if (mAttractOverlays != null){
						mAttractOverlays.clear();
					}
					else{
						mAttractOverlays = new ArrayList<Overlay>();
					}
					System.out.println(result);
					try {
						JSONObject root = new JSONObject(result);
						if (Constants.SUCCESS.equals(root.optString("result"))) {
							JSONArray data = root.optJSONArray("records");
							for (int i = 0; i < data.length(); i++) {
								Attract attract = new Attract(data.optJSONObject(i));
								if (attract.type == Attract.SPOTMARKING) {
									if (attract.locations.size() > 0) {
										mAttractOverlays.add(setMarker(attract));
									}
								} else {									
									mAttractOverlays.add(setMarkers(attract));
								}
							}
						}
						
						if (null != mAttract){
							FileTools.writeLog("scenic.txt", "createMarkers from sendGetInfoRequest");
							createMarkers(mAttract);
						}
					} catch (JSONException e) {
						System.out.println(e);
						FileTools.writeLog("scenic.txt", "JSONException toString:"+e.toString());
						FileTools.writeLog("scenic.txt", "JSONException getMessage:"+e.getMessage());
						showToast(R.string.system_error_prompt);
					} finally {
						hideProgress();
					}
				} else {
					showToast(R.string.network_error_try_again);
					hideProgress();
				}
			}
		});
	}
	
	private Overlay setMarker(Attract attract){
		
		//FileTools.writeLog("scenic.txt", "invoke setMarker");
		Attract.Location location = attract.locations.get(0);
		LatLng latlng = new LatLng(location.lat, location.lon);
		//OverlayOptions option = new MarkerOptions().position(latlng).icon(mSingleBitmap);
		OverlayOptions option = null;//new MarkerOptions().position(point).icon(mSingleBitmap);
		if ((attract.x == -99999 || attract.x==0) && (attract.y==-99999||attract.y==0)){
			option = new MarkerOptions().position(latlng).icon(mSingleBitmap);
		}
		else{
			option = new MarkerOptions().position(latlng).icon(mSingleBindBitmap);
		}
		Overlay ol = (mBaiduMap.addOverlay(option));
		Bundle bundle = new Bundle();
		bundle.putSerializable(Constants.MARKER_LOUCS_KEY,attract);
		ol.setExtraInfo(bundle);
		ol.setVisible(true);
		
		return ol;
	}
	
	private Overlay setMarkers(Attract attract){
		Overlay ol = null;
		ArrayList<LatLng> pts = new ArrayList<LatLng>();
		for (Attract.Location location : attract.locations) {
			if (location.isMarker == true){
				OverlayOptions option = new MarkerOptions().position(new LatLng(location.lat, location.lon)).icon(mMultiBitmap);
				mBaiduMap.addOverlay(option);
			}
			pts.add(new LatLng(location.lat, location.lon));
		}

		if (Attract.REGIONMARKING == attract.type){
			// 构建用户绘制多边形的Option对象
			OverlayOptions polygonOption = new PolygonOptions().points(pts).stroke(new Stroke(5, 0xAA00FF00)).fillColor(0xAAFFFF00);
			ol = mBaiduMap.addOverlay(polygonOption);	
		}
		else if (Attract.PATHMARKING == attract.type){
			// 构建用户绘制多边形的Option对象
			OverlayOptions polylineOption = new PolylineOptions().points(pts).color(0xAAFF0033).width(10);
			ol = mBaiduMap.addOverlay(polylineOption);	
		}
		
		Bundle bundle = new Bundle();
		bundle.putSerializable(Constants.MARKER_LOUCS_KEY,attract);
		ol.setExtraInfo(bundle);
		ol.setVisible(true);
		
		return ol;
	} 
	
	private void createMarkers(Attract attract){
		for (Attract.Location location : attract.locations) {
			if (location.isMarker == true){
				OverlayOptions option = new MarkerOptions().position(new LatLng(location.lat, location.lon)).icon(mMultiBitmap);
				mBaiduMap.addOverlay(option);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_left_btn://退回
			onBackPressed();
			break;
			
		case R.id.title_right_btn://显示景点列表
			startActivityForResult(new Intent(this, ScenicSpotActivity.class), REQUEST_FROM_BAIDUMARKINGACTIVITY);
			break;
			
		case R.id.baidu_map_marking://弹出标记对话框
			mGeneralDialog.show();//显示对话框
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		mAttract = (Attract) marker.getExtraInfo().getSerializable(Constants.MARKER_LOUCS_KEY);
		if (mAttract.type == Attract.SPOTMARKING) {
			mEditDialog.setValue(mAttract.name);
			mEditDialog.show();
		}
		return false;
	}

	public OnClickListener saveSingleMarkerClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.dialog_left_btn:
				if (isNetConnected() == true){
					sendUploadRequest();
				}
				else{
					showToast(R.string.network_error_try_again);
				}
				
				break;
			case R.id.dialog_right_btn:
				mCancelDialog.show();
				break;
			default:
				break;
			}
		}
	};

	// 点击标注
	public OnClickListener saveMultiMarkerClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.dialog_left_btn:
				mNextDialog.dismiss();
				mCollectStatus = COLLECT_STATUS_DOING;
				break;
			case R.id.dialog_right_btn:
				if (mAttract.locations.size() < 2) {
					showToast(R.string.many_marking_err_prompt);
					return;
				}
				mCollectStatus = COLLECT_STATUS_FINISH;
				mIsAutoCollect = false;
				mEditDialog.setValue(mAttract.name);
				mEditDialog.show();
				mNextDialog.dismiss();
				break;
			default:
				break;
			}
		}
	};
	
	public OnClickListener setNameClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.dialog_left_btn:
				if (!mEditDialog.isInput()) {
					showToast("请输入名字");
					return;
				}
				
				if (mAttractOverlays != null){
					FileTools.writeLog("scenic.txt", "mAttractMarkers != null");
					for (int i=0; i<mAttractOverlays.size(); i++){
						Attract attr = (Attract)mAttractOverlays.get(i).getExtraInfo().get(Constants.MARKER_LOUCS_KEY);
						FileTools.writeLog("scenic.txt", "attr = (Attract)mMarkers.get(i)");
						if (attr != null){
							FileTools.writeLog("scenic.txt", "attr != null");
							if (mEditDialog.getInputValue().equals(attr.name) && (!attr.id.equals(mAttract.id))){
								showToast("该名字已设置，请换一个名字");
								return ;
							}
						}
					}
				}
				
				mEditDialog.dismiss();
				if (mEditDialog.getInputValue() != null) {
					mAttract.name = mEditDialog.getInputValue();
					
					if (mAttract.type == Attract.SPOTMARKING) {
						Attract.Location location = mAttract.locations.get(0);
						mTableDialog.setInfo(mAttract.name, location.lat + "", location.lon + "");
						mTableDialog.show();				
					} 
					else {
						if (isNetConnected() == true){
							sendUploadRequest();//上传景点标记
						}
						else{
							showToast(R.string.network_error_try_again);
						}
					}
				}
				
				break;
			case R.id.dialog_right_btn:
				mEditDialog.dismiss();
				if (mCollectType == COLLECT_TYPE_NULL){
					clearCollectAttract();
				}
				
				break;
			default:
				break;
			}
		}
	};
	
	// 多点。单点标注
	public OnClickListener markingTypeClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			switch (v.getId()) {
			case R.id.dialog_left_btn://单点标记
				mCollectType = COLLECT_TYPE_SPOT;//设置单点标记
				mIsNeedCollect = true;//设置采集数据标志
				break;
			case R.id.dialog_middle_btn://多点标记
				mCollectType = COLLECT_TYPE_PATH;//设置多点标记	
				mCollectStatus = COLLECT_STATUS_DOING;//设置采集数据标志
				mIsAutoCollect = true;
				break;	
			case R.id.dialog_right_btn://多点标记
				mCollectType = COLLECT_TYPE_REGION;//设置多点标记
				mCollectStatus = COLLECT_STATUS_DOING;//设置采集数据标志
				break;
			default:
				break;
			}
//			if (mMapView != null) {
//				mMapView.invalidate();
//			}
			mGeneralDialog.dismiss();
		}
	};
	public OnClickListener markingCancelClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.dialog_left_btn:
				mEditDialog.dismiss();
				mTableDialog.dismiss();
				mCancelDialog.dismiss();
				mNextDialog.dismiss();
				mGeneralDialog.dismiss();
				if (isNetConnected() == true){
					clearCollectAttract();
					sendGetInfoRequest();
				}
				else{
					showToast(R.string.network_error_try_again);
				}
				break;
			case R.id.dialog_right_btn:
				mCancelDialog.dismiss();
				break;
			default:
				break;
			}
		}
	};

	private BDLocationListener locationListener = new BDLocationListener() {
		@Override
		public void onReceiveLocation(BDLocation location) {			
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			
			setLocationCenter(location);

			switch (mCollectType) {
				case COLLECT_TYPE_SPOT://景点标记
					if (true == mIsNeedCollect){
						mIsNeedCollect = false;
						addSpotAttract(location.getLatitude(), location.getLongitude());
					}
					
					break;
				case COLLECT_TYPE_REGION://区域标记
					if (mNextDialog.isShowing() == false && mCollectStatus == COLLECT_STATUS_DOING){
						mNextDialog.show();
					}
					
					if (COLLECT_STATUS_IDLE != mCollectStatus){
						int status = mCollectStatus;
						mCollectStatus = COLLECT_STATUS_IDLE;
						addRegionAttract(location.getLatitude(), location.getLongitude());
						
						if (status == COLLECT_STATUS_FINISH){
							deleteInvalidPoint();
						}
					}
					break;
				case COLLECT_TYPE_PATH://道路标记
					if (mNextDialog.isShowing() == false && mCollectStatus == COLLECT_STATUS_DOING){
						mNextDialog.show();
					}
					
					if (COLLECT_STATUS_IDLE != mCollectStatus){
						int status = mCollectStatus;
						mCollectStatus = COLLECT_STATUS_IDLE;
						addPathAttract(location.getLatitude(), location.getLongitude());
						if (status == COLLECT_STATUS_FINISH){
							deleteInvalidPoint();
						}
					}
				
					break;
					
				default ://弹出标记对话框	
			}
		}
	};
	
//	private void setLocationConfig(com.baidu.mapapi.map.MyLocationConfiguration.LocationMode mode){
//		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mode, false, null));
//		if (com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.FOLLOWING == mode){
//			mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(MAX_LEVEL));
//		}
//	} 
	
	private void addSpotAttract(double latitude, double  Longitude){
		if (mAttract == null){
			mAttract = new Attract();	
		}
		
		Attract.Location latlng = new Attract.Location(Longitude, latitude);
		if (null == mAttract.id || mAttract.id.equals("")){
			mAttract.id = "";
		}
		if (mAttract.locations.size() > 0){
			mAttract.locations.clear();
		}
		mAttract.locations.add(latlng);
		mAttract.type = Attract.SPOTMARKING;
		
		mEditDialog.setValue("");
		mEditDialog.show();	
	}
	
	private void addRegionAttract(double latitude, double  Longitude){
		placeMarker(new LatLng(latitude, Longitude), mMultiBitmap);
		mAttract.type =  Attract.REGIONMARKING;		
	}
	
	private void addPathAttract(double latitude, double  Longitude){
		placeMarker(new LatLng(latitude, Longitude), mMultiBitmap);
		mAttract.type =  Attract.PATHMARKING;
	}
	
	private void placeMarker(LatLng point, BitmapDescriptor icon){
		if (mAttract == null){
			mAttract = new Attract();	
		}
		if (mMarkers == null){
			mMarkers = new ArrayList<Marker>();
		}
	
		//增加多点采集的一个景点节点
		Attract.Location latlng = new Attract.Location(point.longitude, point.latitude);
		mAttract.locations.add(latlng);	
			
		// 构建MarkerOption，用于在地图上添加Marker , bitmap为Marker图标
		OverlayOptions option = new MarkerOptions().position(point).icon(icon);
		Overlay marker = mBaiduMap.addOverlay(option);// 在地图上添加Marker，并显示
		
		mMarkers.add((Marker)marker);//记录marker点
	}
	
//	private void gatherPathPoint(LatLng point, boolean isMarker){
//		if (mAttract != null){
//			Attract.Location latlng = new Attract.Location(point.longitude, point.latitude, isMarker);
//			mAttract.locations.add(latlng);	
//		}
//	}
	
	private void deleteInvalidPoint(){
		for (int i=mAttract.locations.size(); i>0; i--){
			if (mAttract.locations.get(i-1).isMarker == false){
				mAttract.locations.remove(i-1);
			}
			else{
				break;
			}
		}
	} 
	
//	private void createMarker(Attract attract) {
//		if (Attract.SPOTMARKING == attract.type){
//			Attract.Location location = attract.locations.get(0);
//			LatLng point = new LatLng(location.lat, location.lon);// 定义Maker坐标点
//			
//			// 构建MarkerOption，用于在地图上添加Marker , bitmap为Marker图标
//			OverlayOptions option = null;//new MarkerOptions().position(point).icon(mSingleBitmap);
//			if ((attract.x == -99999 || attract.x==0) && (attract.y==-99999||attract.y==0)){
//				option = new MarkerOptions().position(point).icon(mSingleBitmap);
//			}
//			else{
//				option = new MarkerOptions().position(point).icon(mSingleBindBitmap);
//			}
//			
//			Marker marker = (Marker) mBaiduMap.addOverlay(option);// 在地图上添加Marker，并显示
//			
//			Bundle bundle = new Bundle();
//			bundle.putSerializable(Constants.MARKER_LOUCS_KEY, attract);
//			marker.setExtraInfo(bundle);
//			marker.setVisible(true);
//		}
//		else if (Attract.REGIONMARKING == attract.type){
//			ArrayList<LatLng> pts = new ArrayList<LatLng>();
//			for (Attract.Location location : attract.locations) {
//				pts.add(new LatLng(location.lat, location.lon));
//			}
//			
//			// 构建用户绘制多边形的Option对象
//			OverlayOptions polygonOption = new PolygonOptions().points(pts).stroke(new Stroke(5, 0xAA00FF00)).fillColor(0xAAFFFF00);
//			mBaiduMap.addOverlay(polygonOption);
//		}
//		else if (Attract.PATHMARKING == attract.type){
//			ArrayList<LatLng> pts = new ArrayList<LatLng>();
//			for (Attract.Location location : attract.locations) {
//				pts.add(new LatLng(location.lat, location.lon));
//			}
//			
//			// 构建用户绘制多边形的Option对象
//			OverlayOptions polylineOption = new PolylineOptions().points(pts).color(0xAAFF0033).width(10);
//			mBaiduMap.addOverlay(polylineOption);
//		}
//	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			mCancelDialog.show();
			return true;
		}
		return false;
	}
	
	public void onMapDoubleClick(LatLng point){
		
	}
	
	private void setLocationCenter(BDLocation location) {
		if (mIsFirstLocation == true) {
			// 设定中心点坐标
			LatLng cenpt = new LatLng(location.getLatitude(),
					location.getLongitude());
			// 定义地图状态
			MapStatus mMapStatus = new MapStatus.Builder().target(cenpt)
					.zoom(18).build();
			// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
			MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
					.newMapStatus(mMapStatus);
			// 改变地图状态
			mBaiduMap.setMapStatus(mMapStatusUpdate);
			mIsFirstLocation = false;
		}
	}
	
}
