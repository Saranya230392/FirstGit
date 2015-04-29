package com.android.settings;

import android.util.Log;

public class SLog {

    public static String appName = "Settings : ";
    private static boolean IsDebugLog = true;

    // =====
    public static void v(String Msg) {
        if (!IsDebugLog) {
            return;
        }
        Log.v(appName, new Throwable().getStackTrace()[1].getFileName() + "["
                + new Throwable().getStackTrace()[1].getLineNumber() + "] : "
                + Msg);
    }

    public static void v(String Tag, String Msg) {
        if (!IsDebugLog) {
            return;
        }

        Log.v(Tag, appName + new Throwable().getStackTrace()[1].getFileName()
                + "[" + new Throwable().getStackTrace()[1].getLineNumber()
                + "] : " + Msg);
    }

    public static void v(String Tag, String Msg, Throwable tr) {
        if (!IsDebugLog) {
            return;
        }
        Log.v(Tag, appName + new Throwable().getStackTrace()[1].getFileName()
                + "[" + new Throwable().getStackTrace()[1].getLineNumber()
                + "] : " + Msg, tr);
    }

    // =====
    public static void d(String Msg) {
        if (!IsDebugLog) {
            return;
        }

        Log.d(appName, new Throwable().getStackTrace()[1].getFileName() + "["
                + new Throwable().getStackTrace()[1].getLineNumber() + "] : "
                + Msg);
    }

    public static void d(String Tag, String Msg) {
        if (!IsDebugLog) {
            return;
        }

        Log.d(Tag, appName + new Throwable().getStackTrace()[1].getFileName()
                + "[" + new Throwable().getStackTrace()[1].getLineNumber()
                + "] : " + Msg);
    }

    public static void d(String Tag, String Msg, Throwable tr) {
        if (!IsDebugLog) {
            return;
        }
        Log.d(Tag, appName + new Throwable().getStackTrace()[1].getFileName()
                + "[" + new Throwable().getStackTrace()[1].getLineNumber()
                + "] : " + Msg, tr);
    }

    // =====
    public static void i(String Msg) {
        if (!IsDebugLog) {
            return;
        }

        Log.i(appName, new Throwable().getStackTrace()[1].getFileName() + "["
                + new Throwable().getStackTrace()[1].getLineNumber() + "] : "
                + Msg);
    }

    public static void i(String Tag, String Msg) {
        if (!IsDebugLog) {
            return;
        }

        Log.i(Tag, appName + new Throwable().getStackTrace()[1].getFileName()
                + "[" + new Throwable().getStackTrace()[1].getLineNumber()
                + "] : " + Msg);
    }

    public static void i(String Tag, String Msg, Throwable tr) {
        if (!IsDebugLog) {
            return;
        }
        Log.i(Tag, appName + new Throwable().getStackTrace()[1].getFileName()
                + "[" + new Throwable().getStackTrace()[1].getLineNumber()
                + "] : " + Msg, tr);
    }

    // =====
    public static void w(String Msg) {
        if (!IsDebugLog) {
            return;
        }

        Log.w(appName, new Throwable().getStackTrace()[1].getFileName() + "["
                + new Throwable().getStackTrace()[1].getLineNumber() + "] : "
                + Msg);
    }

    public static void w(String Tag, String Msg) {
        if (!IsDebugLog) {
            return;
        }

        Log.w(Tag, appName + new Throwable().getStackTrace()[1].getFileName()
                + "[" + new Throwable().getStackTrace()[1].getLineNumber()
                + "] : " + Msg);
    }

    public static void w(String Tag, String Msg, Throwable tr) {
        if (!IsDebugLog) {
            return;
        }

        Log.w(Tag, appName + new Throwable().getStackTrace()[1].getFileName()
                + "[" + new Throwable().getStackTrace()[1].getLineNumber()
                + "] : " + Msg, tr);
    }

    // =====
    public static void e(String Msg) {
        if (!IsDebugLog) {
            return;
        }

        Log.e(appName, new Throwable().getStackTrace()[1].getFileName() + "["
                + new Throwable().getStackTrace()[1].getLineNumber() + "] : "
                + Msg);
    }

    public static void e(String Tag, String Msg) {
        if (!IsDebugLog) {
            return;
        }

        Log.e(Tag, appName + new Throwable().getStackTrace()[1].getFileName()
                + "[" + new Throwable().getStackTrace()[1].getLineNumber()
                + "] : " + Msg);
    }

    public static void e(String Tag, String Msg, Throwable tr) {
        if (!IsDebugLog) {
            return;
        }

        Log.e(Tag, appName + new Throwable().getStackTrace()[1].getFileName()
                + "[" + new Throwable().getStackTrace()[1].getLineNumber()
                + "] : " + Msg, tr);

    }
    // =====
}
