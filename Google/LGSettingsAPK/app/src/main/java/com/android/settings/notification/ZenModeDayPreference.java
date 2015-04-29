package com.android.settings.notification;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Calendar;

import android.app.INotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.preference.Preference;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.widget.ToggleButton;
import android.service.notification.ZenModeConfig;
import android.os.ServiceManager;
import com.android.settings.R;
import com.lge.sui.widget.control.SUIToggleButtonGroup;

public class ZenModeDayPreference extends Preference implements OnClickListener, OnTouchListener {
    private static final String TAG = "ZenModeDayPreference";
    private static final int UNCHECK = 0;
    private Context mContext;
    private SUIToggleButtonGroup mToggleGroup;
    private int[] mDay = {0, 0, 0, 0, 0, 0, 0};
    private ZenModeConfig mConfig;
    private ArrayList<String> mDescription;
    private Resources mRes;
    public static final String DEFAULT_DAY = "0000000";

    protected static final int[] DAYS = {
        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY,
        Calendar.SATURDAY, Calendar.SUNDAY
    };

    protected static final int[] DAYS_IW = {
        Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY,
        Calendar.SATURDAY
    };

    private final SparseBooleanArray mDays = new SparseBooleanArray();

    public ZenModeDayPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mRes = mContext.getResources();
        mConfig = ZenModeUtils.getZenModeConfig();
        setLayoutResource(R.layout.zen_mode_dayset);
    }

    public SUIToggleButtonGroup getToggleGroup() {
        return mToggleGroup;
    }

    @Override
    protected final void onBindView(final View view) {
        super.onBindView(view);
        mToggleGroup = (SUIToggleButtonGroup)view.findViewById(R.id.toggle_button_group);
        setDescription();
        try {
            for (int days = 0; days < Time.WEEK_DAY; days++) {
                if (mToggleGroup.getChildAt(days) != null) {
                    mToggleGroup.getChildAt(days).setContentDescription(mDescription.get(days));
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        setToggleButtonListener();
        mDay = getToggleDay();
        showToggleDayChecked();
    }

    public int[] getToggleDay() {
        final int[] days = ZenModeConfig.tryParseDays(mConfig.sleepMode);
        final Calendar c = Calendar.getInstance();
        int[] toggleDay = { 0, 0, 0, 0, 0, 0, 0 };
        ToggleButton sunDayBtn = (ToggleButton)mToggleGroup.getChildAt(0);

        if (days != null) {
            for (int i = 0; i < days.length; i++) {
                mDays.put(days[i], true);
            }
        }
        if (sunDayBtn.getId() == R.id.toggle_button_su) {
            for (int i = 0; i < DAYS.length; i++) {
                final int day = DAYS_IW[i];
                c.set(Calendar.DAY_OF_WEEK, day);
                if (mDays.get(day) == true) {
                    toggleDay[i] = 1;
                }
            }
        } else {
            for (int i = 0; i < DAYS.length; i++) {
                final int day = DAYS[i];
                c.set(Calendar.DAY_OF_WEEK, day);

                if (mDays.get(day) == true) {
                    toggleDay[i] = 1;
                }
            }
        }
        return toggleDay;
    }

    public void updateDayConfigValue() {
        final ZenModeConfig newConfig = ZenModeUtils.getZenModeConfig();
        newConfig.sleepMode = getToggleDayToString();

        if (ZenModeUtils.setZenModeConfig(newConfig)) {
            mConfig = newConfig;
        }
    }

    private String getToggleDayToString() {
        final StringBuilder sb = new StringBuilder(ZenModeConfig.SLEEP_MODE_DAYS_PREFIX);
        ToggleButton sunDayBtn = (ToggleButton)mToggleGroup.getChildAt(0);
        String toggleDayToString = null;

        if (sunDayBtn.getId() == R.id.toggle_button_su) {
           toggleDayToString = getToggleDayToStringIw(sb, (ToggleButton)mToggleGroup.getChildAt(0));
        } else {
           toggleDayToString = getToggleDayToStringNormal(sb, (ToggleButton)mToggleGroup.getChildAt(mDay.length - 1));
        }

        return toggleDayToString;
    }

    private String getToggleDayToStringIw(StringBuilder sb, ToggleButton sunDayBtn) {
        boolean needComma = false;
        boolean emptyCheck = true;
        
        if (sunDayBtn.isChecked()) {
           sb.append(DAYS_IW[0]);
           mDays.put(DAYS_IW[0], true);
           emptyCheck = false;
           needComma = true;
        } else {
           mDays.put(DAYS_IW[0], false);
        }

        for (int i = 1; i < mDay.length; i++) {
            ToggleButton dayBtn = (ToggleButton)mToggleGroup.getChildAt(i);
            if (dayBtn.isChecked()) {
               if (needComma) {
                sb.append(',');
               }
               sb.append(DAYS_IW[i]);
               needComma = true;
               emptyCheck = false;
               mDays.put(DAYS_IW[i], true);
              } else {
               mDays.put(DAYS_IW[i], false);
            }
        }

        if (emptyCheck) {
           ZenModeUtils.setZenModeTimeComponentEnable(false);
           return null;
        } else {
           ZenModeUtils.setZenModeTimeComponentEnable(true);
           return sb.toString();
        }
    }

    private String getToggleDayToStringNormal(StringBuilder sb, ToggleButton sunDayBtn) {
        boolean needComma = false;
        boolean emptyCheck = true;

        if (sunDayBtn.isChecked()) {
            sb.append(DAYS[mDay.length - 1]);
            mDays.put(DAYS[mDay.length - 1], true);
            emptyCheck = false;
            needComma = true;
        } else {
            mDays.put(DAYS[mDay.length - 1], false);
        }

        for (int i = 0; i < mDay.length - 1; i++) {
            ToggleButton dayBtn = (ToggleButton)mToggleGroup.getChildAt(i);
            if (dayBtn.isChecked()) {
              if (needComma) {
               sb.append(',');
              }
              sb.append(DAYS[i]);
              needComma = true;
              emptyCheck = false;
              mDays.put(DAYS[i], true);
              } else {
              mDays.put(DAYS[i], false);
            }
        }

        if (emptyCheck) {
           ZenModeUtils.setZenModeTimeComponentEnable(false);                   
           return null;
        } else {
           ZenModeUtils.setZenModeTimeComponentEnable(true);
           return sb.toString();
        }
    }

    private void setToggleButtonListener() {
        for (int days = 0; days < Time.WEEK_DAY; days++) {
            if (null != mToggleGroup.getChildAt(days)) {
                mToggleGroup.getChildAt(days).setOnClickListener(this);
                mToggleGroup.getChildAt(days).setOnTouchListener(this);
            }
        }
    }
    @Override
    public void onClick(final View v) {
        Log.d(TAG, "onClick");
        updateDayConfigValue();

    }

    @Override
    public boolean onTouch(View arg0, MotionEvent arg1) {
        return false;
    }

    private void showToggleDayChecked() {
        for ( int i = 0; i < Time.WEEK_DAY; i++ ) {
            ToggleButton dayBtn = (ToggleButton)mToggleGroup.getChildAt(i);
            boolean isChecked = (mDay[i] == UNCHECK) ? false : true;
            if (dayBtn != null) {
                dayBtn.setChecked(isChecked);
            }
        }
    }

    private void setDescription() {
        mDescription = new ArrayList<String>();
        mDescription.add(mRes.getString(R.string.sp_monday_SHORT));
        mDescription.add(mRes.getString(R.string.sp_tuesday_SHORT));
        mDescription.add(mRes.getString(R.string.sp_wednesday_SHORT));
        mDescription.add(mRes.getString(R.string.sp_thursday_SHORT));
        mDescription.add(mRes.getString(R.string.sp_friday_SHORT));
        mDescription.add(mRes.getString(R.string.sp_saturday_SHORT));
        mDescription.add(mRes.getString(R.string.sp_sunday_SHORT));
    }
    public void onResume(ZenModeConfig config) {
        if (mConfig.sleepMode == config.sleepMode) {
            return;
        }

        Log.d(TAG, "onResume");
        mConfig = config;
        for (int i = 0; i < DAYS.length; i++) {
            mDays.put(DAYS[i], false);
        }
        mDay = getToggleDay();
        showToggleDayChecked();
    }
}
