package com.android.settings.notification;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;

public class ZenModeRadioPreference extends Preference {

    private static final String TAG = "ZenModeRadioPreference";
    private ImageButtonListener mButtonClickListener = new ImageButtonListener();
    private boolean mChecked;
    private boolean mDisableDependentsState;
    private ImageView mSettingsButton;
    private TextView mTitleText;
    private TextView mSummaryText;
    private View mCheckBox;
    private OnTreeClickListener mTreeClickListener;
    private boolean mIshideEditIcon = false;
    private Context mContext;
    private Intent mIntent;
    private String mReplyMessage;

    protected interface OnTreeClickListener {
        void onTreeClick(Preference preference);
    }

    private class ImageButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "ImageButton onClick");
            if (null != mIntent) {
                mContext.startActivity(mIntent);
            }
        }
    }

    public ZenModeRadioPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setLayoutResource(R.layout.zen_radio_preference);
        setWidgetLayoutResource(R.layout.zen_radio_widget);

    }

    public ZenModeRadioPreference(Context context) {
        this(context, null);
    }

    public void setOnTreeClickListener(OnTreeClickListener listener) {
        mTreeClickListener = listener;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mCheckBox = view.findViewById(R.id.inputmethod_pref);
        mCheckBox.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        onCheckBoxClicked();
                        mTreeClickListener.onTreeClick(ZenModeRadioPreference.this);
                    }
                });

        mSettingsButton = (ImageView)view.findViewById(R.id.inputmethod_settings);
        mTitleText = (TextView)view.findViewById(android.R.id.title);

        if (null != mReplyMessage) {
            mTitleText.setText(mReplyMessage);
        }
        mSummaryText = (TextView)view.findViewById(android.R.id.summary);
        mSettingsButton.setOnClickListener(mButtonClickListener);

        hideEditIcon(mIshideEditIcon);

        View checkboxView = view.findViewById(android.R.id.checkbox);
        if (checkboxView != null && checkboxView instanceof Checkable) {
            ((Checkable)checkboxView).setChecked(mChecked);
        }
    }

    public void setTitle(String title) {
        if (null != mTitleText) {
            mTitleText.setText(title);
        }
        mReplyMessage = title;
    }

    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    public void setSummary(String summary) {
        mSummaryText.setText(summary);
    }

    public void setRemoveEditIcon(boolean isHide) {
        mIshideEditIcon = isHide;
    }

    public void hideEditIcon(boolean isHide) {
        if (true == isHide) {
            mSettingsButton.setVisibility(View.GONE);
            return;
        }
        mSettingsButton.setVisibility(View.VISIBLE);
    }

    protected void onCheckBoxClicked() {
        setChecked(true);
    }

    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            persistBoolean(checked);
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public boolean shouldDisableDependents() {
        boolean shouldDisable = mDisableDependentsState ? mChecked : !mChecked;
        return shouldDisable || super.shouldDisableDependents();
    }

    @Override
    protected void onClick() {
        boolean newValue = !isChecked();

        if (!callChangeListener(newValue)) {
            return;
        }

        setChecked(newValue);
    }
}