package com.android.settings;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Environment;
import android.util.Log;

import com.lge.bnr.framework.IBNRFrameworkAPI;
import com.lge.bnr.framework.LGBackupAgent;
import com.lge.bnr.framework.LGBackupException;
import com.lge.bnr.framework.LGBackupMediaScanner;
import com.lge.bnr.framework.LGBackupConstant;
import com.lge.bnr.model.BNRFailItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.net.Uri;
import android.provider.Settings;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SqliteWrapper;

//[jaewoong87.lee@lge.com] For Wi-Fi backup. [S]
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import android.os.FileUtils;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import java.util.HashSet;
import android.net.wifi.WifiManager;

//[jaewoong87.lee@lge.com] For Wi-Fi backup. [E]
public class SettingsLGBackupAgent extends LGBackupAgent {

    private static final String TAG = "SettingsLGBackupAgent";
    private Context mContext = null;
    public String mpkgName;
    public String mAction;
    public boolean bSettings_Sound = false;
    public boolean bSettings_Display = false;
    public boolean bSettings_Accessibility = false;
    public boolean bSettings_Wifi = false;
    public boolean bSettings_Call = false;
    //[jaewoong87.lee@lge.com] For Wi-Fi backup. [S]
    private WifiManager mWifiManager;
    private String mWifiConfigFile;
    private static final byte[] EMPTY_DATA = new byte[0];
    private static final String FILE_WIFI_SUPPLICANT = "wifi_backup.txt"; /* SELinux Neverallow: "/data/misc/wifi/wpa_supplicant.conf" */
    private static final String FILE_WIFI_SUPPLICANT_TEMPLATE =
            "/system/etc/wifi/wpa_supplicant.conf";
    private static final String FILE_WIFI_SUPPLICANT_BACKUP =
            "wifi_backup_copy.conf"; /* SELinux Neverallow: "/data/misc/wifi/wpa_supplicant_copy.conf" */
    private static final String FILE_WIFI_CONFIG_BACUP = "wifi_config.txt"; /* SELinux Neverallow: "/data/misc/wifi/ipconfig_copy.txt" */
    //[jaewoong87.lee@lge.com] For Wi-Fi backup. [E]

    public SettingsLGBackupAgent(Context c, Intent intent) {
        super(c, intent);
        // TODO Auto-generated constructor stub
        mContext = c;
        mpkgName = mContext.getPackageName();
        mAction = intent.getAction();
        bSettings_Sound = intent.getExtras().getBoolean("bSETTINGS_SOUND");
        bSettings_Display = intent.getExtras().getBoolean("bSETTINGS_DISPLAY");
        bSettings_Accessibility = intent.getExtras().getBoolean("bSETTINGS_ACCESS");
        bSettings_Wifi = intent.getExtras().getBoolean("bSETTINGS_WIFI");
        bSettings_Call = intent.getExtras().getBoolean("bSETTINGS_CALL");
        Log.d(TAG, "bSettings_Sound = " + bSettings_Sound);
        Log.d(TAG, "bSettings_Display = " + bSettings_Display);
        Log.d(TAG, "bSettings_Accessibility = " + bSettings_Accessibility);
        Log.d(TAG, "bSettings_Wifi = " + bSettings_Wifi);
        Log.d(TAG, "bSettings_Call = " + bSettings_Call);
    }

    private ArrayList<BNRrestoreItem> mArrRestoreList;
    private String mRestoreListXml;
    private final int mSelectPivot = 500;
    private final Uri mSYSTEM_URI = Settings.System.CONTENT_URI;
    private final Uri mSECURE_URI = Settings.Secure.CONTENT_URI;
    private final Uri mGLOBAL_URI = Settings.Global.CONTENT_URI;

    private final Uri[] mSetting_uri = { mSYSTEM_URI, mSECURE_URI, mGLOBAL_URI };

    private final Configuration mCurConfig = new Configuration();
    @Override
    public void onBackupCancel(IBNRFrameworkAPI bnr) {
        // TODO Auto-generated method stub
        Log.v(TAG, "SettingsLGBackupAgent.java onBackupCancel");
        LGBackupAgent.isCancle = true;
    }

    @Override
    public void onBackup(IBNRFrameworkAPI bnr) {
        // TODO Auto-generated method stub
        Log.v(TAG, "onBackup");

        BNRFailItem failItem = new BNRFailItem();

        try {
            if (LGBackupConstant.ACTION_REQUEST_SETTINGS.equals(mAction)) {
                Log.d(TAG, "backup SETTINGS [START]");
                //To do list 
                //---  previous task before backing up (Ex: Vcf file, Vmessage)   --------------------------//
                bnr.setBackupProgress(mpkgName, 0);

                bnr.setBackupProgress(mpkgName, 20);
                //----------------------------------------------------------------------------//	

                //for Test  
                //--- Create Test Data.-----------------------------------------------------------//
                ArrayList<File> FileList = new ArrayList<File>();

                // Settings DB always backup.
                //                if ((true == bSettings_Sound) || (true == bSettings_Display)
                //                        || (true == bSettings_Accessibility) || (true == bSettings_Wifi)
                //                        || (true == bSettings_Call)) {
                {
                    String font_size = Settings.System.getString(mContext.getContentResolver(), "font_scale");
                    if (font_size == null) {
                        Log.v(TAG, "Set default font size DB for LG Backup : " + Float.toString(mCurConfig.fontScale));
                        Settings.System.putString(mContext.getContentResolver(), "font_scale", Float.toString(mCurConfig.fontScale));
                    }
                    FileList.add(mContext
                            .getDatabasePath("/data/data/com.android.providers.settings/databases/settings.db"));
                    Log.d(TAG, "add backup Settings files");
                }
                if (true == bSettings_Wifi) {
                    if (makeWifiSupplicantBackUp()) {
                        FileList.add(mContext
                                .getDatabasePath(FILE_WIFI_SUPPLICANT_BACKUP));
                    }

                    if (makeWifiConfigBackUp()) {
                        FileList.add(mContext
                                .getDatabasePath(FILE_WIFI_CONFIG_BACUP));
                    }
                    Log.d(TAG, "add backup Wi-Fi files");
                }
                /*
                if (true == bSettings_Call) {
                    FileList.add(mContext
                            .getDatabasePath("/data/data/com.android.phone/databases/callreject.db"));

                    FileList.add(mContext
                            .getDatabasePath("/data/data/com.android.phone/databases/callsettings.db"));

                    FileList.add(mContext
                            .getDatabasePath("/data/data/com.android.phone/databases/quickmessage.db"));

                    FileList.add(mContext
                            .getDatabasePath("/data/data/com.android.phone/shared_prefs/PreCallDuration.xml"));

                    FileList.add(mContext
                            .getDatabasePath("/data/data/com.android.phone/shared_prefs/cdma_call_forwarding_date.xml"));

                    FileList.add(mContext
                            .getDatabasePath("/data/data/com.android.phone/shared_prefs/cdma_call_forwarding_number.xml"));

                    FileList.add(mContext
                            .getDatabasePath("/data/data/com.android.phone/shared_prefs/cdma_call_forwarding_token.xml"));

                    FileList.add(mContext
                            .getDatabasePath("/data/data/com.android.phone/shared_prefs/com.android.phone_preferences.xml"));

                    FileList.add(mContext
                            .getDatabasePath("/data/data/com.android.phone/shared_prefs/customized_number.xml"));

                    FileList.add(mContext
                            .getDatabasePath("/data/data/com.android.phone/shared_prefs/enable_call_forwarding_key.xml"));

                    Log.d(TAG, "add backup Call files");
                }
                */
                //--------------------------------------------------------------------------------//

                writeZip(mpkgName, FileList, 20, 80);

                //To do list 
                //--- delete Temp file for backing up   ----------------------//
                bnr.setBackupProgress(mpkgName, 81);
                deleteWifiBackupFilesOnBackup();

                bnr.setBackupProgress(mpkgName, 100);
                //------------------------------------------------------------------//	
                Log.d(TAG, "backup SETTINGS [END]");
            }

            failItem.setFailCode(0);
        } catch (LGBackupException e) {
            Log.v(TAG, "Backup Exception = " + e.getErrorCode().value());
            failItem.setFailCode(e.getErrorCode().value());

        } finally {
            failItem.setPackageNm(mpkgName);
            PackageManager pm = mContext.getPackageManager();
            try {
                PackageInfo pkgInfo = pm.getPackageInfo(mpkgName, 0);
                failItem.setPackageVersion(pkgInfo.versionName);
                failItem.setPackageVersionCode(pkgInfo.versionCode);
            } catch (NameNotFoundException e) {
                Log.v("TAG", "Backup Exception = " + "NameNotFoundException");
            }

            bnr.setBackupComplete(mpkgName, failItem);
        }

    }

    @Override
    public void onRestore(IBNRFrameworkAPI bnr, int versionCode,
            String versionName, String productInfo) {
        // TODO Auto-generated method stub
        Log.v(TAG, "onRestore");
        Log.v(TAG, "versionCode : " + versionCode + "\n" + "versionName : "
                + versionName + "\n" + "productInfo : " + productInfo);

        BNRFailItem failItem = new BNRFailItem();

        try {
            ArrayList<String> filePathList = writeUnzip(mpkgName,
                    "/data/data/com.android.settings", 0, 60);

            LGBackupMediaScanner mediaScanner = new LGBackupMediaScanner(mContext, filePathList);
            mediaScanner.runMediaScan();

            if (LGBackupConstant.ACTION_REQUEST_SETTINGS.equals(mAction)) {
                Log.d(TAG, "restore SETTINGS [START]");

                //To do list 
                //--- Restore DB from filePathList ----------------------------------//
                bnr.setRestoreProgress(mpkgName, 61);

                mRestoreListXml = "setting_restore_list.xml";
                if (true == bSettings_Sound) {
                    //--------------------
                    //to be implement
                    //--------------------
                    loadRestoreItemsFromXml("202");
                    SettingsRestore();
                    Log.d(TAG, "Restore Settings_Sound DB");
                }
                if (true == bSettings_Display) {
                    //--------------------
                    //to be implement
                    //--------------------
                    loadRestoreItemsFromXml("203");
                    SettingsRestore();
                    Log.d(TAG, "Restore Settings_Display DB");
                }
                if (true == bSettings_Accessibility) {
                    //--------------------
                    //to be implement
                    //--------------------
                    loadRestoreItemsFromXml("204");
                    SettingsRestore();
                    Log.d(TAG, "Restore Settings_Accessibility DB");
                }
                if (true == bSettings_Wifi) {
                    final int retainedWifiState = enableWifi(false);

                    restoreWifiSupplicant();
                    FileUtils.setPermissions(FILE_WIFI_SUPPLICANT,
                            FileUtils.S_IRUSR | FileUtils.S_IWUSR |
                                    FileUtils.S_IRGRP | FileUtils.S_IWGRP,
                            Process.myUid(), Process.WIFI_UID);

                    restoreWifiConfig();
                    enableWifi(retainedWifiState == WifiManager.WIFI_STATE_ENABLED ||
                            retainedWifiState == WifiManager.WIFI_STATE_ENABLING);

                    Log.d(TAG, "Restore Wi-Fi DB");
                }
                /*
                if (true == bSettings_Call) {
                    //--------------------
                    //to be implement
                    //--------------------
                    Log.d(TAG, "Restore Call DB");
                }
                */

                bnr.setRestoreProgress(mpkgName, 90);
                //-------------------------------------------------------------------//

                //To do list 
                //--- Do deleting restore completed files. If it is not needed. ----//
                bnr.setRestoreProgress(mpkgName, 91);

                bnr.setRestoreProgress(mpkgName, 100);
                //------------------------------------------------------------------//	
                Log.d(TAG, "restore SETTINGS [END]");
            }

            failItem.setFailCode(0);

        } catch (LGBackupException e) {
            Log.v(TAG, "Restore Exception = " + e.getErrorCode().value());
            failItem.setFailCode(e.getErrorCode().value());
        } finally {
            failItem.setPackageNm(mpkgName);
            PackageManager pm = mContext.getPackageManager();
            try {
                PackageInfo pkgInfo = pm.getPackageInfo(mpkgName, 0);
                failItem.setPackageVersion(pkgInfo.versionName);
                failItem.setPackageVersionCode(pkgInfo.versionCode);
            } catch (NameNotFoundException e) {
                Log.v("TAG", "Backup Exception = " + "NameNotFoundException");
            }

            bnr.setBackupComplete(mpkgName, failItem);
        }

    }

    @Override
    public void onRestoreOld(IBNRFrameworkAPI bnr, ArrayList<String> filePathList) {
        //when restore previous Backup File
        BNRFailItem failItem = new BNRFailItem();

        Log.v("TAG", "filePathList " + filePathList);

        LGBackupMediaScanner mediaScanner = new LGBackupMediaScanner(mContext, filePathList);
        mediaScanner.runMediaScan();

        //To do list 
        //--- Restore from old version LBF file to filePathList -------------------------//
        bnr.setRestoreProgress(mpkgName, 60);

        bnr.setRestoreProgress(mpkgName, 100);
        //-------------------------------------------------------------------//

        failItem.setPackageNm(mpkgName);
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo pkgInfo = pm.getPackageInfo(mpkgName, 0);
            failItem.setPackageVersion(pkgInfo.versionName);
            failItem.setPackageVersionCode(pkgInfo.versionCode);
        } catch (NameNotFoundException e) {
            Log.v("TAG", "Backup Exception = " + "NameNotFoundException");
        }

        bnr.setBackupComplete(mpkgName, failItem);

    }

    /////////////////////////////////////////////////////////////////////////////////////////
    ///////////Supported code for LGBackup Agent ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////

    public ArrayList<File> getFileListInDirectory(String pPath) {
        String[] sFileList;
        ArrayList<File> arrFileList = new ArrayList<File>();

        if (pPath.contains("\"")) {
            sFileList = pPath.trim().substring(1, pPath.length() - 1).split("\" \"");
        } else {
            sFileList = pPath.trim().split(" ");
        }

        for (int i = 0; i < sFileList.length; i++) {
            arrFileList.addAll(getOnlyFileList(sFileList[i]));
        }

        return arrFileList;
    }

    public ArrayList<File> getOnlyFileList(String pPath) {
        ArrayList<File> onlyFileList = new ArrayList<File>();

        File fRoot = new File(pPath);
        File[] fileList = fRoot.listFiles();

        if (fileList == null) {
            if (fRoot.isFile()) {
                onlyFileList.add(fRoot);
            }
            return onlyFileList;
        }

        for (File file : fileList) {
            if (file.isDirectory()) { // directory
                onlyFileList.addAll(getOnlyFileList(file.getAbsolutePath()));
            } else { // file
                onlyFileList.add(file);
            }
        }

        return onlyFileList;
    }

    private void loadRestoreItemsFromXml(String mCategoryNum) {

        mArrRestoreList = new ArrayList<BNRrestoreItem>();

        try {
            InputStream istr = mContext.getAssets().open(mRestoreListXml);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xrp = factory.newPullParser();
            xrp.setInput(istr, "UTF-8");

            int eventType = xrp.getEventType();
            String sTagName = "";
            String sCategoryName = "";
            String sStorage = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {

                switch (eventType) {
                case XmlPullParser.START_TAG: {
                    sTagName = xrp.getName();

                    if (sTagName.equals("category") && xrp.getAttributeCount() > 0) {
                        if (xrp.getAttributeValue(0).equals(mCategoryNum)) {
                            sCategoryName = xrp.getAttributeValue(0);
                        } else {
                            sCategoryName = "";
                        }
                    } else if (sTagName.equals("key") && (xrp.getAttributeCount() > 0)
                            && !"".equals(sCategoryName)) {
                        sStorage = xrp.getAttributeValue(0); // storage type
                    }
                }
                case XmlPullParser.TEXT: {
                    if (sTagName.equals("key")
                            && !"".equals(sCategoryName) && xrp.getText() != null
                            && !"".equals(xrp.getText().trim())) {
                        // save item data
                        BNRrestoreItem item = new BNRrestoreItem();
                        item.setItem(sCategoryName, xrp.getText(), sStorage);
                        Log.d(TAG, "sCategoryName : " + sCategoryName + "\n" + "xrp.getText() : "
                                + xrp.getText() + "\n" + "sStorage : " + sStorage);
                        mArrRestoreList.add(item);
                        Log.d(TAG, "mArrRestoreList : " + mArrRestoreList);
                    }
                }
                default:
                }

                eventType = xrp.next();
            }

        } catch (IOException e) {
            Log.e("EXCEPTION", "Exception", e);
        } catch (XmlPullParserException e) {
            Log.e("EXCEPTION", "Exception", e);
        }
    }

    public boolean SettingsRestore() {
        String tmpPath = "/data/data/com.android.settings";
        try {
            Runtime runtime = Runtime.getRuntime();
            String cmd = "chmod -R 751 " + tmpPath;
            runtime.exec(cmd);
        } catch (Exception e) {
            e.fillInStackTrace();
            Log.e("Process Manager", "Unable to execute top command");
        }
        // ---------------------------------------------------
        // Setting db restore

        ContentResolver cr = mContext.getContentResolver();
        String mBackUpPath = tmpPath
                + "/data/data/com.android.providers.settings/databases/settings.db";
        BNRDBController dbCon = new BNRDBController(mContext, mBackUpPath);
        dbCon.open(null);

        for (int i = 0; i < mSetting_uri.length; i++) {
            Uri uri = mSetting_uri[i];

            // select from backup DB
            int iSelectRound = 0;

            // get cursor from DB backuped system, secure
            Cursor cursor = null;
            try {
                Log.v(TAG, "uri.getLastPathSegment() : " + uri.getLastPathSegment());
                cursor = dbCon.select(uri.getLastPathSegment(),
                        mSelectPivot * iSelectRound, mSelectPivot
                                * (iSelectRound + 1));
                if (cursor != null) {
                    // update
                    Log.v("[TEST]", "Update!!!");
                    // change data from cursor to contentvalues and
                    // insert
                    ContentValues[] cvList = new ContentValues[cursor
                            .getCount()];
                    cursor.moveToFirst();

                    for (int k = 0; k < cursor.getCount(); k++) {
                        cvList[k] = new ContentValues();
                        DatabaseUtils.cursorRowToContentValues(cursor,
                                cvList[k]);
                        // update
                        String sWhere = (String)cvList[k].get("name");
                        String mValue = (String)cvList[k].get("value");
                        // before updating, check mArrRestoreList items
                        // that
                        // this key is in.
                        for (BNRrestoreItem rItem : mArrRestoreList) {
                            if (rItem.getStorage().equals(
                                    uri.getLastPathSegment()) // check
                                                              // the
                                                              // table
                                                              // name
                                                              // is
                                                              // same.
                                    && rItem.getKey().equals(sWhere)) {
                                Log.v(TAG, "rItem.getStorage() : " + rItem.getStorage());
                                Log.v(TAG, "uri.getLastPathSegment() :" + uri.getLastPathSegment());
                                Log.v(TAG, "rItem.getKey : " + rItem.getKey());
                                Log.v(TAG, "sWhere : " + sWhere);
                                Log.v(TAG, "mValue : " + mValue);
                                Log.v(TAG, "cr : " + cr);
                                Log.v(TAG, "uri : " + uri);
                                Log.v(TAG, "cvList[" + k + "] : " + cvList[k]);
                                /*
                                SqliteWrapper.update(mContext, cr, uri,
                                        cvList[k], "name = \"" + sWhere
                                                + "\"", null);
                               */
                                if ("system".equals(uri.getLastPathSegment())) {
                                    if ("sync_large_text".equals(sWhere)) {
                                        if ("1".equals(mValue)) {
                                            // Change Global DB in L OS (All model Multi user provided)
                                            Settings.System.putString(mContext.getContentResolver(), sWhere, "0");
                                            Settings.Global.putString(mContext.getContentResolver(), sWhere, mValue);
                                            Log.v(TAG, "Change Maximum font DB Global");
                                        }
                                    } else {
                                        Settings.System.putString(mContext.getContentResolver(), sWhere, mValue);
                                    }
                                } else if ("global".equals(uri.getLastPathSegment())) {
                                    Settings.Global.putString(mContext.getContentResolver(), sWhere, mValue);
                                } else {
                                    Settings.Secure.putString(mContext.getContentResolver(), sWhere, mValue);
                                }
                                break;
                            }
                        }
                        cursor.moveToNext();
                    }
                }
            } catch (Exception e) {
                Log.w("BNRSetting", "Unknown error : " + e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            Log.v("[TEST]", "Round " + (i + 1) + " end!!!");
        }

        dbCon.close();

        Log.v("[TEST]", "===<<< Restore Complete!!! >>>===");

        return true;
    }

    //[jaewoong87.lee@lge.com] For Wi-Fi backup.[S]
    /*
        private void restoreWifiSupplicant() {
            try {
                WifiNetworkSettings supplicantImage = new WifiNetworkSettings();

                File copiedSupplicantFile =
                    new File("/data/data/com.android.settings" + FILE_WIFI_SUPPLICANT_BACKUP);
                if (copiedSupplicantFile.exists()) {
                    // Retain the existing APs; we'll append the restored ones to them
                    BufferedReader in =
                        new BufferedReader(new FileReader("/data/data/com.android.settings"
                                                                + FILE_WIFI_SUPPLICANT_BACKUP));
                    supplicantImage.readNetworks(in);
                    in.close();

                    copiedSupplicantFile.delete();
                }

                File supplicantFile = new File(FILE_WIFI_SUPPLICANT);
                if (supplicantFile.exists()) {
                    // Retain the existing APs; we'll append the restored ones to them
                    BufferedReader in = new BufferedReader(new FileReader(FILE_WIFI_SUPPLICANT));
                    supplicantImage.readNetworks(in);
                    in.close();
                    supplicantFile.delete();
                }

                // Install the correct default template
                BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_WIFI_SUPPLICANT));
                copyWifiSupplicantTemplate(bw);

                // Write the restored supplicant config and we're done
                supplicantImage.write(bw);
                bw.close();
            } catch (IOException ioe) {
                Log.w(TAG, "Couldn't restore supplicant file");
            }
        }
    */
    private void restoreWifiSupplicant() {
        FileOutputStream filestream = null;
        BufferedOutputStream bufstream = null;
        DataOutputStream out = null;

        try {
            int copiedSize = 0;
            int headerSize = 0;

            //File copiedSupplicantFile =
            //        new File("/data/data/com.android.settings" + FILE_WIFI_SUPPLICANT_BACKUP);

            //if (copiedSupplicantFile.exists()) {
            byte[] copiedSupplicantData = getFileData("/data/data/com.android.settings"
                    + FILE_WIFI_SUPPLICANT_BACKUP);
            copiedSize = copiedSupplicantData.length;

            //copiedSupplicantFile.delete();
            //}

            File supplicantFile = new File(FILE_WIFI_SUPPLICANT);

            //if (supplicantFile.exists()) {
            byte[] supplicantData = getWifiSupplicantInfoData();
            headerSize = supplicantData.length;

            supplicantFile.delete();
            //}

            byte[] wifiSupplicantData = new byte[headerSize + copiedSize];
            System.arraycopy(supplicantData, 0, wifiSupplicantData, 0, headerSize);
            System.arraycopy(copiedSupplicantData, 0, wifiSupplicantData, headerSize, copiedSize);

            if (wifiSupplicantData.length != 0) {
                filestream = new FileOutputStream(FILE_WIFI_SUPPLICANT);
                bufstream = new BufferedOutputStream(filestream);
                out = new DataOutputStream(bufstream);

                out.write(wifiSupplicantData);
                }
        } catch (IOException ioe) {
            Log.w(TAG, "Couldn't restore supplicant file");
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
                if (bufstream != null) {
                    bufstream.close();
                }
                if (filestream != null) {
                    filestream.close();
                }
            } catch (IOException ioe) {
                Log.w(TAG, "Couldn't close" + FILE_WIFI_SUPPLICANT + " file");
            }
        }
    }

    private byte[] getWifiSupplicantInfoData() {
        InputStream is = null;

        try {
            File supplicantFile = new File(FILE_WIFI_SUPPLICANT);

            if (supplicantFile.exists()) {
                is = new FileInputStream(supplicantFile);

                byte[] supplicantData = new byte[(int)supplicantFile.length()];

                int offset = 0;
                int numRead = 0;
                while (offset < supplicantData.length
                        && (numRead = is.read(supplicantData, offset, supplicantData.length
                                - offset)) >= 0) {
                    offset += numRead;
                }

                //read failure
                if (offset < supplicantData.length) {
                    Log.w(TAG, "read failure " + FILE_WIFI_SUPPLICANT);
                    return EMPTY_DATA;
                }

                if (supplicantData.length != 0) {
                    String networkStr = new String(supplicantData, "utf-8");
                    //Log.d(TAG, "supplicantFile.length() : " + supplicantFile.length());
                    //Log.d(TAG, "supplicantData.length : " + supplicantData.length);
                    //Log.d(TAG, "networkStr.length() : " + networkStr.length());

                    int i = networkStr.indexOf("network={");
                    int j = networkStr.indexOf("#network={");
                    int k = networkStr.lastIndexOf("#}");
                    int size = 0;
                    Log.d(TAG, "i : " + i + ", j : " + j + ", k : " + k);

                    if (j == -1) { // "#network={" not found
                        if (i == -1) { // "network={" not found
                            size = (int)supplicantFile.length();
                        } else {
                            size = i;
                        }
                    } else { // wpa_supplicant.conf from "system/etc/wifi"
                        //size = (int)supplicantFile.length();
                        //if ((k + 3) != size)
                        size = (k + 3);
                    }
                    byte[] retData = new byte[size];

                    System.arraycopy(supplicantData, 0, retData, 0, size);

                    return retData;

                } else {
                    return EMPTY_DATA;
                }
            } else {
                return EMPTY_DATA;
            }
        } catch (IOException ioe) {
            //Log.w(TAG, "Couldn't backup " + filename);
            return EMPTY_DATA;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.w(TAG, "Couldn't close" + FILE_WIFI_SUPPLICANT + " file");
                }
            }
        }
    }

    private void restoreWifiConfig() {
        FileInputStream instream = null;
        DataInputStream in = null;
        try {
            File file = new File("/data/data/com.android.settings" + FILE_WIFI_CONFIG_BACUP);
            WifiManager mWfm = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
            if (mWfm != null) {
                mWifiConfigFile = mWfm.getConfigFile();
            } else {
                return;
            }

            if (file.exists()) {
                instream = new FileInputStream(file);
                in = new DataInputStream(instream);

                int nBytes = in.readInt();
                byte[] buffer = new byte[nBytes];
                in.readFully(buffer, 0, nBytes);
                restoreFileData(mWifiConfigFile, buffer, nBytes);

                file.delete();
            }

        } catch (IOException ioe) {
            Log.w(TAG, "Couldn't restore ipconfig file");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (instream != null) {
                    instream.close();
                }
            } catch (IOException ioe) {
                Log.w(TAG, "Couldn't close" + FILE_WIFI_CONFIG_BACUP + " file");
            }
        }
    }

    private void restoreFileData(String filename, byte[] bytes, int size) {
        FileOutputStream fs = null;
        OutputStream os = null;
        try {
            File file = new File(filename);
            if (file.exists()) {
                file.delete();
            }
            fs = new FileOutputStream(filename, true);
            os = new BufferedOutputStream(fs);
            os.write(bytes, 0, size);
        } catch (IOException ioe) {
            Log.w(TAG, "Couldn't restore " + filename);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException ioe) {
                Log.w(TAG, "Couldn't close" + filename + " file");
            }
        }
    }

    private void copyWifiSupplicantTemplate(BufferedWriter bw) {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(FILE_WIFI_SUPPLICANT_TEMPLATE);
            br = new BufferedReader(fr);
            char[] temp = new char[1024];
            int size;
            while ((size = br.read(temp)) > 0) {
                bw.write(temp, 0, size);
            }
        } catch (IOException ioe) {
            Log.w(TAG, "Couldn't copy wpa_supplicant file");
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException ioe) {
                Log.w(TAG, "Couldn't close" + FILE_WIFI_SUPPLICANT_TEMPLATE + " file");
            }

        }
    }

    private boolean makeWifiSupplicantBackUp() {
        
        FileOutputStream filestream = null;
        BufferedOutputStream bufstream = null;
        DataOutputStream out = null;
        

        File supplicantFile = new File(FILE_WIFI_SUPPLICANT);
        if (!supplicantFile.exists()) {
            return false;
        }

        byte[] wifiSupplicantData = getWifiSupplicant(FILE_WIFI_SUPPLICANT);

        try {
            if (wifiSupplicantData.length != 0) {
                filestream = new FileOutputStream(FILE_WIFI_SUPPLICANT_BACKUP);
                bufstream = new BufferedOutputStream(filestream);
                out = new DataOutputStream(bufstream);
                out.write(wifiSupplicantData);
            }
        } catch (IOException ioe) {
            Log.w(TAG, "Couldn't make wpa_supplicant_copy file");
            return false;
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }                
                if (bufstream != null) {
                    bufstream.close();
                }
                if (filestream != null) {
                    filestream.close();
                }
            } catch (IOException ioe) {
                Log.w(TAG, "Couldn't close" + FILE_WIFI_SUPPLICANT + " file");
            }
        }
        return true;
    }

    private boolean makeWifiConfigBackUp() {
        FileOutputStream filestream = null;
        BufferedOutputStream bufstream = null;
        DataOutputStream out = null;
        WifiManager mWfm = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        if (mWfm != null) {
            mWifiConfigFile = mWfm.getConfigFile();
        } else {
            return false;
        }

        File configFile = new File(mWifiConfigFile);
        if (!configFile.exists()) {
            return false;
        }

        byte[] wifiConfigData = getFileData(mWifiConfigFile);

        try {
            File file = new File(FILE_WIFI_CONFIG_BACUP);
            if (wifiConfigData.length != 0) {
                filestream = new FileOutputStream(file);
                bufstream = new BufferedOutputStream(filestream);
                out = new DataOutputStream(bufstream);

                out.writeInt(wifiConfigData.length);
                out.write(wifiConfigData);
            }
        } catch (IOException ioe) {
            Log.w(TAG, "Couldn't make ipconfig_copy.txt file");
            return false;
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
                if (bufstream != null) {
                    bufstream.close();
                }
                if (filestream != null) {
                    filestream.close();
                }
            } catch (IOException ioe) {
                Log.w(TAG, "Couldn't close" + FILE_WIFI_CONFIG_BACUP + " file");
            }
        }
        return true;
    }

    /*
        private byte[] getWifiSupplicant(String filename) {
            BufferedReader br = null;
            try {
                File file = new File(filename);
                if (file.exists()) {
                    br = new BufferedReader(new FileReader(file));
                    StringBuffer relevantLines = new StringBuffer();
                    boolean started = false;
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (!started && line.startsWith("network")) {
                            started = true;
                        }
                        if (started) {
                            relevantLines.append(line).append("\n");
                        }
                    }
                    if (relevantLines.length() > 0) {
                        return relevantLines.toString().getBytes();
                    } else {
                        return EMPTY_DATA;
                    }
                } else {
                    return EMPTY_DATA;
                }
            } catch (IOException ioe) {
                Log.w(TAG, "Couldn't backup " + filename);
                return EMPTY_DATA;
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        Log.w(TAG, "Couldn't close supplicant file");
                    }
                }
            }
        }
    */
    private byte[] getWifiSupplicant(String filename) {
        InputStream is = null;
        try {
            File file = new File(filename);
            is = new FileInputStream(file);

            //Will truncate read on a very long file,
            //should not happen for a config file
            byte[] bytes = new byte[(int)file.length()];

            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            //read failure
            if (offset < bytes.length) {
                Log.w(TAG, "Couldn't backup " + filename);
                return EMPTY_DATA;
            }

            if (bytes.length != 0) {
                String networkStr = new String(bytes, "utf-8");
                //Log.d(TAG, "file.length() : " + file.length());
                //Log.d(TAG, "bytes.length : " + bytes.length);
                //Log.d(TAG, "networkStr.length() : " + networkStr.length());

                int i = networkStr.indexOf("network={");
                int j = networkStr.indexOf("#network={");
                int k = networkStr.lastIndexOf("#}");
                Log.d(TAG, "i : " + i + ", j : " + j + ", k : " + k);

                if (j == -1) { // "#network={" not found
                    if (i == -1) { // "network={" not found
                        return EMPTY_DATA;
                    }
                } else {
                    if ((k + 3) == bytes.length) {
                        return EMPTY_DATA;
                    }
                }

                int size = 0;
                if (j != -1) { // "#network={" found
                    size = (int)file.length() - (k + 3);
                } else {
                    size = (int)file.length() - i;
                }
                byte[] networkData = new byte[size];

                System.arraycopy(bytes, i, networkData, 0, size);

                return networkData;
            } else {
                return EMPTY_DATA;
            }

        } catch (IOException ioe) {
            Log.w(TAG, "Couldn't backup " + filename);
            return EMPTY_DATA;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.w(TAG, "Couldn't close" + filename + " file");
                }
            }
        }
    }

    private byte[] getFileData(String filename) {
        InputStream is = null;
        try {
            File file = new File(filename);
            is = new FileInputStream(file);

            //Will truncate read on a very long file,
            //should not happen for a config file
            byte[] bytes = new byte[(int)file.length()];

            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            //read failure
            if (offset < bytes.length) {
                Log.w(TAG, "Couldn't backup " + filename);
                return EMPTY_DATA;
            }
            return bytes;
        } catch (IOException ioe) {
            Log.w(TAG, "Couldn't backup " + filename);
            return EMPTY_DATA;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.w(TAG, "Couldn't close" + filename + " file");
                }
            }
        }
    }

    private void deleteWifiBackupFilesOnBackup() {
        File supplicantFile = new File(FILE_WIFI_SUPPLICANT_BACKUP);
        if (supplicantFile.exists()) {
            supplicantFile.delete();
        }

        File configFile = new File(FILE_WIFI_CONFIG_BACUP);
        if (configFile.exists()) {
            configFile.delete();
        }
    }

    private int enableWifi(boolean enable) {

        if (mWifiManager == null) {
            mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        }
        if (mWifiManager != null) {
            int state = mWifiManager.getWifiState();
            mWifiManager.setWifiEnabled(enable);
            return state;
        } else {
            Log.e(TAG, "Failed to fetch WifiManager instance");
        }
        return WifiManager.WIFI_STATE_UNKNOWN;
    }

    // Class for capturing a network definition from the wifi supplicant config file
    static class Network {
        String mSsid = ""; // equals() and hashCode() need these to be non-null
        String mKey_mgmt = "";
        boolean mCertUsed = false;
        final ArrayList<String> mRawLines = new ArrayList<String>();

        public static Network readFromStream(BufferedReader in) {
            final Network n = new Network();
            String line;
            try {
                while (in.ready()) {
                    line = in.readLine();
                    if (line == null || line.startsWith("}")) {
                        break;
                    }
                    n.rememberLine(line);
                }
            } catch (IOException e) {
                return null;
            }

            return n;
        }

        void rememberLine(String line) {
            // can't rely on particular whitespace patterns so strip leading/trailing
            line = line.trim();
            if (line.isEmpty()) {
                return; // only whitespace; drop the line
            }

            mRawLines.add(line);

            // remember the ssid and key_mgmt lines for duplicate culling
            if (line.startsWith("ssid")) {
                mSsid = line;
            } else if (line.startsWith("key_mgmt")) {
                mKey_mgmt = line;
            } else if (line.startsWith("client_cert=")) {
                mCertUsed = true;
            } else if (line.startsWith("ca_cert=")) {
                mCertUsed = true;
            } else if (line.startsWith("ca_path=")) {
                mCertUsed = true;
            }
        }

        public void write(Writer w) throws IOException {
            w.write("\nnetwork={\n");
            for (String line : mRawLines) {
                w.write("\t" + line + "\n");
            }
            w.write("}\n");
        }

        public void dump() {
            Log.v(TAG, "network={");
            for (String line : mRawLines) {
                Log.v(TAG, "   " + line);
            }
            Log.v(TAG, "}");
        }

        // Same approach as Pair.equals() and Pair.hashCode()
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Network)) {
                return false;
            }
            final Network other;
            try {
                other = (Network)o;
            } catch (ClassCastException e) {
                return false;
            }
            return mSsid.equals(other.mSsid) && mKey_mgmt.equals(other.mKey_mgmt);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + mSsid.hashCode();
            result = 31 * result + mKey_mgmt.hashCode();
            return result;
        }
    }

    // Ingest multiple wifi config file fragments, looking for network={} blocks
    // and eliminating duplicates
    class WifiNetworkSettings {
        // One for fast lookup, one for maintaining ordering
        final HashSet<Network> mKnownNetworks = new HashSet<Network>();
        final ArrayList<Network> mNetworks = new ArrayList<Network>(8);

        public void readNetworks(BufferedReader in) {
            try {
                String line;
                while (in.ready()) {
                    line = in.readLine();
                    if (line != null) {
                        // Parse out 'network=' decls so we can ignore duplicates
                        if (line.startsWith("network")) {
                            Network net = Network.readFromStream(in);

                            if (!mKnownNetworks.contains(net)) {
                                mKnownNetworks.add(net);
                                mNetworks.add(net);
                            } else {

                            }
                        }
                    }
                }
            } catch (IOException e) {
                // whatever happened, we're done now
                Log.w(TAG, "readNetworks error");
            }
        }

        public void write(Writer w) throws IOException {
            for (Network net : mNetworks) {
                if (net.mCertUsed) {
                    continue;
                }
                net.write(w);
            }
        }

        public void dump() {
            for (Network net : mNetworks) {
                net.dump();
            }
        }
    }

    //[jaewoong87.lee@lge.com] For Wi-Fi backup. [E]

    public class BNRDBController {
        private Context mCtx;
        private DatabaseHelper mDbHelper;
        private SQLiteDatabase mDb;
        // private String DATABASE_NAME = mBackUpPath;
        private String mDATABASE_NAME = null;
        private int mDATABASE_VERSION;
        private int mInsertCnt = 0;

        private class DatabaseHelper extends SQLiteOpenHelper {
            private String[] mCreateTable;

            DatabaseHelper(Context context, String[] pSql) {
                super(context, mDATABASE_NAME, null, mDATABASE_VERSION);
                mCreateTable = pSql;
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                if (mCreateTable != null) {
                    for (int i = 0; i < mCreateTable.length; i++) {
                        db.execSQL(mCreateTable[i]);
                        Log.v("[TEST]", "<<<< Create " + mCreateTable[i] + " Table >>>>");
                    }
                }
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL("DROP TABLE IF EXISTS notes");
                onCreate(db);
            }
        }

        public BNRDBController(Context ctx, String dbName) {
            this.mCtx = ctx;
            mInsertCnt = 0;
            this.mDATABASE_NAME = dbName;
            this.mDATABASE_VERSION = Settings.System.getInt(ctx.getContentResolver(), "settings_db_version", 108);
        }

        public BNRDBController open(String[] pSql) throws SQLException {
            mDbHelper = new DatabaseHelper(mCtx, pSql);
            mDb = mDbHelper.getWritableDatabase();
            Log.v("[TEST]", "mDb : " + mDb);

            Log.v("[TEST]", "I'm Open!");
            return this;
        }

        public void close() {
            mDbHelper.close();
            Log.v("[TEST]", "I'm close!");
        }

        public int insert(String sTable, ContentValues[] pCvList) {
            // String sTable = db_uri.getLastPathSegment();

            try {
                mDb.beginTransaction();

                for (int i = 0; i < pCvList.length; i++) {
                    mDb.insert(sTable, null, pCvList[i]);
                    Log.v("[TEST]", "Insert " + mInsertCnt + " : " + pCvList[i].toString());
                    mInsertCnt++;
                }
                mDb.setTransactionSuccessful();
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLException", e);
            } finally {
                mDb.endTransaction();
            }
            return 1;
        }

        /*
         * public Cursor selectAll(String pTable) { return mDb.query(pTable,
         * null, null, null, null, null, null); }
         */

        public Cursor select(String pTable, int pStartIndex, int pEndIndex) {
            String[] sArg = new String[] { Integer.toString(pStartIndex),
                    Integer.toString(pEndIndex) };
            Log.v("[TEST]", "select : " + mDb.query(pTable, null, null, null, null, null, null));
            return mDb.query(pTable, null, null, null, null, null, null);
        }
    }

    public class BNRrestoreItem {

        private String mCategory;
        private String mKey;
        private String mStorage;

        public BNRrestoreItem() {
        }

        /**
         * set item
         * 
         * @param categoryName
         * @param key
         * @param storage
         */
        public void setItem(String categoryName, String key, String storage) {
            this.mCategory = categoryName;
            this.mKey = key;
            this.mStorage = storage;
        }

        public String getCategory() {
            return mCategory;
        }

        public String getKey() {
            return mKey;
        }

        public String getStorage() {
            return mStorage;
        }

    }

}
