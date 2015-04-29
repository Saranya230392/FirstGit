
package com.android.settings.defaultapp;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class UpdateThread extends HandlerThread implements Callback {

    public static final int START = 1;
    public static final int REFRESH = 2;
    public static final int STOP = 3;
    public static final int SORT = 4;
    public static final int UPDATE_STORAGE = 5;

    public static final int      SORT_TIME               = 0;
    public static final int      SORT_NAME               = 1;
    public static final int      SORT_SIZE               = 2;

    public Handler mThreadHandler = null;

    public UpdateThread(String name) {
        super(name);
    }

    @Override
    public synchronized void start() {

        super.start();

        if (mThreadHandler == null) {
            Looper looper = getLooper();
            if (looper != null) {
                mThreadHandler = new Handler(looper, this);
            }
        }
    }

    public void requestUpdate() {

        mThreadHandler.sendEmptyMessage(START);
    }

    public void sort() {

        mThreadHandler.sendEmptyMessage(SORT);
    }

    public void delayedRefresh(int delay) {
        mThreadHandler.removeMessages(REFRESH);
        mThreadHandler.sendEmptyMessageDelayed(REFRESH, delay);
    }

    public void refresh() {
        mThreadHandler.removeMessages(REFRESH);
        mThreadHandler.sendEmptyMessage(REFRESH);
    }

    public void stopUpdate() {
        //mThreadHandler.sendEmptyMessage(STOP);
        mThreadHandler.removeCallbacksAndMessages(null);
    }

    public boolean handleMessage(Message msg) {
        return false;
    }
}
