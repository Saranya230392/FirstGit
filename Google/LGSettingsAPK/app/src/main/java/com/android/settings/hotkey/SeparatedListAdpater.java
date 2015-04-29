package com.android.settings.hotkey;

import java.util.LinkedHashMap;
import java.util.Map;

import com.android.settings.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

public class SeparatedListAdpater extends BaseAdapter {

    public final static int TYPE_SECTION_HEADER = 1;

    public ArrayAdapter<String> mHeaders;
    public Map<String, Adapter> sections = new LinkedHashMap<String, Adapter>();

    public SeparatedListAdpater(Context context) {
        mHeaders = new ArrayAdapter<String>(context, R.layout.hotkey_item_header);
    }

    public void addSection(String section, Adapter adapter) {
        this.mHeaders.add(section);
        this.sections.put(section, adapter);
    }

    @Override
    public int getCount() {
        int total = 0;
        for (Adapter adapter : this.sections.values()) {
            total += adapter.getCount() + 1;
        }
        for (int i = 0; i < mHeaders.getCount(); i++) {
            String header = mHeaders.getItem(i);
            if ("NO_HEADER".equals(header)) {
                total--;
            }
        }

        return total;
    }

    @Override
    public Object getItem(int position) {
        for (Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;
            if ("NO_HEADER".equals(section)) {
                size--;
                if (position < size) {
                    return adapter.getItem(position);
                }
            } else {

                if (position == 0) {
                	break;
                }
                if (position < size) {
                    return adapter.getItem(position - 1);
                }
            }

            position -= size;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int sectionNum = 0;
        for (Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;
            if ("NO_HEADER".equals(section)) {
                size--;
                if (position < size) {
                    return adapter.getView(position, convertView, parent);
                }
            } else {
                if (position == 0) {
                    return mHeaders.getView(sectionNum, convertView, parent);
                }
                if (position < size) {
                    return adapter.getView(position - 1, convertView, parent);
                }
            }
            position -= size;
            sectionNum++;
        }
        return null;
    }

    public int getViewTypeCount() {
        int total = 1;
        for (Adapter adapter : this.sections.values()) {
            total += adapter.getViewTypeCount();
        }
        return total;
    }

    public int getItemViewType(int position) {
        int type = 1;
        for (Object section : this.sections.keySet()) {

            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1; // +1 : header
            if ("NO_HEADER".equals(section)) {
                size--;
                if (position < size) {
                    return adapter.getItemViewType(position);
                }
            } else {
                if (position == 0) {
                    return TYPE_SECTION_HEADER;
                }
                if (position < size) {
                    return type + adapter.getItemViewType(position - 1);
                }
            }
            position -= size;
            type += adapter.getViewTypeCount();
        }
        return -1;
    }

    public boolean areAllItemsSectable() {
        return false;
    }

    public boolean isEnabled(int position) {
        return (getItemViewType(position) != TYPE_SECTION_HEADER);
    }
}