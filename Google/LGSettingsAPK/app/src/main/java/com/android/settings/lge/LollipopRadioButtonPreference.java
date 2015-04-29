package com.android.settings.lge;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.android.settings.R;

public class LollipopRadioButtonPreference extends Preference implements OnClickListener,
        OnKeyListener {

    private boolean mChecked;
    private RadioButton mRadioButton;
    private View mRadioPreference;
    private int iconImageId;
    private int buttonImageId;
    private int dashImageId;
    private ImageView buttonImage;
    private String contentDesc;

    private OnImageButtonClickListener mImageButtonClickListener = null;

    interface OnImageButtonClickListener {
        void onImageButtonClickListener();
    }

    public LollipopRadioButtonPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference_lollipop_radio_button);
    }

    public LollipopRadioButtonPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LollipopRadioButtonPreference(Context context) {
        this(context, null, 0);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        ImageView iconImage = (ImageView)view.findViewById(R.id.iconImage);
        if (iconImage != null) {
            iconImage.setImageResource(iconImageId);
        }

        ImageView dashImage = (ImageView)view.findViewById(R.id.dashImage);
        if (dashImage != null) {
            dashImage.setBackgroundResource(dashImageId);
        }

        buttonImage = (ImageView)view.findViewById(R.id.buttonImage);
        if (buttonImage != null) {
            buttonImage.setImageResource(buttonImageId);
            buttonImage.setContentDescription(contentDesc);
            buttonImage.setOnClickListener(this);
        }

        mRadioButton = (RadioButton)view.findViewById(R.id.radio_button);
        mRadioButton.setChecked(mChecked);

        mRadioPreference = view.findViewById(R.id.lollipop_pref_radio);
        if (mRadioPreference != null && mRadioPreference instanceof LinearLayout) {
            mRadioPreference.setOnClickListener(this);
            mRadioPreference.setOnKeyListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if ((v != null) && (v.getId() == R.id.lollipop_pref_radio)) {
            boolean newValue = !isChecked();

            if (callChangeListener(newValue)) {
                return;
            }
        }

        if ((v != null) && (v.getId() == R.id.buttonImage)) {
            mImageButtonClickListener.onImageButtonClickListener();

        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if ((v != null) && (v.getId() == R.id.lollipop_pref_radio)) {
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
        if (mRadioButton != null) {
            mRadioButton.setChecked(checked);
        }
    }

    public void setIconImage(int id) {
        iconImageId = id;
    }

    public void setbuttonImage(int id) {
        buttonImageId = id;
    }

    public void setdashImage(int id) {
        dashImageId = id;
    }

    public void setContentDesc(String contentDesc) {
        this.contentDesc = contentDesc;
    }

    public void notifyDataChanged() {
        notifyChanged();
    }

    public void setOnImageButtonClickListener(OnImageButtonClickListener listener) {
        mImageButtonClickListener = listener;
    }
}
