package com.smartlife.smartcart.classes;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import com.smartlife.smartcart.R;

import java.util.HashMap;

public class SoundPoolPlayer {
    private SoundPool mShortPlayer= null;
    private HashMap mSounds = new HashMap();

    public SoundPoolPlayer(Context pContext)
    {
        // setup Soundpool
        this.mShortPlayer = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        mSounds.put(R.raw.scan_success, this.mShortPlayer.load(pContext, R.raw.scan_success, 1));
        mSounds.put(R.raw.bell_put_warn, this.mShortPlayer.load(pContext, R.raw.bell_put_warn, 1));

    }

    public void playShortResource(int piResource) {
        int iSoundId = (Integer) mSounds.get(piResource);
        this.mShortPlayer.play(iSoundId, 0.99f, 0.99f, 0, 0, 1);
    }

    // Cleanup
    public void release() {
        // Cleanup
        this.mShortPlayer.release();
        this.mShortPlayer = null;
    }
}
