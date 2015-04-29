package com.android.settings.unusedapps.utils;

public class Enums {

    public static final String DAY_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final long TIME_ONE_SEC = 1000;
    public static final long TIME_ONE_MINUTE = TIME_ONE_SEC * 60;
    public static final long TIME_ONE_HOUR = TIME_ONE_MINUTE * 60;
    public static final long TIME_ONE_DAY = TIME_ONE_HOUR * 24;
    public static final long TIME_ONE_WEEK = TIME_ONE_DAY * 7;

    public static final int APP_CLEANUP_NOTIFICATION_ID = 0x1011;

    public static final String ACTION_UNUSED_APP_NOTI =
            "com.lge.appcleanup.action.UNUSED_APP_NOTI";
    public static final String ACTION_SAVE_APP_USAGE_STATS =
            "com.lge.appcleanup.action.SAVE_APP_USAGE_STATS";
    public static final String ACTION_NOTICE_INTERVAL_UPDATED =
            "com.lge.appcleanup.action.NOTICE_INTERVAL_UPDATED";
    public static final String EXTRA_SWITCH_ONOFF =
            "com.lge.appcleanup.extra_switch_onoff";
    public static final String EXTRA_START_FROM_NOTI =
            "com.lge.appcleanup.extra_start_from_noti";

    public static final String DEFAULT_PREFERENCES_NAME =
            "com.lge.appcleanup_preferences";
    public static final String DEFAULT_PREFERENCES_START_SERVICE_KEY =
            "settings_service_key_start";
    public static final String DEFAULT_PREFERENCES_NOTICE_DATE_KEY =
            "key_notice_date";
    public static final String DEFAULT_PREFERENCES_UNUSED_APPS_LIST_KEY =
            "key_unused_apps_list";
    public static final String DEFAULT_PREFERENCES_NOTICE_INTERVAL_KEY =
            "key_notice_interval";
    public static final String DEFAULT_PREFERENCES_UNUSED_PERIOD_KEY =
            "key_unused_period";
    public static final String DEFAULT_PREFERENCES_UNUSED_PERIOD_DEFAULT_KEY =
            "1";
    public static final String DEFAULT_PREFERENCES_NOTICE_INTERVAL_DEFAULT_KEY =
            "2";
    public static final long NOTICE_INTERVAL_NONE = -1000;
}
