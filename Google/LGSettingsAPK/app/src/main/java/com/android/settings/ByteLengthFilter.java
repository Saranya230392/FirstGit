package com.android.settings;

import java.io.UnsupportedEncodingException;
import java.util.logging.ErrorManager;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

// pull from Message
public class ByteLengthFilter implements InputFilter {
    private static final boolean DEBUG = true;
    private static final String TAG = "ByteLengthFilter";

    public interface OnMaxLengthListener {
        public void onMaxLength();
    }

    private void onMaxLength() {
        if (mOnMaxLength != null) {
            mOnMaxLength.onMaxLength();
        }
    }

    public interface OnDisplayListener {
        public void onDisplay(CharSequence source);
    }

    private void onDisplay(CharSequence source) {
        if (mOnDisplayMessage != null) {
            mOnDisplayMessage.onDisplay(source);
        }
    }


    public static final String DEFAULT_ENCODING = "KSC5601";
    protected int mMax;    // byte length
    protected String mEncode;
    protected Context mContext;

    private OnMaxLengthListener mOnMaxLength = null;
    private OnDisplayListener mOnDisplayMessage = null;

    public ByteLengthFilter(Context context, int max) {
        this(context, max, DEFAULT_ENCODING);
    }

    public ByteLengthFilter(Context context, int max, String encode) {
        mContext = context;
        mMax = max;
        mEncode = encode;
    }

    public void setOnMaxLengthListener(OnMaxLengthListener aListener) {
        mOnMaxLength = aListener;
    }

    public void setOnDisplayListener(OnDisplayListener aListener) {
        mOnDisplayMessage = aListener;
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
            int dend) {
        int srcLen = source.length();
        if (srcLen == 0) {
            return null; // keep original
        }

        int sourceLength = getByteLength(source.toString());
        int destLength = getByteLength(dest.toString());
        if (DEBUG) {
            Log.d(TAG, "destLength = " + destLength + ", sourceLength = " + sourceLength);
            if (sourceLength < 0) {
                Log.e(TAG, "encoding error... source : " + source);
            }
            if (destLength < 0) {
                Log.e(TAG, "encoding error... dest : " + dest);
            }
        }

        int keep = mMax - destLength;
        if (dend - dstart > 0) {
            keep += getByteLength(dest.subSequence(dstart, dend).toString());
        }

        if (DEBUG) {
            Log.d(TAG, "mMax = " + mMax + ", keep = " + keep);
        }

        if (keep <= 0) {
            if (mImm != null) {
                mImm.restartInput(mEdit);
            }
            onMaxLength();
            return "";
        } else if (keep >= sourceLength) {
            onDisplay(source);
            return null; // keep original
        } else {
            if (mImm != null) {
                mImm.restartInput(mEdit);
            }
            int idx = keep + start;
            if (idx > srcLen) {
                idx = srcLen;
            }
            for (; idx > start; idx--) {
                CharSequence ret = source.subSequence(start, idx);
                if (keep >= getByteLength(ret.toString())) {
                    onMaxLength();
                    onDisplay(ret);
                    return ret;
                }
            }

            onMaxLength();
            return "";
        }
    }

    public int getByteLength(String str) {
        try {
            byte[] bytetext = null;
            bytetext = str.toString().getBytes("KSC5601");
            return bytetext.length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getMaxLength() {
        return mMax;
    }

    public void setMaxLength(int max) {
        mMax = max;
    }

    private InputMethodManager mImm;
    private EditText mEdit;

    public void setInputProperty(InputMethodManager imm, View v) {
        mImm = imm;
        mEdit = (EditText)v;
    }
}
