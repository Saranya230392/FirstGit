package com.android.settings.lge;

import com.android.settings.ImagePreference;
import com.android.settings.R;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.Utils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.lge.constants.SettingsConstants;
import com.android.settings.DoubleTitleListPreference;
import com.android.settings.deviceinfo.Memory;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.LGIntent;

/**
@class       Quick Circle
@date        2014-03-05
@author      seungyeop.yeom@lge.com
@brief       Quick Circle operation
@warning     New feature
*/

public class QuickCircleMain extends Activity {

    private int mCircleWidth = 0;
    private int mCircleHeight = 0;
    private int mCircleXPos = 0;
    private int mCircleYPos = 0;
    private int mCircleDiameter = 0;

    private View mMainView;
    private View mLightView;
    private LayoutInflater mInflater;

    private FrameLayout mMainLayout;
    private TextView mIndiText;
    private ImageView mFirstButton;
    private ImageView mSecondButton;
    private View mMarginView;
    public TextView mDescText;
    private ImageView mBackEnterButton;
    private ContentResolver mResolver;
    private Context mContext;
    private ImageView mMaskView;

    Window mQuickCoverWindow;
    public boolean isCoverOpened = false;

    private GestureDetector mGestures = null;
    private final int mknockOnScreenOff = 1000;

    // [2014-03-05][seungyeop.yeom] Receiver of Quick Circle state (open/close)
    private BroadcastReceiver mCoverStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            if (intent.getAction().equals(LGIntent.ACTION_ACCESSORY_COVER_EVENT)) {
                int coverState = intent.getIntExtra(LGIntent.EXTRA_ACCESSORY_COVER_STATE, 0);
                Log.d("YSY", "quick circle event start");
                if (coverState == LGIntent.EXTRA_ACCESSORY_COVER_OPENED) {
                    // Open state (Close -> Open)
                    Log.d("YSY", "quick circle Cover opened");
                    isCoverOpened = true;

                    ComponentName settingComp;
                    settingComp = new ComponentName("com.android.settings",
                            "com.android.settings.lge.QuickWindowCase");
                    Intent settingIntent = new Intent(Intent.ACTION_MAIN);
                    settingIntent.setComponent(settingComp);
                    settingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(settingIntent);
                    finish();
                } else if (coverState == LGIntent.EXTRA_ACCESSORY_COVER_CLOSED) {
                    // Open state (Open -> Close)
                    Log.d("YSY", "quick circle Cover closed");
                    isCoverOpened = false;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCircleWidth = getResources().getDimensionPixelSize(
                com.lge.R.dimen.config_circle_window_width) - 1;
        mCircleHeight = getResources().getDimensionPixelSize(
                com.lge.R.dimen.config_circle_window_height);
        mCircleXPos = getResources().getDimensionPixelOffset(
                com.lge.R.dimen.config_circle_window_x_pos);
        mCircleYPos = getResources().getDimensionPixelOffset(
                com.lge.R.dimen.config_circle_window_y_pos);
        mCircleDiameter = getResources().getDimensionPixelOffset(
                com.lge.R.dimen.config_circle_diameter);

        Log.d("YSY", "CircleWidth : " + getResources().getDimensionPixelSize(
                com.lge.R.dimen.config_circle_window_width));
        Log.d("YSY", "CircleHeight : " + getResources().getDimensionPixelSize(
                com.lge.R.dimen.config_circle_window_height));
        Log.d("YSY", "CircleXPos : " + getResources().getDimensionPixelOffset(
                com.lge.R.dimen.config_circle_window_x_pos));
        Log.d("YSY", "CircleYPos : " + getResources().getDimensionPixelOffset(
                com.lge.R.dimen.config_circle_window_y_pos));
        Log.d("YSY", "mCircleDiameter : " + getResources().getDimensionPixelOffset(
                com.lge.R.dimen.config_circle_diameter));

        Log.d("YSY", "CircleWidth : " + mCircleWidth);
        Log.d("YSY", "CircleHeight : " + mCircleHeight);
        Log.d("YSY", "CircleXPos : " + mCircleXPos);
        Log.d("YSY", "CircleYPos : " + mCircleYPos);
        Log.d("YSY", "mCircleDiameter : " + mCircleDiameter);

        // Quick Circle is raised on the LockScreen
        mQuickCoverWindow = getWindow();
        mQuickCoverWindow.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        mContext = getApplicationContext();
        mResolver = getContentResolver();

        mMainView = null;
        mLightView = null;
        mInflater = LayoutInflater.from(this);

        processDoubleTapEvent();
        // main screen is displayed.
        establishMainState();
    }

    // [2014-03-05][seungyeop.yeom] layout of Quick Circle main
    private void establishMainState() {
        if (isCoverOpened) {
            finish();
        }

        if (mMainView == null) {
            mMainView = mInflater.inflate(R.layout.quick_circle_main, null);
            mMainLayout = (FrameLayout)mMainView.findViewById(R.id.main_layout);
            mFirstButton =
                    (ImageView)mMainView.findViewById(R.id.first_button);
            mSecondButton =
                    (ImageView)mMainView.findViewById(R.id.second_button);
            mBackEnterButton = (ImageView)mMainView.findViewById(R.id.back_button);

            mMarginView = (View)mMainView.findViewById(R.id.margin_view);
            mMaskView = (ImageView)mMainView.findViewById(R.id.mask_image);

            mIndiText = (TextView)mMainView.findViewById(R.id.indi_text);
            mDescText = (TextView)mMainView.findViewById(R.id.desc_text);

            if (mMainLayout != null) {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mCircleWidth,
                        mCircleHeight);
                mMaskView
                        .setLayoutParams(new FrameLayout.LayoutParams(mCircleWidth, mCircleHeight));

                if (mCircleXPos < 0) {
                    layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                } else {
                    layoutParams.leftMargin = mCircleXPos;
                }

                if (mCircleYPos < 0) {
                    layoutParams.gravity = Gravity.CENTER_VERTICAL;
                } else {
                    layoutParams.topMargin = mCircleYPos;
                }

                mMainLayout.setLayoutParams(layoutParams);
            }

            if (mFirstButton != null) {
                mFirstButton.setImageResource(R.drawable.img_launcher_quickcircle_clocktheme);
                mFirstButton.setOnClickListener(mMainClickButtonListener);
            }

            if (mSecondButton != null) {
                mSecondButton.setImageResource(R.drawable.img_launcher_quickcircle_lighting);
                mSecondButton.setOnClickListener(mMainClickButtonListener);
            }

            if (mBackEnterButton != null) {
                mBackEnterButton.setOnClickListener(mMainClickButtonListener);
                mBackEnterButton.setImageResource(R.drawable.ic_sysbar_back_button);
            }

            if (mIndiText != null) {
                mIndiText.setText(R.string.data_kt_paypopup_setting);
            }

            if (mDescText != null) {
                mDescText.setText(R.string.quick_circle_open_guide_summary);
            }
        }

        setContentView(mMainView);
    }

    // [2014-03-05][seungyeop.yeom] layout of Quick Circle lighting
    private void establishLightState() {
        if (mLightView == null) {
            mLightView = mInflater.inflate(R.layout.quick_circle_main, null);
            mMainLayout = (FrameLayout)mLightView.findViewById(R.id.main_layout);
            mFirstButton =
                    (ImageView)mLightView.findViewById(R.id.first_button);
            mSecondButton =
                    (ImageView)mLightView.findViewById(R.id.second_button);
            mBackEnterButton = (ImageView)mLightView.findViewById(R.id.back_button);

            mMarginView = (View)mLightView.findViewById(R.id.margin_view);
            mMaskView = (ImageView)mMainView.findViewById(R.id.mask_image);

            mIndiText = (TextView)mLightView.findViewById(R.id.indi_text);
            mDescText = (TextView)mLightView.findViewById(R.id.desc_text);

            if (mMainLayout != null) {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mCircleWidth,
                        mCircleHeight);
                mMaskView
                        .setLayoutParams(new FrameLayout.LayoutParams(mCircleWidth, mCircleHeight));

                if (mCircleXPos < 0) {
                    layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                } else {
                    layoutParams.leftMargin = mCircleXPos;
                }

                if (mCircleYPos < 0) {
                    layoutParams.gravity = Gravity.CENTER_VERTICAL;
                } else {
                    layoutParams.topMargin = mCircleYPos;
                }

                mMainLayout.setLayoutParams(layoutParams);
            }

            if (mFirstButton != null) {
                mFirstButton.setImageResource(R.drawable.quickcircle_light_off_button);
                mFirstButton.setOnTouchListener(mLightTouchButtonListener);
            }

            if (mSecondButton != null) {
                mSecondButton.setVisibility(View.GONE);
            }

            if (mMarginView != null) {
                mMarginView.setVisibility(View.GONE);
            }

            if (mBackEnterButton != null) {
                mBackEnterButton.setOnClickListener(mLightClickButtonListener);
                mBackEnterButton.setImageResource(R.drawable.ic_sysbar_back_button);
            }

            if (mIndiText != null) {
                mIndiText.setText(R.string.data_kt_paypopup_setting);
            }

            if (mDescText != null) {
                mDescText.setText(R.string.quick_circle_lighting_switch_summary);
            }

            boolean isEnabled = (Settings.Global.getInt(mResolver,
                    SettingsConstants.Global.QUICK_CIRCLE_LIGHT, 1) != 0);
            if (isEnabled) {
                mFirstButton.setImageResource(R.drawable.quickcircle_light_on_button);
            } else if (isEnabled) {
                mFirstButton.setImageResource(R.drawable.quickcircle_light_off_button);
            }
        }

        setContentView(mLightView);
    }

    // [2014-03-05][seungyeop.yeom] Button listener of Quick Circle main
    private ImageView.OnClickListener mMainClickButtonListener = new ImageView.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.first_button:
                ComponentName guideComp;
                guideComp = new ComponentName("com.lge.clock",
                        "com.lge.clock.quickcover.QuickCoverSettingActivity");
                Intent guideIntent = new Intent(Intent.ACTION_MAIN);
                guideIntent.setComponent(guideComp);
                startActivity(guideIntent);
                break;

            case R.id.second_button:
                establishLightState();
                break;

            case R.id.back_button:
                finish();
                break;

            default:
                break;
            }
        }
    };

    // [2014-03-05][seungyeop.yeom] Button listener of Quick Circle lighting
    private ImageView.OnClickListener mLightClickButtonListener = new ImageView.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.back_button:
                establishMainState();
                break;

            default:
                break;
            }
        }
    };

    // [2014-03-05][seungyeop.yeom] Touch listener of Quick Circle lighting
    private ImageView.OnTouchListener mLightTouchButtonListener = new ImageView.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            boolean isEnabled = (Settings.Global.getInt(mResolver,
                    SettingsConstants.Global.QUICK_CIRCLE_LIGHT, 1) != 0);
            int touchAction = event.getAction();
            v.setSoundEffectsEnabled(true);
            switch (touchAction) {
            case MotionEvent.ACTION_UP:
                v.playSoundEffect(SoundEffectConstants.CLICK);
                if (isEnabled) {
                    Settings.Global.putInt(mResolver,
                            SettingsConstants.Global.QUICK_CIRCLE_LIGHT, 0);
                    mFirstButton.setImageResource(R.drawable.quickcircle_light_off_button);
                } else {
                    Settings.Global.putInt(mResolver,
                            SettingsConstants.Global.QUICK_CIRCLE_LIGHT, 1);
                    mFirstButton.setImageResource(R.drawable.quickcircle_light_on_button);
                }

                break;

            default:
                break;
            }

            return false;
        }

    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        unregisterReceiver(mCoverStateReceiver);
        // [END]

        finish();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(LGIntent.ACTION_ACCESSORY_COVER_EVENT);
        registerReceiver(mCoverStateReceiver, filter);
    }

    // [START][2014-04-02][seungyeop.yeom] merge gesture for KnockOff on QuickCover
    private void processDoubleTapEvent() {
        mGestures = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            private MotionEvent mSecondDownEvent = null;
            private int mDoubleTapTimeout = ViewConfiguration.getDoubleTapTimeout();
            private int mDoubleTapSlop = ViewConfiguration.get(mContext).getScaledDoubleTapSlop();
            private int mDoubleTapSlopSquare = mDoubleTapSlop * mDoubleTapSlop;

            private boolean isConsideredDoubleTap(MotionEvent secondDown, MotionEvent secondUp) {
                if (secondUp.getEventTime() - secondDown.getEventTime() > mDoubleTapTimeout) {
                    return false;
                }
                int deltaX = (int)secondDown.getX() - (int)secondUp.getX();
                int deltaY = (int)secondDown.getY() - (int)secondUp.getY();
                return (deltaX * deltaX + deltaY * deltaY < mDoubleTapSlopSquare);
            }

            public boolean onDoubleTapEvent(MotionEvent ev) {
                final int action = ev.getAction();
                switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mSecondDownEvent = ev.copy();
                    break;

                case MotionEvent.ACTION_UP:
                    if (mSecondDownEvent != null) {
                        if (isConsideredDoubleTap(mSecondDownEvent, ev)) {
                            Log.d("YSY", " GestureDetector onDoubleTap");
                            sendKnockOnScreenOffMsg(mknockOnScreenOff);
                        }
                        mSecondDownEvent.recycle();
                    }
                    break;
                default:
                    break;
                }
                return false;
            }
        });
    }

    Handler mKnockOnHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg == null) {
                return;
            }
            int type = msg.what;
            switch (type) {
            case mknockOnScreenOff:
                PowerManager pm = (PowerManager)mContext
                        .getSystemService(Context.POWER_SERVICE);
                try {
                    pm.goToSleep(SystemClock.uptimeMillis());
                } catch (Exception ex) {
                    Log.e("YSY", "onDoubleTap Exception = " + ex);
                }
                break;

            default:
                break;
            }
        }
    };

    private void sendKnockOnScreenOffMsg(int type) {
        if (mKnockOnHandler == null) {
            return;
        }
        if (mKnockOnHandler.hasMessages(type)) {
            mKnockOnHandler.removeMessages(type);
        }
        mKnockOnHandler.sendEmptyMessage(type);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestures != null && getGestureTurnScreenOn(mContext)) {
            if (mGestures.onTouchEvent(event)) {
                return true;
            } else if (super.onTouchEvent(event)) {
                return false;
            } else {
                return false;
            }
        } else {
            return super.onTouchEvent(event);
        }
    }

    public static boolean getGestureTurnScreenOn(Context mContext) {
        if (mContext == null) {
            return false;
        }
        ContentResolver resolver = mContext.getContentResolver();
        if (resolver == null) {
            return false;
        }
        boolean geustureTurnScreenOn = false;
        //multi-user
        geustureTurnScreenOn = (Settings.System.getIntForUser(resolver, "gesture_trun_screen_on",
                0, UserHandle.USER_CURRENT) == 1 ? true : false);
        return geustureTurnScreenOn;
    }
    // [END]
}
