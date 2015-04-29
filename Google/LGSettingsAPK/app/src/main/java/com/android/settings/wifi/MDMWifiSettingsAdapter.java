/*========================================================================
Copyright (c) 2011 by LG MDM.  All Rights Reserved.

            EDIT HISTORY FOR MODULE

This section contains comments describing changes made to the module.
Notice that changes are listed in reverse chronological order.

when        who                 what, where, why
----------  --------------      ---------------------------------------------------

=========================================================================*/

package com.android.settings.wifi;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.CheckBoxPreference;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.Utils;
import com.lge.cappuccino.IMdm;
import com.lge.mdm.LGMDMManager;
import com.lge.mdm.LGMDMManagerInternal;

public class MDMWifiSettingsAdapter {
    private static String TAG = "MDMWifiSettingsAdapter";

    // make sure LGMDMIntentInternal.java
    private static final String ACTION_WIFI_POLICY_CHANGE = "com.lge.mdm.intent.action.WIFI_POLICY_CHANGE";
    private static final String ACTION_TETHER_POLICY_CHANGE = "com.lge.mdm.intent.action.TETHER_POLICY_CHANGE";

    private static MDMWifiSettingsAdapter mInstance;

    public static MDMWifiSettingsAdapter getInstance() {
        if (mInstance == null) {
            mInstance = new MDMWifiSettingsAdapter();
        }
        return mInstance;
    }

    private MDMWifiSettingsAdapter() {
    }

    /*temporary block
    public void setWifiSettingDisable(WifiSettings.WifiSettingsAs wifiSettingsAs) {
        if (wifiSettingsAs == null) {
            return;
        }
        if (LGMDMManager.getInstance().getAllowWifi(null) == false) {
            Log.i(TAG, "[LGMDM] Disabllow WIFi");
            wifiSettingsAs.onAddMessage(R.string.sp_lgmdm_blockwifi_NORMAL);
            wifiSettingsAs.onRemoveAllAccessPoint();
        }
        return;
    }
    */
    public void setWiFiEnableMenu(Switch menu) {
        if (menu == null) {
            Log.i(TAG, "setWiFiEnableMenu : menu is null");
            return;
        }

        if (LGMDMManager.getInstance().getAllowWifi(null) == false) {
            menu.setEnabled(false);
            Log.i(TAG, "setWiFiEnableMenu : LGMDM disabllow mSwitch");
        }

        return;
    }

    public boolean setWiFiEnableSummary(TextView summary) {
        if (summary == null) {
            Log.i(TAG, "setWiFiEnableSummary : summary is null");
            return false;
        }

        if (LGMDMManager.getInstance().getAllowWifi(null) == false) {
            summary.setText(R.string.sp_lgmdm_blockwifi_NORMAL);
            Log.i(TAG, "setWiFiEnableSummary : LGMDM disabllow summary");
            return true;
        }

        return false;
    }

    public void setWiFiScreenEnablerMenu(Switch switchButton) {
        if (switchButton == null) {
            Log.i(TAG, "[LGMDM] setWiFiScreenEnablerMenu : switchButton is null");
            return;
        }
        if (LGMDMManager.getInstance().getAllowWifi(null) == false
                || LGMDMManager.getInstance().getAllowWifiDirect(null) == false
                || LGMDMManager.getInstance().getAllowMiracast(null) == false) {
            switchButton.setChecked(false);
            switchButton.setEnabled(false);
            Log.i(TAG, "[LGMDM] WifiScreenEnabler disallow mode");
        }
    }

    public void makeToastDisallowHotspot(Context context) {
        Toast.makeText(context, Utils.getString(R.string.sp_block_wifi_hotspot_NORMAL),
                Toast.LENGTH_SHORT).show();
    }

    public boolean checkDisallowHotspot() {
        if (LGMDMManagerInternal.getInstance().getDisallowTetheringType(IMdm.TETHER_HOTSPOT)) {
            Log.i(TAG, "checkDisallowHotspot : LGMDM disallow Hotspot");
            return true; // disallow
        }
        return false; // allow
    }

    // for WifiApEnabler CheckBoxPreference
    public boolean setWifiApEnablerMenu(CheckBoxPreference menu) {
        if (menu == null) {
            return false;
        }

        if (checkDisallowHotspot()) {
            menu.setEnabled(false);
            menu.setSummary(R.string.sp_block_wifi_hotspot_NORMAL);
            Log.i(TAG, "setWifiApEnablerMenu : LGMDM disable Hotspot switch");
            return true;
        }
        return false;
    }

    // for WifiApEnabler Switch
    public boolean setWifiApEnablerMenu(Switch menu) {
        if (menu == null) {
            return false;
        }

        if (checkDisallowHotspot()) {
            menu.setEnabled(false);
            Log.i(TAG, "setWifiApEnablerMenu : LGMDM disable Hotspot switch");
            return true; // disallow
        }
        return false; // allow
    }

    // for use WifiP2pSettings.java onResume()
    public boolean checkDisallowWifiDirect(Context context) {
        if (LGMDMManager.getInstance().getAllowWifiDirect(null) == false
                || LGMDMManager.getInstance().getAllowWifi(null) == false) {
            Log.i(TAG, "[checkDisallowWifiDirect] disallow Wi-Fi direct");
            if (context != null) {
                Toast.makeText(context,
                        Utils.getString(R.string.sp_lgmdm_block_wifi_direct_NORMAL),
                        Toast.LENGTH_SHORT).show();
            }
            return true; // disallow
        }
        return false; // allow
    }

    public void addWifiPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_WIFI_POLICY_CHANGE);
    }

    public void addTetherPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_TETHER_POLICY_CHANGE);
    }

    public boolean receiveWifiPolicyChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        if (ACTION_WIFI_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveTetherPolicyChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        if (ACTION_TETHER_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean getAllowWiFiProfileManagement() {
        return LGMDMManager.getInstance().getAllowWiFiProfileManagement(null);
    }

    public boolean setAllowModifyNetwork(Context context) {
        if (!LGMDMManager.getInstance().getAllowWiFiProfileManagement(null)) {
            Log.d(TAG, "setAllowModifyNetwork check");
            if (context != null) {
                Toast.makeText(context,
                        Utils.getString(R.string.sp_lgmdm_block_modify_wifi_network_NORMAL),
                        Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    public boolean setAllowForgetNetwork(Context context) {
        if (getAllowWiFiProfileManagement() == false) {
            Log.i(TAG, "[setAllowForgetNetwork] disallow forget network");
            if (context != null) {
                Toast.makeText(context,
                        Utils.getString(R.string.sp_lgmdm_block_forget_wifi_network_NORMAL),
                        Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    public boolean checkDisallowMiracast(Context context, boolean needToast) {
        if (LGMDMManager.getInstance().getAllowWifi(null) == false
                || LGMDMManager.getInstance().getAllowWifiDirect(null) == false
                || LGMDMManager.getInstance().getAllowMiracast(null) == false) {
            if (context != null && needToast) {
                Toast.makeText(context, Utils.getString(R.string.sp_lgmdm_block_miracast_NORMAL),
                        Toast.LENGTH_SHORT).show();
            }
            return true; // disallow
        }
        return false; // allow
    }

    public boolean checkDisallowWifiScan(Context context, boolean needToast) {
        if (LGMDMManager.getInstance().getAllowWifiScan(null) == false) {
            Log.i(TAG, "checkDisallowWifiScan : LGMDM disallow WifiAutoScan");
            if (context != null && needToast) {
                Toast.makeText(context, Utils.getString(R.string.sp_lgmdm_block_wifi_scan_point),
                        Toast.LENGTH_SHORT).show();
            }
            return true; // disallow
        }
        return false; // allow
    }

    public boolean checkDisallowWifiPwdVisible(Context context, boolean needToast) {
        if (LGMDMManager.getInstance().getAllowPasswordVisible(null) == false) {
            Log.i(TAG, "checkDisallowWifiPasswordVisible : LGMDM disallow WifiPasswordVisible");
            if (needToast && context != null) {
                Toast.makeText(context,
                        Utils.getString(R.string.sp_pwd_visibility_disabled_by_mdm),
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    public boolean checkDisallowWifiAutoConnection(Context context) {
        if (LGMDMManager.getInstance().getAllowWifiAutoConnection(null) == false) {
            Log.i(TAG, "checkDisallowWifiAutoConnection : LGMDM disallow WifiAutoConnection");
            if (context != null) {
                Toast.makeText(context,
                        Utils.getString(R.string.sp_lgmdm_block_wifi_auto_connect_NORMAL),
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }
}
