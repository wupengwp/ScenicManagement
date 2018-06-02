package com.jiagu.management.activities;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jiagu.management.R;
import com.jiagu.management.activities.bases.VerificationCodeActivity;
import com.jiagu.management.activities.bases.VerificationCodeActivity.OnCodeBtnClickListener;
import com.jiagu.management.application.ScenicApplication;
import com.jiagu.management.https.HttpUtils;
import com.jiagu.management.models.User;
import com.jiagu.management.utils.Constants;
import com.jiagu.management.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * @ClassName: EditPhoneActivity
 * @Description:绑定手机
 * @author zz
 * @date 2015年1月15日 下午12:30:27
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class EditPhoneActivity extends VerificationCodeActivity implements
		OnClickListener, OnCodeBtnClickListener {

	private EditText mOldPhone, mCode, mNewPhone;
	private Button mOkBtn, mCancelBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_phone);

		findView();
	}

	private void findView() {
		mTitle = (TextView) findViewById(R.id.title_bar_text);
		mTitle.setText(R.string.edit_phone);
		mLeftBtn = (TextView) findViewById(R.id.title_left_btn);
		mLeftBtn.setVisibility(View.VISIBLE);

		mOldPhone = (EditText) findViewById(R.id.current_binding_phone_et);
		mCode = (EditText) findViewById(R.id.current_code_et);
		mNewPhone = (EditText) findViewById(R.id.new_phone_et);
		mOkBtn = (Button) findViewById(R.id.ok_btn);
		mCancelBtn = (Button) findViewById(R.id.cancel_btn);

		mLeftBtn.setOnClickListener(this);
		mOkBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);
		super.setOnCodeBtnClickListener(this);
	}

	private void sendRequest(String code, final String newPhone) {
		showProgress();

		String url = ScenicApplication.SEVER_PATH
				+ "scenicUser/updatePhone.htm";
		RequestParams params = new RequestParams();
		params.put("user", User.getUser());
		params.put("code", code);
		params.put("phone", newPhone);
		params.put("userId", User.getScenicuserid());
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
							showToast(R.string.binding_phone_success_prompt);
							User.setMobilephone(newPhone);
							finish();
						} else {
							showToast(R.string.resetpass_error_phone);
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
	public void onCodeBtnClick() {
		String phoneNumber = mOldPhone.getText().toString();
		if (Utils.isNullOrEmpty(phoneNumber)) {
			focusOnError(mOldPhone, R.string.phone_null_prompt);
			return;
		}
		if (!Utils.isMobileNO(phoneNumber)) {
			focusOnError(mOldPhone, R.string.phone_error_prompt);
			return;
		}
		getVerificationCode(phoneNumber);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_left_btn:
		case R.id.cancel_btn:
			onBackPressed();
			break;
		case R.id.ok_btn:
			String oldPhone = mOldPhone.getText().toString();
			String code = mCode.getText().toString();
			String newPhone = mNewPhone.getText().toString();
			if (validateInput(oldPhone, code, newPhone))
				sendRequest(code, newPhone);
			break;
		default:
			break;
		}
	}

	private boolean validateInput(String oldPhone, String code, String newPhone) {
		if (Utils.isNullOrEmpty(oldPhone)) {
			focusOnError(mOldPhone, R.string.input_current_phone);
			return false;
		}
		if (!Utils.isMobileNO(oldPhone)) {
			focusOnError(mOldPhone, R.string.phone_error_prompt);
			return false;
		}
		if (Utils.isNullOrEmpty(code)) {
			focusOnError(mCode, R.string.code_null_prompt);
			return false;
		}
		if (Utils.isNullOrEmpty(newPhone)) {
			focusOnError(mNewPhone, R.string.input_new_phone);
			return false;
		}
		if (!Utils.isMobileNO(newPhone)) {
			focusOnError(mNewPhone, R.string.phone_error_prompt);
			return false;
		}
		return true;
	}

	private void focusOnError(TextView field, int resourceId) {
		field.requestFocus();
		field.setError(getString(resourceId));
	}
}
