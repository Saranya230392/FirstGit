package com.android.settings;

import com.android.settings.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.storage.IMountService;
import android.content.DialogInterface;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.content.Intent;

import android.util.Log;
import android.widget.Button;

public class CryptKeeperWarningDialog extends AlertActivity implements
        DialogInterface.OnClickListener {

    public static final boolean CHECKENCRYPTIONSTATUS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AlertController.AlertParams p = mAlertParams;
        p.mIconId = com.lge.R.drawable.ic_dialog_alert_holo;
        p.mView = getLayoutInflater().inflate(R.layout.encrypt_reset_warning_dialog_activity, null);
        p.mTitle = getString(R.string.crypt_keeper_encrypted_summary);
        p.mPositiveButtonText = getString(android.R.string.ok);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(android.R.string.cancel);
        p.mNegativeButtonListener = this;
        setupAlert();
    }

    public void onClick(DialogInterface dialog, int which) {
        Intent i;

        switch (which) {
        case BUTTON_POSITIVE:
            i = new Intent("com.lge.settings.START_UNENCRYPTION");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(i);
            finish();
            break;
        case BUTTON_NEGATIVE:
            dialog.dismiss();
            break;
        default:
            dialog.dismiss();
            break;
        }
    }
}
