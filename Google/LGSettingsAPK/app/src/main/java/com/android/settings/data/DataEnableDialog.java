package com.android.settings.data;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.util.Log;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.app.AlertActivity;
//import com.android.internal.telephony.DataConnectionManager;
import com.android.settings.R;
import com.android.internal.app.AlertController;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;
import com.android.settings.Utils;

public class DataEnableDialog extends AlertActivity implements DialogInterface.OnClickListener {
    private final String TAG = "DataEnableDialog";

    private int mClickedPos = -1; // 0: Data enabled, 1: Data disabled
    private TextView tViewDescriptionTitle;
    private TextView tViewDescription;
    private boolean mCancelable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "DataEnableDialog onCreate");

        final CharSequence[] items = new String[2];
        items[0] = getText(R.string.enable_text);
        items[1] = getText(R.string.disable_text);
        View view = getLayoutInflater().inflate(R.layout.data_enabled_popup, null);

        mClickedPos = getDataEnableDB(this);

        final AlertController.AlertParams p = mAlertParams;
        p.mTitle = getText(R.string.network_settings_title);
        p.mItems = items;
        p.mOnClickListener = mItemClickListener;
        p.mCheckedItem = mClickedPos;
        p.mIsSingleChoice = true;
        p.mView = view;
        p.mPositiveButtonText = getString(R.string.dlg_ok);
        p.mPositiveButtonListener = this;

        setupAlert();

        tViewDescription = (TextView)view.findViewById(R.id.data_enable_text);
        tViewDescriptionTitle = (TextView)view.findViewById(R.id.data_enable_description_title);

        // [S][2012.03.30][youngmin.jeon][Common][Settings][Common] Fix data enabled popup text mismatch problem.
        if (savedInstanceState != null) {
            mClickedPos = savedInstanceState.getInt("currentPosition");
        }
        // [E][2012.03.30][youngmin.jeon][Common][Settings][Common] Fix data enabled popup text mismatch problem.

        if (mClickedPos == 0) // Data enabled
        {
            tViewDescriptionTitle.setText(R.string.sp_data_enabled_NORMAL);
            tViewDescription.setText(R.string.sp_data_enabled_check_NORMAL);
        } else {
            tViewDescriptionTitle.setText(R.string.sp_data_disabled_NORMAL);
            tViewDescription.setText(R.string.sp_data_enabled_uncheck_NORMAL);
        }

        mCancelable = getIntent().getBooleanExtra("cancelable", true);
        if (!mCancelable) {
            setFinishOnTouchOutside(false);
        }
    }

    // for mOnClickListener
    private DialogInterface.OnClickListener mItemClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            // Save the position of most recently clicked item
            mClickedPos = which;
            if (mClickedPos == 0) // Data enabled
            {
                tViewDescriptionTitle.setText(R.string.sp_data_enabled_NORMAL);
                tViewDescription.setText(R.string.sp_data_enabled_check_NORMAL);
            } else {
                tViewDescriptionTitle.setText(R.string.sp_data_disabled_NORMAL);
                tViewDescription.setText(R.string.sp_data_enabled_uncheck_NORMAL);
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (mCancelable) {
            super.onBackPressed();
        }
    }

    // for mPositiveButtonListener
    public void onClick(DialogInterface dialog, int which) {
        Log.d(TAG, "setPositiveButton select=" + mClickedPos);
        setDataEnableDB(this, mClickedPos);
        setResult(RESULT_OK);
        finish();
    }

    private int getDataEnableDB(Context context)
    {
        int clickedPos = 0;
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        // Fix white box test error
        if (cm != null) {
            if (cm.getMobileDataEnabled() == true) {
                clickedPos = 0;
            } else {
                clickedPos = 1;
            }
        }

        Log.e(TAG, "getDataEnableDB(): Type(0:Enable,1:Disable): " + clickedPos);

        return clickedPos;
    }

    private void setDataEnableDB(Context context, int clickedPos) {
        Log.d(TAG, "setDataEnableDB(): mClickedPos: " + mClickedPos);
	TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        // Fix white box test error
        if (tm != null) {
            if (clickedPos == 0) { // Data enabled
                tm.setDataEnabled(true);
                android.provider.Settings.Secure.putInt(context.getContentResolver(),
                        SettingsConstants.Secure.PREFERRED_DATA_NETWORK_MODE, 1);
                sendBroadcastDdsForMMS(true);
            } else {
                tm.setDataEnabled(false);
                android.provider.Settings.Secure.putInt(context.getContentResolver(),
                        SettingsConstants.Secure.PREFERRED_DATA_NETWORK_MODE, 0);
                if ("KDDI".equals(Config.getOperator()) && Utils.isSupportedVolte(context)) {
                    Intent intent = new Intent();
                    intent.putExtra("is_roaming_onoff", false);
                    intent.putExtra("change_networkmode", -1);
                    intent.setAction("com.lge.settings.action.CHANGE_ROAMING_MODE");
                    context.sendBroadcast(intent);
                }                
                sendBroadcastDdsForMMS(false);
            }
        }
    }

    // [S][2012.03.30][youngmin.jeon][Common][Settings][Common] Fix data enabled popup text mismatch problem.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "onSaveInstanceState(): mClickedPos: " + mClickedPos);
        outState.putInt("currentPosition", mClickedPos);
    }
    // [E][2012.03.30][youngmin.jeon][Common][Settings][Common] Fix data enabled popup text mismatch problem.

    private void sendBroadcastDdsForMMS (boolean mValue) {
        Intent intent = new Intent();
        intent.setAction("com.lge.mms.intent.action.switch_dds");
        intent.putExtra("data_status", mValue);
        sendBroadcast(intent);
        Log.d(TAG, "sendBroadcastDdsForMMS mValue = " + mValue);
    }

}
