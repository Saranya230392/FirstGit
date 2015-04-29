package com.android.settings.lge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.android.settings.R;

public class RadioButtonPreference extends Preference implements OnClickListener, OnKeyListener{

    private boolean mChecked;
    private RadioButton mRadioButton;
    private View mRadioPreference;
    private Drawable mIcon;


    public RadioButtonPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setLayoutResource(R.layout.preference_radio_button);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.IconPreferenceScreen, defStyle, 0);
        mIcon = a.getDrawable(R.styleable.IconPreferenceScreen_icon);
    }

    public RadioButtonPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadioButtonPreference(Context context) {
        this(context, null, 0);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        parent.setMotionEventSplittingEnabled(false);
        return super.onCreateView(parent);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        ImageView imageView = (ImageView) view.findViewById(R.id.icon);
        if (imageView != null && mIcon != null) {
            imageView.setImageDrawable(mIcon);
        }

        mRadioButton = (RadioButton)view.findViewById(R.id.radio_button);
        mRadioButton.setChecked(mChecked);

        mRadioPreference = view.findViewById(R.id.radio_pref);
        if (mRadioPreference != null && mRadioPreference instanceof LinearLayout) {
            mRadioPreference.setOnClickListener(this);
            mRadioPreference.setOnKeyListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if ((v != null) && (v.getId() == R.id.radio_pref)) {
            boolean newValue = !isChecked();

            if (callChangeListener(newValue)) {
                return;
            }
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if ((v != null) && (v.getId() == R.id.radio_pref)) {
           if ((keyCode == KeyEvent.KEYCODE_ENTER) && (KeyEvent.ACTION_UP == event.getAction())) {
                boolean newValue = !isChecked();

                if (callChangeListener(newValue)) {
                    return true;
                }
            }
        }

        return false;
    }


    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        if(mRadioButton != null) {
            mRadioButton.setChecked(checked);
        }
        notifyChanged();
    }

    public void notifyDataChanged() {
        notifyChanged();
    }
}
