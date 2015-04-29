package com.android.settings.soundprofile;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.text.Spannable;
import android.text.Html;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;

import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuInflater;
import android.view.Menu;

import com.android.settings.R;

//[S][2011.12.30][jaeyoon.hyun] Change Config.java location
//[E][2011.12.30][jaeyoon.hyun] Change Config.java location
import com.android.settings.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

//LGE_CHANGE [jonghen.han@lge.com] 2012-05-25, KeepScreenOn
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.CompoundButton.OnCheckedChangeListener;

//LGE_CHANGE [jonghen.han@lge.com] 2012-05-25, KeepScreenOn

public class SoundProfileSetting extends PreferenceActivity implements
        android.view.View.OnClickListener {
    private static final String TAG = "SoundProfileSetting";

    //private static final float TEXT_SIZE = (float)1.1;
    private static final int MAX_ITEMS = 10;
    private static final int DIALOG_DELETE_ITEM = 0;

    private LinearLayout mNoProfileLayout;
    private LinearLayout mEmptyLayout;
    private ListView mListView;
    private TextView mEmptyText;

    private ImageView mListLine;
    private ImageView mEmptyIcon;

    private Toast mToast;

    ArrayList<String> mItems;
    private Context mContext;

    SoundProfileInfo mSoundProfileInfo;

    //MyProfile
    Button mOK;
    Button mCancel;
    Button mEdit;
    private boolean mEditFlag = true;
    private String mSelectProfileName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.sp_my_sound_profile_title_NORMAL);
        }
        setContentView(R.layout.my_sound_profile_list);
        mContext = getApplicationContext();

        mListView = (ListView)findViewById(android.R.id.list);
        mNoProfileLayout = (LinearLayout)findViewById(R.id.no_profiles_layout);
        mEmptyText = (TextView)findViewById(R.id.no_profiles);
        mEmptyIcon = (ImageView)findViewById(R.id.no_profiles_image);
        mListLine = (ImageView)findViewById(R.id.last_line);
        mEmptyLayout = (LinearLayout)findViewById(R.id.empty_layout);
        mSoundProfileInfo = new SoundProfileInfo(mContext);

        mOK = (Button)findViewById(R.id.ok_button);
        mCancel = (Button)findViewById(R.id.cancel_button);
        mEdit = (Button)findViewById(R.id.edit_button);
        mOK.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mEdit.setOnClickListener(this);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        mSelectProfileName = mItems.get(position);
        Log.d(TAG,"SelectProfile : " + mSelectProfileName);
        if (mSelectProfileName
                .equals(getString(R.string.sound_settings_profile_normal))) {
            mEdit.setEnabled(false);
        } else {
            mEdit.setEnabled(true);
        }

    }

    private void updateList() {
        mItems = new ArrayList<String>();
        Log.d(TAG, "initProfilesList!!!");

        //MyProifle Default named Normal
        mItems.add(getString(R.string.sound_settings_profile_normal));

        if (mSoundProfileInfo.getSoundProfilesCount() >= 0) {

            ArrayList<String> userProfileName = mSoundProfileInfo
                    .getAllProfileName_User();
            for (int i = 0; i < mSoundProfileInfo.getSoundProfilesCount(); i++) {
                if (userProfileName.get(i).equals(getResources()
                        .getString(R.string.sp_sound_profile_default_summary_NORMAL))) {
                    mSoundProfileInfo.removeProfiles(getResources()
                            .getString(R.string.sp_sound_profile_default_summary_NORMAL));
                }
                Log.d(TAG, "profile name" + userProfileName.get(i) + i);
            }

            userProfileName = mSoundProfileInfo
                    .getAllProfileName_User();
            for (int i = 0; i < mSoundProfileInfo.getSoundProfilesCount(); i++) {
                mItems.add(userProfileName.get(i));
                Log.d(TAG, "profile name" + userProfileName.get(i));
            }
            Log.d(TAG, "itemsbefore" + mItems);

            int layoutType;
            if (IsUI4_2()) {
                layoutType = android.R.layout.simple_list_item_single_choice;
            } else {
                layoutType = android.R.layout.simple_list_item_1;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    layoutType, mItems);
            // 3.AdapterView
            mListView.setAdapter(adapter);
            mListView.setItemsCanFocus(false);
            mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            setListViewItemMyProfile(userProfileName);
            setEditButtonStatus();
        }
    }

    private void setEditButtonStatus() {
        if(mSoundProfileInfo.getUserProfileName().equals("")) {
            mEdit.setEnabled(false);
        } else {
            mEdit.setEnabled(true);
        }
    }

    // Set MyProfile Item RadioBtn
    private void setListViewItemMyProfile(ArrayList<String> userProfileName) {
        for (int i = 0; i < mSoundProfileInfo.getSoundProfilesCount(); i++) {
            if (userProfileName.get(i).equals(
                    mSoundProfileInfo.getUserProfileName())) {
                mListView.setItemChecked(i+1, true);
                mListView.setSelection(i+1);
                mSelectProfileName = userProfileName.get(i);
                Log.d(TAG, "1. setItem : " + userProfileName.get(i));

            }
        }
        if (mSoundProfileInfo.getUserProfileName().equals("")) {
            mSelectProfileName = getString(R.string.sound_settings_profile_normal);
            mListView.setItemChecked(0, true);
            mListView.setSelection(0);
            Log.d(TAG, "2. setItem : " + mSelectProfileName);
        }

    }

    public boolean IsUI4_2() {
        return com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2;
    }

    // add Add button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.my_sound_profile_action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuItem item = menu.findItem(R.id.info_action_delete_profiles);
        updateNoProfilesLayout();
        Log.d(TAG,
                "mSoundProfileInfo.getSoundProfilesCount() in :"
                        + mSoundProfileInfo.getSoundProfilesCount());
        if (mSoundProfileInfo.getSoundProfilesCount() > 0) {
            item.setEnabled(true);
            item.setVisible(true);
        } else {
            item.setEnabled(false);
            item.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    protected void onResume() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onResume");
        super.onResume();

        invalidateOptionsMenu();
        updateList();
    }

    private void updateNoProfilesLayout() {
        Log.d(TAG, "mSoundProfileInfo.getSoundProfilesCount()"
                + mSoundProfileInfo.getSoundProfilesCount());

        if (mSoundProfileInfo.getSoundProfilesCount() > 0) {
            mNoProfileLayout.setVisibility(View.GONE);
            mListLine.setVisibility(View.VISIBLE);
            mEmptyIcon.setVisibility(View.GONE);
            mEmptyLayout.setVisibility(View.VISIBLE);
            mEmptyText.setVisibility(View.GONE);

        } else {
            mNoProfileLayout.setVisibility(View.GONE);
            mEmptyIcon.setVisibility(View.GONE);
            mEmptyText.setVisibility(View.GONE);
            mListLine.setVisibility(View.VISIBLE);
            mEmptyLayout.setVisibility(View.VISIBLE);
            mEmptyIcon.setClickable(true);
            mEmptyIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                        Intent i = new Intent();
                        i.setClassName("com.android.settings",
                                "com.android.settings.soundprofile.AddSoundProfiles");

                        startActivity(i);
                    } catch (ActivityNotFoundException e) {
                        ;
                    }
                }
            });

            String htmlText = "";

            htmlText = this.getResources().getString(
                    R.string.sp_my_sound_profile_add_button_NORMAL,
                    "+");

            int index = htmlText.indexOf("+");
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(htmlText);

            Drawable drawble = getResources().getDrawable(R.drawable.ic_add);
            if (drawble != null) {
                int textSize = (int)(mEmptyText.getTextSize() * 1.1);
                drawble.setBounds(0, 0, textSize, textSize);
            }

            stringBuilder.setSpan(new ImageSpan(drawble),
                    index, index + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            stringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    widget.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                    try {

                        Intent i = new Intent();
                        i.setClassName("com.android.settings",
                                "com.android.settings.soundprofile.AddSoundProfiles");

                        startActivity(i);
                    } catch (ActivityNotFoundException e) {
                        ;
                    }
                }
            }, index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mEmptyText.setText(stringBuilder, BufferType.SPANNABLE);
            mEmptyText.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        int res_icon = android.R.attr.alertDialogIcon;
        if (Utils.isUI_4_1_model(this)) {
            res_icon = R.drawable.no_icon;
        }
        switch (id) {
        case DIALOG_DELETE_ITEM:
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.delete)
                    .setIconAttribute(res_icon)
                    .setPositiveButton(this.getResources().getString(R.string.yes),
                            new OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    ArrayList<String> mDeleteSingleProfile = mSoundProfileInfo
                                            .getAllProfileName_User();

                                    Log.d(TAG, "profile name" + mDeleteSingleProfile.get(0));

                                    mSoundProfileInfo.removeProfiles(mDeleteSingleProfile.get(0)
                                            .toString());
                                    mSoundProfileInfo.setUserProfileName("");
                                    invalidateOptionsMenu();
                                    updateList();
                                    updateNoProfilesLayout();
                                    mToast.makeText(getApplicationContext(),
                                            R.string.sp_auto_reply_deleted_NORMAL,
                                            Toast.LENGTH_SHORT)
                                            .show();
                                    finish();

                                }
                            })
                    .setNegativeButton(this.getResources().getString(R.string.no), null)
                    .setCancelable(true)
                    .setOnCancelListener(null)
                    .setMessage(R.string.sp_sound_profile_delete_NORMAL)
                    .create();
        default:
            break;
        }

        return super.onCreateDialog(id);
    }

    // touch title to go back
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
        case R.id.info_action_add_profiles:
            if (mSoundProfileInfo.getSoundProfilesCount() < MAX_ITEMS) {

                Intent i = new Intent();
                i.setClassName("com.android.settings",
                        "com.android.settings.soundprofile.AddSoundProfiles");
                startActivity(i);
            } else {
                mToast.makeText(getApplicationContext(),
                        R.string.sp_sound_profile_not_more_NORMAL, Toast.LENGTH_SHORT)
                        .show();

            }

            return true;
        case R.id.info_action_delete_profiles:
            if (mSoundProfileInfo.getSoundProfilesCount() == 1) {
                showDialog(DIALOG_DELETE_ITEM);

            } else {
                Intent i = new Intent();
                i.setClassName("com.android.settings",
                        "com.android.settings.soundprofile.ProfilesDeleteActivity");
                startActivity(i);
            }

            return true;

        case android.R.id.home:
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setClassName("com.android.settings",
                    "com.android.settings.soundprofile.SoundProfileSetting");
            startActivity(intent);
            finish();
            return true;
        default:
            break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

        case R.id.cancel_button:
            finish();
            break;
        case R.id.edit_button:
            Intent i = new Intent();
            i.setClassName("com.android.settings",
                    "com.android.settings.soundprofile.AddSoundProfiles");
            i.putExtra("user_soundprofile_modify", mSelectProfileName);
            startActivity(i);
            break;
        case R.id.ok_button:
            if (mSelectProfileName
                    .equals(getString(R.string.sound_settings_profile_normal))) {
                mSoundProfileInfo.setUserProfileName("");
            } else {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.VIBRATE_WHEN_RINGING, 0);
                mSoundProfileInfo.setProfileDatatoSystem(mSelectProfileName);
                mSoundProfileInfo.setUserProfileName(mSelectProfileName);
            }
            finish();
            break;
        default:
            break;
        }

    }

}
