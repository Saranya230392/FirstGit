package com.android.settings.utils;

import android.util.Log;
import android.content.Context;
import android.telephony.SubscriptionManager;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import android.telephony.TelephonyManager;

public class LGSubscriptionManager {
    private static final String LOG_TAG = "LGSubscriptionManager";

    public LGSubscriptionManager() {

    }

    public static long[] getSubId(int slotId) {
        long[] result = null;        
        long[] subIdForL = null;
        int[] subIdForLMR1 = null;

        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            subIdForL = getSubIdForL(slotId);
            result = new long[subIdForL.length];
            for (int i = 0; i < subIdForL.length; i++) {
                result[i] = subIdForL[i];
            }
        } else {
            subIdForLMR1 = getSubIdForLMR1(slotId);
            result = new long[subIdForLMR1.length];           
            for (int i = 0; i < subIdForLMR1.length; i++) {
                result[i] = (long) subIdForLMR1[i];
            }
        }

        return result;
    }

    public static long[] getSubIdForL(int slotId) {
        long[] result = null;

        try {
            Class<?> klass = Class.forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod("getSubId", new Class[] { Integer.TYPE });
            Object returnResult = method.invoke(null, new Object[] { slotId });
            result = (long[])method.getReturnType().cast(returnResult);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        return result;
    }

    public static int[] getSubIdForLMR1(int slotId) {
        int[] result = null;

        try {
            Class<?> klass = Class.forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod("getSubId", new Class[] { Integer.TYPE });
            Object returnResult = method.invoke(null, new Object[] { slotId });
            result = (int[])method.getReturnType().cast(returnResult);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        return result;

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



    public static long getDefaultSubId() {
        long result = -1;

        try {
            Class<?> klass = Class.forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod("getDefaultSubId");
            Object returnResult = method.invoke(klass);
            Long retval = (Long) returnResult;
            result = retval.longValue();
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        return result;
    }


    public static long getDefaultSubIdForL() {
        long result = -1;

        try {
            Class<?> klass = Class.forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod("getDefaultSubId");
            Object returnResult = method.invoke(klass);
            Long retval = (Long) returnResult;
            result = retval.longValue();
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        return result;
    }

    public static int getDefaultSubIdForLMR1() {
        int result = -1;

        try {
            Class<?> klass = Class.forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod("getDefaultSubId");
            Object returnResult = method.invoke(klass);
            Integer retval = (Integer) returnResult;
            result = retval.intValue();
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        return result;
    }

    public static long getDefaultDataSubIdForL() {
        long result = -1;

        try {
            Class<?> klass = Class.forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod("getDefaultDataSubId");
            Object returnResult = method.invoke(klass);
            Long retval = (Long) returnResult;
            result = retval.longValue();
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        return result;
    }

    public static int getDefaultDataSubIdForLMR1() {
        int result = -1;

        try {
            Class<?> klass = Class.forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod("getDefaultDataSubId");
            Object returnResult = method.invoke(klass);
            Integer retval = (Integer) returnResult;
            result = retval.intValue();
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        return result;
    }

/*******************************************************************************
 * static Method (changed)
 *******************************************************************************
 * SubscriptionManager.getDefaultDataPhoneId() -> SubscriptionManager.from(getApplicationContext())
*/
    public static int getDefaultDataPhoneIdBySubscriptionManager(Context context) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return getDefaultDataPhoneIdForL();
        } else {
            return getDefaultDataPhoneIdAfterLMR1(context);
        }
    }

    private static int getDefaultDataPhoneIdAfterLMR1(Context context) {
        int result = -1;
        try {
            Class<?> klass = Class.forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod("from");
            Object returnResult = method.invoke(klass, context);
            SubscriptionManager subscriptionManager = (SubscriptionManager) returnResult;
            result = subscriptionManager.getDefaultDataPhoneId();
        } catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        } 
        return result;
    }

    private static int getDefaultDataPhoneIdForL() {
        int result = -1;
        try {
            Class<?> klass = Class.forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod("getDefaultDataPhoneId", new Class[] {});
                   Object returnResult = method.invoke(klass);
                   Integer retval = (Integer) returnResult;
                   result = retval.intValue();
        } catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        } 

        Log.d(LOG_TAG, "getDefaultDataPhoneIdForL, result : " + result);
        return result;
    }

/*******************************************************************************
 * static method parameterType (changed)
 * return type void
 * android.telephony.SubscriptionManager
 *******************************************************************************
 * SubscriptionManager.activateSubId(long) -> SubscriptionManager.activateSubId(int)
 * SubscriptionManager.deactivateSubId(long) -> SubscriptionManager.deactivateSubId(int)
 * SubscriptionManager.setDefaultDataSubId(long) -> SubscriptionManager.setDefaultDataSubId(int)
 * SubscriptionManager.setDefaultSubId(long) -> SubscriptionManager.setDefaultSubId(int)
*/
    public static void changedParameterTypeLongToIntReturnVoid(long slotId, String methodName) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            changedParameterTypeLongToIntForL(slotId, methodName);
        } else {
            changedParameterTypeLongToIntAfterLMR1((int)slotId, methodName);
        }
    }

    private static void changedParameterTypeLongToIntAfterLMR1(int slotId, String methodName) {
        try {
            Class<?> klass = Class
                    .forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod(methodName,
                    new Class[] { Integer.TYPE });
            Object returnResult = method.invoke(klass, slotId);
        } catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        } 
    }

    private static void changedParameterTypeLongToIntForL(long slotId, String methodName) {
        try {
            Class<?> klass = Class
                    .forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod(methodName,
                    new Class[] { Long.TYPE });
            Object returnResult = method.invoke(klass, slotId);
        } catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    public static boolean isNetworkRoaming(long subId) {
        boolean result = false;

        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            result = isNetworkRoamingL(subId);
        } else {
            result = isNetworkRoamingLMR1((int) subId);
        }
        return result;
    }

    public static boolean isNetworkRoamingL(long subId) {
        boolean result = false;

        TelephonyManager tm = TelephonyManager.getDefault();

        try {
            Method method = tm.getClass().getMethod("isNetworkRoaming", new Class[] { Long.TYPE });
            Object returnResult = method.invoke(tm.getClass());
            result = (boolean) returnResult;
        } /*catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, e.getMessage());
        }*/catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        Log.d(LOG_TAG, "isNetworkRoamingL " + result);

        return result;
    }

    public static boolean isNetworkRoamingLMR1(int subId) {
        boolean result = false;

        TelephonyManager tm = TelephonyManager.getDefault();

        try {
            Method method = tm.getClass().getMethod("isNetworkRoaming", new Class[] { Integer.TYPE });
            Object returnResult = method.invoke(tm.getClass());
            result = (boolean) returnResult;
        } /*catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, e.getMessage());
        } */catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        Log.d(LOG_TAG, "isNetworkRoamingLMR1 " + result);

        return result;
    }

    /*******************************************************************************
     * static method parameterType (changed)
     * *********************************************
     * SubscriptionManager.getSlotId(long) -> SubscriptionManager.getSlotId(int)
     */
    public static int getSlotId(long slotId) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return getSlotIdForL(slotId);
        } else {
            return getSlotIdAfterLMR1((int)slotId);
        }
    }

    private static int getSlotIdAfterLMR1(int slotId) {
        int result = -1;
        try {
            Class<?> klass = Class
                    .forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod("getSlotId",
                    new Class[] { Integer.TYPE });
            Object returnResult = method.invoke(klass, slotId);
            Integer retval = (Integer) returnResult;
            result = retval.intValue();
        } catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return result;
    }

    private static int getSlotIdForL(long slotId) {
        int result = -1;
        try {
            Class<?> klass = Class.forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod("getSlotId",
                     new Class[] { Long.TYPE });
            Object returnResult = method.invoke(klass, slotId);
            Integer retval = (Integer) returnResult;
            result = retval.intValue();
        } catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return result;
    }

    /*******************************************************************************
     * static method parameterType (changed)
     * *********************************************
     * SubscriptionManager.getPhoneId(long) -> SubscriptionManager.getPhoneId(int)
     */
    public static int getPhoneId(long slotId) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return getPhoneIdForL(slotId);
        } else {
            return getPhoneIdAfterLMR1((int)slotId);
        }
    }

    private static int getPhoneIdAfterLMR1(int slotId) {
        int result = -1;
        try {
            Class<?> klass = Class
                    .forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod("getPhoneId",
                    new Class[] { Integer.TYPE });
            Object returnResult = method.invoke(klass, slotId);
            Integer retval = (Integer) returnResult;
            result = retval.intValue();
        } catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return result;
    }

    private static int getPhoneIdForL(long slotId) {
        int result = -1;
        try {
            Class<?> klass = Class.forName("android.telephony.SubscriptionManager");
            Method method = klass.getMethod("getPhoneId",
                     new Class[] { Long.TYPE });
            Object returnResult = method.invoke(klass, slotId);
            Integer retval = (Integer) returnResult;
            result = retval.intValue();
        } catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return result;
    }

    /*******************************************************************************
     * static method parameterType (changed)
     * *********************************************
     * TelephonyManager.getTelephonyProperty(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, subId, null) : (int, long, null)
     *     ->TelephonyManager.getTelephonyProperty(subId, TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, null) : (int, int, null)
     */
    public static String getTelephonyProperty(long slotId, String field) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return getTelephonyPropertyForL(slotId, field);
        } else {
            return getTelephonyPropertyAfterLMR1((int) slotId, field);
        }
    }

    private static String getTelephonyPropertyAfterLMR1(int slotId, String field) {
        String result = "";
        try {
            Class<?> klass = Class
                    .forName("android.telephony.TelephonyManager");
            Method method = klass.getMethod("getDefault");
            Object returnResult = method.invoke(klass);

            Class<?> klass2 = returnResult.getClass();
            Method method2 = klass2.getMethod("getTelephonyProperty",
                    new Class[] { Integer.TYPE, String.class, String.class });
            Object returnResult2 = method2.invoke(klass2, slotId, field, "");

            result = returnResult2 + "";
            Log.d(LOG_TAG, result + " || " + returnResult2);
        } catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        } 
        return result;
     }

    private static String getTelephonyPropertyForL(long slotId, String field) {
        String result = "";
        try {
            Class<?> klass = Class
                    .forName("android.telephony.TelephonyManager");
            Method method = klass.getMethod("getDefault");
            Object returnResult = method.invoke(klass);

            Class<?> klass2 = returnResult.getClass();
            Method method2 = klass2.getMethod("getTelephonyProperty",
                    new Class[] { String.class, Long.TYPE, String.class });
            Object returnResult2 = method2.invoke(klass2, field, slotId, "");

            result = returnResult2 + "";
            Log.d(LOG_TAG, result + " || " + returnResult2);
        } catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        } 
        return result;
    }

    /*******************************************************************************
     * static method parameterType (changed)
     * *********************************************
     * TelephonyManager.getDefault().getLine1NumberForSubscriber(long) ->TelephonyManager.getDefault().getLine1NumberForSubscriber(int)
     */
    public static String getLine1NumberForSubscriber(long slotId) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return getLine1NumberForSubscribergForL(slotId);
        } else {
            return getLine1NumberForSubscriberAfterLMR1((int) slotId);
        }
    }

    private static String getLine1NumberForSubscriberAfterLMR1(int slotId) {
        String result = "";
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getLine1NumberForSubscriber", new Class[] { Integer.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            result = returnResult + "";
            Log.d(LOG_TAG, "getLine1NumberForSubscriber " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return result;
    }

    private static String getLine1NumberForSubscribergForL(long slotId) {
        String result = "";
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getLine1NumberForSubscriber", new Class[] { Long.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            result = returnResult + "";
            Log.d(LOG_TAG, "getLine1NumberForSubscriber " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return result;
    }

    public static int getNetworkType(long slotId) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return getNetworkTypeForL(slotId);
        } else {
            return getNetworkTypeAfterLMR1((int) slotId);
        }
    }

    private static int getNetworkTypeAfterLMR1(int slotId) {
        int result = 999;
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getNetworkType", new Class[] { Integer.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            Integer retval = (Integer) returnResult;
            result = retval.intValue();
            Log.d(LOG_TAG, "getNetworkType " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return result;
    }

    private static int getNetworkTypeForL(long slotId) {
        int result = 999;
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getNetworkType", new Class[] { Long.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            Integer retval = (Integer) returnResult;
            result = retval.intValue();
            Log.d(LOG_TAG, "getNetworkType " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return result;
    }

    /*******************************************************************************
     * static method parameterType (changed)
     * *********************************************
     * TelephonyManager.getDefault().getNetworkOperatorName(long) ->TelephonyManager.getDefault().getNetworkOperatorName(int)
     */
    public static String getNetworkOperatorName(long slotId) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return getNetworkOperatorNameForL(slotId);
        } else {
            return getNetworkOperatorNameAfterLMR1((int) slotId);
        }
    }

    private static String getNetworkOperatorNameAfterLMR1(int slotId) {
        String result = "";
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getNetworkOperatorName", new Class[] { Integer.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            result = returnResult + "";
            Log.d(LOG_TAG, "getNetworkOperatorName " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return result;
    }

    private static String getNetworkOperatorNameForL(long slotId) {
        String result = "";
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getNetworkOperatorName", new Class[] { Long.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            result = returnResult + "";
            Log.d(LOG_TAG, "getNetworkOperatorName " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return result;
    }

    /*******************************************************************************
     * static method parameterType (changed)
     * *********************************************
     * TelephonyManager.getDefault().getNetworkOperator(long) ->TelephonyManager.getDefault().getNetworkOperator(int)
     */
    public static String getNetworkOperator(long slotId) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return getNetworkOperatorForL(slotId);
        } else {
            return getNetworkOperatorAfterLMR1((int) slotId);
        }
    }

    private static String getNetworkOperatorAfterLMR1(int slotId) {
        String result = "";
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getNetworkOperator", new Class[] { Integer.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            result = returnResult + "";
            Log.d(LOG_TAG, "getNetworkOperator " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return result;
    }

    private static String getNetworkOperatorForL(long slotId) {
        String result = "";
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getNetworkOperator", new Class[] { Long.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            result = returnResult + "";
            Log.d(LOG_TAG, "getNetworkOperator " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return result;
    }

    /*******************************************************************************
     * static method parameterType (changed)
     * *********************************************
     * TelephonyManager.getDefault().getSubscriberId(long) ->TelephonyManager.getDefault().getSubscriberId(int)
     */
    public static String getSubscriberId(long slotId) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return getSubscriberIdForL(slotId);
        } else {
            return getSubscriberIdAfterLMR1((int) slotId);
        }
    }

    private static String getSubscriberIdAfterLMR1(int slotId) {
        String result = "";
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getSubscriberId", new Class[] { Integer.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            result = returnResult + "";
            Log.d(LOG_TAG, "getSubscriberId " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return result;
    }

    private static String getSubscriberIdForL(long slotId) {
        String result = "";
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getSubscriberId", new Class[] { Long.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            result = returnResult + "";
            Log.d(LOG_TAG, "getSubscriberId " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, "getSubscriberId " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, "getSubscriberId " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "getSubscriberId " + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, "getSubscriberId " + e.getMessage());
        }
        return result;
    }

    /*******************************************************************************
     * static method parameterType (changed)
     * *********************************************
     * TelephonyManager.getDefault().getIccOperatorNumeric(long) ->TelephonyManager.getDefault().getIccOperatorNumeric(int)
     */
    public static String getIccOperatorNumeric(long slotId) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return getIccOperatorNumericForL(slotId);
        } else {
            return getIccOperatorNumericAfterLMR1((int) slotId);
        }
    }

    private static String getIccOperatorNumericAfterLMR1(int slotId) {
        String result = "";
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getIccOperatorNumeric", new Class[] { Integer.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            result = returnResult + "";
            Log.d(LOG_TAG, "getIccOperatorNumeric " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, "getIccOperatorNumeric " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, "getIccOperatorNumeric " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "getIccOperatorNumeric " + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, "getIccOperatorNumeric " + e.getMessage());
        }
        return result;
    }

    private static String getIccOperatorNumericForL(long slotId) {
        String result = "";
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getIccOperatorNumeric", new Class[] { Long.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            result = returnResult + "";
            Log.d(LOG_TAG, "getIccOperatorNumeric " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, "getIccOperatorNumeric " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, "getIccOperatorNumeric " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "getIccOperatorNumeric " + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, "getIccOperatorNumeric " + e.getMessage());
        }
        return result;
    }

    /*******************************************************************************
     * static method parameterType (changed)
     * *********************************************
     * mTelephonyManager.getSimOperator(long) ->mTelephonyManager.getSimOperator(int)
     */
    public static String getSimOperator(long slotId) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return getSimOperatorForL(slotId);
        } else {
            return getSimOperatorAfterLMR1((int) slotId);
        }
    }

    private static String getSimOperatorAfterLMR1(int slotId) {
        String result = "";
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getSimOperator", new Class[] { Integer.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            result = returnResult + "";
            Log.d(LOG_TAG, "getSimOperator " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, "getSimOperator " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, "getSimOperator " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "getSimOperator " + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, "getSimOperator " + e.getMessage());
        }
        return result;
    }

    private static String getSimOperatorForL(long slotId) {
        String result = "";
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getSimOperator", new Class[] { Long.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            result = returnResult + "";
            Log.d(LOG_TAG, "getSimOperator " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, "getSimOperator " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, "getSimOperator " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "getSimOperator " + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, "getSimOperator " + e.getMessage());
        }
        return result;
    }

    /*******************************************************************************
     * static method parameterType (changed)
     * *********************************************
     * mTelephonyManager.getGroupIdLevel1(long) ->mTelephonyManager.getGroupIdLevel1(int)
     */
    public static String getGroupIdLevel1(long slotId) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return getSimOperatorForL(slotId);
        } else {
            return getSimOperatorAfterLMR1((int) slotId);
        }
    }

    private static String getGroupIdLevel1AfterLMR1(int slotId) {
        String result = "";
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getGroupIdLevel1", new Class[] { Integer.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            result = returnResult + "";
            Log.d(LOG_TAG, "getGroupIdLevel1 " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, "getGroupIdLevel1 " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, "getGroupIdLevel1 " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "getGroupIdLevel1 " + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, "getGroupIdLevel1 " + e.getMessage());
        }
        return result;
    }

    private static String getGroupIdLevel1ForL(long slotId) {
        String result = "";
        TelephonyManager tm = TelephonyManager.getDefault();
        try {
            Method method = tm.getClass().getMethod("getGroupIdLevel1", new Class[] { Long.TYPE });
            Object returnResult = method.invoke(tm.getClass(), slotId);
            result = returnResult + "";
            Log.d(LOG_TAG, "getGroupIdLevel1 " + result);
        } catch (NoSuchMethodException e) {
            Log.d(LOG_TAG, "getGroupIdLevel1 " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(LOG_TAG, "getGroupIdLevel1 " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "getGroupIdLevel1 " + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d(LOG_TAG, "getGroupIdLevel1 " + e.getMessage());
        }
        return result;
    }
}
