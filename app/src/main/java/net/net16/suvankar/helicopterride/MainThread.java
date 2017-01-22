package net.net16.suvankar.helicopterride;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.io.Serializable;

/**
 * Created by suvankar on 11/1/17.
 */

public class MainThread extends Thread implements Serializable{
    private int FPS = 35;
    private double averagFPS;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;
    public static Canvas canvas;

    private String TAG = MainThread.class.getName();

    //constructor
    public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }

    //game loop
    @Override
    public void run() {
        long startTime;
        long timeMillis;
        long waitTime;
        long totalTime = 0;
        int frameCount = 0;
        long targetTime = 1000/FPS;     //how much I want to take for each game loop to iterate

        while(running) {
            startTime = System.nanoTime();
            canvas = null;
            //try to lock the canvas for pixel editing
            try {
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    canvas = surfaceHolder.getSurface().lockHardwareCanvas();
                }
                else {*/
                    canvas = surfaceHolder.lockCanvas();
                //}
                synchronized (surfaceHolder) {
                    //heart of the game
                    this.gamePanel.update();
                    this.gamePanel.draw(canvas);
                }
            } catch (Exception ex) {

            }
            finally {
                if(canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            timeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - timeMillis;
            waitTime = waitTime<0 ? -1*waitTime : waitTime;
            try {
                this.sleep(waitTime);
            } catch (InterruptedException e) {
            }

            totalTime += System.nanoTime() - startTime;
            frameCount++;

            if(frameCount == FPS) {
                averagFPS = 1000/((totalTime/1000000)/frameCount);
                frameCount = 0;
                totalTime = 0;
                //Log.d(TAG,"Average FPS: "+averagFPS);
            }
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
