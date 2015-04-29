package com.android.settings.lge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Locale;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.lge.constants.SettingsConstants;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.ListPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.lge.systemservice.core.LGPowerManagerHelper;
import com.lge.systemservice.core.LGContext;
import com.android.settings.lgesetting.Config.Config;

public class DisplayWidget extends Activity {
    public static final String EXTRA_DISPLAY_WIDGET_DIALOG = "extra_display_widget_dialog";
    private String dialogMode;
    private DialogFragment dialog = null;
    private static boolean bBrightnessOK = false;
    private static final String TAG = "DisplayWidget";

    @Override
    protected void onCreate(Bundle args) {
        super.onCreate(args);

        Intent intent = getIntent();
        dialogMode = (String)intent.getExtra(EXTRA_DISPLAY_WIDGET_DIALOG);
        if (dialogMode.isEmpty()) {
            finish();
            return;
        }

        if (dialogMode.equals("ScreenTimeoutDialog")) {
            dialog = ScreenTimeoutDialog.newInstance(this);
        } else if (dialogMode.equals("BrightnessDialog")) {
            dialog = BrightnessDialog.newInstance(this);
        } else if (dialogMode.equals("FontSizeDialog")) {
            dialog = FontSizeDialog.newInstance(this);
        }

        if (dialog != null) {
            dialog.show(getFragmentManager(), dialogMode);
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if (dialog != null) {
            Log.d(TAG, "bBrightnessOK -" + bBrightnessOK);
            if (dialogMode.equals("BrightnessDialog") && !bBrightnessOK) {
                BrightnessDialog.restoreOldState();
            }
        }
    }

    public static class FontSizeDialog extends DialogFragment {
        private static final String TAG = "FontSizeDialog";

        private Context mContext;
        private final Configuration mCurConfig = new Configuration();

        private int mCurrentIndex;
        private int mEntriesResourceID;
        private int mValuesResourceID;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mContext = getActivity().getApplicationContext();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_title_font_size);

            if (!Utils.isUpgradeModel()) {
                mEntriesResourceID = R.array.sp_entries_font_size2;
                mValuesResourceID = R.array.entryvalues_font_size2;
                Log.d(TAG, "font size change");
            }
            else {
                mEntriesResourceID = R.array.entries_font_size;
                mValuesResourceID = R.array.entryvalues_font_size;
            }

            mCurrentIndex = getCurrentIndex();
            builder.setSingleChoiceItems(mEntriesResourceID, mCurrentIndex,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String[] values = mContext.getResources().getStringArray(
                                    mValuesResourceID);

                            Settings.Global.putInt(getActivity().getContentResolver(),
                                    "sync_large_text", 0);
                            if ("1.60".equals(values[which])) {
                                // [S] add Maximum font size for Accessbility 2011.11.22 by sunghee.won
                                //for refresh UI(resume)
                                applyFontSize((Object)"1.30");
                                // [E] add Maximum font size for Accessbility 2011.11.22 by sunghee.won
                                Settings.Global.putInt(getActivity().getContentResolver(),
                                        "sync_large_text", 1);
                                // noti applied app for Maximum Font
                                getActivity().sendBroadcast(
                                        new Intent("lge.settings.intent.action.FONT_SIZE"));
                                Log.d(TAG,
                                        "Font Size send intent(lge.settings.intent.action.FONT_SIZE)");
                            }
                            // [E] add Maximum font size for Accessbility 2011.11.22 by sunghee.won
                            else {
                                applyFontSize(values[which]);
                            }

                            dialog.dismiss();
                            getActivity().finish();
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            getActivity().finish();
                        }
                    });

            return builder.create();
        }

        public static FontSizeDialog newInstance(Activity activity) {
            return new FontSizeDialog();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            getActivity().finish();
        }

        private int getCurrentIndex() {
            try {
                mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to retrieve font size");
            }

            int index = floatToIndex(mCurConfig.fontScale);

            if (!Utils.isUpgradeModel()) {
                int bIsMaximumFont = Settings.Global.getInt(getActivity().getContentResolver(),
                        "sync_large_text", 0);
                if (index == 4 && bIsMaximumFont == 1) {
                    index = 5;
                }
            }

            return index;
        }

        private void applyFontSize(Object objValue) {
            try {
                Configuration conf = getResources().getConfiguration();
                if (conf != null) {
                    Log.d(TAG, String.format("orientation config  old:%d new:%d ",
                            mCurConfig.orientation, conf.orientation));
                    mCurConfig.orientation = conf.orientation;
                }

                mCurConfig.fontScale = Float.parseFloat(objValue.toString());
                ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to save font size");
            }
        }

        private int floatToIndex(float val) {
            int ValuesResourceID;
            if (!Utils.isUpgradeModel()) {
                ValuesResourceID = R.array.entryvalues_font_size2;
            }
            else {
                ValuesResourceID = R.array.entryvalues_font_size;
                Log.d(TAG, "normal font size");
            }
            String[] values = getResources().getStringArray(ValuesResourceID);

            float lastVal = Float.parseFloat(values[0]);
            for (int i = 1; i < values.length; i++) {
                float thisVal = Float.parseFloat(values[i]);
                if (val < (lastVal + (thisVal - lastVal) * .5f)) {
                    return i - 1;
                }
                lastVal = thisVal;
            }
            return values.length - 1;
        }
    }

    public static class ScreenTimeoutDialog extends DialogFragment {
        private static final String TAG = "ScreenTimeoutDialog";
        private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;

        private Context mContext;

        private TextView mTitle;
        private TextView mSubTitle;

        private long mCurrentTimeout;
        private int mCurrentIndex;
        private ArrayList<CharSequence> revisedEntries;
        private ArrayList<CharSequence> revisedValues;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mContext = getActivity().getApplicationContext();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_title_secondlines, null);
            builder.setCustomTitle(dialogView);

            mTitle = (TextView)dialogView.findViewById(R.id.title);
            mTitle.setText(R.string.sp_screen_timeout_NORMAL);

            mSubTitle = (TextView)dialogView.findViewById(R.id.subtitle);
            mSubTitle.setText(R.string.sp_screen_timeout_sub_title_NORMAL);

            mCurrentTimeout = Settings.System.getLong(mContext.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT,
                    FALLBACK_SCREEN_TIMEOUT_VALUE);

            createTimeoutList(builder);

            return builder.create();
        }

        public static ScreenTimeoutDialog newInstance(Activity activity) {
            return new ScreenTimeoutDialog();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            getActivity().finish();
        }

        private void createTimeoutList(AlertDialog.Builder builder) {
            final DevicePolicyManager dpm =
                    (DevicePolicyManager)getActivity().getSystemService(
                            Context.DEVICE_POLICY_SERVICE);
            final long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null) : 0;

            mCurrentTimeout = Settings.System.getLong(mContext.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT,
                    FALLBACK_SCREEN_TIMEOUT_VALUE);

            Log.d(TAG, "mCurrentTimeout=" + mCurrentTimeout);

            // Test code
            //            maxTimeout = 60 * 1000 * 15;
            if (maxTimeout == 0) {
                    IPowerManager power = IPowerManager.Stub.asInterface(
                            ServiceManager.getService("power"));
                    if (power != null) {
                        // FIXME : IPowerManager.setMaximumScreenOffTimeoutFromDeviceAdmin was remov
                        //power.setMaximumScreenOffTimeoutFromDeviceAdmin(Integer.MAX_VALUE);
                    }
            }

            final CharSequence[] entries = getResources().getTextArray(
                    R.array.sp_screen_timeout_entries2_NORMAL);
            final CharSequence[] values = getResources()
                    .getTextArray(R.array.screen_timeout_values);
            revisedEntries = new ArrayList<CharSequence>();
            revisedValues = new ArrayList<CharSequence>();

            if (maxTimeout == 0) {
                for (int i = 0; i < values.length; i++) {
                    revisedEntries.add(entries[i]);
                    revisedValues.add(values[i]);
                    Log.d(TAG, "entries[" + i + "]=" + entries[i]);
                    Log.d(TAG, "values[" + i + "]=" + values[i]);
                }

                mCurrentIndex = revisedValues.indexOf(Long.toString(mCurrentTimeout));
            } else {
                for (int i = 0; i < values.length; i++) {
                    long timeout = Long.parseLong(values[i].toString());
                    if (timeout <= maxTimeout) {
                        revisedEntries.add(entries[i]);
                        revisedValues.add(values[i]);
                        Log.d(TAG, "entries[" + i + "]=" + entries[i]);
                        Log.d(TAG, "values[" + i + "]=" + values[i]);
                    }
                }

                if (mCurrentTimeout <= maxTimeout) {
                    mCurrentIndex = revisedValues.indexOf(Long.toString(mCurrentTimeout));
                } else {
                    mCurrentIndex = revisedValues.size() - 1;
                }
            }

            if (mCurrentIndex == -1) {
                Settings.System.putInt(mContext.getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT,
                        FALLBACK_SCREEN_TIMEOUT_VALUE);
                mCurrentIndex = revisedValues.indexOf(Integer
                        .toString(FALLBACK_SCREEN_TIMEOUT_VALUE));

                if (mCurrentIndex == -1) {
                    Log.e(TAG, "Error index of timeout list");
                }
            }

            Log.d(TAG, "mCurrentIndex=" + mCurrentIndex);

            CharSequence[] tempEntries = new CharSequence[revisedEntries.size()];
            revisedEntries.toArray(tempEntries);
            builder.setSingleChoiceItems(tempEntries, mCurrentIndex,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            int value = Integer.parseInt(revisedValues.get(which).toString());
                            try {
                                Settings.System.putInt(mContext.getContentResolver(),
                                        Settings.System.SCREEN_OFF_TIMEOUT,
                                        value);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "could not persist screen timeout setting", e);
                            }

                            dialog.dismiss();
                            getActivity().finish();
                        }
                    });
        }
    }

    public static class BrightnessDialog extends DialogFragment implements OnSeekBarChangeListener,
            OnCheckedChangeListener {
        private static final String TAG = "BrightnessDialog";
        private static int MAXIMUM_BACKLIGHT;

        private static Context mContext;

        private static SeekBar mSeekBar;
        private static CheckBox mCheckBox;
        private TextView mSubTitle;
        private Toast mToast;

        //[jongwon007.kim] NightMode CheckBox , SubTitle
        private CheckBox mNightCheckBox;
        private TextView mSubTitleNight;

        private static boolean mAutomaticAvailable;
        private static int mScreenBrightnessDim;
        private static int mOldBrightness;
        private static int mOldAutomatic;
        private static boolean mRestoredOldState;
        private int mThermalMaxBrightness;
        LGContext mServiceContext;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mContext = getActivity().getApplicationContext();
            PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
            mThermalMaxBrightness = pm.getMaximumScreenBrightnessSetting();
            mScreenBrightnessDim = pm.getMinimumScreenBrightnessSetting();
            MAXIMUM_BACKLIGHT = pm.getMaximumScreenBrightnessSetting();

            mAutomaticAvailable = Config.getFWConfigBool(mContext, com.android.internal.R.bool.config_automatic_brightness_available, 
                                               "com.android.internal.R.bool.config_automatic_brightness_available");
            /*mScreenBrightnessDim = Config.getFWConfigInteger(this, com.android.internal.R.integer.config_screenBrightnessDim, 
                                    "com.android.internal.R.integer.config_screenBrightnessDim");*/

            mOldBrightness = getBrightness(0);
            mThermalMaxBrightness = getThermalMaxBrightness();

            bBrightnessOK = false;
            Log.d(TAG, "mOldBrightness=" + mOldBrightness);
            Log.d(TAG, "mThermalMaxBrightness=" + mThermalMaxBrightness);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.preference_dialog_brightness, null);
            builder.setView(dialogView);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    final ContentResolver resolver = mContext.getContentResolver();

                    Settings.System.putInt(resolver,
                            Settings.System.SCREEN_BRIGHTNESS,
                            mSeekBar.getProgress() + mScreenBrightnessDim);
                    Settings.System.putInt(resolver,
                            SettingsConstants.System.SCREEN_BRIGHTNESS_CUSTOM,
                            mSeekBar.getProgress() + mScreenBrightnessDim);
                    int mode = Settings.System.getInt(mContext.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
                    Settings.System.putInt(mContext.getContentResolver(),
                            SettingsConstants.System.SCREEN_BRIGHTNESS_MODE_CUSTOM, mode
                            );
                    Settings.System.putInt(mContext.getContentResolver(),
                            SettingsConstants.System.CUSTOM_SCREEN_BRIGHTNESS, 1
                            );

                    Log.d(TAG, "setPositiveButton() : brightness=" + mSeekBar.getProgress()
                            + mScreenBrightnessDim);
                    Log.d(TAG, "setPositiveButton() : custom brightness=" + mSeekBar.getProgress()
                            + mScreenBrightnessDim);
                    Log.d(TAG, "setPositiveButton() : custom mode=" + mode);
                    bBrightnessOK = true;
                    dialog.dismiss();
                    getActivity().finish();
                }
            });
            builder.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            restoreOldState();
                            Log.d(TAG, "setNegativeButton()");

                            dialog.dismiss();
                            getActivity().finish();
                        }
                    });

            mSeekBar = (SeekBar)dialogView.findViewById(R.id.seekbar);
            mSeekBar.setMax(MAXIMUM_BACKLIGHT - mScreenBrightnessDim);
            mSeekBar.setOnSeekBarChangeListener(this);

            mCheckBox = (CheckBox)dialogView.findViewById(R.id.automatic_mode);

            mSubTitle = (TextView)dialogView.findViewById(R.id.subtitle);
            mSubTitle.setText(R.string.sp_screen_timeout_sub_title_NORMAL);

            //[jongwon007.kim] NightMode CheckBox , SubTitle
            mNightCheckBox = (CheckBox)dialogView.findViewById(R.id.night_mode);
            mSubTitleNight = (TextView)dialogView.findViewById(R.id.subtitle_night);

            if (mOldBrightness > mThermalMaxBrightness) {
               mSeekBar.setProgress(mThermalMaxBrightness - mScreenBrightnessDim);
            } else {
                mSeekBar.setProgress(mOldBrightness - mScreenBrightnessDim);
            }

            if (mAutomaticAvailable) {
                mCheckBox.setOnCheckedChangeListener(this);
                mOldAutomatic = getBrightnessMode(0);
                mCheckBox.setChecked(mOldAutomatic != 0);
                if (mCheckBox.isChecked()) {
                    mSubTitle.setText(R.string.sp_brightness_auto_checked_summary_NORMAL);
                } else {
                    mSubTitle.setText(R.string.sp_brightness_auto_unchecked_summary_NORMAL);
                }
            } else {
                mCheckBox.setVisibility(View.GONE);
                mSubTitle.setVisibility(View.GONE);
			}
            mNightCheckBox.setVisibility(View.GONE);
            mSubTitleNight.setVisibility(View.GONE);

            mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true,
                    mBrightnessObserver);

            mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), true,
                    mBrightnessModeObserver);
            mRestoredOldState = false;

            builder.setTitle(getUpdatedTitle());

            return builder.create();
        }

        public static BrightnessDialog newInstance(Activity activity) {
            return new BrightnessDialog();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            final ContentResolver resolver = mContext.getContentResolver();
            resolver.unregisterContentObserver(mBrightnessObserver);
            resolver.unregisterContentObserver(mBrightnessModeObserver);

            super.onDismiss(dialog);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            final ContentResolver resolver = mContext.getContentResolver();
            resolver.unregisterContentObserver(mBrightnessObserver);
            resolver.unregisterContentObserver(mBrightnessModeObserver);

            restoreOldState();

            super.onCancel(dialog);
            getActivity().finish();
        }

        private ContentObserver mBrightnessObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                int brightness = getBrightness(MAXIMUM_BACKLIGHT);
                mSeekBar.setProgress(brightness - mScreenBrightnessDim);
                Log.d(TAG, "mBrightnessObserver() : brightness=" + brightness);
            }
        };

        private ContentObserver mBrightnessModeObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                boolean checked = getBrightnessMode(0) != 0;
                mCheckBox.setChecked(checked);
                Log.d(TAG, "mBrightnessModeObserver() : checked=" + checked);
            }
        };

        private String getUpdatedTitle() {
            int percent = (mSeekBar.getProgress() * 100)
                    / (MAXIMUM_BACKLIGHT - mScreenBrightnessDim);
            String title = mContext.getString(R.string.brightness);


                if ("ar".equals(Locale.getDefault().getLanguage())
                        || "fa".equals(Locale.getDefault().getLanguage())) {
                    title = title + " (" + String.format(Locale.getDefault(), "%d", percent) + "%)";
                } else {
                    title = title + " (" + percent + "%)";
                }
            return title;
        }

        private int getPercent() {
            return (mSeekBar.getProgress() * 100) / (MAXIMUM_BACKLIGHT - mScreenBrightnessDim);
        }

        private int getThermalMaxBrightness() {
            String flag_read_path = new String("/sys/class/leds/lcd-backlight/max_brightness");
            String frag = "";
            BufferedReader inFlagFile = null;
            int nBrightness = MAXIMUM_BACKLIGHT;

            try {
                inFlagFile = new BufferedReader(new FileReader(flag_read_path));
                frag = inFlagFile.readLine();
                Log.d(TAG, "thermal max bright -> " + frag);
                nBrightness = Integer.parseInt(frag);
                inFlagFile.close();
            } catch (Exception e) {
                Log.d(TAG, "thermal max bright read fail" + frag);
                /* return  MAXIMUM_BACKLIGHT */
            } finally {
                try {
                    if (inFlagFile != null) {
                        inFlagFile.close();
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Exception");
                    /* return  MAXIMUM_BACKLIGHT */
                } finally {
                    /* return  MAXIMUM_BACKLIGHT */
                }
            }
            return nBrightness;
        }

        private int getBrightness(int defaultValue) {
            int brightness = defaultValue;
            try {
                brightness = Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS);
            } catch (SettingNotFoundException snfe) {
                Log.w(TAG, "SettingNotFoundException");
            }
            return brightness;
        }

        private int getBrightnessMode(int defaultValue) {
            int brightnessMode = defaultValue;
            try {
                brightnessMode = Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE);
            } catch (SettingNotFoundException snfe) {
                Log.w(TAG, "SettingNotFoundException");
            }

            return brightnessMode;
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
            Log.d(TAG, "onProgressChanged###progress + mScreenBrightnessDim :" + (int)progress
                    + mScreenBrightnessDim);
            Log.d(TAG, "onProgressChanged###mThermalMaxBrightness :" + mThermalMaxBrightness);

            if (progress + mScreenBrightnessDim > mThermalMaxBrightness) {
                if (Config.VZW.equals(Config.getOperator())) {
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
                    setBrightness(progress + mScreenBrightnessDim, false);
                    //LGE_CHANGE_E [VZW Only] Brightness / Heat sensor POP_UP scenario
                } else {
                    seekBar.setProgress(mThermalMaxBrightness - mScreenBrightnessDim);
                    showToast(R.string.sp_brightness_over_max_NORMAL);
                }
            } else {
                setBrightness(progress + mScreenBrightnessDim, false);
            }

            if (getDialog() != null) {
                getDialog().setTitle(getUpdatedTitle());
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    mSeekBar.getProgress() + mScreenBrightnessDim);
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setMode(isChecked ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                    : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

            if (mCheckBox.isChecked()) {
                mSubTitle.setText(R.string.sp_brightness_auto_checked_summary_NORMAL);
            }
            else {
                mSubTitle.setText(R.string.sp_brightness_auto_unchecked_summary_NORMAL);
            }

            if (!isChecked) {
                setBrightness(mSeekBar.getProgress() + mScreenBrightnessDim, false);
            }

            if (getDialog() != null) {
                getDialog().setTitle(getUpdatedTitle());
            }
        }

        private static void restoreOldState() {
            if (mRestoredOldState) {
                return;
            }
            if (mAutomaticAvailable) {
                setMode(mOldAutomatic);
            }

            if (!mAutomaticAvailable || mOldAutomatic == 0) {
                setBrightness(mOldBrightness, true);
            } else {
                mCheckBox.setChecked(true);
                setBrightness(mOldBrightness, false);
            }

            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, mOldBrightness);
            mRestoredOldState = true;
        }

        private static void setBrightness(int brightness, boolean force) {
            final int seek_bar_range = MAXIMUM_BACKLIGHT - mScreenBrightnessDim;

            int temp = brightness;
            Log.d(TAG, "setBrightness() : Brightness=" + temp);
            try {
                IPowerManager power = IPowerManager.Stub.asInterface(
                        ServiceManager.getService("power"));
                if (power != null) {
                    if (mCheckBox.isChecked() && !force) {
                        brightness -= mScreenBrightnessDim;
                        float valf = (((float)brightness * 2) / seek_bar_range) - 1.0f; //JB native
                        power.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(valf); //JB native
                        Log.d(TAG, "setBrightness() : MultiALC=" + valf);
                    }
                    else {
                        power.setTemporaryScreenBrightnessSettingOverride(brightness);
                        Log.d(TAG, "setBrightness() : NoMultiALC=" + temp);
                    }
                }
            } catch (RemoteException doe) {
                Log.w(TAG, "RemoteException");
            }
        }

        private static void setMode(int mode) {
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                setBrightness(mSeekBar.getProgress() + mScreenBrightnessDim, false);
            } else {
                mSeekBar.setVisibility(View.VISIBLE);
            }
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
        }

        private void showToast(int aStrID) {
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
    }
}
