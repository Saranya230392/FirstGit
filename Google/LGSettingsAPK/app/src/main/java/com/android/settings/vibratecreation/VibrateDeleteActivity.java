package com.android.settings.vibratecreation;

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

import android.net.Uri;
import android.database.Cursor;

import com.android.settings.Utils;
import com.android.settings.vibratecreation.VibrateDeleteActivity.VibrateDeleteItem;
import com.android.settings.vibratecreation.VibrateDeleteActivity.VibrateSectionItem;
import com.android.settings.vibratecreation.VibrateDeleteActivity.VibrateItem;

public class VibrateDeleteActivity extends ListActivity {

    static final String TAG = "VibrateDeleteActivity";

    private static final int DIALOG_DELETE_ITEM = 0;
    private static final int DIALOG_DELETE_MULTI_ITEM = 1;
    private static final int DIALOG_DELETE_ALL_ITEM = 2;
    private static final int DELETE_ONE_ITEM = 1;
    private static final int NO_DELETE_ITEM = -1;

    ArrayList<VibrateItem> items;

    private int[] deleteItemArray;
    private boolean[] mCheckItemId;

    //private Cursor mExternalCursor;
    private CheckBox mChkboxSelectAll;
    private ListView listView;
    private TextView mTxtSelected;
    private Button mOk;
    private Button mCancel;
    private LinearLayout mButtonLayout;

    int mListViewCount;
    int mParentType = -1;
    private int chkcount;
    private int mStaticItemCount = 0;

    private boolean mCompleteRead = false;

    VibrateDeleteAdapter mAdapter;
    private VibratePatternInfo mVibratePatternInfo;

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
        setContentView(R.layout.vibrate_list_del);

        //mRingtoneInfo = new RingtonePickerInfo(getApplicationContext());
        mVibratePatternInfo = new VibratePatternInfo(getApplicationContext(), 0);
        //Intent intent = getIntent();
        /*mParentType = intent.getIntExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, -1);
        boolean includeDrm = intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_INCLUDE_DRM,
                true);
        mRingtoneInfo.setIncludeDrm(includeDrm);
        mRingtoneInfo.setParentType(mParentType);
        if (mParentType != -1) {
            mRingtoneInfo.setFilterColumnsList();

        }
        */

        mCheckItemId = new boolean[mVibratePatternInfo.getUserPatternCount()];
        deleteItemArray = new int[mVibratePatternInfo.getUserPatternCount()];

        for (int i = 0; i < mVibratePatternInfo.getUserPatternCount(); i++) {
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
        for (int i = 0; i < mListViewCount; i++) {
            if (deleteItemArray[i] != NO_DELETE_ITEM) {
                VibrateDeleteItem item = (VibrateDeleteItem)items.get(i + mStaticItemCount);
                if (Utils.isSPRModel()) {
                    mVibratePatternInfo.mUserPatternManager.deleteVibrateUserPattern(item.name);
                } else {
                    mVibratePatternInfo.removeVibrateNameOthers(item.name);
                }
                mVibratePatternInfo.removeVibratePattern(item.name);
            }
        }
    }

    private void AllCheckDeleteItemArray() {
        Log.d(TAG, "AllCheckDeleteItemArray()");

        if (mVibratePatternInfo.getUserPatternCount() > 0) {
            for (int i = 0; i < mVibratePatternInfo.getUserPatternCount(); i++) {
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

        mStaticItemCount = 0;

        initVibrateList();
        initVibrateUI();

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

    private void initVibrateList() {
        items = new ArrayList<VibrateItem>();
        Log.d(TAG, "initVibrateList!!!");

        if (mVibratePatternInfo.getUserPatternCount() > 0) {
            items.add(new VibrateSectionItem(getString(R.string.my_vibrations_category)));
            //[S][Settings] [donghan07.lee] Add DRM policy 2012.02.14
            mStaticItemCount++;
            //[E][Settings] [donghan07.lee] Add DRM policy 2012.02.14

            mListViewCount = mVibratePatternInfo.getUserPatternCount();
            ArrayList<String> userPatternName = mVibratePatternInfo.getAllPatternName_User();
            for (int i = 0; i < mVibratePatternInfo.getUserPatternCount(); i++) {
                if (null != userPatternName) {
                    items.add(new VibrateDeleteItem(userPatternName.get(i),
                            mVibratePatternInfo.getUserPattern(userPatternName.get(i)), i));
                }
                //mCheckItemId[i] = false;
                //deleteItemArray[i] = NO_DELETE_ITEM;
            }
            mCompleteRead = true;
        }
    }

    private void initVibrateUI() {
        Log.d(TAG, "initVibrateUI!!!");
        mAdapter = new VibrateDeleteAdapter(this,
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
            return R.string.sp_deleteVibration_one_NORMAL;
        } else if (deleteType == DIALOG_DELETE_MULTI_ITEM) {
            return R.string.sp_deleteVibration_multi_NORMAL;
        } else {
            return R.string.sp_deleteVibration_multi_NORMAL;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder ab = null;
        int icon_res = android.R.attr.alertDialogIcon;
        if (Utils.isUI_4_1_model(getApplicationContext())) {
            icon_res = R.drawable.no_icon;
        }
        switch (id) {
        case DIALOG_DELETE_ITEM:
            ab = new AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setIcon(icon_res)
            .setPositiveButton(this.getResources().getString(R.string.yes), new OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    do_deleteItem();
                    finish();
                }
            })
            .setNegativeButton(this.getResources().getString(R.string.no), null)
            .setCancelable(true)
            .setOnCancelListener(null)
            .setMessage(getDeleteMessage(id));
            return ab.create();
        case DIALOG_DELETE_MULTI_ITEM:
            ab =  new AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setIcon(icon_res)
            .setPositiveButton(this.getResources().getString(R.string.yes), new OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    do_deleteItem();
                    finish();
                }
            })
            .setNegativeButton(this.getResources().getString(R.string.no), null)
            .setCancelable(true)
            .setOnCancelListener(null)
            .setMessage(getDeleteMessage(id));
            return ab.create();
        case DIALOG_DELETE_ALL_ITEM:
            ab = new AlertDialog.Builder(this)
            .setCancelable(true)
            .setTitle(R.string.delete)
            .setIcon(icon_res)
            .setPositiveButton(R.string.yes, new OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    do_deleteItem();
                    finish();
                }
            })
            .setNegativeButton(R.string.no, null)
            .setMessage(getDeleteMessage(id));
            return ab.create();
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

    public interface VibrateItem {
        public boolean isSection();
    }

    public static class VibrateSectionItem implements VibrateItem {

        private final String title;

        public VibrateSectionItem(String title) {
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

    public class VibrateDeleteItem implements VibrateItem {
        public final String name;
        public final String pattern;
        public final int id;

        public VibrateDeleteItem(String name, String pattern, int id) {
            this.id = id;
            this.name = name;
            this.pattern = pattern;
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

    public class VibrateDeleteAdapter extends ArrayAdapter<VibrateDeleteItem> {

        final static int TAG_SECTION = 1;
        final static int TAG_ITEM = 2;

        private ArrayList<VibrateItem> items;
        private LayoutInflater vi;

        public VibrateDeleteAdapter(Context context, int resource, ArrayList items) {
            super(context, resource, items);
            this.items = items;
            vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            int checkView = -1;

            final VibrateItem i = items.get(position);

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
                    VibrateSectionItem si = (VibrateSectionItem)i;
                    ViewHolder holder = new ViewHolder();
                    v = vi.inflate(R.layout.vibrate_delete_list_section, null);
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
                        final ViewHolder holder = new ViewHolder();
                        VibrateDeleteItem ei = (VibrateDeleteItem)i;
                        v = vi.inflate(R.layout.vibrate_delete_list_item, null);
                        holder.title = (TextView)v.findViewById(R.id.name);
                        holder.chkbox = (CheckBox)v.findViewById(R.id.select_delete_item);
                        holder.chkbox.setOnCheckedChangeListener(mCheckBoxClickListener);
                        holder.listItemView = v.findViewById(R.id.vibrate_delete_list_view);
                        holder.listItemView.setOnClickListener(mListItemClickListener);

                        holder.chkbox.setVisibility(View.VISIBLE);
                        holder.chkbox.setId(ei.id);
                        holder.chkbox.setTag(position - mStaticItemCount);
                        Log.d(TAG, "mCheckItemId[" + (position - mStaticItemCount) + "] = "
                                + mCheckItemId[position - mStaticItemCount]);
                        holder.chkbox.setChecked(mCheckItemId[position - mStaticItemCount]);

                        if (holder.title != null) {
                            holder.title.setText(ei.name);
                        }
                        holder.tag = TAG_ITEM;

                        v.setTag(holder);
                        holder.listItemView.setTag(holder);
                    }
                }
            } else {
                if (i != null) {
                    if (checkView == TAG_SECTION) {
                        VibrateSectionItem si = (VibrateSectionItem)i;
                        final TextView sectionView = (TextView)v
                                .findViewById(R.id.list_item_section_text);
                        if (si != null) {
                            sectionView.setText(si.getTitle());
                        }
                    } else if (checkView == TAG_ITEM) {
                        VibrateDeleteItem ei = (VibrateDeleteItem)i;
                        ViewHolder holder = (ViewHolder)v.getTag();
                        if (holder.title != null && ei != null) {
                            holder.title.setText(ei.name);
                        }
                        if (ei != null) {
                            holder.chkbox.setId(ei.id);
                        }
                        holder.chkbox.setTag(position - mStaticItemCount);
                        holder.chkbox.setChecked(mCheckItemId[position - mStaticItemCount]);

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
                Log.d(TAG, "chkbox.isChecked()=" + chkbox.isChecked());
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
