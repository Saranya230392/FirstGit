/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
import android.os.SystemProperties;

import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
//[S][2012.03.08][jin850607.hong] No ACTION Bar
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
//[E][2012.03.08][jin850607.hong] No ACTION Bar
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.lgesetting.Config.Config;

import com.lge.uicc.LGUiccCard;
import com.lge.uicc.LGUiccManager;

import java.util.Locale;

/**
 * Implements the preference screen to enable/disable ICC lock and
 * also the dialogs to change the ICC PIN. In the former case, enabling/disabling
 * the ICC lock will prompt the user for the current PIN.
 * In the Change PIN case, it prompts the user for old pin, new pin and new pin
 * again before attempting to change it. Calls the SimCard interface to execute
 * these operations.
 *
 */
public class IccLockSettings extends PreferenceActivity
        implements EditPinPreference.OnPinEnteredListener, Callback {

    private static final String LOG_TAG = "IccLockSettings";

    private static final int OFF_MODE = 0;
    // State when enabling/disabling ICC lock
    private static final int ICC_LOCK_MODE = 1;
    // State when entering the old pin
    private static final int ICC_OLD_MODE = 2;
    // State when entering the new pin - first time
    private static final int ICC_NEW_MODE = 3;
    // State when entering the new pin - second time
    private static final int ICC_REENTER_MODE = 4;

    // Keys in xml file
    private static final String PIN_DIALOG = "sim_pin";
    private static final String PIN_TOGGLE = "sim_toggle";
    // Keys in icicle
    private static final String DIALOG_STATE = "dialogState";
    private static final String DIALOG_PIN = "dialogPin";
    private static final String DIALOG_ERROR = "dialogError";
    private static final String ENABLE_TO_STATE = "enableState";

    // Save and restore inputted PIN code when configuration changed
    // (ex. portrait<-->landscape) during change PIN code
    private static final String OLD_PINCODE = "oldPinCode";
    private static final String NEW_PINCODE = "newPinCode";

    private static final int MIN_PIN_LENGTH = 4;
    private static final int MAX_PIN_LENGTH = 8;
    // Which dialog to show next when popped up
    private int mDialogState = OFF_MODE;

    // [S][2012.2.29][jin850607.hong] for VDF, double sim
    private int mCount;
    // [S][2012.2.29][jin850607.hong] for VDF, double sim
    private String mPin;
    private String mOldPin;
    private String mNewPin;
    private String mError;
    // Are we trying to enable or disable ICC lock?
    private boolean mToState;

    private Phone mPhone;

    private EditPinPreference mPinDialog;
    private CheckBoxPreference mPinToggle;

    private Resources mRes;

    // For async handler to identify request type
    private static final int MSG_ENABLE_ICC_PIN_COMPLETE = 100;
    private static final int MSG_CHANGE_ICC_PIN_COMPLETE = 101;
    private static final int MSG_SIM_STATE_CHANGED = 102;
    private static final int MSG_SIM_CHNAGE_ENTER = 103;

    // For replies from IccCard interface
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar = (AsyncResult)msg.obj;
            switch (msg.what) {
            case MSG_ENABLE_ICC_PIN_COMPLETE:
                iccLockChanged(ar.exception == null);
                break;
            case MSG_CHANGE_ICC_PIN_COMPLETE:
                iccPinChanged(ar.exception == null);
                break;
            case MSG_SIM_STATE_CHANGED:
                updatePreferences();
                break;
            case MSG_SIM_CHNAGE_ENTER:
                iccEnterPinChange(ar.exception == null);
                break;
            default:
                break;
            }

            return;
        }
    };

    private final BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)) {
                mHandler.sendMessage(mHandler.obtainMessage(MSG_SIM_STATE_CHANGED));
            }
        }
    };

    // For top-level settings screen to query
    static boolean isIccLockEnabled() {
        return PhoneFactory.getDefaultPhone().getIccCard().getIccLockEnabled();
    }

    static String getSummary(Context context) {
        Resources res = context.getResources();
        String summary = isIccLockEnabled()
                ? res.getString(R.string.sim_lock_on)
                : res.getString(R.string.sim_lock_off);
        return summary;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Utils.isMonkeyRunning()) {
            finish();
            return;
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        addPreferencesFromResource(R.xml.sim_lock_settings);

        mPinDialog = (EditPinPreference)findPreference(PIN_DIALOG);
        mPinToggle = (CheckBoxPreference)findPreference(PIN_TOGGLE);
        if (savedInstanceState != null && savedInstanceState.containsKey(DIALOG_STATE)) {
            mDialogState = savedInstanceState.getInt(DIALOG_STATE);
            mPin = savedInstanceState.getString(DIALOG_PIN);
            mError = savedInstanceState.getString(DIALOG_ERROR);
            mToState = savedInstanceState.getBoolean(ENABLE_TO_STATE);

            // Restore inputted PIN code
            switch (mDialogState) {
            case ICC_NEW_MODE:
                mOldPin = savedInstanceState.getString(OLD_PINCODE);
                break;

            case ICC_REENTER_MODE:
                mOldPin = savedInstanceState.getString(OLD_PINCODE);
                mNewPin = savedInstanceState.getString(NEW_PINCODE);
                break;

            case ICC_LOCK_MODE:
            case ICC_OLD_MODE:
            default:
                break;
            }
        }

        mPinDialog.setOnPinEnteredListener(this);

        // Don't need any changes to be remembered
        getPreferenceScreen().setPersistent(false);

        mPhone = PhoneFactory.getDefaultPhone();
        mRes = getResources();
        updatePreferences();
    }

    private void updatePreferences() {
        mPinToggle.setChecked(mPhone.getIccCard().getIccLockEnabled());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ACTION_SIM_STATE_CHANGED is sticky, so we'll receive current state after this call,
        // which will call updatePreferences().
        final IntentFilter filter = new IntentFilter(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        registerReceiver(mSimStateReceiver, filter);

        if (Config.DCM.equals(Config.getOperator())) {
            if ("PERM_DISABLED".equals(SystemProperties.get("gsm.sim.state"))) {
                Dialog mPukDialog = new AlertDialog.Builder(this)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle(R.string.sp_dlg_note_NORMAL)
                        .setMessage(getResources().getString(R.string.sp_sim_puk_popup_NORMAL))
                        .setPositiveButton(getResources().getString(R.string.dlg_ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub
                                        finish();
                                        dialog.dismiss();
                                    }
                                })
                        .show();
                mPukDialog.setCancelable(false);
                mPukDialog.setCanceledOnTouchOutside(false);
            }
        }

        if (mDialogState != OFF_MODE) {
            showPinDialog();
        } else {
            // Prep for standard click on "Change PIN"
            resetDialogState();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mSimStateReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        // Need to store this state for slider open/close
        // There is one case where the dialog is popped up by the preference
        // framework. In that case, let the preference framework store the
        // dialog state. In other cases, where this activity manually launches
        // the dialog, store the state of the dialog.
        if (mPinDialog.isDialogOpen()) {
            out.putInt(DIALOG_STATE, mDialogState);
            out.putString(DIALOG_PIN, mPinDialog.getEditText().getText().toString());
            out.putString(DIALOG_ERROR, mError);
            out.putBoolean(ENABLE_TO_STATE, mToState);

            // Save inputted PIN code
            switch (mDialogState) {
            case ICC_NEW_MODE:
                out.putString(OLD_PINCODE, mOldPin);
                break;

            case ICC_REENTER_MODE:
                out.putString(OLD_PINCODE, mOldPin);
                out.putString(NEW_PINCODE, mNewPin);
                break;

            case ICC_LOCK_MODE:
            case ICC_OLD_MODE:
            default:
                break;
            }
        } else {
            super.onSaveInstanceState(out);
        }
    }

    private void showPinDialog() {
        if (mDialogState == OFF_MODE) {
            return;
        }
        setDialogValues();

        mPinDialog.showPinDialog();
    }

    private void setDialogValues() {
        int attempts = new LGUiccCard().getPin1RetryCount();

        // [S][2012.03.26][changyu0218.lee@lge.com] Change SIM lock UI
        String description = mRes.getString(R.string.sp_sim_lock_len_des_NORMAL);
        String remain = "\n"
                + mRes.getString(R.string.sp_sim_lock_remain_NORMAL,
                        String.format(Locale.getDefault(), "%d", attempts));

        if (attempts == 1) {
            remain = "\n"
                    + mRes.getString(R.string.sp_sim_lock_remain_one_NORMAL,
                            String.format(Locale.getDefault(), "%d", attempts));
        }

        mPinDialog.setText(mPin);
        String message = "";
        switch (mDialogState) {
        case ICC_LOCK_MODE:
            message = mRes.getString(R.string.sim_enter_pin) + remain;
            /*
            mPinDialog.setDialogTitle(mToState
                    ? mRes.getString(R.string.sim_enable_sim_lock)
                    : mRes.getString(R.string.sim_disable_sim_lock));
            */
            mPinDialog.setDialogTitle(mRes.getString(R.string.sp_enter_current_pin_NORMAL));
            break;
        case ICC_OLD_MODE:
            message = mRes.getString(R.string.sim_enter_pin) + remain;
            mPinDialog.setDialogTitle(mRes.getString(R.string.sp_enter_current_pin_NORMAL));
            break;
        case ICC_NEW_MODE:
            message = description;
            mPinDialog.setDialogTitle(mRes.getString(R.string.sp_choose_lock_pin_NORMAL));
            break;
        case ICC_REENTER_MODE:
            message = null;
            mPinDialog.setDialogTitle(mRes.getString(R.string.sp_confirm_lock_pin_NORMAL));
            break;
        default:
            break;
        // [E][2012.03.26][changyu0218.lee@lge.com] Change SIM lock UI
        }
        if (mError != null) {
            message = mError + "\n" + message;
            mError = null;
        }
        mPinDialog.setDialogMessage(message);
    }

    public void onPinEntered(EditPinPreference preference, boolean positiveResult) {
        if (!positiveResult) {
            resetDialogState();
            return;
        }

        mPin = preference.getText();
        if (!reasonablePin(mPin)) {
            // inject error message and display dialog again
            mError = mRes.getString(R.string.sim_bad_pin);
            showPinDialog();
            return;
        }
        switch (mDialogState) {
        case ICC_LOCK_MODE:
            tryChangeIccLockState();
            break;
        case ICC_OLD_MODE:
            tryEnterSimChange();
            /*
            mOldPin = mPin;
            mDialogState = ICC_NEW_MODE;
            mError = null;
            mPin = null;
            showPinDialog();
            */
            break;
        case ICC_NEW_MODE:
            mNewPin = mPin;
            mDialogState = ICC_REENTER_MODE;
            mPin = null;
            showPinDialog();
            break;
        case ICC_REENTER_MODE:
            if (!mPin.equals(mNewPin)) {
                mError = mRes.getString(R.string.sim_pins_dont_match);
                mDialogState = ICC_NEW_MODE;
                mPin = null;
                showPinDialog();
            } else {
                mError = null;
                tryChangePin();
            }
            break;
        default:
            break;
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mPinToggle) {
            // Get the new, preferred state
            mToState = mPinToggle.isChecked();
            // Flip it back and pop up pin dialog
            mPinToggle.setChecked(!mToState);
            mDialogState = ICC_LOCK_MODE;
            showPinDialog();
        } else if (preference == mPinDialog) {
            mDialogState = ICC_OLD_MODE;
            return false;
        }
        return true;
    }

    private void tryEnterSimChange() {
        Message callback = Message.obtain(mHandler, MSG_SIM_CHNAGE_ENTER);
        mPhone.getIccCard().changeIccLockPassword(mPin, mPin, callback);
    }

    private void tryChangeIccLockState() {
        // Try to change icc lock. If it succeeds, toggle the lock state and
        // reset dialog state. Else inject error message and show dialog again.
        Message callback = Message.obtain(mHandler, MSG_ENABLE_ICC_PIN_COMPLETE);
        mPhone.getIccCard().setIccLockEnabled(mToState, mPin, callback);

    }

    private void iccLockChanged(boolean success) {
        //        if (ar.exception == null) {
        if (success) {
            // [S][2012.01.21][jin850607.hong@lge.com] Add sim remains
            if (mToState) {
                if (Config.SKT.equals(Config.getOperator())
                        || Config.LGU.equals(Config.getOperator())
                        || Config.KT.equals(Config.getOperator())) {
                    Toast.makeText(this, mRes.getString(R.string.sp_usim_lock_check_NORMAL),
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(this, mRes.getString(R.string.sp_sim_lock_check_NORMAL),
                            Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                if (Config.SKT.equals(Config.getOperator())
                        || Config.LGU.equals(Config.getOperator())
                        || Config.KT.equals(Config.getOperator())) {
                    Toast.makeText(this, mRes.getString(R.string.sp_usim_lock_uncheck_NORMAL),
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(this, mRes.getString(R.string.sp_sim_lock_uncheck_NORMAL),
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
            // [S][2012.01.21][jin850607.hong@lge.com] Add sim remains
            mPinToggle.setChecked(mToState);
        } else {
            displayRetryCounter(mRes.getString(R.string.sim_change_failed));
        }
        resetDialogState();
        //[changyu0218.lee] After entering PUK, fix a remaining count 0 problem in change PIN menu

        int attempts = new LGUiccCard().getPin1RetryCount();
        if (attempts == 0) {
            finish();
        }

        //[changyu0218.lee] After entering PUK, fix a remaining count 0 problem in change PIN menu
    }

    private void iccPinChanged(boolean success) {
        if (!success) {
            // If the exception is REQUEST_NOT_SUPPORTED then change pin couldn't
            // happen because SIM lock is not enabled.
            displayRetryCounter(mRes.getString(R.string.sim_change_failed));
        } else {
            Toast.makeText(this, mRes.getString(R.string.sim_change_succeeded),
                    Toast.LENGTH_SHORT)
                    .show();
        }
        resetDialogState();
        //[changyu0218.lee] After entering PUK, fix a remaining count 0 problem in change PIN menu

        int attempts = new LGUiccCard().getPin1RetryCount();
        if (attempts == 0) {
            finish();
        }

        //[changyu0218.lee] After entering PUK, fix a remaining count 0 problem in change PIN menu
    }

    private void iccEnterPinChange(boolean success) {

        Log.i(LOG_TAG, "iccEnterPinChange = " + success);

        if (success) {
            mOldPin = mPin;
            mDialogState = ICC_NEW_MODE;
            mError = null;
            mPin = null;
            showPinDialog();
        } else {
            displayRetryCounter(mRes.getString(R.string.sim_change_failed));
            resetDialogState();
        }
        int attempts = new LGUiccCard().getPin1RetryCount();
        if (attempts == 0) {
            finish();
        }

    }

    private void tryChangePin() {
        Message callback = Message.obtain(mHandler, MSG_CHANGE_ICC_PIN_COMPLETE);
        mPhone.getIccCard().changeIccLockPassword(mOldPin,
                mNewPin, callback);
    }

    private boolean reasonablePin(String pin) {
        if (pin == null || pin.length() < MIN_PIN_LENGTH || pin.length() > MAX_PIN_LENGTH) {
            //[S][2012.02.14][jin850607.hong@lge.com] sim error message
            Toast.makeText(this, mRes.getString(R.string.sp_sim_lock_few_numbers_NORMAL),
                    Toast.LENGTH_SHORT)
                    .show();
            //[E][2012.02.14][jin850607.hong@lge.com] sim error message
            return false;
        } else {
            return true;
        }
    }

    private void resetDialogState() {
        mError = null;
        mDialogState = ICC_OLD_MODE; // Default for when Change PIN is clicked
        mPin = "";
        setDialogValues();
        mDialogState = OFF_MODE;
    }

    private void displayRetryCounter(String s) {
        int attempts = new LGUiccCard().getPin1RetryCount();
        int icon_Res = android.R.attr.alertDialogIcon;
        // [S][2012.2.29][jin850607.hong] for VDF, double sim
        mCount = attempts;
        // [E][2012.2.29][jin850607.hong] for VDF, double sim

        String targetCountry = Config.getCountry();
        String config_operator = Config.getOperator();

        Log.i(LOG_TAG, "attempts: " + attempts + " config: " + config_operator);
        Log.i(LOG_TAG, "targetCountry: " + targetCountry);

        //[S][20130130][TD:274212][swgi.kim@lge.com] changed a conditions for DuoBill USIM
        if (attempts == 3 && "VDF".equals(Config.getOperator())) {
            Toast.makeText(this, mRes.getString(R.string.sp_double_sim_popup_NORMAL),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        else
        //[E][20130130][TD:274212][swgi.kim@lge.com] changed a conditions for DuoBill USIM
        {
            if ("DCM".equals(Config.getOperator())) {
                if (attempts > 0) {
                    new AlertDialog.Builder(this)
                            .setTitle(
                                    getResources().getString(
                                            R.string.sp_sim_lock_invalid_title_NORMAL))
                            .setIconAttribute(icon_Res)
                            .setMessage(
                                    getResources().getString(
                                            R.string.sp_sim_lock_invalid_popup_NORMAL, mCount))
                            .setPositiveButton(android.R.string.yes, null).show();
                } else {
                    // LGE_UICC_S, [Common] UICC Related Settings.Secure ->preference {
                    LGUiccManager.setProperty("pref.sim_err_popup_msg", "1");
                    // LGE_UICC_E, [Common] UICC Related Settings.Secure ->preference }
                }
            }
            else {
                if (attempts == 1) {
                    new AlertDialog.Builder(this)
                            .setTitle(
                                    getResources().getString(
                                            R.string.sp_sim_lock_invalid_title_NORMAL))
                            .setIconAttribute(icon_Res)
                            .setMessage(
                                    getResources().getString(
                                            R.string.sp_sim_lock_invalid_popup_singular_NORMAL,
                                            String.format(Locale.getDefault(), "%d", mCount)))
                            .setPositiveButton(android.R.string.yes, null).show();
                } else if (attempts > 1) {
                    new AlertDialog.Builder(this)
                            .setTitle(
                                    getResources().getString(
                                            R.string.sp_sim_lock_invalid_title_NORMAL))
                            .setIconAttribute(icon_Res)
                            .setMessage(
                                    getResources().getString(
                                            R.string.sp_sim_lock_invalid_popup_NORMAL,
                                            String.format(Locale.getDefault(), "%d", mCount)))
                            .setPositiveButton(android.R.string.yes, null).show();
                } else {
                    // LGE_UICC_S, [Common] UICC Related Settings.Secure ->preference {
                    LGUiccManager.setProperty("pref.sim_err_popup_msg", "1");
                    // LGE_UICC_E, [Common] UICC Related Settings.Secure ->preference }
                }
            }
        }
    }

    //[S][2012.03.08][jin850607.hong] No ACTION Bar
    public boolean onActionItemClicked(ActionMode arg0, MenuItem arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean onCreateActionMode(ActionMode arg0, Menu arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public void onDestroyActionMode(ActionMode arg0) {
        // TODO Auto-generated method stub

    }

    public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
        // TODO Auto-generated method stub
        return false;
    }
    //[E][2012.03.08][jin850607.hong] No ACTION Bar
}
