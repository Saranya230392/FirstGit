package com.android.settings.lge;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.hotkey.HotkeyInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Activity to manage button selection and order
 * 
 * @author sw.maeng
 * 
 */
public class ISAIAppSettings extends ListActivity implements OnItemClickListener {
    private static final String TAG = "ButtonEditActivity";
    static final boolean DEBUG = false;

    private ComponentListAdapter listAdapter;

    private PackageManager packageManager;

    //private boolean isPausedByBackKey = false;
    //private Context mContext;
    public static final int RESULT_CODE = 101;

    public static final String ISAI_SHORTCUT_PACKAGE = "isai_shortcut_package";
    public static final String ISAI_SHORTCUT_CLASS = "isai_shortcut_class";
    public static final String ISAI_SHORTCUT_APP_NAME = "isai_shortcut_app_name";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.isai_app_list);
        //mContext = getApplicationContext();
        packageManager = getPackageManager();
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(actionBar.getDisplayOptions()
                    | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_CUSTOM);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        refreshItemList();
        super.onResume();
    }

    private void refreshItemList() {

        Collections.sort(getComponentList(), APP_COMPARATOR);
        listAdapter = new ComponentListAdapter(this,
                R.layout.isai_list_item, getComponentList());
        setListAdapter(listAdapter);
    }

    /**
     * Selected app is high priority
     */
    public static final Comparator<ComponentItemAdapter> APP_COMPARATOR = new Comparator<ComponentItemAdapter>() {
        @Override
        public int compare(ComponentItemAdapter lhs, ComponentItemAdapter rhs) {
            if (lhs.isSelected && !rhs.isSelected) {
                return -1;
            } else if (!lhs.isSelected && rhs.isSelected) {
                return 1;
            } else {
                return lhs.getOrder() - rhs.getOrder();
            }
        }
    };

    /**
     * Load component info from shared preference.
     * 
     * @return Returns component info list. Null if failed to load
     *         SharedPreference.
     * 
     */
    ArrayList<ComponentItemAdapter> getComponentList() {
        ArrayList<ComponentItemAdapter> compInfoList = null;
        //PackageManager pm = mContext.getPackageManager();
        compInfoList = new ArrayList<ComponentItemAdapter>();

        // Load all laucher category apps
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(
                mainIntent, 0);
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(
                packageManager));
        for (ResolveInfo info : resolveInfos) {
            ComponentItemAdapter appInfo = new ComponentItemAdapter(info);
            if (!compInfoList.contains(appInfo)) {
                compInfoList.add(appInfo);
            }

        }
        return compInfoList;

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /*        if (keyCode == KeyEvent.KEYCODE_BACK) {
                    isPausedByBackKey = true;
                }*/
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        Log.d("jw ", "onItemClick");

        ComponentItemAdapter item = listAdapter.getItem(position);
        if (item != null) {

            Log.d("jw ", "item.packageName : " + item.packageName);
            Log.d("jw ", "item.className : " + item.className);
            Log.d("jw ", "item.label : " + item.label);

            Settings.System
                    .putString(getContentResolver(), ISAI_SHORTCUT_PACKAGE, item.packageName);
            Settings.System.putString(getContentResolver(), ISAI_SHORTCUT_CLASS, item.className);

            item.setSelected(!item.isSelected);
            listAdapter.notifyDataSetChanged();

            finish();

        }

    }

    private class ComponentItemAdapter extends ComponentInfo {
        private boolean isSelected = false;

        public ComponentItemAdapter(ResolveInfo resolveInfo) {
            super(resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name, Integer.MAX_VALUE);
            label = resolveInfo.loadLabel(getPackageManager()).toString();
            if (label == null && resolveInfo.activityInfo != null) {
                label = resolveInfo.activityInfo.name;
            }
            if (DEBUG) {
                Log.d("jw ", "label :  " + label);
                Log.d("jw ", "getClassName() :  " + getClassName());
                if (resolveInfo.activityInfo != null) {
                    Log.d("jw ", "PackageName :  " + resolveInfo.activityInfo.packageName);

                }
            }

        }

        public ComponentItemAdapter(ComponentName compName, int order)
                throws NameNotFoundException {
            super(compName.getPackageName(), compName.getClassName(), order);
            ActivityInfo activityInfo = packageManager.getActivityInfo(
                    compName, 0);
            label = activityInfo.loadLabel(packageManager);
            if (label == null) {
                label = activityInfo.name;
            }

        }

        /**
         * @param isSelected
         */
        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        @Override
        public boolean isSelected() {
            return isSelected;
        }

    }

    private class ComponentListAdapter extends
            ArrayAdapter<ComponentItemAdapter> {

        private List<ComponentItemAdapter> appInfoList;
        private Context context;

        public ComponentListAdapter(Context context, int resourceId,
                List<ComponentItemAdapter> items) {
            super(context, resourceId, items);
            this.context = context;
            appInfoList = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout layout = null;

            if (convertView != null && convertView instanceof LinearLayout) {
                layout = (LinearLayout)convertView;
            } else {
                layout = (LinearLayout)View.inflate(context,
                        R.layout.isai_list_item, null);
            }

            ComponentItemAdapter appInfoAdapter = appInfoList.get(position);
            ImageView imgIcon = (ImageView)layout
                    .findViewById(R.id.img_app_icon);
            try {
                Drawable activityIcon = packageManager
                        .getActivityIcon(appInfoAdapter.getComponentName());
                imgIcon.setImageDrawable(activityIcon);
            } catch (NameNotFoundException e) {
                Log.w(TAG,
                        "No icon found for "
                                + appInfoAdapter.getComponentName());
            }

            TextView name = (TextView)layout
                    .findViewById(R.id.quicksettings_setting_item_name);
            name.setText(appInfoAdapter.label);

            return layout;
        }

        @Override
        public int getCount() {
            return appInfoList.size();
        }

        @Override
        public int getPosition(ComponentItemAdapter item) {
            // TODO Auto-generated method stub
            if (DEBUG) {
                Log.d("jw ", "onItemClick label :  " + item.label);
                Log.d("jw ", "onItemClick getClassName() :  " + item.getClassName());
                Log.d("jw ", "onItemClick PackageName :  " + item.packageName);
            }
            return super.getPosition(item);

        }

    }

}
