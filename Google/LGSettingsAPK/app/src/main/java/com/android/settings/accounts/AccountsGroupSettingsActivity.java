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

import static android.content.Intent.EXTRA_USER;
import static android.provider.Settings.EXTRA_AUTHORITIES;
import static android.os.UserManager.DISALLOW_MODIFY_ACCOUNTS;

import com.android.settings.AccountPreference;
import com.android.settings.ButtonBarHandler;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.Utils;
import com.android.settings.accounts.AccountSyncSettings;
import com.android.settings.accounts.AuthenticatorHelper;
import com.android.settings.accounts.ManageAccountsSettings;
import com.android.settings.lgesetting.Config.Config;

import android.preference.PreferenceCategory;
import android.accounts.Account;
import android.accounts.OnAccountsUpdateListener;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.Layout;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.whitepages.nameid.NameIDHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.SyncStatusObserver;
import android.content.ContentResolver;
import android.os.Handler;
import android.preference.CheckBoxPreference;

public class AccountsGroupSettingsActivity extends AccountPreferenceBase implements
        OnAccountsUpdateListener, OnPreferenceClickListener, Indexable {

    private static final String TAG = "AccountsGroupSettings";
    private AuthenticatorHelper mAuthenticatorHelper;
    private boolean mListeningToAccountUpdates;
    private static final int REQUEST_SHOW_SYNC_SETTINGS = 1;
    private static final int MENU_ADD_ACCOUNT = Menu.FIRST;
    private String[] mAuthorities;
    protected static final String LG_ACCOUNTS = "com.lge.";
    protected static final String LG_ACCOUNTS_FOR_WIDGET_TYPE = "com.lge.android.";
    //private String KEY_LG_ACCOUNT = "LGAccountGroup";
    private String KEY_BUA_ACCOUNT = "BUAAccountGroup";
    //private AccountPreference accountLG;
    private AccountPreference accountBUA;
    private boolean isMyPhoneBook = false;
    private CheckBoxPreference autoSyncData;
    private PreferenceCategory providerAccounts;
    private static final String KEY_AUTO_SYNC_DATA = "auto_sync_checkbox";
    private String KEY_PROVIDER_ACCOUNT = "providerAccount";

    private AccountPreference mInlineCommand;
    private final int mSetOrderNumberOne = 1;

    private int mStartPoint = 0;
    private Context mContext;
    private UserHandle mCurrentProfile;
    private UserManager mUm;

    //[Account&Sync][AndroidForWork]
    private Button mPreviewButton;
    private LinearLayout mLayout;
    private PreferenceCategory mProviderAccountWork;
    private String KEY_PROVIDER_WORK_ACCOUNT = "providerAccountWork";
    private static final int ORDER_LAST = 1001;
    private static final int ORDER_NEXT_TO_LAST = 1000;
    private boolean mIsManageProfile;
    private static final String TAG_CONFIRM_AUTO_SYNC_CHANGE = "confirmAutoSyncChange";

    private SparseArray<ProfileData> mProfiles = new SparseArray<ProfileData>();
    private Preference mProfileNotAvailablePreference;
    private int mAuthoritiesCount = 0;

    // UX 4.2 Apply List
    private PreferenceCategory mEmptyCategory;

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;
    private final static String CONTENT_URI = "content://com.android.settings.search.SearchDBupdateProvider/prefs_index";

    /**
     * Holds data related to the accounts belonging to one profile.
     */
    private static class ProfileData {
        /**
         * The preference that displays the accounts.
         */
        public PreferenceGroup preferenceGroup;
        /**
         * The preference that displays the add account button.
         */
        public Preference addAccountPreference;
        /**
         * The preference that displays the button to remove the managed profile
         */
        public Preference removeWorkProfilePreference;
        /**
         * The {@link AuthenticatorHelper} that holds accounts data for this profile.
         */
        public AuthenticatorHelper authenticatorHelper;
        /**
         * The {@link UserInfo} of the profile.
         */
        public UserInfo userInfo;
    }

    //[Account&Sync][AndroidForWork]
    private Preference newAddAccountPreference(Context context) {
        Preference preference = new Preference(context);
        preference.setTitle(R.string.add_account_label);
        preference.setIcon(R.drawable.ic_add);
        preference.setOnPreferenceClickListener(this);
        preference.setOrder(ORDER_NEXT_TO_LAST);

        return preference;
    }

    //[Account&Sync][AndroidForWork]
    private Preference newRemoveWorkProfilePreference(Context context) {
        Preference preference = new Preference(context);
        preference.setTitle(R.string.remove_managed_profile_label);
        preference.setIcon(R.drawable.ic_menu_delete_all);
        preference.setOnPreferenceClickListener(this);
        preference.setOrder(ORDER_LAST);
        return preference;
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUm = (UserManager)getSystemService(Context.USER_SERVICE);
        if (Utils.isUI_4_1_model(getActivity())) {
            mStartPoint = 100;
        }
        //JW InlineCommand
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mInlineCommand = new AccountPreference(getActivity(), null,
                    getResources().getDrawable(R.drawable.ic_menu_add2),
                    null, true, true, "",
                    getResources().getString(R.string.add_account_label), mStartPoint);
            mInlineCommand.setOrder(mSetOrderNumberOne);
        }

        mAuthorities = getActivity().getIntent().getStringArrayExtra(AUTHORITIES_FILTER_KEY);
        if (mAuthorities != null) {
            mAuthoritiesCount = mAuthorities.length;
        }

        addPreferencesFromResource(R.xml.account_group_settings);
        //accountLG = (AccountPreference)findPreference(KEY_LG_ACCOUNT);
        //accountBUA = (AccountPreference)findPreference(KEY_BUA_ACCOUNT);

        //accountLG = new AccountPreference(getActivity(), null, getResources().getDrawable(R.drawable.lg_apps_account), null, true, true, "", getResources().getString(R.string.lg_apps_name), 0 );
        //accountLG.setKey(KEY_LG_ACCOUNT);
        //accountLG.setOrder(1);
        accountBUA = new AccountPreference(getActivity(), null, getResources().getDrawable(
                R.drawable.ic_list_bua_plus), null, true, true, "", getResources().getString(
                R.string.sp_preference_bua_plus_title_NORMAL), 0);
        accountBUA.setKey(KEY_BUA_ACCOUNT);
        accountBUA.setOrder(0);
        // init account category

        if (Config.VZW.equals(Config.getOperator())) {
            if (Utils.supportSplitView(getActivity()) == false) {
                    getActivity().getActionBar().setTitle(R.string.account_settings);
            }
        }

        autoSyncData = (CheckBoxPreference)findPreference(KEY_AUTO_SYNC_DATA);
        providerAccounts = (PreferenceCategory)findPreference(KEY_PROVIDER_ACCOUNT);
        mProviderAccountWork = (PreferenceCategory)findPreference(KEY_PROVIDER_WORK_ACCOUNT);
        setHasOptionsMenu(true);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-52][ID-MDM-280]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            android.content.IntentFilter filterLGMDM = new android.content.IntentFilter();
            com.android.settings.MDMSettingsAdapter.getInstance()
                    .addManualSyncPolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
        //[2013_08_14]JW A_Tablet
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.settings.account.Account_ADD");
        filter.addAction("com.android.sync.SYNC_CONN_STATUS_CHANGED");
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED);
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_ADDED);
        getActivity().registerReceiver(mAccountReceiver, filter);

        mIsFirst = true;
    }

    //[2013_07_30]_jw
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_add_button, null);
        mLayout = (LinearLayout)view.findViewById(R.id.add_button_layout);
        mPreviewButton = (Button)view.findViewById(R.id.preview_button);
        mPreviewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("hsmodel", "OnCreateView OnClick");
                Log.d("JW", "onAddAccountClicked");

                onAddAccountClicked();

            }
        });
        //JW InlineCommand
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mLayout.setVisibility(View.GONE);
        }
        return view;
    }

    public void onAddAccountFragment() {

        Bundle bundle = new Bundle();
        bundle.putStringArray(AUTHORITIES_FILTER_KEY, mAuthorities);
        startFragment(this, ChooseAccountFragment.class.getCanonicalName(),
                CHOOSE_ACCOUNT_REQUEST, bundle, R.string.add_account_label);

    }

    //[2013_07_30]_jw
    @Override
    public void onResume() {

        super.onResume();
        if (Utils.isManagedProfile(mUm)) {
            // This should not happen
            Log.e(TAG, "We should not be showing settings for a managed profile");
            finish();
            return;
        }

        mCurrentProfile = Process.myUserHandle();
        mAuthenticatorHelper = new AuthenticatorHelper(mContext, mCurrentProfile, mUm);
        mAuthenticatorHelper.updateAuthDescriptions(getActivity());
        mAuthenticatorHelper.onAccountsUpdated(getActivity(), null);
        if (!mListeningToAccountUpdates) {
            AccountManager.get(getActivity()).addOnAccountsUpdatedListener(this, null, true);
            mListeningToAccountUpdates = true;
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-280]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM && autoSyncData != null) {
            com.android.settings.MDMSettingsAdapter.getInstance().setAutoMasterSynceEnableMenu(
                    null, autoSyncData);
        }
        // LGMDM_END
        if (autoSyncData != null) {
            autoSyncData.setChecked(ContentResolver
                    .getMasterSyncAutomaticallyAsUser(mCurrentProfile.getIdentifier()));
        }
        getActivity().getContentResolver().addStatusChangeListener(
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE
                        | ContentResolver.SYNC_OBSERVER_TYPE_STATUS
                        | ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS, mSyncStatusObserver);
        //providerAccounts.addPreference(accountLG);
        providerAccounts.addPreference(accountBUA);
        makeAccountsGroup();
        // init account category

        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent()
                .getBooleanExtra("perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            boolean newValue =  getActivity().getIntent().getBooleanExtra("newValue", false);
            startResult(newValue);
            mIsFirst = false;
        }
    }

    private void goMainScreen() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setClassName("com.android.settings",
                "com.android.settings.Settings$AccountsGroupSettingsActivity");
        startActivity(i);
        getActivity().finish();
    }

    private void startResult(boolean newValue) {
        if (Utils.supportSplitView(getActivity())) {
            goMainScreen();
            return;
        }

        goMainScreen();
    }

    private void searchDBUpdate(boolean newValue, String key, String field) {
        Log.d(TAG, "searchDBUpdate newValue : " + newValue + " key : " + key);
        ContentResolver cr = getContentResolver();
        ContentValues row = new ContentValues();
        row.put(field, newValue ? 1 : 0);
        String where = "data_key_reference = ? AND class_name = ?";
        Log.d(TAG, "searchDBUpdate where : " + where);
        cr.update(Uri.parse(CONTENT_URI), row, where, new String[] { key,
            "com.android.settings.accounts.AccountsGroupSettingsActivity" });
    }

    /*
    public void makeLGAppCategory() {
        accountLG = (AccountPreference)findPreference(KEY_LG_ACCOUNT);
    }
*/
    public void makeBUACategory() {
        accountBUA = (AccountPreference)findPreference(KEY_BUA_ACCOUNT);
        if (!cloudEnable()) {
            getPreferenceScreen().removePreference(accountBUA);
        } else {
            getPreferenceScreen().addPreference(accountBUA);
        }
    }

    private void refreshAccountGroup() {
        getPreferenceScreen().removeAll();
        if (providerAccounts != null && providerAccounts.getPreferenceCount() > 0) {
            providerAccounts.removeAll();
        }

        if (autoSyncData != null) {
            getPreferenceScreen().addPreference(autoSyncData);
        }
        if (providerAccounts != null) {
            getPreferenceScreen().addPreference(providerAccounts);
            providerAccounts.addPreference(accountBUA);
        }

        if (mProviderAccountWork != null && mProviderAccountWork.getPreferenceCount() > 0) {
            mProviderAccountWork.removeAll();
        }

        if (mProviderAccountWork != null) {
            getPreferenceScreen().addPreference(mProviderAccountWork);
        }
    }

    private void makeAccountsGroup() {

        // 1. provider list, not all accounts
        // 2. The accounts of LG apps should be wrapped to the 'LG app' category.
        // 3. There are some accounts which should be invisible.
        // 4. If there is no accounts related to LG apps, it should be removed.

        refreshAccountGroup();

        //[Account&Sync][AndroidForWork] Check Whether Support Work Profile
        mIsManageProfile = false;
        List<UserInfo> profiles = mUm.getProfiles(UserHandle.myUserId());
        final int profilesCount = profiles.size();

        for (int i = 0; i < profilesCount; i++) {

            if (profiles.get(i).isManagedProfile()) {
                makeWorkProfile();
                mIsManageProfile = true;
            }
        }

        //[Account&Sync][AndroidForWork] If WorkProfile were supported , UI invalidated
        if (!mIsManageProfile) {
            mProviderAccountWork = (PreferenceCategory)findPreference(KEY_PROVIDER_WORK_ACCOUNT);
            removePreference(KEY_PROVIDER_WORK_ACCOUNT);

            mLayout.setVisibility(View.VISIBLE);
            mPreviewButton.setVisibility(View.VISIBLE);

            providerAccounts.setTitle(Utils.getResources().getString(
                    R.string.account_settings));
            setHasOptionsMenu(false);

        }

        String[] accountTypes = mAuthenticatorHelper.getEnabledAccountTypes();

        //boolean isLGApps = false;
        boolean isBAAccount = false;

        for (String accountType : accountTypes) {
            Log.i("hsmodel", "accountType : " + accountType);
            if (accountType.equals("com.fusionone.account")) {
                isBAAccount = true;
            }
            CharSequence label = mAuthenticatorHelper.getLabelForType(getActivity(), accountType);
            if (label == null) {
                continue;
            }

            Account[] accounts;
            if (getActivity() != null) {
                accounts = AccountManager.get(getActivity()).getAccountsByTypeAsUser(accountType,
                        mCurrentProfile);

            } else {
                accounts = AccountManager.get(mContext).getAccountsByTypeAsUser(accountType,
                        mCurrentProfile);
            }
            boolean skipToAccount = accounts.length == 1
                    && !mAuthenticatorHelper.hasAccountPreferences(accountType);

            Drawable icon = mAuthenticatorHelper.getDrawableForType(getActivity(), accountType);

            //if( accountType.startsWith(LG_ACCOUNTS_FOR_WIDGET_TYPE) || (accountType.startsWith(LG_ACCOUNTS) && invisibleLGAccount(accountType)) ) {
            //    isLGApps = true;
            //} else {
            // visible or invisible accounts
            if (!isVisibleAccounts(accountType)) {
                continue;
            }
            AccountPreference preference = null;
            if (skipToAccount) {
                preference = new AccountPreference(getActivity(), accounts[0], icon, null, true,
                        skipToAccount, accountType, label.toString(), 0);
            } else {
                preference = new AccountPreference(getActivity(), null, icon, null, true,
                        skipToAccount, accountType, label.toString(), 0);
            }
            //AccountPreference preference = new AccountPreference(getActivity(), null, icon, null, true, skipToAccount, accountType, label.toString() );

            //[S][changyu0218.lee] Change AAB title & summary
            checkAndSetAABAccount(preference);
            //[E][changyu0218.lee] Change AAB title & summary
            checkAndSetMyPhoneBookAccount(preference);
            preference.setOrder(1);
            providerAccounts.addPreference(preference);
            //JW InlineCommand
            if (SettingsBreadCrumb.isAttached(getActivity())) {
                preference.setOrder(0);
            }
            //}
        }

        //JW InlineCommand
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            if (mInlineCommand == null) {
                mInlineCommand = new AccountPreference(getActivity(), null,
                        getResources().getDrawable(R.drawable.ic_menu_add2),
                        null, true, true, "",
                        getResources().getString(R.string.add_account_label), 0);
                mInlineCommand.setOrder(mSetOrderNumberOne);
            }
            providerAccounts.addPreference(mInlineCommand);
        }
        //if( !isLGApps && accountLG != null) {
        //    providerAccounts.removePreference(accountLG);
        //}
        if (!isBAAccount && !cloudEnable()) {
            providerAccounts.removePreference(accountBUA);
        }

        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            int mCount = providerAccounts.getPreferenceCount();

            if (mCount == 0 && !mIsManageProfile) {
                mEmptyCategory = new PreferenceCategory(getActivity());
                mEmptyCategory.setLayoutResource(R.layout.account_empty_layout);
                getPreferenceScreen().addPreference(mEmptyCategory);
            }
        }
        if (!mListeningToAccountUpdates) {
            AccountManager.get(getActivity()).addOnAccountsUpdatedListener(this, null, true);
            mListeningToAccountUpdates = true;
        }
    }

    //[Account&Sync][AndroidForWork] MakeWorkProfile
    private void makeWorkProfile() {

        List<UserInfo> profiles = mUm.getProfiles(UserHandle.myUserId());
        final int profilesCount = profiles.size();

        for (int i = 0; i < profilesCount; i++) {

            final ProfileData profileData = new ProfileData();
            profileData.userInfo = profiles.get(i);
            profileData.addAccountPreference = newAddAccountPreference(mContext);
            profileData.authenticatorHelper = new AuthenticatorHelper(mContext, profiles.get(i)
                    .getUserHandle(), mUm);
            profileData.authenticatorHelper.updateAuthDescriptions(getActivity());
            profileData.authenticatorHelper.onAccountsUpdated(getActivity(), null);

            if (profiles.get(i).isManagedProfile()) {

                profileData.preferenceGroup = mProviderAccountWork;
                getPreferenceScreen().removePreference(autoSyncData);
                mPreviewButton.setVisibility(View.GONE);
                mLayout.setVisibility(View.GONE);

                profileData.removeWorkProfilePreference = newRemoveWorkProfilePreference(mContext);
                providerAccounts.setTitle(getString(R.string.category_personal));
                if (!mUm.hasUserRestriction(DISALLOW_MODIFY_ACCOUNTS, profileData.userInfo.getUserHandle())) {
                    mProviderAccountWork.addPreference(profileData.addAccountPreference);
                }

                mProviderAccountWork.addPreference(profileData.removeWorkProfilePreference);
                updateAccountTypes(profileData);

            } else {
                profileData.preferenceGroup = providerAccounts;
                if (!mUm.hasUserRestriction(DISALLOW_MODIFY_ACCOUNTS, profileData.userInfo.getUserHandle())) {
                    providerAccounts.addPreference(profileData.addAccountPreference);
                }

            }
            mProfiles.put(profiles.get(i).id, profileData);
        }
    }

    private void updateAccountTypes(ProfileData profileData) {
        profileData.preferenceGroup.removeAll();
        if (profileData.userInfo.isEnabled()) {
            final ArrayList<AccountPreference> preferences = getAccountTypePreferences(
                    profileData.authenticatorHelper, profileData.userInfo.getUserHandle());
            final int count = preferences.size();
            for (int i = 0; i < count; i++) {
                profileData.preferenceGroup.addPreference(preferences.get(i));
            }
            if (profileData.addAccountPreference != null) {
                profileData.preferenceGroup.addPreference(profileData.addAccountPreference);
            }
        } else {
            // Put a label instead of the accounts list
            mProfileNotAvailablePreference.setEnabled(false);
            mProfileNotAvailablePreference.setIcon(R.drawable.empty_icon);
            mProfileNotAvailablePreference.setTitle(null);
            mProfileNotAvailablePreference.setSummary(
                    R.string.managed_profile_not_available_label);
            profileData.preferenceGroup.addPreference(mProfileNotAvailablePreference);
        }
        if (profileData.removeWorkProfilePreference != null) {
            profileData.preferenceGroup.addPreference(profileData.removeWorkProfilePreference);
        }
    }

    private ArrayList<AccountPreference> getAccountTypePreferences(AuthenticatorHelper helper,
            UserHandle userHandle) {
        final String[] accountTypes = helper.getEnabledAccountTypes();
        final ArrayList<AccountPreference> accountTypePreferences =
                new ArrayList<AccountPreference>(accountTypes.length);
        Log.d("jw", "accountTypes : " + accountTypes.length);
        for (int i = 0; i < accountTypes.length; i++) {
            final String accountType = accountTypes[i];

            if (!isVisibleAccounts(accountType)) {
                continue;
            }
            // Skip showing any account that does not have any of the requested authorities
            if (!accountTypeHasAnyRequestedAuthorities(helper, accountType)) {
                continue;
            }
            final CharSequence label = helper.getLabelForType(getActivity(), accountType);
            if (label == null) {
                continue;
            }
            Drawable icon = helper.getDrawableForType(getActivity(), accountType);
            final Account[] accounts = AccountManager.get(getActivity())
                    .getAccountsByTypeAsUser(accountType, userHandle);
            final boolean skipToAccount = accounts.length == 1
                    && !helper.hasAccountPreferences(accountType);

            AccountPreference preference = null;
            if (skipToAccount) {
                preference = new AccountPreference(getActivity(), accounts[0], icon, null, true,
                        skipToAccount, accountType, label.toString(), 0);
                accountTypePreferences.add(preference);
            } else {
                preference = new AccountPreference(getActivity(), null, icon, null, true,
                        skipToAccount, accountType, label.toString(), 0);
                accountTypePreferences.add(preference);
            }
            helper.preloadDrawableForType(getActivity(), accountType);
        }
        return accountTypePreferences;
    }

    private boolean accountTypeHasAnyRequestedAuthorities(AuthenticatorHelper helper,
            String accountType) {
        if (mAuthoritiesCount == 0) {
            // No authorities required
            return true;
        }
        final ArrayList<String> authoritiesForType = helper.getAuthoritiesForAccountType(
                accountType);
        if (authoritiesForType == null) {
            Log.d(TAG, "No sync authorities for account type: " + accountType);
            return false;
        }
        for (int j = 0; j < mAuthoritiesCount; j++) {
            if (authoritiesForType.contains(mAuthorities[j])) {
                return true;
            }
        }
        return false;
    }

    protected static boolean invisibleLGAccount(String accountType) {

        if (accountType.equals("com.lge.myphonebook")) {
            return false;
        }
        if (accountType.equals("com.lge.sync")) {
            return false;
        }
        if (accountType.equals("com.mobileleader.sync")) {
            return false;
        }
        if (accountType.equals("com.lge.email.account")) {
            return false;
        }
        return true;
    }

    //[Account&Sync][AndroidForWork] Create OverFlow Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        //[2013_08_13] JW Remove Menu Item -> Add Button
        /*
        MenuItem addAccountItem = null;
        addAccountItem = menu.add(0, MENU_ADD_ACCOUNT, 0, R.string.add_account_label);
        addAccountItem.setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
        */
        inflater.inflate(R.menu.account_settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final UserHandle currentProfile = Process.myUserHandle();
        if (mProfiles.size() == 1) {
            menu.findItem(R.id.account_settings_menu_auto_sync)
                    .setVisible(true)
                    .setOnMenuItemClickListener(new MasterSyncStateClickListener(currentProfile))
                    .setChecked(ContentResolver.getMasterSyncAutomaticallyAsUser(
                            currentProfile.getIdentifier()));
            menu.findItem(R.id.account_settings_menu_auto_sync_personal).setVisible(false);
            menu.findItem(R.id.account_settings_menu_auto_sync_work).setVisible(false);
        } else if (mProfiles.size() > 1) {
            // We assume there's only one managed profile, otherwise UI needs to change
            final UserHandle managedProfile = mProfiles.valueAt(1).userInfo.getUserHandle();

            menu.findItem(R.id.account_settings_menu_auto_sync_personal)
                    .setVisible(true)
                    .setOnMenuItemClickListener(new MasterSyncStateClickListener(currentProfile))
                    .setChecked(ContentResolver.getMasterSyncAutomaticallyAsUser(
                            currentProfile.getIdentifier()));
            menu.findItem(R.id.account_settings_menu_auto_sync_work)
                    .setVisible(true)
                    .setOnMenuItemClickListener(new MasterSyncStateClickListener(managedProfile))
                    .setChecked(ContentResolver.getMasterSyncAutomaticallyAsUser(
                            managedProfile.getIdentifier()));
            menu.findItem(R.id.account_settings_menu_auto_sync).setVisible(false);
        } else {
            Log.w(TAG, "Method onPrepareOptionsMenu called before mProfiles was initialized");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == MENU_ADD_ACCOUNT) {
            onAddAccountClicked();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void onAddAccountClicked() {
        Intent intent = new Intent("android.settings.ADD_ACCOUNT_SETTINGS");
        intent.putExtra(AUTHORITIES_FILTER_KEY, mAuthorities);
        startActivity(intent);
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        Log.i("hsmodel", "account group : onAccountsUpdated");
        try {
            mAuthenticatorHelper.onAccountsUpdated(getActivity(), accounts);
            makeAccountsGroup();
        } catch (Exception e) {
            Log.e(TAG, "Exception Msg: " + e.getMessage());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferences, Preference preference) {
        if (preference == mInlineCommand) {
            onAddAccountFragment();
        } else if (preference instanceof AccountPreference) {
            String keyForAccount = ((AccountPreference)preference).getKey();
            //if( keyForAccount != null && keyForAccount.equals(KEY_LG_ACCOUNT)  ) {
            //    startLGAccountSettings( preference, true );
            //} else
            if (keyForAccount != null && keyForAccount.equals(KEY_BUA_ACCOUNT)) {
                if (!cloudEnable()) {
                    Log.i("hsmodel", "no cloudEnable");
                    Intent intent = new Intent("com.lge.vzw.bua.intent.action.SUBSCRIBE");
                    startActivity(intent);
                } else {
                    Log.i("hsmodel", "yes cloudEnable");
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(new ComponentName("com.lge.cloudvmm",
                            "com.lge.cloudvmm.ApplicationGateActivity"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } else {
                startAccountSettings((AccountPreference)preference,
                        ((AccountPreference)preference).getIsSkip());
            }
        } else if (preference == autoSyncData) {
            ConfirmAutoSyncChangeFragment.show(AccountsGroupSettingsActivity.this,
                        autoSyncData.isChecked(), mCurrentProfile);
        } else {
            return false;
        }
        return true;
    }

    private final Handler mHandler = new Handler();
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        public void onStatusChanged(int which) {
            mHandler.post(new Runnable() {
                public void run() {
                    if (autoSyncData != null) {
                        Log.i("hsmodel", "mAccountSyncPreference : " + ContentResolver.
                                getMasterSyncAutomaticallyAsUser(mCurrentProfile.getIdentifier()));
                        autoSyncData.setChecked(ContentResolver
                                .getMasterSyncAutomaticallyAsUser(mCurrentProfile.getIdentifier()));

                    }
                }
            });
        }
    };

    private void startLGAccountSettings(Preference acctPref, boolean isSkip) {
        Bundle args = new Bundle();
        args.putString(ManageAccountsSettings.KEY_ACCOUNT_TYPE, LG_ACCOUNTS);
        args.putString(ManageAccountsSettings.KEY_ACCOUNT_LABEL, getActivity().getResources()
                .getString(R.string.lg_apps_name));
        ((PreferenceActivity)getActivity()).startPreferencePanel(
                ManageAccountsSettings.class.getCanonicalName(), args,
                R.string.account_sync_settings_title, null,
                this, REQUEST_SHOW_SYNC_SETTINGS);
    }

    private void startAccountSettings(AccountPreference acctPref, boolean isSkip) {

        if (acctPref.getAccountType().equals("com.att.aab")) {
            Intent intent = new Intent("com.lge.aab.action.STARTLGAAB");
            getActivity().sendBroadcast(intent);

        } else if (acctPref.getAccountType().equals("com.lge.myphonebook")) {
            Intent intent = new Intent("com.lge.myphonebook.DTAG_MYPHONEBOOK_SETTINGS");
            startActivity(intent);
        } else if (isSkip) {
            Bundle args = new Bundle();
            args.putString(ManageAccountsSettings.KEY_ACCOUNT_TYPE, acctPref.getAccountType());
            args.putParcelable(AccountSyncSettings.ACCOUNT_KEY, acctPref.getAccount());
            ((PreferenceActivity)getActivity()).startPreferencePanel(
                    AccountSyncSettings.class.getCanonicalName(), args,
                    R.string.sp_power_saver_select_sync_NORMAL, null,
                    this, REQUEST_SHOW_SYNC_SETTINGS);
        } else {
            Bundle args = new Bundle();
            args.putString(ManageAccountsSettings.KEY_ACCOUNT_TYPE, acctPref.getAccountType());
            args.putString(ManageAccountsSettings.KEY_ACCOUNT_LABEL, mAuthenticatorHelper
                    .getLabelForType(getActivity(), acctPref.getAccountType()).toString());
            //args.putParcelable(ManageAccountsSettings.KEY_ACCOUNT_TYPE, accountType);
            ((PreferenceActivity)getActivity()).startPreferencePanel(
                    ManageAccountsSettings.class.getCanonicalName(), args,
                    R.string.sp_power_saver_select_sync_NORMAL, null,
                    this, REQUEST_SHOW_SYNC_SETTINGS);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListeningToAccountUpdates) {
            AccountManager.get(getActivity()).removeOnAccountsUpdatedListener(this);
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-52][ID-MDM-280]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                android.util.Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        //[2013_08_14]JW A_Tablet
        if (mAccountReceiver != null) {
            try {
                getActivity().unregisterReceiver(mAccountReceiver);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Exception Msg: " + e.getMessage());
            }
        }

    }

    // Check accounts to make visible or invisible
    private boolean isVisibleAccounts(String accountType) {
        // TODO Auto-generated method stub

        if (accountType.equals("com.mobileleader.sync")) {
            return false;
        }
        if (accountType.equals("com.lge.sync")) {
            return false;
        }
        if (accountType.equals("com.att.aab") && !Config.getOperator().equals(Config.ATT)) {
            return false;
        }
        if (accountType.equals("com.fusionone.account")) {
            return false;
        }
        if (accountType.equals("com.lge.myphonebook") && showMyPhoneBookAccount()) {
            isMyPhoneBook = true;
        } else if (accountType.equals("com.lge.myphonebook") && !showMyPhoneBookAccount()) {
            isMyPhoneBook = false;
            return false;
        }
        if (accountType.equals("com.lge.vmemo")) {
            return false;
        }
        if (accountType.equals("com.lge.android.weather.sync")) {
            return false;
        }
        if (accountType.equals("com.verizon.phone")) {
            return false;
        }
        if (accountType.equals("com.android.sim")) {
            return false;
        }
        if (accountType.equals("com.verizon.tablet")) {
            return false;
        }
        if (accountType.equals("com.lge.lifetracker")) {
            return false;
        }
        if (accountType.equals("com.lge.bioitplatform.sdservice")) {
            return false;
        }
        if (accountType.equals(NameIDHelper.ACCOUNT_TYPE)) {
            return false;
        }
        return true;
    }

    // [BUA+] Check whether BUA+ is enabled or not
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

    // [DTAG] get DTAG account name from DB
    private String getMyPhonebookName(Context context) {
        String str = null;
        str = Settings.System.getString(context.getContentResolver(), "myphonebook_FN");
        if (str == null) {
            str = "MyPhonebook";
        }
        return str;
    }

    // [DTAG] Set the title and summary of DTAG account
    private void checkAndSetMyPhoneBookAccount(AccountPreference accountPref) {
        if (accountPref.getAccount() != null
                && accountPref.getAccount().type.equals("com.lge.myphonebook") && isMyPhoneBook) {
            accountPref.setTitle(getMyPhonebookName(getActivity()));
            setSummary(accountPref.getAccount(), accountPref);
        }
    }

    // [DTAG] Set the title and summary of DTAG account
    private void setSummary(final Account account,
            final AccountPreference preference) {
        String providerName = "";
        try {
            providerName = mAuthenticatorHelper.getLabelForType(getActivity(), account.type)
                    .toString();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("hsmodel", account.type + ", " + providerName);
        preference.setSummary(providerName);
    }

    // [BUA+] Check whether DTAG account is enabled or not
    private boolean showMyPhoneBookAccount() {
        boolean bShowAccount = false;
        int iRet = 0;
        final String AUTHORITY = "com.lge.myphonebook.db.provider.MyPhonebookProvider";
        final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
        final Uri mCONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "show_account");
        Cursor cursor = getContentResolver().query(mCONTENT_URI, null, null, null, null);
        if (cursor != null) {
            try {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    iRet = cursor.getInt(0);
                }
                if (iRet != 0) {
                    bShowAccount = true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception Msg: " + e.getMessage());
            } finally {
                cursor.close();
            }
        }
        return bShowAccount;
    }

    //[S][changyu0218.lee] Change AAB title & summary
    private void checkAndSetAABAccount(AccountPreference accountPref) {

        if (Config.getOperator().equals(Config.ATT) && accountPref.getAccount() != null
                && accountPref.getAccount().type.equals("com.att.aab")) {
            accountPref.setTitle(accountPref.getAccount().name);
            setSummary(accountPref.getAccount(), accountPref);
            //accountPref.setSyncStatus(AccountPreference.SYNC_AAB);
            return;
        }
        return;
    }

    //[E][changyu0218.lee] Change AAB title & summary

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-52][ID-MDM-280]
    private final android.content.BroadcastReceiver mLGMDMReceiver = new android.content.BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.android.settings.MDMSettingsAdapter.getInstance()
                        .receiveManualSyncPolicyChangeIntent(intent)) {
                    getActivity().finish();
                }
            }
        }
    };
    // LGMDM_END
    //[2013_08_14] JW A_Tablet SplitView Start
    //private static final String KEY_ADD_CALLED = "AddAccountCalled";

    /**
     * Extra parameter to identify the caller. Applications may display a
     * different UI if the calls is made from Settings or from a specific
     * application.
     */
    private static final String KEY_CALLER_IDENTITY = "pendingIntent";
    private static final String SHOULD_NOT_RESOLVE = "SHOULDN'T RESOLVE!";
    private static final String TAG2 = "JW";
    /* package */static final String EXTRA_SELECTED_ACCOUNT = "selected_account";

    // show additional info regarding the use of a device with multiple users
    static final String EXTRA_HAS_MULTIPLE_USERS = "hasMultipleUsers";
    public static final String ACCOUNT_ADD_ACTION = "com.android.settings.account.Account_ADD";
    private static final int CHOOSE_ACCOUNT_REQUEST = 1;
    private static final int ADD_ACCOUNT_REQUEST = 2;
    public static final boolean ADD_ACCOUNT_FINISH = false;
    private PendingIntent mPendingIntent;
    //private boolean mIsTriedLogin = false;
    //private boolean mIsFinishedNormally = false;
    public static boolean sIsNullActivity = false;
    public static final String EMAIL_FINISH = "com.lge.email";
    //private boolean mAddAccountCalled = false;

    private AccountManagerCallback<Bundle> mCallback = new AccountManagerCallback<Bundle>() {
        public void run(AccountManagerFuture<Bundle> future) {
            boolean done = true;
            try {
                Log.d(TAG2, "-------------AccountManagerCallback-------------");
                Bundle bundle = future.getResult();
                //bundle.keySet();
                Intent intent = (Intent)bundle.get(AccountManager.KEY_INTENT);
                if (intent != null) {
                    done = false;
                    Bundle addAccountOptions = new Bundle();
                    if (mPendingIntent == null) {
                        Log.d(TAG2, "mPendingIntent--------NULL-----");
                    } else {
                        Log.d(TAG2, "mPendingIntent--------Not NULL-----");
                    }
                    addAccountOptions.putParcelable(KEY_CALLER_IDENTITY, mPendingIntent);
                    addAccountOptions.putBoolean(EXTRA_HAS_MULTIPLE_USERS,
                            Utils.hasMultipleUsers(getActivity()));
                    intent.putExtras(addAccountOptions);
                    startActivityForResult(intent, ADD_ACCOUNT_REQUEST);
                    //                    startActivity(intent);

                    if (mPendingIntent != null) {
                        mPendingIntent.cancel();
                        mPendingIntent = null;
                    }

                } else {
                    Log.d(TAG2, "AccountManagerCallback");
                    getActivity();
                    getActivity().setResult(Activity.RESULT_OK);
                    if (mPendingIntent != null) {
                        mPendingIntent.cancel();
                        mPendingIntent = null;
                    }
                }

                if (Log.isLoggable(TAG, Log.VERBOSE)) {

                    Log.v(TAG, "account added: " + bundle);
                }

            } catch (OperationCanceledException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {

                    Log.v(TAG, "addAccount was canceled");
                }
            } catch (IOException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {

                    Log.v(TAG, "addAccount failed: " + e);
                }
            } catch (AuthenticatorException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {

                    Log.v(TAG, "addAccount failed: " + e);
                }
            } finally {
                if (done) {
                    //JW InlineCommand
                    //                    getActivity().finish();
                    //mIsFinishedNormally = true;
                    Log.i("hsmodel", "call back finished");
                }
            }
        }
    };

    private void addAccount(String accountType) {
        Log.d(TAG2, "addAccount");

        /* The identityIntent is for the purposes of establishing the identity
        * of the caller and isn't intended for launching activities, services
        * or broadcasts.
        *
        * Unfortunately for legacy reasons we still need to support this. But
        * we can cripple the intent so that 3rd party authenticators can't
        * fill in addressing information and launch arbitrary actions.
        */
        Intent identityIntent = new Intent();
        identityIntent.setComponent(new ComponentName(SHOULD_NOT_RESOLVE, SHOULD_NOT_RESOLVE));
        identityIntent.setAction(SHOULD_NOT_RESOLVE);
        identityIntent.addCategory(SHOULD_NOT_RESOLVE);

        //mIsTriedLogin = true;
        Bundle addAccountOptions = new Bundle();
        mPendingIntent = PendingIntent.getBroadcast(getActivity(),
                0, identityIntent, 0);
        addAccountOptions.putParcelable(KEY_CALLER_IDENTITY,
                mPendingIntent);
        addAccountOptions.putBoolean(EXTRA_HAS_MULTIPLE_USERS,
                Utils.hasMultipleUsers(getActivity()));
        AccountManager.get(getActivity()).addAccount(
                accountType,
                null, /* authTokenType */
                null, /* requiredFeatures */
                addAccountOptions,
                null,
                mCallback,
                null /* handler */);
        //mAddAccountCalled = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG2, "------onActivityResult-----");
        switch (requestCode) {
        case CHOOSE_ACCOUNT_REQUEST:
            if (resultCode == getActivity().RESULT_CANCELED) {
                getActivity().setResult(resultCode);
                //                finish();
                return;
            }

            Log.d(TAG2, "--------CHOOSE_ACCOUNT_REQUEST-----");
            addAccount(data.getStringExtra(EXTRA_SELECTED_ACCOUNT));
            break;
        case ADD_ACCOUNT_REQUEST:
            getActivity().setResult(resultCode);
            if (mPendingIntent != null) {
                mPendingIntent.cancel();
                mPendingIntent = null;
            }
            //            finish();
            break;

        default:

        }
    }

    private BroadcastReceiver mAccountReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context mContext, Intent intent) {
            Log.d(TAG2, "--------onReceive-----");
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (ACCOUNT_ADD_ACTION.equals(action)) {
                Log.d(TAG2, "--------Account_Add_Action-----");
                addAccount(intent.getStringExtra(EXTRA_SELECTED_ACCOUNT));
            } else if ("com.android.sync.SYNC_CONN_STATUS_CHANGED".equals(action)) {
                if (autoSyncData != null) {
                    autoSyncData.setChecked(ContentResolver
                            .getMasterSyncAutomaticallyAsUser(mCurrentProfile.getIdentifier()));
                }
            }

            //[Account&Sync][AndroidForWork]
            if (intent.getAction().equals(Intent.ACTION_MANAGED_PROFILE_REMOVED)
                    || intent.getAction().equals(Intent.ACTION_MANAGED_PROFILE_ADDED)) {
                makeAccountsGroup();

            }
        }

    };

    //[2013_08_14] JW A_Tablet SplitView END

    //[Account&Sync][AndroidForWork]
    @Override
    public boolean onPreferenceClick(Preference preference) {
        // TODO Auto-generated method stub
        final int count = mProfiles.size();
        for (int i = 0; i < count; i++) {
            ProfileData profileData = mProfiles.valueAt(i);
            if (preference == profileData.addAccountPreference) {
                Intent intent = new Intent("android.settings.ADD_ACCOUNT_SETTINGS");
                intent.putExtra(EXTRA_USER, profileData.userInfo.getUserHandle());
                intent.putExtra(EXTRA_AUTHORITIES, mAuthorities);
                startActivity(intent);
                return true;
            }
            if (preference == profileData.removeWorkProfilePreference) {
                final int userId = profileData.userInfo.id;
                Utils.createRemoveConfirmationDialog(getActivity(), userId,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mUm.removeUser(userId);
                            }
                        }
                        ).show();
                return true;
            }
        }
        return false;
    }

    //[Account&Sync][AndroidForWork]
    private class MasterSyncStateClickListener implements MenuItem.OnMenuItemClickListener {
        private final UserHandle mUserHandle;

        public MasterSyncStateClickListener(UserHandle userHandle) {
            mUserHandle = userHandle;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (ActivityManager.isUserAMonkey()) {
                Log.d(TAG, "ignoring monkey's attempt to flip sync state");
            } else {
                ConfirmAutoSyncChangeFragment.show(AccountsGroupSettingsActivity.this,
                        !item.isChecked(), mUserHandle);
            }
            return true;
        }
    }

    //[Account&Sync][AndroidForWork]
    /**
     * Dialog to inform user about changing auto-sync setting
     */
    public static class ConfirmAutoSyncChangeFragment extends DialogFragment {
        private static final String SAVE_ENABLING = "enabling";
        private boolean mEnabling;

        public static void show(AccountsGroupSettingsActivity parent, boolean enabling,
                UserHandle userHandle) {
            if (!parent.isAdded())
                return;

            final ConfirmAutoSyncChangeFragment dialog = new ConfirmAutoSyncChangeFragment();
            dialog.mEnabling = enabling;
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_AUTO_SYNC_CHANGE);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            if (savedInstanceState != null) {
                mEnabling = savedInstanceState.getBoolean(SAVE_ENABLING);
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (!mEnabling) {
                builder.setTitle(R.string.data_usage_auto_sync_off_dialog_title);
                builder.setMessage(R.string.data_usage_auto_sync_off_dialog);
            } else {
                builder.setTitle(R.string.data_usage_auto_sync_on_dialog_title);
                if (Config.VZW.equals(Config.getOperator())) {
                    builder.setMessage(R.string.accounts_auto_sync_on_dialog_VZW);
                } else {
                    builder.setMessage(R.string.data_usage_auto_sync_on_dialog);
                }
            }

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ContentResolver.setMasterSyncAutomaticallyAsUser(mEnabling,
                            ((AccountsGroupSettingsActivity)getTargetFragment()).mCurrentProfile.getIdentifier());

                    ((AccountsGroupSettingsActivity)getTargetFragment()).
                            searchDBUpdate(mEnabling, KEY_AUTO_SYNC_DATA,"check_value");
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((AccountsGroupSettingsActivity)getTargetFragment()).autoSyncData.
                            setChecked(ContentResolver.getMasterSyncAutomaticallyAsUser(
                            ((AccountsGroupSettingsActivity)getTargetFragment()).mCurrentProfile.getIdentifier()));
                }
            });

            return builder.create();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
                    ((AccountsGroupSettingsActivity)getTargetFragment()).autoSyncData.
                            setChecked(ContentResolver.getMasterSyncAutomaticallyAsUser(
                            ((AccountsGroupSettingsActivity)getTargetFragment()).mCurrentProfile.getIdentifier()));
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean(SAVE_ENABLING, mEnabling);
        }
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }

            int titleRes = R.string.sync_settings;
            if (Config.VZW.equals(Config.getOperator())) {
                titleRes = R.string.account_settings;
            }

            setSearchIndexData(context, "accountsgroup_settings",
                    context.getString(titleRes), "main",
                    null, null,
					"android.settings.SYNC_SETTINGS",
                    null, null, 1,
                    null, null, null, 1, 0);

            int checkValue = ContentResolver.getMasterSyncAutomatically() ? 1 : 0;
            setSearchIndexData(context, KEY_AUTO_SYNC_DATA,
                    context.getString(R.string.data_usage_menu_auto_sync),
                    context.getString(titleRes),
                    null, null,
					"android.settings.SYNC_SETTINGS",
                    null, null, 1,
                    "CheckBox", null, null, 1, checkValue);

            return mResult;
        }
    };

    public static void setSearchIndexData(Context context, String key,
            String title, String screenTtile, String summaryOn,
            String summaryOff, String intentAction, String intentTargetClass,
            String intentTargetPackage, int currentEnable,
            String preferenceType, String settingsDBTableName,
            String settingsDBField, int visible, int checkValue) {
        SearchIndexableRaw data = new SearchIndexableRaw(context);
        data.key = key;
        data.title = title;
        data.screenTitle = screenTtile;
        data.summaryOn = summaryOn;
        data.summaryOff = summaryOff;
        data.intentAction = intentAction;
        data.intentTargetClass = intentTargetClass;
        data.intentTargetPackage = intentTargetPackage;
        data.currentEnable = currentEnable;
        data.preferenceType = preferenceType;
        data.settingsDBTableName = settingsDBTableName;
        data.settingsDBField = settingsDBField;
        data.visible = visible;
        data.checkValue = checkValue;
        mResult.add(data);
    }
}
