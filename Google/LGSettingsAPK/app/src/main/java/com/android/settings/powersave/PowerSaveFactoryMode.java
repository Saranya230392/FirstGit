package com.android.settings.powersave;

import android.os.SystemProperties;

public class PowerSaveFactoryMode {

    private final static String G1_PROPERTY = "ro.factorytest";

    public static boolean checkFactoryMode() {
        boolean factoryTest = false;

        String factoryTestStr = SystemProperties.get(G1_PROPERTY);
        if ("1".equals(factoryTestStr) || "2".equals(factoryTestStr)) {
            factoryTest = true;
        }

        return factoryTest;
    }
}
