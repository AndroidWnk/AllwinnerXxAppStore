package com.e_trans.xxappstore.adapter;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class DownloadPagerAdapter extends PagerAdapter{

	private List<View> mListViews;
	public DownloadPagerAdapter(List<View> views)
	{
		mListViews = views;
	}
	
	@Override
	public int getCount() {
		return mListViews != null && mListViews.size() >0 ? mListViews.size() : 0;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	@Override
    public void destroyItem(View arg0, int arg1, Object arg2)
    {
		((ViewPager) arg0).removeView(mListViews.get(arg1));
    }   
    
    @Override
     public Object instantiateItem(View arg0, int arg1) {
    	if (arg1 == 1) {
    		//XxStatisticsUsers.getXxStatisticsHandle().onEvent(, event)
		}
		((ViewPager) arg0).addView(mListViews.get(arg1), 0);
		return mListViews.get(arg1);
    }
}
