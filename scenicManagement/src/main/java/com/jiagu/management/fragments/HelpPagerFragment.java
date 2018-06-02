package com.jiagu.management.fragments;

import com.jiagu.management.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/** 
* @ClassName: HelpPagerFragment 
* @Description: 
* @author zz
* @date 2015年1月15日 下午12:33:57 
* @version 1
* @company 西安甲骨企业文化传播有限公司  
*/ 
public class HelpPagerFragment extends Fragment implements OnClickListener {
	private static final int FIRSTPAGER = 0;
	private static final int SECONDPAGER = 1;

	private ViewPager mViewPager;
	private HelpPagerAdapter mHelpPagerAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_help_pager, container, false);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mViewPager = (ViewPager) getView().findViewById(R.id.help_view_pager);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		//本来可以动态，但是知道固定2页，为了效果好点写死了
		ImageView[] mPagers = new ImageView[2];
		mPagers[0] = new ImageView(getActivity());
		mPagers[0].setLayoutParams(params);
		mPagers[0].setScaleType(ScaleType.FIT_XY);
		mPagers[0].setId(FIRSTPAGER);
		mPagers[0].setOnClickListener(this);

		mPagers[1] = new ImageView(getActivity());
		mPagers[1].setLayoutParams(params);
		mPagers[1].setScaleType(ScaleType.FIT_XY);
		mPagers[1].setId(SECONDPAGER);
		mPagers[1].setOnClickListener(this);
		
		mHelpPagerAdapter = new HelpPagerAdapter(mPagers);
		mViewPager.setAdapter(mHelpPagerAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case FIRSTPAGER:
			mViewPager.setCurrentItem(SECONDPAGER);
			break;
		case SECONDPAGER:
			mViewPager.setCurrentItem(FIRSTPAGER);
			if (listener == null) {
				throw new NullPointerException(
						"HelpPagerFragment OnHelpItemClickListener is null! You must implement OnLastPagerClickListener");
			}
			listener.OnLastPagerClick();
			break;
		default:
			break;
		}
	}

	public void setImageResources(int firstRes, int secondRes) {
		mHelpPagerAdapter.setRes(firstRes, secondRes);
	}

	class HelpPagerAdapter extends PagerAdapter {
		private ImageView[] images;

		public HelpPagerAdapter(ImageView[] images) {
			this.images = images;
		}

		public void setRes(int firstRes, int secondRes) {
			images[0].setImageResource(firstRes);
			images[1].setImageResource(secondRes);
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return images.length;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup view, int position, Object object) {
			view.removeView(images[position]);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(images[position]);
			return images[position];
		}
	}

	private OnLastPagerClickListener listener;

	public void setOnLastPagerClickListener(OnLastPagerClickListener listener) {
		this.listener = listener;
	}

	public interface OnLastPagerClickListener {
		public void OnLastPagerClick();
	}
}
