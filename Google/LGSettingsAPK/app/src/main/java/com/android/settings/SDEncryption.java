package com.android.settings;

import android.content.Context;

public class SDEncryption {
    Boolean isEnable = false;

    public SDEncryption() {

    }

    public boolean getSDEncryptedSupport() {
        return isEnable;
    }

    public int getSDcardEncryptedMenuId() {
        return R.xml.security_settings_encrypted;
    }

    public int getSDcardUnencryptedMenuId() {
        return R.xml.security_settings_unencrypted;
    }
}
