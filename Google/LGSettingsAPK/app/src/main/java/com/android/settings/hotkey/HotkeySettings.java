package com.android.settings.hotkey;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.lge.constants.SettingsConstants;

import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
//import android.widget.ImageView.ScaleType;


import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

import android.provider.Settings;

import java.util.List;

public class HotkeySettings extends SettingsPreferenceFragment {

    private static final String TAG = "HotkeySettings";
    public static final boolean DEBUG = false;

    private static final String KEY_HOTKEY_QUCIK_INFO = "quick_info";
    private static final String KEY_HOTKEY_SHORT_PRESS = "short_press";
    private static final String KEY_HOTKEY_LONG_PRESS = "long_press";
    private static final String KEY_HOTKEY_IMAGE = "hotkey_image";

    public static final String INTENT_CATEGORY_LOCKSCREEN = "com.lge.lockscreensettings.CATEGORY_LOCKSCREEN";

    // [stephan.na] For universal touch board in Accessibility. 131017
    public static final String INTENT_EXTRA_KEY_UNIVERSAL_TOUCH = "universal_touch";

    private Preference mShrotPress;
    private Preference mLongPress;
    private Preference mHotkeyImage;

    private ImageView mCustomImagePreference;

    private HotkeyInfo mHotkeyInfo;

    PackageManager mPm;
    List<ResolveInfo> mShortcuts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle(R.string.sp_hotkey_NORMAL);

        if ("KR".equals(Config.getCountry2())
                || "JP".equals(Config.getCountry2())) {
            Log.d("YSY", "HotkeySettings, Q button");
            getActivity().setTitle(R.string.sp_q_button_NORMAL);
        }

        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.isSharingBetweenSimHotkey(getActivity().getApplicationContext())) {
            actionBar.setIcon(R.drawable.shortcut_customizing_key_4th);
        }
        mPm = this.getPackageManager();
        mHotkeyInfo = new HotkeyInfo(getActivity().getBaseContext());
        initPreference();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        updateSummary();
    }

    @Override
    public void onDestroy() {
        Utils.recycleView(mCustomImagePreference);
        System.gc();
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    private void initPreference() {
        addPreferencesFromResource(R.xml.hotkey_settings);
        mHotkeyImage = findPreference(KEY_HOTKEY_IMAGE);
        mHotkeyImage.setLayoutResource(R.layout.hotkey_help);
        if (Utils.isSharingBetweenSimHotkey(getActivity().getApplicationContext())) {
            mHotkeyImage.setLayoutResource(R.layout.hotkey_help_4th_key);
        }

        // [stephan.na] For universal touch board in Accessibility. 131017
        Intent intent = getActivity().getIntent();

        if (intent != null) {
            String s = intent.getStringExtra(INTENT_EXTRA_KEY_UNIVERSAL_TOUCH);
            if (s != null && "Universal_Touch_Board".equals(s)) {

                // change quick info summary
                Preference quickInfo = findPreference(KEY_HOTKEY_QUCIK_INFO);
                int id = R.string.sp_hotkey_information_for_assceesibility_NORMAL;
                quickInfo.setSummary(getResources().getString(id));

                // change image for Universal touch board
                mHotkeyImage.setLayoutResource(R.layout.hotkey_help_for_accessibility);
            }
        }

        Preference quickInfo = findPreference(KEY_HOTKEY_QUCIK_INFO);
        if (Config.getCountry().equals("KR")
                || Config.getCountry().equals("JP")) {
            quickInfo.setSummary(getResources().getString(
                    R.string.sp_q_button_information_NORMAL));

        } else {
            quickInfo.setSummary(getResources().getString(
                    R.string.sp_hotkey_information_NORMAL));

        }

        //        setContentView(R.layout.custom_image_preference);
        //        mCustomImagePreference = (ImageView)findViewById(R.id.front_key_light_image);
        //        mCustomImagePreference.setScaleType(ScaleType.FIT_CENTER);
        //        mCustomImagePreference.setImageResource(R.drawable.setting_quiet_launch_key_image);

        mShrotPress = findPreference(KEY_HOTKEY_SHORT_PRESS);
        mLongPress = findPreference(KEY_HOTKEY_LONG_PRESS);

        if (getPreferenceScreen() != null) {
            getPreferenceScreen().removePreference(
                    getPreferenceScreen().findPreference(KEY_HOTKEY_LONG_PRESS));
        }

        updateSummary();
        updateEmptySummary();
    }
    
    private void updateEmptySummary() {

        if (mShrotPress.getSummary() == null) {
            try {
                mShrotPress
                        .setSummary(mPm.getApplicationLabel(mPm.getPackageInfo(
                                HotkeyInfo.DEFAULT_PACKAGE, 0).applicationInfo));
            } catch (NameNotFoundException e) {
                Log.w(TAG, "NameNotFoundException");
            }

            mHotkeyInfo.setDBHotKeyShortPKG(HotkeyInfo.DEFAULT_PACKAGE);
            mHotkeyInfo.setDBHotKeyShortClass(HotkeyInfo.DEFAULT_CLASS);

            mHotkeyInfo.setDBHotKeyLongPKG(HotkeyInfo.DEFAULT_PACKAGE);
            mHotkeyInfo.setDBHotKeyLongClass(HotkeyInfo.DEFAULT_CLASS);

            Log.i(TAG, "[Normal] initPreference - set default hotkey");
        }
    }


    private void updateSummary() {
        if (HotkeyInfo.HOTKEY_NONE.equals(mHotkeyInfo.getDBHotKeyShortPKG())
                && HotkeyInfo.HOTKEY_NONE.equals(mHotkeyInfo.getDBHotKeyShortClass())) {
            mShrotPress.setSummary(getResources().getString(R.string.sp_hotkey_none_NORMAL));
            Log.i(TAG, "[Normal] initPreference - ShrotPress Label : "
                    + getResources().getString(R.string.sp_hotkey_none_NORMAL));

        } else if (HotkeyInfo.DEFAULT_PACKAGE.equals(mHotkeyInfo.getDBHotKeyShortPKG())
                && HotkeyInfo.DEFAULT_CLASS.equals(mHotkeyInfo.getDBHotKeyShortClass())) {
            try {
                mShrotPress.setSummary(mPm.getApplicationLabel(mPm.getPackageInfo(
                        HotkeyInfo.DEFAULT_PACKAGE, 0).applicationInfo));
            } catch (NameNotFoundException e) {
                Log.w(TAG, "NameNotFoundException");
            }

            Log.i(TAG, "[Default] initPreference - ShrotPress Label : "
                    + getResources().getString(R.string.sp_hotkey_qmemo_NORMAL));
        } else if (false == mHotkeyInfo.isNormalPackage(mHotkeyInfo.getDBHotKeyShortPKG())
        /*&& HotkeyInfo.HOTKEY_NONE.equals(mHotkeyInfo.getDBHotKeyShortClass())*/) {

            Log.i(TAG, "[Dummay] initPreference - ShrotPress Label : "
                    + mHotkeyInfo.getDummyPKGResID(mHotkeyInfo.getDBHotKeyShortPKG()));

            mShrotPress.setSummary(mHotkeyInfo.getDummyPKGResID(mHotkeyInfo.getDBHotKeyShortPKG()));
        } else {
            final Intent intentLauncher = new Intent(Intent.ACTION_MAIN, null);
            intentLauncher.addCategory(Intent.CATEGORY_LAUNCHER);
            mShortcuts = mPm.queryIntentActivities(intentLauncher, 0);

            List<ResolveInfo> hotkeyShortcuts;
            final Intent intentHotkey = new Intent(Intent.ACTION_MAIN, null);
            intentHotkey.addCategory(INTENT_CATEGORY_LOCKSCREEN);
            hotkeyShortcuts = mPm.queryIntentActivities(intentHotkey, 0);

            mShortcuts.addAll(hotkeyShortcuts);

            for (ResolveInfo info : mShortcuts) {
                String packageName = info.activityInfo.packageName;
                String className = info.activityInfo.name;

                if (packageName.equals(mHotkeyInfo.getDBHotKeyShortPKG())
                        && className.equals(mHotkeyInfo.getDBHotKeyShortClass())) {
                    mShrotPress.setSummary(info.loadLabel(mPm));
                    Log.i(TAG, "initPreference - ShrotPress Label : " + info.loadLabel(mPm));
                }
            }

        }

        if (HotkeyInfo.HOTKEY_NONE.equals(Settings.System.getString(getContentResolver(),
                HotkeyInfo.HOTKEY_LONG_PACKAGE))
                && HotkeyInfo.HOTKEY_NONE.equals(Settings.System.getString(getContentResolver(),
                        HotkeyInfo.HOTKEY_LONG_CLASS))) {
            mLongPress.setSummary(getResources().getString(R.string.sp_hotkey_none_NORMAL));
            Log.i(TAG, "initPreference - LongPress Label : "
                    + getResources().getString(R.string.sp_hotkey_qmemo_NORMAL));
        } else if (HotkeyInfo.DEFAULT_PACKAGE.equals(Settings.System.getString(
                getContentResolver(), HotkeyInfo.HOTKEY_LONG_PACKAGE))
                && HotkeyInfo.DEFAULT_CLASS.equals(Settings.System.getString(getContentResolver(),
                        HotkeyInfo.HOTKEY_LONG_CLASS))) {
            try {
                mLongPress.setSummary(mPm.getApplicationLabel(mPm.getPackageInfo(
                        HotkeyInfo.DEFAULT_PACKAGE, 0).applicationInfo));
            } catch (NameNotFoundException e) {
                Log.w(TAG, "NameNotFoundException");
            }

            Log.i(TAG, "initPreference - LongPress Label : "
                    + getResources().getString(R.string.sp_hotkey_qmemo_NORMAL));
        } else {
            final Intent intentLauncher = new Intent(Intent.ACTION_MAIN, null);
            intentLauncher.addCategory(Intent.CATEGORY_LAUNCHER);
            mShortcuts = mPm.queryIntentActivities(intentLauncher, 0);

            List<ResolveInfo> hotkeyShortcuts;
            final Intent intentHotkey = new Intent(Intent.ACTION_MAIN, null);
            intentHotkey.addCategory(INTENT_CATEGORY_LOCKSCREEN);
            hotkeyShortcuts = mPm.queryIntentActivities(intentHotkey, 0);

            mShortcuts.addAll(hotkeyShortcuts);

            for (ResolveInfo info : mShortcuts) {
                String packageName = info.activityInfo.packageName;
                String className = info.activityInfo.name;

                if (packageName.equals(Settings.System.getString(getContentResolver(),
                        HotkeyInfo.HOTKEY_LONG_PACKAGE))
                        && className.equals(Settings.System.getString(getContentResolver(),
                                HotkeyInfo.HOTKEY_LONG_CLASS))) {
                    mLongPress.setSummary(info.loadLabel(mPm));
                    Log.i(TAG, "initPreference - LongPress Label : " + info.loadLabel(mPm));
                }
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            int settingStyle = Settings.System.getInt(getContentResolver(),
                    SettingsConstants.System.SETTING_STYLE, 0);
            int resultOption = Utils.getUsingSettings(settingStyle, getActivity());
            if (resultOption == Utils.sUSING_EASYSETTINGS) {
                getActivity().onBackPressed();
                return true;
            } else if (resultOption == Utils.sUSING_SETTINGS) {
                finish();
                return true;
            }
            break;
        default:
            break;
        }

        return super.onOptionsItemSelected(item);
    }
}
