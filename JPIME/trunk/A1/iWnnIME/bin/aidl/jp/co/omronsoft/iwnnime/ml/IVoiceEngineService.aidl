/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/** Need to be same as the package name of iWnn IME */
package jp.co.omronsoft.iwnnime.ml;
/** Need to be same as the package name of iWnn IME */

import android.os.Bundle;

/**
 * iWnnEngine Service.
 */
interface IVoiceEngineService {
    /**
     * Initialize the internal state of the iWnn engine.
     *
     * @param packageName   PackageName.
     * @param password      Password.
     * @param initLevel     Initialize level.
     */
    void init(String packageName, String password, int initLevel);

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
     *   <li>To get strings of at least and no more than 5 characters, set minLen and maxLen to 5. </li>
     *   <li>To get strings of more than 3 characters, set minLen to 3 and maxLen to -1.</li>
     * </ul>
     *
     * @param packageName   PackageName.
     * @param stroke        The stroke (YOMI) of word.
     * @param minLen        The minimum length of a word to predict.
     *                      (minLen <= 0  : no limit)
     * @param maxLen        The maximum length of a word to predict.
     *                      (maxLen <= -1 : no limit)
     * @return              Plus value if there are candidates;
     *                      0 if there is no candidate;
     *                      Minus value if an error occurs.
     */
    int predict(String packageName, String stroke, int minLen, int maxLen);

   /**
     * Set enable consecutive phrase level conversion.
     *
     * Set whether to implement the consecutive phrase level conversion.
     * If you do not call this API once, disable consecutive phrase level conversion.
     *
     * @param packageName   PackageName.
     * @param enable        {@code true} Enable consecutive phrase level conversion.
     *                      {@code false} Disable consecutive phrase level conversion.
     * @return              0 if success setting.
     *                      Minus value if an error occurs.
     */
     int setEnableConsecutivePhraseLevelConversion(String packageName, boolean enable);

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
     * @param packageName           PackageName.
     * @param numberOfCandidates    The number of candidates
     *                                  returned by this method.
     * @return                      Candidate words.
     *                              If you want to get strokes of word,
     *                              call Bundle#getStringArray("stroke").
     *                              Similarly, candidates are "candidate".
     */
    Bundle getNextCandidate(String packageName, int numberOfCandidates);

    /**
     * Gets candidate strings  and DecoEmoji text (Annotation is set to strings)
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
     * @param packageName           PackageName.
     * @param numberOfCandidates    The number of candidates
     *                                  returned by this method.
     * @return                      Candidate words.
     *                              If you want to get strokes of word,
     *                              call Bundle#getStringArray("stroke").
     *                              Similarly, candidates are "candidate",
     *                              and DecoEmoji text (Annotation is set to strings)
     *                              are "annotation_candidate".
     */
    Bundle getNextCandidateWithAnnotation(String packageName, int numberOfCandidates);

    /**
     * Gets candidate strings  and DecoEmoji text (Annotation is set to strings)
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
     * @param packageName           PackageName.
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
    Bundle getNextCandidateWithAnnotation2(String packageName, int numberOfCandidates,int emojitype);

    /**
     * Learns the prediction candidate obtained by {@code getNextCandidate()}.
     *
     * The index of the parameter starts at 0 using the order
     * in which candidates were obtained by {@code getNextCandidate()}.
     * For example, if the third candidate obtained by getNextCandidate()
     * is to be learned, it will be set to index 2.
     *
     * @param packageName   PackageName.
     * @param index         The index of words by getNextCandidate().
     * @return              True if success, false otherwise.
     */
    boolean learnCandidate(String packageName, int index);

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
    Bundle convert(String packageName, String stroke, int divide);

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
    Bundle convertWithAnnotation(String packageName, String stroke, int divide);

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
    Bundle convertWithAnnotation2(String packageName, String stroke, int divide, int emojitype);

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
    int addWord(String packageName, String candidate, String stroke);

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
     * @param hinsi        Index of the lexical category group
     * @param type         Type of dictionaries(0:user dictionary, 1:learning dictionary, 2:pseudo dictionary)
     * @param relation     Relation learning flag(learning relations with the previous registered word. 0:don't learn 1:do learn)
     * @return             Success:0  Failure:minus
     */
    int addWordDetail(String packageName, String candidate, String stroke, int hinsi, int type, int relation);

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
    int searchWords(String packageName, String stroke);

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
     * @param method        Way of searching the dictionary
     * @param order         Order of searching the dictionary
     * @return              found:1  Not found:0  Failure:minus
     */
    int searchWordsDetail(String packageName, String stroke, int method, int order);

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
    boolean deleteWord(String packageName, String candidate, String stroke);

    /**
     * Backup the User dictionary or the Learning dictionary.
     *
     * @param packageName   PackageName.
     * @return              Success:true  Failure:false
     */
    boolean writeoutDictionary(String packageName);

    /**
     * Initialize the User dictionary or the Learning dictionary.
     *
     * @param packageName   PackageName.
     * @return              Success:true  Failure:fals
     */
    boolean initializeDictionary(String packageName);

    /**
     * Sets the User dictionary.
     *
     * Sets the User dictionary as the one to be used by the iWnn Engine.
     *
     * @param packageName   PackageName.
     */
    void setUserDictionary(String packageName);

    /**
     * Sets the Learning dictionary.
     *
     * Sets the Learning dictionary as the one to be used by the iWnn Engine.
     *
     * @param packageName   PackageName.
     */
    void setLearnDictionary(String packageName);

    /**
     * Sets the Normal dictionary.
     *
     * Sets the Normal dictionary as the one to be used by the iWnn Engine.
     *
     * @param packageNamePackageName.
     */
    void setNormalDictionary(String packageName);

   /**
     * Learns the prediction candidate obtained by {@code getNextCandidate()}.
     * Holds the pre-confirmed infomation without saving them into the Learning
     * dictionary.
     * The index of the parameter starts at 1 using the order
     * in which candidates were obtained by *getNextCandidate()*.
     * For example, if the third candidate obtained by
     * getNextCandidate() is to be learned, it will be set to index 3.
     *
     * @param packageName   PackageName.
     * @param index         Index of words to learn.
     * @return              True if success, false otherwise.
     */
    boolean learnCandidateNoStore(String packageName, int index);

    /**
     * Learns the prediction candidate obtained by {@code getNextCandidate()}.
     * Not learns the predictive candidates connection.
     *
     * The index of the parameter starts at 0 using the order
     * in which candidates were obtained by {@code getNextCandidate()}.
     * For example, if the third candidate obtained by getNextCandidate()
     * is to be learned, it will be set to index 2.
     *
     * @param packageName   PackageName.
     * @param index         Index of words to learn.
     * @return              True if success, false otherwise.
     */
    boolean learnCandidateNoConnect(String packageName, int index);

    /**
     * Learns the candidate.
     *
     * @param packageName   PackageName.
     * @param candidate     Candidate of selected word
     * @param stroke        Stroke of selected word
     */
    boolean learnWord(String packageName, String candidate, String stroke);

   /**
     * Learns the candidate.
     * Holds the pre-confirmed infomation without saving them into the Learning
     * dictionary.
     *
     * @param packageName   PackageName.
     * @param candidate     Candidate of selected word
     * @param stroke        Stroke of selected word
     * @return              Success:true Failure:false
     */
    boolean learnWordNoStore(String packageName, String candidate, String stroke);

    /**
     * Learns the candidate.
     * Not learns the predictive candidates connection.
     *
     * @param packageName   PackageName.
     * @param candidate     Candidate of selected word
     * @param stroke        Stroke of selected word
     */
    boolean learnWordNoConnect(String packageName, String candidate, String stroke);

    /**
     * Set the type of dictionary .
     *
     * @param packageName        PackageName.
     * @param configurationFile  Configuration file path.
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
    boolean setDictionary(String packageName, String configurationFile, int language, int dictionary, boolean flexibleSearch,
            boolean tenKeyType, boolean emojiFilter, boolean emailFilter, boolean convertCandidates,
            boolean learnNumber);

    /**
     * Set the type of dictionary with the decoemoji function.
     *
     * @param packageName        PackageName.
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
    boolean setDictionaryDecoratedPict(String packageName, String configurationFile, int language, int dictionary,
            boolean flexibleSearch, boolean tenKeyType, boolean emojiFilter,
            boolean decoemojiFilter, boolean emailFilter, boolean convertCandidates,
            boolean learnNumber);

    /**
     * Undo of learning.
     *
     * @param packageName   PackageName.
     * @return              Success:true  Failure:false
     */
    boolean undo(String packageName);

    /**
     * Check a candidate word if it's on the pseudo dictionary.
     *
     * @param packageName  PackageName.
     * @param index        Index of the candidate word
     * @return             true : If the word is on the pseudo dictionary,
     *                     false: If not
     */
    boolean isGijiDic(String packageName, int index);

    /**
     * Set a pseudo dictionary filter.
     *
     * @param packageName  PackageName.
     * @param type         Pseudo dictionary filter.
     * @return             Success:true  Failure:false
     */
    boolean setGijiFilter(String packageName, inout int[] type);

    /**
     * Starts the input.
     *
     * @param packageName   PackageName.
     */
    void startInput(String packageName);

    /**
     * @hide used for testing only
     *
     * @param packageName   PackageName.
     */
    int getStatus(String packageName);

    /**
     * @hide used for testing only
     *
     * @param packageName   PackageName.
     */
    int getDictionaryType(String packageName);

    /**
     * Get error code.
     *
     * @param packageName   PackageName.
     * @return              Error Code
     */
    int getErrorCode(String packageName);

    /**
     * disconnect from service.
     *
     * @param packageName   PackageName.
     */
    void disconnect(String packageName);

    /**
     * You can know the connector is still connecting the service.
     *
     * @param packageName   PackageName.
     * @return              Alive:true  NoAlive:false
     */
    boolean isAlive(String packageName);
}
