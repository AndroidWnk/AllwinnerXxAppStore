package com.e_trans.xxappstore.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

public class CubeTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View view, float position) {

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setPivotX(view.getMeasuredWidth());
        } else if (position <= 0) {
            view.setPivotX(view.getMeasuredWidth());
            view.setPivotY(view.getMeasuredHeight() * 0.5f);
            view.setRotationY(90f * position);
        } else if (position <= 1) {
            view.setPivotX(0);
            view.setPivotY(view.getMeasuredHeight() * 0.5f);
            view.setRotationY(90f * position);
        } else { // (1,+Infinity]
            // This page is way off-screen to the right.  
            view.setPivotX(0);
        }

    }

}
