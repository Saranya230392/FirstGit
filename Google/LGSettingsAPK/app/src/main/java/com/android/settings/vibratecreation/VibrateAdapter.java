package com.android.settings.vibratecreation;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import com.android.settings.R;

abstract public class VibrateAdapter extends BaseAdapter {

    abstract protected View getHeaderView(String caption, int index, View convertView,
            ViewGroup parent);

    private List<Section> sections = new ArrayList<Section>();
    private static int TYPE_SECTION_HEADER = 1;
    private final static String TAG = "VibrateAdapter";

    public VibrateAdapter() {
        super();
    }

    public void addSection(String caption, Adapter adapter) {
        sections.add(new Section(caption, adapter));
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        int total = 0;
        for (Section section : this.sections) {
            total += section.adapter.getCount() + 1; // add one for header
        }
        return total;
    }

    public void clear() {
        sections.clear();
    }

    public int getViewTypeCount() {
        int total = 1; // one for the header, plus those from sections

        for (Section section : this.sections) {
            total += section.adapter.getViewTypeCount();
        }
        return total;
    }

    public int getItemViewType(int position) {

        /*
        int typeOffset = TYPE_SECTION_HEADER + 1; // start counting from here

        for(Section section : this.sections) {
            if(position == 0) {
                return TYPE_SECTION_HEADER;
            }
            int size = section.adapter.getCount() +1;
            Log.i(TAG, "[getItemViewType] position : " + position);
            Log.i(TAG, "[getItemViewType] size : " + size);
            if(position < size) {
                Log.i(TAG, "[getItemViewType] return : " + typeOffset + section.adapter.getItemViewType(position-1));
                return typeOffset + section.adapter.getItemViewType(position-1);
            }

            position -= size;
            typeOffset += section.adapter.getViewTypeCount();
        }
        */
        return -1;
    }

    // remove
    public boolean areAllItemsSelectable() {
        return false;
    }

    public boolean isEnabled(int position) {
        return (getItemViewType(position) != TYPE_SECTION_HEADER);
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        for (Section section : this.sections) {
            if (position == 0) {
                return section;
            }
            int size = section.adapter.getCount() + 1;
            Log.i(TAG, "[getItem] position : " + position);
            Log.i(TAG, "[getItem] size : " + size);
            if (position < size) {
                return (section.adapter.getItem(position - 1));
            }
            position -= size;
        }
        return null;
    }

    //    public ArrayList<String> getAllItem() {
    //        // TODO Auto-generated method stub
    //        ArrayList<String> allItem = new ArrayList<String>();
    //        for(Section section : this.sections) {
    //            for(int i = 0; i < section.adapter.getCount(); i++) {
    //                allItem.add(section.adapter.getItem(i).);
    //            }
    //
    //        }
    //        return null;
    //    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        int sectionIndex = 0;

        for (Section section : this.sections) {
            if (position == 0) {
                return (getHeaderView(section.caption, sectionIndex, convertView, parent));
            }

            int size = section.adapter.getCount() + 1;
            Log.i(TAG, "[getview] position : " + position);
            Log.i(TAG, "[getview] size : " + size);
            if (position < size) {
                Log.i(TAG, "[getview] size : " + size);
                return (section.adapter.getView(position - 1, convertView, parent));
            }

            position -= size;
            sectionIndex++;
        }
        return null;
    }

    class Section {
        String caption;
        Adapter adapter;

        public Section(String _caption, Adapter _adapter) {
            // TODO Auto-generated constructor stub
            caption = _caption;
            adapter = _adapter;
        }
    }
}
