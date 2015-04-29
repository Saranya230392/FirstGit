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

package com.android.settings.fuelgauge;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Process;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.internal.os.BatterySipper;
import com.android.settings.R;

/**
 * Custom preference for displaying power consumption as a bar and an icon on
 * the left for the subsystem/app type.
 */
public class PowerGaugePreference2 extends ListPreference implements View.OnKeyListener {
    protected interface OnTreeClickListener {
        void onTreeClick(Preference preference);
    }


    public BatteryEntry mInfo;
    private int mProgress;
    private CharSequence mProgressText;
    //jongtak
    private PackageManager mPm;
    private DevicePolicyManager mDpm;
    private Context mContext;
    private LinearLayout mLayout;
    private OnTreeClickListener mTreeClickListener;
    private Button mForceStopButton;

    private String[] mPackages;

    private static final String TAG = "PowerGaugePreference2";
    public PowerGaugePreference2(Context context, Drawable icon, CharSequence contentDescription,
            BatteryEntry info) {
        super(context);
        setLayoutResource(R.layout.powersave_battery_use_details_item);
        setIcon(icon);
        mInfo = info;
        mContext = context;
    }

    //jongtak
    public void setOnTreeClickListener(OnTreeClickListener listener) {
        mTreeClickListener = listener;
    }

    public void setPercent(double percentOfMax, double percentOfTotal) {
        // jongtak0920.kim 120907 Show the gauge bar to match the number
        mProgress = (int)Math.round(percentOfTotal);
        mProgressText = getContext().getResources().getString(
                R.string.percentage, (int)Math.round(percentOfTotal));
        notifyChanged();
    }

    public BatteryEntry getInfo() {
        return mInfo;
    }

    //jongtak
    public Button getForceStopButton() {
        return mForceStopButton;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        final ProgressBar progress = (ProgressBar)view.findViewById(android.R.id.progress);
        progress.setProgress(mProgress);

        final TextView text1 = (TextView)view.findViewById(android.R.id.text1);
        text1.setText(mProgressText);

        //jongtak
        mPm = mContext.getPackageManager();
        mDpm = (DevicePolicyManager)mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (mInfo.sipper.uidObj != null) {
            mPackages = mContext.getPackageManager().getPackagesForUid(
                    mInfo.sipper.uidObj.getUid());
        }

        mForceStopButton = (Button)view.findViewById(R.id.battery_use_stop);
        mForceStopButton.setEnabled(false);
        if (mInfo.sipper.uidObj != null
                && mInfo.sipper.uidObj.getUid() >= Process.FIRST_APPLICATION_UID) {
            mForceStopButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View arg0) {

                    killProcesses();
                }
            });

        } else {
            mForceStopButton.setVisibility(View.INVISIBLE);
        }

        mLayout = (LinearLayout)view.findViewById(R.id.battery_use_details_pref);
        mLayout.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mTreeClickListener.onTreeClick(PowerGaugePreference2.this);
            }
        });

        checkForceStop();
    }

    private void killProcesses() {
        if (mPackages == null) {
            return;
        }
        ActivityManager am = (ActivityManager)mContext.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (int i = 0; i < mPackages.length; i++) {
            am.forceStopPackage(mPackages[i]);
            // yonguk.kim 20120816 ICS Stuff Porting to JB - Send Intent for Music Stop requested by System UI [START]
            if (mPackages[i].equals("com.lge.music")) {
                Log.i(TAG, "stop SystemUI Music controller");
                mContext.sendBroadcast(new Intent("com.lge.music.saveNoDisplay"));
            }
            // yonguk.kim 20120816 ICS Stuff Porting to JB - Send Intent for Music Stop requested by System UI [END]
        }

        checkForceStop();
    }

    private final BroadcastReceiver mCheckKillProcessesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mForceStopButton.setEnabled(getResultCode() != Activity.RESULT_CANCELED);

            //SKT TPhone ForceStop disabled
            if (mPackages == null
                    || (mInfo.sipper.uidObj != null && mInfo.sipper.uidObj.getUid() < Process.FIRST_APPLICATION_UID)) {
                mForceStopButton.setEnabled(false);
                return;
            }
            for (int i = 0; i < mPackages.length; i++) {
                if (mPackages[i].equals("com.skt.prod.dialer")
                        || mPackages[i].equals("com.skt.taction")
                        || mPackages[i].equals("com.android.systemui")) {
                    mForceStopButton.setEnabled(false);
                }
            }
        }
    };

    private void checkForceStop() {
        if (mPackages == null
                || (mInfo.sipper.uidObj != null && mInfo.sipper.uidObj.getUid() < Process.FIRST_APPLICATION_UID)) {
            mForceStopButton.setEnabled(false);
            return;
        }
        for (int i = 0; i < mPackages.length; i++) {
            if (mDpm.packageHasActiveAdmins(mPackages[i])) {
                mForceStopButton.setEnabled(false);
                return;
            }
        }
        for (int i = 0; i < mPackages.length; i++) {
            try {
                ApplicationInfo info = mPm.getApplicationInfo(mPackages[i], 0);
                if ((info.flags & ApplicationInfo.FLAG_STOPPED) == 0) {
                    mForceStopButton.setEnabled(true);
                    //SKT TPhone ForceStop disabled
                    if (mPackages[i].equals("com.skt.prod.dialer")
                            || mPackages[i].equals("com.skt.taction")
                            || mPackages[i].equals("com.android.systemui")) {
                        mForceStopButton.setEnabled(false);
                    }
                    break;
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        Intent intent = new Intent(Intent.ACTION_QUERY_PACKAGE_RESTART,
                Uri.fromParts("package", mPackages[0], null));
        intent.putExtra(Intent.EXTRA_PACKAGES, mPackages);
        intent.putExtra(Intent.EXTRA_UID, mInfo.sipper.uidObj.getUid());
        intent.putExtra(Intent.EXTRA_USER_HANDLE,
                UserHandle.getUserId(mInfo.sipper.uidObj.getUid()));
        mContext.sendOrderedBroadcast(intent, null, mCheckKillProcessesReceiver, null,
                Activity.RESULT_CANCELED, null, null);
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
            mTreeClickListener.onTreeClick(this);
        }
        return false;
    }
}
