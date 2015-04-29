/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
// Copyright (C) 2012 OMRON SOFTWARE Co., Ltd.
// All Rights Reserved.QwertyPeriodCommaTopVerticalGapY

package jp.co.omronsoft.iwnnime.ml;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import jp.co.omronsoft.iwnnime.ml.OpenWnn;
import jp.co.omronsoft.iwnnime.ml.jajp.DefaultSoftKeyboardJAJP;
import jp.co.omronsoft.iwnnime.ml.hangul.ko.DefaultSoftKeyboardKorean;
import java.util.Locale;

public class KeyDrawable extends Drawable {

    /** TEXTBLOCK01 indication letter. */
    private static final String TEXTBLOCK_01[] = {
        "\u3042",   // Hiragana 1
        "\u304b",   // Hiragana 2
        "\u3055",   // Hiragana 3
        "\u305f",   // Hiragana 4
        "\u306a",   // Hiragana 5
        "\u306f",   // Hiragana 6
        "\u307e",   // Hiragana 7
        "\u3084",   // Hiragana 8
        "\u3089",   // Hiragana 9
        "\u308f",   // Hiragana 0
        "@_/:",     // alpha1
        "ABC",      // alpha2
        "DEF",      // alpha3
        "GHI",      // alpha4
        "JKL",      // alpha5
        "MNO",      // alpha6
        "PQRS",     // alpha7
        "TUV",      // alpha8
        "WXYZ",     // alpha9
        "-",        // alpha0
        "@_/:",     // alpha1_lower
        "abc",      // alpha2_lower
        "def",      // alpha3_lower
        "ghi",      // alpha4_lower
        "jkl",      // alpha5_lower
        "mno",      // alpha6_lower
        "pqrs",     // alpha7_lower
        "tuv",      // alpha8_lower
        "wxyz",     // alpha9_lower
        "-",        // alpha0_lower
        "\u30a2",   // katakana1
        "\u30ab",   // katakana2
        "\u30b5",   // katakana3
        "\u30bf",   // katakana4
        "\u30ca",   // katakana5
        "\u30cf",   // katakana6
        "\u30de",   // katakana7
        "\u30e4",   // katakana8
        "\u30e9",   // katakana9
        "\u30ef",   // katakana0
        "1",        // num-half1
        "2",        // num-half2
        "3",        // num-half3
        "4",        // num-half4
        "5",        // num-half5
        "6",        // num-half6
        "7",        // num-half7
        "8",        // num-half8
        "9",        // num-half9
        "0",        // num-half0
        "*",        // num-half11
        "#",        // num-half12
        "1",        // num-full1
        "2",        // num-full2
        "3",        // num-full3
        "4",        // num-full4
        "5",        // num-full5
        "6",        // num-full6
        "7",        // num-full7
        "8",        // num-full8
        "9",        // num-full9
        "0",        // num-full0
        "*",        // num-full11
        "#",        // num-full12
        "SYM",      // SYM
        "\u6587\u5b57", //key_mode_hira.
        "\u6587\u5b57", //key_mode_hira_alpha.
        "\u6587\u5b57", //key_mode_full_alpha.
        "\u6587\u5b57", //key_mode_half_kana.
        "\u6587\u5b57", //key_mode_full_kana.
        "\u6587\u5b57", //key_mode_half_num.
        "\u6587\u5b57", //key_mode_full_num.
        "\u300C\u3000\u300D",
        "A",
        "A",
        "\u309B",       //dakuten.
        ".com",         //.com
        "\u82F1\u6570", //eisukana.
        "\u5909\u63DB",
        "1",
        "2",
        "Select",
        "DeSelect",
        "Copy",
        "Copy",
        "Cut",
        "Cut",
        "\uFF64\uFF61?!",
        ".,?!",
        "1",        // phone-1
        "2",        // phone-2
        "3",        // phone-3
        "4",        // phone-4
        "5",        // phone-5
        "6",        // phone-6
        "7",        // phone-7
        "8",        // phone-8
        "9",        // phone-9
        "0",        // phone-0
        "*",        // phone-11
        "#",        // phone-12
        "Sym",      // phone-sym
        "123",      // phone-123
        "@_'\"",    //alpha1_mail
        "/_'\"",    //alpha1_url
        "\u3002",
        "\u78BA\u5B9A", //ok
        "\u5B9F\u884C", //go
        "\u6B21\u3078", //next
        "\u5B8C\u4E86", //done
        "\u9001\u4FE1", //send
        "\u524D\u3078", //previous
        "SYM",      // SYM
        "\u3001",
        ".",
        "\ud55c",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "0",
        "@",
        "#",
        "%",
        "&",
        "*",
        "(",
        ")",
        ":",
        ";",
        null,
        "\"",
        "'",
        "!",
        "?",
        "/",
        "\u005c\u005c",
        "~",
        null,
        null,
        ".",
        ",",
    };

    /** TEXTBLOCK02 indication letter. */
    private static final String TEXTBLOCK_02[] = {
        "1",    // Hiragana 1
        "2",    // Hiragana 2
        "3",    // Hiragana 3
        "4",    // Hiragana 4
        "5",    // Hiragana 5
        "6",    // Hiragana 6
        "7",    // Hiragana 7
        "8",    // Hiragana 8
        "9",    // Hiragana 9
        "0",    // Hiragana 0
        "1",    // alpha1
        "2",    // alpha2
        "3",    // alpha3
        "4",    // alpha4
        "5",    // alpha5
        "6",    // alpha6
        "7",    // alpha7
        "8",    // alpha8
        "9",    // alpha9
        "0",    // alpha0
        "1",    // alpha1_lower
        "2",    // alpha2_lower
        "3",    // alpha3_lower
        "4",    // alpha4_lower
        "5",    // alpha5_lower
        "6",    // alpha6_lower
        "7",    // alpha7_lower
        "8",    // alpha8_lower
        "9",    // alpha9_lower
        "0",    // alpha0_lower
        null,   // katakana1
        null,   // katakana2
        null,   // katakana3
        null,   // katakana4
        null,   // katakana5
        null,   // katakana6
        null,   // katakana7
        null,   // katakana8
        null,   // katakana9
        null,   // katakana0
        ".@-",                        // num-half1
        "/:_",                        // num-half2
        "~%^",                        // num-half3
        "\uff3b`'\uff3d",             // num-half4
        "<$\uffe5>",                  // num-half5
        "{&\"}",                      // num-half6
        "\u005c\u005c\uff5c",         // num-half7
        "( )",                        // num-half8
        "=;",                         // num-half9
        "+",                          // num-half0
        "!?",                         // num-half11
        ",.",                         // num-half12
        ".@-",                        // num-full1
        "/:_",                        // num-full2
        "~%^",                        // num-full3
        "\uff3b\u2018\u2019\uff3d",   // num-full4
        "<$\uffe5>",                  // num-full5
        "{&\"}",                      // num-full6
        "\u005c\u005c\uff5c",         // num-full7
        "( )",                        // num-full8
        "=;",                         // num-full9
        "+",                          // num-full0
        "!?",                         // num-full11
        ",.",                         // num-full12
        "12?",                        // SYM
        "\u3042",                     //key_mode_hira.
        "AB",                         //key_mode_hira_alpha.
        "A",                          //key_mode_full_alpha.
        "\uFF76\uFF85",               //key_mode_half_kana.
        "\u30AB",                     //key_mode_full_kana.
        "\u3042",                     //key_mode_half_num.
        "1",                          //key_mode_full_num.
        null,
        "/",
        "/",
        "\u3002",                     //dakuten.
        null,
        "\u30AB\u30CA",               //eisukana.
        null,
        "/2",
        "/2",
        null,
        null,
        null,
        null,
        null,
        null,
        "\u3002",
        ".",
        null,       // phone-1
        "ABC",      // phone-2
        "DEF",      // phone-3
        "GHI",      // phone-4
        "JKL",      // phone-5
        "MNO",      // phone-6
        "PQRS",     // phone-7
        "TUV",      // phone-8
        "WXYZ",     // phone-9
        "+",        // phone-0
        null,       // phone-11
        null,       // phone-12
        null,       // phone-sym
        null,       // phone-123
        "1",        //alpha1_mail
        "1",        //alpha1_url
        null,
        "OK", //ok
        "Go", //go
        "Next", //next
        "Done", //done
        "Send", //send
        "Prev", //previous
        "12?",
        "\u3002",
        ",",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
    };

    /** TEXTBLOCK03 indication letter. */
    private static final String TEXTBLOCK_03[] = {
        "@",    // Hiragana 1
        "ABC",  // Hiragana 2
        "DEF",  // Hiragana 3
        "GHI",  // Hiragana 4
        "JKL",  // Hiragana 5
        "MNO",  // Hiragana 6
        "PQRS", // Hiragana 7
        "TUV",  // Hiragana 8
        "WXYZ", // Hiragana 9
        "-",    // Hiragana 0
        null,   // alpha1
        null,   // alpha2
        null,   // alpha3
        null,   // alpha4
        null,   // alpha5
        null,   // alpha6
        null,   // alpha7
        null,   // alpha8
        null,   // alpha9
        null,   // alpha0
        null,   // alpha1_lower
        null,   // alpha2_lower
        null,   // alpha3_lower
        null,   // alpha4_lower
        null,   // alpha5_lower
        null,   // alpha6_lower
        null,   // alpha7_lower
        null,   // alpha8_lower
        null,   // alpha9_lower
        null,   // alpha0_lower
        null,   // katakana1
        null,   // katakana2
        null,   // katakana3
        null,   // katakana4
        null,   // katakana5
        null,   // katakana6
        null,   // katakana7
        null,   // katakana8
        null,   // katakana9
        null,   // katakana0
        null,   // num-half1
        null,   // num-half2
        null,   // num-half3
        null,   // num-half4
        null,   // num-half5
        null,   // num-half6
        null,   // num-half7
        null,   // num-half8
        null,   // num-half9
        null,   // num-half0
        null,   // num-half11
        null,   // num-half12
        null,   // num-full1
        null,   // num-full2
        null,   // num-full3
        null,   // num-full4
        null,   // num-full5
        null,   // num-full6
        null,   // num-full7
        null,   // num-full8
        null,   // num-full9
        null,   // num-full0
        null,   // num-full11
        null,   // num-full12
        "1?",   // SYM
        "A",    //key_mode_hira.
        "A",    //key_mode_hira_alpha.
        null,   //key_mode_full_alpha.
        null,   //key_mode_half_kana.
        null,   //key_mode_full_kana.
        "A",    //key_mode_half_num.
        null,   //key_mode_full_num.
        null,
        "a",
        "a",
        "\u5927", //dakuten.
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "\uff1f",
        "?",
        null,   // phone-1
        null,   // phone-2
        null,   // phone-3
        null,   // phone-4
        null,   // phone-5
        null,   // phone-6
        null,   // phone-7
        null,   // phone-8
        null,   // phone-9
        null,   // phone-0
        null,   // phone-11
        null,   // phone-12
        null,   // phone-sym
        null,   // phone-123
        null,   //alpha1_mail
        null,   //alpha1_url
        null,
        "OK", //ok
        "Go", //go
        "Next", //next
        "Done", //done
        "Send", //send
        "Prev", //previous
        "1?",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
    };

    /** TEXTBLOCK04 indication letter. */
    private static final String TEXTBLOCK_04[] = {
        null,   // key_12key_hiragana1
        null,   // key_12key_hiragana2
        null,   // key_12key_hiragana3
        null,   // key_12key_hiragana4
        null,   // key_12key_hiragana5
        null,   // key_12key_hiragana6
        null,   // key_12key_hiragana7
        null,   // key_12key_hiragana8
        null,   // key_12key_hiragana9
        null,   // key_12key_hiragana0
        null,   // key_12key_alpha1
        null,   // key_12key_alpha2
        null,   // key_12key_alpha3
        null,   // key_12key_alpha4
        null,   // key_12key_alpha5
        null,   // key_12key_alpha6
        null,   // key_12key_alpha7
        null,   // key_12key_alpha8
        null,   // key_12key_alpha9
        null,   // key_12key_alpha0
        null,   // key_12key_alpha1_lower
        null,   // key_12key_alpha2_lower
        null,   // key_12key_alpha3_lower
        null,   // key_12key_alpha4_lower
        null,   // key_12key_alpha5_lower
        null,   // key_12key_alpha6_lower
        null,   // key_12key_alpha7_lower
        null,   // key_12key_alpha8_lower
        null,   // key_12key_alpha9_lower
        null,   // key_12key_alpha0_lower
        null,   // key_12key_katakana1
        null,   // key_12key_katakana2
        null,   // key_12key_katakana3
        null,   // key_12key_katakana4
        null,   // key_12key_katakana5
        null,   // key_12key_katakana6
        null,   // key_12key_katakana7
        null,   // key_12key_katakana8
        null,   // key_12key_katakana9
        null,   // key_12key_katakana0
        null,   // num-half1
        null,   // num-half2
        null,   // num-half3
        null,   // num-half4
        null,   // num-half5
        null,   // num-half6
        null,   // num-half7
        null,   // num-half8
        null,   // num-half9
        null,   // num-half0
        null,   // num-half11
        null,   // num-half12
        null,   // num-full1
        null,   // num-full2
        null,   // num-full3
        null,   // num-full4
        null,   // num-full5
        null,   // num-full6
        null,   // num-full7
        null,   // num-full8
        null,   // num-full9
        null,   // num-full0
        null,   // num-full11
        null,   // num-full12
        null,   // SYM
        "1",    //key_mode_hira.
        "1",    //key_mode_hira_alpha.
        null,   //key_mode_full_alpha.
        null,   //key_mode_half_kana.
        null,   //key_mode_full_kana.
        "1",    //key_mode_half_num.
        null,   //key_mode_full_num.
        null,
        null,
        null,
        "\u5C0F", //dakuten.
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "\uff01",
        "!",
        null,   // phone-1
        null,   // phone-2
        null,   // phone-3
        null,   // phone-4
        null,   // phone-5
        null,   // phone-6
        null,   // phone-7
        null,   // phone-8
        null,   // phone-9
        null,   // phone-0
        null,   // phone-11
        null,   // phone-12
        null,   // phone-sym
        null,   // phone-123
        null,   //alpha1_mail
        null,   //alpha1_url
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
    };

    /** TEXTBLOCK05 indication letter. */
    private static final String TEXTBLOCK_05[] = {
        null,   // key_12key_hiragana1
        null,   // key_12key_hiragana2
        null,   // key_12key_hiragana3
        null,   // key_12key_hiragana4
        null,   // key_12key_hiragana5
        null,   // key_12key_hiragana6
        null,   // key_12key_hiragana7
        null,   // key_12key_hiragana8
        null,   // key_12key_hiragana9
        null,   // key_12key_hiragana0
        null,   // key_12key_alpha1
        null,   // key_12key_alpha2
        null,   // key_12key_alpha3
        null,   // key_12key_alpha4
        null,   // key_12key_alpha5
        null,   // key_12key_alpha6
        null,   // key_12key_alpha7
        null,   // key_12key_alpha8
        null,   // key_12key_alpha9
        null,   // key_12key_alpha0
        null,   // key_12key_alpha1_lower
        null,   // key_12key_alpha2_lower
        null,   // key_12key_alpha3_lower
        null,   // key_12key_alpha4_lower
        null,   // key_12key_alpha5_lower
        null,   // key_12key_alpha6_lower
        null,   // key_12key_alpha7_lower
        null,   // key_12key_alpha8_lower
        null,   // key_12key_alpha9_lower
        null,   // key_12key_alpha0_lower
        null,   // key_12key_katakana1
        null,   // key_12key_katakana2
        null,   // key_12key_katakana3
        null,   // key_12key_katakana4
        null,   // key_12key_katakana5
        null,   // key_12key_katakana6
        null,   // key_12key_katakana7
        null,   // key_12key_katakana8
        null,   // key_12key_katakana9
        null,   // key_12key_katakana0
        null,   // num-half1
        null,   // num-half2
        null,   // num-half3
        null,   // num-half4
        null,   // num-half5
        null,   // num-half6
        null,   // num-half7
        null,   // num-half8
        null,   // num-half9
        null,   // num-half0
        null,   // num-half11
        null,   // num-half12
        null,   // num-full1
        null,   // num-full2
        null,   // num-full3
        null,   // num-full4
        null,   // num-full5
        null,   // num-full6
        null,   // num-full7
        null,   // num-full8
        null,   // num-full9
        null,   // num-full0
        null,   // num-full11
        null,   // num-full12
        null,   // SYM
        null,   //key_mode_hira.
        null,   //key_mode_hira_alpha.
        null,   //key_mode_full_alpha.
        null,   //key_mode_half_kana.
        null,   //key_mode_full_kana.
        null,   //key_mode_half_num.
        null,   //key_mode_full_num.
        null,
        null,
        null,
        null,  //dakuten.
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,   // phone-1
        null,   // phone-2
        null,   // phone-3
        null,   // phone-4
        null,   // phone-5
        null,   // phone-6
        null,   // phone-7
        null,   // phone-8
        null,   // phone-9
        null,   // phone-0
        null,   // phone-11
        null,   // phone-12
        null,   // phone-sym
        null,   // phone-123
        null,   //alpha1_mail
        null,   //alpha1_url
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
    };

    /** PREVIEW_TEXTBLOCK01 indication letter. */
    private static final String PREVIEW_TEXTBLOCK_01[] = {
        "\u3042",   // Hiragana 1
        "\u304b",   // Hiragana 2
        "\u3055",   // Hiragana 3
        "\u305f",   // Hiragana 4
        "\u306a",   // Hiragana 5
        "\u306f",   // Hiragana 6
        "\u307e",   // Hiragana 7
        "\u3084",   // Hiragana 8
        "\u3089",   // Hiragana 9
        "\u308f",   // Hiragana 10
        "@",        // EnglishBig 1
        "A",        // EnglishBig 2
        "D",        // EnglishBig 3
        "G",        // EnglishBig 4
        "J",        // EnglishBig 5
        "M",        // EnglishBig 6
        "P",        // EnglishBig 7
        "T",        // EnglishBig 8
        "W",        // EnglishBig 9
        "-",        // EnglishBig 10
        "@",        // EnglishSmall 1
        "a",        // EnglishSmall 2
        "d",        // EnglishSmall 3
        "g",        // EnglishSmall 4
        "j",        // EnglishSmall 5
        "m",        // EnglishSmall 6
        "p",        // EnglishSmall 7
        "t",        // EnglishSmall 8
        "w",        // EnglishSmall 9
        "-",        // EnglishSmall 10
        "\u30a2",   // Katakana 1
        "\u30ab",   // Katakana 2
        "\u30b5",   // Katakana 3
        "\u30bf",   // Katakana 4
        "\u30ca",   // Katakana 5
        "\u30cf",   // Katakana 6
        "\u30de",   // Katakana 7
        "\u30e4",   // Katakana 8
        "\u30e9",   // Katakana 9
        "\u30ef",   // Katakana 10
        "1",        //Suuji 1
        "2",        //Suuji 2
        "3",        //Suuji 3
        "4",        //Suuji 4
        "5",        //Suuji 5
        "6",        //Suuji 6
        "7",        //Suuji 7
        "8",        //Suuji 8
        "9",        //Suuji 9
        "0",        //Suuji 10
        "*",        //Suuji 11
        "#",        //Suuji 12
        "1",        //SuujiZenkaku 1
        "2",        //SuujiZenkaku 2
        "3",        //SuujiZenkaku 3
        "4",        //SuujiZenkaku 4
        "5",        //SuujiZenkaku 5
        "6",        //SuujiZenkaku 6
        "7",        //SuujiZenkaku 7
        "8",        //SuujiZenkaku 8
        "9",        //SuujiZenkaku 9
        "0",        //SuujiZenkaku 10
        "*",        //SuujiZenkaku 11
        "#",        //SuujiZenkaku 12
        "SYM",      // SYM
        "\u6587\u5b57", //key_mode_hira.
        "\u6587\u5b57", //key_mode_hira_alpha.
        "\u6587\u5b57", //key_mode_full_alpha.
        "\u6587\u5b57", //key_mode_half_kana.
        "\u6587\u5b57", //key_mode_full_kana.
        "\u6587\u5b57", //key_mode_half_num.
        "\u6587\u5b57", //key_mode_full_num.
        "\u300C\u300D",
        "A",
        "A",
        "\u309B",       //dakuten.
        ".com",         //.com
        "\u82F1\u6570", //eisukana.
        "\u5909\u63DB",
        "1",
        "2",
        "Select",
        "DeSelect",
        "Copy",
        "Copy",
        "Cut",
        "Cut",
        "\u3001",
        ".",
        "1",        // phone-1
        "2",        // phone-2
        "3",        // phone-3
        "4",        // phone-4
        "5",        // phone-5
        "6",        // phone-6
        "7",        // phone-7
        "8",        // phone-8
        "9",        // phone-9
        "0",        // phone-0
        "*",        // phone-11
        "#",        // phone-12
        "Sym",      // phone-sym
        "123",      // phone-123
        "@",        //alpha1_mail
        "/",        //alpha1_url
        "\u3002",
        "\u78BA\u5B9A", //ok
        "\u5B9F\u884C", //go
        "\u6B21\u3078", //next
        "\u5B8C\u4E86", //done
        "\u9001\u4FE1", //send
        "\u524D\u3078", //previous
        "SYM",      // SYM
        "\uFF64",
        ".",
        "\ud55c",
        ".",
        ",",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
    };

    /** PREVIEW_TEXTBLOCK02 indication letter. */
    private static final String PREVIEW_TEXTBLOCK_02[] = {
        "\u3044",   // Hiragana 1
        "\u304d",   // Hiragana 2
        "\u3057",   // Hiragana 3
        "\u3061",   // Hiragana 4
        "\u306b",   // Hiragana 5
        "\u3072",   // Hiragana 6
        "\u307f",   // Hiragana 7
        "\uff08",   // Hiragana 8
        "\u308a",   // Hiragana 9
        "\u3092",   // Hiragana 10
        "_",        // EnglishBig 1
        "B",        // EnglishBig 2
        "E",        // EnglishBig 3
        "H",        // EnglishBig 4
        "K",        // EnglishBig 5
        "N",        // EnglishBig 6
        "Q",        // EnglishBig 7
        "U",        // EnglishBig 8
        "X",        // EnglishBig 9
        null,       // EnglishBig 10
        "_",        // EnglishSmall 1
        "b",        // EnglishSmall 2
        "e",        // EnglishSmall 3
        "h",        // EnglishSmall 4
        "k",        // EnglishSmall 5
        "n",        // EnglishSmall 6
        "q",        // EnglishSmall 7
        "u",        // EnglishSmall 8
        "x",        // EnglishSmall 9
        null   ,    // EnglishSmall 10
        "\u30a4",   // Katakana 1
        "\u30ad",   // Katakana 2
        "\u30b7",   // Katakana 3
        "\u30c1",   // Katakana 4
        "\u30cb",   // Katakana 5
        "\u30d2",   // Katakana 6
        "\u30df",   // Katakana 7
        "\uff08",   // Katakana 8
        "\u30ea",   // Katakana 9
        "\u30f2",   // Katakana 10
        ".",        //Suuji 1
        "/",        //Suuji 2
        "~",        //Suuji 3
        "[",        //Suuji 4
        "<",        //Suuji 5
        "{",        //Suuji 6
        "\\",       //Suuji 7
        "(",        //Suuji 8
        "=",        //Suuji 9
        null,       //Suuji 10
        "!",        //Suuji 11
        ",",        //Suuji 12
        ".",        //SuujiZenkaku 1
        "/",        //SuujiZenkaku 2
        "~",        //SuujiZenkaku 3
        "[",        //SuujiZenkaku 4
        "<",        //SuujiZenkaku 5
        "{",        //SuujiZenkaku 6
        "\\",       //SuujiZenkaku 7
        "(",        //SuujiZenkaku 8
        "=",        //SuujiZenkaku 9
        null,       //SuujiZenkaku 10
        "!",        //SuujiZenkaku 11
        ",",        //SuujiZenkaku 12
        null,                         // SYM
        "\u3042",                     //key_mode_hira.
        "\u3042",                     //key_mode_hira_alpha.
        "A",                          //key_mode_full_alpha.
        "\uFF76\uFF85",               //key_mode_half_kana.
        "\u30AB",                     //key_mode_full_kana.
        "\u3042",                     //key_mode_half_num.
        "1",                          //key_mode_full_num.
        null,
        "/",
        "/",
        "\u3002",                      //dakuten.
        null,
        "\u30AB\u30CA",                //eisukana.
        null,
        "/2",
        "/2",
        null,
        null,
        null,
        null,
        null,
        null,
        "\u3002",
        ",",
        null,   // phone-1
        null,   // phone-2
        null,   // phone-3
        null,   // phone-4
        null,   // phone-5
        null,   // phone-6
        null,   // phone-7
        null,   // phone-8
        null,   // phone-9
        null,   // phone-0
        null,   // phone-11
        null,   // phone-12
        null,   // phone-sym
        null,   //alpha1_mail
        "_",    //alpha1_mail
        "_",    //alpha1_url
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "\uFF61",
        ",",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
    };

    /** PREVIEW_TEXTBLOCK03 indication letter. */
    private static final String PREVIEW_TEXTBLOCK_03[] = {
        "\u3046",   // Hiragana 1
        "\u304f",   // Hiragana 2
        "\u3059",   // Hiragana 3
        "\u3064",   // Hiragana 4
        "\u306c",   // Hiragana 5
        "\u3075",   // Hiragana 6
        "\u3080",   // Hiragana 7
        "\u3086",   // Hiragana 8
        "\u308b",   // Hiragana 9
        "\u3093",   // Hiragana 10
        "/",        // EnglishBig 1
        "C",        // EnglishBig 2
        "F",        // EnglishBig 3
        "I",        // EnglishBig 4
        "L",        // EnglishBig 5
        "O",        // EnglishBig 6
        "R",        // EnglishBig 7
        "V",        // EnglishBig 8
        "Y",        // EnglishBig 9
        "0",        // EnglishBig 10
        "/",        // EnglishSmall 1
        "c",        // EnglishSmall 2
        "f",        // EnglishSmall 3
        "i",        // EnglishSmall 4
        "l",        // EnglishSmall 5
        "o",        // EnglishSmall 6
        "r",        // EnglishSmall 7
        "v",        // EnglishSmall 8
        "y",        // EnglishSmall 9
        "0",        // EnglishSmall 10
        "\u30a6",   // Katakana 1
        "\u30af",   // Katakana 2
        "\u30b9",   // Katakana 3
        "\u30c4",   // Katakana 4
        "\u30cc",   // Katakana 5
        "\u30d5",   // Katakana 6
        "\u30e0",   // Katakana 7
        "\u30e6",   // Katakana 8
        "\u30eb",   // Katakana 9
        "\u30f3",   // Katakana 10
        "@",        //Suuji 1
        ":",        //Suuji 2
        "%",        //Suuji 3
        "`",        //Suuji 4
        "$",        //Suuji 5
        "&",        //Suuji 6
        null,       //Suuji 7
        null,       //Suuji 8
        null,       //Suuji 9
        "+",        //Suuji 10
        null,       //Suuji 11
        null,       //Suuji 12
        "@",        //SuujiZenkaku 1
        ":",        //SuujiZenkaku 2
        "%",        //SuujiZenkaku 3
        "\u2018",   //SuujiZenkaku 4
        "$",        //SuujiZenkaku 5
        "&",        //SuujiZenkaku 6
        null,       //SuujiZenkaku 7
        null,       //SuujiZenkaku 8
        null,       //SuujiZenkaku 9
        "+",        //SuujiZenkaku 10
        null,       //SuujiZenkaku 11
        null,       //SuujiZenkaku 12
        null,       // SYM
        "A",        //key_mode_hira.
        "A",        //key_mode_hira_alpha.
        null,       //key_mode_full_alpha.
        null,       //key_mode_half_kana.
        null,       //key_mode_full_kana.
        "A",        //key_mode_half_num.
        null,       //key_mode_full_num.
        null,
        "a",
        "a",
        "\u5927",   //dakuten.
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "\uff1f",
        "?",
        null,   // phone-1
        null,   // phone-2
        null,   // phone-3
        null,   // phone-4
        null,   // phone-5
        null,   // phone-6
        null,   // phone-7
        null,   // phone-8
        null,   // phone-9
        null,   // phone-0
        null,   // phone-11
        null,   // phone-12
        null,   // phone-sym
        null,   // phone-123
        "'",    //alpha1_mail
        "'",    //alpha1_url
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
    };

    /** PREVIEW_TEXTBLOCK04 indication letter. */
    private static final String PREVIEW_TEXTBLOCK_04[] = {
        "\u3048",   // Hiragana 1
        "\u3051",   // Hiragana 2
        "\u305b",   // Hiragana 3
        "\u3066",   // Hiragana 4
        "\u306d",   // Hiragana 5
        "\u3078",   // Hiragana 6
        "\u3081",   // Hiragana 7
        "\uff09",   // Hiragana 8
        "\u308c",   // Hiragana 9
        "-",        // Hiragana 10
        ":",        // EnglishBig 1
        null,       // EnglishBig 2
        null,       // EnglishBig 3
        null,       // EnglishBig 4
        null,       // EnglishBig 5
        null,       // EnglishBig 6
        "S",        // EnglishBig 7
        null,       // EnglishBig 8
        "Z",        // EnglishBig 9
        null,       // EnglishBig 10
        ":",        // EnglishSmall 1
        null,       // EnglishSmall 2
        null,       // EnglishSmall 3
        null,       // EnglishSmall 4
        null,       // EnglishSmall 5
        null,       // EnglishSmall 6
        "s",        // EnglishSmall 7
        null,       // EnglishSmall 8
        "z",        // EnglishSmall 9
        null,       // EnglishSmall 10
        "\u30a8",   // Katakana 1
        "\u30b1",   // Katakana 2
        "\u30bb",   // Katakana 3
        "\u30c6",   // Katakana 4
        "\u30cd",   // Katakana 5
        "\u30d8",   // Katakana 6
        "\u30e1",   // Katakana 7
        "\uff09",   // Katakana 8
        "\u30ec",   // Katakana 9
        "-",        // Katakana 10
        "-",        //Suuji 1
        "_",        //Suuji 2
        "^",        //Suuji 3
        "]",        //Suuji 4
        ">",        //Suuji 5
        "}",        //Suuji 6
        "|",        //Suuji 7
        ")",        //Suuji 8
        ";",        //Suuji 9
        null,       //Suuji 10
        "?",        //Suuji 11
        ".",        //Suuji 12
        "-",        //SuujiZenkaku 1
        "_",        //SuujiZenkaku 2
        "^",        //SuujiZenkaku 3
        "]",        //SuujiZenkaku 4
        ">",        //SuujiZenkaku 5
        "}",        //SuujiZenkaku 6
        "|",        //SuujiZenkaku 7
        ")",        //SuujiZenkaku 8
        ";",        //SuujiZenkaku 9
        null,       //SuujiZenkaku 10
        "?",        //SuujiZenkaku 11
        ".",        //SuujiZenkaku 12
        null,       // SYM
        "1",        //key_mode_hira.
        "1",        //key_mode_hira_alpha.
        null,       //key_mode_full_alpha.
        null,       //key_mode_half_kana.
        null,       //key_mode_full_kana.
        "1",        //key_mode_half_num.
        null,       //key_mode_full_num.
        null,
        null,
        null,
        "\u5C0F",   //dakuten.
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "\uff01",
        "!",
        null,   // phone-1
        null,   // phone-2
        null,   // phone-3
        null,   // phone-4
        null,   // phone-5
        null,   // phone-6
        null,   // phone-7
        null,   // phone-8
        null,   // phone-9
        null,   // phone-0
        null,   // phone-11
        null,   // phone-12
        null,   // phone-sym
        null,   // phone-123
        "\"",   //alpha1_mail
        "\"",   //alpha1_url
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
    };

    /** PREVIEW_TEXTBLOCK05 indication letter. */
    private static final String PREVIEW_TEXTBLOCK_05[] = {
        "\u304a",   // Hiragana 1
        "\u3053",   // Hiragana 2
        "\u305d",   // Hiragana 3
        "\u3068",   // Hiragana 4
        "\u306e",   // Hiragana 5
        "\u307b",   // Hiragana 6
        "\u3082",   // Hiragana 7
        "\u3088",   // Hiragana 8
        "\u308d",   // Hiragana 9
        null,       // Hiragana 10
        "1",        // EnglishBig 1
        "2",        // EnglishBig 2
        "3",        // EnglishBig 3
        "4",        // EnglishBig 4
        "5",        // EnglishBig 5
        "6",        // EnglishBig 6
        "7",        // EnglishBig 7
        "8",        // EnglishBig 8
        "9",        // EnglishBig 9
        null,       // EnglishBig 10
        "1",        // EnglishSmall 1
        "2",        // EnglishSmall 2
        "3",        // EnglishSmall 3
        "4",        // EnglishSmall 4
        "5",        // EnglishSmall 5
        "6",        // EnglishSmall 6
        "7",        // EnglishSmall 7
        "8",        // EnglishSmall 8
        "9",        // EnglishSmall 9
        null,       // EnglishSmall 10
        "\u30aa",   // Katakana 1
        "\u30b3",   // Katakana 2
        "\u30bd",   // Katakana 3
        "\u30c8",   // Katakana 4
        "\u30ce",   // Katakana 5
        "\u30db",   // Katakana 6
        "\u30e2",   // Katakana 7
        "\u30e8",   // Katakana 8
        "\u30ed",   // Katakana 9
        null,       // Katakana 10
        null,       //Suuji 1
        null,       //Suuji 2
        null,       //Suuji 3
        "'",        //Suuji 4
        "\uffe5",   //Suuji 5
        "\"",       //Suuji 6
        null,       //Suuji 7
        null,       //Suuji 8
        null,       //Suuji 9
        null,       //Suuji 10
        null,       //Suuji 11
        null,       //Suuji 12
        null,       //SuujiZenkaku 1
        null,       //SuujiZenkaku 2
        null,       //SuujiZenkaku 3
        "\u2019",   //SuujiZenkaku 4
        "\uffe5",   //SuujiZenkaku 5
        "\"",       //SuujiZenkaku 6
        null,       //SuujiZenkaku 7
        null,       //SuujiZenkaku 8
        null,       //SuujiZenkaku 9
        null,       //SuujiZenkaku 10
        null,       //SuujiZenkaku 11
        null,       //SuujiZenkaku 12
        null,       // SYM
        null,       //key_mode_hira.
        null,       //key_mode_hira_alpha.
        null,       //key_mode_full_alpha.
        null,       //key_mode_half_kana.
        null,       //key_mode_full_kana.
        null,       //key_mode_half_num.
        null,       //key_mode_full_num.
        null,
        null,
        null,
        "\u21D4",   //dakuten.
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,       // phone-1
        "ABC",      // phone-2
        "DEF",      // phone-3
        "GHI",      // phone-4
        "JKL",      // phone-5
        "MNO",      // phone-6
        "PQRS",     // phone-7
        "TUV",      // phone-8
        "WXYZ",     // phone-9
        "+",        // phone-0
        "*",        // phone-11
        null,       // phone-12
        null,       // phone-sym
        null,       // phone-123
        "1",        //alpha1_mail
        "1",        //alpha1_url
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
    };

    private static final int KEY_ID_ALPHA                = 11;
    private static final int KEY_ID_ALPHA_LOWER          = 21;
    private static final int KEY_ID_KATAKANA             = 31;
    private static final int KEY_ID_NUM_HALF             = 41;
    private static final int KEY_ID_SYM                  = 65;
    private static final int KEY_ID_MODE                 = 66;
    private static final int KEY_ID_MODE_ALPHA           = 67;
    private static final int KEY_ID_MODE_ALPHA_FULL      = 68;
    private static final int KEY_ID_MODE_KANA_HALF       = 69;
    private static final int KEY_ID_MODE_KANA_FULL       = 70;
    private static final int KEY_ID_MODE_NUM             = 71;
    private static final int KEY_ID_MODE_NUM_FULL        = 72;
    private static final int KEY_ID_PERIOD               = 73;
    private static final int KEY_ID_12KEY_CAPS           = 74;
    private static final int KEY_ID_12KEY_CAPS_LOWER     = 75;
    private static final int KEY_ID_DAKUTEN              = 76;
    private static final int KEY_ID_COM                  = 77;
    private static final int KEY_ID_EISUKANA             = 78;
    private static final int KEY_ID_12KEY_SPACE_JP       = 79;
    private static final int KEY_ID_QWERTY_NUM_SHIFT_1ST = 80;
    private static final int KEY_ID_QWERTY_NUM_SHIFT_2ND = 81;
    private static final int KEY_ID_12KEY_TEN            = 88;
    private static final int KEY_ID_12KEY_PERIOD_COMMA   = 89;
    private static final int KEY_ID_PHONE_NUM            = 90;
    private static final int KEY_ID_PHONE_NUM_ZERO       = 99;
    private static final int KEY_ID_PHONE_STAR           = 100;
    private static final int KEY_ID_PHONE_SHARP          = 101;
    private static final int KEY_ID_PHONE_SYM            = 102;
    private static final int KEY_ID_PHONE_123            = 103;
    private static final int KEY_ID_ALPHA_MAIL           = 104;
    private static final int KEY_ID_ALPHA_URL            = 105;
    private static final int KEY_ID_QWERTY_FULL_KUTEN    = 106;
    private static final int KEY_ID_OK                   = 107;
    private static final int KEY_ID_PREVIOUS             = 112;
    private static final int KEY_ID_SYM_SMALL            = 113;
    private static final int KEY_ID_QWERTY_PERIOD_FULL   = 114;
    private static final int KEY_ID_QWERTY_PERIOD_HALF   = 115;
    private static final int KEY_ID_QWERTY_KO_KEYMODE    = 116;
    private static final int KEY_ID_QWERTY_NUM_SYMBOL    = 143;
    private static final int KEY_ID_FUNCTION             = 144;
    private static final int KEY_ID_LEFT                 = 145;
    private static final int KEY_ID_PERIOD_KO_KUTEN      = 146;
    private static final int KEY_ID_PERIOD_KO_COMMA      = 147;
    private static final int KEY_ID_CHANGE_LANG_TITLE    = 148;
    private static final int KEY_ID_CHANGE_LANG_JA       = 149;
    private static final int KEY_ID_CHANGE_LANG_KO       = 150;
    private static final int KEY_ID_CHANGE_LANG_EN       = 151;
    private static final int KEY_ID_CHANGE_STYLE_TITLE   = 152;
    private static final int KEY_ID_CHANGE_STYLE_LINE    = 153;
    private static final int KEY_ID_CHANGE_STYLE_NORMAL  = 154;
    private static final int KEY_ID_CHANGE_STYLE_ONEHAND = 155;
    private static final int KEY_ID_CHANGE_STYLE_SPLIT   = 156;
    private static final int KEY_ID_CHANGE_STYLE_FLOATING= 157;

    private static final int KEY_ID_SYMBOL_SYM      = 165;
    private static final int KEY_ID_SYMBOL          = KEY_ID_SYMBOL_SYM - KEY_ID_SYM;

    private static final int KEY_ID_FLICK_PREVIEW                  = 200;
    private static final int KEY_ID_FLICK_PREVIEW_EN_HALF_NUM1     = 210;
    private static final int KEY_ID_FLICK_PREVIEW_EN_FULL_NUM1     = 220;
    private static final int KEY_ID_FLICK_PREVIEW_NUM_HALF_NUM1    = 240;
    private static final int KEY_ID_FLICK_PREVIEW_NUM_FULL_NUM1    = 252;
    private static final int KEY_ID_FLICK_PREVIEW_SYM              = 264;
    private static final int KEY_ID_FLICK_PREVIEW_KEYMODE          = 265;
    private static final int KEY_ID_FLICK_PREVIEW_SYM_NUM          = 266;
    private static final int KEY_ID_FLICK_PREVIEW_PERIOD           = 272;
    private static final int KEY_ID_FLICK_PREVIEW_12KEY_CAPS       = 273;
    private static final int KEY_ID_FLICK_PREVIEW_12KEY_CAPS_LOWER = 274;
    private static final int KEY_ID_FLICK_PREVIEW_DAKUTEN          = 275;
    private static final int KEY_ID_FLICK_PREVIEW_COM              = 276;
    private static final int KEY_ID_FLICK_PREVIEW_EISUKANA         = 277;
    private static final int KEY_ID_FLICK_PREVIEW_12KEY_SPACE_JP   = 278;
    private static final int KEY_ID_FLICK_QWERTY_NUM_SHIFT_1ST     = 279;
    private static final int KEY_ID_FLICK_QWERTY_NUM_SHIFT_2ND     = 280;
    private static final int KEY_ID_FLICK_PREVIEW_12KEY_TEN        = 287;
    private static final int KEY_ID_FLICK_QWERTY_FULL_KUTEN        = 305;
    private static final int KEY_ID_FLICK_PREVIEW_OK               = 306;
    private static final int KEY_ID_FLICK_PREVIEW_PREVIOUS         = 311;
    private static final int KEY_ID_FLICK_PREVIEW_SYM_SMALL        = 312;
    private static final int KEY_ID_FLICK_PREVIEW_END              = 399;

    private static final int KEY_ID_PREVIEW                      = 400;
    private static final int KEY_ID_PREVIEW_SYM                  = 464;
    private static final int KEY_ID_PREVIEW_KEYMODE              = 465;
    private static final int KEY_ID_PREVIEW_SYM_NUM              = 466;
    private static final int KEY_ID_PREVIEW_PERIOD               = 472;
    private static final int KEY_ID_PREVIEW_12KEY_CAPS           = 473;
    private static final int KEY_ID_PREVIEW_12KEY_CAPS_LOWER     = 474;
    private static final int KEY_ID_PREVIEW_DAKUTEN              = 475;
    private static final int KEY_ID_PREVIEW_COM                  = 476;
    private static final int KEY_ID_PREVIEW_EISUKANA             = 477;
    private static final int KEY_ID_PREVIEW_12KEY_SPACE_JP       = 478;
    private static final int KEY_ID_PREVIEW_QWERTY_NUM_SHIFT_1ST = 479;
    private static final int KEY_ID_PREVIEW_QWERTY_NUM_SHIFT_2ND = 480;
    private static final int KEY_ID_PREVIEW_12KEY_TEN            = 487;
    private static final int KEY_ID_PREVIEW_12KEY_PERIOD_COMMA   = 488;
    private static final int KEY_ID_PREVIEW_PHONE_NUM            = 489;
    private static final int KEY_ID_PREVIEW_PHONE_SYM            = 501;
    private static final int KEY_ID_PREVIEW_PHONE_123            = 502;
    private static final int KEY_ID_PREVIEW_SYM_SMALL            = 512;
    private static final int KEY_ID_PREVIEW_END                  = 599;
    private static final int KEY_ID_PREVIEW_QWERTY_PERIOD_FULL   = 513;
    private static final int KEY_ID_PREVIEW_QWERTY_PERIOD_HALF   = 514;
    private static final int KEY_ID_PREVIEW_QWERTY_KO_KEYMODE    = 515;
    private static final int KEY_ID_PREVIEW_PERIOD_KO_KUTEN      = 516;
    private static final int KEY_ID_PREVIEW_PERIOD_KO_COMMA      = 517;

    private static final int KEY_ID_KEYTOP_PREVIEW_OFFSET    = 399;

    private static final int KEY_ID_NAVI_SELECT_PREVIEW      = 601;
    private static final int KEY_ID_NAVI_DESELECT_PREVIEW    = 602;
    private static final int KEY_ID_NAVI_CLOSE_PREVIEW       = 603;
    private static final int KEY_ID_NAVI_COPY_PREVIEW        = 604;
    private static final int KEY_ID_NAVI_PASTE_PREVIEW       = 605;
    private static final int KEY_ID_NAVI_CUT_PREVIEW         = 606;
    private static final int KEY_ID_NAVI_ALL_PREVIEW         = 607;

    private static final int KEY_ID_NAVI_SELECT      = 611;
    private static final int KEY_ID_NAVI_DESELECT    = 612;
    private static final int KEY_ID_NAVI_CLOSE       = 613;
    private static final int KEY_ID_NAVI_ALL         = 614;
    private static final int KEY_ID_NAVI_COPY        = 615;
    private static final int KEY_ID_NAVI_PASTE       = 616;
    private static final int KEY_ID_NAVI_CUT         = 617;
    private static final int KEY_ID_NAVI_COPY_OFF    = 618;
    private static final int KEY_ID_NAVI_PASTE_OFF   = 619;
    private static final int KEY_ID_NAVI_CUT_OFF     = 620;

    private static final int KEY_MODE_CHANGE_EFFECTIVE_OFF_HIRA_B_PREVIEW         = 621;
    private static final int KEY_MODE_CHANGE_EFFECTIVE_OFF_ENG_B_PREVIEW          = 622;
    private static final int KEY_MODE_CHANGE_EFFECTIVE_OFF_NUM_B_PREVIEW          = 623;
    private static final int KEY_MODE_CHANGE_EFFECTIVE_OFF_NON_B_PREVIEW          = 624;
    private static final int KEY_MODE_CHANGE_EFFECTIVE_OFF_ENG_OFF_HIRA_B_PREVIEW = 625;
    private static final int KEY_MODE_CHANGE_EFFECTIVE_OFF_NUM_OFF_HIRA_B_PREVIEW = 626;
    private static final int KEY_MODE_CHANGE_INVALID_OFF_HIRA_B_PREVIEW           = 627;
    private static final int KEY_MODE_CHANGE_INVALID_OFF_ENG_B_PREVIEW            = 628;
    private static final int KEY_MODE_CHANGE_INVALID_OFF_NUM_B_PREVIEW            = 629;
    private static final int KEY_MODE_CHANGE_INVALID_OFF_NON_B_PREVIEW            = 630;
    private static final int KEY_MODE_CHANGE_INVALID_OFF_ENG_OFF_HIRA_B_PREVIEW   = 631;
    private static final int KEY_MODE_CHANGE_INVALID_OFF_NUM_OFF_HIRA_B_PREVIEW   = 632;

    private static final int KEY_MODE_CHANGE_SYMNUM_NUM_B_PREVIEW=633;
    private static final int KEY_MODE_CHANGE_SYMNUM_SYM_B_PREVIEW=634;

    private static final int KEY_ID_NAVI_SELECT_SPLIT    = 635;
    private static final int KEY_ID_NAVI_DESELECT_SPLIT  = 636;
    private static final int KEY_ID_NAVI_PASTE_SPLIT     = 637;
    private static final int KEY_ID_NAVI_PASTE_OFF_SPLIT = 638;

    private static final float KEY_TEXT_SHADOW_RADIUS = 5;

    private static final int capsKeyGap = 30;

    /** keyIconId table for Keyboard style of language popup. */
    private static final int[] KEYBOARD_STYLE_KEY_ID_TABLE =
    {
        KEY_ID_CHANGE_STYLE_NORMAL,
        KEY_ID_CHANGE_STYLE_ONEHAND,
        KEY_ID_CHANGE_STYLE_SPLIT,
        KEY_ID_CHANGE_STYLE_FLOATING,
    };
    /** normal and press icon drawable table for Keyboard style of language popup. */
    private static final int[][] KEYBOARD_STYLE_KEY_ICON_TABLE = {
            {R.drawable.ime_keypad_icon_popup_keyboardstyle_input_normal,
             R.drawable.ime_keypad_icon_popup_keyboardstyle_input_pressed},
            {R.drawable.ime_keypad_icon_popup_keyboardstyle_onehanded_normal,
             R.drawable.ime_keypad_icon_popup_keyboardstyle_onehanded_pressed},
            {R.drawable.ime_keypad_icon_popup_keyboardstyle_split_normal,
             R.drawable.ime_keypad_icon_popup_keyboardstyle_split_pressed},
            {R.drawable.ime_keypad_icon_popup_keyboardstyle_floating_normal,
             R.drawable.ime_keypad_icon_popup_keyboardstyle_floating_pressed }};
    /** normal and press check drawable table for Keyboard style of language popup. */
    private static final int[] KEYBOARD_STYLE_KEY_ICON_CHECK_TABLE = {
            R.drawable.ime_keypad_icon_check_normal,
            R.drawable.ime_keypad_icon_check_pressed};

    private int mId;
    private float mTextBlock01Size;
    private float mTextBlock02Size;
    private float mTextBlock03Size;
    private float mTextBlock04Size;
    private float mTextBlock05Size;

    private int mKeyWidth;
    private int mKeyHeight;
    private CharSequence mLabel;

    /** Rotation. */
    private static boolean mVerticalDisp;

    private static Double mLeftCenterGap;
    private static int m10KeyCenterTopVerticalGapY;
    private static int m10KeyPhoneCenterTopVerticalGapY;
    private static int m10KeyCenterTopGapY;
    private static int m10KeyPhoneCenterTopGapY;
    private static int m10KeyEisukanaCenterTopVerticalGapY;
    private static int m10KeyEisukanaCenterTopHorizontalGapY;
    private static int m10KeyLeftGapX;
    private static int m10KeyLeftEmojioffGapX;
    private static int m10KeyLeftVerticalGapX;
    private static int m10KeyLeftVerticalGapEmojiX;
    private static int m10KeyRightGapX;
    private static int m10KeyRightEmojioffGapX;
    private static int m10KeyRightVerticalGapX;
    private static int m10KeyRightVerticalGapEmojiX;
    private static int m10KeyCenterBottomGapY;
    private static int m10KeyPhoneCenterBottomGapY;
    private static int m10KeyCenterBottomHorizontalGapY;
    private static int m10KeyCenterBottomVerticalGapY;
    private static int m10KeyPhoneCenterBottomVerticalGapY;
    private static int m10KeyEisukanaCenterBottomVerticalGapY;
    private static int m10KeySymKeyTopVerticalGapY;
    private static int m10KeySymKeyTopGapY;
    private static int mQwertySymKeyTopVerticalGapY;
    private static int mQwertySymKeyTopGapY;
    private static int mDakutenKeyVerticalGap;
    private static int mDakutenKeyGap;
    private static int mDakutenKeyTopGap;
    private static int mDakutenKeyBottomVerticalGapY;
    private static int mDakutenKeyBottomGapY;
    private static int mComKeyTopVerticalGapY;
    private static int mComKeyTopGapY;
    private static int mEisukanaTopGapY;
    private static int mEisukanaLineGapY;
    private static int mQwertyShiftTopVerticalGapY;
    private static int mQwertyShiftTopGapY;
    private static int mPreviewCenterVerticalGapX;
    private static int mPreviewCenterTopVerticalGapY;
    private static int mPreviewCenterTopEnVerticalGapY;
    private static int mPreviewCenterLeftRightVerticalGapY;
    private static int mPreviewTopBottomVerticalGapY;

    private static int mQwertyPeriodCommaTopVerticalGapY;
    private static int mQwertyPeriodCommaTopHorizontalGapY;
    private static int mQwertyPeriodCommaLeftVerticalGapX;
    private static int mQwertyPeriodCommaLeftHorizontalGapX;
    private static int mQwertyEmojiPeriodCommaLeftVerticalGapX;
    private static int mQwertyEmojiPeriodCommaLeftHorizontalGapX;
    private static int mQwertyPeriodKutoutenLeftVerticalGapX;
    private static int mQwertyPeriodKutoutenLeftHorizontalGapX;
    private static int mQwertyEmojiPeriodKutoutenRightHorizontalGapX;
    private static int mQwertyPeriodpreviewGapX;
    private static int mQwertyPeriodJaLandLeftAdjustGapX;
    private static int mQwertyPeriodJaPortLeftAdjustGapX;
    private static int mQwertyEmojiPeriodJaLandRightAdjustGapX;
    private static int mQwertyEmojiPeriodJaPortLeftAdjustGapX;
    private static int mQwertyPeriodEnLandLeftAdjustGapX;
    private static int mQwertyPeriodEnPortLeftAdjustGapX;
    private static int mQwertyEmojiPeriodEnLandRightAdjustGapX;
    private static int mQwertyEmojiPeriodEnPortLeftAdjustGapX;

    private static int mQwertyNumSymbolTopGapY;
    private static int mQwertyNumSymbolRightGapX;

    private static int mOneHanded10KeyCenterTopGapY;
    private static int mOneHanded10KeyLeftGapX;
    private static int mOneHanded10KeyRightGapX;
    private static int mOneHanded10KeyCenterBottomGapY;
    private static int mOneHanded10KeySymKeyTopVerticalGapY;

    private static int mOneHandedQwertyPeriodKutoutenLeftVerticalGapX;
    private static int mOneHandedQwertyPeriodCommaLeftVerticalGapX;
    private static int mOneHandedQwertyEmojiPeriodJaPortLeftAdjustGapX;
    private static int mOneHandedQwertyEmojiPeriodEnPortLeftAdjustGapX;
    private static int mOneHandedQwertyPeriodJaPortLeftAdjustGapX;
    private static int mOneHandedQwertyPeriodEnPortLeftAdjustGapX;
    private static int mOneHandedQwertyPeriodCommaTopVerticalGapY;
    private static int mOneHandedQwertyNumSymbolTopGapY;
    private static int mOneHandedQwertyNumSymbolRightGapX;

    private static int mFloatingDakutenKeyBottomGapY;
    private static int mFloatingQwertyPeriodPortLeftAdjustGapX;

    private TextPaint mPaintTextBlock01 = new TextPaint();
    private TextPaint mPaintTextBlock02 = new TextPaint();
    private TextPaint mPaintTextBlock03 = new TextPaint();
    private TextPaint mPaintTextBlock04 = new TextPaint();
    private TextPaint mPaintTextBlock05 = new TextPaint();

    private Paint mPaint = new Paint();
    private static float m12KeyHeightVertical;
    private static float m12KeyHeightHorizontal;
    private static float mQwertyKeyHeightDeviation;
    private static float mKeySubIconGapX;
    private static float mKeySubIconVerticalGapX;
    private static float mKeySymSubIconGapY;
    private static float mKeySymSubIconQwertyGapY;
    private static float mKeyFunctionSubIconGapX;
    private static float mKeyModeSubIconGapX;
    private static float mKeyModeSubIconVerticalGapX;
    private static float mKeyModeSubIconGapY;
    private static float mKeyModeSubIconQwertyGapY;
    private static float mKeyLeftSubIconGapX;
    private static float mKeyLeftSubIconGapY;

    private static boolean mOneTouchEmojiShown = false;

    private static boolean mOneHandedShown  = false;

    private static KeyboardSkinData mKeySkin;
    private static boolean mInitializeFlg = false;
    /** Target language. */

    /**
     * Constructor.
     * @param r Resources.
     * @param id ID of the Key.
     * @param width The width of the Key.
     * @param height The height of the Key.
     * @return none.
     */
    public KeyDrawable(Resources r, int id, int width, int height){
        mId = id;
        mKeyWidth = width;
        mKeyHeight = height;
        mLabel = null;

        mPaintTextBlock01.setTypeface(Typeface.DEFAULT_BOLD);
        mPaintTextBlock02.setTypeface(Typeface.DEFAULT_BOLD);
        mPaintTextBlock03.setTypeface(Typeface.DEFAULT_BOLD);
        mPaintTextBlock04.setTypeface(Typeface.DEFAULT_BOLD);
        mPaintTextBlock05.setTypeface(Typeface.DEFAULT_BOLD);

        mPaintTextBlock01.setAntiAlias(true);
        mPaintTextBlock02.setAntiAlias(true);
        mPaintTextBlock03.setAntiAlias(true);
        mPaintTextBlock04.setAntiAlias(true);
        mPaintTextBlock05.setAntiAlias(true);

        // To camouflage portrait orientation, load resources for portrait orientation.
        // (Layout of floating keyboard in landscape orientation is same as in portrait one)
        boolean isFloatingLandscape =
                InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase.isFloatingLandscape();
        mVerticalDisp = ((Configuration.ORIENTATION_PORTRAIT == r.getConfiguration().orientation) || isFloatingLandscape)
                ? true : false;

        if (!mInitializeFlg) {
            mKeySkin = KeyboardSkinData.getInstance();
            initialize(r);
            mInitializeFlg = true;
        }
    }

    /**
     * Constructor.
     * @param r Resources.
     * @param id ID of the Key.
     * @param width The width of the Key.
     * @param height The height of the Key.
     * @param label The label of the Key.
     * @return none.
     */
    public KeyDrawable(Resources r, int id, int width, int height, CharSequence label){
        mId = id;
        mKeyWidth = width;
        mKeyHeight = height;
        mLabel = (!TextUtils.isEmpty(label))? label : null;

        mPaintTextBlock01.setTypeface(Typeface.DEFAULT_BOLD);
        mPaintTextBlock02.setTypeface(Typeface.DEFAULT_BOLD);
        mPaintTextBlock03.setTypeface(Typeface.DEFAULT_BOLD);
        mPaintTextBlock04.setTypeface(Typeface.DEFAULT_BOLD);
        mPaintTextBlock05.setTypeface(Typeface.DEFAULT_BOLD);

        mPaintTextBlock01.setAntiAlias(true);
        mPaintTextBlock02.setAntiAlias(true);
        mPaintTextBlock03.setAntiAlias(true);
        mPaintTextBlock04.setAntiAlias(true);

        // To camouflage portrait orientation, load resources for portrait orientation.
        // (Layout of floating keyboard in landscape orientation is same as in portrait one)
        boolean isFloatingLandscape =
                InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase.isFloatingLandscape();
        mVerticalDisp = ((Configuration.ORIENTATION_PORTRAIT == r.getConfiguration().orientation) || isFloatingLandscape)
                ? true : false;

        mPaintTextBlock05.setAntiAlias(true);
        if (!mInitializeFlg) {
            mKeySkin = KeyboardSkinData.getInstance();
            initialize(r);
            mInitializeFlg = true;
        }
    }

    /**
     * initialize.
     * Initialization setting of TextPaint.
     * @param r Resources.
     * @return none.
     */
    private void initialize(Resources r) {

        mLeftCenterGap = Double.parseDouble(OpenWnn.getContext().getResources()
                            .getString(R.string.paint_left_center_gap));

        m10KeyCenterTopVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_10key_center_top_vertical_gap);
        m10KeyPhoneCenterTopVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_10key_phone_center_top_vertical_gap);
        m10KeyCenterTopGapY = r.getDimensionPixelSize(R.dimen.paint_10key_center_top_gap);
        m10KeyPhoneCenterTopGapY = r.getDimensionPixelSize(R.dimen.paint_10key_phone_center_top_gap);
        m10KeyEisukanaCenterTopVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_10key_eisukana_center_top_vertical_gap);
        m10KeyEisukanaCenterTopHorizontalGapY = r.getDimensionPixelSize(R.dimen.paint_10key_eisukana_center_top_horizontal_gap);

        m10KeyLeftGapX = r.getDimensionPixelSize(R.dimen.paint_10key_left_gap);
        m10KeyLeftEmojioffGapX = r.getDimensionPixelSize(R.dimen.paint_10key_left_emojioff_gap);
        m10KeyLeftVerticalGapX = r.getDimensionPixelSize(R.dimen.paint_10key_left_vertical_gap);
        m10KeyLeftVerticalGapEmojiX = r.getDimensionPixelSize(R.dimen.paint_10key_left_vertical_gap_emoji);
        m10KeyRightGapX = r.getDimensionPixelSize(R.dimen.paint_10key_right_gap);
        m10KeyRightEmojioffGapX = r.getDimensionPixelSize(R.dimen.paint_10key_right_emojioff_gap);

        m10KeyRightVerticalGapX = r.getDimensionPixelSize(R.dimen.paint_10key_right_vertical_gap);
        m10KeyRightVerticalGapEmojiX = r.getDimensionPixelSize(R.dimen.paint_10key_right_vertical_emoji_gap);
        m10KeyCenterBottomGapY = r.getDimensionPixelSize(R.dimen.paint_10key_center_bottom_gap);
        m10KeyPhoneCenterBottomGapY = r.getDimensionPixelSize(R.dimen.paint_10key_phone_center_bottom_gap);
        m10KeyCenterBottomHorizontalGapY = r.getDimensionPixelSize(R.dimen.paint_10key_center_bottom_horizontal_gap);
        m10KeyCenterBottomVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_10key_center_bottom_vertical_gap);
        m10KeyPhoneCenterBottomVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_10key_phone_center_bottom_vertical_gap);
        m10KeyEisukanaCenterBottomVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_10key_eisukana_center_bottom_vertical_gap);

        m10KeySymKeyTopVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_12key_symkey_top_vertical_gap);
        m10KeySymKeyTopGapY = r.getDimensionPixelSize(R.dimen.paint_12key_symkey_top_gap);
        mQwertySymKeyTopVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_qwerty_symkey_top_vertical_gap);
        mQwertySymKeyTopGapY = r.getDimensionPixelSize(R.dimen.paint_qwerty_symkey_top_gap);

        mDakutenKeyVerticalGap = r.getDimensionPixelSize(R.dimen.paint_dakutenkey_vertical_gap);
        mDakutenKeyGap = r.getDimensionPixelSize(R.dimen.paint_dakutenkey_gap);
        mDakutenKeyTopGap = r.getDimensionPixelSize(R.dimen.paint_dakutenkey_top_gap);

        mDakutenKeyBottomVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_dakutenkey_bottom_vertical_gap);
        mDakutenKeyBottomGapY = r.getDimensionPixelSize(R.dimen.paint_dakutenkey_bottom_gap);

        mComKeyTopVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_comkey_top_vertical_gap);
        mComKeyTopGapY = r.getDimensionPixelSize(R.dimen.paint_comkey_top_gap);

        mEisukanaTopGapY = r.getDimensionPixelSize(R.dimen.paint_eisukana_top_gap);
        mEisukanaLineGapY = r.getDimensionPixelSize(R.dimen.paint_eisukana_line_gap);

        mQwertyShiftTopVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_qwerty_shift_top_vertical_gap);
        mQwertyShiftTopGapY = r.getDimensionPixelSize(R.dimen.paint_qwerty_shift_top_gap);

        mPreviewCenterVerticalGapX = r.getDimensionPixelSize(R.dimen.paint_preview_left_right_vertical_gap);
        mPreviewCenterLeftRightVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_preview_center_left_right_vertical_gap);
        mPreviewCenterTopVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_preview_top_vertical_gap);
        mPreviewCenterTopEnVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_preview_top_en_vertical_gap);
        mPreviewTopBottomVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_preview_top_bottom_vertical_gap);

        mQwertyPeriodCommaTopVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_comma_top_vertical_gap);
        mQwertyPeriodCommaTopHorizontalGapY = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_comma_top_horizontal_gap);
        mQwertyPeriodCommaLeftVerticalGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_comma_left_vertical_gap);
        mQwertyPeriodCommaLeftHorizontalGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_comma_left_horizontal_gap);
        mQwertyEmojiPeriodCommaLeftVerticalGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_emoji_period_comma_left_vertical_gap);
        mQwertyEmojiPeriodCommaLeftHorizontalGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_emoji_period_comma_left_horizontal_gap);
        mQwertyPeriodKutoutenLeftVerticalGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_kutouten_left_vertical_gap);
        mQwertyPeriodKutoutenLeftHorizontalGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_kutouten_left_horizontal_gap);
        mQwertyEmojiPeriodKutoutenRightHorizontalGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_emoji_period_kutouten_right_horizontal_gap);
        mQwertyPeriodpreviewGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_preview_gap);
        mQwertyPeriodJaLandLeftAdjustGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_ja_h_left_adjust);
        mQwertyPeriodJaPortLeftAdjustGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_ja_v_left_adjust);
        mQwertyEmojiPeriodJaLandRightAdjustGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_emoji_ja_h_right_adjust);
        mQwertyEmojiPeriodJaPortLeftAdjustGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_emoji_ja_v_left_adjust);
        mQwertyPeriodEnLandLeftAdjustGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_en_h_left_adjust);
        mQwertyPeriodEnPortLeftAdjustGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_en_v_left_adjust);
        mQwertyEmojiPeriodEnLandRightAdjustGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_emoji_en_h_right_adjust);
        mQwertyEmojiPeriodEnPortLeftAdjustGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_period_emoji_en_v_left_adjust);

        mQwertyNumSymbolTopGapY = r.getDimensionPixelSize(R.dimen.paint_qwerty_num_symbol_top_gap);
        mQwertyNumSymbolRightGapX = r.getDimensionPixelSize(R.dimen.paint_qwerty_num_symbol_right_gap);
        m12KeyHeightVertical = r.getDimensionPixelSize(R.dimen.key_12key_key_height_vertical);
        m12KeyHeightHorizontal = r.getDimensionPixelSize(R.dimen.key_12key_key_height_horizontal);
        mQwertyKeyHeightDeviation = r.getDimensionPixelSize(R.dimen.key_qwerty_key_height_deviation_adjustment);
        mKeySubIconGapX = r.getDimensionPixelSize(R.dimen.key_sub_icon_left_adjustment);
        mKeySubIconVerticalGapX = r.getDimensionPixelSize(R.dimen.key_sub_icon_left_vertical_adjustment);
        mKeySymSubIconGapY = r.getDimensionPixelSize(R.dimen.key_sub_icon_gap_y_adjustment);
        mKeySymSubIconQwertyGapY = r.getDimensionPixelSize(R.dimen.key_sub_icon_qwerty_gap_y_adjustment);
        mKeyFunctionSubIconGapX = r.getDimensionPixelSize(R.dimen.key_function_sub_icon_left_adjustment);
        mKeyModeSubIconGapX = r.getDimensionPixelSize(R.dimen.key_sub_icon_modekey_gap_x_adjustment);
        mKeyModeSubIconVerticalGapX = r.getDimensionPixelSize(R.dimen.key_sub_icon_left_adjustment);
        mKeyModeSubIconGapY = r.getDimensionPixelSize(R.dimen.key_sub_icon_gap_y_adjustment);
        mKeyModeSubIconQwertyGapY = r.getDimensionPixelSize(R.dimen.key_sub_icon_qwerty_gap_y_adjustment);
        mKeyLeftSubIconGapX = r.getDimensionPixelSize(R.dimen.key_sub_icon_keyleft_gap_x_adjustment);
        mKeyLeftSubIconGapY = r.getDimensionPixelSize(R.dimen.key_sub_icon_keyleft_gap_y_adjustment);

        mOneHanded10KeyCenterTopGapY = r.getDimensionPixelSize(R.dimen.paint_onehanded_10key_center_top_gap);
        mOneHanded10KeyLeftGapX = r.getDimensionPixelSize(R.dimen.paint_onehanded_10key_left_gap);
        mOneHanded10KeyRightGapX = r.getDimensionPixelSize(R.dimen.paint_onehanded_10key_right_gap);
        mOneHanded10KeyCenterBottomGapY = r.getDimensionPixelSize(R.dimen.paint_onehanded_10key_center_bottom_gap);
        mOneHanded10KeySymKeyTopVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_onehanded_12key_symkey_top_vertical_gap);
        mOneHandedQwertyPeriodKutoutenLeftVerticalGapX = r.getDimensionPixelSize(R.dimen.paint_onehanded_qwerty_period_kutouten_left_vertical_gap);
        mOneHandedQwertyEmojiPeriodJaPortLeftAdjustGapX = r.getDimensionPixelSize(R.dimen.paint_onehanded_qwerty_period_emoji_ja_v_left_adjust);
        mOneHandedQwertyEmojiPeriodEnPortLeftAdjustGapX = r.getDimensionPixelSize(R.dimen.paint_onehanded_qwerty_period_emoji_en_v_left_adjust);
        mOneHandedQwertyPeriodCommaLeftVerticalGapX = r.getDimensionPixelSize(R.dimen.paint_onehanded_qwerty_period_comma_left_vertical_gap);
        mOneHandedQwertyPeriodJaPortLeftAdjustGapX = r.getDimensionPixelSize(R.dimen.paint_onehanded_qwerty_period_ja_v_left_adjust);
        mOneHandedQwertyPeriodEnPortLeftAdjustGapX = r.getDimensionPixelSize(R.dimen.paint_onehanded_qwerty_period_en_v_left_adjust);
        mOneHandedQwertyPeriodCommaTopVerticalGapY = r.getDimensionPixelSize(R.dimen.paint_onehanded_qwerty_period_comma_top_vertical_gap);
        mOneHandedQwertyNumSymbolTopGapY = r.getDimensionPixelSize(R.dimen.paint_onehanded_qwerty_num_symbol_top_gap);
        mOneHandedQwertyNumSymbolRightGapX = r.getDimensionPixelSize(R.dimen.paint_onehanded_qwerty_num_symbol_right_gap);

        mFloatingDakutenKeyBottomGapY = r.getDimensionPixelSize(R.dimen.paint_floating_dakutenkey_bottom_gap);
        mFloatingQwertyPeriodPortLeftAdjustGapX = r.getDimensionPixelSize(R.dimen.paint_floating_qwerty_period_v_left_adjust);
    }

    /** @see android.graphics.drawable.Drawable#draw10key */
    private void draw10key(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;
        FontMetrics fontMetrics = null;

        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();

        if (!isFloatingShown) {
            if (KEY_ID_ALPHA > mId || (KEY_ID_KATAKANA <= mId && mId < KEY_ID_NUM_HALF)) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_large_top);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_left_right);
                mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_left_right);
            } else if ( (KEY_ID_ALPHA <= mId && mId < KEY_ID_ALPHA_LOWER)
                        || mId == KEY_ID_ALPHA_MAIL || mId == KEY_ID_ALPHA_URL) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_middle_top);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_left_right);
                mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_left_right);
            } else if (KEY_ID_ALPHA_LOWER <= mId && KEY_ID_KATAKANA > mId) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_middle_top);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_left_right);
                mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_left_right);
            }
        } else {
            if (KEY_ID_ALPHA > mId || (KEY_ID_KATAKANA <= mId && mId < KEY_ID_NUM_HALF)) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_large_top);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_left_right);
                mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_left_right);
            } else if ( (KEY_ID_ALPHA <= mId && mId < KEY_ID_ALPHA_LOWER)
                        || mId == KEY_ID_ALPHA_MAIL || mId == KEY_ID_ALPHA_URL) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_middle_top);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_left_right);
                mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_left_right);
            } else if (KEY_ID_ALPHA_LOWER <= mId && KEY_ID_KATAKANA > mId) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_middle_top);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_left_right);
                mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_left_right);
            }

        }
        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard) OpenWnn
                .getCurrentIme().mInputViewManager;
        boolean isPressed = keyboard.getKeyPressed(mId);

        if (isPressed) {
            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                    R.color.key_text_color_main_pressed));
            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                    R.color.key_text_color_sub_pressed));
            mPaintTextBlock03.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                    R.color.key_text_color_sub_pressed));
        } else {
            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                    R.color.key_10key_text_color_top));
            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                    R.color.key_10key_text_color_left_right));
            mPaintTextBlock03.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                    R.color.key_10key_text_color_left_right));
        }
        mPaintTextBlock01.setTextAlign(Align.CENTER);
        mPaintTextBlock01.setTextSize(mTextBlock01Size);

        mPaintTextBlock02.setTextSize(mTextBlock02Size);

        if (mVerticalDisp && (KEY_ID_ALPHA <= mId)) {
            mPaintTextBlock02.setTextAlign(Align.CENTER);
        } else {
            mPaintTextBlock02.setTextAlign(Align.LEFT);
        }

        mPaintTextBlock03.setTextAlign(Align.RIGHT);
        mPaintTextBlock03.setTextSize(mTextBlock03Size);

        // Drawing to the Top position.
        if (TEXTBLOCK_01[mId - 1] != null) {
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            positionX = 0;

            if (mVerticalDisp) {
                if (!isFloatingShown) {
                    if (mId < KEY_ID_ALPHA) {
                        positionY = (float) (Math.abs(fontMetrics.ascent) - (float) m12KeyHeightVertical / 2 + m10KeyCenterTopVerticalGapY);
                    } else {
                        positionY = (float) (Math.abs(fontMetrics.ascent) - (float) m12KeyHeightVertical / 2 + m10KeyEisukanaCenterTopVerticalGapY);
                    }
                } else {
                    positionY = (float) (Math.abs(fontMetrics.ascent) - (float) m12KeyHeightVertical / 2 + mOneHanded10KeyCenterTopGapY);
                }
            } else {
                if (mId < KEY_ID_ALPHA) {
                    positionY = (float) (Math.abs(fontMetrics.ascent) - (float) (m12KeyHeightHorizontal) / 2 + m10KeyCenterTopGapY);
                } else {
                    positionY = (float) (Math.abs(fontMetrics.ascent) - (float) (m12KeyHeightHorizontal) / 2 + m10KeyEisukanaCenterTopHorizontalGapY);
                }
            }

            canvas.drawText(TEXTBLOCK_01[mId - 1], positionX, positionY, mPaintTextBlock01);
        }

        // Drawing to the Center or Left position.
        if (TEXTBLOCK_02[mId - 1] != null) {
            fontMetrics = mPaintTextBlock02.getFontMetrics();

            if((KEY_ID_ALPHA <= mId && mId < KEY_ID_KATAKANA) || mId == KEY_ID_ALPHA_MAIL || mId == KEY_ID_ALPHA_URL){
                if(!mVerticalDisp){
                    mPaintTextBlock02.setTextAlign(Align.CENTER);
                }
            }

            if(mVerticalDisp){
                if (!isFloatingShown) {
                    if (mId < KEY_ID_ALPHA) {
                        positionY = positionY + (float) (Math.abs(fontMetrics.ascent) + m10KeyCenterBottomVerticalGapY);
                    } else {
                        positionY = positionY + (float) (Math.abs(fontMetrics.ascent) + m10KeyEisukanaCenterBottomVerticalGapY);
                    }
                } else {
                    positionY = positionY + (float) (Math.abs(fontMetrics.ascent) + mOneHanded10KeyCenterBottomGapY);
                }
            } else {
                if (mId < KEY_ID_ALPHA) {
                    positionY = positionY + (float) (Math.abs(fontMetrics.ascent) + m10KeyCenterBottomHorizontalGapY);
                } else {
                    positionY = positionY + (float) (Math.abs(fontMetrics.ascent) + m10KeyCenterBottomGapY);
                }
            }

            if (Align.CENTER == mPaintTextBlock02.getTextAlign()) {
                positionX = 0;
                canvas.drawText(TEXTBLOCK_02[mId - 1], positionX, positionY, mPaintTextBlock02);
            } else {
                fontMetrics = mPaintTextBlock01.getFontMetrics();
                if(mVerticalDisp){
                    if (!isFloatingShown) {
                        if(!mOneTouchEmojiShown) {
                            positionX = (float)(m10KeyLeftVerticalGapX - mKeyWidth / 2 );
                        } else {
                            positionX = (float)(m10KeyLeftVerticalGapEmojiX - mKeyWidth / 2 );
                        }
                    } else {
                        positionX = (float)(mOneHanded10KeyLeftGapX - mKeyWidth / 2);
                    }
                }else{
                    positionX = (float)(m10KeyLeftGapX - mKeyWidth / 2);
                }
                canvas.drawText(TEXTBLOCK_02[mId - 1], positionX, positionY, mPaintTextBlock02);
            }
        }

        // Drawing to the Top or Right position.
        if (TEXTBLOCK_03[mId - 1] != null) {
            if(mVerticalDisp){
                if (!isFloatingShown) {
                    if(!mOneTouchEmojiShown){
                        positionX = (float)( mKeyWidth / 2 - m10KeyRightVerticalGapX );
                    } else {
                        positionX = (float)( mKeyWidth / 2 - m10KeyRightVerticalGapEmojiX);
                    }
                } else {
                    positionX = (float)( mKeyWidth / 2 - mOneHanded10KeyRightGapX );
                }
            }else{
                positionX = (float)(mKeyWidth / 2 - m10KeyRightGapX );
            }
            canvas.drawText(TEXTBLOCK_03[mId - 1], positionX, positionY, mPaintTextBlock03);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawNumkey */
    private void drawNumkey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;
        FontMetrics fontMetrics = null;

        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();

        //Font Size
        if (KEY_ID_PHONE_STAR == mId) {
            if (!isFloatingShown) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_phone_star);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_phone_star);
            } else {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_phone_star);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_phone_star);
            }
        } else if (KEY_ID_NUM_HALF <= mId && mId < KEY_ID_SYM) {
            if (!isFloatingShown) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_large_top);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_left_right);
            } else {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_large_top);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_left_right);
            }
        } else if (KEY_ID_PHONE_NUM <= mId && mId < KEY_ID_PHONE_SYM) {
            if (!isFloatingShown) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_phone_large_top);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_phone_left_right);
            } else {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_large_top);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_floating_10key_text_size_left_right);
            }
        }

        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock02.setTextSize(mTextBlock02Size);
        mPaintTextBlock01.setTextAlign(Align.CENTER);
        mPaintTextBlock02.setTextAlign(Align.CENTER);

        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard) OpenWnn
                .getCurrentIme().mInputViewManager;
        boolean isPressed = keyboard.getKeyPressed(mId);

        //Font Color
        if (KEY_ID_NUM_HALF <= mId && mId < KEY_ID_SYM) {
            if (isPressed) {
                mPaintTextBlock01.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(),
                        R.color.key_text_color_main_pressed));
                mPaintTextBlock02.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(),
                        R.color.key_text_color_sub_pressed));
            } else {
                mPaintTextBlock01.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(),
                        R.color.key_10key_text_size_large_top));
                mPaintTextBlock02.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(),
                        R.color.key_10key_text_size_left_right));
            }
        } else if (KEY_ID_PHONE_NUM <= mId && mId < KEY_ID_PHONE_SYM) {
            if (isPressed) {
                mPaintTextBlock01.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(),
                        R.color.key_text_color_main_pressed));
                mPaintTextBlock02.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(),
                        R.color.key_text_color_sub_pressed));
            } else {
                mPaintTextBlock01.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(),
                        R.color.key_10key_text_phone_large_top_color));
                mPaintTextBlock02.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(),
                        R.color.key_10key_text_phone_left_right_color));
            }

        }
        //Gap
        int topGapY = 0;
        int bottomGapY = 0;
        if ((KEY_ID_NUM_HALF <= mId && mId < KEY_ID_SYM)
                || (KEY_ID_PHONE_STAR < mId && mId < KEY_ID_PHONE_SYM)){
            if (mVerticalDisp) {
                topGapY = m10KeyCenterTopVerticalGapY;
                bottomGapY = m10KeyCenterBottomVerticalGapY;
            } else {
                topGapY = m10KeyCenterTopGapY;
                bottomGapY = m10KeyCenterBottomGapY;
            }
        } else if (KEY_ID_PHONE_NUM <= mId && mId <= KEY_ID_PHONE_STAR) {
            if (mVerticalDisp) {
                topGapY = m10KeyPhoneCenterTopVerticalGapY;
                bottomGapY = m10KeyPhoneCenterBottomVerticalGapY;
            } else {
                topGapY = m10KeyPhoneCenterTopGapY;
                bottomGapY = m10KeyPhoneCenterBottomGapY;
            }
        }

        // Drawing to the Top position.
        if (TEXTBLOCK_01[mId - 1] != null) {
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            positionX = 0;
            if (mVerticalDisp) {
                if (!isFloatingShown) {
                    positionY = (float) (Math.abs(fontMetrics.ascent) - (float) m12KeyHeightVertical / 2 + topGapY);
                } else {
                    positionY = (float) (Math.abs(fontMetrics.ascent) - (float) m12KeyHeightVertical / 2 + mOneHanded10KeyCenterTopGapY);
                }
            } else {
                positionY = (float) (Math.abs(fontMetrics.top + fontMetrics.bottom) - (float) (m12KeyHeightHorizontal) / 2 + topGapY);
            }

            canvas.drawText(TEXTBLOCK_01[mId - 1], positionX, positionY, mPaintTextBlock01);
        }

        // Drawing to the Center or Left position.
        if (TEXTBLOCK_02[mId - 1] != null) {
            fontMetrics = mPaintTextBlock02.getFontMetrics();
            positionX = 0;
            if(mVerticalDisp){
                if (!isFloatingShown) {
                    positionY = positionY + (float) (Math.abs(fontMetrics.ascent) + bottomGapY);
                } else {
                    positionY = positionY + (float) (Math.abs(fontMetrics.ascent) + mOneHanded10KeyCenterBottomGapY);
                }
            } else {
                positionY = positionY + (float) (Math.abs(fontMetrics.ascent) + bottomGapY);
            }

            canvas.drawText(TEXTBLOCK_02[mId - 1], positionX, positionY, mPaintTextBlock02);
        }
    }

     /** @see android.graphics.drawable.Drawable#drawQwertykey */
     private void drawQwertykey(Resources r, Canvas canvas) {

         float positionY = 0;
         float positionX = 0;
         float fontWidth = 0;
         FontMetrics fontMetrics = null;
         mPaintTextBlock01.setTextAlign(Align.CENTER);

         setOneHandedShown();
         boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();

         if (!TextUtils.isEmpty(mLabel)) {
             DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
	         boolean isPhone = (keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_JA_HALF_PHONE);
	         boolean isNumeric = ((keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_JA_NUMERIC)
                 || keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_JA_NUMERIC_DECIMAL
                 || keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_JA_NUMERIC_SIGNED);
             if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_QWERTY) {
                 mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_qwerty_num_symbol_text_size_preview);
                 mPaintTextBlock01.setTextSize(mTextBlock01Size);
                 mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_preview_center_text_color));
                 fontMetrics = mPaintTextBlock01.getFontMetrics();
                 positionX = 0;
                 positionY = (float) (-r.getDimensionPixelSize(R.dimen.key_qwerty_preview_height)
                         + Math.abs(fontMetrics.descent + fontMetrics.ascent)
                         + r.getDimensionPixelSize(R.dimen.key_qwerty_num_symbol_preview_top_gap));
             } else {
                 mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_popup);
                 mPaintTextBlock01.setTextSize(mTextBlock01Size);
                 mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_preview_center_text_color));
                 fontMetrics = mPaintTextBlock01.getFontMetrics();
                 positionX = 0;
                 if (isPhone || isNumeric) {
                     positionY = - (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)) - (float)mKeyHeight
                             + r.getDimensionPixelSize(R.dimen.key_preview_text_padding_vgap) *7 /4;
                 } else {
                     positionY = - (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)) - (float)mKeyHeight
                             + r.getDimensionPixelSize(R.dimen.key_preview_text_padding_vgap);
                 }
             }
             canvas.drawText(mLabel.toString(), positionX, positionY, mPaintTextBlock01);
         } else if (TEXTBLOCK_01[mId - 1] != null) {
             if (mOneHandedShown) {
                 mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_onehanded_qwerty_num_symbol_text_size);
             } else if (isFloatingShown){
                 mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_floating_qwerty_num_symbol_text_size);
             } else {
                 mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_qwerty_num_symbol_text_size);
             }
             mPaintTextBlock01.setTextSize(mTextBlock01Size);
             DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
             boolean isPressed = keyboard.getKeyPressed(mId);

             if (isPressed) {
                 mPaintTextBlock01.setColor(mKeySkin.getColor(
                         OpenWnn.getContext(),
                         R.color.key_text_color_sub_pressed));
             } else {
                 mPaintTextBlock01.setColor(mKeySkin.getColor(
                         OpenWnn.getContext(),
                         R.color.key_qwerty_num_symbol_text_color));
             }
             fontMetrics = mPaintTextBlock01.getFontMetrics();
             fontWidth = mPaintTextBlock01.measureText(TEXTBLOCK_01[mId - 1]);
             if (!(mOneHandedShown || isFloatingShown)) {
                 positionX = mKeyWidth / 2- fontWidth - mQwertyNumSymbolRightGapX;
                 positionY = - (mKeyHeight / 2) + (float)(Math.abs(fontMetrics.descent) + Math.abs(fontMetrics.ascent)) + mQwertyNumSymbolTopGapY;
             } else {
                 positionX = mKeyWidth / 2- fontWidth - mOneHandedQwertyNumSymbolRightGapX;
                 positionY = - (mKeyHeight / 2) + (float)(Math.abs(fontMetrics.descent) + Math.abs(fontMetrics.ascent)) + mOneHandedQwertyNumSymbolTopGapY;
             }
             canvas.drawText(TEXTBLOCK_01[mId - 1], positionX, positionY, mPaintTextBlock01);
         }
     }

    /** @see android.graphics.drawable.Drawable#drawKeyModekey */
    private void drawKeyModekey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;
        float positionLeftY = 0;
        float positionLeftX = 0;
        float positionRightY = 0;
        float positionRightX = 0;

        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        Drawable icon = null;
        int themeQwertyGapY = 0;
        FontMetrics fontMetrics = null;
        Drawable slashIcon = null;

        float textBlockSize01;
        float textBlockSize02;
        float textBlockSize03;

        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        setOneHandedShown();
        setOneTouchEmojiShown();
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();

        if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
            if (isFloatingShown) {
                textBlockSize01 = r.getDimensionPixelSize(R.dimen.key_floating_mode_on_text_min_size);
                textBlockSize02 = r.getDimensionPixelSize(R.dimen.key_floating_mode_jp_on_text_min_size);
                textBlockSize03 = r.getDimensionPixelSize(R.dimen.key_onehanded_mode_on_text_size);
            }
            else if (mVerticalDisp) {
                if (!mOneHandedShown) {
                    textBlockSize01 = r.getDimensionPixelSize(R.dimen.key_mode_on_text_small_size);
                    textBlockSize02 = r.getDimensionPixelSize(R.dimen.key_mode_jp_on_text_small_size);
                    textBlockSize03 = r.getDimensionPixelSize(R.dimen.key_mode_on_text_size);
                } else {
                    textBlockSize01 = r.getDimensionPixelSize(R.dimen.key_mode_on_text_small_size);
                    textBlockSize02 = r.getDimensionPixelSize(R.dimen.key_mode_jp_on_text_small_size);
                    textBlockSize03 = r.getDimensionPixelSize(R.dimen.key_onehanded_mode_on_text_size);
                }
                themeQwertyGapY = (int)mQwertyKeyHeightDeviation;
            } else {
                textBlockSize01 = r.getDimensionPixelSize(R.dimen.key_mode_on_text_small_size);
                textBlockSize02 = r.getDimensionPixelSize(R.dimen.key_mode_jp_on_text_small_size);
                textBlockSize03 = r.getDimensionPixelSize(R.dimen.key_mode_on_text_size);
                if (KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY
                                .endsWith(mKeySkin.getPackageName())) {
                    themeQwertyGapY = (int)mQwertyKeyHeightDeviation;
                }
            }
        } else {
            if (isFloatingShown) {
                textBlockSize01 = r.getDimensionPixelSize(R.dimen.key_floating_mode_on_text_small_size);
                textBlockSize02 = r.getDimensionPixelSize(R.dimen.key_floating_mode_jp_on_text_small_size);
                textBlockSize03 = r.getDimensionPixelSize(R.dimen.key_onehanded_mode_qwert_on_text_size);
            } else if (mVerticalDisp) {
                if (!mOneHandedShown ) {
                    if (!mOneTouchEmojiShown) {
                        textBlockSize01 = r.getDimensionPixelSize(R.dimen.key_mode_small_qwert_on_text_size);
                        textBlockSize02 = r.getDimensionPixelSize(R.dimen.key_mode_small_qwert_jp_on_text_size);
                        textBlockSize03 = r.getDimensionPixelSize(R.dimen.key_mode_qwert_on_text_size);
                    } else {
                        textBlockSize01 = r.getDimensionPixelSize(R.dimen.key_mode_small_qwert_on_text_size);
                        textBlockSize02 = r.getDimensionPixelSize(R.dimen.key_mode_small_qwert_jp_on_text_size);
                        textBlockSize03 = r.getDimensionPixelSize(R.dimen.key_mode_small_qwert_on_text_size);
                    }
                } else {
                    textBlockSize01 = r.getDimensionPixelSize(R.dimen.key_onehanded_mode_on_text_small_size);
                    textBlockSize02 = r.getDimensionPixelSize(R.dimen.key_onehanded_mode_jp_on_text_small_size);
                    textBlockSize03 = r.getDimensionPixelSize(R.dimen.key_onehanded_mode_qwert_on_text_size);
                }
            } else {
                textBlockSize01 = r.getDimensionPixelSize(R.dimen.key_mode_jp_on_text_small_size);
                textBlockSize02 = r.getDimensionPixelSize(R.dimen.key_mode_on_text_small_size);
                textBlockSize03 = r.getDimensionPixelSize(R.dimen.key_mode_qwert_on_text_size);
            }
            themeQwertyGapY = (int)mQwertyKeyHeightDeviation;
        }

        if(!mKeySkin.isValid()){
            if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
                icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_globe);
            } else {
                if (mOneTouchEmojiShown && mVerticalDisp) {
                    icon = r.getDrawable(R.drawable.ime_keypad_icon_globe_small);
                } else {
                    icon = r.getDrawable(R.drawable.ime_keypad_icon_globe);
                }
            }
        }else{
            if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
                icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_jp_globe);
            } else {
                if (mOneTouchEmojiShown && mVerticalDisp) {
                    icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_globe_small);
                } else {
                    icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_globe);
                }
            }
        }

        //subIcon display fine adjustment
        int subIconGapX = 0;
        int subIconGapY = 0;
        if(mVerticalDisp){
            if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_QWERTY) {
                subIconGapX = (int)mKeyModeSubIconVerticalGapX;
            }
            subIconGapY = themeQwertyGapY;
        } else {
            if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
                subIconGapY = (int)mKeyModeSubIconGapY;
            } else {
                subIconGapX = (int)mKeyModeSubIconGapX;
                subIconGapY = (int)mKeyModeSubIconQwertyGapY;
            }
        }
        float scaleRatio = getScaleRatio();
        int drawableSubIconWidth = (int)(icon.getIntrinsicWidth() * scaleRatio);
        int drawableSubIconHeight = (int)(icon.getIntrinsicHeight() * scaleRatio);
        final int drawableX = - (drawableSubIconWidth - mKeyWidth / 2) - subIconGapX;
        final int drawableY = - (mKeyHeight / 2) + subIconGapY;

        canvas.translate(drawableX, drawableY);
        icon.setBounds(0, 0, drawableSubIconWidth, drawableSubIconHeight);
        icon.draw(canvas);
        canvas.translate(-drawableX, -drawableY);

        TextPaint paintTextBlockMode = new TextPaint();
        setTextShadow(paintTextBlockMode, R.color.key_text_shadow_color);

        positionX = 0;
        if (!(mOneHandedShown || isFloatingShown)) {
            if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
                positionY = (float) (textBlockSize03 - mKeyHeight / 2
                        + OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_mode_12key_moji_text_top));
            } else {
                paintTextBlockMode.setTextSize(textBlockSize03);
                fontMetrics = paintTextBlockMode.getFontMetrics();
                positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);
            }
        } else {
            if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
                positionY = (float)(textBlockSize03 - mKeyHeight / 2 +
                    OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_onehanded_mode_moji_text_top));
            } else {
                paintTextBlockMode.setTextSize(textBlockSize03);
                fontMetrics = paintTextBlockMode.getFontMetrics();
                positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);
            }
        }

        if ( mId == KEY_ID_MODE_NUM
          || mId == KEY_ID_MODE_NUM_FULL) {
            if (TEXTBLOCK_01[mId - 1] != null) {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_2nd));
                paintTextBlockMode.setTextAlign(Align.CENTER);
                paintTextBlockMode.setTextSize(textBlockSize03);
                paintTextBlockMode.setAntiAlias(true);

                canvas.drawText(TEXTBLOCK_01[mId - 1], positionX, positionY, paintTextBlockMode);
            }
            return;
        }

        if ( mId == KEY_ID_MODE_ALPHA_FULL
          || mId == KEY_ID_MODE_KANA_HALF
          || mId == KEY_ID_MODE_KANA_FULL) {
            if (TEXTBLOCK_02[mId - 1] != null) {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_2nd));
                paintTextBlockMode.setTextAlign(Align.CENTER);
                paintTextBlockMode.setTextSize(textBlockSize03);
                paintTextBlockMode.setAntiAlias(true);

                canvas.drawText(TEXTBLOCK_02[mId - 1], positionX, positionY, paintTextBlockMode);
            }
            return;
        }

        int enableCount = 0;
        if (keyboard.isEnableInputJa()) {enableCount++;}
        if (keyboard.isEnableInputEn()) {enableCount++;}
        if (keyboard.isEnableInputKo()) {enableCount++;}
        if ((enableCount == 3) || (enableCount == 1)) {
            if (TEXTBLOCK_02[mId - 1] != null) {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_2nd));
                paintTextBlockMode.setTextAlign(Align.CENTER);
                paintTextBlockMode.setTextSize(textBlockSize03);
                paintTextBlockMode.setAntiAlias(true);

                canvas.drawText(TEXTBLOCK_02[mId - 1], positionX, positionY, paintTextBlockMode);
            }
            return;
        }

        // if enableCount is 2 ....

        setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);
        setTextShadow(mPaintTextBlock02, R.color.key_text_shadow_color);

        slashIcon = r.getDrawable(R.drawable.ime_keypad_icon_slash);
        int drawableSlashX =  - (slashIcon.getIntrinsicWidth()) / 2;
        int drawableSlashY = - (slashIcon.getIntrinsicHeight()) / 2;

        if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_QWERTY) {
            if ((mVerticalDisp) && (!mOneTouchEmojiShown)) {
                if (isFloatingShown) {
                    drawableSlashX = drawableSlashX - (((mKeyWidth / 2) + drawableSlashX) * 3 / 13);
                }
            }
        }

        canvas.translate(drawableSlashX, drawableSlashY);
        slashIcon.setBounds(0, 0, slashIcon.getIntrinsicWidth(), slashIcon.getIntrinsicHeight());
        slashIcon.draw(canvas);
        canvas.translate(-drawableSlashX, -drawableSlashY);

        float textBlockLeftSize;
        if (keyboard.isEnableInputEn()) {
            textBlockLeftSize = textBlockSize01;
        } else {
            textBlockLeftSize = textBlockSize02;
        }

        float textBlockRightSize;
        if (keyboard.isEnableInputKo()) {
            textBlockRightSize = textBlockSize02;
        } else {
            textBlockRightSize = textBlockSize01;
        }

        if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
            if (isFloatingShown) {
                positionLeftX = drawableSlashX
                        + r.getDimensionPixelSize(R.dimen.key_floating_mode_12key_jp_left);
                positionLeftY = - (mKeyHeight / 2) + textBlockLeftSize
                        + r.getDimensionPixelSize(R.dimen.key_floating_mode_12key_jp_top);
                positionRightX = drawableSlashX + slashIcon.getIntrinsicWidth()
                        + r.getDimensionPixelSize(R.dimen.key_floating_mode_12key_alphabet_left);
                positionRightY = - (mKeyHeight / 2) + textBlockRightSize
                        + r.getDimensionPixelSize(R.dimen.key_floating_mode_12key_alphabet_top);
            } else if ((mVerticalDisp) && (!mOneTouchEmojiShown)) {
                positionLeftX = drawableSlashX
                        + r.getDimensionPixelSize(R.dimen.key_mode_12key_jp_left);
                positionLeftY = - (mKeyHeight / 2) + textBlockLeftSize
                        + r.getDimensionPixelSize(R.dimen.key_mode_12key_jp_top);
                positionRightX = drawableSlashX
                        + r.getDimensionPixelSize(R.dimen.key_mode_12key_alphabet_left);
                positionRightY = - (mKeyHeight / 2) + textBlockRightSize
                        + r.getDimensionPixelSize(R.dimen.key_mode_12key_alphabet_top);
            } else {
                positionLeftX = drawableSlashX
                                + r.getDimensionPixelSize(R.dimen.key_mode_12key_jp_left);
                positionLeftY = - (mKeyHeight / 2) + textBlockLeftSize
                                + r.getDimensionPixelSize(R.dimen.key_mode_12key_jp_top);
                positionRightX = drawableSlashX + slashIcon.getIntrinsicWidth()
                                + r.getDimensionPixelSize(R.dimen.key_mode_12key_alphabet_left);
                positionRightY = - (mKeyHeight / 2) + textBlockRightSize
                                + r.getDimensionPixelSize(R.dimen.key_mode_12key_alphabet_top);
            }
        } else {
            if (isFloatingShown) {
                positionLeftX = drawableSlashX
                        + r.getDimensionPixelSize(R.dimen.key_floating_mode_qwerty_jp_left);
                positionLeftY = - (mKeyHeight / 2) + textBlockLeftSize
                        + r.getDimensionPixelSize(R.dimen.key_floating_mode_qwerty_jp_top);
                positionRightX = drawableSlashX + slashIcon.getIntrinsicWidth()
                        + r.getDimensionPixelSize(R.dimen.key_floating_mode_qwerty_alphabet_left);
                positionRightY = - (mKeyHeight / 2) + textBlockRightSize
                        + r.getDimensionPixelSize(R.dimen.key_floating_mode_qwerty_alphabet_top);
            } else if ((mVerticalDisp) && (!mOneTouchEmojiShown)) {
                positionLeftX = drawableSlashX
                        + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_jp_left);
                positionLeftY = - (mKeyHeight / 2) + textBlockLeftSize
                        + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_jp_top);
                positionRightX = drawableSlashX + slashIcon.getIntrinsicWidth()
                        + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_alphabet_left);
                positionRightY = - (mKeyHeight / 2) + textBlockRightSize
                        + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_alphabet_top);
            } else {
                positionLeftX = drawableSlashX
                                + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_jp_left);
                positionLeftY = - (mKeyHeight / 2) + textBlockLeftSize
                                + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_jp_top);
                positionRightX = drawableSlashX + slashIcon.getIntrinsicWidth()
                                + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_alphabet_left);
                positionRightY = - (mKeyHeight / 2) + textBlockRightSize
                                + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_alphabet_top);
            }
        }

        // draw text in upper left corner

        if (keyboard.isEnableInputJa()) {
            if (TEXTBLOCK_02[KEY_ID_MODE - 1] != null) {
                if (mId == KEY_ID_MODE) {
                    mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                                                        R.color.key_10key_text_color_mode));
                } else {
                    mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                                                        R.color.key_text_color_2nd));
                }
                mPaintTextBlock01.setTextAlign(Align.LEFT);
                mPaintTextBlock01.setTextSize(textBlockSize02);
                mPaintTextBlock01.setAntiAlias(true);

                canvas.drawText(TEXTBLOCK_02[KEY_ID_MODE - 1], positionLeftX, positionLeftY,
                                mPaintTextBlock01);
            }
        }
        if (!keyboard.isEnableInputJa() && keyboard.isEnableInputEn()) {
            if (TEXTBLOCK_02[KEY_ID_MODE_ALPHA - 1] != null) {
                if (mId == KEY_ID_MODE_ALPHA) {
                    mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                                                        R.color.key_10key_text_color_mode));
                } else {
                    mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                                                        R.color.key_text_color_2nd));
                }
                mPaintTextBlock01.setTextAlign(Align.LEFT);
                mPaintTextBlock01.setTextSize(textBlockSize01);
                mPaintTextBlock01.setAntiAlias(true);

                canvas.drawText(TEXTBLOCK_02[KEY_ID_MODE_ALPHA - 1], positionLeftX, positionLeftY,
                                mPaintTextBlock01);
            }
         }

        // draw text in lower right corner

        if (keyboard.isEnableInputKo()) {
            if (TEXTBLOCK_01[KEY_ID_QWERTY_KO_KEYMODE - 1] != null) {
                mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                                                        R.color.key_text_color_2nd));
                mPaintTextBlock02.setTextAlign(Align.LEFT);
                mPaintTextBlock02.setTextSize(textBlockSize02);
                mPaintTextBlock02.setAntiAlias(true);

                canvas.drawText(TEXTBLOCK_01[KEY_ID_QWERTY_KO_KEYMODE - 1],
                                positionRightX, positionRightY, mPaintTextBlock02);
            }
        }
        if (keyboard.isEnableInputJa() && keyboard.isEnableInputEn()) {
            if (TEXTBLOCK_02[KEY_ID_MODE_ALPHA - 1] != null) {
                if (mId == KEY_ID_MODE_ALPHA) {
                    mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                                                        R.color.key_10key_text_color_mode));
                } else {
                    mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                                                        R.color.key_text_color_2nd));
                }
                mPaintTextBlock02.setTextAlign(Align.LEFT);
                mPaintTextBlock02.setTextSize(textBlockSize01);
                mPaintTextBlock02.setAntiAlias(true);

                canvas.drawText(TEXTBLOCK_02[KEY_ID_MODE_ALPHA - 1],
                                positionRightX, positionRightY, mPaintTextBlock02);
            }
         }
    }

    /** @see android.graphics.drawable.Drawable#drawKeyModekeyKo */
    private void drawKeyModekeyKo(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;
        FontMetrics fontMetrics = null;

        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        Drawable icon = null;
        Drawable slashIcon = null;

        float textBlockSize01;
        float textBlockSize02;

        float positionLeftY = 0;
        float positionLeftX = 0;
        float positionRightY = 0;
        float positionRightX = 0;

        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();

        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        setOneTouchEmojiShown();

        if(!mKeySkin.isValid()){
            if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
                icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_globe);
            } else {
                icon = r.getDrawable(R.drawable.ime_keypad_icon_globe_small);
            }
        }else{
            if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
                icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_jp_globe);
            } else {
                icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_globe_small);
            }
        }

        //subIcon display fine adjustment
        int subIconGapX = 0;
        if(mVerticalDisp){
            subIconGapX = (int)mKeyModeSubIconVerticalGapX;
        } else {
            subIconGapX = (int)mKeyModeSubIconGapX;
        }
        float scaleRatio = getScaleRatio();
        int drawableSubIconWidth = (int)(icon.getIntrinsicWidth() * scaleRatio);
        int drawableSubIconHeight = (int)(icon.getIntrinsicHeight() * scaleRatio);
        final int drawableX = - (drawableSubIconWidth - mKeyWidth / 2) - subIconGapX;
        int drawableY = 0;
        if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY) {
            drawableY = - (mKeyHeight / 2);
        } else {
            drawableY = (int)mQwertyKeyHeightDeviation - (mKeyHeight / 2);
        }

        canvas.translate(drawableX, drawableY);
        icon.setBounds(0, 0, drawableSubIconWidth, drawableSubIconHeight);
        icon.draw(canvas);
        canvas.translate(-drawableX, -drawableY);

        positionX = 0;
        positionY = 0;
        mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_mode_on_text_size_korean);
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_2nd));

        setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);

        int enableCount = 0;
        if (keyboard.isEnableInputJa()) {enableCount++;}
        if (keyboard.isEnableInputEn()) {enableCount++;}
        if (keyboard.isEnableInputKo()) {enableCount++;}
        if ((enableCount == 3) || (enableCount == 1)) {
            if (TEXTBLOCK_01[mId - 1] != null) {
                fontMetrics = mPaintTextBlock01.getFontMetrics();
                mPaintTextBlock01.setTextAlign(Align.CENTER);
                positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);
                canvas.drawText(TEXTBLOCK_01[mId - 1], positionX, positionY, mPaintTextBlock01);
            }
            return;
        }

        // if enableCount is 2 ....
        setTextShadow(mPaintTextBlock02, R.color.key_text_shadow_color);

        slashIcon = r.getDrawable(R.drawable.ime_keypad_icon_slash);
        int drawableSlashX =  - (slashIcon.getIntrinsicWidth()) / 2;
        int drawableSlashY = - (slashIcon.getIntrinsicHeight()) / 2;

        if ((mVerticalDisp)) {
            if (isFloatingShown) {
                drawableSlashX = drawableSlashX - (((mKeyWidth / 2) + drawableSlashX) * 3 / 13);
            }
        }

        canvas.translate(drawableSlashX, drawableSlashY);
        slashIcon.setBounds(0, 0, slashIcon.getIntrinsicWidth(), slashIcon.getIntrinsicHeight());
        slashIcon.draw(canvas);
        canvas.translate(-drawableSlashX, -drawableSlashY);

        if (isFloatingShown) {
            textBlockSize01 = r.getDimensionPixelSize(R.dimen.key_floating_mode_on_text_small_size);
            textBlockSize02 = r.getDimensionPixelSize(R.dimen.key_floating_mode_jp_on_text_small_size);
        }else if ((mVerticalDisp) && (!mOneTouchEmojiShown)) {
            if (!(mOneHandedShown || isFloatingShown)) {
                if (!mOneTouchEmojiShown) {
                    textBlockSize01 = r.getDimensionPixelSize(R.dimen.key_mode_small_qwert_on_text_size);
                    textBlockSize02 = r.getDimensionPixelSize(R.dimen.key_mode_small_qwert_jp_on_text_size);
                } else {
                    textBlockSize01 = r.getDimensionPixelSize(R.dimen.key_mode_small_qwert_on_text_size);
                    textBlockSize02 = r.getDimensionPixelSize(R.dimen.key_mode_small_qwert_jp_on_text_size);
                }
            } else {
                textBlockSize01 = r.getDimensionPixelSize(R.dimen.key_onehanded_mode_on_text_small_size);
                textBlockSize02 = r.getDimensionPixelSize(R.dimen.key_onehanded_mode_on_text_small_size);
            }
        } else {
            textBlockSize01 = r.getDimensionPixelSize(R.dimen.key_mode_jp_on_text_small_size);
            textBlockSize02 = r.getDimensionPixelSize(R.dimen.key_mode_on_text_small_size);
        }

        float textBlockLeftSize;
        if (keyboard.isEnableInputEn()) {
            textBlockLeftSize = textBlockSize01;
        } else {
            textBlockLeftSize = textBlockSize02;
        }

        float textBlockRightSize;
        textBlockRightSize = textBlockSize02;

        if (isFloatingShown) {
            positionLeftX = drawableSlashX
                    + r.getDimensionPixelSize(R.dimen.key_floating_mode_qwerty_jp_left);
            positionLeftY = - (mKeyHeight / 2) + textBlockLeftSize
                    + r.getDimensionPixelSize(R.dimen.key_floating_mode_qwerty_jp_top);
            positionRightX = drawableSlashX + slashIcon.getIntrinsicWidth()
                    + r.getDimensionPixelSize(R.dimen.key_floating_mode_qwerty_alphabet_left);
            positionRightY = - (mKeyHeight / 2) + textBlockRightSize
                    + r.getDimensionPixelSize(R.dimen.key_floating_mode_qwerty_alphabet_top);
        } else if ((mVerticalDisp) && (!mOneTouchEmojiShown)) {
            if (!mOneHandedShown ) {
                positionLeftX = drawableSlashX
                        + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_jp_left);
                positionLeftY = - (mKeyHeight / 2) + textBlockLeftSize
                        + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_jp_top);
                positionRightX = drawableSlashX + slashIcon.getIntrinsicWidth()
                        + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_alphabet_left);
                positionRightY = - (mKeyHeight / 2) + textBlockRightSize
                        + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_alphabet_top);
            } else {
                positionLeftX = drawableSlashX
                        + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_jp_left);
                positionLeftY = - (mKeyHeight / 2) + textBlockLeftSize
                        + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_jp_top);
                positionRightX = drawableSlashX + slashIcon.getIntrinsicWidth()
                        + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_alphabet_left);
                positionRightY = - (mKeyHeight / 2) + textBlockRightSize
                        + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_alphabet_top);
            }
        } else {
            positionLeftX = drawableSlashX
                    + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_jp_left);
            positionLeftY = - (mKeyHeight / 2) + textBlockLeftSize
                    + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_jp_top);
            positionRightX = drawableSlashX + slashIcon.getIntrinsicWidth()
                    + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_alphabet_left);
            positionRightY = - (mKeyHeight / 2) + textBlockRightSize
                    + r.getDimensionPixelSize(R.dimen.key_mode_qwerty_alphabet_top);
        }

        // draw text in upper left corner

        if (keyboard.isEnableInputJa()) {
            if (TEXTBLOCK_02[KEY_ID_MODE - 1] != null) {
                mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                                                            R.color.key_text_color_2nd));
                mPaintTextBlock01.setTextAlign(Align.LEFT);
                mPaintTextBlock01.setTextSize(textBlockSize02);
                mPaintTextBlock01.setAntiAlias(true);

                canvas.drawText(TEXTBLOCK_02[KEY_ID_MODE - 1],
                                positionLeftX, positionLeftY, mPaintTextBlock01);
            }
        }
        if (keyboard.isEnableInputEn()) {
            if (TEXTBLOCK_02[KEY_ID_MODE_ALPHA - 1] != null) {
                mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                                                            R.color.key_text_color_2nd));
                mPaintTextBlock01.setTextAlign(Align.LEFT);
                mPaintTextBlock01.setTextSize(textBlockSize01);
                mPaintTextBlock01.setAntiAlias(true);

                canvas.drawText(TEXTBLOCK_02[KEY_ID_MODE_ALPHA - 1],
                                positionLeftX, positionLeftY, mPaintTextBlock01);
            }
         }

        // draw text in lower right corner

        if (TEXTBLOCK_01[mId - 1] != null) {
            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                    R.color.key_10key_text_color_mode));
            mPaintTextBlock02.setTextAlign(Align.LEFT);
            mPaintTextBlock02.setTextSize(textBlockSize02);
            mPaintTextBlock02.setAntiAlias(true);

            canvas.drawText(TEXTBLOCK_01[mId - 1],
                    positionRightX, positionRightY, mPaintTextBlock02);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawChangeLanguagePopkey */
    private void drawChangeLanguagePopkey(Resources r, Canvas canvas) {
        float positionY = 0;
        float positionX = 0;
        FontMetrics fontMetrics = null;
        Drawable icon = null;
        String text = "";

        boolean isPressed = false;
        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        KeyboardView kbv = keyboard.getKeyboardView();
        if(kbv != null){
            isPressed = kbv.isMiniKeyboardPressed(mId);
        }

        if (mId == KEY_ID_CHANGE_LANG_TITLE) {
            mTextBlock01Size = r.getDimensionPixelSize(R.dimen.popup_change_language_text_size);
        } else if (mId == KEY_ID_CHANGE_STYLE_LINE) {
            if (!mKeySkin.isValid()) {
                icon = r.getDrawable(R.drawable.ime_btn_effect_line_ver);
            } else {
                icon = mKeySkin.getDrawable(R.drawable.ime_btn_effect_line_ver);
            }
        } else {
            boolean keyboardStyleIcon = false;
            for (int i = 0; i < KEYBOARD_STYLE_KEY_ID_TABLE.length; i++) {
                if (mId == KEYBOARD_STYLE_KEY_ID_TABLE[i]) {
                    int column = 0;
                    if (isPressed) {
                        column = 1; // key pressed
                    }

                    int checkIndex = 0;
                    if (keyboard.isFloatingMode()) {
                        checkIndex = 3;
                    } else if (mVerticalDisp) {
                        if (keyboard.isOneHandedMode()) {
                            checkIndex = 1;
                        }
                    } else {
                        if (keyboard.isSplitMode()) {
                            checkIndex = 2;
                        }
                    }
                    if (i == checkIndex) {
                        // If the display state of the keyboard and the keyboard style icon
                        //  is the same,add check to the icon.
                        Drawable[] layers = new Drawable[2];
                        if (!mKeySkin.isValid()) {
                            layers[0] = r.getDrawable(KEYBOARD_STYLE_KEY_ICON_TABLE[i][column]);
                            layers[1] = r.getDrawable(KEYBOARD_STYLE_KEY_ICON_CHECK_TABLE[column]);
                        } else {
                            layers[0] = mKeySkin.getDrawable(
                                    KEYBOARD_STYLE_KEY_ICON_TABLE[i][column]);
                            layers[1] = mKeySkin.getDrawable(
                                    KEYBOARD_STYLE_KEY_ICON_CHECK_TABLE[column]);
                        }
                        ((BitmapDrawable)layers[1]).setGravity(
                                Gravity.LEFT | Gravity.CENTER_VERTICAL);
                        int offsetX = (int)(r.getDimension(R.dimen.popup_change_style_key_width)
                                - r.getDimension(
                                        R.dimen.popup_change_style_key_check_horizontal_gap));
                        int offsetY = 0;
                        LayerDrawable layerDrawable = new LayerDrawable(layers);
                        layerDrawable.setLayerInset(1, offsetX, offsetY, -offsetX, -offsetY);
                        icon = layerDrawable;
                    } else {
                        if (!mKeySkin.isValid()) {
                            icon = r.getDrawable(KEYBOARD_STYLE_KEY_ICON_TABLE[i][column]);
                        } else {
                            icon = mKeySkin.getDrawable(KEYBOARD_STYLE_KEY_ICON_TABLE[i][column]);
                        }
                    }
                    keyboardStyleIcon = true;
                    break;
                }
            }
            if (!keyboardStyleIcon) {
                // If is not a keyboard style icon.
                int currentModeId;
                if (keyboard instanceof DefaultSoftKeyboardKorean) {
                    currentModeId = KEY_ID_CHANGE_LANG_KO;
                } else if ((DefaultSoftKeyboard.mCurrentKeyMode
                        == DefaultSoftKeyboard.KEYMODE_JA_FULL_ALPHABET
                         || DefaultSoftKeyboard.mCurrentKeyMode
                         == DefaultSoftKeyboard.KEYMODE_JA_HALF_ALPHABET)) {
                    currentModeId = KEY_ID_CHANGE_LANG_EN;
                } else {
                    currentModeId = KEY_ID_CHANGE_LANG_JA;
                }
                if (mId == currentModeId) {
                    if (isPressed) {
                        if (!mKeySkin.isValid()) {
                            icon = r.getDrawable(R.drawable.ime_keypad_icon_check_pressed);
                        } else {
                            icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_check_pressed);
                        }
                    } else {
                        if (!mKeySkin.isValid()) {
                            icon = r.getDrawable(R.drawable.ime_keypad_icon_check_normal);
                        } else {
                            icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_check_normal);
                        }
                    }
                }
            }

            mTextBlock01Size = r.getDimensionPixelSize(R.dimen.popup_change_language_item_text_size);
        }
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setTypeface(Typeface.DEFAULT);

        if ((mId == KEY_ID_CHANGE_LANG_TITLE) || (mId == KEY_ID_CHANGE_STYLE_TITLE)) {
            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                    R.color.popup_text_color));
        } else {
            if (isPressed) {
                mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                        R.color.popup_text_color_press));
            } else {
                mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                        R.color.popup_text_color));
            }
        }
        fontMetrics = mPaintTextBlock01.getFontMetrics();

        // draw text
        positionX = 0;
        if (mId == KEY_ID_CHANGE_LANG_TITLE) {
            text = r.getString(R.string.ti_preference_lang_setting_keyboard_title_txt);
            positionX = r.getDimensionPixelSize(R.dimen.popup_change_language_x_left_gap_text);
        } else if (mId == KEY_ID_CHANGE_STYLE_TITLE) {
            text = r.getString(R.string.ti_preference_style_setting_keyboard_title_txt);
            positionX = r.getDimensionPixelSize(R.dimen.popup_change_style_x_left_gap_text);
        } else if (mId == KEY_ID_CHANGE_LANG_JA) {
            text = r.getString(R.string.ti_preference_input_language_ja_txt);
        } else if (mId == KEY_ID_CHANGE_LANG_KO) {
            text = r.getString(R.string.ti_preference_input_language_ko_txt);
        } else if (mId == KEY_ID_CHANGE_LANG_EN) {
            text = r.getString(R.string.ti_preference_input_language_en_txt);
        }

        positionY = (mKeyHeight / 2) + (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);
        canvas.drawText(text, positionX, positionY, mPaintTextBlock01);

        // draw icon
        if (icon != null) {
            if ((mId == KEY_ID_CHANGE_STYLE_LINE) ||
                    (mId == KEY_ID_CHANGE_STYLE_NORMAL) ||
                    (mId == KEY_ID_CHANGE_STYLE_ONEHAND) ||
                    (mId == KEY_ID_CHANGE_STYLE_SPLIT) ||
                    (mId == KEY_ID_CHANGE_STYLE_FLOATING)) {
                icon.setBounds(0, 0, mKeyWidth, mKeyHeight);
                icon.draw(canvas);
            } else {
                positionX = r.getDimensionPixelSize(R.dimen.popup_change_language_width)
                        - icon.getIntrinsicWidth()
                        - r.getDimensionPixelSize(
                                R.dimen.popup_view_padding_change_language_right);
                positionY =  (mKeyHeight / 2) +  -icon.getIntrinsicHeight() / 2;
                canvas.translate(positionX, positionY);
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                icon.draw(canvas);
                canvas.translate(-positionX, -positionY);
            }
        }
    }

    /** @see android.graphics.drawable.Drawable#drawKeyModekey */
    private void drawLeftkey(Resources r, Canvas canvas) {
        Drawable subIcon = null;
        Drawable icon = null;

        setOneTouchEmojiShown();
        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        boolean isQwerty = (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_QWERTY);
        boolean isSmall = false;
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();
        if (mOneTouchEmojiShown && mVerticalDisp && isQwerty) {
            isSmall = true;
        }
        if (!mKeySkin.isValid()) {
            if (isQwerty) {
                if (isSmall) {
                    icon = r.getDrawable(R.drawable.ime_keypad_icon_left_small);
                    subIcon = r.getDrawable(R.drawable.ime_keypad_icon_4way_small);
                } else {
                    icon = r.getDrawable(R.drawable.ime_keypad_icon_left);
                    subIcon = r.getDrawable(R.drawable.ime_keypad_icon_4way);
                }
            } else {
                icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_arrow_l);
                subIcon = r.getDrawable(R.drawable.ime_keypad_icon_jp_4way);
            }
        } else {
            if (isQwerty) {
                if (isSmall) {
                    icon = mKeySkin.getDrawable((int)R.drawable.ime_keypad_icon_left_small);
                    subIcon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_4way_small);
                } else {
                    icon = mKeySkin.getDrawable((int)R.drawable.ime_keypad_icon_left);
                    subIcon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_4way);
                }
            } else {
                icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_jp_arrow_l);
                subIcon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_jp_4way);
            }
        }

        //subIcon display fine adjustment
        int subIconGapX = 0;
        if(mVerticalDisp){
            if (isQwerty) {
                subIconGapX = (int)mKeyLeftSubIconGapX;
            }
        }
        float scaleRatio = getScaleRatio();
        int drawableSubIconWidth = (int)(subIcon.getIntrinsicWidth() * scaleRatio);
        int drawableSubIconHeight = (int)(subIcon.getIntrinsicHeight() * scaleRatio);
        int subIconGapY = (int)mKeyLeftSubIconGapY;
        final int drawableSubIconX = - (drawableSubIconWidth - mKeyWidth / 2) - subIconGapX;
        final int drawableSubIconY = - (mKeyHeight / 2) + subIconGapY;
        canvas.translate(drawableSubIconX, drawableSubIconY);
        subIcon.setBounds(0, 0, drawableSubIconWidth, drawableSubIconHeight);
        subIcon.draw(canvas);
        canvas.translate(-drawableSubIconX, -drawableSubIconY);

        if (!mKeySkin.isValid()) {
            icon = modifyIcon(r, icon);
        }

        if (icon != null) {
            int drawableIconX = -icon.getIntrinsicWidth() / 2;
            final int drawableIconY = -icon.getIntrinsicHeight() / 2;
            
            if (isQwerty && isFloatingShown) {
                drawableIconX += r.getDimensionPixelSize(R.dimen.key_floating_qwerty_leftarrow_left);
            }
            
            canvas.translate(drawableIconX, drawableIconY);
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            icon.draw(canvas);
            canvas.translate(-drawableIconX, -drawableIconY);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawKeyModekey */
    private void drawFunctionkey(Resources r, Canvas canvas) {
        Drawable subIcon = null;
        Drawable icon = null;
        int adjustX = 0;
        int adjustY = 0;

        int functionIndex = KeyboardView.FunctionKey_FUNC_VOICEINPUT;
        if(!KeyboardView.mEnableVoiceInput){
            functionIndex = KeyboardView.FunctionKey_FUNC_KBD;
            DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn
                    .getCurrentIme().mInputViewManager;
            if (keyboard instanceof DefaultSoftKeyboardKorean) {
                functionIndex = KeyboardView.FunctionKey_FUNC_HANDWRITING;
            }
        }

        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();

        setOneTouchEmojiShown();
        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        boolean isQwerty = (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_QWERTY);
        boolean isSmall = false;
        if ((mOneTouchEmojiShown || mOneHandedShown || isFloatingShown) && mVerticalDisp && isQwerty) {
            isSmall = true;
        }

        if (!mKeySkin.isValid()) {
            subIcon = r.getDrawable(R.drawable.ime_keypad_btn_more_setting);
            if (isQwerty) {
                if (isSmall) {
                    icon = r.getDrawable(KeyboardView.FUNCTION_KEYTOP_DRAWABLE_TABLE_SMALL[functionIndex]);
                } else {
                    icon = r.getDrawable(KeyboardView.FUNCTION_KEYTOP_DRAWABLE_TABLE[functionIndex]);
                }
            } else {
                icon = r.getDrawable(KeyboardView.FUNCTION_KEYTOP_DRAWABLE_TABLE_JP[functionIndex]);
            }
        } else {
            subIcon = mKeySkin.getDrawable(R.drawable.ime_keypad_btn_more_setting);
            if (isQwerty) {
                if (isSmall) {
                    icon = mKeySkin.getDrawable(KeyboardView.FUNCTION_KEYTOP_DRAWABLE_TABLE_SMALL[functionIndex]);
                } else {
                    icon = mKeySkin.getDrawable(KeyboardView.FUNCTION_KEYTOP_DRAWABLE_TABLE[functionIndex]);
                }
            } else {
                icon = mKeySkin.getDrawable(KeyboardView.FUNCTION_KEYTOP_DRAWABLE_TABLE_JP[functionIndex]);
            }
        }

        adjustX = (int) mKeySubIconGapX;
        float scaleRatio = getScaleRatio();
        int drawableSubIconWidth = (int)(subIcon.getIntrinsicWidth() * scaleRatio);
        int drawableSubIconHeight = (int)(subIcon.getIntrinsicHeight() * scaleRatio);
        final int drawableSubIconX = -(drawableSubIconWidth - mKeyWidth / 2) - adjustX;
        final int drawableSubIconY = - (mKeyHeight / 2);

        canvas.translate(drawableSubIconX, drawableSubIconY);
        subIcon.setBounds(0, 0, drawableSubIconWidth, drawableSubIconHeight);
        subIcon.draw(canvas);
        canvas.translate(-drawableSubIconX, -drawableSubIconY);

        if (icon != null) {
            final int drawableIconX = -icon.getIntrinsicWidth() / 2;
            final int drawableIconY = -icon.getIntrinsicHeight() / 2;

            if (isFloatingShown) {
                adjustY  += OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_floating_fuction_key_v_adjust);
            }
            canvas.translate(drawableIconX, drawableIconY + adjustY);
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            icon.draw(canvas);
            canvas.translate(-drawableIconX, -(drawableIconY + adjustY));
        }
    }

    /** @see android.graphics.drawable.Drawable#drawPreviewKeymodeKeyKo */
    private void drawPreviewKeymodeKeyKo(Resources r, Canvas canvas) {
        float positionY = 0;
        float positionX = 0;
        float offsetY = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_preview_padding_vgap);
        int offsetId = mId - KEY_ID_KEYTOP_PREVIEW_OFFSET;

        FontMetrics fontMetrics = null;

        mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_enter_text_size);
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setColor(mKeySkin.getColor(
                OpenWnn.getContext(), R.color.key_12key_mode_preview_text_color));
        if (!KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY.endsWith(mKeySkin.getPackageName())
                && !KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(
                mKeySkin.getPackageName())) {
            setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);
        }

        if (TEXTBLOCK_01[offsetId - 1] != null) {
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            mPaintTextBlock01.setTextAlign(Align.CENTER);
            positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent) / 2 + offsetY);
            canvas.drawText(TEXTBLOCK_01[offsetId - 1], positionX, positionY, mPaintTextBlock01);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawSymkey */
    private void drawSymkey(Resources r, Canvas canvas) {

        mPaint.setTextAlign(Paint.Align.CENTER);
        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        Drawable icon = null;      // Smile mark (Lower right of key)
        Drawable slashIcon = null; // Slash image (Center of key)
        Drawable subIcon = null;   // Symbol "..." (Upper right of key)
        String numText = null;     // Number text "12?" (Upeer left of key)
        float positionY = 0;
        float positionX = 0;

        boolean isQwerty = (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_QWERTY);
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();

        setOneHandedShown();
        setOneTouchEmojiShown();
        if(!mKeySkin.isValid()){
            if (isQwerty) {
                if (mOneTouchEmojiShown || mOneHandedShown || isFloatingShown) {
                    icon = r.getDrawable(R.drawable.ime_keypad_icon_sym_small);
                    subIcon = r.getDrawable(R.drawable.ime_keypad_icon_input_extention_small);
                } else {
                    icon = r.getDrawable(R.drawable.ime_keypad_icon_sym);
                    subIcon = r.getDrawable(R.drawable.ime_keypad_icon_input_extention);
                }
            } else {
                icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_sym);
                subIcon = r.getDrawable(R.drawable.ime_keypad_icon_jp_input_extention);
            }
            slashIcon = r.getDrawable(R.drawable.ime_keypad_icon_slash);
        }else{
            if (isQwerty) {
                if (mOneTouchEmojiShown || mOneHandedShown || isFloatingShown) {
                    icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_sym_small);
                    subIcon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_input_extention_small);
                } else {
                    icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_sym);
                    subIcon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_input_extention);
                }
            } else {
                icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_jp_sym);
                subIcon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_jp_input_extention);
            }
            slashIcon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_slash);
        }

        //subIcon display fine adjustment
        int subIconGapX = 0;
        int subIconGapY = 0;
        if(mVerticalDisp){
            subIconGapX = (int)mKeySubIconVerticalGapX;
            subIconGapY = (int)mKeySymSubIconGapY;
        } else {
            subIconGapX = (int)mKeySubIconGapX;
            if(isQwerty) {
                subIconGapY = (int)mKeySymSubIconQwertyGapY;
            } else {
                subIconGapY = (int)mKeySymSubIconGapY;
            }
        }

        float scaleRatio = getScaleRatio();
        int drawableSubIconWidth = (int)(subIcon.getIntrinsicWidth() * scaleRatio);
        int drawableSubIconHeight = (int)(subIcon.getIntrinsicHeight() * scaleRatio);
        final int drawableSubIconX = -(drawableSubIconWidth - mKeyWidth / 2)
                - subIconGapX;
        final int drawableSubIconY = - (mKeyHeight / 2) + subIconGapY;

        canvas.translate(drawableSubIconX, drawableSubIconY);
        subIcon.setBounds(0, 0, drawableSubIconWidth, drawableSubIconHeight);
        subIcon.draw(canvas);
        canvas.translate(-drawableSubIconX, -drawableSubIconY);

        int drawableSlashX = ( - slashIcon.getIntrinsicWidth()) / 2;
        int drawableSlashY = - (slashIcon.getIntrinsicHeight()) / 2;

        int drawableX = 0;
        int drawableY = 0;

        switch (keyboard.getKeyMode()) {
        case DefaultSoftKeyboard.KEYMODE_JA_FULL_NUMBER:
            if (TEXTBLOCK_03[mId - 1] != null) {
                numText = TEXTBLOCK_03[mId - 1];
            }
            break;
        default:
            if (TEXTBLOCK_02[mId - 1] != null) {
                numText = TEXTBLOCK_02[mId - 1];
            }
            break;
        }

        if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_QWERTY) {
            if (isFloatingShown) {
                drawableSlashX = drawableSlashX + (((mKeyWidth / 2) + drawableSlashX) * 4 / 15);
                drawableSlashY += r.getDimensionPixelSize(R.dimen.key_floating_sym_qwerty_slash_offset_y);
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_floating_mode_on_text_small_size);
                drawableX =  drawableSlashX  + slashIcon.getIntrinsicWidth()
                        + r.getDimensionPixelSize(R.dimen.key_floating_sym_qwerty_sym_left);
                drawableY =  - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_floating_sym_qwerty_sym_top);
            } else if ((mVerticalDisp) && (!mOneTouchEmojiShown)) {
                if (!mOneHandedShown) {
                    drawableSlashX = drawableSlashX + (((mKeyWidth / 2) + drawableSlashX) * 2 / 12);
                    mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_mode_small_qwert_on_text_size);
                    drawableX =  drawableSlashX  + slashIcon.getIntrinsicWidth()
                                    + r.getDimensionPixelSize(R.dimen.key_sym_qwerty_sym_left);
                    drawableY =  - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_sym_qwerty_sym_top);
                } else {
                    drawableSlashX = drawableSlashX + (((mKeyWidth / 2) + drawableSlashX) * 5 / 15);
                    mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_onehanded_mode_on_text_small_size);
                    drawableX =  drawableSlashX  + slashIcon.getIntrinsicWidth()
                                    + r.getDimensionPixelSize(R.dimen.key_onehanded_sym_qwerty_sym_left);
                    drawableY =  - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_onehanded_sym_qwerty_sym_top);
                }
            } else {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_mode_on_text_small_size);
                drawableX =  drawableSlashX  + slashIcon.getIntrinsicWidth()
                                + r.getDimensionPixelSize(R.dimen.key_sym_qwerty_sym_left_land);
                drawableY =  - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_sym_qwerty_sym_top_land);
            }
        } else {
            if (isFloatingShown) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_floating_mode_on_text_min_size);
                drawableX =  drawableSlashX  + slashIcon.getIntrinsicWidth()
                        + r.getDimensionPixelSize(R.dimen.key_floating_sym_12key_sym_left);
                drawableY =  - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_floating_sym_12key_sym_top);
            } else if ((mVerticalDisp) && (!mOneTouchEmojiShown)) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_mode_on_text_small_size);
                drawableX = drawableSlashX  + r.getDimensionPixelSize(R.dimen.key_sym_12key_sym_left);
                drawableY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_sym_12key_sym_top);
            } else {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_mode_on_text_small_size);
                drawableX =  drawableSlashX  + slashIcon.getIntrinsicWidth()
                             + r.getDimensionPixelSize(R.dimen.key_sym_12key_sym_left_land);
                drawableY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_sym_12key_sym_top_land);
            }
        }

        canvas.translate(drawableSlashX, drawableSlashY);
        slashIcon.setBounds(0, 0, slashIcon.getIntrinsicWidth(), slashIcon.getIntrinsicHeight());
        slashIcon.draw(canvas);
        canvas.translate(-drawableSlashX, -drawableSlashY);

        canvas.translate(drawableX, drawableY);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        icon.draw(canvas);
        canvas.translate(-drawableX, -drawableY);

        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_2nd));

        setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);

        if ((keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_JA_FULL_NUMBER) ||
            (keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_JA_HALF_NUMBER)){
            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_10key_text_color_mode));
        }

        if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_QWERTY) {
            if (isFloatingShown) {
                positionX = drawableSlashX + r.getDimensionPixelSize(R.dimen.key_floating_sym_qwerty_num_left);
                positionY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_floating_sym_qwerty_num_top)
                        + mTextBlock01Size;
            } else if ((mVerticalDisp) && (!mOneTouchEmojiShown)) {
                if (!mOneHandedShown) {
                    positionX = drawableSlashX + r.getDimensionPixelSize(R.dimen.key_sym_qwerty_num_left);
                    positionY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_sym_qwerty_num_top)
                            + mTextBlock01Size;
                } else {
                    positionX = drawableSlashX + r.getDimensionPixelSize(R.dimen.key_onehanded_sym_qwerty_num_left);
                    positionY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_onehanded_sym_qwerty_num_top)
                            + mTextBlock01Size;
                }
            } else {
                positionX = drawableSlashX + r.getDimensionPixelSize(R.dimen.key_sym_qwerty_num_left_land);
                positionY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_sym_qwerty_num_top_land)
                            + mTextBlock01Size;
            }
        } else {
            if (isFloatingShown) {
                positionX = drawableSlashX + r.getDimensionPixelSize(R.dimen.key_floating_sym_12key_num_left);
                positionY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_floating_sym_12key_num_top)
                        + mTextBlock01Size;
            } else if ((mVerticalDisp) && (!mOneTouchEmojiShown)) {
                positionX = drawableSlashX + r.getDimensionPixelSize(R.dimen.key_sym_12key_num_left);
                positionY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_sym_12key_num_top)
                        + mTextBlock01Size;
            } else {
                positionX = drawableSlashX + r.getDimensionPixelSize(R.dimen.key_sym_12key_num_left_land);
                positionY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_sym_12key_num_top_land)
                            + mTextBlock01Size;
            }
        }

        if (numText != null) {
            mPaintTextBlock01.setTextAlign(Align.LEFT);
            canvas.drawText(numText, positionX, positionY, mPaintTextBlock01);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawSymkeySymbol */
    private void drawSymbolSymkey(Resources r, Canvas canvas) {

        mPaint.setTextAlign(Paint.Align.CENTER);
        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        Drawable icon = null;
        Drawable slashIcon = null;
        String numText = null;
        float positionY = 0;
        float positionX = 0;
        int offsetId = mId - KEY_ID_SYMBOL;

        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();

        setOneTouchEmojiShown();
        if(!mKeySkin.isValid()){
            icon = r.getDrawable(R.drawable.ime_keypad_icon_sym);
            slashIcon = r.getDrawable(R.drawable.ime_keypad_icon_slash);
        }else{
            icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_sym);
            slashIcon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_slash);
        }

        int drawableSlashX = ( - slashIcon.getIntrinsicWidth()) / 2;
        int drawableSlashY = - (slashIcon.getIntrinsicHeight()) / 2;

        int drawableX = 0;
        int drawableY = 0;

        switch (keyboard.getKeyMode()) {
        case DefaultSoftKeyboard.KEYMODE_JA_FULL_NUMBER:
            if (TEXTBLOCK_03[offsetId - 1] != null) {
                numText = TEXTBLOCK_03[offsetId - 1];
            }
            break;
        default:
            if (TEXTBLOCK_02[offsetId - 1] != null) {
                numText = TEXTBLOCK_02[offsetId - 1];
            }
            break;
        }

        if (isFloatingShown) {
            mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_floating_mode_on_text_min_size);
            drawableX = drawableSlashX + slashIcon.getIntrinsicWidth()
                        + r.getDimensionPixelSize(R.dimen.key_floating_symbol_sym_left);
            drawableY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_floating_symbol_sym_top);
        } else if ((mVerticalDisp)) {
            mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_mode_on_text_small_size);
            drawableX = drawableSlashX  + r.getDimensionPixelSize(R.dimen.key_sym_12key_sym_left);
            drawableY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_sym_12key_sym_top);
        } else {
            mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_mode_on_text_small_size);
            drawableX =  drawableSlashX  + slashIcon.getIntrinsicWidth()
                         + r.getDimensionPixelSize(R.dimen.key_sym_12key_sym_left_land);
            drawableY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_sym_12key_sym_top_land);
        }

        canvas.translate(drawableSlashX, drawableSlashY);
        slashIcon.setBounds(0, 0, slashIcon.getIntrinsicWidth(), slashIcon.getIntrinsicHeight());
        slashIcon.draw(canvas);
        canvas.translate(-drawableSlashX, -drawableSlashY);

        canvas.translate(drawableX, drawableY);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        icon.draw(canvas);
        canvas.translate(-drawableX, -drawableY);

        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_2nd));

        setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);

        if (isFloatingShown) {
            positionX = drawableSlashX + r.getDimensionPixelSize(R.dimen.key_floating_symbol_num_left);
            positionY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_floating_symbol_num_top)
                    + mTextBlock01Size;
        } else if ((mVerticalDisp)) {
            positionX = drawableSlashX + r.getDimensionPixelSize(R.dimen.key_sym_12key_num_left);
            positionY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_sym_12key_num_top)
                    + mTextBlock01Size;
        } else {
            positionX = drawableSlashX + r.getDimensionPixelSize(R.dimen.key_sym_12key_num_left_land);
            positionY = - (mKeyHeight / 2) + r.getDimensionPixelSize(R.dimen.key_sym_12key_num_top_land)
                        + mTextBlock01Size;
        }

        if (numText != null) {
            mPaintTextBlock01.setTextAlign(Align.LEFT);
            canvas.drawText(numText, positionX, positionY, mPaintTextBlock01);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawPeriodkey */
    private void drawPeriodkey(Resources r, Canvas canvas) {
        if (mId == KEY_ID_QWERTY_PERIOD_FULL
                || mId == KEY_ID_QWERTY_PERIOD_HALF
                || mId == KEY_ID_PERIOD_KO_KUTEN) {
            Drawable subIcon = null;
            int adjustX = 0;

            if (!mKeySkin.isValid()) {
                subIcon = r.getDrawable(R.drawable.ime_keypad_btn_more_01);
            } else {
                subIcon = mKeySkin.getDrawable(R.drawable.ime_keypad_btn_more_01);
            }

            if (KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY
                    .endsWith(mKeySkin.getPackageName())) {
                adjustX = (int) mKeySubIconGapX;
            }

            float scaleRatio = getScaleRatio();
            int drawableSubIconWidth = (int)(subIcon.getIntrinsicWidth() * scaleRatio);
            int drawableSubIconHeight = (int)(subIcon.getIntrinsicHeight() * scaleRatio);
            final int drawableSubIconX = - (drawableSubIconWidth - mKeyWidth / 2) - adjustX;
            final int drawableSubIconY = - (mKeyHeight / 2);

            canvas.translate(drawableSubIconX, drawableSubIconY);
            subIcon.setBounds(0, 0, drawableSubIconWidth, drawableSubIconHeight);
            subIcon.draw(canvas);
            canvas.translate(-drawableSubIconX, -drawableSubIconY);
        }

        float positionY = 0;
        float positionX = 0;
        FontMetrics fontMetrics = null;
        DefaultSoftKeyboard keyboard =
                (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;

        setOneTouchEmojiShown();
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();

        if (mId == KEY_ID_QWERTY_PERIOD_FULL
                || mId == KEY_ID_QWERTY_PERIOD_HALF
                || mId == KEY_ID_PERIOD_KO_KUTEN
                || mId == KEY_ID_PERIOD_KO_COMMA) {
            if (!isFloatingShown) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_qwerty_period_text_size);
            } else {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_onehanded_period_text_size);
            }
        } else if (mId == KEY_ID_PERIOD) {
            if (!isFloatingShown) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_lattice_text_size);
            } else {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_onehanded_period_text_size);
            }
        } else {
            mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_period_text_size);
        }
        if (!isFloatingShown) {
            mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_qwerty_period_text_size);
        } else {
            mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_onehanded_period_text_size);
        }
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock02.setTextSize(mTextBlock02Size);
        if (mId == KEY_ID_QWERTY_PERIOD_FULL
                || mId == KEY_ID_QWERTY_PERIOD_HALF
                || mId == KEY_ID_PERIOD_KO_KUTEN
                || mId == KEY_ID_PERIOD_KO_COMMA) {
            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_symkey_text_color));
            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_symkey_text_color));
            mPaintTextBlock01.setAntiAlias(true);
            mPaintTextBlock02.setAntiAlias(true);
            setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);
            setTextShadow(mPaintTextBlock02, R.color.key_text_shadow_color);
        } else {
            boolean isPressed = keyboard.getKeyPressed(mId);
            if (isPressed) {
                mPaintTextBlock01.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(),
                        R.color.key_text_color_main_pressed));
                mPaintTextBlock02.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(),
                        R.color.key_text_color_main_pressed));
            } else {
                mPaintTextBlock01.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(), R.color.key_period_text_color));
                mPaintTextBlock02.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(), R.color.key_period_text_color));
            }
        }
        int adjustVlaueLeft = 0;
        int adjustVlaueRight = 0;

        if (TEXTBLOCK_01[mId - 1] != null) {
            mPaintTextBlock01.setTextAlign(Align.CENTER);
            if(mId == KEY_ID_PHONE_SYM){
                TEXTBLOCK_01[mId - 1] = r.getString(R.string.label_phone_symkey_txt);
            }

            if(mId == KEY_ID_PHONE_SYM || mId == KEY_ID_PHONE_123){
                if (isFloatingShown) {
                    mPaintTextBlock01.setTextSize(r.getDimensionPixelSize(R.dimen.key_onehanded_phone_keyboard_func_text_size));
                } else {
                    mPaintTextBlock01.setTextSize(r.getDimensionPixelSize(R.dimen.key_phone_keyboard_func_text_size));
                }
                mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_symkey_text_color));
                setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);
            }

            fontMetrics = mPaintTextBlock01.getFontMetrics();
            positionX = 0;
            positionY = (float)(0 + Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);

            if (mId == KEY_ID_QWERTY_PERIOD_FULL) {
                if (mVerticalDisp) {
                    if (mOneTouchEmojiShown) {
                        if (!isFloatingShown) {
                            adjustVlaueLeft = mQwertyEmojiPeriodJaPortLeftAdjustGapX;
                        } else {
                            adjustVlaueLeft = mOneHandedQwertyEmojiPeriodJaPortLeftAdjustGapX;
                        }
                    } else {
                        if (isFloatingShown) {
                            adjustVlaueLeft = mFloatingQwertyPeriodPortLeftAdjustGapX;
                        } else if (mOneHandedShown) {
                            adjustVlaueLeft = mOneHandedQwertyPeriodJaPortLeftAdjustGapX;
                        } else {
                            adjustVlaueLeft = mQwertyPeriodJaPortLeftAdjustGapX;
                        }
                    }
                } else {
                    if (mOneTouchEmojiShown || keyboard.isSplitMode()) {
                        adjustVlaueRight = mQwertyEmojiPeriodJaLandRightAdjustGapX;
                    } else {
                        adjustVlaueLeft = mQwertyPeriodJaLandLeftAdjustGapX;
                    }
                }
            } else if (mId == KEY_ID_QWERTY_PERIOD_HALF) {
                if (mVerticalDisp) {
                    if (mOneTouchEmojiShown) {
                        if (!isFloatingShown) {
                            adjustVlaueLeft = mQwertyEmojiPeriodEnPortLeftAdjustGapX;
                        } else {
                            adjustVlaueLeft = mOneHandedQwertyEmojiPeriodEnPortLeftAdjustGapX;
                        }
                    } else {
                        if (isFloatingShown) {
                            adjustVlaueLeft = mFloatingQwertyPeriodPortLeftAdjustGapX;
                        } else if (mOneHandedShown) {
                            adjustVlaueLeft = mOneHandedQwertyPeriodEnPortLeftAdjustGapX;
                        } else {
                            adjustVlaueLeft = mQwertyPeriodEnPortLeftAdjustGapX;
                        }
                    }
                } else {
                    if (mOneTouchEmojiShown || keyboard.isSplitMode()) {
                        adjustVlaueRight = mQwertyEmojiPeriodEnLandRightAdjustGapX;
                    } else {
                        adjustVlaueLeft = mQwertyPeriodEnLandLeftAdjustGapX;
                    }
                }
            }

            if (mId == KEY_ID_QWERTY_PERIOD_FULL) {
                mPaintTextBlock01.setTextAlign(Align.LEFT);
                if (mVerticalDisp) {
                    if (mOneTouchEmojiShown) {
                        positionX =  (float) (0 - mKeyWidth / 2 + mQwertyEmojiPeriodCommaLeftVerticalGapX + adjustVlaueLeft);
                    } else {
                        positionX =  (float) (0 - mKeyWidth / 2 + mQwertyPeriodCommaLeftVerticalGapX + adjustVlaueLeft);
                    }
                    if (!isFloatingShown && !mOneHandedShown) {
                        positionY = (float)(0 - mKeyHeight / 2 + Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent) + mQwertyPeriodCommaTopVerticalGapY);
                    } else {
                        positionX =  (float) (0 - mKeyWidth / 2 + mOneHandedQwertyPeriodCommaLeftVerticalGapX + adjustVlaueLeft);
                        positionY = (float)(0 - mKeyHeight / 2 + Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent) + mOneHandedQwertyPeriodCommaTopVerticalGapY);
                    }
                } else {
                    if (mOneTouchEmojiShown || keyboard.isSplitMode()) {
                        positionX =  (float) (0 - mKeyWidth / 2 + mQwertyEmojiPeriodCommaLeftHorizontalGapX);
                    } else {
                        positionX =  (float) (0 - mKeyWidth / 2 + mQwertyPeriodCommaLeftHorizontalGapX + adjustVlaueLeft);
                    }
                    positionY = (float)(0 - mKeyHeight / 2 + Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent) +  mQwertyPeriodCommaTopHorizontalGapY);
                }
            } else if (mId == KEY_ID_QWERTY_PERIOD_HALF) {
                mPaintTextBlock01.setTextAlign(Align.LEFT);
                if (mVerticalDisp) {
                    if (mOneTouchEmojiShown) {
                        positionX =  (float) (0 - mKeyWidth / 2 + mQwertyEmojiPeriodCommaLeftVerticalGapX + adjustVlaueLeft);
                    } else {
                        positionX =  (float) (0 - mKeyWidth / 2 + mQwertyPeriodCommaLeftVerticalGapX + adjustVlaueLeft);
                    }
                    if (!isFloatingShown && !mOneHandedShown) {
                        positionY = (float)(0 - mKeyHeight / 2 + Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent) + mQwertyPeriodCommaTopVerticalGapY);
                    } else {
                        positionX =  (float) (0 - mKeyWidth / 2 + mOneHandedQwertyPeriodCommaLeftVerticalGapX + adjustVlaueLeft);
                        positionY = (float)(0 - mKeyHeight / 2 + Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent) + mOneHandedQwertyPeriodCommaTopVerticalGapY);
                    }
                } else {
                    if (mOneTouchEmojiShown || keyboard.isSplitMode()) {
                        positionX =  (float) (0 - mKeyWidth / 2 + mQwertyEmojiPeriodCommaLeftHorizontalGapX);
                    } else {
                        positionX =  (float) (0 - mKeyWidth / 2 + mQwertyPeriodCommaLeftHorizontalGapX + adjustVlaueLeft);
                    }
                    positionY = (float)(0 - mKeyHeight / 2 + Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent) +  mQwertyPeriodCommaTopHorizontalGapY);
                }

            }
            canvas.drawText(TEXTBLOCK_01[mId - 1], positionX, positionY, mPaintTextBlock01);
        }

        if (TEXTBLOCK_02[mId - 1] != null) {
            mPaintTextBlock02.setTextAlign(Align.LEFT);
            if (mId == KEY_ID_QWERTY_PERIOD_FULL || mId == KEY_ID_QWERTY_PERIOD_HALF) {
                if (mVerticalDisp) {
                    if (!isFloatingShown) {
                        positionX += (float)mQwertyPeriodKutoutenLeftVerticalGapX;
                    } else {
                        positionX += (float)mOneHandedQwertyPeriodKutoutenLeftVerticalGapX;
                    }
                } else {
                    if (mOneTouchEmojiShown || keyboard.isSplitMode()) {
                        positionX =  (float) (mKeyWidth / 2 - mQwertyEmojiPeriodKutoutenRightHorizontalGapX - adjustVlaueRight);
                    } else {
                        positionX += (float)mQwertyPeriodKutoutenLeftHorizontalGapX;
                    }
                }
            }
            canvas.drawText(TEXTBLOCK_02[mId - 1], positionX, positionY, mPaintTextBlock02);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawCapskey */
    private void drawCapskey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;

        FontMetrics fontMetrics = null;
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();
        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        boolean isPressed = keyboard.getKeyPressed(mId);

        if( KEY_ID_DAKUTEN != mId){

            if(KEY_ID_12KEY_CAPS == mId){
                if (!isFloatingShown) {
                    mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_capskey_select_text_size);
                    mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_capskey_select_text_size);
                    mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_capskey_unselect_text_size);
                } else {
                    mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_onehanded_capskey_select_text_size);
                    mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_onehanded_capskey_select_text_size);
                    mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_onehanded_capskey_unselect_text_size);
                }
                if (isPressed) {
                    mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_main_pressed));
                    mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_main_pressed));
                } else {
                    mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_select_text_color));
                    mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_select_text_color));
                }
                mPaintTextBlock03.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_unselect_text_color));
            }else{
                if (!isFloatingShown) {
                    mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_capskey_unselect_text_size);
                    mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_capskey_select_text_size);
                    mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_capskey_select_text_size);
                } else {
                    mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_onehanded_capskey_unselect_text_size);
                    mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_onehanded_capskey_select_text_size);
                    mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_onehanded_capskey_select_text_size);
                }
                mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_unselect_text_color));
                if (isPressed) {
                    mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_main_pressed));
                    mPaintTextBlock03.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_main_pressed));
                } else {
                    mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_select_text_color));
                    mPaintTextBlock03.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_select_text_color));
                }
            }

            mPaintTextBlock01.setTextSize(mTextBlock01Size);
            mPaintTextBlock02.setTextSize(mTextBlock02Size);
            mPaintTextBlock03.setTextSize(mTextBlock03Size);
            mPaintTextBlock01.setTextAlign(Align.CENTER);
            mPaintTextBlock02.setTextAlign(Align.CENTER);
            mPaintTextBlock03.setTextAlign(Align.CENTER);


            if (TEXTBLOCK_02[mId - 1] != null) {
                fontMetrics = mPaintTextBlock02.getFontMetrics();
                positionX = 0;
                positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);

                canvas.drawText(TEXTBLOCK_02[mId - 1], positionX, positionY, mPaintTextBlock02);
            }

            if (TEXTBLOCK_01[mId - 1] != null) {
                fontMetrics = mPaintTextBlock01.getFontMetrics();
                positionX = 0 - capsKeyGap;
                    positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);
                canvas.drawText(TEXTBLOCK_01[mId - 1], positionX, positionY, mPaintTextBlock01);
            }

            if (TEXTBLOCK_03[mId - 1] != null) {
                fontMetrics = mPaintTextBlock03.getFontMetrics();
                positionX = 0 + capsKeyGap;
                    positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);
                canvas.drawText(TEXTBLOCK_03[mId - 1], positionX, positionY, mPaintTextBlock03);
            }

        }else{

            if (!isFloatingShown) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_dakuten_left_top_text_size);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_dakuten_right_top_text_size);
                mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_dakuten_left_bottom_text_size);
                mTextBlock04Size = r.getDimensionPixelSize(R.dimen.key_dakuten_right_bottom_text_size);
            } else {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_floating_dakuten_left_top_text_size);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_floating_dakuten_right_top_text_size);
                mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_floating_dakuten_left_bottom_text_size);
                mTextBlock04Size = r.getDimensionPixelSize(R.dimen.key_floating_dakuten_right_bottom_text_size);
            }

            mPaintTextBlock01.setTextSize(mTextBlock01Size);
            mPaintTextBlock01.setTextAlign(Align.RIGHT);
            mPaintTextBlock02.setTextSize(mTextBlock02Size);
            mPaintTextBlock02.setTextAlign(Align.RIGHT);
            mPaintTextBlock03.setTextSize(mTextBlock03Size);
            mPaintTextBlock03.setTextAlign(Align.RIGHT);
            mPaintTextBlock04.setTextSize(mTextBlock04Size);
            mPaintTextBlock04.setTextAlign(Align.RIGHT);

            //DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
            //boolean isPressed = keyboard.getKeyPressed(mId);
            Drawable icon;

            if (isPressed) {
                mPaintTextBlock01.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(),
                        R.color.key_text_color_main_pressed));
                mPaintTextBlock02.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(),
                        R.color.key_text_color_main_pressed));
                mPaintTextBlock03.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(),
                        R.color.key_text_color_main_pressed));
                mPaintTextBlock04.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(),
                        R.color.key_text_color_main_pressed));

                if (!isFloatingShown) {
                    if (!mKeySkin.isValid()) {
                        icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_dakuten_pressed);
                    } else {
                        icon = mKeySkin
                                .getDrawable(R.drawable.ime_keypad_icon_jp_dakuten_pressed);
                    }
                } else {
                    icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_min_dakuten_pressed);
                }

            } else {
                mPaintTextBlock01.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(), R.color.key_dakuten_text_color));
                mPaintTextBlock02.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(), R.color.key_dakuten_text_color));
                mPaintTextBlock03.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(), R.color.key_dakuten_text_color));
                mPaintTextBlock04.setColor(mKeySkin.getColor(
                        OpenWnn.getContext(), R.color.key_dakuten_text_color));

                if (!isFloatingShown) {
                    if (!mKeySkin.isValid()) {
                        icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_dakuten);
                    } else {
                        icon = mKeySkin
                                .getDrawable(R.drawable.ime_keypad_icon_jp_dakuten);
                    }
                } else {
                    icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_min_dakuten);
                }
            }

            float iconPositionX = 0;
            float iconPositionY = 0;

            positionX = ( - icon.getIntrinsicWidth()) / 2;
            if(mVerticalDisp){
                    positionY = - mKeyHeight + mDakutenKeyBottomVerticalGapY;
                    iconPositionY = r.getDimensionPixelSize(R.dimen.key_dakuten_height_size);
                if (isFloatingShown) {
                    positionY -= r.getDimensionPixelSize(R.dimen.key_dakuten_height_size_floating_adjust);
                    iconPositionY -= r.getDimensionPixelSize(R.dimen.key_dakuten_icon_height_size_floating_adjust);
                }
            }else{
                iconPositionX = r.getDimensionPixelSize(R.dimen.key_dakuten_icon_width_size_adjust_horizontal);
                positionY = (float)(- mKeyHeight + mDakutenKeyBottomGapY);
                iconPositionY = (float)r.getDimensionPixelSize(R.dimen.key_dakuten_split_height_size);
            }
            canvas.translate(positionX + iconPositionX, positionY + iconPositionY);
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            icon.draw(canvas);
            canvas.translate(-(positionX + iconPositionX), -(positionY + iconPositionY));

            if (TEXTBLOCK_03[mId - 1] != null) {
                fontMetrics = mPaintTextBlock03.getFontMetrics();
                positionY += (float)(mKeyHeight - Math.abs(fontMetrics.descent + fontMetrics.ascent)/2*3);
                canvas.drawText(TEXTBLOCK_03[mId - 1], positionX, positionY, mPaintTextBlock03);
            }
            if (TEXTBLOCK_04[mId - 1] != null) {
                fontMetrics = mPaintTextBlock04.getFontMetrics();
                if (!isFloatingShown) {
                    if(mVerticalDisp){
                        positionX += (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)*3.3);
                    } else {
                        positionX += (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)*3.8);
                    }
                } else {
                    positionX += (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)*3.8);
                }
                canvas.drawText(TEXTBLOCK_04[mId - 1], positionX, positionY, mPaintTextBlock04);
            }

            if (TEXTBLOCK_02[mId - 1] != null) {
                fontMetrics = mPaintTextBlock02.getFontMetrics();
                positionX += (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)*2/3 - mDakutenKeyTopGap);
                if(mVerticalDisp){
                    positionY = (float)(- mKeyHeight/2 + mDakutenKeyVerticalGap + Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);
                }else{
                    positionY = (float)(- mKeyHeight/2 + mDakutenKeyGap + Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);
                }
                canvas.drawText(TEXTBLOCK_02[mId - 1], positionX, positionY, mPaintTextBlock02);
            }

            if (TEXTBLOCK_01[mId - 1] != null) {
                fontMetrics = mPaintTextBlock01.getFontMetrics();
                if (mVerticalDisp) {
                    positionX -= (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)*2.6);
                } else {
                    positionX -= (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)*3.2);
                }
                positionY += (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent));
                canvas.drawText(TEXTBLOCK_01[mId - 1], positionX, positionY, mPaintTextBlock01);
            }
        }
    }

    /** @see android.graphics.drawable.Drawable#drawPreviewkeyFlick */
    private void drawPreviewkeyFlick(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;
        int previewCenterTopVerticalGapY = 0;

        FontMetrics fontMetrics = null;
        FontMetrics fontMetrics2 = null;
        int offsetId = mId - KEY_ID_FLICK_PREVIEW;

        if ((offsetId >= KEY_ID_ALPHA && offsetId < KEY_ID_KATAKANA)
                || offsetId == KEY_ID_12KEY_PERIOD_COMMA
                || (offsetId >= KEY_ID_NUM_HALF && offsetId < KEY_ID_SYM)
                || mId == KEY_ID_FLICK_PREVIEW_EN_HALF_NUM1
                || mId == KEY_ID_FLICK_PREVIEW_EN_FULL_NUM1
                || mId == KEY_ID_FLICK_PREVIEW_NUM_HALF_NUM1
                || mId == KEY_ID_FLICK_PREVIEW_NUM_FULL_NUM1) {
            mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_preview_center_text_size_en_num);
            mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_preview_side_text_size_en_num);
            mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_preview_side_text_size_en_num);
            mTextBlock04Size = r.getDimensionPixelSize(R.dimen.key_preview_side_text_size_en_num);
            mTextBlock05Size = r.getDimensionPixelSize(R.dimen.key_preview_side_text_size_en_num);
            previewCenterTopVerticalGapY = mPreviewCenterTopEnVerticalGapY;
        } else {
            mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_preview_center_text_size_jp);
            mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_preview_side_text_size_jp);
            mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_preview_side_text_size_jp);
            mTextBlock04Size = r.getDimensionPixelSize(R.dimen.key_preview_side_text_size_jp);
            mTextBlock05Size = r.getDimensionPixelSize(R.dimen.key_preview_side_text_size_jp);
            previewCenterTopVerticalGapY = mPreviewCenterTopVerticalGapY;
        }

        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock02.setTextSize(mTextBlock02Size);
        mPaintTextBlock03.setTextSize(mTextBlock03Size);
        mPaintTextBlock04.setTextSize(mTextBlock04Size);
        mPaintTextBlock05.setTextSize(mTextBlock05Size);

        mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_preview_center_text_color));
        mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_preview_side_text_color));
        mPaintTextBlock03.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_preview_side_text_color));
        mPaintTextBlock04.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_preview_side_text_color));
        mPaintTextBlock05.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_preview_side_text_color));

        mPaintTextBlock01.setTextAlign(Align.CENTER);
        mPaintTextBlock02.setTextAlign(Align.CENTER);
        mPaintTextBlock03.setTextAlign(Align.CENTER);
        mPaintTextBlock04.setTextAlign(Align.CENTER);
        mPaintTextBlock05.setTextAlign(Align.CENTER);

        if (PREVIEW_TEXTBLOCK_01[mId - KEY_ID_FLICK_PREVIEW] != null) {   // Center
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            fontMetrics2 = mPaintTextBlock03.getFontMetrics();
            positionX = 0;
            positionY = (float)(0 - mKeyHeight/8*5 + previewCenterTopVerticalGapY
                    + (Math.abs(fontMetrics.descent + fontMetrics.ascent) - Math.abs(fontMetrics2.descent + fontMetrics2.ascent)) / 2);

            canvas.drawText(PREVIEW_TEXTBLOCK_01[mId - KEY_ID_FLICK_PREVIEW], positionX, positionY, mPaintTextBlock01);
        }
        if ( PREVIEW_TEXTBLOCK_02[mId - KEY_ID_FLICK_PREVIEW] != null) {  // Left
            positionX = 0 - mPreviewCenterVerticalGapX;
            positionY = (float)(0 - mKeyHeight/8*5 + previewCenterTopVerticalGapY + mPreviewCenterLeftRightVerticalGapY);

            canvas.drawText(PREVIEW_TEXTBLOCK_02[mId - KEY_ID_FLICK_PREVIEW], positionX, positionY, mPaintTextBlock02);
        }
        if (PREVIEW_TEXTBLOCK_04[mId - KEY_ID_FLICK_PREVIEW] != null) {   // Right
            positionX = 0 + mPreviewCenterVerticalGapX;
            positionY = (float)(0 - mKeyHeight/8*5 + previewCenterTopVerticalGapY + mPreviewCenterLeftRightVerticalGapY);
            canvas.drawText(PREVIEW_TEXTBLOCK_04[mId - KEY_ID_FLICK_PREVIEW], positionX, positionY, mPaintTextBlock04);
        }

        if ( PREVIEW_TEXTBLOCK_03[mId - KEY_ID_FLICK_PREVIEW] != null){   // Top
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            fontMetrics2 = mPaintTextBlock03.getFontMetrics();
            positionX = 0;
            if ((offsetId >= KEY_ID_ALPHA && offsetId < KEY_ID_KATAKANA)
                    || offsetId == KEY_ID_12KEY_PERIOD_COMMA
                    || (offsetId >= KEY_ID_NUM_HALF && offsetId < KEY_ID_SYM)
                    || mId == KEY_ID_FLICK_PREVIEW_EN_HALF_NUM1
                    || mId == KEY_ID_FLICK_PREVIEW_EN_FULL_NUM1
                    || mId == KEY_ID_FLICK_PREVIEW_NUM_HALF_NUM1
                    || mId == KEY_ID_FLICK_PREVIEW_NUM_FULL_NUM1) {
                positionY = (float)(0 - mKeyHeight/8*5 + previewCenterTopVerticalGapY
                        - Math.abs(fontMetrics.descent + fontMetrics.ascent)/2
                        - Math.abs(fontMetrics2.descent + fontMetrics2.ascent))
                        - mQwertyKeyHeightDeviation;
            } else {
                positionY = (float)(0 - mKeyHeight/8*5 + previewCenterTopVerticalGapY
                        - Math.abs(fontMetrics.descent + fontMetrics.ascent)/2
                        - mPreviewTopBottomVerticalGapY - Math.abs(fontMetrics2.descent + fontMetrics2.ascent)
                        + (Math.abs(fontMetrics.descent + fontMetrics.ascent) - Math.abs(fontMetrics2.descent + fontMetrics2.ascent)) / 2);
            }

            canvas.drawText(PREVIEW_TEXTBLOCK_03[mId - KEY_ID_FLICK_PREVIEW], positionX, positionY, mPaintTextBlock03);;
        }
         if (PREVIEW_TEXTBLOCK_05[mId - KEY_ID_FLICK_PREVIEW] != null) {  // Bottom
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            fontMetrics2 = mPaintTextBlock05.getFontMetrics();
            positionX = 0;
            positionY = (float)(0 - mKeyHeight/8*5 + previewCenterTopVerticalGapY
                            + Math.abs(fontMetrics.descent + fontMetrics.ascent)/2
                            + mPreviewTopBottomVerticalGapY + Math.abs(fontMetrics2.descent + fontMetrics2.ascent)/2);
            canvas.drawText(PREVIEW_TEXTBLOCK_05[mId - KEY_ID_FLICK_PREVIEW], positionX, positionY, mPaintTextBlock05);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawComkey */
    private void drawComkey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;

        FontMetrics fontMetrics = null;

        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();

        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_QWERTY) {
            if (!isFloatingShown) {
                mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_qwerty_comkey_text_size);
            } else {
                mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_onehanded_qwerty_comkey_text_size);
            }
            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_comkey_text_color));
        } else {
            if (!isFloatingShown) {
                mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_comkey_text_size);
            } else {
                mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_onehanded_comkey_text_size);
            }
            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_comkey_text_color));
        }
        setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setTextAlign(Align.CENTER);

        if (TEXTBLOCK_01[mId - 1] != null) {
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            positionX = 0;

            if(mVerticalDisp){
                positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);
            }else{
                positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);
            }

            canvas.drawText(TEXTBLOCK_01[mId - 1], positionX, positionY, mPaintTextBlock01);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawEisukanakey */
    private void drawEisukanakey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;

        FontMetrics fontMetrics = null;
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();

        if (!isFloatingShown) {
            mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_eisukana_text_size);
            mTextBlock02Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_eisukana_text_size);
        } else {
            mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_onehanded_eisukana_text_size);
            mTextBlock02Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_onehanded_eisukana_text_size);
        }
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock02.setTextSize(mTextBlock02Size);
        mPaintTextBlock01.setTextAlign(Align.CENTER);
        mPaintTextBlock02.setTextAlign(Align.CENTER);
        setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);
        setTextShadow(mPaintTextBlock02, R.color.key_text_shadow_color);

        if (TEXTBLOCK_01[mId - 1] != null) {
            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_eisukana_text_color));
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            if(mVerticalDisp){
                positionY = - mEisukanaLineGapY / 2;
            } else {
                positionY = (float) (Math.abs(fontMetrics.ascent) - (float) (m12KeyHeightHorizontal) / 2 + mEisukanaTopGapY);
            }

            canvas.drawText(TEXTBLOCK_01[mId - 1], positionX, positionY, mPaintTextBlock01);
        }
        if (TEXTBLOCK_02[mId - 1] != null) {
            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_eisukana_text_color));
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            if(mVerticalDisp){
                positionY = (float) (Math.abs(fontMetrics.ascent) + mEisukanaLineGapY / 2);
            } else {
                positionY += (float) (Math.abs(fontMetrics.ascent) + mEisukanaLineGapY);
            }

            canvas.drawText(TEXTBLOCK_02[mId - 1], positionX, positionY, mPaintTextBlock02);
        }
    }

    /** @see android.graphics.drawable.Drawable#draw12KeySpaceJpkey */
    private void draw12KeySpaceJpkey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;

        FontMetrics fontMetrics = null;

        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();
        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard) OpenWnn
                .getCurrentIme().mInputViewManager;
        boolean isQwerty= (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_QWERTY);

        if (isFloatingShown) {
            mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_onehanded_12key_space_jp_text_size);
        } else {
            if (isQwerty && mVerticalDisp) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_12key_space_jp_text_size_qwerty_vertical);
            } else {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_12key_space_jp_text_size_12key);
            }
        }
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_12key_space_jp_text_color));
        setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);

        if (TEXTBLOCK_01[mId - 1] != null) {

            fontMetrics = mPaintTextBlock01.getFontMetrics();
            mPaintTextBlock01.setTextAlign(Align.CENTER);
            positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);
            canvas.drawText(TEXTBLOCK_01[mId - 1], positionX, positionY, mPaintTextBlock01);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawQwertyShiftkeyy */
    private void drawQwertyShiftkey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;

        FontMetrics fontMetrics = null;

        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();

        if (!isFloatingShown) {
            mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_comkey_text_size);
            mTextBlock02Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_comkey_text_size);
        } else {
            mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_onehanded_comkey_text_size);
            mTextBlock02Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_onehanded_comkey_text_size);
        }
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);
        mPaintTextBlock02.setTextSize(mTextBlock02Size);
        setTextShadow(mPaintTextBlock02, R.color.key_text_shadow_color);
        float fontWidth = 0;
        positionX = 0;

        if (TEXTBLOCK_01[mId - 1] != null) {
           mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_10key_text_color_mode));
           fontMetrics = mPaintTextBlock01.getFontMetrics();
           fontWidth = mPaintTextBlock01.measureText(TEXTBLOCK_01[mId - 1]);
           positionX = 0 - fontWidth / 2 - fontWidth;

           positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent) / 2);

           canvas.drawText(TEXTBLOCK_01[mId - 1], positionX, positionY, mPaintTextBlock01);
        }

        if (TEXTBLOCK_02[mId - 1] != null) {
            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_qwerty_shift_text_color));
            fontMetrics = mPaintTextBlock02.getFontMetrics();
            positionX = positionX + fontWidth;

            positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent) / 2);

            canvas.drawText(TEXTBLOCK_02[mId - 1], positionX, positionY, mPaintTextBlock02);
        }
    }

    /** @see android.graphics.drawable.Drawable#draw12KeyTenkey */
    private void draw12KeyTenkey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;

        FontMetrics fontMetrics = null;
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();

        if (!isFloatingShown) {
            mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_10key_text_size_middle_top);
        } else {
            mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_onehanded_10key_text_size_middle_top);
        }
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setTextAlign(Align.CENTER);

        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        boolean isPressed = keyboard.getKeyPressed(mId);

        if (isPressed) {
            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                    R.color.key_text_color_pressed));
        } else {
            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(),
                    R.color.key_12key_ten_text_color));
        }

        if (TEXTBLOCK_01[mId - 1] != null) {
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            positionX = 0;
            positionY = 0 + (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);
            canvas.drawText(TEXTBLOCK_01[mId - 1], positionX, positionY, mPaintTextBlock01);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawNaviSelectPreviewkey */
    private void drawPreviewNavikey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;
        float offsetY = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_preview_padding_vgap);

        FontMetrics fontMetrics = null;
        TextPaint paintTextBlockMode = new TextPaint();
        paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_preview_text_color));
        paintTextBlockMode.setTextAlign(Align.CENTER);
        float textBlockSize = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_navi_text_size);
        paintTextBlockMode.setTextSize(textBlockSize);
        paintTextBlockMode.setAntiAlias(true);
        paintTextBlockMode.setTypeface(Typeface.DEFAULT_BOLD);
        fontMetrics = paintTextBlockMode.getFontMetrics();

        positionX = 0;
        positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2 + offsetY);
        switch (mId) {
        case KEY_ID_NAVI_SELECT_PREVIEW:
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_select), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_DESELECT_PREVIEW:
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_deselect), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_COPY_PREVIEW:
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_copy), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_PASTE_PREVIEW:
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_paste), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_CUT_PREVIEW:
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_cut), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_CLOSE_PREVIEW:
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_close), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_ALL_PREVIEW:
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_all), positionX, positionY, paintTextBlockMode);
            break;
        default:
            break;
        }
    }

    /** @see android.graphics.drawable.Drawable#drawNaviSelectkey */
    private void drawNavikey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;


        TextPaint paintTextBlockMode = new TextPaint();
        paintTextBlockMode.setTextAlign(Align.CENTER);
        float textBlockSize;
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();
        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard) OpenWnn.getCurrentIme().mInputViewManager;
        boolean isPressed = keyboard.getKeyPressed(mId);

        if (mOneHandedShown || WnnKeyboardFactory.getSplitMode()) {
            textBlockSize = r.getDimensionPixelSize(R.dimen.key_onehanded_navi_text_size);
        } else if (isFloatingShown) {
            textBlockSize = r.getDimensionPixelSize(R.dimen.key_floating_navi_text_size);
        } else {
            textBlockSize = r.getDimensionPixelSize(R.dimen.key_navi_text_size);
        }
        paintTextBlockMode.setTextSize(textBlockSize);
        paintTextBlockMode.setTypeface(Typeface.DEFAULT_BOLD);
        paintTextBlockMode.setAntiAlias(true);
        positionX = 0;
        positionY = (float)OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_navi_text_top);
        switch (mId) {
        case KEY_ID_NAVI_SELECT:
        case KEY_ID_NAVI_SELECT_SPLIT:
            if (isPressed) {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_main_pressed));
            } else {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_2nd));
                setTextShadow(paintTextBlockMode, R.color.key_text_shadow_color);
            }
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_select), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_DESELECT:
        case KEY_ID_NAVI_DESELECT_SPLIT:
            if (isPressed) {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_main_pressed));
            } else {
                setTextShadow(paintTextBlockMode, R.color.key_text_shadow_color);
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_2nd));
            }
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_deselect), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_COPY:
            if (isPressed) {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_main_pressed));
            } else {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_10key_text_color_top));
            }
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_copy), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_COPY_OFF:
            if (isPressed) {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_main_pressed));
            } else {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_navi_off_color));
            }
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_copy), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_PASTE:
        case KEY_ID_NAVI_PASTE_SPLIT:
            if (isPressed) {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_main_pressed));
            } else {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_10key_text_color_top));
            }
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_paste), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_PASTE_OFF:
        case KEY_ID_NAVI_PASTE_OFF_SPLIT:
            if (isPressed) {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_main_pressed));
            } else {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_navi_off_color));
            }
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_paste), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_CUT:
            if (isPressed) {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_main_pressed));
            } else {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_10key_text_color_top));
            }
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_cut), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_CUT_OFF:
            if (isPressed) {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_main_pressed));
            } else {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_navi_off_color));
            }
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_cut), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_CLOSE:
            if (isPressed) {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_main_pressed));
            } else {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_2nd));
                setTextShadow(paintTextBlockMode, R.color.key_text_shadow_color);
            }
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_close), positionX, positionY, paintTextBlockMode);
            break;
        case KEY_ID_NAVI_ALL:
            if (isPressed) {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_main_pressed));
            } else {
                paintTextBlockMode.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_text_color_2nd));
                setTextShadow(paintTextBlockMode, R.color.key_text_shadow_color);
            }
            canvas.drawText(OpenWnn.getContext().getString(R.string.label_keypad_all), positionX, positionY, paintTextBlockMode);
            break;
        default:
            break;
        }
    }

    /** @see android.graphics.drawable.Drawable#drawKeyModeChangePreviewkey */
    private void drawFlickPopupKeyModekey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;
        float left_gap = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_mode_change_left_gap);
        float right_gap = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_mode_change_right_gap);
        float keyPadding = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_mode_change_key_padding);;
        float oneKeyWidth = (mKeyWidth - left_gap - right_gap) / 4;

        mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_mode_change_text_size);
        mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_mode_change_text_size_en_num);
        mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_mode_change_text_size_en_num);
        mTextBlock04Size = r.getDimensionPixelSize(R.dimen.key_mode_change_text_size_en_num);

        mPaintTextBlock01.setTextAlign(Align.CENTER);
        mPaintTextBlock02.setTextAlign(Align.CENTER);
        mPaintTextBlock03.setTextAlign(Align.CENTER);
        mPaintTextBlock04.setTextAlign(Align.CENTER);

        mPaintTextBlock01.setTypeface(Typeface.DEFAULT);
        mPaintTextBlock02.setTypeface(Typeface.DEFAULT);
        mPaintTextBlock03.setTypeface(Typeface.DEFAULT);
        mPaintTextBlock04.setTypeface(Typeface.DEFAULT);

        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock02.setTextSize(mTextBlock02Size);
        mPaintTextBlock03.setTextSize(mTextBlock03Size);
        mPaintTextBlock04.setTextSize(mTextBlock04Size);

        mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_color));
        mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_color));
        mPaintTextBlock03.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_color));
        mPaintTextBlock04.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_color));

        FontMetrics fontMetrics = mPaintTextBlock02.getFontMetrics();
        positionY -= (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/6 * 5);
        positionX = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)*2 - mKeyWidth/2 + left_gap);
        Paint paint = new Paint();

        switch (mId) {
        case KEY_MODE_CHANGE_EFFECTIVE_OFF_HIRA_B_PREVIEW:
            paint.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_bg_color));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(positionX + oneKeyWidth/2 + keyPadding/2, positionY*2, positionX + oneKeyWidth/2 + oneKeyWidth - keyPadding/2, -positionY, paint);
            //canvas.drawRect(leftGap + positionXAdd, positionY*2, leftGap + positionXAdd * 2, -positionY, paint);
            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_pushed_color));
            break;
        case KEY_MODE_CHANGE_EFFECTIVE_OFF_ENG_B_PREVIEW:
            paint.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_bg_color));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(positionX + oneKeyWidth/2 + oneKeyWidth + keyPadding/2, positionY*2, positionX + oneKeyWidth/2 + oneKeyWidth*2 - keyPadding/2, -positionY, paint);
            mPaintTextBlock03.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_pushed_color));
            break;
        case KEY_MODE_CHANGE_EFFECTIVE_OFF_NUM_B_PREVIEW:
            paint.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_bg_color));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(positionX + oneKeyWidth/2 + oneKeyWidth*2 + keyPadding/2, positionY*2, positionX + oneKeyWidth/2 + oneKeyWidth*3 - keyPadding/2, -positionY, paint);
            mPaintTextBlock04.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_pushed_color));
            break;
        case KEY_MODE_CHANGE_EFFECTIVE_OFF_NON_B_PREVIEW:
            mPaintTextBlock04.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_non_color));
            break;
        case KEY_MODE_CHANGE_EFFECTIVE_OFF_ENG_OFF_HIRA_B_PREVIEW:
            paint.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_bg_color));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(positionX + oneKeyWidth/2 + oneKeyWidth + keyPadding/2, positionY*2, positionX + oneKeyWidth/2 + oneKeyWidth*2 - keyPadding/2, -positionY, paint);
            mPaintTextBlock03.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_pushed_color));
            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_define_color));
            break;
        case KEY_MODE_CHANGE_EFFECTIVE_OFF_NUM_OFF_HIRA_B_PREVIEW:
            paint.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_bg_color));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(positionX + oneKeyWidth/2 + oneKeyWidth*2 + keyPadding/2, positionY*2, positionX + oneKeyWidth/2 + oneKeyWidth*3 - keyPadding/2, -positionY, paint);
            mPaintTextBlock04.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_pushed_color));
            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_define_color));
            break;
        case KEY_MODE_CHANGE_INVALID_OFF_HIRA_B_PREVIEW:
            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_define_color));
            break;
        case KEY_MODE_CHANGE_INVALID_OFF_ENG_B_PREVIEW:
            mPaintTextBlock03.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_define_color));
            break;
        case KEY_MODE_CHANGE_INVALID_OFF_NUM_B_PREVIEW:
            mPaintTextBlock04.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_define_color));
            break;
        case KEY_MODE_CHANGE_INVALID_OFF_NON_B_PREVIEW:
            mPaintTextBlock04.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_non_color));
            break;
        case KEY_MODE_CHANGE_INVALID_OFF_ENG_OFF_HIRA_B_PREVIEW:
            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_define_color));
            mPaintTextBlock03.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_define_color));
            break;
        case KEY_MODE_CHANGE_INVALID_OFF_NUM_OFF_HIRA_B_PREVIEW:
            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_define_color));
            mPaintTextBlock04.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_define_color));
            break;
            default:
            break;
        }

        canvas.drawText("\u6587\u5b57", positionX, 0, mPaintTextBlock01);
        positionX += oneKeyWidth;
        canvas.drawText("\u3042", positionX, 0, mPaintTextBlock02);
        positionX += oneKeyWidth;
        canvas.drawText("AB", positionX, 0, mPaintTextBlock03);
        positionX += oneKeyWidth;
        canvas.drawText("12", positionX, 0, mPaintTextBlock04);
    }

    /** @see android.graphics.drawable.Drawable#drawKeyModeChangePreviewkey */
    private void drawFlickPopupKeySymkey(Resources r, Canvas canvas) {

        float left_gap = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_mode_change_left_gap);
        float right_gap = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_mode_change_right_gap);
        float keyPadding = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_mode_change_key_padding);
        float top_gap = OpenWnn.getContext().getResources().getDimensionPixelSize(
                R.dimen.key_mode_change_top_padding);
        float bottom_gap = OpenWnn.getContext().getResources().getDimensionPixelSize(
                R.dimen.key_mode_change_bottom_padding);
        float top_padding = OpenWnn.getContext().getResources().getDimensionPixelSize(
                R.dimen.preview_flick_padding_top);
        float bottom_padding = OpenWnn.getContext().getResources().getDimensionPixelSize(
                R.dimen.preview_flick_padding_bottom);

        float oneKeyWidth = (mKeyWidth - left_gap - right_gap - keyPadding) / 2;
        float oneKeyHeight = (mKeyHeight - top_gap - bottom_gap);

        float positionY = (-oneKeyHeight / 2  - (bottom_gap - top_gap)
                - ((bottom_padding - top_padding) / 2 / 2 / 2) + 1);
        float numPositionX = (-(keyPadding / 2) - oneKeyWidth - 1);
        float symPositionX = (keyPadding / 2 - 1);

        mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_mode_change_text_size_en_num);
        mPaintTextBlock01.setTextAlign(Align.CENTER);
        mPaintTextBlock01.setTypeface(Typeface.DEFAULT);
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_color));

        Paint paint = new Paint();

        switch (mId) {
        case KEY_MODE_CHANGE_SYMNUM_NUM_B_PREVIEW:
            paint.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_bg_color));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(numPositionX, positionY,
                    numPositionX + oneKeyWidth, positionY + oneKeyHeight, paint);
            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_text_pushed_color));
            break;
        case KEY_MODE_CHANGE_SYMNUM_SYM_B_PREVIEW:
            paint.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_mode_change_bg_color));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(symPositionX, positionY,
                    (symPositionX + oneKeyWidth), (positionY + oneKeyHeight), paint);
            break;
        default:
            break;
        }

        FontMetrics fontMetrics = mPaintTextBlock01.getFontMetrics();
        canvas.drawText("12?", (numPositionX + oneKeyWidth / 2),
                (positionY + oneKeyHeight / 2 - (fontMetrics.descent + fontMetrics.ascent) / 2),
                mPaintTextBlock01);

        Drawable icon;
        switch (mId) {
        case KEY_MODE_CHANGE_SYMNUM_SYM_B_PREVIEW:
            icon = r.getDrawable(R.drawable.ime_keypad_input_icon_sym_pressed);
            break;
        default:
            icon = r.getDrawable(R.drawable.ime_keypad_input_icon_sym_normal);
            break;
        }
        canvas.translate(symPositionX, positionY);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        icon.draw(canvas);
        canvas.translate(-symPositionX, -positionY);

        return;

    }

    /** @see android.graphics.drawable.Drawable#drawPreviewkey */
    private void drawPreviewkey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;
        float offsetY = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_preview_text_padding_vgap);
        FontMetrics fontMetrics = null;
        int offsetId = mId - KEY_ID_KEYTOP_PREVIEW_OFFSET;

        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY
            && keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_EN_ALPHABET ) {
            offsetY = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_preview_12key_en_alphabet_text_padding_vgap);
        }

        if (KEY_ID_ALPHA > offsetId || (KEY_ID_KATAKANA <= offsetId && offsetId < KEY_ID_NUM_HALF)) {
            mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_popup);
            offsetY = r.getDimensionPixelSize(R.dimen.key_preview_text_padding_vgap);
        } else if (KEY_ID_ALPHA <= offsetId && KEY_ID_KATAKANA > offsetId) {
            mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_en_upper_popup);
            offsetY = r.getDimensionPixelSize(R.dimen.key_preview_12key_en_alphabet_text_padding_vgap);
        } else {
            mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_popup);
        }

        mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_preview_center_text_color));
        mPaintTextBlock01.setTextAlign(Align.CENTER);
        mPaintTextBlock01.setTextSize(mTextBlock01Size);

        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();
        int floatingAdjust = 0;
        if (isFloatingShown
                && (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY)
                && ((keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_JA_FULL_NUMBER)
                || (keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_JA_HALF_NUMBER)
                || (keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_JA_NUMERIC)
                || (keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_JA_NUMERIC_DECIMAL)
                || (keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_JA_NUMERIC_SIGNED))) {
            // Adjusting vertical alignment of key preview of number qwerty floating keyboard.
            floatingAdjust = r.getDimensionPixelSize(R.dimen.key_preview_12key_jp_number_floating_text_padding_vgap);
        }

        // Drawing to the Top position.
        if (TEXTBLOCK_01[offsetId - 1] != null) {
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            positionX = 0;
            if (KEY_ID_ALPHA > offsetId || (KEY_ID_KATAKANA <= offsetId && offsetId < KEY_ID_NUM_HALF)) {
                if(mVerticalDisp){
                    positionY = -(float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)) - (float)mKeyHeight/3 * 2  + offsetY;
                } else {
                    positionY = -(float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)) - (float)mKeyHeight/5 * 4  + offsetY;
                }
            } else {
                if(mVerticalDisp){
                    // Adjusting vertical alignment of key preview of number qwerty floating keyboard.
                    positionY = -(float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)) - (float)mKeyHeight + offsetY + floatingAdjust;
                }else{
                    positionY = -(float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)) - (float)mKeyHeight/3 * 4 + offsetY;
                }
            }

            canvas.drawText(TEXTBLOCK_01[offsetId - 1], positionX, positionY, mPaintTextBlock01);
        }

    }

    /** @see android.graphics.drawable.Drawable#drawPreview12KeyTenkey */
    private void drawPreview12KeyTenkey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;

        float offsetY;
        if(mVerticalDisp){
            offsetY = r.getDimensionPixelSize(R.dimen.key_preview_text_padding_vgap);
        } else {
            offsetY = r.getDimensionPixelSize(R.dimen.key_preview_capskey_text_padding_vgap);
        }
        FontMetrics fontMetrics = null;
        int offsetId = mId - KEY_ID_KEYTOP_PREVIEW_OFFSET;

        mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_10key_text_size_en_upper_popup);
        mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_preview_center_text_color));
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setTextAlign(Align.CENTER);

        if (TEXTBLOCK_01[offsetId - 1] != null) {
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            positionX = 0;
            positionY = -(float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)) - (float)mKeyHeight + offsetY;
            canvas.drawText(TEXTBLOCK_01[offsetId - 1], positionX, positionY, mPaintTextBlock01);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawPreviewPeriodkey */
    private void drawPreviewPeriodkey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;
        float offsetY = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_preview_padding_vgap);
        FontMetrics fontMetrics = null;

        int offsetId = 0;

        if(mId ==  KEY_ID_FLICK_PREVIEW_PERIOD){
            offsetId = mId - KEY_ID_FLICK_PREVIEW;
        }else{
            offsetId = mId - KEY_ID_KEYTOP_PREVIEW_OFFSET;
        }

        mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_period_text_size);
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setTextAlign(Align.CENTER);

        mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_period_text_size);
        mPaintTextBlock02.setTextSize(mTextBlock02Size);
        mPaintTextBlock02.setTextAlign(Align.CENTER);

        if (mId == KEY_ID_PREVIEW_PHONE_SYM || mId == KEY_ID_PREVIEW_PHONE_123) {
            mPaintTextBlock01.setColor(mKeySkin.getColor(
                    OpenWnn.getContext(), R.color.key_symkey_preview_text_color));
        } else if (mId == KEY_ID_PREVIEW_PERIOD) {
            mPaintTextBlock01.setColor(mKeySkin.getColor(
                    OpenWnn.getContext(), R.color.key_brackets_preview_text_color));
            mPaintTextBlock02.setColor(mKeySkin.getColor(
                    OpenWnn.getContext(), R.color.key_brackets_preview_text_color));
        } else if (mId == KEY_ID_PREVIEW_QWERTY_PERIOD_FULL
                || mId == KEY_ID_PREVIEW_QWERTY_PERIOD_HALF) {
            mPaintTextBlock01.setColor(mKeySkin.getColor(
                    OpenWnn.getContext(), R.color.key_period_preview_text_color));
            mPaintTextBlock02.setColor(mKeySkin.getColor(
                    OpenWnn.getContext(), R.color.key_period_preview_text_color));
        } else {
            mPaintTextBlock01.setColor(mKeySkin.getColor(
                    OpenWnn.getContext(), R.color.key_12key_space_jp_preview_text_color));
            mPaintTextBlock02.setColor(mKeySkin.getColor(
                    OpenWnn.getContext(), R.color.key_12key_space_jp_preview_text_color));
        }
        if (!KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY.endsWith(mKeySkin.getPackageName())
                && !KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(
                mKeySkin.getPackageName())) {
            setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);
            setTextShadow(mPaintTextBlock02, R.color.key_text_shadow_color);
        }

        if (TEXTBLOCK_01[offsetId - 1] != null) {
            if (mId == KEY_ID_PHONE_SYM) {
                TEXTBLOCK_01[offsetId - 1] = r.getString(R.string.label_phone_symkey_txt);
            }

            fontMetrics = mPaintTextBlock01.getFontMetrics();
            if (mId == KEY_ID_PREVIEW_QWERTY_PERIOD_FULL || mId == KEY_ID_PREVIEW_QWERTY_PERIOD_HALF) {
                positionX = (float)(0 - mQwertyPeriodpreviewGapX);
            } else {
                positionX = 0;
            }
            if (mId == KEY_ID_PREVIEW_PHONE_SYM || mId == KEY_ID_PREVIEW_PHONE_123) {
                positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2 + offsetY);
            } else {
                positionY = (float)(offsetY);
            }

            if (mId == KEY_ID_PREVIEW_QWERTY_PERIOD_FULL
                    || mId == KEY_ID_PREVIEW_QWERTY_PERIOD_HALF
                    || mId == KEY_ID_PREVIEW_PERIOD_KO_KUTEN
                    || mId == KEY_ID_PREVIEW_PERIOD_KO_COMMA) {
                canvas.drawText(PREVIEW_TEXTBLOCK_01[offsetId - 1], positionX, positionY, mPaintTextBlock01);
            } else {
                canvas.drawText(TEXTBLOCK_01[offsetId - 1], positionX, positionY, mPaintTextBlock01);
            }
        }

        if (TEXTBLOCK_02[offsetId - 1] != null) {
            if (mId == KEY_ID_PREVIEW_QWERTY_PERIOD_FULL || mId == KEY_ID_PREVIEW_QWERTY_PERIOD_HALF) {
                positionX = (float)(0 + mQwertyPeriodpreviewGapX);
                canvas.drawText(PREVIEW_TEXTBLOCK_02[offsetId - 1], positionX, positionY, mPaintTextBlock02);
            }
        }
    }

    /** @see android.graphics.drawable.Drawable#drawPreviewEisukanakey */
    private void drawPreviewEisukanakey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;
        float offsetY = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_preview_padding_vgap);
        int offsetId = mId - KEY_ID_KEYTOP_PREVIEW_OFFSET;
        FontMetrics fontMetrics = null;

        mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_eisukana_text_size);
        mTextBlock02Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_eisukana_text_size);
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock02.setTextSize(mTextBlock02Size);
        mPaintTextBlock01.setTextAlign(Align.CENTER);
        mPaintTextBlock02.setTextAlign(Align.CENTER);
        if (!KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY.endsWith(mKeySkin.getPackageName())
                && !KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(
                mKeySkin.getPackageName())) {
            setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);
            setTextShadow(mPaintTextBlock02, R.color.key_text_shadow_color);
        }

        if(mKeySkin.isValid()){
            if (KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_NATURAL.endsWith(mKeySkin.getPackageName())
                    || KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(mKeySkin.getPackageName())) {
                offsetY = offsetY * 4 / 5;
            }
        }
        if (TEXTBLOCK_01[offsetId - 1] != null) {

            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_eisukana_preview_text_color));
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            positionY = (float)(offsetY - mEisukanaLineGapY / 2);
            canvas.drawText(TEXTBLOCK_01[offsetId - 1], positionX, positionY, mPaintTextBlock01);
        }
        if (TEXTBLOCK_02[offsetId - 1] != null) {

            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_eisukana_preview_text_color));
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            positionY = (float) (offsetY + Math.abs(fontMetrics.ascent) + mEisukanaLineGapY / 2);
            canvas.drawText(TEXTBLOCK_02[offsetId - 1], positionX, positionY, mPaintTextBlock02);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawPreview12KeySpaceJpkey */
    private void drawPreview12KeySpaceJpkey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;
        float offsetY = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_preview_padding_vgap);
        int offsetId = mId - KEY_ID_KEYTOP_PREVIEW_OFFSET;
        FontMetrics fontMetrics = null;

        mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_12key_space_jp_text_size);
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_12key_space_jp_preview_text_color));

        if (!KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY.endsWith(mKeySkin.getPackageName())
                && !KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(
                mKeySkin.getPackageName())) {
            setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);
        }
        if(mKeySkin.isValid()){
            if (KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_NATURAL.endsWith(mKeySkin.getPackageName())
                    || KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(mKeySkin.getPackageName())) {
                offsetY = offsetY* 4/5;
            }
        }
        if (TEXTBLOCK_01[offsetId - 1] != null) {

            fontMetrics = mPaintTextBlock01.getFontMetrics();
            mPaintTextBlock01.setTextAlign(Align.CENTER);
            positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2 + offsetY);
            canvas.drawText(TEXTBLOCK_01[offsetId - 1], positionX, positionY, mPaintTextBlock01);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawPreviewCapskey */
    private void drawPreviewCapskey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();
        float offsetY = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_preview_padding_vgap);
        int offsetId = mId - KEY_ID_KEYTOP_PREVIEW_OFFSET;
        if(mId == KEY_ID_FLICK_PREVIEW_DAKUTEN){
            offsetId += KEY_ID_FLICK_PREVIEW;
        }

        FontMetrics fontMetrics = null;

        if( KEY_ID_DAKUTEN != offsetId){

            if(KEY_ID_12KEY_CAPS == offsetId){
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_capskey_select_text_size);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_capskey_select_text_size);
                mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_capskey_unselect_text_size);
                mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_preview_text_color));
                mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_preview_text_color));
                mPaintTextBlock03.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_unselect_text_color));
            }else{
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_capskey_unselect_text_size);
                mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_capskey_select_text_size);
                mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_capskey_select_text_size);
                mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_unselect_text_color));
                mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_preview_text_color));
                mPaintTextBlock03.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_preview_text_color));
            }

            mPaintTextBlock01.setTextSize(mTextBlock01Size);
            mPaintTextBlock02.setTextSize(mTextBlock02Size);
            mPaintTextBlock03.setTextSize(mTextBlock03Size);
            mPaintTextBlock01.setTextAlign(Align.CENTER);
            mPaintTextBlock02.setTextAlign(Align.CENTER);
            mPaintTextBlock03.setTextAlign(Align.CENTER);

            float paddingY = 0;
            if(mVerticalDisp){
                paddingY = r.getDimensionPixelSize(R.dimen.key_preview_text_padding_vgap);
            } else {
                paddingY = r.getDimensionPixelSize(R.dimen.key_preview_capskey_text_padding_vgap);
            }
            if (TEXTBLOCK_02[offsetId - 1] != null) {
                fontMetrics = mPaintTextBlock02.getFontMetrics();
                positionX = 0;
                positionY = -(float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)) - (float)mKeyHeight
                        + paddingY;
                canvas.drawText(TEXTBLOCK_02[offsetId - 1], positionX, positionY, mPaintTextBlock02);
            }

            if (TEXTBLOCK_01[offsetId - 1] != null) {
                fontMetrics = mPaintTextBlock01.getFontMetrics();
                positionX = 0 - capsKeyGap;
                canvas.drawText(TEXTBLOCK_01[offsetId - 1], positionX, positionY, mPaintTextBlock01);
            }

            if (TEXTBLOCK_03[offsetId - 1] != null) {
                fontMetrics = mPaintTextBlock03.getFontMetrics();
                positionX = 0 + capsKeyGap;
                canvas.drawText(TEXTBLOCK_03[offsetId - 1], positionX, positionY, mPaintTextBlock03);
            }

        }else{

            mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_dakuten_left_top_text_size);
            mTextBlock02Size = r.getDimensionPixelSize(R.dimen.key_dakuten_right_top_text_size);
            mTextBlock03Size = r.getDimensionPixelSize(R.dimen.key_dakuten_left_bottom_text_size);
            mTextBlock04Size = r.getDimensionPixelSize(R.dimen.key_dakuten_right_bottom_text_size);
            mTextBlock05Size = r.getDimensionPixelSize(R.dimen.key_dakuten_arrow_text_size);

            mPaintTextBlock01.setTextSize(mTextBlock01Size);
            mPaintTextBlock01.setTextAlign(Align.RIGHT);
            mPaintTextBlock02.setTextSize(mTextBlock02Size);
            mPaintTextBlock02.setTextAlign(Align.RIGHT);
            mPaintTextBlock03.setTextSize(mTextBlock03Size);
            mPaintTextBlock03.setTextAlign(Align.RIGHT);
            mPaintTextBlock04.setTextSize(mTextBlock04Size);
            mPaintTextBlock04.setTextAlign(Align.RIGHT);

            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_preview_text_color));
            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_preview_text_color));
            mPaintTextBlock03.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_preview_text_color));
            mPaintTextBlock04.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_caps_preview_text_color));

            Drawable icon;
            if (!isFloatingShown) {
                if(!mKeySkin.isValid()){
                    icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_dakuten_popup);
                }else{
                    if (!KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(mKeySkin.getPackageName())) {
                        icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_jp_dakuten);
                    } else {
                        icon = mKeySkin.getDrawable(DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_DAKUTEN_POPUP);
                    }
                }
            } else {
                icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_min_dakuten_popup);
            }
            fontMetrics = mPaintTextBlock03.getFontMetrics();

            if (icon != null) {
                positionX = ( - icon.getIntrinsicWidth()) / 2;
                positionY =  (float)(-icon.getIntrinsicHeight()  + Math.abs(fontMetrics.descent)
                        + Math.abs(fontMetrics.ascent) + offsetY);

                canvas.translate(positionX, positionY);
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                icon.draw(canvas);
                canvas.translate(-positionX, -positionY);
            }

            if (TEXTBLOCK_03[offsetId - 1] != null) {
                positionY = (float)(offsetY);
                canvas.drawText(TEXTBLOCK_03[offsetId - 1], positionX, positionY, mPaintTextBlock03);
            }

            if (TEXTBLOCK_04[offsetId - 1] != null) {
                fontMetrics = mPaintTextBlock04.getFontMetrics();
                if (!isFloatingShown) {
                    float fontWidth = mPaintTextBlock04.measureText(TEXTBLOCK_04[offsetId - 1]);
                    if (icon != null) {
                        positionX += (float)(icon.getIntrinsicWidth() + fontWidth);
                    }
                } else {
                    positionX += (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)*3.2);
                }
                canvas.drawText(TEXTBLOCK_04[offsetId - 1], positionX, positionY, mPaintTextBlock04);
            }

            if (TEXTBLOCK_02[offsetId - 1] != null) {
                fontMetrics = mPaintTextBlock02.getFontMetrics();
                positionX += (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2 - mDakutenKeyTopGap);
                if (mId == KEY_ID_PREVIEW_DAKUTEN) {
                    if (mVerticalDisp) {
                        positionY = (float)(- mKeyHeight + mDakutenKeyGap + Math.abs(fontMetrics.descent + fontMetrics.ascent)*2);
                    } else {
                        positionY = (float)(- mKeyHeight/5*6 + mDakutenKeyGap + Math.abs(fontMetrics.descent + fontMetrics.ascent)*2);
                    }
                } else {
                    positionY = (float)(- mKeyHeight/2 + mDakutenKeyGap + Math.abs(fontMetrics.descent + fontMetrics.ascent)*2);
                }
                canvas.drawText(TEXTBLOCK_02[offsetId - 1], positionX, positionY+offsetY, mPaintTextBlock02);
            }

            if (TEXTBLOCK_01[offsetId - 1] != null) {
                fontMetrics = mPaintTextBlock01.getFontMetrics();
                positionX -= (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)*2);
                positionY += (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent));
                canvas.drawText(TEXTBLOCK_01[offsetId - 1], positionX, positionY+offsetY, mPaintTextBlock01);
            }
        }
    }

    /** @see android.graphics.drawable.Drawable#drawPreviewComkey */
    private void drawPreviewComkey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;
        float offsetY = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_preview_padding_vgap);
        int offsetId = mId - KEY_ID_KEYTOP_PREVIEW_OFFSET;

        FontMetrics fontMetrics = null;

        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_QWERTY) {
            mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_qwerty_preview_comkey_text_size);
            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_comkey_preview_text_color));
        } else {
            mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_comkey_text_size);
            mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_comkey_preview_text_color));
        }
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setTextAlign(Align.CENTER);
        if (!KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY.endsWith(mKeySkin.getPackageName())
                && !KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(
                mKeySkin.getPackageName())) {
            setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);
        }
        if (TEXTBLOCK_01[offsetId - 1] != null) {

            fontMetrics = mPaintTextBlock01.getFontMetrics();
            positionX = 0;

            if (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_QWERTY) {
                positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2 + offsetY);
            } else {
                if(mVerticalDisp){
                    positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent) - (float)mKeyHeight / 2  + mComKeyTopVerticalGapY + offsetY);
                }else{
                    positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent) - (float)mKeyHeight / 2  + mComKeyTopGapY + offsetY);
                }
        }

            canvas.drawText(TEXTBLOCK_01[offsetId - 1], positionX, positionY, mPaintTextBlock01);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawPreviewQwertyShiftkeyy */
    private void drawPreviewQwertyShiftkey(Resources r, Canvas canvas) {

        float positionY = 0;
        float positionX = 0;
        float offsetY = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_preview_padding_vgap);
        int offsetId = mId - KEY_ID_KEYTOP_PREVIEW_OFFSET;

        FontMetrics fontMetrics = null;

        mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_comkey_text_size);
        mTextBlock02Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_comkey_text_size);
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock02.setTextSize(mTextBlock02Size);
        if (!KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY.endsWith(mKeySkin.getPackageName())
                && !KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(
                mKeySkin.getPackageName())) {
            setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);
            setTextShadow(mPaintTextBlock02, R.color.key_text_shadow_color);
        }
        float fontWidth = 0;
        positionX = 0;

        if (TEXTBLOCK_01[offsetId - 1] != null) {
           mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_10key_text_color_mode));
           fontMetrics = mPaintTextBlock01.getFontMetrics();
           fontWidth = mPaintTextBlock01.measureText(TEXTBLOCK_01[offsetId - 1]);
           positionX = 0 - fontWidth / 2 - fontWidth;

           if(mVerticalDisp){
               positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent) - (float)mKeyHeight / 2  + mQwertyShiftTopVerticalGapY + offsetY);
           }else{
               positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent) - (float)mKeyHeight / 2  + mQwertyShiftTopGapY + offsetY);
           }

            canvas.drawText(TEXTBLOCK_01[offsetId - 1], positionX, positionY, mPaintTextBlock01);
        }

        if (TEXTBLOCK_02[offsetId - 1] != null) {
            mPaintTextBlock02.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_qwerty_shift_preview_text_color));
            fontMetrics = mPaintTextBlock02.getFontMetrics();
            positionX = positionX + fontWidth;

            if(mVerticalDisp){
                positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent) - (float)mKeyHeight / 2  + mQwertyShiftTopVerticalGapY + offsetY);
            }else{
                positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent) - (float)mKeyHeight / 2  + mQwertyShiftTopGapY + offsetY);
            }

            canvas.drawText(TEXTBLOCK_02[offsetId - 1], positionX, positionY, mPaintTextBlock02);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawEnterkey */
    private void drawEnterkey(Resources r, Canvas canvas) {
        float positionY = 0;
        float positionX = 0;
        FontMetrics fontMetrics = null;

        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        setOneTouchEmojiShown();
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase
                .isNotExpandedFloatingMode();

        boolean isQwerty = (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_QWERTY);
        boolean isPhone = (keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_JA_HALF_PHONE);

        if (isFloatingShown) {
            if (isPhone) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_onehanded_phone_keyboard_func_text_size);
            } else if (isQwerty) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_onehanded_enter_text_size_qwerty_vertical);
            } else {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_onehanded_enter_text_size_10key);
            }
        } else {
            if (isPhone) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_phone_keyboard_func_text_size);
            } else if (isQwerty && mVerticalDisp) {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_enter_text_size_qwerty);
            } else {
                mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_enter_text_size_12key);
            }
        }
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_12key_space_jp_text_color));
        setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);

        String enterkey = TEXTBLOCK_02[mId - 1];
        if (Locale.getDefault().getLanguage().equals(Locale.KOREA.getLanguage())) {
            enterkey = TEXTBLOCK_03[mId - 1];
        } else if (Locale.getDefault().getLanguage().equals(Locale.JAPANESE.getLanguage())) {
            enterkey = TEXTBLOCK_01[mId - 1];
        }

        if (enterkey != null) {
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            mPaintTextBlock01.setTextAlign(Align.CENTER);
            positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent)/2);
            canvas.drawText(enterkey, positionX, positionY, mPaintTextBlock01);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawPreviewEnterkey */
    private void drawPreviewEnterkey(Resources r, Canvas canvas) {
        float positionY = 0;
        float positionX = 0;
        float offsetY = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_preview_padding_vgap);

        FontMetrics fontMetrics = null;

        mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_enter_text_size);
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_12key_enter_jp_preview_text_color));
        if (!KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY.endsWith(
                mKeySkin.getPackageName())
                && !KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(
                mKeySkin.getPackageName())) {
            setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);
        }

        if (mKeySkin.isValid()) {
            if (KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_NATURAL.endsWith(mKeySkin.getPackageName())
                    || KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(mKeySkin.getPackageName())) {
                offsetY = offsetY* 4/5;
            }
        }

        String enterkey = TEXTBLOCK_02[mId - KEY_ID_FLICK_PREVIEW];
        if (Locale.getDefault().getLanguage().equals(Locale.KOREA.getLanguage())) {
            enterkey = TEXTBLOCK_03[mId - KEY_ID_FLICK_PREVIEW];
        } else if (Locale.getDefault().getLanguage().equals(Locale.JAPANESE.getLanguage())) {
            enterkey = TEXTBLOCK_01[mId - KEY_ID_FLICK_PREVIEW];
        }

        if (enterkey != null) {
            fontMetrics = mPaintTextBlock01.getFontMetrics();
            mPaintTextBlock01.setTextAlign(Align.CENTER);
            positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent) / 2 + offsetY);
            canvas.drawText(enterkey, positionX, positionY, mPaintTextBlock01);
        }
    }

    /** @see android.graphics.drawable.Drawable#drawPreviewSymkey */
    private void drawPreviewSymkey(Resources r, Canvas canvas) {

        float offsetY = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_preview_padding_vgap);
        Drawable icon = null;

        if (!mKeySkin.isValid()) {
            if (mId == KEY_ID_FLICK_PREVIEW_SYM_NUM || mId == KEY_ID_PREVIEW_SYM_NUM) {
                icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_sym_popup);
            } else {
                icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_text_popup);
            }
        } else {
            if (KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY.endsWith(
                    mKeySkin.getPackageName())
                    || KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(
                    mKeySkin.getPackageName())) {
                icon = mKeySkin.getDrawable(DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_SYM_POPUP);
            } else {
                if (mId == KEY_ID_FLICK_PREVIEW_SYM_NUM || mId == KEY_ID_PREVIEW_SYM_NUM) {
                    icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_jp_sym_popup);
                } else {
                    icon = mKeySkin.getDrawable(R.drawable.ime_keypad_icon_jp_text_popup);
                }
            }

            if (KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_NATURAL.endsWith(mKeySkin.getPackageName())
                    || KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(mKeySkin.getPackageName())) {
                offsetY = offsetY* 2/3;
            }
        }

        if (icon != null) {
            final int drawableX = ( - icon.getIntrinsicWidth()) / 2;
            final int drawableY = - (icon.getIntrinsicHeight()) / 2 + (int)offsetY;
            canvas.translate(drawableX, drawableY);
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            icon.draw(canvas);
            canvas.translate(-drawableX, -drawableY);
        }

        mTextBlock01Size = r.getDimensionPixelSize(R.dimen.key_sym_text_size);
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_symkey_text_color));
    }

    /** @see android.graphics.drawable.Drawable#drawPreviewKeyModekey */
    private void drawPreviewKeyModekey(Resources r, Canvas canvas) {
        mTextBlock01Size = OpenWnn.getContext().getResources().getDimensionPixelSize(R.dimen.key_12key_space_jp_text_size);
        mPaintTextBlock01.setTextSize(mTextBlock01Size);
        mPaintTextBlock01.setColor(mKeySkin.getColor(OpenWnn.getContext(), R.color.key_12key_mode_preview_text_color));
        if (!KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY.endsWith(
                mKeySkin.getPackageName())
                && !KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(
                mKeySkin.getPackageName())) {
            setTextShadow(mPaintTextBlock01, R.color.key_text_shadow_color);
        }

        int offsetId = 0;
        if (mId == KEY_ID_PREVIEW_KEYMODE) {
            offsetId = mId - KEY_ID_PREVIEW;
        } else {
            offsetId = mId - KEY_ID_FLICK_PREVIEW;
        }

        if (TEXTBLOCK_01[offsetId] != null) {
            float positionX = 0;
            float positionY = 0;
            float offsetY = r.getDimensionPixelSize(R.dimen.key_preview_padding_vgap);
            DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
            if(mKeySkin.isValid() && (keyboard.getKeyboardType() == DefaultSoftKeyboard.KEYBOARD_12KEY)){
                if (KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_NATURAL.endsWith(mKeySkin.getPackageName())
                        || KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(mKeySkin.getPackageName())) {
                    offsetY = offsetY* 4/5;
                }
            }
            FontMetrics fontMetrics = mPaintTextBlock01.getFontMetrics();

            mPaintTextBlock01.setTextAlign(Align.CENTER);
            positionY = (float)(Math.abs(fontMetrics.descent + fontMetrics.ascent) / 2 + offsetY);
            canvas.drawText(TEXTBLOCK_01[offsetId], positionX, positionY, mPaintTextBlock01);
        }
    }

    @Override
    public void draw(Canvas canvas) {

        int stateList[] = getState();
        boolean pressed = false;
        // Check a pressing state.
        for (int state : stateList) {
            if (state == android.R.attr.state_pressed) {
                pressed = true;
                break;
            }
        }

        try {
            if( mId < KEY_ID_NUM_HALF || mId == KEY_ID_ALPHA_MAIL || mId == KEY_ID_ALPHA_URL){
                draw10key(OpenWnn.getContext().getResources(), canvas);
            } else if ((KEY_ID_NUM_HALF <= mId && mId < KEY_ID_SYM)
                        || (KEY_ID_PHONE_NUM <= mId && mId < KEY_ID_PHONE_SYM)){
                drawNumkey(OpenWnn.getContext().getResources(), canvas);
            } else if (KEY_ID_MODE <= mId && mId < KEY_ID_PERIOD){
                drawKeyModekey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId == KEY_ID_SYM || mId == KEY_ID_SYM_SMALL){
                drawSymkey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId == KEY_ID_PERIOD || mId == KEY_ID_PHONE_SYM || mId == KEY_ID_PHONE_123
                        || mId == KEY_ID_QWERTY_FULL_KUTEN || mId == KEY_ID_FLICK_QWERTY_FULL_KUTEN
                        || mId == KEY_ID_QWERTY_PERIOD_FULL || mId == KEY_ID_QWERTY_PERIOD_HALF
                        || mId == KEY_ID_PERIOD_KO_KUTEN || mId == KEY_ID_PERIOD_KO_COMMA){
                drawPeriodkey(OpenWnn.getContext().getResources(), canvas);
            } else if( KEY_ID_12KEY_CAPS == mId || KEY_ID_12KEY_CAPS_LOWER == mId || KEY_ID_DAKUTEN == mId){
                drawCapskey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId == KEY_ID_COM){
                drawComkey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId ==  KEY_ID_EISUKANA){
                drawEisukanakey(OpenWnn.getContext().getResources(), canvas);
            } else if( KEY_ID_12KEY_SPACE_JP == mId){
                draw12KeySpaceJpkey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId == KEY_ID_QWERTY_NUM_SHIFT_1ST
                        || mId == KEY_ID_QWERTY_NUM_SHIFT_2ND){
                drawQwertyShiftkey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId == KEY_ID_12KEY_TEN || mId == KEY_ID_12KEY_PERIOD_COMMA){
                draw12KeyTenkey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId >= KEY_ID_OK && mId <= KEY_ID_PREVIOUS){
                drawEnterkey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId > KEY_ID_QWERTY_KO_KEYMODE && mId <= KEY_ID_QWERTY_NUM_SYMBOL){
                drawQwertykey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId >= KEY_ID_FLICK_PREVIEW_OK && mId <= KEY_ID_FLICK_PREVIEW_PREVIOUS){
                drawPreviewEnterkey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId == KEY_ID_PREVIEW_QWERTY_KO_KEYMODE){
                drawPreviewKeymodeKeyKo(OpenWnn.getContext().getResources(), canvas);
            } else if( (mId >= KEY_ID_FLICK_PREVIEW && mId < KEY_ID_FLICK_PREVIEW_SYM)
                            || (mId >= KEY_ID_FLICK_PREVIEW_12KEY_TEN && mId < KEY_ID_FLICK_PREVIEW_END)){
                drawPreviewkeyFlick(OpenWnn.getContext().getResources(), canvas);
            } else if(mId >= KEY_ID_NAVI_SELECT_PREVIEW && mId < KEY_ID_NAVI_SELECT){
                drawPreviewNavikey(OpenWnn.getContext().getResources(), canvas);
            } else if((mId >= KEY_ID_NAVI_SELECT && mId <= KEY_ID_NAVI_CUT_OFF)
                    || (mId >= KEY_ID_NAVI_SELECT_SPLIT && mId <= KEY_ID_NAVI_PASTE_OFF_SPLIT)){
                drawNavikey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId >= KEY_MODE_CHANGE_EFFECTIVE_OFF_HIRA_B_PREVIEW && mId <= KEY_MODE_CHANGE_INVALID_OFF_NUM_OFF_HIRA_B_PREVIEW){
                drawFlickPopupKeyModekey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId == KEY_ID_FLICK_PREVIEW_PERIOD || mId == KEY_ID_PREVIEW_PERIOD
                    || KEY_ID_PREVIEW_PHONE_SYM == mId || KEY_ID_PREVIEW_PHONE_123 == mId
                    || mId == KEY_ID_PREVIEW_QWERTY_PERIOD_FULL
                    || mId == KEY_ID_PREVIEW_QWERTY_PERIOD_HALF
                    || mId == KEY_ID_PREVIEW_PERIOD_KO_KUTEN
                    || mId == KEY_ID_PREVIEW_PERIOD_KO_COMMA){
                drawPreviewPeriodkey(OpenWnn.getContext().getResources(), canvas);
            } else if( KEY_ID_FLICK_PREVIEW_SYM == mId || KEY_ID_PREVIEW_SYM == mId
                        || KEY_ID_FLICK_PREVIEW_SYM_SMALL == mId || KEY_ID_PREVIEW_SYM_SMALL == mId
                        || KEY_ID_FLICK_PREVIEW_SYM_NUM == mId || KEY_ID_PREVIEW_SYM_NUM == mId) {
                drawPreviewSymkey(OpenWnn.getContext().getResources(), canvas);
            } else if( (mId >= KEY_ID_PREVIEW && mId < KEY_ID_PREVIEW_SYM)
                        || (KEY_ID_PREVIEW_PHONE_NUM <= mId && mId < KEY_ID_PREVIEW_END)){
                drawPreviewkey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId == KEY_ID_PREVIEW_12KEY_TEN || KEY_ID_PREVIEW_12KEY_PERIOD_COMMA == mId){
                drawPreview12KeyTenkey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId == KEY_ID_PREVIEW_EISUKANA || mId == KEY_ID_FLICK_PREVIEW_EISUKANA){
                drawPreviewEisukanakey(OpenWnn.getContext().getResources(), canvas);
            } else if(mId == KEY_ID_FLICK_PREVIEW_12KEY_SPACE_JP || mId == KEY_ID_PREVIEW_12KEY_SPACE_JP){
                drawPreview12KeySpaceJpkey(OpenWnn.getContext().getResources(), canvas);
            } else if( KEY_ID_PREVIEW_12KEY_CAPS == mId || KEY_ID_FLICK_PREVIEW_12KEY_CAPS == mId
                        || KEY_ID_PREVIEW_12KEY_CAPS_LOWER == mId || KEY_ID_FLICK_PREVIEW_12KEY_CAPS_LOWER == mId
                        || KEY_ID_PREVIEW_DAKUTEN == mId || KEY_ID_FLICK_PREVIEW_DAKUTEN == mId){
                drawPreviewCapskey(OpenWnn.getContext().getResources(), canvas);
            } else if( KEY_ID_FLICK_PREVIEW_COM == mId || KEY_ID_PREVIEW_COM == mId){
                drawPreviewComkey(OpenWnn.getContext().getResources(), canvas);
            } else if( KEY_ID_FLICK_QWERTY_NUM_SHIFT_1ST == mId || KEY_ID_FLICK_QWERTY_NUM_SHIFT_2ND == mId
                        || KEY_ID_PREVIEW_QWERTY_NUM_SHIFT_1ST == mId || KEY_ID_PREVIEW_QWERTY_NUM_SHIFT_2ND == mId){
                drawPreviewQwertyShiftkey(OpenWnn.getContext().getResources(), canvas);
            } else if (KEY_ID_QWERTY_KO_KEYMODE == mId) {
                drawKeyModekeyKo(OpenWnn.getContext().getResources(), canvas);
            } else if (mId == KEY_ID_FLICK_PREVIEW_KEYMODE || mId == KEY_ID_PREVIEW_KEYMODE) {
                drawPreviewKeyModekey(OpenWnn.getContext().getResources(), canvas);
            } else if (mId == KEY_ID_LEFT) {
                drawLeftkey(OpenWnn.getContext().getResources(), canvas);
            } else if (mId == KEY_ID_FUNCTION) {
                drawFunctionkey(OpenWnn.getContext().getResources(), canvas);
            } else if (KEY_ID_SYMBOL_SYM == mId) {
                drawSymbolSymkey(OpenWnn.getContext().getResources(), canvas);
            } else if (KEY_MODE_CHANGE_SYMNUM_NUM_B_PREVIEW == mId
                    || KEY_MODE_CHANGE_SYMNUM_SYM_B_PREVIEW == mId) {
                drawFlickPopupKeySymkey(OpenWnn.getContext().getResources(), canvas);
            } else if (mId >= KEY_ID_CHANGE_LANG_TITLE && mId <= KEY_ID_CHANGE_STYLE_FLOATING) {
                drawChangeLanguagePopkey(OpenWnn.getContext().getResources(), canvas);
            } // else {}
        } catch (Exception ex) {
            return;
        }

    }

    /** @see android.graphics.drawable.Drawable#getOpacity */
    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    /** @see android.graphics.drawable.Drawable#setAlpha */
    @Override
    public void setAlpha(int alpha) {
        // Alpha is not supported.
    }

    /** @see android.graphics.drawable.Drawable#setColorFilter */
    @Override
    public void setColorFilter(ColorFilter cf) {
        // Color Filter is not supported.
    }

    /**
     * Set text shadow of key.
     *
     * @param paintTextBlock  The TextPaint for key text.
     * @param color  The shadow color resource Id for key text.
     */
    private void setTextShadow(TextPaint paintTextBlock, int colorId) {
        if(!mKeySkin.isValid() || mKeySkin.getColor(colorId) != 0){
           paintTextBlock.setShadowLayer(KEY_TEXT_SHADOW_RADIUS, 0, 0,
                    mKeySkin.getColor(OpenWnn.getContext(), colorId));
        }
    }

    /**
     * Set OneTouchEmojiShown.
     */
    private void setOneTouchEmojiShown() {

        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        mOneTouchEmojiShown = false;

        if (keyboard instanceof DefaultSoftKeyboardJAJP) {
            mOneTouchEmojiShown = ((DefaultSoftKeyboardJAJP)keyboard).isShownOneTouchEmojiList();
        } else if (keyboard instanceof DefaultSoftKeyboardKorean) {
            mOneTouchEmojiShown = ((DefaultSoftKeyboardKorean)keyboard).isShownOneTouchEmojiList();
        }
    }

    /**
     * Set OneHandedShown.
     */
    private void setOneHandedShown() {

        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
        mOneHandedShown = false;

        if (keyboard != null) {
            mOneHandedShown = ((DefaultSoftKeyboard)keyboard).isOneHandedMode();
        }
    }


    private Drawable modifyIcon(Resources r, Drawable icon){

        if(r == null || icon == null) {
            return null;
        }
        int maxHeight = 0;
        int defaultHeight = 0;
        int setHeight = 0;
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(OpenWnn.getContext());
        if (r.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            maxHeight = r
                    .getDimensionPixelSize(R.dimen.keysize_preference_portrait_max);
            defaultHeight = KeySizeDialogPreference.getKeyHeight(
                    OpenWnn.getContext(), R.dimen.key_height,
                    Configuration.ORIENTATION_PORTRAIT,
                    r.getInteger(R.integer.key_height_default_value));
            setHeight = sharedPref.getInt(
                    KeySizeDialogPreference.KEY_HEIGHT_PORTRAIT_KEY,
                    defaultHeight);
        } else {
            maxHeight = r
                    .getDimensionPixelSize(R.dimen.keysize_preference_landscape_max);
            defaultHeight = KeySizeDialogPreference.getKeyHeight(
                    OpenWnn.getContext(), R.dimen.key_height,
                    Configuration.ORIENTATION_LANDSCAPE,
                    r.getInteger(R.integer.key_height_default_value));
            setHeight = sharedPref.getInt(
                    KeySizeDialogPreference.KEY_HEIGHT_LANDSCAPE_KEY,
                    defaultHeight);
        }
        float scaleRate = (float) setHeight / (float) maxHeight;
        Matrix matrix = new Matrix();
        matrix.setScale(scaleRate, scaleRate);
        final Bitmap tempBitmap = ((BitmapDrawable) (icon)).getBitmap();
        Bitmap scaleBitmap = Bitmap
                .createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(),
                        tempBitmap.getHeight(), matrix, true);

        return new BitmapDrawable(r, scaleBitmap);
    }

    /**
     * Gets the scale ratio of keyboard.
     *
     * @return The scale ratio of keyboard.
     */
    private float getScaleRatio() {
        boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher
                .mInputMethodBase.isNotExpandedFloatingMode();

        float scaleRatio = 1.0f;
        if (isFloatingShown) {
            DefaultSoftKeyboard keyboard
                    = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
            KeyboardView keyboardView = keyboard.getKeyboardView();
            if (keyboardView != null) {
                Keyboard currentKeyboard = keyboardView.getKeyboard();
                if (currentKeyboard != null) {
                    scaleRatio = currentKeyboard.getScaleWidth();
                }
            }
        }
        return scaleRatio;
    }

}