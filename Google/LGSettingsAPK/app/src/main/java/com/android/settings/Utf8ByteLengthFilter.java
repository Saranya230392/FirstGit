package com.android.settings;

import android.app.Activity;
import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class Utf8ByteLengthFilter implements InputFilter {

    private final int mMaxBytes;
    private Toast mToast = null;
    private Activity mActivity = null;
    private EditText mDeviceNamePref = null;
    private InputMethodManager mImm = null;

    public Utf8ByteLengthFilter(int maxBytes, Activity activity, EditText et) {
        mMaxBytes = maxBytes;
        this.mActivity = activity;
        this.mDeviceNamePref = et;
        mImm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);

    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
            int dstart, int dend) {
        int srcByteCount = 0;
        // count UTF-8 bytes in source substring
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            srcByteCount += (c < (char)0x0080) ? 1 : (c < (char)0x0800 ? 2 : 3);
        }
        int destLen = dest.length();
        int destByteCount = 0;
        // count UTF-8 bytes in destination excluding replaced section
        for (int i = 0; i < destLen; i++) {
            if (i < dstart || i >= dend) {
                char c = dest.charAt(i);
                destByteCount += (c < (char)0x0080) ? 1 : (c < (char)0x0800 ? 2 : 3);
            }
        }
        int keepBytes = mMaxBytes - destByteCount;
        if (keepBytes <= 0) {
            if (mImm != null && mDeviceNamePref != null) {
                mImm.restartInput(mDeviceNamePref);
            }

            if (mToast == null) {
                mToast = Toast.makeText(mActivity, R.string.sp_auto_reply_maxlength_NORMAL,
                        Toast.LENGTH_SHORT);
            }
            mToast.show();

            return "";
        } else if (keepBytes >= srcByteCount) {
            return null; // use original dest string
        } else {
            // find end position of largest sequence that fits in keepBytes
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                keepBytes -= (c < (char)0x0080) ? 1 : (c < (char)0x0800 ? 2 : 3);
                if (keepBytes < 0) {
                    if (mImm != null && mDeviceNamePref != null) {
                        mImm.restartInput(mDeviceNamePref);
                    }
                    if (mToast == null) {
                        mToast = Toast.makeText(mActivity,
                                R.string.sp_auto_reply_maxlength_NORMAL,
                                Toast.LENGTH_SHORT);
                    }
                    mToast.show();

                    return source.subSequence(start, i);
                }
            }
            // If the entire substring fits, we should have returned null
            // above, so this line should not be reached. If for some
            // reason it is, return null to use the original dest string.
            return null;
        }
    }
}
