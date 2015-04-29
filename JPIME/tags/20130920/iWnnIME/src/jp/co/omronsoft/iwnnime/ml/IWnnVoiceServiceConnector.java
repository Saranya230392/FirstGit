/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/** Need to be same as the package name of iWnn IME */
package jp.co.omronsoft.iwnnime.ml;
/** Need to be same as the package name of iWnn IME */

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.content.Context;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * This class provides the API for iWnn Engine Service.
 */
public class IWnnVoiceServiceConnector {
    /** for DEBUG */
    private static final boolean DEBUG = false;

    /** for DEBUG */
    private static final String TAG = "iWnn";

    /** Initialize parameters. */
    public static final int INIT = 1;

    /** Break the sequence of words. */
    public static final int BREAK_SEQUENCE = 2;

    /** Normal */
    public static final int NORMAL = 0;

    /** Alphanumeric Japanese KANA */
    public static final int EISUKANA = 1;

    /** Face Mark */
    public static final int KAOMOJI = 2;

    /** User Dictionary */
    public static final int USERDIC = 10;

    /** Learning Dictionary */
    public static final int LEARNDIC = 11;

    /** Return code OK */
    public static final int RETCODE_OK = 0;

    /** Return code NG */
    public static final int RETCODE_NG = -1;

    /** Return code Invalid State */
    public static final int RETCODE_INVALID_STATE = -127;

    /** Return code Invalid State */
    public static final int ERR_NO_DICTIONARY = -126;

    /** Return code Another application connected */
    public static final int RETCODE_ANOTHER_APPLICATION_CONNECTED = -2;

    /** Service Max Connected Count*/
    public static final int SERVICE_MAX_CONNECTED_COUNT = 3;

  /** Lexical category */
    /** Noun(Including verb conjugation) */
    public static final int LEXICAL_NOUN = 0;
    /** Person's name */
    public static final int LEXICAL_PERSONS_NAME = 1;
    /** Noun(Excluding verb conjugation) / Place name / Station name */
    public static final int LEXICAL_NOUN_NO_CONJ = 2;
    /** Symbols */
    public static final int LEXICAL_SYMBOL = 3;

  /** Type of Dictionary */
    /** User Dictionary */
    public static final int DICTIONARY_TYPE_USER = 0;
    /** Learning Dictionary */
    public static final int DICTIONARY_TYPE_LEARNING = 1;

  /** Relational word learning */
    /** Disable the relational word learning */
    public static final int RELATIONAL_LEARNING_OFF = 0;
    /** Enable the relational word learning */
    public static final int RELATIONAL_LEARNING_ON = 1;

  /** Search strategy on dictionary */
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

  /** Order of dictionary retrieval */
    /** Order of frequency */
    public static final int ORDER_FREQUENCY = 0;
    /** Order of reading */
    public static final int ORDER_READING = 1;
    /** Order of registration */
    public static final int ORDER_REGISTRATION = 2;

  /** Pseudo candidate type */
    /**< Pseudo candidate type:None (Initialize) */
    public static final int PSEUDO_TYPE_NONE= 0;
    /**< Pseudo candidate type:Full-width Hiragana */
    public static final int PSEUDO_TYPE_HIRAGANA= 2;
    /**< Pseudo candidate type:Full-width Katakana */
    public static final int PSEUDO_TYPE_KATAKANA= 3;
    /**< Pseudo candidate type:Half-width Katakana */
    public static final int PSEUDO_TYPE_HANKATA= 4;
    /**< Pseudo candidate type:Half-width English characters(with capital on top) */
    public static final int PSEUDO_TYPE_HAN_EIJI_CAP= 5;
    /**< Pseudo candidate type:Full-width English characters(with capital on top) */
    public static final int PSEUDO_TYPE_ZEN_EIJI_CAP= 6;
    /**< Pseudo candidate type:Half-width English characters(All capital letters) */
    public static final int PSEUDO_TYPE_HAN_EIJI_UPPER= 7;
    /**< Pseudo candidate type:Full-width English characters(All capital letters) */
    public static final int PSEUDO_TYPE_ZEN_EIJI_UPPER= 8;
    /**< Pseudo candidate type:Half-width English characters(All small letters) */
    public static final int PSEUDO_TYPE_HAN_EIJI_LOWER= 9;
    /**< Pseudo candidate type:Full-width English characters(All small letters) */
    public static final int PSEUDO_TYPE_ZEN_EIJI_LOWER= 10;
    /**< Pseudo candidate type:Half-width Number */
    public static final int PSEUDO_TYPE_HAN_SUUJI= 11;
    /**< Pseudo candidate type:Full-width Number */
    public static final int PSEUDO_TYPE_ZEN_SUUJI= 12;
    /**< Pseudo candidate type:Half-width Number(Comma) */
    public static final int PSEUDO_TYPE_HAN_SUUJI_COMMA= 13;
    /**< Pseudo candidate type:Half-width Time(H hour) */
    public static final int PSEUDO_TYPE_HAN_TIME_HH= 14;
    /**< Pseudo candidate type:Half-width Time(M minute) */
    public static final int PSEUDO_TYPE_HAN_TIME_MM= 15;
    /**< Pseudo candidate type:Half-width Time(H hour & M minute) */
    public static final int PSEUDO_TYPE_HAN_TIME_HM= 16;
    /**< Pseudo candidate type:Half-width Time(H hour & MM minute) */
    public static final int PSEUDO_TYPE_HAN_TIME_HMM= 17;
    /**< Pseudo candidate type:Half-width Time(HH hour & M minute) */
    public static final int PSEUDO_TYPE_HAN_TIME_HHM= 18;
    /**< Pseudo candidate type:Half-width Time(HH hour & MM minute) */
    public static final int PSEUDO_TYPE_HAN_TIME_HHMM= 19;
    /**< Pseudo candidate type:Half-width Time(H:MM) */
    public static final int PSEUDO_TYPE_HAN_TIME_HMM_SYM= 20;
    /**< Pseudo candidate type:Half-width Time(HH:MM) */
    public static final int PSEUDO_TYPE_HAN_TIME_HHMM_SYM= 21;
    /**< Pseudo candidate type:Half-width Date(YYYY year) */
    public static final int PSEUDO_TYPE_HAN_DATE_YYYY= 22;
    /**< Pseudo candidate type:Half-width Date(M month) */
    public static final int PSEUDO_TYPE_HAN_DATE_MM= 23;
    /**< Pseudo candidate type:Half-width Date(D day) */
    public static final int PSEUDO_TYPE_HAN_DATE_DD= 24;
    /**< Pseudo candidate type:Half-width Date(M month D day) */
    public static final int PSEUDO_TYPE_HAN_DATE_MD= 25;
    /**< Pseudo candidate type:Half-width Date(M month DD day) */
    public static final int PSEUDO_TYPE_HAN_DATE_MDD= 26;
    /**< Pseudo candidate type:Half-width Date(MM month D day) */
    public static final int PSEUDO_TYPE_HAN_DATE_MMD= 27;
    /**< Pseudo candidate type:Half-width Date(MM month DD day) */
    public static final int PSEUDO_TYPE_HAN_DATE_MMDD= 28;
    /**< Pseudo candidate type:Half-width Date(M/D) */
    public static final int PSEUDO_TYPE_HAN_DATE_MD_SYM= 29;
    /**< Pseudo candidate type:Half-width Date(M/DD) */
    public static final int PSEUDO_TYPE_HAN_DATE_MDD_SYM= 30;
    /**< Pseudo candidate type:Half-width Date(MM/D) */
    public static final int PSEUDO_TYPE_HAN_DATE_MMD_SYM= 31;
    /**< Pseudo candidate type:Half-width Date(MM/DD) */
    public static final int PSEUDO_TYPE_HAN_DATE_MMDD_SYM= 32;
    /**< Pseudo candidate type:Reading string of Half-width Date/Time(Initialize) */
    public static final int PSEUDO_TYPE_HAN_YOMI2DATE_INIT= 242;
    /**< Pseudo candidate type:Reading string of Half-width Date(Format:YYYY year) */
    public static final int PSEUDO_TYPE_HAN_YOMI2DATE_YYYY= 243;
    /**< Pseudo candidate type:Reading string of Half-width Date(Format:Heisei(Showa)YY year) */
    public static final int PSEUDO_TYPE_HAN_YOMI2DATE_NENGO= 244;
    /**< Pseudo candidate type:Reading string of Half-width Date(Format:MM month) */
    public static final int PSEUDO_TYPE_HAN_YOMI2DATE_MM= 245;
    /**< Pseudo candidate type:Reading string of Half-width Date(Format:MM/DD) */
    public static final int PSEUDO_TYPE_HAN_YOMI2DATE_MMDD_SYM= 246;
    /**< Pseudo candidate type:Reading string of Half-width Date(Format:MM month DD day) */
    public static final int PSEUDO_TYPE_HAN_YOMI2DATE_MMDD= 247;
    /**< Pseudo candidate type:Reading string of Half-width Date(Format:MM/DD(week)) */
    public static final int PSEUDO_TYPE_HAN_YOMI2DATE_MMDD_SYM_WEEK= 248;
    /**< Pseudo candidate type:Reading string of Half-width Date(Format:MM month DD day(week)) */
    public static final int PSEUDO_TYPE_HAN_YOMI2DATE_MMDD_WEEK= 249;
    /**< Pseudo candidate type:Reading string of Half-width Time(Format:HH:MM) */
    public static final int PSEUDO_TYPE_HAN_YOMI2TIME_HHMM_SYM= 250;
    /**< Pseudo candidate type:Reading string of Half-width Time(Format:AM(PM)HH:MM) */
    public static final int PSEUDO_TYPE_HAN_YOMI2TIME_HHMM_SYM_AMPM= 251;
    /**< Pseudo candidate type:Reading string of Half-width Time(Format:HH hour MM minute) */
    public static final int PSEUDO_TYPE_HAN_YOMI2TIME_HHMM= 252;
    /**< Pseudo candidate type:Reading string of Half-width Time(Format:morning(afternoon)HH hour MM minute) */
    public static final int PSEUDO_TYPE_HAN_YOMI2TIME_HHMM_12H= 253;

/** Need to be same as the package name of iWnn IME */
    /** IWnnEngineService class name */
    private static final String IWNNENGINESERVICE_NAME
            = "jp.co.omronsoft.iwnnime.ml.IWnnVoiceEngineService";
    /** IWnnEngineService class name */
    private static final String SERVICE_PASSWORD
            = "";
/** Need to be same as the package name of iWnn IME */

    /** Service binder. */
    private IVoiceEngineService mServiceIf = null;

    /** Context of Activity. */
    private Context mContext = null;

    /** Whether binding service. */
    private boolean mIsBind;

    /** A listener for asynchronous connecting */
    private OnConnectListener mConnectListener;

    /**
     * Interface for monitoring the state of the iWnn Engine service.
     */
    private ServiceConnection mServiceConn = new ServiceConnection() {
        /** @see android.content.ServiceConnection.onServiceConnected */
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (DEBUG) Log.d(TAG, "ServiceConnection::onServiceConnected()");
            mServiceIf = IVoiceEngineService.Stub.asInterface(service);
            mIsBind = true;
            init(INIT);
            if (mConnectListener != null) {
                mConnectListener.onConnect();
            }
        }

        /** @see android.content.ServiceConnection.onServiceDisconnected */
        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG) Log.d(TAG, "ServiceConnection::onServiceDisconnected()");
            mServiceIf = null;
            mIsBind = false;
            if (mConnectListener != null) {
                mConnectListener.onDisconnect();
            }
        }
    };

    public interface OnConnectListener {
        public void onConnect();
        public void onDisconnect();
    }

    /**
     * Default constructor.
     */
    public IWnnVoiceServiceConnector() {
    }

    /**
     * Open a connection to the iWnn Engine Service.
     *
     * @param context Context.
     * @return If you have successfully bound to the service, 0 is
     * returned. Minus value is returned if the connection is not made.
     */
    public int connect(Context context, OnConnectListener listener) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::connect()");
        if (context == null || listener == null) {
            return RETCODE_NG;
        }

        boolean success = false;
        if(!mIsBind) {
            ActivityManager manager
                    = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (service != null) {
                    if (IWNNENGINESERVICE_NAME.equals(service.service.getClassName())) {
                        if (service.clientCount >= SERVICE_MAX_CONNECTED_COUNT ) {
                            return RETCODE_ANOTHER_APPLICATION_CONNECTED;
                        }
                    }
                }
            }

            Intent intent = new Intent(IVoiceEngineService.class.getName());
            success =
                context.bindService(intent, mServiceConn,
                                     Context.BIND_AUTO_CREATE);
            if (success) {
                mConnectListener = listener;
                mContext = context;
            }
        } else {
            return RETCODE_INVALID_STATE;
        }
        return (success ? RETCODE_OK : RETCODE_NG);
    }

    /**
     * Destructor.
     *
     * @throws Throwable    Finalize.
     */
    @Override protected void finalize() throws Throwable {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::finalize()");
        disconnect();
        super.finalize();
    }

    /**
     * Initialize the internal state of the iWnn engine.
     *
     * @param initLevel     Initialize level.
     * @return              Success:0  Failure:minus
     */
    public int init(int initLevel) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::init(" + initLevel + ")");
        try {
            if (mServiceIf != null) {
                mServiceIf.init(mContext.getPackageName(), SERVICE_PASSWORD, initLevel);
                return RETCODE_OK;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "init", e);
        }
        return RETCODE_NG;
    }

    /**
     * Make the prediction candidates.
     *
     * You can get prediction candidates by the iWnn engine based on
     * given reading strings.
     * Gets the strings of prediction candidates
     * by calling {@code getNextCandidate()}.
     * To realize the function of wildcard prediction,
     * specify the values of minLen and maxLen of the desired input strings.
     * <p>For example,
     * <ul>
     *   <li>To get strings of at least and no more than 5 characters,
     *       set minLen and maxLen to 5. </li>
     *   <li>To get strings of more than 3 characters,
     *       set minLen to 3 and maxLen to -1.</li>
     * </ul>
     *
     * @param stroke     The stroke (YOMI) of word.
     * @param minLen     The minimum length of a word to predict.
     *                   (minLen <= 0  : no limit)
     * @param maxLen     The maximum length of a word to predict.
     *                   (maxLen <= -1 : no limit)
     * @return           Plus value if there are candidates;
     *                   0 if there is no candidate;
     *                   Minus value if an error occurs.
     */
    public int predict(String stroke, int minLen, int maxLen) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::predict(" + stroke + "," + minLen + "," + maxLen + ")");
        try {
            if (mServiceIf != null) {
                return mServiceIf.predict(mContext.getPackageName(),stroke, minLen, maxLen);
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "predict", e);
        }
        return RETCODE_NG;
    }

    /**
     * Set enable consecutive phrase level conversion.
     *
     * Set whether to implement the consecutive phrase level conversion.
     * If you do not call this API once, disable consecutive phrase level conversion.
     *
     * @param enable    {@code true}  Enable consecutive phrase level conversion.
     *                  {@code false} Disable consecutive phrase level conversion.
     * @return          0 if success.
     *                  Minus value if an error occurs.
     */
    public int setEnableConsecutivePhraseLevelConversion(boolean enable) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::setEnableConsecutivePhraseLevelConversion(" + enable + ")");
        try {
            if (mServiceIf != null) {
                return mServiceIf.setEnableConsecutivePhraseLevelConversion(mContext.getPackageName(), enable);
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "setEnableConsecutivePhraseLevelConversion", e);
        }
        return RETCODE_NG;
    }

    /**
     * Gets candidate strings for either prediction or conversion.
     *
     * Only gets the number of candidate strings specified in the parameters.
     * By repeatedly calling this API, you can get
     * the all the available candidates.
     * If there are no more candidates left, the API returns {@code null}.
     * The "reading" and "notation" will both be stored
     * as an array in {@code Bundle}.
     * Calls {@code Bundle#getStringArray("stroke")} to get the "reading",
     * and  set "candidate" as a parameter to get the "notation".
     * If the number specified in the parameters was larger than 
     * the available candidates,
     * the iWnn engine will only return the number of existent ones.
     *
     * @param numberOfCandidates    The number of candidates
     *                                  returned by this method.
     * @return                      Candidate words.
     *                              If you want to get strokes of word,
     *                              call Bundle#getStringArray("stroke").
     *                              Similarly, candidates are "candidate".
     */
    public Bundle getNextCandidate(int numberOfCandidates) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::getNextCandidate(" + numberOfCandidates + ")");
        try {
            if (mServiceIf != null) {
                return mServiceIf.getNextCandidate(mContext.getPackageName(),numberOfCandidates);
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "getNextCandidate", e);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("result", RETCODE_NG);
        return bundle;
    }

    /**
     * Gets candidate strings and DecoEmoji text (Annotation is set to strings)
     * for either prediction or conversion.
     *
     * Only gets the number of candidate strings specified in the parameters.
     * By repeatedly calling this API, you can get
     * the all the available candidates.
     * If there are no more candidates left, the API returns {@code null}.
     * The "reading" and "notation" will both be stored
     * as an array in {@code Bundle}.
     * Calls {@code Bundle#getStringArray("stroke")} to get the "reading",
     * and  set "candidate" as a parameter to get the "notation".
     * If the number specified in the parameters was larger than 
     * the available candidates,
     * the iWnn engine will only return the number of existent ones.
     *
     * @param numberOfCandidates    The number of candidates
     *                                  returned by this method.
     * @return                      Candidate words.
     *                              If you want to get strokes of word,
     *                              call Bundle#getStringArray("stroke").
     *                              Similarly, candidates are "candidate",
     *                              and DecoEmoji text (Annotation is set to strings)
     *                              are "annotation_candidate".
     */
    public Bundle getNextCandidateWithAnnotation(int numberOfCandidates) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::getNextCandidateWithAnnotation("
                + numberOfCandidates + ")");
        try {
            if (mServiceIf != null) {
                return mServiceIf.getNextCandidateWithAnnotation(mContext.getPackageName(),numberOfCandidates);
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "getNextCandidateWithAnnotation", e);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("result", RETCODE_NG);
        return bundle;
    }
    /**
     * Gets candidate strings and DecoEmoji text (Annotation is set to strings)
     * for either prediction or conversion.
     *
     * Only gets the number of candidate strings specified in the parameters.
     * By repeatedly calling this API, you can get
     * the all the available candidates.
     * If there are no more candidates left, the API returns {@code null}.
     * The "reading" and "notation" will both be stored
     * as an array in {@code Bundle}.
     * Calls {@code Bundle#getStringArray("stroke")} to get the "reading",
     * and  set "candidate" as a parameter to get the "notation".
     * If the number specified in the parameters was larger than 
     * the available candidates,
     * the iWnn engine will only return the number of existent ones.
     *
     * @param numberOfCandidates    The number of candidates
     *                                  returned by this method.
     * @param emojitype             Emoji type
     * @return                      Candidate words.
     *                              If you want to get strokes of word,
     *                              call Bundle#getStringArray("stroke").
     *                              Similarly, candidates are "candidate",
     *                              and DecoEmoji text (Annotation is set to strings)
     *                              are "annotation_candidate".
     */
    public Bundle getNextCandidateWithAnnotation2(int numberOfCandidates,int emojitype) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::getNextCandidateWithAnnotation2("
                + numberOfCandidates + ")");
        try {
            if (mServiceIf != null) {
                return mServiceIf.getNextCandidateWithAnnotation2(mContext.getPackageName(),numberOfCandidates,emojitype);
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "getNextCandidateWithAnnotationEmoji", e);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("result", RETCODE_NG);
        return bundle;
    }

    /**
     * Learns the prediction candidate obtained by {@code getNextCandidate()}.
     *
     * The index of the parameter starts at 0 using the order
     * in which candidates were obtained by {@code getNextCandidate()}.
     * For example, if the third candidate obtained by getNextCandidate()
     * is to be learned, it will be set to index 2.
     *
     * @param index         The index of words by getNextCandidate().
     * @return              Success:0  Failure:minus
     */
    public int learnCandidate(int index) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::learnCandidate(" + index + ")");
        try {
            if (mServiceIf != null) {
                int ret = mServiceIf.learnCandidate(mContext.getPackageName(),index) ? RETCODE_OK : RETCODE_NG;
                if (ret < 0) {
                    return mServiceIf.getErrorCode(mContext.getPackageName());
                }
                return ret;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "learnCandidate", e);
        }
        return RETCODE_NG;
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
     * @param componentName ComponentName.
     * @param stroke        The stroke (YOMI) of word.
     * @param divide        Divide position.
     * @return              Candidate words.
     *                      If you want to get strokes of word,
     *                      call Bundle#getStringArray("stroke").
     *                      Similarly, candidates are "candidate".
     */
    public Bundle convert(String stroke, int divide) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::convert(" + stroke + "," + divide + ")");
        try {
            if (mServiceIf != null) {
                return mServiceIf.convert(mContext.getPackageName(),stroke, divide);
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "convert", e);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("result", RETCODE_NG);
        return bundle;
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
     * @param componentName ComponentName.
     * @param stroke        The stroke (YOMI) of word.
     * @param divide        Divide position.
     * @return              Convert words.
     *                      If you want to get strokes of word,
     *                      call Bundle#getStringArray("stroke").
     *                      Similarly, candidates are "candidate",
     *                      and DecoEmoji text (Annotation is set to strings)
     *                      are "annotation_candidate".
     */
    public Bundle convertWithAnnotation(String stroke, int divide) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::convertWithAnnotation(" + stroke + "," + divide + ")");
        try {
            if (mServiceIf != null) {
                return mServiceIf.convertWithAnnotation(mContext.getPackageName(),stroke, divide);
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "convertWithAnnotation", e);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("result", RETCODE_NG);
        return bundle;
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
     * @param componentName ComponentName.
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
    public Bundle convertWithAnnotation2(String stroke, int divide, int emojitype) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::convertWithAnnotation2(" + stroke + "," + divide + ")");
        try {
            if (mServiceIf != null) {
                return mServiceIf.convertWithAnnotation2(mContext.getPackageName(),stroke, divide, emojitype);
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "convertWithAnnotation2", e);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("result", RETCODE_NG);
        return bundle;
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
     * @param candidate   Notation of words to learn.
     * @param stroke      Stroke of words to learn.
     * @return            Success:0  Failure:minus
     */
    public int addWord(String candidate, String stroke) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::addWord(" + candidate + "," + stroke + ")");
        try {
            if (mServiceIf != null) {
                return mServiceIf.addWord(mContext.getPackageName(),candidate, stroke);
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "addWord", e);
        }
        return RETCODE_NG;
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
     * @param candidate   Notation of words to learn.
     * @param stroke      Stroke of words to learn.
     * @param hinsi       Index of the lexical category group
     * @param type        Type of dictionaries(0:user dictionary, 1:learning dictionary, 2:pseudo dictionary)
     * @param relation    Relation learning flag(learning relations with the previous registered word. 0:don't learn 1:do learn)
     * @return            Success:0  Failure:minus
     */
    public int addWordDetail(String candidate, String stroke, int hinsi, int type, int relation) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::addWordDetail("
                                  + candidate + "," + stroke + "," + hinsi + "," + type + "," + relation + ")");
        try {
            if (mServiceIf != null) {
                return mServiceIf.addWordDetail(mContext.getPackageName(),candidate, stroke, hinsi, type, relation);
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "addWordDetail", e);
        }
        return RETCODE_NG;
    }

    /**
     * Searches for words from the User dictionary or the Learning dictionary.
     *
     * Executes by specifying a reading string into "stroke".
     * The result is obtained by getNextCandidate()
     * All words can be searched for if an empty string is specified as the
     * arguments.
     *
     * @param stroke     Stroke of word to search from dictionary.
     * @return           found:1  Not found:0  Failure:minus
     */
    public int searchWords(String stroke) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::searchWords(" + stroke + ")");
        try {
            if (mServiceIf != null) {
                return mServiceIf.searchWords(mContext.getPackageName(),stroke);
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "searchWords", e);
        }
        return RETCODE_NG;
    }

    /**
     * Searches for words from the User dictionary or the Learning dictionary.
     *
     * Executes by specifying a reading string into "stroke".
     * The result is obtained by getNextCandidate()
     * All words can be searched for if an empty string is specified as the
     * arguments.
     *
     * @param stroke     Stroke of word to search from dictionary.
     * @param method     Way of searching the dictionary
     * @param order      Order of searching the dictionary
     * @return           found:1  Not found:0  Failure:minus
     */
    public int searchWordsDetail(String stroke, int method, int order) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::searchWordsDetail(" + stroke + "," + method + "," + order + ")");
        try {
            if (mServiceIf != null) {
                return mServiceIf.searchWordsDetail(mContext.getPackageName(),stroke,method,order);
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "searchWordsDetail", e);
        }
        return RETCODE_NG;
    }

    /**
     * Deletes a word registered in the User dictionary or the Learning
     * dictionary.
     *
     * Executes a word deletion by specifying the reading string into "stroke"
     * and the notation string into "candidate".
     * 
     * @param candidate   Notation of word to delete from dictionary.
     * @param stroke      Stroke of word to delete from dictionary.
     * @return            Success:0  Failure:minus
     */
    public int deleteWord(String candidate, String stroke) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::deleteWord(" + candidate + "," + stroke + ")");
        try {
            if (mServiceIf != null) {
                int ret = mServiceIf.deleteWord(mContext.getPackageName(), candidate, stroke) ? RETCODE_OK : RETCODE_NG;
                if (ret < 0) {
                    return mServiceIf.getErrorCode(mContext.getPackageName());
                }
                return ret;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "deleteWord", e);
        }
        return RETCODE_NG;
    }

    /**
     * Backup the User dictionary or the Learning dictionary.
     *
     * @return            Success:0  Failure:minus
     */
    public int writeoutDictionary() {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::writeoutDictionary()");
        try {
            if (mServiceIf != null) {
                int ret = mServiceIf.writeoutDictionary(mContext.getPackageName()) ? RETCODE_OK : RETCODE_NG;
                if (ret < 0) {
                    return mServiceIf.getErrorCode(mContext.getPackageName());
                }
                return ret;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "writeoutDictionary", e);
        }
        return RETCODE_NG;
    }

    /**
     * Initialize the User dictionary or the Learning dictionary.
     *
     * @return            Success:0  Failure:minus
     */
    public int initializeDictionary() {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::initializeDictionary()");
        try {
            if (mServiceIf != null) {
                int ret = mServiceIf.initializeDictionary(mContext.getPackageName()) ? RETCODE_OK : RETCODE_NG;
                if (ret < 0) {
                    return mServiceIf.getErrorCode(mContext.getPackageName());
                }
                return ret;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "initializeDictionary", e);
        }
        return RETCODE_NG;
    }

    /**
     * Sets the User dictionary.
     *
     * Sets the User dictionary as the one to be used by the iWnn Engine.
     *
     * @return            Success:0  Failure:minus
     */
    public int setUserDictionary() {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::setUserDictionary()");
        try {
            if (mServiceIf != null) {
                mServiceIf.setUserDictionary(mContext.getPackageName());
                return RETCODE_OK;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "setUserDictionary", e);
        }
        return RETCODE_NG;
    }

    /**
     * Sets the Learning dictionary.
     *
     * Sets the Learning dictionary as the one to be used by the iWnn Engine.
     *
     * @return            Success:0  Failure:minus
     */
    public int setLearnDictionary() {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::setLearnDictionary()");
        try {
            if (mServiceIf != null) {
                mServiceIf.setLearnDictionary(mContext.getPackageName());
                return RETCODE_OK;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "setLearnDictionary", e);
        }
        return RETCODE_NG;
    }

    /**
     * Sets the Normal dictionary.
     *
     * Sets the Normal dictionary as the one to be used by the iWnn Engine.
     *
     * @return            Success:0  Failure:minus
     */
    public int setNormalDictionary() {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::setNormalDictionary()");
        try {
            if (mServiceIf != null) {
                mServiceIf.setNormalDictionary(mContext.getPackageName());
                return RETCODE_OK;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "setNormalDictionary", e);
        }
        return RETCODE_NG;
    }

   /**
     * Learns the prediction candidate obtained by {@code getNextCandidate()}.
     * Holds the pre-confirmed infomation without saving them into the Learning
     * dictionary.
     * The index of the parameter starts at 1 using the order
     * in which candidates were obtained by *getNextCandidate()*.
     * For example, if the third candidate obtained by
     * getNextCandidate() is to be learned, it will be set to index 3.
     *
     * @param index         Index of words to learn.
     * @return              Success:0  Failure:minus
     */
    public int learnCandidateNoStore(int index) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::learnCandidateNoStore(" + index + ")");
        try {
            if (mServiceIf != null) {
                int ret = mServiceIf.learnCandidateNoStore(mContext.getPackageName(),index) ? RETCODE_OK : RETCODE_NG;
                if (ret < 0) {
                    return mServiceIf.getErrorCode(mContext.getPackageName());
                }
                return ret;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "learnCandidateNoStore", e);
        }
        return RETCODE_NG;
    }

    /**
     * Learns the prediction candidate obtained by {@code getNextCandidate()}.
     * Not learns the predictive candidates connection.
     *
     * The index of the parameter starts at 0 using the order
     * in which candidates were obtained by {@code getNextCandidate()}.
     * For example, if the third candidate obtained by getNextCandidate()
     * is to be learned, it will be set to index 2.
     *
     * @param index         The index of words by getNextCandidate().
     * @return              Success:0  Failure:minus
     */
    public int learnCandidateNoConnect(int index) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::learnCandidateNoConnect(" + index + ")");
        try {
            if (mServiceIf != null) {
                int ret = mServiceIf.learnCandidateNoConnect(mContext.getPackageName(),index) ? RETCODE_OK : RETCODE_NG;
                if (ret < 0) {
                    return mServiceIf.getErrorCode(mContext.getPackageName());
                }
                return ret;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "learnCandidateNoConnect", e);
        }
        return RETCODE_NG;
    }

   /**
     * Learns the candidate.
     *
     * @param candidate     Candidate of selected word
     * @param stroke        Stroke of selected word
     * @return              Success:0  Failure:minus
     */
    public int learnWord(String candidate, String stroke) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::learnWord(" + candidate + "," + stroke + ")");
        try {
            if (mServiceIf != null) {
                int ret = mServiceIf.learnWord(mContext.getPackageName(),candidate, stroke) ? RETCODE_OK : RETCODE_NG;
                if (ret < 0) {
                    return mServiceIf.getErrorCode(mContext.getPackageName());
                }
                return ret;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "learnWord", e);
        }
        return RETCODE_NG;
    }

   /**
     * Learns the candidate.
     * Holds the pre-confirmed infomation without saving them into the Learning
     * dictionary.
     *
     * @param candidate     Candidate of selected word
     * @param stroke        Stroke of selected word
     * @return              Success:0  Failure:minus
     */
    public int learnWordNoStore(String candidate, String stroke) {
        if (DEBUG) Log.d(TAG, "IWnnEngineService::learnWordNoStore(" + candidate + "," + stroke + ")");
        try {
            if (mServiceIf != null) {
                int ret = mServiceIf.learnWordNoStore(mContext.getPackageName(), candidate, stroke) ? RETCODE_OK : RETCODE_NG;
                if (ret < 0) {
                    return mServiceIf.getErrorCode(mContext.getPackageName());
                }
                return ret;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "learnWordNoStore", e);
        }
        return RETCODE_NG;
    }

   /**
     * Learns the candidate.
     * Not learns the predictive candidates connection.
     *
     * @param candidate     Candidate of selected word
     * @param stroke        Stroke of selected word
     * @return              Success:0  Failure:minus
     */
    public int learnWordNoConnect(String candidate, String stroke) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::learnWordNoConnect(" + candidate + "," + stroke + ")");
        try {
            if (mServiceIf != null) {
                int ret = mServiceIf.learnWordNoConnect(mContext.getPackageName(),candidate, stroke) ? RETCODE_OK : RETCODE_NG;
                if (ret < 0) {
                    return mServiceIf.getErrorCode(mContext.getPackageName());
                }
                return ret;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "learnWordNoConnect", e);
        }
        return RETCODE_NG;
    }

    /**
     * Set the type of dictionary .
     *
     * @param configurationFile  Configuration file path.
     * @param language           Type of language.
     * @param dictionary         Type of dictionary.
     * @param flexibleSearch     Enable the flexible search.
     * @param tenKeyType         Is ten keyboard type.
     * @param emojiFilter        Emoji code is filter.
     * @param emailFilter        Email candidates is filter.
     * @param convertCandidates  Set the convert candidates.
     * @param learnNumber        Learn the numeric mixing candidate.
     * @return                   Success:0  Failure:minus
     */
    public int setDictionary(String configurationFile, int language, int dictionary, boolean flexibleSearch,
            boolean tenKeyType, boolean emojiFilter, boolean emailFilter, boolean convertCandidates,
            boolean learnNumber) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::setDictionary(" + configurationFile + "," + language + "," + dictionary
                + "," + flexibleSearch + "," + tenKeyType + "," + emojiFilter + "," + emailFilter + "," + convertCandidates
                + "," + learnNumber + ")");
        try {
            if (mServiceIf != null) {
                int ret = mServiceIf.setDictionary(mContext.getPackageName(), configurationFile, language, dictionary, flexibleSearch, tenKeyType,
                        emojiFilter, emailFilter, convertCandidates, learnNumber) ? RETCODE_OK : RETCODE_NG;
                if (ret < 0) {
                    return mServiceIf.getErrorCode(mContext.getPackageName());
                }
                return ret;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "setDictionary", e);
        }
        return RETCODE_NG;
    }

    /**
     * Set the type of dictionary with the decoemoji function.
     *
     * @param configurationFile  Configuration file path.
     * @param language           Type of language.
     * @param dictionary         Type of dictionary.
     * @param flexibleSearch     Enable the flexible search.
     * @param tenKeyType         Is ten keyboard type.
     * @param emojiFilter        Emoji code is filter.
     * @param decoemojiFilter    DecoEmoji code is filter.
     * @param emailFilter        Email candidates is filter.
     * @param convertCandidates  Set the convert candidates.
     * @param learnNumber        Learn the numeric mixing candidate.
     * @return                   Success:0  Failure:minus
     */
    public int setDictionaryDecoratedPict(String configurationFile, int language, int dictionary,
            boolean flexibleSearch, boolean tenKeyType, boolean emojiFilter,
            boolean decoemojiFilter, boolean emailFilter, boolean convertCandidates,
            boolean learnNumber) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::setDictionaryDecoratedPict("
                + configurationFile + "," + language + "," + dictionary + "," + flexibleSearch
                + "," + tenKeyType + "," + emojiFilter + "," + decoemojiFilter + "," + emailFilter
                + "," + convertCandidates + "," + learnNumber + ")");
        try {
            if (mServiceIf != null) {
                int ret = mServiceIf.setDictionaryDecoratedPict(mContext.getPackageName(),configurationFile, language,
                        dictionary, flexibleSearch, tenKeyType, emojiFilter, decoemojiFilter,
                        emailFilter, convertCandidates, learnNumber) ? RETCODE_OK : RETCODE_NG;
                if (ret < 0) {
                    return mServiceIf.getErrorCode(mContext.getPackageName());
                }
                return ret;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "setDictionaryDecoratedPict", e);
        }
        return RETCODE_NG;
    }

    /**
     * Check a candidate word if it's on the pseudo dictionary.
     *
     * @param index  index of the candidate word
     * @return       true : if the word is on the pseudo dictionary,
     *               false: if not
     */
    public boolean isGijiDic(int index) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::isGijiDic(" + index + ")");
        try {
            if (mServiceIf != null) {
                return mServiceIf.isGijiDic(mContext.getPackageName(),index);
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "isGijiDic", e);
        }
        return false;
    }

    /**
     * Set a pseudo dictionary filter.
     *
     * @param type  Pseudo dictionary filter
     * @return      Success:true  Failure:false
     */
    public boolean setGijiFilter(int[] type) {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::setGijiFilter()");
        try {
            if (mServiceIf != null) {
                return mServiceIf.setGijiFilter(mContext.getPackageName(),type);
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "setGijiFilter", e);
        }
        return false;
    }

    /**
     * Undo of learning.
     *
     * @return  Success:0  Failure:minus
     */
    public int undo() {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::undo()");
        try {
            if (mServiceIf != null) {
                int ret = mServiceIf.undo(mContext.getPackageName()) ? RETCODE_OK : RETCODE_NG;
                if (ret < 0) {
                    return mServiceIf.getErrorCode(mContext.getPackageName());
                }
                return ret;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "undo", e);
        }
        return RETCODE_NG;
    }

    /**
     * Starts the input.
     *
     * @return  Success:0  Failure:minus
     */
    public int startInput() {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::startInput()");
        try {
            if (mServiceIf != null) {
                mServiceIf.startInput(mContext.getPackageName());
                return RETCODE_OK;
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "startInput", e);
        }
        return RETCODE_NG;
    }

    /**
     * @deprecated used for testing only
     */
    public int getStatus() {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::getStatus()");
        try {
            if (mServiceIf != null) {
                return mServiceIf.getStatus(mContext.getPackageName());
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "getStatus", e);
        }
        return 0;
    }

    /**
     * @deprecated used for testing only
     */
    public int getDictionaryType() {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::getDictionaryType()");
        try {
            if (mServiceIf != null) {
                return mServiceIf.getDictionaryType(mContext.getPackageName());
            }
        } catch (Exception e) {
            Log.e("IWnnServiceConnector", "getDictionaryType", e);
        }
        return 0;
    }

    /**
     * Close the connection to the iWnnIME Service.
     *
     * @return            Success:0  Failure:minus
     */
    public int disconnect() {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::disconnect()");
        if (mIsBind && (mContext != null) && (mServiceConn != null)) {
            try {
                if (mServiceIf != null) {
                    mServiceIf.disconnect(mContext.getPackageName());
                }
            } catch (Exception e) {
                Log.e("IWnnServiceConnector", "disconnect", e);
            }
            mContext.unbindService(mServiceConn);
            mIsBind = false;
            mServiceIf = null;
            return RETCODE_OK;
        }
        return RETCODE_NG;
    }
    
    /**
     * You can know the connector is still connecting the service.
     *
     * @return  true: connecting  false: disconnected
     */
    public boolean isAlive() {
        if (DEBUG) Log.d(TAG, "IWnnServiceConnector::isAlive()");
        if (mServiceIf != null) {
            if (mServiceIf.asBinder().isBinderAlive() == false) {
                return false;
            }
            if (mContext == null) {
                return false;
            }
            try {
                return mServiceIf.isAlive(mContext.getPackageName());
            } catch (Exception e) {
                Log.e("IWnnServiceConnector", "isAlive", e);
            }
        }
        return false;
    }
}

