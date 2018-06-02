package com.jiagu.management.fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.model.LatLng;

import com.jiagu.management.R;
import com.jiagu.management.activities.BaiduMarKingActivity;
import com.jiagu.management.activities.ScenicMarKingActivity;
import com.jiagu.management.application.ScenicApplication;
import com.jiagu.management.https.HttpUtils;
import com.jiagu.management.models.Attract;
import com.jiagu.management.models.User;
import com.jiagu.management.utils.Constants;
import com.jiagu.management.utils.FileTools;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

/** 
* @ClassName: MainFragment 
* @Description: 
* @author zz
* @date 2015年1月15日 下午12:34:37 
* @version 1
* @company 西安甲骨企业文化传播有限公司  
*/ 
public class MainFragment extends Fragment implements OnClickListener,
		BDLocationListener, OnMarkerClickListener, OnMapStatusChangeListener {

	private TextView mTitleTv;
	private TextView mLogoTv;
	private ImageView mLogoIv;
	private TextView mBaiduBtn, mScenicBtn;

	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private LocationClient mLocationClient;
	private BitmapDescriptor bitmap;
	private String uri;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_marker_p);
		return inflater.inflate(R.layout.fragment_main, container, false);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mTitleTv = (TextView) getView().findViewById(R.id.title_bar_text);
		mTitleTv.setText(User.getScenicName());
		mLogoTv = (TextView) getView().findViewById(R.id.user_logo_show);
		mLogoTv.setText(User.getUsername());
		mLogoIv = (ImageView) getView().findViewById(R.id.user_logo_icon);
		String uri = ScenicApplication.SEVER_RES_PATH + User.getTourIcon();
		
		if (User.getTourIcon().length() > 0) {
			FileTools.writeLog("scenic.txt", User.getTourIcon());
			ImageLoader.getInstance().displayImage(uri, mLogoIv);
		}
		mBaiduBtn = (TextView) getView().findViewById(R.id.ok_btn);
		mScenicBtn = (TextView) getView().findViewById(R.id.cancel_btn);

		mBaiduBtn.setOnClickListener(this);
		mScenicBtn.setOnClickListener(this);

		mMapView = (MapView) getView().findViewById(R.id.home_map);
		mMapView.showScaleControl(false);
		mMapView.showZoomControls(false);

		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(14.0f));
		mBaiduMap.setOnMarkerClickListener(this);
		mBaiduMap.setOnMapStatusChangeListener(this);

		mLocationClient = ScenicApplication.getLocationClient();
		mLocationClient.registerLocationListener(this);
		mLocationClient.start();
	}

	@Override
	public void onStart() {
		super.onStart();
		String path = ScenicApplication.SEVER_RES_PATH + User.getTourIcon();
		if (!path.equals(uri)) {
			uri = path;
			if (User.getTourIcon().length() > 0) {
				ImageLoader.getInstance().displayImage(uri, mLogoIv);
			} else {
				mLogoIv.setImageResource(R.drawable.logo_normal);
			}
		}
		mLogoTv.setText(User.getUsername());
		sendRequest();
	}

	private void sendRequest() {
		String url = ScenicApplication.SEVER_PATH+ "scenicUser/getSignScenicInfo.htm";
		RequestParams params = new RequestParams();
		params.put("scenicID", User.getScenicid());
		if (HttpUtils.isNetworkConnected(getActivity())) {
			HttpUtils.post(url, params, new AsyncHttpResponseHandler() {
				@Override
				public void onFailure(Throwable arg0, String result) {
					showToast(R.string.get_data_error_prompt);
				}

				@Override
				public void onSuccess(int code, String result) {
					FileTools.writeLog("scenic.txt", result);
					
					if (HttpUtils.RESULTCODE_OK == code) {
						if (null != result){
							mBaiduMap.clear();
							try {
								JSONObject root = new JSONObject(result);
								if (Constants.SUCCESS.equals(root.optString("result"))) {
									JSONArray data = root.optJSONArray("records");
									if (data == null || data.length() == 0){
										FileTools.writeLog("scenic.txt", "没有景点数据");
										showToast(R.string.get_data_error_prompt);
									}
									for (int i = 0; i < data.length(); i++) {
										Attract attract = new Attract(data
												.optJSONObject(i));
										if (attract.type == Attract.SPOTMARKING) {
											if(attract.locations.size() > 0){
												Attract.Location l = attract.locations.get(0);
												LatLng latlng = new LatLng(l.lat, l.lon);
												OverlayOptions option = new MarkerOptions().position(latlng).icon(bitmap);
												Marker marker = (Marker)(mBaiduMap.addOverlay(option));
												Bundle bundle = new Bundle();
												bundle.putSerializable(Constants.MARKER_LOUCS_KEY,attract);
												marker.setExtraInfo(bundle);
											}
										} 
										else if (attract.type == Attract.REGIONMARKING){
											if (attract.locations.size() < 3) {
												continue;
											}
											ArrayList<LatLng> pts = new ArrayList<LatLng>();
											for (Attract.Location l : attract.locations) {
												pts.add(new LatLng(l.lat, l.lon));
											}
											
											// 构建用户绘制多边形的Option对象
											OverlayOptions polygonOption = new PolygonOptions().points(pts).stroke(new Stroke(5,0xAA00FF00)).fillColor(0xAAFFFF00);
											mBaiduMap.addOverlay(polygonOption);
										}
										else if (attract.type == Attract.PATHMARKING){
											if (attract.locations.size() < 2) {
												continue;
											}
											ArrayList<LatLng> pts = new ArrayList<LatLng>();
											for (Attract.Location l : attract.locations) {
												pts.add(new LatLng(l.lat, l.lon));
											}
											
											// 构建线行的Option对象
											OverlayOptions polylineOption = new PolylineOptions().points(pts).color(0xAAFF0033).width(10);
											mBaiduMap.addOverlay(polylineOption);
										}
									}
								} 
								else if(Constants.NOATTRACT.equals(root.optString("result"))){
									showToast(R.string.get_data_error_prompt);
								}
							} catch (JSONException e) {
								System.out.println(e);
								FileTools.writeLog("scenic.txt", "JSONException toString:"+e.toString());
								FileTools.writeLog("scenic.txt", "JSONException getMessage:"+e.getMessage());
								showToast(R.string.system_error_prompt);
							}
						} 
						else {
							showToast(R.string.network_error_try_again);
						}
					}		
				}
			});
		} else {
			showToast(R.string.network_no_error_prompt);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok_btn:
			startActivity(new Intent(getActivity(), BaiduMarKingActivity.class));
			break;
		case R.id.cancel_btn:
			startActivity(new Intent(getActivity(), ScenicMarKingActivity.class));
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		Attract attract = (Attract) marker.getExtraInfo().getSerializable(
				Constants.MARKER_LOUCS_KEY);
		if (attract == null)
			return false;

		LatLng ll = marker.getPosition();
		Point p = mBaiduMap.getProjection().toScreenLocation(ll);
		p.y -= 47;
		LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);

		TextView tv = new TextView(getActivity());
		tv.setBackgroundResource(R.drawable.popup);
		tv.setText(attract.name);
		tv.setTextColor(Color.WHITE);
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		tv.setPadding(17, 8, 17, 0);

		InfoWindow mInfoWindow = new InfoWindow(tv, llInfo, 0);
		mBaiduMap.showInfoWindow(mInfoWindow);
		return false;
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		try {
			// 设定中心点坐标
			LatLng cenpt = new LatLng(location.getLatitude(),
					location.getLongitude());
			// 定义地图状态
			MapStatus mMapStatus = new MapStatus.Builder().target(cenpt)
					.zoom(14).build();
			// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
			MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
					.newMapStatus(mMapStatus);
			// 改变地图状态̬
			mBaiduMap.setMapStatus(mMapStatusUpdate);
			mLocationClient.stop();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void showToast(int res) {
		Toast.makeText(getActivity(), res, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void onResume() {
		FileTools.writeLog("scenic.txt", "onResume");
		
		super.onResume();
		mMapView.onResume();
		//sendRequest();
	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	public void onMapStatusChange(MapStatus arg0) {
	}

	@Override
	public void onMapStatusChangeFinish(MapStatus arg0) {
	}

	@Override
	public void onMapStatusChangeStart(MapStatus arg0) {
		mBaiduMap.hideInfoWindow();
	}
}
