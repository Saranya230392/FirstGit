package com.android.settings.utils;

import com.android.settings.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.os.SystemProperties;

public class CheckProperty extends Activity {
    TextView mPropertiesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.check_properties);
        mPropertiesText = (TextView) findViewById(R.id.properties_txt);
//Common_START
        propertyCheckString("ro.build.product", "Common");
        propertyCheckString("ro.build.target_country", "Common");
        propertyCheckString("ro.build.target_operator", "Common");
        propertyCheckString("ro.build.target_ril_platform", "Common");
        propertyCheckString("ro.build.type", "Common");
        propertyCheckString("ro.product.device", "Common");
        propertyCheckString("ro.product.device", "Common");
//Common_END
        propertyCheckBoolean("ro.lge.mtk_dualsim", "MTK MultiSim");
        propertyCheckBoolean("ro.lge.mtk_triplesim", "MTK mtk_triplesim");
        propertyCheckBoolean("persist.radio.multisim.config", "MultiSim");
        propertyCheckBoolean("ro.lge.capp_emotional_led", "EmotionalLED");
        propertyCheckString("ro.device.memory.system", "Storage");
        propertyCheckString("ro.device.memory.internal", "Storage");
        propertyCheckBoolean("ime_onehand_keyboard", "One-handed Operation");
        propertyCheckBoolean("ro.support_mpdn", "Networks");
        propertyCheckString("ro.telephony.default_network", "Mobile networks");
        propertyCheckString("ro.radio.networkmode", "Mobile networks");
        propertyCheckString("gsm.sim.operator.numeric", "LTE ready");
        propertyCheckString("gsm.sim.operator.alpha", "LTE ready");
        propertyCheckString("gsm.sim.operator.gid", "LTE ready");
        propertyCheckString("gsm.sim.operator.imsi", "LTE ready");
        propertyCheckString("gsm.sim.state", null);
        propertyCheckString("gsm.operator.isroaming", null);
        propertyCheckString("ro.product.name", null);
        propertyCheckString("ro.factorytest", "2 - usb connection type");
        propertyCheckString("ro.bootmode", "pifboot - usb connection type");
        propertyCheckBoolean("sys.allautotest.run", "usb connection type");
        propertyCheckBoolean("gsm.lge.ota_ignoreKey", "usb connection type");
        propertyCheckBoolean("ro.lge.capp_move_sdcard", "Apps - Move to SDcard");
        propertyCheckString("tangible_device_config", "Tangible");
        propertyCheckBoolean("ril.cdma.inecmmode", "Battery saver");

        propertyCheckInt("ro.lge.lcd_default_brightness", "Display");
        propertyCheckInt("ro.lge.lcd_auto_brightness_mode", "Display");
        propertyCheckInt("ro.lge.led_default_brightness", "Display");
        propertyCheckBoolean("ro.lge.capp_emotional_led", "Display");
        propertyCheckBoolean("lge.hw.frontkeyled", "Display");

        propertyCheckString("ro.lge.basebandversion", "About Phone");
        propertyCheckString("gsm.version.baseband", "About Phone");
        propertyCheckString("ro.ril.fccid", "About Phone");
        propertyCheckString("ro.lge.swversion", "About Phone");
        propertyCheckString("ro.lge.swversion_telcel", "About Phone");
        propertyCheckString("ril.lge.swversion", "About Phone");
        propertyCheckString("lge.version.sw", "About Phone");
        propertyCheckString("ro.lge.priversion", "About Phone");


        propertyCheckInt("ro.device.hapticfeedback", "Sound");
        propertyCheckInt("ro.config.vibrate_type", "Sound");


// DEV Option
        propertyCheckString("ro.build.characteristics", "Developper options");
        propertyCheckString("persist.sys.hdcp_checking", "Developper options");
        propertyCheckString("debug.egl.trace", "Developper options");
        propertyCheckBoolean("persist.sys.ui.hw", "Developper options");
        propertyCheckBoolean("debug.egl.force_msaa", "Developper options");
// DEV Option
        //propertyCheckBoolean("ro.com.lge.embedded_battery", "Regulatory and Safety"); // not using in JB+
        propertyCheckString("ro.product.locale.language", "Language");

        propertyCheckBoolean("persist.sys.encrypt", null);
        propertyCheckBoolean("persist.sys.sdencrypt", null);
        frameworkValueCheckBoolean(com.lge.R.bool.config_quick_memo_hotkey_customizing, "QuickButton");
    }

    private void propertyCheckString(String propertyName, String menuName) {

        if (SystemProperties.get(propertyName) != null) {
            mPropertiesText.setText(mPropertiesText.getText() + propertyName + " : " + SystemProperties.get(propertyName) + "\n");
            if (menuName != null) {
                mPropertiesText.setText(mPropertiesText.getText() + "\t" + menuName + "\n");

            }
        } else {
            //parent.removePreference(mEmotionLED);
        }

    }

    private void propertyCheckBoolean(String propertyName, String menuName) {

        if (SystemProperties.get(propertyName) != null) {
            mPropertiesText.setText(mPropertiesText.getText() + propertyName + " : " + SystemProperties.getBoolean(propertyName, false) + "\n");
            if (menuName != null) {
                mPropertiesText.setText(mPropertiesText.getText() + "\t" + menuName + "\n");

            }
        } else {
            //parent.removePreference(mEmotionLED);
        }

    }

    private void propertyCheckInt(String propertyName, String menuName) {

        if (SystemProperties.getInt(propertyName, -1) != -1) {
            mPropertiesText.setText(mPropertiesText.getText() + propertyName + " : " + SystemProperties.getInt(propertyName, -1) + "\n");
            if (menuName != null) {
                mPropertiesText.setText(mPropertiesText.getText() + "\t" + menuName + "\n");

            }
        } else {
            //parent.removePreference(mEmotionLED);
        }

    }


    private void frameworkValueCheckBoolean(int valueName, String menuName) {
        mPropertiesText.setText(mPropertiesText.getText() + "" + menuName + " : " + getResources().getBoolean(valueName) + "\n");
    }
}
