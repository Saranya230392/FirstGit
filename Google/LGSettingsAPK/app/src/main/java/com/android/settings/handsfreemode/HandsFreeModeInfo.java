package com.android.settings.handsfreemode;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

public class HandsFreeModeInfo {
    private Context mContext;
    private ContentResolver mContentResolver;

    public static final int ON = 1;
    public static final int OFF = 0;

    public static final String TAG = "HandsFreeModeInfo";
    public static final String HANDS_FREE_MODE_STATUS = "hands_free_mode_status";
    public static final String HANDS_FREE_MODE_CALL = "hands_free_mode_call";
    public static final String HANDS_FREE_MODE_MESSAGE = "hands_free_mode_message";
    public static final String HANDS_FREE_MODE_READ_MESSAGE = "hands_free_mode_read_message";

    public HandsFreeModeInfo(Context _context) {
        mContext = _context;
        mContentResolver = mContext.getContentResolver();
    }

    //get DB
    public int getDBHandsFreeModeState() {
        try {
            return Settings.System.getInt(mContentResolver, HANDS_FREE_MODE_STATUS);
        } catch (SettingNotFoundException e) {
            Log.e(TAG, "SettingNotFoundException - getDBHandsFreeModeState()");
            setDBHandsFreeModeState(OFF);
            return 0;
        }
    }

    public int getDBHandsFreeModeCall() {
        try {
            return Settings.System.getInt(mContentResolver, HANDS_FREE_MODE_CALL);
        } catch (SettingNotFoundException e) {
            Log.e(TAG, "SettingNotFoundException - getDBHandsFreeModeCall()");
            setDBHandsFreeModeCall(ON);
            return ON;
        }
    }

    public int getDBHandsFreeModeMessage() {
        try {
            return Settings.System.getInt(mContentResolver, HANDS_FREE_MODE_MESSAGE);
        } catch (SettingNotFoundException e) {
            Log.e(TAG, "SettingNotFoundException - getDBHandsFreeModeMessage()");
            setDBHandsFreeModeReadMessage(ON);
            return ON;
        }
    }

    public int getDBHandsFreeModeReadMessage() {
        try {
            return Settings.System.getInt(mContentResolver, HANDS_FREE_MODE_READ_MESSAGE);
        } catch (SettingNotFoundException e) {
            Log.e(TAG, "SettingNotFoundException - getDBHandsFreeModeReadMessage()");
            setDBHandsFreeModeReadMessage(OFF);
            return 0;
        }
    }

    //set DB
    public void setDBHandsFreeModeState(int state) {
        Settings.System.putInt(mContentResolver, HANDS_FREE_MODE_STATUS, state);
    }

    public void setDBHandsFreeModeCall(int state) {
        Settings.System.putInt(mContentResolver, HANDS_FREE_MODE_CALL, state);
    }

    public void setDBHandsFreeModeMessage(int state) {
        Settings.System.putInt(mContentResolver, HANDS_FREE_MODE_MESSAGE, state);
    }

    public void setDBHandsFreeModeReadMessage(int state) {
        Settings.System.putInt(mContentResolver, HANDS_FREE_MODE_READ_MESSAGE, state);
    }

    public boolean isHandsFreeModeState() {
        return getDBHandsFreeModeState() == ON ? true : false;
    }

    public boolean isEmptyCheckHandsFreeMode() {
        if (getDBHandsFreeModeCall() == OFF && getDBHandsFreeModeMessage() == OFF) {
            return true;
        } else {
            return false;
        }
    }

    public void reSetVoiceNotification() {
        setDBHandsFreeModeState(OFF);
        setDBHandsFreeModeCall(ON);
        setDBHandsFreeModeMessage(ON);
        setDBHandsFreeModeReadMessage(OFF);
    }
}
