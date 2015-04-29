/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.vpn2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.LinkProperties;
import android.net.RouteInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.security.Credentials;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnProfile;
import com.android.internal.util.ArrayUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.google.android.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
// yung.im@lgepartner.com 20141121 [
import java.io.File;
import com.android.settings.Utils;
import android.os.SystemProperties;
// yung.im@lgepartner.com 20141121 ]
import android.app.ActionBar;
import android.preference.PreferenceScreen;

// add for SettingsSearch start
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
// add for SettingsSearch end

// add Indexable implementation for settings search start
public class VpnSettings extends SettingsPreferenceFragment implements
        Handler.Callback, Preference.OnPreferenceClickListener,
        DialogInterface.OnClickListener, DialogInterface.OnDismissListener, Indexable {
// add Indexable implementation for settings search end            
    private static final String TAG = "VpnSettings";

    private static final String TAG_LOCKDOWN = "lockdown";

    private static final String EXTRA_PICK_LOCKDOWN = "android.net.vpn.PICK_LOCKDOWN";

    // TODO: migrate to using DialogFragment when editing

    private final IConnectivityManager mService = IConnectivityManager.Stub
            .asInterface(ServiceManager.getService(Context.CONNECTIVITY_SERVICE));
    // add static for settings search start
    private static final KeyStore mKeyStore = KeyStore.getInstance();
    // add static for settings search end
    private boolean mUnlocking = false;

    private HashMap<String, VpnPreference> mPreferences = new HashMap<String, VpnPreference>();
    private VpnDialog mDialog;

    private Handler mUpdater;
    private LegacyVpnInfo mInfo;
    private UserManager mUm;

    // The key of the profile for the current ContextMenu.
    private String mSelectedKey;

    private boolean mUnavailable;

    private VpnDialog savedDialog;
    private PreferenceCategory mPs;

    public static boolean FEATURE_LG_VPN = false;

    private int mType = 0;
    private String InstalledKey;
    public static final boolean ISTABLET = SystemProperties.get("ro.build.characteristics").equals(
            "tablet");

    // add for settings search start
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    public static int mKeyStoreEnabled = 0;
    final private static String PREF_KEY_BASIC_VPN = "vpn_settings";
    final private static String PREF_KEY_LG_VPN = "lg_vpn";
    final private static String PREF_KEY_ADD_NETWORK = "add_network";
    final private static String VPNSETTINGS_ACTIVITY_ACTION = "android.net.vpn.SETTINGS";
    final private static String INTENT_ACTION = "android.intent.action.MAIN";
    final private static String LGVPN_MAIN_ACTIVITY = "com.ipsec.vpnclient.MainActivity";
    final private static String LGVPN_PACKAGE = "com.ipsec.vpnclient";
    String mSearch_result = "";
    boolean mIsFirstSearch = false;
    // private static BasicVPNSearchIndexableItemUpdater updater = null;


    // [encryption-vpn@lge.com] FRIENDLY VPN [START]
    BroadcastReceiver mInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            String action = intent.getAction();
            Log.d(TAG, "[onReceive] mInfoReceiver getAction = " + action);

            if (action.equals("com.lge.vpn.friendlyvpn")) {
                if (mDialog != null) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        mType = extras.getInt("Type");
                        InstalledKey = extras.getString("CertKey", null);
                    }
                    mDialog.initUserCert(mKeyStore.saw(Credentials.USER_CERTIFICATE), mType,
                            InstalledKey);
                    mDialog.initCACert(mKeyStore.saw(Credentials.CA_CERTIFICATE), mType,
                            InstalledKey);
                    mDialog.initServerCert(mKeyStore.saw(Credentials.USER_CERTIFICATE), mType,
                            InstalledKey);
                }
            } else {
                Log.d(TAG, "Unknown action...");
            }
        }
    };
    // [encryption-vpn@lge.com] FRIENDLY VPN [END]
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        // add for settings search start
        mIsFirstSearch = true;
        // add for settings search end

        mUm = (UserManager)getSystemService(Context.USER_SERVICE);

        if (mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_VPN)) {
            mUnavailable = true;
            setPreferenceScreen(new PreferenceScreen(getActivity(), null));
            return;
        }

        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.vpn_settings2);

        mPs = (PreferenceCategory)findPreference("vpn_list");

        FEATURE_LG_VPN = Utils.isSupportVPN(getActivity());
        if (FEATURE_LG_VPN) {
            PreferenceScreen addNetwork = (PreferenceScreen)findPreference("add_network");
            addNetwork.setTitle(getActivity().getResources().getString(
                    R.string.sp_basic_vpn_create_NORMAL));
        }

        // [encryption-vpn@lge.com] FRIENDLY VPN [START]
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.lge.vpn.vpninfo");
        filter.addAction("com.lge.vpn.friendlyvpn");
        getActivity().registerReceiver(mInfoReceiver, filter);
        // [encryption-vpn@lge.com] FRIENDLY VPN [END]
        if (savedState != null) {
            VpnProfile profile = VpnProfile.decode(savedState.getString("VpnKey"),
                    savedState.getByteArray("VpnProfile"));
            if (profile != null) {
                mDialog = new VpnDialog(getActivity(), this, profile,
                        savedState.getBoolean("VpnEditing"), false);
            }
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-282]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            com.android.settings.MDMSettingsAdapter mdm = com.android.settings.MDMSettingsAdapter
                    .getInstance();
            mdm.addVpnPolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
            if (mdm.checkDisabledVpnConnect(getActivity())) {
                Log.i("MDM", TAG + "Disallow Vpn and finish");
                getActivity().finish();
            }
        }
        // LGMDM_END
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.vpn, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        //myeonghwan.kim@lge.com Hide native add menu
        menu.findItem(R.id.vpn_create).setVisible(false);
        // Hide lockdown VPN on devices that require IMS authentication
        if (SystemProperties.getBoolean("persist.radio.imsregrequired", false)) {
            menu.findItem(R.id.vpn_lockdown).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.vpn_create: {
                // Generate a new key. Here we just use the current time.
                long millis = System.currentTimeMillis();
                while (mPreferences.containsKey(Long.toHexString(millis))) {
                    ++millis;
                }
                mDialog = new VpnDialog(
                getActivity(), this, new VpnProfile(Long.toHexString(millis)), true, true);
                mDialog.setOnDismissListener(this);
                mDialog.show();
                return true;
            }
            case R.id.vpn_lockdown: {
                LockdownConfigFragment.show(this);
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        // We do not save view hierarchy, as they are just profiles.
        if (mDialog != null) {
            VpnProfile profile = mDialog.getProfile();
            savedState.putString("VpnKey", profile.key);
            savedState.putByteArray("VpnProfile", profile.encode());
            savedState.putBoolean("VpnEditing", mDialog.isEditing());
        }
        // else?
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mUnavailable) {
            TextView emptyView = (TextView)getView().findViewById(android.R.id.empty);
            getListView().setEmptyView(emptyView);
            if (emptyView != null) {
                emptyView.setText(R.string.vpn_settings_not_available);
            }
            return;
        }

        final boolean pickLockdown = getActivity()
                .getIntent().getBooleanExtra(EXTRA_PICK_LOCKDOWN, false);
        if (pickLockdown) {
            LockdownConfigFragment.show(this);
        }

        // [encryption-vpn@lge.com] Change title  [START]
        boolean isMultiPane = getResources().getBoolean(R.bool.lg_preferences_prefer_dual_pane);
        if (FEATURE_LG_VPN && !isMultiPane) {
            ActionBar actionBar = getActivity().getActionBar();
            actionBar
                    .setTitle(getActivity().getResources().getString(R.string.sp_basic_vpn_NORMAL));
        }
        // [encryption-vpn@lge.com] Change title  [END]

        // Check KeyStore here, so others do not need to deal with it.
        if (!mKeyStore.isUnlocked()) {
            if (!mUnlocking) {
                // Let us unlock KeyStore. See you later!
                Credentials.getInstance().unlock(getActivity(), "com.android.settings.vpn2");
            } else {
                // We already tried, but it is still not working!
                finishFragment();
            }
            mUnlocking = !mUnlocking;
            return;
        }

        // Now KeyStore is always unlocked. Reset the flag.
        mUnlocking = false;

        // Currently we are the only user of profiles in KeyStore.
        // Assuming KeyStore and KeyGuard do the right thing, we can
        // safely cache profiles in the memory.
        if (mPreferences.size() == 0) {

            final Context context = getActivity();
            final List<VpnProfile> profiles = loadVpnProfiles(mKeyStore);
            for (VpnProfile profile : profiles) {
                final VpnPreference pref = new VpnPreference(context, profile);
                pref.setOnPreferenceClickListener(this);
                mPreferences.put(profile.key, pref);
                mPs.addPreference(pref);
            }
            getPreferenceScreen().findPreference("add_network").setOnPreferenceClickListener(this);
        }

        // Show the dialog if there is one.
        if (mDialog != null) {
            mDialog.setOnDismissListener(this);
            mDialog.show();
        }

        // Start monitoring.
        if (mUpdater == null) {
            mUpdater = new Handler(this);
        }
        mUpdater.sendEmptyMessage(0);

        // Register for context menu. Hmmm, getListView() is hidden?
        registerForContextMenu(getListView());

        // LG add for SettingsSearch start
        mSearch_result = getActivity().getIntent().getStringExtra("search_item");
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirstSearch){
            startPerformClick();
            mIsFirstSearch = false;
        }
    }

    private void startPerformClick(){
        if(mSearch_result.equals(PREF_KEY_ADD_NETWORK)){
            PreferenceScreen ps = getPreferenceScreen();
            Preference pref = ps.findPreference(PREF_KEY_ADD_NETWORK);
            pref.performClick(ps);
        }
    }
    // LG add for settingsSearch end

    @Override
    public void onPause() {
        super.onPause();

        if (mUnavailable) {
            return;
        }

        // Hide the dialog if there is one.
        //        if (mDialog != null) {
        //            mDialog.setOnDismissListener(null);
        //            mDialog.dismiss();
        //        }  // kerry - this is problem after orientation change

        // Unregister for context menu.
        if (getView() != null) {
            unregisterForContextMenu(getListView());
        }
    }
    // [encryption-vpn@lge.com] FRIENDLY VPN [START]
    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mInfoReceiver);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-282]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            getActivity().unregisterReceiver(mLGMDMReceiver);
        }
        // LGMDM_END
    }
    // [encryption-vpn@lge.com] FRIENDLY VPN [START]

    @Override
    public void onDismiss(DialogInterface dialog) {
        // Here is the exit of a dialog.
        mDialog = null;
    }

    @Override
    public void onClick(DialogInterface dialog, int button) {
        if (button == DialogInterface.BUTTON_POSITIVE) {
            if (null == mDialog) {
                mDialog = savedDialog;
            } // kerry - after orientation

            // Always save the profile.
            VpnProfile profile = mDialog.getProfile();
            mKeyStore.put(Credentials.VPN + profile.key, profile.encode(), KeyStore.UID_SELF,
                    KeyStore.FLAG_ENCRYPTED);

            // Update the preference.
            VpnPreference preference = mPreferences.get(profile.key);
            if (preference != null) {
                disconnect(profile.key);
                preference.update(profile);
            } else {
                preference = new VpnPreference(getActivity(), profile);
                preference.setOnPreferenceClickListener(this);
                mPreferences.put(profile.key, preference);
                mPs.addPreference(preference);
            }

            // If we are not editing, connect!
            if (!mDialog.isEditing()) {
// yung.im@lgepartner.com 20141121 [
                if (SystemProperties.get("ro.build.target_operator").equals("SKT")) {
                    final Context context = getActivity();
                    File file;
                    if (Utils.checkPackage(context, "com.skt.tbmon")) {
                        file = new File("/data/tbased/iwlandrunning");
                        Log.v(TAG, "Exist tbased!");
                    } else {
                        file = new File("/data/iwland/iwlandrunning");
                        Log.v(TAG, "Not exist tbased!");
                    }

                    if (file.exists()) {
                        Log.v(TAG, "iwlan is running!");
                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                .setTitle(android.R.string.dialog_alert_title)
                                .setMessage(R.string.sp_vpn_iwlan_running_NORMAL)
                                .setPositiveButton(android.R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).create();
                        alertDialog.show();
                        dialog.dismiss();
                        return;
                    } else {
                        Log.v(TAG, "iwlan is Not running!");
                    }
                    Log.v(TAG, "iwlan !");
                }
// yung.im@lgepartner.com 20141121 ]
                try {
                    connect(profile);
                } catch (Exception e) {
                    Log.e(TAG, "connect", e);
                }
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
        if (mDialog != null) {
            Log.v(TAG, "onCreateContextMenu() is called when mDialog != null");
            return;
        }

        if (info instanceof AdapterContextMenuInfo) {
            Preference preference = (Preference)getListView().getItemAtPosition(
                    ((AdapterContextMenuInfo)info).position);
            if (preference instanceof VpnPreference) {
                VpnProfile profile = ((VpnPreference)preference).getProfile();
                mSelectedKey = profile.key;
                menu.setHeaderTitle(profile.name);
                menu.add(Menu.NONE, R.string.vpn_menu_edit, 0, R.string.edit);
                menu.add(Menu.NONE, R.string.vpn_menu_delete, 0, R.string.delete);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mDialog != null) {
            Log.v(TAG, "onContextItemSelected() is called when mDialog != null");
            return false;
        }

        VpnPreference preference = mPreferences.get(mSelectedKey);
        if (preference == null) {
            Log.v(TAG, "onContextItemSelected() is called but no preference is found");
            return false;
        }

        switch (item.getItemId()) {
            case R.string.vpn_menu_edit:
                mDialog = new VpnDialog(getActivity(), this, preference.getProfile(), true, false);
                mDialog.setOnDismissListener(this);
                mDialog.show();
                return true;
            case R.string.vpn_menu_delete:
                deleteProfile(preference);
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (mDialog != null) {
            Log.v(TAG, "onPreferenceClick() is called when mDialog != null");
            return true;
        }

        if (preference instanceof VpnPreference) {
            VpnProfile profile = ((VpnPreference)preference).getProfile();
            if (mInfo != null && profile.key.equals(mInfo.key) &&
                    mInfo.state == LegacyVpnInfo.STATE_CONNECTED) {
                try {
                    mInfo.intent.send();
                    return true;
                } catch (Exception e) {
                    // ignore
                }
            }
            mDialog = new VpnDialog(getActivity(), this, profile, false, false);
        } else {
            // Generate a new key. Here we just use the current time.
            long millis = System.currentTimeMillis();
            while (mPreferences.containsKey(Long.toHexString(millis))) {
                ++millis;
            }
            mDialog = new VpnDialog(getActivity(), this,
                    new VpnProfile(Long.toHexString(millis)), true, true);
        }
        mDialog.setOnDismissListener(this);
        mDialog.show();
        return true;
    }

    @Override
    public boolean handleMessage(Message message) {
        mUpdater.removeMessages(0);

        if (isResumed()) {
            try {
                LegacyVpnInfo info = mService.getLegacyVpnInfo();
                if (mInfo != null) {
                    VpnPreference preference = mPreferences.get(mInfo.key);
                    if (preference != null) {
                        preference.update(-1);
                    }
                    mInfo = null;
                }
                if (info != null) {
                    VpnPreference preference = mPreferences.get(info.key);
                    if (preference != null) {
                        preference.update(info.state);
                        mInfo = info;
                    }
                }
            } catch (Exception e) {
                // ignore
                Log.w(TAG, "Exception");
            }
            mUpdater.sendEmptyMessageDelayed(0, 1000);
        }
        return true;
    }

    private void connect(VpnProfile profile) throws Exception {

        // LGE_CHANGE_S [encryption-vpn@lge.com][VPN: Mutual Disconnection]
        if (true == FEATURE_LG_VPN) {
            mayDisconnectLgeVpn();
        }
        // LGE_CHANGE_E [encryption-vpn@lge.com][VPN: Mutual Disconnection]

        try {
            mService.startLegacyVpn(profile);
        } catch (IllegalStateException e) {
            Toast.makeText(getActivity(), R.string.vpn_no_network, Toast.LENGTH_LONG).show();
        }
    }

    private void disconnect(String key) {
        if (mInfo != null && key.equals(mInfo.key)) {
            try {
                mService.prepareVpn(VpnConfig.LEGACY_VPN, VpnConfig.LEGACY_VPN);
            } catch (Exception e) {
                // ignore
                Log.w(TAG, "Exception");
            }
        }
    }

    // LGE_CHANGE_S [encryption-vpn@lge.com][VPN: Mutual Disconnection]
    private void mayDisconnectLgeVpn() {
        // 1. get ActivityManager.
        android.app.ActivityManager am = null;
        try {
            am = (android.app.ActivityManager)getSystemService(getActivity().ACTIVITY_SERVICE);
            if (null == am) {
                Log.w(TAG, "ActivityManager is null");
                return;
            }
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }

        boolean sendIntent = false; // prevent send intent duplicately.
        final int WAIT_MILLIS_TIME = 100; // 0.1 second.
        final int WAIT_MILLIS_TIME_TOTAL = 5000; // 5 second.
        final long WAIT_MILLIS_TIME_OVER = System.currentTimeMillis() + WAIT_MILLIS_TIME_TOTAL;

        do {
            // 2. GUIService destroyed?
            boolean isServiceExist = false;

            try {
                // 3. get running service.
                List<android.app.ActivityManager.RunningServiceInfo> rsiList = null;
                rsiList = am.getRunningServices(Integer.MAX_VALUE);
                if (null == rsiList) {
                    Log.w(TAG, "getRunningServices() is null");
                    return;
                }

                // 4. find & send STOP intent.
                for (android.app.ActivityManager.RunningServiceInfo rsi : rsiList) {
                    if (rsi.service.getClassName().equals(
                            "com.ipsec.vpnclient.WorkerService")) {
                        isServiceExist = true;
                        if (false == sendIntent) {
                            android.content.Intent intent = new android.content.Intent(
                                    "com.ipsec.vpnclient.WorkerService.ACTION.STOP");
                            getActivity().sendBroadcast(intent);
                            sendIntent = true;
                        }

                    }
                }
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
            }

            // 5. 'NO GUIService' or 'wait time over' then exit do~while() statement.
            if ((false == isServiceExist) || (WAIT_MILLIS_TIME_OVER < System.currentTimeMillis())) {
                break;
            }

            // 6. wait for GUIService destory.
            if (true == isServiceExist) {
                try {
                    Thread.sleep(WAIT_MILLIS_TIME);
                } catch (InterruptedException e) {
                    Log.w(TAG, "InterruptedException");
                }
            }
        } while (true);
    }

    // LGE_CHANGE_E [encryption-vpn@lge.com][VPN: Mutual Disconnection]
    @Override
    protected int getHelpResource() {
        return R.string.help_url_vpn;
    }

    private static class VpnPreference extends Preference {
        private VpnProfile mProfile;
        private int mState = -1;

        VpnPreference(Context context, VpnProfile profile) {
            super(context);
            setPersistent(false);
            //setOrder(0);

            mProfile = profile;
            update();
        }

        VpnProfile getProfile() {
            return mProfile;
        }

        void update(VpnProfile profile) {
            mProfile = profile;
            update();
        }

        void update(int state) {
            mState = state;
            update();
        }

        void update() {
            if (mState < 0) {
                String[] types = getContext().getResources()
                        .getStringArray(R.array.vpn_types_long);
                setSummary(types[mProfile.type]);
            } else {
                String[] states = getContext().getResources()
                        .getStringArray(R.array.vpn_states);
                setSummary(states[mState]);
            }
            setTitle(mProfile.name);
            notifyHierarchyChanged();
        }

        @Override
        public int compareTo(Preference preference) {
            int result = -1;
            if (preference instanceof VpnPreference) {
                VpnPreference another = (VpnPreference)preference;
                if ((result = another.mState - mState) == 0 &&
                        (result = mProfile.name.compareTo(another.mProfile.name)) == 0 &&
                        (result = mProfile.type - another.mProfile.type) == 0) {
                    result = mProfile.key.compareTo(another.mProfile.key);
                }
            }
            return result;
        }
    }

    /**
     * Dialog to configure always-on VPN.
     */
    public static class LockdownConfigFragment extends DialogFragment {
        private List<VpnProfile> mProfiles;
        private List<CharSequence> mTitles;
        private int mCurrentIndex;
        private int mSelectIndex;

        private static class TitleAdapter extends ArrayAdapter<CharSequence> {
            public TitleAdapter(Context context, List<CharSequence> objects) {
                super(context, com.android.internal.R.layout.simple_list_item_single_choice,
                        android.R.id.text1, objects);
            }
        }

        public static void show(VpnSettings parent) {
            if (!parent.isAdded()) {
                return;
            }

            final LockdownConfigFragment dialog = new LockdownConfigFragment();
            dialog.show(parent.getFragmentManager(), TAG_LOCKDOWN);
        }

        private static String getStringOrNull(KeyStore keyStore, String key) {
            final byte[] value = keyStore.get(Credentials.LOCKDOWN_VPN);
            return value == null ? null : new String(value);
        }

        private void initProfiles(KeyStore keyStore, Resources res) {
            final String lockdownKey = getStringOrNull(keyStore, Credentials.LOCKDOWN_VPN);

            mProfiles = loadVpnProfiles(keyStore, VpnProfile.TYPE_PPTP);
            mTitles = Lists.newArrayList();
            mTitles.add(res.getText(R.string.vpn_lockdown_none));
            mCurrentIndex = 1;
            mSelectIndex = 1;

            for (VpnProfile profile : mProfiles) {
                if (TextUtils.equals(profile.key, lockdownKey)) {
                    mCurrentIndex = mTitles.size() + 1;
                }
                mTitles.add(profile.name);
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final KeyStore keyStore = KeyStore.getInstance();

            initProfiles(keyStore, context.getResources());

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final LayoutInflater dialogInflater = LayoutInflater.from(builder.getContext());

            builder.setTitle(R.string.vpn_menu_lockdown);

            final View view = dialogInflater.inflate(R.layout.vpn_lockdown_editor, null, false);
            final ListView listView = (ListView)view.findViewById(android.R.id.list);
            final View header = dialogInflater.inflate(R.layout.vpn_lockdown_header, null, false);
            listView.addHeaderView(header);

            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setAdapter(new TitleAdapter(context, mTitles));
            listView.setItemChecked(mCurrentIndex, true);
            //myeonghwan.kim@lge.com 20130327 Add item click listener [S]
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                    if (pos == 0) {
                        listView.setItemChecked(mSelectIndex, true);
                    } else {
                        mSelectIndex = pos;
                    }
                    ;
                }
            });
            //myeonghwan.kim@lge.com 20130327 Add item click listener [E]
            builder.setView(view);

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final int newIndex = listView.getCheckedItemPosition();
                    if (mCurrentIndex == newIndex) {
                        return;
                    }
                    if (newIndex == 1) {
                        keyStore.delete(Credentials.LOCKDOWN_VPN);

                    } else {
                        final VpnProfile profile = mProfiles.get(newIndex - 2);
                        if (!profile.isValidLockdownProfile()) {
                            Toast.makeText(context, R.string.vpn_lockdown_config_error,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        keyStore.put(Credentials.LOCKDOWN_VPN, profile.key.getBytes(),
                                KeyStore.UID_SELF, KeyStore.FLAG_ENCRYPTED);
                    }

                    // kick profiles since we changed them
                    ConnectivityManager.from(getActivity()).updateLockdownVpn();
                }
            });

            return builder.create();
        }
    }

    private static List<VpnProfile> loadVpnProfiles(KeyStore keyStore, int... excludeTypes) {
        final ArrayList<VpnProfile> result = Lists.newArrayList();
        final String[] keys = keyStore.saw(Credentials.VPN);
        if (keys != null) {
            for (String key : keys) {
                final VpnProfile profile = VpnProfile.decode(
                        key, keyStore.get(Credentials.VPN + key));
                if (profile != null && !ArrayUtils.contains(excludeTypes, profile.type)) {
                    result.add(profile);
                }
            }
        }
        return result;
    }

    // kerry - confirm delete
    private void deleteProfile(final Preference preference) {
        DialogInterface.OnClickListener onClickListener =
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            disconnect(mSelectedKey);
                            mPs.removePreference(preference);
                            mPreferences.remove(mSelectedKey);
                            mKeyStore.delete(Credentials.VPN + mSelectedKey);
                        }
                    }
                };
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete)
                // .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(R.string.sp_vpn_confirm_profile_deletion_NORMAL)
                .setPositiveButton(android.R.string.ok, onClickListener)
                .setNegativeButton(R.string.cancel, onClickListener)
                .show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        if (mDialog != null) {
            savedDialog = mDialog;

            Spinner vpnType = (Spinner)mDialog.findViewById(R.id.type);

            if (true == vpnType.isActivated()) {
                vpnType.setSelection(mDialog.getProfile().type);
                mDialog.dismiss();
                mDialog.show(); // kerry - workaround for spinner close
            } // kerry - conflict with IME edit mode
        }
    } // KERRY

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-282]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.android.settings.MDMSettingsAdapter.getInstance()
                        .receiveVpnPolicyChangeIntent(intent)) {
                    getActivity().finish();
                }
            }
        }
    };
    // LGMDM_END
    
    // add for SettingsSearch start
     public static void setSearchIndexData(Context context, String key, 
                         String title, String screenTitle, String summaryOn,
                         String summaryOff, String intentAction, String intentTargetClass,
                         String intentTargetPackage, int currentEnable,
                         String preferenceType, String settingsDBTableName,
                         String settingsDBField, int visible, int checkValue) {
        SearchIndexableRaw data = new SearchIndexableRaw(context);
        data.key = key;
        data.title = title;
        data.screenTitle = screenTitle;
        data.summaryOn = summaryOn;
        data.summaryOff = summaryOff;
        data.intentAction = intentAction;
        data.intentTargetClass = intentTargetClass;
        data.intentTargetPackage = intentTargetPackage;
        data.currentEnable = currentEnable;
        data.preferenceType = preferenceType;
        data.settingsDBTableName = settingsDBTableName;
        data.settingsDBField = settingsDBField;
        data.visible = visible;
        data.checkValue = checkValue;
        mResult.add(data);
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            
        Log.i("vpn","new vpn updater making 1");
        // updater = new BasicVPNSearchIndexableItemUpdater(context);
        Log.i("vpn","new vpn updater making 2");
        FEATURE_LG_VPN = Utils.isSupportVPN(context);
        mKeyStoreEnabled = (mKeyStore.state().equals(KeyStore.LOCKED)||mKeyStore.state().equals(KeyStore.UNINITIALIZED))? 0:1;

        String vpnUiPath = context.getString(R.string.wireless_more_settings_title)  // "More"
                           + " > " + context.getString(R.string.vpn_settings_title); // "VPN"

        setSearchIndexData(context,                // context
                           PREF_KEY_BASIC_VPN,     // key
                           context.getString(R.string.sp_basic_vpn_NORMAL),           // title
                           vpnUiPath,              // screenTitle
                           context.getString(R.string.sp_basic_vpn_summary_NORMAL),   // summaryOn
                           null,                   // summaryOff
                           VPNSETTINGS_ACTIVITY_ACTION,            // intentAction
                           null,                   // intentTargetClass
                           null,                   // intentTargetPackage                              
                           (FEATURE_LG_VPN)?1:0,   // currentEnable
                           null,                   // preferenceType
                           null,                   // settingsDBTableName
                           null,                   // settingsDBField
                           (FEATURE_LG_VPN)?1:0,   // visible
                           0                       // checkValue
                           );
        
        setSearchIndexData(context,             // context
                           PREF_KEY_LG_VPN,     // key
                           context.getString(R.string.sp_lg_vpn_NORMAL),          // title
                           vpnUiPath,           // screenTitle
                           context.getString(R.string.sp_lg_vpn_summary_NORMAL),  // summaryOn
                           null,                // summaryOff
                           INTENT_ACTION,       // intentAction
                           LGVPN_MAIN_ACTIVITY, // intentTargetClass
                           LGVPN_PACKAGE,       // intentTargetPackage
                           (FEATURE_LG_VPN)?1:0,// currentEnable
                           null,                // preferenceType
                           null,                // settingsDBTableName
                           null,                // settingsDBField
                           (FEATURE_LG_VPN)?1:0,// visible
                           0                    // checkValue
                           );

        // "add VPN network" when LG VPN disabled, or "add Basic VPN network" when LGVPN enabled

        String addBasicVpnTitle = "";
        String addBasicVpnScreenTitle = "";
        if (FEATURE_LG_VPN){
            addBasicVpnTitle = context.getString(R.string.sp_basic_vpn_create_NORMAL);
            addBasicVpnScreenTitle = vpnUiPath + " > " + context.getString(R.string.sp_basic_vpn_NORMAL);
        } else {
            addBasicVpnTitle = context.getString(R.string.vpn_create);
            addBasicVpnScreenTitle = vpnUiPath;
        }
        setSearchIndexData(context,                 // context
                           PREF_KEY_ADD_NETWORK,    // key
                           addBasicVpnTitle,        // title
                           addBasicVpnScreenTitle,  // screenTitle
                           null,        // summaryOn
                           null,        // summaryOff
                           VPNSETTINGS_ACTIVITY_ACTION, // intentAction
                           null,        // intentTargetClass
                           null,        // intentTargetPackage
                           1,           // currentEnable
                           null,        // preferenceType
                           null,        // settingsDBTableName
                           null,        // settingsDBField
                           mKeyStoreEnabled,    // visible
                           0            // checkValue
                           );
        return mResult;
        }
    };
    // add for SettingsSearch end
}
