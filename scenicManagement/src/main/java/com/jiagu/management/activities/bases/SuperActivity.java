package com.jiagu.management.activities.bases;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jiagu.management.R;
import com.jiagu.management.https.HttpUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/** 
* @ClassName: SuperActivity 
* @Description: 
* @author zz
* @date 2015年1月15日 下午12:33:06 
* @version 1
* @company 西安甲骨企业文化传播有限公司  
*/  
public class SuperActivity extends Activity {

	protected TextView mTitle, mLeftBtn;
	protected Button mRightBtn;

	private View mProgressView;

	@Override
	public void onContentChanged() {
		mProgressView = findViewById(android.R.id.progress);
		super.onContentChanged();
	}

	protected void showProgress() {
		if (mProgressView != null)
			mProgressView.setVisibility(View.VISIBLE);
	}

	protected void hideProgress() {
		if (mProgressView != null)
			mProgressView.setVisibility(View.GONE);
	}

	protected void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	protected void showToast(int res) {
		Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
	}

	protected void onPost(String url, RequestParams params,
			AsyncHttpResponseHandler res) {
		if (HttpUtils.isNetworkConnected(this)) {
			HttpUtils.post(url, params, res);
		} else {
			hideProgress();
			showToast(R.string.network_no_error_prompt);
		}
	}
}
