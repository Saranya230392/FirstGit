package com.android.settings;

import com.android.settings.R;
import android.app.Activity;
import android.os.Bundle;
import android.content.DialogInterface;
import android.content.Intent;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.settings.lockscreen.ChooseLockSettingsHelper;
import com.android.settings.lockscreen.ChooseLockGeneric;
import android.content.res.Resources;
import android.app.admin.DevicePolicyManager;
import android.text.TextUtils;
import android.widget.Toast;
import com.android.internal.widget.LockPatternUtils;
import android.util.Log;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.LGSDEncManager;
import android.os.Handler;
import android.os.Message;
import com.android.settings.Utils;

public class EncryptedSDCardDialogActivity extends AlertActivity implements
DialogInterface.OnClickListener {

        String TAG = "LGSD";
        private static final int SDCARD_CODE = 2;
        private static final String SET_ENCRYPTION_REQUEST = "encryption_confirm";
        private static final int MIN_PASSWORD_QUALITY = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
        private static final int DECRYPTION_END_MSG = 1;
        private LGContext mServiceContext = null;
        private LGSDEncManager mLGSDEncManager = null;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // Be sure to call the super class.
            super.onCreate(savedInstanceState);

            final AlertController.AlertParams p = mAlertParams;
            if (Utils.isUI_4_1_model(this)) {
                 p.mIconId = R.drawable.no_icon;
            } else {
                 p.mIconId = R.drawable.ic_dialog_alert_holo;
            }
            p.mTitle = getString(R.string.sp_sd_encryption_need_password_title_NORMAL);

            p.mView = getLayoutInflater().inflate(R.layout.encrypted_sdcard_dialog_activity, null);
            p.mPositiveButtonText = getString(R.string.sp_lg_software_note_yes);
            p.mPositiveButtonListener = this;
            p.mNegativeButtonText = getString(R.string.sp_lg_software_note_no);
            p.mNegativeButtonListener = this;

            setupAlert();

            if (LGSDEncManager.getSDEncSupportStatus(this.getApplicationContext())) {
                mServiceContext = new LGContext(this.getApplicationContext());
                mLGSDEncManager = (LGSDEncManager)mServiceContext.getLGSystemService(LGContext.LGSDENC_SERVICE);
            }
        }

    private Handler confirmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case DECRYPTION_END_MSG:
                    confirmHandler.removeMessages(DECRYPTION_END_MSG);
                    Toast.makeText(EncryptedSDCardDialogActivity.this, R.string.sp_encrypt_enable_NORMAL,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };


    public void onClick(DialogInterface dialog, int which) {
         switch (which) {
            case BUTTON_NEGATIVE:
                cancel();
                break;
            case BUTTON_POSITIVE:
            if (!runKeyguardConfirmation(SDCARD_CODE)) {
                        Intent intent = new Intent(EncryptedSDCardDialogActivity.this, ChooseLockGeneric.class);
                        intent.putExtra(SET_ENCRYPTION_REQUEST, "encryption");
                        startActivityForResult(intent, SDCARD_CODE);
            }
                break;
            default:
                break;
        }
     }

    @Override
        public void dismiss() {
            Log.e(TAG, "dismiss called");
        }

  private boolean runKeyguardConfirmation(int request) {
        // 1.  Confirm that we have a sufficient PIN/Password to continue
        int quality = new LockPatternUtils(EncryptedSDCardDialogActivity.this).getActivePasswordQuality();
        if (quality < MIN_PASSWORD_QUALITY) {
            return false;
        }
        // 2.  Ask the user to confirm the current PIN/Password
        Resources res = EncryptedSDCardDialogActivity.this.getResources();
        return new ChooseLockSettingsHelper(EncryptedSDCardDialogActivity.this)
                .launchConfirmationActivity(request,
                        res.getText(R.string.master_clear_gesture_prompt),
                        res.getText(R.string.master_clear_gesture_explanation));
    }

    @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
       super.onActivityResult(requestCode, resultCode, data);
       if (resultCode == Activity.RESULT_OK && data != null) {
           final String password = data.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
           if (!TextUtils.isEmpty(password)) {
                if (requestCode == SDCARD_CODE) {
                  new Thread(new Runnable() {
                    @Override
                    public void run() {
                              mLGSDEncManager.externalSDCardEnableEncryptionUserPassword("externalStorage", password);
                               confirmHandler.sendMessage(Message.obtain(confirmHandler, DECRYPTION_END_MSG));

                          }
                  }).start();
                  cancel();
              }
          } else {
                EncryptedSDCardDialogActivity.this.finish();
          }
      }
  }
}
