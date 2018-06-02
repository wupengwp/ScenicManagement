package com.jiagu.management.activities;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jiagu.management.R;
import com.jiagu.management.activities.bases.SuperActivity;
import com.jiagu.management.application.ScenicApplication;
import com.jiagu.management.https.HttpUtils;
import com.jiagu.management.models.User;
import com.jiagu.management.utils.Constants;
import com.jiagu.management.utils.FileTools;
import com.jiagu.management.utils.Md5;
import com.jiagu.management.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * @ClassName: LoginActivity
 * @Description:登陆
 * @author zz
 * @date 2015年1月15日 下午12:31:22
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class LoginActivity extends SuperActivity implements OnClickListener,
		TextWatcher {

	private EditText mAccountEt, mPasswordEt;
	private Button mLoginBtn;
	private TextView mFotgetTvBtn;

	private TextView field;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		findView();
	}

	private void findView() {
		mAccountEt = (EditText) findViewById(R.id.login_user_name_et);
		mPasswordEt = (EditText) findViewById(R.id.login_user_pass_et);
		mAccountEt.addTextChangedListener(this);
		mPasswordEt.addTextChangedListener(this);
		mLoginBtn = (Button) findViewById(R.id.login_btn);
		mFotgetTvBtn = (TextView) findViewById(R.id.forget_tv_btn);
		mLoginBtn.setOnClickListener(this);
		mFotgetTvBtn.setOnClickListener(this);
	}

	private void sendRequest(String account, String password) {
		FileTools.writeLog("scenic.txt", "LoginActivity sendRequest");
		
		showProgress();

		String url = ScenicApplication.SEVER_PATH + "scenicUser/login.htm";
		RequestParams params = new RequestParams();
		params.put("user", account);
		try {
			params.put("pass", Md5.En(password));
		} catch (IOException e1) {
			System.err.println("MD5 Error :::: \n" + e1);
		}
		params.put("os", Constants.OS);
		System.out.println(params);
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
							JSONObject userinfo = root.optJSONArray("records").optJSONObject(0);
							User.phonenumber = userinfo.optString(User.PHONENUMBER_KEY);
							User.photourl = userinfo.optString(User.PHOTOURL_KEY);
							User.tourGrade = userinfo.optString(User.TOURGRADE_KEY);
							User.idcard = userinfo.optString(User.IDCARD_KEY);
							User.lastlogininfo = userinfo.optString(User.LASTLOGININFO_KEY);
							User.pass = userinfo.optString(User.PASS_KEY);
							User.id = userinfo.optString(User.ID_KEY);
							User.unit = userinfo.optString(User.UNIT_KEY);
							User.mobilephone = userinfo.optString(User.MOBILEPHONE_KEY);
							User.adress = userinfo.optString(User.ADRESS_KEY);
							User.username = userinfo.optString(User.USERNAME_KEY);
							User.area = userinfo.optString(User.AREA_KEY);
							User.email = userinfo.optString(User.EMAIL_KEY);
							User.tourIcon = userinfo.optString(User.TOURICON_KEY);
							User.scenicid = userinfo.optString(User.SCENICID_KEY);
							User.scenicName = userinfo.optString(User.SCENICNAME_KEY);
							User.scenicuserid = userinfo.optString(User.SCENICUSERID_KEY);
							User.user = userinfo.optString(User.USER_KEY);
							User.save();

							startActivity(new Intent(LoginActivity.this, MainActivity.class));
							finish();
						} else {
							showToast(R.string.login_error_prompt);
						}
					} catch (JSONException e) {
						System.out.println(e);
						FileTools.writeLog("scenic.txt", "JSONException toString:"+e.toString());
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
		case R.id.login_btn:
			String account = mAccountEt.getText().toString();
			String password = mPasswordEt.getText().toString();
			if (validateInput(account, password))
				sendRequest(account, password);
			break;
		case R.id.forget_tv_btn:
			startActivity(new Intent(this, ForgetPassActivity.class));
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

	private boolean validateInput(String account, String password) {
		if (Utils.isNullOrEmpty(account)) {
			focusOnError(mAccountEt, R.string.account_null_prompt);
			return false;
		}
		if (Utils.isNullOrEmpty(password)) {
			focusOnError(mPasswordEt, R.string.password_null_prompt);
			return false;
		}
		if (password.length() < Constants.PASSWORD_LEN_MIN) {
			focusOnError(mPasswordEt, R.string.password_length_prompt);
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
