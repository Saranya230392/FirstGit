package com.android.settings.defaultapp;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.os.Handler.Callback;
import android.os.IBinder;
import android.app.Activity;
import android.app.ListFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.hardware.usb.IUsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PatternMatcher;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.Utils;

public class DefaultAppSettings extends ListFragment implements Callback, OnItemSelectedListener,
        OnKeyListener {
    static final String TAG = "DefaultAppSetting";

    private static DefaultAppSettings defaultAppFragment = null;
    static final String THREAD_NAME = "DefaultAppUpdate";

    private static boolean mIsAttached = false;
    PackageManager mPackageManager = null;
    Handler mHandler = new Handler(this);
    static final int UPDATE = 1;
    DefaultAppUpdateThread mUpdateThread = null;
    IconLoader mIconLoader = null;
    private IUsbManager mUsbManager;
    static List<ApplicationInfo> mApplicationInfos = null;
    DefaultAppAdapter mAdapter = null;
    private LayoutInflater mInflater = null;
    View mEmptyView = null;
    private int mCurrentPosition = -1;

    @SuppressWarnings("unchecked")
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case UPDATE:
            if (mAdapter != null && msg.obj != null) {
                ArrayList<DefaultAppInfo> info = (ArrayList<DefaultAppInfo>)msg.obj;
                mAdapter.updatePackageInfo(info);
                mAdapter.notifyDataSetChanged();

                if (mEmptyView != null) {
                    int visibility = (info.size() > 0) ? View.GONE : View.VISIBLE;
                    mEmptyView.setVisibility(visibility);
                }
            }
            break;
        default:
            break;
        }
        return false;
    }

    public static DefaultAppSettings newInstance() {
        if (defaultAppFragment == null) {
            defaultAppFragment = new DefaultAppSettings();
        }
        return defaultAppFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ListView listView = getListView();
        ArrayList<DefaultAppInfo> infos = new ArrayList<DefaultAppInfo>();
        mAdapter = new DefaultAppAdapter(getActivity(), infos);
        listView.setAdapter(mAdapter);

        if (Utils.isFolderModel(getActivity())) {
            listView.setOnItemSelectedListener(this);
            listView.setOnKeyListener(this);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        if (mUpdateThread != null) {
            mUpdateThread.quit();
            mUpdateThread = null;
        }

        if (mIconLoader != null) {
            mIconLoader.stop();
            mIconLoader = null;
        }

        super.onDestroy();
    }

    @Override
    public void onPause() {
        mHandler.removeMessages(UPDATE);
        mUpdateThread.stopUpdate();
        super.onPause();
    }

    @Override
    public void onResume() {
        if (mUpdateThread == null) {
            mUpdateThread = new DefaultAppUpdateThread(THREAD_NAME);
            mUpdateThread.start();
        }
        mUpdateThread.requestUpdate();
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        mIsAttached = true;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mIsAttached = false;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View listView = inflater.inflate(R.layout.default_app_list, null);
        mEmptyView = listView.findViewById(R.id.no_contents);
        return listView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPackageManager = getActivity().getPackageManager();
        mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        IBinder b = ServiceManager.getService(Context.USB_SERVICE);
        mUsbManager = IUsbManager.Stub.asInterface(b);

        if (mIconLoader == null) {
            mIconLoader = new IconLoader(getActivity());
        }

        mApplicationInfos = mPackageManager.getInstalledApplications(0);
    }

    public class DefaultAppUpdateThread extends UpdateThread {
        List<DefaultAppInfo> mPackageInfo = new ArrayList<DefaultAppInfo>();

        public DefaultAppUpdateThread(String name) {
            super(name);
        }

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
            case START:
                if (!mIsAttached) {
                    return false;
                }

                List<DefaultAppInfo> infos = makeDefaultPackageInfo(mPackageManager, mUsbManager);
                updateApplicationLabel(infos);
                sort(infos, SORT_NAME);
                uiUpdate(infos);
                break;
            default:
                break;
            }
            return false;
        }

        public void sort(List<DefaultAppInfo> info, int sortType) {
            switch (sortType) {
            case SORT_NAME:
                Collections.sort(info, new SortName());
                break;
            default:
                break;
            }
        }

        private void uiUpdate(List<DefaultAppInfo> infos) {
            mPackageInfo = infos;
            Message msg = mHandler.obtainMessage(UPDATE, new ArrayList<DefaultAppInfo>(infos));
            mHandler.sendMessage(msg);
        }

        private void updateApplicationLabel(List<DefaultAppInfo> infos) {
            if (infos == null) {
                return;
            }

            for (DefaultAppInfo info : infos) {
                ApplicationInfo ainfo;
                try {
                    ainfo = mPackageManager.getApplicationInfo(info.mPackageName, 0);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                    info.mLabel = "";
                    continue;
                }

                info.mLabel = (String)mPackageManager.getApplicationLabel(ainfo);
                if (info.mLabel == null) {
                    info.mLabel = info.mPackageName;
                }

                if (info.mLabel == null) {
                    info.mLabel = "";
                }
            }
        }
    }

    public static class SortName implements Comparator<DefaultAppInfo> {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(DefaultAppInfo arg0, DefaultAppInfo arg1) {
            return sCollator.compare(arg0.mLabel, arg1.mLabel);
        }
    }

    private class IconLoader extends SoftReferenceCacheMap<String, ImageView, Drawable> {
        public IconLoader(Context context) {
            super(context);
        }

        protected Drawable loadDataFromDatabase(String name) {
            try {
                return mPackageManager.getApplicationIcon(name);
            } catch (NameNotFoundException e) {
                Log.d(TAG, "Can't find pacakge name");
            }
            return null;
        }

        protected void updateView(ImageView view, Drawable data) {
            if (data != null) {
                view.setImageDrawable(data);
            } /*else {
                view.setImageResource(R.drawable.default_icon);
              }*/
        }
    }

    public class ViewHolder {
        public ImageView mImageView;
        public TextView mNameView;
        public Button mButtonView;
    }

    public class DefaultAppAdapter extends BaseAdapter {
        private List<DefaultAppInfo> mList = new ArrayList<DefaultAppInfo>();
        @SuppressWarnings("unused")
        private Context mContext = null;

        public DefaultAppAdapter() {
        }

        public DefaultAppAdapter(Context context, List<DefaultAppInfo> list) {
            mContext = context;
            mList = list;
        }

        public void updatePackageInfo(List<DefaultAppInfo> list) {
            mList = list;
        }

        public int getCount() {
            if (mList != null) {
                return mList.size();
            }
            return 0;
        }

        public DefaultAppInfo getItem(int position) {
            if (mList != null && mList.size() != 0) {
                return mList.get(position);
            }
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = null;
            ViewHolder vh = null;
            if (convertView == null) {
                v = mInflater.inflate(R.layout.default_app_item, null);
                vh = new ViewHolder();
                vh.mImageView = (ImageView)v.findViewById(R.id.defaultIcon);
                vh.mNameView = (TextView)v.findViewById(R.id.defaultName);
                vh.mButtonView = (Button)v.findViewById(R.id.clear);
                vh.mButtonView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        onButtonClick(v);
                    }
                });
                v.setTag(vh);
            } else {
                v = convertView;
                vh = (ViewHolder)v.getTag();
            }

            DefaultAppInfo info = getItem(position);
            if (info == null) {
                return new View(getActivity());
            }

            if (mIconLoader != null && !mIconLoader.loadData(vh.mImageView, info.mPackageName)) {
                // vh.mImageView.setImageResource(R.drawable.default_icon);
            }

            if (info.mLabel != null) {
                vh.mNameView.setText(info.mLabel);
            } else {
                vh.mNameView.setText(info.mPackageName);
            }
            vh.mButtonView.setTag(info.mPackageName);
            return v;
        }
    }

    public List<DefaultAppInfo> makeDefaultPackageInfo(PackageManager pm, IUsbManager um) {
        if (mApplicationInfos == null) {
            return null;
        }
        ArrayList<DefaultAppInfo> infos = new ArrayList<DefaultAppInfo>();

        for (ApplicationInfo info : mApplicationInfos) {
            List<ComponentName> prefActList = new ArrayList<ComponentName>();
            List<IntentFilter> intentList = new ArrayList<IntentFilter>();
            pm.getPreferredActivities(intentList, prefActList, info.packageName);
            boolean hasUsbDefaults = false;
            try {
                hasUsbDefaults = um.hasDefaults(info.packageName, 0); // yonguk.kim JB+ migration it should be checked later
            } catch (RemoteException e) {
                continue;
            }

            if (hasUsbDefaults || prefActList.size() > 0) {
                if ("com.android.settings".equals(info.packageName)
                        || "com.lge.settings.easy".equals(info.packageName)
                        || "com.android.LGSetupWizard".equals(info.packageName)
                        ) {
                    continue;
                }

                String mLabel = (String)mPackageManager.getApplicationLabel(info);
                DefaultAppInfo item = new DefaultAppInfo(mLabel, info.packageName);
                infos.add(item);
            }
        }
        return infos;
    }

    private void onButtonClick(View v) {
        final String packageName = (String)v.getTag();
        Log.d(TAG, "onButtonClick, packageName : " + packageName);
        onClearDefaultApp(packageName);
    }

    private void onClearDefaultApp(String packageName) {
        if (packageName == null) {
            return;
        }
        mPackageManager.clearPackagePreferredActivities(packageName);
        try {
            mUsbManager.clearDefaults(packageName, 0); // yonguk.kim JB+ migration it should be checked later
        } catch (RemoteException e) {
            Log.e(TAG, "mUsbManager.clearDefaults", e);
        }

        if (mUpdateThread != null) {
            mUpdateThread.requestUpdate();
        }

    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        Log.d("YSY", "Onkey");
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
            if (mAdapter != null && mAdapter.getCount() > 0) {
                DefaultAppInfo info = mAdapter.getItem(mCurrentPosition);
                if (info == null) {
                    return false;
                }
                String packageName = info.mPackageName;
                onClearDefaultApp(packageName);
            }
            return false;
        }

        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mCurrentPosition = position;
        Log.d("YSY", "onItemSelected, position : " + mCurrentPosition);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }
}