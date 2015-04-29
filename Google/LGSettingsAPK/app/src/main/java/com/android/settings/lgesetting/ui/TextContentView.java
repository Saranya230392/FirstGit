package com.android.settings.lgesetting.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.settings.R;

public abstract class TextContentView extends Activity implements View.OnClickListener, DialogInterface.OnClickListener {
    //private static final String TAG = TextContentView.class.getSimpleName();

    protected static final int DIALOG_ID_ASK_REMOVE = 0;
    protected static final int DIALOG_ID_USER_BASE = 1;
    protected static int getUserDialogBaseId(){
        return DIALOG_ID_USER_BASE;
    }

    protected abstract void onEditButtonClick(View aView);
    protected abstract boolean doRemove(String aText);
    protected abstract String getContentText(Intent aIntent);
    //protected abstract String getNameText(Intent aIntent);

//    private boolean mUseAsyncRemove = false;

    private TextView mContentTextView;
    private Button mEditButton;
    private Button mRemoveButton;
//    protected Handler mMainLooperHandler;

    protected TextView getContentTextView(){
        return mContentTextView;
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewResourceId());

//        mMainLooperHandler = new Handler(getMainLooper());

        findViews();

        // set text
        String text = getContentText(getIntent());
        setText(text);

//        text = getNameText(getIntent());
//        if(text != null){
//            mNameTextView.setText(text);
//            mNameTextView.setVisibility(View.VISIBLE);
//        }

        mEditButton.setOnClickListener(this);
        mRemoveButton.setOnClickListener(this);
    }

    protected Dialog onCreateDialog(int id) {
        if(id == DIALOG_ID_ASK_REMOVE){
            return new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.def_remove_item_btn_caption)
                .setMessage(R.string.def_ask_remove_dialog_title)
                .setPositiveButton(R.string.def_ok_btn_caption, this)
                .setNegativeButton(R.string.def_cancel_btn_caption, null)
                .setCancelable(true)
                .create();
        }

        return super.onCreateDialog(id);
    }

    public void onClick(View aView) {
        if(aView == mEditButton){
            onEditButtonClick(aView);
        }else if(aView == mRemoveButton){
            onRemoveButtonClick(aView);
        }
    }

    protected void onRemoveButtonClick(View aView){
        showDialog(DIALOG_ID_ASK_REMOVE);
    }

    // for AskRemoveDialog
    public void onClick(DialogInterface aDialog, int aWhich) {
//        if(mUseAsyncRemove == true)
//            startAsyncRemove();
//        else
            if(doRemove(getText()))
                finish();
    }


    protected int getContentViewResourceId(){
        return R.layout.ui_text_content_view;
    }

    protected void setText(String aText){
    //    mContentTextView.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        if(TextUtils.isEmpty(aText))
            mContentTextView.setText("");
        else
            mContentTextView.setText(aText);
        Log.d("jeonghun.yi", "aText : "+aText);
    }

    protected String getText(){
        return mContentTextView.getText().toString();
    }

    private void findViews(){
//        mNameTextView = (TextView) findViewById(R.id.name);
        mContentTextView = (TextView) findViewById(R.id.content);
        mEditButton = (Button) findViewById(R.id.editButton);
        mRemoveButton = (Button) findViewById(R.id.removeButton);
    }

//    protected void startAsyncRemove(){
//        ThreadToast toast = new ThreadToast(this, mMainLooperHandler);
//        toast.setMessage(getResources().getString(R.string.def_removing_completed_message));
//
//        RemoveTaskThread removeTaskThread = new RemoveTaskThread();
//        removeTaskThread.setUserData(toast);
//
//        removeTaskThread.start();
//    }
//
//    protected Object startAsyncRemoveProgress(){
//        return TaskThreadHelper.startDefaultProgress(this, getResources().getString(R.string.def_remove_progressing_message));
//    }
//
//    protected void onAsyncRemoveComplete(Object aToast){
//        if(aToast != null){
//            ThreadToast toast = (ThreadToast) aToast;
//            toast.start();
//        }
//    }
//
//    protected void endAsyncRemoveProgress(Object aDialog){
//        TaskThreadHelper.endDefaultProgress(aDialog);
//    }
//
//    protected class RemoveTaskThread extends TaskThread {
////        public RemoveTaskThread(Context aContext, int aProgressMessageResourceID, int aCompleteMessageResourceId){
////            super(aContext, aProgressMessageResourceID, aCompleteMessageResourceId);
////        }
//        protected Object startProgress() {
//            return startAsyncRemoveProgress();
//        }
//
//        protected boolean doTask() {
//            return doRemove(getText());
//        }
//
//        protected void endProgress(Object aProgressHandle) {
//            endAsyncRemoveProgress(aProgressHandle);
//        }
//
//        protected void onComplete(){
//            onAsyncRemoveComplete(getUserData());
//        }
//
//        protected void onFinish() {
//            finish();
//        }
//    }
}
