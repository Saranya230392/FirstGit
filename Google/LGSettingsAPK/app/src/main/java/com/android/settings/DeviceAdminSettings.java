/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.provider.Settings;
import android.os.SystemProperties;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Collection;

// 3LM_MDM L taewon.jang@lge.com
import com.android.settings.lgesetting.Config.Config;
import android.os.IDeviceManager3LM;
import android.os.RemoteException;
import android.os.ServiceManager;

public class DeviceAdminSettings extends ListFragment {
    static final String TAG = "DeviceAdminSettings";

    private final String SPR_EXTENSION_ADMIN_CLASS = "com.sprint.extension.admin.SprintExtensionDeviceAdminReceiver";

    static final int DIALOG_WARNING = 1;
    private DevicePolicyManager mDPM;
    private UserManager mUm;

    /**
     * Internal collection of device admin info objects for all profiles
     * associated with the current user.
     */
    private final SparseArray<ArrayList<DeviceAdminInfo>> mAdminsByProfile = new SparseArray<ArrayList<DeviceAdminInfo>>();

    private String mDeviceOwnerPkg;
    private SparseArray<ComponentName> mProfileOwnerComponents = new SparseArray<ComponentName>();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-156]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            android.content.IntentFilter filterLGMDM = new android.content.IntentFilter();
            MDMSettingsAdapter.getInstance()
                    .addRemoveAdminPolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (getActivity() != null) {
            getActivity().setTitle(getResources().getString(R.string.manage_device_admin));
        }
        mDPM = (DevicePolicyManager)getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mUm = (UserManager)getActivity().getSystemService(Context.USER_SERVICE);
        return inflater.inflate(R.layout.device_admin_settings, container,
                false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDeviceOwnerPkg = mDPM.getDeviceOwner();
        if (mDeviceOwnerPkg != null && !mDPM.isDeviceOwner(mDeviceOwnerPkg)) {
            mDeviceOwnerPkg = null;
        }
        mProfileOwnerComponents.clear();
        final List<UserHandle> profiles = mUm.getUserProfiles();
        final int profilesSize = profiles.size();
        for (int i = 0; i < profilesSize; ++i) {
            final int profileId = profiles.get(i).getIdentifier();
            mProfileOwnerComponents.put(profileId,
                    mDPM.getProfileOwnerAsUser(profileId));
        }
        updateList();
    }

    /**
     * Update the internal collection of available admins for all profiles
     * associated with the current user.
     */
    void updateList() {
        mAdminsByProfile.clear();

        final List<UserHandle> profiles = mUm.getUserProfiles();
        final int profilesSize = profiles.size();
        for (int i = 0; i < profilesSize; ++i) {
            final int profileId = profiles.get(i).getIdentifier();
            updateAvailableAdminsForProfile(profileId);
        }

        getListView().setAdapter(new PolicyListAdapter());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Object o = l.getAdapter().getItem(position);
        if (!(o instanceof DeviceAdminInfo)) {
            // race conditions may cause this
            return;
        }
        DeviceAdminInfo dpi = (DeviceAdminInfo)o;
        final Activity activity = getActivity();
        final int userId = getUserId(dpi);
        // 3LM_MDM_DCM_START jihun.im@lge.com 20121015
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            boolean isKddi = SystemProperties.getInt("ro.3LM.extended", 0) == 1;
            int defaultLocked = isKddi ? 1 : 0;
            boolean lockAdmin = Settings.Secure.getInt(getActivity()
                    .getContentResolver(), "admin_locked", defaultLocked) == 1;
            if (dpi.getPackageName().startsWith("com.threelm.dm")) {
                // DM lock state depends on Settings.Global.ADMIN_LOCKED
                // and not on 3LM service api
                if (lockAdmin && isActiveAdmin(dpi)) {
                    Resources res = getResources();
                    Toast.makeText(getActivity(),
                            res.getString(R.string.cantDeactivate3LM),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // Lock state of other Admins depends on 3LM service api
                IDeviceManager3LM dm = IDeviceManager3LM.Stub.asInterface(
                        ServiceManager.getService(Context.DEVICE_MANAGER_3LM_SERVICE));
                try {
                    if (dm.getDeviceAdminLock(dpi.getPackageName())) {
                        return;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "3LM Exception");
                    // Should never happen
                }
            }
        }
        // 3LM_MDM_DCM_END jihun.im@lge.com 20121015
        if (userId == UserHandle.myUserId() || !isProfileOwner(dpi)) {
            Intent intent = new Intent();
            intent.setClass(activity, DeviceAdminAdd.class);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    dpi.getComponent());
            activity.startActivityAsUser(intent, new UserHandle(userId));
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(getString(
                    R.string.managed_profile_device_admin_info,
                    dpi.loadLabel(activity.getPackageManager())));
            builder.setPositiveButton(android.R.string.ok, null);
            builder.create().show();
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox checkbox;
        TextView description;
    }

    class PolicyListAdapter extends BaseAdapter {
        final LayoutInflater mInflater;

        PolicyListAdapter() {
            mInflater = (LayoutInflater)getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public int getCount() {
            int adminCount = 0;
            final int profileCount = mAdminsByProfile.size();
            for (int i = 0; i < profileCount; ++i) {
                adminCount += mAdminsByProfile.valueAt(i).size();
            }
            // Add 'profileCount' for title items.
            return adminCount + profileCount;
        }

        /**
         * The item for the given position in the list.
         * 
         * @return a String object for title items and a DeviceAdminInfo object
         *         for actual device admins.
         */
        @Override
        public Object getItem(int position) {
            if (position < 0) {
                throw new ArrayIndexOutOfBoundsException();
            }
            // The position of the item in the list of admins.
            // We start from the given position and discount the length of the
            // upper lists until we
            // get the one for the right profile
            int adminPosition = position;
            final int n = mAdminsByProfile.size();
            int i = 0;
            for (; i < n; ++i) {
                // The elements in that section including the title item (that's
                // why adding one).
                final int listSize = mAdminsByProfile.valueAt(i).size() + 1;
                if (adminPosition < listSize) {
                    break;
                }
                adminPosition -= listSize;
            }
            if (i == n) {
                throw new ArrayIndexOutOfBoundsException();
            }
            // If countdown == 0 that means the title item
            if (adminPosition == 0) {
                Resources res = getActivity().getResources();
                if (mAdminsByProfile.keyAt(i) == UserHandle.myUserId()) {
                    return res.getString(R.string.header_category_personal);
                } else {
                    return res.getString(R.string.category_work);
                }
            } else {
                // Subtracting one for the title.
                return mAdminsByProfile.valueAt(i).get(adminPosition - 1);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        /**
         * See {@link #getItemViewType} for the view types.
         */
        @Override
        public int getViewTypeCount() {
            return 2;
        }

        /**
         * Returns 1 for title items and 0 for anything else.
         */
        @Override
        public int getItemViewType(int position) {
            Object o = getItem(position);
            return (o instanceof String) ? 1 : 0;
        }

        @Override
        public boolean isEnabled(int position) {
            Object o = getItem(position);
            return isEnabled(o);
        }

        private boolean isEnabled(Object o) {
            if (!(o instanceof DeviceAdminInfo)) {
                // Title item
                return false;
            }

            DeviceAdminInfo info = (DeviceAdminInfo)o;

            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-156]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                    && com.android.settings.MDMSettingsAdapter.getInstance()
                            .setAdminListEnableClickMenu(getActivity(), info)) {
                return false;
            }
            // LGMDM_END

            if (isActiveAdmin(info) && getUserId(info) == UserHandle.myUserId()
                    && (isDeviceOwner(info) || isProfileOwner(info))) {
                return false;
            }
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Object o = getItem(position);
            if (o instanceof DeviceAdminInfo) {
                if (convertView == null) {
                    convertView = newDeviceAdminView(parent);
                }
                bindView(convertView, (DeviceAdminInfo)o);
            } else {
                if (convertView == null) {
                    convertView = newTitleView(parent);
                }
                final TextView title = (TextView)convertView
                        .findViewById(android.R.id.title);
                title.setText((String)o);
            }
            return convertView;
        }

        private View newDeviceAdminView(ViewGroup parent) {
            View v = mInflater.inflate(R.layout.device_admin_item, parent,
                    false);
            ViewHolder h = new ViewHolder();
            h.icon = (ImageView)v.findViewById(R.id.icon);
            h.name = (TextView)v.findViewById(R.id.name);
            h.checkbox = (CheckBox)v.findViewById(R.id.checkbox);
            h.description = (TextView)v.findViewById(R.id.description);
            v.setTag(h);
            return v;
        }

        private View newTitleView(ViewGroup parent) {
            final TypedArray a = mInflater.getContext().obtainStyledAttributes(
                    null, android.R.styleable.Preference,
                    android.R.attr.preferenceCategoryStyle, 0);
            final int resId = a.getResourceId(
                    android.R.styleable.Preference_layout, 0);
            return mInflater.inflate(resId, parent, false);
        }

        private void bindView(View view, DeviceAdminInfo item) {
            final Activity activity = getActivity();
            ViewHolder vh = (ViewHolder)view.getTag();
            Drawable activityIcon = item.loadIcon(activity.getPackageManager());
            Drawable badgedIcon = activity.getPackageManager()
                    .getUserBadgedIcon(activityIcon,
                            new UserHandle(getUserId(item)));
            vh.icon.setImageDrawable(badgedIcon);
            vh.name.setText(item.loadLabel(activity.getPackageManager()));
            vh.checkbox.setChecked(isActiveAdmin(item));

            // 3LM_MDM_DCM_START jihun.im@lge.com 20121015
            boolean allowedBy3lm = true;
            if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
                // Disable the checkbox for 3LM DeviceAdmin if policy says so.
                boolean isKddi = SystemProperties.getInt("ro.3LM.extended", 0) == 1;
                int defaultLocked = isKddi ? 1 : 0;
                boolean lockAdmin = Settings.Secure.getInt(getActivity()
                        .getContentResolver(), "admin_locked", defaultLocked) == 1;
                if (item.getPackageName().startsWith("com.threelm.dm")) {
                     //  3LM DeviceManager is locked based on global setting
                     if (lockAdmin) {
                         allowedBy3lm = false;
                     }
                } else {
                    // Lock state of other Admins depends on 3LM service api
                    IDeviceManager3LM dm = IDeviceManager3LM.Stub.asInterface(
                                    ServiceManager.getService(Context.DEVICE_MANAGER_3LM_SERVICE));
                    try {
                        if (dm.getDeviceAdminLock(item.getPackageName())) {
                            allowedBy3lm = false;
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "3LM Exception");
                      // Should never happen
                    }
                }
            }
            // 3LM_MDM_DCM_END jihun.im@lge.com 20121015

            final boolean enabled = isEnabled(item);
            try {
                vh.description.setText(item.loadDescription(activity
                        .getPackageManager()));
            } catch (Resources.NotFoundException e) {
            }

            // 3LM_MDM_START KK
            if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
                vh.checkbox.setEnabled(enabled && allowedBy3lm);
                vh.name.setEnabled(enabled && allowedBy3lm);
                vh.description.setEnabled(enabled && allowedBy3lm);
                vh.icon.setEnabled(enabled && allowedBy3lm);
            } else {
                // 3LM_MDM_END
                vh.checkbox.setEnabled(enabled);
                vh.name.setEnabled(enabled);
                vh.description.setEnabled(enabled);
                vh.icon.setEnabled(enabled);
            }

            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-156]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM && vh.icon != null
                    && vh.name != null && vh.checkbox != null
                    && vh.description != null) {
                com.android.settings.MDMSettingsAdapter.getInstance()
                        .setAdminListEnableDisplayMenu(getActivity(), item,
                                vh.icon, vh.name, vh.checkbox, vh.description);
            }
            // LGMDM_END
        }
    }

    private boolean isDeviceOwner(DeviceAdminInfo item) {
        return getUserId(item) == UserHandle.myUserId()
                && item.getPackageName().equals(mDeviceOwnerPkg);
    }

    private boolean isProfileOwner(DeviceAdminInfo item) {
        ComponentName profileOwner = mProfileOwnerComponents
                .get(getUserId(item));
        return item.getComponent().equals(profileOwner);
    }

    private boolean isActiveAdmin(DeviceAdminInfo item) {
        return mDPM.isAdminActiveAsUser(item.getComponent(), getUserId(item));
    }

    /**
     * Add device admins to the internal collection that belong to a profile.
     * 
     * @param profileId
     *            the profile identifier.
     */
    private void updateAvailableAdminsForProfile(final int profileId) {
        // We are adding the union of two sets 'A' and 'B' of device admins to
        // mAvailableAdmins.
        // Set 'A' is the set of active admins for the profile whereas set 'B'
        // is the set of
        // listeners to DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED for the
        // profile.

        // Add all of set 'A' to mAvailableAdmins.
        List<ComponentName> activeAdminsListForProfile = mDPM
                .getActiveAdminsAsUser(profileId);
        addActiveAdminsForProfile(activeAdminsListForProfile, profileId);

        // Collect set 'B' and add B-A to mAvailableAdmins.
        addDeviceAdminBroadcastReceiversForProfile(activeAdminsListForProfile,
                profileId);
    }

    /**
     * Add a profile's device admins that are receivers of
     * {@code DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED} to the internal
     * collection if they haven't been added yet.
     * 
     * @param alreadyAddedComponents
     *            the set of active admin component names. Receivers of
     *            {@code DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED} whose
     *            component is in this set are not added to the internal
     *            collection again.
     * @param profileId
     *            the identifier of the profile
     */
    private void addDeviceAdminBroadcastReceiversForProfile(
            Collection<ComponentName> alreadyAddedComponents,
            final int profileId) {
        final PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> enabledForProfile = pm.queryBroadcastReceivers(
                new Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED),
                PackageManager.GET_META_DATA
                        | PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS,
                profileId);
        if (enabledForProfile == null) {
            enabledForProfile = Collections.emptyList();
        }
        final int n = enabledForProfile.size();
        ArrayList<DeviceAdminInfo> deviceAdmins = mAdminsByProfile
                .get(profileId);
        if (deviceAdmins == null) {
            deviceAdmins = new ArrayList<DeviceAdminInfo>(n);
        }
        for (int i = 0; i < n; ++i) {
            ResolveInfo resolveInfo = enabledForProfile.get(i);
            ComponentName riComponentName = new ComponentName(
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name);
            if (alreadyAddedComponents == null
                    || !alreadyAddedComponents.contains(riComponentName)) {
                DeviceAdminInfo deviceAdminInfo = createDeviceAdminInfo(resolveInfo);
                // add only visible ones (note: active admins are added
                // regardless of visibility)
                if (deviceAdminInfo != null && deviceAdminInfo.isVisible()) {
                    if (("SPR".equals(Config.getOperator()))
                            || ("BM".equals(Config.getOperator()))) {
                        if (deviceAdminInfo.getComponent().getClassName()
                                .equals(SPR_EXTENSION_ADMIN_CLASS)) {
                            continue;
                        }
                    }
                    deviceAdmins.add(deviceAdminInfo);
                }
            }
        }
        if (!deviceAdmins.isEmpty()) {
            mAdminsByProfile.put(profileId, deviceAdmins);
        }
    }

    /**
     * Add a {@link DeviceAdminInfo} object to the internal collection of
     * available admins for all active admin components associated with a
     * profile.
     * 
     * @param profileId
     *            a profile identifier.
     */
    private void addActiveAdminsForProfile(
            final List<ComponentName> activeAdmins, final int profileId) {
        if (activeAdmins != null) {
            final PackageManager packageManager = getActivity()
                    .getPackageManager();
            final int n = activeAdmins.size();
            ArrayList<DeviceAdminInfo> deviceAdmins = new ArrayList<DeviceAdminInfo>(
                    n);
            for (int i = 0; i < n; ++i) {
                ComponentName activeAdmin = activeAdmins.get(i);
                List<ResolveInfo> resolved = packageManager
                        .queryBroadcastReceivers(
                                new Intent().setComponent(activeAdmin),
                                PackageManager.GET_META_DATA
                                        | PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS,
                                profileId);
                if (resolved != null) {
                    final int resolvedMax = resolved.size();
                    for (int j = 0; j < resolvedMax; ++j) {
                        DeviceAdminInfo deviceAdminInfo = createDeviceAdminInfo(resolved
                                .get(j));
                        if (deviceAdminInfo != null) {
                            deviceAdmins.add(deviceAdminInfo);
                        }
                    }
                }
            }
            if (!deviceAdmins.isEmpty()) {
                mAdminsByProfile.put(profileId, deviceAdmins);
            }
        }
    }

    /**
     * Creates a device admin info object for the resolved intent that points to
     * the component of the device admin.
     * 
     * @param resolved
     *            resolved intent.
     * @return new {@link DeviceAdminInfo} object or null if there was an error.
     */
    private DeviceAdminInfo createDeviceAdminInfo(ResolveInfo resolved) {
        try {
            return new DeviceAdminInfo(getActivity(), resolved);
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Skipping " + resolved.activityInfo, e);
        } catch (IOException e) {
            Log.w(TAG, "Skipping " + resolved.activityInfo, e);
        }
        return null;
    }

    /**
     * Extracts the user id from a device admin info object.
     * 
     * @param adminInfo
     *            the device administrator info.
     * @return identifier of the user associated with the device admin.
     */
    private int getUserId(DeviceAdminInfo adminInfo) {
        return UserHandle
                .getUserId(adminInfo.getActivityInfo().applicationInfo.uid);
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-156]
    private final android.content.BroadcastReceiver mLGMDMReceiver = new android.content.BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance()
                        .receiveRemoveAdminPolicyChangeIntent(intent)) {
                    getActivity().finish();
                }
            }
        }
    };

    // LGMDM_END

    @Override
    public void onDestroy() {
        super.onDestroy();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-156]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                android.util.Log.w("MDM", "mLGMDMReceiver unregisterReceiver ",
                        e);
            }
        }
        // LGMDM_END
    }
}
