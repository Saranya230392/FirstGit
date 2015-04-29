/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.settings.users;

import android.os.Looper;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
// 3LM_MDM L
import android.os.IDeviceManager3LM;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.provider.Settings.Secure;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.view.ViewGroup;
import android.os.SystemProperties;

import com.android.internal.util.UserIcons;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtilsEx;
import com.android.settings.lockscreen.ChooseLockGeneric;
import com.android.settings.lockscreen.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.lockscreen.OwnerInfoSettings;

import android.widget.Toast;
import android.provider.ContactsContract.Profile;
import android.content.ContentResolver;

import com.android.settings.AccountPreference;
import com.android.settings.R;
import com.android.settings.SelectableEditTextPreference;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.drawable.CircleFramedDrawable;
import com.android.settings.SettingsPreferenceFragment.SettingsDialogFragment;
import com.android.settings.applications.ManageApplications;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

import android.database.Cursor;
import android.provider.Settings;

import com.lge.constants.UserInfoConstants;
import com.lge.constants.DevicePolicyManagerConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// [START][2014-10-21][seungyeop.yeom] restrict switch user (in call)
import android.telephony.TelephonyManager;
import com.android.internal.telephony.ITelephony;

// [END][2014-10-21][seungyeop.yeom] restrict switch user (in call)

public class UserSettings extends SettingsPreferenceFragment
        implements OnPreferenceClickListener, OnClickListener, DialogInterface.OnDismissListener,
        Preference.OnPreferenceChangeListener,
        EditUserInfoController.OnContentChangedCallback, Indexable {

    private static final String TAG = "UserSettings";

    /** UserId of the user being removed */
    private static final String SAVE_REMOVING_USER = "removing_user";
    /** UserId of the user that was just added */
    private static final String SAVE_ADDING_USER = "adding_user";

    private static final String KEY_USER_LIST = "user_list";
    private static final String KEY_USER_ME = "user_me";
    private static final String KEY_ADD_USER = "user_add";

    private static final int MENU_REMOVE_USER = Menu.FIRST;

    private static final int DIALOG_CONFIRM_REMOVE = 1;
    private static final int DIALOG_ADD_USER = 2;
    private static final int DIALOG_SETUP_USER = 3;
    private static final int DIALOG_SETUP_PROFILE = 4;
    private static final int DIALOG_USER_CANNOT_MANAGE = 5;
    private static final int DIALOG_CHOOSE_USER_TYPE = 6;
    private static final int DIALOG_NEED_LOCKSCREEN = 7;
    private static final int DIALOG_CONFIRM_EXIT_GUEST = 8;
    private static final int DIALOG_USER_PROFILE_EDITOR = 9;
    // [seungyeop.yeom][2014-05-09] add function of kids mode
    private static final int DIALOG_NEED_LOCKSCREEN_KIDS_MODE = 10;

    private static final int MESSAGE_UPDATE_LIST = 1;
    private static final int MESSAGE_SETUP_USER = 2;
    private static final int MESSAGE_CONFIG_USER = 3;

    private static final int USER_TYPE_USER = 1;
    private static final int USER_TYPE_RESTRICTED_PROFILE = 2;

    // [seungyeop.yeom][2014-05-09] add function of kids mode
    private static final int USER_TYPE_KIDS_MODE = 3;

    private static final int REQUEST_CHOOSE_LOCK = 10;

    // [seungyeop.yeom][2014-05-09] add function of kids mode
    private static final int REQUEST_CHOOSE_LOCK_KIDS_MODE = 11;

    private static final String KEY_ADD_USER_LONG_MESSAGE_DISPLAYED =
            "key_add_user_long_message_displayed";

    // [START][seungyeop.yeom][2014-10-13] add preference for function of add users device lock
    private static final String KEY_ADD_ON_LOCK_SCREEN = "add_on_lock_screen";
    private CheckBoxPreference mAddOnLockScreen;
    // [END][seungyeop.yeom][2014-10-13] add preference for function of add users device lock

    // [START][2015-02-16][seungyeop.yeom] Create value for Search function
    private boolean mIsFirst = false;
    private String mSearch_result;
    private boolean mCanSearchUse = true;
    public static final List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    // [END][2015-02-16][seungyeop.yeom] Create value for Search function

    static final int[] USER_DRAWABLES = {
            R.drawable.img_avatar_default_1,
            R.drawable.img_avatar_default_2,
            R.drawable.img_avatar_default_3,
            R.drawable.img_avatar_default_4,
            R.drawable.img_avatar_default_5,
            R.drawable.img_avatar_default_6,
            R.drawable.img_avatar_default_7,
            R.drawable.img_avatar_default_8
    };

    private static final String KEY_TITLE = "title";
    private static final String KEY_SUMMARY = "summary";

    private PreferenceGroup mUserListCategory;
    private Preference mMePreference;
    private SelectableEditTextPreference mNicknamePreference;
    private Preference mAddUser;
    private int mRemovingUserId = -1;
    private int mAddedUserId = 0;
    private boolean mAddingUser;
    private boolean mEnabled = true;
    private boolean mCanAddRestrictedProfile = true;

    // [seungyeop.yeom][2014-02-21]
    private boolean mUserAddStop;

    private final Object mUserLock = new Object();
    private UserManager mUserManager;
    private SparseArray<Bitmap> mUserIcons = new SparseArray<Bitmap>();
    private boolean mIsOwner = UserHandle.myUserId() == UserHandle.USER_OWNER;
    private boolean mIsGuest;

    private EditUserInfoController mEditUserInfoController =
            new EditUserInfoController();

    // A place to cache the generated default avatar
    private Drawable mDefaultIconDrawable;

    // [seungyeop.yeom][2014-03-07] modify issue of getActivity == null
    private Context mContext;

    // [2014-10-14][seungyeop.yeom] add button style for phone (command button)
    private Button mAddUserButton;
    private LinearLayout mAddButtonlayout;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_UPDATE_LIST:
                updateUserList();
                break;
            case MESSAGE_SETUP_USER:
                onUserCreated(msg.arg1);
                break;
            case MESSAGE_CONFIG_USER:
                onManageUserClicked(msg.arg1, true);
                break;
            default:
                break;
            }
        }
    };

    private BroadcastReceiver mUserChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_USER_REMOVED)) {
                mRemovingUserId = -1;
            } else if (intent.getAction().equals(Intent.ACTION_USER_INFO_CHANGED)) {
                int userHandle = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, -1);
                if (userHandle != -1) {
                    mUserIcons.remove(userHandle);
                }
            }
            mHandler.sendEmptyMessage(MESSAGE_UPDATE_LIST);
        }
    };

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Log.d("YSY", "onCreate, icicle : " + icicle);

        if (icicle != null) {
            // [2015-02-16][seungyeop.yeom] support perform click for Search function
            mCanSearchUse = false;

            if (icicle.containsKey(SAVE_ADDING_USER)) {
                mAddedUserId = icicle.getInt(SAVE_ADDING_USER);
            }
            if (icicle.containsKey(SAVE_REMOVING_USER)) {
                mRemovingUserId = icicle.getInt(SAVE_REMOVING_USER);
            }
            mEditUserInfoController.onRestoreInstanceState(icicle);
        }

        final Context context = getActivity();
        mUserManager = (UserManager)getActivity().getSystemService(Context.USER_SERVICE);
        boolean hasMultipleUsers = mUserManager.getUserCount() > 1;
        if ((!UserManager.supportsMultipleUsers() && !hasMultipleUsers)
                || Utils.isMonkeyRunning()) {
            mEnabled = false;
            return;
        }

        // [2015-02-16][seungyeop.yeom] support perform click for Search
        mIsFirst = true;

        final int myUserId = UserHandle.myUserId();
        mIsGuest = mUserManager.getUserInfo(myUserId).isGuest();

        addPreferencesFromResource(R.xml.user_settings);
        mUserListCategory = (PreferenceGroup)findPreference(KEY_USER_LIST);

        // [START][seungyeop.yeom][2014-10-13] add preference for function of add users device lock
        mAddOnLockScreen = (CheckBoxPreference)findPreference(KEY_ADD_ON_LOCK_SCREEN);
        if (mIsOwner && !mUserManager.hasUserRestriction(UserManager.DISALLOW_ADD_USER)) {
            getPreferenceScreen().addPreference(mAddOnLockScreen);
        } else {
            getPreferenceScreen().removePreference(mAddOnLockScreen);
        }

        if (mAddOnLockScreen != null) {
            mAddOnLockScreen.setChecked(Settings.Global.getInt(getContentResolver(),
                    Settings.Global.ADD_USERS_WHEN_LOCKED, 0) == 1);
        }
        // [END][seungyeop.yeom][2014-10-13] add preference for function of add users device lock

        // [START][seungyeop.yeom] modify settings icon of Lock screen function for ATT
        mMePreference = new UserPreference(context, null /* attrs */, myUserId,
                null /* settings icon handler */,
                null /* delete icon handler */);

        /* mMePreference = new UserPreference(getActivity(), null, UserHandle.myUserId(),
                mUserManager.isLinkedUser() ? null : this, null); */
        // [END]

        mMePreference.setKey(KEY_USER_ME);
        mMePreference.setOnPreferenceClickListener(this);
        if (mIsOwner) {
            mMePreference.setSummary(R.string.user_owner);
        }
        mAddUser = findPreference(KEY_ADD_USER);
        mAddUser.setOnPreferenceClickListener(this);

        if (isAddusersButton() == false) {
            Log.d("YB", "remove add user button");
            removePreference(KEY_ADD_USER);
        } else {
            Log.d("YB", "not remove add user button");
            mAddUser.setOnPreferenceClickListener(this);
            DevicePolicyManager dpm = (DevicePolicyManager)context.getSystemService(
                    Context.DEVICE_POLICY_SERVICE);
            // No restricted profiles for tablets with a device owner, or phones.
            if (dpm.getDeviceOwner() != null || Utils.isVoiceCapable(context)) {
                Log.d("YB", "dpm.getDeviceOwner() != null || Utils.isVoiceCapable(context)");
                mCanAddRestrictedProfile = false;
                mAddUser.setTitle(R.string.user_add_user_menu);
            }
        }
        // [seungyeop.yeom][2014-02-21]
        mUserAddStop = true;

        loadProfile();
        setHasOptionsMenu(true);
        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_REMOVED);
        filter.addAction(Intent.ACTION_USER_INFO_CHANGED);
        context.registerReceiverAsUser(mUserChangeReceiver, UserHandle.ALL, filter, null,
                mHandler);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-310]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            com.android.settings.MDMSettingsAdapter.getInstance()
                    .addMultiUserPolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);

            if (com.lge.cappuccino.Mdm.isSupportMultiUser()
                    && com.android.settings.MDMSettingsAdapter.
                            getInstance().checkAllowMultiUser() == false) {
                if (mAddUser != null) {
                    mAddUser.setEnabled(false);    
                }
            }
        }
        // LGMDM_END
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_add_button, null);
        mAddButtonlayout = (LinearLayout)view.findViewById(R.id.add_button_layout);
        mAddUserButton = (Button)view.findViewById(R.id.preview_button);
        mAddUserButton.setText(R.string.user_add_user_menu);

        // [START][2014-10-14][seungyeop.yeom] delete button for phone or tablet
        if (Utils.supportSplitView(getActivity()) == true /* table */) {
            // tablet -> pre
            mAddButtonlayout.setVisibility(View.GONE);
        } else if (Utils.supportSplitView(getActivity()) == false /* phone */) {
            removePreference(KEY_ADD_USER);
        }
        // [END][2014-10-14][seungyeop.yeom] delete button for phone or tablet

        if (isAddusersButton() == false) {
            Log.d("YB", "remove add user button");
            mAddButtonlayout.setVisibility(View.GONE);
        }

        mAddUserButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // If we allow both types, show a picker, otherwise directly go to
                // flow for full user.
                Log.d("YB", "AddUserButton");
                if (mCanAddRestrictedProfile) {
                    Log.d("YB", "AddUserButton, mCanAddRestrictedProfile");
                    showDialog(DIALOG_CHOOSE_USER_TYPE);
                } else {
                    Log.d("YB", "AddUserButton, not mCanAddRestrictedProfile");
                    onAddUserClicked(USER_TYPE_USER);
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mEnabled) {
            return;
        }
        loadProfile();
        updateUserList();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-310]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM == false
                || com.lge.cappuccino.Mdm.isSupportMultiUser() == false
                || com.android.settings.MDMSettingsAdapter.getInstance().checkAllowMultiUser()) {
            removePreference("mdm_disallow_multiuser_summary");
        }
        // LGMDM_END

        // [START][2015-02-16][seungyeop.yeom] support perform click for Search function
        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent().getBooleanExtra(
                "perform", false);
        Log.d("YSY", "onResume(), mSearch_result :" + mSearch_result);
        Log.d("YSY", "onResume(), checkPerfrom :" + checkPerfrom);
        Log.d("YSY", "onResume(), mIsFirst :" + mIsFirst);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom && mCanSearchUse) {
            startResult();
            mIsFirst = false;
        }
        // [END][2015-02-16][seungyeop.yeom] support perform click for Search function
    }

    /*
     * Author : seungyeop.yeom
     * Type : startResult() method
     * Date : 2015-02-16
     * Brief : perform click for search function
     */
    private void startResult() {
        Log.d("YSY", "startResult()");
        boolean isAddusers = true;
        if (isAddusersButton() == false
                || Utils.supportSplitView(getActivity()) == false /* phone */) {
            Log.d("YB", "remove add user button");
            isAddusers = false;
        }

        boolean moreUsers = mUserManager.canAddMoreUsers();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-310]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && com.lge.cappuccino.Mdm.isSupportMultiUser()) {
            if (com.android.settings.MDMSettingsAdapter.getInstance()
                    .checkAllowMultiUser() == false) {
                moreUsers = false;
            }
        }
        // LGMDM_END

        if (mSearch_result.equals(KEY_ADD_USER)) {
            if (isAddusers == true && moreUsers == true) {
                mAddUser.performClick(getPreferenceScreen());
            }
        } else if (mSearch_result.equals(KEY_ADD_ON_LOCK_SCREEN)) {
            mAddOnLockScreen.performClick(getPreferenceScreen());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!mEnabled) {
            return;
        }
        getActivity().unregisterReceiver(mUserChangeReceiver);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-310]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mEditUserInfoController.onSaveInstanceState(outState);
        outState.putInt(SAVE_ADDING_USER, mAddedUserId);
        outState.putInt(SAVE_REMOVING_USER, mRemovingUserId);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        mEditUserInfoController.startingActivityForResult();
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        int pos = 0;
        UserManager um = (UserManager)getActivity().getSystemService(Context.USER_SERVICE);

        // [seungyeop.yeom][2014-05-09] add function of kids mode
        UserInfo userinfo = um.getUserInfo(UserHandle.myUserId());
        Log.d("yeom", "multi user menu : " + UserHandle.myUserId());

        if (!mIsOwner && !um.hasUserRestriction(UserManager.DISALLOW_REMOVE_USER)) {
            Log.d("YB", "!mIsOwner && !um.hasUserRestriction(UserManager.DISALLOW_REMOVE_USER)");
            // [seungyeop.yeom][2014-05-09] add function of kids mode
            if ((userinfo.flags & UserInfoConstants.FLAG_KIDS) != UserInfoConstants.FLAG_KIDS) {
                Log.d("yeom", "multi user menu, not kids mode");
                // [START][2014-10-21][seungyeop.yeom] modify menu label for other user and guest
                MenuItem removeThisUser;
                if (!mIsGuest) {
                    // other users (no owner)
                    removeThisUser = menu.add(0, MENU_REMOVE_USER, pos++,
                            getResources().getString(R.string.user_delete_user_description));
                } else {
                    // only guest (no owner)
                    removeThisUser = menu.add(0, MENU_REMOVE_USER, pos++,
                            getResources().getString(R.string.user_exit_guest_title_ex));
                }
                // [END][2014-10-21][seungyeop.yeom] modify menu label for other user and guest

                removeThisUser.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == MENU_REMOVE_USER) {
            onRemoveUserClicked(UserHandle.myUserId());
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mAddOnLockScreen) {
            // [seungyeop.yeom][2014-10-13] add preference for function of add users device lock
            Log.d("YB", "onPreferenceTreeClick");
            final boolean isChecked = mAddOnLockScreen.isChecked();
            Settings.Global.putInt(getContentResolver(), Settings.Global.ADD_USERS_WHEN_LOCKED,
                    isChecked ? 1 : 0);
        } else {
            return false;
        }

        return true;
    }

    private void loadProfile() {
        Log.d("YB", "loadProfile()");
        if (mIsGuest) {
            Log.d("YB", "mIsGuest, loadProfile()");
            // No need to load profile information
            mMePreference.setIcon(getEncircledDefaultIcon());
            mMePreference.setTitle(R.string.user_guest);
            return;
        }
        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPostExecute(String result) {
                finishLoadProfile(result);
            }

            @Override
            protected String doInBackground(Void... values) {
                UserInfo user = mUserManager.getUserInfo(UserHandle.myUserId());
                if (user.iconPath == null || user.iconPath.equals("")) {
                    assignProfilePhoto(user);
                }
                return user.name;
            }
        }
                .execute();
    }

    private void finishLoadProfile(String profileName) {
        if (getActivity() == null) {
            return;
        }
        mMePreference.setTitle(profileName);
        int myUserId = UserHandle.myUserId();
        Bitmap b = mUserManager.getUserIcon(myUserId);
        if (b != null) {
            mMePreference.setIcon(encircle(b));
            mUserIcons.put(myUserId, b);
        }
    }

    private boolean hasLockscreenSecurity() {
        LockPatternUtilsEx lpu = new LockPatternUtilsEx(getActivity());
        return lpu.isLockPasswordEnabled() || lpu.isLockPatternEnabled()
                || lpu.isLockKnockOnEnabled();
    }

    private void launchChooseLockscreen(int userType) {
        Intent chooseLockIntent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
        chooseLockIntent.putExtra(ChooseLockGeneric.ChooseLockGenericFragment.MINIMUM_QUALITY_KEY,
                DevicePolicyManagerConstants.PASSWORD_QUALITY_KNOCK_ON);

        if (userType == USER_TYPE_RESTRICTED_PROFILE) {
            startActivityForResult(chooseLockIntent, REQUEST_CHOOSE_LOCK);
        } else if (userType == USER_TYPE_KIDS_MODE) {
            startActivityForResult(chooseLockIntent, REQUEST_CHOOSE_LOCK_KIDS_MODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHOOSE_LOCK) {
            if (resultCode != Activity.RESULT_CANCELED && hasLockscreenSecurity()) {
                addUserNow(USER_TYPE_RESTRICTED_PROFILE);
            }
        } else if (requestCode == REQUEST_CHOOSE_LOCK_KIDS_MODE) {
            if (resultCode != Activity.RESULT_CANCELED && hasLockscreenSecurity()) {
                addUserNow(USER_TYPE_KIDS_MODE);
            }
        } else {
            mEditUserInfoController.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onAddUserClicked(int userType) {
        synchronized (mUserLock) {
            if (mRemovingUserId == -1 && !mAddingUser) {
                switch (userType) {
                case USER_TYPE_USER:
                    Log.d("YB", "onAddUserClicked, USER_TYPE_USER");
                    showDialog(DIALOG_ADD_USER);
                    break;
                case USER_TYPE_RESTRICTED_PROFILE:
                    Log.d("YB",
                            "onAddUserClicked, USER_TYPE_RESTRICTED_PROFILE");
                    if (hasLockscreenSecurity()) {
                        addUserNow(USER_TYPE_RESTRICTED_PROFILE);
                    } else {
                        showDialog(DIALOG_NEED_LOCKSCREEN);
                    }

                    break;
                case USER_TYPE_KIDS_MODE:
                    // [seungyeop.yeom][2014-05-09] add function of kids mode
                    Log.d("YB", "onAddUserClicked, USER_TYPE_KIDS_MODE");
                    if (hasLockscreenSecurity()) {
                        addUserNow(USER_TYPE_KIDS_MODE);
                    } else {
                        showDialog(DIALOG_NEED_LOCKSCREEN_KIDS_MODE);
                    }

                    break;
                default:
                    break;
                }
            }
        }
    }

    private void onRemoveUserClicked(int userId) {
        Log.d("YB", "onRemoveUserClicked");

        // [START][seungyeop.yeom][2014-11-03] restrict function of delete users in call
        try {
            ITelephony telephonyService = ITelephony.Stub.asInterface(ServiceManager
                    .checkService(Context.TELEPHONY_SERVICE));
            if (telephonyService != null) {
                if (telephonyService.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                    Log.d("YB", "onRemoveUserClicked, restricted user remove for in call");
                    Toast.makeText(getActivity(),
                            R.string.multi_users_delete_restricted_during_call,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (RemoteException ex) {
            Log.w("YB", "RemoteException from getPhoneInterface()", ex);
        }
        // [END][seungyeop.yeom][2014-11-03] restrict function of delete users in call

        synchronized (mUserLock) {
            if (mRemovingUserId == -1 && !mAddingUser) {
                mRemovingUserId = userId;
                showDialog(DIALOG_CONFIRM_REMOVE);
            }
        }
    }

    private UserInfo createLimitedUser(int userType) {
        // [START][seungyeop.yeom][2014-05-09] add function of kids mode
        UserInfo newUserInfo = null;
        if (userType == USER_TYPE_RESTRICTED_PROFILE) {
            Log.d("YSY", "createLimitedUser, USER_TYPE_RESTRICTED_PROFILE");
            newUserInfo = mUserManager.createSecondaryUser(
                    getResources().getString(R.string.user_new_restricted_user_name),
                    UserInfo.FLAG_RESTRICTED);
        } else if (userType == USER_TYPE_KIDS_MODE) {
            Log.d("YSY", "createLimitedUser, USER_TYPE_KIDS_MODE");
            newUserInfo = mUserManager.createSecondaryUser(
                    getResources().getString(R.string.user_new_kids_name),
                    UserInfoConstants.FLAG_KIDS);
        }
        // [END]

        if (newUserInfo == null) {
            mUserAddStop = false;
            Log.d("YSY", "UserInfo (restricted) == null");
            return newUserInfo;
        }

        Log.d("YSY", "UserInfo (restricted) != null");
        mUserAddStop = true;
        int userId = newUserInfo.id;
        UserHandle user = new UserHandle(userId);
        mUserManager.setUserRestriction(UserManager.DISALLOW_MODIFY_ACCOUNTS, true, user);
        // Change the setting before applying the DISALLOW_SHARE_LOCATION restriction, otherwise
        // the putIntForUser() will fail.
        Secure.putIntForUser(getContentResolver(),
                Secure.LOCATION_MODE, Secure.LOCATION_MODE_OFF, userId);
        mUserManager.setUserRestriction(UserManager.DISALLOW_SHARE_LOCATION, true, user);
        assignDefaultPhoto(newUserInfo);
        // Add shared accounts
        AccountManager am = AccountManager.get(getActivity());
        Account[] accounts = am.getAccounts();
        if (accounts != null) {
            for (Account account : accounts) {
                am.addSharedAccount(account, user);
            }
        }
        return newUserInfo;
    }

    private UserInfo createTrustedUser() {
        Log.d("YB", "createTrustedUser()");
        UserInfo newUserInfo = mUserManager.createSecondaryUser(
                getResources().getString(R.string.user_new_user_name), 0);
        if (newUserInfo == null) {
            Log.d("YB", "createTrustedUser(), newUserInfo == null");
            mUserAddStop = false;
            Log.d("YSY", "UserInfo == null");
            return newUserInfo;
        } else {
            Log.d("YB", "createTrustedUser(), not newUserInfo == null");
            mUserAddStop = true;
            Log.d("YSY", "UserInfo != null");
            assignDefaultPhoto(newUserInfo);
        }
        return newUserInfo;
    }

    private void onManageUserClicked(int userId, boolean newUser) {
        if (userId == UserPreference.USERID_GUEST_DEFAULTS) {
            Log.d("YB", "onManageUserClicked, userId == UserPreference.USERID_GUEST_DEFAULTS");
            Bundle extras = new Bundle();
            extras.putBoolean(UserDetailsSettings.EXTRA_USER_GUEST, true);
            ((PreferenceActivity)getActivity()).startPreferencePanel(
                    UserDetailsSettings.class.getName(),
                    extras, R.string.user_guest, null, null, 0);
            return;
        }
        UserInfo info = mUserManager.getUserInfo(userId);
        if (info.isRestricted() && mIsOwner) {
            Log.d("YB", "onManageUserClicked, info.isRestricted() && mIsOwner");
            Bundle extras = new Bundle();
            extras.putInt(RestrictedProfileSettings.EXTRA_USER_ID, userId);
            extras.putBoolean(RestrictedProfileSettings.EXTRA_NEW_USER, newUser);
            ((PreferenceActivity)getActivity()).startPreferencePanel(
                    RestrictedProfileSettings.class.getName(),
                    extras, R.string.user_restrictions_title_ex, null,
                    null, 0);
        } /* else if (info.id == UserHandle.myUserId()) {
            // delete function for LG multi user scenario
        
            // [2014-05-21][seungyeop.yeom] delete function of profile info
            // Jump to owner info panel
            Log.d("YB", "onManageUserClicked, info.id == UserHandle.myUserId()");
            Bundle extras = new Bundle();
            if (!info.isRestricted()) {
                extras.putBoolean(OwnerInfoSettings.EXTRA_SHOW_NICKNAME, true);
            }
            int titleResId = info.id == UserHandle.USER_OWNER ? R.string.owner_info_settings_title
                    : (info.isRestricted() ? R.string.profile_info_settings_title
                            : R.string.user_info_settings_title);
            ((PreferenceActivity)getActivity()).startPreferencePanel(
                    OwnerInfoSettings.class.getName(),
                    extras, titleResId, null, null, 0);
        }*/ else if (mIsOwner) {
            // [2014-10-03][seungyeop.yeom] Modified code from the Native
            // No title res id -1 -> 0
            // add extras string of title
            Log.d("YB", "onManageUserClicked, mIsOwner");
            Bundle extras = new Bundle();
            extras.putInt(UserDetailsSettings.EXTRA_USER_ID, userId);
            extras.putString("title", info.name);
            ((PreferenceActivity)getActivity()).startPreferencePanel(
                    UserDetailsSettings.class.getName(),
                    extras,
                    0, /* No title res id */
                    info.name, /* title */
                    null, /* resultTo */
                    0 /* resultRequestCode */);
        }
    }

    private void onUserCreated(int userId) {
        Log.d("YB", "onUserCreated");
        mAddedUserId = userId;
        if (mUserManager.getUserInfo(userId).isRestricted()) {
            Log.d("YB", "onUserCreated, mUserManager.getUserInfo(userId).isRestricted()");
            showDialog(DIALOG_SETUP_PROFILE);
        } else {
            Log.d("YB", "onUserCreated, not mUserManager.getUserInfo(userId).isRestricted()");
            showDialog(DIALOG_SETUP_USER);
        }
    }

    @Override
    public void onDialogShowing() {
        super.onDialogShowing();

        setOnDismissListener(this);
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        Context context = getActivity();
        if (context == null) {
            return null;
        }
        switch (dialogId) {
        case DIALOG_CONFIRM_REMOVE: {
            Log.d("YB", "DIALOG_CONFIRM_REMOVE");
            Dialog dlg =
                    Utils.createRemoveConfirmationDialog(getActivity(), mRemovingUserId,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    removeUserNow();
                                }
                            }
                            );
            return dlg;
        }
        case DIALOG_USER_CANNOT_MANAGE:
            Log.d("YB", "DIALOG_USER_CANNOT_MANAGE");
            return new AlertDialog.Builder(context)
                    .setMessage(R.string.user_cannot_manage_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        case DIALOG_ADD_USER: {
            Log.d("YB", "DIALOG_ADD_USER");
            final SharedPreferences preferences = getActivity().getPreferences(
                    Context.MODE_PRIVATE);
            final boolean longMessageDisplayed = preferences.getBoolean(
                    KEY_ADD_USER_LONG_MESSAGE_DISPLAYED, false);
            final int messageResId = longMessageDisplayed
                    ? R.string.user_add_user_message_short
                    : R.string.user_add_user_message_ex2;
            final int userType = dialogId == DIALOG_ADD_USER
                    ? USER_TYPE_USER : USER_TYPE_RESTRICTED_PROFILE;
            Dialog dlg = new AlertDialog.Builder(context)
                    .setTitle(R.string.user_add_user_menu)
                    .setMessage(messageResId)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d("YSY", "DIALOG_ADD_USER");
                                    addUserNow(userType);
                                    if (!longMessageDisplayed) {
                                        preferences.edit().putBoolean(
                                                KEY_ADD_USER_LONG_MESSAGE_DISPLAYED, true).apply();
                                    }
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            return dlg;
        }
        case DIALOG_SETUP_USER: {
            Log.d("YB", "DIALOG_SETUP_USER");
            Dialog dlg = new AlertDialog.Builder(context)
                    .setTitle(R.string.user_setup_dialog_title_ex3)
                    .setMessage(R.string.user_setup_dialog_message_ex2)
                    .setPositiveButton(R.string.user_setup_button_setup_now,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d("YSY", "DIALOG_SETUP_USER");
                                    switchUserNow(mAddedUserId);
                                }
                            })
                    .setNegativeButton(R.string.user_setup_button_setup_later, null)
                    .create();
            return dlg;
        }
        case DIALOG_SETUP_PROFILE: {
            Log.d("YB", "DIALOG_SETUP_PROFILE");
            Dialog dlg = new AlertDialog.Builder(context)
                    .setMessage(R.string.user_setup_profile_dialog_message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d("YSY", "DIALOG_SETUP_PROFILE");
                                    switchUserNow(mAddedUserId);
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            return dlg;
        }
        case DIALOG_CHOOSE_USER_TYPE: {
            Log.d("YB", "DIALOG_CHOOSE_USER_TYPE");
            List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> addUserItem = new HashMap<String, String>();
            addUserItem.put(KEY_TITLE, getString(R.string.user_add_user_item_title));
            addUserItem.put(KEY_SUMMARY, getString(R.string.user_add_user_item_summary));
            HashMap<String, String> addProfileItem = new HashMap<String, String>();
            addProfileItem.put(KEY_TITLE, getString(R.string.user_add_restricted_user_item_title));
            addProfileItem.put(KEY_SUMMARY, getString(R.string.user_add_profile_item_summary));
            HashMap<String, String> addKidsModeItem = new HashMap<String, String>();
            addKidsModeItem.put(KEY_TITLE, getString(R.string.user_kids_user_name));
            addKidsModeItem
                    .put(KEY_SUMMARY, getString(R.string.user_add_kids_mode_item_summary_ex));
            data.add(addUserItem);
            data.add(addProfileItem);
            // [seungyeop.yeom][2014-05-09] add function of kids mode
            if (Config.getFWConfigBool(getActivity(),
                    com.lge.R.bool.config_kidsmode_available,
                    "com.lge.R.bool.config_kidsmode_available") == true) {
                data.add(addKidsModeItem);
            }
            Dialog dlg = new AlertDialog.Builder(context)
                    .setTitle(R.string.user_add_user_type_title)
                    .setAdapter(new SimpleAdapter(context, data, R.layout.two_line_list_item,
                            new String[] { KEY_TITLE, KEY_SUMMARY },
                            new int[] { R.id.title, R.id.summary }),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // [START][seungyeop.yeom][2014-05-09] add function of kids mode
                                    Log.d("yeom", "DIALOG_CHOOSE_USER_TYPE : " + which);
                                    if (which == 0) {
                                        onAddUserClicked(USER_TYPE_USER);
                                    } else if (which == 1) {
                                        onAddUserClicked(USER_TYPE_RESTRICTED_PROFILE);
                                    } else if (which == 2) {
                                        onAddUserClicked(USER_TYPE_KIDS_MODE);
                                    }
                                    // [END]
                                }
                            })
                    .create();
            return dlg;
        }
        case DIALOG_NEED_LOCKSCREEN: {
            Log.d("YB", "DIALOG_NEED_LOCKSCREEN");
            Dialog dlg = new AlertDialog.Builder(context)
                    .setTitle(R.string.user_add_restricted_user_item_title)
                    .setMessage(R.string.user_need_lock_message_ex2)
                    .setPositiveButton(R.string.user_set_lock_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d("YSY", "DIALOG_NEED_LOCKSCREEN");
                                    launchChooseLockscreen(USER_TYPE_RESTRICTED_PROFILE);
                                }
                            })
                    .setNegativeButton(R.string.skip_label,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Auto-generated method stub
                                    addUserNow(USER_TYPE_RESTRICTED_PROFILE);
                                }
                            })
                    .create();
            return dlg;
        }
        case DIALOG_NEED_LOCKSCREEN_KIDS_MODE: {
            // [seungyeop.yeom][2014-05-09] add function of kids mode
            Log.d("YB", "DIALOG_NEED_LOCKSCREEN_KIDS_MODE");
            Dialog dlg = new AlertDialog.Builder(context)
                    .setTitle(R.string.user_kids_user_name)
                    .setMessage(R.string.user_need_lock_message_kids_mode)
                    .setPositiveButton(R.string.user_set_lock_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    launchChooseLockscreen(USER_TYPE_KIDS_MODE);
                                }
                            })
                    .setNegativeButton(R.string.skip_label,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Auto-generated method stub
                                    addUserNow(USER_TYPE_KIDS_MODE);
                                }
                            })
                    .create();
            return dlg;
        }
        case DIALOG_CONFIRM_EXIT_GUEST: {
            Log.d("YB", "DIALOG_CONFIRM_EXIT_GUEST");
            Dialog dlg = new AlertDialog.Builder(context)
                    .setTitle(R.string.user_exit_guest_confirm_title)
                    .setMessage(R.string.user_exit_guest_confirm_message)
                    .setPositiveButton(R.string.user_delete_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    exitGuest();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            return dlg;
        }
        case DIALOG_USER_PROFILE_EDITOR: {
            Log.d("YB", "DIALOG_USER_PROFILE_EDITOR");
            Dialog dlg = mEditUserInfoController.createDialog(
                    (Fragment)this,
                    mMePreference.getIcon(),
                    mMePreference.getTitle(),
                    R.string.user_name_settings_title,
                    this /* callback */,
                    android.os.Process.myUserHandle());
            return dlg;
        }
        default:
            return null;
        }
    }

    private void removeUserNow() {
        if (mRemovingUserId == UserHandle.myUserId()) {
            Log.d("YB", "mRemovingUserId == UserHandle.myUserId()");
            removeThisUser();
        } else {
            new Thread() {
                public void run() {
                    synchronized (mUserLock) {
                        Log.d("YB", "removeUserNow() : " + mRemovingUserId);
                        mUserManager.removeUser(mRemovingUserId);
                        mHandler.sendEmptyMessage(MESSAGE_UPDATE_LIST);
                    }
                }
            }
                    .start();
        }
    }

    private void removeThisUser() {
        try {
            Log.d("YB", "removeThisUser()");
            ActivityManagerNative.getDefault().switchUser(UserHandle.USER_OWNER);
            ((UserManager)getActivity().getSystemService(Context.USER_SERVICE))
                    .removeUser(UserHandle.myUserId());
        } catch (RemoteException re) {
            Log.e(TAG, "Unable to remove self user");
        }
    }

    private void addUserNow(final int userType) {
        synchronized (mUserLock) {
            Log.d("YB", "mUserAddStop : " + mUserAddStop);

            mUserAddStop = true;
            mAddingUser = true;
            //updateUserList();
            new Thread() {
                public void run() {
                    UserInfo user = null;
                    // Could take a few seconds
                    if (userType == USER_TYPE_USER) {
                        Log.d("YSY", "Trusted USER_TYPE_USER");
                        user = createTrustedUser();
                        Log.d("YSY", "trusted user state : " + user);
                    } else if (userType == USER_TYPE_RESTRICTED_PROFILE) {
                        Log.d("YSY", "Limited USER_TYPE");
                        user = createLimitedUser(USER_TYPE_RESTRICTED_PROFILE);
                        Log.d("YSY", "limited user state : " + user);
                    } else if (userType == USER_TYPE_KIDS_MODE) {
                        // [seungyeop.yeom][2014-05-09] add function of kids mode
                        Log.d("YSY", "Limited USER_TYPE_KIDS_MODE");
                        user = createLimitedUser(USER_TYPE_KIDS_MODE);
                        Log.d("YSY", "limited user state : " + user);
                    }

                    if (mUserAddStop) {
                        synchronized (mUserLock) {
                            mAddingUser = false;
                            if (userType == USER_TYPE_USER) {
                                mHandler.sendEmptyMessage(MESSAGE_UPDATE_LIST);
                                Log.d("YSY", "confirmed trusted user : " + user);
                                Log.d("YSY", "confirmed trusted user : " + user.id);
                                Log.d("YSY", "confirmed trusted user : " + user.serialNumber);
                                mHandler.sendMessage(mHandler.obtainMessage(
                                        MESSAGE_SETUP_USER, user.id, user.serialNumber));
                            } else {
                                mHandler.sendMessage(mHandler.obtainMessage(
                                        MESSAGE_CONFIG_USER, user.id, user.serialNumber));
                                Log.d("YSY", "confirmed limited user : " + user);
                                Log.d("YSY", "confirmed limited user : " + user.id);
                                Log.d("YSY", "confirmed limited user : " + user.serialNumber);
                            }
                        }
                    }

                    // [seungyeop.yeom][2013-09-13] toast popup
                    if (!mUserAddStop) {
                        Looper.prepare();
                        Toast.makeText(getActivity(), R.string.multi_users_deleting_toast,
                                Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }
                    .start();
        }
    }

    private void switchUserNow(int userId) {
        Log.d("YB", "switchUserNow");
        // [START][seungyeop.yeom][2014-10-13] restrict function of switch users in call
        try {
            ITelephony telephonyService = ITelephony.Stub.asInterface(ServiceManager
                    .checkService(Context.TELEPHONY_SERVICE));
            if (telephonyService != null) {
                if (telephonyService.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                    Log.d("YB", "switchUserNow, restricted user switch for in call");
                    Toast.makeText(getActivity(),
                            R.string.multi_users_switch_restricted_during_call,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (RemoteException ex) {
            Log.w("YB", "RemoteException from getPhoneInterface()", ex);
        }
        // [END][seungyeop.yeom][2014-10-13] restrict function of switch users in call

        try {
            ActivityManagerNative.getDefault().switchUser(userId);
        } catch (RemoteException re) {
            Log.w("YB", "RemoteException");
        }
    }

    /**
     * Erase the current user (guest) and switch to another user.
     */
    private void exitGuest() {
        // Just to be safe
        if (!mIsGuest) {
            return;
        }
        removeThisUser();
    }

    private void updateUserList() {
        if (getActivity() == null) {
            return;
        }
        List<UserInfo> users = mUserManager.getUsers(true);
        final Context context = getActivity();

        mUserListCategory.removeAll();
        mUserListCategory.setOrderingAsAdded(false);
        mUserListCategory.addPreference(mMePreference);

        final boolean voiceCapable = Utils.isVoiceCapable(context);
        final ArrayList<Integer> missingIcons = new ArrayList<Integer>();
        for (UserInfo user : users) {
            if (user.isManagedProfile()) {
                // Managed profiles appear under Accounts Settings instead
                continue;
            }
            Preference pref;
            if (user.id == UserHandle.myUserId()) {
                pref = mMePreference;
            } else if (user.isGuest()) {
                // Skip over Guest. We add generic Guest settings after this loop
                continue;
            } else {
                // With Telephony:
                //   Secondary user: Settings
                //   Guest: Settings
                //   Restricted Profile: There is no Restricted Profile
                // Without Telephony:
                //   Secondary user: Delete
                //   Guest: Nothing
                //   Restricted Profile: Settings
                final boolean showSettings = mIsOwner && (voiceCapable || user.isRestricted());
                final boolean showDelete = mIsOwner
                        && (!voiceCapable && !user.isRestricted() && !user.isGuest());

                Log.d("YB", "updateUserList(), showSettings : " + showSettings);
                Log.d("YB", "updateUserList(), showDelete : " + showDelete);

                pref = new UserPreference(context, null, user.id,
                        showSettings ? this : null,
                        showDelete ? this : null);
                pref.setOnPreferenceClickListener(this);
                pref.setKey("id=" + user.id);
                mUserListCategory.addPreference(pref);
                if (user.id == UserHandle.USER_OWNER) {
                    pref.setSummary(R.string.user_owner);
                }
                pref.setTitle(user.name);
            }
            if (!isInitialized(user)) {
                if (user.isRestricted()) {
                    // [seungyeop.yeom][2014-05-09] add function of kids mode
                    if ((user.flags & UserInfoConstants.FLAG_KIDS) == UserInfoConstants.FLAG_KIDS) {
                        Log.d("YSY", "Kids mode summary");
                        pref.setSummary(R.string.user_summary_kids_mode_not_set_up_ex);
                    } else {
                        Log.d("YSY", "Restricted profile summary");
                        pref.setSummary(R.string.user_summary_restricted_user_not_set_up);
                    }
                } else if (!user.isGuest()) {
                    pref.setSummary(R.string.user_summary_not_set_up_ex);
                }
            } else {
                if (user.isRestricted()) {
                    // [seungyeop.yeom][2014-05-09] add function of kids mode
                    if ((user.flags & UserInfoConstants.FLAG_KIDS) == UserInfoConstants.FLAG_KIDS) {
                        Log.d("YSY", "isInitialized Kids mode summary");
                        pref.setSummary(R.string.user_kids_user_name);
                    } else {
                        Log.d("YSY", "isInitialized Restricted profile summary");
                        pref.setSummary(R.string.user_add_restricted_user_item_title);
                    }
                } else if (!user.isGuest()) {
                    // [seungyeop.yeom][2014-10-11] add summary of User (Owner is not)
                    if (!(user.id == UserHandle.USER_OWNER)) {
                        pref.setSummary(R.string.user_add_user_item_title);
                    }
                }
            }

            if (user.iconPath != null) {
                if (mUserIcons.get(user.id) == null) {
                    // Icon not loaded yet, print a placeholder
                    missingIcons.add(user.id);
                    pref.setIcon(getEncircledDefaultIcon());
                } else {
                    setPhotoId(pref, user);
                }
            } else {
                // Icon not available yet, print a placeholder
                pref.setIcon(getEncircledDefaultIcon());
            }

            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-310]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                    && com.lge.cappuccino.Mdm.isSupportMultiUser()) {
                if (com.android.settings.MDMSettingsAdapter.getInstance().
                        checkAllowMultiUser() == false) {
                    if (pref != mMePreference) {
                        pref.setEnabled(false);
                    }
                }
            }
            // LGMDM_END
        }
        // Add a temporary entry for the user being created
        if (mAddingUser) {
            Preference pref = new UserPreference(getActivity(), null,
                    UserPreference.USERID_UNKNOWN,
                    null, null);
            pref.setEnabled(false);
            pref.setTitle(R.string.user_new_user_name);
            pref.setIcon(getEncircledDefaultIcon());
            mUserListCategory.addPreference(pref);
        }
        getActivity().invalidateOptionsMenu();

        boolean showGuestPreference = !mIsGuest;
        // If user has DISALLOW_ADD_USER don't allow creating a guest either.
        if (showGuestPreference && mUserManager.hasUserRestriction(UserManager.DISALLOW_ADD_USER)) {
            showGuestPreference = false;
            // If guest already exists, no user creation needed.
            for (UserInfo user : users) {
                if (user.isGuest()) {
                    showGuestPreference = true;
                    break;
                }
            }
        }
        boolean enableAddUser = true;
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            IDeviceManager3LM dm =
                IDeviceManager3LM.Stub.asInterface(
                    ServiceManager.getService(Context.DEVICE_MANAGER_3LM_SERVICE));
            try {
                enableAddUser = dm.getMultiUserEnabled();
                } catch (RemoteException e) {
                // Should never happen
                Log.e(TAG, "3LM Exception");
            }
        }

        // [START][seungyeop.yeom][2015-03-05] Delete of Guest user menu, 
        // depending on the requirements of the CFW (agree with Google)
        if (voiceCapable == true /* phone */) {
            showGuestPreference = false;
        } else if (voiceCapable == false /* tablet */){
            showGuestPreference = true;
        }
        // [END][seungyeop.yeom][2015-03-05] Delete of Guest user menu, 
        // depending on the requirements of the CFW (agree with Google)

        if (showGuestPreference && enableAddUser) {
            Log.d("YB", "updateUserList(), showGuestPreference");

            // Add a virtual Guest user for guest defaults
            Preference pref = new UserPreference(getActivity(), null,
                    UserPreference.USERID_GUEST_DEFAULTS,
                    mIsOwner && voiceCapable ? this : null /* settings icon handler */,
                    null /* delete icon handler */);
            pref.setTitle(R.string.user_guest);
            pref.setIcon(getEncircledDefaultIcon());
            pref.setOnPreferenceClickListener(this);
            mUserListCategory.addPreference(pref);
        }

        // Load the icons
        if (missingIcons.size() > 0) {
            loadIconsAsync(missingIcons);
        }
        boolean moreUsers = mUserManager.canAddMoreUsers();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-310]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM &&
                com.lge.cappuccino.Mdm.isSupportMultiUser()) {
            if (com.android.settings.MDMSettingsAdapter.getInstance().
                    checkAllowMultiUser() == false) {
                moreUsers = false;
            }
        }
        // LGMDM_END
        if (mAddUser != null) {
            mAddUser.setEnabled(moreUsers);
        }

        // [2014-10-14][seungyeop.yeom] add button style for phone (command button)
        if (mAddUserButton != null) {
            mAddUserButton.setEnabled(moreUsers);
        }

    }

    private void loadIconsAsync(List<Integer> missingIcons) {
        new AsyncTask<List<Integer>, Void, Void>() {
            @Override
            protected void onPostExecute(Void result) {
                updateUserList();
            }

            @Override
            protected Void doInBackground(List<Integer>... values) {
                for (int userId : values[0]) {
                    Bitmap bitmap = mUserManager.getUserIcon(userId);
                    if (bitmap == null) {
                        bitmap = UserIcons.convertToBitmap(UserIcons.getDefaultUserIcon(userId,
                                /* light= */false));
                    }
                    mUserIcons.append(userId, bitmap);
                }
                return null;
            }
        }
                .execute(missingIcons);
    }

    private void assignProfilePhoto(final UserInfo user) {
        if (!Utils.copyMeProfilePhoto(getActivity(), user)) {
            Log.d("YSY", "assignProfilePhoto(final UserInfo user)");
            assignDefaultPhoto(user);
        }
    }

    private void assignDefaultPhoto(UserInfo user) {
        Bitmap bitmap = UserIcons.convertToBitmap(UserIcons.getDefaultUserIcon(user.id,
                /* light= */false));
        mUserManager.setUserIcon(user.id, bitmap);
    }

    private Drawable getEncircledDefaultIcon() {
        if (mDefaultIconDrawable == null) {
            mDefaultIconDrawable = encircle(UserIcons.convertToBitmap(
                    UserIcons.getDefaultUserIcon(UserHandle.USER_NULL, /* light= */false)));
        }
        return mDefaultIconDrawable;
    }

    private void setPhotoId(Preference pref, UserInfo user) {
        Bitmap bitmap = mUserIcons.get(user.id);
        if (bitmap != null) {
            pref.setIcon(encircle(bitmap));
        }
    }

    private void setUserName(String name) {
        mUserManager.setUserName(UserHandle.myUserId(), name);
        mNicknamePreference.setSummary(name);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean onPreferenceClick(Preference pref) {
        if (pref == mMePreference) {
            if (mIsGuest) {
                // [2014-10-21] remove function of delete guest for UX Scenario
                Log.d("YB", "onPreferenceClick, mIsGuest");
                // showDialog(DIALOG_CONFIRM_EXIT_GUEST);
                return true;
            }

            // If this is a limited user, launch the user info settings instead of profile editor
            if (mUserManager.isLinkedUser()) {
                onManageUserClicked(UserHandle.myUserId(), false);
            } else {
                showDialog(DIALOG_USER_PROFILE_EDITOR);
            }
        } else if (pref instanceof UserPreference) {
            int userId = ((UserPreference)pref).getUserId();
            if (userId == UserPreference.USERID_GUEST_DEFAULTS) {
                Log.d("YB", "userId == UserPreference.USERID_GUEST_DEFAULTS");
                createAndSwitchToGuestUser();
            } else {
                // Get the latest status of the user
                Log.d("YB", "not userId == UserPreference.USERID_GUEST_DEFAULTS");
                UserInfo user = mUserManager.getUserInfo(userId);
                if (!isInitialized(user)) {
                    mHandler.sendMessage(mHandler.obtainMessage(
                            MESSAGE_SETUP_USER, user.id, user.serialNumber));
                } else {
                    switchUserNow(userId);
                }
            }
        } else if (pref == mAddUser) {
            // If we allow both types, show a picker, otherwise directly go to
            // flow for full user.
            Log.d("YB", "pref == mAddUser");
            if (mCanAddRestrictedProfile) {
                Log.d("YB", "pref == mAddUser, mCanAddRestrictedProfile");
                showDialog(DIALOG_CHOOSE_USER_TYPE);
            } else {
                Log.d("YB", "pref == mAddUser, not mCanAddRestrictedProfile");
                onAddUserClicked(USER_TYPE_USER);
            }
        }
        return false;
    }

    private void createAndSwitchToGuestUser() {
        Log.d("YB", "createAndSwitchToGuestUser");
        List<UserInfo> users = mUserManager.getUsers();
        for (UserInfo user : users) {
            if (user.isGuest()) {
                switchUserNow(user.id);
                return;
            }
        }
        // No guest user. Create one, if there's no restriction.
        // If it is not the primary user, then adding users from lockscreen must be enabled
        if (mUserManager.hasUserRestriction(UserManager.DISALLOW_ADD_USER)
                || (!mIsOwner && Settings.Global.getInt(getContentResolver(),
                        Settings.Global.ADD_USERS_WHEN_LOCKED, 0) != 1)) {
            Log.i(TAG, "Blocking guest creation because it is restricted");
            return;
        }
        UserInfo guestUser = mUserManager.createGuest(getActivity(),
                getResources().getString(R.string.user_guest));
        if (guestUser != null) {
            switchUserNow(guestUser.id);
        }
    }

    private boolean isInitialized(UserInfo user) {
        return (user.flags & UserInfo.FLAG_INITIALIZED) != 0;
    }

    private Drawable encircle(Bitmap icon) {
        Drawable circled = CircleFramedDrawable.getInstance(mContext, icon);
        return circled;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof UserPreference) {
            int userId = ((UserPreference)v.getTag()).getUserId();
            switch (v.getId()) {
            case UserPreference.DELETE_ID:
                Log.d("YB", "UserPreference.DELETE_ID");
                onRemoveUserClicked(userId);
                break;
            case UserPreference.SETTINGS_ID:
                Log.d("YB", "UserPreference.SETTINGS_ID");
                onManageUserClicked(userId, false);
                break;
            default:
                break;
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        synchronized (mUserLock) {
            mAddingUser = false;
            mRemovingUserId = -1;
            updateUserList();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNicknamePreference) {
            String value = (String)newValue;
            if (preference == mNicknamePreference && value != null
                    && value.length() > 0) {
                setUserName(value);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_users;
    }

    @Override
    public void onPhotoChanged(Drawable photo) {
        mMePreference.setIcon(photo);
    }

    @Override
    public void onLabelChanged(CharSequence label) {
        mMePreference.setTitle(label);
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-310]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.android.settings.MDMSettingsAdapter.getInstance()
                        .receiveMultiUserPolicyChangeIntent(intent)) {
                    android.app.Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                }
            }
        }
    };
    // LGMDM_END

    private boolean isAddusersButton() {
        boolean isAddusers = true;
        boolean enableAddUser = true;
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            IDeviceManager3LM dm = IDeviceManager3LM.Stub
                    .asInterface(ServiceManager
                            .getService(Context.DEVICE_MANAGER_3LM_SERVICE));
            try {
                enableAddUser = dm.getMultiUserEnabled();
            } catch (RemoteException e) {
                // Should never happen
                Log.e(TAG, "3LM Exception");
            }
        }
        if (!enableAddUser
                || !mIsOwner
                || UserManager.getMaxSupportedUsers() < 2
                || !UserManager.supportsMultipleUsers()
                || mUserManager
                        .hasUserRestriction(UserManager.DISALLOW_ADD_USER)) {
            Log.d("YB", "remove add user button");
            isAddusers = false;
        }
        return isAddusers;
    }

    /*
     * Author : seungyeop.yeom
     * Type : Search object
     * Date : 2015-02-16
     * Brief : Create of Users search
     */
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        private int isMultiUsers;
        private boolean hasMultipleUsers;

        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }

            isMultiUsers = 1;
            hasMultipleUsers = ((UserManager)context
                    .getSystemService(Context.USER_SERVICE)).getUserCount() > 1;

            if (!UserHandle.MU_ENABLED
                    || (!UserManager.supportsMultipleUsers() && !hasMultipleUsers)
                    || Utils.isMonkeyRunning()) {
                isMultiUsers = 0;
            } else {
                isMultiUsers = 1;
            }

            // Search Multi-user Main
            setSearchIndexData(context, "user_settings",
                    context.getString(R.string.user_list_title), "main", null,
                    null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$UserSettingsActivity",
                    "com.android.settings", 1, null, null, null, isMultiUsers,
                    0);

            // Search Add on lock screen
            setSearchIndexData(
                    context,
                    KEY_ADD_ON_LOCK_SCREEN,
                    context.getString(R.string.user_add_on_lockscreen_menu_ex2),
                    context.getString(R.string.user_list_title), null, null,
                    "android.intent.action.MAIN",
                    "com.android.settings.Settings$UserSettingsActivity",
                    "com.android.settings", 1, "CheckBox", "Global",
                    "add_users_when_locked", isMultiUsers, 0);

            // Search Add user
            setSearchTvApps(context);
            return mResult;
        }

        private void setSearchTvApps(Context context) {
            int isAddusersButton = 1;

            if (isMultiUsers == 1) {
                if (Utils.supportSplitView(context) == true /* tablet */) {
                    isAddusersButton = 1;
                } else if (Utils.supportSplitView(context) == false /* phone */) {
                    isAddusersButton = 0;
                }
            } else if (isMultiUsers == 0) {
                isAddusersButton = 0;
            }

            setSearchIndexData(context, KEY_ADD_USER,
                    context.getString(R.string.user_add_user_menu),
                    context.getString(R.string.user_list_title), null, null,
                    "android.intent.action.MAIN",
                    "com.android.settings.Settings$UserSettingsActivity",
                    "com.android.settings", 1, null, null, null,
                    isAddusersButton, 0);
        }
    };

    public static void setSearchIndexData(Context context, String key,
            String title, String screenTtile, String summaryOn,
            String summaryOff, String intentAction, String intentTargetClass,
            String intentTargetPackage, int currentEnable,
            String preferenceType, String settingsDBTableName,
            String settingsDBField, int visible, int checkValue) {
        SearchIndexableRaw data = new SearchIndexableRaw(context);
        data.key = key;
        data.title = title;
        data.screenTitle = screenTtile;
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
}
