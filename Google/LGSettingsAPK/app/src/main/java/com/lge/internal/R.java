package com.lge.internal;

import com.android.settings.Utils;

public final class R {

	public static final class drawable {
		private final static String TYPE = drawable.class.getSimpleName();

		public static final int btn_check_holo_light = getInternalRes(TYPE, "btn_check_holo_light");
		public static final int scrubber_control_disabled_holo = getInternalRes(TYPE, "scrubber_control_disabled_holo");
		public static final int scrubber_control_pressed_holo = getInternalRes(TYPE, "scrubber_control_pressed_holo");
		public static final int scrubber_control_focused_holo = getInternalRes(TYPE, "scrubber_control_focused_holo");
		public static final int scrubber_control_normal_holo = getInternalRes(TYPE, "scrubber_control_normal_holo");
		public static final int btn_default_transparent = getInternalRes(TYPE, "btn_default_transparent");
		public static final int list_selector_background = getInternalRes(TYPE, "list_selector_background");
	
		/* Wi-fi & BT resources */
		public static final int ic_dialog_alert_holo_light = getInternalRes(TYPE, "ic_dialog_alert_holo_light");
	}

	private static int getInternalRes(String type, String name) {
		return Utils.getInternalRes("com.lge.internal.R$", type, name);
	}
	
	private static int[] getInternalResArray(String type, String name) {
		return Utils.getInternalResArray("com.lge.internal.R$", type, name);
	}	
}