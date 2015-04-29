package com.android.settings.lgesetting.ui;

import android.os.Handler;

public abstract class TaskThread extends Thread {
    private static final String TAG = TaskThread.class.getSimpleName();

    public interface TaskThreadCallback {
        public void onStart(Object aUserData);
        public void onComplete(Object aUserData);
    }

    public static class TaskThreadMessage implements Runnable{
        public static final int MESSAGE_ONSTART = 0;
        public static final int MESSAGE_ONCOMPLETE = 1;

        private int mMessage;
        private TaskThreadCallback mCallback;
        private Object mUserData;

        public TaskThreadMessage(int aMessage, TaskThreadCallback aCallback, Object aUserData){
            mMessage = aMessage;
            mCallback = aCallback;
            mUserData = aUserData;
        }

        public void run() {
            switch(mMessage){
            case MESSAGE_ONSTART:
                mCallback.onStart(mUserData);
                break;

            case MESSAGE_ONCOMPLETE:
                mCallback.onComplete(mUserData);
                break;
            }
        }
    }

    private Handler mMainLooperHandler;
    private TaskThreadCallback mCallback;
    private Object mUserData;

    protected abstract void doTask();

    public TaskThread(Handler aMainLooperHandler, TaskThreadCallback aCallback){
        mMainLooperHandler = aMainLooperHandler;
        mCallback = aCallback;
    }

    public void setUserData(Object aUserData){
        mUserData = aUserData;
    }

    public Object getUserData(){
        return mUserData;
    }

    public Handler getMainLooperHandler(){
        return mMainLooperHandler;
    }

    public void start(){
        TaskThreadMessage msg = new TaskThreadMessage(TaskThreadMessage.MESSAGE_ONSTART, mCallback, mUserData);
        mMainLooperHandler.post(msg);

        super.start();
    }

    public void run(){
//        Log("Task: start");
        //while(mStarted){

            doTask();

            TaskThreadMessage msg = new TaskThreadMessage(TaskThreadMessage.MESSAGE_ONCOMPLETE, mCallback, mUserData);
            mMainLooperHandler.post(msg);

//            if(bFinish)
//                onFinish();

            //else{
            //    try{
            //        wait();
            //    }catch(InterruptedException e){
            //    }
            //}
        //}
//        Log("Task: end");

    }

    public void finalize(){
    //    Log("Finalize");
    }

    ////////////////////////////////////////////////////////////////////////////
    protected void Log(String aMessage){
    //    Log.d(getLogTag(), aMessage);
    }

    protected String getLogTag(){
        return TAG;
    }
}
