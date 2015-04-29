/****************************************************************
 * 本環境設定ファイルは、お客様のサポートを行う上で重要なファイルとなり
 * ます。
 * お問い合わせ、もしくは、本ファイルを修正された場合は、以下の情報を添
 * えて、オムロンソフトウェアまで、本ファイルをお送りください。
 *   ・iWnnバージョン名(README.txtの先頭に記載)
 *   ・コンパイルオプション
 ****************************************************************/
/**
 * @file
 *   変換エンジン定義（共通編）
 *
 *   アプリケーションで使用する為に必要な定義を行う。
 *
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2013 All Rights Reserved.
 */

#define NJ_VERSION "iWnn Version 1.6.4"

#ifndef _NJ_LIB_H_
#define _NJ_LIB_H_

/************************************************/
/*         コンパイルオプション定義             */
/************************************************/
/*
 * 本エンジンにて指定できるコンパイルオプションは、以下の通りとなります。
 *   コンパイルオプション名      説明
 *     NJ_ADD_STATE_TYPE2    -  状況設定バイアス値自動更新オプション
 *     NJ_OPT_UTF16          -  Unicode(UTF-16BE)使用オプション
 *     NJ_OPT_CHARSET_2BYTE  -  曖昧予測検索高速化オプション
 *     NJ_OPT_EN_STATE       -  多言語(英語)状況設定利用オプション
 *     NJ_OPT_ZHCN_STATE     -  多言語(中国語<簡体字>)状況設定利用オプション
 *     NJ_OPT_FORECAST_COMPOUND_VER143 - Ver.1.4.3までの複合語予測機能利用オプション
 *     NJ_OPT_DIC_STORAGE    -  ストレージ辞書機能利用オプション
 */
/*
 * コンパイルオプションは、ビルド時のMakefile等に指定いただくか、
 * 以下のコメントのように、本ファイルに記述してください。
 */
/*
 * SJIS利用時 推奨コンパイルオプション
 * #define NJ_ADD_STATE_TYPE2   1
 * #define NJ_OPT_CHARSET_2BYTE 1
 */
/*
 * UTF-16BE利用時 推奨コンパイルオプション
 * #define NJ_ADD_STATE_TYPE2   1
 * #define NJ_OPT_CHARSET_2BYTE 1
 * #define NJ_OPT_UTF16         1
 */

/************************************************/
/*              define  宣  言                  */
/************************************************/
/*
 * 数字型
 */
/** 8bit 整数 */
typedef signed char    NJ_INT8;
/** 8bit 符号なし整数 */
typedef unsigned char  NJ_UINT8;
/** 16bit 整数 */
typedef signed short   NJ_INT16;
/** 16bit 符号なし整数 */
typedef unsigned short NJ_UINT16;
/** 32bit 整数 */
typedef signed long    NJ_INT32;
/** 32bit 符号なし整数 */
typedef unsigned long  NJ_UINT32;

/**
 * VOID型
 */
typedef void NJ_VOID;

/**
 * FILE型
 */
typedef void NJ_FILE;

/**
 * 文字型
 */
#ifdef NJ_OPT_UTF16
typedef unsigned short   NJ_CHAR;
#else /* NJ_OPT_UTF16 */
typedef unsigned char    NJ_CHAR;
#endif /* NJ_OPT_UTF16 */

/**
 * NUL文字定義
 */
#ifdef NJ_OPT_UTF16
#define NJ_CHAR_NUL  0x0000
#else /* NJ_OPT_UTF16 */
#define NJ_CHAR_NUL  ('\0')
#endif /* NJ_OPT_UTF16 */
#define NJ_BYTE_NUL ('\0')

/**
 * 文字列のNUL文字の配列要素数（変更不可）
 */
#define NJ_TERM_LEN  1
#define NJ_TERM_SIZE (NJ_TERM_LEN)

/**
 * NULL定義
 */
#ifndef NULL
#define NULL 0
#endif


#ifdef NJ_OPT_UTF16
/*
 * 「〜」の割り当て文字コードの定義 (UTF16利用時のみ)
 */
/* [UTF16] 全角[〜]の文字コード定義 */
#ifndef NJ_CHAR_WAVE_DASH_BIG
/*#define NJ_CHAR_WAVE_DASH_BIG   0x301C*/  /**< 文字コード(WAVE DASH)*/
#define NJ_CHAR_WAVE_DASH_BIG   0xFF5E /**< 文字コード(FULLWIDTH TILDE) [CP932] */
#endif /* NJ_CHAR_WAVE_DASH_BIG */
/* [UTF16] 半角[~]の文字コード定義 */
#ifndef NJ_CHAR_WAVE_DASH_SMALL
/*#define NJ_CHAR_WAVE_DASH_SMALL 0x203E*/  /**< 文字コード(OVERLINE)*/
#define NJ_CHAR_WAVE_DASH_SMALL 0x007E /**< 文字コード(TILDE) [CP932] */
#endif /* NJ_CHAR_WAVE_DASH_SMALL */
#endif /* NJ_OPT_UTF16 */

/*
 * 数値共通定義（変更不可）
 */
#define NJ_NUM_ONE              1         /**< 数値定義：1 */
#define NJ_NUM_THOUSAND         1000      /**< 数値定義：1000 */

#define NJ_NUM_BIT4             4         /**< 数値定義： 4(bit) */
#define NJ_NUM_BIT16            16        /**< 数値定義：16(bit) */

#define NJ_NUM_MIN_INT16        -32768    /**< 数値定義： -32768(2byte整数型の最小値) */

#define NJ_NUM_MIN_GIJI_FREQ    -30000    /**< 数値定義： 擬似候補の最小頻度値 */

#define NJ_NUM_SEGMENT1         1         /**< 数値定義：1文節 */
#define NJ_NUM_SEGMENT2         2         /**< 数値定義：2文節 */

#define NJ_FLAG_OFF             0         /**< 数値定義：フラグOFF */
#define NJ_FLAG_ON              1         /**< 数値定義：フラグON  */

/*
 * ストレージ辞書動作モード定義
 */
#define NJ_STORAGE_MODE_DEFAULT                (NJ_STORAGE_MODE_ONMEMORY_MORPHO_DATA)       /**< 動作モード：標準動作モード */
#define NJ_STORAGE_MODE_ONMEMORY               (NJ_STORAGE_MODE_ONMEMORY_MORPHO_DATA | \
                                                NJ_STORAGE_MODE_ONMEMORY_WORD_DATA |   \
                                                NJ_STORAGE_MODE_ONMEMORY_STRING_DATA)       /**< 動作モード：全メモリ */
#define NJ_STORAGE_MODE_ONMEMORY_MORPHO_DATA    0x00000001       /**< 動作モード：形態素データ オンメモリ */
#define NJ_STORAGE_MODE_ONMEMORY_WORD_DATA      0x00000002       /**< 動作モード：単語データ オンメモリ */
#define NJ_STORAGE_MODE_ONMEMORY_STRING_DATA    0x00000004       /**< 動作モード：文字列データ オンメモリ */


/*
 * 辞書用共通定義（変更不可）
 */
/** [辞書] インデックスサイズ */
#define NJ_INDEX_SIZE      2

/*
 * 辞書用共通定義
 */
/** [辞書] 学習領域 辞書ヘッダサイズ */
#define NJ_LEARN_DIC_HEADER_SIZE   72

/*
 * エンジン内限定 データ型
 */
typedef NJ_INT16 NJ_HINDO;      /**< 頻度データ型 */

/**
 * [全機能] 語彙辞書最大マウント数
 *
 * - 最大値：１００<br>
 *      例) 自立語辞書+付属語辞書+単漢字辞書+学習辞書+ユーザ辞書+
 *          標準予測辞書+読み無し予測辞書+カスタマイズ辞書(*13)
 * - 最小値：４<br>
 *      例) 自立語辞書+付属語辞書+単漢字辞書+読み無し予測辞書
 * - 初期値：２０
 */
#ifndef NJ_MAX_DIC
#define NJ_MAX_DIC 20
#endif /* NJ_MAX_DIC */

/**
 * [予測機能] あいまい文字最大登録数
 *
 * - 最大値：２５５
 * - 最小値：１
 * - 初期値：５０
 */
#ifndef NJ_MAX_CHARSET
#define NJ_MAX_CHARSET 50
#endif /* NJ_MAX_CHARSET */

/**
 * [予測機能] 最大キャッシュ領域長
 *
 * - 最大値：１０００
 * - 最小値：１０
 * - 初期値：５０
 */
#ifndef NJ_SEARCH_CACHE_SIZE
#define NJ_SEARCH_CACHE_SIZE   50
#endif /* NJ_SEARCH_CACHE_SIZE */

/**
 * [予測機能] あいまい候補表示切替件数
 *
 * - 最大値：５
 * - 最小値：１
 * - 初期値：２
 */
#ifndef NJ_CACHE_VIEW_CNT
#define NJ_CACHE_VIEW_CNT       2
#endif /* NJ_CACHE_VIEW_CNT */


/**
 * [変換・予測機能] 最大出力文字配列要素数
 *
 * - 最大値：１１０ (SJIS使用時)
 * - 最大値：５５   (UTF16使用時)
 * - 最小値：４０
 * - 初期値：４０
 */
#ifndef NJ_MAX_RESULT_LEN
#define NJ_MAX_RESULT_LEN  40
#endif /* NJ_MAX_RESULT_LEN */

/**
 * [変換・予測機能] 最大読み文字配列最大要素数
 *
 * - 最大値：NJ_MAX_RESULT_LEN
 * - 最小値：２０
 * - 初期値：４０
 *
 * @attention
 *   ヌル文字を含まない長さ
 */
#ifndef NJ_MAX_LEN
#define NJ_MAX_LEN          40
#endif /* NJ_MAX_LEN */

/**
 * [変換・予測機能] 最大付加情報文字配列最大要素数
 *
 * - 最大値：１１０
 * - 最小値：２０
 * - 初期値：４０
 *
 * @attention
 *   ヌル文字を含まない長さ
 */
#ifndef NJ_MAX_ADDITIONAL_LEN
#define NJ_MAX_ADDITIONAL_LEN          40
#endif /* NJ_MAX_ADDITIONAL_LEN */

/**
 * [変換・予測機能] 最大付加情報最大要素数
 *
 * - 最大値：５
 * - 最小値：１
 * - 初期値：２
 *
 * @attention
 *   ヌル文字を含まない長さ
 */
#ifndef NJ_MAX_ADDITIONAL_INFO
#define NJ_MAX_ADDITIONAL_INFO         2
#endif /* NJ_MAX_ADDITIONAL_INFO */


/**
 * [予測機能] 最大キャッシュ文字配列要素数
 *
 * - 固定値：(NJ_MAX_RESULT_LEN + NJ_TERM_LEN)
 */
#ifndef NJ_MAX_KEYWORD
#define NJ_MAX_KEYWORD (NJ_MAX_RESULT_LEN + NJ_TERM_LEN)
#endif /* NJ_MAX_KEYWORD */

/**
 * [変換機能] 最大変換文節数
 *
 * - 固定値：NJ_MAX_LEN
 */
#ifndef NJ_MAX_PHRASE
#define NJ_MAX_PHRASE       NJ_MAX_LEN
#endif /* NJ_MAX_PHRASE */

/**
 * [変換機能] 最大接続解析文節数
 *
 * - 固定値：5
 */
#ifndef NJ_MAX_PHR_CONNECT
#define NJ_MAX_PHR_CONNECT      5
#endif /* NJ_MAX_PHR_CONNECT */


/**
 * [予測機能] 最大複数文節繋がり予測文節数
 *
 * - 固定値：5
 */
#ifndef NJ_MAX_RELATION_SEGMENT
#define NJ_MAX_RELATION_SEGMENT      5
#endif /* NJ_MAX_RELATION_SEGMENT */

/**
 * [全候補] 付属語解析 最大文字配列要素数
 *
 * - 最大値：NJ_MAX_LEN/2
 * - 最小値：０
 * - 初期値：NJ_MAX_LEN/2
 */
#ifndef NJ_MAX_FZK_LEN
#define NJ_MAX_FZK_LEN  (NJ_MAX_LEN / 2)
#endif /* NJ_MAX_FZK_LEN */

/**
 * [全候補] 付属語解析パターン数
 *
 * @attention
 *   16bit版作成時は、40固定。
 */
#ifndef NJ_MAX_FZK_CANDIDATE
#define NJ_MAX_FZK_CANDIDATE    (NJ_MAX_LEN * sizeof(NJ_CHAR))
#endif /* NJ_MAX_FZK_CANDIDATE */

/**
 * [全候補] 自立語解析パターン数
 *
 * @attention
 *   付属語解析パターン数(NJ_MAX_FZK_CANDIDATE)と同じ値を設定してください。
 */
#ifndef NJ_MAX_STEM_CANDIDATE
#define NJ_MAX_STEM_CANDIDATE    NJ_MAX_FZK_CANDIDATE
#endif /* NJ_MAX_STEM_CANDIDATE */

/**
 * [予測] 繋がり予測結果最大保持件数
 *
 */
#ifndef NJ_MAX_RELATION_RESULTS
#define NJ_MAX_RELATION_RESULTS 50
#endif /* NJ_MAX_RELATION_RESULTS */

/**
 * [予測] 複合語予測結果最大保持件数(参照バッファ)
 *
 */
#ifndef NJ_MAX_REFER_CMPDG_RESULTS
#define NJ_MAX_REFER_CMPDG_RESULTS      20
#endif /* NJ_MAX_REFER_CMPDG_RESULTS */

/**
 * [予測] 複合語予測結果最大保持件数(次回バッファ)
 *
 */
#ifndef NJ_MAX_NEXT_CMPDG_RESULTS
#define NJ_MAX_NEXT_CMPDG_RESULTS      20
#endif /* NJ_MAX_NEXT_CMPDG_RESULTS */

/**
 * [予測] 複合語予測結果最大保持件数(合計バッファ)
 *
 */
#ifndef NJ_MAX_CMPDG_RESULTS
#define NJ_MAX_CMPDG_RESULTS      (NJ_MAX_REFER_CMPDG_RESULTS + NJ_MAX_NEXT_CMPDG_RESULTS)
#endif /* NJ_MAX_CMPDG_RESULTS */

/**
 * [変換] 連文節解析時に同読み文字列に対して得る、辞書引き結果最大数
 *
 * - 最大値：３２
 * - 最小値：５
 * - 初期値：３２
 */
#ifndef NJ_MAX_GET_RESULTS
#define NJ_MAX_GET_RESULTS 32
#endif /* NJ_MAX_GET_RESULTS */

/**
 * [単語登録] 読み文字配列最大要素数
 *
 * - 最大値：NJ_MAX_LEN
 * - 最小値：２０
 * - 初期値：４０
 *
 * @attention
 *   ヌル文字を含まない長さ
 */
#ifndef NJ_MAX_USER_LEN
#define NJ_MAX_USER_LEN         40
#endif /* NJ_MAX_USER_LEN */

/**
 * [単語登録] 候補文字配列最大要素数
 *
 * - 最大値：NJ_MAX_RESULT_LEN
 * - 最小値：２０
 * - 初期値：４０
 *
 * @attention
 *   ヌル文字を含まない長さ
 */
#ifndef NJ_MAX_USER_KOUHO_LEN
#define NJ_MAX_USER_KOUHO_LEN   40
#endif /* NJ_MAX_USER_KOUHO_LEN */

/**
 * [単語登録] 付加情報文字配列最大要素数
 *
 * - 最大値：NJ_MAX_ADDITIONAL_LEN
 * - 最小値：２０
 * - 初期値：４０
 *
 * @attention
 *   ヌル文字を含まない長さ
 */
#ifndef NJ_MAX_USER_ADDITIONAL_LEN
#define NJ_MAX_USER_ADDITIONAL_LEN   40
#endif /* NJ_MAX_USER_ADDITIONAL_LEN */

/**
 * [単語登録] 最大単語登録数
 *
 * - 最大値：２５５
 * - 最小値：１０
 * - 初期値：１００
 */
#ifndef NJ_MAX_USER_COUNT
#define NJ_MAX_USER_COUNT       100
#endif /* NJ_MAX_USER_COUNT */

/**
 * [単語登録] ユーザ辞書サイズ（変更不可）
 *
 * NJ_USER_DIC_SIZE
 */
#define NJ_USER_QUE_SIZE        (((NJ_MAX_USER_LEN + NJ_MAX_USER_KOUHO_LEN) * sizeof(NJ_CHAR)) + 5)
#define NJ_USER_DIC_SIZE        ((NJ_USER_QUE_SIZE + NJ_INDEX_SIZE + NJ_INDEX_SIZE) * NJ_MAX_USER_COUNT + NJ_INDEX_SIZE  + NJ_INDEX_SIZE + NJ_LEARN_DIC_HEADER_SIZE + 4)

/**
 * [単語登録] ユーザ辞書(付加情報付)サイズ（変更不可）
 *
 * NJ_USER2_DIC_SIZE
 */
#define NJ_USER2_QUE_SIZE        (((NJ_MAX_USER_LEN + NJ_MAX_USER_KOUHO_LEN + NJ_MAX_ADDITIONAL_LEN) * sizeof(NJ_CHAR)) + 7)
#define NJ_USER2_DIC_SIZE        ((NJ_USER2_QUE_SIZE + NJ_INDEX_SIZE + NJ_INDEX_SIZE) * NJ_MAX_USER_COUNT + NJ_INDEX_SIZE  + NJ_INDEX_SIZE + NJ_LEARN_DIC_HEADER_SIZE + 4)

/**
 * [変換機能] 最大取得全候補数
 *
 * - 最大値：５００
 * - 最小値：１００
 * - 初期値：３００
 */
#ifndef NJ_MAX_CANDIDATE
#define NJ_MAX_CANDIDATE        300
#endif /* NJ_MAX_CANDIDATE */

/**
 * [形態素解析] 最大形態素解析文字配列要素数
 *
 * - 最大値：１００  (SJIS使用時)
 * - 最大値：５０    (UTF16使用時)
 * - 最小値：４０
 * - 初期値：１００  (SJIS使用時)
 * - 初期値：５０    (UTF16使用時)
 */
#ifndef MM_MAX_MORPHO_LEN
#define MM_MAX_MORPHO_LEN  (100 / sizeof(NJ_CHAR))
#endif /* MM_MAX_MORPHO_LEN */

/**
 * [頻度学習入出力機能] 頻度学習データ１ブロックインポート最大数
 *
 * - 固定値：１００(１ブロックのデータ数)
 */
#ifndef NJ_MAX_IMP_EXT_HINDO_CNT
#define NJ_MAX_IMP_EXT_HINDO_CNT    100
#endif /* !NJ_MAX_IMP_EXT_HINDO_CNT */

/**
 * [付属語解析機能] 最大付属語数
 *
 */
#ifndef NJ_FZK_BUF_MAX_WORD_NUM
#define NJ_FZK_BUF_MAX_WORD_NUM    100
#endif /* !NJ_FZK_BUF_MAX_WORD_NUM */

/**
 * [付属語解析機能] 最大付属語パターン数
 *
 */
#ifndef NJ_FZK_BUF_MAX_PATTERN
#define NJ_FZK_BUF_MAX_PATTERN     300
#endif /* !NJ_FZK_BUF_MAX_PATTERN */


/************************************************/
/*              structure  宣  言               */
/************************************************/
/**
 * 辞書ハンドル定義
 */
typedef NJ_UINT8 * NJ_DIC_HANDLE;

/**
 * 辞書頻度情報定義
 */
typedef struct {
    NJ_UINT16 base;         /**< 底上げ頻度 */
    NJ_UINT16 high;         /**< 最大頻度   */
} NJ_DIC_FREQ;

/**
 * キャッシュ情報定義
 */
typedef struct {
    NJ_UINT32  current;     /**< 現在位置   */
    NJ_UINT32  top;         /**< 開始       */
    NJ_UINT32  bottom;      /**< 終了       */
    NJ_UINT8  *node;        /**< ノード情報 */
    NJ_UINT8  *now;         /**< ノード情報 */
    NJ_UINT16  idx_no;      /**< ノード情報 */
} NJ_CACHE_INFO;

/**
 * キャッシュ管理領域定義
 */
typedef struct {
    NJ_UINT8      statusFlg;                        /**< 状態領域 */
#define NJ_STATUSFLG_CACHEOVER ((NJ_UINT8)0x01)     /**< NJ_SEARCH_CACHE::statusFlg キャッシュ溢れ */
#define NJ_STATUSFLG_AIMAI     ((NJ_UINT8)0x02)     /**< NJ_SEARCH_CACHE::statusFlg 曖昧順モード */
#define NJ_STATUSFLG_HINDO     ((NJ_UINT8)0x04)     /**< NJ_SEARCH_CACHE::statusFlg 頻度順モード */
    NJ_UINT8      viewCnt;                          /**< 同頻度切替カウンタ */
    NJ_UINT16     keyPtr[NJ_MAX_KEYWORD];           /**< キャッシュ情報格納位置 */
    NJ_CACHE_INFO storebuff[NJ_SEARCH_CACHE_SIZE];  /**< キャッシュ情報領域 */
} NJ_SEARCH_CACHE;

/**
 * NJ_SEARCH_CACHE: キャッシュ溢れ状態取得
 *
 * @param[in]  s : キャッシュ管理領域(NJ_SEARCH_CACHE*)
 *
 * @retval     0 : 溢れ状態でない
 * @retval    !0 : 溢れ状態
 */
#define NJ_GET_CACHEOVER_FROM_SCACHE(s) ((s)->statusFlg & NJ_STATUSFLG_CACHEOVER)
/**
 * NJ_SEARCH_CACHE: 曖昧順モード状態取得
 *
 * @param[in]  s : キャッシュ管理領域(NJ_SEARCH_CACHE*)
 *
 * @retval     0 : 曖昧順モードでない
 * @retval    !0 : 曖昧順モード
 */
#define NJ_GET_AIMAI_FROM_SCACHE(s)     ((s)->statusFlg & NJ_STATUSFLG_AIMAI)
/**
 * NJ_SEARCH_CACHE: キャッシュ溢れ状態設定
 *
 * @param[out]  s : キャッシュ管理領域(NJ_SEARCH_CACHE*)
 *
 * @return         なし
 */
#define NJ_SET_CACHEOVER_TO_SCACHE(s)   ((s)->statusFlg |= NJ_STATUSFLG_CACHEOVER)
/**
 * NJ_SEARCH_CACHE: 曖昧順モード設定
 *
 * @param[out]  s : キャッシュ管理領域(NJ_SEARCH_CACHE*)
 *
 * @return         なし
 */
#define NJ_SET_AIMAI_TO_SCACHE(s)       ((s)->statusFlg |= NJ_STATUSFLG_AIMAI)
/**
 * NJ_SEARCH_CACHE: キャッシュ溢れ状態設定解除
 *
 * @param[out] s : キャッシュ管理領域(NJ_SEARCH_CACHE*)
 *
 * @return         なし
 */
#define NJ_UNSET_CACHEOVER_TO_SCACHE(s) ((s)->statusFlg &= ~NJ_STATUSFLG_CACHEOVER) 
/**
 * NJ_SEARCH_CACHE: 曖昧順モード設定解除
 *
 * @param[out] s : キャッシュ管理領域(NJ_SEARCH_CACHE*)
 *
 * @return         なし
 */
#define NJ_UNSET_AIMAI_TO_SCACHE(s)     ((s)->statusFlg &= ~NJ_STATUSFLG_AIMAI)


/**
 * ストレージ辞書情報定義
 */
typedef struct {
    NJ_UINT32     dictype;                               /**< [内部使用] 辞書種別 */
    NJ_UINT32     dicsize;                               /**< [内部使用] 辞書サイズ */
    NJ_UINT32     mode;                                  /**< [内部使用] 動作モード */
    NJ_FILE *     filestream;                            /**< [内部使用] ファイルポインタ */
#define NJ_STORAGE_DIC_CACHE_MAX 12                      /**< NJ_STORAGE_DIC_INFO : キャッシュ数 */
#define NJ_STORAGE_DIC_HEAD       0                      /**< 辞書ヘッダ情報 */
    NJ_VOID *     cache_area[NJ_STORAGE_DIC_CACHE_MAX];  /**< [内部使用] キャッシュ領域 */
    NJ_VOID *     extension_data;                        /**< 拡張情報 */
} NJ_STORAGE_DIC_INFO;


/**
 * 辞書情報定義
 */
typedef struct {
    NJ_UINT8            type;           /**< 辞書ハンドルタイプ */
#define NJ_DIC_H_TYPE_NORMAL      0x00  /**< NJ_DIC_INFO::type 通常辞書 */
#define NJ_DIC_H_TYPE_PROGRAM     0x01  /**< NJ_DIC_INFO::type 擬似辞書 */
#define NJ_DIC_H_TYPE_PROGRAM_FZK 0x02  /**< NJ_DIC_INFO::type 擬似辞書-付属語- */
#define NJ_DIC_H_TYPE_ON_STORAGE  0x03  /**< NJ_DIC_INFO::type ストレージ辞書 */
    NJ_UINT8            limit;          /**< 検索制限   */

    NJ_DIC_HANDLE       handle;         /**< 辞書ハンドル＆擬似辞書関数ポインタ */

#define NJ_MAX_EXT_AREA  3  /**< NJ_DIC_INFO: 最大拡張領域数 */ 
    NJ_VOID *           ext_area[NJ_MAX_EXT_AREA];        /**< 拡張領域 */
#define NJ_TYPE_EXT_AREA_DEFAULT  0  /**< NJ_DIC_INFO::頻度学習、擬似辞書モジュール辞書設定 等 */
#define NJ_TYPE_EXT_AREA_INPUT    1  /**< NJ_DIC_INFO::文字入力用拡張インデックス */
#define NJ_TYPE_EXT_AREA_MORPHO   2  /**< NJ_DIC_INFO::形態素解析用拡張インデックス */

    NJ_VOID *           add_info[NJ_MAX_ADDITIONAL_INFO]; /**< 付加情報領域 */

#define NJ_MODE_TYPE_MAX  3  /**< NJ_DIC_INFO: 辞書頻度タイプ数 */ 
    /** 辞書頻度情報 */
    NJ_DIC_FREQ         dic_freq[NJ_MODE_TYPE_MAX];
#define NJ_MODE_TYPE_HENKAN  0  /**< NJ_DIC_INFO::dic_freq 通常変換辞書頻度 */ 
#define NJ_MODE_TYPE_YOSOKU  1  /**< NJ_DIC_INFO::dic_freq 予測変換辞書頻度 */ 
#define NJ_MODE_TYPE_MORPHO  2  /**< NJ_DIC_INFO::dic_freq 形態素解析辞書頻度 */ 

    NJ_SEARCH_CACHE *   srhCache;       /**< キャッシュ管理領域 */
} NJ_DIC_INFO;

/**
 * デフォルト辞書頻度値
 */
#define NJ_DEFAULT_FREQ_DIC_UBASE_H_HIGH    550 /**< デフォルト辞書頻度::統合辞書[変換用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_UBASE_H_BASE    400 /**< デフォルト辞書頻度::統合辞書[変換用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_UBASE_Y_HIGH    560 /**< デフォルト辞書頻度::統合辞書[予測用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_UBASE_Y_BASE    100 /**< デフォルト辞書頻度::統合辞書[予測用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_UBASE_M_HIGH    500 /**< デフォルト辞書頻度::統合辞書[形態素解析用]-最高-       */
#define NJ_DEFAULT_FREQ_DIC_UBASE_M_BASE    400 /**< デフォルト辞書頻度::統合辞書[形態素解析用]-底上げ-     */

#define NJ_DEFAULT_FREQ_DIC_TAN_H_HIGH      10  /**< デフォルト辞書頻度::単漢字辞書[変換用]-最高-       */
#define NJ_DEFAULT_FREQ_DIC_TAN_H_BASE      0   /**< デフォルト辞書頻度::単漢字辞書[変換用]-底上げ-     */
#define NJ_DEFAULT_FREQ_DIC_TAN_Y_HIGH      0   /**< デフォルト辞書頻度::単漢字辞書[予測用]-最高-       */
#define NJ_DEFAULT_FREQ_DIC_TAN_Y_BASE      10  /**< デフォルト辞書頻度::単漢字辞書[予測用]-底上げ-     */
#define NJ_DEFAULT_FREQ_DIC_TAN_M_HIGH      10  /**< デフォルト辞書頻度::単漢字辞書[形態素解析用]-最高-     */
#define NJ_DEFAULT_FREQ_DIC_TAN_M_BASE      0   /**< デフォルト辞書頻度::単漢字辞書[形態素解析用]-底上げ-   */

#define NJ_DEFAULT_FREQ_DIC_FZK_H_HIGH      500 /**< デフォルト辞書頻度::付属語辞書[変換用]-最高-       */
#define NJ_DEFAULT_FREQ_DIC_FZK_H_BASE      400 /**< デフォルト辞書頻度::付属語辞書[変換用]-底上げ-     */
#define NJ_DEFAULT_FREQ_DIC_FZK_Y_HIGH      0   /**< デフォルト辞書頻度::付属語辞書[予測用]-最高-       */
#define NJ_DEFAULT_FREQ_DIC_FZK_Y_BASE      10  /**< デフォルト辞書頻度::付属語辞書[予測用]-底上げ-     */
#define NJ_DEFAULT_FREQ_DIC_FZK_M_HIGH      500 /**< デフォルト辞書頻度::付属語辞書[形態素解析用]-最高-     */
#define NJ_DEFAULT_FREQ_DIC_FZK_M_BASE      400 /**< デフォルト辞書頻度::付属語辞書[形態素解析用]-底上げ-   */

#define NJ_DEFAULT_FREQ_DIC_YOMI_H_HIGH     0   /**< デフォルト辞書頻度::読み無し予測辞書[変換用]-最高-     */
#define NJ_DEFAULT_FREQ_DIC_YOMI_H_BASE     10  /**< デフォルト辞書頻度::読み無し予測辞書[変換用]-底上げ-   */
#define NJ_DEFAULT_FREQ_DIC_YOMI_Y_HIGH     244 /**< デフォルト辞書頻度::読み無し予測辞書[予測用]-最高-     */
#define NJ_DEFAULT_FREQ_DIC_YOMI_Y_BASE     100 /**< デフォルト辞書頻度::読み無し予測辞書[予測用]-底上げ-   */
#define NJ_DEFAULT_FREQ_DIC_YOMI_M_HIGH     0   /**< デフォルト辞書頻度::読み無し予測辞書[形態素解析用]-最高-   */
#define NJ_DEFAULT_FREQ_DIC_YOMI_M_BASE     10  /**< デフォルト辞書頻度::読み無し予測辞書[形態素解析用]-底上げ- */

#define NJ_DEFAULT_FREQ_DIC_HEAD_YOMI_H_HIGH     0   /**< デフォルト辞書頻度::読み無し予測辞書[変換用]-最高-     */
#define NJ_DEFAULT_FREQ_DIC_HEAD_YOMI_H_BASE     10  /**< デフォルト辞書頻度::読み無し予測辞書[変換用]-底上げ-   */
#define NJ_DEFAULT_FREQ_DIC_HEAD_YOMI_Y_HIGH     920 /**< デフォルト辞書頻度::読み無し予測辞書[予測用]-最高-     */
#define NJ_DEFAULT_FREQ_DIC_HEAD_YOMI_Y_BASE     820 /**< デフォルト辞書頻度::読み無し予測辞書[予測用]-底上げ-   */
#define NJ_DEFAULT_FREQ_DIC_HEAD_YOMI_M_HIGH     0   /**< デフォルト辞書頻度::読み無し予測辞書[形態素解析用]-最高-   */
#define NJ_DEFAULT_FREQ_DIC_HEAD_YOMI_M_BASE     10  /**< デフォルト辞書頻度::読み無し予測辞書[形態素解析用]-底上げ- */

#define NJ_DEFAULT_FREQ_DIC_EXT_YOMI_H_HIGH 0   /**< デフォルト辞書頻度::拡張読み無し予測辞書[変換用]-最高-     */
#define NJ_DEFAULT_FREQ_DIC_EXT_YOMI_H_BASE 10  /**< デフォルト辞書頻度::拡張読み無し予測辞書[変換用]-底上げ-   */
#define NJ_DEFAULT_FREQ_DIC_EXT_YOMI_Y_HIGH 244 /**< デフォルト辞書頻度::拡張読み無し予測辞書[予測用]-最高-     */
#define NJ_DEFAULT_FREQ_DIC_EXT_YOMI_Y_BASE 100 /**< デフォルト辞書頻度::拡張読み無し予測辞書[予測用]-底上げ-   */
#define NJ_DEFAULT_FREQ_DIC_EXT_YOMI_M_HIGH 0   /**< デフォルト辞書頻度::拡張読み無し予測辞書[形態素解析用]-最高-   */
#define NJ_DEFAULT_FREQ_DIC_EXT_YOMI_M_BASE 10  /**< デフォルト辞書頻度::拡張読み無し予測辞書[形態素解析用]-底上げ- */

#define NJ_DEFAULT_FREQ_DIC_USER_H_HIGH     410 /**< デフォルト辞書頻度::ユーザ辞書[変換用]-最高-       */
#define NJ_DEFAULT_FREQ_DIC_USER_H_BASE     410 /**< デフォルト辞書頻度::ユーザ辞書[変換用]-底上げ-     */
#define NJ_DEFAULT_FREQ_DIC_USER_Y_HIGH     0   /**< デフォルト辞書頻度::ユーザ辞書[予測用]-最高-       */
#define NJ_DEFAULT_FREQ_DIC_USER_Y_BASE     10  /**< デフォルト辞書頻度::ユーザ辞書[予測用]-底上げ-     */
#define NJ_DEFAULT_FREQ_DIC_USER_M_HIGH     0   /**< デフォルト辞書頻度::ユーザ辞書[形態素解析用]-最高-     */
#define NJ_DEFAULT_FREQ_DIC_USER_M_BASE     10  /**< デフォルト辞書頻度::ユーザ辞書[形態素解析用]-底上げ-   */

#define NJ_DEFAULT_FREQ_DIC_LEARN_H_HIGH    1000    /**< デフォルト辞書頻度::学習辞書[変換用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_LEARN_H_BASE    501     /**< デフォルト辞書頻度::学習辞書[変換用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_LEARN_Y_HIGH    1000    /**< デフォルト辞書頻度::学習辞書[予測用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_LEARN_Y_BASE    501     /**< デフォルト辞書頻度::学習辞書[予測用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_LEARN_M_HIGH    0       /**< デフォルト辞書頻度::学習辞書[形態素解析用]-最高-   */
#define NJ_DEFAULT_FREQ_DIC_LEARN_M_BASE    10      /**< デフォルト辞書頻度::学習辞書[形態素解析用]-底上げ- */

#define NJ_DEFAULT_FREQ_DIC_CUSTOM_H_HIGH   400     /**< デフォルト辞書頻度::カスタマイズ辞書[変換用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_CUSTOM_H_BASE   0       /**< デフォルト辞書頻度::カスタマイズ辞書[変換用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_CUSTOM_Y_HIGH   400     /**< デフォルト辞書頻度::カスタマイズ辞書[予測用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_CUSTOM_Y_BASE   0       /**< デフォルト辞書頻度::カスタマイズ辞書[予測用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_CUSTOM_M_HIGH   400     /**< デフォルト辞書頻度::カスタマイズ辞書[形態素解析用]-最高-   */
#define NJ_DEFAULT_FREQ_DIC_CUSTOM_M_BASE   0       /**< デフォルト辞書頻度::カスタマイズ辞書[形態素解析用]-底上げ- */


/**
 * 辞書セット定義
 */
typedef struct {
    NJ_DIC_INFO   dic[NJ_MAX_DIC];           /**< 辞書情報 */
    NJ_DIC_HANDLE  rHandle[NJ_MODE_TYPE_MAX]; /* ルール辞書マウント領域 */

    /** [内部使用] 動作モード */
    NJ_UINT16           mode;
#define NJ_CACHE_MODE_NONE          0x0000   /**< NJ_DIC_SET::mode 動作なし */ 
#define NJ_CACHE_MODE_VALID         0x0001   /**< NJ_DIC_SET::mode キャッシュ検索モードON */ 
#define NJ_CACHE_MODE_VALID_FUSION  0x0002   /**< NJ_DIC_SET::mode キャッシュ検索モードON
                                                  (辞書引きAPI-統合辞書のみ) */ 
    /** [内部使用] 曖昧検索キーワード */
    NJ_CHAR             keyword[NJ_MAX_KEYWORD];
} NJ_DIC_SET;

/**
 * あいまい文字セット定義
 */
typedef struct {
    NJ_UINT16  charset_count;               /**< charsetの格納数 */
    NJ_CHAR    *from[NJ_MAX_CHARSET];       /**< 置換前文字      */
    NJ_CHAR    *to[NJ_MAX_CHARSET];         /**< 置換後文字      */
} NJ_CHARSET;


/**
 * 辞書検索条件セット定義
 */
typedef struct {

    NJ_UINT8 operation;          /**< 検索方法 */
#define NJ_CUR_OP_COMP      0    /**< NJ_SEARCH_CONDITION::operation 正引き完全一致検索 */
#define NJ_CUR_OP_FORE      1    /**< NJ_SEARCH_CONDITION::operation 正引き前方一致検索 */
#define NJ_CUR_OP_LINK      2    /**< NJ_SEARCH_CONDITION::operation つながり検索      */
#define NJ_CUR_OP_REV       3    /**< NJ_SEARCH_CONDITION::operation 逆引き完全一致検索 */
#define NJ_CUR_OP_REV_FORE  4    /**< NJ_SEARCH_CONDITION::operation 逆引き前方一致検索 */
#define NJ_CUR_OP_COMP_EXT  5    /**< NJ_SEARCH_CONDITION::operation 正引き完全一致検索(拡張入力) */
#define NJ_CUR_OP_FORE_EXT  6    /**< NJ_SEARCH_CONDITION::operation 正引き前方一致検索(拡張入力) */

    NJ_UINT8 mode;               /**< 検索候補取得順 */
#define NJ_CUR_MODE_FREQ    0    /**< NJ_SEARCH_CONDITION::mode 頻度順 */
#define NJ_CUR_MODE_YOMI    1    /**< NJ_SEARCH_CONDITION::mode 読み順 */
#define NJ_CUR_MODE_REGIST  2    /**< NJ_SEARCH_CONDITION::mode 登録順 */

    NJ_UINT8   fzkconnect;       /* 付属語接続有無 */
    NJ_UINT8   ctrl_opt;         /* [内部使用] */
/** NJ_SEARCH_CONDITION::ctrl_opt 検索制限解除 */
#define NJ_SEARCH_DISMANTLING_CONTROL 0x80
    NJ_UINT32  attr;             /**< 状況カテゴリ */

    NJ_DIC_SET *ds;              /**< 検索対象辞書セット   */

    /**
     * [内部使用] 品詞情報
     */
    struct {
        NJ_UINT8 *fore;          /**< 前品詞条件 */
        NJ_UINT16 foreSize;      /**< 登録前品詞数 */
        NJ_UINT16 foreFlag;      /**< 0:通常比較、1:反転比較 */
        NJ_UINT8 *rear;          /**< 後品詞条件 */
        NJ_UINT16 rearSize;      /**< 登録後品詞数 */
        NJ_UINT16 rearFlag;      /**< 0:通常比較、1:反転比較 */
        NJ_UINT8 *yominasi_fore; /**< 前品詞条件(読み無し) */
        NJ_UINT16 prev_bpos;     /**< 前確定単語の後品詞番号(拡張読み無し) */
    } hinsi;

    NJ_CHAR  *yomi;       /**< 検索する単語の読み文字列
                               @attention ターミネートは(NJ_UINT8)0 */
    NJ_CHAR  *kanji;      /**< operation = NJ_CUR_OP_LINK 指定時の予測元表記文字列
                               @attention ターミネートは(NJ_UINT8)0*/
    NJ_CHARSET *charset;  /**< あいまい文字セット */
    NJ_UINT16 ylen;       /**< 読み文字列長(文字配列要素数) */
    NJ_UINT16 yclen;      /**< 読み文字列長(文字数) 評価部でのみ使用する */
} NJ_SEARCH_CONDITION;

/**
 * [内部使用] 辞書内位置情報定義
 */
typedef struct {
    NJ_DIC_HANDLE  handle;        /**< 辞書ハンドル */
    NJ_VOID *      ext_area[NJ_MAX_EXT_AREA];        /**< 拡張領域 */
    NJ_VOID *      add_info[NJ_MAX_ADDITIONAL_INFO]; /**< 付加情報領域 */
    NJ_UINT32      current;       /**< 現在位置     */
    NJ_UINT32      top;           /**< 先頭位置     */
    NJ_UINT32      bottom;        /**< 末尾位置     */
    NJ_UINT32      relation[NJ_MAX_PHR_CONNECT];   /* 先頭位置 */
    NJ_UINT32      attr;         /**< 属性情報 */
    NJ_UINT16      current_cache; /**< 現在検索キャッシュ位置 */
    NJ_UINT8       current_info;  /* 結合文節数   */
    NJ_UINT8       status;   /* 検索状態 */    
    NJ_UINT8       type;     /**< 辞書タイプ */
} NJ_SEARCH_LOCATION;

/**
 * 辞書毎検索単語情報定義
 */
typedef struct {
    NJ_HINDO           cache_freq;   /**< 評価値   */
    NJ_DIC_FREQ        dic_freq;     /**< 辞書頻度 */
    NJ_INT16           dic_freq_max; /**< 辞書頻度最大値 */
    NJ_INT16           dic_freq_min; /**< 辞書頻度最小値 */
    NJ_SEARCH_LOCATION loct;         /**< 検索位置 */
} NJ_SEARCH_LOCATION_SET;

/**
 * 辞書検索カーソル定義
 */
typedef struct {
    NJ_SEARCH_CONDITION cond;                   /**< 辞書検索条件 */
    NJ_SEARCH_LOCATION_SET loctset[NJ_MAX_DIC]; /**< 辞書毎検索単語情報 */
} NJ_CURSOR;

/**
 * 単語登録情報定義
 */
typedef struct {
    NJ_UINT8 hinsi_group;          /**< 品詞グループ */
#define NJ_HINSI_MEISI          0    /**< NJ_WORD_INFO::hinsi_group 名詞(スル活用あり) */
#define NJ_HINSI_JINMEI         1    /**< NJ_WORD_INFO::hinsi_group 人名 */
#define NJ_HINSI_MEISI_NO_CONJ  2    /**< NJ_WORD_INFO::hinsi_group 名詞(スル活用なし) */
#define NJ_HINSI_CHIMEI         2    /**< NJ_WORD_INFO::hinsi_group 地名/駅名 */
#define NJ_HINSI_KIGOU          3    /**< NJ_WORD_INFO::hinsi_group 記号 */
#define NJ_HINSI_DETAIL         255  /**< NJ_WORD_INFO::hinsi_group 詳細取得 */

    NJ_CHAR  yomi[NJ_MAX_LEN + NJ_TERM_LEN];         /**< 登録読み文字列(NUL含む) */
    NJ_CHAR  kouho[NJ_MAX_RESULT_LEN + NJ_TERM_LEN]; /**< 登録候補文字列(NUL含む) */
    NJ_CHAR  additional[NJ_MAX_ADDITIONAL_LEN + NJ_TERM_LEN]; /**< 付加情報文字列(NUL含む) */

    /** 自立語情報 */
    struct {
        NJ_UINT16  yomi_len;    /**< 読み文字配列長  */
        NJ_UINT16  kouho_len;   /**< 候補文字配列長  */
        NJ_UINT32  hinsi;       /**< 品詞            */
        NJ_UINT32  attr;        /**< 属性データ      */
        NJ_INT16   freq;        /**< 頻度            */
    } stem;

    /** 付属語情報 */
    struct {
        NJ_UINT16  yomi_len;    /**< 読み文字配列長  */
        NJ_UINT16  kouho_len;   /**< 候補文字配列長  */
        NJ_UINT32  hinsi;       /**< 品詞           */
        NJ_INT16   freq;        /**< 頻度           */
    } fzk;

    NJ_INT16   connect;         /**< 接続フラグ      */

} NJ_WORD_INFO;



/**
 * 処理結果文節/単語情報定義
 */
typedef struct {
    NJ_CHAR  *yomi;   /**< 読み文字列 */

    /** 自立語情報 */
    struct NJ_STEM {
        NJ_UINT16  info1;       /**< 上位9bit:前品詞番号, 下位7bit:読み文字配列要素数 */
        NJ_UINT16  info2;       /**< 上位9bit:後品詞番号, 下位7bit:候補文字配列要素数 */
        NJ_HINDO   hindo;       /**< 頻度値 */
        NJ_SEARCH_LOCATION loc; /**< 辞書位置 */
        NJ_UINT8   type;        /**< 擬似候補の種類 */
        NJ_UINT8   info3;       /**< 拡張入力用：読み文字配列要素数 */
    } stem;

    /** 付属語情報 */
    struct NJ_FZK {
        NJ_UINT16  info1;       /**< 上位9bit:前品詞番号, 下位7bit:読み文字配列要素数 */
        NJ_UINT16  info2;       /**< 上位9bit:後品詞番号, 下位7bit:候補文字配列要素数 */
        NJ_HINDO   hindo;       /**< 頻度値 */
    } fzk;
} NJ_WORD;

/**
 * NJ_WORD: 自立語前品詞取得
 *
 * @param[in]  s : 文節/単語情報(NJ_WORD*)
 *
 * @return         自立語前品詞
 */
#define NJ_GET_FPOS_FROM_STEM(s) ((NJ_UINT16)((s)->stem.info1 >> 7))
/**
 * NJ_WORD: 自立語読み文字配列長取得
 *
 * @param[in]  s : 文節/単語情報(NJ_WORD*)
 *
 * @return         自立語読み文字配列長
 */
#define NJ_GET_YLEN_FROM_STEM(s) ((NJ_UINT8)((s)->stem.info1 & 0x7F))
/*
 * NJ_WORD: 自立語読み文字配列長取得(拡張検索時)
 *
 * @param[in]  s : 文節/単語情報(NJ_WORD*)
 *
 * @return         自立語読み文字配列長
 */
#define NJ_GET_EXT_YLEN_FROM_STEM(s) ((NJ_UINT8)((s)->stem.info3))
/**
 * NJ_WORD: 自立語後品詞取得
 *
 * @param[in]  s : 文節/単語情報(NJ_WORD*)
 *
 * @return         自立語後品詞
 */
#define NJ_GET_BPOS_FROM_STEM(s) ((NJ_UINT16)((s)->stem.info2 >> 7))
/**
 * NJ_WORD: 自立語候補文字列長取得
 *
 * @param[in]  s : 文節/単語情報(NJ_WORD*)
 *
 * @return         自立語候補文字列長
 */
#define NJ_GET_KLEN_FROM_STEM(s) ((NJ_UINT8)((s)->stem.info2 & 0x7F))
/**
 * NJ_WORD: 自立語読み文字配列長取得(拡張入力対応)
 *
 * @param[in]  s : 文節/単語情報(NJ_WORD*)
 *
 * @return         自立語読み文字配列長
 */
#define NJ_GET_YLEN_FROM_STEM_EXT(s) (((((((s)->stem.loc.status) >> 4) & 0x0F) == NJ_CUR_OP_COMP_EXT) || \
                                       (((((s)->stem.loc.status) >> 4) & 0x0F) == NJ_CUR_OP_FORE_EXT))   \
                                      ? NJ_GET_KLEN_FROM_STEM((s)) : NJ_GET_YLEN_FROM_STEM((s)))
/**
 * NJ_WORD: 自立語頻度値取得
 *
 * @param[in]  s : 文節/単語情報(NJ_WORD*)
 *
 * @return         自立語頻度値
 */
#define NJ_GET_FREQ_FROM_STEM(s) ((NJ_INT16)((s)->stem.hindo))
/**
 * NJ_WORD: 自立語タイプ取得
 *
 * @param[in]  s : 文節/単語情報(NJ_WORD*)
 *
 * @return         自立語タイプ
 */
#define NJ_GET_TYPE_FROM_STEM(s) ((s)->stem.type)
/**
 * NJ_WORD: 自立語前品詞設定
 *
 * @param[out] s : 文節/単語情報(NJ_WORD*)
 * @param[in]  v : 自立語前品詞
 *
 * @return         なし
 */
#define NJ_SET_FPOS_TO_STEM(s,v) ((s)->stem.info1 = ((s)->stem.info1 & 0x007F) | (NJ_UINT16)((v) << 7))
/**
 * NJ_WORD: 自立語読み文字配列長設定
 *
 * @param[out] s : 文節/単語情報(NJ_WORD*)
 * @param[in]  v : 自立語読み文字配列長
 *
 * @return         なし
 */
#define NJ_SET_YLEN_TO_STEM(s,v) ((s)->stem.info1 = ((s)->stem.info1 & 0xFF80) | (NJ_UINT16)((v) & 0x7F))
/**
 * NJ_WORD: 自立語読み文字配列長設定(拡張検索)
 *
 * @param[out] s : 文節/単語情報(NJ_WORD*)
 * @param[in]  v : 自立語読み文字配列長
 *
 * @return         なし
 */
#define NJ_SET_EXT_YLEN_TO_STEM(s,v) ((s)->stem.info3 = (NJ_UINT8)(v))
/**
 * NJ_WORD: 自立語後品詞設定
 *
 * @param[out] s : 文節/単語情報(NJ_WORD*)
 * @param[in]  v : 自立語後品詞
 *
 * @return         なし
 */
#define NJ_SET_BPOS_TO_STEM(s,v) ((s)->stem.info2 = ((s)->stem.info2 & 0x007F) | (NJ_UINT16)((v) << 7))
/**
 * NJ_WORD: 自立語候補文字配列長設定
 *
 * @param[out] s : 文節/単語情報(NJ_WORD*)
 * @param[in]  v : 自立語候補文字配列長
 *
 * @return         なし
 */
#define NJ_SET_KLEN_TO_STEM(s,v) ((s)->stem.info2 = ((s)->stem.info2 & 0xFF80) | (NJ_UINT16)((v) & 0x7F))
/**
 * NJ_WORD: 自立語頻度値設定
 *
 * @param[out] s : 文節/単語情報(NJ_WORD*)
 * @param[in]  v : 自立語頻度値
 *
 * @return         なし
 */
#define NJ_SET_FREQ_TO_STEM(s,v) ((s)->stem.hindo = (NJ_HINDO)(v))
/**
 * NJ_WORD: 自立語タイプ設定
 *
 * @param[out] s : 文節/単語情報(NJ_WORD*)
 * @param[in]  v : 自立語タイプ
 *
 * @return         なし
 */
#define NJ_SET_TYPE_TO_STEM(s,v) ((s)->stem.type = (v))

/**
 * NJ_WORD: 付属語前品詞取得
 *
 * @param[in]  f : 文節/単語情報(NJ_WORD*)
 *
 * @return         付属語前品詞
 */
#define NJ_GET_FPOS_FROM_FZK(f) ((NJ_UINT16)((f)->fzk.info1 >> 7))
/**
 * NJ_WORD: 付属語読み文字配列長取得
 *
 * @param[in]  f : 文節/単語情報(NJ_WORD*)
 *
 * @return         付属語読み文字配列長
 */
#define NJ_GET_YLEN_FROM_FZK(f) ((NJ_UINT8)((f)->fzk.info1 & 0x7F))
/**
 * NJ_WORD: 付属語後品詞取得
 *
 * @param[in]  f : 文節/単語情報(NJ_WORD*)
 *
 * @return         付属語後品詞
 */
#define NJ_GET_BPOS_FROM_FZK(f) ((NJ_UINT16)((f)->fzk.info2 >> 7))
/**
 * NJ_WORD: 付属語候補文字列長取得
 *
 * @param[in]  f : 文節/単語情報(NJ_WORD*)
 *
 * @return         付属語候補文字列長
 */
#define NJ_GET_KLEN_FROM_FZK(f) ((NJ_UINT8)((f)->fzk.info2 & 0x7F))
/**
 * NJ_WORD: 付属語頻度値取得
 *
 * @param[in]  f : 文節/単語情報(NJ_WORD*)
 *
 * @return         付属語頻度値
 */
#define NJ_GET_FREQ_FROM_FZK(f) ((f)->fzk.hindo)
/**
 * NJ_WORD: 付属語前品詞設定
 *
 * @param[out]  s : 文節/単語情報(NJ_WORD*)
 * @param[in]  v : 付属語前品詞
 *
 * @return         なし
 */
#define NJ_SET_FPOS_TO_FZK(s,v) ((s)->fzk.info1 = ((s)->fzk.info1 & 0x007F) | (NJ_UINT16)((v) << 7))
/**
 * NJ_WORD: 付属語読み文字配列長設定
 *
 * @param[out]  s : 文節/単語情報(NJ_WORD*)
 * @param[in]  v : 付属語読み文字配列長
 *
 * @return         なし
 */
#define NJ_SET_YLEN_TO_FZK(s,v) ((s)->fzk.info1 = ((s)->fzk.info1 & 0xFF80) | (NJ_UINT16)((v) & 0x7F))
/**
 * NJ_WORD: 付属語後品詞設定
 *
 * @param[out]  s : 文節/単語情報(NJ_WORD*)
 * @param[in]  v : 付属語後品詞
 *
 * @return         なし
 */
#define NJ_SET_BPOS_TO_FZK(s,v) ((s)->fzk.info2 = ((s)->fzk.info2 & 0x007F) | (NJ_UINT16)((v) << 7))
/**
 * NJ_WORD: 付属語候補文字配列長設定
 *
 * @param[out]  s : 文節/単語情報(NJ_WORD*)
 * @param[in]  v : 付属語候補文字配列長
 *
 * @return         なし
 */
#define NJ_SET_KLEN_TO_FZK(s,v) ((s)->fzk.info2 = ((s)->fzk.info2 & 0xFF80) | (NJ_UINT16)((v) & 0x7F))
/**
 * NJ_WORD: 付属語頻度値設定
 *
 * @param[out] s : 文節/単語情報(NJ_WORD*)
 * @param[in]  v : 付属語頻度値
 *
 * @return         なし
 */
#define NJ_SET_FREQ_TO_FZK(s,v) ((s)->fzk.hindo = (NJ_HINDO)(v))

/**
 * 処理結果情報定義
 */
typedef struct {
    /** オペレーション番号 */
    NJ_UINT16 operation_id;
#define NJ_OP_MASK          0x000f  /**< NJ_RESULT::operation_id: オペレーションマスク */
    /**
     * NJ_RESULT::operation_id: オペレーション取得
     *
     * @param[in]  id : operation_id
     *
     * @return          オペレーション（NJ_OP_* のいずれか）
     */
#define NJ_GET_RESULT_OP(id) ((id) & NJ_OP_MASK)
#define NJ_OP_SEARCH        0x0000  /**< NJ_RESULT::operation_id: 辞書検索 */
#define NJ_OP_CONVERT       0x0001  /**< NJ_RESULT::operation_id: 通常変換 */
#define NJ_OP_ANALYZE       0x0002  /**< NJ_RESULT::operation_id: 予測変換 */
#define NJ_OP_MORPHOLIZE    0x0003  /**< NJ_RESULT::operation_id: 形態素解析 */
#define NJ_OP_ENV           0x0004  /**< NJ_RESULT::operation_id: 全候補  */

#define NJ_FUNC_MASK        0x00f0  /**< NJ_RESULT::operation_id: 機能マスク */
    /**
     * NJ_RESULT::operation_id: 関数取得
     *
     * @param[in]  id : operation_id
     *
     * @return          関数（NJ_FUNC_* のいずれか）
     */
#define NJ_GET_RESULT_FUNC(id) ((id) & NJ_FUNC_MASK)
#define NJ_FUNC_SEARCH              0x0000  /**< NJ_RESULT::operation_id: 辞書検索 */
#define NJ_FUNC_CONVERT_MULTIPLE    0x0010  /**< NJ_RESULT::operation_id: 連文節 */
#define NJ_FUNC_CONVERT_SINGLE      0x0020  /**< NJ_RESULT::operation_id: 単文節 */
#define NJ_FUNC_ZENKOUHO            0x0030  /**< NJ_RESULT::operation_id: 全候補 */
#define NJ_FUNC_NEXT                0x0040  /**< NJ_RESULT::operation_id: 次候補 */
#define NJ_FUNC_SEARCH_R            0x0050  /**< NJ_RESULT::operation_id: 逆引き */
#define NJ_FUNC_SPLIT_WORD          0x0060  /**< NJ_RESULT::operation_id: 形態素 */
#define NJ_FUNC_GET_GIJI            0x0070  /**< NJ_RESULT::operation_id: 擬似候補 */
#define NJ_FUNC_RELATION            0x0080  /**< NJ_RESULT::operation_id: つながり */
#define NJ_FUNC_ZENKOUHO_HEAD       0x0090  /**< NJ_RESULT::operation_id: 先頭文節全候補 */

#define NJ_TYPE_GIJI_MASK           0x0800  /**< NJ_RESULT::operation_id: 擬似タイプマスク */
    /**
     * NJ_RESULT::operation_id: 擬似タイプ取得
     *
     * @param[in]  id : operation_id
     *
     * @return          擬似タイプ（NJ_TYPE_* のいずれか）
     */
#define NJ_GET_GIJI_BIT(id) ((id) & NJ_TYPE_GIJI_MASK)
#define NJ_TYPE_GIJI_BIT            0x0800  /**< NJ_RESULT::operation_id: 擬似候補フラグ*/
#define NJ_TYPE_UNUSED_MASK         0x0700  /**< NJ_RESULT::operation_id: 未使用マスク */
#define NJ_TYPE_MERGE_TMP_BIT       0x0400  /**< NJ_RESULT::operation_id: マージ処理内部処理用*/

#define NJ_DIC_MASK                 0xf000  /**< NJ_RESULT::operation_id: 辞書タイプマスク */
    /**
     * NJ_RESULT::operation_id: 辞書タイプ取得
     *
     * @param[in]  id : operation_id
     *
     * @return          辞書タイプ（NJ_DIC_* のいずれか）
     */
#define NJ_GET_RESULT_DIC(id) ((id) & 0xF000)
#define NJ_DIC_GIJI                 0x0000  /**< NJ_RESULT::operation_id: 擬似候補 */
#define NJ_DIC_STATIC               0x1000  /**< NJ_RESULT::operation_id: 標準辞書 */
#define NJ_DIC_CUSTOMIZE            0x2000  /**< NJ_RESULT::operation_id: 配信辞書 */
#define NJ_DIC_LEARN                0x3000  /**< NJ_RESULT::operation_id: 学習辞書 */
#define NJ_DIC_USER                 0x4000  /**< NJ_RESULT::operation_id: ユーザ辞書 */
#define NJ_DIC_PROGRAM              0x5000  /**< NJ_RESULT::operation_id: 擬似辞書 */

    /** [内部使用] 単語情報 */
    NJ_WORD word;
} NJ_RESULT;

/**
 * オプション定義
 */
typedef struct {
    NJ_UINT16  autoconv_cnt;      /**< 連文節変換自動開始候補数 */
    /** NJ_OPTION: 連文節変換自動開始候補数デフォルト値 */
#define NJ_OPT_AUTOCONV_CNT       NJ_MAX_CANDIDATE
    NJ_UINT16  ext_mode;            /**< API拡張動作モード */
#define NJ_ADD_WORD_OPTIMIZE_OFF         0x0001  /**< NJ_OPTION: API拡張動作モード-辞書最適化処理OFF */
#define NJ_OPT_FORECAST_TOP_FREQ         0x0002  /**< NJ_OPTION: 曖昧検索仕様OFF */
#define NJ_OPT_FORECAST_COMPOUND_WORD    0x0004  /**< NJ_OPTION: 複合語予測学習機能ON */
#define NJ_OPT_CLEARED_EXTAREA_WORD      0x0008  /**< NJ_OPTION: 単語削除時頻度学習データクリアON */
    NJ_VOID *  phase3_filter;       /**< 統合辞書フィルタリング関数ポインタ */
    NJ_VOID *  phase3_option;       /**< 統合辞書フィルタリング関数オプション */
    NJ_VOID *  phase2_filter;       /**< 候補フィルタリング関数ポインタ */
    NJ_VOID *  phase2_option;       /**< 候補フィルタリング関数オプション */
    NJ_VOID *  phase1_filter;       /**< 辞書引きフィルタリング関数ポインタ */
    NJ_VOID *  phase1_option;       /**< 辞書引きフィルタリング関数オプション */
    NJ_VOID *  aip_work;            /**< AI予測用擬似辞書ワークエリア */
    NJ_VOID *  conjugation_data;    /**< 活用形変形データ */
} NJ_OPTION;


/*
 * nj_analyze API 解析制限種別
 */
#define NJ_NO_RENBUN            (NJ_UINT16)0x0001 /**< 解析制限：連文節変換結果無し */
#define NJ_NO_TANBUN            (NJ_UINT16)0x0002 /**< 解析制限：単文節変換結果無し */
#define NJ_NO_CLEAR_LEARN_CACHE (NJ_UINT16)0x0004 /**< 解析制限：学習辞書検索キャッシュクリア抑制 */
#define NJ_NO_LEARN             (NJ_UINT16)0x0008 /**< 解析制限：学習辞書内予測結果無し */
#define NJ_NO_YOSOKU            (NJ_UINT16)0x0010 /**< 解析制限：予測辞書内予測結果無し */
#define NJ_NO_ZEN               (NJ_UINT16)0x0020 /**< 解析制限：全候補結果無し */
#define NJ_HEAD_CONV_ON         (NJ_UINT16)0x0400 /**< 解析制限：逐次単文節変換有効  */
#define NJ_HEAD_CONV2_ON        (NJ_UINT16)0x0800 /**< 解析制限：逐次単文節変換常時有効  */
#define NJ_YOMINASI_ON          (NJ_UINT16)0x0100 /**< 解析制限：読み無し予測絞込検索有り */
#define NJ_RELATION_ON          (NJ_UINT16)0x0200 /**< 解析制限：繋がり予測絞込検索有り */

/**
 * 予測オプション定義
 */
typedef struct {
    NJ_UINT16  mode;                            /**< 解析制限 */
#define NJ_DEFAULT_MODE (NJ_NO_TANBUN|NJ_RELATION_ON|NJ_YOMINASI_ON|NJ_HEAD_CONV_ON)/**< NJ_ANALYZE_OPTION::mode デフォルト値 */
    NJ_UINT16  forecast_learn_limit;            /**< 学習辞書内最大予測候補取得数 */
#define NJ_DEFAULT_FORECAST_LEARN_LIMIT 30      /**< NJ_ANALYZE_OPTION::forecast_learn_limit デフォルト値 */
    NJ_UINT16  forecast_limit;                  /**< 最大予測候補取得数 */
#define NJ_DEFAULT_FORECAST_LIMIT 100           /**< NJ_ANALYZE_OPTION::forecast_limit デフォルト値 */
    NJ_UINT8   char_min;                        /**< 最小読み文字数 */
#define NJ_DEFAULT_CHAR_MIN 0                   /**< NJ_ANALYZE_OPTION::char_min デフォルト値 */
    NJ_UINT8   char_max;                        /**< 最大読み文字数 */
#define NJ_DEFAULT_CHAR_MAX NJ_MAX_LEN          /**< NJ_ANALYZE_OPTION::char_max デフォルト値 */
    NJ_UINT8   in_divide_pos;                   /**< 逐次単文節変換：指定文節区切り位置 */
#define NJ_DEFAULT_IN_DIVIDE_POS 0              /**< NJ_ANALYZE_OPTION::in_divide_pos デフォルト値 */
    NJ_UINT8   out_divide_pos;                  /**< 逐次単文節変換：文節区切り位置結果 */
#define NJ_DEFAULT_OUT_DIVIDE_POS 0             /**< NJ_ANALYZE_OPTION::out_divide_pos デフォルト値 */
} NJ_ANALYZE_OPTION;

#define NJ_MAX_STATE 32                  /**< iWnn利用状況設定数 */

/**
 * 状況計算パラメータ設定定義
 */
typedef struct {
    NJ_INT16 system_max_bias[NJ_MAX_STATE];                 /**< NJ_STATE_CALC_PARAMETER: 状況設定最大バイアス値設定 */
    NJ_INT16 system_min_bias[NJ_MAX_STATE];                 /**< NJ_STATE_CALC_PARAMETER: 状況設定最小バイアス値設定 */
    NJ_INT16 system_add_bias[NJ_MAX_STATE];                 /**< NJ_STATE_CALC_PARAMETER: 状況設定バイアス値常時加算値 */
    NJ_INT16 system_subtract_bias[NJ_MAX_STATE];            /**< NJ_STATE_CALC_PARAMETER: 状況設定バイアス値常時減算値 */
    NJ_INT16 system_base_bias[NJ_MAX_STATE];                /**< NJ_STATE_CALC_PARAMETER: 状況設定バイアス値回復値 */
    NJ_INT16 system_change_bias[NJ_MAX_STATE][NJ_MAX_STATE];/**< NJ_STATE_CALC_PARAMETER: 状況設定学習時バイアス加算値 */
    NJ_INT16 dicinfo_max[NJ_MAX_DIC + 1];                   /**< NJ_STATE_CALC_PARAMETER: 状況設定辞書頻度最大値 */
    NJ_INT16 dicinfo_min[NJ_MAX_DIC + 1];                   /**< NJ_STATE_CALC_PARAMETER: 状況設定辞書頻度最小値 */
} NJ_STATE_CALC_PARAMETER;
#define NJ_STATE_MAX_FREQ     NJ_NUM_THOUSAND         /**< 状況設定 辞書頻度最大値 */
#define NJ_STATE_MIN_FREQ     0                       /**< 状況設定 辞書頻度最小値 */
#define NJ_STATE_TERMINATE    ((NJ_INT16)0x8000)      /**< 状況設定 辞書頻度値設定領域ターミネーター */

/**
 * 状況設定定義
 */
typedef struct {
    NJ_INT16 system[NJ_MAX_STATE];        /**< アプリケーション＆エンジン利用状況設定 */
#define NJ_MAX_EXT_STATE 2               /**< NJ_STATE: アプリケーション利用拡張状況設定数  */
    NJ_INT16 extension[NJ_MAX_EXT_STATE]; /**< アプリケーション利用拡張状況設定          */
    NJ_STATE_CALC_PARAMETER *calc_parameter;
} NJ_STATE;
#define NJ_STATE_MAX_BIAS  NJ_NUM_THOUSAND         /**< 状況設定最大バイアス値 */
#define NJ_STATE_MIN_BIAS  ((-1) * NJ_NUM_THOUSAND)    /**< 状況設定最小バイアス値 */

/**
 * 擬似候補種別の定義
 *
 *   生成できる擬似候補タイプは、NJ_TYPE_HIRAGANA以降とみなします。
 *   また、定義値は連続している必要があります。
 */
/*    処理用定義    */
#define NJ_TYPE_UNDEFINE                 0 /**< 擬似候補処理：未定義 */
#define NJ_TYPE_NONE                     1 /**< 擬似候補処理：(検査対象なし) */
/*    擬似候補タイプ定義    */
#define NJ_TYPE_HIRAGANA                 2 /**< 擬似候補タイプ：ひらがな(無変換) */
#define NJ_TYPE_KATAKANA                 3 /**< 擬似候補タイプ：全角 カタカナ */
#define NJ_TYPE_HANKATA                  4 /**< 擬似候補タイプ：半角 カタカナ */
#define NJ_TYPE_HAN_SUUJI               11 /**< 擬似候補タイプ：半角 数字 */
#define NJ_TYPE_ZEN_SUUJI               12 /**< 擬似候補タイプ：全角 数字 */




/*
 * mm_get_hinsi が返す品詞グループ番号の定義（dtoa でも使用している）
 * 品詞グループ数は HGROUP_COUNT に依存する
 */
#define MM_HGROUP_MEISI         0  /**< 品詞グループ番号：名詞 */
#define MM_HGROUP_GIJI          1  /**< 品詞グループ番号：擬似 */
#define MM_HGROUP_OTHER         2  /**< 品詞グループ番号：その他 */

/*
 * 学習辞書操作API
 * オペレーション定義
 */
#define NJ_MLD_OP_COMMIT        0  /**< 学習辞書操作OP：操作位置記憶   */
#define NJ_MLD_OP_COMMIT_TO_TOP 1  /**< 学習辞書操作OP：学習情報移動   */
#define NJ_MLD_OP_COMMIT_CANCEL 2  /**< 学習辞書操作OP：操作処理中止   */
#define NJ_MLD_OP_GET_SPACE     3  /**< 学習辞書操作OP：登録可能数取得 */


/**
 * 頻度学習値データ定義
 */
typedef struct {
    NJ_UINT32 word_cnt;           /**< 該当データ数 */
    NJ_UINT32 size;               /**< 該当データサイズ */
    NJ_UINT32 offset;             /**< データ先頭からのオフセット位置 */
    NJ_UINT32 exp_offset;         /**< [内部使用]データエクスポート用オフセット */
} NJ_EXT_HINDO_DATA;

/**
 * 頻度学習値情報定義
 */
typedef struct {
    NJ_UINT32 size;                                    /**< 頻度学習値データトータルサイズ */
    NJ_INT32 word_cnt;                                 /**< 頻度学習値データ登録数 */
    NJ_UINT32 imp_offset;                              /**< [内部使用]インポートデータオフセット */
    NJ_UINT32 block_cnt;                               /**< [書き換え禁止]頻度学習値データのブロック数(1ブロック：100件) */
    NJ_UINT32 imp_block;                               /**< [書き換え禁止]インポートしたブロック数 */
    NJ_INT16 imp_hindo;                                /**< [書き換え禁止]インポート中の頻度学習値 */
    NJ_INT16 status;                                   /**< [内部使用]インポート中の状況Status */
#define NJ_OP_IMP_HINDO_INIT     0x0000    /**< 頻度学習データインポート::status: 初期状態 */
#define NJ_OP_IMP_HINDO_READY    0x0001    /**< 頻度学習データインポート::status: インポート実行中 */
#define NJ_OP_IMP_HINDO_END      0x0002    /**< 頻度学習データインポート::status: インポート終了 */

#define NJ_MAX_EXT_HINDO    64                         /**< 頻度学習 最大頻度学習値 */
    NJ_EXT_HINDO_DATA hindo_data[NJ_MAX_EXT_HINDO];    /**< 頻度学習値データ：頻度学習データ */
    NJ_EXT_HINDO_DATA delete_data;                     /**< 頻度学習値データ：削除用データ */
} NJ_EXT_HINDO_INFO;

#define NJ_EXT_HINDO_COMMON_HEADER_SIZE    0x3C    /**< 頻度学習領域入出力データ：共通ヘッダサイズ */
#define NJ_EXT_HINDO_IDENTIFIER_SIZE       0x04    /**< 頻度学習領域入出力データ：識別子サイズ */


/**
 * ユーザープロファイル情報定義
 */
typedef struct {
    NJ_EXT_HINDO_INFO ext_hindo_info;      /**< 頻度学習値情報 */
    NJ_UINT32 size;                        /**< ユーザープロファイルデータサイズ */
} NJ_USER_PROF_INFO;


#define NJ_MAX_CONJ_TYPE        4    /**< 活用タイプ最大数 */
#define NJ_CONJ_TYPE_BASIC      1    /**< 活用タイプ : 基本形 */
#define NJ_CONJ_TYPE_END        2    /**< 活用タイプ : 終止形 */
#define NJ_CONJ_TYPE_PAST       3    /**< 活用タイプ : 過去形 */
#define NJ_CONJ_TYPE_CONT_TE    4    /**< 活用タイプ : テ形 */


/**
 * 付属語解析定義
 */
#define NJ_FZK_MAX_LEN          (NJ_MAX_RESULT_LEN-2) /**< 最大解析文字長 */

/**
 * 付属語情報
 */
typedef struct {
    NJ_WORD    word; /**< 付属語単語情報（API内部で使用、外部からの直接参照不可） */
} NJ_FZK_WORD;

/**
 * 付属語情報から辞書ハンドルを取得する
 *
 * @param[in] x 付属語情報（NJ_FZK_WORD*)
 * @return 辞書ハンドル
 */
#define NJ_FZK_WORD_GET_HANDLE(x)   ((x)->word.stem.loc.handle)

/**
 * 付属語情報から単語IDを取得する
 *
 * @param[in] x 付属語情報（NJ_FZK_WORD*)
 * @return 単語ID
 */
#define NJ_FZK_WORD_GET_ID(x)       ((x)->word.stem.loc.top + (x)->word.stem.loc.current)

/**
 * 付属語情報から前品詞番号を取得する
 *
 * @param[in] x 付属語情報（NJ_FZK_WORD*)
 * @return 前品詞番号
 */
#define NJ_FZK_WORD_GET_POSL(x)     (((x)->word.stem.info1 >> 7) & 0x01FF)

/**
 * 付属語情報から後品詞番号を取得する
 *
 * @param[in] x 付属語情報（NJ_FZK_WORD*)
 * @return 後品詞番号
 */
#define NJ_FZK_WORD_GET_POSR(x)     (((x)->word.stem.info2 >> 7) & 0x01FF)

/**
 * 付属語情報から付属語文字配列長を取得する
 *
 * @param[in] x 付属語情報（NJ_FZK_WORD*)
 * @return 付属語文字配列長（ヌル文字は含まない）
 */
#define NJ_FZK_WORD_GET_LEN(x)      ((x)->word.stem.info1 & 0x007F)

/**
 * 付属語情報から付属語文字配列先頭ポインタを取得する
 *
 * @param[in] x 付属語情報（NJ_FZK_WORD*)
 * @return 付属語文字配列先頭ポインタ
 *
 * @attention 付属語文字配列は必ずしもヌル文字で終端されているとは限らない。@c NJ_FZK_WORD_GET_LEN() マクロで長さを取得し、その範囲で読み込みを行うこと。
 */
#define NJ_FZK_WORD_GET_STR_TOP(x)  ((x)->word.yomi)


/**
 * 付属語パターン
 */
typedef struct _nj_fzk_pattern {
    NJ_FZK_WORD* fzk;              /**< 付属語情報 */
    struct _nj_fzk_pattern* prev;  /**< 前接続付属語パターン */
} NJ_FZK_PATTERN;

/**
 * 付属語解析バッファ
 */
typedef struct {
    NJ_DIC_SET     fds;                                 /**< 付属語解析用辞書セット */
    NJ_FZK_WORD    fzks[NJ_FZK_BUF_MAX_WORD_NUM];       /**< 付属語バッファ */
    NJ_UINT32      fzks_next;                           /**< 付属語バッファカーソル */
    NJ_FZK_PATTERN fzkpat[NJ_FZK_BUF_MAX_PATTERN];      /**< 付属語パターンバッファ */
    NJ_UINT8       fzkpat_tail[NJ_FZK_BUF_MAX_PATTERN]; /**< 付属語長 */
    NJ_INT32       fzkpat_num;                          /**< 付属語パターンバッファ登録数 */
    NJ_INT32       fzkpat_current;                      /**< 付属語パターンバッファカーソル */
    NJ_CHAR        yomi[NJ_FZK_MAX_LEN + NJ_TERM_LEN];  /**< 付属語部分読み文字列 */
    NJ_DIC_HANDLE  rule;                                /**< ルール辞書 */
    NJ_UINT16      max_posL;                            /**< 最大前品詞番号 */
    NJ_UINT16      max_posR;                            /**< 最大後品詞番号 */
    NJ_UINT8       yomi_len;                            /**< 付属語部分読み文字列長 */
    NJ_UINT8       status;                              /**< 内部使用 */
} NJ_FZK_BUF;

#define MM_GET_STEM_LEN(x) ((NJ_UINT16)NJ_GET_YLEN_FROM_STEM(&((x)->word)))


#define MM_GET_CANDIDATE_LEN(x) ((NJ_UINT16)(MM_GET_STEM_LEN(x) + NJ_GET_YLEN_FROM_FZK(&((x)->word))))


/************************************************/
/*            解析情報クラスヘッダ              */
/************************************************/
#include "njx_lib.h"
#include "njx_state.h"

/**
 * 候補リスト定義
 */
typedef struct {
    NJ_RESULT *result;  /**< 処理結果情報   */
    NJ_CLASS  *iwnn;    /**< 解析情報クラス */
} NJ_M_RESULT;

/***********************************************
 * njx_merge_word_listモード定義
 */
/** njx_merge_word_list : リストをクリアして追加 */
#define NJ_MERGE_INIT  0x0001
/** njx_merge_word_list : 存在しないものを追加 */
#define NJ_MERGE_NOT_EXIST  0x0002
/** njx_merge_word_list : 存在しないものを必ず追加 */
#define NJ_MERGE_NOT_EXIST_FORCE  0x0102
/** njx_merge_word_list : すべて必ず末尾に移動 */
#define NJ_MERGE_FORCE  0x0104


/************************************************/
/*              extern  宣  言                  */
/************************************************/
/**
 * extern 宣言用の定義
 */
#define NJ_EXTERN extern
/*
 * (参考) Windows DLL を作成する場合は以下の宣言
 * に変更する
 * #define NJ_EXTERN extern __declspec(dllexport)
 */


/*
 * 形態素解析API
 */
NJ_EXTERN NJ_INT16 mmx_split_word(NJ_CLASS *iwnn, NJ_CHAR  *input, NJ_UINT8 *process_len, NJ_RESULT *results);
NJ_EXTERN NJ_INT16 mmx_get_hinsi(NJ_CLASS *iwnn, NJ_RESULT *result);
NJ_EXTERN NJ_INT16 mmx_get_info(NJ_CLASS *iwnn, NJ_RESULT *target, NJ_CHAR *yomi, NJ_UINT16 yomi_size, NJ_UINT8 *stem_len, NJ_RESULT *result);
NJ_EXTERN NJ_INT16 mmx_select(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_CHAR  *yomi, NJ_UINT8 nofzk, NJ_UINT8 connect);

/*
 * 予測変換API
 */
NJ_EXTERN NJ_INT16 njx_analyze(NJ_CLASS *iwnn, NJ_CHARSET *charset, NJ_CHAR *yomi, NJ_RESULT *result, NJ_ANALYZE_OPTION *option);

/*
 * かな変換API
 */
NJ_EXTERN NJ_INT16 njx_conv(NJ_CLASS *iwnn, NJ_CHAR  *yomi, NJ_UINT8 analyze_level, NJ_UINT8 devide_pos, NJ_RESULT *results);
NJ_EXTERN NJ_INT16 njx_zenkouho(NJ_CLASS *iwnn, NJ_RESULT *target, NJ_UINT16 candidate_no, NJ_RESULT *result);
NJ_EXTERN NJ_INT16 njx_get_stroke_word(NJ_CLASS *iwnn, NJ_CHAR *yomi, NJ_RESULT *result);
NJ_EXTERN NJ_INT32 njx_fzk_conv(NJ_CLASS *iwnn, NJ_UINT32 type, NJ_WORD_INFO *clause_in, NJ_WORD_INFO *clause_out);
NJ_EXTERN NJ_INT32 njx_get_conjugation_type(NJ_CLASS *iwnn, NJ_WORD_INFO *clause);
NJ_EXTERN NJ_INT16 njx_fzk_split_word(NJ_CLASS *iwnn, NJ_RESULT *clause, NJ_FZK_BUF *fzks, NJ_FZK_PATTERN **fzk_result);
NJ_EXTERN NJ_INT16 njx_fzk_get_info(NJ_CLASS *iwnn, NJ_FZK_BUF *fzks, NJ_FZK_PATTERN *fzk_pattern, NJ_INT32 index, NJ_FZK_WORD **fzk_word);

/*
 * 辞書引きAPI
 */
NJ_EXTERN NJ_INT16 njx_get_stroke(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_CHAR  *buf, NJ_UINT16 buf_size);
NJ_EXTERN NJ_INT16 njx_get_candidate(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_CHAR  *buf, NJ_UINT16 buf_size);
NJ_EXTERN NJ_INT16 njx_search_word(NJ_CLASS *iwnn, NJ_CURSOR *cursor);
NJ_EXTERN NJ_INT16 njx_get_word(NJ_CLASS *iwnn, NJ_CURSOR *cursor, NJ_RESULT *result);
NJ_EXTERN NJ_INT16 njx_check_dic(NJ_CLASS *iwnn, NJ_UINT8 dic_type, NJ_DIC_HANDLE handle, NJ_UINT8 restore, NJ_UINT32 size);
NJ_EXTERN NJ_DIC_HANDLE njx_get_dic_handle(NJ_CLASS *iwnn, NJ_RESULT *result);
NJ_EXTERN NJ_INT16 njx_add_word(NJ_CLASS *iwnn, NJ_WORD_INFO *word, NJ_UINT8 type, NJ_UINT8 connect);
NJ_EXTERN NJ_INT16 njx_delete_word(NJ_CLASS *iwnn, NJ_RESULT *result);
NJ_EXTERN NJ_INT16 njx_create_dic(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_INT8 type, NJ_UINT32 size);
NJ_EXTERN NJ_INT32 njx_get_additional_info(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_INT8 index, NJ_CHAR *buf, NJ_UINT32 buf_size);

/*
 * 共通API
 */
NJ_EXTERN NJ_INT16 njx_set_state(NJ_CLASS *iwnn, NJ_STATE *state);
NJ_EXTERN NJ_INT16 njx_get_state(NJ_CLASS *iwnn, NJ_STATE *state);
NJ_EXTERN NJ_INT16 njx_set_option(NJ_CLASS *iwnn, NJ_OPTION *option);
NJ_EXTERN NJ_INT16 njx_select(NJ_CLASS *iwnn, NJ_RESULT *l_result, NJ_RESULT *r_result, NJ_UINT8 connect);
NJ_EXTERN NJ_INT16 njx_undo(NJ_CLASS *iwnn, NJ_UINT16 undo_count);
NJ_EXTERN NJ_INT16 njx_get_char_type(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_UINT8 *stem_type, NJ_UINT8 *fzk_type);
NJ_EXTERN NJ_INT16 njx_init(NJ_CLASS *iwnn, NJ_OPTION *option);
NJ_EXTERN NJ_INT16 njx_change_dic_type(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 direct);
NJ_EXTERN NJ_INT16 njx_get_ext_area_size(NJ_CLASS *iwnn, NJ_UINT8 dic_type, NJ_DIC_HANDLE handle, NJ_UINT32 *size);
NJ_EXTERN NJ_INT16 njx_init_ext_area(NJ_CLASS *iwnn, NJ_UINT8 dic_type, NJ_DIC_HANDLE handle, NJ_VOID *ext_area, NJ_UINT32 size);
NJ_EXTERN NJ_INT16 njx_check_ext_area(NJ_CLASS *iwnn, NJ_UINT8 dic_type, NJ_DIC_HANDLE handle, NJ_VOID *ext_area, NJ_UINT32 size, NJ_UINT8 index);
NJ_EXTERN NJ_INT16 njx_check_additional_info(NJ_CLASS *iwnn, NJ_UINT8 dic_type, NJ_DIC_HANDLE handle, NJ_VOID *add_info, NJ_UINT32 size);
NJ_EXTERN NJ_INT16 njx_merge_word_list(NJ_M_RESULT *word_list, NJ_INT32 list_max, NJ_UINT32 mode, NJ_CLASS *iwnn, NJ_RESULT *result, NJ_INT32 num);
NJ_EXTERN NJ_INT16 njx_get_word_info(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_WORD_INFO *info);
NJ_EXTERN NJ_INT16 njx_manage_learndic(NJ_CLASS *iwnn, NJ_UINT32 operation);
NJ_EXTERN NJ_INT32 njx_get_user_prof_data_size(NJ_CLASS *iwnn, NJ_DIC_SET *dicset, NJ_USER_PROF_INFO *prof_info);
NJ_EXTERN NJ_INT32 njx_export_user_prof_data(NJ_CLASS *iwnn, NJ_DIC_SET *dicset, NJ_USER_PROF_INFO *prof_info, 
                                             NJ_VOID *exp_data, NJ_INT32 size, NJ_UINT8 hindo);
NJ_EXTERN NJ_INT32 njx_import_user_prof_data(NJ_CLASS *iwnn, NJ_DIC_SET *dicset, NJ_USER_PROF_INFO *prof_info, NJ_VOID *imp_data, NJ_UINT8 hindo);

/*
 * ファイルアクセスAPI
 */
NJ_EXTERN NJ_INT32 njx_get_storage_dic_cache_size(NJ_CLASS *iwnn, NJ_FILE* filestream, NJ_UINT32 mode);
NJ_EXTERN NJ_INT32 njx_set_storage_dic_info(NJ_CLASS *iwnn, NJ_STORAGE_DIC_INFO *fdicinfo, 
                                            NJ_FILE* filestream, NJ_UINT8 *cache_area, NJ_UINT32 cache_size, NJ_UINT32 mode);

#endif /* _NJ_LIB_H_ */
