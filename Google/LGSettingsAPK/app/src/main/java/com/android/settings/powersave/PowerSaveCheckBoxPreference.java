package com.android.settings.powersave;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.SystemProperties;

import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;
import android.provider.Settings;

public class PowerSaveCheckBoxPreference extends ListPreference {
    protected interface OnTreeClickListener {
        void onTreeClick(Preference preference);
    }

    private static final float DISABLED_ALPHA = 0.4f;
    private ImageButtonListener mButtonClickListener = new ImageButtonListener();
    private boolean mChecked;
    private boolean mDisableDependentsState;
    private ImageView mSetingsButton;
    private TextView mTitleText;
    private TextView mSummaryText;
    private View mCheckBox;
    private OnTreeClickListener mTreeClickListener;

    private class ImageButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            showDialog(null);
        }
    }

    public PowerSaveCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_inputmethod);
        setWidgetLayoutResource(R.layout.preference_inputmethod_widget);

        if ("power_save_front_led".equals(getKey()) && "ATT".equals(Config.getOperator())) {
            setEntries(R.array.sp_power_save_front_led_entries_att_NORMAL);
        }
    }

    public PowerSaveCheckBoxPreference(Context context) {
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
                        mTreeClickListener.onTreeClick(PowerSaveCheckBoxPreference.this);
                    }
                });

        mSetingsButton = (ImageView)view.findViewById(R.id.inputmethod_settings);
        mTitleText = (TextView)view.findViewById(android.R.id.title);
        mSummaryText = (TextView)view.findViewById(android.R.id.summary);
        mSetingsButton.setOnClickListener(mButtonClickListener);

        enableSettingsButton();

        View checkboxView = view.findViewById(android.R.id.checkbox);
        if (checkboxView != null && checkboxView instanceof Checkable) {
            ((Checkable)checkboxView).setChecked(mChecked);
        }
    }

    protected void onCheckBoxClicked() {
        if (isChecked()) {
            setChecked(false);
        } else {
            setChecked(true);
        }
    }

    private void enableSettingsButton() {
        if (mSetingsButton != null) {
            CharSequence[] entries = getEntries();
            if (entries == null /*|| entries.length==0*/) {
                mSetingsButton.setVisibility(View.GONE);
            } else {
                final boolean checked = isEnabled() && isChecked(); //yonguk.kim Immediately start
                mSetingsButton.setEnabled(checked);
                mSetingsButton.setClickable(checked);
                mSetingsButton.setFocusable(checked);
                if (!checked) {
                    mSetingsButton.setAlpha(DISABLED_ALPHA);
                }
            }
        }
        if (mTitleText != null) {
            mTitleText.setEnabled(true);
        }
        if (mSummaryText != null) {
            mSummaryText.setEnabled(true);
        }
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

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
            onCheckBoxClicked();
            v.setSoundEffectsEnabled(true);
            v.playSoundEffect(SoundEffectConstants.CLICK);
            mTreeClickListener.onTreeClick(PowerSaveCheckBoxPreference.this);

        }
        return super.onKey(v, keyCode, event);

    }

    private void updateSummary() {
        //String value = Settings.System.getString(
        //        getContentResolver(), SettingsConstants.System.POWER_SAVE_BRIGHTNESS)+"% ";

        if (isChecked()) {
            setSummary(""); // need this action for "%"
            setSummary("%s");
        } else {
            if ("power_save_brightness".equals(getKey())) {
                setSummary(R.string.sp_power_saver_brightness_adjust_summary_NORMAL);
            } else if ("power_save_screen_timeout".equals(getKey())) {
                setSummary(R.string.sp_power_saver_screen_timeout_summary_NORMAL);
            } else if ("power_save_front_led".equals(getKey())) {
                setSummary(R.string.sp_power_saver_front_light_summary_NORMAL);
            }
        }
    }

}