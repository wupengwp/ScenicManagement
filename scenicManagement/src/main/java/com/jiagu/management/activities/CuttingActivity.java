package com.jiagu.management.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jiagu.management.R;
import com.jiagu.management.activities.bases.SuperActivity;
import com.jiagu.management.application.ScenicApplication;
import com.jiagu.management.https.HttpMultipartPost;
import com.jiagu.management.https.HttpMultipartPost.OnUploadExecutedListener;
import com.jiagu.management.models.User;
import com.jiagu.management.utils.Constants;
import com.jiagu.management.utils.FileTools;
import com.jiagu.management.widget.ClipImageView;

/**
 * @ClassName: CuttingActivity
 * @Description: 头像剪切
 * @author zz
 * @date 2015年1月15日 下午12:29:41
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class CuttingActivity extends SuperActivity implements OnClickListener,
		OnUploadExecutedListener {
	private TextView mCancelTv;
	private Button mOkBtn;
	private ClipImageView mImageView;

	private Bitmap mChooseBitmap;
	private Bitmap mHeadBitmap;

	private PopupWindow mMenu;
	private View mPicView;
	private ImageView mPicIv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cutting);
		
		//byte[] bis = getIntent().getByteArrayExtra("ChooseBitmap");
		//mChooseBitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);
		
		String filepath = getIntent().getStringExtra("ChooseBitmap");
		mChooseBitmap = BitmapFactory.decodeFile(filepath);
		
		initOkPopup();
		findView();
	}

	private void findView() {
		mCancelTv = (TextView) findViewById(R.id.logo_cancel_btn);
		mOkBtn = (Button) findViewById(R.id.logo_upload_btn);
		mImageView = (ClipImageView) findViewById(R.id.src_pic);
		mPicView = findViewById(R.id.dialog_picture_view);
		mPicIv = (ImageView) findViewById(R.id.current_choose_pic);

		mCancelTv.setOnClickListener(this);
		mOkBtn.setOnClickListener(this);
		mImageView.setImageBitmap(mChooseBitmap);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.logo_cancel_btn:
			onBackPressed();
			break;
		case R.id.logo_upload_btn:
			// 此处获取剪裁后的bitmap
			mHeadBitmap = mImageView.clip();

			if (!mMenu.isShowing()) {
				mPicIv.setImageBitmap(mHeadBitmap);
				mPicView.setVisibility(View.VISIBLE);
				mMenu.showAtLocation(getWindow().getDecorView(),
						Gravity.BOTTOM, 0, 0);
			}
			break;
		case R.id.popup_ok:
			sendUploadHead();
			break;
		default:
			break;
		}
	}

	public String saveHeadIcon(Bitmap bitmap) {
		String path = ScenicApplication.RootPath + "cache/";
		File folder = new File(path);
		try {
			if (!folder.exists()) {
				folder.mkdirs();
			}
			String filePath = path + "UserHead.jpg";
			FileOutputStream out = new FileOutputStream(filePath);
			bitmap.compress(CompressFormat.JPEG, 80, out);
			out.flush();
			out.close();
			return filePath;
		} catch (Exception e) {
		}
		return "";
	}

	private void sendUploadHead() {

		String url = ScenicApplication.SEVER_PATH + "scenicUser/updateIcon.htm";
		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("userId", User.getScenicuserid()));
		postData.add(new BasicNameValuePair("scenicID", User.getScenicid()));
		String filePath = saveHeadIcon(mHeadBitmap);
		HttpMultipartPost post = new HttpMultipartPost(this, url, filePath,
				postData, "icon");
		post.setOnUploadExecutedListener(this);
		post.execute();
	}

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

				if (mHeadBitmap != null && !mHeadBitmap.isRecycled()) {
					mHeadBitmap.recycle();
					mHeadBitmap = null;
				}
			}
		});
	}

	@Override
	public void OnUploadExecuted(String result) {
		
		//FileTools.writeLog("scenic.txt", result);
		System.out.println(result);
		try {
			JSONObject root = new JSONObject(result);
			if (Constants.SUCCESS.equals(root.optString("result"))) {
				String icon = root.optJSONArray("records").optJSONObject(0)
						.optString("icon");
				//showToast(icon);
				User.setTourIcon(icon);
				finish();
			} else {
				showToast(R.string.upload_map_head_prompt);
			}
		} catch (JSONException e) {
			System.out.println(e);
			showToast(R.string.upload_map_head_prompt);
		}
	}
}
