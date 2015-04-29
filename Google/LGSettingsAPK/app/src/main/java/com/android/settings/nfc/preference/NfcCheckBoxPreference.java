package com.android.settings.nfc;

import com.android.internal.R;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NfcCheckBoxPreference extends CheckBoxPreference {

    public NfcCheckBoxPreference(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public NfcCheckBoxPreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public NfcCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        // TODO Auto-generated method stub
        View v = super.onCreateView(parent);

        // The worst UI
        TextView tv = (TextView)v.findViewById(R.id.title);
        tv.setSingleLine(false);

        // The worst UI
        TextView tvSummary = (TextView)v.findViewById(R.id.summary);
        tvSummary.setMaxLines(32);
        return v;
    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);
    }

}
