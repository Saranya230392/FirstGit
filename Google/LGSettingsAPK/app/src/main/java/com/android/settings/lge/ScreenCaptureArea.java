package com.android.settings.lge;

import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.provider.Settings;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.R;

public class ScreenCaptureArea extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
    private static final String TAG = "ScreenCaptureArea";

    private static final String FULL_SCREEN_MODE = "full_screen_mode";
    private static final String PART_SCREEN_MODE = "parr_screen_mode";
    private static final String DUMMY = "dummy";

    private RadioButtonPreference mFullScreenMode;
    private RadioButtonPreference mPartScreenMode;

    private LinearLayout mFullScreenModeHelp;
    private LinearLayout mPartScreenModeHelp;
    private TextView mFullDescText;
    private TextView mPartDescText;
    private static final int FULL_MODE_NUM = 0;
    private static final int PART_MODE_NUM = 1;
    private View mView;
    private ListView mlistView;

    @Override
    public void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.screen_capture_area);

        mFullScreenMode = (RadioButtonPreference)findPreference(FULL_SCREEN_MODE);
        mFullScreenMode.setOnPreferenceChangeListener(this);
        mPartScreenMode = (RadioButtonPreference)findPreference(PART_SCREEN_MODE);
        mPartScreenMode.setOnPreferenceChangeListener(this);

        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.supportSplitView(getActivity())) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Log.d(TAG, "onCreate");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        setLayout();
    }

    private void setLayout() {
        mlistView = getListView();
        if (Utils.isUI_4_1_model(getActivity())) {
            if (mView != null) {
                mlistView.removeHeaderView(mView);
            }
            mView = getActivity().getLayoutInflater().inflate(R.layout.screen_capture_area_new,
                    null);
            mlistView.addHeaderView(mView, null, false);
            if (findPreference(DUMMY) != null) {
                getPreferenceScreen().removePreference(findPreference(DUMMY));
            }
        } else {
            if (mView != null) {
                mlistView.removeFooterView(mView);
            }
            mView = getActivity().getLayoutInflater().inflate(R.layout.screen_capture_area, null);
            mlistView.addFooterView(mView, null, false);
            mlistView.setFooterDividersEnabled(false);
        }
        initView();
        ViewGroup rootView = (ViewGroup)getView();
        rootView.removeAllViews();
        rootView.addView(mlistView);
    }

    private void initView() {
        mFullScreenModeHelp = (LinearLayout)mView.findViewById(R.id.full_mode_help);
        mPartScreenModeHelp = (LinearLayout)mView.findViewById(R.id.part_mode_help);
        mFullDescText = (TextView)mView.findViewById(R.id.full_desc_text);
        mPartDescText = (TextView)mView.findViewById(R.id.part_desc_text);

        if (Utils.isUI_4_1_model(getActivity())) {
            mFullDescText.setText(R.string.screen_capture_area_full_desc_home);
            mPartDescText.setText(R.string.screen_capture_area_part_desc_home);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLayout();
        updateToggles();
        updateUI();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        initToggles();
        // Click the radio button to change the DB
        // The change of screen mode in function
        if (preference == mFullScreenMode) {
            Settings.System.putInt(getContentResolver(), "screen_capture_area", FULL_MODE_NUM);
            mFullScreenMode.setChecked(true);
        } else if (preference == mPartScreenMode) {
            Settings.System.putInt(getContentResolver(), "screen_capture_area", PART_MODE_NUM);
            mPartScreenMode.setChecked(true);
        }
        updateUI();
        return true;
    }

    private void initToggles() {
        mFullScreenMode.setChecked(false);
        mPartScreenMode.setChecked(false);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        updateToggles();
        updateUI();
        Log.d(TAG, "onResume");
    }

    private void updateToggles() {
        int modeDbValue = Settings.System.getInt(getContentResolver(), "screen_capture_area",
                FULL_MODE_NUM);
        Log.d(TAG, "Refresh: " + modeDbValue);
        if (modeDbValue == FULL_MODE_NUM) {
            mFullScreenMode.setChecked(true);
        } else if (modeDbValue == PART_MODE_NUM) {
            mPartScreenMode.setChecked(true);
        }
    }

    private void updateUI() {
        int modeDbValue = Settings.System.getInt(getContentResolver(), "screen_capture_area",
                FULL_MODE_NUM);
        if (modeDbValue == FULL_MODE_NUM) {
            mFullScreenModeHelp.setVisibility(View.VISIBLE);
            mPartScreenModeHelp.setVisibility(View.GONE);
        } else if (modeDbValue == PART_MODE_NUM) {
            mFullScreenModeHelp.setVisibility(View.GONE);
            mPartScreenModeHelp.setVisibility(View.VISIBLE);
        }
    }
}
