/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/**
  * Copyright (C) 2012 NTT DOCOMO, INC. All Rights Reserved.
  */
package com.nttdocomo.android.voiceeditorif;

import android.os.Bundle;

/**
 * This interface provides the API for VoiceEditorService.
 */
interface IVoiceEditorService {

    /**
     * Disconnect from service.
     *
     * @return              Success:0  Failure:-1
     */
    int disconnect();

    /**
     * Check the connector is still connecting the service.
     *
     * @return              Alive:true  NoAlive:false
     */
    boolean isConnected();

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
    int init();

    /**
     * Setting the search condition candidate.
     *
     * @param dictionaryType     Type of used dictionary.
     * @param emojiFilter        true if an emoji filter will be enable.
     * @param decoemojiFilter    true if an decoemoji filter will be enable.
     * @return                   Success:0 Failure:-1
     */
    int setDictionary(int dictionaryType, boolean emojiFilter, boolean decoemojiFilter);

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
    int searchCandidate(String phonetic, int minLen, int maxLen);

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
    Bundle getCandidate(int maxCandidates);

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
    int memorizeCandidate(int index, boolean learnFlag, boolean connectFlag);

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
    int addWord(String candidate, String phonetic, int attribute, int relation);

    /**
     * Search words on the user dictionary.
     *
     * Get search results by calling {@link #getCandidate(int)}.
     * Search method is a matching front.
     *
     * @param phonetic      phonetic of word to search from dictionary.
     * @return              found:1  Not found:0  Failure:-1
     */
    int searchWord(String phonetic);

   /**
     * Input start of "Deco Emoji".
     *
     * @return              Success:0  Failure:-1
     */
    int startInputDecoEmoji();

   /**
     * Check a candidate word if it's on the pseudo dictionary.
     *
     * @param index         The index of words by {@link #getCandidate(int)}.
     * @return              true If the candidate is pseudo.
     *                      false If the candidate is not pseudo.
     */
    boolean isPseudoCandidate(int index);

   /**
     * Get IMESetting Information.
     *
     * @return              IMESetting Information.
     *                      If you want to get IMESetting Information,
     *                      call Bundle#getBoolean("keySound") or 
     *                      call Bundle#getBoolean("keyVibration") or 
     *                      call Bundle#getBoolean("previewPopup").
     */
    Bundle getIMESettingInfo();

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
    Bundle splitWord(String input, int readingsMax);
    
}
    