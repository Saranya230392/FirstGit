package com.android.settings.lge;

import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class DeviceInfoLgeSoftwareUpdate extends SettingsPreferenceFragment {

    private static final String LOG_TAG = "aboutphone  # DeviceInfoLge";

    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);

        Log.i(LOG_TAG, "onCreate");

        addPreferencesFromResource(R.xml.device_info_lge_software_update);
    }

    /**
    * Removes the specified preference, if it exists.
    * @param key the key for the Preference item
    */
    private void removePreferenceFromScreen(String key) {
        Preference pref = findPreference(key);
        // [2012.08.24][munjohn.kang] added a Null pointer exception handling
        if (pref != null && getPreferenceScreen() != null) {
            getPreferenceScreen().removePreference(pref);
        } else {
            if (pref == null) {
                Log.i(LOG_TAG,
                        "removePreferenceFromScreen() removePreferenceFromScreen pref == null");
            } else {
                Log.i(LOG_TAG, "removePreferenceFromScreen() getPreferenceScreen() == null");
            }
        }
    }
}
