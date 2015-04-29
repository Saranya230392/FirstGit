package com.android.settings.accessibility;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;

abstract class ScreenContentObserver extends ContentObserver {
    public ScreenContentObserver(Handler handler) {
        super(handler);
    }

    public void register(ContentResolver contentResolver) {
        contentResolver.registerContentObserver(Settings.Secure.getUriFor(
                Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED), true, this);
    }

    public void unregister(ContentResolver contentResolver) {
        contentResolver.unregisterContentObserver(this);
    }

    @Override
    public abstract void onChange(boolean selfChange, Uri uri);
}
