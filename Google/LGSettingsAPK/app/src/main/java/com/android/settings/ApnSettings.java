/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants; // Modified JB 4.2 MobileNetwork System ->
// Global
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lgesetting.Config.Config;

import com.lge.telephony.provider.TelephonyProxy;

import java.util.ArrayList;
import android.os.IDeviceManager3LM;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.android.settings.utils.LGSubscriptionManager;

public class ApnSettings extends PreferenceActivity implements
        // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-06, Add user create APN popup.
        Preference.OnPreferenceChangeListener, DialogInterface.OnClickListener {
    // [END_LGE_SETTINGS], MOD, kiseok.son, 2012-07-06, Add user create APN popup.
    static final String TAG = "ApnSettings";

    public static final String EXTRA_POSITION = "position";
    public static final String RESTORE_CARRIERS_URI =
            "content://telephony/carriers/restore";
    public static final String PREFERRED_APN_URI =
            "content://telephony/carriers/preferapn";
    public static final String OPERATOR_NUMERIC_EXTRA = "operator";
    public static final String APN_ID = "apn_id";

    private static final int ID_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int APN_INDEX = 2;
    private static final int TYPES_INDEX = 3;
    private static final int PROTOCOL_INDEX = 4;
    // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
    private static final int USER_INDEX = 5;
    private static final int USERCREATESETTING_INDEX = 6;
    private static final int BEARER_INDEX = 7; //add the sprint apn requirement.
    private static final int DEFAULTSETTING_INDEX = 8;
    // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.

    private static final int MENU_NEW = Menu.FIRST;
    private static final int MENU_RESTORE = Menu.FIRST + 1;

    private static final int EVENT_RESTORE_DEFAULTAPN_START = 1;
    private static final int EVENT_RESTORE_DEFAULTAPN_COMPLETE = 2;

    private static final int DIALOG_RESTORE_DEFAULTAPN = 1001;
    // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-06, Add user create APN popup.
    private static final int DIALOG_RESTORE_DEFAULTAPN_CHECK = 1002;
    // [END_LGE_SETTINGS], MOD, kiseok.son, 2012-07-06, Add user create APN popup.
    // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-09-05, Add the LGCPA_ menu.
    private static final int DIALOG_APN_NAME_CHECK = 1100;
    // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-09-05, Add the LGCPA_ menu.

    private static final Uri DEFAULTAPN_URI = Uri.parse(RESTORE_CARRIERS_URI);
    private static final Uri PREFERAPN_URI = Uri.parse(PREFERRED_APN_URI);

    private static boolean mRestoreDefaultApnMode;
    private static boolean sApnPause = false;
    private static boolean sApnDialog = false;

    private RestoreApnUiHandler mRestoreApnUiHandler;
    private RestoreApnProcessHandler mRestoreApnProcessHandler;
    private HandlerThread mRestoreDefaultApnThread; // Modified JB 4.2 MobileNetwork System -> Global

    private UserManager mUm;
    private long mSubId;
    private String mSelectedKey;
    private boolean mUseNvOperatorForEhrpd = SystemProperties.getBoolean(
            "persist.radio.use_nv_for_ehrpd", false);

    // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-09-05, Add the LGCPA_ menu.
    private String mPrevSelectedKey = "";;
    private String mUserManualKey = "";

    // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-09-05, Add the LGCPA_ menu.

    private IntentFilter mMobileStateFilter;
    private boolean mUnavailable;
    public static final String SKT_MCCMNC = "45005";
    public static final String KT_MCCMNC = "45008";
    public static final String LGU_MCCMNC = "45006";
    public static final String SPR_MCCMNC = "310120"; //add the sprint apn requirement.
    public static final String CT_MCCMNC = "46003"; //CT Requirement
    public static final String VZW_MCCMNC1 = "311480";
    public static final String VZW_MCCMNC2 = "20404";

    private boolean mDomesticApn = true;
    private boolean mRoamingApn = true; // for LGU
    private boolean mRoamingApnLte = true; // for LGU

    ServiceState mServiceState;

    /* LGE_CHANGE_S, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
    private int mTetheredSate = 1;
    /* LGE_CHANGE_E, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */

    public TelephonyManager mPhone;
    public PhoneStateListener mPhoneStateListener; // kerry - for dcm req
    boolean csActive = false;

    public Context mContext;

    //add the sprint apn requirement.
    public boolean mSPR_IOT_MENU = false;
    // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
    // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.

    public ContentObserver mObserverPreferredApn;

    private final BroadcastReceiver mMobileStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (sApnPause) {
                return;
            }
            if (intent.getAction().equals(
                    TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
                // Modified JB 4.2 MobileNetwork System -> Global
                //Phone.DataState state = getMobileDataState(intent);
                PhoneConstants.DataState state = getMobileDataState(intent);

                switch (state) {
                case CONNECTED:
                    if (!mRestoreDefaultApnMode) {
                        fillList();
                    } else {
                        showDialog(DIALOG_RESTORE_DEFAULTAPN);
                    }
                    break;
                default:
                    break;
                }
            }
            // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-09-05, Add the LGCPA_ menu.
            else if (intent.getAction().equals("lge.intent.action.DATA_CPA_CONNECT_FAIL")) {
                showDialog(DIALOG_APN_NAME_CHECK);
            }
            // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-09-05, Add the LGCPA_ menu.
            /* LGE_CHANGE_S, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
            else if (intent.getAction().equals(ConnectivityManager.ACTION_TETHER_STATE_CHANGED)) {
                //int State = intent.getIntExtra (ConnectivityManager.TETHERED_STATE, 0);
                //ArrayList<String> availableList = intent.getStringArrayListExtra(
                //        ConnectivityManager.EXTRA_AVAILABLE_TETHER);
                ArrayList<String> activeList = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ACTIVE_TETHER);
                //ArrayList<String> erroredList = intent.getStringArrayListExtra(
                //        ConnectivityManager.EXTRA_ERRORED_TETHER);
                //if( State == 0 || State == 1 ){
                if (null != activeList) {
                    if (activeList.size() > 0) {
                        if ("DCM".equals(Config.getOperator())) {
                            mTetheredSate = 0; // kerry - DCM requirements block apn change while tethering
                        }
                    } else {
                        mTetheredSate = 1;
                    }
                }
                fillList();
            }
            /* LGE_CHANGE_E, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
        }
    };

    // Modified JB 4.2 MobileNetwork System -> Global +
    /*
    private static Phone.DataState getMobileDataState(Intent intent) {
        String str = intent.getStringExtra(Phone.STATE_KEY);
        if (str != null) {
            return Enum.valueOf(Phone.DataState.class, str);
        } else {
            return Phone.DataState.DISCONNECTED;
        }
    } */
    private static PhoneConstants.DataState getMobileDataState(Intent intent) {
        String str = intent.getStringExtra(PhoneConstants.STATE_KEY);
        if (str != null) {
            return Enum.valueOf(PhoneConstants.DataState.class, str);
        } else {
            return PhoneConstants.DataState.DISCONNECTED;
        }
    }

    // Modified JB 4.2 MobileNetwork System -> Global -

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.no_apn);
        mUm = (UserManager)getSystemService(Context.USER_SERVICE);

        if (mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS)) {
            mUnavailable = true;
            setContentView(R.layout.apn_disallowed_preference_screen);
            return;
        }
        addPreferencesFromResource(R.xml.apn_settings);
        getListView().setItemsCanFocus(true);
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mSubId = getIntent().getLongExtra("subscription",
                LGSubscriptionManager.getDefaultSubIdForL());
        } else {
            mSubId = (long) getIntent().getIntExtra("subscription",
                LGSubscriptionManager.getDefaultSubIdForLMR1());
        }
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (Utils.supportSplitView(getApplicationContext())
                && (false == Utils.isUI_4_1_model(getApplicationContext()))) {
                actionBar.setIcon(R.mipmap.ic_launcher_settings);
            }
            actionBar.setTitle(R.string.sp_apn_settings_NORMAL);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mMobileStateFilter = new IntentFilter(
                TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
        mMobileStateFilter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        if ("JP".equals(Config.getCountry()) && "KDDI".equals(Config.getOperator())) {
            mMobileStateFilter.addAction("lge.intent.action.DATA_CPA_CONNECT_FAIL");
        }
        registerReceiver(mMobileStateReceiver, mMobileStateFilter);

        mObserverPreferredApn = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                String mStr = getSelectedApnKey();
                SLog.i("onChange mObserverPreferredApn mStr " + mStr);
                if (null != mSelectedKey
                        && false == mSelectedKey.equals(mStr)) {
                    SLog.i("mObserverPreferredApn update apn list = " + mStr);
                    fillList();
                }
            }
        };
        register(getContentResolver());
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if ("DCM".equals(Config.getOperator())) {
                    csActive = (state != TelephonyManager.CALL_STATE_IDLE) ? true : false;
                    fillList();
                }
            }
            @Override
            public void onDataActivity(int direction) {
            }

            @Override
            public void onServiceStateChanged(ServiceState state) {
                mServiceState = state;
                if ((null != mServiceState) &&
                    ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator()))) {
                    int mRadioState = mINVALID_DATA_RADIO;
                    mRadioState = dataRadio(mServiceState);
                    if ((mRadioState != mINVALID_DATA_RADIO) && mConnectNwMode != mRadioState) {
                        mConnectNwMode = mRadioState;
                        fillList();
                    }
                }
            }
        };
        mPhone = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume received subId :" + mSubId);
        super.onResume();
        if (sApnPause) {
            sApnPause = false;
        }
        if (sApnDialog) {
            completeRestore();
            sApnDialog = false;
        }
        if (mUnavailable) {
            return;
        }

        //add the sprint apn requirement.
        if ("SPR".equals(Config.getOperator())) {
            mSPR_IOT_MENU = "1".equals(SystemProperties.get("sys.iothidden", "0"));
            SLog.i("mSPR_IOT_MENU = " + mSPR_IOT_MENU);
            //Log.d(TAG, "onCreate received SPR_IOT_MENU (false is limited state acooring to SIM, true is eidt possible (android original )):" + mSPR_IOT_MENU);
        }
        //ask130402 :: +
        if (OverlayUtils.isMultiSimEnabled()) {
            OverlayUtils.set_MsimTelephonyListener(getApplicationContext(), mPhoneStateListener);
        } else {
            Utils.set_TelephonyListener(mPhone, mPhoneStateListener);
        }
        //ask130402 :: -

        if ("DCM".equals(Config.getOperator())
                && mPhone.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
            csActive = true;
        } else {
            csActive = false;
        }

        if (!mRestoreDefaultApnMode) {
            fillList();
        } else {
            showDialog(DIALOG_RESTORE_DEFAULTAPN);
        }
    }

    private Uri getUri(Uri uri) {
        return Uri.withAppendedPath(uri, "/subId/" + mSubId);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mSubId = getIntent().getLongExtra("subscription",
                LGSubscriptionManager.getDefaultSubIdForL());
        } else {
            mSubId = (long) getIntent().getIntExtra("subscription",
                LGSubscriptionManager.getDefaultSubIdForLMR1());
        }
        Log.d(TAG, "onNewIntent received sub :" + mSubId);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "ApnSettings onPause");
        sApnPause = true;
        if (mUnavailable) {
            return;
        }

        //ask130402 :: +
        if (OverlayUtils.isMultiSimEnabled()) {
            OverlayUtils.release_MsimTelephonyListener(getApplicationContext(), mPhoneStateListener);
        } else {
            Utils.release_TelephonyListener(mPhone, mPhoneStateListener);
        }
        //ask130402 :: -

    }

    // Added JB 4.2 MobileNetwork +
    @Override
    public void onDestroy() {
        SLog.i("onDestroy");
        super.onDestroy();
        sApnPause = false;
        sApnDialog = false;
        if (mUnavailable) {
            return;
        }
        unregister(getContentResolver());
        unregisterReceiver(mMobileStateReceiver);
        if (mRestoreApnUiHandler != null) {
            mRestoreDefaultApnMode = false;
            mRestoreApnUiHandler.removeMessages(EVENT_RESTORE_DEFAULTAPN_COMPLETE);
        }
        if (mRestoreDefaultApnThread != null) {
            mRestoreDefaultApnThread.quit();
        }

        //add the sprint apn requirement.
        if ("SPR".equals(Config.getOperator())) {
            SystemProperties.set("sys.iothidden", "0");
            SLog.i("onDestroy :: sys.iothidden = 0");
        }

    }

    // Added JB 4.2 MobileNetwork -

    private void fillList() {

        String mSimNumeric = getSimNumeric(mServiceState);
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]    */
        //String where = "numeric ='" + TelephonyProxy.Carriers.getNumeric(mSubId)
        String where = "numeric ='" + mSimNumeric
                + "' and mvno_type ='" + TelephonyProxy.Carriers.getMvnoType(mSubId)
                + "' and mvno_match_data ='" + TelephonyProxy.Carriers.getMvnoData(mSubId)
                + "' and defaultsetting IS NOT '" + TelephonyProxy.Carriers.DEFAULTSETTING_HIDDEN
                + "' and carrier_enabled = '1'";

        Log.i(TAG, "fillList(): where=" + where);

        if (!TelephonyProxy.Carriers.getPersisAutoProfileKey().equals("")) {
            where = "(" + where + ") or " + TelephonyProxy.Carriers.USERCREATESETTING + " >= '"
                    + TelephonyProxy.Carriers.USERCREATESETTING_MANUAL + "'";
        }
        Log.i(TAG, "fillList(): where=" + where);
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */

        if ("SKT".equals(Config.getOperator())) {
            String strAttach = setSKTSBSMAPNSkipDisplayFilter(SKT_MCCMNC);
            if (strAttach != null) {
                where = where + strAttach;
            }
        }
        if ("LGU".equals(Config.getOperator())) {
            String strAttach = setLGUSBSMAPNSkipDisplayFilter(LGU_MCCMNC);
            if (strAttach != null) {
                where = where + strAttach;
            }
        }
        Log.d(TAG, "where : " + where);

        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        // Cursor cursor = getContentResolver().query(getUri(Telephony.Carriers.CONTENT_URI), new String[] {
        //      "_id", "name", "apn", "type", "protocol", "user", "bearer"}, where, null,
        //      Telephony.Carriers.DEFAULT_SORT_ORDER); //add the sprint apn requirement. "bearer"
        Cursor cursor = getContentResolver().query(
                getUri(Telephony.Carriers.CONTENT_URI),
                new String[] { "_id", "name", "apn", "type", "protocol", "user",
                        "usercreatesetting",
                        "bearer",
                        "defaultsetting" }, where, null, null);
        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.

        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.

        SLog.i("cusor = " + cursor);
        PreferenceGroup apnList = (PreferenceGroup)findPreference("apn_list");
        apnList.removeAll();
        // DCM 3LM MDM feature START
        ArrayList<Preference> unselectableApnList = new ArrayList<Preference>();
        // DCM 3LM MDM feature END

        ArrayList<Preference> mmsApnList = new ArrayList<Preference>();
        /* LGE_CHANGE_S, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
        if (mTetheredSate == 0) {
            mSelectedKey = null;
        } else {
            mSelectedKey = getSelectedApnKey();
        }

        /* LGE_CHANGE_E, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */

        mSelectedKey = getSelectedApnKey();
        mDomesticApn = true;
        mRoamingApn = true;

        mUserManualKey = " ";

        mRoamingApnLte = true;
        if (null != cursor) {
            try {
                // DCM 3LM MDM feature START
                IDeviceManager3LM dm = null;
                if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
                    dm = IDeviceManager3LM.Stub.asInterface(
                        ServiceManager.getService(Context.DEVICE_MANAGER_3LM_SERVICE));
                }
                // DCM 3LM MDM feature END

                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String name = cursor.getString(NAME_INDEX);
                    String apn = cursor.getString(APN_INDEX);
                    String key = cursor.getString(ID_INDEX);
                    String type = cursor.getString(TYPES_INDEX);
                    String protocol = cursor.getString(PROTOCOL_INDEX);

                    // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                    String user = cursor.getString(USER_INDEX);
                    //boolean isKddiCpa = false;
                    // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                    String userCreateSetting = cursor.getString(USERCREATESETTING_INDEX);
                    String mBearer = cursor.getString(BEARER_INDEX);
                    String mDefaultSetting = cursor.getString(DEFAULTSETTING_INDEX);
                    //add the sprint apn requirement.

                    SLog.i("name = " + name);
                    SLog.i("apn = " + apn);
                    SLog.i("key = " + key);
                    SLog.i("type = " + type);
                    SLog.i("protocol = " + protocol);
                    SLog.i("user = " + user);
                    SLog.i("mBearer = " + mBearer);
                    SLog.i("mDefaultSetting = " + mDefaultSetting);

                    ApnPreference pref = new ApnPreference(this, mSubId, mSimNumeric, isLTEOrEHRPD(mServiceState));

                    pref.setKey(key);
                    if (mSelectedKey == null) {
                        // unselect all if preferred APN is null
                        Log.i(TAG, "unselect all if preferred APN is null");
                        pref.setUnChecked();
                    }
                    if ("VZW".equals(Config.getOperator()) 
                        || "LRA".equals(Config.getOperator())) {
                        pref.setTitle(apn);

                        if (mTetheredSate == 0) {
                            pref.setUnChecked();
                        }
                        if ("VZWADMIN".equalsIgnoreCase(apn)) {
                            pref.setSummary("ADMINISTRATIVE PDN APN");
                        } else if ("VZWAPP".equalsIgnoreCase(apn)) {
                            pref.setSummary("APPLICATION PDN APN");
                        } else if ("VZWIMS".equalsIgnoreCase(apn)) {
                            pref.setSummary("IMS PDN APN");
                        } else if ("VZWINTERNET".equalsIgnoreCase(apn)) {
                            pref.setSummary("INTERNET PDN APN");
                        } else {
                            pref.setSummary(apn);
                        }
                    } else {
                        pref.setTitle(name);
                        if ( "TW".equals(Config.getCountry()) ) {
                            if ( apn.equals("internet") && mSimNumeric.equals("46601") ) {
                                pref.setTitle( getResources().getString( R.string.sp_apn_fareastone_internet_summary) );
                            }
                            if ( apn.equals("fetnet01")) {
                                pref.setTitle( getResources().getString( R.string.sp_apn_fareastone_mms_summary) );
                            }
                        }
                        if ("CN".equals(Config.getCountry()) 
                            && "0".equals(userCreateSetting)) {
                            if ("ctwap".equals(apn) && "CTWAP".equals(name)) {
                                pref.setTitle( getResources().getString( R.string.sp_apn_ctwap_title) );
                            } else if ( "ctnet".equals(apn) && "CTNET".equals(name)) {
                                pref.setTitle( getResources().getString( R.string.sp_apn_ctnet_title) );
                            }
                        }
                        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                        if ("KDDI".equals(Config.getOperator())) {
                        } else if ("SBM".equals(Config.getOperator()) 
                                    && "0".equals(userCreateSetting) 
                                    && "1".equals(mDefaultSetting)) {
                            pref.setSummary("**********");
                        } else {
                            pref.setSummary(apn);
                        }
                        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                    }
                    pref.setPersistent(false);
                    pref.setOnPreferenceChangeListener(this);

                    /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
                    //boolean selectable = ((type == null) || !type.equals("mms"));
                    boolean selectable = ((type == null) || (type.trim().equals(""))
                            || type.trim().contains("default") || type.trim().equals("*"));
                    /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */

                    if ("VZW".equals(Config.getOperator())
                        || "LRA".equals(Config.getOperator())) {
                        if ("admin".equalsIgnoreCase(type)) {
                        } else if ("vzwapp,mms,cbs".equalsIgnoreCase(type)) {
                        } else if ("ims".equalsIgnoreCase(type)) {
                        } else if ("emergency".equalsIgnoreCase(type)) {
                        } else if ("vzw800".equalsIgnoreCase(type)) {
                        } else {
                            apnList.addPreference(pref);
                        }
                        pref.setSelectable(selectable);
                        if (mTetheredSate == 0) {
                            pref.setUnChecked();
                        }
                        Log.d(TAG, "mSelectedKey = " + mSelectedKey + "selectable = " + selectable
                                + "type = " + type + "key = " + key);
                        if (selectable) {
                            if ((mSelectedKey != null) && mSelectedKey.equals(key)) {
                                pref.setChecked();
                            }
                            if (mTetheredSate == 0) {
                                pref.setEnabled(false);
                            }

                            //  apnList.addPreference(pref);
                        }
                    } else {
                        pref.setSelectable(selectable);
                        String strSimOperator = SystemProperties
                                .get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
                        boolean isRoaming = Config.isDataRoaming(mSubId);
                        boolean lockable = false;
                        int networkMode = Global.getInt(getContentResolver(),
                                Global.PREFERRED_NETWORK_MODE, 0 /*mPhone.NT_MODE_LTE_GSM_WCDMA*/);
                        boolean sktDontAdd = false;
                        final int NETWORK_MODE_WCDMA_PREF = 0; /* GSM/WCDMA (WCDMA preferred) */
                        final int NETWORK_MODE_LTE_GSM_WCDMA = 9; /* LTE, GSM/WCDMA */

                        boolean mpdn_enable = SystemProperties.get("ro.support_mpdn", "false")
                                .equals("true"); // kiseok.son 20120831 APN to KT.
                        int apnEditOn = Global.getInt(getContentResolver(), "apn_onoff_setting", 0); // kiseok.son 20120903 APN to KT

                        if ("SKT".equals(Config.getOperator()) && SKT_MCCMNC.equals(strSimOperator)) {
                            if (!isRoaming && ("lte.sktelecom.com".equals(apn))) {
                                if (mDomesticApn) {
                                    lockable = true;
                                    mDomesticApn = false;
                                }
                            } else if (isRoaming && ("lte.sktelecom.com".equals(apn))) {
                                sktDontAdd = true;
                            } else if (!isRoaming && ("web.sktelecom.com".equals(apn))) {
                                if (mDomesticApn) {
                                    lockable = true;
                                    mDomesticApn = false;
                                }
                            } else if (isRoaming && ("web.sktelecom.com".equals(apn))) {
                                sktDontAdd = true;
                            } else if (isRoaming && ("roaming.sktelecom.com".equals(apn))) {
                                if (mDomesticApn) {
                                    lockable = true;
                                    mDomesticApn = false;
                                }

                                if (NETWORK_MODE_LTE_GSM_WCDMA == networkMode) {
                                    sktDontAdd = true;
                                }
                            } else if (!isRoaming && ("roaming.sktelecom.com".equals(apn))) {
                                sktDontAdd = true;
                            } else if (isRoaming && ("lte-roaming.sktelecom.com".equals(apn))) {
                                if (mRoamingApn) {
                                    lockable = true;
                                    mRoamingApn = false;
                                }

                                if (NETWORK_MODE_WCDMA_PREF == networkMode) {
                                    sktDontAdd = true;
                                }
                            } else if (!isRoaming && ("lte-roaming.sktelecom.com".equals(apn))) {
                                sktDontAdd = true;
                            } else if ((mpdn_enable == true) && "ims".equals(apn)) {
                                // remove "ims" APN : kiseok.son 20120816
                                sktDontAdd = true;
                                selectable = true;
                            }
                        } else if ("KT".equals(Config.getOperator())
                                && KT_MCCMNC.equals(strSimOperator)) {
                            // ksson start for Vu2 : Add Locking image for ims APN. 2012-08-09
                            if (("lte150.ktfwing.com".equals(apn))
                                    || ("lte.ktfwing.com".equals(apn))
                                    || ("alwayson-r6.ktfwing.com".equals(apn))
                                    || ("ims.ktfwing.com".equals(apn))) {
                                // ksson end for Vu2 : Add Locking image for ims APN. 2012-08-09
                                // kiseok.son 20120831 APN to KT. - start
                                if (!isRoaming && mDomesticApn) {
                                    if (mpdn_enable && (type == null || "".equals(type))) {
                                        sktDontAdd = true; // here to KT value.
                                    }
                                } else if (isRoaming) {
                                    if (mpdn_enable && !(type == null || "".equals(type))) {
                                        sktDontAdd = true; // here to KT value.
                                    }
                                }
                                lockable = true;
                                //                                mDomesticApn = false;

                                if ((apnEditOn == 1) || userCreateSetting.equals("1")) {
                                    lockable = false;
                                }
                                // kiseok.son 20120831 APN to KT. - end
                            }
                        } else if ("LGU".equals(Config.getOperator())) {
                            Log.d(TAG, "apn" + apn);

                            if (isRoaming) {
                                if (("wroaming.lguplus.co.kr".equals(apn))) {
                                    if (mRoamingApn) {
                                        lockable = true;
                                        mRoamingApn = false;

                                        pref.setLockable(lockable);
                                    }
                                } else if ("lte-roaming.lguplus.co.kr".equals(apn)) {
                                    if (mRoamingApnLte) {
                                        lockable = true;
                                        mRoamingApnLte = false;

                                        pref.setLockable(lockable);
                                    }
                                } else if ("sinternet.lguplus.co.kr".equals(apn)) {
                                    if (mDomesticApn) {
                                        lockable = true;
                                        selectable = false;

                                        mDomesticApn = false;
                                    }
                                    else {
                                        lockable = false;
                                        selectable = true;
                                    }

                                    pref.setLockable(lockable);
                                    pref.setSelectable(selectable);
                                }
                            } else {
                                if (("sinternet.lguplus.co.kr".equals(apn))) {
                                    if (mDomesticApn) {
                                        lockable = true;
                                        mDomesticApn = false;

                                        pref.setLockable(lockable);
                                    }
                                } else if ("wroaming.lguplus.co.kr".equals(apn)) {
                                    if (mRoamingApn) {
                                        lockable = true;
                                        selectable = false;

                                        mRoamingApn = false;
                                    } else {
                                        lockable = false;
                                        selectable = true;
                                    }

                                    pref.setLockable(lockable);
                                    pref.setSelectable(selectable);
                                } else if ("lte-roaming.lguplus.co.kr".equals(apn)) {
                                    if (mRoamingApnLte) {
                                        lockable = true;
                                        selectable = false;

                                        mRoamingApnLte = false;
                                    } else {
                                        lockable = false;
                                        selectable = true;
                                    }

                                    pref.setLockable(lockable);
                                    pref.setSelectable(selectable);
                                }
                            }

                            if ("LG uplus Admin".equals(name) || "LG uplus Emergency".equals(name)) {
                                selectable = true;
                                sktDontAdd = true;
                            }
                            // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                        } else if ("CTC".equals(Config.getOperator())
                                || "CTO".equals(Config.getOperator())) {
                            if (false == isCTCSim(mServiceState)) {
                                SLog.i("skip to hide the apn list for ctc ");
                            } else if (null != mServiceState && false == isRoaming) {
                                //int mConnectNwMode = dataRadio(mServiceState);
                                SLog.i("mConnectNwMode = " + mConnectNwMode);
                                if ((true == "ctwap".equals(apn)) && (mGSM == mConnectNwMode)) {
                                    //lockable = true;
                                    sktDontAdd = true;
                                    SLog.i("hide apn = " + apn);
                                } else if ((true == "ctlte".equals(apn))
                                        && (mCDMA == mConnectNwMode)) {
                                    //lockable = true;
                                    sktDontAdd = true;
                                    SLog.i("hide apn = " + apn);
                                }
                            } else if (true == isRoaming) {
                                if (true == ("ctwap".equals(apn) || "ctlte".equals(apn))) {
                                    SLog.i("if roaming state, hide the " + apn);
                                    //lockable = true;
                                    sktDontAdd = true;
                                }
                            } else {
                                SLog.i("no svc apn ");
                            }
                            if (userCreateSetting.equals("1")) {
                                SLog.i("userCreateSetting = " + userCreateSetting);
                                lockable = false;
                                sktDontAdd = false;
                            }
                        } else if ("SBM".equals(Config.getOperator()) 
                                    && "0".equals(userCreateSetting) 
                                    && "1".equals(mDefaultSetting)) {
                                lockable = true;
                        }
                        if ("LGU".equals(Config.getOperator()) 
                            || "SKT".equals(Config.getOperator())
                            || "KT".equals(Config.getOperator())
                        ) {
                            if ("LG uplus Emergency".equals(name) 
                                || "SKT Emergency".equals(name) 
                                || "KT Emergency".equals(name)) {
                                SLog.i("hide the " + apn);
                                selectable = true;
                                sktDontAdd = true;
                            }
                        }
                        
                        if (false == "LGU".equals(Config.getOperator())) {
                            pref.setLockable(lockable);
                        }
                        /* LGE_CHANGE_S, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
                        if (mTetheredSate == 0) {
                            pref.setUnChecked();
                        }
                        /* LGE_CHANGE_E, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */

                        //add the sprint apn requirement.
                        if ("SPR".equals(Config.getOperator())) {
                            if (mBearer != null
                                    && false == mBearer.equals("3")
                                    && SPR_MCCMNC.equals(strSimOperator)
                                    && mSPR_IOT_MENU == false) {
                                sktDontAdd = true;
                                SLog.i("if unlock sim, not add the apn list");
                            }
                        }

                        Log.d(TAG, "mSelectedKey = " + mSelectedKey + "selectable = " + selectable
                                + "type = " + type + "key = " + key);
                        if (selectable) {
                            if ((mSelectedKey != null) && mSelectedKey.equals(key)) {
                                pref.setChecked();
                                // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-09-05, Add the LGCPA_ menu.
                                if ("KDDI".equals(Config.getOperator())) {
                                    if (false == userCreateSetting.equals("1")) {
                                        mPrevSelectedKey = mSelectedKey;
                                        SLog.i("filllist mPrevSelectedKey = " + mSelectedKey);
                                    }
                                }
                                // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-09-05, Add the LGCPA_ menu.
                            }
                            /* LGE_CHANGE_S, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
                            if (mTetheredSate == 0 || true == csActive) {
                                pref.setEnabled(false);
                            }
                            /* LGE_CHANGE_E, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */

                            if (false == sktDontAdd) {
                                // DCM 3LM MDM feature START
                                if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
                                    try {
                                    if (!dm.isApnSelectable(name)) {
                                        pref.setEnabled(false);
                                        unselectableApnList.add(pref);
                                    }
                                    else {
                                         apnList.addPreference(pref);
                                    }
                                    } catch (RemoteException e) {
                                        // Should never happen
                                        Log.e(TAG, "RemoteException : " + e);
                                    }
                                // DCM 3LM MDM feature END
                                } else {
                                    apnList.addPreference(pref);
                                }
                            }
                        } else {
                            /* LGE_CHANGE_S, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
                            if (mTetheredSate == 0 || true == csActive) {
                                pref.setEnabled(false);
                            }
                            /*
                             * LGE_CHANGE_E, donguk.ki@lge.com,
                             * 2011-04-11,<disable apn menu during tethering>
                             */
                            if ("ATT".equals(Config.getOperator())) {
                                if ((type != null)
                                        && (!type.equals("entitlement"))) {
                                    mmsApnList.add(pref);
                                }
                                // [START_LGE_SETTINGS], ADD, kiseok.son,
                                // 2012-07-13, Add the LGCPA_ menu.
                            } else if ("KDDI".equals(Config.getOperator())) {
                                // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-09-05, Add the LGCPA_ menu.
                                //if (false == userCreateSetting.equals("1")) {
                                //    mPrevSelectedKey = mSelectedKey;
                                //    SLog.i("filllist mPrevSelectedKey = " + mSelectedKey);
                                //}
                                // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-09-05, Add the LGCPA_ menu.
                            }
                            // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                            // kiseok.son 20120831 APN to KT. - start
                            else if ("KT".equals(Config.getOperator()) 
                                    || "CTC".equals(Config.getOperator())
                                    || "CTO".equals(Config.getOperator())) {
                                if (false == sktDontAdd) {
                                    apnList.addPreference(pref);
                                }
                            }
                            // kiseok.son 20120831 APN to KT. - end
                            // LGE_CHANGE_S, [LGE_DATA] global-wdata@lge.com, 2012-11-21 <Hide Dun Type FOR TMUS>
                            else if ("TMO".equals(Config.getOperator())
                                    && "US".equals(Config.getCountry())) {

                                if ((type != null) && (!type.equals("dun"))) {
                                    mmsApnList.add(pref);
                                }

                            } // LGE_CHANGE_E, [LGE_DATA] global-wdata@lge.com, 2012-11-21 <Hide Dun Type FOR TMUS>
                            else if ("SPR".equals(Config.getOperator())
                                    && SPR_MCCMNC.equals(strSimOperator)
                                    && mSPR_IOT_MENU == false) {
                                //add the sprint apn requirement.
                                SLog.i("SPR :: mBeaer = " + mBearer);
                                if (mBearer != null && true == mBearer.equals("3")) {
                                    mmsApnList.add(pref);
                                }
                            }
                            else {
                                mmsApnList.add(pref);
                            }
                        }
                    }
                    cursor.moveToNext();
                }
            } catch (Exception e) {
                // TODO: handle exception
                Log.e("ApnSettings",
                        " An error occurred  on fillList : "
                                + e.getMessage());
            } finally {
                cursor.close();
            }
        }
        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        // DCM 3LM MDM feature START
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            for (Preference preference : unselectableApnList) {
                apnList.addPreference(preference);
            }
        }
        // DCM 3LM MDM feature END
        for (Preference preference : mmsApnList) {
            apnList.addPreference(preference);
        }

        if (apnList.getPreferenceCount() > 0) {
            findViewById(R.id.no_apn_layout).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.no_apn_layout).setVisibility(View.VISIBLE);
        }

    }

    private void completeRestore() {
        fillList();
        getPreferenceScreen().setEnabled(true);
        mRestoreDefaultApnMode = false;
        removeDialog(DIALOG_RESTORE_DEFAULTAPN);
        Toast.makeText(
                this,
                getResources().getString(
                        R.string.restore_default_apn_completed),
                Toast.LENGTH_LONG).show();
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mUnavailable) {
            menu.add(0, MENU_NEW, 0,
                    getResources().getString(R.string.menu_new))
                    .setIcon(android.R.drawable.ic_menu_add)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add(0, MENU_RESTORE, 0,
                    getResources().getString(R.string.menu_restore))
                    .setIcon(android.R.drawable.ic_menu_upload);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }
     */

    /* LGE_CHANGE_S, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        super.onPrepareOptionsMenu(menu);

        if (mUnavailable) {
            return false;
        }

        //shlee1219 20120723 MPDN Data team Req START
        String strSimOperator =
                SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
        Boolean mpdn_enable = SystemProperties.get("ro.support_mpdn", "false")
                .equals("true");
        int apnEditOn = Global.getInt(getContentResolver(), "apn_onoff_setting", 0);
        //shlee1219 20120723 MPDN Data team Req END
        SLog.i("strSimOperator = " + strSimOperator);
        SLog.i("Config.getOperator() = " + Config.getOperator());
        menu.clear();
        if (mTetheredSate == 1) {
            /* LGE_CHANGE_E, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
            // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
            if ("LGU".equals(Config.getOperator())
                    && LGU_MCCMNC.equals(strSimOperator)
                    && (mpdn_enable == true) && (apnEditOn == 0)) {
                menu.add(0, MENU_NEW, 0,
                        getResources().getString(R.string.menu_new))
                        .setIcon(android.R.drawable.ic_menu_add)
                        .setEnabled(false); // shlee1219 20120723 MPDN Data team Req
            } else if ("SPR".equals(Config.getOperator())
                    && true == SPR_MCCMNC.equals(strSimOperator)
                    && mSPR_IOT_MENU == false) {
                //add the sprint apn requirement.
                //if unlock local sim, skip the new apn menu.
                SLog.i("if unlock local sim, skip the new apn menu.");
            } else if ("VZW".equals(Config.getOperator()) 
                && (VZW_MCCMNC1.equals(strSimOperator) || VZW_MCCMNC2.equals(strSimOperator))) {
                SLog.i("if Class 3 APN NI local sim, skip the new apn menu.");
                // reqs-LTE_DataDevices.docx
                // http://mlm.lge.com/di/browse/PRD-5214
            } else
            // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-13, Add the LGCPA_
            // menu.
            {
                menu.add(0, MENU_NEW, 0,
                        getResources().getString(R.string.menu_new)).setIcon(
                        android.R.drawable.ic_menu_add);
            }

            // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-06, Add user create APN popup.
            if (supportRestoreMenu()) { // shlee1219.lee
                // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-06, Add user create APN popup.
                menu.add(0, MENU_RESTORE, 0,
                        getResources().getString(R.string.menu_restore))
                        .setIcon(android.R.drawable.ic_menu_upload);
            }
            /* LGE_CHANGE_S, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
        }
        /* LGE_CHANGE_E, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
        if ((Config.getOperator().equals("VZW")
            && com.lge.os.Build.LGUI_VERSION.RELEASE == com.lge.os.Build.LGUI_VERSION_NAMES.V4_1)
                || Utils.getResources().getBoolean(R.bool.config_settings_search_enable)) {
            getMenuInflater().inflate(R.menu.settings_search, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 3LM_MDM_DCM_START jihun.im@lge.com 20121015 Check if 3LM has locked APN changes.
        boolean allowAddOrRestore = true;
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if ((Settings.Global.getInt(getContentResolver(), "apn_locked", 0) == 1) ||
                (Settings.Global.getInt(getContentResolver(), "apn_lock_mode", 0) == 1)) {
                allowAddOrRestore = false;
            }
        }
        // 3LM_MDM_DCM_END jihun.im@lge.com 20121015

        /* LGE_CHANGE_S, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
        if (mTetheredSate == 1) {
            /* LGE_CHANGE_E, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
            switch (item.getItemId()) {
            case MENU_NEW:
                // DCM 3LM MDM feature START
                if (!com.android.settings.lgesetting.Config.Config.THREELM_MDM || allowAddOrRestore) {
                // DCM 3LM MDM feature END
                    addNewApn();
                }
                return true;

            case MENU_RESTORE:
                // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-06, Add user create APN popup.
                if ("US".equals(Config.getCountry()) && "TMO".equals(Config.getOperator())
                    || "DCM".equals(Config.getOperator())
                    || "MPCS".equals(Config.getOperator())
                    ) { //shlee1219.lee
                    showDialog(DIALOG_RESTORE_DEFAULTAPN_CHECK);
                } else {
                    // DCM 3LM MDM feature START
                    if (!com.android.settings.lgesetting.Config.Config.THREELM_MDM || allowAddOrRestore) {
                    // DCM 3LM MDM feature END
                        restoreDefaultApn();
                    }
                }
                // [END_LGE_SETTINGS], MOD, kiseok.son, 2012-07-06, Add user create APN popup.
                return true;
            default:
                break;
            }
            /* LGE_CHANGE_S, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
        }
        /* LGE_CHANGE_E, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
        if (item.getItemId() == R.id.search) {
            switchToSearchResults();
        }

        if (item.getItemId() == android.R.id.home) {
            finish();
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

    private void addNewApn() {
        SLog.i("addNewApn");
        Intent intent = new Intent(Intent.ACTION_INSERT, getUri(Telephony.Carriers.CONTENT_URI));
        intent.putExtra(OPERATOR_NUMERIC_EXTRA, getSimNumeric(mServiceState));
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
        // intent.putExtra(SelectSubscription.SUBSCRIPTION_KEY, mSubId);
        intent.putExtra("subscription", mSubId);
        if ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
            intent.putExtra("operator_numeric", getSimNumeric(mServiceState));
            intent.putExtra("is_gsm_state", isLTEOrEHRPD(mServiceState));
        }
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */
        startActivity(intent);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // 3LM_MDM_DCM_START jihun.im@lge.com 20121015 Check if 3LM has locked APN changes.
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (Settings.Secure.getInt(getContentResolver(), "apn_locked", 0) == 1) {
                return true;
            }
        }
        // 3LM_MDM_DCM_END jihun.im@lge.com 20121015
        Log.i(TAG, "onPreferenceTreeClick");
        // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
            // [END_LGE_SETTINGS], MOD, kiseok.son, 2012-07-13, Add the LGCPA_
            // menu.
            int pos = Integer.parseInt(preference.getKey());
            Uri url = ContentUris.withAppendedId(
                    getUri(Telephony.Carriers.CONTENT_URI), pos);
            if ("KR".equals(Config.getCountry())) {
                if (!((ApnPreference)preference).getLockable()) {
                    startActivity(new Intent(Intent.ACTION_EDIT, url));
                }
                SLog.i("ACTION_EDIT :: subscription = " + mSubId);
            } else {
                Intent intent = new Intent(Intent.ACTION_EDIT, url);
                intent.putExtra("subscription", mSubId);
                if ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
                    intent.putExtra("operator_numeric", getSimNumeric(mServiceState));
                    intent.putExtra("is_gsm_state", ((ApnPreference)preference).getServiceState());
                }
                startActivity(intent);
                SLog.i("ACTION_EDIT :: subscription = " + mSubId);
                //startActivity(new Intent(Intent.ACTION_EDIT, url));
            }
            // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-13, Add the LGCPA_
            // menu.
        // [END_LGE_SETTINGS], MOD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "onPreferenceChange(): Preference - " + preference
                + ", newValue - " + newValue + ", newValue type - "
                + newValue.getClass());
        if (newValue instanceof String) {
            if (false == mUserManualKey.equals(mSelectedKey)) {
                SLog.d(TAG, "onPreferenceChange before mPrevSelectedKey = " + mPrevSelectedKey);
                mPrevSelectedKey = mSelectedKey;
                SLog.d(TAG, "onPreferenceChange after mPrevSelectedKey = " + mPrevSelectedKey);
            }
            setSelectedApnKey((String)newValue);
            if (Config.isDataRoaming(mSubId)) {
                fillList();
            }
            if (isTlfSpainSim()) {
                Log.d(TAG, "isTlfSpainSim : save apn profile");
                Intent mIntent = new Intent("com.lge.dualsimTLF.apnChanged");
                sendBroadcast(mIntent);
            }
        }
        return true;
    }

    public void setSelectedApnKey(String key) {
        SLog.i("setSelectedApnKey before mSelectedKey = " + mSelectedKey);
        SLog.i("setSelectedApnKey after mSelectedKey = " + key);
        mSelectedKey = key;
        ContentResolver resolver = getContentResolver();

        ContentValues values = new ContentValues();
        values.put(APN_ID, mSelectedKey);
        resolver.update(getUri(PREFERAPN_URI), values, null, null);
    }

    public String getSelectedApnKey() {
        String key = null;

        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [START]  */
        Cursor cursor = getContentResolver().query(getUri(PREFERAPN_URI), new String[] {"_id"},
                null, null, Telephony.Carriers.DEFAULT_SORT_ORDER);
        /* 2013-08-15 wonkwon.lee@lge.com LGP_DATA_APN_AUTOPROFILE [END]  */
        if (cursor != null) { // 120103 WBT
            try {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    key = cursor.getString(ID_INDEX);
                }
            } catch (Exception e) {
                // TODO: handle exception
                Log.e("ApnSettings",
                        " An error occurred  on getSelectedApnKey : "
                                + e.getMessage());
            } finally {
                cursor.close();
            }
        }

        return key;
    }

    // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-06, Add user create APN popup.
    private boolean supportRestoreMenu() {
        boolean isExistUserValue = false;
        if ("US".equals(Config.getCountry()) && "TMO".equals(Config.getOperator())
                || "MPCS".equals(Config.getOperator())
                || "DCM".equals(Config.getOperator())) { //shlee1219.lee
            Cursor cursor = null;
            String selection = "usercreatesetting <> '"
                    + TelephonyProxy.Carriers.USERCREATESETTING_PRELOADED + "'";

            cursor = getContentResolver().query(Telephony.Carriers.CONTENT_URI, new String[] {
                    "usercreatesetting" }, selection, null, null);
            SLog.i("test cursor = " + cursor);
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        isExistUserValue = true;
                    } else {
                        isExistUserValue = false;
                    }
                    SLog.i("test isExistUserValue = " + isExistUserValue);
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e("ApnSettings",
                            " An error occurred  on getUserCreateSetting : "
                                    + e.getMessage());
                } finally {
                    cursor.close();
                }
            }
        } else if ("SPR".equals(Config.getOperator())
                && mSPR_IOT_MENU == false) {
            //add the sprint apn requirement.
            //skip the reset to default menu in sprint model.
            SLog.i("sprint :: skip reset to default.");
        } else {
            isExistUserValue = true;
        }
        return isExistUserValue;

    }

    // [END_LGE_SETTINGS], MOD, kiseok.son, 2012-07-06, Add user create APN popup.

    private boolean restoreDefaultApn() {
        showDialog(DIALOG_RESTORE_DEFAULTAPN);
        mRestoreDefaultApnMode = true;

        if (mRestoreApnUiHandler == null) {
            mRestoreApnUiHandler = new RestoreApnUiHandler();
        }
        // Modified JB 4.2 MobileNetwork System -> Global +
        /*
        if (mRestoreApnProcessHandler == null) {
            HandlerThread restoreDefaultApnThread = new HandlerThread(
                    "Restore default APN Handler: Process Thread");
            restoreDefaultApnThread.start();

            if (null != restoreDefaultApnThread.getLooper()) {
                mRestoreApnProcessHandler = new RestoreApnProcessHandler(
                        restoreDefaultApnThread.getLooper(), mRestoreApnUiHandler);
            } // 120103 WBT
        }
        */
        if (mRestoreApnProcessHandler == null ||
                mRestoreDefaultApnThread == null) {
            mRestoreDefaultApnThread = new HandlerThread(
                    "Restore default APN Handler: Process Thread");
            mRestoreDefaultApnThread.start();
            mRestoreApnProcessHandler = new RestoreApnProcessHandler(
                    mRestoreDefaultApnThread.getLooper(), mRestoreApnUiHandler);
        }
        // Modified JB 4.2 MobileNetwork System -> Global -

        if (mRestoreApnProcessHandler != null) {
            mRestoreApnProcessHandler.sendEmptyMessage(EVENT_RESTORE_DEFAULTAPN_START);
        }
        return true;
    }

    private class RestoreApnUiHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (!mRestoreDefaultApnMode) {
                SLog.i("skip update for finish()");
                return;
            }
            switch (msg.what) {
            case EVENT_RESTORE_DEFAULTAPN_COMPLETE:
                Log.d(TAG, "ApnSettings RestoreApnUiHandler restoring = " + sApnPause);
                sApnDialog = true;
                if (!sApnPause) {
                    completeRestore();
                    sApnDialog = false;
                }
                break;
            default:
                break;
            }
        }
    }

    private class RestoreApnProcessHandler extends Handler {
        private Handler mRestoreApnUiHandler;

        public RestoreApnProcessHandler(Looper looper, Handler restoreApnUiHandler) {
            super(looper);
            this.mRestoreApnUiHandler = restoreApnUiHandler;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EVENT_RESTORE_DEFAULTAPN_START:
                ContentResolver resolver = getContentResolver();
                resolver.delete(getUri(DEFAULTAPN_URI), null, null);
                SLog.i("EVENT_RESTORE_DEFAULTAPN_START");
                mRestoreApnUiHandler
                    .sendEmptyMessage(EVENT_RESTORE_DEFAULTAPN_COMPLETE);
                SLog.i("EVENT_RESTORE_DEFAULTAPN_end");
                break;
            default:
                break;
            }
        }
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (id == DIALOG_RESTORE_DEFAULTAPN) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage(getResources().getString(R.string.restore_default_apn));
            dialog.setCancelable(false);
            return dialog;
        }
        // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-06, Add user create APN popup.
        else if (id == DIALOG_RESTORE_DEFAULTAPN_CHECK) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);

            b.setTitle(R.string.sp_reset_to_default_NORMAL)
                    .setMessage(R.string.sp_restore_default_apn_desc_NORMAL)
                    .setPositiveButton(R.string.yes, this)
                    .setNegativeButton(R.string.no, this);
            if (false == Utils.isUI_4_1_model(getApplicationContext())) {
                b.setIconAttribute(android.R.attr.alertDialogIcon);
            }
            AlertDialog dialog = b.create();
            return dialog;
        }
        // [END_LGE_SETTINGS], MOD, kiseok.son, 2012-07-06, Add user create APN popup.
        // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-09-05, Add the LGCPA_ menu.
        else if (id == DIALOG_APN_NAME_CHECK) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);

            b.setTitle(R.string.error_title)
                    .setMessage(R.string.sp_connection_unavailable_NORMAL)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialoginterface, int i) {
                                }
                            });
            if (false == Utils.isUI_4_1_model(getApplicationContext())) {
                b.setIconAttribute(android.R.attr.alertDialogIcon);
            }
            AlertDialog dialog = b.create();
            return dialog;
        }
        // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-09-05, Add the LGCPA_ menu.
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (id == DIALOG_RESTORE_DEFAULTAPN) {
            getPreferenceScreen().setEnabled(false);
        }
    }

    private String setSKTSBSMAPNSkipDisplayFilter(String strOperator) {
        String strAttach = null;

        String strSimOperator = SystemProperties
                .get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
        Log.d(TAG, "[ApnSettings] fillList strSimOperator :" + strSimOperator);

        if (strSimOperator.equals(strOperator)) {
            boolean isRoaming = Config.isDataRoaming(mSubId);
            Log.d(TAG, "[ApnSettings] fillList isRoaming :" + isRoaming);

            if (isRoaming) {
                strAttach = " AND " + "apn !=\"" + "lte.sktelecom.com" + "\"";
                strAttach += " AND " + "apn !=\"" + "web.sktelecom.com" + "\"";
            }
            else {
                strAttach = " AND " + "apn !=\"" + "roaming.sktelecom.com"
                        + "\"";
            }

        }

        return strAttach;
    }

    private String setLGUSBSMAPNSkipDisplayFilter(String strOperator) {
        String strAttach = null;

        String strSimOperator = SystemProperties
                .get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
        Log.d(TAG, "[ApnSettings] fillList strSimOperator :" + strSimOperator);

        if (strSimOperator.equals(strOperator)) {
            strAttach = " AND " + "apn !=\"" + "ims.lguplus.co.kr" + "\"";
            strAttach += " AND " + "apn !=\"" + "stethering.lguplus.co.kr"
                    + "\"";
            strAttach += " AND " + "apn !=\"" + "tethering.lguplus.co.kr" + "\"";
            strAttach += " AND " + "apn !=\"" + "imsv6.lguplus.co.kr" + "\"";
        }
        return strAttach;
    }

    // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-06, Add user create APN popup.
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
            restoreDefaultApn();
            break;
        case DialogInterface.BUTTON_NEGATIVE:
            dialog.dismiss();
            break;
        default:
            break;
        }
    }

    // [END_LGE_SETTINGS], MOD, kiseok.son, 2012-07-06, Add user create APN popup.

    private boolean isTlfSpainSim() {
        String mccmnc = ((TelephonyManager)getSystemService(
                Context.TELEPHONY_SERVICE)).getSimOperator();
        if (mccmnc == null || mccmnc.length() < 5) {
            Log.d(TAG, "isTlfSpainSim : false, mccmnc null ");
            return false;
        }

        String mcc = mccmnc.substring(0, 3);
        String mnc = mccmnc.substring(3);
        //Log.i(TAG, "[Step1] MCC: " + mcc + "/ MNC: " + mnc);
        if (!("214".equals(mcc) && (/* "05".equals(mnc) || */"07".equals(mnc)))) {
            Log.d(TAG, "isTlfSpainSim : false ");
            return false;
        }
        Log.d(TAG, "isTlfSpainSim : true");
        return true;
    }

    //CT Requirement 140310 
    //private final int mLTE = 1;
    private final int mGSM = 2;
    private final int mCDMA = 3;
    //private final int mEVDO = 4;
    //private final int mEHRPD = 5;
    private final int mINVALID_DATA_RADIO = 6;
    private int mConnectNwMode = mINVALID_DATA_RADIO;

    private int dataRadio(ServiceState serviceState) {
        if (serviceState == null) {
            SLog.e(TAG, "Service state not updated");
            return mINVALID_DATA_RADIO;
        }
        int mRadioTech = 0;
        mRadioTech = serviceState.getRilDataRadioTechnology();
        if (ServiceState.isGsm(mRadioTech)
                || mRadioTech == ServiceState.RIL_RADIO_TECHNOLOGY_EHRPD) {
            SLog.i("ServiceState.isGsm = " + mRadioTech);
            return mGSM;
        } else if (ServiceState.isCdma(mRadioTech)) {
            SLog.i("ServiceState.isCdma = " + mRadioTech);
            return mCDMA;
        } else {
            SLog.i("ServiceState no svc = " + mRadioTech);
            return mINVALID_DATA_RADIO;
        }
    }

    private String getSimNumeric(ServiceState serviceState) {
        String mNumeric = " ";
        int mRadioTech = 0;
        if (("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator()))
                && (null != serviceState)) {
            mRadioTech = serviceState.getRilDataRadioTechnology();
            if (ServiceState.isGsm(mRadioTech)
                    || mRadioTech == ServiceState.RIL_RADIO_TECHNOLOGY_EHRPD) {
                SLog.i("getSimNumeric.isGsm = " + mRadioTech);
                mNumeric = LGSubscriptionManager.getTelephonyProperty(mSubId,
                        "gsm.apn.sim.operator.numeric");
            } else if (ServiceState.isCdma(mRadioTech)) {
                SLog.i("getSimNumeric.isCdma = " + mRadioTech);
                mNumeric = TelephonyProxy.Carriers.getNumeric(mSubId);
            } else {
                SLog.i("getSimNumeric no svc = " + mRadioTech);
                mNumeric = TelephonyProxy.Carriers.getNumeric(mSubId);
            }
        } else if ("USC".equals(Config.getOperator()) 
                    || "LRA".equals(Config.getOperator())
                    || "ACG".equals(Config.getOperator())) {
            mNumeric = LGSubscriptionManager.getTelephonyProperty(mSubId,
                "gsm.apn.sim.operator.numeric");
            SLog.i("USC mNumeric =  " + mNumeric );
        } else {
            SLog.i("getSimNumeric single = " + mRadioTech);
            mNumeric = TelephonyProxy.Carriers.getNumeric(mSubId);
        }
        SLog.i("mccmnc = " + mNumeric);
        return mNumeric;
    }

    private boolean isCTCSim(ServiceState serviceState) {
        boolean mValue = true;
        String mNumeric = getSimNumeric(serviceState);
        if ("46003".equals(mNumeric) 
            || "46011".equals(mNumeric)
            || "20404".equals(mNumeric)
        )  {
            SLog.i("is ctc sim = " + mNumeric);
            mValue = true;
        } else {
            SLog.i("is not ctc sim = " + mNumeric);
            mValue = false;
        }
        return mValue;
    }
    public void register(ContentResolver contentResolver) {
        contentResolver.registerContentObserver(getUri(PREFERAPN_URI), false, mObserverPreferredApn);
        SLog.i("register mObserverPreferredApn");
    }

    public void unregister(ContentResolver contentResolver) {
        contentResolver.unregisterContentObserver(mObserverPreferredApn);
        SLog.i("unregister mObserverPreferredApn");
    }

    private String getOperatorNumericSelection() {
        String[] mccmncs = getOperatorNumeric();
        String where;
        where = (mccmncs[0] != null) ? "numeric=\"" + mccmncs[0] + "\"" : "";
        where += (mccmncs[1] != null) ? " or numeric=\"" + mccmncs[1] + "\"" : "";
        Log.d(TAG, "getOperatorNumericSelection: " + where);
        return where;
    }

    private String[] getOperatorNumeric() {
        ArrayList<String> result = new ArrayList<String>();
        if (mUseNvOperatorForEhrpd) {
            String mccMncForEhrpd = SystemProperties.get("ro.cdma.home.operator.numeric", null);
            if (mccMncForEhrpd != null && mccMncForEhrpd.length() > 0) {
                result.add(mccMncForEhrpd);
            }
        }
        String mccMncFromSim = LGSubscriptionManager.getIccOperatorNumeric(mSubId);
        Log.d(TAG, "getOperatorNumeric: sub= " + mSubId +
                    " mcc-mnc= " + mccMncFromSim);
        if (mccMncFromSim != null && mccMncFromSim.length() > 0) {
            result.add(mccMncFromSim);
        }
        return result.toArray(new String[2]);
    }

    private boolean isRuimOperatorNumericRequired(int phoneType, int netType) {
        return (phoneType == PhoneConstants.PHONE_TYPE_CDMA &&
                netType != ServiceState.RIL_RADIO_TECHNOLOGY_LTE &&
                netType != ServiceState.RIL_RADIO_TECHNOLOGY_EHRPD);
    }

    private boolean isLTEOrEHRPD(ServiceState serviceState) {
        if (serviceState == null) {
            SLog.e(TAG, "Service state not updated");
            return false;
        }
        int mRadioTech = 0;
        mRadioTech = serviceState.getRilDataRadioTechnology();
        if (mRadioTech == ServiceState.RIL_RADIO_TECHNOLOGY_LTE
                || mRadioTech == ServiceState.RIL_RADIO_TECHNOLOGY_EHRPD) {
            SLog.i("ServiceState.isGsm = " + mRadioTech);
            return true;
        }
        return false;
    }
}
