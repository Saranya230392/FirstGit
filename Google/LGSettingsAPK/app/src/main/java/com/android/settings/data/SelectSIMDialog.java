//package com.android.systemui.data;
package com.android.settings.data;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.android.systemui.R;
import com.android.internal.app.AlertActivity;
import com.android.settings.R;
import com.android.internal.app.AlertController;
import com.lge.constants.SettingsConstants;
import com.android.settings.Utils;
import android.provider.Settings;

public class SelectSIMDialog extends AlertActivity implements DialogInterface.OnClickListener {
    private final String TAG = "SelectSIMDialog";

    private final String SIM_TYPE_CHANGED = "android.intent.action.SIM_TYPE_CHANGED";

    private int mClickedPos = -1;
    private boolean mCancelable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // [S][2012.03.08][seungeene][LGP700][NA] add VDFUK & SIM condition
        if (!Utils.isVodafoneUKSIM(this)) {
            finish();
            return;
        }
        // [E][2012.03.08][seungeene][LGP700][NA] add VDFUK & SIM condition

        LinearLayout alertLayout = (LinearLayout)View.inflate(this, R.layout.data_dialog, null);
        TextView alertextView = (TextView)alertLayout.findViewById(R.id.AlertTextView);

        final CharSequence[] items = new String[2];

        alertextView.setText(R.string.sp_sim_type_comment_NORMAL);
        items[0] = getText(R.string.sp_sim_type_postpaid_NORMAL);
        items[1] = getText(R.string.sp_sim_type_prepaid_NORMAL);

        mClickedPos = getSIMTypeDB(this);

        final AlertController.AlertParams p = mAlertParams;
        p.mTitle = getText(R.string.sp_select_simcard_type_NORMAL);
        p.mItems = items;
        p.mOnClickListener = mItemClickListener;
        p.mView = alertLayout;
        p.mCheckedItem = mClickedPos;
        p.mIsSingleChoice = true;
        p.mPositiveButtonText = getString(R.string.dlg_ok);
        p.mPositiveButtonListener = this;

        setupAlert();

        mCancelable = getIntent().getBooleanExtra("cancelable", true);
        if (!mCancelable) {
            setFinishOnTouchOutside(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (mCancelable) {
            super.onBackPressed();
        }
    }

    // for mOnClickListener
    private DialogInterface.OnClickListener mItemClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            // Save the position of most recently clicked item
            mClickedPos = which;
        }
    };

    // for mPositiveButtonListener
    public void onClick(DialogInterface dialog, int which) {
        Log.d(TAG, "setPositiveButton select=" + mClickedPos);
        setSIMTypeDB(this, mClickedPos);
        sendIntentToSIMTypeChange();
        setResult(RESULT_OK); // [2012.03.06][seungeene][LGP700][NA] Add setResult

        // [S][2012.04.04][seungeene][LGP700][NA] start Data enable popup
        if (!mCancelable) {
            startDataEnableDialog(this);
        }
        // [E][2012.04.04][seungeene][LGP700][NA] start Data enable popup

        finish();
    }

    /*
     * 0: user not select
     * 1: Vodafone Contract
     * 2: Vodafone PAYG
     */
    private int getSIMTypeDB(Context context)
    {
        int simType = 0;
        int clickedPos = 0;

        simType = Settings.Secure.getInt(context.getContentResolver(),
                SettingsConstants.Secure.SIM_TYPE_SETTINGS, 0);
        Log.e(TAG, "getSIMTypeDB(): SimType (0:NS,1:Post,2:Pre): " + simType);

        // if did not selected, the default value is 1 (Vodafone Contract).
        // convert DB value(1, 2) to clickedPos(0,1)
        if (simType == 0) {
            clickedPos = 0;
            Settings.Secure.putInt(context.getContentResolver(),
                    SettingsConstants.Secure.SIM_TYPE_SETTINGS, 1);
        } else if (simType == 1) {
            clickedPos = 0;
        } else {
            clickedPos = 1;
        }

        return clickedPos;
    }

    private void setSIMTypeDB(Context context, int clickedPos) {
        Log.d(TAG, "setSIMTypeDB(): mClickedPos: " + mClickedPos);

        int simType = 0;

        // convert clickedPos(0, 1) to DB value(1,2)
        if (clickedPos == 0) {
            simType = 1;
        } else {
            simType = 2;
        }
        Settings.Secure.putInt(context.getContentResolver(),
                SettingsConstants.Secure.SIM_TYPE_SETTINGS, simType);
    }

    private void sendIntentToSIMTypeChange() {
        Log.d(TAG, "send intent SIM Type Changed");

        Intent intent = new Intent(SIM_TYPE_CHANGED);
        sendBroadcast(intent);
    }

    // [S][2012.04.04][seungeene][LGP700][NA] start Data enable popup
    private void startDataEnableDialog(Context context) {
        Log.i(TAG, "start startDataEnableDialog");
        Intent i = new Intent("android.intent.action.DATA_ENABLE_DIALOG");
        i.putExtra("cancelable", false);
        i.setClass(context, DataEnableDialog.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
    // [E][2012.04.04][youngmin.jeon][LGP700][NA] start Data enable popup
}
