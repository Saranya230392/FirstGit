package com.android.settings.lgesetting.ui;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


public class InputMethodHelper {
    public static void popupInputMethod(Context aContext, EditText aEdit) {
        aEdit.requestFocus();
        try{
            InputMethodManager imm = (InputMethodManager)((Activity)aContext).getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }catch(NullPointerException e){

        }
    }

    //phjjiny.park 100805 QM1: inputmethod hide
    public static void HideInputMethod(Context aContext, EditText aEdit) {
        try{
            InputMethodManager imm = (InputMethodManager)((Activity)aContext).getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(aEdit.getWindowToken(), 0);
        }catch(NullPointerException e){

        }
    }

    public static void ShowInputMethod(Context aContext, EditText aEdit) {
        try{
            InputMethodManager imm = (InputMethodManager)((Activity)aContext).getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(aEdit, InputMethodManager.SHOW_FORCED);
        }catch(NullPointerException e){

        }
    }
}
