package com.android.settings.lgesetting;

import android.app.Activity;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.android.settings.lgesetting.Config.Config;

public class Utils {

    public static final boolean DBG = false;

    //==================================================================================
    // Operator

    public static boolean COUNTRY_KR = "KR".equals(Config.getCountry());
    private static boolean CARRIER_LGUPLUS = "LGU".equals(Config.getOperator());
    private static boolean CARRIER_KT = "KT".equals(Config.getOperator());
    private static boolean CARRIER_SKT = "SKT".equals(Config.getOperator());

    public static boolean COUNTRY_OPERATOR_LGUPLUS = COUNTRY_KR && CARRIER_LGUPLUS;
    public static boolean COUNTRY_OPERATOR_KT = COUNTRY_KR && CARRIER_KT;
    public static boolean COUNTRY_OPERATOR_SKT = COUNTRY_KR && CARRIER_SKT;
    public static boolean COUNTRY_OPERATOR_KOREA = COUNTRY_KR  && (CARRIER_LGUPLUS || CARRIER_KT || CARRIER_SKT);


    //==================================================================================
    // Soft Input
    public static void showSoftInput(Activity mActivity, EditText mEditText) {
        mEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        // ** forced soft input keyboard
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        // ** no force
        //imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);

        // android.R.attr.windowSoftInputMode | stateAlwaysVisible);
        // http://developer.android.com/reference/android/R.attr.html#windowSoft...
        //imm.showSoftInputFromInputMethod(mEditText.getWindowToken(), InputMethodManager.SHOW_FORCED);
    }

    public static void hideSoftInput(Activity mActivity, EditText mEditText) {
        if (mEditText.getWindowToken() != null) {
            InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            // ** hidden soft input keyboard
            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
    }

    //==================================================================================
    // EditText
    public static void setEditTextEndCursor(EditText editText) {
        int position = editText.length();
        // ** end cursor
        editText.setSelection(position, position);
    }

    public static void setEditTextSelection(EditText editText) {
        int position = editText.length();
        // ** mark all-string
        editText.setSelection(0, position);
    }

    //==================================================================================
    // Resune / Pause
    public static void onResume(Window window) {
        // ** keep lcd always on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // ** block key of Home/Search/Endcall/etc
        //android.os.SystemProperties.set("persist.sys.testmode", "keytest");
        //WindowManager.LayoutParams attrs = window.getAttributes();
        //attrs.extend = WindowManager.LayoutParams.EXTEND_BYPASS_HOME_KEY
        //             | WindowManager.LayoutParams.EXTEND_BYPASS_SEARCH_KEY;
        //window.setAttributes(attrs);
    }

    public static void onPause(Window window) {
        // ** release lcd off
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // ** release key
        //android.os.SystemProperties.set("persist.sys.testmode", "0");
        //WindowManager.LayoutParams attrs = window.getAttributes();
        //attrs.extend = 0;
        //window.setAttributes(attrs);
    }

}