package com.android.settings.lge;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;

import android.util.Log;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemProperties;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.util.PrefixPrinter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lge.constants.SettingsConstants;
import com.android.settings.DoubleTitleListPreference;
import com.android.settings.lge.LollipopRadioButtonPreference.OnImageButtonClickListener;

import com.android.settings.lgesetting.Config.Config;

import android.app.ActionBar;
import android.view.MenuItem;

public class QuickCaseSettingsGuide extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String FRONT_QUICK_CASE_CATEGORY = "front_quick_case_category";

    private static final String QUICK_COVER_KEY = "quick_cover";
    private static final String QUICK_CASE_WINDOW_KEY = "quick_case_window";
    private static final String QUICK_CASE_NEVER_KEY = "quick_case_never";

    // This object is preferenceCategory
    private PreferenceCategory frontQuickCaseCategory;

    // This object is used in the lollipop. 
    private LollipopRadioButtonPreference mQuickCover;
    private LollipopRadioButtonPreference mQuickCaseWindow;
    private LollipopRadioButtonPreference mQuickCaseNever;

    private static int coverSelect = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.quick_case_settings);

        PreferenceScreen root = getPreferenceScreen();

        // A Category is registered.
        frontQuickCaseCategory = (PreferenceCategory)root.findPreference(FRONT_QUICK_CASE_CATEGORY);

        // Items of perference are registered.
        mQuickCaseNever = (LollipopRadioButtonPreference)frontQuickCaseCategory
                .findPreference(QUICK_CASE_NEVER_KEY);
        mQuickCover = (LollipopRadioButtonPreference)frontQuickCaseCategory
                .findPreference(QUICK_COVER_KEY);
        mQuickCaseWindow = (LollipopRadioButtonPreference)frontQuickCaseCategory
                .findPreference(QUICK_CASE_WINDOW_KEY);

        coverSelect = Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.COVER_TYPE, 0);
        Log.d("YSY", "cover type : " + coverSelect);

        if (frontQuickCaseCategory != null) {
            if (mQuickCaseNever != null) {
                mQuickCaseNever.setOnPreferenceChangeListener(this);
            }

            if (mQuickCover != null) {
                mQuickCover.setIconImage(R.drawable.shortcut_smart_cover);
                mQuickCover.setbuttonImage(R.drawable.ic_quick_setting_button);
                mQuickCover.setContentDesc(getResources().getString(
                        R.string.tethering_help_button_text));
                mQuickCover.setOnPreferenceChangeListener(this);
                mQuickCover.setOnImageButtonClickListener(new OnImageButtonClickListener() {
                    @Override
                    public void onImageButtonClickListener() {
                        ComponentName c;
                        c = new ComponentName("com.android.settings",
                                "com.android.settings.lge.QuickCaseCoverHelp");
                        Intent i = new Intent(Intent.ACTION_MAIN);
                        i.setComponent(c);
                        startActivity(i);

                        Log.d("YSY", "click : Quick Cover help");
                    }
                });
            }

            if (mQuickCaseWindow != null) {
                mQuickCaseWindow.setIconImage(R.drawable.shortcut_quickcover_general);
                mQuickCaseWindow.setbuttonImage(R.drawable.ic_quick_setting_button);
                mQuickCaseWindow.setContentDesc(getResources().getString(
                        R.string.tethering_help_button_text));
                mQuickCaseWindow.setOnPreferenceChangeListener(this);
                mQuickCaseWindow.setOnImageButtonClickListener(new OnImageButtonClickListener() {
                    @Override
                    public void onImageButtonClickListener() {
                        ComponentName c;

                        // [2014-01-24][seungyeop.yeom] only Gpro featuring for seleted apps
                        if (Utils.is_G_model_Device(Build.DEVICE)) {
                            c = new ComponentName("com.android.settings",
                                    "com.android.settings.lge.QuickWindowCase");
                        } else {
                            c = new ComponentName("com.android.settings",
                                    "com.android.settings.lge.QuickCaseWindowHelp");
                        }

                        Intent i = new Intent(Intent.ACTION_MAIN);
                        i.setComponent(c);
                        startActivity(i);

                        Log.d("YSY", "click : QuickWindow case help");
                    }
                });
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.lollipop_cover_bottom_bar, null);
        LinearLayout quickCaseButtonBar = (LinearLayout)view
                .findViewById(R.id.lollipop_button_bar);
        quickCaseButtonBar.setVisibility(View.GONE);

        return view;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        initToggles();

        Log.d("YSY", "onPreferenceChange, cover type : " + coverSelect);
        Log.d("YSY",
                "quick_view_enable : "
                        + Settings.Global.getInt(getContentResolver(),
                                SettingsConstants.Global.QUICK_VIEW_ENABLE, 0));
        Log.d("YSY",
                "quick_cover_enable : "
                        + Settings.Global.getInt(getContentResolver(),
                                SettingsConstants.Global.QUICK_COVER_ENABLE, 0));

        // Click the radio button to change the DB 
        if (preference == mQuickCaseNever) {
            Settings.Global.putInt(getContentResolver(), SettingsConstants.Global.COVER_TYPE, 5);

            // front cover disable
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 0);

            // Quick Cover disable
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, 0);

            mQuickCaseNever.setChecked(true);

        } else if (preference == mQuickCover) {
            Settings.Global.putInt(getContentResolver(), SettingsConstants.Global.COVER_TYPE, 0);

            // front cover disable
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 0);

            // front cover enable
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, 1);

            mQuickCover.setChecked(true);

        } else if (preference == mQuickCaseWindow) {
            Settings.Global.putInt(getContentResolver(), SettingsConstants.Global.COVER_TYPE, 1);

            // front cover enable
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 1);

            // front cover disable
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, 0);

            mQuickCaseWindow.setChecked(true);

        }
        return true;
    }

    // Initialize the state of radiobutton
    private void initToggles() {
        mQuickCaseNever.setChecked(false);
        mQuickCover.setChecked(false);
        mQuickCaseWindow.setChecked(false);
    }

    // Check DB values
    private void updateToggles() {
        int coverDbValue = Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.COVER_TYPE, 0);

        if (coverDbValue == 5) {
            mQuickCaseNever.setChecked(true);
        } else if (coverDbValue == 0) {
            mQuickCover.setChecked(true);
        } else if (coverDbValue == 1) {
            mQuickCaseWindow.setChecked(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateToggles();
    }
}
