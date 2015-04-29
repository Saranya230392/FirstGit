package com.android.settings.lge;

import android.util.Log;
import android.app.Activity;
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
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lge.constants.SettingsConstants;
import com.android.settings.DoubleTitleListPreference;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

import android.app.ActionBar;
import android.view.MenuItem;
import com.android.settings.R;

/**
 * @class QuickWindowCaseHelp
 * @date 2014/11/18 (updated)
 * @author seungyeop.yeom@lge.com
 * @brief Help guide of window cover (QuickWindow case, QuickCircle case)
 * @warning Nothing
 */

public class QuickWindowCaseHelp extends Activity {

    ImageView mHelpImage;
    Button mButton;
    TextView mHelpTitle;
    TextView mHelpDesc1;
    TextView mHelpDesc2;
    TextView mHelpDesc2_2;
    TextView mHelpDesc3;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        setContentView(R.layout.quick_window_case_help);
        Log.d("YSY", "OnCreate of Window cover");
        mHelpImage = (ImageView)findViewById(R.id.hepl_image);
        mButton = (Button)findViewById(R.id.ok_button);
        mHelpTitle = (TextView)findViewById(R.id.help_title);
        mHelpDesc1 = (TextView)findViewById(R.id.hepl_desc_1);
        mHelpDesc2 = (TextView)findViewById(R.id.hepl_desc_2);
        mHelpDesc2_2 = (TextView)findViewById(R.id.hepl_desc_2_2);
        mHelpDesc3 = (TextView)findViewById(R.id.hepl_desc_3);

        // layout of QuickWindow case
        if (Config.getFWConfigBool(this,
                com.lge.R.bool.config_using_window_cover,
                "com.lge.R.bool.config_using_window_cover") == true) {
            Log.d("YSY", "Help of QuickWindow case");
            mHelpTitle.setText(R.string.display_quick_cover_window_title);
            mHelpImage.setImageResource(R.drawable.help_quickcover_case_clock_02);
            mHelpDesc1
                    .setText(R.string.quick_window_case_help_desc1_kk_version);
            mHelpDesc2
                    .setText(R.string.quick_window_case_help_desc2_kk_version);
            mHelpDesc2_2.setVisibility(View.GONE);
            mHelpDesc3
                    .setText(R.string.quick_window_case_help_desc3_kk_version);

            if (Utils.is_G_model_Device(Build.DEVICE)) {
                // only G and Gpro model
                mHelpDesc1
                        .setText(R.string.display_quick_case_window_help_first);
                mHelpDesc2
                        .setText(R.string.display_quick_case_window_help_seconds_ex);
                mHelpDesc3.setVisibility(View.GONE);
            } else {
                // It is a label that come out if there is no weather app and no
                // cover contacts app
                if (!Utils.checkPackage(this,
                        "com.lge.widget.quickcovercontacts")) {
                    if (!Utils.checkPackage(this,
                            "com.lge.sizechangable.weather")) {
                        mHelpDesc2
                                .setText(R.string.quick_window_case_help_desc2_kk_out_weather_phone);
                    } else {
                        mHelpDesc2
                                .setText(R.string.quick_window_case_help_desc2_kk_out_phone);
                    }
                } else {
                    if (!Utils.checkPackage(this,
                            "com.lge.sizechangable.weather")) {
                        mHelpDesc2
                                .setText(R.string.quick_window_case_help_desc2_kk_version_trf);
                    }
                }
            }
        }

        // layout of QuickCircle case
        if (Config.getFWConfigBool(this,
                com.lge.R.bool.config_using_circle_cover,
                "com.lge.R.bool.config_using_circle_cover") == true) {
            Log.d("YSY", "Help of QuickCircle case");
            mHelpTitle.setText(R.string.quick_circle_case_help_title);
            mHelpImage.setImageResource(R.drawable.help_quickcover_case);
            mHelpDesc1.setText(R.string.quick_circle_case_help_desc1_ex);
            mHelpDesc2.setText(R.string.quick_circle_case_help_desc2_ex_1);
            mHelpDesc2_2.setText(R.string.quick_circle_case_help_desc2_ex_2);
            mHelpDesc3.setText(R.string.quick_circle_case_help_desc3);

            if (!Utils.checkPackage(this, "com.android.mms")) {
                mHelpDesc2_2
                        .setText(R.string.quick_circle_case_help_desc2_ex_2_no_message);
            }
        }

        // layout of Disney case
        if (Config.getFWConfigBool(this,
                com.lge.R.bool.config_using_disney_cover,
                "com.lge.R.bool.config_using_disney_cover") == true) {
            Log.d("YSY", "Help of QuickCircle case");
            mHelpTitle.setText(R.string.disney_case_title_ex);
            mHelpImage.setImageResource(R.drawable.help_quickcover_case);
            mHelpDesc1.setText(R.string.disney_case_help_desc1);
            mHelpDesc2.setText(R.string.disney_case_help_desc2);
            mHelpDesc2_2.setVisibility(View.GONE);
            mHelpDesc3.setVisibility(View.GONE);
        }

        mButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                finish();
            }
        });

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
