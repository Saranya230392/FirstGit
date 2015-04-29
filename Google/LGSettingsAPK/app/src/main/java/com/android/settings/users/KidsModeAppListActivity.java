package com.android.settings.users;

import com.android.settings.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.app.ActivityManagerNative;

public class KidsModeAppListActivity extends Activity implements
        OnClickListener {

    private Button mStartUserBtn;
    private int mUserId;
    boolean mNewUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.kids_mode_activity);

        mUserId = intent.getIntExtra(RestrictedProfileSettings.EXTRA_USER_ID, 0);
        mNewUser = intent.getBooleanExtra(
                RestrictedProfileSettings.EXTRA_NEW_USER, false);

        mStartUserBtn = (Button)findViewById(R.id.start_user_btn);
        mStartUserBtn.setOnClickListener(this);

        Log.d("YSY", "Kids userId : " + mUserId);
        Log.d("YSY", "Kids newUser : " + mNewUser);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction tr = fm.beginTransaction();

        Bundle bundle = new Bundle();
        bundle.putInt(RestrictedProfileSettings.EXTRA_USER_ID, mUserId);
        bundle.putBoolean(RestrictedProfileSettings.EXTRA_NEW_USER, mNewUser);

        Fragment fragment = Fragment.instantiate(this,
                RestrictedProfileSettings.class.getName(), bundle);
        tr.add(R.id.kids_mode_frame, fragment);
        tr.commit();
    }

    private void switchUserNow(int userId) {
        try {
            ActivityManagerNative.getDefault().switchUser(userId);
        } catch (RemoteException re) {
            Log.w("YSY", "Kids mode, switchUserNow RemoteException");
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.start_user_btn:
            switchUserNow(mUserId);
            finish();
            break;

        default:
            break;
        }
    }
}
