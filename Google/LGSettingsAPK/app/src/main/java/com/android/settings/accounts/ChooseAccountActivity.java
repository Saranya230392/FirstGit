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

package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
import com.android.internal.util.CharSequences;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.google.android.collect.Maps;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import com.lge.constants.SettingsConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import android.view.MenuItem;
import android.provider.Settings;

import static android.content.Intent.EXTRA_USER;

/**
 * Activity asking a user to select an account to be set up.
 */
public class ChooseAccountActivity extends PreferenceActivity {

    private static final String TAG = "ChooseAccountActivity";
    private String[] mAuthorities;
    private PreferenceGroup mAddAccountGroup;
    private final ArrayList<ProviderEntry> mProviderList = new ArrayList<ProviderEntry>();
    public HashSet<String> mAccountTypesFilter;
    private AuthenticatorDescription[] mAuthDescs;
    private Map<String, AuthenticatorDescription> mTypeToAuthDescription = new HashMap<String, AuthenticatorDescription>();
    private Account[] accounts;
    private static HashMap<String, ArrayList<String>> sAccountTypeToAuthorities = null;

    // The UserHandle of the user we are choosing an account for
    private UserHandle mUserHandle;
    private UserManager mUm;

    private static class ProviderEntry implements Comparable<ProviderEntry> {
        private final CharSequence name;
        private final String type;

        ProviderEntry(CharSequence providerName, String accountType) {
            name = providerName;
            type = accountType;
        }

        public int compareTo(ProviderEntry another) {
            if (name == null) {
                return -1;
            }
            if (another.name == null) {
                return +1;
            }
            return CharSequences.compareToIgnoreCase(name, another.name);
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.add_account_screen);
        addPreferencesFromResource(R.xml.add_account_settings);
        mAuthorities = getIntent().getStringArrayExtra(
                AccountPreferenceBase.AUTHORITIES_FILTER_KEY);
        String[] accountTypesFilter = getIntent().getStringArrayExtra(
                AccountPreferenceBase.ACCOUNT_TYPES_FILTER_KEY);
        if (accountTypesFilter != null) {
            mAccountTypesFilter = new HashSet<String>();
            for (String accountType : accountTypesFilter) {
                mAccountTypesFilter.add(accountType);
            }
        }
        mAddAccountGroup = getPreferenceScreen();
        accounts = AccountManager.get(this).getAccounts();
        mUm = UserManager.get(this);
        mUserHandle = Utils.getSecureTargetUser(getActivityToken(), mUm, null /* arguments */,
                getIntent().getExtras());
        updateAuthDescriptions();
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (!"VZW".equals(Config.getOperator())) {
                Log.i("hsmodel", "may be here");
                /*Intent intent = new Intent("android.settings.SYNC_SETTINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);*/
                finish();
                return true;
            } else {
                //finish();
                Log.i("hsmodel", "may be here1111");
                int settingStyle = Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                        Log.d("kimyow", "tabIndex=" + tabIndex);
                        settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                        startActivity(settings);
                        finish();
                        return true;
                    }
                } else {
                    finish();
                    return true;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates provider icons. Subclasses should call this in onCreate()
     * and update any UI that depends on AuthenticatorDescriptions in onAuthDescriptionsUpdated().
     */
    private void updateAuthDescriptions() {
        mAuthDescs = AccountManager.get(this).getAuthenticatorTypesAsUser(
                mUserHandle.getIdentifier());
        for (int i = 0; i < mAuthDescs.length; i++) {
            mTypeToAuthDescription.put(mAuthDescs[i].type, mAuthDescs[i]);
        }
        onAuthDescriptionsUpdated();
    }

    private void onAuthDescriptionsUpdated() {
        // Create list of providers to show on preference screen
        for (int i = 0; i < mAuthDescs.length; i++) {
            String accountType = mAuthDescs[i].type;
            CharSequence providerName = getLabelForType(accountType);

            if (!AccountsUtils.isVisibleAccount(accountType)) {
                continue;
            }
            // Skip preferences for authorities not specified. If no authorities specified,
            // then include them all.
            ArrayList<String> accountAuths = getAuthoritiesForAccountType(accountType);

            boolean addAccountPref = AccountsUtils.getAddAccountPref(accountAuths, mAuthorities);

            if (addAccountPref && mAccountTypesFilter != null
                    && !mAccountTypesFilter.contains(accountType)) {
                addAccountPref = false;
            }
            if (addAccountPref) {
                mProviderList.add(new ProviderEntry(providerName, accountType));
            } else {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Skipped pref " + providerName + ": has no authority we need");
                }
            }
        }

        if (mProviderList.size() == 1) {
            // If there's only one provider that matches, just run it.
            finishWithAccountType(mProviderList.get(0).type);
        } else if (mProviderList.size() > 0) {
            Collections.sort(mProviderList);
            mAddAccountGroup.removeAll();
            for (ProviderEntry pref : mProviderList) {
                Drawable drawable = getDrawableForType(pref.type);
                boolean isExistedAccount = false;
                //String accountName = "";

                for (int i = 0; i < accounts.length; i++) {
                    if (pref.type.equals(accounts[i].type)) {
                        isExistedAccount = true;
                        //accountName = accounts[i].name;
                        break;
                    }
                }
                ProviderPreference mProviderPref = new ProviderPreference(this, pref.type,
                        drawable, pref.name, isExistedAccount);
                mAddAccountGroup.addPreference(mProviderPref);
            }
        } else {
            if (Log.isLoggable(TAG, Log.VERBOSE) && (mAuthorities != null)) {
                final StringBuilder auths = new StringBuilder();
                for (String a : mAuthorities) {
                    auths.append(a);
                    auths.append(' ');
                }
                Log.v(TAG, "No providers found for authorities: " + auths);
            }
            Log.d(TAG, "No providers found");
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    /**
     * Gets an icon associated with a particular account type. If none found, return null.
     * @param accountType the type of account
     * @return a drawable for the icon or null if one cannot be found.
     */
    protected Drawable getDrawableForType(final String accountType) {
        Drawable icon = null;
        if (mTypeToAuthDescription.containsKey(accountType)) {
            try {
                AuthenticatorDescription desc = mTypeToAuthDescription.get(accountType);
                Context authContext = createPackageContextAsUser(desc.packageName, 0, mUserHandle);
                icon = getPackageManager().getUserBadgedIcon(
                        authContext.getResources().getDrawable(desc.iconId), mUserHandle);
            } catch (PackageManager.NameNotFoundException e) {
                // TODO: place holder icon for missing account icons?
                Log.w(TAG, "No icon name for account type " + accountType);
            } catch (Resources.NotFoundException e) {
                // TODO: place holder icon for missing account icons?
                Log.w(TAG, "No icon resource for account type " + accountType);
            }
        }
        return icon;
    }

    /**
     * Gets the label associated with a particular account type. If none found, return null.
     * @param accountType the type of account
     * @return a CharSequence for the label or null if one cannot be found.
     */
    protected CharSequence getLabelForType(final String accountType) {
        CharSequence label = null;
        if (mTypeToAuthDescription.containsKey(accountType)) {
            try {
                AuthenticatorDescription desc = mTypeToAuthDescription.get(accountType);
                Context authContext = createPackageContextAsUser(desc.packageName, 0, mUserHandle);
                label = authContext.getResources().getText(desc.labelId);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "No label name for account type " + accountType);
            } catch (Resources.NotFoundException e) {
                Log.w(TAG, "No label resource for account type " + accountType);
            }
        }
        return label;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferences, Preference preference) {
        if (preference instanceof ProviderPreference) {
            ProviderPreference pref = (ProviderPreference)preference;
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Attempting to add account of type " + pref.getAccountType());
            }
            finishWithAccountType(pref.getAccountType());
        }
        return true;
    }

    private void finishWithAccountType(String accountType) {
        Intent intent = new Intent();
        intent.putExtra(AddAccountSettings.EXTRA_SELECTED_ACCOUNT, accountType);
        intent.putExtra(EXTRA_USER, mUserHandle);
        setResult(RESULT_OK, intent);
        finish();
    }

    public ArrayList<String> getAuthoritiesForAccountType(String type) {
        if (sAccountTypeToAuthorities == null) {
            sAccountTypeToAuthorities = Maps.newHashMap();
            SyncAdapterType[] syncAdapters = ContentResolver
                    .getSyncAdapterTypesAsUser(mUserHandle.getIdentifier());
            for (int i = 0, n = syncAdapters.length; i < n; i++) {
                final SyncAdapterType sa = syncAdapters[i];
                ArrayList<String> authorities = sAccountTypeToAuthorities
                        .get(sa.accountType);
                if (authorities == null) {
                    authorities = new ArrayList<String>();
                    sAccountTypeToAuthorities.put(sa.accountType, authorities);
                }
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.d(TAG, "added authority " + sa.authority
                            + " to accountType " + sa.accountType);
                }
                authorities.add(sa.authority);
            }
        }
        return sAccountTypeToAuthorities.get(type);
    }
}
