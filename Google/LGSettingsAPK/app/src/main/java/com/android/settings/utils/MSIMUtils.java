package com.android.settings.utils;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneStateIntentReceiver;

import com.android.settings.utils.LGSubscriptionManager;

public class MSIMUtils {
    public static final String LOG_TAG = "MSIMUtils";

    public static final int SIM_SLOT_1_SEL = 0;
    public static final int SIM_SLOT_2_SEL = 1;
    public static final int SIM_SLOT_3_SEL = 2;
    
    public static final int INVALID_SUB_ID = -1000; //INVALID_SUBSCRIPTION_ID
    public static final int INVALID_SLOT_ID = -1000; //INVALID_SIM_SLOT_INDEX

    public static String read_SharedPreference(String sp_key, Context context) {
        int sp_mode = Context.MODE_WORLD_READABLE
                | Context.MODE_WORLD_WRITEABLE | Context.MODE_MULTI_PROCESS;
        return context.getSharedPreferences("dsim_test_preference", sp_mode)
                .getString(sp_key, "");
    }

    public static void write_SharedPreference(String mode, String value,
            Context context) {
        Log.i(LOG_TAG, "write_SharedPreference:" + mode + ", value:" + value);
        int sp_mode = Context.MODE_WORLD_READABLE
                | Context.MODE_WORLD_WRITEABLE | Context.MODE_MULTI_PROCESS;
        context.getSharedPreferences("dsim_test_preference", sp_mode).edit()
                .putString(mode, value).commit();
    }

    private static int getMaxSlotCount() {
        return TelephonyManager.getDefault().getPhoneCount();
    }

    public static long getSubIdBySlotId(int slotId) {
        long[] subIdForL = null;
        int[] subIdForLMR1 = null;
        long actualSubId = 99999;

        try {
            if(android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
                subIdForL = LGSubscriptionManager.getSubIdForL(slotId);

                actualSubId = subIdForL[0];
            } else {
                subIdForLMR1 = LGSubscriptionManager.getSubIdForLMR1(slotId);

                actualSubId = subIdForLMR1[0];
            }

            Log.i(LOG_TAG, "getSubIdBySlotId() slotId " + slotId + ", subId " + actualSubId);
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "NullPointerException slot " + slotId);
        }

        return actualSubId;
    }

    public static int getSlotIdBySubId(long subId) {
        int slotId;
        try {
            slotId = LGSubscriptionManager.getSlotId(subId);
            Log.i(LOG_TAG, "getSlotIdBySubId() subId " + subId + ", slotId " + slotId);
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "NullPointerException sub " + subId);
            slotId = INVALID_SLOT_ID;
        }
        return slotId;
    }

    public static String get_SIM_phone_number(Context context) {
        Log.i(LOG_TAG, "get_SIM_phone_number");

        String sTabName = read_SharedPreference("tab", context);
        if (sTabName.equals("sim1")) {
            return LGSubscriptionManager.getLine1NumberForSubscriber(
                    getSubIdBySlotId(SIM_SLOT_1_SEL));
        } else if (sTabName.equals("sim2")) {
            return LGSubscriptionManager.getLine1NumberForSubscriber(
                    getSubIdBySlotId(SIM_SLOT_2_SEL));
        } else if (sTabName.equals("sim3")) {
            return LGSubscriptionManager.getLine1NumberForSubscriber(
                    getSubIdBySlotId(SIM_SLOT_3_SEL));
        } else {
            return "NULL";
        }
    }

    public static String get_IMSI_Current_Tab(Context context) {
        Log.i(LOG_TAG, "get_IMSI_Current_Tab");

        String sTabName = read_SharedPreference("tab", context);
        Log.i(LOG_TAG, "get_IMSI_Current_Tab, sTabName: ");
        String sIMSI = null;

        if (sTabName.equals("sim1")) {
            sIMSI = LGSubscriptionManager.getSubscriberId(
                    getSubIdBySlotId(SIM_SLOT_1_SEL));
        } else if (sTabName.equals("sim2")) {
            sIMSI = LGSubscriptionManager.getSubscriberId(
                    getSubIdBySlotId(SIM_SLOT_2_SEL));
        } else if (sTabName.equals("sim3")) {
            sIMSI = LGSubscriptionManager.getSubscriberId(
                    getSubIdBySlotId(SIM_SLOT_3_SEL));
        }

        Log.i(LOG_TAG, "get_IMSI_Current_Tab, IMSI: ");

        return sIMSI;
    }

    public static String get_device_ID(Context context,
            TelephonyManager mTelephonyManager) {
        String sTabName = read_SharedPreference("tab", context);

        if (sTabName.equals("sim1")) {
            return TelephonyManager.getDefault().getDeviceId(SIM_SLOT_1_SEL);
        } else if (sTabName.equals("sim2")) {
            return TelephonyManager.getDefault().getDeviceId(SIM_SLOT_2_SEL);
        } else if (sTabName.equals("sim3")) {
            return TelephonyManager.getDefault().getDeviceId(SIM_SLOT_3_SEL);
        } else {
            return PhoneFactory.getDefaultPhone().getDeviceId();
        }
    }

    public static String get_operator_numeric(Context context) {
        Log.i(LOG_TAG, "get_operator_numeric");

        String sTabName = read_SharedPreference("tab", context);
        String sGetValue = "";
        if (sTabName.equals("sim1")) {
            sGetValue = LGSubscriptionManager.getNetworkOperator(
                    getSubIdBySlotId(SIM_SLOT_1_SEL));
        } else if (sTabName.equals("sim2")) {
            sGetValue = LGSubscriptionManager.getNetworkOperator(
                    getSubIdBySlotId(SIM_SLOT_2_SEL));
        } else if (sTabName.equals("sim3")) {
            sGetValue = LGSubscriptionManager.getNetworkOperator(
                    getSubIdBySlotId(SIM_SLOT_3_SEL));
        }
        return sGetValue;
    }

    public static String get_roamming_state(Context context,
            TelephonyManager mTelephonyManager, String sRoamming,
            String sNotRoamming) { // Roaming
        Log.i(LOG_TAG, "get_roamming_state");

        String sTabName = read_SharedPreference("tab", context);
        boolean bRoaming = false;

        if (sTabName.equals("sim1")) {
            bRoaming = LGSubscriptionManager.isNetworkRoaming(
                    getSubIdBySlotId(SIM_SLOT_1_SEL));
        } else if (sTabName.equals("sim2")) {
            bRoaming = LGSubscriptionManager.isNetworkRoaming(
                    getSubIdBySlotId(SIM_SLOT_2_SEL));
        } else if (sTabName.equals("sim3")) {
            bRoaming = LGSubscriptionManager.isNetworkRoaming(
                    getSubIdBySlotId(SIM_SLOT_3_SEL));
        } else {
            return "common_pushed";
        }

        if (bRoaming) {
            return sRoamming;
        } else {
            return sNotRoamming;
        }
    }

    public static int get_mobileNetwork_state(Context context,
            TelephonyManager mTelephonyManager) {
        int simDataState = TelephonyManager.DATA_DISCONNECTED;
        int currentDds = LGSubscriptionManager.getDefaultDataPhoneIdBySubscriptionManager(context);

        if (read_SharedPreference("tab", context).equals("sim1")
                && currentDds == 0) {
            simDataState = mTelephonyManager.getDataState();
            return simDataState;
        } else if (read_SharedPreference("tab", context).equals("sim2")
                && currentDds == 1) {
            simDataState = mTelephonyManager.getDataState();
            return simDataState;
        } else if (read_SharedPreference("tab", context).equals("sim3")
                && currentDds == 2) {
            simDataState = mTelephonyManager.getDataState();
            return simDataState;
        } else {
            return simDataState;
        }
    }

    public static String makeTabName(int slotID) {
        String sTabName = "common";
        if (slotID == 0) {
            sTabName = "sim1";
        } else if (slotID == 1) {
            sTabName = "sim2";
        } else if (slotID == 2) {
            sTabName = "sim3";
        }
        return sTabName;
    }

    public static int makeSlotIDByTabName(String tabName) {
        int sSlotID = 0;
        if ("sim1".equals(tabName)) {
            sSlotID = 0;
        } else if ("sim2".equals(tabName)) {
            sSlotID = 1;
        } else if ("common".equals(tabName)) {
            sSlotID = 2;
        }
        return sSlotID;
    }

    public static String get_operator_name(Context context, String unknown) {
        Log.i(LOG_TAG, "get_operator_name");
        String sTabName = read_SharedPreference("tab", context);
        return opertator_name_common(sTabName, unknown);
    }

    public static String get_operator_name_ctc(int slotID, String unknown) {
        Log.i(LOG_TAG, "get_operator_name_ctc");
        String sTabName = makeTabName(slotID);
        return opertator_name_common(sTabName, unknown);
    }

    public static String opertator_name_common(String sTabName, String unknown) {
        String sGetValue = "";
        if (sTabName.equals("sim1")) {
            sGetValue = LGSubscriptionManager.getNetworkOperatorName(
                    getSubIdBySlotId(SIM_SLOT_1_SEL));
        } else if (sTabName.equals("sim2")) {
            sGetValue = LGSubscriptionManager.getNetworkOperatorName(
                    getSubIdBySlotId(SIM_SLOT_2_SEL));
        } else if (sTabName.equals("sim3")) {
            sGetValue = LGSubscriptionManager.getNetworkOperatorName(
                    getSubIdBySlotId(SIM_SLOT_3_SEL));
        } else {
            return "common_pushed";
        }

        if (sGetValue.equalsIgnoreCase("null")
                || sGetValue.equalsIgnoreCase("unknown")
                || sGetValue.equalsIgnoreCase("")) {
            return unknown;
        } else {
            return sGetValue;
        }
    }

    public static String get_network_type(Context context, String unknown) {
        Log.i(LOG_TAG, "get_network_type");
        String sTabName = read_SharedPreference("tab", context);
        return network_type_common(sTabName, unknown);
    }

    public static String get_network_type_ctc(int slotID, String unknown) {
        Log.i(LOG_TAG, "get_network_type_ctc");
        String sTabName = makeTabName(slotID);
        return network_type_common(sTabName, unknown);
    }

    public static String network_type_common(String sTabName, String unknown) {
        String sGetValue = "";
        int networkType = 0;
        if (sTabName.equals("sim1")) {
            networkType = LGSubscriptionManager.getNetworkType(
                    getSubIdBySlotId(SIM_SLOT_1_SEL));
        } else if (sTabName.equals("sim2")) {
            networkType = LGSubscriptionManager.getNetworkType(
                    getSubIdBySlotId(SIM_SLOT_2_SEL));
        } else if (sTabName.equals("sim3")) {
            networkType = LGSubscriptionManager.getNetworkType(
                    getSubIdBySlotId(SIM_SLOT_3_SEL));
        } else {
            return "common_pushed";
        }
        sGetValue = TelephonyManager.getDefault().getNetworkTypeName(
                networkType);
        if (sGetValue.equalsIgnoreCase("null")
                || sGetValue.equalsIgnoreCase("unknown")
                || sGetValue.equalsIgnoreCase("")) {
            return unknown;
        } else {
            return sGetValue;
        }
    }

    public static String network_type_name(int sTypeNum, String unknown) {
        String sGetValue = "";

        sGetValue = TelephonyManager.getDefault().getNetworkTypeName(
                sTypeNum);
        if (sGetValue.equalsIgnoreCase("null")
                || sGetValue.equalsIgnoreCase("unknown")
                || sGetValue.equalsIgnoreCase("")) {
            return unknown;
        } else {
            return sGetValue;
        }
    }

    public static int get_signal_strength_Dbm(Context context,
            PhoneStateIntentReceiver mPhoneStateReceiver) { // Signal Strength
        Log.i(LOG_TAG, "get_signal_strength_Dbm");
        String sTabName = read_SharedPreference("tab", context);
        return signal_strength_Dbm_common(sTabName);
    }

    public static int get_signal_strength_Dbm_ctc(int slotID,
            PhoneStateIntentReceiver mPhoneStateReceiver) {
        Log.i(LOG_TAG, "get_signal_strength_Dbm_ctc");
        String sTabName = makeTabName(slotID);
        return signal_strength_Dbm_common(sTabName);
    }

    public static int signal_strength_Dbm_common(String sTabName) {
        int signalDbm = 0;
        try {
            if (sTabName.equals("sim1")) {
                signalDbm = PhoneFactory.getPhone(SIM_SLOT_1_SEL)
                        .getSignalStrength().getDbm();
            } else if (sTabName.equals("sim2")) {
                signalDbm = PhoneFactory.getPhone(SIM_SLOT_2_SEL)
                        .getSignalStrength().getDbm();
            } else if (sTabName.equals("sim3")) {
                signalDbm = PhoneFactory.getPhone(SIM_SLOT_3_SEL)
                        .getSignalStrength().getDbm();
            }
        } catch (NullPointerException e) {
            Log.i(LOG_TAG, " getSignalStrength NullPointerException " + e.getMessage());
        }
        return signalDbm;
    }

    public static int get_signal_strength_Asu(Context context,
            PhoneStateIntentReceiver mPhoneStateReceiver) { // Signal Strength
        Log.i(LOG_TAG, "get_signal_strength_Asu");
        String sTabName = read_SharedPreference("tab", context);
        return signal_strength_Asu_common(sTabName);
    }

    public static int get_signal_strength_Asu_ctc(int slotID,
            PhoneStateIntentReceiver mPhoneStateReceiver) { // Signal Strength
        Log.i(LOG_TAG, "get_signal_strength_Asu_ctc");
        String sTabName = makeTabName(slotID);
        return signal_strength_Asu_common(sTabName);
    }

    public static int signal_strength_Asu_common(String sTabName) {
        int signalAsu = 0;
        try {
            if (sTabName.equals("sim1")) {
                signalAsu = PhoneFactory.getPhone(SIM_SLOT_1_SEL)
                        .getSignalStrength().getAsuLevel();
            } else if (sTabName.equals("sim2")) {
                signalAsu = PhoneFactory.getPhone(SIM_SLOT_1_SEL)
                        .getSignalStrength().getAsuLevel();
            } else if (sTabName.equals("sim3")) {
                signalAsu = PhoneFactory.getPhone(SIM_SLOT_3_SEL)
                        .getSignalStrength().getAsuLevel();
            }
        } catch (NullPointerException e) {
            Log.i(LOG_TAG, " getSignalStrength NullPointerException " + e.getMessage());
        }
        return signalAsu;
    }
}
