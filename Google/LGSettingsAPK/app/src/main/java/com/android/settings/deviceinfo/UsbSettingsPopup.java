/**
 * ConnectivityNotiDiagActivity.java
 *
 * Alert Dialog screen extends Activity class
 *
 * @author Ja Hoon Ku  ( jahoon.ku@lge.com )
 * @version 1.0
 * @since 1.0
 * @Show diag screen which users can notice that tethering needs to be charged.
 * @Optimized for P2
 */

package com.android.settings.deviceinfo;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.LayoutInflater;
import android.content.Context;
import android.webkit.WebSettings;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.usb.UsbManager;

import com.lge.constants.UsbManagerConstants;

public final class UsbSettingsPopup extends Activity {

    private static final String TAG = "UsbSettingsPopup";

    private Context mContext;
    private UsbManager mUsbManager;

    private String mDefaultFunction;
    private boolean mChargeModeChanged;
    private boolean mTetherModeKeep;
    private boolean mCheckingEntitlement;

    private static final int MHS_REQUEST = 0;
    private static final int TETHER_USC_REQUEST = MHS_REQUEST + 1;
    private static final int TETHER_KDDI_REQUEST = MHS_REQUEST + 2;

    private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "[AUTORUN] mStateReceiver() : action=" + action);
            if (action.equals(UsbManager.ACTION_USB_STATE)) {
                boolean usbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
                boolean usbConfigured = intent.getBooleanExtra(UsbManager.USB_CONFIGURED, false);

                if (usbConnected == true && usbConfigured == false) {
                    Log.d(TAG, "[AUTORUN] mStateReceiver() : ===== USB Connected =====");
                    UsbSettingsControl.setUsbConnected(context, true);
                } else if (usbConnected == true && usbConfigured == true) {
                    Log.d(TAG, "[AUTORUN] mStateReceiver() : ===== USB Configured =====");
                    UsbSettingsControl.setUsbConnected(context, true);
                } else if (usbConnected == false && usbConfigured == false) {
                    Log.d(TAG, "[AUTORUN] mStateReceiver() : ===== USB Disconnected =====");
                    UsbSettingsControl.setUsbConnected(context, false);

                    if (UsbSettingsControl.isDisconnectBugModel()) {
                        if (!mUsbManager.getDefaultFunction().equals(
                                UsbManagerConstants.USB_FUNCTION_TETHER)) {
                            UsbSettingsControl.connectUsbTether(mContext, false);
                            mTetherModeKeep = true;
                            finish();
                        }
                    } else {
                        UsbSettingsControl.connectUsbTether(mContext, false);
                        mTetherModeKeep = true;
                        finish();
                    }
                } else {
                    Log.d(TAG, "[AUTORUN] mStateReceiver() : ===== USB Unknown Connected ====");
                    UsbSettingsControl.setUsbConnected(context, false);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);

        mTetherModeKeep = false;
        mChargeModeChanged = false;
        mCheckingEntitlement = false;

        mDefaultFunction = mUsbManager.getDefaultFunction();
        Log.d(TAG, "[AUTORUN] onCreate() : mDefaultFunction=" + mDefaultFunction);

        /*if (mDefaultFunction.equals(UsbManager.USB_FUNCTION_TETHER))*/ {
            if (Config.getCountry().equals("US")
                    && Config.getOperator().equals("ATT")
                    && (Settings.System.getInt(getContentResolver(),
                            SettingsConstants.System.TETHER_ENTITLEMENT_CHECK_STATE, 1) > 0)) {
                if (!mCheckingEntitlement) {
                    checkEntitlement();
                    mCheckingEntitlement = true;
                }
            }
            else if (Config.getOperator().equals("USC")) {
                startUsbTetherIntroPopup();
            }
            else if (Config.getOperator().equals(Config.KDDI)) {
                startTetherKDDIPopup();
            }
            else {
                callPopup(UsbSettingsControl.DIALOG_TETHERING_ALERT);
            }
        } /*else {
            finish();
          }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "[AUTORUN] onResume()");
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_STATE);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mStateReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "[AUTORUN] onPause()");
        unregisterReceiver(mStateReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "[AUTORUN] onStop() : mChargeModeChanged=" + mChargeModeChanged);
        Log.d(TAG, "[AUTORUN] onStop() : mTetherModeKeep=" + mTetherModeKeep);

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if (!isScreenOn) {
            return;
        }

        if (mChargeModeChanged) {
            Intent i = new Intent(UsbSettingsControl.ACTION_TETHER_STATE_CHANGE);
            i.putExtra(UsbSettingsControl.EXTRA_USB_DEFAULT_MODE,
                    UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY);
            mContext.sendStickyBroadcast(i);
        } else {
            if (!mTetherModeKeep) {
                UsbSettingsControl.connectUsbTether(mContext, false);

                UsbSettingsControl.mActivityUsbModeChange = true;
                mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY, true);

                Intent i1 = new Intent(UsbSettingsControl.ACTION_TETHER_STATE_CHANGE);
                i1.putExtra(UsbSettingsControl.EXTRA_USB_DEFAULT_MODE,
                        UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY);
                mContext.sendStickyBroadcast(i1);

                Intent i2 = new Intent(UsbSettingsControl.ACTION_ACTIVITY_FINISH);
                mContext.sendStickyBroadcast(i2);

                finish();
                return;
            }
        }

        if (UsbSettingsControl.mActivityFinish) {
            Intent i = new Intent(UsbSettingsControl.ACTION_ACTIVITY_FINISH);
            mContext.sendStickyBroadcast(i);
        } else {
            UsbSettingsControl.mActivityFinish = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "[AUTORUN] onDestroy()");
    }

    //      public boolean onKeyDown(int keyCode, KeyEvent event) {
    //          Log.d(TAG, "[AUTORUN] onKeyDown() : keyCode=" + keyCode);
    //        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
    //            finish();
    //        }
    //
    //        return super.onKeyDown(keyCode, event);
    //    }

    private void callPopup(int dialogId) {
        Log.d(TAG, "[AUTORUN] callPopup() : popup id=" + dialogId);

        Message m = null;
        m = mHandler.obtainMessage(dialogId);
        mHandler.sendMessage(m);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            try {
                switch (message.what) {
                case UsbSettingsControl.DIALOG_TETHERING_ALERT:
                    Dialog tetherAlertDialog = new AlertDialog.Builder(mContext)
                            .setTitle(R.string.sp_usb_tethering_NORMAL)
                            .setMessage(R.string.sp_usbtether_may_incur_charges_NORMAL)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setOnKeyListener(new DialogInterface.OnKeyListener() {
                                @Override
                                public boolean onKey(DialogInterface dialog, int keyCode,
                                        KeyEvent event) {
                                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                                        UsbSettingsControl.connectUsbTether(mContext, false);

                                        UsbSettingsControl.mActivityUsbModeChange = true;
                                        mUsbManager.setCurrentFunction(
                                                UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY, true);
                                        mChargeModeChanged = true;

                                        dialog.dismiss();
                                        finish();
                                        return true;
                                    }
                                    return false;
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    UsbSettingsControl.connectUsbTether(mContext, false);

                                    UsbSettingsControl.mActivityUsbModeChange = true;
                                    mUsbManager.setCurrentFunction(
                                            UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY, true);
                                    mChargeModeChanged = true;

                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            UsbSettingsControl.connectUsbTether(mContext, false);

                                            UsbSettingsControl.mActivityUsbModeChange = true;
                                            mUsbManager.setCurrentFunction(
                                                    UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY,
                                                    true);
                                            mChargeModeChanged = true;

                                            dialog.dismiss();
                                            finish();
                                        }
                                    })
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (!UsbSettingsControl.getUsbConnected(mContext)) {
                                                mTetherModeKeep = true;
                                                dialog.dismiss();
                                                finish();
                                                return;
                                            }

                                            if (UsbSettingsControl.connectUsbTether(mContext, true) == -1) {
                                                callPopup(UsbSettingsControl.DIALOG_TETHERING_DISCONNECTION);
                                                return;
                                            }

                                            UsbSettingsControl.mActivityUsbModeChange = true;
                                            mUsbManager.setCurrentFunction(
                                                    UsbManagerConstants.USB_FUNCTION_TETHER, true);
                                            UsbSettingsControl.connectUsbTether(mContext, true);
                                            mTetherModeKeep = true;

                                            dialog.dismiss();
                                            finish();
                                        }
                                    })
                            .create();
                    tetherAlertDialog.show();
                    break;
                case UsbSettingsControl.DIALOG_TETHERING_DISCONNECTION:
                    Dialog tetherDisconnectionDialog = new AlertDialog.Builder(mContext)
                            .setTitle(R.string.sp_usb_tethering_NORMAL)
                            .setMessage(R.string.sp_usbtether_may_incur_charges_NORMAL)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setOnKeyListener(new DialogInterface.OnKeyListener() {
                                @Override
                                public boolean onKey(DialogInterface dialog, int keyCode,
                                        KeyEvent event) {
                                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                                        UsbSettingsControl.connectUsbTether(mContext, false);

                                        UsbSettingsControl.mActivityUsbModeChange = true;
                                        mUsbManager.setCurrentFunction(
                                                UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY, true);
                                        mChargeModeChanged = true;

                                        dialog.dismiss();
                                        finish();
                                        return true;
                                    }
                                    return false;
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    UsbSettingsControl.connectUsbTether(mContext, false);

                                    UsbSettingsControl.mActivityUsbModeChange = true;
                                    mUsbManager.setCurrentFunction(
                                            UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY, true);
                                    mChargeModeChanged = true;

                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            UsbSettingsControl.connectUsbTether(mContext, false);

                                            UsbSettingsControl.mActivityUsbModeChange = true;
                                            mUsbManager.setCurrentFunction(
                                                    UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY,
                                                    true);
                                            mChargeModeChanged = true;

                                            dialog.dismiss();
                                            finish();
                                        }
                                    })
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (!UsbSettingsControl.getUsbConnected(mContext)) {
                                                mTetherModeKeep = true;
                                                dialog.dismiss();
                                                finish();
                                                return;
                                            }

                                            if (UsbSettingsControl.connectUsbTether(mContext, true) == -1) {
                                                callPopup(UsbSettingsControl.DIALOG_TETHERING_DISCONNECTION);
                                                return;
                                            }

                                            UsbSettingsControl.mActivityUsbModeChange = true;
                                            mUsbManager.setCurrentFunction(
                                                    UsbManagerConstants.USB_FUNCTION_TETHER, true);
                                            UsbSettingsControl.connectUsbTether(mContext, true);
                                            mTetherModeKeep = true;

                                            dialog.dismiss();
                                            finish();
                                        }
                                    })
                            .create();
                    tetherDisconnectionDialog.show();
                    break;
                case UsbSettingsControl.DIALOG_PROGRESS:
                    break;
                default:
                    break;
                }
            } catch (NullPointerException e) {
                Log.w(TAG, "[AUTORUN] Handle message process nullpointer exception for dialog=" + e);
            }
        }
    };

    private void checkEntitlement() {
        Intent send = new Intent(Intent.ACTION_MAIN);
        send.putExtra("Tether_Type", "USB");
        send.setClassName("com.lge.entitlementcheckservice",
                "com.lge.entitlementcheckservice.EntitlementDialogActivity");
        startActivityForResult(send, MHS_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Log.d(TAG, "[AUTORUN] onActivityResult() : requestCode=" + requestCode);
        Log.d(TAG, "[AUTORUN] onActivityResult() : resultCode=" + resultCode);
        Log.d(TAG, "[AUTORUN] onActivityResult() : intent=" + intent);

        if (requestCode == MHS_REQUEST) {
            if (intent != null) {
                if (resultCode == Activity.RESULT_OK) {
                    if ("USB".equals(intent.getExtra("Tether_Type"))) {
                        callPopup(UsbSettingsControl.DIALOG_TETHERING_ALERT);
                    }
                }
                else if (resultCode == Activity.RESULT_CANCELED) {
                    if ("USB".equals(intent.getExtra("Tether_Type"))) {
                        boolean usb_disConnected = intent
                                .getBooleanExtra("usb_disConnected", false);

                        UsbSettingsControl.connectUsbTether(mContext, false);
                        if (usb_disConnected) {
                            // nothing
                            mTetherModeKeep = true;
                        }
                        else {
                            UsbSettingsControl.mActivityUsbModeChange = true;
                            mUsbManager.setCurrentFunction(
                                    UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY, true);
                            mChargeModeChanged = true;
                        }
                        finish();
                    }
                }
            }
        }
        else if (requestCode == TETHER_USC_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (!UsbSettingsControl.getUsbConnected(mContext)) {
                    mTetherModeKeep = true;
                    finish();
                    return;
                }
                UsbSettingsControl.mActivityUsbModeChange = true;
                mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_TETHER, true);
                UsbSettingsControl.connectUsbTether(mContext, true);
                mTetherModeKeep = true;
                finish();
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                UsbSettingsControl.connectUsbTether(mContext, false);

                UsbSettingsControl.mActivityUsbModeChange = true;
                mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY, true);
                mChargeModeChanged = true;

                finish();
            }
        }
        else if (requestCode == TETHER_KDDI_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (!UsbSettingsControl.getUsbConnected(mContext)) {
                    mTetherModeKeep = true;
                    finish();
                    return;
                }

                if (UsbSettingsControl.connectUsbTether(mContext, true) == -1) {
                    callPopup(UsbSettingsControl.DIALOG_TETHERING_DISCONNECTION);
                    return;
                }
                UsbSettingsControl.mActivityUsbModeChange = true;
                mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_TETHER, true);
                UsbSettingsControl.connectUsbTether(mContext, true);
                mTetherModeKeep = true;
                finish();
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                UsbSettingsControl.connectUsbTether(mContext, false);

                UsbSettingsControl.mActivityUsbModeChange = true;
                mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY, true);
                mChargeModeChanged = true;
                finish();
            }
        }
    }

    private void startUsbTetherIntroPopup() {
        Intent send = new Intent(Intent.ACTION_MAIN);
        send.setClassName("com.android.settings",
                "com.android.settings.deviceinfo.UsbTetherIntroUSCActivity");
        startActivityForResult(send, TETHER_USC_REQUEST);
    }

    private void startTetherKDDIPopup() {
        int checkShow = Settings.System.getInt(mContext.getContentResolver(), "TETHER_POPUP_KDDI",
                0);
        if (checkShow == 1) {
            setTetherExceptPopup(mContext);
            return;
        }

        Intent send = new Intent(Intent.ACTION_MAIN);
        send.setClassName("com.android.settings",
                "com.android.settings.deviceinfo.TetherPopupKDDIActivity");
        startActivityForResult(send, TETHER_KDDI_REQUEST);
    }

    private void setTetherExceptPopup(Context mContext) {
        if (!UsbSettingsControl.getUsbConnected(mContext)) {
            mTetherModeKeep = true;
            finish();
            return;
        }

        if (UsbSettingsControl.connectUsbTether(mContext, true) == -1) {
            callPopup(UsbSettingsControl.DIALOG_TETHERING_DISCONNECTION);
            return;
        }

        UsbSettingsControl.mActivityUsbModeChange = true;
        mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_TETHER, true);
        UsbSettingsControl.connectUsbTether(mContext, true);
        mTetherModeKeep = true;
        Intent i_finish = new Intent(UsbSettingsControl.ACTION_ACTIVITY_FINISH);
        mContext.sendStickyBroadcast(i_finish);
        finish();
    }
}
