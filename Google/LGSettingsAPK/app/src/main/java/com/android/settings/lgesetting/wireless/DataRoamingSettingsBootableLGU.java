package com.android.settings.lgesetting.wireless;

import android.os.Bundle;

//this is a roaming settings at boot time
//must be : block of Home key
//request : LGU only
public class DataRoamingSettingsBootableLGU extends DataRoamingSettingsLGU {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setIsBootable();
        super.onCreate(savedInstanceState);
    }

}