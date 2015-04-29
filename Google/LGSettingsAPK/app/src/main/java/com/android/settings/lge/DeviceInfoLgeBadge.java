package com.android.settings.lge;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.settings.R;

public class DeviceInfoLgeBadge extends Preference {
    public static int appUpdatesBadgeCount = 0;
    public DeviceInfoLgeBadge (Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.badge_list);

    }

      @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);
        TextView badge_text = (TextView)view.findViewById(R.id.badge);
        Log.d("starmotor" , "appUpdatesBadgeCount = " + appUpdatesBadgeCount);
        badge_text.setVisibility(View.GONE);
        if (appUpdatesBadgeCount > 0) {
            /*
            badge_text.setVisibility(View.VISIBLE);
            badge_text.setText(appUpdatesBadgeCount + "");
            */
            float fontSize = badge_text.getTextSize();
            badge_text.setVisibility(View.VISIBLE);
            badge_text.setText(appUpdatesBadgeCount + "");
            badge_text.setPaddingRelative(1, 1, 1, 1);
            badge_text.setWidth(getBadgeBackgroundWidthDp(appUpdatesBadgeCount, fontSize));            
        }
    }

    private int getBadgeBackgroundWidthDp(int badgeCount, float fontSize) {
	Log.d("starmotor", "****getBadgeBackgroundWidthDp****");

        int defaultListTitleFontSize = 18;

        if (badgeCount < 10) {
            return (int)(25 * fontSize / defaultListTitleFontSize);
        } else if (badgeCount < 100) {
            return (int)(35 * fontSize / defaultListTitleFontSize);
        } else if (badgeCount < 1000) {
            return (int)(45 * fontSize / defaultListTitleFontSize);
        } else {
            return (int)(55 * fontSize / defaultListTitleFontSize);
        }
    }      
}
