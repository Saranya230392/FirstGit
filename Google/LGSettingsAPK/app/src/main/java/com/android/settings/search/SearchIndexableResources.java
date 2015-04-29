/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.search;

import com.android.settings.AirplaneModeFragment;
import com.android.settings.DisplaySettings;
import com.android.settings.SoundSettings;
import com.android.settings.SecuritySettings;
import com.android.settings.lge.EmotionalLEDEffectTabFront;
import com.android.settings.lge.FrontTouchKey;
import com.android.settings.lge.QuickWindowCase;
import com.android.settings.hotkey.ShortcutkeySettings;
import com.android.settings.PrivacySettings;
import com.android.settings.vpn2.VpnSettings;
import android.provider.SearchIndexableResource;

import com.android.settings.lge.DeviceInfoLge;
import com.android.settings.lge.DeviceInfoLgeBattery;
import com.android.settings.lge.DeviceInfoLgeNetwork;
import com.android.settings.lge.DeviceInfoLgePhoneIdentity;
import com.android.settings.lge.DeviceInfoLgeStatus;
import com.android.settings.lge.DeviceInfoLgeSoftwareInformation;
import com.android.settings.lge.DeviceInfoLgeHardwareInformation;

import com.android.settings.deviceinfo.Memory;
import com.android.settings.users.UserSettings;
import com.android.settings.applications.ManageApplications;
import com.android.settings.notification.ZenModeSettings;
import com.android.settings.powersave.BatterySettings;
import com.android.settings.sound.ACRSettings;

import com.android.settings.OneHandOperationSettings;
import com.android.settings.DualWindowSettings;
import com.android.settings.inputmethod.InputMethodAndLanguageSettings;
import com.android.settings.location.LocationSettings;
import com.android.settings.accounts.AccountsGroupSettingsActivity;
import com.android.settings.TouchFeedbackAndSystemPreference;
import com.android.settings.handsfreemode.HandsFreeModePreferenceActivity;

import com.android.settings.lge.ShareConnection;
import com.android.settings.TetherNetworkSettings;
import com.android.settings.DateTimeSettings;
import com.android.settings.DataUsageSummary;

import com.android.settings.R;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public final class SearchIndexableResources {

    public static int sNO_DATA = 0;
    public static int sNO_DATA_RES_ID = 0;

    private static HashMap<String, SearchIndexableResource> sResMap = new HashMap<String, SearchIndexableResource>();

    static {
        sResMap.put(DisplaySettings.class.getName(),
                new SearchIndexableResource(sNO_DATA, sNO_DATA,
                        DisplaySettings.class.getName(), sNO_DATA));
        sResMap.put(FrontTouchKey.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, FrontTouchKey.class.getName(), sNO_DATA));
        sResMap.put(PrivacySettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, PrivacySettings.class.getName(), sNO_DATA));
        sResMap.put(QuickWindowCase.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, QuickWindowCase.class.getName(), sNO_DATA));
        sResMap.put(ShortcutkeySettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, ShortcutkeySettings.class.getName(), sNO_DATA));
        sResMap.put(EmotionalLEDEffectTabFront.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, EmotionalLEDEffectTabFront.class.getName(), sNO_DATA));
        sResMap.put(Memory.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, Memory.class.getName(), sNO_DATA));
        sResMap.put(UserSettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, UserSettings.class.getName(), sNO_DATA));
        sResMap.put(ManageApplications.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, ManageApplications.class.getName(), sNO_DATA));
        sResMap.put(ZenModeSettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, ZenModeSettings.class.getName(), sNO_DATA));
        sResMap.put(DeviceInfoLge.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, DeviceInfoLge.class.getName(), sNO_DATA));
        sResMap.put(DeviceInfoLgeBattery.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, DeviceInfoLgeBattery.class.getName(), sNO_DATA));
        sResMap.put(DeviceInfoLgeNetwork.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, DeviceInfoLgeNetwork.class.getName(), sNO_DATA));
        sResMap.put(DeviceInfoLgePhoneIdentity.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, DeviceInfoLgePhoneIdentity.class.getName(), sNO_DATA));
        sResMap.put(DeviceInfoLgeStatus.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, DeviceInfoLgeStatus.class.getName(), sNO_DATA));
        sResMap.put(DeviceInfoLgeSoftwareInformation.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, DeviceInfoLgeSoftwareInformation.class.getName(), sNO_DATA));
        sResMap.put(DeviceInfoLgeHardwareInformation.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, DeviceInfoLgeHardwareInformation.class.getName(), sNO_DATA));
        sResMap.put(AirplaneModeFragment.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, AirplaneModeFragment.class.getName(), sNO_DATA));
        sResMap.put(DualWindowSettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, DualWindowSettings.class.getName(), sNO_DATA));
        sResMap.put(InputMethodAndLanguageSettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, InputMethodAndLanguageSettings.class.getName(), sNO_DATA));
        sResMap.put(LocationSettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, LocationSettings.class.getName(), sNO_DATA));
        sResMap.put(AccountsGroupSettingsActivity.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, AccountsGroupSettingsActivity.class.getName(), sNO_DATA));
        sResMap.put(OneHandOperationSettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, OneHandOperationSettings.class.getName(), sNO_DATA));
        sResMap.put(BatterySettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, BatterySettings.class.getName(), sNO_DATA));
        sResMap.put(ShareConnection.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, ShareConnection.class.getName(), sNO_DATA));
        sResMap.put(TetherNetworkSettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, TetherNetworkSettings.class.getName(), sNO_DATA));
        sResMap.put(DateTimeSettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, DateTimeSettings.class.getName(), sNO_DATA));
        sResMap.put(DataUsageSummary.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, DataUsageSummary.class.getName(), sNO_DATA));

        // Add Sound ResMap
        sResMap.put(SoundSettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, SoundSettings.class.getName(), sNO_DATA));
        // Add VPN ResMap
        sResMap.put(VpnSettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, VpnSettings.class.getName(), sNO_DATA));
        // Add ACRSettings ResMap
        sResMap.put(ACRSettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, ACRSettings.class.getName(), sNO_DATA));
        sResMap.put(SecuritySettings.class.getName(), new SearchIndexableResource(
                sNO_DATA, sNO_DATA, SecuritySettings.class.getName(), sNO_DATA));

        // Add Sound > SoundEffect ResMap
        sResMap.put(TouchFeedbackAndSystemPreference.class.getName(),
                new SearchIndexableResource(sNO_DATA, sNO_DATA,
                        TouchFeedbackAndSystemPreference.class.getName(),
                        sNO_DATA));
        // Add Sound > HandsFreeMode ResMap
        sResMap.put(HandsFreeModePreferenceActivity.class.getName(),
                new SearchIndexableResource(sNO_DATA, sNO_DATA,
                        HandsFreeModePreferenceActivity.class.getName(),
                        sNO_DATA));
    }

    private SearchIndexableResources() {

    }

    public static int size() {
        return sResMap.size();
    }

    public static SearchIndexableResource getResourceByName(String className) {
        return sResMap.get(className);
    }

    public static Collection<SearchIndexableResource> values() {
        return sResMap.values();
    }

}
