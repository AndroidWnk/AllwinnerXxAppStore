package com.e_trans.xxappstore.top;

import android.content.Context;
import android.media.AudioManager;
import android.os.Message;


public class XxOtherSettingManager extends XxBaseModule {
    private XxVoiceUtils mVoiceUtils;

    public XxVoiceUtils getVoiceUtils() {
        return mVoiceUtils;
    }

    private static class XxOtherSettingManagerInstance {
        private static final XxOtherSettingManager mOtherSettingManager = new XxOtherSettingManager();
    }

    public static XxOtherSettingManager getInstance() {
        return XxOtherSettingManagerInstance.mOtherSettingManager;
    }

    public XxOtherSettingManager() {
        mVoiceUtils = new XxVoiceUtils();
    }

    public XxOtherSettingManager init(Context context) {
        super.init(context);
        mVoiceUtils.init(context);
        if (XxConfig.getInstance().isVoiceAutoMute()) {
            mVoiceUtils.setStreamMute(AudioManager.STREAM_MUSIC, true);
        } else {
            mVoiceUtils.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
        return this;
    }

    private boolean gbMute;
    public boolean isMute() {
        return gbMute;
    }
    public void changeVoiceStatus(boolean bmute) {
        gbMute = bmute;
        Message msg = mHandler.obtainMessage(XxMessage.MSG_SETTING_RE_VOICE);
        msg.arg1 = AudioManager.STREAM_MUSIC;
        msg.obj = bmute;
        mHandler.sendMessage(msg);
    }
}
