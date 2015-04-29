package com.android.settings.deviceinfo;

import com.android.settings.R;
import com.lge.constants.SettingsConstants;
import com.lge.constants.LGIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;
import android.view.Surface;

// at HiddenMenu
public class AutorunSwitch extends PreferenceActivity {

    private final static String KEY_AUTORUN_ENABLE = "autorun_enable";

    private CheckBoxPreference mAutorunEnable;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.autorun_switch);

        mAutorunEnable = (CheckBoxPreference)findPreference(KEY_AUTORUN_ENABLE);
        mAutorunEnable.setPersistent(false);

        mAutorunEnable.setChecked(Settings.System.getInt(
                getContentResolver(),
                SettingsConstants.System.AUTORUN_SWITCH, 1) != 0);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mAutorunEnable) {
            boolean value = mAutorunEnable.isChecked();
            Settings.System.putInt(getContentResolver(), SettingsConstants.System.AUTORUN_SWITCH,
                    value ? 1 : 0);
            sendBroadcast(new Intent(LGIntent.AUTORUN_ENABLE_CHANGED));
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
