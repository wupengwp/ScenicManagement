package com.jiagu.management.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jiagu.management.R;
import com.jiagu.management.activities.bases.SuperActivity;

/**
 * @ClassName: SoftwareActivity
 * @Description:
 * @author zz
 * @date 2015年1月15日 下午12:32:45
 * @version 1
 * @company 西安甲骨企业文化传播有限公司
 */
public class SoftwareActivity extends SuperActivity implements OnClickListener,
		OnItemClickListener {
	private ListView mFuntionList;
	private FuntionAdapter mFuntionAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_software_funtion);
		findView();
	}

	private void findView() {
		mTitle = (TextView) findViewById(R.id.title_bar_text);
		mTitle.setText(R.string.soft_info);
		mLeftBtn = (TextView) findViewById(R.id.title_left_btn);
		mLeftBtn.setVisibility(View.VISIBLE);
		mLeftBtn.setOnClickListener(this);

		mFuntionList = (ListView) findViewById(R.id.software_funtion_list);
		mFuntionAdapter = new FuntionAdapter(this);
		mFuntionList.setAdapter(mFuntionAdapter);
		mFuntionList.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_left_btn:
			onBackPressed();
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Funtion f = (Funtion) mFuntionAdapter.getItem(position);
		mFuntionAdapter.setCurrent(f);
	}

	class FuntionAdapter extends BaseAdapter {
		private Funtion[] funtions = new Funtion[8];
		private LayoutInflater mLayoutInflater;

		public FuntionAdapter(Context context) {
			mLayoutInflater = LayoutInflater.from(context);
			initData();
		}

		public void setCurrent(Funtion f) {
			for (Funtion ft : funtions) {
				if (ft.id == f.id) {
					ft.iconShow = View.VISIBLE;
					ft.panelColor = R.color.background;
					ft.textColor = R.color.sys_back;
				} else {
					ft.iconShow = View.INVISIBLE;
					ft.panelColor = R.color.transparent;
					ft.textColor = R.color.text;
				}
			}
			this.notifyDataSetInvalidated();
		}

		@Override
		public int getCount() {
			return funtions.length;
		}

		@Override
		public Object getItem(int position) {
			return funtions[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh = null;
			if (convertView == null) {
				vh = new ViewHolder();
				convertView = mLayoutInflater.inflate(
						R.layout.item_software_function, null);
				vh.id = (TextView) convertView
						.findViewById(R.id.software_funtion_id);
				vh.text = (TextView) convertView
						.findViewById(R.id.software_funtion_text);
				vh.panel = convertView
						.findViewById(R.id.software_funtion_panel);
				vh.icon = (ImageView) convertView
						.findViewById(R.id.software_funtion_icon);
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}
			vh.id.setText(funtions[position].id + "");
			vh.text.setText(funtions[position].text);
			vh.text.setTextColor(getResources().getColor(
					funtions[position].textColor));
			vh.panel.setBackgroundResource(funtions[position].panelColor);
			vh.icon.setVisibility(funtions[position].iconShow);
			return convertView;
		}

		class ViewHolder {
			TextView id, text;
			View panel;
			ImageView icon;
		}

		private void initData() {
			Funtion f = new Funtion(1, R.string.funtion_1);
			f.iconShow = View.VISIBLE;
			f.panelColor = R.color.background;
			f.textColor = R.color.sys_back;
			funtions[0] = f;
			funtions[1] = new Funtion(2, R.string.funtion_2);
			funtions[2] = new Funtion(3, R.string.funtion_3);
			funtions[3] = new Funtion(4, R.string.funtion_4);
			funtions[4] = new Funtion(5, R.string.funtion_5);
			funtions[5] = new Funtion(6, R.string.funtion_6);
			funtions[6] = new Funtion(7, R.string.funtion_7);
			funtions[7] = new Funtion(8, R.string.funtion_8);
		}
	};

	private class Funtion {
		public int id;
		public int text;
		public int iconShow = View.INVISIBLE;
		public int panelColor = R.color.transparent;
		public int textColor = R.color.text;

		public Funtion(int id, int text) {
			this.id = id;
			this.text = text;
		}
	}
}
