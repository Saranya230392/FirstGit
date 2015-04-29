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

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.view.View;
import android.os.Bundle;
import com.android.settings.R;
import android.view.KeyEvent;
import android.preference.PreferenceScreen;
import android.preference.Preference;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.text.Html;
import android.widget.TextView;
import android.app.ActionBar;
import android.util.Log;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import java.util.List;
import android.os.SystemProperties;

import com.android.settings.lgesetting.Config.Config;

public class ChooseAccountActivitySetupWizard extends ChooseAccountActivity {

    public String TAG = "ChooseAccountActivitySetupWizard";
    private Button mBackButton;
    private Button mNextButton;
    private TextView mDescMessage;
    String targetProduct = SystemProperties.get("ro.build.product");

    AddAccountSettings addAccountSettings = new AddAccountSettings();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (targetProduct.equals("p2"))
        {
            setTheme(com.lge.R.style.Theme_LGE_White);
        }
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.dark_header_title));
        }

        setContentView(R.layout.add_account_screen_setupwizard);

        if (!isSNSAvailable())
        {
            setTitle(R.string.sp_set_email_accounts_NORMAL);
            mDescMessage = (TextView)findViewById(R.id.email_sns_desc);
            mDescMessage.setText(getString(R.string.sp_email_summary_NORMAL));
        }

        mBackButton = (Button)findViewById(R.id.back_button); //20100101 fix button_key action error
        mBackButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("BACK");
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        mNextButton = (Button)findViewById(R.id.next_button); //20100101 fix button_key action error
        mNextButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("NEXT");
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            mBackButton.performClick();

            return false;
        default:
            return super.onKeyDown(keyCode, event);
        }
    }

    private boolean isSNSAvailable() {
        PackageManager pkg = getPackageManager();
        List<PackageInfo> appinfo = pkg
                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        for (int i = 0; i < appinfo.size(); i++) {
            PackageInfo pi = appinfo.get(i);
            String appname = pi.packageName;
            Log.i(TAG, "appname = " + appname);

            if (appname.equals("com.lge.sns")) {
                Log.i(TAG, "isSNSAvailable true - Twitter");
                return true;
            }
        }
        Log.i(TAG, "isSNSAvailable false");
        return false;
    }

    protected boolean isVisibleAccount(String accountType) {

        Log.i(TAG, "isVisibleAccount : " + accountType);

        boolean bResult = AccountsUtils.isVisibleAccount(accountType);

        if (accountType.equals("com.lge.android.finance.sync")) {
            bResult = false;
        }
        if (accountType.equals("com.lge.android.weather.sync")) {
            bResult = false;
        }
        if (accountType.equals("com.lge.android.news.sync")) {
            bResult = false;
        }
        if (accountType.equals("com.lge.android.todayplus.sync")) {
            bResult = false;
        }
        if (accountType.equals("com.lge.sns.myspace")) {
            bResult = false;
        }
        // Exception for nttdocomo account
        if (accountType.equals("com.android.nttdocomo")) {
            bResult = false;
        }
        //KDDI requirement
        if (accountType.equals("com.kddi.ast.auoneid")) {
            bResult = false;
        }
        if (accountType.equals("com.kddi.ast.au")) {
            bResult = false;
        }
        if (accountType.equals("lg.uplusbox")) {
            bResult = false;
        }

        if (accountType.equals("com.skp.tcloud")) {
            bResult = false;
        }

        if ("ORG".equals(Config.getOperator())
                && !"geehrc_org_com".equals(SystemProperties.get("ro.product.name"))) {
            if (accountType.equals("com.google")) {
                bResult = false;
            }
        }
        return bResult;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferences, Preference preference) {
        ProviderPreference pref = (ProviderPreference)preference;
        if (preference instanceof ProviderPreference) {
            Log.d(TAG, "Attempting to add account of type " + pref.getAccountType());
            finishWithAccountType(pref.getAccountType());
            if (pref.getAccountType().equals("com.twitter.android.auth.login")) {
                Log.d(TAG, "isNullActivity = true");
                AddAccountSettings.isNullActivity = true;
            }
        }
        return true;
    }

    private void finishWithAccountType(String accountType) {
        Intent intent = new Intent();
        intent.putExtra(AddAccountSettings.EXTRA_SELECTED_ACCOUNT, accountType);
        setResult(RESULT_OK, intent);
        Log.d(TAG, "finishWithAccountType");
        finish();
    }
    /*
        private boolean IsEmailVersionCheck() {
            boolean bResult = false;
            PackageManager pm = this.getPackageManager();
            try {
                PackageInfo pkgInfo = pm.getPackageInfo("com.lge.email", 0);

                String currentEmailVersion = pkgInfo.versionName;
                currentEmailVersion = currentEmailVersion.replace(".", "");
                currentEmailVersion = currentEmailVersion.substring(0,2);
                Log.d(TAG, "string currentEmailVersion : " + currentEmailVersion);

                int nCurrentEmailVersion = Integer.parseInt(currentEmailVersion);
                Log.d(TAG, "int nCurrentEmailVersion : " + nCurrentEmailVersion);

                if ( nCurrentEmailVersion >= 51 ) {
                    Log.d(TAG, "nCurrentEmailVersion 51 over");
                    bResult = true;
                }
            } catch ( PackageManager.NameNotFoundException e ) {
                bResult = false;
            }

            Log.d(TAG, "IsEmailVersionCheck : " + bResult);
            return bResult;
        }
    */
}
