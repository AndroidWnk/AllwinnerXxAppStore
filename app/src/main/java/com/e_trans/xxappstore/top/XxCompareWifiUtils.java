package com.e_trans.xxappstore.top;


import java.util.Comparator;

/**
 * @file   CompareWifiInfo.java
 * @date   2014-10-30
 * @author liyz
 * @desc   wifi 比较
 */
public class XxCompareWifiUtils implements Comparator<XxWifiInfo>{

	@Override
	public int compare(XxWifiInfo lhs, XxWifiInfo rhs) {
		int flag = 0 ;
		
		if(lhs.isConnected()) {
			flag = -1 ;
		}else if(rhs.isConnected()) {
			flag = 1;
		}
		
		if(0 == flag) {
			if(lhs.isSaved()) {
				flag = -1;
			}else if(rhs.isSaved()) {
				flag = 1;
			}
		}
		
		if(0 == flag) {
			Integer i1 = lhs.getSignalLevel();
			Integer i2 = rhs.getSignalLevel();
			flag = 0 - i1.compareTo(i2);
		}
		return flag;
	}
}
