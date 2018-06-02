package com.jiagu.management.fragments;

import com.jiagu.management.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

/**
 * @ClassName: HelpHomeFragment
 * @Description:帮助页面
 * @author zz
 * @date 2015年1月15日 下午12:33:46
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class HelpHomeFragment extends Fragment implements OnClickListener {

	private RelativeLayout mGetPass, mMraking, mBinding, mUpload;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_help_home, container, false);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mGetPass = (RelativeLayout) getView().findViewById(R.id.help_get_pass);
		mMraking = (RelativeLayout) getView().findViewById(R.id.help_mraking);
		mBinding = (RelativeLayout) getView().findViewById(R.id.help_binding);
		mUpload = (RelativeLayout) getView().findViewById(R.id.help_upload);

		mGetPass.setOnClickListener(this);
		mMraking.setOnClickListener(this);
		mBinding.setOnClickListener(this);
		mUpload.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (listener == null) {
			throw new NullPointerException(
					"HelpHomeFragment OnHelpItemClickListener is null! You must implement OnHelpItemClickListener");
		}
		listener.OnHelpItemClick(v.getId());
	}

	private OnHelpItemClickListener listener;

	public void setOnHelpItemClickListener(OnHelpItemClickListener listener) {
		this.listener = listener;
	}

	public interface OnHelpItemClickListener {
		public void OnHelpItemClick(int viewid);
	};
}
