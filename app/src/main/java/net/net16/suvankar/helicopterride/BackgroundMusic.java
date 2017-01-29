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

public class BackgroundMusic extends SoundAndMusic{

    public BackgroundMusic(Context context) {
        super(context, true, 0.4f, R.raw.cartoony);
    }
}
