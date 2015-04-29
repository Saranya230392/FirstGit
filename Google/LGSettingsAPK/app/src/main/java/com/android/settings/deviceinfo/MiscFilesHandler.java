/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.deviceinfo;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageVolume;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.StorageMeasurement.FileInfo;
import com.android.settings.lgesetting.Config.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.ActivityManager;
import android.os.Environment.UserEnvironment;

/**
 * This class handles the selection and removal of Misc files.
 */
public class MiscFilesHandler extends ListActivity {
    private static final String TAG = "MemorySettings";
    private String mNumSelectedFormat;
    private String mNumBytesSelectedFormat;
    private MemoryMearurementAdapter mAdapter;
    private LayoutInflater mInflater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setFinishOnTouchOutside(true);
        setTitle(R.string.misc_files);
        mNumSelectedFormat = getString(R.string.misc_files_selected_count);
        mNumBytesSelectedFormat = getString(R.string.misc_files_selected_count_bytes);
        mAdapter = new MemoryMearurementAdapter(this);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setContentView(R.layout.settings_storage_miscfiles_list);
        ListView lv = getListView();
        lv.setItemsCanFocus(true);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(new ModeCallback(this));
        setListAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            getMenuInflater().inflate(R.menu.settings_search, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }   
    
    private class ModeCallback implements ListView.MultiChoiceModeListener {
        private int mDataCount;
        private final Context mContext;

        public ModeCallback(Context context) {
            mContext = context;
            mDataCount = mAdapter.getCount();
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            final MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.misc_files_menu, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            ListView lv = getListView();
            switch (item.getItemId()) {
            case R.id.action_delete:
                // delete the files selected
                SparseBooleanArray checkedItems = lv.getCheckedItemPositions();
                int checkedCount = getListView().getCheckedItemCount();
                if (checkedCount > mDataCount) {
                    throw new IllegalStateException("checked item counts do not match. " +
                            "checkedCount: " + checkedCount + ", dataSize: " + mDataCount);
                }
                if (mDataCount > 0) {
                    ArrayList<Object> toRemove = new ArrayList<Object>();
                    for (int i = 0; i < mDataCount; i++) {
                        if (!checkedItems.get(i)) {
                            //item not selected
                            continue;
                        }
                        if (StorageMeasurement.LOGV) {
                            Log.i(TAG, "deleting: " + mAdapter.getItem(i));
                        }
                        // delete the file
                        File file = new File(mAdapter.getItem(i).mFileName);
                        if (file.isDirectory()) {
                            deleteDir(file);
                        } else {
                            file.delete();
                        }
                        toRemove.add(mAdapter.getItem(i));
                    }
                    mAdapter.removeAll(toRemove);
                    mAdapter.notifyDataSetChanged();
                    mDataCount = mAdapter.getCount();
                    final int currentUser = ActivityManager.getCurrentUser();
                    final UserEnvironment currentEnv = new UserEnvironment(currentUser);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                            + currentEnv.getExternalStorageDirectory())));
                    //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
                }
                mode.finish();
                break;

            case R.id.action_select_all:
                // check ALL items
                if (!Utils.isUI_4_1_model(mContext)) {
                    for (int i = 0; i < mDataCount; i++) {
                        if (i == 0 && Config.getOperator().equals(Config.VZW)) {
                            continue;
                        }
                        lv.setItemChecked(i, true);
                    }
                } else {
                    for (int i = 0; i < mDataCount; i++) {
                        if (i == 0) {
                            continue;
                        }
                        lv.setItemChecked(i, true);
                    }
                }
                // update the title and subtitle with number selected and numberBytes selected
                onItemCheckedStateChanged(mode, 1, 0, true);
                break;
            default:
                break;
            }
            return true;
        }

        // Deletes all files and subdirectories under given dir.
        // Returns true if all deletions were successful.
        // If a deletion fails, the method stops attempting to delete and returns false.
        private boolean deleteDir(File dir) {

            String[] children = dir.list();
            // [S][2012.01.03][hyoungjun21.lee@lge.com][Common] Fix WBT issues
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }

                }
                // [E][2012.01.03][hyoungjun21.lee@lge.com][Common] Fix WBT issues
            }
            // The directory is now empty so delete it
            return dir.delete();
        }

        public void onDestroyActionMode(ActionMode mode) {
            // This block intentionally left blank
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                boolean checked) {
            ListView lv = getListView();
            int numChecked = lv.getCheckedItemCount();
            mode.setTitle(String.format(mNumSelectedFormat, numChecked, mAdapter.getCount()));

            // total the sizes of all items selected so far
            SparseBooleanArray checkedItems = lv.getCheckedItemPositions();
            long selectedDataSize = 0;
            if (numChecked > 0) {
                for (int i = 0; i < mDataCount; i++) {
                    if (checkedItems.get(i)) {
                        // item is checked
                        selectedDataSize += mAdapter.getItem(i).mSize;
                    }
                }
            }
            mode.setSubtitle(String.format(mNumBytesSelectedFormat,
                    Formatter.formatFileSize(mContext, selectedDataSize),
                    Formatter.formatFileSize(mContext, mAdapter.getDataSize())));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.search) {
            if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
                Intent search_intent = new Intent();
                search_intent.setAction("com.lge.settings.SETTINGS_SEARCH");
                search_intent.putExtra("search", true);
                startActivity(search_intent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class MemoryMearurementAdapter extends BaseAdapter {
        private ArrayList<StorageMeasurement.FileInfo> mData = null;
        private long mDataSize = 0;
        private Context mContext;

        public MemoryMearurementAdapter(Activity activity) {
            mContext = activity;
            final StorageVolume storageVolume = activity.getIntent().getParcelableExtra(
                    StorageVolume.EXTRA_STORAGE_VOLUME);
            StorageMeasurement mMeasurement = StorageMeasurement.getInstance(
                    activity, storageVolume);
            if (mMeasurement == null) {
                return;
            }
            mData = (ArrayList<StorageMeasurement.FileInfo>)mMeasurement.mFileInfoForMisc;
            if (mData != null) {
                boolean isSystemData = false;

                // [seungyeop.yeom][2013-11-26] make sure that there is a named System Data.
                for (int i = 0; i < mData.size(); i++) {
                    if (mData.get(i).getFileName().equals("System Data")) {
                        isSystemData = true;
                        Log.d("YSY", "for isSystemData : " + isSystemData);
                    }
                }
                // [seungyeop.yeom] end

                if (!Utils.isUI_4_1_model(mContext)) {
                    if (Config.getOperator().equals(Config.VZW) && !isSystemData) {
                        Log.d("YSY", "VZW isSystemData : " + isSystemData);
                        mData.add(new FileInfo("System Data",
                                StorageVolumePreferenceCategory.miscForSystemData, 0));
                        Collections.swap(mData, mData.size() - 1, 0);
                    }
                } else {
                    if (!isSystemData) {
                        Log.d("YSY", "VZW isSystemData : " + isSystemData);
                        mData.add(new FileInfo("System Data",
                                StorageVolumePreferenceCategory.miscForSystemData, 0));
                        Collections.swap(mData, mData.size() - 1, 0);
                    }
                }

                for (StorageMeasurement.FileInfo info : mData) {
                    mDataSize += info.mSize;
                }
            }
        }

        @Override
        public int getCount() {
            return (mData == null) ? 0 : mData.size();
        }

        @Override
        public StorageMeasurement.FileInfo getItem(int position) {
            if (mData == null || mData.size() <= position) {
                return null;
            }
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            if (mData == null || mData.size() <= position) {
                return 0;
            }
            return mData.get(position).mId;
        }

        public void removeAll(List<Object> objs) {
            if (mData == null) {
                return;
            }
            for (Object o : objs) {
                mData.remove(o);
                mDataSize -= ((StorageMeasurement.FileInfo)o).mSize;
            }
        }

        public long getDataSize() {
            return mDataSize;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final FileItemInfoLayout view = (convertView == null) ?
                    (FileItemInfoLayout)mInflater.inflate(R.layout.settings_storage_miscfiles,
                            parent, false) : (FileItemInfoLayout)convertView;
            FileInfo item = getItem(position);
            // [seungyeop.yeom][2013-11-28][STRAT] static analysis
            if (item != null) {
                view.setFileName(item.mFileName);
                view.setFileSize(Formatter.formatFileSize(mContext, item.mSize));
            }
            // [seungyeop.yeom][END]

            // [START][seungyeop.yeom][2014-05-09] modify condition of misc file list
            if (!Utils.isUI_4_1_model(mContext)) {
                if (Config.getOperator().equals(Config.VZW)) {
                    if (view.getFileName().equals("System Data")) {
                        ((CheckBox)view.findViewById(R.id.misc_checkbox))
                                .setVisibility(View.VISIBLE);
                        ((CheckBox)view.findViewById(R.id.misc_checkbox)).setEnabled(false);
                        view.setEnabled(false);
                    } else {
                        ((CheckBox)view.findViewById(R.id.misc_checkbox))
                                .setVisibility(View.VISIBLE);
                        ((CheckBox)view.findViewById(R.id.misc_checkbox)).setEnabled(true);
                        view.setEnabled(true);
                    }
                }
            } else {
                Log.d("YSY", "view.getFileName() : " + view.getFileName());

                if (view.getFileName().equals("System Data")) {
                    ((CheckBox)view.findViewById(R.id.misc_checkbox)).setVisibility(View.VISIBLE);
                    ((CheckBox)view.findViewById(R.id.misc_checkbox)).setEnabled(false);
                    view.setEnabled(false);
                } else {
                    ((CheckBox)view.findViewById(R.id.misc_checkbox))
                            .setVisibility(View.VISIBLE);
                    ((CheckBox)view.findViewById(R.id.misc_checkbox)).setEnabled(true);
                    view.setEnabled(true);
                }
            }
            // [END]

            final ListView listView = (ListView)parent;
            final int listPosition = position;
            view.getCheckBox().setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    listView.setItemChecked(listPosition, isChecked);
                }

            });
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listView.getCheckedItemCount() > 0) {
                        return false;
                    }
                    listView.setItemChecked(listPosition, !view.isChecked());
                    return true;
                }
            });
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //if (listView.getCheckedItemCount() > 0) {
                    listView.setItemChecked(listPosition, !view.isChecked());
                    //}
                }
            });
            return view;
        }
    }
}