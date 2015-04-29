/**
 * @file
 *  [拡張] 数詞付き数字変換擬似辞書 (UTF16/SJIS版)
 *
 * 数詞付き数字変換候補の作成を行う辞書。
 *
 * @author
 * (C) OMRON SOFTWARE Co., Ltd. 2009-2011 All Rights Reserved.
 */
#include "ex_nmscgiji.h"
#include "nj_ext.h"
#include "nj_dic.h"
#include "njd.h"
#include "nj_err.h"
#ifdef NJ_OPT_UTF16
#include "ex_nmgtabU.h"
#else /* NJ_OPT_UTF16 */
#include "ex_nmgtab.h"
#endif /* NJ_OPT_UTF16 */

/************************************************/
/*         define  宣  言                       */
/************************************************/
/*
 * 文字種ごとの文字長定義
 */
#ifdef NJ_OPT_UTF16
/** 半角文字長 (UTF16) */
#define NJG_NMSC_HAN_MOJI_LEN  1
/** 全角文字長 (UTF16) */
#define NJG_NMSC_ZEN_MOJI_LEN  1

#else /* NJ_OPT_UTF16 */
/** 半角文字長 (SJIS) */
#define NJG_NMSC_HAN_MOJI_LEN  1
/** 全角文字長 (SJIS) */
#define NJG_NMSC_ZEN_MOJI_LEN  2
#endif /* NJ_OPT_UTF16 */

/*
 * 作成候補数字タイプ定義
 *
 * @note 値が0の数字タイプから順に数字タイプ数分候補が作成される。
 */
/** 最大数字タイプ番号 */
#define NJG_NMSC_SUUJI_MAX       2
/** 数字タイプ 漢字 */
#define NJG_NMSC_SUUJI_KAN       0
/** 数字タイプ 全角 */
#define NJG_NMSC_SUUJI_ZEN       1
/** 数字タイプ 半角 */
#define NJG_NMSC_SUUJI_HAN       2

/** 数詞付き数字変換辞書頻度段階 */
#define NMSC_DIC_FREQ_DIV 63

/**
 * 検索位置最大数定義
 *
 * @note 1候補に対して数字部分が数字タイプ数分作成する為、テーブル要素数*数字タイプ数とする。
 */
#define NJG_NMSC_CURRENT_MAX (NJG_NM_SUYM_IDX_MAX * (NJG_NMSC_SUUJI_MAX + 1))

/* 内部用マクロ定義 */
/**
 * 検索位置から作成する数字タイプを取得する
 *
 * @param[in]      x      検索位置
 * @retval                数字タイプ
 */
#define NJG_NMSC_GET_SUUJI_TYPE(x) \
    (NJ_UINT8)(((x) < NJG_NM_SUYM_IDX_MAX) ? 0 : (((x) < (NJG_NM_SUYM_IDX_MAX * 2)) ? 1 : 2))
/**
 * 次の数字タイプを取得する
 *
 * @param[in]      x      数字タイプ
 * @retval                次の数字タイプ
 */
#define NJG_NMSC_GET_NEXT_SUUJI_TYPE(x) (NJ_UINT8)(((x) >= NJG_NMSC_SUUJI_MAX) ? 0 : ((x) + 1))
/**
 * 数字部分タイプから数字文字長を取得。
 *
 * @param[in]      x  数字部分タイプ
 * @return 数字文字長
 */
#define NJG_NMSC_GET_SUUJI_LEN(x) (NJ_UINT16)(((x) == NJG_NMSC_SUUJI_ZEN) ? NJG_NMSC_ZEN_MOJI_LEN : NJG_NMSC_HAN_MOJI_LEN)

/************************************************/
/*              マ ク ロ 宣 言                  */
/************************************************/

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
#define NJG_NMSC_CONV_TO_WCHAR(x)                                            \
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
#define NJG_NMSC_IS_WCHAR(x)  1
#else /* NJ_OPT_UTF16 */
/** SJIS版 */
#define NJG_NMSC_IS_WCHAR(x)                                                 \
    (((((x) >= 0x81) && ((x) <= 0x9f)) || (((x) >= 0xe0) && ((x) <= 0xfc))) ? 1 : 0)
#endif/* NJ_OPT_UTF16 */

/**
 * NJ_CHARにテーブルに定義された16bit文字をコピーする。
 *
 * @note Big Endianで2byte格納する。
 *
 * @param[in] to    コピー先 (NJ_CHAR*)
 * @param[in] from  コピー元 (NJ_UINT16/NJ_INT16)
 */
#define NJG_NMSC_COPY_INT16_TO_CHAR(to, from)                                \
    { ((NJ_UINT8*)(to))[0] = (NJ_UINT8)(((from) >> 8) & 0x00ff);        \
        ((NJ_UINT8*)(to))[1] = (NJ_UINT8)((from) & 0x00ff); }

/**
 * 検索位置上位１バイトから区切り位置を取得する
 *
 * @param[in]      x : current
 *
 * @return         区切り位置
 */
#define NJG_NMSC_GET_DIV_FROM_CUR(x) ((NJ_UINT16)(((x) >> 24) & 0xff))

/**
 * 検索位置の上位１バイトへ変換成功分割位置をセットする
 *
 * @param[out]     x : current
 * @param[in]    div : 分割位置
 *
 * @return             なし
 */
#define NJG_NMSC_SET_DIV(x, div) (*(x) = (NJ_UINT32)((*(x) & 0x00ffffff) | ((div) << 24)))

/**
 * 検索位置の下位３バイトから検索位置を取得する
 *
 * @param[in]      x : current
 *
 * @return         検索位置
 */
#define NJG_NMSC_GET_POS_FROM_CUR(x) (NJ_UINT32)((x) & 0x00ffffff)
/**
 * 区切り位置毎頻度値をセットする
 *
 * @param[in]      x   : 頻度値格納領域
 *
 * @return         区切り位置毎の頻度値
 */
#define NJG_NMSC_SET_FREQ(x)                                            \
    (NJ_UINT32)((((NJ_UINT32)(x)[0]) << 24) | (((NJ_UINT32)(x)[1]) << 16) | (((NJ_UINT32)(x)[2]) << 8) | ((NJ_UINT32)(x)[3]))
/**
 * 区切り位置毎頻度値を取得する
 *
 * @param[out]    to  : コピー先
 * @param[in]   from1 : コピー元(１)
 * @param[in]   from2 : コピー元(２)
 *
 * @return             なし
 */
#define NJG_NMSC_GET_FREQ(to, from1, from2)                             \
    { ((NJ_UINT8*)(to))[0] = ((NJ_UINT8)(0x000000ff & (NJ_UINT32)(from1 >> 24)));   \
        ((NJ_UINT8*)(to))[1] = ((NJ_UINT8)(0x000000ff & (NJ_UINT32)(from1 >> 16))); \
        ((NJ_UINT8*)(to))[2] = ((NJ_UINT8)(0x000000ff & (NJ_UINT32)(from1 >> 8)));  \
        ((NJ_UINT8*)(to))[3] = ((NJ_UINT8)(0x000000ff & (NJ_UINT32)(from1)));       \
        ((NJ_UINT8*)(to))[4] = ((NJ_UINT8)(0x000000ff & (NJ_UINT32)(from2 >> 24))); \
        ((NJ_UINT8*)(to))[5] = ((NJ_UINT8)(0x000000ff & (NJ_UINT32)(from2 >> 16))); \
        ((NJ_UINT8*)(to))[6] = ((NJ_UINT8)(0x000000ff & (NJ_UINT32)(from2 >> 8)));  \
        ((NJ_UINT8*)(to))[7] = ((NJ_UINT8)(0x000000ff & (NJ_UINT32)(from2))); }

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
 * 内部関数 プロトタイプ宣言
 ***********************************************************************/
static NJ_INT16 njg_nmsc_get_result(NJ_UINT16 request, NJ_DIC_HANDLE rule,
                                    NJ_CHAR *yomi, NJ_UINT16 len, NJ_UINT32 *current,
                                    NJ_RESULT *giji, NJ_UINT8 *max_current, NJ_UINT16 *current_pos);
static NJ_INT16 njg_nmsc_get_giji(NJ_UINT16 request, NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                  NJ_CHAR *yomi, NJ_UINT16 len, NJ_UINT32 *current,
                                   NJ_RESULT *p, NJ_UINT8 *max_current, NJ_UINT16 *current_pos);
static NJ_INT16 njg_nmsc_chk_giji(NJ_UINT16 request, NJ_CHAR *pY, NJ_UINT16 len,
                                  NJ_UINT32 *current, NJ_UINT8 *max_current, NJ_UINT16 *current_pos);
static NJ_INT16 njg_nmsc_cmp_yomi(NJ_CHAR *yomi, NJ_UINT16 len, const NJG_NUM_CONN_TBL *tbl);
static NJ_INT16 njg_nmsc_get_candidate(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size);
static NJ_INT16 njg_nmsc_get_candidate_string(NJ_WORD *word, NJ_CHAR *candidate,
                                              const NJG_NUM_CONN_TBL *ctbl, NJ_UINT16 sutype);
static NJ_INT16 njg_nmsc_get_ksuuji_ketapos(NJ_CHAR *yomi, NJ_UINT8 *yomi_len, NJ_UINT8 *tblidx);
static NJ_INT16 njg_nmsc_get_ksuuji_index_from_yomi(PART_OF_NJ_ENV *tmp, NJ_CHAR *yomi, NJ_UINT16 ylen,
                                                    NJ_UINT16 *dlen, NJ_UINT8 *yomi_cnt, NJ_UINT8  *yomi_type);
static NJ_INT16 njg_nmsc_check_ksuuji_array(NJ_CHAR *suuji_strings);
static NJ_INT16 njg_nmsc_convert_ksuuji_data(NJ_UINT16 *kanpt, NJ_INT16 kan_len,
                                             NJ_CHAR *suuji_strings, NJ_INT16 suuji_size);
static NJ_INT16 njg_nmsc_get_ksuuji_data_from_index(PART_OF_NJ_ENV *iwnn);
static NJ_INT16 njg_nmsc_cmp_suujiyomi(NJ_CHAR *yomi, NJG_SUUJIYOMITBL *tbl);
static NJ_INT16 njg_nmsc_get_max_hindo(NJ_UINT8 *max_hindo, NJ_CHAR *pY, NJ_UINT16 len,
                                        NJ_UINT8 *hindo, NJ_UINT16 *pos, NJ_UINT16 *dlen);
static NJ_UINT16 njg_nmsc_search_candidate(NJ_CHAR *pYomi, NJ_UINT16 tlen, NJ_UINT32 *current);
static NJ_UINT16 njg_nmsc_get_table_index(NJ_UINT32 current);

/***********************************************************************/

/**
 * 数詞付き数字変換擬似辞書 辞書インタフェース
 *
 * @param[in,out] iwnn      iWnn内部情報(通常は参照のみ)
 * @param[in]     request   iWnnからの処理要求
 *                          - NJG_OP_SEARCH：初回検索
 *                          - NJG_OP_SEARCH_NEXT：次候補検索
 *                          - NJG_OP_GET_WORD_INFO：単語情報取得
 *                          - NJG_OP_GET_STROKE：読み文字列取得
 *                          - NJG_OP_GET_STRING：候補文字列取得
 * @param[in,out] message   iWnn←→擬似辞書間でやり取りする情報
 *
 * @retval >=0 [NJG_OP_SEARCHの場合]<br>
 *                1:候補あり、0:候補なし<br>
 *             [NJG_OP_SEARCH_NEXTの場合]<br>
 *                1:候補あり、0:候補なし<br>
 *             [NJG_OP_GET_WORD_INFOの場合]<br>
 *                0:正常終了<br>
 *             [NJG_OP_GET_STROKEの場合]<br>
 *                読み文字列長<br>
 *             [NJG_OP_GET_STRINGの場合]<br>
 *                表記文字列長<br>
 *             [NJG_OP_GET_ADDITIONALの場合]<br>
 *                付加情報文字列長
 * @retval <0  エラー
 */
NJ_EXTERN NJ_INT16 njex_nmscgiji_dic(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE *message) {

    NJ_INT16  ret;
    NJ_UINT32 current = 0;
    NJ_RESULT rlt;
    NJ_UINT16 len;
    NJ_HINDO  hindo;
    NJ_UINT16 tblidx;
    NJ_UINT8  max_hindo[NJG_NM_HINDO_TBL_MAX];
    NJ_UINT16 current_pos;
    NJ_UINT8  i;

    switch (request) {
    case NJG_OP_SEARCH:
        if ((message->condition->operation != NJ_CUR_OP_COMP)
            || (message->condition->mode != NJ_CUR_MODE_FREQ)) {
            /* 正引き完全一致頻度順以外は非対応 */
            message->location->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        current_pos = 0;
        for (i = 0; i < NJG_NM_HINDO_TBL_MAX; i++) {
            max_hindo[i] = 0x00;
        }

        /* 候補を作ってみる */
        ret = njg_nmsc_get_result(request, message->dicset->rHandle[0],
                                 message->condition->yomi, message->condition->ylen,
                                 &current, &rlt, max_hindo, &current_pos);
        if (ret == 1) {
            /* 候補作成可能 */
            message->location->loct.current = current;
            message->location->loct.top = NJG_NMSC_SET_FREQ(&max_hindo[0]);
            message->location->loct.bottom = NJG_NMSC_SET_FREQ(&max_hindo[4]);
            message->location->loct.current_cache = current_pos;
            message->location->loct.current_info = 0x10;
            /* 頻度を設定する */
            tblidx = njg_nmsc_get_table_index(current);
            hindo = CALCULATE_HINDO(num_conn_tbl[tblidx].conv_hindo,
                                    message->location->dic_freq.base, message->location->dic_freq.high,
                                    NMSC_DIC_FREQ_DIV);
            message->location->cache_freq = hindo;
            message->location->loct.status = NJ_ST_SEARCH_READY;
            return 1;
        } else if (ret < 0) {
            /* エラー */
            message->location->loct.status = NJ_ST_SEARCH_END;
            return ret;
        } else {
            /* 候補作成不可 */
            message->location->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }

    case NJG_OP_SEARCH_NEXT:
        /* 次に作成可能な候補を求める */

        current = message->location->loct.current;
        NJG_NMSC_GET_FREQ(max_hindo, message->location->loct.top, message->location->loct.bottom);
        current_pos = message->location->loct.current_cache;
        /* 候補を作ってみる */
        ret = njg_nmsc_get_result(request, message->dicset->rHandle[0],
                                 message->condition->yomi, message->condition->ylen,
                                 &current, &rlt, max_hindo, &current_pos);
        if (ret > 0) {
            /* 候補作成可能 */
            message->location->loct.current = current;
            message->location->loct.top = NJG_NMSC_SET_FREQ(&max_hindo[0]);
            message->location->loct.bottom = NJG_NMSC_SET_FREQ(&max_hindo[4]);
            message->location->loct.current_cache = current_pos;
            message->location->loct.current_info = 0x10;
            /* 頻度を設定する */
            tblidx = njg_nmsc_get_table_index(current);
            hindo = CALCULATE_HINDO(num_conn_tbl[tblidx].conv_hindo,
                                    message->location->dic_freq.base, message->location->dic_freq.high,
                                    NMSC_DIC_FREQ_DIV);
            message->location->cache_freq = hindo;
            message->location->loct.status = NJ_ST_SEARCH_READY;
            return 1;
        } else if (ret < 0) {
            /* エラー */
            message->location->loct.status = NJ_ST_SEARCH_END;
            return ret;
        } else {
            /* 候補作成不可 */
            message->location->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }

    case NJG_OP_GET_WORD_INFO:
        /* 候補の検索位置を取得して候補を作成する */
        if (NJG_NMSC_GET_POS_FROM_CUR(message->location->loct.current) > NJG_NMSC_CURRENT_MAX) {
            /* 念のため候補の範囲をチェック。ココを通ることはない */
            return -1;
        }
        NJG_NMSC_GET_FREQ(max_hindo, message->location->loct.top, message->location->loct.bottom);
        current_pos = message->location->loct.current_cache;

        /* 候補を作成する */
        ret = njg_nmsc_get_result(request, message->dicset->rHandle[0],
                                 message->word->yomi, NJ_GET_YLEN_FROM_STEM(message->word),
                                 &message->location->loct.current, &rlt, max_hindo, &current_pos);
        if (ret > 0) {
            /* 候補作成可能なはず */
            *(message->word) = rlt.word;
            /* 頻度を設定する */
            tblidx = njg_nmsc_get_table_index(message->location->loct.current);
            hindo = CALCULATE_HINDO(num_conn_tbl[tblidx].conv_hindo,
                                    message->location->dic_freq.base, message->location->dic_freq.high,
                                    NMSC_DIC_FREQ_DIV);
            NJ_SET_FREQ_TO_STEM(message->word, hindo);
            message->word->stem.loc = message->location->loct;
            return 0;
        } else {
            /* ここに来ることはないが、念のため */
            return -1;
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
        len = NJ_GET_KLEN_FROM_STEM(message->word);
        if (message->string_size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
            /* 格納領域に収まらない場合はエラー */
            return -1;
        }
        ret = njg_nmsc_get_candidate(message->word, message->string, message->string_size);
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
    return -1; /* エラー */
}

/**
 * 数詞付き数字変換候補を作成する。
 *
 * @param[in]     request     iWnnからの処理要求
 * @param[in]     rule        ルール辞書ハンドル
 * @param[in]     yomi        読み文字列
 * @param[in]     len         yomi 文字長
 * @param[in,out] current     検索位置
 * @param[out]    giji        候補格納領域
 * @param[in,out] max_hindo   頻度格納領域
 * @param[in,out] current_pos 区切り位置
 *
 * @retval 1  候補を作成できた
 * @retval 0  候補を作成できない
 * @retval <0 エラー
 */
static NJ_INT16 njg_nmsc_get_result(NJ_UINT16 request, NJ_DIC_HANDLE rule,
                                   NJ_CHAR *yomi, NJ_UINT16 len, NJ_UINT32 *current,
                                   NJ_RESULT *giji, NJ_UINT8 *max_hindo, NJ_UINT16 *current_pos) {

    NJ_UINT16 fpos, bpos;
    NJ_INT16 ret;
    NJ_INT16 i;

    /*
     * 引数チェック
     */
    if (rule == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_GIJI_RESULT, NJ_ERR_NO_RULEDIC);
    }
    if ((yomi == NULL) || (len == 0)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_GIJI_RESULT, NJ_ERR_PARAM_YOMI_NULL);
    }
    if (giji == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_GIJI_RESULT, NJ_ERR_PARAM_RESULT_NULL); /*NCH*/
    }

    /*
     * 候補の品詞番号をルール辞書から取得する
     */
    fpos = njd_r_get_hinsi(rule, NJ_HINSI_GIJI_F);  /* 前品詞：擬似 の品詞番号取得 */
    bpos = njd_r_get_hinsi(rule, NJ_HINSI_GIJI_B);  /* 後品詞：擬似 の品詞番号取得 */

    if ((fpos == 0) || (bpos == 0)) {
        /* 擬似 品詞番号取得失敗 */
        return 0; /*NCH_FB*/
    }

    /* 候補格納領域を初期化 */
    giji->operation_id = 0;
    giji->word.yomi                  = NULL;
    giji->word.stem.info1            = 0;
    giji->word.stem.info2            = 0;
    giji->word.stem.hindo            = 0;
    giji->word.stem.type             = NJ_TYPE_UNDEFINE;
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

    /* 数詞付き数字変換候補取得処理 */
    ret = njg_nmsc_get_giji(request, fpos, bpos, yomi, len, current, giji, max_hindo, current_pos);

    return ret;
}

/**
 * 数詞付き数字変換候補取得処理
 *
 * @param[in]     request     iWnnからの処理要求
 * @param[in]     gfpos       擬似品詞(擬似) 前品詞番号
 * @param[in]     gbpos       擬似品詞(擬似) 後品詞番号
 * @param[in]     yomi        候補 読み
 * @param[in]     len         読み文字長
 * @param[in,out] current     検索位置
 * @param[out]    p           候補格納バッファ
 * @param[in,out] max_hindo   頻度格納領域
 * @param[in,out] current_pos 区切り位置
 *
 * @retval 1  候補を作成できた
 * @retval 0  候補を作成できない
 */
static NJ_INT16 njg_nmsc_get_giji(NJ_UINT16 request, NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                 NJ_CHAR *yomi, NJ_UINT16 len, NJ_UINT32 *current,
                                 NJ_RESULT *p, NJ_UINT8 *max_hindo, NJ_UINT16 *current_pos) {

    NJ_UINT16 len0;

    /*
     * 候補が作成できるか、チェックする。
     */
    len0 = njg_nmsc_chk_giji(request, yomi, len, current, max_hindo, current_pos);

    if (len0 > 0) {
        /* 候補が作成できた */
        p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
        p->word.yomi = yomi;
        NJ_SET_YLEN_TO_STEM(&p->word, len);
        NJ_SET_KLEN_TO_STEM(&p->word, len0);
        NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
        NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
        NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_NONE);
        return 1;
    }
    return 0;
}

/**
 * 数詞付き数字変換候補作成チェック処理
 *
 * @param[in]     request     iWnnからの処理要求
 * @param[in]     pY          読み文字列
 * @param[in]     len         読み文字列の文字長
 * @param[in,out] current     検索位置
 * @param[in,out] max_hindo   頻度格納領域
 * @param[in,out] current_pos 区切り位置
 *
 * @retval  >0  作成できる候補の文字列長
 * @retval   0  作成できない
 */
static NJ_INT16 njg_nmsc_chk_giji(NJ_UINT16 request, NJ_CHAR *pY, NJ_UINT16 len,
                                 NJ_UINT32 *current, NJ_UINT8 *max_hindo, NJ_UINT16 *current_pos) {

    PART_OF_NJ_ENV tmp;
    NJ_UINT16 len0 = 0;
    NJ_INT16  i, j;
    NJ_UINT16 slen = 0;
    NJ_UINT16 pos, pos_tmp, pos_max = 0;
    NJ_UINT16 tlen;
    NJ_UINT16 ylen = 0;
    NJ_UINT16 dlen = 0;
    NJ_UINT8  sutype;
    NJ_UINT8  yomi_type = 0;
    NJ_UINT8  yomi_cnt = 0;
    NJ_UINT32 work;
    NJ_UINT16 tblidx = 0;
    NJ_UINT32 num = 0;
    NJ_UINT32 current_tmp, current_max = 0;
    NJ_UINT8  hindo_max = 0;
    NJ_UINT8  hindo = 0;
    NJ_CHAR   *pYomi;
    NJ_CHAR   yomibuf[NJ_MAX_LEN + NJ_TERM_LEN];

    current_tmp = *current;
    pos_tmp = *current_pos;
    if (request == NJG_OP_SEARCH) {
        /* 初回検索の場合 */
        sutype = 0;
        dlen = len;
        /* 全区切り位置の最高頻度格納処理 */
        while (dlen > 0) {
            len0 = 0;
            num = 0;
            yomi_type = 0;
            ylen = dlen;

            /* 読み文字列から数字インデックスを作成 */
            slen = njg_nmsc_get_ksuuji_index_from_yomi(&tmp, pY, ylen, &dlen, &yomi_cnt, &yomi_type);
            if (slen <= 0) {
                continue;/* 作成できない */
            }

            /* 接続単位読み文字数算出 */
            nj_strncpy(yomibuf, pY, len);
            yomibuf[len] = NJ_CHAR_NUL;
            pos = nj_charlen(yomibuf + slen);
            if (pos > NJG_NM_HINDO_TBL_MAX) {
                /* 接続単位読み文字数が８文字を超えた */
                break;
            }

            /* 「し」「しち」「く」が２回以上出現したか */
            if (yomi_cnt > 1) {
                continue;
            }

            /* 数字インデックスから数字データを作成 */
            if (njg_nmsc_get_ksuuji_data_from_index(&tmp) < 0) {
                continue;/* 作成できない */
            }

            if (tmp.gw_len > NJG_NM_LIMIT_MAX_LEN) {
                /* 候補作成不可 */
                continue;
            }

            if ((yomi_cnt > 0) &&
                ((yomi_type == 0) || (yomi_type >=  NJG_NM_YOMI_TYPE_IXTU))) {
                /* １桁目以外で「し」「しち」「く」が出現 */
                continue;
            }

            /* 変換後データ長算出 */
            /* 数字インデックスの数と漢数字(位)は一致する。*/
            len0 = (NJ_UINT16) (tmp.kan_len * NJG_NMSC_ZEN_MOJI_LEN);

            /* 数字部分の数値を取得 */
            for (i = 0; i < tmp.gw_len; i++) {
                work = 1;
                for (j = 0; j < (NJ_INT16)(tmp.gw_len - i - 1); j++) {
                    work *= 10;
                }
                num += (NJ_UINT32)(tmp.gijiwork[i] * work);
            }

            /* 接続単位読み文字のチェックを行う */
            tlen = len - slen;
            pYomi = pY + slen;
            if ((slen > 0) && (tlen > 0)) {
                /* 数字読み文字と接続単位読み文字に分解できた */

                /* 接続単位読み文字とテーブルの読みが一致する候補の先頭位置を２分検索する */
                if (njg_nmsc_search_candidate(pYomi, tlen, &current_tmp) == 1) {
                    /* 候補の範囲チェックを行う */
                    while (current_tmp < NJG_NM_SUYM_IDX_MAX) {
                        tblidx = njg_nmsc_get_table_index(current_tmp);

                        /* 数字部分１桁目チェック */
                        if ((yomi_type == 0) ||
                            ((yomi_type > 0) && (NJG_NM_IS_CHK_YOMI_CONV(yomi_type, num_conn_tbl[tblidx].info2) > 0))) {
                            /* 範囲チェックOKなら処理結果を作成 */
                            if ((NJG_NM_IS_CHK_CONV(num_conn_tbl[tblidx].info) == 0) ||
                                ((NJG_NM_IS_CHK_CONV(num_conn_tbl[tblidx].info) == NJG_NM_CHK_CONV_MASK) && (NJG_NM_IS_RANGE(num, tblidx) == 1))) {
                                /* 候補作成可 */
                                NJG_NMSC_SET_DIV(&current_tmp, slen);
                                break;
                            }
                        }
                        if (NJG_NMSC_GET_POS_FROM_CUR(current_tmp) >= (NJG_NM_SUYM_IDX_MAX - 1)) {
                            /* 最後まで到達した為、候補作成不可とする */
                            current_tmp = 0;
                            break; /*NCH_FB*/
                        }
                        current_tmp++;
                        if (suym_idx_tbl[current_tmp] == NJG_NM_DUMY_IDX_NUM) {
                            /* ダミー候補が出現した */
                            current_tmp = 0;
                            break;
                        }
                    }
                    /* 候補作成可 */
                    if (current_tmp != 0) {
                        /* 頻度格納処理 */
                        hindo = (NJ_UINT8)num_conn_tbl[suym_idx_tbl[NJG_NMSC_GET_POS_FROM_CUR(current_tmp)]].conv_hindo + 1;
                        max_hindo[pos - 1] = hindo;
                        if (hindo_max < hindo) {
                            /* 候補作成を行う検索位置の更新 */
                            hindo_max = hindo;
                            pos_max = pos;
                            current_max = current_tmp;
                        }
                    }
                }
            }
        }
        if (hindo_max == 0) {
            /* 全区切り位置で候補作成不可 */
            return 0;
        }
        /* 頻度値更新 */
        pos = pos_max;
        current_tmp = current_max;
        if (suym_idx_tbl[NJG_NMSC_GET_POS_FROM_CUR(current_tmp) + 1] < NJG_NM_DUMY_IDX_NUM) {
            max_hindo[pos - 1] = (NJ_UINT8)num_conn_tbl[suym_idx_tbl[NJG_NMSC_GET_POS_FROM_CUR(current_tmp) + 1]].conv_hindo + 1;
        } else {
            /* ダミー候補が出現した */
            max_hindo[pos - 1] = 0;
        }
    } else if (request == NJG_OP_SEARCH_NEXT){
        /* 次回検索の場合 */

        /* 作成する数字タイプ */
        sutype = NJG_NMSC_GET_SUUJI_TYPE(NJG_NMSC_GET_POS_FROM_CUR(current_tmp));
        /* 次候補の場合は次の数字タイプにする */
        sutype = NJG_NMSC_GET_NEXT_SUUJI_TYPE(sutype);

        if (sutype == 0) {
            current_tmp = NJG_NMSC_GET_POS_FROM_CUR(current_tmp) - (NJG_NM_SUYM_IDX_MAX * NJG_NMSC_SUUJI_MAX);

            while (njg_nmsc_get_max_hindo(max_hindo, pY, len, &hindo, &pos, &ylen) != 0) {
                /* 頻度格納領域に頻度値が格納されている */
                /* 読み文字列から数字インデックスを作成 */
                slen = njg_nmsc_get_ksuuji_index_from_yomi(&tmp, pY, ylen, &dlen, &yomi_cnt, &yomi_type);

                /* 数字インデックスから数字データを作成 */
                njg_nmsc_get_ksuuji_data_from_index(&tmp);

                /* 変換後データ長算出 */
                /* 数字インデックスの数と漢数字(位)は一致する。*/
                len0 = (NJ_UINT16) (tmp.kan_len * NJG_NMSC_ZEN_MOJI_LEN);

                /* 数字部分の数値を取得 */
                for (i = 0; i < tmp.gw_len; i++) {
                    work = 1;
                    for (j = 0; j < (NJ_INT16)(tmp.gw_len - i - 1); j++) {
                        work *= 10;
                    }
                    num += (NJ_UINT32)(tmp.gijiwork[i] * work);
                }

                if (pos == pos_tmp) {
                    /* 検索する区切り位置が前回と同じ */
                    current_tmp++;
                    /* 頻度値更新 */
                    if (suym_idx_tbl[NJG_NMSC_GET_POS_FROM_CUR(current_tmp) + 1] < NJG_NM_DUMY_IDX_NUM) {
                        max_hindo[pos - 1] = (NJ_UINT8)num_conn_tbl[suym_idx_tbl[NJG_NMSC_GET_POS_FROM_CUR(current_tmp) + 1]].conv_hindo + 1;
                    } else {
                        max_hindo[pos - 1] = 0;
                    }
                    tblidx = njg_nmsc_get_table_index(current_tmp);
                    /* 数字部分１桁目チェック */
                    if ((yomi_type == 0) ||
                        ((yomi_type > 0) && (NJG_NM_IS_CHK_YOMI_CONV(yomi_type, num_conn_tbl[tblidx].info2) > 0))) {
                        /* 範囲チェックOKなら処理結果を作成 */
                        if ((NJG_NM_IS_CHK_CONV(num_conn_tbl[tblidx].info) == 0) ||
                            ((NJG_NM_IS_CHK_CONV(num_conn_tbl[tblidx].info) == NJG_NM_CHK_CONV_MASK) && (NJG_NM_IS_RANGE(num, tblidx) == 1))) {
                            /* 候補作成可 */
                            NJG_NMSC_SET_DIV(&current_tmp, slen);
                            break;
                        }
                    } else {
                        /* 候補作成不可 */
                        continue;
                    }
                } else {
                    /* 検索する区切り位置が前回から異なる */
                    tlen = len - slen;
                    nj_strncpy(yomibuf, pY, len);
                    yomibuf[len] = NJ_CHAR_NUL;
                    pYomi = yomibuf + slen;

                    /* 接続単位読み文字とテーブルの読みが一致する候補の先頭位置を２分検索する */
                    if (njg_nmsc_search_candidate(pYomi, tlen, &current_tmp) == 1) {
                        /* 候補の範囲チェックを行う */
                        while ((current_tmp < NJG_NM_SUYM_IDX_MAX) && (suym_idx_tbl[current_tmp] < NJG_NM_DUMY_IDX_NUM)) {
                            tblidx = njg_nmsc_get_table_index(current_tmp);
                            /* 頻度チェック */
                            if (hindo >= ((NJ_UINT8)(num_conn_tbl[tblidx].conv_hindo + 1))) {
                                break;
                            }
                            current_tmp++;
                        }
                        if ((NJG_NMSC_GET_POS_FROM_CUR(current_tmp) >= (NJG_NM_SUYM_IDX_MAX - 1)) ||
                            (suym_idx_tbl[current_tmp] >= NJG_NM_DUMY_IDX_NUM)) {
                            /* 頻度が一致する候補を検索できなかった */
                            max_hindo[pos - 1] = 0;
                            pos_tmp = pos;
                            continue; /*NCH*/
                        }
                        /* 頻度値更新 */
                        if (suym_idx_tbl[NJG_NMSC_GET_POS_FROM_CUR(current_tmp) + 1] < NJG_NM_DUMY_IDX_NUM) {
                            max_hindo[pos - 1] = (NJ_UINT8)num_conn_tbl[suym_idx_tbl[NJG_NMSC_GET_POS_FROM_CUR(current_tmp) + 1]].conv_hindo + 1;
                        } else {
                            /* ダミー候補が出現した */
                            max_hindo[pos - 1] = 0;
                        }
                        /* 数字部分１桁目チェック */
                        if ((yomi_type > 0) &&
                            (NJG_NM_IS_CHK_YOMI_CONV(yomi_type, num_conn_tbl[tblidx].info2) == 0)) {
                            /* 候補作成不可 */
                            pos_tmp = pos;
                            continue; /*NCH_FB*/
                        }
                        /* 範囲チェックOKなら処理結果を作成 */
                        if ((NJG_NM_IS_CHK_CONV(num_conn_tbl[tblidx].info) == 0) ||
                            ((NJG_NM_IS_CHK_CONV(num_conn_tbl[tblidx].info) == NJG_NM_CHK_CONV_MASK) && (NJG_NM_IS_RANGE(num, tblidx) == 1))) {
                            NJG_NMSC_SET_DIV(&current_tmp, slen);
                            break;
                        } else {
                            /* 候補作成不可 */
                            pos_tmp = pos;
                            continue ; /*NCH_FB*/
                        }
                    } else {
                        /* 候補を検索できなかった */
                        max_hindo[pos - 1] = 0;
                        pos_tmp = pos;
                        continue; /*NCH*/
                    }
                }
            }
            if (hindo == 0) {
                /* 全区切り位置で候補作成不可 */
                return 0;
            }
        } else {
            /* sutypeが0以外の場合 */

            /*
             * 次回検索で作成する数字タイプが0以外の場合は0と同じ候補の為、
             * 検索位置を数字タイプ0以外の値に設定するのみ(チェックは不要)
             */
            current_tmp += NJG_NM_SUYM_IDX_MAX;
            pos = *current_pos;
        }
    } else if (request == NJG_OP_GET_WORD_INFO) {
        /* 単語情報取得 */

        /* 作成する数字タイプ */
        sutype = NJG_NMSC_GET_SUUJI_TYPE(NJG_NMSC_GET_POS_FROM_CUR(current_tmp));

        /* 区切り位置は前回と同じ */
        ylen = NJG_NMSC_GET_DIV_FROM_CUR(current_tmp);

        /* 読み文字列から数字インデックスを作成 */
        slen = njg_nmsc_get_ksuuji_index_from_yomi(&tmp, pY, ylen, &dlen, &yomi_cnt, &yomi_type);

        /* 数字インデックスから数字データを作成 */
        njg_nmsc_get_ksuuji_data_from_index(&tmp);

        if (sutype == NJG_NMSC_SUUJI_KAN) {
            /* 変換後データ長算出 */
            /* 数字インデックスの数と漢数字(位)は一致する。*/
            len0 = (NJ_UINT16) (tmp.kan_len * NJG_NMSC_ZEN_MOJI_LEN);
        } else {
            /* 作成した候補文字長格納 */
            len0 = tmp.gw_len * NJG_NMSC_GET_SUUJI_LEN(sutype);
        }

        pos = *current_pos;
    } else {
        /* 初回検索、次回検索、単語情報取得以外 */
        return 0; /*NCH*/
    }
    /* 作成する候補の文字列長がNJ_MAX_RESULT_LEN を超えていなければ、候補作成可 */
    tblidx = njg_nmsc_get_table_index(current_tmp);
    len0 += num_conn_tbl[tblidx].kouho_len;
    if (len0 <= NJ_MAX_RESULT_LEN) {
        *current_pos = pos;
        *current = current_tmp;
        return len0;
    }
    return 0; /*NCH_FB*/
}


/**
 * 読み文字比較処理
 *
 * @param[in]  yomi   読み文字列
 * @param[in]  len    読み文字列長
 * @param[in]  tbl    数字繋がり候補テーブル
 *
 * @retval  >0     読み文字列 > テーブル読み文字列
 * @retval  =0     読み文字列 = テーブル読み文字列
 * @retval  <0     読み文字列 < テーブル読み文字列
 */
static NJ_INT16 njg_nmsc_cmp_yomi(NJ_CHAR *yomi, NJ_UINT16 len, const NJG_NUM_CONN_TBL *tbl) {

#ifdef NJ_OPT_UTF16
    NJ_CHAR tblyomi[NJG_NM_YOMI_MAX + NJ_TERM_LEN];
#else /* NJ_OPT_UTF16 */
    NJ_CHAR tblyomi[(NJG_NM_YOMI_MAX * 2) + NJ_TERM_LEN];
#endif /* NJ_OPT_UTF16 */
    NJ_CHAR yomibuf[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_CHAR* dst;
    NJ_UINT16 i;

    /* テーブル読み文字を格納 */
    dst = tblyomi;
    for (i = 0; i < (NJ_UINT16)((tbl->yomi_len * sizeof(NJ_CHAR)) / sizeof(NJ_UINT16)); i++) {
#ifdef NJ_OPT_UTF16
        NJG_NMSC_COPY_INT16_TO_CHAR(dst, tbl->yomi[i]);
        dst += NJG_NMSC_ZEN_MOJI_LEN;
#else /* NJ_OPT_UTF16 */
        if ((tbl->yomi[i] & 0xff00) != 0) {
            /* 全角文字なら上位バイトも格納 */
            *dst++ = (NJ_UINT8)((tbl->yomi[i] >> 8) & 0xff);
        }
        *dst++ = (NJ_UINT8)(tbl->yomi[i] & 0xff);
#endif /* NJ_OPT_UTF16 */
    }
    *dst = NJ_CHAR_NUL;

    /* 残りの読み文字を格納 */
    nj_strncpy(yomibuf, yomi, len);
    yomibuf[len] = NJ_CHAR_NUL;

    return nj_strcmp(yomibuf, tblyomi);
}

/**
 * 候補表記取得
 *
 * @param[in]  word       文節情報
 * @param[out] candidate  表記文字列格納バッファ
 * @param[in]  size       candidate バイトサイズ
 *
 * @return  格納文字列の文字配列長
 */
static NJ_INT16 njg_nmsc_get_candidate(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size) {

    NJ_INT16 len = 0;
    NJ_UINT16 tblidx;
    NJ_UINT8 sutype;

    /* 検索位置から数字繋がり候補テーブル要素番号、数字部分タイプを取得 */
    tblidx = njg_nmsc_get_table_index(word->stem.loc.current);
    sutype = NJG_NMSC_GET_SUUJI_TYPE(NJG_NMSC_GET_POS_FROM_CUR(word->stem.loc.current));

    /* 候補表記取得 */
    len = njg_nmsc_get_candidate_string(word, candidate, &num_conn_tbl[tblidx], sutype);

    return len;
}

/**
 * 数詞付き数字変換候補の共通作成処理
 *
 * @param[in]  word      文節情報
 * @param[out] candidate 候補文字列格納領域
 * @param[in]  ctbl      数字繋がり候補作成テーブル<br>
 *                       作成する候補のテーブルのみセットされる。
 * @param[in]  sutype    数字部分タイプ
 *
 * @retval >=0  格納した候補文字列文字長
 * @retval <0   エラー
 */
static NJ_INT16 njg_nmsc_get_candidate_string(NJ_WORD *word, NJ_CHAR *candidate,
                                             const NJG_NUM_CONN_TBL *ctbl, NJ_UINT16 sutype) {

    NJ_CHAR   *yomi;
    NJ_CHAR   *dst;
    NJ_UINT16 len, cnt;
    NJ_UINT16 i;
    NJ_INT16 ret;
    NJ_UINT16 slen = 0;
    NJ_UINT16  dlen;
    const NJ_UINT16 *giji_tbl;
    NJ_INT16  index;
    NJ_UINT8  yomi_cnt = 0;
    NJ_UINT8  yomi_type = 0;
    NJ_UINT8  yomi_len;
    NJ_UINT8  tblidx;
    PART_OF_NJ_ENV tmp;

    yomi = word->yomi;
    dst = candidate;
    len = NJG_NMSC_GET_DIV_FROM_CUR(word->stem.loc.current);

    if (sutype == NJG_NMSC_SUUJI_KAN) {
        /* 数字部分が漢数字(位)の場合 */
        for (cnt = 0; cnt < len;) {
            if ((ret = njg_nmsc_get_ksuuji_ketapos((yomi + cnt), &yomi_len, &tblidx)) > 0) {
                if ((ret >= 0) && (ret <= 9))  { /* 1から９の数字*/
                    NJG_NMSC_COPY_INT16_TO_CHAR(dst, nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_KAN][ret]);
                } else {
                    switch (ret) {
                    case NJG_NM_KETA_JYUU: /* 十 */
                        NJG_NMSC_COPY_INT16_TO_CHAR(dst, nmgiji_kurai_ksuuji_table[0]);
                        break;
                    case NJG_NM_KETA_HYAKU: /* 百 */
                        NJG_NMSC_COPY_INT16_TO_CHAR(dst, nmgiji_kurai_ksuuji_table[1]);
                        break;
                    case NJG_NM_KETA_SEN: /* 千 */
                    case NJG_NM_KETA_ZEN: /* 千 */
                        NJG_NMSC_COPY_INT16_TO_CHAR(dst, nmgiji_kurai_ksuuji_table[2]);
                        break;
                    case NJG_NM_KETA_MAN: /* 万  */
                        NJG_NMSC_COPY_INT16_TO_CHAR(dst, nmgiji_kurai_ksuuji_table[3]);
                        break;
                    default:
                        /* テーブルの設定値エラーとして変換対象外とする */
                        return -1; /*NCH*/
                    }
                }
                dst += NJG_NMSC_ZEN_MOJI_LEN;
                cnt += (NJ_UINT16)yomi_len;
            } else {
                /* テーブルと一致しないため変換対象外とする */
                return -1; /*NCH_FB*/
            }
        }
    } else {
        /* 数字部分が数字文字の場合 */
        if (sutype == NJG_NMSC_SUUJI_ZEN) {
            giji_tbl = nmgiji_suuji_tbl[1];
        } else {
            giji_tbl = nmgiji_suuji_tbl[0];
        }

        /* 読み文字列から数字インデックスを作成 */
        slen = njg_nmsc_get_ksuuji_index_from_yomi(&tmp, yomi, len, &dlen, &yomi_cnt, &yomi_type);
        if (slen <= 0) {
            return -1; /*NCH*/
        }

        /* 数字インデックスから数字データを作成 */
        if (njg_nmsc_get_ksuuji_data_from_index(&tmp) < 0) {
            return -1; /*NCH*/
        }

        if (tmp.gw_len > NJG_NM_LIMIT_MAX_LEN) {
            /* 候補作成不可 */
            return -1; /*NCH*/
        }

        /* 数字データから、数字候補テーブル取り出す */
        for (i = 0; i < tmp.gw_len; i++) {
            index = (NJ_INT16)(tmp.gijiwork[i] & 0xff);

            if (giji_tbl[index] != 0) {
#ifdef NJ_OPT_UTF16
                NJG_NMSC_COPY_INT16_TO_CHAR(dst, giji_tbl[index]);
                dst += NJG_NMSC_ZEN_MOJI_LEN;
#else /* NJ_OPT_UTF16 */
                if ((giji_tbl[index] & 0xff00) != 0) {
                    NJG_NMSC_COPY_INT16_TO_CHAR(dst, giji_tbl[index]);
                    dst += NJG_NMSC_ZEN_MOJI_LEN;
                } else {
                    *dst = (NJ_UINT8)(giji_tbl[index] & 0xff);
                    dst += NJG_NMSC_HAN_MOJI_LEN;
                }
#endif /* NJ_OPT_UTF16 */
            } else {
                return -1;/* 作成できない */ /*NCH*/
            }
        }
    }

    /* 数字繋がり部分を格納 */
    for (i = 0; i < (NJ_UINT16)((ctbl->kouho_len * sizeof(NJ_CHAR)) / sizeof(NJ_UINT16)); i++) {
#ifdef NJ_OPT_UTF16
        NJG_NMSC_COPY_INT16_TO_CHAR(dst, ctbl->kouho[i]);
        dst += NJG_NMSC_ZEN_MOJI_LEN;
#else /* NJ_OPT_UTF16 */
        if ((ctbl->kouho[i] & 0xff00) != 0) {
            /* 全角文字なら上位バイトも格納 */
            *dst++ = (NJ_UINT8)((ctbl->kouho[i] >> 8) & 0xff);
        }
        *dst++ = (NJ_UINT8)(ctbl->kouho[i] & 0xff);
#endif /* NJ_OPT_UTF16 */
    }
    *dst = NJ_CHAR_NUL;

    /* 成功 */
    return (NJ_INT16)NJ_GET_KLEN_FROM_STEM(word);
}

/**
 * giji_suuji_yomi_tbl[]を基に読み文字を検索し、
 * 数字に置き換えられる読み文字数を返す。
 *
 * @param[in]  yomi      数字読み文字列
 * @param[out] yomi_len  数字に置き換えた読み文字の文字配列数
 * @param[out] tblidx    数字に置き換えた読み文字の数字読み文字テーブルインデックス番号
 *
 * @retval >1   置き換える数字(Hexデータ)。giji_suuji_yomi_tbl[].ketaposを返す。
 * @retval <=0  テーブル不一致
 */
static NJ_INT16 njg_nmsc_get_ksuuji_ketapos(NJ_CHAR *yomi, NJ_UINT8 *yomi_len, NJ_UINT8 *tblidx) {

    NJG_SUUJIYOMITBL* ptr;
    NJ_CHAR* pY;
    NJ_INT16 ret;

    /* 数字読みテーブルを検索 */
    ptr = (NJG_SUUJIYOMITBL *)nmgiji_suuji_yomi_tbl;

    /*
     * 数字読みテーブル単位で、読み文字の一致チェックを行う。
     * テーブルに合わせて、UINT16でチェックする。
     */
    while (ptr->ketapos > 0) {
        /* 読み文字の取り出し */
        pY = yomi;

        ret = njg_nmsc_cmp_suujiyomi(pY, ptr);
        /*
         * 比較結果
         */
        if (ret == 0) {
            /* 検索一致 */
            *yomi_len = (NJ_UINT8)(ptr->len * NJG_NMSC_ZEN_MOJI_LEN);
            return ptr->ketapos;
        } else {
            if ((ret > 0) && (NJG_NMSC_CONV_TO_WCHAR(yomi) != ptr->yomi[0])) {
                /* 降順にソートされているため，yomi の方が大きければ
                 * それ以上検索する必要はない．
                 */
                return -1;
            }
        }
        /* 次の数字読みテーブル */
        ptr++;
        (*tblidx)++;
    }
    return -1;
}

/**
 * 擬似候補取得処理：読み文字列から数字インデックスを作成する
 *
 *  この数字インデックスは、漢数字擬似候補を作成するための中間データである。
 *
 * @param[out] tmp       作業領域<br>
 *                       tmp->kan_data : 数字インデックス<br>
 *                       tmp->kan_len  : 数字インデックスの有効配列長
 * @param[in]  yomi      擬似候補 読み
 * @param[in]  ylen       読み文字長
 * @param[out] dlen      １つ前の区切り位置
 * @param[out] yomi_cnt  「し」「く」の出現回数
 * @param[out] yomi_type １桁目読み文字タイプ
 *
 * @retval >0 作成済み文字長
 * @retval 0  作成できない
 */
static NJ_INT16 njg_nmsc_get_ksuuji_index_from_yomi(PART_OF_NJ_ENV *tmp, NJ_CHAR *yomi, NJ_UINT16 ylen,
                                               NJ_UINT16 *dlen, NJ_UINT8 *yomi_cnt, NJ_UINT8  *yomi_type) {
    NJ_UINT8  yomi_len;
    NJ_UINT8  tblidx;
    NJ_UINT16 cnt = 0;
    NJ_INT16  index;
    NJ_UINT8  old_tblidx = 0;
    NJ_UINT8  i;
    NJ_UINT16 *kanpt;
    /*
     * 最初に、読み文字列から、数字読みテーブルを基に
     * 数字インデックスを作成する。
     */
    *dlen = 0;
    *yomi_cnt = 0;
    kanpt = tmp->kan_data;
    tmp->kan_len = 0;
    for (cnt = 0; cnt < ylen;) {
        tblidx = 0;
        /*
         * 数字インデックスを、kanpt へ格納していく。
         * 例えば、読みが「ろっぴゃく」の場合、数字インデックスは、
         *「ろっ」と「ぴゃく」の2つが作成される。
         * 読みがどのように分割されるかは、数字読みテーブルの
         * 設定文字に左右される。
         */
        if ((index = njg_nmsc_get_ksuuji_ketapos((yomi + cnt), &yomi_len, &tblidx)) > 0) {
            if ((index >= 0) && (index <= 9)) { /* 0から９の数字*/
                if ((tblidx == NJG_NM_YOMI_SI_INDEX) ||(tblidx == NJG_NM_YOMI_SICHI_INDEX) ||
                    (tblidx == NJG_NM_YOMI_KU_INDEX)) {
                    (*yomi_cnt)++;
                }
                tmp->kan_len++;
                *kanpt++ = (NJ_UINT16)index;
            } else {
                switch (index) {
                case NJG_NM_KETA_JYUU: /* 十 */
                case NJG_NM_KETA_HYAKU: /* 百 */
                case NJG_NM_KETA_SEN: /* 千 */
                case NJG_NM_KETA_ZEN: /* 千 */
                case NJG_NM_KETA_MAN: /* 万  */
                    if (cnt == 0) {
                        /* 「ぴゃく」で始まる場合は作成不可とする */
                        if (tblidx == NJG_NM_YOMI_PYAKU_INDEX) {
                            return 0;
                        }
                    } else {
                        /* 「いっ」「ろっ」「はっ」チェック */
                        if (tblidx == NJG_NM_YOMI_PYAKU_INDEX) {
                            if ((old_tblidx != NJG_NM_YOMI_ROXTU_INDEX) && (old_tblidx != NJG_NM_YOMI_HAXTU_INDEX)) {
                                /* 「ぴゃく」は「ろっぴゃく」「はっぴゃく」以外は作成不可 */
                                *dlen = cnt;
                                return 0;
                            }
                        } else if (tblidx == NJG_NM_YOMI_SEN_INDEX) {
                            if (old_tblidx == NJG_NM_YOMI_ROXTU_INDEX) {
                                /* 「せん」は「ろっせん」の場合のみ作成不可 */
                                *dlen = cnt;
                                return 0;
                            }
                        } else {
                            if ((old_tblidx == NJG_NM_YOMI_IXTU_INDEX) || (old_tblidx == NJG_NM_YOMI_ROXTU_INDEX)
                                || (old_tblidx == NJG_NM_YOMI_HAXTU_INDEX)) {
                                /* 「じゅう」「ひゃく」「ぜん」「まん」は「いっ」「ろっ」「はっ」の場合作成不可 */
                                *dlen = cnt;
                                return 0;
                            }
                        }
                    }
                    tmp->kan_len++;
                    *kanpt++ = (NJ_UINT16)index;
                    break;
                default:
                    /* テーブルの設定値エラーとして変換対象外とする */
                    return 0; /*NCH*/
                }
            }
            /* 前回値保存 */
            old_tblidx = tblidx;
            /*
             * 数字に変換できた読み文字列の文字配列長が返ってくるので、
             * 続きの読み文字列から検索を繰り返す。
             */
            *dlen = cnt;
            cnt += yomi_len;
            if (cnt > ylen) {
                /* cnt が ylen を超えたらエラー */
                return 0;
            }
        } else {
            if (tmp->kan_len == 0) {
                /* テーブルと一致しないため変換対象外とする */
                return 0;
            } else {
                if (tmp->kan_len >= NJ_MAX_RESULT_LEN) {
                    /*
                     * kan_dataのサイズがNJ_MAX_RESULT_LENのため、
                     * バッファオーバーしないようにこのタイミングで
                     * サイズチェックを行う。
                     */
                    return 0; /*NCH_FB*/
                } else {
                    /* テーブルと一致したため変換対象とする */
                    break;
                }
            }
        }
        if (tmp->kan_len >= NJ_MAX_RESULT_LEN) {
            /*
             * kan_dataのサイズがNJ_MAX_RESULT_LENのため、
             * バッファオーバーしないようにこのタイミングで
             * サイズチェックを行う。
             */
            return 0; /*NCH_FB*/
        }
    }
    for (i = 0; i < NJG_NM_CHK_TBL_MAX; i++) {
        if (old_tblidx == conn_chk_tbl[i][0]){
            /* 数字部分１桁目がチェック対象 */
            *yomi_type = conn_chk_tbl[i][1];
        }
    }

    *kanpt = 0;

    return cnt;
}

/**
 * 「まん」のチェック
 *
 * 数字に変換した配列をチェックし、「まん」のみの
 * 構成ではないかチェックする。
 *
 * @param[in]  suuji_strings  チェックする数字データ
 *
 * @retval  0  数字文字列として扱える
 * @retval -1  数字文字列として扱えない
 */
static NJ_INT16 njg_nmsc_check_ksuuji_array(NJ_CHAR *suuji_strings) {

    NJ_INT16 cnt, check_keta_cnt;
    NJ_INT16 right_flg;

    /* 万のフラグが立っている場合にそれより前に数字があるかどうかチェックする。*/
    /* 「まん」といったものは、数字としてみなさないため。*/
    check_keta_cnt = (NJ_INT16)(NJG_NM_KETA_MAN - NJG_NM_SUB_CNT_MAN);
    if (suuji_strings[check_keta_cnt] == NJG_NM_FIND_MAN) {
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
    return(0);
}

/**
 * 文字列を数字に変換する
 *
 * @param[in]  kanpt         入力文字列
 * @param[in]  kan_len       入力文字列長
 * @param[out] suuji_strings 変換結果格納バッファ
 * @param[out] suuji_size    変換結果格納バッファ文字長
 *
 * @retval 1  数字に変換できた
 * @retval 0  数字に変換できない
 */
static NJ_INT16 njg_nmsc_convert_ksuuji_data(NJ_UINT16 *kanpt, NJ_INT16 kan_len,
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
                && (suuji_strings[add_cnt + base_cnt] != NJG_NM_FIND_MAN)
                && (suuji_strings[add_cnt + base_cnt] != NJG_NM_FIND_JYUUHYAKUSEN))  {
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
            if ((kanval >= NJG_NM_KETA_JYUU) && (kanval <= NJG_NM_KETA_ZEN)) {
                /********************************************
                 * 十、百、千
                 */
                if (kanval == NJG_NM_KETA_ZEN ) {
                    /*
                     *「ぜん」の読みは「せん」で処理する。
                     * また、「ぜん〜」という不自然な読みのチェックのため
                     * zen_flag をセットしておく。
                     */
                    kanval = NJG_NM_KETA_SEN;
                    zen_flag = 1;
                }
                if (add_cnt >= (kanval - NJG_NM_SUB_CNT_JYUUTOSEN)) {
                    /* にひゃくにせんなどは、漢数字としてみなさない */
                    return(0);
                } else {
                    add_cnt = (NJ_INT16)(kanval - NJG_NM_SUB_CNT_JYUUTOSEN);
                    if (suuji_strings[add_cnt + base_cnt] == 0x00) {
                        suuji_strings[add_cnt + base_cnt] = NJG_NM_FIND_JYUUHYAKUSEN;
                    } else {
                        /* じゅうじゅうじゅうも、漢数字としてみなさない */
                        return(0); /*NCH*/
                    }
                }
            } else {
                /********************************************
                 * 万
                 */
                if (base_cnt >= (kanval - NJG_NM_SUB_CNT_MAN)) {
                    /* 七万八万などは、漢数字としてみなさない*/
                    return(0);
                } else {
                    base_cnt = (NJ_INT16)(kanval - NJG_NM_SUB_CNT_MAN);
                    add_cnt = 0;

                    /* まんがあったときは、
                     * 後でチェック可能なようにフラグをセットする
                     */
                    if (suuji_strings[add_cnt + base_cnt] == 0x00) {
                        suuji_strings[add_cnt + base_cnt] = NJG_NM_FIND_MAN;
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

    if (njg_nmsc_check_ksuuji_array(suuji_strings) == -1) {
        return(0);
    }
    return(1);     /* 一応、数字変換できた*/
}

/**
 * 擬似候補取得処理：数字インデックスから数字データを作成する
 *
 * この数字データは、数字の候補テーブルを取り出すための要素として使用される。
 *
 * @param[out] tmp    作業領域<br>
 *                    tmp->gijiwork : 数字データ<br>
 *                    tmp->gw_len   : 数字データの有効バイト
 *
 * @retval  1  指定された擬似候補を取得した
 * @retval  0  指定された擬似候補を取得できない
 */
static NJ_INT16 njg_nmsc_get_ksuuji_data_from_index(PART_OF_NJ_ENV *tmp) {

    NJ_INT16 cnt;
    NJ_INT16 setcnt;
    NJ_INT16 ret;
    NJ_CHAR  *suuji_strings;
    NJ_CHAR *tmppt;

    suuji_strings = tmp->gijiwork;

    /* 数字インデックスから数字変換データに変換する。 */
    /* この関数では、位を表すデータ(万、億など)は、まだ独自の数値がセットされている。 */
    /* また、(Out)である数字変換データ(suuji_strings)は、読み文字に対して、逆順にセットされている。*/
    ret = njg_nmsc_convert_ksuuji_data(tmp->kan_data, tmp->kan_len, suuji_strings, NJ_MAX_RESULT_LEN);
    if (ret <= 0) {
        return (-1); /* 読み方の並びがおかしいので作成できない */
    }
    /*
     * 一旦、逆順のままワーク用領域へ退避する
     */
    tmppt = tmp->tmp;
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
        if ((tmppt[setcnt] == 0x00) || (tmppt[setcnt] == NJG_NM_FIND_MAN)) {
            suuji_strings[cnt] = 0;
        } else {
            if (tmppt[setcnt] == NJG_NM_FIND_JYUUHYAKUSEN) { /* せんひゃくは、１１００となるべき*/
                suuji_strings[cnt] = 1;
            } else {
                suuji_strings[cnt] = (NJ_UINT8)(tmppt[setcnt] & 0x0f);
            }
        }
        setcnt--;
        cnt++;
    }
    tmp->gw_len = cnt;

    return 0;
}

/**
 * 数字読みテーブルと読み文字比較処理
 *
 * @param[in]  yomi       読み文字
 * @param[in]  tbl        数字読み文字列テーブルインデックス先頭アドレス
 *
 * @retval 0  検索一致
 * @retval !0 検索一致しない
 */
static NJ_INT16 njg_nmsc_cmp_suujiyomi(NJ_CHAR *yomi, NJG_SUUJIYOMITBL *tbl) {

    NJ_INT16 ret;
    NJ_INT16 n;
    NJ_UINT16 s1;
    NJ_UINT16 *s2;

    s1 = NJG_NMSC_CONV_TO_WCHAR(yomi);
    /* 数字読みテーブルの読みの取り出し */
    s2 = tbl->yomi;
    n = tbl->len;

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
        yomi += NJG_NMSC_ZEN_MOJI_LEN;
        s1 = NJG_NMSC_CONV_TO_WCHAR(yomi);
        s2++;
        n--;
    }

    return ret;
}

/**
 * 最高頻度情報取得処理
 *
 * @param[in]     max_hindo 最高頻度格納領域
 * @param[in]     pY        読み文字列
 * @param[in]     len       読み文字列の文字長
 * @param[out]    hindo     最高頻度値
 * @param[out]    pos       区切り位置
 * @param[out]    dlen      区切り位置までの文字長
 *
 * @retval 1  作成候補あり
 * @retval 0  作成候補なし
 */
static NJ_INT16 njg_nmsc_get_max_hindo(NJ_UINT8 *max_hindo, NJ_CHAR *pY, NJ_UINT16 len,
                                        NJ_UINT8 *hindo, NJ_UINT16 *pos, NJ_UINT16 *dlen) {

    NJ_CHAR   *pYomi;
    NJ_CHAR   yomibuf[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_UINT16 count = 0;
    NJ_UINT16  i;

    *hindo = 0;
    *dlen = 0;
    /* 一番頻度の高い区切り位置検索 */
    for (i = 0; i < NJG_NM_HINDO_TBL_MAX; i++) {
        if ((max_hindo[i] != 0) && (*hindo < max_hindo[i])) {
            *pos = i + 1;
            *hindo = max_hindo[i];
        }
    }
    if (*hindo == 0) {
        /* 対象の候補なし */
        return 0;
    }

    /* 入力文字数取得 */
    nj_strncpy(yomibuf, pY, len);
    yomibuf[len] = NJ_CHAR_NUL;
    count = nj_charlen(yomibuf);

    /* 区切り位置までの文字長算出 */
    count -= (*pos);
    pYomi = pY;
    for (i = 0; i < count; i++) {
        if (NJG_NMSC_IS_WCHAR(*pYomi) == 1) {
            *dlen += NJG_NMSC_ZEN_MOJI_LEN;
            pYomi += NJG_NMSC_ZEN_MOJI_LEN;
        } else {
            *dlen += NJG_NMSC_HAN_MOJI_LEN;
            pYomi += NJG_NMSC_HAN_MOJI_LEN;
        }
    }
    return 1;
}

/**
 * 接続単位読み文字候補２分検索処理
 *
 * @param[in]     pYomi      接続単位読み文字
 * @param[in]     tlen       接続単位読み文字長
 * @param[out]    match_top  一致した先頭
 *
 * @retval 1  候補あり
 * @retval 0  候補なし
 */
static NJ_UINT16 njg_nmsc_search_candidate(NJ_CHAR *pYomi, NJ_UINT16 tlen, NJ_UINT32 *match_top) {

    NJ_INT16  cmp;
    NJ_INT16  left, right, mid = 0;
    NJ_UINT16 findflg = 0;

    /* 新しい分割位置での初回検索の場合は、変換インデックスを２分検索する */
    left = 0;
    right = NJG_NM_SUYM_IDX_MAX - 1;
    while (left <= right) {
        mid = left + ((right - left) / 2);

        /* 読み文字とテーブルの読み文字を比較する */
        if (suym_idx_tbl[mid] == NJG_NM_DUMY_IDX_NUM) {
            cmp = njg_nmsc_cmp_yomi(pYomi, tlen, &num_conn_tbl[suym_idx_tbl[mid - 1]]);
        } else {
            cmp = njg_nmsc_cmp_yomi(pYomi, tlen, &num_conn_tbl[suym_idx_tbl[mid]]);
        }
        if (cmp < 0) {
            right = mid - 1;
        } else if (cmp > 0) {
            left = mid + 1;
        } else {
            findflg = 1;
            break;
        }
    }
    if (findflg == 1) {
        /* 候補が見つかった */
        *match_top = mid;

        /* 前方向に同じ読み文字を検索する */
        while (*match_top > 0) {
            /* インデックステーブルの1つ前の候補をチェック */
            if (suym_idx_tbl[*match_top - 1] == NJG_NM_DUMY_IDX_NUM) {
                break;
            }
            (*match_top)--;
        }
    }
    return findflg;
}

/**
 * テーブルインデックス取得処理
 *
 * @param[in]     current    検索位置
 *
 * @retval テーブル要素番号
 */
static NJ_UINT16 njg_nmsc_get_table_index(NJ_UINT32 current) {
    NJ_UINT32 current_tmp;
    NJ_UINT16 tblidx = 0;

    /* currentから検索位置を取得 */
    current_tmp = NJG_NMSC_GET_POS_FROM_CUR(current);
    
    if (current_tmp < NJG_NM_SUYM_IDX_MAX) {
        /* 数字タイプが0の場合 */
        tblidx = suym_idx_tbl[current_tmp];
    } else if (current_tmp < (NJG_NM_SUYM_IDX_MAX * 2)) {
        /* 数字タイプが1の場合 */
        tblidx = suym_idx_tbl[current_tmp - NJG_NM_SUYM_IDX_MAX];
    } else {
        /* 数字タイプが2の場合 */
        tblidx = suym_idx_tbl[current_tmp - (NJG_NM_SUYM_IDX_MAX * 2)];
    }
    
    return tblidx;
}
