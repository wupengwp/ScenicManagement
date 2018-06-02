package com.jiagu.management.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
import com.jiagu.management.widget.ImgMapView;
import com.jiagu.management.widget.ImgMapView.Coordinate;
import com.jiagu.management.widget.ImgMapView.OnMarkaClickListener;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * @ClassName: ScenicMarKingActivity
 * @Description: 景区地图标点和绑定光临
 * @author zz
 * @date 2015年1月15日 下午12:32:07
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class ScenicMarKingActivity extends SuperActivity implements
		OnClickListener, OnMarkaClickListener, OnItemClickListener,
		OnKeyListener {
	public static final int ACTIVITY_RESULTCODE = 200;

	private int windowWidth;
	private int windowheight;
	private int widthPixels = -1;
	private int heightPixels = -1;
	private double xOffset = 1;
	private double yOffset = 1;

	private DrawerLayout mDrawerLayout;
	private View mDrawerView;
	private ListView mDrawerList;
	private InfoAdapter mDrawerAdapter;
	private ImageButton mDrawerCloseBtn;
	private Button mDrawerSaveBtn, mDrawerDeleteBtn;
	private TextView mGetPoint, mBindInfo;
	private ImageButton mClearPointBtn;

	private ImgMapView mMapView;
	Bitmap bitmap;
	private CustomDialog.NoMapDialog mNoMapDialog;

	private SharedPreferences mPreferences;
	private int flg = 0;
	boolean isFileExists = false;
	
	SysMsgReceiver mSysMsgReceiver = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		FileTools.writeLog("scenic.txt", "ScenicMarKingActivity:onCreate");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scenic_marking);
		
		//初始化网络连接状态接受
		mSysMsgReceiver = new SysMsgReceiver();
		mSysMsgReceiver.createReceiver(this);
		
		//窗口控件初始化
		findView();
				
		//初始化图片视图
		initImageView();
	}
	
	protected void onDestory(){
		mSysMsgReceiver.destoryReceiver();
	}
	
	private void initImageView(){
		//获取窗口尺寸
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		windowWidth = dm.widthPixels;
		windowheight = dm.heightPixels;
		mPreferences = getSharedPreferences(Constants.SIZE_KEY, MODE_PRIVATE);
		
		//加载景区地图文件
		String filePath = ScenicApplication.RootPath + "map/"+ User.getScenicid() + ".jpg";
		File imageFile = new File(filePath);
		try{
			isFileExists = imageFile.exists();
		}catch (Exception e){
			isFileExists = false;
		}
		if (true == isFileExists) {
			//地图初始化参数设置
			widthPixels = mPreferences.getInt(Constants.WIDTHKEY, -1);
			heightPixels = mPreferences.getInt(Constants.HEIGHTKEY, -1);
			if (widthPixels != -1) {
				xOffset = widthPixels / windowWidth;
				yOffset = heightPixels / windowheight;

				float fwidthPixels = widthPixels;
				float fheightPixels = heightPixels;
				float fwindowWidth = windowWidth;
				float fwindowheight = windowheight;
				
				xOffset = fwidthPixels / fwindowWidth;
				yOffset = fheightPixels / fwindowheight;
				
				mMapView.setOffset(xOffset, yOffset);
			}
			
			//设置和显示景区地图，若网络连接正常，获取景区地图标点信息
			setMapImage(filePath);
			if (isNetConnected() == true){
				sendGetInfoRequest();
			}
			else{
				showToast(R.string.network_error_try_again);
			}
		} 
		else {
			//若网络连接正常，向服务器请求获取景区地图
			if (isNetConnected() == true){
				sendRequest();
			}
			else{
				showToast(R.string.network_error_try_again);
			}
		}
	}

	@Override
	protected void onStart() {
		//FileTools.writeLog("scenic.txt", "ScenicMarKingActivity onStart");
		super.onStart();
		if (flg != 0) {
			//网络连接正常，获取景区地图标点信息
			if (isNetConnected() == true){
				sendGetInfoRequest();
			}
			else{
				showToast(R.string.network_error_try_again);
			}
		}
		flg++;
	}
	
	@Override
	protected void onResume() {
		//FileTools.writeLog("scenic.txt", "ScenicMarKingActivity onResume");
		super.onResume();
		if (isFileExists==true && mMapView!=null){
			ImgMapView.calcCoordinate(mMapView);
		}
	}
	
	private boolean isNetConnected(){
		return mSysMsgReceiver.isNetConnected();
	}

	private void findView() {
		mTitle = (TextView) findViewById(R.id.title_bar_text);
		mTitle.setText(User.getScenicName());
		mLeftBtn = (TextView) findViewById(R.id.title_left_btn);
		mLeftBtn.setVisibility(View.VISIBLE);
		mRightBtn = (Button) findViewById(R.id.title_right_btn);
		mRightBtn.setText(R.string.show_scenic_spot_by_list);
		mRightBtn.setVisibility(View.VISIBLE);
		mLeftBtn.setOnClickListener(this);
		mRightBtn.setOnClickListener(this);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);// 关闭手势滑动
		mDrawerView = findViewById(R.id.left_drawer);
		mDrawerCloseBtn = (ImageButton) findViewById(R.id.left_close_btn);
		mDrawerSaveBtn = (Button) findViewById(R.id.left_save_btn);
		mDrawerDeleteBtn = (Button) findViewById(R.id.left_delete_btn);
		mDrawerSaveBtn.setOnClickListener(this);
		mDrawerCloseBtn.setOnClickListener(this);
		mDrawerDeleteBtn.setOnClickListener(this);
		mGetPoint = (TextView) findViewById(R.id.set_map_marka);
		mBindInfo = (TextView) findViewById(R.id.bind_map_marka);
		mClearPointBtn = (ImageButton) findViewById(R.id.clear_map_marka);
		mGetPoint.setOnClickListener(this);
		mBindInfo.setOnClickListener(this);
		mClearPointBtn.setOnClickListener(this);

		mDrawerList = (ListView) findViewById(R.id.left_scenic_spot_list);
		mDrawerAdapter = new InfoAdapter(this);
		mDrawerList.setAdapter(mDrawerAdapter);
		mDrawerList.setOnItemClickListener(this);

		mMapView = (ImgMapView) findViewById(R.id.scanic_map_view);
		mNoMapDialog = new CustomDialog.NoMapDialog(this);
		mNoMapDialog.setOnKeyListener(this);
	}

	// 获取景区地图
	private void sendRequest() {
		showProgress();

		String url = ScenicApplication.SEVER_PATH+ "scenicUser/getScenicMap.htm";
		RequestParams params = new RequestParams();
		params.put("scenicID", User.getScenicid());
		onPost(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(Throwable arg0, String result) {
				showToast(R.string.request_error_prompt);
				finish();
			}

			@Override
			public void onSuccess(int code, String result) {
				FileTools.writeLog("scenic.txt", "getScenicMap");
				FileTools.writeLog("scenic.txt", result);
				if (HttpUtils.RESULTCODE_OK == code) {
					System.out.println(result);
					try {
						JSONObject root = new JSONObject(result);
						if (Constants.SUCCESS.equals(root.optString("result"))) {
							JSONObject data = root.optJSONArray("records").optJSONObject(0);

							String w = data.optString("widthpixels");
							String h = data.optString("heightpixels");

							if (w != null && w.length() > 0) {
								widthPixels = Integer.parseInt(w);
								heightPixels = Integer.parseInt(h);
								SharedPreferences.Editor edit = mPreferences
										.edit();
								edit.putInt(Constants.WIDTHKEY, widthPixels);
								edit.putInt(Constants.HEIGHTKEY, heightPixels);

								float fwidthPixels = widthPixels;
								float fheightPixels = heightPixels;
								float fwindowWidth = windowWidth;
								float fwindowheight = windowheight;
								xOffset = fwidthPixels / fwindowWidth;
								yOffset = fheightPixels / fwindowheight;
				
								mMapView.setOffset(xOffset, yOffset);
							}
							final String mapUrl = ScenicApplication.SEVER_RES_PATH + data.optString("mapurl");
							if (mapUrl.length() > 0) {
								String path = ScenicApplication.RootPath + "map/";
								File folder = new File(path);
								if (!folder.exists()) {
									folder.mkdirs();
								}
								final String filePath = path + User.getScenicid() + ".jpg";
								new Thread(new Runnable() {
									@Override
									public void run() {
										writeFile(filePath, downFile(mapUrl));
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												setMapImage(filePath);
												hideProgress();
											}
										});
									}
								}).start();
							} else {
								mNoMapDialog.show();
							}
							if (isNetConnected() == true){
								sendGetInfoRequest();
							}
							else{
								showToast(R.string.network_error_try_again);
							}
						} else if (Constants.NULL.equals(root.optString("result"))){
							mNoMapDialog.show();
						}
						else{
							showToast(R.string.request_error_prompt);
							finish();
						}
					} catch (Exception e) {
						System.out.println(e);
						FileTools.writeLog("scenic.txt", e.toString());
						showToast(R.string.system_error_prompt);
						finish();
					}
				} 
				else{
					showToast(R.string.network_error_try_again);
					finish();
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_left_btn://返回按钮
			onBackPressed();
			break;
		case R.id.title_right_btn://查看景点列表
			Intent intent = new Intent(this, ScenicSpotActivity.class);
			intent.putExtra("UpActivity", 0);
			startActivity(intent);
			break;
		case R.id.left_close_btn://关闭弹出的景点绑定列表
			mDrawerLayout.closeDrawer(mDrawerView);
			mMapView.invalidate();
			break;
		case R.id.left_save_btn://绑定景点和景区图的标记
			if (isNetConnected() == true){
				if (chooseAttractId != null && chooseAttractId.length() > 0) {
					//发送绑定请求
					sendBindingRequest();
					mDrawerLayout.closeDrawer(mDrawerView);
					mMapView.invalidate();
				}
				else{
					showToast(R.string.bind_prompt_no_bind);
				}
			}
			else{
				showToast(R.string.network_error_try_again);
			}
			break;
		case R.id.left_delete_btn://删除景区图的标记，或者解除景点绑定景点
			if (chooseCoordinate.id == null || chooseCoordinate.id.length() == 0) {
				mMapView.deleteCoordinateByXY(chooseCoordinate.imgX, chooseCoordinate.imgY);
			} 
			else {
				if (isNetConnected() == true){
					mMapView.deleteCoordinateByXY(chooseCoordinate.imgX, chooseCoordinate.imgY);
					sendDeleteBindRequest(chooseCoordinate.id);//向服务器发送解除景点绑定景点请求
				}
				else
				{
					showToast(R.string.network_error_try_again);
				}
			}
			mDrawerLayout.closeDrawer(mDrawerView);
			mMapView.invalidate();
			break;
		case R.id.set_map_marka://使设置标点有效
			mGetPoint.setBackgroundResource(R.drawable.scenic_map_redio_selected);
			mBindInfo.setBackgroundResource(R.drawable.scenic_map_redio_normal);
			mMapView.setMarkaClickable(false);
			break;
		case R.id.bind_map_marka://设置绑定标点有效
			mBindInfo.setBackgroundResource(R.drawable.scenic_map_redio_selected);
			mGetPoint.setBackgroundResource(R.drawable.scenic_map_redio_normal);
			mMapView.setMarkaClickable(true);
			break;
		case R.id.clear_map_marka://清除未绑定的标点
			mMapView.clearUnbindCoordinates();
			break;
			
		default:
			mMapView.invalidate();
			break;
		}
	}

	public void setMapImage(String path) {
		bitmap = BitmapFactory.decodeFile(path);
		if (bitmap == null) {
			mNoMapDialog.show();
			return;
		}
		mMapView.setImageBitmap(bitmap);
		mMapView.setOnMarkaClickListener(this);
	}

	//被ImageView回调函数，设置当前被选中的标点
	@Override
	public void onMarkaClick(Coordinate coordinate) {
		FileTools.writeLog("scenic.txt", "onMarkaClick id :"+coordinate.id);
		chooseCoordinate = coordinate;
		mDrawerAdapter.chooseData(coordinate.id);
		mDrawerAdapter.notifyDataSetChanged();
		mDrawerLayout.openDrawer(mDrawerView);
	}

	//回调函数，设置景区地图
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == ACTIVITY_RESULTCODE) {
			String path = data.getStringExtra(Constants.IAMGE_PATH);
			if (path != null && path.length() > 0) {
				setMapImage(path);
				return;
			}
		}
		mNoMapDialog.show();
	}
	
	//景区信息列表适配器
	class InfoAdapter extends BaseAdapter {
		private LayoutInflater mLayoutInflater;//布局
		private ArrayList<Attract> mDataList;//存放服务器返回的景点列表

		public InfoAdapter(Context context) {
			mLayoutInflater = LayoutInflater.from(context);
			mDataList = new ArrayList<Attract>();
		}

		public void setData(ArrayList<Attract> mDataList) {
			this.mDataList.clear();
			this.mDataList.addAll(mDataList);
			this.notifyDataSetChanged();
		}

		//设置当前被点击的列表记录
		public void chooseData(String attractId) {
			if (attractId != null){
				for (int i = 0; i < mDataList.size(); i++) {
					if (mDataList.get(i).id.equals(attractId)) {
						mDataList.get(i).isChoose = true;
					} 
					else {
						mDataList.get(i).isChoose = false;
					}
				}
				this.notifyDataSetChanged();
			}
		}

		//设置景区信息的标点位置信息
		public void setImgXY(String attractId, Coordinate coordinate) {
			if (null != attractId){
				for (int i = 0; i < mDataList.size(); i++) {
					if (mDataList.get(i).id.equals(attractId)) {
						Attract attract = mDataList.get(i);
						attract.x = coordinate.imgX;
						attract.y = coordinate.imgY;
						break;
					}
				}	
			}
		}

		//设置景区信息的标点位置信息
		public void setImgXY(String attractId, float x, float y, boolean isBind, boolean isChoose) {
			if (null != attractId){
				for (int i = 0; i < mDataList.size(); i++) {
					if (mDataList.get(i).id.equals(attractId)) {
						Attract attract = mDataList.get(i);
						attract.x = x;
						attract.y = y;
						attract.isBind = isBind;
						attract.isChoose = isChoose;
						oldView = null;
						break;
					}
				}
			}
		}

		@Override
		public int getCount() {
			return mDataList.size();
		}

		@Override
		public Object getItem(int position) {
			for (int i=0; i<mDataList.size(); i++){
				mDataList.get(i).isChoose = false;
			}
			
			mDataList.get(position).isChoose = true;
			return mDataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh = null;
			if (convertView == null) {
				vh = new ViewHolder();
				convertView = mLayoutInflater.inflate(R.layout.item_choose_info, null);
				vh.id = (TextView) convertView.findViewById(R.id.scenic_spot_id);
				vh.name = (TextView) convertView.findViewById(R.id.scenic_spot_name);
				vh.choose = (ImageView) convertView.findViewById(R.id.scenic_spot_choose);
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}
			final Attract a = mDataList.get(position);
			vh.id.setText(a.id);
			vh.name.setText(a.name);
					
			convertView.setTag(R.id.tag_bind, a);
			
			//设置列表记录的背景和选中状态信息
			if (a.isChoose){
				convertView.setBackgroundColor(getResources().getColor(R.color.gary_transparent));
			}
			else{
				convertView.setBackgroundColor(getResources().getColor(R.color.white_transparent));
			}
			
			if (a.isBind) {
				vh.choose.setImageResource(R.drawable.icon_choose_selected);
			} 
			else {	
				vh.choose.setImageResource(R.drawable.icon_choose_normal);
			}

			return convertView;
		}

		class ViewHolder {
			TextView id, name;
			ImageView choose;
		}
	}

	public InputStream downFile(String url) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			return entity.getContent();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	//将景区地图写入文件
	public void writeFile(String filePath, InputStream is) {
		if (is != null){
			try {
				File file = new File(filePath);
				if (file.exists()) {
					final File to = new File(file.getAbsolutePath() + System.currentTimeMillis());
					file.renameTo(to);
					file.delete();
				}
				file.createNewFile();
				FileOutputStream fos = null;
				fos = new FileOutputStream(file);
				byte[] buffer = new byte[8192];
				int temp = 0;
				while ((temp = is.read(buffer)) > 0) {
					fos.write(buffer, 0, temp);
				}
				fos.close();
				is.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	//向服务器请求获取景区和景点标注信息
	private void sendGetInfoRequest() {
		FileTools.writeLog("scenic.txt", "excute sendGetInfoRequest");
		
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
				FileTools.writeLog("scenic.txt", "result:"+result);
				
				if (HttpUtils.RESULTCODE_OK == code) {
					System.out.println(result);
					try {
						if (bitmap != null) {
							double widthPixels = bitmap.getWidth();
							double heightPixels = bitmap.getHeight();
							double xOffset = widthPixels / windowWidth;
							double yOffset = heightPixels / windowheight;
							String xOf = xOffset + "";
							String yOf = yOffset + "";
							System.out.println(xOf + yOf);
						}
						JSONObject root = new JSONObject(result);
						if (Constants.SUCCESS.equals(root.optString("result"))) {
							JSONArray data = root.optJSONArray("records");
							ArrayList<Attract> mDataList = new ArrayList<Attract>();
							ArrayList<Coordinate> coordinates = new ArrayList<ImgMapView.Coordinate>();
							for (int i = 0; i < data.length(); i++) {
								Attract attract = new Attract(data.optJSONObject(i));
								
								//没有景区景点已在景区地图上标注
								if (attract.x!=-99999 && attract.y!=-99999){
									attract.isBind = true;
								}
								
								mDataList.add(attract);
								
								if (attract.x != -99999) {
									float x = (float) (attract.x * xOffset);
									float y = (float) (attract.y * yOffset);
									Coordinate coo = new Coordinate(x, y);
									coo.id = attract.id;
									coo.imgX = (float)attract.x;
									coo.imgY = (float)attract.y;
									coo.isBind = true;
									coordinates.add(coo);
								}
							}

							mDrawerAdapter.setData(mDataList);
							mMapView.setCoordinate(coordinates);
							
						} else if (Constants.NOATTRACT.equals(root.optString("result"))){
							showToast(R.string.get_data_error_prompt);
							mMapView.invalidate();//更新界面标记
						}
						else{
							showToast(R.string.get_data_error_prompt);
						}
					} catch (JSONException e) {
						System.out.println(e);
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

	private View oldView;
	private Coordinate chooseCoordinate;
	private String chooseAttractId;

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (chooseCoordinate.isBind){
			showToast(R.string.bind_prompt_is_bind);
			return;
		}
		
		Attract attract = (Attract)view.getTag(R.id.tag_bind);	
		if (oldView != null) {
			if (oldView.equals(view)) {
				oldView.setBackgroundColor(getResources().getColor(R.color.white_transparent));	
				if (attract.isBind == true){
					((ImageView) oldView.findViewById(R.id.scenic_spot_choose)).setImageResource(R.drawable.icon_choose_selected);
				}
				else {
					((ImageView) oldView.findViewById(R.id.scenic_spot_choose)).setImageResource(R.drawable.icon_choose_normal);
				}
				attract.isChoose = false;
				
				oldView = null;
			} 
			else {
				// 旧的view
				oldView.setBackgroundColor(getResources().getColor(R.color.white_transparent));
				Attract oldAttr = (Attract)oldView.getTag(R.id.tag_bind);
				if (oldAttr.isBind == true){
					((ImageView) oldView.findViewById(R.id.scenic_spot_choose)).setImageResource(R.drawable.icon_choose_selected);
				}
				else{
					((ImageView) oldView.findViewById(R.id.scenic_spot_choose)).setImageResource(R.drawable.icon_choose_normal);
				}
				oldAttr.isChoose = false;
				oldView = null;

				// 新view
				view.setBackgroundColor(getResources().getColor(R.color.gary_transparent));
				if (attract.isBind == true){
					((ImageView) view.findViewById(R.id.scenic_spot_choose)).setImageResource(R.drawable.icon_choose_selected);
				}
				else{
					((ImageView) view.findViewById(R.id.scenic_spot_choose)).setImageResource(R.drawable.icon_choose_normal);
				}
				
				attract.isChoose = true;
				oldView = view;
			}

		} 
		else {
			view.setBackgroundColor(getResources().getColor(R.color.gary_transparent));
			if (attract.isBind == true){
				((ImageView) view.findViewById(R.id.scenic_spot_choose)).setImageResource(R.drawable.icon_choose_selected);
			}
			else{
				((ImageView) view.findViewById(R.id.scenic_spot_choose)).setImageResource(R.drawable.icon_choose_normal);
			}
			
			attract.isChoose = true;
			oldView = view;
		}
		
		if (attract.isBind == true){
			chooseAttractId = null;
			showToast(R.string.bind_prompt_is_bind);
		}
		else 
		{
			if (attract.isChoose == true){
				chooseAttractId = ((Attract) mDrawerAdapter.getItem(position)).id;
			}
			else{
				chooseAttractId = null;
				showToast(R.string.bind_prompt_no_bind);
			}	
		}
	}

	private void sendBindingRequest() {
		showProgress();
		String url = ScenicApplication.SEVER_PATH + "scenicUser/bindAttrac.htm";
		RequestParams params = new RequestParams();
		params.put("scenicID", User.getScenicid());
		params.put("attractId", chooseAttractId);
		params.put("attractX", chooseCoordinate.imgX + "");
		params.put("attractY", chooseCoordinate.imgY + "");
		onPost(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(Throwable arg0, String result) {
				showToast(R.string.request_error_prompt);
				hideProgress();
			}

			@Override
			public void onSuccess(int code, String result) {
				FileTools.writeLog("scenic.txt", "bindAttrac");
				FileTools.writeLog("scenic.txt", result);
				
				if (HttpUtils.RESULTCODE_OK == code) {
					System.out.println(result);
					try {
						JSONObject root = new JSONObject(result);
						if (Constants.SUCCESS.equals(root.optString("result"))) {
							mDrawerAdapter.setImgXY(chooseAttractId, chooseCoordinate);
							mDrawerAdapter.notifyDataSetChanged();
							
							mMapView.setCoordinateByXY(chooseAttractId,chooseCoordinate.imgX,chooseCoordinate.imgY);
							showToast(R.string.binding_success_prompt);
							chooseAttractId = null;
							if (isNetConnected() == true){
								sendGetInfoRequest();
							}
							else
							{
								showToast(R.string.network_error_try_again);
							}
							
						} else {
							showToast(R.string.request_error_prompt);
						}
					} catch (JSONException e) {
						System.out.println(e);
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

	
	public void onDeleteAttract(String id){
		sendDeleteRequest(id);
	}
	/**
	 * 
	 * 删除绑定
	 * 
	 * @param id
	 */
	private void sendDeleteBindRequest(final String id) {
		showProgress();

		String url = ScenicApplication.SEVER_PATH + "scenicUser/deletebind.htm";
		RequestParams params = new RequestParams();
		params.put("scenicID", User.getScenicid());
		params.put("attractId", id);
		onPost(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(Throwable arg0, String result) {
				showToast(R.string.request_error_prompt);
				hideProgress();
			}

			@Override
			public void onSuccess(int code, String result) {
				if (HttpUtils.RESULTCODE_OK == code) {
					System.out.println(result);
					try {
						JSONObject root = new JSONObject(result);
						if (Constants.SUCCESS.equals(root.optString("result"))) {
							mDrawerAdapter.setImgXY(id, -99999, -99999, false, false);
							mDrawerAdapter.notifyDataSetChanged();
							mMapView.deleteCoordinateById(id);
							showToast(R.string.del_binding_success_prompt);
						} else {
							showToast(R.string.request_error_prompt);
						}
					} catch (JSONException e) {
						System.out.println(e);
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
	
	
	/*
	 * 景点删除
	 */
	private void sendDeleteRequest(final String attractId) {
		showProgress();

		String url = ScenicApplication.SEVER_PATH+ "scenicUser/delteAttrac.htm";
		RequestParams params = new RequestParams();
		params.put("scenicID", User.getScenicid());
		params.put("attractId", attractId);
		onPost(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(Throwable arg0, String result) {
				showToast(R.string.request_error_prompt);
				hideProgress();
			}

			@Override
			public void onSuccess(int code, String result) {
				if (HttpUtils.RESULTCODE_OK == code) {
					System.out.println(result);
					try {
						JSONObject root = new JSONObject(result);
						if (Constants.SUCCESS.equals(root
								.optString("result"))) {
							//deleteData(attractId);
							
							mMapView.deleteCoordinateById(attractId);
							if (isNetConnected() == true){
								sendGetInfoRequest();
							}
							else{
								showToast(R.string.network_error_try_again);
							}
							
							showToast(R.string.request_success_prompt);
						} else {
							showToast(R.string.request_error_prompt);
						}
					} catch (JSONException e) {
						System.out.println(e);
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


	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
			return true;
		}
		return false;
	}
}
