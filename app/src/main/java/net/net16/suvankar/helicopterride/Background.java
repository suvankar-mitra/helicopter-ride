package net.net16.suvankar.helicopterride;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.io.Serializable;

/**
 * Created by suvankar on 11/1/17.
 */

public class Background implements Serializable{

    private Bitmap image;
    private int x;
    private int y;
    private int dx; //movement

    //constructor
    public Background(Bitmap bitmap) {
        this.image = bitmap;
        dx = GamePanel.MOVE_SPEED;
    }

    public void update() {
        //scroll the background horizontally
        x+=dx;
        if(x< -(GamePanel.WIDTH))
            x=0;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(image,x,y,null);
        //what happens to the empty space when the image moves left
        //we fill that part with the same image
        if(x<0) {
            canvas.drawBitmap(image, x+GamePanel.WIDTH, y, null);
        }
    }
}
