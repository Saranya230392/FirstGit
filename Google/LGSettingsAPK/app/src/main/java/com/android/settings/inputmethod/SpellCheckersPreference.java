/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.settings.inputmethod;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.AttributeSet;
import android.view.textservice.TextServicesManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class SpellCheckersPreference extends CheckBoxAndSettingsPreference {
    private final TextServicesManager mTsm;

    public SpellCheckersPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTsm = (TextServicesManager)context.getSystemService(
                Context.TEXT_SERVICES_MANAGER_SERVICE);
        setChecked(mTsm.isSpellCheckerEnabled());
    }

    @Override
    protected void onCheckBoxClicked() {
        super.onCheckBoxClicked();
        final boolean checked = isChecked();
        mTsm.setSpellCheckerEnabled(checked);
    }

    @Override
    protected void onSettingsButtonClicked() {
        final SettingsPreferenceFragment fragment = getSettingsPreferenceFragment();
        if (fragment != null) {
            startFragment(fragment, SpellCheckersSettings.class.getCanonicalName(), -1, null, 
                R.string.spellcheckers_settings_title);
        }
    }

    private void startFragment(Fragment caller, String fragmentClass, int requestCode, Bundle extras, int titleres) {
        if (caller != null) {
            if (caller.getActivity() instanceof PreferenceActivity) {
                PreferenceActivity preferenceActivity = (PreferenceActivity)caller.getActivity();
                preferenceActivity.startPreferencePanel(fragmentClass, extras,
                    titleres, null, caller, requestCode);
            }
        }
    }
}
