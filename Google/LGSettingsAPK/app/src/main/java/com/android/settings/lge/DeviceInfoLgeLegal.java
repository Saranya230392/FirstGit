package com.android.settings.lge;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
//import android.telephony.TelephonyManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.os.Build;

//[S][2012.03.17][seungeene][DCM][NA] Add Next-i FOTA for Docomo
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.remote.RemoteFragmentManager;

public class DeviceInfoLgeLegal extends SettingsPreferenceFragment {

    private static final String LOG_TAG = "aboutphone # DeviceInfoLgeLegal";

    //private static final String KEY_CONTAINER = "container";
    private static final String KEY_TEAM = "team";
    //private static final String KEY_CONTRIBUTORS = "contributors";
    private static final String KEY_TERMS = "terms";
    private static final String KEY_LICENSE = "license";
    private static final String KEY_LICENSE_MR1 = "licensemr1";
    private static final String KEY_COPYRIGHT = "copyright";
    //private static final String KEY_SYSTEM_UPDATE_SETTINGS = "system_update_settings";

    //private static final String KEY_PHONEID_PROFILE = "phoneidentity";

    private static String operator_code;
    private static final String KEY_CARRIER_LEGAL = "carrier_legal";
    private static final String KEY_CARRIER_LEGAL_UPDATE = "carrier_legal_update";
    private static final String KEY_MPCS_LEGAL = "mpcs_legal";
    private static final String KEY_EULA = "eula";

    @Override
    public void onCreate(Bundle icicle) {

        //int activePhoneType = TelephonyManager.getDefault().getCurrentPhoneType();

        super.onCreate(icicle);

        Log.i(LOG_TAG, "onCreate");

        addPreferencesFromResource(R.xml.device_info_legalinfo);

        operator_code = Config.getOperator();

        final Activity act = getActivity();
        // These are contained in the "container" preference group
        //        PreferenceGroup parentPreference = (PreferenceGroup) findPreference(KEY_CONTAINER);
        PreferenceGroup parentPreference = getPreferenceScreen();
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_EULA,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        if ("CN".equals(Config.getCountry())) {
            Log.i(LOG_TAG, "remove terms");
            removePreferenceFromScreen(KEY_TERMS);
        } else {
            Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_TERMS,
                    Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        }
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            removePreferenceFromScreen(KEY_LICENSE_MR1);
            Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_LICENSE,
                    Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        } else {
            removePreferenceFromScreen(KEY_LICENSE);
            Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_LICENSE_MR1,
                    Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        }
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_COPYRIGHT,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_TEAM,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);

        if ("SPR".equals(operator_code) || "BM".equals(operator_code)) {
            if ("TRF".equals(operator_code)) { // added for exception in L25L
                removePreferenceFromScreen(KEY_CARRIER_LEGAL);
                removePreferenceFromScreen(KEY_CARRIER_LEGAL_UPDATE);
            } else {
                if ("g2".equals(Build.DEVICE)
                        && !Utils.isUI_4_1_model(getActivity())) {
                    Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference,
                            KEY_CARRIER_LEGAL_UPDATE,
                            Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
                    removePreferenceFromScreen(KEY_CARRIER_LEGAL);
                } else {
                    Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference,
                            KEY_CARRIER_LEGAL,
                            Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
                    removePreferenceFromScreen(KEY_CARRIER_LEGAL_UPDATE);
                }
            }
        } else {
            Log.i(LOG_TAG, "remove carrier_legal");
            removePreferenceFromScreen(KEY_CARRIER_LEGAL);
            removePreferenceFromScreen(KEY_CARRIER_LEGAL_UPDATE);
        }

        if ("MPCS".equals(operator_code)) {
            Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_MPCS_LEGAL,
                    Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        } else {
            Log.i(LOG_TAG, "remove mpcs_legal");
            removePreferenceFromScreen(KEY_MPCS_LEGAL);
        }
    }

    /**
     * Removes the specified preference, if it exists.
     *
     * @param key
     *            the key for the Preference item
     */
    private void removePreferenceFromScreen(String key) {
        Preference pref = findPreference(key);

        if (pref != null && getPreferenceScreen() != null) {
            getPreferenceScreen().removePreference(pref);
        }
    }
    
    // yonguk.kim 2015-02-14 Apply remote fragment feature [START]
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        Log.d("kimyow", "onPreferenceTreeClick:" + preference.getKey());
        if (Utils.supportRemoteFragment(getActivity())) {
            if ("eula".equals(preference.getKey())) {
                PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
                boolean result = RemoteFragmentManager.setPreferenceForRemoteFragment(
                        "com.lge.lgsetupwizard.eula", preference);
                if (result) {
                    preferenceActivity.onPreferenceStartFragment(this, preference);
                    return true;
                }
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    // yonguk.kim 2015-02-14 Apply remote fragment feature [END]    

}
