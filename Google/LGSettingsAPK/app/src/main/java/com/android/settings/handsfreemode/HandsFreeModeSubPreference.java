package com.android.settings.handsfreemode;

import android.content.Context;
import android.content.res.Resources;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.android.settings.Utils;

public class HandsFreeModeSubPreference extends CheckBoxPreference {
    private Context mContext;
    private static final int LEFT_PADDING = 10;

    public HandsFreeModeSubPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);
        TextView titlet = (TextView)view.findViewById(android.R.id.title);
        TextView summary = (TextView)view.findViewById(android.R.id.summary);
        titlet.setPaddingRelative(convertDpToPixel(LEFT_PADDING), titlet.getPaddingTop(), 0, 0);
        summary.setPaddingRelative(convertDpToPixel(LEFT_PADDING), summary.getPaddingTop(), 0, 0);
    }

    public float convertPixelsToDp(float px) {
        Resources resources = mContext.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    public int convertDpToPixel(float dp) {
        Resources resources = mContext.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int)px;
    }

}
