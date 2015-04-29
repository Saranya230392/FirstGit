package com.android.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.UserManager;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class DeveloperIDEditTextPreference extends DialogPreference
        implements TextWatcher, DialogInterface.OnClickListener {
    private static final String LOG_TAG = "MirrorLink";
    private EditTextPreference mDeveloperIDPref;
    private Context mContext;

    public DeveloperIDEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void setDeveloperIDPref(EditTextPreference developerIDPref) {
        this.mDeveloperIDPref = developerIDPref;
    }

    public void afterTextChanged(Editable s) {
        Dialog d = mDeveloperIDPref.getDialog();

        if (d instanceof AlertDialog) {
            if (((AlertDialog)d).getButton(AlertDialog.BUTTON_POSITIVE) == null) {
                return;
            }
            ((AlertDialog)d).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(s.length() > 0);
        }

        if (d instanceof AlertDialog) {
            if (((AlertDialog)d).getButton(AlertDialog.BUTTON_POSITIVE) == null) {
                Log.i(LOG_TAG, "afterTextChanged, AlertDialog.BUTTON_POSITIVE i s null~~");
                return;
            } else {
                ((AlertDialog)d).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                String et_string = mDeveloperIDPref.getEditText().getText()
                                        .toString();
                                Log.i(LOG_TAG, "Save@@@ et_string:" + et_string);
                                Settings.Global.putString(mContext.getContentResolver(),
                                        "mirrorlink_developer_id", et_string);

                                ((AlertDialog)mDeveloperIDPref.getDialog()).dismiss();
                                mDeveloperIDPref.setSummary(et_string);
                                mDeveloperIDPref.setText(et_string);
                            }
                        });
            }
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
