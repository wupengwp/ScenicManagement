package com.jiagu.management.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.jiagu.management.R;
import com.jiagu.management.activities.ChoosePicActivity;
import com.jiagu.management.activities.EditPassActivity;
import com.jiagu.management.activities.FeedbackActivity;
import com.jiagu.management.activities.LoginActivity;
import com.jiagu.management.activities.MyInfoActivity;
import com.jiagu.management.application.ScenicApplication;
import com.jiagu.management.models.User;
import com.jiagu.management.utils.FileTools;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @ClassName: PersonalFragment
 * @Description: 个人中心
 * @author zz
 * @date 2015年1月15日 下午12:34:27
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class PersonalFragment extends Fragment implements OnClickListener {

	private ImageView mLogoIv;
	private TextView mUsernameTv, mUserLevelTv, mLastLoginTv;
	private Button mMapManage, /*mFeedback,*/ mEditPass, mUnLogin;
	private RelativeLayout mInfoSetting;

	private String uri;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_personal, container, false);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mInfoSetting = (RelativeLayout) getView().findViewById(
				R.id.setting_my_info);
		mLogoIv = (ImageView) getView().findViewById(R.id.personal_logo_img);
		mUsernameTv = (TextView) getView().findViewById(
				R.id.personal_username_tv);
		mUserLevelTv = (TextView) getView()
				.findViewById(R.id.personal_level_tv);
		mLastLoginTv = (TextView) getView().findViewById(R.id.personal_info_tv);

		mMapManage = (Button) getView()
				.findViewById(R.id.scenic_map_manage_btn);
		//mFeedback = (Button) getView().findViewById(R.id.feedback_btn);
		mEditPass = (Button) getView().findViewById(R.id.edit_user_pass_btn);
		mUnLogin = (Button) getView().findViewById(R.id.unlogin_btn);

		mInfoSetting.setOnClickListener(this);
		mMapManage.setOnClickListener(this);
		//mFeedback.setOnClickListener(this);
		mEditPass.setOnClickListener(this);
		mUnLogin.setOnClickListener(this);
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
		mUsernameTv.setText(getString(R.string.account) + "："
				+ User.getUsername());
		mUserLevelTv.setText(getString(R.string.user_rating)
				+ User.getTourGrade());
		mLastLoginTv.setText(getString(R.string.last_login_info)
				+ User.getLastlogininfo());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_my_info:
			startActivity(new Intent(getActivity(), MyInfoActivity.class));
			break;
		case R.id.scenic_map_manage_btn:
			startActivity(new Intent(getActivity(), ChoosePicActivity.class));
			break;
//		case R.id.feedback_btn:
//			startActivity(new Intent(getActivity(), FeedbackActivity.class));
//			break;
		case R.id.edit_user_pass_btn:
			startActivityForResult(new Intent(getActivity(),
					EditPassActivity.class), 0);
			break;
		case R.id.unlogin_btn:
			ScenicApplication.getUserPreferences().edit().clear().commit();
			startActivity(new Intent(getActivity(), LoginActivity.class));
			getActivity().finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == EditPassActivity.EDITPASS_SUCCESS) {
			startActivity(new Intent(getActivity(), LoginActivity.class));
			getActivity().finish();
		}
	}
}
