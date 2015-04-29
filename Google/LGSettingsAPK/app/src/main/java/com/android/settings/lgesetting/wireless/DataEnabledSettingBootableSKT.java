package com.android.settings.lgesetting.wireless;

import android.os.Bundle;

//this is a SKT Data Popup at boot time
//must be : block of Home key
//request : SKT only
public class DataEnabledSettingBootableSKT extends DataNetworkModeSetting {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setIsBootable();
        super.onCreate(savedInstanceState);
    }

}