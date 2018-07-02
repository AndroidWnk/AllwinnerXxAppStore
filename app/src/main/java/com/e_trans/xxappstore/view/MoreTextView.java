package com.e_trans.xxappstore.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Html;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.e_trans.xxappstore.R;

public class MoreTextView extends LinearLayout{
	protected TextView contentView;
	protected TextView expandView;
	
	protected int textColor;
	protected float textSize;
	protected int maxLine;
	protected String text;
	
	public int defaultTextColor = Color.WHITE;
	public int defaultTextSize = 12;
	public int defaultLine = 3;
	private boolean isCanClick = true;

	public MoreTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initalize();
		initWithAttrs(context, attrs);
		bindListener();
	}

	protected void initWithAttrs(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs,  
                R.styleable.MoreTextStyle);
		int textColor = a.getColor(R.styleable.MoreTextStyle_textColor,  
				defaultTextColor);  
		textSize = a.getDimensionPixelSize(R.styleable.MoreTextStyle_textSize, defaultTextSize);
		maxLine = a.getInt(R.styleable.MoreTextStyle_maxLine, defaultLine);
		text = a.getString(R.styleable.MoreTextStyle_text);
		bindTextView(textColor,textSize,maxLine,text);
		a.recycle();
	}

	protected void initalize() {
		setOrientation(VERTICAL);
		setGravity(Gravity.RIGHT);
		contentView = new TextView(getContext());
		addView(contentView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		expandView = new TextView(getContext());
		int padding = dip2px(getContext(), 5);
		expandView.setPadding(padding, padding, padding, padding);
		expandView.setTextSize(20);
		expandView.setTextColor(getResources().getColor(R.color.white));
		expandView.setText(Html.fromHtml("<u>"+getResources().getString(R.string.string_expand)+"</u>"));
//		expandView.setImageResource(R.drawable.text_ic_expand);
		LayoutParams llp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		addView(expandView, llp);
	}
	
	protected void bindTextView(int color,float size,final int line,String text){
		contentView.setTextColor(color);
		contentView.setTextSize(TypedValue.COMPLEX_UNIT_PX,size);
		contentView.setText(text);
		contentView.setHeight(contentView.getLineHeight() * line);
		post(new Runnable() {

			@Override
			public void run() {
				expandView.setVisibility(contentView.getLineCount() > line ? View.VISIBLE : View.GONE);

			}
		});
	}
	
	protected void bindListener(){
		setOnClickListener(new OnClickListener() {
			boolean isExpand;

			@Override
			public void onClick(View v) {
				if(!isCanClick)
					return;
				isExpand = !isExpand;
				contentView.clearAnimation();
				final int deltaValue;
				final int startValue = contentView.getHeight();
				int durationMillis = 350;
				if (isExpand) {
					deltaValue = contentView.getLineHeight() * contentView.getLineCount() - startValue;
//					RotateAnimation animation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//					animation.setDuration(durationMillis);
//					animation.setFillAfter(true);
//					expandView.startAnimation(animation);
					expandView.setText(Html.fromHtml("<u>"+getResources().getString(R.string.string_collapse)+"</u>"));
				} else {
					deltaValue = contentView.getLineHeight() * maxLine - startValue;
//					RotateAnimation animation = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//					animation.setDuration(durationMillis);
//					animation.setFillAfter(true);
//					expandView.startAnimation(animation);
					expandView.setText(Html.fromHtml("<u>"+getResources().getString(R.string.string_expand)+"</u>"));
				}
				Animation animation = new Animation() {
					protected void applyTransformation(float interpolatedTime, Transformation t) {
						contentView.setHeight((int) (startValue + deltaValue * interpolatedTime));
					}

				};
				animation.setDuration(durationMillis);
				contentView.startAnimation(animation);
			}
		});
	}

	public TextView getTextView(){
		return contentView;
	}
	
	public void setText(CharSequence charSequence){
		contentView.setText(charSequence);
		post(new Runnable() {

			@Override
			public void run() {
				if(contentView.getLineCount() > maxLine){
					isCanClick = true;
					expandView.setVisibility(View.VISIBLE);
				}else{
					isCanClick = false;
					expandView.setVisibility(View.GONE);
				}

			}
		});
	}
	
	public static int dip2px(Context context, float dipValue){              
        final float scale = context.getResources().getDisplayMetrics().density;                   
        return (int)(dipValue * scale + 0.5f);           
    }      
}
