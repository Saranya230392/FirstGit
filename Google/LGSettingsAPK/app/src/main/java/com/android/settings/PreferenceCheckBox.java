
package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Checkable;
import android.widget.CompoundButton;

public class PreferenceCheckBox extends CheckBoxPreference {
    private Context mContext;
    private CheckBox mCheckBox;
    private boolean mCheckBoxEnabled = true;

    protected boolean mEnabledAppearance = false;
    protected boolean mEnabledClickOnDisableSwitch = false;
    private View mDummyCheckBoxView;

    private final Listener mListener = new Listener();
    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SLog.i("onCheckedChanged 1= " + isChecked);
            if (!callChangeListener(isChecked)) {
                SLog.i("onCheckedChanged 2= " + isChecked);
                buttonView.setChecked(!isChecked);
                return;
            }
            SLog.i("onCheckedChanged 3= " + isChecked);
            setChecked(isChecked);
        }
    }

    public CheckBox getCheckBox() {
        return mCheckBox;
    }

    public boolean getCheckBoxEnabled() {
        return mCheckBoxEnabled;
    }

    public void setCheckBoxEnabled(boolean enable) {
        mCheckBoxEnabled = enable;
        SLog.i("mCheckBoxEnabled enable= " + enable);
        if (null != mCheckBox) {
            SLog.i("mCheckBoxEnabled null != mCheckBox= " + enable);
            mCheckBox.setEnabled(mCheckBoxEnabled);
        }
    }

    public PreferenceCheckBox(Context context) {
        super(context, null);
    }

    public PreferenceCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs, com.android.internal.R.attr.checkBoxPreferenceStyle);
        setWidgetLayoutResource(R.layout.preference_checkbox);
        mContext = context;
    }

    public PreferenceCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        View checkableView = view.findViewById(R.id.CheckBoxWidget);
        if (checkableView != null && checkableView instanceof Checkable) {
            ((Checkable)checkableView).setChecked(super.isChecked());
            if (checkableView instanceof CheckBox) {
                mCheckBox = (CheckBox)checkableView;
                mCheckBox.setOnCheckedChangeListener(mListener);
                mCheckBox.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        SLog.i("PreferenceCheckBox - mCheckBox onClick");
                    }
                });
            }
        }
        mDummyCheckBoxView = view.findViewById(R.id.dummyCheckBoxView);
        if (mDummyCheckBoxView != null) {
            if (mEnabledClickOnDisableSwitch) {
                mDummyCheckBoxView.setVisibility(View.VISIBLE);
                SLog.i("PreferenceCheckBox - mDummyCheckBoxView visible");
            } else {
                mDummyCheckBoxView.setVisibility(View.GONE);
                SLog.i("PreferenceCheckBox - mDummyCheckBoxView gone");
            }
            mDummyCheckBoxView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    SLog.i("PreferenceCheckBox - mDummyCheckBoxView onClick");
                    callOnClick();
                }
            });
        }
        boolean viewEnabled = isEnabled() && mEnabledAppearance;
        enableView(view, viewEnabled);
        if (mDummyCheckBoxView != null) {
            if (mEnabledClickOnDisableSwitch) {
                mDummyCheckBoxView.setEnabled(true);
            }
        }
    }

    protected View onCreateView(ViewGroup aParent) {
        final View layout = super.onCreateView(aParent);
        SLog.i("onCreateView");
        return layout;
    }

    public void setEnabledAppearance(boolean enabled) {
        mEnabledAppearance = enabled;
        notifyChanged();
    }

    public void setEnableClickOnSwitch(boolean enabled) {
        SLog.i("setEnableClickOnSwitch() enabled:" + enabled);
        mEnabledClickOnDisableSwitch = enabled;
        if (mDummyCheckBoxView != null) {
            if (mEnabledClickOnDisableSwitch) {
                SLog.i("PreferenceCheckBox - mDummyCheckBoxView visible"); 
                mDummyCheckBoxView.setVisibility(View.VISIBLE);
            } else {
                SLog.i("PreferenceCheckBox - mDummyCheckBoxView gone");
                mDummyCheckBoxView.setVisibility(View.GONE);
            }
        }
        notifyChanged();
    }

    public void callOnClick() {
        SLog.i("callOnClick");
        onClick();
    }

    protected void enableView(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup grp = (ViewGroup)view;
            for (int index = 0; index < grp.getChildCount(); index++) {
                enableView(grp.getChildAt(index), enabled);
            }
        }
    }

    @Override
    protected void onClick() {
        SLog.i("onClick() mEnabledAppearance = " + mEnabledAppearance);
        if (mEnabledAppearance) {
            super.onClick();
        } else {
            SLog.i("onClick() mEnabledAppearance = " + mEnabledAppearance);
            Intent intent = new Intent("com.lge.setting.action.voltepopup");
            if (null != mContext) {
                mContext.sendBroadcast(intent);
            }
        }
    }

}
