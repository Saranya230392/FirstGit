/*
 * A3 Lab Mobile Communication R&D Center, Mobile Communication Company, LG ELECTRONICS INC., SEOUL, KOREA
 * Copyright(c) 2013 by LG Electronics Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LG Electronics Inc.
 */

package com.android.settings.applications;

import android.content.pm.PackageManager;

/*
 *  Share string with Setting. (ex. action name, extra name, specific result message)
 */
public class EnumManager {

    public static enum SupportStorage {
        Support_InternalSD_Only, // System Memory + InternalSD
        Support_ExternalSD_Only, // System Memory + ExternalSD
        Support_DualSD, // System Memory + InternalSD + ExternalSD
    }

    public static enum StorageType {
        Internal, // System Memory
        Ext_Primary, // ExterSD
        Ext_Secondary, // Other External SD
    }

    public static enum RecordType {
        APK_INFO_TYPE, REQUEST_INFO_TYPE,
    }

    public static enum ApkStatus {
        INVALID_PACKAGE(0, "invalid pakcage name"), // cant't find that installed package
        BACKUP_SUCCESS(1, "backup success"), // backup success
        RECOVERY_SUCCESS(2, "recovery success"), // recovery success
        REMOVE_SUCCESS(3, "remove success"); // delete success

        private int mIndex = 0;
        String mValue = null;

        ApkStatus(int index, String value) {
            mIndex = index;
            mValue = value;
        }

        public int getIndex() {
            return mIndex;
        }

        public String getValue() {
            return mValue;
        }
    }

    public static enum RequestType {
        REQUEST_TYPE_BACKUP("backup"), // request backup
        REQUEST_TYPE_RECOVERY("recovery"), // request recovery
        REQUEST_TYPE_DELETE("delete"); // request delete

        private String mValue = null;

        RequestType(String value) {
            mValue = value;
        }

        public String getValue() {
            return mValue;
        }
    }

    public static enum RequestResult {
        SUCCESS("success"), // request function success
        FAIL("fail"), // request function fail
        NULL("null"); // null
        private String mValue = null;

        RequestResult(String value) {
            mValue = value;
        }

        public String getValue() {
            return mValue;
        }
    }

    public static enum UriType {
        APK_LIST_URI("content://com.lge.apprecovery.provider.AppRecoveryProvider/apklist");

        private String mValue = null;

        UriType(String value) {
            mValue = value;
        }

        public String getValue() {
            return mValue;
        }
    }

    public static enum IntentActionType {
        REQUEST_BACKUP("com.lge.apprecovery.action.VZW_APP_BACKUP"), // request action to apprecovery about backup
        REQUEST_RECOVERY("com.lge.apprecovery.action.VZW_APP_RECOVERY"), // request action to apprecovery about recovery

        REQUEST_REMOVE_BACKUP_DATA("com.lge.apprecovery.action.VZW_REMOVE_BACKUP_DATA"), // remove backup date
        REQUEST_BACKUP_CANCEL("com.lge.apprecovery.action.VZW_REQUEST_BACKUP_CANCEL"), // cancel backup request

        RESULT_BACKUP("com.lge.settings.action.VZW_APP_BACKUP_RESULT"), // result action to setting about backup
        RESULT_RECOVERY("com.lge.settings.action.VZW_APP_RECOVERY_RESULT"), // result action to setting about recovery

        // result action to setting about recovery
        RESULT_TOAST("com.lge.appmultidelete.action.toastpopup"),
        REQUEST_DELETE_EXPIRED_BACKUP_DATA(
                "com.lge.apprecovery.action.REQUEST_DELETE_EXPIRED_BACKUP_DATA");
        // start delete alarm

        private String mValue = null;

        IntentActionType(String value) {
            mValue = value;
        }

        public String getValue() {
            return mValue;
        }
    }

    public static enum ExtraNameType {
        EXTRA_PACKAGE_NAME("app_package_name"), // application package name
        EXTRA_RESULT("request_result"), // backup, recovery function
        EXTRA_MESSAGE("request_specific meesage"), // specific message
        EXTRA_PACKAGE_INSTALL_DATE("app_package_date"), // application installed date
        EXTRA_PACKAGE_INSTALL_SIZE("app_package_size"), // application install size
        EXTRA_PACKAGE_SUCCESS_CNT("app_success_cnt"), // success item count
        EXTRA_PACKAGE_TOTAL_CNT("app_total_cnt"); // total item count
        private String mValue = null;

        ExtraNameType(String value) {
            mValue = value;
        }

        public String getValue() {
            return mValue;
        }
    }

    public static enum DBColumnType {
        COLUMN_PACKAGE_NAME("package_name"), // package name
        COLUMN_APP_NAME("app_name"), // app name
        COLUMN_APP_VERSION_NAME("app_version_name"), // app version
        COLUMN_APK_FILE_SIZE("apk_file_size"), // apk file size
        COLUMN_APP_ICON("app_icon"), // app icon
        COLUMN_STATUS("status"), // apk status
        COLUMN_APP_PACKAGE_INSTALL_DATE("app_package_date"), // package install date
        COLUMN_APP_PACKAGE_INSTALL_SIZE("app_package_size"); // package install size
        private String mValue = null;

        DBColumnType(String value) {
            mValue = value;
        }

        public String getValue() {
            return mValue;
        }
    }

    public static enum BackupMessage {
        BACKUP_SUCCESS(0, "Success File Backup."), // 0
        // default msg
        BACKUP_FAILED_INVALID_PACKAGE_NAME(1, "Failed to find package name. Package name is null."), // 1
        BACKUP_FAILED_CANNOT_FIND_SRC_FILE_PATH(2, "Failed to find src file path."), // 2
        BACKUP_FAILED_INVALID_SRC_FILE_PATH(3, "SrcFilePath doesn't contain .apk"), // 3
        BACKUP_FAILED_INSUFFICIENT_STORAGE(4, "Storage is not enough to backup"), // 4
        BACKUP_FAILED_CANNOT_MAKE_DEST_FOLDER(5, "Failed to make destination folder"), // 5
        BACKUP_FAILED_CANNOT_GET_APK_FILE_NAME(6, "Failed to get apk file name"), // 6
        BACKUP_FAILED_CANNOT_COPY_APK_FILE(7, "Failed to copy apk file."), // 7
        BACKUP_FAILED_DUPLICATE_PACKAGE(8, "It is already backed up with the same name."), // 8
        BACKUP_REQUEST_CANCELED(9, "Backup request has been cannceled"); // 9

        private int mIndex = 0;
        private String mValue = null;

        private BackupMessage(int errCode, String desc) {
            this.mIndex = errCode;
            this.mValue = desc;
        }

        public int getIndex() {
            return mIndex;
        }

        public String getValue() {
            return mValue;
        }
    }

    public static enum RecoveryMessage {
        /* platform error message */
        INSTALL_SUCCEEDED(PackageManager.INSTALL_SUCCEEDED, "Install Success."), // 1
        INSTALL_FAILED_ALREADY_EXISTS(PackageManager.INSTALL_FAILED_ALREADY_EXISTS,
                "the package is already installed."), // -1
        INSTALL_FAILED_INVALID_APK(PackageManager.INSTALL_FAILED_INVALID_APK,
                "the package archive file is invalid."), // -2
        INSTALL_FAILED_INVALID_URI(PackageManager.INSTALL_FAILED_INVALID_URI,
                "the URI passed in is invalid."), // -3
        INSTALL_FAILED_INSUFFICIENT_STORAGE(PackageManager.INSTALL_FAILED_INSUFFICIENT_STORAGE,
                "the package manager service found that the device didn't have enough"
                        + " storage space to install the app."), // -4
        INSTALL_FAILED_DUPLICATE_PACKAGE(PackageManager.INSTALL_FAILED_DUPLICATE_PACKAGE,
                "a package is already installed with the same name."), // -5
        INSTALL_FAILED_NO_SHARED_USER(PackageManager.INSTALL_FAILED_NO_SHARED_USER,
                "the requested shared user does not exist."), // -6
        INSTALL_FAILED_UPDATE_INCOMPATIBLE(PackageManager.INSTALL_FAILED_UPDATE_INCOMPATIBLE,
                "a previously installed package of the same name has a different signature "
                        + "than the new package (and the old package's data was not removed)."), // -7
        INSTALL_FAILED_SHARED_USER_INCOMPATIBLE(
                PackageManager.INSTALL_FAILED_SHARED_USER_INCOMPATIBLE,
                "the new package is requested a shared user which is already installed on the"
                        + " device and does not have matching signature."), // -8
        INSTALL_FAILED_MISSING_SHARED_LIBRARY(PackageManager.INSTALL_FAILED_MISSING_SHARED_LIBRARY,
                "the new package uses a shared library that is not available."), // -9
        INSTALL_FAILED_REPLACE_COULDNT_DELETE(PackageManager.INSTALL_FAILED_REPLACE_COULDNT_DELETE,
                "the new package uses a shared library that is not available."), // -10

        INSTALL_FAILED_DEXOPT(PackageManager.INSTALL_FAILED_DEXOPT,
                "the new package failed while optimizing and validating its dex files,"
                        + " either because there was not enough storage or the validation failed."), // -11
        INSTALL_FAILED_OLDER_SDK(PackageManager.INSTALL_FAILED_OLDER_SDK,
                "the new package failed because the current SDK version is older than"
                        + " that required by the package."), // -12
        INSTALL_FAILED_CONFLICTING_PROVIDER(PackageManager.INSTALL_FAILED_CONFLICTING_PROVIDER,
                "the new package failed because it contains a content provider with the"
                        + " same authority as a provider already installed in the system."), // -13
        INSTALL_FAILED_NEWER_SDK(PackageManager.INSTALL_FAILED_NEWER_SDK,
                "the new package failed because the current SDK version is newer than"
                        + " that required by the package."), // -14
        INSTALL_FAILED_TEST_ONLY(
                PackageManager.INSTALL_FAILED_TEST_ONLY,
                "the new package failed because it has specified that it is a test-only"
                        + " package and the caller has not supplied the {@link #INSTALL_ALLOW_TEST} flag."), // -15
        INSTALL_FAILED_CPU_ABI_INCOMPATIBLE(PackageManager.INSTALL_FAILED_CPU_ABI_INCOMPATIBLE,
                "the package being installed contains native code, but none that is"
                        + " compatible with the the device's CPU_ABI."), // -16
        INSTALL_FAILED_MISSING_FEATURE(PackageManager.INSTALL_FAILED_MISSING_FEATURE,
                "the new package uses a feature that is not available."), // -17
        INSTALL_FAILED_CONTAINER_ERROR(PackageManager.INSTALL_FAILED_CONTAINER_ERROR,
                "a secure container mount point couldn't be accessed on external media."), // -18
        INSTALL_FAILED_INVALID_INSTALL_LOCATION(
                PackageManager.INSTALL_FAILED_INVALID_INSTALL_LOCATION,
                "the new package couldn't be installed in the specified install location."), // -19
        INSTALL_FAILED_MEDIA_UNAVAILABLE(PackageManager.INSTALL_FAILED_MEDIA_UNAVAILABLE,
                "the new package couldn't be installed in the specified install"
                        + " location because the media is not available."), // -20
        INSTALL_FAILED_VERIFICATION_TIMEOUT(PackageManager.INSTALL_FAILED_VERIFICATION_TIMEOUT,
                "the new package couldn't be installed because the verification timed out."), // -21
        INSTALL_FAILED_VERIFICATION_FAILURE(PackageManager.INSTALL_FAILED_VERIFICATION_FAILURE,
                "the new package couldn't be installed because the verification did not succeed."), // -22
        INSTALL_FAILED_PACKAGE_CHANGED(PackageManager.INSTALL_FAILED_PACKAGE_CHANGED,
                "the package changed from what the calling program expected."), // -23
        INSTALL_FAILED_UID_CHANGED(PackageManager.INSTALL_FAILED_UID_CHANGED,
                "the new package is assigned a different UID than it previously held."), // -24

        INSTALL_PARSE_FAILED_NOT_APK(PackageManager.INSTALL_PARSE_FAILED_NOT_APK,
                "the parser was given a path that is not a file, or does not end with the expected"
                        + " '.apk' extension."), // -100
        INSTALL_PARSE_FAILED_BAD_MANIFEST(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST,
                "the parser was unable to retrieve the AndroidManifest.xml file."), // -101
        INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION(
                PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION,
                "the parser did not find any certificates in the .apk."), // -102
        INSTALL_PARSE_FAILED_NO_CERTIFICATES(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES,
                "the parser did not find any certificates in the .apk."), // -103
        INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES(
                PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES,
                "the parser found inconsistent certificates on the files in the .apk."), // -104
        INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING(
                PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING,
                "the parser encountered a CertificateEncodingException in one of the"
                        + " files in the .apk."), // -105
        INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME(PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME,
                "the parser encountered a bad or missing package name in the manifest."), // -106
        INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID(
                PackageManager.INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID,
                "the parser encountered a bad shared user id name in the manifest."), // -107
        INSTALL_PARSE_FAILED_MANIFEST_MALFORMED(
                PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED,
                "the parser encountered some structural problem in the manifest."), // -108
        INSTALL_PARSE_FAILED_MANIFEST_EMPTY(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY,
                "the parser did not find any actionable tags (instrumentation or application)"
                        + " in the manifest."), // -109
        INSTALL_FAILED_INTERNAL_ERROR(PackageManager.INSTALL_FAILED_INTERNAL_ERROR,
                "the system failed to install the package because of system issues."), // -110

        /* Custom error message */
        CANNOT_FIND_BACKUP_DATA(10,
                "there is no information of requested package in the backup db."), // 10
        CANNOT_FIND_APK_FILE(11,
                "there is no information of apk file related to requested packaged"), // 11
        ACCOUNT_IS_NOT_MATCHED(12, "backuped account is not same with current account!"), // 12
        CANNOT_GET_BACKUP_SRC_FILE_PATH(13, "cannot get backup file path!"); // 13

        private int mIndex = 0;
        private String mValue = null;

        RecoveryMessage(int errorCode, String desp) {
            this.mIndex = errorCode;
            this.mValue = desp;
        }

        public int getIndex() {
            return this.mIndex;
        }

        public String getValue() {
            return this.mValue;
        }
    }

    public static enum DeleteMessage {
        DELETE_SUCCESS(0, "Success File Delete by Request."), // 0
        DELETE_SUCCESS_INSTALLED_FILE(1,
                "Success to Delete the File that is installed by other path."), // 1
        DELETE_SUCCESS_EXPIRED_FILE(2, "Success to Delete the File that is expired."), // 2
        DELETE_FAILED_INVALID_FILE_PATH(3, "Failed to Delete the File for invalid file path."), // 3
        DELETE_ALREADY_BACKUPED_FILE_BY_CANCEL(4,
                "Success to Delete already backuped file by cancel"); // 4

        private int mIndex = 0;
        private String mValue = null;

        private DeleteMessage(int errCode, String desc) {
            this.mIndex = errCode;
            this.mValue = desc;
        }

        public int getIndex() {
            return mIndex;
        }

        public String getValue() {
            return mValue;
        }
    }

    static public RecoveryMessage getRecoveryMessageFromIndex(int index) {
        RecoveryMessage retMessage = RecoveryMessage.CANNOT_FIND_APK_FILE;

        RecoveryMessage[] messages = RecoveryMessage.values();
        for (RecoveryMessage message : messages) {
            if (message.getIndex() == index) {
                retMessage = message;
            }
        }

        return retMessage;
    }

    static public ApkStatus getApkStatusFromString(String value) {
        ApkStatus ret = ApkStatus.INVALID_PACKAGE;

        ApkStatus[] statusList = ApkStatus.values();
        for (ApkStatus status : statusList) {
            if (status.getValue().equals(value)) {
                ret = status;
            }
        }

        return ret;
    }
}