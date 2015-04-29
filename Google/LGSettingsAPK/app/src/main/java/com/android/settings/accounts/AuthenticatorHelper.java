/*
 * Copyright (C) 2012 The Android Open Source Project
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
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncAdapterType;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;

import com.google.android.collect.Maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AuthenticatorHelper {

    private static final String TAG = "AuthenticatorHelper";
    private Map<String, AuthenticatorDescription> mTypeToAuthDescription = new HashMap<String, AuthenticatorDescription>();
    private AuthenticatorDescription[] mAuthDescs;
    private ArrayList<String> mEnabledAccountTypes = new ArrayList<String>();
    private Map<String, Drawable> mAccTypeIconCache = new HashMap<String, Drawable>();
    private HashMap<String, ArrayList<String>> mAccountTypeToAuthorities = Maps.newHashMap();


    private final UserHandle mUserHandle;
    private final UserManager mUm;
    public final Context mContext;
    private String[] mEnabledAccountTypesString;

    public AuthenticatorHelper(Context context, UserHandle userHandle, UserManager userManager) {
        mContext = context;
        mUm = userManager;
        mUserHandle = userHandle;

    }

    public String[] getEnabledAccountTypes() {
        //[2013_08_16] JW Handle NullPointerException
        try {
            mEnabledAccountTypesString = mEnabledAccountTypes.toArray
                    (new String[mEnabledAccountTypes.size()]);
        } catch (NullPointerException e) {
            Log.i("jw", " NullPointerException " + e.getMessage());
        }
        return mEnabledAccountTypesString;
    }

    public void preloadDrawableForType(final Context context, final String accountType) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                getDrawableForType(context, accountType);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
    }

    /**
     * Gets an icon associated with a particular account type. If none found, return null.
     * @param accountType the type of account
     * @return a drawable for the icon or null if one cannot be found.
     */
    public Drawable getDrawableForType(Context context, final String accountType) {
        Drawable icon = null;
        synchronized (mAccTypeIconCache) {
            if (mAccTypeIconCache.containsKey(accountType)) {
                return mAccTypeIconCache.get(accountType);
            }
        }
        if (mTypeToAuthDescription.containsKey(accountType)) {
            try {
                AuthenticatorDescription desc = mTypeToAuthDescription.get(accountType);
                Context authContext = context.createPackageContextAsUser(desc.packageName, 0,
                        mUserHandle);
                icon = mUm.getBadgedIconForUser(
                        authContext.getResources().getDrawable(desc.iconId), mUserHandle);
                synchronized (mAccTypeIconCache) {
                    mAccTypeIconCache.put(accountType, icon);
                }
            } catch (PackageManager.NameNotFoundException e) {
            } catch (Resources.NotFoundException e) {
            } catch (NullPointerException e) {
                Log.i(TAG, " getDrawableForType NullPointerException " + e.getMessage());
            }
        }
        if (icon == null) {
            try {
                icon = context.getPackageManager().getDefaultActivityIcon();
            } catch (NullPointerException e) {
                Log.i(TAG, " NullPointerException " + e.getMessage());
            }
        }
        return icon;
    }

    /**
     * Gets the label associated with a particular account type. If none found, return null.
     * @param accountType the type of account
     * @return a CharSequence for the label or null if one cannot be found.
     */
    public CharSequence getLabelForType(Context context, final String accountType) {
        CharSequence label = "";
        if (mTypeToAuthDescription.containsKey(accountType)) {
            try {
                AuthenticatorDescription desc = mTypeToAuthDescription.get(accountType);
                Context authContext = context.createPackageContextAsUser(desc.packageName, 0,
                        mUserHandle);
                label = authContext.getResources().getText(desc.labelId);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "No label name for account type " + accountType);
            } catch (Resources.NotFoundException e) {
                Log.w(TAG, "No label icon for account type " + accountType);
            } catch (NullPointerException e) {
                Log.i(TAG, " NullPointerException " + e.getMessage());
            }
        }
        return label;
    }

    /**
     * Updates provider icons. Subclasses should call this in onCreate()
     * and update any UI that depends on AuthenticatorDescriptions in onAuthDescriptionsUpdated().
     */
    public void updateAuthDescriptions(Context context) {
        mAuthDescs = AccountManager.get(context)
                .getAuthenticatorTypesAsUser(mUserHandle.getIdentifier());
        for (int i = 0; i < mAuthDescs.length; i++) {
            mTypeToAuthDescription.put(mAuthDescs[i].type, mAuthDescs[i]);
        }
    }

    public void onAccountsUpdated(Context context, Account[] accounts) {
        updateAuthDescriptions(mContext);
        if (accounts == null) {
            accounts = AccountManager.get(mContext).getAccountsAsUser(mUserHandle.getIdentifier());
        }
        mEnabledAccountTypes.clear();
        mAccTypeIconCache.clear();
        for (Account account : accounts) {
            if (!mEnabledAccountTypes.contains(account.type)) {
                mEnabledAccountTypes.add(account.type);
            }
        }
        buildAccountTypeToAuthoritiesMap();
    }

    public boolean containsAccountType(String accountType) {
        return mTypeToAuthDescription.containsKey(accountType);
    }

    public AuthenticatorDescription getAccountTypeDescription(String accountType) {
        return mTypeToAuthDescription.get(accountType);
    }

    public boolean hasAccountPreferences(final String accountType) {
        if (containsAccountType(accountType)) {
            AuthenticatorDescription desc = getAccountTypeDescription(accountType);
            if (desc != null && desc.accountPreferencesId != 0) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getAuthoritiesForAccountType(String type) {
        return mAccountTypeToAuthorities.get(type);
    }

    private void buildAccountTypeToAuthoritiesMap() {
        mAccountTypeToAuthorities.clear();
        SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypesAsUser(
                mUserHandle.getIdentifier());
        for (int i = 0, n = syncAdapters.length; i < n; i++) {
            final SyncAdapterType sa = syncAdapters[i];
            ArrayList<String> authorities = mAccountTypeToAuthorities.get(sa.accountType);
            if (authorities == null) {
                authorities = new ArrayList<String>();
                mAccountTypeToAuthorities.put(sa.accountType, authorities);
            }
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.d(TAG, "Added authority " + sa.authority + " to accountType "
                        + sa.accountType);
            }
            authorities.add(sa.authority);
        }
    }
}
