package com.android.settings.search;

import android.content.Context;
import android.content.Intent;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.LinearLayout;

import com.android.settings.R;

public class SearchCheckBoxPreference extends CheckBoxPreference {
    private CheckBox mCheckBox;
    private LinearLayout mLayout;
    private Intent mIntent;
    private Context mContext;
    private boolean mEnable;

    public SearchCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchCheckBoxPreference(Context context) {
        super(context);
        this.setLayoutResource(R.layout.preference_search_checkbox);
        mContext = context;
    }

    public SearchCheckBoxPreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mLayout = (LinearLayout)view.findViewById(R.id.text_layout);
        mCheckBox = (CheckBox)view.findViewById(android.R.id.checkbox);
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

        if (mCheckBox != null) {
            mCheckBox.setEnabled(mEnable);
            mCheckBox.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = mIntent;
                    intent.putExtra("perform", true);
                    intent.putExtra("newValue", mCheckBox.isChecked());
                    if (intent != null) {
                        mContext.startActivity(intent);
                    }
                }
            });
        }
    }

    @Override
    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnable = enabled;
    }
}
