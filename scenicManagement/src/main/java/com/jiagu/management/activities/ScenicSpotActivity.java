package com.jiagu.management.activities;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jiagu.management.R;
import com.jiagu.management.activities.bases.SuperActivity;
import com.jiagu.management.application.ScenicApplication;
import com.jiagu.management.https.HttpUtils;
import com.jiagu.management.models.Attract;
import com.jiagu.management.models.User;
import com.jiagu.management.utils.Constants;
import com.jiagu.management.utils.FileTools;
import com.jiagu.management.widget.CircleImageView;
import com.jiagu.management.widget.ImgMapView;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @ClassName: ScenicSpotActivity
 * @Description: 景点列表
 * @author zz
 * @date 2015年1月15日 下午12:32:17
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class ScenicSpotActivity extends SuperActivity implements
		OnClickListener, TextWatcher {

	public static final int EDITINFO = 0;
	public static final int RESETINFO = 1;
	private ListView mScenicSpotLv;
	private ScenicSpotAdapter mAdapter;
	private EditText mSreachEt;
	private ArrayList<Attract> mDataList = new ArrayList<Attract>();
	private int upActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spot_list);
		upActivity = getIntent().getIntExtra("UpActivity", -1);
		
		findView();
		sendGetInfoRequest();
	}

	private void findView() {
		mTitle = (TextView) findViewById(R.id.title_bar_text);
		mTitle.setText(User.getScenicName());
		mLeftBtn = (TextView) findViewById(R.id.title_left_btn);
		mLeftBtn.setVisibility(View.VISIBLE);
		mLeftBtn.setOnClickListener(this);

		View view = LayoutInflater.from(this).inflate(R.layout.view_search,null);
		mSreachEt = (EditText) view.findViewById(R.id.edit_search_et);
		mSreachEt.setHint(R.string.attract_search_tip);
		mSreachEt.addTextChangedListener(this);

		mScenicSpotLv = (ListView) findViewById(R.id.scenic_spot_list);
		mScenicSpotLv.addHeaderView(view);
		mAdapter = new ScenicSpotAdapter(this);
		mScenicSpotLv.setAdapter(mAdapter);
	}

	// 获取景点信息
	private void sendGetInfoRequest() {
		showProgress();

		String url = ScenicApplication.SEVER_PATH
				+ "scenicUser/getSignScenicInfo.htm";
		RequestParams params = new RequestParams();
		params.put("scenicID", User.getScenicid());
		onPost(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(Throwable arg0, String result) {
				// showToast(R.string.get_data_error_prompt);
				hideProgress();
			}

			@Override
			public void onSuccess(int code, String result) {
				FileTools.writeLog("scenic.txt", result);
				if (HttpUtils.RESULTCODE_OK == code) {
					System.out.println(result);
					try {
						JSONObject root = new JSONObject(result);
						if (Constants.SUCCESS.equals(root.optString("result"))) {
							JSONArray data = root.optJSONArray("records");

							for (int i = 0; i < data.length(); i++) {
								Attract attract = new Attract(data
										.optJSONObject(i));
								mDataList.add(attract);
							}
							mAdapter.addDataAndRef(mDataList);
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_left_btn:
			onBackPressed();
			break;

		default:
			break;
		}
	}

	/**
	 * @author Administrator
	 *
	 */
	class ScenicSpotAdapter extends BaseAdapter {

		private ArrayList<Attract> mDataList;
		private LayoutInflater mLayoutInflater;
		private HashMap<Integer, View> map;

		public ScenicSpotAdapter(Context context) {
			mLayoutInflater = LayoutInflater.from(context);
			mDataList = new ArrayList<Attract>();
			map = new HashMap<Integer, View>();
		}

		public void addDataAndRef(ArrayList<Attract> mDataList) {
			this.mDataList.clear();
			this.mDataList.addAll(mDataList);
			this.notifyDataSetChanged();
		}

		public void deleteData(String attractId) {	
			FileTools.writeLog("scenic.txt", "invoke deleteData starttime list size="+mDataList.size());
			for (int i = 0; i < mDataList.size(); i++) {				
				if (mDataList.get(i).id.equals(attractId)) {				
					float x = (float)mDataList.get(i).x;
					float y = (float)mDataList.get(i).y;
					
					mDataList.remove(i);
					ScenicSpotActivity.this.mDataList.remove(i);
					ImgMapView.deleteCoordinateByXYFromList(x, y);				
					break;
				}
			}
			
			FileTools.writeLog("scenic.txt", "invoke deleteData endtime list size="+mDataList.size());
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mDataList.size();
		}

		@Override
		public Object getItem(int position) {
			return mDataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder vh;
			View view;
			if (map.get(position) == null) {
				vh = new ViewHolder();
				view = mLayoutInflater.inflate(R.layout.item_scenic_spot, null);
				vh.logo = (CircleImageView) view
						.findViewById(R.id.item_scenic_spot_logo);
				vh.name = (TextView) view
						.findViewById(R.id.item_scenic_spot_name);
				vh.type = (TextView) view
						.findViewById(R.id.item_scenic_spot_marking);
				vh.content = (TextView) view
						.findViewById(R.id.item_scenic_spot_location);
				vh.status = (TextView) view
						.findViewById(R.id.item_scenic_spot_binding_status);
				vh.date = (TextView) view
						.findViewById(R.id.item_scenic_spot_time);
				vh.edit = (TextView) view
						.findViewById(R.id.item_list_edit_info);
				vh.again = (TextView) view.findViewById(R.id.item_list_re_info);
				vh.show = (ImageButton) view
						.findViewById(R.id.item_show_delete_btn);
				vh.hide = (ImageButton) view
						.findViewById(R.id.item_hide_delete_btn);
				vh.mask = (LinearLayout) view.findViewById(R.id.item_mask);
				vh.delete = (TextView) view
						.findViewById(R.id.item_scenic_spot_delete_btn);

				view.setTag(vh);
				map.put(position, view);
			} else {
				view = map.get(position);
				vh = (ViewHolder) view.getTag();
			}
			
			String uri = ScenicApplication.SEVER_RES_PATH + User.getTourIcon();
			if (User.getTourIcon().length() > 0) {
				ImageLoader.getInstance().displayImage(uri, vh.logo);
			}
			
			final Attract a = mDataList.get(position);
			vh.mask.setVisibility(View.GONE);
			vh.name.setText(getString(R.string.scenic_spot_name) + "   "
					+ a.name);

			String type = null;
			switch (a.type){
			case Attract.SPOTMARKING:
				type =getString(R.string.marking_type_spot);
				break;
			case Attract.REGIONMARKING:
				type =getString(R.string.marking_type_region);
				break;
			case Attract.PATHMARKING:
				type =getString(R.string.marking_type_path);
				break;
			default:
				type = "";	
			}
			vh.type.setText(getString(R.string.tagging_methods) + "  "+ type);

			vh.content.setText(a.getInfoLocations());
			vh.date.setText(getString(R.string.upload_date) + "   " + a.date);
			vh.status.setText(getString(R.string.tagging_binding_status)
					+ "   "
					+ getString((a.x == -99999 ? R.string.unbind
							: R.string.binded)));

			vh.show.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					vh.mask.setVisibility(View.VISIBLE);
				}
			});


			vh.hide.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					vh.mask.setVisibility(View.GONE);
				}
			});
			
			vh.mask.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					vh.mask.setVisibility(View.GONE);
				}
			});
			
			vh.delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog(a.id);

				}

			});
			
			//标注的景点信息进行修改
			vh.edit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					FileTools.writeLog("scenic.txt", "edit onClick");
					
					if (upActivity == -1) {
						FileTools.writeLog("scenic.txt", "setResult(BaiduMarKingActivity.RESULT_CODE, intent)");
						
						Intent intent = new Intent();
						intent.putExtra("Operation", EDITINFO);
						intent.putExtra("Attract", a);
						intent.putExtra("name", a.name);
						setResult(BaiduMarKingActivity.RESULT_CODE, intent);
						
						FileTools.writeLog("scenic.txt", "编辑 invoke setResult");
						
						finish();
					} 
					else {
						FileTools.writeLog("scenic.txt", "new Intent(ScenicSpotActivity.this, BaiduMarKingActivity.class)");
						Intent intent = new Intent(ScenicSpotActivity.this, BaiduMarKingActivity.class);
						intent.putExtra("UpActivity", 0);
						intent.putExtra("Operation", EDITINFO);
						intent.putExtra("Attract", a);
						startActivity(intent);
					}
				}
			});
			
			//标注的景点重新标注
			vh.again.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					FileTools.writeLog("scenic.txt", "again onClick");
					
					if (upActivity == -1) {
						//FileTools.writeLog("scenic.txt", "重新标注 invoke setResult");
						
						Intent intent = new Intent();
						intent.putExtra("Operation", RESETINFO);
						intent.putExtra("Attract", a);
						setResult(BaiduMarKingActivity.RESULT_CODE, intent);
						finish();
					} else {
						//FileTools.writeLog("scenic.txt", "重新标注 invoke startActivity");
						
						Intent intent = new Intent(ScenicSpotActivity.this, BaiduMarKingActivity.class);
						intent.putExtra("UpActivity", 0);
						intent.putExtra("Operation", RESETINFO);
						intent.putExtra("Attract", a);
						startActivity(intent);
					}
				}
			});

			return view;
		}

		// 删除框
		View layout;

		private void dialog(final String id) {
			// TODO Auto-generated method stub
			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.popu_sendchsenic,
					(ViewGroup) findViewById(R.id.dialog));
			final Dialog dialog = new Dialog(ScenicSpotActivity.this,
					R.style.CustomDialog);
			dialog.setContentView(layout);
			final TextView btsuccess = (TextView) layout
					.findViewById(R.id.btn_canlic);
			final TextView btn_sumber = (TextView) layout
					.findViewById(R.id.btn_sumber);
			
			btsuccess.setText(R.string.dialog_ahead);
			btn_sumber.setText(R.string.dialog_cancel);	
			
			btsuccess.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					sendDeleteRequest(id);
					dialog.dismiss();
				}
			});
			btn_sumber.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();

				}
			});

			dialog.show();
		}

		class ViewHolder {
			CircleImageView logo;
			TextView name, type, content, date, status;
			TextView edit, again;
			ImageButton show, hide;
			LinearLayout mask;
			TextView delete;
		}

		/*
		 * 景点删除
		 */
		private void sendDeleteRequest(final String attractId) {
			showProgress();

			String url = ScenicApplication.SEVER_PATH
					+ "scenicUser/delteAttrac.htm";
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
							if (Constants.SUCCESS.equals(root.optString("result"))) {
								deleteData(attractId);
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
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		ArrayList<Attract> searchResult = new ArrayList<Attract>();
		//FileTools.writeLog("scenic.txt", "invoke afterTextChanged list size="+mDataList.size());
		
		for (Attract a : mDataList) {
			if (a.name.contains(mSreachEt.getText().toString())) {
				searchResult.add(a);
			}
		}
		mAdapter.addDataAndRef(searchResult);
	}
}
