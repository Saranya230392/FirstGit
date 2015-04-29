/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml.iwnn;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
//for debug
import android.util.Log;

import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.DecoEmojiAttrInfo;
import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.IDecoEmojiConstant;
import jp.co.omronsoft.iwnnime.ml.ComposingText;
import jp.co.omronsoft.iwnnime.ml.ControlPanelStandard;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiOperation;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiOperationQueue;
import jp.co.omronsoft.iwnnime.ml.DownloadDictionaryPreferenceActivity;
import jp.co.omronsoft.iwnnime.ml.StrSegment;
import jp.co.omronsoft.iwnnime.ml.WebAPIWnnEngine;
import jp.co.omronsoft.iwnnime.ml.WnnEngine;
import jp.co.omronsoft.iwnnime.ml.WnnWord;
import jp.co.omronsoft.iwnnime.ml.OpenWnn;
import jp.co.omronsoft.iwnnime.ml.KeyboardLanguagePackData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

/**
 * iWnn IM Class iWnnEngine
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class iWnnEngine implements WnnEngine {
    /** Type of Language */
    public static final class LanguageType {
        /** None */
        public static final int NONE = -1;
        /** Japanese */
        public static final int JAPANESE = 0;
        /** English */
        public static final int ENGLISH = 1;
        /** German */
        public static final int GERMAN = 2;
        /** English US */
        public static final int ENGLISH_US = 3;
        /** English UK */
        public static final int ENGLISH_UK = 4;
        /** Italian */
        public static final int ITALIAN = 5;
        /** French */
        public static final int FRENCH = 6;
        /** Spanish */
        public static final int SPANISH = 7;
        /** Dutch */
        public static final int DUTCH = 8;
        /** Polish */
        public static final int POLISH = 9;
        /** Russian */
        public static final int RUSSIAN = 10;
        /** Swedish */
        public static final int SWEDISH = 11;
        /** Norwegian Bokmal */
        public static final int NORWEGIAN_BOKMAL = 12;
        /** Czech */
        public static final int CZECH = 13;
        /** Chinese (PRC) */
        public static final int SIMPLIFIED_CHINESE = 14;    //WLF_LANG_TYPE_ZHCN
        /** Chinese (TW) */
        public static final int TRADITIONAL_CHINESE = 15;   //WLF_LANG_TYPE_ZHTW
        /** Portuguese */
        public static final int PORTUGUESE = 16;
        /** French (Canada) */
        public static final int CANADA_FRENCH = 17;
        /** Korean */
        public static final int KOREAN = 18; //WLF_LANG_TYPE_KOREAN
        /** Count of LanguageType */
        public static final int COUNT_OF_LANGUAGETYPE = 19;
        /**
         * Default constructor
         */
        public LanguageType() {
            super();
        }
    }

    /** Language Configuration FilePath */
    private static final String[] CONF_TABLE = {
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_ja_JP.conf.so",     //0
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_en_USUK.conf.so",   //1
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_de_DE.conf.so",     //2
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_en_US.conf.so",     //3
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_en_UK.conf.so",     //4
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_it_IT.conf.so",     //5
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_fr_FR.conf.so",     //6
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_es_ES.conf.so",     //7
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_nl_NL.conf.so",     //8
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_pl_PL.conf.so",     //9
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_ru_RU.conf.so",     //10
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_sv_SE.conf.so",     //11
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_nb_NO.conf.so",     //12
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_cs_CZ.conf.so",     //13
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_zh_CN.conf.so",     //14
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_zh_TW.conf.so",     //15
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_pt_PT.conf.so",     //16
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_fr_CA.conf.so",     //17
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_ko_KR.conf.so"      //18
    };

    /** Language Configuration Tablet FilePath */
    private static final String[] CONF_TABLET_TABLE = {
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_ja_JP.conf.so",              //0
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_en_tablet_USUK.conf.so",     //1
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_de_DE.conf.so",              //2
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_en_US.conf.so",              //3
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_en_UK.conf.so",              //4
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_it_IT.conf.so",              //5
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_fr_FR.conf.so",              //6
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_es_ES.conf.so",              //7
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_nl_NL.conf.so",              //8
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_pl_PL.conf.so",              //9
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_ru_RU.conf.so",              //10
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_sv_SE.conf.so",              //11
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_nb_NO.conf.so",              //12
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_cs_CZ.conf.so",              //13
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_zh_CN.conf.so",              //14
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_zh_TW.conf.so",              //15
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_pt_PT.conf.so",              //16
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_fr_CA.conf.so",              //17
        "/data/data/jp.co.omronsoft.iwnnime.ml/lib/lib_dic_ko_KR.conf.so"               //18
    };

    /** Type of Dictionary (Set No) */
    public static final class SetType {
        /** None */
        public static final int NONE = -1;
        /** Normal */
        public static final int NORMAL = 0;
        /** Alphanumeric JapaneseKANA */
        public static final int EISUKANA = 1;
        /** Face Mark */
        public static final int KAOMOJI = 2;
        /** Person's Name */
        public static final int JINMEI = 3;
        /** Postal Address */
        public static final int POSTAL_ADDRESS = 4;
        /** EMail Address */
        public static final int EMAIL_ADDRESS = 5;
        /** User Dictionary */
        public static final int USERDIC = 10;
        /** Learning Dictionary */
        public static final int LEARNDIC = 11;
        /** Person's Name User Dictionary */
        public static final int USERDIC_NAME = 12;
        /** EMail User Dictionary */
        public static final int USERDIC_EMAIL = 13;
        /** Phone User Dictionary */
        public static final int USERDIC_PHONE = 14;
        /** Additional Dictionary */
        public static final int ADDITIONALDIC = 35;
        /** Additional Dictionary */
        public static final int AUTOLEARNINGDIC = 45;
        /** Download Dictionary */
        public static final int DOWNLOADDIC = 46;
        /** Dictionary Set Type Max */
        public static final int DICTIONARY_TYPE_MAX = 57;//WLF_DICMODE_MAX+1
        /**
         * Default constructor
         */
        public SetType() {
            super();
        }
    }

    /** To get Type of Language from Type of Dictionary (Unused) */
    public static final int IWNNIME_DIC_LANG_DEF = 100;

    /** Type of Dictionary (Delete Dictionary)(Learning Dictionary) */
    public static final int LEARN_DICTIONARY_DELETE = 1;
    /** Type of Dictionary (Delete Dictionary)(User Dictionary) */
    public static final int USER_DICTIONARY_DELETE = 2;
    /** Type of Dictionary (Delete Dictionary)(error) */
    public static final int DICTIONARY_DELETE_FAILURE = -1;

    /** Candidate Max */
    public static final int CANDIDATE_MAX = 350;

    /** EngineServiceConnect Max */
    public static final int SERVICE_CONNECT_MAX = 3;

    /** Service configuration file max */
    public static final int SERVICE_CONFIGURATION_FILE_MAX = 3;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is history. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_HISTORY = 0x0001;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is possible to delete a word. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_DELETABLE = 0x0002;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is non-conversion learning. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_MUHENKAN = 0x0004;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is symbol. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_SYMBOL = 0x0008;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is symbolList. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_SYMBOLLIST = 0x0010;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is latin-pseudo. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_LATIN_GIJI = 0x0020;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is no dictionary. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_NO_DICTIONARY = 0x0040;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is Japanese-Pseudo. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_JAPANESE_QWERTY_GIJI = 0x0080;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is non-conversion lowercase learning. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_MUHENKAN_LOWERCASE = 0x0100;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is WebAPI Button. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_WEBAPI = 0x0200;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is WebAPI Word. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_WEBAPI_WORD = 0x0400;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is No CANDIDATE Button. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_NO_CANDIDATE = 0x0800;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is a word specifying stroke and candidate through the service. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_SERVICE_WORD_NO_ENGINE = 0x1000;

     /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether this word is following the previous word. */
    public static final int WNNWORD_ATTRIBUTE_CONNECTED = 0x2000;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is WebAPI Word get again. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_WEBAPI_GET_AGAIN = 0x4000;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is JoJo Word. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_JOJO_WORD = 0x10000;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether this word is target for learn dictionary. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_TARGET_LEARN = 0x8000;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is deco emoji. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_DECOEMOJI = 0x01000000;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is previous button. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_PREV_BUTTON = 0x02000000;

    /** Special attribute of word(Constant for WnnWord.attribute),
        This attribute indicates whether it is next button. Field Length: 1 bit */
    public static final int WNNWORD_ATTRIBUTE_NEXT_BUTTON = 0x04000000;

    /** Attribute of NJ_WORD_INFO,
        This attribute indicates whether it is JoJo Word. Field Length: 1 bit Bit position: LSB 25bit */
    private static final int NJ_WORD_INFO_STEM_ATTRIBUTE_JOJO_WORD = 0x01000000;

    /*
     * Converter Type
     */
    public static final int CONVERT_TYPE_NONE = 1;
    public static final int CONVERT_TYPE_HIRAGANA = 2;
    public static final int CONVERT_TYPE_KATAKANA = 3;
    public static final int CONVERT_TYPE_HANKATA  = 4;
    public static final int CONVERT_TYPE_HAN_EIJI_CAP = 5;
    public static final int CONVERT_TYPE_ZEN_EIJI_CAP = 6;
    public static final int CONVERT_TYPE_HAN_EIJI_UPPER = 7;
    public static final int CONVERT_TYPE_ZEN_EIJI_UPPER = 8;
    public static final int CONVERT_TYPE_HAN_EIJI_LOWER = 9;
    public static final int CONVERT_TYPE_ZEN_EIJI_LOWER = 10;

    /** Maximum length of input string */
    private static final int LIMIT_INPUT_NUMBER = 50;

    /** Type of Dictionary (Add Word)*/
    public static final class AddWordDictionaryType {
        /** User Dictionary */
        public static final int ADD_WORD_DICTIONARY_TYPE_USER = 0;
        /** Learning Dictionary */
        public static final int ADD_WORD_DICTIONARY_TYPE_LEARNING = 1;
        /** Pseudo (GIJI) Dictionary */
        public static final int ADD_WORD_DICTINARY_TYPE_PROGRAM = 2;
        /**
         * Default constructor
         */
        public AddWordDictionaryType() {
            super();
        }
    }

    /** Flexible Search Type */
    public static final class FlexibleSearchType {
        /** Flexible Search OFF */
        public static final int FLEXIBLE_SEARCH_OFF = 0;
        /** Flexible Search ON */
        public static final int FLEXIBLE_SEARCH_ON = 1;
        /**
         * Default constructor
         */
        public FlexibleSearchType() {
            super();
        }
    }

    /** Key board Type */
    public static final class KeyboardType {
        /** Key None */
        public static final int KEY_TYPE_NONE = 255;
        /** 12 Key Pad */
        public static final int KEY_TYPE_KEYPAD12 = 0;
        /** Qwerty Key board */
        public static final int KEY_TYPE_QWERTY = 1;
        /**
         * Default constructor
         */
        public KeyboardType() {
            super();
        }
    }

    /** Search strategy on dictionary */
    public static final class SearchMethod {
        /** Forward lookup Exact phrase search */
        public static final int SEARCH_ORIGINAL_PULL_PERFECTION = 0;
        /** Forward lookup prefix search */
        public static final int SEARCH_ORIGINAL_PULL_FRONT = 1;
        /** Connection search */
        public static final int SEARCH_CONNECTION = 2;
        /** Reverse lookup Exact phrase search */
        public static final int SEARCH_REVERSE_PULL_PERFECTION = 3;
        /** Reverse lookup prefix search */
        public static final int SEARCH_REVERSE_PULL_FRONT = 4;
        /**
         * Default constructor
         */
        public SearchMethod() {
            super();
        }
    }

    /** Order of dictionary retrieval */
    public static final class SearchOrder {
        /** Order of frequency */
        public static final int ORDER_FREQUENCY = 0;
        /** Order of reading */
        public static final int ORDER_READING = 1;
        /** Order of registration */
        public static final int ORDER_REGISTRATION = 2;
        /**
         * Default constructor
         */
        public SearchOrder() {
            super();
        }
    }

    /** for DEBUG */
    private static final boolean DEBUG = false;
    /** for DEBUG */
    private static final String TAG = "iWnn";

    /** The language in which the constant is not defined in the "class Locale" is made here. */
    /** Locale Dutch     */
    private static final Locale LOCALE_NL = new Locale("nl", "NL");
    /** Locale Czech     */
    private static final Locale LOCALE_CZ = new Locale("cs", "CZ");
    /** Locale Polish    */
    private static final Locale LOCALE_PL = new Locale("pl", "PL");
    /** Locale Spanish   */
    private static final Locale LOCALE_ES = new Locale("es", "ES");
    /** Locale Swedish   */
    private static final Locale LOCALE_SV = new Locale("sv", "SE");
    /** Locale Russian   */
    private static final Locale LOCALE_RU = new Locale("ru", "RU");
    /** Locale Nowegian  */
    private static final Locale LOCALE_NB = new Locale("nb", "NO");
    /** Locale Portuguese    */
    private static final Locale LOCALE_PT = new Locale("pt", "PT");

    /**
     * Declaration of locate table.
     * <br>
     * [Remarks] It synchronizes with the value of "class LanguageType".
     * Order to tailor KeyboardLanguagePackData#LIST_LOCALE_TABLE
     */
    private static final Locale LOCALE_TABLE[] = {Locale.JAPANESE,/*Japanese  */
                                                    Locale.ENGLISH, /*English   */
                                                    Locale.GERMAN,  /*German    */
                                                    Locale.US,      /*English US*/
                                                    Locale.UK,      /*English UK*/
                                                    Locale.ITALIAN, /*Italian   */
                                                    Locale.FRENCH,  /*French    */
                                                    LOCALE_ES,      /*Spanish   */
                                                    LOCALE_NL,      /*Dutch     */
                                                    LOCALE_PL,      /*Polish    */
                                                    LOCALE_RU ,     /*Russian   */
                                                    LOCALE_SV ,     /*Swedish   */
                                                    LOCALE_NB ,     /*Nowegian  */
                                                    LOCALE_CZ,      /*Czech     */
                                                    Locale.SIMPLIFIED_CHINESE, /*Chinese(PRC) */
                                                    Locale.TRADITIONAL_CHINESE, /*Chinese(TW) */
                                                    LOCALE_PT,      /*Portuguese */
                                                    Locale.CANADA_FRENCH, /*French (Canada) */
                                                    Locale.KOREA    /*Korea           */
    };

    /** Offset from half-width to full-width. */
    private static final int OFFSET_FULL_WIDTH = 0xFEE0;

    /** The instance of {@code iWnnEngine} for Singleton.*/
    private static iWnnEngine mEngine = new iWnnEngine();

    /** The instance of {@code iWnnEngine} for Singleton is used from Service.*/
    private static ArrayList<iWnnEngine> mServiceEngine = new ArrayList<iWnnEngine>();

    /** Service ConnectedName*/
    private String mServiceConnectedName;

    /** Search string */
    private String mSearchKey = null;
    /** Output number */
    private int mOutputNum = 0;
    /** iWnnCore parameter, See {@link jp.co.omronsoft.iwnnime.ml.iwnn.IWnnCore} */
    private IWnnCore mCore = null;
    /** Segment position */
    private int mSegment = 0;
    /** Segment count */
    private int mSegmentCount = 0;
    /** Search count */
    private int mSearchCnt = 0;
    /** Candidate table, See {@link java.util.HashMap} */
    private HashMap<String, WnnWord> mCandTable = null;
    /** Composing text for search, See {@link jp.co.omronsoft.iwnnime.ml.ComposingText} */
    private ComposingText mSearchComposingText = null;

    /** Pseudo candidates */
    private String[] mCaseGijiList = null;

    /** handwriting candidates */
    protected String[] mHCandidatesList = null;

    /** Index value of pseudo candidates */
    private int mCaseGijiListIndex;

    /** When pseudo candidate is necessary [true]*/
    private boolean mIsRequestGiji = true;

    /** Retrieval character pattern that permits the candidate's repetition */
    private Pattern mAllowDuplicationCharPattern
        // You can use following command to convert ascii to utf8.
        //  $ native2ascii -reverse <file>
        = Pattern.compile(".*[\u3041\u3042\u3043\u3044\u3045\u3046\u3047\u3048\u3049\u304a\u304b\u304c\u304d\u304e\u304f\u3050\u3051\u3052\u3053\u3054\u3055\u3056\u3057\u3058\u3059\u305a\u305b\u305c\u305d\u305e"
                          + "\u305f\u3060\u3061\u3062\u3063\u3064\u3065\u3066\u3067\u3068\u3069\u306a\u306b\u306c\u306d\u306e\u306f\u3070\u3071\u3072\u3073\u3074\u3075\u3076\u3077\u3078\u3079\u307a\u307b"
                          + "\u307c\u307d\u307e\u307f\u3080\u3081\u3082\u3083\u3084\u3085\u3086\u3087\u3088\u3089\u308a\u308b\u308c\u308d\u308e\u308f\u3090\u3091\u3092\u3093].*");

    /** When the candidate's repetition is permitted [true] */
    private boolean mIsForbidDuplication = false;

    /** When multiple phrase or clause kana-kanji conversion is executed [true] */
    private boolean mIsConverting = false;

    /** Judge if it's English or not, See {@link jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine.LatinFilter} */
    private LatinFilter mLatinFilter = new LatinFilter();

    /** Whether pseudo candidate is enabled */
    private boolean mEnableConvertedCandidate = false;

    /** Language type, See the {@link LanguageType} */
    private int mLangType = LanguageType.NONE;

    /** Dictionary type, See the {@link SetType} */
    private int mDictionarySet = SetType.NONE;

    /** Whether sequence of words are broke.  */
    private boolean mHasBroke = true;

    /** The caller of {@link #setDictionary}. */
    private int mCaller;

    /** Whether {@link #searchWords} is called */
    private boolean mHasSearchWords;

    /** WebAPIEEngine */
    private WebAPIWnnEngine mWebAPIWnnEngine;

    /** When WebAPI Words is disp [true] */
    private boolean mDispWebAPIWords;

    /** When WebAPI button is disp [true] */
    private boolean mDispWebAPIButton;

    /** When WebAPI Enable [true] */
    private boolean mWebAPIEnableFromSettings;
    private boolean mWebAPIEnable;

    /** WebAPI Output number */
    private int mWebAPIOutputNum = 0;

    /** When Learn number candidates [true] */
    private boolean mEnableLearnNumber = false;

    /** True when iwnn engine runs as service mode */
    private boolean mIsServiceDics = false;

    /** Whether download dictionaries has been loaded */
    private boolean mHasLoadedDownloadDictionary = false;

    /** Whether download dictionaries updating */
    private boolean mUpdateDownloadDictionary = false;

    /** Package Name */
    private String mPackageName = null;

    /** Password */
    private String mPassWord = null;

    /** The queue of operation.(DecoEmojiOperation, type FLAG_DELETE and FLAG_SET_TO_INITIALIZING only) */
    private LinkedList<DecoEmojiOperation> mOperationQueue = new LinkedList<DecoEmojiOperation>();

    /** File name of decoemoji operation event cache file. */
    public static final String FILENAME_DECO_OPERATION_EVENT_CACHE = "decoope_event.txt";

    /** File name of decoemoji operation processed index cache file(xml). */
    public static final String FILENAME_DECO_OPERATION_PROCESSED_INDEX_CACHE = "decoope_processed_index";

    /** Key top name of decoemoji operation processed max index */
    public static final String KEYNAME_DECO_OPERATION_EVENT_COUNT = "event_count";

    /** Separator decoemoji operation event and id. */
    public static final String DECO_OPERATION_SEPARATOR = ",";

    /** Key top name of decoemoji operation processed max index */
    private static final String KEYNAME_DECO_OPERATION_PROCESSED_INDEX = "processed_index";

    /** Index of operation event data */
    private static final int INDEX_OPERATION_EVENT = 0;

    /** Index of operation id data */
    private static final int INDEX_OPERATION_ID = 1;

    /** Number of operation category data */
    private static final int NUM_OPERATION_CATEGORY_DATA = 2;

    /** Id of operation init */
    public static int OPERATION_ID_INIT = -1;

    /** Whether regenerated operation queue */
    private boolean mIsRegeneratedOperationQueue = false;

    /** Enable head conversion */
    private boolean mEnableHeadConv = false;

    /** iWnn user data directory path */
    private String mFilesDirPath = null;

    /** Whether doing normalizationUserDic() */
    private boolean mIsNormalizationUserDic = false;

    /** Preferene key of is normalization user dictionary */
    private static final String KEY_NORMALIZATION_USER_DIC = "normalizationUserDic";

    /** Old file path of dictionary */
    private static final String OLD_DIC_PATH = "/data/user/0/jp.co.omronsoft.iwnnime.ml/dicset";
    private static final String DIC_DIRCTORY_NAME = "/dicset";


    /** Class for English character conversion */
    private class LatinFilter {
        /** Entire small letter. */
        private static final int CASE_LOWER = 0;
        /** Only the head is a capital letter. */
        private static final int CASE_UPPER = 1;
        /** Entire capital letter. */
        private static final int CASE_HEAD_UPPER = 3;

        /**
         * Candidate type.
         * <br>
         * CASE_LOWER: Entire small letter.<br>
         * CASE_HEAD_UPPER: Only the head is a capital letter.<br>
         * CASE_UPPER: Entire capital letter.<br>
         */
        private int mCandidateCase;
        /** Input string */
        private String mInputString;
        /** HashMap for checking duplicate word */
        private HashMap<String, WnnWord> mCandEnglishTable;

        /**
         * Constructor
         */
        public LatinFilter(){
            mCandEnglishTable = new HashMap<String, WnnWord>();
        }

        /**
         * Initialization.
         */
        private void clearLatinFilter() {
            mCandEnglishTable.clear();
            mCandidateCase = CASE_LOWER;
        }

        /**
         * Set the search key and the search mode from {@link ComposingText}.
         *
         * @param input Input text, See {@link java.lang.String}
         */
        private void setSearchKey(String input) {
            /* set mInputString */
            mInputString = input;

            if (input.length() == 0) {
                return;
            }

            if (Character.isUpperCase(input.charAt(0))) {
                if (input.length() > 1 && Character.isUpperCase(input.charAt(1))) {
                    mCandidateCase = CASE_UPPER;
                } else {
                    mCandidateCase = CASE_HEAD_UPPER;
                }
            } else {
                mCandidateCase = CASE_LOWER;
            }
            return;
        }

        /**
         * Convert candidate character according to the candidate type.
         *
         * @param candidate Candidate character, See {@link java.lang.String}
         * @return Converted character string, See {@link java.lang.String}
         */
        private String candidateConversion( String candidate ){
            String str = candidate;

            if (str.equals(toLowerCase(mSearchKey))) {
                return mSearchKey;
            }

            switch (mCandidateCase){
            case CASE_HEAD_UPPER:
                char top = candidate.charAt(0);
                if (Character.isLowerCase(top)) {
                    String tmp = toUpperCase(candidate);
                    if (tmp.length() == candidate.length()) {
                        top = tmp.charAt(0);
                        str = Character.toString(top) + candidate.substring(1);
                    }
                }
                break;
            case CASE_UPPER:
                String tmp = toUpperCase(candidate);
                if (tmp.length() == candidate.length()) {
                    str = toUpperCase(candidate);
                }
                break;
            default:
                break;
            }
            return str;
        }
        /**
         * Put a candidate.
         *
         * @param word Registered word, See {@link jp.co.omronsoft.iwnnime.ml.WnnWord}
         * @return {@code true} if the word added; {@code false} if already been registered.
         */
        private boolean putCandidate(WnnWord word) {
            if (mInputString.length() <= 1) {
                if (mCandEnglishTable.containsKey(word.candidate)) {
                    return false;
                }
                mCandEnglishTable.put(word.candidate, word);
                return true;
            } else {
                if (mCandEnglishTable.containsKey(toLowerCase(word.candidate))) {
                    return false;
                }
                mCandEnglishTable.put(toLowerCase(word.candidate), word);
                return true;
            }
        }
    }

    /**
     * Constructor
     */
    protected iWnnEngine() {
        if (DEBUG) Log.d(TAG, "iWnnEngine::iWnnEngine()");
        mCore = new IWnnCore();
        mCandTable = new HashMap<String, WnnWord>();
        mWebAPIWnnEngine = new WebAPIWnnEngine();
    }

    /**
     * Return an instance of {@code IWnnCore}.
     *
     * @return A concrete {@code IWnnCore} instance.
     */
    public static iWnnEngine getEngine() {
        return mEngine;
    }

    /**
     * Return an instance of {@code IWnnCore} for Service.
     *
     * @return A concrete {@code IWnnCore} instance for Service.
     */
    public static iWnnEngine getEngineForService(String serviceConnectedName) {
        for (int i = 0; i < mServiceEngine.size(); i++) {
           if (!(serviceConnectedName.equals("")) && mServiceEngine.get(i).mServiceConnectedName.equals(serviceConnectedName)) {
               return mServiceEngine.get(i);
            }
        }

        if (mServiceEngine.size() >= SERVICE_CONNECT_MAX ) {
            return null;
        }

        iWnnEngine serviceEngine = new iWnnEngine();
        serviceEngine.mServiceConnectedName = serviceConnectedName;
        mServiceEngine.add(serviceEngine);
        return serviceEngine;
    }
    /**
     * Clear Service Engine
     *
     */
    public static void clearServiceEngine() {
        for (int i = 0; i < mServiceEngine.size(); i++) {
            mServiceEngine.get(i).mCore.destroyWnnInfo();
        }
        mServiceEngine.clear();
    }


    /**
     * Set Connected Name for Service.
     *
     * @param serviceConnectedName  The package name of the service client.
     */
    public void setServiceConnectedName(String serviceConnectedName) {
        mServiceConnectedName = serviceConnectedName;
    }

    /**
     * Get encrypted Password .
     *
     * @param password        The Orginal Password.
     * @return The encrypted password string; {@code null} if there is happend Exception.
     */
    private String getEncryptedPassword(String password) {
        String encryptedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes(OpenWnn.CHARSET_NAME_UTF8));
            byte[] hash = md.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                if ((0xff & hash[i]) < 0x10) {
                    hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
               } else {
                    hexString.append(Integer.toHexString(0xFF & hash[i]));
                }
            }
           encryptedPassword = hexString.toString();
       } catch (Exception ex) {
           Log.e("OpenWnn", "iWnnEngine:setDictionary "+ex);
        }
       return encryptedPassword;
    }

    /**
     * Set the type of dictionary.
     * <br>
     * Unmount dictionaries if the caller is changed and a language is changed.
     *
     * @param language     See the {@link LanguageType} class.
     * @param setType      See the {@link SetType} class.
     * @param caller       The hash code of this method caller instance.
     * @param serviceFile  The configuration file path for Service.
     * @param packageName  The package name of the service client.
     * @param password     The password of the service client.
     * @return  {@code true} if Success; {@code false} if Failure.
    */
    public boolean setDictionary(int language, int setType, int caller, String serviceFile, String packageName, String password) {
        normalizationUserDic();
        String encryptedPassword = null;

        if (packageName != null && packageName.equals("") == false) {
            encryptedPassword = getEncryptedPassword(password);
            if (encryptedPassword == null) {
                return false;
            }
            mPassWord = encryptedPassword;
        }
        if ((mCaller != caller) && (mLangType != language)) {
            close();
        }
        mCaller = caller;
        mPackageName = packageName;
        return setDictionary(language, setType, serviceFile);
    }

    /**
     * Set the type of dictionary.
     * <br>
     * Unmount dictionaries if the caller is changed and a language is changed.
     *
     * @param language See the {@link LanguageType} class.
     * @param setType See the {@link SetType} class.
     * @param caller  The hash code of this method caller instance.
     * @return  {@code true} if Success; {@code false} if Failure.
    */
    public boolean setDictionary(int language, int setType, int caller){
        return setDictionary(language, setType, caller, null, mPackageName, mPassWord);
    }

    /**
     * Set the type of dictionary.
     * <br>
     * Unmount dictionaries if the caller is changed.
     *
     * @param caller  The hash code of this method caller instance.
     * @return  {@code true} if Success; {@code false} if Failure.
    */
    public boolean setDictionary(Object caller){
        return setDictionary(mLangType, mDictionarySet, caller.hashCode());
    }

    /**
     * Set the type of dictionary .
     *
     * @param language     See the {@link LanguageType} class.
     * @param dictionary   See the {@link SetType} class.
     * @return  {@code true} if Success; {@code false} if Failure.
     */
    private boolean setDictionary(int language, int dictionary) {
        return setDictionary(language, dictionary, null);
    }

    /**
     * Set the type of dictionary .
     *
     * @param language     See the {@link LanguageType} class.
     * @param dictionary   See the {@link SetType} class.
     * @param serviceFile  The configuration file path for Service.
     * @return  {@code true} if Success; {@code false} if Failure.
     */
    private boolean setDictionary(int language, int dictionary, String serviceFile) {
        if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "iWnnEngine::setDictionary()  Start");}
        if (DEBUG) Log.d(TAG, "iWnnEngine::setDictionary("+language+","+dictionary+")");
        String[] confTable = getConfTable();
        if (!(serviceFile != null) && ((language < 0) || (confTable.length <= language))) {
            Log.e(TAG, "iWnnEngine::setDictionary() END unknown Language type error. return = false");
            if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "iWnnEngine::setDictionary() " +
                    "END unknown Language type error. return = false");}
            return false;
        }
        if ((mDictionarySet == dictionary) && (mLangType == language) && (mUpdateDownloadDictionary == false)) {
            if (DEBUG) Log.d(TAG, "iWnnEngine::setDictionary() END No change dictionary error. return = false");
            if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "iWnnEngine::setDictionary() " +
                    "END No change dictionary error. return = false");}
            return false;
        }
        if (( SetType.DICTIONARY_TYPE_MAX <= dictionary) || (SetType.NONE >= dictionary)) {
            Log.e(TAG, "iWnnEngine::setDictionary() END unknown dictionary type error. return = false");
            if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "iWnnEngine::setDictionary() " +
                    "END unknown dictionary type error. return = false");}
            return false;
        }

        setDownloadDictionary();
        mCore.setServicePackageName(mPackageName, mPassWord);

        String confFile;
        if (serviceFile != null) {
            mIsServiceDics = true;
            confFile = serviceFile;
        } else {
            confFile = confTable[language];
            if ((language != LanguageType.JAPANESE) && (language != LanguageType.ENGLISH)) {
                // Language is specified, the language of the case of multi-language pack.
                // To get the path of the conf file from the specified language multilingual pack.
                Context con = OpenWnn.superGetContext();
                if (con == null) {
                    // For the acquisition path, to obtain the Context.
                    // Otherwise, if the current iWnn IME IME is
                    // It is called in the (edit user dictionary) from the configuration process,
                    // To get the Context of the configuration screen.
                    con = ControlPanelStandard.getCurrentControlPanel();
                }
                if (con != null) {
                    // get conf file
                    KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
                    String targetLangPackClassName = langPack.getLocalLangPackClassName(con, language);
                    if (targetLangPackClassName != null) {
                        String targetLangPackName = targetLangPackClassName.substring(0, targetLangPackClassName.lastIndexOf('.'));
                        confFile = langPack.getConfFile(con, targetLangPackName);
                    }
                }
            }
        }
        boolean success = mCore.setDictionary(language, dictionary, confFile,
                (language != mLangType), mFilesDirPath);
        if (success) {
            mLangType = language;
            mDictionarySet = dictionary;
            clearCandidates();
            setUpdateDownloadDictionary(false);
        }
        if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "iWnnEngine::setDictionary()  End");}
        return success;
    }

    /**
     * Cut connection of the word learning.
     * <br>
     *  When being studied next time, connection information
     *  that is one of the study parameters is cleared.
     */
    public void breakSequence() {
        if (DEBUG) Log.d(TAG, "iWnnEngine::breakSequence()");
        mHasBroke = true;
    }

    /**
     * Get the Clause candidate string of ream clause result.
     *
     * @param index Clause position
     * @return The clause candidate string; {@code null} if there is no candidate.
     */
    private String getSegmentString(int index) {
        if (DEBUG) Log.d(TAG, "iWnnEngine::getSegmentString("+index+")");
        String string = mCore.getSegmentString(index);
        if (string == null) {
            return null;
        }
        return string;
    }

    /**
     * Get the SegmentStroke.
     * <br>
     * This get the reading phrase of ream clause candidate result.
     *
     * @param index Clause position
     * @return The clause candidate reading string.
     */
    private String getSegmentStroke(int index) {
        if (DEBUG) Log.d(TAG, "iWnnEngine::getSegmentStroke("+index+")");
        String stroke = mCore.getSegmentStroke(index);
        if (stroke == null) {
            return null;
        }
        return stroke;
    }

    /**
     * Get a candidate.
     *
     * @param index Index of a candidate.
     * @return The candidate; {@code null} if there is no candidate.
     */
    private WnnWord getCandidate(int index) {
        if (DEBUG) Log.d(TAG, "iWnnEngine::getCandidate("+index+")");
        String candidate = mCore.getResultString(mSegment, index);
        String stroke    = mCore.getResultStroke(mSegment, index);
        if (candidate == null || stroke == null) {
            return null;
        }
        int attribute = 0;
        boolean islearn = mCore.isLearnDictionary(index);
        if (islearn == true) {
            attribute = WNNWORD_ATTRIBUTE_DELETABLE;
        }
        if (DEBUG) Log.d(TAG, "candidate=[" + candidate + "] stroke=[" + stroke + "] attribute=[" + attribute + "]");

        // It treats as a pseudo candidate for the same candidate as the input string.
        if ((isLowercaseStrokeInLearning())
            && (mCore.isGijiDic(index))
            && (mSearchKey.equals(candidate))) {
            attribute = WNNWORD_ATTRIBUTE_LATIN_GIJI;
        }
        //When English is set, the setting necessary for the candidate conversion is done.
        if (mEnableConvertedCandidate) {
            candidate = mLatinFilter.candidateConversion(candidate);
        }

        WnnWord word = new WnnWord(index, candidate, stroke, attribute);
        return word;
    }

    /**
     * Clear the candidates.
     */
    private void clearCandidates() {
        if (DEBUG) Log.d(TAG, "iWnnEngine::clearCandidates()");
        mOutputNum = 0;
        mSearchKey = null;
        mIsForbidDuplication = false;
        mDispWebAPIWords = false;
        mDispWebAPIButton = true;
        mCandTable.clear();
        mLatinFilter.clearLatinFilter();
        mWebAPIWnnEngine.clearCandidates();
        mWebAPIOutputNum = 0;
    }

    /**
     * Converting a string to capital letter from small letter.
     *
     * @param str Converting string.(small letter)
     * @return  Converted string.(capital letter)
     */
    public String toUpperCase(String str) {
        if (mLangType < getConfTable().length) {
            return str.toUpperCase(LOCALE_TABLE[mLangType]);
        } else {
            return str.toUpperCase();
        }
    }

    /**
     * Converting a String to small letter from capital letter.
     *
     * @param str Converting string.(capital letter)
     * @return  Converted string.(small letter)
     */
    public String toLowerCase(String str) {
        if (mLangType < getConfTable().length) {
            return str.toLowerCase(LOCALE_TABLE[mLangType]);
        } else {
            return str.toLowerCase();
        }
    }


    //----------------------------------------------------------------------
    // WnnEngine's interface
    //----------------------------------------------------------------------
    /** from WnnEngine class */
    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#init */
    @Override
    public void init(String dirPath) {
        if (DEBUG) Log.d(TAG, "iWnnEngine::init()");
        if (dirPath != null) {
            mFilesDirPath = dirPath;
        }
        mCore.init(mFilesDirPath);
        clearCandidates();
        mIsConverting = false;
        mWebAPIWnnEngine.init();
    }

    /** from WnnEngine class */
    /**@see jp.co.omronsoft.iwnnime.ml.WnnEngine#close */
    public void close() {
        if (DEBUG) Log.d(TAG, "iWnnEngine::close()");
        mDictionarySet = SetType.NONE;
        mLangType = LanguageType.NONE;
        mCore.unmountDictionary();
        clearCandidates();
        mIsServiceDics = false;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#predict */
    public int predict(ComposingText text, int minLen, int maxLen) {
        mSearchComposingText = text;
        mCaseGijiList = null;
        clearCandidates();
        mSegment = 0;
        mSegmentCount = 0;
        mIsRequestGiji = (minLen == 0);
        mIsConverting = false;
        mHasSearchWords = false;

        if (text == null) { return 0; }

        //When English is set, the setting necessary for the candidate conversion is done.
        if (mEnableConvertedCandidate) {
            mLatinFilter.setSearchKey(text.toString(1));
        }

        String input = text.toString(ComposingText.LAYER1);
        if (input == null) { return 0; }

        if (0 <= maxLen && maxLen < input.length()) {
            input = input.substring(0, maxLen);
        }

        mSearchKey = input;
        Matcher matcher = mAllowDuplicationCharPattern.matcher(input);
        if (!matcher.matches()) {
            mIsForbidDuplication = true;
        }
        int ret;
        if (DEBUG) Log.d(TAG, "iWnnEngine::input("+input+")");
        input = stripAlphabetsIfJP(input);
        ret = mCore.forecast(input, minLen, maxLen, getEnableHeadConversion());
        if (DEBUG) Log.d(TAG, "iWnnEngine::predict("+minLen+", "+maxLen+")="+ret);

        mWebAPIEnable = mWebAPIEnableFromSettings;
        if (mWebAPIEnable &&
                ((input.length() == 0)
                || (input.length() < minLen)
                || (mDictionarySet == SetType.EISUKANA)
                || (mDictionarySet == SetType.KAOMOJI))) {
            mWebAPIEnable = false;
        }

        return ret;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#convert */
    public int convert(ComposingText text) {
        if (DEBUG) Log.d(TAG, "iWnnEngine::convert()");
        mSearchComposingText = text;
        mCaseGijiList = null;
        clearCandidates();
        mSegment = 0;
        mSegmentCount = 0;
        mIsRequestGiji = false;
        mIsConverting = true;
        mHasSearchWords = false;
        mWebAPIEnable = mWebAPIEnableFromSettings;

        if (text == null) { return 0; }
        //Unable to create a dictionary of input strings
        //When English is set, the setting necessary for the candidate conversion is done.
        if (mEnableConvertedCandidate) {
            mLatinFilter.setSearchKey(text.toString(1));
        }

        String input = text.toString(ComposingText.LAYER1);
        if (input == null) { return 0; }

        mSearchKey = input;
        Matcher matcher = mAllowDuplicationCharPattern.matcher(input);
        if (!matcher.matches()) {
            mIsForbidDuplication = true;
        }
        int ret;

        ret = mCore.conv(input, text.getCursor(ComposingText.LAYER1));
        if (ret <= 0) {
            return 0;
        }

        int i, pos, len;
        StrSegment[] ss = new StrSegment[ret];

        pos = 0;
        for (i = 0; i < ret; i++) {
            String candidate = getSegmentString(i);
            String stroke = getSegmentStroke(i);
            if (candidate == null || stroke == null) {
                return 0;
            }
            len = stroke.length();
            ss[i] = new StrSegment(candidate, pos, pos+len-1);
            pos += len;
        }
        text.setCursor(ComposingText.LAYER2, text.size(ComposingText.LAYER2));
        text.replaceStrSegment(2, ss, text.getCursor(ComposingText.LAYER2));
        mSegmentCount = ret;
        return ret;
    }

  /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#searchWords */
    public int searchWords(String key, int method, int order) {
        if (DEBUG) Log.d(TAG, "iWnnEngine::searchWords(" + key + "," + method + "," + order + ")");
        mSearchCnt = 0;

        int ret = mCore.searchWord(method, order, key);
        if (ret < 0) {
            Log.e(TAG, "iWnnEngine::searchWord() error. ret=" + ret);
        }

        mHasSearchWords = true;
        return ret;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#searchWords */
    public int searchWords(String key) {
        if (DEBUG) Log.d(TAG, "iWnnEngine::searchWords(" + key + ")");

        int method;
        int order;
        if ("".equals(key)) {
            method = SearchMethod.SEARCH_ORIGINAL_PULL_FRONT;
            order = SearchOrder.ORDER_READING;
        } else {
            method = SearchMethod.SEARCH_ORIGINAL_PULL_PERFECTION;
            order = SearchOrder.ORDER_FREQUENCY;
        }

        return searchWords(key, method, order);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#getNextCandidate */
    public WnnWord getNextCandidate() {
        WnnWord word;
        if (mHCandidatesList != null) {
            if (mHCandidatesList.length <= mOutputNum) {
                word = null;
            } else {
                word = new WnnWord(mOutputNum, mHCandidatesList[mOutputNum], mHCandidatesList[mOutputNum]);
                mOutputNum++;
            }
        } else {
            word = getNextCandidateInternal();
            if (word != null) {
                mCandTable.put(word.candidate, word);
                if (mSearchKey != null) {
                    if (mSearchKey.equals(word.candidate) && (mLangType == LanguageType.KOREAN)) {
                        word = getNextCandidate();
                    }
                }
            }
        }
        return word;
    }

    /**
     * Get a next candidate.
     * @return  Next candidate
     */
    public WnnWord getNextCandidateInternal() {
        if (DEBUG) Log.d(TAG, "iWnnEngine::getNextCandidateInternal()");

        WnnWord word = null;
        if (mHasSearchWords) {
            word = getWord(mSearchCnt);
            if (word != null) {
                mSearchCnt++;
            }
        } else {
            if (mSearchKey == null) {
                return null;
            }

            if (mCaseGijiList != null) {
                word = createCaseGiji(null, false, false);
            } else {

                for (int cnt = 0; cnt < CANDIDATE_MAX; cnt++) {
                    word = getCandidate(mOutputNum);
                    if (word != null) {
                        boolean giji = mCore.isGijiDic(mOutputNum);
                        mOutputNum++;

                        if (mIsForbidDuplication || giji) {
                            if (mCandTable.containsKey(word.candidate)) {
                                continue;
                            }
                        } else if (mEnableConvertedCandidate) {
                            if (!mLatinFilter.putCandidate(word)) {
                                continue;
                            }
                        } // else {}
                    }
                    break;
                }


                if (word == null) {
                    if (mEnableConvertedCandidate) {
                        if (mSearchKey.length() < 1) {
                            word = null;
                        } else {
                            word = createCaseGiji(mSearchKey, true, false);
                        }
                    } else {
                        if ((!mIsConverting && (mLangType == LanguageType.JAPANESE))) {
                            Pattern alphabetPattern = Pattern.compile("^[a-z]+$");
                            int cursor = mSearchComposingText.getCursor(ComposingText.LAYER0);
                            String text = mSearchComposingText.toString(ComposingText.LAYER0, 0, cursor - 1);
                            if (alphabetPattern.matcher(text).matches()) {
                                word = createCaseGiji(text, true, true);
                            }
                        }
                    }
                }
            }

            if (word == null && mLangType == LanguageType.JAPANESE && mWebAPIEnable) {
                if (mDispWebAPIButton) {
                    mDispWebAPIButton = false;
                    word = new WnnWord(mOutputNum, "", "", WNNWORD_ATTRIBUTE_WEBAPI);
                    mOutputNum++;
                } else if (mDispWebAPIWords) {
                    for (int cnt = 0; cnt < WebAPIWnnEngine.WEBAPI_CANDIDATE_MAX; cnt++) {
                        word = mWebAPIWnnEngine.getNextCandidate();
                        if (word != null) {
                            mOutputNum++;
                            if (mCandTable.containsKey(word.candidate)) {
                                continue;
                            }
                            mWebAPIOutputNum++;
                        } else {
                            if (!isWebApiAllSuccessReceived()) {
                                word = new WnnWord(mOutputNum, "", "", WNNWORD_ATTRIBUTE_WEBAPI_GET_AGAIN);
                                mOutputNum++;
                                mWebAPIOutputNum++;
                                mDispWebAPIWords = false;
                            } else if (mWebAPIOutputNum <= 0) {
                                word = new WnnWord(mOutputNum, "", "", WNNWORD_ATTRIBUTE_NO_CANDIDATE);
                                mOutputNum++;
                                mWebAPIOutputNum++;
                            }
                        }
                        break;
                    }
                }
            }
        }
        return word;
    }

    /**
     * Learn a word.
     * <br>
     * This method is used to register the word selected from
     * candidates to the learning dictionary or update the frequency
     * of the word.
     * <br><br>
     * The {@code word} is from {@link #getNextCandidate} or
     * created {@link jp.co.omronsoft.iwnnime.ml.WnnWord}
     * with {@link jp.co.omronsoft.iwnnime.ml.WnnWord#attribute}
     * = {@link #WNNWORD_ATTRIBUTE_MUHENKAN}.
     * <br><br>
     * If after calling {@link #convert} and {@link #makeCandidateListOf},
     * call this method as {@code learn(word = null)}
     * for learning the consecutive clause conversion.
     *
     * @param word      The word will be learned
     * @return          {@code true} if success; {@code false} if failure or not supported.
     * @see jp.co.omronsoft.iwnnime.ml.WnnEngine#learn
     */
    public boolean learn(WnnWord word) {
        if (DEBUG) Log.d(TAG, "iWnnEngine::learn()");

        boolean noDictionary = false;
        boolean breakSequence = false;

        try {
            int index;
            if (word != null) {
                index = word.id; // The candidate who selects it is studied.

                if ((word.attribute & WNNWORD_ATTRIBUTE_NO_DICTIONARY) != 0) {
                    noDictionary = true;
                }

                if (!isEnableLearnNumber()) {
                    Pattern numberPattern = Pattern.compile(".*[0-9\uff10\uff11\uff12\uff13\uff14\uff15\uff16\uff17\uff18\uff19].*");
                    Matcher m = numberPattern.matcher(word.candidate);

                    if (m.matches()) {
                        if (((mLangType != LanguageType.ENGLISH) && (mDictionarySet == SetType.EISUKANA))
                                || (mCore.isGijiDic(word.id) && !word.candidate.equals(mSearchKey))) {

                            noDictionary = true;
                        }
                    }
                }
                if (noDictionary) {
                    breakSequence = true;
                }

                if ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_MUHENKAN) != 0) {
                    if (DEBUG) { Log.d(TAG, "muhenkan:" + word.attribute); }

                    if (isLowercaseStrokeInLearning()) {
                        word.attribute |= iWnnEngine.WNNWORD_ATTRIBUTE_MUHENKAN_LOWERCASE;
                    } else {
                        boolean success = mCore.noConv(word.stroke);
                        if (!success) {
                            return false;
                        }
                        index = -1; // The non-conversion result is studied.
                    }
                    index = -1; // The non-conversion result is studied.
                }
                if (((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_JAPANESE_QWERTY_GIJI) != 0)
                    || ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_LATIN_GIJI) != 0)
                    || ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_MUHENKAN_LOWERCASE) != 0)
                    || ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI_WORD) != 0)
                    || ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_SERVICE_WORD_NO_ENGINE) != 0)) {

                    if (noDictionary) {
                        mCore.init(mFilesDirPath);
                        mHasBroke = true;
                        return true;
                    }

                    int type;
                    int relation;

                    // Learns the selected word.
                    type = AddWordDictionaryType.ADD_WORD_DICTIONARY_TYPE_LEARNING;
                    if (mHasBroke) {
                        relation = IWnnCore.RELATIONAL_LEARNING_OFF;
                    } else {
                        relation = IWnnCore.RELATIONAL_LEARNING_ON;
                    }

                    int ret = mCore.addWord(word.stroke, word.candidate, word.lexicalCategory, type, relation);
                    if (ret < 0) {
                        return false;
                    }

                    if (((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI_WORD) != 0)) {
                        if (mSegment < (mSegmentCount - 1)) {
                            mHasBroke = breakSequence;
                            return true;
                        } else {
                            mSegment = 0;
                            mSegmentCount = 0;
                        }
                    }

                    // Searches the learning word, and then stores the word to next word information.
                    ret = mCore.forecast(word.stroke, 0, -1, getEnableHeadConversion());
                    if (ret == 0) {
                        return false;
                    }
                    index = 0;
                    String candidate = mCore.getResultString(0, index);
                    while (candidate != null) {
                        if (candidate.equals(word.candidate)) {
                            break;
                        }
                        candidate = mCore.getResultString(0, ++index);
                    }
                    noDictionary = true;
                }

            } else {
                index = -1; // The multiple phrase or clause kana-kanji conversion result is studied.
            }

            boolean ret = (mCore.select(mSegment, index, !noDictionary, mHasBroke) >= 0);
            mHasBroke = breakSequence;
            return ret;

        } catch (Exception ex) {
            Log.e("OpenWnn", "iWnnEngine:learn "+ex);
        }
        return false;
    }

    /**
     * Learn a word.
     * Not learns the predictive candidates connection.
     * <br>
     * This method is used to register the word selected from
     * candidates to the learning dictionary or update the frequency
     * of the word.
     * <br><br>
     * The {@code word} is from {@link #getNextCandidate} or
     * created {@link jp.co.omronsoft.iwnnime.ml.WnnWord}
     * with {@link jp.co.omronsoft.iwnnime.ml.WnnWord#attribute}
     * = {@link #WNNWORD_ATTRIBUTE_MUHENKAN}.
     * <br><br>
     * If after calling {@link #convert} and {@link #makeCandidateListOf},
     * call this method as {@code learn(word = null)}
     * for learning the consecutive clause conversion.
     *
     * @param word       The word will be learned
     * @param connected  {@code true}: if words are sequenced.
     * @return           {@code true}: if success; {@code false} if failure or not supported.
     * @see jp.co.omronsoft.iwnnime.ml.WnnEngine#learn
     */
    public boolean learn(WnnWord word, boolean connected) {
        if (!connected) {
            mHasBroke = true;
        }
        return learn(word);
    }

    /**
     * Learn a word.
     * <br>
     * This method is used to register the word selected from
     * candidates to the learning dictionary or update the frequency
     * of the word.
     * <br><br>
     * Please use this method after calling {@link #convert} and {@link #makeCandidateListOf}.
     * for learning the consecutive clause conversion.
     *
     * @param learn    true (as putting the candidate into the learning dictionary)
     *                  false (only put the previous committing information)
     * @return          {@code true} if success; {@code false} if failure or not supported.
     * @see jp.co.omronsoft.iwnnime.ml.WnnEngine#learn
     */
    public boolean learn(boolean learn) {
        if (DEBUG) Log.d(TAG, "iWnnEngine::learn(" + learn + ")");

        int select_result = 0;
        boolean ret = false;

        try {
            select_result = mCore.select(mSegment, -1, learn, mHasBroke);
            if (select_result < 0) {
                Log.e(TAG, "iWnnEngine::learn(" + learn + ") = " + select_result + "failure");
                ret = false;
            } else {
                if (DEBUG) Log.d(TAG, "iWnnEngine::learn(" + learn + ") = " + select_result + " Successful");
                ret = true;
            }
            if (!learn) {
                mHasBroke = true;
            } else {
                mHasBroke = false;
            }
        } catch (Exception ex) {
            Log.e("OpenWnn", "iWnnEngine::learn "+ex);
        }
        return ret;
    }

    /** from WnnEngine class */
    /**
     * Get a word.
     *
     * @param index Word candidate index
     * @return  {@code WnnWord} if Exists; {@code null} if Not exists.
     */
    private WnnWord getWord(int index) {
        String stroke = mCore.getWord(index, 0);
        String candidate = mCore.getWord(index, 1);

        if (stroke == null || candidate == null) {
            return null;
        }

        WnnWord word = new WnnWord(index, candidate, stroke);
        return word;
    }

  /**
     * Add a word to the dictionary.
     *
     * @param word      Word registration information structure
     * @param hinsi     index of the lexical category group
     * @param type      type of dictionaries(0:user dictionary, 1:learning dictionary, 2:pseudo dictionary)
     * @param relation  relation learning flag(learning relations with the previous registered word. 0:don't learn 1:do learn)
     * @return          {@code 0} if success; {@code negative number} if not.
     */
    public int addWord(WnnWord word, int hinsi, int type, int relation) {
        if (word == null) {
            Log.e(TAG, "iWnnEngine::addWord() END parameter error. return = false");
            return -1;
        }

        int ret = mCore.addWord(word.stroke, word.candidate, hinsi, type, relation);
        if (ret<0) {
            Log.e(TAG, "iWnnEngine::addWord() error. ret=" + ret);
        }

        return ret;
    }

    /** from WnnEngine class */
    /**
     * Add a word to the dictionary.
     *
     * @param word     Word registration information structure
     *                  Specify the word information to be registered.
     * @return {@code 0} if success; {@code negative number} if not.
     */
    public int addWord(WnnWord word) {

        int hinsi;
        int type;
        int relation;

        if (word == null) {
            Log.e(TAG, "iWnnEngine::addWord() END parameter error. return = false");
            return -1;
        }

        hinsi = IWnnCore.Hinshi.MEISI;
        if ((word.attribute & WNNWORD_ATTRIBUTE_TARGET_LEARN) != 0) {
            type = AddWordDictionaryType.ADD_WORD_DICTIONARY_TYPE_LEARNING;
        } else {
            type = AddWordDictionaryType.ADD_WORD_DICTIONARY_TYPE_USER;
        }
        if ((word.attribute & WNNWORD_ATTRIBUTE_CONNECTED) == 0) {
            relation = IWnnCore.RELATIONAL_LEARNING_OFF;
        } else {
            relation = IWnnCore.RELATIONAL_LEARNING_ON;
        }

        return addWord(word, hinsi, type, relation);
    }

    /** from WnnEngine class */
    /**
     * Delete the word.
     *<br>
     * Delete specified word if {@code word} is in the learning dictionary.
     *
     * @param  word  The word from getWord(), getNextCandidate(), etc..
     * @return {@code true} if success; {@code false} if not.
     */
    public boolean deleteWord(WnnWord word) {
        if (DEBUG) Log.d(TAG, "iWnnEngine::deleteWord()");

        if (word == null) {
            Log.e(TAG, "iWnnEngine::deleteWord() END parameter error. return = false");
            return false;
        }

        int result;
        if ((word.attribute & WNNWORD_ATTRIBUTE_DELETABLE) != 0) {
            result = mCore.deleteWord(word.id);
        } else {
            result = mCore.deleteSearchWord(word.id);
         }
        if (result < 0) {
            return false;
        }

        return true;
    }

    /** from WnnEngine class */
    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#setPreferences */
    public void setPreferences(SharedPreferences pref) {
        Set<String> className = pref.getStringSet("opt_multiwebapi", null);
        mWebAPIEnableFromSettings = ((className != null) && !className.isEmpty());
        mWebAPIWnnEngine.setPreferences(pref);
    }

    /**
     * Makes the candidate list.
     *
     * @param clausePosition  position of the clause.
     * @return plus value if there are candidates; 0
     * -        if there is no candidate; minus value if an error occurs.
     */
    public int makeCandidateListOf(int clausePosition) {
        if (DEBUG) Log.d(TAG, "iWnnEngine::makeCandidateListOf("+clausePosition+")");
        mSegment = clausePosition;
        mOutputNum = 0;
        mWebAPIOutputNum = 0;
        mDispWebAPIWords = false;
        mDispWebAPIButton = true;
        mIsForbidDuplication = false;
        mCandTable.clear();
        mLatinFilter.clearLatinFilter();
        if (mSearchKey == null) {
            return 0;
        }
        WnnWord word = getCandidate(0);
        if (word == null) {
            return 0;
        }
        Matcher matcher = mAllowDuplicationCharPattern.matcher(word.stroke);
        if (!matcher.matches()) {
            mIsForbidDuplication = true;
        }
        return 1;
    }


    /**
     * Write out the Dictionary.
     *
     * Write the dictionary (user dictionary / learning dictionary)
     * of the specified language.
     *
     * @param language {@link LanguageType}
     * @param setType {@link SetType}
     * @return {@code true} if success; {@code false} if not.
     */
    public boolean writeoutDictionary(int language, int setType) {
        if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "iWnnEngine::writeoutDictionary()  Start");}
        if (DEBUG) Log.d(TAG, "iWnnEngine::writeOutDictionary("+language+", "+setType+")");

        boolean ret = false;
        int currentLanguage = mLangType;
        int currentDictionary = mDictionarySet;

        int dicType;
        switch (setType) {
        case SetType.USERDIC:
        case SetType.USERDIC_NAME:
        case SetType.USERDIC_EMAIL:
        case SetType.USERDIC_PHONE:
            dicType = IWnnCore.DictionaryType.DICTIONARY_TYPE_USER;
            break;
        case SetType.LEARNDIC:
            dicType = IWnnCore.DictionaryType.DICTIONARY_TYPE_LEARNING;
            break;
        default:
            if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "iWnnEngine::writeoutDictionary() END " +
                    "unknown dictionary type error. return = false");}
            return false;
        }

        //Save the Dictionary and Language Settings
        setDictionary(language, SetType.NORMAL);
        if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "iWnnEngine::writeoutDictionary()  End");}
        return ret;
    }

    /**
     * Sync the Dictionary.
     *
     * Sync the dictionary (user dictionary / learning dictionary)
     * of the specified language.
     *
     * @param language {@link LanguageType}
     * @param setType {@link SetType}
     * @return {@code true} if success; {@code false} if not.
     */
    public boolean syncDictionary(int language, int setType) {
        if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "iWnnEngine::syncDictionary()  Start");}
        if (DEBUG) Log.d(TAG, "IWnnEngine::syncDictionary("+language+", "+setType+")");

        boolean ret = false;
        int currentLanguage = mLangType;
        int currentDictionary = mDictionarySet;

        int dicType;
        switch (setType) {
        case SetType.USERDIC:
        case SetType.USERDIC_NAME:
        case SetType.USERDIC_EMAIL:
        case SetType.USERDIC_PHONE:
            dicType = IWnnCore.DictionaryType.DICTIONARY_TYPE_USER;
            break;
        case SetType.LEARNDIC:
            dicType = IWnnCore.DictionaryType.DICTIONARY_TYPE_LEARNING;
            break;
        default:
            if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "iWnnEngine::syncDictionary() END " +
                    "unknown dictionary type error. return = false");}
            return false;
        }

        //Save the Dictionary and Language Settings
        setDictionary(language, SetType.NORMAL);
        ret = mCore.syncDictionary(dicType);

        if(!ret) {
            Log.e(TAG, "IWnnEngine::syncDictionary() END failed error. return = false");
        }

        //Restore the Dictionary and Language Settings
        setDictionary(currentLanguage, currentDictionary);
        if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "iWnnEngine::syncDictionary()  End");}

        ret = mCore.writeoutDictionary(dicType);

        if(!ret) {
            Log.e(TAG, "iWnnEngine::writeoutDictionary() END failed error. return = false");
        }

        //Restore the Dictionary and Language Settings
        setDictionary(currentLanguage, currentDictionary);

        return ret;
    }

    /**
     * Sync filesystem.
     */
    public void sync() {
        if (DEBUG) Log.d(TAG, "IWnnEngine::sync()");
        mCore.sync();
    }

    /**
     * Flexible Charset.
     * <br>
     * Flexible Character Setting.
     *
     * @param charset FlexibleSearchType.FLEXIBLE_SEARCH_OFF :Flexible Search OFF
     *                 FlexibleSearchType.FLEXIBLE_SEARCH_ON  :Flexible Search ON
     * @param keytype KeyboardType.KEY_TYPE_KEYPAD12          :Keyboard type KEYPAD12
     *                 KeyboardType.KEY_TYPE_QWERTY            :Keyboard type QWERTY
     * @return        {@code 1} success; {@code 0} error.
     */
    public int setFlexibleCharset(int charset, int keytype) {
        if (DEBUG) Log.d(TAG, "iWnnEngine::setFlexibleCharset("+charset+ "," + keytype + ")");
        int ret = 0;

        if (mLangType == LanguageType.NONE) {
            return ret;
        }
        if ( (FlexibleSearchType.FLEXIBLE_SEARCH_OFF != charset)
          && (FlexibleSearchType.FLEXIBLE_SEARCH_ON != charset) ) {
            return ret;
        }
        if ( (KeyboardType.KEY_TYPE_KEYPAD12 != keytype)
           && (KeyboardType.KEY_TYPE_QWERTY != keytype) ) {
            return ret;
        }
        ret = mCore.setFlexibleCharset(charset, keytype);
        mCore.init(mFilesDirPath);

        return ret;
    }

    /**
     * Convert half-width to full-width.
     *
     * @param string  the string.
     * @return the char converted to full-width.
     */
    private String convertHalftoFull(String string) {
        char[] chars = string.toCharArray();
        int length = chars.length;
        for (int i = 0; i < length; i++) {
            chars[i] += OFFSET_FULL_WIDTH;
        }
        return new String(chars);
    }

    /**
     * Add a word and converted full-width word to list.
     *
     * @param list  the list.
     * @param word  the word.
     * @param fullWidth  true if require full-width candidate.
     */
    private void addToListWithFullWidth(ArrayList<String> list, String word, boolean fullWidth) {
        if (!word.equals(mSearchKey)) {
            list.add(word);
        }
        if (fullWidth) {
            list.add(convertHalftoFull(word));
        }
    }

    /**
     * Create the pseudo candidate of the inputted stroke.
     * <br>
     * ex) "aaa" -> "Aaa", "AAA"
     *
     * @param stroke create pseudo candidate
     * @param init if first time create is true
     * @param fullWidth  true if require full-width candidate. This value is enabled only when 'init' is true.
     * @return pseudo candidate
     */
    private WnnWord createCaseGiji(String stroke, boolean init, boolean fullWidth) {
        if (!mIsRequestGiji) {
            return null;
        }

        String result = null;
        if (init) {
            mCaseGijiList = null;
            ArrayList<String> list = new ArrayList<String>();

            // Lower case.
            String word = toLowerCase(stroke);
            addToListWithFullWidth(list, word, fullWidth);

            int length = word.length();
            String upperCase = toUpperCase(word);
            if (length == upperCase.length()) {
                // Upper case.
                if (1 < length) {
                    addToListWithFullWidth(list, upperCase, fullWidth);
                }

                // Capitalise the first letter of a word.
                char c = upperCase.charAt(0);
                word = Character.toString(c) + word.substring(1);
                addToListWithFullWidth(list, word, fullWidth);
            }

            if (0 < list.size()) {
                mCaseGijiList = list.toArray(new String[list.size()]);
                mCaseGijiListIndex = 0;
                result = mCaseGijiList[0];
            }
        } else {
            mCaseGijiListIndex++;
            if (mCaseGijiListIndex < mCaseGijiList.length) {
                result = mCaseGijiList[mCaseGijiListIndex];
            }
        }

        if (result == null) {
            mIsRequestGiji = false;
            return null;
        } else {
            int attribute = 0;
            if (mEnableConvertedCandidate) {
                attribute = iWnnEngine.WNNWORD_ATTRIBUTE_LATIN_GIJI;
            } else {
                /* mDictionarySet == IWNNIME_DIC_LANG_JP Route for Japanese dictionary.
                   Increase the condition when the language increases. */
                attribute = iWnnEngine.WNNWORD_ATTRIBUTE_JAPANESE_QWERTY_GIJI;
            }
            return new WnnWord(0, result, mSearchKey, attribute);
        }
    }

    /**
     * Initialize an user dictionary.
     *
     * @param language {@link LanguageType}
     * @param setType {@link SetType}
     * @return  {@code true} if Success; {@code false} if Failure.
     */
    public boolean initializeUserDictionary(int language, int setType) {
        if ((setType != SetType.USERDIC)
                && (setType != SetType.USERDIC_NAME)
                && (setType != SetType.USERDIC_EMAIL)
                && (setType != SetType.USERDIC_PHONE)) {

            return false;
        }

        if (mCore.runInitialize(USER_DICTIONARY_DELETE, language, setType)
                != DICTIONARY_DELETE_FAILURE) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Create an additional dictionary.
     *
     * @param setType {@link SetType}
     * @return  {@code true} if Success; {@code false} if Failure.
     */
    public boolean createAdditionalDictionary(int setType) {
        if (setType < SetType.ADDITIONALDIC) {
            return false;
        }
        return mCore.createAdditionalDictionary(setType);
    }

    /**
     * Delete an additional dictionary.
     *
     * @param setType {@link SetType}
     * @return  {@code true} if Success; {@code false} if Failure.
     */
    public boolean deleteAdditionalDictionary(int setType) {
        if (setType < SetType.ADDITIONALDIC) {
            return false;
        }
        return mCore.deleteAdditionalDictionary(setType);
    }

    /**
     * Save an additional dictionary.
     *
     * @param setType {@link SetType}
     * @return  {@code true} if Success; {@code false} if Failure.
     */
    public boolean saveAdditionalDictionary(int setType) {
        if (setType < SetType.ADDITIONALDIC) {
            return false;
        }
        return mCore.saveAdditionalDictionary(setType);
    }

    /**
     * Create an additional dictionary.
     *
     * @param setType {@link SetType}
     * @return  {@code true} if Success; {@code false} if Failure.
     */
    public boolean createAutoLearningDictionary(int setType) {
        if (setType < SetType.AUTOLEARNINGDIC) {
            return false;
        }
        return mCore.createAutoLearningDictionary(setType);
    }

    /**
     * Delete an additional dictionary.
     *
     * @param setType {@link SetType}
     * @return  {@code true} if Success; {@code false} if Failure.
     */
    public boolean deleteAutoLearningDictionary(int setType) {
        if (setType < SetType.AUTOLEARNINGDIC) {
            return false;
        }
        return mCore.deleteAutoLearningDictionary(setType);
    }

    /**
     * Save an additional dictionary.
     *
     * @param setType {@link SetType}
     * @return  {@code true} if Success; {@code false} if Failure.
     */
    public boolean saveAutoLearningDictionary(int setType) {
        if (setType < SetType.AUTOLEARNINGDIC) {
            return false;
        }
        return mCore.saveAutoLearningDictionary(setType);
    }

    /**
     * Initialize a learning dictionary.
     *
     * @param language {@link LanguageType}
     * @return  {@code true} if Success; {@code false} if Failure.
     */
    public boolean initializeLearnDictionary(int language) {
        if (mCore.runInitialize(LEARN_DICTIONARY_DELETE, language, SetType.NONE)
                !=  DICTIONARY_DELETE_FAILURE) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * Reset a Extended Info.
     *
     * @param fileName
     * @return  {@code true} if Success; {@code false} if Failure.
     */
    public boolean resetExtendedInfo(String fileName) {
        if (mCore.resetExtendedInfo(fileName)
                !=  DICTIONARY_DELETE_FAILURE) {
            return true;
        } else {
            return false;
        }
    }
    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#initializeDictionary */
    public boolean initializeDictionary(int dictionary, int type) {
        // no implements
        return false;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#initializeDictionary(int, int) */
    public boolean initializeDictionary(int dictionary) {
        // no implements
        return false;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#searchWords */
    public int searchWords(WnnWord word) {
        /* !!! WARNING !!!
         * This method in OpenWnn Framework is able to search words from all dictionaries.
         * But this searchWords(WnnWord) can only search from a learning dictionary.
         * iWnnEngine.searchWords(String) is as same behavior as searchWords(WnnWord) in OpenWnn Framework.
         */

        int ret = 0;

        if (word == null) {
            Log.e(TAG, "iWnnEngine::searchWords() END parameter error. return = false");
            return ret;
        }

        if ((word.attribute & WNNWORD_ATTRIBUTE_DELETABLE) != 0) {
            //Save dictionary settings before switch learning dictionary.
            WnnWord getWord;
            int dictionary = mDictionarySet;
            int language = mLangType;
            if (!setDictionary(language, SetType.LEARNDIC)) {
                Log.e(TAG, "iWnnEngine::searchWords() END setDictionary() failed error. return = " + ret);
                return ret;
            }
            //Delete all words in the learning dictionary.
            while (searchWords(word.stroke) != 0 ) {
                while ((getWord = getNextCandidate()) != null){
                    // Use uppercase. See deleteWord()
                    if (toUpperCase(word.candidate).equals(toUpperCase(getWord.candidate))){
                        ret = 1;
                        break;
                    }
                }
                if(getWord == null || ret == 1 ){
                    break;
                }
            }
            //Restore dictionary settings.
            setDictionary(language, dictionary);
        }
        return ret;
    }

    /** from WnnEngine */
    /**
     * Retrieve the list of registered words.
     *
     * @return null
     */
    public WnnWord[] getUserDictionaryWords( ) {
        // Not implements
        return null;
    }

    /**
     * Undo of learning.
     *
     * @param count Count of Undo
     * @return  {@code true} if Success; {@code false} if Failure.
     */
    public boolean undo(int count) {
        return mCore.undo(count);
    }


    /**
     * Check a candidate word if it's on the pseudo dictionary.
     *
     * @param index  index of the candidate word
     * @return       true : if the word is on the pseudo dictionary,
     *               false: if not
     */
    public boolean isGijiDic(int index) {
        return mCore.isGijiDic(index);
    }

    /**
     * Set a pseudo dictionary filter.
     *
     * @param type  Pseudo dictionary filter.
     * @return  {@code true} if Success; {@code false} if Failure.
     */
    public boolean setGijiFilter(int[] type) {
        return mCore.setGijiFilter(type);
    }

    /**
     * Control pictograph candidate.
     *
     * @param enabled {@code true}: Pictograph is not included in the candidate;
     *                 {@code false}: included in the candidate
     */
    public void setEmojiFilter(boolean enabled) {
        mCore.setEmojiFilter(enabled);
    }

    /**
     * Set emailAddress filter.
     * <br>
     * Control the candidate including or excluding the character
     * used in the mail address.
     *
     * @param enabled {@code true}: controls; {@code false}:not controls
     */
    public void setEmailAddressFilter(boolean enabled) {
        mCore.setEmailAddressFilter(enabled);
    }

    /**
     * Enables to convert candidate.
     * <br>
     * This engine will convert candidates in like an input text.
     * And an input text will be converted to uppercase or CamelCase as pseudo candidate.
     *
     * @param enabled {@code true}: to enable pseudo candidate; {@code false}: otherwise
     */
    public void setConvertedCandidateEnabled(boolean enabled) {
        mEnableConvertedCandidate = enabled;
    }

    /**
     * Get the dictionary set.
     *
     * @return mDictionarySet See
     * the {@link iWnnEngine.SetType} class
     */
    public int getDictionary() {
        return mDictionarySet;
    }

    /**
     * Get the Language type.
     *
     * @return mLangType See
     * the {@link iWnnEngine.LanguageType} class
     */
    public int getLanguage() {
        return mLangType;
    }

    /**
     * Return the specified locale represents.
     *
     * @param language  {@link LanguageType}
     * @return LocaleString
     */
    public String getLocaleString(int language) {
        if (mLangType < getConfTable().length) {
            return LOCALE_TABLE[language].toString();
        } else {
            return Locale.getDefault().toString();
        }
    }

    /**
     * Return the specified locale.
     *
     * @param language  {@link LanguageType}
     * @return locale
     */
    public Locale getLocale(int language) {
        if (mLangType < getConfTable().length) {
            return LOCALE_TABLE[language];
        } else {
            return Locale.getDefault();
        }
    }

    /**
     * Delete last alphabet for Japanese mode.
     * <br>
     * (It doesn't cut down for an alphabet alone.)
     * @param input  Input string
     * @return Result string
     */
    private String stripAlphabetsIfJP(String input) {
        if (mLangType == LanguageType.JAPANESE) {
            Pattern p = Pattern.compile("^[a-zA-Z]*$");
            if (!p.matcher(input).matches()) {
                p = Pattern.compile("[a-zA-Z]+$");
                input = p.matcher(input).replaceAll("");
            }
        }
        return input;
    }

    /**
     * Check whether current language learning dictionary is stroke lowerCase
     * <br>
     * @return {@code true} current language dictionary is stroke lowerCase;
     * {@code false} else
     */
    private boolean isLowercaseStrokeInLearning() {
        boolean result = false;
        switch(mLangType) {
        case LanguageType.JAPANESE:
        case LanguageType.GERMAN:
        case LanguageType.SIMPLIFIED_CHINESE:
        case LanguageType.TRADITIONAL_CHINESE:
            result = false;
            break;
        default:
            if (mLangType < getConfTable().length) {
                result = true;
            } else {
                result = false;
            }
            break;
        }

        return result;
    }

    /**
     * Enables to WebAPI Words.
     *
     * @param enabled {@code true}: to enable WebAPI Words; {@code false}: otherwise
     */
    public void setWebAPIWordsEnabled(boolean enabled) {
        mDispWebAPIWords = enabled;
        if (mDispWebAPIWords) {
            mDispWebAPIButton = false;
        } else {
            mDispWebAPIButton = true;
        }
        mOutputNum = 0;
        mWebAPIOutputNum = 0;
        mCandTable.clear();
    }

   /**
     * Get WebAPI Words State.
     *
     * @return {@code true}: to display WebAPI Words; {@code false}: otherwise
     */
    public boolean getWebAPIWordsEnabled() {
        return mDispWebAPIWords;
    }

    /**
     * Start WebAPI.
     *
     * @param text      The input string
     */
    public void startWebAPI(ComposingText text) {
        mWebAPIWnnEngine.start(text);
    }

    /**
     * Start WebAPI get again.
     *
     * @param text      The input string
     */
    public void startWebAPIGetAgain(ComposingText text) {
        mWebAPIWnnEngine.getAgain(text);
    }

    /**
     * Set WebAPI candidates.
     *
     * @param yomi       The yomi string
     * @param candidates The candidates string
     */
    public void setWebApiCandidates(String yomi, String[] candidates, short[] hinshi) {
        mWebAPIWnnEngine.setCandidates(yomi, candidates, hinshi);
    }

    /**
     * Get sending yomi string to WebAPI.
     *
     * @return The yomi string.
     */
    public String getSendingYomiToWebApi() {
        return mWebAPIWnnEngine.getSendingYomi();
    }

    /**
     * Done getting candidates.
     */
    public void onDoneGettingCandidates() {
        mWebAPIWnnEngine.onDoneGettingCandidates();
    }

    /**
     * Set download dictionary config to temporary area in native layer.
     */
    public void setDownloadDictionary() {
        if (!mHasLoadedDownloadDictionary && OpenWnn.getCurrentIme() != null) {
            DownloadDictionaryPreferenceActivity.setDownloadDictionary(OpenWnn.superGetContext());
            mHasLoadedDownloadDictionary = true;
        }
    }

    /**
     * Set download dictionary config to temporary area in native layer.
     *
     * @param index index of download dictionary array
     * @param name name of download dictionary
     * @param file file path of download dictionary
     * @param convertHigh value of convert high
     * @param convertBase value of convert base
     * @param predictHigh value of predict high
     * @param predictBase value of predict base
     * @param morphoHigh value of morpho high
     * @param morphoBase value of morpho base
     * @param cache value of cache
     * @param limit value of limit
     */
    public void setDownloadDictionary(int index, String name, String file, int convertHigh,
            int convertBase, int predictHigh, int predictBase, int morphoHigh, int morphoBase,
            boolean cache, int limit) {
        mCore.setDownloadDictionary(
                index,
                name,
                file,
                convertHigh,
                convertBase,
                predictHigh,
                predictBase,
                morphoHigh,
                morphoBase,
                cache,
                limit
        );
    }

    /**
     * Re-read conf file and refresh dictionary data in native layer.
     * When setting download dictionary before IME opened, mLangType is initialized value -1,
     * so default result value in this method is "true".
     *
     * @return true if setting dictionary succeed.
     */
    public boolean refreshConfFile() {
        return mCore.refreshConfFile();
    }

    /**
     * Set the learn number candidate flg used by service.
     *
     * @param enabled {@code true}: to enable learn number candidates;
     *                {@code false}: otherwise
     */
    public void setEnableLearnNumber(boolean enableLearnNumber) {
        mEnableLearnNumber = enableLearnNumber;
    }

    /**
     * Get the learn number candidate.
     *
     * @return {@code true}: to enable learn number candidates;
     *         {@code false}: otherwise
     */
    private boolean isEnableLearnNumber() {
        if (mIsServiceDics) {
            // Service connected language.
            return mEnableLearnNumber;
        } else {
            // iWnnIME language.
            if ((mLangType == LanguageType.JAPANESE) || (mLangType == LanguageType.ENGLISH)) {
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * Get the conf table.
     *
     * @return conf table
     */
    public String[] getConfTable() {
        String[] confTable = CONF_TABLE.clone();
        if (OpenWnn.isTabletMode()) {
            confTable = CONF_TABLET_TABLE.clone();
        }
        return confTable;
    }

    /**
     * Init the Giji List.
     */
    public void initGijiList() {
        if (mCaseGijiList != null) {
            mCaseGijiList = null;
            mCaseGijiListIndex = 0;
            mIsRequestGiji = true;
        }
    }

    /**
     * Return whether current engine state is conversion or not.
     *
     * @return {@code true} if conversion is executed currently.
     */
    public boolean isConverting() {
        return mIsConverting;
    }

    /**
     * Control decoemoji candidate.
     *
     * @param enabled {@code true}: DecoEmoji is not included in the candidate;
     *                 {@code false}: included in the candidate
     */
    public void setDecoEmojiFilter(boolean enabled) {
        mCore.setDecoEmojiFilter(enabled);
    }

    /** from WnnEngine class */
    /**
     * Control a decoemoji the dictionary.
     *
     * @param id            The id string
     * @param yomi          The yomi string
     * @param hinsi         Number of hinsi
     * @param control_flag  Control Type
     */
    public void controlDecoEmojiDictionary(String id, String yomi, int hinsi, int control_flag) {
        mCore.controlDecoEmojiDictionary(id, yomi, hinsi, control_flag);
        return;
    }

    /**
     * Check a decoemoji the dictionary.
     *
     */
    public int checkDecoEmojiDictionary() {
        return mCore.checkDecoEmojiDictionary();
    }

    /**
     * Reset a decoemoji the dictionary.
     *
     */
    public int resetDecoEmojiDictionary() {
        return mCore.resetDecoEmojiDictionary();
    }

    /**
     * Check a decoemoji the dicset.
     *
     */
    public int checkDecoemojiDicset() {
        return mCore.checkDecoemojiDicset();
    }

    /**
     * Set webapi result.
     *
     * @param packageName The packageName string
     * @param Success     If true is success. if false is response error.
     */
    public void setWebApiResult(String packageName, boolean success) {
        mWebAPIWnnEngine.setWebApiResult(packageName, success);
    }

    /**
     * Is webapi engine all result received.
     *
     * @return If true is all success.
     */
    public boolean isWebApiAllReceived() {
        return mWebAPIWnnEngine.isWebApiAllReceived();
    }

    /**
     * Is webapi engine result success received.
     *
     * @return If true is success.
     */
    public boolean isWebApiSuccessReceived() {
        return mWebAPIWnnEngine.isWebApiSuccessReceived();
    }

    /**
     * Is webapi engine all succes.
     *
     * @return If true is all success.
     */
    public boolean isWebApiAllSuccessReceived() {
        return mWebAPIWnnEngine.isWebApiAllSuccessReceived();
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#getgijistr */
    public int getgijistr(ComposingText text, int type) {
        int ret = 0;
        switch(type) {
            case CONVERT_TYPE_HIRAGANA:
            case CONVERT_TYPE_KATAKANA:
            case CONVERT_TYPE_HANKATA:
                ret = getGijiKanaStr(text, type);
                break;

            case CONVERT_TYPE_HAN_EIJI_CAP:
            case CONVERT_TYPE_ZEN_EIJI_CAP:
            case CONVERT_TYPE_HAN_EIJI_UPPER:
            case CONVERT_TYPE_ZEN_EIJI_UPPER:
            case CONVERT_TYPE_HAN_EIJI_LOWER:
            case CONVERT_TYPE_ZEN_EIJI_LOWER:
                ret = getGijiEijiStr(text, type);
                break;

            default:
                break;
        }
        return ret;
    }

    /**
     * Create the pseudo candidate of the inputted stroke.(For Kana)
     *
     * @param text          The input string
     * @param type          Casing to be converted
     * @return              Candidate segment count. 0 if fails.
     */
    private int getGijiKanaStr(ComposingText text, int type) {
        if (null == text) {
             return 0;
        }
        String tempStr = text.toString(ComposingText.LAYER1);
        String tempStrLay0 = text.toString(ComposingText.LAYER0);
        if (null == tempStr || null == tempStrLay0) {
             return 0;
        }
        String tempCandidate = null;
        StringBuilder compCandidate = new StringBuilder();
        int len     = tempStr.length();
        int lenLay0 = tempStrLay0.length();
        int posLay0 = 0;

        // In using a pseudo-dictionary, a different character types
        // if mixed with other Member States will not get the conversion result.
        // So, while confirming the character type one character, do the appropriate conversions.
        for (int count = 0; count < len; count++) {
            String tempChar = tempStr.substring(count, count + 1);
            char    tc = tempChar.charAt(0);
            int     ret = 0;
            if (isHiragana(tc)) {
                ret = mCore.getgijistr(tempChar, tempChar.length(), type);
            }
            if (0 >= ret) {
                // Than or Hiragana, failed to getgijistr().
                tempCandidate = tempChar;
                if (isAlphabet(tc)) {
                    // If the alphabet.
                    if (CONVERT_TYPE_HANKATA != type) {
                        tempCandidate = convertHalftoFull(tempChar);
                    }
                } else {
                    // If you are not in the alphabet.
                    if (CONVERT_TYPE_HANKATA == type) {
                        for (; posLay0 < lenLay0; posLay0++) {
                            if (!isAlphabet(tempStrLay0.charAt(posLay0))) {
                                tempCandidate = convertFulltoHalfKanaSymbol(tc);
                                if (null == tempCandidate) {
                                    tempCandidate = tempStrLay0.substring(posLay0, posLay0 + 1);
                                }
                                posLay0++;
                                break;
                            }
                        }
                    } else if (mLangType != LanguageType.JAPANESE) {
                        // Full-width input mode, as is a good character Layer1.
                        // Half-width input mode, so half-width the Layer1, and must not be full-width.
                        tempCandidate = convertHalftoFull(tempChar);
                    }
                }
            } else {
                // successed to getgijistr().
                tempCandidate = getSegmentString(0);
            }
            compCandidate.append(tempCandidate);
        }

        // Note the logic: convert()
        StrSegment[] ss = new StrSegment[1];
        ss[0] = new StrSegment(new String(compCandidate), 0, len - 1);

        text.setCursor(ComposingText.LAYER2, text.size(ComposingText.LAYER2));
        text.replaceStrSegment(ComposingText.LAYER2, ss, text.getCursor(ComposingText.LAYER2));
        mSegmentCount = 1;
        return mSegmentCount;
    }

    /**
     * Create the pseudo candidate of the inputted stroke.(For English)
     * <br>
     * ex) "aaa" -> "Aaa", "AAA"
     *
     * @param text          The input string
     * @param type          Casing to be converted
     * @return              Candidate segment count. 0 if fails.
     */
    private int getGijiEijiStr(ComposingText text, int type) {
        if (null == text) {
             return 0;
        }
        String input = text.toString(ComposingText.LAYER0);
        if (null == input) {
             return 0;
        }
        String candidate = null;

        // Note the logic: createCaseGiji()
        switch (type) {
            case CONVERT_TYPE_HAN_EIJI_LOWER:
                candidate = toLowerCase(input);
                break;

            case CONVERT_TYPE_ZEN_EIJI_LOWER:
                candidate = convertHalftoFull(toLowerCase(input));
                break;

            case CONVERT_TYPE_HAN_EIJI_UPPER:
                candidate = toUpperCase(input);
                break;

            case CONVERT_TYPE_ZEN_EIJI_UPPER:
                candidate = convertHalftoFull(toUpperCase(input));
                break;

            case CONVERT_TYPE_HAN_EIJI_CAP:
                candidate = Character.toString(toUpperCase(input).charAt(0)) + toLowerCase(input).substring(1);
                break;

            case CONVERT_TYPE_ZEN_EIJI_CAP:
                candidate = convertHalftoFull(Character.toString(toUpperCase(input).charAt(0)) + toLowerCase(input).substring(1));
                break;

             default:
                return 0;
        }

        // Note the logic: convert()
        StrSegment[] ss = new StrSegment[1];
        int len = text.toString(ComposingText.LAYER1).length();
        ss[0] = new StrSegment(candidate, 0, len - 1);

        text.setCursor(ComposingText.LAYER2, text.size(ComposingText.LAYER2));
        text.replaceStrSegment(ComposingText.LAYER2, ss, text.getCursor(ComposingText.LAYER2));
        mSegmentCount = 1;
        return mSegmentCount;
    }

    /**
     * Judge hiragana
     * Note the logic: UserDictionaryToolsListJa.java ListComparatorJA#isHiragana()
     *
     * @param checkChar Compared characters
     * @return {@code true} if hiragana; {@code false} if Not hiragana.
     */
    private boolean isHiragana(char checkChar) {
        boolean ret = false;
        if (('\u3041' <= checkChar) && (checkChar <='\u3096')) {
            ret = true;
        }
        return ret;
    }

    /**
     * Judge alphabet.
     *  Note the logic: UserDictionaryToolsListEn.java ListComparatorEN#isHiragana()
     *
     * @param checkChar Compared characters
     * @return {@code true} if alphabet; {@code false} if Not alphabet.
     */
    private boolean isAlphabet(char checkChar) {
        boolean ret = false;
        if ((('\u0041' <= checkChar) && (checkChar <='\u005a')) || (('\u0061' <= checkChar) && (checkChar <='\u007a'))) {
            ret = true;
        }
        return ret;
    }

    /**
     * "Full-width Katakana symbol" to "Half-width Katakana symbol" conversion.
     *
     * @param convChar Conversion characters
     * @return {@code String} Converted String; {@code null} if Not supported symbol.
     */
    private String convertFulltoHalfKanaSymbol(char convChar) {
        String retString = null;
        switch (convChar) {
            case '\u3001':    // ､
                retString = Character.toString('\uFF64');
                break;

            case '\u3002':    // ｡
                retString = Character.toString('\uFF61');
                break;

            case '\u300C':    // ｢
                retString = Character.toString('\uFF62');
                break;

            case '\u300D':    // ｣
                retString = Character.toString('\uFF63');
                break;

            case '\u30FB':    // ･
                retString = Character.toString('\uFF65');
                break;

            default:         // not supported
                break;
        }
        return retString;
    }

    /**
     * Set update download dictionary state
     * @param update state update download dictionary
     */
    public void setUpdateDownloadDictionary(boolean update) {
        mUpdateDownloadDictionary = update;
    }

    /**
     * Dequeues the DecoEmoji operation.
     *
     * @param  context      Context.
     * @return {@code true} if operation is executed.
     */
    public boolean executeOperation(Context context) {
        boolean result = false;
        if (!mIsRegeneratedOperationQueue) {
            // Has been disposed of instance, or if the service client has changed.
            // Since there is a possibility on the memory queue is incomplete,
            // Current queue are discarded, to rebuild the queue from the cache file.
            mOperationQueue.clear();
            regenerationOperationQueue(context);
        }
        DecoEmojiOperation operation = mOperationQueue.poll();
        if (operation != null) {
            switch (operation.getType()) {
            // 1) Case of deletion
            case IDecoEmojiConstant.FLAG_DELETE:
                DecoEmojiAttrInfo[] queueInfo = operation.getDecoEmojiAttrInfo();
                StringBuffer strBuf = new StringBuffer();
                for (int i = 0; i < queueInfo.length; i++) {
                    strBuf.delete(0, strBuf.length());
                    strBuf.append(String.valueOf(DecoEmojiOperationQueue.ESC_CODE));
                    if (queueInfo[i] != null) {
                        strBuf.append(String.format(DecoEmojiOperationQueue.DECO_ID_FORMAT, queueInfo[i].getId()));
                    } else {
                        // Invalid data (though it is removed, there is no ID deleted)
                        strBuf.append(String.format(DecoEmojiOperationQueue.DECO_ID_FORMAT, iWnnEngine.OPERATION_ID_INIT));
                    }
                    result = deleteLearnDicDecoEmojiWord(strBuf.toString());
                }
                break;
            // 2) Case of initialization
            case IDecoEmojiConstant.FLAG_SET_TO_INITIALIZING:
                result = deleteLearnDicDecoEmojiWord(String.valueOf(DecoEmojiOperationQueue.ESC_CODE));
                break;
            // Do not process other than the above.
            default:
                return true;
            }
            if (result) {
                updateOperationProcessedIndexCache(context);
            } else {
                // Since the process failed, I returned to the top of the queue.
                mOperationQueue.addFirst(operation);
            }
        }
        return result;
    }

    /**
     * Delete a word on the learning dictionary.
     *
     * @param  delword  The delete decoemoji word.
     * @return {@code true} if success; {@code false} if not.
     */
    public boolean deleteLearnDicDecoEmojiWord(String delword) {
        int result = mCore.deleteLearnDicDecoEmojiWord(delword);
        if (result < 0) {
            return false;
        }
        return true;
    }

    /**
     * Get num instance of {@code IWnnCore} for Service.
     *
     * @return Number that holds instance of {@code IWnnCore} for Service.
     */
    public static int getNumEngineForService() {
        return mServiceEngine.size();
    }

    /**
     * Return an instance of {@code IWnnCore} for Service.
     *
     * @param  index  get index
     * @return A concrete {@code IWnnCore} instance for Service.
     */
    public static iWnnEngine getEngineForService(int index) {
        if ((0 <= index) && (index < mServiceEngine.size())) {
            return mServiceEngine.get(index);
        }
        return null;
    }

    /**
     * Enqueues the DecoEmoji operation.
     *
     * @param  addOperation  Enqueues the instance of DecoEmoji operation.
     * @param  context       Context.
     * @return none
     */
    public void enqueueOperation(DecoEmojiOperation addOperation, Context context) {
        int type = addOperation.getType();
        if ((type == IDecoEmojiConstant.FLAG_DELETE || type == IDecoEmojiConstant.FLAG_SET_TO_INITIALIZING)) {
            if (type == IDecoEmojiConstant.FLAG_SET_TO_INITIALIZING) {
                // Since the initialization request, it will be all clear.
                // Therefore, up to now the queue is no longer needed treatment.
                mOperationQueue.clear();
            }
            mOperationQueue.add(addOperation);
        }
    }

    /**
     * Update decoemoji operation processed index.
     *
     * @param  context      Context.
     * @return none
     */
    private void updateOperationProcessedIndexCache(Context context) {
        if (context != null) {
            SharedPreferences pref = context.getSharedPreferences(FILENAME_DECO_OPERATION_PROCESSED_INDEX_CACHE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();

            StringBuffer key = new StringBuffer(KEYNAME_DECO_OPERATION_PROCESSED_INDEX);
            if (mPackageName != null) {
                key.append("_");
                key.append(mPackageName);
            }

            String keyStr = key.toString();
            long index = pref.getLong(keyStr, 0);
            index++;
            editor.putLong(keyStr, index);
            editor.commit();
        }
    }

    /**
     * Regeneration Operation queue.
     *
     * @param  context      Context.
     * @return none
     */
    private void regenerationOperationQueue(Context context) {
        if (context != null) {
            SharedPreferences pref = context.getSharedPreferences(FILENAME_DECO_OPERATION_PROCESSED_INDEX_CACHE, Context.MODE_PRIVATE);
            StringBuffer key = new StringBuffer(KEYNAME_DECO_OPERATION_PROCESSED_INDEX);
            if (mPackageName != null) {
                key.append("_");
                key.append(mPackageName);
            }

            long eventCount = pref.getLong(KEYNAME_DECO_OPERATION_EVENT_COUNT, 0);
            long processedIndex = pref.getLong(key.toString(), 0);

            if (processedIndex < eventCount) {
                BufferedReader reader = null;
                try {
                    // There are unprocessed events.
                    InputStream in = context.openFileInput(FILENAME_DECO_OPERATION_EVENT_CACHE);
                    reader = new BufferedReader(new InputStreamReader(in, OpenWnn.CHARSET_NAME_UTF8));
                    for (int index = 0; index < processedIndex; index++) {
                        // Event that has already been processed, skipped.
                        if (reader.readLine() == null) {
                            Log.e(TAG, "iWnnEngine:regenerationOperationQueue() nothing event index["+index+"] processedIndex["+processedIndex+"]");
                            mIsRegeneratedOperationQueue = true;
                            // I'll make the correction process of the Index.
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putLong(iWnnEngine.KEYNAME_DECO_OPERATION_EVENT_COUNT, index);
                            editor.putLong(key.toString(), index);
                            editor.commit();
                            return;  // error case
                        }
                    }

                    String readStr = null;
                    while((readStr = reader.readLine()) != null) {
                        String splitStr[] = readStr.split(DECO_OPERATION_SEPARATOR);
                        String event = null;
                        String id = null;
                        if (splitStr.length >= NUM_OPERATION_CATEGORY_DATA) {
                            event = splitStr[INDEX_OPERATION_EVENT];
                            id = splitStr[INDEX_OPERATION_ID];
                        } else {
                            // Because of incomplete data, ignored. (error case)
                            // Since the value of the Index is processed to prevent the shift,
                            // When the removal process is not always move, valid event (delete event, ID -1) to add to the queue.
                            Log.e(TAG, "iWnnEngine:regenerationOperationQueue() incomplete data!!");
                            event = String.valueOf(IDecoEmojiConstant.FLAG_DELETE);
                            id = String.valueOf(OPERATION_ID_INIT);
                        }
                        DecoEmojiAttrInfo addDecoemojiattrinfo = new DecoEmojiAttrInfo();
                        addDecoemojiattrinfo.setID(Integer.valueOf(id));
                        mOperationQueue.add(new DecoEmojiOperation(addDecoemojiattrinfo, Integer.valueOf(event)));
                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "iWnnEngine:regenerationOperationQueue() Exception1["+e+"]");
                    return;
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "iWnnEngine:regenerationOperationQueue() Exception2["+e+"]");
                    return;
                } catch (IOException e) {
                    Log.e(TAG, "iWnnEngine:regenerationOperationQueue() Exception3["+e+"]");
                    return;
                } finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "iWnnEngine:regenerationOperationQueue() Exception4["+e+"]");
                        return;
                    }
                }
            }
            mIsRegeneratedOperationQueue = true;
        }
    }

    /**
     * Clear OperationQueue.
     *
     * @return none
     */
    public void clearOperationQueue() {
        mOperationQueue.clear();
        mIsRegeneratedOperationQueue = false;
    }

    /**
     * Set enable head conversion flag.
     *
     * @param set set head conversion flag.
     */
    public void setEnableHeadConversion(boolean set) {
        mEnableHeadConv = set;
    }

    /**
     * Get enable head conversion.
     *
     * @return enable head conversion value.
     */
    private int getEnableHeadConversion() {
        int ret = IWnnCore.HEAD_CONVERSION_OFF;
        if (mEnableHeadConv) {
            ret = IWnnCore.HEAD_CONVERSION_ON;
        }
        return ret;
    }

    /**
     * Normalization User Dic.
     */
    private void normalizationUserDic() {
        if (mIsNormalizationUserDic) {
            return;
        }

        Context con = OpenWnn.superGetContext();
        if (con == null) {
            con = ControlPanelStandard.getCurrentControlPanel();
        }

        if (con == null) {
            Log.e(TAG, "normalizationUserDic() Fail to get context");
            return;
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(con);
        boolean wasNormalization = pref.getBoolean(KEY_NORMALIZATION_USER_DIC, false);
        if (wasNormalization) {
            return;
        }

        mIsNormalizationUserDic = true;
        String tempDirPath = mFilesDirPath;

        moveFiles(OLD_DIC_PATH, tempDirPath+DIC_DIRCTORY_NAME);

        mIsNormalizationUserDic = false;
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_NORMALIZATION_USER_DIC, true);
        editor.commit();
        close();
        init(tempDirPath);
    }

    /**
     * Move file.
     *
     * @param from The source file.
     * @param to   The destination file.
     */
    public void moveFiles(String from, String to) {
        File fromDir = new File(from);
        if (!fromDir.exists()) {
            return;
        }
        File[] fromFiles = fromDir.listFiles();
        if (fromFiles == null) {
            return;
        }


        boolean isSuccess = false;
        File toDir = new File(to);
        if (!toDir.exists()) {
            isSuccess = toDir.mkdir();
            if (!isSuccess) {
                Log.e(TAG, "iWnnEngine:moveFiles() Fail to mkdir destination directory");
                return;
            }
        }

        for (int i = 0; i < fromFiles.length; i++) {
            if (fromFiles[i].isDirectory()) {
                moveFiles(fromFiles[i].getPath(), to + "/" + fromFiles[i].getName());
            } else {
                File toFile = new File(toDir.getPath() + "/" + fromFiles[i].getName());
                if(toFile.exists()) {
                    isSuccess = toFile.delete();
                    if (!isSuccess) {
                        Log.e(TAG, "iWnnEngine:moveFiles() Fail to delete destination file");
                        continue;
                    }
                }
                isSuccess = fromFiles[i].renameTo(toFile);
                if (!isSuccess) {
                    Log.e(TAG, "iWnnEngine:moveFiles() Fail to renameTo source file");
                    continue;
                }
            }
        }

        isSuccess = fromDir.delete();
        if (!isSuccess) {
            Log.e(TAG, "iWnnEngine:moveFiles() Fail to delete source directory");
            return;
        }
    }

    /**
     * Set user directory path.
     *
     * @param set iWnn user data directory path.
     */
    public void setFilesDirPath(String dirPath) {
        mFilesDirPath = dirPath;
    }

    /**
     * Delete dictionary file.
     *
     * @param  file   delete file path
     * @return  {@code true} if Success; {@code false} if Failure.
     */
    public boolean deleteDictionaryFile(String file) {
        if (DEBUG) Log.d(TAG, "iWnnEngine::deleteDictionaryFile("+file+")");
        return mCore.deleteDictionaryFile(file);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#predict */
    /** Change String to ComposingText */
    public int predict(String str) {
        ComposingText compStr = new ComposingText();
        StrSegment tmp_str = new StrSegment(str);
        if (compStr.size(ComposingText.LAYER1) >= LIMIT_INPUT_NUMBER) {
            return -1;
        }
        compStr.insertStrSegment(ComposingText.LAYER0, ComposingText.LAYER1, tmp_str);
        this.mHCandidatesList = null;
        return predict(compStr, 0, -1);
    }

    public void setCandidates(String[] strs){
        if (strs != null) {
            mHCandidatesList = strs.clone();
        } else {
            mHCandidatesList = null;
        }
        mOutputNum = 0;
    }


}
