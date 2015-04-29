package com.android.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.security.KeyStore;
import android.preference.PreferenceScreen;
import com.android.settings.R;
import android.util.Log;

public class CertificateManagement extends SettingsPreferenceFragment {

    private static final String KEY_CLEAR_CREDENTIALS = "clear_credentials";
    private Preference mClearCredentials;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.security_settings_certificate_management);
        checkClearCredentials();
    }

    private void checkClearCredentials() {
        mClearCredentials = (Preference)findPreference(KEY_CLEAR_CREDENTIALS);
        KeyStore.State state = KeyStore.getInstance().state();
        if (mClearCredentials != null) {
            mClearCredentials.setEnabled(state != KeyStore.State.UNINITIALIZED);
        }
   }

    @Override
    public void onResume() {
        super.onResume();
        checkClearCredentials();
    }

}

