package com.e_trans.xxappstore.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class CustomGridView extends GridView {

	public CustomGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomGridView(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int customSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, customSpec);
	}
}
