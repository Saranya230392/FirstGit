package com.android.settings.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.android.settings.lgesetting.Config.Config;
import com.android.settings.Utils;
import com.android.settings.R;

public class JumpActivity extends PreferenceActivity {

    private final String KEY_WIRELESS_SETTINGS = "wireless_settings";
    private final String KEY_SHARE_CONNECT_SETTINGS = "share_connect";
    private final String KEY_TETHER_NETWORK_SETTINGS = "tether_network_settings";
    private final String KEY_HOTKEY_SETTINGS = "hotkey_settings";
    private Preference mWirelessSettings;
    private Preference mShareConnectSettings;
    private Preference mTetherNetworkSettings;
    private Preference mHotKeySettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.jump_activity);
        CreateWirelessNetworksMenu();

    }

    private void CreateWirelessNetworksMenu() {
        mWirelessSettings = (Preference)findPreference(KEY_WIRELESS_SETTINGS);
        mShareConnectSettings = (Preference)findPreference(KEY_SHARE_CONNECT_SETTINGS);
        mTetherNetworkSettings = (Preference)findPreference(KEY_TETHER_NETWORK_SETTINGS);
        mHotKeySettings = (Preference)findPreference(KEY_HOTKEY_SETTINGS);

        if (mWirelessSettings != null) {
            if (!"DCM".equals(Config.getOperator())) {
                getPreferenceScreen().removePreference(mWirelessSettings);
                mWirelessSettings = null;
            }
        }

        if (mShareConnectSettings != null) {
            if (!Utils.supportShareConnect(this)) {
                getPreferenceScreen().removePreference(mShareConnectSettings);
                mShareConnectSettings = null;
            }
        }

        if (mTetherNetworkSettings != null) {
            if ("DCM".equals(Config.getOperator())) {
                getPreferenceScreen().removePreference(mTetherNetworkSettings);
                mTetherNetworkSettings = null;
            }
        }

        if (mHotKeySettings != null) {
            mHotKeySettings.setTitle(R.string.sp_hotkey_NORMAL);

            if ("KR".equals(Config.getCountry2())
                    || "JP".equals(Config.getCountry2())) {
                Log.d("YSY", "HotkeySettings, Q button");
                mHotKeySettings.setTitle(R.string.sp_q_button_NORMAL);
            }
        }
    }

}
