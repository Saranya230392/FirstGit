package com.android.settings.lgesetting.ui;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class ThreadToast implements Runnable {
    private Context mContext = null;
    private Handler mHandler = null;
    private String mMessage = "";

    public ThreadToast(Context aContext, Handler aMainLooperHandler){
        mContext = aContext;
        if(aMainLooperHandler == null)
            mHandler = new Handler(aContext.getMainLooper());
        else
            mHandler = aMainLooperHandler;
    }

    public void start(){
        if(mHandler != null){
            mHandler.postAtTime(this, 0);
        }
    }

    public void setMessage(String aMessage){
        mMessage = aMessage;
    }

    public void run() {
        if(mMessage != null){
            Toast toast = Toast.makeText(mContext, mMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
