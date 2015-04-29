package com.android.settings.dragndrop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.DisplayMetrics;
import android.os.SystemProperties;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.Utils;

public class ButtonConfiguration {
    private static final String TAG = "ButtonConfiguration";
    public static int CONFIG_MAX_UP_TRAY_KEY_NUM;
    public static int CONFIG_FIXED_DOWN_TRAY_KEY_NUM;
    public static int CONFIG_INITIAL_UP_TRAY_KEY_NUM;

    public static List<String> DOWN_TRAY_ITEMS = new ArrayList<String>();
    public static List<String> UP_TRAY_ITEMS = new ArrayList<String>();

    public static HashMap<String, ButtonInfo> mButtonInfos = new HashMap<String, ButtonInfo>();

    public static class ButtonInfo {
        public int mDrawableId;
        public int mStringId;

        public ButtonInfo(int drawableId, int stringId) {
            mDrawableId = drawableId;
            mStringId = stringId;
        }
    }

    public static void configure(Context context) {
        CONFIG_MAX_UP_TRAY_KEY_NUM = context.getResources().getInteger(
                R.integer.up_tray_max_items_count);
        CONFIG_INITIAL_UP_TRAY_KEY_NUM = context.getResources().getInteger(
                R.integer.up_tray_initial_items_count);
        setUpTrayItems(context);
        setDownTrayItems(context);
    }

    public static void resetConfigure() {
    	UP_TRAY_ITEMS.clear();
        DOWN_TRAY_ITEMS.clear();
        //mButtonInfos.clear();
    }

    private static void setDownTrayItems(Context context) {
        if (context == null) {
            return;
        }

        if (DOWN_TRAY_ITEMS.size() == 0) {
            Log.d("chan", "setDownTrayItems");
            String[] items = context.getResources().getStringArray(R.array.down_tray_items);
            for (int i = 0; i < items.length; i++) {
                if ("SimSwitch".equals(items[i])) {
                    if (!Utils.isMultiSimEnabled() && !Utils.isTripleSimEnabled()) {
                        continue;
                    }
                }

                if ("QSlide".equals(items[i])) {
                    if (!Utils.isSupportQslide()) {
                        continue;
                    }
                }

                if ("QMemo".equals(items[i])) {
                    if (Utils.isUI_4_1_model(context)) {
                        if (!isCheckQmemo(context)) {
                            continue;
                        }
                    }
                }

                if ("DualWindow".equals(items[i])) {
                    boolean mSupportDualWindow = SystemProperties.getBoolean(
                            "ro.lge.capp_splitwindow", false);
                    if (!Utils.isUI_4_1_model(context)
                            || !mSupportDualWindow) {
                        continue;
                    }
                }

                DOWN_TRAY_ITEMS.add(items[i]);
                mButtonInfos.put(items[i], new ButtonInfo(getDrawableId(items[i]),
                        getStringId(items[i])));
            }
        }
        CONFIG_FIXED_DOWN_TRAY_KEY_NUM = DOWN_TRAY_ITEMS.size();
    }

    private static void setUpTrayItems(Context context) {
        if (context == null) {
            return;
        }

        if (UP_TRAY_ITEMS.size() == 0) {
            String[] items = context.getResources().getStringArray(R.array.up_tray_items);
            for (int i = 0; i < items.length; i++) {
                UP_TRAY_ITEMS.add(items[i]);
                mButtonInfos.put(items[i], new ButtonInfo(getDrawableId(items[i]),
                        getStringId(items[i])));
            }
        }
    }

    public static List<String> getUpTrayItems(Context context) {
        if (UP_TRAY_ITEMS.size() == 0) {
            setUpTrayItems(context);
        }
        return UP_TRAY_ITEMS;
    }

    public static List<String> getDownTrayItems(Context context) {
        if (DOWN_TRAY_ITEMS.size() == 0) {
            setDownTrayItems(context);
        }
        return DOWN_TRAY_ITEMS;
    }

    public static HashMap<String, ButtonInfo> getButtonInfos() {
        return mButtonInfos;
    }

    public static int getColumnOfDownTray(String key) {
        if (DOWN_TRAY_ITEMS != null) {
            for (int i = 0; i < DOWN_TRAY_ITEMS.size(); i++) {
                if (key.equalsIgnoreCase(DOWN_TRAY_ITEMS.get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int getDrawableId(String key) {
        if ("Back".equalsIgnoreCase(key)) {
            return R.drawable.setting_button_combination_icon_back;
        } else if ("Home".equalsIgnoreCase(key)) {
            return R.drawable.setting_button_combination_icon_home;
        } else if ("Menu".equalsIgnoreCase(key)) {
            return R.drawable.setting_button_combination_icon_menu;
        } else if ("Notification".equalsIgnoreCase(key)) {
            return R.drawable.setting_button_combination_icon_notification;
        } else if ("QMemo".equalsIgnoreCase(key)) {
            return R.drawable.setting_button_combination_icon_qmemo;
        } else if ("RecentApps".equalsIgnoreCase(key)) {
            return R.drawable.setting_button_combination_icon_recentapps;
        } else if ("SimSwitch".equalsIgnoreCase(key)) {
            return R.drawable.setting_button_combination_icon_sim;
        } else if ("QSlide".equalsIgnoreCase(key)) {
            return R.drawable.setting_button_combination_icon_qslide;
        } else if ("DualWindow".equalsIgnoreCase(key)) {
            return R.drawable.setting_button_combination_icon_dualwindow;
        }
        return 0;
    }

    private static int getStringId(String key) {
        if ("Back".equalsIgnoreCase(key)) {
            return R.string.setting_button_combination_back;
        } else if ("Home".equalsIgnoreCase(key)) {
            return R.string.setting_button_combination_home;
        } else if ("Menu".equalsIgnoreCase(key)) {
            return R.string.setting_button_combination_menu;
        } else if ("Notification".equalsIgnoreCase(key)) {
            return R.string.notification_home_button;
        } else if ("QMemo".equalsIgnoreCase(key)) {
            return R.string.setting_button_combination_qmemo;
        } else if ("RecentApps".equalsIgnoreCase(key)) {
            return R.string.setting_button_combination_recentapps;
        } else if ("SimSwitch".equalsIgnoreCase(key)) {
            return R.string.setting_button_combination_sim;
        } else if ("QSlide".equalsIgnoreCase(key)) {
            return R.string.setting_button_combination_qslide;
        } else if ("DualWindow".equalsIgnoreCase(key)) {
            return R.string.app_split_view_dual_window;
        }
        return 0;
    }

    private static boolean isCheckQmemo(Context context) {
        PackageInfo pi = null;
        PackageManager pm = context.getPackageManager();
        String mQmemoPackageName = "com.lge.qmemoplus";

        try {
            pi = pm.getPackageInfo(mQmemoPackageName, PackageManager.GET_ACTIVITIES);
            if (pi.packageName.equals(mQmemoPackageName)) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            Log.w(TAG, "package is not found(" + mQmemoPackageName + ")");
            return false;
        }
    }
}