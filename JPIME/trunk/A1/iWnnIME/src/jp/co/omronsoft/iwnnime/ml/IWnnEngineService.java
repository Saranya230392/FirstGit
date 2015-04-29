/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;
///////////////// iwnn_trial_add_0

import android.app.Service;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.IDecoEmojiManager;
import jp.co.omronsoft.android.emoji.EmojiAssist;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiOperationQueue;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiUtil;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;
import jp.co.omronsoft.iwnnime.ml.OpenWnn;

import java.util.ArrayList;
import java.util.HashSet;
///////////////// iwnn_trial_add_1

/**
 *  The Service of iWnnEngine.
 */
public class IWnnEngineService extends Service {
///////////////// iwnn_trial_add_2

    /** for DEBUG */
    private static final boolean DEBUG = false;
    /** for DEBUG */
    private static final String TAG = "iWnn";
    /** Number of array elements to store the acquired candidate. */
    private static final int RESULTLIST_ARRAY = 2;

    /** Password Max Length */
    private static final int PASSWORD_MAX_LENGTH = 16;

    /** Status of Engine (unconnection) */
    private static final int ENGINE_UNCONNECTION = 0;
    /** Status of Engine (unsetting) */
    private static final int ENGINE_UNSETTING = 1;
    /** Status of Engine (initialize) */
    private static final int ENGINE_INITIALIZE = 2;
    /** Status of Engine (search) */
    private static final int ENGINE_SEARCH = 3;
    /** Status of Engine (pre-fixation) */
    private static final int ENGINE_PREFIXATION = 4;
    /** Status of Engine (pre-fixation and search) */
    private static final int ENGINE_PREFIXATION_AND_SEARCH = 5;
    /** Status of Engine (userdic/learn initialize) */
    private static final int ENGINE_INITIALIZE_REGISTRATION_DICTIONARY = 6;
    /** Status of Engine (userdic/learn search) */
    private static final int ENGINE_SEARCH_REGISTRATION_DICTIONARY = 7;

    /** Function (connect) */
    private static final int FUNCTION_CONNECT = 0;
    /** Function (disconnect) */
    private static final int FUNCTION_DISCONNECT = 1;
    /** Function (init(INIT)) */
    private static final int FUNCTION_INIT_INIT = 2;
    /** Function (init(BREAK_SEQUENCE)) */
    private static final int FUNCTION_INIT_BREAK = 3;
    /** Function (setDictionary, setDictionaryDecoratedPict) */
    private static final int FUNCTION_SETDICTIONARY = 4;
    /** Function (setNormalDictionary) */
    private static final int FUNCTION_SETNORMALDICTIONARY = 5;
    /** Function (setUserDictionary) */
    private static final int FUNCTION_SETUSERDICTIONARY = 6;
    /** Function (setLearnDictionary) */
    private static final int FUNCTION_SETLEARNDICTIONARY = 7;
    /** Function (predict, setEnableConsecutivePhraseLevelConversion) */
    private static final int FUNCTION_PREDICT = 8;
    /** Function (convert, convertWithAnnotation) */
    private static final int FUNCTION_CONVERT = 9;
    /** Function (getNextCandidate, getNextCandidateWithAnnotation) */
    private static final int FUNCTION_GETNEXTCANDIDATE = 10;
    /** Function (learnCandidate, learnCandidateNoStore, learnCandidateNoConnect) */
    private static final int FUNCTION_LEARNCANDIDATE = 11;
    /** Function (learnWord, learnWordNoStore, learnWordNoConnect) */
    private static final int FUNCTION_LEARNWORD = 12;
    /** Function (addWord, addWordDetail) */
    private static final int FUNCTION_ADDWORD = 13;
    /** Function (searchWords, searchWordsDetail) */
    private static final int FUNCTION_SEARCHWORDS = 14;
    /** Function (deleteWord) */
    private static final int FUNCTION_DELETEWORD = 15;
    /** Function (writeoutDictionary) */
    private static final int FUNCTION_WRITEOUTDICTIONARY = 16;
    /** Function (initializeDictionary) */
    private static final int FUNCTION_INITIALIZEDICTIONARY = 17;
    /** Function (undo) */
    private static final int FUNCTION_UNDO = 18;
    /** Function (startInput) */
    private static final int FUNCTION_STARTINPUT = 19;
    /** Function (isGijiDic) */
    private static final int FUNCTION_IS_GIJI_DIC = 20;

    /** Error code (invalid state) */
    private static final int ERR_INVALID_STATE = -127;
    /** Error code (no dictionary) */
    private static final int ERR_NO_DICTIONARY = -126;
    /** Error code (common) */
    private static final int ERR_COMMON = -1;

    /** Message for {@code mHandler} (updating the DecoEmoji dictionary) */
    private static final int MSG_UPDATE_DECOEMOJI_DICTIONARY = 1;

    /** Message for {@code mHandler} (delete decoemoji the learning dictionary) */
    private static final int MSG_DELETE_DECOEMOJI_LEARNING_DICTIONARY = 2;

    /** Delay time(msec.) updating the DecoEmoji dictionary. */
    private static final int DELAY_MS_UPDATE_DECOEMOJI_DICTIONARY = 0;

    /** Delay time(msec.) delete decoemoji the learning dictionary. */
    private static final int DELAY_MS_DELETE_DECOEMOJI_LEARNING_DICTIONARY = 0;

    /** Status check table */
    private static final boolean ENGINE_STATUS_CHECK_TABLE[][] = {
        {true,false,false,false,false,false,false,false},   //connect
        {false,true,true,true,true,true,true,true},         //disconnect
        {false,true,true,true,true,true,true,true},         //init(INIT)
        {false,false,true,true,true,true,false,false},      //init(BREAK_SEQUENCE)
        {false,true,true,true,true,true,true,true},         //setDictionary, setDictionaryDecoratedPict ,setGijiFilter
        {false,false,true,true,true,true,true,true},        //setNormalDictionary
        {false,false,true,true,true,true,true,true},        //setUserDictionary
        {false,false,true,true,true,true,true,true},        //setLearnDictionary
        {false,false,true,true,true,true,false,false},      //predict, setEnableConsecutivePhraseLevelConversion
        {false,false,true,true,true,true,false,false},      //convert, convertWithAnnotation
        {false,false,false,true,false,true,false,true},     //getNextCandidate, getNextCandidateWithAnnotation
        {false,false,false,true,false,true,false,false},    //learnCandidate, learnCandidateNoStore, learnCandidateNoConnect
        {false,false,true,true,true,true,false,false},      //learnWord, learnWordNoStore, learnWordNoConnect
        {false,false,false,false,false,false,true,true},    //addWord, addWordDetail
        {false,false,false,false,false,false,true,true},    //searchWords, searchWordsDetail
        {false,false,false,false,false,false,true,true},    //deleteWord
        {false,false,false,false,false,false,true,true},    //writeoutDictionary
        {false,false,false,false,false,false,true,true},    //initializeDictionary
        {false,false,false,true,true,true,false,false},     //undo
        {false,false,true,true,true,true,true,true},        //startInput
        {false,false,false,true,false,true,false,false}     //isGijiDic
    };

    private static class EngineService {
        /** iWnn Engine. */
        private iWnnEngine mEngine = iWnnEngine.getEngineForService("");

        /** Candidate List. */
        private ArrayList<WnnWord> mWnnWordArray = new ArrayList<WnnWord>();

        /** When converting [true]*/
        private boolean mIsConverting = false;

        /** Segment position. */
        private int mSegment = 0;

        /** Engine Status */
        private int mEngineStatus = ENGINE_UNSETTING;

        /** Flexible charset init */
        private boolean mIsFlexibleCharsetInit = true;

        /** Flexible Search */
        private boolean mFlexibleSearch = false;

        /** Ten key type */
        private boolean mTenKeyType = false;

        /** Error code */
        private int mErrorCode = 0;

        /** Connected package name */
        private String mConnectedPackage = "";

        /** Package Password */
        private String mPassword = "";

        /** Used Time */
        private long mUsedTime = 0;

        /** Configuration file name */
        private String mConfigurationFile = "";

        /** Map of cached dictionaries */
        private HashSet<Integer> mCachedDicSet = new HashSet<Integer>();
    };
    private static ArrayList<EngineService> mEngineServiceArray = new ArrayList<EngineService>();

    /** Whether binding service. */
    private boolean mIsBind;

    /** Context. */
    private Context mContext = null;

    /** This instance */
    private static IWnnEngineService mCurrentService = null;

    /** iWnn user data directory path */
    private String mFilesDirPath = null;

    /** {@code Handler} for updating the DecoEmoji dictionary */
    Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            Bundle  bundle = msg.getData();
            String packageName = bundle.getString("packageName");
            boolean result = false;
            int     delay  = -1;
            switch (msg.what) {
            case MSG_UPDATE_DECOEMOJI_DICTIONARY:
                result = DecoEmojiOperationQueue.getInstance().executeOperation(packageName, mContext);
                delay  = DELAY_MS_UPDATE_DECOEMOJI_DICTIONARY;
                break;

            case MSG_DELETE_DECOEMOJI_LEARNING_DICTIONARY:
                iWnnEngine engine = iWnnEngine.getEngineForService(packageName);
                if (engine != null) {
                    result = engine.executeOperation(mCurrentService);
                    delay  = DELAY_MS_DELETE_DECOEMOJI_LEARNING_DICTIONARY;
                }
                break;

            default:
                break;
            }

            if (result) {
                Message message = mHandler.obtainMessage(msg.what);
                bundle = new Bundle();
                bundle.putString("packageName", packageName);
                message.setData(bundle);
                mHandler.sendMessageDelayed(message, delay);
            }
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
     * Implementation of IEngineService.
     */
    private IEngineService.Stub mEngineService = new IEngineService.Stub() {
            /**
             * Initialize the internal state of the iWnn engine.
             *
             * @param packageName   PackageName.
             * @param initLevel     How to initialize.
             *                      INIT:Initialize parameters.
             *                      BREAK_SEQUENCE:Break the sequence of words.
             */
            public void init(String packageName, String password ,int initLevel) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::init(" + initLevel + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        if (initLevel == IWnnServiceConnector.INIT) {
                            long oldtime = 0;
                            String removecomoponentName = "";
                            for (int i = 0; i < mEngineServiceArray.size(); i++) {
                                if (mEngineServiceArray.get(i).mUsedTime < oldtime || i == 0) {
                                    oldtime = mEngineServiceArray.get(i).mUsedTime;
                                    removecomoponentName = mEngineServiceArray.get(i).mConnectedPackage;
                                    }
                                }
                            clearServiceInfo(removecomoponentName);
                            engineService = getEngineService(packageName);
                            if (engineService == null ) {
                                return;
                            }
                        }
                        else {
                            return;
                        }
                    }
                    switch (initLevel) {
                    case IWnnServiceConnector.INIT:
                        if (password.length() > PASSWORD_MAX_LENGTH) {
                            engineService.mPassword = password.substring(0, PASSWORD_MAX_LENGTH);
                        } else {
                            engineService.mPassword = password;
                        }
                        if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_INIT_INIT][engineService.mEngineStatus]) {
                            stopDecoEmojiUpdating();
                            engineService.mEngine.close();
                            engineService.mEngine.init(mFilesDirPath);
                            engineService.mIsFlexibleCharsetInit = true;
                            engineService.mIsConverting = false;
                            engineService.mEngineStatus = ENGINE_UNSETTING;
                            engineService.mCachedDicSet.clear();
                        }
                        break;

                    case IWnnServiceConnector.BREAK_SEQUENCE:
                        if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_INIT_BREAK][engineService.mEngineStatus]) {
                            engineService.mEngine.breakSequence();
                            engineService.mEngine.init(mFilesDirPath);
                            engineService.mIsConverting = false;
                            engineService.mEngineStatus = ENGINE_INITIALIZE;
                        }
                        break;

                    default:
                        break;

                    }
                }
            }

           /**
             * Make the prediction candidates.
             * You can get prediction candidates by the iWnn engine based on
             * given reading strings.
             * Gets the strings of prediction candidates
             * by calling *getNextCandidate().
             * To realize the function of wildcard prediction,
             * specify the values of minLen and maxLen of
             * the desired input strings.
             * For example,
             * - To get strings of at least and no more than 5 characters,
             * set minLen and maxLen to 5.
             * - To get strings of more than 3 characters,
             * set minLen to 3 and maxLen to -1.
             *
             * @param packageName   PackageName.
             * @param stroke        The input stroke.
             * @param minLen        The minimum length of a word to predict.
             *                      (minLen <= 0  : no limit)
             * @param maxLen        The maximum length of a word to predict.
             *                      (maxLen <= -1 : no limit)
             * @return              Plus value if there are candidates;
             *                      0 if there is no candidate;
             *                      Minus value if an error occurs.
             */
            public int predict(String packageName, String stroke, int minLen, int maxLen) {
///////////////// iwnn_trial_add_3

                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::predict(" + stroke + "," + minLen + "," + maxLen + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return ERR_INVALID_STATE;
                    }
                    int result = -1;
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_PREDICT][engineService.mEngineStatus]) {
                        ComposingText composingText = new ComposingText();
                        composingText.insertStrSegment(ComposingText.LAYER0,
                                            ComposingText.LAYER1,
                                            new StrSegment(stroke));
                        engineService.mWnnWordArray.clear();
                        engineService.mIsConverting = false;
                        result = engineService.mEngine.predict(composingText, minLen, maxLen);
                        if (engineService.mEngineStatus == ENGINE_PREFIXATION) {
                            engineService.mEngineStatus = ENGINE_PREFIXATION_AND_SEARCH;
                        } else {
                            engineService.mEngineStatus = ENGINE_SEARCH;
                        }
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            result = ERR_NO_DICTIONARY;
                        } else {
                            result = ERR_INVALID_STATE;
                        }
                    }
                    return result;
                }
            }


            /**
            * Set enable consecutive phrase level conversion.
            *
            * Set whether to implement the consecutive phrase level conversion.
            * If you do not call this API once, disable consecutive phrase level conversion.
            *
            * @param packageName   PackageName.
            * @param enable     {@code true}  Enable consecutive phrase level conversion.
            *                   {@code false} Disable consecutive phrase level conversion.
            * @return           0 if success setting.
            *                   Minus value if an error occurs.
            */
            public int setEnableConsecutivePhraseLevelConversion(String packageName, boolean enable) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::setEnableConsecutivePhraseLevelConversion(" + enable + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return ERR_INVALID_STATE;
                    }
                    int result = 0;
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_PREDICT][engineService.mEngineStatus]) {
                        engineService.mEngine.setEnableHeadConversion(enable);
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            result = ERR_NO_DICTIONARY;
                        } else {
                            result = ERR_INVALID_STATE;
                        }
                    }
                    return result;
                }
            }

           /**
             * Gets candidate strings for either prediction or conversion.
             * Only gets the number of candidate strings
             * specified in the parameters.
             * By repeatedly calling this API, you can get
             * the all the available candidates.
             * If there are no more candidates left, the API returns *null*.
             * The "reading" and "notation" will both be stored
             * as an array in *Bundle*.
             * Calls *Bundle#getStringArray("stroke")* to get the "reading",
             * and  set "candidate" as a parameter to get the "notation".
             * If the number specified in the parameters was larger than
             * the available candidates,
             * the iWnn engine will only return the number of existent ones.
             *
             * @param packageName         PackageName.
             * @param numberOfCandidates  Get the number of candidates.
             * @return                    Candidate.(Reading and word)
             */
            public Bundle getNextCandidate(String packageName, int numberOfCandidates) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::getNextCandidate(" + numberOfCandidates + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        Bundle ret = new Bundle();
                        ret.putInt("result", ERR_COMMON);
                        return ret;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_GETNEXTCANDIDATE][engineService.mEngineStatus]) {
                        if (numberOfCandidates <= 0) {
                            Bundle ret = new Bundle();
                            ret.putInt("result", ERR_COMMON);
                            return ret;
                        }
                        ArrayList<String>[] resultLists =
                                (ArrayList<String>[]) new ArrayList[RESULTLIST_ARRAY];
                        int attributes[] = new int[numberOfCandidates];
                        for (int i = 0; i < (RESULTLIST_ARRAY); i++) {
                            resultLists[i] = new ArrayList<String>();
                        }
                        for (int i = 0; i < numberOfCandidates; i++) {
                            WnnWord word = engineService.mEngine.getNextCandidate();
                            if (word == null) {
                                break;
                            }
                            engineService.mWnnWordArray.add(word);
                            resultLists[0].add(word.candidate);
                            resultLists[1].add(word.stroke);
                            attributes[i] = word.attribute;
                        }

                        // Store result to Bundle
                        Bundle candidateList = new Bundle();
                        int size = resultLists[0].size();
                        candidateList.putStringArray("candidate",
                                        resultLists[0].toArray(new String[size]));
                        candidateList.putStringArray("stroke",
                                        resultLists[1].toArray(new String[size]));
                        candidateList.putIntArray("attributes", attributes);
                        return candidateList;
                    } else {
                        Bundle ret = new Bundle();
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            ret.putInt("result", ERR_NO_DICTIONARY);
                        } else {
                            ret.putInt("result", ERR_INVALID_STATE);
                        }
                        return ret;
                    }
                }
            }

           /**
             * Gets candidate strings and DecoEmoji text (Annotation is set to strings)
             * for either prediction or conversion.
             * Only gets the number of candidate strings
             * specified in the parameters.
             * By repeatedly calling this API, you can get
             * the all the available candidates.
             * If there are no more candidates left, the API returns *null*.
             * The "reading" and "notation" will both be stored
             * as an array in *Bundle*.
             * Calls *Bundle#getStringArray("stroke")* to get the "reading",
             * and  set "candidate" as a parameter to get the "notation".
             * If the number specified in the parameters was larger than
             * the available candidates,
             * the iWnn engine will only return the number of existent ones.
             *
             * @param packageName         PackageName.
             * @param numberOfCandidates  Get the number of candidates.
             * @return                    Candidate.(Reading and word)
             */
            public Bundle getNextCandidateWithAnnotation(String packageName, int numberOfCandidates) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::getNextCandidateWithAnnotation("
                            + numberOfCandidates + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        Bundle ret = new Bundle();
                        ret.putInt("result", ERR_COMMON);
                        return ret;
                    }
                    Bundle ret = getNextCandidate(packageName, numberOfCandidates);
                    String[] candidateList = ret.getStringArray("candidate");
                    if ((candidateList != null) && (candidateList.length > 0)) {
                        CharSequence[] decoText = new CharSequence[candidateList.length];
                        int[] decoTextStatus = new int[candidateList.length];

                        for (int i = 0; i < candidateList.length; i++) {
                            int[] status = new int[1];
                            decoText[i] = DecoEmojiUtil.getSpannedText(
                                    candidateList[i], status);
                            decoTextStatus[i] = status[0];
                        }
                        ret.putCharSequenceArray("annotation_candidate", decoText);
                        ret.putIntArray("annotation_result", decoTextStatus);
                    }
                    return ret;
                }
            }

            /**
             * Gets candidate strings and DecoEmoji text (Annotation is set to strings)
             * for either prediction or conversion.
             * Only gets the number of candidate strings
             * specified in the parameters.
             * By repeatedly calling this API, you can get
             * the all the available candidates.
             * If there are no more candidates left, the API returns *null*.
             * The "reading" and "notation" will both be stored
             * as an array in *Bundle*.
             * Calls *Bundle#getStringArray("stroke")* to get the "reading",
             * and  set "candidate" as a parameter to get the "notation".
             * If the number specified in the parameters was larger than
             * the available candidates,
             * the iWnn engine will only return the number of existent ones.
             *
             * @param packageName         PackageName.
             * @param numberOfCandidates  Get the number of candidates.
             * @param emojitype           Emoji type
             * @return                    Candidate.(Reading and word)
             */
            public Bundle getNextCandidateWithAnnotation2(String packageName, int numberOfCandidates, int emojitype) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::getNextCandidateWithAnnotation2("
                            + numberOfCandidates + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        Bundle ret = new Bundle();
                        ret.putInt("result", ERR_COMMON);
                        return ret;
                    }
                    OpenWnn.setEmojiType(emojitype);
                    Bundle ret = getNextCandidateWithAnnotation(packageName, numberOfCandidates);
                    return ret;
                }
            }

           /**
             * Learns the prediction candidate obtained
             * by *getNextCandidate()*.
             * The index of the parameter starts at 1 using the order
             * in which candidates were obtained by *getNextCandidate()*.
             * For example, if the third candidate obtained by
             * getNextCandidate() is to be learned, it will be set to index 3.
             *
             * @param packageName   PackageName.
             * @param index         Index of words to learn.
             * @return              Success:true Failure:false
             */
            public boolean learnCandidate(String packageName, int index) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::learnCandidate(" + index + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return false;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_LEARNCANDIDATE][engineService.mEngineStatus]) {
                        if ((engineService.mWnnWordArray.size() <= index) || (index < 0)) {
                            return false;
                        }
                        boolean result = learn(packageName, engineService.mWnnWordArray.get(index), true);
                        engineService.mErrorCode = result ? 0 : ERR_COMMON;
                        return result;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            engineService.mErrorCode = ERR_NO_DICTIONARY;
                        } else {
                            engineService.mErrorCode = ERR_INVALID_STATE;
                        }
                        return false;
                    }
                }
            }

            /**
             * Convert the strings.
             *
             * The iWnn Engine performs a conversion by using the specified reading
             * strings.
             * The result is obtained by getNextCandidate()
             * Convert the specified string into "stroke" and returns the number of
             * phrases.
             * If there are no candidates it returns 0.
             *
             * @param packageName   PackageName.
             * @param stroke        The stroke (YOMI) of word.
             * @param divide        Divide position.
             * @return              Candidate words.
             *                      If you want to get strokes of word,
             *                      call Bundle#getStringArray("stroke").
             *                      Similarly, candidates are "candidate".
             */
            public Bundle convert(String packageName, String stroke, int divide) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::convert(" + stroke + "," + divide + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        Bundle ret = new Bundle();
                        ret.putInt("result", ERR_COMMON);
                        return ret;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_CONVERT][engineService.mEngineStatus]) {
                        if (stroke == null) {
                            Bundle ret = new Bundle();
                            ret.putInt("result", ERR_COMMON);
                            return ret;
                        }
                        ComposingText composingText = new ComposingText();

                        String[] strSpilit = stroke.split("");
                        int length = stroke.split("").length;
                        length--;
                        for (int i = 0; i < length; i++) {
                            composingText.insertStrSegment(ComposingText.LAYER0,
                                                              ComposingText.LAYER1,
                                                              new StrSegment(strSpilit[i + 1]));
                        }
                        composingText.setCursor(ComposingText.LAYER1, divide);
                        int result = engineService.mEngine.convert(composingText);
                        // Store result to Bundle
                        Bundle bundle = new Bundle();
                        bundle.putInt("result", result);
                        if (result > 0) {
                            ArrayList<String> candidates = (ArrayList<String>) new ArrayList();
                            ArrayList<String> strokes = (ArrayList<String>) new ArrayList();
                            for (int i = 0; i < result; i++) {
                                StrSegment candidate = composingText.getStrSegment(ComposingText.LAYER2, i);
                                if (candidate != null) {
                                    candidates.add(candidate.string);
                                    strokes.add(composingText.toString(ComposingText.LAYER1,
                                                                        candidate.from, candidate.to));
                                }
                            }
                            bundle.putStringArray("candidate",candidates.toArray(new String[result]));
                            bundle.putStringArray("stroke",strokes.toArray(new String[result]));
                            engineService.mIsConverting = true;
                            engineService.mWnnWordArray.clear();
                            engineService.mSegment = 0;
                        }
                        if (engineService.mEngineStatus == ENGINE_PREFIXATION) {
                            engineService.mEngineStatus = ENGINE_PREFIXATION_AND_SEARCH;
                        } else {
                            engineService.mEngineStatus = ENGINE_SEARCH;
                        }
                        return bundle;
                    } else {
                        Bundle ret = new Bundle();
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            ret.putInt("result", ERR_NO_DICTIONARY);
                        } else {
                            ret.putInt("result", ERR_INVALID_STATE);
                        }
                        return ret;
                    }
                }
            }

            /**
            * Convert the strings and DecoEmoji text (Annotation is set to strings).
             *
             * The iWnn Engine performs a conversion by using the specified reading
             * strings.
             * The result is obtained by getNextCandidate()
             * Convert the specified string into "stroke" and returns the number of
             * phrases.
             * If there are no candidates it returns 0.
             *
             * @param packageName   PackageName.
             * @param stroke        The stroke (YOMI) of word.
             * @param divide        Divide position.
             * @return              Convert words.
             *                      If you want to get strokes of word,
             *                      call Bundle#getStringArray("stroke").
             *                      Similarly, candidates are "candidate",
             *                      and DecoEmoji text (Annotation is set to strings)
             *                      are "annotation_candidate".
             */
            public Bundle convertWithAnnotation(String packageName, String stroke, int divide) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::convertWithAnnotation(" + stroke + "," + divide + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        Bundle ret = new Bundle();
                        ret.putInt("result", ERR_COMMON);
                        return ret;
                    }
                    Bundle ret = convert(packageName, stroke, divide);
                    String[] candidateList = ret.getStringArray("candidate");
                    if ((candidateList != null) && (candidateList.length > 0)) {
                        CharSequence[] decoText = new CharSequence[candidateList.length];
                        int[] decoTextStatus = new int[candidateList.length];

                        for (int i = 0; i < candidateList.length; i++) {
                            int[] status = new int[1];
                            decoText[i] = DecoEmojiUtil.getSpannedText(
                                    candidateList[i], status);
                            decoTextStatus[i] = status[0];
                        }
                        ret.putCharSequenceArray("annotation_candidate", decoText);
                        ret.putIntArray("annotation_result", decoTextStatus);
                    }
                    return ret;
                }
            }

            /**
            * Convert the strings and DecoEmoji text (Annotation is set to strings).
             *
             * The iWnn Engine performs a conversion by using the specified reading
             * strings.
             * The result is obtained by getNextCandidate()
             * Convert the specified string into "stroke" and returns the number of
             * phrases.
             * If there are no candidates it returns 0.
             *
             * @param packageName   PackageName.
             * @param stroke        The stroke (YOMI) of word.
             * @param divide        Divide position.
             * @param emojitype     Emoji type
             * @return              Convert words.
             *                      If you want to get strokes of word,
             *                      call Bundle#getStringArray("stroke").
             *                      Similarly, candidates are "candidate",
             *                      and DecoEmoji text (Annotation is set to strings)
             *                      are "annotation_candidate".
             */
            public Bundle convertWithAnnotation2(String packageName, String stroke, int divide, int emojitype) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::convertWithAnnotation2(" + stroke + "," + divide + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        Bundle ret = new Bundle();
                        ret.putInt("result", ERR_COMMON);
                        return ret;
                    }
                    OpenWnn.setEmojiType(emojitype);
                    Bundle ret = convertWithAnnotation(packageName, stroke, divide);
                    return ret;
                }
            }

           /**
             * Registers a word into either the User dictionary or the Learning
             * dictionary.
             *
             * Registers reading and notation into either the User dictionary or the
             * Learning dictionary.
             * Executes a word registration by specifying the reading string into
             * "stroke" and the notation string into "candidate".
             *
             * @param packageName  PackageName.
             * @param candidate    Notation of words to learn.
             * @param stroke       Stroke of words to learn.
             * @param hinsi        index of the lexical category group
             * @param type         type of dictionaries(0:user dictionary, 1:learning dictionary, 2:pseudo dictionary)
             * @param relation     relation learning flag(learning relations with the previous registered word. 0:don't learn 1:do learn)
             * @return             Success:0  Failure:minus
             */
            public int addWordDetail(String packageName, String candidate, String stroke, int hinsi, int type, int relation) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::addWord("
                                             + candidate + "," + stroke + "," + hinsi + "," + type + "," + relation
                                             + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return ERR_INVALID_STATE;
                    }
                    int result = -1;
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_ADDWORD][engineService.mEngineStatus]) {
                        WnnWord word = new WnnWord();
                        word.candidate = candidate;
                        word.stroke = stroke;
                        word.attribute |= iWnnEngine.WNNWORD_ATTRIBUTE_SERVICE_WORD_NO_ENGINE;
                        int dictionary = engineService.mEngine.getDictionary();
                        if (dictionary == iWnnEngine.SetType.USERDIC) {
                            result = engineService.mEngine.addWord(word,hinsi,type,relation);
                        } else if (dictionary == iWnnEngine.SetType.LEARNDIC) {
                            if (learn(packageName, word, true)) {
                                result = 0;
                            } else {
                                result = -1;
                            }
                        }
                        engineService.mEngineStatus = ENGINE_INITIALIZE_REGISTRATION_DICTIONARY;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            result = ERR_NO_DICTIONARY;
                        } else {
                            result = ERR_INVALID_STATE;
                        }
                    }
                    return result;
                }
            }

            /**
             * Registers a word into either the User dictionary or the Learning
             * dictionary.
             *
             * Registers reading and notation into either the User dictionary or the
             * Learning dictionary.
             * Executes a word registration by specifying the reading string into
             * "stroke" and the notation string into "candidate".
             *
             * @param packageName   PackageName.
             * @param candidate     Notation of words to learn.
             * @param stroke        Stroke of words to learn.
             * @return              Success:0  Failure:minus
             */
            public int addWord(String packageName, String candidate, String stroke) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::addWord(" + candidate + "," + stroke + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return ERR_INVALID_STATE;
                    }
                    int result = -1;
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_ADDWORD][engineService.mEngineStatus]) {
                        WnnWord word = new WnnWord();
                        word.candidate = candidate;
                        word.stroke = stroke;
                        word.attribute |= iWnnEngine.WNNWORD_ATTRIBUTE_SERVICE_WORD_NO_ENGINE;
                        int dictionary = engineService.mEngine.getDictionary();
                        if (dictionary == iWnnEngine.SetType.USERDIC) {
                            result = engineService.mEngine.addWord(word);
                        } else if (dictionary == iWnnEngine.SetType.LEARNDIC) {
                            if (learn(packageName, word, true)) {
                                result = 0;
                            } else {
                                result = -1;
                            }
                        }
                        engineService.mEngineStatus = ENGINE_INITIALIZE_REGISTRATION_DICTIONARY;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            result = ERR_NO_DICTIONARY;
                        } else {
                            result = ERR_INVALID_STATE;
                        }
                    }
                    return result;
                }
            }

            /**
             * Searches for words from the User dictionary or the Learning dictionary.
             *
             * Executes by specifying a reading string into "stroke".
             * The result is obtained by getNextCandidate()
             * All words can be searched for if an empty string is specified as the
             * arguments.
             *
             * @param packageName  PackageName.
             * @param stroke       Stroke of word to search from dictionary.
             * @param method       way of searching the dictionary
             * @param order        order of searching the dictionary
             * @return             found:1  Not found:0  Failure:minus
             */
            public int searchWordsDetail(String packageName, String stroke, int method, int order) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::searchWords("
                                              + stroke + "," + method + "," + order
                                              + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return ERR_INVALID_STATE;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_SEARCHWORDS][engineService.mEngineStatus]) {
                        engineService.mWnnWordArray.clear();
                        engineService.mSegment = 0;
                        engineService.mEngineStatus = ENGINE_SEARCH_REGISTRATION_DICTIONARY;
                        return engineService.mEngine.searchWords(stroke,method,order);
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            return ERR_NO_DICTIONARY;
                        } else {
                            return ERR_INVALID_STATE;
                        }
                    }
                }
            }

            /**
             * Searches for words from the User dictionary or the Learning dictionary.
             *
             * Executes by specifying a reading string into "stroke".
             * The result is obtained by getNextCandidate()
             * All words can be searched for if an empty string is specified as the
             * arguments.
             *
             * @param packageName   PackageName.
             * @param stroke        Stroke of word to search from dictionary.
             * @return              found:1  Not found:0  Failure:minus
             */
            public int searchWords(String packageName, String stroke) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::searchWords(" + stroke + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return ERR_INVALID_STATE;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_SEARCHWORDS][engineService.mEngineStatus]) {
                        engineService.mWnnWordArray.clear();
                        engineService.mSegment = 0;
                        engineService.mEngineStatus = ENGINE_SEARCH_REGISTRATION_DICTIONARY;
                        return engineService.mEngine.searchWords(stroke);
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            return ERR_NO_DICTIONARY;
                        } else {
                            return ERR_INVALID_STATE;
                        }
                    }
                }
            }

            /**
             * Deletes a word registered in the User dictionary or the Learning
             * dictionary.
             *
             * Executes a word deletion by specifying the reading string into "stroke"
             * and the notation string into "candidate".
             *
             * @param packageName   PackageName.
             * @param candidate     Notation of word to delete from dictionary.
             * @param stroke        Stroke of word to delete from dictionary.
             * @return              Success:true  Failure:false
             */
            public boolean deleteWord(String packageName, String candidate, String stroke) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::deleteWord(" + candidate + "," + stroke + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return false;
                    }
                    boolean result = false;
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_DELETEWORD][engineService.mEngineStatus]) {
                        WnnWord getWord = null;
                        //Delete word in the user or learning dictionary.
                        for (int i = 0; i < engineService.mWnnWordArray.size(); i++) {
                            if (engineService.mWnnWordArray.get(i) != null && engineService.mWnnWordArray.get(i).candidate.equals(candidate) && engineService.mWnnWordArray.get(i).stroke.equals(stroke)) {
                                getWord = engineService.mWnnWordArray.get(i);
                                break;
                            }
                        }
                        if (getWord != null) {
                            result = engineService.mEngine.deleteWord(getWord);
                        }

                        engineService.mEngineStatus = ENGINE_INITIALIZE_REGISTRATION_DICTIONARY;
                        engineService.mErrorCode = result ? 0 : ERR_COMMON;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            engineService.mErrorCode = ERR_NO_DICTIONARY;
                        } else {
                            engineService.mErrorCode = ERR_INVALID_STATE;
                        }
                        return false;
                    }
                    return result;
                }
            }

            /**
             * Backup the User dictionary or the Learning dictionary.
             *
             * @param packageName   PackageName.
             * @return              Success:true  Failure:false
             */
            public boolean writeoutDictionary(String packageName) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::writeoutDictionary()[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return false;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_WRITEOUTDICTIONARY][engineService.mEngineStatus]) {
                        boolean result = engineService.mEngine.writeoutDictionary(engineService.mEngine.getLanguage(), engineService.mEngine.getDictionary());
                        engineService.mErrorCode = result ? 0 : ERR_COMMON;
                        return result;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            engineService.mErrorCode = ERR_NO_DICTIONARY;
                        } else {
                            engineService.mErrorCode = ERR_INVALID_STATE;
                        }
                        return false;
                    }
                }
            }

            /**
             * Initialize the User dictionary or the Learning dictionary.
             *
             * @param packageName   PackageName.
             * @return              Success:true  Failure:fals
             */
            public boolean initializeDictionary(String packageName) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::initializeDictionary()[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return false;
                    }
                    boolean result = false;
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_INITIALIZEDICTIONARY][engineService.mEngineStatus]) {
                        int dictionary = engineService.mEngine.getDictionary();
                        if (dictionary == iWnnEngine.SetType.USERDIC) {
                            result = engineService.mEngine.initializeUserDictionary(engineService.mEngine.getLanguage(),
                                    dictionary);
                        } else if (dictionary == iWnnEngine.SetType.LEARNDIC) {
                            result = engineService.mEngine.initializeLearnDictionary(engineService.mEngine.getLanguage());
                        } // else {
                        engineService.mEngineStatus = ENGINE_INITIALIZE_REGISTRATION_DICTIONARY;
                        engineService.mErrorCode = result ? 0 : ERR_COMMON;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            engineService.mErrorCode = ERR_NO_DICTIONARY;
                        } else {
                            engineService.mErrorCode = ERR_INVALID_STATE;
                        }
                    }
                    return result;
                }
            }

            /**
             * Sets the User dictionary.
             *
             * Sets the User dictionary as the one to be used by the iWnn Engine.
             *
             * @param packageName   PackageName.
             */
            public void setUserDictionary(String packageName) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::setUserDictionary()[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_SETUSERDICTIONARY][engineService.mEngineStatus]) {
                        engineService.mEngine.setDictionary(engineService.mEngine.getLanguage(), iWnnEngine.SetType.USERDIC,
                                this.hashCode(), engineService.mConfigurationFile, engineService.mConnectedPackage, engineService.mPassword);
                        engineService.mEngineStatus = ENGINE_INITIALIZE_REGISTRATION_DICTIONARY;
                    }
                }
            }

            /**
             * Sets the Learning dictionary.
             *
             * Sets the Learning dictionary as the one to be used by the iWnn Engine.
             *
             * @param packageName   PackageName.
             */
            public void setLearnDictionary(String packageName) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::setLearnDictionary()[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_SETLEARNDICTIONARY][engineService.mEngineStatus]) {
                        engineService.mEngine.setDictionary(engineService.mEngine.getLanguage(), iWnnEngine.SetType.LEARNDIC,
                                this.hashCode(), engineService.mConfigurationFile, engineService.mConnectedPackage, engineService.mPassword);
                        engineService.mEngineStatus = ENGINE_INITIALIZE_REGISTRATION_DICTIONARY;
                    }
                }
            }

            /**
             * Sets the Normal dictionary.
             *
             * Sets the Normal dictionary as the one to be used by the iWnn Engine.
             *
             * @param packageName   PackageName.
             */
            public void setNormalDictionary(String packageName) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::setNormalDictionary()[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_SETNORMALDICTIONARY][engineService.mEngineStatus]) {
                        engineService.mEngine.setDictionary(engineService.mEngine.getLanguage(), iWnnEngine.SetType.NORMAL,
                                this.hashCode(), engineService.mConfigurationFile, engineService.mConnectedPackage, engineService.mPassword);
                        engineService.mEngineStatus = ENGINE_INITIALIZE;
                    }
                }
            }

           /**
             * Learns the prediction candidate obtained
             * by *getNextCandidate()*.
             *
             * @param packageName   PackageName.
             * @param word          Word will be learned.
             * @param connected     {@code true}: if words are sequenced.
             * @return              Success:true Failure:false
             */
            private boolean learn(String packageName, WnnWord word, boolean connected) {
                EngineService engineService = getEngineService(packageName);
                if (engineService == null ) {
                    return false;
                }
                boolean result = engineService.mEngine.learn(word,connected);
                if (engineService.mIsConverting) {
                    result = makeNextCandidateList(packageName);
                    engineService.mEngineStatus = ENGINE_PREFIXATION_AND_SEARCH;
                } else {
                    engineService.mEngineStatus = ENGINE_PREFIXATION;
                }
                return result;
            }

           /**
            * Make the candidate list.
             *
             * @param packageName   PackageName.
            */
            private boolean makeNextCandidateList(String packageName) {
                EngineService engineService = getEngineService(packageName);
                if (engineService == null ) {
                    return false;
                }
                engineService.mWnnWordArray.clear();
                engineService.mSegment++;
                return (engineService.mEngine.makeCandidateListOf(engineService.mSegment) == 1) ? true : false;
            }

           /**
             * Learns only the previous committing information that relates to
             * the prediction candidate obtained by *getNextCandidate()*.
             * The index of the parameter starts at 1 using the order
             * in which candidates were obtained by *getNextCandidate()*.
             * For example, if the third candidate obtained by
             * getNextCandidate() is to be learned, it will be set to index 3.
             *
             * @param packageName   PackageName.
             * @param index         Index of words to learn.
             * @return              Success:true Failure:false
             */
            public boolean learnCandidateNoStore(String packageName, int index) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::learnCandidateNoStore(" + index + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return false;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_LEARNCANDIDATE][engineService.mEngineStatus]) {
                        if ((engineService.mWnnWordArray.size() <= index) || (index < 0)) {
                            return false;
                        }
                        WnnWord word = engineService.mWnnWordArray.get(index);
                        word.attribute = word.attribute | iWnnEngine.WNNWORD_ATTRIBUTE_NO_DICTIONARY;
                        boolean result = learn(packageName,word,true);
                        engineService.mErrorCode = result ? 0 : ERR_COMMON;
                        return result;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            engineService.mErrorCode = ERR_NO_DICTIONARY;
                        } else {
                            engineService.mErrorCode = ERR_INVALID_STATE;
                        }
                        return false;
                    }
                }
            }

           /**
             * Learns the prediction candidate obtained by *getNextCandidate()*.
             * Not learns the predictive candidates connection.
             * The index of the parameter starts at 1 using the order
             * in which candidates were obtained by *getNextCandidate()*.
             * For example, if the third candidate obtained by
             * getNextCandidate() is to be learned, it will be set to index 3.
             *
             * @param packageName  PackageName.
             * @param index        Index of words to learn.
             * @return             Success:true Failure:false
             */
            public boolean learnCandidateNoConnect(String packageName, int index) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::learnCandidateNoConnect(" + index + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return false;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_LEARNCANDIDATE][engineService.mEngineStatus]) {
                        if ((engineService.mWnnWordArray.size() <= index) || (index < 0)) {
                            return false;
                        }
                        WnnWord word = engineService.mWnnWordArray.get(index);
                        boolean result = learn(packageName,word,false);
                        engineService.mErrorCode = result ? 0 : ERR_COMMON;
                        return result;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            engineService.mErrorCode = ERR_NO_DICTIONARY;
                        } else {
                            engineService.mErrorCode = ERR_INVALID_STATE;
                        }
                        return false;
                    }
                }
            }

           /**
             * Learns the candidate.
             *
             * @param packageName   PackageName.
             * @param candidate     Candidate of selected word
             * @param stroke        Stroke of selected word
             * @return              Success:true Failure:false
             */
            public boolean learnWord(String packageName, String candidate, String stroke) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::learnWord(" + candidate + "," + stroke + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return false;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_LEARNWORD][engineService.mEngineStatus]) {
                        WnnWord word = new WnnWord(0, candidate, stroke,
                                                        iWnnEngine.WNNWORD_ATTRIBUTE_SERVICE_WORD_NO_ENGINE);
                        boolean result = learn(packageName,word,true);
                        engineService.mErrorCode = result ? 0 : ERR_COMMON;
                        return result;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            engineService.mErrorCode = ERR_NO_DICTIONARY;
                        } else {
                            engineService.mErrorCode = ERR_INVALID_STATE;
                        }
                        return false;
                    }
                }
            }

           /**
             * Learns only the previous committing information.
             *
             * @param packageName   PackageName.
             * @param candidate     Candidate of selected word
             * @param stroke        Stroke of selected word
             * @return              Success:true Failure:false
             */
            public boolean learnWordNoStore(String packageName, String candidate, String stroke) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::learnWordNoStore(" + candidate + "," + stroke + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return false;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_LEARNWORD][engineService.mEngineStatus]) {

                        // To call IWnnCore.noConv() with candidate (not stroke),
                        // this WnnWord's stroke is candidate
                        // and not have WNNWORD_ATTRIBUTE_SERVICE_WORD_NO_ENGINE attribute.
                        WnnWord word = new WnnWord(0, candidate, candidate,
                                                        iWnnEngine.WNNWORD_ATTRIBUTE_MUHENKAN |
                                                        iWnnEngine.WNNWORD_ATTRIBUTE_NO_DICTIONARY);
                        boolean result = learn(packageName,word,true);
                        engineService.mErrorCode = result ? 0 : ERR_COMMON;
                        return result;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            engineService.mErrorCode = ERR_NO_DICTIONARY;
                        } else {
                            engineService.mErrorCode = ERR_INVALID_STATE;
                        }
                        return false;
                    }
                }
            }

           /**
             * Learns the candidate.
             * Not learns the predictive candidates connection.
             *
             * @param packageName   PackageName.
             * @param candidate     Candidate of selected word
             * @param stroke        Stroke of selected word
             * @return              Success:true Failure:false
             */
            public boolean learnWordNoConnect(String packageName, String candidate, String stroke) {
                synchronized (this) {
                    if (DEBUG) Log.d(TAG, "IWnnEngineService::learnWord(" + candidate + "," + stroke + ")[" + getEngineService(packageName).mEngineStatus + "]");
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return false;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_LEARNWORD][engineService.mEngineStatus]) {
                        WnnWord word = new WnnWord(0, candidate, stroke,
                                                        iWnnEngine.WNNWORD_ATTRIBUTE_SERVICE_WORD_NO_ENGINE);
                        boolean result = learn(packageName,word,false);
                        engineService.mErrorCode = result ? 0 : ERR_COMMON;
                        return result;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            engineService.mErrorCode = ERR_NO_DICTIONARY;
                        } else {
                            engineService.mErrorCode = ERR_INVALID_STATE;
                        }
                        return false;
                    }
                }
            }

            /**
             * Set the type of dictionary .
             *
             * @param packageName        PackageName.
             * @param configurationFile  Language configuration file path.
             * @param language           Type of language.
             * @param dictionary         Type of dictionary.
             * @param flexibleSearch     Enable the flexible search.
             * @param tenKeyType         Is ten keyboard type.
             * @param emojiFilter        Emoji code is filter.
             * @param emailFilter        Email candidates is filter.
             * @param convertCandidates  Set the convert candidates.
             * @param learnNumber        Learn the numeric mixing candidate.
             * @return                   Success:true Failure:false
             */
            public boolean setDictionary(String packageName, String configurationFile, int language, int dictionary, boolean flexibleSearch,
                    boolean tenKeyType, boolean emojiFilter, boolean emailFilter, boolean convertCandidates,
                    boolean learnNumber) {

                synchronized (this) {
                    if (DEBUG) {
                        EngineService engineService = getEngineService(packageName);
                        Log.d(TAG, "IWnnEngineService::setDictionary(" + configurationFile + ","
                                + language + "," + dictionary
                                + "," + flexibleSearch + "," + tenKeyType + ","
                                + emojiFilter + "," + emailFilter + "," + convertCandidates
                                + "," + learnNumber + ")["
                                + ((engineService == null)? "NULL" : engineService.mEngineStatus)
                                + "]");
                    }
                    return setDictionaryDecoratedPict(packageName,configurationFile, language, dictionary,
                            flexibleSearch, tenKeyType, emojiFilter, true, emailFilter,
                            convertCandidates, learnNumber);
                }
            }

            /**
             * Set the type of dictionary with DecoEmoji.
             *
             * @param packageName        PackageName.
             * @param configurationFile  Language configuration file path.
             * @param language           Type of language.
             * @param dictionary         Type of dictionary.
             * @param flexibleSearch     Enable the flexible search.
             * @param tenKeyType         Is ten keyboard type.
             * @param emojiFilter        Emoji code is filter.
             * @param decoEmojiFilter    DecoEmoji code is filter.
             * @param emailFilter        Email candidates is filter.
             * @param convertCandidates  Set the convert candidates.
             * @param learnNumber        Learn the numeric mixing candidate.
             * @return                   Success:true Failure:false
             */
            public boolean setDictionaryDecoratedPict(String packageName, String configurationFile, int language,
                    int dictionary, boolean flexibleSearch, boolean tenKeyType,
                    boolean emojiFilter, boolean decoEmojiFilter, boolean emailFilter,
                    boolean convertCandidates, boolean learnNumber) {

                synchronized (this) {
                    EngineService engineService = getEngineService(packageName);
                    if (DEBUG) {
                        Log.d(TAG, "IWnnEngineService::setDictionaryDecoratedPict("
                                + configurationFile + "," + language + "," + dictionary + ","
                                + flexibleSearch + "," + tenKeyType + "," + emojiFilter + ","
                                + decoEmojiFilter + "," + emailFilter + "," + convertCandidates + ","
                                + learnNumber + ")["
                                + ((engineService == null)? "NULL" : engineService.mEngineStatus)
                                + "]");
                    }
                    if (engineService == null ) {
                        return false;
                    }
                    iWnnEngine engine = engineService.mEngine;
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_SETDICTIONARY][engineService.mEngineStatus]) {
                        if (language < 0) {
                            Log.e(TAG, "iWnnEngineService::setDictionary(): language < 0");
                            engineService.mErrorCode = ERR_COMMON;
                            return false;
                        }
                        boolean isLanguageChanged = (language != engine.getLanguage());
                        boolean isDictionaryChanged
                            = (isLanguageChanged
                                    || (dictionary != engine.getDictionary())
                                    || (engineService.mEngineStatus == ENGINE_UNSETTING));

                        if (isDictionaryChanged) {
                            if (!engineService.mCachedDicSet.contains(language)
                                        && iWnnEngine.SERVICE_CONFIGURATION_FILE_MAX <= engineService.mCachedDicSet.size()) {
                                init(packageName, engineService.mPassword, IWnnServiceConnector.INIT);
                            }
                            if (!engine.setDictionary(language, dictionary, this.hashCode(),
                                            configurationFile, engineService.mConnectedPackage, engineService.mPassword)) {
                                engineService.mErrorCode = ERR_COMMON;
                                return false;
                            } else {
                                engineService.mCachedDicSet.add(language);
                            }
                        }
                        engineService.mConfigurationFile = configurationFile;

                        if (isLanguageChanged || engineService.mIsFlexibleCharsetInit
                                || (engineService.mFlexibleSearch != flexibleSearch)
                                || (engineService.mTenKeyType != tenKeyType)) {

                            int flexible;
                            if (flexibleSearch) {
                                flexible = iWnnEngine.FlexibleSearchType.FLEXIBLE_SEARCH_ON;
                            } else {
                                flexible = iWnnEngine.FlexibleSearchType.FLEXIBLE_SEARCH_OFF;
                            }

                            int keyType;
                            if (tenKeyType) {
                                keyType = iWnnEngine.KeyboardType.KEY_TYPE_KEYPAD12;
                            } else {
                                keyType = iWnnEngine.KeyboardType.KEY_TYPE_QWERTY;
                            }

                            engine.setFlexibleCharset(flexible, keyType);
                            engineService.mFlexibleSearch = flexibleSearch;
                            engineService.mTenKeyType = tenKeyType;
                            engineService.mIsFlexibleCharsetInit = false;
                        }

                        engine.setEmojiFilter(emojiFilter);
                        engine.setDecoEmojiFilter(decoEmojiFilter);
                        engine.setEmailAddressFilter(emailFilter);
                        engine.setConvertedCandidateEnabled(convertCandidates);
                        engine.setEnableLearnNumber(learnNumber);

                        if (dictionary == iWnnEngine.SetType.LEARNDIC
                                || dictionary == iWnnEngine.SetType.USERDIC) {
                            engineService.mEngineStatus = ENGINE_INITIALIZE_REGISTRATION_DICTIONARY;
                        } else {
                            engineService.mEngineStatus = ENGINE_INITIALIZE;
                        }
                        return true;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            engineService.mErrorCode = ERR_NO_DICTIONARY;
                        } else {
                            engineService.mErrorCode = ERR_INVALID_STATE;
                        }
                        return false;
                    }
                }
            }

            /**
             * Set a pseudo dictionary filter.
             *
             * @param packageName  PackageName.
             * @param type         Pseudo dictionary filter.
             * @return             Success:true Failure:false
             */
            public boolean setGijiFilter(String packageName, int[] type) {
                synchronized (this) {
                    EngineService engineService = getEngineService(packageName);
                    if (DEBUG) {
                        Log.d(TAG, "IWnnEngineService::setGijiFilter(" + packageName + ")["
                                + ((engineService == null)? "NULL" : engineService.mEngineStatus)
                                + "]");
                    }
                    if (engineService == null) {
                        return false;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_SETDICTIONARY][engineService.mEngineStatus]) {
                        if (type == null) {
                            return false;
                        }
                        boolean result = engineService.mEngine.setGijiFilter(type);
                        engineService.mErrorCode = result ? 0 : ERR_COMMON;
                        return result;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            engineService.mErrorCode = ERR_NO_DICTIONARY;
                        } else {
                            engineService.mErrorCode = ERR_INVALID_STATE;
                        }
                        return false;
                    }
                }
            }

            /**
             * Undo of learning.
             *
             * @param packageName   PackageName.
             * @return              Success:true Failure:false
             */
            public boolean undo(String packageName) {
                synchronized (this) {
                    EngineService engineService = getEngineService(packageName);
                    if (DEBUG) {
                        Log.d(TAG, "IWnnEngineService::undo()["
                                + ((engineService == null)? "NULL" : engineService.mEngineStatus)
                                + "]");
                    }
                    if (engineService == null ) {
                        return false;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_UNDO][engineService.mEngineStatus]) {
                        boolean result = engineService.mEngine.undo(1);
                        engineService.mErrorCode = result ? 0 : ERR_COMMON;
                        engineService.mEngineStatus = ENGINE_SEARCH;
                        return result;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            engineService.mErrorCode = ERR_NO_DICTIONARY;
                        } else {
                            engineService.mErrorCode = ERR_INVALID_STATE;
                        }
                        return false;
                    }
                }
            }

            /**
             * Starts the input.
             *
             * @param packageName   PackageName.
             */
            public void startInput(String packageName) {
                synchronized (this) {
                    EngineService engineService = getEngineService(packageName);
                    if (DEBUG) {
                        Log.d(TAG, "IWnnEngineService::startInput()["
                                + ((engineService == null)? "NULL" : engineService.mEngineStatus)
                                + "]");
                    }
                    if (engineService == null ) {
                        return;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_STARTINPUT][engineService.mEngineStatus]) {
                        Message message = mHandler.obtainMessage(MSG_UPDATE_DECOEMOJI_DICTIONARY);

                        Bundle bundle = new Bundle();
                        bundle.putString("packageName", packageName);
                        message.setData(bundle);

                        mHandler.sendMessageDelayed(message,
                                DELAY_MS_UPDATE_DECOEMOJI_DICTIONARY);

                        message = mHandler.obtainMessage(MSG_DELETE_DECOEMOJI_LEARNING_DICTIONARY);
                        bundle = new Bundle();
                        bundle.putString("packageName", packageName);
                        message.setData(bundle);

                        mHandler.sendMessageDelayed(message,
                                MSG_DELETE_DECOEMOJI_LEARNING_DICTIONARY);
                    }
                    callCheckDecoEmoji();
                }
            }


            /**
             * Check a candidate word if it's on the pseudo dictionary.
             *
             * @param packageName  PackageName.
             * @param index        index of the candidate word
             * @return             Success:true Failure:false
             */
            public boolean isGijiDic(String packageName, int index) {
                synchronized (this) {
                    EngineService engineService = getEngineService(packageName);
                    if (DEBUG) {
                        Log.d(TAG, "IWnnEngineService::isGijiDic(" + packageName
                                + "," + index + ")["
                                + ((engineService == null)? "NULL" : engineService.mEngineStatus)
                                + "]");
                    }
                    if (engineService == null ) {
                        return false;
                    }
                    if (ENGINE_STATUS_CHECK_TABLE[FUNCTION_IS_GIJI_DIC][engineService.mEngineStatus]) {
                        boolean result = engineService.mEngine.isGijiDic(index);
                        engineService.mErrorCode = result ? 0 : ERR_COMMON;
                        return result;
                    } else {
                        if (engineService.mEngineStatus == ENGINE_UNSETTING) {
                            engineService.mErrorCode = ERR_NO_DICTIONARY;
                        } else {
                            engineService.mErrorCode = ERR_INVALID_STATE;
                        }
                        return false;
                    }
                }
            }

            /**
             * @hide used for testing only
             *
             * @param packageName   PackageName.
             */
            public int getStatus(String packageName) {
                synchronized (this) {
                    EngineService engineService = getEngineService(packageName);
                    if (DEBUG) {
                        Log.d(TAG, "IWnnEngineService::getStatus(" + packageName + ")["
                                + ((engineService == null)? "NULL" : engineService.mEngineStatus)
                                + "]");
                    }
                    if (engineService == null ) {
                        return ENGINE_UNSETTING;
                    }
                    return engineService.mEngineStatus;
                }
            }

            /**
             * @hide used for testing only
             *
             * @param packageName   PackageName.
             */
            public int getDictionaryType(String packageName) {
                synchronized (this) {
                    EngineService engineService = getEngineService(packageName);
                    if (DEBUG) {
                        Log.d(TAG, "IWnnEngineService::getDictionaryType()["
                                + ((engineService == null)? "NULL" : engineService.mEngineStatus)
                                + "]");
                    }
                    if (engineService == null ) {
                        return -1;
                    }
                    return engineService.mEngine.getDictionary();
                }
            }

            /**
             * Get error code.
             *
             * @param packageName   PackageName.
             * @return              error code.
             */
            public int getErrorCode(String packageName) {
                synchronized (this) {
                    EngineService engineService = getEngineService(packageName);
                    if (DEBUG) {
                        Log.d(TAG, "IWnnEngineService::getErrorCode()["
                                + ((engineService == null)? "NULL" : engineService.mEngineStatus)
                                + "]");
                    }
                    if (engineService == null ) {
                        return ERR_INVALID_STATE;
                    }
                    return engineService.mErrorCode;
                }
            }
            /**
             * disconnect
             *
             * @param packageName   PackageName.
             */
            public void disconnect(String packageName) {
                synchronized (this) {
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return;
                    }
                    clearServiceInfo(packageName);
                    return;
                }
            }

            /**
             * You can know the connector is still connecting the service.
             *
             * @param packageName   PackageName.
             * @return              Alive:true  NoAlive:false
             */
            public boolean isAlive(String packageName) {
                synchronized (this) {
                    EngineService engineService = getEngineService(packageName);
                    if (engineService == null ) {
                        return false;
                    }
                    return true;
                }
            }
        };

    /**
     * Default Constructor.
     */
    public IWnnEngineService() {}

    /**
     * @see android.app.Service.onCreate
     */
    @Override public void onCreate() {
        super.onCreate();
        mCurrentService = this;
        mContext = this;
        mEngineServiceArray.clear();
        iWnnEngine.clearServiceEngine();
        mFilesDirPath = getFilesDir().getPath();
        for (int i = 0; i < iWnnEngine.SERVICE_CONNECT_MAX; i++) {
            EngineService engineService = new EngineService();
            mEngineServiceArray.add(engineService);
        }
    }

    /**
     * @see android.app.Service.onDestroy
     */
    @Override public void onDestroy() {
        stopDecoEmojiUpdating();
        synchronized (this) {
            for (int i = 0; i < mEngineServiceArray.size(); i++) {
                if (mEngineServiceArray.get(i).mEngine != null) {
                    mEngineServiceArray.get(i).mEngine.close();
                }
            }
        }
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
                decoEmojiBindStart();
            }
        }
        OpenWnn.copyPresetDecoEmojiGijiDictionary();
        return mEngineService;
    }

    /**
     * Bind to the Service of iWnnEngine.
     *
     * @param intent        Intent.
     * @return              Stub.
     * @see android.app.Service.onBind
     */
    @Override public boolean onUnbind(Intent intent) {
        if (mIsBind) {
            unbindService(mServiceConn);
            mIsBind = false;
        }
        stopDecoEmojiUpdating();
        synchronized (this) {
            for (int i = 0; i < mEngineServiceArray.size(); i++) {
                if (mEngineServiceArray.get(i).mEngine != null) {
                    mEngineServiceArray.get(i).mEngine.close();
                }
            }
            mEngineServiceArray.clear();
            iWnnEngine.clearServiceEngine();
            for (int i = 0; i < iWnnEngine.SERVICE_CONNECT_MAX; i++) {
                EngineService engineService = new EngineService();
                mEngineServiceArray.add(engineService);
            }
        }
        return super.onUnbind(intent);
    }

    /**
     * Get the instance of current service.
     *
     * @return the instance of current service
     */
    public static IWnnEngineService getCurrentService() {
        return mCurrentService;
    }

    /**
     * Bind to the DecoEmojiManager.
     */
    private void decoEmojiBindStart() {
        Intent intent = new Intent(IDecoEmojiManager.class.getName());
        boolean success = bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
        if (success) {
            mIsBind = true;
        }
    }

    /**
     * Stops updating of DecoEmoji.
     */
    private void stopDecoEmojiUpdating() {
        mHandler.removeMessages(MSG_UPDATE_DECOEMOJI_DICTIONARY);
        mHandler.removeMessages(MSG_DELETE_DECOEMOJI_LEARNING_DICTIONARY);
    }

    /**
     * Call DecoEmojiManager bind function.
     */
    private void callCheckDecoEmoji() {

        //#ifdef Only-DOCOMO
        DecoEmojiUtil.getDecoEmojiDicInfo(OpenWnn.getIntFromNotResetSettingsPreference(this, DecoEmojiListener.PREF_KEY, -1));
        //#endif /* Only-DOCOMO */

    }
    /**
     * Get the Service of iWnnEngine.
     *
     * @param packageName packageName
     * @return              Instance of EngineService
     */
    private EngineService getEngineService(String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return null;
        }

        for (int i = 0; i < mEngineServiceArray.size(); i++) {
            if (mEngineServiceArray.get(i).mConnectedPackage.equals(packageName)) {
                mEngineServiceArray.get(i).mUsedTime = System.currentTimeMillis();
                return mEngineServiceArray.get(i);
            }
        }

        for (int i = 0; i < mEngineServiceArray.size(); i++) {
            if (mEngineServiceArray.get(i).mConnectedPackage.length() == 0) {
                mEngineServiceArray.get(i).mConnectedPackage = packageName;
                mEngineServiceArray.get(i).mUsedTime = System.currentTimeMillis();
                mEngineServiceArray.get(i).mEngine.setServiceConnectedName(packageName);
                return mEngineServiceArray.get(i);
            }
        }
        return null;
    }
    /**
     * Clear ServiceInfo of iWnnEngine.
     *
     * @param packageName packageName
     */
    private void clearServiceInfo(String packageName) {
        for (int i = 0; i < mEngineServiceArray.size(); i++) {
            if (mEngineServiceArray.get(i).mConnectedPackage.equals(packageName)) {
                mEngineServiceArray.get(i).mEngine.close();
                mEngineServiceArray.get(i).mEngine.setServiceConnectedName("");
                mEngineServiceArray.get(i).mWnnWordArray = new ArrayList<WnnWord>();
                mEngineServiceArray.get(i).mIsConverting = false;
                mEngineServiceArray.get(i).mSegment = 0;
                mEngineServiceArray.get(i).mEngineStatus = ENGINE_UNSETTING;
                mEngineServiceArray.get(i).mIsFlexibleCharsetInit = true;
                mEngineServiceArray.get(i).mFlexibleSearch = false;
                mEngineServiceArray.get(i).mTenKeyType = false;
                mEngineServiceArray.get(i).mErrorCode = 0;
                mEngineServiceArray.get(i).mConnectedPackage = "";
                mEngineServiceArray.get(i).mPassword = "";
                mEngineServiceArray.get(i).mUsedTime = 0;
                mEngineServiceArray.get(i).mConfigurationFile = "";
                mEngineServiceArray.get(i).mCachedDicSet.clear();
                mEngineServiceArray.get(i).mEngine.clearOperationQueue();
                break;
            }
        }
    }
}
