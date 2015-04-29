package com.android.settings.search;
        
import android.content.ContentResolver;
import android.content.ContentValues;
import android.util.Log;
import android.net.Uri;
import android.os.ServiceManager;
import android.os.IBinder;
import android.content.Context;
import android.os.RemoteException;
import com.android.internal.widget.*;
import android.app.admin.DevicePolicyManager;
import android.os.UserHandle;
import android.os.Handler;
import com.android.settings.Utils;       
       
public class VpnSearchIndexableItemUpdater {

    static final String CONTENT_URI = "content://com.android.settings.search.SearchDBupdateProvider/prefs_index";
    private static ILockSettings lockSettingsService = null;
    private LockPatternUtilsEx mLockPatternUtils;
    private Context mContext = null;
    private final String CHANGE_LOCKSCREEN = "lockscreen.password_type";
    private final String LOCK_SETTINGS = "lock_settings";
    private int mUserId = UserHandle.USER_OWNER;
    private boolean mLGVpnEnabled = false;

    private Handler mHandler = null;

    public VpnSearchIndexableItemUpdater(Context context) {
        mLGVpnEnabled = Utils.isSupportVPN(context);
        mContext = context;
        mHandler = new Handler();
        mLockPatternUtils = new LockPatternUtilsEx(mContext);
        lockSettingsService = ILockSettings.Stub.asInterface((IBinder) ServiceManager.getService(LOCK_SETTINGS));
        registerLockscreenObserver();
    }

    public boolean registerLockscreenObserver(){
        if (lockSettingsService != null) {
            try {
                //lockSettingsService.registerObserver(bObserver);// block code for L MR1
                return true;
            } catch (Exception e) { // RemoteException to Exception for L MR1
                return false;
            }
        }
        return false;
    }

    // block code for L MR1
    /*private final ILockSettingsObserver bObserver = new ILockSettingsObserver.Stub() {
        @Override
        public void onLockSettingChanged(String key, int userId) throws RemoteException {
            mUserId = userId;
            if (CHANGE_LOCKSCREEN.equals(key)) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int lockType = mLockPatternUtils.getKeyguardStoredPasswordQuality(mUserId);
                        switch (lockType) {
                            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
                            case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                            case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
                            case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
                            case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
                                updateVpnSearchDB(1);
                                break;
                            default:
                                updateVpnSearchDB(0);
                                break;
                        }
                    }
                }, 500);
            }
        }
    };*/

    private boolean updateVpnSearchDB(int visible) {
        ContentResolver cr = mContext.getContentResolver();
        ContentValues row = new ContentValues();
        if(cr != null && row != null){
            row.put("visible", visible);
            String where = "data_key_reference = ? AND class_name = ?";
            cr.update(Uri.parse(CONTENT_URI), row, where, new String[] {
                "add_network", "com.android.settings.vpn2.VpnSettings" });
            if(mLGVpnEnabled){
                cr.update(Uri.parse(CONTENT_URI), row, where, new String[] {
                    "add_network", "com.ipsec.vpnclient.MainActivity" });
                cr.update(Uri.parse(CONTENT_URI), row, where, new String[] {
                    "minimizeOnConnect", "com.ipsec.vpnclient.SettingsActivity" });
                cr.update(Uri.parse(CONTENT_URI), row, where, new String[] {
                    "resetKeyStore", "com.ipsec.vpnclient.SettingsActivity" });
            }
            return true;
        }
        return false;
    }
}





