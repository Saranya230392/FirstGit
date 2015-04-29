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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.text.InputType;
import android.util.Log;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

/**
 * TODO: Add a soft dialpad for PIN entry.
 */
class EditPinPreference_kr extends EditTextPreference implements TextWatcher {

    private static final String TAG = "EditPinPreference_kr";

    private static final int MAX_PIN_LENGTH = 8;
    private static final int MIN_PIN_LENGTH = 4;

    private static final String IME_OPTION_NO_PASTE = "DISABLE_BUBBLE_POPUP_PASTE";

    private static final InputFilter MAXLENFILTER[] = {new InputFilter.LengthFilter(
            MAX_PIN_LENGTH)};
    private InputFilter[] mMaxLenFilter = MAXLENFILTER;

    private OnPinEnteredListener mPinListener;
    private OnPersoEnteredListener mPersoListener;

    private EditPreferenceBindListener mEditPreferenceBindListener;

    public EditPinPreference_kr(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    interface OnPinEnteredListener {
        void onPinEntered(EditPinPreference_kr preference,
                boolean positiveResult);
    }

    interface OnPersoEnteredListener {
        void onPersoEntered(EditPinPreference_kr preference,
                boolean positiveResult);
    }

    interface EditPreferenceBindListener {
        void onBindEditPreferenceView(String key, View view);
    }

    public void setOnPinEnteredListener(OnPinEnteredListener listener) {
        Log.d(TAG, "setOnPinEnteredListener  ");
        mPinListener = listener;
    }

    public void setOnPersoEnteredListener(OnPersoEnteredListener listener2) {
        Log.d(TAG, "setOnPersoEnteredListener  ");
        mPersoListener = listener2;
    }

    public void setEditPreferenceBindListener(EditPreferenceBindListener l) {
        Log.d(TAG, "setEditPreferenceBindListener  ");
        mEditPreferenceBindListener = l;
    }

    public void setMaxLenInputFilter(InputFilter[] aFilter) {
        mMaxLenFilter = aFilter;
    }

    @Override
    protected void onBindDialogView(View view) {
        Log.d(TAG, "onBindDialogView() ");
        super.onBindDialogView(view);

        final EditText editText = (EditText)view
                .findViewById(android.R.id.edit);

        if (editText != null) {
            Log.d(TAG, "editText != null ");
            editText.setSingleLine(true);

            editText.setFilters(mMaxLenFilter);

            editText.setKeyListener(DigitsKeyListener.getInstance());
            editText.setInputType(InputType.TYPE_CLASS_NUMBER
                    | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            editText.addTextChangedListener(this);
            editText.setPrivateImeOptions(IME_OPTION_NO_PASTE);

            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    InputMethodManager inputManager = (InputMethodManager)getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(editText, 0);
                }
            }, 150);
        }

    }

    public boolean isDialogOpen() {
        Log.d(TAG, "isDialogOpen()");
        Dialog dialog = getDialog();
        return dialog != null && dialog.isShowing();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        Log.d(TAG, "onDialogClosed()");
        super.onDialogClosed(positiveResult);

        if (mPinListener != null) {
            Log.d(TAG, "mPinListener.onPinEntered(this, positiveResult)");
            mPinListener.onPinEntered(this, positiveResult);
        } else if (mPersoListener != null) {
            Log.d(TAG, "mPersoListener.onPersoEntered(this, positiveResult)");
            mPersoListener.onPersoEntered(this, positiveResult);
        }

    }

    public void showPinDialog() {
        Log.d(TAG, "showPinDialog()");
        Dialog dialog = getDialog();
        if (dialog == null || !dialog.isShowing()) {
            showDialog(null);
        }
    }

    public void dismissDialog() {
        Log.d(TAG, "dismissDialog()");
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    protected void onClick() {
        Log.d(TAG, "onClick");
        super.onClick();
    }

    protected void onBindView(View aView) {
        Log.d(TAG, "onBindView");
        super.onBindView(aView);

        if (mEditPreferenceBindListener != null) {
            Log.d(TAG, "(mEditPreferenceBindListener != null");
            Log.d(TAG, "getKey()" + getKey());
            mEditPreferenceBindListener.onBindEditPreferenceView(getKey(),
                    aView);
        }
    }

    protected View onCreateView(ViewGroup arg0) {
        Log.d(TAG, "onCreateView");

        View view = super.onCreateView(arg0);

        Log.d(TAG, "ViewGroup getChildCount() " + arg0.getChildCount());
        return view;
    }

    protected void showDialog(Bundle state) {
        Log.d(TAG, "showDialog()");

        super.showDialog(state);
        AlertDialog ad = (AlertDialog)getDialog();
        ad.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(
                getEditText().length() >= MIN_PIN_LENGTH);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d(TAG, "onTextChanged");

        final Dialog d = getDialog();
        final EditText textfield = getEditText();
        if (d instanceof AlertDialog) {
            if (textfield.getText().length() >= MIN_PIN_LENGTH) {
                Log.d(TAG,
                        "onTextChanged,  ((AlertDialog)d).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);");
                ((AlertDialog)d).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);
            } else {
                ((AlertDialog)d).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(false);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

}
// LGE_UICC_E, [USIM_Patch 2055][KR]
