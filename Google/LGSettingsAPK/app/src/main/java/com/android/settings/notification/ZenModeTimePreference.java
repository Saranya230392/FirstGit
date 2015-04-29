package com.android.settings.notification;

import java.util.Calendar;
import java.util.Date;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.preference.Preference;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.service.notification.ZenModeConfig;
import com.android.settings.R;

import com.lge.sui.widget.control.SUIDrumTimePicker;
import com.lge.sui.widget.dialog.SUIDrumTimePickerDialog;
import com.android.settings.notification.ZenModeUtils;

public class ZenModeTimePreference extends Preference implements OnClickListener, SUIDrumTimePickerDialog.OnTimeSetListener {
    static final String TAG = "ZenModeTimePreference";
    static final String START_TIME_BTN = "start_time_btn";
    static final String END_TIME_BTN = "end_time_btn";
    private Context mContext;
    private Button mTimeStartBtn;
    private Button mTimeEndBtn;
    private TextView mTimeStart;
    private TextView mTimeEnd;
    private Button mTimePikerOKButton;
    private ZenModeConfig mConfig;
    private AlertDialog dialog = null;
    private String mPosition = START_TIME_BTN;

    public ZenModeTimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mConfig = ZenModeUtils.getZenModeConfig();
        setLayoutResource(R.layout.zen_mode_timeset);
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        return super.getView(convertView, parent);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mConfig = ZenModeUtils.getZenModeConfig();
        int[] days = ZenModeConfig.tryParseDays(mConfig.sleepMode);

        mTimeStart = (TextView)view.findViewById(R.id.from);
        mTimeStartBtn = (Button)view.findViewById(R.id.set_time_start);
        mTimeStartBtn.setOnClickListener(this);
        mTimeStartBtn.setText(ZenModeUtils.getZenModeTimeString(mContext, mConfig.sleepStartHour, mConfig.sleepStartMinute, false));

        mTimeEnd = (TextView)view.findViewById(R.id.to);
        mTimeEndBtn = (Button)view.findViewById(R.id.set_time_end);
        mTimeEndBtn.setOnClickListener(this);

        mTimeEndBtn.setText(ZenModeUtils.getZenModeTimeString(mContext, mConfig.sleepEndHour, mConfig.sleepEndMinute,
                            ZenModeUtils.zenModeCheckNextdayString(mConfig.sleepStartHour, mConfig.sleepStartMinute, mConfig.sleepEndHour, mConfig.sleepEndMinute)));

        ZenModeUtils.setZenModeTimeComponent(mTimeStart, mTimeStartBtn, mTimeEnd, mTimeEndBtn);

        if (days == null) {
          zenModeTimeComponentEnable(false);
        }

    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        // TODO Auto-generated method stub
        return super.onCreateView(parent);
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {
        case R.id.set_time_start:
            mPosition = START_TIME_BTN;
            createDialog().show();
            break;
        case R.id.set_time_end:
            mPosition = END_TIME_BTN;
            createDialog().show();
            break;
        default:
            break;
        }
    }

    @Override
    public void onTimeSet(SUIDrumTimePicker view, int hourOfDay, int minute) {
        if (!ZenModeConfig.isValidHour(hourOfDay)) {
            return;
        }
        if (!ZenModeConfig.isValidMinute(minute)) {
            return;
        }
        if (hourOfDay == mConfig.sleepStartHour && minute == mConfig.sleepStartMinute) {
            return;
        }

        final ZenModeConfig newConfig = ZenModeUtils.getZenModeConfig();

        if (mPosition.equals(START_TIME_BTN)) {
            newConfig.sleepStartHour = hourOfDay;
            newConfig.sleepStartMinute = minute;
        } else {
            newConfig.sleepEndHour = hourOfDay;
            newConfig.sleepEndMinute = minute;
        }

        if (ZenModeUtils.setZenModeConfig(newConfig)) {
            mConfig = newConfig;
            if (mPosition.equals(START_TIME_BTN)) {
               mTimeStartBtn.setText(ZenModeUtils.getZenModeTimeString(mContext, mConfig.sleepStartHour, mConfig.sleepStartMinute, false));
            } else if (mPosition.equals(END_TIME_BTN)) {
               mTimeEndBtn.setText(ZenModeUtils.getZenModeTimeString(mContext, mConfig.sleepEndHour, mConfig.sleepEndMinute, 
                                   ZenModeUtils.zenModeCheckNextdayString(mConfig.sleepStartHour, mConfig.sleepStartMinute, mConfig.sleepEndHour, mConfig.sleepEndMinute)));
            }
        }
    }

    private DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface _dialog) {
                    // TODO Auto-generated method stub
                    dialog = null;
                }
            };

    private SUIDrumTimePickerDialog.TitleBuilder mTitleBuilder =
            new SUIDrumTimePickerDialog.TitleBuilder() {

                @Override
                public String getTitle(Calendar c, int dateFormat) {
                    return mContext.getString(R.string.date_time_set_time);
                }
            };

    private Dialog createDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }

        int hour = 0;
        int minute = 0;
        if (mPosition.equals(START_TIME_BTN)) {
            hour = mConfig.sleepStartHour;
            minute = mConfig.sleepStartMinute;
        } else if (mPosition.equals(END_TIME_BTN)) {
            hour = mConfig.sleepEndHour;
            minute = mConfig.sleepEndMinute;
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
                    mTimePikerOKButton.setText(R.string.dlg_ok);
                }
            });
        }
        return dialog;
    }

    public void zenModeTimeComponentEnable(boolean isEnable) {
        if (mTimeStart.isEnabled()) {
            mTimeStart.setEnabled(isEnable);
            mTimeStartBtn.setEnabled(isEnable);
            mTimeEnd.setEnabled(isEnable);
            mTimeEndBtn.setEnabled(isEnable);
        }
   }

}

