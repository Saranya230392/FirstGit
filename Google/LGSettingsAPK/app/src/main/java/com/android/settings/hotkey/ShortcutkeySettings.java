package com.android.settings.hotkey;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lgmdm.MDMShortcutKeyAdapter;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.lge.constants.SettingsConstants;

public class ShortcutkeySettings extends SettingsPreferenceFragment implements Indexable {
    // implements OnClickListener { 0511. shortcut UI will change for tablet

    private static final String TAG = "ShortcutkeySettings";
    public static final boolean DEBUG = false;

    private Switch mShortcutkeySwitch;
    private HotkeyInfo mHotkeyInfo;
    ActionBar actionBar;
    SettingsBreadCrumb mBreadCrumb;
    private Context mContext;
    
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getActivity().getActionBar();
        if (!Utils.supportSplitView(getActivity())) {
            if (actionBar != null) {
               actionBar.setDisplayHomeAsUpEnabled(true);
               actionBar.setIcon(R.drawable.shortcut_key);
               actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            }
        }
        mContext = getActivity().getApplicationContext();
        mHotkeyInfo = new HotkeyInfo(getActivity().getBaseContext());
        initPreference();

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-433]
        MDMShortcutKeyAdapter.getInstance().initMdmShortcutKeyPolicy(mContext, mShortcutkeySwitch, mLGMDMReceiver);
        // LGMDM_END
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = SettingsBreadCrumb.get(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.addSwitch(mShortcutkeySwitch);
            }
        }
        mShortcutkeySwitch.setChecked(mHotkeyInfo.getDBShortcutKeyStatus() == 1 ? true
                : false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            if (mBreadCrumb != null) {
                mBreadCrumb.removeSwitch();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final Activity activity = getActivity();
        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            View view = activity.getActionBar().getCustomView();
            if (view != null) {
                view.destroyDrawingCache();
            }
            activity.getActionBar().setCustomView(null);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-433]
        MDMShortcutKeyAdapter.getInstance().finalizeMdmShortcutKeyMonitor(activity, mLGMDMReceiver);
        // LGMDM_END
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    private void initPreference() {
        mShortcutkeySwitch = new Switch(getActivity());
        try {
            Activity activity = getActivity();
            if (null != activity) {
                activity = getActivity();
            }
            if (null != activity) {
                if (SettingsBreadCrumb.isAttached(getActivity())) {
                    mShortcutkeySwitch.setPaddingRelative(0, 0, 0, 0);
                } else {
                    setSwitchPadding();
                }
            }
            else {
                Log.e(TAG, "activity is null!!!");
                setSwitchPadding();
            }
        } catch (ClassCastException e) {
            Log.i(TAG, e.getLocalizedMessage());
        }

        mShortcutkeySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            }
        });
        mShortcutkeySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                mHotkeyInfo.setDBShortcutKeyStatus(mShortcutkeySwitch.isChecked() == true ? 1 : 0);
            }
        });
    }

    private void setSwitchPadding() {
        final int padding = getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        mShortcutkeySwitch.setPaddingRelative(0, 0, padding, 0);
        if (null != actionBar) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(mShortcutkeySwitch, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL | Gravity.END));
        }
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-433]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM == false) {
                return;
            }

            if (MDMShortcutKeyAdapter.getInstance().isShortcutKeyPolicyChanged(intent) == false) {
                return;
            }

            Activity activity = getActivity();

            if (activity == null) {
                return;
            }

            _checkAndShowToast(activity);

            activity.finish();

        }

        private void _checkAndShowToast(Context context) {
            if (MDMShortcutKeyAdapter.getInstance().isShortcutKeyPolicyAllowed() == false) {
                Toast.makeText(context,
                        Utils.getString(R.string.sp_lgmdm_block_shortcut_key_NORMAL),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };
    // LGMDM_END

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            inflater.inflate(R.menu.settings_search, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            int settingStyle = Settings.System.getInt(getContentResolver(),
                    SettingsConstants.System.SETTING_STYLE, 0);
            int resultOption = Utils.getUsingSettings(settingStyle, getActivity());
            if (resultOption == Utils.sUSING_EASYSETTINGS) {
                getActivity().onBackPressed();
                return true;
            } else if (resultOption == Utils.sUSING_SETTINGS) {
                finish();
                return true;
            }
            break;
        case R.id.search:
            switchTosearchResults();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchTosearchResults() {
        Intent intent = new Intent();
        intent.setAction("com.lge.settings.SETTINGS_SEARCH");
        intent.putExtra("search", true);
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mRootview = inflater.inflate(R.layout.shortcutkey_main, null);
        return getView(mRootview);
    }

    public View getView(View v) {
        TextView mDescriptionShortcut = (TextView)
                v.findViewById(R.id.main_description);
        mDescriptionShortcut.setVisibility(View.VISIBLE);
        ImageView mHelpImage = (ImageView)v.findViewById(R.id.help_image);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-433]
        if (MDMShortcutKeyAdapter.getInstance().blindShortcutKeyUI(mDescriptionShortcut, mHelpImage)) {
            return v;
        }
        // END LGMDM

        if (Utils.supportRearVolumeKey(mContext)) {
            if (Utils.supportCameraOnlyShortcutKey(mContext)) {
                mDescriptionShortcut.setText(R.string.sp_shortcut_key_description_volume_camera);
                mHelpImage.setImageResource(R.drawable.help_settings_one_shortcutkey_help);
            } else {
                if (Utils.supportShortCutKeyNameAsQmemo(mContext)) {
                    mDescriptionShortcut.setText(R.string.sp_shortcut_key_description_back_new);
                } else {
                    mDescriptionShortcut.setText(R.string.sp_shortcut_key_description_back_new_global);
                }
                mHelpImage.setImageResource(R.drawable.help_settings_shortcutkey_help);
            }
        } else {
            if (Utils.supportCameraOnlyShortcutKey(mContext)) {
                mDescriptionShortcut.setText(R.string.sp_shortcut_key_description_volume_camera);
                mHelpImage.setImageResource(R.drawable.help_settings_sidekey_help_e2);
            } else {
                if (Utils.supportShortCutKeyNameAsQmemo(mContext)) {
                    mDescriptionShortcut.setText(R.string.sp_shortcut_key_description_volume_new);
				} else {
                    mDescriptionShortcut.setText(R.string.sp_shortcut_key_description_volume_new_global);
                }
				mHelpImage.setImageResource(R.drawable.help_settings_sidekey_help);
            }
        }
        // TODO Auto-generated method stub
        return v;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        LayoutInflater mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        populateViewForOrientation(mInflater, (ViewGroup)getView());
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        View v = inflater.inflate(R.layout.shortcutkey_main, viewGroup);
        getView(v);
    }
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            setSearchIndexData(context, "shortcutkey_settings",
                    context.getString(R.string.sp_shortcut_key_NORMAL), "main",
                    null, 
                    null, "com.lge.settings.SHORTCUT_SETTINGS",
                    null, null, 1,
                    null, null, null, 1, 0);
            return mResult;
        }
    };

    public static void setSearchIndexData(Context context, String key,
            String title, String screenTtile, String summaryOn,
            String summaryOff, String intentAction, String intentTargetClass,
            String intentTargetPackage, int currentEnable,
            String preferenceType, String settingsDBTableName,
            String settingsDBField, int visible, int checkValue) {
        SearchIndexableRaw data = new SearchIndexableRaw(context);
        data.key = key;
        data.title = title;
        data.screenTitle = screenTtile;
        data.summaryOn = summaryOn;
        data.summaryOff = summaryOff;
        data.intentAction = intentAction;
        data.intentTargetClass = intentTargetClass;
        data.intentTargetPackage = intentTargetPackage;
        data.currentEnable = currentEnable;
        data.preferenceType = preferenceType;
        data.settingsDBTableName = settingsDBTableName;
        data.settingsDBField = settingsDBField;
        data.visible = visible;
        data.checkValue = checkValue;
        mResult.add(data);
    }
}
