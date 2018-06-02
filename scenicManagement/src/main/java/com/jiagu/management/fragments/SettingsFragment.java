package com.jiagu.management.fragments;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.jiagu.management.R;
import com.jiagu.management.activities.FeedbackActivity;
import com.jiagu.management.activities.HelpActivity;
import com.jiagu.management.activities.SoftwareActivity;
import com.jiagu.management.application.ScenicApplication;
import com.jiagu.management.https.DownloadService;
import com.jiagu.management.https.HttpUtils;
import com.jiagu.management.utils.Constants;
import com.jiagu.management.utils.CustomDialog;
import com.jiagu.management.utils.FileTools;
import com.jiagu.management.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * @ClassName: SettingsFragment
 * @Description: 设置中心
 * @author zz
 * @date 2015年1月15日 下午12:34:14
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class SettingsFragment extends Fragment implements OnClickListener {
	private View mProgressView;
	private TextView mTitleTv;

	private TextView mSofeBtn, mHelpBtn, mBackBtn;
	private RelativeLayout mVersionBtn;
	private TextView mVersionTv;

	private CustomDialog.GeneralDialog mAlertDialog;
	private String newVersionUrl;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Intent intent = new Intent(getActivity(), DownloadService.class);
		getActivity().startService(intent);
		return inflater.inflate(R.layout.fragment_settings, container, false);
	}

	@SuppressLint("CutPasteId")
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mProgressView = getView().findViewById(android.R.id.progress);
		mTitleTv = (TextView) getView().findViewById(R.id.title_bar_text);
		mTitleTv.setText(R.string.setting);

		mSofeBtn = (TextView) getView().findViewById(R.id.soft_function_btn);
		mHelpBtn = (TextView) getView().findViewById(R.id.settings_help_btn);
		mBackBtn = (TextView) getView().findViewById(R.id.system_feedback_btn);
		mVersionBtn = (RelativeLayout) getView().findViewById(
				R.id.settings_verson_btn);
		mSofeBtn.setOnClickListener(this);
		mHelpBtn.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
		mVersionBtn.setOnClickListener(this);

		mVersionTv = (TextView) getView().findViewById(R.id.show_verson_tv);
		mVersionTv.setText(Utils.getVersionName(getActivity()));

		mAlertDialog = new CustomDialog.GeneralDialog(getActivity());
		mAlertDialog.setMessage(getString(R.string.new_version_prompt));
		mAlertDialog.hideMiddleButton();
		mAlertDialog.setOnLeftButtonClickListener(
				getString(R.string.dialog_ok), null, new OnClickListener() {
					@Override
					public void onClick(View v) {
						downUpdateApk(newVersionUrl);
					}
				});
		mAlertDialog.setOnRightButtonClickListener(
				getString(R.string.dialog_cancel), null, new OnClickListener() {
					@Override
					public void onClick(View v) {
						mAlertDialog.dismiss();
					}
				});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.soft_function_btn:
			startActivity(new Intent(getActivity(), SoftwareActivity.class));
			break;
		case R.id.settings_help_btn:
			startActivity(new Intent(getActivity(), HelpActivity.class));
			break;
		case R.id.system_feedback_btn:
			startActivity(new Intent(getActivity(), FeedbackActivity.class));
			break;
		case R.id.settings_verson_btn:
			sendRequest();
			break;
		default:
			break;
		}
	}

	/*
	 * 版本查询
	 */
	private void sendRequest() {
		showProgress();
		String url = ScenicApplication.SEVER_PATH + "scenicUser/getVersion.htm";
		RequestParams params = new RequestParams();
		params.put("type", Constants.APK);
		params.put("version", Utils.getVersionName(getActivity()));
		
		FileTools.writeLog("scenic.txt", "Version:"+Utils.getVersionName(getActivity()));
		
		params.put("os", Constants.OS);
		if (HttpUtils.isNetworkConnected(getActivity())) {
			HttpUtils.post(url, params, new AsyncHttpResponseHandler() {
				@Override
				public void onFailure(Throwable arg0, String result) {
					showToast(R.string.request_error_prompt);
					hideProgress();
				}

				@Override
				public void onSuccess(int code, String result) {
					FileTools.writeLog("scenic.txt", result);
					
					if (HttpUtils.RESULTCODE_OK == code) {
						System.out.println(result);
						try {
							JSONObject root = new JSONObject(result);
							if (Constants.SUCCESS.equals(root
									.optString("result"))) {
								String url = root.optJSONArray("records")
										.optJSONObject(0).optString("url");
								if (url != null && url.length() > 0) {
									newVersionUrl = ScenicApplication.SEVER_APK_PATH
											+ url;
									Log.i("111", newVersionUrl);
									mAlertDialog.show();
								} else {
									showToast(R.string.no_new_version);
								}
							} else {
								showToast(R.string.request_error_prompt);
							}
						} catch (JSONException e) {
							System.out.println(e);
							FileTools.writeLog("scenic.txt", "JSONException toString:"+e.toString());
							FileTools.writeLog("scenic.txt", "JSONException getMessage:"+e.getMessage());
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
		} else {
			showToast(R.string.network_no_error_prompt);
		}
	}

	/*
	 * 下载apk
	 */
	private void downUpdateApk(final String url) {

		final String savePath = ScenicApplication.RootPath + "APP/";
		File folder = new File(savePath);
		if (!folder.exists()) {
			folder.mkdir();
		}
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (url != null || !url.equals("")) {
					DownloadService.downNewFile(url, 351,
							getString(R.string.app_name), savePath);
					mAlertDialog.dismiss();
				}
			}
		});
	}

	private void showToast(int res) {
		Toast.makeText(getActivity(), res, Toast.LENGTH_SHORT).show();
	}

	protected void showProgress() {
		if (mProgressView != null)
			mProgressView.setVisibility(View.VISIBLE);
	}

	protected void hideProgress() {
		if (mProgressView != null)
			mProgressView.setVisibility(View.GONE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent(getActivity(), DownloadService.class);
		getActivity().stopService(intent);
	}
}
