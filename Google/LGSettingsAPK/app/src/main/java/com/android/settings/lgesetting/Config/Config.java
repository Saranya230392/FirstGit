package com.android.settings.lgesetting.Config;

import java.util.HashMap;
import java.util.Map;

import com.android.internal.telephony.TelephonyProperties;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Build;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.telephony.SubscriptionManager;

import com.android.settings.SLog;
import com.android.settings.Utils;
import com.android.settings.utils.LGSubscriptionManager;

public class Config {
    public static final String TAG = "Config";
    private static final String NOT_SUPPORT = "NOT_SUPPORT";

    private static boolean initCalled = false;

    /*
     * If your model does not support any flexible method, please modify below
     * values suitable for your model.
     */
    private static final Operator DEFAULT_OPERATOR = Operator.OPEN;
    private static final Country DEFAULT_COUNTRY = Country.EU;

    private static String mOperator;
    private static String mCountry;

    // **************************************************************
    public final static String OPEN = "OPEN";
    public final static String DCM = "DCM";
    public final static String MPCS = "MPCS";
    public final static String SPRINT = "SPR";
    public final static String SPR = "SPR";
    public final static String ATT = "ATT";
    public final static String VZW = "VZW";
    public final static String SKT = "SKT";
    public final static String KT = "KT";
    public final static String LGU = "LGU";
    public final static String TCL = "TCL";
    public final static String UNF = "UNF";
    public final static String TLS = "TLS";
    public final static String BELL = "BELL";
    public final static String USC = "USC";
    public final static String TRF = "TRF";
    public final static String KDDI = "KDDI";
    public final static String BM = "BM";
    public final static String CRK = "CRK";
    public final static String CCM = "CCM";
    public final static String ACG = "ACG";
    public final static String TMO = "TMO";
    public final static String SHB = "SHB";
    public final static String VIV = "VIV";
    public final static String VDF = "VDF";
    public final static String STL = "STL";
    public final static String MON = "MON"; // Chandrakanth Added for STL & MON
                                            // chandrakanth.kanala@lge.com
                                            // 20121227
    public final static String CHT = "CHT";
    // 3LM_MDM_DCM jihun.im@lge.com
    public final static boolean THREELM_MDM = SystemProperties.getBoolean(
            "ro.3lm.build.lg", false);
    public final static String ORG = "ORG";
    public final static String RGS = "RGS";
    public final static String CMCC = "CMCC";
    public final static String CTC = "CTC";
    public final static String TEL = "TEL";
    public final static String CMO = "CMO";
    public final static String CTO = "CTO";

    public enum Operator {
        ATT, VZW, TMO, SKT, OPEN, KT, LGT, DCM, TCL, UNF, KDDI, NOT_REGISTERED;

        private static final Map<String, Operator> stringToEnum = new HashMap<String, Operator>();

        static {
            for (Operator o : values())
                stringToEnum.put(o.toString(), o);
        }

        public static Operator fromString(String operator) {
            return stringToEnum.get(operator);
        }
    }

    public enum Country {
        ES, GB, FR, HK, TW, NZ, AT, CH, DE, GR, HU, NO, IT, BE, PT, PL, RO, SK, CIS, BAL, IL, EU, // Israel
        US, KR, COM, JP, MX, CN, ESA, AME, // LGEYT qingtai.wang@lge.com
                                           // 20121123 add CN country
        NOT_REGISTERED;

        private static final Map<String, Country> stringToEnum = new HashMap<String, Country>();

        static {
            for (Country c : values())
                stringToEnum.put(c.toString(), c);
        }

        public static Country fromString(String operator) {
            return stringToEnum.get(operator);
        }
    }

    public static void init() {
        if (initCalled == true) {
            return;
        }

        initCalled = true;

        if (checkNativeCA()) {
            return;
        } else {
            setCountryAndOperator(DEFAULT_COUNTRY.toString(),
                    DEFAULT_OPERATOR.toString(), Build.MODEL);
        }
    }

    private static boolean setCountryAndOperator(String country,
            String operator, String model) {

        if ("not definded".equals(operator)) {
            return false;
        }

        if (NOT_SUPPORT.equals(operator)) {
            return false;
        }

        if (!TextUtils.isEmpty(operator)) {
            mCountry = country;
            mOperator = operator;

            if (mOperator == null) {
                mOperator = OPEN;
            }
            return true;
        }
        return false;
    }

    private static boolean checkNativeCA() {

        String operator;
        String country;
        String model;

        operator = SystemProperties.get("ro.build.target_operator");
        country = SystemProperties.get("ro.build.target_country");
        model = SystemProperties.get("ro.product.model");

        // yonguk.kim 20131231 AIO is prepaid operator of ATT.
        // so, the feature should be same as ATT.
        if ("AIO".equalsIgnoreCase(operator)) {
            operator = "ATT";
        }
        return setCountryAndOperator(country, operator, model);
    }

    public static boolean isVZWAdminDisabled(Context ctx) {
        if (("VZW".equals(Config.getOperator()) || "LRA".equals(Config
                .getOperator())) == false) {
            return false;
        }

        String selection = null;
        String operator = null;

        if ("VZW".equals(Config.getOperator())) {
            operator = SystemProperties
                    .get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
        } else if ("LRA".equals(Config.getOperator())) {
            operator = SystemProperties.get("gsm.apn.sim.operator.numeric");
        }

        if (operator == null) {
            return false;
        }

        if (false == operator.equals("311480"))
            return false;

        selection = "numeric = '" + operator + "'" + " and type = 'admin'";
        selection += " and carrier_enabled = 1";

        Cursor cursor = ctx.getContentResolver().query(
                Telephony.Carriers.CONTENT_URI, null, selection, null, null);

        if (null != cursor) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return false;
            } else {
                cursor.close();
                return true;
            }
        }

        return false;
    }

    // kiseok.son 20121025: Add the airplane mode menu in setting screen for
    // VZW.
    public static boolean supportAirplaneListMenu() {
        // yonguk.kim 20140204 For testing remote fragment
        if (SystemProperties.getBoolean("settings.remote.test", false) == true) {
            return false;
        }

        if ("US".equals(getCountry())) {
            return true;
        }

        return false;
    }

    // 2013-01-31 RaeYoung-Jeong (ry.jeong@lge.com) :
    // [DCM] Not support model for GPS notification menu in Location access
    public static boolean isNotSupportGPSNotificationModel() {
        String product = SystemProperties.get("ro.product.name");

        // Model Info : L-01E(geehdc_dcm_jp), L-02E(l1_DCM_JP), L-05D(l_DCM_JP),
        // L-06D(batman_dcm)
        if ((true == product.equals("geehdc_dcm_jp"))
                || (true == product.equals("l1_DCM_JP"))
                || (true == product.equals("l_DCM_JP"))
                || (true == product.equals("batman_dcm"))) {
            return true;
        }

        return false;
    }

    // 2013-04-08 RaeYoung-Jeong (ry.jeong@lge.com) :
    // [VZW] Remove E911,Verizon location services. Apply similar Native UI in
    // Location access.
    public static boolean isSupportVZWLocationAccessScenario4_0() {

        // Model Info : FXQ(VS890), A1(G2), and all verizon operator
        if (true == "VZW".equals(Config.getOperator())) {
            return true;
        }

        return false;
    }

    public static boolean isDataRoaming() {
        return isDataRoaming(LGSubscriptionManager.getDefaultSubId());
    }

    public static boolean isDataRoaming(long subId) {
        String country = SystemProperties.get("ro.build.target_country");
        boolean isTMRoaming = LGSubscriptionManager.isNetworkRoaming(subId);
        boolean isPropertyRoaming = "true".equals(SystemProperties
                .get("persist.radio.isroaming"));
        SLog.d("isNetworkRoaming isTMRoaming = " + isTMRoaming
                + "isPropertyRoaming = " + isPropertyRoaming);
        if ("KR".equals(country)) {
            return (isTMRoaming || isPropertyRoaming);
        }
        return isTMRoaming;
    }

    public static String getOperator2() {
        return SystemProperties.get("ro.build.target_operator", "NOT_DEFINED");
    }

    public static String getCountry2() {
        return SystemProperties.get("ro.build.target_country", "NOT_DEFINED");
    }

    public static Context sContext;
    public static String sOperator;
    public static String sCountry;

    public static String getOperator() {
        if (checkIfTestMode() == false) {
            init();

            if (SystemProperties.get("sys.settings.operator") != null
                    && SystemProperties.get("sys.settings.operator").length() > 1) {
                return SystemProperties.get("sys.settings.operator");
            } else {
                return mOperator;
            }
        }

        if (sSharedPreferencesForTest != null) {
            if (sOperator == null || "none".equals(sOperator)) {
                sOperator = sSharedPreferencesForTest.getString(
                        "settings.test.operator", "none");
            }

            if ("none".equals(sOperator) == false) {
                return sOperator;
            }
        }
        Log.d("kimyow",
                "Test mode is failed. What happened? : getOperator() = "
                        + mOperator);
        return mOperator;
    }

    public static String getCountry() {
        if (checkIfTestMode() == false) {
            init();
            if (SystemProperties.get("sys.settings.country") != null
                    && SystemProperties.get("sys.settings.country").length() > 1) {
                return SystemProperties.get("sys.settings.country");
            }
            return mCountry;
        }

        if (sSharedPreferencesForTest != null) {
            if (sCountry == null || "none".equals(sCountry)) {
                sCountry = sSharedPreferencesForTest.getString(
                        "settings.test.country", "none");
            }
            if ("none".equals(sCountry) == false) {
                return sCountry;
            }
        }
        Log.d("kimyow", "Test mode is failed. What happened? : getCountry() = "
                + mCountry);
        return mCountry;
    }

    private static String sTEST_APP_PACKAGE_NAME = "com.example.mytest001";
    private static String sKEY_NAME_TEST_ENABLE = "settings_test_enable";
    private static SharedPreferences sSharedPreferencesForTest;
    private static int sIsTestMode = 0; /*
                                         * 0: uncheck, 1: testmode, 2: general
                                         * mode
                                         */

    public static boolean getFWConfigBool(Context context, int resId, String key) {
        if (checkIfTestMode() == false) {
            return context.getResources().getBoolean(resId);
        }

        if (sSharedPreferencesForTest != null) {
            boolean result = sSharedPreferencesForTest.getBoolean(key, false);
            Log.d("kimyow", key + ": " + result);
            return result;
        }
        Log.d("kimyow", "Test mode is failed. What happened? :" + key);
        return context.getResources().getBoolean(resId);
    }

    public static boolean getFWConfigBool(Context context, boolean value,
            String key) {
        if (checkIfTestMode() == false) {
            return value;
        }

        if (sSharedPreferencesForTest != null) {
            boolean result = sSharedPreferencesForTest.getBoolean(key, false);
            Log.d("kimyow", key + ": " + result);
            return result;
        }
        Log.d("kimyow", "Test mode is failed. What happened? :" + key);
        return value;
    }

    public static int getFWConfigInteger(Context context, int resId, String key) {
        if (checkIfTestMode() == false) {
            return context.getResources().getInteger(resId);
        }

        if (sSharedPreferencesForTest != null) {
            int result = Integer.parseInt(sSharedPreferencesForTest.getString(
                    key, "0"));
            Log.d("kimyow", key + ": " + result);
            return result;
        }
        Log.d("kimyow", "Test mode is failed. What happened? :" + key);
        return context.getResources().getInteger(resId);
    }

    private static boolean checkIfTestMode() {
        if (sContext == null) {
            return false;
        }

        SharedPreferences temp = PreferenceManager
                .getDefaultSharedPreferences(sContext);
        if ("false".equals(temp.getString(sKEY_NAME_TEST_ENABLE, "false"))) {
            return false;
        }

        if (sIsTestMode == 0) {
            getSharedPreferencesForTest();

            if (sSharedPreferencesForTest != null) {
                sIsTestMode = sSharedPreferencesForTest.getBoolean(
                        sKEY_NAME_TEST_ENABLE, false) ? 1 : 2;
            } else {
                sIsTestMode = 2;
                temp.edit().putString(sKEY_NAME_TEST_ENABLE, "false").commit();
            }
        }

        return sIsTestMode == 1;
    }

    public static SharedPreferences getSharedPreferencesForTest() {

        if (sSharedPreferencesForTest != null) {
            return sSharedPreferencesForTest;
        }

        int flags = (Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        Context testAppContext = null;
        try {
            testAppContext = sContext.createPackageContext(
                    sTEST_APP_PACKAGE_NAME, flags);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (testAppContext != null) {
            sSharedPreferencesForTest = testAppContext.getSharedPreferences(
                    sTEST_APP_PACKAGE_NAME + "_preferences",
                    Context.MODE_MULTI_PROCESS);
            return sSharedPreferencesForTest;
        }
        return null;
    }

}
