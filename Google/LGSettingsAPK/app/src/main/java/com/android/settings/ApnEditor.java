/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Telephony;
import android.telephony.SubscriptionManager;
//import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;
import com.lge.os.Build;
import com.lge.telephony.provider.TelephonyProxy;

import com.android.settings.utils.LGSubscriptionManager;

// kiseok.son, 2012-07-18, KU5900 TD#120211 : Not select display the string.
public class ApnEditor extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    private final static String TAG = ApnEditor.class.getSimpleName();

    private final static String SAVED_POS = "pos";
    private final static String KEY_AUTH_TYPE = "auth_type";
    private final static String KEY_PROTOCOL = "apn_protocol";
    private final static String KEY_ROAMING_PROTOCOL = "apn_roaming_protocol";
    private final static String KEY_CARRIER_ENABLED = "carrier_enabled";
    private final static String KEY_BEARER = "bearer";
    private final static String KEY_MVNO_TYPE = "mvno_type";

    private static final int MENU_DELETE = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;
    private static final int MENU_CANCEL = Menu.FIRST + 2;
    private static final int MENU_BACK = android.R.id.home;
    private static final int ERROR_DIALOG_ID = 0;

    private static String sNotSet;
    private EditTextPreference mName;
    private EditTextPreference mApn;
    private EditTextPreference mProxy;
    private EditTextPreference mPort;
    private EditTextPreference mUser;
    private EditTextPreference mServer;
    private EditTextPreference mPassword;
    private EditTextPreference mMmsc;
    private EditTextPreference mMcc;
    private EditTextPreference mMnc;
    private EditTextPreference mMmsProxy;
    private EditTextPreference mMmsPort;
    private ListPreference mAuthType;
    private EditTextPreference mApnType;
    private ListPreference mProtocol;
    private ListPreference mRoamingProtocol;
    private CheckBoxPreference mCarrierEnabled;
    private ListPreference mBearer;

    private ListPreference mMvnoType;
    private EditTextPreference mMvnoMatchData;

    // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
    private EditTextPreference mPrimaryDNS;
    private EditTextPreference mSecondaryDNS;
    // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
    private EditTextPreference mPPPDialingNumber;
    
    private String mCurMnc;
    private String mCurMcc;
    private long mSubId;

    private String mNameCTC;
    private boolean mIsGsmCTC = false;

    private Uri mUri;
    private Cursor mCursor;
    private boolean mNewApn;
    private boolean mFirstTime;
    private Resources mRes;
    private TelephonyManager mTelephonyManager;

    public static final String LGU_MCCMNC = "45006"; //shlee1219 20120723

    /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
    private Integer mDefaultSetting = 0;
    private Integer mUserCreateSetting = 0;
    /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */

    //add the sprint apn requirement.
    public static final String SPR_MCCMNC = "310120";
    private Integer mReadOnlySetting = 0;
    public boolean mSPR_IOT_MENU = false;
    //add the vzw requirement
    public static final String VZW_MCCMNC1 = "311480";
    public static final String VZW_MCCMNC2 = "20404";

    /**
     * Standard projection for the interesting columns of a normal note.
     */
    private static final String[] sProjection = new String[] {
            Telephony.Carriers._ID, // 0
            Telephony.Carriers.NAME, // 1
            Telephony.Carriers.APN, // 2
            Telephony.Carriers.PROXY, // 3
            Telephony.Carriers.PORT, // 4
            Telephony.Carriers.USER, // 5
            Telephony.Carriers.SERVER, // 6
            Telephony.Carriers.PASSWORD, // 7
            Telephony.Carriers.MMSC, // 8
            Telephony.Carriers.MCC, // 9
            Telephony.Carriers.MNC, // 10
            Telephony.Carriers.NUMERIC, // 11
            Telephony.Carriers.MMSPROXY, // 12
            Telephony.Carriers.MMSPORT, // 13
            Telephony.Carriers.AUTH_TYPE, // 14
            Telephony.Carriers.TYPE, // 15
            Telephony.Carriers.PROTOCOL, // 16
            Telephony.Carriers.CARRIER_ENABLED, // 17
            Telephony.Carriers.BEARER, // 18
            Telephony.Carriers.ROAMING_PROTOCOL, // 19
            Telephony.Carriers.MVNO_TYPE, // 20
            Telephony.Carriers.MVNO_MATCH_DATA, // 21
            /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
            TelephonyProxy.Carriers.DEFAULTSETTING, // 22
            TelephonyProxy.Carriers.USERCREATESETTING, // 23
            /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */
    };

    private static final int ID_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int APN_INDEX = 2;
    private static final int PROXY_INDEX = 3;
    private static final int PORT_INDEX = 4;
    private static final int USER_INDEX = 5;
    private static final int SERVER_INDEX = 6;
    private static final int PASSWORD_INDEX = 7;
    private static final int MMSC_INDEX = 8;
    private static final int MCC_INDEX = 9;
    private static final int MNC_INDEX = 10;
    private static final int MMSPROXY_INDEX = 12;
    private static final int MMSPORT_INDEX = 13;
    private static final int AUTH_TYPE_INDEX = 14;
    private static final int TYPE_INDEX = 15;
    private static final int PROTOCOL_INDEX = 16;
    private static final int CARRIER_ENABLED_INDEX = 17;
    private static final int BEARER_INDEX = 18;
    private static final int ROAMING_PROTOCOL_INDEX = 19;
    private static final int MVNO_TYPE_INDEX = 20;
    private static final int MVNO_MATCH_DATA_INDEX = 21;

    /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
    private static final int DEFAULTSETTING_INDEX = 22;
    private static final int USERCREATESETTING_INDEX = 23;

    private static final int PPP_DIALING_NUMBER_INDEX = 24;
    
    /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */

    private static final int DNS1_INDEX = 24;
    private static final int DNS2_INDEX = 25;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        SLog.i("onCreate : start");
        addPreferencesFromResource(R.xml.apn_editor);

        //add the sprint apn requirement.
        if ("SPR".equals(Config.getOperator())) {
            mSPR_IOT_MENU = "1".equals(SystemProperties.get("sys.iothidden", "0"));
            SLog.i("mSPR_IOT_MENU = " + mSPR_IOT_MENU);
            //Log.d(TAG, "onCreate received SPR_IOT_MENU (false is limited state acooring to SIM, true is eidt possible (android original )):" + mSPR_IOT_MENU);
        }

        /*[START] 20121107 jun02.kim@lge.com Added ActionBar Back key*/
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (Utils.supportSplitView(getApplicationContext()) 
                && (false == Utils.isUI_4_1_model(getApplicationContext()))) {
                actionBar.setIcon(R.mipmap.ic_launcher_settings);
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        /*[END] 20121107 jun02.kim@lge.com Added ActionBar Back key*/
        sNotSet = getResources().getString(R.string.apn_not_set);
        mName = (EditTextPreference)findPreference("apn_name");
        mApn = (EditTextPreference)findPreference("apn_apn");
        mProxy = (EditTextPreference)findPreference("apn_http_proxy");
        mPort = (EditTextPreference)findPreference("apn_http_port");
        mUser = (EditTextPreference)findPreference("apn_user");

        mServer = (EditTextPreference)findPreference("apn_server");
        if (false == "VZW".equals(Config.getOperator())
                && false == "DCM".equals(Config.getOperator())
                && false == "CA".equals(Config.getCountry())) {
            getPreferenceScreen().removePreference(mServer);
        }

        mPassword = (EditTextPreference)findPreference("apn_password");
        mMmsProxy = (EditTextPreference)findPreference("apn_mms_proxy");
        mMmsPort = (EditTextPreference)findPreference("apn_mms_port");
        mMmsc = (EditTextPreference)findPreference("apn_mmsc");
        mMcc = (EditTextPreference)findPreference("apn_mcc");
        mMnc = (EditTextPreference)findPreference("apn_mnc");
        mApnType = (EditTextPreference)findPreference("apn_type");

        mAuthType = (ListPreference)findPreference(KEY_AUTH_TYPE);
        mAuthType.setOnPreferenceChangeListener(this);

        mProtocol = (ListPreference)findPreference(KEY_PROTOCOL);
        mProtocol.setOnPreferenceChangeListener(this);

        if (true == "DCM".equals(Config.getOperator())) {
            getPreferenceScreen().removePreference(mProtocol);
        } else if (SystemProperties.get("ro.build.product").equals("i_eu")) {
            getPreferenceScreen().removePreference(mProtocol);
        }

        mRoamingProtocol = (ListPreference)findPreference(KEY_ROAMING_PROTOCOL);

        // Deleted JB 4.2 MobileNetwork +
        // Only enable this on CDMA phones for now, since it may cause problems
        // on other phone
        // types. (This screen is not normally accessible on CDMA phones, but is
        // useful for
        // testing.)
        //TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //if (tm.getCurrentPhoneType() == Phone.PHONE_TYPE_CDMA) {
        mRoamingProtocol.setOnPreferenceChangeListener(this);
        //} else {
        //    getPreferenceScreen().removePreference(mRoamingProtocol);
        //}
        // Deleted JB 4.2 MobileNetwork -

        /* 2013-12-02 juhyup.kim@lge.com LGP_DATA_APN_DISABLE_PROTOCOL_UI [START] */
        if ("BELL".equals(Config.getOperator())) {
            Log.v(TAG, "Do not show Protocol and Roaming Protocol UI");
            getPreferenceScreen().removePreference(mProtocol);
            getPreferenceScreen().removePreference(mRoamingProtocol);
        }
        /* 2013-12-02 juhyup.kim@lge.com LGP_DATA_APN_DISABLE_PROTOCOL_UI [END] */

        mCarrierEnabled = (CheckBoxPreference)findPreference(KEY_CARRIER_ENABLED);
        if (false == "VZW".equals(Config.getOperator())) {
            getPreferenceScreen().removePreference(mCarrierEnabled);
        }

        mBearer = (ListPreference)findPreference(KEY_BEARER);
        mBearer.setOnPreferenceChangeListener(this);
        if (false == "VZW".equals(Config.getOperator())
                && false == "LGU".equals(Config.getOperator())) {
            getPreferenceScreen().removePreference(mBearer);
        }

        mMvnoType = (ListPreference)findPreference(KEY_MVNO_TYPE);
        mMvnoType.setOnPreferenceChangeListener(this);
        mMvnoMatchData = (EditTextPreference)findPreference("mvno_match_data");

        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        mPrimaryDNS = (EditTextPreference)findPreference("primary_DNS");
        mSecondaryDNS = (EditTextPreference)findPreference("secondary_DNS");
        if (!"KDDI".equals(Config.getOperator())) {
            getPreferenceScreen().removePreference(mPrimaryDNS);
            getPreferenceScreen().removePreference(mSecondaryDNS);
        }
        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.

        mPPPDialingNumber = (EditTextPreference)findPreference("ppp_dialing_number_key");
        String mOperatorNumeric = getIntent().getStringExtra("operator_numeric");
        mIsGsmCTC = getIntent().getBooleanExtra("is_gsm_state", true);
        SLog.i("mOperatorNumeric = " + mOperatorNumeric);
        SLog.i("mIsGsmCTC = " + mIsGsmCTC);
        if (("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator()))
                && mIsGsmCTC) {
            mAuthType.setEntries(
                    R.array.apn_auth_entries_gsm);
            mAuthType.setEntryValues(
                    R.array.apn_auth_values_gsm);
        }
        if (false == (("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator()))
                && ("46003".equals(mOperatorNumeric)))) {
            SLog.i("remove mPPPDialingNumber");
            getPreferenceScreen().removePreference(mPPPDialingNumber);
        }

        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
        mUserCreateSetting = TelephonyProxy.Carriers.USERCREATESETTING_PRELOADED;
        final int mLineLength_255 = 255;
        final int mLineLength_127 = 127;
        final int mLineLength_100 = 100;

        mName.getEditText().setFilters(
                new InputFilter[] {
                new InputFilter.LengthFilter(mLineLength_255) });
        mApn.getEditText().setFilters(
                new InputFilter[] {
                new InputFilter.LengthFilter(mLineLength_100) });
        mUser.getEditText().setFilters(
                new InputFilter[] {
                new InputFilter.LengthFilter(mLineLength_127) });
        mPassword.getEditText().setFilters(
                new InputFilter[] {
                new InputFilter.LengthFilter(mLineLength_127) });
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */

        mRes = getResources();

        final Intent intent = getIntent();
        final String action = intent.getAction();

        // Read the subscription received from Phone settings.
        /*[START] jun02.kim@lge.com added overlay 2012.08.24*/
        /*
        mSubId = getIntent().getLongExtra("subscription",
                OverlayUtils.getDefaultSubscription());
        */
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mSubId = getIntent().getLongExtra("subscription",
                LGSubscriptionManager.getDefaultSubIdForL());
        } else {
            mSubId = (long) getIntent().getIntExtra("subscription",
                LGSubscriptionManager.getDefaultSubIdForLMR1());
        }
        /*[END] jun02.kim@lge.com added overlay 2012.08.24*/

        Log.d(TAG, "ApnEditor onCreate received sub: " + mSubId);

        mFirstTime = icicle == null;

        SLog.i("action = " + action);

        if (action.equals(Intent.ACTION_EDIT)) {
            mUri = intent.getData();

            /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
            try {
                mCursor = managedQuery(mUri, sProjection, null, null);
                if (null != mCursor) {
                    mCursor.moveToFirst();
                    mDefaultSetting = mCursor.getInt(DEFAULTSETTING_INDEX);
                    mUserCreateSetting = mCursor.getInt(USERCREATESETTING_INDEX);
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception e: " + e);
            }
            Log.v(TAG, "mDefalutSetting value: " + mDefaultSetting);
            Log.v(TAG, "mUserCreateSetting value: " + mUserCreateSetting);

            //add the sprint apn requirement.
            String strSimOperator = TelephonyProxy.Carriers.getNumeric(mSubId);
            Log.v(TAG, "Numeric value: " + strSimOperator);

            mReadOnlySetting = 0;
            if ("SPR".equals(Config.getOperator())
                    && true == SPR_MCCMNC.equals(strSimOperator)
                    && mSPR_IOT_MENU == false) {
                mReadOnlySetting = 1;
                SLog.i("Sprint : read only");
            }

            final int mDefaultOn = 1;
            if (mDefaultSetting == TelephonyProxy.Carriers.DEFAULTSETTING_NOT_EDITABLE
                    || mDefaultSetting == mDefaultOn
                    || mReadOnlySetting == mDefaultOn) {
                Log.v(TAG, "This is the Default Setting.");
                mName.setEnabled(false);
                mApn.setEnabled(false);
                mProxy.setEnabled(false);
                mPort.setEnabled(false);
                mUser.setEnabled(false);

                if (null != mServer) {
                    mServer.setEnabled(false);
                }

                mPassword.setEnabled(false);
                mMmsProxy.setEnabled(false);
                mMmsPort.setEnabled(false);
                mMmsc.setEnabled(false);
                mMcc.setEnabled(false);
                mMnc.setEnabled(false);
                mApnType.setEnabled(false);
                mAuthType.setEnabled(false);

                if (null != mProtocol) {
                    mProtocol.setEnabled(false);
                }

                mRoamingProtocol.setEnabled(false);

                if (null != mCarrierEnabled) {
                    mCarrierEnabled.setEnabled(false);
                }

                if (null != mBearer) {
                    mBearer.setEnabled(false);
                }

                // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                mPrimaryDNS.setEnabled(false);
                mSecondaryDNS.setEnabled(false);
                // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.

                mPPPDialingNumber.setEnabled(false);

                mName.setShouldDisableView(false);
                mApn.setShouldDisableView(false);
                mProxy.setShouldDisableView(false);
                mPort.setShouldDisableView(false);
                mUser.setShouldDisableView(false);

                if (null != mServer) {
                    mServer.setShouldDisableView(false);
                }

                mPassword.setShouldDisableView(false);
                mMmsProxy.setShouldDisableView(false);
                mMmsPort.setShouldDisableView(false);
                mMmsc.setShouldDisableView(false);
                mMcc.setShouldDisableView(false);
                mMnc.setShouldDisableView(false);
                mApnType.setShouldDisableView(false);
                mAuthType.setShouldDisableView(false);

                if (null != mProtocol) {
                    mProtocol.setShouldDisableView(false);
                }

                mRoamingProtocol.setShouldDisableView(false);

                if (null != mCarrierEnabled) {
                    mCarrierEnabled.setShouldDisableView(false);
                }

                if (null != mBearer) {
                    mBearer.setShouldDisableView(false);
                }

                if (null != mMvnoType) {
                    mMvnoType.setEnabled(false);
                    mMvnoType.setShouldDisableView(false);
                }

                if (null != mMvnoMatchData) {
                    mMvnoMatchData.setEnabled(false);
                    mMvnoMatchData.setShouldDisableView(false);
                }

                // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                mPrimaryDNS.setShouldDisableView(false);
                mSecondaryDNS.setShouldDisableView(false);
                // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                mPPPDialingNumber.setShouldDisableView(false);
            }
            /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */

            if ("VZW".equals(Config.getOperator())) {
                if (null != mCarrierEnabled) {
                    mCarrierEnabled.setEnabled(true);
                }
            } // kerry

        } else if (action.equals(Intent.ACTION_INSERT)) {

            /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START] */
            mUserCreateSetting = TelephonyProxy.Carriers.USERCREATESETTING_MANUAL;
            /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END] */
            if (mFirstTime || icicle.getInt(SAVED_POS) == 0) {
                mUri = getContentResolver().insert(intent.getData(),
                        new ContentValues());
            } else {
                mUri = ContentUris.withAppendedId(
                        Telephony.Carriers.CONTENT_URI,
                        icicle.getInt(SAVED_POS));
            }

            mNewApn = true;
            // If we were unable to create a new note, then just finish
            // this activity. A RESULT_CANCELED will be sent back to the
            // original activity if they requested a result.
            if (mUri == null) {
                Log.w(TAG, "Failed to insert new telephony provider into "
                        + getIntent().getData());
                finish();
                return;
            }

            // The new entry was created, so assume all will end well and
            // set the result to be returned.
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));

        } else {
            SLog.i("ApnEditor finish  ====");
            finish();
            return;
        }

        if ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
            makeProjectionForCTC();
            mCursor = managedQuery(mUri, mProjectionForCTC, null, null);
        } else if ("KDDI".equals(Config.getOperator())) {
            makeProjectionForKDDI();
            mCursor = managedQuery(mUri, mProjectionForKDDI, null, null);
        } else {
            mCursor = managedQuery(mUri, sProjection, null, null);
        }
        
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
        if (mCursor == null) {
            SLog.w(TAG, "Null cursor with Uri= " + mUri);
            finish();
            return;
        }

        if (mCursor.moveToFirst() == false) {
            SLog.w(TAG, "Cursor is empty ");
            finish();
            return;
        }
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */

        mTelephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);

        if ("USC".equals(Config.getOperator()) 
            || "LRA".equals(Config.getOperator())
            || "ACG".equals(Config.getOperator())) {
            fillUi(LGSubscriptionManager.getTelephonyProperty(mSubId,
                        "gsm.apn.sim.operator.numeric"));
        } else {
            fillUi(intent.getStringExtra(ApnSettings.OPERATOR_NUMERIC_EXTRA));
        }
        SLog.i("onCreate : end");
        if ("ATT".equals(Config.getOperator2()) || "CRK".equals(Config.getOperator2())) {
            if (action.equals(Intent.ACTION_INSERT)
            // || mUserCreateSetting == TelephonyProxy.Carriers.USERCREATESETTING_MANUAL
            ) {
                String operator = TelephonyProxy.Carriers.getNumeric(mSubId);
                if (operator.equals("310410")
                        || operator.equals("310150")
                        || operator.equals("310170")
                        || operator.equals("310560")
                        || operator.equals("311180")) {
                    mApnType.setText("default,hipri");
                    mApnType.setSummary("default,hipri");
                }
            }
        } else if ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
            if (action.equals(Intent.ACTION_INSERT)) {
                mApnType.setText("default");
                mApnType.setSummary("default");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
        if (mUri == null) {
            mUri = getContentResolver().insert(Telephony.Carriers.CONTENT_URI,
                    new ContentValues());
        }
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */
        SLog.i("onResume ");
    }

    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
        SLog.i("onResume ");

    }

    private void fillUi(String defaultOperatorNumeric) {
        if (mFirstTime) {
            mFirstTime = false;
            // Fill in all the values from the db in both text editor and
            // summary
            if (null != mCursor) {
                mName.setText(mCursor.getString(NAME_INDEX));
                mApn.setText(mCursor.getString(APN_INDEX));
                mProxy.setText(mCursor.getString(PROXY_INDEX));
                mPort.setText(mCursor.getString(PORT_INDEX));
                mUser.setText(mCursor.getString(USER_INDEX));

                if (null != mServer) {
                    mServer.setText(mCursor.getString(SERVER_INDEX));
                }

                mPassword.setText(mCursor.getString(PASSWORD_INDEX));
                mMmsProxy.setText(mCursor.getString(MMSPROXY_INDEX));
                mMmsPort.setText(mCursor.getString(MMSPORT_INDEX));
                mMmsc.setText(mCursor.getString(MMSC_INDEX));
                mMcc.setText(mCursor.getString(MCC_INDEX));
                mMnc.setText(mCursor.getString(MNC_INDEX));
                mApnType.setText(mCursor.getString(TYPE_INDEX));

            if ("TW".equals(Config.getCountry())) {
                String farapn = null;
                if ( mApn.getText().toString().equals("internet") && mMcc.getText().toString().equals("466") && mMnc.getText().toString().equals("01") ) {
                    farapn = getResources().getString(R.string.sp_apn_fareastone_internet_summary);
                    mName.setText(farapn);
                }
                if ( mApn.getText().toString().equals("fetnet01") && mMcc.getText().toString().equals("466") && mMnc.getText().toString().equals("01") ) {
                    farapn = getResources().getString(R.string.sp_apn_fareastone_mms_summary);
                    mName.setText(farapn);
                }
            }


                if ("CN".equals(Config.getCountry())) {
                    String mOperatorNumeric = getIntent().getStringExtra("operator_numeric");
                    SLog.i("mOperatorNumeric = " + mOperatorNumeric);
                    SLog.i("mName = " + mNameCTC);
                    if (mUserCreateSetting == 
                                TelephonyProxy.Carriers.USERCREATESETTING_PRELOADED) {
                        String ctapn = mApn.getText().toString();
                        mNameCTC = mName.getText().toString();
                        if ("ctnet".equals(ctapn) 
                            && "CTNET".equals(mNameCTC)) {
                            ctapn = getResources().getString(R.string.sp_apn_ctnet_title);
                            mName.setText(ctapn);
                        } else if ("ctwap".equals(ctapn) 
                            && "CTWAP".equals(mNameCTC)) {
                            ctapn = getResources().getString(R.string.sp_apn_ctwap_title);
                            mName.setText(ctapn);
                        }
                    }
                }

                if (mNewApn) {
                    final int mLengthOperatorNumeric = 4;
                    final int mLengthMccMnc = 3;
                    if ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
                        defaultOperatorNumeric = getIntent().getStringExtra("operator_numeric");
                        SLog.i("defaultOperatorNumeric = " + defaultOperatorNumeric);
                    }
                    // MCC is first 3 chars and then in 2 - 3 chars of MNC
                    if (defaultOperatorNumeric != null
                            && (defaultOperatorNumeric.length()
                            > mLengthOperatorNumeric)) {
                        // Country code
                        String mcc = defaultOperatorNumeric.substring(0, mLengthMccMnc);
                        // Network code
                        String mnc = defaultOperatorNumeric.substring(mLengthMccMnc);
                        // Auto populate MNC and MCC for new entries, based on what SIM reports
                        mMcc.setText(mcc);
                        mMnc.setText(mnc);
                        mCurMnc = mnc;
                        mCurMcc = mcc;
                    }
                }
                int authVal = mCursor.getInt(AUTH_TYPE_INDEX);
                if (authVal != -1) {
                    mAuthType.setValueIndex(authVal);
                }
                /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
                else {
                    if (mNewApn) {
                        Log.d("APNEditor", "NewAPN AuthVal set NoAuth");
               /* 2014-08-13 mounesh.b@lge.com TLF UK: PAP to be set as auth type by default [START]  */
                        boolean isTlfCom
                            = ("TLF".equals(Config.getOperator()) && "COM".equals(Config.getCountry()));
                        String simmccmnc      = TelephonyProxy.Carriers.getNumeric(mSubId);
                        if (isTlfCom && simmccmnc.equals("23410")) {
                            mAuthType.setValueIndex(1);  // 1 - PAP
                            Log.d("APNEditor", "NewAPN AuthVal set");
                        } else {
                           mAuthType.setValue(null);
                        }
               /* 2014-08-13 mounesh.b@lge.com TLF UK: PAP to be set as auth type by default [END]  */
                    }
                }
                /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */

                if (null != mProtocol) {
                    mProtocol.setValue(mCursor.getString(PROTOCOL_INDEX));
                }

                mRoamingProtocol
                        .setValue(mCursor.getString(ROAMING_PROTOCOL_INDEX));
                if (mNewApn
                        && ("ATT".equals(Config.getOperator())
                                || "CRK".equals(Config.getOperator2()))
                        && (SystemProperties.getBoolean("persist.lg.data.IPV6Support", false))) {
                    SLog.i("set protocol ipv4v6");
                    mProtocol.setValue("IPV4V6");
                    mRoamingProtocol.setValue("IPV4V6");
                }
                // String value = mCursor.getString(CARRIER_ENABLED_INDEX);
                // juno.jung fix
                boolean value = (mCursor.getInt(CARRIER_ENABLED_INDEX) == 1);

                if (null != mCarrierEnabled) {
                    mCarrierEnabled.setChecked(value);
                }

                if (null != mBearer) {
                    mBearer.setValue(mCursor.getString(BEARER_INDEX));
                }

                /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]    */
                if (mNewApn) {
                    if (null != mMvnoType) {
                        mMvnoType.setValue(TelephonyProxy.Carriers.getMvnoType(mSubId));
                    }
                    if (null != mMvnoMatchData) {
                        mMvnoMatchData.setEnabled(false);
                        mMvnoMatchData.setText(TelephonyProxy.Carriers.getMvnoData(mSubId));
                    }
                } else {
                    if (null != mMvnoType) {
                        mMvnoType.setValue(mCursor.getString(MVNO_TYPE_INDEX));
                    }
                    if (null != mMvnoMatchData) {
                        mMvnoMatchData.setEnabled(false);
                        mMvnoMatchData.setText(mCursor.getString(MVNO_MATCH_DATA_INDEX));
                    }
                }
                /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */

                // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                if ("KDDI".equals(Config.getOperator())) {
                    mPrimaryDNS.setText(mCursor.getString(DNS1_INDEX));
                    mSecondaryDNS.setText(mCursor.getString(DNS2_INDEX));
                }
                // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.

                if ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
                    mPPPDialingNumber.setText(mCursor.getString(PPP_DIALING_NUMBER_INDEX));
                }
            }
        }

        mName.setSummary(checkNull(mName.getText()));
        mApn.setSummary(checkNull(mApn.getText()));
        mProxy.setSummary(checkNull(mProxy.getText()));
        mPort.setSummary(checkNull(mPort.getText()));
        mUser.setSummary(checkNull(mUser.getText()));

        if (null != mServer) {
            mServer.setSummary(checkNull(mServer.getText()));
        }

        mPassword.setSummary(starify(mPassword.getText()));
        mMmsProxy.setSummary(checkNull(mMmsProxy.getText()));
        mMmsPort.setSummary(checkNull(mMmsPort.getText()));
        mMmsc.setSummary(checkNull(mMmsc.getText()));
        mMcc.setSummary(checkNull(mMcc.getText()));
        mMnc.setSummary(checkNull(mMnc.getText()));
        mApnType.setSummary(checkNull(mApnType.getText()));
        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        if ("KDDI".equals(Config.getOperator())) {
            mPrimaryDNS.setSummary(checkNull(mPrimaryDNS.getText()));
            mSecondaryDNS.setSummary(checkNull(mSecondaryDNS.getText()));
        }
        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        if ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
            mPPPDialingNumber.setSummary(checkNull(mPPPDialingNumber.getText()));
        }

        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-18, KU5900 TD#120211 : Not select display the string.
        mName.setOnPreferenceClickListener(this);
        mApn.setOnPreferenceClickListener(this);
        mProxy.setOnPreferenceClickListener(this);
        mUser.setOnPreferenceClickListener(this);
        if (null != mServer) {
            mServer.setOnPreferenceClickListener(this);
        }
        mPassword.setOnPreferenceClickListener(this);
        mMmsProxy.setOnPreferenceClickListener(this);
        mMmsPort.setOnPreferenceClickListener(this);
        mMmsc.setOnPreferenceClickListener(this);
        mMcc.setOnPreferenceClickListener(this);
        mMnc.setOnPreferenceClickListener(this);
        mApnType.setOnPreferenceClickListener(this);
        mName.setOnPreferenceClickListener(this);
        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-18, KU5900 TD#120211 : Not select display the string.
        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        if ("KDDI".equals(Config.getOperator())) {
            mPrimaryDNS.setOnPreferenceClickListener(this);
            mSecondaryDNS.setOnPreferenceClickListener(this);
        }
        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        if ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
            mPPPDialingNumber.setOnPreferenceClickListener(this);
        }

        String authVal = mAuthType.getValue();
        String authtype = null; // PAP to be set as default authtype for TLF UK
        if (authVal != null) {
            int authValIndex = Integer.parseInt(authVal);
            mAuthType.setValueIndex(authValIndex);

            String[] values = mRes.getStringArray(R.array.apn_auth_entries);
            mAuthType.setSummary(values[authValIndex]);
            authtype = values[authValIndex]; // PAP to be set as default authtype for TLF UK
        } else {
            mAuthType.setSummary(sNotSet);
        }

        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
        if (mNewApn) {
            Log.d("APNEditor", "NewAPN AuthVal set NoAuth");
            /* 2014-08-13 mounesh.b@lge.com TLF UK: PAP to be set as auth type by default [START]  */
            boolean isTlfCom
                = ("TLF".equals(Config.getOperator()) && "COM".equals(Config.getCountry()));
            String simmccmnc      = TelephonyProxy.Carriers.getNumeric(mSubId);
             if (isTlfCom && simmccmnc.equals("23410")) {
                   mAuthType.setSummary(authtype);
                   Log.d("APNEditor", "NewAPN Summary for AuthVal set ");
             } else {
                   mAuthType.setSummary(sNotSet);
                   Log.d("APNEditor", "NewAPN Summary for AuthVal NOT set ");
             }
             /* 2014-08-13 mounesh.b@lge.com TLF UK: PAP to be set as auth type by default [END]  */
        }
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */

        if (null != mProtocol) {
            mProtocol.setSummary(checkNull(protocolDescription(
                    mProtocol.getValue(), mProtocol)));
        }

        mRoamingProtocol.setSummary(checkNull(protocolDescription(
                mRoamingProtocol.getValue(), mRoamingProtocol)));

        if (null != mBearer) {
            mBearer.setSummary(checkNull(bearerDescription(mBearer.getValue())));
        }
        if (null != mMvnoType) {
            mMvnoType.setSummary(
                checkNull(mvnoDescription(mMvnoType.getValue())));
        }
        if (null != mMvnoMatchData) {
            mMvnoMatchData.setSummary(checkNull(mMvnoMatchData.getText()));
        }

        if ("VZW".equals(Config.getOperator())) {
            if (mName.getSummary().toString().equals("VZWINTERNET")) {
                getPreferenceScreen().removePreference(mName);
                getPreferenceScreen().removePreference(mProxy);
                getPreferenceScreen().removePreference(mPort);
                getPreferenceScreen().removePreference(mUser);
                getPreferenceScreen().removePreference(mPassword);

                if (null != mServer) {
                    getPreferenceScreen().removePreference(mServer);
                }

                getPreferenceScreen().removePreference(mMmsc);
                getPreferenceScreen().removePreference(mMmsProxy);
                getPreferenceScreen().removePreference(mMmsPort);
                getPreferenceScreen().removePreference(mMcc);
                getPreferenceScreen().removePreference(mMnc);
                getPreferenceScreen().removePreference(mAuthType);
                getPreferenceScreen().removePreference(mApnType);

                if (null != mBearer) {
                    getPreferenceScreen().removePreference(mBearer);
                }

                if (null != mProtocol) {
                    getPreferenceScreen().removePreference(mProtocol);
                }

                if (null != mCarrierEnabled) {
                    getPreferenceScreen().removePreference(mCarrierEnabled);
                }

                getPreferenceScreen().removePreference(mRoamingProtocol);
                // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                getPreferenceScreen().removePreference(mPrimaryDNS);
                getPreferenceScreen().removePreference(mSecondaryDNS);
                // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                getPreferenceScreen().removePreference(mPPPDialingNumber);
            }
        }
        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        else if (true == "VZW".equals(Config.getOperator())
                || true == "TMO".equals(Config.getOperator())
                || true == "DCM".equals(Config.getOperator())) {
            getPreferenceScreen().removePreference(mRoamingProtocol);
        } else if ("ATT".equals(Config.getOperator()) && Utils.isTablet()) {
            // To remove the apn field about MMS requested by att e7 model team.
            getPreferenceScreen().removePreference(mMmsProxy);
            getPreferenceScreen().removePreference(mMmsPort);
            getPreferenceScreen().removePreference(mMmsc);
        }

        /* 2013-12-02 juhyup.kim@lge.com LGP_DATA_APN_DISABLE_PROTOCOL_UI [START] */
        if (!(SystemProperties.getBoolean("persist.lg.data.IPV6Support", false))) {
            //CDR-CDS-160
            if ("ATT".equals(Config.getOperator2()) || "CRK".equals(Config.getOperator2())) {
                SLog.i(TAG, "Do not show Protocol and Roaming Protocol UI");
                getPreferenceScreen().removePreference(mProtocol);
                getPreferenceScreen().removePreference(mRoamingProtocol);
            }
        }
        /* 2013-12-02 juhyup.kim@lge.com LGP_DATA_APN_DISABLE_PROTOCOL_UI [END] */

        //Not applied the Mvno/MvnoMatchData.
        if (null != mMvnoType) {
            getPreferenceScreen().removePreference(mMvnoType);
        }
        if (null != mMvnoMatchData) {
            getPreferenceScreen().removePreference(mMvnoMatchData);
        }
        //[END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.

        // allow user to edit carrier_enabled for some APN
        boolean ceEditable = getResources().getBoolean(R.bool.config_allow_edit_carrier_enabled);
        if (false == "VZW".equals(Config.getOperator())) {
            if (ceEditable) {
                mCarrierEnabled.setEnabled(true);
            } else {
                mCarrierEnabled.setEnabled(false);
            }
        }
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            boolean apnLocked;
            apnLocked = Settings.Global.getInt(getContentResolver(), "apn_locked", 0) == 1;

            boolean lockMode;
            lockMode = Settings.Global.getInt(getContentResolver(),
                                              "apn_lock_mode", 0) == 1;

            if (apnLocked || lockMode) {
                mName.setEnabled(false);
                mApn.setEnabled(false);
                mProxy.setEnabled(false);
                mPort.setEnabled(false);
                mUser.setEnabled(false);
                mServer.setEnabled(false);
                mPassword.setEnabled(false);
                mMmsProxy.setEnabled(false);
                mMmsPort.setEnabled(false);
                mMmsc.setEnabled(false);
                mMcc.setEnabled(false);
                mMnc.setEnabled(false);
                mApnType.setEnabled(false);
                mAuthType.setEnabled(false);
                mProtocol.setEnabled(false);
                mRoamingProtocol.setEnabled(false);
                mBearer.setEnabled(false);
                mMvnoType.setEnabled(false);
                mCarrierEnabled.setEnabled(false);
            }
        }
    }

    /**
     * Returns the UI choice (e.g., "IPv4/IPv6") corresponding to the given raw
     * value of the protocol preference (e.g., "IPV4V6"). If unknown, return
     * null.
     */
    private String protocolDescription(String raw, ListPreference protocol) {
        int protocolIndex = protocol.findIndexOfValue(raw);
        if (protocolIndex == -1) {
            return null;
        } else {
            String[] values = mRes.getStringArray(R.array.apn_protocol_entries);
            try {
                return values[protocolIndex];
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
    }

    private String bearerDescription(String raw) {
        final int mDefaultIndex = -1;
        int mBearerIndex = 0;

        if (null != mBearer) {
            mBearerIndex = mBearer.findIndexOfValue(raw);
        }

        if (mBearerIndex == mDefaultIndex) {
            return null;
        } else {
            String[] values = mRes.getStringArray(R.array.bearer_entries);
            try {
                return values[mBearerIndex];
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
    }

    private String mvnoDescription(String newValue) {
        final int mDefaultIndex = -1;
        if (null == mMvnoType) {
            return null;
        }
        int mvnoIndex = mMvnoType.findIndexOfValue(newValue);
        String oldValue = mMvnoType.getValue();

        if (mvnoIndex == mDefaultIndex) {
            return null;
        } else {
            if (null == mMvnoMatchData) {
                return null;
            }
            String[] values = mRes.getStringArray(R.array.mvno_type_entries);
            if (values[mvnoIndex].equals("None")) {
                mMvnoMatchData.setEnabled(false);
            } else {
                mMvnoMatchData.setEnabled(true);
            }
            if (newValue != null && newValue.equals(oldValue) == false) {
                if (values[mvnoIndex].equals("SPN")) {
                    mMvnoMatchData.setText(mTelephonyManager.getSimOperatorName());
                } else if (values[mvnoIndex].equals("IMSI")) {
                    String numeric = LGSubscriptionManager.getSimOperator(mSubId);
                    mMvnoMatchData.setText(numeric + "x");
                } else if (values[mvnoIndex].equals("GID")) {
                    mMvnoMatchData.setText(LGSubscriptionManager.getGroupIdLevel1(mSubId));
                }
            }

            try {
                return values[mvnoIndex];
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (KEY_AUTH_TYPE.equals(key)) {
            try {
                int index = Integer.parseInt((String)newValue);
                mAuthType.setValueIndex(index);

                String[] values = mRes.getStringArray(R.array.apn_auth_entries);
                mAuthType.setSummary(values[index]);
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (KEY_PROTOCOL.equals(key)) {
            if (null != mProtocol) {
                String protocol = protocolDescription((String)newValue,
                        mProtocol);
                if (protocol == null) {
                    return false;
                }
                mProtocol.setSummary(protocol);
                // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                if ("KDDI".equals(Config.getOperator())) {
                    mProtocol.setValue("IPV4V6");
                } else {
                    mProtocol.setValue((String)newValue);
                }
                // [END_LGE_SETTINGS], MOD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
            }
        } else if (KEY_ROAMING_PROTOCOL.equals(key)) {
            String protocol = protocolDescription((String)newValue,
                    mRoamingProtocol);
            if (protocol == null) {
                return false;
            }
            mRoamingProtocol.setSummary(protocol);
            mRoamingProtocol.setValue((String)newValue);
        } else if (KEY_BEARER.equals(key)) {
            String bearer = bearerDescription((String)newValue);
            if (bearer == null) {
                return false;
            }

            if (null != mBearer) {
                mBearer.setValue((String)newValue);
                mBearer.setSummary(bearer);
            }
        } else if (KEY_MVNO_TYPE.equals(key)) {
            String mvno = mvnoDescription((String)newValue);
            if (mvno == null) {
                return false;
            }
            if (null != mMvnoType) {
                mMvnoType.setValue((String)newValue);
                mMvnoType.setSummary(mvno);
            }
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);



        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
        if (!mNewApn && mDefaultSetting == TelephonyProxy.Carriers.DEFAULTSETTING_NOT_EDITABLE) {
            Log.w(TAG, "This item is read-only");
            return true;
        }
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */

        // If it's a new APN, then cancel will delete the new entry in onPause
        //shlee1219 20120723 MPDN Data team Req START
        String strSimOperator = TelephonyProxy.Carriers.getNumeric(mSubId);;
        Boolean mpdn_enable = SystemProperties.get("ro.support_mpdn", "false").equals("true");
        //shlee1219 20120723 MPDN Data team Req END

        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START] */
        //add the sprint apn requirement.
        if (!mNewApn && (mDefaultSetting == 1 || mReadOnlySetting == 1)) {
            Log.w(TAG, "This item is read-only");
        } else {
            if (!mNewApn) {
                //shlee1219 20120723 MPDN Data team Req START
                if ("LGU".equals(Config.getOperator()) && LGU_MCCMNC.equals(strSimOperator)
                        && (mpdn_enable == true)) {
                    menu.add(0, MENU_DELETE, 0, R.string.menu_delete)
                            .setIcon(R.drawable.common_menu_delete_holo_dark).setEnabled(false);
                } else if ("VZW".equals(Config.getOperator()) 
                    && (VZW_MCCMNC1.equals(strSimOperator) || VZW_MCCMNC2.equals(strSimOperator))) {
                    // request by junsop.shin@lge.com [data part].
                    // http://mlm.lge.com/di/browse/PRD-5214
                // DCM 3LM MDM feature START
                } else if (!com.android.settings.lgesetting.Config.Config.THREELM_MDM || !isApnLockedBy3LM()) {
                // DCM 3LM MDM feature END
                        menu.add(0, MENU_DELETE, 0, R.string.menu_delete).setIcon(
                                R.drawable.common_menu_delete_holo_dark);
                    //shlee1219 20120723 MPDN Data team Req END
                }
            }
            // DCM 3LM MDM feature START
            if (!com.android.settings.lgesetting.Config.Config.THREELM_MDM || !isApnLockedBy3LM()) {
            // DCM 3LM MDM feature END
                menu.add(0, MENU_SAVE, 0, R.string.menu_save).setIcon(
                        android.R.drawable.ic_menu_save);
            }

            menu.add(0, MENU_CANCEL, 0, R.string.menu_cancel).setIcon(
                    android.R.drawable.ic_menu_close_clear_cancel);
        }
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END] */
        if ((Config.getOperator().equals("VZW")
            && com.lge.os.Build.LGUI_VERSION.RELEASE == com.lge.os.Build.LGUI_VERSION_NAMES.V4_1)
                || Utils.getResources().getBoolean(R.bool.config_settings_search_enable)) {
            getMenuInflater().inflate(R.menu.settings_search, menu);
        }
        return true;
    }

    private boolean isApnLockedBy3LM() {
        boolean apnLocked = false;
        boolean lockMode = false;
        apnLocked = Settings.Global.getInt(getContentResolver(), "apn_locked", 0) == 1;
        lockMode = Settings.Global.getInt(getContentResolver(), "apn_lock_mode", 0) == 1;
        return (apnLocked || lockMode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search) {
            switchToSearchResults();
        }
        switch (item.getItemId()) {
        case MENU_DELETE:
            deleteApn();
            return true;
        case MENU_SAVE:
            if (validateAndSave(false)) {
                finish();
            }
            return true;
        case MENU_CANCEL:
            if (mNewApn && mUri != null) { // hyk 13.6.24 double cancel problem fix
                getContentResolver().delete(mUri, null, null);
                mUri = null; //ask130503 ::   move the getContentResolver().delete () from validateAndSave () to onDestroy.
            }
            finish();
            return true;
        case MENU_BACK:
            finish();
            return true;
        default:
            break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void switchToSearchResults() {
        Intent intent = new Intent();
        intent.setAction("com.lge.settings.SETTINGS_SEARCH");
        intent.putExtra("search", true);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK: {
            // hyk 13.6.24 double cancel problem fix - [start]
            if (isFinishing()) {
                Log.v(TAG, "[hyk][onKeyDown] isFinishing() = " + isFinishing());
                return true;
            }
            // hyk 13.6.24 double cancel problem fix - [end]
            if (validateAndSave(false)) {

                if ("DCM".equals(Config.getOperator())) {
                    //add the dcm popup in setting network 4.0_b005.
                    String type = checkNotSet(mApnType.getText());
                    if (type.length() == 0 || type.toLowerCase().equals("*")) {
                        notifiyWarningApn();
                        return true;
                    }
                }
                finish();
            }

            return true;
        }
        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        if (validateAndSave(true)) {
            /*
             * LGSI_CHANGE_S : U0_APN_EDIT_FORCE_CLOSE 2012-02-17,
             * cecilia.fernandes@lge.com, Cursor Issue, TD126078 Repository :
             * android/vendor/lge/apps/Settings
             */
            if (null != mCursor) {
                final int currentPos = mCursor.getPosition();
                final int count = mCursor.getCount();
                try {
                    if ((currentPos >= count) || (currentPos < 0)) {
                        mCursor.moveToFirst();
                    }
                    icicle.putInt(SAVED_POS, mCursor.getInt(ID_INDEX));
                } catch (Exception e) {
                    Log.e("ApnEditor",
                            " An error occurred  on onSaveInstanceState : "
                                    + e.getMessage());
                }
            }
            /* LGSI_CHANGE_E: APN_EDIT_FORCE_CLOSE */
        }
    }

    /**
     * Check the key fields' validity and save if valid.
     *
     * @param force
     *            save even if the fields are not valid, if the app is being
     *            suspended
     * @return true if the data was saved
     */
    private boolean validateAndSave(boolean force) {
        String name = checkNotSet(mName.getText());
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
        String apn = checkNotSet(mApn.getText()).trim();
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */
        String mcc = checkNotSet(mMcc.getText());
        String mnc = checkNotSet(mMnc.getText());
        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        String CPAConnectionID = "";
        String CPAConnectionPW = "";
        String primaryDNS = checkNotSet(mPrimaryDNS.getText());
        String secondaryDNS = checkNotSet(mSecondaryDNS.getText());
        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.

        String mPPPDialingNumberText = checkNotSet(mPPPDialingNumber.getText());
        
        boolean mpdn_enable = SystemProperties.get("ro.support_mpdn", "false").equals("true");
        boolean isRoaming = "true".equals(SystemProperties
                .get(TelephonyProperties.PROPERTY_OPERATOR_ISROAMING));

        SLog.i("validateAndSave start");
        //att requirement, applies the data centric device.
        if ("ATT".equals(Config.getOperator())
                //&& !force
                && Utils.isTablet()) {
            Log.v(TAG, "[ATT Requirement] mApn = " + apn);
            if (isRestrictApnForATT(apn)) {
                Toast.makeText(ApnEditor.this,
                        R.string.sp_msg_not_save_apn_phone_for_att, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        //att requirement

        if (getErrorMsg() != null && !force) {
            showDialog(ERROR_DIALOG_ID);
            return false;
        }

        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
        //add the sprint apn requirement.
        if (mDefaultSetting == TelephonyProxy.Carriers.DEFAULTSETTING_NOT_EDITABLE
                || mReadOnlySetting == TelephonyProxy.Carriers.DEFAULTSETTING_NOT_EDITABLE) {
            return true;
        }
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */

        if ("ATT".equals(Config.getOperator2()) || "CRK".equals(Config.getOperator2())) {
            String operator = TelephonyProxy.Carriers.getNumeric(mSubId);
            if ((operator.equals("310410"))
                    || (operator.equals("310150"))
                    || (operator.equals("310170"))
                    || (operator.equals("310560"))
                    || (operator.equals("311180"))) {
                String type = checkNotSet(mApnType.getText());
                SLog.i("validateAndSave before apnType = " + type);
                boolean mValue = false;
                if (type.length() < 1) {
                    type = "default,hipri";
                } else if ("*".equals(type)) {
                    type = "default,mms,supl,hipri,fota";
                } else {
                    String mTempStr = type;
                    mTempStr = Utils.removeAllWhitespace(mTempStr);
                    mValue = Utils.existInTokens(mTempStr, "hipri");
                    type = mValue ? type : ("hipri," + type);
                    mValue = Utils.existInTokens(mTempStr, "default");
                    type = mValue ? type : ("default," + type);
                }
                SLog.i("validateAndSave after apnType = " + type);
                mApnType.setText(type);
            }
        }

        /* 2012-08-10 juhyup.lee@lge.com LGP_DATA_APN_ADD_APN_SCENARIO_TLS [START]  */
        if ("TLS".equals(Config.getOperator())) {
            String type = checkNotSet(mApnType.getText());
            if (type.length() < 1 && !mcc.equals("001") && !mnc.equals("01")) {
                mApnType.setText("default,mms,supl");
            }
        }
        /* 2012-08-10 juhyup.lee@lge.com LGP_DATA_APN_ADD_APN_SCENARIO_TLS [END]  */

        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        else if ("KDDI".equals(Config.getOperator())) {
            String type = checkNotSet(mApnType.getText());
            if (type.length() < 1) {
                mApnType.setText("default,mms,supl,hipri,dun");
            }
        }
        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        else if ("KR".equals(Config.getCountry())) {
            String type = checkNotSet(mApnType.getText());
            if ("45011".equals(TelephonyProxy.Carriers.getNumeric(mSubId))) {
                if ((type.length() < 1)) {
                    mApnType.setText("");
                } else if (type.equals("*")) {
                    mApnType.setText("");
                }
            } else if (("KT".equals(Config.getOperator())
                    || "SKT".equals(Config.getOperator())
                    || "LGU".equals(Config.getOperator())
                    ) && (mpdn_enable == true)) {
                //if mpdn is true, the device is applied the apn type as skt/kt by data team (bongsook.jeong)
                if (!isRoaming) {
                    if ((type.length() < 1)) {
                        mApnType.setText("default,mms,dun,hipri,supl,fota,cbs");
                    } else if (type.equals("*")) {
                        mApnType.setText("");
                    }
                }
            }
        }

        if (!mCursor.moveToFirst()) {
            Log.w(TAG,
                    "Could not go to the first row in the Cursor when saving data.");
            return false;
        }

        // If it's a new APN and a name or apn haven't been entered, then erase the entry
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
        if (force && mNewApn && (name.length() < 1 || apn.length() < 1)) {
            //ask130503 ::   move the getContentResolver().delete () from validateAndSave () to onDestroy.
            //getContentResolver().delete(mUri, null, null);
            /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */
            return false;
        }

        ContentValues values = new ContentValues();

        // Add a dummy name "Untitled", if the user exits the screen without
        // adding a name but
        // entered other information worth keeping.
        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        if ("KDDI".equals(Config.getOperator())) {
            values.put(Telephony.Carriers.NAME, name.length() < 1 ? getResources()
                    .getString(R.string.sp_vpn_manual_list) : name); //same KEY_APN_MANUAL title in apnsettings.java
        } else if ("CN".equals(Config.getCountry())) {
            String mOperatorNumeric = getIntent().getStringExtra("operator_numeric");
            if (mUserCreateSetting == 
                        TelephonyProxy.Carriers.USERCREATESETTING_PRELOADED) {
                String ctapn = mApn.getText().toString();
                String ctnetname = getResources().getString(R.string.sp_apn_ctnet_title);
                String ctwapname = getResources().getString(R.string.sp_apn_ctwap_title);
                if (("ctnet".equals(ctapn) && ctnetname.equals(name)) 
                    || ("ctwap".equals(ctapn) && ctwapname.equals(name))) {
                    name = mNameCTC;
                }
            }
            values.put(Telephony.Carriers.NAME, name.length() < 1 ? getResources()
                    .getString(R.string.untitled_apn) : name);
        } else {
            values.put(Telephony.Carriers.NAME, name.length() < 1 ? getResources()
                    .getString(R.string.untitled_apn) : name);
        }
        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        values.put(Telephony.Carriers.APN, apn);
        values.put(Telephony.Carriers.PROXY, checkNotSet(mProxy.getText()));
        values.put(Telephony.Carriers.PORT, checkNotSet(mPort.getText()));
        values.put(Telephony.Carriers.MMSPROXY,
                checkNotSet(mMmsProxy.getText()));
        values.put(Telephony.Carriers.MMSPORT, checkNotSet(mMmsPort.getText()));
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        if ("KDDI".equals(Config.getOperator())
                && !checkNotSet(mUser.getText()).equals("")) {
            CPAConnectionID = mUser.getText();
            values.put(Telephony.Carriers.USER, checkNotSet(CPAConnectionID.trim()));
        } else {
            values.put(Telephony.Carriers.USER, checkNotSet(mUser.getText()).trim());
        }
        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */
        if (null != mServer) {
            values.put(Telephony.Carriers.SERVER,
                    checkNotSet(mServer.getText()));
        }
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
        // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        if ("KDDI".equals(Config.getOperator())
                && !checkNotSet(mPassword.getText()).equals("")) {
            CPAConnectionPW = mPassword.getText();
            values.put(Telephony.Carriers.PASSWORD, checkNotSet(CPAConnectionPW.trim()));
        } else {
            values.put(Telephony.Carriers.PASSWORD,
                    checkNotSet(mPassword.getText()).trim());
        }
        // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */
        values.put(Telephony.Carriers.MMSC, checkNotSet(mMmsc.getText()));

        String authVal = mAuthType.getValue();
        if (authVal != null) {
            values.put(Telephony.Carriers.AUTH_TYPE, Integer.parseInt(authVal));
        }

        if (null != mProtocol)
        // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        {
            if ("KDDI".equals(Config.getOperator())) {
                values.put(Telephony.Carriers.PROTOCOL, "IPV4V6");
            } else {
                values.put(Telephony.Carriers.PROTOCOL,
                        checkNotSet(mProtocol.getValue()));
            }
        }
        // [END_LGE_SETTINGS], MOD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.12345

        values.put(Telephony.Carriers.ROAMING_PROTOCOL, checkNotSet(mRoamingProtocol.getValue()));

        values.put(Telephony.Carriers.TYPE, checkNotSet(mApnType.getText()));

        values.put(Telephony.Carriers.MCC, mcc);
        values.put(Telephony.Carriers.MNC, mnc);

        values.put(Telephony.Carriers.NUMERIC, mcc + mnc);

        if (mCurMnc != null && mCurMcc != null) {
            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
                if (mCurMnc.equals(mnc) && mCurMcc.equals(mcc) &&
                        mSubId == LGSubscriptionManager.getDefaultDataSubIdForL()) {
                    values.put(Telephony.Carriers.CURRENT, 1);
                }
            } else {
                if (mCurMnc.equals(mnc) && mCurMcc.equals(mcc) &&
                        mSubId == (long)LGSubscriptionManager.getDefaultSubIdForLMR1()) {
                    values.put(Telephony.Carriers.CURRENT, 1);
                }
            }
        }
        String bearerVal = null;

        if (null != mBearer) {
            bearerVal = mBearer.getValue();
        }

        if (bearerVal != null) {
            values.put(Telephony.Carriers.BEARER, Integer.parseInt(bearerVal));
        }

        if (null != mMvnoType) {
            values.put(Telephony.Carriers.MVNO_TYPE, checkNotSet(mMvnoType.getValue()));
        }
        if (null != mMvnoMatchData) {
            values.put(Telephony.Carriers.MVNO_MATCH_DATA, checkNotSet(mMvnoMatchData.getText()));
        }

        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
        values.put(TelephonyProxy.Carriers.DEFAULTSETTING, mDefaultSetting);
        values.put(TelephonyProxy.Carriers.USERCREATESETTING, mUserCreateSetting);
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */

        if ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
            values.put(PPP_DIALING_NUMBER, mPPPDialingNumberText);
        }

        if ("VZW".equals(Config.getOperator()) || mCarrierEnabled.isEnabled()) {
            values.put(Telephony.Carriers.CARRIER_ENABLED,
                mCarrierEnabled.isChecked() ? true : false);
        }
        if ("JP".equals(Config.getCountry()) && "KDDI".equals(Config.getOperator())) {
            SLog.i("KDDI put dns1/dns2"+primaryDNS+"   "+secondaryDNS);
            values.put("dns1", primaryDNS);
            values.put("dns2", secondaryDNS);
         }
        try {
            SLog.i("values = " + values);
            getContentResolver().update(mUri, values, null, null);
        } catch (Exception e) {
            Log.e(TAG, "An error occurred  on validateAndSave : "
                    + e.getMessage());
        }

        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.

        /* 2013-04-05 seungmin.jeong@lge.com [LGE_DATA][SYNC_3GPP_LEGACY_APN] sync 3gpp apn with legacy apn [START] */
        if ("VZW".equals(Config.getOperator())) {
            if (("311".equals(mcc)) && ("480".equals(mnc))) {
                Log.v(TAG,
                        "[LG_DATA] 3GPP APN Changed, addtional update for legacy apn. Changed APN type is : "
                                + checkNotSet(mApnType.getText()));
                sync3GPPLegacyApn("204", "04", values, checkNotSet(mApnType.getText()), "3");
            } else if (("204".equals(mcc)) && ("04".equals(mnc))) {
                Log.v(TAG,
                        "Legacy apn, addtional update for 3GPP APN Changed. Changed APN type is : "
                                + checkNotSet(mApnType.getText()));
                sync3GPPLegacyApn("311", "480", values, checkNotSet(mApnType.getText()), "0");
            }
            else {
                Log.v(TAG, "[LG_DATA] no sync");
            }
        }
        /* 2013-04-05 seungmin.jeong@lge.com [LGE_DATA][SYNC_3GPP_LEGACY_APN] sync 3gpp apn with legacy apn [END] */
        SLog.i("validateAndSave end");
       
        return true;
    }

    private String getErrorMsg() {
        String errorMsg = null;

        String name = checkNotSet(mName.getText());
        String apn = checkNotSet(mApn.getText());
        String mcc = checkNotSet(mMcc.getText());
        String mnc = checkNotSet(mMnc.getText());

        /* 2013-12-19 beney.kim@lge.com LGP_DATA_APN_ALLOW_EMPTY_IA_TYPE [START]  */
        String apnType = checkNotSet(mApnType.getText());
        /* 2013-12-19 beney.kim@lge.com LGP_DATA_APN_ALLOW_EMPTY_IA_TYPE [END]  */

        // LGE_CHANGE_S, [LGE_DATA_US_010] d3sw1-data@lge.com, 2012-02-07 <Entitlement Requirement FOR ATT>
        String type = checkNotSet(mApnType.getText());
        // LGE_CHANGE_E, [LGE_DATA_US_010] d3sw1-data@lge.com, 2012-02-07 <Entitlement Requirement FOR ATT>

        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        if ("KDDI".equals(Config.getOperator())) {
            if ((apn.length() == 0)) {
                errorMsg = mRes.getString(R.string.error_apn_empty);
            } else if (((apn.length() >= 1) && (true == apn.contains(" "))) ||
                    (true == apn.contains(".au-net.ne.jp"))) {
                errorMsg = mRes.getString(R.string.sp_format_not_support_NORMAL, apn);
            }
        } else {
            // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
            if (name.length() < 1) {
                errorMsg = mRes.getString(R.string.error_name_empty);
                /* 2013-12-19 beney.kim@lge.com LGP_DATA_APN_ALLOW_EMPTY_IA_TYPE [START]  */
            } else if ((apn.length() == 0) && (true != "ia".equalsIgnoreCase(apnType))) {
                /* 2013-12-19 beney.kim@lge.com LGP_DATA_APN_ALLOW_EMPTY_IA_TYPE [END]  */
                errorMsg = mRes.getString(R.string.error_apn_empty);
            } else if ((apn.length() >= 1) && (true == apn.contains(" "))) {
                errorMsg = mRes.getString(R.string.sp_format_not_support_NORMAL, apn);
            } else if (mcc.length() != 3) {
                errorMsg = mRes.getString(R.string.error_mcc_not3);
            } else if ((mnc.length() & 0xFFFE) != 2) {
                errorMsg = mRes.getString(R.string.error_mnc_not23);
            }
            // LGE_CHANGE_S, [LGE_DATA_US_010] d3sw1-data@lge.com, 2012-02-07 <Entitlement Requirement FOR ATT>
            else if (type.toLowerCase().equals("entitlement")) {
                errorMsg = mRes.getString(R.string.sp_no_entitlement_NORMAL);
            }
            // LGE_CHANGE_E, [LGE_DATA_US_010] d3sw1-data@lge.com, 2012-02-07 <Entitlement Requirement FOR ATT>
            else if ((apn.length() >= 1) && apn.toLowerCase().equals("ims.ktfwing.com")) {
                int apnEditOn = Settings.Global
                        .getInt(getContentResolver(), "apn_onoff_setting", 0);
                SLog.i("apnEditOn = " + apnEditOn);
                if (apnEditOn == 0) {
                    errorMsg = mRes.getString(R.string.sp_not_save_apn_msg);
                }
            }
            // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        }
        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        return errorMsg;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder mPopupBuilder;
        if (id == ERROR_DIALOG_ID) {
            String msg = getErrorMsg();
            mPopupBuilder = new AlertDialog.Builder(this).setTitle(R.string.sp_dlg_note_NORMAL)
                    .setPositiveButton(android.R.string.ok, null)
                    .setMessage(msg);
            if (false == Utils.isUI_4_1_model(getApplicationContext())) {
                mPopupBuilder.setIconAttribute(android.R.attr.alertDialogIcon);
            }
            return mPopupBuilder.create();
        }

        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        if (id == ERROR_DIALOG_ID) {
            String msg = getErrorMsg();

            if (msg != null) {
                ((AlertDialog)dialog).setMessage(msg);
            }
        }
    }

    //add the dcm popup in setting network 4.0_b005.
    private void notifiyWarningApn() {
        AlertDialog.Builder mPopupBuilder;
        mPopupBuilder = new AlertDialog.Builder(this)
                .setTitle(R.string.sp_dlg_note_NORMAL)
                .setMessage(getString(R.string.sp_apn_type_notice_dialog_NORMAL))
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                SLog.i("notifiyWarningApn = finish");
                                finish();
                            }
                        });
        if (false == Utils.isUI_4_1_model(getApplicationContext())) {
            mPopupBuilder.setIconAttribute(android.R.attr.alertDialogIcon);
        }
        mPopupBuilder.show();
    }

    private void deleteApn() {
        AlertDialog.Builder mPopupBuilder;
        // nver1029 APN Scenario change start
        mPopupBuilder = new AlertDialog.Builder(this)
                .setTitle(R.string.def_option_menu_remove)
                .setMessage(getString(R.string.sp_apn_deleted_popup_NORMAL))
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                if (mUri != null) { // hyk 13.6.24 double cancel problem fix
                                    getContentResolver().delete(mUri, null, null);
                                }
                                mUri = null; //ask130503 ::   move the getContentResolver().delete () from validateAndSave () to onDestroy.
                                finish();
                            }
                        })
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                Log.d(TAG, "DelteApn Cancel Button");
                            }
                        });
        if (false == Utils.isUI_4_1_model(getApplicationContext())) {
            mPopupBuilder.setIconAttribute(android.R.attr.alertDialogIcon);
        }
        mPopupBuilder.show();
        // nver1029 APN Scenario change end
        // getContentResolver().delete(mUri, null, null);
        // finish();
    }

    private String starify(String value) {
        if (value == null || value.length() == 0) {
            return sNotSet;
        } else {
            char[] password = new char[value.length()];
            for (int i = 0; i < password.length; i++) {
                password[i] = '*';
            }
            return new String(password);
        }
    }

    private String checkNull(String value) {
        if (value == null || value.length() == 0) {
            return sNotSet;
        } else {
            return value;
        }
    }

    private String checkNotSet(String value) {
        if (value == null || value.equals(sNotSet)) {
            return "";
        } else {
            return value;
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        Preference pref = findPreference(key);
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
        if (pref == mPassword) {
            pref.setSummary(starify(sharedPreferences.getString(key, "")));
        } else if (pref != null) {
            if (pref != mCarrierEnabled && pref != mProtocol && pref != mRoamingProtocol) {
                pref.setSummary(checkNull(sharedPreferences.getString(key, "")));
            }
        }
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */
    }

    // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-18, KU5900 TD#120211 : Not select display the string.
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);
        return false;
    }

    /**
     *  Implements OnPreferenceClickListener interface
     */
    public boolean onPreferenceClick(Preference pref) {
        EditTextPreference etp = (EditTextPreference)pref;
        EditText et = etp.getEditText();
        CharSequence text = et.getText();
        if (text != null && text.length() != 0) {
            et.setSelection(text.length());
            et.selectAll();
        }
        return false;
    }

    // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-18, KU5900 TD#120211 : Not select display the string.

    /* 2013-04-05 seungmin.jeong@lge.com [LGE_DATA][SYNC_3GPP_LEGACY_APN] sync 3gpp apn with legacy apn [START] */
    private void sync3GPPLegacyApn(String syncMcc, String syncMnc, ContentValues values,
            String apnType, String Bearer) {

        Cursor mLegacyCursor;

        String networkOperator = syncMcc + syncMnc;

        Log.v(TAG, "[LG_DATA] sync3GPPLegacyApn() Sync networkOperator : " + networkOperator);
        Log.v(TAG, "[LG_DATA] sync3GPPLegacyApn() Sync apnType : " + networkOperator);

        String where = "numeric = '" + networkOperator + "' and type = '" + apnType + "'";

        mLegacyCursor = getContentResolver().query(Telephony.Carriers.CONTENT_URI, sProjection,
                where, null, "_id");

        if (mLegacyCursor != null) {
            if (mLegacyCursor.getCount() != 0) {
                mLegacyCursor.moveToFirst();

                String key = mLegacyCursor.getString(ID_INDEX);
                int synPos = Integer.parseInt(key);
                Uri syncUrl = ContentUris.withAppendedId(Telephony.Carriers.CONTENT_URI, synPos);

                Log.d(TAG, "[LG_DATA] synPos : " + synPos);

                values.put(Telephony.Carriers.BEARER, Bearer);
                values.put(Telephony.Carriers.MCC, syncMcc);
                values.put(Telephony.Carriers.MNC, syncMnc);
                values.put(Telephony.Carriers.NUMERIC, networkOperator);

                int result = getContentResolver().update(syncUrl, values, null, null); //update!!

                Log.d(TAG, "[LG_DATA] update result : " + result);
            }
            else {
                Log.e(TAG, "[LG_DATA] Cursor count is 0");
            }
            mLegacyCursor.close();
        }
        else {
            Log.e(TAG, "[LG_DATA] Cursor is null");
        }
    }

    /* 2013-04-05 seungmin.jeong@lge.com [LGE_DATA][SYNC_3GPP_LEGACY_APN] sync 3gpp apn with legacy apn [END] */

    @Override
    protected void onDestroy() {
        //ask130503 :: + move the getContentResolver().delete () from validateAndSave () to onDestroy.
        //when the activity is recreated, the content resolver initialize.
        String name = checkNotSet(mName.getText());
        String apn = checkNotSet(mApn.getText()).trim();

        /* 2013-12-19 beney.kim@lge.com LGP_DATA_APN_ALLOW_EMPTY_IA_TYPE [START]  */
        boolean keepIaApn = false;
        String apnType = checkNotSet(mApnType.getText()).trim();
        if ("ia".equalsIgnoreCase(apnType))
        {
            keepIaApn = true;
        }
        /* 2013-12-19 beney.kim@lge.com LGP_DATA_APN_ALLOW_EMPTY_IA_TYPE [END]  */

        Preference pref = findPreference("apn_name");
        if (pref == null) {
            Log.e("ApnEditor", "onDestroy() skip apn_name check");
            /* 2013-12-19 beney.kim@lge.com LGP_DATA_APN_ALLOW_EMPTY_IA_TYPE [START]  */
            if (mUri != null && mNewApn && (apn.length() < 1 && !keepIaApn)) {
                /* 2013-12-19 beney.kim@lge.com LGP_DATA_APN_ALLOW_EMPTY_IA_TYPE [END]  */
                getContentResolver().delete(mUri, null, null);
                Log.e("ApnEditor", "onDestroy() contentResolve initialized");
            }
        } else {
            /* 2013-12-19 beney.kim@lge.com LGP_DATA_APN_ALLOW_EMPTY_IA_TYPE [START]  */
            if (mUri != null && mNewApn && (name.length() < 1 || (apn.length() < 1 && !keepIaApn))) {
                /* 2013-12-19 beney.kim@lge.com LGP_DATA_APN_ALLOW_EMPTY_IA_TYPE [END]  */
                getContentResolver().delete(mUri, null, null);
                Log.e("ApnEditor", "onDestroy() contentResolve initialized");
            }

        }
        //ask130503 :: - move the getContentResolver().delete () from validateAndSave () to onDestroy.

        super.onDestroy();
    }

    public boolean isRestrictApnForATT(String mApn) {
        boolean mValue = false;
        String mTempStr = mApn;
        SLog.i("isRestrictApnForATT apn = " + mApn);
        mTempStr = Utils.removeAllWhitespace(mTempStr);

        mValue = (Utils.existInTokens(mTempStr, "phone")
                    || Utils.existInTokens(mTempStr, "wap.cingular")
                    || Utils.existInTokens(mTempStr, "nxtgenphone") 
                    || Utils.existInTokens(mTempStr, "pta"));
        SLog.i("isRestrictApnForATT mValue = " + mValue);
        return mValue;
    }

    private String[] mProjectionForCTC;
    public static final String PPP_DIALING_NUMBER = "ppp_dialing_number";
    void makeProjectionForCTC() {
        mProjectionForCTC = new String[sProjection.length + 1];
        SLog.i("makeProjectionForCTC sProjection.length = " + sProjection.length);
        System.arraycopy(sProjection, 0, mProjectionForCTC, 0, sProjection.length);
        mProjectionForCTC[sProjection.length] = PPP_DIALING_NUMBER;
        SLog.i("makeProjectionForCTC = " + mProjectionForCTC);
    }

    private String[] mProjectionForKDDI;
    public static final String PRIMARY_DNS = "dns1";
    public static final String SECONDARY_DNS = "dns2";
    void makeProjectionForKDDI() {
        mProjectionForKDDI = new String[sProjection.length + 2];
        SLog.i("mProjectionForKDDI sProjection.length = " + sProjection.length);
        System.arraycopy(sProjection, 0, mProjectionForKDDI, 0, sProjection.length);
        mProjectionForKDDI[sProjection.length] = PRIMARY_DNS;
        mProjectionForKDDI[sProjection.length + 1] = SECONDARY_DNS;
        SLog.i("makeProjectionForKDDI = " + mProjectionForKDDI);
    }
}
