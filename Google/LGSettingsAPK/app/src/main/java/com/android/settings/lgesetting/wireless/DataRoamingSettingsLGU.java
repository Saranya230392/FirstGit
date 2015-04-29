package com.android.settings.lgesetting.wireless;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.StatusBarManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Telephony;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManagerEx;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckedTextView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.net.ConnectivityManager;
import android.net.Uri;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.TelephonyProperties;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Switch;
import android.view.ViewGroup;
import com.lge.constants.SettingsConstants;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.Uri;
import android.widget.Toast;
import com.android.settings.R;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import java.io.File;
import java.util.List;
import android.provider.Settings.Global;
import android.widget.CheckBox;
import android.graphics.Color;
import android.content.BroadcastReceiver;
import android.provider.Settings;
import com.lge.constants.KeyExceptionConstants;
import com.lge.constants.SettingsConstants;
import android.content.IntentFilter;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.Utils;

//this is a data roaming settings of 'Mobile network'
//request : LGU only
public class DataRoamingSettingsLGU extends Activity implements OnCheckedChangeListener {
    final static String LOG_TAG = "DataRoamingSettingsLGU";

    private boolean mIsBootable = false;
    private StatusBarManager mStatusBar = null;
    private ConnectivityManager mConnMgr;
    private Switch mRoamingSwitdh;
    private CheckBox mBackgroundSwitdh;
    private NetworkPolicyManager mPolicyManager;
    public static final String ROAMING_POPUP_CLOSE = "com.lge.settings.ROAMING_POPUP_CLOSE";

    protected void setIsBootable() {
        mIsBootable = true;
        mIsCancelable = false;
    }

    //LGE_UPDATE_S jake.choi 2012-02-20 [325/Cayman] Remove roaming data popup remaining until coming back home
    private boolean mIsForg = false;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.COME_BACK_HOME".equals(intent.getAction())) {
                // Log.d(TAG, "Received android.intent.action.COME_BACK_HOME");
                if (mDialog != null) {
                    mDialog.dismiss();
                    //LGE_UPDATE_S heekyung.kang 2012-03-02 [325/Cayman] Remove roaming data popup remaining until coming back home
                    Settings.Secure.putInt(getApplicationContext().getContentResolver(),
                            SettingsConstants.Secure.DATA_NETWORK_WAIT_FOR_PAYPOPUP_RESPONSE, 0);
                    //LGE_UPDATE_E heekyung.kang 2012-03-02 [325/Cayman] Remove roaming data popup remaining until coming back home
                }

                finish();
            }
            if ("com.lge.android.intent.action.BEFORE_KILL_APP_BY_FWK"
                .equals(intent.getAction())) {
                if (false == mIsForg) {
                    Log.d(LOG_TAG, "action : skip the Action for popup");
                    return;
                }
                Log.i(LOG_TAG, "action BEFORE_KILL_APP_BY_FWK");
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
    //LGE_UPDATE_E jake.choi 2012-02-20 [325/Cayman] Remove roaming data popup remaining until coming back home

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mPolicyManager = NetworkPolicyManager.from(DataRoamingSettingsLGU.this);

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.COME_BACK_HOME");
        intentFilter.addAction("com.lge.android.intent.action.BEFORE_KILL_APP_BY_FWK");
        registerReceiver(mBroadcastReceiver, intentFilter);

        mConnMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        mStatusBar = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);

        createCustomViewDialogLTE();
        if (getDataRoamingEnabled()) {
            final Resources res = getResources();
            mRoamingSwitdh.setChecked(true);
            mBackgroundSwitdh.setEnabled(true);
            mBackgroundSwitdh.setFocusable(true);
            mBackgroundSwitdh.setChecked(Settings.Secure.getInt(getApplicationContext()
                    .getContentResolver(),
                    SettingsConstants.Secure.DATA_NETWORK_USER_BACKGROUND_SETTING_DATA, 1) == 1);
            mBackgroundDataText.setTextColor(res.getColor(R.color.graph_text_color));
        } else {
            mBackgroundSwitdh.setEnabled(false);
            mBackgroundSwitdh.setChecked(false);
            mBackgroundSwitdh.setFocusable(false);
            mBackgroundDataText.setTextColor(Color.GRAY);
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-236]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            com.android.settings.MDMSettingsAdapter.getInstance()
                    .checkEnforceBackgroundDataRestrictedPolicy(null, mBackgroundSwitdh);
        }
        // LGMDM_END

        Log.d(LOG_TAG, "mBackgroundSwitdh.isChecked = " + mBackgroundSwitdh.isChecked());

        if (mIsBootable)
        {
            // *** Block 'Home Key'.
            WindowManager.LayoutParams attrs;
            attrs = getWindow().getAttributes();
            // attrs.privateFlags |= KeyExceptionConstants.BYPASS_POWER_KEY;
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_HOME_KEY;
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_APP_SWITCH_KEY;
            getWindow().setAttributes(attrs);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsForg = true;
        if (mStatusBar != null) {
            mStatusBar.disable(StatusBarManager.DISABLE_EXPAND);
            Log.d(LOG_TAG, "resume StatusBarManager.DISABLE_EXPAND");
        }
    }

    @Override
    protected void onPause() {
        mIsForg = false;
        if (mStatusBar != null) {
            mStatusBar.disable(StatusBarManager.DISABLE_NONE);
            Log.d(LOG_TAG, "pause StatusBarManager.DISABLE_NONE");
        }
        super.onPause();
    }

    private void setDataRoamingEnabled(boolean enabled) {
        Log.d(LOG_TAG, "setDataRoamingEnabled : " + enabled);
        setDataRoaming(enabled);
    }

    private boolean getDataRoamingEnabled() {
        return getDataRoaming();
    }

    private boolean getDataRoaming() {
        final ContentResolver resolver = getApplicationContext().getContentResolver();
        String mDataRoaming
            = Settings.Global.DATA_ROAMING
                + OverlayUtils.getDefaultPhoneID(getApplicationContext());
        Log.d(LOG_TAG, "getDataRoaming : " + mDataRoaming);
        Log.d(LOG_TAG, "getDataRoaming : " + Settings.Global.getInt(resolver, mDataRoaming, 0));
        return Settings.Global.getInt(resolver, mDataRoaming, 0) != 0;
    }

    private void setDataRoaming(boolean enabled) {
        Intent intent = new Intent();
        intent.putExtra("is_roaming_onoff", enabled);
        intent.putExtra("change_networkmode", -1);
        intent.setAction("com.lge.settings.action.CHANGE_ROAMING_MODE");
        getApplicationContext().sendBroadcast(intent);
        TelephonyManagerEx mTelephonyMgrEx 
            = (TelephonyManagerEx)getApplicationContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyMgrEx.setRoamingDataEnabled_RILCMD(enabled);
    }

    //======================================================================================
    private AlertDialog.Builder mPopupBuilder;
    private Dialog mDialog;
    private TextView mDialogRoamingInfo;
    private boolean mIsCancelable = true;
    private TextView mBackgroundDataText;

    private void createCustomViewDialogLTE() {
        View view = View.inflate(this, R.layout.wireless_data_network_roaming_lte_lgu, null);
        mPopupBuilder = new AlertDialog.Builder(this)
                .setTitle(R.string.dataroaming)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setDataNetwrokType(true);
                        Intent intent = new Intent(ROAMING_POPUP_CLOSE);
                        intent.putExtra("data_roaming_value",
                                mRoamingSwitdh.isChecked() ? 1 : 0);
                        sendBroadcast(intent);
                    }
                }).setInverseBackgroundForced(true);
        if (false == Utils.isUI_4_1_model(this)) {
            mPopupBuilder.setIconAttribute(android.R.attr.alertDialogIcon);
        }
        mDialog = mPopupBuilder.create();
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                Log.i(LOG_TAG, "CHRISWON - DISMISSED!");
                finish();
            }
        });
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent keyEvent) {
                if (!mIsCancelable && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    return DefineBlockKey.onKey(keyCode);
                }
                return false;
            }
        });
        mDialog.setCancelable(mIsCancelable);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        if (mIsBootable)
        {
            // *** Block 'Home Key'.
            WindowManager.LayoutParams attrs;
            attrs = mDialog.getWindow().getAttributes();
            // attrs.privateFlags |= KeyExceptionConstants.BYPASS_POWER_KEY;
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_HOME_KEY;
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_APP_SWITCH_KEY;
            mDialog.getWindow().setAttributes(attrs);
        }

        mRoamingSwitdh = (Switch)mDialog.findViewById(R.id.roamingSwitchWidget);
        mRoamingSwitdh.setOnCheckedChangeListener(this);

        mRoamingSwitdh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            }
        });

        mBackgroundSwitdh = (CheckBox)mDialog.findViewById(R.id.id_BackgroundDataSwitchWidget);

        //onClick sound
        mBackgroundSwitdh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-236]
                if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                    com.android.settings.MDMSettingsAdapter.getInstance()
                            .checkEnforceBackgroundDataRestrictedPolicy(null, mBackgroundSwitdh);
                    if (com.android.settings.MDMSettingsAdapter.getInstance()
                            .isShowEnforceBackgroundDataRestrictedToastIfNeed(
                                    getApplicationContext())) {
                        return;
                    }
                }
                // LGMDM_END
            }
        });

        mBackgroundSwitdh.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
            }
        });
        mDialogRoamingInfo = (TextView)mDialog.findViewById(R.id.data_network_roaming_text);
        mBackgroundDataText = (TextView)mDialog.findViewById(R.id.id_background_data);

        mDialogRoamingInfo.setText(Html
                .fromHtml(getString(R.string.sp_data_roaming_lgu_popup_body)));

    }

    private void setDataNetwrokType(boolean isDispToastMsg) {
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-164]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && (com.android.settings.MDMSettingsAdapter.getInstance()
                        .isShowDataUsageRoamingToastIfNeed(getApplicationContext()))) {
            return;
        }
        // LGMDM_END
        boolean isRoaming = mRoamingSwitdh.isChecked();
        boolean isBackground = mBackgroundSwitdh.isChecked();
        changePreferrredNetworkMode(isRoaming);
        if (isRoaming) {
            setDataRoamingEnabled(true);
            setRestrictBackground(isBackground);
            if (isDispToastMsg) {
                Toast.makeText(this, R.string.sp_data_roaming_on_toast_msg,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            setDataRoamingEnabled(false);
            setRestrictBackground(isBackground);
            if (isDispToastMsg) {
                Toast.makeText(this, R.string.sp_data_roaming_off_toast_msg,
                        Toast.LENGTH_SHORT).show();
            }
        }
        mDialog.dismiss();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundbutton, boolean flag) {
        //check switch
        mRoamingSwitdh.setChecked(flag);

        if (flag) {
            final Resources res = getResources();
            mBackgroundSwitdh.setEnabled(true);
            mBackgroundSwitdh.setChecked(true);
            mBackgroundSwitdh.setFocusable(true);
            mBackgroundDataText.setTextColor(res.getColor(R.color.graph_text_color));
        } else {
            mBackgroundSwitdh.setEnabled(false);
            mBackgroundSwitdh.setChecked(false);
            mBackgroundSwitdh.setFocusable(false);
            mBackgroundDataText.setTextColor(Color.GRAY);
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-236]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            com.android.settings.MDMSettingsAdapter.getInstance()
                    .checkEnforceBackgroundDataRestrictedPolicy(null, mBackgroundSwitdh);
        }
        // LGMDM_END
    }

    public void setRestrictBackground(boolean restrictBackground) {

        Settings.Secure.putInt(getApplicationContext().getContentResolver(),
                SettingsConstants.Secure.DATA_NETWORK_USER_BACKGROUND_SETTING_DATA,
                restrictBackground ? 1 : 0);

        mPolicyManager.setRestrictBackground(restrictBackground);

    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Log.d(LOG_TAG, "onUserLeaveHint, press the home, and close popup.");
        //finish();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        if (mDialog != null) {
            mDialog.dismiss();
        }
        super.onDestroy();
    }

    private boolean getDataLteRoaming() {
        final ContentResolver resolver = getApplicationContext().getContentResolver();
        return Settings.Secure.getInt(resolver, "data_lte_roaming", 0) != 0;
    }

    private int getDB_NetwrokMode() {
        int intDB = Settings.Global.getInt(getContentResolver(),
                        Settings.Global.PREFERRED_NETWORK_MODE,
                        Phone.NT_MODE_LTE_GSM_WCDMA);
        return intDB;
    }

    public void changePreferrredNetworkMode(boolean enabled) {
        boolean mIsIntelChipset = 
            "imc".equals(SystemProperties.get("ro.build.target_ril_platform", "common"));
        if (mIsIntelChipset) {
            boolean mIsHidden = isSelectNetworkModeInHiddenMenu();
            int mCurrNetworkMode = getDB_NetwrokMode();
            boolean mDataLteRoaming = getDataLteRoaming();            
            Log.d(LOG_TAG, "changePreferrredNetworkMode mDataLteRoaming = " + mDataLteRoaming);
            int newPreferMode = 
                    mDataLteRoaming ? Phone.NT_MODE_LTE_GSM_WCDMA : Phone.NT_MODE_WCDMA_PREF;
            Log.d(LOG_TAG, "[changePreferrredNetworkMode] newPreferMode = " + newPreferMode);
            newPreferMode = enabled ? newPreferMode : Phone.NT_MODE_WCDMA_PREF;
            if ((mCurrNetworkMode == Phone.NT_MODE_GSM_ONLY
                    || mCurrNetworkMode == Phone.NT_MODE_WCDMA_ONLY) && mIsHidden) {
                Log.d(LOG_TAG,
                    "[changePreferrredNetworkMode] Do not change network mode = "
                    + mCurrNetworkMode);
                return;
            }
            Log.d(LOG_TAG, "[changePreferrredNetworkMode] newPreferMode = " + newPreferMode);
            Settings.Global.putInt(getContentResolver(),
                    Settings.Global.PREFERRED_NETWORK_MODE,
                    newPreferMode);
        }
    }

    public boolean isSelectNetworkModeInHiddenMenu() {
        int mValue = SystemProperties.getInt("persist.radio.forced_net_type", 0);
        Log.d(LOG_TAG,
            "[isSelectNetworkModeInHiddenMenu] = "
            + mValue);
        return (mValue == 1);
    }

}
