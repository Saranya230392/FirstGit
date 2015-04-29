package com.android.settings.lge;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

//import com.android.settings.CustomImagePreference;
import com.android.settings.R;
import com.android.settings.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
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
import android.util.Log;
import android.util.PrefixPrinter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.lge.constants.SettingsConstants;
import com.android.settings.DoubleTitleListPreference;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.SettingsPreferenceFragment;

import android.app.ActionBar;
import android.view.MenuItem;
import android.view.ViewGroup;

public class KnockOnTablet extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String KEY_KNOCK_ON = "key_knock_on";
    private CheckBoxPreference mKnockOnCheckBox;
    private ContentResolver mResolver;
    private final Configuration mCurConfig = new Configuration();

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        mResolver = getContentResolver();
        addPreferencesFromResource(R.xml.knock_on_tablet);

        mKnockOnCheckBox = (CheckBoxPreference)findPreference(KEY_KNOCK_ON);
        mKnockOnCheckBox.setOnPreferenceChangeListener(this);
        mKnockOnCheckBox
                .setChecked(Settings.System.getInt(mResolver, "gesture_trun_screen_on", 0) != 0);

        if ("DCM".equals(Config.getOperator())) {
            mKnockOnCheckBox.setTitle(R.string.gesture_title_turn_screen_on);
        }

        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.supportSplitView(getActivity())) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mKnockOnCheckBox.setSummary(R.string.gesture_summary_turn_screen_on_tablet_knock_code);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        mCurConfig.updateFrom(newConfig);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View imageView = getActivity().getLayoutInflater().inflate(R.layout.knock_on_tablet, null);
        getListView().addFooterView(imageView, null, false);
        getListView().setFooterDividersEnabled(false);
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

    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        final String key = arg0.getKey();

        int value = arg1.toString().equals("true") ? 1 : 0;
        if (key.equals(KEY_KNOCK_ON)) {
            Settings.System.putInt(mResolver, "gesture_trun_screen_on", value);
        }
        mKnockOnCheckBox.setChecked(value != 0);
        return false;
    }
}
