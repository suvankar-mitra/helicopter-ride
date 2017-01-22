package net.net16.suvankar.helicopterride;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by suvankar on 11/1/17.
 */

public class GamePanel extends GLSurfaceView implements SurfaceHolder.Callback {

    private MainThread mainThread;
    private Background background;
    private Player player;
    private BackgroundMusic backgroundMusic;
    private ArrayList<Missile> missiles;
    private ArrayList<TopWall> topWall;
    private ArrayList<BottomWall> bottomWalls;
    private Explosion explosion;
    private Fuel fuel;

    private final Bitmap fuelBitmap = Utility.getBitmapFromVectorDrawable(getContext(),R.drawable.fuel, 107, 65);
    private Bitmap gameStartBitMap = BitmapFactory.decodeResource(getResources(),R.drawable.start_game);
    private Bitmap gameOverBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.game_over);
    private Bitmap boomBitmap = Utility.getBitmapFromVectorDrawable(getContext(),R.drawable.boom,187,52);

    private Drawable pauseBtn;
    private Drawable playBtn;

    private long missileStartTimer;
    private long gameStartTime;

    //dissappear player
    private boolean dissappear = false;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private int touchCount = 0;

    public static final int MOVE_SPEED = -3;
    public static final int WIDTH = 800;    //background image width
    public static final int HEIGHT = 450;   //background image height
    private static final int PLAYER_WIDTH = 145;
    private static final int PLAYER_HEIGHT = 55;
    private static final int FUEL_WIDTH = 40;
    private static final int FUEL_HEIGHT = 25;

    private int DEV_WIDTH;
    private int DEV_HEIGHT;

    private Surface mSurface;

    private Random random = new Random(System.currentTimeMillis());

    private final String PREF = "helicopter_ride_pref_"+GameActivity.class.getName();
    private boolean gamePaused = false;

    private boolean mute = false;

    private final int[] svgs = new int[]{
            R.drawable.helicopter_1,
            R.drawable.helicopter_2,
            R.drawable.helicopter_3,
            R.drawable.helicopter_4,
            R.drawable.helicopter_5
    };

    //AdMob Interstitial ad
    InterstitialAd mInterstitialAd;

    public GamePanel(Context context, InterstitialAd interstitialAd) {
        super(context);
        mInterstitialAd = interstitialAd;
        //add the callback to surface holder to intercept events
        getHolder().addCallback(this);

        //make gamepanel focusable to intercept events
        setFocusable(true);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mSurface=holder.getSurface();

        DEV_WIDTH = getWidth();
        DEV_HEIGHT = getHeight();

        //we are starting the game loop
        //main thread of the game loop
        mainThread = new MainThread(getHolder(), this);
        mainThread.setRunning(true);
        mainThread.start();

        //background music
        backgroundMusic = new BackgroundMusic(getContext());

        gameStartTime = System.nanoTime();

        //background image of game
        background = new Background(BitmapFactory.decodeResource(getResources(),R.drawable.landscape));

        //player
        Bitmap[] sprites = new Bitmap[5];
        for(int i=0; i<sprites.length;i++) {
            sprites[i] = Utility.getBitmapFromVectorDrawable(getContext(),svgs[i]);
        }
        Bitmap levelUp = Utility.getBitmapFromVectorDrawable(getContext(), R.drawable.level_up, 114, 31);
        player = new Player(PLAYER_WIDTH, PLAYER_HEIGHT, sprites, levelUp);

        //missiles
        missiles = new ArrayList<>();
        missileStartTimer = System.nanoTime();

        //borders
        topWall = new ArrayList<>();
        bottomWalls = new ArrayList<>();

        //fuel tank
        fuel = new Fuel(WIDTH, HEIGHT/2, FUEL_WIDTH, FUEL_HEIGHT,
                Utility.getBitmapFromVectorDrawable(getContext(),R.drawable.fuel, 107, 65));

        //pause button
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            pauseBtn = getResources().getDrawable(R.drawable.ic_pause_green,null);
        else
            pauseBtn = getResources().getDrawable(R.drawable.ic_pause_green);
        //play button
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            playBtn = getResources().getDrawable(R.drawable.ic_play,null);
        else
            playBtn = getResources().getDrawable(R.drawable.ic_play);

        //update hi score from preferences - this will not be deleted even if the activity gets killed
        this.getPlayer().setHiscore(getContext().getSharedPreferences(PREF,MODE_PRIVATE).getInt("HISCORE",MODE_PRIVATE));
        //update if it was mute last time
        mute = getContext().getSharedPreferences(PREF, MODE_PRIVATE).getBoolean("MUTE",false);

        if(gamePaused) {
            gamePaused = false;
            try {
                //after resume
                //this codes are required to resume the game after onPause called from GameActivity
                //to restore the state of game
                SharedPreferences preferences = getContext().getSharedPreferences(PREF, MODE_PRIVATE);
                this.getPlayer().setX(preferences.getInt("PLAYER_X",player.X));
                this.getPlayer().setY(preferences.getInt("PLAYER_Y",HEIGHT/2));
                this.getPlayer().setDx(preferences.getFloat("PLAYER_DX",0));
                this.getPlayer().setDy(preferences.getFloat("PLAYER_DY",0));
                this.getPlayer().setScore(preferences.getInt("SCORE",0));
                ArrayList<String> missileX = new ArrayList<>(preferences.getStringSet("MISSILE_X", null));
                ArrayList<String> missileY = new ArrayList<>(preferences.getStringSet("MISSILE_Y", null));
                for(int i=0; i<missileX.size(); i++) {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            Integer.parseInt(missileX.get(i).trim()), Integer.parseInt(missileY.get(i).trim()), 15, 45, player.getScore(), 13));
                }
                missileX.clear();
                missileX=null;
                missileY.clear();
                missileY=null;

            } catch (Exception e){}
        }

        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;
        while(retry && counter < 1000) {
            counter ++;
            try {
                mainThread.setRunning(false);
                mainThread.join();
                retry = false;
                mainThread = null;
            } catch (InterruptedException e) {
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN) {

            //pause the game when Pause on bottom right corner clicked
            if(event.getX() >= DEV_WIDTH - 80 && event.getY() >= DEV_HEIGHT - 80 && player.isPlaying()) {
                player.setPlaying(false);
                gameStarted = false;
                if(!mute) {
                    if(backgroundMusic.isPlaying())
                        backgroundMusic.pause();
                }
                Log.d("PAUSE","true");

                System.gc();

                return true;
            }

            //if this is first time pressing just increment
            if(gameOver && touchCount<1){
                touchCount++;
                gameOver = true;
                gameStarted = false;    //this is to show the Start Game image at the start

                //showing AD
                if(mInterstitialAd.isLoaded()) {
                    Log.d("AD","Showing AD after destroy");
                    mInterstitialAd.show();
                }
            }
            //if we are starting after a game over
            else if(gameOver && touchCount==1) {
                if(!player.isPlaying()) {
                    player.setPlaying(true);
                    gameStarted = true;
                    if(gameOver)
                        resetGame();
                }
                else {
                    player.setUp(true);
                }
                //start background music
                if(!mute)
                    backgroundMusic.play();
            }
            //first time game starting
            else if(!gameOver) {
                if(!player.isPlaying()) {
                    player.setPlaying(true);
                    gameStarted = true;
                }
                else {
                    player.setUp(true);
                }
                //start background music
                if(!mute)
                    backgroundMusic.play();
            }
            return true;    //we handled the touch event
        }
        //releasing touch
        if(event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    //resetGame the game when game is over
    public void resetGame() {
        dissappear = false;
        gameOver = false;
        player.resetDy();
        player.resetY();
        player.resetScore();
        player.resetFuelGauge();
        missiles.clear();
        backgroundMusic.reset();
        fuel = null;
    }

    //update the canvas for each game loop
    public void update() {
        if(player.isPlaying()) {

            //update background
            background.update();

            //destroy the player if fuel tank is empty
            if(player.getFuelGauge() == 0) {
                //eplosion
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion),
                        player.getX()+30, player.getY()-30,100,100,25,boomBitmap);
                destroy();
                fuel = null;
            }

            //update player
            if(!dissappear)
                player.update();

            //update fuel tank
            if(fuel != null){
                fuel.update();
                if(fuel.getX() <= -50){
                    int randomNum = random.nextInt(HEIGHT-100) + 45;
                    fuel = new Fuel(WIDTH, randomNum, FUEL_WIDTH, FUEL_HEIGHT, fuelBitmap);
                }
            }
            else {
                int randomNum = random.nextInt(HEIGHT-100) + 45;
                fuel = new Fuel(WIDTH, randomNum, FUEL_WIDTH, FUEL_HEIGHT, fuelBitmap);
            }
            //if player got the fuel
            if(collision(player, fuel)) {
                Log.d("FUEL","got fuel");
                int randomNum = random.nextInt(HEIGHT-100) + 45;
                fuel = new Fuel(WIDTH, randomNum, FUEL_WIDTH, FUEL_HEIGHT, fuelBitmap);
                player.setFuelGauge(player.getFuelGauge() + Player.FUEL_INCREASE);
            }

            //create missiles
            createAndUpdateMissiles();

            //udate top border
            createAndUpdateTopBorder();
            //update bottom border
            createAndUpdateBottomBorder();
        }

        //explosion update
        if(explosion!=null)
            explosion.update();
    }

    @Override
    public void draw(Canvas canvas) {
        if(canvas != null) {

            //we need to scale the image according to the device
            final float scaleFactorX = ((float)getWidth()/WIDTH);
            final float scaleFactorY = (float)getHeight()/HEIGHT;
            //saving the state of canvas so that we can restore it after scaling
            final int savedState = canvas.save();

            //background
            canvas.scale(scaleFactorX, scaleFactorY);
            background.draw(canvas);

            //borders
            for(TopWall border: topWall) {
                border.draw(canvas);
            }
            for(BottomWall border: bottomWalls) {
                border.draw(canvas);
            }


            //we are drawing player only if there is no explosion
            if(!dissappear)
                player.draw(canvas);

            //missiles
            for(Missile missile: missiles) {
                missile.draw(canvas);
            }

            //explosion
            if(explosion!=null) {
                explosion.draw(canvas);
            }

            //fuel tank
            if(fuel !=null)
                fuel.draw(canvas);

            //draw all stats on screen
            drawScoreAndStats(canvas);

            //play pause button
            if(player.isPlaying()) {
                pauseBtn.setBounds(WIDTH-70, HEIGHT-70, WIDTH-5, HEIGHT-5);
                pauseBtn.draw(canvas);
            }
            else {
                playBtn.setBounds(WIDTH-70, HEIGHT-70, WIDTH-5, HEIGHT-5);
                playBtn.draw(canvas);
            }
            if(gameOver && !player.isPlaying() && gameStarted) {
                canvas.drawBitmap(gameOverBitmap, WIDTH/2 - 240, HEIGHT/2 - 40, null);
            }

            if(!gameStarted) {
                canvas.drawBitmap(gameStartBitMap, WIDTH/2 - 244, HEIGHT/2 - 44, null);
            }

            canvas.restoreToCount(savedState);      //if we dont restore, it will scale up n up n up
        }
    }

    public void drawScoreAndStats(Canvas canvas) {
        //score
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setAlpha(230);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setFakeBoldText(true);
        canvas.drawText("Score: "+player.getScore(), WIDTH - 130, 20, paint);
        //Fuel
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setAlpha(230);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setFakeBoldText(true);
        canvas.drawText("Fuel Meter", 10, 20, paint);
        //level
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setAlpha(230);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setFakeBoldText(true);
        canvas.drawText("Level: "+player.getLevel(), 10, HEIGHT - 10, paint);
        //hi score
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setAlpha(230);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setFakeBoldText(true);
        canvas.drawText("Hi-Score: "+player.getHiscore(), WIDTH/2 - 55, 20, paint);
    }

    public boolean collision(GameObject a, GameObject b) {
        if(Rect.intersects(a.getRectangle(), b.getRectangle())) {
            return true;
        }
        return false;
    }

    public void destroy() {
        player.setPlaying(false);
        dissappear = true;
        gameOver = true;
        //to show the Start Game image at the start again
        touchCount = 0;
        //stop music
        if(!mute)
            backgroundMusic.pause();
        //set hi score
        setHiscore();

        System.gc();
    }

    public void createAndUpdateMissiles() {
        long missileElapsed = (System.nanoTime() - missileStartTimer) / 1000000;
        if(missileElapsed > 2000 - player.getScore()) {
            //first missile at middle of screen
            if(missiles.size()==0) {
                missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                        WIDTH+10, HEIGHT/2, 15, 45, player.getScore(), 13));    //13 missiles in bitmap
            }
            //next missiles at all different height
            else {
                int randomNum = random.nextInt((HEIGHT-45 - 45) + 1) + 45;
                missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                        WIDTH+10, randomNum, 15, 45, player.getScore(), 13));
            }
            missileStartTimer = System.nanoTime();
        }
        //update every missile object
        for(int i=0; i<missiles.size(); i++) {
            missiles.get(i).update();
            //collision detection of helicopter and missile
            if(collision(player, missiles.get(i))) {
                //eplosion
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion),
                        player.getX()+30, player.getY()-30,100,100,25,boomBitmap);
                missiles.remove(i);

                destroy();

                break;
            }
            if(missiles.get(i).getX()<-60) {
                missiles.set(i,null);
                missiles.remove(i);
            }
        }
    }

    public void createAndUpdateBottomBorder() {
        //there is no border yet
        if(bottomWalls.size()==0) {
            bottomWalls.add(new BottomWall(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                    -10,HEIGHT-30, 30));
        }
        //their is border created, lets create new one behind it
        else if(bottomWalls.get(bottomWalls.size()-1).getX() < WIDTH+40)
            bottomWalls.add(new BottomWall(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                    bottomWalls.get(bottomWalls.size()-1).getX()+20,HEIGHT-30,30));

        for(int i = 0; i< bottomWalls.size(); i++) {
            bottomWalls.get(i).update();
            if(collision(player, bottomWalls.get(i))) {
                //eplosion
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion),
                        player.getX()+20, player.getY()-30,100,100,25,boomBitmap);
                destroy();
            }
            if(bottomWalls.get(i).getX()<-20) {
                bottomWalls.set(i,null);
                bottomWalls.remove(i);
            }
        }
    }

    public void createAndUpdateTopBorder() {
        //there is no border yet
        if(topWall.size()==0) {
            topWall.add(new TopWall(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    -10, 0, 30));
        }
        //there is border, lets create behind it
        else if(topWall.get(topWall.size()-1).getX() < WIDTH+40)
            topWall.add(new TopWall(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                    topWall.get(topWall.size()-1).getX()+20,0, 30));

        for(int i = 0; i< topWall.size(); i++) {
            topWall.get(i).update();
            if(collision(player, topWall.get(i))) {
                //eplosion
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion),
                        player.getX()+20, player.getY()-20,100,100,25,boomBitmap);
                destroy();
            }
            if(topWall.get(i).getX()<-40) {
                topWall.set(i,null);
                topWall.remove(topWall.get(i));
            }
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<Missile> getMissiles() {
        return missiles;
    }

    public void setMissiles(ArrayList<Missile> missiles) {
        this.missiles = missiles;
    }

    public ArrayList<TopWall> getTopWall() {
        return topWall;
    }

    public ArrayList<BottomWall> getBottomWalls() {
        return bottomWalls;
    }

    public boolean isGamePaused() {
        return gamePaused;
    }

    public void setGamePaused(boolean gamePaused) {
        this.gamePaused = gamePaused;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public BackgroundMusic getBackgroundMusic() {
        return backgroundMusic;
    }

    public void setHiscore() {
        if(player.getScore() > player.getHiscore()) {
            player.setHiscore(player.getScore());

        }
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }
}
