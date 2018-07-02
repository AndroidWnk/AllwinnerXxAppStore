package com.e_trans.xxappstore.top;

/**
 * 界面刷新消息定义 * 
 */
public class XxMessage
{
	
	public final static int MSG_SYSTEM_BASE = 100;
	public final static int MSG_SYSTEM_ROTATE = MSG_SYSTEM_BASE + 1;
	
	public final static int MSG_MUSIC_BASE = 1000;	
	public final static int MSG_PROGRESS_UPDATE				= MSG_MUSIC_BASE + 7;
	public final static int MSG_PLAY_COMPLETE 				= MSG_MUSIC_BASE + 8;
	public final static int MSG_PLAYINFO_UPDATE 			= MSG_MUSIC_BASE + 9;	
	public final static int MSG_LRC_INIT_FINISH 			= MSG_MUSIC_BASE + 10;
	public final static int MSG_LRC_REFRESH 				= MSG_MUSIC_BASE + 11;
	public final static int MSG_PLAY_BUFFERED 				= MSG_MUSIC_BASE + 12;
	public final static int MSG_PLAYLIST_LOADED 			= MSG_MUSIC_BASE + 13;
	public final static int MSG_PLAY_START 					= MSG_MUSIC_BASE + 14;
	public final static int MSG_NETREQUEST_START			= MSG_MUSIC_BASE + 15;
	public final static int MSG_NETREQUEST_FINISH 			= MSG_MUSIC_BASE + 16;
	public final static int MSG_NETREQUEST_ERROR 			= MSG_MUSIC_BASE + 17;
	public final static int MSG_NETREQUEST_TIMEOUT 			= MSG_MUSIC_BASE + 18;
	public final static int MSG_PLAYLIST_NUM 				= MSG_MUSIC_BASE + 19;
	public final static int MSG_PLAY_PAUSE 					= MSG_MUSIC_BASE + 20;
	public final static int MSG_PLAY_STOP 					= MSG_MUSIC_BASE + 21;
	public final static int MSG_LOCAL_LIST_REFRESH_START	= MSG_MUSIC_BASE + 22;
	public final static int MSG_LOCAL_LIST_REFRESH_FINISH 	= MSG_MUSIC_BASE + 23;
	public final static int MSG_VIDEO_PLAY_STOP 			= MSG_MUSIC_BASE + 24;
	public final static int MSG_CYCLE_MODE_SET				= MSG_MUSIC_BASE + 25;
	public final static int MSG_PLAY_RESUME					= MSG_MUSIC_BASE + 26;
	public final static int MSG_MUSIC_CONTROL_CONNECTED		= MSG_MUSIC_BASE + 27;
	public final static int MSG_VIDEO_LIST_REFRESH			= MSG_MUSIC_BASE + 28;
	public final static int MSG_HIDE_KEYBOARD				= MSG_MUSIC_BASE + 29;
	public final static int MSG_VIDEO_PAUSE					= MSG_MUSIC_BASE + 30;
	public final static int MSG_VIDEO_RESUME				= MSG_MUSIC_BASE + 31;
	public final static int MSG_VOICE_VIDEO_OPEN			= MSG_MUSIC_BASE + 32;
	public final static int MSG_VOICE_VIDEO_CLOSE			= MSG_MUSIC_BASE + 33;
	public final static int MSG_MUSIC_PLAY_ERROR			= MSG_MUSIC_BASE + 34;

	public final static int MSG_DOWNLOAD_STATE				= MSG_MUSIC_BASE + 35;
	public final static int MSG_DOWNLOAD_PROGRESS			= MSG_MUSIC_BASE + 36;	
	
	public final static int MSG_REPORT_NETREQUEST_FINISH 	= MSG_MUSIC_BASE + 37;
	public final static int MSG_REPORT_NETREQUEST_ERROR 	= MSG_MUSIC_BASE + 38;
	public final static int MSG_REPORT_NETREQUEST_TIMEOUT 	= MSG_MUSIC_BASE + 39;
	
	public final static int MSG_DITRICTCITY 				= MSG_MUSIC_BASE + 40;
	
	public final static int MSG_CAN_VIDEO_EXIT				= MSG_MUSIC_BASE + 41;
	
	public final static int MSG_DOWNLOAD_SPACE  			= MSG_MUSIC_BASE + 42;
	public final static int MSG_VOICE_MUSIC_CLOSE 			= MSG_MUSIC_BASE + 43;
	public final static int MSG_CONNECTIVITY_ACTION_OK		= MSG_MUSIC_BASE + 44;
	public final static int MSG_GPS_AVAILABLE				= MSG_MUSIC_BASE + 45;
	public final static int MSG_GPS_UNAVAILABLE				= MSG_MUSIC_BASE + 46;		

	public final static int MSG_REQUESTERROR				= MSG_MUSIC_BASE + 47;
	public final static int MSG_WEATHER_CMD				= MSG_MUSIC_BASE + 48;
	
	public final static int MSG_MUSIC_BUFFERUPDATE 			= MSG_MUSIC_BASE + 49;
	public final static int MSG_DATE_CHANGE    			    = MSG_MUSIC_BASE + 50;
	public final static int MSG_REPORT_NETCURENT_FINISH    	= MSG_MUSIC_BASE + 51;
	public final static int MSG_NETWORK_DISCONNECT = MSG_MUSIC_BASE+1;
	public final static int MSG_MUSIC_PLAYING_NEXT			= MSG_MUSIC_BASE + 53;
	public final static int MSG_MUSIC_PLAYING_PERVIOUS		= MSG_MUSIC_BASE + 54;
	public final static int MSG_MUSIC_3G_NETWORK_DISCONNECT		= MSG_MUSIC_BASE + 55;
	public final static int MSG_MUSIC_3G_NETWORK_WARNING		= MSG_MUSIC_BASE + 56;

	public final static int MSG_APP_BASE = 2000;
	public final static int MSG_APP_ONKEYDOWN = 2001;
	public final static int MSG_APP_REFRESH = MSG_APP_BASE + 2;

	public final static int MSG_APP_CLOSE_UI = MSG_APP_BASE + 3;



	public final static int MSG_BTEARPHONE_BASE = 3001;
	
	public final static int MSG_BTEARPHONE_OFF = 3002;
	
	public final static int MSG_BTEARPHONE_ON = 3003;
	
	public final static int MSG_BTEARPHONE_LINKED = 3004;
	
	public final static int MSG_BTEARPHONE_LINKEDCHANGED = 3005;
	
	public final static int MSG_BTEARPHONE_DOWNLOADBOOK_START = 3006;
	
	public final static int MSG_BTEARPHONE_DOWNLOADBOOK_FINISH = 3007;
	
	public final static int MSG_BTEARPHONE_BOOKCANDOWN = 3008;
	
	public final static int MSG_BTEARPHONE_TAKING = 3009;
	
	public final static int MSG_BTEARPHONE_CALL_PHONENUMBER = 3010;
	
	public final static int MSG_BTEARPHONE_HANGUP = 3011;
	
	public final static int MSG_BTEARPHONE_TIMER = 3012;
	
	public final static int MSG_BTEARPHONE_SECOND_CALLIN = 3013;
	
	public final static int MSG_BTEARPHONE_SECOND_PHONENUMBER = 3014;

	public final static int MSG_BTEARPHONE_NAME_CHANGE = 3015;
	
	public final static int MSG_BTEARPHONE_PASSWD_CHANGE = 3016;
	//
	public final static int MSG_BTEARPHONE_LOADING_CONTACT_TIMER = 3017;
	
	public final static int MSG_BTEARPHONE_DOWNLOADING_CONTACT = 3018;
	//蓝牙电话闲置状态
	public final static int MSG_BTEARPHONE_CALL_STATE_IDLE = 3019;
	//销毁拨号界面
	public final static int MSG_BTEARPHONE_FINISH_PHONE_ACTIVITY = 3020;
	
	public final static int MSG_BTEARPHONE_MEDIA_PLAY = 3021;
	
	public final static int MSG_BTEARPHONE_MEDIA_STOP = 3022;
	

	public final static int MSG_BTEARPHONE_DISCONNECT = 3023;
        public final static int MSG_BTEARPHONE_CLICK_HOME = 3028;
    	public final static int MSG_BTEARPHONE_CLICK_BACK = 3029;

	/**
	 * mic 状态 
	 */
	public final static int MSG_BTEARPHONE_MIC_STATE = 3024;

	public final static int MSG_BTEARPHONE_SWITCH_VOICE = 3025;

	public final static int MSG_BTEARPHONE_MUSICINFO = 3026;


	public final static int MSG_SETTING_BASE = 4000;
	
	public final static int MSG_SETTING_RE_WIFI = MSG_SETTING_BASE + 1;
	
	public final static int MSG_SETTING_RE_AP = MSG_SETTING_BASE + 2;
	
	public final static int MSG_SETTING_RE_WIFI_AND_AP = MSG_SETTING_BASE + 3;
	
	public final static int MSG_SETTING_RE_VOICE = MSG_SETTING_BASE + 4;
	
	public final static int MSG_SETTING_RE_LIGHT = MSG_SETTING_BASE + 5;
	
	public final static int MSG_SETTING_RE_BLUETOOTH = MSG_SETTING_BASE + 6;
	
	public final static int MSG_SETTING_RE_MOBILE_DATA = MSG_SETTING_BASE + 7;
	
	public final static int MSG_SETTING_RE_BG = MSG_SETTING_BASE + 8;
	
	public final static int MSG_SETTING_UPGRADE = MSG_SETTING_BASE + 9;
	
	public final static int MSG_SETTING_WIFILIST_TOP = MSG_SETTING_BASE + 10;
	
	public final static int MSG_SETTING_RE_WIFILIST = MSG_SETTING_BASE + 11;
	
	public final static int MSG_SETTING_RE_WIFILIST_DESC = MSG_SETTING_BASE + 12;
	
	public final static int MSG_RADIO_BASE = 5000;
	
	public final static int MSG_RADIO_STATUS_CHANGE = MSG_RADIO_BASE + 1;
	
	public final static int MSG_RADIO_START_AUTO = MSG_RADIO_BASE + 2;
	
	public final static int MSG_RADIO_END_AUTO = MSG_RADIO_BASE + 3;
	
	public final static int MSG_RADIO_START_SEARCH = MSG_RADIO_BASE + 4;
	
	public final static int MSG_RADIO_END_SEARCH = MSG_RADIO_BASE + 5 ;
	
	public final static int MSG_RADIO_CHANGE_FREQ = MSG_RADIO_BASE + 6;
	
	public final static int MSG_RADIO_AUTO_TEXT = MSG_RADIO_BASE + 7;
	
	public final static int MSG_RADIO_CHANGE_CHANNELNAME = MSG_RADIO_BASE + 8;
	
	public final static int MSG_RADIO_CHANGE_LIST_CHANNELNAME = MSG_RADIO_BASE + 9;
	
	public final static int MSG_RADIO_CHANGE_LIST_VIEW_CHANNELNAME = MSG_RADIO_BASE + 10;
	
	public final static int MSG_RADIO_FINISH_RADIO_ACTIVITY = MSG_RADIO_BASE + 11;
	
	public final static int MSG_SETTING_RE_VOICE_MUTE = MSG_SETTING_BASE + 12;
	
	public final static int MSG_SETTING_RE_LIGHT_AUTO = MSG_SETTING_BASE + 13;
	
	public final static int MSG_NOTIFICATION_BASE = 6000;
	public final static int MSG_NOTIFICATION_TIME = MSG_NOTIFICATION_BASE + 1;
	public final static int MSG_NOTIFICATION_MOBILE = MSG_NOTIFICATION_BASE +2;
	public final static int MSG_NOTIFICATION_GPS_STATE = MSG_NOTIFICATION_BASE + 3;

	
}
