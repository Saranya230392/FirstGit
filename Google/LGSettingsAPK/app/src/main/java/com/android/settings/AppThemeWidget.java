package com.android.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;

import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

class AppThemeWidget {
    private static final String LGHOME_DEFAULT_THEME_OPTIMUS = "com.lge.launcher2.theme.optimus";
    private static final String LGHome2_PACKAGE_NAME = "com.lge.launcher2";
    //private static final String THEME_PACKAAGE_NAME = "com.lge.launcher2.theme";
    private static final String SHAREDPREF_NAME = "LGHome2_Theme";
    private static final String SHAREDPREF_CURRENT_THEME_KEY = "CurrentTheme";
    //private static final String SHAREDPREF_CURRENT_THEME_TYPE_KEY = "CurrentThemeType";
    private static final String THEME_XMLFILE_NAME = "theme_resources";
    private static final String THEME_RESOURCE_XML_TYPE = "xml";
    //private static final String THEME_LOCKSCREEN_WALLPAPER_TAG_NAME = "LockScreenWallpaper";
    private static final String THEME_RESOURCE_IMAGE_TYPE = "drawable";
    private static final int THEME_INVALID_RESOURCE_ID = 0;
    private static final String CN_WIDGET_INFO = "WidgetInfo";

    // key = widget name and value  = widget image
    private HashMap<String, String> mWidgetInfo = new HashMap<String, String>();
    private Resources mResources = null;
    private String mCurrentThemeName = null;

    public AppThemeWidget(Context context) {
        if (context == null) {
            return;
        }
        initCurrentTheme(context);
        initResource(context);
        readWidgetInfoFromTheme();
    }

    private void initCurrentTheme(Context context) {
        SharedPreferences pref = null;
        try {
            pref = context.createPackageContext(LGHome2_PACKAGE_NAME, 0).getSharedPreferences(
                    SHAREDPREF_NAME, Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (pref != null) {
            mCurrentThemeName = pref.getString(SHAREDPREF_CURRENT_THEME_KEY, null);
        }

        if (mCurrentThemeName == null) {
            mCurrentThemeName = LGHOME_DEFAULT_THEME_OPTIMUS;
        }
    }

    private void initResource(Context context) {
        PackageManager pm = context.getPackageManager();

        if (pm != null) {
            ApplicationInfo appInfo;
            try {
                appInfo = pm.getApplicationInfo(mCurrentThemeName,
                        PackageManager.GET_UNINSTALLED_PACKAGES);
                mResources = pm.getResourcesForApplication(appInfo);
            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void readWidgetInfoFromTheme() {

        try {
            if (null == mResources) {
                return;
            }
            int resId = THEME_INVALID_RESOURCE_ID;
            resId = mResources.getIdentifier(THEME_XMLFILE_NAME, THEME_RESOURCE_XML_TYPE,
                    mCurrentThemeName);

            if (resId == THEME_INVALID_RESOURCE_ID) {
                return;
            }

            XmlResourceParser xpp = mResources.getXml(resId);
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String sTag = xpp.getName();

                    if (sTag.equals(CN_WIDGET_INFO)) {
                        mWidgetInfo.put(xpp.getAttributeValue(0), xpp.getAttributeValue(1));
                    }
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Drawable getDrawable(String widgetName) {
        Drawable drawable = null;
        if (widgetName != null) {
            String widgetImage = mWidgetInfo.get(widgetName);
            if (widgetImage == null) {
                return null;
            }

            try {
                int resId = mResources.getIdentifier(widgetImage, THEME_RESOURCE_IMAGE_TYPE,
                        mCurrentThemeName);

                if (resId != THEME_INVALID_RESOURCE_ID) {
                    drawable = mResources.getDrawable(resId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return drawable;
    }

}