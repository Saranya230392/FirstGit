/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.SettingsSwitchPreference;
import com.android.settings.Utils;

import java.util.List;

/**
 * Settings screen for configuring a specific user. It can contain user restrictions
 * and deletion controls. It is shown when you tap on the settings icon in the
 * user management (UserSettings) screen.
 *
 * Arguments to this fragment must include the userId of the user (in EXTRA_USER_ID) for whom
 * to display controls, or should contain the EXTRA_USER_GUEST = true.
 */
public class UserDetailsSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    // [2014-10-17][seungyeop.yeom] add menu item for delete user
    private static final int MENU_REMOVE_USER = Menu.FIRST;

    private static final String KEY_ENABLE_TELEPHONY = "enable_calling";
    private static final String KEY_REMOVE_USER = "remove_user";

    /** Integer extra containing the userId to manage */
    static final String EXTRA_USER_ID = "user_id";
    /** Boolean extra to indicate guest preferences */
    static final String EXTRA_USER_GUEST = "guest_user";

    private static final int DIALOG_CONFIRM_REMOVE = 1;
    private static final int DIALOG_CONFIRM_ENABLE_CALLING = 2;
    private static final int DIALOG_CONFIRM_ENABLE_CALLING_SMS = 3;

    private UserManager mUserManager;
    private SettingsSwitchPreference mPhonePref;
    private Preference mRemoveUserPref;

    private UserInfo mUserInfo;
    private boolean mGuestUser;
    private Bundle mDefaultGuestRestrictions;
    private boolean mIsOwner = UserHandle.myUserId() == UserHandle.USER_OWNER;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final Context context = getActivity();
        ActionBar actionBar = getActivity().getActionBar();
        mUserManager = (UserManager)context.getSystemService(Context.USER_SERVICE);

        addPreferencesFromResource(R.xml.user_details_settings);
        mPhonePref = (SettingsSwitchPreference)findPreference(KEY_ENABLE_TELEPHONY);
        mPhonePref.setSwitchTextOn("");
        mPhonePref.setSwitchTextOff("");
        mPhonePref.setDivider(false);
        mRemoveUserPref = findPreference(KEY_REMOVE_USER);

        mGuestUser = getArguments().getBoolean(EXTRA_USER_GUEST, false);

        if (!mGuestUser) {
            setHasOptionsMenu(true);
            // Regular user. Get the user id from the caller.
            final int userId = getArguments().getInt(EXTRA_USER_ID, -1);
            if (userId == -1) {
                throw new RuntimeException("Arguments to this fragment must contain the user id");
            }

            // [START][2014-10-03][seungyeop.yeom] Modified code from the Native (add title)
            String mTitle = getArguments().getString("title", "");
            actionBar.setTitle(mTitle);
            // [END][2014-10-03][seungyeop.yeom] Modified code from the Native (add title)

            mUserInfo = mUserManager.getUserInfo(userId);
            mPhonePref.setChecked(!mUserManager.hasUserRestriction(
                    UserManager.DISALLOW_OUTGOING_CALLS, new UserHandle(userId)));
            mRemoveUserPref.setOnPreferenceClickListener(this);
        } else {
            setHasOptionsMenu(false);
            // These are not for an existing user, just general Guest settings.
            removePreference(KEY_REMOVE_USER);
            // Default title is for calling and SMS. Change to calling-only here
            mPhonePref.setTitle(R.string.user_enable_calling_ex);
            mDefaultGuestRestrictions = mUserManager.getDefaultGuestRestrictions();
            Log.d("YSY",
                    "init mDefaultGuestRestrictions.getBoolean(UserManager.DISALLOW_OUTGOING_CALLS) : "
                            + mDefaultGuestRestrictions
                                    .getBoolean(UserManager.DISALLOW_OUTGOING_CALLS));
            Log.d("YSY",
                    "init mDefaultGuestRestrictions.getBoolean(UserManager.DISALLOW_SMS) : "
                            + mDefaultGuestRestrictions
                                    .getBoolean(UserManager.DISALLOW_SMS));
            mPhonePref.setChecked(
                    !mDefaultGuestRestrictions.getBoolean(UserManager.DISALLOW_OUTGOING_CALLS));
        }
        // [START][2014-10-17][seungyeop.yeom] add menu item for delete user, delete pref button of remove user
        /*if (mUserManager.hasUserRestriction(UserManager.DISALLOW_REMOVE_USER)) {
            removePreference(KEY_REMOVE_USER);
        }*/
        removePreference(KEY_REMOVE_USER);
        // [END][2014-10-17][seungyeop.yeom] add menu item for delete user

        mPhonePref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mRemoveUserPref) {
            if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
                throw new RuntimeException("Only the owner can remove a user");
            }
            showDialog(DIALOG_CONFIRM_REMOVE);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mGuestUser) {
            Log.d("YSY", "onPreferenceChange, newValue : " + ((Boolean)newValue));
            // TODO: Show confirmation dialog: b/15761405
            mDefaultGuestRestrictions.putBoolean(UserManager.DISALLOW_OUTGOING_CALLS,
                    !((Boolean)newValue));
            // SMS is always disabled for guest
            mDefaultGuestRestrictions.putBoolean(UserManager.DISALLOW_SMS, true);

            Log.d("YSY", "omDefaultGuestRestrictions, UserManager.DISALLOW_OUTGOING_CALLS : "
                    + mDefaultGuestRestrictions.getBoolean(UserManager.DISALLOW_OUTGOING_CALLS));
            Log.d("YSY", "omDefaultGuestRestrictions, UserManager.DISALLOW_SMS : "
                    + mDefaultGuestRestrictions.getBoolean(UserManager.DISALLOW_SMS));

            mUserManager.setDefaultGuestRestrictions(mDefaultGuestRestrictions);
            // Update the guest's restrictions, if there is a guest
            List<UserInfo> users = mUserManager.getUsers(true);
            for (UserInfo user : users) {
                if (user.isGuest()) {
                    UserHandle userHandle = new UserHandle(user.id);
                    Bundle userRestrictions = mUserManager.getUserRestrictions(userHandle);
                    userRestrictions.putAll(mDefaultGuestRestrictions);
                    mUserManager.setUserRestrictions(userRestrictions, userHandle);
                }
            }
        } else {
            // TODO: Show confirmation dialog: b/15761405
            UserHandle userHandle = new UserHandle(mUserInfo.id);
            mUserManager.setUserRestriction(UserManager.DISALLOW_OUTGOING_CALLS,
                    !((Boolean)newValue), userHandle);
            mUserManager.setUserRestriction(UserManager.DISALLOW_SMS,
                    !((Boolean)newValue), userHandle);
        }
        return true;
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        Context context = getActivity();
        if (context == null) {
            return null;
        }

        switch (dialogId) {
        case DIALOG_CONFIRM_REMOVE: {
            Dialog dlg = Utils.createRemoveConfirmationDialog(getActivity(), mUserInfo.id,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getActivity(), R.string.multi_users_deleted_toast,
                                    Toast.LENGTH_SHORT).show();
                            removeUser();
                        }
                    });
            return dlg;
        }
        case DIALOG_CONFIRM_ENABLE_CALLING:
        case DIALOG_CONFIRM_ENABLE_CALLING_SMS:
            // TODO: b/15761405
        default:
        }
        return null;
    }

    void removeUser() {
        mUserManager.removeUser(mUserInfo.id);
        finishFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // [START][2014-10-17][seungyeop.yeom] add menu item for delete user
        int pos = 0;
        UserManager um = (UserManager)getActivity().getSystemService(Context.USER_SERVICE);

        if (mIsOwner && !um.hasUserRestriction(UserManager.DISALLOW_REMOVE_USER)) {
            Log.d("YB", "!mIsOwner && !um.hasUserRestriction(UserManager.DISALLOW_REMOVE_USER)");
            MenuItem removeThisUser = menu.add(0, MENU_REMOVE_USER, pos++,
                    getResources().getString(R.string.user_delete_user_description));
            removeThisUser.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // [START][2014-10-17][seungyeop.yeom] add menu item for delete user
        final int itemId = item.getItemId();
        if (itemId == MENU_REMOVE_USER) {
            if (!mIsOwner) {
                throw new RuntimeException("Only the owner can remove a user");
            }
            showDialog(DIALOG_CONFIRM_REMOVE);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
