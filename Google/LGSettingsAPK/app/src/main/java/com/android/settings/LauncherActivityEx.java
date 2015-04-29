package com.android.settings;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.os.UserHandle;

public class LauncherActivityEx extends ListActivity {
    final String TAG = "LauncherActivityEx";
    final String CATEGORY = "category";

    Intent mIntent;
    PackageManager mPackageManager;
    IconResizer mIconResizer;
    ListAdapter mAdapter;

    public static class ListItem {
        public ResolveInfo resolveInfo;
        public CharSequence label;
        public Drawable icon;
        public String packageName;
        public String className;
        public Bundle extras;
        public boolean fFinalitem; //whether or not draws divider line

        ListItem(PackageManager pm, ResolveInfo resolveInfo, IconResizer resizer) {
            this.resolveInfo = resolveInfo;
            label = resolveInfo.loadLabel(pm);
            ComponentInfo ci = resolveInfo.activityInfo;
            if (ci == null) {
                ci = resolveInfo.serviceInfo;
            }
            if (label == null && ci != null) {
                label = resolveInfo.activityInfo.name;
            }

            if (resizer != null) {
                icon = resizer.createIconThumbnail(resolveInfo.loadIcon(pm));
            }

            if (ci != null) { //myeonghwan.kim@lge.com 120926 WBT issue
                packageName = ci.applicationInfo.packageName;
                className = ci.name;
            }
        }

        public ListItem() {
        }
    }

    private class ActivityAdapter extends BaseAdapter implements Filterable {
        private final Object lock = new Object();
        private ArrayList<ListItem> mOriginalValues;

        protected final IconResizer mIconResizer;
        protected final LayoutInflater mInflater;

        protected List<ListItem> mActivitiesList;

        private Filter mFilter;
        private final boolean mShowIcons;

        public ActivityAdapter(IconResizer resizer) {
            mIconResizer = resizer;
            mInflater = (LayoutInflater)LauncherActivityEx.this.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mShowIcons = onEvaluateShowIcons();

            //mActivitiesList = makeListItems();
            MakeCategoryList categoryList = new MakeCategoryList(makeListItems());
            mActivitiesList = categoryList.makeCategoryListItems();
        }

        public Intent intentForPosition(int position) {
            if (mActivitiesList == null) {
                return null;
            }

            Intent intent = new Intent(mIntent);
            ListItem item = mActivitiesList.get(position);
            intent.setClassName(item.packageName, item.className);
            if (item.extras != null) {
                intent.putExtras(item.extras);
            }
            return intent;
        }

        public ListItem itemForPosition(int position) {
            if (mActivitiesList == null) {
                return null;
            }

            return mActivitiesList.get(position);
        }

        public int getCount() {
            return mActivitiesList != null ? mActivitiesList.size() : 0;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ListItem item = mActivitiesList.get(position);
            if (item.className.equals(CATEGORY)) {
                convertView = mInflater.inflate(R.layout.launcher_activity_category_section,
                        parent, false);
                convertView.setOnClickListener(null);
                convertView.setOnLongClickListener(null);
                convertView.setLongClickable(false);

                final TextView categoryView = (TextView)convertView
                        .findViewById(R.id.list_item_section_text);
                categoryView.setText(item.label);

                return convertView;
            }
            else {
                convertView = mInflater
                        .inflate(R.layout.launcher_activity_list_item, parent, false);
                bindView(convertView, mActivitiesList.get(position));
                return convertView;
            }
        }

        private void bindView(View view, ListItem item) {
            final TextView text = (TextView)view.findViewById(R.id.launcher_list_item);
            final ImageView divider = (ImageView)view.findViewById(R.id.launcher_list_divider);
            final Resources resources = LauncherActivityEx.this.getResources();

            text.setText(item.label);
            if (!item.fFinalitem) {
                divider.setBackgroundDrawable(resources
                        .getDrawable(android.R.drawable.divider_horizontal_textfield));
            }
            if (mShowIcons) {
                if (item.icon == null) {
                    item.icon = mIconResizer.createIconThumbnail(item.resolveInfo
                            .loadIcon(getPackageManager()));
                }
                text.setCompoundDrawablesRelativeWithIntrinsicBounds(item.icon, null, null, null);
            }
        }

        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new ArrayFilter();
            }
            return mFilter;
        }

        /**
         * An array filters constrains the content of the array adapter with a prefix. Each
         * item that does not start with the supplied prefix is removed from the list.
         */
        private class ArrayFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence prefix) {
                FilterResults results = new FilterResults();

                if (mOriginalValues == null) {
                    synchronized (lock) {
                        mOriginalValues = new ArrayList<ListItem>(mActivitiesList);
                    }
                }

                if (prefix == null || prefix.length() == 0) {
                    synchronized (lock) {
                        ArrayList<ListItem> list = new ArrayList<ListItem>(mOriginalValues);
                        results.values = list;
                        results.count = list.size();
                    }
                } else {
                    final String prefixString = prefix.toString().toLowerCase();

                    ArrayList<ListItem> values = mOriginalValues;
                    int count = values.size();

                    ArrayList<ListItem> newValues = new ArrayList<ListItem>(count);

                    for (int i = 0; i < count; i++) {
                        ListItem item = values.get(i);

                        String[] words = item.label.toString().toLowerCase().split(" ");
                        int wordCount = words.length;

                        for (int k = 0; k < wordCount; k++) {
                            final String word = words[k];

                            if (word.startsWith(prefixString)) {
                                newValues.add(item);
                                break;
                            }
                        }
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                mActivitiesList = (List<ListItem>)results.values;
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    }

    public class IconResizer {
        // Code is borrowed from com.android.launcher.Utilities.
        private int mIconWidth = -1;
        private int mIconHeight = -1;

        private final Rect mOldBounds = new Rect();
        private Canvas mCanvas = new Canvas();

        public IconResizer() {
            mCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                    Paint.FILTER_BITMAP_FLAG));

            final Resources resources = LauncherActivityEx.this.getResources();
            mIconWidth = mIconHeight = (int)resources.getDimension(
                    android.R.dimen.app_icon_size);
        }

        public Drawable createIconThumbnail(Drawable icon) {
            int width = mIconWidth;
            int height = mIconHeight;

            final int iconWidth = icon.getIntrinsicWidth();
            final int iconHeight = icon.getIntrinsicHeight();

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable)icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            }

            if (width > 0 && height > 0) {
                if (width < iconWidth || height < iconHeight) {
                    final float ratio = (float)iconWidth / iconHeight;

                    if (iconWidth > iconHeight) {
                        height = (int)(width / ratio);
                    } else if (iconHeight > iconWidth) {
                        width = (int)(height * ratio);
                    }

                    final Bitmap.Config c = icon.getOpacity() != PixelFormat.OPAQUE ?
                            Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
                    final Bitmap thumb = Bitmap.createBitmap(mIconWidth, mIconHeight, c);
                    final Canvas canvas = mCanvas;
                    canvas.setBitmap(thumb);
                    // Copy the old bounds to restore them later
                    // If we were to do oldBounds = icon.getBounds(),
                    // the call to setBounds() that follows would
                    // change the same instance and we would lose the
                    // old bounds
                    mOldBounds.set(icon.getBounds());
                    final int x = (mIconWidth - width) / 2;
                    final int y = (mIconHeight - height) / 2;
                    icon.setBounds(x, y, x + width, y + height);
                    icon.draw(canvas);
                    icon.setBounds(mOldBounds);
                    icon = new BitmapDrawable(getResources(), thumb);
                    canvas.setBitmap(null);
                } else if (iconWidth < width && iconHeight < height) {
                    final Bitmap.Config c = Bitmap.Config.ARGB_8888;
                    final Bitmap thumb = Bitmap.createBitmap(mIconWidth, mIconHeight, c);
                    final Canvas canvas = mCanvas;
                    canvas.setBitmap(thumb);
                    mOldBounds.set(icon.getBounds());
                    final int x = (width - iconWidth) / 2;
                    final int y = (height - iconHeight) / 2;
                    icon.setBounds(x, y, x + iconWidth, y + iconHeight);
                    icon.draw(canvas);
                    icon.setBounds(mOldBounds);
                    icon = new BitmapDrawable(getResources(), thumb);
                    canvas.setBitmap(null);
                }
            }
            return icon;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPackageManager = getPackageManager();

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(true);
        onSetContentView();

        mIconResizer = new IconResizer();

        mIntent = new Intent(getTargetIntent());
        mIntent.setComponent(null);
        mAdapter = new ActivityAdapter(mIconResizer);

        setListAdapter(mAdapter);
        getListView().setTextFilterEnabled(true);

        //updateAlertTitle();
        //updateButtonText();

        setProgressBarIndeterminateVisibility(false);
    }

    /*
        private void updateAlertTitle() {
            TextView alertTitle = (TextView) findViewById(com.android.internal.R.id.alertTitle);
            if (alertTitle != null) {
                alertTitle.setText(getTitle());
            }
        }

        private void updateButtonText() {
            Button cancelButton = (Button) findViewById(com.android.internal.R.id.button1);
            if (cancelButton != null) {
                cancelButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
        }

        @Override
        public void setTitle(CharSequence title) {
            super.setTitle(title);
            updateAlertTitle();
        }

        @Override
        public void setTitle(int titleId) {
            super.setTitle(titleId);
            updateAlertTitle();
        }
    */
    /**
     * Override to call setContentView() with your own content view to
     * customize the list layout.
     */
    protected void onSetContentView() {
        setContentView(R.layout.launcher_activity_list);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = intentForPosition(position);

        //myeonghwan.kim@lge.com 20120926 WBT issue
        if (intent != null) {
            startActivity(intent);
        }
    }

    protected Intent intentForPosition(int position) {
        ActivityAdapter adapter = (ActivityAdapter)getListAdapter();
        return adapter.intentForPosition(position);
    }

    protected ListItem itemForPosition(int position) {
        ActivityAdapter adapter = (ActivityAdapter)getListAdapter();
        return adapter.itemForPosition(position);
    }

    protected Intent getTargetIntent() {
        return new Intent();
    }

    protected List<ResolveInfo> onQueryPackageManager(Intent queryIntent) {
        return mPackageManager.queryIntentActivities(queryIntent, /* no flags */0);
    }

    public List<ListItem> makeListItems() {
        // Load all matching activities and sort correctly
        List<ResolveInfo> list = onQueryPackageManager(mIntent);
        //Collections.sort(list, new ResolveInfo.DisplayNameComparator(mPackageManager));

        ArrayList<ListItem> result = new ArrayList<ListItem>(list.size());
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            ResolveInfo resolveInfo = list.get(i);
            result.add(new ListItem(mPackageManager, resolveInfo, null));
        }

        return result;
    }

    public class MakeCategoryList {
        private List<ListItem> list;
        private ArrayList<String> categorys;
        private ArrayList<String[]> categoryItems;

        public MakeCategoryList(List<ListItem> list) {
            this.list = list;
            fillCategoryItems();
            fillEachListItems();
        }

        public List<ListItem> makeCategoryListItems() {
            ArrayList<ListItem> categoryListItems = new ArrayList<ListItem>();

            for (int i = 0; i < categorys.size(); i++) {
                insertEachCategoryItems(i, categorys.get(i), categoryListItems);
            }
            return categoryListItems;
        }

        private void insertEachCategoryItems(int index, String categoryName,
                List<ListItem> categoryListItems) {
            ListItem categoryItem = new ListItem();
            categoryItem.className = CATEGORY;
            categoryItem.label = categoryName;

            categoryListItems.add(categoryItem);
            sortCategoryItems(index, categoryListItems);
        }

        private void sortCategoryItems(int index, List<ListItem> categoryListItems) {
            String[] sortItems = categoryItems.get(index);
            int listSize = list.size();
            int itemsSize = sortItems.length;

            boolean isSecondaryUser = UserHandle.myUserId() != UserHandle.USER_OWNER;
            String VPN_SETTINGS = "com.android.settings.Settings$VpnSettingsActivity";
            String VPN_SELECTOR = "com.android.settings.Settings$VpnSelectorActivity";

            for (int i = 0; i < itemsSize; i++)
            {
                for (int j = 0; j < listSize; j++) {
                    //String listItem =list.get(j).className.substring("com.android.settings.Settings$".length());

                    if (sortItems[i].equals(list.get(j).className/*listItem*/)) {
                        if (isSecondaryUser
                                && (sortItems[i].equals(VPN_SETTINGS) || sortItems[i]
                                        .equals(VPN_SELECTOR))) {
                            android.util.Log.d(TAG,
                                    "VPN shortcut is not avaiable to secondary users.");
                        } else {
                            categoryListItems.add(list.get(j));
                        }
                    }
                }
            }

            int lastNum = categoryListItems.size() - 1;
            categoryListItems.get(lastNum).fFinalitem = true;
        }

        private void fillCategoryItems() {
            categorys = new ArrayList<String>();
            Resources res = LauncherActivityEx.this.getResources();

            categorys.add(res.getString(R.string.sp_easy_wireless_network_category_NORMAL));
            categorys.add(res.getString(R.string.header_category_device));
            categorys.add(res.getString(R.string.header_category_personal));
            categorys.add(res.getString(R.string.header_category_system));
        }

        private void fillEachListItems() {
            categoryItems = new ArrayList<String[]>();
            Resources res = LauncherActivityEx.this.getResources();
            // [shpark82.park] VPN shortcut
            String[] wirelessNetworksItems;
            if (Utils.isSupportVPN(getApplicationContext())) {
                wirelessNetworksItems = res.getStringArray(R.array.wireless_networks_lgvpn);
            } else {
                wirelessNetworksItems = res.getStringArray(R.array.wireless_networks);
            }
            String[] deviceItems = res.getStringArray(R.array.device);
            String[] personalItems = res.getStringArray(R.array.personal);
            String[] systemItems = res.getStringArray(R.array.system);

            categoryItems.add(wirelessNetworksItems);
            categoryItems.add(deviceItems);
            categoryItems.add(personalItems);
            categoryItems.add(systemItems);
        }
    }

    protected boolean onEvaluateShowIcons() {
        return true;
    }

}
