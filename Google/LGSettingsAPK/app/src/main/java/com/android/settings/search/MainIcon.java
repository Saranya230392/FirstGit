package com.android.settings.search;



import com.android.settings.Utils;

import java.util.HashMap;

import com.android.settings.R;

public class MainIcon {

    private static HashMap<String, Integer> sIconMap = new HashMap<String, Integer>();
    public static final int RANK_OTHERS = R.drawable.ic_au_setting;
    public static final String AU_SETTINGS = "au_settings";
    public static final String AIRPLANE = "airplane";
    public static final String DUAL_SIM = "dual_sim";
    public static final String TRIPLE_SIM = "triple_sim";
    public static final String WIFI = "wifi";
    public static final String BLUETOOTH = "bluetooth";
    public static final String DATA_NETWORKS = "Data networks";
    public static final String DATA_MANAGER = "Data Manager";    
    public static final String DATA_USAGE = "data_usage";    
    public static final String MOBILE_DATA = "Mobile data";            
    public static final String USE_4G_NETWORK = "use_4g_network";
    public static final String ADVANCED_CALLING = "advanced_calling";
    public static final String AU_CUSTOMER_SUPPORT = "au_customer_support";
    public static final String ROAMING = "roaming";
    public static final String T_ROAMING = "t_roaming";
    public static final String RCS = "rcs";
    public static final String SHARE_CONNECT = "share_connect";
    public static final String MORE = "more";
    public static final String TETHER_NETWORK_SETTINGS = "tether_network_settings";
    public static final String TETHERING = "tethering";
    public static final String SOUND = "sound";
    public static final String SOUND_NOTIFICATOIN = "sound_notification";
    public static final String DISPLAY = "display";
    public static final String HOME_SCREEN = "home_screen";
    public static final String LOCK_SCREEN = "lock_screen";
    public static final String T_ACTION = "t_action";
    public static final String ONE_HAND = "one_hand";
    public static final String DUAL_WINDOW = "dual_window";
    public static final String STORAGE = "storage";
    public static final String BATTERY = "battery";
    public static final String SAFETY_CLEANER = "safety_cleaner";
    public static final String SMART_CLEANING = "smart_cleaning";
    public static final String APPS = "apps";
    public static final String APPLICATION_MANAGER = "application_manager";
    public static final String DEFAULT_SETTINGS = "default_settings";
    public static final String FLIP_CLOSE_SETTINGS = "flip_close_settings";
    public static final String YAHOO_BOX = "yahoo_box";
    public static final String DOCOMO_SETTINGS = "docomo_settings";
    public static final String CLOUD = "cloud";
    public static final String MULTI_USER = "multi_user";
    public static final String LOCATION = "location";
    public static final String SECURITY = "security";
    public static final String SAFETY_CARE = "safety_care";
    public static final String ACCOUNTS = "accounts";
    public static final String ACCOUNTS_SYNC = "accounts_sync";
    public static final String LANGUAGE = "language";
    public static final String PRIVACY = "privacy";
    public static final String SHORTCUT_KEY = "shortcut_key";
    public static final String DATE_TIME = "date_time";
    public static final String ACCESSIBILITY = "accessibility";
    public static final String QUICK_CIRCLE = "quick_circle";
    public static final String QUICK_COVER = "quick_cover";
    public static final String ACTIVATE_DEVICE = "activate_device";
    public static final String PRINT = "print";
    public static final String ABOUT = "about";
    public static final String SWUPDATE = "swupate";
    
    private static int[] SETTINGS_TITLE_RESID = {
            R.string.au_setting_title,
            R.string.airplane_mode,
            R.string.sp_dual_sim_menu_new_NORMAL,
            R.string.sp_triple_sim_menu_new_NORMAL,
            R.string.wifi_settings_title,
            R.string.bluetooth_settings_title,
            R.string.data_network_settings_title,
            R.string.shortcut_datausage_att,
            R.string.data_usage_summary_title,
            R.string.data_usage_enable_mobile,
            R.string.sp_use_4g_network_title,
            R.string.advance_calling_main_title,
            R.string.sp_au_customer_support_NORMAL,
            R.string.roaming_lgt_title,
            R.string.sp_troaming_NORMAL,
            R.string.sp_chat_service_NORMAL,
            R.string.sp_title_share_connect_NORMAL_jb_plus,
            R.string.wireless_more_settings_title,
            R.string.sp_title_tether_network_NORMAL_jb_plus,
            R.string.wireless_tethering_settings_title,
            R.string.sound_settings,
            R.string.notification_settings,
            R.string.display_settings,
            R.string.sp_screen_home_screen_title_NORMAL,
            R.string.sp_screen_lock_screen_title_NORMAL,
            R.string.taction_title,
            R.string.one_handed_operation,
            R.string.app_split_view_dual_window,
            R.string.storage_settings,
            R.string.sp_battery_power_saving_title,
            R.string.sp_safety_cleaner_title,
            R.string.smart_cleaning,
            R.string.applications_settings,
            R.string.applications_settings_title,
            R.string.sms_application_title2,
            R.string.sp_flip_settings_title,
            R.string.sp_yahoo_box_menu,
            R.string.docomo_settings_title_addcloud,
            R.string.settings_menu_cloud,
            R.string.user_list_title,
            R.string.location_settings_title_kk,
            R.string.security_settings_title,
            R.string.safety_care_app_title,
            R.string.account_settings,
            R.string.sync_settings,
            R.string.language_settings,
            R.string.privacy_settings,
            R.string.sp_shortcut_key_NORMAL,
            R.string.date_and_time_settings_title,
            R.string.accessibility_settings,
            R.string.quick_circle_case_title,
            R.string.quickcover_title,
            R.string.sp_settings_activate_this_device_title_NORMAL,
            R.string.print_settings,
            R.string.about_settings,
            R.string.system_update_settings_list_item_title
    };
    
    private static String[] SETTINGS_TITLE_STRING = {
            AU_SETTINGS,
            AIRPLANE,
            DUAL_SIM,
            TRIPLE_SIM,
            WIFI,
            BLUETOOTH,
            DATA_NETWORKS ,
            DATA_MANAGER,
            DATA_USAGE,
            MOBILE_DATA,
            USE_4G_NETWORK,
            ADVANCED_CALLING,
            AU_CUSTOMER_SUPPORT,
            ROAMING,
            T_ROAMING,
            RCS,
            SHARE_CONNECT,
            MORE,
            TETHER_NETWORK_SETTINGS,
            TETHERING,
            SOUND,
            SOUND_NOTIFICATOIN,
            DISPLAY,
            HOME_SCREEN,
            LOCK_SCREEN,
            T_ACTION,
            ONE_HAND,
            DUAL_WINDOW,
            STORAGE,
            BATTERY,
            SAFETY_CLEANER,
            SMART_CLEANING,
            APPS,
            APPLICATION_MANAGER,
            DEFAULT_SETTINGS,
            FLIP_CLOSE_SETTINGS,
            YAHOO_BOX,
            DOCOMO_SETTINGS,
            CLOUD,
            MULTI_USER,
            LOCATION,
            SECURITY,
            SAFETY_CARE,
            ACCOUNTS,
            ACCOUNTS_SYNC,
            LANGUAGE,
            PRIVACY,
            SHORTCUT_KEY,
            DATE_TIME,
            ACCESSIBILITY,
            QUICK_CIRCLE,
            QUICK_COVER,
            ACTIVATE_DEVICE,
            PRINT,
            ABOUT,
            SWUPDATE 
    };
    
    static {
        sIconMap.put(AU_SETTINGS, R.drawable.ic_au_setting);
        sIconMap.put(AIRPLANE, R.drawable.ic_airplane);
        sIconMap.put(DUAL_SIM, R.drawable.ic_list_dualsim);
        sIconMap.put(TRIPLE_SIM, R.drawable.ic_list_dualsim);
        sIconMap.put(WIFI, R.drawable.ic_settings_wireless);
        sIconMap.put(BLUETOOTH, R.drawable.ic_settings_bluetooth2);
        sIconMap.put(DATA_NETWORKS , R.drawable.ic_settings_data_usage);
        sIconMap.put(DATA_MANAGER , R.drawable.ic_settings_data_usage);
        sIconMap.put(DATA_USAGE , R.drawable.ic_settings_data_usage);
        sIconMap.put(MOBILE_DATA , R.drawable.ic_settings_data_usage);
        sIconMap.put(USE_4G_NETWORK, R.drawable.ic_use_4g_network);
        sIconMap.put(ADVANCED_CALLING, R.drawable.ic_advanced_calling_vzw);
        sIconMap.put(AU_CUSTOMER_SUPPORT, R.drawable.ic_au_supporticon);
        sIconMap.put(ROAMING, R.drawable.ic_settings_roaming_lgt);
        sIconMap.put(T_ROAMING, R.drawable.ic_settings_roaming_skt);
        sIconMap.put(RCS, R.drawable.ic_chat);
        sIconMap.put(SHARE_CONNECT, R.drawable.ic_settings_share_connect);
        sIconMap.put(MORE, R.drawable.empty_icon);
        sIconMap.put(TETHER_NETWORK_SETTINGS, R.drawable.ic_network);
        sIconMap.put(TETHERING, R.drawable.ic_network);
        sIconMap.put(SOUND, R.drawable.ic_settings_sound);
        sIconMap.put(SOUND_NOTIFICATOIN, R.drawable.ic_settings_sound);
        sIconMap.put(DISPLAY, R.drawable.ic_settings_display);
        sIconMap.put(HOME_SCREEN, R.drawable.ic_home_white);
        sIconMap.put(LOCK_SCREEN, R.drawable.ic_lockscreen_white);
        sIconMap.put(T_ACTION, R.drawable.ic_taction);
        sIconMap.put(ONE_HAND, R.drawable.ic_one_hand_operation);
        sIconMap.put(DUAL_WINDOW, R.drawable.ic_split_view);
        sIconMap.put(STORAGE, R.drawable.ic_settings_storage);
        sIconMap.put(BATTERY, R.drawable.ic_settings_battery);
        sIconMap.put(SAFETY_CLEANER, R.drawable.ic_safety_cleaner);
        sIconMap.put(SMART_CLEANING, R.drawable.ic_spring_cleaning);
        sIconMap.put(APPS, R.drawable.ic_settings_applications);
        sIconMap.put(APPLICATION_MANAGER, R.drawable.ic_settings_applications);
        sIconMap.put(DEFAULT_SETTINGS, R.drawable.ic_default_message);
        sIconMap.put(FLIP_CLOSE_SETTINGS, R.drawable.ic_flip);
        sIconMap.put(YAHOO_BOX, R.drawable.ic_boxicon);
        sIconMap.put(DOCOMO_SETTINGS, R.drawable.ic_settings_docomo);
        sIconMap.put(CLOUD, R.drawable.ic_cloud);
        sIconMap.put(MULTI_USER, R.drawable.ic_multi_user);
        sIconMap.put(LOCATION, R.drawable.ic_settings_location);
        sIconMap.put(SECURITY, R.drawable.ic_settings_security);
        sIconMap.put(SAFETY_CARE, R.drawable.ic_safety_care);
        sIconMap.put(ACCOUNTS, R.drawable.ic_settings_sync);
        sIconMap.put(ACCOUNTS_SYNC, R.drawable.ic_settings_sync);
        sIconMap.put(LANGUAGE, R.drawable.ic_settings_language);
        sIconMap.put(PRIVACY, R.drawable.ic_settings_backup);
        sIconMap.put(SHORTCUT_KEY, R.drawable.ic_shortcut_key);
        sIconMap.put(DATE_TIME, R.drawable.ic_settings_date_time);
        sIconMap.put(ACCESSIBILITY, R.drawable.ic_settings_accessibility);
        sIconMap.put(QUICK_CIRCLE, R.drawable.ic_quickcover_circle);
        sIconMap.put(QUICK_COVER, R.drawable.ic_smartcover);
        sIconMap.put(ACTIVATE_DEVICE, R.drawable.ic_activatedevice);
        sIconMap.put(PRINT, R.drawable.ic_print_list);
        sIconMap.put(ABOUT, R.drawable.ic_settings_about);
        sIconMap.put(SWUPDATE, R.drawable.ic_swupdate);
    }
    
    public static String getMainTtleString(int resId) {
        return Utils.getString(resId);
    }
    
    public static String getChangeTitleString(String title) {
        for (int i = 0; i < SETTINGS_TITLE_RESID.length; i++) {
            if (title.equals(getMainTtleString(SETTINGS_TITLE_RESID[i]))) {
                return SETTINGS_TITLE_STRING[i];
            }
        }
        return "";
    }

    public static int getIconkForTitle(String title) {
        String match = getChangeTitleString(title);
        Integer rank = sIconMap.get(match);
        return (rank != null) ? (int)rank : RANK_OTHERS;
    }
}
