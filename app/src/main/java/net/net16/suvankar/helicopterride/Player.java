package net.net16.suvankar.helicopterride;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created by suvankar on 11/1/17.
 */

public class Player extends GameObject{
    private int score;
    private int hiscore;    //this will not be deleted even if the activity gets killed
    private int level = 1;
    private int oldLevel = 1;
    private int fuelGauge;
    private final int MAX_FUEL = 200;
    public static final int FUEL_INCREASE = 60;
    private boolean up;     //up or down movement
    private boolean playing;
    private Animation animation = new Animation();
    private long startTime;
    private boolean gameOver = false;
    private float dyChange = 0.35f;
    private final int FUEL_X = 12;
    private final int FUEL_Y = 32;
    private float fuelEmptyRate = -2;
    private boolean levelUp = false;
    private Bitmap levelUpBitmap;
    public static final int X = 100;


    public Player(int w, int h, Bitmap[] sprites, Bitmap levelUp) {
        x = this.X;
        y = GamePanel.HEIGHT - (GamePanel.HEIGHT*3)/4;
        dy = 0;
        score = 0;
        height = h;
        width = w;
        animation.setFrames(sprites);
        animation.setDelay(10);
        startTime = System.nanoTime();
        fuelGauge = MAX_FUEL;
        levelUpBitmap = levelUp;
    }

    public void update() {
        //increment the score
        long elapsed = (System.nanoTime() - startTime)/1000000;
        if(elapsed>600) {
            score++;
            startTime = System.nanoTime();
            fuelGauge += fuelEmptyRate;
            if(fuelGauge<0)
                fuelGauge = 0;
        }

        //set the level
        level = score / 100 + 1;
        //when level is changing
        if(level - oldLevel>0) {
            levelUp = true;
            oldLevel = level;
            Log.d("LEVEL","Level:OldLevel="+level+":"+oldLevel);
            switch (level) {
                case 1:
                    dyChange = 0.35f;
                    fuelEmptyRate = -2;
                    break;
                case 2:
                    dyChange = 0.4f;
                    fuelEmptyRate = -2.5f;
                    break;
                case 3:
                    dyChange = 0.45f;
                    fuelEmptyRate = -3;
                    break;
                case 4:
                    dyChange = 0.5f;
                    fuelEmptyRate = -3.5f;
                    break;
                case 5:
                    dyChange = 0.55f;
                    fuelEmptyRate = -4;
                    break;
                case 6:
                    dyChange = 0.6f;
                    fuelEmptyRate = -5f;
                    break;
                case 7:
                    dyChange = 0.65f;
                    fuelEmptyRate = -6f;
                    break;
                default:
                    dyChange = 0.7f;
                    fuelEmptyRate = -7f;
                    break;
            }
        }

        //update the animation of bitmap -- helicopter
        animation.update();

        if(up) {
            dy -= dyChange;
        }
        else {
            dy += dyChange;
        }

        /*if(score%100 == 0){
            dyChange += 0.05f;
        }*/

        if(dy>14) dy=14;
        if(dy<-14) dy=-14;
        y += dy;

        if(fuelEmptyRate<-7) {
            fuelEmptyRate = -7;
        }

        //game over
        /*if(y<=0 || y>=(GamePanel.HEIGHT - 25)) {
            playing = false;
            gameOver = true;
        }*/
    }

    private int levelUpShowTimerCount = 0;
    public void draw(Canvas canvas) {
        canvas.drawBitmap(animation.getImage(),x,y,null);

        //fuel gauge
        Paint paint = new Paint();
        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);
        canvas.drawRect(FUEL_X-2, FUEL_Y-2, FUEL_X + MAX_FUEL + 2, FUEL_Y + 12, paint);
        if(fuelGauge>=120)
            paint.setColor(Color.parseColor("#06b723"));
        else if(fuelGauge>=60)
            paint.setColor(Color.parseColor("#FFD2C813"));
        else
            paint.setColor(Color.RED);
        paint.setStrokeWidth(0);
        canvas.drawRect(FUEL_X, FUEL_Y, FUEL_X + fuelGauge, FUEL_Y + 10, paint);

        //canvas.drawBitmap(levelUpBitmap, GamePanel.WIDTH/2 - 50, GamePanel.HEIGHT/2, null);
        //show level up whem level up happens
        if(levelUp) {
            levelUpShowTimerCount ++;
            if(levelUpShowTimerCount < 50) {    //then level up msg will be showing only for 50 iteration of draw method
                canvas.drawBitmap(levelUpBitmap, GamePanel.WIDTH/2 - 50, GamePanel.HEIGHT/2, null);
            }
            else {
                levelUp = false;
                levelUpShowTimerCount = 0;
            }
        }
    }

    public void setUp(boolean b) {
        up =b;
    }

    public int getScore() {
        return score;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void resetDy() {
        dy = 0;
        dyChange = 0.5f;
    }

    public void resetY() {
        y = GamePanel.HEIGHT/2;
    }

    public void resetScore() {
        score = 0;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLevel() {
        return level;
    }

    public void setHiscore(int hiscore) {
        this.hiscore = hiscore;
    }

    public int getHiscore() {
        return hiscore;
    }

    public void setFuelGauge(int fuelGauge) {
        this.fuelGauge = fuelGauge > MAX_FUEL ? MAX_FUEL : fuelGauge;
    }

    public int getFuelGauge() {
        return fuelGauge;
    }

    public void resetFuelGauge() {
        fuelGauge = MAX_FUEL;
    }

    public boolean isLevelUp() {
        return levelUp;
    }
}
