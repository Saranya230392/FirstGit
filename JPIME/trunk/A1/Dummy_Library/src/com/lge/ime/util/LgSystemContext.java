/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */

package com.lge.ime.util;

import android.content.*;

import java.lang.reflect.*;

/**
 * Reflection of LGContext
 * 
 * refer to  http://blog.naver.com/ijly2004?Redirect=Log&logNo=40179813405
 *  
 * @author dongseok.lee
 *
 */
public class LgSystemContext {
    private static final String TAG = "LgSystemContext";
    private static LgSystemContext sInstance;
    private static final String CLASSPATH_SYSTEM_LGCONTEXT = "com.lge.systemservice.core.LGContextImpl";
    private Method mMethodgetLGSystemService;
    private Object[] mParamGet;
    private Object mObj;

    private LgSystemContext(Context context) {
        try {
            ClassLoader cl = context.getClassLoader();
            // lookup class
            Class<?> c = cl.loadClass(CLASSPATH_SYSTEM_LGCONTEXT);
            //
            Class<?>[] paras = new Class[]{Context.class};
            Constructor<?> con = c.getConstructor(paras);
            Object[] arg_paras = new Object[]{context};
            mObj  = con.newInstance(arg_paras);
            
            // generate a buffer to contain arguments.
            mParamGet = new Object[1];
            // lookup method
            Class<?>[] class_args = new Class[]{String.class};
            mMethodgetLGSystemService = c.getMethod("getLGSystemService", class_args);
//            Debug.i(TAG, "info : '%s' is loadded successfully.", CLASSPATH_SYSTEM_LGCONTEXT);
        } catch (Exception e) {
//            Debug.e(TAG, "failure : '%s' cannot be loaded.", CLASSPATH_SYSTEM_LGCONTEXT);
            e.printStackTrace();
        }
    }

    public static LgSystemContext getInstances(Context context) {
        if (sInstance == null) {
            sInstance = new LgSystemContext(context);
        }
        return sInstance;
    }

    public Object getLGSystemService(String key) {
        Object ret = null;
        
        if (mMethodgetLGSystemService == null) {
            return null;
        }
        if (mObj == null) {
            return null;
        }
        try {
            // Parameters
            Object[] params = mParamGet;
            params[0] = key;
            ret = mMethodgetLGSystemService.invoke(mObj, key);
        } catch (Exception e) {
//            Debug.e(TAG, "failure : method invocation failed.");
            e.printStackTrace();
        } finally {
            mParamGet[0] = null;
        }
        return ret;
    }
    
    public static void release() {
        sInstance = null;
    }
}