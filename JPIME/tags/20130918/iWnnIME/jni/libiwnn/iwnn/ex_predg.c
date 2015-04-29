/**
 * @file
 *  [拡張] iWnn標準絞込予測検索用擬似辞書 (UTF16/SJIS版)
 *
 * 繋がり/読みなし予測で取得された候補を、繋がり/読みなし予測の絞り込み検索時に、
 * 優先順序の上位に持ってくる処理を行う擬似辞書。
 *
 * NJ_CLASS内部に保存された繋がり/読みなし予測候補のリストを、
 * 本擬似辞書に設定された辞書頻度値の範囲で出力する。
 *
 * @author
 * (C) OMRON SOFTWARE Co., Ltd. 2008 All Rights Reserved.
 */
#include "ex_predg.h"
#include "nj_ext.h"
#include "njd.h"
#include "nj_err.h"

/************************************************/
/*         define  宣  言                       */
/************************************************/
/*
 * 文字種ごとの文字長定義
 */
#ifdef NJ_OPT_UTF16
/** 半角文字長 (UTF16) */
#define NJG_PRED_HAN_MOJI_LEN  1
/** 全角文字長 (UTF16) */
#define NJG_PRED_ZEN_MOJI_LEN  1

#else /* NJ_OPT_UTF16 */
/** 半角文字長 (SJIS) */
#define NJG_PRED_HAN_MOJI_LEN  1
/** 全角文字長 (SJIS) */
#define NJG_PRED_ZEN_MOJI_LEN  2
#endif /* NJ_OPT_UTF16 */

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
#define NJG_PRED_CONV_TO_WCHAR(x)                                            \
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
#define NJG_PRED_IS_WCHAR(x)  1
#else /* NJ_OPT_UTF16 */
/** SJIS版 */
#define NJG_PRED_IS_WCHAR(x)                                                 \
    (((((x) >= 0x81) && ((x) <= 0x9f)) || (((x) >= 0xe0) && ((x) <= 0xfc))) ? 1 : 0)
#endif/* NJ_OPT_UTF16 */

/***********************************************************************
 * 内部関数 プロトタイプ宣言
 ***********************************************************************/
static NJ_INT16 njex_pred_chk_yomi_match(NJ_CHAR *yomi, NJ_UINT16 len, NJ_CHAR *yomi_buff, NJ_UINT16 ylen,
                                         NJ_CHARSET* charset);


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
NJ_EXTERN NJ_INT16 njex_pred_giji_dic(NJ_CLASS *iwnn, NJ_UINT16 request, 
                                      NJ_PROGRAM_DIC_MESSAGE *message) {
    NJ_INT16 ret;
    NJ_UINT32 i;
    NJ_CHAR  yomi_buff[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_CHAR  *yomi;
    NJ_UINT16  ylen;


    switch (request) {
    case NJG_OP_SEARCH:
        if ((message->condition->operation != NJ_CUR_OP_FORE) ||
            (message->condition->mode != NJ_CUR_MODE_FREQ) ||
            (iwnn->relation_results_count == 0) ||
            (iwnn->njc_mode != 0) ||
            ((iwnn->environment.option.mode & NJ_RELATION_ON) == 0)) {
            /* 正引き前方一致、頻度順、繋がり予測候補保持有り、絞込予測時のみ対応 */
            message->location->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        yomi = message->condition->yomi;
        ylen = message->condition->ylen;
        message->location->loct.top = 0;
        message->location->loct.bottom = iwnn->relation_results_count - 1;
        for (i = 0; i <= message->location->loct.bottom; i++) {
            ret = njx_get_stroke(iwnn, &(iwnn->relation_results[i]), &yomi_buff[0], sizeof(yomi_buff));
            if (ret < 0) {
                /* エラーが返ってきた場合は、検索対象としない。 */
                continue;
            }
            if (njex_pred_chk_yomi_match(yomi, ylen, yomi_buff, ret, iwnn->environment.cursor.cond.charset) == 1) {
                message->location->loct.current = i;
                message->location->loct.current_info = 0x10;
                message->location->cache_freq = message->location->dic_freq.high;
                message->location->loct.status = NJ_ST_SEARCH_READY;
                return 1;
            }
        }
        
        /* 候補作成不可 */
        message->location->loct.status = NJ_ST_SEARCH_END_EXT;
        return 0;

    case NJG_OP_SEARCH_NEXT:
        if ((message->condition->operation != NJ_CUR_OP_FORE) ||
            (message->condition->mode != NJ_CUR_MODE_FREQ) ||
            (iwnn->relation_results_count == 0) ||
            (iwnn->njc_mode != 0) ||
            ((iwnn->environment.option.mode & NJ_RELATION_ON) == 0)) {
            /* 正引き前方一致、頻度順、繋がり予測候補保持有り、絞込予測時のみ対応 */
            message->location->loct.status = NJ_ST_SEARCH_END_EXT; /*NCH_FB*/
            return 0; /*NCH_FB*/
        }
        yomi = message->condition->yomi;
        ylen = message->condition->ylen;
        for (i = message->location->loct.current + 1; i <= message->location->loct.bottom; i++) {
            ret = njx_get_stroke(iwnn, &(iwnn->relation_results[i]), &yomi_buff[0], sizeof(yomi_buff));
            if (ret < 0) {
                /* エラーが返ってきた場合は、検索対象としない。 */
                continue;
            }
            if (njex_pred_chk_yomi_match(yomi, ylen, yomi_buff, ret, iwnn->environment.cursor.cond.charset) == 1) {
                message->location->loct.current = i;
                message->location->loct.current_info = 0x10;
                message->location->cache_freq--;
                if (message->location->dic_freq.base > message->location->cache_freq) {
                    message->location->cache_freq = message->location->dic_freq.base;
                }
                message->location->loct.status = NJ_ST_SEARCH_READY;
                return 1;
            }
        }
        
        /* 候補作成不可 */
        message->location->loct.status = NJ_ST_SEARCH_END;
        return 0;

    case NJG_OP_GET_WORD_INFO:
        if (message->location->loct.current > message->location->loct.bottom) {
            /* 念のため保持候補の範囲をチェック。ココを通ることはない */
            return -1; /*NCH*/
        }
        *(message->word) = iwnn->relation_results[message->location->loct.current].word;
        message->word->stem.hindo = message->location->cache_freq;
        return 0;

    case NJG_OP_GET_STROKE:
    case NJG_OP_GET_STRING:
    case NJG_OP_GET_ADDITIONAL:
        return -1; /*NCH*/

    case NJG_OP_LEARN:
    case NJG_OP_UNDO_LEARN:
    case NJG_OP_ADD_WORD:
    case NJG_OP_DEL_WORD:
        /* 非対応の操作 */
        return 0;

    default:
        break;
    }
    return -1; /* エラー */ /*NCH*/
}

/**
 * 読み文字前方一致判定処理(曖昧検索対応)
 *
 * @param[in]  yomi          読み文字
 * @param[in]  len           読み文字長
 * @param[in]  yomi_buff     処理結果の読み文字
 * @param[in]  ylen          処理結果の読み文字長
 * @param[in]  charset       あいまい文字セット
 *
 * @retval  1  マッチングする
 * @retval  0  マッチングしない
 */
static NJ_INT16 njex_pred_chk_yomi_match(NJ_CHAR *yomi, NJ_UINT16 len, NJ_CHAR *yomi_buff, NJ_UINT16 ylen,
                                         NJ_CHARSET* charset) {
    NJ_UINT16 cnt;
    NJ_UINT16 i;
    NJ_UINT16 tolen = 0;
    NJ_INT16  found = 0;
    NJ_CHAR* dst;
#ifdef NJ_OPT_CHARSET_2BYTE
    NJ_UINT16 start;
    NJ_UINT16 end;
#endif /* NJ_OPT_CHARSET_2BYTE */

    dst = yomi_buff;
    cnt = 0;
    while (cnt < len) {
        found = 0;
#ifndef NJ_OPT_UTF16
        if (NJG_PRED_IS_WCHAR(*yomi)) {
            /* 2バイト文字の場合 */
#endif /* !NJ_OPT_UTF16 */
            if (NJG_PRED_CONV_TO_WCHAR(yomi) != NJG_PRED_CONV_TO_WCHAR(dst)) {
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
                    if ((nj_strlen(charset->from[i]) == NJG_PRED_ZEN_MOJI_LEN) &&
                        (NJG_PRED_CONV_TO_WCHAR(yomi) == NJG_PRED_CONV_TO_WCHAR(charset->from[i]))) {
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
                dst += NJG_PRED_ZEN_MOJI_LEN;
            }
            yomi += NJG_PRED_ZEN_MOJI_LEN;
            cnt += NJG_PRED_ZEN_MOJI_LEN;
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
                    if ((nj_strlen(charset->from[i]) == NJG_PRED_HAN_MOJI_LEN) &&
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
                dst += NJG_PRED_HAN_MOJI_LEN;
            }
            yomi += NJG_PRED_HAN_MOJI_LEN;
            cnt += NJG_PRED_HAN_MOJI_LEN;
        }
#endif /* !NJ_OPT_UTF16 */
    }

    return 1;
}
