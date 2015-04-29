package com.android.settings.lge;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.util.Log;

import com.android.settings.Utils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class DeviceInfoLgeSoftwareUpdateWifi extends SettingsPreferenceFragment {

    private static final String LOG_TAG = "aboutphone  # DeviceInfoLge";

    private static final String KEY_SYSTEM_UPDATE_SETTINGS = "system_update_settings";

    //private static final String KEY_SYSTEM_UPDATE_SETTINGS_FOR_TMUS = "system_update_settings_for_tmus";

    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);

        Log.i(LOG_TAG, "onCreate");

        addPreferencesFromResource(R.xml.device_info_lge_software_update_wifi);

        /* Settings is a generic app and should not contain any device-specific info. */
        final Activity act = getActivity();

        // These are contained by the root preference screen
        PreferenceGroup parentPreference = getPreferenceScreen();

        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference,
                KEY_SYSTEM_UPDATE_SETTINGS,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "onResume()");
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
