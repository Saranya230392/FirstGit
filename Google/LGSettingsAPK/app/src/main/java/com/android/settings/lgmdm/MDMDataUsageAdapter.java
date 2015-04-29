/*========================================================================
Copyright (c) 2011 by LG MDM.  All Rights Reserved.

            EDIT HISTORY FOR MODULE

This section contains comments describing changes made to the module.
Notice that changes are listed in reverse chronological order.

when        who                 what, where, why
----------  --------------      ---------------------------------------------------
03 Feb 2015 a1-mdm-dev@lge.com
=========================================================================*/

package com.android.settings.lgmdm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.graphics.Color;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;


import com.android.settings.widget.ChartDataUsageView.DataUsageChartListener;
import com.android.settings.widget.ChartSweepView;
import com.android.settings.R;

import com.android.settings.Utils;
import com.lge.mdm.LGMDMManager;

public class MDMDataUsageAdapter {
    private static final String TAG = "MDMDataUsageAdapter";

    private static final String ACTION_MOBILE_NETWORK_POLICY_CHANGE =
        "com.lge.mdm.intent.action.MOBILE_NETWORK_POLICY_CHANGE";
    private static final String ACTION_DATA_ROAMING_POLICY_CHANGE =
        "com.lge.mdm.intent.action.DATA_ROAMING_POLICY_CHANGE";
    private static final String ACTION_BACKGROUND_DATA_RESTRICTED_POLICY_CHANGE =
        "com.lge.mdm.intent.action.BACKGROUND_DATA_RESTRICTED_POLICY_CHANGE";
    private static final String ACTION_CHANGING_MOBILE_DATAUSAGE_CYCLE =
        "com.lge.mdm.intent.action.CHANGING_MOBILE_DATAUSAGE_CYCLE";
    private static final String ACTION_ENFORCE_MOBILE_DATAUSAGE_LIMIT =
        "com.lge.mdm.intent.action.ACTION_ENFORCE_MOBILE_DATAUSAGE_LIMIT";
    private static final String ACTION_ENFORCE_MOBILE_DATAUSAGE_WARN =
        "com.lge.mdm.intent.action.ACTION_ENFORCE_MOBILE_DATAUSAGE_WARN";

    private static final MDMDataUsageAdapter mInstance = new MDMDataUsageAdapter();

    public static MDMDataUsageAdapter getInstance() {
        return mInstance;
    }

    private MDMDataUsageAdapter() {
    }

    public void initMdmPolicyMonitor(Context context, BroadcastReceiver receiver) {
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM == false) {
            return;
        }

        if (context == null || receiver == null) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        addDataUsageSettingPolicyChangeIntentFilter(filter);

        try {
            context.registerReceiver(receiver, filter);
        } catch (Exception e) {
            Log.w(TAG, "mLGMDMReceiver unregisterReceiver ", e);
        }
    }

    private void addDataUsageSettingPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_MOBILE_NETWORK_POLICY_CHANGE);
        filter.addAction(ACTION_DATA_ROAMING_POLICY_CHANGE);
        filter.addAction(ACTION_BACKGROUND_DATA_RESTRICTED_POLICY_CHANGE);
        filter.addAction(ACTION_CHANGING_MOBILE_DATAUSAGE_CYCLE);
        filter.addAction(ACTION_ENFORCE_MOBILE_DATAUSAGE_LIMIT);
        filter.addAction(ACTION_ENFORCE_MOBILE_DATAUSAGE_WARN);
    }

    public void finalizeMdmPolicyMonitor(Context context, BroadcastReceiver receiver) {
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM == false) {
            return;
        }

        if (context == null || receiver == null) {
            return;
        }

        try {
            context.unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.w(TAG, "mLGMDMReceiver unregisterReceiver ", e);
        }

    }

    public boolean setDataUsageSwitch(Switch mDataEnabled, OnCheckedChangeListener listener) {
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM == false) {
            return false;
        }

        if (mDataEnabled == null) {
            return false;
        }

        if (LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(null)) {
            mDataEnabled.setEnabled(false);
        } else if (!LGMDMManager.getInstance().getAllowMobileNetwork(null)) {
            mDataEnabled.setOnCheckedChangeListener(null);
            mDataEnabled.setChecked(false); // for Settings UI update
            mDataEnabled.setEnabled(false);
            mDataEnabled.setOnCheckedChangeListener(listener);
        }
        return true;
    }

    public boolean isMobileNetworkDisallowed() {
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM == false) {
            return false;
        }

        if (com.lge.mdm.LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(null) == true
                || LGMDMManager.getInstance().getAllowMobileNetwork(null) == false) {
            return true;
        }

        return false;
    }

    public boolean setDataUsageSwitchMultiSIM(Switch mDataEnabled,
            OnCheckedChangeListener listener, boolean airPlaneMode) {

        if (com.lge.config.ConfigBuildFlags.CAPP_MDM == false) {
            return false;
        }

        if (mDataEnabled == null) {
            return false;
        }

        if (LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(null)) {
            mDataEnabled.setEnabled(false);
        } else if (!LGMDMManager.getInstance().getAllowMobileNetwork(null)) {
            mDataEnabled.setOnCheckedChangeListener(null);
            mDataEnabled.setChecked(false); // for Settings UI update
            mDataEnabled.setEnabled(false);
            mDataEnabled.setOnCheckedChangeListener(listener);
        }

        return true;
    }

    public boolean isDataUsageSettingPolicyChanged(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }

        Log.i(TAG, "isDataUsageSettingPolicyChanged action : " + intent.getAction());

        if (ACTION_MOBILE_NETWORK_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_DATA_ROAMING_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_BACKGROUND_DATA_RESTRICTED_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_CHANGING_MOBILE_DATAUSAGE_CYCLE.equals(intent.getAction())
                || ACTION_ENFORCE_MOBILE_DATAUSAGE_LIMIT.equals(intent.getAction())
                || ACTION_ENFORCE_MOBILE_DATAUSAGE_WARN.equals(intent.getAction())
           ) {
            return true;
        }
        return false;
    }


    public boolean isShowEnforceBackgroundDataRestrictedToastIfNeed(Context context) {
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM == false) {
            return false;
        }

        if (LGMDMManager.getInstance().getEnforceBackgroundDataRestricted(null) == false) {
            Toast.makeText(context,
                    Utils.getString(R.string.sp_lgmdm_block_restrict_background_data_NORMAL),
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    public boolean isDataUsageCycleDisallowed(Context context, String tabInfo) {
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM == false) {
            return false;
        }

        if ((tabInfo == null)
                || "wifi".equals(tabInfo)
                || "ethernet".equals(tabInfo)
                || (LGMDMManager.getInstance().getAllowChangingMobileDataUsageCycle(null) == true)) {
            return false;
        }

        Toast.makeText(context, Utils.getString(R.string.sp_lgmdm_block_change_mobile_period),
                Toast.LENGTH_SHORT).show();

        return true;
    }

    public boolean onClickDataUsageLimitMenu() {
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM == false) {
            return false;
        }

        if (LGMDMManager.getInstance().getEnforceLimitMobileDataUsage(null) == false) {
            return false;
        }
        return true;
    }

    public void setDataUsageLimitMenu(CheckBox mDisableAtLimit, View mDisableAtLimitView) {
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM == false) {
            return;
        }

        if (LGMDMManager.getInstance().getEnforceLimitMobileDataUsage(null) == false) {
            return;
        }

        if (mDisableAtLimit == null) {
            return;
        }

        mDisableAtLimit.setChecked(true);
        mDisableAtLimit.setEnabled(false);

        if (mDisableAtLimitView != null) {
            final TextView summary = (TextView)mDisableAtLimitView
                    .findViewById(android.R.id.summary);
            summary.setVisibility(View.VISIBLE);
            summary.setText(R.string.sp_lgmdm_block_limit_mobile_data);
            summary.setTextColor(Color.GRAY);

            final TextView title = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
            title.setTextColor(Color.GRAY);
        }
    }

    public boolean setDataUsageLimitToast(Context context) {
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM == false) {
            return false;
        }

        if (LGMDMManager.getInstance().getEnforceLimitMobileDataUsage(null) == false) {
            return false;
        }

        Toast.makeText(context, Utils.getString(R.string.sp_lgmdm_block_change_mobile_data),
                Toast.LENGTH_SHORT).show();

        return true;
    }


    public boolean setDataUsageWarnToast(Context context) {
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM == false) {
            return false;
        }

        if (LGMDMManager.getInstance().getEnforceAlertMobileDataUsage(null) == false) {
            return false;
        }

        Toast.makeText(context, Utils.getString(R.string.sp_lgmdm_block_change_mobile_data),
                Toast.LENGTH_SHORT).show();

        return true;
    }

    public boolean setDataUsageWarnChart(ChartSweepView sweep, ChartSweepView mSweepWarn,
            DataUsageChartListener mListener) {
        boolean ret = false;
        if (LGMDMManager.getInstance().getEnforceAlertMobileDataUsage(null) == false) {
            return ret;
        }
        if (sweep == mSweepWarn && mListener != null) {
            ret = true;
        }
        return ret;
    }
}
