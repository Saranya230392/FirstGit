package com.android.settings.lge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.deviceinfo.UsbSettingsControl;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;

public class TetherSettingsHelp extends SettingsPreferenceFragment {

    private static final String TAG = "TetherSettingsHelp";
    private static final String KEY_USB_TETHERING = "usb_tethering";
    private static final String KEY_WIFI_TETHERING = "wifi_tethering";
    private static final String KEY_WIFI_TETHERING_SPR = "wifi_tethering_spr";
    private static final String KEY_BLUETOOTH_TETHERING = "bluetooth_tethering";

    // U2 TMUS - JB upgrade
    private static final String KEY_USB_TETHERING2 = "usb_tethering_u2_tmus";
    private static final String KEY_WIFI_TETHERING2 = "wifi_tethering_u2_tmus";
    //VZW Mobile hotspot
    private static final String KEY_WIFI_TETHERING_VZW = "wifi_tethering_vzw";

    //LGE_ATT_WIFI_S, [jeongwook.kim@lge.com], 2014-05-15, Hide Mobile Hotspot help menu
    private static final int ATT_DEFAULT = 0;
    private static final int ATT_TABLET_DEFAULT = 1;
    //LGE_ATT_WIFI_E, [jeongwook.kim@lge.com], 2014-05-15, Hide Mobile Hotspot help menu

    private String[] mUsbRegexs;
    private String[] mWifiRegexs;
    //private String[] mBluetoothRegexs;

    private boolean mUsbAvailable = false;
    private boolean mWifiAvailable = false;
    private boolean mBluetoothAvailable = false;

    private PreferenceScreen mUsbTethering;
    private PreferenceScreen mWifiTethering;
    private PreferenceScreen mWifiTethering_spr;
    private PreferenceScreen mBtTethering;
    private PreferenceScreen mUsbTethering2;
    private PreferenceScreen mWifiTethering2;
    private PreferenceScreen mWifiTethering_vzw;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        String strNewCo = SystemProperties.get("ro.build.target_operator_ext");
        final Activity activity = getActivity();
        if ("SBM".equals(Config.getOperator())
                    && "user".equals(Build.TYPE)) {
            activity.finish();
        }

        //activity.getActionBar().setTitle(R.string.tether_settings_title_usb);
        //LGE_CHANGE_S, moon-wifi@lge.com by wo0ngs 2012-12-09, Applying New Tethering Icon for DCM
        if (!Utils.supportSplitView(getActivity())) {
            if ("DCM".equals(Config.getOperator())) {
                activity.getActionBar().setIcon(R.drawable.shortcut_hotspot_dcm);
            }
            else if (Config.getCountry().equals("US") && Config.getOperator().equals("TMO")) {
                if ("MPCS_TMO".equals(strNewCo)) {
                    activity.getActionBar().setIcon(R.drawable.shortcut_hotspot_newco);
                } else {
                    activity.getActionBar().setIcon(R.drawable.shortcut_hotspot_tmus);
                }
            }
            else if ("ATT".equals(Config.getOperator())) {
                activity.getActionBar().setIcon(R.drawable.shortcut_networks_setting);
            }
            else if ("MPCS".equals(Config.getOperator())) {
                activity.getActionBar().setIcon(R.drawable.shortcut_hotspot_mpcs);
            }
            else {
                if ("SPR".equals(Config.getOperator()) == false) {
                    activity.getActionBar().setIcon(R.drawable.shortcut_networks_setting);
                }
            }
        }
        //LGE_CHANGE_E, moon-wifi@lge.com by wo0ngs 2012-12-09, Applying New Tethering Icon for DCM
        addPreferencesFromResource(R.xml.tether_settings_help);
        /*LGE_CHANGE_S, [jongpil.yoon@lge.com], 2012-12-24, change the text for att*/
        if ("ATT".equals(Config.getOperator())) {
            getPreferenceScreen().findPreference(KEY_WIFI_TETHERING).setTitle(
                    R.string.sp_mobile_hotspot_NORMAL);
        }
        /*LGE_CHANGE_E, [jongpil.yoon@lge.com], 2012-12-24, change the text for att*/

        //[jaewoong87.lee@lge.com] 2013.06.05 change Wi-Fi tethering title for DCM[S]
        if ("DCM".equals(Config.getOperator())) {
            getPreferenceScreen().findPreference(KEY_WIFI_TETHERING).setTitle(
                    R.string.sp_wifi_tethering_jp_NORMAL);
        }
        //[jaewoong87.lee@lge.com] 2013.06.05 change Wi-Fi tethering title for DCM[E]

        if (Config.getCountry().equals("US") && (Config.getOperator().equals("TMO"))) {
            Log.d("YSY", "TMUS, Help");
            getPreferenceScreen().findPreference(KEY_WIFI_TETHERING_SPR)
                .setTitle(R.string.wifi_tether_checkbox_tmus_text);
        }

        if (Config.getCountry().equals("US") && (Config.getOperator().equals("MPCS"))) {
            getPreferenceScreen().findPreference(KEY_WIFI_TETHERING_SPR)
                .setTitle(R.string.sp_mobile_hotspot_mpcs_title);
        }

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        mUsbRegexs = cm.getTetherableUsbRegexs();
        mWifiRegexs = cm.getTetherableWifiRegexs();
        //mBluetoothRegexs = cm.getTetherableBluetoothRegexs();

        if ((Config.getCountry().equals("US") && Config.getOperator().equals(Config.TMO))
                || Config.getOperator().equals(Config.DCM)
                || Config.getOperator().equals(Config.VZW)
                || Config.getOperator().equals(Config.SPRINT)
                || Config.getOperator().equals(Config.BM)
                || Config.getOperator().equals(Config.MPCS)
                || !UsbSettingsControl
                        .isNotSupplyUSBTethering(getActivity())) {
            mUsbAvailable = mUsbRegexs.length != 0;

            if ((Config.getOperator().equals(Config.SPRINT) || Config.getOperator().equals(
                    Config.BM))
                    && !Utils.getChameleonUsbTetheringMenuEnabled()) {
                mUsbAvailable = false;
            }
            if ("w3c_vzw".equals(SystemProperties.get("ro.product.name"))
                    || "w5c_vzw".equals(SystemProperties.get("ro.product.name"))) {
                mUsbAvailable = false;
            }
        }

        mWifiAvailable = mWifiRegexs.length != 0;

        if (Config.getOperator().equals(Config.VZW)) {
            //mWifiAvailable = false;
        }

        // [S] LGE_BT: MOD/ilbeom.kim/'12-12-12 - [GK] LGBT_COMMON_SCENARIO_PAN_HELPGUIDE
        String ispan = SystemProperties.get("bluetooth.pan", "false");
        if ("true".equals(ispan)) {
            mBluetoothAvailable = true;
        }
        // [E] LGE_BT: MOD/ilbeom.kim/'12-12-12 - [GK] LGBT_COMMON_SCENARIO_PAN_HELPGUIDE
        //+s LGBT_TMUS_BT_TETHERING_DISABLE, [hyuntae0.kim@lge.com 2013.06.18]
        if ("US".equals(Config.getCountry())
                && ("TMO".equals(Config.getOperator()) || "MPCS".equals(Config.getOperator()))) {
            mBluetoothAvailable = false;
        }
        //*e LGBT_TMUS_BT_TETHERING_DISABLE

        Log.d(TAG, "mUsbAvailable : " + mUsbAvailable);
        Log.d(TAG, "mWifiAvailable : " + mWifiAvailable);
        Log.d(TAG, "mBluetoothAvailable : " + mBluetoothAvailable);

        PreferenceScreen parent = getPreferenceScreen();

        mUsbTethering = (PreferenceScreen)parent.findPreference(KEY_USB_TETHERING);
        mWifiTethering = (PreferenceScreen)parent.findPreference(KEY_WIFI_TETHERING);
        mWifiTethering_spr = (PreferenceScreen)parent.findPreference(KEY_WIFI_TETHERING_SPR);
        mWifiTethering_vzw = (PreferenceScreen)parent.findPreference(KEY_WIFI_TETHERING_VZW);
        mBtTethering = (PreferenceScreen)parent.findPreference(KEY_BLUETOOTH_TETHERING);

        // U2 TMUS - JB upgrade
        mUsbTethering2 = (PreferenceScreen)parent.findPreference(KEY_USB_TETHERING2);
        mWifiTethering2 = (PreferenceScreen)parent.findPreference(KEY_WIFI_TETHERING2);

        if (Config.getOperator().equals(Config.VZW)) {
            mUsbTethering.setTitle(R.string.sp_mobile_broadband_connection_NORMAL);
        }


            parent.removePreference(mUsbTethering2);
            parent.removePreference(mWifiTethering2);

            if (!mUsbAvailable || !cm.isTetheringSupported()) {
                parent.removePreference(mUsbTethering);
            }

            if (!mWifiAvailable) {
                parent.removePreference(mWifiTethering);
                parent.removePreference(mWifiTethering_spr);
                parent.removePreference(mWifiTethering_vzw);
            }
            //LGE_CHANGE_S, neo-wifi@lge.com by garry.shin 2013-01-02, change list from wi-fi tethering to portable wi-fi hotspot for sprint
            else {
                if (Config.getCountry().equals("US") &&
                        (Config.getOperator().equals(Config.SPR)
                                || Config.getOperator().equals(Config.TMO)
                                || Config.getOperator().equals(Config.MPCS))) {
                    parent.removePreference(mWifiTethering);
                    parent.removePreference(mWifiTethering_vzw);
                }
                else if (Config.getOperator().equals("VZW")) {
                    parent.removePreference(mWifiTethering);
                    parent.removePreference(mWifiTethering_spr);
                    if (android.provider.Settings.System.getInt(this.getContentResolver(),
                            SettingsConstants.System.VZW_HIDDEN_FEATURE_WIFI, 0) == 1) {
                        Log.d(TAG, "##feature remove Mobile Hotspot Help");
                        parent.removePreference(mWifiTethering_vzw);
                    } //LGE_ATT_WIFI_S, [jeongwook.kim@lge.com], 2014-05-15, Hide Mobile Hotspot help menu
                } else if ((Config.getOperator().equals("ATT"))
                        && (SystemProperties.getInt("wlan.lge.atthotspot", ATT_DEFAULT) == ATT_TABLET_DEFAULT)) {
                    parent.removePreference(mWifiTethering);
                    parent.removePreference(mWifiTethering_spr);
                    parent.removePreference(mWifiTethering_vzw);
                } //LGE_ATT_WIFI_E, [jeongwook.kim@lge.com], 2014-05-15, Hide Mobile Hotspot help menu
                else {
                    parent.removePreference(mWifiTethering_spr);
                    parent.removePreference(mWifiTethering_vzw);
                }
            }
            //LGE_CHANGE_E, neo-wifi@lge.com by garry.shin 2013-01-02, change list from wi-fi tethering to portable wi-fi hotspot for sprint

            if (!mBluetoothAvailable) {
                parent.removePreference(mBtTethering);
            }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mUsbTethering2) {
            Intent intent = new Intent();
            intent.putExtra("Tethering_Type", "USB");
            intent.setClassName("com.android.settings", "com.android.settings.TetherHelpPopup");
            startActivity(intent);
        }
        else if (preference == mWifiTethering2) {
            Intent intent = new Intent();
            intent.putExtra("Tethering_Type", "Wifi");
            intent.setClassName("com.android.settings", "com.android.settings.TetherHelpPopup");
            startActivity(intent);
        } else if (preference == mUsbTethering) {
            Intent intent = new Intent();
            if ("VZW".equals(Config.getOperator())) {
                intent.setClassName("com.android.settings",
                        "com.android.settings.lge.TetherSettingsHelpUsbVZW");
            } else {
                intent.setClassName("com.android.settings",
                        "com.android.settings.lge.TetherSettingsHelpUsb");
            }
            startActivity(intent);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

}
