/*
 * A2 lab 4 team, Mobile Communication Company, LG ELECTRONICS INC., SEOUL, KOREA
 * Copyright(c) 2011 by LG Electronics Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LG Electronics Inc.
 */

package com.android.settings.nfc.preference;

import android.content.Context;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.nfc.NfcStateListener.NfcSwitchListener;

/**
 * If you want to use this class please contact sy.yoon@lge.com
 */

final public class NfcSwitchPreference extends SwitchPreference {
    private boolean mOnDivider;
    private final String TAG = "NfcSwitchPreference";
    private Switch switchView;
    private Switch oldswitchView;

    private boolean back_Checked;
    //private boolean back_Enabled;

    private boolean mIsSwitchChange;
    private int mMoveCount;
    private int back_Move_status;
    private static int MIN_ACTION_MOVE_CNT = 3;

    private NfcSwitchListener nfcSwitchListener;
    private final Listener mListener = new Listener();

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(TAG, "onCheckedChanged = " + isChecked);
            back_Checked = isChecked;

            if (nfcSwitchListener == null) {
                Log.d(TAG, "onCheckedChanged but nfcSwitchListener is null, return");
                buttonView.setChecked(!isChecked);
                return;
            }

            if (mIsSwitchChange) {
                Log.d(TAG, "onCheckedChanged, onSwitchChange = " + mMoveCount);

                if (buttonView instanceof Switch) {

                    if (buttonView.isChecked() != isChecked) {
                        Log.d(TAG, " buttonView status is different from isChecked : " + isChecked);
                        buttonView.setChecked(isChecked);
                     }

                    nfcSwitchListener.onSwitchChange((Switch)buttonView);

                    if (buttonView.isChecked() != back_Checked) {
                        back_Checked = buttonView.isChecked();
                        Log.d(TAG, "onCheckedChanged, back_Checked is changed to  " + back_Checked);
                    }

                } else {
                    Log.e(TAG, "onCheckedChanged, buttonView is not instance of Switch ");
                }

                //nfcSwitchListener.onSwitchChange(oldswitchView);
                mIsSwitchChange = false;
            }
            back_Move_status = 0;
        }
    }

    public NfcSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_switch);
        mOnDivider = true;
        switchView = null;
        oldswitchView = null;
        back_Checked = false;
        //back_Enabled = false;
        mIsSwitchChange = false;
        mMoveCount = 0;
        back_Move_status = 0;
    }

    public NfcSwitchPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);

        View checkableView = view.findViewById(R.id.switchWidget);
        Log.d(TAG, "switchView = " + checkableView);

        if (checkableView != null) {

            switchView = (Switch)checkableView;

            if (!switchView.equals(oldswitchView)) {
                if (switchView.isChecked() == back_Checked) {
                    Log.i(TAG, "check is same -> return");
                } else {
                    switchView.setChecked(back_Checked);
                    Log.i(TAG, "set Checked ok = " + back_Checked);
                }

                switchView.setOnCheckedChangeListener(mListener);
            } else {
                Log.i(TAG, "Switch not same!!!");
            }
            // rebestm - Switch drag issue
            switchView.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "MotionEvent = " + arg1);
                    back_Move_status = arg1.getAction();

                    if (back_Move_status == MotionEvent.ACTION_DOWN) {
                        mMoveCount = 0;
                    }

                    if (arg1.getAction() == MotionEvent.ACTION_MOVE
                            && (back_Move_status == MotionEvent.ACTION_MOVE
                            || back_Move_status == MotionEvent.ACTION_DOWN)) {
                        mMoveCount++;
                        if (mMoveCount >= MIN_ACTION_MOVE_CNT) {
                            mIsSwitchChange = true;
                        }
                    }
                    return false;
                }
            });

            switchView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (nfcSwitchListener != null) {
                        if (mMoveCount < MIN_ACTION_MOVE_CNT) {
                            Log.d(TAG, "setOnClickListener, onSwitchChange = " + mMoveCount);
                            nfcSwitchListener.onSwitchChange(arg0);
                        } else {
                            Log.d(TAG, "setOnClickListener, return = " + mMoveCount);
                        }
                    }

                }
            });

            oldswitchView = switchView;
        }

        if (!mOnDivider) {
            View vertical_divider = view.findViewById(R.id.switchImage);
            vertical_divider.setVisibility(View.INVISIBLE);
        }
    }

    public void setDivider(boolean enable) {
        mOnDivider = enable;
    }

    public void setNfcSwitchListener(NfcSwitchListener listener) {
        nfcSwitchListener = listener;
    }

    public void setChecked(boolean Checked) {
        if (switchView != null) {
            switchView.setChecked(Checked);
        }
        back_Checked = Checked;
    }

    // rebestm - Preference block issue (only switch block) 
    //    public void setEnabled(boolean Enabled) {
    //        if (switchView != null) {
    //            switchView.setEnabled(Enabled);
    //        }
    //        back_Enabled = Enabled;
    //    }
}
