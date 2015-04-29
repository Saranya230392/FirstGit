package com.android.settings.lge;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.android.settings.R;
// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;
import android.provider.Settings;

/**
@class       ScreenOffEttectAni
@date        2013/06/03
@author      seungyeop.yeom@lge.com
@brief        Production of preview animation
@warning    Animation must be careful.
*/

public class ScreenOffEttectAni extends Activity {

    private static final String TAG = "ScreenOffEttectAni";

    private ImageView aniImage = null;
    private int totalFrame;

    private AnimationDrawable aniDrawable = null;
    private Handler mCheckingAnimHandler = null;
    private Runnable mCheckingAnimRunnable = null;

    private static final int NO_EFFECT_NUM = 0;
    private static final int CIRCLE_EFFECT_NUM = 1;
    private static final int ANDROID_EFFECT_NUM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_off_effect_ani);

        // To create an object of the image
        aniImage = (ImageView)findViewById(R.id.screen_off_image);
        aniImage.setVisibility(View.VISIBLE);

        // Load current DB values and to reflect
        int modeDbValue = Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.SCREEN_OFF_EFFECT_SET, ANDROID_EFFECT_NUM);
        if (modeDbValue == NO_EFFECT_NUM) {
            aniImage.setBackgroundResource(R.anim.ani_screen_off_noeffect);
        } else if (modeDbValue == CIRCLE_EFFECT_NUM) {
            aniImage.setBackgroundResource(R.anim.ani_screen_off_circle);
        } else if (modeDbValue == ANDROID_EFFECT_NUM) {
            aniImage.setBackgroundResource(R.anim.ani_screen_off_android_style);
        }

        // To create object of the animation
        aniDrawable = (AnimationDrawable)aniImage.getBackground();

        // To sum ​​the number of frame
        for (int i = 0; i < aniDrawable.getNumberOfFrames(); i++) {
            totalFrame += aniDrawable.getDuration(i);
        }

        // To start the animation
        aniDrawable.start();

        Log.d(TAG, "[onCreate()] Preview animation start");

        mCheckingAnimHandler = new Handler();
        mCheckingAnimHandler.postDelayed(mCheckingAnimRunnable = new Runnable() {
            @Override
            public void run() {

                finish();
                overridePendingTransition(0, 0);
            }
        }, totalFrame);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        finish();
        overridePendingTransition(0, 0);
    }

    // To clean the animation
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.d(TAG, "[onPause()] Preview animation stop");
        aniDrawable.stop();
        mCheckingAnimHandler.removeCallbacks(mCheckingAnimRunnable);
        mCheckingAnimRunnable = null;
        aniDrawable = null;
        aniImage.clearAnimation();
        aniImage = null;
        System.gc();

        finish();
        overridePendingTransition(0, 0);
    }
}
