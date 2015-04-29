package com.android.settings;

import com.android.settings.lge.RadioButtonPreference;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;

public class MenuButtonSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private static final String TAG = "MenuButtonSettings";
    
    private static final String MENU_MODE = "menu_mode";
    private static final String RECENT_MODE = "recent_mode";
    private static final int MENU_MODE_NUM = 0;
    private static final int RECENT_MODE_NUM = 1;
    private RadioButtonPreference mMenuMode;
    private RadioButtonPreference mRecentMode;

    private View mView;
    private ListView mlistView;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.menu_settings);
        mMenuMode = (RadioButtonPreference)findPreference(MENU_MODE);
        mMenuMode.setOnPreferenceChangeListener(this);
        mRecentMode = (RadioButtonPreference)findPreference(RECENT_MODE);
        mRecentMode.setOnPreferenceChangeListener(this);

        updateSummary();
        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.supportSplitView(getActivity())) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void updateSummary() {
        if (Config.getFWConfigInteger(getActivity(), com.android.internal.R.integer.config_longPressOnHomeBehavior, 
                                    "com.android.internal.R.integer.config_longPressOnHomeBehavior") == 2) {
            mMenuMode.setSummary(R.string.menu_button_settings_menu_desc);
        }
        mRecentMode.setSummary(R.string.menu_button_settings_recent_desc);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLayout();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateToggles();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLayout();
        updateToggles();
    }

    private void setLayout() {
        mlistView = getListView();
        if (mView != null) {
            mlistView.removeHeaderView(mView);
        }
        mView = getActivity().getLayoutInflater().inflate(R.layout.menu_settings,
                 null);
        mlistView.addHeaderView(mView, null, false);
        ViewGroup rootView = (ViewGroup)getView();
        rootView.removeAllViews();
        rootView.addView(mlistView);
    }

    private void updateToggles() {
        int modeDbValue = Settings.System.getInt(getContentResolver(), 
                SettingsConstants.System.SET_HW_MENU_KEY_OPTIONS,
                MENU_MODE_NUM);
        Log.d(TAG, "Refresh: " + modeDbValue);
        if (modeDbValue == MENU_MODE_NUM) {
            mMenuMode.setChecked(true);
        } else if (modeDbValue == RECENT_MODE_NUM) {
            mRecentMode.setChecked(true);
        }
    }

    private void initToggles() {
        mMenuMode.setChecked(false);
        mRecentMode.setChecked(false);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object arg1) {
        initToggles();
        if (preference == mMenuMode) {
            Settings.System.putInt(getContentResolver(), 
                    SettingsConstants.System.SET_HW_MENU_KEY_OPTIONS, MENU_MODE_NUM);
            mMenuMode.setChecked(true);
        } else if (preference == mRecentMode) {
            Settings.System.putInt(getContentResolver(), 
                    SettingsConstants.System.SET_HW_MENU_KEY_OPTIONS, RECENT_MODE_NUM);
            mRecentMode.setChecked(true);
        }
        return true;
    }
}
