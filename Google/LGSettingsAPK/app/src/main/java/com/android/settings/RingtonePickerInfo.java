/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
//import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.content.ContentValues;
import android.content.ContentResolver;
import android.os.Environment;

import com.lge.media.RingtoneManagerEx;

import android.provider.MediaStore;

//CAPP_DRM [lg-drm@lge.com 100414]
//import com.lge.config.ConfigBuildFlags;
import com.lge.lgdrm.Drm;
//CAPP_DRM_END

//[S][Settings] [donghan07.lee] Add DRM Cursor for DRM files
import com.android.internal.database.SortCursor;
// KLP import android.provider.DrmStore;
//[E][Settings] [donghan07.lee] Add DRM Cursor for DRM files

import android.os.SystemProperties;

//[S][Settings] [donghan07.lee] Add DRM policy 2012.02.14
import com.lge.lgdrm.DrmManager;
import com.lge.lgdrm.DrmContentSession;
import android.media.AudioManager;
import android.widget.Toast;
//[E][Settings] [donghan07.lee] Add DRM policy 2012.02.14

//[S][Settings] [donghan07.lee] Sorry popup error fixed when music app is disabled 2012.04.05
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
//[E][Settings] [donghan07.lee] Sorry popup error fixed when music app is disabled 2012.04.05

//[S]expired DRM check
import com.lge.lgdrm.DrmContent;
import com.lge.lgdrm.DrmException;
//[E]expired DRM check
//CAPP_DRM [lg-drm@lge.com 100624]
import com.lge.config.ConfigBuildFlags;
import com.lge.lgdrm.DrmFwExt;
//CAPP_DRM_END

//LGE_S nexti. dongkyu31.lee
import android.drm.DrmManagerClient;
import com.android.settings.lgesetting.Config.Config;

import com.android.settings.soundprofile.SoundProfileInfo;
import android.os.Build;

/**
 * This service is used to handle calendar event reminders.
 */
public class RingtonePickerInfo {
    private static final String TAG = "RingtonePickerInfo";

    private static boolean sIsSatotakuRingtoneModel = false;
    private static boolean sIsSilentItemAddModel = Build.DEVICE.equals("geefhd");

    public static final String RINGTONE_SIM2 = "ringtone_sim2";
    public static final String NOTIFICATION_SOUND_SIM2 = "notification_sound_sim2";
    public static final String RINGTONE_SIM3 = "ringtone_sim3";
    public static final String NOTIFICATION_SOUND_SIM3 = "notification_sound_sim3";
    public static final String RINGTONE_VIDEOCALL = "ringtone_videocall";

    private static String[][] sParentTypeDB = { { "1", Settings.System.RINGTONE },
            { "2", Settings.System.NOTIFICATION_SOUND },
            { "4", Settings.System.ALARM_ALERT },
            { "8", RINGTONE_SIM2 },
            { "16", NOTIFICATION_SOUND_SIM2 },
            { "32", RINGTONE_VIDEOCALL },
            { "128", RINGTONE_SIM3 },
            { "256", NOTIFICATION_SOUND_SIM3 } };

    /**
     * The column index (in the cursor returned by {@link #getCursor()} for the
     * media provider's URI.
     */
    public static final int URI_COLUMN_INDEX = 2;
    /**
     * The column index (in the cursor returned by {@link #getCursor()} for the
     * row ID.
     */
    public static final int ID_COLUMN_INDEX = 0;

    public static final int TYPE_RINGTONE = 1;
    public static final int TYPE_NOTIFICATION = 2;
    public static final int TYPE_ALARM = 4;
    public static final int TYPE_RINGTONE_SIM2 = 8;
    public static final int TYPE_NOTIFICATION_SIM2 = 16;
    public static final int TYPE_RINGTONE_VC = 32;
    public static final int TYPE_RINGTONE_SIM3 = 128;
    public static final int TYPE_NOTIFICATION_SIM3 = 256;

    final static int INTERNAL_CURSOR_TYPE = 1;
    final static int EXTERNAL_CURSOR_TYPE = 2;
    final static int SATOTAKU_CURSOR_TYPE = 3;
    final static int ADDSILENT_CURSOR_TYPE = 4;
    final static int DISNEY_CURSOR_TYPE = 5;
    final static int LG_CURSOR_TYPE = 6;

    private boolean mIncludeDrm = false;

    private static Context mContext = null;
    //private ContentResolver mContentResolver;

    private List<String> mFilterColumns = new ArrayList<String>();

    private static int sParentType = TYPE_RINGTONE;

    private static final String[] INTERNAL_COLUMNS = new String[] {
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            "\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\"",
            MediaStore.Audio.Media.TITLE_KEY };

    private static final String[] MEDIA_COLUMNS = new String[] {
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            "\"" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "\"",
            MediaStore.Audio.Media.TITLE_KEY };

    // [S][Settings] [donghan07.lee] Add DRM Cursor for DRM files
    private static final String[] DRM_COLUMNS = new String[] { null /*
                                                                    DrmStore.Audio._ID, DrmStore.Audio.TITLE,
                                                                    "\"" + DrmStore.Audio.CONTENT_URI + "\"",
                                                                    DrmStore.Audio.TITLE + " AS " + MediaStore.Audio.Media.TITLE_KEY  KLP */};

    public RingtonePickerInfo(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
        //mContentResolver = mContext.getContentResolver();
    }

    public static void setParentType(int value) {
        sParentType = value;
    }

    public int getParentType() {
        return sParentType;
    }

    public int getRingtoneUriPosition(Uri ringtoneUri, Cursor c) {
        if (ringtoneUri == null) {
            return -1;
        }
        // [S][Settings][donghan07.lee@lge.com]WBT for nullpointerException
        // 2012.02.10
        if (c == null) {
            return -1;
        }
        final Cursor cursor = c;
        final int cursorCount = cursor.getCount();

        if (!cursor.moveToFirst()) {
            return -1;
        }

        // Only create Uri objects when the actual URI changes
        Uri currentUri = null;
        String previousUriString = null;
        for (int i = 0; i < cursorCount; i++) {
            String uriString = cursor.getString(URI_COLUMN_INDEX);
            if (currentUri == null || !uriString.equals(previousUriString)) {
                currentUri = Uri.parse(uriString);
            }

            if (ringtoneUri.equals(ContentUris.withAppendedId(currentUri,
                    cursor.getLong(ID_COLUMN_INDEX)))) {
                return i;
            }

            cursor.move(1);

            previousUriString = uriString;
        }

        return -1;
    }

    public static Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder,
            int limit) {
        try {
            ContentResolver resolver = mContext.getContentResolver();
            if (resolver == null) {
                return null;
            }
            if (limit > 0) {
                uri = uri.buildUpon().appendQueryParameter("limit", "" + limit)
                        .build();
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (UnsupportedOperationException ex) {
            return null;
        }

    }

    public static Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        return query(uri, projection, selection, selectionArgs,
                sortOrder, 0);
    }

    public Cursor getCursor(int type) {
        switch (type) {
        case INTERNAL_CURSOR_TYPE:
            if (sParentType == TYPE_RINGTONE || sParentType == TYPE_RINGTONE_SIM2
                    || sParentType == TYPE_RINGTONE_SIM3 || sParentType == TYPE_RINGTONE_VC) {
                return query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, INTERNAL_COLUMNS,
                        constructBooleanTrueWhereClause(mFilterColumns, mIncludeDrm),
                        null, MediaStore.Audio.Media.DISPLAY_NAME);
            } else {
                return query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, INTERNAL_COLUMNS,
                        constructBooleanTrueWhereClause(mFilterColumns, mIncludeDrm),
                        null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

            }
            //break;
        case EXTERNAL_CURSOR_TYPE: {
            final Cursor drmCursor = null; //KLP mIncludeDrm ? getDrmRingtones() : null;
            final Cursor mediaCursor = getMediaRingtones();
            return (new SortCursor(
                    new Cursor[] { drmCursor, mediaCursor },
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER));
        }
        //break;
        case SATOTAKU_CURSOR_TYPE:
            return query(
                    MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                    INTERNAL_COLUMNS,
                    constructBooleanTrueWhereClauseForSatotakuRingtone(mFilterColumns, mIncludeDrm),
                    null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        case ADDSILENT_CURSOR_TYPE:
            return query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, INTERNAL_COLUMNS,
                    constructBooleanTrueWhereClauseForSilentItem(mFilterColumns, mIncludeDrm),
                    null, null);

        case DISNEY_CURSOR_TYPE:
            return query(
                    MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                    INTERNAL_COLUMNS,
                    getDisneySelection(mFilterColumns, mIncludeDrm, DISNEY_CURSOR_TYPE),
                    null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        case LG_CURSOR_TYPE:
            return query(
                    MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                    INTERNAL_COLUMNS,
                    getDisneySelection(mFilterColumns, mIncludeDrm, LG_CURSOR_TYPE),
                    null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        default:
            break;
        }
        return null;
    }

    public void setFilterColumnsList() {
        List<String> columns = mFilterColumns;
        columns.clear();

        Log.d(TAG, "##########################################" + sParentType);
        if (((sParentType & TYPE_RINGTONE) != 0) || ((sParentType & TYPE_RINGTONE_SIM2) != 0)
                || ((sParentType & TYPE_RINGTONE_SIM3) != 0)
                || ((sParentType & TYPE_RINGTONE_VC) != 0)) {
            Log.d(TAG, "##################columns.add(MediaStore.Audio.AudioColumns.IS_RINGTONE)");
            columns.add(MediaStore.Audio.AudioColumns.IS_RINGTONE);
        }

        if (((sParentType & TYPE_NOTIFICATION) != 0)
                || ((sParentType & TYPE_NOTIFICATION_SIM2) != 0)
                || ((sParentType & TYPE_NOTIFICATION_SIM3) != 0)) {
            Log.d(TAG,
                    "##################columns.add(MediaStore.Audio.AudioColumns.IS_NOTIFICATION)");
            columns.add(MediaStore.Audio.AudioColumns.IS_NOTIFICATION);
        }

        if ((sParentType & TYPE_ALARM) != 0) {
            columns.add(MediaStore.Audio.AudioColumns.IS_ALARM);
        }
    }

    private static String constructBooleanTrueWhereClause(List<String> columns,
            boolean includeDrm) {

        if (columns == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");

        for (int i = columns.size() - 1; i >= 0; i--) {
            if (sIsSatotakuRingtoneModel) {
                sb.append("(");
                sb.append(columns.get(i)).append(
                        "=1 and not " + MediaStore.Audio.Media.ARTIST + " like 'Satoh-Taku') or ");
            } else if (sIsSilentItemAddModel) {
                sb.append("(");
                sb.append(columns.get(i)).append(
                        "=1 and not " + MediaStore.Audio.Media.DATA
                                + "= '/system/media/audio/notifications/Silent.ogg') or ");
            } else {
                sb.append(columns.get(i)).append("=1 or ");
            }
        }

        if (columns.size() > 0) {
            // Remove last ' or '
            sb.setLength(sb.length() - 4);
        }

        sb.append(")");

        if (!includeDrm) {
            // If not DRM files should be shown, the where clause
            // will be something like "(is_notification=1) and is_drm=0"
            sb.append(" and ");
            sb.append(MediaStore.MediaColumns.IS_DRM);
            sb.append("=0");
        }
        //[nishant11.kumar@lge.com_S] TMO ringtone customizations TD#160139
        String mOperator = Config.getOperator();
        String mCountry = Config.getCountry();
        if (mOperator != null && mCountry != null && mOperator.equalsIgnoreCase("TMO")
                && mCountry.equalsIgnoreCase("COM")) {
            String ntCodeMCC = SystemProperties.get("persist.sys.mcc-list", "F");
            if (ntCodeMCC != null && (
                    (ntCodeMCC.contains("262") == true)
                            || (ntCodeMCC.contains("216") == true)
                            || (ntCodeMCC.contains("231") == true)
                            || (ntCodeMCC.contains("232") == true)
                            || (ntCodeMCC.contains("219") == true))) {
                sb.append(" and "
                        + MediaStore.Audio.Media.TITLE
                        + " not in ( 'Cosmote_Backringtone', 't-mobile_receive_message', 't-mobile_ring')");
            } else if (ntCodeMCC != null && (
                    (ntCodeMCC.contains("230") == true)
                    || (ntCodeMCC.contains("204") == true)
                    || (ntCodeMCC.contains("294") == true))) {
                sb.append(" and "
                        + MediaStore.Audio.Media.TITLE
                        + " not in ( 'Cosmote_Backringtone', 'telekom_receive_message', 'telekom_ring')");
            } else if (ntCodeMCC != null && (ntCodeMCC.contains("202") == true)) {
                sb.append(" and "
                        + MediaStore.Audio.Media.TITLE
                        + " not in ( 't-mobile_ring', 'telekom_receive_message', 't-mobile_receive_message', 'telekom_ring')");
            } else {
                sb.append(" and "
                        + MediaStore.Audio.Media.TITLE
                        + " not in ( 't-mobile_ring', 'telekom_receive_message', 't-mobile_receive_message', 'telekom_ring', 'Cosmote_Backringtone')");
            }
        }
        //[nishant11.kumar@lge.com_S] TMO ringtone customizations TD#160139
        return sb.toString();
    }

    private static String constructBooleanTrueWhereClauseForSatotakuRingtone(List<String> columns,
            boolean includeDrm) {

        if (columns == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(");

        for (int i = columns.size() - 1; i >= 0; i--) {
            if (sIsSatotakuRingtoneModel) {
                sb.append("(");
                sb.append(columns.get(i)).append(
                        "=1 and " + MediaStore.Audio.Media.ARTIST + "= 'Satoh-Taku') or ");
            } else {
                sb.append(columns.get(i)).append("=1 or ");
            }
        }

        if (columns.size() > 0) {
            // Remove last ' or '
            sb.setLength(sb.length() - 4);
        }

        sb.append(")");

        if (!includeDrm) {
            // If not DRM files should be shown, the where clause
            // will be something like "(is_notification=1) and is_drm=0"
            sb.append(" and ");
            sb.append(MediaStore.MediaColumns.IS_DRM);
            sb.append("=0");
        }

        return sb.toString();
    }

    private static String constructBooleanTrueWhereClauseForSilentItem(List<String> columns,
            boolean includeDrm) {

        if (columns == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(");

        for (int i = columns.size() - 1; i >= 0; i--) {
            sb.append("(");
            sb.append(columns.get(i)).append(
                    "=1 and " + MediaStore.Audio.Media.DATA
                            + "= '/system/media/audio/notifications/Silent.ogg') or ");
        }

        if (columns.size() > 0) {
            // Remove last ' or '
            sb.setLength(sb.length() - 4);
        }

        sb.append(")");

        if (!includeDrm) {
            // If not DRM files should be shown, the where clause
            // will be something like "(is_notification=1) and is_drm=0"
            sb.append(" and ");
            sb.append(MediaStore.MediaColumns.IS_DRM);
            sb.append("=0");
        }

        return sb.toString();
    }

    /**
     * Returns whether DRM ringtones will be included.
     *
     * @return Whether DRM ringtones will be included.
     * @see #setIncludeDrm(boolean)
     */
    public boolean getIncludeDrm() {
        return mIncludeDrm;
    }

    /**
     * Sets whether to include DRM ringtones.
     *
     * @param includeDrm
     *            Whether to include DRM ringtones.
     */
    public void setIncludeDrm(boolean includeDrm) {
        mIncludeDrm = includeDrm;
    }

    // [S][Settings] [donghan07.lee] Add DRM Cursor for DRM files
    private Cursor getDrmRingtones() {
        // DRM store does not have any columns to use for filtering
        return query(null /* DrmStore.Audio.CONTENT_URI KLP */,
                DRM_COLUMNS, null, null, null/* KLP DrmStore.Audio.TITLE */);
    }

    private Cursor getMediaRingtones() {
        // Get the external media cursor. First check to see if it is mounted.
        final String status = Environment.getExternalStorageState();

        return (status.equals(Environment.MEDIA_MOUNTED) || status
                .equals(Environment.MEDIA_MOUNTED_READ_ONLY)) ? query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MEDIA_COLUMNS,
                constructBooleanTrueWhereClause(mFilterColumns, mIncludeDrm),
                null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER) : null;
    }

    // [E][Settings] [donghan07.lee] Add DRM Cursor for DRM files

    public boolean appIsEnabled() {

        PackageManager pm = mContext.getPackageManager();
        ApplicationInfo info = null;

        try {
            info = pm.getApplicationInfo("com.lge.music", PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            return false;
        }
        return info.enabled;
    }

    public Uri getURI() {
        //RingtoneManager mRingtoneManager = new RingtoneManager(this);
        Log.d(TAG,
                "getActualDefaultRingtoneUri = "
                        + RingtoneManagerEx.getActualDefaultRingtoneUri(mContext,
                                sParentType));
        Uri uri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext,
                sParentType);

        return uri;
    }

    public static String do_getURI(int parentType) {
        String db_Uri = null;
        for (int i = 0; i < sParentTypeDB.length; i++) {
            if (parentType == Integer.parseInt(sParentTypeDB[i][0])) {
                db_Uri = Settings.System.getString(
                        mContext.getContentResolver(),
                        sParentTypeDB[i][1]);
                return db_Uri;
            }
        }
        return db_Uri;
    }

    private static boolean sIsUserSoundProfile = false;

    public void do_setUserSoundProfile(boolean value) {
        sIsUserSoundProfile = value;
    }

    public static void do_setURI(Uri uri, int parentType) {
        SoundProfileInfo soundProfileInfo = new SoundProfileInfo(mContext);
        Log.d(TAG, "sIsUserSoundProfile : " + sIsUserSoundProfile);
        if (sIsUserSoundProfile && uri != null) {
            soundProfileInfo.setSoundProfileEachData(
                    soundProfileInfo.getRingtoneIndexFromType(sParentType),
                    uri.toString());
        } else {
            for (int i = 0; i < sParentTypeDB.length; i++) {
                if (parentType == Integer.parseInt(sParentTypeDB[i][0])) {
                    Settings.System.putString(
                            mContext.getContentResolver(),
                            sParentTypeDB[i][1],
                            uri != null ? uri.toString() : null);
                }
            }
            if (soundProfileInfo.getUserProfileName().equals("") && uri != null) {
                soundProfileInfo.setSoundDefaultData(
                        soundProfileInfo.getRingtoneIndexFromType(sParentType),
                        uri.toString());
            }
        }
    }

    public boolean isRingtoneType() {
        if (sParentType == TYPE_RINGTONE
                || sParentType == TYPE_RINGTONE_SIM2
                || sParentType == TYPE_RINGTONE_SIM3
                || sParentType == TYPE_RINGTONE_VC) {
            return true;
        } else {
            return false;
        }
    }

    public static Uri getDefaultPhoneRingtone() {
        String defaultRingtoneName = "";
        // LGE_CHANGE_S : 120615, MFW, byungju.ko, There are 2 more same sounds in internal DB like US_Cellular.ogg. Because this case, enforce query conditions(soundtype).
        String soundtype = "";
        // [S][Settings][donghan07.lee] Add default rigtone URI according to stream type
        //defaultRingtoneName = SystemProperties.get("ro.config.ringtone");

        if (sParentType == TYPE_RINGTONE || sParentType == TYPE_RINGTONE_SIM2
                || sParentType == TYPE_RINGTONE_SIM3 || sParentType == TYPE_RINGTONE_VC) {
            defaultRingtoneName = SystemProperties.get("ro.config.ringtone");
            soundtype = MediaStore.Audio.AudioColumns.IS_RINGTONE;
        }
        //for Notification ringtone
        else if (sParentType == TYPE_NOTIFICATION || sParentType == TYPE_NOTIFICATION_SIM2
                || sParentType == TYPE_NOTIFICATION_SIM3) {
            defaultRingtoneName = SystemProperties.get("ro.config.notification_sound");
            soundtype = MediaStore.Audio.AudioColumns.IS_NOTIFICATION;
        }
        //for Alarm ringtone
        else if (sParentType == TYPE_ALARM) {
            //to do

        }
        // [E][Settings][donghan07.lee] Add default rigtone URI according to stream type

        Log.e(TAG, "Default Ringtone Name(stream):" + defaultRingtoneName);

        // get uri of defaultRingtone
        Cursor cursor = query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, INTERNAL_COLUMNS,
                MediaStore.Audio.Media.DISPLAY_NAME + " = " + "\""
                        + defaultRingtoneName + "\""
                        + " AND " + soundtype + " =  " + "\""
                        + 1 + "\""
                , null, null);
        // LGE_CHANGE_E : 120615, MFW, byungju.ko
        Uri uri = getValidRingtoneUriFromCursorAndClose(cursor);

        if (null != cursor) {
            cursor.close();
        }
        return uri;
    }

    private static Uri getValidRingtoneUriFromCursorAndClose(Cursor cursor) {
        if (cursor != null) {
            Uri uri = null;

            if (cursor.moveToFirst()) {
                uri = getUriFromCursor(cursor);
            }
            cursor.close();

            return uri;
        } else {
            return null;
        }
    }

    public static String getValidRingtoneUriStringFromCursor(Cursor cursor) {
        if (cursor != null) {
            Uri uri = null;

            if (cursor.moveToFirst()) {
                uri = getUriFromCursor(cursor);
            }
            //cursor.close();
            if (uri == null) {
                return null;
            } else {
                return uri.toString();
            }
        } else {
            return null;
        }
    }

    private static Uri getUriFromCursor(Cursor cursor) {
        return ContentUris.withAppendedId(
                Uri.parse(cursor.getString(URI_COLUMN_INDEX)),
                cursor.getLong(ID_COLUMN_INDEX));
    }

    public String getDefaultPath() {
        int mStreamType = AudioManager.STREAM_MUSIC;
        RingtoneManagerEx mRingtoneManager = new RingtoneManagerEx(mContext);
        mStreamType = mRingtoneManager.inferStreamType();

        switch (mStreamType) {
        case AudioManager.STREAM_RING:
            // Log.e(TAG, "default path is ring!!");
            return SystemProperties.get("ro.config.ringtone");
        case AudioManager.STREAM_NOTIFICATION:
            // Log.e(TAG, "default path is noti!!");
            return SystemProperties.get("ro.config.notification_sound");
        case AudioManager.STREAM_ALARM:
            // Log.e(TAG, "default path is alarm!!");
            return SystemProperties.get("ro.config.alarm_alert");
        default:
            // Log.e(TAG, "default path is noti!!");
            return SystemProperties.get("ro.config.ringtone");
        }

    }

    public boolean isExpiredDrm() {
        String db_Uri = null;
        db_Uri = do_getURI(sParentType);
        Log.d(TAG, "!!!!!db_uri = " + db_Uri);
        //position_uri.toString().equals(db_Ur   i);
        if (db_Uri != null) {
            String path = GetFilePath(db_Uri.toString());
            if (path != null) {
                int isDrm = DrmManager.isDRM(path);
                if (isDrm > 0) {
                    int isDrmExpired = CheckDrmExpired(path);
                    Log.d(TAG, "!!!!!isDrm = " + isDrm + ", isDrmExpired = " + isDrmExpired);
                    if (isDrm > 0 && isDrmExpired == Drm.RIGHT_STATE_INVALID) {
                        Log.d(TAG, "!!!!! return true");
                        return true;
                    }
                }
            }
        }
        Log.d(TAG, "!!!!! return false");
        return false;
    }

    private int CheckDrmExpired(String filePath) {
        int drmContentType = -1;
        int drmJudge = -1;
        try {
            DrmContentSession drmContentSession = DrmManager.createContentSession(filePath,
                    mContext);

            if (drmContentSession == null) {
                // GpmLog.i(TAG, "#################createContentSession() : Fail");
                return -1;
            }

            DrmContent drmContent = drmContentSession.getSelectedContent(true);
            if (drmContent == null) {
                // GpmLog.i(TAG, "getSelectedContent(true) : Fail");
                return -1;
            }

            drmContentType = drmContent.getContentType(); // original content type. image/audio/video
            if (drmContentType == Drm.MEDIA_TYPE_IMAGE) {
                // display
                drmJudge = drmContentSession.judgeRight(Drm.PERMISSION_DISPLAY, false);
            }
            else if (drmContentType == Drm.MEDIA_TYPE_AUDIO
                    || drmContentType == Drm.MEDIA_TYPE_VIDEO) {
                // play
                drmJudge = drmContentSession.judgeRight(Drm.PERMISSION_PLAY, false);
            }
            else {
                return -1;
            }
            return drmJudge;
        } catch (SecurityException e) {
            e.printStackTrace();
            return -1;
        } catch (DrmException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String GetFilePath(String path) {
        String data = MediaStore.Audio.Media.DATA;
        Uri uri = Uri.parse(path);
        String scheme = uri.getScheme();
        String filename = null;

        if (scheme == null || scheme.equals("file")) {
            filename = uri.getPath();
        } else if (scheme != null && scheme.equals("content")) {

            if (path.startsWith("content://settings")) {
                String fileuri = getFilepathFromUri(uri, "value");
                uri = Uri.parse(fileuri);
                scheme = uri.getScheme();

                if (scheme == null || scheme.equals("file")) {
                    filename = uri.getPath();
                } else if (scheme != null && scheme.equals("content")) {
                    filename = getFilepathFromUri(uri, data);
                }
            } else {
                filename = getFilepathFromUri(uri, data);
            }
        }
        Log.v(TAG, "[MultiPlayer] setDataSource : filename = "
                + filename);
        return filename;
    }

    private String getFilepathFromUri(Uri uri, String column) {
        String filepath = null;
        Cursor c = null;

        try {
            ContentResolver resolver = mContext.getContentResolver();
            c = resolver.query(uri, new String[] { column }, null, null, null);

            int count = (c != null) ? c.getCount() : 0;
            if (count != 1) {

                if (count == 0) {
                    return null;
                }
            }

            c.moveToFirst();
            int i = c.getColumnIndex(column);
            filepath = (i >= 0 ? c.getString(i) : null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
        }
        if (filepath == null) {
            return null;
        }

        Log.i(TAG,
                "getFilepathFromUri system value result = " + filepath + " column = " + column);
        return filepath;
    }

    //[S][Settings] [donghan07.lee] Add DRM policy 2012.02.14
    // ANDY_PORTING LGDRM [lg-drm@lge.com 100414]
    /*
     * 0 : Not DRM 1 : Rights expired or can't be used as ringtone 2 : Valid
     * Rights exist for ringtone
     */

    public int checkDRM(String filename) {
        if (filename == null) {
            return 0;
        }

        int length = filename.length();
        if (false == (filename.regionMatches(true, length - 3, ".dm", 0, 3)
                || filename.regionMatches(true, length - 4, ".dcf", 0, 4)
                || filename.regionMatches(true, length - 4, ".odf", 0, 4)
                || filename.regionMatches(true, length - 4, ".o4a", 0, 4) || filename
                    .regionMatches(true, length - 4, ".o4v", 0, 4))) {
            return 0; // Normal file
        }

        int mDrmFile = DrmManager.isDRM(filename);
        if (mDrmFile < Drm.CONTENT_TYPE_DM || mDrmFile > Drm.CONTENT_TYPE_DCFV2) {
            mDrmFile = 0;
            return 0; // Normal file or not wrapped format
        }

        try {
            DrmContentSession session = DrmManager.createContentSession(
                    filename, mContext);
            if (false == session.isActionSupported(Drm.CONTENT_ACT_RINGTONE)) {
                return 1; // Expired
            }

            return 2;
        } catch (Exception e) {
            Log.w(TAG, "Exception");
        }

        return 1;
    }

    public void resetRingtone() {
        int length = sParentTypeDB.length;
        for (int i = 0; i < length; i++) {
            if (TYPE_ALARM == Integer.parseInt(sParentTypeDB[i][0])) {
                continue;
            }
            setParentType(Integer.parseInt(sParentTypeDB[i][0]));
            Uri uri = getDefaultPhoneRingtone();
            do_setURI(uri, Integer.parseInt(sParentTypeDB[i][0]));
        }
    }

    public void deleteRingtone(String deleteUri, int parentType) {
        Uri ringUri = Uri.parse(deleteUri);
        ContentResolver resolver = mContext.getContentResolver();
        if (parentType == TYPE_RINGTONE ||
                parentType == TYPE_RINGTONE_SIM2 ||
                parentType == TYPE_RINGTONE_SIM3 ||
                parentType == TYPE_RINGTONE_VC) {
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
        } else if (parentType == TYPE_NOTIFICATION ||
                parentType == TYPE_NOTIFICATION_SIM2 ||
                parentType == TYPE_NOTIFICATION_SIM3) {
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
    }

    private static String getDisneySelection(List<String> columns,
            boolean includeDrm, int cousorType) {

        if (columns == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(");

        for (int i = columns.size() - 1; i >= 0; i--) {
            sb.append("(");
            if (cousorType == DISNEY_CURSOR_TYPE) {
                sb.append(columns.get(i)).append(
                        "=1 and " + MediaStore.Audio.Media.ALBUM
                                + "= 'Disney Mobile') or ");
            } else if (cousorType == LG_CURSOR_TYPE) {
                sb.append(columns.get(i)).append(
                        "=1 and " + MediaStore.Audio.Media.ARTIST
                                + "= 'LG Electronics') or ");
            }
        }

        if (columns.size() > 0) {
            // Remove last ' or '
            sb.setLength(sb.length() - 4);
        }

        sb.append(")");

        Log.d("jw", "sb : " + sb.toString());

        if (!includeDrm) {
            sb.append(" and ");
            sb.append(MediaStore.MediaColumns.IS_DRM);
            sb.append("=0");
        }

        return sb.toString();
    }
}
