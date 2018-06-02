package com.jiagu.management.activities;

import java.io.FileOutputStream;

import com.jiagu.management.R;
import com.jiagu.management.utils.FileTools;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * @ClassName: MainActivity
 * @Description:首页
 * @author zz
 * @date 2015年1月15日 下午12:31:33
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class MainActivity extends FragmentActivity implements OnClickListener {

	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	private Fragment mMainFragment, mPersonalFragment, mSettingFragment;

	private RelativeLayout mHomeBtn;
	private ImageButton mMenuBtn;
	private LinearLayout mMenuPanel, mPersonal, mSetting;

	private int oldClickId = R.id.home_panel_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mFragmentManager = getSupportFragmentManager();
		findView();
		
		////////////////////////////////////////////////////////////////
//		
//		try{
//			Bitmap chooseBitmap = BitmapFactory.decodeFile("/mnt/sdcard/ScenicManagement/map/S208b3c6670f94be49de1ac83eca412e4.jpg");
//			
//			FileOutputStream fos = new java.io.FileOutputStream("/mnt/sdcard/compress2.jpg");
//			chooseBitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
//			fos.close();
//		}
//		catch (Exception e){
//			FileTools.writeLog("scenic.txt", e.toString());
//		}
	}

	private void findView() {
		mHomeBtn = (RelativeLayout) findViewById(R.id.home_panel_btn);
		mMenuBtn = (ImageButton) findViewById(R.id.main_bottom_menu);
		mMenuPanel = (LinearLayout) findViewById(R.id.bottom_menu_panel);
		mPersonal = (LinearLayout) findViewById(R.id.menu_personal_btn);
		mSetting = (LinearLayout) findViewById(R.id.menu_setting_btn);
		mHomeBtn.setOnClickListener(this);
		mPersonal.setOnClickListener(this);
		mSetting.setOnClickListener(this);

		mMenuBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMenuPanel.getVisibility() == View.GONE) {
					mMenuPanel.setVisibility(View.VISIBLE);
					mMenuBtn.setImageResource(R.drawable.btn_bottom_menu_p);
				} else {
					mMenuPanel.setVisibility(View.GONE);
					mMenuBtn.setImageResource(R.drawable.btn_bottom_menu_n);
				}
			}
		});

		mMainFragment = mFragmentManager.findFragmentById(R.id.fragement_main);
		mPersonalFragment = mFragmentManager
				.findFragmentById(R.id.fragement_personal_center);
		mSettingFragment = mFragmentManager
				.findFragmentById(R.id.fragement_settings_center);
		mFragmentTransaction = mFragmentManager.beginTransaction()
				.hide(mMainFragment).hide(mPersonalFragment)
				.hide(mSettingFragment);
		mFragmentTransaction.show(mMainFragment).commit();
	}

	@Override
	public void onClick(View v) {
		if (oldClickId == v.getId()) {
			return;
		}
		mFragmentTransaction = mFragmentManager.beginTransaction()
				.hide(mMainFragment).hide(mPersonalFragment)
				.hide(mSettingFragment);
		switch (v.getId()) {
		case R.id.home_panel_btn:
			mFragmentTransaction.show(mMainFragment).commit();
			break;
		case R.id.menu_personal_btn:
			mFragmentTransaction.show(mPersonalFragment).commit();
			break;
		case R.id.menu_setting_btn:
			mFragmentTransaction.show(mSettingFragment).commit();
			break;
		default:
			break;
		}
		oldClickId = v.getId();
		mMenuPanel.setVisibility(View.GONE);
		mMenuBtn.setImageResource(R.drawable.btn_bottom_menu_n);
	}

	private boolean isExit = false;

	private void exit() {
		if (!isExit) {
			Toast.makeText(this, R.string.prompt_to_exit, Toast.LENGTH_SHORT)
					.show();
			isExit = true;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					isExit = false;
				}
			}, 5000);
		} else {
			finish();
			System.exit(0);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			exit();
		}
		return false;
	}
}
