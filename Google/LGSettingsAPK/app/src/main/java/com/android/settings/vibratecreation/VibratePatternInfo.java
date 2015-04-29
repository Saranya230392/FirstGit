package com.android.settings.vibratecreation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import com.android.settings.R;
import android.os.SystemProperties;
import com.android.settings.Utils;

public class VibratePatternInfo {
    private SharedPreferences mVibrateLGPatternPref; // Vibrate LG pattern
    private SharedPreferences mVibrateUserPatternPref; // Vibrate User pattern
    private SharedPreferences.Editor mVibratePrefEditor;
    private SharedPreferences.Editor mVibrateUserPrefEditor;
    public VibrateUserPatternManager mUserPatternManager;
    private Map<String, ?> mPrefsLGPatternMap; // LGPattern
    private Map<String, ?> mPrefsUserPatternMap; // User Pattern
    public int mHaptic_value = 0;
    private TimerTask task;
    private Timer timer;

    private static final String TAG = "VibratePatternInfo";
    public static final String EMPTY = "empty";
    private static final String LG_VIBRATE = "lg_vibrate_pattern";
    private static final String USER_VIBRATE = "user_vibrate_pattern";

    public static final int INCOMING_CALL_SIM1 = 0;
    public static final int INCOMING_CALL_SIM2 = 1;
    public static final int MESSAGE = 2;
    public static final int EMAIL = 3;
    public static final int ALARM = 4;
    public static final int CALENDAR = 5;
    public static final int INCOMING_CALL_SIM3 = 6;
    public static final int DEFAULT_HAPTIC = 1;
    public static final String PARENT_TYPE = "vibrate_parent_type";
    public static final String PKG = "com.android.settings";
    public static final String BG_COLOR_ID = "vibrate_create_bg_color";

    private static final int NOT_FONUD = -1;

    private static final int DEFAULT_VIRBATE_COUNT = 1;
    private static final int DEFAULT_VIBRATE_PATTERN_NUM = 2;

    // Setting DB name

    public static final String[] VIBRATE_NAME_SPR = {
            "distinctive_vibration_incoming_calls",
            "distinctive_vibration_sub_incoming_calls",
            "distinctive_vibration_messaging",
            "distinctive_vibration_email",
            "distinctive_vibration_alarm",
            "distinctive_vibration_calendar",
            "distinctive_vibration_third_incoming_calls",
    };
    public static final String[] VIBRATE_NAME = {
            "default_vibrate_name",
            "default_sub_vibrate_name",
            "default_message_vibrate_name",
            "default_email_vibrate_name",
            "default_alarm_vibrate_name",
            "default_calendar_vibrate_name",
            "default_third_vibrate_name",
    };

    public static final String[] VIBRATE_PATTERN = {
            "default_vibrate_pattern",
            "default_sub_vibrate_pattern",
            "default_message_vibrate_pattern",
            "default_email_vibrate_pattern",
            "default_alarm_vibrate_pattern",
            "default_calendar_vibrate_pattern",
            "default_third_vibrate_pattern",
    };

    public static final String USER_VIBRATION_COUNT = "user_vibration_conut";

    public static final String[] VIBRATE_TYPE = {
            "0, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 0", // Rapid
            "0, 400, 600, 400, 600, 400, 600, 0", //Short Repeated
            "0, 960, 320, 960, 320, 960, 320, 0", // Standard
            "0, 2000, 200, 0", // Long Lasting
            "0, 250, 750, 250, 750, 250, 750, 250, 750, 0" }; // Ticktock
    private Context mContext;
    private static String mOriginalDefaultVibrate;
    private static String mOriginalDefaultPattern;
    String[] vibrate_type_entry;
    String[] vibrate_type_value;
    ArrayList<String> vibrate_name;
    ArrayList<String> vibrate_pattern;
    private static boolean isItemSelected = false;

    public VibratePatternInfo(Context context, int parent_type) {
        // TODO Auto-generated constructor stub
        mContext = context;

        //getShardPreference(mVibrateLGPatternPref);
        //getShardPreference(mVibrateUserPatternPref);
        mVibrateLGPatternPref = context.getSharedPreferences(LG_VIBRATE, 0);
        mVibrateUserPatternPref = context.getSharedPreferences(USER_VIBRATE, 0);
        if (Utils.isSPRModel()) {
            mUserPatternManager = new VibrateUserPatternManager(mContext);
            vibrate_type_entry = mContext.getResources().getStringArray(R.array.preferences_labels);
            vibrate_type_value = mContext.getResources().getStringArray(R.array.preferences_values);
        }
        mVibratePrefEditor = mVibrateLGPatternPref.edit();
        mVibrateUserPrefEditor = mVibrateUserPatternPref.edit();

        defaultVibrateSet();

        if (Utils.isSPRModel()) {
            UpdateUserPatternInfo(); //for sprint
        }
        //mUserVibrate = new ArrayList<String[]>();
        mHaptic_value = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 1);
    }

    public static boolean getItemSelected() {
        //Log.d(TAG, "getItemSelected " +  isItemSelected);
        return isItemSelected;
    }

    public static void setItemSelected(boolean value) {
        //Log.d(TAG, "setItemSelected " +  isItemSelected);
        isItemSelected = value;
    }

    private void defaultVibrateSet() {
        mVibratePrefEditor.clear();
        if (Utils.isSPRModel()) {
            try {
                mVibratePrefEditor.putString(vibrate_type_entry[0],
                        getVibratePatterns(Integer.parseInt(vibrate_type_value[0])));
                mVibratePrefEditor.putString(vibrate_type_entry[1],
                        getVibratePatterns(Integer.parseInt(vibrate_type_value[1])));
                mVibratePrefEditor.putString(vibrate_type_entry[2],
                        getVibratePatterns(Integer.parseInt(vibrate_type_value[2])));
                mVibratePrefEditor.putString(vibrate_type_entry[3],
                        getVibratePatterns(Integer.parseInt(vibrate_type_value[3])));
                mVibratePrefEditor.putString(vibrate_type_entry[4],
                        getVibratePatterns(Integer.parseInt(vibrate_type_value[4])));
                mVibratePrefEditor.putString(vibrate_type_entry[5],
                        getVibratePatterns(Integer.parseInt(vibrate_type_value[5])));
                mVibratePrefEditor.putString(vibrate_type_entry[6],
                        getVibratePatterns(Integer.parseInt(vibrate_type_value[6])));
            } catch (Exception e) {
                Log.w(TAG, "Exception");
            }
        }
        else {
            mVibratePrefEditor.putString(
                    mContext.getResources().getString(R.string.vibrate_pattern_rapid),
                    removeToken(VIBRATE_TYPE[0]));
            mVibratePrefEditor.putString(
                    mContext.getResources().getString(R.string.vibrate_pattern_short_repeated),
                    removeToken(VIBRATE_TYPE[1]));
            mVibratePrefEditor.putString(
                    mContext.getResources().getString(R.string.vibrate_pattern_standard),
                    removeToken(VIBRATE_TYPE[2]));
            mVibratePrefEditor.putString(
                    mContext.getResources().getString(R.string.vibrate_pattern_long_lasting),
                    removeToken(VIBRATE_TYPE[3]));
            mVibratePrefEditor.putString(
                    mContext.getResources().getString(R.string.vibrate_pattern_ticktock),
                    removeToken(VIBRATE_TYPE[4]));
        }
        mVibratePrefEditor.commit();
        Log.d(TAG, "Shardpreference empty case");
    }

    public void UpdateUserPatternInfo() {
        mVibrateUserPrefEditor.clear();
        try {
            if (mUserPatternManager != null) {
                int nCount = mUserPatternManager.getUserPatternCount();
                vibrate_name = mUserPatternManager.getAllUserPatternName();
                vibrate_pattern = mUserPatternManager.getAllUserPattern();
                for (int i = 0; i < nCount; i++) {
                    mVibrateUserPrefEditor.putString(vibrate_name.get(i), vibrate_pattern.get(i));
                    Log.d(TAG, "defaultUserVibrateSet : " + vibrate_name.get(i) + " "
                            + vibrate_pattern.get(i));
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Exception");
        }

        mVibrateUserPrefEditor.commit();
        Log.d(TAG, "Shardpreference empty case");
    }

    public void checkedLGVibrateName(int simType, String pattern) {
        if (isLGPattern(pattern)) {
            defaultVibrateSet();
            mPrefsLGPatternMap = mVibrateLGPatternPref.getAll();
            for (Map.Entry<String, ?> entry : mPrefsLGPatternMap.entrySet())
            {
                //Log.d(TAG,"[checkedLGVibrateName] getkey : " + entry.getKey().toString());
                //Log.d(TAG,"[checkedLGVibrateName] getvalue : " + ((String)entry.getValue()).toString());
                if (pattern.equals(entry.getValue().toString())) {
                    Log.d(TAG, "[checkedLGVibrateName] matching vibrate - "
                            + entry.getKey().toString());
                    if (!Utils.isSPRModel()) {
                        setDBVibrateName(simType, entry.getKey().toString());
                        break;
                    }
                }
            }
        }
    }

    public void saveVibratePattern(String vibrateName, String pattern) {
        mVibrateUserPrefEditor.putString(vibrateName, pattern);
        mVibrateUserPrefEditor.commit();
        if (Utils.isSPRModel()) {
            if (mUserPatternManager != null) {
                mUserPatternManager.insertVibrateUserPattern(vibrateName, pattern);
                UpdateUserPatternInfo();
            }
        }
    }

    public void removeVibratePattern(String vibrateName) {
        mVibrateUserPrefEditor.remove(vibrateName);
        mVibrateUserPrefEditor.commit();
    }

    public void removeVibrateInfo() {
        mVibrateUserPrefEditor.clear();
        mVibrateUserPrefEditor.commit();
    }

    public String getPattern(String vibrateName) {
        String pattern = EMPTY;
        pattern = getLGPattern(vibrateName);
        if (!EMPTY.equals(pattern)) {
            return pattern;
        }
        else {
            pattern = getUserPattern(vibrateName);
            if (!(EMPTY.equals(pattern))) {
                return pattern;
            }
        }
        return EMPTY;
    }

    public boolean isDuplicateName(String name) {
        return isLGName(name) || isUserName(name);
    }

    public boolean isLGName(String vibrateName) {
        mPrefsLGPatternMap = mVibrateLGPatternPref.getAll();
        for (Map.Entry<String, ?> entry : mPrefsLGPatternMap.entrySet())
        {
            //Log.d(TAG,"[isLGName] getkey : " + entry.getKey().toString());
            if (vibrateName.equals(entry.getKey().toString())) {
                Log.d(TAG, "[isLGName] matching vibrate - " + entry.getKey().toString());
                return true;
            }
        }
        return false;
    }

    public boolean isUserPatternEmply() {
        boolean isEmpty = true;
        try {
            isEmpty = mVibrateUserPatternPref.getAll().isEmpty();
        } catch (NullPointerException e) {
            return true;
        }
        return isEmpty;
    }

    public boolean isUserName(String vibrateName) {
        mPrefsUserPatternMap = mVibrateUserPatternPref.getAll();
        for (Map.Entry<String, ?> entry : mPrefsUserPatternMap.entrySet())
        {
            //Log.d(TAG,"[isUserName] getkey : " + entry.getKey().toString());
            if (vibrateName.equals(entry.getKey().toString())) {
                Log.d(TAG, "[isUserName] matching vibrate - " + entry.getKey().toString());
                return true;
            }
        }
        return false;
    }

    private String getLGPattern(String vibrateName) {
        mPrefsLGPatternMap = mVibrateLGPatternPref.getAll();
        for (Map.Entry<String, ?> entry : mPrefsLGPatternMap.entrySet())
        {
            //Log.d(TAG,"[getLGpattern] getkey : " + entry.getKey().toString());
            //Log.d(TAG,"[getLGpattern] getvalue : " + ((String)entry.getValue()).toString());
            if (vibrateName.equals(entry.getKey().toString())) {
                Log.d(TAG, "[getLGpattern] matching vibrate - " + entry.getKey().toString());
                return ((String)entry.getValue()).toString();
            }
        }
        return EMPTY;
    }

    public int getUserPatternCount() {
        mPrefsUserPatternMap = mVibrateUserPatternPref.getAll();
        return mPrefsUserPatternMap.size();
    }

    public String getUserPattern(String vibrateName) {
        mPrefsUserPatternMap = mVibrateUserPatternPref.getAll();
        for (Map.Entry<String, ?> entry : mPrefsUserPatternMap.entrySet()) {
            if (vibrateName.equals(entry.getKey().toString())) {
                Log.d(TAG, "[getUserPattern] matching vibrate - " + entry.getKey().toString());
                return ((String)entry.getValue()).toString();
            }
        }
        return EMPTY;
    }

    public boolean isLGPattern(String pattern) {
        mPrefsLGPatternMap = mVibrateLGPatternPref.getAll();
        for (Map.Entry<String, ?> entry : mPrefsLGPatternMap.entrySet())
        {
            //Log.d(TAG,"[isLGPattern] getkey : " + entry.getKey().toString());
            //Log.d(TAG,"[isLGPattern] getvalue : " + ((String)entry.getValue()).toString());
            if (pattern.equals(entry.getValue().toString())) {
                Log.d(TAG, "[isLGPattern] matching vibrate - " + entry.getValue().toString());
                return true;
            }
        }
        return false;
    }

    public boolean isUserPattern(String pattern) {
        mPrefsUserPatternMap = mVibrateUserPatternPref.getAll();
        for (Map.Entry<String, ?> entry : mPrefsUserPatternMap.entrySet())
        {
            //Log.d(TAG,"[isUserPattern] getkey : " + entry.getKey().toString());
            //Log.d(TAG,"[isUserPattern] getvalue : " + ((String)entry.getValue()).toString());
            if (pattern.equals(entry.getValue().toString())) {
                Log.d(TAG, "[isUserPattern] matching vibrate - " + entry.getValue().toString());
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getAllpatternName() {
        ArrayList<String> patternList_All = new ArrayList<String>();

        ArrayList<String> patternList_LG = getAllPatternName_LG();
        ArrayList<String> patternList_User = getAllPatternName_User();

        if (isUserPatternEmply()) {
            patternList_All.addAll(patternList_LG);
        }
        else {
            patternList_All.add(mContext.getResources().getString(R.string.lg_vibrations_category));
            if (null != patternList_User) {
                patternList_All.addAll(patternList_User);
            }
            patternList_All.add(mContext.getResources().getString(R.string.my_vibrations_category));
            patternList_All.addAll(patternList_LG);
        }

        return patternList_All;
    }

    public ArrayList<String> getAllPatternName_LG() {
        ArrayList<String> patternNameList_LG = new ArrayList<String>();
        mPrefsLGPatternMap = mVibrateLGPatternPref.getAll();
        for (Map.Entry<String, ?> entry : mPrefsLGPatternMap.entrySet())
        {
            patternNameList_LG.add(entry.getKey().toString());
        }
        Collections.sort(patternNameList_LG);
        return patternNameList_LG;
    }

    public ArrayList<String> getAllPatternName_User() {
        if (Utils.isSPRModel()) {
            if (mUserPatternManager != null) {
                return mUserPatternManager.getAllUserPatternName();
            }
        } else {
            ArrayList<String> patternNameList_User = new ArrayList<String>();
            mPrefsUserPatternMap = mVibrateUserPatternPref.getAll();
            for (Map.Entry<String, ?> entry : mPrefsUserPatternMap.entrySet())
            {
                patternNameList_User.add(entry.getKey().toString());
            }
            Collections.sort(patternNameList_User);
            return patternNameList_User;
        }
        return null;
    }

    public long[] patternTokenizer(String pattern) {
        pattern = removeToken(pattern);
        StringTokenizer stk = new StringTokenizer(pattern, ",");
        Log.d(TAG, "[token] token count :" + stk.countTokens());
        long[] vibratePattern = new long[stk.countTokens()];
        int i = 0;
        while (stk.hasMoreTokens()) {
            vibratePattern[i] = Long.parseLong(stk.nextToken());
            i++;
        }
        return vibratePattern;
    }

    private String removeToken(String pattern) {
        pattern = pattern.replace(" ", "");
        return pattern;
    }

    public void saveSelectVibrate(int parent_type, String name, String pattern) {
        setDBVibrateName(parent_type, name);
        setDBVibratePattern(parent_type, pattern);
    }

    public void saveSelectVibrate(int parent_type, String name, String pattern, int position) {
        if (isUserName(name)) {
            setVibrateType(parent_type, -1, name, pattern);
        } else {
            int pos = getPositionbyName(parent_type, name);
            setVibrateType(parent_type, pos, name, pattern);
        }
    }

    private int getPositionbyName(int parent_type, String name) {
        for (int i = 0; i < vibrate_type_entry.length; i++) {
            if (vibrate_type_entry[i].equals(name)) {
                return i;
            }
        }
        return getDefaultVibrateType(parent_type);
    }

    public String getOriginalDefaultVibrate() {
        return mOriginalDefaultVibrate;
    }

    public String getOriginalDefaultPattern() {
        return mOriginalDefaultPattern;
    }

    public void setOriginalDefaultVibrate(String value) {
        mOriginalDefaultVibrate = value;
    }

    public void setOriginalDefaultPattern(String value) {
        mOriginalDefaultPattern = value;
    }

    private void setVibrateType(int parent_type, int type, String name, String pattern) {
        //Log.d("hkk","[parent_type:" + parent_type + "]"+"-"+type);
        if (type == -1) //userpattern
        {
            if (mUserPatternManager != null) {
                mUserPatternManager.updateVibrateUserPatternDefault(name, parent_type, 1);
                Settings.System.putInt(mContext.getContentResolver(),
                        VIBRATE_NAME_SPR[parent_type], type);
            }
        }
        else if (true == typeIntegrityCheck(type + 1)) {
            Settings.System.putInt(mContext.getContentResolver(), VIBRATE_NAME_SPR[parent_type],
                    type + 1);
            setDBVibrateName(parent_type, vibrate_type_entry[type]);
            setDBVibratePattern(parent_type, getVibratePatterns(type));
        }
        else {
            //Log.e(TAG,"[parent_type:" + parent_type + "]"+"- bad value"+type);
        }
    }

    public void setDBVibrateName(int parent_type, String name) {
        Settings.System.putString(mContext.getContentResolver(), VIBRATE_NAME[parent_type], name);
    }

    public void setDBVibratePattern(int parent_type, String pattern) {
        Settings.System.putString(mContext.getContentResolver(), VIBRATE_PATTERN[parent_type],
                pattern);
    }

    public String getDBVibrateName(int parent_type) {
        if (Utils.isSPRModel()) {
            int type = getVibrateType(parent_type);
            String name = null;
            if (type != -1) {
                name = vibrate_type_entry[type];
            } else {
                if (mUserPatternManager != null) {
                    name = mUserPatternManager.queryUserPatternDefaultName(parent_type);
                }
            }

            if (null == name) {
                //Log.d(TAG, "getDBVibrateName() name null!!");
                setDBVibrateName(parent_type,
                        vibrate_type_entry[getDefaultVibrateType(parent_type)]);
                setDBVibratePattern(parent_type,
                        vibrate_type_value[getDefaultVibrateType(parent_type)]);
                setOriginalDefaultVibrate(vibrate_type_entry[getDefaultVibrateType(parent_type)]);
                setOriginalDefaultPattern(vibrate_type_value[getDefaultVibrateType(parent_type)]);
                Log.d(TAG, "getDBVibrateName() name : null -> "
                        + vibrate_type_entry[getDefaultVibrateType(parent_type)]);
                return vibrate_type_entry[getDefaultVibrateType(parent_type)];
            }
            Log.d(TAG, "getDBVibrateName() : " + name);
            return name;
        }
        else {
            String name = null;
            String pattern = getDBVibratePattern(parent_type);
            mPrefsUserPatternMap = mVibrateUserPatternPref.getAll();
            for (Map.Entry<String, ?> entry : mPrefsUserPatternMap.entrySet())
            {
                //Log.d(TAG,"[isUserName] getkey : " + entry.getKey().toString());
                if (pattern.equals(entry.getValue().toString())) {
                    Log.d(TAG, "[getDBVibrateName]user vibrate:" + entry.getKey().toString());
                    name = entry.getKey().toString();
                    break;
                }
            }
            mPrefsLGPatternMap = mVibrateLGPatternPref.getAll();
            for (Map.Entry<String, ?> entry : mPrefsLGPatternMap.entrySet())
            {
                if (pattern.equals(entry.getValue().toString())) {
                    Log.d(TAG, "[getDBVibrateName]lg vibrate:" + entry.getKey().toString());
                    name = entry.getKey().toString();
                    break;
                }
            }

            if (null == name) {
                setDBVibrateName(parent_type,
                        mContext.getResources().getString(R.string.vibrate_pattern_standard));
                setDBVibratePattern(parent_type, removeToken(VIBRATE_TYPE[2]));
                setOriginalDefaultVibrate(mContext.getResources().getString(
                            R.string.vibrate_pattern_standard));
                setOriginalDefaultPattern(removeToken(VIBRATE_TYPE[2]));
                Log.d(TAG, "getDBVibrateName() name : null -> "
                        + mContext.getResources().getString(R.string.vibrate_pattern_standard));
                return mContext.getResources().getString(R.string.vibrate_pattern_standard);
            }
            Log.d(TAG, "getDBVibrateName() : " + name);
            return name;
        }
    }

    public String getDBVibratePattern(int parent_type) {
        if (Utils.isSPRModel()) {
            int type = getVibrateType(parent_type);
            String pattern = null;
            if (type != -1) {
                pattern = getVibratePatterns(type);
            }
            else { //userpattern
                if (mUserPatternManager != null) {
                    pattern = mUserPatternManager.queryUserPatternDefaultPattern(parent_type);
                }
                if (pattern == null) {
                    pattern = getVibratePatterns(type);
                }
            }
            return removeToken(pattern);
        }
        else {

            String pattern = Settings.System.getString(mContext.getContentResolver(),
                    VIBRATE_PATTERN[parent_type]);
            if (null == pattern) {
                Log.d(TAG, "getDBVibratePattern() name null!!");
                setDBVibratePattern(parent_type, removeToken(VIBRATE_TYPE[2]));
                return removeToken(VIBRATE_TYPE[2]);
            }
            Log.d(TAG, "getDBVibratePattern() : type=" + parent_type);
            return removeToken(pattern);
        }
    }

    public void setDBMyVibrationCount(int count) {
        Settings.System.putInt(mContext.getContentResolver(), USER_VIBRATION_COUNT, count);
    }

    public int getDBMyVibrationCount() {
        try {
            return Settings.System.getInt(mContext.getContentResolver(), USER_VIBRATION_COUNT);
        } catch (SettingNotFoundException e) {
            setDBMyVibrationCount(0);
            return 0;
        }
    }

    public String getDBMyVibrationCountString() {
        return String.format("%05d", getDBMyVibrationCount() + 1);
    }

    public boolean isAllSpace(String name) {
        int length = name.length();
        boolean isAllSpace = true;
        for (int i = 0; i < length; i++) {
            if (name.charAt(i) == ' ') {
                isAllSpace = true;
            }
            else {
                isAllSpace = false;
            }
        }
        return isAllSpace;
    }

    public boolean iskeyBlank(String saveName) {
        return "".equals(saveName) ? true : false;
    }

    public int[] getVolume(long[] pattern) {
//        Vibrator mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
        int volume = 0; //mVibrator.getVibrateVolume(Vibrator.VIBRATE_TYPE_RING); // yonguk.kim JB+ migration 20130130
        int[] type_ring = new int[pattern.length];
        if (pattern.length > 0) {
            int length = pattern.length;
            int i = 0;
            for (i = 0; i < length; i++) {
                type_ring[i] = volume;
            }
        }
        return type_ring;

    }

    public String getTypeDefaultName(int type) {

        return getDBVibrateName(type);
    }

    public String getTypeDefaultPattern(int type) {

        return getDBVibratePattern(type);
    }

    public void renameVibrateNameOthers(String oldName, String newName) {
        for (int i = 0; i < VIBRATE_NAME.length; i++) {
            if (oldName.equals(getDBVibrateName(i))) {
                setDBVibrateName(i, newName);
            }
        }
    }

    public void removeVibrateNameOthers(String vibrateName) {
        for (int i = 0; i < VIBRATE_NAME.length; i++) {
            if (vibrateName.equals(getDBVibrateName(i))) {
                if (Utils.isSPRModel()) {
                    setDBVibrateName(i, vibrate_type_entry[getDefaultVibrateType(i)]);
                    setDBVibratePattern(i, vibrate_type_value[getDefaultVibrateType(i)]);
                    setOriginalDefaultVibrate(vibrate_type_entry[getDefaultVibrateType(i)]);
                    setOriginalDefaultPattern(vibrate_type_value[getDefaultVibrateType(i)]);
                } else {
                    setDBVibrateName(i,
                            mContext.getResources().getString(R.string.vibrate_pattern_standard));
                    setDBVibratePattern(i, removeToken(VIBRATE_TYPE[2]));
                    /*setOriginalDefaultVibrate(mContext.getResources().getString(
                            R.string.vibrate_pattern_standard));
                    setOriginalDefaultPattern(removeToken(VIBRATE_TYPE[2]));
                    */
                }
            }
        }
    }

    public int sumVibrateTime(long[] pattern) {
        long result = 0;
        long length = pattern.length;
        for (int i = 0; i < length; i++) {
            result = result + pattern[i];
        }
        return (int)result;
    }

    public void hapticFeedbackOff(long[] pattern) {
        if (null != timer) {
            timer.cancel();
        }
        if (null != task) {
            task.cancel();
        }

        task = new TimerTask() {
            public void run() {
                Settings.System.putInt(mContext.getContentResolver(),
                        Settings.System.HAPTIC_FEEDBACK_ENABLED, mHaptic_value);
            }
        };

        timer = new Timer();

        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
        if (null != timer && null != task) {
            timer.schedule(task, sumVibrateTime(pattern));
        }
    }

    public int getHaptic_value() {
        return mHaptic_value;
    }

    private long[] getLongIntArray(int resid) {

        int[] ar = null;
        long[] out = null;

        try {
            ar = mContext.getResources().getIntArray(resid);
            if (ar == null) {
                return null;
            }
            out = new long[ar.length];
            for (int i = 0; i < ar.length; i++) {
                out[i] = ar[i];
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "[getLongIntArray] NullPointerEception :  " + e.getMessage());
            return out;
        }
        return out;
    }

    private String getVibratePatterns(int vibrateType) {

        int resid = 0;

        switch (vibrateType) {
        case 1:
            resid = R.array.config_distinctiveVibrationType1;
            break;
        case 2:
            resid = R.array.config_distinctiveVibrationType2;
            break;
        case 3:
            resid = R.array.config_distinctiveVibrationType3;
            break;
        case 4:
            resid = R.array.config_distinctiveVibrationType4;
            break;
        case 5:
            resid = R.array.config_distinctiveVibrationType5;
            break;
        case 6:
            resid = R.array.config_distinctiveVibrationType6;
            break;
        case 7:
            resid = R.array.config_distinctiveVibrationType7;
            break;
        default:
            resid = R.array.config_distinctiveVibrationType1;
            break;
        }

        long[] patterns = getLongIntArray(resid);
        String retStr = "";
        if (patterns != null) {
            int length = patterns.length;
            if (length != 0) {
                for (int i = 0; i < length; i++) {
                    retStr += Long.toString(patterns[i]);
                    retStr += ",";
                }
            }
        }
        //Log.d(TAG,"vibrate type="+vibrateType+"     retStr="+retStr);
        return retStr;
    }

    private boolean typeIntegrityCheck(int type) {
        if (type >= 1 && type < 8) {
            return true;
        }
        return false;
    }

    private int getVibrateType(int parent_type) {
        int type = Settings.System.getInt(mContext.getContentResolver(),
                VIBRATE_NAME_SPR[parent_type], 1);
        int temp_type = 1;
        if (type == -1) {
            return type; //userpattern
        }
        if (true == typeIntegrityCheck(type)) {
            return type - 1;
        }
        //Log.e(TAG,"[parent type:" + parent_type + "]"+" "+type);
        temp_type = getDefaultVibrateType(parent_type);
        return temp_type;
    }

    private int getDefaultVibrateType(int parent_type) {
        if (parent_type == INCOMING_CALL_SIM1) {
            return 0;
        }
        else if (parent_type == INCOMING_CALL_SIM2) {
            return 0;
        }
        else if (parent_type == INCOMING_CALL_SIM3) {
            return 0;
        }
        else if (parent_type == MESSAGE) {
            return 2;
        }
        else if (parent_type == ALARM) {
            return 3;
        }
        else if (parent_type == CALENDAR) {
            return 4;
        }
        else if (parent_type == EMAIL) {
            return 1;
        }
        return 0;
    }

    public void reSetVibratePattern() {
        removeVibrateInfo();
        setDBMyVibrationCount(DEFAULT_VIRBATE_COUNT);
        for (int i = 0; i < VibratePatternInfo.VIBRATE_NAME.length; i++) {
            setDBVibrateName(
                    i,
                    mContext.getResources().getString(
                            R.string.vibrate_pattern_standard));
            setDBVibratePattern(i, removeToken(
                    VibratePatternInfo.VIBRATE_TYPE[DEFAULT_VIBRATE_PATTERN_NUM]));
        }
    }

    public int getColorForResName(Context context, String pkgName, String Name) {
        Context con = null;
        int id = 0;
        try {
            con = context.createPackageContext(pkgName, Context.MODE_PRIVATE);
            Resources res = null;
            if (null != con && null != con.getResources()) {
                res = con.getResources();
                id = res.getIdentifier(Name, "color", pkgName);
                return Color.parseColor(con.getText(id).toString());
            }
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "NameNotFoundException : " + e.getMessage());
            return NOT_FONUD;
        } catch (NotFoundException e) {
            Log.e(TAG, "NotFoundException : " + e.getMessage());
            return NOT_FONUD;
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException : " + e.getMessage());
            return NOT_FONUD;
        }
        return 0;
    }
}
