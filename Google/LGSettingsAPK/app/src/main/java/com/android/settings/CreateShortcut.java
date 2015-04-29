/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;
import com.lge.wifi.config.LgeWifiConfig;
import android.app.LauncherActivity;
import android.app.LauncherActivity.ListItem;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.os.SystemProperties;
import android.provider.Settings;
import android.os.Build;
//jaewoong87.lee 20141222 Check this is owner mode or not.
import android.app.ActivityManager;
import android.os.UserHandle;

public class CreateShortcut extends LauncherActivityEx {

    private static final String TAG = "CreateShortcut";

    private static final int EASYSETTING_STYLE = 1;

    @Override
    protected Intent getTargetIntent() {
        Intent targetIntent = new Intent(Intent.ACTION_MAIN, null);
        targetIntent.addCategory("com.android.settings.SHORTCUT");
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return targetIntent;
    }

    @Override
    public List<ListItem> makeListItems() {

        ArrayList<ListItem> li = (ArrayList<ListItem>)super.makeListItems();

        if ("TLS".equals(Config.getOperator())) { // portable hotspot
            for (ListItem item : li) {
                if (item.className.equals("com.android.settings.TetheringShortcutActivity")) {
                    item.label = this.getResources().getString(R.string.sp_mobile_hotspot_NORMAL);
                }
            }
        } else if (Config.VZW.equals(Config.getOperator())) {
            for (ListItem item : li) {
                if (item.className.equals("com.android.settings.Settings$ManageApplicationsActivity")) {
                    item.label = this.getResources().getString(R.string.applications_settings_title);
                } else if (item.className.equals("com.android.settings.Settings$SoundSettingsActivity")) {
                    item.label = this.getResources().getString(R.string.notification_settings);
                }
            }
        }

        for (ListItem item : li) {
            if (item.className
                    .equals("com.android.settings.Settings$ConnectivitySettingsActivity")) {
                li.remove(item);
                break;
            }
        }

        //yonguk.kim 20120409 Check if tethering is supported or not
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        //jaewoong87.lee 20141222 Check this is owner mode or not.
        final boolean isOwner = ActivityManager.getCurrentUser() == UserHandle.USER_OWNER;
        Log.d(TAG, "isTetheringSupported=" + cm.isTetheringSupported());
        if (cm.isTetheringSupported() == false || LgeWifiConfig.getOperator().equals("AIO")
                || LgeWifiConfig.getOperator().equals("SBM")
                || isOwner == false) {
            for (ListItem item : li) {
                Log.d(TAG, "className=" + item.className);
                if (item.className.equals("com.android.settings.TetheringShortcutActivity")) {
                    li.remove(item);
                    break;
                }
            }
        } else if ("DCM".equals(Config.getOperator())) {
            for (ListItem item : li) {
                if (item.className.equals("com.android.settings.TetheringShortcutActivity")) {
                    li.remove(item);
                    break;
                }
            }

            for (ListItem item : li) {
                if (item.className.equals("com.android.settings.Settings$TetherSettingsActivity")) {
                    item.label = this.getResources()
                            .getString(R.string.sp_tethering_title_jp_NORMAL);
                    item.icon = this.getResources()
                            .getDrawable(R.drawable.shortcut_hotspot_dcm);
                    break;
                }
            }
        } else {
            if ("VZW".equals(Config.getOperator()) ||
                    "ATT".equals(Config.getOperator()) ||
                    (Config.getCountry().equals("US") &&
                    "TMO".equals(Config.getOperator()))|| 
                    "MPCS".equals(Config.getOperator())) {
                try {
                    synchronized (li) {
                        for (ListItem item : li) {
                            Log.d(TAG, "VZW className=" + item.className);
                            if (item.className
                                    .equals(
                                    "com.android.settings.TetheringShortcutActivity")) {
                                if ("VZW".equals(Config.getOperator())) {
                                    li.remove(item);
                                }
                                if ("ATT".equals(Config.getOperator())) {
                                    if ("e7lte".equals(Build.DEVICE)
                                            || ("t8lte".equals(Build.DEVICE))) {
                                        li.remove(item);
                                    } else {
                                        item.label = this.getResources()
                                            .getString(R.string.sp_mobile_hotspot_NORMAL);
                                        item.icon = this.getResources()
                                            .getDrawable(R.drawable.shortcut_hotspot_att);
                                    }
                                }
                                if ((Config.getCountry().equals("US") && "TMO".equals(Config
                                        .getOperator()))) {
                                    item.label = this.getResources()
                                            .getString(R.string.wifi_tether_checkbox_tmus_text);
                                    item.icon = this.getResources()
                                            .getDrawable(R.drawable.shortcut_hotspot_tmus);
                                }

                                if ("MPCS".equals(Config.getOperator())) {
                                    item.icon = this.getResources()
                                            .getDrawable(R.drawable.shortcut_hotspot_mpcs);
                                }
                                break;
                            }
                        }
                    }
                } catch (ConcurrentModificationException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }

            for (ListItem item : li) {
                if (item.className.equals("com.android.settings.Settings$TetherSettingsActivity")) {
                    li.remove(item);
                    break;
                }
            }            
        }

        try {
            for (ListItem item : li) {
                if (item.className
                        .equals("com.android.settings.Settings$DataUsageSummaryActivity")) {

                    if (Utils.supportSplitView(this)) {
                        Log.d(TAG, "Utils.isWifiOnly() = " + Utils.isWifiOnly(this));
                        if ("ATT".equals(Config.getOperator())) {
                            item.label =
                                    this.getResources().getString(R.string.shortcut_datausage_att);
                        } else if ("VZW".equals(Config.getOperator())) {
                            item.label = this.getResources()
                                    .getString(R.string.data_usage_summary_title);
                        } else if (Utils.isWifiOnly(this)) {
                            item.label = this.getResources()
                                    .getString(R.string.data_usage_summary_title);
                        } else {
                            item.label = this.getResources()
                                    .getString(R.string.data_usage_enable_mobile);
                        }
                    } else {
                        if ("SKT".equals(Config.getOperator()) ||
                                "KT".equals(Config.getOperator())) {
                            item.label = this.getResources()
                                    .getString(R.string.data_network_settings_title);
                        } else if ("LGU".equals(Config.getOperator())) {
                            item.label = this.getResources()
                                    .getString(R.string.data_usage_enable_mobile);
                        } else if ("ATT".equals(Config.getOperator())) {
                            item.label =
                                    this.getResources().getString(R.string.shortcut_datausage_att);
                        } else if ("VZW".equals(Config.getOperator())) {
                            item.label = this.getResources()
                                    .getString(R.string.data_usage_summary_title);
                        } else if (SystemProperties.getBoolean("ro.lge.mtk_dualsim", false) ||
                                SystemProperties.getBoolean("ro.lge.mtk_triplesim", false) ||
                                Utils.isMultiSimEnabled()) {
                            item.label = this.getResources()
                                    .getString(R.string.data_usage_enable_mobile);
                        }
                    }
                    break;
                }
            }
        } catch (ConcurrentModificationException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        return li;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // [S][2012.01.03][hyoungjun21.lee@lge.com][Common] Fix WBT issues
        try {
            //myeonghwan.kim@lge.com 20120926 WBT issue
            if (intentForPosition(position) == null) {
                return;
            }

            Intent shortcutIntent = intentForPosition(position);
            shortcutIntent
                    .setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
                            Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
            Intent intent = new Intent();

            //myeonghwan.kim@lge.com 20120926 WBT issue
            if (itemForPosition(position) == null) {
                return;
            }

            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, itemForPosition(position).label);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher_settings));

            // yonguk.kim 20120423 Support Shortcut Icon [START]
            if (Utils.supportFunctionIcon()) {
                String className = "";
                int settingStyle = Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE,
                        0);

                if (!Utils.supportEasySettings(this)) {
                    settingStyle = 0;
                }

                //myeonghwan.kim@lge.com 20120926 WBT issue
                if (itemForPosition(position) != null &&
                        itemForPosition(position).className != null) {
                    className = itemForPosition(position).className;
                } else {
                    Log.d(TAG, "itemForPosition is null, position=" + position);
                }

                // wifi
                if (className.equals("com.android.settings.WifiShortcutActivity")) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource
                                    .fromContext(this, R.drawable.shortcut_wifi));
                    // bluetooth
                } else if (className.equals(
                        "com.android.settings.BluetoothShortcutActivity")) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource.fromContext(
                                    this,
                                    R.drawable.shortcut_bluetooth));
                    // hotspot
                } else if (className.equals(
                        "com.android.settings.Settings$TetherSettingsActivity")) {
                        if ("DCM".equals(Config.getOperator())) {
                            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                    Intent.ShortcutIconResource
                                            .fromContext(this, R.drawable.shortcut_hotspot_dcm));
                        }
                } else if (className.equals(
                        "com.android.settings.TetheringShortcutActivity")) {
                    //[S][chris.won@lge.com][2012-10-18] Applying New Tethering Icon for DCM
                    if ("ATT".equals(Config.getOperator())) {
                        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                Intent.ShortcutIconResource
                                        .fromContext(this, R.drawable.shortcut_hotspot_att));
                    } else if ((Config.getCountry().equals("US") && "TMO".equals(Config
                            .getOperator()))) {
                        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                Intent.ShortcutIconResource
                                        .fromContext(this, R.drawable.shortcut_hotspot_tmus));
                    } else if ("MPCS".equals(Config.getOperator())) {
                        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                Intent.ShortcutIconResource
                                        .fromContext(this, R.drawable.shortcut_hotspot_mpcs));
                    } else {
                        //[E][chris.won@lge.com][2012-10-18] Applying New Tethering Icon for DCM
                        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                Intent.ShortcutIconResource
                                        .fromContext(this, R.drawable.shortcut_hotspot));
                    }
                    // vpn
                } else if (className.equals("com.android.settings.Settings$VpnSettingsActivity")) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource.fromContext(this, R.drawable.shortcut_vpn));
                    // [shpark82.park] VPN shortcut [S]
                    // lgvpn
                } else if (className.equals("com.android.settings.Settings$VpnSelectorActivity")) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource.fromContext(this, R.drawable.shortcut_vpn));
                    // [shpark82.park] VPN shortcut [E]
                    // sound
                } else if (className.equals(
                        "com.android.settings.Settings$SoundSettingsActivity")) {
                    if (settingStyle == EASYSETTING_STYLE) {
                        shortcutIntent
                                .setClassName("com.lge.settings.easy",
                                        "com.lge.settings.easy.EasySettings");
                        shortcutIntent.putExtra(Intent.EXTRA_TEXT, "sound");
                    }
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource
                                    .fromContext(this, R.drawable.shortcut_sound));
                    // display
                } else if (className.equals(
                        "com.android.settings.Settings$DisplaySettingsActivity")) {
                    if (settingStyle == EASYSETTING_STYLE) {
                        shortcutIntent
                                .setClassName("com.lge.settings.easy",
                                        "com.lge.settings.easy.EasySettings");
                        shortcutIntent.putExtra(Intent.EXTRA_TEXT, "display");
                    }
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource
                                    .fromContext(this, R.drawable.shortcut_display));
                    // manage applications
                } else if (className.equals(
                        "com.android.settings.Settings$ManageApplicationsActivity")) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource
                                    .fromContext(this, R.drawable.shortcut_apps));
                    // location
                } else if (className.equals(
                        "com.android.settings.Settings$LocationSettingsActivity")) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource
                                    .fromContext(this, R.drawable.shortcut_location));
                    // accessibility
                } else if (className.equals(
                        "com.android.settings.Settings$AccessibilitySettingsActivity") ||
                           className.equals(
                        "com.android.settings.Settings$LGAccessibilityShortcut")) {
                    if (Utils.checkPackage(this, "com.android.settingsaccessibility")) {
                        if (Utils.supportSplitView(this)) {
                            shortcutIntent.setAction("com.lge.accessibility.fromshorcut_tablet");
                        } else {
                            shortcutIntent.setClassName(
                                "com.android.settingsaccessibility",
                                "com.android.settingsaccessibility.SettingsAccessibilityActivity");
                        }
                    }
                    
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource
                                    .fromContext(this, R.drawable.shortcut_accessibility));
                    // data usage
                } else if (className.equals(
                        "com.android.settings.Settings$DataUsageSummaryActivity")) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource
                                    .fromContext(this, R.drawable.shortcut_data_usage));
                    // battery
                } else if (className.equals(
                        "com.android.settings.Settings$PowerSaveBatteryDetailActivity")) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource
                                    .fromContext(this, R.drawable.shortcut_battery));
                    // connectivity
                } else if (className.equals(
                        "com.android.settings.Settings$ConnectivitySettingsActivity")) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource
                                    .fromContext(this, R.drawable.shortcut_connectivity));
                    // lockscreen
                } else if (className.equals("com.android.settings.LockScreenShortcutActivity")) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource
                                    .fromContext(this, R.drawable.shortcut_lockscreen));
                    // powerSave
                } else if (className.equals(
                        "com.android.settings.Settings$BatterySaverSettingsActivity")) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource
                                    .fromContext(this, R.drawable.shortcut_powersave));
                    // storage
                } else if (className.equals(
                        "com.android.settings.Settings$StorageSettingsActivity")) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource
                                    .fromContext(this, R.drawable.shortcut_storage));
                    // MobileNFS
                } else if (className.equals("com.lge.wireless_storage.NetworkStorageSettings")) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource
                                    .fromContext(this, R.drawable.shortcut_filenetworking));
                }
            }
            // yonguk.kim 20120423 Support Shortcut Icon [END]
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

            setResult(RESULT_OK, intent);
        } catch (Exception e) {
            ;
        }
        // [E][2012.01.03][hyoungjun21.lee@lge.com][Common] Fix WBT issues
        finish();
    }

    @Override
    protected boolean onEvaluateShowIcons() {
        return Utils.supportFunctionIcon();
    }
}
