package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
//import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.android.settings.lgesetting.Config.Config;

import java.util.ArrayList;
import java.util.HashMap;

public class EasyToUseSelectDescript extends Activity {

    public static final String TAG = "EasyToUseSelectDescript";
    private int PAGE_COUNT = 2;
    private TextView mTitle;
    private TextView mDesc1;
    private TextView mDesc2;
    private Button mBack;
    //ry.jeong : VZW mode change ODR SU issue [S]
    private Button mCenter;
    //ry.jeong : VZW mode change ODR SU issue [E]
    private Button mNext;
    private ViewFlipper mHint;
    private ImageView navigation0 = null;
    private ImageView navigation1 = null;
    private ImageView navigation2 = null;
    private ArrayList<HashMap<String, String>> mAnimationViewArrayList = null;
    private ArrayList<ImageView> mImageViewArray = null;
    private View mImageView;
    private AnimationDrawable mAnimation;
    private Intent intent;

    private static final long START_DELAY = 1000;
    public static final String MODE_KEY = "mode_key";
    public static final String MODE_STARTER = "starter";
    public static final String MODE_STANDARD = "standard";

    boolean IsStart = false;
    static boolean isLandscape = false;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        public void run() {
            startAnimation(mAnimation);
        }
    };

    /*    //ry.jeong : VZW mode change ODR SU issue [S]
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            // TODO Auto-generated method stub
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                int childId = mHint.getDisplayedChild();
                if (childId == 0) {
                    finish();
                }
                previous();
                return true;
            }
            return super.onKeyDown(keyCode, event);
    }
    *///ry.jeong : VZW mode change ODR SU issue [E]

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.easy_to_use_select_descript);

        Configuration mConfig = getResources().getConfiguration();
        setIsLandscape(mConfig);

        intent = getIntent();
        String name = intent.getStringExtra(MODE_KEY);

        if (MODE_STARTER.equals(name)) {
            IsStart = true;
            getActionBar().setTitle(R.string.easytouse_starter_title_old);
        } else if (MODE_STANDARD.equals(name)) {
            IsStart = false;
            getActionBar().setTitle(R.string.easytouse_standard_title_old);
        }

        mAnimationViewArrayList = null;
        mAnimationViewArrayList = new ArrayList<HashMap<String, String>>();

        initView();
        setButtonListener();

        if (savedInstanceState == null) {

        }
        else {
            mHint.setDisplayedChild(savedInstanceState.getInt("page"));
            setAnimationView(savedInstanceState.getInt("page"));
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mHandler.postDelayed(mRunnable, START_DELAY);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        stopAnimation(mAnimation);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        outState.putInt("page", mHint.getDisplayedChild());
    }

    private void initView() {
        mImageViewArray = new ArrayList<ImageView>();
        mHint = (ViewFlipper)findViewById(R.id.hint_image);
        mTitle = (TextView)findViewById(R.id.themeName);
        mDesc1 = (TextView)findViewById(R.id.theme_desc1);
        mDesc2 = (TextView)findViewById(R.id.theme_desc2);

        mBack = (Button)findViewById(R.id.back_button);
        //ry.jeong : VZW mode change ODR SU issue [S]
        mCenter = (Button)findViewById(R.id.center_button);
        mCenter.setEnabled(false);
        //ry.jeong : VZW mode change ODR SU issue [E]
        mNext = (Button)findViewById(R.id.next_button);

        navigation0 = (ImageView)findViewById(R.id.navi_zero);
        navigation1 = (ImageView)findViewById(R.id.navi_one);
        navigation2 = (ImageView)findViewById(R.id.navi_two);

        mImageViewArray.add(navigation0);
        mImageViewArray.add(navigation1);
        mImageViewArray.add(navigation2);

        setDefaultSettings();
    }

    private void setDefaultSettings() {
        int id;
        int drawable;

        for (int i = 0; i < PAGE_COUNT + 1; i++) {
            HashMap<String, String> viewItem = new HashMap<String, String>();
            viewItem = setViewItemGroup(viewItem, i);
            mAnimationViewArrayList.add(viewItem);
        }
        if (IsStart) {
            id = R.id.image_home;
            if ((Config.VZW).equals(Config.getOperator())) {
                drawable = R.drawable.img_mode_change_homescreen_starter;
            } else {
                drawable = R.drawable.img_mode_change_homescreen_starter;
            }
            mTitle.setText(R.string.easytouse_home_screen);
            mDesc1.setText(R.string.easytouse_home_starter_desc1);
            mDesc2.setText(R.string.easytouse_home_starter_desc2);

        }
        else {
            id = R.id.image_home;
            drawable = R.drawable.img_mode_change_homescreen;
            mTitle.setText(R.string.easytouse_home_screen);
            mDesc1.setText(R.string.easytouse_home_starter_desc1);
            //mDesc1.setText(R.string.easytouse_home_standard_desc1);
            mDesc2.setText(R.string.easytouse_home_standard_desc2);
        }
        navigation0.setImageResource(R.drawable.gesture_ani_navi_active);

        setDescriptionVisible();
        setAnimationView(id, drawable);
    }

    private void clearNavigation() {
        for (int i = 0; i < mImageViewArray.size(); i++) {
            mImageViewArray.get(i).setImageResource(R.drawable.gesture_ani_navi_unactive);
        }
    }

    private void setButtonListener() {
        mNext.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                int childId = mHint.getDisplayedChild();
                if (childId == PAGE_COUNT) {
                    finish();
                }
                next();
            }
        });

        //ry.jeong : VZW mode change ODR SU issue [S]
        mCenter.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                int childId = mHint.getDisplayedChild();
                if (childId == 0) {
                    finish();
                }
                previous();
            }
        });
        //ry.jeong : VZW mode change ODR SU issue [E]

        mBack.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                /*
                int childId = mHint.getDisplayedChild();
                if (childId == 0) {
                    finish();
                }
                previous();
                */
                finish();
            }
        });
    }

    private void next() {
        int childId = mHint.getDisplayedChild();
        //int childCount = mHint.getChildCount();

        if (childId != PAGE_COUNT) {
            stopAnimation(mAnimation);
            mHint.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
            mHint.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));

            mHint.setDisplayedChild(childId + 1);
            setAnimationView(childId + 1);
            mHandler.postDelayed(mRunnable, START_DELAY);
        }
    }

    private void previous() {
        int childId = mHint.getDisplayedChild();
        //int childCount = mHint.getChildCount();

        if (childId != 0) {
            stopAnimation(mAnimation);
            mHint.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
            mHint.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));

            mHint.setDisplayedChild(childId - 1);
            setAnimationView(childId - 1);
            mHandler.postDelayed(mRunnable, START_DELAY);
            mNext.setText(R.string.easytouse_next);
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

    private void setAnimationView(final int id, final int drawable) {
        // TODO Auto-generated method stub
        mImageView = (ImageView)findViewById(id);
        mImageView.setBackgroundResource(drawable);
        Drawable background = (Drawable)mImageView.getBackground().getCurrent();
        if (background instanceof AnimationDrawable) {
            mAnimation = (AnimationDrawable)background;
        }
    }

    void setAnimationView(int viewId) {
        clearNavigation();
        HashMap<String, String> viewItem = mAnimationViewArrayList.get(viewId);
        int id = Integer.parseInt(viewItem.get("id"));
        int drawable = Integer.parseInt(viewItem.get("drawable"));
        int titleRes = Integer.parseInt(viewItem.get("title"));
        int desc1 = Integer.parseInt(viewItem.get("desc1"));
        int desc2 = Integer.parseInt(viewItem.get("desc2"));
        int navigationRes = Integer.parseInt(viewItem.get("navigation"));

        mTitle.setText(titleRes);
        mDesc1.setText(desc1);
        mDesc2.setText(desc2);

        mImageViewArray.get(viewId).setImageResource(navigationRes);

        if (PAGE_COUNT == viewId) {
            mBack.setVisibility(View.GONE);
            //ry.jeong : VZW mode change ODR SU issue [S]
            mCenter.setVisibility(View.GONE);
            mNext.setText(R.string.easytouse_close_preview);
            //ry.jeong : VZW mode change ODR SU issue [E]
        } else {
            //ry.jeong : VZW mode change ODR SU issue [S]
            if (0 == viewId) {
                mCenter.setEnabled(false);
            } else {
                mCenter.setEnabled(true);
            }
            //ry.jeong : VZW mode change ODR SU issue [E]
            mBack.setVisibility(View.VISIBLE);
        }

        setDescriptionVisible();
        setAnimationView(id, drawable);

    }

    HashMap<String, String> setViewItemGroup(HashMap<String, String> viewItem, int viewId) {
        if (IsStart) {
            switch (viewId) {
            case 0:
                viewItem.put("id", Integer.toString(R.id.image_home));
                if ((Config.VZW).equals(Config.getOperator())) {
                    viewItem.put("drawable",
                            Integer.toString(R.drawable.img_mode_change_homescreen_starter_vzw));
                } else {
                    viewItem.put("drawable",
                            Integer.toString(R.drawable.img_mode_change_homescreen_starter));
                }
                //viewItem.put("drawable", Integer.toString(R.drawable.lg_easyui_setting_starter_homescreen));
                viewItem.put("title", Integer.toString(R.string.easytouse_home_screen));
                viewItem.put("desc1", Integer.toString(R.string.easytouse_home_starter_desc1));
                viewItem.put("desc2", Integer.toString(R.string.easytouse_home_starter_desc2));
                viewItem.put("navigation", Integer.toString(R.drawable.gesture_ani_navi_active));
                viewItem.put("mNext", Integer.toString(R.string.easytouse_next));
                break;
            case 1:
                viewItem.put("id", Integer.toString(R.id.image_call));
                viewItem.put("drawable",
                        Integer.toString(R.drawable.img_mode_change_incomingcall_starter));
                viewItem.put("title", Integer.toString(R.string.easytouse_incoming_call));
                viewItem.put("desc1", Integer.toString(R.string.easytouse_home_starter_desc1));
                viewItem.put("desc2", Integer.toString(R.string.easytouse_incoming_starter_desc2));
                viewItem.put("navigation", Integer.toString(R.drawable.gesture_ani_navi_active));
                viewItem.put("mNext", Integer.toString(R.string.easytouse_next));
                break;
            case 2:
                viewItem.put("id", Integer.toString(R.id.image_lock));
                viewItem.put("drawable",
                        Integer.toString(R.drawable.img_mode_change_lockscreen_starter));
                viewItem.put("title", Integer.toString(R.string.easytouse_lock_screen));
                viewItem.put("desc1", Integer.toString(R.string.easytouse_home_starter_desc1));
                viewItem.put("desc2", Integer.toString(R.string.easytouse_lock_starter_desc2));
                viewItem.put("navigation", Integer.toString(R.drawable.gesture_ani_navi_active));
                //ry.jeong : VZW mode change ODR SU issue [S]
                viewItem.put("mNext", Integer.toString(R.string.easytouse_close_preview));
                //ry.jeong : VZW mode change ODR SU issue [E]
                break;
            default:
                break;
            }
        }
        else {
            switch (viewId) {
            case 0:
                viewItem.put("id", Integer.toString(R.id.image_home));
                viewItem.put("drawable",
                        Integer.toString(R.drawable.img_mode_change_homescreen));
                viewItem.put("title", Integer.toString(R.string.easytouse_home_screen));
                //viewItem.put("desc1", Integer.toString(R.string.easytouse_home_standard_desc1));
                viewItem.put("desc1", Integer.toString(R.string.easytouse_home_starter_desc1));
                viewItem.put("desc2", Integer.toString(R.string.easytouse_home_standard_desc2));
                viewItem.put("navigation", Integer.toString(R.drawable.gesture_ani_navi_active));
                viewItem.put("mNext", Integer.toString(R.string.easytouse_next));
                break;
            case 1:
                viewItem.put("id", Integer.toString(R.id.image_call));
                viewItem.put("drawable",
                        Integer.toString(R.drawable.img_mode_change_incomingcall));
                viewItem.put("title", Integer.toString(R.string.easytouse_incoming_call));
                //viewItem.put("desc1", Integer.toString(R.string.easytouse_home_standard_desc1));
                viewItem.put("desc1", Integer.toString(R.string.easytouse_home_starter_desc1));
                viewItem.put("desc2", Integer.toString(R.string.easytouse_incoming_standard_desc2));
                viewItem.put("navigation", Integer.toString(R.drawable.gesture_ani_navi_active));
                viewItem.put("mNext", Integer.toString(R.string.easytouse_next));
                break;
            case 2:
                viewItem.put("id", Integer.toString(R.id.image_lock));
                viewItem.put("drawable",
                        Integer.toString(R.drawable.img_mode_change_lockscreen));
                viewItem.put("title", Integer.toString(R.string.easytouse_lock_screen));
                //viewItem.put("desc1", Integer.toString(R.string.easytouse_home_standard_desc1));
                viewItem.put("desc1", Integer.toString(R.string.easytouse_home_starter_desc1));
                viewItem.put("desc2", Integer.toString(R.string.easytouse_lock_standard_desc2));
                viewItem.put("navigation", Integer.toString(R.drawable.gesture_ani_navi_active));
                //ry.jeong : VZW mode change ODR SU issue [S]
                viewItem.put("mNext", Integer.toString(R.string.easytouse_close_preview));
                //ry.jeong : VZW mode change ODR SU issue [E]
                break;
            default:
                break;
            }
        }
        return viewItem;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        setIsLandscape(newConfig);
        super.onConfigurationChanged(newConfig);

    }

    public void setIsLandscape(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            isLandscape = false;
        }
    }

    public void setDescriptionVisible() {
        if (isLandscape) {
            mDesc1.setVisibility(View.INVISIBLE);
        }
        else {
            mDesc1.setVisibility(View.GONE);
        }
    }
}
