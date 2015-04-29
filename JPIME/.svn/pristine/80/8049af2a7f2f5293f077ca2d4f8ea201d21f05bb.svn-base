/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml.iwnn;

/**
 * iWnn Native Interface Class IWnnNative
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
class IWnnNative {
    public final static native int getInfo();
    public final static native int setActiveLang(int handle, int no);
    public final static native int setBookshelf(int handle, int no);
    public final static native int unmountDics(int handle);
    public final static native void destroy(int handle);
    public final static native int setInput(int handle, String str);
    public final static native String getInput(int handle);
    public final static native int setdicByConf(int handle, String conf_file, int lang);
    public final static native int init(int handle, String dirPath);
    public final static native int getState(int handle);
    public final static native int setState(int handle);
    public final static native void setStateSystem(int handle, int situation, int bias);
    public final static native int forecast(int handle, int minLen, int maxLen, int headConv);
    public final static native int select(int handle, int segment, int cand, int mode);
    public final static native int searchWord(int handle, int method, int order);
    public final static native String getWord(int handle, int index, int type);
    public final static native int addWord(int handle, String yomi, String repr, int pos, int dtype, int con);
    public final static native int deleteWord(int handle, int index);
    public final static native int deleteSearchWord(int handle, int index);
    public final static native int conv(int handle, int divide_pos);
    public final static native int noconv(int handle);
    public final static native String getWordStroke(int handle, int segment, int cand);
    public final static native String getWordString(int handle, int segment, int cand);
    public final static native String getSegmentStroke(int handle, int segment);
    public final static native String getSegmentString(int handle, int segment);
    public final static native int deleteDictionary(int handle, int type, int language, int dictionary);
    public final static native int resetExtendedInfo(String fileName);
    public final static native int setFlexibleCharset(int handle, int charset, int keytype);
    public final static native int WriteOutDictionary(int handle, int dictype);
    public final static native int syncDictionary(int handle, int dictype);
    public final static native void sync(int handle);
    public final static native int isLearnDictionary(int handle, int index);
    public final static native int isGijiResult(int handle, int index);
    public final static native int undo(int handle, int count);
    public final static native void emojiFilter(int handle, int enabled);
    public final static native void emailAddressFilter(int handle, int enabled);
    public final static native void splitWord(int handle, String input, int[] result);
    public final static native void getMorphemeWord(int handle, int index, String[] word);
    public final static native short getMorphemeHinsi(int handle, int index);
    public final static native void getMorphemeYomi(int handle, int index, String[] yomi);
    public final static native int deleteAdditionalDictionary(int handle, int type);
    public final static native int createAdditionalDictionary(int handle, int type);
    public final static native int saveAdditionalDictionary(int handle, int type);
    public final static native int deleteAutoLearningDictionary(int handle, int type);
    public final static native int createAutoLearningDictionary(int handle, int type);
    public final static native int saveAutoLearningDictionary(int handle, int type);
    public final static native void setDownloadDictionary(int handle, int index, String name,
            String file, int convertHigh, int convertBase, int predictHigh, int predictBase,
            int morphoHigh, int morphoBase, boolean cache, int limit);
    public final static native int refreshConfFile(int handle);
    public final static native int setServicePackageName(int handle, String packageName, String password);

    public final static native void decoemojiFilter(int handle, int enabled);
    public final static native void controlDecoEmojiDictionary(int handle, String id, String yomi, int hinsi, int control_flag);
    public final static native int checkDecoEmojiDictionary(int handle);
    public final static native int resetDecoEmojiDictionary(int handle);
    public final static native int checkDecoemojiDicset(int handle);
    public final static native int getgijistr(int handle, int devide_pos, int type);
    public final static native int deleteLearnDicDecoEmojiWord(int handle);
    public final static native int setGijiFilter(int handle, int[] type);
    public final static native int deleteDictionaryFile(String file);

    public final static native int getWordInfoStemAttribute(int handle, int index);

    public final static native int getSegmentStrokeLength(int handle, int segment);
    public final static native int getWordStrokeLength(int handle, int cand);

    /**
     * Default constructor
     */
    public IWnnNative() {
        super();
    }
}
