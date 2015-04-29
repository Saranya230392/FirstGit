package com.android.settings.lge;

import android.app.ActivityManager.RecentTaskInfo;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;

public class RunningAppInfo {
    String mName;
    String mPackageName;
    int mPid;
    ResolveInfo mResolveInfo;
    Intent mIntent;
    int mTaskId = -1;
    boolean isForeground = false;
    
    RunningAppInfo(String packageName, int pid) {
        mPackageName = packageName;
        mPid = pid;
    }
    
    public String getName() {
        return mName;
    }
    
    public String getPackageName() {
        return mPackageName;
    }
    
    public boolean isForeground() {
        return isForeground;
    }
    
    public void setForeground(boolean value) {
        isForeground = value;
    }
    
    public void setAppInfo(RecentTaskInfo task, PackageManager pm) {

        if (task != null) {
            Intent intent = new Intent(task.baseIntent);
            if (task.origActivity != null) {
                intent.setComponent(task.origActivity);
            }
            intent.setFlags((intent.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
            if (resolveInfo != null) {
                mResolveInfo = resolveInfo;
                mName = resolveInfo.loadLabel(pm).toString();
            }
            mTaskId = task.persistentId;
            mIntent = intent;
        } else {
            mTaskId = -1;
            mIntent = null;
            mResolveInfo = null;
        }
        if (mName == null) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(mPackageName,
                        PackageManager.GET_UNINSTALLED_PACKAGES);
                mName = (String)pm.getApplicationLabel(appInfo);
            } catch (NameNotFoundException e) {
                //Utils.logDebug("", e.getMessage());
            }
        }
    }

}
