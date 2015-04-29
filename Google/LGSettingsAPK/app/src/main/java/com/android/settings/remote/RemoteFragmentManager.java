package com.android.settings.remote;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.Fragment;
import android.app.LGFragment;
import android.app.LGListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.LGPreferenceFragment;
import android.preference.Preference;
import android.preference.PreferenceActivity.Header;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.android.settings.R;
import com.android.settings.Settings;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

public class RemoteFragmentManager {
    private static final String TAG = RemoteFragmentManager.class
            .getSimpleName();
    private Activity mActivity;

    public static HashMap<String, RemoteContextInfo> sContextMap = new HashMap<String, RemoteContextInfo>();

    private ArrayList<ClassLoader> mClassLoaders = new ArrayList<ClassLoader>();

    public static final String ARG_PACKAGE_NAME = "arg_package_name";
    public static final String ARG_CLASS_NAME = "arg_class_name";

    public static final String RESOURCE_NAME_HEADER_TEXT = "settings_header_text";
    public static final String RESOURCE_NAME_HEADER_ICON = "settings_header_icon";
    public static final String RESOURCE_NAME_HEADER_ORDER = "settings_header_order";
    public static final String RESOURCE_DEFTYPE_STRING = "string";
    public static final String RESOURCE_DEFTYPE_DRAWABLE = "drawable";
    public static final String RESOURCE_DEFTYPE_INTEGER = "integer";

    public static final String EXTRA_HEADER_DATA_PACKAGE_NAME = "extra_header_data_pkg_name";
    public static final String EXTRA_HEADER_DATA_ORDER = "extra_header_data_order";
    public static final String EXTRA_HEADER_DATA_REPLACE_FLAG = "extra_header_data_replace_flag";

    public static final String META_DATA_REMOTE_FRAGMENT = "com.lge.settings.fragment";
    public static final String META_DATA_REMOTE_SUB_FRAGMENT = "com.lge.settings.subfragment";

    public final class RemoteContextInfo {
        public Context mContext;
        public String mEntryFragmentClassName;
        private ArrayList<String> mSubFragmentClassName;

        public RemoteContextInfo(Context context, String fragment) {
            mContext = context;
            mEntryFragmentClassName = fragment;
        }
        
        public void addSubFragment(String subFragmentClassName) {
            if (mSubFragmentClassName == null) {
                mSubFragmentClassName = new ArrayList<String>();
            }
            mSubFragmentClassName.add(subFragmentClassName);
        }
    }

    public RemoteFragmentManager(Activity activity) {
        mActivity = activity;
    }

    public Fragment instantiateFragment(String packageName) {
        String fragmentClassName = null;
        RemoteContextInfo contextInfo = sContextMap.get(packageName);
        if (contextInfo != null) {
            Log.d(TAG, "Hit the cache, Fragment class name:"
                    + contextInfo.mEntryFragmentClassName);
            fragmentClassName = contextInfo.mEntryFragmentClassName;
        } else {
            Log.d(TAG, "The remote context of " + packageName
                    + " should be loaded first.");
            return null;
        }

        return instantiateFragment(packageName, fragmentClassName, null);
    }

    public Fragment instantiateFragment(String packageName,
            String fragmentClassName, Bundle bundle) {
        Class<? extends Fragment> clazz = null;
        Fragment fragment = null;

        try {
            Context remote_context = null;

            RemoteContextInfo contextInfo = sContextMap.get(packageName);
            if (contextInfo != null) {
                Log.d(TAG, "Hit the cache, Fragment class name:"
                        + contextInfo.mEntryFragmentClassName);
                remote_context = contextInfo.mContext;
            } else {
                Log.d(TAG, "The remote context of " + packageName
                        + " should be loaded first.");
                return null;
            }

            clazz = remote_context.getClassLoader()
                    .loadClass(fragmentClassName).asSubclass(Fragment.class);

            fragment = clazz.newInstance();

            setFragment(fragment, packageName, bundle);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return fragment;
    }

    public void setFragment(Fragment fragment, String packageName, Bundle bundle) {
        if (fragment != null) {
            if (packageName == null) {
                fragment.setArguments(bundle);
                return;
            }

            if (fragment instanceof LGFragment) {
                ((LGFragment)fragment).setPackageContext(mActivity,
                        packageName, bundle);
            } else if (fragment instanceof LGPreferenceFragment) {
                ((LGPreferenceFragment)fragment).setPackageContext(mActivity,
                        packageName, bundle);
            } else if (fragment instanceof LGListFragment) {
                ((LGListFragment)fragment).setPackageContext(mActivity,
                        packageName, bundle);
            } else {
                fragment.setArguments(bundle);
            }
        }
    }

    public static Context getRemoteContext(String packageName) {
        RemoteContextInfo contextInfo = sContextMap.get(packageName);
        return contextInfo != null ? contextInfo.mContext : null;
    }

    public Header makeHeader() {
        Header header = new Header();
        return header;
    }

    public void buildHeaders(List<Header> headers, Context context) {
        if (sContextMap.size() == 0) {
            /*
             * for (Header header : headers) { if (header.id ==
             * R.id.apps_section) { headers.remove(header); break; } }
             */
            return;
        }

        boolean addCategory = true;
        Set<String> keySet = sContextMap.keySet();
        Object[] keyArray = keySet.toArray();

        for (int i = 0; i < keyArray.length; i++) {
            String key = (String)keyArray[i];
            Log.d(TAG, "buildHeaders():packageName_" + i + " = " + key);
            RemoteContextInfo contextInfo = sContextMap.get(key);
            if (contextInfo == null) {
                continue;
            }

            Context cached_context = contextInfo.mContext;

            boolean replace_header = false;
            for (Header header : headers) {

                if (Utils.supportRemoteFragment(mActivity)
                        && header.id == R.id.home_settings) {
                    if (!"VZW".equals(Config.getOperator()) && !"ATT".equals(Config.getOperator())) {
                        continue;
                    }
                }

                if (header.intent != null
                        && header.intent.getComponent() != null) {
                    String targetPackage = header.intent.getComponent()
                            .getPackageName();
                    if (cached_context.getPackageName().equals(targetPackage)) {
                        if (SystemProperties.getBoolean("settings.remote.test",
                                false) == false) {
                            replace_header = true;
                        }
                        header.fragment = contextInfo.mEntryFragmentClassName != null ? ("remote:" + contextInfo.mEntryFragmentClassName)
                                : "remote:dummy";
                        if (header.extras == null) {
                            header.extras = new Bundle();
                        }
                        header.extras.putString(EXTRA_HEADER_DATA_PACKAGE_NAME,
                                cached_context.getPackageName());
                        header.extras.putInt(EXTRA_HEADER_DATA_ORDER, -1);
                        header.extras.putBoolean(
                                EXTRA_HEADER_DATA_REPLACE_FLAG, true);
                    }
                }
            }

            if (replace_header == false
                    && (SystemProperties.getBoolean("settings.remote.test",
                            false) == true)) {
                Header header = buildHeader(cached_context,
                        contextInfo.mEntryFragmentClassName);
                if (addCategory == true) {
                    addCategory = false;
                    Header category = new Header();
                    category.titleRes = R.string.applications_settings;
                    if ("VZW".equals(Config.getOperator())) {
                        category.titleRes = R.string.applications_settings_title;
                    } else if ("LRA".equals(Config.getOperator())) {
                        category.titleRes = R.string.applications_settings;
                    }

                    addHeader(headers, category);
                }

                if (header != null) {
                    addHeader(headers, header);
                }
            }
        }
    }

    private void addHeader(List<Header> headers, Header header) {
        headers.add(header);
    }

    private Header buildHeader(Context cached_context, String fragmentName) {
        if (cached_context == null) {
            Log.d(TAG, "Can not build header. cached_context is null.");
            return null;
        }

        int header_title = getResourceId(cached_context,
                RESOURCE_NAME_HEADER_TEXT, RESOURCE_DEFTYPE_STRING);

        int header_icon = getResourceId(cached_context,
                RESOURCE_NAME_HEADER_ICON, RESOURCE_DEFTYPE_DRAWABLE);

        int header_order = getResourceId(cached_context,
                RESOURCE_NAME_HEADER_ORDER, RESOURCE_DEFTYPE_INTEGER);
        if (header_order > 0) {
            header_order = cached_context.getResources().getInteger(
                    header_order);
        }

        Header header = new Header();
        header.titleRes = header_title;
        if (header_title == 0) {
            header.title = fragmentName;
            if (fragmentName.lastIndexOf(".") > 0) {
                header.title = fragmentName.substring(fragmentName
                        .lastIndexOf(".") + 1);
            }
        }
        header.iconRes = header_icon;
        header.fragment = fragmentName != null ? ("remote:" + fragmentName)
                : "remote:dummy";
        header.extras = new Bundle();
        header.extras.putString(EXTRA_HEADER_DATA_PACKAGE_NAME,
                cached_context.getPackageName());
        header.extras.putInt(EXTRA_HEADER_DATA_ORDER, header_order);
        // header.id=0x80000000;
        return header;
    }

    private int getResourceId(Context cached_context,
            String resourceNameHeaderText, String resourceDeftypeString) {
        if (cached_context == null) {
            return 0;
        }
        return cached_context.getResources().getIdentifier(
                resourceNameHeaderText, resourceDeftypeString,
                cached_context.getPackageName());
    }

    public ArrayList<ClassLoader> getClassLoaders() {
        return mClassLoaders;
    }

    public void checkInstalledPackagesForRemoteLoading(final Context context,
            ClassLoader settingsClassLoader) {

        PackageManager packageManager = context.getPackageManager();
        long time = System.currentTimeMillis();
        List<ApplicationInfo> infos = packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA);
        time = System.currentTimeMillis() - time;
        Log.d(TAG,
                "getInstalledApplications time=" + time + ", count="
                        + infos.size());

        time = System.currentTimeMillis();

        sContextMap.clear();
        mClassLoaders.clear();
        mClassLoaders.add(settingsClassLoader);
        for (ApplicationInfo appInfo : infos) {
            if (appInfo.metaData == null) {
                continue;
            }
            
            Context remote_context = null;
            int sub_fragment_index = 1;
            RemoteContextInfo contextInfo = null;
            
            if (appInfo.metaData.containsKey(META_DATA_REMOTE_FRAGMENT)) {
                Log.d(TAG, "remote package : " + appInfo.packageName);
                String fragmentClassName = appInfo.metaData
                        .getString(META_DATA_REMOTE_FRAGMENT);
                
                remote_context = loadRemoteContext(context, appInfo.packageName);
                
                if (remote_context != null) {
                    contextInfo = new RemoteContextInfo(remote_context, fragmentClassName);
                }
            } 
            
            if (appInfo.metaData.containsKey(META_DATA_REMOTE_SUB_FRAGMENT+sub_fragment_index)) {
                do {
                    String subFragmentClassName = appInfo.metaData.getString(META_DATA_REMOTE_SUB_FRAGMENT+sub_fragment_index);
                    Log.d(TAG, "remote package : " + appInfo.packageName + ", subFragment : " + subFragmentClassName);
                    if (contextInfo != null && subFragmentClassName != null) {
                        contextInfo.addSubFragment(subFragmentClassName);
                    }
                    ++sub_fragment_index;
                } while (appInfo.metaData.containsKey(META_DATA_REMOTE_SUB_FRAGMENT+sub_fragment_index));
            }
            
            if (contextInfo != null) {
                sContextMap.put(appInfo.packageName, contextInfo);
            }
            
            if (remote_context != null) {
                mClassLoaders.add(remote_context.getClassLoader());                
            }
        }

        time = System.currentTimeMillis() - time;
        Log.d(TAG, "context map build completed time=" + time);
    }

    private Context loadRemoteContext(final Context context, String packageName) {
        Context remote_context = null;
        try {
            int flags = (Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            remote_context = context.createPackageContext(packageName, flags);

            remote_context = new ContextThemeWrapper(remote_context,
                    context.getThemeResId()) {
                @Override
                public Object getSystemService(String name) {
                    if (name.equals(Context.WINDOW_SERVICE)) {
                        return context.getSystemService(name);
                    }
                    return super.getSystemService(name);
                }

                @Override
                public void startActivity(Intent intent) {
                    context.startActivity(intent);
                }
            };
            remote_context.setTheme(com.lge.R.style.Theme_LGE_White);

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return remote_context;
    }

    public HashMap<String, RemoteContextInfo> getContextMap() {
        return sContextMap;
    }

    public Context findContext(String fragmentClass) {

        Set<String> keySet = sContextMap.keySet();

        Object[] keyArray = keySet.toArray();

        for (int i = 0; i < keyArray.length; i++) {
            String key = (String)keyArray[i];
            RemoteContextInfo contextInfo = sContextMap.get(key);
            Class<?> clazz = null;
            if (contextInfo == null) {
                continue;
            }

            Context cached_context = contextInfo.mContext;
            try {
                clazz = cached_context.getClassLoader()
                        .loadClass(fragmentClass);
            } catch (ClassNotFoundException e) {
                //e.printStackTrace();
                continue;
            }
            if (clazz != null) {
                return cached_context;
            }
        }
        return null;
    }

    public void clear() {
        // sContextMap.clear();
        mClassLoaders.clear();
    }

    public static String getFragmentName(String packageName) {
        if (sContextMap == null) {
            return null;
        }

        RemoteContextInfo contextInfo = sContextMap.get(packageName);
        if (contextInfo != null) {
            Log.d(TAG, "getFragmentName():Hit the cache, Fragment class name:"
                    + contextInfo.mEntryFragmentClassName);
            return contextInfo.mEntryFragmentClassName;
        }
        return null;
    }
    
    public static ArrayList<String> getSubFragmentName(String packageName) {
        if (sContextMap == null) {
            return null;
        }

        RemoteContextInfo contextInfo = sContextMap.get(packageName);
        if (contextInfo != null) {
            Log.d(TAG, "getFragmentName():Hit the cache, Fragment class name:"
                    + contextInfo.mEntryFragmentClassName);
            return contextInfo.mSubFragmentClassName;
        }
        return null;
    }
    

    public static String getPackageName(String fragmentName) {
        if (sContextMap == null) {
            return null;
        }

        Set<String> keySet = sContextMap.keySet();

        Object[] keyArray = keySet.toArray();

        for (int i = 0; i < keyArray.length; i++) {
            String key = (String)keyArray[i];
            RemoteContextInfo contextInfo = sContextMap.get(key);
            if (fragmentName.equals(contextInfo.mEntryFragmentClassName)) {
                return key;
            } else if (contextInfo.mSubFragmentClassName != null) {
                for (String subFragment : contextInfo.mSubFragmentClassName) {
                    if (fragmentName.equals(subFragment)) {
                        return key;
                    }
                }
            }
        }
        return null;
    }

    public static boolean setPreferenceForRemoteFragment(String packageName,
            Preference preference) {
        String fName = getFragmentName(packageName);
        if (fName != null && preference != null && fName.length() > 0) {
            preference.setFragment(fName);
            if (preference.getTitleRes() != 0) {
                int resId = preference.getTitleRes();
                preference.setTitle("wow");
                preference.setTitle(Settings.mMyOwnResources.getString(resId));
            }
            return true;
        }
        return false;
    }

    public static boolean setPreferenceForRemoteFragment2(String fragmentClassName,
            Preference preference) {
        if (fragmentClassName != null && preference != null && fragmentClassName.length() > 0) {
            preference.setFragment(fragmentClassName);
            if (preference.getTitleRes() != 0) {
                int resId = preference.getTitleRes();
                preference.setTitle("wow");
                preference.setTitle(Settings.mMyOwnResources.getString(resId));
            }
            return true;
        }
        return false;
    }    
    
    private String getHeaderTitle(Header header) {
        if (header.title != null && header.title.length() > 0) {
            return (String)header.title;
        }

        if (header.titleRes > 0) {
            return Settings.mMyOwnResources.getString(header.titleRes);
        }
        return null;
    }

    public static class RemoteActionInfo {
        public int headerId;
        public String fragmentClassName;
        public String breadcrumbTitle;

        public RemoteActionInfo(int id, String fragment, String breadcrumbTitle) {
            this.headerId = id;
            this.fragmentClassName = fragment;
            this.breadcrumbTitle = breadcrumbTitle;
        }
    }

    public static RemoteActionInfo checkRemoteAction(String action) {
        if ("com.lge.settings.remote_test_action".equals(action)) {
            return new RemoteActionInfo(R.id.storage_settings,
                    "com.example.remotefragmenttest.MyPreferenceFragment",
                    "RemoteFragment Test");
        } else if ("wiflus.intent.action.SETTING_TABLET".equals(action)) {

            int header_id = R.id.wireless_settings;
            if ("US".equals(Config.getCountry())) {
                header_id = R.id.share_connect;
            }
            return new RemoteActionInfo(header_id,
                    getFragmentName("itectokyo.wiflus.service"),
                    Utils.getResources().getString(
                            R.string.sp_smart_share_title_NORMAL_jb_plus));
        } else if ("com.lge.springcleaning.main_tablet".equals(action)) {
            return new RemoteActionInfo(R.id.springcleaning,
                    getFragmentName("com.lge.springcleaning"), Utils
                            .getResources().getString(R.string.smart_cleaning));
        } else if ("com.lge.launcher2.intent.action.homescreensettings"
                .equals(action)) {
            int header_id = R.id.display_settings;
            if ("VZW".equals(Config.getOperator()) || "ATT".equals(Config.getOperator())) {
                header_id = R.id.home_settings;
            }
            return new RemoteActionInfo(header_id,
                    getFragmentName("com.lge.launcher2"),
                    Utils.getResources().getString(
                            R.string.sp_screen_home_screen_title_NORMAL));
        } else if ("com.lge.smartsharebeam.setting.tablet".equals(action)) {
            return new RemoteActionInfo(R.id.share_connect,
                    getFragmentName("com.lge.smartsharepush"),
                    Utils.getResources().getString(
                            R.string.sp_smart_share_title_NORMAL_jb_plus));
        } else if ("com.lge.smartshare.dms.ACTION_MEDIASERVER_SHOW".equals(action)) {
            return new RemoteActionInfo(R.id.share_connect,
                    getFragmentName("com.lge.smartshare"),
                    Utils.getResources().getString(
                            R.string.settings_shareconnect_mediaserver_title));
        } else if ("com.lge.accessibility.fromshorcut_tablet".equals(action)) {
            return new RemoteActionInfo(R.id.accessibility_settings_lge,
                    getFragmentName("com.android.settingsaccessibility"),
                    Utils.getResources().getString(
                            R.string.accessibility_settings));
        } else if ("com.lge.springcleaning.settings_tablet".equals(action)) {
            return new RemoteActionInfo(R.id.springcleaning,
                    "com.lge.springcleaning.ui.settings.SpringCleaningSettingsFragment", Utils
                            .getResources().getString(R.string.settings_label));
        } else if ("android.settings.BLUETOOTH_SETTINGS_SPLITVIEW".equals(action)) {
            return new RemoteActionInfo(R.id.bluetooth_settings,
                    getFragmentName("com.lge.bluetoothsetting"),
                    Utils.getResources().getString(
                            R.string.bluetooth_settings_title));
        } else if ("android.settings.WIFI_SETTINGS_SPLITVIEW".equals(action)) {
            return new RemoteActionInfo(R.id.wifi_settings,
                    getFragmentName("com.lge.wifisettings"),
                    Utils.getResources().getString(
                            R.string.wifi_settings_title));
        } else if ("android.settings.NFC_SETTINGS_SPLITVIEW".equals(action)) {
            return new RemoteActionInfo(R.id.share_connect,
                    getFragmentName("com.lge.NfcSettings"),
                    Utils.getResources().getString(
                            R.string.nfc_quick_toggle_title));
        } else if ("android.settings.LOCK_SETTINGS_SPLITVIEW".equals(action)) {
            int header_id = R.id.home_settings;
            if ("VZW".equals(Config.getOperator()) || "ATT".equals(Config.getOperator())) {
                header_id = R.id.lock_settings;
            }
            return new RemoteActionInfo(header_id,
                    getFragmentName("com.lge.lockscreensettings"),
                    Utils.getResources().getString(
                            R.string.sp_screen_lock_screen_title_NORMAL));        
        }
        return null;
    }

    public void buildRemoteHeader(Header header, String fragmentClass) {
        if (header.extras == null) {
            header.extras = new Bundle();
        }
        header.extras.putString(EXTRA_HEADER_DATA_PACKAGE_NAME,
                getPackageName(fragmentClass));
        header.extras.putInt(EXTRA_HEADER_DATA_ORDER, -1);
        header.extras.putBoolean(EXTRA_HEADER_DATA_REPLACE_FLAG, true);
    }
}