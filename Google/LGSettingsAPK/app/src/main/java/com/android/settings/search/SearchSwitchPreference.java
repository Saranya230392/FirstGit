package com.android.settings.search;

import android.content.Context;
import android.content.Intent;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.android.settings.R;

public class SearchSwitchPreference extends SwitchPreference {
    private LinearLayout mLayout;
    private Intent mIntent;
    private Context mContext;
    private Switch mSwitch;
    private boolean mIsChecked;
    private boolean mEnable;
    
    public SearchSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchSwitchPreference(Context context) {
        super(context);
        this.setLayoutResource(R.layout.preference_search_switch);
        mContext = context;
    }

    public SearchSwitchPreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mLayout = (LinearLayout)view.findViewById(R.id.text_layout);
        mSwitch = (Switch)view.findViewById(R.id.switchWidget);
        
        if (mLayout != null) {
            mLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = mIntent;
                    intent.putExtra("perform", false);
                    if (intent != null) { 
                        mContext.startActivity(intent);
                    }
                }
            });
        }
        
        if (mSwitch != null) {
            mSwitch.setChecked(mIsChecked);
            mSwitch.setEnabled(mEnable);
            mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Intent intent = mIntent;
                    intent.putExtra("perform", true);
                    intent.putExtra("newValue", isChecked);
                    if (intent != null) { 
                        mContext.startActivity(intent);
                    }
                }
            });
            mSwitch.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    @Override
    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    @Override
    public void setChecked(boolean checked) {
        mIsChecked = checked;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnable = enabled;
    }
}
