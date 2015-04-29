package com.lge.handwritingime.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.lge.handwritingime.HandwritingKeyboard;

public class ThemeManager {
    private static final boolean DEBUG = HandwritingKeyboard.DEBUG;
    private static final boolean USE_ORIGINAL_IF_NOT_EXISTS = true;
    private static final String TAG = "LGHWIMEThemeManager";
    
    // resource name and type for checking right context for the theme
    private static final String TEST_NAME = "handwriting_ime";
    private static final String TEST_TYPE = "layout";
    
    private static final String ALTERNATIVE_PACKAGE_NAME_PREFIX = "com.lge.handwritingime.kbd";
    
    private Context mBaseContext;
    private Context mThemeContext;
    private SharedPreferences mSharedPref;
    private String mThemeName;
    

    public ThemeManager(Context baseContext) {
        mBaseContext = baseContext;
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(baseContext);
        // NOTE : mThemeName is not initiolized, because it should be null
        //        in case Theme is DEFAULT.
        // mThemeName = "It do not have theme";

        String className = mSharedPref.getString("keyboard_skin_add", "");
        if (DEBUG) Log.d(TAG, "className=" + className);        
        if (className.equals("")) {
            mThemeContext = mBaseContext;
            return;
        }
        String packageName = className.substring(0, className.lastIndexOf('.'));
        
        if (DEBUG) { // if DEBUG is set, check hwPackageName first.
            String hwPackageName = ALTERNATIVE_PACKAGE_NAME_PREFIX + packageName.substring(packageName.lastIndexOf('.'));
            mThemeContext = createContextFromPackageName(hwPackageName);
            if (mThemeContext != null) {
                return;
            }
        }
        
        mThemeContext = createContextFromPackageName(packageName);
        if (mThemeContext == null) {
            String hwPackageName = ALTERNATIVE_PACKAGE_NAME_PREFIX + packageName.substring(packageName.lastIndexOf('.'));
            mThemeContext = createContextFromPackageName(hwPackageName);
        }        
        if (mThemeContext == null) {
            mThemeContext = mBaseContext;
            return ;
        }
        
        mThemeName = className;
    }
    
    private Context createContextFromPackageName(String packageName) {
        Context context;
        try {
            context = mBaseContext.createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE
                    | Context.CONTEXT_IGNORE_SECURITY);
            if (DEBUG) Log.d(TAG, "createContextFromPackageName package name=" + packageName);
        } catch (NameNotFoundException e) {
            if (DEBUG) Log.w(TAG, "not found package " + packageName, e);
            return null;
        }
        
        int resId = context.getResources().getIdentifier(TEST_NAME, TEST_TYPE, packageName);
        if (resId == 0) {
            if (DEBUG) Log.w(TAG, "not found resource " + TEST_TYPE + "/" + TEST_NAME + " in package " + packageName);
            return null;
        }
        
        return context;
    }

    /**
     * Get resource id from theme package which has same resource name with parameter.
     * 
     * @param resid resource id from base context
     * @return resource id from theme package, <i>zero</i> if there is no resources
     * 
     */
    public int getResId(int resid) {
        if(mBaseContext == mThemeContext) {
            return resid;
        }
        
        Resources resources = mBaseContext.getResources();

        String type = resources.getResourceTypeName(resid);
        String entryName = resources.getResourceEntryName(resid);

        int themeResId = mThemeContext.getResources().getIdentifier(entryName, type, mThemeContext.getPackageName());
        Log.d(TAG, "name=" + entryName + " type=" + type);

        if (DEBUG) return themeResId;
        if (themeResId == 0) {
            themeResId = resid;
        }
        return themeResId;
    }

    public Context getThemeContext() {
        return mThemeContext;
    }
    
    public View inflate(int resid, ViewGroup root) {
        return View.inflate(mThemeContext, getResId(resid), root);
    }
    
    public View findViewById(View parent, int id) {
        return parent.findViewById(getResId(id));
    }
    
    public Drawable getDrawable(int id) {
        Drawable result = null;
        int themeId = getResId(id);
        if (themeId == 0 && USE_ORIGINAL_IF_NOT_EXISTS) {
            result = mBaseContext.getResources().getDrawable(id);
        } else {
            result = mThemeContext.getResources().getDrawable(themeId);
        }        
        return result;
    }
    
    // TODO : looks similar, refactoring needed
    public int getColor(int id) {
        int result = 0;        
        int themeId = getResId(id);
        if (themeId == 0 && USE_ORIGINAL_IF_NOT_EXISTS) {
            result = mBaseContext.getResources().getColor(id);
        } else {
            result = mThemeContext.getResources().getColor(themeId);
        }
        return result;
    }
    
    public boolean isThemeChanged() {
        if (mSharedPref == null) {
            mSharedPref = PreferenceManager.getDefaultSharedPreferences(HandwritingKeyboard.getBaseContext());
        }
        
        String className = mSharedPref.getString("keyboard_skin_add", "");
        if (DEBUG) Log.d(TAG, "mThemeName='" + mThemeName + "' className='" + className + "' isEqual=");
        
        if (mThemeName == null) {
            return true;
        }
        return !mThemeName.equals(className);
    }
}
