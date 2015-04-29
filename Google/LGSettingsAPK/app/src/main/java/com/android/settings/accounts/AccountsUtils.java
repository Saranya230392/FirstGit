package com.android.settings.accounts;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.SyncAdapterType;
import android.util.Log;
import com.google.android.collect.Maps;
import com.whitepages.nameid.NameIDHelper;
import com.android.settings.lgesetting.Config.Config;
import android.os.Build;

public class AccountsUtils {
    private static HashMap<String, ArrayList<String>> mAccountTypeToAuthorities = null;

    public static ArrayList<String> getAuthoritiesForAccountType(String type) {
        if (mAccountTypeToAuthorities == null) {
            mAccountTypeToAuthorities = Maps.newHashMap();
            SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypes();
            for (int i = 0, n = syncAdapters.length; i < n; i++) {
                final SyncAdapterType sa = syncAdapters[i];
                ArrayList<String> authorities = mAccountTypeToAuthorities.get(sa.accountType);
                if (authorities == null) {
                    authorities = new ArrayList<String>();
                    mAccountTypeToAuthorities.put(sa.accountType, authorities);
                }

                authorities.add(sa.authority);
            }
        }
        return mAccountTypeToAuthorities.get(type);
    }

    public static boolean isVisibleAccount(String accountType) {

        if (accountType.equals("com.att.aab")) {
            return false;
        }
        if (accountType.equals("com.mobileleader.sync")) {
            return false;
        }
        if (accountType.equals("com.lge.sync")) {
            return false;
        }
        if (accountType.equals("com.fusionone.account")) {
            return false;
        }
        if (accountType.equals("com.lge.myphonebook")) {
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
        if (accountType.equals("com.vcast.mediamanager.account")) {
            return false;
        }
        if (accountType.equals("com.lge.lifetracker")) {
            return false;
        }
        if (accountType.equals("com.lge.bioitplatform.sdservice")) {
            return false;
        }
        if (accountType.equals("com.verizon.tablet")) {
            return false;
        }
        if (accountType.equals("com.android.localphone")) {
            return false;
        }
        if (accountType.equals("com.android.sim")) {
            return false;
        }
        if (accountType.equals(NameIDHelper.ACCOUNT_TYPE)) {
            return false;
        }
        return true;
    }

    public static boolean getAddAccountPref(ArrayList<String> accountAuths, String[] mAuthorities) {
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
        return addAccountPref;
    }

}
