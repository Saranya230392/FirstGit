/**
 * @file
 *  [拡張]擬似候補部定義
 *
 *   擬似候補取得を使用する為の外部関数宣言を行う
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2010 All Rights Reserved.
 */

/************************************************/
/*           インクルードファイル               */
/************************************************/
#ifndef _EX_GIJI_H_
#define _EX_GIJI_H_
#include "nj_lib.h"
#include "nj_dicif.h"


/************************************************/
/* 辞書頻度定義                                 */
/************************************************/
#define NJ_DEFAULT_FREQ_DIC_PROGRAM_H_HIGH    0      /**< デフォルト辞書頻度::擬似辞書[変換用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_PROGRAM_H_BASE    0      /**< デフォルト辞書頻度::擬似辞書[変換用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_PROGRAM_Y_HIGH    0      /**< デフォルト辞書頻度::擬似辞書[予測用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_PROGRAM_Y_BASE    10     /**< デフォルト辞書頻度::擬似辞書[予測用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_PROGRAM_M_HIGH    0      /**< デフォルト辞書頻度::擬似辞書[形態素解析用]-最高-   */
#define NJ_DEFAULT_FREQ_DIC_PROGRAM_M_BASE    10     /**< デフォルト辞書頻度::擬似辞書[形態素解析用]-底上げ- */


/************************************************/
/* 擬似候補情報定義                             */
/************************************************/
/**
 * 擬似候補タイプの最大数定義
 *
 * 最小値：７、最大値：250
 */
#define NJ_GIJISET_MAX          250

/**
 * 擬似候補セット定義構造体
 */
typedef struct {
    NJ_INT16  count;                /* type[]に格納している登録数 */
    NJ_UINT8  type[NJ_GIJISET_MAX]; /* 擬似候補タイプ */
} NJ_GIJISET;

/* 日付/時間確保用領域 */
typedef struct {
    NJ_INT16   year;
    NJ_INT16   month;
    NJ_INT16   day;
    NJ_INT16   dayofweek;
    NJ_INT16   hour;
    NJ_INT16   minute;
    NJ_INT16   second;
    NJ_UINT8   leap;
    NJ_INT8   adjust;
} NJ_TIME_ST;

/**
 * 擬似候補種別の定義
 *
 *   生成できる擬似候補タイプは、NJ_TYPE_HIRAGANA以降とみなします。
 *   また、定義値は連続している必要があります。
 */
/*    擬似候補タイプ定義    */
#define NJ_TYPE_HAN_EIJI_CAP             5 /**< 擬似候補タイプ：半角 英字 (先頭のみ大文字) */
#define NJ_TYPE_ZEN_EIJI_CAP             6 /**< 擬似候補タイプ：全角 英字 (先頭のみ大文字) */
#define NJ_TYPE_HAN_EIJI_UPPER           7 /**< 擬似候補タイプ：半角 英字 (全て大文字) */
#define NJ_TYPE_ZEN_EIJI_UPPER           8 /**< 擬似候補タイプ：全角 英字 (全て大文字) */
#define NJ_TYPE_HAN_EIJI_LOWER           9 /**< 擬似候補タイプ：半角 英字 (全て小文字) */
#define NJ_TYPE_ZEN_EIJI_LOWER          10 /**< 擬似候補タイプ：全角 英字 (全て小文字) */
#define NJ_TYPE_HAN_SUUJI_COMMA         13 /**< 擬似候補タイプ：半角 数字 (コンマ区切り) */
#define NJ_TYPE_HAN_TIME_HH             14 /**< 擬似候補タイプ：半角 時間 (H時) */
#define NJ_TYPE_HAN_TIME_MM             15 /**< 擬似候補タイプ：半角 時間 (M分) */
#define NJ_TYPE_HAN_TIME_HM             16 /**< 擬似候補タイプ：半角 時間 (H時M分) */
#define NJ_TYPE_HAN_TIME_HMM            17 /**< 擬似候補タイプ：半角 時間 (H時MM分) */
#define NJ_TYPE_HAN_TIME_HHM            18 /**< 擬似候補タイプ：半角 時間 (HH時M分) */
#define NJ_TYPE_HAN_TIME_HHMM           19 /**< 擬似候補タイプ：半角 時間 (HH時MM分) */
#define NJ_TYPE_HAN_TIME_HMM_SYM        20 /**< 擬似候補タイプ：半角 時間 (H:MM) */
#define NJ_TYPE_HAN_TIME_HHMM_SYM       21 /**< 擬似候補タイプ：半角 時間 (HH:MM) */
#define NJ_TYPE_HAN_DATE_YYYY           22 /**< 擬似候補タイプ：半角 日付 (YYYY年) */
#define NJ_TYPE_HAN_DATE_MM             23 /**< 擬似候補タイプ：半角 日付 (M月) */
#define NJ_TYPE_HAN_DATE_DD             24 /**< 擬似候補タイプ：半角 日付 (D日) */
#define NJ_TYPE_HAN_DATE_MD             25 /**< 擬似候補タイプ：半角 日付 (M月D日) */
#define NJ_TYPE_HAN_DATE_MDD            26 /**< 擬似候補タイプ：半角 日付 (M月DD日) */
#define NJ_TYPE_HAN_DATE_MMD            27 /**< 擬似候補タイプ：半角 日付 (MM月D日) */
#define NJ_TYPE_HAN_DATE_MMDD           28 /**< 擬似候補タイプ：半角 日付 (MM月DD日) */
#define NJ_TYPE_HAN_DATE_MD_SYM         29 /**< 擬似候補タイプ：半角 日付 (M/D) */
#define NJ_TYPE_HAN_DATE_MDD_SYM        30 /**< 擬似候補タイプ：半角 日付 (M/DD) */
#define NJ_TYPE_HAN_DATE_MMD_SYM        31 /**< 擬似候補タイプ：半角 日付 (MM/D) */
#define NJ_TYPE_HAN_DATE_MMDD_SYM       32 /**< 擬似候補タイプ：半角 日付 (MM/DD) */
#define NJ_TYPE_HAN_SUUJI_YOMI          33 /**< 擬似候補タイプ：半角 数字読み文字→数字 */
#define NJ_TYPE_ZEN_SUUJI_YOMI          34 /**< 擬似候補タイプ：全角 数字読み文字→数字 */
#define NJ_TYPE_ZEN_KANSUUJI_YOMI       35 /**< 擬似候補タイプ：全角 数字読み文字→漢数字 */
#define NJ_TYPE_ZEN_KANSUUJI_KURAI_YOMI 36 /**< 擬似候補タイプ：全角 数字読み文字→漢数字(位) */

#define NJ_TYPE_HAN_YOMI2DATE_INIT             242 /* 半角 読み日付/時間変換 初期化処理    */
#define NJ_TYPE_HAN_YOMI2DATE_YYYY             243 /* 半角 読み日付変換 (YYYY年)           */
#define NJ_TYPE_HAN_YOMI2DATE_NENGO            244 /* 半角 読み日付変換 (平成(昭和)YY年)   */
#define NJ_TYPE_HAN_YOMI2DATE_MM               245 /* 半角 読み日付変換 (MM月)             */
#define NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM         246 /* 半角 読み日付変換 (MM/DD)            */
#define NJ_TYPE_HAN_YOMI2DATE_MMDD             247 /* 半角 読み日付変換 (MM月DD日)         */
#define NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM_WEEK    248 /* 半角 読み日付変換 (MM/DD(曜日))      */
#define NJ_TYPE_HAN_YOMI2DATE_MMDD_WEEK        249 /* 半角 読み日付変換 (MM月DD日(曜日))   */
#define NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM         250 /* 半角 読み時間変換 (HH:MM)            */
#define NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM_AMPM    251 /* 半角 読み時間変換 (AM(PM)HH:MM       */
#define NJ_TYPE_HAN_YOMI2TIME_HHMM             252 /* 半角 読み時間変換 (HH時MM分)         */
#define NJ_TYPE_HAN_YOMI2TIME_HHMM_12H         253 /* 半角 読み時間変換 (午前(後)HH時MM分) */
#define NJ_TYPE_DAY_OF_WEEK                    254 /* 読み曜日変換 (X曜日)                 */
#define NJ_TYPE_DAY_OF_WEEK_SYM                255 /* 読み曜日変換 ((曜日))                */
/**
 * 擬似候補タイプの最大値を定義
 *
 *   Awnnの標準擬似候補についての最大値を定義します。
 *   引数の入力値チェックに使用しています。
 *   Awnnで擬似候補タイプを拡張した場合は、この最大値も
 *   変更してください。
 */
#define NJ_TYPE_LIMIT_VALUE  (NJ_TYPE_ZEN_KANSUUJI_KURAI_YOMI)
/*
 * アプリ拡張用の擬似候補タイプの定義範囲
 *
 * 擬似候補タイプがこの範囲の場合、アプリカスタマイズ
 * 関数を呼び出す。
 */
/** アプリ拡張用の擬似候補タイプの定義範囲(最小値) */
#define NJ_GIJI_TYPE_CUST_MIN    170
/** アプリ拡張用の擬似候補タイプの定義範囲(最大値) */
#define NJ_GIJI_TYPE_CUST_MAX    255

/************************************************
 *   テーブル定義                   
 ************************************************/
// 擬似候補セットの標準設定(かな入力モード時の予測候補).
static NJ_GIJISET GijiSet = {
    16,
    {
		NJ_TYPE_ZEN_KANSUUJI_KURAI_YOMI,
		NJ_TYPE_ZEN_KANSUUJI_YOMI,
		NJ_TYPE_HIRAGANA,
		NJ_TYPE_KATAKANA,
		NJ_TYPE_HANKATA,
		NJ_TYPE_HAN_YOMI2DATE_INIT,
		NJ_TYPE_HAN_YOMI2DATE_YYYY,
		NJ_TYPE_HAN_YOMI2DATE_MM,
		NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM,
		NJ_TYPE_HAN_YOMI2DATE_MMDD,
		NJ_TYPE_HAN_YOMI2DATE_MMDD_WEEK,
		NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM,
		NJ_TYPE_HAN_YOMI2TIME_HHMM,
		NJ_TYPE_HAN_YOMI2TIME_HHMM_12H,
		NJ_TYPE_HAN_SUUJI_YOMI,
		NJ_TYPE_ZEN_SUUJI_YOMI,
		0
    }
};


/***********************************************************************
 * 辞書インタフェース extern 宣言
 ***********************************************************************/
NJ_EXTERN NJ_INT16 njex_giji_dic(NJ_CLASS* iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE* message);
NJ_EXTERN NJ_INT16 njex_get_giji(NJ_CLASS *iwnn, NJ_TIME_ST *time_info, NJ_CHAR *yomi, NJ_UINT8 type, NJ_RESULT *result);


#endif /*_EX_GIJI_H_*/
