/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml.decoemoji;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.RemoteException;
import android.util.Log;

import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.DecoEmojiCategoryInfo;
import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.DecoEmojiContract;
import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.IDecoEmojiConstant;
import jp.co.omronsoft.android.emoji.EmojiAssist;
import jp.co.omronsoft.iwnnime.ml.OpenWnn;

import java.util.ArrayList;
import java.util.Locale;

/** The list of DecoEmoji. */
public class DecoEmojiList {
    /** [For debugging] TAG character string of debugging log */
    private static final String TAG = "iWnn";

    /** Max number of category. */
    private static final int MAX_CATEGORY = 200;

    /** Categories of DecoEmoji. */
    private ArrayList<DecoEmojiCategoryInfo> mCategories = new ArrayList<DecoEmojiCategoryInfo>();

    /** Next index of DecoEmoji Categories. */
    private int mCategoriesNextIndex;

    /** list of DecoEmoji. */
    private ArrayList<EmojiAssist.DecoEmojiTextInfo> mDecoEmojiList = new ArrayList<EmojiAssist.DecoEmojiTextInfo>();

    /** Next index of DecoEmoji list. */
    private int mDecoEmojiListNextIndex;

    /** Next item. */
    private EmojiAssist.DecoEmojiTextInfo mNextItem;

    /** Current category id. */
    private int mCategoryId;

    /** Current page id. */
    private int mPageId;

    /** Whether Index of URI list reaches end of list. */
    private boolean mHasEnded;

    /** Whether locale is japanese. */
    private boolean mIsJapanese;

    /** List of created DecoEmoji. */
    private ArrayList<EmojiAssist.DecoEmojiTextInfo> mCreatedList = new ArrayList<EmojiAssist.DecoEmojiTextInfo>();

    /** context. */
    private Context mContext;

    /** Constructor. */
    public DecoEmojiList(Context context) {
        mContext = context;
    }

    /** Initialize list. */
    public void initializeList() {
        mCreatedList.clear();

        mHasEnded = true;

        mIsJapanese = Locale.getDefault().getLanguage().equals(Locale.JAPANESE.toString());
        mCategories.clear();
        mCategoriesNextIndex = 0;

        int emojiType = DecoEmojiUtil.getEditorEmojiType();
        if (emojiType != DecoEmojiUtil.EMOJITYPE_INVALID) {
            Cursor cursor = null;
            try {
                //get CategoryList
                String emojiTypeColum = DecoEmojiContract.makeStringEmojiKind((byte)emojiType);
                String where = DecoEmojiContract.DecoEmojiInfoColumns.KIND + " IN (" + emojiTypeColum + ")";
                String order = DecoEmojiContract.DecoEmojiInfoColumns.CATEGORY_PRESET_ID  + " asc" ;

                cursor = mContext.getContentResolver().query(DecoEmojiContract.CONTENT_DECOINFOLIST_URI, null, where, null, order);

                if (cursor != null ) {
                    if (cursor.getCount() > 0) {
                        int count = 0;
                        boolean overlap = false;
                        while (cursor.moveToNext()) {
                            overlap = false;
                            int categoryId = cursor.getInt(cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.CATEGORY_ID));
                            DecoEmojiCategoryInfo values = new DecoEmojiCategoryInfo();
                            values.setCategoryId(categoryId);
                            values.setCategoryName_jpn(cursor.getString(cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.CATEGORY_NAME_JPN)));
                            values.setCategoryName_eng(cursor.getString(cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.CATEGORY_NAME_ENG)));

                            // check Duplicate
                            for (int cnt = 0; cnt < mCategories.size(); cnt++) {
                                if (mCategories.get(cnt).getCategoryId() == categoryId) {
                                    overlap = true;
                                    break;
                                }
                            }

                            if (!overlap) {
                                mCategories.add(values);
                                count++;
                            }
                        }
                    }
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
           } finally {
                if ( cursor != null ) {
                    cursor.close();
                }
           }
        }
        mHasEnded = false;
        mNextItem = createNextCategory();
    }

    /**
     * Get a item.
     * @param index Index of items.
     * @retrun The item.
     */
    public EmojiAssist.DecoEmojiTextInfo getItem(int index) {
        if (index < mCreatedList.size()) {
            EmojiAssist.DecoEmojiTextInfo ret = mCreatedList.get(index);
            return ret;
        }

        if (index != mCreatedList.size()) {
            Log.e(TAG, "Request index is mismatch: " + index + "/" + mCreatedList.size());
        }

        if (mHasEnded) {
            return null;
        }

        EmojiAssist.DecoEmojiTextInfo ret = mNextItem;

        boolean nextCategory = false;
        if (mDecoEmojiList.size() <= mDecoEmojiListNextIndex) {
            createNextDecoEmojiList();
            if (mDecoEmojiList.size() == 0) {
                nextCategory = true;
            }
        }

        if (nextCategory) {
            mNextItem = createNextCategory();
        } else {
            mNextItem = mDecoEmojiList.get(mDecoEmojiListNextIndex);
            mDecoEmojiListNextIndex++;
        }

        mCreatedList.add(ret);
        return ret;
    }

    /**
     * Create text of category.
     * @return Category candidate.
     */
    private EmojiAssist.DecoEmojiTextInfo createNextCategory() {
        if (mCategories.size() <= mCategoriesNextIndex) {
            mHasEnded = true;
            return null;
        }

        DecoEmojiCategoryInfo info = mCategories.get(mCategoriesNextIndex);
        mCategoriesNextIndex++;

        createDecoEmojiList(info.getCategoryId());

        String categoryText;
        if (mIsJapanese) {
            categoryText = info.getCategoryName_jpn();
        } else {
            categoryText = info.getCategoryName_eng();
        }
        EmojiAssist.DecoEmojiTextInfo ret = new EmojiAssist.DecoEmojiTextInfo();
        ret.setUri("[" + categoryText + "]"); 
        return ret;
    }

    /**
     * Create uri list.
     * @param categoryId  Category ID of DecoEmoji list.
     */
    private void createDecoEmojiList(int categoryId) {
        mCategoryId = categoryId;
        mPageId = -1;
        createNextDecoEmojiList();
    }

    /**
     * Create next DecoEmoji list.
     */
    private void createNextDecoEmojiList() {
        mPageId++;
        getDecoEmojiList();
    }

    /**
     * Create DecoEmoji list.
     */
    private void getDecoEmojiList() {
        mDecoEmojiList.clear();
        mDecoEmojiListNextIndex = 0;

        int start = (mPageId * MAX_CATEGORY);

        int emojiType = DecoEmojiUtil.getEditorEmojiType();
        if (emojiType == DecoEmojiUtil.EMOJITYPE_INVALID) {
            return;
        }

         // get Count of EmojiD
        int count = 0;
        String emojiTypeColum = DecoEmojiContract.makeStringEmojiKind((byte)emojiType);
        String where = " (" + DecoEmojiContract.DecoEmojiInfoColumns.CATEGORY_ID + " = ?) and ("
                        + DecoEmojiContract.DecoEmojiInfoColumns.KIND + " IN (" + emojiTypeColum + "))"
                        + "and " + DecoEmojiContract.DecoEmojiInfoColumns.KIND + " <> " + DecoEmojiContract.KIND_PICTURE;
        String arg[] = {""};
        arg[0]= String.valueOf( mCategoryId );

        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(DecoEmojiContract.CONTENT_DECOINFOLIST_COUNT_URI, null, where, arg, null);
            if (cursor == null) {
                return;
            }

            if (cursor.getCount() <= 0) {
                return;
            }
            cursor.moveToNext();
            count = cursor.getInt(0);
            cursor.close();

            String selection[] = DecoEmojiUtil.SELECTION_DECOEMOJI_INFO;
            where = " (" + DecoEmojiContract.DecoEmojiInfoColumns.CATEGORY_ID + " = ?) and ("
                        + DecoEmojiContract.DecoEmojiInfoColumns.KIND + " IN (" + emojiTypeColum + "))"
                        + "and " + DecoEmojiContract.DecoEmojiInfoColumns.KIND + " <> " + DecoEmojiContract.KIND_PICTURE;

            String order = DecoEmojiContract.DecoEmojiInfoColumns.LAST_USE_CNT + " desc, " + 
                                   DecoEmojiContract.DecoEmojiInfoColumns.URI + 
                                   " asc limit " + MAX_CATEGORY + " offset " + start ;

            int emojiDCount = 0;
            if (start < count) {
                // get EmojiD
                cursor = mContext.getContentResolver().query(DecoEmojiContract.CONTENT_DECOINFOLIST_URI, selection, where, arg, order);

                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        while (cursor.moveToNext()) {
                            EmojiAssist.DecoEmojiTextInfo info = new EmojiAssist.DecoEmojiTextInfo();
                            DecoEmojiUtil.setDecoEmojiInfo(cursor, info);
                            mDecoEmojiList.add(info);
                            emojiDCount++;
                        }
                    }
                }
            }
/*
            if (start + MAX_CATEGORY > count) {
                // get DecomePicture
                int offset = 0;
                if (emojiDCount == 0) {
                    offset = start - count;
                    if (offset < 0) {
                        offset = 0;
                    }
                 }
                where = " (" + DecoEmojiContract.DecoEmojiInfoColumns.CATEGORY_ID + " = ?) and ("
                            + DecoEmojiContract.DecoEmojiInfoColumns.KIND + " IN (" + emojiTypeColum + "))"
                            + "and " + DecoEmojiContract.DecoEmojiInfoColumns.KIND + " = " + DecoEmojiContract.KIND_PICTURE;
                order = DecoEmojiContract.DecoEmojiInfoColumns.LAST_USE_CNT + " desc, " +
                                       DecoEmojiContract.DecoEmojiInfoColumns.URI +
                                       " asc limit " + (MAX_CATEGORY - emojiDCount) + " offset " + offset;
                cursor = mContext.getContentResolver().query(DecoEmojiContract.CONTENT_DECOINFOLIST_URI, selection, where, arg, order);

                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        while (cursor.moveToNext()) {
                            DecoEmojiUtil.DecoEmojiInfo info = new DecoEmojiUtil.DecoEmojiInfo();
                            DecoEmojiUtil.DecoEmojiInfo.setDecoEmojiInfo(cursor, info);
                            mDecoEmojiList.add(info);
                        }
                    }
                    cursor.close();
                }
            }
*/

        } catch (SQLiteException e) {
            e.printStackTrace();
       } finally {
            if ( cursor != null ) {
                cursor.close();
            }
        }
    }
}
