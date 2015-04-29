package com.android.settings.defaultapp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;

import com.android.settings.R;

public class DefaultAppActivity extends Activity  {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_app_main);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            int options = actionBar.getDisplayOptions();
            actionBar.setDisplayOptions(options | ActionBar.DISPLAY_HOME_AS_UP);
        }

        if (savedInstanceState == null) {
            Fragment newFragment = DefaultAppSettings.newInstance();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.main_pane, newFragment).commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

}
