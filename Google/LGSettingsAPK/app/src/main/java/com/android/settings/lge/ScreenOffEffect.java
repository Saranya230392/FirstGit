package com.android.settings.lge;

import com.android.settings.R;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;

import android.app.ActionBar;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.settings.Utils;
import com.android.settings.SettingsPreferenceFragment;

// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;

/**
@class       ScreenOffEffect
@date        2013/06/03
@author      seungyeop.yeom@lge.com
@brief        Screen off effect operation
@warning    Animation must be careful.
*/

public class ScreenOffEffect extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
    private static final String TAG = "ScreenOffEffect";

    private static final String First_EFFECT_KEY = "no_effect";
    private static final String SECOND_EFFECT_KEY = "circle_effect";
    private static final String THIRD_EFFECT_KEY = "android_style_effect";

    private RadioButtonPreference mFirstScreenOffEffect;
    private RadioButtonPreference mSecondScreenOffEffect;
    private RadioButtonPreference mThirdScreenOffEffect;

    private static final int NO_EFFECT_NUM = 0;
    private static final int CIRCLE_EFFECT_NUM = 1;
    private static final int ANDROID_EFFECT_NUM = 2;

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        addPreferencesFromResource(R.xml.screen_off_effect);

        // Create radio button object
        // Declaration of a radio button click event
        PreferenceScreen root = getPreferenceScreen();
        mFirstScreenOffEffect = (RadioButtonPreference)root.findPreference(First_EFFECT_KEY);
        mFirstScreenOffEffect.setOnPreferenceChangeListener(this);

        mSecondScreenOffEffect = (RadioButtonPreference)root.findPreference(SECOND_EFFECT_KEY);
        mSecondScreenOffEffect.setOnPreferenceChangeListener(this);

        mThirdScreenOffEffect = (RadioButtonPreference)root.findPreference(THIRD_EFFECT_KEY);
        mThirdScreenOffEffect.setOnPreferenceChangeListener(this);

        // actionbar status settings
        //setHasOptionsMenu(true);

        // Back setting is disable. (only tablet)
        // Back Setting is enable. (others)
        // [seungyeop.yeom] 2013-06-20
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {

            if (Utils.supportSplitView(getActivity())) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    // Preview button at the bottom
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.screen_off_effect_button, null);
        Button previewButton = (Button)view.findViewById(R.id.preview_button);
        previewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().startActivity(
                        new Intent(getActivity(), ScreenOffEttectAni.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            }
        });

        return view;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (Utils.isMonkeyRunning()) {
            return true;
        }
        initToggles();

        // Click the radio button to change the DB
        // The change of screen off effect in function
        if (preference == mFirstScreenOffEffect) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.SCREEN_OFF_EFFECT_SET,
                    NO_EFFECT_NUM);
            mFirstScreenOffEffect.setChecked(true);
        } else if (preference == mSecondScreenOffEffect) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.SCREEN_OFF_EFFECT_SET,
                    CIRCLE_EFFECT_NUM);
            mSecondScreenOffEffect.setChecked(true);
        } else if (preference == mThirdScreenOffEffect) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.SCREEN_OFF_EFFECT_SET,
                    ANDROID_EFFECT_NUM);
            mThirdScreenOffEffect.setChecked(true);
        } else {
        }
        Log.d(TAG, "Set: " + Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.SCREEN_OFF_EFFECT_SET, CIRCLE_EFFECT_NUM));
        return true;
    }

    // Initialize the state of radiobutton
    private void initToggles() {
        mFirstScreenOffEffect.setChecked(false);
        mSecondScreenOffEffect.setChecked(false);
        mThirdScreenOffEffect.setChecked(false);

    }

    // Check DB values
    private void updateToggles() {
        int modeDbValue = Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.SCREEN_OFF_EFFECT_SET, CIRCLE_EFFECT_NUM);
        Log.d(TAG,
                "Refresh: "
                        + Settings.System.getInt(getContentResolver(),
                                SettingsConstants.System.SCREEN_OFF_EFFECT_SET, CIRCLE_EFFECT_NUM));
        if (modeDbValue == NO_EFFECT_NUM) {
            mFirstScreenOffEffect.setChecked(true);
        } else if (modeDbValue == CIRCLE_EFFECT_NUM) {
            mSecondScreenOffEffect.setChecked(true);
        } else if (modeDbValue == ANDROID_EFFECT_NUM) {
            mThirdScreenOffEffect.setChecked(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateToggles();
    }

    // Operation of the Menu button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

}
