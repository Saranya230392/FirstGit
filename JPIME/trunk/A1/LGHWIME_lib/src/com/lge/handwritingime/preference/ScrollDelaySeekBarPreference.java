package com.lge.handwritingime.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.lge.handwritingime.R;

public final class ScrollDelaySeekBarPreference extends DialogPreference implements OnSeekBarChangeListener {

    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    private static final String ATTR_DEFAULT_VALUE = "defaultValue";
    private static final String ATTR_MIN_VALUE = "minValue";
    private static final String ATTR_MAX_VALUE = "maxValue";

    private static final int DEFAULT_CURRENT_VALUE = 5;
    private static final int DEFAULT_MIN_VALUE = 1;
    private static final int DEFAULT_MAX_VALUE = 10;

    private final int mDefaultValue;
    private final int mMaxValue;
    private final int mMinValue;

    private int mCurrentValue;
    private SeekBar mSeekBar;
    private TextView mValueText;

    public ScrollDelaySeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mMinValue = attrs.getAttributeIntValue(ANDROID_NS, ATTR_MIN_VALUE, DEFAULT_MIN_VALUE);
        mMaxValue = attrs.getAttributeIntValue(ANDROID_NS, ATTR_MAX_VALUE, DEFAULT_MAX_VALUE);
        mDefaultValue = attrs.getAttributeIntValue(ANDROID_NS, ATTR_DEFAULT_VALUE, DEFAULT_CURRENT_VALUE);
    }

    @Override
    protected View onCreateDialogView() {
        mCurrentValue = getPersistedInt(mDefaultValue);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.scroll_delay_seekbar_dialog, null);
        mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setProgress(mCurrentValue - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);

        mValueText = (TextView) view.findViewById(R.id.current_value);
        mValueText.setText(Float.toString((float) (mCurrentValue * 0.1)) + " "
                + getContext().getString(R.string.auto_scroll_input_delay_popup_text_sec));

        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (!positiveResult) {
            return;
        }
        if (shouldPersist()) {
            persistInt(mCurrentValue);
        }
        notifyChanged();
    }

    @Override
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        mCurrentValue = value + mMinValue;
        mValueText.setText(Float.toString((float) (mCurrentValue * 0.1)) + " "
                + getContext().getString(R.string.auto_scroll_input_delay_popup_text_sec));
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
    }

}