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

package com.android.settings.accessibility;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import android.widget.Switch;
import com.android.settings.SettingsBreadCrumb;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.provider.Settings;

import com.android.settings.R;
import android.util.Log;

import com.android.settings.SettingsPreferenceFragment;

public abstract class ToggleFeaturePreferenceFragment
        extends SettingsPreferenceFragment {

    static final String TAG = "ToggleFeaturePreferenceFragment";

    protected Switch mToggleSwitch;

    protected String mPreferenceKey;
    protected Preference mSummaryPreference;

    protected CharSequence mSettingsTitle;
    protected Intent mSettingsIntent;

    ActionBar actionBar;
    SettingsBreadCrumb mBreadCrumb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(
                getActivity());
        setPreferenceScreen(preferenceScreen);
        actionBar = getActivity().getActionBar();
        mToggleSwitch = new Switch(getActivity());
        mSummaryPreference = new Preference(getActivity()) {
            @Override
            protected void onBindView(View view) {
                super.onBindView(view);
                TextView summaryView = (TextView)view.findViewById(R.id.summary);
                summaryView.setText(getSummary());
                sendAccessibilityEvent(summaryView);
            }

            private void sendAccessibilityEvent(View view) {
                // Since the view is still not attached we create, populate,
                // and send the event directly since we do not know when it
                // will be attached and posting commands is not as clean.
                AccessibilityManager accessibilityManager =
                        AccessibilityManager.getInstance(getActivity());
                if (accessibilityManager.isEnabled()) {
                    AccessibilityEvent event = AccessibilityEvent.obtain();
                    event.setEventType(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                    view.onInitializeAccessibilityEvent(event);
                    view.dispatchPopulateAccessibilityEvent(event);
                    accessibilityManager.sendAccessibilityEvent(event);
                }
            }
        };
        mSummaryPreference.setPersistent(false);
        mSummaryPreference.setLayoutResource(R.layout.text_description_preference);
        preferenceScreen.addPreference(mSummaryPreference);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onProcessArguments(getArguments());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //initSwitch();
    }

    @Override
    public void onDestroyView() {
        actionBar.setCustomView(null);
        super.onDestroyView();
    }

    protected abstract void onPreferenceToggled(String preferenceKey, boolean enabled);

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem = menu.add(mSettingsTitle);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setIntent(mSettingsIntent);
    }

    private void initSwitch() {
        mToggleSwitch.setChecked(
                Settings.Secure.getInt(getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_CAPTIONING_ENABLED, 0) == 1);
        try {
            Activity activity = getActivity();
            if (null != activity) {
                activity = getActivity();
            }
            if (null != activity) {
                if (SettingsBreadCrumb.isAttached(getActivity())) {
                    mToggleSwitch.setPaddingRelative(0, 0, 0, 0);
                } else {
                    setSwitchPadding();
                }
            }
            else {
                Log.e(TAG, "activity is null!!!");
                setSwitchPadding();
            }
        } catch (ClassCastException e) {
            Log.i(TAG, e.getLocalizedMessage());
        }

        mToggleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            }
        });
        mToggleSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                 Settings.Secure.putInt(getActivity().getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_CAPTIONING_ENABLED, mToggleSwitch.isChecked() ? 1 : 0);
            }
        });
    }

    private void setSwitchPadding() {
        final int padding = getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        mToggleSwitch.setPaddingRelative(0, 0, padding, 0);
        if (null != actionBar) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(mToggleSwitch, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL | Gravity.END));
        }
    }

    public void setTitle(String title) {
        getActivity().setTitle(title);
    }

    protected void onProcessArguments(Bundle arguments) {
        if (arguments == null) {
            getPreferenceScreen().removePreference(mSummaryPreference);
            return;
        }

        // Key.
        mPreferenceKey = arguments.getString(AccessibilitySettings.EXTRA_PREFERENCE_KEY);

        // Enabled.
        if (arguments.containsKey(AccessibilitySettings.EXTRA_CHECKED)) {
            final boolean enabled = arguments.getBoolean(AccessibilitySettings.EXTRA_CHECKED);
            mToggleSwitch.setChecked(enabled);
        }

        // Title.
        if (arguments.containsKey(AccessibilitySettings.EXTRA_TITLE)) {
            setTitle(arguments.getString(AccessibilitySettings.EXTRA_TITLE));
        }

        // Summary.
        if (arguments.containsKey(AccessibilitySettings.EXTRA_SUMMARY)) {
            final CharSequence summary = arguments.getCharSequence(
                    AccessibilitySettings.EXTRA_SUMMARY);
            mSummaryPreference.setSummary(summary);

            // Set a transparent drawable to prevent use of the default one.
            getListView().setSelector(new ColorDrawable(Color.TRANSPARENT));
            getListView().setDivider(null);
            // enable for daltonizer list preference
            if (!arguments.getString(AccessibilitySettings.EXTRA_TITLE).
                contains(getString(R.string.accessibility_display_daltonizer_preference_title))) {
                getListView().setEnabled(false);
            }
        } else {
            getPreferenceScreen().removePreference(mSummaryPreference);
        }
    }
}
