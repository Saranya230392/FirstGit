/**
 * @file
 *   変換部 内部データフォーマット定義
 *
 *   変換部で使用するデータフォーマットを定義する
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2010 All Rights Reserved.
 */

#ifndef _NJC_H_
#define _NJC_H_

/************************************************/
/*        パラメータ 宣 言                      */
/************************************************/
#ifndef NJC_MAX_WORK_PHRASE
#define NJC_MAX_WORK_PHRASE 2
#endif /* NJC_MAX_WORK_PHRASE */

#ifndef NJC_MAX_WORK_CANDIDATE
#define NJC_MAX_WORK_CANDIDATE  100
#endif /* NJC_MAX_WORK_CANDIDATE */

#ifndef NJC_MAX_FZK_LEN
#define NJC_MAX_FZK_LEN NJ_MAX_FZK_LEN
#endif /* NJC_MAX_FZK_LEN */

#ifndef NJC_MAX_FZK_CANDIDATE
#define NJC_MAX_FZK_CANDIDATE   NJ_MAX_FZK_CANDIDATE
#endif /* NJC_MAX_FZK_CANDIDATE */

#ifndef NJC_MAX_STEM_CANDIDATE
#define NJC_MAX_STEM_CANDIDATE   NJ_MAX_STEM_CANDIDATE
#endif /* NJC_MAX_STEM_CANDIDATE */

#ifndef NJC_CUTOFF_LENGTH_THRESHOLD
#define NJC_CUTOFF_LENGTH_THRESHOLD 3
#endif /* NJC_CUTOFF_LENGTH_THRESHOLD */

#ifndef NJC_MAX_GET_RESULTS
#define NJC_MAX_GET_RESULTS NJ_MAX_GET_RESULTS
#endif /* NJC_MAX_GET_RESULTS */

#ifndef NJC_PHRASE_COST
#define NJC_PHRASE_COST  (-1000)
#endif /* NJC_PHRASE_COST */

/*
 * njc_modeの設定値定義
 */
#define NJC_MODE_NOT_CONVERTED 0        /**< njc_modeの設定値:未変換中 */
#define NJC_MODE_CONVERTED     1        /**< njc_modeの設定値:変換中 */
#define NJC_MODE_ZENKOUHO      2        /**< njc_modeの設定値:全候補中 */
#define NJC_MODE_MORPHO        3        /**< njc_modeの設定値:分かち書き中 */


/************************************************/
/*        構造体 宣 言                          */
/************************************************/
/**
 * [変換] 連文節変換処理結果保持領域
 */
typedef struct {
    NJ_CHAR     *yomi;              /**< 解析対象読み文字列格納位置 */
    NJ_UINT16   reprLen;            /**< 解析対象表記文字配列数     */
    NJ_RESULT   *phrases;           /**< 連文節変換結果             */
    NJ_UINT16   phrMax;             /**< phrase 格納最大数          */
    NJ_UINT16   phrNum;             /**< phrase 格納数              */

    NJ_UINT32   flag_latest;        /**< 直前に格納された文節に付与されているフラグ（NJC_WORK_SENTENCE::flags[] の内容をそのまま転記）*/
} NJC_SENTENCE;

/**
 * [変換] 評価対象文節群 付加情報 保持領域
 */
typedef struct {
    NJ_UINT16       flag;           /**< 評価対象文節群 付加情報 フラグ                            */
#define NJC_WS_FLAG_DEFAULT             0x0000
#define NJC_WS_FLAG_FUNCTION_MASK       0xc000
#define NJC_WS_FLAG_ASSUME_TWO_PHRASES  0x8000   /**< NJC_PHRASE_INFO::flags[] 文節分割されている（該当文節＋後続文節＝実際の文節）*/
#define NJC_WS_FLAG_DIVIDE_FOR_WORD     0x4000   /**< NJC_PHRASE_INFO::flags[] 文節分割の理由が単語に拠れば(1)、品詞に拠れば(0)    */
#define NJC_WS_FLAG_RPOS_MASK           0x01ff   /**< NJC_PHRASE_INFO::flags[] 文節分割されている場合、前の文節の右品詞を格納      */
    NJ_DIC_HANDLE   handle;         /**< 評価対象文節群 付加情報 接続対象付属語辞書ハンドル        */
    NJ_UINT32       id;             /**< 評価対象文節群 付加情報 接続対象付属語ID または左品詞番号 */
} NJC_PHRASE_INFO;

/**
 * [変換] 作業用文情報保持領域
 */
typedef struct {
    NJ_UINT16       yomiLen;                      /**< 解析対象読み文字配列数                           */
    NJ_UINT16       reprLen;                      /**< 解析対象表記文字配列数                           */
    NJ_INT32        score;                        /**< 評価値                                           */

    NJ_RESULT       phrases[NJC_MAX_WORK_PHRASE]; /**< 評価対象文節群 情報                              */
    NJC_PHRASE_INFO info[NJC_MAX_WORK_PHRASE];    /**< 評価対象文節群 付加情報                          */

    NJ_UINT8        phrNum;                       /**< phrase 格納数 [0-NJC_MAX_WORK_PHRASE]            */
    NJ_UINT8        vphrNum;                      /**< 仮想文節追加数                                   */
    NJ_UINT8        invalid;                      /**< 0:解析対象,1:解析対象外
                                                   *   @attention 最上位ビットは conv_fzkProcess で破壊
                                                   */
    NJ_UINT8        *connect;                     /**< 接続情報                                         */
    NJ_UINT8        *connect_ext;                 /**< 拡張接続情報                                     */
} NJC_WORK_SENTENCE;


/**
 * [変換] 品詞情報
 */
typedef struct {
    NJ_UINT16       f_v2;       /**< V2 前品詞番号   */
    NJ_UINT16       f_v1;       /**< V1 前品詞番号   */
    NJ_UINT16       f_v3;       /**< V3 前品詞番号   */
    NJ_UINT16       f_giji;     /**< 擬似 前品詞番号 */
    NJ_UINT16       r_giji;     /**< 擬似 後品詞番号 */
    NJ_UINT16       r_ngiji;    /**< 数字 後品詞番号 */
    NJ_UINT16       r_bunto;    /**< 文頭 後品詞番号 */
    NJ_UINT16       fcnt;       /**< 登録前品詞数    */
    NJ_UINT16       bcnt;       /**< 登録後品詞数    */
} NJC_HINSI_INFO;

/**
 * [変換] 連文節変換処理ワーク
 */
typedef struct {
    /** 解析文格納バッファ */
    NJC_WORK_SENTENCE   sentence[NJC_MAX_WORK_CANDIDATE + 1];
    NJ_INT16            sentNum;            /**< sentence 格納数                         */
    NJ_UINT16           restYomiLen;        /**< 未解析読み文字配列数                    */
    NJ_CHAR             *pyomi;             /**< yomi 解析先頭アドレス                   */
    NJ_UINT8            maxRltLen;          /**< 最大変換結果出力文字配列長              */
    NJ_DIC_SET          jds;                /**< 自立語解析用辞書セット                  */
    NJ_DIC_SET          fds;                /**< 付属語解析用辞書セット                  */
    NJC_HINSI_INFO      hinsi;              /**< 品詞情報                                */
    /** 解析条件 */
    struct {
        NJ_DIC_SET      *ds;                /**< 解析用辞書セット                        */
        NJ_UINT8        nogiji;             /**< 擬似候補設定
                                             *
                                             * -1:擬似候補が発生した場合処理中断
                                             * - 0:処理中断しない
                                             */
        NJ_UINT8        top_conv;           /**< 先頭文節解析設定
                                             *
                                             * -1:先頭文節の解析が完了した場合、処理中断
                                             * 0:処理中断しない
                                             */
        NJ_UINT8        level;              /**< 解析文節数                              */
    } cond;
    NJ_UINT16           flags;              /**< 形態素解析処理制御フラグ                */
} NJC_CONV;

/**
 * [全候補] 辞書検索カーソルリスト
 */
typedef struct {
    NJ_CURSOR   *cur;                     /**< 辞書検索カーソル */
    NJ_RESULT   rlt[NJD_MAX_CONNECT_CNT]; /**< 辞書引き結果 */
    NJ_UINT8    rltcnt;                   /**< 辞書引き結果個数 */
    NJ_UINT8    status;                   /**< 0:検索語なし、1:あり */
} NJC_CURSOR_LIST;

/**
 * [全候補] 付属語処理結果
 */
typedef struct {
    NJ_UINT16  info1;   /**< 上位9bit:前品詞番号, 下位7bit:読み文字配列要素数 */
    NJ_UINT16  info2;   /**< 上位9bit:後品詞番号, 下位7bit:候補文字配列要素数 */
    NJ_HINDO   hindo;   /**< 頻度値 */
} NJC_FZK_RESULT;

/**
 * 自立語解析用ワーク領域・最終解析結果格納
 */
typedef struct {
    NJC_CURSOR_LIST list[NJC_MAX_STEM_CANDIDATE]; /**< 辞書検索カーソルリスト */
    NJ_UINT16   listNum;                          /**< list登録数 */
} NJC_CANDIDATE_WORK;

/**
 * [全候補] 全候補処理結果保持領域
 */
typedef struct {
    NJ_CHAR             *yomi;                               /**< 読み文字列                    */
    NJC_FZK_RESULT      fzk_phrs[NJC_MAX_FZK_CANDIDATE + 1]; /**< 付属語解析用ワーク領域        */
    NJ_UINT16           fzkPhrNum;                           /**< 付属語解析用ワーク領域 登録数 */
    NJC_CANDIDATE_WORK  *jwork;                              /**< 候補作成ワーク領域            */
    NJ_RESULT           phrases[NJ_MAX_CANDIDATE + 1];       /**< 解析結果バッファ              */
    NJ_UINT16           yomiLen;                             /**< 解析読み文字配列数            */
    NJ_INT16            offset[NJ_MAX_CANDIDATE + 1];        /**< 同表記バッファへの格納位置    */
    NJ_UINT16           phrNum;                              /**< 解析結果数                    */
    NJ_DIC_SET          jds;                                 /**< 自立語解析用辞書セット        */
    NJ_DIC_SET          jds2;                                /**< 自立語解析用学習辞書セット    */
    NJ_DIC_SET          fds;                                 /**< 付属語解析用辞書セット        */
    NJC_HINSI_INFO          hinsi;                               /**< 品詞情報                      */
    /** 解析条件 */
    struct {
        NJ_DIC_SET      *ds;                                 /**< 解析用辞書セット */
        NJ_RESULT       *target;                             /**< 全候補リスト先頭追加候補 */
        NJ_UINT8        top_set;                             /**< 1: targetを全候補リスト先頭に追加,
                                                              *   0: 追加しない
                                                              */
        NJ_UINT8        mode;                                /**< 継続実行用検索カーソル
                                                              *
                                                              * - 0: 学習辞書検索(準備)
                                                              * - 1: 学習辞書検索
                                                              * - 2: その他辞書検索(準備)
                                                              * - 3: その他辞書検索
                                                              * - 4: 疑似候補検索
                                                              * - 5: 完了（低頻度付属語付加語出力中）
                                                              */
        NJ_UINT16       low_prio_offset;                    /**< 低頻度付属語溜まりオフセット */
    } cond;
} NJC_CANDIDATE;


/**********************************************************************/

#endif /* _NJC_H_ */
