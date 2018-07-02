package com.e_trans.xxappstore.top;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author liyz
 * @filename XxWifiManager.java
 * @desc wifi管理类
 * @date 2015-02-09
 */
public class XxWifiManager extends XxBaseModule {

    private XxWifiUtils mWifiUtils;
    private boolean bInWifiListActivity;

    private Thread wifiViewThread;
    private boolean bStartScan;
    private Timer mScanTimer;
    private XxNetManager mNetManager;

    private String mConnName;

    private XxWifiConst.XxWifiEncryDesc mConnEncryDesc;

    private List<String> mConnectFailedList = new ArrayList<String>();

    private boolean bHasAutoConnect = false;

    public XxWifiManager() {
    }

    /**
     * @param context netManager
     * @return this
     * @author liyz
     * @desc 初始化
     */
    public XxWifiManager init(Context context, XxNetManager netManager) {
        super.init(context);
        mWifiUtils = XxSetting.getInstance().getWifiUtils();
        wifiViewThread = new Thread(new RefreshWifiInfosThread());
        mNetManager = netManager;
        return this;
    }

    /**
     * @param networkid
     * @return
     * @author liyz
     * @desc 删除保存的wifi
     */
    public boolean removeNetwork(int networkid, String wifiName) {
        updateConnFaildName(wifiName);
        boolean flag = mWifiUtils.removeNetwork(networkid);
        startScan();
        return flag;
    }

    public void updateConnFaildName(String wifiName) {
        if (mConnectFailedList != null) {
            mConnectFailedList.remove(wifiName);
        }
    }

    /**
     * 连接wifi
     *
     * @return
     */
    public boolean connect(int netId) {
        boolean flag = mWifiUtils.enableNetwork(netId, true);
        startScan();
        return flag;
    }

    public boolean connect(int netId, String wifiName) {
        updateConnFaildName(wifiName);
        changeConnectName(wifiName);
        return connect(netId);
    }

    private void changeConnectName(String wifiName) {
        mConnName = wifiName;
        mHandler.removeMessages(XxMessage.MSG_SETTING_RE_WIFILIST_DESC);
    }

    /**
     * @param wifiInfo pass
     * @return id
     * @author liyz
     * @desc 生成连接配置 返回网络id
     */
    public int createWifiConfigure(XxWifiInfo wifiInfo, String pass) {
        int retVal = -1;
        WifiConfiguration wifiConf = new WifiConfiguration();
        wifiConf.allowedAuthAlgorithms.clear();
        wifiConf.allowedGroupCiphers.clear();
        wifiConf.allowedKeyManagement.clear();
        wifiConf.allowedPairwiseCiphers.clear();
        wifiConf.allowedProtocols.clear();
        wifiConf.SSID = "\"" + wifiInfo.getName() + "\"";// \"转义字符，代表"
        mConnName = wifiInfo.getName();
        mHandler.removeMessages(XxMessage.MSG_SETTING_RE_WIFILIST_DESC);
        if (pass != null && !pass.isEmpty()) {
            if (wifiInfo.getKeyMgmt() == (XxWifiConst.XxWifiEncryConst.WPAPSK | XxWifiConst.XxWifiEncryConst.WPA2PSK)) {
                wifiConf.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConf.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.OPEN);
            } else if (wifiInfo.getKeyMgmt() == XxWifiConst.XxWifiEncryConst.WPAPSK) {
                wifiConf.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConf.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.OPEN);
            } else if (wifiInfo.getKeyMgmt() == XxWifiConst.XxWifiEncryConst.WPA2PSK) {
                wifiConf.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConf.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.OPEN);
            } else if (wifiInfo.getKeyMgmt() == XxWifiConst.XxWifiEncryConst.WEP) {
                wifiConf.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.NONE);
                wifiConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConf.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.OPEN);
                wifiConf.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.SHARED);
                wifiConf.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.CCMP);
                wifiConf.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.TKIP);
                wifiConf.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.WEP104);
                wifiConf.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.WEP40);
                wifiConf.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConf.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.TKIP);
            }

            if (wifiInfo.getGroupCipher() == (XxWifiConst.XxWifiCipherConst.CCMP | XxWifiConst.XxWifiCipherConst.TKIP)) {
                wifiConf.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConf.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.TKIP);
            } else if (wifiInfo.getGroupCipher() == XxWifiConst.XxWifiCipherConst.CCMP) {
                wifiConf.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.CCMP);
            } else if (wifiInfo.getGroupCipher() == XxWifiConst.XxWifiCipherConst.TKIP) {
                wifiConf.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.TKIP);
            }
            wifiConf.status = WifiConfiguration.Status.ENABLED;
            wifiConf.preSharedKey = "\"" + pass + "\"";// WPA-PSK密码
        } else {
            if (wifiInfo.getKeyMgmt() == XxWifiConst.XxWifiEncryConst.NONE) {
                wifiConf.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.NONE);
            }
        }
        retVal = mWifiUtils.addNetwork(wifiConf);
        return retVal;
    }

    private boolean bTryConnWifi = false;

    private void autoConnect() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                tryConnectingWifi();
            }
        }).start();
    }

    private void tryConnectingWifi() {
        bTryConnWifi = true;
        List<ScanResult> scanResults = mWifiUtils.getScanResults();
        List<WifiConfiguration> saveResults = mWifiUtils
                .getConfiguredNetworks();

        boolean bFailed = false;

        List<WifiConfiguration> onLineSaveResults = new ArrayList<WifiConfiguration>();
        for (WifiConfiguration conf : saveResults) {
            boolean flag = false;
            for (ScanResult result : scanResults) {
                String wcSSID = conf.SSID.replaceAll("\"", "");
                String itemName = result.SSID.replaceAll("\"", "");
                if (wcSSID.equals(itemName)) {
                    flag = true;
                }
            }
            if (flag) {
                onLineSaveResults.add(conf);
            }
        }

        int scanResultsLength = onLineSaveResults.size();
        for (int i = 0; i < scanResultsLength; i++) {
            WifiConfiguration wifiConf = onLineSaveResults.get(i);
            for (String str : mConnectFailedList) {
                String wcSSID = wifiConf.SSID.replaceAll("\"", "");
                if (wcSSID.replace("\"", "").equals(str.replace("\"", ""))) {
                    bFailed = true;
                    break;
                }
            }
            if (!bFailed) {
                connect(wifiConf.networkId, wifiConf.SSID);
                bTryConnWifi = false;
                return;
            }
            /*
             * int saveResultsLength = saveResults.size(); for(int j=0 ; j
			 * <saveResultsLength ; j++) { WifiConfiguration wifiConf =
			 * saveResults.get(j); String wcSSID =
			 * wifiConf.SSID.replaceAll("\"", ""); String itemName =
			 * scanResult.SSID.replaceAll("\"", "");
			 * if(wcSSID.equals(itemName)){
			 *
			 * }
			 *
			 *
			 * }
			 */
        }
        // for(XxWifiInfo info : wifiInfos) {
        // bFailed = false ;
        // for(String str : mConnectFailedList) {
        // if(info.getName().replace("\"", "").equals(str.replace("\"", ""))) {
        // bFailed = true;
        // break;
        // }
        // }
        // if(!bFailed) {
        // connect(info.getNetworkId() , info.getName());
        // break;
        // }
        // }
    }

    public synchronized List<XxWifiInfo> updateWifiInfos(boolean forceGet) {
        List<XxWifiInfo> wifiInfos = null;
        List<XxWifiInfo> savedWifiInfos = null;
        if (bInWifiListActivity) {
            if ((bStartScan && mWifiUtils.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
                    || forceGet) {
                List<ScanResult> scanResults = mWifiUtils.getScanResults();
                List<WifiConfiguration> saveResults = mWifiUtils
                        .getConfiguredNetworks();
                if (scanResults != null && saveResults != null) {
                    int scanResultsLength = scanResults.size();
                    for (int i = 0; i < scanResultsLength; i++) {
                        ScanResult scanResult = scanResults.get(i);
                        if (scanResult.SSID == null
                                || scanResult.SSID.trim().equals("")) {
                            continue;
                        }
                        XxWifiInfo item = new XxWifiInfo();
                        if (!inflateData(item, scanResult)) {
                            continue;
                        }
                        int saveResultsLength = saveResults.size();
                        WifiConfiguration reWifiConf = null;
                        for (int j = 0; j < saveResultsLength; j++) {
                            WifiConfiguration wifiConf = saveResults.get(j);
                            String wcSSID = wifiConf.SSID;
                            String itemName = "\"" + item.getName() + "\"";
                            if (wcSSID.equals(itemName)) {
//								if (isRemoveSaveItem(item, wifiConf)) {
//									boolean bSucc = mWifiUtils
//											.removeNetwork(wifiConf.networkId);
//									if (bSucc) {
//										continue;
//									}
//								}
                                item.setNetworkId(wifiConf.networkId);
                                item.setSaved(true);
                                updateEncryDesc(item);
                                WifiInfo wifiInfo = mWifiUtils
                                        .getConnectionInfo();
                                if (!wifiInfo.getSSID().equals("0x")) {
                                    mConnName = wifiInfo.getSSID();
                                    mHandler.removeMessages(XxMessage.MSG_SETTING_RE_WIFILIST_DESC);
                                }
                                if (mConnName != null
                                        && mConnName.replaceAll("\"", "")
                                        .equals(wcSSID.replaceAll("\"",
                                                ""))
                                        && mConnName.replaceAll("\"", "")
                                        .equals(itemName.replaceAll(
                                                "\"", ""))) {
                                    item.setConnected(true);
                                    if (mNetManager
                                            .getConnectivityManager()
                                            .getStateByConnType(
                                                    ConnectivityManager.TYPE_WIFI) == State.CONNECTED) {
                                        mConnEncryDesc = XxWifiConst.XxWifiEncryDesc.WIFI_CONNECTED;
                                    }
                                }
                                reWifiConf = wifiConf;
                            }

                        }
                        saveResults.remove(reWifiConf);
                        if (wifiInfos == null) {
                            wifiInfos = new ArrayList<XxWifiInfo>();
                        }

                        // WifiInfo connItem = mWifiUtils.getConnectionInfo();
                        // if(connItem != null) {
                        // if(connItem.getSSID().replace("\"",
                        // "").equals(item.getName().replace("\"", ""))) {
                        // updateConnectingItemDesc(item);
                        // }
                        // }
                        wifiInfos.add(item);
                    }
                    if (wifiInfos == null) {
                        wifiInfos = new ArrayList<XxWifiInfo>();
                    }
                    Collections.sort(wifiInfos, new XxCompareWifiUtils());
                    addSurplusWifiInfo(wifiInfos, saveResults);
                } else if (scanResults != null && saveResults == null) {
                    for (ScanResult scanResult : scanResults) {
                        XxWifiInfo item = new XxWifiInfo();
                        inflateData(item, scanResult);
                        if (wifiInfos == null) {
                            wifiInfos = new ArrayList<XxWifiInfo>();
                        }
                        wifiInfos.add(item);
                    }
                } else if (scanResults == null && saveResults != null) {
                    if (wifiInfos == null) {
                        wifiInfos = new ArrayList<XxWifiInfo>();
                    }
                    addSurplusWifiInfo(wifiInfos, saveResults);
                }
            }
        }

        return wifiInfos;
    }

    /**
     * @param forceGet
     * @return
     * @author liyz
     * @desc 更新wifi显示信息
     */
    public synchronized List<XxWifiInfo> updateWifiInfos1(boolean forceGet) {
        List<XxWifiInfo> wifiInfos = null;
        if (bInWifiListActivity || forceGet) {
            if (bStartScan
                    && mWifiUtils.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                List<ScanResult> scanResults = mWifiUtils.getScanResults();
                List<WifiConfiguration> saveResults = mWifiUtils
                        .getConfiguredNetworks();
                if (scanResults != null && saveResults != null) {
                    int scanResultsLength = scanResults.size();
                    for (int i = 0; i < scanResultsLength; i++) {
                        ScanResult scanResult = scanResults.get(i);
                        if (scanResult.SSID == null
                                || scanResult.SSID.trim().equals("")) {
                            continue;
                        }
                        XxWifiInfo item = new XxWifiInfo();
                        if (!inflateData(item, scanResult)) {
                            continue;
                        }
                        int saveResultsLength = saveResults.size();
                        WifiConfiguration reWifiConf = null;
                        for (int j = 0; j < saveResultsLength; j++) {
                            WifiConfiguration wifiConf = saveResults.get(j);
                            String wcSSID = wifiConf.SSID;
                            String itemName = "\"" + item.getName() + "\"";
                            if (wcSSID.equals(itemName)) {
                                if (isRemoveSaveItem(item, wifiConf)) {
                                    boolean bSucc = mWifiUtils
                                            .removeNetwork(wifiConf.networkId);
                                    if (bSucc) {
                                        continue;
                                    }
                                }
                                item.setNetworkId(wifiConf.networkId);
                                item.setSaved(true);
                                updateEncryDesc(item);
                                WifiInfo wifiInfo = mWifiUtils
                                        .getConnectionInfo();
                                if (wifiInfo != null
                                        && wifiInfo.getSSID().equals(wcSSID)) {
                                    item.setConnected(true);

                                }
                                reWifiConf = wifiConf;
                            }
                        }
                        saveResults.remove(reWifiConf);
                        if (wifiInfos == null) {
                            wifiInfos = new ArrayList<XxWifiInfo>();
                        }

                        WifiInfo connItem = mWifiUtils.getConnectionInfo();
                        if (connItem != null) {
                            if (connItem.getSSID().replace("\"", "")
                                    .equals(item.getName().replace("\"", ""))) {
                                updateConnectingItemDesc(item);
                            }
                        }

                        wifiInfos.add(item);
                    }
                    if (wifiInfos == null) {
                        wifiInfos = new ArrayList<XxWifiInfo>();
                    }
                    Collections.sort(wifiInfos, new XxCompareWifiUtils());
                    addSurplusWifiInfo(wifiInfos, saveResults);
                } else if (scanResults != null && saveResults == null) {
                    for (ScanResult scanResult : scanResults) {
                        XxWifiInfo item = new XxWifiInfo();
                        inflateData(item, scanResult);
                        if (wifiInfos == null) {
                            wifiInfos = new ArrayList<XxWifiInfo>();
                        }
                        wifiInfos.add(item);
                    }
                } else if (scanResults == null && saveResults != null) {
                    if (wifiInfos == null) {
                        wifiInfos = new ArrayList<XxWifiInfo>();
                    }
                    addSurplusWifiInfo(wifiInfos, saveResults);
                }
            }
        }
        return wifiInfos;
    }

    /**
     * @return 填充完的wifi信息
     * @author liyz
     * @desc 将网络wifi信息添加到本地wifi信息中
     */

    private void addSurplusWifiInfo(List<XxWifiInfo> wifiInfos,
                                    List<WifiConfiguration> wifiConfs) {
        for (WifiConfiguration wifiConf : wifiConfs) {
            XxWifiInfo wifiInfo = new XxWifiInfo();
            String ssId = wifiConf.SSID;
            wifiInfo.setName(ssId.substring(1, ssId.length() - 1));
            wifiInfo.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_NO_SCOPE);
            wifiInfo.setNetworkId(wifiConf.networkId);
            StringBuffer sBuf = new StringBuffer();
            BitSet protocol = wifiConf.allowedProtocols;
            if (protocol.get(WifiConfiguration.Protocol.RSN)
                    && protocol.get(WifiConfiguration.Protocol.WPA)) {
                sBuf.append("WPA/WPA2 PSK");
            } else if (protocol.get(WifiConfiguration.Protocol.RSN)) {
                sBuf.append("WPA2 PSK");
            } else if (protocol.get(WifiConfiguration.Protocol.WPA)) {
                sBuf.append("WPA PSK");
            } else {
                sBuf.append(" ");
            }
            wifiInfo.setCapabilities(sBuf.toString());
            wifiInfos.add(wifiInfo);
        }
    }

    /**
     * @param item scanresult
     * @return
     * @author liyz
     * @desc 填充基本信息
     */
    private boolean inflateData(XxWifiInfo item, ScanResult scanResult) {
        item.setCapabilities(scanResult.capabilities);
        item.setSignalLevel(scanResult.level); // level
        item.setName(scanResult.SSID); // name
        if (!parseWifiDesc(item)) { // 如果是企业级加密那么过滤不显示
            return false;
        }
        createEncryDesc(item);
        return true;
    }

    /**
     * @param item saveItem
     * @return boolean
     * @author liyz
     * @desc 检查可用的wifi是否与保存的wifi配置是否一致
     */
    private boolean isRemoveSaveItem(XxWifiInfo item, WifiConfiguration saveItem) {
        boolean bRemove = false;
        int saveKeyMgmt = 0;

        if (saveItem.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            if (saveItem.allowedProtocols.get(WifiConfiguration.Protocol.WPA)
                    && saveItem.allowedProtocols
                    .get(WifiConfiguration.Protocol.RSN)) {
                saveKeyMgmt |= XxWifiConst.XxWifiEncryConst.WPAPSK;
                saveKeyMgmt |= XxWifiConst.XxWifiEncryConst.WPA2PSK;
            } else if (saveItem.allowedProtocols
                    .get(WifiConfiguration.Protocol.WPA)) {
                saveKeyMgmt |= XxWifiConst.XxWifiEncryConst.WPAPSK;
            } else if (saveItem.allowedProtocols
                    .get(WifiConfiguration.Protocol.RSN)) {
                saveKeyMgmt |= XxWifiConst.XxWifiEncryConst.WPA2PSK;
            }
        }

        if (saveItem.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)
                && saveItem.allowedAuthAlgorithms
                .get(WifiConfiguration.AuthAlgorithm.OPEN)
                && saveItem.allowedAuthAlgorithms
                .get(WifiConfiguration.AuthAlgorithm.SHARED)) {
            saveKeyMgmt |= XxWifiConst.XxWifiEncryConst.WEP;
        } else if (saveItem.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.NONE)) {
            saveKeyMgmt |= XxWifiConst.XxWifiEncryConst.NONE;
        }

        if (saveKeyMgmt != item.getKeyMgmt()) {
            bRemove = true;
        }
        // if(saveItem.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK))
        // {
        // saveKeyMgmt |= XxWifiConst.XxWifiConst.XxWifiEncryConst.WPAPSK;
        // }
        //
        // if(saveItem.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA2_PSK))
        // {
        // saveKeyMgmt |= XxWifiConst.XxWifiConst.XxWifiEncryConst.WPAPSK;
        // }
        //
        // if(saveItem.allowedAuthAlgorithms.get(WifiConfiguration.KeyMgmt.NONE)
        // &&
        // saveItem.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.WEP40)
        // &&
        // saveItem.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.WEP104))
        // {
        // saveKeyMgmt |= XxWifiConst.XxWifiConst.XxWifiEncryConst.WEP;
        // }else
        // if(saveItem.allowedAuthAlgorithms.get(WifiConfiguration.KeyMgmt.NONE))
        // {
        // saveKeyMgmt |= XxWifiConst.XxWifiConst.XxWifiEncryConst.NONE;
        // }
        //
        // if(saveKeyMgmt != item.getKeyMgmt()) {
        // bRemove = true;
        // }
        return bRemove;
    }

    /**
     * @param item -保存部分数据的wifi信息
     * @author liyz
     * @desc 解析wifi描述
     */
    private boolean parseWifiDesc(XxWifiInfo item) {
        String desc = item.getCapabilities();
        int keyMgmt = XxWifiConst.XxWifiEncryConst.NONE;
        int cipher = XxWifiConst.XxWifiCipherConst.NONE;

        if (desc.contains("WEP")) {
            keyMgmt |= XxWifiConst.XxWifiEncryConst.WEP;
            item.setLocked(true);
        }

        if (desc.contains("WPA-PSK")) {
            keyMgmt |= XxWifiConst.XxWifiEncryConst.WPAPSK;
            item.setLocked(true);
        }

        if (desc.contains("WPA2-PSK")) {
            keyMgmt |= XxWifiConst.XxWifiEncryConst.WPA2PSK;
            item.setLocked(true);
        }

        if (desc.contains("WPA-EAP")) {
            return false;
        }

        if (desc.contains("WPA2-EAP")) {
            return false;
        }

        if (desc.contains("TKIP")) {
            cipher |= XxWifiConst.XxWifiCipherConst.TKIP;
        }

        if (desc.contains("CCMP")) {
            cipher |= XxWifiConst.XxWifiCipherConst.CCMP;
        }

        if (desc.contains("WPS")) {
            item.setWps(true);
        }
        item.setKeyMgmt(keyMgmt);
        item.setGroupCipher(cipher);
        return true;
    }

    /**
     * @param item
     * @return
     * @author liyz
     * @desc 更新新显示信息
     */
    private void updateEncryDesc(XxWifiInfo item) {
        if (item.getWifiEncryDesc() == XxWifiConst.XxWifiEncryDesc.WIFI_WPA_WPS
                || item.getWifiEncryDesc() == XxWifiConst.XxWifiEncryDesc.WIFI_WPA) {
            item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_SAVE_WPA);
        } else if (item.getWifiEncryDesc() == XxWifiConst.XxWifiEncryDesc.WIFI_WPA2_WPS
                || item.getWifiEncryDesc() == XxWifiConst.XxWifiEncryDesc.WIFI_WPA2) {
            item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_SAVE_WPA2);
        } else if (item.getWifiEncryDesc() == XxWifiConst.XxWifiEncryDesc.WIFI_WPA_WPA2_WPS
                || item.getWifiEncryDesc() == XxWifiConst.XxWifiEncryDesc.WIFI_WPA_WPA2) {
            item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_SAVE_WPA_WPA2);
        } else if (item.getWifiEncryDesc() == XxWifiConst.XxWifiEncryDesc.WIFI_WEP
                || item.getWifiEncryDesc() == XxWifiConst.XxWifiEncryDesc.WIFI_WEP_WPS) {
            item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_SAVE_WEP);
        } else if (item.getWifiEncryDesc() == XxWifiConst.XxWifiEncryDesc.WIFI_WPS
                || item.getWifiEncryDesc() == XxWifiConst.XxWifiEncryDesc.WIFI_NONE) {
            item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_SAVE);
        }
    }

    /**
     * @param item
     * @return
     * @author liyz
     * @desc 生成显示信息
     */
    private void createEncryDesc(XxWifiInfo item) {
        if (item.getKeyMgmt() == XxWifiConst.XxWifiEncryConst.NONE) {
            if (item.isWps()) {
                item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_WPS);
            } else {
                item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_NONE);
            }
        } else if (item.getKeyMgmt() == XxWifiConst.XxWifiEncryConst.WEP) {
            if (item.isWps()) {
                item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_WEP_WPS);
            } else {
                item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_WEP);
            }
        } else if (item.getKeyMgmt() == (XxWifiConst.XxWifiEncryConst.WPAPSK | XxWifiConst.XxWifiEncryConst.WPA2PSK)) {
            if (item.isWps()) {
                item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_WPA_WPA2_WPS);
            } else {
                item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_WPA_WPA2);
            }
        } else if (item.getKeyMgmt() == XxWifiConst.XxWifiEncryConst.WPAPSK) {
            if (item.isWps()) {
                item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_WPA_WPS);
            } else {
                item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_WPA);
            }
        } else if (item.getKeyMgmt() == XxWifiConst.XxWifiEncryConst.WPA2PSK) {
            if (item.isWps()) {
                item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_WPA2_WPS);
            } else {
                item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_WPA2);
            }
        }
    }

    private boolean bThreadRunning = false;
    private boolean bTimerRun = false;

    /**
     * @param listener
     * @return
     * @author liyz
     * @desc 进入wifi列表界面
     */
    public void enterWifiDetail(IUpdateUI listener) {
        bInWifiListActivity = true;
        this.addUpdateUIListener(listener);
        if (mWifiUtils.isWifiEnabled()) {
            startRefresh();
        }
    }

    /**
     * @param
     * @return
     * @author liyz
     * @desc 开始刷新
     */
    public void startRefresh() {
        if (bInWifiListActivity) {
            bStartScan = mWifiUtils.startScan();
            startTimer();
            if (!bThreadRunning) {
                wifiViewThread.start();
            }
        }
    }

    /**
     * @param
     * @return
     * @author liyz
     * @desc 取消刷新
     */
    public void cancelRefresh() {
        if (bInWifiListActivity) {
            cancelTimer();
        }
    }

    /**
     * @param
     * @return
     * @author liyz
     * @desc 开始刷新计时器
     */
    public void startTimer() {
        if (!bTimerRun) {
            mScanTimer = new Timer();
            mScanTimer.schedule(new StartScanTimerTask(), 0, 4000);
            bTimerRun = true;
        }
    }

    /**
     * @param
     * @return
     * @author liyz
     * @desc 取消刷新计时器
     */

    public void cancelTimer() {
        if (bTimerRun) {
            mScanTimer.cancel();
            bTimerRun = false;
        }
    }

    /**
     * @param
     * @return
     * @author liyz
     * @desc 重启刷新计时器
     */
    public void restartTimer() {
        cancelTimer();
        startTimer();
    }

    /**
     * @param
     * @return
     * @author liyz
     * @desc 离开wifi列表界面
     */
    public void leaveWifiDetail(IUpdateUI listener) {
        this.removeUpdateUIListener(listener);
        cancelRefresh();
        bInWifiListActivity = false;
    }

    /**
     * @param
     * @return
     * @author liyz
     * @desc 开始刷新
     */
    public synchronized void scanWifiFinish() {
        if (bInWifiListActivity) {
            notify();
        }
    }

    /**
     * @param
     * @return
     * @author liyz
     * @desc 更新界面
     */
    public synchronized void updateUI() {
        List<XxWifiInfo> wifiInfos = updateWifiInfos(false);
        try {
            if (bStartScan) {
                bStartScan = false;
                if (wifiInfos != null /* && lastWifiListSize != wifiInfos.size() */) {
                    Message msg = mHandler.obtainMessage(
                            XxMessage.MSG_SETTING_RE_WIFILIST, wifiInfos);
                    mHandler.sendMessageAtFrontOfQueue(msg);
                }
            }
            wait();
        } catch (InterruptedException e) {
        }
    }

    /**
     * @param item
     * @return
     * @author liyz
     * @desc 刷新连接中的wifi描述信息
     */
    public void updateConnectingItemDesc(XxWifiInfo item) {
        WifiInfo wifiInfo = mWifiUtils.getConnectionInfo();
        if (wifiInfo == null) {
            return;
        }
        SupplicantState st = wifiInfo.getSupplicantState();
        DetailedState state = WifiInfo.getDetailedStateOf(st);
        if (state == DetailedState.OBTAINING_IPADDR) {
            item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_GET_ADDRESS);
            if (mNetManager.getConnectivityManager().getStateByConnType(
                    ConnectivityManager.TYPE_WIFI) == State.CONNECTED) {
                item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_CONNECTED);
            }
        } else if (state == DetailedState.DISCONNECTED) {
            item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_VERIFY_FAILED);
        } else {
            item.setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc.WIFI_VERIFYING);
        }
    }

    public void updateConnectingDesc() {
        WifiInfo wifiInfo = mWifiUtils.getConnectionInfo();
        if (wifiInfo == null) {
            return;
        }

        SupplicantState st = wifiInfo.getSupplicantState();
        DetailedState state = WifiInfo.getDetailedStateOf(st);
        if (st == SupplicantState.INACTIVE || st == SupplicantState.SCANNING) {
            if (bHasAutoConnect) {
                bHasAutoConnect = false;
                autoConnect();
            }
            return;
        }
        // if(mConnName != null && !mConnName.equals(wifiInfo.getSSID())) {
        // startScan();
        // }
        if (mConnName != null) {
            if (state == DetailedState.OBTAINING_IPADDR) {
                mConnEncryDesc = XxWifiConst.XxWifiEncryDesc.WIFI_GET_ADDRESS;

            } else if (state == DetailedState.DISCONNECTED) {
                if (mConnName.replaceAll("\"", "").equals(
                        wifiInfo.getSSID().replaceAll("\"", ""))) {
                    mConnEncryDesc = XxWifiConst.XxWifiEncryDesc.WIFI_VERIFY_FAILED;
                    bHasAutoConnect = true;
                }
                if (!mConnectFailedList.contains(mConnName)) {
                    mConnectFailedList.add(mConnName);
                }
            } else {
                mConnEncryDesc = XxWifiConst.XxWifiEncryDesc.WIFI_VERIFYING;
            }
            Message msg = mHandler
                    .obtainMessage(XxMessage.MSG_SETTING_RE_WIFILIST_DESC);
            if (st == SupplicantState.ASSOCIATING
                    || state == DetailedState.AUTHENTICATING) {
                List<XxWifiInfo> lists = updateWifiInfos(true);
                msg.obj = lists;
            }
            mHandler.sendMessage(msg);

        }
    }

    public XxWifiConst.XxWifiEncryDesc getConnEncryDesc() {
        return mConnEncryDesc;
    }


    private class RefreshWifiInfosThread implements Runnable {
        @Override
        public void run() {
            bThreadRunning = true;
            while (bInWifiListActivity) {
                updateUI();
            }
            bThreadRunning = false;
        }
    }

    private class StartScanTimerTask extends TimerTask {
        @Override
        public void run() {
            startScan();
        }
    }

    public int getWifiStatus() {
        return mWifiUtils.getWifiState();
    }

    public boolean isWifiEnabled() {
        return mWifiUtils.isWifiEnabled();
    }

    public void openWifi() {
        mWifiUtils.openWifi();
    }

    public void closeWifi() {
        mWifiUtils.closeWifi();
    }

    /**
     * @param
     * @return
     * @author liyz
     * @desc 请求刷新
     */
    public void startScan() {
        if (bInWifiListActivity) {
            bStartScan = mWifiUtils.startScan();
        }
    }

    public WifiInfo getConnInfo() {
        return mWifiUtils.getConnectionInfo();
    }

    public String getConnName() {
        return mConnName;
    }

    public boolean isInWifiListActivity() {
        return bInWifiListActivity;
    }

    public int getConnectionWifiStrength() {
        WifiInfo info = mWifiUtils.getConnectionInfo();
        if (info == null) {
            return 1;
        }
        return info.getRssi();
    }
}
