/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml.decoemoji;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Annotation;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;

import jp.co.omronsoft.android.emoji.EmojiAssist;
import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.DecoEmojiAttrInfo;
import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.DecoEmojiContract;
import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.IDecoEmojiConstant;
import jp.co.omronsoft.iwnnime.ml.ComposingText;
import jp.co.omronsoft.iwnnime.ml.OpenWnn;
import jp.co.omronsoft.iwnnime.ml.WnnWord;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/** Utility class for Deco Emoji. */
public class DecoEmojiUtil {


    /** Info of DecoEmoji */
    public static class DecoEmojiInfo {
        /** Uri  */
        public String uri = "";
        /** Width */
        public int width = 0;
        /** Height */
        public int height = 0;
        /** Kind */
        public int kind = 0;
        /** Pop */
        public int pop = 0;

        /** Selection Info of DecoEmoji */
        public static final String[] SELECTION_DECOEMOJI_INFO = {DecoEmojiContract.DecoEmojiInfoColumns.URI,
             DecoEmojiContract.DecoEmojiInfoColumns.WIDTH, DecoEmojiContract.DecoEmojiInfoColumns.HEIGHT,
             DecoEmojiContract.DecoEmojiInfoColumns.KIND, DecoEmojiContract.DecoEmojiInfoColumns.DECOME_POP_FLAG};

        /** Set DecoEmoji Info */
        public static void setDecoEmojiInfo(Cursor cursor, DecoEmojiInfo decoEmojiInfo) {
            if (cursor == null || decoEmojiInfo == null) {
                return;
            }
            int uriIndex = cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.URI);
            int widthIndex = cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.WIDTH);
            int heightIndex = cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.HEIGHT);
            int kindIndex = cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.KIND);
            int popIndex = cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.DECOME_POP_FLAG);

            if (uriIndex < 0 || widthIndex < 0 || heightIndex < 0 || kindIndex < 0 || popIndex < 0) {
                return;
            }

            decoEmojiInfo.uri = cursor.getString(uriIndex);
            decoEmojiInfo.width = cursor.getInt(widthIndex);
            decoEmojiInfo.height = cursor.getInt(heightIndex);
            decoEmojiInfo.kind = cursor.getInt(kindIndex);
            decoEmojiInfo.pop = cursor.getInt(popIndex);
        }
    }

    /** [For debugging] TAG character string of debugging log */
    private static final String TAG = "iWnn";

    /** Specified characters that indicate Deco Emoji. */
    public static final int DECOEMOJI_ID_PREFIX = 0x1b;

    /** Lenght of DecoEmoji ID characters. */
    public static final int DECOEMOJI_ID_LENGTH = 5;

    /** Status of getting spanned text (span decoemoji text) */
    private static final int STATE_SPANNED_DECOEMOJI = 1;
    /** Status of getting spanned text (not decoemoji text) */
    private static final int STATE_NOT_DECOEMOJI = 0;
    /** Status of getting spanned text (disable DecoEmoji) */
    private static final int STATE_ERROR_DISABLE_DECOEMOJI = -100;
    /** Status of getting spanned text (invalid ID) */
    private static final int STATE_ERROR_INVALID_ID = -101;
    /** Status of getting spanned text (not bind DecoEmojiManager) */
    private static final int STATE_ERROR_NOT_BIND_MANAGER = -102;
    /** Status of getting spanned text (RemoteException for DecoEmojiManager) */
    private static final int STATE_ERROR_REMOTE_EXCEPTION_MANAGER = -103;
    /** Status of getting spanned text (not found file) */
    private static final int STATE_ERROR_NOT_FOUND_FILE = -104;
    /** Status of not EmojiType */
    private static final int STATE_ERROR_NOT_EMOJI_TYPE = -105;
    /** Count of getting DecoEmojiInfo */
    private static final int DECOEMOJI_INFO_CNT = 100;


    /** Whether DecoEmoji is enable. */
    private static boolean sEnableDecoEmoji;

    /** Invalid emojiType. */
    public static final int EMOJITYPE_INVALID = -1;

    /** Selection Info of DecoEmoji */
    public static final String[] SELECTION_DECOEMOJI_INFO = {DecoEmojiContract.DecoEmojiInfoColumns.URI,
         DecoEmojiContract.DecoEmojiInfoColumns.WIDTH, DecoEmojiContract.DecoEmojiInfoColumns.HEIGHT,
         DecoEmojiContract.DecoEmojiInfoColumns.KIND, DecoEmojiContract.DecoEmojiInfoColumns.DECOME_POP_FLAG};

    /**
     * Define whether text should be converted.
     * @param enable  true if text should be converted.
     */
    public static void setConvertFunctionEnabled(boolean enable) {
        sEnableDecoEmoji = enable;
    }

    /**
     * Gets the text to which DecoEmojiSpan is set.
     * @param uri  The content URI.
     * @param width  The context width.
     * @param height  The context height.
     * @param kind  The context kind.
     * @param decome_pop_flag  The context decome pop.
     * @return The text to which DecoEmojiSpan is set.
     */
    public static CharSequence getDecoEmojiText(String uri, int width, int height, int kind, int decome_pop_flag) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        text.append(EmojiAssist.DECO_EMOJI_CHAR);

        Object span;
        String value = "";
        StringBuffer tmpValue = new StringBuffer(uri);
        tmpValue.append(EmojiAssist.SPLIT_KEY);
        tmpValue.append(width);
        tmpValue.append(EmojiAssist.SPLIT_KEY);
        tmpValue.append(height);
        tmpValue.append(EmojiAssist.SPLIT_KEY);
        tmpValue.append(kind);
        tmpValue.append(EmojiAssist.SPLIT_KEY);
        tmpValue.append(decome_pop_flag);
        value = tmpValue.toString();
        span = new Annotation(EmojiAssist.DECO_EMOJI_KEY, value);
        text.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return text;
    }

    /**
     * Get spanned text from candidate in word.
     * DecoEmoji ID is converted to DecoEmojiSpan.
     *
     * @param word  The candidate word.
     * @return Spanned text.
     */
    public static CharSequence getSpannedCandidate(WnnWord word) {
        Context context = OpenWnn.getContext();
        if (context == null) {
            return "";
        }
        if (sEnableDecoEmoji) {
            if ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_DECOEMOJI) != 0) {
                EmojiAssist.DecoEmojiTextInfo decoEmojiTextInfo = new EmojiAssist.DecoEmojiTextInfo();
                if (word.decoEmojiInfo != null) {
                    int width = word.decoEmojiInfo.getWidth();
                    int height = word.decoEmojiInfo.getHeight();
                    if (width > 0 && height > 0) {
                        decoEmojiTextInfo.setWidth(width);
                        decoEmojiTextInfo.setHeight(height);
                        decoEmojiTextInfo.setKind(word.decoEmojiInfo.getKind());
                        decoEmojiTextInfo.setPop(word.decoEmojiInfo.getPop());
                    }
                }
                decoEmojiTextInfo.setUri("file://" + word.candidate);
                decoEmojiTextInfo.setEmojiType(OpenWnn.getEmojiType());
                decoEmojiTextInfo.setContext(context);
                return EmojiAssist.getInstance().getDecoEmojiText(decoEmojiTextInfo);
            }
        }
        return getSpannedText(word.candidate);
    }

    /**
     * Get spanned text from String.
     * DecoEmoji ID is converted to DecoEmojiSpan.
     *
     * @param text  The text.
     * @return Spanned text.
     */
    public static CharSequence getSpannedText(CharSequence text) {
        return getSpannedText(text, new int[1]);
    }

    /**
     * Get spanned text from String.
     * DecoEmoji ID is converted to DecoEmojiSpan.
     *
     * @param text     The text.
     * @param status   The status.
     * @return Spanned text.
     */
    public static CharSequence getSpannedText(
            CharSequence text, int[] status) {
        status[0] = STATE_NOT_DECOEMOJI;

        if (text.toString().indexOf(DECOEMOJI_ID_PREFIX) < 0) {
            return text;
        }

        if (!sEnableDecoEmoji) {
            status[0] = STATE_ERROR_DISABLE_DECOEMOJI;

            if (OpenWnn.getCurrentIme() != null) {
                return text;
            }
            return "";
        }

        Context context = OpenWnn.getContext();
        if (context == null) {
            return "";
        }

        byte[] charArray;
        try {
            charArray = text.toString().getBytes(OpenWnn.CHARSET_NAME_UTF8);
            int left = 0;
            int last = charArray.length;
            SpannableStringBuilder builder = new SpannableStringBuilder();
            int emojiType = getEditorEmojiType();
            if (emojiType == EMOJITYPE_INVALID) {
                emojiType = 0;
            }
            for (int i = 0; i < last;) {
                if (charArray[i] == DECOEMOJI_ID_PREFIX) {
                    int count = (i - left);
                    if (0 < count) {
                        builder.append(new String(charArray, left, count, OpenWnn.CHARSET_NAME_UTF8));
                    }
                    EmojiAssist.DecoEmojiTextInfo info = getDecoEmojiInfo(convertToEmojiIdInt(charArray, i), status);
                    File file = new File(info.getUri());
                    if (!file.exists()) {
                        if (status[0] == STATE_NOT_DECOEMOJI) {
                            status[0] = STATE_ERROR_NOT_FOUND_FILE;
                        }
                        return "";
                    }

                    CharSequence decoEmoji;
                    info.setUri("file://" + info.getUri());
                    info.setEmojiType(OpenWnn.getEmojiType());
                    info.setContext(context);
                    decoEmoji = EmojiAssist.getInstance().getDecoEmojiText(info);

                    int convEmojiType = DecoEmojiContract.convertEmojiType(info.getKind());
                    if ((convEmojiType & emojiType) == 0) {
                        return "";
                    }
                    builder.append(decoEmoji);
                    i += DECOEMOJI_ID_LENGTH;
                    left = i;
                } else { // normal string
                    i++;
                }
            }

            if (0 < builder.length()) {
                if (left < last) {
                    builder.append(new String(charArray, left, last - left, OpenWnn.CHARSET_NAME_UTF8));
                }

                status[0] = STATE_SPANNED_DECOEMOJI;
                return builder;
            } else {
                return text;
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "DecoEmojiUtil#getSpannedText() Exception", e);
            return text;
        }
    }

    /**
     * Convert byte array to integer.
     *
     * @param charArray  Number characters array. (ex. ['1', '2', '3', '4', '5'])
     * @param offset   Start index.
     * @return DecoEmoji ID
     */
    public static int convertToEmojiIdInt(byte[] charArray, int offset) {
        if (charArray.length < (offset + DECOEMOJI_ID_LENGTH)) {
            return -1;
        }

        int id = 0;
        int last = offset + DECOEMOJI_ID_LENGTH;
        for (int i = offset + 1; i < last; i++) {
            int digit = charArray[i] - 0x30; // char -> int
            if (digit < 0) {
                Log.e(TAG, "digit = " + digit);
                return -1;
            }
            id += digit;
            id *= 10;
        }
        return id / 10;
    }

    /**
     * Set DecoEmoji Info.
     * @param cursor Emoji list cursor.
     * @param decoEmojiInfo The decoEmoji info.
     * @return true if the setting success .
     */
    public static boolean setDecoEmojiInfo(Cursor cursor, EmojiAssist.DecoEmojiTextInfo decoEmojiInfo) {
        if (cursor == null || decoEmojiInfo == null) {
            return false;
        }

        int uriIndex = cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.URI);
        int widthIndex = cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.WIDTH);
        int heightIndex = cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.HEIGHT);
        int kindIndex = cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.KIND);
        int popIndex = cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.DECOME_POP_FLAG);

        if (uriIndex < 0 || widthIndex < 0 || heightIndex < 0 || kindIndex < 0 || popIndex < 0) {
            return false;
        }

        decoEmojiInfo.setUri(cursor.getString(uriIndex));
        decoEmojiInfo.setWidth(cursor.getInt(widthIndex));
        decoEmojiInfo.setHeight(cursor.getInt(heightIndex));
        decoEmojiInfo.setKind(cursor.getInt(kindIndex));
        decoEmojiInfo.setPop(cursor.getInt(popIndex));
        return true;
    }

    /**
     * Get DecoEmojiInfo
     *
     * @param decoEmojiId  DecoEmoji ID
     * @param status       The status.
     * @return URI
     */
    private static EmojiAssist.DecoEmojiTextInfo getDecoEmojiInfo(int decoEmojiId, int[] status) {
        EmojiAssist.DecoEmojiTextInfo info = new EmojiAssist.DecoEmojiTextInfo();
        if (decoEmojiId < 0) {
            status[0] = STATE_ERROR_INVALID_ID;
            return info;
        }
        Context context = OpenWnn.getContext();
        if (context == null) {
            return info;
        }

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver()
                         .query(ContentUris.withAppendedId(DecoEmojiContract.CONTENT_DECOINFOLIST_URI, decoEmojiId)
                         , SELECTION_DECOEMOJI_INFO, null, null, null);

            if ( cursor != null && cursor.getCount() != 0) {
                cursor.moveToFirst();
                setDecoEmojiInfo(cursor, info);
            } else {
                status[0] = STATE_NOT_DECOEMOJI;
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
       } finally {
            if ( cursor != null ) {
                cursor.close();
            }
       }
        return info;
    }

    /**
     * Convert ComposingText to CommitText with DecoEmoji span.
     *
     * @param  composingText  ComposingText
     * @param  targetLayer  Layer of converting
     * @param  from  Convert range from
     * @param  to  Convert range to
     * @return  CommitText
     */
    public static CharSequence convertComposingToCommitText(ComposingText composingText,
            int targetLayer, int from, int to) {
        CharSequence composingString = composingText.toString(targetLayer, from, to);
        if (targetLayer == ComposingText.LAYER2) {
            return getSpannedText(composingString);
        } else {
            return composingString;
        }
    }

    /**
     * Returns whether the given text contains any Deco Emoji characters.
     * @param  text A text to be checked.
     * @return true if the text contains any Deco Emoji characters.
     */
    public static boolean isDecoEmoji(CharSequence text) {
        boolean ret = true;
        if (text.toString().indexOf(DECOEMOJI_ID_PREFIX) < 0) {
            ret = false;
        }
        return ret;
    }

    /**
     * getDecoEmojiDicInfo.
     * @param  decoemoji_id decoemoji_id
     */
    public static void getDecoEmojiDicInfo(int decoemoji_id) {

        Context context = OpenWnn.getContext();
        if (context == null) {
            return;
        }
        int timestamp = 0;

        Cursor tCursor = null;
        Cursor dDursor = null;
        Cursor cursor = null;
        try {

            if (decoemoji_id > -1) {
                // Get TimeStamp
                String tColumn[] = {DecoEmojiContract.DecoEmojiInfoColumns.TIMESTAMP};
                String tSelection = DecoEmojiContract.DecoEmojiInfoColumns.DECOEMOJI_ID + " = " + decoemoji_id;
                tCursor = context.getContentResolver().query(DecoEmojiContract.CONTENT_DECOINFOLIST_URI, tColumn
                           , tSelection, null, null);

                if ( tCursor != null ) {
                     if (tCursor.getCount() > 0 ) {
                         tCursor.moveToNext();
                         timestamp = tCursor.getInt(tCursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.TIMESTAMP));
                     }
                 }
             }

            // Get DecoEmojiID
            String dColumn[] = {DecoEmojiContract.DecoEmojiInfoColumns.DECOEMOJI_ID};
            String dSelection = "( " + DecoEmojiContract.DecoEmojiInfoColumns.DECOEMOJI_ID + " > " + decoemoji_id + " or " + DecoEmojiContract.DecoEmojiInfoColumns.TIMESTAMP + " > " + timestamp + " )";
            String dOrder = DecoEmojiContract.DecoEmojiDicColumns.DECOEMOJI_ID;

            dDursor = context.getContentResolver().
                                query(DecoEmojiContract.CONTENT_DECOINFOLIST_URI, dColumn, dSelection, null, dOrder );

            if (dDursor != null ) {
                if (dDursor.getCount() > 0) {
                    dDursor.moveToFirst();
                    int startId = dDursor.getInt(dDursor.getColumnIndex(DecoEmojiContract.DecoEmojiDicColumns.DECOEMOJI_ID));
                    dDursor.moveToLast();
                    int endId = dDursor.getInt(dDursor.getColumnIndex(DecoEmojiContract.DecoEmojiDicColumns.DECOEMOJI_ID));

                    // Get DicInfo
                    String column[] = {DecoEmojiContract.DecoEmojiDicColumns.DECOEMOJI_ID,
                               DecoEmojiContract.DecoEmojiDicColumns.DECOEMOJI_NAME,
                               DecoEmojiContract.DecoEmojiDicColumns.DECOEMOJI_PART,
                               DecoEmojiContract.DecoEmojiDicColumns.DECOEMOJI_NOTE};

                    String selection = DecoEmojiContract.DecoEmojiDicColumns.DECOEMOJI_ID + " >= " + startId + " and " + DecoEmojiContract.DecoEmojiDicColumns.DECOEMOJI_ID + " <= " + endId;
                    String order = DecoEmojiContract.DecoEmojiDicColumns.DECOEMOJI_ID + " ASC";
                    cursor = context.getContentResolver().query(DecoEmojiContract.CONTENT_DECODICLIST_URI, column
                               , selection, null, order);

                    if ( cursor != null && cursor.getCount() > 0) {
                        List<DecoEmojiAttrInfo> decoList = new ArrayList<DecoEmojiAttrInfo>();
                        int preId = -1;
                        int dicCnt = 0;
                        DecoEmojiAttrInfo attrinfo = null;
                        while (cursor.moveToNext()) {
                            int id = cursor.getInt(cursor.getColumnIndex(DecoEmojiContract.DecoEmojiDicColumns.DECOEMOJI_ID));
                            String name = cursor.getString(cursor.getColumnIndex(DecoEmojiContract.DecoEmojiDicColumns.DECOEMOJI_NAME));
                            int partId = cursor.getInt(cursor.getColumnIndex(DecoEmojiContract.DecoEmojiDicColumns.DECOEMOJI_PART));
                            String note = cursor.getString(cursor.getColumnIndex(DecoEmojiContract.DecoEmojiDicColumns.DECOEMOJI_NOTE));

                            if (id != preId) {
                                // Max Register Size is DECOEMOJI_INFO_CNT(100)
                                if (decoList.size() >= DECOEMOJI_INFO_CNT - 1) {
                                    break;
                                }
                                if (attrinfo != null ) {
                                    decoList.add(attrinfo);
                                }
                                attrinfo = new DecoEmojiAttrInfo();
                                dicCnt = 0;
                            } else {
                                dicCnt++;
                            }
                            if (dicCnt >= IDecoEmojiConstant.MAX_CONTENT ) {
                                continue;
                            }
                            if (attrinfo != null ) {
                                attrinfo.setID(id);
                                attrinfo.setName(dicCnt, name);
                                Integer part = new Integer(partId);
                                attrinfo.setPart(dicCnt, part.byteValue());
                                attrinfo.setNote(dicCnt, note);
                            }
                            preId = id;
                        }
                        if (attrinfo != null ) {
                            decoList.add(attrinfo);
                        }
                        // Send BroadCasts Dictionary Info of DecoEmoji
                        Intent intent = new Intent(context.getPackageName());

                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList(IDecoEmojiConstant.BROADCAST_DATA_TAG, (ArrayList<? extends Parcelable>) decoList);
                        bundle.putInt(IDecoEmojiConstant.BROADCAST_TYPE_TAG, IDecoEmojiConstant.FLAG_INSERT);
                        intent.putExtras(bundle);
                        context.sendBroadcast(intent);
                    }
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
       } finally {
            if ( tCursor != null ) {
                tCursor.close();
            }
            if ( dDursor != null ) {
                dDursor.close();
            }
            if ( cursor != null ) {
                cursor.close();
            }
       }

        return;
    }

    /**
     * Get emojiType
     *
     * @param decoEmojiId  DecoEmoji ID
     * @return emojiType
     */
    public static int getEmojiType(int decoEmojiId) {
        if (decoEmojiId < 0) {
            return 0;
        }

        Context context = OpenWnn.getContext();
        if (context == null) {
            return 0;
        }

        Cursor cursor = null;
        int type = 0;
        try {
            cursor = context.getContentResolver().query(ContentUris.withAppendedId(DecoEmojiContract.CONTENT_DECOINFOLIST_URI, decoEmojiId),
                                                                null, null, null, null);

            if (cursor != null && cursor.getCount() != 0) {
                cursor.moveToFirst();
                type = cursor.getInt(cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.KIND));
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
       } finally {
            if ( cursor != null ) {
                cursor.close();
            }
       }
        return type;
    }

    /**
     * Get editor emojiType.
     * @return emojiType. (-1:Invalid setting emojiType)
     */
    public static int getEditorEmojiType() {
        int emojiType = OpenWnn.getEmojiType();
        if (emojiType == 0 || emojiType > EmojiAssist.EMOJITYPE_COMB_ANY) {
            // if it does not emojiType, get a 20*20.
            emojiType = EmojiAssist.EMOJITYPE_PICTD_SQ20;
        } else {
            emojiType = (emojiType & EmojiAssist.EMOJITYPE_COMB_SQ);
            if (emojiType == 0) {
                // emojiType invalid.
                return EMOJITYPE_INVALID;
            }
        }
        return emojiType;
    }

}
