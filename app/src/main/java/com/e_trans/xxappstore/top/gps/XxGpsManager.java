package com.e_trans.xxappstore.top.gps;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.e_trans.xxappstore.top.GpsInfo;
import com.linkgent.remoute.api.XxLocationConfig;
import com.linkgent.xxaidl.IXxDistrictListener;
import com.linkgent.xxaidl.IXxGpsStateListener;
import com.linkgent.xxaidl.IXxLocationListener;
import com.linkgent.xxaidl.IXxLocationService;
import com.linkgent.xxaidl.XxDistrict;
import com.linkgent.xxaidl.XxLocationInfo;

import java.util.ArrayList;

/**
 * GPS 管理者
 *
 * @author ping
 */
public class XxGpsManager {


    public static final int STATE_OUT_OF_SERVICE = XxLocationConfig.STATE_OUT_OF_SERVICE;

    public static final int STATE_TEMPORARILY_UNAVAILABLE = XxLocationConfig.STATE_TEMPORARILY_UNAVAILABLE;

    public static final int STATE_AVAILABLE = XxLocationConfig.STATE_AVAILABLE;

    public static final int STATE_GPS_DISABLE = XxLocationConfig.STATE_GPS_DISABLE;

    public static final int STATE_GPS_ENABLE = XxLocationConfig.STATE_GPS_ENABLE;


    private  static  final int MSG_STATE = 1;

    public interface OnXxDistrictChangeListener {
        /**
         * 分发行政区划改变
         *
         */
        public abstract void onDistrictChanged(DistrictInfo district);
    }

    public interface OnXxGpsChangeListener {

        /**
         * GPS信息改变
         *
         * @param type   gps坐标的类型 是否加密
         * @param gpsInfo
         */
        public abstract void onGpsChange(boolean isEncrypt, GpsInfo gpsInfo);

        /**
         * GPS 状态改变
         *
         * @param state {@link #STATE_OUT_OF_SERVICE } {@link #STATE_GPS_ENABLE}
         */
        public abstract void onGpsStateChanged(int state);
    }

    private Context mContext;

    private static final class XxGpsManagerHolder {
        static XxGpsManager instance = new XxGpsManager();
    }

    private XxGpsManager() {
    }

    public static XxGpsManager getInstance() {
        return XxGpsManagerHolder.instance;
    }

    public void init(Context context) {
        mContext = context;
        bind();
    }

    private void bind(){
        Intent service = new Intent(XxLocationConfig.LOCATION_SERVICE);
        mContext.bindService(service, con, Context.BIND_AUTO_CREATE);
    }

    /**
     * 设置行政改变的监听器
     *
     * @param l
     */
    public void addOnDistrictChangeListener(OnXxDistrictChangeListener l) {
        mArrayDistrictListener.add(l);
    }

    public void removeOnDistrictChangeListener(OnXxDistrictChangeListener l) {
        mArrayDistrictListener.remove(l);
    }

    public void removeOnXxGpsChangeListener(OnXxGpsChangeListener l) {
        mArrayGpsListener.remove(l);
    }

    /**
     * 添加GPS改变监听器
     *
     * @param l
     */
    public void addOnXxGpsChangeListener(OnXxGpsChangeListener l) {
        mArrayGpsListener.add(l);
    }

    /**
     * GPS 状态
     * @return
     */
    public int getGpsState(){
        return mGpsState;
    }

    public DistrictInfo getDistrictInfo(){
        return mDistrict;
    }

    public GpsInfo getGpsInfo(){
        return mGpsInfo;
    }
    /**
     * 根据地理坐标查询行政区划
     * @param gpsType
     *    地理坐标类型 {@link GPSTYPE_WGS84}{@link GPSTYPE_GCJ02}
     * @param lon
     *      经度
     * @param lat
     *      纬度
     * @return
     *    行政区划信息
     */
    public DistrictInfo queryDistrict(int gpsType,double lon,double lat){
        if(mLocationService == null){
            return  null;
        }
        try {
            XxDistrict xxdistrict =  mLocationService.queryDistrictByGeo(gpsType,lon,lat);
            DistrictInfo district = new DistrictInfo();
            setDistrict(district,xxdistrict);
            return district;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据行政区划代码查询行政区划
     * @param districtCode
     *    行政区划代码
     * @return
     *    行政区划信息
     */
    public DistrictInfo queryDistrict(int districtCode){
        if(mLocationService == null){
            return null;
        }
        try {
            XxDistrict xxdistrict = mLocationService.queryDistritByCode(districtCode);
            DistrictInfo district = new DistrictInfo();
            setDistrict(district,xxdistrict);
            return district;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }


    private ServiceConnection con = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocationService = IXxLocationService.Stub.asInterface(service);

            try {
                mGpsState = mLocationService.getGpsState();
                Message msg = mHander.obtainMessage();
                msg.what = MSG_STATE;
                msg.arg1 = mGpsState;
                mHander.sendMessage(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                XxLocationInfo location = mLocationService.getLocationInfo();
                if(location!=null) {
                    mGpsInfo.setSpeed(location.getSpeed());
                    mGpsInfo.setTimestamp(location.getTimestamp());
                    mGpsInfo.setBearing(location.getBearing());
                    mGpsInfo.setAltitude(location.getAltitude());
                    mGpsInfo.setLatitude(location.getLat());
                    mGpsInfo.setLongitude(location.getLon());
                    mGpsInfo.setGpsType(location.getType());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                XxDistrict district = mLocationService.getCurDistrictInfo();
                if(district != null){
                    setDistrict(mDistrict,district);
                    for(OnXxDistrictChangeListener listener : mArrayDistrictListener){
                        if(listener!=null)
                            listener.onDistrictChanged(mDistrict);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                mLocationService.addIXxDistrictListener(mDistrictListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                mLocationService.addIXxGpsStateListener(mGpsStateListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                mLocationService.addIXxLocationListener(mLocationListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bind();
        }
    };

    private Handler mHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_STATE){
                for(int i = 0; i < mArrayGpsListener.size(); i++){
                    OnXxGpsChangeListener listener = mArrayGpsListener.get(i);
                    if(listener != null){
                        listener.onGpsStateChanged(msg.arg1);
                    }
                }
            }
        }
    };

    private IXxLocationListener mLocationListener = new IXxLocationListener.Stub(){

        @Override
        public void onLocationChange(int gpsType, double lon, double lat, float speed, float bearing, double altitude, long timestamp) throws RemoteException {
            mGpsInfo.setLongitude(lon);
            mGpsInfo.setLatitude(lat);
            mGpsInfo.setAltitude(altitude);
            mGpsInfo.setBearing(bearing);
            mGpsInfo.setSpeed(speed);
            mGpsInfo.setTimestamp(timestamp);
            for(OnXxGpsChangeListener listener:mArrayGpsListener){
                if(listener!=null){
                    listener.onGpsChange(gpsType != XxLocationConfig.GPSTYPE_WGS84,mGpsInfo);
                }
            }
            //mGpsInfo.set
        }
    };


    private IXxDistrictListener mDistrictListener = new IXxDistrictListener.Stub() {
        @Override
        public void onDistrictChange(XxDistrict xxDistrict) throws RemoteException {
            setDistrict(mDistrict,xxDistrict);
            for(OnXxDistrictChangeListener listener : mArrayDistrictListener){
                if(listener!=null)
                    listener.onDistrictChanged(mDistrict);
            }
        }
    };

    private IXxGpsStateListener mGpsStateListener = new IXxGpsStateListener.Stub() {
        @Override
        public void onState(int state) throws RemoteException {
            for(OnXxGpsChangeListener listener:mArrayGpsListener){
                if(listener!=null){
                    listener.onGpsStateChanged(state);
                }
            }
        }
    };

    private void setDistrict(DistrictInfo districtInfo,XxDistrict xxdistrict){
        if(xxdistrict == null)
            return;
        districtInfo.setCountry(xxdistrict.getCountry());
        districtInfo.setProvince(xxdistrict.getProvince());
        districtInfo.setProvinceCode(xxdistrict.getProvinceCode());
        districtInfo.setArea(xxdistrict.getArea());
        districtInfo.setAreaCode(xxdistrict.getAreaCode());
        districtInfo.setCity(xxdistrict.getCity());
        districtInfo.setCityCode(xxdistrict.getCityCode());
    }

    private IXxLocationService mLocationService;
    private  ArrayList<OnXxDistrictChangeListener> mArrayDistrictListener = new ArrayList<OnXxDistrictChangeListener>();


    private ArrayList<OnXxGpsChangeListener> mArrayGpsListener = new ArrayList<OnXxGpsChangeListener>();

    private int mGpsState = XxLocationConfig.STATE_GPS_DISABLE;
    private DistrictInfo mDistrict = new DistrictInfo();

    private GpsInfo mGpsInfo = new GpsInfo();
}
