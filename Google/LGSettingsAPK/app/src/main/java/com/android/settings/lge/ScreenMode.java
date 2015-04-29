package com.android.settings.lge;

import java.util.ArrayList;

import com.android.settings.ImagePreference;
import com.android.settings.R;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import com.android.settings.Utils;
// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;

import android.util.Log;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.MenuItem;
import android.view.Surface;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
@class       ScreenMode
@date        2013/09/16 (updated)
@author      seungyeop.yeom@lge.com
@brief        Screen mode operation
@warning    New feature
*/

public class ScreenMode extends PreferenceActivity implements
        OnPreferenceChangeListener {

    private static final int STANDARD_MODE_NUM = 0;
    private static final int VIVID_MODE_NUM = 1;
    private static final int NATURAL_MODE_NUM = 2;

    private ArrayList<RadioButtonPreference> PreferenceList;
    private PreferenceScreen root;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        setContentView(R.layout.screen_mode);
        setPreferenceScreen(createPreference());

        // actionbar status settings
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**@method        createPreference()
     * @author         seungyeop.yeom
     * @brief        Reflected in layout by creating a Preference List
     */
    private PreferenceScreen createPreference() {
        // Root
        PreferenceList = new ArrayList<RadioButtonPreference>();
        root = getPreferenceManager().createPreferenceScreen(this);

        Log.d("YSY", "createPreference()");

        RadioButtonPreference tempPreference;
        final Resources r = getResources();
        if (r == null || root == null) {
            return root;
        }

        if (root != null) {
            root.removeAll();
            PreferenceList.clear();
        }

        tempPreference = new RadioButtonPreference(this);
        tempPreference.setTitle(r.getString(R.string.screen_mode_standard));
        tempPreference.setEnabled(true);
        tempPreference.setOnPreferenceChangeListener(this);
        PreferenceList.add(tempPreference);

        tempPreference = new RadioButtonPreference(this);
        tempPreference.setTitle(r.getString(R.string.screen_mode_vivid));
        tempPreference.setEnabled(true);
        tempPreference.setOnPreferenceChangeListener(this);
        PreferenceList.add(tempPreference);

        tempPreference = new RadioButtonPreference(this);
        tempPreference.setTitle(r.getString(R.string.screen_mode_natural));
        tempPreference.setEnabled(true);
        tempPreference.setOnPreferenceChangeListener(this);
        PreferenceList.add(tempPreference);

        for (final RadioButtonPreference tempPref : PreferenceList) {
            root.addPreference(tempPref);
        }
        return root;
    }

    /**@method        initToggles()
     * @author         seungyeop.yeom
     * @brief        When you press the radio button,
     *                 created to false once the entire first
     */
    private void initToggles() {
        Log.d("YSY", "initToggles()");
        PreferenceList.get(STANDARD_MODE_NUM).setChecked(false);
        PreferenceList.get(VIVID_MODE_NUM).setChecked(false);
        PreferenceList.get(NATURAL_MODE_NUM).setChecked(false);
    }

    /**@method        updateToggles()
    * @author         seungyeop.yeom
    * @brief        Updating the status of the radio button of the current
    */
    private void updateToggles() {
        int modeDbValue = Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.SCREEN_MODE_SET, STANDARD_MODE_NUM);
        Log.d("YSY",
                "updateToggles(): " + Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SCREEN_MODE_SET, STANDARD_MODE_NUM));
        if (modeDbValue == STANDARD_MODE_NUM) {
            PreferenceList.get(STANDARD_MODE_NUM).setChecked(true);
        } else if (modeDbValue == VIVID_MODE_NUM) {
            PreferenceList.get(VIVID_MODE_NUM).setChecked(true);
        } else if (modeDbValue == NATURAL_MODE_NUM) {
            PreferenceList.get(NATURAL_MODE_NUM).setChecked(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("YSY", "onResume()");
        updateToggles();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**@method        onConfigurationChanged()
     * @author         seungyeop.yeom
     * @brief        When the screen is vertically and horizontally,
     *                 I operate the method.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.screen_mode);
        setPreferenceScreen(createPreference());
        updateToggles();
    }

    /**@method        onPreferenceChange()
     * @author         seungyeop.yeom
     * @brief        When you press the radio button, the event occurs.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // Click the radio button to change the DB
        // The change of screen mode in function

        Log.d("YSY", "onPreferenceChange()");
        Log.d("YSY", "preference : " + preference);

        initToggles();

        if (preference == PreferenceList.get(STANDARD_MODE_NUM)) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.SCREEN_MODE_SET, STANDARD_MODE_NUM);
            PreferenceList.get(STANDARD_MODE_NUM).setChecked(true);
        } else if (preference == PreferenceList.get(VIVID_MODE_NUM)) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.SCREEN_MODE_SET, VIVID_MODE_NUM);
            PreferenceList.get(VIVID_MODE_NUM).setChecked(true);
        } else if (preference == PreferenceList.get(NATURAL_MODE_NUM)) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.SCREEN_MODE_SET, NATURAL_MODE_NUM);
            PreferenceList.get(NATURAL_MODE_NUM).setChecked(true);
        } else {
        }
        Log.d("YSY",
                "Set: "
                        + Settings.System.getInt(getContentResolver(),
                                SettingsConstants.System.SCREEN_MODE_SET, STANDARD_MODE_NUM));
        return true;
    }
}
