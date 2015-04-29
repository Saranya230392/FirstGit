package com.android.settings.applications;

import java.util.ArrayList;
import java.util.Calendar;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.android.settings.Settings;
import com.android.settings.lgesetting.Config.Config;


public class AppMultiDeleteGlobalVariable extends Application {

    private Context context;
    public ManageApplications.ApplicationsAdapter mApplicationsAdapter;
    public ApplicationsState.Session mSession;
    public ApplicationsState applicationsState;
    private ArrayList<ApplicationsState.AppEntry> mEntries;
    private Calendar mCalendar;
    private boolean mIsThread;

    public AppMultiDeleteGlobalVariable() {
    }

    public void onCreate() {
        super.onCreate();
        Settings.mMyOwnResources = getResources();
        Config.sContext = this;
        context = null;
        mApplicationsAdapter = null;
        mSession = null;
        applicationsState = null;
        mEntries = null;
        mCalendar = null;
        mIsThread = false;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    ArrayList<ApplicationsState.AppEntry> getEntries() {
        return mEntries;
    }

    void setEntries(ArrayList<ApplicationsState.AppEntry> Entries) {
        mEntries = Entries;
    }

    ApplicationsState.Session getSession() {
        return mSession;
    }

    void setSession(ApplicationsState.Session session) {
        mSession = session;
    }

    ManageApplications.ApplicationsAdapter getApplicationsAdapter() {
        return mApplicationsAdapter;
    }

    void setApplicationsAdapter(ManageApplications.ApplicationsAdapter adapter) {
        mApplicationsAdapter = adapter;
    }

    void setPickCalendar(Calendar calendar) {
        mCalendar = calendar;
    }

    Calendar getPickCalendar() {
        return mCalendar;
    }

    void setIsTherad(boolean _isThread) {
        mIsThread = _isThread;
        Log.d("AppMultiDeleteGlobalVariable", "set _isThread = " + _isThread);
    }

    Boolean getIsTherad() {
        Log.d("AppMultiDeleteGlobalVariable", "get isThread = " + mIsThread);
        return mIsThread;
    }

}