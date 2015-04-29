package com.android.settings.lgesetting.wireless;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Build;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.ModelFeatureUtils;
import com.android.settings.R;
import com.lge.constants.KeyExceptionConstants;

import org.apache.harmony.security.x509.InvalidityDate;

//this is a data roaming settings of 'Mobile network'
//request : SKT only
public class DataRoamingSettingsSKT extends Activity implements OnCheckedChangeListener {
    final static String LOG_TAG = "DataRoamingSettingsSKT";

    private Phone mPhone;

    private boolean mIsOwner = true;
    private boolean mIsBootable = false;
    private StatusBarManager mStatusBar = null;
    private Switch mSwitch;
    private static final int MESSAGE_SET_PREFERRED_NETWORK_TYPE_LTE_GSM_WCDMA = 100;
    private static final int MESSAGE_SET_PREFERRED_NETWORK_TYPE_WCDMA_PREF = 200;
    private static final int LIST_LTE_GSM_WCDMA = 0;
    private static final int LIST_WCDMA_PREF = 1;
    private static final String PREFERRED_APN_URI = "content://telephony/carriers/preferapn";
    private static final Uri PREFERAPN_URI = Uri.parse(PREFERRED_APN_URI);
    private static final String APN_ID = "apn_id";
    private static final int ID_INDEX = 0;
    public static final String SKT_MCCMNC = "45005";

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
            Log.d("ChangeNetworkPreferredModeReceiver", "return under secondary user.");
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
        mStatusBar = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);

        createNewCustomViewDialogLTE();

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.lge.android.intent.action.BEFORE_KILL_APP_BY_FWK");
        registerReceiver(mBroadcastReceiver, intentFilter);
        Log.d(LOG_TAG, "********      oncreate 4.2 new");
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
        if (false == mIsOwner) {
            return;
        }
        mIsForg = true;
        if (!mIsCancelable && mStatusBar != null) {
            Log.d(LOG_TAG, "********      onresume DISABLE_EXPAND");
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
            Log.d(LOG_TAG, "********      onPause");
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
        return mPhone.getDataRoamingEnabled();
    }

    //======================================================================================
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

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar = (AsyncResult)msg.obj;
            if (ar.exception == null) {
                switch (msg.what) {
                case MESSAGE_SET_PREFERRED_NETWORK_TYPE_LTE_GSM_WCDMA:
                    setDB_NetwrokMode(getApplicationContext().getContentResolver(),
                            mPhone.NT_MODE_LTE_GSM_WCDMA); //update DB to GWL or GW
                    break;

                case MESSAGE_SET_PREFERRED_NETWORK_TYPE_WCDMA_PREF:
                    setDB_NetwrokMode(getApplicationContext().getContentResolver(),
                            mPhone.NT_MODE_WCDMA_PREF); //update DB to GWL or GW
                    break;
                default:
                    break;
                }
            }
        }
    };

    private void setDataNetwrokType() {
        int mode = -1;

        if (mSwitch.isChecked()) {
            mIsSelected = true;

            if (mRadioLTEmodeChecked.isChecked()) {
                mode = 9; // mPhone.NT_MODE_LTE_GSM_WCDMA;
                
                // kerry - SEND_DB_TO_MODEM TASK
                // mPhone.setPreferredNetworkType(mode, mHandler.obtainMessage(MESSAGE_SET_PREFERRED_NETWORK_TYPE_LTE_GSM_WCDMA)); // Change network mode to GWL auto or GW
            } else if (mRadio3G2GmodeChecked.isChecked()) {
                mode = mPhone.NT_MODE_WCDMA_PREF;
                
                // kerry - SEND_DB_TO_MODEM TASK
                // mPhone.setPreferredNetworkType(mode, mHandler.obtainMessage(MESSAGE_SET_PREFERRED_NETWORK_TYPE_WCDMA_PREF)); // Change network mode to GWL auto or GW
            }
            
            Log.d(LOG_TAG, "Switch Button checked! *NetworkMode = " + mode);
            setDataRoamingEnabled(true);
        }
        else {
            Log.d(LOG_TAG, "Switch Button unchecked! *NetworkMode = NT_MODE_WCDMA_PREF");
            
            // kerry - SEND_DB_TO_MODEM TASK
            // mPhone.setPreferredNetworkType(mPhone.NT_MODE_WCDMA_PREF, mHandler.obtainMessage(MESSAGE_SET_PREFERRED_NETWORK_TYPE_WCDMA_PREF)); // Change network mode to GW
            setDataRoamingEnabled(false);
        }

        mDialog.dismiss();
        
        if (mIsBootable && !mIsSelected) {
            setDataRoamingEnabled(false);
        }
    }

    private void setLTEModeChecked(boolean allow) {
        mRadioLTEmodeChecked.setChecked(allow);
        mRadio3G2GmodeChecked.setChecked(!allow);
        setRoamingInfoLTE(allow);
    }

    private void setRoamingInfoLTE(boolean allow) {
        mDialogRoamingInfo.setText(Html.fromHtml(getString(
                allow ? R.string.data_roaming_allow_skt_lte_info
                        : R.string.data_roaming_block_skt_lte_info)));
    }

    private void setRadioGroupEnabled(boolean allow) {
        Log.d("ksson", "Radio enabled ? = " + allow);
        mRadioLTEmodeChecked.setEnabled(allow);
        mRadio3G2GmodeChecked.setEnabled(allow);

        mRadioLTEmodeChecked.refreshDrawableState();
        mRadio3G2GmodeChecked.refreshDrawableState();
        mRadioLTEmodeChecked.invalidate();
        mRadio3G2GmodeChecked.invalidate();
    }

    public int getNetworkMode() {
        int mode = LIST_WCDMA_PREF;
        int networkMode = getDB_NetwrokMode(getApplicationContext().getContentResolver());
        Log.d(LOG_TAG, "getDB_NetwrokMode : " + networkMode);

        switch (networkMode) {
        case RILConstants.NETWORK_MODE_LTE_GSM_WCDMA:
            mode = LIST_LTE_GSM_WCDMA;
            break;

        case RILConstants.NETWORK_MODE_WCDMA_PREF:
            mode = LIST_WCDMA_PREF;
            break;
        default:
            break;
        }
        return mode;
    }

    private int getDB_NetwrokMode(ContentResolver cr) {
        int networkMode = Global.getInt(cr,
                android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                RILConstants.PREFERRED_NETWORK_MODE);
        return networkMode;
    }

    private void setDB_NetwrokMode(ContentResolver cr, int networkMode) {
        Log.d(LOG_TAG, "setDB_NetwrokMode = " + networkMode);
        Global.putInt(cr, Global.PREFERRED_NETWORK_MODE, networkMode);
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        String apnKey = getSelectedApnKey(networkMode);
        values.put(APN_ID, apnKey);
        resolver.update(PREFERAPN_URI, values, null, null);
    }

    private String getSelectedApnKey(int networkMode) {
        String key = null;
        String where = null;

        where = setSKTSBSMAPNSkipDisplayFilter(SKT_MCCMNC, networkMode);

        Cursor cursor = getContentResolver().query(Telephony.Carriers.CONTENT_URI,
                new String[] { "_id" },
                where, null, Telephony.Carriers.DEFAULT_SORT_ORDER);

        if (cursor != null) { // 120103 WBT
            try {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    key = cursor.getString(ID_INDEX);
                }
            } catch (Exception e) {
                // TODO: handle exception
                Log.e("DataRoamingSettingsSKTLTE", " An error occurred  on getSelectedApnKey : "
                        + e.getMessage());
            } finally {
                cursor.close();
            }
        }
        Log.d(LOG_TAG, "getSelectedApnKey = " + key);

        return key;
    }

    private String setSKTSBSMAPNSkipDisplayFilter(String strOperator, int networkMode) {
        String strAttach = null;

        switch (networkMode) {
        case 9:
            strAttach = " apn ==\"" + "lte-roaming.sktelecom.com" + "\"";
            break;
        case 0:
            strAttach = " apn ==\"" + "roaming.sktelecom.com" + "\"";
            break;
        default:
            break;
        }

        return strAttach;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundbutton, boolean flag) {
        setSwitchChecked(flag);
        setRadioGroupEnabled(flag);
    }

    //kiseok.son 20130124 SKT IRR2.4 - start
    private TextView mDialogRoamingAgree2Info;
    private TextView mDialogRoamingDescriptionInfo;
    private CheckBox mAgree1CheckBox;
    private CheckBox mAgree2CheckBox;
    private Button mAgreeButton;
    private Button mDisagreeButton;

    private void createNewCustomViewDialogLTE() {
        View view = View.inflate(this, R.layout.wireless_data_network_roaming_lte_new, null);

        mDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.sp_data_roaming_skt_agree_title_NORMAL)
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

        if (mIsBootable) {
            // *** Block 'Home Key'.
            WindowManager.LayoutParams attrs;
            attrs = mDialog.getWindow().getAttributes();
            // attrs.privateFlags |= KeyExceptionConstants.BYPASS_POWER_KEY;
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_HOME_KEY;
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_APP_SWITCH_KEY;
            //getWindow().setAttributes(attrs);
            mDialog.getWindow().setAttributes(attrs);
        }

        mAgree1CheckBox = (CheckBox)mDialog.findViewById(R.id.data_network_roaming_agree1_checkbox);
        mDialogRoamingAgree2Info = (TextView)mDialog
                .findViewById(R.id.data_network_roaming_agree2_text);
        mDialogRoamingAgree2Info.setText(Html
                .fromHtml(getString(R.string.sp_data_roaming_skt_info_agree2_NORMAL)));
        mAgree2CheckBox = (CheckBox)mDialog.findViewById(R.id.data_network_roaming_agree2_checkbox);
        mAgreeButton = (Button)mDialog.findViewById(R.id.agree_button);
        mDisagreeButton = (Button)mDialog.findViewById(R.id.disagree_button);
        mDialogRoamingDescriptionInfo = (TextView)mDialog
                .findViewById(R.id.data_network_roaming_agree3_text);
        if ("true".equals(
                ModelFeatureUtils.getFeature(getApplicationContext(), "actionbardatausagepadding"))) {
            mDialogRoamingDescriptionInfo.setVisibility(View.GONE);
        }

        mAgreeButton.setEnabled(false);
        mAgreeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mIsSelected = true;
                setDataRoamingEnabled(true);
                mDialog.dismiss();
            }
        });
        mDisagreeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                int settingNetworkMode = getDB_NetwrokMode(getApplicationContext().getContentResolver());
                if ((settingNetworkMode == RILConstants.NETWORK_MODE_LTE_ONLY) ||
                        (settingNetworkMode == RILConstants.NETWORK_MODE_WCDMA_ONLY) ||
                        (settingNetworkMode == RILConstants.NETWORK_MODE_GSM_ONLY) ||
                        (settingNetworkMode == RILConstants.NETWORK_MODE_WCDMA_PREF)) {
                    Log.d(LOG_TAG, "Do not Changed settingNetworkMode : " + settingNetworkMode);
                    setDataRoamingEnabled(false);
                } else {
                    if (Config.isDataRoaming()) {
                        // kerry - SEND_DB_TO_MODEM TASK
                        // mPhone.setPreferredNetworkType(mPhone.NT_MODE_WCDMA_PREF, mHandler.obtainMessage(MESSAGE_SET_PREFERRED_NETWORK_TYPE_WCDMA_PREF));
                        Log.d(LOG_TAG, "GW settingNetworkMode : " + settingNetworkMode);
                    }
                    setDataRoamingEnabled(false);
                }
                finish();
            }
        });

        mAgree1CheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (getroamingInfoAgreeAll()) {
                    mAgreeButton.setEnabled(true);
                } else {
                    mAgreeButton.setEnabled(false);
                }
            }
        });
        mAgree2CheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getroamingInfoAgreeAll()) {
                    mAgreeButton.setEnabled(true);
                } else {
                    mAgreeButton.setEnabled(false);
                }
            }
        });
    }

    private boolean getroamingInfoAgreeAll() {
        return ((mAgree1CheckBox.isChecked() == true) && (mAgree2CheckBox.isChecked() == true));
    }
    //kiseok.son 20130124 SKT IRR2.4 - end
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
}