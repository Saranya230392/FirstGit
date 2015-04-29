package com.android.settings.notification;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.settings.R;

public class ZenModeSummaryDescriptionPreference extends Preference {
    static final String TAG = "ZenModeSummaryDescriptionPreference";
    private TextView mTextView;

    public ZenModeSummaryDescriptionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.zen_mode_summary_description);
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        return super.getView(convertView, parent);
    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);
        mTextView = (TextView)view.findViewById(R.id.zen_mode_common_summary_description);
        mTextView.setText(getSummary());
        mTextView.setVisibility(View.VISIBLE);
        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
                return true;
            }
        });
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return super.onCreateView(parent);
    }

}
