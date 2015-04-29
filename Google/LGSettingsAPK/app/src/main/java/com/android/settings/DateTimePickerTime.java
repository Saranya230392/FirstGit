package com.android.settings;

import java.util.Calendar;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Window;
import android.widget.TimePicker;

public class DateTimePickerTime extends Activity {
    private static final int DIALOG_TIMEPICKER = 1;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        showDialog(DIALOG_TIMEPICKER);
    }
    @Override
    public Dialog onCreateDialog(int id) {
        Dialog d;

        switch (id) {
        case DIALOG_TIMEPICKER: {
            final Calendar calendar = Calendar.getInstance();
            d = new TimePickerDialog(
                    this,
                    timePickerListener,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false);
            break;
        }
        default:
            d = null;
            break;
        }
        //WBT fix
        if(d!=null){
            d.setOnDismissListener(mOnDismissListener);
            d.setCanceledOnTouchOutside(false);
        }
        //WBT fix
        return d;
    }
    private TimePickerDialog.OnTimeSetListener timePickerListener
    = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker arg0, int hourOfDay, int minute) {
            // TODO Auto-generated method stub
            Calendar c = Calendar.getInstance();

            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long when = c.getTimeInMillis();

            if (when / 1000 < Integer.MAX_VALUE) {
                SystemClock.setCurrentTimeMillis(when);
            }
            finish();
        }
    };
    private DialogInterface.OnDismissListener mOnDismissListener =
        new DialogInterface.OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            finish();
        }
    };
}