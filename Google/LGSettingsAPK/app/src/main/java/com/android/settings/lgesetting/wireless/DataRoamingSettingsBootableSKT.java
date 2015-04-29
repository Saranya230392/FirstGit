package com.android.settings.lgesetting.wireless;

import android.os.Bundle;

//this is a roaming settings at boot time
//must be : block of Home key
//request : SKT only
public class DataRoamingSettingsBootableSKT extends DataRoamingSettingsSKT {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setIsBootable();
        super.onCreate(savedInstanceState);
    }

}