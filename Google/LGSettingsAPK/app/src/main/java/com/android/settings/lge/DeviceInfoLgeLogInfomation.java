package com.android.settings.lge;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

public class DeviceInfoLgeLogInfomation extends SettingsPreferenceFragment {

    private View mView;
    private Button mOkButton;
    private Button mCancelButton;
    private CheckBox mLogInformationCheck;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {

            if (Utils.supportSplitView(getActivity())) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        getActivity().setTitle(R.string.sp_activity_logs_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        mView = inflater.inflate(R.layout.device_info_log_information, null);

        mLogInformationCheck = (CheckBox)mView.findViewById(R.id.LogInformationCheck);
        mLogInformationCheck.setChecked(Utils.readLogInformationFile());
        mLogInformationCheck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });
        mCancelButton = (Button)mView.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        mOkButton = (Button)mView.findViewById(R.id.ok_button);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Utils.writeLogInformationFile(mLogInformationCheck.isChecked());
                finish();
            }
        });
        
        
        return mView;
    }

    

}
