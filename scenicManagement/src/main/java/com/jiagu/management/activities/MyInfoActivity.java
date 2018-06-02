package com.jiagu.management.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jiagu.management.R;
import com.jiagu.management.activities.bases.SuperActivity;
import com.jiagu.management.application.ScenicApplication;
import com.jiagu.management.models.User;
import com.jiagu.management.utils.FileTools;
import com.jiagu.management.utils.ImageTools;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @ClassName: MyInfoActivity
 * @Description: 个人中心
 * @author zz
 * @date 2015年1月15日 下午12:31:45
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class MyInfoActivity extends SuperActivity implements OnClickListener {

	private static final int TAKE_PICTURE = 0;
	private static final int CHOOSE_PICTURE = 1;

	private DisplayMetrics dm;

	private ImageView mLogoIv;
	private RelativeLayout mNickBtn, mPhoneBtn;
	private TextView mUsernameTv, mUserLevelTv, mLastLoginTv;
	private TextView mNickTv, mEmailTv, mPhoneTv;
	private PopupWindow mMenu;

	private String uri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_logo);
		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		initMenu();
		findView();
	}

	@Override
	protected void onStart() {
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
		mUsernameTv.setText(getString(R.string.account) + "："
				+ User.getUsername());
		mNickTv.setText(User.getUsername());
		mUserLevelTv.setText(getString(R.string.user_rating)
				+ User.getTourGrade());
		mLastLoginTv.setText(getString(R.string.last_login_info)
				+ User.getLastlogininfo());
	}

	private void initMenu() {
		View view = LayoutInflater.from(this).inflate(
				R.layout.popup_change_logo, null);
		mMenu = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mMenu.setAnimationStyle(R.style.pop_anim_style);
		mMenu.setFocusable(true);
		mMenu.setTouchable(true);
		mMenu.setOutsideTouchable(true);
		view.setFocusableInTouchMode(true);

		view.findViewById(R.id.popup_photograph).setOnClickListener(this);
		view.findViewById(R.id.popup_choose_pic).setOnClickListener(this);
		view.findViewById(R.id.popup_cancel).setOnClickListener(this);
	}

	private void findView() {
		mTitle = (TextView) findViewById(R.id.title_bar_text);
		mTitle.setText(R.string.my_info);
		mLeftBtn = (TextView) findViewById(R.id.title_left_btn);
		mLeftBtn.setVisibility(View.VISIBLE);

		findViewById(R.id.rigth_btn_img).setVisibility(View.INVISIBLE);
		mLogoIv = (ImageView) findViewById(R.id.personal_logo_img);
		mUsernameTv = (TextView) findViewById(R.id.personal_username_tv);
		mUserLevelTv = (TextView) findViewById(R.id.personal_level_tv);
		mLastLoginTv = (TextView) findViewById(R.id.personal_info_tv);

		mNickTv = (TextView) findViewById(R.id.show_nick_tv);
		mEmailTv = (TextView) findViewById(R.id.show_email_tv);
		mPhoneTv = (TextView) findViewById(R.id.show_phone_tv);
		mNickTv.setText(User.getUsername());
		mEmailTv.setText(User.getEmail());
		mPhoneTv.setText(User.getMobilephone());

		mNickBtn = (RelativeLayout) findViewById(R.id.setting_nick_btn);
		mPhoneBtn = (RelativeLayout) findViewById(R.id.setting_phone_btn);

		mLeftBtn.setOnClickListener(this);
		mLogoIv.setOnClickListener(this);
		mNickBtn.setOnClickListener(this);
		mPhoneBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_left_btn:
			onBackPressed();
			break;
		case R.id.personal_logo_img:
			if (!mMenu.isShowing()) {
				mTitle.setText(R.string.change_logo);
				mMenu.showAtLocation(getWindow().getDecorView(),
						Gravity.BOTTOM, 0, 0);
			}
			break;
		case R.id.popup_photograph:
			mMenu.dismiss();
			Intent openCameraIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);
			Uri imageUri = Uri.fromFile(new File(Environment
					.getExternalStorageDirectory(), "image.jpg"));
			// 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
			openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			startActivityForResult(openCameraIntent, TAKE_PICTURE);
			break;
		case R.id.popup_choose_pic:
			mMenu.dismiss();
			Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
			openAlbumIntent.setType("image/*");
			startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
			break;
		case R.id.popup_cancel:
			if (mMenu != null && mMenu.isShowing()) {
				mTitle.setText(R.string.my_info);
				mMenu.dismiss();
			}
			break;
		case R.id.setting_nick_btn:
			startActivity(new Intent(this, EditNameActivity.class));
			break;
		case R.id.setting_phone_btn:
			startActivity(new Intent(this, EditPhoneActivity.class));
			break;
		default:
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Bitmap chooseBitmap = null;
			switch (requestCode) {
			case TAKE_PICTURE:
				Bitmap camera = BitmapFactory.decodeFile(Environment
						.getExternalStorageDirectory() + "/image.jpg");
				int[] cameraSize = ImageTools.calcViewSize(dm,
						camera.getWidth() / 3, camera.getHeight() / 3);
				chooseBitmap = ImageTools.zoomBitmap(camera, cameraSize[0],
						cameraSize[1]);
				camera.recycle();
				camera = null;
				break;
			case CHOOSE_PICTURE:
				ContentResolver resolver = getContentResolver();
				Uri originalUri = data.getData(); // 照片的原始资源地址
				try {
					Bitmap photo = MediaStore.Images.Media.getBitmap(resolver,
							originalUri);
					int[] photoSize = ImageTools.calcViewSize(dm,
							photo.getWidth() / 3, photo.getHeight() / 3);
					chooseBitmap = ImageTools.zoomBitmap(photo, photoSize[0],
							photoSize[1]);
					photo.recycle();
					photo = null;
				} catch (Exception e) {
					System.out.println(e);
					showToast(R.string.system_error_prompt);
				}
				break;
			}
			if (chooseBitmap != null) {
//				ByteArrayOutputStream baos = new ByteArrayOutputStream();
//				chooseBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//				byte[] bitmapByte = baos.toByteArray();
//				Intent intent = new Intent(this, CuttingActivity.class);
//				intent.putExtra("ChooseBitmap", bitmapByte);
//				startActivity(intent);				
				
				String portraitFilePath = Environment.getExternalStorageDirectory()+"/portrait.jpg";
				try{
					FileOutputStream fos = new java.io.FileOutputStream(portraitFilePath);
					chooseBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
					fos.close();
					
					Intent intent = new Intent(this, CuttingActivity.class);
					intent.putExtra("ChooseBitmap", portraitFilePath);
					startActivity(intent);
				}
				catch (Exception e){
					FileTools.writeLog("scenic.txt", e.toString());
				}
			}
		}
	}
}
