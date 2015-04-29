/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.provider.Telephony;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import com.android.settings.lgesetting.Config.Config;

// 3LM_MDM_DCM jihun.im@lge.com 20121015
import android.provider.Settings;
import android.os.Build;

public class ApnPreference extends Preference implements
        CompoundButton.OnCheckedChangeListener, OnClickListener {
    final static String TAG = "ApnPreference";

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public ApnPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * @param context
     * @param attrs
     */
    public ApnPreference(Context context, AttributeSet attrs) {
        //this(context, attrs, R.attr.apnPreferenceStyle);
        super(context, attrs);
        init();
    }

    /**
     * @param context
     */
    public ApnPreference(Context context,
                        long mSubsciptionID,
                        String mNumeric,
                        boolean mServieState) {
        this(context, null);
        init();
        mSubscription = mSubsciptionID;
        mOperatorNumeric = mNumeric;
        mIsGsm = mServieState;
        SLog.i("mSubscriptionID = " + mSubsciptionID + ", mIsGsm = " + mIsGsm);
    }

    public ApnPreference(Context context) {
        this(context, null);
        init();
    }

    private static String mSelectedKey = null;
    private static CompoundButton mCurrentChecked = null;
    private boolean mProtectFromCheckedChange = false;
    private boolean mSelectable = true;
    private boolean mLockable = false;
    private long mSubscription = 0;
    private String mOperatorNumeric = "";
    public boolean mIsGsm = true;

    @Override
    public View getView(View convertView, ViewGroup parent) {
        View view = super.getView(convertView, parent);

        View widget = view.findViewById(R.id.apn_radiobutton);
        if ((widget != null) && widget instanceof RadioButton) {
            RadioButton rb = (RadioButton)widget;
            if (mSelectable) {
                rb.setOnCheckedChangeListener(this);
                rb.setOnClickListener(this); //ask130329 :: td#263518, set the click sound.
                boolean isChecked = getKey().equals(mSelectedKey);
                if (isChecked) {
                    mCurrentChecked = rb;
                    mSelectedKey = getKey();
                }

                mProtectFromCheckedChange = true;
                rb.setChecked(isChecked);
                mProtectFromCheckedChange = false;
            } else {
                rb.setVisibility(View.GONE);
            }
        }

        View textLayout = view.findViewById(R.id.text_layout);
        if ((textLayout != null) && textLayout instanceof RelativeLayout) {
            textLayout.setOnClickListener(this);
        }
        if ("KR".equals(Config.getCountry())) {
            if (mLockable) {
                ImageView lockIcon = (ImageView)view.findViewById(R.id.lock_image);
                if (lockIcon != null) {
                    lockIcon.setBackgroundResource(R.drawable.ic_lock_lock);
                }
            }
        }
        return view;
    }

    private void init() {
        if ("KR".equals(Config.getCountry())) {
            setLayoutResource(R.layout.apn_preference_layout_kr);
        } else {
            setLayoutResource(R.layout.apn_preference_layout);
            // this(context, attrs, R.attr.apnPreferenceStyle);
        }
    }

    public boolean isChecked() {
        return getKey().equals(mSelectedKey);
    }

    public void setChecked() {
        mSelectedKey = getKey();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.i(TAG, "ID: " + getKey() + " :" + isChecked);
        if (mProtectFromCheckedChange) {
            return;
        }

        if (isChecked) {
            if (mCurrentChecked != null) {
                mCurrentChecked.setChecked(false);
            }
            mCurrentChecked = buttonView;
            mSelectedKey = getKey();
            callChangeListener(mSelectedKey);
        } else {
            mCurrentChecked = null;
            mSelectedKey = null;
        }
    }

    public void onClick(android.view.View v) {
        if ((v != null) && (R.id.text_layout == v.getId())) {
            Context context = getContext();
            if (context != null) {
                // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                if (getKey().equals("apn_manual")) {
                    context.startActivity(new Intent(Intent.ACTION_INSERT,
                            Telephony.Carriers.CONTENT_URI));
                } else {
                    // [END_LGE_SETTINGS], MOD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                    int pos = Integer.parseInt(getKey());
                    Uri url = ContentUris.withAppendedId(Telephony.Carriers.CONTENT_URI, pos);
                    if (!mLockable) {
                        //context.startActivity(new Intent(Intent.ACTION_EDIT, url));
                        Intent intent = new Intent(Intent.ACTION_EDIT, url);
                        intent.putExtra("subscription", mSubscription);
                        if ("CTC".equals(Config.getOperator())
                                || "CTO".equals(Config.getOperator())) {
                            intent.putExtra("operator_numeric", mOperatorNumeric);
                            intent.putExtra("is_gsm_state", mIsGsm);
                        }
                        context.startActivity(intent);
                        SLog.i("ACTION_EDIT :: subscription = " + mSubscription);
                    }
                    // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
                }
                // [END_LGE_SETTINGS], MOD, kiseok.son, 2012-07-13, Add the LGCPA_ menu.
            }
        }
    }

    public void setSelectable(boolean selectable) {
        mSelectable = selectable;
    }

    public boolean getSelectable() {
        return mSelectable;
    }

    /* LGE_CHANGE_S, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
    public void setUnChecked() {
        mSelectedKey = null;
    }

    /* LGE_CHANGE_E, donguk.ki@lge.com, 2011-04-11,<disable apn menu during tethering> */
    public void setLockable(boolean lockable) {
        mLockable = lockable;
    }

    public boolean getLockable() {
        return mLockable;
    }

    public boolean getServiceState() {
        return mIsGsm;
    }
}
