package com.android.settings.soundprofile;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActivityManager;
import android.app.ListActivity;

import android.os.Bundle;
import android.os.Handler;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ContentValues;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton.OnCheckedChangeListener;
import java.util.Map;

import android.provider.MediaStore;
import android.provider.Settings;

import com.android.settings.R;

import android.net.Uri;
import android.database.Cursor;

import com.android.settings.Utils;
import com.android.settings.soundprofile.ProfilesDeleteActivity.ProfilesDeleteItem;
import com.android.settings.soundprofile.ProfilesDeleteActivity.ProfilesSectionItem;
import com.android.settings.soundprofile.ProfilesDeleteActivity.ProfilesItem;

public class ProfilesDeleteActivity extends ListActivity {
    static final String TAG = "ProfilesDeleteActivity";

    private static final int DIALOG_DELETE_ITEM = 0;
    private static final int DIALOG_DELETE_MULTI_ITEM = 1;
    private static final int DIALOG_DELETE_ALL_ITEM = 2;
    private static final int DELETE_ONE_ITEM = 1;
    private static final int NO_DELETE_ITEM = -1;

    private SharedPreferences.Editor mProfilesPrefEditor;

    private Map<String, ?> mProfilesMap;
    ArrayList<ProfilesItem> items;
    private SharedPreferences mSoundProfilesPref; //my sound profiles list sp

    int mListViewCount;
    int mParentType = -1;
    private int chkcount;
    private Toast mToast;
    private int[] deleteItemArray;

    private boolean[] mCheckItemId;
    private boolean mCompleteRead = false;

    //private Cursor mExternalCursor;
    private CheckBox mChkboxSelectAll;
    private ListView listView;
    private TextView mTxtSelected;
    private Button mOk;
    private Button mCancel;
    private LinearLayout mButtonLayout;

    ProfilesDeleteAdapter mAdapter;
    SoundProfileInfo mSoundProfileInfo;
    private static final String USER_SOUNDPROFILE = "user_soundprofile";

    @Override
    public void onCreate(Bundle savedInsttatnceState) {
        super.onCreate(savedInsttatnceState);
        setContentView(R.layout.profiles_list_del);

        mSoundProfilesPref = this.getSharedPreferences(USER_SOUNDPROFILE, 0);

        mCheckItemId = new boolean[getSoundProfilesCount()];
        deleteItemArray = new int[getSoundProfilesCount()];

        for (int i = 0; i < getSoundProfilesCount(); i++) {
            mCheckItemId[i] = false;
            deleteItemArray[i] = NO_DELETE_ITEM;
        }
        mSoundProfileInfo = new SoundProfileInfo(getApplicationContext());
    }

    public int getSoundProfilesCount() {
        mProfilesMap = mSoundProfilesPref.getAll();
        return mProfilesMap.size();
    }

    public void removeProfiles(String ProfilesName) {
        //weiyt.jiang add for summary update when deleted chosen profile [s]
        if (mSoundProfileInfo.getUserProfileName().equals(ProfilesName)) {
            mSoundProfileInfo.setUserProfileName("");
        }
        //weiyt.jiang add for summary update when deleted chosen profile [e]
        mProfilesPrefEditor = mSoundProfilesPref.edit();
        mProfilesPrefEditor.remove(ProfilesName);
        mProfilesPrefEditor.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        outState.putIntArray("id", deleteItemArray);
        outState.putBooleanArray("check_id", mCheckItemId);
        outState.putBoolean("mCompleteRead", mCompleteRead);
        outState.putInt("mListViewCount", mListViewCount);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        Log.d(TAG, "onRestoreInstanceState()");
        super.onRestoreInstanceState(state);
        if (state != null) {
            mCompleteRead = state.getBoolean("mCompleteRead");
            mListViewCount = state.getInt("mListViewCount");
            if (mCompleteRead) {
                deleteItemArray = new int[mListViewCount];
                mCheckItemId = new boolean[mListViewCount];
                deleteItemArray = state.getIntArray("id");
                mCheckItemId = state.getBooleanArray("check_id");
            }
        }
    }

    private void do_deleteItem() {
        for (int i = 0; i < mListViewCount; i++) {
            if (deleteItemArray[i] != NO_DELETE_ITEM) {
                ProfilesDeleteItem item = (ProfilesDeleteItem)items.get(i);
                removeProfiles(item.name);
            }
        }
        mToast.makeText(getApplicationContext(),
            R.string.sp_auto_reply_deleted_NORMAL, Toast.LENGTH_SHORT)
            .show();
    }

    private void AllCheckDeleteItemArray() {
        Log.d(TAG, "AllCheckDeleteItemArray()");

        if (getSoundProfilesCount() > 0) {
            for (int i = 0; i < getSoundProfilesCount(); i++) {
                deleteItemArray[i] = i;
                Log.e(TAG, "count = " + i + " deleteItemArray[]= " + deleteItemArray[i]);
            }
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d(TAG, "onResume!!!");

        initProfilesList();
        initProfilesUI();

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(R.layout.action_bar_select_all);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);

            View layoutSelectAll = actionBar.getCustomView();
            mTxtSelected = (TextView)layoutSelectAll.findViewById(R.id.txt_selected);
            mTxtSelected.setText(getString(R.string.sp_quiet_mode_contact_selected_number_NORMAL,
                    chkcount));
            mChkboxSelectAll = (CheckBox)layoutSelectAll.findViewById(R.id.chkbox_selectAll);

            mChkboxSelectAll.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (mChkboxSelectAll.isChecked() == true) {
                        AllCheckDeleteItemArray();
                        for (int i = 0; i < mListViewCount; i++) {
                            mCheckItemId[i] = true;
                        }
                    } else {
                        for (int i = 0; i < mListViewCount; i++) {
                            deleteItemArray[i] = NO_DELETE_ITEM;
                            mCheckItemId[i] = false;
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    mAdapter.setSelectedCount();
                }
            });
        }
    }

    private void initProfilesList() {
        items = new ArrayList<ProfilesItem>();
        Log.d(TAG, "initProfilesList!!!");

        if (getSoundProfilesCount() > 0) {
            mListViewCount = getSoundProfilesCount();
            //add profile name here 
            ArrayList<String> userProfileName = mSoundProfileInfo.getAllProfileName_User();
            for (int i = 0; i < getSoundProfilesCount(); i++) {
                items.add(new ProfilesDeleteItem(userProfileName.get(i), i));
                Log.d(TAG, "profile name" + userProfileName.get(i));
            }
            mCompleteRead = true;
        }
    }

    private void initProfilesUI() {
        Log.d(TAG, "initProfilesUI!!!");
        mAdapter = new ProfilesDeleteAdapter(this.getApplicationContext(),
                R.layout.vibrate_delete_list_item, items);
        setListAdapter(mAdapter);
        listView = getListView();
        listView.setItemsCanFocus(false);

        mButtonLayout = (LinearLayout)findViewById(R.id.bottomLayout);
        mButtonLayout.setVisibility(View.VISIBLE);

        mOk = (Button)findViewById(R.id.ok_button);
        mCancel = (Button)findViewById(R.id.cancel_button);
        mOk.setText(getString(R.string.delete));
        mCancel.setEnabled(true);
        mCancel.setFocusable(true);
        mOk.setEnabled(false);
        mOk.setFocusable(false);
        mOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mChkboxSelectAll.isChecked()) {
                    showDialog(DIALOG_DELETE_ALL_ITEM);
                } else {
                    deleteAlertDlg();
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected void deleteAlertDlg() {
        if (mAdapter.getCountCheckedItem() == DELETE_ONE_ITEM) {
            showDialog(DIALOG_DELETE_ITEM);
        } else {
            showDialog(DIALOG_DELETE_MULTI_ITEM);
        }
    }

    private int getDeleteMessage(int deleteType) {
        if (deleteType == DIALOG_DELETE_ITEM
                || mAdapter.getCountCheckedItem() == DELETE_ONE_ITEM) {
            return R.string.sp_sound_profile_delete_NORMAL;
        } else if (deleteType == DIALOG_DELETE_MULTI_ITEM) {
            return R.string.sp_sound_profile_select_delete_NORMAL;
        } else {
            return R.string.sp_sound_profile_select_delete_NORMAL;
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
                                    do_deleteItem();
                                    finish();
                                }
                            })
                    .setNegativeButton(this.getResources().getString(R.string.no), null)
                    .setCancelable(true)
                    .setOnCancelListener(null)
                    .setMessage(getDeleteMessage(id))
                    .create();
        case DIALOG_DELETE_MULTI_ITEM:
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.delete)
                    .setIconAttribute(res_icon)
                    .setPositiveButton(this.getResources().getString(R.string.yes),
                            new OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    do_deleteItem();
                                    finish();
                                }
                            })
                    .setNegativeButton(this.getResources().getString(R.string.no), null)
                    .setCancelable(true)
                    .setOnCancelListener(null)
                    .setMessage(getDeleteMessage(id))
                    .create();
        case DIALOG_DELETE_ALL_ITEM:
            return new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle(R.string.delete)
                    .setIconAttribute(res_icon)
                    .setPositiveButton(R.string.yes, new OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            do_deleteItem();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .setMessage(getDeleteMessage(id))
                    .create();
        default:
            break;
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    public interface ProfilesItem {
        public boolean isSection();
    }

    public static class ProfilesSectionItem implements ProfilesItem {
        private final String title;

        public ProfilesSectionItem(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public boolean isSection() {
            return true;
        }
    }

    public class ProfilesDeleteItem implements ProfilesItem {
        public final String name;
        public final int id;

        public ProfilesDeleteItem(String name, int id) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean isSection() {
            return false;
        }
    }

    static class ViewHolder {
        TextView title;
        CheckBox chkbox;
        View listItemView;
        int position;
        int tag;
    }

    public class ProfilesDeleteAdapter extends ArrayAdapter<ProfilesDeleteItem> {
        final static int TAG_SECTION = 1;
        final static int TAG_ITEM = 2;
        private ArrayList<ProfilesItem> items;
        private LayoutInflater vi;

        public ProfilesDeleteAdapter(Context context, int resource, ArrayList items) {
            super(context, resource, items);
            this.items = items;
            vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            int checkView = -1;
            final ProfilesItem i = items.get(position);
            if (i == null) {
                return v;
            }
            if (convertView != null) {
                if (i != null) {
                    ViewHolder holder = new ViewHolder();
                    holder = (ViewHolder)convertView.getTag();
                    if (i.isSection() && holder.tag == TAG_SECTION) {
                        v = convertView;
                        checkView = TAG_SECTION;
                    } else if (!i.isSection() && holder.tag == TAG_ITEM) {
                        v = convertView;
                        checkView = TAG_ITEM;
                    } else {
                        convertView = null;
                        checkView = 0;
                    }
                }
            }

            if (convertView == null) {
                if (i.isSection()) {
                    ProfilesSectionItem si = (ProfilesSectionItem)i;
                    ViewHolder holder = new ViewHolder();
                    v = vi.inflate(R.layout.vibrate_delete_list_section, null);
                    v.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View arg0) {
                        }
                    });
                    v.setOnLongClickListener(null);
                    v.setLongClickable(false);

                    final TextView sectionView =
                            (TextView)v.findViewById(R.id.list_item_section_text);
                    sectionView.setText(si.getTitle());
                    holder.tag = TAG_SECTION;
                    v.setTag(holder);
                } else {
                    if (items != null && items.size() > 0) {
                        final ViewHolder holder = new ViewHolder();
                        ProfilesDeleteItem ei = (ProfilesDeleteItem)i;
                        v = vi.inflate(R.layout.vibrate_delete_list_item, null);
                        holder.title = (TextView)v.findViewById(R.id.name);
                        holder.chkbox = (CheckBox)v.findViewById(R.id.select_delete_item);
                        holder.chkbox.setOnCheckedChangeListener(mCheckBoxClickListener);
                        holder.listItemView = v.findViewById(R.id.vibrate_delete_list_view);
                        holder.listItemView.setOnClickListener(mListItemClickListener);
                        holder.chkbox.setVisibility(View.VISIBLE);
                        holder.chkbox.setId(ei.id);
                        holder.chkbox.setTag(position);
                        Log.d(TAG, "  " + position);
                        Log.d(TAG, "mCheckItemId[" + (position) + "] = " + mCheckItemId[position]);
                        holder.chkbox.setChecked(mCheckItemId[position]);
                        if (holder.title != null) {
                            holder.title.setText(ei.name);
                        }
                        holder.tag = TAG_ITEM;
                        v.setTag(holder);
                        holder.listItemView.setTag(holder);
                    }
                }
            } else {
                if (checkView == TAG_SECTION) {
                    ProfilesSectionItem si = (ProfilesSectionItem)i;
                    final TextView sectionView = (TextView)v
                            .findViewById(R.id.list_item_section_text);
                    sectionView.setText(si.getTitle());
                } else if (checkView == TAG_ITEM) {
                    if (null != i) {
                        ProfilesDeleteItem ei = (ProfilesDeleteItem)i;
                        ViewHolder holder = (ViewHolder)v.getTag();
                        if (holder.title != null) {
                            holder.title.setText(ei.name);
                        }
                        holder.chkbox.setId(ei.id);
                        holder.chkbox.setTag(position);
                        holder.chkbox.setChecked(mCheckItemId[position]);
                        v.setTag(holder);
                        holder.listItemView.setTag(holder);
                    }
                }
            }

            return v;
        }

        public int getCountCheckedItem() {
            chkcount = 0;
            for (int i = 0; i < mListViewCount; i++) {
                if (mCheckItemId[i] == true) {
                    chkcount++;
                }
            }
            return chkcount;
        }

        public final OnCheckedChangeListener mCheckBoxClickListener =
                new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // TODO Auto-generated method stub
                        if (isChecked) {
                            int mSelectedPosition = (Integer)buttonView.getTag();
                            deleteItemArray[mSelectedPosition] = buttonView.getId();
                            mCheckItemId[mSelectedPosition] = true;
                        } else {
                            for (int i = 0; i < mListViewCount; i++) {
                                if (deleteItemArray[i] == buttonView.getId()) {
                                    int mSelectedPosition = (Integer)buttonView.getTag();
                                    Log.d(TAG, "mSelectedPosition = " + mSelectedPosition);
                                    mCheckItemId[mSelectedPosition] = false;
                                    deleteItemArray[i] = NO_DELETE_ITEM;
                                }
                            }
                        }
                        setSelectedCount();
                    }
                };

        public void setSelectedCount() {
            chkcount = getCountCheckedItem();
            mTxtSelected.setText(
                    getString(R.string.sp_quiet_mode_contact_selected_number_NORMAL, chkcount));
            Log.d(TAG, "chkcount=" + chkcount + "   " + "mListViewCount=" + mListViewCount);
            if (chkcount == mListViewCount) {
                mChkboxSelectAll.setChecked(true);
            } else {
                mChkboxSelectAll.setChecked(false);
            }
            if (chkcount != 0) {
                mOk.setEnabled(true);
                mOk.setFocusable(true);
            } else {
                mOk.setEnabled(false);
                mOk.setFocusable(false);
            }
        }

        public final View.OnClickListener mListItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewHolder holder = (ViewHolder)view.getTag();

                CheckBox chkbox = holder.chkbox;
                Log.d(TAG, "chkbox.isChecked()=" + chkbox.isChecked());
                if (chkbox.isChecked()) {
                    chkbox.setChecked(false);
                } else {
                    chkbox.setChecked(true);
                }
            }
        };
    }
}
