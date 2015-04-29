package com.android.settings.lgesetting.wireless;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings;
import android.provider.Telephony;
import android.text.Html;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.ModelFeatureUtils;
import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.Utils;

import com.lge.constants.KeyExceptionConstants;
import com.lge.constants.SettingsConstants;
import com.lge.os.Build;

import org.apache.harmony.security.x509.InvalidityDate;

import android.widget.CheckBox;
import android.widget.CheckedTextView;

//this is a data roaming settings of 'Mobile network'
//request : KT only
public class DataRoamingSettingsKT extends Activity implements OnCheckedChangeListener {
    final static String LOG_TAG = "DataRoamingSettingsKT";

    private Phone mPhone;

    private boolean mIsOwner = true;
    private boolean mIsBootable = false;
    private StatusBarManager mStatusBar = null;
    private ConnectivityManager mConnMgr;
    private TelephonyManager mTM;
    private Switch mSwitch;

    private static final int MESSAGE_GET_PREFERRED_NETWORK_TYPE = 100;
    private static final int MESSAGE_SET_PREFERRED_NETWORK_TYPE = 200;
    private static final String PREFERRED_APN_URI = "content://telephony/carriers/preferapn";
    private static final Uri PREFERAPN_URI = Uri.parse(PREFERRED_APN_URI);
    private static final String APN_ID = "apn_id";
    private static final int ID_INDEX = 0;
    public static final String KT_MCCMNC = "45008";

    protected void setIsBootable() {
        mIsBootable = true;
        mIsCancelable = false;
    }

    public void setSwitchChecked(boolean checked) {
        mSwitch.setChecked(checked);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mIsOwner = UserHandle.myUserId() == UserHandle.USER_OWNER;
        if (false == mIsOwner) {
            Log.d("DataRoamingSettingsKT", "return under secondary user.");
            finish();
            return;
        }

        mPhone = PhoneFactory.getDefaultPhone();
        if (mIsBootable == false) {
            Intent intent = getIntent();
            //mIsBootable = intent.getBooleanExtra("IsBootable", false);
            mIsCancelable = intent.getBooleanExtra("IsCancelable", true); // must be
            mIsBootable = !mIsCancelable;
        }

        mConnMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        mTM = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        mStatusBar = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);

        if ("true".equals(
                ModelFeatureUtils.getFeature(getApplicationContext(), "add_kt_ui_ver12"))
            || (Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2)) {
            createDataRoamingDialogPopup_KTUIVer12();
        } else {
            createCustomViewDialogLTE();
        }
        if (mIsBootable) {
            // *** Block 'Home Key'.
            WindowManager.LayoutParams attrs;
            attrs = getWindow().getAttributes();
            // attrs.privateFlags |= KeyExceptionConstants.BYPASS_POWER_KEY;
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_HOME_KEY;
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_APP_SWITCH_KEY;
            getWindow().setAttributes(attrs);
        }

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.lge.android.intent.action.BEFORE_KILL_APP_BY_FWK");
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (false == mIsOwner) {
            return;
        }
        mIsForg = true;
        if (!mIsCancelable && mStatusBar != null) {
            mStatusBar.disable(StatusBarManager.DISABLE_EXPAND);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (false == mIsOwner) {
            return;
        }
        mIsForg = false;
        if (!mIsCancelable && mStatusBar != null) {
            mStatusBar.disable(StatusBarManager.DISABLE_NONE);
            Log.d(LOG_TAG, "pause StatusBarManager.DISABLE_NONE");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (false == mIsOwner) {
            return;
        }
        if (null != mBroadcastReceiver) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    private void setDataRoamingEnabled(boolean enabled) {
        Log.d(LOG_TAG, "setDataRoamingEnabled : " + enabled);
        mPhone.setDataRoamingEnabled(enabled);
    }

    private boolean getDataRoamingEnabled() {
        Log.d(LOG_TAG, "getDataRoamingEnabled : " + mPhone.getDataRoamingEnabled());
        return mPhone.getDataRoamingEnabled();
    }

    //======================================================================================
    AlertDialog mRoamingPopup;
    private Dialog mDialog;
    private CheckedTextView mRadioBlockChecked;
    private CheckedTextView mRadioAllowChecked;
    private TextView mDialogRoamingInfo;
    private boolean mIsCancelable = true;
    private boolean mIsSelected = false;
    private CheckedTextView mRadioLTEmodeChecked;
    private CheckedTextView mRadio3G2GmodeChecked;

    private void setRadioAllowChecked(boolean allow) {
        mRadioAllowChecked.setChecked(allow);
        mRadioBlockChecked.setChecked(!allow);
        setRoamingInfo(allow);
    }

    private void setRoamingInfo(boolean allow) {
        mDialogRoamingInfo.setText(Html.fromHtml(getString(
                allow ? R.string.data_roaming_allow_skt_info
                        : R.string.data_roaming_block_skt_info)));
    }

    private void createCustomViewDialogLTE() {
        View view = View.inflate(this, R.layout.wireless_data_network_roaming_lte, null);

        mDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.data_roaming_dialog_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setDataNetwrokType();
                    }
                })
                .setInverseBackgroundForced(true)
                .create();
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mIsBootable && !mIsSelected) {
                    setDataRoamingEnabled(false);
                }
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
        mDialog.setCanceledOnTouchOutside(mIsCancelable);
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

        mRadioLTEmodeChecked = (CheckedTextView)mDialog
                .findViewById(R.id.data_network_roaming_order_LTE_mode);
        mRadio3G2GmodeChecked = (CheckedTextView)mDialog
                .findViewById(R.id.data_network_roaming_3G_2G_mode);

        mRadioLTEmodeChecked.setText(R.string.sp_auto_select_SKT_NORMAL);
        mRadio3G2GmodeChecked.setText(R.string.sp_3g_select_SKT_NORMAL);

        mRadioLTEmodeChecked.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setLTEModeChecked(true);
            }
        });
        mRadio3G2GmodeChecked.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setLTEModeChecked(false);
            }
        });
        mDialogRoamingInfo = (TextView)mDialog.findViewById(R.id.data_network_roaming_text);
        mDialogRoamingInfo.setText(R.string.sp_roaming_warning_kt_NORMAL);
        mDialogRoamingInfo.setTextSize(19);

        mSwitch = (Switch)view.findViewById(R.id.roamingSwitchWidget);
        mSwitch.setOnCheckedChangeListener(this);

        int lteRoamingMode = Settings.Secure.getInt(getApplicationContext().getContentResolver(),
                "lte_roaming", 0);
        boolean isLteMode = Utils.isLteEnabled(getApplicationContext()) || (lteRoamingMode == 1);
        Log.d(LOG_TAG, "lteRoamingMode = " + lteRoamingMode);
        Log.d(LOG_TAG, "isLteMode = " + isLteMode);
        if (getDataRoamingEnabled()) {
            setSwitchChecked(true);
        }
        setLTEModeChecked(isLteMode);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar = (AsyncResult)msg.obj;
            switch (msg.what) {
            case MESSAGE_SET_PREFERRED_NETWORK_TYPE:
            {
                if (ar.exception == null) {
                    Log.d(LOG_TAG, "SetPreferredNetworkType is success");
                } else {
                    Log.d(LOG_TAG, "SetPreferredNetworkType is failed, exception=" + ar.exception);
                    mPhone.getPreferredNetworkType(mHandler
                            .obtainMessage(MESSAGE_GET_PREFERRED_NETWORK_TYPE));
                }
                break;
            }
            case MESSAGE_GET_PREFERRED_NETWORK_TYPE:
            {
                break;
            }
            default:
                break;
            }
        }
    };

    private void setDataNetwrokType() {
        boolean changeNetworkMode = false;

        if ((getMobileDataEnabled() == true)) {
            if (mSwitch.isChecked()) {
                if (mRadioLTEmodeChecked.isChecked()) {
                    setDB_LteRoaming(true);
                    changeNetworkMode = true;
                    // KT Requirement
                    Toast.makeText(this,
                            R.string.sp_select_lte_roaming_alert_msg_kt,
                            Toast.LENGTH_LONG).show();
                } else if (mRadio3G2GmodeChecked.isChecked()) {
                    setDB_LteRoaming(false);
                    changeNetworkMode = false;
                }
                setDataRoamingEnabled(true);
            } else {
                setDB_LteRoaming(false);
                setDataRoamingEnabled(false);
            }
            Log.d(LOG_TAG, "Mobile Data ON and change network mode : " + changeNetworkMode);
            changePreferrredNetworkMode(changeNetworkMode);
        } else {
            if (mSwitch.isChecked()) {
                if (mRadioLTEmodeChecked.isChecked()) {
                    setDB_LteRoaming(true);
                    changeNetworkMode = true;
                } else if (mRadio3G2GmodeChecked.isChecked()) {
                    setDB_LteRoaming(false);
                }
                setMobileDataEnabled(true);
                setDataRoamingEnabled(true);
            } else {
                setDB_LteRoaming(false);
                setDataRoamingEnabled(false);
            }
            Log.d(LOG_TAG, "Mobile Data OFF and change network mode : " + changeNetworkMode);
            changePreferrredNetworkMode(changeNetworkMode);
        }

        mDialog.dismiss();
        if (mIsBootable && !mIsSelected) {
            setDataRoamingEnabled(false);
        }
    }

    private void setLTEModeChecked(boolean allow) {
        Log.d(LOG_TAG, "setLTEModeChecked = " + allow);
        mRadioLTEmodeChecked.setChecked(allow);
        mRadio3G2GmodeChecked.setChecked(!allow);
    }

    private void setRadioGroupEnabled(boolean allow) {
        Log.d(LOG_TAG, "Radio enabled ? = " + allow);
        mRadioLTEmodeChecked.setEnabled(allow);
        mRadio3G2GmodeChecked.setEnabled(allow);

        mRadioLTEmodeChecked.refreshDrawableState();
        mRadio3G2GmodeChecked.refreshDrawableState();
        mRadioLTEmodeChecked.invalidate();
        mRadio3G2GmodeChecked.invalidate();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundbutton, boolean flag) {
        setSwitchChecked(flag);
        setRadioGroupEnabled(flag);
    }

    private boolean getMobileDataEnabled() {
        Log.d(LOG_TAG, "getMobileDataEnabled : " + mConnMgr.getMobileDataEnabled());
        return mConnMgr.getMobileDataEnabled();
    }

    private void setMobileDataEnabled(boolean flag) {
        Log.d(LOG_TAG, "setMobileDataEnabled : " + flag);
        mTM.setDataEnabled(flag);
    }

    private void setDB_LteRoaming(boolean lte_roaming) {
        Log.d(LOG_TAG, "[Set lte_roaming DB]lte_roaming = " + lte_roaming);
        Settings.Secure.putInt(getApplicationContext().getContentResolver(), "lte_roaming",
                lte_roaming ? 1 : 0);
    }

    public void changePreferrredNetworkMode(boolean enabled) {
        int NT_MODE_INVALID = -1;
        int newPreferMode = NT_MODE_INVALID;
        int curPreferMode = Settings.Global.getInt(getApplicationContext().getContentResolver(),
                Settings.Global.PREFERRED_NETWORK_MODE, 0);

        Log.d(LOG_TAG, "[changePreferrredNetworkMode] enabled = " + enabled);

        if (enabled) {
            switch (curPreferMode) {
            case Phone.NT_MODE_WCDMA_PREF:
            case Phone.NT_MODE_GSM_UMTS:
            case Phone.NT_MODE_LTE_GSM_WCDMA:
                newPreferMode = Phone.NT_MODE_LTE_GSM_WCDMA;
                break;
            case Phone.NT_MODE_WCDMA_ONLY:
                break;
            case Phone.NT_MODE_LTE_WCDMA:
                newPreferMode = Phone.NT_MODE_LTE_WCDMA;
                break;
            default:
                break;
            }
        }
        else {
            switch (curPreferMode) {
            case Phone.NT_MODE_WCDMA_PREF:
            case Phone.NT_MODE_GSM_UMTS:
            case Phone.NT_MODE_LTE_GSM_WCDMA:
                newPreferMode = Phone.NT_MODE_WCDMA_PREF;
                break;
            case Phone.NT_MODE_WCDMA_ONLY:
                break;
            case Phone.NT_MODE_LTE_WCDMA:
                newPreferMode = Phone.NT_MODE_WCDMA_ONLY;
                break;
            default:
                break;
            }
        }

        Log.d(LOG_TAG, "[changePreferrredNetworkMode] curPreferMode = " + curPreferMode);
        Log.d(LOG_TAG, "[changePreferrredNetworkMode] newPreferMode = " + newPreferMode);

        if ((newPreferMode != NT_MODE_INVALID) && (newPreferMode != curPreferMode)) {
            // kerry - SEND_DB_TO_MODEM TASK

            // mPhone.setPreferredNetworkType(newPreferMode, mHandler.obtainMessage(MESSAGE_SET_PREFERRED_NETWORK_TYPE));
            // Settings.Global.putInt(getApplicationContext().getContentResolver(), Settings.Global.PREFERRED_NETWORK_MODE, newPreferMode); // Network Mode Change
        }

    }
    private boolean mIsForg = false;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.lge.android.intent.action.BEFORE_KILL_APP_BY_FWK"
                .equals(intent.getAction())) {

                if (mIsCancelable || false == mIsForg) {
                    Log.d(LOG_TAG, "action : skip the Action for popup");
                    return;
                }
                Log.d(LOG_TAG, "action : BEFORE_KILL_APP_BY_FWK");
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

//=========================
    private CheckBox mAgree1CheckBox;
    private CheckBox mAgree2CheckBox;
    private Button mAgreeButton;
    private Button mDisagreeButton;

    private void createDataRoamingDialogPopup_KTUIVer12() {
        View view = View.inflate(this, R.layout.wireless_data_network_roaming_lte_kt, null);
        mDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.sp_data_roaming_kt_agree_title_NORMAL)
                .setView(view)
                .setInverseBackgroundForced(true)
                .create();
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent keyEvent) {
                if (!mIsCancelable && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    return DefineBlockKey.onKey(keyCode);
                }
                return false;
            }
        });
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mIsBootable && !mIsSelected) {
                    setDataRoamingEnabled(false);
                }
                finish();
            }
        });
        mDialog.setCancelable(mIsCancelable);
        mDialog.setCanceledOnTouchOutside(mIsCancelable);
        mDialog.show();
        createDataRoamingDialogPopup_KTUIVer12_sub();
    }

    private void createDataRoamingDialogPopup_KTUIVer12_sub() {
        if (mIsBootable) {
            WindowManager.LayoutParams attrs;
            attrs = mDialog.getWindow().getAttributes();
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_HOME_KEY;
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_APP_SWITCH_KEY;
            mDialog.getWindow().setAttributes(attrs);
        }
        mAgree1CheckBox = (CheckBox)mDialog.findViewById(R.id.data_network_roaming_agree1_checkbox);
        mAgree2CheckBox = (CheckBox)mDialog.findViewById(R.id.data_network_roaming_agree2_checkbox);
        mAgreeButton = (Button)mDialog.findViewById(R.id.agree_button);
        mDisagreeButton = (Button)mDialog.findViewById(R.id.disagree_button);
        mAgreeButton.setEnabled(false);
        mAgreeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsSelected = true;
                setDataRoamingEnabled(true);
                mDialog.dismiss();
            }
        });
        mDisagreeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setDataRoamingEnabled(false);
                Log.d(LOG_TAG, "mDisagreeButton roaming off");
                finish();
            }
        });
        mAgree1CheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mAgreeButton.setEnabled(getroamingInfoAgreeAll());
            }
        });
        mAgree2CheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAgreeButton.setEnabled(getroamingInfoAgreeAll());
            }
        });
    }

    private boolean getroamingInfoAgreeAll() {
        return ((mAgree1CheckBox.isChecked() == true) && (mAgree2CheckBox.isChecked() == true));
    }

//========================
}