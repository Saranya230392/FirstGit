package com.android.settings;

import com.android.settings.accessibility.AccessibilitySettings;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class AccessibilityActivity extends PreferenceActivity {
    private final int mSWITCH_ACTION_BAR_DISPLAY_OPTION = ActionBar.DISPLAY_HOME_AS_UP
            | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO
            | ActionBar.DISPLAY_SHOW_HOME;
    private final static String KEY_WHERE_ARE_YOU_FROM = "where_are_you_from";
    private final static String VALUE_FROM_STARTUP_WIZARD = "from_startup_wizard";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        getActionBar().setDisplayOptions(mSWITCH_ACTION_BAR_DISPLAY_OPTION);

        Intent intent = getIntent();

        if (intent != null) {
            Bundle extra = intent.getExtras();

            if (extra == null) {
                addFragment();
            } else {
                String from = extra.getString(KEY_WHERE_ARE_YOU_FROM);
                if (from != null && VALUE_FROM_STARTUP_WIZARD.equals(from)) {
                    // Start up wizard intent case                    
                    addFragment();

                    // Disable home button of action bar corner
                    actionBar.setHomeButtonEnabled(false);
                    actionBar.setDisplayHomeAsUpEnabled(false);
                }
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(android.R.id.content, new AccessibilitySettings());
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // 20140109 jeonghun.ye
        // TD 148831 - Prevent CKError when pressing back key multiple after Talkback on
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }
}
