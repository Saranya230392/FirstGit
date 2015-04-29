package com.android.settings.nfc;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

public class NfcSettings extends PreferenceFragment {
    private final String TAG = "NFC_SETTINGS";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub3
    
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
    
        //[START][PORTING_FOR_NFCSETTING] 
        if (NfcSettingAdapter.NfcUtils.isSupportedRemoteNfcSettings(activity)) {
            Log.d (TAG, "Jump to NfcSettings app.");
            //[START][PORTING_FOR_NFCSETTING]
            NfcSettingAdapter.ExtRequestConnection.startActivityForNfcSetting(activity,
                                NfcSettingAdapter.ExtRequestConnection.REQUEST_START_NFC_SETTING);
        }
        activity.finish();
        return;
        //[END][PORTING_FOR_NFCSETTING]
    }
}
