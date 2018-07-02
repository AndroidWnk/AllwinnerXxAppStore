package com.e_trans.xxappstore.top;

import android.content.Context;
import android.media.AudioManager;

import cn.com.etrans.etsdk.manager.AudioExManager;


public class XxVoiceUtils {
    private AudioManager mAudioManager;
    private AudioExManager audioExManager;
    public int voiceUp = 1;
    public int voiceDown = 2;

    public XxVoiceUtils() {
    }

    public void init(final Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        new Thread(){
            @Override
            public void run() {
                super.run();
                audioExManager = new AudioExManager(context);
            }
        }.start();
//        audioExManager = (AudioExManager) context.getSystemService(Context.AUDIOEX_SERVICE);
    }

    public boolean isVoiceSenlice() {
        return (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0);
    }

    public int getStreamVoice(int streamType) {
        return mAudioManager.getStreamVolume(streamType);
    }

    public void setStreamVoice(int streamType, int size) {
        mAudioManager.setStreamVolume(streamType, size, 0);
    }

    public void setStreamVolume(int streamType, int index, int flags) {
        mAudioManager.setStreamVolume(streamType, index, flags);
    }

    public void setMusicVolumeLate(int upOrDown) {
        if (upOrDown == voiceUp) {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
        } else if (upOrDown == voiceDown) {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
        }
    }

    public void setRadioVolumeLate(int upOrDown) {
        if (upOrDown == voiceUp) {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_RADIO,
                    AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
        } else if (upOrDown == voiceDown) {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_RADIO,
                    AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
        }
    }


    public void setSimuLateVolume(int index) {
        if (audioExManager != null) {
            //audioExManager.set_simulateVolume(index);

        }
    }


    public void setStreamMute(int streamType, boolean bMute) {
        if (bMute) {
            XxConfig.getInstance().setVoiceAutoMute(true);
        } else {
            XxConfig.getInstance().setVoiceAutoMute(false);
        }
        mAudioManager.setStreamMute(streamType, bMute);
    }

    public boolean isMusicActive() {
        return mAudioManager.isMusicActive();
    }

    public int getMaxVolume(int streamType) {
        return mAudioManager.getStreamMaxVolume(streamType);
    }

    public void setRingerMode(int ringerMode) {
        mAudioManager.setRingerMode(ringerMode);
    }


    public void setRadioLosesFocus(boolean on) {
        mAudioManager.setRadioLosesFocus(on);
    }

}
