package com.android.settings.dragndrop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Log;

import com.android.settings.Utils;

public class ButtonCombinationReceiver extends BroadcastReceiver {
    private static final String TAG = "ButtonCombinationReceiver";
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        mContext = context;
        String action = intent.getAction();
        String pkgName = intent.getData().getSchemeSpecificPart();
        String QmemoPackageName = "com.lge.qmemoplus";

        if (!Utils.isUI_4_1_model(context)) {
            return;
        }
        final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            Log.d(TAG, "Receive package name : " + pkgName);
            Log.d(TAG, "replacing : " + replacing);
            if (!replacing && pkgName.equals(QmemoPackageName)) {
            	ButtonItemManager buttonItemManager = new ButtonItemManager(mContext);
            	buttonItemManager.deleteButtonCombination(pkgName);
            }
        }
    }
}
