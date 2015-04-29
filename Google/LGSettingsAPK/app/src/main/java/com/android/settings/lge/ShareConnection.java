package com.android.settings.lge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;

import com.android.settings.R;
import com.android.settings.MiracastSwitchPreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.SmartShareBeamSwitchPreference;
import com.android.settings.Utils;
import com.android.settings.WirelessStorageSwitchPreference;
import com.android.settings.deviceinfo.UsbSettingsControl;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.remote.RemoteFragmentManager;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.lge.constants.SettingsConstants;
import com.lge.os.Build;
import android.provider.Settings;

//[E][2011.12.30][jaeyoon.hyun] Change Config.java location

//[START] change new nfcadapterIf
import android.preference.PreferenceGroup;
import com.android.settings.nfc.NfcSettingAdapter;
import com.android.settings.nfc.NfcSettingAdapterIf;
//[END]
import com.android.settings.nfc.preference.NfcSwitchPreference;
import android.nfc.NfcAdapter;

public class ShareConnection extends SettingsPreferenceFragment implements Indexable{
    private static final String TAG = "ShareConnection";

    private PreferenceCategory mMediaShare;
    private PreferenceCategory mDataTransfer;
    //[youngju.do]PCSuiteSetting_v4.1
    private PreferenceCategory mLGSoftware;
    //eunjungjudy.kim change switchpreference to preference for UI 4.2 scenario
    private PreferenceScreen mMiracast;
    private SmartShareBeamSwitchPreference mSmartBeam;

    //[youngju.do]PCSuiteSetting_v4.1
    private Preference mPCSuiteSettings;

    private static final String KEY_NEARBY_DEVICE = "nearby_device";
    private static final String KEY_SMARTSHARE = "smart_share_beam";
    private static final String CHECK_PACKAGE_NAME_SMARTSHARE = "com.lge.smartshare";
    //After A1(G2) & exception Main Launcher
    private static final String CHECK_LABEL_NAME_DLNA = "DLNA";

    //[youngju.do]PCSuiteSetting_v4.1
    private static final String KEY_LGSOFTWARE = "lg_software";
    private static final String KEY_PCSUITE_SETTINGS = "pcsuite_settings";
    private static final String PACKAGE_NAME_PCSUITE = "com.lge.sync";
    private static final String PACKAGE_NAME_PCSUITEUI = "com.lge.pcsyncui";
    private static final String CLASS_NAME_PCSUITEUI = "com.lge.pcsyncui.settings.PCSuiteSettingsActivity";
    private static final String INTENT_USB_CONNECT_STATUS = "usb_connect_status";

    private static final String KEY_PRINT_SETTINGS = "print_settings";
    private static final String KEY_MIRRORLINK_SETTINGS = "mirrorlink_settings";

    private static final String KEY_MIRACST_SETTINGS = "wifi_screen_settings";

    private static final int OPTION_ID_DLNA = 0x4321;
    private static final int SHARTSHARE_VERSIONCODE = 300000;

    //[START] change new nfcadapterIf
    private NfcSettingAdapterIf mNfcSettingAdapterIf;
    //[END]
    private Preference nearby_device;
    private Preference mPrintSettings;
    private Preference mMirrorLink;
    private NfcSwitchPreference mAndroidBeam;
    private NfcSwitchPreference mToggleNfc;

    private IntentFilter mFilterSSB = null;
    private BeamStatusReceiver mBeamReceiver = new BeamStatusReceiver();
    private static final String SSB_PACKAGE_NAME = "com.lge.smartsharepush";
    private static final String MirrorLink_APP_PACKAGE = "com.lge.mirrorlinkcertupdate";
    
    //search
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;
    static final String CONTENT_URI = "content://com.android.settings.search.SearchDBupdateProvider/prefs_index";

    public class BeamStatusReceiver extends BroadcastReceiver {

        public static final int BEAM_STATUS_ON = 1;
        public static final int BEAM_STATUS_OFF = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "broadcast received action : " + action);
            if (action.equals("smartsharebeam.status")) {
                int status = intent.getIntExtra("smartsharebeam.status.key", BEAM_STATUS_OFF);
                mSmartBeam.setEnabled(true);
                if (status == BEAM_STATUS_ON) {
                    Log.d(TAG, "SmartShare Beam On.");
                    mSmartBeam.setState(true);
                } else {
                    Log.d(TAG, "SmartShare Beam Off.");
                    mSmartBeam.setState(false);
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        final Activity activity = getActivity();
        int isDataTransferSupport = 0;

        setHasOptionsMenu(true);

        addPreferencesFromResource(R.xml.share_connection_category);

        mDataTransfer = (PreferenceCategory)getPreferenceScreen().findPreference("data_transfer");
        mMediaShare = (PreferenceCategory)getPreferenceScreen().findPreference("media_share");
        //[youngju.do]PCSuiteSetting_v4.1
        mLGSoftware = (PreferenceCategory)getPreferenceScreen().findPreference("lg_software");
        if (Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2) {
            mMediaShare.setTitle(R.string.sp_vmemo_screen_share_NORMAL_ux42);
            mLGSoftware.setTitle(R.string.pc_connection_ux42);
        }

        nearby_device = findPreference(KEY_NEARBY_DEVICE);
        mAndroidBeam = (NfcSwitchPreference)findPreference("android_beam_settings");
        mToggleNfc = (NfcSwitchPreference)findPreference("toggle_nfc");

        if (nearby_device != null) {
            if (!isNearbyMenu(getActivity())) {
                getPreferenceScreen().removePreference(nearby_device);
            } else {
                if (Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2) {
                    mDataTransfer.setTitle(R.string.sp_data_transfer_cat_jb_plus_ux42);                    
                } else {
                    mDataTransfer.setTitle(R.string.settings_shareconnect_data_transfer_title);
                }

                if (isEnableCheckPackage(getActivity(), CHECK_PACKAGE_NAME_SMARTSHARE)) {
                    nearby_device.setEnabled(true);
                } else {
                    nearby_device.setEnabled(false);
                }
                isDataTransferSupport++;
            }
        }

        mMirrorLink = findPreference(KEY_MIRRORLINK_SETTINGS);
        if (mMirrorLink != null) {
            if (!isSupportMirrorLink(getActivity())) {
                Log.d(TAG, "This device doesn't support MirrorLink");
                getPreferenceScreen().removePreference(mMirrorLink);
            }
        }

        //[START] change new nfcadapterIf
        mNfcSettingAdapterIf = NfcSettingAdapter.getInstance((Context)activity, (PreferenceGroup)getPreferenceScreen());

        if (mNfcSettingAdapterIf == null) {
            Log.d(TAG, "This device doesn't support NFC");
        } else {
            isDataTransferSupport++;
        }

        mSmartBeam = (SmartShareBeamSwitchPreference)findPreference(KEY_SMARTSHARE);

        if (Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2) {
            mSmartBeam.setSummary(R.string.sp_smartshare_beam_summary_Public_4_2);
        } else {
            mSmartBeam.setSummary(R.string.sp_smartshare_beam_summary_Public);
        }

        if (!isSupportSmartShareBeam(activity)) {
            if (mSmartBeam != null) {
                getPreferenceScreen().removePreference(mSmartBeam);
                mSmartBeam = null;
            }
        } else {
            isDataTransferSupport++;
        }

        if (isDataTransferSupport == 0) {
            getPreferenceScreen().removePreference(mDataTransfer);
        }
        //END
        //MediaShare
        //Miracast
        if (!getActivity().getPackageManager().hasSystemFeature("com.lge.software.wfdService")) {
            getPreferenceScreen().removePreference(findPreference("wifi_screen_settings"));
            if (mMediaShare != null) {
                getPreferenceScreen().removePreference(mMediaShare);
            }
        } else {
            if (mMediaShare != null) {
                mMiracast = (PreferenceScreen)findPreference("wifi_screen_settings");
            }
        }
        // TBD : get exact Condition for smartshare is excluded, eg. Package Not found, Capp value, or model.

        //[youngju.do]PCSuiteSetting_v4.1
        mPCSuiteSettings = (Preference)findPreference(KEY_PCSUITE_SETTINGS);

        if (null != mPCSuiteSettings) {
            if (Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2) {
                mPCSuiteSettings.setTitle(R.string.lg_bridge_title);
                mPCSuiteSettings.setSummary(R.string.sp_pcdavid_list_desc);
            }

            mPCSuiteSettings.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (Utils.supportRemoteFragment(getActivity()) == true) {
                        String fname = RemoteFragmentManager.getFragmentName(PACKAGE_NAME_PCSUITEUI);
                        if (fname != null) {
                            preference.setFragment(fname);
                            if (preference.getTitleRes() != 0) {
                                int resId = preference.getTitleRes();
                                preference.setTitle("wow");
                                preference.setTitle(getResources().getString(resId));
                            }

                            PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
                            preferenceActivity.onPreferenceStartFragment(ShareConnection.this, preference);
                            return true;
                        }
                    }

                    Intent intent = new Intent();
                    intent.setPackage(PACKAGE_NAME_PCSUITEUI);
                    intent.setClassName(PACKAGE_NAME_PCSUITEUI, CLASS_NAME_PCSUITEUI);
                    intent.addCategory("com.lge.pcsyncui.category");
                    intent.putExtra(INTENT_USB_CONNECT_STATUS, UsbSettingsControl.getUsbConnected(getActivity()));
                    startActivity(intent);
                    return true;
                }
            });
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-35][ID-MDM-167][ID-MDM-198][ID-MDM-42][ID-MDM-243]
        // [ID-MDM-242][ID-MDM-235][ID-MDM-75][ID-MDM-76][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
        // [ID-MDM-274][ID-MDM-47][ID-MDM-195]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            com.android.settings.MDMSettingsAdapter.getInstance()
                    .addWirelessSettingsPolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END

        //TODO add GK base Scenario

        //[youngju.do]PCSuiteSetting_v4.1
        if (!isPCSuiteUISupportSettingMenu(getActivity())) {
            getPreferenceScreen().removePreference(mPCSuiteSettings);
            if (!(Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2)) {
                getPreferenceScreen().removePreference(mLGSoftware);
            }
        } 
        removeCategoryPreference();

        mFilterSSB = new IntentFilter();
        mFilterSSB.addAction("smartsharebeam.status");
        mIsFirst = true;
        
    }
    private void removeCategoryPreference() {
        mPrintSettings = findPreference(KEY_PRINT_SETTINGS);

        if (Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2) {
            boolean isSecondaryUser = UserHandle.myUserId() != UserHandle.USER_OWNER;
            if ("CN".equals(Config.getCountry()) || Utils.isFolderModel(getActivity()) || isSecondaryUser) {
                if (mPrintSettings != null) {
                   getPreferenceScreen().removePreference(mPrintSettings);
                   getPreferenceScreen().removePreference(mLGSoftware);
                }
            }
        } else {
            if (mPrintSettings != null) {
                getPreferenceScreen().removePreference(mPrintSettings);
            }
        }
    }

    private static boolean isPCSuiteUISupportSettingMenu(Context context) {

        if (!isEnableCheckPackage(context, PACKAGE_NAME_PCSUITE)) {
            Log.e("YJDO", "[isPCSuiteUISupportSettingMenu] not support com.lge.sync");
            return false;
        }

        if (Utils.isUI_4_1_model(context)) {
            PackageManager pm = context.getPackageManager();
            if (null != pm) {
                try {
                    PackageInfo pkgInfo = pm.getPackageInfo(PACKAGE_NAME_PCSUITEUI, 0);
                    if (null != pkgInfo && (pkgInfo.versionCode >= 40200000)) {
                        return true;
                    } else {
                        Log.e("YJDO",
                                "[isPCSuiteUISupportSettingMenu] pcsyncui versionCode is lower than 40200000");
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("YJDO", "[isPCSuiteUISupportSettingMenu] not support com.lge.pcsyncui");
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if ("toggle_nfc".equals(preference.getKey())) {
            // yonguk.kim 2015-02-22 Apply remote fragment feature to NFC 
            if (Utils.supportRemoteFragment(getActivity()) == true) {
                PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
                boolean result = RemoteFragmentManager.setPreferenceForRemoteFragment("com.lge.NfcSettings", preference);
                if (result == true) {
                    preferenceActivity.onPreferenceStartFragment(this, preference);
                    return true;
                }
            }
           
        } else if (KEY_SMARTSHARE.equals(preference.getKey())) {
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47][ID-MDM-195]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.android.settings.MDMSettingsAdapter.getInstance()
                        .checkDisallowSmartShareBeam(getActivity(), true)) {
                    return super.onPreferenceTreeClick(preferenceScreen, preference);
                }
            }
            // LGMDM_END
            // yonguk.kim 2014-03-06 Apply remote fragment feature [START]
            if (Utils.supportRemoteFragment(getActivity()) == true) {
                PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
                boolean result = RemoteFragmentManager.setPreferenceForRemoteFragment("com.lge.smartsharepush", preference);
                if (result == true) {
                    preferenceActivity.onPreferenceStartFragment(this, preference);
                    return true;
                }
            }
            // yonguk.kim 2014-03-06 Apply remote fragment feature [END]

            Intent intent = new Intent("com.lge.smartsharebeam.setting");
            startActivity(intent);
        } 
        else if (KEY_NEARBY_DEVICE.equals(preference.getKey())) {
            
            if (Utils.supportRemoteFragment(getActivity()) == true) {
                if (gotoFragment(preference, CHECK_PACKAGE_NAME_SMARTSHARE) == true) {
                    return true;
                }
            }
            
            Intent intent = new Intent();
            intent.setAction("com.lge.smartshare.dlna.action.launcher");
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        else if (KEY_MIRRORLINK_SETTINGS.equals(preference.getKey())) {
            if (isSupportMirrorLink(getActivity()) == true) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.lge.mirrorlinkcertupdate", "com.lge.mirrorlinkcertupdate.MainActivity"));                
                startActivity(intent);
                return true;
            }
        }
        else if (KEY_MIRACST_SETTINGS.equals(preference.getKey())) {
            Log.d(TAG, "KEY_MIRACST_SETTINGS in shareConnection");
            Intent intent = new Intent();
            intent.setAction("android.settings.WIFI_SCREEN_SHARE");
            startActivity(intent);
        }
        //[START] change new nfcadapterIf
        else {
            if (mNfcSettingAdapterIf != null) {
                mNfcSettingAdapterIf.processPreferenceEvent(preference.getKey());
            }
        }
        //[END]


        // TODO Auto-generated method stub
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    private boolean gotoFragment(Preference preference, String packageName) {
        String fname = RemoteFragmentManager.getFragmentName(packageName);
        if (fname != null) {
            preference.setFragment(fname);
            if (preference.getTitleRes() != 0) {
                int resId = preference.getTitleRes();
                preference.setTitle("wow");
                preference.setTitle(getResources().getString(resId));
            }

            PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
            preferenceActivity.onPreferenceStartFragment(ShareConnection.this,
                    preference);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        getActivity().registerReceiver(mBeamReceiver, mFilterSSB);
        //[START] change new nfcadapterIf
        if (mNfcSettingAdapterIf != null) {
            mNfcSettingAdapterIf.resume();
        }
        if (mSmartBeam != null) {
            mSmartBeam.resume();
            mSmartBeam.setEnabled(true);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47][ID-MDM-277]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            // restrick miracast setting
            Preference miracastPref = findPreference("wifi_screen_settings");
            com.android.settings.MDMSettingsAdapter.getInstance().setMiracastPreference(miracastPref);
        }

        getActivity().invalidateOptionsMenu();
        // LGMDM_END
        // TODO Auto-generated method stub
        // TODO Need to get switch status
        super.onResume();

        if (nearby_device != null) {
            if (isEnableCheckPackage(getActivity(), CHECK_PACKAGE_NAME_SMARTSHARE)) {
                nearby_device.setEnabled(true);
            } else {
                nearby_device.setEnabled(false);
            }
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47][ID-MDM-195]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            com.android.settings.MDMSettingsAdapter.getInstance().setSmartShareBeamMenu(mSmartBeam);
        }
        // LGMDM_END
        
        //search
        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent()
                .getBooleanExtra("perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            boolean newValue =  getActivity().getIntent().getBooleanExtra("newValue", false);
            startResult(newValue);
            mIsFirst = false;
        }
    }
    
    //search
    private void startResult(boolean newValue) {
        /*if (mSearch_result.equals("toggle_nfc")) {
            mToggleNfc.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals("android_beam_settings")) {
            mAndroidBeam.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_SMARTSHARE)) {
            mSmartBeam.performClick(getPreferenceScreen());
        }*/
    }
    
    private void searchDBUpdate(boolean newValue, String key, String field) {
        Log.d("skku", "searchDBUpdate newValue : " + newValue + " key : " + key);
        ContentResolver cr = getActivity().getContentResolver();
        ContentValues row = new ContentValues();
        row.put(field, newValue ? 1 : 0);
        String where = "data_key_reference = ? AND class_name = ?";
        Log.d("skku", "searchDBUpdate where : " + where);
        cr.update(Uri.parse(CONTENT_URI), row, where, new String[] { key,
                "com.android.settings.PrivacySettings" });
    }
    
    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mBeamReceiver);

        //[START] change new nfcadapterIf
        if (mNfcSettingAdapterIf != null) {
            mNfcSettingAdapterIf.pause();
        }       
        //[END]
        if (mSmartBeam != null) {
            mSmartBeam.pause();
        }
        // TODO Auto-generated method stub

        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        //[START] change new nfcadapterIf
        if (mNfcSettingAdapterIf != null) {
            mNfcSettingAdapterIf.onConfigChange();
        }
        //[END]
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onPrepareOptionsMenu(menu);
        // [Start] rebestm - Use DLNA feature
        if (IsDLNAOptionMenu(CHECK_PACKAGE_NAME_SMARTSHARE)) {
            if (menu.findItem(OPTION_ID_DLNA) == null) {
                menu.add(0, OPTION_ID_DLNA, 0, getString(R.string.sp_shareconnect_dlna_option_NORMAL));
            }
        }
        //[End] rebestm - Use DLNA feature
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == android.R.id.home) {
            int settingStyle = Settings.System.getInt(getContentResolver(), SettingsConstants.System.SETTING_STYLE, 0);
            if (settingStyle == 1 && Utils.supportEasySettings(getActivity())) {
                Intent intent = new Intent();
                intent.setClassName("com.lge.settings.easy", "com.lge.settings.easy.EasySettings");
                intent.putExtra(Intent.EXTRA_TEXT, "networks");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } 
        } else if (item.getItemId() == OPTION_ID_DLNA) {
            Intent intent = new Intent();
            intent.setAction("com.lge.smartshare.dlna.action.launcher");
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.search) {
            switchTosearchResults();
        }

        return super.onOptionsItemSelected(item);
    }

    private void switchTosearchResults() {
        Intent intent = new Intent();
        intent.setAction("com.lge.settings.SETTINGS_SEARCH");
        intent.putExtra("search", true);
        startActivity(intent);
    }
    
    //[start] rebestm - Use DLNA feature
    public boolean IsDLNAOptionMenu(String packageName) {
        PackageManager pm = getPackageManager();
        android.content.pm.ApplicationInfo pmApplicationInfo = null;

        if (isNearbyMenu(getActivity())) {
            return false;
        }
        try {
            pmApplicationInfo = pm.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "NameNotFoundException com.lge.smartshare");
            return false;
        }

        if (pmApplicationInfo == null) {
            Log.e(TAG, "pmApplicationInfo is null");
            return false;
        }

        CharSequence charSequence = pm.getApplicationLabel(pmApplicationInfo);
        Log.d(TAG, "EnableApplication ->   " + charSequence);

        try {
            if (pm.getApplicationEnabledSetting(packageName) == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && charSequence.equals(CHECK_LABEL_NAME_DLNA)) {
                Log.d(TAG, "EnableApplication ->   " + pm.getApplicationEnabledSetting(packageName)); // 0
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception IsDLNAOptionMenu Disable");
            return false;
        }
        Log.e(TAG, "IsDLNAOptionMenu Disable");
        return false;
    }

    //[End] rebestm - Use DLNA feature
    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-35][ID-MDM-167][ID-MDM-198][ID-MDM-42][ID-MDM-243]
    // [ID-MDM-242][ID-MDM-235][ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
    // [ID-MDM-274][ID-MDM-47][ID-MDM-195]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.android.settings.MDMSettingsAdapter.getInstance()
                        .receiveWirelessSettingsPolicyChangeIntent(intent)) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        Log.i(TAG, "MDM Policy changed and activity finish");
                        activity.finish();
                    }
                }
            }
        }
    };

    // LGMDM_END

    @Override
    public void onDestroy() {
        super.onDestroy();

        //[START] change new nfcadapterIf
        if (mNfcSettingAdapterIf != null) {
            mNfcSettingAdapterIf.destroy(); ////[PORTING_FOR_NFCSETTING]
            mNfcSettingAdapterIf = null;
        }       
         //[END]
         
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-35][ID-MDM-167][ID-MDM-198][ID-MDM-42][ID-MDM-243]
        // [ID-MDM-242][ID-MDM-235][ID-MDM-75][ID-MDM-76][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
        // [ID-MDM-274][ID-MDM-47][ID-MDM-195]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
    }

    public boolean isCheckPackage(Context context, String packageName, int type) {
        PackageInfo pi = null;
        PackageManager pm = context.getPackageManager();
        try {
            pi = pm.getPackageInfo(packageName, type);
            if (pi.packageName.equals(packageName)) {
                return true;
            }
        } catch (NameNotFoundException e) {
            Log.w(TAG, "isCheckPackage() is not found(" + packageName + ")");
            return false;
        }
        return false;
    }

    public static boolean isEnableCheckPackage(Context context, String packageName) {
        PackageInfo pi = null;
        PackageManager pm = context.getPackageManager();
        try {
            pi = pm.getPackageInfo(packageName, 0);
            if (pi.packageName.equals(packageName) && (pi.applicationInfo.enabled == true)) {
                return true;
            }
        } catch (NameNotFoundException e) {
            Log.w(TAG, "isEnableCheckPackage() is not found(" + packageName + ")");
            return false;
        }
        return false;
    }

    public static boolean isNearbyMenu(Context context) {
        PackageInfo pi = null;
        int versionCode = 0;

        try {
            pi = context.getPackageManager().getPackageInfo(CHECK_PACKAGE_NAME_SMARTSHARE, 0);
            versionCode = pi.versionCode;
        } catch (NameNotFoundException e) {
            Log.w(TAG, "isNearbyMenu() is not found(" + CHECK_PACKAGE_NAME_SMARTSHARE + ")");
            return false;
        }

        if (versionCode >= SHARTSHARE_VERSIONCODE) {
            Log.i(TAG, "isNearbyMenu is true = " + versionCode);
            return true;
        }
        Log.i(TAG, "isNearbyMenu is false = " + versionCode);
        return false;
    }

    public static boolean isSupportSmartShareBeam(Context context) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo info = null;
        boolean isSupport = false;

        try {
            info = pm.getApplicationInfo(SSB_PACKAGE_NAME, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            Log.i(TAG, "isSupportSmartShareBeam result : false");
            return false;
        }
        
        boolean config_enabled = Config.getFWConfigBool(context, com.lge.config.ConfigBuildFlags.CAPP_WFDS_SEND, "com.lge.config.ConfigBuildFlags.CAPP_WFDS_SEND");
        
        isSupport = info.enabled || config_enabled;
        Log.i(TAG, "isSupportSmartShareBeam result : " + isSupport);
        return isSupport;
    }

    public static boolean isSupportMirrorLink(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(MirrorLink_APP_PACKAGE, 0) != null;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
	
    //search
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        
        private Map<String, Integer> mDisplayCheck = new HashMap<String, Integer>();
        
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            setRemoveVisible(context);
            setSearchIndexData(context, "share_connect",
                    context.getString(R.string.sp_title_share_connect_NORMAL_jb_plus), "main",
                    null, null, "android.settings.SHARECONNECTION",
                    null, null, 1,
                    null, null, null, 1, 0);
            if (mDisplayCheck.get("toggle_nfc") == null) {
                setSearchIndexData(context, "toggle_nfc",
                        context.getString(R.string.nfc_quick_toggle_title), 
                        context.getString(R.string.sp_title_share_connect_NORMAL_jb_plus),
                        context.getString(R.string.nfc_quick_toggle_summary_easily), 
                        null, "android.settings.TOGGLENFC",
                        null, null, 1,
                        null, null, null, 1, 0);
            }
            /*
            if (mDisplayCheck.get("sprint_manager") == null) {
                setSearchIndexData(context, "sprint_manager",
                        context.getString(R.string.settings_share_nfc_cards_title),
                        context.getString(R.string.sp_title_share_connect_NORMAL_jb_plus),
                        context.getString(R.string.settings_share_nfc_cards_summary), 
                        null, "android.intent.action.MAIN",
                        "com.sequent.controlpanel.MainActivity", "com.sequent.controlpanel", 1,
                        null, null, null, 1, 0);
            }*/
            
            if (mDisplayCheck.get("android_beam_settings") == null) {
                setSearchIndexData(context, "android_beam_settings",
                        context.getString(R.string.android_beam_settings_title),
                        context.getString(R.string.sp_title_share_connect_NORMAL_jb_plus),
                        context.getString(R.string.nfc_preference_direct_beam_summary_easily),  
                        null, "android.settings.TOGGLEANDROIDBEAM",
                        null, null, 1,
                        null, null, null, 1, 0);
            }

            /*
            if (mDisplayCheck.get("nfc_settings") == null) {
                setSearchIndexData(context, "nfc_settings",
                        context.getString(R.string.nfc_preference_title_main),
                        context.getString(R.string.sp_title_share_connect_NORMAL_jb_plus),
                        context.getString(R.string.nfc_preference_summary_main), 
                        null, "android.settings.SHARECONNECTION",
                        null, null, 1,
                        null, null, null, 1, 0);
            }*/
            
            if (mDisplayCheck.get(KEY_SMARTSHARE) == null) {
                setSearchIndexData(context, KEY_SMARTSHARE,
                        context.getString(R.string.sp_smart_share_title_NORMAL_jb_plus),
                        context.getString(R.string.sp_title_share_connect_NORMAL_jb_plus),
                        context.getString(R.string.sp_smartshare_beam_summary_Public), 
                        null, "com.lge.smartsharebeam.setting",
                        null, null, 1,
                        null, null, null, 1, 0);
            }            
            if (mDisplayCheck.get(KEY_NEARBY_DEVICE) == null) {
                setSearchIndexData(context, KEY_NEARBY_DEVICE,
                        context.getString(R.string.settings_shareconnect_mediaserver_title),
                        context.getString(R.string.sp_title_share_connect_NORMAL_jb_plus),
                        context.getString(R.string.settings_shareconnect_nearby_summary), 
                        null, "com.lge.smartshare.dlna.action.launcher",
                        null, null, 1,
                        null, null, null, 1, 0);
            }
            if (mDisplayCheck.get(KEY_MIRACST_SETTINGS) == null) {
                setSearchIndexData(context, KEY_MIRACST_SETTINGS,
                        context.getString(R.string.sp_miracast_title_ver2_NORMAL),
                        context.getString(R.string.sp_title_share_connect_NORMAL_jb_plus),
                        context.getString(R.string.sp_miracast_summary_ver3_NORMAL), 
                        null, "android.settings.WIFI_SCREEN_SHARE",
                        null, null, 1,
                        null, null, null, 1, 0);
            }
            if (mDisplayCheck.get(KEY_PRINT_SETTINGS) == null) {
                setSearchIndexData(context, KEY_PRINT_SETTINGS,
                        context.getString(R.string.print_settings), 
                        context.getString(R.string.sp_title_share_connect_NORMAL_jb_plus),
                        context.getString(R.string.wireless_printer_summary), 
                        null, "android.settings.ACTION_PRINT_SETTINGS",
                        null, null, 1,
                        null, null, null, 1, 0);
            }

            /*
            if (mDisplayCheck.get(KEY_MIRRORLINK_SETTINGS) == null) {
                setSearchIndexData(context, KEY_MIRRORLINK_SETTINGS,
                        context.getString(R.string.mirrorlink_title),
                        context.getString(R.string.sp_title_share_connect_NORMAL_jb_plus),
                        context.getString(R.string.mirrorlink_summary), 
                        null, "android.intent.action.MAIN",
                        "com.lge.mirrorlink", "com.lge.mirrorlink", 1,
                        null, null, null, 1, 0);
            }*/

            /*
            if (mDisplayCheck.get(KEY_PCSUITE_SETTINGS) == null) {
                setSearchIndexData(context, KEY_PCSUITE_SETTINGS,
                        context.getString(R.string.lg_bridge_title),
                        context.getString(R.string.sp_title_share_connect_NORMAL_jb_plus),
                        context.getString(R.string.sp_pcdavid_list_desc), 
                        null, "android.intent.action.MAIN",
                        "com.lge.pcsyncui", "com.lge.pcsyncui.settings.PCSuiteSettingsActivity", 1,
                        null, null, null, 1, 0);
            }*/
            
            return mResult;
        }

        public void setRemoveVisible(Context context) {
            if (!isNearbyMenu(context)) {
                mDisplayCheck.put(KEY_NEARBY_DEVICE, 0);
            }
            if (!isSupportMirrorLink(context)) {
                mDisplayCheck.put(KEY_MIRRORLINK_SETTINGS, 0);
            }

            if (!isSupportSmartShareBeam(context)) {
                mDisplayCheck.put(KEY_SMARTSHARE, 0);
            }

            if (!context.getPackageManager().hasSystemFeature(
                    "com.lge.software.wfdService")) {
                mDisplayCheck.put(KEY_MIRACST_SETTINGS, 0);
            }

            if (!isPCSuiteUISupportSettingMenu(context)) {
                mDisplayCheck.put(KEY_PCSUITE_SETTINGS, 0);
            }

            if (Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2) {
                boolean isSecondaryUser = UserHandle.myUserId() != UserHandle.USER_OWNER;

                if ("CN".equals(Config.getCountry())
                        || Utils.isFolderModel(context) || isSecondaryUser) {
                    mDisplayCheck.put(KEY_PRINT_SETTINGS, 0);
                }
            } else {
                mDisplayCheck.put(KEY_PRINT_SETTINGS, 0);
            }            

            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
            if (nfcAdapter == null) {
                mDisplayCheck.put("toggle_nfc", 0);
                mDisplayCheck.put("android_beam_settings", 0);
            }     
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
