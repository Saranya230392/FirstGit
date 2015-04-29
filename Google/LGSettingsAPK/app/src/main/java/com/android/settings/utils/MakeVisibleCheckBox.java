
package com.android.settings.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import com.lge.mdm.LGMDMManager;
import com.android.settings.R;

public class MakeVisibleCheckBox extends CheckBox implements View.OnKeyListener {
    private static final String MDM_PASSWORD_VISIBILITY_CHANGE_ACTION =
            "com.lge.mdm.intent.action.PASSWORD_VISIBLE_POLICY_CHANGE";
    private static final int MSG_ID_MDM_PASSWORD_VISIBILITY_CHANGE = 1001;

    private Context mContext;
    private Toast mToast = null;
    boolean mIsPwdVisibleAllowed = true;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ID_MDM_PASSWORD_VISIBILITY_CHANGE:
                    updatePasswordVisibleCheckboxState();
                    break;
                default:
                    break;
            }
        }
    };

    private BroadcastReceiver mMDMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // ATT does not support MDM
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (action.equals(MDM_PASSWORD_VISIBILITY_CHANGE_ACTION)
                        && mHandler != null) {
                    Log.i("MDM", "Attached handlers called for PASSWORD MDM changes.");
                    mHandler.sendEmptyMessage(MSG_ID_MDM_PASSWORD_VISIBILITY_CHANGE);
                }
            }
        }
    };

    public MakeVisibleCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public MakeVisibleCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MakeVisibleCheckBox(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setOnKeyListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsPwdVisibleAllowed) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return true;
                case MotionEvent.ACTION_UP:
                    showPasswordDisabledMDMToast();
                    return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (!mIsPwdVisibleAllowed && keyCode == KeyEvent.KEYCODE_ENTER) {
            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    return true;
                case KeyEvent.ACTION_UP:
                    showPasswordDisabledMDMToast();
                    return true;
            }
        }
        return false;
    }

    private void updatePasswordVisibleCheckboxState() {
        // ATT does not support MDM
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            mIsPwdVisibleAllowed = LGMDMManager.getInstance().getAllowPasswordVisible(null);
            Log.i("MDM", "MDM pwd visible Ui update function called isPasswordVisible  = " +
                    mIsPwdVisibleAllowed);
            if (!mIsPwdVisibleAllowed) {
                setChecked(false);
            }
        }
    }

    private void showPasswordDisabledMDMToast() {
        if (null == mToast) {
            mToast = Toast.makeText(mContext, R.string.sp_pwd_visibility_disabled_by_mdm,
                    Toast.LENGTH_SHORT);
        } else {
            mToast.setText(R.string.sp_pwd_visibility_disabled_by_mdm);
        }
        mToast.show();
    }

    public void onResume() {
        // ATT does not support MDM
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM && (mMDMReceiver != null)) {
            updatePasswordVisibleCheckboxState();
            mContext.registerReceiver(mMDMReceiver,
                    new IntentFilter(MDM_PASSWORD_VISIBILITY_CHANGE_ACTION));
        }
    }

    public void onPause() {
        // ATT does not support MDM
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM && (mMDMReceiver != null)) {
            mContext.unregisterReceiver(mMDMReceiver);
        }
    }

}
