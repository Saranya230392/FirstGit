package com.android.settings.lgesetting.wireless;

import android.view.KeyEvent;

class DefineBlockKey {

    public static boolean onKey(int keyCode) {
        //must be : modify for mobile hard-key
        if (keyCode == KeyEvent.KEYCODE_SEARCH)
            return true;
        return false;
    }

}