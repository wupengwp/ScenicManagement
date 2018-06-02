package com.jiagu.management.activities.bases;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.jiagu.management.R;
import com.jiagu.management.application.ScenicApplication;
import com.jiagu.management.https.HttpUtils;
import com.jiagu.management.utils.Constants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/** 
* @ClassName: VerificationCodeActivity 
* @Description: 
* @author zz
* @date 2015年1月15日 下午12:33:10 
* @version 1
* @company 西安甲骨企业文化传播有限公司  
*/  
public class VerificationCodeActivity extends SuperActivity {
	private boolean isGetCode;
	private int second = Constants.WAITING_TIME;
	private Button mCodeBtn;

	@Override
	public void onContentChanged() {
		mCodeBtn = (Button) findViewById(R.id.getcode_btn);
		mCodeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCodeBtnClickListener == null) {
					throw new NullPointerException(
							"You must implements OnCodeBtnClickListener and set!");
				}
				mCodeBtnClickListener.onCodeBtnClick();
			}
		});
		super.onContentChanged();
	}

	protected void getVerificationCode(String phone) {
		mCodeBtn.setBackgroundResource(R.drawable.btn_getcode_bg_perssed);
		mCodeBtn.setText(getString(R.string.wait_code) +  "(…)");
		mCodeBtn.setClickable(false);
		mCodeBtn.setEnabled(false);

		String url = ScenicApplication.SEVER_PATH + "scenicUser/getCode.htm";
		RequestParams params = new RequestParams();
		params.put("phone", phone);
		onPost(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(Throwable arg0, String arg1) {
				showToast(R.string.get_code_error_prompt);
				mCodeBtn.setBackgroundResource(R.drawable.btn_getcode_selector);
				mCodeBtn.setText(R.string.get_code);
				mCodeBtn.setClickable(true);
				mCodeBtn.setEnabled(true);
			}

			@Override
			public void onSuccess(int code, String result) {
				if (HttpUtils.RESULTCODE_OK == code) {
					System.out.println(result);
					try {
						JSONObject root = new JSONObject(result);
						if (Constants.SUCCESS.equals(root.optString("result"))) {
							isGetCode = true;
							mCodeBtn.setText(getString(R.string.wait_code)
									+ "(" + second + ")");
							//getCodeing.start();
							new GetCodeThread().start();
						} else {
							showToast(R.string.get_code_error_prompt);
							mCodeBtn.setBackgroundResource(R.drawable.btn_getcode_selector);
							mCodeBtn.setText(R.string.get_code);
							mCodeBtn.setClickable(true);
							mCodeBtn.setEnabled(true);
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
					mCodeBtn.setBackgroundResource(R.drawable.btn_getcode_selector);
					mCodeBtn.setText(R.string.get_code);
					mCodeBtn.setClickable(true);
					mCodeBtn.setEnabled(true);
				}
			}
		});
	}

	Handler getCodeHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.GETCODING:
				if (isGetCode) {
					second--;
					mCodeBtn.setText(getString(R.string.wait_code) + "("
							+ second + ")");
				}
				break;
			case Constants.GETCODED:
				second = Constants.WAITING_TIME;
				isGetCode = false;
				mCodeBtn.setClickable(true);
				mCodeBtn.setEnabled(true);
				mCodeBtn.setBackgroundResource(R.drawable.btn_getcode_selector);
				mCodeBtn.setText(R.string.get_code);
				break;
			default:
				break;
			}
		};
	};

	class GetCodeThread extends Thread{
		@Override
		public void run() {
			Message message;
			while (isGetCode) {
				message = new Message();
				message.what = Constants.GETCODING;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (second == 0) {
					message.what = Constants.GETCODED;
				}
				getCodeHandler.sendMessage(message);
			}
		}
	}
//	Thread getCodeing = new Thread(new Runnable() {
//		
//	});

	private OnCodeBtnClickListener mCodeBtnClickListener;

	protected void setOnCodeBtnClickListener(OnCodeBtnClickListener listener) {
		this.mCodeBtnClickListener = listener;
	}

	public interface OnCodeBtnClickListener {
		public void onCodeBtnClick();
	}
}
