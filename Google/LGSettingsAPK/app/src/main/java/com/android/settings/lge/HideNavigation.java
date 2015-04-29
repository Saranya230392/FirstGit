package com.android.settings.lge;

import android.app.ActionBar;
import android.content.ContentResolver;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.R;

public class HideNavigation extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.hide_navigation);

        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.supportSplitView(getActivity())) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);

        View imageView = getActivity().getLayoutInflater().inflate(R.layout.hide_navigation_help,
                null);
        getListView().addFooterView(imageView, null, false);
        getListView().setFooterDividersEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
