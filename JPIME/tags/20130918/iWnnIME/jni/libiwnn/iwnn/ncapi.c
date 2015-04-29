/**
 * @file
 *   変換部API
 *
 *   アプリケーションに提供する関数を定義する。
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2012 All Rights Reserved.
 */

/************************************************/
/*           インクルードファイル               */
/************************************************/
#include "nj_lib.h"
#include "nj_err.h"
#include "nj_dicif.h"
#include "njfilter.h"
#include "nj_ext.h"
#include "njc.h"
#include "nj_dic.h"


/************************************************/
/*              define  宣  言                  */
/************************************************/
/*
 * [内部使用]活用形変形タイプ
 */
#define CONJ_TYPE_NONE     0    /**< CONJ_TYPE: 活用形変形無し   */
#define CONJ_TYPE_FZK      1    /**< CONJ_TYPE: 付属語変形タイプ */
#define CONJ_TYPE_JIRITSU  2    /**< CONJ_TYPE: 自立語変形タイプ */

/*
 * [内部使用]付属語解析バッファ初期化値
 */
#define FZK_SPLIT_INIT  0xDA

#define CONJ_GET_HINSI_NUM(h)  (NJ_INT32_READ((h) + 0x0C))

#define CONJ_GET_CONV_NUM(h)  (NJ_INT32_READ((h) + 0x10))

#define CONJ_GET_CONV_BE_TOP_ADDR(h)  ((h) + NJ_INT32_READ((h) + 0x14))

#define CONJ_GET_CONV_AF_TOP_ADDR(h)  ((h) + NJ_INT32_READ((h) + 0x18))

#define CONJ_GET_SEARCH_TOP_ADDR(h)  ((h) + NJ_INT32_READ((h) + 0x1C))

#define CONJ_GET_MOJI_TOP_ADDR(h)  ((h) + NJ_INT32_READ((h) + 0x20))

#define CONJ_GET_FPOS_UNUSED_NO(h)  (NJ_INT16_READ((h) + 0x24))

#define CONJ_GET_BPOS_UNUSED_NO(h)  (NJ_INT16_READ((h) + 0x26))

#define CONJ_GET_CONV_BE_EACH_ADDR(h, x)  (CONJ_GET_CONV_BE_TOP_ADDR(h) + ((x) - 1) * 0x04)

#define CONJ_GET_CONV_TYPE(h, x)  (*(CONJ_GET_CONV_BE_EACH_ADDR((h), (x))))

#define CONJ_GET_CONV_AF_ADDR(h, x)  (CONJ_GET_CONV_AF_TOP_ADDR((h))    \
                                      + NJ_INT24_READ(CONJ_GET_CONV_BE_EACH_ADDR((h), (x)) + 1))

#define CONJ_GET_CONV_AF_EACH_ADDR_TYPE1(h, x, y)  (CONJ_GET_CONV_AF_ADDR((h), (x)) + (y) * 0x08)

#define CONJ_GET_YOMI_TYPE1(h, x, y)  (CONJ_GET_MOJI_TOP_ADDR((h))    \
                                       + NJ_INT24_READ(CONJ_GET_CONV_AF_EACH_ADDR_TYPE1((h), (x), (y))))

#define CONJ_GET_YOMI_LEN_TYPE1(h, x, y)  (*(CONJ_GET_CONV_AF_EACH_ADDR_TYPE1((h), (x), (y)) + 0x03))

#define CONJ_GET_NEW_FZK_HINSI_TYPE1(h, x, y)  (NJ_INT32_READ(CONJ_GET_CONV_AF_EACH_ADDR_TYPE1((h), (x), (y)) + 0x04))

#define CONJ_GET_SEARCH_ADDR(h, x)  (CONJ_GET_SEARCH_TOP_ADDR((h))    \
                                     + NJ_INT24_READ(CONJ_GET_CONV_BE_EACH_ADDR((h), (x)) + 1))

#define CONJ_GET_SEARCH_NUM(h, x)  (NJ_INT32_READ(CONJ_GET_SEARCH_ADDR((h), (x))))

#define CONJ_GET_SEARCH_EACH_DATA(h, x, y)  (CONJ_GET_SEARCH_ADDR((h), (x)) + 0x04 + (y) * 0x0C)

#define CONJ_GET_SEARCH_DATA(h, x, y)  (CONJ_GET_MOJI_TOP_ADDR((h))    \
                                        + NJ_INT24_READ(CONJ_GET_SEARCH_EACH_DATA((h), (x), (y)) + 0x04))

#define CONJ_GET_SEARCH_DATA_LEN(h, x, y)  ((*(CONJ_GET_SEARCH_EACH_DATA((h), (x), (y)) + 0x07)) & 0x7F)

#define CONJ_GET_CONV_AF_EACH_ADDR_TYPE2(h, x, y, z)  (CONJ_GET_CONV_AF_TOP_ADDR((h))    \
                                                       + NJ_INT32_READ(CONJ_GET_SEARCH_EACH_DATA((h), (x), (y)) + 0x08)    \
                                                       + (z) * 0x14)

#define CONJ_GET_YOMI_TYPE2(h, x, y, z)  (CONJ_GET_MOJI_TOP_ADDR((h))    \
                                          + NJ_INT24_READ(CONJ_GET_CONV_AF_EACH_ADDR_TYPE2((h), (x), (y), (z))))

#define CONJ_GET_YOMI_LEN_TYPE2(h, x, y, z)  (*(CONJ_GET_CONV_AF_EACH_ADDR_TYPE2((h), (x), (y), (z)) + 0x03))

#define CONJ_GET_NEW_FZK_HINSI_TYPE2(h, x, y, z)  (NJ_INT32_READ(CONJ_GET_CONV_AF_EACH_ADDR_TYPE2((h), (x), (y), (z)) + 0x04))

#define CONJ_GET_KOUHO(h, x, y, z)  (CONJ_GET_MOJI_TOP_ADDR((h))    \
                                       + NJ_INT24_READ(CONJ_GET_CONV_AF_EACH_ADDR_TYPE2((h), (x), (y), (z)) + 0x08))

#define CONJ_GET_KOUHO_LEN(h, x, y, z)  (*(CONJ_GET_CONV_AF_EACH_ADDR_TYPE2((h), (x), (y), (z)) + 0x0B))

#define CONJ_GET_NEW_STEM_HINSI(h, x, y, z)  (NJ_INT32_READ(CONJ_GET_CONV_AF_EACH_ADDR_TYPE2((h), (x), (y), (z)) + 0x0C))

#define CONJ_GET_NEW_FZK_LEN(h, x, y, z)  (NJ_INT32_READ(CONJ_GET_CONV_AF_EACH_ADDR_TYPE2((h), (x), (y), (z)) + 0x10))

#define IS_CONJ_AF_EACH_OFFSET_TYPE1(h, x, y)  (NJ_INT24_READ(CONJ_GET_CONV_AF_EACH_ADDR_TYPE1((h), (x), (y))) != 0x00FFFFFF ? 1 : 0)

#define IS_CONJ_AF_EACH_OFFSET_TYPE1_NO_FZK(h, x, y)  (NJ_INT24_READ(CONJ_GET_CONV_AF_EACH_ADDR_TYPE1((h), (x), (y))) != 0x00000000 ? 1 : 0)

#define CONJ_CHK_DIFF_FZK_INF(x, y)    ((((x)->fzk.yomi_len == (y)->fzk.yomi_len) &&      \
                                         ((x)->fzk.kouho_len == (y)->fzk.kouho_len) &&    \
                                         ((x)->fzk.hinsi == (y)->fzk.hinsi) &&            \
                                         ((x)->fzk.freq == (y)->fzk.freq))                \
                                        ? 1 : 0)
#define CONJ_CHK_DIFF_STEM_INF(x, y)    ((((x)->stem.yomi_len == (y)->stem.yomi_len) &&      \
                                          ((x)->stem.kouho_len == (y)->stem.kouho_len) &&    \
                                          ((x)->stem.hinsi == (y)->stem.hinsi) &&            \
                                          ((x)->stem.attr == (y)->stem.attr) &&              \
                                          ((x)->stem.freq == (y)->stem.freq))                \
                                         ? 1 : 0)

/************************************************/
/*        static 変数宣言                       */
/************************************************/

/************************************************/
/*              extern  宣  言                  */
/************************************************/

/************************************************/
/*              prototype  宣  言               */
/************************************************/
static NJ_INT16 njc_stroke_filter(NJ_CLASS * iwnn, NJ_PHASE1_FILTER_MESSAGE *message);
static NJ_INT32 njc_fzk_conv_process(NJ_CLASS *iwnn, NJ_UINT32 type,
                                     NJ_WORD_INFO *info_in, NJ_WORD_INFO *info_out, NJ_UINT32 *con_type);
static void njc_init_fzk_buf(NJ_FZK_BUF *fzkbuf, NJ_RESULT *clause);
static void njc_set_fzk_dics(NJ_DIC_SET *dics, NJ_DIC_SET *fzkdic);
static NJ_INT32 njc_connect(NJ_FZK_BUF *fzkbuf, NJ_FZK_WORD *fzk1, NJ_WORD *fzk2);
static NJ_INT32 njc_get_next_fzkpat(NJ_FZK_BUF *fzks, NJ_INT32 n, NJ_UINT8 len);
static NJ_INT16 njc_push_fzkpat(NJ_FZK_BUF *fzkbuf, NJ_FZK_WORD *fzk, NJ_FZK_PATTERN *prev, NJ_UINT8 len);
static NJ_INT16 njc_fzk_get_next_pattern(NJ_FZK_BUF *fzks, NJ_FZK_PATTERN **pattern);



/**********************************************************************
 * APIs
 **********************************************************************/

/**
 * かな漢字変換API
 *
 * 連文節変換・単文節変換を実行する。
 *
 * @attention 読み文字列はNUL文字でターミネートされていること。
 * @attention 変換結果格納バッファは、analyze_level分の領域を呼出元で用意すること。
 *
 * @param[in,out] iwnn          解析情報クラス
 * @param[in]     yomi          変換を行う読み文字列
 * @param[in]     analyze_level 解析文節数(1:単文節変換、NJ_MAX_PHRASE:連文節変換)
 * @param[in]     devide_pos    文節区切り位置(0:指定なし)
 * @param[out]    results       変換結果格納バッファ
 *
 * @retval >0 resultsへ格納した処理結果数(=文節数)
 * @retval <0 異常終了
 */
NJ_EXTERN NJ_INT16 njx_conv(NJ_CLASS *iwnn, NJ_CHAR *yomi, NJ_UINT8 analyze_level,
                            NJ_UINT8 devide_pos, NJ_RESULT *results) {

    NJ_INT16 ret;
    NJ_UINT16 i;


    /*
     * 引数チェック
     *  ※ 下記以外の引数チェックは、njc_conv()で実施する
     */
    /*
     * 解析文節数が規定(1 or NJ_MAX_PHRASE)以外の場合は異常
     */
    if ((analyze_level != NJ_MAX_PHRASE) && (analyze_level != 1)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CONV, NJ_ERR_PARAM_ILLEGAL_LEVEL);
    } else {
        if ((devide_pos != 0) && (analyze_level == 1)) {
            /*
             * 文節区切り位置指定時、解析文節数に１(単文節変換)が指定された場合
             * は異常
             */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CONV, NJ_ERR_PARAM_ILLEGAL_LEVEL);
        }
    }
    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CONV, NJ_ERR_PARAM_ENV_NULL);
    }
    if (iwnn->dic_set.rHandle[NJ_MODE_TYPE_HENKAN] == NULL) {
        /* 辞書セット内ルール辞書がNULLの場合はエラーとする */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CONV, NJ_ERR_NO_RULEDIC);
    }


    /*
     * 変換用辞書セット構造体の作成
     **/
    iwnn->tmp_dic_set.rHandle[NJ_MODE_TYPE_HENKAN] = iwnn->dic_set.rHandle[NJ_MODE_TYPE_HENKAN];
    iwnn->tmp_dic_set.rHandle[NJ_MODE_TYPE_YOSOKU] = NULL;
    iwnn->tmp_dic_set.rHandle[NJ_MODE_TYPE_MORPHO] = NULL;
    INIT_KEYWORD_IN_NJ_DIC_SET(&(iwnn->tmp_dic_set));
    iwnn->tmp_dic_set.mode = NJ_CACHE_MODE_NONE;

    /*
     * 使用辞書のフィルタリングを行う
     **/
    for (i = 0; i < NJ_MAX_DIC; i++) {
        njd_clear_dicinfo(&(iwnn->tmp_dic_set.dic[i]));
        if (iwnn->dic_set.dic[i].handle != NULL) {
            if (iwnn->dic_set.dic[i].dic_freq[NJ_MODE_TYPE_HENKAN].high >=
                iwnn->dic_set.dic[i].dic_freq[NJ_MODE_TYPE_HENKAN].base ) {
                /* 使用辞書のみを設定する */
                njd_copy_dicinfo(&(iwnn->tmp_dic_set.dic[i]), &(iwnn->dic_set.dic[i]), NJ_MODE_TYPE_HENKAN);
            }
        }
    }

    /* 同表記バッファ初期化 */
    nje_clear_homonym_buf(iwnn);

    /* 学習の準備を行う。*/
    ret = njd_l_make_space(iwnn, NJ_MAX_PHRASE, 1);
    if (ret < 0) {
        iwnn->njc_mode = NJC_MODE_NOT_CONVERTED;
        return ret;
    }

    /*
     * 変換実行
     */
    iwnn->njc_mode = NJC_MODE_CONVERTED;        /* 1:変換中 */

    ret = njc_conv(iwnn, &(iwnn->tmp_dic_set), yomi, analyze_level, devide_pos, results, 0);

    if (ret < 0) {
        iwnn->njc_mode = NJC_MODE_NOT_CONVERTED;        /* 0:初期状態 */
    }

    return ret;
}


/**
 * 全候補取得API
 *
 * @param[in,out] iwnn       解析情報クラス
 * @param[in]     target       njx_convで返された処理結果のうち、全候補取得対象文節位置の処理結果
 * @param[in]     candidate_no 全候補候補番号(0 origin)
 * @param[out]    result       全候補結果格納バッファ(１候補分の領域を呼出元で用意すること)
 *
 * @retval >=0  全候補の候補数（０：候補なし）
 * @retval <0   異常終了
 */
NJ_EXTERN NJ_INT16 njx_zenkouho(NJ_CLASS *iwnn, NJ_RESULT *target, NJ_UINT16 candidate_no,
                                NJ_RESULT *result) {

    NJ_INT16 ret;


    /*
     * 引数チェックは、njc_zenkouho()で実施する
     */

    /*
     * 第1引数(iwnn)がNULLであれば、異常とする。
     */
    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ZENKOUHO, NJ_ERR_PARAM_ENV_NULL);
    }

    /*
     * 変換モードが初期状態もしくは形態素解析中であれば、異常とする。
     */
    if ((iwnn->njc_mode == NJC_MODE_NOT_CONVERTED) || (iwnn->njc_mode == NJC_MODE_MORPHO)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ZENKOUHO, NJ_ERR_NOT_CONVERTED);
    }
    /*
     * 変換モードが変換中であり、targetがNULLであれば、異常とする。
     */
    if ((iwnn->njc_mode == NJC_MODE_CONVERTED) && (target == NULL)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ZENKOUHO, NJ_ERR_NO_CANDIDATE_LIST);
    }

    /* 変換部出力結果以外はエラーとする */
    if ((target != NULL) && (NJ_GET_RESULT_OP(target->operation_id) != NJ_OP_CONVERT)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ZENKOUHO, NJ_ERR_INVALID_RESULT);
    }


    /* 同表記バッファ初期化 */
    nje_clear_homonym_buf(iwnn);

    /*
     * 全候補取得実行
     */
    iwnn->njc_mode = NJC_MODE_ZENKOUHO; /* 2:全候補中 */

    ret = njc_zenkouho(iwnn, target, candidate_no, result, 1);

    if (ret < 0) {
        /* エラー発生時は、変換モードを強制的に未変換にする */
        iwnn->njc_mode = NJC_MODE_NOT_CONVERTED;        /* 0:初期状態 */
    }
    return ret;
}


/**
 * 無変換候補取得API
 *
 * 無変換候補を取得する。
 *
 * @param[in,out] iwnn  解析情報クラス
 * @param[in] yomi      読み文字列
 * @param[out] result   変換結果バッファ
 *
 * @retval 1 取得成功
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njx_get_stroke_word(NJ_CLASS *iwnn, NJ_CHAR *yomi, NJ_RESULT *result) {
    NJ_INT16 ret;
    NJ_UINT16 i;
    NJ_VOID * backup_phase1_filter = NULL;
    NJ_VOID * backup_phase2_filter = NULL;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_GET_STROKE_WORD, NJ_ERR_PARAM_ENV_NULL);
    }

    if (yomi == NULL) {
        /* 第2引数(yomi)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_GET_STROKE_WORD, NJ_ERR_PARAM_YOMI_NULL);
    }

    if (result == NULL) {
        /* 第3引数(result)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_GET_STROKE_WORD, NJ_ERR_PARAM_RESULT_NULL);
    }


    /*
     * 変換用辞書セット構造体の作成
     */
    iwnn->tmp_dic_set.rHandle[NJ_MODE_TYPE_HENKAN] = iwnn->dic_set.rHandle[NJ_MODE_TYPE_HENKAN];
    iwnn->tmp_dic_set.rHandle[NJ_MODE_TYPE_YOSOKU] = NULL;
    iwnn->tmp_dic_set.rHandle[NJ_MODE_TYPE_MORPHO] = NULL;
    INIT_KEYWORD_IN_NJ_DIC_SET(&(iwnn->tmp_dic_set));
    iwnn->tmp_dic_set.mode = NJ_CACHE_MODE_NONE;

    /*
     * 使用辞書のフィルタリングを行う
     */
    for (i = 0; i < NJ_MAX_DIC; i++) {
        njd_clear_dicinfo(&(iwnn->tmp_dic_set.dic[i]));
        if (iwnn->dic_set.dic[i].handle != NULL) {
            if (iwnn->dic_set.dic[i].dic_freq[NJ_MODE_TYPE_HENKAN].high >=
                iwnn->dic_set.dic[i].dic_freq[NJ_MODE_TYPE_HENKAN].base ) {
                /* 使用辞書のみを設定する */
                njd_copy_dicinfo(&(iwnn->tmp_dic_set.dic[i]), &(iwnn->dic_set.dic[i]), NJ_MODE_TYPE_HENKAN);
            }
        }
    }

    backup_phase2_filter = iwnn->option_data.phase2_filter;
    backup_phase1_filter = iwnn->option_data.phase1_filter;

    /* 無変換確定用の辞書引きフィルタリング関数を設定 */
    iwnn->option_data.phase2_filter = NULL;
    iwnn->option_data.phase1_filter = (NJ_VOID*)njc_stroke_filter;

    /* 同表記バッファ初期化 */
    nje_clear_homonym_buf(iwnn);

    /*
     * 変換実行
     */
    iwnn->njc_mode = NJC_MODE_CONVERTED;        /* 1:変換中 */

    /* 解析文節数を1にして単文節変換を行う*/
    ret = njc_conv(iwnn, &(iwnn->tmp_dic_set), yomi, 1, 0, result, 0);

    if (ret < 0) {
        iwnn->njc_mode = NJC_MODE_NOT_CONVERTED;        /* 0:初期状態 */ /*NCH*/
    }

    /* フィルタリング関数をリストアする */
    iwnn->option_data.phase2_filter = (NJ_VOID*)backup_phase2_filter;
    iwnn->option_data.phase1_filter = (NJ_VOID*)backup_phase1_filter;

    return ret;
}


/**
 * 活用形変形API
 *
 * 入力された文節情報を、指定された活用タイプの活用形に変形する。
 *
 * @param[in]   iwnn        解析情報クラス
 * @param[in]   type        活用タイプ
 * @param[in]   clause_in   入力文節情報
 * @param[out]  clause_out  変形後文節情報
 *
 * @retval   1  活用形変形成功
 * @retval   0  活用形変形候補無し
 * @retval  <0  エラー
 */
NJ_INT32 njx_fzk_conv(NJ_CLASS *iwnn, NJ_UINT32 type, NJ_WORD_INFO *clause_in, NJ_WORD_INFO *clause_out) {
    NJ_INT32 ret;
    NJ_UINT8 *conj_data;
    NJ_UINT32 con_type;
    NJ_INT32  conj_cnt;
    NJ_UINT32 id;


    /* パラメーターチェック */
    if (iwnn == NULL) {
        /* 第一引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_FZK_CONV, NJ_ERR_PARAM_ENV_NULL);
    }
    if (clause_in == NULL) {
        /* 第三引数(clause_in)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_FZK_CONV, NJ_ERR_PARAM_NULL);
    }
    if (clause_out == NULL) {
        /* 第四引数(clause_out)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_FZK_CONV, NJ_ERR_PARAM_NULL);
    }

    /* 入力文節情報チェック */
    if (clause_in->hinsi_group != NJ_HINSI_DETAIL) {
        /* clause_in->hinsi_groupがNJ_HINSI_DETAIL以外の場合は活用形変形候補無しとする */
        return 0;
    }

    /* 活用形変形データチェック */
    if (iwnn->option_data.conjugation_data == NULL) {
        /*
         * オプション構造体の活用形変形データ
         * (iwnn->option_data.conjugation_data)がNULLの場合はエラー
         */
        return NJ_SET_ERR_VAL(NJ_FUNC_FZK_CONV, NJ_ERR_CONJ_DATA_NULL);
    }

    conj_data = (NJ_UINT8 *)iwnn->option_data.conjugation_data;
    conj_cnt = CONJ_GET_CONV_NUM(conj_data);
    /* 識別子チェック */
    id = NJ_INT32_READ(conj_data);
    if (id != NJ_FZK_CONJ_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_CONJ_DATA_INVALID);
    }
    /* 最大活用形定義より小さいデータの場合 */
    if (conj_cnt < NJ_MAX_CONJ_TYPE) {
        /* 活用形変形データ不正エラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_FZK_CONV, NJ_ERR_CONJ_DATA_INVALID);
    }
    if ((type < NJ_CONJ_TYPE_BASIC) || (type > (NJ_UINT32)conj_cnt)) {
        /* 第二引数(type)が範囲外の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_FZK_CONV, NJ_ERR_PARAM_VALUE_INVALID);
    }

    /* 活用形変形処理 */
    ret = njc_fzk_conv_process(iwnn, type, clause_in, clause_out, &con_type);

    return ret;
}


/**
 * 活用形タイプ取得API
 *
 * 入力文節情報から活用形タイプを取得する。
 *
 * @param[in]   iwnn    解析情報クラス
 * @param[in]   clause  入力文節情報
 *
 * @retval  >=1  該当活用形タイプ
 * @retval    0  活用形タイプ無し
 * @retval   <0  エラー
 */
NJ_INT32 njx_get_conjugation_type(NJ_CLASS *iwnn, NJ_WORD_INFO *clause) {
    NJ_INT32 conv_cnt;
    NJ_UINT8 *conj_data;
    NJ_INT32 ret_type;
    NJ_WORD_INFO wk_word_info;
    NJ_INT32 ret;
    NJ_UINT32 con_type;
    NJ_UINT32 id;


    /* パラメーターチェック */
    if (iwnn == NULL) {
        /* 第一引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_GET_CONJUGATION_TYPE, NJ_ERR_PARAM_ENV_NULL);
    }
    if (clause == NULL) {
        /* 第二引数(clause)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_GET_CONJUGATION_TYPE, NJ_ERR_PARAM_NULL);
    }

    /* 入力文節情報チェック */
    if (clause->hinsi_group != NJ_HINSI_DETAIL) {
        /* clause->hinsi_groupがNJ_HINSI_DETAIL以外の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_GET_CONJUGATION_TYPE, NJ_ERR_PARAM_VALUE_INVALID);
    }

    /* 活用形変形データチェック */
    if (iwnn->option_data.conjugation_data == NULL) {
        /*
         * オプション構造体の活用形変形データ
         * (iwnn->option_data.conjugation_data)がNULLの場合はエラー
         */
        return NJ_SET_ERR_VAL(NJ_FUNC_GET_CONJUGATION_TYPE, NJ_ERR_CONJ_DATA_NULL);
    }
    conj_data = (NJ_UINT8 *)iwnn->option_data.conjugation_data;
    conv_cnt = CONJ_GET_CONV_NUM(conj_data);
    /* 識別子チェック */
    id = NJ_INT32_READ(conj_data);
    if (id != NJ_FZK_CONJ_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_GET_CONJUGATION_TYPE, NJ_ERR_CONJ_DATA_INVALID);
    }
    /* 最大活用形パターンを超えるデータの場合 */
    if (conv_cnt < NJ_MAX_CONJ_TYPE) {
        /* 活用形変形データ不正エラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_GET_CONJUGATION_TYPE, NJ_ERR_CONJ_DATA_INVALID);
    }

    con_type = 0;

    for (ret_type = 1; ret_type <= conv_cnt; ret_type++) {
        /* 活用形変形処理 */
        ret = njc_fzk_conv_process(iwnn, ret_type, clause, &wk_word_info, &con_type);

        /* 活用形変形できた場合 */
        if (ret > 0) {
            switch (con_type) {
            case CONJ_TYPE_FZK:    /* 付属語変形タイプ */
                /* 活用形変形データを比較する */
                /* 付属語の情報を比較する */
                if (CONJ_CHK_DIFF_FZK_INF(clause, &wk_word_info)) {
                    return ret_type;
                }
                break;
            case CONJ_TYPE_JIRITSU:    /* 自立語変形タイプ */
                /* 活用形変形データを比較する */
                /* 自立語、付属語の情報を比較する */
                if ((CONJ_CHK_DIFF_STEM_INF(clause, &wk_word_info)) &&
                    (CONJ_CHK_DIFF_FZK_INF(clause, &wk_word_info))) {
                    return ret_type;
                }
                break;
            default:
                /* ここでは何も処理しない */
                break;
            }
        }
    }

    /* 活用形タイプなし */
    return 0;
}


/**
 * 無変換候補取得用辞書引きフィルタリング関数
 * 
 * @param[in] iwnn      解析情報クラス
 * @param[in] message   フィルターメッセージ
 *
 * @retval 1 使用
 * @retval 0 未使用
 *
 */
static NJ_INT16 njc_stroke_filter(NJ_CLASS * iwnn, NJ_PHASE1_FILTER_MESSAGE *message) {
    NJ_INT16 ret;


    ret = 1;

    if (nj_strncmp(message->string, message->condition->yomi, message->string_len) != 0) {
        ret = 0;
    }

    return ret;
}


/**
 * 活用形変形処理関数
 *
 * 入力された文節情報を、指定された活用タイプの活用形に変形する。
 *
 * @param[in]   iwnn        解析情報クラス
 * @param[in]   type        活用タイプ
 * @param[in]   info_in     入力文節情報
 * @param[out]  info_out    変形後文節情報
 * @param[out]  con_type    活用形変形タイプ
 *
 * @retval   1  活用形変形成功
 * @retval   0  活用形変形候補無し
 */
static NJ_INT32 njc_fzk_conv_process(NJ_CLASS *iwnn, NJ_UINT32 type,
                                     NJ_WORD_INFO *info_in, NJ_WORD_INFO *info_out, NJ_UINT32 *con_type) {
    NJ_INT32 stem_bpos;
    NJ_INT32 fzk_fpos, fzk_bpos;
    NJ_INT32 search_num, cnt;
    NJ_UINT32 wk_type;
    NJ_INT32 pos_data;
    NJ_UINT8 *conj_data;
    NJ_CHAR *new_fzk, *new_yomi, *new_kouho;
    NJ_INT16 i;
    NJ_INT16 conj_type = 0;
    NJ_INT16 fpos_no, bpos_no;
    NJ_UINT8 search_len, new_yomi_len, new_kouho_len, new_fzk_len;



    /* 入力文節情報を変形後文節情報に設定 */
    nj_memcpy((NJ_UINT8 *)info_out, (NJ_UINT8 *)info_in, sizeof(NJ_WORD_INFO));

    /* 自立語後品詞を取得 */
    stem_bpos = (info_in->stem.hinsi & 0x0000FFFF);

    /* 付属語前品詞を取得 */
    fzk_fpos = ((info_in->fzk.hinsi & 0xFFFF0000) >> 16);

    /* 付属語後品詞を取得 */
    fzk_bpos = (info_in->fzk.hinsi & 0x0000FFFF);

    /* 活用形変形データ先頭を取得 */
    conj_data = (NJ_UINT8 *)iwnn->option_data.conjugation_data;
    *con_type = 0;
    wk_type = type - 1;

    /* 変換未使用 */
    fpos_no = CONJ_GET_FPOS_UNUSED_NO(conj_data);
    bpos_no = CONJ_GET_BPOS_UNUSED_NO(conj_data);

    /* 自立語後品詞と付属語前品詞がいずれも「変換未使用」の場合 */
    if ((stem_bpos == bpos_no) && (fzk_fpos == fpos_no)) {
        /* 付属語後品詞を設定 */
        pos_data = fzk_bpos;
    } else {
        /* 自立語後品詞を設定 */
        pos_data = stem_bpos;
    }

    /* 活用形変形タイプ判定 */
    conj_type = CONJ_GET_CONV_TYPE(conj_data, pos_data);
    switch (conj_type) {
    case CONJ_TYPE_NONE:    /* 活用形変形候補無し */
        /* 活用形変形候補なしで返す */
        return 0;

    case CONJ_TYPE_FZK:    /* 付属語変形タイプ */
        if (IS_CONJ_AF_EACH_OFFSET_TYPE1(conj_data, stem_bpos, wk_type)) {
            /*
             * 活用形変形パターンタイプが存在する場合、
             * 付属語情報(読み文字列情報、候補文字列情報、品詞情報)を更新する。
             */
            if (IS_CONJ_AF_EACH_OFFSET_TYPE1_NO_FZK(conj_data, stem_bpos, wk_type)) {
                new_fzk = (NJ_CHAR*)CONJ_GET_YOMI_TYPE1(conj_data, stem_bpos, wk_type);
            } else {
                new_fzk = NULL;
            }
            new_fzk_len = (CONJ_GET_YOMI_LEN_TYPE1(conj_data, stem_bpos, wk_type) / sizeof(NJ_CHAR));
            if ((info_in->stem.yomi_len + new_fzk_len > NJ_MAX_LEN) ||
                (info_in->stem.kouho_len + new_fzk_len > NJ_MAX_RESULT_LEN)) {
                /* 活用形変形候補なしで返す */
                return 0;
            }

            /* 付属語情報を設定 */
            info_out->fzk.hinsi = CONJ_GET_NEW_FZK_HINSI_TYPE1(conj_data, stem_bpos, wk_type);

            /* 付属語前品詞を取得 */
            fzk_fpos = ((info_out->fzk.hinsi & 0xFFFF0000) >> 16);

            /* 付属語後品詞を取得 */
            fzk_bpos = (info_out->fzk.hinsi & 0x0000FFFF);

            /* 自立語後品詞と付属語前品詞がいずれも「変換未使用」の場合 */
            if ((fzk_fpos == fpos_no) && (fzk_bpos == bpos_no)) {
                info_out->yomi[info_in->stem.yomi_len + new_fzk_len] = NJ_CHAR_NUL;
                info_out->kouho[info_in->stem.kouho_len + new_fzk_len] = NJ_CHAR_NUL;
                info_out->fzk.yomi_len = new_fzk_len;
                info_out->fzk.kouho_len = new_fzk_len;
                info_out->fzk.hinsi = 0x00000000;
                *con_type = conj_type;
            } else {
                if (new_fzk != NULL) {
                    nj_strncpy(&(info_out->yomi[info_in->stem.yomi_len]), new_fzk, new_fzk_len);
                    nj_strncpy(&(info_out->kouho[info_in->stem.kouho_len]), new_fzk, new_fzk_len);
                }
                info_out->yomi[info_in->stem.yomi_len + new_fzk_len] = NJ_CHAR_NUL;
                info_out->kouho[info_in->stem.kouho_len + new_fzk_len] = NJ_CHAR_NUL;
                info_out->fzk.yomi_len = new_fzk_len;
                info_out->fzk.kouho_len = new_fzk_len;
                *con_type = conj_type;
            }
            return 1;

        } else {
            /*
             * 活用形変形パターンタイプが存在しない場合、
             * 活用形変形候補無しで返す。
             */
            return 0;
        }

    case CONJ_TYPE_JIRITSU:    /* 自立語変形タイプ */
        /* 検索データ数を取得 */
        search_num = CONJ_GET_SEARCH_NUM(conj_data, stem_bpos);
        search_len = 0;
        for (cnt = 0; cnt < search_num; cnt++) {
            search_len = CONJ_GET_SEARCH_DATA_LEN(conj_data, stem_bpos, cnt) / sizeof(NJ_CHAR);
            for (i = 0; i < search_len; i++) {
                if (!NJ_CHAR_IS_EQUAL(((info_in->kouho) + (info_in->stem.kouho_len) - search_len + i),
                                      (NJ_CHAR*)CONJ_GET_SEARCH_DATA(conj_data, stem_bpos, cnt) + i)) {
                    break;
                }
            }
            if (i == search_len) {
                break;
            }
        }
        /* 最後まで一致しなかった場合 */
        if (cnt >= search_num) {
            /* 活用形変形無しで返す */
            return 0;
        }

        /* 読み文字列を更新 */
        new_yomi = (NJ_CHAR*)(CONJ_GET_YOMI_TYPE2(conj_data, stem_bpos, cnt, wk_type));
        new_yomi_len = (CONJ_GET_YOMI_LEN_TYPE2(conj_data, stem_bpos, cnt, wk_type) / sizeof(NJ_CHAR));
        if (new_yomi_len > NJ_MAX_LEN) {
            /* 活用形変形無しで返す */
            return 0;
        }
        nj_strncpy(&(info_out->yomi[info_in->stem.kouho_len - search_len]), new_yomi, new_yomi_len);
        info_out->yomi[(info_in->stem.kouho_len - search_len) + new_yomi_len] = NJ_CHAR_NUL;

        /* 候補文字列を更新 */
        new_kouho = (NJ_CHAR*)CONJ_GET_KOUHO(conj_data, stem_bpos, cnt, wk_type);
        new_kouho_len = (CONJ_GET_KOUHO_LEN(conj_data, stem_bpos, cnt, wk_type) / sizeof(NJ_CHAR));
        if (new_kouho_len > NJ_MAX_LEN) {
            /* 活用形変形無しで返す */
            return 0;
        }
        nj_strncpy(&(info_out->kouho[info_in->stem.kouho_len - search_len]), new_kouho, new_kouho_len);
        info_out->kouho[(info_in->stem.kouho_len - search_len) + new_kouho_len] = NJ_CHAR_NUL;

        /* 付属語情報を設定 */
        info_out->fzk.hinsi = CONJ_GET_NEW_FZK_HINSI_TYPE2(conj_data, stem_bpos, cnt, wk_type);

        /* 付属語前品詞を取得 */
        fzk_fpos = ((info_out->fzk.hinsi & 0xFFFF0000) >> 16);

        /* 付属語後品詞を取得 */
        fzk_bpos = (info_out->fzk.hinsi & 0x0000FFFF);

        /* 自立語後品詞と付属語前品詞がいずれも「変換未使用」の場合 */
        if ((fzk_fpos == fpos_no) && (fzk_bpos == bpos_no)) {
            /* 文字列長・品詞 */
            info_out->stem.yomi_len = new_yomi_len;
            info_out->stem.kouho_len = new_kouho_len;
            info_out->stem.hinsi = CONJ_GET_NEW_STEM_HINSI(conj_data, stem_bpos, cnt, wk_type);
            info_out->fzk.yomi_len = 0;
            info_out->fzk.kouho_len = 0;
            info_out->fzk.hinsi = 0x00000000;
            *con_type = conj_type;
        } else {
            /* 文字列長・品詞 */
            new_fzk_len = (NJ_UINT8)(CONJ_GET_NEW_FZK_LEN(conj_data, stem_bpos, cnt, wk_type) / sizeof(NJ_CHAR));
            info_out->stem.yomi_len = new_yomi_len - new_fzk_len;
            info_out->stem.kouho_len = new_kouho_len - new_fzk_len;
            info_out->stem.hinsi = CONJ_GET_NEW_STEM_HINSI(conj_data, stem_bpos, cnt, wk_type);
            info_out->fzk.yomi_len = new_fzk_len;
            info_out->fzk.kouho_len = new_fzk_len;
            *con_type = conj_type;
        }

        /* 活用形変形成功で返す */
        return 1;

    default:    /* 上記以外の場合 */
        /* 活用形変形候補無しで返す */
        return 0; /*NCH*/
    }

    /* 活用形変形候補無しで返す */
    return 0;
}


/**
 * @brief
 *   付属語解析API
 *
 * @param[in]     iwnn       解析情報クラス
 * @param[in]     clause     付属語解析対象文節(NJ_RESULT*)
 * @param[in,out] fzks       付属語解析バッファ
 * @param[out]    fzk_result 付属語パターン
 *
 * @retval 0   正常終了(解析未実施)
 * @retval 1   正常終了(解析実施)
 * @retval <0  エラー
 *            - @c NJ_ERR_PARAM_ENV_NULL    解析情報クラスに NULL が指定された
 *            - @c NJ_ERR_PARAM_RESULT_NULL 付属語解析対象文節に NULL が指定された
 *            - @c NJ_ERR_PARAM_FZK_NULL    付属語解析バッファまたは付属語パターンに NULL が指定された
 *            - @c NJ_ERR_INVALID_RESULT    形態素解析結果以外の付属語解析対象文節が @a clause に渡された
 *            - その他のエラーコード        内部エラー
 *
 */
NJ_INT16 njx_fzk_split_word(NJ_CLASS *iwnn, NJ_RESULT *clause, NJ_FZK_BUF *fzks, NJ_FZK_PATTERN **fzk_result) {
    NJ_CURSOR   cur;
    NJ_UINT8    exit_flag, add_flag, force_exit_flag;
    NJ_UINT8    len, start;
    NJ_INT16    n, ret;
    NJ_RESULT   result;
    NJ_FZK_WORD *fzkinfo;


    /* パラメータチェック */
    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_CONJ_NJX_FZK_SPLIT_WORD, NJ_ERR_PARAM_ENV_NULL);
    }
    if ((fzks == NULL) || (fzk_result == NULL)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_CONJ_NJX_FZK_SPLIT_WORD, NJ_ERR_PARAM_FZK_NULL);
    }
    if ((fzks->status != FZK_SPLIT_INIT) && (clause == NULL)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_CONJ_NJX_FZK_SPLIT_WORD, NJ_ERR_PARAM_RESULT_NULL);
    }

    if (clause != NULL) {
        /* 初回呼び出し時 */

        /* 許容しない処理結果の場合、エラーとする */
        if (((NJ_GET_RESULT_OP(clause->operation_id) == NJ_OP_ANALYZE) &&
             ((NJ_GET_RESULT_FUNC(clause->operation_id) == NJ_FUNC_CONVERT_MULTIPLE) ||
              (NJ_GET_RESULT_FUNC(clause->operation_id) == NJ_FUNC_CONVERT_SINGLE)))
            ) {
            return NJ_SET_ERR_VAL(NJ_FUNC_CONJ_NJX_FZK_SPLIT_WORD, NJ_ERR_INVALID_RESULT);
        }
        
        /* 解析用ワークエリアを初期化する */
        njc_init_fzk_buf(fzks, clause);

        fzks->yomi_len = NJ_GET_YLEN_FROM_FZK(&(clause->word));
        if ( fzks->yomi_len == 0 ) {
            /* 付属語が存在しない場合は、解析用ワークエリアを初期化後、正常終了 */
            return 0;
        }

        if ( fzks->yomi_len > NJ_FZK_MAX_LEN ) {
            /* 付属語の長さが長すぎる場合、解析用ワークエリアを初期化後、エラー */
            /* 正常に動作している限りは発生しない */
            return NJ_SET_ERR_VAL(NJ_FUNC_CONJ_NJX_FZK_SPLIT_WORD, NJ_ERR_FZK_TOO_LONG);
        }

        /* 付属語部分の文字列をワークエリアにコピー */
        nj_strncpy( fzks->yomi, (clause->word.yomi + NJ_GET_YLEN_FROM_STEM(&(clause->word))), fzks->yomi_len );
        fzks->yomi[fzks->yomi_len] = NJ_CHAR_NUL;

        /* 辞書セットの取得 */
        njc_set_fzk_dics(&(iwnn->dic_set), &(fzks->fds));
        fzks->rule = fzks->fds.rHandle[NJ_MODE_TYPE_HENKAN];

        /* 品詞情報の取得 */
        njd_r_get_count(fzks->rule, &(fzks->max_posL), &(fzks->max_posR));

        /* 辞書検索カーソルの検索条件(読み以外)を設定 */
        njd_init_search_condition(&(cur.cond));
        cur.cond.operation = NJ_CUR_OP_COMP;    /* 読み解析なので、形態素解析でも正引きする */
        cur.cond.mode      = NJ_CUR_MODE_FREQ;
        cur.cond.ds        = &(fzks->fds);

        force_exit_flag = 0;

        /* 読み文字列の先頭から末尾まで解析する */
        for (start = 0; start < fzks->yomi_len; ) {
            cur.cond.yomi = fzks->yomi + start;
            /* 読み文字列長を 1〜（読み文字列末尾までの長さ）まで変更しながら、一致する付属語を探す */
            for (len = start + 1; len <= fzks->yomi_len; len++) {
                cur.cond.ylen = len - start;

                n = njd_search_word(iwnn, &cur, 1, &exit_flag);
                if (n == 0) {
                    /* 単語が見つからなければ、長さを変更して探索を続ける */
                    if (exit_flag == 1) {
                        /* 前方一致候補も存在しないので、これ以上長さを変更しながら検索しても意味なし */
                        break;
                    }
                    continue;
                } else if (n < 0) {
                    /* 単語検索処理で異常発生 */
                    return NJ_SET_ERR_VAL(NJ_FUNC_CONJ_NJX_FZK_SPLIT_WORD, NJ_GET_ERR_CODE(n)); /*NCH_FB*/
                }

                /* 単語が見つかれば、接続可能なパターンにくっ付けて新しいパターンを登録 */
                while ((ret = njd_get_word(iwnn, &cur, &result, 1)) > 0) {
                    add_flag = 0;
                    for( n = -1; ( n = (NJ_INT16)njc_get_next_fzkpat(fzks, n, start) ) >= 0; ) {
                        fzkinfo = NULL;
                        if (!njc_connect(fzks, fzks->fzkpat[n].fzk, &result.word)) {
                            continue;
                        }
                        if (!add_flag) {
                            /* まだ付属語が登録されていなければ、バッファに登録する */
                            if (fzks->fzks_next >= NJ_FZK_BUF_MAX_WORD_NUM) {
                                /* 付属語バッファが満杯になっている場合は、途中で終了 */
                                ret = njc_fzk_get_next_pattern(fzks, fzk_result);
                                return ret;
                            }
                            fzkinfo       = &(fzks->fzks[fzks->fzks_next]);
                            fzkinfo->word = result.word;
                            fzks->fzks_next++;
                            add_flag = 1;
                        }

                        /* 接続可能なパターン (fzkpat[n]) にくっ付ける新しいパターンを登録 */
                        if (fzkinfo != NULL) {
                            if (njc_push_fzkpat(fzks, fzkinfo, &(fzks->fzkpat[n]), len) < 0) {
                                /* 付属語パターンが多すぎる場合、途中で終了 */
                                ret = njc_fzk_get_next_pattern(fzks, fzk_result);
                                return ret;
                            }
                        }
                    }
                }
            }
            start += NJ_CHAR_LEN(cur.cond.yomi);
        }
    } else {
        /* clause == NULL */
    }

    /* 付属語解析パターンを取得し、返却する */
    ret = njc_fzk_get_next_pattern(fzks, fzk_result);

    return ret;
}


/**
 * @brief
 *   付属語情報取得API
 *
 * @param[in] iwnn         解析情報クラス
 * @param[in] fzks         付属語解析バッファ
 * @param[in] fzk_pattern  付属語パターン
 * @param[in] index        付属語ID（0〜NJ_FZK_MAX_LEN-1）
 * @param[out] fzk_word    付属語情報
 *
 * @retval 1 付属語情報継続あり
 * @retval 0 付属語情報の終端またはエラー
 *
 */
NJ_INT16 njx_fzk_get_info(NJ_CLASS *iwnn, NJ_FZK_BUF *fzks, NJ_FZK_PATTERN *fzk_pattern, NJ_INT32 index, NJ_FZK_WORD **fzk_word) {
    NJ_INT32 i;
    NJ_FZK_PATTERN *ptr;


    /* パラメータチェック */
    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_CONJ_NJX_FZK_SPLIT_WORD, NJ_ERR_PARAM_ENV_NULL);
    }
    if ((fzks == NULL) || (fzk_pattern == NULL) || (fzk_word == NULL)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_CONJ_NJX_FZK_SPLIT_WORD, NJ_ERR_PARAM_FZK_NULL);
    }
    if ((index < 0) || (index >= NJ_FZK_MAX_LEN)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_CONJ_NJX_FZK_SPLIT_WORD, NJ_ERR_PARAM_INDEX_INVALID);
    }

    /* 指定された番号の付属語情報までリンクを辿り、出力する */
    ptr = fzk_pattern;
    for (i = 0; i < index; i++) {
        if (ptr == NULL) {
            return 0;
        }
        ptr = ptr->prev;
    }

    /* 先頭には必ず自立語要素があるので、ptr->prev が NULL になるのは不正な状態 */
    if ((ptr == NULL) || (ptr->prev == NULL)) {
        return 0; /*NCH*/
    }

    /* 付属語情報を取得する */
    *fzk_word = (ptr->fzk);

    return 1;
}

/**
 * @brief
 *   付属語解析バッファを完全に初期化する
 *
 * @param[out] fzkbuf 付属語解析バッファ(NJ_FZK_BUF)
 * @param[in]  clause 文節情報(mmx_split_wordの結果)
 *
 * @details
 *   付属パターン取得用カーソルもあわせて初期化する
 */
static void njc_init_fzk_buf(NJ_FZK_BUF *fzkbuf, NJ_RESULT *clause) {
    NJ_UINT8 i;


    /* 辞書セットの内容を初期化する */
    for (i = 0; i < NJ_MAX_DIC; i++) {
        njd_clear_dicinfo(&(fzkbuf->fds.dic[i]));
    }
    fzkbuf->fds.rHandle[NJ_MODE_TYPE_HENKAN] = NULL;
    fzkbuf->fds.rHandle[NJ_MODE_TYPE_YOSOKU] = NULL;
    fzkbuf->fds.rHandle[NJ_MODE_TYPE_MORPHO] = NULL;
    fzkbuf->fds.keyword[0] = NJ_CHAR_NUL;
    fzkbuf->fds.keyword[1] = NJ_CHAR_NUL;
    fzkbuf->fds.mode       = NJ_CACHE_MODE_NONE;

    /* 付属語バッファの内容をクリアし、先頭に自立語情報を格納する */
    nj_memcpy((NJ_UINT8*)&(fzkbuf->fzks[0].word), (NJ_UINT8*)&(clause->word), sizeof(NJ_WORD));
    fzkbuf->fzks_next    = 1;

    /* 付属語パターンバッファの内容をクリアし、先頭に自立語情報を格納する */
    fzkbuf->fzkpat[0].fzk  = &(fzkbuf->fzks[0]);
    fzkbuf->fzkpat[0].prev = NULL;
    fzkbuf->fzkpat_tail[0] = 0;
    fzkbuf->fzkpat_num     = 1;

    /* 付属語パターン取得カーソルを初期化 */
    fzkbuf->fzkpat_current = 0;

    fzkbuf->yomi[0]  = NJ_CHAR_NUL;
    fzkbuf->yomi_len = 0;
    fzkbuf->rule     = NULL;
    fzkbuf->max_posL = 0;
    fzkbuf->max_posR = 0;
    fzkbuf->status     = FZK_SPLIT_INIT;

    return;
}


/**
 * @brief
 *   辞書セットから付属語辞書のみの設定を抜き出す
 *
 * @param[in]  dics    辞書セット
 * @param[out] fzkdic  付属語辞書のみの辞書セット
 */
static void njc_set_fzk_dics(NJ_DIC_SET *dics, NJ_DIC_SET *fzkdic) {
    NJ_DIC_INFO* dicinfo;
    NJ_UINT8 i;


    for (i = 0; i < NJ_MAX_DIC; i++) {
        /* 辞書セットの中を走査し、設定されていてかつ有効な辞書のみを選ぶ */
        dicinfo = &(dics->dic[i]);
        if ((dicinfo->handle != NULL) &&
            (NJ_CHECK_USE_DIC_FREQ(dicinfo->dic_freq) != 0)) {

            /* 付属語に関連する辞書設定のみをコピーする */
            switch (NJ_GET_DIC_TYPE_EX(dicinfo->type, dicinfo->handle)) {
            case NJ_DIC_TYPE_FZK:                           /* 付属語辞書 */
            case NJ_DIC_TYPE_PROGRAM_FZK:                   /* 擬似辞書-付属語-  */
                njd_copy_dicinfo(&(fzkdic->dic[i]), dicinfo, NJ_MODE_TYPE_HENKAN);
                break;
            default:
                break;
            }
        }
    }

    /* ルール辞書設定をコピーする */
    for (i = 0; i < NJ_MODE_TYPE_MAX; i++) {
        fzkdic->rHandle[i] = dics->rHandle[i];
    }

    return;
}


/**
 * @brief
 *   付属語が品詞的に接続可能であるか調べる
 *
 * @param[in]  fzkbuf 付属語解析バッファ
 * @param[in]  fzk1   前付属語情報
 * @param[in]  fzk2   後付属語単語情報
 *
 * @retval !0  接続可能
 * @retval  0  接続不可能
 */
static NJ_INT32 njc_connect(NJ_FZK_BUF *fzkbuf, NJ_FZK_WORD *fzk1, NJ_WORD *fzk2) {
    NJ_UINT16 posR, posL;
    NJ_UINT8 *condition;


    posR = NJ_FZK_WORD_GET_POSR(fzk1);
    posL = ((fzk2->stem.info1 >> 7) & 0x01FF);

    /* 品詞番号が範囲外なら接続不可能として帰る */
    if ((posR == 0) || (posR > fzkbuf->max_posR) ||
        (posL == 0) || (posL > fzkbuf->max_posL)) {
        return 0;   /* NCH */
    }

    /* 接続情報を取得し、接続可否の結果を返す */
    njd_r_get_connect(fzkbuf->rule, posR, 0, &condition);

    posL--;
    return ((*(condition + (posL / 8)) & (0x80 >> (posL % 8))) ? 1 : 0);
}


/**
 * @brief
 *   指定した長さに一致する、次の付属語パターンのIDを取得する
 *
 * @param[in,out] fzks  付属語解析バッファ
 * @param[in]     n     現在の付属語パターン番号（先頭から走査する場合は -1 を指定）
 * @param[in]     len   付属語長
 *
 * @retval >= 0   次の付属語パターン番号
 * @retval -1     指定条件に一致する付属語パターンが見つからなかった場合
 */
static NJ_INT32 njc_get_next_fzkpat(NJ_FZK_BUF *fzks, NJ_INT32 n, NJ_UINT8 len) {
    NJ_INT32 i;


    /* 付属語パターンを先頭から走査し、指定の長さと一致するパターンを見つける */
    for (i = n + 1; i < fzks->fzkpat_num; i++) {
        if (fzks->fzkpat_tail[i] == len) {
            return i;
        }
    }
    return -1;
}


/**
 * @brief
 *   付属語パターンを追加する
 *
 * @param[in,out] fzkbuf  付属語解析バッファ
 * @param[in]     fzk     付属語情報
 * @param[in]     prev    前連接付属語パターン
 * @param[in]     len     全付属語長
 *
 * @retval 0  正常終了
 * @retval <0 エラー（付属語パターンバッファが FULL の場合）
 */
static NJ_INT16 njc_push_fzkpat(NJ_FZK_BUF *fzkbuf, NJ_FZK_WORD *fzk, NJ_FZK_PATTERN *prev, NJ_UINT8 len) {
    NJ_FZK_PATTERN *fzkpat;


    if (fzkbuf->fzkpat_num >= NJ_FZK_BUF_MAX_PATTERN) {
        /* 付属語パターンバッファが一杯になっている場合はエラー */
        return -1;
    }

    /* 新しい付属語パターンを追加し、前連接付属語パターンに接続する */
    fzkpat       = &(fzkbuf->fzkpat[fzkbuf->fzkpat_num]);
    fzkpat->fzk  = fzk;
    fzkpat->prev = prev;
    fzkbuf->fzkpat_tail[fzkbuf->fzkpat_num] = len;


    fzkbuf->fzkpat_num++;
    return 0;
}


/**
 * @brief
 *   付属語パターンを取得する
 *
 * @param[in,out] fzks 付属語解析バッファ
 *
 * @retval !NULL 次付属語パターン
 * @retval NULL 付属語パターン取得終了またはエラー
 *
 * @retval 0   未取得
 * @retval 1   取得
 *
 */
static NJ_INT16 njc_fzk_get_next_pattern(NJ_FZK_BUF *fzks, NJ_FZK_PATTERN **pattern) {


    while ((++fzks->fzkpat_current) < fzks->fzkpat_num) {
        if (fzks->fzkpat_tail[fzks->fzkpat_current] == fzks->yomi_len) {
            /* 該当文節の付属語長と一致する（＝解析完了）パターンがあれば、候補として出力 */
            *pattern = &(fzks->fzkpat[fzks->fzkpat_current]);
            return 1;
        }
    }

    /* 解析完了したパターンが見つからない、もしくは既に全パターンを処理している場合ば NULL */
    fzks->fzkpat_current--;
    return 0;
}
