package net.net16.suvankar.helicopterride;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.io.Serializable;

/**
 * Created by suvankar on 13/1/17.
 */

public class BottomBorder extends GameObject implements Serializable{
    private Bitmap image;

    public BottomBorder(Bitmap res, int x, int y, int h) {
        height = h;
        width = 20; //from brick.png
        this.x = x;
        this.y = y;
        dx = GamePanel.MOVE_SPEED;
        image = Bitmap.createBitmap(res,0,0,width,height);
    }

    public void update() {
        x += dx;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(image,x,y,null);
    }
}
