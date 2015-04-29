package com.android.settings.utils;

import com.android.settings.R;
import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

public class CheckSettingDB extends Activity {

    private AutoCompleteTextView mDBname;
    private AutoCompleteTextView mDBsetValue;

    private TextView mDBvalue;
    private Button mDBfind;
    private Button mDBpush;

    private String[] list = {
            "smart_ringtone",
            "gentle_vibration_status",
            "dnd_auto_reply_message",
            "dnd_auto_reply_option",
            "default_vibrate_name",
            "default_vibrate_pattern",
            "default_sub_vibrate_pattern",
            "default_third_vibrate_pattern",
            "user_vibration_conut",
            "hands_free_mode_status",
            "hands_free_mode_call",
            "hands_free_mode_message",
            "hands_free_mode_read_message",
            "vibrate_when_ringing",
            "lockscreen_sounds_enabled",
            "haptic_feedback_enabled"
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_setting_db);

        setTitle("Settigns.System DB find");
        mDBname = (AutoCompleteTextView)findViewById(R.id.db_name);
        mDBname.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, list));
        mDBvalue = (TextView)findViewById(R.id.db_value);
        mDBfind = (Button)findViewById(R.id.db_find);

        mDBname.setHint("input DB name");
        mDBname.setSingleLine();
        mDBvalue.setText("Write DB name & push button");
        mDBfind.setText("find");
        mDBfind.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (true == hasFocus) {

                }
                else {
                    mDBvalue.setText("Write DB name & push button");
                }

            }
        });
        mDBfind.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Log.i("soosin", "button click!");
                if (null != mDBname.getText()) {
                    String value = Settings.System.getString(getContentResolver(), mDBname.getText().toString());

                    if (null != value) {
                        mDBvalue.setText("DB Value : " + value);
                    }
                    else {
                        mDBvalue.setText("not found value");
                    }
                }
                else {
                    mDBvalue.setText("write DB name");
                }
            }
        });

        mDBsetValue = (AutoCompleteTextView)findViewById(R.id.set_db_value);
        mDBpush = (Button)findViewById(R.id.db_push);

        mDBsetValue.setHint("write db value");
        mDBpush.setText("push");

        mDBpush.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (null == mDBsetValue.getText()) {
                    mDBvalue.setText("db push fail!!!");
                }
                else {
                    Settings.System.putString(getContentResolver(), mDBname.getText().toString(), mDBsetValue.getText().toString());

                    if (null != mDBname.getText()) {
                        String value = Settings.System.getString(getContentResolver(), mDBname.getText().toString());

                        if (null != value) {
                            mDBvalue.setText("set DB Value : " + value);
                        }
                        else {
                            mDBvalue.setText("not found value");
                        }
                    }
                    else {
                        mDBvalue.setText("write DB name");
                    }
                }

            }
        });
    }

}
