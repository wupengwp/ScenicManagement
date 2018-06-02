package com.jiagu.management.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jiagu.management.R;
import com.jiagu.management.activities.bases.SuperActivity;
import com.jiagu.management.application.ScenicApplication;
import com.jiagu.management.https.HttpMultipartPost;
import com.jiagu.management.https.HttpMultipartPost.OnUploadExecutedListener;
import com.jiagu.management.models.User;
import com.jiagu.management.utils.Constants;
import com.jiagu.management.utils.CustomDialog;

/**
 * @ClassName: FileActivity
 * @Description: 文件浏览
 * @author zz
 * @date 2015年1月15日 下午12:30:48
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */

public class FileActivity extends SuperActivity implements OnClickListener,
		OnItemClickListener, OnUploadExecutedListener {
	private static final String FILE = "file";
	private static final String FOLDER = "folder";

	private ImageButton mBack;
	private TextView mPath;
	private ListView mFileList;
	private FileAdapter mFileAdapter;

	private String currentpath;

	private PopupWindow mMenu;
	private View mPicView;
	private ImageView mPicIv;
	private Bitmap currentBitmap;

	private String mImagePath;

	private int windowWidth = -1;
	private int windowHeight = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		windowWidth = dm.widthPixels;
		windowHeight = dm.heightPixels;
		mFileAdapter = new FileAdapter(this);
		findView();
		initOkPopup();
		findFile(Environment.getExternalStorageDirectory());
	}

	private void findView() {
		mTitle = (TextView) findViewById(R.id.title_bar_text);
		mTitle.setText(R.string.choose_floder);
		mLeftBtn = (TextView) findViewById(R.id.title_left_btn);
		mLeftBtn.setVisibility(View.VISIBLE);
		mLeftBtn.setOnClickListener(this);

		mBack = (ImageButton) findViewById(R.id.file_back);
		mBack.setOnClickListener(this);
		mPath = (TextView) findViewById(R.id.current_path);
		mPicView = findViewById(R.id.dialog_picture_view);
		mPicIv = (ImageView) findViewById(R.id.current_choose_pic);
		mFileList = (ListView) findViewById(R.id.file_list);
		mFileList.setAdapter(mFileAdapter);
		mFileList.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_left_btn:
			onBackPressed();
			break;
		case R.id.file_back:
			if (!currentpath.equals(File.separator + "mnt")) {
				String path = currentpath.substring(0,
						currentpath.lastIndexOf(File.separator) + 1);
				findFile(new File(path));
			} else {
				showToast(R.string.file_back_error);
			}
			break;
		case R.id.popup_ok:

			final CustomDialog.GeneralDialog dialog = new CustomDialog.GeneralDialog(
					this);
			dialog.setMessage("重复上传景区图会导致之前绑定的数据丢失");
			dialog.hideMiddleButton();
			dialog.setOnLeftButtonClickListener(getString(R.string.dialog_ok),
					null, new OnClickListener() {
						@Override
						public void onClick(View v) {
							List<NameValuePair> postData = new ArrayList<NameValuePair>();
							postData.add(new BasicNameValuePair("scenicID",
									User.getScenicid()));
							postData.add(new BasicNameValuePair("widthPixels",
									windowWidth + ""));
							postData.add(new BasicNameValuePair("heightPixels",
									windowHeight + ""));

							HttpMultipartPost post = new HttpMultipartPost(
									FileActivity.this,
									ScenicApplication.SEVER_PATH + "upload.htm",
									mImagePath, postData, "scenicMap");
							post.setOnUploadExecutedListener(FileActivity.this);
							post.execute();
							dialog.dismiss();
						}
					});
			dialog.setOnRightButtonClickListener(
					getString(R.string.dialog_cancel), null,
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
			dialog.show();
			break;
		default:
			break;
		}
	}

	@Override
	public void OnUploadExecuted(String result) {
		try {
			JSONObject root = new JSONObject(result);
			if (Constants.SUCCESS.equals(root.optString("result"))) {
				String filePath = ScenicApplication.RootPath + "map/"
						+ User.getScenicid() + ".jpg";
				File file = new File(filePath);
				if (file.exists()) {
					file.delete();
				}

				Intent data = new Intent();
				data.putExtra(Constants.IAMGE_PATH, mImagePath);
				setResult(ChoosePicActivity.ACTIVITY_RESULTCODE, data);
				onBackPressed();
			} else {
				showToast(R.string.upload_map_failure_prompt);
			}
		} catch (JSONException e) {
			System.out.println(e);
			showToast(R.string.upload_map_failure_prompt);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		HashMap<String, String> map = (HashMap<String, String>) mFileAdapter
				.getItem(position);
		if (map.get(Constants.FILE_TYPE).equals(FOLDER)) {
			String path = currentpath + File.separator
					+ map.get(Constants.FILE_NAME);
			findFile(new File(path));
		} else {
			mImagePath = currentpath + File.separator
					+ map.get(Constants.FILE_NAME);
			currentBitmap = BitmapFactory.decodeFile(mImagePath);
			if (!mMenu.isShowing()) {
				mPicIv.setImageBitmap(currentBitmap);
				mPicView.setVisibility(View.VISIBLE);
				mMenu.showAtLocation(getWindow().getDecorView(),
						Gravity.BOTTOM, 0, 0);
			}
		}
	}

	/*
	 * 查找图片文件
	 */
	private void findFile(File file) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			showProgress();

			File[] files = file.listFiles();
			currentpath = file.getPath();
			mPath.setText(currentpath);
			ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
			for (File f : files) {
				if (f.isFile()) {
					if (!f.getPath().endsWith(".jpg")
							&& !f.getPath().endsWith(".png")
							&& !f.getPath().endsWith(".bmp")
							&& !f.getPath().endsWith(".jpeg")) {
						continue;
					}
				}
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(Constants.FILE_NAME, f.getName());
				if (f.isFile()) {
					map.put(Constants.FILE_TYPE, FILE);
					map.put(Constants.FILE_ICON, R.drawable.icon_pic + "");
				} else {
					map.put(Constants.FILE_TYPE, FOLDER);
					map.put(Constants.FILE_ICON, R.drawable.icon_folder + "");
				}
				data.add(map);
			}
			mFileAdapter.setDataAndRefresh(data);
			hideProgress();
		} else {
			showToast(R.string.no_sdcard);
		}
	}

	// 图片上传
	private void initOkPopup() {
		View view = LayoutInflater.from(this).inflate(R.layout.popup_ok, null);
		mMenu = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mMenu.setAnimationStyle(R.style.pop_anim_style);
		mMenu.setFocusable(true);
		mMenu.setTouchable(true);
		mMenu.setBackgroundDrawable(new BitmapDrawable());
		mMenu.setOutsideTouchable(true);
		view.setFocusableInTouchMode(true);
		view.findViewById(R.id.popup_ok).setOnClickListener(this);

		mMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				mPicView.setVisibility(View.GONE);

				if (currentBitmap != null && !currentBitmap.isRecycled()) {
					currentBitmap.recycle();
					currentBitmap = null;
				}
			}
		});
	}

	/**
	 * @author Administrator 文件列表适配器
	 */
	
	class FileAdapter extends BaseAdapter {
		private LayoutInflater mLayoutInflater;
		private ArrayList<HashMap<String, String>> data;

		public FileAdapter(Context context) {
			mLayoutInflater = LayoutInflater.from(context);
			data = new ArrayList<HashMap<String, String>>();
		}

		public void setDataAndRefresh(ArrayList<HashMap<String, String>> data) {
			this.data.clear();
			this.data.addAll(data);
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
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
				convertView = mLayoutInflater.inflate(R.layout.item_file, null);
				vh.icon = (ImageView) convertView
						.findViewById(R.id.item_file_icon);
				vh.name = (TextView) convertView
						.findViewById(R.id.item_file_name);
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}
			HashMap<String, String> map = data.get(position);
			vh.name.setText(map.get(Constants.FILE_NAME));
			int res = Integer.parseInt(map.get(Constants.FILE_ICON));
			vh.icon.setImageResource(res);
			return convertView;
		}

		class ViewHolder {
			ImageView icon;
			TextView name;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!currentpath.equals(File.separator + "mnt")) {
				String path = currentpath.substring(0,
						currentpath.lastIndexOf(File.separator) + 1);
				findFile(new File(path));
			} else {
				onBackPressed();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
