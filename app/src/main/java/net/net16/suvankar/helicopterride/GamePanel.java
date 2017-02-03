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
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by suvankar on 11/1/17.
 */

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    private MainThread mainThread;
    private Background background;
    private Player player;

    private BackgroundMusic backgroundMusic;
    private GameOverMusic gameOverMusic;
    private HelicopterBlastSound blastSound;
    private PointUpSound pointUpSound;

    private ArrayList<Missile> missiles;
    private ArrayList<TopWall> topWalls;
    private ArrayList<BottomWall> bottomWalls;
    private Explosion explosion;
    private Fuel fuel;
    private final Context mContext = getContext();

    private final Bitmap fuelBitmap = Utility.getBitmapFromVectorDrawable(mContext,R.drawable.fuel, 107, 75);
    private final Bitmap smallFuelBitmap = Utility.getBitmapFromVectorDrawable(mContext, R.drawable.fuel,80,50);
    private final Bitmap wallBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.brick); //Utility.getBitmapFromVectorDrawable(mContext,R.drawable.brick, BRICK_WALL_WIDTH,50);
    private final Bitmap gameStartBitMap = BitmapFactory.decodeResource(getResources(),R.drawable.start_game);
    private final Bitmap gameOverBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.game_over);
    private final Bitmap boomBitmap = Utility.getBitmapFromVectorDrawable(mContext,R.drawable.boom,77,28);
    private final Bitmap lifeBitmap = Utility.getBitmapFromVectorDrawable(mContext,R.drawable.life,30,30);
    private final Bitmap levelUpBitmap = Utility.getBitmapFromVectorDrawable(mContext,R.drawable.level_up,60, 50);
    private final Bitmap smallHelicopterBitmap = Utility.getBitmapFromVectorDrawable(mContext,R.drawable.helicopter_1,90,70);

    private Drawable pauseBtn;
    private Drawable playBtn;

    //how many times the player can get hit before game over
    private int life = 3;

    //fuel count
    //private int fuelCount = 0;

    private long missileStartTimer;
    private long gameStartTime;

    //dissappear player
    private boolean dissappear = false;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private int touchCount = 0;

    private static int mWidth;    //background image mWidth
    private static int mHeight;   //background image mHeight

    public static final int MOVE_SPEED = -4;
    private static final int PLAYER_WIDTH = 145;
    private static final int PLAYER_HEIGHT = 55;
    private static final int FUEL_WIDTH = 55;
    private static final int FUEL_HEIGHT = 65;
    private static final int BRICK_WALL_WIDTH = 20;
    //private static final int MAX_FUEL_COUNT = 100;  //if fuel count (which increases by 4) reaches this, life will be incremented by 1

    /*private int DEV_WIDTH;
    private int DEV_HEIGHT;*/

    private Random random = new Random(System.currentTimeMillis());

    private final String PREF = "helicopter_ride_pref_"+GameActivity.class.getName();
    private boolean gamePaused = false;

    private boolean mute = false;

    private final int[] playerSvgs = new int[]{
            R.drawable.helicopter_1,
            R.drawable.helicopter_2,
            R.drawable.helicopter_3,
            R.drawable.helicopter_4,
            R.drawable.helicopter_5
    };

    private final int[] missileSVGs = new int[] {
            R.drawable.missile_1,
            R.drawable.missile_2,
            R.drawable.missile_3
    };
    private Bitmap[] missileSprites = new Bitmap[missileSVGs.length];

    //AdMob Interstitial ad
    InterstitialAd mInterstitialAd;

    private Paint scorePaint;

    public GamePanel(Context context, InterstitialAd interstitialAd) {
        super(context);
        mInterstitialAd = interstitialAd;
        //add the callback to surface holder to intercept events
        getHolder().addCallback(this);

        //make gamepanel focusable to intercept events
        setFocusable(true);

        //score
        scorePaint = new Paint();
        scorePaint.setColor(Color.YELLOW);
        scorePaint.setTextSize(30);
        scorePaint.setAlpha(230);
        scorePaint.setTypeface(Typeface.MONOSPACE);
        scorePaint.setFakeBoldText(true);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        /*DEV_WIDTH = getWidth();
        DEV_HEIGHT = getHeight();*/

        mWidth = getWidth();
        mHeight = getHeight();

        //background music
        backgroundMusic = new BackgroundMusic(mContext);

        //gameover music
        gameOverMusic = new GameOverMusic(mContext);

        //blast sound
        blastSound = new HelicopterBlastSound(mContext);

        //point up sound
        pointUpSound = new PointUpSound(mContext);

        gameStartTime = System.nanoTime();

        //background image of game
        //background = new Background(BitmapFactory.decodeResource(getResources(),R.drawable.landscape));
        //scale background image to device
        background = new Background(Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(),R.drawable.landscape),
                mWidth, mHeight,false));

        //player
        Bitmap[] sprites = new Bitmap[5];
        for(int i=0; i<sprites.length;i++) {
            sprites[i] = Utility.getBitmapFromVectorDrawable(mContext, playerSvgs[i], 192, 144);
        }
        Bitmap levelUp = Utility.getBitmapFromVectorDrawable(mContext, R.drawable.level_up_dialogue, 114, 31);
        player = new Player(PLAYER_WIDTH, PLAYER_HEIGHT, sprites, levelUp);

        //missiles
        for(int i=0; i<missileSVGs.length; i++) {
            missileSprites[i] = Utility.getBitmapFromVectorDrawable(mContext, missileSVGs[i],61,35);
        }
        missiles = new ArrayList<>();
        missileStartTimer = System.nanoTime();

        //walls
        topWalls = new ArrayList<>();
        bottomWalls = new ArrayList<>();

        //fuel tank
        fuel = new Fuel(mWidth, mHeight /2, FUEL_WIDTH, FUEL_HEIGHT, fuelBitmap);

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
        this.getPlayer().setHiscore(mContext.getSharedPreferences(PREF,MODE_PRIVATE).getInt("HISCORE",MODE_PRIVATE));
        //update if it was mute last time
        mute = mContext.getSharedPreferences(PREF, MODE_PRIVATE).getBoolean("MUTE",false);

        if(gamePaused) {
            gamePaused = false;
            try {
                //after resume
                //this codes are required to resume the game after onPause called from GameActivity
                //to restore the state of game
                SharedPreferences preferences = mContext.getSharedPreferences(PREF, MODE_PRIVATE);
                this.getPlayer().setX(preferences.getInt("PLAYER_X",player.X));
                this.getPlayer().setY(preferences.getInt("PLAYER_Y", mHeight /2));
                this.getPlayer().setDx(preferences.getFloat("PLAYER_DX",0));
                this.getPlayer().setDy(preferences.getFloat("PLAYER_DY",0));
                this.getPlayer().setScore(preferences.getInt("SCORE",0));
                ArrayList<String> missileX = new ArrayList<>(preferences.getStringSet("MISSILE_X", null));
                ArrayList<String> missileY = new ArrayList<>(preferences.getStringSet("MISSILE_Y", null));
                for(int i=0; i<missileX.size(); i++) {
                    missiles.add(new Missile(missileSprites,
                            Integer.parseInt(missileX.get(i).trim()), Integer.parseInt(missileY.get(i).trim()), 15, 45, player.getScore()));
                }
                missileX.clear();
                missileX=null;
                missileY.clear();
                missileY=null;

            } catch (Exception e){}
        }

        setFocusable(true);

        //we are starting the game loop
        //main thread of the game loop
        mainThread = new MainThread(getHolder(), this);
        mainThread.setRunning(true);
        mainThread.start();
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
            if(event.getX() >= mWidth - 80 && event.getY() >= mHeight - 80 && player.isPlaying()) {
                player.setPlaying(false);
                gameStarted = false;
                if(!mute) {
                    if(backgroundMusic.isPlaying())
                        backgroundMusic.pause();
                    else backgroundMusic.play();
                }
                Log.d("PAUSE","true");

                System.gc();

                return true;
            }

            //if this is the first time pressing just increment
            if(gameOver && touchCount<1){
                touchCount++;
                gameOver = true;
                gameStarted = false;    //this is to show the Start Game image at the start

                //showing AD
                if(mInterstitialAd == null) {
                    mInterstitialAd = new InterstitialAd(mContext);
                    mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            requestNewInterstitial();
                        }
                    });
                }

                if(mInterstitialAd!=null && mInterstitialAd.isLoaded()) {
                    Log.d("AD","Showing AD after destroyAndGameOver");
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
                    //start background music
                    if(!mute) {
                        if (!backgroundMusic.isPlaying()) {
                            backgroundMusic.play();
                        }
                    }
                }
                else {
                    player.setUp(true);
                }
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
                if(!mute) {
                    if (!backgroundMusic.isPlaying()) {
                        backgroundMusic.play();
                    }
                }
            }
            //if player is not visible, make it visible
            if(dissappear)
                dissappear = false;

            return true;    //we handled the touch event
        }
        //releasing touch
        if(event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    //AdMob Interstitial Ad
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("18A6E611855D359A4B85D031E94D424B")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    //resetGame the game when game is over
    public void resetGame() {
        dissappear = false;
        gameOver = false;
        player.resetAll();
        backgroundMusic.reset();
        fuel = null;
        topWalls.clear();
        bottomWalls.clear();
        missiles.clear();
        life = 3;
        //fuelCount = 0;
    }

    //update the canvas for each game loop
    public void update() {
        if(player.isPlaying()) {

            //update background
            background.update();

            //destroyAndGameOver the player if fuel tank is empty
            if(player.getFuelGauge() == 0) {
                life--;
                if(life == 0) {
                    destroyAndGameOver();
                }
                else {
                    //pause the game
                    player.setPlaying(false);
                    gameStarted = false;
                    player.resetY();
                    player.resetDy();
                    player.resetFuelGauge();
                    missiles.clear();
                }
            }

            //update player
            if(!dissappear)
                player.update();

            //update fuel tank
            if(fuel != null){
                fuel.update();
                if(fuel.getX() <= -50){
                    int randomNum = random.nextInt(mHeight -150) + 60;
                    fuel = new Fuel(mWidth, randomNum, FUEL_WIDTH, FUEL_HEIGHT, fuelBitmap);
                }
            }
            else {
                int randomNum = random.nextInt(mHeight -150) + 60;
                fuel = new Fuel(mWidth, randomNum, FUEL_WIDTH, FUEL_HEIGHT, fuelBitmap);
            }
            //if player got the fuel
            if(collision(player, fuel)) {
                if(!mute)
                    pointUpSound.play();
                //Log.d("FUEL","got fuel");
                int randomNum = random.nextInt(mHeight -100) + 45;
                fuel = new Fuel(mWidth, randomNum, FUEL_WIDTH, FUEL_HEIGHT, fuelBitmap);
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
        if(explosion!=null) {
            explosion.update();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if(canvas != null) {
            //background
            background.draw(canvas);

            //borders
            for(TopWall border: topWalls) {
                border.draw(canvas);
            }
            for(BottomWall border: bottomWalls) {
                border.draw(canvas);
            }
            Log.d("WALLS","TOP="+topWalls.size()+", BOT="+bottomWalls.size());


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
                if(explosion.isPlayedOnce())
                    explosion = null;
            }

            //fuel tank
            if(fuel !=null)
                fuel.draw(canvas);

            //draw all stats on screen
            drawScoreAndStats(canvas);

            //play pause button
            if(player.isPlaying()) {
                pauseBtn.setBounds(mWidth -100, mHeight -100, mWidth, mHeight);
                pauseBtn.draw(canvas);
            }
            else {
                playBtn.setBounds(mWidth -100, mHeight -100, mWidth, mHeight);
                playBtn.draw(canvas);
            }

            if(gameOver && !player.isPlaying() && gameStarted) {
                canvas.drawBitmap(gameOverBitmap, mWidth /2 - 240, mHeight /2 - 40, null);
            }

            if(!gameStarted) {
                canvas.drawBitmap(gameStartBitMap, mWidth /2 - 125, mHeight /2 - 30, null);
            }
        }
    }

    public void drawScoreAndStats(Canvas canvas) {
        //score
        canvas.drawText(" x"+player.getScore(), mWidth - 180, 35, scorePaint);
        canvas.drawBitmap(smallHelicopterBitmap, mWidth -250, 5, null);

        //Fuel
        canvas.drawBitmap(smallFuelBitmap,10, 5, null);

        //level
        canvas.drawText(" x"+player.getLevel(), (int)(mWidth * 0.66), 35, scorePaint);
        canvas.drawBitmap(levelUpBitmap,(int)(mWidth * 0.66) - 30,-1,null);

        //Life
        canvas.drawText(" x"+ life, mWidth /2 - 5, 35, scorePaint);
        canvas.drawBitmap(lifeBitmap, mWidth /2 - 35, 10,null);
    }

    public boolean collision(GameObject a, GameObject b) {
        if(Rect.intersects(a.getRectangle(), b.getRectangle())) {
            return true;
        }
        return false;
    }

    public void createAndUpdateMissiles() {
        long missileElapsed = (System.nanoTime() - missileStartTimer) / 1000000;
        if(missileElapsed > 2000 - player.getScore()) {
            //first missile at middle of screen
            if(missiles.size()==0) {
                missiles.add(new Missile(missileSprites, mWidth +10, mHeight /2, 25, 55, player.getScore()));    //13 missiles in bitmap
            }
            //next missiles at all different mHeight
            else {
                int randomNum = random.nextInt((mHeight -150) + 1) + 60;
                missiles.add(new Missile(missileSprites,
                        mWidth +10, randomNum, 15, 45, player.getScore()));
            }
            missileStartTimer = System.nanoTime();
        }
        //update every missile object
        for(Iterator<Missile> iterator = missiles.iterator();iterator.hasNext();) {
            Missile m = iterator.next();
            m.update();
            //collision detection of helicopter and missile
            if(collision(player, m)) {
                //eplosion
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion),
                        player.getX()+30, player.getY()-30,100,100,25,boomBitmap);
                iterator.remove();
                life--;
                if(life == 0) {
                    destroyAndGameOver();
                    break;
                }
                else {
                    //pause the game
                    destroyAndPause();
                    break;
                }
            }
            if(m.getX() < -60) {
                iterator.remove();
                //missileCount++;
            }
        }
//this deletion can happen on a new thread as this does not hamper the current thread
        /*if(missileCount > missileRemoveRate) {
            *//*new Thread(new Runnable() {
                @Override
                public void run() {*//*
                    ArrayList<Missile> missileArrayList = new ArrayList<>();
                    for(Missile m:missiles) {
                        if(m.getX()>= -60) {
                            missileArrayList.add(m);
                        }
                    }
                    missiles = null;
                    missiles = missileArrayList;
                    missileCount = 0;
                *//*}
            }).start();*//*
        }*/
    }

    public void createAndUpdateBottomBorder() {
        //there is no border yet
        if(bottomWalls.size()==0) {
            bottomWalls.add(new BottomWall(wallBitmap, -10, mHeight -50, 50));
        }
        //their is border created, lets create new one behind it
        else if(bottomWalls.get(bottomWalls.size()-1).getX() < mWidth +40) {
            bottomWalls.add(new BottomWall(wallBitmap,
                    bottomWalls.get(bottomWalls.size()-1).getX()+BRICK_WALL_WIDTH, mHeight -50,50));
        }

        for(BottomWall bottom: bottomWalls) {
            if(collision(player, bottom)) {
                //explosion
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion),
                        player.getX()+20, player.getY()-20,100,100,25,boomBitmap);
                life--;
                if(life == 0) {
                    destroyAndGameOver();
                    break;
                }
                else {
                    //pause the game
                    destroyAndPause();
                    break;
                }
            }

            //this deletion can happen on a new thread as this does not hamper the current thread
            /*if(bottomWallRemoveCount > bottomWallRemoveRate) {
                *//*new Thread(new Runnable() {
                    @Override
                    public void run() {*//*
                        //Log.d("Bottom walls", "before size = "+bottomWalls.size());
                        ArrayList<BottomWall> walls = new ArrayList<>();
                        for(BottomWall t: bottomWalls){
                            if(t.getX()>=-40) {
                                walls.add(t);
                            }
                        }
                        bottomWalls = null;
                        bottomWalls = walls;
                        bottomWallRemoveCount = 0;
                        //Log.d("Top walls", "after size = "+bottomWalls.size());
                    *//*}
                }).start();*//*
            }*/
        }
    }

    public void createAndUpdateTopBorder() {
        //there is no border yet
        if(topWalls.size()==0) {
            topWalls.add(new TopWall(wallBitmap, -10, 0 ,50));
        }
        //there is border, lets create behind it
        else if(topWalls.get(topWalls.size()-1).getX() < mWidth +40) {
            topWalls.add(new TopWall(wallBitmap  , topWalls.get(topWalls.size() - 1).getX() + BRICK_WALL_WIDTH, 0, 50));
        }

        for(TopWall top : topWalls) {
            if(collision(player, top)) {
                //explosion
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion),
                        player.getX()+20, player.getY()-20,100,100,25,boomBitmap);
                life--;
                if(life == 0) {
                    destroyAndGameOver();
                    break;
                }
                else {
                    //pause the game
                    destroyAndPause();
                    break;
                }
            }
        }
        //this deletion can happen on a new thread as this does not hamper the current thread
        /*if(topBorderRemoveCount > topWallRemoveRate) {
            *//*new Thread(new Runnable() {
                @Override
                public void run() {*//*
                    //Log.d("Top walls", "before size = "+topWalls.size());
                    int i=0;
                    ArrayList<TopWall> walls = new ArrayList<>();
                    for(TopWall t: topWalls){
                        if(t.getX()>=-40) {
                            walls.add(t);
                        }
                    }
                    topWalls = null;
                    topWalls = walls;
                    topBorderRemoveCount = 0;
                *//*}
            }).start();*//*
            //Log.d("Top walls", "after size = "+topWalls.size());
        }*/

    }

    public void destroyAndGameOver() {
        Log.d("destroyAndGameOver","game over");
        player.setPlaying(false);
        player.resetY();
        dissappear = true;
        gameOver = true;
        //to show the Start Game image at the start again
        touchCount = 0;
        //stop music
        if(backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
            Log.d("destroyAndGameOver","music pause");
        }
        if(!mute) {
            Log.d("destroyAndGameOver","destroyAndGameOver play");
            gameOverMusic.play();
        }
        //set hi score
        setHiscore();
        System.gc();

        if(mInterstitialAd!=null) {
            requestNewInterstitial();
        }
    }

    private void destroyAndPause() {
        //pause the game
        player.setPlaying(false);
        dissappear = true;
        Log.d("destroyAndPause","clearing all lists");
        //topWalls.clear();
        //bottomWalls.clear();
        missiles.clear();

        //if it hits the wall reset the player coordinates
        player.resetY();
        player.resetDy();
        player.resetFuelGauge();
        if(!mute) {
            backgroundMusic.pause();
            blastSound.play();
        }
        //fuelCount = 0;
        gameStarted = false;

        setHiscore();
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

    public ArrayList<TopWall> getTopWalls() {
        return topWalls;
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
            SharedPreferences preferences = mContext.getSharedPreferences(PREF, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("HISCORE",player.getHiscore());
            editor.commit();
        }
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public static int getmHeight() {
        return mHeight;
    }

    public static int getmWidth() {
        return mWidth;
    }
}
