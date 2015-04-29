/**
 * @file
 *  [拡張] iWnn標準擬似候補作成辞書 (UTF16/SJIS版)
 *
 * @author
 * (C) OMRON SOFTWARE Co., Ltd. 2008-2012 All Rights Reserved.
 */
#include "ex_giji.h"
#include "nj_ext.h"
#include "nj_dic.h"
#include "njd.h"
#include "nj_err.h"
#ifdef NJ_OPT_UTF16
#include "ex_gtabU.h"
#else /* NJ_OPT_UTF16 */
#include "ex_gtab.h"
#endif /* NJ_OPT_UTF16 */

#ifdef WIN32
#include <windows.h>
#else
/* device*/
#include <time.h>
#include <sys/time.h>
#endif


/************************************************/
/*              extern  宣  言                  */
/************************************************/

/************************************************
 *           カスタマイズ設定項目
 ************************************************/


/************************************************/
/*         define  宣  言 (以下AWnnより流用)      */
/************************************************/
/*
 * 文字種ごとの文字長定義
 */
#ifdef NJ_OPT_UTF16
/** 半角文字長 (UTF16) */
#define NJG_HAN_MOJI_LEN  1
/** 全角文字長 (UTF16) */
#define NJG_ZEN_MOJI_LEN  1

#else /* NJ_OPT_UTF16 */
/** 半角文字長 (SJIS) */
#define NJG_HAN_MOJI_LEN  1
/** 全角文字長 (SJIS) */
#define NJG_ZEN_MOJI_LEN  2
#endif /* NJ_OPT_UTF16 */

/**
 * 読みテーブルのうち、読み記号テーブルを表すビット定義
 */
#define NJ_GIJI_SYM_TBL 0x8000

/** 数字候補の出力桁数(設定桁数以上は候補対象外とする) */
#define NJG_DIGIT_SUUJI_LIMIT 17

/************************************************/
/*                                              */
/* 時間・日付擬似候補定義                          */
/*                                              */
/************************************************/
/* 時間・日付判定のための範囲定義 */
#define NJG_NO_CHK      (-1)      /**< 時間・日付範囲定義: なし */
#define NJG_HOUR_MIN    0         /**< 時間・日付範囲定義: 最小：時 */
#define NJG_HOUR_MAX    26        /**< 時間・日付範囲定義: 最大：時 */
#define NJG_MIN_MIN     0         /**< 時間・日付範囲定義: 最小：分 */
#define NJG_MIN_MAX     59        /**< 時間・日付範囲定義: 最大：分 */
#define NJG_YEAR_MIN    1         /**< 時間・日付範囲定義: 最小：年 */
#define NJG_YEAR_MAX    9999      /**< 時間・日付範囲定義: 最大：年 */
#define NJG_MONTH_MIN   1         /**< 時間・日付範囲定義: 最小：月 */
#define NJG_MONTH_MAX   12        /**< 時間・日付範囲定義: 最大：月 */
#define NJG_DAY_MIN     1         /**< 時間・日付範囲定義: 最小：日 */
#define NJG_DAY_MAX     31        /**< 時間・日付範囲定義: 最大：日 */

/* 内部用マクロ定義 */
/**
 * 時の範囲かどうか確認
 *
 * @param[in]      x  任意の数値
 * @retval         0  時でない
 * @retval        !0  時
 */
#define NJG_IS_HOUR_RANGE(x)  ((((x)>=NJG_HOUR_MIN)  && ((x)<=NJG_HOUR_MAX))  ? 1 : 0)
/**
 * 分の範囲かどうか確認
 *
 * @param[in]      x  任意の数値
 * @retval         0  分でない
 * @retval        !0  分
 */
#define NJG_IS_MIN_RANGE(x)   ((((x)>=NJG_MIN_MIN)   && ((x)<=NJG_MIN_MAX))   ? 1 : 0)
/**
 * 月の範囲かどうか確認
 *
 * @param[in]      x  任意の数値
 * @retval         0  月でない
 * @retval        !0  月
 */
#define NJG_IS_MONTH_RANGE(x) ((((x)>=NJG_MONTH_MIN) && ((x)<=NJG_MONTH_MAX)) ? 1 : 0)
/**
 * 日の範囲かどうか確認
 *
 * @param[in]      x  任意の数値
 * @retval         0  日でない
 * @retval        !0  日
 */
#define NJG_IS_DAY_RANGE(x)   ((((x)>=NJG_DAY_MIN)   && ((x)<=NJG_DAY_MAX))   ? 1 : 0)


/************************************************/
/*              マ ク ロ 宣 言                  */
/************************************************/
/**
 * 読み文字テーブルの要素番号から、読み文字が「記号(ひらがな)」を判定する
 *
 * @param[in] x 読み文字テーブルの要素番号(NJ_UINT16)
 *
 * @retval 1 読み文字が「記号(ひらがな)」
 * @retval 0 読み文字が「ひらがな」
 */
#define NJG_IS_GIJI_SYM_TBL(x)                  \
    ((((x) & NJ_GIJI_SYM_TBL) != 0) ? 1 : 0)

/**
 * 読み文字 ひらがなテーブルから、指定した要素番号に対応する
 * キーアサイン番号とトグル数を取得する
 *
 * @param[in]   x  読み文字テーブルの要素番号(NJ_UINT16)
 *
 * @return  対象配列要素のキーアサイン番号(左8bit)とトグル数(右8bit) (NJ_UINT16)
 */
#define NJG_GET_HIRA_DATA(x)                                            \
    ((NJ_UINT16)((giji_yomi_hira_tbl[(x)].num==NJ_NUM_NON) ? (0) : ((giji_yomi_hira_tbl[(x)].num << 8) | ((giji_yomi_hira_tbl[(x)].toggle & 0xff)))))


/**
 * 読み文字 記号(ひらがな)テーブルから、指定した要素番号に対応する
 * キーアサイン番号とトグル数を取得する
 *
 * @param[in] x  読み文字テーブルの要素番号(NJ_UINT16)
 *
 * @return 対象配列要素のキーアサイン番号(左8bit)とトグル数(右8bit) (NJ_UINT16)
 */
#define NJG_GET_SYM_DATA(x)                                             \
    ((NJ_UINT16)((giji_yomi_sym_tbl[(x)].num==NJ_NUM_NON) ? (0) : ((giji_yomi_sym_tbl[(x)].num << 8) | ((giji_yomi_sym_tbl[(x)].toggle & 0xff)))))



/**
 * 読みテーブルからキー番号を取り出す。
 * キーアサイン情報は giji_yomi_hira_tbl[] から取り出す。
 *
 * @param[in] x 読みテーブル要素番号(njg_get_yomi_table関数で取得できた番号) (NJ_UINT16)
 *
 * @attention
 *  xは正当なインデックスであることがわかっていなければ使用できない。
 *
 * @retval NJ_NUM_NON     キーアサイン設定されていない。
 * @retval NJ_NUM_NON以外  キー番号
 */
#define NJG_GET_NUM_HIRA(x)                                             \
    ((NJ_UINT16)((giji_yomi_hira_tbl[(x)].num==NJ_NUM_NON) ? (NJ_NUM_NON) : (giji_yomi_hira_tbl[(x)].num)))

/**
 * 読みテーブルからキー番号を取り出す。
 * キーアサイン情報は giji_yomi_sym_tbl[] から取り出す。
 *
 * @param[in] x 読みテーブル要素番号(njg_get_yomi_table関数で取得できた番号) (NJ_UINT16)
 *
 * @attention
 *  xは正当なインデックスであることがわかっていなければ使用できない。
 *
 * @retval NJ_NUM_NON     キーアサイン設定されていない。
 * @retval NJ_NUM_NON以外  キー番号
 */
#define NJG_GET_NUM_SYM(x)                                              \
    ((NJ_UINT16)((giji_yomi_sym_tbl[(x)].num==NJ_NUM_NON) ? (NJ_NUM_NON) : (giji_yomi_sym_tbl[(x)].num)))

/**
 * 読みテーブルからキー番号を取り出す。
 * キーアサイン情報は giji_yomi_hira_tbl[], giji_yomi_sym_tbl[] から取り出す。
 *
 * @param[in] x 読みテーブル要素番号(njg_get_yomi_table関数で取得できた番号) (NJ_UINT16)
 *
 * @attention
 *  xは正当なインデックスであることがわかっていなければ使用できない。
 *
 * @retval NJ_NUM_NON     キーアサイン設定されていない。
 * @retval NJ_NUM_NON以外  キー番号
 */
#define NJG_GET_NUM_YOMI(x)                                             \
    ((NJ_UINT16)((NJG_IS_GIJI_SYM_TBL(x)==1) ? (NJG_GET_NUM_SYM((x)^NJ_GIJI_SYM_TBL)) : (NJG_GET_NUM_HIRA(x))))

/**
 * 読みテーブルからトグル数を取り出す。
 * キーアサイン情報は giji_yomi_hira_tbl[] から取り出す。
 *
 * @param[in] x 読みテーブル要素番号(njg_get_yomi_table関数で取得できた番号) (NJ_UINT16)
 *
 * @attention
 *  xは正当なインデックスであることがわかっていなければ使用できない。
 *
 * @retval NJ_NUM_NON     キーアサイン設定されていない。
 * @retval NJ_NUM_NON以外  トグル数
 */
#define NJG_GET_TOGGLE_HIRA(x)                                          \
    ((NJ_UINT16)((giji_yomi_hira_tbl[(x)].num==NJ_NUM_NON) ? (NJ_NUM_NON) : (giji_yomi_hira_tbl[(x)].toggle)))

/**
 * 読みテーブルからトグル数を取り出す。
 * キーアサイン情報は giji_yomi_sym_tbl[] から取り出す。
 *
 * @param[in] x 読みテーブル要素番号(njg_get_yomi_table関数で取得できた番号) (NJ_UINT16)
 *
 * @attention
 *  xは正当なインデックスであることがわかっていなければ使用できない。
 *
 * @retval NJ_NUM_NON     キーアサイン設定されていない。
 * @retval NJ_NUM_NON以外  トグル数
 */
#define NJG_GET_TOGGLE_SYM(x)                                           \
    ((NJ_UINT16)((giji_yomi_sym_tbl[(x)].num==NJ_NUM_NON) ? (NJ_NUM_NON) : (giji_yomi_sym_tbl[(x)].toggle)))

/**
 * 読みテーブルからトグル数を取り出す。
 * キーアサイン情報は giji_yomi_hira_tbl[], giji_yomi_sym_tbl[] から取り出す。
 *
 * @param[in] x 読みテーブル要素番号(njg_get_yomi_table関数で取得できた番号) (NJ_UINT16)
 *
 * @attention
 *  xは正当なインデックスであることがわかっていなければ使用できない。
 *
 * @retval NJ_NUM_NON     キーアサイン設定されていない。
 * @retval NJ_NUM_NON以外  トグル数
 */
#define NJG_GET_TOGGLE_YOMI(x)                                          \
    ((NJ_UINT16)((NJG_IS_GIJI_SYM_TBL(x)==1) ? (NJG_GET_TOGGLE_SYM((x)^NJ_GIJI_SYM_TBL)) : (NJG_GET_TOGGLE_HIRA(x))))


/**
 * NJ_CHAR/NJ_UINT8型の2バイト文字を、NJ_UINT16型へ変換。
 *
 * @attention
 *  あらかじめ、2バイト文字であることがわかっていないと使用できない。
 *
 * @param[in] x  変換対象文字列(2byte) (NJ_CHAR* or NJ_UINT8)
 *
 * @return 変換後データ (NJ_UINT16)
 *
 */
#define NJG_CONV_TO_WCHAR(x)                                            \
    ((NJ_UINT16)(((*((NJ_UINT8*)(x)) << 8) & 0xff00) | (*(((NJ_UINT8*)(x))+1) & 0xff)))


/**
 * チェック対象文字列の先頭バイトからデータタイプをチェックする。
 *
 * @note  UTF16/SJIS 対応とする。
 *
 * @param[in] x  チェック対象文字列(1byte分) (NJ_CHAR)
 *
 * @retval 1  2バイトデータ
 * @retval 0  1バイトデータ
 */
#ifdef NJ_OPT_UTF16
/** UTF16版 */
#define NJG_IS_WCHAR(x)  1
#else /* NJ_OPT_UTF16 */
/** SJIS版 */
#define NJG_IS_WCHAR(x)                                                 \
    (((((x) >= 0x81) && ((x) <= 0x9f)) || (((x) >= 0xe0) && ((x) <= 0xfc))) ? 1 : 0)
#endif/* NJ_OPT_UTF16 */


/**
 * 引数の日付が実際に存在しうる日付か判定する
 *
 * @param[in] x  日付(月) (NJ_UINT8)
 * @param[in] y  日付(日) (NJ_UINT8)
 *
 * @retval  1  日付が存在
 * @retval  0  日付が存在しない
 */
#define NJG_IS_EXIST_DATE(x, y) ( (((y) >= 1) && ((y) <= giji_last_day[(x)-1])) ? 1 : 0)

/**
 * NJ_CHARの全角１文字(2byte文字)をコピーする。
 *
 * @note SJISの場合：1byteずつ、2byteをコピーする。
 * @note UTF16の場合：NJ_CHAR１文字をコピーする。
 *
 * @param[in] to    コピー先 (NJ_CHAR*)
 * @param[in] from  コピー元 (NJ_CHAR*)
 */
#ifdef NJ_OPT_UTF16
#define NJG_COPY_W_CHAR(to, from)               \
    { *(to) = *(from); }
#else /* NJ_OPT_UTF16 */
#define NJG_COPY_W_CHAR(to, from)                       \
    { (to)[0] = (from)[0]; (to)[1] = (from)[1]; }
#endif /* NJ_OPT_UTF16 */

/**
 * NJ_CHARにテーブルに定義された16bit文字をコピーする。
 *
 * @note Big Endianで2byte格納する。
 *
 * @param[in] to    コピー先 (NJ_CHAR*)
 * @param[in] from  コピー元 (NJ_UINT16/NJ_INT16)
 */
#define NJG_COPY_INT16_TO_CHAR(to, from)                                \
    { ((NJ_UINT8*)(to))[0] = (NJ_UINT8)(((from) >> 8) & 0x00ff);        \
        ((NJ_UINT8*)(to))[1] = (NJ_UINT8)((from) & 0x00ff); }

/**
 * 年月日より年号(昭和 or 平成)を判定する
 *
 * @note 1989/1/7以降であれば平成と判断する。
 *
 * @param[in] x 西暦年(NJ_UINT16)
 * @param[in] y 月(NJ_UINT16)
 * @param[in] z 日(NJ_UINT16)
 *
 * @retval 1 年号は「平成」
 * @retval 0 年号は「昭和」
 */
#define NJG_IS_HEISEI_NENGO(x, y, z)                                                   \
    ((((x) > NJ_NENGO_LIMIT_SHOWA_YEAR) ||                                             \
      (((x) == NJ_NENGO_LIMIT_SHOWA_YEAR) && ((y) > NJ_NENGO_LIMIT_SHOWA_MONTH)) ||    \
      (((x) == NJ_NENGO_LIMIT_SHOWA_YEAR) && ((y) == NJ_NENGO_LIMIT_SHOWA_MONTH) &&    \
       ((z) > NJ_NENGO_LIMIT_SHOWA_DAY))) ? 1 : 0)

/***********************************************************************
 * 構造体宣言
 ***********************************************************************/
/**
 * 旧NJ_ENV構造体の必要データ定義
 *
 * @note 旧擬似候補作成処理を流用するため、旧NJ_ENVの中の作業領域を抽出。
 */
typedef struct {
    /* 擬似候補作成時のワーク用領域 */
    NJ_UINT16 kan_data[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];   /**< 漢数字変換情報 */
    NJ_CHAR   gijiwork[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];   /**< 変換ワーク領域 */
    NJ_INT16 kan_len;                       /**< kan_dataのデータ長 */
    NJ_INT16 gw_len;                        /**< gijiworkの文字長 */

    /** 汎用ワーク領域 */
    NJ_CHAR tmp[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
} PART_OF_NJ_ENV;


/***********************************************************************
 * 辞書インタフェース プロトタイプ宣言
 ***********************************************************************/

/***********************************************************************
 * 内部関数 プロトタイプ宣言 (AWnnより流用)
 ***********************************************************************/
static NJ_INT16 get_giji_result(PART_OF_NJ_ENV *iwnn, NJ_TIME_ST *time_info, NJ_DIC_HANDLE rule,
                                 NJ_CHAR *yomi, NJ_UINT16 len,
                                 NJ_UINT8 type, NJ_RESULT *giji);
static NJ_INT16 get_giji_all_result(PART_OF_NJ_ENV *iwnn, NJ_TIME_ST *time_info,
                                     NJ_UINT16 gfpos, NJ_UINT16 gbpos, NJ_UINT16 gnbpos,
                                     NJ_CHAR *yomi, NJ_UINT16 len,
                                     NJ_RESULT *giji, NJ_UINT8 gtype);
static NJ_INT16 get_giji_candidate(PART_OF_NJ_ENV *iwnn, NJ_TIME_ST *time_info, NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size);
static NJ_INT16 gethiraString(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size);
static NJ_INT16 getKatakanaString(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size);
static NJ_INT16 getHanKataString(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size);
static NJ_INT16 getEijiString (NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size,
                               const NJ_UINT16 *giji_tbl, const NJ_UINT16 *giji_tbl_cap);
static NJ_INT16 getSuujiString(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size,
                               const NJ_UINT16 *giji_tbl);
static NJ_INT16 getHanSuujiCommaString(PART_OF_NJ_ENV *iwnn, NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size);
static NJ_INT16 getHanTimeDate (NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size, const NJ_CONVERT_TBL *ctbl);
static NJ_INT16 getSuujiYomi(PART_OF_NJ_ENV *iwnn, NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size,
                             const NJ_UINT16 *giji_tbl);
static NJ_INT16 getZenksuujiKuraiYomiString(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size);

static NJ_INT16 get_giji_hiragana(NJ_UINT16 gfpos,
                                  NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_katakana(NJ_UINT16 gfpos,
                                  NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_hankata(NJ_UINT16 gfpos,
                                 NJ_UINT16 gbpos,NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_eiji_length (NJ_UINT16 gfpos,  NJ_UINT16 gbpos, NJ_CHAR *yomi,
                                      NJ_UINT16 len, NJ_RESULT *p, NJ_UINT8 gtype,
                                      const NJ_UINT16 *giji_tbl, const NJ_UINT16 *giji_tbl_cap);
static NJ_INT16 get_giji_suuji(NJ_UINT16 gfpos, NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len,
                               NJ_RESULT *p, NJ_UINT8 gtype, const NJ_UINT16 *giji_tbl);
static NJ_INT16 get_giji_han_time_hh(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                     NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_time_mm(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                     NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_time_hm(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                     NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_time_hmm(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                      NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_time_hhm(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                      NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_time_hhmm(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                       NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_time_hmm_sym(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                          NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_time_hhmm_sym(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                           NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_date_yyyy(PART_OF_NJ_ENV *iwnn, 
                                       NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                       NJ_CHAR *yomi, NJ_UINT16 len,
                                       NJ_RESULT *p);
static NJ_INT16 get_giji_han_date_mm(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                     NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_date_dd(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                     NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_date_md(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                     NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_date_mdd(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                      NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_date_mmd(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                      NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_date_mmdd(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                       NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_date_md_sym(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                         NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_date_mdd_sym(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                          NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_date_mmd_sym(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                          NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_han_date_mmdd_sym(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                           NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_INT16 get_giji_suuji_yomi(PART_OF_NJ_ENV *iwnn, 
                                    NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                    NJ_CHAR *yomi, NJ_UINT16 len,
                                    NJ_RESULT *p, NJ_UINT16 gtype,
                                    const NJ_UINT16 *giji_tbl);
static NJ_INT16 get_giji_zen_ksuuji_kurai(PART_OF_NJ_ENV *iwnn, NJ_UINT16 gfpos,
                                          NJ_UINT16 gbpos, NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p);
static NJ_UINT16 njg_get_han_time_date( PART_OF_NJ_ENV *iwnn, const NJ_CONVERT_TBL *ctbl, 
                                        NJ_CHAR *pY, NJ_UINT16 len );
static NJ_INT16 njg_get_ksuuji_ketapos(NJ_CHAR *yomi, NJ_UINT8 *yomi_len);
static NJ_INT16 njg_check_ksuuji_array(NJ_CHAR *suuji_strings);
static NJ_INT16 njg_get_ksuuji_index_from_yomi( PART_OF_NJ_ENV *iwnn, 
                                                NJ_CHAR *yomi, NJ_UINT16 len );
static NJ_INT16 njg_convert_ksuuji_data(NJ_UINT16 *kanpt, NJ_INT16 kan_len,
                                        NJ_CHAR *suuji_strings, NJ_INT16 suuji_size);
static NJ_INT16 njg_get_ksuuji_data_from_index( PART_OF_NJ_ENV *iwnn );
static NJ_INT16 njg_get_yomi_table(NJ_UINT16 yomi, NJ_UINT16 *elm_num);
static NJ_INT16 njg_cust_get_giji (PART_OF_NJ_ENV *iwnn, NJ_TIME_ST *time_info, NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                      NJ_UINT16 gnbpos, NJ_UINT8 *yomi, NJ_UINT16 len,
                                      NJ_RESULT *giji, NJ_UINT8 gtype);
static NJ_INT16 njg_cust_get_giji_candidate (PART_OF_NJ_ENV *iwnn, NJ_TIME_ST *time_info, NJ_WORD *word, 
                                                NJ_UINT8 *candidate, NJ_UINT16 size);
/* 読み日付/時間変換処理 */
static void njg_cust_get_time(NJ_TIME_ST *time_st, NJ_UINT8 *yomi, NJ_UINT16 len);
static NJ_INT16 njg_cust_get_yomi2datetime( NJ_TIME_ST *time_st, NJ_UINT8 gtype, NJ_UINT16 *klen );
static void njg_cust_adjust_time_st( NJ_TIME_ST *time_st, NJ_UINT16 gctype );
static NJ_INT16 njg_cust_get_result_len( NJ_TIME_ST *time_st, NJ_TYPE2CONVERT_TBL *cptr);
static NJ_UINT16 njg_cust_calc_len( NJ_INT16 data, NJ_UINT16 base, NJ_UINT16 keta);
static NJ_UINT16 njg_cust_get_yomi2datestring( NJ_TIME_ST *time_st, NJ_UINT8 gtype, NJ_UINT8 *candidate, NJ_UINT16 size );
static NJ_INT16 njg_cust_get_result_string(NJ_TIME_ST *time_st, NJ_TYPE2CONVERT_TBL *cptr, NJ_UINT8 *candidate, NJ_UINT16 size);
static NJ_UINT16 njg_cust_calc_string( NJ_INT16 data, NJ_UINT16 base, NJ_UINT16 keta, NJ_UINT8 *out, NJ_UINT8 flg);

/***********************************************************************/

/**
 * 擬似辞書 辞書インタフェース
 *
 * @param[in,out] iwnn      iWnn内部情報(通常は参照のみ)
 * @param[in]     request   iWnnからの処理要求
 *                          - NJG_OP_SEARCH：初回検索
 *                          - NJG_OP_SEARCH_NEXT：次候補検索
 *                          - NJG_OP_GET_WORD_INFO：単語情報取得
 *                          - NJG_OP_GET_STROKE：読み文字列取得
 *                          - NJG_OP_GET_STRING：候補文字列取得
 *                          - NJG_OP_LEARN：単語学習
 * @param[in,out] message   iWnn←→擬似辞書間でやり取りする情報
 *
 * @retval >=0 正常終了(requestの種類によって規定)
 * @retval <0  以上終了
 */
NJ_EXTERN NJ_INT16 njex_giji_dic(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE *message) {

    NJ_INT16 ret;
    NJ_UINT8 type;
    NJ_INT16 i;
    NJ_RESULT rlt;
    NJ_UINT16 len;
    PART_OF_NJ_ENV tmp;
    NJ_GIJISET *giji_set;
    NJ_TIME_ST *time_info;

    switch (request) {
    case NJG_OP_SEARCH:
    case NJG_OP_SEARCH_NEXT:
    case NJG_OP_GET_WORD_INFO:
        if ((message->dic_idx < NJ_MAX_DIC) &&
            (iwnn->dic_set.dic[message->dic_idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] == NULL)) {
            /*
             * 頻度学習領域がNULLの場合は、デフォルトの擬似セットを使う。
             * また、message->dic_idxの値が不正になることはないが、念のため
             * 条件として入れている。
             */
            giji_set = &GijiSet;
            time_info = NULL;
        } else {
            /* 頻度学習領域に設定されたものをNJ_GIJISETとして扱う */
            giji_set = (NJ_GIJISET *)(iwnn->dic_set.dic[message->dic_idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
            if (iwnn->dic_set.dic[message->dic_idx].add_info[1] != NULL) {
                time_info = (NJ_TIME_ST *)(iwnn->dic_set.dic[message->dic_idx].add_info[1]);
            } else {
                time_info = NULL;
            }
        }
        break;

    case NJG_OP_GET_STROKE:
    case NJG_OP_GET_STRING:
    case NJG_OP_GET_ADDITIONAL:
    case NJG_OP_LEARN:
    case NJG_OP_DEL_WORD:
        /* giji_setを使用しない場合は、message->word->stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]を設定 */
        giji_set = message->word->stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT];
        time_info = message->word->stem.loc.add_info[1];
        break;

    case NJG_OP_UNDO_LEARN:
    case NJG_OP_ADD_WORD:
    default:
        /* giji_setを使用しない場合は、NULLを設定 */
        giji_set = NULL;
        time_info = NULL;
        break;
    }

    switch (request) {
    case NJG_OP_SEARCH:
        if ((message->condition->operation != NJ_CUR_OP_COMP)
            || (message->condition->mode != NJ_CUR_MODE_FREQ)) {
            /* 正引き完全一致頻度順以外は非対応 */
            message->location->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        if ((iwnn->njc_mode == 2) ||
            ((iwnn->njc_mode == 0) && (message->condition->fzkconnect == 1) && (iwnn->environment.type == NJ_ANALYZE_COMPLETE)) ||
            ((iwnn->njc_mode == 0) && (message->condition->fzkconnect == 1) && (iwnn->environment.type == NJ_ANALYZE_COMPLETE_HEAD)) ||
            ((iwnn->njc_mode == 0) && (iwnn->environment.type == NJ_ANALYZE_COMPLETE_GIJI))) {
            /*
             * 擬似候補検索状態ZEN_MODE_GIJI_PROCで、
             * AWnn擬似候補は全候補取得処理のときのみ使用
             */

            for (i = 0; i < giji_set->count; i++) {
                type = giji_set->type[i];
                if (message->condition->fzkconnect == 1) {
                    /* 付属語解析ありの場合 */
                    if (!((type <= NJ_TYPE_HANKATA) || 
                         ((type >= NJ_TYPE_HAN_SUUJI_YOMI) &&
                          (type <= NJ_TYPE_ZEN_KANSUUJI_KURAI_YOMI))) ) {
                        /*  片仮名、平仮名、読み数字以外は作成しない */
                        continue;
                    }
                }

                /* 擬似候補を作ってみる */
                ret = get_giji_result(&tmp, time_info, message->dicset->rHandle[0],
                                      message->condition->yomi, message->condition->ylen,
                                      type, &rlt);
                if (ret > 0) {
                    /* 擬似候補作成可能 */
                    message->location->loct.current = i; /* 擬似セット番号をIDとする */
                    message->location->loct.current_info = 0x10;
                    message->location->cache_freq = NJ_GET_FREQ_FROM_STEM(&(rlt.word));
                    message->location->loct.status = NJ_ST_SEARCH_READY;
                    return 1;
                }
            }
        }
        /* 擬似候補作成不可 */
        message->location->loct.status = NJ_ST_SEARCH_END;
        return 0;

    case NJG_OP_SEARCH_NEXT:
        /* 次に作成可能な擬似候補を求める */
        for (i = (NJ_UINT8)(message->location->loct.current + 1); i < giji_set->count; i++) {
            type = giji_set->type[i];
            if (message->condition->fzkconnect == 1) {
                /* 付属語解析ありの場合 */
                if (((type > NJ_TYPE_HANKATA) && (type < NJ_TYPE_HAN_SUUJI_YOMI)) ||
                    (type >= NJ_GIJI_TYPE_CUST_MIN)) {
                    /*  片仮名、平仮名、読み数字以外は作成しない */
                    continue;
                }
            }

            /* 擬似候補を作ってみる */
            ret = get_giji_result(&tmp, time_info, message->dicset->rHandle[0],
                                  message->condition->yomi, message->condition->ylen,
                                  type, &rlt);
            if (ret > 0) {
                /* 擬似候補作成可能 */
                message->location->loct.current = i; /* 擬似セット番号をIDとする */
                message->location->loct.current_info = 0x10;
                message->location->cache_freq = NJ_GET_FREQ_FROM_STEM(&(rlt.word));
                message->location->loct.status = NJ_ST_SEARCH_READY;
                return 1;
            }
        } 
        /* 擬似候補作成不可 */
        message->location->loct.status = NJ_ST_SEARCH_END;
        return 0;

    case NJG_OP_GET_WORD_INFO:
        /* 擬似候補タイプを取得して擬似候補を作成する */
        if ((NJ_UINT8)message->location->loct.current >= giji_set->count) {
            /* 念のため擬似候補の範囲をチェック。ココを通ることはない */
            return -1; /*NCH*/
        }
        type = giji_set->type[(NJ_UINT8)message->location->loct.current];
        ret  = get_giji_result(&tmp, time_info, message->dicset->rHandle[0],
                              message->word->yomi, NJ_GET_YLEN_FROM_STEM(message->word),
                              type, &rlt);
        if (ret > 0) {
            /* 擬似候補作成可能なはず */
            *(message->word) = rlt.word;
            message->word->stem.loc = message->location->loct;
            return 0;
        } else {
            /* ここに来ることはないが、念のため */
            return -1; /*NCH*/
        }

    case NJG_OP_GET_STROKE:
        /* 読み文字列を作成する */
        len = NJ_GET_YLEN_FROM_STEM(message->word);
        if (message->stroke_size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
            /* 格納バッファ不足の場合はエラー */
            return -1;
        }
        nj_strncpy(message->stroke, message->word->yomi, len);
        message->stroke[len] = NJ_CHAR_NUL;
        return (NJ_INT16)len;

    case NJG_OP_GET_STRING:
        /* 表記文字列を作成する */
        ret = get_giji_candidate(&tmp, time_info, message->word, message->string, message->string_size);
        return ret;

    case NJG_OP_GET_ADDITIONAL:
    case NJG_OP_LEARN:
    case NJG_OP_UNDO_LEARN:
    case NJG_OP_ADD_WORD:
    case NJG_OP_DEL_WORD:
        return 0;
    default:
        break;
    }
    return -1; /* エラー */ /*NCH*/
}

/***********************************************************************
 * 以下、AWnnより流用
 ***********************************************************************/
/**
 * AWnnV2.3互換 擬似候補取得API
 *
 * 指定した読みに対する擬似候補を取得。
 * AWnnV2.3との互換性保持のため作成。
 *
 * @param[in]   iwnn     解析情報クラス
 * @param[in]   time_info 時間情報
 * @param[in]   yomi     擬似候補作成対象の読み文字列
 * @param[in]   type     擬似候補のタイプ<br>
 *                       NJ_TYPE_HIRAGANA : 入力文字列<br>
 *                       NJ_TYPE_KATAKANA : 入力文字列を全角カタカナに変換<br>
 *                       NJ_TYPE_HANKATA  : 入力文字列を半角カタカナに変換<br>
 *                       NJ_TYPE_ZEN_SUUJI: 入力文字列を全角数字に変換<br>
 *                       NJ_TYPE_HAN_SUUJI: 入力文字列を半角数字に変換
 * @param[out]   result  作成した擬似候補（上位で領域を用意すること）
 *
 * @retval 1  擬似候補作成完了
 * @retval 0  指定タイプの擬似候補が作成できない為、NJ_TYPE_HIRAGANA候補作成
 * @retval <0 エラー(resultがNULLの場合を含む)
 */
NJ_EXTERN NJ_INT16 njex_get_giji(NJ_CLASS *iwnn, NJ_TIME_ST *time_info, NJ_CHAR *yomi, NJ_UINT8 type,
                                 NJ_RESULT *result) {
    NJ_UINT16 len;        /* 読み文字列バイト長 */
    NJ_INT16 ret;        /* 戻り値 */
    NJ_UINT16 bnpos;        /* "数字"後品詞の品詞番号 */
    NJ_DIC_SET *dics;
    PART_OF_NJ_ENV tmp;

    /*
     * 入力パラメータ チェック
     */
    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_GIJI, NJ_ERR_PARAM_ENV_NULL); /*NCH*/
    }
    dics = &(iwnn->dic_set);

    if (dics->rHandle[NJ_MODE_TYPE_HENKAN] == NULL) {
        /* 第1引数(dics)でルール辞書ハンドルがNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_GIJI, NJ_ERR_NO_RULEDIC);
    } else {
        bnpos = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_SUUJI_B);/* 後品詞：数字 の品詞番号取得 */

        if (bnpos == 0) {
            /* 品詞番号取得失敗 */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_GIJI, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
        }
    }
    if (result == NULL) {
        /* 第4引数(result)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_GIJI, NJ_ERR_PARAM_RESULT_NULL); /*NCH_FB*/
    }
    if (yomi == NULL) {
        /* 第2引数(yomi)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_GIJI, NJ_ERR_PARAM_YOMI_NULL); /*NCH_FB*/
    }
    /* 読み文字列長取得 */
    len = nj_strlen(yomi);
    if (len == 0) {
        /* 第2引数(yomi)が空文字列の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_GIJI, NJ_ERR_PARAM_YOMI_NULL); /*NCH_FB*/
    }
    if (len > NJ_MAX_LEN) {
        /* 第2引数(yomi)の読み文字列長が、NJ_MAX_LENを超過している場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_GIJI, NJ_ERR_YOMI_TOO_LONG);
    }


    /* 引数の擬似候補タイプの範囲チェック */
    if ( (type < NJ_TYPE_HIRAGANA)
         || ((type > NJ_TYPE_LIMIT_VALUE) && (type < NJ_GIJI_TYPE_CUST_MIN)) ) {
        /* 第3引数(type)に規定外の値が指定されていた場合 */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_GIJI, NJ_ERR_PARAM_TYPE_INVALID);
    }

    /* 指定擬似候補取得 */
    ret = get_giji_result(&tmp, time_info, dics->rHandle[NJ_MODE_TYPE_HENKAN], yomi, len, type, result);
    if (ret < 0) {
        /* get_giji_resultが異常終了 */
        return ret; /*NCH*/
    }

    /*
     * 指定された擬似候補タイプの擬似候補が作成できなかった
     *  (例) 入力がひらがな文字以外で、タイプにNJ_TYPE_HIRAGANA以外を
     *       指定した場合など。
     */
    if (ret == 0) {
        /* NJ_TYPE_HIRAGANAで作成した結果を返す */
        ret = get_giji_result(&tmp, time_info, dics->rHandle[NJ_MODE_TYPE_HENKAN], yomi, len,
                              NJ_TYPE_HIRAGANA, result);
        if (ret < 0) {
            /* get_giji_resultが異常終了 */
            return ret; /*NCH_FB*/
        }
        if (ret == 0) {
            /*
             * NJ_TYPE_HIRAGANAの擬似候補は必ず作成できる
             * よって、作成できない場合は内部エラーとする
             */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_GIJI, NJ_ERR_DIC_BROKEN); /*NCH*/
        }
        /*
         * typeに、NJ_TYPE_HAN_SUUJI, NJ_TYPE_ZEN_SUUJI, NJ_TYPE_HAN_SUUJI_COMMA,
         * NJ_TYPE_HAN_SUUJI_YOMI, NJ_TYPE_ZEN_SUUJI_YOMI, NJ_TYPE_ZEN_KANSUUJI_YOMI,
         * NJ_TYPE_ZEN_KANSUUJI_KURAI_YOMI のいずれかが指定された
         * 場合は、強制的に処理結果の後品詞を"数字"にする
         */
        if (   (type == NJ_TYPE_HAN_SUUJI)               || (type == NJ_TYPE_ZEN_SUUJI)
            || (type == NJ_TYPE_HAN_SUUJI_COMMA)         || (type == NJ_TYPE_HAN_SUUJI_YOMI)
            || (type == NJ_TYPE_ZEN_SUUJI_YOMI)          || (type == NJ_TYPE_ZEN_KANSUUJI_YOMI)
            || (type == NJ_TYPE_ZEN_KANSUUJI_KURAI_YOMI)) {
            NJ_SET_BPOS_TO_STEM(&result->word, bnpos);
        }

        /*
         * オペレーションIDの更新
         */
        result->operation_id = (NJ_OP_ENV | NJ_FUNC_GET_GIJI | NJ_TYPE_GIJI_BIT | NJ_DIC_GIJI);

        return 0;
    }
    
    /*
     * typeが、NJ_TYPE_ZEN_SUUJIまたは、NJ_TYPE_HAN_SUUJIが指定された
     * 場合は、強制的に処理結果の後品詞を"数字"にする
     */
    if ((type == NJ_TYPE_ZEN_SUUJI) || (type == NJ_TYPE_HAN_SUUJI)) {
        NJ_SET_BPOS_TO_STEM(&result->word, bnpos);
    }

    /*
     * オペレーションIDの更新
     */
    result->operation_id = (NJ_OP_ENV | NJ_FUNC_GET_GIJI | NJ_TYPE_GIJI_BIT | NJ_DIC_GIJI);
    
    result->word.stem.loc.type   = NJ_DIC_H_TYPE_PROGRAM;
    result->word.stem.loc.handle = (NJ_DIC_HANDLE)njex_giji_dic;
    for (ret = 0; ret < NJ_MAX_EXT_AREA; ret++) {
        result->word.stem.loc.ext_area[ret] = NULL;
    }
    for (ret = 0; ret < NJ_MAX_ADDITIONAL_INFO; ret++) {
        result->word.stem.loc.add_info[ret] = NULL;
    }
    result->word.stem.loc.add_info[1] = (NJ_VOID*)time_info;

    /* get_giji_resultで作成できた場合は、戻り値１ */
    return 1;
}

/**
 * 擬似候補を作成する。
 *
 * @param[in]  iwnn  作業領域
 * @param[in]  time_info 時間情報
 * @param[in]  rule  ルール辞書ハンドル
 * @param[in]  yomi  読み文字列
 * @param[in]  len   yomi 文字長
 * @param[in]  type  擬似候補タイプ
 * @param[out] giji  擬似候補格納領域
 *
 * @retval 1  指定された擬似候補が作成した
 * @retval 0  指定された擬似候補は作成できない
 * @retval <0 エラー
 */
static NJ_INT16 get_giji_result(PART_OF_NJ_ENV *iwnn, NJ_TIME_ST *time_info, NJ_DIC_HANDLE rule,
                                NJ_CHAR *yomi, NJ_UINT16 len,
                                NJ_UINT8 type, NJ_RESULT *giji) {

    NJ_UINT16 fpos, bpos, bnpos;
    NJ_INT16 num;


    /*
     * 引数チェック
     */
    if (rule == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_GIJI_RESULT, NJ_ERR_NO_RULEDIC); /*NCH_FB*/
    }
    if ((yomi == NULL) || (len == 0)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_GIJI_RESULT, NJ_ERR_PARAM_YOMI_NULL); /*NCH_FB*/
    }
    if (giji == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_GIJI_RESULT, NJ_ERR_PARAM_RESULT_NULL); /*NCH_FB*/
    }

    /* 引数の擬似候補タイプの範囲チェック */
    if ( (type < NJ_TYPE_HIRAGANA)
         || ((type > NJ_TYPE_LIMIT_VALUE) && (type < NJ_GIJI_TYPE_CUST_MIN)) ) {

        return 0; /*NCH_FB*/
    }

    /*
     * 擬似候補の品詞番号をルール辞書から取得する
     * 数字擬似候補に関しては、繋がり予測の精度向上の為
     * 後品詞に"数字"後品詞を使用する
     */
    fpos = njd_r_get_hinsi(rule, NJ_HINSI_GIJI_F);  /* 前品詞：擬似 の品詞番号取得 */
    bpos = njd_r_get_hinsi(rule, NJ_HINSI_GIJI_B);  /* 後品詞：擬似 の品詞番号取得 */
    bnpos = njd_r_get_hinsi(rule, NJ_HINSI_SUUJI_B);/* 後品詞：数字 の品詞番号取得 */

    if ((fpos == 0) || (bpos == 0) || (bnpos == 0)) {
        /* 擬似 品詞番号取得失敗 */
        return 0; /*NCH_FB*/
    }

    /* num = 1:候補取得成功  0:失敗 */
    num = get_giji_all_result(iwnn, time_info, fpos, bpos, bnpos, yomi, len, giji, type);

    return num;
}

/**
 * 引数で渡された品詞で擬似候補を生成する
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  time_info 時間情報
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  gnbpos 擬似品詞(数字) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] giji   擬似候補格納バッファ(指定した擬似候補タイプのみ準備する)
 * @param[in]  gtype  擬似候補タイプ
 *
 * @retval 1  指定された擬似候補を取得した
 * @retval 0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_all_result(PART_OF_NJ_ENV *iwnn, NJ_TIME_ST *time_info,
                                    NJ_UINT16 gfpos, NJ_UINT16 gbpos, NJ_UINT16 gnbpos,
                                    NJ_CHAR *yomi, NJ_UINT16 len,
                                    NJ_RESULT *giji, NJ_UINT8 gtype) {
    NJ_INT16  ret;
    NJ_UINT16 type;
    NJ_INT16  i;
    NJ_CHAR convert_yomi[NJ_MAX_LEN+1] = {NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,
                              NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,
                              NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,
                              NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,
                              NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL};

    for (i = 0; i < len; i++)
        {
        convert_yomi[i] = *(yomi + i);
        }

    /* 初期化 */
    ret = 0;
    giji->operation_id = 0;
    giji->word.yomi                  = NULL;
    giji->word.stem.info1            = 0;
    giji->word.stem.info2            = 0;
    giji->word.stem.hindo            = 0;
    giji->word.stem.type             = NJ_TYPE_UNDEFINE;
    giji->word.stem.info3            = 0;
    giji->word.fzk.info1             = 0;
    giji->word.fzk.info2             = 0;
    giji->word.fzk.hindo             = 0;

    giji->word.stem.loc.handle       = NULL;
    giji->word.stem.loc.type         = NJ_DIC_H_TYPE_NORMAL;
    for (i = 0; i < NJ_MAX_EXT_AREA; i++) {
        giji->word.stem.loc.ext_area[i] = NULL;
    }
    for (i = 0; i < NJ_MAX_ADDITIONAL_INFO; i++) {
        giji->word.stem.loc.add_info[i] = NULL;
    }
    giji->word.stem.loc.current      = 0;
    giji->word.stem.loc.top          = 0;
    giji->word.stem.loc.bottom       = 0;
    giji->word.stem.loc.current_cache= 0;
    giji->word.stem.loc.current_info = 0x10;  /* num=1, offset = 0    */
    giji->word.stem.loc.status       = NJ_ST_SEARCH_NO_INIT;
    giji->word.stem.loc.attr         = 0x00000000;
    for (i = 0; i < NJ_MAX_PHR_CONNECT; i++) {
        giji->word.stem.loc.relation[i] = 0;
    }

    /* 入力データの読み文字列の文字種をチェック */
    type = nje_check_string(yomi, len);

    switch (gtype) {
    case NJ_TYPE_HIRAGANA:
        /* ひらがな（無変換）タイプ */
        if ((type == NJ_TYPE_HAN_SUUJI)               || (type == NJ_TYPE_ZEN_SUUJI)
            || (type == NJ_TYPE_HAN_SUUJI_COMMA)         || (type == NJ_TYPE_HAN_SUUJI_YOMI)
            || (type == NJ_TYPE_ZEN_SUUJI_YOMI)          || (type == NJ_TYPE_ZEN_KANSUUJI_YOMI)
            || (type == NJ_TYPE_ZEN_KANSUUJI_KURAI_YOMI)) {
            /* 入力文字列が数字文字のみで構成されている場合、後品詞を”数字”とする */
            ret = get_giji_hiragana(gfpos, gnbpos, yomi, len, giji);
        } else {
            /* その他の場合は、後品詞を”擬似”とする */
            ret = get_giji_hiragana(gfpos, gbpos, yomi, len, giji);
        }
        break;
        
    case NJ_TYPE_KATAKANA:
        /* 全角カタカナタイプ */
        ret = get_giji_katakana(gfpos, gbpos, yomi, len, giji);
        break;
        
    case NJ_TYPE_HANKATA:
        /* 半角カタカナタイプ */
        ret = get_giji_hankata(gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_EIJI_CAP:
        /* 半角英字(先頭のみ大文字)タイプ */
        ret = get_giji_eiji_length(gfpos,  gbpos, yomi, len, giji, gtype,
                                   giji_han_eiji_lower_tbl,
                                   giji_han_eiji_upper_tbl);
        break;

    case NJ_TYPE_ZEN_EIJI_CAP:
        /* 全角英字(先頭のみ大文字)タイプ */
        ret = get_giji_eiji_length(gfpos,  gbpos, yomi, len, giji, gtype,
                                   giji_zen_eiji_lower_tbl,
                                   giji_zen_eiji_upper_tbl);
        break;

    case NJ_TYPE_HAN_EIJI_UPPER:
        /* 半角英字(全て大文字)タイプ */
        ret = get_giji_eiji_length(gfpos,  gbpos, yomi, len, giji, gtype,
                                   giji_han_eiji_upper_tbl,
                                   NULL);
        break;

    case NJ_TYPE_ZEN_EIJI_UPPER:
        /* 全角英字(全て大文字)タイプ */
        ret = get_giji_eiji_length(gfpos,  gbpos, yomi, len, giji, gtype,
                                   giji_zen_eiji_upper_tbl,
                                   NULL);
        break;

    case NJ_TYPE_HAN_EIJI_LOWER:
        /* 半角英字(全て小文字)タイプ */
        ret = get_giji_eiji_length(gfpos,  gbpos, yomi, len, giji, gtype,
                                   giji_han_eiji_lower_tbl,
                                   NULL);
        break;

    case NJ_TYPE_ZEN_EIJI_LOWER:
        /* 全角英字(全て小文字)タイプ */
        ret = get_giji_eiji_length(gfpos,  gbpos, yomi, len, giji, gtype,
                                   giji_zen_eiji_lower_tbl,
                                   NULL);
        break;

    case NJ_TYPE_HAN_SUUJI:
        /* 半角数字タイプ */
        ret = get_giji_suuji(gfpos, gnbpos, yomi, len, giji, gtype,
                             &giji_suuji_tbl[NJG_SUUJI_TBL_HAN][0]);
        break;

    case NJ_TYPE_ZEN_SUUJI:
        /* 全角数字タイプ */
        ret = get_giji_suuji(gfpos, gnbpos, yomi, len, giji, gtype,
                             &giji_suuji_tbl[NJG_SUUJI_TBL_ZEN][0]);
        break;

    case NJ_TYPE_HAN_SUUJI_COMMA:
        /* 半角 数字 (コンマ区切り) */
        ret = get_giji_suuji(gfpos, gnbpos, yomi, len, giji, gtype,
                             &giji_suuji_tbl[NJG_SUUJI_TBL_HAN][0]);
        break;

    case NJ_TYPE_HAN_TIME_HH:
        /* 半角時間(H時)タイプ */
        ret = get_giji_han_time_hh(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_TIME_MM:
        /* 半角時間(M分)タイプ */
        ret = get_giji_han_time_mm(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_TIME_HM:
        /* 半角時間(H時M分)タイプ */
        ret = get_giji_han_time_hm(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_TIME_HMM:
        /* 半角時間(H時MM分)タイプ */
        ret = get_giji_han_time_hmm(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_TIME_HHM:
        /* 半角時間(HH時M分)タイプ */
        ret = get_giji_han_time_hhm(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_TIME_HHMM:
        /* 半角時間(HH時MM分)タイプ */
        ret = get_giji_han_time_hhmm(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_TIME_HMM_SYM:
        /* 半角時間(H:MM分)タイプ */
        ret = get_giji_han_time_hmm_sym(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_TIME_HHMM_SYM:
        /* 半角時間(HH:MM分)タイプ */
        ret = get_giji_han_time_hhmm_sym(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_DATE_YYYY:
        /* 日付(YYYY年)タイプ */
        ret = get_giji_han_date_yyyy(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_DATE_MM:
        /* 半角日付(M月)タイプ */
        ret = get_giji_han_date_mm(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_DATE_DD:
        /* 半角日付(D日)タイプ */
        ret = get_giji_han_date_dd(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_DATE_MD:
        /* 半角日付(M月D日)タイプ */
        ret = get_giji_han_date_md(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_DATE_MDD:
        /* 半角日付(M月DD日)タイプ */
        ret = get_giji_han_date_mdd(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_DATE_MMD:
        /* 半角日付(MM月D日)タイプ */
        ret = get_giji_han_date_mmd(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_DATE_MMDD:
        /* 半角日付(MM月DD日)タイプ */
        ret = get_giji_han_date_mmdd(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_DATE_MD_SYM:
        /* 半角日付(M/D)タイプ */
        ret = get_giji_han_date_md_sym(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_DATE_MDD_SYM:
        /* 半角日付(M/DD)タイプ */
        ret = get_giji_han_date_mdd_sym(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_DATE_MMD_SYM:
        /* 半角日付(MM/D)タイプ */
        ret = get_giji_han_date_mmd_sym(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_DATE_MMDD_SYM:
        /* 半角日付(MM/DD)タイプ */
        ret = get_giji_han_date_mmdd_sym(iwnn, gfpos, gbpos, yomi, len, giji);
        break;

    case NJ_TYPE_HAN_SUUJI_YOMI:
        /* 数字読み文字→半角数字タイプ */
        ret = get_giji_suuji_yomi(iwnn, gfpos, gnbpos, yomi, len, giji, gtype,
                                  &giji_suuji_tbl[NJG_SUUJI_TBL_HAN][0]);
        break;

    case NJ_TYPE_ZEN_SUUJI_YOMI:
        /* 数字読み文字→全角数字タイプ */
        ret = get_giji_suuji_yomi(iwnn, gfpos, gnbpos, yomi, len, giji, gtype,
                                  &giji_suuji_tbl[NJG_SUUJI_TBL_ZEN][0]);
        break;

    case NJ_TYPE_ZEN_KANSUUJI_YOMI:
        /* 数字読み文字→漢数字タイプ */
        ret = get_giji_suuji_yomi(iwnn, gfpos, gnbpos, yomi, len, giji, gtype,
                                  &giji_suuji_tbl[NJG_SUUJI_TBL_KAN][0]);
        break;

    case NJ_TYPE_ZEN_KANSUUJI_KURAI_YOMI:
        /* 数字読み文字→漢数字(位)タイプ */
        ret = get_giji_zen_ksuuji_kurai(iwnn, gfpos, gnbpos, yomi, len, giji);
        break;

    default:
        /************************************************/
        /* 擬似候補 カスタマイズ処理                    */
        /*   NJ_RESULTを生成する処理を拡張することが    */
        /*   できる。                                   */
        /************************************************/
        if ((gtype >= (NJ_UINT8) NJ_GIJI_TYPE_CUST_MIN) 
            && (gtype <= (NJ_UINT8) NJ_GIJI_TYPE_CUST_MAX))
        {
            ret = njg_cust_get_giji (iwnn, time_info, gfpos, gbpos, gnbpos, (NJ_UINT8*)yomi, len, giji, gtype);
        }
        break;
    }
    return ret;
}

/**
 * 擬似候補取得処理（ひらがな・無変換）
 *
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval 1  指定された擬似候補を取得した
 * @retval 0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_hiragana(NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                  NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p) {


    p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
    p->word.yomi = yomi;
    NJ_SET_YLEN_TO_STEM(&p->word, len);
    NJ_SET_KLEN_TO_STEM(&p->word, len);
    NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
    NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
    NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HIRAGANA);
    NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

    return 1;
}

/**
 * 擬似候補取得処理（全角カタカナ）
 *
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval 1  指定された擬似候補を取得した
 * @retval 0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_katakana(NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                  NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p) {

    NJ_CHAR   *pY;      /* 読みのアドレス */
    NJ_UINT16 len0;     /* 擬似候補文字長 */
    NJ_UINT16 cnt = 0;  /* 確認用ループカウンター */
    NJ_UINT16 elm;      /* 読みの読み文字テーブル上の要素番号 */
    NJ_INT16  ret;      /* 読みが読み文字テーブルに存在するかを示す */
                        /* 0:存在 負数:存在せず */
    NJ_UINT16 kana;
    NJ_UINT16 prev_kana;


    /* 変換後の文字長(len0)取得処理 */
    pY = yomi;
    len0 = 0;
    prev_kana = 0x0000;

    while (cnt < len) {
#ifndef NJ_OPT_UTF16
        /* 読み文字長を計算 */
        if (NJG_IS_WCHAR(*pY)) {
            /* 読みが2byteの場合 */
#endif /* !NJ_OPT_UTF16 */
 
            /* 読みが読み文字テーブルに存在するか判定 */
            ret = njg_get_yomi_table(NJG_CONV_TO_WCHAR(pY), &elm);
            
            if (ret == 0) {
                /* 読みが読み文字テーブルに存在 */
                if (NJG_IS_GIJI_SYM_TBL(elm)) {
                    /* 読みが記号 */
                    /* 最上位bitを元に戻す */
                    elm ^= (NJ_UINT16)NJ_GIJI_SYM_TBL;
                    kana = giji_katakana_sym_tbl[elm];
                    if (kana == 0) {
                        return 0; /*NCH_FB*/
                    } else if ((prev_kana == NJG_KATAKANA_U)
                               && (kana == NJG_KATAKANA_DAKUON)) {
                        /* 「う゛」は「ヴ」に変換するため、
                         * 表記文字列を１文字分減らす
                         */
                        len0 -= NJG_ZEN_MOJI_LEN; /*NCH_FB*/
                    } else {
                        /* 処理なし */
                    }
                    prev_kana = kana;
                } else {
                    /* 読みがひらがな */
                    kana = giji_katakana_tbl[elm];
                    if (kana == 0) {
                        return 0; /*NCH_FB*/
                    }
                    prev_kana = kana;
                }
            } else {
                /* 読みが読み文字テーブルに存在しないため、候補無し */
                return 0;
            }
            len0 += NJG_ZEN_MOJI_LEN;
            pY += NJG_ZEN_MOJI_LEN;
            cnt += NJG_ZEN_MOJI_LEN;

#ifndef NJ_OPT_UTF16
        } else {
            /* 読みが1byteである場合は候補無し */
            return 0;
        }
#endif /* !NJ_OPT_UTF16 */
    }

    /*
     * もし、解析完了時に最大表記文字列長を超過している場合
     * 全角カタカナの疑似候補は作成しない。
     * 読みに半角カタカナがふくまれると表記長が長くなる場合がある。
     */
    if (len0 > NJ_MAX_RESULT_LEN) {
        return 0; /*NCH_FB*/
    }
    p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
    p->word.yomi = yomi;
    NJ_SET_YLEN_TO_STEM(&p->word, len);
    NJ_SET_KLEN_TO_STEM(&p->word, len0);
    NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
    NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
    NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_KATAKANA);
    NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

    return 1;
}

/**
 * 擬似候補取得処理（半角カタカナ）
 *
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_hankata(NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                 NJ_CHAR *yomi, NJ_UINT16 len, NJ_RESULT *p) {

    NJ_CHAR  *pY;   /* 読みのアドレス */
    NJ_UINT16 len0; /* 擬似候補文字長 */
    NJ_UINT16 elm;  /* 読みの読み文字テーブル上の要素番号      */
    NJ_INT16  ret;  /* 読みが読み文字テーブルに存在するかを示す */
                    /* 0:存在 負数:存在せず                  */
    NJ_UINT16 giji_yomi;    /* 対象読み文字第１要素   */
    NJ_UINT16 giji_yomi2;   /* 対象読み文字第２要素   */
    NJ_UINT16 cnt = 0;      /* 確認用ループカウンター */


    pY = yomi;
    len0 = 0;
    elm = 0;
    ret = 0;

    /* 変換後の文字長(len0)取得処理 */
    while (cnt < len) {
        /* 読み文字長を計算 */
#ifndef NJ_OPT_UTF16
        if (NJG_IS_WCHAR(*pY)) {
            /*
             * 読みが2byte
             */
#endif /* !NJ_OPT_UTF16 */
            /* 読みが読み文字テーブルに存在するか判定 */
            ret = njg_get_yomi_table(NJG_CONV_TO_WCHAR(pY), &elm);
            
            if (ret == 0) {
                /*
                 * 読みが読み文字テーブルに存在
                 */
                if (NJG_IS_GIJI_SYM_TBL(elm)) {
                    /* 最上位bitを元に戻す */
                    elm ^= (NJ_UINT16)NJ_GIJI_SYM_TBL;
                    /* 読みが記号 */
                    giji_yomi  = giji_hankata_sym_tbl[elm][0];
                    giji_yomi2 = giji_hankata_sym_tbl[elm][1];
                } else {
                    /* 読みがひらがな */
                    giji_yomi  = giji_hankata_tbl[elm][0];
                    giji_yomi2 = giji_hankata_tbl[elm][1];
                }
                /*
                 * 第１要素サイズ取得 (カナ)
                 */
                if (giji_yomi == 0) {
                    /* 候補対象が存在しないため、候補無し */
                    return 0; /*NCH_FB*/
                } else if ((giji_yomi & 0xFF00) == 0) {
                    /* 候補が半角文字 */
                    len0 += NJG_HAN_MOJI_LEN;
                } else {
                    /* 候補が全角文字 */
                    len0 += NJG_ZEN_MOJI_LEN; /*NCH_FB*/
                }
                /*
                 * 第２要素サイズ取得 (濁点・半濁点)
                 * 濁点・半濁点がなければサイズを加算しない。
                 */
                if (giji_yomi2 != 0) {
                    if ((giji_yomi2 & 0xFF00) == 0) {
                        /* 候補が半角文字 */
                        len0 += NJG_HAN_MOJI_LEN;
                    } else {
                        /* 候補が全角文字 */
                        len0 += NJG_ZEN_MOJI_LEN; /*NCH_FB*/
                    }
                }
            } else {
                /*
                 * 読みが読み文字テーブルに存在しないため、候補無し
                 */
                return 0;
            }
            pY += NJG_ZEN_MOJI_LEN;
            cnt += NJG_ZEN_MOJI_LEN;

#ifndef NJ_OPT_UTF16
        } else {
            /*
             * 読みが1byte
             * 読みが読み文字テーブルに存在しないため、候補無し
             */
            return 0;
        }
#endif /* !NJ_OPT_UTF16 */
    }

    /*
     * もし、解析完了時に最大表記文字列長を超過している場合
     * 半角カタカナの疑似候補は作成しない。
     * 濁音がふくまれると表記長が長くなる場合がある。
     */
    if (len0 > NJ_MAX_RESULT_LEN) {
        return 0; /*NCH_FB*/
    }
    p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
    p->word.yomi = yomi;
    NJ_SET_YLEN_TO_STEM(&p->word, len);
    NJ_SET_KLEN_TO_STEM(&p->word, len0);
    NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
    NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
    NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HANKATA);
    NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

    return 1;
}

/**
 * 英字擬似候補の取得処理(共通)
 *
 * @param[in]  gfpos      擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos      擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi       擬似候補 読み
 * @param[in]  len        読み文字長
 * @param[out] p          擬似候補格納バッファ
 * @param[in]  gtype      英字擬似候補のタイプ
 * @param[in]  giji_tbl   英字擬似候補テーブル
 * @param[in]  giji_tbl_cap 先頭文字専用の英字擬似候補テーブル(NULLなら指定なし)
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_eiji_length(NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                     NJ_CHAR *yomi, NJ_UINT16 len,
                                     NJ_RESULT *p, NJ_UINT8 gtype,
                                     const NJ_UINT16 *giji_tbl,
                                     const NJ_UINT16 *giji_tbl_cap) {

    NJ_CHAR  *pY;          /* 読みのアドレス */
    const NJ_UINT16 *ctbl;
    NJ_UINT16 elm;         /* 読みの読み文字テーブル上の要素番号 */
    NJ_UINT16 len0;        /* 擬似候補文字長 */
    NJ_UINT16 cnt = 0;     /* 確認用ループカウンター */


    pY = yomi;
    elm = 0;
    len0 = 0;

    if (giji_tbl_cap != NULL) {
        ctbl = giji_tbl_cap;
    } else {
        ctbl = giji_tbl;
    }

    while (cnt < len) {
#ifndef NJ_OPT_UTF16
        /* 読み文字長を計算 */
        if (NJG_IS_WCHAR(*pY)) {
            /*
             * 読みが2byte
             */
#endif /* !NJ_OPT_UTF16 */
            /* 読みが読み文字テーブルに存在するか判定 */
            if (njg_get_yomi_table(NJG_CONV_TO_WCHAR(pY), &elm) != 0) {
                /* 読みが読み文字テーブルに存在しない場合は擬似候補を作成しない */
                return 0;
            }
            /*
             * 読みが読み文字テーブルの 記号テーブル に存在する場合、
             * 記号は取得対象ではないため、擬似候補を作成しない。
             */
            if (NJG_IS_GIJI_SYM_TBL(elm)) {
                return 0;
            }
            /*
             * 候補対象が存在しないため、擬似候補を作成しない
             */
            if (ctbl[elm] == 0) {
                return 0;
            }
            /*
             * 候補が1バイトか2バイトかをチェックして、候補バイト長を加算する
             */
            if ((ctbl[elm] & 0xFF00) == 0) {
                len0 += NJG_HAN_MOJI_LEN;
            } else {
                len0 += NJG_ZEN_MOJI_LEN;
            }
            pY += NJG_ZEN_MOJI_LEN;
            cnt += NJG_ZEN_MOJI_LEN;

#ifndef NJ_OPT_UTF16
        } else {
            /* 読みが1byteの場合は擬似候補を作成しない */
            return 0;
        }
#endif /* !NJ_OPT_UTF16 */
        /* 2文字目以降の擬似テーブルをセットする */
        ctbl = giji_tbl;
    }

    /*
     * もし、解析完了時に最大表記文字列長を超過している場合
     * 疑似候補は作成しない。
     */
    if (len0 > NJ_MAX_RESULT_LEN) {
        return 0; /*NCH_FB*/
    }
    p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
    p->word.yomi = yomi;
    NJ_SET_YLEN_TO_STEM(&p->word, len);
    NJ_SET_KLEN_TO_STEM(&p->word, len0);
    NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
    NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
    NJ_SET_TYPE_TO_STEM(&p->word, gtype);
    NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

    return 1;
}

/**
 * 擬似候補取得処理（数字共通）
 *
 * @param[in]  gfpos    擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos    擬似品詞(数字) 後品詞番号
 * @param[in]  yomi     擬似候補 読み
 * @param[in]  len      読み文字長
 * @param[out] p        擬似候補格納バッファ
 * @param[in]  gtype    擬似候補のタイプ
 * @param[in]  giji_tbl 数字擬似候補テーブル
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_suuji(NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                               NJ_CHAR *yomi, NJ_UINT16 len,
                               NJ_RESULT *p, NJ_UINT8 gtype,
                               const NJ_UINT16 *giji_tbl) {
    NJ_UINT16 j;
    NJ_CHAR  *pY;
    NJ_UINT16 index;
    NJ_UINT16 len0;
    NJ_UINT16 keynum, toggle;
    NJ_UINT16 keta = 0;
    NJ_UINT8 chk_buf = 0x00;


    pY = yomi;
    len0 = 0;
    for (j = 0; j < len; pY += NJG_ZEN_MOJI_LEN, j += NJG_ZEN_MOJI_LEN) {
#ifndef NJ_OPT_UTF16
        /* 2バイト文字の場合、読み文字テーブルを検索 */
        if (NJG_IS_WCHAR(*pY)) {
#endif /* !NJ_OPT_UTF16 */

            /* 文字コードをデータ変換し、読み文字テーブルを検索 */
            if (njg_get_yomi_table(NJG_CONV_TO_WCHAR(pY), &index) < 0) {
                /* 読み文字テーブルにデータがないので変換対象外とする */
                break;
            }
            /*
             * 読みテーブルより、変換数値情報が設定されているかチェックする。
             *
             * 設定されている場合は、数字とトグル数を取得し、
             * 変換後データ長(len0)はトグル数分を加算する。
             * 
             * 設定されていない場合は、変換対象外とする
             */
            keynum = NJG_GET_NUM_YOMI(index);
            if ((keynum != NJ_NUM_NON) && (giji_tbl[keynum] != 0)) {
                toggle = NJG_GET_TOGGLE_YOMI(index);
                if ((giji_tbl[keynum] & 0xff00) == 0) {
                    len0 += (NJ_UINT16)(NJG_HAN_MOJI_LEN * toggle);
                } else {
                    /* 2バイトコード */
                    len0 += (NJ_UINT16)(NJG_ZEN_MOJI_LEN * toggle);
                }
                /* 擬似候補の桁数(文字数)を算出 */
                keta += toggle;
                /* 先頭が0の時の半角数字カンマ区切り候補非表示判定を行う為に、
                 * 先頭文字の擬似候補データを取得する */
                if (j == 0) {
                    chk_buf = (NJ_UINT8)(keynum & 0x00ff);
                }
            } else {
                break;
            }
#ifndef NJ_OPT_UTF16
        } else {
            /* その他の文字種が混在することになり、
             * その場合は変換対象外とする
             */
            break;
        }
#endif /* !NJ_OPT_UTF16 */
    }
    if (j == len) {
       /*
        * 制限桁数以上の数字候補の場合、擬似候補として作成しない。
        * (カンマの文字数は制限桁数に含めない。)
        */
        if (keta >= NJG_DIGIT_SUUJI_LIMIT) {
            /* 制限桁数オーバーの為、擬似候補として作成しない */
            return 0;
        }
        if ((gtype == NJ_TYPE_HAN_SUUJI_COMMA) && (keta > 3)) {
            /*
             * 半角数字カンマ区切りの擬似候補のとき
             */
            if (chk_buf == 0x00) {
                /* 先頭が0の半角数字カンマ区切り候補は非表示の為、
                 * 処理を終了する */
                return 0;
            }
            /* カンマ記号数を算出してデータ長に加算する */
            /* マイナス1しておくと、例えば3桁のとき、先頭に付くのを防げる */
            len0 += (NJ_UINT16) ((keta - 1) / 3);
        }
        if (len0 > NJ_MAX_RESULT_LEN) {
            /*
             * もし、解析完了時に最大表記文字列長を超過している場合
             * 疑似候補は作成しない。
             */
            return 0;
        }
        p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
        p->word.yomi = yomi;
        NJ_SET_YLEN_TO_STEM(&p->word, len);
        NJ_SET_KLEN_TO_STEM(&p->word, len0);
        NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
        NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
        NJ_SET_TYPE_TO_STEM(&p->word, gtype);
        NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));
    
        return 1;
    }
    return 0;
}

/**
 * 時間・日付擬似候補の共通取得処理：読みテーブルから数字データを取り出す
 *
 * @param[out] iwnn  作業領域<br>
 *                    iwnn->gijiwork : 数字データ<br>
 *                    iwnn->gw_len   : 数字データの有効文字長
 * @param[in]  ctbl  時間・日付擬似候補の変換テーブル
 * @param[in]  pY    読み文字列
 * @param[in]  len   読み文字列の文字数
 *
 * @retval  >0  作成できる擬似候補の文字列長
 * @retval   0  作成できない
 */
static NJ_UINT16 njg_get_han_time_date(PART_OF_NJ_ENV *iwnn, const NJ_CONVERT_TBL *ctbl, 
                                       NJ_CHAR *pY, NJ_UINT16 len) {
    NJ_UINT16 len0;
    NJ_UINT16 index;
    NJ_UINT16 keynum, toggle;
    NJ_UINT16 i, j;
    NJ_INT16 h;
    NJ_UINT16 data_len;


    /* 擬似候補の単位バイト数も含めて最大を超えないこと */
    data_len = (NJ_UINT16)(NJ_MAX_RESULT_LEN - (ctbl->code_size / sizeof(NJ_CHAR)));

    /*
     * 擬似候補が作成できるか、チェックする。
     * 
     * 以下の処理を同時に行う。
     *   ・読みテーブルから、キーアサイン情報を取り出す。
     *   ・数字擬似変換後のデータ長が規定サイズを上回る場合は、
     *     変換できない。
     *   ・規定サイズ内なら、時間擬似候補の範囲チェック用のHex
     *     データを作成する。
     *   ・半角数字文字が読み文字に混在する場合は、数字擬似テーブル
     *     (giji_suuji_tbl)を基にキーアサインNoを決定し、擬似候補
     *     の範囲チェック用のHexデータに含める。
     */
    len0 = 0;
    h = 0;
    for (j = 0; j < len; pY += NJG_ZEN_MOJI_LEN, j += NJG_ZEN_MOJI_LEN) {
#ifndef NJ_OPT_UTF16
        if (NJG_IS_WCHAR(*pY)) {
            /* 2バイト文字の場合、読み文字テーブルを検索 */
#endif /* !NJ_OPT_UTF16 */

            /* 文字コードをデータ変換し、読み文字テーブルを検索 */
            if (njg_get_yomi_table(NJG_CONV_TO_WCHAR(pY), &index) < 0) {
                /* 読み文字テーブルにデータがないので変換対象外とする */
                break;
            }
            /*
             * 読みテーブルより、変換数値情報が設定されているかチェックする。
             *
             * 設定されている場合は、数字とトグル数を取得し、
             * 変換後データ長(len0)はトグル数分を加算する。
             * 
             * 設定されていない場合は、変換対象外とする
             */
            keynum = NJG_GET_NUM_YOMI(index);
            if ((keynum != NJ_NUM_NON) && (giji_suuji_tbl[NJG_SUUJI_TBL_HAN][keynum] != 0)) {
                /* 変換後データ長を算出 */
                if ((giji_suuji_tbl[NJG_SUUJI_TBL_HAN][keynum] & 0xff00) == 0) {
                    len0 += (NJ_UINT16)(NJG_HAN_MOJI_LEN * NJG_GET_TOGGLE_YOMI(index));
                } else {
                    /* 2バイトコード */
                    len0 += (NJ_UINT16)(NJG_ZEN_MOJI_LEN * NJG_GET_TOGGLE_YOMI(index)); /*NCH_FB*/
                }
            } else {
                break;
            }
            /* NJ_MAX_RESULT_LEN を超えないか、データ長をチェック */
            if (len0 > data_len) {
                break;
            }
            /* 擬似候補の数値範囲チェック用のHexデータ作成 */
            /* データは、トグル数分必要 */
            toggle = NJG_GET_TOGGLE_YOMI(index);
            keynum &= 0x00ff;
            for (i = 0; i < toggle; i++) {
                iwnn->gijiwork[h++] = (NJ_UINT8)keynum;
            }
#ifndef NJ_OPT_UTF16
        } else {
            /* その他の文字種が混在することになり、
             * その場合は変換対象外とする
             */
            break;
        }
#endif /* !NJ_OPT_UTF16 */
    }
    if (j == len) {
        iwnn->gw_len = h;/* 数字候補の桁数(iwnn->gijiworkの有効文字要素数) */

        /* 変換可能なら、処理結果を作成する */
        len0 += (NJ_UINT16)(ctbl->code_size / sizeof(NJ_CHAR));
        return len0;
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角時間(H時)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_time_hh(PART_OF_NJ_ENV *iwnn, 
                                     NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                     NJ_CHAR *yomi, NJ_UINT16 len,
                                     NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_INT8   wFirst = 0;


    /*
     * 時間擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_THH],
                                 yomi, len);

    if (len0 > 0) {
        /* 有効桁チェック */
        switch (iwnn->gw_len) {
            /* H時*/
        case 1:
            wFirst = (NJ_INT8)iwnn->gijiwork[0];
            break;
            /* HH時 */
        case 2:
            if ((iwnn->gijiwork[0] == 0) && (iwnn->gijiwork[1] == 0)) {
                /* 00時はエラー */
                return 0;
            }
            wFirst = (NJ_INT8)(iwnn->gijiwork[0] * 10);
            wFirst += (NJ_INT8)iwnn->gijiwork[1];
            break;

        default:
            return 0;
        }

        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if (NJG_IS_HOUR_RANGE(wFirst) == 1) {
            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_TIME_HH);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));
            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角時間(M分)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p   擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_time_mm(PART_OF_NJ_ENV *iwnn, 
                                     NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                     NJ_CHAR *yomi, NJ_UINT16 len,
                                     NJ_RESULT *p) {
    NJ_UINT16 len0;


    /*
     * 時間擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_TMM],
                                 yomi, len);

    if (len0 > 0) {
        /* MM分の有効桁を判定する */
        if (iwnn->gw_len >= 4) {
            /* 表示範囲が0 〜 999の為、4桁以上はエラーとする。 */
            return 0;
        }

        if ((iwnn->gw_len >= 2) &&
            ((iwnn->gijiwork[0] == 0x00) && (iwnn->gijiwork[1] == 0x00))) {
            /* 先頭の2文字が00ならばエラー */
            return 0;
        }

        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
        p->word.yomi = yomi;
        NJ_SET_YLEN_TO_STEM(&p->word, len);
        NJ_SET_KLEN_TO_STEM(&p->word, len0);
        NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
        NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
        NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_TIME_MM);
        NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

        return 1;
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角時間(H時M分)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p   擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_time_hm(PART_OF_NJ_ENV *iwnn, 
                                     NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                     NJ_CHAR *yomi, NJ_UINT16 len,
                                     NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_INT8   wFirst = 0;
    NJ_INT8   wSecond  = 0;


    /*
     * 時間擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_THM],
                                 yomi, len);

    /* 有効桁数のチェック */
    if ((len0 > 0) && (iwnn->gw_len == 2)) {

        /* H時M分 */
        wFirst = (NJ_INT8)iwnn->gijiwork[0];
        wSecond = (NJ_INT8)iwnn->gijiwork[1];
        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((NJG_IS_HOUR_RANGE(wFirst) == 1)  && (NJG_IS_MIN_RANGE(wSecond) == 1)) {
            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_TIME_HM);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));
            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角時間(H時MM分)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos   擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos   擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi    擬似候補 読み
 * @param[in]  len     読み文字長
 * @param[out] p    擬似候補格納バッファ
 *
 * @retval  1    指定された擬似候補を取得した
 * @retval  0    指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_time_hmm(PART_OF_NJ_ENV *iwnn, 
                                      NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                      NJ_CHAR *yomi, NJ_UINT16 len,
                                      NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_INT8   wFirst = 0;
    NJ_INT8   wSecond = 0;


    /*
     * 時間擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_THMM],
                                 yomi, len);

    /* 有効桁数のチェック */
    if ((len0 > 0) && (iwnn->gw_len == 3)) {

        /* H時MM分 */
        wFirst = (NJ_INT8)iwnn->gijiwork[0];
        wSecond = (NJ_INT8)(iwnn->gijiwork[1] * 10);
        wSecond += (NJ_INT8)iwnn->gijiwork[2];
        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((NJG_IS_HOUR_RANGE(wFirst) == 1) && (NJG_IS_MIN_RANGE(wSecond) == 1)) {
            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_TIME_HMM);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角時間(HH時M分)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_time_hhm(PART_OF_NJ_ENV *iwnn, 
                                      NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                      NJ_CHAR *yomi, NJ_UINT16 len,
                                      NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_INT8   wFirst = 0;
    NJ_INT8   wSecond = 0;


    /*
     * 時間擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_THHM],
                                 yomi, len);

    /* 有効桁数のチェック */
    if ((len0 > 0) && (iwnn->gw_len == 3)) {

        /* HH時M分 */
        if ((iwnn->gijiwork[0] == 0) && (iwnn->gijiwork[1] == 0)) {
            /* 00時はエラー */
            return 0;
        }
        wFirst = (NJ_INT8)(iwnn->gijiwork[0] * 10);
        wFirst += (NJ_INT8)iwnn->gijiwork[1];
        wSecond = (NJ_INT8)iwnn->gijiwork[2];

        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((NJG_IS_HOUR_RANGE(wFirst) == 1)  && (NJG_IS_MIN_RANGE(wSecond) == 1)) {
            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_TIME_HHM);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角時間(HH時MM分)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_time_hhmm(PART_OF_NJ_ENV *iwnn, 
                                       NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                       NJ_CHAR *yomi, NJ_UINT16 len,
                                       NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_INT8   wFirst = 0;
    NJ_INT8   wSecond = 0;


    /*
     * 時間擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_THHMM],
                                 yomi, len);

    /* 有効桁数のチェック */
    if ((len0 > 0) && (iwnn->gw_len == 4)) {

        /* HH時MM分 */
        if ((iwnn->gijiwork[0] == 0) && (iwnn->gijiwork[1] == 0)) {
            /* 00時はエラー */
            return 0;
        }
        wFirst = (NJ_INT8)(iwnn->gijiwork[0] * 10);
        wFirst += (NJ_INT8)iwnn->gijiwork[1];
        wSecond = (NJ_INT8)(iwnn->gijiwork[2] * 10);
        wSecond += (NJ_INT8)iwnn->gijiwork[3];

        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((NJG_IS_HOUR_RANGE(wFirst) == 1)  && (NJG_IS_MIN_RANGE(wSecond) == 1)) {
            
            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_TIME_HHMM);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角時間(H:MM)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_time_hmm_sym(PART_OF_NJ_ENV *iwnn, 
                                          NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                          NJ_CHAR *yomi, NJ_UINT16 len,
                                          NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_INT8   wFirst = 0;
    NJ_INT8   wSecond = 0;


    /*
     * 時間擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_THMM_SYM],
                                 yomi, len);

    /* 有効桁数のチェック */
    if ((len0 > 0) && (iwnn->gw_len == 3)) {

        /* H時MM分 */
        wFirst = (NJ_INT8)iwnn->gijiwork[0];
        wSecond = (NJ_INT8)(iwnn->gijiwork[1] * 10);
        wSecond += (NJ_INT8)iwnn->gijiwork[2];
        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((NJG_IS_HOUR_RANGE(wFirst) == 1)  && (NJG_IS_MIN_RANGE(wSecond) == 1)) {

            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_TIME_HMM_SYM);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角時間(HH:MM分)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_time_hhmm_sym(PART_OF_NJ_ENV *iwnn, 
                                           NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                           NJ_CHAR *yomi, NJ_UINT16 len,
                                           NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_INT8   wFirst = 0;
    NJ_INT8   wSecond = 0;


    /*
     * 時間擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_THHMM_SYM],
                                 yomi, len);

    /* 有効桁数のチェック */
    if ((len0 > 0) && (iwnn->gw_len == 4)) {

        /* HH時MM分 */
        if ((iwnn->gijiwork[0] == 0) && (iwnn->gijiwork[1] == 0)) {
            /* 00:MM はエラー */
            return 0;
        }
        wFirst = (NJ_INT8)(iwnn->gijiwork[0] * 10);
        wFirst += (NJ_INT8)iwnn->gijiwork[1];
        wSecond = (NJ_INT8)(iwnn->gijiwork[2] * 10);
        wSecond += (NJ_INT8)iwnn->gijiwork[3];

        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((NJG_IS_HOUR_RANGE(wFirst) == 1)  && (NJG_IS_MIN_RANGE(wSecond) == 1)) {

            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_TIME_HHMM_SYM);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角日付(YYYY年)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_date_yyyy(PART_OF_NJ_ENV *iwnn, 
                                       NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                       NJ_CHAR *yomi, NJ_UINT16 len,
                                       NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_UINT32  wwFirst = 0;


    /*
     * 日付擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_DYYYY],
                                 yomi, len);

    if (len0 > 0) {
        /* 有効桁チェック */
        switch (iwnn->gw_len) {
        case 1:
            wwFirst = iwnn->gijiwork[0];
            break;
        case 2:
            wwFirst = (iwnn->gijiwork[0] * 10);
            wwFirst += iwnn->gijiwork[1];
            break;
        case 3:
            wwFirst = (iwnn->gijiwork[0] * 100);
            wwFirst += (iwnn->gijiwork[1] * 10);
            wwFirst += (iwnn->gijiwork[2]);
            break;
        case 4:
            wwFirst = (iwnn->gijiwork[0] * 1000);
            wwFirst += (iwnn->gijiwork[1] * 100);
            wwFirst += (iwnn->gijiwork[2] * 10);
            wwFirst += (iwnn->gijiwork[3]);
            break;
        default:
            return 0;
        }

        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((wwFirst >= NJG_YEAR_MIN) && (wwFirst <= NJG_YEAR_MAX)) {

            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_DATE_YYYY);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角日付(M月)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_date_mm(PART_OF_NJ_ENV *iwnn, 
                                     NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                     NJ_CHAR *yomi, NJ_UINT16 len,
                                     NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_UINT8  wFirst = 0;


    /*
     * 日付擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_DMM],
                                 yomi, len);

    if (len0 > 0) {
        /* 有効桁チェック */
        switch (iwnn->gw_len) {
            /* M月*/
        case 1:
            wFirst = (NJ_UINT8)iwnn->gijiwork[0];/*  ASCII範囲内のみ使用のため、キャストは問題ない */
            break;
            /* MM月 */
        case 2:
            if (iwnn->gijiwork[0] == 0) {
                /* 0X月はエラー */
                return 0;
            }
            wFirst = (NJ_UINT8)(iwnn->gijiwork[0] * 10);
            wFirst += iwnn->gijiwork[1];
            break;
        default:
            return 0;
        }

        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if (NJG_IS_MONTH_RANGE(wFirst) == 1) {

            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_DATE_MM);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角日付(D日)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_date_dd(PART_OF_NJ_ENV *iwnn, 
                                     NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                     NJ_CHAR *yomi, NJ_UINT16 len,
                                     NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_UINT8  wFirst = 0;


    /*
     * 日付擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_DDD],
                                 yomi, len);

    if (len0 > 0) {
        /* 有効桁チェック */
        switch (iwnn->gw_len) {
            /* D日 */
        case 1:
            wFirst = (NJ_UINT8)iwnn->gijiwork[0];/*  ASCII範囲内のみ使用のため、キャストは問題ない */
            break;
            /* DD日 */
        case 2:
            if (iwnn->gijiwork[0] == 0) {
                /* 0X日はエラー */
                return 0;
            }
            wFirst = (NJ_UINT8)(iwnn->gijiwork[0] * 10);
            wFirst += iwnn->gijiwork[1];
            break;
        default:
            return 0;
        }

        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if (NJG_IS_DAY_RANGE(wFirst) == 1) {

            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_DATE_DD);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角日付(M月D日)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_date_md(PART_OF_NJ_ENV *iwnn, 
                                     NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                     NJ_CHAR *yomi, NJ_UINT16 len,
                                     NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_UINT8  wFirst = 0;
    NJ_UINT8  wSecond  = 0;


    /*
     * 日付擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_DMD],
                                 yomi, len);

    /* 有効桁数のチェック */
    if ((len0 > 0) && (iwnn->gw_len == 2)) {

        /* M月D日 */
        wFirst = (NJ_UINT8)iwnn->gijiwork[0];/*  ASCII範囲内のみ使用のため、キャストは問題ない */
        wSecond = (NJ_UINT8)iwnn->gijiwork[1];/*  ASCII範囲内のみ使用のため、キャストは問題ない */
        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((NJG_IS_MONTH_RANGE(wFirst) == 1)  && (NJG_IS_EXIST_DATE(wFirst, wSecond) == 1)) {

            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_DATE_MD);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));
    
            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角日付(M月DD日)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_date_mdd(PART_OF_NJ_ENV *iwnn, 
                                      NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                      NJ_CHAR *yomi, NJ_UINT16 len,
                                      NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_UINT8  wFirst = 0;
    NJ_UINT8  wSecond  = 0;


    /*
     * 日付擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_DMDD],
                                 yomi, len);

    /* 有効桁数のチェック */
    if ((len0 > 0) && (iwnn->gw_len == 3)) {

        if (iwnn->gijiwork[1] == 0x00) {
            /* 日の先頭0 はエラー */
            return 0;
        }

        /* M月DD日 */
        wFirst = (NJ_UINT8)iwnn->gijiwork[0];/*  ASCII範囲内のみ使用のため、キャストは問題ない */
        wSecond = (NJ_UINT8)(iwnn->gijiwork[1] * 10);
        wSecond += iwnn->gijiwork[2];
        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((NJG_IS_MONTH_RANGE(wFirst) == 1)  && (NJG_IS_EXIST_DATE(wFirst, wSecond) == 1)) {

            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_DATE_MDD);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));
    
            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角日付(MM月D日)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_date_mmd(PART_OF_NJ_ENV *iwnn, 
                                      NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                      NJ_CHAR *yomi, NJ_UINT16 len,
                                      NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_UINT8  wFirst = 0;
    NJ_UINT8  wSecond  = 0;


    /*
     * 日付擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_DMMD],
                                 yomi, len);

    /* 有効桁数のチェック */
    if ((len0 > 0) && (iwnn->gw_len == 3)) {

        if (iwnn->gijiwork[0] == 0x00) {
            /* 月の先頭0 はエラー */
            return 0;
        }

        /* MM月D日 */
        wFirst = (NJ_UINT8)(iwnn->gijiwork[0] * 10);
        wFirst += iwnn->gijiwork[1];
        wSecond = (NJ_UINT8)iwnn->gijiwork[2];/*  ASCII範囲内のみ使用のため、キャストは問題ない */
        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((NJG_IS_MONTH_RANGE(wFirst) == 1)  && (NJG_IS_EXIST_DATE(wFirst, wSecond) == 1)) {

            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_DATE_MMD);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));
            
            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角日付(MM月DD日)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_date_mmdd(PART_OF_NJ_ENV *iwnn, 
                                       NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                       NJ_CHAR *yomi, NJ_UINT16 len,
                                       NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_UINT8  wFirst = 0;
    NJ_UINT8  wSecond  = 0;


    /*
     * 日付擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_DMMDD],
                                 yomi, len);

    /* 有効桁数のチェック */
    if ((len0 > 0) && (iwnn->gw_len == 4)) {

        if (iwnn->gijiwork[0] == 0x00) {
            /* 月の先頭0 はエラー */
            return 0;
        }

        /* MM月DD日 */
        wFirst = (NJ_UINT8)(iwnn->gijiwork[0] * 10);
        wFirst += iwnn->gijiwork[1];
        wSecond = (NJ_UINT8)(iwnn->gijiwork[2] * 10);
        wSecond += iwnn->gijiwork[3];
        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((NJG_IS_MONTH_RANGE(wFirst) == 1) && (NJG_IS_EXIST_DATE(wFirst, wSecond) == 1)) {

            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_DATE_MMDD);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));
    
            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角日付(M/D)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_date_md_sym(PART_OF_NJ_ENV *iwnn, 
                                         NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                         NJ_CHAR *yomi, NJ_UINT16 len,
                                         NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_UINT8  wFirst = 0;
    NJ_UINT8  wSecond  = 0;


    /*
     * 日付擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_DMD_SYM],
                                 yomi, len);

    /* 有効桁数のチェック */
    if ((len0 > 0) && (iwnn->gw_len == 2)) {

        /* M月D日 */
        wFirst = (NJ_UINT8)iwnn->gijiwork[0];/*  ASCII範囲内のみ使用のため、キャストは問題ない */
        wSecond = (NJ_UINT8)iwnn->gijiwork[1];/*  ASCII範囲内のみ使用のため、キャストは問題ない */
        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((NJG_IS_MONTH_RANGE(wFirst) == 1) && (NJG_IS_EXIST_DATE(wFirst, wSecond) == 1)) {

            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_DATE_MD_SYM);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));
    
            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角日付(M/DD)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_date_mdd_sym(PART_OF_NJ_ENV *iwnn, 
                                          NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                          NJ_CHAR *yomi, NJ_UINT16 len,
                                          NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_UINT8  wFirst = 0;
    NJ_UINT8  wSecond  = 0;


    /*
     * 日付擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_DMDD_SYM],
                                 yomi, len );

    /* 有効桁数のチェック */
    if ((len0 > 0) && (iwnn->gw_len == 3)) {

        if (iwnn->gijiwork[1] == 0x00) {
            /* 日の先頭0 はエラー */
            return 0;
        }

        /* M月DD日 */
        wFirst = (NJ_UINT8)iwnn->gijiwork[0];/*  ASCII範囲内のみ使用のため、キャストは問題ない */
        wSecond = (NJ_UINT8)(iwnn->gijiwork[1] * 10);
        wSecond += iwnn->gijiwork[2];
        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((NJG_IS_MONTH_RANGE(wFirst) == 1)  && (NJG_IS_EXIST_DATE(wFirst, wSecond) == 1)) {

            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_DATE_MDD_SYM);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));
    
            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角日付(MM/D)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_date_mmd_sym(PART_OF_NJ_ENV *iwnn, 
                                          NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                          NJ_CHAR *yomi, NJ_UINT16 len,
                                          NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_UINT8  wFirst = 0;
    NJ_UINT8  wSecond  = 0;


    /*
     * 日付擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_DMMD_SYM],
                                 yomi, len);
    /* 有効桁数のチェック */
    if ((len0 > 0) && (iwnn->gw_len == 3)) {

        if (iwnn->gijiwork[0] == 0x00) {
            /* 月の先頭0 はエラー */
            return 0;
        }

        /* MM月D日 */
        wFirst = (NJ_UINT8)(iwnn->gijiwork[0] * 10);
        wFirst += iwnn->gijiwork[1];
        wSecond = (NJ_UINT8)iwnn->gijiwork[2]; /*  ASCII範囲内のみ使用のため、キャストは問題ない */
        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((NJG_IS_MONTH_RANGE(wFirst) == 1)  && (NJG_IS_EXIST_DATE(wFirst, wSecond) == 1)) {

            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_DATE_MMD_SYM);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));
    
            return 1;
        }
    }
    return 0;
}

/**
 * 擬似候補取得処理（半角日付(MM/DD)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_han_date_mmdd_sym(PART_OF_NJ_ENV *iwnn, 
                                           NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                           NJ_CHAR *yomi, NJ_UINT16 len,
                                           NJ_RESULT *p) {
    NJ_UINT16 len0;
    NJ_UINT8  wFirst = 0;
    NJ_UINT8  wSecond  = 0;


    /*
     * 日付擬似候補が作成できるか、チェックする。
     */
    len0 = njg_get_han_time_date(iwnn, &giji_conv_table[NJG_TBL_NO_DMMDD_SYM],
                                 yomi, len);

    /* 有効桁数のチェック */
    if ((len0 > 0) && (iwnn->gw_len == 4)) {

        /* MM月DD日 */
        wFirst = (NJ_UINT8)(iwnn->gijiwork[0] * 10);
        wFirst += iwnn->gijiwork[1];
        wSecond = (NJ_UINT8)(iwnn->gijiwork[2] * 10);
        wSecond += iwnn->gijiwork[3];
        /*
         * 擬似候補作成が可能なら処理結果を作成
         */
        if ((NJG_IS_MONTH_RANGE(wFirst) == 1)  && (NJG_IS_EXIST_DATE(wFirst, wSecond) == 1)) {

            p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
            p->word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&p->word, len);
            NJ_SET_KLEN_TO_STEM(&p->word, len0);
            NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
            NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
            NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_HAN_DATE_MMDD_SYM);
            NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));
    
            return 1;
        }
    }
    return 0;
}

/**
 * giji_suuji_yomi_tbl[]を基に読み文字を検索し、
 * 漢数字に置き換えられる読み文字数を返す。
 *
 * @param[in]  yomi      数字読み文字列
 * @param[out] yomi_len  数字に置き換えた読み文字の文字配列数
 *
 * @retval >1   置き換える数字(Hexデータ)。giji_suuji_yomi_tbl[].ketaposを返す。
 * @retval <=0  テーブル不一致
 */
static NJ_INT16 njg_get_ksuuji_ketapos(NJ_CHAR *yomi, NJ_UINT8 *yomi_len) {

    NJ_SUUJIYOMITBL* ptr;
    NJ_CHAR* pY;
    NJ_INT16 ret;
    NJ_INT16 n;
    NJ_UINT16 s1;
    NJ_UINT16 *s2;


    /* 数字読みテーブルを検索 */
    ptr = (NJ_SUUJIYOMITBL *)giji_suuji_yomi_tbl;

    /*
     * 数字読みテーブル単位で、読み文字の一致チェックを行う。
     * テーブルに合わせて、UINT16でチェックする。
     */
    while (ptr->ketapos > 0) {
        /* 読み文字の取り出し */
        pY = yomi;
        s1 = NJG_CONV_TO_WCHAR(pY);
        /* 数字読みテーブルの読みの取り出し */
        s2 = ptr->yomi;
        n = ptr->len;

        /*
         * 1テーブル内で読みを比較チェック
         */
        /* s1 == s2 の時は 0 を返す */
        ret = 0;
        while (n > 0) {
            if (s1 > *s2) {
                /* s1 > s2 の時は 1 を返す */
                ret = 1;
                break;
            } else {
                if (s1 < *s2) {
                    /* s1 < s2 の時は -1 を返す */
                    ret = -1;
                    break;
                }
            }
            /* 次の文字を取り出す */
            pY += NJG_ZEN_MOJI_LEN;
            s1 = NJG_CONV_TO_WCHAR(pY);
            s2++;
            n--;
        }
        /*
         * 比較結果
         */
        if (ret == 0) {
            /* 検索一致 */
            *yomi_len = (NJ_UINT8)(ptr->len * NJG_ZEN_MOJI_LEN);
            return ptr->ketapos;
        } else {
            if ((ret > 0) && (NJG_CONV_TO_WCHAR(yomi) != ptr->yomi[0])) {
                /* 降順にソートされているため，yomi の方が大きければ
                 * それ以上検索する必要はない．
                 */
                return -1;
            }
        }
        /* 次の数字読みテーブル */
        ptr++;
    }
    return -1;
}

/**
 * 「おく」「まん」「ちょう」のチェック
 *
 * 数字に変換した配列をチェックし、「おく」「まん」「ちょう」のみの
 * 構成ではないかチェックする。
 *
 * @param[in]  suuji_strings  チェックする数字データ
 *
 * @retval  0  数字文字列として扱える
 * @retval -1  数字文字列として扱えない
 */
static NJ_INT16 njg_check_ksuuji_array(NJ_CHAR *suuji_strings) {

    const NJ_INT16 check_keta[] = {NJ_KETA_MAN, NJ_KETA_OKU, NJ_KETA_CHO};
    NJ_INT16 cnt, check_keta_cnt;
    NJ_INT16 i;
    NJ_INT16 right_flg;


    /* 万 兆 億 のフラグが立っている場合にそれより前に数字があるかどうかチェックする。*/
    /* 「おくまんちょう」といったものは、数字としてみなさないため。*/
    for (i = 0; i < 3; i++) {  /* まん 、おく、ちょうを順次チェックする */
        check_keta_cnt = (NJ_INT16)(check_keta[i] - NJG_SUB_CNT_MANTOCHO);
        if (suuji_strings[check_keta_cnt] == NJG_FIND_OKUMANCHO) {
            check_keta_cnt++;
            right_flg = 0;
            for (cnt = 0; cnt < 3; cnt++) {
                if (suuji_strings[check_keta_cnt + cnt] != 0x00) {
                    right_flg = 1;
                    break;
                }
            }
            if (!right_flg) {
                return(-1);
            }
        }
    }
    return(0);
}

/**
 * 擬似候補取得処理：読み文字列から数字インデックスを作成する
 *  
 *  この数字インデックスは、漢数字擬似候補を作成するための中間データである。
 *
 * @param[out] iwnn   作業領域<br>
 *                    iwnn->kan_data : 数字インデックス<br>
 *                    iwnn->kan_len  : 数字インデックスの有効配列長
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 *
 * @retval  0  数字インデックスが作成できた
 * @retval -1  作成できない
 */
static NJ_INT16 njg_get_ksuuji_index_from_yomi(PART_OF_NJ_ENV *iwnn, 
                                               NJ_CHAR *yomi, NJ_UINT16 len) {
    NJ_UINT8 yomi_len;
    NJ_UINT16 cnt;
    NJ_INT16 index;
    NJ_UINT16 *kanpt;


    /*
     * 最初に、読み文字列から、数字読みテーブルを基に
     * 数字インデックスを作成する。
     */
    kanpt = iwnn->kan_data;
    iwnn->kan_len = 0;
    for (cnt = 0; cnt < len;) {
        /*
         * 数字インデックスを、kanpt へ格納していく。
         * 例えば、読みが「ろっぴゃく」の場合、数字インデックスは、
         *「ろっ」と「ぴゃく」の2つが作成される。
         * 読みがどのように分割されるかは、数字読みテーブルの
         * 設定文字に左右される。
         */
        if ((index = njg_get_ksuuji_ketapos((yomi + cnt), &yomi_len)) > 0) {
            if ((index >= 0) && (index <= 9)) { /* 0から９の数字*/
                iwnn->kan_len++;
                *kanpt++ = (NJ_UINT16)index;
            } else {
                switch (index) {
                case NJ_KETA_JYUU: /* 十 */
                case NJ_KETA_HYAKU: /* 百 */
                case NJ_KETA_SEN: /* 千 */
                case NJ_KETA_ZEN: /* 千 */
                case NJ_KETA_MAN: /* 万  */
                case NJ_KETA_OKU: /* 億  */
                    iwnn->kan_len++;
                    *kanpt++ = (NJ_UINT16)index;
                    break;
                default:
                    /* テーブルの設定値エラーとして変換対象外とする */
                    return -1; /*NCH*/
                }
            }
            /*
             * 漢数字に変換できた読み文字列の文字配列長が返ってくるので、
             * 続きの読み文字列から検索を繰り返す。
             */
            cnt += yomi_len;
            if (cnt > len) {
                /* cnt が len を超えたらエラー */
                return -1; /*NCH_FB*/
            }
        } else {
            /* テーブルと一致しないため変換対象外とする */
            return -1;
        }
        if (iwnn->kan_len >= NJ_MAX_RESULT_LEN) {
            /*
             * kan_dataのサイズがNJ_MAX_RESULT_LENのため、
             * バッファオーバーしないようにこのタイミングで
             * サイズチェックを行う。
             */
            return -1; /*NCH_FB*/
        }
    }
    *kanpt = 0;

    return 0;
}

/**
 * 文字列を数字に変換する
 * 
 * @param[in]  kanpt         入力文字列
 * @param[in]  kan_len       入力文字列長
 * @param[out] suuji_strings 変換結果格納バッファ
 * @param[out] suuji_size    変換結果格納バッファ文字長
 *
 * @return  数字に変換できた文字数
 */
static NJ_INT16 njg_convert_ksuuji_data(NJ_UINT16 *kanpt, NJ_INT16 kan_len,
                                        NJ_CHAR *suuji_strings, NJ_INT16 suuji_size) {
    NJ_INT16 base_cnt, add_cnt, cnt;
    NJ_INT16 zen_flag;
    NJ_INT16 dspcnt;
    NJ_INT16 kanval;


    zen_flag = 0;

    /* セットエリアの初期化*/
    for (cnt = 0; cnt < suuji_size; cnt++) {
        suuji_strings[cnt] = 0x00;
    }

    add_cnt = 0;   /* セットしたデータ位置からのオフセット */
    base_cnt = 0;  /* 先頭からのオフセット(数値の桁位置を表す)) */
    dspcnt = 0;

    /*
     * 数字インデックスから数字データを作成する。
     * 数字インデックスは最後から取り出して数字データに変換する。
     * これにより数字データは逆順となり、バッファの最後に格納されている
     * データ位置が数字の桁数となる。
     *   例) 漢字データ [9, NJ_KETA_SEN, 1]
     *       数字データ [9, 0, 0, 1]
     */
    for (cnt = (NJ_INT16)(kan_len-1); cnt >= 0; ) {
        kanval = (NJ_INT16)kanpt[cnt];
        dspcnt++;
        
        /********************************************
         * 1から9の数字
         */
        if ((kanval >= 1) && (kanval <= 9))  {
            if ((suuji_strings[add_cnt + base_cnt] != 0x00)
                && (suuji_strings[add_cnt + base_cnt] != NJG_FIND_OKUMANCHO)
                && (suuji_strings[add_cnt + base_cnt] != NJG_FIND_JYUUHYAKUSEN))  {
                /* 「いちにさん」など同じ桁に何度も値が入る場合は、エラーとする*/
                return(0);
            }
            if ((zen_flag == 1) && (kanval != 3)) {
                /* 「ぜん」で始まるもののうち「さんぜん」以外は数字としてみなさない */
                return(0);
            }
            zen_flag = 0;
            suuji_strings[add_cnt + base_cnt] =  (NJ_UINT8)(kanval + 0x30);
        } else {
            if ((kanval >= NJ_KETA_JYUU) && (kanval <= NJ_KETA_ZEN)) {
                /********************************************
                 * 十、百、千
                 */
                if ( kanval == NJ_KETA_ZEN ) {
                    /*
                     *「ぜん」の読みは「せん」で処理する。
                     * また、「ぜん〜」という不自然な読みのチェックのため
                     * zen_flag をセットしておく。
                     */
                    kanval = NJ_KETA_SEN;
                    zen_flag = 1;
                }
                if (add_cnt >= (kanval - NJG_SUB_CNT_JYUUTOSEN)) {
                    /* にひゃくにせんなどは、漢数字としてみなさない */
                    return(0);
                } else {
                    add_cnt = (NJ_INT16)(kanval - NJG_SUB_CNT_JYUUTOSEN);
                    if (suuji_strings[add_cnt + base_cnt] == 0x00) {
                        suuji_strings[add_cnt + base_cnt] = NJG_FIND_JYUUHYAKUSEN;
                    } else {
                        /* じゅうじゅうじゅうも、漢数字としてみなさない */
                        return(0); /*NCH*/
                    }
                }
            } else {
                /********************************************
                 * 万、億、兆
                 */
                if (base_cnt >= (kanval - NJG_SUB_CNT_MANTOCHO)) {
                    /* 七億八兆などは、漢数字としてみなさない*/
                    return(0);
                } else {
                    base_cnt = (NJ_INT16)(kanval - NJG_SUB_CNT_MANTOCHO);
                    add_cnt = 0;
                    
                    /* まん、おく、ちょうなどがあったときは、
                     * 後でチェック可能なようにフラグをセットする
                     */
                    if (suuji_strings[add_cnt + base_cnt] == 0x00) {
                        suuji_strings[add_cnt + base_cnt] = NJG_FIND_OKUMANCHO;
                    } else {
                        return(0); /*NCH*/
                    }
                }
            }
        }
        cnt--;
    }/* end-of-for */
    if (zen_flag == 1) {/* チェック対象の先頭が「ぜん」で始まるものはエラーとする。*/
        return(0);
    }

    if (njg_check_ksuuji_array(suuji_strings) == -1) {
        return(0);
    }
    return(1);     /* 一応、数字変換できた*/
}

/**
 * 擬似候補取得処理：数字インデックスから数字データを作成する
 *        
 * この数字データは、数字の候補テーブルを取り出すための要素として使用される。
 *
 * @param[out] iwnn   作業領域<br>
 *                    iwnn->gijiwork : 数字データ<br>
 *                    iwnn->gw_len   : 数字データの有効バイト
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 njg_get_ksuuji_data_from_index(PART_OF_NJ_ENV *iwnn) {

    NJ_INT16 cnt;
    NJ_INT16 setcnt;
    NJ_INT16 ret;
    NJ_CHAR  *suuji_strings;
    NJ_CHAR *tmppt;


    suuji_strings = iwnn->gijiwork;

    /* 数字インデックスから数字変換データに変換する。 */
    /* この関数では、位を表すデータ(万、億など)は、まだ独自の数値がセットされている。 */
    /* また、(Out)である数字変換データ(suuji_strings)は、読み文字に対して、逆順にセットされている。*/
    ret = njg_convert_ksuuji_data(iwnn->kan_data, iwnn->kan_len, suuji_strings, NJ_MAX_RESULT_LEN);
    if (ret <= 0) {
        return (-1); /* 読み方の並びがおかしいので作成できない */
    }
    /*
     * 一旦、逆順のままワーク用領域へ退避する
     */
    tmppt = iwnn->tmp;
    setcnt = 0; /* 桁数 */
    for (cnt = (NJ_MAX_RESULT_LEN - 1) ; cnt >= 0 ; cnt--) { /* エリアに待避 */
        tmppt[cnt] = suuji_strings[cnt];
        if ((suuji_strings[cnt] != 0x00) && (setcnt == 0)) { /* 最初に数が出現した桁を取得する*/
            setcnt = cnt;
        }
    }
    /*
     * 退避している数字変換データ(逆順)を、数字データに変換しながら
     * 昇順に並べ替える。
     */
    cnt = 0;
    while (setcnt >= 0) {
        if ((tmppt[setcnt] == 0x00) || (tmppt[setcnt] == NJG_FIND_OKUMANCHO)) {
            suuji_strings[cnt] = 0;
        } else {
            if (tmppt[setcnt] == NJG_FIND_JYUUHYAKUSEN) { /* せんひゃくは、１１００となるべき*/
                suuji_strings[cnt] = 1;
            } else {
                suuji_strings[cnt] = (NJ_UINT8)(tmppt[setcnt] & 0x0f);
            }
        }
        setcnt--;
        cnt++;
    }
    iwnn->gw_len = cnt;

    return 0;
}

/**
 * 擬似候補取得処理（数字読み文字→数字(共通)）
 *
 * @param[in]  iwnn     作業領域
 * @param[in]  gfpos    擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos    擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi     擬似候補 読み
 * @param[in]  len      読み文字長
 * @param[out] p        擬似候補格納バッファ
 * @param[in]  gtype    擬似タイプ
 * @param[in]  giji_tbl 擬似候補テーブル
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_suuji_yomi(PART_OF_NJ_ENV *iwnn, 
                                    NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                    NJ_CHAR *yomi, NJ_UINT16 len,
                                    NJ_RESULT *p, NJ_UINT16 gtype,
                                    const NJ_UINT16 *giji_tbl) {
    NJ_INT16 i, index;
    NJ_UINT16 len0;


    /* 読み文字列から数字インデックスを作成 */
    if (njg_get_ksuuji_index_from_yomi(iwnn, yomi, len) < 0) {
        return 0;/* 作成できない */
    }

    /* 数字インデックスから数字データを作成 */
    if (njg_get_ksuuji_data_from_index(iwnn) < 0) {
        return 0;/* 作成できない */
    }

    /* 数字データから、数字候補テーブルをチェックする */
    len0 = 0;
    for (i = 0; i < iwnn->gw_len; i++) {
        index = (NJ_INT16)(iwnn->gijiwork[i] & 0xff);

        if (giji_tbl[index] != 0) {
            if ((giji_tbl[index] & 0xff00) != 0) {
                len0 += NJG_ZEN_MOJI_LEN;
            } else {
                len0 += NJG_HAN_MOJI_LEN;
            }
        } else {
            return 0;/* 作成できない */ /*NCH_FB*/
        }
    }
    /*
     * もし、解析完了時に最大表記文字列長を超過している場合
     * 疑似候補は作成しない。
     */
    if (len0 > NJ_MAX_RESULT_LEN) {
        return 0; /*NCH_FB*/
    }

    /*
     * 擬似候補作成が可能なら処理結果を作成
     */
    p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
    p->word.yomi = yomi;
    NJ_SET_YLEN_TO_STEM(&p->word, len);
    NJ_SET_KLEN_TO_STEM(&p->word, len0);
    NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
    NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
    NJ_SET_TYPE_TO_STEM(&p->word, (NJ_UINT8)gtype);
    NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

    return 1;
}

/**
 * 擬似候補取得処理（数字読み文字→漢数字(位)）
 *
 * @param[in]  iwnn   作業領域
 * @param[in]  gfpos  擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos  擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi   擬似候補 読み
 * @param[in]  len    読み文字長
 * @param[out] p      擬似候補格納バッファ
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 get_giji_zen_ksuuji_kurai(PART_OF_NJ_ENV *iwnn, 
                                          NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                          NJ_CHAR *yomi, NJ_UINT16 len,
                                          NJ_RESULT *p) {
    NJ_UINT16 len0 = 0;


    /* 読み文字列から数字インデックスを作成 */
    if (njg_get_ksuuji_index_from_yomi(iwnn, yomi, len) < 0) {
        return 0;/* 作成できない */
    }

    /* 数字インデックスから数字データを作成 */
    if (njg_get_ksuuji_data_from_index(iwnn) < 0) {
        return 0;/* 作成できない */
    }

    /* 変換後データ長算出 */
    /* 数字インデックスの数と漢数字(位)は一致する。*/
    len0 = (NJ_UINT16) (iwnn->kan_len * NJG_ZEN_MOJI_LEN);

    /*
     * もし、解析完了時に最大表記文字列長を超過している場合
     * 疑似候補は作成しない。
     */
    if (len0 > NJ_MAX_RESULT_LEN) {
        return 0; /*NCH_FB*/
    }
    /*
     * 擬似候補作成が可能なら処理結果を作成
     */
    p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
    p->word.yomi = yomi;
    NJ_SET_YLEN_TO_STEM(&p->word, len);
    NJ_SET_KLEN_TO_STEM(&p->word, len0);
    NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
    NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
    NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_ZEN_KANSUUJI_KURAI_YOMI);
    NJ_SET_FREQ_TO_STEM(&p->word, GIJI_GETSCORE(len));

    return 1;
}


/**
 * 擬似候補表記取得
 *
 * @param[in]  iwnn       作業領域
 * @param[in]  time_info  時間情報
 * @param[in]  word       文節情報
 * @param[out] candidate  表記文字列格納バッファ
 * @param[in]  size       candidate バイトサイズ
 *
 * @return  格納文字列の文字配列長
 */
static NJ_INT16 get_giji_candidate(PART_OF_NJ_ENV *iwnn, NJ_TIME_ST *time_info, NJ_WORD *word, NJ_CHAR *candidate,
                                   NJ_UINT16 size) {
    NJ_INT16 len = 0;
    NJ_INT16 i = 0;
    NJ_INT16  ret  = 0;
    NJ_CHAR temp_yomi[NJ_MAX_LEN + 1] = {NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,
                              NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,
                              NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,
                              NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,
                              NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL,NJ_CHAR_NUL};
    
    for (i = 0; i < NJ_GET_YLEN_FROM_STEM(word); i++)
        {
        temp_yomi[i] = *(word->yomi+i);
        }

    switch (NJ_GET_TYPE_FROM_STEM(word)) {
    case NJ_TYPE_HIRAGANA:
        /*  ひらがな（無変換）タイプ */
        len = gethiraString(word, candidate, size); /*NCH_FB*/
        break; /*NCH_FB*/

    case NJ_TYPE_KATAKANA:
        /*  全角カタカナタイプ */
        len = getKatakanaString(word, candidate, size);
        break;

    case NJ_TYPE_HANKATA:
        /*  半角カタカナタイプ */
        len = getHanKataString(word, candidate, size);
        break;

    case NJ_TYPE_HAN_EIJI_CAP:
        /*  半角英字 (先頭のみ大文字)タイプ */
        len = getEijiString(word, candidate, size, giji_han_eiji_lower_tbl,
                            giji_han_eiji_upper_tbl);
        break;

    case NJ_TYPE_ZEN_EIJI_CAP:
        /*  全角英字 (先頭のみ大文字)タイプ */
        len = getEijiString(word, candidate, size, giji_zen_eiji_lower_tbl,
                            giji_zen_eiji_upper_tbl);
        break;

    case NJ_TYPE_HAN_EIJI_UPPER:
        /*  半角英字 (全て大文字)タイプ */
        len = getEijiString(word, candidate, size, giji_han_eiji_upper_tbl,
                            NULL);
        break;

    case NJ_TYPE_ZEN_EIJI_UPPER:
        /*  全角英字 (全て大文字)タイプ */
        len = getEijiString (word, candidate, size, giji_zen_eiji_upper_tbl,
                             NULL);
        break;

    case NJ_TYPE_HAN_EIJI_LOWER:
        /*  半角英字 (全て小文字)タイプ */
        len = getEijiString(word, candidate, size, giji_han_eiji_lower_tbl,
                            NULL);
        break;

    case NJ_TYPE_ZEN_EIJI_LOWER:
        /* 全角英字 (全て小文字)タイプ */
        len = getEijiString(word, candidate, size, giji_zen_eiji_lower_tbl,
                            NULL);
        break;

    case NJ_TYPE_HAN_SUUJI:
        /* 半角数字タイプ */
        len = getSuujiString(word, candidate, size, 
                             &giji_suuji_tbl[NJG_SUUJI_TBL_HAN][0]);
        break;

    case NJ_TYPE_ZEN_SUUJI:
        /* 全角数字タイプ */
        len = getSuujiString(word, candidate, size, 
                             &giji_suuji_tbl[NJG_SUUJI_TBL_ZEN][0]);
        break;

    case NJ_TYPE_HAN_SUUJI_COMMA:
        /* 半角 数字  (コンマ区切り)      */
        len = getHanSuujiCommaString(iwnn, word, candidate, size);
        break;

    case NJ_TYPE_HAN_TIME_HH:
        /* 半角時間 (H時)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_THH]);
        break;

    case NJ_TYPE_HAN_TIME_MM:
        /* 半角時間 (M分)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_TMM]);
        break;

    case NJ_TYPE_HAN_TIME_HM:
        /* 半角時間 (H時M分)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_THM]);
        break;

    case NJ_TYPE_HAN_TIME_HMM:
        /* 半角時間 (H時MM分)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_THMM]);
        break;

    case NJ_TYPE_HAN_TIME_HHM:
        /* 半角時間 (HH時M分)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_THHM]);
        break;

    case NJ_TYPE_HAN_TIME_HHMM:
        /* 半角時間 (HH時MM分)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_THHMM]);
        break;

    case NJ_TYPE_HAN_TIME_HMM_SYM:
        /* 半角時間 (H:MM分)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_THMM_SYM]);
        break;

    case NJ_TYPE_HAN_TIME_HHMM_SYM:
        /* 半角時間 (HH:MM分)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_THHMM_SYM]);
        break;

    case NJ_TYPE_HAN_DATE_YYYY:
        /* 日付 (YYYY年)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_DYYYY]);
        break;

    case NJ_TYPE_HAN_DATE_MM:
        /* 半角日付 (M月)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_DMM]);
        break;

    case NJ_TYPE_HAN_DATE_DD:
        /* 半角日付 (D日)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_DDD]);
        break;

    case NJ_TYPE_HAN_DATE_MD:
        /* 半角日付 (M月D日)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_DMD]);
        break;

    case NJ_TYPE_HAN_DATE_MDD:
        /* 半角日付 (M月DD日)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_DMDD]);
        break;

    case NJ_TYPE_HAN_DATE_MMD:
        /* 半角日付 (MM月D日)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_DMMD]);
        break;

    case NJ_TYPE_HAN_DATE_MMDD:
        /* 半角日付 (MM月DD日)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_DMMDD]);
        break;

    case NJ_TYPE_HAN_DATE_MD_SYM:
        /* 半角日付 (M/D)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_DMD_SYM]);
        break;

    case NJ_TYPE_HAN_DATE_MDD_SYM:
        /* 半角日付 (M/DD)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_DMDD_SYM]);
        break;

    case NJ_TYPE_HAN_DATE_MMD_SYM:
        /* 半角日付 (MM/D)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_DMMD_SYM]);
        break;

    case NJ_TYPE_HAN_DATE_MMDD_SYM:
        /* 半角日付 (MM/DD)タイプ */
        len = getHanTimeDate(word, candidate, size, 
                             &giji_conv_table[NJG_TBL_NO_DMMDD_SYM]);
        break;

    case NJ_TYPE_HAN_SUUJI_YOMI:
        /* 数字読み文字→半角数字タイプ */
        len = getSuujiYomi(iwnn, word, candidate, size,
                           &giji_suuji_tbl[NJG_SUUJI_TBL_HAN][0]);
        break;

    case NJ_TYPE_ZEN_SUUJI_YOMI:
        /* 数字読み文字→全角数字タイプ */
        len = getSuujiYomi(iwnn, word, candidate, size,
                           &giji_suuji_tbl[NJG_SUUJI_TBL_ZEN][0]);
        break;

    case NJ_TYPE_ZEN_KANSUUJI_YOMI:
        /* 数字読み文字→漢数字タイプ */
        len = getSuujiYomi(iwnn, word, candidate, size,
                           &giji_suuji_tbl[NJG_SUUJI_TBL_KAN][0]);
        break;

    case NJ_TYPE_ZEN_KANSUUJI_KURAI_YOMI:
        /* 数字読み文字→漢数字 (位)タイプ */
        len = getZenksuujiKuraiYomiString(word, candidate, size);
        break;

    default:
        /************************************************/
        /* 擬似候補 カスタマイズ処理                    */
        /*   NJ_RESULTを基に擬似候補文字列を生成する    */
        /*   処理を拡張することができる。               */
        /************************************************/
        if ((NJ_GET_TYPE_FROM_STEM(word) >= (NJ_UINT8) NJ_GIJI_TYPE_CUST_MIN)
            && (NJ_GET_TYPE_FROM_STEM(word) <= (NJ_UINT8) NJ_GIJI_TYPE_CUST_MAX))
        {
            len = njg_cust_get_giji_candidate (iwnn, time_info, word, (NJ_UINT8*)candidate, size);
        }
        break;
    }

    if (len <= 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_CANDIDATE, NJ_ERR_BUFFER_NOT_ENOUGH);
    }
    return len;
}

/**
 * 全角かな擬似候補の候補文字列を取得
 *
 * @param[in]  word       文節情報
 * @param[out] candidate  候補文字列格納領域
 * @param[in]  size       candidate バイトサイズ
 *
 * @retval  >=0  格納した候補文字列文字長
 * @retval  <0   エラー
 */
static NJ_INT16 gethiraString(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size) { /*NCH_FB*/

    NJ_UINT16 len;


    len = NJ_GET_YLEN_FROM_STEM(word); /*NCH_FB*/
    if (((len + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) { /*NCH_FB*/
        return -1; /*NCH_FB*/
    }
    nj_strncpy(candidate, word->yomi, len); /*NCH_FB*/
    *(candidate + len) = NJ_CHAR_NUL; /*NCH_FB*/
    return (NJ_INT16)len; /*NCH_FB*/
}

/**
 * 全角カナ擬似候補の候補文字列を取得
 *
 * @param[in]  word       文節情報
 * @param[out] candidate  候補文字列格納領域
 * @param[in]  size       candidate バイトサイズ
 *
 * @retval  >=0  格納した候補文字列文字長
 * @retval  <0   エラー
 */
static NJ_INT16 getKatakanaString(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size) {
    NJ_CHAR  *yomi;    /* 読みのアドレス                           */
    NJ_CHAR  *dst;     /* 候補結果のアドレス                       */
    NJ_UINT16 len;     /* 読み文字列バイト長                       */
    NJ_UINT16 elm;     /* 読みの読み文字テーブル上の要素番号       */
    NJ_UINT16 len_cnt; /* 読み文字列バイト長カウンタ               */
    NJ_UINT16 klen;   /* 変換後文字列長                           */
    NJ_UINT16 kana;
    NJ_UINT16 prev_kana;
    NJ_UINT16 klen_max;


    /* 引数チェック */
    if ((NJ_UINT16)((NJ_GET_KLEN_FROM_STEM(word) + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) {
        /* 格納領域に収まらない場合はエラー */
        return -1; /*NCH*/
    }

    /* 変数初期化 */
    yomi = word->yomi;
    dst = candidate;
    len = NJ_GET_YLEN_FROM_STEM(word);
    klen_max = NJ_GET_KLEN_FROM_STEM(word);
    elm = 0;
    len_cnt = 0;
    prev_kana = 0x0000;
    klen = 0;

    while ((len_cnt < len) && (klen <= klen_max)) {
        /* klen <= klen_max とするのは、最後の「ヴ」を処理するため */

#ifndef NJ_OPT_UTF16
        if (NJG_IS_WCHAR(*yomi)) {
            /*
             * 読みが2byte
             */
#endif /* !NJ_OPT_UTF16 */

            /* 読みが読み文字テーブルに存在するか判定 */
            if (njg_get_yomi_table(NJG_CONV_TO_WCHAR(yomi), &elm) == 0) {
                /* 読みが読み文字テーブルに存在 */
                if (NJG_IS_GIJI_SYM_TBL(elm)) {
                    /* 読みが記号 */

                    /* 最上位bitを元に戻す */
                    elm ^= (NJ_UINT16)NJ_GIJI_SYM_TBL;
                    kana = giji_katakana_sym_tbl[elm];
                    if (kana == 0x0000) {
                        /* 候補対象が存在しないため変換せずにそのまま */
                        NJG_COPY_W_CHAR(dst, yomi); /*NCH_FB*/
                    } else {
                        /* テーブルに定義あり（候補が2byte）*/
                        if ((prev_kana == NJG_KATAKANA_U)
                            && (kana == NJG_KATAKANA_DAKUON)) {
                            /*
                             *「ウ゛」は「ヴ」に変換する
                             */
                            /* 直前の「ウ」の位置に戻す */
                            dst -= NJG_ZEN_MOJI_LEN; /*NCH_FB*/
                            klen -= NJG_ZEN_MOJI_LEN; /*NCH_FB*/
                            /* ヴを書く */
                            NJG_COPY_INT16_TO_CHAR(dst, NJG_KATAKANA_VU); /*NCH_FB*/
                        } else {
                            /* テーブルに定義された文字を書く */
                            NJG_COPY_INT16_TO_CHAR(dst, kana);
                        }
                    }
                } else {
                    /* 読みがひらがな */
                    kana = giji_katakana_tbl[elm];
                    if (kana == 0x0000) {
                        /* 候補対象が存在しないため変換せずにそのまま */
                        NJG_COPY_W_CHAR(dst, yomi); /*NCH_FB*/
                    } else {
                        /* テーブルに定義あり（候補が2byte）*/
                        NJG_COPY_INT16_TO_CHAR(dst, kana);
                    }
                }
                prev_kana = kana;
            } else {
                /* (読みが2byte)
                 * 読みが読み文字テーブルに存在しない
                 * 読みをそのまま出力
                 */
                NJG_COPY_W_CHAR(dst, yomi); /*NCH_FB*/
                prev_kana = 0x0000; /*NCH_FB*/
            }
            len_cnt += NJG_ZEN_MOJI_LEN;
            dst += NJG_ZEN_MOJI_LEN;
            yomi += NJG_ZEN_MOJI_LEN;
            klen += NJG_ZEN_MOJI_LEN;
#ifndef NJ_OPT_UTF16
        } else {
            /*
             * 読みが1byte
             * 読みが読み文字テーブルに存在しない
             * 読みをそのまま出力
             */
            *dst = *yomi; /*NCH*/
            prev_kana = 0x0000; /*NCH*/
            len_cnt += NJG_HAN_MOJI_LEN; /*NCH*/
            dst += NJG_HAN_MOJI_LEN; /*NCH*/
            yomi += NJG_HAN_MOJI_LEN; /*NCH*/
            klen += NJG_HAN_MOJI_LEN; /*NCH*/
        }
#endif /* !NJ_OPT_UTF16 */
    }
    if (klen < klen_max) {
        /* ここを通ることは無いが念のため */
        *dst = NJ_CHAR_NUL; /*NCH*/
    } else {
        candidate[klen] = NJ_CHAR_NUL;
    }

    /* 変換後文字列長をセット */
    NJ_SET_KLEN_TO_STEM(word, klen);

    return (NJ_INT16)(NJ_GET_KLEN_FROM_STEM(word));
}


/**
 * 半角カナ擬似候補の候補文字列を取得
 *
 * @param[in]  word       文節情報
 * @param[out] candidate  候補文字列格納領域
 * @param[in]  size       candidate バイトサイズ
 *
 * @retval >=0 格納した候補文字列文字長
 * @retval <0  エラー
 */
static NJ_INT16 getHanKataString(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size) {

    NJ_CHAR   *yomi;    /* 読みのアドレス                           */
    NJ_CHAR   *dst;     /* 候補結果のアドレス                       */
    NJ_UINT16 len;     /* 読み文字列バイト長                       */
    NJ_UINT16 elm;     /* 読みの読み文字テーブル上の要素番号       */
    NJ_UINT16 len_cnt; /* 読み文字列バイト長カウンタ               */
    NJ_UINT16 giji_yomi;    /* 対象読み文字第１要素             */
    NJ_UINT16 giji_yomi2;   /* 対象読み文字第２要素             */
    NJ_UINT16 klen;
    NJ_UINT16 klen_max;


    yomi = word->yomi;
    dst = candidate;
    len = NJ_GET_YLEN_FROM_STEM(word);
    klen = 0;
    klen_max = NJ_GET_KLEN_FROM_STEM(word);
    elm = 0;
    len_cnt = 0;

    if ((NJ_UINT16)((NJ_GET_KLEN_FROM_STEM(word) + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) {
        /* 格納領域に収まらない場合はエラー */
        return -1; /*NCH*/
    }

    while ((len_cnt < len) && (klen < klen_max)) {
#ifndef NJ_OPT_UTF16
        /*
         * 読みが2byte
         */
        if (NJG_IS_WCHAR(*yomi)) {
#endif /* NJ_OPT_UTF16 */
            len_cnt += NJG_ZEN_MOJI_LEN;

            /* 読みが読み文字テーブルに存在するか判定 */
            if (njg_get_yomi_table(NJG_CONV_TO_WCHAR(yomi), &elm) == 0) {
                /*
                 * 読みが読み文字テーブルに存在
                 */
                if (NJG_IS_GIJI_SYM_TBL(elm)) {
                    /*
                     * 読みが記号
                     */
                    elm ^= NJ_GIJI_SYM_TBL; /* 最上位bitを元に戻す */
                    giji_yomi  = giji_hankata_sym_tbl[elm][0];
                    giji_yomi2 = giji_hankata_sym_tbl[elm][1];
                } else {
                    giji_yomi  = giji_hankata_tbl[elm][0];
                    giji_yomi2 = giji_hankata_tbl[elm][1];
                }
                /*
                 * カナの変換
                 */
                if (giji_yomi == 0) {
                    /* 候補対象が存在しないため変換せずにそのまま */
                    NJG_COPY_W_CHAR(dst, yomi); /*NCH*/
                    dst  += NJG_ZEN_MOJI_LEN; /*NCH*/
                    klen += NJG_ZEN_MOJI_LEN; /*NCH*/
#ifndef NJ_OPT_UTF16
                } else if ((giji_yomi & 0xff00) == 0) {
                    /* 候補が1byte */
                    *dst = (NJ_UINT8)(giji_yomi & 0x00ff);
                    dst  += NJG_HAN_MOJI_LEN;
                    klen += NJG_HAN_MOJI_LEN;
#endif /* !NJ_OPT_UTF16 */
                } else {
                    /* 候補が2byte */
                    NJG_COPY_INT16_TO_CHAR(dst, giji_yomi); /*NCH*/
                    dst  += NJG_ZEN_MOJI_LEN; /*NCH*/
                    klen += NJG_ZEN_MOJI_LEN; /*NCH*/
                }
                /*
                 * 濁点・半濁点の変換
                 * 候補対象が存在しないときはなにもしない。
                 */
                if (giji_yomi2 != 0) {
#ifdef NJ_OPT_UTF16
                    NJG_COPY_INT16_TO_CHAR(dst, giji_yomi2);
                    dst  += NJG_ZEN_MOJI_LEN;
                    klen += NJG_ZEN_MOJI_LEN;
#else /* NJ_OPT_UTF16 */
                    if ((giji_yomi2 & 0xff00) == 0) {
                        /* 候補が1byte */
                        *dst = (NJ_UINT8)(giji_yomi2 & 0x00ff);
                        dst  += NJG_HAN_MOJI_LEN;
                        klen += NJG_HAN_MOJI_LEN;
                    } else {
                        /* 候補が2byte */
                        NJG_COPY_INT16_TO_CHAR(dst, giji_yomi2); /*NCH*/
                        dst  += NJG_ZEN_MOJI_LEN; /*NCH*/
                        klen += NJG_ZEN_MOJI_LEN; /*NCH*/
                    }
#endif /* NJ_OPT_UTF16 */
                }
            } else {
                /*
                 * 読みが読み文字テーブルに存在しない場合は、
                 * そのまま出力
                 */
                NJG_COPY_W_CHAR(dst, yomi); /*NCH*/
                dst += NJG_ZEN_MOJI_LEN; /*NCH*/
                klen += NJG_ZEN_MOJI_LEN; /*NCH*/
            }
            yomi += NJG_ZEN_MOJI_LEN;
#ifndef NJ_OPT_UTF16
        } else {
            /*
             * 読みが1byte
             * 読みが読み文字テーブルに存在しない場合は、
             * そのまま出力
             */
            len_cnt += NJG_HAN_MOJI_LEN; /*NCH_FB*/
            *dst = *yomi;  /*NCH_FB*/
            dst  += NJG_HAN_MOJI_LEN;  /*NCH_FB*/
            klen += NJG_HAN_MOJI_LEN;  /*NCH_FB*/
            yomi += NJG_HAN_MOJI_LEN;  /*NCH_FB*/
        }
#endif /* ! NJ_OPT_UTF16 */
    }
    *dst = NJ_CHAR_NUL;

    return (NJ_INT16)NJ_GET_KLEN_FROM_STEM(word);
}

/**
 * 英字擬似候補の候補文字列を取得 (共通)
 *
 * @param[in]  word         文節情報
 * @param[out] candidate    候補文字列格納領域
 * @param[in]  size         candidate バイトサイズ
 * @param[in]  giji_tbl     英字擬似候補テーブル
 * @param[in]  giji_tbl_cap 先頭文字専用の英字擬似候補テーブル(NULL:指定なし)
 *
 * @retval  >=0  格納した候補文字列文字長
 * @retval  <0   エラー
 */
static NJ_INT16 getEijiString(NJ_WORD *word, NJ_CHAR *candidate,
                              NJ_UINT16 size,
                              const NJ_UINT16 *giji_tbl,
                              const NJ_UINT16 *giji_tbl_cap) {
    const NJ_UINT16 *ctbl;
    NJ_CHAR  *yomi;  /* 読みのアドレス                     */
    NJ_CHAR  *dst;   /* 候補結果のアドレス                 */
    NJ_UINT16 len;   /* 読み文字列バイト長                 */
    NJ_UINT16 cnt;   /* ループカウンタ                     */
    NJ_UINT16 elm;   /* 読みの読み文字テーブル上の要素番号 */


    yomi = word->yomi;
    dst = candidate;
    len = NJ_GET_YLEN_FROM_STEM(word);
    if ((NJ_UINT16)((NJ_GET_KLEN_FROM_STEM(word) + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) {
        /* 格納領域に収まらない場合はエラー */
        return -1; /*NCH_FB*/
    }

    elm = 0;
    cnt = len;

    /*
     * 先頭文字のみ、別のテーブルで変換する。
     */
    if (giji_tbl_cap != NULL) {
        ctbl = giji_tbl_cap;
    } else {
        ctbl = giji_tbl;
    }

    while (cnt > 0) {
        /* 文字コードをデータ変換し、読み文字テーブルを検索 */
        if (njg_get_yomi_table (NJG_CONV_TO_WCHAR(yomi), &elm) < 0) {
            /* 読み文字テーブルにデータがないので変換対象外とする
             * 但し、NJ_RESULT作成時に既にチェックしているので
             * ここでこのエラーが発生することはない。
             * 発生した場合は、致命的なエラーとなる。
             */
            return -1; /*NCH*/
        }
        
        /*
         * 擬似候補取得 (文字コードのバイト数をみて格納する)
         */
#ifdef NJ_OPT_UTF16
        NJG_COPY_INT16_TO_CHAR(dst, ctbl[elm]);
        dst += NJG_ZEN_MOJI_LEN;
#else /* NJ_OPT_UTF16 */
        if ((ctbl[elm] & 0xFF00) == 0) {
            *dst = (NJ_UINT8)(ctbl[elm] & 0x00FF);
            dst += NJG_HAN_MOJI_LEN;
        } else {
            NJG_COPY_INT16_TO_CHAR(dst, ctbl[elm]);
            dst += NJG_ZEN_MOJI_LEN;
        }
#endif /* NJ_OPT_UTF16 */
        yomi += NJG_ZEN_MOJI_LEN;
        cnt -= NJG_ZEN_MOJI_LEN;
        /* 2文字目以降の擬似テーブルをセットする */
        ctbl = giji_tbl;
    }
    *dst = NJ_CHAR_NUL;

    return (NJ_INT16)NJ_GET_KLEN_FROM_STEM(word);
}

/**
 * 数字擬似候補の候補文字列を取得(共通)
 *
 * @param[in]  word       文節情報
 * @param[out] candidate  候補文字列格納領域
 * @param[in]  size       candidate バイトサイズ
 * @param[in]  giji_tbl   数字擬似候補テーブル
 *
 * @retval >= 0 格納した候補文字列文字長
 * @retval <0   エラー
 */
static NJ_INT16 getSuujiString(NJ_WORD *word, NJ_CHAR *candidate,
                               NJ_UINT16 size, const NJ_UINT16 *giji_tbl) {
    NJ_CHAR   *yomi, *dst;
    NJ_UINT16 len;
    NJ_UINT16 i, j;
    NJ_UINT16 index;
    NJ_UINT16 keynum, toggle;


    yomi = word->yomi;
    dst = candidate;
    len = NJ_GET_YLEN_FROM_STEM(word);
    if ((NJ_UINT16)((NJ_GET_KLEN_FROM_STEM(word) + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) {
        /* 格納領域に収まらない場合はエラー */
        return -1; /*NCH*/
    }

    for (i = 0; i < len; yomi += NJG_ZEN_MOJI_LEN, i += NJG_ZEN_MOJI_LEN) {
#ifndef NJ_OPT_UTF16
        /* 2バイト文字の場合、読み文字テーブルを検索 */
        if (NJG_IS_WCHAR(*yomi)) {
#endif /* !NJ_OPT_UTF16 */
            /* 文字コードをデータ変換し、読み文字テーブルを検索 */
            if (njg_get_yomi_table(NJG_CONV_TO_WCHAR(yomi), &index) < 0) {
                /* 読み文字テーブルにデータがないので変換対象外とする
                 * 但し、NJ_RESULT作成時に既にチェックしているので
                 * ここでこのエラーが発生することはない。
                 * 発生した場合は、致命的なエラーとなる。
                 */
                return -1; /*NCH*/
            }
            /*
             * 読みテーブルより、変換数値情報を取得する。
             */
            keynum = NJG_GET_NUM_YOMI(index);
            if (keynum == NJ_NUM_NON) {
                /* 読み文字テーブルに変換数値情報がないため、
                 * 変換対象外とする。
                 * 但し、NJ_RESULT作成時に既にチェックしているので
                 * ここでこのエラーが発生することはない。
                 * 発生した場合は、致命的なエラーとなる。
                 */
                return -1; /*NCH*/
            }
            toggle = NJG_GET_TOGGLE_YOMI(index);
#ifdef NJ_OPT_UTF16
            /*
             * 格納するトグル数分並べる
             */
            for (j = 0; j < toggle; j++) {
                NJG_COPY_INT16_TO_CHAR(dst, giji_tbl[keynum]);
                dst += NJG_ZEN_MOJI_LEN;
            }
#else /* NJ_OPT_UTF16 */
            /*
             * 半角数字の文字コードが2バイトなら、上位バイトから格納する。
             * トグル数分並べる。
             *
             *   上位バイトが0なら、文字コードは下位1バイトのみを使用する
             *   ものとし、同じ位置に下位コードを上書きする。
             *   但し、JISX0201ではSJIS/EUC共に1バイトのみなので、
             *   2バイトの処理は行われない。
             */
            if ((giji_tbl[keynum] & 0xff00) != 0) {
                for (j = 0; j < toggle; j++) {
                    NJG_COPY_INT16_TO_CHAR(dst, giji_tbl[keynum]);
                    dst += NJG_ZEN_MOJI_LEN;
                }
            } else {
                for (j = 0; j < toggle; j++) {
                    *dst = (NJ_UINT8)(giji_tbl[keynum] & 0xff);
                    dst += NJG_HAN_MOJI_LEN;
                }
            }
#endif /* NJ_OPT_UTF16 */
#ifndef NJ_OPT_UTF16
        } else {
            /* その他の文字種が混在することになり、
             * その場合は変換対象外とする
             * 但し、NJ_RESULT作成時に既にチェックしているので
             * ここでこのエラーが発生することはない。
             * 発生した場合は、致命的なエラーとなる。
             */
            return -1; /*NCH*/
        }
#endif /* !NJ_OPT_UTF16 */
    }
    *dst = NJ_CHAR_NUL;

    return (NJ_INT16)NJ_GET_KLEN_FROM_STEM(word);
}

/**
 * 半角数字(コンマ区切り)擬似候補の候補文字列を取得
 *
 * @param[in]  iwnn       作業領域
 * @param[in]  word       文節情報
 * @param[out] candidate  候補文字列格納領域
 * @param[in]  size       candidate バイトサイズ
 *
 * @retval >=0  格納した候補文字列文字長
 * @retval <0   エラー
 */
static NJ_INT16 getHanSuujiCommaString(PART_OF_NJ_ENV *iwnn, NJ_WORD *word,
                                       NJ_CHAR *candidate, NJ_UINT16 size) {
    NJ_CHAR   *yomi, *dst, *gw;
    NJ_UINT16 len;
    NJ_UINT16 i, j;
    NJ_UINT16 index;
    NJ_UINT16 keynum, toggle;
    NJ_UINT16 keta = 0;


    yomi = word->yomi;
    dst = candidate;
    gw = iwnn->gijiwork;

    len = NJ_GET_YLEN_FROM_STEM(word);
    if ((NJ_UINT16)((NJ_GET_KLEN_FROM_STEM(word) + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) {
        /* 格納領域に収まらない場合はエラー */
        return -1; /*NCH*/
    }

    for (i = 0; i < len; yomi += NJG_ZEN_MOJI_LEN, i += NJG_ZEN_MOJI_LEN) {
#ifndef NJ_OPT_UTF16
        /* 2バイト文字の場合、読み文字テーブルを検索 */
        if (NJG_IS_WCHAR(*yomi)) {
#endif /* !NJ_OPT_UTF16 */
            /* 文字コードをデータ変換し、読み文字テーブルを検索 */
            if (njg_get_yomi_table(NJG_CONV_TO_WCHAR(yomi), &index) < 0) {
                /* 読み文字テーブルにデータがないので変換対象外とする
                 * 但し、NJ_RESULT作成時に既にチェックしているので
                 * ここでこのエラーが発生することはない。
                 * 発生した場合は、致命的なエラーとなる。
                 */
                return -1; /*NCH*/
            }
            /*
             * 読みテーブルより、変換数値情報を取得する。
             */
            keynum = NJG_GET_NUM_YOMI(index);
            if (keynum == NJ_NUM_NON) {
                /* 読み文字テーブルに変換数値情報がないため、
                 * 変換対象外とする。
                 * 但し、NJ_RESULT作成時に既にチェックしているので
                 * ここでこのエラーが発生することはない。
                 * 発生した場合は、致命的なエラーとなる。
                 */
                return -1; /*NCH*/
            }
            toggle = NJG_GET_TOGGLE_YOMI(index);
#ifdef NJ_OPT_UTF16
            /*
             * 格納するトグル数分並べる
             */
            for (j = 0; j < toggle; j++) {
                NJG_COPY_INT16_TO_CHAR(gw, giji_suuji_tbl[NJG_SUUJI_TBL_HAN][keynum]);
                gw += NJG_ZEN_MOJI_LEN;
            }
#else /* NJ_OPT_UTF16 */
            /*
             * 半角数字の文字コードが2バイトなら、上位バイトから格納する。
             * トグル数分並べる。
             *
             *   上位バイトが0なら、文字コードは下位1バイトのみを使用する
             *   ものとし、同じ位置に下位コードを上書きする。
             *   但し、JISX0201ではSJIS/EUC共に1バイトのみなので、
             *   2バイトの処理は行われない。
             */
            if ((giji_suuji_tbl[NJG_SUUJI_TBL_HAN][keynum] & 0xff00) != 0) {
                for (j = 0; j < toggle; j++) { /*NCH*/
                    NJG_COPY_INT16_TO_CHAR(gw, giji_suuji_tbl[NJG_SUUJI_TBL_HAN][keynum]); /*NCH*/
                    gw += NJG_ZEN_MOJI_LEN; /*NCH*/
                }
            } else {
                for (j = 0; j < toggle; j++) {
                    *gw = (NJ_UINT8)(giji_suuji_tbl[NJG_SUUJI_TBL_HAN][keynum] & 0xff);
                    gw += NJG_HAN_MOJI_LEN;
                }
            }
#endif /* NJ_OPT_UTF16 */
            /* 擬似候補の桁数(文字数)を算出 */
            keta += toggle;
#ifndef NJ_OPT_UTF16
        } else {
            /* その他の文字種が混在することになり、
             * その場合は変換対象外とする
             * 但し、NJ_RESULT作成時に既にチェックしているので
             * ここでこのエラーが発生することはない。
             * 発生した場合は、致命的なエラーとなる。
             */
            return -1; /*NCH*/
        }
#endif /* !NJ_OPT_UTF16 */
    }
    *gw = NJ_CHAR_NUL;

    gw = iwnn->gijiwork;
    if (keta <= 3) {
        /* カンマがなければそのままコピー */
        while (*gw != NJ_CHAR_NUL) {
            *dst++ = *gw++;
        }
    } else {
        /* 先頭のカンマまでまずコピー */
        len = (NJ_UINT16)((keta - 1) % 3);
        for (i = 0; i <= len; i++) {
            /* 半角なので１文字要素ずつコピー */
            *dst = *gw;
            dst += NJG_HAN_MOJI_LEN;
            gw  += NJG_HAN_MOJI_LEN;
        }
        /* 後は3桁ずつカンマを格納しながらコピー */
        while (*gw != NJ_CHAR_NUL) {
#ifdef NJ_OPT_UTF16
            NJG_COPY_INT16_TO_CHAR(dst, NJG_COMMA_MARK);
            dst += NJG_HAN_MOJI_LEN;
#else /* NJ_OPT_UTF16 */
            *dst++ = NJG_COMMA_MARK;
#endif /* NJ_OPT_UTF16 */
            for (i = 0; i < 3; i++) {
                /* 半角なので１文字要素ずつコピー */
                *dst = *gw;
                dst += NJG_HAN_MOJI_LEN;
                gw  += NJG_HAN_MOJI_LEN;
            }
        }
    }

    *dst = NJ_CHAR_NUL;

    return (NJ_INT16)NJ_GET_KLEN_FROM_STEM(word);
}

/**
 * 時間・日付擬似候補の共通作成処理
 *
 * @param[in]  word      文節情報
 * @param[out] candidate 候補文字列格納領域
 * @param[in]  size      candidate バイトサイズ
 * @param[in]  ctbl      時間・日付擬似候補作成テーブル<br>
 *                       作成する擬似候補のテーブルのみセットされる。
 *
 * @retval >=0  格納した候補文字列文字長
 * @retval <0   エラー
 */
static NJ_INT16 getHanTimeDate(NJ_WORD *word,
                               NJ_CHAR *candidate, NJ_UINT16 size,
                               const NJ_CONVERT_TBL *ctbl) {
    NJ_CHAR   *yomi;
    NJ_CHAR   *dst;
    NJ_UINT16 index;
    NJ_UINT16 keynum, toggle;
    NJ_UINT16 len;
    NJ_UINT16 i, j;
    NJ_INT16  fmoji;


    if ((NJ_UINT16)((NJ_GET_KLEN_FROM_STEM(word) + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) {
        /* 格納領域に収まらない場合はエラー */
        return -1; /*NCH*/
    }

    yomi = word->yomi;
    dst = candidate;

    len  = NJ_GET_YLEN_FROM_STEM(word);

    /* 1桁目の単位を格納する桁数を取り出す */
    fmoji = ctbl->f_digit;

    for (i = 0; i < len; yomi += NJG_ZEN_MOJI_LEN, i += NJG_ZEN_MOJI_LEN) {
#ifndef NJ_OPT_UTF16
        /* 2バイト文字の場合、読み文字テーブルを検索 */
        if (NJG_IS_WCHAR(*yomi)) {
#endif /* !NJ_OPT_UTF16 */
            if (njg_get_yomi_table(NJG_CONV_TO_WCHAR(yomi), &index) < 0) {
                /* NJ_RESULT作成時に既にチェックしているので
                 * ここでこのエラーが発生することはない。
                 * 発生した場合は、致命的なエラーとなる。
                 */
                return -1; /*NCH*/
            }
            /*
             * 読みテーブルより、変換数値情報を取得する。
             */
            keynum = NJG_GET_NUM_YOMI(index);
            if (keynum == NJ_NUM_NON) {
                /* 読み文字テーブルに変換数値情報がないため、
                 * 変換対象外とする。
                 * 但し、NJ_RESULT作成時に既にチェックしているので
                 * ここでこのエラーが発生することはない。
                 * 発生した場合は、致命的なエラーとなる。
                 */
                return -1; /*NCH*/
            }
            toggle = NJG_GET_TOGGLE_YOMI(index);

            /*
             * 数字擬似文字をトグル数分並べる。
             */
            for (j = 0; j < toggle; j++) {
#ifdef NJ_OPT_UTF16
                NJG_COPY_INT16_TO_CHAR(dst, giji_suuji_tbl[NJG_SUUJI_TBL_HAN][keynum]);
                dst += NJG_ZEN_MOJI_LEN;
#else /* NJ_OPT_UTF16 */
                if ((giji_suuji_tbl[NJG_SUUJI_TBL_HAN][keynum] & 0xff00) == 0) {
                    /* 数字擬似テーブルの設定文字が1バイト文字のときの処理 */
                    *dst = (NJ_UINT8)(giji_suuji_tbl[NJG_SUUJI_TBL_HAN][keynum] & 0xff);
                    dst += NJG_HAN_MOJI_LEN;
                } else {
                    /* 数字擬似テーブルの設定文字が2バイト文字のときの処理 */
                    NJG_COPY_INT16_TO_CHAR(dst, giji_suuji_tbl[NJG_SUUJI_TBL_HAN][keynum]); /*NCH_FB*/
                    dst += NJG_ZEN_MOJI_LEN; /*NCH_FB*/
                }
#endif /* NJ_OPT_UTF16 */
                /*
                 * 単位文字コードを格納する
                 */
                if (fmoji > 0) {
                    fmoji--;
                    if ((fmoji == 0) && (ctbl->f_code != 0)) {
                        /* 設定されていれば、単位文字列を格納 */
#ifdef NJ_OPT_UTF16
                        NJG_COPY_INT16_TO_CHAR(dst, ctbl->f_code);
                        dst += NJG_ZEN_MOJI_LEN;
#else /* NJ_OPT_UTF16 */
                        if ((ctbl->f_code & 0xff00) != 0) {
                            /* 全角文字なら上位バイトも格納 */
                            *dst++ = (NJ_UINT8)((ctbl->f_code >> 8) & 0xff);
                        }
                        *dst++ = (NJ_UINT8)(ctbl->f_code & 0xff);
#endif /* NJ_OPT_UTF16 */
                    }
                }
            }
#ifndef NJ_OPT_UTF16
        } else {
            /* NJ_RESULT作成時に既にチェックしているので
             * ここでこのエラーが発生することはない。
             * 発生した場合は、致命的なエラーとなる。
             */
            return -1; /*NCH*/
        }
#endif /* NJ_OPT_UTF16 */
    }
    if (ctbl->s_code != 0) {
        /* 設定されていれば、単位文字列を格納 */
#ifdef NJ_OPT_UTF16
        NJG_COPY_INT16_TO_CHAR(dst, ctbl->s_code);
        dst += NJG_ZEN_MOJI_LEN;
#else /* NJ_OPT_UTF16 */
        if ((ctbl->s_code & 0xff00) != 0) {
            /* 全角文字なら上位バイトも格納 */
            *dst++ = (NJ_UINT8)((ctbl->s_code >> 8) & 0xff);
        }
        *dst++ = (NJ_UINT8)(ctbl->s_code & 0xff);
#endif /* NJ_OPT_UTF16 */
    }

    *dst = NJ_CHAR_NUL;

    /* 成功 */
    return (NJ_INT16)NJ_GET_KLEN_FROM_STEM(word);
}

/**
 * 数字読み文字→数字候補(共通)
 *
 * @param[in]  iwnn       作業領域
 * @param[in]  word       文節情報
 * @param[out] candidate  候補文字列格納領域
 * @param[in]  size       candidate バイトサイズ
 * @param[in]  giji_tbl   擬似候補変換テーブル
 *
 * @retval >=0   格納した候補文字列文字長
 * @retval <0    エラー
 */
static NJ_INT16 getSuujiYomi(PART_OF_NJ_ENV *iwnn, NJ_WORD *word, 
                             NJ_CHAR *candidate, NJ_UINT16 size,
                             const NJ_UINT16 *giji_tbl) {
    NJ_CHAR   *yomi, *dst;
    NJ_UINT16 len;
    NJ_INT16 i, index;


    yomi = word->yomi;
    dst = candidate;
    len = NJ_GET_YLEN_FROM_STEM(word);
    if ((NJ_UINT16)((NJ_GET_KLEN_FROM_STEM(word) + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) {
        /* 格納領域に収まらない場合はエラー */
        return -1; /*NCH*/
    }

    /* 読み文字列から数字インデックスを作成 */
    if (njg_get_ksuuji_index_from_yomi(iwnn, yomi, len) < 0) {
        return 0;/* 作成できない */ /*NCH_FB*/
    }

    /* 数字インデックスから数字データを作成 */
    if (njg_get_ksuuji_data_from_index(iwnn) < 0) {
        return 0;/* 作成できない */ /*NCH_FB*/
    }

    /* 数字データから、数字候補テーブル取り出す */
    for (i = 0; i < iwnn->gw_len; i++) {
        index = (NJ_INT16)(iwnn->gijiwork[i] & 0xff);

        if (giji_tbl[index] != 0) {
#ifdef NJ_OPT_UTF16
            NJG_COPY_INT16_TO_CHAR(dst, giji_tbl[index]);
            dst += NJG_ZEN_MOJI_LEN;
#else /* NJ_OPT_UTF16 */
            if ((giji_tbl[index] & 0xff00) != 0) {
                NJG_COPY_INT16_TO_CHAR(dst, giji_tbl[index]);
                dst += NJG_ZEN_MOJI_LEN;
            } else {
                *dst = (NJ_UINT8)(giji_tbl[index] & 0xff);
                dst += NJG_HAN_MOJI_LEN;
            }
#endif /* NJ_OPT_UTF16 */
        } else {
            return -1;/* 作成できない */ /*NCH_FB*/
        }
    }
    *dst = NJ_CHAR_NUL;

    return (NJ_INT16)NJ_GET_KLEN_FROM_STEM(word);
}

/**
 * 数字読み文字→漢数字(位)タイプ
 *
 * @param[in]  word      文節情報
 * @param[out] candidate 候補文字列格納領域
 * @param[in]  size      candidate バイトサイズ
 *
 * @retval >=0  格納した候補文字列文字長
 * @retval <0   エラー
 */
static NJ_INT16 getZenksuujiKuraiYomiString(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size) {
    NJ_CHAR   *yomi, *dst;
    NJ_UINT16 len, cnt;
    NJ_INT16  ret;
    NJ_UINT8  yomi_len;


    yomi = word->yomi;
    dst = candidate;
    len = NJ_GET_YLEN_FROM_STEM(word);
    if ((NJ_UINT16)((NJ_GET_KLEN_FROM_STEM(word) + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) {
        /* 格納領域に収まらない場合はエラー */
        return -1; /*NCH*/
    }

    for (cnt = 0; cnt < len;) {
        if ((ret = njg_get_ksuuji_ketapos((yomi + cnt), &yomi_len)) > 0) {
            if ((ret >= 0) && (ret <= 9))  { /* 1から９の数字*/
                NJG_COPY_INT16_TO_CHAR(dst, giji_suuji_tbl[NJG_SUUJI_TBL_KAN][ret]);
            } else {
                switch (ret) {
                case NJ_KETA_JYUU: /* 十 */
                    NJG_COPY_INT16_TO_CHAR(dst, kurai_ksuuji_table[0]);
                    break;
                case NJ_KETA_HYAKU: /* 百 */
                    NJG_COPY_INT16_TO_CHAR(dst, kurai_ksuuji_table[1]);
                    break;
                case NJ_KETA_SEN: /* 千 */
                case NJ_KETA_ZEN: /* 千 */
                    NJG_COPY_INT16_TO_CHAR(dst, kurai_ksuuji_table[2]);
                    break;
                case NJ_KETA_MAN: /* 万  */
                    NJG_COPY_INT16_TO_CHAR(dst, kurai_ksuuji_table[3]);
                    break;
                case NJ_KETA_OKU: /* 億  */
                    NJG_COPY_INT16_TO_CHAR(dst, kurai_ksuuji_table[4]);
                    break;
                default:
                    /* テーブルの設定値エラーとして変換対象外とする */
                    return -1; /*NCH*/
                }
            }
            dst += NJG_ZEN_MOJI_LEN;
            cnt += (NJ_UINT16)yomi_len;
        } else {
            /* テーブルと一致しないため変換対象外とする */
            return -1; /*NCH_FB*/
        }
    }
    *dst = NJ_CHAR_NUL;

    return (NJ_INT16)NJ_GET_KLEN_FROM_STEM(word);
}

/**
 * 読み文字テーブルから、指定した読み文字に対応する要素番号を取得する
 *
 * @param[in]  yomi     読み文字
 * @param[out] elm_num  読み文字に対応する要素番号
 *
 * @retval  0   読み文字に対応する要素番号を取得できた
 * @retval  <0  読み文字に対応する要素番号を取得できない
 */
static NJ_INT16 njg_get_yomi_table(NJ_UINT16 yomi, NJ_UINT16 *elm_num) {

    NJ_INT16  tbl_num = 0;
    NJ_INT16  cnt = 0;


    /*
     * 引数チェック
     */
    if (yomi == 0x0000) {
        /* 読み文字が0x0000の場合はエラー */
        return -1; /*NCH_FB*/
    }

    /*
     * 読み文字(ひらがな)チェック
     */

    /* 読み文字に対応する要素番号を取得 */
    tbl_num = (NJ_INT16)(yomi - giji_yomi_hira_tbl[0].yomi);

    if ( (tbl_num >= 0) && (tbl_num <= (NJ_YOMI_HIRA_MAX - 1)) ) {
        /* 読み文字 ひらがなテーブルに対象要素発見 */
        if ( yomi == giji_yomi_hira_tbl[tbl_num].yomi ) {
            /* 読み文字と対象要素の読みが一致 */
            *elm_num = (NJ_UINT16)tbl_num;
            return 0;
        }
        /* 読み文字は「ひらがな」とみなさない */
        return -1;
    }

    /*
     * 読み文字(記号(ひらがな))チェック
     */
    for ( cnt = 0; cnt < NJ_YOMI_SYM_MAX; cnt++ ) {
        if ( yomi == giji_yomi_sym_tbl[cnt].yomi ) {
            /*
             * 読み文字(記号(ひらがな))取得成功
             * ひらがな取得時と区別をつけるため
             * 最上位bitに1を立てる
             */
            *elm_num = (NJ_UINT16)(cnt | NJ_GIJI_SYM_TBL);
            return 0;
        }
    }

    /* 読み文字がひらがな、記号(ひらがな)でないときはエラー */
    return -1;
}

/*:::DOC_START
 *
 *   Function Name : njg_cust_get_giji
 *
 *   Description : カスタマイズ用擬似候補の処理結果を作成する。
 *
 *   Parameter : 
 *      menv   :(In)  データ解析環境。
 *                    Awnnの内部データを保持している。
 *      time_info :(In) 時間情報
 *      gfpos  :(In)  擬似品詞:(擬似) 前品詞番号
 *                    本関数で作成する擬似候補の品詞番号。
 *      gbpos  :(In)  擬似品詞:(擬似) 後品詞番号
 *                    本関数で作成する擬似候補の次に続く候補のための
 *                    品詞番号。予測変換機能などで参照される。
 *      gnbpos :(In)  擬似品詞:(数字) 後品詞番号
 *                    本関数で作成する擬似候補の次に続く候補のための
 *                    品詞番号。特に、擬似候補が数字を表す場合に使用
 *                    する場合が多い。予測変換機能などで参照される。
 *      yomi   :(In)  擬似候補 読み
 *                    読み文字列の先頭ポインタ。コンパイルオプション
 *                    によりSJIS/EUCの文字コードでセットされる。
 *                    (EUCであっても、上位バイトと下位バイトで
 *                    NJ_UINT8ごとにセットされる。) '\0'終端である。
 *      len    :(In)  読み(yomi)バイト長
 *      giji   :(Out) 擬似候補格納バッファ
 *                    作成した処理結果を格納するバッファ。アプリケー
 *                    ションは、このバッファにデータを格納する必要が
 *                    ある。詳細については、手順書に定義する。
 *      gtype  :(In)  擬似候補タイプ
 *                    アプリケーションが拡張する擬似候補タイプの定義
 *                    値がセットされている。定義値の範囲は、
 *                    NJ_GIJI_TYPE_CUST_MIN ? NJ_GIJI_TYPE_CUST_MAX
 *                    である。
 *
 *   Return value : 
 *      1    : 指定された擬似候補を作成した
 *      0    : 指定された擬似候補が作成できない
 *
 *:::DOC_END
 */
static NJ_INT16 njg_cust_get_giji (PART_OF_NJ_ENV *iwnn, NJ_TIME_ST *time_info, NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                      NJ_UINT16 gnbpos, NJ_UINT8 *yomi, NJ_UINT16 len,
                                      NJ_RESULT *giji, NJ_UINT8 gtype)
{
/* 読み日付/時間変換処理 */
    NJ_UINT16 klen;
    
    if (time_info == NULL) {
        return 0;
    }
    
    /* yomi : 読み文字列 */
    /* len : 読み文字列長 */
    /* giji : 候補情報出力先 */
    /* gtype : 擬似タイプ番号 */
    switch (gtype) {
    case NJ_TYPE_HAN_YOMI2DATE_INIT :
        /* 半角 読み日付/時間変換 初期化処理    */
        njg_cust_get_time(time_info, yomi, (NJ_UINT16)(len*sizeof(NJ_CHAR)));
        return 0;
    case NJ_TYPE_HAN_YOMI2DATE_YYYY :
        /* 半角 読み日付変換 (YYYY年)           */
    case NJ_TYPE_HAN_YOMI2DATE_NENGO :
        /* 半角 読み日付変換 (平成(昭和)YY年)   */
    case NJ_TYPE_HAN_YOMI2DATE_MM :
        /* 半角 読み日付変換 (MM月)             */
    case NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM :
        /* 半角 読み日付変換 (MM/DD)            */
    case NJ_TYPE_HAN_YOMI2DATE_MMDD :
        /* 半角 読み日付変換 (MM月DD日)         */
    case NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM_WEEK :
        /* 半角 読み日付変換 (MM/DD(曜日))      */
    case NJ_TYPE_HAN_YOMI2DATE_MMDD_WEEK :
        /* 半角 読み日付変換 (MM月DD日(曜日))   */
    case NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM :
        /* 半角 読み時間変換 (HH:MM)            */
    case NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM_AMPM :
        /* 半角 読み時間変換 (AM(PM)HH:MM)      */
    case NJ_TYPE_HAN_YOMI2TIME_HHMM :
        /* 半角 読み時間変換 (HH時MM分)         */
    case NJ_TYPE_HAN_YOMI2TIME_HHMM_12H :
        /* 半角 読み時間変換 (午前(後)HH時MM分) */
    case NJ_TYPE_DAY_OF_WEEK :
        /* 読み曜日変換 (X曜日)                 */
    case NJ_TYPE_DAY_OF_WEEK_SYM :
        /* 読み曜日変換 ((曜日))                */
        if (!njg_cust_get_yomi2datetime( time_info, gtype, &klen ) ){
            return 0;
        }
        NJ_SET_KLEN_TO_STEM(&(giji->word), klen);  /* 候補長設定 */
        NJ_SET_BPOS_TO_STEM(&(giji->word), gbpos); /* 一般擬似品詞を設定 */
        break;
    default :
        /* 定義していない擬似候補タイプの場合は、作成できないものとする。。*/
        return 0;
    }

    /*******************/
    /* 定型的な設定を行う */
    /*******************/
    giji->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
    giji->word.yomi = (NJ_CHAR*)yomi;
    NJ_SET_YLEN_TO_STEM(&(giji->word), len);
    NJ_SET_FPOS_TO_STEM(&(giji->word), gfpos);
    NJ_SET_TYPE_TO_STEM(&(giji->word), gtype);
    NJ_SET_FREQ_TO_STEM(&(giji->word), (len * NJC_PHRASE_COST / 2));
    /* 以上の設定は擬似候補の種類にかかわらず同じです */
    return 1;


/*    return 0; */   /* Awnnの標準仕様では、作成できないとする。*/
}

/*:::DOC_START
 *
 *   Function Name : njg_cust_get_giji_candidate
 *
 *   Description : カスタマイズ用擬似候補の候補文字列を作成する。
 *                 njg_cust_get_giji関数で擬似候補格納バッファを作成
 *                 しなければ、本関数は呼び出されない。
 *
 *   Parameter : 
 *      menv      :(In)  データ解析環境。
 *                       Awnnの内部データを保持している。
 *      time_info :(In)  時間情報
 *      word      :(In)  擬似候補格納バッファのメンバー
 *                       njg_cust_get_giji関数で作成した擬似候補格納
 *                       バッファのうち、候補文字列作成に必要なメンバ
 *                       ーの先頭ポインタ。
 *      candidate :(Out) 擬似候補文字列格納バッファ
 *                       作成した擬似候補文字列を格納する。'\0'終端で
 *                       あること。
 *      size      :(In)  擬似候補文字列格納バッファのバイト長
 *                       引数candidateの格納バイト長。'\0'文字のバイ
 *                       ト長を含むこと。
 *   Return value : 
 *      1以上: 作成した擬似候補文字列のバイト長。'\0'は含まない。
 *      0、負数 : エラー発生
 *
 *:::DOC_END
 */
static NJ_INT16 njg_cust_get_giji_candidate (PART_OF_NJ_ENV *iwnn, NJ_TIME_ST *time_info, NJ_WORD *word, 
                                                NJ_UINT8 *candidate, NJ_UINT16 size)
{
    NJ_UINT16   klen;
    NJ_UINT8   gtype;

    /* word : 候補情報 */
    /* candidate : 表記文字列出力先 */
    /* size : 出力先(candidate)サイズ */
    klen = NJ_GET_KLEN_FROM_STEM(word); /* wordに記録された候補長 */
    if (size <= (klen*sizeof(NJ_CHAR))) {
        /* 出力バッファサイズ不足の場合 */
        return 0;
    }
    /* 擬似候補タイプを取得 */
    gtype = NJ_GET_TYPE_FROM_STEM(word);

    switch (gtype) {
    case NJ_TYPE_HAN_YOMI2DATE_YYYY :
        /* 半角 読み日付変換 (YYYY年)           */
    case NJ_TYPE_HAN_YOMI2DATE_NENGO :
        /* 半角 読み日付変換 (平成(昭和)YY年)   */
    case NJ_TYPE_HAN_YOMI2DATE_MM :
        /* 半角 読み日付変換 (MM月)             */
    case NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM :
        /* 半角 読み日付変換 (MM/DD)            */
    case NJ_TYPE_HAN_YOMI2DATE_MMDD :
        /* 半角 読み日付変換 (MM月DD日)         */
    case NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM_WEEK :
        /* 半角 読み日付変換 (MM/DD(曜日))      */
    case NJ_TYPE_HAN_YOMI2DATE_MMDD_WEEK :
        /* 半角 読み日付変換 (MM月DD日(曜日))   */
    case NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM :
        /* 半角 読み時間変換 (HH:MM)            */
    case NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM_AMPM :
        /* 半角 読み時間変換 (AM(PM)HH:MM)      */
    case NJ_TYPE_HAN_YOMI2TIME_HHMM :
        /* 半角 読み時間変換 (HH時MM分)         */
    case NJ_TYPE_HAN_YOMI2TIME_HHMM_12H :
        /* 半角 読み時間変換 (午前(後)HH時MM分) */
    case NJ_TYPE_DAY_OF_WEEK :
        /* 読み曜日変換 (X曜日)                 */
    case NJ_TYPE_DAY_OF_WEEK_SYM :
        /* 読み曜日変換 ((曜日))                */
        klen = njg_cust_get_yomi2datestring( time_info, gtype, candidate, size );
        break;
    default :
        /* 定義していない擬似候補タイプの場合は、作成できないものとする。。*/
        return 0;
    }


    return klen;    /* Awnnの標準仕様では、エラー発生とする。*/


/*    return 0;    *//* Awnnの標準仕様では、エラー発生とする。*/
}


/* 読み日付/時間変換処理 */
/*:::DOC_START
 *
 *   Function Name : njg_cust_get_giji_candidate
 *
 *   Description : 半角 読み日付/時間変換 初期化処理
 *                 読み日付/時間変換に利用する時間を取得する。
 *
 *   Parameter : 
 *      time_st   :(In/Out)  日時構造体
 *      yomi      :(In)      読み文字列
 *      len       :(In)      読み文字列長
 *
 *   Return value : 
 *      戻り値なし
 *
 *:::DOC_END
 */
static void njg_cust_get_time(NJ_TIME_ST *time_st, NJ_UINT8 *yomi, NJ_UINT16 len)
{
    NJ_YOMI2TYPE_TBL    *ptr;
#ifdef WIN32
    SYSTEMTIME st;
#else
	int ret;
    struct tm *tp;
    struct timeval tv;
#endif

    // グローバル領域の変数を初期化
    time_st->year      = 0;
    time_st->month     = 0;
    time_st->day       = 0;
    time_st->dayofweek = 0;
    time_st->hour      = 0;
    time_st->minute    = 0;
    time_st->second    = 0;
    time_st->leap      = 0;
    time_st->adjust    = 0;

    // 現在時刻の取得
#ifdef WIN32
    GetLocalTime( &st );
    time_st->year      = st.wYear;
    time_st->month     = st.wMonth;
    time_st->day       = st.wDay;
    time_st->dayofweek = st.wDayOfWeek;
    time_st->hour      = st.wHour;
    time_st->minute    = st.wMinute;
    time_st->second    = st.wSecond;
#else
    ret = gettimeofday(&tv, NULL);
    if (ret == 0) {
        tp = localtime(&tv.tv_sec);
        if (tp != NULL) {
            time_st->year = tp->tm_year + 1900;
            time_st->month = tp->tm_mon + 1;
            time_st->day = tp->tm_mday;
            time_st->dayofweek = tp->tm_wday;
            time_st->hour = tp->tm_hour;
            time_st->minute = tp->tm_min;
            time_st->second = tp->tm_sec;
        } else {
        	return;
        }
    } else {
    	return;
    }
#endif
    
    /* 閏年判定処理*/
    if ((time_st->year % 4) == 0) {
        if ((time_st->year % 100)==0) {
            if ((time_st->year % 400)==0 ){
                /**
                 4で割り切れる年で、100で割り切れて、
                 400で割り切れる年を閏年とする。
                */
                time_st->leap = 1;
            }
        } else {
            time_st->leap = 1;
        }
    }

    ptr = (NJ_YOMI2TYPE_TBL *)&giji_yomi_2_type_tbl;
    /* 擬似候補作成対象文字列かどうかをチェック */
    while (ptr->gctype != 0xFF) {
        /* 文字列の長さをチェック */
        if (ptr->input_size != len) {
            ptr++;
            continue;
        }

        /* 入力文字列が作成対象の文字列かチェック */
        if (nj_memcmp( yomi, ptr->input, ptr->input_size) != 0) {
            ptr++;
            continue;
        }
        
        /* 補正時間を記憶 */
        time_st->adjust = ptr->adjust;

        /* 取得時間の補正処理 */
        njg_cust_adjust_time_st(time_st, ptr->gctype);
        
        /* 作成有無の記憶 */
        time_st->adjust = ptr->gctype;
        break;
    }

    return;
}

/*:::DOC_START
 *
 *   Function Name : njg_cust_get_yomi2datetime
 *
 *   Description : 読み日付/時間変換 作成判定処理
 *
 *   Parameter : 
 *      *time_st  :(In/Out)  日時構造体
 *      gtype     :(In)      擬似候補タイプ
 *      klen      :(Out)     候補長
 *
 *   Return value : 
 *      戻り値なし
 *
 *:::DOC_END
 */
static NJ_INT16 njg_cust_get_yomi2datetime(  NJ_TIME_ST *time_st, NJ_UINT8 gtype, NJ_UINT16 *klen )
{
    NJ_TYPE2CONVERT_TBL *cptr;

/*  if (time_st->adjust <= NJGC_TYPE_INIT) { */
    if ((time_st->day == 0 ) || (time_st->adjust <= NJGC_TYPE_INIT)) {
        return 0;
    }

    cptr = (NJ_TYPE2CONVERT_TBL *)&giji_conv_tbl;
    
    /* 作成擬似候補の選択 */
    while (cptr->gctype != 0xFF) {
        /* 作成擬似候補タイプかチェック */
        if ((cptr->gtype != gtype) ||
            (cptr->gctype != time_st->adjust)) {
            cptr++;
            continue;
        }

        *klen = njg_cust_get_result_len(time_st, cptr);
        break;
    }

    if( cptr->gctype == 0xFF ) {
        return 0;
    }

    return 1;
}

/*:::DOC_START
 *
 *   Function Name : njg_cust_adjust_time_st
 *
 *   Description : 日付/時間 調整処理
 *
 *   Parameter : 
 *      *time_st  :(In/Out)  日時構造体
 *      gctype    :(In)      擬似候補作成分類定義
 *
 *   Return value : 
 *      戻り値なし
 *
 *:::DOC_END
 */
static void njg_cust_adjust_time_st( NJ_TIME_ST *time_st, NJ_UINT16 gctype )
{
    NJ_INT16    wAdjust = 0;
    NJ_INT16    tmpweek = 0;

    if (time_st->adjust == 0) {
        return;
    }

    switch (gctype) {
    case NJGC_TYPE_YYYY : /* 年表示   */
        time_st->year += time_st->adjust;
        /* 年は調整処理を行わない */
        break;
    case NJGC_TYPE_MM   : /* 月表示   */
        time_st->month += time_st->adjust;

        /* 月の補正を行う。*/
        if (time_st->month <= 0) {
            time_st->month += 12;
        } else if (time_st->month >= 13) {
            time_st->month -= 12;
        } else {
            /* 処理なし */
        }
        break;
        
    case NJGC_TYPE_MMDD : /* 日付表示 */
        time_st->day += time_st->adjust;

        /* 日付の補正を行う。*/
        /* 月補正の有無を判定 */
        if (time_st->day <= 0) {
            time_st->month += (-1);
            wAdjust = 1;
        } else if ((time_st->month == 2) &&
                   (time_st->day > (giji_month_days[time_st->month-1]+time_st->leap))) {
            time_st->day   -= giji_month_days[time_st->month-1]+time_st->leap;
            time_st->month += 1;
        } else if ((time_st->month != 2) &&
                   (time_st->day > giji_month_days[time_st->month-1])) {
            time_st->day   -= giji_month_days[time_st->month-1];
            time_st->month += 1;
        } else {
            /* 処理なし */
        }
        
        /* 月の補正を行う。*/
        if (time_st->month <= 0) {
            time_st->month += 12;
        } else if (time_st->month >= 13) {
            time_st->month -= 12;
        } else {
            /* 処理なし */
        }

        /* 日のマイナス補正を行う。*/
        if (wAdjust) {
            if (time_st->month == 2) {
                /* 閏年を考慮 */
                time_st->day += giji_month_days[time_st->month-1] + time_st->leap;
            } else {
                time_st->day += giji_month_days[time_st->month-1];
            }
        }

        /* 曜日の情報の補正を行う */
        tmpweek = time_st->dayofweek + time_st->adjust;
        if (tmpweek < 0) {
            tmpweek += NJ_DAYOFWEEK_TABLE_MAX;
        } else if (tmpweek >= NJ_DAYOFWEEK_TABLE_MAX) {
            tmpweek -= NJ_DAYOFWEEK_TABLE_MAX;
        }
        time_st->dayofweek = tmpweek;

        break;

    case NJGC_TYPE_HHMM : /* 時間表示 */
        /* 時間は補正なし */
        break;
    default:
        break;
    }

    return;
}

/*:::DOC_START
 *
 *   Function Name : njg_cust_get_result_len
 *
 *   Description : 日付/時間 候補文字列取得
 *
 *   Parameter : 
 *      *time_st  :(In)      日時構造体
 *      gctype    :(In)      擬似候補作成テーブル
 *
 *   Return value : 
 *      ret       :(Out)     候補文字列長
 *
 *:::DOC_END
 */
static NJ_INT16 njg_cust_get_result_len( NJ_TIME_ST *time_st, NJ_TYPE2CONVERT_TBL *cptr)
{
    NJ_UINT16 ret = 0;
    NJ_INT16 nen_year = 0;

    ret += (cptr->f_code_size / sizeof(NJ_CHAR));
    ret += (cptr->s_code_size / sizeof(NJ_CHAR));
    
    switch (cptr->gctype) {
    case NJGC_TYPE_YYYY : /* 年表示   */
        switch (cptr->gtype) {
        case NJ_TYPE_HAN_YOMI2DATE_YYYY :
            /* 半角 読み日付変換 (YYYY年)           */
            ret += njg_cust_calc_len(time_st->year, 1000, 4);
            break;
        case NJ_TYPE_HAN_YOMI2DATE_NENGO :
            /* 半角 読み日付変換 (平成(昭和)YY年)   */

            ret += (4 / sizeof(NJ_CHAR)); /* 昭和/平成分を加算 */

            /* 西暦年を基に年号の年を算出 */
            if (NJG_IS_HEISEI_NENGO(time_st->year, time_st->month, time_st->day)) {
                /* 平成と判断 */
                nen_year = time_st->year - NJ_NENGO_BASE_HEISEI;
            } else {
                /* 昭和と判断 */
                nen_year = time_st->year - NJ_NENGO_BASE_SHOWA;
            }
            if (nen_year == 1) {
                /* 1年の場合、元年として扱う */
                ret += (2 / sizeof(NJ_CHAR)); /* 元分を加算 */
            } else if (nen_year <= 0) {
                /* 擬似候補を作成しない */
                ret = 0;
            } else {
                ret += njg_cust_calc_len(nen_year, 10, 2);
            }
            break;
        }
        break;

    case NJGC_TYPE_MMDD : /* 日付表示 */
        if ((cptr->gtype == NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM_WEEK) ||
            (cptr->gtype == NJ_TYPE_HAN_YOMI2DATE_MMDD_WEEK)) {
            ret += (GIJI_STRING_DAYOFWEEK_LEN / sizeof(NJ_CHAR)); /* (曜日)を加算 */
        }
        ret += njg_cust_calc_len(time_st->day, 10, 2);
    case NJGC_TYPE_MM   : /* 月表示   */
        ret += njg_cust_calc_len(time_st->month, 10, 2);
        break;

    case NJGC_TYPE_HHMM : /* 時間表示 */
        switch (cptr->gtype) {
        case NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM :
            /* 半角 読み時間変換 (HH:MM)            */
            if ( time_st->hour == 0 ){
                ret += 1;
            } else {
                ret += njg_cust_calc_len(time_st->hour, 10, 2);
            }
            ret += 2; /* 分は強制的に二桁 */
            break;
            
        case NJ_TYPE_HAN_YOMI2TIME_HHMM :
            /* 半角 読み時間変換 (HH時MM分)         */
            if ( time_st->hour == 0 ){
                ret += 1;
            } else {
                ret += njg_cust_calc_len(time_st->hour, 10, 2);
            }
            ret += 2; /* 分は強制的に二桁 */
/*
            if ( time_st->minute == 0 ){
                ret += 1;
            } else {
                ret += njg_cust_calc_len(time_st->minute, 10, 2);
            }
*/

            break;
        case NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM_AMPM :
            /* 半角 読み時間変換 (AM(PM)HH:MM)      */
        case NJ_TYPE_HAN_YOMI2TIME_HHMM_12H :
            /* 半角 読み時間変換 (午前(後)HH時MM分) */
            /**
             *  午前／午後表示
             *  24H表記     12H表記
             *    0:00  →  午前0:00
             *   12:00  →  午後12:00
             *  AM／PM表示
             *  24H表記     12H表記
             *    0:00  →  AM0:00
             *   12:00  →  PM12:00
             */
            /* AM/PMの場合は2byteとなる為、SJISで処理させる場合は4ではなく、2に改善が必要 */
            ret += (4 / sizeof(NJ_CHAR)); /* 午前/午後 or AM/PM分を加算 */ /* OMRON M.UEDA modify 100924 */

            if ( time_st->hour == 0 ){
                ret += 1;
            }
            
            if ( time_st->hour == 12 ){ /* 12時は午後0時となるため１桁となる */
                ret += 1;
            } else if ( time_st->hour > 12 ){
                ret += njg_cust_calc_len((NJ_INT16)(time_st->hour-12), 10, 2);
            } else {
                ret += njg_cust_calc_len(time_st->hour, 10, 2);
            }

            ret += 2; /* 分は強制的に二桁 */
/*
            if ( time_st->minute == 0 ){
                ret += 1;
            } else {
                ret += njg_cust_calc_len(time_st->minute, 10, 2);
            }
*/
            break;
        }
        break;

    case NJGC_TYPE_WEEK : /* 曜日表示 */
        switch (cptr->gtype) {
        case NJ_TYPE_DAY_OF_WEEK :
            /* 読み曜日変換 (X曜日)                 */
            ret += (GIJI_STRING_DAYOFWEEK_KANJI_LEN / sizeof(NJ_CHAR));  /* X曜日を加算 */
            break;
        case NJ_TYPE_DAY_OF_WEEK_SYM :
            /* 読み曜日変換 ((曜日))                */
            ret += (GIJI_STRING_DAYOFWEEK_LEN / sizeof(NJ_CHAR));  /* (曜日)を加算 */
            break;
        }
        break;
    default:
        break;
    }
    return ret;
}

static NJ_UINT16 njg_cust_calc_len( NJ_INT16 data, NJ_UINT16 base, NJ_UINT16 keta)
{
    while( keta > 0 ) {
        if ((data/base) != 0){
            break;
        }
        data = data % base;
        base    = base / 10;
        keta--;
    }

    return keta;
}

/*:::DOC_START
 *
 *   Function Name : njg_cust_get_yomi2datestring
 *
 *   Description : 読み日付/時間変換 作成処理
 *
 *   Parameter : 
 *      *time_st  :(In/Out)  日時構造体
 *      gtype     :(In)      擬似候補タイプ
 *      candidate :(Out)     候補バッファ
 *      size      :(In)      候補バッファサイズ
 *
 *   Return value : 
 *      作成擬似候補のサイズ
 *
 *:::DOC_END
 */
static NJ_UINT16 njg_cust_get_yomi2datestring( NJ_TIME_ST *time_st, NJ_UINT8 gtype, NJ_UINT8 *candidate, NJ_UINT16 size )
{
    NJ_UINT16 ret = 0;
    NJ_TYPE2CONVERT_TBL *cptr;

    if (time_st->adjust < 0) {
        return 0;
    }

    cptr = (NJ_TYPE2CONVERT_TBL *)&giji_conv_tbl;
    
    /* 作成擬似候補の選択 */
    while (cptr->gctype != 0xFF) {
        /* 作成擬似候補タイプかチェック */
        if ((cptr->gtype != gtype) ||
            (cptr->gctype != time_st->adjust)) {
            cptr++;
            continue;
        }

        ret = njg_cust_get_result_string(time_st, cptr, candidate, size);
        break;
    }

    if( cptr->gctype == 0xFF ) {
        return 0;
    }

    return ret;
    

}

/*:::DOC_START
 *
 *   Function Name : njg_cust_get_result_len
 *
 *   Description : 日付/時間 候補文字列取得
 *
 *   Parameter : 
 *      *time_st  :(In)      日時構造体
 *      gctype    :(In)      擬似候補作成テーブル
 *
 *   Return value : 
 *      ret       :(Out)     候補文字列長
 *
 *:::DOC_END
 */
static NJ_INT16 njg_cust_get_result_string(NJ_TIME_ST *time_st, NJ_TYPE2CONVERT_TBL *cptr, NJ_UINT8 *candidate, NJ_UINT16 size)
{
    NJ_INT16 ret = 0;
    NJ_INT16 len = 0;
    NJ_INT16 nen_year = 0;
    NJ_UINT8 *p = candidate;
    NJ_UINT16 i;
    
    
    switch (cptr->gctype) {
    case NJGC_TYPE_YYYY : /* 年表示   */
        switch (cptr->gtype) {
        case NJ_TYPE_HAN_YOMI2DATE_YYYY :
            /* 半角 読み日付変換 (YYYY年)           */
            len = njg_cust_calc_string(time_st->year, 1000, 4, p, 0);
            p   += len;
            ret += len;
            if (cptr->f_code_size == 2) {
                *p = (NJ_UINT8)((cptr->f_code >> 8) & 0xff);
                p++;
            }
#ifdef NJ_OPT_UTF16
            if (cptr->f_code_size == 1) {
                *p++ = 0x00;
                ret++;
            }
#endif
            *p = (NJ_UINT8)(cptr->f_code & 0xff);
            p++;
            ret += cptr->f_code_size;
            break;

        case NJ_TYPE_HAN_YOMI2DATE_NENGO :
            /* 半角 読み日付変換 (平成(昭和)YY年)   */
            /* 西暦年を基に年号の年を算出 */
            if (NJG_IS_HEISEI_NENGO(time_st->year, time_st->month, time_st->day)) {
                /* 平成と判断 */
                for (i = 0; i < giji_string_heisei_len; i++) {
                    *p = giji_string_heisei[i];
                    p++;
                }
                ret += giji_string_heisei_len;
                nen_year = time_st->year - NJ_NENGO_BASE_HEISEI;
            } else {
                /* 昭和と判断 */
                for (i = 0; i < giji_string_showa_len; i++) {
                    *p = giji_string_showa[i];
                    p++;
                }
                ret += giji_string_showa_len;
                nen_year = time_st->year - NJ_NENGO_BASE_SHOWA;
            }
            if (nen_year == 1) {
                /* 1年の場合、元年として扱う */
                for (i = 0; i < giji_string_gannen_len; i++) {
                    *p = giji_string_gannen[i];
                    p++;
                }
                ret += giji_string_gannen_len;
            } else if (nen_year <= 0) {
                /* 擬似候補を作成しない */
                ret = 0;
            } else {
                len = njg_cust_calc_string(nen_year, 10, 2, p, 0);
                p   += len;
                ret += len;
                if (cptr->f_code_size == 2) {
                    *p = (NJ_UINT8)((cptr->f_code >> 8) & 0xff);
                    p++;
                }
#ifdef NJ_OPT_UTF16
                if (cptr->f_code_size == 1) {
                    *p++ = 0x00;
                    ret++;
                }
#endif
                *p = (NJ_UINT8)(cptr->f_code & 0xff);
                p++;
                ret += cptr->f_code_size;
            }
            break;
        }
        break;

    case NJGC_TYPE_MM   : /* 月表示   */
        len = njg_cust_calc_string(time_st->month, 10, 2, p, 0);
        p   += len;
        ret += len;
        if (cptr->f_code_size == 2) {
            *p = (NJ_UINT8)((cptr->f_code >> 8) & 0xff);
            p++;
        }
#ifdef NJ_OPT_UTF16
        if (cptr->f_code_size == 1) {
            *p++ = 0x00;
            ret++;
        }
#endif
        *p = (NJ_UINT8)(cptr->f_code & 0xff);
        p++;
        ret += cptr->f_code_size;
        break;

    case NJGC_TYPE_MMDD : /* 日付表示 */
        len = njg_cust_calc_string(time_st->month, 10, 2, p, 0);
        p   += len;
        ret += len;
        if (cptr->f_code_size != 0) {
            if (cptr->f_code_size == 2) {
                *p = (NJ_UINT8)((cptr->f_code >> 8) & 0xff);
                p++;
            }
#ifdef NJ_OPT_UTF16
            if (cptr->f_code_size == 1) {
                *p++ = 0x00;
                ret++;
            }
#endif
            *p = (NJ_UINT8)(cptr->f_code & 0xff);
            p++;
            ret += cptr->f_code_size;
        }
        
        len = njg_cust_calc_string(time_st->day, 10, 2, p, 0);
        p   += len;
        ret += len;
        if (cptr->s_code_size != 0) {
            if (cptr->s_code_size == 2) {
                *p = (NJ_UINT8)((cptr->s_code >> 8) & 0xff);
                p++;
            }
#ifdef NJ_OPT_UTF16
            if (cptr->s_code_size == 1) {
                *p++ = 0x00;
                ret++;
            }
#endif
            *p = (NJ_UINT8)(cptr->s_code & 0xff);
            p++;
            ret += cptr->s_code_size;
        }
        if ((cptr->gtype == NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM_WEEK) ||
            (cptr->gtype == NJ_TYPE_HAN_YOMI2DATE_MMDD_WEEK)) {
        /* 半角 読み日付変換 (MM/DD(曜日) or MM月DD日(曜日))   */
            for(i = 0; i < GIJI_STRING_DAYOFWEEK_LEN; i++){
                *p = giji_string_dayofweek_tbl[time_st->dayofweek][i];
                p++;
            }
            ret += GIJI_STRING_DAYOFWEEK_LEN;
        }
        break;

    case NJGC_TYPE_HHMM : /* 時間表示 */
        switch (cptr->gtype) {
        case NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM :
            /* 半角 読み時間変換 (HH:MM)            */
        case NJ_TYPE_HAN_YOMI2TIME_HHMM :
            /* 半角 読み時間変換 (HH時MM分)         */
            len = njg_cust_calc_string(time_st->hour, 10, 2, p, 0);
            p   += len;
            ret += len;
            if (cptr->f_code_size != 0) {
                if (cptr->f_code_size == 2) {
                    *p = (NJ_UINT8)((cptr->f_code >> 8) & 0xff);
                    p++;
                }
#ifdef NJ_OPT_UTF16
                if (cptr->f_code_size == 1) {
                    *p++ = 0x00;
                    ret++;
                }
#endif
                *p = (NJ_UINT8)(cptr->f_code & 0xff);
                p++;
                ret += cptr->f_code_size;
            }
            
            len = njg_cust_calc_string(time_st->minute, 10, 2, p, 1);
            p   += len;
            ret += len;
            if (cptr->s_code_size != 0) {
                if (cptr->s_code_size == 2) {
                    *p = (NJ_UINT8)((cptr->s_code >> 8) & 0xff);
                    p++;
                }
#ifdef NJ_OPT_UTF16
                if (cptr->s_code_size == 1) {
                    *p++ = 0x00;
                    ret++;
                }
#endif
                *p = (NJ_UINT8)(cptr->s_code & 0xff);
                p++;
                ret += cptr->s_code_size;
            }
            break;

        case NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM_AMPM :
            /* 半角 読み時間変換 (AM(PM)HH:MM)      */
        case NJ_TYPE_HAN_YOMI2TIME_HHMM_12H :
            /* 半角 読み時間変換 (午前(後)HH時MM分) */
            /**
             *  午前／午後表示
             *  24H表記     12H表記
             *    0:00  →  午前0:00
             *   12:00  →  午後0:00
             */
             if (cptr->gtype == NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM_AMPM) {
                if (time_st->hour >= 12) {
                    for(i = 0; i < giji_string_pm_en_len; i++){
                        *p = giji_string_pm_en[i];
                        p++;
                    }
                    ret += giji_string_pm_en_len;
                } else {
                    for(i = 0; i < giji_string_am_en_len; i++){
                        *p = giji_string_am_en[i];
                        p++;
                    }
                    ret += giji_string_am_en_len;
                }
             } else {
                if (time_st->hour >= 12) {
                    for(i = 0; i < giji_string_pm_len; i++){
                        *p = giji_string_pm[i];
                        p++;
                    }
                    ret += giji_string_pm_len;
                } else {
                    for(i = 0; i < giji_string_am_len; i++){
                        *p = giji_string_am[i];
                        p++;
                    }
                    ret += giji_string_am_len;
                }
            }

            if ( time_st->hour >= 12 ){
                len = njg_cust_calc_string((NJ_INT16)(time_st->hour-12), 10, 2, p, 0);
            } else {
                len = njg_cust_calc_string(time_st->hour, 10, 2, p, 0);
            }
            p   += len;
            ret += len;
            if (cptr->f_code_size != 0) {
                if (cptr->f_code_size == 2) {
                    *p = (NJ_UINT8)((cptr->f_code >> 8) & 0xff);
                    p++;
                }
#ifdef NJ_OPT_UTF16
                if (cptr->f_code_size == 1) {
                    *p++ = 0x00;
                    ret++;
                }
#endif
                *p = (NJ_UINT8)(cptr->f_code & 0xff);
                p++;
                ret += cptr->f_code_size;
            }


            len = njg_cust_calc_string(time_st->minute, 10, 2, p, 1);
            p   += len;
            ret += len;
            if (cptr->s_code_size != 0) {
                if (cptr->s_code_size == 2) {
                    *p = (NJ_UINT8)((cptr->s_code >> 8) & 0xff);
                    p++;
                }
#ifdef NJ_OPT_UTF16
                if (cptr->s_code_size == 1) {
                    *p++ = 0x00;
                    ret++;
                }
#endif
                *p = (NJ_UINT8)(cptr->s_code & 0xff);
                p++;
                ret += cptr->s_code_size;
            }
            break;
        }
        break;

    case NJGC_TYPE_WEEK : /* 曜日表示 */
        switch (cptr->gtype) {
        case NJ_TYPE_DAY_OF_WEEK :
            /* 読み曜日変換 (X曜日)                 */
            for(i = 0; i < GIJI_STRING_DAYOFWEEK_KANJI_LEN; i++){
                *p = giji_string_dayofweek_kanji_tbl[time_st->dayofweek][i];
                p++;
            }
            ret += GIJI_STRING_DAYOFWEEK_KANJI_LEN;
            break;
        case NJ_TYPE_DAY_OF_WEEK_SYM :
            /* 読み曜日変換 ((曜日))                */
            for(i = 0; i < GIJI_STRING_DAYOFWEEK_LEN; i++){
                *p = giji_string_dayofweek_tbl[time_st->dayofweek][i];
                p++;
            }
            ret += GIJI_STRING_DAYOFWEEK_LEN;
            break;
        }
        break;

    default:
        break;
    }
#ifdef NJ_OPT_UTF16
    *p++ = 0x00;
#endif
    *p = 0x00;

/* OMRON M.UEDA ADD-START */
    if (ret > 0) {
        ret = (NJ_INT16)(ret / sizeof(NJ_CHAR));
    }
/* OMRON M.UEDA ADD-END */
    return ret;

}

static NJ_UINT16 njg_cust_calc_string( NJ_INT16 data, NJ_UINT16 base, NJ_UINT16 keta, NJ_UINT8 *out, NJ_UINT8 flg)
{
    NJ_UINT8 str;
    NJ_INT16 len = 0;
    NJ_UINT16 i;
    
    for( i = 0; i < keta; i++ ) {
        str = (NJ_UINT8)(data/base);
        if ((str == 0)&&(flg == 0)&&(i != (keta-1))){
            data = data % base;
            base = base / 10;
            continue;
        }
        flg = 1;
#ifdef NJ_OPT_UTF16
        *out++ = 0x00;
        len++;
#endif
        *out = (NJ_UINT8)(0x30 + str);
        out++;
        
        data = data % base;
        base = base / 10;
        len++;
    }

    return len;
}
