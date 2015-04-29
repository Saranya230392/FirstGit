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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.telecom.TelecomManager;

import com.lge.constants.SettingsConstants;

public class AdvancedCallingSwitch extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnClickListener {

    private static final String TAG = "AdvancedCallingSwitch";

    public static final String AUTHORITY = "settings";
    public static final Uri GLOBAL_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/global");

    private static final String ALLOW_VOLTE_PROVISIONING = "allow_volte_provisioning";

    private ContentQueryMap mContentQueryMap;
    private Cursor settingsCursor;
    private ConnectivityManager mCm;
    private Context mContext;

    // HD Voice , Video calling
    private static final String KEY_HD_VOICE = "hd_voice";
    private static final String KEY_VIDEO_CALLING = "video_calling";
    private PreferenceCheckBox mHdVoice;
    private PreferenceCheckBox mVideoCalling;

    private Switch mSwitch;
    SettingsBreadCrumb mBreadCrumb;
    private boolean mAllowCheckBox = false;
    int mCount = 0;
    int settingsTtyMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.advanced_calling_switch);
        mContext = getActivity();
        mCm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        mHdVoice = (PreferenceCheckBox)findPreference(KEY_HD_VOICE);
        mHdVoice.setOnPreferenceChangeListener(this);
        mVideoCalling = (PreferenceCheckBox)findPreference(KEY_VIDEO_CALLING);
        mVideoCalling.setOnPreferenceChangeListener(this);

        mSwitch = new Switch(mContext);
        mSwitch.setChecked(AdvancedCalling.queryCallSettingValueByKey(mContext,
                mCALL_ORDER_PRIORITY) > 0);
        mSwitch.setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                mSwitch.setChecked(isChecked);
                AdvancedCalling.updateCallSettingByKey(mContext, null,
                        isChecked ? 1 : 0, mCALL_ORDER_PRIORITY);
                Log.d(TAG, "onCheckedChanged : " + isChecked);
                updateScreenSetting();

            }
        });
        setActionBarBreadCrumb(getActivity());
        addMobileDataObserver();
    }

    private void setActionBarBreadCrumb(Activity activity) {
        final int padding = activity.getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mSwitch.setPaddingRelative(0, 0, 0, 0);
        } else {
            mSwitch.setPaddingRelative(0, 0, padding, 0);
        }
        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            activity.getActionBar().setDisplayOptions(
                    ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getActionBar().setCustomView(
                    mSwitch,
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.WRAP_CONTENT,
                            ActionBar.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER_VERTICAL | Gravity.END));
        }

        activity.getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        activity.getActionBar().setCustomView(
                mSwitch,
                new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.END));

        if (activity.getActionBar() != null) {
            if (Utils.supportSplitView(getActivity())) {
                activity.getActionBar().setDisplayHomeAsUpEnabled(false);
            } else {
                activity.getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void updateScreenSetting() {

        settingsTtyMode = android.provider.Settings.Secure.getInt(
                getContentResolver(),
                android.provider.Settings.Secure.PREFERRED_TTY_MODE, 0);

        mHdVoice.setEnabled(true);
        mHdVoice.setEnabledAppearance(true);
        mHdVoice.setChecked(AdvancedCalling.queryCallSettingValueByKey(
                mContext, mCALL_ORDER_PRIORITY) > 0);

        mVideoCalling.setEnabled(true);
        mVideoCalling.setEnabledAppearance(true);

        boolean misVtEnabled = isVtEnabled(mContext);
        boolean misEabFlag = isEabFlag(mContext);

        if (misVtEnabled && misEabFlag) {
            mVideoCalling.setEnabled(true);
            mVideoCalling.setEnabledAppearance(true);
        } else {
            mVideoCalling.setEnabledAppearance(false);
            mVideoCalling.setEnabled(false);
            setVideoCallingFlagVZW(mContext, 0);
        }

        if (settingsTtyMode == 1) {

            int value = getVideoCallingFlagShownVZW(mContext);
            Log.d(TAG, "settingsTtyMode value : " + value);

            if (TelecomManager.TTY_MODE_OFF == value) {
                Log.d(TAG, "settingsTtyMode");
                mVideoCalling.setChecked(false);
            } else if (TelecomManager.TTY_MODE_FULL == value
                    || TelecomManager.TTY_MODE_HCO == value
                    || TelecomManager.TTY_MODE_VCO == value) {
                mVideoCalling.setChecked(true);
            }
        } else {
            mVideoCalling.setChecked(getVideoCallingFlagVZW(mContext) > 0);
        }

    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        updateScreenSetting();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deleteMobileDataObserver();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        Log.d(TAG, "onPreferenceTreeClick");

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        boolean mIsChecked = ((Boolean)objValue).booleanValue();
        boolean mIsVideoDlg = Settings.System.getInt(getActivity()
                .getContentResolver(), "advanced_allow_checkbox", 0) > 0;

        if (preference == mHdVoice) {
            mIsChecked = ((Boolean)objValue).booleanValue();
            Log.d(TAG, "onPreferenceChange mHdVoice " + mIsChecked);
            AdvancedCalling.updateCallSettingByKey(mContext, null,
                    mIsChecked ? 1 : 0, mCALL_ORDER_PRIORITY);
            mSwitch.setChecked(mIsChecked);
        } else if (preference == mVideoCalling) {
            mIsChecked = ((Boolean)objValue).booleanValue();
            Log.d(TAG, "onPreferenceChange mVideoCalling " + mIsChecked);

            // show Dialog
            if (!mIsVideoDlg && !mIsChecked) {
                showAdvancedVideoCallingDlg();
            } else {
                setVideoCallingFlagVZW(mContext, mIsChecked ? 1 : 0);
            }

        }
        return true;
    }

    private void showAdvancedVideoCallingDlg() {
        /**
         * T-4/C-1/C-11/B-5
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Title (T-4)
        builder.setTitle(getString(R.string.sp_dlg_note_NORMAL));

        // Contents (C-1/C-11)
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View frame_wrapper = inflater.inflate(com.lge.R.layout.dialog_c_frame2,
                null);
        LinearLayout c_frame = (LinearLayout)frame_wrapper
                .findViewById(android.R.id.content);
        View c_1 = inflater
                .inflate(com.lge.R.layout.dialog_c_1, c_frame, false);
        View c_11 = inflater.inflate(com.lge.R.layout.dialog_c_11, c_frame,
                false);

        c_frame.addView(c_1);
        c_frame.addView(c_11);

        ((TextView)c_1.findViewById(android.R.id.text1))
                .setText(getString(R.string.vt_setting_video_calling_off_msg));

        CheckBox checkBox = (CheckBox)c_11.findViewById(android.R.id.checkbox);
        checkBox.setText(getString(R.string.sp_data_popup_checked));
        checkBox.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), "advanced_allow_checkbox", 0) > 0);
        mCount = 0;
        mAllowCheckBox = false;
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dummy listener for click sound
                Log.d(TAG, "checkBox click");
                if (mCount % 2 == 0) {
                    mAllowCheckBox = true;
                } else {
                    mAllowCheckBox = false;
                }
                mCount++;
            }
        });

        builder.setView(frame_wrapper);

        // Button (B-5)
        builder.setPositiveButton(getString(R.string.dlg_ok),
                new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Log.d(TAG, "OK click");
                Log.d(TAG, "mAllowCheckBox " + mAllowCheckBox);
                if (mAllowCheckBox) {
                    Settings.System.putInt(getActivity().getContentResolver(),
                            "advanced_allow_checkbox",
                            true == mAllowCheckBox ? 1 : 0);
                }
                setVideoCallingFlagVZW(mContext, 0);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                mVideoCalling.setChecked(getVideoCallingFlagVZW(mContext) > 0);
            }

        });
        builder.show();
    }

    public static final String VT_AUTHORITY = "com.lge.vt.provider.VTSettingContentProvider";
    public static final Uri VT_CONTENT_URI = Uri.parse("content://"
            + VT_AUTHORITY + "/vt_setting");
    // column string
    public static final String VT_VIDEO_CALLING_VZW = "vt_video_calling_vzw";
    // column index
    public static final int VT_VIDEO_CALLING_VZW_COL = 7; // modify for VZW

    // column string
    public static final String VT_VIDEO_CALLING_VZW_SHOWN = "vt_video_calling_vzw_shown";
    // Integer(0:un-showing, 1:now showing)
    // column index
    public static final int VT_VIDEO_CALLING_VZW_SHOWN_COL = 11;

    private static int query(Context context, int columnIndex, int defaultValue) {
        int returnValue = defaultValue;

        Cursor cursor = getCursor(context);
        if (cursor != null) {
            try {
                returnValue = cursor.getInt(columnIndex);
            } finally {
                cursor.close();
            }
        }

        return returnValue;
    }

    private static boolean updateDB(Context context, String column, int value) {
        Log.d(TAG, "updateDB() : column=" + column + ", value=" + value);
        ContentResolver cr = context.getContentResolver();

        ContentValues newValues = new ContentValues();
        newValues.put(column, value);

        try {
            cr.update(VT_CONTENT_URI, newValues, null, null);
            return true;

        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Cursor getCursor(Context context) {
        int queryedCount = 0;

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(VT_CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            queryedCount = cursor.getCount();
            cursor.moveToFirst();

            Log.d(TAG, " Query Count:" + queryedCount);

            if (queryedCount != 0) {
                return cursor;
            }

            cursor.close();
        }

        return null;
    }

    public static int getVideoCallingFlagShownVZW(Context context) {
        return query(context, VT_VIDEO_CALLING_VZW_SHOWN_COL, 0);
    }

    public static int getVideoCallingFlagVZW(Context context) {
        return query(context, VT_VIDEO_CALLING_VZW_COL, 0);
    }

    public static void setVideoCallingFlagVZW(Context context, int value) {
        updateDB(context, VT_VIDEO_CALLING_VZW, value);
    }

    // call setting provider api
    private static final String mCALL_ORDER_PRIORITY = "call_order_priority";
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_VALUE_STR = "value_str";
    public static final String KEY_VALUE_INT = "value_int";
    private static final String CALL_AUTHORITY = "com.android.phone.CallSettingsProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://"
            + CALL_AUTHORITY + "/callsettings");
    public static final String[] CALLSETTINGS_PROJECTION = new String[] {
            KEY_ID, KEY_NAME, KEY_VALUE_STR, KEY_VALUE_INT };

    public static void updateCallSettingByKey(Context context,
            String value_str, int value_int, String key) {
        Log.d(TAG, "updateCallSettingByKey(" + key + ") : value_str is "
                + value_str + ", value_int is " + value_int);
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
                CALLSETTINGS_PROJECTION, selection, null, null);
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
        Log.d(TAG, "queryCallSettingValueByKey(" + key + ") : return value is "
                + value);
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
        settingsCursor = mContext.getContentResolver().query(
                GLOBAL_CONTENT_URI, null, "(" + Settings.Global.NAME + "=?)",
                new String[] { Global.MOBILE_DATA }, null);
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
            }
        });
    }

    public boolean isVtEnabled(Context context) {
        final Uri mCONTENT_URI = Uri
                .parse("content://com.lge.ims.provider.uc/ucstate");
        int value = 0;
        value = queryIMSIntegerValue(context, mCONTENT_URI, "vt_enabled");
        Log.i(TAG, "isVtEnabled : value = " + value);
        return (value == 1) ? true : false;
    }

    public boolean isEabFlag(Context context) {
        final Uri mCONTENT_URI = Uri
                .parse("content://com.lge.ims.provider.lgims/lgims_com_vzw_service_eab");
        int value = 0;
        value = queryIMSIntegerValue(context, mCONTENT_URI, "presence_eab_flag");
        Log.i(TAG, "isEabFlag : value = " + value);
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
                SLog.i("queryIMSIntegerValue() : key value is = " + value
                        + ", index = " + index);
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

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

    }

}
