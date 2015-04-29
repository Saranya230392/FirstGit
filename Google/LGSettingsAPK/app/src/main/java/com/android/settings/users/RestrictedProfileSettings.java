/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lge.constants.UserInfoConstants;
import com.android.settings.R;
import com.android.settings.Utils;

import java.util.List;

public class RestrictedProfileSettings extends AppRestrictionsFragment implements
        EditUserInfoController.OnContentChangedCallback {

    public static final String FILE_PROVIDER_AUTHORITY = "com.android.settings.files";
    static final int DIALOG_ID_EDIT_USER_INFO = 1;
    private static final int DIALOG_CONFIRM_REMOVE = 2;

    private View mHeaderView;
    private ImageView mUserIconView;
    private TextView mUserNameView;
    private ImageView mDeleteButton;

    // [seungyeop.yeom][2014-05-09] add function of kids mode
    private TextView mUserSummaryView;
    private LinearLayout mKidsModeDescLayout;

    private EditUserInfoController mEditUserInfoController =
            new EditUserInfoController();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (icicle != null) {
            mEditUserInfoController.onRestoreInstanceState(icicle);
        }

        init(icicle);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (mHeaderView == null) {
            mHeaderView = LayoutInflater.from(getActivity()).inflate(
                    R.layout.user_info_header, null);
            setPinnedHeaderView(mHeaderView);

            mHeaderView.setOnClickListener(this);
            mKidsModeDescLayout = (LinearLayout)mHeaderView.findViewById(R.id.kids_mode_desc_pref);
            mUserIconView = (ImageView)mHeaderView.findViewById(android.R.id.icon);
            mUserNameView = (TextView)mHeaderView.findViewById(android.R.id.title);
            mDeleteButton = (ImageView)mHeaderView.findViewById(R.id.delete);
            mDeleteButton.setOnClickListener(this);

            // [START][seungyeop.yeom][2014-05-09] add function of kids mode
            mKidsModeDescLayout.setVisibility(View.GONE);
            mUserSummaryView = (TextView)mHeaderView.findViewById(android.R.id.summary);
            UserInfo userinfo = mUserManager.getUserInfo(mUser.getIdentifier());
            Log.d("yeom", "Restricted profile user id : " + mUser.getIdentifier());

            if ((userinfo.flags & UserInfoConstants.FLAG_KIDS) == UserInfoConstants.FLAG_KIDS) {
                Log.d("yeom", "Restricted profile kids summary");
                mKidsModeDescLayout.setVisibility(View.VISIBLE);
                mUserSummaryView.setText(R.string.user_kids_user_name);
            } else {
                mUserSummaryView.setText(R.string.user_add_restricted_user_item_title);
            }
            // [END]

            getListView().setFastScrollEnabled(false);
        }
        // This is going to bind the preferences.
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mEditUserInfoController.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if user still exists
        UserInfo info = getExistingUser(mUser);
        if (info == null) {
            finishFragment();
        } else {
            ((TextView)mHeaderView.findViewById(android.R.id.title)).setText(info.name);
            ((ImageView)mHeaderView.findViewById(android.R.id.icon)).setImageDrawable(
                    getCircularUserIcon());
        }
    }

    private UserInfo getExistingUser(UserHandle thisUser) {
        final List<UserInfo> users = mUserManager.getUsers(true); // Only get non-dying
        for (UserInfo user : users) {
            if (user.id == thisUser.getIdentifier()) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        mEditUserInfoController.startingActivityForResult();
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mEditUserInfoController.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        if (view == mHeaderView) {
            showDialog(DIALOG_ID_EDIT_USER_INFO);
        } else if (view == mDeleteButton) {
            showDialog(DIALOG_CONFIRM_REMOVE);
        } else {
            super.onClick(view); // in AppRestrictionsFragment
        }
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        if (dialogId == DIALOG_ID_EDIT_USER_INFO) {
            return mEditUserInfoController.createDialog(this, mUserIconView.getDrawable(),
                    mUserNameView.getText(), R.string.profile_info_settings_title,
                    this, mUser);
        } else if (dialogId == DIALOG_CONFIRM_REMOVE) {
            Dialog dlg =
                    Utils.createRemoveConfirmationDialog(getActivity(), mUser.getIdentifier(),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    removeUser();
                                }
                            }
                            );
            return dlg;
        }

        return null;
    }

    private void removeUser() {
        getView().post(new Runnable() {
            public void run() {
                mUserManager.removeUser(mUser.getIdentifier());
                finishFragment();
            }
        });
    }

    @Override
    public void onPhotoChanged(Drawable photo) {
        mUserIconView.setImageDrawable(photo);
    }

    @Override
    public void onLabelChanged(CharSequence label) {
        mUserNameView.setText(label);
    }
}
