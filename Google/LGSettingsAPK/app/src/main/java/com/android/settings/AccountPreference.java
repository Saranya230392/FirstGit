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

package com.android.settings;

import java.util.ArrayList;

import com.android.settings.accounts.AuthenticatorHelper;
import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * AccountPreference is used to display a username, status and provider icon for an account on
 * the device.
 */
public class AccountPreference extends Preference {
    private static final String TAG = "AccountPreference";
    public static final int SYNC_ENABLED = 0; // all know sync adapters are enabled and OK
    public static final int SYNC_DISABLED = 1; // no sync adapters are enabled
    public static final int SYNC_ERROR = 2; // one or more sync adapters have a problem
    public static final int SYNC_IN_PROGRESS = 3; // currently syncing
    private int mStatus;
    private Account mAccount;
    private ArrayList<String> mAuthorities;
    private ImageView mSyncStatusIcon;
    private boolean mShowTypeIcon;
    private boolean mIsSkipToAccount;
    private String mAccountType;
    //private AuthenticatorHelper mAuthenticatorHelper;

    // New constructor. It is used to XML Tag.
    public AccountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AccountPreference(Context context, Account account, Drawable icon,
            ArrayList<String> authorities, boolean showTypeIcon, int startPoint) {
        super(context);
        if (startPoint == 0) {
            setLayoutResource(R.layout.account_preference);
        } else if (startPoint == 100) {
            setLayoutResource(R.layout.account_preference_add);
        } else {
            setLayoutResource(R.layout.account_preference_manage_account);
        }
        mAccount = account;
        mAuthorities = authorities;
        mShowTypeIcon = showTypeIcon;
        if (showTypeIcon) {
            setIcon(icon);
        } else {
            setIcon(getSyncStatusIcon(SYNC_DISABLED));
        }
        setTitle(mAccount.name);
        setSummary("");
        setPersistent(false);
        setSyncStatus(SYNC_DISABLED, false);
    }

    public AccountPreference(Context context, Account account, Drawable icon,
            ArrayList<String> authorities, boolean showTypeIcon, boolean isSkip,
            String accountType, String accountLabel, int startPoint) {
        super(context);
        if (startPoint == 0) {
            setLayoutResource(R.layout.account_preference);
        } else if (startPoint == 100) {
            setLayoutResource(R.layout.account_preference_add);
        } else {
            setLayoutResource(R.layout.account_preference_manage_account);
        }
        mAccountType = accountType;
        mIsSkipToAccount = isSkip;
        mAccount = account;
        mAuthorities = authorities;
        mShowTypeIcon = showTypeIcon;
        if (showTypeIcon) {
            setIcon(icon);
        } else {
            setIcon(getSyncStatusIcon(SYNC_DISABLED));
        }

        //mAuthenticatorHelper = new AuthenticatorHelper();
        //CharSequence label = mAuthenticatorHelper.getLabelForType(context, mAccountType);
        if (accountLabel == null) {
            setTitle(" ");
        } else {
            setTitle(accountLabel);
        }
        setSummary("");
        setPersistent(false);
        setSyncStatus(SYNC_DISABLED, false);
    }

    public String getAccountType() {
        return mAccountType;
    }

    public boolean getIsSkip() {
        return mIsSkipToAccount;
    }

    public Account getAccount() {
        return mAccount;
    }

    public ArrayList<String> getAuthorities() {
        return mAuthorities;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        if (!mShowTypeIcon) {
            mSyncStatusIcon = (ImageView)view.findViewById(android.R.id.icon);
            mSyncStatusIcon.setImageResource(getSyncStatusIcon(mStatus));
            mSyncStatusIcon.setContentDescription(getSyncContentDescription(mStatus));
        }
    }

    public void setSyncStatus(int status, boolean updateSummary) {
        mStatus = status;
        if (!mShowTypeIcon && mSyncStatusIcon != null) {
            mSyncStatusIcon.setImageResource(getSyncStatusIcon(status));
            mSyncStatusIcon.setContentDescription(getSyncContentDescription(mStatus));
        }
        if (updateSummary) {
            setSummary(getSyncStatusMessage(status));
        }
    }

    private int getSyncStatusMessage(int status) {
        int res;
        switch (status) {
        case SYNC_ENABLED:
            res = R.string.sync_enabled;
            break;
        case SYNC_DISABLED:
            res = R.string.sync_disabled;
            break;
        case SYNC_ERROR:
            res = R.string.sync_error;
            break;
        case SYNC_IN_PROGRESS:
            res = R.string.sync_in_progress;
            break;
        default:
            res = R.string.sync_error;
            Log.e(TAG, "Unknown sync status: " + status);
        }
        if (mAccount.type.equals("lg.uplusbox")) {

            if (isAutoCheck()) {
                res = R.string.sync_enabled_on;
            } else {
                res = R.string.sync_enabled_off;
            }

        }
        return res;
    }

    private int getSyncStatusIcon(int status) {
        int res;
        switch (status) {
        case SYNC_ENABLED:
            res = R.drawable.ic_sync_green_holo;
            break;
        case SYNC_DISABLED:
            res = R.drawable.ic_sync_grey_holo;
            break;
        case SYNC_ERROR:
            res = R.drawable.ic_sync_red_holo;
            break;
        case SYNC_IN_PROGRESS:
            res = R.drawable.ic_sync_green_holo;
            break;
        default:
            res = R.drawable.ic_sync_red_holo;
            Log.e(TAG, "Unknown sync status: " + status);
        }
        if (mAccount.type.equals("lg.uplusbox")) {

            if (isAutoCheck()) {
                res = R.drawable.ic_sync_green_holo;
            } else {
                res = R.drawable.ic_sync_grey_holo;
            }

        }
        return res;
    }

    private String getSyncContentDescription(int status) {
        switch (status) {
        case SYNC_ENABLED:
            return getContext().getString(R.string.accessibility_sync_enabled);
        case SYNC_DISABLED:
            return getContext().getString(R.string.accessibility_sync_disabled);
        case SYNC_ERROR:
            return getContext().getString(R.string.accessibility_sync_error);
        default:
            Log.e(TAG, "Unknown sync status: " + status);
            return getContext().getString(R.string.accessibility_sync_error);
        }
    }

    public boolean isAutoCheck() {

        Context otherAppsContenxt = null;
        boolean result;

        try {
            otherAppsContenxt = getContext().createPackageContext
                    ("lg.uplusbox", Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        SharedPreferences sp = otherAppsContenxt.getSharedPreferences(
                "EXTERNAL_AUTOBACKUP_STATUS", Context.MODE_MULTI_PROCESS);

        result = sp.getBoolean("AUTOBACKUP_ON_OFF", false);

        return result;
    }
}
