package com.android.settings;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
//import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.content.ContentValues;
import android.content.ContentResolver;

import com.lge.media.RingtoneManagerEx;

import android.view.LayoutInflater;
import android.app.ListFragment;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.provider.MediaStore;

import com.android.settings.RingtoneEntryAdapter;
import com.android.settings.RingtoneEntryAdapter.RingtoneEntryItem;
import com.android.settings.RingtoneEntryAdapter.RingtoneSectionItem;
import com.android.settings.RingtoneItem;
import com.android.settings.R;

//import com.android.settings.SettingsBreadCrumb; //KLP

//CAPP_DRM [lg-drm@lge.com 100414]
//import com.lge.config.ConfigBuildFlags;
import com.lge.lgdrm.Drm;
//import android.widget.ImageView;
//import android.view.Gravity;
//CAPP_DRM_END

//[S][Settings] [donghan07.lee] Add DRM Cursor for DRM files
//import com.android.internal.database.SortCursor;
// KLP import android.provider.DrmStore;
//[E][Settings] [donghan07.lee] Add DRM Cursor for DRM files

import android.os.SystemProperties;

//[S][Settings] [donghan07.lee] Add DRM policy 2012.02.14
import com.lge.lgdrm.DrmManager;
//import com.lge.lgdrm.DrmContentSession;
import android.media.AudioManager;
import android.media.AudioManagerEx;
import android.widget.Toast;
//[E][Settings] [donghan07.lee] Add DRM policy 2012.02.14

//[AUDIO_FWK]_START, 20120109, dk.han@lge.com,  To check defualt ringtone index. {
//20120116, dk.han@lge.com, Use AudioFocus if headset was connected and playing BGM.
import android.media.MediaPlayer;
//[AUDIO_FWK]_END, 20120109, dk.han@lge.com }

//[S][2012.03.08][donghan07.lee][common][common] Activity finish when SD card change event
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
//[E][2012.03.08][donghan07.lee][common][common] Activity finish when SD card change event

//[S][Settings] [donghan07.lee] Sorry popup error fixed when music app is disabled 2012.04.05
import android.app.ActivityManager.RunningTaskInfo;
import android.content.DialogInterface;
//[E][Settings] [donghan07.lee] Sorry popup error fixed when music app is disabled 2012.04.05

//[S]expired DRM check
//import com.lge.lgdrm.DrmContent;
//import com.lge.lgdrm.DrmException;
//[E]expired DRM check
//CAPP_DRM [lg-drm@lge.com 100624]
import com.lge.lgdrm.DrmFwExt;
//CAPP_DRM_END

//LGE_S nexti. dongkyu31.lee
import android.drm.DrmManagerClient;
import android.os.Build;
import android.os.RemoteException;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View.OnTouchListener;
//import android.view.MotionEvent;

import android.content.SharedPreferences;

import android.widget.ImageButton;

import com.android.settings.lgesetting.Config.Config;
import com.android.settings.RingtonePickerInfo;
import com.android.settings.soundprofile.SoundProfileInfo;

//LGE_E nexti. dongkyu31.lee
public class RingtonePicker extends ListFragment implements Runnable {

    private static final String SETTING_STYLE = "settings_style";
    static final String TAG = "RingtonePicker";

    private static final int RENAME_SIM_TYPE_RINGTONE = 0;
    private static final int RENAME_SIM_TYPE_NOTIFICATION = 1;
    //private static final int RENAME_SIM_TYPE_VIBRATION = 2;

    private static final int RENAME_SIM1_INDEX = 0;
    private static final int RENAME_SIM2_INDEX = 1;
    private static final int RENAME_SIM3_INDEX = 2;

    ArrayList<RingtoneItem> items;

    Button mCancelButton;
    Button mOKButton;
    Context mContext;
    private Cursor mExternalCursor;
    private Cursor mInternalCursor;
    private Cursor mInternalSatoTakuCursor;
    private Cursor mInternalDisneyCursor;
    private Cursor mInternalLGCursor;
    RingtoneManagerEx mRingtoneManager;

    private ListView listView;

    private Handler mHandler;

    int internal_ringtone_position;
    int external_ringtone_position;
    int default_ringtone_position;
    int mRemovePos = -1;
    Uri position_uri;
    private AlertDialog mDeleteDialog;

    //LGE_S. Docomo. nexti. dongkyu31.lee for playready ringtone.
    //private Build mBuild = new Build();
    private MediaPlayer mAudio;
    //private String mDevice = "l_dcm";
    private String mOperator = "DCM";
    private String strTargetOperator = Config.getOperator();
    private static boolean mIsSatotakuRingtoneModel = false; 
    private static DrmManagerClient mDrmManagerClient = null;
    // components of DrmStore. ex) DrmStore.RightsStatus.RIGHTS_VALID
    private final int mRIGHTS_VALID = 0;
    private final int mRIGHTS_INVALID = 1;
    private final int mRIGHTS_EXPIRED = 2;
    private final int mRIGHTS_NOT_ACQUIRED = 3;

    int LAUNCHED_REQUEST_CODE = 20;

    private static final int DELAY_MS_SELECTION_PLAYED = 300;

    private Uri mSampleRingtoneUri = null;

    /**
     * A Ringtone for the default ringtone. In most cases, the RingtoneManager
     * will stop the previous ringtone. However, the RingtoneManager doesn't
     * manage the default ringtone for us, so we should stop this one manually.
    */
    private Ringtone mDefaultRingtone;
    private SoundProfileInfo mSoundProfileInfo;
    private AudioManager mAudioManager;

    //[S][Settings] [donghan07.lee] Add DRM policy 2012.02.14
    // ANDY_PORTING LGDRM [lg-drm@lge.com 100414]
    private boolean mSelected = false;
    private boolean mDrmRingtone = true; // Can use as a ringtone
    private boolean mDrmJob = false;
    private Runnable mDrmTask = new Runnable() {
        public void run() {
            stopAnyPlayingRingtone(); // mRingtoneManager.stopPreviousRingtone();
            mDrmJob = false;
        }
    };
    // ANDY_END

    /** Whether this list has the 'Default' item. */
    private boolean mHasDefaultItem;

    /** The Uri to play when the 'Default' item is clicked. */
    private Uri mUriForDefaultItem;

    /** Whether this list has the 'Silent' item. */
    private boolean mHasSilentItem;

    /** The Uri to place a checkmark next to. */
    private Uri mExistingUri;

    /** The position in the list of the last clicked item. */
    private int mClickedPos = -1;

    /** The position in the list of the 'Default' item. */
    private int mDefaultRingtonePos = -1;

    /** The position in the list of the 'Silent' item. */
    private int mSilentPos = -1;

    /** The number of static items in the list. */
    private int mStaticItemCount;

    /** The position in the list of the ringtone to sample. */
    private int mSampleRingtonePos = -1;
    //[E][Settings] [donghan07.lee] Add DRM policy 2012.02.14

    //[S][2012.02.17][donghan07.lee][ICS Settings][Not for TD] for Dual Sim ringtone
    int mParentType = -1;

    //private ImageView mRingtoneAddBtn;

    boolean isDualSim;
    boolean isTripleSim;

    //[S][2012.03.08][donghan07.lee][common][common] Activity finish when SD card change event
    private StorageManager mStorageManager = null;
    //[E][2012.03.08][donghan07.lee][common][common] Activity finish when SD card change event

    private ImageButton mButton; //for breadcrumb
    private ImageButton mButton2; //for breadcrumb
    private View mHeaderView;
    private boolean mIsAddSoundProfileMode = false;
    RingtonePickerInfo mRingtoneInfo;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        //inflater= getActivity().getLayoutInflater();
        mContext = getActivity();
        mRingtoneInfo = new RingtonePickerInfo(getActivity().getApplicationContext());
        Intent intent = getActivity().getIntent();
        mParentType = intent.getIntExtra(RingtoneManagerEx.EXTRA_RINGTONE_TYPE, -1);
        mIsAddSoundProfileMode = intent.getBooleanExtra("user_soundprofile", false);
        if (mIsAddSoundProfileMode) {
            mSoundProfileInfo = new SoundProfileInfo(getActivity().getApplicationContext());
            mRingtoneInfo.do_setUserSoundProfile(mIsAddSoundProfileMode);
        } else {
            mSoundProfileInfo = new SoundProfileInfo(getActivity().getApplicationContext());
            mRingtoneInfo.do_setUserSoundProfile(false);
        }
        if (mParentType == -1) {
            SharedPreferences pref = getActivity().getSharedPreferences("RINGTONE_PARENT",
                    Activity.MODE_PRIVATE);
            mParentType = pref.getInt(RingtoneManagerEx.EXTRA_RINGTONE_TYPE, -1);
        }

        mRingtoneInfo.setParentType(mParentType);

        //do_InitRingtoneActionbar();
        do_InitDefalutSetting();
        //do_InitTitleName();
        do_InitSIMDependancyMenu();
        //[E][Settings] [donghan07.lee] Add DRM policy 2012.02.14
        //setContentView(R.xml.ringtone_picker);

        View row = inflater.inflate(R.xml.ringtone_picker, container, false);
        do_InitCancelOkBtn(row);

        return row;
        // [S][Settings] [donghan07.lee] The volume keys will control the stream that we are choosing a ringtone for
    }

    private void do_setBreadCrumb() {
        mHeaderView = LayoutInflater.from(getActivity()).inflate(
                R.layout.ringtonepicker_header, null);
        mButton = (ImageButton)mHeaderView.findViewById(R.id.add_button);
        mButton2 = (ImageButton)mHeaderView.findViewById(R.id.del_button);
        //mButton.setId(AUTORUN_BUTTON_ID);
        //mButton.setBackgroundColor(Color.TRANSPARENT);
        mButton.setImageResource(R.drawable.ic_menu_add_field_holo_light);
        mButton2.setImageResource(R.drawable.common_menu_trash_holo_light);

        if (checkDeleteRingtoneExist()) {
            mButton2.setVisibility(View.VISIBLE);
        } else {
            mButton2.setVisibility(View.INVISIBLE);
        }

        final int padding = getActivity().getResources().getDimensionPixelSize(
                R.dimen.action_bar_option_menu_padding);

        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mButton.setClickable(true);
            mButton.setBackground(
                    getActivity().getResources().getDrawable(R.drawable.breadcrumb_background));
            mButton.setPaddingRelative(0, 0, 0, 0);
            mButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    if (mRingtoneInfo.appIsEnabled()) {
                        // go to mp3 list (music app)
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_PICK);
                        intent.setData(Uri.parse("content://media/external/audio/media"));
                        intent.putExtra(Drm.EXTRA_CONTENT_ACTION, Drm.CONTENT_ACT_RINGTONE);
                        startActivityForResult(intent, LAUNCHED_REQUEST_CODE);
                    } else {
                        confirmDialog();
                    }
                }
            });
            mButton2.setClickable(true);
            mButton2.setBackground(
                    getActivity().getResources().getDrawable(R.drawable.breadcrumb_background));
            mButton2.setPaddingRelative(0, 0, 0, 0);
            mButton2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    if (mRingtoneInfo.appIsEnabled()) {
                        // go to mp3 list (music app)
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                                Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.setClassName("com.android.settings",
                                "com.android.settings.RingtoneDeleteActivity");
                        i.putExtra(RingtoneManagerEx.EXTRA_RINGTONE_TYPE, mParentType);
                        startActivity(i);
                    } else {
                        confirmDialog();
                    }
                }
            });
        } else {
            mButton.setPaddingRelative(0, 0, padding, 0);
            mButton2.setPaddingRelative(0, 0, padding, 0);
        }
    }

    private int getListPosition(int ringtoneManagerPos) {

        // If the manager position is -1 (for not found), return that
        if (ringtoneManagerPos < 0) {
            return ringtoneManagerPos;
        }

        return ringtoneManagerPos + mStaticItemCount;
    }

    // ///////////////////////////// LGE_UPDATE_S pae.jaeyong@lge.com (
    // 2010.4.27 ) //////////////////////////////

    // ANDY_END
    //[E][Settings] [donghan07.lee] Add DRM policy 2012.02.14

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if (Utils.supportSplitView(getActivity())) {
            do_setBreadCrumb(); //KLP
        }
        initRingtoneList();
        initRingtoneUI();

        // using breadcrumb
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            SettingsBreadCrumb breadcrumb = SettingsBreadCrumb.get(getActivity());
            if (breadcrumb != null) {
                breadcrumb.addView(mHeaderView, null);
            }
        }
        getActivity().invalidateOptionsMenu();
    }

    private boolean checkDeleteRingtoneExist() {
        Cursor tempCursor;
        tempCursor = mRingtoneInfo
                .getCursor(mRingtoneInfo.EXTERNAL_CURSOR_TYPE);
        try {
            if (tempCursor != null) {
                if (tempCursor.getCount() > 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } finally {
            if (tempCursor != null) {
                tempCursor.close();
                tempCursor = null;
            }
        }
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        /*
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM);

        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                                       LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                                       Gravity.CENTER_VERTICAL | Gravity.END);
        getActionBar().setCustomView(mRingtoneAddBtn, lp);
        */
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        stopAnyPlayingRingtone();
        if (mDeleteDialog != null) {
            mDeleteDialog.dismiss();
            mDeleteDialog = null;
        }
        if (mExternalCursor != null || items != null || mInternalCursor != null) {
            try {
                mExternalCursor = null;
                mInternalCursor = null;
                mInternalSatoTakuCursor = null;
                items = null;
                if (mInternalDisneyCursor != null
                        || mInternalDisneyCursor != null) {
                    mInternalDisneyCursor = null;
                    mInternalLGCursor = null;

                }
            } catch (Exception e) {
                Log.w(TAG, "Exception");
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopAnyPlayingRingtone();

        getActivity().getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_CUSTOM);
        getActivity().getActionBar().setCustomView(null);
        //[S]expired DRM check
        if (mRingtoneInfo.isExpiredDrm()) {
            Log.d(TAG, "!!!!!it is expired uri change to default!!!!");
            Uri uri = mRingtoneInfo.getDefaultPhoneRingtone();
            mRingtoneInfo.do_setURI(uri, mParentType);
        }
        //[E]expired DRM check
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            SettingsBreadCrumb breadcrumb = SettingsBreadCrumb.get(getActivity());
            if (breadcrumb != null) {
                breadcrumb.removeView(mHeaderView);
            }
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        //[S][2012.03.08][donghan07.lee][common][common] Activity finish when SD card change event
        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
        //[E][2012.03.08][donghan07.lee][common][common] Activity finish when SD card change event
        if (mAudio != null) {
            mAudio.release();
        }
    }

    // prepare thie ringtone list
    private void initRingtoneList() {
        items = new ArrayList<RingtoneItem>();

        mStaticItemCount = 0;

        mInternalCursor = mRingtoneInfo.getCursor(mRingtoneInfo.INTERNAL_CURSOR_TYPE);
        if (mIsSatotakuRingtoneModel) {
            mInternalSatoTakuCursor = mRingtoneInfo.getCursor(mRingtoneInfo.SATOTAKU_CURSOR_TYPE);
        }

        mExternalCursor = mRingtoneInfo.getCursor(mRingtoneInfo.EXTERNAL_CURSOR_TYPE);

        String my_ring_category = "";
        String lg_ring_category = "";
        String ts_ring_category = ""; //sato-taku
        if (mParentType == RingtonePickerInfo.TYPE_RINGTONE ||
                mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM2 ||
                mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM3 ||
                mParentType == RingtonePickerInfo.TYPE_RINGTONE_VC) {
            my_ring_category = getString(R.string.sp_my_ringtones_NORMAL);
            lg_ring_category = getString(R.string.sp_lg_ringtones_NORMAL);
            if (mIsSatotakuRingtoneModel) {
                ts_ring_category = getString(R.string.sp_ts_ringtones_NORMAL);
            }
        }
        //for Notification ringtone
        else if (mParentType == RingtonePickerInfo.TYPE_NOTIFICATION ||
                mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM2 ||
                mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM3) {
            my_ring_category = getString(R.string.my_notification_sound);
            lg_ring_category = getString(R.string.lg_notification_sound);
        }

        // [S][WBT] null exception
        if (mExternalCursor != null) {
            if (mExternalCursor.getCount() > 0) {
                mExternalCursor.moveToFirst();
                items.add(new RingtoneSectionItem(
                        my_ring_category));
                //[S][Settings] [donghan07.lee] Add DRM policy 2012.02.14
                mStaticItemCount++;
                //[E][Settings] [donghan07.lee] Add DRM policy 2012.02.14
                for (int i = 0; i < mExternalCursor.getCount(); i++) {
                    items.add(new RingtoneEntryItem(mExternalCursor
                            .getString(1), mExternalCursor.getString(2) + "/"
                            + mExternalCursor.getString(0)));
                    mExternalCursor.moveToNext();
                }
            }
        }
        // [DCM] Disney model Ringtone , Notification

        if (Utils.isDisneyModel(getActivity())) {
            getDisneyList(lg_ring_category);
        } else {
            if (mIsSatotakuRingtoneModel) {
                if (mParentType == RingtonePickerInfo.TYPE_RINGTONE) {
                    items.add(new RingtoneSectionItem(ts_ring_category));

                    mStaticItemCount++;

                    if (mHasDefaultItem) {
                        mDefaultRingtonePos = items.size() - 1;

                        if (RingtoneManagerEx.isDefault(mExistingUri)) {
                            mClickedPos = mDefaultRingtonePos;
                        }
                    }

                    if (mHasSilentItem) {
                        mSilentPos = items.size();

                        // The 'Silent' item should use a null Uri
                        if (mExistingUri == null) {
                            mClickedPos = mSilentPos;
                        }
                    }

                    if (mClickedPos == -1) {
                        mClickedPos = getListPosition(mRingtoneManager
                                .getRingtonePosition(mExistingUri));
                    }

                    if (mClickedPos == -1) {
                        if (mInternalSatoTakuCursor != null) {
                            int nIdexDefaultRingtone = -1;
                            int nCount = mInternalSatoTakuCursor.getCount();
                            int nColumeIndex = mInternalSatoTakuCursor
                                    .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                            if (nColumeIndex != -1) {
                                mInternalSatoTakuCursor.moveToFirst();
                                String pathName;
                                String defaultPath = mRingtoneInfo.getDefaultPath();
                                Log.v(TAG, "defaultPath = " + defaultPath
                                        + ", mHasSilentItem = " + mHasSilentItem);
                                for (int index = 0; index < nCount; index++) {
                                    pathName = mInternalSatoTakuCursor.getString(nColumeIndex);
                                    Log.v(TAG, "pathName = " + pathName);
                                    if (pathName.equals(defaultPath)) {
                                        nIdexDefaultRingtone = getListPosition(index);
                                        break;
                                    }
                                    mInternalSatoTakuCursor.moveToNext();
                                }
                                mClickedPos = nIdexDefaultRingtone; // RingtoneManager.INDEX_DEFAULT_RINGTONE;
                            } else {
                                Log.e(TAG,
                                        "onPrepareListView() MediaStore.Audio.Media.DISPLAY_NAME colume is error!!! nColumeIndex "
                                                + nColumeIndex);
                            }
                        }
                    }
                    if (mInternalSatoTakuCursor != null) {
                        mInternalSatoTakuCursor.moveToNext();
                        for (int i = 0; i < mInternalSatoTakuCursor.getCount(); i++) {
                            items.add(new RingtoneEntryItem(mInternalSatoTakuCursor.getString(1),
                                    mInternalSatoTakuCursor.getString(2) + "/"
                                            + mInternalSatoTakuCursor.getString(0)));
                            mInternalSatoTakuCursor.moveToNext();
                        }
                    }
                }
            }
            if (items.size() > 0) {
                items.add(new RingtoneSectionItem(
                        lg_ring_category));
                //[S][Settings] [donghan07.lee] Add DRM policy 2012.02.14
                mStaticItemCount++;
            }

            // Cosmo onPrepareListView Merge...
            if (mHasDefaultItem) {
                mDefaultRingtonePos = items.size() - 1;

                if (RingtoneManagerEx.isDefault(mExistingUri)) {
                    mClickedPos = mDefaultRingtonePos;
                }
            }

            if (mHasSilentItem) {
                mSilentPos = items.size();
                items.add(new RingtoneEntryItem(getString(R.string.ringtone_silent), null));
                mStaticItemCount++;
                Log.d(TAG, "mHasSilentItem staticcount = " + mStaticItemCount);
                // The 'Silent' item should use a null Uri
                if (mExistingUri == null) {
                    mClickedPos = mSilentPos;
                }
            }

            if (mClickedPos == -1) {
                mClickedPos = getListPosition(mRingtoneManager
                        .getRingtonePosition(mExistingUri));
            }

            if (mClickedPos == -1) {
                if (mInternalCursor != null) {
                    int nIdexDefaultRingtone = -1;
                    int nCount = mInternalCursor.getCount();
                    int nColumeIndex = mInternalCursor
                            .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                    if (nColumeIndex != -1) {
                        mInternalCursor.moveToFirst();
                        String pathName;
                        String defaultPath = mRingtoneInfo.getDefaultPath();
                        Log.v(TAG, "defaultPath = " + defaultPath
                                + ", mHasSilentItem = " + mHasSilentItem);
                        for (int index = 0; index < nCount; index++) {
                            pathName = mInternalCursor.getString(nColumeIndex);
                            Log.v(TAG, "pathName = " + pathName);
                            if (pathName.equals(defaultPath)) {
                                nIdexDefaultRingtone = getListPosition(index);
                                break;
                            }
                            mInternalCursor.moveToNext();
                        }
                        mClickedPos = nIdexDefaultRingtone; // RingtoneManager.INDEX_DEFAULT_RINGTONE;
                    } else {
                        Log.e(TAG,
                                "onPrepareListView() MediaStore.Audio.Media.DISPLAY_NAME colume is error!!! nColumeIndex "
                                        + nColumeIndex);
                    }
                }
            }

            // [S][WBT] null exception
            if (mInternalCursor != null) {
                mInternalCursor.moveToNext();
                for (int i = 0; i < mInternalCursor.getCount(); i++) {
                    items.add(new RingtoneEntryItem(mInternalCursor
                            .getString(1), mInternalCursor.getString(2) + "/"
                            + mInternalCursor.getString(0)));
                    mInternalCursor.moveToNext();

                }
            }
        }
    }

    private void getDisneyList(String lg_ring_category) {
        if (mParentType == RingtonePickerInfo.TYPE_RINGTONE) {
            items.add(new RingtoneSectionItem("Disney Ringtone"));
            mStaticItemCount++;
        }

        if (mParentType == RingtonePickerInfo.TYPE_NOTIFICATION) {
            items.add(new RingtoneSectionItem("Disney Notification Sound"));
            mStaticItemCount++;
        }
        setListItemCousor(mInternalDisneyCursor);

        mInternalDisneyCursor = mRingtoneInfo.getCursor(mRingtoneInfo.DISNEY_CURSOR_TYPE);
        if (mInternalDisneyCursor != null) {
            mInternalDisneyCursor.moveToNext();
            Log.d("jw", "mInternalDisneyCursor.getCount() : "
                    + mInternalDisneyCursor.getCount());
            for (int i = 0; i < mInternalDisneyCursor.getCount(); i++) {
                items.add(new RingtoneEntryItem(mInternalDisneyCursor.getString(1),
                        mInternalDisneyCursor.getString(2) + "/"
                                + mInternalDisneyCursor.getString(0)));
                mInternalDisneyCursor.moveToNext();
            }
        }
        mInternalLGCursor = mRingtoneInfo.getCursor(mRingtoneInfo.LG_CURSOR_TYPE);
        if (items.size() > 0) {
            items.add(new RingtoneSectionItem(
                    lg_ring_category));
            //[S][Settings] [donghan07.lee] Add DRM policy 2012.02.14
            mStaticItemCount++;
        }

        setListItemCousor(mInternalLGCursor);

        if (mInternalLGCursor != null) {
            mInternalLGCursor.moveToNext();
            Log.d("jw", "mInternalLGCursor.getCount() : "
                    + mInternalLGCursor.getCount());
            for (int i = 0; i < mInternalLGCursor.getCount(); i++) {
                items.add(new RingtoneEntryItem(mInternalLGCursor.getString(1),
                        mInternalLGCursor.getString(2) + "/"
                                + mInternalLGCursor.getString(0)));
                mInternalLGCursor.moveToNext();
            }
        }
    }

    private void setListItemCousor(Cursor cursor) {
        if (mClickedPos == -1) {
            if (cursor != null) {
                int nIdexDefaultRingtone = -1;
                int nCount = cursor.getCount();
                int nColumeIndex = cursor
                        .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                if (nColumeIndex != -1) {
                    cursor.moveToFirst();
                    String pathName;
                    String defaultPath = mRingtoneInfo.getDefaultPath();
                    Log.v(TAG, "defaultPath = " + defaultPath
                            + ", mHasSilentItem = " + mHasSilentItem);
                    for (int index = 0; index < nCount; index++) {
                        pathName = cursor.getString(nColumeIndex);
                        Log.v(TAG, "pathName = " + pathName);
                        if (pathName.equals(defaultPath)) {
                            nIdexDefaultRingtone = getListPosition(index);
                            break;
                        }
                        cursor.moveToNext();
                    }
                    mClickedPos = nIdexDefaultRingtone; // RingtoneManager.INDEX_DEFAULT_RINGTONE;
                } else {
                    Log.e(TAG,
                            "onPrepareListView() MediaStore.Audio.Media.DISPLAY_NAME colume is error!!! nColumeIndex "
                                    + nColumeIndex);
                }
            }
        }
    }

    // init list UI
    private void initRingtoneUI() {
        RingtoneEntryAdapter adapter = new RingtoneEntryAdapter(getActivity(),
                android.R.layout.simple_list_item_single_choice, items);

        setListAdapter(adapter);

        listView = getListView();
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        /* //not support delete function until scenario accept : hakgyu98.kim
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int row, long arg3) {
                // your code
                mRemovePos = row;
                if (mExternalCursor != null) {
                    ContentResolver resolver = getContentResolver();
                    RingtoneEntryItem item = (RingtoneEntryItem) items.get(mRemovePos);
                    Uri ringUri = Uri.parse(item.uri);
                    Cursor c = mRingtoneInfo.getCursor(mRingtoneInfo.EXTERNAL_CURSOR_TYPE);
                    int external_pos = mRingtoneInfo.getRingtoneUriPosition(ringUri, c);

                    if (external_pos != -1) {
                        showDeleteDialog();
                    }
                }
                return false;
            }
        });
        */
        if (Utils.isDisneyModel(getActivity())) {
            setDisneyUI();
        } else {
            onPrepareUI();
        }
    }

    private void setDisneyUI() {
        Uri ringtone_uri = mRingtoneInfo.getURI();
        Uri default_ringtone_uri = mRingtoneInfo.getDefaultPhoneRingtone();

        int mDisneyPosition = mRingtoneInfo.getRingtoneUriPosition(ringtone_uri,
                mInternalDisneyCursor);
        int mLGPosition = mRingtoneInfo.getRingtoneUriPosition(ringtone_uri,
                mInternalLGCursor);
        Log.d("test", "TestPosition1 : " + mDisneyPosition);
        Log.d("test", "TestPosition2 : " + mLGPosition);

        if (mExternalCursor != null) {
            external_ringtone_position = mRingtoneInfo.getRingtoneUriPosition(ringtone_uri,
                    mExternalCursor);
        }

        setDefaultURI(default_ringtone_uri, mDisneyPosition, mLGPosition);

        if (mExternalCursor != null && mExternalCursor.getCount() > 0) {
            if (external_ringtone_position == -1) {
                if (mLGPosition == -1) {
                    int position = mDisneyPosition + mExternalCursor.getCount()
                            + mStaticItemCount - 1;
                    listView.setItemChecked(position, true);
                    listView.setSelection(position);
                    mClickedPos = position;

                } else {
                    int position = mLGPosition + mExternalCursor.getCount()
                            + mInternalDisneyCursor.getCount()
                            + mStaticItemCount;
                    listView.setItemChecked(position, true);
                    listView.setSelection(position);
                    mClickedPos = position;
                }
            } else {
                listView.setItemChecked(external_ringtone_position + 1, true);
                listView.setSelection(external_ringtone_position + 1);
                mClickedPos = external_ringtone_position + 1;
            }
        } else {
            if (mLGPosition == -1) {
                int position = mDisneyPosition + mStaticItemCount - 1;
                listView.setItemChecked(position, true);
                listView.setSelection(position);
                mClickedPos = position;
            } else {
                int position = mLGPosition + mStaticItemCount
                        + mInternalDisneyCursor.getCount();
                listView.setItemChecked(position, true);
                listView.setSelection(position);
                mClickedPos = position;
            }
        }
        closedCursor();
    }

    private void setDefaultURI(Uri default_ringtone_uri, int mDisneyPosition,
            int mLGPosition) {
        if ((mDisneyPosition == -1 && mLGPosition == -1)
                && external_ringtone_position == -1) {
            if (mParentType == RingtonePickerInfo.TYPE_RINGTONE ||
                    mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM2 ||
                    mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM3 ||
                    mParentType == RingtonePickerInfo.TYPE_RINGTONE_VC) {
                mRingtoneInfo.do_setURI(default_ringtone_uri, RingtonePickerInfo.TYPE_RINGTONE);
                mRingtoneInfo.do_setURI(default_ringtone_uri, RingtonePickerInfo.TYPE_RINGTONE_VC);
            } else if (mParentType == RingtonePickerInfo.TYPE_NOTIFICATION ||
                    mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM2 ||
                    mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM3) {
                mRingtoneInfo.do_setURI(default_ringtone_uri, RingtonePickerInfo.TYPE_NOTIFICATION);
            }
        }
    }

    private void closedCursor() {
        if (mExternalCursor != null) {
            mExternalCursor.close();
        }
        if (mInternalCursor != null) {
            mInternalCursor.close();
        }
        if (mInternalDisneyCursor != null) {
            mInternalDisneyCursor.close();
        }
        if (mInternalLGCursor != null) {
            mInternalLGCursor.close();
        }
    }

    // prepare UI
    private void onPrepareUI() {
        Uri ringtone_uri = null;
        if (mIsAddSoundProfileMode) {
            ringtone_uri = Uri.parse(mSoundProfileInfo
                    .getSoundProfileEachData(mSoundProfileInfo.
                            getRingtoneIndexbyParentType(mParentType)));
        } else {
            if ("CMCC".equals(Config.getOperator()) || "CTC".equals(Config.getOperator())
                || "CMO".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
                ringtone_uri = mRingtoneInfo.getURI();
                if (mSoundProfileInfo.getUserProfileName().equals("")) {
                    Uri ringtoneUriFromPref = Uri.parse(mSoundProfileInfo
                         .getSoundDefaultData(mSoundProfileInfo.
                                 getRingtoneIndexbyParentType(mParentType)));
                    if (!ringtoneUriFromPref.equals(ringtone_uri)) {
                        mSoundProfileInfo.setDeafultSoundValue_Default();
                        ringtone_uri = Uri.parse(mSoundProfileInfo
                                           .getSoundDefaultData(mSoundProfileInfo.
                                               getRingtoneIndexbyParentType(mParentType)));
                    } else {
                        ringtone_uri = ringtoneUriFromPref;
                    }
                }
                Log.d(TAG, "mSoundProfileInfo.getUserProfileName() : " + mSoundProfileInfo.getUserProfileName());
            } else {
                ringtone_uri = mRingtoneInfo.getURI();
            }
            Log.d(TAG, "ringtone_uri : " + ringtone_uri);
        }
        Uri default_ringtone_uri = mRingtoneInfo.getDefaultPhoneRingtone();

        if (mInternalCursor != null) {
            internal_ringtone_position = mRingtoneInfo.getRingtoneUriPosition(ringtone_uri,
                    mInternalCursor);
        }

        if (mParentType == RingtonePickerInfo.TYPE_RINGTONE && mIsSatotakuRingtoneModel) {
            if (mInternalSatoTakuCursor != null) {
                if (internal_ringtone_position == -1) {
                    internal_ringtone_position = mRingtoneInfo.getRingtoneUriPosition(ringtone_uri,
                            mInternalSatoTakuCursor);
                }
                else {
                    internal_ringtone_position = mRingtoneInfo.getRingtoneUriPosition(ringtone_uri,
                            mInternalCursor) + mInternalSatoTakuCursor.getCount() + 1;
                }
            }
        }

        if (mExternalCursor != null) {
            external_ringtone_position = mRingtoneInfo.getRingtoneUriPosition(ringtone_uri,
                    mExternalCursor);
        }
        // [S][Settings][donghan07.lee] Add default rigtone URI when SD card
        // removed.
        if (mInternalCursor != null) {
            default_ringtone_position = mRingtoneInfo.getRingtoneUriPosition(default_ringtone_uri,
                    mInternalCursor);
        }

        if (internal_ringtone_position == -1 && external_ringtone_position == -1) {
            if (mParentType == RingtonePickerInfo.TYPE_RINGTONE ||
                    mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM2 ||
                    mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM3 ||
                    mParentType == RingtonePickerInfo.TYPE_RINGTONE_VC) {
                mRingtoneInfo.do_setURI(default_ringtone_uri, RingtonePickerInfo.TYPE_RINGTONE);
                mRingtoneInfo
                        .do_setURI(default_ringtone_uri, RingtonePickerInfo.TYPE_RINGTONE_SIM2);
                mRingtoneInfo
                        .do_setURI(default_ringtone_uri, RingtonePickerInfo.TYPE_RINGTONE_SIM3);
                mRingtoneInfo.do_setURI(default_ringtone_uri, RingtonePickerInfo.TYPE_RINGTONE_VC);
            } else if (mParentType == RingtonePickerInfo.TYPE_NOTIFICATION ||
                    mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM2 ||
                    mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM3) {
                mRingtoneInfo.do_setURI(default_ringtone_uri, RingtonePickerInfo.TYPE_NOTIFICATION);
                mRingtoneInfo.do_setURI(default_ringtone_uri,
                        RingtonePickerInfo.TYPE_NOTIFICATION_SIM2);
                mRingtoneInfo.do_setURI(default_ringtone_uri,
                        RingtonePickerInfo.TYPE_NOTIFICATION_SIM3);
            }
        }

        // external list exist
        // [S][2012.02.03][donghan07.lee][ICS Settings][Not for TD] Default
        // Ringtone issue when SD card removed.
        if (mExternalCursor != null && mExternalCursor.getCount() > 0) {
            Log.d(TAG, "internal_ringtone_position = "
                    + internal_ringtone_position);
            if (external_ringtone_position == -1) {

                // [S][Settings][donghan07.lee] Add default rigtone URI when SD
                // card removed.
                if (internal_ringtone_position == -1) {
                    if (ringtone_uri == null && mHasSilentItem) {
                        listView.setItemChecked(mExternalCursor.getCount() + mStaticItemCount - 1,
                                true);
                        listView.setSelection(mExternalCursor.getCount() + mStaticItemCount - 1);
                        mClickedPos = mExternalCursor.getCount() + mStaticItemCount - 1;
                    } else {
                        listView.setItemChecked(default_ringtone_position
                                + mExternalCursor.getCount() + mStaticItemCount, true);
                        listView.setSelection(default_ringtone_position
                                + mExternalCursor.getCount() + mStaticItemCount);
                        mClickedPos = default_ringtone_position
                                + mExternalCursor.getCount()
                                + mStaticItemCount;
                    }
                } else {
                    listView.setItemChecked(internal_ringtone_position
                            + mExternalCursor.getCount() + mStaticItemCount, true);
                    listView.setSelection(internal_ringtone_position
                            + mExternalCursor.getCount() + mStaticItemCount);
                    mClickedPos = internal_ringtone_position
                            + mExternalCursor.getCount()
                            + mStaticItemCount;
                }
                // [E][Settings][donghan07.lee] Add default rigtone URI when SD
                // card removed.

            } else {
                listView.setItemChecked(external_ringtone_position + 1, true);
                listView.setSelection(external_ringtone_position + 1);
                mClickedPos = external_ringtone_position + 1;
            }
        }
        // external list not exist
        else {
            // [S][Settings][donghan07.lee] Add default rigtone URI when SD card
            // removed.
            if (internal_ringtone_position == -1) {
                if (ringtone_uri == null && mHasSilentItem) {
                    listView.setItemChecked(mStaticItemCount - 1, true);
                    listView.setSelection(mStaticItemCount - 1);
                    mClickedPos = mStaticItemCount - 1;
                } else {
                    listView.setItemChecked(default_ringtone_position + mStaticItemCount, true);
                    listView.setSelection(default_ringtone_position + mStaticItemCount);
                    mClickedPos = default_ringtone_position + mStaticItemCount;
                }
            } else {
                listView.setItemChecked(internal_ringtone_position + mStaticItemCount, true);
                listView.setSelection(internal_ringtone_position + mStaticItemCount);
                mClickedPos = internal_ringtone_position + mStaticItemCount;
            }
            // [E][Settings][donghan07.lee] Add default rigtone URI when SD card
            // removed.
            // [E][2012.12.03][donghan07.lee][ICS Settings][Not for TD] Default
            // Ringtone issue when SD card removed.
        }

        //[S][Settings][donghan07.lee] Add curdor close 2012.04.10
        if (mExternalCursor != null) {
            mExternalCursor.close();
        }
        if (mInternalCursor != null) {
            mInternalCursor.close();
        }
        if (mInternalSatoTakuCursor != null) {
            mInternalSatoTakuCursor.close();
        }
        //[E][Settings][donghan07.lee] Add curdor close 2012.04.10

    }

    // click the list item
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);

        //[S][Settings] [donghan07.lee] Click timing error fixed 2012.02.26
        try {

            if (position == mSilentPos) {
                Log.d(TAG, "silent position = " + position);
                //position_uri = null;
                mClickedPos = position;
                playRingtone(null, DELAY_MS_SELECTION_PLAYED);
                return;
            } else if (!items.get(position).isSection()) {

                Log.d(TAG, "position = " + position);

                //[S][Settings] [donghan07.lee] Add DRM policy 2012.02.14
                // ANDY_PORTING LGDRM [lg-drm@lge.com 100414]
                // Save the position of most recently clicked item
                mClickedPos = position;

                mSelected = true;
                mDrmRingtone = true;
                // ANDY_END
                //[E][Settings] [donghan07.lee] Add DRM policy 2012.02.14

                RingtoneEntryItem item = (RingtoneEntryItem)items.get(position);

                position_uri = Uri.parse(item.uri);
                // Log.d(TAG,"position = "+clicked_position);
                Log.d(TAG, "position_uri = " + position_uri);

                //LGE_S. Docomo. nexti. dongkyu31.lee
                if (mOperator.equals(strTargetOperator)) {
                    String path = mRingtoneInfo.GetFilePath(position_uri.toString());
                    boolean bCanHandle = false;
                    Log.d(TAG, "~~~31> creating DrmManagerClient. ");
                    if (null == mDrmManagerClient) {
                        Log.d(TAG, "~~~31> mDrmStore is null");
                        mDrmManagerClient = new DrmManagerClient(mContext);
                    }
                    if (path != null) {
                        //check if the content is PlayReady DRM contents or not
                        try {
                            Log.d(TAG, "~~~31> check. mDrmManagerClient.canHandle()");
                            bCanHandle = mDrmManagerClient.canHandle(path, "audio/mp4");
                            Log.d(TAG, "~~~31> nCanHandle is " + bCanHandle);
                        } catch (Exception ex) {
                            Log.e(TAG, "~~~31> exception is occured during checking canHandle");
                        }

                        if (bCanHandle == true) {
                            //Check Ringtone flags of the Docomo PlayReady DRM contents
                            try {
                                int nDrmStatus;
                                Log.d(TAG, "~~~31> checkRightsStatus()");

                                //nDrmStatus = checkRightsStatus(path, mDrmStore.Action.RINGTONE);
                                nDrmStatus = mDrmManagerClient.checkRightsStatus(path, 2);
                                switch (nDrmStatus) {
                                case mRIGHTS_EXPIRED:
                                    Log.e(TAG, "~~~31>  mDrmStore.RightsStatus.RIGHTS_EXPIRED ");
                                case mRIGHTS_INVALID:
                                    Log.e(TAG, "~~~31>  mDrmStore.RightsStatus.RIGHTS_INVALID");
                                case mRIGHTS_NOT_ACQUIRED:
                                    Log.e(TAG,
                                            "~~~31>  mDrmStore.RightsStatus.RIGHTS_NOT_ACQUIRED ");
                                    Toast.makeText(
                                            getActivity(),
                                            getString(R.string.sp_toast_protected_ringtone_set_NORMAL),
                                            Toast.LENGTH_LONG).show();
                                    mOKButton.setClickable(false);
                                    mOKButton.setEnabled(false);
                                    break;
                                case mRIGHTS_VALID:
                                    Log.e(TAG, "~~~31>  mDrmStore.RightsStatus.RIGHTS_VALID");
                                    mOKButton.setClickable(true);
                                    mOKButton.setEnabled(true);
                                    try {
                                        Log.d(TAG, "~~~31> mAudio =  new MediaPlayer(); ");
                                        mAudio = new MediaPlayer();
                                        Log.d(TAG,
                                                "~~~31> int mStreamType = AudioManager.STREAM_MUSIC ");
                                        int mStreamType = AudioManager.STREAM_MUSIC;
                                        Log.d(TAG,
                                                "~~~31> mAudio.setDataSource(this, position_uri) ");
                                        mAudio.setDataSource(getActivity(), position_uri);
                                        Log.d(TAG, "~~~31> mAudio.setAudioStreamType(mStreamType) ");
                                        mAudio.setAudioStreamType(mStreamType);
                                        Log.d(TAG, "~~~31> mAudio.prepare() ");
                                        mAudio.prepare();
                                        Log.d(TAG, "~~~31> finished try catch");
                                        playRingtone(position_uri, DELAY_MS_SELECTION_PLAYED);
                                    } catch (Exception ex) {
                                        Log.d(TAG, "~~~31> Play exception is occured.");
                                        //Toast.makeText(RingtonePicker.this,"Protected file. Cannot be set as ringtone",    Toast.LENGTH_LONG).show();
                                        Toast.makeText(
                                                getActivity(),
                                                getString(R.string.sp_toast_protected_ringtone_set_NORMAL),
                                                Toast.LENGTH_LONG).show();
                                        mOKButton.setClickable(false);
                                        mOKButton.setEnabled(false);
                                    }

                                    //playRingtone(position_uri, DELAY_MS_SELECTION_PLAYED);
                                    break;
                                default:
                                    Log.e(TAG, "~~~31> default");
                                    //Toast.makeText(RingtonePicker.this,"Protected file. Cannot be set as ringtone",    Toast.LENGTH_LONG).show();
                                    Toast.makeText(
                                            getActivity(),
                                            getString(R.string.sp_toast_protected_ringtone_set_NORMAL),
                                            Toast.LENGTH_LONG).show();
                                    mOKButton.setClickable(false);
                                    mOKButton.setEnabled(false);
                                    break;
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "~~~31> exception is occured whiling check nDrmStatus"
                                        + ex);
                            }
                        } else {
                            mOKButton.setClickable(true);
                            mOKButton.setEnabled(true);
                            playRingtone(position_uri, DELAY_MS_SELECTION_PLAYED);
                        }
                    }//End of if(path != null)

                }
                 //LGE_E. Docomo. nexti. dongkyu31.lee

                else { //dongkyu31.lee

                    // play Ringtone
                    playRingtone(position_uri, DELAY_MS_SELECTION_PLAYED);
                }
            }
        } catch (NullPointerException e) {
            getActivity().finish();
        }
        //[E][Settings] [donghan07.lee] Click timing error fixed 2012.02.26
    }

    // play Ringtone
    private void playRingtone(Uri uri, int delayMs) {
        mHandler.removeCallbacks(this);
        mSampleRingtoneUri = uri;
        mSampleRingtonePos = mClickedPos;
        mHandler.postDelayed(this, delayMs);
    }

    public void run() {

        //[S][Settings] [donghan07.lee] Add DRM policy 2012.02.14
        if (mSampleRingtonePos == mSilentPos) {
            // LGE_CHANGE_S : stop default ringtone, too. on 20110831,
            // minsoo3.kim
            // mRingtoneManager.stopPreviousRingtone();
            stopAnyPlayingRingtone();
            // LGE_CHANGE_E : stop default ringtone, too. on 20110831,
            // minsoo3.kim

            // ANDY_PORTING LGDRM [lg-drm@lge.com 100414]
            mSelected = false;
            // ANDY_END
            return;
        }

        // ANDY_PORTING LGDRM [lg-drm@lge.com 100414]
        if (mDrmJob) {
            mHandler.removeCallbacks(mDrmTask);
            mDrmJob = false;
        }
        // ANDY_END
        //[E][Settings] [donghan07.lee] Add DRM policy 2012.02.14

        // Stop the default ringtone, if it's playing (other ringtones will be
        // stopped by the RingtoneManager when we get another Ringtone from it.
        //
        if (mDefaultRingtone != null && mDefaultRingtone.isPlaying()) {
            mDefaultRingtone.stop();
            mDefaultRingtone = null;
        }

        if (mSampleRingtoneUri == null) {
            return;
        }

        mDefaultRingtone = mRingtoneManager.getRingtone(getActivity(), mSampleRingtoneUri);
        //[S][Settings] [donghan07.lee] Add play stream 2012.03.25
        if (mDefaultRingtone != null) {
            //mRingtoneManager.setType(mParentType);
            mDefaultRingtone.setStreamType(mRingtoneManager.inferStreamType());
        }
        //[E][Settings] [donghan07.lee] Add play stream 2012.03.25
        Ringtone ringtone;
        ringtone = mDefaultRingtone;

        if (ringtone != null) {
            //[S][Settings] [donghan07.lee] Add DRM policy 2012.02.14
            // ANDY_PORTING LGDRM [lg-drm@lge.com 100414]

            if (Config.getFWConfigBool(mContext,
                    com.lge.config.ConfigBuildFlags.CAPP_DRM,
                    "com.lge.config.ConfigBuildFlags.CAPP_DRM")) {
                int nStatus = 0;
                String path = mRingtoneInfo.GetFilePath(mSampleRingtoneUri.toString());
                nStatus = DrmFwExt.checkDRMRingtone(getActivity(), path, true, false, false);

                if (nStatus == 1) {
                    if (mSelected) {
                        // Selected file is not allowed to set as a ringtone
                        mDrmRingtone = false;
                        // TODO: Apply translated string, not hard coded english
                        Toast.makeText(getActivity(),
                                //"Protected file. Cannot be set as ringtone",
                                getString(R.string.sp_toast_protected_ringtone_set_NORMAL),
                                Toast.LENGTH_LONG).show();
                    }
                    mSelected = false;
                    return; // Not allowed as a ringtone
                }
                //[S][Settings] [donghan07.lee] for KR DRM policy 2012.02.24

                if (path != null) {
                    int mDrmFile = DrmManager.isDRM(path);
                    if (mDrmFile != Drm.CONTENT_TYPE_SKT && mDrmFile != Drm.CONTENT_TYPE_LGU
                            && mDrmFile != Drm.CONTENT_TYPE_FL) {
                        if (nStatus == 2) {
                            // Stop playing after 5 seconds elapsed
                            mDrmJob = true;
                            mHandler.postDelayed(mDrmTask, 5 * 1000);
                        }
                    }
                }
                //[E][Settings] [donghan07.lee] for KR DRM policy 2012.02.24

            }

            // ANDY_END
            //[E][Settings] [donghan07.lee] Add DRM policy 2012.02.14
            if (mAudioManager.getMode() == AudioManagerEx.MODE_IN_CALL ||
                    mAudioManager.getMode() == AudioManagerEx.MODE_IN_COMMUNICATION) {
                return;
            }
            //[AUDIO_FWK]_START, 20120116, dk.han@lge.com, Use AudioFocus if headset was connected and playing BGM. {

             /* not used
            if (mAudioManager.isWiredHeadsetOn() == true) {
                int streamType = AudioManager.STREAM_RING;
                if (mRingtoneManager != null) {
                    streamType = mRingtoneManager.inferStreamType();
                }
            }
            ringtone.play();
            */

            boolean isSoundException = SystemProperties.getBoolean("ro.lge.audio_soundexception",
                    false);
            if (isSoundException && mAudioManager.getStreamVolume(AudioManager.STREAM_RING) == 0) {
                Log.d(TAG, "isSoundException = true, ringtone volume= 0");
                Log.d(TAG, "mAudioManager.isWiredHeadsetOn() : " + mAudioManager.isWiredHeadsetOn());
                Log.d(TAG, "mAudioManager.isBluetoothA2dpOn() : " + mAudioManager.isBluetoothA2dpOn());
                if (mAudioManager.isWiredHeadsetOn() || mAudioManager.isBluetoothA2dpOn()) {
                    ringtone.play();
                }
            } else {
                ringtone.play();
            }


            //[AUDIO_FWK]_END, 20120116, dk.han@lge.com }
        }
        //[S][Settings] [donghan07.lee] Add DRM policy 2012.02.14
        // ANDY_PORTING LGDRM [lg-drm@lge.com 100414]

        if (Drm.LGDRM) {
            mSelected = false;
        }

        // ANDY_END
        //[E][Settings] [donghan07.lee] Add DRM policy 2012.02.14

    }

    //[AUDIO_FWK]_END, 20120116, dk.han@lge.com }

    private void stopAnyPlayingRingtone() {
        mSampleRingtoneUri = null;
        if (mDefaultRingtone != null && mDefaultRingtone.isPlaying()) {
            mDefaultRingtone.stop();
        }

        if (mRingtoneManager != null) {
            mRingtoneManager.stopPreviousRingtone();
        }
    }

    // make option menu in action bar

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        if (!Utils.supportSplitView(getActivity())) {
            inflater.inflate(R.menu.ringtone_action_menu, menu);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        if (!Utils.supportSplitView(getActivity())) {
            MenuItem item = menu.findItem(R.id.item1);
            //hakgyu98.kim [B1 target, ringtone delete function activated]
            Cursor tempCursor;
            tempCursor = mRingtoneInfo.getCursor(mRingtoneInfo.EXTERNAL_CURSOR_TYPE);
            if (tempCursor != null) {
                if (tempCursor.getCount() > 0) {
                    item.setEnabled(true);
                    item.setVisible(true);
                } else {
                    item.setEnabled(false);
                    item.setVisible(false);
                }
            }
            tempCursor = null;
        }

        super.onPrepareOptionsMenu(menu);
    }

    // action bar menu selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        switch (item.getItemId()) {
        case android.R.id.home:
            int settingStyle = Settings.System.getInt(getActivity().getContentResolver(),
                    SETTING_STYLE, 0);
            ActivityManager am = (ActivityManager)getActivity().getSystemService(
                    Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> info = am.getRunningTasks(1);
            try {
                ComponentName topActivity = info.get(0).topActivity;
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            String baseActivityClassName = info.get(0).baseActivity.getClassName();

            if (settingStyle == 1 && Utils.supportEasySettings(getActivity())) {

                if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName) ||
                        "com.android.settings.Settings".equals(baseActivityClassName)) {
                    Log.d("soosin", "onBackPressed");
                    getActivity().onBackPressed();
                    return true;
                } else {
                    Log.d("soosin", "intent action - SOUND_SETTINGS");
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    i.setAction("com.android.settings.SOUND_SETTINGS");
                    startActivity(i);
                    getActivity().finish();
                    return true;
                }
            }
            else if ("com.android.settings.Settings".equals(baseActivityClassName)) {
                Log.d("soosin", "onBackPressed");
                getActivity().onBackPressed();
                return true;
            }
            else {
                Log.d("soosin", "intent action - SOUND_SETTINGS");
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.setAction("com.android.settings.SOUND_SETTINGS");
                startActivity(i);
                getActivity().finish();
                return true;
            }

        case R.id.item2:
            // go to mp3 list (music app)
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setData(Uri.parse("content://media/external/audio/media"));
            intent.putExtra(Drm.EXTRA_CONTENT_ACTION, Drm.CONTENT_ACT_RINGTONE);
            startActivityForResult(intent, LAUNCHED_REQUEST_CODE);
            break;
        case R.id.item1:
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.setClassName("com.android.settings",
                    "com.android.settings.RingtoneDeleteActivity");
            i.putExtra(RingtoneManagerEx.EXTRA_RINGTONE_TYPE, mParentType);
            startActivity(i);
            return true;
        default:
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    //[S][Settings] [donghan07.lee] Sorry popup error fixed when music app is disabled 2012.04.05

    private void confirmDialog() {
        new AlertDialog.Builder(getActivity())
                //.setIcon(android.R.drawable.ic_dialog_alert)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(getText(R.string.sp_dlg_note_NORMAL))
                .setPositiveButton(getText(R.string.dlg_ok),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {
                                final Intent intent = new Intent(
                                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:" + "com.lge.music"));
                                startActivity(intent);

                            }
                        })
                .setNegativeButton(getText(R.string.dlg_cancel),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {

                            }
                        }).setMessage(getText(R.string.sp_app_enable_confirm_message_NORMAL))
                .show();
    }

    //[E][Settings] [donghan07.lee] Sorry popup error fixed when music app is disabled 2012.04.05

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        if (requestCode == LAUNCHED_REQUEST_CODE) {
            if (resultCode != getActivity().RESULT_OK) {
                Log.d(TAG, "resultCode != RESULT_OK");
            } else {

                Log.d(TAG, "data.getExtras() = " + (Uri)data.getData());
                Uri uri = (Uri)data.getData();
                String _id[] = uri.toString().split("/");
                int id = Integer.parseInt(_id[_id.length - 1]);

                ContentResolver resolver = getActivity().getContentResolver();

                // Set the flag in the database to mark this as a ringtone
                Uri ringUri = uri;
                if (mParentType == RingtonePickerInfo.TYPE_RINGTONE ||
                        mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM2 ||
                        mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM3 ||
                        mParentType == RingtonePickerInfo.TYPE_RINGTONE_VC) {
                    try {
                        ContentValues values = new ContentValues(2);
                        values.put(MediaStore.Audio.Media.IS_RINGTONE, "1");
                        //values.put(MediaStore.Audio.Media.IS_ALARM, "1");
                        resolver.update(ringUri, values, null, null);
                    } catch (UnsupportedOperationException ex) {
                        // most likely the card just got unmounted
                        // Log.d("couldn't set ringtone flag for id " + id);
                        return;
                    }
                }
                else if (mParentType == RingtonePickerInfo.TYPE_NOTIFICATION ||
                        mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM2 ||
                        mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM3) {
                    try {
                        ContentValues values = new ContentValues(2);
                        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, "1");
                        //values.put(MediaStore.Audio.Media.IS_ALARM, "1");
                        resolver.update(ringUri, values, null, null);
                    } catch (UnsupportedOperationException ex) {
                        // most likely the card just got unmounted
                        // Log.d("couldn't set ringtone flag for id " + id);
                        return;
                    }
                }

                String[] cols = new String[] { MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.TITLE };

                String where = MediaStore.Audio.Media._ID + "=" + id;
                Cursor cursor = mRingtoneInfo.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        cols,
                        where, null, null);

                //for Phone Ringtone
                mRingtoneInfo.do_setURI(uri, mParentType);

                if (null != cursor) {
                    cursor.close();
                }

                //[S][Settings][donghan07.lee@lge.com] Change MP# ringtone setting sequence 2012.02.24.
                getActivity().onBackPressed();
                //initRingtoneList();
                //initRingtoneUI();
                //[E][Settings][donghan07.lee@lge.com] Change MP# ringtone setting sequence 2012.02.24.
            }
            //[E][2012.02.22][susin.park][common][TD:SE 137147] Title unset issue fix
            if (!Utils.supportSplitView(getActivity())) {
                do_InitTitleName();
            }

            //getActionBar().setTitle(R.string.ringtone_title);
            //[E][2012.02.22][susin.park][common][TD:SE 137147] Title unset issue fix
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    //[S][2012.03.08][donghan07.lee][common][common] Activity finish when SD card change event
    StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            if (null != getActivity()) {
                getActivity().finish();
            }
        }
    };

    //[E][2012.03.08][donghan07.lee][common][common] Activity finish when SD card change event
    /* not used
    private void showDeleteDialog() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Delete?")
                .setPositiveButton("OK KLP",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface _dialog, int which) {
                                ContentResolver resolver = getActivity().getContentResolver();
                                RingtoneEntryItem item = (RingtoneEntryItem)items.get(mRemovePos);
                                Uri ringUri = Uri.parse(item.uri);
                                if (mParentType == RingtonePickerInfo.TYPE_RINGTONE ||
                                        mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM2 ||
                                        mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM3 ||
                                        mParentType == RingtonePickerInfo.TYPE_RINGTONE_VC) {
                                    try {
                                        ContentValues values = new ContentValues(2);
                                        values.put(MediaStore.Audio.Media.IS_RINGTONE, "0");
                                        //values.put(MediaStore.Audio.Media.IS_ALARM, "1");
                                        resolver.update(ringUri, values, null, null);
                                    } catch (UnsupportedOperationException ex) {
                                        // most likely the card just got unmounted
                                        // Log.d("couldn't set ringtone flag for id " + id);
                                        return;
                                    }
                                }
                                else if (mParentType == RingtonePickerInfo.TYPE_NOTIFICATION ||
                                        mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM2 ||
                                        mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM3) {
                                    try {
                                        ContentValues values = new ContentValues(2);
                                        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, "0");
                                        //values.put(MediaStore.Audio.Media.IS_ALARM, "1");
                                        resolver.update(ringUri, values, null, null);
                                    } catch (UnsupportedOperationException ex) {
                                        // most likely the card just got unmounted
                                        // Log.d("couldn't set ringtone flag for id " + id);
                                        return;
                                    }
                                }
                                stopAnyPlayingRingtone();
                                initRingtoneList();
                                initRingtoneUI();

                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    */

    private void do_InitTitleName() {
        String title_res = mContext.getResources().getString(R.string.ringtone_title);
        int icon_res = R.drawable.shortcut_phone_ringtone;
        if (mParentType == RingtonePickerInfo.TYPE_RINGTONE) {
            if (isDualSim || isTripleSim) {
                if (Utils.isUI_4_1_model(mContext)) {
                    title_res = Utils.do_getSoundSimName(mContext,
                            RENAME_SIM_TYPE_RINGTONE,
                            RENAME_SIM1_INDEX);
                } else {
                    title_res = mContext.getResources().getString(
                            R.string.sp_sub_sim1_ringtone_title_NORMAL);
                }
            } else {
                if (Utils.isUI_4_1_model(mContext)) {
                    title_res = mContext.getResources().getString(
                            R.string.ringtone_title_ex);
                }
            }
        }
        if (mParentType == RingtonePickerInfo.TYPE_NOTIFICATION) {
            icon_res = R.drawable.shortcut_notification_sound;
            if (isDualSim || isTripleSim) {
                if (Utils.isUI_4_1_model(mContext)) {
                    title_res = Utils.do_getSoundSimName(mContext,
                            RENAME_SIM_TYPE_NOTIFICATION,
                            RENAME_SIM1_INDEX);
                } else {
                    title_res = mContext.getResources().getString(
                            R.string.sp_sub_sim1_notification_sound_title_NORMAL);
                }
            } else {
                if ("ATT".equals(Config.getOperator())) {
                    title_res = mContext.getResources().getString(
                            R.string.default_notification_sound);
                } else {
                    title_res = mContext.getResources().getString(
                            R.string.sp_sound_noti_NORMAL);
                }
            }
        }
        if (mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM2) {
            icon_res = R.drawable.shortcut_phone_ringtone;
            if (Utils.isUI_4_1_model(mContext)) {
                title_res = Utils.do_getSoundSimName(mContext,
                        RENAME_SIM_TYPE_RINGTONE,
                        RENAME_SIM2_INDEX);
            } else {
                title_res = mContext.getResources().getString(
                        R.string.sp_sub_sim2_ringtone_title_NORMAL);
            }
        }
        if (mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM2) {
            icon_res = R.drawable.shortcut_notification_sound;
            if (Utils.isUI_4_1_model(mContext)) {
                title_res = Utils.do_getSoundSimName(mContext,
                        RENAME_SIM_TYPE_NOTIFICATION,
                        RENAME_SIM2_INDEX);
            } else {
                title_res = mContext.getResources().getString(
                        R.string.sp_sub_sim2_notification_sound_title_NORMAL);
            }
        }
        if (mParentType == RingtonePickerInfo.TYPE_RINGTONE_SIM3) {
            icon_res = R.drawable.shortcut_phone_ringtone;
            if (Utils.isUI_4_1_model(mContext)) {
                title_res = Utils.do_getSoundSimName(mContext,
                        RENAME_SIM_TYPE_RINGTONE,
                        RENAME_SIM3_INDEX);
            } else {
                title_res = mContext.getResources().getString(
                        R.string.sp_sub_sim3_ringtone_title_NORMAL);
            }

        }
        if (mParentType == RingtonePickerInfo.TYPE_NOTIFICATION_SIM3) {
            icon_res = R.drawable.shortcut_notification_sound;
            if (Utils.isUI_4_1_model(mContext)) {
                title_res = Utils.do_getSoundSimName(mContext,
                        RENAME_SIM_TYPE_NOTIFICATION,
                        RENAME_SIM3_INDEX);
            } else {
                title_res = mContext.getResources().getString(
                        R.string.sp_sub_sim3_notification_sound_title_NORMAL);
            }
        }
        if (mParentType == RingtonePickerInfo.TYPE_RINGTONE_VC) {
            icon_res = R.drawable.shortcut_phone_ringtone;
            title_res = mContext.getResources().getString(
                    R.string.sp_vc_ringtone_title_NORMAL);
        }
        getActivity().getActionBar().setIcon(icon_res);
        getActivity().getActionBar().setTitle(title_res);
    }

    private void do_InitSIMDependancyMenu() {
        Intent intent = getActivity().getIntent();

        isDualSim = Utils.isMultiSimEnabled();
        isTripleSim = Utils.isTripleSimEnabled();
        if (!Utils.supportSplitView(getActivity())) {
            do_InitTitleName();
        }

        if ("ATT".equals(Config.getOperator()) &&
                ((mParentType == mRingtoneInfo.TYPE_NOTIFICATION) ||
                        (mParentType == mRingtoneInfo.TYPE_NOTIFICATION_SIM2) ||
                (mParentType == mRingtoneInfo.TYPE_NOTIFICATION_SIM3))) {
            mHasSilentItem = intent.getBooleanExtra(
                    RingtoneManagerEx.EXTRA_RINGTONE_SHOW_SILENT, true);

        } else {
            mHasSilentItem = false;
        }

        if (mParentType != -1) {
            // [S][Settings] [donghan07.lee] The volume keys will control the stream that we are choosing a ringtone for
            mRingtoneManager.setType(mParentType);
            mRingtoneInfo.setFilterColumnsList();

        }
        getActivity().setVolumeControlStream(mRingtoneManager.inferStreamType());
    }

    private void do_InitCancelOkBtn(View view) {
        mCancelButton = (Button)view.findViewById(R.id.cancel_button);
        mOKButton = (Button)view.findViewById(R.id.ok_button);
        //LGE_S. Docomo. nexti  dongkyu31.lee. for checking Docomo PlayReady DRM ringtone flag.
        if (mOperator.equals(strTargetOperator)) {
            //if(mOperator.equals(SystemProperties.get("ro.build.target_operator"))){
            mOKButton.setEnabled(false);
        }
        //LGE_E. Docomo. nexti. dongkyu31.lee

        mCancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                // finish
                getActivity().onBackPressed();
            }
        });

        mOKButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (items == null
                        || items.get(mClickedPos) instanceof RingtoneSectionItem) {
                    return;
                }
                RingtoneEntryItem item = (RingtoneEntryItem)items.get(mClickedPos);
                if (item.uri != null) {
                    position_uri = Uri.parse(item.uri);
                } else {
                    position_uri = null;
                }

                Uri uri = position_uri;

                //[S][Settings] [donghan07.lee] Add DRM policy 2012.02.14
                // ANDY_PORTING LGDRM [lg-drm@lge.com 100414]

                if (Drm.LGDRM && mDrmRingtone == false) {
                    // TODO: Apply translated string, not hard coded english
                    Toast.makeText(getActivity(),
                            //"Protected file. Cannot be set as ringtone",
                            getString(R.string.sp_toast_protected_ringtone_set_NORMAL),
                            Toast.LENGTH_LONG).show();
                    getActivity().setResult(getActivity().RESULT_CANCELED);
                } else {
                    // ANDY_END

                    if (mClickedPos == mDefaultRingtonePos) {
                        // Set it to the default Uri that they originally gave
                        // us
                        uri = mUriForDefaultItem;
                    } else if (mClickedPos == mSilentPos) {
                        // A null Uri is for the 'Silent' item
                        uri = null;
                    } else {
                        uri = position_uri;
                        // ANDY_PORTING LGDRM [lg-drm@lge.com 100414]
                        String path = null;
                        if (uri != null) {
                            path = mRingtoneInfo.GetFilePath(uri.toString());
                        }

                        if (path != null) {
                            Log.v("RingToneTest", "[onClick] uri : " + path);
                            int nStatus = mRingtoneInfo.checkDRM(path);
                            if (nStatus == 1) {
                                mDrmRingtone = false;
                                Toast.makeText(
                                        getActivity(),
                                        getString(R.string.sp_toast_protected_ringtone_set_NORMAL),
                                        Toast.LENGTH_LONG).show();
                                uri = mUriForDefaultItem;
                            }
                        }
                        // ANDY_END
                    }

                    /*
                     * resultIntent.putExtra(RingtoneManager.
                     * EXTRA_RINGTONE_PICKED_URI, uri); setResult(RESULT_OK,
                     * resultIntent);
                     */

                    if (uri != null) {
                        //[S][Settings][donghan07.lee@lge.com]Ringtone picker UI issue 2012.02.27
                        if (uri.equals(Uri.parse("content://settings/system/ringtone"))) {
                            if (internal_ringtone_position == -1
                                    && external_ringtone_position == -1) {
                                uri = mRingtoneInfo.getDefaultPhoneRingtone();
                            }
                            else {
                                uri = mRingtoneInfo.getURI();
                            }
                        }
                    }
                    //[E][Settings][donghan07.lee@lge.com]Ringtone picker UI issue 2012.02.27
                    //for Phone Ringtone
                    mRingtoneInfo.do_setURI(uri, mParentType);
                    // finish
                    getActivity().onBackPressed();
                }
            }
            //[E][Settings] [donghan07.lee] Add DRM policy 2012.02.14
        });
    }

    private void do_InitDefalutSetting() {
        ActionBar actionBar = getActivity().getActionBar();

        if (!Utils.supportSplitView(getActivity())) {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                        ActionBar.DISPLAY_SHOW_CUSTOM);
            }
        }

        mHandler = new Handler();
        Intent intent = getActivity().getIntent();
        mAudioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (mStorageManager == null) {
            mStorageManager = (StorageManager)getActivity().getSystemService(
                    Context.STORAGE_SERVICE);
            mStorageManager.registerListener(mStorageListener);
        }
        boolean includeDrm = intent.getBooleanExtra(RingtoneManagerEx.EXTRA_RINGTONE_INCLUDE_DRM,
                true);
        mRingtoneInfo.setIncludeDrm(includeDrm);

        mUriForDefaultItem = intent
                .getParcelableExtra(RingtoneManagerEx.EXTRA_RINGTONE_DEFAULT_URI);
        if (mUriForDefaultItem == null) {
            mUriForDefaultItem = Settings.System.DEFAULT_RINGTONE_URI;
        }
        mRingtoneManager = new RingtoneManagerEx(getActivity());

        mExistingUri = intent
                .getParcelableExtra(RingtoneManagerEx.EXTRA_RINGTONE_EXISTING_URI);
        Log.d(TAG, "mExistingUri = " + mExistingUri);
    }
}
