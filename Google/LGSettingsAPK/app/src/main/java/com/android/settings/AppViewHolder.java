package com.android.settings;

import com.android.settings.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

// View Holder used when displaying views
public class AppViewHolder {
    public View rootView;
    public TextView text1;
    public ProgressBar progress;
    public ImageView appIcon;
    public TextView title;

    static public AppViewHolder createOrRecycle(LayoutInflater inflater, View convertView) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.data_usage_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            AppViewHolder holder = new AppViewHolder();
            holder.rootView = convertView;
            holder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
            holder.progress = (ProgressBar) convertView.findViewById(android.R.id.progress);
            holder.appIcon = (ImageView) convertView.findViewById(android.R.id.icon);
            holder.title = (TextView) convertView.findViewById(android.R.id.title);
            convertView.setTag(holder);
            return holder;
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            return (AppViewHolder)convertView.getTag();
        }
    }
}