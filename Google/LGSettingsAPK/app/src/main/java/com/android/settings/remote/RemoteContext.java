package com.android.settings.remote;

import android.app.ActivityThread;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class RemoteContext extends ContextWrapper {
    private static final String TAG = "RemoteContext";
    Context mBase;
    ClassLoader mMyClassLoader;

    public RemoteContext(Context base) {
        super(base);
        mBase = base;
        ApplicationInfo ai = mBase.getApplicationInfo();
        String sourceDir = ai.sourceDir;
        Log.d(TAG, "sourceDir=" + sourceDir + ", package=" + getPackageName());
        mMyClassLoader = new PathClassLoader(sourceDir, mBase.getClassLoader().getParent()) {

            @Override
            protected Class<?> loadClass(String className, boolean resolve)
                    throws ClassNotFoundException {
                return super.loadClass(className, resolve);
                //                return findClass(className);
            }

        };
    }

    @Override
    public ClassLoader getClassLoader() {
        if (mMyClassLoader != null) {
            return mMyClassLoader;
        } else {
            return super.getClassLoader();
        }
    }
}