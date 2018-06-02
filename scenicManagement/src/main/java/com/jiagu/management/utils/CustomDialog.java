package com.jiagu.management.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jiagu.management.R;
import com.jiagu.management.activities.ChoosePicActivity;

/** 
* @ClassName: CustomDialog 
* @Description: 
* @author zz
* @date 2015年1月15日 下午12:35:42 
* @version 1
* @company 西安甲骨企业文化传播有限公司  
*/  
public abstract class CustomDialog {
	// ���� dialog
	public static class GeneralDialog extends Dialog {
		private Activity context;
		private TextView message;
		private TextView leftbtn;
		private TextView rightbtn;
		private TextView middlebtn;

		public GeneralDialog(Activity context) {
			super(context, R.style.CustomDialog);
			this.context = context;
			this.setContentView(R.layout.dialog_general);

			message = (TextView) findViewById(R.id.dialog_message);
			leftbtn = (TextView) findViewById(R.id.dialog_left_btn);
			rightbtn = (TextView) findViewById(R.id.dialog_right_btn);
			middlebtn = (TextView) findViewById(R.id.dialog_middle_btn);
		}

		public void setMessage(String text) {
			message.setText(text);
		}

		public void setOnLeftButtonClickListener(String text, Integer res,
				View.OnClickListener listener) {
			leftbtn.setText(text);
			leftbtn.setOnClickListener(listener);
			if (res != null) {
				leftbtn.setCompoundDrawablesWithIntrinsicBounds(context
						.getResources().getDrawable(res), null, null, null);
			}
		}
		
		public void setOnMiddleButtonClickListener(String text, Integer res, View.OnClickListener listener){
			middlebtn.setText(text);
			middlebtn.setOnClickListener(listener);
			if (res != null) {
				middlebtn.setCompoundDrawablesWithIntrinsicBounds(context
						.getResources().getDrawable(res), null, null, null);
			}
		}
		
		public void hideMiddleButton(){
			middlebtn.setWidth(0);
			middlebtn.setHeight(0);
			middlebtn.setVisibility(View.GONE);
		} 

		public void setOnRightButtonClickListener(String text, Integer res,
				View.OnClickListener listener) {
			rightbtn.setText(text);
			rightbtn.setOnClickListener(listener);
			if (res != null) {
				rightbtn.setCompoundDrawablesWithIntrinsicBounds(context
						.getResources().getDrawable(res), null, null, null);
			}
		}
	}

	public static class EditDialog extends Dialog {
		private TextView leftbtn, rightbtn;
		private EditText edit;

		public EditDialog(Activity context) {
			super(context, R.style.CustomDialog);
			this.setContentView(R.layout.dialog_edit);
			this.setCanceledOnTouchOutside(false);

			edit = (EditText) findViewById(R.id.dialog_edit);
			leftbtn = (TextView) findViewById(R.id.dialog_left_btn);
			rightbtn = (TextView) findViewById(R.id.dialog_right_btn);
		}

		public void setOnLeftButtonClickListener(String text,
				View.OnClickListener listener) {
			leftbtn.setText(text);
			leftbtn.setOnClickListener(listener);
		}

		public void setOnRightButtonClickListener(String text,
				View.OnClickListener listener) {
			rightbtn.setText(text);
			rightbtn.setOnClickListener(listener);
		}

		public boolean isInput() {
			return edit.getText().toString().length() > 0;
		}

		public String getInputValue() {
			return edit.getText().toString();
		}
		
		public void setValue(String name){
			edit.setText(name);
		}
	}

	public static class TableDialog extends Dialog {
		private Activity context;
		private TextView leftbtn, rightbtn;
		private TextView name, lat, lon;

		public TableDialog(Activity context) {
			super(context, R.style.CustomDialog);
			this.context = context;
			this.setContentView(R.layout.dialog_table);
			this.setCanceledOnTouchOutside(false);

			name = (TextView) findViewById(R.id.dialog_scenic_spot_name);
			lat = (TextView) findViewById(R.id.dialog_scenic_spot_lat);
			lon = (TextView) findViewById(R.id.dialog_scenic_spot_lon);

			leftbtn = (TextView) findViewById(R.id.dialog_left_btn);
			rightbtn = (TextView) findViewById(R.id.dialog_right_btn);
		}

		public void setInfo(String name, String lat, String lon) {
			this.name.setText(context.getString(R.string.scenic_spot_name)
					+ ":" + name);
			this.lat.setText(context.getString(R.string.scenic_spot_lat) + lat);
			this.lon.setText(context.getString(R.string.scenic_spot_lon) + lon);
		}

		public void setOnLeftButtonClickListener(View.OnClickListener listener) {
			leftbtn.setOnClickListener(listener);
		}

		public void setOnRightButtonClickListener(View.OnClickListener listener) {
			rightbtn.setOnClickListener(listener);
		}
	}

	public static class NoMapDialog extends Dialog {

		public NoMapDialog(final Activity context) {
			super(context, R.style.CustomDialog);
			this.setContentView(R.layout.dialog_nomap);
			this.setCanceledOnTouchOutside(false);

			TextView upload = (TextView) findViewById(R.id.dialog_upload_btn);
			upload.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					context.startActivityForResult(new Intent(context,
							ChoosePicActivity.class), 0);
					NoMapDialog.this.dismiss();
				}
			});
		}
	}

	public static class BindingDialog extends Dialog {
		private Activity context;
		private TextView leftbtn, rightbtn;
		private TextView name, id;

		public BindingDialog(Activity context) {
			super(context, R.style.CustomDialog);
			this.context = context;
			this.setContentView(R.layout.dialog_binding);
			this.setCanceledOnTouchOutside(true);

			id = (TextView) findViewById(R.id.dialog_scenic_spot_id);
			name = (TextView) findViewById(R.id.dialog_scenic_spot_name);
			leftbtn = (TextView) findViewById(R.id.dialog_left_btn);
			rightbtn = (TextView) findViewById(R.id.dialog_right_btn);
		}

		public void setInfo(String id, String name) {
			this.id.setText(context.getString(R.string.scenic_spot_id) + ":"
					+ id);
			this.name.setText(context.getString(R.string.scenic_spot_name)
					+ ":" + name);
		}

		public void setOnLeftButtonClickListener(View.OnClickListener listener) {
			leftbtn.setOnClickListener(listener);
		}

		public void setOnRightButtonClickListener(View.OnClickListener listener) {
			rightbtn.setOnClickListener(listener);
		}
	}
}
