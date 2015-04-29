/*
 * 
 */

package com.android.settings;

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.android.settings.lgesetting.Config.Config;
import com.android.settings.R;

public class MultitaskingHelpMenu extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnCheckedChangeListener, OnSeekBarChangeListener {

    private Preference mHelpAppSplitView;
    private Preference mHelpSlideAside;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.multitasking_help_menu);
        getActivity().getActionBar().setIcon(R.drawable.ic_settings_multitasking_splitview);
        getActivity().getActionBar().setTitle(R.string.sp_gesture_category_help_NOMAL);

        mHelpSlideAside = (Preference)findPreference("slideaside_help");
        mHelpAppSplitView = (Preference)findPreference("appsplitview_help");

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mHelpSlideAside) {

        } else if (preference == mHelpAppSplitView) {

        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        // Don't remove this function
        // If remove this, onCreate() will show password popup when orientation is changed. 
        super.onConfigurationChanged(newConfig);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {

        return true;

    }

    @Override
    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        // TODO Auto-generated method stub

    }

}
