package com.android.settings.lge;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class RunningAppsList extends ListManager<RunningAppInfo>{
    PackageManager mPm;
    ActivityManager mAm;
    ConcurrentHashMap<String, RunningAppInfo> mAppInfoList = new ConcurrentHashMap<String, RunningAppInfo>();
    Set<String> mListToRemove = new HashSet<String>();
    Set<String> mExclusions = new HashSet<String>(Arrays.asList(sExclusions));
    static final String[] sExclusions = { "com.lge.appbox.client", "com.nttdocomo.android.mascot" };
    static final String RCS_SERVICE_NAME = "RcsLayerService";
    
    public RunningAppsList(Context c) {
        super();
        mAm = (ActivityManager)c.getSystemService(Context.ACTIVITY_SERVICE);
        mPm = c.getPackageManager();
    }
	
	public void clearList() {
        //mCurJiffiesMap.clear();
        mAppInfoList.clear();
    }
	
	@Override
	public List<RunningAppInfo> createList(Set<String> scope) {
		// TODO Auto-generated method stub
		mAppInfoList.clear();
        HashMap<String, RunningAppProcessInfo> processes = new HashMap<String, RunningAppProcessInfo>();
        List<RunningAppProcessInfo> runningAppProcesses = mAm.getRunningAppProcesses();
        for (RunningAppProcessInfo process : emptyIfNull(runningAppProcesses)) {
            processes.put(process.processName, process);
        }
        setRunningTasks(processes, false, scope);
        setForegroundApps(processes, false, scope);
        list.clear();
        list.addAll(mAppInfoList.values());
        return list;
	}
	
	 void setRunningTasks(HashMap<String, RunningAppProcessInfo> processes, boolean update,
	            Set<String> scope) {

	        List<RecentTaskInfo> recentTasks = mAm.getRecentTasks(100,
	                ActivityManager.RECENT_IGNORE_UNAVAILABLE);
	        HashSet<String> runningTasks = getRunningTasks();

	        for (RecentTaskInfo task : emptyIfNull(recentTasks)) {
	            if (task.baseIntent == null || task.baseIntent.getComponent() == null) {
	                continue;
	            }
	            String packageName = task.baseIntent.getComponent().getPackageName();
	            if (runningTasks.contains(packageName) == false) {
	                continue;
	            }
	            if (scope.contains(packageName) == false) {
	                continue;
	            }
	            try {
	                ApplicationInfo appInfo = mPm.getApplicationInfo(packageName,
	                        PackageManager.GET_UNINSTALLED_PACKAGES);
	                RunningAppProcessInfo info = processes.get(appInfo.processName);
	                if (info != null) {
	                    /**
	                     * Recent task list is a list of the tasks that the user has recently launched, <br>
	                     * with the most recent being first and older ones after in order. <br>
	                     * If package name is same, older ones are skipped.
	                     */
	                    if (mAppInfoList.containsKey(packageName) == false) {
	                        mAppInfoList.put(packageName, getAppInfo(task, packageName, info.pid));
	                    }
	                    if (update) {
	                        mListToRemove.remove(packageName);
	                    }
	                }
	            } catch (NameNotFoundException e) {
	                //Utils.logDebug(TAG, e.getMessage());
	            }
	        }
	    }

	    /**
	     * App list with foreground service such as Qslide apps, Music, Voice recoder, FM Radio, <br>
	     * and 3rd party Music apps etc. When user stops them by pressing "Stop" button, <br>
	     * {@link ActivityManager#forceStopPackage(String)} is used.
	     * 
	     * @param processes
	     * @param update
	     * @param scope
	     */
	    void setForegroundApps(HashMap<String, RunningAppProcessInfo> processes, boolean update,
	            Set<String> scope) {

	        List<RunningServiceInfo> runningServices = mAm.getRunningServices(200);
	        for (RunningServiceInfo serviceInfo : emptyIfNull(runningServices)) {
	            if (serviceInfo.flags == (RunningServiceInfo.FLAG_FOREGROUND | RunningServiceInfo.FLAG_STARTED)) {
	                String packageName = serviceInfo.service.getPackageName();
	                if (scope.contains(packageName) == false) {
	                    continue;
	                }
	                if (mExclusions.contains(packageName)) {
	                    continue;
	                }
	                // Exclude joyn App. (http://www.joynus.com/ http://www.gsma.com/rcs/)
	                String serviceName = serviceInfo.service.getShortClassName();
	                if (serviceName != null && serviceName.endsWith(RCS_SERVICE_NAME)) {
	                    continue;
	                }
	                RunningAppProcessInfo info = processes.get(serviceInfo.process);
	                if (info != null) {
	                    if (mAppInfoList.containsKey(packageName)) {
	                        mAppInfoList.get(packageName).setForeground(true);
	                    } else {
	                        mAppInfoList.put(packageName, getForegroundAppInfo(packageName, info.pid));
	                    }
	                    if (update) {
	                        mListToRemove.remove(packageName);
	                    }
	                }
	            }
	        }
	    }

	@Override
	public List<RunningAppInfo> updateList(Set<String> scope) {
		// TODO Auto-generated method stub
		mListToRemove.clear();
        mListToRemove.addAll(mAppInfoList.keySet());
        list.clear();
        HashMap<String, RunningAppProcessInfo> processes = new HashMap<String, RunningAppProcessInfo>();
        List<RunningAppProcessInfo> runningAppProcesses = mAm.getRunningAppProcesses();
        for (RunningAppProcessInfo process : emptyIfNull(runningAppProcesses)) {
            processes.put(process.processName, process);
        }
        setRunningTasks(processes, true, scope);
        setForegroundApps(processes, true, scope);
        for (String target : mListToRemove) {
            if (mAppInfoList.containsKey(target)) {
                //mCurJiffiesMap.remove(mAppInfoList.get(target).getPid());
                mAppInfoList.remove(target);
            }
        }
        list.addAll(mAppInfoList.values());
        return list;
	}
	
    HashSet<String> getRunningTasks() {
        HashSet<String> runningTasks = new HashSet<String>();
        List<RunningTaskInfo> tasks = mAm.getRunningTasks(100);
        if (tasks != null) {
            for (RunningTaskInfo task : tasks) {
                if (task.baseActivity != null) {
                    runningTasks.add(task.baseActivity.getPackageName());
                }
                if (task.topActivity != null) {
                    runningTasks.add(task.topActivity.getPackageName());
                }
            }
        }
        return runningTasks;
    }
    
    RunningAppInfo getAppInfo(RecentTaskInfo task, String packageName, int pId) {

    	RunningAppInfo app = new RunningAppInfo(packageName, pId);
        app.setAppInfo(task, mPm);
        return app;
    }
    

    RunningAppInfo getForegroundAppInfo(String packageName, int pId) {
    	RunningAppInfo app = getAppInfo(null, packageName, pId);
        app.setForeground(true);
        return app;
    }
    
    static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
        return iterable == null ? Collections.<T> emptyList() : iterable;
    }
    
}
