package com.android.sdencryption.settings;

import com.android.settings.SecuritySettings;
import com.android.settings.sdencryption.Encryption_Data;
import com.android.settings.Utils;
import com.android.settings.R;
import android.app.Activity;
import android.os.Bundle;
import android.content.DialogInterface;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.content.Intent;
import android.os.SystemProperties;
import android.content.res.Configuration;
import android.widget.Toast;

public class SdcardFullDialogActivity extends AlertActivity implements
        DialogInterface.OnClickListener {

    private boolean mCrypteEnProperty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);
        mCrypteEnProperty = SecuritySettings.SDEncryption_checkEnableProperty();
        // See assets/res/any/layout/dialog_activity.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.

        final AlertController.AlertParams p = mAlertParams;
        if (Utils.isUI_4_1_model(this)) {
        } else {
            p.mIconId = com.lge.R.drawable.ic_dialog_alert_holo;
        }
        p.mTitle = getString(R.string.sp_sd_encryption_need_password_title_NORMAL);
        if (Encryption_Data.getSDEncryptionSpacesError() == true) {
            if (mCrypteEnProperty == false) {
                p.mMessage = getString(R.string.sp_full_encryption_spaces_NORMAL);
            } else {
                p.mMessage = getString(R.string.sp_full_decryption_spaces_NORMAL);
            }
            Encryption_Data.setSDEncryptionSpacesError(false);
        } else {
            if (mCrypteEnProperty == false) {
                p.mMessage = getString(R.string.sp_full_decryption_warning_msg);
            } else {
                p.mMessage = getString(R.string.sp_full_encryption_warning_msg);
            }
        }
        p.mNegativeButtonText = getString(android.R.string.ok);
        p.mNegativeButtonListener = this;
        setupAlert();
    }

    public void onClick(DialogInterface dialog, int which) {
        finish();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
