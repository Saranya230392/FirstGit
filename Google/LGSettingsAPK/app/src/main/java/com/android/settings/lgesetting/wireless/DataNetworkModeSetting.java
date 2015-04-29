package com.android.settings.lgesetting.wireless;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentQueryMap;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.KeyExceptionConstants;

import java.util.Observable;
import java.util.Observer;
import com.android.settings.SLog;
import android.view.LayoutInflater;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.lge.constants.SettingsConstants;
import com.android.settings.Utils;

public class DataNetworkModeSetting extends Activity
        implements DialogInterface.OnKeyListener {
    private static final String TAG = DataNetworkModeSetting.class.getSimpleName();

    public static boolean COUNTRY_KR = "KR".equals(Config.getCountry());
    private static boolean CARRIER_SKT = "SKT".equals(Config.getOperator());
    private static boolean CARRIER_OPEN = "OPEN".equals(Config.getOperator());
    public static boolean COUNTRY_OPERATOR_SKT = COUNTRY_KR && CARRIER_SKT;
    public static boolean COUNTRY_OPERATOR_OPEN = COUNTRY_KR && CARRIER_OPEN;

    private static final String INTENT_EXTRA_KEY_CANCELABLE = "cancelable";

    private boolean mModeDialogCancelable = true;
    private StatusBarManager mStatusBar = null;
    private boolean mIsBootable = false;
    private boolean mIsCancelable = true;
    private CheckBox mDataCheckbox;
    
    protected void setIsBootable() {
        mIsBootable = true;
        mIsCancelable = false;
    }

    protected void onCreate(Bundle savedInstanceState) {
        SLog.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mStatusBar = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);
        String cancelable = getIntent().getStringExtra(INTENT_EXTRA_KEY_CANCELABLE);
        if (TextUtils.isEmpty(cancelable) == false) {
            mModeDialogCancelable = cancelable.toUpperCase().equals("TRUE") ? true : false;
        }
        showDataNetworkModePopupSKT();
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.lge.android.intent.action.BEFORE_KILL_APP_BY_FWK");
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    protected void onResume() {
        SLog.d(TAG, "onResume()");
        super.onResume();
        mIsForg = true;
        if (!mIsCancelable && mStatusBar != null) {
            mStatusBar.disable(StatusBarManager.DISABLE_EXPAND);
        }
    }

    protected void onPause() {
        SLog.d(TAG, "onPause()");
        mIsForg = false;
        if (!mIsCancelable && mStatusBar != null) {
            mStatusBar.disable(StatusBarManager.DISABLE_NONE);
            SLog.d(TAG, "pause StatusBarManager.DISABLE_NONE");
        }
        super.onPause();
        if (!mIsBootable && mModeDialogCancelable) {
            SLog.d(TAG, "no boot == finish() ");
            finish();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (null != mBroadcastReceiver) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    private Dialog mDialog;
    private ConnectivityManager mConnMgr;
    void showDataNetworkModePopupSKT() {
        SLog.i("showDataNetworkModePopupSKT");
        View view = View.inflate(this, R.layout.dialog_checkbox_common, null);
        mConnMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final TelephonyManager tm 
                = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        mDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.data_network_mode_dialog_title)
                .setView(view)
                .setInverseBackgroundForced(true)
                .setNegativeButton(R.string.unknown_sources_disable, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        tm.setDataEnabled(false);
                        mDialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.unknown_sources_enable, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        tm.setDataEnabled(true);
                        mDialog.dismiss();
                    }
                })
                .create();
        mDialog.setCancelable(mIsCancelable);
        mDialog.setCanceledOnTouchOutside(mIsCancelable);
        mDialog.show();
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    return DefineBlockKey.onKey(keyCode);
                }
                return false;
            }
        });
        TextView mMsgStr = (TextView)mDialog.findViewById(R.id.dialog_disable_popup_text);
        mMsgStr.setText(R.string.sp_data_network_popup_msg);
        makeCheckBoxInPopup ();
        if (mIsBootable) {
            WindowManager.LayoutParams attrs;
            attrs = mDialog.getWindow().getAttributes();
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_HOME_KEY;
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_APP_SWITCH_KEY;
            mDialog.getWindow().setAttributes(attrs);
        }
    }

    private void makeCheckBoxInPopup () {
        if (null == mDialog) {
            return;
        }
        mDataCheckbox = (CheckBox)mDialog.findViewById(R.id.do_not_show_check);
        mDataCheckbox.setText(R.string.data_network_mode_ask_at_boot);
        int mAskAt = android.provider.Settings.Secure.getInt(getContentResolver(),
                SettingsConstants.Secure.PREFERRED_DATA_NETWORK_MODE, 1);
        mDataCheckbox.setChecked(mAskAt == 1 ? true : false);
        SLog.i("mDataCheckbox = " + mAskAt);
        mDataCheckbox.setSoundEffectsEnabled(true);
        mDataCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SLog.i("onCheckedChanged :: mDataCheckbox = " + isChecked);
                android.provider.Settings.Secure.putInt(getContentResolver(),
                        SettingsConstants.Secure.PREFERRED_DATA_NETWORK_MODE, isChecked ? 1 : 0);
            }
        });
        mDataCheckbox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //onClick sound
            }
        });
    }

    private boolean mIsForg = false;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.lge.android.intent.action.BEFORE_KILL_APP_BY_FWK"
                .equals(intent.getAction())) {
                if (mIsCancelable || false == mIsForg) {
                    SLog.d("action : skip the Action for popup");
                    return;
                }
                SLog.i("action : BEFORE_KILL_APP_BY_FWK");
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

    private boolean shouldConsumeKey(int aKeyCode, KeyEvent aKeyEvent) {
        if ((aKeyCode == KeyEvent.KEYCODE_BACK || aKeyCode == KeyEvent.KEYCODE_SEARCH) &&
                mModeDialogCancelable == false && aKeyEvent.getAction() == KeyEvent.ACTION_DOWN) {
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

}
