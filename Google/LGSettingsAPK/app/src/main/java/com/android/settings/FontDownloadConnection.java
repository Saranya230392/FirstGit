/**
 * 
 */
package com.android.settings;

import java.io.File;
import java.util.List;

import com.android.settings.lgesetting.Config.Config;

import android.app.AlertDialog;
import android.content.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.GetChars;
import android.text.TextUtils;
import android.os.SystemProperties;

/**
 * @author dongseok.lee
 *
 */
public class FontDownloadConnection {
    private static final String SYSPROP_REMOVE_SMARTWRD = "persist.sys.cust.rmsmartwrd";

    /****************************************************************************
    ** Add fonts.
    ****************************************************************************/
    private static boolean isDomesticProduct() {
        String country = Config.getCountry();
        if (!TextUtils.isEmpty(country)) {
            if (country.equalsIgnoreCase("kr")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isjapaneseProduct() {
        String country = Config.getCountry();
        if (!TextUtils.isEmpty(country)) {
            if (country.equalsIgnoreCase("jp")) {
                return true;
            }
        }
        return false;
    }

    private static void dlgLGSmartWorld(final Context context) {
        if (context == null) {
            return;
        }
        
        // Question : go Download lgsmartworld app?
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        ab.setTitle(context.getResources().getString(R.string.sp_add_fonts_NORMAL));
        ab.setMessage(
                context.getResources().getString(R.string.sp_install_LGSmartWorld_app_msg_NORMAL));
        ab.setCancelable(true);
        ab.setNegativeButton(context.getResources().getString(R.string.sp_btn_font_cancel_SHORT),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        ab.setPositiveButton(context.getResources().getString(R.string.sp_go_LGSmartWorld_NORMAL),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startAppManager(context);
            }
        });
        ab.show();
    }
    
    private static void dlgUpdateCenter(final Context context) {
        if (context == null) {
            return;
        }
        
        // Question : go Download lgsmartworld app?
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        ab.setTitle(context.getResources().getString(R.string.sp_add_fonts_NORMAL));
        ab.setMessage(
                context.getResources().getString(R.string.sp_install_LGSmartWorld_app_msg_NORMAL));
        ab.setCancelable(true);
        ab.setNegativeButton(context.getResources().getString(R.string.sp_btn_font_cancel_SHORT),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        ab.setPositiveButton(context.getResources().getString(R.string.sp_go_LGSmartWorld_NORMAL),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startUpdateCenter(context);
            }
        });
        ab.show();
    }

    protected static void startUpdateCenter(Context context) {
        if (context != null) {
            Intent intent = new Intent("com.lge.appbox.commonservice.update");
            ComponentName component = new ComponentName("com.lge.appbox.client", "com.lge.appbox.service.AppBoxCommonService");
            intent.setComponent(component);
            intent.putExtra("packagename", "com.lge.lgworld");
            intent.putExtra("type", "download");
            context.startService(intent);
        }
    }

    public static boolean isStartUpPreloadModel() {
        boolean startUpPreloadModel = false;
        final String AppManager_path = "/system/apps/bootup";

        File file = new File(AppManager_path);
        if (file.isDirectory()) {
            String[] list = file.list();

            if (list == null) {
                return startUpPreloadModel;
            }

            if (forceRemoveSmartWorld()) {
                return false;
            }

            for (int i = 0; i < list.length; i++) {
                if (list[i].contains("LGSmartWorld")) {
                    startUpPreloadModel = true;
                }
            }
        }

        return startUpPreloadModel;
    }

    public static boolean forceRemoveSmartWorld() {
        if (SystemProperties.getBoolean(SYSPROP_REMOVE_SMARTWRD, false)) {
            return true;
        }
        return false;
    }

    public static boolean isInstalledLGSmartWorld(Context context) {
        if (context == null) {
            return false;
        }
        PackageManager pm = context.getPackageManager();
        List<android.content.pm.ApplicationInfo> mInstalledPkgList =
                pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

        boolean isExistLGSmartWorld = false;

        if (forceRemoveSmartWorld()) {
            return false;
        }

        for (android.content.pm.ApplicationInfo a : mInstalledPkgList) {
            if (a.packageName.equals("com.lg.apps.cubeapp")) {
                isExistLGSmartWorld = true;
            } else if (a.packageName.equals("com.lge.apps.jp.phone")) {
                isExistLGSmartWorld = true;
            } else if (a.packageName.equals("com.lge.lgworld")) {
                isExistLGSmartWorld = true;
            }

        }
        
        // if user uninstall SmartWorld in Domestic Operator, can be download with UpdateCenter.
        // http://mlm.lge.com/di/browse/LGEIME-1861
        // http://mlm.lge.com/di/browse/LGEIME-1876
        if (isExistLGSmartWorld == false) {
            if (isDownloadSmartWorld(context) == true) {
                isExistLGSmartWorld = true;
            }
        }

        return isExistLGSmartWorld;
    }

    public static void connectLGSmartWorld(Context context) {
        
        if (context == null) {
            return;
        }

        boolean notInstalledLGSmartWorld_GL = false;
        boolean notInstalledLGSmartWorld_KR = false;
        boolean notInstalledLGSmartWorld_JP = false;

        final String LGSmartWorld_GL_packagename = "com.lge.lgworld";
        final String LGSmartWorld_KR_packagename = "com.lg.apps.cubeapp";
        final String LGSmartWorld_JP_packagename = "com.lge.apps.jp.phone";

        PackageManager pm = context.getPackageManager();

        try {
            pm.getPackageInfo(LGSmartWorld_GL_packagename, 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            notInstalledLGSmartWorld_GL = true;
        }

        try {
            pm.getPackageInfo(LGSmartWorld_KR_packagename, 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            notInstalledLGSmartWorld_KR = true;
        }

        try {
            pm.getPackageInfo(LGSmartWorld_JP_packagename, 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            notInstalledLGSmartWorld_JP = true;
        }

        if (!notInstalledLGSmartWorld_GL) {

            Intent dlIntent = new Intent("com.lge.lgworld.intent.action.VIEW");
            dlIntent.setClassName("com.lge.lgworld", "com.lge.lgworld.LGReceiver");
            dlIntent.putExtra("lgworld.receiver", "LGSW_INVOKE_CATEGORY");
            dlIntent.putExtra("CATEGORY_ID", "118");
            dlIntent.putExtra("CATEGORY_NAME", "Fonts");
            context.sendBroadcast(dlIntent);
        } else if (!notInstalledLGSmartWorld_KR && isDomesticProduct() == true) {

            Intent dlIntent = new Intent();
            dlIntent.setClassName("com.lg.apps.cubeapp",
                    "com.lgworld.mobile.phone.ux.LGWorldIntroActivity");
            dlIntent.putExtra("catCode", "000019");
            dlIntent.putExtra("catName", 
                    context.getResources().getString(R.string.font_settings_intent_cat_name));
            dlIntent.putExtra("isCategory", true);
            dlIntent.putExtra("catListTpCode", "00");
            context.startActivity(dlIntent);
        } else if (!notInstalledLGSmartWorld_JP && isjapaneseProduct() == true) {

            Intent dlIntent = new Intent();
            dlIntent =
                    context.getPackageManager().getLaunchIntentForPackage("com.lge.apps.jp.phone");
            dlIntent.putExtra("CategoryID", "FONT");
            context.startActivity(dlIntent);
        } else if (isStartUpPreloadModel()) {
            dlgLGSmartWorld(context);
        } else {
            if (isDownloadSmartWorld(context) == true) {
                dlgUpdateCenter(context);
            }
        }
    }

    private static boolean isDownloadSmartWorld(Context context) {
        return isDPPreloadCountry(context);
    }

    private static boolean isDPPreloadCountry(Context context) {
        String country = Config.getCountry();
        if (TextUtils.isEmpty(country) == false && country.equalsIgnoreCase("kr")) {
            return true; 
        }
        return false;
    }

    public static void startAppManager(Context context) {
        if (context == null) {
            return;
        }
        try {
            Intent intent = null;
            PackageManager pm = context.getPackageManager();

            intent = pm.getLaunchIntentForPackage("com.lge.appbox.client");

            if (intent != null) {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
