package net.net16.suvankar.helicopterride;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by suvankar on 12/1/17.
 */

public class Missile extends GameObject {
    private int score;
    private int speed;
    private Random random;
    private Animation animation;
    private Bitmap spritesheet;
    private int BASE_SPEED = 5;
    public Missile(Bitmap[] res, int x, int y, int h, int w, int s) {
        super.x = x;
        super.y = y;
        super.width = w;
        super.height = h;
        this.score = s;

        random = new Random();
        animation = new Animation();
        speed = BASE_SPEED + (int)(random.nextDouble()*score/50);

        //cap the speed
        if(speed>=20)   speed = 20;
        animation.setFrames(res);
        animation.setDelay(100 - speed);
    }

    public void update() {
        x -= speed;
        animation.update();
    }

    public void draw(Canvas canvas) {
        try {
            canvas.drawBitmap(animation.getImage(),x,y,null);
        }catch(Exception e){}
    }

    @Override
    public int getWidth() {
        //offset the mWidth to make it realistic, like if helicopter collides with tail of missile
        //it should not explode
        return width - 10;
    }

    @Override
    public Rect getRectangle() {
        return new Rect(x,y,x+width-20,y+height-10);
    }
}
