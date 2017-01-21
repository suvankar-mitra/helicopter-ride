package net.net16.suvankar.helicopterride;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.io.Serializable;

/**
 * Created by suvankar on 13/1/17.
 */

public class Explosion implements Serializable{
    private int x;
    private int y;
    private int width;
    private int height;
    private Animation animation;
    private Bitmap spritesheet;
    private int row = 0;
    private Bitmap boom;

    public Explosion(Bitmap res, int x, int y, int w, int h, int numFrames, Bitmap boom){
        animation = new Animation();
        spritesheet = res;
        this.boom = boom;
        this.x = x;
        this.y = y;
        width = w;
        height = h;
        Bitmap[] image = new Bitmap[numFrames];
        for (int i=0; i<image.length; i++) {
            if(i%5==0 && i>0)  row++;
            image[i] = Bitmap.createBitmap(spritesheet, (i-(5*row))*width, row*height, width, height);
        }
        animation.setFrames(image);
        animation.setDelay(10);
    }

    public void update() {
        if(!animation.isPlayedOnce()) {
            animation.update();
        }
    }

    public void draw(Canvas canvas) {
        if(!animation.isPlayedOnce()) {
            canvas.drawBitmap(animation.getImage(),x,y,null);
            canvas.drawBitmap(boom,x+10, y+20, null);
            //Log.d("collision","draw");
        }
    }

    public int getHeight() {
        return height;
    }
}
