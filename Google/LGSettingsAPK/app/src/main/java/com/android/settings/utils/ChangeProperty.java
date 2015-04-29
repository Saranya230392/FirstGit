package com.android.settings.utils;

import com.android.settings.R;
import java.util.ArrayList;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.SystemProperties;

public class ChangeProperty extends Activity {
    Spinner mCountry;
    Spinner mOperator;
    Button mChange;
    TextView mChangedText;
    String setting_country;
    String setting_operator;
    ArrayList<String> arraylist_oper;
    ArrayList<String> arraylist_country;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.change_properties);

        mCountry = (Spinner)findViewById(R.id.spn_country);
        mOperator = (Spinner)findViewById(R.id.spn_oper);
        mChange = (Button)findViewById(R.id.btn_change);
        mChangedText = (TextView)findViewById(R.id.edt_changed);
        setting_country = "";
        setting_operator = "";

        arraylist_country = new ArrayList<String>();
        arraylist_country.add("");
        arraylist_country.add("KR");
        arraylist_country.add("US");
        arraylist_country.add("JP");
        arraylist_country.add("MX");
        arraylist_country.add("CN");
        arraylist_country.add("ES");
        arraylist_country.add("GB");
        arraylist_country.add("FR");
        arraylist_country.add("HK");
        arraylist_country.add("TW");
        arraylist_country.add("NZ");

        arraylist_oper = new ArrayList<String>();
        arraylist_oper.add("");
        arraylist_oper.add("LGU");
        arraylist_oper.add("SKT");
        arraylist_oper.add("KT");
        arraylist_oper.add("VZW");
        arraylist_oper.add("ATT");
        arraylist_oper.add("DCM");
        arraylist_oper.add("MPCS");
        arraylist_oper.add("SPR");
        arraylist_oper.add("TCL");
        arraylist_oper.add("TLS");
        arraylist_oper.add("BELL");
        arraylist_oper.add("USC");
        arraylist_oper.add("KDDI");
        arraylist_oper.add("TMO");
        arraylist_oper.add("VDF");

        ArrayAdapter<String> adapter_country = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arraylist_country);
        ArrayAdapter<String> adapter_oper = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arraylist_oper);

        mCountry.setPrompt("Select country");
        mCountry.setAdapter(adapter_country);
        mCountry.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                // TODO Auto-generated method stub
                setting_country = arraylist_country.get(arg2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        mOperator.setPrompt("Select operator");
        mOperator.setAdapter(adapter_oper);
        mOperator.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                // TODO Auto-generated method stub
                setting_operator = arraylist_oper.get(arg2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        mChange.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //if (mCountry.getText() != null) {
                    SystemProperties.set("sys.settings.country", setting_country);
                //}

            //    if (mOperator.getText() != null) {
                    SystemProperties.set("sys.settings.operator", setting_operator);
            //    }

                mChangedText.setText("Target country : " + setting_country + "\n" + "Target operator : " + setting_operator + "\n set!!!!" );
            }
        });
    }

}
