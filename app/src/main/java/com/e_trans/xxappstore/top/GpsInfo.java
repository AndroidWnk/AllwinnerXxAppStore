package com.e_trans.xxappstore.top;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationProvider;

import java.util.Iterator;

public class GpsInfo {
	
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public float getBearing() {
		return bearing;
	}
	public void setBearing(float bearing) {
		this.bearing = bearing;
	}
	public float getSpeed() {
		return speed;
	}
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;



	}
	public int getUTC_year() {
		return UTC_year;
	}
	public void setUTC_year(int uTC_year) {
		UTC_year = uTC_year;
	}
	public int getUTC_mon() {
		return UTC_mon;
	}
	public void setUTC_mon(int uTC_mon) {
		UTC_mon = uTC_mon;
	}
	public int getUTC_day() {
		return UTC_day;
	}
	public void setUTC_day(int uTC_day) {
		UTC_day = uTC_day;
	}
	public int getUTC_hour() {
		return UTC_hour;
	}
	public void setUTC_hour(int uTC_hour) {
		UTC_hour = uTC_hour;
	}
	public int getUTC_min() {
		return UTC_min;
	}
	public void setUTC_min(int uTC_min) {
		UTC_min = uTC_min;
	}
	public int getUTC_sec() {
		return UTC_sec;
	}
	public void setUTC_sec(int uTC_sec) {
		UTC_sec = uTC_sec;
	}
	public double getAltitude() {
		return altitude;
	}
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	public int[] getStar_sat_no_arr() {
		return star_sat_no_arr;
	}
	public void setStar_sat_no_arr(int[] star_sat_no_arr) {
		this.star_sat_no_arr = star_sat_no_arr;
	}
	public int[] getStar_elev_arr() {
		return star_elev_arr;
	}
	public void setStar_elev_arr(int[] star_elev_arr) {
		this.star_elev_arr = star_elev_arr;
	}
	public int[] getStar_azim_arr() {
		return star_azim_arr;
	}
	public void setStar_azim_arr(int[] star_azim_arr) {
		this.star_azim_arr = star_azim_arr;
	}
	public int[] getStar_snr_arr() {
		return star_snr_arr;
	}
	public void setStar_snr_arr(int[] star_snr_arr) {
		this.star_snr_arr = star_snr_arr;
	}
	public int getStarcount() {
		return starcount;
	}
	public void setStarcount(int starcount) {
		this.starcount = starcount;
	}
	//经度
	private double longitude = 0.0;
	//纬度
	private double latitude = 0.0;
	//定位的状态
	private int status = 0;
	//方向
	private float bearing = 0.0f;
	//速度
	private float speed = 0.0f;
	//时间戳
	private long timestamp = 0;
	//年
	private int  UTC_year = 0;
	//月
	private int  UTC_mon  = 0;
	private int  UTC_day  = 0;
	private int  UTC_hour = 0;
	private int  UTC_min  = 0;
	private int  UTC_sec  = 0;
	//海拔
	private double altitude = 0.0;
	private int[]  star_sat_no_arr = new int[12];
	private int[]  star_elev_arr   = new int[12];
	private int[]  star_azim_arr   = new int[12];
	private int[]  star_snr_arr   = new int[12];

	public int getGpsType() {
		return gpsType;
	}

	public void setGpsType(int gpsType) {
		this.gpsType = gpsType;
	}

	private int gpsType;
	/**
	 * 当前卫星数目
	 */
	private int starcount = 0;
	
	public void setGpsInfo(Location location,GpsStatus gpsStatus){
		if(location != null){
			if (this.status != LocationProvider.AVAILABLE
					&& location.getProvider().equals("gps")) {
				this.status = LocationProvider.AVAILABLE;
			}
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			bearing = location.hasBearing()?location.getBearing():0.0f;
			altitude = location.hasAltitude()?location.getAltitude():0.0;
			timestamp = location.getTime();
			java.util.Date dt =new java.util.Date( timestamp );
		  	UTC_year =dt.getYear() + 1900;
		  	UTC_mon=dt.getMonth() + 1;
		  	UTC_day=dt.getDate();
		  	UTC_hour=dt.getHours() - 8;
		  	UTC_min=dt.getMinutes();
		  	UTC_sec=dt.getSeconds();
		}
		
		if (gpsStatus != null) {
		  	Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
		  	Iterator<GpsSatellite> iterator = satellites.iterator();
		  	for (starcount = 0;starcount < 12 && iterator.hasNext();starcount ++)
		  	{
		  		GpsSatellite sat = iterator.next();
		  		star_sat_no_arr[starcount] = sat.getPrn();
		  		star_snr_arr[starcount] = (int) sat.getSnr();
		  		star_azim_arr[starcount] = (int) sat.getAzimuth();
		  		star_elev_arr[starcount] = (int) sat.getElevation();
		  	}
		}
	}
	
	
	
}
