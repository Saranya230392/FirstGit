package com.android.settings.lge;

//import com.android.settings.CustomImagePreference;
import com.android.settings.R;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.android.settings.SettingsPreferenceFragment;

import android.app.ActionBar;

public class KnockOnSwitch extends SettingsPreferenceFragment implements OnClickListener {

    private ContentResolver mResolver;
    private Switch mActionBarSwitch;
    private boolean isKnockOnChecked = false;
    private ActionBar mActionBar;
    SettingsBreadCrumb mBreadCrumb;

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        mResolver = getContentResolver();
        mActionBar = getActivity().getActionBar();
        updateTitle();

        if (Utils.supportSplitView(getActivity())) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View mView = inflater.inflate(R.layout.knock_on_switch, container, false);
        return mView;
    }

    private void updateTitle() {
        mActionBarSwitch = new Switch(getActivity());
        final int padding = getActivity().getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mActionBarSwitch.setPaddingRelative(0, 0, 0, 0);
        } else {
            mActionBarSwitch.setPaddingRelative(0, 0, padding, 0);
        }

        isKnockOnChecked = Settings.System.getInt(mResolver,
                "gesture_trun_screen_on", 0) == 1 ? true : false;
        mActionBarSwitch.setChecked(isKnockOnChecked);

        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            mActionBar.setCustomView(mActionBarSwitch,
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.WRAP_CONTENT,
                            ActionBar.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER_VERTICAL | Gravity.END));
        }

        mActionBarSwitch.setOnClickListener(this);
        mActionBarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Settings.System.putInt(mResolver, "gesture_trun_screen_on", 1);
                }
                else {
                    Settings.System.putInt(mResolver, "gesture_trun_screen_on", 0);
                }
            }
        });
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = new SettingsBreadCrumb(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.addSwitch(mActionBarSwitch);
            }
        }
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

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub

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
    public void onDestroyView() {
        // TODO Auto-generated method stub
        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            getActivity().getActionBar().setCustomView(null);
        }
        super.onDestroyView();
    }

}
