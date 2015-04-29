// LGE_UICC_S, [USIM_Patch 2055][KR] jaewoo.seo, 2014-11-14, SWMD UICC LGSettings Refactoring.
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.lgesetting.Config.Config;
import com.lge.uicc.LGUiccCard;
import com.lge.uicc.LGUiccManager;
import android.app.ProgressDialog;
import android.text.InputFilter;
import android.graphics.Color;
import android.widget.TextView;
import java.util.HashMap;

import java.util.Locale;
/**
 * Implements the preference screen to enable/disable ICC lock and also the
 * dialogs to change the ICC PIN. In the former case, enabling/disabling the ICC
 * lock will prompt the user for the current PIN. In the Change PIN case, it
 * prompts the user for old pin, new pin and new pin again before attempting to
 * change it. Calls the SimCard interface to execute these operations.
 */
public class IccLockSettings_kr extends PreferenceActivity
        implements
            EditPinPreference_kr.OnPersoEnteredListener,
            EditPinPreference_kr.OnPinEnteredListener,
            Callback {

    private static final String LOG_TAG = "IccLockSettings_kr";

    // [PIN Dialog Mode]
    private static final int OFF_MODE = 0;
    // State when enabling/disabling ICC lock
    private static final int ICC_LOCK_MODE = 1;
    // State when entering the old pin
    private static final int ICC_OLD_MODE = 2;
    // State when entering the new pin - first time
    private static final int ICC_NEW_MODE = 3;
    // State when entering the new pin - second time
    private static final int ICC_REENTER_MODE = 4;

    private static final int ICC_UNLOCK_MODE = 5;

    // [PERSO Dialog Mode]
    private static final int PERSO_OFF_MODE = 10;
    // State when enabling/disabling ICC lock
    private static final int PERSO_LOCK_MODE = 11;
    // State when entering the old pin
    private static final int PERSO_OLD_MODE = 12;
    // State when entering the new pin - first time
    private static final int PERSO_NEW_MODE = 13;
    // State when entering the new pin - second time
    private static final int PERSO_REENTER_MODE = 14;

    private static final int PERSO_UNLOCK_MODE = 15;

    // Keys in xml file
    private static final String PIN_TOGGLE = "sim_toggle";
    private static final String PIN_DIALOG = "sim_pin";
    // add perso
    private static final String PERSO_TOGLE = "sim_perso_toggle";
    private static final String PERSO_DIALOG = "sim_perso_pin";

    // Keys in icicle
    private static final String DIALOG_STATE = "dialogState";
    private static final String DIALOG_PIN = "dialogPin";
    private static final String DIALOG_ERROR = "dialogError";
    private static final String ENABLE_TO_STATE = "enableState";

    private static final String DIALOG_PERSO_STATE = "dialogPersoState";
    private static final String DIALOG_PERSO = "dialogPerso";
    private static final String DIALOG_PERSO_ERROR = "dialogPersoError";
    private static final String ENABLE_TO_PERSO_STATE = "enablePersoState";

    // Save and restore inputted PIN code when configuration changed
    // (ex. portrait<-->landscape) during change PIN code
    private static final String OLD_PINCODE = "oldPinCode";
    private static final String NEW_PINCODE = "newPinCode";

    private static final String OLD_PERSOCODE = "oldPersoCode";
    private static final String NEW_PERSOCODE = "newPersoCode";

    public static final int MAX_SIM_RETRY_COUNT = 3;

    // Which dialog to show next when popped up
    private int mDialogState = OFF_MODE;
    private int mDialogPersoState = PERSO_OFF_MODE;

    private String mPin;
    private String mOldPin;
    private String mNewPin;
    private String mError;

    private String mPerso;
    private String mOldPerso;
    private String mNewPerso;

    private boolean mToState;
    private boolean mToStatePerso;

    private boolean sContactsLoaded = false;
    private boolean sContactsLoaded_first = true;

    private boolean first = true;

    private int defaultColorTitle = Color.BLACK;
    private int defaultColorSummary = Color.BLACK;

    private Phone mPhone;

    private CheckBoxPreference mPinToggle;
    private EditPinPreference_kr mPinDialog;

    private CheckBoxPreference mPersoToggle;
    private EditPinPreference_kr mPersoDialog;

    private Resources mRes;

    private ProgressDialog progress;

    private HashMap<Integer, String> pinTitle;
    private HashMap<Integer, String> pinMsg;
    private HashMap<Integer, String> persoTitle;
    private HashMap<Integer, String> persoMsg;

    // For async handler to identify request type
    private static final int MSG_ENABLE_ICC_PIN_COMPLETE = 100; // ICC_LOCK_MODE
    private static final int MSG_SIM_CHNAGE_ENTER = 101; // ICC_OLD_MODE
    private static final int MSG_CHANGE_ICC_PIN_COMPLETE = 102; // ICC_REENTER_MODE

    private static final int MSG_ENABLE_ICC_PERSO_COMPLETE = 110; // PERSO_LOCK_MODE
    private static final int MSG_PERSO_CHNAGE_ENTER = 111; // PERSO_OLD_MODE
    private static final int MSG_CHANGE_ICC_PERSO_COMPLETE = 112; // PERSO_REENTER_MODE
    private static final int MSG_GET_PIN_RETRY_COUNT_DELAY = 113; // LGE_UICC, [USIM_Patch_2059][KOR] eunju.choi, 2015-02-16, Apply KOR USIM Setting label integration for GUI 4.2.

    private static final int MSG_SIM_STATE_CHANGED = 120;

    private static final int SIM_LOADING_CONTACTS = 123;

    private static final String ACTION_REFRESH_SIMSEARCH_STATUS = "com.lge.phone.ACTION_REFRESH_SIMSEARCH_STATUS";

    // For replies from IccCard interface
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar = (AsyncResult)msg.obj;

            Log.i(LOG_TAG, "handleMessage , msg.what =" + msg.what);

            switch (msg.what) {
                // LGE_UICC_S, [USIM_Patch_2059][KOR] eunju.choi, 2015-02-16, Apply KOR USIM Setting label integration for GUI 4.2. {
                case MSG_GET_PIN_RETRY_COUNT_DELAY :
                     Log.i(LOG_TAG, "MSG_GET_PIN_RETRY_COUNT_DELAY");
                     iccPinChanged(true);
                     break;
                // LGE_UICC_E, [USIM_Patch_2059][KOR] }
                case MSG_ENABLE_ICC_PIN_COMPLETE : // ICC_LOCK_MODE
                    iccLockChanged(ar.exception == null);
                    break;
                case MSG_SIM_CHNAGE_ENTER : // ICC_OLD_MODE
                    if (!Config.KT.equals(Config.getOperator())) { // SKT + U+
                        iccEnterPinChange(ar.exception == null);
                    } else {
                        // for korea(KT) spec, verify after PIN1 -> NEW PIN1 ->
                        // REENTER PIN1
                        iccEnterPinChange(msg.arg1 == 1);
                    }
                    break;
                case MSG_CHANGE_ICC_PIN_COMPLETE : // ICC_REENTER_MODE
                    // LGE_UICC_S, [USIM_Patch_2059][KOR] eunju.choi, 2015-02-16, Apply KOR USIM Setting label integration for GUI 4.2. {
                    if(ar.exception == null) {
                        this.sendEmptyMessageDelayed(MSG_GET_PIN_RETRY_COUNT_DELAY, 400);
                        Log.i(LOG_TAG, "send msg MSG_GET_PIN_RETRY_COUNT_DELAY");
                    } else {
                        iccPinChanged(ar.exception == null); //=false
                    }
                    // LGE_UICC_E, [USIM_Patch_2059][KOR] }
                    break;
                case MSG_SIM_STATE_CHANGED :
                    updatePreferences();
                    break;
                case MSG_ENABLE_ICC_PERSO_COMPLETE : // PERSO_LOCK_MODE
                    persoLockChanged(msg.arg1 == 1);
                    break;
                case MSG_PERSO_CHNAGE_ENTER : // PERSO_OLD_MODE
                    iccEnterPersoChange(msg.arg1 == 1);
                    break;
                case MSG_CHANGE_ICC_PERSO_COMPLETE : // PERSO_REENTER_MODE
                    iccPersoChanged(msg.arg1 == 1);
                    break;
                default :
                    break;
            }

        }
    };

    private final BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)) {
                Log.i(LOG_TAG, "onReceive ACTION_SIM_STATE_CHANGED ");
                mHandler.sendMessage(mHandler
                        .obtainMessage(MSG_SIM_STATE_CHANGED));
            } else if (ACTION_REFRESH_SIMSEARCH_STATUS.equals(action)// Contact
                                                                     // loading
                                                                     // Completed
                    && intent.getBooleanExtra("mInitialized", true)) {

                Log.i(LOG_TAG, "onReceive ACTION_REFRESH_SIMSEARCH_STATUS ");
                Log.i(LOG_TAG, "sContactsLoaded : " + sContactsLoaded);
                sContactsLoaded = true;
                Log.i(LOG_TAG, "sContactsLoaded : " + sContactsLoaded);
                progress.dismiss();
            }
        }
    };

    private EditPinPreference_kr.EditPreferenceBindListener mEditPreferenceBindListener = new EditPinPreference_kr.EditPreferenceBindListener() {

        public void onBindEditPreferenceView(String key, View view) {
            Log.i(LOG_TAG, "onBindEditPreferenceView() ");
            Log.i(LOG_TAG, "key :  " + key);

            if ("sim_pin".equals(key)) {
                Log.i(LOG_TAG, "sim_pin.equals(key() ");

                TextView textView_title = (TextView)view
                        .findViewById(com.android.internal.R.id.title);
                TextView textView_summary = (TextView)view
                        .findViewById(com.android.internal.R.id.summary);

                if (textView_title == null) {
                    Log.i(LOG_TAG, "(textView_title ==null)");
                }

                if (textView_summary == null) {
                    Log.i(LOG_TAG, "(textView_summary ==null) ");
                }

                if ((textView_title != null) && (textView_summary != null)) {
                    if (first) {
                        Log.i(LOG_TAG, "first ");

                        defaultColorTitle = textView_title
                                .getCurrentTextColor();

                        defaultColorSummary = textView_summary
                                .getCurrentTextColor();
                        first = false;
                    }

                    if (!PhoneFactory.getDefaultPhone().getIccCard()
                            .getIccLockEnabled()) // If Pin Disable, PIN1
                    {
                        Log.i(LOG_TAG,
                                "getIccLockEnabled : pin disable, Color change : LTGRAY");

                        textView_title.setTextColor(Color.LTGRAY);
                        textView_summary.setTextColor(Color.LTGRAY);
                    } else {
                        Log.i(LOG_TAG,
                                "getIccLockEnabled : pin enable, Color change : defaultColor");
                        textView_title.setTextColor(defaultColorTitle);
                        textView_summary.setTextColor(defaultColorSummary);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Together PIN&PERSO
        Log.i(LOG_TAG, "onCreate() ");
        super.onCreate(savedInstanceState);
        if (Utils.isMonkeyRunning()) {
            finish();
            return;
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true); // display "<"
        }
        addPreferencesFromResource(R.xml.icc_lock_settings);

        mPinToggle = (CheckBoxPreference)findPreference(PIN_TOGGLE);
        mPinDialog = (EditPinPreference_kr)findPreference(PIN_DIALOG);

        mPersoToggle = (CheckBoxPreference)findPreference(PERSO_TOGLE);
        mPersoDialog = (EditPinPreference_kr)findPreference(PERSO_DIALOG);

        final int maxSimPersoControlkeyLen = (Config.SKT
                .equals(Config.getOperator()) ? 4 : 8);

        progress = new ProgressDialog(this);

        if (savedInstanceState != null) {
            restoreSavedInstanceState(savedInstanceState);
        }

        mPinDialog.setOnPinEnteredListener(this);
        mPersoDialog.setOnPersoEnteredListener(this);

        mPinDialog.setEditPreferenceBindListener(mEditPreferenceBindListener);

        // Don't need any changes to be remembered
        getPreferenceScreen().setPersistent(false);

        mPhone = PhoneFactory.getDefaultPhone();
        mRes = getResources();

        InputFilter[] filter = new InputFilter[]{new InputFilter.LengthFilter(
                maxSimPersoControlkeyLen)};
        mPersoDialog.setMaxLenInputFilter(filter);
        Log.i(LOG_TAG,
                "mPersoDialog.setMaxLenInputFilter(filter) : maxSimPersoControlkeyLen = "
                        + maxSimPersoControlkeyLen);

        if (Config.LGU.equals(Config.getOperator())) {
            getPreferenceScreen().removePreference(mPersoToggle);
            getPreferenceScreen().removePreference(mPersoDialog);
            mPersoToggle = null;
            mPersoDialog = null;
        }

        setResPinString();
        setResPersoString();

        updatePreferences();

    }

    private void restoreSavedInstanceState(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "restoreSavedInstanceState() ");

        if (savedInstanceState.containsKey(DIALOG_STATE)) {
            Log.i(LOG_TAG, "savedInstanceState.containsKey(DIALOG_STATE) ");

            mDialogState = savedInstanceState.getInt(DIALOG_STATE);
            mPin = savedInstanceState.getString(DIALOG_PIN);
            mError = savedInstanceState.getString(DIALOG_ERROR);
            mToState = savedInstanceState.getBoolean(ENABLE_TO_STATE);

            Log.i(LOG_TAG, "mDialogState = " + mDialogState);

            // Restore inputted PIN code
            if (mDialogState == ICC_NEW_MODE) {
                mOldPin = savedInstanceState.getString(OLD_PINCODE);
            } else if (mDialogState == ICC_REENTER_MODE) {
                mOldPin = savedInstanceState.getString(OLD_PINCODE);
                mNewPin = savedInstanceState.getString(NEW_PINCODE);
            }

        }

        else if (savedInstanceState.containsKey(DIALOG_PERSO_STATE)) {
            Log.i(LOG_TAG, "savedInstanceState.containsKey(DIALOG_PERSO_STATE)");

            mDialogPersoState = savedInstanceState.getInt(DIALOG_PERSO_STATE);
            mPerso = savedInstanceState.getString(DIALOG_PERSO);
            mToStatePerso = savedInstanceState
                    .getBoolean(ENABLE_TO_PERSO_STATE);

            Log.i(LOG_TAG, "mDialogPersoState = " + mDialogPersoState);

            if (mDialogPersoState == PERSO_NEW_MODE) {
                mOldPerso = savedInstanceState.getString(OLD_PERSOCODE);
            } else if (mDialogPersoState == PERSO_REENTER_MODE) {
                mOldPerso = savedInstanceState.getString(OLD_PERSOCODE);
                mNewPerso = savedInstanceState.getString(NEW_PERSOCODE);
            }

        }
    }

    private void updatePreferences() { // Together PIN&PERSO
        Log.i(LOG_TAG, "updatePreferences()  ");

        mPinToggle.setChecked(mPhone.getIccCard().getIccLockEnabled());

        if (!(Config.LGU.equals(Config.getOperator()))) {
            mPersoToggle.setChecked(LGUiccManager.getProperty(
                    "pref.usim_perso_locked", "0").equals("1"));
            Log.i(LOG_TAG, "  mToStatePerso " + mToStatePerso);
            Log.i(LOG_TAG,
                    "  mPersoToggle.isChecked() " + mPersoToggle.isChecked());
        }
    }

    @Override
    protected void onResume() { // Together PIN&PERSO

        Log.i(LOG_TAG, "onResume()  ");

        super.onResume();

        if (!sContactsLoaded) {
            if (sContactsLoaded_first) {
                Log.i(LOG_TAG, "onCreateDialog(SIM_LOADING_CONTACTS) ");
                showDialog(SIM_LOADING_CONTACTS);
                sContactsLoaded_first = false;
            }
        }

        Log.d(LOG_TAG, "onResume sContactsLoaded " + sContactsLoaded);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_REFRESH_SIMSEARCH_STATUS);
        intentFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);

        registerReceiver(mSimStateReceiver, intentFilter);

        if (mDialogState != OFF_MODE) {
            Log.i(LOG_TAG, "mDialogState != OFF_MODE  ");
            showPinDialog();
        } else if (mDialogPersoState != PERSO_OFF_MODE) {
            Log.i(LOG_TAG, " mDialogPersoState != PERSO_OFF_MODE  ");
            showPersoDialog();
        }
        if (mDialogState == OFF_MODE) {
            Log.i(LOG_TAG, "mDialogState == OFF_MODE  ");
            resetDialogState();
        }

        if (!(Config.LGU.equals(Config.getOperator()))
                && (mDialogPersoState == PERSO_OFF_MODE)) {
            Log.i(LOG_TAG, "mDialogPersoState == PERSO_OFF_MOD  ");
            resetPersoDialogState();
        }
    }

    @Override
    protected void onPause() { // Together PIN&PERSO
        Log.i(LOG_TAG, "onPause()    ");
        super.onPause();
        unregisterReceiver(mSimStateReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) { // Together PIN&PERSO
        // Need to store this state for slider open/close
        // There is one case where the dialog is popped up by the preference
        // framework. In that case, let the preference framework store the
        // dialog state. In other cases, where this activity manually launches
        // the dialog, store the state of the dialog.

        Log.i(LOG_TAG, "onSaveInstanceState()    ");

        if (mPinDialog.isDialogOpen()) {
            Log.i(LOG_TAG, "mPinDialog.isDialogOpen())    ");
            out.putInt(DIALOG_STATE, mDialogState);
            out.putString(DIALOG_PIN, mPinDialog.getEditText().getText()
                    .toString());
            out.putString(DIALOG_ERROR, mError);
            out.putBoolean(ENABLE_TO_STATE, mToState);
            Log.i(LOG_TAG, "mDialogState  =  " + mDialogState);
            if (mDialogState == ICC_NEW_MODE) {
                out.putString(OLD_PINCODE, mOldPin);
            } else if (mDialogState == ICC_REENTER_MODE) {
                out.putString(OLD_PINCODE, mOldPin);
                out.putString(NEW_PINCODE, mNewPin);
            }

        }

        else if (!Config.LGU.equals(Config.getOperator())
                && mPersoDialog.isDialogOpen()) {
            Log.i(LOG_TAG, "mPersoDialog.isDialogOpen()    ");
            out.putInt(DIALOG_PERSO_STATE, mDialogState);
            out.putString(DIALOG_PERSO, mPinDialog.getEditText().getText()
                    .toString());
            out.putString(DIALOG_PERSO_ERROR, mError);
            out.putBoolean(ENABLE_TO_PERSO_STATE, mToState);

            Log.i(LOG_TAG, "mDialogPersoState  =  " + mDialogPersoState);

            if (mDialogPersoState == PERSO_NEW_MODE) {
                out.putString(OLD_PERSOCODE, mOldPerso);

            } else if (mDialogPersoState == PERSO_REENTER_MODE) {
                out.putString(OLD_PERSOCODE, mOldPerso);
                out.putString(NEW_PERSOCODE, mNewPerso);

            }

        } else {
            Log.i(LOG_TAG, " super.onSaveInstanceState(out) ");
            super.onSaveInstanceState(out);
        }
    }

    @Override
    protected Dialog onCreateDialog(int aId) {
        Log.i(LOG_TAG, " onCreateDialog() ");
        Dialog dialog = null;

        if (aId == SIM_LOADING_CONTACTS) { // DIALOG_ID_SIM_LOADING_CONTACTS
            if (progress != null) {
                progress.setMessage(mRes
                        .getString(R.string.sim_loading_contacts));
                progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        IccLockSettings_kr.this.finish();
                    }
                });
                dialog = progress;
            }
        }
        return dialog;
    }

    private void showPinDialog() {
        Log.i(LOG_TAG, " showPinDialog() ");

        if (mDialogState == OFF_MODE) {
            Log.i(LOG_TAG, " mDialogState == OFF_MODE");
            return;
        }
        setDialogValues();

        if (mError != null) {
            Log.i(LOG_TAG, "showPersoDialog mError != null " + mError);
            mError = null;
            if (!(Config.SKT.equals(Config.getOperator()) && (mDialogState == ICC_NEW_MODE))) {
                mPinDialog.showPinDialog(); // EditPinPreference_kr
                Log.i(LOG_TAG, "mPinDialog.showPinDialog() ");

            }
        } else {
            mPinDialog.showPinDialog(); // EditPinPreference_kr
            Log.i(LOG_TAG, "mPinDialog.showPinDialog() ");
        }
    }

    private void showPersoDialog() {
        Log.i(LOG_TAG, " showPersoDialog() ");

        if (mDialogPersoState == PERSO_OFF_MODE) {
            Log.i(LOG_TAG, " mDialogPersoState == PERSO_OFF_MODE");

            return;
        }
        setPersoDialogValues();

        if (mError != null) {
            Log.i(LOG_TAG, "showPersoDialog mError != null " + mError);
            mError = null;
            if (!(Config.SKT.equals(Config.getOperator()) && (mDialogPersoState == PERSO_NEW_MODE))) {
                Log.i(LOG_TAG, "mPersoDialog.showPinDialog() ");
                mPersoDialog.showPinDialog(); // EditPinPreference_kr
            }
        } else {
            Log.i(LOG_TAG, "mPersoDialog.showPinDialog() ");
            mPersoDialog.showPinDialog(); // EditPinPreference_kr
        }
    }

    private void setDialogValues() {
        Log.i(LOG_TAG, " setDialogValues() ");
        // LGE_UICC_S, [USIM_Patch_2059][KOR] eunju.choi, 2015-02-16, Apply KOR USIM Setting label integration for GUI 4.2. {
        int attempts = new LGUiccCard().getPin1RetryCount();
        String remain = "\n"
            + mRes.getString(R.string.sp_sim_lock_remain_NORMAL,
                    String.format(Locale.getDefault(), "%d", attempts));
        // LGE_UICC_E, [USIM_Patch_2059][KOR] }
        mPinDialog.setText(mPin);
        String message = "";
        Log.i(LOG_TAG, " mDialogState :" + mDialogState);

        if (mDialogState == ICC_LOCK_MODE) {
            mPinDialog.setDialogTitle(mToState
                    ? pinTitle.get(ICC_LOCK_MODE)
                    : pinTitle.get(ICC_UNLOCK_MODE));
        // LGE_UICC_S, [USIM_Patch_2059][KOR] eunju.choi, 2015-02-16, Apply KOR USIM Setting label integration for GUI 4.2. {
                message = mToState
                    ? pinMsg.get(ICC_LOCK_MODE) + remain
                    : pinMsg.get(ICC_UNLOCK_MODE) + remain;
        } else if (mDialogState == ICC_OLD_MODE) {
                message = pinMsg.get(mDialogState) + remain;
                mPinDialog.setDialogTitle(pinTitle.get(mDialogState));
        } else if ( (mDialogState == ICC_NEW_MODE)
                      || (mDialogState == ICC_REENTER_MODE)) {
                mPinDialog.setDialogTitle(pinTitle.get(mDialogState));
                message = pinMsg.get(mDialogState);
        }
        // LGE_UICC_E, [USIM_Patch_2059][KOR] }
        if (mError != null) {
            Log.i(LOG_TAG, " mError :" + mError);
            if ((Config.KT.equals(Config.getOperator()) || Config.LGU
                    .equals(Config.getOperator()))
                    && (mDialogState == ICC_NEW_MODE)) {
                message = mError;
                mError = null;
            } else if (Config.SKT.equals(Config.getOperator())
                    && (mDialogState == ICC_NEW_MODE)) {
                new AlertDialog.Builder(this)
                        .setMessage(mError)
                        .setNeutralButton(R.string.sim_btn_ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0,
                                            int arg1) {
                                        mPinDialog.showPinDialog(); // EditPinPreference_kr
                                    }
                                }).show();
            } else {
                message = mError + "\n" + message;
                mError = null;
            }
        }
        mPinDialog.setDialogMessage(message);
    }

    private void setPersoDialogValues() {
        Log.i(LOG_TAG, " setPersoDialogValues()");

        mPersoDialog.setText(mPerso);

        String message = "";
        Log.i(LOG_TAG, " mDialogPersoState : " + mDialogPersoState);

        if (mDialogPersoState == PERSO_LOCK_MODE) {
            mPersoDialog.setDialogTitle(mToStatePerso ? persoTitle
                    .get(PERSO_LOCK_MODE) : persoTitle.get(PERSO_UNLOCK_MODE));
            message = mToStatePerso ? persoMsg.get(PERSO_LOCK_MODE) : persoMsg
                    .get(PERSO_UNLOCK_MODE);
        } else if ((mDialogPersoState == PERSO_OLD_MODE)
                || (mDialogPersoState == PERSO_NEW_MODE)
                || (mDialogPersoState == PERSO_REENTER_MODE)) {
            mPersoDialog.setDialogTitle(persoTitle.get(mDialogPersoState));
            message = persoMsg.get(mDialogPersoState);
        }
        if (mError != null) {
            Log.i(LOG_TAG, " mError :" + mError);
            if (Config.KT.equals(Config.getOperator())
                    && (mDialogPersoState == PERSO_NEW_MODE)) {
                message = mError;
                mError = null;
            } else if (Config.SKT.equals(Config.getOperator())
                    && (mDialogPersoState == PERSO_NEW_MODE)) {
                new AlertDialog.Builder(this)
                        .setMessage(mError)
                        .setNeutralButton(R.string.sim_btn_ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0,
                                            int arg1) {
                                        mPersoDialog.showPinDialog(); // EditPinPreference_kr
                                    }
                                }).show();
                Log.i(LOG_TAG, " AlertDialog.Builder(this) :" + mError);
            } else {
                message = mError + "\n" + message;
                mError = null;

            }
        }
        mPersoDialog.setDialogMessage(message);
    }

    public void onPinEntered(EditPinPreference_kr preference,
            boolean positiveResult) {
        Log.i(LOG_TAG, " onPinEntered()");

        if (!positiveResult) { // cancel or back key
            Log.i(LOG_TAG, " !positiveResult");
            resetDialogState();
            return;
        }

        mPin = preference.getText();

        Log.i(LOG_TAG, "mDialogState " + mDialogState);
        switch (mDialogState) {
            case ICC_LOCK_MODE :
                tryChangeIccLockState();
                break;
            case ICC_OLD_MODE :
                tryEnterSimChange();
                break;
            case ICC_NEW_MODE :
                mNewPin = mPin;
                mDialogState = ICC_REENTER_MODE;
                mPin = null;
                showPinDialog();
                break;
            case ICC_REENTER_MODE :
                if (!mPin.equals(mNewPin)) {
                    mError = mRes.getString(R.string.sim_pin_change_enter_new_retry_dialog_message); // LGE_UICC_S, [USIM_Patch_2059][KOR] eunju.choi, 2015-02-16, Apply KOR USIM Setting label integration for GUI 4.2.
                    mDialogState = ICC_NEW_MODE;
                    mPin = null;
                    showPinDialog();
                } else {
                    mError = null;
                    tryChangePin();
                }
                break;
            default :
                break;
        }
    }

    public void onPersoEntered(EditPinPreference_kr preference,
            boolean positiveResult) {
        Log.i(LOG_TAG, " onPersoEntered()");
        if (!positiveResult) {
            Log.i(LOG_TAG, " !positiveResult");
            resetPersoDialogState();
            return;
        }

        mPerso = preference.getText();

        Log.i(LOG_TAG, "mDialogPersoState " + mDialogPersoState);
        switch (mDialogPersoState) {
            case PERSO_LOCK_MODE :
                tryChangePersoLockState();
                break;
            case PERSO_OLD_MODE :
                tryEnterPesoChange();
                break;
            case PERSO_NEW_MODE :
                mNewPerso = mPerso;
                mDialogPersoState = PERSO_REENTER_MODE;
                mPerso = null;
                showPersoDialog();
                break;
            case PERSO_REENTER_MODE :
                if (!mPerso.equals(mNewPerso)) {
                    mError = mRes
                            .getString(R.string.sim_perso_controlkey_change_enter_new_retry_dialog_message);
                    mDialogPersoState = PERSO_NEW_MODE;
                    mPerso = null;
                    showPersoDialog();
                } else {
                    mError = null;
                    tryChangePerso();
                }
                break;
            default :
                break;
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) { // Together PIN&PERSO

        Log.i(LOG_TAG, "onPreferenceTreeClick");

        if (preference == mPinToggle) {
            Log.i(LOG_TAG, "preference == mPinToggle");
            // Get the new, preferred state
            mToState = mPinToggle.isChecked();
            // Flip it back and pop up pin dialog
            mPinToggle.setChecked(!mToState);
            mDialogState = ICC_LOCK_MODE;

            showPinDialog();

        } else if (preference == mPinDialog) {
            Log.i(LOG_TAG, "preference == mPinDialog");
            mDialogState = ICC_OLD_MODE;

            if (!mPinToggle.isChecked()) {
                mPinDialog.dismissDialog(); // pinDialog dismiss

                new AlertDialog.Builder(this)
                        .setTitle(
                                R.string.sim_pin_change_is_not_available_because_lock_is_note)
                        .setMessage(
                                R.string.sim_pin_change_is_not_available_because_lock_is_disabled)
                        .setNeutralButton(R.string.sim_btn_ok, null).show();
            }
            return false;
        } else if (preference == mPersoToggle) {
            Log.i(LOG_TAG, "preference == mPersoToggle");
            mToStatePerso = mPersoToggle.isChecked();
            mPersoToggle.setChecked(!mToStatePerso);
            mDialogPersoState = PERSO_LOCK_MODE;

            Log.i(LOG_TAG, "  mToStatePerso " + mToStatePerso);
            Log.i(LOG_TAG,
                    "  mPersoToggle.isChecked() " + mPersoToggle.isChecked());

            showPersoDialog();
        } else if (preference == mPersoDialog) {
            Log.i(LOG_TAG, "preference == mPersoDialog");
            mDialogPersoState = PERSO_OLD_MODE;

            return false;
        }
        return true;
    }

    private void tryChangeIccLockState() { // ICC_LOCK_MODE
        Log.i(LOG_TAG, "tryChangeIccLockState()");
        // Try to change icc lock. If it succeeds, toggle the lock state and
        // reset dialog state. Else inject error message and show dialog again.
        Message callback = Message
                .obtain(mHandler, MSG_ENABLE_ICC_PIN_COMPLETE);
        mPhone.getIccCard().setIccLockEnabled(mToState, mPin, callback);
    }

    private void tryEnterSimChange() { // ICC_OLD_MODE
        Log.i(LOG_TAG, "tryEnterSimChange()");

        if (!Config.KT.equals(Config.getOperator())) // SKT + U+
        {
            Message callback = Message.obtain(mHandler, MSG_SIM_CHNAGE_ENTER);
            mPhone.getIccCard().changeIccLockPassword(mPin, mPin, callback);
        } else // KT
        {
            // for korea(KT) spec, verify after PIN1 -> NEW PIN1 -> REENTER PIN1
            mHandler.sendMessage(Message.obtain(mHandler, MSG_SIM_CHNAGE_ENTER,
                    1, 1));
        }

    }

    private void tryChangePin() { // ICC_REENTER_MODE
        Log.i(LOG_TAG, "tryChangePin()");
        Message callback = Message
                .obtain(mHandler, MSG_CHANGE_ICC_PIN_COMPLETE);
        mPhone.getIccCard().changeIccLockPassword(mOldPin, mNewPin, callback);
    }

    private void tryChangePersoLockState() { // PERSO_LOCK_MODE
        Log.i(LOG_TAG, "tryChangePersoLockState()");

        if (mPerso.equals(LGUiccManager.getProperty(
                "pref.usim_perso_control_key", "0"))) {
            if (LGUiccManager.getProperty("pref.usim_perso_locked", "0")
                    .equals("1")) {
                LGUiccManager.setProperty("pref.usim_perso_locked", "0");
                Log.i(LOG_TAG, "set : pref.usim_perso_locked : 0");
                LGUiccManager.setProperty("pref.usim_perso_imsi",
                        "000000000000000");
                mHandler.sendMessage(Message.obtain(mHandler,
                        MSG_ENABLE_ICC_PERSO_COMPLETE, 1, 1)); // true perso
            } else if (LGUiccManager.getProperty("pref.usim_perso_locked", "0")
                    .equals("0")) {
                LGUiccManager.setProperty("pref.usim_perso_locked", "1");
                Log.i(LOG_TAG, "set : pref.usim_perso_locked : 1");
                LGUiccManager.setProperty("pref.usim_perso_imsi",
                        mPhone.getSubscriberId());
                mHandler.sendMessage(Message.obtain(mHandler,
                        MSG_ENABLE_ICC_PERSO_COMPLETE, 1, 1)); // true perso
            } else {
                mHandler.sendMessage(Message.obtain(mHandler,
                        MSG_ENABLE_ICC_PERSO_COMPLETE, 0, 0)); // wrong perso
            }
        } else {
            mHandler.sendMessage(Message.obtain(mHandler,
                    MSG_ENABLE_ICC_PERSO_COMPLETE, 0, 0)); // wrong perso
        }
    }

    private void tryEnterPesoChange() { // PERSO_OLD_MODE
        Log.i(LOG_TAG, "tryEnterPesoChange()");
        if (mPerso.equals(LGUiccManager.getProperty(
                "pref.usim_perso_control_key", "0"))) {
            mHandler.sendMessage(Message.obtain(mHandler,
                    MSG_PERSO_CHNAGE_ENTER, 1, 1)); // true perso
        } else {
            mHandler.sendMessage(Message.obtain(mHandler,
                    MSG_PERSO_CHNAGE_ENTER, 0, 0)); // wrong perso
        }

    }

    private void tryChangePerso() { // PERSO_REENTER_MODE
        Log.i(LOG_TAG, "tryChangePerso()");
        if (LGUiccManager.setProperty("pref.usim_perso_control_key", mNewPerso)) {
            mHandler.sendMessage(Message.obtain(mHandler,
                    MSG_CHANGE_ICC_PERSO_COMPLETE, 1, 1)); // true perso
        } else {
            mHandler.sendMessage(Message.obtain(mHandler,
                    MSG_CHANGE_ICC_PERSO_COMPLETE, 0, 0)); // true perso
        }
    }

    private void iccLockChanged(boolean success) { // ICC_LOCK_MODE
        Log.i(LOG_TAG, "iccLockChanged() = " + success);
        if (success) {
            if (mToState) {
                Toast.makeText(
                        this,
                        mRes.getString(R.string.sim_lock_enabled_toast_message),
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(
                        this,
                        mRes.getString(R.string.sim_lock_disabled_toast_message),
                        Toast.LENGTH_SHORT).show();
            }
            mPinToggle.setChecked(mToState);
        } else {
            displayRetryCounter();
        }
        resetDialogState();
    }

    private void persoLockChanged(boolean success) { // PERSO_LOCK_MODE
        Log.i(LOG_TAG, "persoLockChanged() = " + success);
        Log.i(LOG_TAG, "  mToStatePerso " + mToStatePerso);

        if (success) { // perso input success
            if (mToStatePerso) { // perso enable
                Toast.makeText(
                        this,
                        mRes.getString(R.string.sim_perso_lock_enabled_toast_message),
                        Toast.LENGTH_SHORT).show();
            } else { // perso disable
                Toast.makeText(
                        this,
                        mRes.getString(R.string.sim_perso_lock_disabled_toast_message),
                        Toast.LENGTH_SHORT).show();
            }
            mPersoToggle.setChecked(mToStatePerso);

            Log.i(LOG_TAG,
                    "  mPersoToggle.isChecked() " + mPersoToggle.isChecked());
        } else // perso input fail
        {
            new AlertDialog.Builder(this)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setTitle(
                            getResources()
                                    .getString(
                                            R.string.sim_perso_controlkey_change_incorrect_old_dialog_title))
                    .setMessage(
                            getResources()
                                    .getString(
                                            R.string.sim_perso_controlkey_change_incorrect_old_dialog_message))
                    .setPositiveButton(android.R.string.yes, null).show();
        }
        resetPersoDialogState();
    }

    private void iccEnterPinChange(boolean success) { // ICC_OLD_MODE
        Log.i(LOG_TAG, "iccEnterPinChange() = " + success);
        if (success) {
            mOldPin = mPin;
            mDialogState = ICC_NEW_MODE;
            mError = null;
            mPin = null;
            showPinDialog();
        } else {
            displayRetryCounter();
            resetDialogState();
        }
    }

    private void iccEnterPersoChange(boolean success) { // PERSO_OLD_MODE
        Log.i(LOG_TAG, "iccEnterPersoChange() = " + success);
        if (success) {
            mOldPerso = mPerso;
            mDialogPersoState = PERSO_NEW_MODE;
            mError = null;
            mPerso = null;
            showPersoDialog();
        } else {
            new AlertDialog.Builder(this)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setTitle(
                            getResources()
                                    .getString(
                                            R.string.sim_perso_controlkey_change_incorrect_old_dialog_title))
                    .setMessage(
                            getResources()
                                    .getString(
                                            R.string.sim_perso_controlkey_change_incorrect_old_dialog_message))
                    .setPositiveButton(android.R.string.yes, null).show();

            resetPersoDialogState();
        }
    }

    private void iccPinChanged(boolean success) {
        Log.i(LOG_TAG, "iccPinChanged() = " + success);
        if (!success) {
            // If the exception is REQUEST_NOT_SUPPORTED then change pin
            // couldn't
            // happen because SIM lock is not enabled.
            displayRetryCounter();
        } else {
            Toast.makeText(
                    this,
                    mRes.getString(R.string.sim_pin_change_succeed_toast_message),
                    Toast.LENGTH_SHORT).show();
        }
        resetDialogState();
    }

    private void iccPersoChanged(boolean success) { // PERSO_REENTER_MODE
        Log.i(LOG_TAG, "iccPersoChanged() = " + success);
        if (!success) {
            Toast.makeText(
                    this,
                    mRes.getString(R.string.sim_perso_controlkey_change_failed_toast_message),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(
                    this,
                    mRes.getString(R.string.sim_perso_controlkey_change_succeed_toast_message),
                    Toast.LENGTH_SHORT).show();
        }
        resetPersoDialogState();
    }

    private void resetDialogState() {
        Log.i(LOG_TAG, "resetDialogState() ");
        mError = null;
        mDialogState = ICC_OLD_MODE; // Default for when Change PIN is clicked
        mPin = "";
        setDialogValues();
        mDialogState = OFF_MODE;
    }

    private void resetPersoDialogState() {
        Log.i(LOG_TAG, "resetPersoDialogState() ");
        mError = null;
        mDialogPersoState = PERSO_OLD_MODE; // Default for when Change PIN is
                                            // clicked
        mPerso = "";
        setPersoDialogValues();
        mDialogPersoState = PERSO_OFF_MODE;
    }

    private void displayRetryCounter() {
        Log.i(LOG_TAG, "displayRetryCounter() ");
        int attempts = new LGUiccCard().getPin1RetryCount();

        if (attempts > 0) {
            // LGE_UICC_S, [USIM_Patch_2059][KOR] eunju.choi, 2015-02-16, Apply KOR USIM Setting label integration for GUI 4.2. {
            new AlertDialog.Builder(this)
                .setTitle(
                getResources().getString(
                R.string.sim_lock_failed_dialog_title))
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setMessage(stringResourceFormat(
                R.string.sim_lock_failed_dialog_message,
                attempts, MAX_SIM_RETRY_COUNT, MAX_SIM_RETRY_COUNT))
                    .setPositiveButton(android.R.string.yes, null).show();
            // LGE_UICC_E, [USIM_Patch_2059][KOR] }
        } else {
            Log.d(LOG_TAG, "PIN Retry 0, pref.sim_err_popup_msg, set 1");
            LGUiccManager.setProperty("pref.sim_err_popup_msg", "1");
        }
    }

    private String stringResourceFormat(int aResource, Object... aArgs) {
        String format = String.format(mRes.getString(aResource), aArgs);
        Log.d(LOG_TAG, "stringResourceFormat " + format);
        return format;
    }

    public boolean onActionItemClicked(ActionMode arg0, MenuItem arg1) { // Together
                                                                         // PIN&PERSO
        // TODO Auto-generated method stub
        return false;
    }

    public boolean onCreateActionMode(ActionMode arg0, Menu arg1) { // Together
                                                                    // PIN&PERSO
        // TODO Auto-generated method stub
        return false;
    }

    public void onDestroyActionMode(ActionMode arg0) { // Together PIN&PERSO
        // TODO Auto-generated method stub

    }

    public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) { // Together
                                                                     // PIN&PERSO
        // TODO Auto-generated method stub
        return false;
    }

    private void setResPinString() {
        pinTitle = new HashMap<Integer, String>();
        pinMsg = new HashMap<Integer, String>();

        pinTitle.put(ICC_LOCK_MODE, mRes
                .getString(R.string.sim_lock_enter_cur_for_lock_dialog_title));
        pinTitle.put(ICC_UNLOCK_MODE, mRes
                .getString(R.string.sim_lock_enter_cur_for_unlock_dialog_title));
        pinTitle.put(ICC_OLD_MODE,
                mRes.getString(R.string.sim_pin_change_enter_old_dialog_title));
        pinTitle.put(ICC_NEW_MODE,
                mRes.getString(R.string.sim_pin_change_enter_new_dialog_title));
        pinTitle.put(ICC_REENTER_MODE,
                mRes.getString(R.string.sim_pin_change_reenter_dialog_title));

       // LGE_UICC_S, [USIM_Patch_2059][KOR] eunju.choi, 2015-02-16, Apply KOR USIM Setting label integration for GUI 4.2. {
        pinMsg.put(ICC_LOCK_MODE, mRes.getString(R.string.sim_lock_enter_cur_for_lock_dialog_message));
        pinMsg.put(ICC_UNLOCK_MODE, mRes.getString(R.string.sim_lock_enter_cur_for_unlock_dialog_message));
        pinMsg.put(ICC_OLD_MODE, mRes.getString(R.string.sim_pin_change_enter_old_dialog_message));
        pinMsg.put(ICC_NEW_MODE, mRes.getString(R.string.sim_pin_change_enter_new_dialog_message));
        pinMsg.put(ICC_REENTER_MODE,mRes.getString(R.string.sim_pin_change_reenter_dialog_message));
       // LGE_UICC_E, [USIM_Patch_2059][KOR] }
    }

    private void setResPersoString() {
        persoTitle = new HashMap<Integer, String>();
        persoMsg = new HashMap<Integer, String>();
        persoTitle
                .put(PERSO_LOCK_MODE,
                        mRes.getString(R.string.sim_perso_lock_enter_cur_for_lock_dialog_title));
        persoTitle
                .put(PERSO_UNLOCK_MODE,
                        mRes.getString(R.string.sim_perso_lock_enter_cur_for_unlock_dialog_title));
        persoTitle
                .put(PERSO_OLD_MODE,
                        mRes.getString(R.string.sim_perso_controlkey_change_enter_old_dialog_title));
        persoTitle
                .put(PERSO_NEW_MODE,
                        mRes.getString(R.string.sim_perso_controlkey_change_enter_new_dialog_title));
        persoTitle
                .put(PERSO_REENTER_MODE,
                        mRes.getString(R.string.sim_perso_controlkey_change_reenter_dialog_title));

        persoMsg.put(
                PERSO_LOCK_MODE,
                mRes.getString(R.string.sim_perso_lock_enter_cur_for_lock_dialog_message));
        persoMsg.put(
                PERSO_UNLOCK_MODE,
                mRes.getString(R.string.sim_perso_lock_enter_cur_for_unlock_dialog_message));
        persoMsg.put(
                PERSO_OLD_MODE,
                mRes.getString(R.string.sim_perso_controlkey_change_enter_old_dialog_message));
        persoMsg.put(
                PERSO_NEW_MODE,
                mRes.getString(R.string.sim_perso_controlkey_change_enter_new_dialog_message));
        persoMsg.put(
                PERSO_REENTER_MODE,
                mRes.getString(R.string.sim_perso_controlkey_change_reenter_dialog_message));
    }

}
// LGE_UICC_E, [USIM_Patch 2055][KR]
