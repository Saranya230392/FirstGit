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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.settings.R;
import com.android.settings.drawable.CircleFramedDrawable;

/**
 * This class encapsulates a Dialog for editing the user nickname and photo.
 */
public class EditUserInfoController {

    private static final String KEY_AWAITING_RESULT = "awaiting_result";
    private static final String KEY_SAVED_PHOTO = "pending_photo";

    private Dialog mEditUserInfoDialog;
    private Bitmap mSavedPhoto;
    private EditUserPhotoController mEditUserPhotoController;
    private UserHandle mUser;
    private UserManager mUserManager;
    private boolean mWaitingForActivityResult = false;

    public interface OnContentChangedCallback {
        public void onPhotoChanged(Drawable photo);

        public void onLabelChanged(CharSequence label);
    }

    public void clear() {
        mEditUserInfoDialog = null;
        mSavedPhoto = null;
    }

    public Dialog getDialog() {
        return mEditUserInfoDialog;
    }

    public void onRestoreInstanceState(Bundle icicle) {
        mSavedPhoto = (Bitmap)icicle.getParcelable(KEY_SAVED_PHOTO);
        mWaitingForActivityResult = icicle.getBoolean(KEY_AWAITING_RESULT, false);
    }

    public void onSaveInstanceState(Bundle outState) {
        if (mEditUserInfoDialog != null && mEditUserInfoDialog.isShowing()
                && mEditUserPhotoController != null) {
            outState.putParcelable(KEY_SAVED_PHOTO,
                    mEditUserPhotoController.getNewUserPhotoBitmap());
        }
        if (mWaitingForActivityResult) {
            outState.putBoolean(KEY_AWAITING_RESULT,
                    mWaitingForActivityResult);
        }
    }

    public void startingActivityForResult() {
        mWaitingForActivityResult = true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mWaitingForActivityResult = false;

        if (mEditUserInfoDialog != null && mEditUserInfoDialog.isShowing()
                && mEditUserPhotoController.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

    Drawable getCircularUserIcon(Activity activity) {
        Bitmap userIcon = mUserManager.getUserIcon(mUser.getIdentifier());
        if (userIcon == null) {
            return null;
        }
        CircleFramedDrawable circularIcon =
                CircleFramedDrawable.getInstance(activity, userIcon);
        return circularIcon;
    }

    public Dialog createDialog(final Fragment fragment, final Drawable currentUserIcon,
            final CharSequence currentUserName,
            int titleResId, final OnContentChangedCallback callback, UserHandle user) {
        Activity activity = fragment.getActivity();
        mUser = user;
        if (mUserManager == null) {
            mUserManager = UserManager.get(activity);
        }
        LayoutInflater inflater = activity.getLayoutInflater();
        View content = inflater.inflate(R.layout.edit_user_info_dialog_content, null);

        UserInfo info = mUserManager.getUserInfo(mUser.getIdentifier());

        // [START][2014-10-14][seungyeop.yeom] modify status of Edit Text component (limit length)
        final EditText userNameView = (EditText)content.findViewById(R.id.user_name);
        userNameView.setText(info.name);
        userNameView.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        InputFilter[] mFilterArray = new InputFilter[1];
        mFilterArray[0] = new InputFilter.LengthFilter(512);
        userNameView.setFilters(mFilterArray);
        // [END][2014-10-14][seungyeop.yeom] modify status of Edit Text component (limit length)

        final ImageView userPhotoView = (ImageView)content.findViewById(R.id.user_photo);
        Drawable drawable = null;
        if (mSavedPhoto != null) {
            drawable = CircleFramedDrawable.getInstance(activity, mSavedPhoto);
        } else {
            drawable = currentUserIcon;
            if (drawable == null) {
                drawable = getCircularUserIcon(activity);
            }
        }
        userPhotoView.setImageDrawable(drawable);
        mEditUserPhotoController = new EditUserPhotoController(fragment, userPhotoView,
                mSavedPhoto, drawable, mWaitingForActivityResult);
        mEditUserInfoDialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.user_name_settings_title)
                .setView(content)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            // Update the name if changed.
                            CharSequence userName = userNameView.getText();
                            if (!TextUtils.isEmpty(userName)) {
                                if (currentUserName == null
                                        || !userName.toString().equals(currentUserName.toString())) {
                                    if (callback != null) {
                                        callback.onLabelChanged(userName.toString());
                                    }
                                    mUserManager.setUserName(mUser.getIdentifier(),
                                            userName.toString());
                                }
                            }
                            // Update the photo if changed.
                            Drawable drawable = mEditUserPhotoController.getNewUserPhotoDrawable();
                            Bitmap bitmap = mEditUserPhotoController.getNewUserPhotoBitmap();
                            if (drawable != null && bitmap != null
                                    && !drawable.equals(currentUserIcon)) {
                                if (callback != null) {
                                    callback.onPhotoChanged(drawable);
                                }
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        mUserManager.setUserIcon(mUser.getIdentifier(),
                                                mEditUserPhotoController.getNewUserPhotoBitmap());
                                        return null;
                                    }
                                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
                            }
                            fragment.getActivity().removeDialog(
                                    RestrictedProfileSettings.DIALOG_ID_EDIT_USER_INFO);
                        }
                        clear();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clear();
                    }
                })
                .create();

        userNameView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() <= 0) {
                    ((AlertDialog)mEditUserInfoDialog).getButton(
                            Dialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    ((AlertDialog)mEditUserInfoDialog).getButton(
                            Dialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        // Make sure the IME is up.
        mEditUserInfoDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return mEditUserInfoDialog;
    }
}
