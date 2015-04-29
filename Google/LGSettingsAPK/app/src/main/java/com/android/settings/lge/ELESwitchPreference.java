/*
 * A2 lab 4 team, Mobile Communication Company, LG ELECTRONICS INC., SEOUL, KOREA
 * Copyright(c) 2011 by LG Electronics Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LG Electronics Inc.
 */

package com.android.settings.lge;

import android.app.Activity;
import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.android.settings.R;

import android.provider.Settings;


/**
 * If you want to use this class please contact sy.yoon@lge.com
 */

final public class ELESwitchPreference extends SwitchPreference {
    private final Listener mListener = new Listener();
    private Activity activity = null;

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Settings.System.putInt(activity.getContentResolver(), "lge_notification_light_pulse", isChecked==true ? 1 : 0);
            ELESwitchPreference.this.setChecked(isChecked);
        }
    }

    public ELESwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_switch);
        // TODO Auto-generated constructor stub
        activity = (Activity) context;
    }

    public void setActivity(Activity _activity){
        activity = _activity;
    }

    @Override
    protected void onClick() {
        // TODO Auto-generated method stub
        //super.onClick();
    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);
        View checkableView = view.findViewById(R.id.switchWidget);
        if (checkableView != null && checkableView instanceof Checkable) {
            ((Checkable) checkableView).setChecked(super.isChecked());

            if (checkableView instanceof Switch) {
                final Switch switchView = (Switch) checkableView;
                switchView.setOnCheckedChangeListener(mListener);
            }
            // [SWITCH_SOUND]
            checkableView.setOnClickListener(new CompoundButton.OnClickListener() {
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                }
            });
        }
    }
}
