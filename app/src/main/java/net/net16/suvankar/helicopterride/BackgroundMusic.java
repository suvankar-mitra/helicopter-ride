package net.net16.suvankar.helicopterride;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by suvankar on 16/1/17.
 */

public class BackgroundMusic {
    private MediaPlayer mp;
    private boolean isPlaying = false;
    public BackgroundMusic(Context context) {
        mp = MediaPlayer.create(context, R.raw.cartoony);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            AssetManager am=context.getAssets();
            AssetFileDescriptor afd = am.openFd("android.resource://"+
                    context.getPackageName()+"/"+R.raw.cartoony);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
        } catch (Exception e){}
    }

    public void play() {
        mp.setLooping(true);
        mp.setVolume(0.5f,0.5f);
        mp.start();
        isPlaying = true;
    }

    public void pause() {
        mp.pause();
        isPlaying = false;
        Log.d("MUSIC","paused");
    }

    public void reset() {
        mp.seekTo(0);
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void stop() {
        if(mp!=null) {
            mp.stop();
            mp.release();
            Log.d("MUSIC","stopped");
        }
    }
}
