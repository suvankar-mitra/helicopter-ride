package net.net16.suvankar.helicopterride;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;

/**
 * Created by suvankar on 21/1/17.
 */

public class Fuel extends GameObject {
    private boolean consumed;
    private Bitmap[] image;
    private Animation animation = new Animation();
    public Fuel(int x, int y, int w, int h, @NonNull Bitmap[] bitmap){
        super.x = x;
        super.y = y;
        super.width = w;
        super.height = h;
        super.dx = GamePanel.MOVE_SPEED;
        this.image = bitmap;
        animation.setFrames(image);
        animation.setDelay(200);
    }

    public void update() {
        x += dx;
        animation.update();
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(animation.getImage(),x,y,null);
    }

    @Override
    public Rect getRectangle() {
        return new Rect(x,y,x+width-40,y+height-20);
    }
}
