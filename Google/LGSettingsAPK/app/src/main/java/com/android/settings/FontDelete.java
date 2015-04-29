/*
***************************************************************************
** FontDelete.java
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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class FontDelete extends ListFragment {
    private FontServerConnection mRemoteFontServer = null;
    private FontDeleteListAdapter mDeleteFontLists = null;
    private boolean mFontServerConnected = false;
    private View mTitleBar = null;
    private CheckBox mSelectAll = null;
    private TextView mSelectedCount = null;
    private Button mCancel = null;
    private Button mDelete = null;
    private boolean mFinished = false;

    private Handler mFontServerHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case FontServerConnection.FONT_SERVER_CONNECTED:
                setFontServerConnected(true);
                loadFontSettings();
                break;

            default:
                break;
            }
        }
    };

    /****************************************************************************
    ** Override
    ****************************************************************************/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectFontServer();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        createTitleBar();
        createButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFinished == true) {
            final Activity activity = getActivity();
            activity.finish();
            return;
        }

        if (isFontServerConnected()) {
            loadFontSettings();
            updateButtonState();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectFontServer();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mDeleteFontLists != null) {
            boolean isSelectAll = mDeleteFontLists.setChecked(position);
            if (mSelectAll != null) {
                mSelectAll.setChecked(isSelectAll);
                updateFontLists();
                updateButtonState();
            }
        }
    }

    /****************************************************************************
    ** TitleBar
    ****************************************************************************/
    // 20140205 gaenoo.lee@lge.com Apply Font type delete feature [S]
    private void createTitleBar() {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(R.layout.action_bar_select_all);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);

            mTitleBar = actionBar.getCustomView();

            if (mTitleBar == null) {
                return;
            }
            mSelectedCount = (TextView)mTitleBar.findViewById(R.id.txt_selected);
            mSelectAll = (CheckBox)mTitleBar.findViewById(R.id.chkbox_selectAll);

            if (mSelectAll == null || mSelectedCount == null) {
                return;
            }

            mSelectedCount.setText(getString(R.string.sp_quiet_mode_contact_selected_number_NORMAL,
                    0));
            mSelectAll.setChecked(false);
            mSelectAll.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (mDeleteFontLists != null) {
                        mDeleteFontLists.updateAllChecked(mSelectAll.isChecked());
                        updateFontLists();
                        updateButtonState();
                    }
                }
            });
        }
    }

    /****************************************************************************
    ** Buttons
    ****************************************************************************/
    private void createButtons() {
        final Activity activity = getActivity();

        if ((mCancel = (Button)activity.findViewById(R.id.cancel)) != null) {
            mCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.finish(); // back to FontSettings
                }
            });
        }

        if ((mDelete = (Button)activity.findViewById(R.id.delete)) != null) {
            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlgDeleteFont();
                }
            });
        }
        updateButtonState();
    }

    /****************************************************************************
    ** FontDelete
    ****************************************************************************/
    private void loadFontSettings() {
        updateFontServer();
        makeFontLists();

        if (mDeleteFontLists != null) {
            setListAdapter(mDeleteFontLists);
            getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        }
    }

    private void makeFontLists() {
        if (isFontServerConnected() == true) {
            if (mDeleteFontLists == null) {
                mDeleteFontLists = new FontDeleteListAdapter(getActivity(), mRemoteFontServer);
            }
        }
    }

    private void updateFontLists() {
        if (mSelectedCount != null) {
            int mCheckedCount = mDeleteFontLists.numChecked();
            mSelectedCount.setText(getString(R.string.sp_quiet_mode_contact_selected_number_NORMAL,
                    mCheckedCount));
        }
        setListAdapter(mDeleteFontLists);
    }

    private void updateButtonState() {
        if (mSelectedCount == null || mDelete == null) {
            return;
        }

        if (mDeleteFontLists == null) {
            mDelete.setEnabled(false);
            return;
        }

        int mCheckedCount = mDeleteFontLists.numChecked();
        mDelete.setEnabled(mCheckedCount > 0 ? true : false);
    }

    // 20140205 gaenoo.lee@lge.com Apply Font type delete feature [E]

    private void dlgDeleteFont() {
        if (mDeleteFontLists == null || mRemoteFontServer == null
                || mDeleteFontLists.numChecked() == 0) {
            return;
        }

        boolean useFontDelete = mDeleteFontLists.useFontDelete();
        final Activity activity = getActivity();
        AlertDialog.Builder deleteDlg = new AlertDialog.Builder(activity);
        deleteDlg.setTitle(getString(R.string.user_dict_settings_context_menu_delete_title));
        deleteDlg.setMessage((useFontDelete ? getString(R.string.sp_delete_use_fonts_msg_NORMAL) :
                getString(R.string.sp_delete_fonts_msg_NORMAL)));
        deleteDlg.setNegativeButton(getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }
                );
        deleteDlg.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFont();
                    }
                }
                );
        deleteDlg.show();
    }

    private void deleteFont() {
        int numDownloadFont = mDeleteFontLists.numDownloadFont();
        int numDelete = 0;
        for (int n = 0; n < numDownloadFont; n++) {
            if (mDeleteFontLists.getChecked(n) == true) {
                int fontIndex = (n + mDeleteFontLists.numEmbeddedFont());
                String fontAppName = mRemoteFontServer.getDownloadFontAppName(fontIndex);
                if (fontAppName != null) {
                    String fontPackageName = fontAppName.substring(0, fontAppName.length() - 6);
                    numDelete++;
                    deletePackage(fontPackageName);
                }
            }
        }

        if (numDelete > 0) {
            mFinished = true;
        }
    }

    private void deletePackage(String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        startActivity(uninstallIntent);
    }

    /****************************************************************************
    ** FontServer
    ****************************************************************************/
    private void updateFontServer() {
        if (isFontServerConnected() == true) {
            mRemoteFontServer.updateFontServer(); // system settings only.
        }
    }

    private void setFontServerConnected(boolean connected) {
        mFontServerConnected = connected;
    }

    private boolean isFontServerConnected() {
        return mFontServerConnected;
    }

    private void connectFontServer() {
        if (mRemoteFontServer == null) {
            mRemoteFontServer = new FontServerConnection(getActivity(), mFontServerHandler);
            mRemoteFontServer.connectFontServerService();
        }
    }

    private void disconnectFontServer() {
        if (isFontServerConnected() == true) {
            mRemoteFontServer.disconnectFontServerService();
            mRemoteFontServer = null;
        }
    }
}
// CAPP_FONTS_HYFONTS_END