package com.android.settings.lge;

import com.android.settings.lge.LedSeekBarPreference;
import com.android.settings.R;
import android.database.ContentObserver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.lge.systemservice.core.EmotionalLedManager;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.LGLedRecord;
import android.os.SystemProperties;
import android.graphics.Color;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;

// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;

public class LedBrightnessPreference extends LedSeekBarPreference {

    private static final int EMOTIONAL_LED_SEEK_BAR_NOTI = 10;

    public static final int HANDLER_MSG_STORE_BRIGHTNESS = 1;
    public static final int STORE_BRIGHTNESS_DELAY = 500;
    public static final int LED_BRIGHTNESS_DEFAULT = 255;

    private View mtempView;
    private static final String TAG = "LedBrightnessPreference";
    private SeekBar mSeekBar;
    private int mOldBrightness;
    private int mScreenBrightnessDim = 0;
    EmotionalLedManager mLedManager;
    LGContext mServiceContext;
    private ValueAnimator mValueAnimator;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_MSG_STORE_BRIGHTNESS) {
                final ContentResolver resolver = getContext().getContentResolver();
                int value = msg.arg1;
                Log.d(TAG, "save Brightness DB");
                Settings.System.putInt(resolver, SettingsConstants.System.LED_BRIGHTNESS, value);
            }
        }
    };
    private TextView mBrightnessPercentView;

    public LedBrightnessPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LedBrightnessPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LedBrightnessPreference(Context context) {
        this(context, null);
    }

    private ContentObserver mBrightnessObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onBrightnessChanged();
        }
    };

    private void init() {
        mServiceContext = new LGContext(getContext());
        if (mLedManager == null) {
            mLedManager = (EmotionalLedManager)mServiceContext
                    .getLGSystemService(LGContext.EMOTIONALLED_SERVICE);
        }
        setMax(LED_BRIGHTNESS_DEFAULT - mScreenBrightnessDim);
        mOldBrightness = getBrightness(LED_BRIGHTNESS_DEFAULT);
        setProgress(mOldBrightness - mScreenBrightnessDim);
        mValueAnimator = ValueAnimator.ofInt(0, 2000);
        mValueAnimator.setDuration(2000);

        mValueAnimator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
                // [Stop LED] 4sec is passed.
                mLedManager.stop(getContext().getPackageName(), EMOTIONAL_LED_SEEK_BAR_NOTI);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub
            }
        });

    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mtempView = view;
        mSeekBar = (SeekBar)view.findViewById(R.id.easy_seekbar);
        mBrightnessPercentView = (TextView)view.findViewById(R.id.sub_title);

        getContext().getContentResolver().registerContentObserver(
                Settings.System.getUriFor(SettingsConstants.System.LED_BRIGHTNESS), true,
                mBrightnessObserver);

        mSeekBar.setProgress(mOldBrightness - mScreenBrightnessDim);

        String percent = "(" + (mSeekBar.getProgress() * 100 / getMax()) + "%)";
        mBrightnessPercentView.setText(percent);
    }

    public void onResume()
    {
        if (mtempView != null)
        {
            mOldBrightness = getBrightness(0);
            mtempView.invalidate();
        }
    }

    private int getBrightness(int defaultValue) {
        int brightness = defaultValue;
        int sp_led_def_brightness = SystemProperties.getInt("ro.lge.led_default_brightness",
                LED_BRIGHTNESS_DEFAULT);
        brightness = Settings.System.getInt(getContext().getContentResolver(),
                SettingsConstants.System.LED_BRIGHTNESS, sp_led_def_brightness);
        Log.d(TAG, "getBrightness() : Brightness=" + brightness);
        return brightness;
    }

    private void setBrightness(int brightness, boolean force) {
        int temp = brightness;
        Log.d(TAG, "setBrightness() : Brightness=" + temp);
        mLedManager.setBrightness(brightness);
        /*
                try {
                    IPowerManager power = IPowerManager.Stub.asInterface(
                            ServiceManager.getService("power"));
                    if (power != null) {
                            power.setBacklightBrightness(brightness);
                    }
                } catch (RemoteException doe) {

                }
        */
    }

    private void sendStoreMsg(int brightness) {
        Message msg = Message.obtain();
        msg.what = HANDLER_MSG_STORE_BRIGHTNESS;
        msg.arg1 = brightness;
        while (mHandler.hasMessages(HANDLER_MSG_STORE_BRIGHTNESS)) {
            mHandler.removeMessages(HANDLER_MSG_STORE_BRIGHTNESS);
        }
        mHandler.sendMessageDelayed(msg, STORE_BRIGHTNESS_DELAY);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        super.onProgressChanged(seekBar, progress, fromUser);
        int prog = progress;
        if (fromUser == true) {
            setBrightness(prog + mScreenBrightnessDim, false);
            String percent = "(" + (prog * 100 / getMax()) + "%)";
            if (mBrightnessPercentView != null) {
                mBrightnessPercentView.setText(percent);
            }
            sendStoreMsg(prog + mScreenBrightnessDim);

        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // NA
        Log.d(TAG, "onStartTrackingTouch()");
        if (mValueAnimator.isRunning()) {
            mValueAnimator.end();
        } else {
            Log.d("hong", "getFunction(): " + EmotionalLEDEffectUtils.sShareFunction);
            mLedManager.stop(getContext().getPackageName(), EmotionalLEDEffectUtils.sShareFunction);
        }
        LGLedRecord record = new LGLedRecord();
        record.priority = LGLedRecord.PRIORITY_DEFAULT;
        record.patternId = LGLedRecord.ID_STOP;
        record.mNativeNotification = true;
        record.flags = LGLedRecord.FLAG_SHOW_LIGHTS_ONLY_LED_ON;
        record.onMS = 60000;
        record.offMS = 50;
        record.color = Color.GREEN;
        record.mExceptional = true;
        record.patternId = LGLedRecord.ID_STOP;
        mLedManager.start(getContext().getPackageName(), EMOTIONAL_LED_SEEK_BAR_NOTI, record);
        EmotionalLEDEffectUtils.sShareFunction = EMOTIONAL_LED_SEEK_BAR_NOTI;
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // NA
        Log.d(TAG, "onStopTrackingTouch()");
        //mLedManager.stop(getContext().getPackageName(), EMOTIONAL_LED_SEEK_BAR_NOTI);
        mValueAnimator.start();
        Settings.System.putInt(getContext().getContentResolver(),
                SettingsConstants.System.LED_BRIGHTNESS,
                mSeekBar.getProgress() + mScreenBrightnessDim);
    }

    private void onBrightnessChanged() {
        int brightness = getBrightness(LED_BRIGHTNESS_DEFAULT);
        int prog = brightness - mScreenBrightnessDim;
        onResume();
        mSeekBar.setProgress(prog);
        String percent = "(" + (prog * 100 / getMax()) + "%)";
        if (mBrightnessPercentView != null) {
            mBrightnessPercentView.setText(percent);
        }
    }

    public void updateState(int progress) {
        if (progress == 0) {
            progress = getBrightness(LED_BRIGHTNESS_DEFAULT) - mScreenBrightnessDim;
        }
        setProgress(progress);
        notifyChanged();
    }

}
