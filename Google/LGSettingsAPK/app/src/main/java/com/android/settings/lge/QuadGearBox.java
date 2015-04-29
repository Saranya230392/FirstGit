package com.android.settings.lge;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.settings.CustomImagePreference2;
import com.android.settings.R;
import com.android.settings.Utils;
import com.lge.constants.SettingsConstants;

import java.util.List;

public class QuadGearBox extends PreferenceActivity implements OnPreferenceChangeListener {

    private static final String KEY_QUAD_GEARBOX = "checkbox_quad_gearbox";
    private static final String KEY_QUAD_GEARBOX_IMAGE = "quad_gearbox_image";
    //private static final String ACTION_ECOMODE_CHANGED = "com.android.settings.ecomode.CHANGED"; // sent from SystemUI requested by chohee.park
    public static final int ECO_MODE = 2;
    public static final int QUAD_MODE = 4;

    private CheckBoxPreference mQuadGearboxPreference;
    private CustomImagePreference2 mCustomImagePreference;

    private Handler handler = new Handler();
    private ContentObserver mEcoModeObserver = new ContentObserver(handler) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            int current_mode = Settings.System.getInt(getContentResolver(),
                    SettingsConstants.System.ECO_MODE, 0);
            if (mQuadGearboxPreference != null) {
                if (mQuadGearboxPreference.isChecked() != (current_mode > 0)) {
                    mQuadGearboxPreference.setChecked(current_mode > 0);
                    updateImage(mQuadGearboxPreference.isChecked());
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        ContentResolver resolver = getContentResolver();
        addPreferencesFromResource(R.xml.quad_gearbox);
        mCustomImagePreference = (CustomImagePreference2)findPreference(KEY_QUAD_GEARBOX_IMAGE);
        mCustomImagePreference.setSelectable(false);

        mQuadGearboxPreference = (CheckBoxPreference)findPreference(KEY_QUAD_GEARBOX);
        mQuadGearboxPreference.setChecked(Settings.System.getInt(resolver,
                SettingsConstants.System.ECO_MODE, 0) > 0);
        mQuadGearboxPreference.setOnPreferenceChangeListener(this);
        updateImage(mQuadGearboxPreference.isChecked());

        Uri uri = Settings.System.getUriFor(SettingsConstants.System.ECO_MODE);
        resolver.registerContentObserver(uri, true, mEcoModeObserver);

        getActionBar().setIcon(R.drawable.shortcut_quadcore);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mEcoModeObserver);
    }

    private void updateImage(boolean on) {
        if (on == true) {
            mCustomImagePreference.setImage(R.drawable.ic_settings_quadcore_eco_on);
        } else {
            mCustomImagePreference.setImage(R.drawable.ic_settings_quadcore_eco_off);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object arg1) {

        if (preference == mQuadGearboxPreference)
        {
            Boolean newValue = (Boolean)arg1;
            updateImage(newValue.booleanValue());
            Settings.System.putInt(getContentResolver(), SettingsConstants.System.ECO_MODE,
                    newValue ? 1 : 0);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            int settingStyle = Settings.System.getInt(getContentResolver(),
                    SettingsConstants.System.SETTING_STYLE, 0);
            if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningTaskInfo> info = am.getRunningTasks(1);
                // yonguk.kim WBT Issue fixed
                if (info == null) {
                    return super.onOptionsItemSelected(item);
                }

                ComponentName topActivity = info.get(0).topActivity;
                String topActivityClassName = topActivity.getClassName();
                String baseActivityClassName = info.get(0).baseActivity.getClassName();
                Log.d("kimyow", "top=" + topActivityClassName + "  base=" + baseActivityClassName);
                if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                    onBackPressed();
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
}
