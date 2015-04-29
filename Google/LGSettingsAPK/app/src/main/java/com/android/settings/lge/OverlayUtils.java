
package com.android.settings.lge;
import java.util.HashSet;
import static android.net.ConnectivityManager.TYPE_MOBILE;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionManager;
import android.text.TextUtils; 
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneStateIntentReceiver;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.SLog;
import com.android.settings.Utils;
import com.lge.constants.SettingsConstants;
import com.lge.telephony.provider.TelephonyProxy;
import com.lge.uicc.LGUiccCard.PinState;
import com.lge.uicc.LGUiccCard;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.android.settings.R;

import com.android.settings.utils.LGSubscriptionManager;

public class OverlayUtils {
    public static final String TAG = "OverlayUtils";
    public static final String LOG_TAG = "aboutphone , OverlayUtils";

    public static List<Long> s_SimMapKeyList = null;
    public static boolean s_HasSim = false;

    public static final int IN_SIM_NOT = 0;
    public static final int IN_SIM_1 = 1;
    public static final int IN_SIM_2 = 2;
    public static final int IN_SIM_3 = 4;
    public static final int IN_SIM_1_2 = 3;
    public static final int  IN_SIM_1_3 = 5;
    public static final int  IN_SIM_2_3 = 6;
    public static final int  IN_SIM_1_2_3 = 7;

    public static final int SIM_SLOT_1_SEL = 0;
    public static final int SIM_SLOT_2_SEL = 1;
    public static final int SIM_SLOT_3_SEL = 2;

    public static final int SIM_COMMON_SEL = 9999;

    public static Phone sPhone = null;

    public static String read_SharedPreference(String sp_key, Context context) {
        int sp_mode = Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE
                | Context.MODE_MULTI_PROCESS;
        return context.getSharedPreferences("dsim_test_preference", sp_mode).getString(sp_key, "");
    }

    public static int check_SIM_inserted(Context context) {
        int cnt = 0;
        boolean mSim1State = TelephonyManager.getDefault().hasIccCard(SIM_SLOT_1_SEL);
        boolean mSim2State = TelephonyManager.getDefault().hasIccCard(SIM_SLOT_2_SEL);
        boolean mSim3State = TelephonyManager.getDefault().hasIccCard(SIM_SLOT_3_SEL);
        if (mSim1State) {
            cnt += IN_SIM_1;
        }
        if (mSim2State) {
            cnt += IN_SIM_2;
        }
        if (Utils.isTripleSimEnabled()) {
            if (mSim3State) {
                cnt += IN_SIM_3;
            }
        }
        Log.i(LOG_TAG, "check_SIM_inserted = " + cnt);
        return cnt;
    }
    // [E][2012.09.22][yongjaeo.lee@lge.com][Dual_SIM] dual sim phoneNumber

    public static String get_SIM_state(Context context, int sim_num) {
        String rSim_state = "";
        PinState mSIM_STATE = new LGUiccCard(sim_num).getPin1State();
        if (mSIM_STATE == PinState.ENABLED_NOT_VERIFIED) {
            rSim_state = "pin_lock";
        } else if (mSIM_STATE == PinState.ENABLED_BLOCKED) {
            rSim_state = "puk_lock";
        } else if (mSIM_STATE == PinState.ENABLED_PERM_BLOCKED) {
            rSim_state = "perm_lock";
        } else {
            rSim_state = "ready";
        }
        return rSim_state;
    }

    public static void setCPUMode(Context context, int mode) {
        return;
    }

    public static int getCPUMode(Context context) {
        return 0;
    }

    public static boolean isX3Model() {
        return false;
    }

    public static void initNV(Context context) {
        if (context != null) {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setClassName("com.android.settings", "com.android.settings.lge.NVItemClear");
            context.startActivity(intent);
        }
        return;
    }

    public static boolean checkAppUninstallPolicies(String packageName) {
        return true;
    }
    public static boolean isPackageDisabled(String packageName) {
        return false;
    }
    public static boolean isWiFiBlocked3LM() {
        return false;
    }
    public static boolean isSsidAllowed3LM(String ssid) {
        return true;
    }

    public static boolean getBluetoothEnabled() {
        return false;
    }
    public static boolean getMultiUserEnabled() {
        return true;
    }

    public static boolean getAllowUsbDrive(ComponentName who) {
        if (com.lge.cappuccino.MdmSprint.getInstance() != null) {
            //ANDR-209 Customer Equipment MUST remove or grey out the removable drive
            //option in the PC connection menu when the DeviceManagement object USB_DRIVE is set to false.
            boolean allowUsbDrive = com.lge.cappuccino.MdmSprint.getInstance().getAllowUsbDrive(who);
            return allowUsbDrive;
        }
        return true;
    }
    public static boolean getAllowTethering(ComponentName who) {
        return true;
    }
    public static boolean getAllowUsbPort(ComponentName who) {
        if (com.lge.cappuccino.MdmSprint.getInstance() != null) {
            // ANDR-209 Customer Equipment MUST remove or grey out the removable
            // drive
            // option in the PC connection menu when the DeviceManagement object
            // USB_DRIVE is set to false.
            boolean allowUsbPort = com.lge.cappuccino.MdmSprint.getInstance().getAllowUsbPort(who);
            return allowUsbPort;
        }
        return true;
    }
    public static boolean isMultiSimEnabled() {
        return TelephonyManager.getDefault().isMultiSimEnabled();
    }

    public static int getSIMslotCount() {
        return TelephonyManager.getDefault().getPhoneCount();
    }

    public static String getNumeric(int mSubscription) {
        return TelephonyProxy.Carriers.getNumeric(mSubscription);
    }

    public static int sGEMINI_SIM_1 = 0;
    public static int sGEMINI_SIM_2 = 0;
    public static int sGEMINI_SIM_3 = 0;
    public static String sSIM_SLOT = "sim_slot"; // kk build :
                                                 // TelephonyProxy.Carriers.SIM_SLOT;
    public static String sMULTI_SIM_DATA_CALL_SUBSCRIPTION = SettingsConstants.System.MULTI_SIM_DATA_CALL_SUBSCRIPTION;

    // applied the VZW requirements(eri_version,
    // life_time_call, warranty_date_code) for About-phone.
    public static String LgSvcCmd_getCmdValue(int LgSvcCmdId) {
        return "NULL";
    }

    public static int LgSvcCmd_setCmdValue(int LgSvcCmdId, String zero) {
        return 0;
    }

    public static String LifeTime_NvBackup() {
        return "NULL";
    }

    public static String getEriVersion() {
        return "NULL";
    }

    public static void setRes_Regulatory(ImageView imgView) {
        return;
    }
    public static String get_device_IMEI() {
        return "0";
    }

    /* 2014-03-26 sujitk.mohapatra@lge.com LGSI_NWNAME_DEBITEL_HARD_CODING  [START]  */
    public static Phone getPhoneFactory(Context context) {
        String sTabName = read_SharedPreference("tab", context);
        Log.i(LOG_TAG, "sTabName : " + sTabName);
        if (sTabName.equals("sim1")) {
            sPhone = PhoneFactory.getPhone(SIM_SLOT_1_SEL);
        } else if (sTabName.equals("sim2")) {
            sPhone = PhoneFactory.getPhone(SIM_SLOT_2_SEL);
        } else if (sTabName.equals("sim3")) {
            sPhone = PhoneFactory.getPhone(SIM_SLOT_3_SEL);
        } else {
            return sPhone;
        }

        return sPhone;
    }    

    public static String getPhoneFactoryCtc(Context context, int mCtcSubscriber) {
        sPhone = getMSimPhoneFactoryCtc(context, mCtcSubscriber);
        Log.i(LOG_TAG, "getPhoneFactoryCtc sim : " + mCtcSubscriber);        
        return sPhone.getServiceState().getOperatorNumeric();
    }

    public static Phone getMSimPhoneFactoryCtc(Context context, int mCtcSubscriber) {
        Log.i(LOG_TAG, "getLGPhoneFactoryCtc sim : " + mCtcSubscriber);
        int mValue = TelephonyManager.getDefault().getPhoneCount();
        if (mCtcSubscriber < mValue) {
            sPhone = PhoneFactory.getPhone(mCtcSubscriber);
        }
        if (null == sPhone) {
            sPhone = PhoneFactory.getDefaultPhone();
        }
        return sPhone;
    }

/* 2014-03-26 sujitk.mohapatra@lge.com LGSI_NWNAME_DEBITEL_HARD_CODING  [End]  */

    public static int get_service_state(Context context) { // Service state
        Log.i(LOG_TAG, "get_service_state");

        String sTabName = read_SharedPreference("tab", context);
        int service_state = 0;
        Phone mPhone;

        if (sTabName.equals("sim1")) {
            service_state = PhoneFactory.getPhone(SIM_SLOT_1_SEL).getServiceState().getState();
        } else if (sTabName.equals("sim2")) {
            service_state = PhoneFactory.getPhone(SIM_SLOT_2_SEL).getServiceState().getState();
        } else if (sTabName.equals("sim3")) {
            service_state = PhoneFactory.getPhone(SIM_SLOT_3_SEL).getServiceState().getState();
        } else {
            return SIM_COMMON_SEL;
        }

        return service_state;
    }

    public static int get_service_state(Context context,
        PhoneStateIntentReceiver mPhoneStateReceiver) { 
        String sTabName = read_SharedPreference("tab", context);
        int service_state = 0;
        if (sTabName.equals("sim1")) {
            service_state = PhoneFactory.getPhone(SIM_SLOT_1_SEL).getServiceState().getState();
        } else if (sTabName.equals("sim2")) {
            service_state = PhoneFactory.getPhone(SIM_SLOT_2_SEL).getServiceState().getState();
        } else if (sTabName.equals("sim3")) {
            service_state = PhoneFactory.getPhone(SIM_SLOT_3_SEL).getServiceState().getState();
        } else {
            return SIM_COMMON_SEL;
        }
        return service_state;
    }
    public static CellLocation getCellLocation(Context context, int subscriber) { // Cell Id
        Log.i(LOG_TAG, "getCellLocation subscriber = " + subscriber);
        return PhoneFactory.getPhone(subscriber).getCellLocation();
    }

    public static int getDefaultPhoneID (Context mContext) {
        Log.i(LOG_TAG, "getDefaultPhoneID = "
            + LGSubscriptionManager.getDefaultDataPhoneIdBySubscriptionManager(mContext));
        return LGSubscriptionManager.getDefaultDataPhoneIdBySubscriptionManager(mContext);
    }

    public static String atClient_readValue_hw(int command, Context context) {
        return "NULL";
    }

    public static void atClient_BindService_hw(Context context) {
        return;
    }

    public static void atClient_unBindService_hw() {
        return;
    }

    public static final String[] sSimNameDBValue = {
            SettingsConstants.System.SIM1_NAME, SettingsConstants.System.SIM2_NAME,
            SettingsConstants.System.SIM3_NAME };

    private static final int DEFAULT_SIM_NAME[]  = {
        R.string.sp_defaultNameSIM1, R.string.sp_defaultNameSIM2,
        R.string.sp_defaultNameSIM3,
    };

    public static final String[] sSimNameDefaultValue = {
            "SIM card 1", "SIM card 2", "SIM card 3"
    };

    private static int getDefaultSimNameResourceIdx(int simId) {
        return DEFAULT_SIM_NAME[simId];
    }

    public static String getTranslatedSimName(Context mContext, final int simId) {
//        final Context context = PhoneGlobals.getInstance().getApplicationContext();
        return mContext.getResources().getText(getDefaultSimNameResourceIdx(simId)).toString();
    }
    


    // get the SimSlot Name by slot id.
    public static String getMSimTabName(Context mContext, String mSimSlotId) {
        int mMAX_SUBSCRIPTIONS;
        if (Utils.isTripleSimEnabled()) {
            mMAX_SUBSCRIPTIONS = 3;
        } else {
            mMAX_SUBSCRIPTIONS = 2;
        }

        int mSlotId;
        for (mSlotId = 0; mSlotId < mMAX_SUBSCRIPTIONS; mSlotId++) {
            if (sSimNameDBValue[mSlotId].equals(mSimSlotId)) {
                SLog.d(TAG, "mSimSlotId = " + mSimSlotId 
                    + " mSlotId : " + Integer.toString(mSlotId));
                break;
            }
        }

        String mSimName = getTranslatedSimName(mContext, mSlotId);
        mSimName = getTabName(mContext, mSlotId, mMAX_SUBSCRIPTIONS, mSimName);
        
        SLog.d(TAG, "mSimName Value  :  " + mSimName);
        //return getDefalutSimNameResourceIdx(mContext, mSlotId);
        return mSimName;
    }
    
    private static String getTabName(Context mContext, int mSlotId, int mMAX_SUBSCRIPTIONS
            , String mSimName) {
        if (mSlotId != mMAX_SUBSCRIPTIONS) {
            try {
                mSimName = Settings.System.getString(mContext.getContentResolver(),
                                sSimNameDBValue[mSlotId]);
                if ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
                    if (mSlotId == SIM_SLOT_2_SEL 
                        && ("SIM card".equals(mSimName) || "SIM card 2".equals(mSimName))) {
                        mSimName = mContext.getResources().getText(
                            R.string.settings_nfc_payment_rotue_simcard).toString();
                        } else if (mSimName.equals("Slot 1") || mSimName.equals("Slot 2")
                                || mSimName.equals("Slot 3")) {
                           mSimName = Utils.setSimName(mContext, mSlotId, mSimName);
                        }
                } else 
                if (sSimNameDefaultValue[mSlotId].equals(mSimName)) {
                    mSimName = getTranslatedSimName(mContext, mSlotId);
                } else
                if (TextUtils.isEmpty(mSimName)) {
                    //mSimName = getTranslatedSimName(mContext, mSlotId);
                    mSimName = getDefalutSimNameResourceIdx(mContext, mSlotId);
                }
            } catch (Exception e) {
                SLog.w(TAG, "Exception : ", e);
            }
        }
        
        return mSimName;
    }

    private static String getDefalutSimNameResourceIdx(Context mContext, int simId) {
        int ret = 0;
        SLog.d(TAG, "simId Value  :  " + simId);
        switch (simId) {
            case 0 :
                ret = R.string.sp_sim1_number_NORMAL;
                break;
            case 1 :
                ret = R.string.sp_sim2_number_NORMAL;
                break;
            case 2 :
                ret = R.string.sp_sim3_number_NORMAL;
                break;
            default :
                ret = R.string.sp_sim1_number_NORMAL;
                break;                
        }
        SLog.d(TAG, "mContext  :  " + mContext);
        SLog.d(TAG, "ret  :  " + ret);
        return mContext.getResources().getString(ret);
    }    
    

    public static boolean check_SIM_inserted(int mSlotId) {
        boolean mValue = TelephonyManager.getDefault().hasIccCard(mSlotId);
        SLog.i(LOG_TAG, "check_SIM_inserted = " + mValue);
        return mValue; 
    }

    public static boolean check_sim_lock_state (Context context, int sim_num) {
        String m_sim_state = get_SIM_state(context, sim_num);;
        boolean mValue = false;
        if (m_sim_state.equals("pin_lock") 
            || m_sim_state.equals("puk_lock") 
            || m_sim_state.equals("perm_lock")) {
            mValue = true;
        }
        SLog.i(LOG_TAG, "check_sim_lock_state = " + mValue);
        return mValue;
    }

    private static final String PINSTATE_UNKNOWN = "UNKNOWN"; //no sim
    private static final String PINSTATE_ENABLED_NOT_VERIFIED = "ENABLED_NOT_VERIFIED"; //pin locked
    private static final String PINSTATE_ENABLED_BLOCKED = "ENABLED_BLOCKED"; //puk locked
    private static final String PINSTATE_ENABLED_PERM_BLOCKED = "PERM_BLOCKED"; //perm locked
    private static final String PINSTATE_ENABLED_VERIFIED = "ENABLED_VERIFIED";   //checkbox
    private static final String PINSTATE_DISABLED = "DISABLED";  //checkbox

    public static final int SIM_PIN_STATE_ERROR = -1;
    public static final int PIN_STATE_UNKNOWN = 0;
    public static final int PIN_STATE_ENABLED_NOT_VERIFIED = 1;
    public static final int PIN_STATE_ENABLED_VERIFIED = 2;
    public static final int PIN_STATE_DISABLED = 3;
    public static final int PIN_STATE_ENABLED_BLOCKED = 4;
    public static final int PIN_STATE_ENABLED_PERM_BLOCKED = 5;

    //public static final int MAX_SUBSCRIPTIONS = SubscriptionManager.NUM_SUBSCRIPTIONS;

    private static final String SIMSTATE_UNKNOWN = "UNKNOWN";
    private static final String SIMSTATE_ABSENT = "ABSENT"; // absent or off
    private static final String SIMSTATE_PIN_REQUIRED = "PIN_REQUIRED"; // pin locked
    private static final String SIMSTATE_PUK_REQUIRED = "PUK_REQUIRED"; // puk locked
    private static final String SIMSTATE_PERSO_LOCKED = "PERSO_LOCKED"; // perso locked
    private static final String SIMSTATE_READY = "READY"; // ready

    private static final String SIMSTATE_NOT_READY = "NOT_READY"; // ready
    private static final String SIMSTATE_PERM_DISABLED = "PERM_DISABLED"; // ready
    private static final String SIMSTATE_CARD_IO_ERROR = "CARD_IO_ERROR"; // ready
    private static final String SIMSTATE_DETECTED = "DETECTED"; // ready

    public static final int SIM_STATE_UNKNOWN = 0;
    public static final int SIM_STATE_ABSENT = 1;
    public static final int SIM_STATE_PIN_REQUIRED = 2;
    public static final int SIM_STATE_PUK_REQUIRED = 3;
    public static final int SIM_STATE_PERSO_LOCKED = 4;
    public static final int SIM_STATE_READY = 5;

    public static final int SIM_STATE_NOT_READY = 6;
    public static final int SIM_STATE_PERM_DISABLED = 7;
    public static final int SIM_STATE_CARD_IO_ERROR = 8;
    public static final int SIM_STATE_DETECTED = 9;

    private static final String SIM_UNKNOWN = "SIM UNKNOWN";

    private static int updateSimStatus(String simState) {
        if (simState.equals(PINSTATE_UNKNOWN)) {
            return PIN_STATE_UNKNOWN;
        } else if (simState.equals(PINSTATE_ENABLED_NOT_VERIFIED)) {
            return PIN_STATE_ENABLED_NOT_VERIFIED;
        } else if (simState.equals(PINSTATE_ENABLED_BLOCKED)) {
            return PIN_STATE_ENABLED_BLOCKED;
        } else if (simState.equals(PINSTATE_ENABLED_PERM_BLOCKED)) {
            return PIN_STATE_ENABLED_PERM_BLOCKED;
        } else if (simState.equals(PINSTATE_ENABLED_VERIFIED)) {
            return PIN_STATE_ENABLED_VERIFIED;
        } else if (simState.equals(PINSTATE_DISABLED)) {
            return PIN_STATE_DISABLED;
        }
        return SIM_PIN_STATE_ERROR;
    }

    public static int getPinState(int sim_num) {
        String pinState = PINSTATE_UNKNOWN;
        try {
            pinState = new LGUiccCard(sim_num).getPin1State().toString();
        }
        catch (Exception e) {
            // e.printStackTrace();
            Log.e(LOG_TAG, "getPinState Exception");
        }
        Log.i(LOG_TAG, "sim_num : " + sim_num + ", pinState = " + pinState);
        return updateSimStatus(pinState);
    }

    private static int updatePropertySimStatus(String simState) {
        if (simState.equals(SIMSTATE_UNKNOWN)) {
            return SIM_STATE_UNKNOWN;
        } else if (simState.equals(SIMSTATE_ABSENT)) {
            return SIM_STATE_ABSENT;
        } else if (simState.equals(SIMSTATE_PIN_REQUIRED)) {
            return SIM_STATE_PIN_REQUIRED;
        } else if (simState.equals(SIMSTATE_PUK_REQUIRED)) {
            return SIM_STATE_PUK_REQUIRED;
        } else if (simState.equals(SIMSTATE_PERSO_LOCKED)) {
            return SIM_STATE_PERSO_LOCKED;
        } else if (simState.equals(SIMSTATE_READY)) {
            return SIM_STATE_READY;
        } else if (simState.equals(SIMSTATE_NOT_READY)) {
            return SIM_STATE_NOT_READY;
        } else if (simState.equals(SIMSTATE_PERM_DISABLED)) {
            return SIM_STATE_PERM_DISABLED;
        } else if (simState.equals(SIMSTATE_CARD_IO_ERROR)) {
            return SIM_STATE_CARD_IO_ERROR;
        } else if (simState.equals(SIMSTATE_DETECTED)) {
            return SIM_STATE_DETECTED;
        }

        return SIM_PIN_STATE_ERROR;
    }

    public static int get_PROPERTY_SIM_STATE(int sim_num) {
        String simState = SIMSTATE_UNKNOWN;
        try {
            simState = LGSubscriptionManager.getTelephonyProperty(LGSubscriptionManager.getSubIdBySlotId(sim_num),
                        TelephonyProperties.PROPERTY_SIM_STATE);
        } catch (Exception e) {
            Log.e(LOG_TAG, "get_PROPERTY_SIM_STATE Exception!");
        }
        Log.i(LOG_TAG, "sim_num : " + sim_num + ", simState = " + simState);
        getPinState(sim_num); // debug log
        return updatePropertySimStatus(simState);
    }
    public static boolean isEmptySim(int subscription) {
        boolean ret_value = false;

        switch (get_PROPERTY_SIM_STATE(subscription)) {
            case SIM_STATE_UNKNOWN:
            case SIM_STATE_ABSENT :
                ret_value = true;
                break;
            default :
                ret_value = false;
        }
        SLog.i(LOG_TAG, "isEmptySim: " + ret_value);
        return ret_value;
    }

    public static int get_current_sim_slot(Context context) {
        int mCurSimIdx = LGSubscriptionManager.getDefaultDataPhoneIdBySubscriptionManager(context);
        SLog.i(LOG_TAG, " = " + mCurSimIdx);
        return mCurSimIdx;
    }

    public static boolean getAvailableCurrNetworkSimState(Context context) {
        int mCurSimIdx = get_current_sim_slot(context);
        boolean mIsEmptySim = isEmptySim(mCurSimIdx);
        boolean mIsLockedSim = check_sim_lock_state(context, mCurSimIdx);
        boolean mIsRadioOffSim = isEmptySim(mCurSimIdx);
        ConnectivityManager conn = ConnectivityManager.from(context);
        TelephonyManager mSimStatusMgr = TelephonyManager.getDefault();
        mIsRadioOffSim = 
            conn.isNetworkSupported(TYPE_MOBILE) 
            && (mSimStatusMgr.getSimState(mCurSimIdx) == SIM_STATE_READY);
        SLog.i(LOG_TAG, "SIM STATE = " + mSimStatusMgr.getSimState(mCurSimIdx));
        SLog.i(LOG_TAG, "mIsRadioOffSim = " + mIsRadioOffSim);
        if (mIsEmptySim 
            || mIsLockedSim 
            || false == mIsRadioOffSim) {
            SLog.i(LOG_TAG, "getAvailableCurrNetworkSimState = false");
            return false;
        } else {
            SLog.i(LOG_TAG, "getAvailableCurrNetworkSimState = true");
            return true;
        }
    }

    public static void set_MsimTelephonyListener(
        Context context, 
        PhoneStateListener mPhoneStateListener) {
        TelephonyManager mPhone = TelephonyManager.getDefault();
        if (mPhone != null && mPhoneStateListener != null) {
            mPhone.listen(mPhoneStateListener, 
                PhoneStateListener.LISTEN_SERVICE_STATE
                | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                | PhoneStateListener.LISTEN_CELL_LOCATION
                | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                | PhoneStateListener.LISTEN_CALL_STATE);
        }
    }
    public static void release_MsimTelephonyListener(
        Context context, 
        PhoneStateListener mPhoneStateListener)  {
        TelephonyManager mPhone = TelephonyManager.getDefault();
        if (mPhone != null && mPhoneStateListener != null) {
            mPhone.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
}
