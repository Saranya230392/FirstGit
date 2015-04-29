package com.android.settings.applications;

import com.android.settings.R;
import com.android.settings.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

// View Holder used when displaying views
public class AppViewHolder {
    public ApplicationsState.AppEntry entry;
    public View rootView;
    public TextView appName;
    public ImageView appIcon;
    public TextView appSize;
    public TextView disabled;
    public CheckBox checkBox;
    public TextView appDate;
    public CheckBox appCheckBox;

    static final int LIST_LAYOUT_TYPE_NORMAL = 1;
    static final int LIST_LAYOUT_TYPE_DELETE = 2;
    static final int LIST_LAYOUT_TYPE_RECOVER = 3;

    static public AppViewHolder createOrRecycle(LayoutInflater inflater, View convertView) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.manage_applications_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            AppViewHolder holder = new AppViewHolder();
            if (convertView != null) {
                holder.rootView = convertView;
                holder.appName = (TextView)convertView.findViewById(R.id.app_name);
                holder.appIcon = (ImageView)convertView.findViewById(R.id.app_icon);
                holder.appSize = (TextView)convertView.findViewById(R.id.app_size);
                holder.disabled = (TextView)convertView.findViewById(R.id.app_disabled);
                holder.checkBox = (CheckBox)convertView.findViewById(R.id.app_on_sdcard);
                holder.appDate = (TextView)convertView.findViewById(R.id.app_date);
                holder.appCheckBox = (CheckBox)convertView.findViewById(R.id.app_on_check);
                convertView.setTag(holder);
                return holder;
            } else {
                return null;
            }
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            return (AppViewHolder)convertView.getTag();
        }
    }

    static public AppViewHolder createOrRecycle(LayoutInflater inflater,
            View convertView, int listType) {
        if (convertView == null) {
            switch (listType) {
            case LIST_LAYOUT_TYPE_NORMAL:
                convertView = inflater.inflate(R.layout.manage_applications_item, null);
                break;
            case LIST_LAYOUT_TYPE_DELETE:
                convertView = inflater
                        .inflate(R.layout.manage_applications_multi_delete_item, null);
                break;
            case LIST_LAYOUT_TYPE_RECOVER:
                convertView = inflater.inflate(R.layout.manage_applications_recover_item, null);
                break;
            default:
                break;
            }
            AppViewHolder holder = new AppViewHolder();
            if (convertView != null) {
                holder.rootView = convertView;
                holder.appName = (TextView)convertView.findViewById(R.id.app_name);
                holder.appIcon = (ImageView)convertView.findViewById(R.id.app_icon);
                holder.appSize = (TextView)convertView.findViewById(R.id.app_size);
                holder.disabled = (TextView)convertView.findViewById(R.id.app_disabled);
                holder.checkBox = (CheckBox)convertView.findViewById(R.id.app_on_check);
                holder.appDate = (TextView)convertView.findViewById(R.id.app_date);
                convertView.setTag(holder);
                return holder;
            } else {
                return null;
            }
        } else {
            return (AppViewHolder)convertView.getTag();
        }
    }

    void updateSizeText(CharSequence invalidSizeStr, int whichSize, int mLastSortMode) {
        if (ManageApplications.DEBUG) {
            Log.i(ManageApplications.TAG, "updateSizeText of " + entry.label + " " + entry
                    + ": " + entry.sizeStr);
        }
        if (entry.sizeStr != null) {
            switch (whichSize) {
            case ManageApplications.SIZE_INTERNAL:
                appSize.setText(Utils.onLGFormatter(entry.internalSizeStr));
                break;
            case ManageApplications.SIZE_EXTERNAL:
                appSize.setText(Utils.onLGFormatter(entry.externalSizeStr));
                break;
            default:
                appSize.setText(Utils.onLGFormatter(entry.sizeStr));
                break;
            }
        } else if (entry.size == ApplicationsState.SIZE_INVALID) {
            appSize.setText(invalidSizeStr);
        }
        //        if (mLastSortMode == ManageApplications.SORT_ORDER_DATE)
        {
            appDate.setText(entry.installed);
        }
    }
}