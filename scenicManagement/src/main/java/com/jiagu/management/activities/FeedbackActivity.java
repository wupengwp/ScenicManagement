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
import com.jiagu.management.activities.bases.SuperActivity;
import com.jiagu.management.application.ScenicApplication;
import com.jiagu.management.https.HttpUtils;
import com.jiagu.management.models.User;
import com.jiagu.management.utils.Constants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * @ClassName: FeedbackActivity
 * @Description:意见反馈
 * @author zz
 * @date 2015年1月15日 下午12:30:37
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class FeedbackActivity extends SuperActivity implements OnClickListener {

	private EditText mContent;
	private Button mOkBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);
		findView();
	}

	private void findView() {
		mTitle = (TextView) findViewById(R.id.title_bar_text);
		mTitle.setText(R.string.feedback);
		mLeftBtn = (TextView) findViewById(R.id.title_left_btn);
		mLeftBtn.setVisibility(View.VISIBLE);

		mContent = (EditText) findViewById(R.id.feedback_content);
		mOkBtn = (Button) findViewById(R.id.submit_feed_btn);

		mLeftBtn.setOnClickListener(this);
		mOkBtn.setOnClickListener(this);
	}

	private void sendRequest(String content) {
		showProgress();

		String url = ScenicApplication.SEVER_PATH + "scenicUser/userAdvice.htm";
		RequestParams params = new RequestParams();
		params.put("user", User.getUser());
		params.put("content", content);
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
							showToast(R.string.feedback_success_prompt);
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
		case R.id.submit_feed_btn:
			String content = mContent.getText().toString();
			if (content == null || content.trim().length() == 0) {
				showToast(R.string.feedback_none_prompt);
				return;
			}
			sendRequest(content);
			break;
		default:
			break;
		}
	}
}
