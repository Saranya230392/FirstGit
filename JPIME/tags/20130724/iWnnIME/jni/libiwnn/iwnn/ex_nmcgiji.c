/**
 * @file
 *  [拡張] 数字交じり変換候補作成辞書 (UTF16/SJIS版)
 *
 * 数字交じり変換候補の作成を行う辞書。
 *
 * @author
 * (C) OMRON SOFTWARE Co., Ltd. 2009 All Rights Reserved.
 */
#include "ex_nmcgiji.h"
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
#define NJG_NMC_HAN_MOJI_LEN  1
/** 全角文字長 (UTF16) */
#define NJG_NMC_ZEN_MOJI_LEN  1

#else /* NJ_OPT_UTF16 */
/** 半角文字長 (SJIS) */
#define NJG_NMC_HAN_MOJI_LEN  1
/** 全角文字長 (SJIS) */
#define NJG_NMC_ZEN_MOJI_LEN  2
#endif /* NJ_OPT_UTF16 */

/*
 * 作成候補数字タイプ定義
 *
 * @note 値が0の数字タイプの候補が最初に作成され、次に値が1の候補が作成される。
 */
/** 数字タイプ 全角 */
#define NJG_NMC_SUUJI_ZEN       0
/** 数字タイプ 半角 */
#define NJG_NMC_SUUJI_HAN       1

/** 数字交じり変換辞書頻度段階 */
#define NMC_DIC_FREQ_DIV 63

/*
 * 検索位置最大数定義
 *
 * @note 1候補に対して数字部分が全角半角を作成する為、テーブル要素数の2倍とする。
 */
#define NJG_NMC_CURRENT_MAX (NJG_NM_CONV_IDX_MAX * 2)

/* 内部用マクロ定義 */
/**
 * 検索位置から作成する数字タイプを取得する
 *
 * @param[in]      x      検索位置
 * @retval         0 or 1 数字タイプ
 */
#define NJG_NMC_GET_SUUJI_TYPE(x) (NJ_UINT8)((((x) < NJG_NM_CONV_IDX_MAX)) ? 0 : 1)
/**
 * 数字部分タイプが全角かどうか確認
 *
 * @param[in]      x  数字部分タイプ
 * @retval         0  全角
 * @retval        !0  半角
 */
#define NJG_NMC_IS_SUUJI_ZEN(x) (((x) == NJG_NMC_SUUJI_ZEN) ? 1 : 0)
/**
 * 数字部分タイプから数字文字長を取得。
 *
 * @param[in]      x  数字部分タイプ
 * @return 数字文字長
 */
#define NJG_NMC_GET_SUUJI_LEN(x) (NJ_UINT16)(((x) == NJG_NMC_SUUJI_ZEN) ? NJG_NMC_ZEN_MOJI_LEN : NJG_NMC_HAN_MOJI_LEN)
/**
 * 検索位置から数字繋がり候補テーブル要素番号を取得。
 *
 * @param[in]      x  検索位置
 *
 * @return 数字繋がり候補テーブル要素番号 (NJ_UINT16)
 */
#define NJG_NMC_GET_TABLE_INDEX(x) (NJ_UINT16)((NJG_NMC_GET_SUUJI_TYPE(x) == 0) ? (conv_idx_tbl[(x)]) : (conv_idx_tbl[((x) - (NJG_NM_CONV_IDX_MAX))]))                  

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
#define NJG_NMC_CONV_TO_WCHAR(x)                                            \
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
#define NJG_NMC_IS_WCHAR(x)  1
#else /* NJ_OPT_UTF16 */
/** SJIS版 */
#define NJG_NMC_IS_WCHAR(x)                                                 \
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
#define NJG_NMC_COPY_INT16_TO_CHAR(to, from)                                \
    { ((NJ_UINT8*)(to))[0] = (NJ_UINT8)(((from) >> 8) & 0x00ff);        \
        ((NJ_UINT8*)(to))[1] = (NJ_UINT8)((from) & 0x00ff); }

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
static NJ_INT16 njg_nmc_get_result(NJ_UINT16 request, NJ_DIC_HANDLE rule,
                                   NJ_CHAR *yomi, NJ_UINT16 len,
                                   NJ_UINT32 *current, NJ_RESULT *giji);
static NJ_INT16 njg_nmc_get_giji(NJ_UINT16 request, NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                 NJ_CHAR *yomi, NJ_UINT16 len,
                                 NJ_UINT32 *current, NJ_RESULT *p);
static NJ_INT16 njg_nmc_chk_giji(NJ_UINT16 request, NJ_CHAR *pY, NJ_UINT16 len, 
                                 NJ_UINT32 *current);
static NJ_INT16 njg_nmc_cmp_yomi(NJ_CHAR *yomi, NJ_UINT16 len, const NJG_NUM_CONN_TBL *tbl);
static NJ_INT16 njg_nmc_get_candidate(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size);
static NJ_INT16 njg_nmc_get_candidate_string(NJ_WORD *word, NJ_CHAR *candidate,
                                             const NJG_NUM_CONN_TBL *ctbl, NJ_UINT16 sutype);

/***********************************************************************/

/**
 * 数字交じり変換辞書 辞書インタフェース
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
NJ_EXTERN NJ_INT16 njex_nmcgiji_dic(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE *message) {

    NJ_INT16 ret;
    NJ_UINT32 current = 0;
    NJ_RESULT rlt;
    NJ_UINT16 len;
    NJ_HINDO hindo;
    NJ_UINT16 tblidx;

    switch (request) {
    case NJG_OP_SEARCH:
        if ((message->condition->operation != NJ_CUR_OP_COMP)
            || (message->condition->mode != NJ_CUR_MODE_FREQ)) {
            /* 正引き完全一致頻度順以外は非対応 */
            message->location->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }

        /* 候補を作ってみる */
        ret = njg_nmc_get_result(request, message->dicset->rHandle[0],
                                 message->condition->yomi, message->condition->ylen,
                                 &current, &rlt);
        if (ret == 1) {
            /* 候補作成可能 */
            message->location->loct.current = current;
            message->location->loct.current_info = 0x10;
            /* 頻度を設定する */
            tblidx = NJG_NMC_GET_TABLE_INDEX(current);
            hindo = CALCULATE_HINDO(num_conn_tbl[tblidx].conv_hindo, 
                                    message->location->dic_freq.base, message->location->dic_freq.high,
                                    NMC_DIC_FREQ_DIV);
            NJ_SET_FREQ_TO_STEM(&(rlt.word), hindo);
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

        /* 候補を作ってみる */
        ret = njg_nmc_get_result(request, message->dicset->rHandle[0],
                                 message->condition->yomi, message->condition->ylen,
                                 &current, &rlt);
        if (ret > 0) {
            /* 候補作成可能 */
            message->location->loct.current = current;
            message->location->loct.current_info = 0x10;
            /* 頻度を設定する */
            tblidx = NJG_NMC_GET_TABLE_INDEX(current);
            hindo = CALCULATE_HINDO(num_conn_tbl[tblidx].conv_hindo, 
                                    message->location->dic_freq.base, message->location->dic_freq.high,
                                    NMC_DIC_FREQ_DIV);
            NJ_SET_FREQ_TO_STEM(&(rlt.word), hindo);
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
        if (message->location->loct.current > NJG_NMC_CURRENT_MAX) {
            /* 念のため候補の範囲をチェック。ココを通ることはない */
            return -1;
        }
        /* 候補を作成する */
        ret = njg_nmc_get_result(request, message->dicset->rHandle[0],
                                 message->word->yomi, NJ_GET_YLEN_FROM_STEM(message->word),
                                 &message->location->loct.current, &rlt);
        if (ret > 0) {
            /* 候補作成可能なはず */
            *(message->word) = rlt.word;
            /* 頻度を設定する */
            tblidx = NJG_NMC_GET_TABLE_INDEX(message->location->loct.current);
            hindo = CALCULATE_HINDO(num_conn_tbl[tblidx].conv_hindo, 
                                    message->location->dic_freq.base, message->location->dic_freq.high,
                                    NMC_DIC_FREQ_DIV);
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
        ret = njg_nmc_get_candidate(message->word, message->string, message->string_size);
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
 * 数字繋がり変換候補を作成する。
 *
 * @param[in]     request iWnnからの処理要求
 * @param[in]     rule    ルール辞書ハンドル
 * @param[in]     yomi    読み文字列
 * @param[in]     len     yomi 文字長
 * @param[in,out] current 検索位置
 * @param[out]    giji    候補格納領域
 *
 * @retval 1  候補を作成できた
 * @retval 0  候補を作成できない
 * @retval <0 エラー
 */
static NJ_INT16 njg_nmc_get_result(NJ_UINT16 request, NJ_DIC_HANDLE rule,
                                   NJ_CHAR *yomi, NJ_UINT16 len,
                                   NJ_UINT32 *current, NJ_RESULT *giji) {

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
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_GIJI_RESULT, NJ_ERR_PARAM_RESULT_NULL);
    }

    /*
     * 候補の品詞番号をルール辞書から取得する
     */
    fpos = njd_r_get_hinsi(rule, NJ_HINSI_GIJI_F);  /* 前品詞：擬似 の品詞番号取得 */
    bpos = njd_r_get_hinsi(rule, NJ_HINSI_GIJI_B);  /* 後品詞：擬似 の品詞番号取得 */

    if ((fpos == 0) || (bpos == 0)) {
        /* 擬似 品詞番号取得失敗 */
        return 0;
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

    /* 数字繋がり変換候補取得処理 */
    ret = njg_nmc_get_giji(request, fpos, bpos, yomi, len, current, giji);

    return ret;
}

/**
 * 数字繋がり変換候補取得処理
 *
 * @param[in]     request iWnnからの処理要求
 * @param[in]     gfpos   擬似品詞(擬似) 前品詞番号
 * @param[in]     gbpos   擬似品詞(擬似) 後品詞番号
 * @param[in]     yomi    候補 読み
 * @param[in]     len     読み文字長
 * @param[in,out] current 検索位置
 * @param[out]    p       候補格納バッファ
 *
 * @retval 1  候補を作成できた
 * @retval 0  候補を作成できない
 */
static NJ_INT16 njg_nmc_get_giji(NJ_UINT16 request, NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                 NJ_CHAR *yomi, NJ_UINT16 len,
                                 NJ_UINT32 *current, NJ_RESULT *p) {

    NJ_UINT16 len0;

    /*
     * 候補が作成できるか、チェックする。
     */
    len0 = njg_nmc_chk_giji(request, yomi, len, current);

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
 * 数字繋がり変換候補作成チェック処理
 *
 * @param[in]     request iWnnからの処理要求
 * @param[in]     pY      読み文字列
 * @param[in]     len     読み文字列の文字長
 * @param[in,out] current 検索位置
 *
 * @retval  >0  作成できる候補の文字列長
 * @retval   0  作成できない
 */
static NJ_INT16 njg_nmc_chk_giji(NJ_UINT16 request, NJ_CHAR *pY, NJ_UINT16 len, 
                                 NJ_UINT32 *current) {

    PART_OF_NJ_ENV tmp;
    NJ_UINT16 len0;
    NJ_INT16  i, j;
    NJ_UINT16 sucnt;
    NJ_UINT16 cnt = 0;     /* 確認用ループカウンター */
    NJ_UINT16 slen = 0;
    NJ_UINT16 tlen = 0;
    NJ_UINT8 sutype;
    NJ_UINT8 last_suuji_type = 0;
    NJ_UINT32 work;
    NJ_INT16 left, right, mid = 0;
    NJ_INT16 cmp;
    NJ_UINT16 findflg;
    NJ_UINT16 tblidx;
    NJ_UINT32 num = 0;

    /* 作成する数字タイプ */
    sutype = NJG_NMC_GET_SUUJI_TYPE(*current);
    if (request == NJG_OP_SEARCH_NEXT) {
        /* 次候補の場合は数字タイプを反転する */
        sutype = (NJ_UINT8)((sutype == 0) ? 1 : 0);
    }

    /*
     * 候補が作成できるか、チェックする。
     * 
     * 以下の処理を同時に行う。
     *   ・読み文字を1文字ずつ数字文字であるかをチェックする。
     *   ・全角数字と半角数字が混在した場合は候補作成不可とする。
     *   ・数字文字以外が出現した残りの文字を読み文字とする。
     *   ・読み文字が0文字の場合は、候補作成不可とする。
     *   ・読み文字がテーブルの読み文字と一致するかをチェックする。
     */
    len0 = 0;
    sucnt = 0;
    while (cnt < len) {
#ifndef NJ_OPT_UTF16
        if (NJG_NMC_IS_WCHAR(*pY)) {
            /* 2バイト文字の場合 */
#endif /* !NJ_OPT_UTF16 */
            for (i = 0; i < NJG_NM_SUUJI_REC; i++) {
                if (nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_ZEN][i] == NJG_NMC_CONV_TO_WCHAR(pY)) {
                    /* 全角数字と一致した */
                    if ((len0 != 0) && (last_suuji_type != NJG_NMC_SUUJI_ZEN)) {
                        /* 数字部分に全角半角が混在する為作成不可とする */
                        return 0;
                    }
                    slen += NJG_NMC_ZEN_MOJI_LEN;
                    last_suuji_type = NJG_NMC_SUUJI_ZEN;
                    break;
                }
#ifdef NJ_OPT_UTF16
                if (nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_HAN][i] == NJG_NMC_CONV_TO_WCHAR(pY)) {
                    /* 半角数字と一致した */
                    if ((len0 != 0) && (last_suuji_type != NJG_NMC_SUUJI_HAN)) {
                        /* 数字部分に全角半角が混在する為作成不可とする */
                        return 0;
                    }
                    slen += NJG_NMC_HAN_MOJI_LEN;
                    last_suuji_type = NJG_NMC_SUUJI_HAN;
                    break;
                }
#endif /* NJ_OPT_UTF16 */
            }
            if (i < NJG_NM_SUUJI_REC) {
                /* 数字である */
                tmp.gijiwork[sucnt++] = (NJ_UINT8)i;
                len0 += NJG_NMC_GET_SUUJI_LEN(sutype);
            } else {
                /* 数字以外 */
                break;
            }
            pY += NJG_NMC_ZEN_MOJI_LEN;
            cnt += NJG_NMC_ZEN_MOJI_LEN;
#ifndef NJ_OPT_UTF16
        } else {
            /* 1バイト文字の場合 */
            for (i = 0; i < NJG_NM_SUUJI_REC; i++) {
                if (nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_HAN][i] == (NJ_UINT16)(pY[0] | 0x0000)) {
                    /* 半角数字と一致した */
                    if ((len0 != 0) && (last_suuji_type != NJG_NMC_SUUJI_HAN)) {
                        /* 数字部分に全角半角が混在する為作成不可とする */
                        return 0;
                    }
                    slen += NJG_NMC_HAN_MOJI_LEN;
                    last_suuji_type = NJG_NMC_SUUJI_HAN;
                    break;
                }
            }

            if (i < NJG_NM_SUUJI_REC) {
                /* 数字である */
                tmp.gijiwork[sucnt++] = (NJ_UINT8)i;
                len0 += NJG_NMC_GET_SUUJI_LEN(sutype);
            } else {
                /* 数字以外 */
                break;
            }
            pY += NJG_NMC_HAN_MOJI_LEN;
            cnt += NJG_NMC_HAN_MOJI_LEN;
        }
#endif /* !NJ_OPT_UTF16 */
    }

    /* 数字部分が有効桁数以内かをチェック */
    tmp.gw_len = sucnt;
    if (tmp.gw_len > NJG_NM_LIMIT_MAX_LEN) {
        /* 候補作成不可 */
        return 0;
    }

    /* 数字部分の数値を取得 */
    for (i = 0; i < tmp.gw_len; i++) {
        work = 1;
        for (j = 0; j < (NJ_INT16)(tmp.gw_len - i - 1); j++) {
            work *= 10;
        }
        num += (NJ_UINT32)(tmp.gijiwork[i] * work);
    }


    /* 残りの文字列長 */
    tlen = len - slen;

    if ((cnt > 0) && (tlen > 0)) {
        /* 数字部分と繋がり部分に分解できた */

        if (request == NJG_OP_SEARCH) {
            /* 初回検索の場合は、変換インデックスを２分検索する */
            left = 0;
            right = NJG_NM_CONV_IDX_MAX - 1;
            findflg = 0;
            while (left <= right) {
                mid = left + ((right - left) / 2);

                /* 読み文字とテーブルの読み文字を比較する */
                cmp = njg_nmc_cmp_yomi(pY, tlen, &num_conn_tbl[conv_idx_tbl[mid]]);

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
                *current = mid;

                /* 前方向に同じ読み文字を検索する */
                while (*current > 0) {
                    /* インデックステーブルの1つ前の候補をチェック */
                    cmp = njg_nmc_cmp_yomi(pY, tlen, &num_conn_tbl[conv_idx_tbl[*current - 1]]);
                    if (cmp != 0) {
                        break;
                    }
                    (*current)--;
                }

                /* 候補の範囲チェックを行う */
                while (*current < NJG_NM_CONV_IDX_MAX) {
                    tblidx = NJG_NMC_GET_TABLE_INDEX(*current);
                    /* 範囲チェックOKなら処理結果を作成 */
                    if ((NJG_NM_IS_CHK_CONV(num_conn_tbl[tblidx].info) == 0) ||
                        ((NJG_NM_IS_CHK_CONV(num_conn_tbl[tblidx].info) == NJG_NM_CHK_CONV_MASK) && (NJG_NM_IS_RANGE(num, tblidx) == 1))) {
                       break;
                    }
                    if (*current >= (NJG_NM_CONV_IDX_MAX - 1)) {
                        /* 最後まで到達した為、候補作成不可とする */
                        return 0;
                    }
                    (*current)++;
                    cmp = njg_nmc_cmp_yomi(pY, tlen, &num_conn_tbl[conv_idx_tbl[*current]]);
                    if (cmp != 0) {
                        /* 異なる読み文字が出現した */
                        return 0;
                    }
                }
            } else {
                /* 候補作成不可 */
                return 0;
            }
        } else if (request == NJG_OP_SEARCH_NEXT) {
            /* 次回検索 */
            if (sutype == 0) {
                /* 
                 * 次回検索で作成する数字タイプが0の場合は新しい候補となる為、
                 * 検索位置を設定し、読み文字一致、範囲チェックチェックを行う
                 */
                *current = (*current) - NJG_NM_CONV_IDX_MAX + 1;

                while (*current < NJG_NM_CONV_IDX_MAX) {
                    
                    /* 読み文字とテーブルの読み文字を比較する */
                    tblidx = NJG_NMC_GET_TABLE_INDEX(*current);
                    cmp = njg_nmc_cmp_yomi(pY, tlen, &num_conn_tbl[tblidx]);

                    if (cmp != 0) {
                        /* 候補作成不可 */
                        return 0;
                    }
                    
                    /* 範囲チェックOKなら処理結果を作成 */
                    if ((NJG_NM_IS_CHK_CONV(num_conn_tbl[tblidx].info) == 0) ||
                        ((NJG_NM_IS_CHK_CONV(num_conn_tbl[tblidx].info) == NJG_NM_CHK_CONV_MASK) && (NJG_NM_IS_RANGE(num, tblidx) == 1))) {
                       break;
                    }
 
                    (*current)++;
                }
                if (*current >= NJG_NM_CONV_IDX_MAX) {
                    /* 最後まで到達した為、候補作成不可とする */
                    return 0;
                }
            } else if (sutype == 1) {
                /* 
                 * 次回検索で作成する数字タイプが1の場合は0と同じ候補の為、
                 * 検索位置を数字タイプ1の値に設定するのみ(チェックは不要)
                 */
                *current += NJG_NM_CONV_IDX_MAX;
            }
        }

        /* 作成する候補の文字列長がNJ_MAX_RESULT_LEN を超えていなければ、候補作成可 */
        tblidx = NJG_NMC_GET_TABLE_INDEX(*current);
        len0 += num_conn_tbl[tblidx].kouho_len;
        if (len0 <= NJ_MAX_RESULT_LEN) {
            return len0;
        }
    }
        
    return 0;
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
static NJ_INT16 njg_nmc_cmp_yomi(NJ_CHAR *yomi, NJ_UINT16 len, const NJG_NUM_CONN_TBL *tbl) {

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
        NJG_NMC_COPY_INT16_TO_CHAR(dst, tbl->yomi[i]);
        dst += NJG_NMC_ZEN_MOJI_LEN;
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
static NJ_INT16 njg_nmc_get_candidate(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size) {

    NJ_INT16 len = 0;
    NJ_UINT16 tblidx;
    NJ_UINT8 sutype;

    /* 検索位置から数字繋がり候補テーブル要素番号、数字部分タイプを取得 */
    tblidx = NJG_NMC_GET_TABLE_INDEX(word->stem.loc.current);
    sutype = NJG_NMC_GET_SUUJI_TYPE(word->stem.loc.current);

    /* 候補表記取得 */
    len = njg_nmc_get_candidate_string(word, candidate, &num_conn_tbl[tblidx], sutype);

    return len;
}

/**
 * 数字繋がり変換候補の共通作成処理
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
static NJ_INT16 njg_nmc_get_candidate_string(NJ_WORD *word, NJ_CHAR *candidate,
                                             const NJG_NUM_CONN_TBL *ctbl, NJ_UINT16 sutype) {

    NJ_CHAR   *yomi;
    NJ_CHAR   *dst;
    NJ_UINT16 len;
    NJ_UINT16 i;
    NJ_UINT16 cnt = 0;     /* 確認用ループカウンター */

    yomi = word->yomi;
    dst = candidate;

    len  = NJ_GET_YLEN_FROM_STEM(word);

    /* 数字部分を格納 */
    while (cnt < len) {
#ifndef NJ_OPT_UTF16
        if (NJG_NMC_IS_WCHAR(*yomi)) {
            /* 2バイト文字の場合 */
#endif /* !NJ_OPT_UTF16 */
            for (i = 0; i < NJG_NM_SUUJI_REC; i++) {
                if (nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_ZEN][i] == NJG_NMC_CONV_TO_WCHAR(yomi)) {
                    /* 全角数字と一致した */
                    break;
                }
#ifdef NJ_OPT_UTF16
                if (nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_HAN][i] == NJG_NMC_CONV_TO_WCHAR(yomi)) {
                    /* 半角数字と一致した */
                    break;
                }
#endif /* NJ_OPT_UTF16 */
            }
            if (i < NJG_NM_SUUJI_REC) {
                /* 数字の場合は作成する数字タイプを格納する */
                if (NJG_NMC_IS_SUUJI_ZEN(sutype)) {
                    NJG_NMC_COPY_INT16_TO_CHAR(dst, nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_ZEN][i]);
                    dst += NJG_NMC_ZEN_MOJI_LEN;
                } else {
#ifdef NJ_OPT_UTF16
                    NJG_NMC_COPY_INT16_TO_CHAR(dst, nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_HAN][i]);
#else /* NJ_OPT_UTF16 */
                    *dst = (NJ_UINT8)(nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_HAN][i] & 0xff);
#endif /* NJ_OPT_UTF16 */
                    dst += NJG_NMC_HAN_MOJI_LEN;
                }

            } else {
                /* 数字文字以外 */
                break;
            }
            yomi += NJG_NMC_ZEN_MOJI_LEN;
            cnt += NJG_NMC_ZEN_MOJI_LEN;
#ifndef NJ_OPT_UTF16
        } else {
            /* 1バイト文字の場合 */
            for (i = 0; i < NJG_NM_SUUJI_REC; i++) {
                if (nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_HAN][i] == (NJ_UINT16)(yomi[0] | 0x0000)) {
                    /* 半角数字と一致した */
                    break;
                }
            }
            if (i < NJG_NM_SUUJI_REC) {
                /* 数字の場合は作成する数字タイプを格納する */
                if (NJG_NMC_IS_SUUJI_ZEN(sutype)) {
                    NJG_NMC_COPY_INT16_TO_CHAR(dst, nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_ZEN][i]);
                    dst += NJG_NMC_ZEN_MOJI_LEN;
                } else {
                    *dst = (NJ_UINT8)(nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_HAN][i] & 0xff);
                    dst += NJG_NMC_HAN_MOJI_LEN;
                }
            } else {
                /* 数字文字以外 */
                break;
            }
            yomi += NJG_NMC_HAN_MOJI_LEN;
            cnt += NJG_NMC_HAN_MOJI_LEN;
        }
#endif /* !NJ_OPT_UTF16 */
    }

    /* 数字繋がり部分を格納 */
    for (i = 0; i < (NJ_UINT16)((ctbl->kouho_len * sizeof(NJ_CHAR)) / sizeof(NJ_UINT16)); i++) {
#ifdef NJ_OPT_UTF16
        NJG_NMC_COPY_INT16_TO_CHAR(dst, ctbl->kouho[i]);
        dst += NJG_NMC_ZEN_MOJI_LEN;
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
