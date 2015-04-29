package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
//import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.lge.constants.SettingsConstants;

import java.util.List;

public class MultiTaskingSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener,
        OnCheckedChangeListener {

    private static final String TAG = "MultiTaskingSettings";
    //private static final String KEY_SLIDE_ASIDE = "slide_aside";
    private static final String KEY_SLIDE_ASIDE_IMAGE = "slide_aside_image";
    private static final String INDEX_DUAL_WINDOW_ACTION_NAME =
            "com.lge.settings.ACTION_DUAL_WINDOW";
    //private Context mContext;
    private Switch mSwitch;
    private static final String DB_TABLE_MULTITASKINGSETTINGS[] = {
            "multitasking_slide_aside",
            "auto_split_view",
            "dual_window"
    };
    private CheckBoxPreference mAppSplitViewPreferences;
    private CheckBoxPreference mSlideAsidePreferences;
    private CheckBoxPreference mDualWindowPreferences;
    private Preference mHelpPreferences;

    public static final boolean SUPPORT_APP_SPLIT_VIEW =
            SystemProperties.getBoolean("ro.lge.capp_splitwindow", false);
    SettingsBreadCrumb mBreadCrumb;
    //    private CheckBoxPreference mSlideAsidePercentage;
    private CustomImagePreference mCustomImagePreference = null;

    //private Preference mSlideAside;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Activity activity = getActivity();
        //mContext = getActivity().getApplicationContext();

        mSwitch = new Switch(activity);
        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            }
        });

        mSwitch.setOnCheckedChangeListener(this);

        final int padding = activity.getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        if (SettingsBreadCrumb.isAttached(getActivity())) {

            mSwitch.setPaddingRelative(0, 0, 0, 0);

        } else {
            mSwitch.setPaddingRelative(0, 0, padding, 0);
        }
        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getActionBar().setCustomView(
                    mSwitch,
                    new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                            ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
                                    | Gravity.END));
        }
        setHasOptionsMenu(true);
        if (SUPPORT_APP_SPLIT_VIEW) {
            getActivity().getActionBar().setTitle(R.string.sp_settings_multitasking);
        }
        initPreference();
        checkPreference();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        boolean isEnabled = (Settings.System.getInt(getContentResolver(),
                "multitasking_slide_aside", 1) > 0);
        mSwitch.setChecked(isEnabled);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPreference();
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = SettingsBreadCrumb.get(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.addSwitch(mSwitch);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            if (mBreadCrumb != null) {
                mBreadCrumb.removeSwitch();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mCustomImagePreference != null) {
            mCustomImagePreference.cleanup();
            //            mCustomImagePreference.getView();
            //            Log.d("kimyow",  "onConfigurationChanged::animation cleanup");
        }
        if (mCustomImagePreference != null) {
            ViewGroup rootView = (ViewGroup)getView();
            rootView.removeAllViews();
            rootView.addView(mCustomImagePreference.getView());
        }
        super.onConfigurationChanged(newConfig);
        Log.d("kimyow", "onConfigurationChanged");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCustomImagePreference != null) {
            mCustomImagePreference.cleanup();
        }
        final Activity activity = getActivity();
        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            View view = activity.getActionBar().getCustomView();
            if (view != null) {
                view.destroyDrawingCache();
            }
            activity.getActionBar().setCustomView(null);
        }
    }

    private void initPreference() {
        if (SUPPORT_APP_SPLIT_VIEW) {
            addPreferencesFromResource(R.xml.multitasking_settings2);

            getActivity().getActionBar().setIcon(
                    R.drawable.ic_settings_multitasking_splitview);

            mSlideAsidePreferences =
                    (CheckBoxPreference)
                    findPreference(
                    "multitasking_slide_aside");
            mDualWindowPreferences =
                    (CheckBoxPreference)
                    findPreference(
                    "multitasking_dual_window");
            mAppSplitViewPreferences =
                    (CheckBoxPreference)
                    findPreference(
                    "multitasking_app_split_view");
            mHelpPreferences = (Preference)findPreference("multitasking_help");

            ContentResolver resolver = getContentResolver();

            if (Settings.System.
                    getInt(
                            resolver,
                            DB_TABLE_MULTITASKINGSETTINGS[0],
                            0) == 1) {
                mSlideAsidePreferences.setChecked(true);
            } else {
                mSlideAsidePreferences.setChecked(false);
            }
            if (Settings.System.
                    getInt(
                            resolver,
                            DB_TABLE_MULTITASKINGSETTINGS[1],
                            0) == 1) {
                mAppSplitViewPreferences.setChecked(true);
            } else {
                mAppSplitViewPreferences.setChecked(false);
            }
            if (Settings.System.
                    getInt(
                            resolver,
                            DB_TABLE_MULTITASKINGSETTINGS[2],
                            0) == 1) {
                mDualWindowPreferences.setChecked(true);
                mAppSplitViewPreferences.setEnabled(true);
            } else {
                mDualWindowPreferences.setChecked(false);
                mAppSplitViewPreferences.setEnabled(false);
            }
        } else {
            addPreferencesFromResource(R.xml.multitasking_settings);
            mCustomImagePreference = (CustomImagePreference)findPreference(KEY_SLIDE_ASIDE_IMAGE);
            mCustomImagePreference.setSelectable(false);
            //mSlideAside = (Preference)findPreference(KEY_SLIDE_ASIDE);
        }
        //        mSlideAsidePercentage = (CheckBoxPreference)findPreference(KEY_SLIDE_ASIDE);
    }

    private void checkPreference() {
        //boolean isEnabled = (Settings.System.getInt(getContentResolver(),
        //        "multitasking_slide_aside", 1) > 0);
        //        mSlideAsidePercentage.setChecked(isEnabled);
        if (SUPPORT_APP_SPLIT_VIEW) {

            if (mSwitch != null) {
                mSwitch.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = getContentResolver();

        if (preference == mSlideAsidePreferences) {
            Settings.System.putInt(resolver, DB_TABLE_MULTITASKINGSETTINGS[0],
                    mSlideAsidePreferences.isChecked() ? 1 : 0);
            Log.d(TAG, "TREE CLICK SLIDEASIDE");
        } else if (preference == mAppSplitViewPreferences) {
            Settings.System.putInt(resolver, DB_TABLE_MULTITASKINGSETTINGS[1],
                    mAppSplitViewPreferences.isChecked() ? 1 : 0);
            Log.d(TAG, "TREE CLICK APPSPLITVIEW");
        } else if (preference == mDualWindowPreferences) {
            Settings.System.
                    putInt(
                            resolver,
                            DB_TABLE_MULTITASKINGSETTINGS[2],
                            mDualWindowPreferences.isChecked() ? 1 : 0);
            if (mDualWindowPreferences.isChecked()) {
                mAppSplitViewPreferences.setEnabled(true);
            } else {
                mAppSplitViewPreferences.setEnabled(false);
            }

            getActivity().
                    sendBroadcast(
                            new Intent(
                                    INDEX_DUAL_WINDOW_ACTION_NAME));
        } else if (preference == mHelpPreferences) {
            /*Intent i = new Intent(Intent.ACTION_VIEW); 
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setClassName("com.android.settings","com.android.settings.GestureHelp");
            startActivity(i);*/
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        return false;
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

    @Override
    public void onCheckedChanged(CompoundButton button, boolean status) {
        if (mSwitch != null) {
            mSwitch.setChecked(status);
        }
        Settings.System.putInt(getContentResolver(), "multitasking_slide_aside",
                status ? 1 : 0);
    }
}
