/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The abstract class for user dictionary's word editor.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public abstract class UserDictionaryToolsEdit extends Activity implements View.OnClickListener {

    /** The flag start/close of User Dictionary Editor */
    private static boolean editing = false;

    /** The interface of a engine for an user dictionary. */
    protected UserDictionaryToolsEngineInterface mEngineInterface;

    /** The operation mode (Unknown) */
    private static final int STATE_UNKNOWN = 0;
    /** The operation mode (Add the word) */
    private static final int STATE_INSERT = 1;
    /** The operation mode (Edit the word) */
    private static final int STATE_EDIT = 2;

    /** Maximum length of a word's string */
    private static final int MAX_TEXT_SIZE = 50;

    /** The error code (Already registered the same word) */
    private static final int RETURN_SAME_WORD = -11;
    /** The error code (Full registration of the user dictionary) */
    private static final int RETURN_USER_DICTIONARY_FULL = -12;

    /** Text edit widget for reading */
    protected EditText mReadEditText;
    /** Text edit widget for candidate */
    private EditText mCandidateEditText;
    /** Entry button */
    private Button mEntryButton;
    /** Cancel button */
    private Button mCancelButton;

    /** The word information which holds the previous one */
    private WnnWord mBeforeEditWnnWord;

    /** The constant for notifying dialog (Already exists the specified word) */
    private static final int DIALOG_CONTROL_WORDS_DUPLICATE = 0;
    /** The constant for notifying dialog (The length of specified stroke or candidate exceeds the limit) */
    private static final int DIALOG_CONTROL_OVER_MAX_TEXT_SIZE = 1;

    /** The operation mode of this activity */
    private int mRequestState;

    /** Whether current language has no reading */
    protected boolean mIsNoStroke = false;

    /**
     * Constructor
     */
    public UserDictionaryToolsEdit() {
        super();
    }

    /**
     * Called when the activity is starting.
     * 
     * @see android.app.Activity#onCreate
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onCreate() : start.");}

        super.onCreate(savedInstanceState);
        mEngineInterface.setDirPath(getFilesDir().getPath());        
        
        /* create view from XML layout */
        setContentView(R.layout.user_dictionary_tools_edit);

        /* get widgets */
        mEntryButton = (Button)findViewById(R.id.addButton);
        mCancelButton = (Button)findViewById(R.id.cancelButton);
        mReadEditText = (EditText)findViewById(R.id.editRead);
        mCandidateEditText = (EditText)findViewById(R.id.editCandidate);

        if (mIsNoStroke) {
            mReadEditText.setVisibility(View.GONE);
            findViewById(R.id.labelRead).setVisibility(View.GONE);
        }

        /* set the listener */
        mEntryButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

        /* initialize */
        mRequestState = STATE_UNKNOWN;
        mReadEditText.setSingleLine();
        mCandidateEditText.setSingleLine();
        
        /* get the request and do it */
        Intent intent = getIntent();
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_INSERT)) {
            /* add a word */
            mEntryButton.setEnabled(false);
            mRequestState = STATE_INSERT;
        } else if (action.equals(Intent.ACTION_EDIT)) {
            /* edit a word */
            String stroke = intent.getStringExtra("stroke");
            String candidate = intent.getStringExtra("candidate");
            mEntryButton.setEnabled(true);
            mReadEditText.setText(stroke);
            mCandidateEditText.setText(candidate);
            mRequestState = STATE_EDIT;

            /* save the word's information before this edit */
            mBeforeEditWnnWord = new WnnWord();
            mBeforeEditWnnWord.stroke = stroke;
            mBeforeEditWnnWord.candidate = candidate;
        } else {
            /* finish if it is unknown request */
            Log.e("OpenWnn", "onCreate() : Invaled Get Intent. ID=" + intent);
            finish();
            return;
        }

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                                  R.layout.user_dictionary_tools_edit_header);

        /* set control buttons */
        setAddButtonControl();

        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onCreate() : end.");}
    }

    /**
     * Called when the activity has detected the user's press of the back key. 
     * 
     * @see android.app.Activity#onBackPressed
     */
    @Override public void onBackPressed() {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onBackPressed() : start.");}
        screenTransition();
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onBackPressed() : end.");}
    }

    /**
     * Called when you are longer visible to the user.
     * 
     * @see android.app.Activity#onStart
     */
    @Override protected void onStart() {
        editing = true;
        super.onStart();
    }

    /**
     * Called when you are no longer visible to the user.
     * 
     * @see android.app.Activity#onStop
     */
    @Override protected void onStop() {
        editing = false;
        super.onStop();
    }

    /**
     * Change the state of the "Add" button into the depending state of input area.
     */
    public void setAddButtonControl() {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "setAddButtonControl() : start.");}

        /* Text changed listener for the reading text */
        mReadEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /* Enable/disable the "Add" button */
                if ((mReadEditText.getText().toString().length() != 0) && 
                    ((mCandidateEditText.getText().toString().length() != 0))) {
                    mEntryButton.setEnabled(true);
                } else {
                    mEntryButton.setEnabled(false);
                }
            }
        });
        /* Text changed listener for the candidate text */
        mCandidateEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /* Enable/disable the "Add" button */
                if ((mIsNoStroke || (mReadEditText.getText().toString().length() != 0)) && 
                    (mCandidateEditText.getText().toString().length() != 0)) {
                    mEntryButton.setEnabled(true);
                } else {
                    mEntryButton.setEnabled(false);
                }
            }
        });

        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "setAddButtonControl() : end.");}
    }

    /**
     * Called when a view has been clicked.
     * 
     * @see android.view.View.OnClickListener#onClick
     */
    public void onClick(View v) {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onClick() : start.");}

        mEntryButton.setEnabled(false);
        mCancelButton.setEnabled(false);

        switch (v.getId()) {
            case R.id.addButton:
                /* save the word */
                doSaveAction();
                break;
 
            case R.id.cancelButton:
                /* cancel the edit */
                doRevertAction();
                break;

            default:
                Log.e("OpenWnn", "onClick: Get Invalid ButtonID. ID=" + v.getId());
                finish();
                return;
        }
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onClick() : end.");}
    }

    /**
     * Process the adding or editing action.
     */
    private void doSaveAction() {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "doSaveAction() : start.");}

        switch (mRequestState) {
        case STATE_INSERT:
            /* register a word */
            if ((mIsNoStroke || inputDataCheck(mReadEditText)) && inputDataCheck(mCandidateEditText)) {
                String candidate = mCandidateEditText.getText().toString();
                String stroke = mIsNoStroke ? candidate : mReadEditText.getText().toString();
                if (addToDictionary(stroke, candidate)) {
                    screenTransition();
                }
            }
            break;
            
        case STATE_EDIT:
            /* edit a word (=delete the word selected & add the word edited) */
            if ((mIsNoStroke || inputDataCheck(mReadEditText)) && inputDataCheck(mCandidateEditText)) {
                deleteDictionary(mBeforeEditWnnWord);
                String candidate = mCandidateEditText.getText().toString();
                String stroke = mIsNoStroke ? candidate : mReadEditText.getText().toString();
                if (addToDictionary(stroke, candidate)) {
                    screenTransition();
                } else {
                    addToDictionary(mBeforeEditWnnWord.stroke, mBeforeEditWnnWord.candidate);
                }
            }
            break;

        default:
            Log.e("OpenWnn", "doSaveAction: Invalid Add Status. Status=" + mRequestState);
            break;
        }
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "doSaveAction() : end.");}
    }
    
    /**
     * Process the cancel action.
     */
    private void doRevertAction() {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "doRevertAction() : start.");}
        /* go back to the words list */
        screenTransition();
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "doRevertAction() : end.");}
    }

    /**
     * Create the alert dialog for notifying the error.
     *
     * @param  id        The dialog ID
     * @return           The information of the dialog
     * @see    android.app.Activity#onCreateDialog
     */
    @Override protected Dialog onCreateDialog(int id) {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onCreateDialog() : start.");}
        switch (id) {
            case DIALOG_CONTROL_WORDS_DUPLICATE:
                /* there is the same word in the dictionary */
                return new AlertDialog.Builder(UserDictionaryToolsEdit.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.ti_user_dictionary_words_duplication_message_txt)
                        .setPositiveButton(R.string.ti_dialog_button_ok_txt, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mEntryButton.setEnabled(true);
                                mCancelButton.setEnabled(true);
                            }
                        })
                        .setCancelable(true)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                mEntryButton.setEnabled(true);
                                mCancelButton.setEnabled(true);
                            }
                        })
                        .create();

            case DIALOG_CONTROL_OVER_MAX_TEXT_SIZE:
                /* the length of the word exceeds the limit */
                return new AlertDialog.Builder(UserDictionaryToolsEdit.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.ti_user_dictionary_over_max_text_size_message_txt)
                        .setPositiveButton(R.string.ti_dialog_button_ok_txt, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int witchButton) {
                                mEntryButton.setEnabled(true);
                                mCancelButton.setEnabled(true);
                            }
                        })
                        .setCancelable(true)
                        .create();
        }
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onCreateDialog() : end.");}
        return super.onCreateDialog(id);
    }

    /**
     * Add the word to the user dictionary.
     *
     * @param  stroke       The stroke of the word
     * @param  candidate    The string of the word
     * @return              {@code true} if success; {@code false} if fail.
     */
    private boolean addToDictionary(String stroke, String candidate) {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "addToDictionary() : start.");}

        int ret = mEngineInterface.addWord(new WnnWord(candidate, stroke));
        if (ret < 0) {
            /* get error code if the process in IME is failed */
            if (ret == RETURN_SAME_WORD) {
                showDialog(DIALOG_CONTROL_WORDS_DUPLICATE);
            }
        }
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "addToDictionary() : end.");}
        return (0 <= ret);
    }

    /**
     * Delete the word on the user dictionary.
     *
     * @param  word     The information of word
     */
    private void deleteDictionary(WnnWord word) {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "deleteDictionary() : start.");}
        /* delete the word from the dictionary */
        boolean deleted = mEngineInterface.deleteWord(word);
        if (!deleted) {
            Toast.makeText(getApplicationContext(),
                           R.string.ti_user_dictionary_delete_fail_txt,
                           Toast.LENGTH_SHORT).show();
        }
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "deleteDictionary() : end.");}
    }

    /**
     * Check the input string.
     *
     * @param   v       The information of view
     * @return          {@code true} if success; {@code false} if fail.
     */
    private boolean inputDataCheck(View v) {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "inputDataCheck() : start.");}

        /* return false if the length of the string exceeds the limit. */
        if ((v instanceof TextView) && ((((TextView)v).getText().length()) > MAX_TEXT_SIZE)) {
            showDialog(DIALOG_CONTROL_OVER_MAX_TEXT_SIZE);
            Log.e("OpenWnn", "inputDataCheck() : over max string length.");
            return false;
        }

        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "inputDataCheck() : end.");}
        return true;
    }

    /**
     * Transit the new state.
     */
    private void screenTransition() {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "screenTransition() : start.");}
        finish();
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "screenTransition() : end.");}
    }

    /**
     * Whether the User dictionary Editor starts.
     * 
     * @return      {@code true} if editing; {@code false} if not editing.
     */
    public static boolean isEditing() {
        return editing;
    }
}
