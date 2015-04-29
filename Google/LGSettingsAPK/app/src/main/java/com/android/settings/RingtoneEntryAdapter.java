package com.android.settings;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.settings.R;

public class RingtoneEntryAdapter extends ArrayAdapter<RingtoneItem> {

    private ArrayList<RingtoneItem> items;
    private LayoutInflater vi;
    RingtoneEntryItem ei;

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

    /*
    public static interface Item {

        public boolean isSection();

    }
    */

    public static class RingtoneEntryItem implements RingtoneItem {

        public final String title;
        public final String uri;

        public RingtoneEntryItem(String title, String uri) {
            this.title = title;
            this.uri = uri;
        }

        @Override
        public boolean isSection() {
            return false;
        }

    }

    public RingtoneEntryAdapter(Context context, int resource, ArrayList items) {
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
                if (i.isSection() && (Integer)convertView.getTag() == 1) {
                    v = convertView;
                    checkView = 1;
                } else if (!i.isSection() && (Integer)convertView.getTag() == 2) {
                    v = convertView;
                    checkView = 2;
                } else {
                    convertView = null;
                    checkView = 0;
                }
            }
        }

        if (convertView == null) {
            if (i != null) {
                if (i.isSection()) {
                    RingtoneSectionItem si = (RingtoneSectionItem)i;
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
                    v.setTag(1);
                } else {
                    ei = (RingtoneEntryItem)i;
                    v = vi.inflate(R.layout.ringtone_list_item_entry, null);
                    final TextView title = (TextView)v
                            .findViewById(R.id.ringtone_list_item_entry_title);

                    if (title != null) {
                        title.setText(ei.title);
                    }
                    v.setTag(2);
                }
            }
        } else {
            if (checkView == 1) {
                RingtoneSectionItem si = (RingtoneSectionItem)i;
                final TextView sectionView = (TextView)v.findViewById(R.id.list_item_section_text);
                sectionView.setText(si.getTitle());
                v.setTag(1);
            } else if (checkView == 2) {
                ei = (RingtoneEntryItem)i;
                final TextView title = (TextView)v
                        .findViewById(R.id.ringtone_list_item_entry_title);
                if (title != null) {
                    title.setText(ei.title);
                }
                v.setTag(2);
            }
        }
        return v;
    }
}
