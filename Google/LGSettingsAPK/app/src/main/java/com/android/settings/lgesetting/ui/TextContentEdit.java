package com.android.settings.lgesetting.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.settings.lgesetting.ui.TaskThread.TaskThreadCallback;

import com.android.settings.R;

public abstract class TextContentEdit extends Activity implements View.OnClickListener, OnClickListener {

    protected abstract boolean doSave();

    protected abstract String getCaption(Intent aIntent, int aIndex);
    protected abstract String getTextToEdit(Intent aIntent, int aIndex);

    protected static final int DIALOG_ID_EMPTY_CONTENT_ON_SAVE = 0;
    protected static final int DIALOG_ID_SAME_CONTENT_ON_SAVE = 1;
    protected static final int DIALOG_ID_START_PROGRESS = 2;
    protected static final int DIALOG_ID_USER_BASE = 3;

    protected static int getUserDialogBaseId(){
        return DIALOG_ID_USER_BASE;
    }

    private boolean mUseAsyncSave = false;
    private String mText;
    private EditText mEditText;
    private Button mSaveButton;
    private Button mCancelButton;
    protected Handler mMainLooperHandler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewResourceId());

        mMainLooperHandler = new Handler(getMainLooper());

        onFindViews();

        Intent intent = getIntent();

        String caption = getCaption(intent, 0);
        TextView captionTextView = (TextView)findViewById(R.id.editCaption);
        if(TextUtils.isEmpty(caption))
            captionTextView.setText("");
        else
            captionTextView.setText(caption);

        mText = getTextToEdit(intent, 0);
        if(TextUtils.isEmpty(mText))
            mText = "";

        restoreEditTextToOrgTextAndSelect(0);

        mSaveButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

    }


    protected Dialog onCreateDialog(int id) {
        switch(id){
        case DIALOG_ID_EMPTY_CONTENT_ON_SAVE:
            return new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.def_empty_text_on_save_dialog_title)
                .setMessage(R.string.def_empty_text_on_save_dialog_message)
                .setPositiveButton(R.string.def_confirm_btn_caption, null)
                .setCancelable(true)
                .create();

        case DIALOG_ID_SAME_CONTENT_ON_SAVE:
            return new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.def_same_text_on_save_dialog_title)
                .setMessage(R.string.def_same_text_on_save_dialog_message)
                .setPositiveButton(R.string.def_confirm_btn_caption, null)
                .setCancelable(true)
                .create();

        case DIALOG_ID_START_PROGRESS:
            return DialogFactory.createProgressingDialog(this, R.string.def_save_progressing_message, this);
        }

        return super.onCreateDialog(id);
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id){
        case DIALOG_ID_EMPTY_CONTENT_ON_SAVE:
            return;

        case DIALOG_ID_SAME_CONTENT_ON_SAVE:
            return;
        }

        super.onPrepareDialog(id, dialog);
    }

    public void onClick(DialogInterface aDialog, int aWhich) {
    }

    public void onClick(View aView) {
        if(aView == mSaveButton){
            onSaveButtonClick(aView);
        }else if(aView == mCancelButton){
            onCancelButtonClick(aView);
        }
    }

    protected void onSaveButtonClick(View aView){
        if (checkSave() == false)
            return;

        if (mUseAsyncSave == true)
            startAsyncUpdate(getContentText(0));
        else {
            if(doSave()) {
                finish();
            }
        }

        InputMethodManager input = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        // LGE_CHANGE_S [hd.jin@lge.com] by CA3 151617
        if ( input != null && input.isActive()) input.hideSoftInputFromWindow(mSaveButton.getWindowToken(),0);
        // LGE_CHANGE_E [hd.jin@lge.com] by CA3

    }

    protected int getContentViewResourceId(){
        return R.layout.ui_content_edit;
    }

    protected boolean checkSave(/*String aText*/){
        String text = getContentText(0);
        if(TextUtils.isEmpty(text)){
            restoreEditTextToOrgTextAndSelect(0);

            showDialog(DIALOG_ID_EMPTY_CONTENT_ON_SAVE);
            return false;
        }

        if(mText.equals(text)){
            restoreEditTextToOrgTextAndSelect(0);

            showDialog(DIALOG_ID_SAME_CONTENT_ON_SAVE);
            return false;
        }

        return true;
    }

    protected void onCancelButtonClick(View aView){
        mEditText.setText(mText);
        InputMethodManager input = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        // LGE_CHANGE_S [hd.jin@lge.com] by CA3 151618
        if (input != null && input.isActive()) input.hideSoftInputFromWindow(mCancelButton.getWindowToken(),0);
        // LGE_CHANGE_E [hd.jin@lge.com] by CA3
        finish();
    }

    protected String getOrgTextSource(int aIndex){
        return mText;
    }

    protected String getContentText(int aIndex){
        Log.d("jeonghun.yi", "getText() : "+ mEditText.getText());
        return mEditText.getText().toString();
    }

    protected EditText getEditText(int aIndex){
        return mEditText;
    }

    protected void restoreEditTextToOrgTextAndSelect(int aIndex){
        mEditText.setText(mText);
        mEditText.selectAll();        //seongjin7.kim 100808 SKLU3700,SLU310 COMMON LAB1_UX  TD 26391 Fix
    }

    protected void onFindViews(){
        mEditText = (EditText)findViewById(R.id.editText);
        mSaveButton = (Button)findViewById(R.id.saveButton);
        mCancelButton = (Button) findViewById(R.id.cancelButton);
    }

    protected void startAsyncUpdate(String aText){
        SaveTaskThread thread = new SaveTaskThread(mMainLooperHandler, new SaveTaskThreadCallback(this, mMainLooperHandler));
        thread.start();
    }

    protected class SaveTaskThreadCallback implements TaskThreadCallback{
        private Handler mMainLooperHandler = null;
        private Context mContext = null;

        public SaveTaskThreadCallback(Context aContext, Handler aMainLooperHandler){
            mMainLooperHandler = aMainLooperHandler;
            mContext = aContext;
        }

        public void onComplete(Object aUserData) {
            dismissDialog(DIALOG_ID_START_PROGRESS);

            ThreadToast toast = new ThreadToast(mContext, mMainLooperHandler);
            toast.setMessage(mContext.getResources().getString(R.string.def_saving_completed_message));
        }

        public void onStart(Object aUserData) {
            showDialog(DIALOG_ID_START_PROGRESS);
        }
    }

    protected class SaveTaskThread extends TaskThread {
        public SaveTaskThread(Handler aMainLooperHandler, TaskThreadCallback aCallback){
            super(aMainLooperHandler, aCallback);
        }

        protected void doTask() {
            if(doSave()){
                finish();
            }
        }
    }
}
