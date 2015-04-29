package com.android.settings;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;

public class VibrateTypeListPreference extends ListPreference {

    private static final String TAG = "VibrateTypeListPreference";
    private static final int REPEAT = 0;
    private int selectiedPosision = -1;
    private Context mContext;
    private Vibrator mVibrator;

    private static final int TYPE1 = 0;
    private static final int TYPE2 = 1;
    private static final int TYPE3 = 2;
    private static final int TYPE4 = 3;
    private static final int TYPE5 = 4;
    private static final long[] DEFAULT_VIBRATE = { 0, 200, 200, 200, 200, 200, 200, 200, 200, 200,
            200 };

    public VibrateTypeListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mContext = context;
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        // TODO Auto-generated method stub
        super.onPrepareDialogBuilder(builder);

        builder.setSingleChoiceItems(getEntries(), findIndexOfValue(getValue()),
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "Selected List : " + which);
                        selectiedPosision = which;
                        playVibrate(selectiedPosision);
                    }
                });

        builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Log.d(TAG, "NegativeButton OnClickListener");
            }
        });
        builder.setPositiveButton(R.string.dlg_ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Log.d(TAG, "PositiveButton OnClickListener");
                onDialogClosed(true);
            }
        });
    }

    private long[] getLongIntArray(int resid) {
        Resources mResource = mContext.getResources();
        int[] ar = null;
        long[] out = null;

        try {
            ar = mResource.getIntArray(resid);
            if (ar == null) {
                return null;
            }
            out = new long[ar.length];
            for (int i = 0; i < ar.length; i++) {
                out[i] = ar[i];
            }
        } catch (NotFoundException e) {
            Log.e(TAG, "[getLongIntArray] NotFoundException : " + e.getMessage());
            return DEFAULT_VIBRATE;
        } catch (NullPointerException e) {
            Log.e(TAG, "[getLongIntArray] NullPointerEception :  " + e.getMessage());
            return DEFAULT_VIBRATE;
        }
        return out;
    }

    private long[] getVibratePatterns(int vibrateType) {

        int resid = 0;
        switch (vibrateType) {
        case TYPE1:
            resid = R.array.config_distinctiveVibrationType1;
            break;
        case TYPE2:
            resid = R.array.config_distinctiveVibrationType2;
            break;
        case TYPE3:
            resid = R.array.config_distinctiveVibrationType3;
            break;
        case TYPE4:
            resid = R.array.config_distinctiveVibrationType4;
            break;
        case TYPE5:
            resid = R.array.config_distinctiveVibrationType5;
            break;
        default:
            resid = R.array.config_distinctiveVibrationType1;
            break;
        }
        return getLongIntArray(resid);
    }

    private void playVibrate(int vibrateType) {
        mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);

        try {
            mVibrator.vibrate(getVibratePatterns(vibrateType), REPEAT);
        } catch (NullPointerException e) {
            Log.e(TAG, "playVibrate() Null point exception!!!" + e.getMessage());
        }
    }

    public void vibrateStop() {
        if (null != mVibrator) {
            mVibrator.cancel();
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // TODO Auto-generated method stub
        Log.d(TAG, "Dialog Closed! PositiveResult : " + positiveResult);
        try {
            if (positiveResult && selectiedPosision >= 0 && getEntryValues() != null) {
                String value = getEntryValues()[selectiedPosision].toString();
                Log.d(TAG, "[DialogClosed] selected value : " + value);
                if (callChangeListener(value)) {
                    setValue(value);
                }
                selectiedPosision = -1;
            }
            if (null != mVibrator) {
                mVibrator.cancel();
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "onDialogZClosed() - NullPointException !!!");
        }

    }
}
