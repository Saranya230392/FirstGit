package com.android.settings;

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

import android.media.Ringtone;
//import android.media.RingtoneManager;
import com.lge.media.RingtoneManagerEx;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton.OnCheckedChangeListener;

import android.provider.MediaStore;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.RingtonePickerInfo;
import com.android.settings.RingtoneDeleteActivity.RingtoneDeleteItem;
import com.android.settings.RingtoneDeleteActivity.RingtoneSectionItem;
import com.android.settings.RingtoneDeleteActivity.RingtoneItem;

import android.net.Uri;
import android.database.Cursor;

public class RingtoneDeleteActivity extends ListActivity {

    static final String TAG = "RingtoneDeleteActivity";

    private static final int DIALOG_DELETE_ITEM = 0;
    private static final int DIALOG_DELETE_MULTI_ITEM = 1;
    private static final int DIALOG_DELETE_ALL_ITEM = 2;
    private static final int DELETE_ONE_ITEM = 1;
    private static final int NO_DELETE_ITEM = -1;

    ArrayList<RingtoneItem> items;

    private int[] deleteItemArray;
    private boolean[] mCheckItemId;

    private Cursor mExternalCursor;
    private CheckBox mChkboxSelectAll;
    private ListView listView;
    private TextView mTxtSelected;
    private Button mOk;
    private Button mCancel;
    private LinearLayout mButtonLayout;

    int mListViewCount;
    int mParentType = -1;
    private int chkcount;
    private boolean mCompleteRead = false;

    private int mStaticItemCount;

    RingtonePickerInfo mRingtoneInfo;
    RingtoneDeleteAdapter mAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            MenuInflater inflater_search = getMenuInflater();
            inflater_search.inflate(R.menu.settings_search, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            if (item.getItemId() == R.id.search) {
                Intent search_intent = new Intent();
                search_intent.setAction("com.lge.settings.SETTINGS_SEARCH");
                search_intent.putExtra("search", true);
                startActivity(search_intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInsttatnceState) {
        super.onCreate(savedInsttatnceState);
        setContentView(R.layout.ringtone_list_del);

        mRingtoneInfo = new RingtonePickerInfo(getApplicationContext());

        Intent intent = getIntent();
        mParentType = intent.getIntExtra(RingtoneManagerEx.EXTRA_RINGTONE_TYPE, -1);
        boolean includeDrm = intent.getBooleanExtra(RingtoneManagerEx.EXTRA_RINGTONE_INCLUDE_DRM,
                true);
        mRingtoneInfo.setIncludeDrm(includeDrm);
        mRingtoneInfo.setParentType(mParentType);
        if (mParentType != -1) {
            mRingtoneInfo.setFilterColumnsList();
        }

        mExternalCursor = mRingtoneInfo.getCursor(mRingtoneInfo.EXTERNAL_CURSOR_TYPE);

        mCheckItemId = new boolean[mExternalCursor.getCount()];
        deleteItemArray = new int[mExternalCursor.getCount()];

        for (int i = 0; i < mExternalCursor.getCount(); i++) {
            mCheckItemId[i] = false;
            deleteItemArray[i] = NO_DELETE_ITEM;
        }
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
        ContentResolver resolver = getContentResolver();
        for (int i = 0; i < mListViewCount; i++) {
            if (deleteItemArray[i] != NO_DELETE_ITEM) {
                RingtoneDeleteItem item = (RingtoneDeleteItem)items.get(i + mStaticItemCount);
                Uri ringUri = Uri.parse(item.uri);
                if (mParentType == RingtonePickerInfo.TYPE_RINGTONE ||
                        mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM2 ||
                        mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM3 ||
                        mParentType == RingtonePickerInfo.TYPE_RINGTONE_VC) {
                    try {
                        ContentValues values = new ContentValues(2);
                        values.put(MediaStore.Audio.Media.IS_RINGTONE, "0");
                        resolver.update(ringUri, values, null, null);
                    } catch (UnsupportedOperationException ex) {
                        // most likely the card just got unmounted
                        // Log.d("couldn't set ringtone flag for id " + id);
                        return;
                    }
                }
                else if (mParentType == RingtonePickerInfo.TYPE_NOTIFICATION ||
                        mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM2 ||
                        mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM3) {
                    try {
                        ContentValues values = new ContentValues(2);
                        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, "0");
                        //values.put(MediaStore.Audio.Media.IS_ALARM, "1");
                        resolver.update(ringUri, values, null, null);
                    } catch (UnsupportedOperationException ex) {
                        // most likely the card just got unmounted
                        // Log.d("couldn't set ringtone flag for id " + id);
                        return;
                    }
                }
                do_deleteItem_otherSIM(ringUri);
            }
        }
    }

    private void do_deleteItem_otherSIM(Uri ringUri) {
        if (ringUri.toString().equals(
            RingtonePickerInfo.do_getURI(RingtonePickerInfo.TYPE_RINGTONE))) {
            RingtonePickerInfo.setParentType(RingtonePickerInfo.TYPE_RINGTONE);
            RingtonePickerInfo.do_setURI(RingtonePickerInfo.getDefaultPhoneRingtone(), RingtonePickerInfo.TYPE_RINGTONE);
        }
        if (ringUri.toString().equals(
            RingtonePickerInfo.do_getURI(RingtonePickerInfo.TYPE_RINGTONE_SIM2))) {
            RingtonePickerInfo.setParentType(RingtonePickerInfo.TYPE_RINGTONE_SIM2);
            RingtonePickerInfo.do_setURI(RingtonePickerInfo.getDefaultPhoneRingtone(), RingtonePickerInfo.TYPE_RINGTONE_SIM2);
        }
        if (ringUri.toString().equals(
            RingtonePickerInfo.do_getURI(RingtonePickerInfo.TYPE_RINGTONE_SIM3))) {
            RingtonePickerInfo.setParentType(RingtonePickerInfo.TYPE_RINGTONE_SIM3);
            RingtonePickerInfo.do_setURI(RingtonePickerInfo.getDefaultPhoneRingtone(), RingtonePickerInfo.TYPE_RINGTONE_SIM3);
        }
        if (ringUri.toString().equals(
            RingtonePickerInfo.do_getURI(RingtonePickerInfo.TYPE_RINGTONE_VC))) {
            RingtonePickerInfo.setParentType(RingtonePickerInfo.TYPE_RINGTONE_VC);
            RingtonePickerInfo.do_setURI(RingtonePickerInfo.getDefaultPhoneRingtone(), RingtonePickerInfo.TYPE_RINGTONE_VC);
        }
        if (ringUri.toString().equals(
            RingtonePickerInfo.do_getURI(RingtonePickerInfo.TYPE_NOTIFICATION))) {
            RingtonePickerInfo.setParentType(RingtonePickerInfo.TYPE_NOTIFICATION);
            RingtonePickerInfo.do_setURI(RingtonePickerInfo.getDefaultPhoneRingtone(), RingtonePickerInfo.TYPE_NOTIFICATION);
        }
        if (ringUri.toString().equals(
            RingtonePickerInfo.do_getURI(RingtonePickerInfo.TYPE_NOTIFICATION_SIM2))) {
            RingtonePickerInfo.setParentType(RingtonePickerInfo.TYPE_NOTIFICATION_SIM2);
            RingtonePickerInfo.do_setURI(RingtonePickerInfo.getDefaultPhoneRingtone(), RingtonePickerInfo.TYPE_NOTIFICATION_SIM2);
        }
        if (ringUri.toString().equals(
            RingtonePickerInfo.do_getURI(RingtonePickerInfo.TYPE_NOTIFICATION_SIM3))) {
            RingtonePickerInfo.setParentType(RingtonePickerInfo.TYPE_NOTIFICATION_SIM3);
            RingtonePickerInfo.do_setURI(RingtonePickerInfo.getDefaultPhoneRingtone(), RingtonePickerInfo.TYPE_NOTIFICATION_SIM3);
        }

        RingtonePickerInfo.setParentType(mParentType);
    }

    private void AllCheckDeleteItemArray() {

        Log.d(TAG, "AllCheckDeleteItemArray()");
        final int ID_COLUMN_INDEX = 0;
        int count = 0;

        int sValue = NO_DELETE_ITEM;

        if (mExternalCursor != null) {
            try {
                mExternalCursor.moveToFirst();
                while (mExternalCursor != null) {
                    sValue = mExternalCursor.getInt(ID_COLUMN_INDEX);
                    deleteItemArray[count] = sValue;
                    Log.e(TAG, "count = " + count + " deleteItemArray[]= " + deleteItemArray[count]);
                    count++;
                    mExternalCursor.moveToNext();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.i(TAG, "ArrayIndexOutOfBoundsException!!!");
            } catch (NullPointerException e) {
                Log.i(TAG, "NullPointerException!!!");
            }
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d(TAG, "onResume!!!");

        mStaticItemCount = 0;

        initRingtoneList();
        initRingtoneUI();

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
            Log.d(TAG, "mChkboxSelectAll!!!" + mChkboxSelectAll);
            if (mChkboxSelectAll != null) {
                if (mListViewCount == 0) {
                    mChkboxSelectAll.setEnabled(false);
                    mChkboxSelectAll.setFocusable(false);
                } else {
                    mChkboxSelectAll.setEnabled(true);
                    mChkboxSelectAll.setFocusable(true);
                }
            }

            if (mChkboxSelectAll == null) {
                return;
            }

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

    private void initRingtoneList() {
        items = new ArrayList<RingtoneItem>();
        Log.d(TAG, "initRingtoneList!!!");

        String my_ring_category = "";
        if (mParentType == RingtonePickerInfo.TYPE_RINGTONE ||
                mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM2 ||
                mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM3 ||
                mParentType == RingtonePickerInfo.TYPE_RINGTONE_VC) {
            my_ring_category = getString(R.string.sp_my_ringtones_NORMAL);
        }
        //for Notification ringtone
        else if (mParentType == RingtonePickerInfo.TYPE_NOTIFICATION ||
                mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM2 ||
                mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM3) {
            my_ring_category = getString(R.string.my_notification_sound);
        }

        if (mExternalCursor == null) {
            mExternalCursor = mRingtoneInfo.getCursor(mRingtoneInfo.EXTERNAL_CURSOR_TYPE);
        }

        // [S][WBT] null exception
        if (mExternalCursor != null) {
            if (mExternalCursor.getCount() > 0) {
                mExternalCursor.moveToFirst();
                items.add(new RingtoneSectionItem(
                        my_ring_category));
                //[S][Settings] [donghan07.lee] Add DRM policy 2012.02.14
                mStaticItemCount++;
                //[E][Settings] [donghan07.lee] Add DRM policy 2012.02.14

                mListViewCount = mExternalCursor.getCount();
                mExternalCursor.moveToFirst();
                Log.d(TAG, "mExternalCursor.getCount() = " + mExternalCursor.getCount());
                for (int i = 0; i < mExternalCursor.getCount(); i++) {
                    items.add(new RingtoneDeleteItem(mExternalCursor
                            .getString(1), mExternalCursor.getString(2) + "/"
                            + mExternalCursor.getString(0), mExternalCursor.getInt(0)));
                    mExternalCursor.moveToNext();
                    //mCheckItemId[i] = false;
                    //deleteItemArray[i] = NO_DELETE_ITEM;
                }
                mCompleteRead = true;
            }
        }
    }

    private void initRingtoneUI() {
        Log.d(TAG, "initRingtoneUI!!!");
        mAdapter = new RingtoneDeleteAdapter(this,
                R.layout.ringtone_delete_list_item, items);
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
        if ((mParentType == mRingtoneInfo.TYPE_RINGTONE) ||
                (mParentType == mRingtoneInfo.TYPE_RINGTONE_SIM2) ||
                (mParentType == mRingtoneInfo.TYPE_RINGTONE_SIM3) ||
                (mParentType == mRingtoneInfo.TYPE_RINGTONE_VC)) {
            if (deleteType == DIALOG_DELETE_ITEM
                    || mAdapter.getCountCheckedItem() == DELETE_ONE_ITEM) {
                return R.string.sp_deleteRingtone_NORMAL;
            } else if (deleteType == DIALOG_DELETE_MULTI_ITEM) {
                return R.string.sp_deleteRingtone_multi_NORMAL;
            } else {
                return R.string.sp_deleteRingtone_multi_NORMAL;
            }
        } else {
            if (deleteType == DIALOG_DELETE_ITEM
                    || mAdapter.getCountCheckedItem() == DELETE_ONE_ITEM) {
                return R.string.sp_deleteNotification_one_NORMAL;
            } else if (deleteType == DIALOG_DELETE_MULTI_ITEM) {
                return R.string.sp_deleteNotification_multi_NORMAL;
            } else {
                return R.string.sp_deleteNotification_multi_NORMAL;
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        int icon_res = android.R.attr.alertDialogIcon;
        if (Utils.isUI_4_1_model(this)) {
            icon_res = R.drawable.no_icon;
        }
        switch (id) {
        case DIALOG_DELETE_ITEM:
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.delete)
                    .setIconAttribute(icon_res)
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
                    .setIconAttribute(icon_res)
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
                    .setIconAttribute(icon_res)
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
        if (mExternalCursor != null || items != null) {
            try {
                mExternalCursor = null;
                items = null;
            } catch (Exception e) {
                Log.w(TAG, "Exception");
            }
        }
    }

    public interface RingtoneItem {
        public boolean isSection();
    }

    public static class RingtoneSectionItem implements RingtoneItem {

        private final String title;

        public RingtoneSectionItem(String title) {
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

    public class RingtoneDeleteItem implements RingtoneItem {
        public final String title;
        public final String uri;
        public final int id;

        public RingtoneDeleteItem(String title, String uri, int id) {
            this.id = id;
            this.title = title;
            this.uri = uri;
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

    public class RingtoneDeleteAdapter extends ArrayAdapter<RingtoneDeleteItem> {

        final static int TAG_SECTION = 1;
        final static int TAG_ITEM = 2;
        private ArrayList<RingtoneItem> items;
        private LayoutInflater vi;

        public RingtoneDeleteAdapter(Context context, int resource, ArrayList items) {
            super(context, resource, items);
            this.items = items;
            vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = null;
            int checkView = -1;

            final RingtoneItem i = items.get(position);

            if (convertView != null) {
                if (i != null) {
                    ViewHolder holder = new ViewHolder();
                    holder = (ViewHolder)convertView.getTag();
                    if (i.isSection() && holder.tag == TAG_SECTION) {
                        v = convertView;
                        checkView = 1;
                    } else if (!i.isSection() && holder.tag == TAG_ITEM) {
                        v = convertView;
                        checkView = 2;
                    } else {
                        convertView = null;
                        checkView = 0;
                    }
                }
            }

            if (convertView == null) {
                if (i.isSection()) {
                    RingtoneSectionItem si = (RingtoneSectionItem)i;
                    ViewHolder holder = new ViewHolder();
                    v = vi.inflate(R.layout.ringtone_list_item_section, null);
                    v.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View arg0) {
                        }
                    });
                    v.setOnLongClickListener(null);
                    v.setLongClickable(false);

                    final TextView sectionView = (TextView)v
                            .findViewById(R.id.list_item_section_text);
                    sectionView.setText(si.getTitle());
                    holder.tag = TAG_SECTION;
                    v.setTag(holder);
                } else {
                    if (items != null && items.size() > mStaticItemCount) {
                        ViewHolder holder = new ViewHolder();
                        RingtoneDeleteItem ei = (RingtoneDeleteItem)i;
                        v = vi.inflate(R.layout.ringtone_delete_list_item, null);
                        holder.title = (TextView)v.findViewById(R.id.name);
                        holder.chkbox = (CheckBox)v.findViewById(R.id.select_delete_item);
                        holder.chkbox.setOnCheckedChangeListener(mCheckBoxClickListener);
                        holder.listItemView = v.findViewById(R.id.ringtone_delete_list_view);
                        holder.listItemView.setOnClickListener(mListItemClickListener);
                        holder.chkbox.setVisibility(View.VISIBLE);
                        holder.chkbox.setId(ei.id);
                        holder.chkbox.setTag(position - mStaticItemCount);
                        holder.chkbox.setChecked(mCheckItemId[position - mStaticItemCount]);

                        if (holder.title != null) {
                            holder.title.setText(ei.title);
                        }
                        holder.tag = TAG_ITEM;
                        holder.listItemView.setTag(holder);
                        v.setTag(holder);

                    }
                }
            } else {
                if (checkView == 1) {
                    RingtoneSectionItem si = (RingtoneSectionItem)i;
                    final TextView sectionView = (TextView)v
                            .findViewById(R.id.list_item_section_text);
                    sectionView.setText(si.getTitle());
                } else if (checkView == 2) {
                    RingtoneDeleteItem ei = (RingtoneDeleteItem)i;
                    ViewHolder holder = (ViewHolder)v.getTag();
                    if (holder.title != null) {
                        holder.title.setText(ei.title);
                    }
                    holder.chkbox.setId(ei.id);
                    holder.chkbox.setTag(position - mStaticItemCount);
                    holder.chkbox.setChecked(mCheckItemId[position - mStaticItemCount]);
                    holder.listItemView.setTag(holder);
                    v.setTag(holder);

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

        public final OnCheckedChangeListener mCheckBoxClickListener = new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    int mSelectedPosition = (Integer)buttonView.getTag();
                    Log.d(TAG, "[onCheckedChanged][mSelectedPosition] = " + mSelectedPosition);
                    deleteItemArray[mSelectedPosition] = buttonView.getId();
                    mCheckItemId[mSelectedPosition] = true;
                }
                else {
                    for (int i = 0; i < mListViewCount; i++) {
                        Log.i(TAG, "deleteItemArray= " + deleteItemArray[i]
                                + " buttonView.getId() = " + buttonView.getId());
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
            mTxtSelected.setText(getString(R.string.sp_quiet_mode_contact_selected_number_NORMAL,
                    chkcount));
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
                if (chkbox.isChecked()) {
                    chkbox.setChecked(false);
                } else {
                    chkbox.setChecked(true);
                }
            }
        };
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        ViewHolder holder = (ViewHolder)v.getTag();
        CheckBox ch = holder.chkbox;

        if (ch.isChecked()) {
            ch.setChecked(false);
        } else {
            ch.setChecked(true);
        }
    }
}
