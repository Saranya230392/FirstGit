package com.android.internal;

import com.android.settings.Utils;

public final class R {

	public static final class id {
		private final static String TYPE = id.class.getSimpleName();
		public static final int summary = getInternalRes(TYPE, "summary");
		public static final int alwaysUse = getInternalRes(TYPE, "alwaysUse");
		public static final int empty = getInternalRes(TYPE, "empty");
		public static final int alertTitle = getInternalRes(TYPE, "alertTitle");
		public static final int seekbar = getInternalRes(TYPE, "seekbar");
		public static final int titleDivider = getInternalRes(TYPE, "titleDivider");
		public static final int title = getInternalRes(TYPE, "title");
		public static final int customPanel = getInternalRes(TYPE, "customPanel");
		public static final int radio = getInternalRes(TYPE, "radio");
		public static final int button1 = getInternalRes(TYPE, "button1");
		public static final int headers = getInternalRes(TYPE, "headers");
		public static final int prefs_frame = getInternalRes(TYPE, "prefs_frame");
		public static final int breadcrumb_section = getInternalRes(TYPE, "breadcrumb_section");
		public static final int prefs = getInternalRes(TYPE, "prefs");
		public static final int perm_icon = getInternalRes(TYPE, "perm_icon");
		public static final int permission_group = getInternalRes(TYPE, "permission_group");
		public static final int permission_list = getInternalRes(TYPE, "permission_list");
		public static final int body = getInternalRes(TYPE, "body");
		
		/*Wi-fi & BT resources*/
		
		public static final int widget_frame = getInternalRes(TYPE, "widget_frame");
		public static final int split_action_bar = getInternalRes(TYPE, "split_action_bar");
		public static final int account_name = getInternalRes(TYPE, "account_name");
		public static final int button3 = getInternalRes(TYPE, "button3");
		public static final int button_bar = getInternalRes(TYPE, "button_bar");
		public static final int back_button = getInternalRes(TYPE, "back_button");
		public static final int skip_button = getInternalRes(TYPE, "skip_button");
		public static final int next_button = getInternalRes(TYPE, "next_button");
	}

	public static final class attr {
		private final static String TYPE = attr.class.getSimpleName();
		public static final int checkBoxPreferenceStyle = getInternalRes(TYPE, "checkBoxPreferenceStyle");
		public static final int preferenceStyle = getInternalRes(TYPE, "preferenceStyle");
		public static final int seekBarStyle = getInternalRes(TYPE, "seekBarStyle");
		public static final int alertDialogStyle = getInternalRes(TYPE, "alertDialogStyle");
		public static final int preferenceCategoryStyle = getInternalRes(TYPE, "preferenceCategoryStyle");
	}
	
	public static final class integer {
		private final static String TYPE = integer.class.getSimpleName();
		public static final int config_longPressOnPowerBehavior = getInternalRes(TYPE, "config_longPressOnPowerBehavior");
		public static final int config_screenBrightnessDim = getInternalRes(TYPE, "config_screenBrightnessDim");
		public static final int config_longPressOnHomeBehavior = getInternalRes(TYPE, "config_longPressOnHomeBehavior");
	}
	
	public static final class bool {
		private final static String TYPE = bool.class.getSimpleName();
		public static final int config_automatic_brightness_available = getInternalRes(TYPE, "config_automatic_brightness_available");
		public static final int config_voice_capable = getInternalRes(TYPE, "config_voice_capable");
		public static final int config_dreamsSupported = getInternalRes(TYPE, "config_dreamsSupported");
		public static final int config_intrusiveNotificationLed = getInternalRes(TYPE, "config_intrusiveNotificationLed");
		public static final int config_dreamsEnabledByDefault = getInternalRes(TYPE, "config_dreamsEnabledByDefault");
		public static final int config_dreamsActivatedOnSleepByDefault = getInternalRes(TYPE, "config_dreamsActivatedOnSleepByDefault");
		public static final int config_dreamsActivatedOnDockByDefault = getInternalRes(TYPE, "config_dreamsActivatedOnDockByDefault");
		public static final int config_wimaxEnabled = getInternalRes(TYPE, "config_wimaxEnabled");
		public static final int config_cellBroadcastAppLinks = getInternalRes(TYPE, "config_cellBroadcastAppLinks");
	}
	
	public static final class drawable {
		private final static String TYPE = drawable.class.getSimpleName();
		public static final int ic_menu_close_clear_cancel = getInternalRes(TYPE, "ic_menu_close_clear_cancel");
		public static final int sym_app_on_sd_unavailable_icon = getInternalRes(TYPE, "sym_app_on_sd_unavailable_icon");
		public static final int ic_menu_archive = getInternalRes(TYPE, "ic_menu_archive");
		public static final int ic_menu_goto = getInternalRes(TYPE, "ic_menu_goto");
		public static final int expander_ic_minimized = getInternalRes(TYPE, "expander_ic_minimized");
		public static final int expander_ic_maximized = getInternalRes(TYPE, "expander_ic_maximized");
		public static final int ic_menu_info_details = getInternalRes(TYPE, "ic_menu_info_details");
		public static final int btn_circle = getInternalRes(TYPE, "btn_circle");
		public static final int ic_btn_round_more = getInternalRes(TYPE, "ic_btn_round_more");
		public static final int ic_print = getInternalRes(TYPE, "ic_print");
		public static final int ic_print_error = getInternalRes(TYPE, "ic_print_error");
		public static final int ic_menu_cc = getInternalRes(TYPE, "ic_menu_cc");
		public static final int ic_text_dot = getInternalRes(TYPE, "ic_text_dot");
		public static final int ic_audio_vol = getInternalRes(TYPE, "ic_audio_vol");
		public static final int ic_audio_ring_notif_mute = getInternalRes(TYPE, "ic_audio_ring_notif_mute");
		public static final int ic_corp_icon = getInternalRes(TYPE, "ic_corp_icon");
		
		/*Wi-fi & BT resources*/
		public static final int sym_def_app_icon = getInternalRes(TYPE, "sym_def_app_icon");
	}
	
	public static final class layout {
		private final static String TYPE = layout.class.getSimpleName();
		public static final int always_use_checkbox = getInternalRes(TYPE, "always_use_checkbox");
		public static final int select_dialog_singlechoice = getInternalRes(TYPE, "select_dialog_singlechoice");
		public static final int simple_list_item_2_single_choice = getInternalRes(TYPE, "simple_list_item_2_single_choice");
		public static final int simple_list_item_single_choice = getInternalRes(TYPE, "simple_list_item_single_choice");
		public static final int app_permission_item_old = getInternalRes(TYPE, "app_permission_item_old");
		public static final int preference_list_fragment = getInternalRes(TYPE, "preference_list_fragment");
		public static final int select_dialog_singlechoice_holo = getInternalRes(TYPE, "select_dialog_singlechoice_holo");
		public static final int select_dialog_singlechoice_material = getInternalRes(TYPE, "select_dialog_singlechoice_material");
	}

       public static final class xml {
              private final static String TYPE = "xml";
              public static final int time_zones_by_country = getInternalRes(TYPE, "time_zones_by_country");
       }
	
	public static final class dimen {
		private final static String TYPE = dimen.class.getSimpleName();
		public static final int status_bar_height = getInternalRes(TYPE, "status_bar_height");
	}
	
	public static final class styleable {
		private final static String TYPE = styleable.class.getSimpleName();
		public static final int [] ProgressBar = getInternalResArray(TYPE, "ProgressBar");
		public static final int ProgressBar_max = getInternalRes(TYPE, "ProgressBar_max");
		public static final int [] TextAppearance = getInternalResArray(TYPE, "TextAppearance");
		public static final int TextAppearance_textColor = getInternalRes(TYPE, "TextAppearance_textColor");
		public static final int TextAppearance_textSize = getInternalRes(TYPE, "TextAppearance_textSize");
		public static final int TextAppearance_typeface = getInternalRes(TYPE, "TextAppearance_typeface");
		public static final int TextAppearance_textStyle = getInternalRes(TYPE, "TextAppearance_textStyle");
		public static final int [] Dream = getInternalResArray(TYPE, "Dream");
		public static final int Dream_settingsActivity = getInternalRes(TYPE, "Dream_settingsActivity");
		public static final int [] AlertDialog = getInternalResArray(TYPE, "AlertDialog");
		public static final int AlertDialog_singleChoiceItemLayout = getInternalRes(TYPE, "AlertDialog_singleChoiceItemLayout");
		public static final int [] TextView = getInternalResArray(TYPE, "TextView");
		public static final int TextView_maxLength = getInternalRes(TYPE, "TextView_maxLength");
		public static final int TextView_text = getInternalRes(TYPE, "TextView_text");
		public static final int checkBoxPreferenceStyle = getInternalRes(TYPE, "checkBoxPreferenceStyle");
		public static final int [] PreferenceHeader = getInternalResArray(TYPE, "PreferenceHeader");
		public static final int PreferenceHeader_id = getInternalRes(TYPE, "PreferenceHeader_id");
		public static final int PreferenceHeader_title = getInternalRes(TYPE, "PreferenceHeader_title");
		public static final int PreferenceHeader_summary = getInternalRes(TYPE, "PreferenceHeader_summary");
		public static final int PreferenceHeader_breadCrumbTitle = getInternalRes(TYPE, "PreferenceHeader_breadCrumbTitle");
		public static final int PreferenceHeader_breadCrumbShortTitle = getInternalRes(TYPE, "PreferenceHeader_breadCrumbShortTitle");
		public static final int PreferenceHeader_icon = getInternalRes(TYPE, "PreferenceHeader_icon");
		public static final int PreferenceHeader_fragment = getInternalRes(TYPE, "PreferenceHeader_fragment");
		public static final int [] RecognitionService = getInternalResArray(TYPE, "RecognitionService");
		public static final int RecognitionService_settingsActivity = getInternalRes(TYPE, "RecognitionService_settingsActivity");
		public static final int [] VolumePreference = getInternalResArray(TYPE, "VolumePreference");

		public static final int []TrustAgent = getInternalResArray(TYPE, "TrustAgent");
		public static final int TrustAgent_summary = getInternalRes(TYPE, "TrustAgent_summary");
		public static final int TrustAgent_title = getInternalRes(TYPE, "TrustAgent_title");
		public static final int TrustAgent_settingsActivity = getInternalRes(TYPE, "TrustAgent_settingsActivity");

        public static final int[] Preference = getInternalResArray(TYPE,
                "Preference");
        public static final int Preference_key = getInternalRes(TYPE,
                "Preference_key");
        public static final int Preference_title = getInternalRes(TYPE,
                "Preference_title");
        public static final int Preference_summary = getInternalRes(TYPE,
                "Preference_summary");
        public static final int[] CheckBoxPreference = getInternalResArray(
                TYPE, "CheckBoxPreference");
        public static final int CheckBoxPreference_summaryOn = getInternalRes(
                TYPE, "CheckBoxPreference_summaryOn");
        public static final int CheckBoxPreference_summaryOff = getInternalRes(
                TYPE, "CheckBoxPreference_summaryOff");
        public static final int[] ListPreference = getInternalResArray(TYPE,
                "CheckBoxPreference");
        public static final int ListPreference_entries = getInternalRes(TYPE,
                "CheckBoxPreference_summaryOn");
	}	
	
	public static final class string {
		private final static String TYPE = string.class.getSimpleName();
		public static final int report = getInternalRes(TYPE, "report");
		public static final int bugreport_message = getInternalRes(TYPE, "bugreport_message");
		public static final int gigabyteShort = getInternalRes(TYPE, "gigabyteShort");
		public static final int megabyteShort = getInternalRes(TYPE, "megabyteShort");
		public static final int byteShort = getInternalRes(TYPE, "byteShort");
		public static final int kilobyteShort = getInternalRes(TYPE, "kilobyteShort");
		public static final int fileSizeSuffix = getInternalRes(TYPE, "fileSizeSuffix");
		public static final int wifi_tether_configure_ssid_default = getInternalRes(TYPE, "wifi_tether_configure_ssid_default");
		public static final int default_sms_application = getInternalRes(TYPE, "default_sms_application");
//		public static final int device_admin_extra_service = getInternalRes(TYPE, "device_admin_extra_service");
//		public static final int device_admin_service_bluetooth = getInternalRes(TYPE, "device_admin_service_bluetooth");
		public static final int ringtone_unknown = getInternalRes(TYPE, "ringtone_unknown");
		public static final int ringtone_silent = getInternalRes(TYPE, "ringtone_silent");
		public static final int ringtone_default_with_actual = getInternalRes(TYPE, "ringtone_default_with_actual");
		public static final int terabyteShort = getInternalRes(TYPE, "terabyteShort");
		public static final int petabyteShort = getInternalRes(TYPE, "petabyteShort");
		public static final int ok = getInternalRes(TYPE, "ok");
		public static final int ssl_certificate = getInternalRes(TYPE, "ssl_certificate");
		public static final int fast_scroll_alphabet = getInternalRes(TYPE, "fast_scroll_alphabet");
	}
	
	public static final class array {
		private final static String TYPE = array.class.getSimpleName();
		public static final int config_mobile_hotspot_provision_app = getInternalRes(TYPE, "config_mobile_hotspot_provision_app");
		public static final int special_locale_codes = getInternalRes(TYPE, "special_locale_codes");
		public static final int special_locale_names = getInternalRes(TYPE, "special_locale_names");
	}
	
	private static int getInternalRes(String type, String name) {
		return Utils.getInternalRes("com.android.internal.R$", type, name);
	}
	
	private static int[] getInternalResArray(String type, String name) {
		return Utils.getInternalResArray("com.android.internal.R$", type, name);
	}	
	
}