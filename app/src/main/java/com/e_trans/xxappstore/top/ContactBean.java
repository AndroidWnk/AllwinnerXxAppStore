package com.e_trans.xxappstore.top;

import android.graphics.Bitmap;

public class ContactBean {
	
	/**
	 * id
	 */
	private int id;
	/**
	 * PY表中的id
	 */
	private int pyid;
	
	/**
	 * version
	 */
	private int version;

	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * 姓名
	 */
	private String name;

	/**
	 * 手机号
	 */
	private String mobilePhone;
	/**
	 * 家庭号码
	 */
	private String homePhone;


	private String sortKey;
	/**
	 * 头像
	 */
	private Bitmap headBitmap = null;
	/**
	 * 头像id
	 */
	private int photoId = 0;
	/**
	 * 首字母
	 */
	private String initial;
	/**
	 * 拼音
	 */
	private String pinyin;
	
	public int getPyid() {
		return pyid;
	}
	public void setPyid(int pyid) {
		this.pyid = pyid;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	public String getMobilePhone() {
		return mobilePhone;
	}
	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}
	public String getHomePhone() {
		return homePhone;
	}
	public void setHomePhone(String homePhone) {
		this.homePhone = homePhone;
	}
	
	public String getSortKey() {
		return sortKey;
	}
	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}
	
	public String getPinyin() {
		return pinyin;
	}
	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}
	public int getPhotoId() {
		return photoId;
	}
	public void setPhotoId(int photoId) {
		this.photoId = photoId;
	}
	public String getInitial() {
		return initial;
	}
	public void setInitial(String initial) {
		this.initial = initial;
	}
	public Bitmap getHeadBitmap() {
		return headBitmap;
	}
	public void setHeadBitmap(Bitmap headBitmap) {
		this.headBitmap = headBitmap;
	}

	public void setData(ContactBean data){
		this.id = data.id;
		
		this.name = data.name;
		
		this.mobilePhone = data.mobilePhone;
		
		this.homePhone = data.homePhone;

		this.sortKey  = data.sortKey;
		
		this.headBitmap = data.headBitmap;
		
		this.photoId = data.photoId;
		
		this.initial = data.initial;
		
		this.pinyin = data.pinyin;
		
	}

}
