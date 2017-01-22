package net.net16.suvankar.helicopterride;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by suvankar on 13/1/17.
 */

public class TopWall extends GameObject {
    private Bitmap image;

    public TopWall(Bitmap res, int x, int y, int h) {
        height = h;
        width = 20; //from brick.png
        super.x = x;
        super.y = y;
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
