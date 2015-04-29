/*
 *
 */

package com.android.settings.lge;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
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
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.LinearLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
//[S][2011.12.30][jaeyoon.hyun] Change Config.java location
import com.android.settings.lgesetting.Config.Config;
//[E][2011.12.30][jaeyoon.hyun] Change Config.java location
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
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

public class ISAIMotionSettings extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener, OnCheckedChangeListener, OnSeekBarChangeListener {
    private static final String TAG = "ISAIMotionSettings";

    public static final String ISAI_INFORMATION = "isai_motion_information";
    public static final String ISAI_HOME_ARRANGE = "isai_motion_home";
    public static final String ISAI_FUN = "isai_motion_fun";

    private CheckBoxPreference mInformation;
    private CheckBoxPreference mHomeArragne;
    private CheckBoxPreference mFun;

    //[jongwon007.kim] ISAI Check Current HomeLauncher
    private final String mANDROID_INTENT_ACTION_MAIN = "android.intent.action.MAIN";
    private final String mANDROID_INTENT_CATEGORY_HOME = "android.intent.category.HOME";
    private final String mLGHOME2_PACKAGE_NAME = "com.lge.launcher2";
    private static final String mEASY_HOME_PACKAGE_NAME = "com.lge.easyhome";
    private boolean mSelectedLGHome;
    private boolean mIsEasyHome;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.sp_gesture_isai_motion);

        }

        addPreferencesFromResource(R.xml.isai_motion_settings);

        mInformation = (CheckBoxPreference)findPreference("information");
        mHomeArragne = (CheckBoxPreference)findPreference("home_arrage");
        mFun = (CheckBoxPreference)findPreference("fun");

        init_UI();

    }

    //Actionbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init_UI() {

        ContentResolver resolver = getContentResolver();

        if (Settings.System.getInt(resolver, ISAI_INFORMATION, 1) == 1) {
            mInformation.setChecked(true);
        } else {
            mInformation.setChecked(false);
        }

        if (Settings.System.getInt(resolver, ISAI_HOME_ARRANGE, 1) == 1) {
            mHomeArragne.setChecked(true);
        } else {
            mHomeArragne.setChecked(false);
        }

        if (Settings.System.getInt(resolver, ISAI_FUN, 1) == 1) {
            mFun.setChecked(true);
        } else {
            mFun.setChecked(false);
        }
    }

    private void checkMenuList() {
        mHomeArragne.setEnabled(IsLGLauncher());
    }

    @Override
    public void onResume() {
        super.onResume();
        checkMenuList();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = getContentResolver();

        if (preference == mInformation) {
            Settings.System.putInt(resolver, ISAI_INFORMATION,
                    mInformation.isChecked() ? 1 : 0);

        } else if (preference == mHomeArragne) {
            Settings.System.putInt(resolver, ISAI_HOME_ARRANGE,
                    mHomeArragne.isChecked() ? 1 : 0);

        } else if (preference == mFun) {
            Settings.System.putInt(resolver, ISAI_FUN,
                    mFun.isChecked() ? 1 : 0);

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

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        // TODO Auto-generated method stub

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

    private boolean IsLGLauncher() {
        final PackageManager pkgMngr = getPackageManager();

        if (pkgMngr == null) {
            Log.w(TAG, "mPkgMngr is null");
            return false;
        }

        final Intent intent = new Intent();
        intent.setAction(mANDROID_INTENT_ACTION_MAIN);
        intent.addCategory(mANDROID_INTENT_CATEGORY_HOME);

        final List<ResolveInfo> rList = pkgMngr.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY
                        | PackageManager.GET_RESOLVED_FILTER);

        final List<ComponentName> prefActList = new ArrayList<ComponentName>();
        final List<IntentFilter> intentList = new ArrayList<IntentFilter>();

        int listNum = 0;
        if ((rList != null) && ((listNum = rList.size()) > 0)) {
            for (int i = 0; i < listNum; i++) {
                final ResolveInfo ri = rList.get(i);
                if (ri != null) {
                    pkgMngr.getPreferredActivities(intentList, prefActList,
                            ri.activityInfo.applicationInfo.packageName);
                    if (prefActList != null && prefActList.size() > 0) {
                        mSelectedLGHome = (ri.activityInfo.applicationInfo.packageName
                                .equals(mLGHOME2_PACKAGE_NAME)) ? true
                                : false;

                        mIsEasyHome = ri.activityInfo.applicationInfo.packageName
                                .equals(mEASY_HOME_PACKAGE_NAME);
                    }
                }
            }
        }

        if (mSelectedLGHome || mIsEasyHome) {
            return true;
        } else {
            return false;
        }

    }

}
