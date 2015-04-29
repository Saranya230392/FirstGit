/* LGE_CHANGE, User Friendly VPN */
package com.android.settings.vpn2;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.android.settings.R;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;

public class CertInstallSender extends Activity {

    public static final String INSTALL_ACTION = "android.credentials.INSTALL";
    public int mType = 0;

    // [shpark82.park] Keystore User Dialog [START]
    private static final int DIALOG_KEYSTORE_USER_ID = 1;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private boolean keystoreUserCheck = false;
    // [shpark82.park] Keystore User Dialog [END]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if (extras != null) {
            mType = extras.getInt("Type");
        }

        // [shpark82.park] Keystore User Dialog [START]
//        Intent intent = new Intent(INSTALL_ACTION);
//        startActivityForResult(intent, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        keystoreUserCheck = preferences.getBoolean("keystore_user_check", false);
        if (VpnSettings.FEATURE_LG_VPN) {
            if (!keystoreUserCheck) {
                showDialog(DIALOG_KEYSTORE_USER_ID);
            } else {
                Intent intent = new Intent(INSTALL_ACTION);
                startActivityForResult(intent, 1);
            }
        } else {
            Intent intent = new Intent(INSTALL_ACTION);
            startActivityForResult(intent, 1);
        }
        // [shpark82.park] Keystore User Dialog [END]
    }
    // [cgyu.yu] Keystore user dialog duplicate runs [Start]
    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
            case DIALOG_KEYSTORE_USER_ID:
                return keystoreUserDialog();
            default:
                return super.onCreateDialog(id, bundle);
        }
    }
    // [cgyu.yu] Keystore user dialog duplicate runs [END]

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("CertInstallerSender", "requestCode = " + requestCode + " resultCode = " + resultCode);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String CertKey = null;
            Bundle extras = data.getExtras();
            if (extras != null) {
                CertKey = extras.getString("CertKey", null);
            }

            Intent intent = new Intent();
            intent.setAction("com.lge.vpn.friendlyvpn");
            intent.putExtra("Type", mType);
            intent.putExtra("CertKey", CertKey);
            sendBroadcast(intent);
        } else {
            Log.w("CertInstallerIntent", "unknown request code: " + requestCode);
        }
        finish();

    }
    // [shpark82.park] Keystore User Dialog [START]
    private Dialog keystoreUserDialog() {
        LayoutInflater inflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.vpn_native_user_popup_dialog, null);

        final CheckBox notShowAgain = (CheckBox)layout.findViewById(R.id.do_not_show_again);
        notShowAgain.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.sp_dlg_note_NORMAL);
//        dialog.setIconAttribute(android.R.attr.alertDialogIcon);
        dialog.setView(layout);
        dialog.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int button) {
                if (notShowAgain.isChecked()) {
                    editor = preferences.edit();
                    editor.putBoolean("keystore_user_check", true);
                    editor.commit();
                }
                Intent intent = new Intent(INSTALL_ACTION);
                startActivityForResult(intent, 1);
            }
        });
        dialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                finish();
            }
        });
        return dialog.create();
    }
    // [shpark82.park] Keystore User Dialog [END]
}
