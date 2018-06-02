package com.jiagu.management.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.jiagu.management.R;
import com.jiagu.management.utils.Constants;

/**
 * @ClassName: ChoosePicActivity
 * @Description: 图片选择
 * @author zz
 * @date 2015年1月15日 下午12:29:29
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class ChoosePicActivity extends Activity implements OnClickListener {

	public static final int ACTIVITY_RESULTCODE = 201;

	private Button mChooseFolder, mChoosePicture;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.jiagu.management.R.layout.activity_choose);
		findView();
	}

	private void findView() {
		mChooseFolder = (Button) findViewById(R.id.choose_folder_btn);
		mChoosePicture = (Button) findViewById(R.id.choose_picture_btn);
		mChooseFolder.setOnClickListener(this);
		mChoosePicture.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.choose_folder_btn:
			startActivityForResult(new Intent(this, FileActivity.class), 1);
			break;
		case R.id.choose_picture_btn:
			startActivityForResult(new Intent(this, PictureActivity.class), 1);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == ACTIVITY_RESULTCODE) {
			String path = data.getStringExtra(Constants.IAMGE_PATH);
			Intent intent = new Intent();
			intent.putExtra(Constants.IAMGE_PATH, path);
			setResult(ScenicMarKingActivity.ACTIVITY_RESULTCODE, intent);
			onBackPressed();
		}
	}
}
