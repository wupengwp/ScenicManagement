package com.jiagu.management.activities;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.jiagu.management.utils.Md5;
import com.jiagu.management.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * @ClassName: EditPassActivity
 * @Description:修改密码
 * @author zz
 * @date 2015年1月15日 下午12:30:16
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class EditPassActivity extends VerificationCodeActivity implements
		OnClickListener, TextWatcher, OnCodeBtnClickListener {
	public static final int EDITPASS_SUCCESS = 1;

	private EditText mOldPassEt, mPhoneEt, mCodeEt, mNewPassEt, mRePassEt;
	private Button mOkBtn;

	private TextView field;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_pass);
		findView();
	}

	private void findView() {
		mOldPassEt = (EditText) findViewById(R.id.edit_old_pass_et);
		mPhoneEt = (EditText) findViewById(R.id.edit_buding_phone_et);
		mCodeEt = (EditText) findViewById(R.id.edit_user_code_et);
		mNewPassEt = (EditText) findViewById(R.id.edit_new_pass_et);
		mRePassEt = (EditText) findViewById(R.id.edit_new_repass_et);
		mOldPassEt.addTextChangedListener(this);
		mPhoneEt.addTextChangedListener(this);
		mCodeEt.addTextChangedListener(this);
		mNewPassEt.addTextChangedListener(this);
		mRePassEt.addTextChangedListener(this);
		mOkBtn = (Button) findViewById(R.id.ok_edit_pass);
		mOkBtn.setOnClickListener(this);

		setOnCodeBtnClickListener(this);
	}

	private void sendRequest(String phone, String code, String oldpass,
			String newpass) {
		showProgress();

		String url = ScenicApplication.SEVER_PATH + "scenicUser/updatePass.htm";
		RequestParams params = new RequestParams();
		params.put("userId", User.getScenicuserid());
		params.put("phone", phone);
		params.put("code", code);
		try {
			params.put("oldpass", Md5.En(oldpass));
			params.put("newpass", Md5.En(newpass));
		} catch (IOException e1) {
			System.err.println("MD5 Error :::: \n" + e1);
		}
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
							showToast(R.string.resetpass_success_prompt);
							ScenicApplication.getUserPreferences().edit()
									.clear().commit();
							setResult(EDITPASS_SUCCESS);
							finish();
						} else {
							showToast(R.string.resetpass_error_prompt);
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
		String phoneNumber = mPhoneEt.getText().toString();
		if (Utils.isNullOrEmpty(phoneNumber)) {
			focusOnError(mPhoneEt, R.string.phone_null_prompt);
			return;
		}
		if (!Utils.isMobileNO(phoneNumber)) {
			focusOnError(mPhoneEt, R.string.phone_error_prompt);
			return;
		}
		getVerificationCode(phoneNumber);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok_edit_pass:
			String oldpass = mOldPassEt.getText().toString();
			String phone = mPhoneEt.getText().toString();
			String code = mCodeEt.getText().toString();
			String newpass = mNewPassEt.getText().toString();
			String repass = mRePassEt.getText().toString();
			if (validateInput(oldpass, phone, code, newpass, repass))
				sendRequest(phone, code, oldpass, newpass);
			break;
		default:
			break;
		}
	}

	private void focusOnError(TextView field, int resourceId) {
		this.field = field;
		field.setBackgroundResource(R.drawable.view_edittext_error_border);
		field.requestFocus();
		field.setError(getString(resourceId));
	}

	private boolean validateInput(String oldpass, String phone, String code,
			String newpass, String repass) {
		if (Utils.isNullOrEmpty(oldpass)) {
			focusOnError(mOldPassEt, R.string.input_old_pass);
			return false;
		}
		if (Utils.isNullOrEmpty(phone)) {
			focusOnError(mPhoneEt, R.string.phone_null_prompt);
			return false;
		}
		if (!Utils.isMobileNO(phone)) {
			focusOnError(mPhoneEt, R.string.phone_error_prompt);
			return false;
		}
		if (Utils.isNullOrEmpty(code)) {
			focusOnError(mCodeEt, R.string.code_null_prompt);
			return false;
		}
		if (Utils.isNullOrEmpty(newpass)) {
			focusOnError(mNewPassEt, R.string.newpass_null_prompt);
			return false;
		}
		if (newpass.length() < Constants.PASSWORD_LEN_MIN) {
			focusOnError(mNewPassEt, R.string.password_length_prompt);
			return false;
		}
		if (Utils.isNullOrEmpty(repass)) {
			focusOnError(mRePassEt, R.string.repass_null_prompt);
			return false;
		}
		if (!newpass.equals(repass)) {
			focusOnError(mRePassEt, R.string.repass_error_prompt);
			return false;
		}
		return true;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (field != null)
			field.setBackgroundResource(R.drawable.view_edittext_normal_border);
	}

	@Override
	public void afterTextChanged(Editable s) {
	}
}
