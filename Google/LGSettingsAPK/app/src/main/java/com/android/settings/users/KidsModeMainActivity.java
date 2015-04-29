package com.android.settings.users;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtilsEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.lge.constants.DevicePolicyManagerConstants;
import com.lge.constants.UserInfoConstants;
import com.android.settings.R;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.Utils;
import com.android.settings.lockscreen.ChooseLockGeneric;

import android.app.ActivityManagerNative;

import com.android.internal.util.UserIcons;

public class KidsModeMainActivity extends Activity implements OnClickListener {
    private static final int DIALOG_NEED_LOCKSCREEN_KIDS_MODE = 0;
    private static final int USER_TYPE_KIDS_MODE = 3;
    private static final int MESSAGE_CONFIG_USER = 1;
    private static final int REQUEST_CHOOSE_LOCK_KIDS_MODE = 11;
    private boolean mIsOwner = UserHandle.myUserId() == UserHandle.USER_OWNER;

    private Context mContext;
    private UserManager mUserManager;

    private Button mAddUserBtn;
    private int mUserCount = 0;
    private int maxUserId = 0;
    boolean moreUsers = true;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_CONFIG_USER:
                onManageUserClicked(msg.arg1, true);
                break;
            default:
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mUserManager = (UserManager)mContext
                .getSystemService(Context.USER_SERVICE);
        List<UserInfo> users = mUserManager.getUsers(true);
        UserInfo userinfo = mUserManager.getUserInfo(UserHandle.myUserId());
        moreUsers = mUserManager.canAddMoreUsers();

        for (UserInfo user : users) {
            if ((user.flags & UserInfoConstants.FLAG_KIDS) == UserInfoConstants.FLAG_KIDS) {
                mUserCount++;
                if (maxUserId < user.id) {
                    maxUserId = user.id;
                }

            }
        }

        if (mUserCount > 0) {
            if ((userinfo.flags & UserInfoConstants.FLAG_KIDS) == UserInfoConstants.FLAG_KIDS) {
                switchUserNow(UserHandle.USER_OWNER);
            } else {
                switchUserNow(maxUserId);
            }

            finish();
        }

        int dialog_theme_res = getResources().getIdentifier(
                "Theme.LGE.Default", "style", "com.lge");
        setTheme(dialog_theme_res);
        setContentView(R.layout.kids_mode_main);

        if (userinfo.iconPath == null || userinfo.iconPath.equals("")) {
            assignProfilePhoto(userinfo);
        }

        mAddUserBtn = (Button)findViewById(R.id.add_user_btn);
        mAddUserBtn.setOnClickListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.kids_mode_main);
        mAddUserBtn = (Button)findViewById(R.id.add_user_btn);
        mAddUserBtn.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if (moreUsers == true) {
            mAddUserBtn.setEnabled(true);
        } else {
            mAddUserBtn.setEnabled(false);
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.add_user_btn:
            if (hasLockscreenSecurity()) {
                addUserNow(USER_TYPE_KIDS_MODE);
                finish();
            } else {
                showDialog(DIALOG_NEED_LOCKSCREEN_KIDS_MODE);
            }
            break;

        default:
            break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int dialogId) {
        // TODO Auto-generated method stub
        Context context = this;
        if (context == null) {
            return null;
        }
        switch (dialogId) {
        case DIALOG_NEED_LOCKSCREEN_KIDS_MODE: {
            Dialog dlg = new AlertDialog.Builder(context)
                    .setTitle(R.string.user_kids_user_name)
                    .setMessage(R.string.user_need_lock_message_kids_mode)
                    .setPositiveButton(R.string.user_set_lock_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    launchChooseLockscreen(USER_TYPE_KIDS_MODE);
                                }
                            })
                    .setNegativeButton(R.string.skip_label,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    // TODO Auto-generated method stub
                                    addUserNow(USER_TYPE_KIDS_MODE);
                                    finish();
                                }
                            }).create();
            return dlg;
        }
        default:
            return null;
        }
    }

    private void addUserNow(final int userType) {
        UserInfo user = null;
        if (userType == USER_TYPE_KIDS_MODE) {
            user = createLimitedUser(USER_TYPE_KIDS_MODE);
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_CONFIG_USER,
                    user.id, user.serialNumber));
        }
    }

    private UserInfo createLimitedUser(int userType) {
        // [START][seungyeop.yeom][2014-05-09] add function of kids mode
        UserInfo newUserInfo = null;
        if (userType == USER_TYPE_KIDS_MODE) {
            Log.d("YSY", "createLimitedUser, USER_TYPE_KIDS_MODE");
            newUserInfo = mUserManager.createSecondaryUser(getResources()
                    .getString(R.string.user_new_kids_name),
                    UserInfoConstants.FLAG_KIDS);
        }
        // [END]

        if (newUserInfo == null) {
            Log.d("YSY", "UserInfo (restricted) == null");
            return newUserInfo;
        }

        Log.d("YSY", "UserInfo (restricted) != null");
        int userId = newUserInfo.id;
        UserHandle user = new UserHandle(userId);
        mUserManager.setUserRestriction(UserManager.DISALLOW_MODIFY_ACCOUNTS,
                true, user);
        // Change the setting before applying the DISALLOW_SHARE_LOCATION
        // restriction, otherwise
        // the putIntForUser() will fail.
        Secure.putIntForUser(getContentResolver(), Secure.LOCATION_MODE,
                Secure.LOCATION_MODE_OFF, userId);
        mUserManager.setUserRestriction(UserManager.DISALLOW_SHARE_LOCATION,
                true, user);
        assignDefaultPhoto(newUserInfo);
        // Add shared accounts
        AccountManager am = AccountManager.get(mContext);
        Account[] accounts = am.getAccounts();
        if (accounts != null) {
            for (Account account : accounts) {
                am.addSharedAccount(account, user);
            }
        }
        return newUserInfo;
    }

    private void onManageUserClicked(int userId, boolean newUser) {
        UserInfo info = mUserManager.getUserInfo(userId);
        if (info.isRestricted() && mIsOwner) {
            Intent intent = new Intent(mContext, KidsModeAppListActivity.class);
            intent.putExtra(RestrictedProfileSettings.EXTRA_USER_ID, userId);
            intent.putExtra(RestrictedProfileSettings.EXTRA_NEW_USER, newUser);

            startActivity(intent);
        }
    }

    private boolean hasLockscreenSecurity() {
        LockPatternUtilsEx lpu = new LockPatternUtilsEx(mContext);
        return lpu.isLockPasswordEnabled() || lpu.isLockPatternEnabled()
                || lpu.isLockKnockOnEnabled();
    }

    private void launchChooseLockscreen(int userType) {
        Intent chooseLockIntent = new Intent(
                DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
        chooseLockIntent
                .putExtra(
                        ChooseLockGeneric.ChooseLockGenericFragment.MINIMUM_QUALITY_KEY,
                        DevicePolicyManagerConstants.PASSWORD_QUALITY_KNOCK_ON);

        if (userType == USER_TYPE_KIDS_MODE) {
            startActivityForResult(chooseLockIntent,
                    REQUEST_CHOOSE_LOCK_KIDS_MODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHOOSE_LOCK_KIDS_MODE) {
            if (resultCode != Activity.RESULT_CANCELED
                    && hasLockscreenSecurity()) {
                addUserNow(USER_TYPE_KIDS_MODE);
                finish();
            } else {
                showDialog(DIALOG_NEED_LOCKSCREEN_KIDS_MODE);
            }
        }
    }

    private void switchUserNow(int userId) {
        try {
            ActivityManagerNative.getDefault().switchUser(userId);
        } catch (RemoteException re) {
            Log.w("YSY", "RemoteException");
        }
    }

    private void assignProfilePhoto(final UserInfo user) {
        if (!Utils.copyMeProfilePhoto(mContext, user)) {
            Log.d("YSY", "Kids mode, assignProfilePhoto(final UserInfo user)");
            assignDefaultPhoto(user);
        }
    }

    private void assignDefaultPhoto(UserInfo user) {
        Bitmap bitmap = UserIcons.convertToBitmap(UserIcons.getDefaultUserIcon(
                user.id,
                /* light= */false));
        mUserManager.setUserIcon(user.id, bitmap);
    }
}
