package com.android.settings.lge;

import android.database.ContentObserver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.os.SystemProperties;

import com.android.settings.R;
import com.lge.constants.SettingsConstants;

//LGE_CHANGE_S Thermal configuration
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
//LGE_CHANGE_E Thermal configuration
import java.util.Locale;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.lge.systemservice.core.LGPowerManagerHelper;
import com.lge.systemservice.core.LGContext;

public class BrightnessPreferenceTablet extends SeekBarPreferenceTablet implements
        CheckBox.OnCheckedChangeListener, android.view.View.OnClickListener {

    public static final int HANDLER_MSG_STORE_BRIGHTNESS = 1;
    public static final int STORE_BRIGHTNESS_DELAY = 500;

    //LGE_CHANGE_S Thermal configuration
    private Context mContext;
    private int mThermalMaxBrightness = MAXIMUM_BACKLIGHT;
    private Toast mToast;
    //LGE_CHANGE_E Thermal configuration
    //LGE_CHANGE_S [VZW Only] Brightness / Heat sensor POP_UP scenario
    static int mBatteryTemperature = 0;
    //private static final int WARNING_TEMPERATURE = 480;
    //private static final int WARNING_BRIGHTNESS_VALUE = 196;
    //LGE_CHANGE_E [VZW Only] Brightness / Heat sensor POP_UP scenario
    private View mtempView;
    private static final String TAG = "BrightnessPreferenceTablet";
    private SeekBar mSeekBar;
    private CheckBox mAutomaticBox;
    private int mOldBrightness;
    private boolean mAutomaticAvailable;
    private static final int MAXIMUM_BACKLIGHT = android.os.PowerManager.BRIGHTNESS_ON; // yonguk.kim JB+ Migration 20130125
    private int mScreenBrightnessDim;
    LGContext mServiceContext;
    private NightModeInfo mNightModeInfo;
    private CheckBox mNightModeBox;
    private TextView mNightSummary;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_MSG_STORE_BRIGHTNESS) {
                final ContentResolver resolver = getContext().getContentResolver();
                int value = msg.arg1;
                Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, value);
                Settings.System.putInt(resolver, SettingsConstants.System.SCREEN_BRIGHTNESS_CUSTOM,
                        value);
                Settings.System.putInt(getContext().getContentResolver(),
                        SettingsConstants.System.CUSTOM_SCREEN_BRIGHTNESS, 1);
            }
        }
    };
    private TextView mBrightnessPercentView;
    private TextView mSummaryView;

    public void updateTabletNightCheckBox() {
        if (mNightModeBox != null) {
            mNightModeBox.setChecked(Settings.System.getInt
                    (getContext().getContentResolver(), "check_night_mode", 0) > 0);
        }

    }

    public BrightnessPreferenceTablet(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //LGE_CHANGE_S Thermal configuration
        mContext = context;
        //LGE_CHANGE_E Thermal configuration
        init();
    }

    public BrightnessPreferenceTablet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BrightnessPreferenceTablet(Context context) {
        this(context, null);
    }

    private void init() {
        if (getContext() != null && getContext().getResources() != null)
        {
            mScreenBrightnessDim = Config.getFWConfigInteger(mContext, com.android.internal.R.integer.config_screenBrightnessDim, 
                                    "com.android.internal.R.integer.config_screenBrightnessDim");
        }
        setMax(MAXIMUM_BACKLIGHT - mScreenBrightnessDim);
        mOldBrightness = getBrightness(0);
        setProgress(mOldBrightness - mScreenBrightnessDim);

        mAutomaticAvailable = Config.getFWConfigBool(mContext, com.android.internal.R.bool.config_automatic_brightness_available, 
                                               "com.android.internal.R.bool.config_automatic_brightness_available");
        //LGE_CHANGE_S Thermal configuration
        getThermalBrightMax();
        //LGE_CHANGE_E Thermal configuration
        mNightModeInfo = new NightModeInfo(mContext);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mtempView = view;
        mSeekBar = (SeekBar)view.findViewById(R.id.tablet_seekbar);
        mAutomaticBox = (CheckBox)view.findViewById(R.id.tablet_automatic_mode);
        mBrightnessPercentView = (TextView)view.findViewById(R.id.sub_title);
        mSummaryView = (TextView)view.findViewById(com.android.internal.R.id.summary);

        mAutomaticBox.setOnClickListener(this);
        mNightModeBox = (CheckBox)view.findViewById(R.id.night_mode_check);
        mNightModeBox.setOnClickListener(this);
        mNightSummary = (TextView)view.findViewById(R.id.night_summary);

        //LGE_CHANGE_S Thermal configuration
        if (mOldBrightness > mThermalMaxBrightness) {
            mSeekBar.setProgress(mThermalMaxBrightness - mScreenBrightnessDim);
        } else {
            //LGE_CHANGE_E Thermal configuration
            mSeekBar.setProgress(mOldBrightness - mScreenBrightnessDim);
        }

        String summary;
        if (mAutomaticAvailable) {
            mAutomaticBox.setOnCheckedChangeListener(this);
            boolean bChecked = getBrightnessMode(0) != 0;
            mAutomaticBox.setChecked(bChecked);
            if (bChecked) {
                Settings.System.putInt(getContext().getContentResolver(),
                        SettingsConstants.System.SCREEN_BRIGHTNESS_MODE_CUSTOM, 1);
                Settings.System.putInt(getContext().getContentResolver(),
                        SettingsConstants.System.CUSTOM_SCREEN_BRIGHTNESS, 1);
                if ("VZW".equals(Config.getOperator())
                        || "ATT".equals(Config.getOperator())) {
                    summary = getContext().getString(R.string.display_easy_brightness_vzw_summary);
                } else {
                    summary = getContext().getString(R.string.sp_brightness_auto_checked);
                }
            } else {
                Settings.System.putInt(getContext().getContentResolver(),
                        SettingsConstants.System.SCREEN_BRIGHTNESS_MODE_CUSTOM, 0);
                Settings.System.putInt(getContext().getContentResolver(),
                        SettingsConstants.System.CUSTOM_SCREEN_BRIGHTNESS, 1);
                if ("VZW".equals(Config.getOperator())
                        || "ATT".equals(Config.getOperator())) {
                    summary = getContext().getString(
                            R.string.sp_brightness_auto_unchecked_summary_NORMAL);
                } else {
                    summary = getContext().getString(R.string.sp_brightness_auto_checked);
                }
            }

            if (mAutomaticBox.isChecked() && noapplyMultiALC()) {
                mSeekBar.setEnabled(false);
            } else {
                mSeekBar.setEnabled(true);
            }

            mSummaryView.setText(summary);
            mSummaryView.setVisibility(View.VISIBLE);
            mNightModeBox.setVisibility(View.GONE);
            mNightSummary.setVisibility(View.GONE);
        } else {
            mAutomaticBox.setVisibility(View.GONE);
            summary = getContext().getString(R.string.sp_brightness_sub_title_NORMAL);
            mSummaryView.setVisibility(View.GONE);
            mNightModeBox.setChecked(mNightModeInfo.getNightCheckDB() == 1);
            mNightModeBox.setOnCheckedChangeListener(this);

            boolean is24 = DateFormat.is24HourFormat(mContext);
            String text = Utils.getResources().getString(is24 ?
                    R.string.brightness_night_mode_24 : R.string.brightness_night_mode_12);
            mNightSummary.setText(text);
        }

        String percent = null;
        if (Utils.isRTLLanguage()) {
            if (((mSeekBar.getProgress() * 100 / getMax())) == 0 && (mSeekBar.getProgress() != 0)) {
                percent = "(%"
                        + String.format(Locale.getDefault(), "%d",
                                1) + ")";
            } else {
                percent = "(%"
                        + String.format(Locale.getDefault(), "%d",
                                (mSeekBar.getProgress() * 100 / getMax())) + ")";
            }
        } else {
            if (((mSeekBar.getProgress() * 100 / getMax())) == 0 && (mSeekBar.getProgress() != 0)) {
                percent = "(1%)";
            } else {
                percent = "(" + (mSeekBar.getProgress() * 100 / getMax()) + "%)";
            }
        }
        mBrightnessPercentView.setText(percent);
        if (mAutomaticBox.isChecked() && mAutomaticAvailable && noapplyMultiALC()) {
            mBrightnessPercentView.setVisibility(View.GONE);
        } else {
            mBrightnessPercentView.setVisibility(View.VISIBLE);
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mAutomaticBox) {
            setMode(isChecked ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                    : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

            if (!isChecked) {
                setBrightness(mSeekBar.getProgress() + mScreenBrightnessDim, false);
                if ("VZW".equals(Config.getOperator())
                        || "ATT".equals(Config.getOperator())) {
                    mSummaryView.setText(R.string.sp_brightness_auto_unchecked_summary_NORMAL);
                } else {
                    mSummaryView.setText(R.string.sp_brightness_auto_checked);
                }
                mSeekBar.setEnabled(true);
                mBrightnessPercentView.setVisibility(View.VISIBLE);
            } else {
                if ("VZW".equals(Config.getOperator())
                        || "ATT".equals(Config.getOperator())) {
                    mSummaryView.setText(R.string.display_easy_brightness_vzw_summary);
                } else {
                    mSummaryView.setText(R.string.sp_brightness_auto_checked);
                }
                if (noapplyMultiALC()) {
                    mSeekBar.setEnabled(false);
                    mBrightnessPercentView.setVisibility(View.GONE);
                } else {
                    mSeekBar.setEnabled(true);
                    mBrightnessPercentView.setVisibility(View.VISIBLE);
                }
            }
        }
        if (buttonView == mNightModeBox) {
            if (isChecked) {
                mNightModeInfo.setNightDB(1);
            } else {
                mNightModeInfo.setNightDB(0);
                mNightModeInfo.setTabNightCheck(1);
            }
            mNightModeInfo.requestPendingIntent(mContext);
        }
    }

    public void onResume()
    {
        getThermalBrightMax();
        if (mtempView != null)
        {
            mOldBrightness = getBrightness(0);
            mtempView.invalidate();
            //            mOldBrightness = getBrightness(0);
            //            getThermalBrightMax();
            //            mSeekBar = (SeekBar) mtempView.findViewById(R.id.easy_seekbar);
            //            //LGE_CHANGE_S Thermal configuration
            //            if (mOldBrightness > mThermalMaxBrightness)
            //                mSeekBar.setProgress(mThermalMaxBrightness - mScreenBrightnessDim);
            //            else
            //            //LGE_CHANGE_E Thermal configuration
            //                mSeekBar.setProgress(mOldBrightness - mScreenBrightnessDim);
            //
            //            String percent = "("+(mSeekBar.getProgress()*100/getMax())+"%)";
            //            mBrightnessPercentView.setText(percent);
        }
    }

    private int getBrightness(int defaultValue) {
        int brightness = defaultValue;
        try {
            brightness = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException snfe) {
            Log.w(TAG, "SettingNotFoundException");
        }
        return brightness;
    }

    private int getBrightnessMode(int defaultValue) {
        int brightnessMode = defaultValue;
        try {
            brightnessMode = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException snfe) {
            Log.w(TAG, "SettingNotFoundException");
        }
        return brightnessMode;
    }

    private void setBrightness(int brightness, boolean force) {
        int temp = brightness;
        Log.d(TAG, "setBrightness() : Brightness=" + temp);
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(
                    ServiceManager.getService("power"));
            if (power != null) {
                if (mAutomaticBox.isChecked()) {
                    brightness -= mScreenBrightnessDim;
                    float valf = (((float)brightness * 2) / (MAXIMUM_BACKLIGHT - mScreenBrightnessDim)) - 1.0f; //JB native
                    power.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(valf); // yonguk.kim JB+ Migration 20130125
                    Log.d(TAG, "setBrightness() : MultiALC=" + valf);
                    if (force) {
                        final ContentResolver resolver = getContext().getContentResolver();
                        Settings.System.putFloat(resolver,
                                Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, valf);
                    }
                }
                else {
                    power.setTemporaryScreenBrightnessSettingOverride(brightness); // yonguk.kim JB+ Migration 20130125
                }
            }
        } catch (RemoteException doe) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void setMode(int mode) {

        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            //mSeekBar.setVisibility(View.GONE);
            setBrightness(mSeekBar.getProgress() + mScreenBrightnessDim, false);
        } else {
            mSeekBar.setVisibility(View.VISIBLE);
        }
        Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
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
            //LGE_CHANGE_S Thermal configuration
            getThermalBrightMax();
            if (progress + mScreenBrightnessDim > mThermalMaxBrightness) {
                //LGE_CHANGE_S [VZW Only] Brightness / Heat sensor POP_UP scenario
                if ("VZW".equals(Config.getOperator())) {
                    try {
                        mServiceContext = new LGContext(mContext);
                        LGPowerManagerHelper pmh = (LGPowerManagerHelper)mServiceContext
                                .getLGSystemService(LGContext.LGPOWERMANAGER_HELPER_SERVICE);
                        if (pmh != null) {
                            pmh.turnOffThermald();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
                    showToast(R.string.sp_brightness_over_max_2_NORMAL);
                    //LGE_CHANGE_E [VZW Only] Brightness / Heat sensor POP_UP scenario
                } else {
                    prog = mThermalMaxBrightness - mScreenBrightnessDim;
                    seekBar.setProgress(prog);
                    showToast(R.string.sp_brightness_over_max_NORMAL);
                }
            } else {
                //LGE_CHANGE_E Thermal configuration

            }
            setBrightness(prog + mScreenBrightnessDim, false);
            String percent = null;
            if (Utils.isRTLLanguage()) {
                if (((prog * 100 / getMax())) == 0 && (prog != 0)) {
                    percent = "(%" + String.format(Locale.getDefault(), "%d", 1)
                            + ")";
                } else {
                    percent = "(%"
                            + String.format(Locale.getDefault(), "%d", (prog * 100 / getMax()))
                            + ")";
                }
            } else {
                if (((prog * 100 / getMax())) == 0 && (prog != 0)) {
                    percent = "(1%)";
                } else {
                    percent = "(" + (prog * 100 / getMax()) + "%)";
                }
            }
            if (mBrightnessPercentView != null) {
                mBrightnessPercentView.setText(percent);
                if (mAutomaticBox.isChecked() && mAutomaticAvailable && noapplyMultiALC()) {
                    mBrightnessPercentView.setVisibility(View.GONE);
                } else {
                    mBrightnessPercentView.setVisibility(View.VISIBLE);
                }
            }
            sendStoreMsg(prog + mScreenBrightnessDim);
        }
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // NA
        //LGE_CHANGE_S [L1V Only] Brightness / Heat sensor POP_UP scenario
        Log.d(TAG, "onStopTrackingTouch()");
        setBrightness(seekBar.getProgress() + mScreenBrightnessDim, true);
        Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                mSeekBar.getProgress() + mScreenBrightnessDim);
    }

    public void onBrightnessChanged() {
        int brightness = getBrightness(MAXIMUM_BACKLIGHT);
        Log.d(TAG, "onBrightnessChanged() : brightness = " + brightness);
        int prog = brightness - mScreenBrightnessDim;
        //LGE_CHANGE_S [L1V Only] Brightness / Heat sensor POP_UP scenario
        if (!"VZW".equals(Config.getOperator())) {
            if (brightness > mThermalMaxBrightness) {
                prog = mThermalMaxBrightness - mScreenBrightnessDim;
            }
        }
        onResume();
        //LGE_CHANGE_E [L1V Only] Brightness / Heat sensor POP_UP scenario
        if (mSeekBar != null) {
            mSeekBar.setProgress(prog);

        }
        String percent = null;
        if (Utils.isRTLLanguage()) {
            if (((prog * 100 / getMax())) == 0 && (prog != 0)) {
                percent = "(%" + String.format(Locale.getDefault(), "%d", 1)
                        + ")";
            } else {
                percent = "(%" + String.format(Locale.getDefault(), "%d", (prog * 100 / getMax()))
                        + ")";
            }
        } else {
            if (((prog * 100 / getMax())) == 0 && (prog != 0)) {
                percent = "(1%)";
            } else {
                percent = "(" + (prog * 100 / getMax()) + "%)";
            }
        }
        if (mBrightnessPercentView != null) {
            mBrightnessPercentView.setText(percent);
            if (mAutomaticBox.isChecked() && mAutomaticAvailable && noapplyMultiALC()) {
                mBrightnessPercentView.setVisibility(View.GONE);
            } else {
                mBrightnessPercentView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onBrightnessModeChanged() {
        if (mAutomaticBox != null) {
            mAutomaticBox.setChecked(getBrightnessMode(0) != 0);
            onResume();
        }
    }

    public void updateState(int progress) {
        if (progress == 0) {
            progress = getBrightness(0) - mScreenBrightnessDim;
        }
        setProgress(progress);
        notifyChanged();
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub

    }

    //LGE_CHANGE_S Thermal configuration
    private void getThermalBrightMax()
    {
        String flag_read_path = new String("/sys/class/leds/lcd-backlight/max_brightness");
        String frag = "";
        BufferedReader inFlagFile = null;

        try
        {
            inFlagFile = new BufferedReader(new FileReader(flag_read_path));
            frag = inFlagFile.readLine();
            Log.d(TAG, "thermal max bright -> " + frag);
            inFlagFile.close();
            mThermalMaxBrightness = Integer.parseInt(frag);
        } catch (Exception e)
        {
            Log.d(TAG, "thermal max bright read fail" + frag);
            mThermalMaxBrightness = MAXIMUM_BACKLIGHT;
        } finally
        {
            try {
                if (inFlagFile != null) {
                    inFlagFile.close();
                }
            } catch (Exception e) {
                Log.w(TAG, "Exception");
            }
        }
    }

    private void showToast(int aStrID)
    {
        if (mContext == null) {
            return;
        }
        if (mToast == null) {
            mToast = Toast.makeText(mContext, aStrID, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(aStrID);
        }
        mToast.show();
    }

    //LGE_CHANGE_E Thermal configuration

    public boolean noapplyMultiALC() {
        if ("DCM".equals(Config.getOperator())
                || ("TMO".equals(Config.getOperator())
                && "US".equals(Config.getCountry()))) {
            return true;
        }
        return false;
    }
}
