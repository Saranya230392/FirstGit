/*
 ***************************************************************************
 ** FontSettings.java
 ***************************************************************************
 **
 ** 2013.10.22 Android KK
 **
 ** Mobile Communication R&D Center, Hanyang
 ** Sangmin, Lee (TMSword) ( Mobile Communication R&D Center / Senior Researcher )
 **
 ** This code is a program that changes the font.
 **
 ***************************************************************************
 */
// CAPP_FONTS_HYFONTS
package com.android.settings;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.Button;
import android.text.TextUtils;
import android.net.Uri;
import android.util.Log;
import android.content.Context;
import android.content.pm.UserInfo;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import java.util.List;
import java.io.File;

public class FontSettings extends ListFragment {
    //private static final String KEY_TIME_TO_FINISH = "time_to_finish";
    private FontServerConnection mRemoteFontServer = null;
    private FontSettingsListAdapter mFontLists = null;
    private int mDefaultTypefaceIndex = 0;
    private int mAllFontCount = 0;
    private int mEmbeddedFontCount = 0;
    private int mDownloadFontCount = 0;
    private View mTitleBar = null;
    private ImageButton mAddButton;
    private ImageButton mRemoveButton;
    private Toast mToast = null;
    private Handler mFontServerHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case FontServerConnection.FONT_SERVER_CONNECTED:
                loadFontSettings();
                break;

            default:
                break;
            }
        }
    };
    private Context mContext = null; // LGE Code Support.

    // Remove Reference to SmartWorld
    // http://lgapps.lge.com:8097/LG_apps/325169
    //private static final String SYSPROP_REMOVE_SMARTWRD = "persist.sys.cust.rmsmartwrd";

    /****************************************************************************
     ** Override
     ****************************************************************************/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = (Context)activity; // LGE Code Support.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        connectFontServer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View row = inflater.inflate(R.xml.font_settings, container, false);
        if (Utils.supportSplitView(getActivity())) {
            createTitleBar();
        } else {
            ActionBar actionbar = getActivity().getActionBar();
            if (actionbar != null) {
                actionbar.setDisplayHomeAsUpEnabled(true);
                actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                        ActionBar.DISPLAY_SHOW_CUSTOM);
                actionbar.setIcon(R.drawable.font_type);
            }
        }
        return row;
    }

    @Override
    public void onResume() {
        super.onResume();
        startTitleBar();
        if (mRemoteFontServer != null) { // resume, update.
            loadFontSettings();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopTitleBar();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectFontServer();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mDefaultTypefaceIndex == position) {
            showToast(R.string.sp_font_type_select_nitify_msg_NORMAL);
        } else {
            changeDefaultTypeface(position);
        }
    }

    private void showToast(int msgId) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, msgId, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msgId);
        }
        mToast.show();
    }

    /****************************************************************************
     ** TitleBar
     ****************************************************************************/
    // 20140205 gaenoo.lee@lge.com Apply Font type delete feature [S]
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!Utils.supportSplitView(getActivity())) {
            inflater.inflate(R.menu.fonttype_action_menu, menu);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean hasDownloadFont = mDownloadFontCount > 0 ? true : false;
        if (!Utils.supportSplitView(getActivity())) {
            MenuItem remove = menu.findItem(R.id.remove);
            remove.setEnabled(hasDownloadFont);
            remove.setVisible(hasDownloadFont);

            // 20130220 dongseok.lee : [Z ATT][TD174437] if not support LG smart world.
            // http://mlm.lge.com/di/browse/LGEIME-1670
            if (isSupportDonwLoadFont() == false) {
                MenuItem addBtn = menu.findItem(R.id.add);
                if (addBtn != null) {
                    addBtn.setEnabled(false);
                    addBtn.setVisible(false);
                }
            }
        } else {
            setRemoveButtonState(hasDownloadFont);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            getActivity().onBackPressed();
            return true;
        case R.id.add:
            FontDownloadConnection.connectLGSmartWorld(mContext);
            break;
        case R.id.remove:
            goFontDelete();
            break;
        default:
            return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createTitleBar() {
        if (hasTitleBar() == true) { // Check support device
            final Activity activity = getActivity();
            final LayoutInflater mInflater = LayoutInflater.from(activity);
            mTitleBar = mInflater.inflate(R.layout.font_settings_header, null);
            if (mTitleBar == null) {
                return;
            }
            mAddButton = (ImageButton)mTitleBar.findViewById(R.id.btn_add);
            mRemoveButton = (ImageButton)mTitleBar.findViewById(R.id.btn_delete);
            if (mAddButton == null || mRemoveButton == null) {
                return;
            }

            mAddButton.setImageResource(R.drawable.ic_menu_add_field_holo_light);
            mRemoveButton.setImageResource(R.drawable.common_menu_trash_holo_light);

            final int padding = activity.getResources().getDimensionPixelSize(
                    R.dimen.action_bar_option_menu_padding);

            if (!SettingsBreadCrumb.isAttached(activity)) {
                mAddButton.setPaddingRelative(0, 0, padding, 0);
                mRemoveButton.setPaddingRelative(0, 0, padding, 0);
                return;
            }
            mAddButton.setClickable(true);
            mRemoveButton.setClickable(true);
            mAddButton.setPaddingRelative(0, 0, 0, 0);
            mRemoveButton.setPaddingRelative(0, 0, 0, 0);
            mAddButton.setBackground(getActivity().getResources().getDrawable(
                    R.drawable.breadcrumb_background));
            mRemoveButton.setBackground(getActivity().getResources().getDrawable(
                    R.drawable.breadcrumb_background));

            if (isAdmin(activity)) {
                mAddButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FontDownloadConnection.connectLGSmartWorld(mContext);
                        // Connect LGSmartWorld
                    }
                });
                mRemoveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goFontDelete();
                    }
                });
            } else {
                mAddButton.setVisibility(View.GONE); // User mode : invisible
                mRemoveButton.setVisibility(View.GONE); // User mode : invisible
            }
        }
    }

    private void startTitleBar() {
        // using breadcrumb
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            SettingsBreadCrumb breadcrumb = SettingsBreadCrumb.get(getActivity());
            if (breadcrumb != null) {
                breadcrumb.addView(mTitleBar, null);
            }
        }
    }

    private void stopTitleBar() {
        if (mTitleBar != null) {
            final Activity activity = getActivity();
            activity.getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getActionBar().setCustomView(null);

            if (SettingsBreadCrumb.isAttached(getActivity())) {
                SettingsBreadCrumb breadcrumb = SettingsBreadCrumb.get(getActivity());
                if (breadcrumb != null) {
                    breadcrumb.removeView(mTitleBar);
                }
            }
        }
    }

    // 20140205 gaenoo.lee@lge.com Apply Font type delete feature [E]

    private boolean hasTitleBar() {
        return ((FontDownloadConnection.isStartUpPreloadModel() == true || FontDownloadConnection
                .isInstalledLGSmartWorld(mContext) == true) ? true : false);
    }

    private boolean isSupportDonwLoadFont() {
        return ((FontDownloadConnection.isStartUpPreloadModel() == true || FontDownloadConnection
                .isInstalledLGSmartWorld(mContext) == true) ? true : false);
    }

    /****************************************************************************
     ** FontSettings
     ****************************************************************************/
    private void loadFontSettings() {
        if (getView() == null) {
            return;
        }

        updateFontServer();
        makeFontLists();

        if (mFontLists != null) {
            setListAdapter(mFontLists);
            setSelection(mDefaultTypefaceIndex);
            getListView().requestFocus();
        }
    }

    private void setRemoveButtonState(boolean enable) {
        if (mRemoveButton != null) {
            mRemoveButton.setVisibility(enable ? View.VISIBLE : View.GONE);
        }
    }

    private void makeFontLists() {
        if (mRemoteFontServer != null) {
            mFontLists = new FontSettingsListAdapter(mContext, mRemoteFontServer);
        }
    }

    private void selectDefaultTypeface(int position) {
        getActivity().onBackPressed();
        mRemoteFontServer.selectDefaultTypeface(position);
    }

    private void changeDefaultTypeface(int position) {
        if (mRemoteFontServer != null) {
            final int fontIndex = position;
            //            final Activity activity = getActivity();
            //            if (hasMultipleUsers(activity) == true) { // case Multi-User
            //                AlertDialog.Builder globalWarning = new AlertDialog.Builder(activity);
            //                globalWarning.setTitle(R.string.sp_global_warning_font_change_title);
            //                globalWarning.setMessage(R.string.sp_global_warning_font_change_warning);
            //                globalWarning.setNegativeButton(android.R.string.cancel, null);
            //                globalWarning.setPositiveButton(android.R.string.ok,
            //                        new DialogInterface.OnClickListener() {
            //                            @Override
            //                            public void onClick(DialogInterface dialog, int which) {
            //                                selectDefaultTypeface(fontIndex);
            //                            }
            //                        });
            //                globalWarning.show();
            //            } else { // case Single-User
            selectDefaultTypeface(fontIndex);
            //            }
        }
    }

    /****************************************************************************
     ** Go FontDelete
     ****************************************************************************/
    private void goFontDelete() {
        final Activity activity = getActivity();
        Intent intent = new Intent();
        intent.setClass(activity, FontDeleteActivity.class);
        startActivity(intent);
    }

    /****************************************************************************
     ** FontServer
     ****************************************************************************/
    private void updateFontServer() {
        if (mRemoteFontServer != null) {
            mRemoteFontServer.updateFontServer(); // system settings only.
            mDefaultTypefaceIndex = mRemoteFontServer.getDefaultTypefaceIndex();
            mAllFontCount = mRemoteFontServer.getNumAllFonts();
            mEmbeddedFontCount = mRemoteFontServer.getNumEmbeddedFonts();
            mDownloadFontCount = (mAllFontCount - mEmbeddedFontCount);
            mDownloadFontCount = (mDownloadFontCount < 0 ? 0 : mDownloadFontCount);
        }
        getActivity().invalidateOptionsMenu();
    }

    private void connectFontServer() {
        if (mRemoteFontServer == null) {
            mRemoteFontServer = new FontServerConnection(getActivity(), mFontServerHandler);
            mRemoteFontServer.connectFontServerService();
        }
    }

    private void disconnectFontServer() {
        if (mRemoteFontServer != null) {
            mRemoteFontServer.disconnectFontServerService();
            mRemoteFontServer = null;
        }
    }

    /****************************************************************************
     ** Multi-User
     ****************************************************************************/
    public static boolean isAdmin(Context context) {
        UserManager userManager;
        UserInfo userInfo;
        userManager = (UserManager)context.getSystemService(Context.USER_SERVICE);
        userInfo = userManager.getUserInfo(userManager.getUserHandle());
        return userInfo.isAdmin();
    }

    //    public static boolean hasMultipleUsers(Context context) {
    //        return ((UserManager)context.getSystemService(Context.USER_SERVICE)).getUsers().size() > 1;
    //    }
}
// CAPP_FONTS_HYFONTS_END