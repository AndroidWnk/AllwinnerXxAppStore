package com.e_trans.xxappstore.view.pullableview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

import com.e_trans.xxappstore.constant.Constant;

public class PullableGridView extends GridView implements Pullable {

    public PullableGridView(Context context) {
        super(context);
    }

    public PullableGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullableGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean canPullDown() {
        try {
            if (getCount() == 0) {
                // 没有item的时候也可以下拉刷新
                return true;
            } else if (getFirstVisiblePosition() == 0
                    && getChildAt(0).getTop() >= 0) {
                // 滑到顶部了
                return true;
            } else
                return false;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean canPullUp() {
        if (getCount() < Constant.pagesize) {
            // 少于分页每页条数时不可以上拉
            return false;
        } else if (getLastVisiblePosition() == (getCount() - 1)) {
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
