package com.android.settings.vpn2;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.lgesetting.Config.Config;

import com.lge.mdm.LGMDMManager;

public class VpnSelector extends SettingsPreferenceFragment {

    private static final int VPN_SELECTER_REQUEST = 1;
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        addPreferencesFromResource(R.xml.vpn_selector);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-282]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            android.content.IntentFilter filterLGMDM = new android.content.IntentFilter();
            com.android.settings.MDMSettingsAdapter mdm = com.android.settings.MDMSettingsAdapter
                    .getInstance();
            mdm.addVpnPolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);

            Preference vpnSettings = (Preference)findPreference("vpn_settings");
            Preference lgVpn = (Preference)findPreference("lg_vpn");
            mdm.setVpnSelectorMenu(vpnSettings, lgVpn);
        }
        // LGMDM_END
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        final String key = preference.getKey();

        if ("vpn_settings".equals(key)) {
            PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
            preferenceActivity.startPreferencePanel("com.android.settings.vpn2.VpnSettings", null,
                    R.string.sp_basic_vpn_NORMAL, null, this, VPN_SELECTER_REQUEST);
        } else if ("lg_vpn".equals(key)) {
            Intent intent = new Intent();
            // [shpark82.park][TD:32547] LG VPN flow
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName("com.ipsec.vpnclient", "com.ipsec.vpnclient.MainActivity");
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-282]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            getActivity().unregisterReceiver(mLGMDMReceiver);
        }
        // LGMDM_END
    }

    @Override
    public void onResume() {
        super.onResume();
        checkVpnAllowPolicy();
    }

    public void checkVpnAllowPolicy() {
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            Preference vpnSettings = (Preference)findPreference("vpn_settings");
            Preference lgVpn = (Preference)findPreference("lg_vpn");
            if (LGMDMManager.getInstance() != null) {
                if (!LGMDMManager.getInstance().getAllowNativeVpn(null)) {
                    vpnSettings.setEnabled(false);
                    vpnSettings.setSummary(R.string.sp_lgmdm_block_vpn_connection_NORMAL);
                }
                if (!LGMDMManager.getInstance().getAllowLGVpn(null)) {
                    lgVpn.setEnabled(false);
                    lgVpn.setSummary(R.string.sp_lgmdm_block_vpn_connection_NORMAL);
                }
            }
        }
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-282]
    private final android.content.BroadcastReceiver mLGMDMReceiver = new android.content.BroadcastReceiver() {
        public void onReceive(android.content.Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.android.settings.MDMSettingsAdapter.getInstance()
                        .receiveVpnPolicyChangeIntent(intent)) {
                    getActivity().finish();
                }
            }
        }
    };
    // LGMDM_END
}
