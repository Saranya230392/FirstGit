package com.android.settings.lgesetting.wireless;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.settings.R;
import android.net.ConnectivityManager;
import com.lge.constants.KeyExceptionConstants;
import com.lge.constants.SettingsConstants;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import com.android.settings.Utils;

public class DataNetworkModePayPopupLGT extends Activity implements DialogInterface.OnKeyListener {

    private static final String TAG = "LGE_DATA_PAYPOPUP_LGT";

    private AlertDialog.Builder paypopup;
    private AlertDialog paydialog;
    private static final int USER_RESPONSE_YES = 2;
    private static final int USER_RESPONSE_NO = 1;
    private static final int USER_RESPONSE_NOTYET = 0;
    private ConnectivityManager mConnMgr;
    private TelephonyManager mTM;

    boolean is_toggled = false;
    boolean is_selected = false;
    boolean mIsWizard = false;
    boolean is_3G_Model = SystemProperties.get("net.is_3g", "false").equals("true");
    private StatusBarManager mStatusBar = null;

    private DialogInterface.OnDismissListener AlwaysReqWhenPS =
            new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    int userRes = Settings.Secure.getInt(getApplicationContext()
                            .getContentResolver(),
                            SettingsConstants.Secure.DATA_NETWORK_USER_PAYPOPUP_RESPONSE, 0);
                    Log.d(TAG, "onDismiss() userRes " + userRes + " is_selected " + is_selected);

                    if (is_selected == true) {
                        Log.d(TAG, "onDismiss() finish ");
                        is_selected = false;
                        finish();
                    }
                }
            };

    private boolean mIsForg = false;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.GO_ABROAD".equals(intent.getAction())) {
                Log.d(TAG, "Received android.intent.action.GO_ABROAD");
                if (paydialog != null) {
                    paydialog.dismiss();
                    Settings.Secure.putInt(getApplicationContext().getContentResolver(),
                            SettingsConstants.Secure.DATA_NETWORK_WAIT_FOR_PAYPOPUP_RESPONSE, 0);
                }
                finish();
            }
            if ("com.lge.android.intent.action.BEFORE_KILL_APP_BY_FWK"
                .equals(intent.getAction())) {
                if (false == mIsForg) {
                    Log.d(TAG, "action : skip the Action for popup");
                    return;
                }
                Log.d(TAG, "action : BEFORE_KILL_APP_BY_FWK");
                String topPackage
                    = intent.getStringExtra("com.lge.intent.extra.topPkgName");
                if (topPackage.equals(context.getPackageName()) == false) {
                    return;
                } else {
                    setResultCode(2);
                    setResultData(context.getPackageName());
                    abortBroadcast();
                }
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams attrs;
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mConnMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        mTM = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.GO_ABROAD");
        intentFilter.addAction("com.lge.android.intent.action.BEFORE_KILL_APP_BY_FWK");
        registerReceiver(mBroadcastReceiver, intentFilter);
        mIsWizard
            = (Settings.Secure.getInt(getApplicationContext().getContentResolver(),
                    Settings.Secure.USER_SETUP_COMPLETE, 0) == 0);
        Log.d(TAG, "mIsWizard " + mIsWizard);
        int isNegativeBtnStr
            = mIsWizard ? android.R.string.no : R.string.sp_settings_label_NORMAL;
        // Show Pay Popup
        paypopup = new AlertDialog.Builder(this)
                .setTitle(R.string.data_usage_enable_mobile)
                .setCancelable(true)
                .setNegativeButton(isNegativeBtnStr, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        SetUserResponse(USER_RESPONSE_NO);
                        is_selected = true;
                        Log.d(TAG, "mIsWizard " + mIsWizard);
                        // Release Resource
                        mTM.setDataEnabled(mIsWizard ? false : true);
                        int mToastMsg;
                        if (mIsWizard) {
                            mToastMsg = is_3G_Model ?
                                                R.string.sp_paypopup_blocked_for_lgt_3G_NORMAL :
                                                R.string.sp_data_network_no_toast_msg_lgu;
                        } else {
                            mToastMsg = is_3G_Model ?
                                            R.string.sp_paypopup_allowed_for_lgt_3G_NORMAL :
                                            R.string.sp_data_network_yes_toast_msg_lgu;
                        }
                        Toast.makeText(getApplicationContext(),
                            mToastMsg, Toast.LENGTH_SHORT).show();
                        paydialog.dismiss();
                        if (!mIsWizard) {
                            Intent mIntent = new Intent();
                            mIntent.setClassName("com.android.phone", "com.android.phone.MobileNetworkSettings");
                            startActivity(mIntent);
                        }
                    }
                })
                .setPositiveButton(R.string.data_lgt_allow, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        SetUserResponse(USER_RESPONSE_YES);
                        is_selected = true;
                        mTM.setDataEnabled(true);
                        int mToastMsg = is_3G_Model ?
                                            R.string.sp_paypopup_allowed_for_lgt_3G_NORMAL : 
                                            R.string.sp_data_network_yes_toast_msg_lgu;
                        Toast.makeText(getApplicationContext(),
                            mToastMsg, Toast.LENGTH_SHORT).show();
                        paydialog.dismiss();
                    }
                });

        if (is_3G_Model == false) {
            paypopup.setMessage(R.string.sp_data_enable_lgu_NORMAL_ux4);
        } else if (is_3G_Model == true) {
            paypopup.setMessage(R.string.sp_paypopup_for_lgt_3G_NORMAL);
        }
        if (false == Utils.isUI_4_1_model(getApplicationContext())) {
            paypopup.setIconAttribute(android.R.attr.alertDialogIcon);
        }

        paydialog = paypopup.create();
        attrs = getWindow().getAttributes();
        attrs.privateFlags |= KeyExceptionConstants.BYPASS_HOME_KEY;
        attrs.privateFlags |= KeyExceptionConstants.BYPASS_APP_SWITCH_KEY;
        paydialog.getWindow().setAttributes(attrs);
        mStatusBar = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);
        paydialog.setOnDismissListener(AlwaysReqWhenPS);
        paydialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    return DefineBlockKey.onKey(keyCode);
                }
                return false;
            }
        });
        paydialog.setCancelable(false);
        paydialog.setCanceledOnTouchOutside(false);
    }

    private void SetUserResponse(int response) {
        Log.d(TAG, "SetUserResponse " + response);
        if (response == USER_RESPONSE_NOTYET) {
            Settings.Secure.putInt(getApplicationContext()
                    .getContentResolver(),
                    SettingsConstants.Secure.DATA_NETWORK_USER_PAYPOPUP_RESPONSE,
                    response);
            Settings.Secure.putInt(getApplicationContext()
                    .getContentResolver(),
                    SettingsConstants.Secure.DATA_NETWORK_WAIT_FOR_PAYPOPUP_RESPONSE,
                    1);
        } else if (response == USER_RESPONSE_YES) {
            Settings.Secure.putInt(getApplicationContext()
                    .getContentResolver(),
                    SettingsConstants.Secure.DATA_NETWORK_USER_PAYPOPUP_RESPONSE,
                    response);
            Log.d(TAG,
                    "end SetUserResponse "
                            + Settings.Secure.getInt(getApplicationContext().getContentResolver(),
                                    SettingsConstants.Secure.DATA_NETWORK_USER_PAYPOPUP_RESPONSE,
                                    response));
        } else if (response == USER_RESPONSE_NO) {
            // Value Init.
            Settings.Secure.putInt(getApplicationContext()
                    .getContentResolver(),
                    SettingsConstants.Secure.DATA_NETWORK_WAIT_FOR_PAYPOPUP_RESPONSE,
                    0);
            Settings.Secure.putInt(getApplicationContext()
                    .getContentResolver(),
                    SettingsConstants.Secure.DATA_NETWORK_USER_PAYPOPUP_RESPONSE,
                    response);
        }
    }

    private boolean shouldConsumeKey(int aKeyCode, KeyEvent aKeyEvent) {
        if (aKeyCode == KeyEvent.KEYCODE_SEARCH) {
            return true;
        }
        return false;
    }

    public boolean onKeyDown(int aKeyCode, KeyEvent aKeyEvent) {
        if (shouldConsumeKey(aKeyCode, aKeyEvent)) {
            return true;
        }
        return super.onKeyDown(aKeyCode, aKeyEvent);
    }

    public boolean onKey(DialogInterface aDialog, int aKeyCode, KeyEvent aKeyEvent) {
        if (shouldConsumeKey(aKeyCode, aKeyEvent)) {
            return true;
        }
        return false;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "[LGE_DATA]DataNetworkModePayPopupLGT onDestory ");
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsForg = true;
        if ((paydialog != null) && !paydialog.isShowing()) {
            Log.d(TAG, "onResume");
            paydialog.show();
        }
        if (mStatusBar != null) {
            mStatusBar.disable(StatusBarManager.DISABLE_EXPAND);
            Log.d(TAG, "onResume StatusBarManager.DISABLE_EXPAND");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsForg = false;
        if (mStatusBar != null) {
            mStatusBar.disable(StatusBarManager.DISABLE_NONE);
            Log.d(TAG, "onPause StatusBarManager.DISABLE_NONE");
        }
    }

}
