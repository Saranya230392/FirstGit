/**
 * @file
 *  [拡張] 数字繋がり予測候補作成辞書 (UTF16/SJIS版)
 *
 * 数字繋がり予測候補の作成を行う辞書。
 *
 * @author
 * (C) OMRON SOFTWARE Co., Ltd. 2009-2012 All Rights Reserved.
 */
#include "ex_nmfgiji.h"
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
#define NJG_NMF_HAN_MOJI_LEN  1
/** 全角文字長 (UTF16) */
#define NJG_NMF_ZEN_MOJI_LEN  1

#else /* NJ_OPT_UTF16 */
/** 半角文字長 (SJIS) */
#define NJG_NMF_HAN_MOJI_LEN  1
/** 全角文字長 (SJIS) */
#define NJG_NMF_ZEN_MOJI_LEN  2
#endif /* NJ_OPT_UTF16 */

/*
 * 確定文字分割情報文字種
 */
/** 文字種: 数字 */
#define NJG_NMF_CHAR_TYPE_SUUJI      1
/** 文字種: 数字以外 */
#define NJG_NMF_CHAR_TYPE_NOT_SUUJI  2

/*
 * 数字部分タイプ定義
 */
/** 数字部分タイプ 半角 */
#define NJG_NMF_SUUJI_HAN       1
/** 数字部分タイプ 全角 */
#define NJG_NMF_SUUJI_ZEN       2

/** 数字繋がり予測辞書頻度段階 */
#define NMF_DIC_FREQ_DIV 100

/*
 * 検索位置最大数定義
 */
#define NJG_NMF_SEARCH_MAX (NJG_NM_RPRD_IDX_MAX + NJG_NM_PRED_IDX_MAX)

/* 内部用マクロ定義 */
/**
 * 検索位置から数字繋がり候補テーブル要素番号を取得。
 *
 * @param[in]      x  検索位置
 *
 * @return 数字繋がり候補テーブル要素番号 (NJ_UINT16)
 */
#define NJG_NMF_GET_TABLE_INDEX(x) (NJ_UINT16)((NJG_NMF_IS_RPRD_POS(x) == 1) ? rprd_idx_tbl[(x)] : pred_idx_tbl[(x) - NJG_NM_RPRD_IDX_MAX])
/**
 * 検索位置が前候補対象位置かをチェックする。
 *
 * @param[in]      x  検索位置
 *
 * @retval         0  前候補対象領域でない
 * @retval        !0  前候補対象領域
 */
#define NJG_NMF_IS_RPRD_POS(x) (((x) < NJG_NM_RPRD_IDX_MAX) ? 1 : 0)


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
#define NJG_NMF_CONV_TO_WCHAR(x)                                            \
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
#define NJG_NMF_IS_WCHAR(x)  1
#else /* NJ_OPT_UTF16 */
/** SJIS版 */
#define NJG_NMF_IS_WCHAR(x)                                                 \
    (((((x) >= 0x81) && ((x) <= 0x9f)) || (((x) >= 0xe0) && ((x) <= 0xfc))) ? 1 : 0)
#endif/* NJ_OPT_UTF16 */

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
#define NJG_NMF_COPY_W_CHAR(to, from)               \
    { *(to) = *(from); }
#else /* NJ_OPT_UTF16 */
#define NJG_NMF_COPY_W_CHAR(to, from)                       \
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
#define NJG_NMF_COPY_INT16_TO_CHAR(to, from)                                \
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

/**
 * 確定文字分割情報
 */
typedef struct {
    NJ_CHAR hyouki[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];  /**< 表記文字 */
    NJ_UINT16 len;                                    /**< 表記文字長 */
    NJ_UINT8 char_type;                               /**< 文字種 */
    NJ_UINT32 num;                                    /**< 数値 */
} NJG_NMF_DIV_WORK;

/***********************************************************************
 * 辞書インタフェース プロトタイプ宣言
 ***********************************************************************/

/***********************************************************************
 * 内部関数 プロトタイプ宣言
 ***********************************************************************/
static NJ_INT16 njg_nmf_get_result(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_DIC_HANDLE rule,
                                   NJG_NMF_DIVINF *divinfo, NJ_CHAR *yomi, NJ_UINT16 len,
                                   NJ_UINT16 curpos, NJ_RESULT *giji);
static NJ_INT16 njg_nmf_get_giji(NJ_CLASS *iwnn, NJ_UINT16 request, NJG_NMF_DIVINF *divinfo,
                                 NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                 NJ_CHAR *yomi, NJ_UINT16 len,
                                 NJ_UINT16 curpos, NJ_RESULT *p);
static NJ_INT16 njg_nmf_cmp_kouho(NJ_CHAR *hyouki, const NJG_NUM_CONN_TBL *tbl);
static NJ_UINT16 njg_nmf_div_prv(NJ_CHAR *hyouki, NJ_UINT16 len, NJG_NMF_DIV_WORK* divinf);
static NJ_INT16 njg_nmf_chk_yomi_match(NJ_CHAR *yomi, NJ_UINT16 len, const NJG_NUM_CONN_TBL *tbl,
                                       NJ_CHARSET* charset);
static NJ_UINT32 njg_nmf_get_num(PART_OF_NJ_ENV *tmp);
static NJ_INT16 njg_nmf_get_stroke(NJ_WORD *word, NJ_CHAR *stroke, NJ_UINT16 size);
static NJ_INT16 njg_nmf_get_candidate(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size);
static NJ_INT16 njg_nmf_get_candidate_string(NJ_WORD *word, NJ_CHAR *candidate, 
                                             const NJG_NUM_CONN_TBL *tbl);

/***********************************************************************/

/**
 * 数字繋がり予測辞書 辞書インタフェース
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
NJ_EXTERN NJ_INT16 njex_nmfgiji_dic(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE *message) {

    NJ_INT16 ret;
    NJ_UINT16 i;
    NJ_RESULT rlt;
    NJ_CHAR *yomi;
    NJ_UINT16 ylen;
    NJ_HINDO hindo;
    NJ_UINT16 tblidx;
    NJ_UINT16 len;
    NJG_NMF_DIVINF *divinf;

    if ((request == NJG_OP_SEARCH) || (request == NJG_OP_SEARCH_NEXT) || (request == NJG_OP_GET_WORD_INFO)) {
        if ((message->dic_idx < NJ_MAX_DIC) &&
            (iwnn->dic_set.dic[message->dic_idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] == NULL)) {
            /* 拡張領域がNULLの場合は、候補作成不可とする */
            message->location->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        } else {
            /* 拡張領域を確定情報記憶領域として扱う */
            divinf = (NJG_NMF_DIVINF *)(iwnn->dic_set.dic[message->dic_idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
        }
    } else {
        /* 拡張領域を使用しない */
        divinf = NULL;
    }

    switch (request) {
    case NJG_OP_SEARCH:
        if (((message->condition->operation != NJ_CUR_OP_LINK) &&
            (message->condition->operation != NJ_CUR_OP_FORE)) ||
            (message->condition->mode != NJ_CUR_MODE_FREQ) ||
            (iwnn->njc_mode != NJC_MODE_NOT_CONVERTED) ||
            (iwnn->previous_selection.count == 0)) {
            /* つながり検索、正引き前方一致、頻度順、前確定候補ありのみ対応 */
            message->location->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }

        if (divinf != NULL) {
            divinf->divnum = 0;
        }

        if (((iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH) ||
            (iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI))) {
            /* 絞込予測 */
            yomi = message->condition->yomi;
            ylen = message->condition->ylen;
        } else {
            /* 繋がり予測 */
            if (message->condition->kanji == NULL) {
                /* 候補作成不可 */
                message->location->loct.status = NJ_ST_SEARCH_END;
                return 0;
            } else {
                yomi = message->condition->kanji;
                ylen = nj_strlen(message->condition->kanji);
            }
        }

        for (i = 0; i < NJG_NMF_SEARCH_MAX; i++) {
            /* 候補を作ってみる */
            ret = njg_nmf_get_result(iwnn, request, message->dicset->rHandle[0], divinf, yomi, ylen, i, &rlt);

            if (ret == 0) {
                /* 指定された候補は作成不可 */
                continue;
            } else if (ret == 1) {
                /* 候補作成可能 */
                message->location->loct.current = i;
                message->location->loct.current_info = 0x10;
                /* 頻度を設定する */
                if (NJG_NMF_IS_RPRD_POS(i) != 0) {
                    /* 数字+候補+数字に繋がる候補が作成された場合 */
                    hindo = message->location->dic_freq.high;
                } else {
                    /* 数字に繋がる候補が作成された場合 */
                    tblidx = NJG_NMF_GET_TABLE_INDEX(i);
                    hindo = CALCULATE_HINDO(num_conn_tbl[tblidx].pred_hindo, 
                                            message->location->dic_freq.base, message->location->dic_freq.high,
                                            NMF_DIC_FREQ_DIV);
                }
                NJ_SET_FREQ_TO_STEM(&(rlt.word), hindo);
                message->location->cache_freq = hindo; 
                message->location->loct.status = NJ_ST_SEARCH_READY;
                return 1;
            } else if (ret == 2) {
                /* 候補作成条件を満たさない為、検索終了 */
                message->location->loct.status = NJ_ST_SEARCH_END;
                return 0;
            } else if (ret < 0) {
                /* エラー */
                message->location->loct.status = NJ_ST_SEARCH_END;
                return ret;
            }
        }

        /* 候補作成不可 */
        message->location->loct.status = NJ_ST_SEARCH_END;
        return 0;

    case NJG_OP_SEARCH_NEXT:
        /* 次に作成可能な候補を求める */

        if (((iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH) ||
            (iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI))) {
            /* 絞込み */
            yomi = message->condition->yomi;
            ylen = message->condition->ylen;
        } else {
            /* 繋がり予測 */
            if (message->condition->kanji == NULL) {
                /* 候補作成不可 */
                message->location->loct.status = NJ_ST_SEARCH_END;
                return 0;
            } else {
                yomi = message->condition->kanji;
                ylen = nj_strlen(message->condition->kanji);
            }
        }

        for (i = (NJ_UINT16)(message->location->loct.current + 1); i < NJG_NMF_SEARCH_MAX; i++) {
            /* 候補を作ってみる */
            ret = njg_nmf_get_result(iwnn, request, message->dicset->rHandle[0],
                                     divinf, yomi, ylen, i, &rlt);
            if (ret == 0) {
                /* 指定された候補は作成不可 */
                continue;
            } else if (ret == 1) {
                /* 候補作成可能 */
                message->location->loct.current = i;
                message->location->loct.current_info = 0x10;
                /* 頻度を設定する */
                if (NJG_NMF_IS_RPRD_POS(i) != 0) {
                    /* 数字+候補+数字に繋がる候補が作成された場合 */
                    hindo = message->location->dic_freq.high;
                } else {
                    /* 数字に繋がる候補が作成された場合 */
                    tblidx = NJG_NMF_GET_TABLE_INDEX(i);
                    hindo = CALCULATE_HINDO(num_conn_tbl[tblidx].pred_hindo, 
                                            message->location->dic_freq.base, message->location->dic_freq.high,
                                            NMF_DIC_FREQ_DIV);
                }
                NJ_SET_FREQ_TO_STEM(&(rlt.word), hindo);
                message->location->cache_freq = hindo; 
                message->location->loct.status = NJ_ST_SEARCH_READY;
                return 1;
            } else if (ret == 2) {
                /* 候補作成条件を満たさない為、検索終了 */
                message->location->loct.status = NJ_ST_SEARCH_END;
                return 0;
            } else if (ret < 0) {
                /* エラー */
                message->location->loct.status = NJ_ST_SEARCH_END;
                return ret;
            }
        } 
        /* 候補作成不可 */
        message->location->loct.status = NJ_ST_SEARCH_END;
        return 0;

    case NJG_OP_GET_WORD_INFO:
        /* 候補タイプを取得して候補を作成する */
        if ((NJ_UINT16)message->location->loct.current >= NJG_NMF_SEARCH_MAX) {
            /* 念のため候補の範囲をチェック。ココを通ることはない */
            return -1;
        }
        /* 候補を作成する */
        ret = njg_nmf_get_result(iwnn, request, message->dicset->rHandle[0], divinf,
                                 message->word->yomi, NJ_GET_YLEN_FROM_STEM(message->word),
                                 (NJ_UINT16)message->location->loct.current, &rlt);
        if (ret == 1) {
            /* 候補作成可能なはず */
            *(message->word) = rlt.word;
            /* 単語情報の頻度を設定する */
            if (NJG_NMF_IS_RPRD_POS(message->location->loct.current) != 0) {
                /* 数字+候補+数字に繋がる候補が作成された場合 */
                hindo = message->location->dic_freq.high;
            } else {
                /* 数字に繋がる候補が作成された場合 */
                tblidx = NJG_NMF_GET_TABLE_INDEX(message->location->loct.current);
                hindo = CALCULATE_HINDO(num_conn_tbl[tblidx].pred_hindo, 
                                        message->location->dic_freq.base, message->location->dic_freq.high,
                                        NMF_DIC_FREQ_DIV);
            }
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
        ret = njg_nmf_get_stroke(message->word, message->stroke, message->stroke_size);
        return ret;

    case NJG_OP_GET_STRING:
        /* 表記文字列を作成する */
        len = NJ_GET_KLEN_FROM_STEM(message->word);
        if (message->string_size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
            /* 格納領域に収まらない場合はエラー */
            return -1;
        }
        ret = njg_nmf_get_candidate(message->word, message->string, message->string_size);
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
 * 数字繋がり予測候補を作成する。
 *
 * @param[in]  iwnn    解析情報クラス
 * @param[in]  request iWnnからの処理要求
 * @param[in]  rule    ルール辞書ハンドル
 * @param[in]  divinf  分割情報
 * @param[in]  yomi    読み文字
 * @param[in]  len     読み文字長
 * @param[in]  curpos  検索位置
 * @param[out] giji    候補格納領域
 *
 * @retval  2  検索終了
 * @retval  1  指定された候補が作成できた
 * @retval  0  指定された候補が作成できない
 * @retval <0 エラー
 */
static NJ_INT16 njg_nmf_get_result(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_DIC_HANDLE rule,
                                   NJG_NMF_DIVINF *divinf, NJ_CHAR *yomi, NJ_UINT16 len,
                                   NJ_UINT16 curpos, NJ_RESULT *giji) {

    NJ_UINT16 fpos, bpos;
    NJ_INT16 ret;
    NJ_INT16 i;

    /*
     * 引数チェック
     */
    if (rule == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_GIJI_RESULT, NJ_ERR_NO_RULEDIC);
    }
    if (((iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH) ||
        (iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI)) &&
        ((yomi == NULL) || (len == 0))) {
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

    /* 数字繋がり予測候補取得処理 */
    ret = njg_nmf_get_giji(iwnn, request, divinf, fpos, bpos, yomi, len, curpos, giji);

    return ret;
}

/**
 * 数字繋がり予測候補取得処理
 *
 * @param[in]  iwnn    解析情報クラス
 * @param[in]  request iWnnからの処理要求
 * @param[in]  divinf  分割情報
 * @param[in]  gfpos   擬似品詞(擬似) 前品詞番号
 * @param[in]  gbpos   擬似品詞(擬似) 後品詞番号
 * @param[in]  yomi    読み文字
 * @param[in]  len     読み文字長
 * @param[in]  curpos  検索位置
 * @param[out] p       候補格納バッファ
 *
 * @retval  2  検索終了
 * @retval  1  指定された候補が作成できた
 * @retval  0  指定された候補が作成できない
 */
static NJ_INT16 njg_nmf_get_giji(NJ_CLASS *iwnn, NJ_UINT16 request, NJG_NMF_DIVINF *divinf,
                                 NJ_UINT16 gfpos, NJ_UINT16 gbpos,
                                 NJ_CHAR *yomi, NJ_UINT16 len,
                                 NJ_UINT16 curpos, NJ_RESULT *p) {
    NJ_UINT16 ret;
    NJ_CHAR* pH;
    NJ_UINT16 rflg = 0;
    NJ_UINT16 rindex;
    NJ_UINT16 tblidx;
    NJ_PREVIOUS_SELECTION_INFO *prvinf;
    NJG_NMF_DIV_WORK divwork[3];
    NJ_CHAR hyouki[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_CHAR work[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_INT8 pos;
    NJ_UINT16 i;
    NJ_UINT16 len0, hlen;
    NJ_UINT8 flg;

    /* 検索位置から数字繋がり候補テーブルの要素番号を取得 */
    tblidx = NJG_NMF_GET_TABLE_INDEX(curpos);

    /* 確定情報を数字と数字以外に分割する */
    if (((iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH) ||
        (iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI))) {
        /* 絞込予測 */

        /* 読み文字が作成する候補の読み文字と前方一致するかを判定 */
        if (njg_nmf_chk_yomi_match(yomi, len, &num_conn_tbl[tblidx],
                                   iwnn->environment.cursor.cond.charset) == 0) {
            /* 読み文字不一致の為、候補作成不可 */
            return 0;
        }

        if ((request == NJG_OP_SEARCH) && (divinf->divnum == 0)) {
            /* 初回検索の場合は前確定情報の分割処理を行う */

            prvinf = &iwnn->previous_selection;
            pos = prvinf->selection_now;
            nj_strcpy(hyouki, prvinf->selection_data[pos].hyouki);
            len0 = prvinf->selection_data[pos].hyouki_len;
            i = 0;
            flg = 0;
            while (i < prvinf->count) {
                /* 前確定情報を数字と文字に分割する */
                ret = njg_nmf_div_prv(hyouki, len0, divwork);
                if (ret == 0) {
                    if (flg == 0) {
                        /* チェックOKがない為、候補作成不可 */
                        return 2;
                    } else if (flg == 1) {
                        /* チェックOKがある為、ループから抜ける */
                        break;
                    }
                } else if ((ret == 3) &&
                           (divwork[0].char_type == NJG_NMF_CHAR_TYPE_SUUJI) &&
                           (divwork[1].char_type == NJG_NMF_CHAR_TYPE_NOT_SUUJI) &&
                           (divwork[2].char_type == NJG_NMF_CHAR_TYPE_SUUJI)) {

                    /* 数字+候補+数字に分割できた */
                    divinf->divnum = 3;
                    divinf->bnum = divwork[2].num;
                    divinf->fnum = divwork[0].num;
                    nj_strcpy(divinf->fhyouki, divwork[1].hyouki);
                    divinf->fhyouki_len = divwork[1].len;
                    flg = 1;
                } else if (ret == 1) {
                    /* 数字に分割できた */
                    divinf->divnum = 1;
                    divinf->bnum = divwork[0].num;
                    divinf->fnum = 0;
                    flg = 1;
                }

                i++;

                if (i < prvinf->count) {
                    /* １つ前の前確定情報が存在する */
                    if ((prvinf->selection_now < (prvinf->count - 1)) && (pos == 0)) {
                        pos = prvinf->count - 1;
                    } else {
                        pos--;
                    }

                    /* 
                     * 結合する前確定情報の文節数を増やした場合に、
                     * 結合後の表記文字列長が最大値(NJ_MAX_RESULT_LEN)を超えないかチェック
                     */
                    if ((len0 + prvinf->selection_data[pos].hyouki_len) > NJ_MAX_RESULT_LEN) {
                        /*
                         * チェック前の状態で、分割処理が正常であった場合、
                         * ループを抜け、チェック前の前確定情報の状態で候補作成可とする。
                         */
                        break;
                    }
                    
                    /* 結合済みの表記文字列を一時保存 */
                    nj_strcpy(work, hyouki);

                    /* 1つ前の前確定情報の表記文字を先頭にコピーする */
                    pH = hyouki;
                    nj_strcpy(pH, prvinf->selection_data[pos].hyouki);
                    hlen = prvinf->selection_data[pos].hyouki_len;
                    pH += hlen;

                    nj_strcpy(pH, work);
                    len0 += hlen;
                }
            }
        }
    } else {
        /* 繋がり予測 */

        if ((request == NJG_OP_SEARCH) && (divinf->divnum == 0)) {
            /* 確定情報を数字と文字に分割する */
            ret = njg_nmf_div_prv(yomi, len, divwork);
            if (ret == 0) {
                /* 候補作成不可 */
                return 2;
            } else if ((ret == 3) && (NJG_NMF_IS_RPRD_POS(curpos) != 0)) {
                /* 3分割で数字+候補+数字に繋がる候補を作成する場合 */
                divinf->divnum = 3;
                divinf->bnum = divwork[2].num;
                divinf->fnum = divwork[0].num;
                nj_strcpy(divinf->fhyouki, divwork[1].hyouki);
                divinf->fhyouki_len = divwork[1].len;
            } else if ((ret == 1) && (NJG_NMF_IS_RPRD_POS(curpos) == 0)) {
                /* 1分割で数字に繋がる候補を作成する場合 */
                divinf->divnum = 1;
                divinf->bnum = divwork[0].num;
            } else {
                /* 分割数と作成候補の対象分割数が不一致 */
                return 0;
            }
        }
    }
    
    if (NJG_NMF_IS_RPRD_POS(curpos) != 0) {
        /* 検索位置が数字+候補+数字に繋がる候補作成の場合 */
        if (divinf->divnum == 3) {
            /* 前候補の要素番号を取得 */
            rindex = num_conn_tbl[tblidx].rindex;
            /* 候補部分の文字と前候補が一致するかチェックする */
            pH = divinf->fhyouki;
            rflg = 0;
            if (divinf->fhyouki_len == num_conn_tbl[rindex].kouho_len) {
                /* 文字長が一致 */
                if (njg_nmf_cmp_kouho(pH, &num_conn_tbl[rindex]) == 0) {
                    /* 文字列が一致 */
                    if ((NJG_NM_IS_CHK_PRED(num_conn_tbl[rindex].info) == 0) ||
                        ((NJG_NM_IS_CHK_PRED(num_conn_tbl[rindex].info) == NJG_NM_CHK_PRED_MASK) && (NJG_NM_IS_RANGE(divinf->fnum, rindex) == 1))) {
                        /* 候補作成可能フラグを立てる */
                        rflg = 1;
                    }
                }
            }
            if (rflg == 0) {
                /* 候補作成不可 */
                return 0;
            }
        } else {
            /* 確定文字が数字＋候補＋数字でない */
            return 0;
        }
    }

    /* 作成する候補の直前の数字のチェックを行う */
    if ((NJG_NM_IS_CHK_PRED(num_conn_tbl[tblidx].info) == NJG_NM_CHK_PRED_MASK) &&
        (NJG_NM_IS_RANGE(divinf->bnum, tblidx) == 0)) {
        /* 範囲チェックNG */
        return 0;
    }

    /* 候補を格納 */
    p->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI);
    p->word.yomi = yomi;
    NJ_SET_YLEN_TO_STEM(&p->word, num_conn_tbl[tblidx].yomi_len);
    NJ_SET_KLEN_TO_STEM(&p->word, num_conn_tbl[tblidx].kouho_len);
    NJ_SET_FPOS_TO_STEM(&p->word, gfpos);
    NJ_SET_BPOS_TO_STEM(&p->word, gbpos);
    NJ_SET_TYPE_TO_STEM(&p->word, NJ_TYPE_NONE);

    return 1;
}

/**
 * 表記文字比較処理
 *
 * @param[in]  hyouki 表記文字列
 * @param[in]  tbl    数字繋がり候補テーブル
 *
 * @retval  >0     表記文字列 > テーブル候補文字列
 * @retval  =0     表記文字列 = テーブル候補文字列
 * @retval  <0     表記文字列 < テーブル候補文字列
 */
static NJ_INT16 njg_nmf_cmp_kouho(NJ_CHAR *hyouki, const NJG_NUM_CONN_TBL *tbl) {

    NJ_CHAR tblkouho[NJG_NM_KOUHO_MAX * 2];
    NJ_CHAR* dst;
    NJ_UINT16 i;

    dst = tblkouho;
    
    for (i = 0; i < (NJ_UINT16)((tbl->kouho_len * sizeof(NJ_CHAR)) / sizeof(NJ_UINT16)); i++) {
#ifdef NJ_OPT_UTF16
        NJG_NMF_COPY_INT16_TO_CHAR(dst, tbl->kouho[i]);
        dst += NJG_NMF_ZEN_MOJI_LEN;
#else /* NJ_OPT_UTF16 */
        if ((tbl->kouho[i] & 0xff00) != 0) {
            /* 全角文字なら上位バイトも格納 */
            *dst++ = (NJ_UINT8)((tbl->kouho[i] >> 8) & 0xff);
        }
        *dst++ = (NJ_UINT8)(tbl->kouho[i] & 0xff);
#endif /* NJ_OPT_UTF16 */
    }
    *dst = NJ_CHAR_NUL;

    return nj_strcmp(hyouki, tblkouho);
}

/**
 * 確定文字分割処理
 *
 * @param[in]  hyouki  確定表記文字
 * @param[in]  len     確定表記文字長
 * @param[out] divwork 確定解析情報
 *
 * @retval  >0     分割数
 * @retval  =0     候補作成不可
 */
static NJ_UINT16 njg_nmf_div_prv(NJ_CHAR *hyouki, NJ_UINT16 len, NJG_NMF_DIV_WORK* divwork) {

    PART_OF_NJ_ENV tmp;
    NJ_UINT16 i;
    NJ_UINT16 dividx = 0;
    NJ_UINT8 lastchar_type = 0;
    NJ_UINT8 lastsuuji_type = 0;
    NJ_UINT16 h = 0, cnt;
    NJ_CHAR *pH;

    /* 前確定情報を解析する */
    divwork[0].len = 0;
    divwork[1].len = 0;
    divwork[2].len = 0;
    
    pH = hyouki;
    cnt = 0;
    tmp.gw_len = 0;
    while (cnt < len) {
#ifndef NJ_OPT_UTF16
        if (NJG_NMF_IS_WCHAR(*pH)) {
            /* 2バイト文字の場合 */
#endif /* !NJ_OPT_UTF16 */
            lastchar_type = NJG_NMF_CHAR_TYPE_NOT_SUUJI;
            /* 数字と一致するか */
            for (i = 0; i < NJG_NM_SUUJI_REC; i++) {
                if (nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_ZEN][i] == NJG_NMF_CONV_TO_WCHAR(pH)) {
                   /* 全角数字と一致した */
                    if ((lastsuuji_type != 0) && (lastsuuji_type != NJG_NMF_SUUJI_ZEN)) {
                        /* 全角数字と半角数字が混在する */
                        return 0;
                    }
                    lastsuuji_type = NJG_NMF_SUUJI_ZEN;
                    break;
                }
#ifdef NJ_OPT_UTF16
                if (nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_HAN][i] == NJG_NMF_CONV_TO_WCHAR(pH)) {
                    /* 半角数字と一致した */
                    if ((lastsuuji_type != 0) && (lastsuuji_type != NJG_NMF_SUUJI_HAN)) {
                        /* 全角数字と半角数字が混在する */
                        return 0;
                    }
                    lastsuuji_type = NJG_NMF_SUUJI_HAN;
                    break;
                }
#endif /* !NJ_OPT_UTF16 */
            }
            if (i < NJG_NM_SUUJI_REC) {
                /* 数字と一致した */
                tmp.gijiwork[tmp.gw_len++] = (NJ_UINT8)i;
                lastchar_type = NJG_NMF_CHAR_TYPE_SUUJI;
            }
            if ((divwork[dividx].len != 0) && (divwork[dividx].char_type != lastchar_type)) {
                /* 文字種が変更した */
                divwork[dividx].hyouki[h] = NJ_CHAR_NUL;
                /* 前回の文字種が数字の場合は数値情報を格納 */
                if (divwork[dividx].char_type == NJG_NMF_CHAR_TYPE_SUUJI) {
                    if (tmp.gw_len > NJG_NM_LIMIT_MAX_LEN) {
                        /* 有効桁数オーバー */
                        return 0;
                    }
                    divwork[dividx].num = njg_nmf_get_num(&tmp);
                    lastsuuji_type = 0;
                    tmp.gw_len = 0;
                }
                if (dividx < 2) {
                    dividx++;
                } else {
                    /* 4分割目が出現した */
                    return 0;
                }
                h = 0;
                divwork[dividx].len = 0;
            }
            /* 前確定解析情報に格納 */
            divwork[dividx].hyouki[h++] = pH[0];
#ifndef NJ_OPT_UTF16
            divwork[dividx].hyouki[h++] = pH[1];
#endif /* !NJ_OPT_UTF16 */
            divwork[dividx].len += NJG_NMF_ZEN_MOJI_LEN;
            divwork[dividx].char_type = lastchar_type;
            cnt += NJG_NMF_ZEN_MOJI_LEN;
            pH += NJG_NMF_ZEN_MOJI_LEN;
#ifndef NJ_OPT_UTF16
        } else {
            /* 1バイト文字の場合 */
            lastchar_type = NJG_NMF_CHAR_TYPE_NOT_SUUJI;
            for (i = 0; i < NJG_NM_SUUJI_REC; i++) {
                if (nmgiji_suuji_tbl[NJG_NM_SUUJI_TBL_HAN][i] == (NJ_UINT16)(pH[0] | 0x0000)) {
                    /* 半角数字と一致した */
                    if ((lastsuuji_type != 0) && (lastsuuji_type != NJG_NMF_SUUJI_HAN)) {
                        /* 全角数字と半角数字が混在する */
                        return 0;
                    }
                    tmp.gijiwork[tmp.gw_len++] = (NJ_UINT8)i;
                    lastsuuji_type = NJG_NMF_SUUJI_HAN;
                    lastchar_type = NJG_NMF_CHAR_TYPE_SUUJI;
                    break;
                }
            }
            if ((divwork[dividx].len != 0) && (divwork[dividx].char_type != lastchar_type)) {
                /* 文字種が変更した */
                divwork[dividx].hyouki[h] = NJ_CHAR_NUL;
                /* 前回の文字種が数字の場合は数値情報を格納 */
                if (divwork[dividx].char_type == NJG_NMF_CHAR_TYPE_SUUJI) {
                    if (tmp.gw_len > NJG_NM_LIMIT_MAX_LEN) {
                        /* 有効桁数オーバー */
                        return 0;
                    }
                    divwork[dividx].num = njg_nmf_get_num(&tmp);
                    lastsuuji_type = 0;
                    tmp.gw_len = 0;
                }
                if (dividx < 2) {
                    dividx++;
                } else {
                    /* 4分割目が出現した */
                    return 0;
                }
                h = 0;
                divwork[dividx].len = 0;
            }
            /* 前確定解析情報に格納 */
            divwork[dividx].hyouki[h++] = pH[0];
            divwork[dividx].len += NJG_NMF_HAN_MOJI_LEN;
            divwork[dividx].char_type = lastchar_type;
            cnt += NJG_NMF_HAN_MOJI_LEN;
            pH += NJG_NMF_HAN_MOJI_LEN;
        }
#endif /* !NJ_OPT_UTF16 */
    }

    /* 最後の分割情報を更新 */
    if (divwork[dividx].char_type == NJG_NMF_CHAR_TYPE_SUUJI) {
        /* 文字種が数字の場合 */
        if (tmp.gw_len > NJG_NM_LIMIT_MAX_LEN) {
            /* 有効桁数オーバー */
            return 0;
        }
        divwork[dividx].num = njg_nmf_get_num(&tmp);
    } else {
        /* 直前の分割情報が数字でない */
        return 0;
    }
    divwork[dividx].hyouki[h] = NJ_CHAR_NUL;

    if (divwork[0].len == 0) {
        /* 分割できない */
        return 0;
    }

    return dividx + 1;
}

/**
 * 分割情報数値取得処理
 *
 * @param[in]  tmp    旧NJ_ENV構造体
 *
 * @retval  数値
 */
static NJ_UINT32 njg_nmf_get_num(PART_OF_NJ_ENV *tmp) {

    NJ_UINT32 num;
    NJ_UINT32 work;
    NJ_INT16  i, j;

    /* 数値に変換できる */
    num = 0;
    for (i = 0; i < tmp->gw_len; i++) {
        work = 1;
        for (j = 0; j < (NJ_INT16)(tmp->gw_len - i - 1); j++) {
            work *= 10;
        }
        num += (NJ_UINT32)(tmp->gijiwork[i] * work);
    }

    return num;
}

/**
 * 読み文字前方一致判定処理(曖昧検索対応)
 *
 * @param[in]  yomi          読み文字
 * @param[in]  len           読み文字長
 * @param[in]  tbl           数字繋がり候補作成テーブル<br>
 *                           作成する候補のテーブルのみセットされる。
 * @param[in]  charset       あいまい文字セット
 *
 * @retval  1  マッチングする
 * @retval  0  マッチングしない
 */
static NJ_INT16 njg_nmf_chk_yomi_match(NJ_CHAR *yomi, NJ_UINT16 len, const NJG_NUM_CONN_TBL *tbl,
                                       NJ_CHARSET* charset) {
    NJ_UINT16 cnt;
    NJ_UINT16 i;
    NJ_UINT16 tolen = 0;
    NJ_INT16  found = 0;
#ifdef NJ_OPT_UTF16
    NJ_CHAR tblyomi[NJG_NM_YOMI_MAX + NJ_TERM_LEN];
#else /* NJ_OPT_UTF16 */
    NJ_CHAR tblyomi[(NJG_NM_YOMI_MAX * 2) + NJ_TERM_LEN];
#endif /* NJ_OPT_UTF16 */
    NJ_CHAR* dst;
#ifdef NJ_OPT_CHARSET_2BYTE
    NJ_UINT16 start;
    NJ_UINT16 end;
#endif /* NJ_OPT_CHARSET_2BYTE */

    /* 読みの文字長が候補の文字長より長い場合、マッチングしない */
    if (len > tbl->yomi_len) {
        return 0;
    }

    dst = tblyomi;

    /* テーブル読み文字配列作成 */
    for (i = 0; i < (NJ_UINT16)((tbl->yomi_len * sizeof(NJ_CHAR)) / sizeof(NJ_UINT16)); i++) {
#ifdef NJ_OPT_UTF16
        NJG_NMF_COPY_INT16_TO_CHAR(dst, tbl->yomi[i]);
        dst += NJG_NMF_ZEN_MOJI_LEN;
#else /* NJ_OPT_UTF16 */
        if ((tbl->yomi[i] & 0xff00) != 0) {
            /* 全角文字なら上位バイトも格納 */
            *dst++ = (NJ_UINT8)((tbl->yomi[i] >> 8) & 0xff);
        }
        *dst++ = (NJ_UINT8)(tbl->yomi[i] & 0xff);
#endif /* NJ_OPT_UTF16 */
    }
    *dst = NJ_CHAR_NUL;

    dst = tblyomi;
    cnt = 0;
    while (cnt < len) {
        found = 0;
#ifndef NJ_OPT_UTF16
        if (NJG_NMF_IS_WCHAR(*yomi)) {
            /* 2バイト文字の場合 */
#endif /* !NJ_OPT_UTF16 */
            if (NJG_NMF_CONV_TO_WCHAR(yomi) != NJG_NMF_CONV_TO_WCHAR(dst)) {
                /* マッチングしない場合は曖昧検索でマッチするかチェックする */
                if (charset == NULL) {
                    /* あいまい文字セットが指定されていない為不一致とする */
                    return 0;
                }
#ifdef NJ_OPT_CHARSET_2BYTE
                /* yomiに一致するあいまい文字セットの範囲を２分検索する */
                if (njd_search_charset_range(charset, yomi, &start, &end) == 1) {
                    /* 範囲が見つかった */
                    for (i = start; i <= end; i++) {
#else /* NJ_OPT_CHARSET_2BYTE */
                /* あいまい文字セットが指定されている */
                for (i = 0; i < charset->charset_count; i++) {
                    if ((nj_strlen(charset->from[i]) == NJG_NMF_ZEN_MOJI_LEN) &&
                        (NJG_NMF_CONV_TO_WCHAR(yomi) == NJG_NMF_CONV_TO_WCHAR(charset->from[i]))) {
#endif /* NJ_OPT_CHARSET_2BYTE */
                        tolen = nj_strlen(charset->to[i]);
                        if (nj_strncmp(charset->to[i], dst, tolen) == 0) {
                            /* 曖昧検索で一致する為、一致と見なす */
                            found = 1;
                            break;
                        }
                    }
                }
                if (found == 1) {
                    /* 曖昧検索で一致した */
                    dst += tolen;
                } else {
                    /* 曖昧検索でも一致しなかった */
                    return 0;
                }
            } else {
                dst += NJG_NMF_ZEN_MOJI_LEN;
            }
            yomi += NJG_NMF_ZEN_MOJI_LEN;
            cnt += NJG_NMF_ZEN_MOJI_LEN;
#ifndef NJ_OPT_UTF16
        } else {
            /* 1バイト文字の場合 */
            if (*yomi != *dst) {
                /* マッチングしない場合は曖昧検索でマッチするかチェックする */
                if (charset == NULL) {
                    /* あいまい文字セットが指定されていない為不一致とする */
                    return 0;
                }
#ifdef NJ_OPT_CHARSET_2BYTE
                /* yomiに一致するあいまい文字セットの範囲を２分検索する */
                if (njd_search_charset_range(charset, yomi, &start, &end) == 1) {
                    /* 範囲が見つかった */
                    for (i = start; i <= end; i++) {
#else /* NJ_OPT_CHARSET_2BYTE */

                /* あいまい文字セットが指定されている */
                for (i = 0; i < charset->charset_count; i++) {
                    if ((nj_strlen(charset->from[i]) == NJG_NMF_HAN_MOJI_LEN) &&
                        (*yomi == *(charset->from[i]))) {
#endif /* NJ_OPT_CHARSET_2BYTE */
                        tolen = nj_strlen(charset->to[i]);
                        if (nj_strncmp(charset->to[i], dst, tolen) == 0) {
                            /* 曖昧検索で一致する為、一致と見なす */
                            found = 1;
                            break;
                        }
                    }
                }
                if (found == 1) {
                    /* 曖昧検索で一致した */
                    dst += tolen;
                } else {
                    /* 曖昧検索でも一致しなかった */
                    return 0;
                }
            } else {
                dst += NJG_NMF_HAN_MOJI_LEN;
            }
            yomi += NJG_NMF_HAN_MOJI_LEN;
            cnt += NJG_NMF_HAN_MOJI_LEN;
        }
#endif /* !NJ_OPT_UTF16 */
    }

    return 1;
}

/**
 * 読み文字取得
 *
 * @param[in]  word    文節情報
 * @param[out] stroke  読み文字列格納バッファ
 * @param[in]  size    stroke バイトサイズ
 *
 * @return  格納文字列の文字配列長
 */
static NJ_INT16 njg_nmf_get_stroke(NJ_WORD *word, NJ_CHAR *stroke, NJ_UINT16 size) {

    NJ_UINT16 tblidx;
    NJ_UINT16 len;
    NJ_UINT16 i;
    NJ_CHAR   *dst;

    tblidx = NJG_NMF_GET_TABLE_INDEX(word->stem.loc.current);
    len = num_conn_tbl[tblidx].yomi_len;

    /* 読み文字を格納する */
    dst = stroke;
    for (i = 0; i < (NJ_UINT16)((num_conn_tbl[tblidx].yomi_len * sizeof(NJ_CHAR)) / sizeof(NJ_UINT16)); i++) {
#ifdef NJ_OPT_UTF16
        NJG_NMF_COPY_INT16_TO_CHAR(dst, num_conn_tbl[tblidx].yomi[i]);
        dst += NJG_NMF_ZEN_MOJI_LEN;
#else /* NJ_OPT_UTF16 */
        if ((num_conn_tbl[tblidx].yomi[i] & 0xff00) != 0) {
            /* 全角文字なら上位バイトも格納 */
            *dst++ = (NJ_UINT8)((num_conn_tbl[tblidx].yomi[i] >> 8) & 0xff);
        }
        *dst++ = (NJ_UINT8)(num_conn_tbl[tblidx].yomi[i] & 0xff);
#endif /* NJ_OPT_UTF16 */
    }
    *dst = NJ_CHAR_NUL;

    return (NJ_INT16)len;
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
static NJ_INT16 njg_nmf_get_candidate(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size) {

    NJ_INT16 len = 0;
    NJ_UINT16 tblidx;

    /* 検索位置から数字繋がり候補テーブル要素番号、数字部分タイプを取得 */
    tblidx = NJG_NMF_GET_TABLE_INDEX(word->stem.loc.current);

    /* 候補表記取得 */
    len = njg_nmf_get_candidate_string(word, candidate, &num_conn_tbl[tblidx]);

    return len;
}

/**
 * 数字繋がり候補の共通作成処理
 *
 * @param[in]  word      文節情報
 * @param[out] candidate 候補文字列格納領域
 * @param[in]  tbl       数字繋がり候補作成テーブル<br>
 *                       作成する候補のテーブルのみセットされる。
 *
 * @retval >=0  格納した候補文字列文字長
 * @retval <0   エラー
 */
static NJ_INT16 njg_nmf_get_candidate_string(NJ_WORD *word, NJ_CHAR *candidate,
                                             const NJG_NUM_CONN_TBL *tbl) {
    NJ_CHAR   *dst;
    NJ_UINT16 i;

    dst = candidate;

    /* 数字繋がり候補を格納 */
    for (i = 0; i < (NJ_UINT16)((tbl->kouho_len * sizeof(NJ_CHAR)) / sizeof(NJ_UINT16)); i++) {
#ifdef NJ_OPT_UTF16
        NJG_NMF_COPY_INT16_TO_CHAR(dst, tbl->kouho[i]);
        dst += NJG_NMF_ZEN_MOJI_LEN;
#else /* NJ_OPT_UTF16 */
        if ((tbl->kouho[i] & 0xff00) != 0) {
            /* 全角文字なら上位バイトも格納 */
            *dst++ = (NJ_UINT8)((tbl->kouho[i] >> 8) & 0xff);
        }
        *dst++ = (NJ_UINT8)(tbl->kouho[i] & 0xff);
#endif /* NJ_OPT_UTF16 */
    }
    *dst = NJ_CHAR_NUL;

    /* 成功 */
    return (NJ_INT16)NJ_GET_KLEN_FROM_STEM(word);
}
