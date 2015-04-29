package com.android.settings.data;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.app.AlertActivity;
import com.android.settings.R;
import com.android.internal.app.AlertController;
import com.lge.constants.SettingsConstants;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

public class DataWarningDialog extends AlertActivity implements DialogInterface.OnClickListener, OnClickListener {
    private final String TAG = "DataWarningDialog";

    private TextView tViewDescription;
    //private boolean mCancelable = true;
    private CheckBox mDoNotShow;
    private boolean mCheck = false;

    @Override
    public void onClick(View arg0) {
        
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "DataEnableDialog onCreate");

        View view = getLayoutInflater().inflate(R.layout.dialog_checkbox_tcl, null);

        final AlertController.AlertParams p = mAlertParams;
        if ("VZW".equals(Config.getOperator())) {
            if (Utils.getVoipEnabledForVoLTE(getApplicationContext())) {
                p.mTitle = getText(R.string.data_usage_enable_mobile);
            } else {
                p.mTitle = getText(R.string.sp_data_pay_popup_title_VZW_NORMAL);
            }
        } else {
            p.mTitle = getText(R.string.data_warning_notification_title);
        }

        p.mView = view;
        p.mPositiveButtonText = getString(R.string.dlg_ok);
        p.mPositiveButtonListener = this;
        mDoNotShow = (CheckBox)view.findViewById(R.id.do_not_show_check);
        mDoNotShow.setOnClickListener(this);

        if ("VZW".equals(Config.getOperator())) {
            mDoNotShow.setVisibility(View.GONE);
        } else {
            mCheck = (android.provider.Settings.Secure.getInt(
                    getContentResolver(),
                    SettingsConstants.Secure.DO_NOT_SHOW_AGAIN_TCL_WARN,
                    0) == 1) ? true : false;
            mDoNotShow.setChecked(mCheck);
            mDoNotShow.setOnCheckedChangeListener(mCheckboxListener);
        }

        setupAlert();

        tViewDescription = (TextView)view.findViewById(R.id.vzw_disable_popup_text);
        if ("VZW".equals(Config.getOperator())) {
            if (Utils.getVoipEnabledForVoLTE(getApplicationContext())) {
                tViewDescription.setText(R.string.sp_data_pay_popup_volte_VZW_NORMAL);
            } else {
                tViewDescription.setText(R.string.sp_data_pay_popup_VZW_NORMAL);
            }
        } else {
            tViewDescription.setText(R.string.sp_data_consume_warn_TCL);
        }

        //making  text href clickable
        tViewDescription.setMovementMethod(LinkMovementMethod.getInstance());

        if (savedInstanceState != null && "VZW".equals(Config.getOperator()) == false) {
            mCheck = savedInstanceState.getBoolean("currentValue");
        }

        // mCancelable = getIntent().getBooleanExtra("cancelable", true);
        //if (!mCancelable) {
        if ("VZW".equals(Config.getOperator()) == false) {
            setFinishOnTouchOutside(false);
        }
        //}
    }

    private OnCheckedChangeListener mCheckboxListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            // TODO Auto-generated method stub
            mCheck = arg1;
        }
    };

    // for mPositiveButtonListener
    public void onClick(DialogInterface dialog, int which) {
        Log.d(TAG, "setPositiveButton select=" + mCheck);
        if ("TCL".equals(Config.getOperator())
                || SystemProperties.getBoolean("persist.sys.cust.data_warning", false)) {
            setCheckboxDB(this, mCheck);
        }
        setResult(RESULT_OK);
        finish();
    }

    private void setCheckboxDB(Context context, boolean checked) {
        Log.d(TAG, "setDataEnableDB(): mClickedPos: " + mCheck);
        if (mCheck) { // Data enabled
            android.provider.Settings.Secure.putInt(
                    context.getContentResolver(),
                    SettingsConstants.Secure.DO_NOT_SHOW_AGAIN_TCL_WARN,
                    1);
        } else {
            android.provider.Settings.Secure.putInt(
                    context.getContentResolver(),
                    SettingsConstants.Secure.DO_NOT_SHOW_AGAIN_TCL_WARN,
                    0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "onSaveInstanceState(): mCheck: " + mCheck);
        outState.putBoolean("currentValue", mCheck);
    }

}
