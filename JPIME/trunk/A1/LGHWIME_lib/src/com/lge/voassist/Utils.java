package com.lge.voassist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.lge.handwritingime.HandwritingKeyboard;

public class Utils {
    private final static String TAG = "voassist.Utils";
    private final static boolean DEBUG = HandwritingKeyboard.DEBUG;

    private final static int NUMBER_MIN_VALUE = 0x0030;
    private final static int NUMBER_MAX_VALUE = 0x0039;
    private final static int ENGLISH_UPPER_MIN_VALUE = 0x0041;
    private final static int ENGLISH_UPPER_MAX_VALUE = 0x005a;
    private final static int ENGLISH_LOWER_MIN_VALUE = 0x0061;
    private final static int ENGLISH_LOWER_MAX_VALUE = 0x007a;
    private final static int HIRAGANA_MIN_VALUE = 0x3040;
    private final static int HIRAGANA_MAX_VALUE = 0x309f;
    private final static int KATAKANA_MIN_VALUE = 0x30a0;
    private final static int KATAKANA_MAX_VALUE = 0x30ff;
    private final static int RARE_KANJI_MIN_VALUE = 0x3400;
    private final static int RARE_KANJI_MAX_VALUE = 0x4dbf;
    private final static int COMMON_KANJI_MIN_VALUE = 0x4e00;
    private final static int COMMON_KANJI_MAX_VALUE = 0x9faf;
    private final static int KOREAN_SYLLABLES_MIN_VALUE = 0xAC00;
    private final static int KOREAN_SYLLABLES_MAX_VALUE = 0xD7A3;
    private final static int KOREAN_JAMO_MIN_VALUE = 0x1100;
    private final static int KOREAN_JAMO_MAX_VALUE = 0x11FF;
    private final static int KOREAN_WANSUNG_JAMO_MIN_VALUE = 0x3130;
    private final static int KOREAN_WANSUNG_JAMO_MAX_VALUE = 0x318F;
    private final static int KOREAN_JAMO_EXPAND_A_MIN_VALUE = 0xA960;
    private final static int KOREAN_JAMO_EXPAND_A_MAX_VALUE = 0xA97F;
    private final static int KOREAN_JAMO_EXPAND_B_MIN_VALUE = 0xD7B0;
    private final static int KOREAN_JAMO_EXPAND_B_MAX_VALUE = 0xD7FF;

    private static Integer mSymbolSingle[] = { 0x00B0, 0x00B1, 0x00B5, 0x00D7, 0x00F7, 0x2022, 0x203B, 0x20AC, 0xFFE6,
            0x3001, 0x3002, 0x30FB, 0x4EDD, 0x3016, 0x3017, 0x3012, 0x2190, 0x2192, 0x21B3, 0x21D0, 0x21D2, 0x25B3,
            0x25CB, 0x2606, 0x2713 };

    private static Integer mCharTypeRange[][] = { { RARE_KANJI_MIN_VALUE, RARE_KANJI_MAX_VALUE },
            { COMMON_KANJI_MIN_VALUE, COMMON_KANJI_MAX_VALUE }, { HIRAGANA_MIN_VALUE, HIRAGANA_MAX_VALUE },
            { KATAKANA_MIN_VALUE, KATAKANA_MAX_VALUE }, { ENGLISH_UPPER_MIN_VALUE, ENGLISH_UPPER_MAX_VALUE },
            { ENGLISH_LOWER_MIN_VALUE, ENGLISH_LOWER_MAX_VALUE }, { NUMBER_MIN_VALUE, NUMBER_MAX_VALUE },
            { KOREAN_SYLLABLES_MIN_VALUE, KOREAN_SYLLABLES_MAX_VALUE },
            { KOREAN_JAMO_MIN_VALUE, KOREAN_WANSUNG_JAMO_MAX_VALUE },
            { KOREAN_WANSUNG_JAMO_MIN_VALUE, KOREAN_JAMO_MAX_VALUE },
            { KOREAN_JAMO_EXPAND_A_MIN_VALUE, KOREAN_JAMO_EXPAND_A_MAX_VALUE },
            { KOREAN_JAMO_EXPAND_B_MIN_VALUE, KOREAN_JAMO_EXPAND_B_MAX_VALUE } };

    // range include space
    private static Integer mCharSymbolRange[][] = { { 0x0020, 0x002F }, { 0x003A, 0x0040 }, { 0x005B, 0x0060 },
            { 0x007B, 0x007E }, { 0x00A2, 0x00A7 }, { 0x3005, 0x300D }, { 0x2474, 0x247C }, { 0x249C, 0x24B5 },
            { 0x3220, 0x3229 } };

    public static enum CHAR_TYPE {
        KANJI, HIRAGANA, KATAKANA, ENGLISH, NUMBER, KOREAN, SYMBOL, OTHER
    }

    public static void unsplitResources(Context context) throws IOException {
        // list asset resources
        AssetManager manager = context.getAssets();
        File path = context.getDir("data", 0);

        // HW resource
        File destDir = new File(path.getAbsolutePath() + "/resources");
        if(!destDir.exists()) {
            destDir.mkdir();
        }
        if (DEBUG) Log.d(TAG, "baseDir=" + destDir);

        String[] resources = new String[] { "resources/ja_JP", "resources/ko_KR", "resources/mul" };
        String[][] resource_files = new String[][] {
                {
                    "ja_JP_hiragana-sk.res",
                    "ja_JP_jisx0208-ak-cur.lite.res",
                    "ja_JP_jisx0208-ak-iso.lite.res",
                    "ja_JP_jisx0208-lk-text.lite.res",
                    "ja_JP_kanji-sk.res",
                    "ja_JP_katakana-sk.res",
                    "ja_JP_latin-sk.res",
                    "ja_JP_symbol-sk.res"
                }, {
                    "ko_KR_wansung-ak-cur.lite.res",
                    "ko_KR_wansung-ak-iso.lite.res",
                    "wansung_jamo_digit_symbol.res"
                }, {
                    "mul-lk-number.res"
                }
        };
        
        String[] files = null;
        for (int i = 0; i < resources.length; i++) {
            String resource = resources[i];
            files = resource_files[i];

            destDir = new File(path.getAbsolutePath() + "/" + resource);
            if (!destDir.exists()) {
                destDir.mkdir();
            }
            if (DEBUG) Log.d(TAG, "destDir=" + destDir);
            for (String lang : files) {
                File complete = new File(destDir.getAbsolutePath(), lang);
                if (!complete.exists()) {
                    File source = new File(resource + "/" + lang);
                    File destination = complete;
                    simulateUnsplitResource(manager, source, destination);
                } else if (DEBUG) {
                    Log.d(TAG, ">> already exists " + complete.getName());
                }
            }
        }
        
        
//        String[] files = null;        
//        for (String resource : resources) {
//            files = manager.list(resource);
//            
//            destDir = new File(path.getAbsolutePath() + "/" + resource);
//            if(!destDir.exists()) {
//                destDir.mkdir();
//            }
//            if (DEBUG) Log.d(TAG, "destDir=" + destDir);
//            for (String lang : files) {
//                File complete = new File(destDir.getAbsolutePath(), lang);
//                if (!complete.exists()) {
//                    File source = new File(resource + "/" + lang);
//                    File destination = complete;
//                    simulateUnsplitResource(manager, source, destination);
//                } else if (DEBUG) {
//                    Log.d(TAG, ">> already exists " + complete.getName());
//                }
//            }
//        }
        
    


        // data folder
//        files = manager.list("conf");
        destDir = new File(path.getAbsolutePath() + "/conf");
        if(!destDir.exists()) {
            destDir.mkdir();
        }
        if (DEBUG) Log.d(TAG, "destDir=" + destDir);

        files = new String[] {
                "Engine.properties",
                "ja-JP.lang",
                "ko-KR.lang",
                "LanguageManager.properties",
                "Recognizer.properties"
        };
        
        for (String conf : files) {
            File complete = new File(destDir.getAbsolutePath(), conf);
            if (!complete.exists()) {
                File source = new File("conf/" + conf);
                File destination = complete;
                simulateUnsplitResource(manager, source, destination);
            } else if (DEBUG) {
                Log.d(TAG, "already exists " + complete.getName());
            }
        }
    }

    /**
     * Simulate a merge resource
     * 
     * @param context
     *            the application context
     * @param source
     *            the resource file to merge
     * @param destination
     *            the merged file destination
     * @param language
     *            the resource language
     * @param suffix
     *            ak or lk file suffix
     */
    private static void simulateUnsplitResource(AssetManager manager, File source, File destination) throws IOException {
        if (DEBUG)
            Log.d(TAG, "creating " + source.toString());
        OutputStream os = null;
        InputStream is = null;
        try {
            os = new FileOutputStream(destination);
            is = manager.open(source.getPath());
            destination.createNewFile();
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = is.read(bytes)) != -1)
                os.write(bytes, 0, read);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "There's no file like " + source.getPath(), e);
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    private static boolean isBetween(int x, int min, int max) {
        return min <= x && x <= max;
    }

    private static int getCharTypeNum(int charToUnicode) {
        for (int i = 0; i < mCharTypeRange.length; i++) {
            if (isBetween(charToUnicode, mCharTypeRange[i][0], mCharTypeRange[i][1])) {
                return i;
            }
        }
        return mCharTypeRange.length + 1;
    }

    public static CHAR_TYPE getCharType(String str, int index) {
        return getCharType(str.charAt(index));
    }

    public static CHAR_TYPE getCharType(char jChar) {

        int charToUnicode = Integer.valueOf(jChar);
        switch (getCharTypeNum(charToUnicode)) {
        case 0:
        case 1:
            return CHAR_TYPE.KANJI;
        case 2:
            return CHAR_TYPE.HIRAGANA;
        case 3:
            return CHAR_TYPE.KATAKANA;
        case 4:
        case 5:
            return CHAR_TYPE.ENGLISH;
        case 6:
            return CHAR_TYPE.NUMBER;
        case 7:
        case 8:
        case 9:
        case 10:
        case 11:
            return CHAR_TYPE.KOREAN;
        default:
            boolean bFounded = false;
            for (int i = 0; i < mSymbolSingle.length; i++) {
                if (mSymbolSingle[i] == charToUnicode) {
                    bFounded = true;
                    break;
                }
            }
            if (!bFounded) {
                for (int i = 0; i < mCharSymbolRange.length; i++) {
                    if (isBetween(charToUnicode, mCharSymbolRange[i][0], mCharSymbolRange[i][1])) {
                        bFounded = true;
                        break;
                    }
                }
            }
            if (bFounded)
                return CHAR_TYPE.SYMBOL;
            else
                return CHAR_TYPE.OTHER;
        }
    }
}
