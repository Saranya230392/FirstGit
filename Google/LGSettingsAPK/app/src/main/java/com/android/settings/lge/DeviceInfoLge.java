package com.android.settings.lge;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.content.Intent;

import android.telephony.TelephonyManager;

import com.android.settings.R;
import com.android.settings.RenameEditTextPreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.Utf8ByteLengthFilter;

//[S][2012.03.17][seungeene][DCM][NA] Add Next-i FOTA for Docomo
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.remote.RemoteFragment;
import com.android.settings.remote.RemoteFragmentManager;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.unusedapps.ui.settings.AppCleanupSettingsFragment;

//2013-03-07 SungKyoung-Kim(sungkyoung.kim@lge.com) [VS890] add to meet vzw req. refer to VS840 [START]
// import com.lge.provider.SettingsEx;
import com.lge.constants.SettingsConstants;
//2013-03-07 SungKyoung-Kim(sungkyoung.kim@lge.com) [VS890] add to meet vzw req. refer to VS840 [END]

import android.preference.EditTextPreference;
import android.text.TextWatcher;
import android.text.Editable;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.Fragment;
import android.widget.EditText;
import android.view.View;
import android.text.InputFilter;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.content.Context;
import android.view.View.OnClickListener;
import android.preference.DialogPreference;
import android.content.DialogInterface;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;
import android.text.Spanned;
import android.text.InputType;
import android.text.Html;

import android.os.UserHandle;
import android.os.UserManager;
import android.content.pm.UserInfo;

import com.android.internal.telephony.PhoneConstants;

public class DeviceInfoLge extends SettingsPreferenceFragment implements
        Preference.OnPreferenceClickListener, Indexable {
    private static final String LOG_TAG = "aboutphone  # DeviceInfoLge";
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;

    //private static final String KEY_CONTAINER = "container";
    //private static final String KEY_TEAM = "team";
    private static final String KEY_CONTRIBUTORS = "contributors";
    //private static final String KEY_TERMS = "terms";
    //private static final String KEY_LICENSE = "license";
    //private static final String KEY_COPYRIGHT = "copyright";
    private static final String KEY_SYSTEM_UPDATE_SETTINGS = "system_update_settings";
    private static final String PROPERTY_URL_SAFETYLEGAL = "ro.url.safetylegal";

    //private static final String KEY_SYSTEM_UPDATE_SETTINGS_FOR_TMUS = "system_update_settings_for_tmus";

    private static final String KEY_PHONEID_PROFILE = "status";
    private Preference mAboutPhonePreference;

    //Sutel information for Costa Rica versions
    private Preference mSutelPreference;

    private static String operator_code;
    private Preference mHardwareInfo;
    private Preference mLegalInfo;
    private Preference mUpdate;

    private CheckBoxPreference mLogInfomation;

    // H/W Version
    private static final String PROPERTY_PCB_VER = "ro.pcb_ver";
    private static final String PROPERTY_HW_REVISION = "ro.lge.hw.revision";

    private static final String KEY_BATTERY = "battery";
    //private static final String KEY_SYSTEM_UPDATE_CENTER = "update_center";

    private EditTextPreference mDeviceNamePref;
    private RenameEditTextPreference mRenameDeviceNamePref;
    private Toast mToast = null;
    private static final int PHONE_NAME_MAXLEN = 32;
    private static final String KEY_RENAME_DEVICE = "my_phone_name";
    private static final String DEFAULT_DEVICE_NAME = "Optimus";
    private static final String KEY_NETWORK = "network";

    @Override
    public void onCreate(Bundle icicle) {
        int activePhoneType = TelephonyManager.getDefault().getCurrentPhoneType();
        //[S][2013.04.10][never1029] Update Center list apply
        boolean mUpdateCenter = Utils.IsVersionCheck(getActivity(), "com.lge.updatecenter");
        //[E][2013.04.10][never1029] Update Center list apply

        super.onCreate(icicle);

        Log.i(LOG_TAG, "onCreate");
        Log.i(LOG_TAG, "mUpdateCenter : " + mUpdateCenter);
        mIsFirst = true;

        addPreferencesFromResource(R.xml.device_info_lge);
        removePreferenceFromScreen("hardwareinformation");

        operator_code = Config.getOperator();

        mDeviceNamePref = (EditTextPreference)findPreference(KEY_RENAME_DEVICE);
        mDeviceNamePref.setPositiveButtonText(getString(R.string.def_save_btn_caption));
        if (!"MCC".equals(SystemProperties.get("wlan.lge.concurrency", ""))) {
            mDeviceNamePref.setDialogMessage(getString(R.string.sp_phone_name_dialog_description_without_wifi));
        }
        mAboutPhonePreference = findPreference(KEY_PHONEID_PROFILE);
        /*
        * int resIndex = getArguments().getInt(Utils.RESOURCE_INDEX, 0); if
        * (Utils.MULTISIM_RESID == resIndex) {
        * findPreference(KEY_STATUS).getIntent().setClassName(
        * "com.android.settings","com.android.settings.deviceinfo.MSimStatus");
        * }
        */
        // Remove Safety information preference if PROPERTY_URL_SAFETYLEGAL is
        // not set
        removePreferenceIfPropertyMissing(getPreferenceScreen(), "safetylegal",
                PROPERTY_URL_SAFETYLEGAL);

        // [S][2012.03.17][seungeene][DCM][NA] Add Next-i FOTA for Docomo

        // [E][2012.03.17][seungeene][DCM][NA] Add Next-i FOTA for Docomo

        //[S][2013.01.23][never1029][VZW JB first model apply - VZW operator req
        //if (Utils.isUpgradeModel()) {
        //    removePreferenceFromScreen("icongr");
        //} else {
        //if (!"VZW".equals(operator_code)) {
        removePreferenceFromScreen("icongr");
        //}
        //}
        //[E][2013.01.23][never1029][VZW JB first model apply - VZW operator req
        /*
        if (SystemProperties.get("ro.product.model").equals("L-04E")) {
            findPreference(KEY_BATTERY).setSummary(getString(R.string.sp_battery_condition_NORMAL));
        } else {
            findPreference(KEY_BATTERY).setSummary(getString(R.string.sp_BatterySummary_NORMAL));
        }
        */

        if (!"KR".equals(Config.getCountry())
                || "".equals(SystemProperties.get("ro.lge.sar.value", ""))) {
            removePreferenceFromScreen("sarvalue");
        }

        if (TelephonyManager.PHONE_TYPE_CDMA != activePhoneType) {
            mAboutPhonePreference.setSummary(getString(R.string.sp_phoneinformation_not_cdma));
        } else {
            if (TelephonyManager.getDefault().getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE) {
                mAboutPhonePreference.setSummary(getString(R.string.sp_phoneinformation_not_cdma));
            } else {
                mAboutPhonePreference.setSummary(getString(R.string.sp_phoneinformation_cdma));
            }
        }

        if ((Utils.isEmbededBattery(getActivity().getApplicationContext()) && "DCM".equals(Config
                .getOperator()))) {
            findPreference(KEY_BATTERY).setSummary(getString(R.string.sp_battery_condition_NORMAL));
        } else {
            findPreference(KEY_BATTERY).setSummary(getString(R.string.sp_BatterySummary_NORMAL));
        }

        if (Utils.isWifiOnly(getActivity())) {
            if (Utils.isUI_4_1_model(getActivity())) {
                removePreferenceFromScreen("update_center_admin");
                removePreferenceFromScreen("update_center_guest");
                if (UserManager.get(getActivity()).isLinkedUser()) {
                    removePreferenceFromScreen("update_center");
                    removePreferenceFromScreen(KEY_SYSTEM_UPDATE_SETTINGS);
                }
            } else {
                removePreferenceFromScreen("update_center");
                if (UserManager.get(getActivity()).isLinkedUser()) {
                    removePreferenceFromScreen("update_center_admin");
                    removePreferenceFromScreen("update_center_guest");
                    removePreferenceFromScreen(KEY_SYSTEM_UPDATE_SETTINGS);
                } else {
                    if (!(UserHandle.myUserId() == UserHandle.USER_OWNER)) {
                        removePreferenceFromScreen("update_center_admin");
                    } else {
                        removePreferenceFromScreen("update_center_guest");
                    }
                }
            }
            removePreferenceFromScreen(KEY_PHONEID_PROFILE);
            removePreferenceFromScreen(KEY_NETWORK);
            mDeviceNamePref.setDialogMessage(getString(R.string.sp_tablet_name_dialog_summary));
        } else {
            removePreferenceFromScreen("update_center_admin");
            removePreferenceFromScreen("update_center_guest");
        }

        /* Settings is a generic app and should not contain any device-specific info. */
        final Activity act = getActivity();

        // These are contained by the root preference screen
        PreferenceGroup parentPreference = getPreferenceScreen();
        /*
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_SYSTEM_UPDATE_CENTER, Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        */
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference,
                KEY_SYSTEM_UPDATE_SETTINGS,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        /*
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_SYSTEM_UPDATE_SETTINGS_FOR_TMUS, Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        */
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_CONTRIBUTORS,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);

        if (Utils.isWifiOnly(getActivity())) {
            removePreferenceFromScreen("phoneidentity");
        } else {
            removePreferenceFromScreen("wifi");
        }

        //[S][2013.04.10][never1029] Update Center list apply
        if (SystemProperties.get("ro.product.model").equals("L-05E")) { //F9J only
            removePreferenceFromScreen("update_center");
            removePreferenceFromScreen(KEY_SYSTEM_UPDATE_SETTINGS);
        } else if (mUpdateCenter) {
            removePreferenceFromScreen("update_center_dcm");
            removePreferenceFromScreen(KEY_SYSTEM_UPDATE_SETTINGS);
            if ("LRA".equals(Config.getOperator()) || "SPR".equals(Config.getOperator())) {
                mUpdate = findPreference("update_center");
                mUpdate.setSummary(getString(R.string.sp_update_center_guest_NORMAL));
            }
        } else {
            if ("VZW".equals(Config.getOperator()) || "LRA".equals(Config.getOperator())) {
                removePreferenceFromScreen(KEY_SYSTEM_UPDATE_SETTINGS);
            }
            removePreferenceFromScreen("update_center_dcm");
            removePreferenceFromScreen("update_center");
        }
        //[E][2013.04.10][never1029] Update Center list apply

        Log.i(LOG_TAG, "Build.DEVICE :" + Build.DEVICE);
        Log.i(LOG_TAG, "country:" + Config.getCountry());

        /* hardware version summary */
        mHardwareInfo = findPreference("hardwareinformation");
        if (mHardwareInfo != null) {
            if (!"MPCS".equals(Config.getOperator())) {
                if (Utils.isEmbededBattery(getActivity().getApplicationContext())) {
                    if (SystemProperties.get(PROPERTY_HW_REVISION).equals("")
                            || SystemProperties.get(PROPERTY_PCB_VER).equals("")) {
                        mHardwareInfo.setSummary(getString(R.string.sp_HWversion_NORMAL));
                    } else {
                        mHardwareInfo
                                .setSummary(getString(R.string.sp_HWversionSummary_includeHWversion));
                    }
                } else {
                    mHardwareInfo.setSummary(getString(R.string.sp_HWversion_NORMAL));
                }
            }
        }

        /* legal information summary */
        mLegalInfo = findPreference("legalinformation");
        if (mLegalInfo != null) {
            if ("SPR".equals(operator_code) || "BM".equals(operator_code)
                    || "MPCS".equals(operator_code)) {
                if ("TRF".equals(operator_code)) { // added for exception in L25L
                    mLegalInfo.setSummary(getString(R.string.Legal_information_summary));
                } else {
                    mLegalInfo.setSummary(getString(R.string.Legal_information_summary_etc));
                }
            } else if (Config.CMCC.equals(Config.getOperator()) || "CMO".equals(Config.getOperator())
                    || "CN".equals(Config.getCountry())) {
                Log.d("Deviceinfolge", "Config.getOperator() IN" + Config.getOperator());
                mLegalInfo.setSummary(getString(R.string.settings_license_activity_title));
            } else {
                mLegalInfo.setSummary(getString(R.string.Legal_information_summary));
            }
        }

        if (Utils.isEmbededBatteryWithCover(getActivity().getApplicationContext())) {
            Log.i(LOG_TAG, "isEmbededBatteryWithCover, not support device_serial_info");
            if (findPreference("device_serial_info") != null) {
                removePreferenceFromScreen("device_serial_info");
            }
        } else if (!Utils.isEmbededBattery(getActivity().getApplicationContext())) { // JB+
            Log.i(LOG_TAG, "not support device_serial_info");
            if (findPreference("device_serial_info") != null) {
                removePreferenceFromScreen("device_serial_info");
            }
        } else {
            if (Utils.isWifiOnly(getActivity()) && "KR".equals(Config.getCountry())
                    || Build.DEVICE.equals("altev")
                    || "CN".equals(Config.getCountry())
                    || "HK".equals(Config.getCountry())
                    || "TW".equals(Config.getCountry())
                    || "SPR".equals(operator_code)
                    || "USC".equals(operator_code)
                    || Build.DEVICE.equals("e8lte")
                    || "ATT".equals(operator_code)
                    || "USC".equals(operator_code)) {
                removePreferenceFromScreen("device_serial_info");
            }
            Log.i(LOG_TAG, "support device_serial_info");
        }
        // [E][2012.07.20][yongjaeo.lee@lge.com] embedded battery model (
        // regulatory & factory date & SN )

        if (!(UserHandle.myUserId() == UserHandle.USER_OWNER)) {
            removePreferenceFromScreen("status");
            removePreferenceFromScreen("network");
            removePreferenceFromScreen("phoneidentity");
        }
        // [E][2012.07.20][yongjaeo.lee@lge.com] embedded battery model (
        // regulatory & factory date & SN )

        if (!"US".equals(Config.getCountry())
                || SystemProperties.get("ro.product.model").equals("LGL25L")) {
            removePreferenceFromScreen("patentinfo");
        }

        //if (SystemProperties.get("ro.product.model").equals("L-04E") || SystemProperties.get("ro.product.model").equals("L-05D") || SystemProperties.get("ro.product.model").equals("L-02E"))
        /*
        if ((Config.DCM).equals(Config.getOperator())) {
            removePreferenceFromScreen(KEY_SYSTEM_UPDATE_SETTINGS);
            removePreferenceFromScreen(KEY_SYSTEM_UPDATE_SETTINGS_FOR_TMUS);
        } else if ("TMO".equals(Config.getOperator())) {
            if ("US".equals(Config.getCountry())) {
                removePreferenceFromScreen(KEY_SYSTEM_UPDATE_SETTINGS); //TMO-US
            } else {
                removePreferenceFromScreen(KEY_SYSTEM_UPDATE_SETTINGS_FOR_TMUS); //TMO-EU
            }
            removePreferenceFromScreen("update_center");
        } else {
            removePreferenceFromScreen(KEY_SYSTEM_UPDATE_SETTINGS_FOR_TMUS);
            removePreferenceFromScreen("update_center");
        }
        */

        //2013-03-07 SungKyoung-Kim(sungkyoung.kim@lge.com) [VS890] add to meet vzw req. refer to VS840 [START]
        // BEGIN: FOTA&SDM Pool, 2011-06-14
        // LG_UI juil.kang@lge.com VZW feature hidden menu check
        int otadmSetting = Settings.System.getInt(getContentResolver(),
                "KLP" /* SettingsConstants.System.VZW_HIDDEN_FEATURE_OTADM */, 1);
        if (otadmSetting == 0) {
            removePreferenceFromScreen(KEY_SYSTEM_UPDATE_SETTINGS);
        }
        // END: FOTA&SDM Pool, 2011-06-14
        //2013-03-07 SungKyoung-Kim(sungkyoung.kim@lge.com) [VS890] add to meet vzw req. refer to VS840 [END]

        //[S][2012.06.18][yongjaeo.lee][E0][NA] Move developer settings to inside of about phone from top header (ORG request)
        /*
        if (!"ORG".equals(Config.getOperator())) {
            removePreferenceFromScreen("development_settings");
        } else { // in case ORG
            if ("LG-E975".equals(Build.MODEL)) {
                removePreferenceFromScreen("development_settings");
            }
        }
        */
        removePreferenceFromScreen("development_settings");
        //[E][2012.06.18][yongjaeo.lee][E0][NA] Move developer settings to inside of about phone from top header (ORG request)

        //Sutel information for Costa Rica versions
        mSutelPreference = (Preference)findPreference("sutel");
        if (mSutelPreference != null && getPreferenceScreen() != null) {
            String sutelNumber = SystemProperties.get("ro.build.sutel_number", "");
            if (!"".equals(sutelNumber) && sutelNumber != null) {
                mSutelPreference.setSummary("SUTEL " + sutelNumber);
            }
            else {
                getPreferenceScreen().removePreference(mSutelPreference);
            }
        }
        //Sutel information for Costa Rica versions

        mDeviceNamePref.setOnPreferenceClickListener(this);

        mDeviceNamePref.getEditText().setFilters(new InputFilter[] {
                new Utf8ByteLengthFilter(PHONE_NAME_MAXLEN,
                        getActivity(), mDeviceNamePref.getEditText())
        });

        mRenameDeviceNamePref = new RenameEditTextPreference(getActivity(), null);
        mRenameDeviceNamePref.setmDeviceNamePref(mDeviceNamePref);
        mDeviceNamePref.setSummary(read_db_device_name());
        mDeviceNamePref.setText(read_db_device_name());
        mDeviceNamePref.getEditText().addTextChangedListener(mRenameDeviceNamePref);

        if (!Utils.checkPackage(getActivity(), "com.ctc.epush")) {
            removePreferenceFromScreen("epush");
        }

        mLogInfomation = (CheckBoxPreference)findPreference("loginfomation");
        if (Utils.checkPackage(getActivity(), "com.lge.mlt")
                && (com.lge.os.Build.LGUI_VERSION.RELEASE
                        >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2)) {
            mLogInfomation.setChecked(isLogInfomationOn());
        } else {
            removePreferenceFromScreen("loginfomation");
        }

        if (!(Utils.checkPackage(getActivity(), "com.lge.deperso")
                && "KDDI".equals(Config.getOperator()))) {
            removePreferenceFromScreen("simlock_management");
        }
    }

    public String read_db_device_name() {
        String device_name = null;
        if (UserManager.supportsMultipleUsers()) {
            device_name = Settings.Global.getString(getContentResolver(), "lg_device_name");
        } else {
            device_name = Settings.System.getString(getContentResolver(), "lg_device_name");
        }

        if (device_name == null) {
            Log.d(LOG_TAG, "read_db_device_name() : device_name : (on DB) , default ~~~ : "
                    + device_name);
            device_name = DEFAULT_DEVICE_NAME;
        } else {
            Log.d(LOG_TAG, "read_db_device_name() : device_name : (on DB) " + device_name);
        }
        return device_name;
    }

    // yonguk.kim 2014-03-06 Apply remote fragment feature [START]
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        Log.d("kimyow", "onPreferenceTreeClick:" + preference.getKey());
        if (Utils.supportRemoteFragment(getActivity())) {
            if ("update_center".equals(preference.getKey())) {
                PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
                boolean result = RemoteFragmentManager.setPreferenceForRemoteFragment(
                        "com.lge.updatecenter", preference);
                if (result) {
                    preferenceActivity.onPreferenceStartFragment(this, preference);
                    return true;
                }
            }
        }

        if ("loginfomation".equals(preference.getKey())) {
            if (mLogInfomation != null) {
                mLogInfomation.setChecked(isLogInfomationOn());
                startFragment(this, DeviceInfoLgeLogInfomation.class.getCanonicalName(),
                        -1, null, R.string.sp_log_information_title);
                return true;
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    // yonguk.kim 2014-03-06 Apply remote fragment feature [END]

    public boolean onPreferenceClick(Preference pref) {
        //Log.d(LOG_TAG, "onPreferenceClick");

        mDeviceNamePref.setText(read_db_device_name());
        mDeviceNamePref.getEditText().selectAll();
        //        mDeviceNamePref.getEditText().setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "onResume()");
        if (mLogInfomation != null
                && Utils.checkPackage(getActivity(), "com.lge.mlt")
                && (com.lge.os.Build.LGUI_VERSION.RELEASE
                        >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2)) {
            mLogInfomation.setChecked(isLogInfomationOn());
        }

        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent().getBooleanExtra(
                "perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            startResult();
            mIsFirst = false;
        }
    }

    private void removePreferenceIfPropertyMissing(PreferenceGroup preferenceGroup,
            String preference, String property) {
        if (SystemProperties.get(property).equals("")) { // Property is missing so remove preference from group
            try {
                if (preferenceGroup != null) {
                    preferenceGroup.removePreference(findPreference(preference));
                }
            } catch (RuntimeException e) {
                Log.d(LOG_TAG, "Property '" + property + "' missing and no '" + preference
                        + "' preference");
            }
        }
    }

    private void removePreferenceFromScreen(String key) {
        Preference pref = findPreference(key);

        if (pref != null && getPreferenceScreen() != null) {
            getPreferenceScreen().removePreference(pref);
        } else {
            if (pref == null) {
                //Log.i(LOG_TAG, "removePreferenceFromScreen() removePreferenceFromScreen pref == null");
            } else {
                Log.i(LOG_TAG, "removePreferenceFromScreen() getPreferenceScreen() == null");
            }
        }
    }

    private void showMaxCharExceededToast() {
        Log.i(LOG_TAG, "showMaxCharExceededToast");

        if (null == mToast) {
            mToast = Toast.makeText(getActivity(), R.string.sp_owner_text_size_NORMAL,
                    Toast.LENGTH_SHORT);
        } else {
            mToast.setText(R.string.sp_owner_text_size_NORMAL);
        }
        mToast.show();
    }

    public static boolean isLogInfomationOn() {
        boolean bReturn = Utils.readLogInformationFile();

        if (bReturn) {
            Log.d (LOG_TAG, "isLogInfomationOn -> checked");
        } else {
            Log.d (LOG_TAG, "isLogInfomationOn -> unchecked");
        }

        return bReturn;
    }

    private void startResult() {
        if (mSearch_result.equals(KEY_RENAME_DEVICE)) {
            mDeviceNamePref.performClick(getPreferenceScreen());
        }
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            String myClassName = "com.android.settings.Settings$DeviceInfoLgeActivity";
            String myPackageName = "com.android.settings";
            String screenTitle = context.getString(R.string.about_settings);
            
            setSearchIndexData(context, "aboutphone",
                    context.getString(R.string.about_settings),
                    "main", null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "my_phone_name",
                    context.getString(R.string.sp_phone_name_title),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "update_center",
                    context.getString(R.string.sp_title_dcm_update_center),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    "com.lge.updatecenter.UpdateCenterPrfActivity",
                    "com.lge.updatecenter", 1, null, null, null, 1, 0);
            setSearchIndexData(context, "network",
                    context.getString(R.string.status_operator),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    "com.android.settings.lge.DeviceInfoLgeNetwork",
                    myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "status",
                    context.getString(R.string.device_status),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    "com.android.settings.lge.DeviceInfoLgeStatus",
                    myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "battery",
                    context.getString(R.string.power_usage_summary_title),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$DeviceInfoLgeBatteryActivity",
                    myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "phoneidentity",
                    context.getString(R.string.sp_HardwareInfo_NORMAL),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    "com.android.settings.lge.DeviceInfoLgePhoneIdentity",
                    myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "softwareinformation",
                    context.getString(R.string.sp_Softwareinfo_NORMAL),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$DeviceInfoLgeSoftwareInformationActivity",
                    myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "legalinformation",
                    context.getString(R.string.legal_information),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$DeviceInfoLgeLegalActivity",
                    myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "patent_information",
                    context.getString(R.string.sp_patent_information),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    "com.android.settings.SettingsPatentInformation",
                    myPackageName, 1, null, null, null, 1, 0);
            int regulatory = Utils.isEmbededBattery(context) == true ? 1 : 0;     
            setSearchIndexData(context, "Regulatory",
                    context.getString(R.string.sp_regulatory_safety),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$DeviceInfoLgeSerialActivity",
                    myPackageName, 1, null, null, null, regulatory, 0);
            return mResult;
        }
    };

    public static void setSearchIndexData(Context context, String key,
            String title, String screenTtile, String summaryOn,
            String summaryOff, String intentAction, String intentTargetClass,
            String intentTargetPackage, int currentEnable,
            String preferenceType, String settingsDBTableName,
            String settingsDBField, int visible, int checkValue) {
        SearchIndexableRaw data = new SearchIndexableRaw(context);
        data.key = key;
        data.title = title;
        data.screenTitle = screenTtile;
        data.summaryOn = summaryOn;
        data.summaryOff = summaryOff;
        data.intentAction = intentAction;
        data.intentTargetClass = intentTargetClass;
        data.intentTargetPackage = intentTargetPackage;
        data.currentEnable = currentEnable;
        data.preferenceType = preferenceType;
        data.settingsDBTableName = settingsDBTableName;
        data.settingsDBField = settingsDBField;
        data.visible = visible;
        data.checkValue = checkValue;
        mResult.add(data);
    }
}
