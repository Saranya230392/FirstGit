package com.android.settings.sound;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.lge.constants.SettingsConstants;

public class ACRSettingsHelp extends Activity {

    TextView mBlankLine;
    Button mOK;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.acr_settings_help);

        mBlankLine = (TextView)findViewById(R.id.tv3);
        mBlankLine.setText("\n");

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mOK = (Button)findViewById(R.id.auto_btn);
        mOK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            MenuInflater inflater_search = getMenuInflater();
            inflater_search.inflate(R.menu.settings_search, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setClassName("com.android.settings",
                    "com.android.settings.Settings$ACRSettingsActivity");
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
            finish();
            return true;
        }

        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            if (item.getItemId() == R.id.search) {
                Intent search_intent = new Intent();
                search_intent.setAction("com.lge.settings.SETTINGS_SEARCH");
                search_intent.putExtra("search", true);
                startActivity(search_intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);

    }

/*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.acr_settings_help, container, false);

        mBlankLine = (TextView)mainView.findViewById(R.id.tv3);
        mBlankLine.setText("\n");

        return mainView;
    }
*/
}
