package com.android.settings.nfc;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

public class NfcSettingsFragment extends PreferenceFragment  {
    private final String TAG = "NFC_SETTINGS";

    public static final int NUMBER_REMINDER = 100;
    public static final String KEY_NFC_EVERY = "nfc_everyday";
    public static final String KEY_NFC_REMIDER = "nfc_reminders";

    public static final int NUMBER_TAG_DEFAULT_CHECKBOX = 1;
    public static final int NUMBER_HANDOVER_CHECKBOX = 2;
    public static final int NUMBER_CALLING_NFCSOUND = 3;

    public static final String KEY_SHOW_NFC_EVERY = "nfc_every";
    public static final String PREFS_NAME = "nfc_settings";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //[START][PORTING_FOR_NFCSETTING] 
        Activity activity = getActivity();

        if (NfcSettingAdapter.NfcUtils.isSupportedRemoteNfcSettings(activity)) {

            Log.d (TAG, "Jump to NfcSettings app.");
            NfcSettingAdapter.ExtRequestConnection.startActivityForNfcSetting(activity,
                                                NfcSettingAdapter.ExtRequestConnection.REQUEST_START_NFC_SETTING_FRAGMENT);
        }
        //[END][PORTING_FOR_NFCSETTING]
        activity.finish();
        return;
    }

}
