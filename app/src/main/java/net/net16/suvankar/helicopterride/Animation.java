package net.net16.suvankar.helicopterride;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by suvankar on 11/1/17.
 */

public class Animation{
    private Bitmap[] frames;
    private int currentFrame;
    private long startTime;
    private long delay;
    private boolean playedOnce;

    public void setFrames(@NonNull Bitmap[] frames) {
        this.frames = frames;
        currentFrame = 0;
        startTime = System.nanoTime();
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setFrame(int i) {
        currentFrame = i;
    }

    public void update() {
        long elapsed = (System.nanoTime() - startTime)/1000000;
        if(elapsed>delay) {
            currentFrame ++;
            startTime = System.nanoTime();
        }
        if(currentFrame == frames.length) {
            playedOnce = true;
            currentFrame = 0;
        }
    }

    public Bitmap getImage() {
        return frames[currentFrame];
    }

    public int getFrame() {
        return currentFrame;
    }

    public boolean isPlayedOnce() {
        return playedOnce;
    }
}
