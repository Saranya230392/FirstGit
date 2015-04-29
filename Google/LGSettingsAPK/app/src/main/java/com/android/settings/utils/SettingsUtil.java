package com.android.settings.utils;

import com.android.settings.R;
import com.android.settings.Utils;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class SettingsUtil extends PreferenceActivity {

    Preference mListPatchInfo;
    Preference mEasyPatchInfo;
    Preference mSettingDBTest;

    public final String[] items = {"Sound",
                                   "Quiet button",
                                   "Short cut",
                                   "Accessibility",
                                   "Security"};
    public final static String ITEM = "item";
    public final static int SOUND = 0;
    public final static int QUICK_BUTTON = 1;
    public final static int SHORT_CUT = 2;
    public final static int ACCESSIBILITY = 3;
    public final static int SECURITY = 4;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_util);
        addListPatchPreference();
        addEasyPatchPreference();
        addSettingDBTest();

    }

    void addSettingDBTest() {
        mSettingDBTest = new Preference(this);
        mSettingDBTest.setTitle("Setting DB Test");
        Intent i = new Intent();
        i.setComponent(new ComponentName("com.android.settings",
                                         "com.android.settings.utils.CheckSettingDB"));
        mSettingDBTest.setIntent(i);
        getPreferenceScreen().addPreference(mSettingDBTest);
    }

    void addListPatchPreference() {
        mListPatchInfo = new Preference(this);
        mListPatchInfo.setTitle("[List] patch code info");
        getPreferenceScreen().addPreference(mListPatchInfo);
    }

    void addEasyPatchPreference() {
        mEasyPatchInfo = new Preference(this);
        mEasyPatchInfo.setTitle("[Easy] patch code info");
        //mEasyPatchInfo.setIntent(new Intent("com.lge.settings.CHECK_PATCH"));
        if (Utils.supportEasySettings(this)) {
            getPreferenceScreen().addPreference(mEasyPatchInfo);
        }
    }

    @Override
    @Deprecated
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        // TODO Auto-generated method stub
        if (preference.equals(mListPatchInfo)) {
            patchDialog(false);
            return true;
        }
        else if (preference.equals(mEasyPatchInfo)) {
            patchDialog(true);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void patchDialog(final boolean isTabView) {
        AlertDialog.Builder dialogBulder = new AlertDialog.Builder(this);
        dialogBulder.setTitle("select");

        LayoutInflater inflate = (LayoutInflater)this
                                        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final View contentView = inflate.inflate(R.layout.patchinfo_dialog, null);
        final EditText edit = (EditText)contentView.findViewById(R.id.pw_edit);
        edit.setHint("Init keycode");

        dialogBulder.setView(contentView);
        dialogBulder.setItems(items, new OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int touchItem) {
                // TODO Auto-generated method stub

                if (edit.getText().toString().equals("patchlog")) {
                    Intent i = new Intent();
                    if (false == isTabView) {
                        i.setComponent(new ComponentName(
                                                "com.android.settings",
                                                "com.android.settings.utils.CheckPatchInfo"));
                    }
                    else {
                        i.setAction("com.lge.settings.CHECK_PATCH");
                    }

                    i.putExtra(ITEM, touchItem);
                    startActivity(i);
                }
                else {
                    Toast toast = Toast.makeText(getBaseContext(),
                                "password is not matched.", Toast.LENGTH_SHORT);
                    if (toast != null) {
                        toast.show();
                    }
                }
            }
        });
        dialogBulder.show();
    }
}
