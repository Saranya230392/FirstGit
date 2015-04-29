package com.android.settings.lge;

import com.android.settings.BrightnessPreference;
import com.android.settings.LollipopQuickCoverView;
import com.android.settings.SettingsPreferenceFragment;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.DreamBackend.DreamInfo;
import com.android.settings.lgesetting.Config.Config;
// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;

import com.android.settings.lge.LollipopRadioButtonPreference.OnImageButtonClickListener;

/**
   @class       QuickCaseLollipop
   @date        2013/06/18
   @author      seungyeop.yeom@lge.com
   @brief        Quick Cover Lollipop Interface
   @warning        This code will change.
*/
public class QuickCaseLollipopSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String FRONT_QUICK_CASE_CATEGORY = "front_quick_case_category";

    private static final String LOLLIPOP_QUICK_COVER_KEY = "lollipop_quick_cover";
    private static final String LOLLIPOP_QUICK_COVER_LOLLIPOP_BLACK_KEY = "lollipop_quick_cover_lollipop_black";
    private static final String LOLLIPOP_QUICK_COVER_LOLLIPOP_WHITE_KEY = "lollipop_quick_cover_lollipop_white";
    private static final String LOLLIPOP_NEVER_KEY = "lollipop_never";

    // This object is preferenceCategory
    private PreferenceCategory frontQuickCaseCategory;

    // This object is used in the lollipop.
    private LollipopRadioButtonPreference mLollipopQuickCover;
    private LollipopRadioButtonPreference mLollipopQuickCoverLollipopBlack;
    private LollipopRadioButtonPreference mLollipopQuickCoverLollipopWhite;
    private LollipopRadioButtonPreference mLollipopNever;

    private static int coverSelect = 0;
    private static int lollipopSelect = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.quick_case_lollipop_settings);

        PreferenceScreen root = getPreferenceScreen();

        int coverDefalut = Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.COVER_TYPE, 0);

        if (coverDefalut == 1) {
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.COVER_TYPE, 0);
        }

        // A Category is registered.
        frontQuickCaseCategory = (PreferenceCategory)root.findPreference(FRONT_QUICK_CASE_CATEGORY);

        // Items of perference are registered.
        mLollipopNever = (LollipopRadioButtonPreference)frontQuickCaseCategory
                .findPreference(LOLLIPOP_NEVER_KEY);
        mLollipopQuickCover = (LollipopRadioButtonPreference)frontQuickCaseCategory
                .findPreference(LOLLIPOP_QUICK_COVER_KEY);
        mLollipopQuickCoverLollipopWhite = (LollipopRadioButtonPreference)frontQuickCaseCategory
                .findPreference(LOLLIPOP_QUICK_COVER_LOLLIPOP_WHITE_KEY);
        mLollipopQuickCoverLollipopBlack = (LollipopRadioButtonPreference)frontQuickCaseCategory
                .findPreference(LOLLIPOP_QUICK_COVER_LOLLIPOP_BLACK_KEY);

        coverSelect = Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.COVER_TYPE, 0);
        lollipopSelect = Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.LOLLIPOP_COVER_TYPE, 0);
        operateCondition();

        if (frontQuickCaseCategory != null) {
            if (mLollipopNever != null) {
                mLollipopNever.setOnPreferenceChangeListener(this);
            }

            if (mLollipopQuickCover != null) {
                mLollipopQuickCover.setIconImage(R.drawable.img_quickcover_01);
                mLollipopQuickCover.setbuttonImage(R.drawable.ic_quick_help_button);
                mLollipopQuickCover.setContentDesc(getResources().getString(
                        R.string.tethering_help_button_text));
                mLollipopQuickCover.setOnPreferenceChangeListener(this);
                mLollipopQuickCover.setOnImageButtonClickListener(new OnImageButtonClickListener() {
                    @Override
                    public void onImageButtonClickListener() {
                        Intent intent = new Intent(getActivity(), LollipopQuickCoverView.class);
                        startActivity(intent);
                        Log.d("YSY", "click_1");
                    }
                });
            }

            if (mLollipopQuickCoverLollipopWhite != null) {
                mLollipopQuickCoverLollipopWhite.setIconImage(R.drawable.img_quickcover_02);
                mLollipopQuickCoverLollipopWhite.setbuttonImage(R.drawable.ic_quick_setting_button);
                mLollipopQuickCoverLollipopWhite.setContentDesc(getResources().getString(
                        R.string.input_method_settings_button));
                mLollipopQuickCoverLollipopWhite.setOnPreferenceChangeListener(this);
                mLollipopQuickCoverLollipopWhite
                        .setOnImageButtonClickListener(new OnImageButtonClickListener() {
                            @Override
                            public void onImageButtonClickListener() {
                                Intent intent = new Intent("com.lge.lollipop.action.SETTINGS");
                                intent.putExtra("lollipop_cover_type", 1); //White
                                startActivity(intent);
                                Log.d("YSY", "click_3");
                            }
                        });
            }

            if (mLollipopQuickCoverLollipopBlack != null) {
                mLollipopQuickCoverLollipopBlack.setIconImage(R.drawable.img_quickcover_03);
                mLollipopQuickCoverLollipopBlack.setbuttonImage(R.drawable.ic_quick_setting_button);
                mLollipopQuickCoverLollipopBlack.setContentDesc(getResources().getString(
                        R.string.input_method_settings_button));
                mLollipopQuickCoverLollipopBlack.setOnPreferenceChangeListener(this);
                mLollipopQuickCoverLollipopBlack
                        .setOnImageButtonClickListener(new OnImageButtonClickListener() {
                            @Override
                            public void onImageButtonClickListener() {
                                Intent intent = new Intent("com.lge.lollipop.action.SETTINGS");
                                intent.putExtra("lollipop_cover_type", 0); //Black
                                startActivity(intent);
                                Log.d("YSY", "click_2");
                            }
                        });
            }
        }
    }

    // Lollipop description
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lollipop_cover_bottom_bar, null);
        LinearLayout lollipopDescBar = (LinearLayout)view.findViewById(R.id.lollipop_desc_bar);
        lollipopDescBar.setVisibility(View.GONE);

        Button cancelButton = (Button)view.findViewById(R.id.lollipop_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        Button okButton = (Button)view.findViewById(R.id.lollipop_ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Settings.Global.putInt(getContentResolver(),
                        SettingsConstants.Global.COVER_TYPE, coverSelect);
                Settings.Global.putInt(getContentResolver(),
                        SettingsConstants.Global.LOLLIPOP_COVER_TYPE, lollipopSelect);

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

        // Click the radio button to change the DB
        // The change of Quick cover lollipop in function
        if (preference == mLollipopNever) {
            coverSelect = 5;
            mLollipopNever.setChecked(true);

        } else if (preference == mLollipopQuickCover) {
            coverSelect = 0;
            mLollipopQuickCover.setChecked(true);

        } else if (preference == mLollipopQuickCoverLollipopWhite) {
            coverSelect = 2;
            lollipopSelect = 1;
            mLollipopQuickCoverLollipopWhite.setChecked(true);

        } else if (preference == mLollipopQuickCoverLollipopBlack) {
            coverSelect = 2;
            lollipopSelect = 0;
            mLollipopQuickCoverLollipopBlack.setChecked(true);
        }
        return true;
    }

    // Initialize the state of radiobutton
    private void initToggles() {
        mLollipopNever.setChecked(false);
        mLollipopQuickCover.setChecked(false);
        mLollipopQuickCoverLollipopWhite.setChecked(false);
        mLollipopQuickCoverLollipopBlack.setChecked(false);
    }

    // Check DB values
    private void updateToggles() {
        if (coverSelect == 5) {
            mLollipopNever.setChecked(true);
        } else if (coverSelect == 0) {
            mLollipopQuickCover.setChecked(true);

        } else if (coverSelect == 2) {
            if (lollipopSelect == 1) {
                mLollipopQuickCoverLollipopWhite.setChecked(true);
            } else if (lollipopSelect == 0) {
                mLollipopQuickCoverLollipopBlack.setChecked(true);
            }
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
        } else if (coverSelect == 2) {
            if (lollipopSelect == 1) {
                Settings.Global.putInt(getContentResolver(),
                        SettingsConstants.Global.QUICK_VIEW_ENABLE, 1);
                Settings.Global.putInt(getContentResolver(),
                        SettingsConstants.Global.QUICK_COVER_ENABLE, 0);
            } else if (lollipopSelect == 0) {
                Settings.Global.putInt(getContentResolver(),
                        SettingsConstants.Global.QUICK_VIEW_ENABLE, 1);
                Settings.Global.putInt(getContentResolver(),
                        SettingsConstants.Global.QUICK_COVER_ENABLE, 0);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateToggles();
    }
}
