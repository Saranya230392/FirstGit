package com.android.settings.lge;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

//import com.android.settings.CustomImagePreference;
import com.android.settings.R;
import com.android.settings.SettingsBreadCrumb;
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

import com.lge.constants.SettingsConstants;
import com.android.settings.DoubleTitleListPreference;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.SettingsPreferenceFragment;

import android.app.ActionBar;
import android.view.MenuItem;
import android.view.ViewGroup;

public class QuickCoverView extends SettingsPreferenceFragment implements OnCheckedChangeListener {

    private Switch mSwitch;
    private SettingsBreadCrumb mBreadCrumb;

    @Override
    public void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        Init();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        boolean isEnabled = (Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.QUICK_COVER_ENABLE, 1) != 0);
        mSwitch.setChecked(isEnabled);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean status) {
        if (mSwitch != null) {
            mSwitch.setChecked(status);
        }
        Settings.Global.putInt(getContentResolver(),
                SettingsConstants.Global.QUICK_COVER_ENABLE, status ? 1 : 0);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mSwitch.setChecked(Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.QUICK_COVER_ENABLE, 1) != 0);

        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = SettingsBreadCrumb.get(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.addSwitch(mSwitch);
            }
        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            if (mBreadCrumb != null) {
                mBreadCrumb.removeSwitch();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final Activity activity = getActivity();
        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            View view = activity.getActionBar().getCustomView();
            if (view != null) {
                view.destroyDrawingCache();
            }
            activity.getActionBar().setCustomView(null);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        getPreferenceScreen().removeAll(); // The removal of all preference
        Init(); // Re-initialization
    }

    public void Init() {
        Activity activity = getActivity();

        addPreferencesFromResource(R.xml.quick_cover_view);

        mSwitch = new Switch(activity);
        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            }
        });
        mSwitch.setOnCheckedChangeListener(this);
        final int padding = activity.getResources().
                getDimensionPixelSize(R.dimen.action_bar_switch_padding);
        mSwitch.setPaddingRelative(0, 0, padding, 0);

        mSwitch.setChecked(Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.QUICK_COVER_ENABLE, 1) != 0);

        if (SettingsBreadCrumb.isAttached(getActivity())) {
            activity.getActionBar().setDisplayHomeAsUpEnabled(false);
            mSwitch.setPaddingRelative(0, 0, 0, 0);
        }
        else {
            activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getActionBar().setCustomView(mSwitch,
                    new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                            ActionBar.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER_VERTICAL | Gravity.END));
            activity.getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
