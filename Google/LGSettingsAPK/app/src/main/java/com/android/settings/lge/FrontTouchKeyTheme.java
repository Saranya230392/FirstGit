package com.android.settings.lge;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.android.settings.lge.RadioButtonPreference;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.SettingsPreferenceFragment;

public class FrontTouchKeyTheme extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
    private RadioButtonPreference mWhite;
    private RadioButtonPreference mBlack;

    private static final String THEME_WHITE = "theme_white";
    private static final String THEME_BLACK = "theme_black";

    // key name
    private static final String DB_KEY_NAME = "navigation_bar_theme";

    private static final String WHITE_DB = "#fff5f5f5";
    private static final String BLACK_DB = "#ff000000";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            if (Utils.supportSplitView(getActivity())) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setPreference();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        setPreference();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // Don't allow any changes to take effect as the USB host will be
        // disconnected, killing
        // the monkeys
        if (isMonkeyRunning()) {
            return true;
        }

        if (preference == mWhite) {
            Settings.System.putString(getContentResolver(), DB_KEY_NAME,
                    WHITE_DB);
        } else if (preference == mBlack) {
            Settings.System.putString(getContentResolver(), DB_KEY_NAME,
                    BLACK_DB);
        }

        if (Utils.supportSplitView(getActivity())) {
            getActivity().onBackPressed();
        } else {
            getActivity().finish();
        }
        return true;
    }

    private void setPreference() {
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().removeAll();
        }
        updateThemesPreferences();
        readStoredPreference();
    }

    private void updateThemesPreferences() {
        addPreferencesFromResource(R.xml.front_touch_key_theme);
        PreferenceScreen root = getPreferenceScreen();
        mWhite = (RadioButtonPreference)root.findPreference(THEME_WHITE);
        mBlack = (RadioButtonPreference)root.findPreference(THEME_BLACK);

        mWhite.setLayoutResource(R.layout.preference_radio_button2);
        mBlack.setLayoutResource(R.layout.preference_radio_button2);

        mWhite.setOnPreferenceChangeListener(this);
        mBlack.setOnPreferenceChangeListener(this);

        if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
            setSIMPreviewImage();
        }
    }

    private void readStoredPreference() {
        // Get theme value.
        String currThemeName = Settings.System.getString(getContentResolver(),
                DB_KEY_NAME);

        if (currThemeName.equals(WHITE_DB)) {
            mWhite.setChecked(true);
        } else if (currThemeName.equals(BLACK_DB)) {
            mBlack.setChecked(true);
        }
    }

    private void setSIMPreviewImage() {
        mWhite.setIcon(R.drawable.img_fronttouchkey_theme_image_sim_01);
        mBlack.setIcon(R.drawable.img_fronttouchkey_theme_image_sim_03);
    }

    // Temp method
    private boolean isMonkeyRunning() {
        return ActivityManager.isUserAMonkey();
    }
}
