package com.android.settings.sdencryption;

import android.content.Context;

import com.android.settings.R;
public class SDEncryption {
    Boolean isEnable = false;

    public SDEncryption() {

    }

    public boolean getSDEncryptedSupport() {
        return isEnable;
    }

    public boolean defaultEncryptionSupport(Context context) {
        // 
        return context.getResources().getBoolean(com.lge.R.bool.config_default_encrypt);
    }

    public int getSDcardEncryptedMenuId() {
        return R.xml.security_settings_encrypted_sd;
    }
    public int getSDcardUnencryptedMenuId() {
        return R.xml.security_settings_unencrypted_sd;
    }
}
