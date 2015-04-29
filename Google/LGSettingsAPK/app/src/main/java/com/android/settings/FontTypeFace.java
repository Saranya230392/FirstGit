package com.android.settings;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.graphics.Typeface;

import android.util.Log;
import com.lge.config.*;

public class FontTypeFace {

    final static String TAG = "FontTypeFace";

    // 20120204 dongseok.lee : Separate using the config var by CAPP_ICS.
    private static Field _CAPP_FONTS;

    static {
         try {

             // 20120204 dongseok.lee : Separate using the config var by CAPP_ICS.
            _CAPP_FONTS = ConfigBuildFlags.class.getField("CAPP_FONTS");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 20120204 dongseok.lee : Separate using the config var by CAPP_ICS.
    public static boolean getUseCappFonts(){
        boolean result = false;
        if(_CAPP_FONTS != null) {
             try {
                 result =  _CAPP_FONTS.getBoolean(ConfigBuildFlags.class);
                } catch (Throwable t) {
                    Log.e(TAG, "Error : not support capp fonts.");
                }
            }
        return result;
    }
}
