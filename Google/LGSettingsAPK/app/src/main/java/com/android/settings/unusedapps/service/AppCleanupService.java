package com.android.settings.unusedapps.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
//Only for Tablet model, you can check this source at overlay/tablet/ folder
public class AppCleanupService extends Service {
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
