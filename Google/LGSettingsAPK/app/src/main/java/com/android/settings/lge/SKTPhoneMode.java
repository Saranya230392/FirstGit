package com.android.settings.lge;

import java.util.ArrayList;
import java.util.List;

import com.android.settings.ImagePreference;
import com.android.settings.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import com.android.settings.Utils;
// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;

import android.util.Log;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.Toast;
import android.telephony.TelephonyManager;
import android.os.ServiceManager;
import com.android.internal.telephony.ITelephony;

/**
@class       SKTPhoneMode
@date        2013/12/16
@author      seungyeop.yeom@lge.com
@brief       SKT PhoneMode operation
@warning     New feature
*/

public class SKTPhoneMode extends PreferenceActivity implements
        OnPreferenceChangeListener {
    private static final String TAG = "SKTPhoneMode";

    //private static final String SKT_PHONE_MODE = "skt_phone_mode";
    //private static final String LG_PHONE_MODE = "lg_phone_mode";

    private static final int SKT_PHONE_MODE_NUM = 1;
    private static final int LG_PHONE_MODE_NUM = 0;

    private static final int SKT_PHONE_MODE_VALUE = 1;
    //private static final int LG_PHONE_MODE_VALUE = 0;

    private ImageView mPreviewImage;
    private TextView mModeDesc;
    private Button mCancelbtn;
    private Button mApplybtn;
    private ArrayList<RadioButtonPreference> PreferenceList;
    private PreferenceScreen root;
    public static final int RESULT_CODE = 100;

    private static int sModeDbValue = 0;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        init();
        // actionbar status settings
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (Settings.System.getInt(getContentResolver(),
                SettingsConstants.Global.PHONE_MODE_SET, LG_PHONE_MODE_NUM) == 1) {

            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.PHONE_MODE_APPLY, 1);
        } else {
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.PHONE_MODE_APPLY, 0);
        }
    }

    private ContentObserver mUIObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (Settings.System.getInt(getContentResolver(),
                    SettingsConstants.Global.PHONE_MODE_SET, LG_PHONE_MODE_NUM) == 1) {

                Settings.Global.putInt(getContentResolver(),
                        SettingsConstants.Global.PHONE_MODE_APPLY, 1);
            } else {
                Settings.Global.putInt(getContentResolver(),
                        SettingsConstants.Global.PHONE_MODE_APPLY, 0);
            }

        }
    };

    private boolean isInCall() {
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            if (tm.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                return true;
            }
        }

        int vtCallState = 0;
        ITelephony ts = ITelephony.Stub.asInterface(ServiceManager
                .checkService(Context.TELEPHONY_SERVICE));
        if (ts != null) {
            try {
                vtCallState = ts.getCallState();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (vtCallState >= 100) {
            return true;
        }

        return false;
    }

    private void init() {
        setContentView(R.layout.skt_phone_mode);
        setPreferenceScreen(createPreference());

        mPreviewImage = (ImageView)findViewById(R.id.image_desc);
        mCancelbtn = (Button)findViewById(R.id.cancel_button);
        mApplybtn = (Button)findViewById(R.id.ok_button);
        mModeDesc = (TextView)findViewById(R.id.text_desc);

        mCancelbtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.d("TAG", "click : Cancel button");
                finish();
            }
        });

        mApplybtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.d("TAG", "click : Apply button");

                if (isInCall() && mApplybtn != null) {
                    Toast.makeText(
                            getApplicationContext(),
                            getResources()
                                    .getString(R.string.sp_not_available_during_a_call_NORMAL),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (sModeDbValue == SKT_PHONE_MODE_VALUE) {
                    /*if (Settings.Global.getInt(getContentResolver(),
                            SettingsConstants.Global.PHONE_MODE_AGREE, SKT_PHONE_MODE_NUM) == 1) {

                        Settings.System.putInt(getContentResolver(),
                                SettingsConstants.Global.PHONE_MODE_SET, sModeDbValue);
                        
                        Intent intent = new Intent("com.skt.prod.dialer.CHANGE_TPHONE_MODE_SETTING");
                        sendBroadcast(intent);
                        Log.d("jw", "sendBroadcast CHANGE_TPHONE_MODE_SETTING");

                        finish();
                    } else {*/
                    // Jump Agree Activity
                    Log.d(TAG, "Jump Agree Activity");
                    ComponentName c;
                    c = new ComponentName("com.android.settings",
                            "com.android.settings.lge.SKTPhoneModeSetupWizard");
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.setComponent(c);
                    startActivityForResult(i, RESULT_CODE);

                    //   }
                } else {
                    setPreferred();
                    Settings.System.putInt(getContentResolver(),
                            SettingsConstants.Global.PHONE_MODE_SET, sModeDbValue);
                    Intent intent = new Intent("com.skt.prod.dialer.CHANGE_TPHONE_MODE_SETTING");
                    sendBroadcast(intent);
                    finish();
                }

            }
        });
    }

    /**@method        createPreference()
     * @author         seungyeop.yeom
     * @brief        Reflected in layout by creating a Preference List
     */
    private PreferenceScreen createPreference() {
        // Root
        PreferenceList = new ArrayList<RadioButtonPreference>();
        root = getPreferenceManager().createPreferenceScreen(this);

        Log.d("YSY", "createPreference()");

        RadioButtonPreference tempPreference;
        final Resources r = getResources();
        if (r == null || root == null) {
            return root;
        }

        if (root != null) {
            root.removeAll();
            PreferenceList.clear();
        }

        tempPreference = new RadioButtonPreference(this);
        tempPreference.setTitle(r.getString(R.string.sp_lg_phone_mode_title));
        tempPreference.setEnabled(true);
        tempPreference.setOnPreferenceChangeListener(this);
        PreferenceList.add(tempPreference);

        tempPreference = new RadioButtonPreference(this);
        tempPreference.setTitle(r.getString(R.string.sp_tphone_title));
        tempPreference.setEnabled(true);
        tempPreference.setOnPreferenceChangeListener(this);
        PreferenceList.add(tempPreference);

        for (final RadioButtonPreference tempPref : PreferenceList) {
            root.addPreference(tempPref);
        }
        return root;
    }

    /**@method       initToggles()
     * @author       seungyeop.yeom
     * @brief        When you press the radio button,
     *               created to false once the entire first
     */
    private void initToggles() {
        Log.d("YSY", "initToggles()");
        PreferenceList.get(SKT_PHONE_MODE_NUM).setChecked(false);
        PreferenceList.get(LG_PHONE_MODE_NUM).setChecked(false);
    }

    /**@method      updateToggles()
    * @author       seungyeop.yeom
    * @brief        Updating the status of the radio button of the current
    */
    private void updateToggles() {

        int phoneModeDb = Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.PHONE_MODE_APPLY, LG_PHONE_MODE_NUM);
        Log.d("YSY",
                "updateToggles(): " + Settings.Global.getInt(getContentResolver(),
                        SettingsConstants.Global.PHONE_MODE_APPLY, LG_PHONE_MODE_NUM));
        if (phoneModeDb == SKT_PHONE_MODE_NUM) {
            PreferenceList.get(SKT_PHONE_MODE_NUM).setChecked(true);
            PreferenceList.get(LG_PHONE_MODE_NUM).setChecked(false);
            mPreviewImage.setImageResource(R.drawable.img_pre_skt_phone_mode);
            mModeDesc.setText(R.string.sp_phone_mode_setting_t_summary_first);
            sModeDbValue = 1;
        } else if (phoneModeDb == LG_PHONE_MODE_NUM) {
            PreferenceList.get(LG_PHONE_MODE_NUM).setChecked(true);
            PreferenceList.get(SKT_PHONE_MODE_NUM).setChecked(false);
            mPreviewImage.setImageResource(R.drawable.img_pre_lg_phone_mode);
            mModeDesc.setText(R.string.sp_phone_mode_setting_lg_summary_first);
            sModeDbValue = 0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("YSY", "onResume()");
        Log.d("YSY",
                "MODE SET : " + Settings.System.getInt(getContentResolver(),
                        SettingsConstants.Global.PHONE_MODE_SET, LG_PHONE_MODE_NUM));

        Uri uri = Settings.System.getUriFor(SettingsConstants.Global.PHONE_MODE_SET);
        Log.d(TAG, "register observer uri: " + uri.toString());
        getContentResolver().registerContentObserver(uri, true, mUIObserver);

        updateToggles();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        getContentResolver().unregisterContentObserver(mUIObserver);
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

    /**@method        onConfigurationChanged()
     * @author        seungyeop.yeom
     * @brief         When the screen is vertically and horizontally,
     *                I operate the method.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        init();
        updateToggles();
    }

    /**@method        onPreferenceChange()
     * @author        seungyeop.yeom
     * @brief         When you press the radio button, the event occurs.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // Click the radio button to change the DB
        // The change of screen mode in function

        initToggles();

        Log.d("YSY",
                "onPreferenceChange(): " + Settings.System.getInt(getContentResolver(),
                        SettingsConstants.Global.PHONE_MODE_SET, LG_PHONE_MODE_NUM));

        if (preference == PreferenceList.get(SKT_PHONE_MODE_NUM)) {
            PreferenceList.get(SKT_PHONE_MODE_NUM).setChecked(true);
            sModeDbValue = 1;
            mPreviewImage.setImageResource(R.drawable.img_pre_skt_phone_mode);
            mModeDesc.setText(R.string.sp_phone_mode_setting_t_summary_first);
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.PHONE_MODE_APPLY, 1);
        } else if (preference == PreferenceList.get(LG_PHONE_MODE_NUM)) {
            PreferenceList.get(LG_PHONE_MODE_NUM).setChecked(true);
            sModeDbValue = 0;
            mPreviewImage.setImageResource(R.drawable.img_pre_lg_phone_mode);
            mModeDesc.setText(R.string.sp_phone_mode_setting_lg_summary_first);
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.PHONE_MODE_APPLY, 0);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("jw", "onActivityResult");
        if (resultCode == RESULT_CODE) {
            Log.d("jw", "RESULT_CODE " + RESULT_CODE);
            setPreferred();
            finish();
        }
    }

    public void setPreferred() {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("tel:01047403772"));

        Intent intent2 = new Intent(Intent.ACTION_VIEW);
        intent2.setType("vnd.android.cursor.dir/calls");

        Intent intent3 = new Intent(Intent.ACTION_DIAL);
        intent3.setData(Uri.parse("tel:01047403772"));

        Intent intent4 = new Intent(Intent.ACTION_CALL_BUTTON, null);

        Intent intent5 = new Intent(Intent.ACTION_VIEW);
        intent5.addCategory(Intent.CATEGORY_BROWSABLE);
        intent5.setData(Uri.parse("tel:01047403772"));

        clearPreferredActivity(intent);
        clearPreferredActivity(intent2);
        clearPreferredActivity(intent3);
        clearPreferredActivity(intent4);
        clearPreferredActivity(intent5);

    }

    public void clearPreferredActivity(Intent intent) {
        try {
            List<ResolveInfo> rList = null;
            List<PreferedStorage> mPreferedStorage = new ArrayList<PreferedStorage>();

            PackageManager mPm = getPackageManager();
            rList = mPm.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                            | PackageManager.GET_RESOLVED_FILTER);
            int N = rList.size();
            ComponentName[] set = new ComponentName[N];
            int bestMatch = 0;
            Log.d(TAG, "clearPreferredActivity");
            for (int i = 0; i < N; i++) {
                ResolveInfo r = rList.get(i);
                set[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
                Log.d(TAG, set[i].toString());
                if (r.match > bestMatch)
                    bestMatch = r.match;
                mPm.clearPackagePreferredActivities(set[i].getPackageName());
            }

            mPreferedStorage.add(new PreferedStorage(set, intent, bestMatch));

        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d(TAG, "printStackTrace " + e.toString());
        }
    }

    class PreferedStorage {
        public ComponentName[] set;
        public Intent intent;
        public int bestmatch;

        public PreferedStorage(ComponentName[] comp, Intent i, int match) {
            set = comp.clone();
            intent = i;
            bestmatch = match;
        }

    }
}
