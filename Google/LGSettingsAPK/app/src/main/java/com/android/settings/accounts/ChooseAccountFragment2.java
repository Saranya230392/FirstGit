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
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorDescription;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.PendingIntent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

public class ChooseAccountFragment2 extends ChooseAccountFragment {

    private static final String TAG = "ChooseAccountFragment";

    @Override
    public void onCreate(Bundle icicle) {
        ((PreferenceActivity)getActivity()).showBreadCrumbs(
                getString(R.string.add_account_label), null);
        super.onCreate(icicle);
    }

    @Override
    public void finishDependOnScenario() {
        // do not finish() !!
    }

    private void finishWithAccountType(String accountType) {
        addAccount(accountType);
    }

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
    //private boolean isTriedLogin = false;
    //private boolean isFinishedNormally = false;
    public static boolean isNullActivity = false;
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
                    Log.d(TAG2, "------------AccountManagerCallback-------[intent == null]-----");
                    //getActivity();
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
                    //                      getActivity().finish();
                    //                isFinishedNormally = true;
                    Log.i("hsmodel", "call back finished");
                }
            }
        }
    };

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

        //isTriedLogin = true;
        Bundle addAccountOptions = new Bundle();
        mPendingIntent = PendingIntent.getBroadcast(getActivity(), 0, identityIntent, 0);
        addAccountOptions.putParcelable(KEY_CALLER_IDENTITY, mPendingIntent);
        addAccountOptions.putBoolean(EXTRA_HAS_MULTIPLE_USERS,
                Utils.hasMultipleUsers(getActivity()));
        AccountManager.get(getActivity()).addAccount(accountType, null, /* authTokenType */
        null, /* requiredFeatures */
        addAccountOptions, null, mCallback, null /* handler */);
        //mAddAccountCalled = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG2, "------onActivityResult-----");
        switch (requestCode) {
        case CHOOSE_ACCOUNT_REQUEST:
            if (resultCode == getActivity().RESULT_CANCELED) {
                //getActivity().setResult(resultCode);
                //                finish();
                //                return;
            }
            //[S][2011.12.16][dongho.song@lge.com][Common][NA] Return to LGSetupWizard when activity is returned from ChooseAccountActivitySetupWizard
            //                      else if ( resultCode == RESULT_OK ) {
            //                              if ( selectReturnedIntent(data) ) {
            //                                      return;
            //                              }
            //            }
            //[E][2011.12.16][dongho.song@lge.com][Common][NA] Return to LGSetupWizard when activity is returned from ChooseAccountActivitySetupWizard

            // Go to account setup screen. finish() is called inside mCallback.
            Log.d(TAG2, "--------CHOOSE_ACCOUNT_REQUEST-----");

            break;
        case ADD_ACCOUNT_REQUEST:
            Log.d(TAG2, "------ADD_ACCOUNT_REQUEST-----");

            mProviderList.clear();
            mAddAccountGroup.removeAll();
            mAuthorities = null;
            mAccountTypesFilter = null;
            accounts = null;

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
            accounts = AccountManager.get(getActivity()).getAccounts();
            updateAuthDescriptions();

            if (mPendingIntent != null) {
                mPendingIntent.cancel();
                mPendingIntent = null;
            }
            //            finish();
            break;

        default:

        }

        //[2013_08_14] JW A_Tablet SplitView END
    }
}
