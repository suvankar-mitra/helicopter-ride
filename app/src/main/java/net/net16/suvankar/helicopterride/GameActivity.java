package net.net16.suvankar.helicopterride;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GameActivity extends FragmentActivity {

    /*
            Future update - firebase authentication and save player data
        */

    //implements GoogleApiClient.OnConnectionFailedListener{
    //firebase interation
    //private FirebaseAnalytics mFirebaseAnalytics;
    /*private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;*/

    private GamePanel gamePanel;
    private boolean mute;
    private ImageButton muteBtn;
    private boolean gameStarted;

    private InterstitialAd mInterstitialAd;
    private AdView mAdView;

    private int hiscore = 0;

    private final String TAG = "GoogleAuth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("TAG","onCreate called");

        //turn title off
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //set activity to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //hardware acceleration
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        setContentView(R.layout.activity_game);

        //clearing all values under this shared pref before the game starts
        SharedPreferences preferences = getSharedPreferences(PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for(String s: KEYS) {
            editor.remove(s);
            editor.commit();
        }
        //get the hiscore value
        hiscore = preferences.getInt("HISCORE",0);

        TextView flashText = (TextView) findViewById(R.id.flastText);

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(400); //time of the blink
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        flashText.startAnimation(anim);

        //update if it was mute last time
        mute = getSharedPreferences(PREF, MODE_PRIVATE).getBoolean("MUTE",false);
        muteBtn = (ImageButton) findViewById(R.id.muteBtn);
        if(mute) {
            muteBtn.setImageResource(R.drawable.music_off);
        }
        else {
            muteBtn.setImageResource(R.drawable.music_on);
        }

        //Banner Ad
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewBanner();
            }
        });
        requestNewBanner();

        //AdMob
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.app_id));

        //Interstitial Ad
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();

        //creating Game Panel
        gamePanel = new GamePanel(getApplicationContext(), mInterstitialAd);

        /*
            Future update - firebase authentication and save player data
        */
        //Obtain the FirebaseAnalytics instance.
        //mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //firebase authentication
        /*mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this *//* FragmentActivity *//*, this *//* OnConnectionFailedListener *//*)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();*/
    }

    /*Future update - firebase authentication and save player data*/
    /*private static final int RC_SIGN_IN = 9001;

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        ImageButton signOut = (ImageButton) findViewById(R.id.signOut);
                        ImageButton signIn = (ImageButton) findViewById(R.id.signIn);
                        signOut.setFocusable(false);
                        signIn.setFocusable(true);
                        Toast.makeText(GameActivity.this, "Sign out successful!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google sign in failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        Toast.makeText(GameActivity.this, "Sign in successful!",
                                Toast.LENGTH_SHORT).show();
                        ImageButton signOut = (ImageButton) findViewById(R.id.signOut);
                        ImageButton signIn = (ImageButton) findViewById(R.id.signIn);
                        signOut.setFocusable(true);
                        signIn.setFocusable(false);

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(GameActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }*/


    //AdMob Banner Ad
    private void requestNewBanner() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("18A6E611855D359A4B85D031E94D424B")
                .build();
        mAdView.loadAd(adRequest);
    }

    //AdMob Interstitial Ad
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("18A6E611855D359A4B85D031E94D424B")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d("TAG","onTouch");
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            gameStarted = true;
            setContentView(gamePanel);
            return true;
        }
        return super.onTouchEvent(event);
    }

    private final String PREF = "helicopter_ride_pref_"+this.getClass().getName();
    private final String[] KEYS = new String[]{
            "PLAYER_X",
            "PLAYER_Y",
            "PLAYER_DX",
            "PLAYER_DY",
            "SCORE",
            "MISSILE_X",
            "MISSILE_Y",
            "TOP_B_X",
            "TOP_B_Y",
            "BOT_B_X",
            "BOT_B_Y",
            "RESUME"
            //HISCORE
    };

    /*@Override
    protected void onResume() {
        Log.d("TAG","on resume");
        super.onResume();
        gameStarted = true;
        setContentView(R.layout.activity_game);

        //clearing all values under this shared pref before the game starts
        SharedPreferences preferences = getSharedPreferences(PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for(String s: KEYS) {
            editor.remove(s);
            editor.commit();
        }
        hiscore = preferences.getInt("HISCORE",0);

        //AdMob
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.app_id));

        //Interstitial Ad
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();
    }*/

    @Override
    public void onBackPressed() {
        Log.d("TAG","back pressed");
        saveGameState(false);
        setContentView(R.layout.exit_layout);
        //super.onBackPressed(); //DO NOT CALL super, otherwise it will exit the game immediately
    }

    @Override
    protected void onPause() {
        Log.d("TAG","onPause");
        saveGameState(false);
        super.onPause();
    }

    //pause the game and save state of the game
    private void saveGameState(boolean resumeAfter) {
        Log.d("TAG","saveGameState");
        gameStarted = false;
        //pause the music
        try {
            if(gamePanel!=null && gamePanel.getBackgroundMusic()!=null)
                gamePanel.getBackgroundMusic().pause();
        }catch (IllegalStateException e){}

        if(gamePanel!=null && gamePanel.getPlayer()!=null) {

            //game pause
            gamePanel.setGamePaused(true);
            gamePanel.setHiscore();
            gamePanel.getPlayer().setPlaying(false);
            gamePanel.setGameStarted(false);

            SharedPreferences preferences = getSharedPreferences(PREF, MODE_PRIVATE);

            SharedPreferences.Editor editor = preferences.edit();
            //clearing all values under this shared pref
            for(String s: KEYS) {
                editor.remove(s);
                editor.commit();
            }

            editor.putInt("PLAYER_X",gamePanel.getPlayer().getX());
            editor.putInt("PLAYER_Y",gamePanel.getPlayer().getY());
            editor.putFloat("PLAYER_DX",gamePanel.getPlayer().getDx());
            editor.putFloat("PLAYER_DY",gamePanel.getPlayer().getDy());
            editor.putInt("SCORE",gamePanel.getPlayer().getScore());
            editor.putInt("HISCORE",gamePanel.getPlayer().getHiscore());
            editor.putBoolean("MUTE",gamePanel.isMute());

            Set<String> MISSILE_X = new TreeSet<>();
            Set<String> MISSILE_Y = new TreeSet<>();
            for(Missile missile: gamePanel.getMissiles()) {
                MISSILE_X.add(missile.getX()+"");
                MISSILE_Y.add(missile.getY()+"");
            }
            editor.putStringSet("MISSILE_X",MISSILE_X);
            editor.putStringSet("MISSILE_Y",MISSILE_Y);

            if(resumeAfter){
                editor.putBoolean("RESUME",true);
            }

            editor.commit();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(gamePanel != null && gamePanel.getBackgroundMusic()!=null)
            gamePanel.getBackgroundMusic().stop();
        saveGameState(false);
    }

    public void changeVolume(View view) {
        SharedPreferences preferences = getSharedPreferences(PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if(mute) {
            mute = false;
            muteBtn.setImageResource(R.drawable.music_on);
        }
        else {
            mute = true;
            muteBtn.setImageResource(R.drawable.music_off);
        }
        editor.putBoolean("MUTE",mute);
        editor.commit();
    }

    private int gameResetCounter = 0;
    private long gameResetTime = 0;
    public void resetGame(View view) {
        if(gameResetCounter>0) {
            if((System.nanoTime() - gameResetTime)/1000000 > 2000) {
                gameResetTime = 0;
                gameResetCounter = 0;
                Toast.makeText(this, "Press again to reset game", Toast.LENGTH_SHORT).show();
                gameResetTime = System.nanoTime();
                gameResetCounter++;
                return;
            }
            SharedPreferences preferences = getSharedPreferences(PREF, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            List<String> KEYS = new ArrayList<>(Arrays.asList(this.KEYS));
            KEYS.add("HISCORE");
            KEYS.add("MUTE");
            for(String s: KEYS) {
                editor.remove(s);
                editor.commit();
            }
            gameResetCounter = 0;
            gameResetTime = 0;
            Toast.makeText(this, "Game reset complete", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Press again to reset game", Toast.LENGTH_SHORT).show();
            gameResetTime = System.nanoTime();
            gameResetCounter++;
        }
    }

    public void quitGame(View view) {
        //moveTaskToBack(true);
        //saveGameState(false);
        finish();
    }

    public void backToGame(View view) {
        //saveGameState(true);
        finish();
        startActivity(new Intent(this, GameActivity.class));
    }

    public void showStats(View view) {
        LayoutInflater inflator=getLayoutInflater();
        view=inflator.inflate(R.layout.stats, null, true);
        view.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        setContentView(view);
        TextView hiScore = (TextView) findViewById(R.id.hiScore);
        hiScore.setText(String.valueOf(hiscore));
    }

    public void showCredit(View view) {
        LayoutInflater inflator=getLayoutInflater();
        view=inflator.inflate(R.layout.credit, null, true);
        view.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        setContentView(view);
    }

    /*Future update - firebase authentication and save player data*/
    /*@Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }*/

    /*Future update - firebase authentication and save player data*/
    /*public void signIn(View view) {
        signIn();
    }

    public void signOut(View view) {
        signOut();
    }*/
}
