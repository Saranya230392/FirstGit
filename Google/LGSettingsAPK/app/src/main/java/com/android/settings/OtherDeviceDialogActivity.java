package com.android.settings;

import com.android.settings.R;
import android.app.Activity;
import android.os.Bundle;
import android.content.DialogInterface;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

public class OtherDeviceDialogActivity extends AlertActivity implements
        DialogInterface.OnClickListener {

        String TAG = "LGSD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        final AlertController.AlertParams p = mAlertParams;
        int res_icon = com.lge.R.drawable.ic_dialog_alert_holo;
        if (Config.getFWConfigBool(this,
                com.lge.config.ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY,
                "com.lge.config.ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY")) {
            p.mTitle = getString(R.string.sp_storage_sd_card_format_title);
            p.mMessage = getString(R.string.sp_storage_sd_card_format_description);
        } else {
            p.mTitle = getString(R.string.encrypt_other_device_msg_title);
            p.mMessage = getString(R.string.encrypt_other_device_msg);
        }
        p.mIconId = res_icon;
        p.mPositiveButtonText = getString(android.R.string.ok);
        p.mPositiveButtonListener = this;
        setupAlert();
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
            default:
                finish();
                break;
        }
    }
}
