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

import android.app.Dialog;
import android.content.Context;
import android.preference.EditTextPreference;
//[S][2012.03.12][jin850607.hong] disable button along text length
import android.os.Bundle;
import android.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
//[E][2012.03.12][jin850607.hong] disable button along text length
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.graphics.Typeface;

/**
 * TODO: Add a soft dialpad for PIN entry.
 */
//[S][2012.03.12][jin850607.hong] disable button along text length
class EditPinPreference extends EditTextPreference implements TextWatcher {
//[E][2012.03.12][jin850607.hong] disable button along text length

//[S][2012.03.12][jin850607.hong] disable button along text length
    private int maxPinCode = 4;
//[E][2012.03.12][jin850607.hong] disable button along text length
//[S][2012.03.20][jin850607.hong] requested framework, for removed paste
    private static final String IME_OPTION_NO_PASTE = "IMENP";
//[E][2012.03.20][jin850607.hong] requested framework, for removed paste
    interface OnPinEnteredListener {
        void onPinEntered(EditPinPreference preference, boolean positiveResult);
    }

    private OnPinEnteredListener mPinListener;

    public EditPinPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditPinPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnPinEnteredListener(OnPinEnteredListener listener) {
        mPinListener = listener;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        final EditText editText = (EditText)view.findViewById(android.R.id.edit);

        if (editText != null) {
            editText.setSingleLine(true);
            //[S][2012.03.08][jin850607.hong] No ACTION Bar
            editText.setCustomSelectionActionModeCallback(new IccLockSettings());
            //[E][2012.03.08][jin850607.hong] No ACTION Bar
            //editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            editText.setKeyListener(DigitsKeyListener.getInstance());
            editText.setInputType( InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD );
//[S][2012.03.12][jin850607.hong] disable button along text length
        editText.addTextChangedListener(this);
//[E][2012.03.12][jin850607.hong] disable button along text length
      //[S][2012.03.20][jin850607.hong] requested framework, for removed paste
        editText.setPrivateImeOptions(IME_OPTION_NO_PASTE);
      //[E][2012.03.20][jin850607.hong] requested framework, for removed paste
            //[S][2012.01.24][swgi.kim][TD:287730] Added the bold font Property
            Typeface tf = Typeface.defaultFromStyle(Typeface.BOLD);
            editText.setTypeface(tf);
            //[E][2012.01.24][swgi.kim][TD:287730] Added the bold font Property
        }
    }

    public boolean isDialogOpen() {
        Dialog dialog = getDialog();
        return dialog != null && dialog.isShowing();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (mPinListener != null) {
            mPinListener.onPinEntered(this, positiveResult);
        }
    }

    public void showPinDialog() {
        Dialog dialog = getDialog();
        if (dialog == null || !dialog.isShowing()) {
            showDialog(null);
        }
    }


//[S][2012.03.12][jin850607.hong] disable button along text length
    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        final Dialog d = getDialog();
        final EditText textfield = getEditText();
        if (d instanceof AlertDialog) {
            if (textfield.getText().length() >= maxPinCode) {
                ((AlertDialog)d).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            } else {
                ((AlertDialog)d).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void afterTextChanged(Editable arg0) {
    }
    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        AlertDialog ad = (AlertDialog)getDialog();
        ad.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(getEditText().length() >= maxPinCode);
    }
//[E][2012.03.12][jin850607.hong] disable button along text length
}
