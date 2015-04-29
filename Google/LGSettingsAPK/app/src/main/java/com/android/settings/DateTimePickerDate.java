package com.android.settings;

import java.util.Calendar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Window;
import android.widget.DatePicker;

public class DateTimePickerDate extends Activity  {
    /** Called when the activity is first created. */
    private static final int DIALOG_DATEPICKER = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        showDialog(DIALOG_DATEPICKER);
    }


    @Override
    public Dialog onCreateDialog(int id) {
        Dialog d;


        switch (id) {
        case DIALOG_DATEPICKER: {
            final Calendar calendar = Calendar.getInstance();
            d = new DatePickerDialog(
                    this,
                    datePickerListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
        // [S][2012.04.04][donghan07.lee][ICS Settings][Not for TD] date picker min value set to 1980_01_01
        ((DatePickerDialog) d).getDatePicker().setMinDate(315551215436L);
      //((DatePickerDialog) d).getDatePicker().setMaxDate(2145830400000L);
      //((DatePickerDialog) d).getDatePicker().setMinDate(315532800000L);
        ((DatePickerDialog) d).getDatePicker().setMaxDate(1924905606246L);
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

    private DatePickerDialog.OnDateSetListener datePickerListener
    = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int selectedYear,
                int selectedMonth, int selectedDay) {
            // TODO Auto-generated method stub

            Calendar c = Calendar.getInstance();

            c.set(Calendar.YEAR, selectedYear);
            c.set(Calendar.MONTH, selectedMonth);
            c.set(Calendar.DAY_OF_MONTH, selectedDay);
            long when = c.getTimeInMillis();

            if (when / 1000 < Integer.MAX_VALUE) {
                SystemClock.setCurrentTimeMillis(when);
            }
            Intent intent = new Intent(DateTimePickerDate.this, DateTimePickerTime.class);
                 startActivity(intent);

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