package com.android.settings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;

public class ConfirmEncryptionSummaryDescriptionPreference extends Preference {
    static final String TAG = "ConfirmEncryptionSummaryDescriptionPreference";
    private TextView mTextView1;
    private TextView mTextView2;
    private TextView mTextView3;
    private TextView mTextView4;

    public ConfirmEncryptionSummaryDescriptionPreference(Context context,
            AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.confirm_encrytion_summary_description);
        // TODO Auto-generated constructor stub
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Log.d(TAG, "getView");
        return super.getView(convertView, parent);
    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);
        mTextView1 = (TextView)view.findViewById(R.id.confirm_summary_description1);
        mTextView2 = (TextView)view.findViewById(R.id.confirm_summary_description2);
        mTextView3 = (TextView)view.findViewById(R.id.confirm_summary_description3);
        mTextView4 = (TextView)view.findViewById(R.id.confirm_summary_description4);
        mTextView1.setVisibility(View.VISIBLE);
        mTextView2.setVisibility(View.VISIBLE);
        mTextView3.setVisibility(View.VISIBLE);
        mTextView4.setVisibility(View.VISIBLE);
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
        // TODO Auto-generated method stub
        Log.d(TAG, "onCreateView");
        return super.onCreateView(parent);
    }
}
