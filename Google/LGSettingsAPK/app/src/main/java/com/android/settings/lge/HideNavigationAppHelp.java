package com.android.settings.lge;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import com.android.settings.Utils;

import com.android.settings.SettingsPreferenceFragment;

public class HideNavigationAppHelp extends SettingsPreferenceFragment {
    private View rootview;
    private ViewGroup mContainer;

    @Override
    public void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);

        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.supportSplitView(getActivity())) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        mContainer = container;
        rootview = inflater.inflate(R.layout.hide_navigation_help, container, false);
        return rootview;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = (LayoutInflater)getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        rootview = inflater.inflate(R.layout.hide_navigation_help, mContainer, false);
        ViewGroup rootView = (ViewGroup)getView();
        rootView.removeAllViews();
        rootView.addView(rootview);
    }

}
