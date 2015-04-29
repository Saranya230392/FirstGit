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
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
import com.android.internal.util.CharSequences;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
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
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * Activity asking a user to select an account to be set up.
 */
public class ChooseAccountFragment extends AccountPreferenceBase {

    private static final String TAG = "ChooseAccountFragment";
    public String[] mAuthorities;
    public PreferenceGroup mAddAccountGroup;
    public final ArrayList<ProviderEntry> mProviderList = new ArrayList<ProviderEntry>();
    public HashSet<String> mAccountTypesFilter;
    public AuthenticatorDescription[] mAuthDescs;
    public Map<String, AuthenticatorDescription> mTypeToAuthDescription = new HashMap<String, AuthenticatorDescription>();
    public Account[] accounts;

    static class ProviderEntry implements Comparable<ProviderEntry> {
        private final CharSequence name;
        final String mType;

        ProviderEntry(CharSequence providerName, String accountType) {
            name = providerName;
            mType = accountType;
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
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        //        setContentView(R.layout.add_account_screen);
        addPreferencesFromResource(R.xml.add_account_settings);
        mAuthorities = getActivity().getIntent().getStringArrayExtra(
                AccountPreferenceBase.AUTHORITIES_FILTER_KEY);
        String[] accountTypesFilter = getActivity().getIntent().getStringArrayExtra(
                AccountPreferenceBase.ACCOUNT_TYPES_FILTER_KEY);
        if (accountTypesFilter != null) {
            mAccountTypesFilter = new HashSet<String>();
            for (String accountType : accountTypesFilter) {
                mAccountTypesFilter.add(accountType);
            }
        }
        mAddAccountGroup = getPreferenceScreen();
        accounts = AccountManager.get(getActivity()).getAccounts();
        updateAuthDescriptions();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_account_screen, null);

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {

            if (Utils.supportSplitView(getActivity())) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (!"VZW".equals(Config.getOperator())) {
                Log.i("hsmodel", "may be here");
                finishDependOnScenario();
                return true;
            } else {
                Log.i("hsmodel", "may be here1111");
                int settingStyle = Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(getActivity())) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        getActivity().onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                        Log.d("kimyow", "tabIndex=" + tabIndex);
                        settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                        startActivity(settings);
                        finishDependOnScenario();
                        return true;
                    }
                } else {
                    finishDependOnScenario();
                    return true;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void finishDependOnScenario() {
        finish();
    }

    /**
     * Updates provider icons. Subclasses should call this in onCreate()
     * and update any UI that depends on AuthenticatorDescriptions in onAuthDescriptionsUpdated().
     */
    @Override
    public void updateAuthDescriptions() {
        mAuthDescs = AccountManager.get(getActivity()).getAuthenticatorTypes();
        for (int i = 0; i < mAuthDescs.length; i++) {
            mTypeToAuthDescription.put(mAuthDescs[i].type, mAuthDescs[i]);
        }
        onAuthDescriptionsUpdated();
    }

    @Override
    protected void onAuthDescriptionsUpdated() {
        // Create list of providers to show on preference screen
        for (int i = 0; i < mAuthDescs.length; i++) {
            String accountType = mAuthDescs[i].type;
            CharSequence providerName = getLabelForType(accountType);

            if (!AccountsUtils.isVisibleAccount(accountType)) {
                continue;
            }
            // Skip preferences for authorities not specified. If no authorities specified,
            // then include them all.
            ArrayList<String> accountAuths = AccountsUtils
                    .getAuthoritiesForAccountType(accountType);
            boolean addAccountPref = true;
            if (mAuthorities != null && mAuthorities.length > 0 && accountAuths != null) {
                addAccountPref = false;
                for (int k = 0; k < mAuthorities.length; k++) {
                    if (accountAuths.contains(mAuthorities[k])) {
                        addAccountPref = true;
                        break;
                    }
                }
            }
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
            finishWithAccountType(mProviderList.get(0).mType);
        } else if (mProviderList.size() > 0) {
            Collections.sort(mProviderList);
            mAddAccountGroup.removeAll();
            for (ProviderEntry pref : mProviderList) {
                Drawable drawable = getDrawableForType(pref.mType);
                boolean isExistedAccount = false;
                //String accountName = "";

                for (int i = 0; i < accounts.length; i++) {
                    if (pref.mType.equals(accounts[i].type)) {
                        isExistedAccount = true;
                        //accountName = accounts[i].name;
                        break;
                    }
                }
                ProviderPreference mProviderPref = new ProviderPreference(getActivity(), pref.mType,
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
            getActivity().setResult(getActivity().RESULT_CANCELED);
            finishDependOnScenario();
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
                Context authContext = getActivity().createPackageContext(desc.packageName, 0);
                icon = authContext.getResources().getDrawable(desc.iconId);
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
                Context authContext = getActivity().createPackageContext(desc.packageName, 0);
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
        //        Intent intent = new Intent();
        //        intent.putExtra(AddAccountSettings.EXTRA_SELECTED_ACCOUNT, accountType);
        //        getActivity().setResult(getActivity().RESULT_OK, intent);

        Intent intent = new Intent("com.android.settings.account.Account_ADD");
        intent.putExtra(AddAccountSettings.EXTRA_SELECTED_ACCOUNT, accountType);
        getActivity().sendBroadcast(intent);
        finishDependOnScenario();
    }
}
