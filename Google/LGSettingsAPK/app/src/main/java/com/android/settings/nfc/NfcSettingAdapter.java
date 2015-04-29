/*
 * A2 lab 4 team, Mobile Communication Company, LG ELECTRONICS INC., SEOUL, KOREA
 * Copyright(c) 2011 by LG Electronics Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LG Electronics Inc.
 */
package com.android.settings.nfc;

//[START][PORTING_FOR_NFCSETTING]
import android.app.Activity;
//[END][PORTING_FOR_NFCSETTING]
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ComponentName;

import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceGroup;

import android.telephony.TelephonyManager;

import android.util.Log;
import android.nfc.NfcAdapter;
import android.widget.Toast;

import com.lge.nfcconfig.NfcConfigure;
//NfcConfigure.IsNfcConfigureValue("AdvancedHCEEnable", "true")
import com.lge.nfcaddon.NfcAdapterAddon;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;

import java.lang.NullPointerException;
import com.android.settings.nfc.trigger.*;
import com.android.settings.nfc.preference.*;

//[START][PORTING_FOR_NFCSETTING]
import android.app.LGSharedPreferences;
//[END][PORTING_FOR_NFCSETTING]
public class NfcSettingAdapter implements NfcSettingAdapterIf {

    private static final String TAG = "NFC_SettingAdapter";

    public static final int NUMBER_TAG_DEFAULT_FIRSTCONNCET = 200;

    private static final String KEY_TOGGLE_NFC = "toggle_nfc";
    private static final String KEY_NFC_SETTINGS = "nfc_settings";
    private static final String KEY_ANDROID_BEAM_SETTINGS = "android_beam_settings";
    private static final String KEY_SPRINT_MANAGER = "sprint_manager";

    private static final int BIT_SET_FOR_TOGGLE_NFC   = 0x0001;
    private static final int BIT_SET_FOR_NFC_SETTING  = 0x0002;
    private static final int BIT_SET_FOR_ANDROID_BEAM = 0x0004;
    private static final int BIT_SET_FOR_SPRINT_MNGR  = 0x0008;

    private static NfcSettingAdapter sInstance = null;

    private LGNfcEnabler mEnablerObj = null;

    private NfcAdapter mNfcAdapter = null;
    private Context mCtx = null;
    private PreferenceGroup mPreferGroupData = null;

    private NfcSettingAdapter () {
        Log.d(TAG, "Create Object instance.");
    }
    private NfcSettingAdapter (Context ctx, PreferenceGroup preferGroupData) throws NullPointerException {
        if (this.init(ctx, preferGroupData) == false) {
            throw new NullPointerException();
        }
    }
    private boolean init(Context ctx, PreferenceGroup preferGroupData) {

        mPreferGroupData = preferGroupData;
        mCtx = ctx;
        mNfcAdapter = NfcAdapter.getDefaultAdapter(mCtx);

        if (mNfcAdapter == null) {
            removePreference(BIT_SET_FOR_TOGGLE_NFC | BIT_SET_FOR_NFC_SETTING | BIT_SET_FOR_ANDROID_BEAM | BIT_SET_FOR_SPRINT_MNGR);
            return false;
        }

        NfcUtils.createNfcConfigure(mCtx);  // wonjong77.lee [2014-08-26]

        if (NfcSettingAdapter.NfcUtils.hasLockOperation()) {
            Log.d(TAG, "hasFeatureNfcLock");

            PreferenceScreen nfc_setting = (PreferenceScreen)mPreferGroupData.findPreference(KEY_NFC_SETTINGS);

            removePreference(BIT_SET_FOR_TOGGLE_NFC | BIT_SET_FOR_ANDROID_BEAM);

            if (NfcSettingAdapter.NfcUtils.hasOperator("KDDI")) {
                nfc_setting.setSummary(R.string.nfc_preference_summary_main);
            } else {
                nfc_setting.setTitle(R.string.nfc_preference_title_main_dcm);
                nfc_setting.setSummary(R.string.nfc_preference_summary_main_dcm);
            }

            mEnablerObj = new LGNfcEnabler(mCtx, nfc_setting);

        } else {

            NfcSwitchPreference nfc_toggle_setting = (NfcSwitchPreference)mPreferGroupData.findPreference(KEY_TOGGLE_NFC);
            removePreference(BIT_SET_FOR_NFC_SETTING);

            //jw_skt_modify_menu
            if (NfcSettingAdapter.NfcUtils.hasInner()) {
                removePreference (BIT_SET_FOR_ANDROID_BEAM);
                mEnablerObj = new LGNfcEnabler(mCtx, nfc_toggle_setting);

            } else if (Utils.isWifiOnly(mCtx)) {
                removePreference (BIT_SET_FOR_ANDROID_BEAM);
                nfc_toggle_setting.setSummary(R.string.nfc_quick_toggle_summary_easily_tablet);
                mEnablerObj = new LGNfcEnabler(mCtx, nfc_toggle_setting);
            } else {

                Preference androidBeam = (Preference)mPreferGroupData.findPreference(KEY_ANDROID_BEAM_SETTINGS);

                if (NfcSettingAdapter.NfcUtils.hasCountry("FR")) {
                    nfc_toggle_setting.setSummary(R.string.settings_share_nfc_summary_fr);
                 } else if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                     nfc_toggle_setting.setSummary(R.string.nfc_quick_toggle_summary_easily_dualsim);
                 } else {
                     nfc_toggle_setting.setSummary(R.string.nfc_quick_toggle_summary_easily);
                 }

                mEnablerObj = new LGNfcEnabler(mCtx, (NfcSwitchPreference)nfc_toggle_setting, (NfcSwitchPreference)androidBeam);
            }
        }

        if (false == Utils.hasSprintTouchV2(mCtx)) {
            Log.d(TAG, "doesn't have sprint touch v2 or up version.");
            removePreference(BIT_SET_FOR_SPRINT_MNGR);
        }

        return true;
    }

    private void removePreference(int removedSet) {

        if ((removedSet & BIT_SET_FOR_TOGGLE_NFC) != 0 ) {

            Preference nfc = (Preference)mPreferGroupData.findPreference(KEY_TOGGLE_NFC);

            if (nfc != null) {
                mPreferGroupData.removePreference(nfc);
            }
        }

        if ((removedSet & BIT_SET_FOR_NFC_SETTING) != 0) {

            Preference nfc_settings = (Preference)mPreferGroupData.findPreference(KEY_NFC_SETTINGS);

            if (nfc_settings != null) {
                mPreferGroupData.removePreference(nfc_settings);
            }
        }

        if ((removedSet & BIT_SET_FOR_ANDROID_BEAM) != 0) {

            Preference androidBeam = (Preference)mPreferGroupData.findPreference(KEY_ANDROID_BEAM_SETTINGS);

            if (androidBeam != null) {
                mPreferGroupData.removePreference(androidBeam);
                }
        }

        if ((removedSet & BIT_SET_FOR_SPRINT_MNGR) != 0) {

            Preference sprint_manager = (Preference)mPreferGroupData.findPreference(KEY_SPRINT_MANAGER);

            if (sprint_manager != null) {
                mPreferGroupData.removePreference(sprint_manager);
            }

        }
    }

    private void removePreferenceProcess() {

        if (NfcSettingAdapter.NfcUtils.hasLockOperation()) {
            Log.d(TAG, "hasFeatureNfcLock");
            removePreference(BIT_SET_FOR_TOGGLE_NFC | BIT_SET_FOR_ANDROID_BEAM);

        } else {

            removePreference(BIT_SET_FOR_NFC_SETTING);

            if (NfcSettingAdapter.NfcUtils.hasInner()) {
                removePreference (BIT_SET_FOR_ANDROID_BEAM);
            }
        }

        if (false == Utils.hasSprintTouchV2(mCtx)) {
            Log.d(TAG, "doesn't have sprint touch v2 or up version.");
            removePreference(BIT_SET_FOR_SPRINT_MNGR);
        }
    }
    public static synchronized NfcSettingAdapter getInstance(Context ctx, PreferenceGroup preferGroupData) {
        Log.d(TAG, "ctx hash code : " + ctx.hashCode());
        Log.d(TAG, "preferGroupData hash code : " + preferGroupData.hashCode());

        NfcSettingAdapter obj = null;

        try {
            obj = new NfcSettingAdapter(ctx, preferGroupData);
        } catch (NullPointerException e) {
            Log.d(TAG, "get instance is null....");
        }

        return obj;
    }

    @Override
    public void resume() {
        Log.d(TAG, "resume()");

        if (mEnablerObj != null) {
            mEnablerObj.resume();
         }
    }

    @Override
    public void pause() {
        Log.d(TAG, "pause()");

        if (mEnablerObj != null) {
            mEnablerObj.pause();
         }
    }
    //[START][PORTING_FOR_NFCSETTING]
    @Override
    public void destroy() {
        Log.d(TAG, "destroy()");

        if (mEnablerObj != null) {
            mEnablerObj.destroy();
         }
    }
    //[END][PORTING_FOR_NFCSETTING]
    @Override
    public void onConfigChange() {
        Log.d(TAG, "pause()");

        if (mEnablerObj != null) {
            mEnablerObj.onConfigChange();
        }
    }

    public boolean processPreferenceEvent(String eventValue) {
        boolean result = true;

        if (KEY_NFC_SETTINGS.equals(eventValue)) {

            if (TelephonyManager.getDefault().getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
                Intent intent = new Intent("android.settings.NFC_SETTINGS");
                intent.putExtra("FromSettings", true);
                mCtx.startActivity(intent);

            } else {
                if (NfcSettingAdapter.NfcUtils.hasOperator("KDDI")) {
                    //[START][PORTING_FOR_NFCSETTING]
                    //SharedPreferences preferences = mCtx.getSharedPreferences("NfcUIMinit", 0);
                    //boolean bInterNFC = preferences.getBoolean("NfcUIMinit", false);
                    SharedPreferences pf = LGSharedPreferences.get(mCtx, "NfcUIMinit", Context.MODE_MULTI_PROCESS);
                    boolean bInterNFC = pf.getBoolean("NfcUIMinit", false);
                    //[END][PORTING_FOR_NFCSETTING]
                    Log.d(TAG, "bInterNFC = " + bInterNFC);

                    if (bInterNFC) {
                        Intent intent = new Intent("android.settings.NFC_SETTINGS");
                        intent.putExtra("FromSettings", true);
                        mCtx.startActivity(intent);
                    } else {
                        Toast.makeText(mCtx, R.string.nfc_toast_sim_state_unknown_docomo, Toast.LENGTH_LONG).show();
                        result = false;
                    }
                } else if ((NfcSettingAdapter.NfcUtils.hasOperator("DCM"))) {
                    if (TelephonyManager.SIM_STATE_UNKNOWN == TelephonyManager.getDefault().getSimState()) {
                        // In ECM mode launch ECM app dialog
                        Log.d(TAG, "onPreferenceTreeClick - uim not Ready!");
                        result = false;
                    }
                }
            }

        }

        return result;

    }

    //VZW_JW_Start
    static public int isUnchecked(Context ctx, int id) {

        if (id != NUMBER_TAG_DEFAULT_FIRSTCONNCET) {
            Log.e(TAG, "isUnchecked - NFC_FIRST_ON_CHECK error");
            return 1;
        }

        int isCheck = android.provider.Settings.Global.getInt(ctx.getContentResolver(), SettingsConstants.Global.NFC_FIRST_ON_CHECK, 0);
        Log.d(TAG, "NFC_FIRST_ON_CHECK = " + isCheck);
        return isCheck;
    }

    static public void markUnchecked(Context ctx, int id) {
        if (id != NUMBER_TAG_DEFAULT_FIRSTCONNCET) {
            Log.e(TAG, "markUnchecked - NFC_FIRST_ON_CHECK error");
            return;
        }
        android.provider.Settings.Global.putInt(ctx.getContentResolver(), SettingsConstants.Global.NFC_FIRST_ON_CHECK, 1);
    }

    class NfcSettingAdapterInstanceException extends Exception {

        NfcSettingAdapterInstanceException() {
            super("Can not create NfcSettingAdapter Instance");
        }
    }

    public static class NfcUtils {
    // This class consists of that static functions have some utils about nfc operation.
        public static final String[] innerOperator = {"SKT", "LGU", "KT", "CMCC"};
        public static final String[] beamInP2pOperator = {"SKT", "LGU", "KT", "CMCC"};
        public static final String[] koreaOperator = {"SKT", "LGU", "KT"};

        private static NfcConfigure sConfig;
        //[START][PORTING_FOR_NFCSETTING]
        static boolean sIsUseNfcOffDlg = false;

        public static boolean isShowNfcOffDialog() {
            return sIsUseNfcOffDlg;
        }

        public static void setShowNfcOffDialog(boolean isUse) {
            sIsUseNfcOffDlg = isUse;
        }
        //[END][PORTING_FOR_NFCSETTING]

        public static boolean hasLockOperation() {
            // instead of hasFeatureNfcLock();
            boolean result = false;
            
            if ("JP".equals(Config.getCountry())) {

                if (NfcConfigure.IsNfcConfigureValue(NfcConfigure.NFC_SECUREELEMENT_TYPE, NfcConfigure.SecureElementList.INITVALUE) == false) {
                    Log.d (TAG, "Operator is JP, but SE is supported and Lock needs to support ...");
                    result = true;
                } else {
                    Log.d (TAG, "Operator is JP, but SE is not supported...");
                }
            }

            return result;
        }
        public static void createNfcConfigure (Context ctx) {
            sConfig = NfcConfigure.getInstance(ctx);
        }

        public static boolean hasOperator(String operator) {
            String curOperator = Config.getOperator();

            Log.d (TAG, " current operator : " + curOperator + " , compared operator : " + operator);

            return operator.equals(curOperator);
        }
        //[CHECK][START][PORTING_NFC]
        public static boolean hasOperator(String[] operators) {

            boolean result = false;
            String curOperator = Config.getOperator();

            if (curOperator == null || operators == null) {
                Log.e (TAG, "[hasOperator] curOperator is null or operators param is null.");
                return false;
            }

            for (String operator : operators) {
                if (curOperator.equals(operator)) {
                    result = true;
                    break;
                 }
            }

            return result;
        }
        //[CHECK][END][PORTING_NFC]
        public static boolean hasCountry(String contry) {
            String curCountry = Config.getCountry();
            
            Log.d (TAG, " current country : " + curCountry + " , compared country : " + contry);

            return contry.equals(curCountry);
        }

        public static boolean hasInner() {
        // instead of Utils.hasFeatureNfcInner()
            boolean result = false;

            String type = NfcConfigure.getNfcConfigureValue("IndicatorType");

            Log.d(TAG, "[hasInner]");

            if (type != null && type.startsWith("three")) {

                result = true;
            }

            Log.d(TAG, "[hasInner] type = " + type);

            return result;
            
        }

        public static boolean hasBeamInP2p() {
        // instead of Utils.hasFeatureNfcP2P
            boolean result = false;

            String type = NfcConfigure.getNfcConfigureValue("IndicatorType");

            Log.d(TAG, "[hasBeamInP2p");

            if (type != null && type.startsWith("three")) {
                result = true;
            }

            Log.d(TAG, "[hasBeamInP2p] type = " + type);

            return result;

        }

        // [MLM][NFC-857][START]
        public static boolean hasCardEmulation() {
        // instead of Utils.hasI30NfcSetting
            boolean result = false;
            if (NfcConfigure.IsNfcConfigureValue(NfcConfigure.NFC_SECUREELEMENT_TYPE,
                NfcConfigure.SecureElementList.INITVALUE) == false) {
                result = true;
            }

            return result;
        }
        // [MLM][NFC-857][END]

        public static boolean isActivatedTapPay(Context ctx) {
        // This function was renamed from isTapPay to this.
        // This function shold be used after isSupportedTapPay function was called and returned true.
            boolean result = false;
            NfcAdapter adapter = NfcAdapter.getDefaultAdapter(ctx);
            
            if (adapter != null) {

                if (adapter.isEnabled()) {
                    result = true;
                } else {

                    if (hasInner()) {
                        NfcAdapterAddon nfcAdapterAddon = NfcAdapterAddon.getNfcAdapterAddon();
                        
                        if (nfcAdapterAddon != null && nfcAdapterAddon.isNfcCardModeEnabled()) {
                            result = true;
                        }
                    }

                }
            }
            
            Log.d (TAG, "[isActivatedTapPay] result = " + result);

            return result;
        }

        public static boolean isSupportedTapPay(Context ctx) {
            boolean result = false;

            if (ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {

                if (ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)) {

                    result = true;
                }
            }

            Log.d (TAG, "[isSupportedTapPay] result = " + result);

            return result;
        }

        public static boolean hasNfcDisplaySettings() {
            Log.d (TAG, "[hasNfcDisplaySettings] ");

            return hasOperator(Config.VZW);
        }

        //[START][PORTING_FOR_NFCSETTING]
        public static boolean isSupportedRemoteNfcSettings(Activity atv) {

            boolean isSupport = false;
            Intent intent = atv.getIntent();
                    
            if (intent != null 
                    && Utils.checkPackage(atv.getApplicationContext(), "com.lge.NfcSettings")) {
                isSupport = true;
            }

            Log.d (TAG, "[isSupportedRemoteNfcSettings] isSupport : " + isSupport);

            return isSupport;
        }
        //[END][PORTING_FOR_NFCSETTING]
    }
    //[START][PORTING_FOR_NFCSETTING]
    //wonjong77.lee - for seperating NFC Setting Module...
    public static class ExtRequestConnection {
            private final static String TAG = "NfcSetting_ExtRequest";

            public static int REQUEST_POPUP_BEAM_DLG_IN_AIRPLANE = 1;
            public static int REQUEST_POPUP_NFC_DLG_IN_AIRPLANE = 2;
            public static int REQUEST_POPUP_NFC_DLG_IN_FIRST_CONNECT_OFF = 3;
            public static int REQUEST_POPUP_NFC_DLG_IN_NFC_OFF = 4;
            public static int REQUEST_POPUP_NFC_DLG_IN_BEAM_OFF = 5;

            public static String REQUEST_START_NFC_SETTING_FRAGMENT = "NfcSettingsFragments";
            public static String REQUEST_START_NFC_SETTING = "NfcSettings";
            public static String REQUEST_START_NFC_ANDROID_BEAM = "AndroidBeam";
            public static String REQUEST_START_NFC_PAYMENT_SETTING = "NfcSettingsPayment";

            public static String SEND_BROADCAST_TYPE_FOR_MAIN_TRIGGER = "main_trigger";
            public static String SEND_BROADCAST_TYPE_FOR_CARD_TRIGGER = "card_trigger";
            public static String SEND_BROADCAST_TYPE_FOR_P2P_BEAM_TRIGGER = "p2p_beam_trigger";

            private static boolean isUseTriggerActivity = false;

            public static void startActivityForPopup(Context ctx, int type) {
                    /*
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.setComponent( new ComponentName ("com.lge.NfcSettings", "com.lge.NfcSettings.PopupActivity"));
                                intent.putExtra("POPUP_TYPE", type);
                                */
                    Intent intent = new Intent("com.lge.nfcsettings.ACTION_NFC_SETTING_POPUP");
                    intent.putExtra("POPUP_TYPE", type);
                    ctx.startActivity(intent);
            }

            public static void startActivityForNfcSetting(Context ctx, String className) {
                /*
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.setComponent( new ComponentName ("com.lge.NfcSettings", "com.lge.NfcSettings.MainActivity"));
                            intent.addCategory("com.lge.NfcSettings.LUNCH");
                            intent.putExtra("CLASS_NAME", className);
                        */
                Intent intent = new Intent("com.lge.nfcsettings.ACTION_NFC_SETTING_MAIN");
                intent.putExtra("CLASS_NAME", className);
                ctx.startActivity(intent);
            }

            public static void startActivityForTrigger(Context ctx, String triggerType, boolean isEnabled) {
                Intent intent = new Intent("com.lge.nfcsettings.ACTION_NFC_SETTING_FOR_TRIGGER");

                intent.putExtra("TRIGGER_TYPE", triggerType);
                intent.putExtra("IS_ENABLED", isEnabled);
                intent.setPackage("com.lge.NfcSettings");
                setTriggerActivityStatus(true);
                ctx.startActivity(intent);
            }

            public static void sendBroadcastForTrigger(Context ctx, String triggerType, boolean isEnabled) {
                Intent intent = new Intent("com.lge.nfcsettings.ACTION_NFC_SETTING_FOR_TRIGGER");

                intent.putExtra("TRIGGER_TYPE", triggerType);
                intent.putExtra("IS_ENABLED", isEnabled);
                intent.setPackage("com.lge.NfcSettings");
                ctx.sendBroadcast(intent);
            }

            public static void setTriggerActivityStatus(boolean isUse) {
                isUseTriggerActivity = isUse;
                Log.d(TAG, "[setTriggerActivityStatus] isUseTriggerActivity : " + isUseTriggerActivity);
            }

            public static boolean getTriggerActivityStatus() {
                return isUseTriggerActivity;
            }
    }

    public static class ExtResponseProcess {
        private final static String TAG = "NfcSetting_ExtResponse";

        private Context mCtx;
        private NfcAdapterAddon mNfcAdapterAddon = NfcAdapterAddon.getNfcAdapterAddon();

        private NfcSwitchPreference mSwitchNfc;
        private NfcSwitchPreference mSwitchBeam;

        private static final String ACTION_POPUP_BEAM_RESULT_IN_AIRPLANE = "com.lge.nfcsettings.action.POPUP_BEAM_AIRPLANE";
        private static final String ACTION_POPUP_NFC_RESULT_IN_AIRPLANE = "com.lge.nfcsettings.action.POPUP_NFC_AIRPLANE";
        private static final String ACTION_POPUP_NFC_RESULT_IN_FIRST_CONNECT_OFF = "com.lge.nfcsettings.action.POPUP_NFC_FIRST_CONECT_OFF";
        private static final String ACTION_POPUP_NFC_RESULT_IN_OFF = "com.lge.nfcsettings.action.POPUP_NFC_IN_OFF";
        private static final String ACTION_POPUP_NFC_RESULT_IN_BEAM_OFF = "com.lge.nfcsettings.action.POPUP_NFC_IN_BEAM_OFF";

        private final BroadcastReceiver mProcessBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            // handling results from external nfssetting popup
                String action = intent.getAction();

                boolean bIsPositiveBtn = intent.getBooleanExtra("IS_POSITIVE_BTN", false);
                boolean bIsFirstConn = intent.getBooleanExtra("IS_FIRST_CONNECT", false);
                
                Log.d(TAG, "[showNfcOffDlg] bIsPositiveBtn : " + bIsPositiveBtn);

                if (ACTION_POPUP_BEAM_RESULT_IN_AIRPLANE.equals(action)) {
                    
                    if (bIsPositiveBtn) {
                        if (bIsFirstConn) {
                            ExtRequestConnection.startActivityForPopup(mCtx,
                                ExtRequestConnection.REQUEST_POPUP_NFC_DLG_IN_FIRST_CONNECT_OFF);
                        } else {
                            if (NfcSettingAdapter.NfcUtils.hasBeamInP2p()) {
                                Toast.makeText(mCtx, R.string.nfc_toast_trun_on_nfc_rw, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mCtx, R.string.nfc_toast_turn_on_nfc, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        mSwitchBeam.setChecked(false);
                    }

                } else if (ACTION_POPUP_NFC_RESULT_IN_AIRPLANE.equals(action)) {

                    if (bIsPositiveBtn) {

                        if (bIsFirstConn) {
                            ExtRequestConnection.startActivityForPopup(mCtx,
                                ExtRequestConnection.REQUEST_POPUP_NFC_DLG_IN_FIRST_CONNECT_OFF);
                        } else {
                            NfcTriggerIf mainTrigger = new NfcMainTrigger(mCtx);
                            mainTrigger.trigger(true);
                            Toast.makeText(mCtx, R.string.sp_toast_under_airplane_mode, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        mSwitchNfc.setChecked(false);
                    }

                } else if (ACTION_POPUP_NFC_RESULT_IN_FIRST_CONNECT_OFF.equals(action)) {

                    if (bIsPositiveBtn) {
                        if (NfcBeamSwitchPref.getFirstBeamStatusStatus()) {
                            NfcBeamSwitchPref.setFirstBeamStatusStatus(false);

                           if (NfcSettingAdapter.NfcUtils.hasBeamInP2p()) {
                             Toast.makeText(mCtx, R.string.nfc_toast_trun_on_nfc_rw, Toast.LENGTH_SHORT).show();
                            } else {
                             Toast.makeText(mCtx, R.string.nfc_toast_turn_on_nfc, Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                    } else {
                        NfcBeamSwitchPref.setFirstBeamStatusStatus(false);
                        mSwitchNfc.setChecked(false);
                        mSwitchBeam.setChecked(false);
                    }

                } else if (ACTION_POPUP_NFC_RESULT_IN_OFF.equals(action)) {
                    
                    if (bIsPositiveBtn == false) {
                        
                        if (mSwitchNfc != null) {
                            // [MLN][NFC-1245]
                            if (mNfcAdapterAddon.isNfcSystemEnabled()) {
                            // [MLM][NFC-812][START]
                                mSwitchNfc.setChecked(true);
                            // [MLM][NFC-812][END]
                            }
                        } else {
                            Log.e(TAG, "[showNfcOffDlg] mSwitchNfc is null...");
                        }
                    }
                    
                    NfcUtils.setShowNfcOffDialog(false);

                } else if (ACTION_POPUP_NFC_RESULT_IN_BEAM_OFF.equals(action)) {
                    Log.d (TAG, "[onReceive] : ACTION_POPUP_NFC_RESULT_IN_BEAM_OFF Action from external NfcSetting.");

                } else {
                    Log.e (TAG, "[onReceive] : Receiver does not receive valid Action from external NfcSetting.");
                }
            
                return;
            }
        };

        public ExtResponseProcess(Context ctx, NfcSwitchPreference switchNfc, NfcSwitchPreference switchBeam) {
            mCtx = ctx;

            mSwitchNfc = (switchNfc != null) ? switchNfc : null;
            mSwitchBeam = (switchNfc != null) ? switchBeam : null;
        }

        public void registerBroadcastReceiver() {
            IntentFilter filter = new IntentFilter();

            filter.addAction(ACTION_POPUP_BEAM_RESULT_IN_AIRPLANE);
            filter.addAction(ACTION_POPUP_NFC_RESULT_IN_AIRPLANE);
            filter.addAction(ACTION_POPUP_NFC_RESULT_IN_FIRST_CONNECT_OFF);
            filter.addAction(ACTION_POPUP_NFC_RESULT_IN_OFF);
            filter.addAction(ACTION_POPUP_NFC_RESULT_IN_BEAM_OFF);
            
            mCtx.registerReceiver(mProcessBR, filter);
        }

        public void unregisterBroadcastReceiver() {
            mCtx.unregisterReceiver(mProcessBR);
        }

    }
    //[END][PORTING_FOR_NFCSETTING]
}
