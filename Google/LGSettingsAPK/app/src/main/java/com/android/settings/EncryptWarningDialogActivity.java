package com.android.settings;

import com.android.settings.Utils;
import com.android.settings.R;
import com.android.settings.SecuritySettings;
import android.app.Activity;
import android.os.Bundle;
import android.content.DialogInterface;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.content.Intent;
import java.io.File;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.LGSDEncManager;
import android.os.Handler;
import android.os.Message;
import com.android.settings.SecuritySettings;
import com.lge.config.ConfigBuildFlags;

import com.lge.mdm.LGMDMManager;

import android.util.Log;
import android.widget.Button;
/*[S] [2012.11.23] seunggu.yang SDCARD FULL & MEDIAE EXCEPTION ENCRYPTION*/
import android.widget.Toast;
/*[E] [2012.11.23] seunggu.yang SDCARD FULL & MEDIAE EXCEPTION ENCRYPTION*/

public class EncryptWarningDialogActivity extends AlertActivity implements
        DialogInterface.OnClickListener{

    /**
     * Indicates the encrypt nothing
     */
    public static final int LGMDMENCRYPTION_DISABLED = 0;

    /**
     * Indicates the encrypt only device
     */
    public static final int LGMDMENCRYPTION_DEVICE = 1;

    /**
     * Indicates the encrypt only storage
     */
    public static final int LGMDMENCRYPTION_STORAGE = 2;

    /**
     * Indicates the encrypt device and storage
     */
    public static final int LGMDMENCRYPTION_DEVICE_AND_STORAGE = 3;

    /**
     * Indicates the not use encryption policy
     */
    public static final int LGMDMENCRYPTION_NONE = 4;

    private static final int ENCRYPTION_END_MSG = 1;

    private LGContext mServiceContext = null;
    private LGSDEncManager mLGSDEncManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        // See assets/res/any/layout/dialog_activity.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.

        final AlertController.AlertParams p = mAlertParams;
        int res_icon = com.lge.R.drawable.ic_dialog_alert_holo;
        if (Utils.isUI_4_1_model(this)) {
            res_icon = R.drawable.no_icon;
        }
        p.mIconId = res_icon;
        p.mTitle = getString(R.string.sp_sd_encryption_need_password_title_NORMAL);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-29]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (com.android.settings.MDMSettingsAdapter.getInstance().setEncryptionInsetSD(p, this,
                    this) == true) {
                setupAlert();
                return;
            }
        }
        // LGMDM_END
        p.mPositiveButtonText = getString(R.string.sp_lg_software_note_yes);
        if (Utils.isSupportUIV4_2()) {
            p.mMessage = getString(R.string.sp_encrypt_warning_message_NORMAL_new);
        } else {
            p.mMessage = getString(R.string.sp_encrypt_warning_message_NORMAL);
        }
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.sp_lg_software_note_no);
        p.mNegativeButtonListener = this;

        setupAlert();

        if (ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY &&
            LGSDEncManager.getSDEncSupportStatus(this.getApplicationContext())) {
            mServiceContext = new LGContext(this.getApplicationContext());
            mLGSDEncManager = (LGSDEncManager)mServiceContext.getLGSystemService(LGContext.LGSDENC_SERVICE);
        }

    }

    private Handler confirmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case ENCRYPTION_END_MSG:
                    confirmHandler.removeMessages(ENCRYPTION_END_MSG);
                    Toast.makeText(EncryptWarningDialogActivity.this, R.string.sp_encrypt_warning_toast_NORMAL,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    public void onClick(DialogInterface dialog, int which) {
        Intent i;

        switch (which) {
            case BUTTON_NEGATIVE:
                if (ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mLGSDEncManager.externalSDCardDeleteMetaDir();
                            confirmHandler.sendMessage(Message.obtain(confirmHandler, ENCRYPTION_END_MSG));
                        }
                    }).start();
                } else {
                    if (SecuritySettings.getFullMediaExceptionOption() == SecuritySettings.NORMAL_CASE) {
                        i = new Intent("com.lge.settings.SD_ENCRYPTION_SETTINGS");
                    } else {
                        i = new Intent("com.lge.settings.SD_ENCRYPTION_SETTINGS_EXTENSION");
                    }
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(i);
                 }
                    finish();
                break;

            case BUTTON_POSITIVE:
                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-29]
                if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                    if (com.android.settings.MDMSettingsAdapter.getInstance()
                            .setOKEncryptionInsetSD(EncryptWarningDialogActivity.this) == true) {
                        return;
                    }
                }
                // LGMDM_END
                /*[S] [2012.11.23] seunggu.yang SDCARD FULL & MEDIAE EXCEPTION ENCRYPTION*/
                if (SecuritySettings.getFullMediaExceptionOption() != SecuritySettings.NORMAL_CASE) {
                        Toast.makeText(EncryptWarningDialogActivity.this, R.string.sp_encryption_keepenabling_NORMAL,
                        Toast.LENGTH_SHORT).show();
                }
                /*[E] [2012.11.23] seunggu.yang SDCARD FULL & MEDIAE EXCEPTION ENCRYPTION*/
                finish();
                break;

            default:
                finish();
                break;
        }
    }

}
