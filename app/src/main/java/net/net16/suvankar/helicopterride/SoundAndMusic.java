package net.net16.suvankar.helicopterride;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by suvankarmitra on 29/1/2017.
 */

public abstract class SoundAndMusic {
    private MediaPlayer mp;
    private boolean isPlaying = false;
    private boolean setLoop = false;
    private float volume = 1f;
    private AssetFileDescriptor afd;
    public SoundAndMusic(Context context, int rawId) {
        this.setLoop = false;
        mp = MediaPlayer.create(context, rawId);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            AssetManager am=context.getAssets();
            afd = am.openFd("android.resource://"+
                    context.getPackageName()+"/"+rawId);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
        } catch (Exception e){}
    }
    public SoundAndMusic(Context context, boolean loop, int rawId) {
        this.setLoop = loop;
        mp = MediaPlayer.create(context, rawId);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            AssetManager am=context.getAssets();
            afd = am.openFd("android.resource://"+
                    context.getPackageName()+"/"+rawId);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
        } catch (Exception e){}
    }
    public SoundAndMusic(Context context, boolean loop, float volume, int rawId) {
        this.setLoop = loop;
        this.volume = volume;
        mp = MediaPlayer.create(context, rawId);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            AssetManager am=context.getAssets();
            afd = am.openFd("android.resource://"+
                    context.getPackageName()+"/"+rawId);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
        } catch (Exception e){}
    }
    public SoundAndMusic(Context context, float volume, int rawId) {
        this.setLoop = false;
        this.volume = volume;
        mp = MediaPlayer.create(context, rawId);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            AssetManager am=context.getAssets();
            afd = am.openFd("android.resource://"+
                    context.getPackageName()+"/"+rawId);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
        } catch (Exception e){}
    }

    public void play() {
        mp.setLooping(setLoop);
        mp.setVolume(volume,volume);
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
