package com.e_trans.xxappstore.activity;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.e_trans.xxappstore.R;
import com.e_trans.xxappstore.adapter.PageviewAdapter;
import com.e_trans.xxappstore.entity.AppDetailsEntity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 图片展示页
 * Created by wk on 2016/4/22.
 */
public class ShowPicturesActivity extends XxBaseActivity implements View.OnClickListener {
    @Bind(R.id.back)
    ImageView back;
    @Bind(R.id.view_pager)
    ViewPager viewPager;
    @Bind(R.id.indicator)
    LinearLayout indicator;

    private LayoutInflater inflater;
    private View item;
    private List<View> list = new ArrayList<View>();
    private List<AppDetailsEntity.ImageInfo> imageList = new ArrayList<AppDetailsEntity.ImageInfo>();
    private int position = 0;
    private PageviewAdapter pageviewAdapter;
    private ImageView[] indicator_imgs;

    @Override
    protected int getLayout() {
        return R.layout.activity_app_show_pictures;
    }

    @Override
    protected void initView() {
        inflater = LayoutInflater.from(this);

    }

    @Override
    protected void initData() {
        imageList = (List<AppDetailsEntity.ImageInfo>) getIntent().getSerializableExtra("imgs");
        position = getIntent().getIntExtra("position", 0);
        if(imageList != null){
            indicator_imgs = new ImageView[imageList.size()];//存放引到图片数组
            for (int i = 0; i < imageList.size(); i++) {
                item = inflater.inflate(R.layout.pageview_item, null);
//            ((TextView) item.findViewById(R.id.text_view)).setText((i + 1) + "/" + imageList.size());
                list.add(item);
            }
            pageviewAdapter = new PageviewAdapter(ShowPicturesActivity.this, list, imageList);
            viewPager.setAdapter(pageviewAdapter);
            viewPager.setOnPageChangeListener(new MyListener());
            initIndicator();
            viewPager.setCurrentItem(position);
        }
    }

    @Override
    protected void setListener() {
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    /**
     * 初始化引导图标
     * 动态创建多个小圆点，然后组装到线性布局里
     */
    private void initIndicator() {
        ImageView imgView;
        View v = findViewById(R.id.indicator);// 线性水平布局，负责动态调整导航图标
        for (int i = 0; i < imageList.size(); i++) {
            imgView = new ImageView(this);
            LinearLayout.LayoutParams params_linear = new LinearLayout.LayoutParams(10, 10);
            params_linear.setMargins(7, 10, 7, 10);
            imgView.setLayoutParams(params_linear);
            indicator_imgs[i] = imgView;
            if (i == position) { // 初始化第一个为选中状态
                indicator_imgs[i].setBackgroundResource(R.drawable.indicator_focused);
            } else {
                indicator_imgs[i].setBackgroundResource(R.drawable.indicator);
            }
            ((ViewGroup) v).addView(indicator_imgs[i]);
        }

    }

    @Override
    public void onUpdateUI(Message msg) {

    }

    /**
     * 动作监听器，可异步加载图片
     */
    private class MyListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
            // TODO Auto-generated method stub
            if (state == 0) {
                //new MyAdapter(null).notifyDataSetChanged();
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onPageSelected(int position) {
            // 改变所有导航的背景图片为：未选中
            for (int i = 0; i < indicator_imgs.length; i++) {
                indicator_imgs[i].setBackgroundResource(R.drawable.indicator);
            }

            // 改变当前背景图片为：选中
            indicator_imgs[position].setBackgroundResource(R.drawable.indicator_focused);
        }

    }
}
