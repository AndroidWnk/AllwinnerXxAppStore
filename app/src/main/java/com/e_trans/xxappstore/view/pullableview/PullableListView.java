package com.e_trans.xxappstore.view.pullableview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListView;

import com.e_trans.xxappstore.constant.Constant;

public class PullableListView extends ListView implements Pullable
{

	public PullableListView(Context context)
	{
		super(context);
	}

	public PullableListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public PullableListView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	public boolean canPullDown()
	{
		if (getCount() == 0)
		{
			// 没有item的时候不可以下拉刷新
			return false;
		} else if (getFirstVisiblePosition() == 0
				&& getChildAt(0).getTop() >= 0)
		{
			// 滑到ListView的顶部了
			return true;
		} else
			return false;
	}

	@Override
	public boolean canPullUp()
	{
		if (getCount() < Constant.pagesize)
		{
			// 少于分页每页条数时不可以上拉
			return false;
		} else if (getLastVisiblePosition() == (getCount() - 1))
		{
			// 滑到底部了
			if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null
					&& getChildAt(
							getLastVisiblePosition()
									- getFirstVisiblePosition()).getBottom() <= getMeasuredHeight())
				return true;
		}
		return false;
	}
}
