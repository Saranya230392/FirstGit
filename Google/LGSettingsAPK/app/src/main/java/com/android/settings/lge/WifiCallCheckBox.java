
package com.android.settings.lge;

import android.content.Context;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;
import com.android.settings.R;

import com.movial.ipphone.WifiCallSwitchPreference;

public class WifiCallCheckBox extends WifiCallSwitchPreference {

    public static boolean isSupport() {
        return ("ims".equals(android.os.SystemProperties.get("ro.product.ims")) || "epdg".equals(android.os.SystemProperties.get("ro.product.ims")));
    }

    public WifiCallCheckBox(Context context) {
        super(context);
    }

    public WifiCallCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WifiCallCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
