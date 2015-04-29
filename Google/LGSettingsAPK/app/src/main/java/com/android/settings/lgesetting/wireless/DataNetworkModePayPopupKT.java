package com.android.settings.lgesetting.wireless;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.os.Handler;
import android.os.Message;
import com.android.settings.R;
import com.lge.constants.KeyExceptionConstants;
import com.lge.constants.SettingsConstants;
import android.os.UserHandle;

public class DataNetworkModePayPopupKT extends Activity implements DialogInterface.OnKeyListener {

    private static final String TAG = "LGE_DATA_PAYPOPUP";
    private static final String INTENT_PAYPOPUP_WAITING_ALARM = "com.lge.settings.wireless.PAYPOPUPWAITINGALARM";
    private static final int PAYPOPUP_WAITING_TIMEOUT_MSG = 1;
    private static final int PAYPOPUP_TIME_OUT = 30 * 1000;

    private AlertDialog.Builder paypopup;
    private AlertDialog paydialog;
    private static final int USER_RESPONSE_YES = 2;
    private static final int USER_RESPONSE_SETTING = 1;
    private static final int USER_RESPONSE_NOTYET = 0;
    private ConnectivityManager mConnMgr;
    private TelephonyManager mTM;
    private int isRoaming;
    private StatusBarManager mStatusBar = null;
    private boolean isTransitionWifiToMobile;

    boolean is_toggled = false;
    boolean is_clicked = false;

    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals((INTENT_PAYPOPUP_WAITING_ALARM))) {
                Log.d(TAG, "got Action = " + action);
                if (Settings.Secure.getInt(getApplicationContext().getContentResolver(),
                        SettingsConstants.Secure.DATA_NETWORK_USER_PAYPOPUP_RESPONSE, 0) == USER_RESPONSE_NOTYET) {
                        SetUserResponse(USER_RESPONSE_YES);
                        if (isTransitionWifiToMobile) {
                            sendBroadcastAsUser(new Intent(ConnectivityManager.ACTION_DATA_WIFI_TO_MOBILE_TRANSITION), UserHandle.ALL);
                        } else {
                            mTM.setDataEnabled(true);
                        }
                }
                try {
                    paydialog.dismiss();
                } catch (Exception e) {
                    Log.w(TAG, "Exception");
                }
            }
        }
    };

    private DialogInterface.OnDismissListener AlwaysReqWhenPS = new DialogInterface.OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            Log.d(TAG, "********       OnDismissListener, is_clicked =  " + is_clicked);
            if (is_clicked == true) {
                Log.d(TAG, "********    finish()");
                mHandler.removeMessages(PAYPOPUP_WAITING_TIMEOUT_MSG);
                getApplicationContext().unregisterReceiver(mIntentReceiver);
                is_clicked = false;
                finish();
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams attrs;
        Log.d(TAG, "onCreate() ");

        final Intent extraIntent = getIntent();
        isRoaming = 0;
        isTransitionWifiToMobile = false;

        if (extraIntent != null) {
            isRoaming = extraIntent.getIntExtra("isRoaming", 0);
            isTransitionWifiToMobile = extraIntent.getBooleanExtra("isTransitionWifiToMobile", false);
        } else {
            Log.d(TAG, "intent = null");
        }
        Log.d(TAG, "isRoaming = " + isRoaming + ", isTransitionWifiToMobile = " + isTransitionWifiToMobile);
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_PAYPOPUP_WAITING_ALARM);
        getApplicationContext().registerReceiver(mIntentReceiver, filter);
        mConnMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        mTM = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mStatusBar = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);

        // Show Pay Popup
        paypopup = new AlertDialog.Builder(this)
                .setTitle(R.string.data_network_mode_dialog_title)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        mHandler.removeMessages(PAYPOPUP_WAITING_TIMEOUT_MSG);
                        SetUserResponse(USER_RESPONSE_YES);
                        if (isTransitionWifiToMobile) {
                            sendBroadcastAsUser(new Intent(ConnectivityManager.ACTION_DATA_WIFI_TO_MOBILE_TRANSITION), UserHandle.ALL);
                        } else {
                            mTM.setDataEnabled(true);
                        }
                         paydialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.data_kt_paypopup_setting, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        mHandler.removeMessages(PAYPOPUP_WAITING_TIMEOUT_MSG);
                        SetUserResponse(USER_RESPONSE_YES);
                        if (isTransitionWifiToMobile) {
                            sendBroadcastAsUser(new Intent(ConnectivityManager.ACTION_DATA_WIFI_TO_MOBILE_TRANSITION), UserHandle.ALL);
                        } else {
                            mTM.setDataEnabled(true);
                        }
                         SystemClock.sleep(100);
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        if (isRoaming == 1) {
                            intent.setClassName("com.lge.roamingsettings",
                                    "com.lge.roamingsettings.ktroaming.KTRoaming");
                        } else {
                            intent.setClassName("com.android.phone",
                                    "com.android.phone.MobileNetworkSettings");
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                        paydialog.dismiss();
                    }
                });
        if (isRoaming == 1) {
            paypopup.setMessage(R.string.paypopup_for_kt_roaming);
        } else {
            if (SystemProperties.get("net.is_3g", "false").equals("true")) {
                paypopup.setMessage(R.string.sp_paypopup_for_kt_3g_NORMAL);
            } else {
                paypopup.setMessage(R.string.paypopup_for_kt);
            }
        }
        paydialog = paypopup.create();

        attrs = getWindow().getAttributes();
        attrs.privateFlags |= KeyExceptionConstants.BYPASS_HOME_KEY;
        attrs.privateFlags |= KeyExceptionConstants.BYPASS_APP_SWITCH_KEY;
        paydialog.getWindow().setAttributes(attrs);
        paydialog.setOnDismissListener(AlwaysReqWhenPS);

        paydialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    return DefineBlockKey.onKey(keyCode);
                }
                return false;
            }
        });
        paydialog.setCancelable(false);
        paydialog.setCanceledOnTouchOutside(false);
        paydialog.show();

        // Initiate 30 sec timer
        Log.d(TAG, "sendEmptyMessageDelayed( " + PAYPOPUP_TIME_OUT + ")");
        mHandler.sendEmptyMessageDelayed(PAYPOPUP_WAITING_TIMEOUT_MSG, PAYPOPUP_TIME_OUT);
        SetUserResponse(USER_RESPONSE_NOTYET);

        Log.d(TAG, "Ask for answer for pay popup ");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.lge.android.intent.action.BEFORE_KILL_APP_BY_FWK");
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void SetUserResponse(int response) {
        Log.d(TAG, "SetUserResponse " + response);
        if (response == USER_RESPONSE_NOTYET) {
            is_clicked = false;
            Settings.Secure.putInt(getApplicationContext().getContentResolver(),
                    SettingsConstants.Secure.DATA_NETWORK_USER_PAYPOPUP_RESPONSE, response);
            Settings.Secure.putInt(getApplicationContext().getContentResolver(),
                    SettingsConstants.Secure.DATA_NETWORK_WAIT_FOR_PAYPOPUP_RESPONSE, 1);
        }
        else if (response == USER_RESPONSE_YES) {
            is_clicked = true;
            // Value Init.
            Settings.Secure.putInt(getApplicationContext().getContentResolver(),
                    SettingsConstants.Secure.DATA_NETWORK_USER_PAYPOPUP_RESPONSE, response);
            Settings.Secure.putInt(getApplicationContext().getContentResolver(),
                    SettingsConstants.Secure.DATA_NETWORK_WAIT_FOR_PAYPOPUP_RESPONSE, 0);

            Log.d(TAG, "********     SetUserResponse, dismiss    ");

            try {
                paydialog.dismiss();
            } catch (Exception e) {
                Log.w(TAG, "Exception");
            }
        }
        else if (response == USER_RESPONSE_SETTING) {
            is_clicked = true;
            // Value Init.
            Settings.Secure.putInt(getApplicationContext().getContentResolver(),
                    SettingsConstants.Secure.DATA_NETWORK_WAIT_FOR_PAYPOPUP_RESPONSE, 0);
            Settings.Secure.putInt(getApplicationContext().getContentResolver(),
                    SettingsConstants.Secure.DATA_NETWORK_USER_PAYPOPUP_RESPONSE, response);
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
        if (aKeyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "onKeyDown =  " + aKeyCode);
            SetUserResponse(USER_RESPONSE_YES);
            if (isTransitionWifiToMobile) {
                sendBroadcastAsUser(new Intent(ConnectivityManager.ACTION_DATA_WIFI_TO_MOBILE_TRANSITION), UserHandle.ALL);
            } else {
                mTM.setDataEnabled(true);
            }
         }
        return super.onKeyDown(aKeyCode, aKeyEvent);
    }

    public boolean onKey(DialogInterface aDialog, int aKeyCode, KeyEvent aKeyEvent) {
        if (shouldConsumeKey(aKeyCode, aKeyEvent)) {
            return true;
        }
        if (aKeyCode == KeyEvent.KEYCODE_BACK && is_toggled == false) {
            is_toggled = true;
            Log.d(TAG, "onKey =  " + aKeyCode + " is_toggled = " + is_toggled);
            SetUserResponse(USER_RESPONSE_YES);
            if (isTransitionWifiToMobile) {
                sendBroadcastAsUser(new Intent(ConnectivityManager.ACTION_DATA_WIFI_TO_MOBILE_TRANSITION), UserHandle.ALL);
            } else {
                mTM.setDataEnabled(true);
            }
         }
        return false;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "********      onConfigurationChanged ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "********      onStop ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBroadcastReceiver) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsForg = true;
        Log.d(TAG, "********      paydialog.isShowing  = " + paydialog.isShowing());
        if (paydialog != null) {
            paydialog.show();
        }

        if (mStatusBar != null) {
            mStatusBar.disable(StatusBarManager.DISABLE_EXPAND);
        }
        Log.d(TAG, "********      onResume ");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "********      onStart ");
    }

    @Override
    public void onPause() {
        mIsForg = false;
        if (mStatusBar != null) {
            mStatusBar.disable(StatusBarManager.DISABLE_NONE);
            Log.d(TAG, "pause StatusBarManager.DISABLE_NONE");
        }

        super.onPause();
        Log.d(TAG, "********      onPause ");
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Intent intent = new Intent(INTENT_PAYPOPUP_WAITING_ALARM);
            if (msg.what == PAYPOPUP_WAITING_TIMEOUT_MSG) {
                Log.d(TAG, "sendBroadcast(INTENT_PAYPOPUP_WAITING_ALARM)");
                getApplicationContext().sendBroadcast(intent);
            }
        }
    };

    private boolean mIsForg = false;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
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
}
