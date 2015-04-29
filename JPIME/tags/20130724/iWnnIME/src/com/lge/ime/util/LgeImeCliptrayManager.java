/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package com.lge.ime.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.view.inputmethod.EditorInfo;

/**
 * Reflection of CliptrayManager *
 * 
 * @author dongseok.lee
 * 
 */
public class LgeImeCliptrayManager {

    private static final String TAG = "LgeImeCliptrayManager";
    private static LgeImeCliptrayManager sInstance;
    private Object mObj;
    private Method mMethodshowCliptray;
    private Method mMethodfinishCliptrayService;
    private Method mMethodsetInputType;
    private Field mFieldclipDataType;
    private static final String CLASSPATH_SYSTEM_CLIPTRAY = "com.lge.systemservice.core.cliptraymanager.CliptrayManager";
    private static final String KEY_CLIPTRAY_SERVICE = "cliptray";

    private LgeImeCliptrayManager(Context context) {

        LgSystemContext contextLgSystem = LgSystemContext.getInstances(context);
        if (contextLgSystem == null) {
            // Debug.v(TAG, "Warning : LgSystemContext get failed.");
            LgSystemContext.release();
            return;
        }

        try {
            mObj = contextLgSystem.getLGSystemService(KEY_CLIPTRAY_SERVICE);
            if (mObj == null) {
                // Debug.v(TAG, "Warning : LgSystem Services get failed.");
                return;
            }
            ClassLoader cl = context.getClassLoader();
            // lookup class
            Class<?> c = cl.loadClass(CLASSPATH_SYSTEM_CLIPTRAY);
            // lookup method
            mMethodshowCliptray = c.getMethod("showCliptray");
            // lookup method
            mMethodfinishCliptrayService = c.getMethod("finishCliptrayService");
            Class[] paramTypes = { Integer.TYPE };
            mMethodsetInputType = c.getMethod("setInputType", paramTypes);
            mFieldclipDataType = EditorInfo.class.getField("clipDataType");
            // Debug.i(TAG, "info : '%s' is loadded successfully.",
            // CLASSPATH_SYSTEM_CLIPTRAY);
        } catch (Exception e) {
            // Debug.w(TAG, "failure : '%s' cannot be loaded.",
            // CLASSPATH_SYSTEM_CLIPTRAY);
            e.printStackTrace();
        }
    }

    public static LgeImeCliptrayManager getInstances(Context context) {
        if (sInstance == null) {
            sInstance = new LgeImeCliptrayManager(context);
        }
        return sInstance;
    }

    public static void release() {
        LgSystemContext.release();
        sInstance = null;
    }

    public void showClipTray(EditorInfo ei) {
        if (mMethodshowCliptray == null) {
            // Debug.v(TAG, "Warning : mMethodshowCliptray is null.");
            return;
        }
        if (mObj == null) {
            // Debug.v(TAG, "Warning : mClass is null.");
            return;
        }
        try {
            // call method
            if (mMethodsetInputType != null && ei != null) {
                if (mFieldclipDataType != null) {
                    int inputType = mFieldclipDataType.getInt(ei);
                    mMethodsetInputType.invoke(mObj, inputType);
                }
            }
            mMethodshowCliptray.invoke(mObj);
        } catch (Exception e) {
            // Debug.e(TAG, "failure : mMethodshowCliptray invocation failed.");
            e.printStackTrace();
        }
    }

    public void finishCliptrayService() {
        if (mMethodfinishCliptrayService == null) {
            // Debug.v(TAG, "Warning : mMethodfinishCliptrayService is null.");
            return;
        }
        if (mObj == null) {
            // Debug.v(TAG, "Warning : mClass is null.");
            return;
        }
        try {
            // call method
            mMethodfinishCliptrayService.invoke(mObj);
        } catch (Exception e) {
            // Debug.e(TAG,
            // "failure : mMethodfinishCliptrayService invocation failed.");
            e.printStackTrace();
        }
    }

    public static boolean isSupportCliptray(Context context) {
        String ret = "false";
        try {
            ClassLoader cl = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");

            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = String.class;

            Method get = SystemProperties.getMethod("get", paramTypes);

            // Parameters
            Object[] params = new Object[2];
            params[0] = new String("ro.lge.capp_cliptray");
            params[1] = new String("false");

            ret = (String) get.invoke(SystemProperties, params);

        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            ret = "false";
            // TODO
        }
        
        return Boolean.parseBoolean(ret);
    }
}