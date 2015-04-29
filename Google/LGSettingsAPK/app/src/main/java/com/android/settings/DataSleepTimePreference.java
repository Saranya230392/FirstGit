package com.android.settings;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.settings.DataSleepTimeInfo;
import com.android.settings.R;
import com.android.settings.Utils;
import com.lge.sui.widget.control.SUIDrumTimePicker;
import com.lge.sui.widget.dialog.SUIDrumTimePickerDialog;
import android.widget.LinearLayout;
import com.android.settings.Utils;

public class DataSleepTimePreference extends Preference
        implements OnClickListener, SUIDrumTimePickerDialog.OnTimeSetListener {
    static final String TAG = "DataSleepTimePreference";
    static final String START_TIME_BTN = "start_time_btn";
    static final String END_TIME_BTN = "end_time_btn";
    private Context mContext;
    private Button mTimeStartBtn;
    private Button mTimeEndBtn;
    private Button mTimePikerOKButton;
    private DataSleepInfo mDataSleepInfo;
    private AlertDialog dialog = null;
    private String mPosition = START_TIME_BTN;
    private int mHour = DataSleepInfo.DEFAULT_EMPTY;
    private int mMinute = DataSleepInfo.DEFAULT_EMPTY;

    public DataSleepTimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setLayoutResource(R.layout.data_sleep_timeset);
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        SLog.d(TAG, "getView");
        return super.getView(convertView, parent);
    }

    @Override
    protected void onBindView(View view) {
        SLog.d(TAG, "onBindView");
        super.onBindView(view);
        mTimeStartBtn = (Button)view.findViewById(R.id.set_time_start);
        mTimeStartBtn.setOnClickListener(this);
        mTimeEndBtn = (Button)view.findViewById(R.id.set_time_end);
        mTimeEndBtn.setOnClickListener(this);
        if (Utils.isEnglishDigitRTLLanguage() ||
                java.util.Locale.getDefault().getLanguage().equals("fa")) {
            LinearLayout mDataSleepLayout = (LinearLayout)view
                    .findViewById(R.id.time_selector_group);
            mDataSleepLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        mDataSleepInfo = new DataSleepInfo(mContext);
        //mDataSleepInfo.initDataSleepTime();
        mTimeStartBtn.setText(
                mDataSleepInfo.set24TimeString(
                        true,
                        mDataSleepInfo.getTimeString(mDataSleepInfo.getDBDataSleepStartTime())));
        mTimeEndBtn
                .setText(
                mDataSleepInfo.set24TimeString(
                        false, mDataSleepInfo.getTimeString(mDataSleepInfo.getDBDataSleepEndTime())));
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        // TODO Auto-generated method stub
        SLog.d(TAG, "onCreateView");
        return super.onCreateView(parent);
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {
        case R.id.set_time_start:
            mPosition = START_TIME_BTN;
            mHour = mDataSleepInfo.getDBDataSleepStartTimeHour();
            mMinute = mDataSleepInfo.getDBDataSleepStartTimeMinute();
            SLog.i("R.id.set_time_start" );
            createDialog().show();
            break;
        case R.id.set_time_end:
            mPosition = END_TIME_BTN;
            mHour = mDataSleepInfo.getDBDataSleepEndTimeHour();
            mMinute = mDataSleepInfo.getDBDataSleepEndTimeMinute();
            SLog.i("R.id.set_time_end" );
            createDialog().show();
            break;
        default:
            break;
        }
    }

    @Override
    public void onTimeSet(SUIDrumTimePicker view, int hourOfDay, int minute) {
        // TODO Auto-generated method stub
        mHour = hourOfDay;
        mMinute = minute;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.AM_PM, 0);
        calendar.set(Calendar.HOUR, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        if (mPosition.equals(START_TIME_BTN)) {
            SLog.i("onTimeSet START_TIME_BTN mHour = " + mHour);
            SLog.i("onTimeSet START_TIME_BTN mMinute = " + mMinute);
            mDataSleepInfo.setDBDataSleepStartTimeHour(mHour);
            mDataSleepInfo.setDBDataSleepStartTimeMinute(mMinute);
            mTimeStartBtn.setText(
                    mDataSleepInfo.set24TimeString(
                            true,
                            mDataSleepInfo.getTimeString(calendar.getTimeInMillis())));
        } else if (mPosition.equals(END_TIME_BTN)) {
            SLog.i("onTimeSet END_TIME_BT NmHour = " + mHour);
            SLog.i("onTimeSet mEND_TIME_BTN Minute = " + mMinute);
            mDataSleepInfo.setDBDataSleepEndTimeHour(mHour);
            mDataSleepInfo.setDBDataSleepEndTimeMinute(mMinute);
            mTimeEndBtn.setText(
                    mDataSleepInfo.set24TimeString(
                            false, mDataSleepInfo.getTimeString(calendar.getTimeInMillis())));
        }
    }

    private void updateTime(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        Date mDate;
        long removeSecond;
        calendar.set(Calendar.AM_PM, 0);
        calendar.set(Calendar.HOUR, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));

        if (mPosition.equals(START_TIME_BTN)) {
            SLog.i(TAG, "updateTime Start hour : " + mDataSleepInfo.getDBDataSleepStartTimeHour());
            SLog.i(TAG,
                    "updateTime Start minite : " + mDataSleepInfo.getDBDataSleepStartTimeMinute());

            removeSecond = calendar.getTimeInMillis() / DataSleepInfo.REMOVE_SECOND;
            removeSecond = removeSecond * DataSleepInfo.REMOVE_SECOND;

            mDataSleepInfo.setDBDataSleepStartTime(removeSecond);
            SLog.i(TAG, "updateTime Start time : " + mDataSleepInfo.getDBDataSleepStartTime());

            // end time set
            calendar.set(Calendar.AM_PM, 0);
            calendar.set(Calendar.HOUR, mDataSleepInfo.getDBDataSleepEndTimeHour());
            calendar.set(Calendar.MINUTE, mDataSleepInfo.getDBDataSleepEndTimeMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH,
                    calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));

            removeSecond = calendar.getTimeInMillis() / DataSleepInfo.REMOVE_SECOND;
            removeSecond = removeSecond * DataSleepInfo.REMOVE_SECOND;
            mDataSleepInfo.setDBDataSleepEndTime(removeSecond);
        } else if (mPosition.equals(END_TIME_BTN)) {

            SLog.i(TAG, "updateTime End hour : " + mDataSleepInfo.getDBDataSleepEndTimeHour());
            SLog.i(TAG, "updateTime End minite : " + mDataSleepInfo.getDBDataSleepEndTimeMinute());

            removeSecond = calendar.getTimeInMillis() / DataSleepInfo.REMOVE_SECOND;
            removeSecond = removeSecond * DataSleepInfo.REMOVE_SECOND;
            mDataSleepInfo.setDBDataSleepEndTime(removeSecond);
            SLog.i(TAG, "updateTime End time : " + mDataSleepInfo.getDBDataSleepEndTime());

            // start time set
            calendar.set(Calendar.AM_PM, 0);
            calendar.set(Calendar.HOUR, mDataSleepInfo.getDBDataSleepStartTimeHour());
            calendar.set(Calendar.MINUTE, mDataSleepInfo.getDBDataSleepStartTimeMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH,
                    calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
            removeSecond = calendar.getTimeInMillis() / DataSleepInfo.REMOVE_SECOND;
            removeSecond = removeSecond * DataSleepInfo.REMOVE_SECOND;
            mDataSleepInfo.setDBDataSleepStartTime(removeSecond);
        }

        mDate = new Date(mDataSleepInfo.getDBDataSleepEndTime());
        SLog.i(TAG, "updateTime before date : " + mDate.getTime());
        SLog.i(TAG, "updateTime Start time : " + mDataSleepInfo.getDBDataSleepStartTime());
        SLog.i(TAG, "updateTime End time : " + mDataSleepInfo.getDBDataSleepEndTime());

        if (mDataSleepInfo.getDBDataSleepEndTime() < mDataSleepInfo.getDBDataSleepStartTime()) {
            SLog.i(TAG, "updateTime after day case ");
            mDate.setTime(mDate.getTime() + DataSleepInfo.ONE_DAY);
            mDataSleepInfo.setDBDataSleepEndTime(mDate.getTime());
        }
        SLog.i(TAG, "updateTime after Start time : " + mDataSleepInfo.getDBDataSleepStartTime() +
                "                   " +
                mDataSleepInfo.getDayInfo(mDataSleepInfo.getDBDataSleepStartTime()));
        SLog.i(TAG, "updateTime after End time : " + mDataSleepInfo.getDBDataSleepEndTime() +
                "                   " +
                mDataSleepInfo.getDayInfo(mDataSleepInfo.getDBDataSleepEndTime()));

        SLog.i(TAG, "updateTime after end full string : " + mDate.toGMTString());
        mDate.setTime(mDataSleepInfo.getDBDataSleepStartTime());
        SLog.i(TAG, "updateTime after start full string : " + mDate.toGMTString());
        mDate = null;
    }

    private DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface _dialog) {
                    // TODO Auto-generated method stub
                    SLog.d(TAG, "popup dismiss");
                    SLog.d(TAG, "OnDismissListener mHour " + mHour);
                    SLog.d(TAG, "OnDismissListener mMinute " + mMinute);
                    //updateTime(mHour, mMinute);
                    dialog = null;
                }
            };

    private SUIDrumTimePickerDialog.TitleBuilder mTitleBuilder =
            new SUIDrumTimePickerDialog.TitleBuilder() {

                @Override
                public String getTitle(Calendar c, int dateFormat) {
                    // TODO Auto-generated method stub
                    int hour = c.get(java.util.Calendar.HOUR_OF_DAY);
                    int minute = c.get(java.util.Calendar.MINUTE);

                    if (END_TIME_BTN.equals(mPosition)) {
                        if (hour == mDataSleepInfo.getDBDataSleepStartTimeHour() &&
                                minute == mDataSleepInfo.getDBDataSleepStartTimeMinute()) {
                            if (null != mTimePikerOKButton) {
                                mTimePikerOKButton.setEnabled(false);
                            }
                        }
                        else {
                            if (null != mTimePikerOKButton) {
                                mTimePikerOKButton.setEnabled(true);
                            }
                        }
                    }
                    else {
                        if (hour == mDataSleepInfo.getDBDataSleepEndTimeHour() &&
                                minute == mDataSleepInfo.getDBDataSleepEndTimeMinute()) {
                            if (null != mTimePikerOKButton) {
                                mTimePikerOKButton.setEnabled(false);
                            }
                        }
                        else {
                            if (null != mTimePikerOKButton) {
                                mTimePikerOKButton.setEnabled(true);
                            }
                        }
                    }
                    SLog.i(TAG, "[TitleBuilder]" +
                            " mPosition : " + mPosition +
                            " hour : " + hour +
                            " minute : " + minute);
                    return mContext.getString(R.string.date_time_set_time);
                }
            };

    private Dialog createDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        SLog.i(TAG, "getDBDataSleepStartTimeHour : " + mDataSleepInfo.getDBDataSleepStartTimeHour());
        SLog.i(TAG,
                "getDBDataSleepStartTimeHour : " + mDataSleepInfo.getDBDataSleepStartTimeMinute());
        SLog.i(TAG, "getDBDataSleepEndTimeHour : " + mDataSleepInfo.getDBDataSleepEndTimeHour());
        SLog.i(TAG, "getDBDataSleepEndTimeHour : " + mDataSleepInfo.getDBDataSleepEndTimeMinute());

        int hour = 0;
        int minute = 0;
        if (mPosition.equals(START_TIME_BTN)) {
            hour = mDataSleepInfo.getDBDataSleepStartTimeHour();
            minute = mDataSleepInfo.getDBDataSleepStartTimeMinute();
        } else if (mPosition.equals(END_TIME_BTN)) {
            hour = mDataSleepInfo.getDBDataSleepEndTimeHour();
            minute = mDataSleepInfo.getDBDataSleepEndTimeMinute();
        }

        if (dialog == null) {
            dialog =
                    new SUIDrumTimePickerDialog(mContext
                            , this
                            , hour
                            , minute
                            , 0
                            , 0
                            , 0
                            , true
                            , mTitleBuilder
                    );

            SUIDrumTimePicker timePicker = ((SUIDrumTimePickerDialog)dialog).getTimePicker();
            if (DateFormat.is24HourFormat(mContext)) {
                timePicker.setTimeFormat(SUIDrumTimePicker.TIMEFORMAT_24_HHMM);
            } else {
                timePicker.setTimeFormat(SUIDrumTimePicker.TIMEFORMAT_12_AMPM);
            }

            dialog.setOnDismissListener(mOnDismissListener);

            dialog.setOnShowListener(new OnShowListener() {

                @Override
                public void onShow(DialogInterface dialog) {
                    // TODO Auto-generated method stub
                    mTimePikerOKButton = ((AlertDialog)dialog)
                            .getButton(AlertDialog.BUTTON_POSITIVE);
                    mTimePikerOKButton.setText(R.string.band_mode_set);
                }
            });
        }
        return dialog;
    }

    /*
        private void initDialogTime() {
            Calendar c = Calendar.getInstance();

            c.setTimeInMillis(mDataSleepInfo.getDBDataSleepStartTime());
            mDataSleepInfo.setDBDataSleepStartTimeHour(c.getTime().getHours());
            mDataSleepInfo.setDBDataSleepStartTimeMinute(c.getTime().getMinutes());

            c.setTimeInMillis(mDataSleepInfo.getDBDataSleepEndTime());
            mDataSleepInfo.setDBDataSleepEndTimeHour(c.getTime().getHours());
            mDataSleepInfo.setDBDataSleepEndTimeMinute(c.getTime().getMinutes());
        }
    */
    public void onResume(DataSleepInfo mInfo) {
        SLog.i("onResume");
        if (null == mDataSleepInfo) {
            mDataSleepInfo = mInfo;
        }

        //initDialogTime();
        mDataSleepInfo.initDataSleepTime();

        if (null != mTimeStartBtn && null != mTimeEndBtn) {
            mTimeStartBtn
                    .setText(
                    mDataSleepInfo.set24TimeString(
                            true,
                            mDataSleepInfo.getTimeString(mDataSleepInfo.getDBDataSleepStartTime())));
            mTimeEndBtn.setText(
                    mDataSleepInfo.set24TimeString(
                            false,
                            mDataSleepInfo.getTimeString(mDataSleepInfo.getDBDataSleepEndTime())));
        }
    }

    public void onPause() {
        SLog.i("onPause");
        save();
    }

    public void save() {
        // time Picker no showing case
        //if (mHour == DataSleepInfo.DEFAULT_EMPTY && mMinute == DataSleepInfo.DEFAULT_EMPTY) {
        //    SLog.i("DataSleepTimePreference save() init");
            mPosition = START_TIME_BTN;
            updateTime(
                    mDataSleepInfo.getDBDataSleepStartTimeHour(),
                    mDataSleepInfo.getDBDataSleepStartTimeMinute());
        //}
        SLog.i("DataSleepTimePreference save() ");
    }
}
