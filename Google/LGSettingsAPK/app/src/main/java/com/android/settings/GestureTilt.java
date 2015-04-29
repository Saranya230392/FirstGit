/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import com.android.settings.R;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ViewFlipper;
import android.os.SystemClock;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.os.Vibrator;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.content.Intent;

public class GestureTilt extends Activity implements SensorEventListener {

    private static final long START_DELAY = 1000;
    private static final int PAGE_COUNT = 3;
    float start = 0;
    float end = 0;
    int value = 0;
    int count = 10;
    int divide = 10;
    private int page = 0;
    private ViewFlipper vf = null;
    private boolean move = false;
    private ImageView mIcon;
    private Vibrator myVib;
    private TextView mTextView;
    private ImageView mNavigation0 = null;
    private ImageView mNavigation1 = null;
    private ImageView mNavigation2 = null;
    private SensorManager mSm;
    private View mImageView;
    private AnimationDrawable mAnimation;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        public void run() {
            startAnimation(mAnimation);
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture_tilt);
        setTitle(R.string.sp_gesture_tilt_test_title_NORMAL);
        mNavigation0 = (ImageView)findViewById(R.id.navi_zero);
        mNavigation1 = (ImageView)findViewById(R.id.navi_one);
        mNavigation2 = (ImageView)findViewById(R.id.navi_two);
        value = getIntent().getIntExtra("value", 2);
        //Log.e("JJJJJJJJJJ",value+"");
        mSm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mTextView = (TextView)findViewById(R.id.tilt_test_text);
        mIcon = (ImageView)findViewById(R.id.icon);
        mIcon.setHapticFeedbackEnabled(true);
        myVib = (Vibrator)this.getSystemService(VIBRATOR_SERVICE);
        initView();
        mIcon.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                {
                    SystemClock.sleep(500);
                    myVib.vibrate(40);
                    move = true;
                    mTextView.setText(getResources().getString(
                            R.string.sp_gesture_tilt_test_pressed_NOMAL));

                    break;
                }
                case MotionEvent.ACTION_UP:
                {
                    move = false;
                    mTextView.setText(getResources().getString(
                            R.string.sp_gesture_tilt_test_text_NOMAL));
                    break;
                }
                default:
                    break;
                }

                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(mRunnable, START_DELAY);
        mSm.registerListener(this, mSm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        move = false;
        mTextView.setText(getResources().getString(R.string.sp_gesture_tilt_test_text_NOMAL));
        mSm.unregisterListener(this);
        stopAnimation(mAnimation);
    }

    private void initView() {

        vf = (ViewFlipper)findViewById(R.id.flipper);
        //vf.setOnTouchListener(touchListener);

        setDefaultSettings();

    }

    private void setDefaultSettings() {
        int id = R.id.gesture_anim_tilt;
        int drawable = R.drawable.gesture_anim_tilt_zero;

        setAnimationView(id, drawable);
    }

    private void next() {
        int childId = vf.getDisplayedChild();

        if (childId != PAGE_COUNT) {
            stopAnimation(mAnimation);

            switch (value) {
            case 0:
                vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in_zero));
                vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out_zero));
                divide = 14;
                break;
            case 1:
                vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in_one));
                vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out_one));
                divide = 12;
                break;
            case 2:
                vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
                vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
                divide = 10;
                break;
            case 3:
                vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in_three));
                vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out_three));
                divide = 8;
                break;
            case 4:
                vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in_max));
                vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out_max));
                divide = 6;
                break;
            default:
                vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
                vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
                divide = 10;

            }

            //vf.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_in));
            //vf.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_out));
            vf.setDisplayedChild(childId + 1);
            setAnimationView(childId + 1);
            mHandler.postDelayed(mRunnable, START_DELAY);

        }
    }

    private void previous() {
        int childId = vf.getDisplayedChild();

        if (childId != 0) {
            stopAnimation(mAnimation);

            switch (value) {
            case 0:
                vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in_zero));
                vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out_zero));
                divide = 14;
                break;
            case 1:
                vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in_one));
                vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out_one));
                divide = 12;
                break;
            case 2:
                vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
                vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
                divide = 10;
                break;
            case 3:
                vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in_three));
                vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out_three));
                divide = 8;
                break;
            case 4:
                vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in_max));
                vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out_max));
                divide = 6;
                break;
            default:
                vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
                vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
                divide = 10;

            }

            //vf.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_in));
            //vf.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_out));
            vf.setDisplayedChild(childId - 1);
            setAnimationView(childId - 1);
            mHandler.postDelayed(mRunnable, START_DELAY);

        }
    }

    protected void startAnimation(final AnimationDrawable animation) {
        if (animation != null && !animation.isRunning()) {
            animation.run();
        }
    }

    protected void stopAnimation(final AnimationDrawable animation) {
        if (animation != null && animation.isRunning()) {
            animation.stop();
        }
    }

    private void setAnimationView(int id, int drawable) {
        mImageView = (ImageView)findViewById(id);
        mImageView.setBackgroundResource(drawable);
        mAnimation = (AnimationDrawable)mImageView.getBackground();
    }

    void setAnimationView(int viewId) {

        int id;
        int drawable;
        switch (viewId) {
        case 0:
            id = R.id.gesture_anim_tilt;
            drawable = R.drawable.gesture_anim_tilt_zero;
            mNavigation0.setImageResource(R.drawable.gesture_ani_navi_active);
            mNavigation1.setImageResource(R.drawable.gesture_ani_navi_unactive);
            mNavigation2.setImageResource(R.drawable.gesture_ani_navi_unactive);
            break;
        case 1:
            id = R.id.gesture_anim_facing;
            drawable = R.drawable.gesture_anim_tilt_one;
            mNavigation0.setImageResource(R.drawable.gesture_ani_navi_unactive);
            mNavigation1.setImageResource(R.drawable.gesture_ani_navi_active);
            mNavigation2.setImageResource(R.drawable.gesture_ani_navi_unactive);
            break;
        case 2:
            id = R.id.gesture_anim_tap;
            drawable = R.drawable.gesture_anim_tilt_two;
            mNavigation0.setImageResource(R.drawable.gesture_ani_navi_unactive);
            mNavigation1.setImageResource(R.drawable.gesture_ani_navi_unactive);
            mNavigation2.setImageResource(R.drawable.gesture_ani_navi_active);
            break;
        default:
            id = R.id.gesture_anim_tilt;
            drawable = R.drawable.gesture_anim_tilt_zero;
            mNavigation0.setImageResource(R.drawable.gesture_ani_navi_active);
            mNavigation1.setImageResource(R.drawable.gesture_ani_navi_unactive);
            mNavigation2.setImageResource(R.drawable.gesture_ani_navi_unactive);
        }
        setAnimationView(id, drawable);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        int panningX = 0;
        count++;
        //Log.e("JJJJJ", count + "");
        if (count < divide) {
            return;
        }
        panningX = (int)event.values[2];
        // Log.e("JJJJJ", panningX + "");
        if (move) {
            if ((panningX < -20) && (page == 0)) {
                next();
                page = 1;
                //move=false;
                Log.e("JJJJJ", "next");
            }
            else if ((panningX < -20) && (page == 1)) {
                next();
                page = 2;
                //    move=false;
                Log.e("JJJJJ", "next");
            }

            else if ((panningX > 20) && (page == 2)) {
                previous();
                page = 1;
                //    move=false;
                Log.e("JJJJJ", "previous");
            }

            else if ((panningX > 20) && (page == 1)) {
                previous();
                page = 0;
                //    move=false;
                Log.e("JJJJJ", "previous");
            }
        }
        count = 0;
        // TODO Auto-generated method stub

    }
}
