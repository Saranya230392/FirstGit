package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.android.settings.SettingsPreferenceFragment;


public class SKTVerifyAppsPreference extends SettingsPreferenceFragment
      implements Preference.OnPreferenceChangeListener {

    private final static String TAG = "SKTVerifyAppsPreference";
    private static final String KEY_GOOGLE_VERIFY_APPS = "google_verify_apps";
    private static final String KEY_SKT_VERIFY_APPS = "skt_verify_apps";
    public static final String SKT_VERIFIER = "skt_verifier";
    private CheckBoxPreference mGoogleVerifyApps;
    private CheckBoxPreference mSKTVerifyApps;
    private Context mContext;
    private ContentResolver mContentResolver;
    private static final int DIALOG_CHECKED_SKT = 0;
    private static final int DIALOG_UNCHECKED_SKT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        do_Init();
        do_InitPreferenceMenu();
    }

    @Override
    public void onResume() {
        super.onResume();
        do_updateUI();
    }

    private void do_Init() {
        mContext = getActivity().getApplicationContext();
        mContentResolver = mContext.getContentResolver();
        addPreferencesFromResource(R.layout.skt_verify_apps_preference);
    }

    private void do_InitPreferenceMenu() {
        mGoogleVerifyApps = (CheckBoxPreference)findPreference(KEY_GOOGLE_VERIFY_APPS);
        mSKTVerifyApps = (CheckBoxPreference)findPreference(KEY_SKT_VERIFY_APPS);
    }

    private void do_updateUI() {
        mGoogleVerifyApps.setChecked(getVerifierValueGoogle() == 1 ? true : false);
        mSKTVerifyApps.setChecked(getVerifierValueSKT() == 1 ? true : false);
		Log.d(TAG, "do_updateUI()+getVerifierValueSKT()" + getVerifierValueSKT());
        Log.d(TAG, "do_updateUI()+getVerifierValueGoogle()" + getVerifierValueGoogle());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
        Preference preference) {
        if (preference.equals(mGoogleVerifyApps)) {
           setVerifierValueGoogle(mGoogleVerifyApps.isChecked());
        }
        else if (preference.equals(mSKTVerifyApps)) {
            if (!mSKTVerifyApps.isChecked()) {
                showDialog(DIALOG_UNCHECKED_SKT);
            } else {
                showDialog(DIALOG_CHECKED_SKT);
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


    @Override
    public Dialog onCreateDialog(int id) {
        AlertDialog.Builder ab = null;
        switch (id) {
        case DIALOG_CHECKED_SKT:
            ab = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.verify_applications_title_skt)
                    .setPositiveButton(this.getResources().getString(R.string.dlg_ok),
                            new OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                      setVerifierValueSKT(mSKTVerifyApps.isChecked());
                                }
                            })
                    .setMessage(R.string.verify_applications_skt_checked_dialog);
            break;
        case DIALOG_UNCHECKED_SKT:
            ab = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.verify_applications_title_skt)
                    .setPositiveButton(this.getResources().getString(R.string.dlg_ok),
                            new OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                      setVerifierValueSKT(mSKTVerifyApps.isChecked());
                                }
                            })
                    .setMessage(R.string.verify_applications_skt_unchecked_dialog);
            break;
        default:
            break;
        }
        ab.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                	setVerifierValueSKT(!mSKTVerifyApps.isChecked());
					do_updateUI();
                	dialog.dismiss();
                    return true;
                }
                return false;
            }
        });
        if (null != ab) {
            if (true == Utils.isUI_4_1_model(getActivity().getApplicationContext())) {
            } else {
                ab.setIconAttribute(android.R.attr.alertDialogIcon);
            }
            return ab.create();
        } else {
            return super.onCreateDialog(id);
        }
    }


    private int getVerifierValueGoogle() {
        try {
            return Settings.Global.getInt(mContentResolver,
               Settings.Global.PACKAGE_VERIFIER_ENABLE);
        } catch (SettingNotFoundException e) {
               setVerifierValueGoogle(true);
            return 1;
        }
    }

    private void setVerifierValueGoogle(boolean isGoogleChecked) {
    try {
         Settings.Global.putInt(
                mContentResolver,
                Settings.Global.PACKAGE_VERIFIER_ENABLE,
                isGoogleChecked ? 1 : 0);
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    private int getVerifierValueSKT() {
        try {
            return Settings.System.getInt(mContentResolver,
                SKT_VERIFIER);
        } catch (SettingNotFoundException e) {
                setVerifierValueSKT(true);
            return 1;
        }
    }

    public void setVerifierValueSKT(boolean isSKTChecked) {
    Log.d(TAG, "Verify Apps = isSKTChecked" + isSKTChecked);
        try {
        Settings.System.putInt(mContentResolver,
                SKT_VERIFIER,
                isSKTChecked ? 1 : 0);
            Intent intent = new Intent("com.skt.tprotect.action.SERVICE_STATE_CHANGE");
            intent.putExtra("extra.IS_SERVICE_ON", isSKTChecked);
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
