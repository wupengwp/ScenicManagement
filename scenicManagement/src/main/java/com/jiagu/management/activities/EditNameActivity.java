package com.jiagu.management.activities;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jiagu.management.R;
import com.jiagu.management.activities.bases.SuperActivity;
import com.jiagu.management.application.ScenicApplication;
import com.jiagu.management.https.HttpUtils;
import com.jiagu.management.models.User;
import com.jiagu.management.utils.Constants;
import com.jiagu.management.utils.FileTools;
import com.jiagu.management.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * @ClassName: EditNameActivity
 * @Description: 用户名
 * @author zz
 * @date 2015年1月15日 下午12:29:52
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class EditNameActivity extends SuperActivity implements OnClickListener,
		TextWatcher {

	private EditText mNameEt;
	private ImageButton mClearBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_name);
		findView();
	}

	private void findView() {
		mTitle = (TextView) findViewById(R.id.title_bar_text);
		mTitle.setText(R.string.edit_nick);
		mLeftBtn = (TextView) findViewById(R.id.title_left_btn);
		mLeftBtn.setVisibility(View.VISIBLE);
		mRightBtn = (Button) findViewById(R.id.title_right_btn);
		mRightBtn.setVisibility(View.VISIBLE);

		mNameEt = (EditText) findViewById(R.id.edit_user_name_et);
		mNameEt.setText(User.getUsername());
		mNameEt.addTextChangedListener(this);
		mClearBtn = (ImageButton) findViewById(R.id.edit_user_name_clear_btn);

		if (mNameEt.getText().toString().length() > 0) {
			mClearBtn.setVisibility(View.VISIBLE);
		} else {
			mClearBtn.setVisibility(View.INVISIBLE);
		}

		mLeftBtn.setOnClickListener(this);
		mRightBtn.setOnClickListener(this);
		mClearBtn.setOnClickListener(this);
	}

	private void sendRequest(final String name) {
		showProgress();

		String url = ScenicApplication.SEVER_PATH
				+ "scenicUser/updateLoginName.htm";
		RequestParams params = new RequestParams();
		params.put("user", User.getUser());
		params.put("usernick", name);
		params.put("userId", User.getScenicuserid());
		onPost(url, params, new AsyncHttpResponseHandler() {
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
						if (Constants.SUCCESS.equals(root.optString("result"))) {
							User.setUsername(name);
							finish();
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_left_btn:
			onBackPressed();
			break;
		case R.id.title_right_btn:
			String name = mNameEt.getText().toString();
			if (validateInput(name))
				sendRequest(name);
			break;
		case R.id.edit_user_name_clear_btn:
			mNameEt.requestFocus();
			mNameEt.setText("");
			break;
		default:
			break;
		}
	}

	// 判断昵称
	private boolean validateInput(String name) {
		if (Utils.isNullOrEmpty(name)) {
			focusOnError(mNameEt, R.string.input_nick);
			return false;
		}
		if (User.getUsername().equals(name)) {
			focusOnError(mNameEt, R.string.submit_edit_nick_error);
			return false;
		}
		return true;
	}

	private void focusOnError(TextView field, int resourceId) {
		field.requestFocus();
		field.setError(getString(resourceId));
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (mNameEt.getText().toString().length() > 0) {
			if (mNameEt.getText().toString().length() >= 12) {
				showToast("您的昵称太长，请重新输入");
			}
			mClearBtn.setVisibility(View.VISIBLE);
		} else {
			mClearBtn.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
	}
}
