package com.jiagu.management.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.jiagu.management.R;
import com.jiagu.management.fragments.HelpHomeFragment;
import com.jiagu.management.fragments.HelpHomeFragment.OnHelpItemClickListener;
import com.jiagu.management.fragments.HelpPagerFragment;
import com.jiagu.management.fragments.HelpPagerFragment.OnLastPagerClickListener;

/**
 * @ClassName: HelpActivity
 * @Description:帮助中心
 * @author zz
 * @date 2015年1月15日 下午12:31:12
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */

public class HelpActivity extends FragmentActivity implements OnClickListener,
		OnHelpItemClickListener, OnLastPagerClickListener {
	private TextView mTitle, mLeftBtn;

	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	private HelpHomeFragment mHomeFragment;
	private HelpPagerFragment mPagerFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		mFragmentManager = getSupportFragmentManager();
		findView();
	}

	private void findView() {
		mTitle = (TextView) findViewById(R.id.title_bar_text);
		mTitle.setText(R.string.soft_info);
		mLeftBtn = (TextView) findViewById(R.id.title_left_btn);
		mLeftBtn.setVisibility(View.VISIBLE);
		mLeftBtn.setOnClickListener(this);

		mHomeFragment = (HelpHomeFragment) mFragmentManager
				.findFragmentById(R.id.fragement_home);
		mHomeFragment.setOnHelpItemClickListener(this);
		mPagerFragment = (HelpPagerFragment) mFragmentManager
				.findFragmentById(R.id.fragement_pager);
		mPagerFragment.setOnLastPagerClickListener(this);
		mFragmentTransaction = mFragmentManager.beginTransaction()
				.hide(mHomeFragment).hide(mPagerFragment);
		mFragmentTransaction.show(mHomeFragment).commit();
	}

	@Override
	public void onClick(View v) {
		onBackPressed();
	}

	@Override
	public void OnHelpItemClick(int viewid) {
		mFragmentTransaction = mFragmentManager.beginTransaction()
				.hide(mHomeFragment).hide(mPagerFragment);
		switch (viewid) {
		case R.id.help_get_pass: // 密码找回
			mPagerFragment.setImageResources(R.drawable.pic_get_pass_1,
					R.drawable.pic_get_pass_2);
			break;
		case R.id.help_mraking: // 标注景点
			mPagerFragment.setImageResources(R.drawable.pic_new_1,
					R.drawable.pic_new_2);
			break;
		case R.id.help_binding: // 绑定景点
			mPagerFragment.setImageResources(R.drawable.pic_binding_1,
					R.drawable.pic_binding_2);
			break;
		case R.id.help_upload: // 图片上传
			mPagerFragment.setImageResources(R.drawable.pic_upload_1,
					R.drawable.pic_upload_2);
			break;
		default:
			break;
		}
		mFragmentTransaction.show(mPagerFragment).commit();
	}

	@Override
	public void OnLastPagerClick() {
		mFragmentTransaction = mFragmentManager.beginTransaction()
				.hide(mHomeFragment).hide(mPagerFragment);
		mFragmentTransaction.show(mHomeFragment).commit();
	}
}
