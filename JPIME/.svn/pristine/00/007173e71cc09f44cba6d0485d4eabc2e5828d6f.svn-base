/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/**
  * Copyright (C) 2012 NTT DOCOMO, INC. All Rights Reserved.
  */
package com.nttdocomo.android.voiceeditorif;

import android.app.Service;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import jp.co.omronsoft.android.emoji.EmojiAssist;
import jp.co.omronsoft.iwnnime.ml.IWnnMorpheme;
import jp.co.omronsoft.iwnnime.ml.IWnnServiceConnector;
import jp.co.omronsoft.iwnnime.ml.IWnnVoiceServiceConnector;
import jp.co.omronsoft.iwnnime.ml.OpenWnn;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiUtil;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;
import com.nttdocomo.android.voiceeditorif.IVoiceEditorService;

/**
 *  The Service of VoiceEditor.
 */
public class VoiceEditorService extends Service {

    /** for DEBUG */
    private static final boolean DEBUG = false;
    /** for DEBUG */
    private static final String TAG = "iWnn";

    /** Error code (common) */
    private static final int ERR_COMMON = -1;
    /** Success code (common) */
    private static final int SUCCESS_COMMON = 0;

    /** Initialize parameters. */
    public static final int INIT = 1;

    /** Type of Dictionary */
    public static final class DictionaryType {
        /** NormalDictionary  */
        public static final int DICTIONARY_TYPE_NORMAL = 0;
        /** LearningDictionary */
        public static final int DICTIONARY_TYPE_LEARNING = 1;
        /** UserDictionary */
        public static final int DICTIONARY_TYPE_USER = 2;
        /** Person'sNameDictionary  */
        public static final int DICTIONARY_TYPE_JINMEI = 3;
        /** PostalAddressDictionary  */
        public static final int DICTIONARY_TYPE_POSTAL_ADDRESS = 4;  
        /**
         * Default constructor
         */
        public DictionaryType() {
            super();
        }
    }

    /** Whether binding service. */
    private boolean mIsBind;

    /** EngineClient. */
    private static IWnnVoiceServiceConnector mServiceConnector = new IWnnVoiceServiceConnector();

    /** MorphemeeClient. */
    private static IWnnMorpheme mMorphemeConnector = new IWnnMorpheme();

    /** This instance */
    private static VoiceEditorService mCurrentService = null;

    private IWnnVoiceServiceConnector.OnConnectListener mListener = new IWnnVoiceServiceConnector.OnConnectListener() {
        public void onConnect() {
            if (DEBUG) Log.d(TAG, "OnConnectListener::onConnect()");
            mServiceConnector.init(INIT);
        }
        public void onDisconnect() {
            if (DEBUG) Log.d(TAG, "OnConnectListener::onDisconnect()");
        }
    };

    /** bind service */
    private ServiceConnection mServiceConn = new ServiceConnection() {
        /** @see android.content.ServiceConnection.onServiceConnected */
        public void onServiceConnected(ComponentName name, IBinder binder) {
        }

        /** @see android.content.ServiceConnection.onServiceDisconnected */
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    /**
     * Implementation of IVoiceEditorService.
     */
    private IVoiceEditorService.Stub mEngineService = new IVoiceEditorService.Stub() {
        /**
         * Disconnect from service.
         *
         * @return              Success:0  Failure:-1
         */
        public int disconnect() {
            if (DEBUG) Log.d(TAG, "VoiceEditorService::disconnect()");
            mMorphemeConnector.disconnect();
            return mServiceConnector.disconnect();
        }

        /**
         * Check the connector is still connecting the service.
         *
         * @return              Alive:true  NoAlive:false
         */
        public boolean isConnected() {
            if (DEBUG) Log.d(TAG, "VoiceEditorService::isConnected()");
            return mServiceConnector.isAlive();
        }

        /**
         * Initialize the internal state of the VoiceEditor.
         * <BR>For initialization
         * <ul>
         * <li>Result of the candidate.</li>
         * <li>Result of the prediction.</li>
         * <li>Result of the consecutive clauses.</li>
         * </ul>
         * @return              Success:0  Failure:-1
         */
        public int init(){
            if (DEBUG) Log.d(TAG, "VoiceEditorService::init()");
            if (!mServiceConnector.isAlive()) {
                Context context = mCurrentService;
                if (DEBUG) Log.d(TAG, "VoiceEditorService::init()" + "context::" + context + "mListener::" + mListener);               
                mServiceConnector.connect(context, mListener);
                mMorphemeConnector.connect(mCurrentService);
            }
         
            return mServiceConnector.init(INIT);
        }
        
        /**
         * Setting the search condition candidate.
         *
         * @param dictionaryType     Type of used dictionary.
         * @param emojiFilter        true if an emoji filter will be enable.
         * @param decoemojiFilter    true if an decoemoji filter will be enable.
         * @return                   Success:0 Failure:-1
         */
        public int setDictionary(int dictionaryType, boolean emojiFilter, boolean decoemojiFilter) {
            if (DEBUG) Log.d(TAG, "VoiceEditorService::setDictionary(" + dictionaryType + "," + emojiFilter 
                    + "," + decoemojiFilter + ")]");
            int engineDicType;
            
            switch (dictionaryType) {
            case DictionaryType.DICTIONARY_TYPE_NORMAL:
                engineDicType = iWnnEngine.SetType.NORMAL;
                break;
            case DictionaryType.DICTIONARY_TYPE_LEARNING:
                engineDicType = iWnnEngine.SetType.LEARNDIC;
                break;
            case DictionaryType.DICTIONARY_TYPE_USER:
                engineDicType = iWnnEngine.SetType.USERDIC;
                break;
            case DictionaryType.DICTIONARY_TYPE_JINMEI:
                engineDicType = iWnnEngine.SetType.JINMEI;
                break;
            case DictionaryType.DICTIONARY_TYPE_POSTAL_ADDRESS:
                engineDicType = iWnnEngine.SetType.POSTAL_ADDRESS;
                break;
            default:
                return ERR_COMMON;
            }

            int ret = mServiceConnector.setDictionaryDecoratedPict(null, 0, engineDicType, true, true,
                    emojiFilter, decoemojiFilter, false, false, false);

            return (ret >= 0 ? ret : ERR_COMMON);
        }

        /**
         * Make the prediction candidates.
         *
         * You can get prediction candidates by the engine based on
         * given reading strings.
         * Gets the strings of prediction candidates
         * by calling {@link #getCandidate(int)}.
         * To realize the function of wildcard prediction,
         * specify the values of minLen and maxLen of the desired input strings.
         * <p>For example,
         * <ul>
         *   <li>To get strings of at least and no more than 5 characters, set minLen and maxLen to 5. </li>
         *   <li>To get strings of more than 3 characters, set minLen to 3 and maxLen to -1.</li>
         * </ul>
         *
         * @param phonetic      The phonetic (YOMI) of word.
         * @param minLen        The minimum length of a word to predict.
         *                      (minLen <= 0  : no limit)
         * @param maxLen        The maximum length of a word to predict.
         *                      (maxLen <= -1 : no limit)
         * @return              Plus value if there are candidates;
         *                      0 if there is no candidate;
         *                      -1 if an error occurs.
         */
        public int searchCandidate(String phonetic, int minLen, int maxLen) {
            if (DEBUG) Log.d(TAG, "VoiceEditorService::searchCandidate(" + phonetic + "," + minLen 
                    + "," + maxLen + ")");
            int ret = mServiceConnector.predict(phonetic, minLen, maxLen);
            return (ret >= 0 ? ret : ERR_COMMON);
        }

        /**
         * Gets candidate strings for either prediction or conversion.
         *
         * Only gets the number of candidate strings specified in the parameters.
         * By repeatedly calling this API, you can get
         * the all the available candidates.
         * If there are no more candidates left, the API returns {@code null}.
         * The "candidate" and "stroke" will both be stored
         * as an array in {@link Bundle}.
         * Calls {@code Bundle#getStringArray("candidate")} to get the "reading",
         * and  set "stroke" as a parameter to get the "notation".
         * If the number specified in the parameters was larger than 
         * the available candidates,
         * the engine will only return the number of existent ones.
         *
         * @param maxCandidates         The maximum number of candidates
         *                                  returned by this method.
         * @return                      Candidate words.
         *                              If you want to get phonetic of word,
         *                              call Bundle#getStringArray("stroke").
         *                              Similarly, candidates are "candidate".
         *                              If there is no candidate words result,
         *                              'Bundle # getStringArray ("candidate")' return zero-length array.
         */
        public Bundle getCandidate(int maxCandidates) {
            if (DEBUG) Log.d(TAG, "VoiceEditorService::getCandidate(" + maxCandidates 
                + ")");
            Bundle bundle = mServiceConnector.getNextCandidateWithAnnotation(maxCandidates);
            int result = bundle.getInt("result");

            if (result >= 0) {
                bundle.putInt("result", SUCCESS_COMMON);
             } else {
                bundle.putInt("result", ERR_COMMON);
             }

            return bundle;
        }

        /**
         * Learn prediction candidates and consecutive clauses.
         *
         * The index of the parameter starts at 0 using the order
         * in which candidates were obtained by {@link getCandidate(int)}.
         * For example, if the third candidate obtained by getCandidate()
         * is to be learned, it will be set to index 2.
         *
         * @param index                 The index of words by calling "getCandidate(int)".
         * @param learnFlag             true if learning predictions candidate will be enable.
         * @param connectFlag           true if learning consecutive clauses will be enable.
         * @return                      Success:0  Failure:-1
         */
        public int memorizeCandidate(int index, boolean learnFlag, boolean connectFlag) {
            if (DEBUG) Log.d(TAG, "VoiceEditorService::memorizeCandidate(" + index + "," + learnFlag 
                    + "," + connectFlag + ")");
            int ret;
            if (learnFlag && connectFlag) {
                ret = mServiceConnector.learnCandidate(index);
                return (ret >= 0 ? ret : ERR_COMMON);

            } else if (!learnFlag && connectFlag) {
                ret = mServiceConnector.learnCandidateNoStore(index);
                return (ret >= 0 ? ret : ERR_COMMON);
                
            } else if (learnFlag && !connectFlag) {
                ret = mServiceConnector.learnCandidateNoConnect(index);
                return (ret >= 0 ? ret : ERR_COMMON);

            } else {
                return ERR_COMMON;
            }
        }

        /**
         * Add a word to the learning dictionary.
         *
         * Registers reading and notation into the Learning dictionary.
         * Executes a word registration by specifying the reading string into
         * "phonetic" and the notation string into "candidate".
         *
         * @param candidate     Notation of words to learn.
         * @param phonetic      phonetic of words to learn.
         * @param attribute     Attribute value of the word. Index of the lexical category group.
         * @param relation      Relation learning flag
         *                      (learning relations with the previous registered word.
         *                       0:don't learn 1:do learn)
         * @return              Success:0  Failure:-1
         */
        public int addWord(String candidate, String phonetic, int attribute, int relation) {
            if (DEBUG) Log.d(TAG, "VoiceEditorService::addWord(" + candidate + "," + phonetic + "," + attribute + ","
                    + relation + ")");
            int ret = mServiceConnector.addWordDetail(candidate, phonetic, attribute, iWnnEngine.DICTIONARY_TYPE_LEARN, relation);
            return (ret >= 0 ? ret : ERR_COMMON);
        }

        /**
         * Search words on the user dictionary.
         *
         * Get search results by calling {@link #getCandidate(int)}.
         * Search method is a matching front.
         *
         * @param phonetic      phonetic of word to search from dictionary.
         * @return              found:1  Not found:0  Failure:-1
         */
        public int searchWord(String phonetic) {
            if (DEBUG) Log.d(TAG, "VoiceEditorService::searchWord(" + phonetic
                    + ")");
            int ret = mServiceConnector.searchWordsDetail(phonetic, 
                    mServiceConnector.SEARCH_ORIGINAL_PULL_FRONT, mServiceConnector.ORDER_FREQUENCY);

            return (ret >= 0 ? ret : ERR_COMMON);
        }

        /**
         * Input start of "Deco Emoji".
         *
         * @return              Success:0  Failure:-1
         */
        public int startInputDecoEmoji() {
            if (DEBUG) Log.d(TAG, "VoiceEditorService::startInputDecoEmoji()");
            return mServiceConnector.startInput();
        }

        /**
         * Check a candidate word if it's on the pseudo dictionary.
         *
         * @param index         The index of words by {@link #getCandidate(int)}.
         * @return              true If the candidate is pseudo.
         *                      false If the candidate is not pseudo.
         */
        public boolean isPseudoCandidate(int index) {
            if (DEBUG) Log.d(TAG, "VoiceEditorService::isPseudoCandidate(" + index +")");
                return mServiceConnector.isGijiDic(index);
        }

        /**
         * Get IMESetting Information.
         *
         * @return              IMESetting Information.
         *                      If you want to get IMESetting Information,
         *                      call Bundle#getBoolean("keySound") or 
         *                      call Bundle#getBoolean("keyVibration") or 
         *                      call Bundle#getBoolean("previewPopup").
         */
        public Bundle getIMESettingInfo() {
            if (DEBUG) Log.d(TAG, "VoiceEditorService::getIMESettingInfo()");

            SharedPreferences settingPref = PreferenceManager.getDefaultSharedPreferences(mCurrentService);
            Bundle bundle = new Bundle();

            /* vibrator */
            try {
                bundle.putBoolean("keyVibration", settingPref.getBoolean("key_vibration", false));
            } catch (Exception ex) {
                if (DEBUG) Log.d(TAG, "NO VIBRATOR");
            }

            /* sound */
            try {
                bundle.putBoolean("keySound", settingPref.getBoolean("key_sound", true));
            } catch (Exception ex) {
                if (DEBUG) Log.d(TAG, "NO SOUND");
            }

            /* previewPopup */
            try {
                bundle.putBoolean("previewPopup", settingPref.getBoolean("popup_preview", true));
            } catch (Exception ex) {
               if (DEBUG)  Log.d(TAG, "NO POPUP");
            }

            return bundle;
        }

        /**
         * Get the result of Morphological analysis.
         *
         * @param input         String to morphological analysis.
         * @param readingsMax   Maximum count of getting readings.
         * @return              The result of Morphological analysis.
         * <BR>For values
         * <ul>
         * <li>Calls {@code Bundle#getInt("result")} to get the Result of the processing.</li>
         * <li>Calls {@code Bundle#getStringArray("strings")} to get the Notation of words.</li>
         * <li>Calls {@code Bundle#getStringArray("readings")} to get the readings.</li>
         * <li>Calls {@code Bundle#getShortArray("wordclasses")} to get the attributes.</li>
         * </ul>
         */
        public Bundle splitWord(String input, int readingsMax) {
            if (DEBUG) Log.d(TAG, "VoiceEditorService::splitWord(" + input + "," + readingsMax 
                    + ")");

            Bundle bundle = mMorphemeConnector.splitWord(input, readingsMax);
            if (bundle != null) {
                bundle.putInt("result", SUCCESS_COMMON);
                return bundle;
            } else {
                bundle = new Bundle();
                bundle.putInt("result", ERR_COMMON);
            }
            return bundle; 
        }
    };

    /**
     * Default Constructor.
     */
    public VoiceEditorService() {}


    /**
     * @see android.app.Service.onCreate
     */
    @Override public void onCreate() {
        super.onCreate();
        mCurrentService = this;
    }

    /**
     * @see android.app.Service.onDestroy
     */
    @Override public void onDestroy() {
        mCurrentService = null;
    }

    /**
     * Bind to the Service of iWnnEngine.
     *
     * @param intent        Intent.
     * @return              Stub.
     * @see android.app.Service.onBind
     */
    @Override public IBinder onBind(Intent intent) {
        DecoEmojiUtil.setConvertFunctionEnabled(false);
        EmojiAssist assist = EmojiAssist.getInstance();
        if (assist != null) {
            int functype = assist.getEmojiFunctionType();
            if (functype != 0) {
                DecoEmojiUtil.setConvertFunctionEnabled(true);
            }
        }
        OpenWnn.copyPresetDecoEmojiGijiDictionary();
        return mEngineService;
    }

    /**
     * Unbind to the Service.
     *
     * @param intent        Intent.
     * @return              Success:true  Failure:false
     * @see android.app.Service.onUnbind
     */
    @Override public boolean onUnbind(Intent intent) {
        if (mIsBind) {
            unbindService(mServiceConn);
            mIsBind = false;
        }
        return super.onUnbind(intent);
    }

}
