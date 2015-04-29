/*========================================================================
Copyright (c) 2014 by LG MDM.  All Rights Reserved.

            EDIT HISTORY FOR MODULE

This section contains comments describing changes made to the module.
Notice that changes are listed in reverse chronological order.

when        who                 what, where, why
----------  --------------      ---------------------------------------------------
2014-12-04  sanghyuck.na        created new
=========================================================================*/

package com.android.settings.lgmdm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lge.mdm.LGMDMManager;

import com.android.settings.R;

public class MDMShortcutKeyAdapter {
    private static final String TAG = "MDMShortcutKeyAdapter";

    public static final String ACTION_SHORTCUT_KEY_POLICY_CHANGE =
            "com.lge.mdm.intent.action.ACTION_SHORTCUT_KEY_POLICY_CHANGE";

    private final static MDMShortcutKeyAdapter M_INSTANCE = new MDMShortcutKeyAdapter();

    public static MDMShortcutKeyAdapter getInstance() {
        return M_INSTANCE;
    }

    private MDMShortcutKeyAdapter() {
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-433]
    public void initMdmShortcutKeyPolicy(Context context, View sswitch, BroadcastReceiver receiver) {
        initMdmPolicyLayout(sswitch);
        initMdmPolicyMonitor(context, receiver);
    }

    private void initMdmPolicyLayout(View shortcutkeySwitch) {
        if (shortcutkeySwitch == null) {
            return;
        }

        if (isShortcutKeyPolicyAllowed() == true) {
            return;
        }

        shortcutkeySwitch.setEnabled(false); }

    private void initMdmPolicyMonitor(Context context, BroadcastReceiver receiver) {
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filter = new IntentFilter();
            Log.i(TAG, "addShortcutKeySettingChangeIntentFilter");
            filter.addAction(ACTION_SHORTCUT_KEY_POLICY_CHANGE);
            context.registerReceiver(receiver, filter);
        }
    }

    public boolean isShortcutKeyPolicyChanged(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }

        Log.i(TAG, "isShortcutKeyPolicyChanged action : " + intent.getAction());
        if (ACTION_SHORTCUT_KEY_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }

        return false;
    }

    public boolean isShortcutKeyPolicyAllowed() {
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            LGMDMManager mdm =  LGMDMManager.getInstance();
            if (mdm != null) {
                return mdm.getAllowShortcutKey(null);
            }
        }

        return true;
    }


    public boolean blindShortcutKeyUI(TextView descriptionShortcut, ImageView helpImage) {
        if (isShortcutKeyPolicyAllowed() == true ) {
            return false;
        }

        if (descriptionShortcut != null) {
            descriptionShortcut.setText(R.string.sp_lgmdm_block_shortcut_key_NORMAL);
        }
        if (helpImage != null) {
            helpImage.setImageDrawable(null);
        }

        return true;
    }


    public void finalizeMdmShortcutKeyMonitor(Context context, BroadcastReceiver receiver) {
        if (context == null) {
            return;
        }

        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                context.unregisterReceiver(receiver);
            } catch (Exception e) {
                Log.w(TAG, "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
    }
    // LGMDM_END
}
