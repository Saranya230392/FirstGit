/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml.iwnn;

import android.content.Context;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
//for debug
import android.util.Log;

import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.DecoEmojiContract;
import jp.co.omronsoft.android.text.EmojiDrawable;
import jp.co.omronsoft.android.emoji.EmojiAssist;
import jp.co.omronsoft.iwnnime.ml.AdditionalSymbolList;
import jp.co.omronsoft.iwnnime.ml.ComposingText;
import jp.co.omronsoft.iwnnime.ml.KeyboardLanguagePackData;
import jp.co.omronsoft.iwnnime.ml.OpenWnn;
import jp.co.omronsoft.iwnnime.ml.R;
import jp.co.omronsoft.iwnnime.ml.WnnEngine;
import jp.co.omronsoft.iwnnime.ml.WnnWord;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiList;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiUtil;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The symbol engine class for IME.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class IWnnSymbolEngine implements WnnEngine {
    /** The mode of symbol */
    public static final int MODE_SYMBOL = 1;

    /** The mode of Kaomoji */
    public static final int MODE_KAO_MOJI = 2;

    /** The mode of emoji */
    public static final int MODE_EMOJI = 3;

    /** The number of the togglable symbol list. */
    private static final int TOGGLE_MAX = 4;

    /** The mode of the other symbols */
    public static final int MODE_OTHERS_SYMBOL = 4;

    /** The mode of the other Kaomoji */
    public static final int MODE_OTHERS_KAO_MOJI = 5;

    /** The mode of decoEmoji */
    public static final int MODE_DECOEMOJI = 6;

    /** The mode of additional symbol */
    public static final int MODE_ADD_SYMBOL = 7;

    /** The mode of unicode6 emoji */
    public static final int MODE_EMOJI_UNI6 = 8;

    /** The mode of symbol for one touch emoji */
    public static final int MODE_ONETOUCHEMOJI_SYMBOL = 9;

    /** The mode of emoji for one touch emoji */
    public static final int MODE_ONETOUCHEMOJI_EMOJI = 10;

    /** The mode of decoemoji for one touch emoji */
    public static final int MODE_ONETOUCHEMOJI_DECOEMOJI = 11;

    /** The mode of mixed emoji and decoemoji for one touch emoji */
    public static final int MODE_ONETOUCHEMOJI_MIXED_EMOJI = 12;

    /** NO mode for SYM key mode */
    public static final int MODE_NONE = -1;

    /** Max number of page. */
    private static final int MAX_ITEM_IN_PAGE = 1000;

    /** for debug */
    private static final boolean DEBUG = false;

    /** for debug */
    private static final String TAG = "iWnn";

    /** Maximum number in history database */
    private static final int DB_MAXHISTORY = 69;

    /** The name of a database for a history of the emoji list. */
    private static final String DB_NAME_EMOJI = "db_select_emoji";

    /** The name of a database for a history of the unicode6 emoji list. */
    private static final String DB_NAME_EMOJI_UNI6 = "db_select_emoji_uni6";

    /** The name of a database for a history of the decoemoji list. */
    private static final String DB_NAME_DECOEMOJI = "db_select_decoemoji";

    /** The name of a database for a history of the mixed emoji and decoemoji list. */
    private static final String DB_NAME_MIXED_EMOJI = "db_select_mixed_emoji";

    /** The name of a database for a history of the full-symbol list. */
    private static final String DB_NAME_FULLSIZESYMBOL = "db_select_fullwidth_symbol";

    /** The name of a database for a history of the half-symbol list. */
    private static final String DB_NAME_HARFSIZESYMBOL = "db_select_harfwidth_symbol";

    /** The name of a database for a history of the other symbol list. */
    private static final String DB_NAME_OTHERS = "db_select_others";

    /** The name of a database for a history of the kaomoji list. */
    private static final String DB_NAME_KAOMOJI = "db_select_kaomoji";

    /** The name of a database for a history of the other kaomoji list. */
    private static final String DB_NAME_OTHERS_KAOMOJI = "db_select_others_kaomoji";

    /** The name of a table for the histories database. */
    private static final String TABLE_NAME = "SymbolEngine";

    /** The ID of a row (column name)*/
    private static final String ROWID = "rowid";

    /** The history data of a row (column name)*/
    private static final String HISTORY_DATA = "history_data";

    /** The columns are used for Query */
    private static final String[] QUERY_COLUMNS = new String[] { ROWID, HISTORY_DATA };

    /** The SQL to create a database */
    private static final String QUERY_CREATE_TABLE =
        "create table " + TABLE_NAME + " (" + ROWID + " integer primary key autoincrement, "
        + HISTORY_DATA + " text not null, integer);";

    /** The version of a database */
    private static final int DATABASE_VERSION = 2;

    /** Maximum number of a row ID */
    private static final int MAX_ROWID = 10000;

    /** Interface to manage a SQLite database */
    private SQLiteDatabase mDatabase = null;

    /** Cache history for a database */
    private String[] mHistories = new String[DB_MAXHISTORY];
    private String[] mTmpHistories = new String[DB_MAXHISTORY];

    /** Candidates of symbols */
    private String[] mCandidates;
    private String[] mCandidates1;
    private String[] mCandidates2;
    private String[] mCandidates3;

    /** The type of Additional symbol */
    private int mAddSymbolType = AdditionalSymbolList.ADD_SYMBOL_LIST_SYMBOLTYPE_INIT;

    /** Candidates of emoji_unicode6_nature */
    private String[] mCandidates_emoji_unicode6_nature = {
                    "\u2600"      ,"\u2601"                     ,"\u26c4"      ,"\u26a1"      ,"\ud83c\udf00","\ud83c\udf01",
                    "\ud83c\udf02","\ud83c\udf03","\ud83c\udf04","\ud83c\udf05","\ud83c\udf06","\ud83c\udf07","\ud83c\udf08",
                    "\u2744"      ,"\u26c5"      ,"\ud83c\udf09","\ud83c\udf0a","\ud83c\udf0b","\ud83c\udf0c","\ud83c\udf0f",
                    "\ud83c\udf11","\ud83c\udf14","\ud83c\udf13","\ud83c\udf19","\ud83c\udf15","\ud83c\udf1b","\ud83c\udf1f",
                    "\ud83c\udf20","\ud83d\udd50","\ud83d\udd51","\ud83d\udd52","\ud83d\udd53","\ud83d\udd54","\ud83d\udd55",
                    "\ud83d\udd56","\ud83d\udd57","\ud83d\udd58","\ud83d\udd59","\ud83d\udd5a","\ud83d\udd5b","\u231a"      ,
                    "\u231b"      ,"\u23f0"      ,"\u23f3"      ,"\u2648"      ,"\u2649"      ,"\u264a"      ,"\u264b"      ,
                    "\u264c"      ,"\u264d"      ,"\u264e"      ,"\u264f"      ,"\u2650"      ,"\u2651"      ,"\u2652"      ,
                    "\u2653"      ,"\u26ce"      ,"\ud83c\udf40","\ud83c\udf37","\ud83c\udf31","\ud83c\udf41","\ud83c\udf38",
                    "\ud83c\udf39","\ud83c\udf42","\ud83c\udf43","\ud83c\udf3a","\ud83c\udf3b","\ud83c\udf34","\ud83c\udf35",
                    "\ud83c\udf3e","\ud83c\udf3d","\ud83c\udf44","\ud83c\udf30","\ud83c\udf3c","\ud83c\udf3f","\ud83c\udf52",
                    "\ud83c\udf4c","\ud83c\udf4e","\ud83c\udf4a","\ud83c\udf53","\ud83c\udf49","\ud83c\udf45","\ud83c\udf46",
                    "\ud83c\udf48","\ud83c\udf4d","\ud83c\udf47","\ud83c\udf51","\ud83c\udf4f"};

    /** Candidates of emoji_unicode6_human */
    private String[] mCandidates_emoji_unicode6_human = {
                    "\ud83d\udc40","\ud83d\udc42","\ud83d\udc43","\ud83d\udc44","\ud83d\udc45","\ud83d\udc84","\ud83d\udc85",
                    "\ud83d\udc86","\ud83d\udc87","\ud83d\udc88","\ud83d\udc64","\ud83d\udc66","\ud83d\udc67","\ud83d\udc68",
                    "\ud83d\udc69","\ud83d\udc6a","\ud83d\udc6b","\ud83d\udc6e","\ud83d\udc6f","\ud83d\udc70",
                                                  "\ud83d\udc74","\ud83d\udc75","\ud83d\udc76","\ud83d\udc77","\ud83d\udc78",
                    "\ud83d\udc79","\ud83d\udc7a","\ud83d\udc7b","\ud83d\udc7c","\ud83d\udc7d","\ud83d\udc7e","\ud83d\udc7f",
                    "\ud83d\udc80","\ud83d\udc81","\ud83d\udc82","\ud83d\udc83","\ud83d\udc0c","\ud83d\udc0d","\ud83d\udc0e",
                    "\ud83d\udc14","\ud83d\udc17","\ud83d\udc2b","\ud83d\udc18","\ud83d\udc28","\ud83d\udc12","\ud83d\udc11",
                    "\ud83d\udc19","\ud83d\udc1a","\ud83d\udc1b","\ud83d\udc1c","\ud83d\udc1d","\ud83d\udc1e","\ud83d\udc20",
                    "\ud83d\udc21","\ud83d\udc22","\ud83d\udc24","\ud83d\udc25","\ud83d\udc26","\ud83d\udc23","\ud83d\udc27",
                    "\ud83d\udc29","\ud83d\udc1f","\ud83d\udc2c","\ud83d\udc2d","\ud83d\udc2f","\ud83d\udc31","\ud83d\udc33",
                    "\ud83d\udc34","\ud83d\udc35","\ud83d\udc36","\ud83d\udc37","\ud83d\udc3b","\ud83d\udc39","\ud83d\udc3a",
                    "\ud83d\udc2e","\ud83d\udc30","\ud83d\udc38","\ud83d\udc3e","\ud83d\udc32","\ud83d\udc3c","\ud83d\udc3d"};

    /** Candidates of emoji_unicode6_faces */
    private String[] mCandidates_emoji_unicode6_faces = {
                    "\ud83d\ude20","\ud83d\ude29","\ud83d\ude32","\ud83d\ude1e","\ud83d\ude35","\ud83d\ude30","\ud83d\ude12",
                    "\ud83d\ude0d","\ud83d\ude24","\ud83d\ude1c","\ud83d\ude1d","\ud83d\ude0b","\ud83d\ude18","\ud83d\ude1a",
                    "\ud83d\ude37","\ud83d\ude33","\ud83d\ude03","\ud83d\ude05","\ud83d\ude06","\ud83d\ude01","\ud83d\ude02",
                    "\ud83d\ude0a","\u263a"      ,"\ud83d\ude04","\ud83d\ude22","\ud83d\ude2d","\ud83d\ude28","\ud83d\ude23",
                    "\ud83d\ude21","\ud83d\ude0c","\ud83d\ude16","\ud83d\ude14","\ud83d\ude31","\ud83d\ude2a","\ud83d\ude0f",
                    "\ud83d\ude13","\ud83d\ude25","\ud83d\ude2b","\ud83d\ude09","\ud83d\ude3a","\ud83d\ude38","\ud83d\ude39",
                    "\ud83d\ude3d","\ud83d\ude3b","\ud83d\ude3f","\ud83d\ude3e","\ud83d\ude3c","\ud83d\ude40","\ud83d\ude45",
                    "\ud83d\ude46","\ud83d\ude47","\ud83d\ude48","\ud83d\ude4a","\ud83d\ude49","\ud83d\ude4b","\ud83d\ude4c",
                    "\ud83d\ude4d","\ud83d\ude4e","\ud83d\ude4f"};

    /** Candidates of emoji_unicode6_artifacts */
    private String[] mCandidates_emoji_unicode6_artifacts = {
                    "\ud83c\udfe0","\ud83c\udfe1","\ud83c\udfe2","\ud83c\udfe3","\ud83c\udfe5","\ud83c\udfe6","\ud83c\udfe7",
                    "\ud83c\udfe8","\ud83c\udfe9","\ud83c\udfea","\ud83c\udfeb","\u26ea"      ,"\u26f2"      ,"\ud83c\udfec",
                    "\ud83c\udfef","\ud83c\udff0","\ud83c\udfed","\u2693"      ,"\ud83c\udfee","\ud83d\uddfb","\ud83d\uddfc",
                    "\ud83d\uddfd","\ud83d\uddfe","\ud83d\uddff","\ud83d\udc5e","\ud83d\udc5f","\ud83d\udc60","\ud83d\udc61",
                    "\ud83d\udc62","\ud83d\udc63","\ud83d\udc53","\ud83d\udc55","\ud83d\udc56","\ud83d\udc51","\ud83d\udc54",
                    "\ud83d\udc52","\ud83d\udc57","\ud83d\udc58","\ud83d\udc59","\ud83d\udc5a","\ud83d\udc5b","\ud83d\udc5c",
                    "\ud83d\udc5d","\ud83d\udcb0","\ud83d\udcb1","\ud83d\udcb9","\ud83d\udcb2","\ud83d\udcb3","\ud83d\udcb4",
                    "\ud83d\udcb5","\ud83d\udcb8","\ud83c\udde8\ud83c\uddf3","\ud83c\udde9\ud83c\uddea","\ud83c\uddea\ud83c\uddf8",
                    "\ud83c\uddeb\ud83c\uddf7","\ud83c\uddec\ud83c\udde7","\ud83c\uddee\ud83c\uddf9","\ud83c\uddef\ud83c\uddf5",
                    "\ud83c\uddf0\ud83c\uddf7","\ud83c\uddf7\ud83c\uddfa","\ud83c\uddfa\ud83c\uddf8","\ud83d\udd25","\ud83d\udd26",
                    "\ud83d\udd27","\ud83d\udd28","\ud83d\udd29","\ud83d\udd2a","\ud83d\udd2b","\ud83d\udd2e","\ud83d\udd2f",
                    "\ud83d\udd30","\ud83d\udd31","\ud83d\udc89","\ud83d\udc8a","\ud83c\udd70","\ud83c\udd71","\ud83c\udd8e",
                    "\ud83c\udd7e","\ud83c\udf80","\ud83c\udf81","\ud83c\udf82","\ud83c\udf84","\ud83c\udf85","\ud83c\udf8c",
                    "\ud83c\udf86","\ud83c\udf88","\ud83c\udf89","\ud83c\udf8d","\ud83c\udf8e","\ud83c\udf93","\ud83c\udf92",
                    "\ud83c\udf8f","\ud83c\udf87","\ud83c\udf90","\ud83c\udf83","\ud83c\udf8a","\ud83c\udf8b","\ud83c\udf91",
                    "\ud83d\udcdf","\u260e"      ,"\ud83d\udcde","\ud83d\udcf1","\ud83d\udcf2","\ud83d\udcdd","\ud83d\udce0",
                    "\u2709"      ,"\ud83d\udce8","\ud83d\udce9","\ud83d\udcea","\ud83d\udceb","\ud83d\udcee","\ud83d\udcf0",
                    "\ud83d\udce2","\ud83d\udce3","\ud83d\udce1","\ud83d\udce4","\ud83d\udce5","\ud83d\udce6","\ud83d\udce7",
                    "\ud83d\udd20","\ud83d\udd21","\ud83d\udd22","\ud83d\udd23","\ud83d\udd24","\u2712"      ,"\ud83d\udcba",
                    "\ud83d\udcbb","\u270f"      ,"\ud83d\udcce","\ud83d\udcbc","\ud83d\udcbd","\ud83d\udcbe","\ud83d\udcbf",
                    "\ud83d\udcc0","\u2702"      ,"\ud83d\udccd","\ud83d\udcc3","\ud83d\udcc4","\ud83d\udcc5","\ud83d\udcc1",
                    "\ud83d\udcc2","\ud83d\udcd3","\ud83d\udcd6","\ud83d\udcd4","\ud83d\udcd5","\ud83d\udcd7","\ud83d\udcd8",
                    "\ud83d\udcd9","\ud83d\udcda","\ud83d\udcdb","\ud83d\udcdc","\ud83d\udccb","\ud83d\udcc6","\ud83d\udcca",
                    "\ud83d\udcc8","\ud83d\udcc9","\ud83d\udcc7","\ud83d\udccc","\ud83d\udcd2","\ud83d\udccf","\ud83d\udcd0",
                    "\ud83d\udcd1"};

    /** Candidates of emoji_unicode6_activities */
    private String[] mCandidates_emoji_unicode6_activities = {
                    "\ud83c\udfbd","\u26be"      ,"\u26f3"      ,"\ud83c\udfbe","\u26bd"      ,"\ud83c\udfbf","\ud83c\udfc0",
                    "\ud83c\udfc1","\ud83c\udfc2","\ud83c\udfc3","\ud83c\udfc4","\ud83c\udfc6","\ud83c\udfc8","\ud83c\udfca",
                    "\ud83d\ude83","\ud83d\ude87","\u24c2"      ,"\ud83d\ude84","\ud83d\ude85","\ud83d\ude97","\ud83d\ude99",
                    "\ud83d\ude8c","\ud83d\ude8f","\ud83d\udea2","\u2708"      ,"\u26f5"      ,"\ud83d\ude89","\ud83d\ude80",
                    "\ud83d\udea4","\ud83d\ude95","\ud83d\ude9a","\ud83d\ude92","\ud83d\ude91","\ud83d\ude93","\u26fd"      ,
                    "\ud83c\udd7f","\ud83d\udea5","\ud83d\udea7","\ud83d\udea8","\u2668"      ,"\u26fa"      ,"\ud83c\udfa0",
                    "\ud83c\udfa1","\ud83c\udfa2","\ud83c\udfa3","\ud83c\udfa4","\ud83c\udfa5","\ud83c\udfa6","\ud83c\udfa7",
                    "\ud83c\udfa8","\ud83c\udfa9","\ud83c\udfaa","\ud83c\udfab","\ud83c\udfac","\ud83c\udfad","\ud83c\udfae",
                    "\ud83c\udc04","\ud83c\udfaf","\ud83c\udfb0","\ud83c\udfb1","\ud83c\udfb2","\ud83c\udfb3","\ud83c\udfb4",
                    "\ud83c\udccf","\ud83c\udfb5","\ud83c\udfb6","\ud83c\udfb7","\ud83c\udfb8","\ud83c\udfb9","\ud83c\udfba",
                    "\ud83c\udfbb","\ud83c\udfbc","\u303d"      ,"\ud83d\udcf7","\ud83d\udcf9","\ud83d\udcfa","\ud83d\udcfb",
                    "\ud83d\udcfc","\ud83d\udc8b","\ud83d\udc8c","\ud83d\udc8d","\ud83d\udc8e","\ud83d\udc8f","\ud83d\udc90",
                    "\ud83d\udc91","\ud83d\udc92","\ud83d\udd1e","\u00a9"      ,"\u00ae"      ,"\u2122"      ,"\u2139"      ,
                    "\u0023\u20e3","\u0031\u20e3","\u0032\u20e3","\u0033\u20e3","\u0034\u20e3","\u0035\u20e3","\u0036\u20e3",
                    "\u0037\u20e3","\u0038\u20e3","\u0039\u20e3","\u0030\u20e3","\ud83d\udd1f","\ud83d\udcf6","\ud83d\udcf3",
                    "\ud83d\udcf4"};

    /** Candidates of emoji_unicode6_foods */
    private String[] mCandidates_emoji_unicode6_foods = {
                    "\ud83c\udf54","\ud83c\udf59","\ud83c\udf70","\ud83c\udf5c","\ud83c\udf5e","\ud83c\udf73","\ud83c\udf66",
                    "\ud83c\udf5f","\ud83c\udf61","\ud83c\udf58","\ud83c\udf5a","\ud83c\udf5d","\ud83c\udf5b","\ud83c\udf62",
                    "\ud83c\udf63","\ud83c\udf71","\ud83c\udf72","\ud83c\udf67","\ud83c\udf56","\ud83c\udf65","\ud83c\udf60",
                    "\ud83c\udf55","\ud83c\udf57","\ud83c\udf68","\ud83c\udf69","\ud83c\udf6a","\ud83c\udf6b","\ud83c\udf6c",
                    "\ud83c\udf6d","\ud83c\udf6e","\ud83c\udf6f","\ud83c\udf64","\ud83c\udf74"               ,"\ud83c\udf78",
                    "\ud83c\udf7a","\ud83c\udf75","\ud83c\udf76","\ud83c\udf77","\ud83c\udf7b","\ud83c\udf79"};

    /** Candidates of emoji_unicode6_symbols */
    private String[] mCandidates_emoji_unicode6_symbols = {
                    "\u2197"      ,"\u2198"      ,"\u2196"      ,"\u2199"      ,"\u2934"      ,"\u2935"      ,"\u2194"      ,
                    "\u2195"      ,"\u2b06"      ,"\u2b07"      ,"\u27a1"      ,"\u2b05"      ,"\u25b6"      ,"\u25c0"      ,
                    "\u23e9"      ,"\u23ea"      ,"\u23eb"      ,"\u23ec"      ,"\ud83d\udd3a","\ud83d\udd3b","\ud83d\udd3c",
                    "\ud83d\udd3d","\u2b55"      ,"\u274c"      ,"\u274e"      ,"\u2757"      ,"\u2049"      ,"\u203c"      ,
                    "\u2753"      ,"\u2754"      ,"\u2755"      ,"\u3030"      ,"\u27b0"      ,"\u27bf"      ,"\u2764"      ,
                    "\ud83d\udc93","\ud83d\udc94","\ud83d\udc95","\ud83d\udc96","\ud83d\udc97","\ud83d\udc98","\ud83d\udc99",
                    "\ud83d\udc9a","\ud83d\udc9b","\ud83d\udc9c","\ud83d\udc9d","\ud83d\udc9e","\ud83d\udc9f","\u2665"      ,
                    "\u2660"      ,"\u2666"      ,"\u2663"      ,"\ud83d\udeac","\ud83d\udead","\u267f"      ,"\ud83d\udea9",
                    "\u26a0"      ,"\u26d4"      ,"\u267b"      ,"\ud83d\udeb2","\ud83d\udeb6","\ud83d\udeb9","\ud83d\udeba",
                    "\ud83d\udec0","\ud83d\udebb","\ud83d\udebd","\ud83d\udebe","\ud83d\udebc","\ud83d\udeaa","\ud83d\udeab",
                    "\u2714"      ,"\ud83c\udd91","\ud83c\udd92","\ud83c\udd93","\ud83c\udd94","\ud83c\udd95","\ud83c\udd96",
                    "\ud83c\udd97","\ud83c\udd98","\ud83c\udd99","\ud83c\udd9a","\ud83c\ude01","\ud83c\ude02","\ud83c\ude32",
                    "\ud83c\ude33","\ud83c\ude34","\ud83c\ude35","\ud83c\ude36","\ud83c\ude1a","\ud83c\ude37","\ud83c\ude38",
                    "\ud83c\ude39","\ud83c\ude2f","\ud83c\ude3a","\u3299"      ,"\u3297"      ,"\ud83c\ude50","\ud83c\ude51",
                    "\u2795"      ,"\u2796"      ,"\u2716"      ,"\u2797"      ,"\ud83d\udca0","\ud83d\udca1","\ud83d\udca2",
                    "\ud83d\udca3","\ud83d\udca4","\ud83d\udca5","\ud83d\udca6","\ud83d\udca7","\ud83d\udca8","\ud83d\udca9",
                    "\ud83d\udcaa","\ud83d\udcab","\ud83d\udcac","\u2728"      ,"\u2734"      ,"\u2733"      ,"\u26aa"      ,
                    "\u26ab"      ,"\ud83d\udd34","\ud83d\udd35","\ud83d\udd32","\ud83d\udd33","\u2b50"      ,"\u2b1c"      ,
                    "\u2b1b"      ,"\u25ab"      ,"\u25aa"      ,"\u25fd"      ,"\u25fe"      ,"\u25fb"      ,"\u25fc"      ,
                    "\ud83d\udd36","\ud83d\udd37","\ud83d\udd38","\ud83d\udd39","\u2747"      ,"\ud83d\udcae","\ud83d\udcaf",
                    "\u21a9"      ,"\u21aa"      ,"\ud83d\udd03","\ud83d\udd0a","\ud83d\udd0b","\ud83d\udd0c","\ud83d\udd0d",
                    "\ud83d\udd0e","\ud83d\udd12","\ud83d\udd13","\ud83d\udd0f","\ud83d\udd10","\ud83d\udd11","\ud83d\udd14",
                                   "\ud83d\udd18","\ud83d\udd16","\ud83d\udd17","\ud83d\udd19","\ud83d\udd1a","\ud83d\udd1b",
                    "\ud83d\udd1c","\ud83d\udd1d","\u2003"      ,"\u2002"      ,"\u2005"      ,"\u2705"      ,"\u270a"      ,
                    "\u270b"      ,"\u270c"      ,"\ud83d\udc4a","\ud83d\udc4d","\u261d"      ,"\ud83d\udc46","\ud83d\udc47",
                    "\ud83d\udc48","\ud83d\udc49","\ud83d\udc4b","\ud83d\udc4f","\ud83d\udc4c","\ud83d\udc4e","\ud83d\udc50"};

    /** Candidates of emoji_docomo_nature */
    private static final String[] EMOJI_DOCOMO_NATURE_TABLE = {
                    "\udbb8\udc00","\udbb8\udc01","\udbb8\udc02","\udbb8\udc03","\udbb8\udc04","\udbb8\udc05","\udbb8\udc06",
                    "\udbb8\udc07","\udbb8\udc08","\udbb8\udc11","\udbb8\udc12","\udbb8\udc13","\udbb8\udc14","\udbb8\udc15",
                    "\udbb8\udc18","\udbb8\udc19","\udbb8\udc1a","\udbb8\udc1b","\udbb8\udc1d","\udbb8\udc2a","\udbb8\udc2b",
                    "\udbb8\udc2c","\udbb8\udc2d","\udbb8\udc2e","\udbb8\udc2f","\udbb8\udc30","\udbb8\udc31","\udbb8\udc32",
                    "\udbb8\udc33","\udbb8\udc34","\udbb8\udc35","\udbb8\udc36","\udbb8\udc38","\udbb8\udc3c","\udbb8\udc3d",
                    "\udbb8\udc3e","\udbb8\udc3f","\udbb8\udc40","\udbb8\udc4f","\udbb8\udc50","\udbb8\udc51"};

    /** Candidates of emoji_docomo_human */
    private static final String[] EMOJI_DOCOMO_HUMAN_TABLE = {
                    "\udbb8\udd90","\udbb8\udd91","\udbb8\udd95","\udbb8\udd9a","\udbb8\uddb7","\udbb8\uddb8","\udbb8\uddb9",
                    "\udbb8\uddba","\udbb8\uddbc","\udbb8\uddbd","\udbb8\uddbe","\udbb8\uddbf"};

    /** Candidates of emoji_docomo_faces */
    private static final String[] EMOJI_DOCOMO_FACES_TABLE = {
                    "\udbb8\udf20","\udbb8\udf23","\udbb8\udf24","\udbb8\udf26","\udbb8\udf27","\udbb8\udf29","\udbb8\udf2b",
                    "\udbb8\udf30","\udbb8\udf31","\udbb8\udf32","\udbb8\udf33","\udbb8\udf39","\udbb8\udf3a","\udbb8\udf3c",
                    "\udbb8\udf3d","\udbb8\udf3e","\udbb8\udf3f","\udbb8\udf40","\udbb8\udf41","\udbb8\udf43","\udbb8\udf44",
                    "\udbb8\udf47"};

    /** Candidates of emoji_docomo_artifacts */
    private static final String[] EMOJI_DOCOMO_ARTIFACTS_TABLE = {
                    "\udbb9\udcb0","\udbb9\udcb2","\udbb9\udcb3","\udbb9\udcb4","\udbb9\udcb5","\udbb9\udcb6","\udbb9\udcb7",
                    "\udbb9\udcb9","\udbb9\udcba","\udbb9\udcc3","\udbb9\udcc9","\udbb9\udccd","\udbb9\udcd6","\udbb9\udcce",
                    "\udbb9\udccf","\udbb9\udcd0","\udbb9\udcd1","\udbb9\udcdc","\udbb9\udcdd","\udbb9\udce2","\udbb9\udcef",
                    "\udbb9\udcf0","\udbb9\udcf1","\udbb9\udcf2","\udbb9\udcf3","\udbb9\udd06","\udbb9\udd0f","\udbb9\udd10",
                    "\udbb9\udd11","\udbb9\udd12","\udbb9\udd22","\udbb9\udd23","\udbb9\udd25","\udbb9\udd26","\udbb9\udd27",
                    "\udbb9\udd28","\udbb9\udd29","\udbb9\udd2b","\udbb9\udd36","\udbb9\udd37","\udbb9\udd38","\udbb9\udd39",
                    "\udbb9\udd3a","\udbb9\udd3e","\udbb9\udd46","\udbb9\udd53"};

    /** Candidates of emoji_docomo_activities */
    private static final String[] EMOJI_DOCOMO_ACTIVITIES_TABLE = {
                    "\udbb9\udfd0","\udbb9\udfd1","\udbb9\udfd2","\udbb9\udfd3","\udbb9\udfd4","\udbb9\udfd5","\udbb9\udfd6",
                    "\udbb9\udfd7","\udbb9\udfd8","\udbb9\udfd9","\udbb9\udfdf","\udbb9\udfe1","\udbb9\udfe2","\udbb9\udfe4",
                    "\udbb9\udfe5","\udbb9\udfe6","\udbb9\udfe8","\udbb9\udfe9","\udbb9\udfea","\udbb9\udfeb","\udbb9\udff5",
                    "\udbb9\udff6","\udbb9\udff7","\udbb9\udffa","\udbb9\udffc","\udbba\udc00","\udbba\udc01","\udbba\udc03",
                    "\udbba\udc04","\udbba\udc05","\udbba\udc06","\udbba\udc07","\udbba\udc08","\udbba\udc0a","\udbba\udc13",
                    "\udbba\udc14","\udbba\udc1c","\udbba\udc1d","\udbba\udc23","\udbba\udc24","\udbba\udc25","\udbba\udc2c",
                    "\udbba\udc2e","\udbba\udc2f","\udbba\udc30","\udbba\udc31","\udbba\udc32","\udbba\udc33","\udbba\udc34",
                    "\udbba\udc35","\udbba\udc36","\udbba\udc37"};

    /** Candidates of emoji_docomo_foods */
    private static final String[] EMOJI_DOCOMO_FOODS_TABLE = {
                    "\udbba\udd60","\udbba\udd61","\udbba\udd62","\udbba\udd63","\udbba\udd64","\udbba\udd80","\udbba\udd81",
                    "\udbba\udd82","\udbba\udd83","\udbba\udd84","\udbba\udd85","\udbba\udd86"};

    /** Candidates of emoji_docomo_symbols */
    private static final String[] EMOJI_DOCOMO_SYMBOLS_TABLE = {
                    "\udbba\udef0","\udbba\udef1","\udbba\udef2","\udbba\udef3","\udbba\udef4","\udbba\udef5","\udbba\udef6",
                    "\udbba\udef7","\udbba\udf04","\udbba\udf05","\udbba\udf06","\udbba\udf07","\udbba\udf08","\udbba\udf0c",
                    "\udbba\udf0d","\udbba\udf0e","\udbba\udf0f","\udbba\udf1a","\udbba\udf1b","\udbba\udf1c","\udbba\udf1d",
                    "\udbba\udf1e","\udbba\udf1f","\udbba\udf20","\udbba\udf22","\udbba\udf23","\udbba\udf2c","\udbba\udf21",
                    "\udbba\udf27","\udbba\udf28","\udbba\udf29","\udbba\udf2d","\udbba\udf2a","\udbba\udf36","\udbba\udf2e",
                    "\udbba\udf2f","\udbba\udf30","\udbba\udf31","\udbba\udf2b","\udbba\udf55","\udbba\udf56","\udbba\udf57",
                    "\udbba\udf58","\udbba\udf59","\udbba\udf5a","\udbba\udf5b","\udbba\udf5c","\udbba\udf5d","\udbba\udf60",
                    "\udbba\udf81","\udbba\udf84","\udbba\udf83","\udbba\udf85","\udbba\udf82","\udbba\udf93","\udbba\udf95",
                    "\udbba\udf94","\udbba\udf96","\udbba\udf97","\udbbb\ude10","\udbbb\ude11","\udbbb\ude12","\udbbb\ude13",
                    "\udbbb\ude14","\udbbb\ude15","\udbba\udc2b","\udbba\udc2d"};

    /** Candidates of emoji_unicode6_to_kddi_faces */
    private static final String[] EMOJI_KDDI_FACES_TABLE = {
                    "\ud83d\ude03","\ud83d\ude09","\ud83d\ude0a","\ud83d\ude01","\ud83d\ude02","\ud83d\ude18","\ud83d\ude1a",
                    "\ud83d\ude0c","\ud83d\ude0d","\ud83d\ude31","\ud83d\ude14","\ud83d\ude16","\ud83d\ude22","\ud83d\ude30",
                    "\ud83d\ude2d","\ud83d\ude13","\ud83d\ude23","\ud83d\ude28","\ud83d\ude20","\ud83d\ude21","\ud83d\ude12",
                    "\ud83d\ude0f","\ud83d\ude1c","\ud83d\ude2a","\ud83d\ude35","\ud83d\ude37","\ud83d\ude33","\ud83d\ude46",
                    "\ud83d\ude45","\ud83d\ude47","\ud83d\ude4c","\ud83d\ude4f","\ud83d\udc68","\ud83d\udc69","\ud83d\udc76",
                    "\ud83d\udc74","\ud83d\udc75","\ud83d\udc78","\ud83d\udc6f","\ud83d\udc6e","\ud83d\udc77","\ud83d\udc7c",
                    "\ud83d\udc7f","\ud83d\udc7b","\ud83d\udc7e"};

    /** Candidates of emoji_unicode6_to_kddi_human */
    private static final String[] EMOJI_KDDI_HUMAN_TABLE = {
                    "\ud83d\udc4d","\ud83d\udc4e","\ud83d\udc4c","\ud83d\udc4a","\u261d"      ,"\ud83d\udc4b","\ud83d\udcaa",
                    "\ud83d\udc4f","\u270a"      ,"\u270c"      ,"\u270b"      ,"\ud83d\udc46","\ud83d\udc47","\ud83d\udc49",
                    "\ud83d\udc48","\u2764"      ,"\ud83d\udc94","\ud83d\udc95","\ud83d\udc93","\ud83d\udc96","\ud83d\udc98",
                    "\ud83d\udc9d","\ud83d\udc99","\ud83d\udc9a","\ud83d\udc9b","\ud83d\udc9c","\ud83d\udc8f","\ud83d\udc91",
                    "\u2728"      ,"\u2b50"      ,"\ud83c\udf89","\ud83c\udfaf","\ud83d\udc90","\ud83c\udf7b","\u3297"      ,
                    "\ud83d\udca1","\ud83d\udca7","\ud83d\udca6","\ud83d\udca4","\ud83d\udca8","\u27b0"      ,"\ud83d\udca2",
                    "\ud83d\udca9","\ud83d\udc80","\ud83d\udd25","\ud83d\udca3","\ud83d\udca5","\u26a0"      ,"\ud83c\udd9a",
                    "\ud83d\udc40","\ud83d\udc43","\ud83d\udc8b","\ud83d\udc44","\ud83d\udc42","\ud83d\udc63","\u2757"      ,
                    "\u2753"      ,"\u203c"      ,"\u2049"      ,"\ud83c\udfb5","\ud83c\udfb6"};

    /** Candidates of emoji_unicode6_to_kddi_critter */
    private static final String[] EMOJI_KDDI_CRITTER_TABLE = {
                    "\ud83d\udc36","\ud83d\udc31","\ud83d\udc3b","\ud83d\udc2f","\ud83d\udc35","\ud83d\udc30","\ud83d\udc38",
                    "\ud83d\udc2d","\ud83d\udc2e","\ud83d\udc37","\ud83d\udc14","\ud83d\udc24","\ud83d\udc28","\ud83d\udc18",
                    "\ud83d\udc34","\ud83d\udc2b","\ud83d\udc17","\ud83d\udc0d","\ud83d\udc0c","\ud83d\udc1b","\ud83d\udc33",
                    "\ud83d\udc2c","\ud83d\udc27","\ud83d\udc19","\ud83d\udc21","\ud83d\udc20","\ud83d\udc1a","\u2648"      ,
                    "\u2649"      ,"\u264a"      ,"\u264b"      ,"\u264c"      ,"\u264d"      ,"\u264e"      ,"\u264f"      ,
                    "\u2650"      ,"\u2651"      ,"\u2652"      ,"\u2653"      ,"\u26ce"      };

    /** Candidates of emoji_unicode6_to_kddi_foods */
    private static final String[] EMOJI_KDDI_FOODS_TABLE = {
                    "\ud83c\udf5c","\ud83c\udf5b","\ud83c\udf5d","\ud83c\udf54","\ud83c\udf5f","\ud83c\udf5e","\ud83c\udf73",
                    "\ud83c\udf74","\ud83c\udf72","\ud83c\udf63","\ud83c\udf62","\ud83c\udf71","\ud83c\udf5a","\ud83c\udf59",
                    "\ud83c\udf7a","\ud83c\udf77","\ud83c\udf78","\ud83c\udf76","\ud83c\udf75","\u2615"      ,"\ud83c\udf82",
                    "\ud83c\udf70","\ud83c\udf66","\ud83c\udf67","\ud83c\udf58","\ud83c\udf61","\ud83c\udf53","\ud83c\udf52",
                    "\ud83c\udf49","\ud83c\udf4e","\ud83c\udf4a","\ud83c\udf4c","\ud83c\udf45","\ud83c\udf46"};

    /** Candidates of emoji_unicode6_to_kddi_nature */
    private static final String[] EMOJI_KDDI_NATURE_TABLE = {
                    "\ud83c\udf38","\ud83c\udf37","\ud83c\udf39","\ud83c\udf3a","\ud83c\udf3b","\ud83c\udf31","\ud83c\udf40",
                    "\ud83c\udf35","\ud83c\udf42","\ud83c\udf41","\u2600"      ,"\u2601"      ,"\u2614"      ,"\u26a1"      ,
                    "\ud83c\udf00","\u26c4"      ,"\ud83c\udf19","\ud83c\udf8d","\ud83c\udf8e","\ud83c\udf93","\ud83c\udf92",
                    "\ud83c\udf8f","\ud83c\udf90","\u26fa"      ,"\ud83c\udf34","\ud83c\udf86","\ud83c\udf87","\ud83c\udf91",
                    "\ud83c\udf83","\ud83c\udf84","\ud83c\udf85","\ud83d\uddfb","\ud83c\udf05","\ud83c\udf08","\ud83c\udf06",
                    "\ud83c\udf03","\ud83c\udf0a"};

    /** Candidates of emoji_unicode6_to_kddi_activities */
    private static final String[] EMOJI_KDDI_ACTIVITIES_TABLE = {
                    "\ud83d\udc57","\ud83d\udc58","\ud83d\udc59","\ud83d\udc5c","\ud83d\udc62","\ud83d\udc60","\ud83d\udc52",
                    "\ud83c\udf80","\ud83d\udc8d","\ud83d\udc84","\ud83d\udc85","\ud83d\udc86","\ud83d\udc87","\ud83d\udec0",
                    "\ud83d\udc55","\ud83d\udc54","\ud83d\udc56","\ud83d\udcbc","\ud83d\udc5f","\ud83c\udf02","\ud83d\udc88",
                    "\u26be"      ,"\u26bd"      ,"\ud83c\udfc8","\ud83c\udfbe","\ud83c\udfc0","\ud83c\udfc3","\ud83d\udeb6",
                    "\ud83c\udfca","\ud83c\udfc4","\ud83c\udfbf","\ud83c\udfc2","\u26f3"      ,"\ud83c\udfc1","\ud83c\udfb1",
                    "\ud83c\udfa1","\ud83c\udfa2","\u2668"      ,"\ud83d\udd2e","\ud83c\udfac","\ud83d\udc83","\ud83c\udfba",
                    "\ud83c\udfb8","\ud83c\udfae","\ud83c\udc04","\ud83c\udfb0","\ud83d\udcb9"};

    /** Candidates of emoji_unicode6_to_kddi_artifacts */
    private static final String[] EMOJI_KDDI_ARTIFACTS_TABLE = {
                    "\ud83d\udeb2","\ud83d\ude97","\ud83d\ude8c","\ud83d\ude9a","\ud83d\ude93","\ud83d\ude92","\ud83d\ude91",
                    "\ud83d\ude83","\ud83d\ude85","\ud83d\ude87","\u26f5"      ,"\ud83d\udea2","\u2708"      ,"\ud83d\ude80",
                    "\ud83c\udfe0","\ud83c\udfe2","\ud83c\udfeb","\ud83c\udfe3","\ud83c\udfe5","\ud83c\udfe8","\ud83c\udfe9",
                    "\ud83c\udfec","\ud83d\ude89","\u26ea"      ,"\ud83c\udfef","\ud83c\udff0","\ud83d\ude8f","\ud83d\udea7",
                    "\ud83d\udea5","\u26f2"      ,"\ud83d\uddfc","\u26fd"      ,"\ud83c\udfea","\ud83c\udfe7","\ud83c\udfe6",
                    "\ud83c\udd7f","\ud83d\udd30","\ud83d\udebb","\u267f"      };

    /** Candidates of emoji_unicode6_to_kddi_tools */
    private static final String[] EMOJI_KDDI_TOOLS_TABLE = {
                    "\ud83d\udcfa","\ud83d\udce1","\ud83c\udfa5","\ud83d\udcf7","\ud83c\udfa4","\ud83d\udcbf","\ud83d\udcbd",
                    "\ud83d\udcfc","\ud83c\udfa7","\ud83d\udd0a","\ud83d\udcbb","\u270f"      ,"\u2702"      ,"\ud83d\udcce",
                    "\ud83d\udcd6","\ud83d\udcdd","\ud83d\udd0d","\ud83d\udc53","\u23f0"      ,"\u23f3"      ,"\ud83d\udd14",
                    "\ud83d\udc51","\ud83c\udfc6","\ud83d\udcb0","\ud83d\udc5b","\ud83d\udd11","\ud83d\udd12","\ud83d\udd2b",
                    "\ud83c\udfab","\ud83d\udcea","\ud83c\udf81","\ud83d\udd28","\ud83c\udfa8","\ud83c\udf88","\ud83d\udc89",
                    "\ud83d\udc8a"};

    /** Candidates of emoji_unicode6_to_kddi_symbols */
    private static final String[] EMOJI_KDDI_SYMBOLS_TABLE = {
                    "\u2934"      ,"\u2935"      ,"\u2b06"      ,"\u2b07"      ,"\u2b05"      ,"\u27a1"      ,"\u2197"      ,
                    "\u2196"      ,"\u2198"      ,"\u2199"      ,"\u2194"      ,"\u2195"      ,"\u267b"      ,"\u2b55"      ,
                    "\u274c"      ,"\ud83d\udeac","\ud83d\udead","\ud83d\udd1e","\u3299"      ,"\ud83c\ude50","\ud83c\ude39",
                    "\ud83c\ude35","\ud83c\ude33","\ud83c\ude2f","\ud83c\ude3a","\ud83c\udd95","\ud83c\udd99","\ud83c\udd92",
                    "\ud83c\udf8c","\ud83c\uddef\ud83c\uddf5","\ud83c\uddfa\ud83c\uddf8","\ud83c\uddeb\ud83c\uddf7",
                    "\ud83c\udde9\ud83c\uddea","\ud83c\uddee\ud83c\uddf9","\ud83c\uddec\ud83c\udde7","\ud83c\udde8\ud83c\uddf3",
                    "\ud83c\uddf0\ud83c\uddf7","\u2665"      ,"\u2660"      ,"\u2666"      ,"\u2663"      ,"\u260e"      ,
                    "\ud83d\udcf1","\u2709"      ,"\ud83d\udc8c","\ud83d\udcf6","\ud83d\udcf3","\ud83d\udcf4","\ud83d\udcf2",
                    "\ud83d\udce9","\ud83d\udce0"};

    /** emoji_unicode6_nature */
    /** Candidates of emoji_unicode6_to_sbm_facial_expression */
    private static final String[] EMOJI_SBM_FACIAL_EXPRESSION_TABLE = {
        "\ud83d\ude04","\ud83d\ude0a","\ud83d\ude03","\u263a"      ,"\ud83d\ude09","\ud83d\ude0d","\ud83d\ude18",
        "\ud83d\ude1a","\ud83d\ude33","\ud83d\ude0c","\ud83d\ude01","\ud83d\ude1c","\ud83d\ude1d","\ud83d\ude12",
        "\ud83d\ude0f","\ud83d\ude13","\ud83d\ude14","\ud83d\ude1e","\ud83d\ude16","\ud83d\ude25","\ud83d\ude30",
        "\ud83d\ude28","\ud83d\ude23","\ud83d\ude22","\ud83d\ude2d","\ud83d\ude02","\ud83d\ude32","\ud83d\ude31",
        "\ud83d\ude20","\ud83d\ude21","\ud83d\ude37","\ud83d\ude2a"};

    /** Candidates of emoji_unicode6_to_sbm_emotion */
    private static final String[] EMOJI_SBM_EMOTION_TABLE = {
        "\u2764"      ,"\ud83d\udc94","\ud83d\udc93","\ud83d\udc97","\ud83d\udc98","\ud83d\udc99","\ud83d\udc9a",
        "\ud83d\udc9b","\ud83d\udc9c","\u2757"      ,"\u2755"      ,"\u2753"      ,"\u2754"      ,"\ud83c\udfb5",
        "\ud83c\udfb6","\u2728"      ,"\u2b50",      "\ud83c\udf1f","\u270a"      ,"\u270c"      ,"\u270b"      ,
        "\ud83d\udc4d","\ud83d\udc4a","\u261d"      ,"\ud83d\udc4c","\ud83d\udc4e","\ud83d\ude4f","\ud83d\udc4b",
        "\ud83d\udc4f","\ud83d\udcaa","\ud83d\udc8b","\ud83d\udc44","\ud83d\udc40","\ud83d\udc42","\ud83d\udc43",
        "\ud83d\ude4c","\ud83d\udc50","\ud83d\ude46","\ud83d\ude45","\ud83d\ude47","\ud83d\udc63","\ud83d\udeb6",
        "\ud83c\udfc3","\ud83d\udca8","\ud83d\udca6","\ud83d\udca4","\ud83d\udca2","\ud83d\udc8f","\ud83d\udc91",
        "\ud83d\udc6b"};

    /** Candidates of emoji_unicode6_to_sbm_season_event */
    private static final String[] EMOJI_SBM_SEASON_EVENT_TABLE = {
        "\u2600"      ,"\u2614"      ,"\u2601"      ,"\u26c4"      ,"\ud83c\udf19","\u26a1"      ,"\ud83c\udf00",
        "\ud83c\udf0a","\ud83c\udf08","\ud83d\uddfb","\ud83c\udf38","\ud83c\udf37","\ud83c\udf41","\ud83c\udf40",
        "\ud83c\udf39","\ud83c\udf3a","\ud83c\udf3b","\ud83d\udc90","\ud83c\udf34","\ud83c\udf35","\ud83c\udf43",
        "\ud83c\udf3e","\ud83c\udf42","\ud83c\udf81","\ud83c\udf82","\ud83c\udf89","\ud83c\udfc6","\ud83d\udc51",
        "\ud83c\udfaf","\ud83d\udd14","\ud83d\udd0d","\ud83d\udca1","\ud83c\udf88","\ud83d\udd2b","\ud83d\udd28",
        "\ud83d\udd25","\ud83d\udca3","\ud83d\udce2","\ud83d\udce3","\ud83c\udfc1","\ud83c\udc04","\ud83d\udc83",
        "\ud83d\udc6f","\ud83c\udf04","\ud83c\udf05","\ud83c\udf07","\ud83c\udf06","\ud83c\udf03","\ud83c\udf8d",
        "\ud83d\udc9d","\ud83c\udf8e","\ud83c\udf93","\ud83c\udf92","\ud83c\udf8f","\ud83c\udf86","\ud83c\udf87",
        "\ud83c\udf90","\ud83c\udf91","\ud83c\udf83","\ud83c\udf85","\ud83c\udf84"};

    /** Candidates of emoji_unicode6_to_sbm_character */
    private static final String[] EMOJI_SBM_CHARACTER_TABLE = {
        "\ud83d\udc31","\ud83d\udc36","\ud83d\udc37","\ud83d\udc2d","\ud83d\udc2f","\ud83d\udc35","\ud83d\udc3b",
        "\ud83d\udc30","\ud83d\udc2e","\ud83d\udc28","\ud83d\udc39","\ud83d\udc38","\ud83d\udc34","\ud83d\udc3a",
        "\ud83d\udc17","\ud83d\udc2b","\ud83d\udc12","\ud83d\udc0e","\ud83d\udc18","\ud83d\udc11","\ud83d\udc14",
        "\ud83d\udc24","\ud83d\udc26","\ud83d\udc27","\ud83d\udc33","\ud83d\udc2c","\ud83d\udc1f","\ud83d\udc20",
        "\ud83d\udc19","\ud83d\udc0d","\ud83d\udc1b","\ud83d\udc1a","\ud83d\udc66","\ud83d\udc67","\ud83d\udc68",
        "\ud83d\udc69","\ud83d\udc74","\ud83d\udc75","\ud83d\udc76","\ud83d\udc81","\ud83d\udc6e","\ud83d\udc77",
        "\ud83d\udc71","\ud83d\udc72","\ud83d\udc73","\ud83d\udc82","\ud83d\uddfd","\ud83d\udc78","\ud83d\udc7c",
        "\ud83d\udc7f","\ud83d\udc7b","\ud83d\udc80","\ud83d\udc7d","\ud83d\udc7e","\ud83d\udca9"};

    /** Candidates of emoji_unicode6_to_sbm_food */
    private static final String[] EMOJI_SBM_FOOD_TABLE = {
        "\ud83c\udf74","\ud83c\udf7a","\ud83c\udf7b","\ud83c\udf78","\ud83c\udf76","\ud83c\udf75","\u2615"      ,
        "\ud83c\udf70","\ud83c\udf66","\ud83c\udf61","\ud83c\udf58","\ud83c\udf67","\ud83c\udf59","\ud83c\udf5a",
        "\ud83c\udf5e","\ud83c\udf54","\ud83c\udf5b","\ud83c\udf5d","\ud83c\udf5c","\ud83c\udf63","\ud83c\udf71",
        "\ud83c\udf72","\ud83c\udf62","\ud83c\udf5f","\ud83c\udf73","\ud83c\udf4e","\ud83c\udf53","\ud83c\udf4a",
        "\ud83c\udf49","\ud83c\udf45","\ud83c\udf46"};

    /** Candidates of emoji_unicode6_to_sbm_daily_life */
//TODO FE4C5のコード(U+E50A)渋谷 はunicode6では非採用
    private static final String[] EMOJI_SBM_DAILY_LIFE_TABLE = {
        "\ud83c\udf8c","\ud83d\udeb2","\ud83d\ude97","\ud83d\ude99","\ud83d\ude8c","\ud83d\ude9a","\ud83d\ude93",
        "\ud83d\ude91","\ud83d\ude92","\ud83d\ude95","\ud83d\ude83","\ud83d\ude87","\ud83d\ude89","\ud83d\ude85",
        "\ud83d\ude84","\ud83d\udea2","\u26f5"      ,"\u2708"      ,"\ud83d\ude80","\ud83d\udead","\ud83c\udfe0",
        "\ud83c\udfe2","\ud83d\udceb","\ud83d\udcee","\ud83c\udfe3","\ud83c\udfe6","\ud83c\udfe7","\ud83c\udfe5",
        "\ud83c\udfea","\ud83c\udfeb","\ud83c\udfe8","\ud83c\udfe9","\ud83c\udfec","\ud83d\udc92","\u26ea"      ,
        "\ud83c\udfef","\ud83c\udff0","\ud83d\uddfc","\udbb9\udcc5","\ud83c\udfed","\ud83c\udfa1","\ud83c\udfa2",
        "\u26f2"      ,"\u26fa"      ,"\u2668"      ,"\u26fd"      ,"\ud83d\ude8f","\ud83d\udea5","\u26a0"      ,
        "\ud83d\udea7","\ud83d\udd30","\ud83c\udd7f","\ud83d\udebb","\ud83d\udebe","\u267f"      ,"\ud83c\udfa6",
        "\ud83d\udeb9","\ud83d\udeba","\ud83d\udebc","\ud83c\uddef\ud83c\uddf5"   ,"\ud83c\uddfa\ud83c\uddf8"   ,
        "\ud83c\uddeb\ud83c\uddf7"   ,"\ud83c\udde9\ud83c\uddea"   ,"\ud83c\uddee\ud83c\uddf9"   ,"\ud83c\uddec\ud83c\udde7",
        "\ud83c\uddea\ud83c\uddf8"   ,"\ud83c\uddf7\ud83c\uddfa"   ,"\ud83c\udde8\ud83c\uddf3"   ,"\ud83c\uddf0\ud83c\uddf7"};

    /** Candidates of emoji_unicode6_to_sbm_tools */
    private static final String[] EMOJI_SBM_TOOLS_TABLE = {
        "\ud83d\udd11","\ud83d\udd12","\ud83d\udd13","\ud83d\udcb0","\ud83d\udcd6","\ud83d\udcdd","\u2702"      ,
        "\ud83d\udcfa","\ud83d\udcbb","\ud83d\udce9","\ud83d\udcf2","\ud83d\udcf1","\u260e"      ,"\ud83d\udce0",
        "\ud83d\udcba","\ud83d\udeac","\ud83d\udc8a","\ud83d\udc89","\ud83d\udebd","\ud83d\udd5b","\ud83d\udd50",
        "\ud83d\udd51","\ud83d\udd52","\ud83d\udd53","\ud83d\udd54","\ud83d\udd55","\ud83d\udd56","\ud83d\udd57",
        "\ud83d\udd58","\ud83d\udd59","\ud83d\udd5a"};

    /** Candidates of emoji_unicode6_to_sbm_hobbies */
    private static final String[] EMOJI_SBM_HOBBIES_TABLE = {
        "\u26bd"      ,"\u26be"      ,"\ud83c\udfbe","\ud83c\udfc0","\u26f3"      ,"\ud83c\udfbf","\ud83c\udfc8",
        "\ud83c\udfb1","\ud83c\udfca","\ud83c\udfc4","\ud83d\udea4","\ud83d\udc55","\ud83d\udc57","\ud83d\udc58",
        "\ud83d\udc59","\ud83d\udc54","\ud83d\udc5f","\ud83d\udc60","\ud83d\udc61","\ud83d\udc62","\ud83c\udf80",
        "\ud83c\udfa9","\ud83d\udc52","\ud83d\udc5c","\ud83d\udcbc","\ud83c\udf02","\ud83d\udc8d","\ud83d\udc8e",
        "\ud83d\udc84","\ud83d\udcf7","\ud83d\udcfb","\ud83d\udce1","\ud83d\udd0a","\ud83c\udfa4","\ud83c\udfa7",
        "\ud83d\udcbf","\ud83d\udcc0","\ud83d\udcfc","\ud83d\udcbd","\ud83c\udfb8","\ud83c\udfba","\ud83c\udfb7",
        "\ud83c\udfa5","\ud83c\udfac","\ud83c\udfab","\ud83c\udfa8","\ud83d\udc88","\ud83d\udc87","\ud83d\udc85",
        "\ud83d\udc86","\ud83d\udec0"};

    /** Candidates of emoji_unicode6_to_sbm_symbol */
    private static final String[] EMOJI_SBM_SYMBOL_TABLE = {
        "\u2b55"      ,"\u274c"      ,"\u2665"      ,"\u2666"      ,"\u2660"      ,"\u2663"      ,"\u2197"      ,
        "\u2196"      ,"\u2198"      ,"\u2199"      ,"\u2b06"      ,"\u2b07"      ,"\u27a1"      ,"\u2b05"      ,
        "\u25b6"      ,"\u25c0"      ,"\u23e9"      ,"\u23ea"      ,"\ud83d\udc46","\ud83d\udc47","\ud83d\udc48",
        "\ud83d\udc49","\u0031\u20e3","\u0032\u20e3","\u0033\u20e3","\u0034\u20e3","\u0035\u20e3","\u0036\u20e3",
        "\u0037\u20e3","\u0038\u20e3","\u0039\u20e3","\u0030\u20e3","\u0023\u20e3","\ud83d\udd2f","\u2648"      ,
        "\u2649"      ,"\u264a"      ,"\u264b"      ,"\u264c"      ,"\u264d"      ,"\u264e"      ,"\u264f"      ,
        "\u2650"      ,"\u2651"      ,"\u2652"      ,"\u2653"      ,"\u26ce"      ,"\ud83c\udd70","\ud83c\udd71",
        "\ud83c\udd8e","\ud83c\udd7e","\ud83d\udcb9","\ud83d\udcb1","\ud83c\udfb0","\ud83c\udd97","\ud83d\udd1d",
        "\ud83c\udd95","\ud83c\udd99","\ud83c\udd92","\ud83c\ude01","\ud83c\udd9a","\ud83c\ude35","\ud83c\ude33",
        "\ud83c\ude50","\ud83c\ude39","\ud83c\ude02","\ud83c\ude2f","\ud83c\ude3a","\ud83c\udd94","\ud83c\ude36",
        "\ud83c\ude1a","\ud83c\ude37","\ud83c\ude38","\u3297"      ,"\u3299"      ,"\ud83d\udd1e","\ud83d\udd31",
        "\u303d"      ,"\ud83d\udd34","\ud83d\udc9f","\u2734"      ,"\u2733"      ,"\ud83d\udd32","\ud83d\udd33",
        "\ud83d\udcf6","\ud83d\udcf3","\ud83d\udcf4","\u27bf"      ,"\u00a9"      ,"\u00ae"      ,"\u2122"};

    /** Candidates of one touch emoji emoji_symbols */
    private static final String[] ONETOUCHEMOJI_EMOJI_SYMBOLS_TABLE = {
                    "\udbba\udf0c","\udbba\udef4","\udbba\udf60","\udbb8\udf30","\udbba\udf05","\udbba\udf5d","\udbb8\udf29",
                    "\udbb8\udf2b","\udbba\udef5","\udbb8\udc00","\udbb8\udc01","\udbb8\udc02","\udbb8\udc03","\udbb8\udc04",
                    "\udbba\udf56","\udbba\udf57","\udbba\udf59","\udbba\udf5b","\udbba\udf5c","\udbb8\udf24","\udbb8\udf3e",
                    "\udbb8\udf31","\udbb8\udf3d","\udbb8\udf26","\udbb8\udf47","\udbb8\udf32","\udbb8\udf3c","\udbb8\udf3a",
                    "\udbb8\udf39","\udbb8\udf41","\udbba\udd80","\udbb8\udc08","\udbba\udd83","\udbba\udf06","\udbba\udf5a",
                    "\udbba\udf93","\udbba\udf94","\udbba\udf95","\udbba\udf97","\udbba\udf96","\udbba\udf0d","\udbba\udf0e",
                    "\udbba\udf0f","\udbba\udc13","\udbb8\udf43","\udbb8\udf27","\udbb8\udf33","\udbb8\udf20","\udbb8\udf23",
                    "\udbb8\udf40","\udbb9\udfd9","\udbba\udc24","\udbba\udd61","\udbba\udf22","\udbba\udf85",

                    //TBD start(QA NO.16)
                    "\udbba\udef0","\udbba\udef1","\udbba\udef2","\udbba\udef3","\udbba\udef6","\udbba\udef7","\udbba\udf04",
                    "\udbba\udf07","\udbba\udf08","\udbba\udf82"
                    //TBD end
    };

    /** The size of history */
    private int mHistorySize;

    /** Whether history is displayed */
    private boolean mCandidateHistory = false;

    /** Context */
    private Context mLocalContext = null;

    /** The index of candidates */
    private int mCurrentIndex = 0;

    /** Interface for the iWnn Engine */
    private iWnnEngine mEngine = null;

    /** Current mode (half-symbol, full-symbol, etc..) */
    private int mMode = MODE_NONE;

    /** Toggle table for SYM key (Japanese) */
    private static final int[] SYM_TOGGLE_TABLE_JP = {
            MODE_SYMBOL,
            MODE_KAO_MOJI,
            MODE_DECOEMOJI,
            MODE_ADD_SYMBOL,
            MODE_EMOJI,

            };

    /** Toggle table for SYM key (Non Japanese) */
    private static final int[] SYM_TOGGLE_TABLE_OTH = {MODE_OTHERS_SYMBOL, MODE_OTHERS_KAO_MOJI,
            MODE_ADD_SYMBOL};

    /** Current language */
    private int mLanguage = iWnnEngine.LanguageType.NONE;

    /** Current dictionary */
    private int mDictionary = iWnnEngine.SetType.NONE;

    /** The specified local represents. This value is used for a name of a history database. */
    private String mLocale = null;

    /** List of DecoEmoji. */
    private DecoEmojiList mDecoEmojiList = null;

    /** Max of return item. */
    private int mMaxItem;

    /** Whether next is previous button. */
    private boolean mIsNextPreviousButton;

    /** Whether last is next button. */
    private boolean mIsLastNextButton;

    /** Count of category. */
    private int mCountCategory;

    /** Stack for top index of partial list. */
    private LinkedList<Integer> mStartIndexStack = new LinkedList<Integer>();

    /** Save the index of candidates */
    private int mSavedCurrentIndex = 0;
    private int mSavedCategoryCount = 0;

    /** The list of Additional Symbol package name. */
    private String[] mAdditionalSymbolPackageList;

    /** The list of tab name is Additional Symbol. */
    private String[] mAdditionalSymbolTabList;

    /** The index of Additional Symbol list. */
    private int mAdditionalSymbolIndex;

    /** The manager of Additional Symbol list. */
    private AdditionalSymbolList mAdditionalSymbolList;

    /** A helper class to manage database creation */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context,String str) {
            super(context, str, null, DATABASE_VERSION);
        }
        /**
         * Called when the database is created for the first time.
         *
         * @see android.database.sqlite.SQLiteOpenHelper#onCreate(SQLiteDatabase)
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL( QUERY_CREATE_TABLE );
            } catch (SQLException e) {
                Log.e(TAG, "IWnnSymbolEngine::onCreate " + e.toString());
            }
        }
        /**
         * Called when the database needs to be upgraded.
         *
         * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(SQLiteDatabase, int, int)
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL("DROP TABLE IF EXISTS notes");
            } catch (SQLException e) {
                Log.e(TAG, "IWnnSymbolEngine::onUpgrade " + e.toString());
            }
            onCreate(db);
        }
    }

    /**
     * Constructor
     * @param context  Context
     * @param locale  The specified local represents
     */
    public IWnnSymbolEngine(Context context, String locale) {
        if (DEBUG) Log.d(TAG, "IWnnSymbolEngine::IWnnSymbolEngine()");
        mEngine = iWnnEngine.getEngine();
        mLocalContext = context;
        mHistorySize = 0;
        mCandidateHistory = false;
        mLocale = locale;
        mDecoEmojiList = new DecoEmojiList(mLocalContext);
        mAdditionalSymbolList = new AdditionalSymbolList(mLocalContext);
    }

    /**
     * Set The Kind of Dictionary
     *
     * @param language See the {@link jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine.LanguageType} class.
     * @param dictionary See the {@link jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine.SetType} class.
     * @return  {@code true} if Success; {@code false} if Failure.
    */
    public boolean setDictionary(int language, int dictionary){
        if (DEBUG) Log.d(TAG, "iWnnSymbolEngine::setDictionary("+language+","+dictionary+")");

        boolean success = mEngine.setDictionary(language, dictionary, mLocalContext.hashCode());
        if (success) {
            init(OpenWnn.getFilesDirPath(mLocalContext));
        } else {
            Log.e(TAG, "failed setDictionary()");
        }
        return success;
    }

    /**
     * Set mode initializing
     */
    public void initializeMode() {
        mLanguage = iWnnEngine.LanguageType.NONE;
        mDictionary = iWnnEngine.LanguageType.NONE;
        mMode = MODE_NONE;
    }

    /**
     * Toggle SYM key
     */
    public void setSymToggle() {
        setSymToggle(true, true, true, true);
    }

    /**
     * Toggle SYM key
     *
     * @param enableEmoji      Whether Emoji is enable to use.
     * @param  enableDecoEmoji  Whether DecoEmoji is enable to use.
     * @param  enableUnicode6   Whether Unicode6Emoji is enable to use.
     * @param  enableKaoMoji    Whether Kaomoji is enable to use.
     */
    public void setSymToggle(boolean enableEmoji, boolean enableDecoEmoji, boolean enableUnicode6, boolean enableKaoMoji) {

        boolean isInAddSymbol = false;
        if ((mMode == MODE_ADD_SYMBOL) && (mAdditionalSymbolPackageList != null)) {
            mAdditionalSymbolIndex++;
            if (mAdditionalSymbolIndex < mAdditionalSymbolPackageList.length) {
                isInAddSymbol = true;
            }
        }

        if (isInAddSymbol) {
            setMode(MODE_ADD_SYMBOL);
        } else {
            int[] table = getSymToggleTable();
            int length = table.length;
            int i = 0, next = 0;

            if (mMode != MODE_NONE) {
                // Search the current symbol list mode
                for (i = 0; i < length; i++) {
                    if (mMode == table[i]) {
                        i++;
                        for (; i < length; i++) {
                            if ((!enableEmoji && (table[i] == MODE_EMOJI))
                                    || (!enableDecoEmoji && (table[i] == MODE_DECOEMOJI))
                                    || (!enableUnicode6 && (table[i] == MODE_EMOJI_UNI6))
                                    || (!enableKaoMoji && (table[i] == MODE_KAO_MOJI))
                                    || (!enableKaoMoji && (table[i] == MODE_OTHERS_KAO_MOJI))
                                    || ((mAdditionalSymbolPackageList == null)
                                            && (table[i] == MODE_ADD_SYMBOL))) {
                                continue;
                            }
                            break;
                        }
                        break;
                    }
                }
                next = i % length;
            }

            // Set a next symbol list
            setMode(table[next]);
        }
    }

    /**
     * Switch from a Symbol list mode to the specific one
     *
     * @param mode  the specific mode
     */
    public void setMode(int mode) {
        if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "IWnnSymbolEngine::setMode()  Start");}
        mMode = mode;

        if ((mLanguage != iWnnEngine.LanguageType.NONE)
                && (mDictionary != iWnnEngine.LanguageType.NONE)) {
            setDictionary(mLanguage, mDictionary);
        }
        mDictionary = iWnnEngine.SetType.NONE;
        mLanguage = iWnnEngine.LanguageType.NONE;

        if (mode != MODE_ADD_SYMBOL) {
            mAdditionalSymbolIndex = 0;
            openHistories();
        }

        mCurrentIndex = 0;
        mHistorySize = 0;
        mCandidateHistory = false;

        int count = loadHistories();
        if (count == 0) {
            loadResource();
        }

        if (mode == MODE_DECOEMOJI) {
            mDecoEmojiList.initializeList();
            mMaxItem = MAX_ITEM_IN_PAGE;
        } else {
            mMaxItem = -1;
        }
        mIsNextPreviousButton = false;
        mIsLastNextButton = false;
        mCountCategory = 0;
        mStartIndexStack.clear();
        if (OpenWnn.FILEIO_PERFORMANCE_DEBUG) {Log.d(TAG, "IWnnSymbolEngine::setMode()  End");}
    }

    /**
     * Get the current mode
     *
     * @return current mode
     */
    public int getMode() {
        return mMode;
    }

    /**
     * Open a database
     *
     */
    private void openHistories(){

        closeHistories();
        DatabaseHelper mDbHelper = new DatabaseHelper(mLocalContext, getDBName());

        try {
            mDatabase = mDbHelper.getWritableDatabase();
        } catch (SQLException e) {
            Log.e(TAG, "IWnnSymbolEngine::openHistories " + e.toString());
        }
    }

    /**
     * Close a database
     *
     */
    private void closeHistories(){
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    /**
     * Load histories from a database
     *
     * @return total number of loading histories
     */
    private int loadHistories() {
        mAddSymbolType = AdditionalSymbolList.ADD_SYMBOL_LIST_SYMBOLTYPE_INIT;
        if(mDatabase == null) {
            return 0;
        }

        if (mMode == MODE_ADD_SYMBOL) {
            int historyCount = 0;

            String packageName = getCurrentPackageName();

            if ((packageName!= null) && (mAdditionalSymbolList.getCandidates(packageName) != null)) {
                mAddSymbolType = mAdditionalSymbolList.getSymbolType(mLocalContext, packageName);
                String[] histories = mAdditionalSymbolList.getHistories(packageName);
                if (histories != null) {
                    for (historyCount = 0; historyCount < mHistories.length; historyCount++) {
                        if (historyCount >= histories.length) {
                            break;
                        }
                        mHistories[historyCount] = histories[historyCount];
                    }
                }

                mHistorySize = historyCount;
                mCandidateHistory = true;
                mCurrentIndex = 0;
            }

            return historyCount;
        }


//#ifdef Only-DOCOMO
        int rowcount = 0;
        SQLiteDatabase db = mDatabase;
        Cursor decoinfolist_cursor = null;
        try {
            if (mMode != MODE_DECOEMOJI
                    && mMode != MODE_ONETOUCHEMOJI_DECOEMOJI
                    && mMode != MODE_ONETOUCHEMOJI_MIXED_EMOJI) {
                //Getting data
                Cursor cursor = db.query(TABLE_NAME, QUERY_COLUMNS, null, null, null, null, null);
                rowcount = cursor.getCount();
                if (rowcount <= 0) {
                    cursor.close();
                    return 0;
                }
                cursor.moveToLast();
                for (int i = 0; i < rowcount ; i++) {
                    mHistories[i] = cursor.getString(1);
                    cursor.moveToPrevious();
                }
                cursor.close();
            } else if (mMode == MODE_ONETOUCHEMOJI_MIXED_EMOJI) {
                //Getting data
                Cursor cursor = db.query(TABLE_NAME, QUERY_COLUMNS, null, null, null, null, null);
                rowcount = cursor.getCount();
                if (rowcount <= 0) {
                    cursor.close();
                    return 0;
                }
                cursor.moveToLast();
                for (int i = 0; i < rowcount ; i++) {
                    mHistories[i] = cursor.getString(1);
                    cursor.moveToPrevious();
                }
                int cnt  = 0;
                for (int i = 0; i < rowcount; i++) {
                    if (EmojiDrawable.isEmoji(mHistories[i])) {
                        mTmpHistories[cnt] = mHistories[i];
                        cnt++;
                        continue;
                    }
                    File file = new File(mHistories[i]);
                    if (file.exists()) {
                        mTmpHistories[cnt] = mHistories[i];
                        cnt++;
                    }
                }
                rowcount = cnt;
                for (int j = 0; j < rowcount; j++) {
                    mHistories[j] = mTmpHistories[j];
                }
                cursor.close();
            } else {
                int emojiType = DecoEmojiUtil.getEditorEmojiType();
                if (emojiType == DecoEmojiUtil.EMOJITYPE_INVALID) {
                    return 0;
                }
                ContentResolver cr = mLocalContext.getContentResolver();
                String order =  DecoEmojiContract.DecoEmojiInfoColumns.HISTORY_CNT + " desc " + "limit " + DB_MAXHISTORY;
                String emojiTypeColum = DecoEmojiContract.makeStringEmojiKind((byte)emojiType);
                String where = DecoEmojiContract.DecoEmojiInfoColumns.KIND + " IN (" + emojiTypeColum + ")";
                String selection[] = {DecoEmojiContract.DecoEmojiInfoColumns.HISTORY_CNT,DecoEmojiContract.DecoEmojiInfoColumns.URI};
                decoinfolist_cursor = cr.query(DecoEmojiContract.CONTENT_DECOINFOLIST_URI, selection, where, null, order);
                if (decoinfolist_cursor != null) {
                    rowcount = decoinfolist_cursor.getCount();
                    if (rowcount == 0) {
                        return 0;
                    }
                    decoinfolist_cursor.moveToFirst();
                    int cnt  = 0;
                    for (int i = 0; i < rowcount; i++) {
                        if (decoinfolist_cursor.getInt((decoinfolist_cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.HISTORY_CNT))) != 0) {
                            mTmpHistories[cnt] = decoinfolist_cursor.getString(decoinfolist_cursor.getColumnIndex(DecoEmojiContract.DecoEmojiInfoColumns.URI));
                            cnt++;
                        }
                        decoinfolist_cursor.moveToNext();
                    }
                    rowcount = cnt;
                    for (int j = 0; j < rowcount; j++) {
                        mHistories[j] = mTmpHistories[j];
                    }
                } else {
                    return 0;
                }
            }
            mHistorySize = rowcount;
            mCandidateHistory = true;
            mCurrentIndex = 0;
        } catch (SQLException e) {
            Log.e("Exception on query", e.toString());
       } finally {
            if ( decoinfolist_cursor != null ) {
                decoinfolist_cursor.close();
            }
       }
        return rowcount;
//#endif Only-DOCOMO

    }

    /**
     * Load histories from a internal database
     *
     * @return total number of loading histories
     */
    private int loadHistoriesInternal() {
        int rowcount = 0;
        SQLiteDatabase db = mDatabase;
        try {
            //Getting data
            Cursor cursor = db.query(TABLE_NAME, QUERY_COLUMNS, null, null, null, null, null);
            rowcount = cursor.getCount();
            if (rowcount <= 0) {
                cursor.close();
                return 0;
            }
            cursor.moveToLast();
            for (int i = 0; i < rowcount ; i++) {
                mHistories[i] = cursor.getString(1);
                cursor.moveToPrevious();
            }
            if (mMode == MODE_DECOEMOJI) {
                int cnt  = 0;
                for (int i = 0; i < rowcount; i++) {
                    File file = new File(mHistories[i]);
                    if (file.exists()) {
                        mTmpHistories[cnt] = mHistories[i];
                        cnt++;
                    }
                }
                rowcount = cnt;
                for (int j = 0; j < rowcount; j++) {
                    mHistories[j] = mTmpHistories[j];
                }
            }
            mHistorySize = rowcount;
            mCandidateHistory = true;
            mCurrentIndex = 0;
            cursor.close();
        } catch (SQLException e) {
            Log.e("Exception on query", e.toString());
        }
        return rowcount;
    }

    /**
     * Get a name of databases
     *
     * @return name
     */
    private String getDBName() {
        String name = null;
        switch (mMode) {
        case MODE_EMOJI:
        case MODE_ONETOUCHEMOJI_EMOJI:
            return DB_NAME_EMOJI;
        case MODE_EMOJI_UNI6:
            return DB_NAME_EMOJI_UNI6;
        case MODE_DECOEMOJI:
        case MODE_ONETOUCHEMOJI_DECOEMOJI:
            return DB_NAME_DECOEMOJI;
        case MODE_ONETOUCHEMOJI_MIXED_EMOJI:
            return DB_NAME_MIXED_EMOJI;
        case MODE_SYMBOL:
        case MODE_ONETOUCHEMOJI_SYMBOL:
            return DB_NAME_HARFSIZESYMBOL;
        case MODE_KAO_MOJI:
            return DB_NAME_KAOMOJI;
        case MODE_OTHERS_KAO_MOJI:
            name = DB_NAME_OTHERS_KAOMOJI;
            break;
        case MODE_OTHERS_SYMBOL:
        default:
            name = DB_NAME_OTHERS;
            break;
        }
        if (mLocale != null) {
            name += "_" + mLocale;
        }
        return name;
    }

    /**
     * Update a database
     *
     * @param info  data for a database
     */
    private void updateHistory(String info) {
//        if(mMode == MODE_KAO_MOJI || mMode == MODE_OTHERS_KAO_MOJI || mDatabase == null) {
        if (mDatabase == null) {
            return;
        }

        int length = mHistorySize;
        int i = 0;

        //Replace a history to the top if there's the same history on temporary holdings
        for (i = 0; i < length; i++) {
            String str = mHistories[i];
            if(str.equals(info)) {
                break;
            }
        }

        if (i == length) {
            if (i == DB_MAXHISTORY) {
                i--;
            } else {
                mHistorySize++;
            }
        }

        for (int j = i; j > 0; j--) {
            mHistories[j] = mHistories[j - 1];
        }

        mHistories[0] = info;

        if (!isPartialList()) {
            mCandidateHistory = true;
            mCurrentIndex = 0;
        }

        if (mMode == MODE_ADD_SYMBOL) {
            String packageName = getCurrentPackageName();
            if (packageName != null) {
                mAdditionalSymbolList.updateHistories(packageName, mHistories, mHistorySize);
            }
            return;
        }

        SQLiteDatabase db = mDatabase;
        try {

        Cursor cursor = db.query(TABLE_NAME, QUERY_COLUMNS, null, null, null, null, null);

        int historyCount = cursor.getCount();
        historyCount = (DB_MAXHISTORY < historyCount) ? DB_MAXHISTORY : historyCount;

        int lastRowId = 0;
        if (cursor.moveToLast()) {
            lastRowId = cursor.getInt(0);
            lastRowId++;
            //Update lastRowId if RowID is over the MAX_ROWID
            if (lastRowId > MAX_ROWID) {
                cursor.moveToFirst();
                ContentValues args = new ContentValues();
                int rowid = 0;
                for (i = 0; i < historyCount; i++) {
                    rowid = cursor.getInt(0);
                    args.put(ROWID, i + 1);
                    db.update(TABLE_NAME, args, "rowid=" +  rowid, null);
                    cursor.moveToNext();
                }
                cursor.close();
                cursor = db.query(TABLE_NAME, QUERY_COLUMNS, null, null, null, null, null);
                lastRowId = i + 1;
            }
        } else {
            lastRowId = 1;
        }

        if (0 < historyCount) {
            int rowid = 0;
            cursor.moveToFirst();
            //Update a history if info is the same
            for (i = 0; i < historyCount ; i++) {
                if(cursor.getString(1).equals(info)) {
                    ContentValues args = new ContentValues();
                    rowid = cursor.getInt(0);
                    cursor.close();
                    args.put(ROWID, lastRowId);
                    db.update(TABLE_NAME, args, "rowid=" +  rowid, null);
                    return;
                }
                cursor.moveToNext();
            }

            //Delete the oldest history if a total of histories is FULL
            if(i >= DB_MAXHISTORY){
                cursor.moveToFirst();
                rowid = cursor.getInt(0);
                db.delete(TABLE_NAME, "rowid=" + rowid, null);
            }
        }
        cursor.close();
        //Put a new word
        ContentValues cv = new ContentValues();
        cv.put(ROWID, lastRowId);
        cv.put(HISTORY_DATA, info);
        db.insert(TABLE_NAME, null, cv);
        } catch (SQLException e) {
            Log.e(TAG, "IWnnSymbolEngine::updateHistory " + e.toString());
    }
    }

    /**
     * Return a candidate.
     *
     * @return A candidate. See {@link jp.co.omronsoft.iwnnime.ml.WnnWord}
     */
    private WnnWord getCandidate() {
        if (DEBUG) Log.d(TAG, "IWnnSymbolEngine::getCandidate()");

        int length = 0;
        int attribute = 0;

        attribute |= iWnnEngine.WNNWORD_ATTRIBUTE_SYMBOLLIST;
        if (mCandidateHistory) {
            if (mCurrentIndex >= mHistorySize) {
                loadResource();
                if (mCandidates != null) {
                    length = mCandidates.length;
                }
            } else {
                attribute |= iWnnEngine.WNNWORD_ATTRIBUTE_HISTORY;
                length = mHistorySize;
            }
        } else {
            if (mCandidates != null) {
                length = mCandidates.length;
            }
        }

        String candidate = null;
        EmojiAssist.DecoEmojiTextInfo info = null;
        if ((mMode == MODE_DECOEMOJI)
                || (mMode == MODE_ONETOUCHEMOJI_DECOEMOJI)
                || (mMode == MODE_ONETOUCHEMOJI_MIXED_EMOJI)) {
            info = mDecoEmojiList.getItem(mCurrentIndex);
            if (info != null){
                candidate = info.getUri();
             }
        }

        if (((mMode != MODE_DECOEMOJI) && (length <= mCurrentIndex))
                || ((mMode == MODE_DECOEMOJI) && (candidate == null))){
            return null;
        }

        if ((attribute & iWnnEngine.WNNWORD_ATTRIBUTE_HISTORY) != 0) {
            // When showing history
            candidate = mHistories[mCurrentIndex];
            info = new EmojiAssist.DecoEmojiTextInfo();
            info.setUri(candidate);

            Cursor cursor = null;
            String selection[] = DecoEmojiUtil.SELECTION_DECOEMOJI_INFO;
            String where =  DecoEmojiContract.DecoEmojiInfoColumns.URI + " = ?";
            String arg[] = {""};
            arg[0]= candidate;

            try {
                cursor = mLocalContext.getContentResolver().query(DecoEmojiContract.CONTENT_DECOINFOLIST_URI, selection, where, arg, null);

                if ( cursor != null && cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    DecoEmojiUtil.setDecoEmojiInfo(cursor, info);
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                if ( cursor != null ) {
                    cursor.close();
                }
            }

            if ((mMode == MODE_DECOEMOJI)
                    || (mMode == MODE_ONETOUCHEMOJI_DECOEMOJI)
                    || ((mMode == MODE_ONETOUCHEMOJI_MIXED_EMOJI)
                    && !EmojiDrawable.isEmoji(candidate))
                    || (mAddSymbolType == AdditionalSymbolList.ADD_SYMBOL_LIST_SYMBOLTYPE_DECOEMOJI)) {
                attribute |= iWnnEngine.WNNWORD_ATTRIBUTE_DECOEMOJI;
            }
        } else {
            Resources r = mLocalContext.getResources();
            if (mIsNextPreviousButton) {
                candidate = r.getString(R.string.ti_symbol_prev_button_txt);
                attribute |= iWnnEngine.WNNWORD_ATTRIBUTE_PREV_BUTTON;
                mCurrentIndex--; // getNextCandidate() increases index.
                mIsNextPreviousButton = false;
            } else if ((0 < mMaxItem) && ((mMaxItem + mCountCategory) <= mCurrentIndex)) {
                candidate = r.getString(R.string.ti_symbol_next_button_txt);
                attribute |= iWnnEngine.WNNWORD_ATTRIBUTE_NEXT_BUTTON;
                mCurrentIndex--; // getNextCandidate() increases index.
                mIsLastNextButton = true;
            } else {
                if (mMode == MODE_DECOEMOJI) {
                    attribute |= iWnnEngine.WNNWORD_ATTRIBUTE_DECOEMOJI;
                } else {
                    if (mCandidates != null) {
                        candidate = mCandidates[mCurrentIndex];
                        if ((mAddSymbolType == AdditionalSymbolList.ADD_SYMBOL_LIST_SYMBOLTYPE_DECOEMOJI)
                                || (mMode == MODE_ONETOUCHEMOJI_DECOEMOJI)) {
                            attribute |= iWnnEngine.WNNWORD_ATTRIBUTE_DECOEMOJI;
                        }
                    }
                }
            }
        }

        if (candidate == null ) {
            return null;
        }

        WnnWord word = new WnnWord(mCurrentIndex, candidate, candidate, attribute, 0, info);
        word.setSymbolMode(mMode);
        return word;

    }

    /**
     * Load a list Resource from the current mode
     */
    private void loadResource() {
        mAddSymbolType = AdditionalSymbolList.ADD_SYMBOL_LIST_SYMBOLTYPE_INIT;
        switch (mMode) {
        case MODE_EMOJI:
            mCandidates = CandidatesEmojiDocomo();
            break;
        case MODE_EMOJI_UNI6:
            mCandidates = CandidatesUnicode6Emoji();
            break;
        case MODE_SYMBOL:
            mCandidates = mLocalContext.getResources().getStringArray(R.array.symbol_item);
            break;
        case MODE_KAO_MOJI:
            mCandidates1 = mLocalContext.getResources().getStringArray(R.array.kaomoji1);
            mCandidates2 = mLocalContext.getResources().getStringArray(R.array.kaomoji2);
            mCandidates3 = mLocalContext.getResources().getStringArray(R.array.kaomoji3);
            mCandidates = (String[])Array.newInstance(String.class, mCandidates1.length + mCandidates2.length + mCandidates3.length);
            System.arraycopy(mCandidates1, 0, mCandidates, 0, mCandidates1.length);
            System.arraycopy(mCandidates2, 0, mCandidates, mCandidates1.length, mCandidates2.length);
            System.arraycopy(mCandidates3, 0, mCandidates, mCandidates1.length + mCandidates2.length , mCandidates3.length);
            break;
        case MODE_OTHERS_KAO_MOJI:
            mCandidates1 = mLocalContext.getResources().getStringArray(R.array.other_kaomoji1);
            mCandidates2 = mLocalContext.getResources().getStringArray(R.array.other_kaomoji2);
            mCandidates3 = mLocalContext.getResources().getStringArray(R.array.other_kaomoji3);
            mCandidates = (String[])Array.newInstance(String.class, mCandidates1.length + mCandidates2.length + mCandidates3.length);
            System.arraycopy(mCandidates1, 0, mCandidates, 0, mCandidates1.length);
            System.arraycopy(mCandidates2, 0, mCandidates, mCandidates1.length, mCandidates2.length);
            System.arraycopy(mCandidates3, 0, mCandidates, mCandidates1.length + mCandidates2.length , mCandidates3.length);
            break;
        case MODE_DECOEMOJI:
            mCandidates = new String[]{""};
            break;
        case MODE_ADD_SYMBOL:
            String packageName = getCurrentPackageName();
            mCandidates = null;
            if (packageName != null) {
                mCandidates = mAdditionalSymbolList.getCandidates(packageName);
                mAddSymbolType = mAdditionalSymbolList.getSymbolType(mLocalContext, packageName);
            }
            if (mCandidates == null) {
                mCandidates = new String[] { null };
            }
            break;
        case MODE_ONETOUCHEMOJI_SYMBOL:
            mCandidates = mLocalContext.getResources().getStringArray(R.array.onetouchemoji_symbol_item);
            break;
        case MODE_ONETOUCHEMOJI_EMOJI:
            mCandidates = ONETOUCHEMOJI_EMOJI_SYMBOLS_TABLE;
            break;
        case MODE_ONETOUCHEMOJI_DECOEMOJI:
            mCandidates = mLocalContext.getResources().getStringArray(R.array.onetouchemoji_decoemoji_item);
            break;
        case MODE_ONETOUCHEMOJI_MIXED_EMOJI:
            mCandidates = new String[] { null };
            break;
        case MODE_OTHERS_SYMBOL:
        default:
            mCandidates = mLocalContext.getResources().getStringArray(R.array.latin_item);
            break;
        }
        mCurrentIndex = 0;
        mCandidateHistory = false;
     }

    /**
     * Load  emoji data for dummy
     */
    private String[] CandidatesEmoji() {
         return null;
    }

    /**
     * Load  emoji data for Unicode6
     */
    private String[] CandidatesUnicode6Emoji() {
        List<String> candidates = new ArrayList<String>();
        int length = 0;
        int index = 0;

        String cate = mLocalContext.getResources().getString(R.string.ti_emoji_category_nature_txt);

        candidates.add(cate);
        length = mCandidates_emoji_unicode6_nature.length;
        for (int i = 0 ; i < length ; i++) {
            candidates.add(mCandidates_emoji_unicode6_nature[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_category_human_txt));
        length = mCandidates_emoji_unicode6_human.length;
        for (int i = 0 ; i < length ; i++) {
            candidates.add(mCandidates_emoji_unicode6_human[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_category_faces_txt));
        length = mCandidates_emoji_unicode6_faces.length;
        for (int i = 0 ; i < length ; i++) {
            candidates.add(mCandidates_emoji_unicode6_faces[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_category_artifacts_txt));
        length = mCandidates_emoji_unicode6_artifacts.length;
        for (int i = 0 ; i < length ; i++) {
            candidates.add(mCandidates_emoji_unicode6_artifacts[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_category_activities_txt));
        length = mCandidates_emoji_unicode6_activities.length;
        for (int i = 0 ; i < length ; i++) {
            candidates.add(mCandidates_emoji_unicode6_activities[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_category_foods_txt));
        length = mCandidates_emoji_unicode6_foods.length;
        for (int i = 0 ; i < length ; i++) {
            candidates.add(mCandidates_emoji_unicode6_foods[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_category_symbols_txt));
        length = mCandidates_emoji_unicode6_symbols.length;
        for (int i = 0 ; i < length ; i++) {
            candidates.add(mCandidates_emoji_unicode6_symbols[i]);
        }

         String[] array=(String[])candidates.toArray(new String[candidates.size()]);

         return array;
}

    /**
     * Load  emoji data for docomo
     */
    private String[] CandidatesEmojiDocomo() {
        List<String> candidates = new ArrayList<String>();
        int length = 0;

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_docomo_category_nature_txt));
        length = EMOJI_DOCOMO_NATURE_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_DOCOMO_NATURE_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_docomo_category_human_txt));
        length = EMOJI_DOCOMO_HUMAN_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_DOCOMO_HUMAN_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_docomo_category_faces_txt));
        length = EMOJI_DOCOMO_FACES_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_DOCOMO_FACES_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_docomo_category_artifacts_txt));
        length = EMOJI_DOCOMO_ARTIFACTS_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_DOCOMO_ARTIFACTS_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_docomo_category_activities_txt));
        length = EMOJI_DOCOMO_ACTIVITIES_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_DOCOMO_ACTIVITIES_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_docomo_category_foods_docomo_txt));
        length = EMOJI_DOCOMO_FOODS_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_DOCOMO_FOODS_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_docomo_category_symbols_txt));
        length = EMOJI_DOCOMO_SYMBOLS_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_DOCOMO_SYMBOLS_TABLE[i]);
        }

         String[] array=(String[])candidates.toArray(new String[candidates.size()]);

         return array;
    }

    /**
     * Load  emoji data for kddi
     */
    private String[] CandidatesEmojiKddi() {
        List<String> candidates = new ArrayList<String>();
        int length = 0;

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_kddi_category_faces_txt));
        length = EMOJI_KDDI_FACES_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_KDDI_FACES_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_kddi_category_human_txt));
        length = EMOJI_KDDI_HUMAN_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_KDDI_HUMAN_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_kddi_category_critter_txt));
        length = EMOJI_KDDI_CRITTER_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_KDDI_CRITTER_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_kddi_category_foods_txt));
        length = EMOJI_KDDI_FOODS_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_KDDI_FOODS_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_kddi_category_nature_txt));
        length = EMOJI_KDDI_NATURE_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_KDDI_NATURE_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_kddi_category_activities_txt));
        length = EMOJI_KDDI_ACTIVITIES_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_KDDI_ACTIVITIES_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_kddi_category_artifacts_txt));
        length = EMOJI_KDDI_ARTIFACTS_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_KDDI_ARTIFACTS_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_kddi_category_tools_txt));
        length = EMOJI_KDDI_TOOLS_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_KDDI_TOOLS_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_kddi_category_symbols_txt));
        length = EMOJI_KDDI_SYMBOLS_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_KDDI_SYMBOLS_TABLE[i]);
        }

         String[] array=(String[])candidates.toArray(new String[candidates.size()]);

         return array;
    }

    /**
     * Load  emoji data for sbm
     */
    private String[] CandidatesEmojiSbm() {
        List<String> candidates = new ArrayList<String>();
        int length = 0;

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_sbm_category_facial_expression_txt));
        length = EMOJI_SBM_FACIAL_EXPRESSION_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_SBM_FACIAL_EXPRESSION_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_sbm_category_emotion_txt));
        length = EMOJI_SBM_EMOTION_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_SBM_EMOTION_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_sbm_category_season_event_txt));
        length = EMOJI_SBM_SEASON_EVENT_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_SBM_SEASON_EVENT_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_sbm_category_character_txt));
        length = EMOJI_SBM_CHARACTER_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_SBM_CHARACTER_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_sbm_category_food_txt));
        length = EMOJI_SBM_FOOD_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_SBM_FOOD_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_sbm_category_daily_life_txt));
        length = EMOJI_SBM_DAILY_LIFE_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_SBM_DAILY_LIFE_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_sbm_category_tools_txt));
        length = EMOJI_SBM_TOOLS_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_SBM_TOOLS_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_sbm_category_hobbies_txt));
        length = EMOJI_SBM_HOBBIES_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_SBM_HOBBIES_TABLE[i]);
        }

        candidates.add(mLocalContext.getResources().getString(R.string.ti_emoji_sbm_category_symbol_txt));
        length = EMOJI_SBM_SYMBOL_TABLE.length;
        for(int i = 0 ; i < length ; i++){
            candidates.add(EMOJI_SBM_SYMBOL_TABLE[i]);
        }

         String[] array=(String[])candidates.toArray(new String[candidates.size()]);

         return array;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#init */
    public void init(String dirPath) {
        if (DEBUG) Log.d(TAG, "WnnSymbolEngine::init()");

        mEngine.init(dirPath);
        mCurrentIndex = 0;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#close */
    public void close() {
        if (DEBUG) Log.d(TAG, "WnnSymbolEngine::close()");
        closeHistories();
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#predict */
    public int predict(ComposingText text, int minLen, int maxLen) {
        if (DEBUG) Log.d(TAG, "IWnnSymbolEngine::predict()");

        int ret = 1;

        if (!isPartialList()) {
            mCurrentIndex = 0;
            mCandidateHistory = true;
        }

        mIsLastNextButton = false;
        mCountCategory = 0;
        return ret;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#convert */
    public int convert(ComposingText text) {
        if (DEBUG) Log.d(TAG, "IWnnSymbolEngine::convert()");
        mCurrentIndex = 0;
        return 0;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#searchWords */
    public int searchWords(String key) {
        if (DEBUG) Log.d(TAG, "IWnnSymbolEngine::searchWords()");
        mCurrentIndex = 0;
        return 0;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#getNextCandidate */
    public WnnWord getNextCandidate() {
        if (DEBUG) Log.d(TAG, "IWnnSymbolEngine::getNextCandidate()");

        if (mIsLastNextButton) {
            return null;
        }

        WnnWord word = getCandidate();
        if (word != null) {
            mCurrentIndex++;

            if (isCategory(word)) {
                mCountCategory++;
            }
        }
        return word;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#learn */
    public boolean learn(WnnWord word) {
        if (DEBUG) Log.d(TAG, "IWnnSymbolEngine::learn()");
        if (word == null) {
            return false;
        }

        if(!word.candidate.equals("\n")){
            int symbolModeType = word.getSymbolMode();
            boolean setOtherMode = false;
            int currentMode = getMode();
            if (symbolModeType != MODE_NONE && symbolModeType != currentMode) {
                setOtherMode = true;
                setMode(symbolModeType);
            }
            updateHistory(word.candidate);
            if (setOtherMode) {
                setMode(currentMode);
            }

            if ((symbolModeType == MODE_EMOJI) || (symbolModeType == MODE_DECOEMOJI)
                    || (symbolModeType == MODE_ONETOUCHEMOJI_EMOJI)
                    || (symbolModeType == MODE_ONETOUCHEMOJI_DECOEMOJI)) {
                setMode(MODE_ONETOUCHEMOJI_MIXED_EMOJI);
                updateHistory(word.candidate);
                setMode(currentMode);
            }
        }

        if ((mMode == MODE_DECOEMOJI) || (word.getSymbolMode() == MODE_ONETOUCHEMOJI_DECOEMOJI)) {
            mEngine.breakSequence();
            return true;
        }

        boolean success = false;
        if ((word.getSymbolMode() == MODE_ONETOUCHEMOJI_SYMBOL)
                || (word.getSymbolMode() == MODE_ONETOUCHEMOJI_EMOJI)
                || (word.getSymbolMode() == MODE_ONETOUCHEMOJI_DECOEMOJI)) {
            success = true;
        } else {
        try {
            String tmp = word.candidate;
            WnnWord learnWord = new WnnWord(0, tmp, tmp,
                                            iWnnEngine.WNNWORD_ATTRIBUTE_MUHENKAN | word.attribute);
            mEngine.learn(learnWord);
            success = true;

        } catch (Exception ex) {
            Log.d("OpenWnn", "IWnnSymbolEngine:learn "+ex);
        }
        }

        return success;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#addWord */
    public int addWord(WnnWord word) {
        if (DEBUG) Log.d(TAG, "IWnnSymbolEngine::addWord()");
        // no implements
        return 0;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#deleteWord */
    public boolean deleteWord(WnnWord word) {
        if (DEBUG) Log.d(TAG, "IWnnSymbolEngine::deleteWord()");
        // no implements
        return true;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#setPreferences */
    public void setPreferences(SharedPreferences pref) {
        // no implements
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#breakSequence */
    public void breakSequence() {
        if (DEBUG) Log.d(TAG, "IWnnSymbolEngine::breakSequence()");
        mEngine.breakSequence();
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#makeCandidateListOf */
    public int makeCandidateListOf(int clausePosition) {
        if (DEBUG) Log.d(TAG, "IWnnSymbolEngine::makeCandidateListOf()");
        // no implements
        return 1;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#initializeDictionary */
    public boolean initializeDictionary(int dictionary, int type) {
        // no implements
        return false;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#initializeDictionary */
    public boolean initializeDictionary(int dictionary) {
        // no implements
        return false;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#searchWords */
    public int searchWords(WnnWord word) {
        // no implements
        return 0;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#getUserDictionaryWords */
    public WnnWord[] getUserDictionaryWords( ) {
        // no implements
        return null;
    }

    /**
     * Get a SYM key toggle table.
     *
     * @return Toggle table; depends on a language.(Japanese or the other)
     */
    private int[] getSymToggleTable() {
        if (!KeyboardLanguagePackData.getInstance().isValid()) {
            return SYM_TOGGLE_TABLE_JP;
        } else {
            return SYM_TOGGLE_TABLE_OTH;
        }
    }

    /**
     * Return whether current engine state is conversion or not.
     *
     * @return {@code true} if conversion is executed currently.
     */
    public boolean isConverting() {
        return false;
    }

    /**
     * Save the index of candidates.
     */
    public void saveCurrentIndex() {
        mSavedCurrentIndex = mCurrentIndex;
        mSavedCategoryCount = mCountCategory;
    }

    /**
     * Restore the index of candidates.
     */
    public void restoreCurrentIndex() {
        mCurrentIndex = mSavedCurrentIndex;
        mCountCategory = mSavedCategoryCount;
    }

    /**
     * Go to a next page.
     */
    public void pageNext() {
        int itemCount = MAX_ITEM_IN_PAGE + mCountCategory;
        mMaxItem += itemCount;
        mIsNextPreviousButton = true;
        mIsLastNextButton = false;
        mCountCategory = 0;
        mStartIndexStack.push(Math.max(mCurrentIndex - itemCount, 0));
    }

    /**
     * Go to a previous page.
     */
    public void pagePrev() {
        mCurrentIndex = (mStartIndexStack.size() == 0) ? 0 : mStartIndexStack.pop();
        if (0 < mCurrentIndex) {
            mIsNextPreviousButton = true;
        } else {
            mCandidateHistory = true;
        }
        mIsLastNextButton = false;
        mMaxItem = mCurrentIndex + MAX_ITEM_IN_PAGE;
        mCountCategory = 0;
    }

    /**
     * Return whether it's a category.
     *
     * @return {@code true} if category
     */
    public static boolean isCategory(WnnWord word) {
        int length = word.candidate.length();
        return ((length >= 3) && (word.candidate.charAt(0) == '['));
    }

    /**
     * Return whether symbol list is partial.
     * @return true if partial.
     */
    private boolean isPartialList() {
        return (0 < mStartIndexStack.size());
    }

    /**
     * Updates Additional Symbol info.
     *
     * @param enableEmoji      Whether being able to use Emoji.
     * @param enableDecoEmoji :Whether being able to use DecoEmoji.
     */
    public void updateAdditionalSymbolInfo(boolean enableEmoji, boolean enableDecoEmoji) {
        mAdditionalSymbolPackageList
                = AdditionalSymbolList.getSelectAdditionalSymbolList(mLocalContext, enableEmoji, enableDecoEmoji);
        mAdditionalSymbolTabList = null;
        if (mAdditionalSymbolPackageList != null) {
            mAdditionalSymbolTabList = new String[mAdditionalSymbolPackageList.length];
            for (int i = 0; i < mAdditionalSymbolTabList.length; i++) {
                String packageName = mAdditionalSymbolPackageList[i];
                mAdditionalSymbolTabList[i] = mAdditionalSymbolList.getTabName(packageName);
            }
        }
    }

    /**
     * Gets tab names of Additional Symbol.
     *
     * @return The list of tab names.
     */
    public String[] getAdditionalSymbolTabNames() {
        if (mAdditionalSymbolTabList == null) {
            return null;
        }
        return mAdditionalSymbolTabList.clone();
    }

    /**
     * Gets index of Additional Symbol.
     *
     * @return The index of Additional Symbol.
      */
    public int getAdditionalSymbolIndex() {
        return mAdditionalSymbolIndex;
     }

    /**
     * Gets index of Additional Symbol.
     *
     * @param index  The index of Additional Symbol.
     */
    public void setAdditionalSymbolIndex(int index) {
        mAdditionalSymbolIndex = index;
    }

    /**
     * Gets current package name of Additional Symbol.
     *
     * @return The current package name of Additional Symbol.
     */
    private String getCurrentPackageName() {
        String packageName = null;
        if (mAdditionalSymbolPackageList != null) {
            packageName = mAdditionalSymbolPackageList[mAdditionalSymbolIndex];
        }
        return packageName;
    }

    /**
     * Deletes the history of additional symbol list.
     */
    public void deleteAdditionalSymbolHistories() {
        mAdditionalSymbolList.deleteHistories();
    }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#getgijistr */
    public int getgijistr(ComposingText text, int type) {
        mCurrentIndex = 0;
        return 0;
    }

    /**
     * Start long-press's action of additional symbol list.
     *
     * @param word  The long press candidate.
     * @return Result of action start request. Success{@code true} or Fail{@code false}.
     */
    public boolean startLongPressActionAdditionalSymbol(WnnWord clickWord) {
        boolean ret = false;
        if (mMode == MODE_ADD_SYMBOL && clickWord != null) {
            String packageName = getCurrentPackageName();
            if (packageName != null) {
                ret = mAdditionalSymbolList.startLongPressActionAdditionalSymbol(packageName, clickWord.candidate);
            }
        }
        return ret;
    }

    /**
     * Get state of last set next button.
     *
     * @return State of last set next button. last set next button{@code true} or not last set next button{@code false}.
     */
   public boolean isIsLastNextButton() {
       return mIsLastNextButton;
   }

    /** @see jp.co.omronsoft.iwnnime.ml.WnnEngine#getWordStrokeLength */
    public int getWordStrokeLength(WnnWord word) {
        if (DEBUG) Log.d(TAG, "IWnnSymbolEngine::getWordStrokeLength()");
        // no implements
        return 0;
    }
}
