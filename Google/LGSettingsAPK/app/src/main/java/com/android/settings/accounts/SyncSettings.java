/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.AccountPreference;
import com.android.settings.DialogCreatable;
import com.android.settings.R;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import android.content.SyncStatusObserver;
import android.os.Handler;

public class SyncSettings extends AccountPreferenceBase
        implements OnAccountsUpdateListener, DialogCreatable {

    private static final String KEY_SYNC_SWITCH = "sync_switch";

    private String[] mAuthorities;

    private SettingsDialogFragment mDialogFragment;
    private CheckBoxPreference mAutoSyncPreference;
    private final Handler mHandler = new Handler();
    private boolean isRemovedBUA = false;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.sync_settings);
        mAutoSyncPreference =
                (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_SYNC_SWITCH);
        mAutoSyncPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (ActivityManager.isUserAMonkey()) {
                    Log.d("SyncSettings", "ignoring monkey's attempt to flip sync state");
                } else {
                    ContentResolver.setMasterSyncAutomatically((Boolean)newValue);
                }
                return true;
            }
        });

        getActivity().getContentResolver().addStatusChangeListener(
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE
                        | ContentResolver.SYNC_OBSERVER_TYPE_STATUS
                        | ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS, mSyncStatusObserver);

        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Activity activity = getActivity();
        AccountManager.get(activity).addOnAccountsUpdatedListener(this, null, true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-280][ID-MDM-52]
        if (null == mAutoSyncPreference) {
            // Do nothing.
        }
        else if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && com.lge.mdm.LGMDMManager.getInstance().isManualSyncCurrent()) {
            com.android.settings.MDMSettingsAdapter.getInstance().setAutoMasterSynceEnableMenu(
                    null, mAutoSyncPreference);
        }
        // LGMDM_END
        else {
            mAutoSyncPreference.setChecked(ContentResolver.getMasterSyncAutomatically());
        }

        mAuthorities = activity.getIntent().getStringArrayExtra(AUTHORITIES_FILTER_KEY);
        isRemovedBUA = activity.getIntent().getBooleanExtra("REMOVE_BUA", false);
        Log.i("hsmodel", "isRemovedBUA : " + (isRemovedBUA ? "true" : "false"));
        updateAuthDescriptions();
    }

    @Override
    public void onStop() {
        super.onStop();
        final Activity activity = getActivity();
        AccountManager.get(activity).removeOnAccountsUpdatedListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferences, Preference preference) {
        if (preference instanceof AccountPreference) {
            if (preference.getTitleRes() == R.string.sp_preference_bua_plus_title_NORMAL) {
                if (!cloudEnable()) {
                    Intent intent = new Intent("com.lge.vzw.bua.intent.action.SUBSCRIBE");
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(new ComponentName("com.lge.cloudvmm",
                            "com.lge.cloudvmm.ApplicationGateActivity"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } else {
                startAccountSettings((AccountPreference)preference);
            }
        } else {
            return false;
        }
        return true;
    }

    private void startAccountSettings(AccountPreference acctPref) {
        Intent intent = new Intent("android.settings.ACCOUNT_SYNC_SETTINGS");
        intent.putExtra(AccountSyncSettings.ACCOUNT_KEY, acctPref.getAccount());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void showDialog(int dialogId) {
        if (mDialogFragment != null) {
            Log.e(TAG, "Old dialog fragment not null!");
        }
        mDialogFragment = new SettingsDialogFragment(this, dialogId);
        mDialogFragment.show(getActivity().getFragmentManager(), Integer.toString(dialogId));
    }

    private void removeAccountPreferences() {
        PreferenceScreen parent = getPreferenceScreen();
        for (int i = 0; i < parent.getPreferenceCount();) {
            if (parent.getPreference(i) instanceof AccountPreference) {
                parent.removePreference(parent.getPreference(i));
            } else {
                i++;
            }
        }
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        if (getActivity() == null) {
            return;
        }
        removeAccountPreferences();
        for (int i = 0, n = accounts.length; i < n; i++) {
            final Account account = accounts[i];
            final ArrayList<String> auths = AccountsUtils
                    .getAuthoritiesForAccountType(account.type);
            if (!isVisibleAccounts(account)) {
                continue;
            }
            boolean showAccount = true;
            if (mAuthorities != null && auths != null) {
                showAccount = false;
                for (String requestedAuthority : mAuthorities) {
                    if (auths.contains(requestedAuthority)) {
                        showAccount = true;
                        break;
                    }
                }
            }

            if (showAccount) {
                final Drawable icon = getDrawableForType(account.type);
                final AccountPreference preference =
                        new AccountPreference(getActivity(), account, icon, auths, true, 0);
                getPreferenceScreen().addPreference(preference);

                if (account.type.equals("com.fusionone.account")) {
                    preference.setTitle(R.string.sp_preference_bua_plus_title_NORMAL);
                    preference.setSummary("");
                } else if (account.type
                        .startsWith(AccountsGroupSettingsActivity.LG_ACCOUNTS_FOR_WIDGET_TYPE)) {
                    preference.setSummary("");
                } else {
                    preference.setSummary(getLabelForType(account.type));
                }
            }
        }
        onSyncStateUpdated();
    }

    @Override
    protected void onAuthDescriptionsUpdated() {
        // Update account icons for all account preference items
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (pref instanceof AccountPreference) {
                AccountPreference accPref = (AccountPreference)
                        getPreferenceScreen().getPreference(i);
                accPref.setIcon(getDrawableForType(accPref.getAccount().type));
                accPref.setSummary(getLabelForType(accPref.getAccount().type));
            }
        }
    }

    private boolean isVisibleAccounts(Account account) {
        // TODO Auto-generated method stub

        if (account.type.equals("com.mobileleader.sync")) {
            return false;
        }
        if (account.type.equals("com.lge.sync")) {
            return false;
        }
        if (account.type.equals("com.att.aab") && !Config.getOperator().equals(Config.ATT)) {
            return false;
        }
        if (account.type.equals("com.fusionone.account") && isRemovedBUA) {
            return false;
        }
        if (account.type.equals("com.lge.vmemo")) {
            return false;
        }
        if (account.type.equals("com.lge.android.weather.sync")) {
            return false;
        }
        if (account.type.equals("com.verizon.phone")) {
            return false;
        }
        if (account.type.equals("com.android.sim")) {
            return false;
        }
        if (account.type.equals("com.lge.lifetracker")) {
            return false;
        }
        if (account.type.equals("com.lge.bioitplatform.sdservice")) {
            return false;
        }
        if (account.type.equals("com.verizon.tablet")) {
            return false;
        }
        /*if( accountType.equals("com.lge.myphonebook") && showMyPhoneBookAccount() ) {
            isMyPhoneBook = true;
        } else if ( accountType.equals("com.lge.myphonebook") && !showMyPhoneBookAccount() ) {
            isMyPhoneBook = false;
            return false;
        }*/

        return true;
    }

    protected boolean cloudEnable() {
        boolean result = false;
        PackageManager pm = getPackageManager();
        if (pm != null) {
            Log.d("BUA+",
                    "pm.hasSystemFeature(com.lge.cloudservice.enabled); : "
                            + pm.hasSystemFeature("com.lge.cloudservice.enabled"));
            result = pm.hasSystemFeature("com.lge.cloudservice.enabled");
        }
        return result;
    }

    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        public void onStatusChanged(int which) {
            mHandler.post(new Runnable() {
                public void run() {
                    if (mAutoSyncPreference != null) {
                        Log.i("hsmodel",
                                "mAccountSyncPreference : "
                                        + ContentResolver.getMasterSyncAutomatically());
                        //autoSyncData.setChecked( ContentResolver.getMasterSyncAutomatically() );
                        mAutoSyncPreference.setChecked(ContentResolver.getMasterSyncAutomatically());
                    }
                }
            });
        }
    };

}
