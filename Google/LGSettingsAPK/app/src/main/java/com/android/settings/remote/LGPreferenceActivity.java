package com.android.settings.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.PreferenceActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import com.android.internal.util.XmlUtils;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.accounts.ManageAccountsSettings;
import com.android.settings.lge.HideNavigationAppsReceiver;

public class LGPreferenceActivity extends PreferenceActivity {

    private static final String BACK_STACK_PREFS = ":android:prefs";
    private static final String PROPERTY_NAME_TESTMODE = "settings.remote.test";

    public Resources mMyOwnResources;
    public RemoteFragmentManager mRemoteManager;
    public MultiDexClassLoader mMultiDexClassLoader;

    private BroadcastReceiver mPackageReceiver;
    private FragmentManager mFragmentManager;
    private BackStackChangedListener mBackStackChangedListener;
    private Header mSelectedHeader;
    private boolean mPerformOnCreate = false;
    private Header mSelectedHeaderBackup;
    private MenuInflater mMenuInflater;

    private static HashMap<String, RemoteFragmentInfo> mRemoteFragmentInfoMap;

    private class BackStackChangedListener implements
            FragmentManager.OnBackStackChangedListener {

        @Override
        public void onBackStackChanged() {
            int numEntries = mFragmentManager.getBackStackEntryCount();
            Log.d("kimyow", "onBackStackChanged(), numEntries=" + numEntries);
            for (int i = 0; i < numEntries; i++) {
                BackStackEntry entry = mFragmentManager.getBackStackEntryAt(i);
                Log.d("kimyow", "onBackStackChanged(), entry " + i + " = "
                        + entry);
            }
            if (mSelectedHeaderBackup != null) {
                mSelectedHeader = mSelectedHeaderBackup;
                mSelectedHeaderBackup = null;
            }
        }
    }

    public void setPerformOnCreate(boolean flag) {
        mPerformOnCreate = flag;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        mMyOwnResources = super.getResources();

        if (Utils.supportRemoteFragment(this) == true) {
            if (mRemoteManager == null) {
                mRemoteManager = new RemoteFragmentManager(this);
                mRemoteManager.checkInstalledPackagesForRemoteLoading(this,
                        super.getClassLoader());
            }
            mMultiDexClassLoader = new MultiDexClassLoader(
                    mRemoteManager.getClassLoaders());
            mPackageReceiver = new HideNavigationAppsReceiver();
            IntentFilter packageFilter = new IntentFilter();
            packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
            packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            packageFilter.addDataScheme("package");
            registerReceiver(mPackageReceiver, packageFilter);
            mFragmentManager = getFragmentManager();
            if (mBackStackChangedListener == null) {
                mBackStackChangedListener = new BackStackChangedListener();
            }
        }
        super.onCreate(bundle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPackageReceiver != null) {
            unregisterReceiver(mPackageReceiver);
        }
        if (mRemoteManager != null) {
            mRemoteManager.clear();
        }
        if (mMultiDexClassLoader != null) {
            mMultiDexClassLoader.clear();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Utils.supportRemoteFragment(this) == true
                && mBackStackChangedListener != null) {
            mFragmentManager
                    .removeOnBackStackChangedListener(mBackStackChangedListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.supportRemoteFragment(this) == true
                && mBackStackChangedListener != null) {
            mFragmentManager
                    .addOnBackStackChangedListener(mBackStackChangedListener);
        }
    }

    @Override
    public boolean onIsMultiPane() {
        if (SystemProperties.getBoolean(PROPERTY_NAME_TESTMODE, false) == true) {
            return true;
        }
        return super.onIsMultiPane();
    }

    private void switchToHeaderRemote(Header header, String remotePackageName) {
        /* Pop BackStackRecord for removing history */
        getFragmentManager().popBackStack(BACK_STACK_PREFS,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        /* set Tag to Fragment for identify this is remote fragment */
        Fragment fragment = mRemoteManager
                .instantiateFragment(remotePackageName);
        if (fragment != null) {
            transaction.replace(com.android.internal.R.id.prefs, fragment,
                    remotePackageName);
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void startPreferencePanel(String fragmentClass, Bundle args,
            int titleRes, CharSequence titleText, Fragment resultTo,
            int resultRequestCode) {

        if (Utils.supportRemoteFragment(this) == true) {
            startPreferencePanelForRemote(fragmentClass, args, titleRes,
                    titleText, resultTo, resultRequestCode);
            return;
        }

        super.startPreferencePanel(fragmentClass, args, titleRes, titleText,
                resultTo, resultRequestCode);
    }

    private void startPreferencePanelForRemote(String fragmentClass,
            Bundle args, int titleRes, CharSequence titleText,
            Fragment resultTo, int resultRequestCode) {

        Fragment f = null;
        Context remote_context = null;
        String packageName = null;

        f = Fragment.instantiate(this, fragmentClass, args);

        remote_context = mRemoteManager.findContext(fragmentClass);
        if (remote_context != null) {
            packageName = remote_context.getPackageName();
        }

        mRemoteManager.setFragment(f, packageName, args);

        if (f == null) {
            Log.d("kimyow", "fragment:" + fragmentClass + " instantiate failed");
            return;
        }

        startFragmentInternal(f, titleRes, titleText, resultTo,
                resultRequestCode);
    }

    private void startFragmentInternal(Fragment f, int titleRes,
            CharSequence titleText, Fragment resultTo, int resultRequestCode) {
        if (resultTo != null) {
            f.setTargetFragment(resultTo, resultRequestCode);
        }
        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.replace(com.android.internal.R.id.prefs, f);
        if (titleRes != 0) {
            //transaction.setBreadCrumbTitle(titleRes);
            if (checkIfUseOwnResources(f) == true) {
                transaction.setBreadCrumbTitle(Utils.getResources().getString(titleRes));
            } else {
                transaction.setBreadCrumbTitle(getResources().getString(titleRes));                
            }
        } else if (titleText != null) {
            transaction.setBreadCrumbTitle(titleText);
        }
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(BACK_STACK_PREFS);
        transaction.commitAllowingStateLoss();
    }

    private boolean checkIfUseOwnResources(Fragment f) {
        String fName = f.getClass().getName();
        if ("com.lge.springcleaning.ui.SpringCleaningSubFragment".equals(fName) ||
                "com.lge.springcleaning.ui.SpringCleaningSubFragment".equals(fName) ||
                "com.lge.springcleaning.ui.SpringCleaningSubFragment".equals(fName) ||
                "com.lge.springcleaning.ui.settings.SpringCleaningSettingsFragment".equals(fName) ||
                "com.android.settings.tts.TextToSpeechSettings".equals(fName) ||
                "com.android.settings.lge.EmotionalLEDEffectTab".equals(fName)) {
            return true;
        }
        return false;
    }

    private String getSelectedRemotePackageName() {
        Fragment f = getFragmentManager().findFragmentById(
                com.android.internal.R.id.prefs);
        if (f != null) {
            Bundle bundle = f.getArguments();
            if (bundle != null) {
                String packageName = bundle
                        .getString(RemoteFragmentManager.ARG_PACKAGE_NAME);
                if (getPackageName().equals(packageName) == false
                        && /* f.isResumed() */f.isAdded()) {
                    return packageName;
                }
            } else {
                // Do nothing (return null)
            }
        }
        return null;
    }

    private String checkRemoteCondition() {
        if (Utils.supportRemoteFragment(this) && mPerformOnCreate == false) {
            return getSelectedRemotePackageName();
        }
        return null;
    }

    public Resources getMyOwnResources() {
        return mMyOwnResources;
    }

    public Header getCurrentHeader() {
        return mSelectedHeader;
    }

    public void setCurrentHeader(Header header) {
        mSelectedHeader = header;
    }

    @Override
    public void switchToHeader(Header header) {
        if (Utils.supportRemoteFragment(this) == true) {
            if (mSelectedHeader == header) {
                return;
            }

            Log.d("kimyow",
                    "switchToHeader(), mFragmentManager.getBackStackEntryCount="
                            + mFragmentManager.getBackStackEntryCount());
            if (mFragmentManager.getBackStackEntryCount() > 0) {
                mSelectedHeaderBackup = header;
                mSelectedHeader = null;
            } else {
                mSelectedHeader = header;
            }

            if (header.extras != null
                    && header.extras
                            .containsKey(ManageAccountsSettings.KEY_ACCOUNT_TYPE)) {
                super.switchToHeader(header);
            }

            Log.d("kimyow", "switchToHeader, header=" + header);
            if (header.extras != null) {
                String remotePackageName = header.extras
                        .getString(RemoteFragmentManager.EXTRA_HEADER_DATA_PACKAGE_NAME);
                Log.d("kimyow", "switchToHeader, header's packageName ="
                        + remotePackageName);
                if (getPackageName().equals(remotePackageName) == false) {
                    switchToHeaderRemote(header, remotePackageName);
                    setSelectedHeader(header); // FW Method modifying to hidden
                                               // public api for accessing by
                                               // System application.
                    return;
                }
            }
        } else {
            mSelectedHeader = header;
        }

        super.switchToHeader(header);
    }

    @Override
    public ClassLoader getClassLoader() {
        if (Utils.supportRemoteFragment(this) == true
                && mMultiDexClassLoader != null) {
            return mMultiDexClassLoader;
        } else {
            return super.getClassLoader();
        }
    }

    @Override
    public Resources getResources() {
        String remotePackageName = checkRemoteCondition();
        if (remotePackageName != null) {
            Context remoteContext = mRemoteManager
                    .getRemoteContext(remotePackageName);
            if (remoteContext != null) {
                Resources res = remoteContext.getResources();
                Log.d("kimyow", "return remote resources from package: "
                        + remoteContext.getPackageName());
                return res;
            }
        }
        return super.getResources();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        String packageName = checkRemoteCondition();
        if (packageName != null && mRemoteManager != null) {
            Context remoteContext = mRemoteManager
                    .getRemoteContext(packageName);
            if (remoteContext != null) {
                Log.d("kimyow", "return remote sharedPreferences for "
                        + remoteContext.getPackageName());
                return remoteContext.getSharedPreferences(name, mode);
            }
        }
        return super.getSharedPreferences(name, mode);
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        String packageName = checkRemoteCondition();
        if (packageName != null) {
            Context remoteContext = mRemoteManager
                    .getRemoteContext(packageName);
            if (remoteContext != null) {
                Log.d("kimyow", "return remote LayoutInflater for "
                        + remoteContext.getPackageName());
            }
            return LayoutInflater.from(remoteContext);
        }
        Log.d("kimyow", "return local LayoutInflater");
        return super.getLayoutInflater();
    }

    @Override
    public void loadHeadersFromResource(int resid, List<Header> target) {
        if (Utils.supportRemoteFragment(this) == false) {
            super.loadHeadersFromResource(resid, target);
            return;
        }
        XmlResourceParser parser = null;
        try {
            parser = super.getResources().getXml(resid);
            AttributeSet attrs = Xml.asAttributeSet(parser);

            int type;
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && type != XmlPullParser.START_TAG) {
                // Parse next until start tag is found
            }

            String nodeName = parser.getName();
            if (!"preference-headers".equals(nodeName)) {
                throw new RuntimeException(
                        "XML document must start with <preference-headers> tag; found"
                                + nodeName + " at "
                                + parser.getPositionDescription());
            }

            Bundle curBundle = null;

            final int outerDepth = parser.getDepth();
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }

                nodeName = parser.getName();
                if ("header".equals(nodeName)) {
                    Header header = new Header();

                    TypedArray sa = super.getResources().obtainAttributes(
                            attrs,
                            com.android.internal.R.styleable.PreferenceHeader);
                    header.id = sa
                            .getResourceId(
                                    com.android.internal.R.styleable.PreferenceHeader_id,
                                    (int)HEADER_ID_UNDEFINED);
                    TypedValue tv = sa
                            .peekValue(com.android.internal.R.styleable.PreferenceHeader_title);
                    if (tv != null && tv.type == TypedValue.TYPE_STRING) {
                        if (tv.resourceId != 0) {
                            header.titleRes = tv.resourceId;
                        } else {
                            header.title = tv.string;
                        }
                    }
                    tv = sa.peekValue(com.android.internal.R.styleable.PreferenceHeader_summary);
                    if (tv != null && tv.type == TypedValue.TYPE_STRING) {
                        if (tv.resourceId != 0) {
                            header.summaryRes = tv.resourceId;
                        } else {
                            header.summary = tv.string;
                        }
                    }
                    tv = sa.peekValue(com.android.internal.R.styleable.PreferenceHeader_breadCrumbTitle);
                    if (tv != null && tv.type == TypedValue.TYPE_STRING) {
                        if (tv.resourceId != 0) {
                            header.breadCrumbTitleRes = tv.resourceId;
                        } else {
                            header.breadCrumbTitle = tv.string;
                        }
                    }
                    tv = sa.peekValue(com.android.internal.R.styleable.PreferenceHeader_breadCrumbShortTitle);
                    if (tv != null && tv.type == TypedValue.TYPE_STRING) {
                        if (tv.resourceId != 0) {
                            header.breadCrumbShortTitleRes = tv.resourceId;
                        } else {
                            header.breadCrumbShortTitle = tv.string;
                        }
                    }
                    header.iconRes = sa
                            .getResourceId(
                                    com.android.internal.R.styleable.PreferenceHeader_icon,
                                    0);
                    header.fragment = sa
                            .getString(com.android.internal.R.styleable.PreferenceHeader_fragment);
                    sa.recycle();

                    if (curBundle == null) {
                        curBundle = new Bundle();
                    }

                    final int innerDepth = parser.getDepth();
                    while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                            && (type != XmlPullParser.END_TAG || parser
                                    .getDepth() > innerDepth)) {
                        if (type == XmlPullParser.END_TAG
                                || type == XmlPullParser.TEXT) {
                            continue;
                        }

                        String innerNodeName = parser.getName();
                        if (innerNodeName.equals("extra")) {
                            super.getResources().parseBundleExtra("extra",
                                    attrs, curBundle);
                            XmlUtils.skipCurrentTag(parser);

                        } else if (innerNodeName.equals("intent")) {
                            header.intent = Intent.parseIntent(
                                    super.getResources(), parser, attrs);

                        } else {
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }

                    if (curBundle.size() > 0) {
                        header.fragmentArguments = curBundle;
                        curBundle = null;
                    }

                    target.add(header);
                } else {
                    XmlUtils.skipCurrentTag(parser);
                }
            }

        } catch (XmlPullParserException e) {
            throw new RuntimeException("Error parsing headers", e);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing headers", e);
        } finally {
            if (parser != null)
                parser.close();
        }
    }

    @Override
    public void showBreadCrumbs(Header header) {
        Resources resources = super.getResources();
        if (header != null) {
            if (Utils.supportRemoteFragment(this) && header.extras != null) {
                Log.d("kimyow",
                        "showBreadCrumb:using remote resource for displaying the header");
                String packageName = header.extras
                        .getString(RemoteFragmentManager.EXTRA_HEADER_DATA_PACKAGE_NAME);
                boolean replace_flag = header.extras
                        .getBoolean(RemoteFragmentManager.EXTRA_HEADER_DATA_REPLACE_FLAG);
                Context remote_context = mRemoteManager
                        .getRemoteContext(packageName);
                if (remote_context != null && replace_flag == false) {
                    resources = remote_context.getResources();
                }
            }

            CharSequence title = header.getBreadCrumbTitle(resources);
            if (title == null)
                title = header.getTitle(resources);
            if (title == null)
                title = getTitle();
            showBreadCrumbs(title, header.getBreadCrumbShortTitle(resources));
        } else {
            showBreadCrumbs(getTitle(), null);
        }
    }

    public static final class RemoteFragmentInfo {
        public String action;
        public String fragmentClass;
        public Bundle args;
        public int titleRes;
        public String titleText;
        public Fragment resultTo;
        public int resultRequestCode;

        public RemoteFragmentInfo(String fragmentClass, Bundle args,
                int titleRes, String titleText, Fragment resultTo,
                int resultRequestCode) {
            this.fragmentClass = fragmentClass;
            this.args = args;
            this.titleRes = titleRes;
            this.titleText = titleText;
            this.resultTo = resultTo;
            this.resultRequestCode = resultRequestCode;
        }
    }

    public void addRemoteFragmentInfo(String action, RemoteFragmentInfo info) {

        if (mRemoteFragmentInfoMap == null) {
            mRemoteFragmentInfoMap = new HashMap<String, RemoteFragmentInfo>();
        }

        if (action != null) {
            info.action = action;
            mRemoteFragmentInfoMap.put(action, info);
        }
    }

    public HashMap<String, RemoteFragmentInfo> getRemoteFragmentInfoMap() {
        return mRemoteFragmentInfoMap;
    }

    @Override
    public void startActivity(Intent intent) {
        Log.d("kimyow", "startActivity(): intent=" + intent);
        try {
            if (Utils.supportRemoteFragment(this) == true) {
                RemoteFragmentInfo fragmentInfo = mRemoteFragmentInfoMap
                        .get(intent.getAction());
                if (fragmentInfo != null) {

                    Bundle args = fragmentInfo.args;
                    if (intent != null && args != null) {
                        args.putString("key_action_name", intent.getAction());
                    }

                    startPreferencePanel(fragmentInfo.fragmentClass, args,
                            fragmentInfo.titleRes, fragmentInfo.titleText,
                            fragmentInfo.resultTo,
                            fragmentInfo.resultRequestCode);
                    return;
                }
            }
            super.startActivity(intent);
        } catch (ActivityNotFoundException ane) {
            Toast.makeText(this, "Activity is not exist this device",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Exception! : " + e.toString(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        SettingsMenu tempMenu = new SettingsMenu(this, menu);
        return super.onCreatePanelMenu(featureId, tempMenu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        SettingsMenu tempMenu = new SettingsMenu(this, menu);
        return super.onPreparePanel(featureId, view, tempMenu);
    }

    @Override
    public MenuInflater getMenuInflater() {
        if (Utils.supportRemoteFragment(this) == true) {
            String remotePackageName = checkRemoteCondition();
            if (remotePackageName != null) {
                if (mMenuInflater == null) {
                    mMenuInflater = new MenuInflater(this);
                }
                return mMenuInflater;
            }
        }
        return super.getMenuInflater();
    }
}