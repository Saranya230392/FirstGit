package com.android.settings;

import android.os.Bundle;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;
import android.content.res.Resources;
import android.content.ActivityNotFoundException;
import android.provider.Settings;
import android.net.Uri;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.provider.Settings.Global;
import java.util.Observable;
import java.util.Observer;
import android.content.ContentQueryMap;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.telephony.TelephonyManager;

import com.lge.constants.SettingsConstants;

public class AdvancedCalling extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "AdvancedCalling";

    public static final String AUTHORITY = "settings";
    public static final Uri GLOBAL_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/global");

    private static final String KEY_ACTIVE_ADVANCED_CALLING = "active_advanced_calling";
    private static final String KEY_ADVANCED_CALLING_SWITCH = "advanced_calling_switch";
    private static final String ALLOW_VOLTE_PROVISIONING = "allow_volte_provisioning";

    private Preference mActiveAdvancedCalling;
    private PreferenceCheckBox mAdvancedCallingSwitch;
    private ContentQueryMap mContentQueryMap;
    private Cursor settingsCursor;
    private ConnectivityManager mCm;
    private Context mContext;

    // Advanced Calling Switch
    private AdvancedCallingSwitchPreference mAdvancedCallingSwitchPref;
    private static final String KEY_ADVANCED_CALLING_SWITCH_PREF = "advanced_calling_switch_pref";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.advanced_calling);
        mContext = getActivity();
        mCm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        mActiveAdvancedCalling = (Preference)findPreference(KEY_ACTIVE_ADVANCED_CALLING);
        mAdvancedCallingSwitch = (PreferenceCheckBox)findPreference(KEY_ADVANCED_CALLING_SWITCH);
        mAdvancedCallingSwitch.setOnPreferenceChangeListener(this);

        mAdvancedCallingSwitchPref = (AdvancedCallingSwitchPreference)findPreference(KEY_ADVANCED_CALLING_SWITCH_PREF);
        mAdvancedCallingSwitchPref.setOnPreferenceChangeListener(this);

        addMobileDataObserver();
    }

    private void updateScreenSetting() {
        try {
            Settings.System.getInt(getContentResolver(), ALLOW_VOLTE_PROVISIONING);
        } catch (SettingNotFoundException e) {
            Settings.System.putInt(getContentResolver(), ALLOW_VOLTE_PROVISIONING, 0);
            Log.i(TAG, "First reboot reset ");
        }

        int value = Settings.System.getInt(getContentResolver(), ALLOW_VOLTE_PROVISIONING, 0);

        Log.d(TAG, "updateScreenSetting - value : " + value);

        getPreferenceScreen().removeAll();

        if (value == 0) {
            if (mActiveAdvancedCalling != null) {
                getPreferenceScreen().addPreference(mActiveAdvancedCalling);
                mActiveAdvancedCalling.setEnabled(true);
                mActiveAdvancedCalling.setSummary(R.string.advance_calling_before_provision_summary);
            }
        } else {
            if (mActiveAdvancedCalling != null) {
                getPreferenceScreen().addPreference(mActiveAdvancedCalling);
                mActiveAdvancedCalling.setEnabled(false);
                mActiveAdvancedCalling.setSummary(R.string.advance_calling_after_provision_summary);
            }
            if (isVoipEnabled(getActivity()) && mAdvancedCallingSwitch != null) {
                getPreferenceScreen().addPreference(mAdvancedCallingSwitch);
            }

            if (isVoipEnabled(getActivity()) && mAdvancedCallingSwitchPref != null) {
                getPreferenceScreen().addPreference(mAdvancedCallingSwitchPref);
            }
        }

        // AlwayRemove
        if (mAdvancedCallingSwitch != null) {
            getPreferenceScreen().removePreference(mAdvancedCallingSwitch);
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        updateScreenSetting();

        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction("com.lge.setting.action.voltepopup");
        getActivity().registerReceiver(mIMSReceiver, mFilter);

        updateVolteCallMenu();
        updateVolteCallSwitch();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        try {
            getActivity().unregisterReceiver(mIMSReceiver);
        } catch (Exception e) {
            SLog.i("MDM", "mIMSReceiver unregisterReceiver ", e);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deleteMobileDataObserver();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        Log.d(TAG, "onPreferenceTreeClick");
        if (preference == mActiveAdvancedCalling) {
            Log.d(TAG, "onPreferenceTreeClick - startActivity");
            Intent intent = new Intent("com.vzw.hss.intent.action.PROVISION_VOLTE");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
               Log.w(TAG, "VZW_Activate_VoLTE_Service :com.vzw.hss.intent.action.PROVISION_VOLTE action Not Found", e);
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mAdvancedCallingSwitch) {
            boolean mIsChecked = ((Boolean)objValue).booleanValue();
            SLog.i("onPreferenceChange mIsChecked = " + mIsChecked);

            if (false == mCm.getMobileDataEnabled()) {
                //isVolteoff
                SLog.i("volte popup");
                showMobileDataPopupForVolteCall();
                updateVolteCallMenu();
                return false;
            }

            mAdvancedCallingSwitch.setChecked(mIsChecked);
            updateCallSettingByKey(getActivity(), null, mIsChecked ? 1 : 0, mCALL_ORDER_PRIORITY);
            updateVolteCallMenu();
        }
        return true;
    }

    // call setting provider api
    private static final String mCALL_ORDER_PRIORITY = "call_order_priority";
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_VALUE_STR = "value_str";
    public static final String KEY_VALUE_INT = "value_int";
    private static final String CALL_AUTHORITY = "com.android.phone.CallSettingsProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + CALL_AUTHORITY + "/callsettings");
    public static final String[] CALLSETTINGS_PROJECTION = new String[] {
            KEY_ID,
            KEY_NAME,
            KEY_VALUE_STR,
            KEY_VALUE_INT
    };
    public static void updateCallSettingByKey(Context context, String value_str, int value_int, String key) {
        Log.d(TAG, "updateCallSettingByKey(" + key + ") : value_str is " + value_str
                    + ", value_int is " + value_int);
        String selection = KEY_NAME + "=" + "'" + key + "'";
        ContentValues cv = new ContentValues();
        cv.put(KEY_VALUE_STR, value_str == null ? "" : value_str);
        cv.put(KEY_VALUE_INT, value_int);

        try {
            context.getContentResolver().update(CONTENT_URI, cv, selection,
                    null);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(context, e);
        }
        SLog.i("set ok = " + value_int);
    }
    public static int queryCallSettingValueByKey(Context context, String key) {
        String selection = KEY_NAME + "=" + "'" + key + "'";
        Cursor c = context.getContentResolver().query(CONTENT_URI,
                CALLSETTINGS_PROJECTION,
                selection, null, null);
        int value = -1;
        if (c != null) {
            try {
                if (c.moveToFirst() && c.getCount() == 1) {
                    value = c.getInt(3);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                c.close();
                c = null;
            }
        }
        Log.d(TAG, "queryCallSettingValueByKey(" + key + ") : return value is " + value);
        return value;
    }

    private void deleteMobileDataObserver() {
        if (mContentQueryMap != null) {
            mContentQueryMap.close();
            mContentQueryMap = null;
        }
        if (null != settingsCursor) {
            settingsCursor.close();
        }
    }

    private void addMobileDataObserver() {
        settingsCursor = mContext.getContentResolver()
                .query(GLOBAL_CONTENT_URI, null,
                        "(" + Settings.Global.NAME + "=?)", new String[] {
                        Global.MOBILE_DATA
                        }, null);
        if (settingsCursor != null) {
            mContentQueryMap = new ContentQueryMap(settingsCursor,
                    Settings.Global.NAME, true, null);
        }
        if (mContentQueryMap == null) {
            return;
        }
        mContentQueryMap.addObserver(new Observer() {
            public void update(Observable o, Object arg) {
                SLog.i("SettingsObserver ===>mButtonDataEnabled");                
                updateVolteCallMenu();

                updateVolteCallSwitch();
            }
        });
    }

    private void updateVolteCallMenu() {
        if (null != mAdvancedCallingSwitch
            && (null != (PreferenceCheckBox)findPreference(KEY_ADVANCED_CALLING_SWITCH))) {

            int nValue = queryCallSettingValueByKey(getActivity(), mCALL_ORDER_PRIORITY);
            SLog.i("nvalue = " + nValue);
            mAdvancedCallingSwitch.setChecked( (nValue == 1) ? true : false);
            mAdvancedCallingSwitch
                .setSummary((nValue == 1) ? R.string.disable_voice_over_lte_summary : R.string.allow_voice_over_lte_summary);

            boolean mIsDimming = Utils.isDimmingMobileDataForVolte(getActivity());
            boolean mIsVolteCall = isUseInVideoCall(getActivity()) || isUseInVolteCall(getActivity());
            if (mIsDimming || mIsVolteCall) {
                mAdvancedCallingSwitch.setEnabled(false);
                mAdvancedCallingSwitch.setCheckBoxEnabled(false);
            } else {
                mAdvancedCallingSwitch.setEnabled(true);
                mAdvancedCallingSwitch.setCheckBoxEnabled(mCm.getMobileDataEnabled());
                SLog.i("updateVolteCallMenu setCheckBoxEnabled = " + mCm.getMobileDataEnabled());
                if (mCm.getMobileDataEnabled() == false) {
                    mAdvancedCallingSwitch.setEnabledAppearance(false);
                    mAdvancedCallingSwitch.setEnableClickOnSwitch(true);
                } else {
                    mAdvancedCallingSwitch.setEnabledAppearance(true);
                    mAdvancedCallingSwitch.setEnableClickOnSwitch(false);
                }
            }
        }
    }

    private void updateVolteCallSwitch() {
        if (null != mAdvancedCallingSwitchPref) {

            int nValue = queryCallSettingValueByKey(getActivity(),
                    mCALL_ORDER_PRIORITY);
            SLog.i("updateVolteCallSwitch nvalue = " + nValue);
            // switch
            mAdvancedCallingSwitchPref.setChecked((nValue == 1) ? true : false);

            boolean mIsDimming = Utils
                    .isDimmingMobileDataForVolte(getActivity());
            boolean mIsVolteCall = isUseInVideoCall(getActivity())
                    || isUseInVolteCall(getActivity());
            if (mIsDimming || mIsVolteCall) {
                // switch
                mAdvancedCallingSwitchPref.setEnabled(false);
            } else {
                // switch
                mAdvancedCallingSwitchPref.setEnabled(mCm
                        .getMobileDataEnabled());

                SLog.i("updateVolteCallSwitch setCheckBoxEnabled = "
                        + mCm.getMobileDataEnabled());
                if (mCm.getMobileDataEnabled() == false) {
                    // switch
                    mAdvancedCallingSwitchPref.setEnabled(false);

                } else {
                    // switch
                    mAdvancedCallingSwitchPref.setEnabled(true);
                }
            }
        }
    }

    public boolean isUseInVideoCall(Context context) {
        final Uri mCONTENT_URI = Uri.parse("content://com.lge.ims.provider.uc/ucstate");
        int value = 0;
        value = queryIMSIntegerValue(context, mCONTENT_URI, "vt_state");
        Log.i(TAG, "isUseInVideoCall : value = " + value);
        return (value == 1) ? true : false;
    }

    public boolean isUseInVolteCall(Context context) {
        final Uri mCONTENT_URI = Uri.parse("content://com.lge.ims.provider.uc/ucstate");
        int value = 0;
        value = queryIMSIntegerValue(context, mCONTENT_URI, "voip_state");
        Log.i(TAG, "isUseInVolteCall : value = " + value);
        return (value == 1) ? true : false;
    }

    public boolean isVoipEnabled(Context context) {
        final Uri mCONTENT_URI = Uri.parse("content://com.lge.ims.provider.uc/ucstate");
        int value = 0;
        value = queryIMSIntegerValue(context, mCONTENT_URI, "voip_enabled");
        Log.i(TAG, "isVoipEnabled : value = " + value);
        return (value == 1) ? true : false;
    }

    public int queryIMSIntegerValue(Context context, Uri uri, String columnName) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        int value = -1;
        try {
            cursor = cr.query(uri, null, null, null, null);
            if (cursor == null) {
                SLog.i("Cursor is null");
                return value;
            }
            if (cursor.getCount() > 0) {
                int index = cursor.getColumnIndex(columnName);
                if (cursor.moveToFirst() && cursor.getCount() == 1) {
                    value = cursor.getInt(index);
                }
                SLog.i("queryIMSIntegerValue() : key value is = " + value + ", index = " + index);
            } else {
                SLog.i("Cursor count is invalid");
            }
        } catch (Exception e) {
            SLog.i("Exception = " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return value;
    }

    private final android.content.BroadcastReceiver mIMSReceiver = new android.content.BroadcastReceiver() {
        public void onReceive(Context context, android.content.Intent intent) {
            String action = intent.getAction();
            SLog.d("mIMSReceiver onReceive " + action);
            if ("com.lge.setting.action.voltepopup".equals(action)) {
                showMobileDataPopupForVolteCall();
            }            
        }
    };

    private void showMobileDataPopupForVolteCall() {
        boolean mIsDimming = Utils.isDimmingMobileDataForVolte(getActivity());
        if (mIsDimming) {
            SLog.d("showMobileDataPopupForVolteCall() skip popup under video call");
            return;
        }
        new AlertDialog.Builder(getActivity())
        .setTitle(R.string.title_mobile_data_off)
        .setMessage(R.string.mobile_data_off)
        .setPositiveButton(R.string.def_yes_btn_caption,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    setEnableForMobileData(true);
                    setEnableForVolteCall(true);
                    updateVolteCallMenu();
                }
            })
        .setNegativeButton(R.string.def_no_btn_caption,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            })
        .setOnCancelListener(
            new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {

                }
            }).show();
    }

    private void setEnableForVolteCall (boolean mIsEnabled) {
        mAdvancedCallingSwitch.setChecked(mIsEnabled);
        updateCallSettingByKey(getActivity(), null, mIsEnabled ? 1 : 0, mCALL_ORDER_PRIORITY);
        updateVolteCallMenu();
    }

    private void setEnableForMobileData (boolean mIsEnabled) {
		TelephonyManager mTm = TelephonyManager.from(mContext);
        mTm.setDataEnabled(mIsEnabled);
    }

}
