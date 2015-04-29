package com.android.settings;

import java.lang.reflect.Method;
import java.util.Date;
import com.android.settings.Utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class DisplaySizeinfo {

    static final String TAG = "soosin";

    private static final boolean ARE_DISPLAY_GET_RAW_WIDTH_AND_HEIGHT_SUPPORTED = true;

    /**
     * Indicator whether {@link Display#getRealMetrics(DisplayMetrics)} is supported.
     */
    private static final boolean IS_DISPLAY_GET_REAL_METRICS_SUPPORTED = true;

    private DisplayMetrics mTempDisplayMetrics;
    private Method mMethod_Display_getRealMetrics;
    private Method mMethod_Display_getRawWidth;
    private Object[] mArgs1 = new Object[1];
    private WindowManager mWindowManager;
    private Context mContext;

    public DisplaySizeinfo(Context _context) {
        // TODO Auto-generated constructor stub
        // default value set
        mContext = _context.getApplicationContext();

    }

    public float getSquaredDiagonalLengthOfLcdInInches() {
        DisplayMetrics dm = getRealMetrics();
        if (dm == null) {
            return 0f;
        }

        //L.d(TAG, "debug : display metrics - %s", dm);

        final float width = getWidthOfLcdInInches(dm);
        final float height = getHeightOfLcdInInches(dm);

        Log.i("displaysizeinfo", "width :" + width);
        Log.i("displaysizeinfo", "height :" + height);
        final float squaredDiagonalLength = width * width + height * height;
        return squaredDiagonalLength;
    }

    public final DisplayMetrics getRealMetrics() {
        final Display display = getDefaultDisplay();
        if (display == null) {
            Log.e(TAG, "failure : default Display cannot be obtained.");
            return null;
        }

        final DisplayMetrics dm = mTempDisplayMetrics;
        if (IS_DISPLAY_GET_REAL_METRICS_SUPPORTED) {
            if (getRealMetricsInternal(display, dm) == false) {
                Log.e(TAG, "failure : getRealMetricsInternal() has been failed.");
                return null;
            } else {
                return dm;
            }
        } else if (ARE_DISPLAY_GET_RAW_WIDTH_AND_HEIGHT_SUPPORTED) {
            display.getMetrics(dm);
            int width = getRawWidthInternal(display);
            int height = getRawHeightInternal(display);
            if (width > 0 && height > 0) {
                dm.widthPixels = width;
                dm.heightPixels = height;
                return dm;
            } else {
                //if (Debug.enabled(Debug.LOG_LEVEL_E)) {
                //    Debug.e(TAG, "failure : invalid width & height - [%d,%d]", width, height);
                //}
                return null;
            }
        }

        // unsupported.
        //    Log.e(TAG, "failure : unsupported SDK level - %d", LgeIme.SDK);
        return null;
    }

    private Display getDefaultDisplay() {

        final WindowManager wm = getWindowManager();

        if (wm == null) {
            Log.e(TAG, "failure : WindowManager cannot be obtained.");
            return null;
        }

        if (mTempDisplayMetrics == null) {
            mTempDisplayMetrics = new DisplayMetrics();
        }
        final Display display = wm.getDefaultDisplay();
        return display;
    }

    private WindowManager getWindowManager() {
        // TODO Auto-generated method stub
        if (mWindowManager != null) {
            return mWindowManager;
        }

        if (mContext == null) {
            return null;
        }

        mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);

        return mWindowManager;
    }

    public float getHeightOfLcdInInches() {
        DisplayMetrics dm = getRealMetrics();
        if (dm == null) {
            return 0f;
        }
        return getHeightOfLcdInInches(dm);
    }

    public float getWidthOfLcdInInches() {
        DisplayMetrics dm = getRealMetrics();
        if (dm == null) {
            return 0f;
        }
        return getWidthOfLcdInInches(dm);
    }

    private final float getHeightOfLcdInInches(DisplayMetrics dm) {
        return dm.heightPixels / dm.ydpi;
    }

    private final float getWidthOfLcdInInches(DisplayMetrics dm) {
        return dm.widthPixels / dm.xdpi;
    }

    private final boolean getRealMetricsInternal(Display display, DisplayMetrics dm) {
        if (!IS_DISPLAY_GET_REAL_METRICS_SUPPORTED) {
            return false;
        }

        Method method = mMethod_Display_getRealMetrics;
        if (method == null) {
            try {
                method = Display.class.getMethod("getRealMetrics", DisplayMetrics.class);
                mMethod_Display_getRealMetrics = method;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                Log.e(TAG,
                        "failure : Display.getRealMetrics(DisplayMetrics) method cannot be found.");
                return false;
            }
        }

        mArgs1[0] = dm;
        try {
            method.invoke(display, mArgs1);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "failure : invocation failure.");
            return false;
        } finally {
            mArgs1[0] = null;
        }
        return true;
    }

    private final int getRawWidthInternal(Display display) {
        if (!ARE_DISPLAY_GET_RAW_WIDTH_AND_HEIGHT_SUPPORTED) {
            return -1;
        }

        Method method = mMethod_Display_getRawWidth;
        if (method == null) {
            try {
                method = Display.class.getMethod("getRawWidth");
                mMethod_Display_getRawWidth = method;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                Log.e(TAG, "failure : Display.getRawWidth() method cannot be found.");
                return -1;
            }
        }

        try {
            Object rv = method.invoke(display);
            if (rv != null && rv instanceof Integer) {
                return ((Integer)rv).intValue();
            } else {
                //        Log.e(TAG, "failure : invalid returned value. - %s", rv);
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "failure : invocation failure.");
            return -1;
        }
    }

    private final int getRawHeightInternal(Display display) {
        if (!ARE_DISPLAY_GET_RAW_WIDTH_AND_HEIGHT_SUPPORTED) {
            return -1;
        }

        Method method = mMethod_Display_getRawWidth;
        if (method == null) {
            try {
                method = Display.class.getMethod("getRawWidth");
                mMethod_Display_getRawWidth = method;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                Log.e(TAG, "failure : Display.getRawWidth() method cannot be found.");
                return -1;
            }
        }

        try {
            Object rv = method.invoke(display);
            if (rv != null && rv instanceof Integer) {
                return ((Integer)rv).intValue();
            } else {
                //    Log.e(TAG, "failure : invalid returned value. - %s", rv);
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "failure : invocation failure.");
            return -1;
        }
    }

}
