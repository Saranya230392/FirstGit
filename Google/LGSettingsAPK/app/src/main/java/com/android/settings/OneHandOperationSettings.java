/*
 *
 */

package com.android.settings;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.LinearLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.view.KeyEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
//[S][2011.12.30][jaeyoon.hyun] Change Config.java location
import com.android.settings.lgesetting.Config.Config;
//[E][2011.12.30][jaeyoon.hyun] Change Config.java location
import com.android.settings.lgesetting.Config.ConfigHelper;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
//[S][2012.02-01][jaeyoon.hyun] Add constant value of LGSensor
//import android.hardware.LGSensor;
//[E][2012.02-01][jaeyoon.hyun] Add constant value of LGSensor
import android.content.Intent;
import android.os.Build;
import android.os.SystemProperties;
import android.media.AudioManager;
import com.lge.constants.SettingsConstants;

public class OneHandOperationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnCheckedChangeListener, Indexable {
    private static final String TAG = "OneHandOperationSettings";

    public static final String ONE_HAND_DIAL_KEYPAD = "one_hand_dial_keypad";
    public static final String ONE_HAND_CALL_SCREEN = "one_hand_call_screen";
    public static final String ONE_HAND_LG_KEYBOARD = "one_hand_lg_keyboard";
    public static final String ONE_HAND_KEYBOARD_GESTURE = "one_hand_keyboard_gesture";
    public static final String ONE_HAND_LOCK_SCREEN = "one_hand_lock_screen";
    public static final String ONE_HAND_FRONT_TOUCH_BUTTON = "one_hand_front_touch_button";
    public static final String ONE_HAND_NAVIGATION_BUTTON = "one_hand_navigation_button";
    public static final String ONE_HAND_PULL_DOWN_SCREEN = "one_hand_pull_down_screen";
    public static final String ONE_HAND_MINI_VIEW = "one_hand_mini_view";
    public static final String ACCESSIBILITY_ASSISTIVE_TOUCH_SERVICE_ENABLE = "accessibility_assistive_touch_service_enable";
    public static final String ACTION_FLOATING_TOUCH_BUTTON = "com.lge.navibtn";
    public static final String PHONE_MODE_SET = "phone_mode_set";

    private PreferenceScreen parent;
    private CheckBoxPreference mDialKeypadPreferences;
    private CheckBoxPreference mCallScreenPreferences;
    private CheckBoxPreference mLGKeyboardPreferences;
    private CheckBoxPreference mKeyboardGesturePreferences;
    private CheckBoxPreference mFrontTouchButtonPreferences;
    private CheckBoxPreference mLockScreenPreferences;
    private CheckBoxPreference mFloatingFrontTouchButtonPreferences;
    private CheckBoxPreference mPullDownScreenPreferences;
    private CheckBoxPreference mMiniviewPreferences;
    //private Preference mHelpPreferences;
    private int DISABLED = 0;
    private Intent intentForFloatingActionName = new Intent(ACTION_FLOATING_TOUCH_BUTTON);

    // [2014-01-07][seungyeop.yeom] Variable to make sure that it supports the Dual Window
    public static final boolean SUPPORT_APP_SPLIT_VIEW =
            SystemProperties.getBoolean("ro.lge.capp_splitwindow", false);

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.one_hand_settings);
        //getActivity().getActionBar().setIcon(R.drawable.ic_settings_gesture);
        parent = (PreferenceScreen)findPreference("one_handed_operation");
        mDialKeypadPreferences = (CheckBoxPreference)findPreference("one_hand_dial_keypad");
        mCallScreenPreferences = (CheckBoxPreference)findPreference("one_hand_call_screen");
        mLGKeyboardPreferences = (CheckBoxPreference)findPreference("one_hand_lg_keyboard");
        mKeyboardGesturePreferences = (CheckBoxPreference)findPreference("one_hand_keyboard_gesture");
        mFrontTouchButtonPreferences = (CheckBoxPreference)findPreference("one_hand_front_touch_button");
        mLockScreenPreferences = (CheckBoxPreference)findPreference("one_hand_lockscreen");
        mFloatingFrontTouchButtonPreferences = (CheckBoxPreference)findPreference("one_hand_floating_front_touch_button");
        mPullDownScreenPreferences = (CheckBoxPreference)findPreference("one_hand_pull_down_screen");
        mMiniviewPreferences = (CheckBoxPreference)findPreference("one_hand_mini_view");
        //mHelpPreferences = (Preference)findPreference("one_hand_help");

        mIsFirst = true;
    }

    private void removePreference(Preference preference) {
        if ((parent != null) && (preference != null)) {
            parent.removePreference(preference);
        }
    }

    private void init_UI() {
        if (Settings.System.getInt(getContentResolver(), ONE_HAND_DIAL_KEYPAD, 0) == 1) {
            mDialKeypadPreferences.setChecked(true);

            Log.i(TAG, "Dial Keypad checked");
        } else {
            mDialKeypadPreferences.setChecked(false);
            Log.i(TAG, "Dial Keypad unchecked");
        }

        // [seungyeop.yeom][2013-01-07] Additional Requirements of Phone mode 2.0 ------- [START]
        if (Utils.checkPackage(getActivity(), "com.skt.prod.phone")
                && Utils.checkPackage(getActivity(), "com.skt.prod.dialer")) {
            if (Settings.System.getInt(getContentResolver(), PHONE_MODE_SET, 0) == 1) {
                mDialKeypadPreferences.setEnabled(false);
                Log.i("YSY", "Phone 2.0 : SKT Phone mode");
            } else {
                mDialKeypadPreferences.setEnabled(true);
                Log.i("YSY", "Phone 2.0 : LG Phone mode");
            }
        }
        // [seungyeop.yeom][2013-01-07] Additional Requirements of Phone mode 2.0 ------- [END]

        if (Settings.System.getInt(getContentResolver(), ONE_HAND_CALL_SCREEN, 0) == 1) {
            mCallScreenPreferences.setChecked(true);
        } else {
            mCallScreenPreferences.setChecked(false);
            Log.i(TAG, "Call screen button unchecked");
        }

        if (Settings.System.getInt(getContentResolver(), ONE_HAND_FRONT_TOUCH_BUTTON, 0) == 1) {
            mFrontTouchButtonPreferences.setChecked(true);
        } else {
            mFrontTouchButtonPreferences.setChecked(false);
            Log.i(TAG, "Front touch button unchecked");
        }

        if (Settings.System.getInt(getContentResolver(), ONE_HAND_LG_KEYBOARD, 0) == 1) {
            mLGKeyboardPreferences.setChecked(true);
            Log.i(TAG, "LG Keyboard checked");
        } else {
            mLGKeyboardPreferences.setChecked(false);
            Log.i(TAG, "LG Keyboard unchecked");
        }

        if (Settings.System.getInt(getContentResolver(), ONE_HAND_KEYBOARD_GESTURE, 0) == 1) {
            mKeyboardGesturePreferences.setChecked(true);
            Log.i(TAG, "Keyboard Gesture checked");
        } else {
            mKeyboardGesturePreferences.setChecked(false);
            Log.i(TAG, "Keyboard Gesture unchecked");
        }

        if (Settings.System.getInt(getContentResolver(), ONE_HAND_LOCK_SCREEN, 0) == 1) {
            mLockScreenPreferences.setChecked(true);
            Log.i(TAG, "Lockscreen checked");
        } else {
            mLockScreenPreferences.setChecked(false);
            Log.i(TAG, "Lockscreen unchecked");
        }
        if (Settings.System.getInt(getContentResolver(), ONE_HAND_NAVIGATION_BUTTON, 0) == 1) {
            mFloatingFrontTouchButtonPreferences.setChecked(true);
            getActivity().startService(intentForFloatingActionName);
            Log.i(TAG, "Floating Buttons checked");
        } else {
            mFloatingFrontTouchButtonPreferences.setChecked(false);
            //    getActivity().stopService(intentForFloatingActionName);
            Log.i(TAG, "Floating Buttons unchecked");
        }

        if (Settings.System.getInt(getContentResolver(), ONE_HAND_PULL_DOWN_SCREEN, 0) == 1) {
            mPullDownScreenPreferences.setChecked(true);

        } else {
            mPullDownScreenPreferences.setChecked(false);
        }
        if (Settings.System.getInt(getContentResolver(), ONE_HAND_MINI_VIEW, 1) == 1) {
            mMiniviewPreferences.setChecked(true);

        } else {
            mMiniviewPreferences.setChecked(false);
        }
        // [START][seungyeop.yeom][2014-04-30] modify mini view state for accessibility
        if (Settings.Secure.getInt(getContentResolver(),
                SettingsConstants.Secure.ACCESSIBILITY_TOUCH_CONTROL_AREAS_ENABLE, 1) == 1) {
            mMiniviewPreferences.setEnabled(false);
        } else {
            mMiniviewPreferences.setEnabled(true);
        }
        // [END]
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        init_UI();
        checkMenuList();

        if (parent.findPreference("one_hand_mini_view") != null) {
            if (Settings.Secure.getInt(getContentResolver(),
                    SettingsConstants.Secure.ACCESSIBILITY_TOUCH_CONTROL_AREAS_ENABLE, 1) == 1) {
                Toast.makeText(getActivity(), R.string.turn_off_touch_control_area, Toast.LENGTH_SHORT).show();
            }
        }

        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent()
                .getBooleanExtra("perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            boolean newValue = getActivity().getIntent().getBooleanExtra("newValue", false);
            startResult(newValue);
            mIsFirst = false;
        }
    }

    private void goMainScreen() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setClassName("com.android.settings",
                "com.android.settings.Settings$OneHandOperationSettingsActivity");
        startActivity(i);
        getActivity().finish();
    }

    private void startResult(boolean newValue) {
        if (Utils.supportSplitView(getActivity())) {
            goMainScreen();
            return;
        }

        goMainScreen();
    }

    private void checkMenuList() {
        // TODO Auto-generated method stub

        PackageManager pm = getPackageManager();
        ApplicationInfo info = null;

        try {
            info = pm.getApplicationInfo("com.lge.navibtn",
                    PackageManager.GET_META_DATA);

            if (Utils.supportNavigationButtons()) {
                //hardkeyless model
                mFloatingFrontTouchButtonPreferences
                        .setTitle(R.string.sp_one_hand_navigation_button_title_NORMAL);

                if (Settings.Secure.getInt(getContentResolver(),
                        ACCESSIBILITY_ASSISTIVE_TOUCH_SERVICE_ENABLE, 0) == DISABLED) {

                    mFloatingFrontTouchButtonPreferences.setEnabled(true);

                } else {

                    mFloatingFrontTouchButtonPreferences
                            .setSummary(R.string.one_hand_floating_front_touch_buttons_summary_disabled);
                    mFloatingFrontTouchButtonPreferences.setEnabled(false);
                }

            } else {
                //hardkey model
                mFloatingFrontTouchButtonPreferences
                        .setTitle(R.string.one_hand_floating_front_touch_buttons_title_hardkey);

                if (Settings.Secure.getInt(getContentResolver(),
                        ACCESSIBILITY_ASSISTIVE_TOUCH_SERVICE_ENABLE, 0) == DISABLED) {

                    mFloatingFrontTouchButtonPreferences.setEnabled(true);

                } else {
                    mFloatingFrontTouchButtonPreferences
                            .setSummary(R.string.one_hand_floating_front_touch_buttons_summary_hardkey_disabled);
                    mFloatingFrontTouchButtonPreferences.setEnabled(false);
                }
            }
        } catch (NameNotFoundException e) {

            removePreference(mFloatingFrontTouchButtonPreferences);
        }

        removePreference(mCallScreenPreferences);
        removePreference(mFrontTouchButtonPreferences);
        removePreference(mPullDownScreenPreferences);
        removePreference(mKeyboardGesturePreferences);

        // [START][seungyeop.yeom][2014-03-31] delete category of LG Keyboard for JP country
        if ("JP".equals(Config.getCountry())) {
            removePreference(mLGKeyboardPreferences);
        }
        // [END]

        try {
            info = pm.getApplicationInfo("com.lge.onehandcontroller",
                    PackageManager.GET_META_DATA);
            Log.i(TAG, "info = " + info.name);

            if (ConfigHelper.isSupportSlideCover(getActivity())) {
                Log.i(TAG, "support config_using_slide_cover : miniview removed.");
                removePreference(mMiniviewPreferences);
            }
        } catch (NameNotFoundException e) {
            removePreference(mMiniviewPreferences);
            Log.i(TAG, "MINI VIEW APP NOT FOUND");
        }

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mDialKeypadPreferences) {
            Settings.System.putInt(getContentResolver(), ONE_HAND_DIAL_KEYPAD,
                    mDialKeypadPreferences.isChecked() ? 1 : 0);
            Log.i(TAG, "Dial Keypad Click");
        } else if (preference == mCallScreenPreferences) {
            Settings.System.putInt(getContentResolver(), ONE_HAND_CALL_SCREEN,
                    mCallScreenPreferences.isChecked() ? 1 : 0);
            Log.i(TAG, "Call Screen Click");
        } else if (preference == mLGKeyboardPreferences) {
            Settings.System.putInt(getContentResolver(), ONE_HAND_LG_KEYBOARD,
                    mLGKeyboardPreferences.isChecked() ? 1 : 0);
            Settings.System.putInt(getContentResolver(), ONE_HAND_KEYBOARD_GESTURE,
                    mLGKeyboardPreferences.isChecked() ? 1 : 0);
            Log.i(TAG, "LG Keyboard Click");
        } else if (preference == mFrontTouchButtonPreferences) {
            Settings.System.putInt(getContentResolver(), ONE_HAND_FRONT_TOUCH_BUTTON,
                    mFrontTouchButtonPreferences.isChecked() ? 1 : 0);
            Log.i(TAG, "Front touch button Click");
        } else if (preference == mLockScreenPreferences) {
            Settings.System.putInt(getContentResolver(), ONE_HAND_LOCK_SCREEN,
                    mLockScreenPreferences.isChecked() ? 1 : 0);
            Log.i(TAG, "Lockscreen Click");
        } else if (preference == mKeyboardGesturePreferences) {
            Settings.System.putInt(getContentResolver(), ONE_HAND_KEYBOARD_GESTURE,
                    mKeyboardGesturePreferences.isChecked() ? 1 : 0);
            Log.i(TAG, "Keyboard Gesture Click");
        } else if (preference == mFloatingFrontTouchButtonPreferences) {
            Settings.System.putInt(getContentResolver(), ONE_HAND_NAVIGATION_BUTTON,
                    mFloatingFrontTouchButtonPreferences.isChecked() ? 1 : 0);
            if (mFloatingFrontTouchButtonPreferences.isChecked()) {
                getActivity().startService(intentForFloatingActionName);
                Log.i(TAG, "FLOATING TOUCH Click");
            } else {
                //getActivity().stopService(intentForFloatingActionName);
            }
        } else if (preference == mPullDownScreenPreferences) {
            Settings.System.putInt(getContentResolver(), ONE_HAND_PULL_DOWN_SCREEN,
                    mPullDownScreenPreferences.isChecked() ? 1 : 0);
        } else if (preference == mMiniviewPreferences) {
            if (Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED, 0) == 1) {
                disable_by_touch_zoom_dialog();
            }
            Settings.System.putInt(getContentResolver(), ONE_HAND_MINI_VIEW,
                    mMiniviewPreferences.isChecked() ? 1 : 0);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        // TODO Auto-generated method stub

    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == android.R.id.home) {
            int settingStyle = Settings.System.getInt(getContentResolver(),
                    SettingsConstants.System.SETTING_STYLE, 0);
            if (settingStyle == 1 && Utils.supportEasySettings(getActivity())) {
                ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningTaskInfo> info = am.getRunningTasks(1);
                ComponentName topActivity = info.get(0).topActivity;
                String topActivityClassName = topActivity.getClassName();
                String baseActivityClassName = info.get(0).baseActivity.getClassName();
                Log.d("kimyow", "top=" + topActivityClassName + "  base=" + baseActivityClassName);
                if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                    getActivity().onBackPressed();
                    return true;
                } else {
                    Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                    settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                    Log.d("kimyow", "tabIndex=" + tabIndex);
                    settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                    startActivity(settings);
                    finish();
                    return true;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public String getEasyTabIndex(String topActivity) {
        if ("com.android.settings.Settings$ManageApplicationsActivity".equals(topActivity)) {
            return "sound";
        } else if ("com.android.settings.Settings$ManageApplicationsActivity".equals(topActivity) ||
                "com.android.settings.Settings$LocationSettingsActivity".equals(topActivity) ||
                "com.android.settings.Settings$AccountSyncSettingsActivity".equals(topActivity)) {
            return "general";
        }
        return null;
    }

    public static boolean CheckOHOConfigValue(Context context, String config_name) {
        Context con = null;
        final String pkgName = "com.lge.systemui";
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

    private void disable_by_touch_zoom_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.one_hand_mini_view_title);
        builder.setMessage(R.string.mini_view_popup);
        //builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setCancelable(true);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface arg0) {
                // TODO Auto-generated method stub

            }
        });
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Settings.Secure.putInt(getContentResolver(),
                                Settings.Secure.ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED, 0);
                        Settings.System.putInt(getContentResolver(), ONE_HAND_MINI_VIEW,
                                1);
                        mMiniviewPreferences.setChecked(true);

                    }
                });
        builder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Settings.System.putInt(getContentResolver(), ONE_HAND_MINI_VIEW,
                                0);
                        mMiniviewPreferences.setChecked(false);
                    }
                });

        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                    case KeyEvent.KEYCODE_BACK:
                        Settings.System.putInt(getContentResolver(), ONE_HAND_MINI_VIEW,
                                0);
                        mMiniviewPreferences.setChecked(false);
                        break;
                    default:
                        break;
                    }
                }
                return false;
            }
        });
        builder.show();
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }

            int visible = 0; // Utils.isTablet() == true ? 0 : 1;
            setSearchIndexData(context, "one_hand_settings",
                    context.getString(R.string.one_handed_operation), "main",
                    null, null,
                    "android.intent.action.MAIN",
                    "com.android.settings.Settings$OneHandOperationSettingsActivity",
                    "com.android.settings", 1,
                    null, null, null, visible, 0);

            return mResult;
        }
    };

    public static void setSearchIndexData(Context context, String key,
            String title, String screenTtile, String summaryOn,
            String summaryOff, String intentAction, String intentTargetClass,
            String intentTargetPackage, int currentEnable,
            String preferenceType, String settingsDBTableName,
            String settingsDBField, int visible, int checkValue) {
        SearchIndexableRaw data = new SearchIndexableRaw(context);
        data.key = key;
        data.title = title;
        data.screenTitle = screenTtile;
        data.summaryOn = summaryOn;
        data.summaryOff = summaryOff;
        data.intentAction = intentAction;
        data.intentTargetClass = intentTargetClass;
        data.intentTargetPackage = intentTargetPackage;
        data.currentEnable = currentEnable;
        data.preferenceType = preferenceType;
        data.settingsDBTableName = settingsDBTableName;
        data.settingsDBField = settingsDBField;
        data.visible = visible;
        data.checkValue = checkValue;
        mResult.add(data);
    }
}
