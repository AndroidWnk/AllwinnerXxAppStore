package com.e_trans.xxappstore.transformer;

import android.support.v4.view.ViewPager;

public class TransformerFactory
{	
	public enum TRANSFORMER_TYPE
	{
		CUBE,		
		DEPTH,		
		ZOOMOUT
	}
	
	public static ViewPager.PageTransformer getTransformer(TRANSFORMER_TYPE type)	
	{
		switch (type)
		{
			case CUBE:
				return new CubeTransformer();
				
			case DEPTH:
				return new DepthPageTransformer();
				
			case ZOOMOUT:
				return new ZoomOutPageTransformer();				
	
			default:
				return new CubeTransformer();
		}
	}
	
}
