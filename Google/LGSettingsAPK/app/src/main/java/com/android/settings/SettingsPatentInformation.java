package com.android.settings;

import com.android.settings.lgesetting.Config.Config;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class SettingsPatentInformation extends Activity {

    private View view = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.sp_patent_information);

        view = getLayoutInflater().inflate(R.layout.patent_marking_us, null);
        this.setContentView(view);

        Log.d("starmotor", "123456789");

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        Log.d("starmotor", "resume1");
        // TODO Auto-generated method stub
        view = getLayoutInflater().inflate(R.layout.patent_marking_us, null);
        this.setContentView(view);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("starmotor", "onPause1");
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d("starmotor", "destroy1");
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ((Config.getOperator().equals("VZW")
            && com.lge.os.Build.LGUI_VERSION.RELEASE == com.lge.os.Build.LGUI_VERSION_NAMES.V4_1)
                || Utils.getResources().getBoolean(R.bool.config_settings_search_enable)) {
            getMenuInflater().inflate(R.menu.settings_search, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (item.getItemId() == R.id.search) {
            switchToSearchResults();
        }
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void switchToSearchResults() {
        Intent intent = new Intent();
        intent.setAction("com.lge.settings.SETTINGS_SEARCH");
        intent.putExtra("search", true);
        startActivity(intent);
    }

}
