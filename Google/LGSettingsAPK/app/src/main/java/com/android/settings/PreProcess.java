package com.android.settings;

import java.io.File;
import android.util.Log;

public class PreProcess {
    static String TAG = "PreProcess";
    static String encryptionInfoPaths[] = {
    "/mnt/sdcard/.ecryptfs/.sdinfo",
    "/mnt/external_sd/.ecryptfs/.sdinfo",
    "/mnt/_ExternalSD/.ecryptfs/.sdinfo"};

    public void execute(){
        Log.i(TAG, "START PreProcess");
        delEncryptionInfo();
    }

    private void delEncryptionInfo(){
        for(int i=0;i<encryptionInfoPaths.length;i++){
            File file = new File(encryptionInfoPaths[i]);
            if(file != null) file.delete();
            Log.i(TAG, encryptionInfoPaths[i]+" is deleted");
        }
    }
}
