/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import android.provider.Telephony.Sms.Intents;
import com.android.settings.SmsDefaultItem.SmsAppInfo;
import com.android.settings.Utils;

import java.util.List;

public class SmsDefaultSettings extends SettingsPreferenceFragment implements OnClickListener {
	
    public static final String TAG = "SmsDefaultSettings";
    private static final String LGSMS_PACKAGENAME = "com.android.mms";
    private static final String VZWMSG_PACKAGENAME = "com.verizon.messaging.vzmsgs";
    private LayoutInflater mInflater;
    private SmsDefaultItem mSmsDefaultItem;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mSmsDefaultItem = new SmsDefaultItem(getActivity());
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (Utils.supportSplitView(getActivity())) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void refresh() {
        PreferenceManager manager = getPreferenceManager();
        PreferenceScreen screen = manager.createPreferenceScreen(getActivity());

        // Get all payment services
        List<SmsAppInfo> appInfos = mSmsDefaultItem.getSmsAppInfo();

        if (appInfos != null && appInfos.size() > 0) {
            // Add all payment apps
            for (SmsAppInfo appInfo : appInfos) {
                SmsDefaultAppPreference preference =
                        new SmsDefaultAppPreference(getActivity(), appInfo, this);
                screen.addPreference(preference);
            }
        }
        setPreferenceScreen(screen);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = mInflater.inflate(R.layout.sms_default, container, false);
        return v;
    }

    @Override
    public void onClick(View v) {
        if (v != null) {    	
            if (v.getTag() instanceof SmsAppInfo) {
                SmsAppInfo appInfo = (SmsAppInfo)v.getTag();
                if (appInfo.mAPKName == null) {
                    return;
                }
                Resources r =  getActivity().getResources();
                String defaultPackage = r.getString(com.android.internal.R.string.default_sms_application);
            
                if (mSmsDefaultItem == null) {
                    return;
                }
                if (!appInfo.mAPKName.equals(LGSMS_PACKAGENAME)
                        && mSmsDefaultItem.getDefaultSMSApp(getActivity()).equals(LGSMS_PACKAGENAME)
                        && !defaultPackage.equalsIgnoreCase(VZWMSG_PACKAGENAME)) {
                    Intent intent = new Intent("com.lge.settings.ACTION_CHANGE_DEFAULT_WARMING_POPUP");
                    intent.putExtra(Intents.EXTRA_PACKAGE_NAME, appInfo.mAPKName);
                    startActivity(intent);
                } else {
                    mSmsDefaultItem.setDefaultSMSApp(getActivity(), appInfo.mAPKName);
                }
                refresh();
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public class SmsDefaultAppPreference extends Preference {
        private final OnClickListener mListener;
        private final SmsAppInfo appInfo;

        public SmsDefaultAppPreference(Context context, SmsAppInfo appInfo, OnClickListener listener) {
            super(context);
            setLayoutResource(R.layout.sms_default_option);
            this.appInfo = appInfo;
            this.mListener = listener;
         }

        @Override
        protected void onBindView(View view) {
            super.onBindView(view);

            view.setOnClickListener(mListener);
            view.setTag(appInfo);

            RadioButton radioButton = (RadioButton)view.findViewById(android.R.id.button1);
            radioButton.setChecked(appInfo.isDefault);

            ImageView app_icon = (ImageView)view.findViewById(R.id.app_icon);
            TextView app_name = (TextView)view.findViewById(R.id.app_name);

            app_icon.setImageDrawable(appInfo.mAppImages);
            app_name.setText(appInfo.mAppName);

        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
                if (mListener != null) {
                    mListener.onClick(v);
                }
                return true;
            }
            return false;
        }        
    }
}
