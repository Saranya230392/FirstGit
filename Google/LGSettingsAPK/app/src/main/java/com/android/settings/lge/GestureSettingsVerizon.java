/*
 * 
 */

package com.android.settings.lge;

//import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
//import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
//import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
//import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
//import android.widget.TextView;

import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lgesetting.Config.ConfigHelper;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.List;

public class GestureSettingsVerizon extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnCheckedChangeListener, OnSeekBarChangeListener {
    private static final String TAG = "GestureSettings";
    private static final int ORIENTATION = 3;
    //private static final int TYPE_SENSOR_LGE_GESTURE_TILT = 21;
    //private static final int TYPE_SENSOR_LGE_GESTURE_TAP = 22;
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
            "multitasking_slide_aside"

    };

    private PreferenceScreen parent;
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
    private Preference mHelpPreferences;
    private CheckBox mCheckBox;
    //private TextView mTextViewslow;
    //private TextView mTextViewfast;
    private SeekBar mSeekBar;
    //private View mview;
    private LinearLayout mLinearLayout;
    //private boolean hasTAB_SENSOR = false;
    private boolean hasFacing_SENSOR = false;
    private boolean hasOrient_SENSOR = false;
    private boolean testing = false;

    //private int test_value = 0;
    //private boolean test_use_default = false;

    private PreferenceCategory mSensorCategory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.gesture_settings_verizion);
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
        mHelpPreferences = (Preference)findPreference("gesture_help");

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

        mHomePreferences.setChecked((Settings.System.getInt(resolver,
                DB_TABLE_GESTURESETTING[0], 0) == 1) ? true : false);
        mCallPreferences.setChecked((Settings.System.getInt(resolver,
                DB_TABLE_GESTURESETTING[1], 0) == 1) ? true : false);
        mAlarmPreferences.setChecked((Settings.System.getInt(resolver,
                DB_TABLE_GESTURESETTING[2], 0) == 1) ? true : false);
        mVideoPreferences.setChecked((Settings.System.getInt(resolver,
                DB_TABLE_GESTURESETTING[3], 0) == 1) ? true : false);
        mAnswertheincomingcallPreferences.setChecked((Settings.System.getInt(resolver,
                DB_TABLE_GESTURESETTING[4], 0) == 1) ? true : false);
        mFadeoutRingtonePreferences.setChecked((Settings.System.getInt(resolver,
                DB_TABLE_GESTURESETTING[5], 0) == 1) ? true : false);
        mTurnoOnOffSpeakerphonePreferences.setChecked((Settings.System.getInt(resolver,
                DB_TABLE_GESTURESETTING[6], 0) == 1) ? true : false);
        mTakeScreenshotPreferences.setChecked((Settings.System.getInt(resolver,
                DB_TABLE_GESTURESETTING[7], 0) == 1) ? true : false);
        mHideDisplayPreferences.setChecked((Settings.System.getInt(resolver,
                DB_TABLE_GESTURESETTING[8], 0) == 1) ? true : false);
        mTurnScreenOnPreferences.setChecked((Settings.System.getInt(resolver,
                DB_TABLE_GESTURESETTING[9], 0) == 1) ? true : false);
        mSlideAsidePreferences.setChecked((Settings.System.getInt(resolver,
                DB_TABLE_GESTURESETTING[10], 0) == 1) ? true : false);
    }

    private void checkMenuList() {
        if ((Config.DCM).equals(Config.getOperator())) {
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
            Log.d(TAG, "info.nave = " + info.name);
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
        } else {
            //support knock OFF
            if (ConfigHelper.isSupportScreenOff(mcontext)) {
            } else {
                removePreference(mTurnScreenOnPreferences);
            }
        }

        if (Utils.isWifiOnly(mcontext)) {
            removePreference(mCallPreferences);
        }

        SensorManager sensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) == null) {
            removePreference(mHomePreferences);
        }
        //[jongwon007.kim] KK Remove Move Home screen Item
        removePreference(mHomePreferences);

        if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
            removePreference(mSensorCategory);
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
            /* if (sensorList.get(i).getType() == TYPE_SENSOR_LGE_GESTURE_TAP) {
                hasTAB_SENSOR = true;
            } else */
            if (sensorList.get(i).getType() == TYPE_SENSOR_LGE_GESTURE_FACING) {
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

    @Override
    public void onResume() {
        super.onResume();
        init_UI();
        if (testing) {
            //openCustomDialog();
            testing = false;
        }

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = getContentResolver();

        if (preference == mHomePreferences) {
            Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[0],
                    mHomePreferences.isChecked() ? 1 : 0);

        } else if (preference == mCallPreferences) {
            Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[1],
                    mCallPreferences.isChecked() ? 1 : 0);

        } else if (preference == mAlarmPreferences) {
            Settings.System.putInt(resolver, DB_TABLE_GESTURESETTING[2],
                    mAlarmPreferences.isChecked() ? 1 : 0);

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
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        // Don't remove this function
        // If remove this, onCreate() will show password popup when orientation is changed.
        super.onConfigurationChanged(newConfig);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {

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

}
