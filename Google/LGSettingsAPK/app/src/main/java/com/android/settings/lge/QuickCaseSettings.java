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

import com.lge.constants.SettingsConstants;
import com.android.settings.DoubleTitleListPreference;
import com.android.settings.lge.LollipopRadioButtonPreference.OnImageButtonClickListener;

import com.android.settings.lgesetting.Config.Config;

import android.app.ActionBar;
import android.view.MenuItem;

public class QuickCaseSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String QUICK_COVER_KEY = "quick_cover";
    private static final String QUICK_CASE_WINDOW_KEY = "quick_case_window";
    private static final String QUICK_CASE_NEVER_KEY = "quick_case_never";

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
        // Items of perference are registered.
        mQuickCaseNever = (LollipopRadioButtonPreference)root.findPreference(QUICK_CASE_NEVER_KEY);
        mQuickCover = (LollipopRadioButtonPreference)root.findPreference(QUICK_COVER_KEY);
        mQuickCaseWindow = (LollipopRadioButtonPreference)root
                .findPreference(QUICK_CASE_WINDOW_KEY);

        coverSelect = Settings.Global.getInt(getContentResolver(), "cover_type", 0);
        Log.d("YSY", "cover type : " + coverSelect);
        operateCondition();

        if (root != null) {
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
        LinearLayout quickCaseDescBar = (LinearLayout)view.findViewById(R.id.lollipop_desc_bar);
        quickCaseDescBar.setVisibility(View.GONE);

        Button cancelButton = (Button)view.findViewById(R.id.lollipop_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("YSY", "click : cancel");
                finish();
            }
        });

        Button okButton = (Button)view.findViewById(R.id.lollipop_ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("YSY", "click : ok button");

                Settings.Global.putInt(getContentResolver(), "cover_type", coverSelect);

                operateCondition();
                finish();
            }
        });

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
        // The change of Quick cover lollipop in function
        if (preference == mQuickCaseNever) {
            coverSelect = 5;
            mQuickCaseNever.setChecked(true);

        } else if (preference == mQuickCover) {
            coverSelect = 0;
            mQuickCover.setChecked(true);

        } else if (preference == mQuickCaseWindow) {
            coverSelect = 1;
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
        if (coverSelect == 5) {
            mQuickCaseNever.setChecked(true);
        } else if (coverSelect == 0) {
            mQuickCover.setChecked(true);
        } else if (coverSelect == 1) {
            mQuickCaseWindow.setChecked(true);
        }
    }

    private void operateCondition() {

        if (coverSelect == 5) {
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 0);
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, 0);
        } else if (coverSelect == 0) {
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 0);
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, 1);
        } else if (coverSelect == 1) {
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 1);
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateToggles();
    }
}
