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

import com.android.settings.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.view.View;
import android.widget.ImageView;

/**
 * ProviderPreference is used to display an image to the left of a provider name.
 * The preference ultimately calls AccountManager.addAccount() for the account type.
 */
public class ProviderPreference extends Preference {
    private Drawable mProviderIcon;
    private ImageView mProviderIconView;
    private String mAccountType;
    private boolean isExistedAccount = false;
    private Drawable checkedAccount;
    private ImageView mCheckAccount;
    private Context context;

    public ProviderPreference(
            Context context, String accountType, Drawable icon, CharSequence providerName,
            boolean isExisted) {
        super(context);
        mAccountType = accountType;
        isExistedAccount = isExisted;
        //setIcon(icon);
        mProviderIcon = icon;
        setPersistent(false);
        setTitle(providerName);
        // Add local layout for convenient control
        setLayoutResource(R.layout.provider_preference);
        this.context = context;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mProviderIconView = (ImageView)view.findViewById(R.id.providerIcon);
        mProviderIconView.setImageDrawable(mProviderIcon);

        // Added check image for accounts logged in
        if (isExistedAccount) {
            checkedAccount = context.getResources().getDrawable(R.drawable.common_btn_check_buttonless_on);
            mCheckAccount = (ImageView)view.findViewById(R.id.checkAccount);
            mCheckAccount.setImageDrawable(checkedAccount);
        }
        //setSummary(mProviderName);
    }

    public String getAccountType() {
        return mAccountType;
    }
}
