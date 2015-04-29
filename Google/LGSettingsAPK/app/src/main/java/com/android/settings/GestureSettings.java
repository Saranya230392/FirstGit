/*
 *
 */

package com.android.settings;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.LinearLayout;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
//[S][2011.12.30][jaeyoon.hyun] Change Config.java location
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lgesetting.Config.ConfigHelper;
//[E][2011.12.30][jaeyoon.hyun] Change Config.java location
import com.android.settings.R;
//[S][2012.02-01][jaeyoon.hyun] Add constant value of LGSensor
//import android.hardware.LGSensor;
//[E][2012.02-01][jaeyoon.hyun] Add constant value of LGSensor
import android.content.Intent;
import android.os.Build;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.ContentObserver;

public class GestureSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnCheckedChangeListener, OnSeekBarChangeListener {
    private static final String TAG = "GestureSettings";
    private static final int ORIENTATION = 3;
    //private static final int TYPE_SENSOR_LGE_GESTURE_TILT = 21;
    private static final int TYPE_SENSOR_LGE_GESTURE_TAP = 22;
    private static final int TYPE_SENSOR_LGE_GESTURE_FACING = 23;
    //private static final int TYPE_SENSOR_LGE_GESTURE_BASIC = 24;
    private static final String DB_TABLE_GESTURESETTING[] = {
            "gesture_home_rearrange",
            "gesture_voice_call",
            "gesture_alarm",
            "gesture_video_player",
            "gesture_answering",
            "gesture_fadeout_ringtone",
            "gesture_turn_onoff_speakerphone",
            "take_screenshot",
            "hide_display",
            "gesture_trun_screen_on",
            "multitasking_slide_aside",
            "gesture_app_shortcut",
            "gesture_media_volume_control"

    };

    //JW ISAI Setting
    private SwitchPreference mISAISettings;
    private PreferenceCategory mISAICategory;

    private PreferenceScreen parent;
    private PreferenceCategory mKnockOnCategory;
    private PreferenceCategory mAnswermeCategory;
    private PreferenceCategory mOthersCategory;
    private CheckBoxPreference mHomePreferences;

    private CheckBoxPreference mCallPreferences;
    private CheckBoxPreference mAlarmPreferences;
    private CheckBoxPreference mVideoPreferences;
    private CheckBoxPreference mFadeoutRingtonePreferences;
    private CheckBoxPreference mAnswertheincomingcallPreferences;
    private CheckBoxPreference mTurnoOnOffSpeakerphonePreferences;
    private CheckBoxPreference mTakeScreenshotPreferences;
    private CheckBoxPreference mHideDisplayPreferences;
    private CheckBoxPreference mTurnScreenOnPreferences;
    private CheckBoxPreference mSlideAsidePreferences;
    private CheckBoxPreference mAppShortCutPreferences;
    private CheckBoxPreference mMediaVolumeControlPreferences;
    private Preference mHelpPreferences;
    private CheckBox mCheckBox;
    //private TextView mTextViewslow;
    //private TextView mTextViewfast;
    private SeekBar mSeekBar;
    //private View mview;
    private LinearLayout mLinearLayout;
    //private boolean isLGE_VIDEO_PLYAER = false;
    //private boolean hasTAB_SENSOR = false;
    private boolean hasFacing_SENSOR = false;
    private boolean hasOrient_SENSOR = false;
    private boolean testing = false;
    //private int test_value = 0;
    //private boolean test_use_default = false;
    private static String sKNOCKON_LOCK_SET = "KNOCKON_LOCK_SET";

    public static final String ISAI_ENABLED = "isai_enabled";
    private boolean mSupportIsaISettings = false;

    private PreferenceCategory mSensorCategory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.gesture_settings);
        //getActivity().getActionBar().setIcon(R.drawable.ic_settings_gesture);
        parent = (PreferenceScreen)findPreference("gesture_settings");
        mHomePreferences = (CheckBoxPreference)findPreference("gesture_homescreen");

        mCallPreferences = (CheckBoxPreference)findPreference("gesture_calls");
        mAlarmPreferences = (CheckBoxPreference)findPreference("gesture_alarm");
        mVideoPreferences = (CheckBoxPreference)findPreference("gesture_video");
        mAnswertheincomingcallPreferences = (CheckBoxPreference)findPreference("gesture_answer_the_incoming_call_new");
        mFadeoutRingtonePreferences = (CheckBoxPreference)findPreference("gesture_fadeout_ringtone_new");
        mTurnoOnOffSpeakerphonePreferences = (CheckBoxPreference)findPreference("gesture_turn_onoff_speakerphone_new");
        mTakeScreenshotPreferences = (CheckBoxPreference)findPreference("gesture_take_screenshot");
        mHideDisplayPreferences = (CheckBoxPreference)findPreference("gesture_hide_display");
        mTurnScreenOnPreferences = (CheckBoxPreference)findPreference("gesture_turn_screen_on");
        mSlideAsidePreferences = (CheckBoxPreference)findPreference("gesture_slide_aside");
        mAppShortCutPreferences = (CheckBoxPreference)findPreference("gesture_app_shortcut");
        mMediaVolumeControlPreferences =
                (CheckBoxPreference)findPreference("gesture_media_volume_control");
        mHelpPreferences = (Preference)findPreference("gesture_help");

        mKnockOnCategory = (PreferenceCategory)findPreference("knockon_category");
        mAnswermeCategory = (PreferenceCategory)findPreference("answering_category");
        mOthersCategory = (PreferenceCategory)findPreference("other_category");

        //JW ISAI
        mISAICategory = (PreferenceCategory)findPreference("isai_category");
        mISAISettings = (SwitchPreference)findPreference("gesture_isai");
        mISAISettings.setOnPreferenceChangeListener(this);

        mSensorCategory = (PreferenceCategory)findPreference("sensor_category");

        checkMenuList();

    }

    private void removePreference(Preference preference) {
        if ((parent != null) && (preference != null)) {
            parent.removePreference(preference);
        }
    }

    private void init_UI() {

        ContentResolver resolver = getContentResolver();

        mHomePreferences.setChecked(getSettingsDBValue(resolver, DB_TABLE_GESTURESETTING[0]));
        mCallPreferences.setChecked(getSettingsDBValue(resolver, DB_TABLE_GESTURESETTING[1]));
        mAlarmPreferences.setChecked(getSettingsDBValue(resolver, DB_TABLE_GESTURESETTING[2]));
        mVideoPreferences.setChecked(getSettingsDBValue(resolver, DB_TABLE_GESTURESETTING[3]));
        mAnswertheincomingcallPreferences.setChecked(getSettingsDBValue(resolver,
                DB_TABLE_GESTURESETTING[4]));
        mFadeoutRingtonePreferences.setChecked(getSettingsDBValue(resolver,
                DB_TABLE_GESTURESETTING[5]));
        mTurnoOnOffSpeakerphonePreferences.setChecked(getSettingsDBValue(resolver,
                DB_TABLE_GESTURESETTING[6]));
        mTakeScreenshotPreferences.setChecked(getSettingsDBValue(resolver,
                DB_TABLE_GESTURESETTING[7]));
        mHideDisplayPreferences
                .setChecked(getSettingsDBValue(resolver, DB_TABLE_GESTURESETTING[8]));
        mTurnScreenOnPreferences
                .setChecked(getSettingsDBValue(resolver, DB_TABLE_GESTURESETTING[9]));
        mSlideAsidePreferences
                .setChecked(getSettingsDBValue(resolver, DB_TABLE_GESTURESETTING[10]));
        mAppShortCutPreferences
                .setChecked(getSettingsDBValue(resolver, DB_TABLE_GESTURESETTING[11]));
        mMediaVolumeControlPreferences.setChecked(getSettingsDBValue(resolver,
                DB_TABLE_GESTURESETTING[12]));
    }

    private boolean getSettingsDBValue(ContentResolver resolver, String dbName) {
        return (Settings.System
                .getInt(resolver, dbName, 0) == 1) ? true : false;
    }

    private void checkMenuList() {

        if ((Config.DCM).equals(Config.getOperator())
                || !Utils.checkPackage(getActivity(), "com.lge.videoplayer")) {
            removePreference(mVideoPreferences);

        }

        //        Log.i(TAG,
        //                "SystemProperties.getBoolean(ro.lge.capp_touch_palm_swipe, false) == "
        //                        + SystemProperties.getBoolean(
        //                                "ro.lge.capp_touch_palm_swipe", false));
        //        if (!SystemProperties.getBoolean("ro.lge.capp_touch_palm_swipe", false)) {

        removePreference(mTakeScreenshotPreferences);
        //        }

        Log.i(TAG,
                "SystemProperties.getBoolean(ro.lge.capp_touch_palm_swipe, false) == "
                        + SystemProperties.getBoolean(
                                "ro.lge.capp_touch_palm_swipe", false));

        if (!SystemProperties.getBoolean("ro.lge.capp_privatemode", false)) {

            removePreference(mHideDisplayPreferences);

        }

        PackageManager pm = getPackageManager();
        ApplicationInfo info = null;
        try {
            info = pm.getApplicationInfo("com.lge.gestureanswering",
                    PackageManager.GET_META_DATA);
            Log.d(TAG, "info.name = " + info.name);
        } catch (NameNotFoundException e) {
            removePreference(mAnswertheincomingcallPreferences);
            removePreference(mFadeoutRingtonePreferences);
        }

        removePreference(mTurnoOnOffSpeakerphonePreferences);

        /* try {
             info = pm.getApplicationInfo("com.lge.doubletap",
                     PackageManager.GET_META_DATA);

             if ("VZW".equals(Config.getOperator())) {

               removePreference(mKnockOnCategory);
             } else {

                 if ("DCM".equals(Config.getOperator())) {
                 mKnockOnCategory
                         .setTitle(
                             R.string.gesture_title_turn_screen_on);
                 mTurnScreenOnPreferences
                         .setTitle(
                             R.string.gesture_seekbar_screenonoff);
                 }
             }
         } catch (NameNotFoundException e) {

             removePreference(mTurnScreenOnPreferences);
             removePreference(mKnockOnCategory);
         }
         */
        Context mcontext = getActivity();
        //if (supportScreenOnOff(mcontext)) {
        if (ConfigHelper.isSupportScreenOnOff(mcontext)) {
            //support knock ONOFF
            if ("DCM".equals(Config.getOperator())) {
                if (mKnockOnCategory != null) {
                    mKnockOnCategory
                            .setTitle(
                            R.string.gesture_title_turn_screen_on);
                }
                if (mTurnScreenOnPreferences != null) {
                    mTurnScreenOnPreferences
                            .setTitle(
                            R.string.gesture_seekbar_screenonoff);
                }
            }
            if ("VZW".equals(Config.getOperator())) {
                if (mTurnScreenOnPreferences != null) {
                    mTurnScreenOnPreferences.setTitle(R.string.gesture_title_turn_screen_on);
                    mTurnScreenOnPreferences
                            .setSummary(R.string.gesture_summary_turn_screen_on_fixed);
                }
            }
        } else {
            //support knock OFF
            //	if (supportScreenOff(mcontext)) {
            if (ConfigHelper.isSupportScreenOff(mcontext)) {
                if (mTurnScreenOnPreferences != null) {
                    mTurnScreenOnPreferences
                            .setTitle(
                            R.string.gesture_knockon_screenoff);
                }
                if (mTurnScreenOnPreferences != null) {
                    mTurnScreenOnPreferences
                            .setSummary(
                            R.string.gesture_screen_on_off_help_summary);
                }
            } else {
                updateKnockonMenu(mcontext);
            }
        }

        if (Utils.isWifiOnly(mcontext)) {
            removePreference(mCallPreferences);
        }

        removePreference(mSlideAsidePreferences);

        if (!CheckConfigValue(getActivity(), "config_knockon_app_shortcut_available")) {
            removePreference(mAppShortCutPreferences);
        }

        if (!CheckConfigValue(getActivity(), "config_knockon_media_volume_control_available")) {
            removePreference(mMediaVolumeControlPreferences);
        }
        SensorManager sensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) == null) {
            removePreference(mHomePreferences);
        }
        //[jongwon007.kim] KK Remove Move Home screen Item
        removePreference(mHomePreferences);

        //VZW RemoveCategory
        if ("VZW".equals(Config.getOperator())) {
            removePreference(mKnockOnCategory);
            removePreference(mAnswermeCategory);
            removePreference(mOthersCategory);
        }
        mSupportIsaISettings = ConfigHelper.isSupportShakingGesture(mcontext);
        Log.d("jw", "mSupportIsaISettings :" + mSupportIsaISettings);

        if (!mSupportIsaISettings || !Utils.isUI_4_1_model(mcontext)) {
            removePreference(mISAICategory);
            removePreference(mISAISettings);

        }
        if (!"KDDI".equals(Config.getOperator())) {
            removePreference(mISAICategory);
            removePreference(mISAISettings);
        }

        if (ConfigHelper.isSupportScreenOff(mcontext)) {
            Log.d(TAG, "should show Screen Off menu"); // don't remove category.
        } else {
            updateKnockonMenu(mcontext);
        }

        Log.i(TAG, "my user " + UserHandle.myUserId());
        if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
            removePreference(mSensorCategory);
        }

        if (ConfigHelper.isRemovedFadeoutRington(mcontext)) {
            removePreference(mFadeoutRingtonePreferences);
        }
    }

    private void updateKnockonMenu(Context context) {
        if (!ConfigHelper.isSupportKnockCode2_0(context)) {
            if (Utils.isUI_4_1_model(context)) {
                if (!SystemProperties.getBoolean("settings.knockon.test", false) == true) {
                    removePreference(mTurnScreenOnPreferences);
                    removePreference(mKnockOnCategory);
                }
            }
        } else {
            Log.d(TAG, "show knockOn menu");
        }
    }

    public static boolean CheckConfigValue(Context context, String config_name) {
        Context con = null;
        final String pkgName = "com.lge.internal";
        final String res_string = config_name;
        Log.d(TAG, "res_string" + res_string);
        int id = 0;
        try {
            con = context.createPackageContext(pkgName, Context.MODE_PRIVATE);
            Resources res = null;
            if (null != con && null != con.getResources()) {
                res = con.getResources();
                id = res.getIdentifier(res_string, "bool", pkgName);
                return Boolean.parseBoolean(con.getText(id).toString());
            }
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, " NameNotFoundException : " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, " NullPointerException : " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, " resourceException : " + e.getMessage());
        }
        return false;
    }

    private void checkSensor() {
        // create new SensorManager
        SensorManager sensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
        // get all sensors of all types
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (int i = 0; i < sensorList.size(); i++) {
            Log.i(TAG, "Sensor name : " + sensorList.get(i).getName());
            Log.i(TAG, "Sensor type : " + sensorList.get(i).getType());
            if (sensorList.get(i).getType() == TYPE_SENSOR_LGE_GESTURE_TAP) {
                //hasTAB_SENSOR = true;
            } else if (sensorList.get(i).getType() == TYPE_SENSOR_LGE_GESTURE_FACING) {
                hasFacing_SENSOR = true;
            } else if (sensorList.get(i).getType() == ORIENTATION) {
                hasOrient_SENSOR = true;
            }
        }
        //Log.e("JJJ", hasTAB_SENSOR+"");
        //Log.e("JJJ", LGSensor.TYPE_SENSOR_LGE_GESTURE_FACING+"");
        //Log.e("JJJ", hasOrient_SENSOR+"");
        if (hasOrient_SENSOR) {
            hasOrient_SENSOR = false;
        } else {
            removePreference(mHomePreferences);

        }

        if (hasFacing_SENSOR) {
            hasFacing_SENSOR = false;
        } else {
            removePreference(mCallPreferences);
            removePreference(mAlarmPreferences);
            removePreference(mVideoPreferences);
        }
    }

    private ContentObserver mIsaISwitch = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            upDateISAISwitch();
        }
    };

    private ContentObserver mSilenceCalls = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (Settings.System.getInt(getContentResolver(), DB_TABLE_GESTURESETTING[1], 0) == 1) {
                mCallPreferences.setChecked(true);
            } else {
                mCallPreferences.setChecked(false);
            }
        }
    };

    private ContentObserver mStopAlarm = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (Settings.System.getInt(getContentResolver(), DB_TABLE_GESTURESETTING[2], 0) == 1) {
                mAlarmPreferences.setChecked(true);
            } else {
                mAlarmPreferences.setChecked(false);
            }
        }
    };

    private void upDateISAISwitch() {
        //ISAI Settings Switch
        if (Settings.System.getInt(getContentResolver(), ISAI_ENABLED, 0) == 1) {
            mISAISettings.setChecked(true);
        } else {
            mISAISettings.setChecked(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        init_UI();
        if (testing) {
            //openCustomDialog();
            testing = false;
        }
        //[Security Knock on Feature][jongwon007.kim] If Security turn on Gesture Knock on Change Summary

        if (mTurnScreenOnPreferences != null) {
            boolean mScurityKnock = Settings.Secure.getIntForUser(getContentResolver(),
                    sKNOCKON_LOCK_SET,
                    0, UserHandle.USER_CURRENT) > 0 ? true : false;
            Log.d("jw", "mScurityKnock : " + mScurityKnock);
            if (mScurityKnock) {
                mTurnScreenOnPreferences.setSummary(R.string.gesture_screen_knock_code_summary);
            }

        }
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(ISAI_ENABLED), true, mIsaISwitch);
        upDateISAISwitch();

        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(DB_TABLE_GESTURESETTING[1]), true, mSilenceCalls);
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(DB_TABLE_GESTURESETTING[2]), true, mStopAlarm);

    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        getContentResolver().unregisterContentObserver(mIsaISwitch);
        getContentResolver().unregisterContentObserver(mSilenceCalls);
        getContentResolver().unregisterContentObserver(mStopAlarm);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = getContentResolver();

        if (preference == mHomePreferences) {
            Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[0],
                    mHomePreferences.isChecked() ? 1 : 0);

        } else if (preference == mCallPreferences) {
            if (isTactionEnabled(resolver, mCallPreferences)
                    && mCallPreferences.isChecked() == true) {
                showTActionDialog(getString(R.string.taction_call_summary), mCallPreferences);
            } else {
                Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[1],
                        mCallPreferences.isChecked() ? 1 : 0);
            }
        } else if (preference == mAlarmPreferences) {
            if (isTactionEnabled(resolver, mAlarmPreferences)
                    && mAlarmPreferences.isChecked() == true) {
                showTActionDialog(getString(R.string.taction_alram_summary), mAlarmPreferences);
            } else {
                Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[2],
                        mAlarmPreferences.isChecked() ? 1 : 0);
            }
        } else if (preference == mVideoPreferences) {
            Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[3],
                    mVideoPreferences.isChecked() ? 1 : 0);

        } else if (preference == mHelpPreferences) {
            /*Intent i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setClassName("com.android.settings","com.android.settings.GestureHelp");
            startActivity(i);*/
        } else if (preference == mAnswertheincomingcallPreferences) {
            Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[4],
                    mAnswertheincomingcallPreferences.isChecked() ? 1 : 0);
        } else if (preference == mFadeoutRingtonePreferences) {
            Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[5],
                    mFadeoutRingtonePreferences.isChecked() ? 1 : 0);
        } else if (preference == mTurnoOnOffSpeakerphonePreferences) {
            Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[6],
                    mTurnoOnOffSpeakerphonePreferences.isChecked() ? 1 : 0);
        } else if (preference == mTakeScreenshotPreferences) {
            Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[7],
                    mTakeScreenshotPreferences.isChecked() ? 1 : 0);
        } else if (preference == mHideDisplayPreferences) {
            Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[8],
                    mHideDisplayPreferences.isChecked() ? 1 : 0);
        } else if (preference == mTurnScreenOnPreferences) {
            Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[9],
                    mTurnScreenOnPreferences.isChecked() ? 1 : 0);
        } else if (preference == mSlideAsidePreferences) {
            Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[10],
                    mSlideAsidePreferences.isChecked() ? 1 : 0);
        } else if (preference == mAppShortCutPreferences) {
            Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[11],
                    mAppShortCutPreferences.isChecked() ? 1 : 0);
        } else if (preference == mMediaVolumeControlPreferences) {
            Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[12],
                    mMediaVolumeControlPreferences.isChecked() ? 1 : 0);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private boolean isTactionEnabled(ContentResolver resolver, CheckBoxPreference gestureType) {

        String[] mVioceCall = { "motion_voicecall" };
        String[] mAlaram = { "motion_alarm" };

        if (!(Utils.checkPackage(getActivity(), "com.skt.taction")
        && Settings.System.getInt(resolver, "t_action_setting", 0) > 0)) {
            return false;
        }

        if (gestureType == mCallPreferences) {
            if (GestureMotionReceiver.getTActionDB(mVioceCall, getActivity())) {
                return true;
            } else {
                return false;
            }
        } else if (gestureType == mAlarmPreferences) {
            if (GestureMotionReceiver.getTActionDB(mAlaram, getActivity())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        // Don't remove this function
        // If remove this, onCreate() will show password popup when orientation is changed.
        super.onConfigurationChanged(newConfig);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {

        if (preference == mISAISettings) {
            boolean value = (Boolean)objValue;
            Log.d("jw", "Set Value : " + value);
            Settings.System.putInt(getContentResolver(), ISAI_ENABLED,
                    value ? 1 : 0);
        }

        return true;

    }

    /*void openCustomDialog(){
        ContentResolver resolver = getContentResolver();
         AlertDialog.Builder customDialog       = new AlertDialog.Builder(getActivity());
         customDialog.setTitle(R.string.sp_gesture_title_home_tilt_NOMAL);
         LayoutInflater layoutInflater   = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         mview=layoutInflater.inflate(R.layout.preference_dialog_gesture_tilt,null);
        mLinearLayout = (LinearLayout)mview.findViewById(R.id.tilt_text);
         mCheckBox = (CheckBox)mview.findViewById(R.id.tilt_mode);
         mCheckBox.setOnCheckedChangeListener(this);
         mSeekBar = (SeekBar)mview.findViewById(R.id.tilt_seek);
         mSeekBar.setMax(4);
         mTextViewslow = (TextView)mview.findViewById(R.id.tilt_mode_slow);
         mTextViewfast = (TextView)mview.findViewById(R.id.tilt_mode_fast);
         Typeface tf = Typeface.defaultFromStyle(Typeface.BOLD);
        mTextViewslow.setTypeface(tf);
         mTextViewfast.setTypeface(tf);
         mCheckBox.setTypeface(tf);
         if(testing){

            mCheckBox.setChecked(test_use_default);
            mSeekBar.setProgress(test_value);
            test_value=0;
        }else{

             if (Settings.System.getInt(resolver,  "gesture_tilt_default_use", 1) == 1){
                mCheckBox.setChecked(true);
                Settings.System.putInt(resolver, "gesture_tilt_value", 2);
            }else{
                mCheckBox.setChecked(false);
            }
            mSeekBar.setProgress(Settings.System.getInt(resolver, "gesture_tilt_value",2));
        }


        customDialog.setPositiveButton(R.string.sim_enter_ok, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            // TODO Auto-generated method stub
                ContentResolver resolver = getContentResolver();
                if(mCheckBox.isChecked()){
                    Settings.System.putInt(resolver,"gesture_tilt_default_use", 1);
                    Settings.System.putInt(resolver,
                                "gesture_tilt_value", 2);
                }  else {
                    Settings.System.putInt(resolver,"gesture_tilt_default_use", 0);
                    Settings.System.putInt(resolver,
                                "gesture_tilt_value", mSeekBar.getProgress());
                }

        }});
        customDialog.setNegativeButton(R.string.sim_enter_cancel, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            // TODO Auto-generated method stub

        }});
         customDialog.setNeutralButton(R.string.sp_gesture_dialog_tilt_test_NOMAL, new DialogInterface.OnClickListener(){
             @Override
             public void onClick(DialogInterface arg0, int arg1) {
             // TODO Auto-generated method stub
            testing=true;
            test_value = mSeekBar.getProgress();
            test_use_default = mCheckBox.isChecked();
             Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName("com.android.settings","com.android.settings.GestureTilt");
            if(test_use_default){
                intent.putExtra("value",2);
            }else{
                intent.putExtra("value",test_value);
            }
            //intent.putExtra("value",test_value);
            startActivity(intent);

         }});
         customDialog.setView(mview);
             customDialog.show();    }
    */
    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        // TODO Auto-generated method stub
        mCheckBox.playSoundEffect(AudioManager.FX_KEY_CLICK);
        if (mCheckBox.isChecked()) {
            mSeekBar.setVisibility(View.GONE);
            mLinearLayout.setVisibility(View.GONE);

        } else {

            mSeekBar.setVisibility(View.VISIBLE);
            mLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub

    }

    //[jongwon007.kim]T-Action Dialog

    private void showTActionDialog(CharSequence text, CheckBoxPreference prefer) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final CheckBoxPreference preference = prefer;
        //T-1 Title
        builder.setTitle(R.string.gesture_settings);

        //C-1 content
        builder.setMessage(text);

        //B-3 button
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (preference == mCallPreferences) {
                    Settings.System.putInt(getContentResolver(), DB_TABLE_GESTURESETTING[1], 1);
                    Intent intent;
                    intent = new Intent("com.skt.motions.oem_motion.SETTING_CHANGED");
                    intent.putExtra("motion_setting", "motion_mute");
                    getActivity().sendBroadcast(intent);
                } else if (preference == mAlarmPreferences) {
                    Settings.System.putInt(getContentResolver(), DB_TABLE_GESTURESETTING[2], 1);
                    Intent intent;
                    intent = new Intent("com.skt.motions.oem_motion.SETTING_CHANGED");
                    intent.putExtra("motion_setting", "motion_alarm");
                    getActivity().sendBroadcast(intent);
                }

            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (preference == mCallPreferences) {
                    mCallPreferences.setChecked(false);
                    Settings.System.putInt(getContentResolver(), DB_TABLE_GESTURESETTING[1], 0);
                } else if (preference == mAlarmPreferences) {
                    mAlarmPreferences.setChecked(false);
                    Settings.System.putInt(getContentResolver(), DB_TABLE_GESTURESETTING[2], 0);
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                if (preference == mCallPreferences) {
                    mCallPreferences.setChecked(false);
                    Settings.System.putInt(getContentResolver(), DB_TABLE_GESTURESETTING[1], 0);
                } else if (preference == mAlarmPreferences) {
                    mAlarmPreferences.setChecked(false);
                    Settings.System.putInt(getContentResolver(), DB_TABLE_GESTURESETTING[2], 0);
                }
            }
        });
        builder.show();

    }
}
