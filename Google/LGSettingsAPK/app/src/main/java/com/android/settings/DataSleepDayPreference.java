package com.android.settings;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.preference.Preference;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;
import com.android.settings.R;
import com.lge.sui.widget.control.SUIToggleButtonGroup;

public class DataSleepDayPreference extends Preference implements OnClickListener {
    private static final String TAG = "DataSleepDayPreference";
    private static final int CHECK = 1;
    private static final int UNCHECK = 0;

    private Context mContext;
    private SUIToggleButtonGroup toggleGroup;
    private int[] day = { 0, 0, 0, 0, 0, 0, 0 };
    private DataSleepInfo mDataSleepInfo;
    private ArrayList<String> mDescription;
    private Resources mRes;

    public DataSleepDayPreference(Context context, AttributeSet attrs) {
        // TODO Auto-generated constructor stub
        super(context, attrs);
        mContext = context;
        mRes = mContext.getResources();
        Locale clsLocale = mContext.getResources().getConfiguration().locale;
        if ("iw".equals(clsLocale.getLanguage())) {
            setLayoutResource(R.layout.data_sleep_dayset_iw);
        } else {
            setLayoutResource(R.layout.data_sleep_dayset);
        }
    }

    @Override
    protected final void onBindView(final View view) {
        // TODO Auto-generated method stub
        SLog.d(TAG, "onBindView");
        super.onBindView(view);
        toggleGroup = (SUIToggleButtonGroup)view.findViewById(R.id.toggle_button_group);
        setDescription();
        try {
            for (int days = 0; days < Time.WEEK_DAY; days++) {
                if (toggleGroup.getChildAt(days) != null) {
                    toggleGroup.getChildAt(days).setContentDescription(
                            mDescription.get(days));
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        setToggleButtonListener();
        mDataSleepInfo = new DataSleepInfo(mContext);
        day = mDataSleepInfo.getToggleDayToArray();
        showToggleDayChecked();

        Locale clsLocale = mContext.getResources().getConfiguration().locale;
        SLog.d("language info : " + clsLocale.getLanguage());
    }

    public void onResume(final DataSleepInfo mInfo) {
        if (null == mDataSleepInfo) {
            mDataSleepInfo = mInfo;
        }

        if (null != toggleGroup) {
            showToggleDayChecked();
        }
    }

    public void onPause() {
        save();
    }

    public void save() {
        mDataSleepInfo.setDBDataSleepDays(getToggleDayToString());
        SLog.i("DataSleepDayPreference save()");
    }

    private String getToggleDayToString() {
        StringBuilder s = new StringBuilder();
        int current_day = 0;
        try {

            ToggleButton sunBtn = (ToggleButton)toggleGroup.getChildAt(0);
            if (sunBtn.getId() == R.id.toggle_button_su) {
                for (int i = DataSleepInfo.MON_IW; i < Time.WEEK_DAY; i++) {
                    ToggleButton dayBtn = (ToggleButton)toggleGroup.getChildAt(i);
                    if (dayBtn != null) {
                        if (dayBtn.isChecked()) {
                            day[i + DataSleepInfo.SHIFT_DAY] = CHECK;
                        } else {
                            day[i + DataSleepInfo.SHIFT_DAY] = UNCHECK;
                        }
                    }
                    s.append(day[i + DataSleepInfo.SHIFT_DAY]);
                }
                if (sunBtn.isChecked()) {
                    day[DataSleepInfo.SUN] = CHECK;
                } else {
                    day[DataSleepInfo.SUN] = UNCHECK;
                }
                s.append(day[DataSleepInfo.SUN]);
            }
            else {
                for (int i = 0; i < Time.WEEK_DAY; i++) {
                    current_day = i;
                    ToggleButton dayBtn = (ToggleButton)toggleGroup.getChildAt(i);
                    if (dayBtn != null) {
                        if (dayBtn.isChecked()) {
                            day[i] = CHECK;
                        } else {
                            day[i] = UNCHECK;
                        }
                    }
                    s.append(day[i]);
                }
            }
        } catch (NullPointerException e) {
            Log.i(TAG, "Toggle button null point exception :" + current_day);
            return DataSleepInfo.DEFAULT_DAY;
        }
        return String.valueOf(s);
    }

    private void setToggleButtonListener() {
        for (int days = 0; days < Time.WEEK_DAY; days++) {
            if (null != toggleGroup.getChildAt(days)) {
                toggleGroup.getChildAt(days).setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(final View v) {
        // TODO Auto-generated method stub
        //save();
        SLog.i("onClick");
    }

    private void showToggleDayChecked() {
        ToggleButton sunBtn = (ToggleButton)toggleGroup.getChildAt(0);
        if (sunBtn.getId() == R.id.toggle_button_su) {
            Log.i("soosin", "[getToggleDayToString] IW case");
            boolean isChecked = (day[DataSleepInfo.SUN] == DataSleepInfo.OFF) ? false : true;
            sunBtn.setChecked(isChecked);

            // mon - dat    523
            for (int i = 0; i < Time.WEEK_DAY + DataSleepInfo.SHIFT_DAY; i++) {
                ToggleButton dayBtn = (ToggleButton)toggleGroup
                        .getChildAt(i + DataSleepInfo.MON_IW);

                isChecked = (day[i] == UNCHECK) ? false : true;
                if (dayBtn != null) {
                    dayBtn.setChecked(isChecked);
                }
            }
        }

        else {
            for (int i = 0; i < Time.WEEK_DAY; i++) {
                ToggleButton dayBtn = (ToggleButton)toggleGroup.getChildAt(i);

                boolean isChecked = (day[i] == UNCHECK) ? false : true;
                if (dayBtn != null) {
                    dayBtn.setChecked(isChecked);
                }
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
}
