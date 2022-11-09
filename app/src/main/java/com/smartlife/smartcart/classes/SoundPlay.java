package com.smartlife.smartcart.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;
import java.util.HashMap;

public class SoundPlay {
    private final String TAG;
    private final float WARN_VOLUME;
    private int loopPlayID;
    private int normalPlayID;
    private int normalSoundId;
    private long normalTime;
    private MediaPlayer player;
    private int priority;
    private SoundPool soundPool;
    private HashMap<Integer, Integer> soundPoolMap;

    public static SoundPlay getInstance() {
        return Singleton.APP_DATA.getInstance();
    }

    /* renamed from: cn.chaohi.shopping.managers.sound.SoundPlay$Singleton */
    private enum Singleton {
        APP_DATA;

        private SoundPlay singleton;

        public SoundPlay getInstance() {
            return this.singleton;
        }
    }

    private SoundPlay() {
        this.WARN_VOLUME = 0.8f;
        this.TAG = "wxy-SoundPlay";
        this.priority = 1;
        this.soundPoolMap = null;
        this.loopPlayID = 0;
        this.normalPlayID = 0;
        this.normalSoundId = -1;
        this.normalTime = 0;
        this.soundPool = new SoundPool(15, 3, 100);
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            }
        });
        this.player = new MediaPlayer();
    }

    public void initSound(Context context) {
        if (this.soundPoolMap == null) {
            initSoundPlay(context);
        }
    }

    @SuppressLint({"UseSparseArrays"})
    private void initSoundPlay(Context context) {
        this.soundPoolMap = new HashMap<>();
        for (SoundsEnum soundsEnum : SoundsEnum.values()) {
            this.soundPoolMap.put(Integer.valueOf(soundsEnum.getCode()), Integer.valueOf(this.soundPool.load(context, soundsEnum.getResourceId(), this.priority)));
        }
    }

    public synchronized int playSound(SoundsEnum soundsEnum) {
        int p;
        if (!isCanPlaySpund(soundsEnum)) {
            p = -1;
        } else {
            p = 0;
            int sound = soundsEnum.getCode();
            int type = soundsEnum.getType();
            Log.d("SoundPlay", "playSound(" + sound + ")-start-normalPlayID =" + this.normalPlayID);
            if (type == 2) {
                Log.w("wxy-SoundPlay", "playSound()---播放铃声-start");
                p = playSounds(soundsEnum, getVolume(), 0);
                Log.w("wxy-SoundPlay", "playSound()---播放铃声-end-PayId=" + p);
            } else {
                Log.w("wxy-SoundPlay", "playSound()---播放语音-start");
                if (sound != this.normalSoundId || Math.abs(this.normalTime - System.currentTimeMillis()) > 2500) {
                    Log.w("wxy-SoundPlay", "playSound()---播放语音-满足条件");
                    stopPlay(this.normalPlayID);
                    this.normalSoundId = sound;
                    float volume = getVolume();
                    this.normalTime = System.currentTimeMillis();
                    this.normalPlayID = playSounds(soundsEnum, volume, 0);
                    p = this.normalPlayID;
                    Log.w("wxy-SoundPlay", "playSound()---播放语音-start");
                }
            }
            Log.d("SoundPlay", "playSound(" + sound + ")-end-normalPlayID =" + this.normalPlayID);
        }
        return p;
    }

    private boolean isCanPlaySpund(SoundsEnum soundsEnum) {
        switch (soundsEnum) {

            case SOUNDS_SCAN_SUCCESS:

                return true;
            default:
                return false;
        }
    }

    public synchronized int playSoundLoop(SoundsEnum soundsEnum, int loopCount) {
        int sound = soundsEnum.getCode();
        Log.d("SoundPlay", "playSoundLoop(" + sound + "," + loopCount + ")-start-loopPlayID =" + this.loopPlayID);
        stopPlay(this.loopPlayID);
        this.loopPlayID = playSounds(soundsEnum, getVolume(), loopCount - 1);
        Log.d("SoundPlay", "playSoundLoop(" + sound + "," + loopCount + ")-end-loopPlayID =" + this.loopPlayID);
        return this.loopPlayID;
    }

    public void playByPath(String path) {
        Log.w("wxy-SoundPlay", "playByPath(" + path + ")-start");

        if (this.player != null) {
            boolean isPlaying = false;
            try {
                isPlaying = this.player.isPlaying();
            } catch (IllegalStateException e) {
                this.player = null;
                this.player = new MediaPlayer();
            }
            if (!isPlaying) {
                try {
                    this.player.reset();
                    this.player.setDataSource(path);
                    this.player.prepare();
                    this.player.start();
                } catch (Exception e2) {
                    Log.w("wxy-SoundPlay", "IOException=" + e2.getMessage());

                }
                Log.w("wxy-SoundPlay", "playByPath(" + path + ")-end");

            }
        }
    }

    public void stopPlayByPath() {
        if (this.player != null) {
            try {
                if (this.player.isPlaying()) {
                    this.player.stop();
                    this.player.release();
                }
            } catch (IllegalStateException e) {
            }
        }
    }

    private synchronized int playSounds(SoundsEnum soundsEnum, float volume, int loop) {
        int i;
        Integer sId = this.soundPoolMap.get(Integer.valueOf(soundsEnum.getCode()));
        if (sId != null) {
            i = this.soundPool.play(sId.intValue(), volume, volume, this.priority, loop, 1.0f);
        } else {
            i = -1;
        }
        return i;
    }

    public void stopPlay(int turnSignalId) {
        if (turnSignalId != 0) {
            try {
                this.soundPool.stop(turnSignalId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private float getVolume() {
        return 20.0f;//((float) (UserData.volume * MarketData.systemVolume)) / 10000.0f;
    }
}
