package com.e_trans.xxappstore.top;
/**
 * @file WifiInfoEx.java 
 * @date 2015-2-9
 * @author liyz
 * @desc  wifi 信息类
 */
public class XxWifiInfo {
	
	private String capabilities; //协议描述
	private int keyMgmt;  //加密方式  0 为加密  1.wpa  2.wpa2  3.wep  4.Eap
	private int authAlgorithm ; //wpa wpa2  = OPEN   wep = SHARED     
	private boolean wps ;     //是否可用wps
	private int groupCipher; 
	private int signalLevel;  // 信号等级
	private XxWifiConst.XxWifiEncryDesc wifiEncryDesc;
	private String name ; //名字
	private boolean bLocked;
	private boolean saved; //是否保存
	private boolean connected; //是否为当前连接
	private int networkId = -1;    //id
	
	public String getCapabilities() {
		return capabilities;
	}
	public void setCapabilities(String capabilities) {
		this.capabilities = capabilities;
	}
	
	public int getKeyMgmt() {
		return keyMgmt;
	}
	public void setKeyMgmt(int keyMgmt) {
		this.keyMgmt = keyMgmt;
	}

	public int getAuthAlgorithm() {
		return authAlgorithm;
	}
	public void setAuthAlgorithm(int authAlgorithm) {
		this.authAlgorithm = authAlgorithm;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isSaved() {
		return saved;
	}
	public void setSaved(boolean saved) {
		this.saved = saved;
	}
	public boolean isConnected() {
		return connected;
	}
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	public int getNetworkId() {
		return networkId;
	}
	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}
	public int getSignalLevel() {
		return signalLevel;
	}
	public void setSignalLevel(int signalLevel) {
		this.signalLevel = signalLevel;
	}
	public boolean isWps() {
		return wps;
	}
	public void setWps(boolean wps) {
		this.wps = wps;
	}
	public XxWifiConst.XxWifiEncryDesc getWifiEncryDesc() {
		return wifiEncryDesc;
	}
	public void setWifiEncryDesc(XxWifiConst.XxWifiEncryDesc wifiEncryDesc) {
		this.wifiEncryDesc = wifiEncryDesc;
	}
	
	public int getGroupCipher() {
		return groupCipher;
	}
	public void setGroupCipher(int groupCipher) {
		this.groupCipher = groupCipher;
	}
	public boolean isLocked() {
		return bLocked;
	}
	public void setLocked(boolean bLocked) {
		this.bLocked = bLocked;
	}
}
