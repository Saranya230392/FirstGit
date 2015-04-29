package com.android.settings.lge;

import com.android.settings.R;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.Utils;

import android.util.Log;
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
import android.util.PrefixPrinter;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;
import com.android.settings.DoubleTitleListPreference;
import com.android.settings.lgesetting.Config.Config;

import android.app.ActionBar;
import android.view.MenuItem;

public class QuickCaseView extends PreferenceActivity implements OnCheckedChangeListener {

    private Switch mSwitch;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        addPreferencesFromResource(R.xml.quick_case_view);

        mSwitch = new Switch(this);
        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            }
        });
        mSwitch.setOnCheckedChangeListener(this);
        final int padding = getResources().getDimensionPixelSize(R.dimen.action_bar_switch_padding);
        mSwitch.setPaddingRelative(0, 0, padding, 0);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);

            getActionBar().setCustomView(mSwitch,
                    new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                            ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
                                    | Gravity.END));
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        boolean isEnabled = (Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.QUICK_VIEW_ENABLE, 1) != 0);
        mSwitch.setChecked(isEnabled);
        View footer = getLayoutInflater().inflate(R.layout.quick_case_view, null);
        getListView().addFooterView(footer, null, false);
        getListView().setFooterDividersEnabled(false);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mSwitch.setChecked(Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.QUICK_VIEW_ENABLE, 1) != 0);
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        View view = getActionBar().getCustomView();
        if (view != null) {
            view.destroyDrawingCache();
        }
        getActionBar().setCustomView(null);
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

    public void onCheckedChanged(CompoundButton button, boolean status) {
        // TODO Auto-generated method stub
        if (mSwitch != null) {
            mSwitch.setChecked(status);
        }
        Settings.Global.putInt(getContentResolver(),
                SettingsConstants.Global.QUICK_VIEW_ENABLE, status ? 1 : 0);
    }
}
