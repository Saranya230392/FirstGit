/**
 * @file
 *   他階層インターフェース部
 *
 *   エンジン内の他階層とのインターフェースを提供する
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
#include "nj_dic.h"
#include "njd.h"
#include "nj_fio.h"

/************************************************/
/*              define  宣  言                  */
/************************************************/
#define NJ_DIC_UNCOMP_EXT_HEADER_SIZE   0x002C      /**< 辞書ヘッダ: 拡張情報サイズ-非圧縮辞書- */
#define CREATE_DIC_TYPE_USER            0           /**< ユーザ辞書作成             */
#define CREATE_DIC_TYPE_USER2           3           /**< ユーザ辞書作成(付加情報有) */
#define CREATE_DIC_TYPE_LEARN_AWNN      1           /**< 学習辞書作成(AWnn互換)     */
#define CREATE_DIC_TYPE_LEARN           2           /**< 学習辞書作成               */
#define CREATE_DIC_TYPE_LEARN2          4           /**< 学習辞書作成(付加情報有)   */

#define POS_DIC_TYPE_SIZE               8           /**< 辞書タイプオフセット */

#define ADD_LEARN_FLG(h) ((NJ_UINT32)(NJ_DIC_ADD_LEARN_FLG & NJ_INT32_READ((h) + 0x24)))

#define NJD_GET_WORD_LOOP_LIMIT  1000000

#define NJ_GET_STEM_F_HINSI_FROM_NJ_WORD_INFO(word)             \
    ((NJ_UINT16)(((word)->stem.hinsi >> 16) & 0x0000FFFF))

#define NJ_GET_STEM_B_HINSI_FROM_NJ_WORD_INFO(word)     \
    ((NJ_UINT16)(((word)->stem.hinsi) & 0x0000FFFF))

#define NJ_GET_STEM_HINSI_FROM_NJ_WORD(word)                            \
    (((NJ_UINT32)NJ_GET_FPOS_FROM_STEM((word)) << 16) | (NJ_UINT32)NJ_GET_BPOS_FROM_STEM((word)))

#define NJ_GET_FZK_F_HINSI_FROM_NJ_WORD_INFO(word)              \
    ((NJ_UINT16)(((word)->fzk.hinsi >> 16) & 0x0000FFFF))

#define NJ_GET_FZK_B_HINSI_FROM_NJ_WORD_INFO(word)      \
    ((NJ_UINT16)(((word)->fzk.hinsi) & 0x0000FFFF))

#define NJ_GET_FZK_HINSI_FROM_NJ_WORD(word)                             \
    (((NJ_UINT32)NJ_GET_FPOS_FROM_FZK((word)) << 16) | (NJ_UINT32)NJ_GET_BPOS_FROM_FZK((word)))

#define GET_HYOKI_INDEX_OFFSET(cnt)                             \
    (NJ_LEARN_DIC_HEADER_SIZE + NJ_INDEX_SIZE * ((cnt)+1))

#define GET_DATA_AREA_OFFSET(cnt)                               \
    (NJ_LEARN_DIC_HEADER_SIZE + NJ_INDEX_SIZE * ((cnt)+1) * 2)

#define GET_EXT_DATA_AREA_OFFSET(cnt)                                   \
    (NJ_LEARN_DIC_HEADER_SIZE + NJ_INDEX_SIZE * ((cnt)+1) * 2 + LEARN_DIC_QUE_SIZE * (cnt))

#define GET_LEARN_ADDITIONAL_DATA_AREA_OFFSET(cnt)                                   \
    (NJ_LEARN_DIC_HEADER_SIZE + NJ_INDEX_SIZE * ((cnt)+1) * 2 + (LEARN_DIC_QUE_SIZE + LEARN_DIC_EXT_QUE_SIZE) * (cnt))

#define GET_USER_ADDITIONAL_DATA_AREA_OFFSET(cnt)                                   \
    (NJ_LEARN_DIC_HEADER_SIZE + NJ_INDEX_SIZE * ((cnt)+1) * 2 + NJ_USER_QUE_SIZE * (cnt))

#define MIN_SIZE_OF_LEARN_DIC_AWNN                                      \
    (NJ_LEARN_DIC_HEADER_SIZE + LEARN_DIC_QUE_SIZE + 2 * (NJ_INDEX_SIZE * (1+1)) + 4)

#define GET_MAX_WORD_NUM_IN_LEARN_DIC_AWNN(size)                   \
    (((size) - NJ_LEARN_DIC_HEADER_SIZE - (2 * NJ_INDEX_SIZE) - 4) \
     / (LEARN_DIC_QUE_SIZE + 2 * NJ_INDEX_SIZE))

#define MIN_SIZE_OF_LEARN_DIC_IWNN                                      \
    (NJ_LEARN_DIC_HEADER_SIZE + LEARN_DIC_QUE_SIZE                      \
     + LEARN_DIC_EXT_QUE_SIZE + 2 * (NJ_INDEX_SIZE * (1+1)) + 4)

#define GET_MAX_WORD_NUM_IN_LEARN_DIC_IWNN(size)                        \
    (((size) - NJ_LEARN_DIC_HEADER_SIZE - (2 * NJ_INDEX_SIZE) - 4)      \
     / (LEARN_DIC_QUE_SIZE + LEARN_DIC_EXT_QUE_SIZE + 2 * NJ_INDEX_SIZE))

#define MIN_SIZE_OF_LEARN_DIC_IWNN2                                     \
    (NJ_LEARN_DIC_HEADER_SIZE + LEARN_DIC_QUE_SIZE                      \
     + LEARN_DIC_EXT_QUE_SIZE                                           \
     + LEARN_DIC_ADDITIONAL_QUE_HEAD_SIZE + (NJ_MAX_ADDITIONAL_LEN * sizeof(NJ_CHAR)) \
     + 2 * (NJ_INDEX_SIZE * (1+1)) + 4)

#define GET_MAX_WORD_NUM_IN_LEARN_DIC_IWNN2(size)                        \
    (((size) - NJ_LEARN_DIC_HEADER_SIZE - (2 * NJ_INDEX_SIZE) - 4)      \
     / (LEARN_DIC_QUE_SIZE + LEARN_DIC_EXT_QUE_SIZE                     \
        + LEARN_DIC_ADDITIONAL_QUE_HEAD_SIZE + (NJ_MAX_ADDITIONAL_LEN * sizeof(NJ_CHAR)) \
        + 2 * NJ_INDEX_SIZE))

#define MIN_SIZE_OF_USER_DIC                                            \
    (NJ_LEARN_DIC_HEADER_SIZE + NJ_USER_QUE_SIZE + 2 * (NJ_INDEX_SIZE * (1+1)) + 4)

#define GET_MAX_WORD_NUM_IN_USER_DIC(size)                              \
    (((size) - NJ_LEARN_DIC_HEADER_SIZE - (2 * NJ_INDEX_SIZE) - 4)      \
     / (NJ_USER_QUE_SIZE + 2 * NJ_INDEX_SIZE))

#define MIN_SIZE_OF_USER2_DIC                                            \
    (NJ_LEARN_DIC_HEADER_SIZE + NJ_USER2_QUE_SIZE + 2 * (NJ_INDEX_SIZE * (1+1)) + 4)

#define GET_MAX_WORD_NUM_IN_USER2_DIC(size)                              \
    (((size) - NJ_LEARN_DIC_HEADER_SIZE - (2 * NJ_INDEX_SIZE) - 4)      \
     / (NJ_USER2_QUE_SIZE + 2 * NJ_INDEX_SIZE))

#define IS_HINDO_DATA_OVER(hindo, hindo_data)                           \
    ((((hindo) > 63) && !((hindo_data) & 0x40)) ||                      \
     (((hindo) <= 63) && ((hindo) > ((hindo_data) & 0x7F))) ? 1 : 0)

#define IS_END_IMP_OFFSET(offset, size)                                \
    ((offset) >= ((size) + NJ_EXT_HINDO_COMMON_HEADER_SIZE) ? 1 : 0)

#define IS_HINDO_CHECK(first, tmp_len, max_len, tmp_hindo, hindo)    \
                      ((first) ? (((tmp_len) > (max_len)) || (((tmp_len) == (max_len)) && ((tmp_hindo) > (hindo))))     \
                               : ((((tmp_len) > (max_len)) && ((tmp_hindo) == (hindo))) || ((tmp_hindo) > (hindo))))


/************************************************/
/*              extern  宣  言                  */
/************************************************/

/************************************************/
/*              prototype  宣  言               */
/************************************************/
static NJ_INT16 check_search_cursor(NJ_CLASS *iwnn, NJ_CURSOR *cursor);
static NJ_INT16 search_word(NJ_CLASS *iwnn, NJ_CURSOR *cursor, NJ_UINT8 comp_flg, NJ_UINT8 *exit_flag);
static void set_operation_id(NJ_SEARCH_LOCATION *dicinfo, NJ_UINT8 reverse, NJ_RESULT *result);
static NJ_INT16 get_word_and_search_next_word(NJ_CLASS *iwnn, NJ_CURSOR *cursor, NJ_RESULT *result, NJ_UINT8 comp_flg);
static NJ_INT16 search_next_word(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION* cond, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT32 dic_type, NJ_UINT16 dic_idx, NJ_UINT16 request, NJ_UINT8 comp_flg);
static NJ_INT16 njd_check_dic(NJ_CLASS *iwnn, NJ_UINT8 dic_type, NJ_DIC_HANDLE handle, NJ_UINT8 restore);


/************************************************/
/* Private関数                                  */
/************************************************/

/**
 * 指定された検索カーソルをチェックし、検索位置(loctset)データを初期化する。
 *
 * @param[in]     iwnn     解析情報クラス
 * @param[in,out] cursor   検索カーソル
 *
 * @retval 0    正常終了
 * @retval <0   異常終了
 */
static NJ_INT16 check_search_cursor(NJ_CLASS *iwnn, NJ_CURSOR *cursor) {
    NJ_UINT16 i, j;
    NJ_DIC_INFO *dicinfo;
    NJ_SEARCH_LOCATION_SET *loctset;


    if (cursor->cond.ds == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_CHECK_SEARCH_CURSOR, NJ_ERR_PARAM_DIC_NULL); /*NCH_FB*/
    }


    for (i = 0; i < NJ_MAX_DIC; i++) {
        loctset = &(cursor->loctset[i]);
        dicinfo = &(cursor->cond.ds->dic[i]);
        
        /* 検索単語情報構造体の初期化を行う */
        njd_init_search_location_set(loctset);

        if (dicinfo->handle != NULL) {
            /* 指定辞書があるの場合は、検索対象とする */

            /* 辞書頻度が、0 〜 1000 の範囲であるかチェック*/
            if (/* (dicinfo->dic_freq[NJ_MODE_TYPE_HENKAN].base < DIC_FREQ_BASE) || */
                (dicinfo->dic_freq[NJ_MODE_TYPE_HENKAN].high > DIC_FREQ_HIGH) ) {
                return NJ_SET_ERR_VAL(NJ_FUNC_CHECK_SEARCH_CURSOR, NJ_ERR_DIC_FREQ_INVALID);
            }

            /* カーソルの位置情報の初期設定 */
            loctset->loct.handle        = dicinfo->handle;
            loctset->loct.type          = dicinfo->type;
            for (j = 0; j < NJ_MAX_EXT_AREA; j++) {
                loctset->loct.ext_area[j] = dicinfo->ext_area[j];
            }
            for (j = 0; j < NJ_MAX_ADDITIONAL_INFO; j++) {
                loctset->loct.add_info[j] = dicinfo->add_info[j];
            }
            loctset->loct.current_info  = 0x10;  /* num=1, offset = 0    */
            loctset->loct.status        = NJ_ST_SEARCH_NO_INIT;
            loctset->dic_freq           = dicinfo->dic_freq[NJ_MODE_TYPE_HENKAN];
            loctset->dic_freq_max       = iwnn->state.calc_parameter->dicinfo_max[i];
            loctset->dic_freq_min       = iwnn->state.calc_parameter->dicinfo_min[i];
        }
    }

    if (cursor->cond.yomi == NULL) {
        /* 読み文字列は必須、空文字列は許す                     */
        return NJ_SET_ERR_VAL(NJ_FUNC_CHECK_SEARCH_CURSOR, NJ_ERR_PARAM_YOMI_NULL);
    } 

    if ((cursor->cond.operation == NJ_CUR_OP_REV) ||
        (cursor->cond.operation == NJ_CUR_OP_REV_FORE)) {
        /* 逆引き検索時は、検索文字列が NJ_MAX_RESULT_LENを
         * 超えたらエラーとする */
        if (cursor->cond.ylen > NJ_MAX_RESULT_LEN) {
            return NJ_SET_ERR_VAL(NJ_FUNC_CHECK_SEARCH_CURSOR, NJ_ERR_YOMI_TOO_LONG);
        }
    } else {
        /* 逆引き検索以外は、検索文字列が NJ_MAX_LENを
         * 超えたらエラーとする */
        if (cursor->cond.ylen > NJ_MAX_LEN) {
            /* 読み文字列が長過ぎる     */
            return NJ_SET_ERR_VAL(NJ_FUNC_CHECK_SEARCH_CURSOR, NJ_ERR_YOMI_TOO_LONG);
        }
    }

    if ((cursor->cond.operation == NJ_CUR_OP_LINK) && (cursor->cond.kanji == NULL)) {
        /* つながりのときは候補文字列は必須、空文字列は許す     */
        return NJ_SET_ERR_VAL(NJ_FUNC_CHECK_SEARCH_CURSOR, NJ_ERR_PARAM_KANJI_NULL);
    } else if (cursor->cond.kanji != NULL) {
        /* NULLチェックの後、候補文字列長チェック       */
        if (nj_strlen(cursor->cond.kanji) > NJ_MAX_RESULT_LEN) {
            return NJ_SET_ERR_VAL(NJ_FUNC_CHECK_SEARCH_CURSOR, NJ_ERR_CANDIDATE_TOO_LONG);
        }
    }

    switch (cursor->cond.operation) {
    case NJ_CUR_OP_COMP:
    case NJ_CUR_OP_FORE:
    case NJ_CUR_OP_LINK:
    case NJ_CUR_OP_REV:
    case NJ_CUR_OP_REV_FORE:
    case NJ_CUR_OP_COMP_EXT:
    case NJ_CUR_OP_FORE_EXT:
        break;
    default:
        return NJ_SET_ERR_VAL(NJ_FUNC_CHECK_SEARCH_CURSOR, NJ_ERR_PARAM_OPERATION);
    }

    switch (cursor->cond.mode) {
    case NJ_CUR_MODE_FREQ:
    case NJ_CUR_MODE_YOMI:
    case NJ_CUR_MODE_REGIST:
        break;
    default:
        return NJ_SET_ERR_VAL(NJ_FUNC_CHECK_SEARCH_CURSOR, NJ_ERR_PARAM_MODE);
    }

    return 0;
}


/**
 * 検索カーソルから初期候補を検索する
 *
 * @param[in]     iwnn      解析情報クラス
 * @param[in,out] cursor    検索カーソル
 * @param[in]     comp_flg  学習辞書、統合辞書の完全一致で複数キュー対象の有無
 *                           （1:対象、1以外:対象としない）
 * @param[out]    exit_flag 前方一致候補有無 
 *                           （0:有り、1:無し）
 *
 * @retval 0  検索候補なし
 * @retval 1  検索候補あり
 * @retval <0 エラー
 */
static NJ_INT16 search_word(NJ_CLASS *iwnn, NJ_CURSOR *cursor, NJ_UINT8 comp_flg,
                            NJ_UINT8 *exit_flag) {
    NJ_UINT32 dic_type;
    NJ_INT16 i;
    NJ_INT16 ret = 0;
    NJ_INT16 flag = 0;
    NJ_SEARCH_LOCATION_SET *loctset;
    NJ_PROGRAM_DIC_IF program_dic_operation;
    NJ_PROGRAM_DIC_MESSAGE prog_msg;
    NJ_UINT8 ext_flag;
    NJ_UINT8 lop_flag = 1;
    NJ_ANALYZE_ENV *env;


    *exit_flag = 1;
    ext_flag = 0;
    env = &(iwnn->environment);
    while (lop_flag) {
        for (i = 0; i < NJ_MAX_DIC; i++) {
            loctset = &(cursor->loctset[i]);
            /* 各辞書用絞込検索対象候補確認フラグの初期化 */
            iwnn->wk_relation_cand_flg[i] = 0;
            if (loctset->loct.handle == NULL) {
                continue;   /* 検索対象外辞書 */
            }

            /* 辞書種別を取得 */
            dic_type = NJ_GET_DIC_TYPE_EX(loctset->loct.type, loctset->loct.handle);

            /* 拡張入力データが設定されている場合 */
            if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
                /* 拡張入力検索フラグをONにする */
                ext_flag = 1;
                /* キャッシュ領域が設定されている場合、検索キャッシュをクリア */
                if (cursor->cond.ds->dic[i].srhCache != NULL) {
                    cursor->cond.ds->dic[i].srhCache->statusFlg = 0;
                    cursor->cond.ds->dic[i].srhCache->viewCnt = 0;
                    cursor->cond.ds->dic[i].srhCache->keyPtr[0] = 0;
                    cursor->cond.ds->dic[i].srhCache->keyPtr[1] = 0;
                }
            }

            switch (dic_type) {
            case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書       */
            case NJ_DIC_TYPE_FUSION:                        /* 統合辞書       */
            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
                ret = njd_t_search_word(iwnn, &cursor->cond, loctset, comp_flg, (NJ_UINT16)i);
                break;
            case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
            case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
                ret = njd_s_search_word(iwnn, &cursor->cond, loctset, comp_flg, (NJ_UINT16)i);
                break;
            case NJ_DIC_TYPE_PROGRAM:                       /* 擬似辞書         */
            case NJ_DIC_TYPE_PROGRAM_FZK:                   /* 擬似辞書-付属語- */
                /* 擬似辞書呼出 */
                /* 関数ポインタの設定 */
                program_dic_operation = (NJ_PROGRAM_DIC_IF)(loctset->loct.handle);
                /* 擬似辞書メッセージを設定 */
                njd_init_program_dic_message(&prog_msg);
                prog_msg.condition = &cursor->cond;
                prog_msg.location = loctset;
                prog_msg.dicset = cursor->cond.ds;
                prog_msg.dic_idx = i;
                ret = (*program_dic_operation)(iwnn, NJG_OP_SEARCH, &prog_msg);
                if (ret < 0) {
                    return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, GET_ERR_FUNCVAL(ret));
                }
                break;
            case NJ_DIC_TYPE_JIRITSU:                       /* 自立語辞書             */
            case NJ_DIC_TYPE_FZK:                           /* 付属語辞書             */
            case NJ_DIC_TYPE_TANKANJI:                      /* 単漢字辞書             */
            case NJ_DIC_TYPE_STDFORE:                       /* 標準予測辞書           */
            case NJ_DIC_TYPE_CUSTOM_COMPRESS:               /* 圧縮カスタマイズ辞書   */
            case NJ_DIC_TYPE_FORECONV:                      /* 予測変換辞書           */
                ret = njd_b_search_word(&cursor->cond, loctset);
                break;
            case NJ_DIC_TYPE_LEARN:                         /* 学習辞書               */
            case NJ_DIC_TYPE_USER:                          /* ユーザ辞書 */
            case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:             /* 非圧縮カスタマイズ辞書 */
            case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:       /* 非圧縮カスタマイズ辞書(学習辞書変更) */
                ret = njd_l_search_word(iwnn, &cursor->cond, loctset, comp_flg);
                break;

            case NJ_DIC_TYPE_YOMINASHI:                     /* 読み無し予測辞書 */
                ret = njd_f_search_word(&cursor->cond, loctset);
                break;

            case NJ_DIC_TYPE_EXT_YOMINASI:                  /* 拡張読み無し予測辞書 */
                ret = njd_p_search_word(iwnn, &cursor->cond, loctset, i);
                break;

            default:
                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_WORD, NJ_ERR_DIC_TYPE_INVALID);
            }
            if (ret < 0) {
                return ret;
            }
            if (ret == 0) {
                if ((GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_END)
                    && (*exit_flag == 1)) {
                    *exit_flag = 0;
                }
                /* NJ_ST_SEARCH_END_EXTはNJ_ST_SEARCH_ENDに書き換え */
                loctset->loct.status = NJ_ST_SEARCH_END;
            
            } else {
                flag = 1;
                *exit_flag = 0;
            }
        }

        /* 拡張領域の指定があり、単語検索処理以外の場合に検索方法を切り替える。 */
        if ((!flag) &&
            (ext_flag) && 
            ((cursor->cond.ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00)) {
            switch (cursor->cond.operation) {
            case NJ_CUR_OP_COMP:
            case NJ_CUR_OP_FORE:
                if (cursor->cond.operation == NJ_CUR_OP_COMP) {
                    /* 正引き完全一致拡張検索へ移行 */
                    cursor->cond.operation = NJ_CUR_OP_COMP_EXT;
                } else {
                    /* 正引き前方一致拡張検索へ移行 */
                    cursor->cond.operation = NJ_CUR_OP_FORE_EXT;
                }
                for (i = 0; i < NJ_MAX_DIC; i++) {
                    loctset = &(cursor->loctset[i]);
                    if (loctset->loct.handle == NULL) {
                        continue;   /* 検索対象外辞書 */
                    }

                    dic_type = NJ_GET_DIC_TYPE_EX(loctset->loct.type, loctset->loct.handle);

                    switch (dic_type) {
                    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:            /* 統合辞書(高圧縮タイプ) */
                    case NJ_DIC_TYPE_FUSION_AWNN:                     /* 統合辞書(AWnnタイプ) */
                    case NJ_DIC_TYPE_FUSION:                          /* 統合辞書 */
                    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:    /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
                    case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:             /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
                    case NJ_DIC_TYPE_FUSION_STORAGE:                  /* 統合辞書(ストレージ辞書) */
                    case NJ_DIC_TYPE_PROGRAM:                         /* 擬似辞書 */
                    case NJ_DIC_TYPE_PROGRAM_FZK:                     /* 擬似辞書-付属語- */
                        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
                            /* 拡張インデックス領域が指定されている場合のみ、statusを切り替える */
                            loctset->loct.status = NJ_ST_SEARCH_NO_INIT;
                        }
                        break;
                    case NJ_DIC_TYPE_LEARN:                           /* 学習辞書 */
                        if (((cursor->cond.ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) &&
                            (iwnn->njc_mode == 0) &&
                            ((iwnn->environment.type == NJ_ANALYZE_NEXT_YOSOKU) ||
                             (iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH) ||
                             (iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI))) {
                            /* 学習辞書内最大予測候補取得数未満の場合のみ処理を行う */
                            if (env->learn_cnt < env->option.forecast_learn_limit) {
                                /* statusを切り替え */
                                loctset->loct.status = NJ_ST_SEARCH_NO_INIT;
                            }
                        } else {
                            /* statusを切り替え */
                            loctset->loct.status = NJ_ST_SEARCH_NO_INIT;
                        }
                        break;
                    default:
                        /* 上記以外は無視する */
                        break;
                    }
                }
                break;
            default:
                /* それ以外は処理を抜ける為、ループフラグをOFFにする */
                lop_flag = 0;
                break;
            }
        } else {
            lop_flag = 0;
        }
    }

    return flag;
}


/**
 * 検索カーソルから、候補情報を取得する
 * また、候補情報取得後、次候補の検索を行う。
 *
 * @param[in]     iwnn      解析情報クラス
 * @param[in,out] cursor    検索カーソル
 * @param[out]    result    検索結果
 * @param[in]     comp_flg  学習辞書、統合辞書の完全一致で複数キュー対象の有無
 *                           （1:対象、1以外:対象としない）
 *
 * @retval 0  候補なし
 * @retval 1  候補あり
 * @retval <0 異常終了
 */
static NJ_INT16 get_word_and_search_next_word(NJ_CLASS *iwnn, NJ_CURSOR *cursor, NJ_RESULT *result,
                                              NJ_UINT8 comp_flg) {
    NJ_INT16  ret = -1;
    NJ_INT32  i, next, first;
    NJ_WORD   tmp_word;
    NJ_RESULT tmp_result;
    NJ_CHAR   tmp_stroke[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_CHAR   result_stroke[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_INT32  j, max_len = 0;
    NJ_UINT32 dic_type;
    NJ_SEARCH_LOCATION_SET *loctset;
    NJ_UINT8 ext_flag;
    NJ_UINT8 lop_flag = 1;
    NJ_UINT8 search_flag = 0;
    NJ_UINT8 exit_flag;
    NJ_ANALYZE_ENV *env;


    next = -1;
    first= 0;
    /* tmp_wordとresultを初期化 */
    njd_init_word(&tmp_word);

    result->word = tmp_word;
    tmp_result.word = tmp_word;

    ext_flag = 0;
    env = &(iwnn->environment);
    while (lop_flag) {

        for (i = 0; i < NJ_MAX_DIC; i++) {

            loctset = &(cursor->loctset[i]);

            /*
             * 検索対象の辞書であるかを調べる
             * ・辞書ハンドルチェック(未使用ハンドル時は次に)
             */
            if (loctset->loct.handle == NULL) {
                continue;
            }

            /* 辞書タイプを取得する */
            dic_type = NJ_GET_DIC_TYPE_EX(loctset->loct.type, loctset->loct.handle);

            /* 拡張入力データが設定されている場合 */
            if ((!ext_flag) && 
                (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL)) {
                /* 拡張入力検索フラグをONにする */
                ext_flag = 1;
            }

            /*
             * 検索対象の辞書であるかを調べる
             * ・カーソルチェック(単語がなければ次に)
             */
            if ((GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_END) ||
                (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_END_EXT)) {
                continue;
            }

            switch (dic_type) {
            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:            /* 統合辞書(高圧縮タイプ) */
            case NJ_DIC_TYPE_FUSION_AWNN:                     /* 統合辞書             */
            case NJ_DIC_TYPE_FUSION:                          /* 統合辞書             */
            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:    /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
            case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:             /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
            case NJ_DIC_TYPE_FUSION_STORAGE:                  /* 統合辞書(ストレージ辞書) */
            case NJ_DIC_TYPE_JIRITSU:                         /* 自立語辞書           */
            case NJ_DIC_TYPE_FZK:                             /* 付属語辞書           */
            case NJ_DIC_TYPE_TANKANJI:                        /* 単漢字辞書           */
            case NJ_DIC_TYPE_STDFORE:                         /* 標準予測辞書         */
            case NJ_DIC_TYPE_CUSTOM_COMPRESS:                 /* 圧縮カスタマイズ辞書 */
            case NJ_DIC_TYPE_FORECONV:                        /* 予測変換辞書         */
            case NJ_DIC_TYPE_PROGRAM:                         /* 擬似辞書             */
            case NJ_DIC_TYPE_PROGRAM_FZK:                     /* 擬似辞書-付属語-     */
                /* 圧縮辞書フォーマットの場合は検索結果から読みを取得できないため、 */
                /* 先に読みと読み長をセットしておく                                 */
                tmp_word.yomi = cursor->cond.yomi;
                tmp_word.stem.info1 = cursor->cond.ylen;
                tmp_result.word.yomi = cursor->cond.yomi;
                tmp_result.word.stem.info1 = cursor->cond.ylen;
                break;
            default:
                break;
            }

            /* 
             * 候補を頻度順で取得するモード時は、
             * 取得順をあいまい度合低い順→頻度順に切り替える
             */
            if ((iwnn->option_data.ext_mode & NJ_OPT_FORECAST_TOP_FREQ) &&
                (cursor->cond.mode == NJ_CUR_MODE_FREQ) &&
                (cursor->cond.ds->mode & (NJ_CACHE_MODE_VALID | NJ_CACHE_MODE_VALID_FUSION)) &&
                (cursor->cond.ds->dic[i].srhCache != NULL) &&
                (NJ_GET_AIMAI_FROM_SCACHE(cursor->cond.ds->dic[i].srhCache)) &&
                (cursor->cond.operation == NJ_CUR_OP_FORE)) {
                /* あいまい度合低い順→頻度順の切替 */
                ret = search_next_word(iwnn, &cursor->cond, loctset, dic_type, (NJ_UINT16)i, NJG_OP_SEARCH, comp_flg);
                if (ret < 0) {
                    return ret; /*NCH_FB*/
                }
            }

            loctset->loct.status |= SET_LOCATION_OPERATION(cursor->cond.operation);
            if (cursor->cond.mode == NJ_CUR_MODE_FREQ) {
                if ((cursor->cond.ds->mode & (NJ_CACHE_MODE_VALID | NJ_CACHE_MODE_VALID_FUSION)) &&
                    (cursor->cond.ds->dic[i].srhCache != NULL) &&
                    (NJ_GET_AIMAI_FROM_SCACHE(cursor->cond.ds->dic[i].srhCache)) &&
                    ((cursor->cond.operation == NJ_CUR_OP_FORE) || (cursor->cond.operation == NJ_CUR_OP_FORE_EXT))) {
                    /* あいまい度合が低い候補を返す場合       */
                    /* 入力読み文字列に一番近い候補を返す     */
                    first = 1;

                    /* 辞書から候補データを取得する */
                    ret = njd_get_word_data(iwnn, cursor->cond.ds, loctset, (NJ_UINT16)i, &tmp_result.word);
                    if (ret < 0) {
                        return ret; /*NCH_FB*/
                    }

                    if (cursor->cond.operation == NJ_CUR_OP_FORE) {
                        /* 候補データからよみを取得する */
                        ret = njd_get_stroke(iwnn, &tmp_result, tmp_stroke, sizeof(tmp_stroke));
                        if (ret <= 0) {
                            if ((ret == 0) || (NJ_GET_ERR_CODE(ret) == NJ_ERR_BUFFER_NOT_ENOUGH)) { /*NCH_FB*/
                                return NJ_SET_ERR_VAL(NJ_FUNC_GET_WORD_AND_SEARCH_NEXT_WORD, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
                            } else {
                                return ret; /*NCH_FB*/
                            }
                        }
                    } else {
                        /* 拡張検索の場合 */
                        /* 候補データから表記を取得する */
                        ret = njd_get_candidate(iwnn, &tmp_result, tmp_stroke, sizeof(tmp_stroke));
                        if (ret <= 0) {
                            if ((ret == 0) || (NJ_GET_ERR_CODE(ret) == NJ_ERR_BUFFER_NOT_ENOUGH)) { /*NCH_FB*/
                                return NJ_SET_ERR_VAL(NJ_FUNC_GET_WORD_AND_SEARCH_NEXT_WORD, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
                            } else {
                                return ret; /*NCH_FB*/
                            }
                        }
                    }

                    /* 入力読み文字列と候補読み文字列が何配列要素一致するかチェックを行う */
                    for (j = 0; j < cursor->cond.ylen; j++) {
                        if (cursor->cond.yomi[j] != tmp_stroke[j]) {
                            break;
                        }
                    }

                    /* あいまい度合低い順→頻度順の切替 */
                    ret = search_next_word(iwnn, &cursor->cond, loctset, dic_type, (NJ_UINT16)i, NJG_OP_SEARCH, comp_flg);

                    if (ret < 0) {
                        return ret; /*NCH_FB*/
                    }
                } else {
                    /* 頻度順を返す場合 */
                    /* 辞書から候補データを取得する */
                    ret = njd_get_word_data(iwnn, cursor->cond.ds, loctset, (NJ_UINT16)i, &tmp_result.word);
                    if (ret < 0) {
                        return ret; /*NCH_FB*/
                    }

                    if (cursor->cond.operation == NJ_CUR_OP_FORE_EXT) {
                        /* 候補データから表記を取得する */
                        ret = njd_get_candidate(iwnn, &tmp_result, tmp_stroke, sizeof(tmp_stroke));
                        if (ret <= 0) {
                            if ((ret == 0) || (NJ_GET_ERR_CODE(ret) == NJ_ERR_BUFFER_NOT_ENOUGH)) { /*NCH_FB*/
                                return NJ_SET_ERR_VAL(NJ_FUNC_GET_WORD_AND_SEARCH_NEXT_WORD, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
                            } else {
                                return ret; /*NCH_FB*/
                            }
                        }

                        /* 入力読み文字列と候補読み文字列が何配列要素一致するかチェックを行う */
                        for (j = 0; j < cursor->cond.ylen; j++) {
                            if (cursor->cond.yomi[j] != tmp_stroke[j]) {
                                break;
                            }
                        }
                    } else {
                        /* 入力文字列とのチェック不要のため、そのまま読み配列要素数を格納 */
                        j = cursor->cond.ylen;

                    }
                }

                if (IS_HINDO_CHECK(first, j, max_len, tmp_result.word.stem.hindo, result->word.stem.hindo) ||
                    (next == -1)) {
                    /* 検索結果を格納       */
                    result->word = tmp_result.word;
                    if ((cursor->cond.operation == NJ_CUR_OP_REV) || 
                        (cursor->cond.operation == NJ_CUR_OP_REV_FORE)) {
                        set_operation_id(&(loctset->loct), 1, result);
                    } else {
                        set_operation_id(&(loctset->loct), 0, result);
                    }
                    /* 上位へ返す候補が絞込検索候補であった場合 */
                    if (iwnn->wk_relation_cand_flg[i] == 1) {
                        /* 絞込検索候補フラグの立てる */
                        iwnn->relation_cand_flg = 1;
                    } else {
                        /* 絞込検索候補フラグの落とす */
                        iwnn->relation_cand_flg = 0;
                    }
                    next = i;  /* 上位へ返す候補を持つ辞書位置をセーブ */
                    max_len = j;
                }

            } else {
                /* 辞書から候補データを取得する */
                ret = njd_get_word_data(iwnn, cursor->cond.ds, loctset, (NJ_UINT16)i, &(tmp_result.word));
                if (ret < 0) {
                    return ret; /*NCH_FB*/
                }

                /* 読み順   */
                ret = njd_get_stroke(iwnn, &tmp_result, tmp_stroke, sizeof(tmp_stroke));
                if (ret <= 0) {
                    if ((ret == 0) || (NJ_GET_ERR_CODE(ret) == NJ_ERR_BUFFER_NOT_ENOUGH)) { /*NCH_FB*/
                        return NJ_SET_ERR_VAL(NJ_FUNC_GET_WORD_AND_SEARCH_NEXT_WORD, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
                    } else {
                        return ret; /*NCH_FB*/
                    }
                }
                /* 最初は無条件、今取得したtmp_strokeの方が前だったら   */
                /* tmp_strokeにコピー                                   */
                if ((next == -1) || (nj_strcmp(result_stroke, tmp_stroke) > 0)) {
                    /* 検索結果を格納       */
                    result->word = tmp_result.word;
                    if ((cursor->cond.operation == NJ_CUR_OP_REV) || 
                        (cursor->cond.operation == NJ_CUR_OP_REV_FORE)) {
                        set_operation_id(&(loctset->loct), 1, result);
                    } else {
                        set_operation_id(&(loctset->loct), 0, result);
                    }
                    /* 上位へ返す候補が絞込検索候補であった場合 */
                    if (iwnn->wk_relation_cand_flg[i] == 1) {
                        /* 絞込検索候補フラグの立てる */
                        iwnn->relation_cand_flg = 1; /*NCH*/
                    } else {
                        /* 絞込検索候補フラグの落とす */
                        iwnn->relation_cand_flg = 0;
                    }
                    next = i;  /* 上位へ返す候補を持つ辞書位置をセーブ */
                    nj_strcpy(result_stroke, tmp_stroke);
                }
            }
        }

        /* 返す候補が無い場合 */
        if (next == -1) {
            /* 拡張領域の指定があり、単語検索処理以外の場合に検索方法を切り替える。 */
            if ((ext_flag) &&
                ((cursor->cond.ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00)) {
                switch (cursor->cond.operation) {
                case NJ_CUR_OP_COMP:
                case NJ_CUR_OP_FORE:
                    if (cursor->cond.operation == NJ_CUR_OP_COMP) {
                        /* 正引き完全一致拡張検索へ移行 */
                        cursor->cond.operation = NJ_CUR_OP_COMP_EXT;
                    } else {
                        /* 正引き前方一致拡張検索へ移行 */
                        cursor->cond.operation = NJ_CUR_OP_FORE_EXT;
                    }
                    for (i = 0; i < NJ_MAX_DIC; i++) {
                        loctset = &(cursor->loctset[i]);
                        if (loctset->loct.handle == NULL) {
                            continue;
                        }

                        /* 辞書タイプを取得する */
                        dic_type = NJ_GET_DIC_TYPE_EX(loctset->loct.type, loctset->loct.handle);

                        switch (dic_type) {
                        case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
                        case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書(AWnnタイプ) */
                        case NJ_DIC_TYPE_FUSION:                        /* 統合辞書 */
                        case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
                        case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
                        case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
                            if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
                                /* 拡張インデックス領域が指定されている場合のみ、statusを切り替える */
                                loctset->loct.status = NJ_ST_SEARCH_NO_INIT;
                                /* キャッシュ領域が設定されている場合、検索キャッシュをクリア */
                                if (cursor->cond.ds->dic[i].srhCache != NULL) {
                                    cursor->cond.ds->dic[i].srhCache->statusFlg = 0;
                                    cursor->cond.ds->dic[i].srhCache->viewCnt = 0;
                                    cursor->cond.ds->dic[i].srhCache->keyPtr[0] = 0;
                                    cursor->cond.ds->dic[i].srhCache->keyPtr[1] = 0;
                                }
                                search_flag = 1;
                            }
                            break;
                        case NJ_DIC_TYPE_PROGRAM:                       /* 擬似辞書 */
                        case NJ_DIC_TYPE_PROGRAM_FZK:                   /* 擬似辞書-付属語- */
                            if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
                                /* 拡張インデックス領域が指定されている場合のみ、statusを切り替える */
                                loctset->loct.status = NJ_ST_SEARCH_NO_INIT;
                                search_flag = 1;
                            }
                            break;
                        case NJ_DIC_TYPE_LEARN:                         /* 学習辞書 */
                            if (((cursor->cond.ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) &&
                                (iwnn->njc_mode == 0) &&
                                ((iwnn->environment.type == NJ_ANALYZE_NEXT_YOSOKU) ||
                                 (iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH) ||
                                 (iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI))) {
                                /* 学習辞書内最大予測候補取得数未満の場合のみ処理を行う */
                                if (env->learn_cnt < env->option.forecast_learn_limit) {
                                    /* statusを切り替え */
                                    loctset->loct.status = NJ_ST_SEARCH_NO_INIT;
                                    search_flag = 1;
                                }
                            } else {
                                /* statusを切り替え */
                                loctset->loct.status = NJ_ST_SEARCH_NO_INIT;
                                search_flag = 1;
                            }
                            break;
                        default:
                            /* 上記以外は無視する */
                            break;
                        }
                    }
                    if (search_flag) {
                        /* 検索種別が切り替わっているので、単語検索処理を実施 */
                        ret = search_word(iwnn, cursor, comp_flg, &exit_flag);
                        if (ret < 0) {
                            return ret;
                        }
                    }
                    break;
                default:
                    /* それ以外は候補なし */
                    lop_flag = 0;
                    return 0;
                }
            } else {
                lop_flag = 0;
                return 0;
            }
        } else {
            lop_flag = 0;
        }
    }

    loctset = &(cursor->loctset[next]);
    if ((!first) ||
        ((loctset->loct.handle != NULL) &&
         (cursor->cond.ds->dic[next].srhCache == NULL))) {
        dic_type = NJ_GET_DIC_TYPE_EX(loctset->loct.type, loctset->loct.handle);

        /* 上位へ返す候補(word)を含む辞書に対し、次候補を検索 */
        ret = search_next_word(iwnn, &cursor->cond, loctset, dic_type, (NJ_UINT16)next, NJG_OP_SEARCH_NEXT, comp_flg);
    }

    if (ret < 0) {
        return ret; /*NCH_FB*/
    }
    return 1;
}


/**
 * 検索カーソルを使い、次候補の検索を行う。
 *
 * @param[in]     iwnn      解析情報クラス
 * @param[in,out] condition 辞書検索条件セット構造体
 * @param[in,out] loctset   候補情報 辞書内格納位置
 * @param[in]     dic_type  辞書種別
 * @param[in]     dic_idx   辞書マウント位置
 * @param[in]     comp_flg  学習辞書、統合辞書の完全一致で複数キュー対象の有無
 *                           （1:対象、1以外:対象としない）
 *
 * @retval 0  候補なし
 * @retval 1  候補あり
 * @retval <0 異常終了
 */
static NJ_INT16 search_next_word(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION* cond, NJ_SEARCH_LOCATION_SET *loctset,
                                 NJ_UINT32 dic_type, NJ_UINT16 dic_idx, NJ_UINT16 request, NJ_UINT8 comp_flg) {
    NJ_INT16  ret = -1;
    NJ_PROGRAM_DIC_IF program_dic_operation;
    NJ_PROGRAM_DIC_MESSAGE prog_msg;


    switch (dic_type) {
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書                     */
    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書                     */
        ret = njd_t_search_word(iwnn, cond, loctset, comp_flg, dic_idx);
        break;

    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
    case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書)     */
        ret = njd_s_search_word(iwnn, cond, loctset, comp_flg, dic_idx);
        break;

    case NJ_DIC_TYPE_PROGRAM:                       /* 擬似辞書                     */
    case NJ_DIC_TYPE_PROGRAM_FZK:                   /* 擬似辞書-付属語-             */
        /* 擬似辞書呼出 */
        /* 関数ポインタの設定 */
        program_dic_operation = (NJ_PROGRAM_DIC_IF)(loctset->loct.handle);
        /* 擬似辞書メッセージを設定 */
        njd_init_program_dic_message(&prog_msg);
        prog_msg.condition = cond;
        prog_msg.location = loctset;
        prog_msg.dicset = (NJ_DIC_SET*)(cond->ds);
        prog_msg.dic_idx = dic_idx;
        ret = (*program_dic_operation)(iwnn, request, &prog_msg);
        if (ret < 0) {
            return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, GET_ERR_FUNCVAL(ret));
        }
        break;

    case NJ_DIC_TYPE_JIRITSU:                       /* 自立語辞書                   */
    case NJ_DIC_TYPE_FZK:                           /* 付属語辞書                   */
    case NJ_DIC_TYPE_TANKANJI:                      /* 単漢字辞書                   */
    case NJ_DIC_TYPE_STDFORE:                       /* 標準予測辞書                 */
    case NJ_DIC_TYPE_CUSTOM_COMPRESS:               /* 圧縮カスタマイズ辞書         */
    case NJ_DIC_TYPE_FORECONV:                      /* 予測変換辞書           */
        ret = njd_b_search_word(cond, loctset);
        break;

    case NJ_DIC_TYPE_LEARN:                         /* 学習辞書                     */
    case NJ_DIC_TYPE_USER:                          /* ユーザ辞書                   */
    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:             /* 非圧縮カスタマイズ辞書       */
    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:       /* 非圧縮カスタマイズ辞書(学習辞書変更) */
        ret = njd_l_search_word(iwnn, cond, loctset, comp_flg);
        break;

    case NJ_DIC_TYPE_YOMINASHI:                     /* 読み無し予測辞書 */
        ret = njd_f_search_word(cond, loctset);
        break;

    case NJ_DIC_TYPE_EXT_YOMINASI:                  /* 拡張読み無し予測辞書 */
        ret = njd_p_search_word(iwnn, cond, loctset, dic_idx);
        break;

    default:
        return NJ_SET_ERR_VAL(NJ_FUNC_GET_WORD_AND_SEARCH_NEXT_WORD, NJ_ERR_DIC_TYPE_INVALID); /*NCH*/
    }

    return ret;
}


/**
 * 検索カーソルから候補データを取得する
 *
 * @param[in]  iwnn     解析情報クラス
 * @param[in]  dicset   検索辞書セット
 * @param[in]  loctset  候補情報 辞書内格納位置
 * @param[in]  dic_idx  辞書マウント位置
 * @param[out] word     候補データ格納バッファ
 *
 * @retval 0  正常終了
 * @retval <0 異常終了
 */
NJ_INT16 njd_get_word_data(NJ_CLASS *iwnn, NJ_DIC_SET *dicset, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 dic_idx, NJ_WORD *word) {
    NJ_INT16 ret = 0;
    NJ_UINT32 dic_type;
    NJ_PROGRAM_DIC_IF program_dic_operation;
    NJ_PROGRAM_DIC_MESSAGE prog_msg;


    /* NJ_ST_SEARCH_ENDなら候補なし     */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_END) {
        return 0; /*NCH_FB*/
    }

    if (loctset->loct.handle == NULL) {
        /*
         * 本条件で呼び出されることはないが、
         * 念のためにNULL参照しないように本ロジックを入れる。
         */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_WORD_DATA, NJ_ERR_DIC_TYPE_INVALID); /*NCH*/
    }
    /* 擬似辞書との処理切り分け */
    dic_type = NJ_GET_DIC_TYPE_EX(loctset->loct.type, loctset->loct.handle);

    switch (dic_type) {
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書             */
    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書             */
        ret = njd_t_get_word(loctset, word);
        break;

    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
    case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
        ret = njd_s_get_word(loctset, word);
        break;

    case NJ_DIC_TYPE_PROGRAM:                       /* 擬似辞書             */
    case NJ_DIC_TYPE_PROGRAM_FZK:                   /* 擬似辞書-付属語-     */
        /* 擬似辞書呼出*/
        /* 関数ポインタの設定 */
        program_dic_operation = (NJ_PROGRAM_DIC_IF)(loctset->loct.handle);
        /* 擬似辞書メッセージを設定 */
        njd_init_program_dic_message(&prog_msg);
        prog_msg.location = loctset;
        prog_msg.dicset = dicset;
        prog_msg.word = word;
        prog_msg.dic_idx = dic_idx;
        ret = (*program_dic_operation)(iwnn, NJG_OP_GET_WORD_INFO, &prog_msg);
        if (ret < 0) {
            return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, GET_ERR_FUNCVAL(ret));
        }
        break;

    case NJ_DIC_TYPE_JIRITSU:                   /* 自立語辞書           */
    case NJ_DIC_TYPE_FZK:                       /* 付属語辞書           */
    case NJ_DIC_TYPE_TANKANJI:                  /* 単漢字辞書           */
    case NJ_DIC_TYPE_STDFORE:                   /* 標準予測辞書           */
    case NJ_DIC_TYPE_CUSTOM_COMPRESS:           /* 圧縮カスタマイズ辞書 */
    case NJ_DIC_TYPE_FORECONV:                  /* 予測変換辞書         */
        ret = njd_b_get_word(loctset, word);
        break;

    case NJ_DIC_TYPE_LEARN:                     /* 学習辞書               */
    case NJ_DIC_TYPE_USER:                      /* ユーザ辞書 */
    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:         /* 非圧縮カスタマイズ辞書 */
    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:   /* 非圧縮カスタマイズ辞書(学習辞書変更) */
        ret = njd_l_get_word(iwnn, loctset, word);
        break;

    case NJ_DIC_TYPE_YOMINASHI:                 /* 読み無し予測辞書 */
        ret = njd_f_get_word(loctset, word);
        break;

    case NJ_DIC_TYPE_EXT_YOMINASI:              /* 拡張読み無し予測辞書 */
        ret = njd_p_get_word(loctset, word);
        break;

    default:
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_WORD_DATA, NJ_ERR_DIC_TYPE_INVALID); /*NCH*/
    }
    return ret;
}


/**
 * 検索結果にoperation_idをセットする
 *
 * @param[in]  dicinfo   検索位置
 * @param[in]  reverse   逆引きフラグ
 * @param[out] result    検索結果
 *
 * @return なし
 */
static void set_operation_id(NJ_SEARCH_LOCATION *dicinfo, NJ_UINT8 reverse, NJ_RESULT *result) {
    NJ_UINT16 dictype;
    NJ_UINT32 type;
    NJ_UINT32 type2;

    if (dicinfo->handle == NULL) {
        /* ここに来ることはない */
        dictype = NJ_DIC_STATIC; /*NCH*/
        return; /*NCH*/
    }

    type = NJ_GET_DIC_TYPE_EX(NJ_GET_DIC_INFO(dicinfo), dicinfo->handle);

    /* 辞書タイプの振り分け     */
    switch (type) {
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書                     */
    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書                     */
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
    case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書)     */
    case NJ_DIC_TYPE_JIRITSU:                       /* 自立語辞書                   */
    case NJ_DIC_TYPE_FZK:                           /* 付属語辞書                   */
    case NJ_DIC_TYPE_TANKANJI:                      /* 単漢字辞書                   */
    case NJ_DIC_TYPE_STDFORE:                       /* 標準予測辞書                 */
    case NJ_DIC_TYPE_YOMINASHI:                     /* 読み無し予測辞書             */
    case NJ_DIC_TYPE_EXT_YOMINASI:                  /* 拡張読み無し予測辞書           */
    case NJ_DIC_TYPE_FORECONV:                      /* 予測変換辞書                 */
        dictype = NJ_DIC_STATIC;
        break;

    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:             /* 非圧縮カスタマイズ辞書       */
    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:       /* 非圧縮カスタマイズ辞書(学習辞書変更) */
    case NJ_DIC_TYPE_CUSTOM_COMPRESS:               /* 圧縮カスタマイズ辞書         */
        dictype = NJ_DIC_CUSTOMIZE;
        break;

    case NJ_DIC_TYPE_LEARN:                         /* 学習辞書                     */
        dictype = NJ_DIC_LEARN;
        break;

    case NJ_DIC_TYPE_USER:                          /* ユーザ辞書                   */
        dictype = NJ_DIC_USER;
        break;

    case NJ_DIC_TYPE_PROGRAM:                       /* 擬似辞書                     */
    case NJ_DIC_TYPE_PROGRAM_FZK:                   /* 擬似辞書-付属語-             */
        dictype = NJ_DIC_PROGRAM;
        if (result->word.stem.loc.handle != NULL) {
            /* 辞書タイプを取得 */
            type2 = NJ_GET_DIC_TYPE_EX(result->word.stem.loc.type, result->word.stem.loc.handle);

            /* 辞書タイプの振り分け     */
            switch (type2) {
            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ)     */
            case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書                             */
            case NJ_DIC_TYPE_FUSION:                        /* 統合辞書                             */
            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
            case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
            case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書)             */
            case NJ_DIC_TYPE_JIRITSU:                       /* 自立語辞書                           */
            case NJ_DIC_TYPE_FZK:                           /* 付属語辞書                           */
            case NJ_DIC_TYPE_TANKANJI:                      /* 単漢字辞書                           */
            case NJ_DIC_TYPE_STDFORE:                       /* 標準予測辞書                         */
            case NJ_DIC_TYPE_YOMINASHI:                     /* 読み無し予測辞書                     */
            case NJ_DIC_TYPE_EXT_YOMINASI:                  /* 拡張読み無し予測辞書                 */
            case NJ_DIC_TYPE_FORECONV:                      /* 予測変換辞書                         */
                dictype = NJ_DIC_STATIC;
                break;

            case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:             /* 非圧縮カスタマイズ辞書               */
            case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:       /* 非圧縮カスタマイズ辞書(学習辞書変更) */
            case NJ_DIC_TYPE_CUSTOM_COMPRESS:               /* 圧縮カスタマイズ辞書                 */
                dictype = NJ_DIC_CUSTOMIZE;
                break;

            case NJ_DIC_TYPE_LEARN:                         /* 学習辞書                             */
                dictype = NJ_DIC_LEARN;
                break;

            case NJ_DIC_TYPE_USER:                          /* ユーザ辞書                           */
                dictype = NJ_DIC_USER; /*NCH*/
                break; /*NCH*/

            case NJ_DIC_TYPE_PROGRAM:                       /* 擬似辞書                             */
            case NJ_DIC_TYPE_PROGRAM_FZK:                   /* 擬似辞書-付属語-                     */
            default:
                dictype = NJ_DIC_PROGRAM;
                break;
            }
        }
        break;

    default:
        /* ここに来ることはない */
        dictype = NJ_DIC_STATIC; /*NCH*/
    }
    if (reverse == 1) {
        /* 逆引き検索   */
        result->operation_id =
            (NJ_UINT16)((NJ_UINT16)NJ_OP_SEARCH | (NJ_UINT16)NJ_FUNC_SEARCH_R | dictype);
    } else {
        /* 通常の検索   */
        result->operation_id =
            (NJ_UINT16)((NJ_UINT16)NJ_OP_SEARCH | (NJ_UINT16)NJ_FUNC_SEARCH | dictype);
    }
}


/************************************************/
/* Global関数                                   */
/************************************************/
/**
 * 指定された検索条件から、検索カーソルの作成をする
 *
 * @param[in]     iwnn      解析情報クラス
 * @param[in,out] cursor    検索カーソル
 * @param[in]     comp_flg  学習辞書の完全一致で複数キュー対象の有無
 *                           （1:対象、1以外:対象としない）
 * @param[out]    exit_flag 前方一致候補有無 
 *                           （0:有り、1:無し）
 *
 * @retval 0  検索候補なし
 * @retval 1  検索候補あり
 * @retval <0 エラー
 */
NJ_INT16 njd_search_word(NJ_CLASS *iwnn, NJ_CURSOR *cursor, NJ_UINT8 comp_flg,
                         NJ_UINT8 *exit_flag) {
    NJ_INT16 ret;


    /*
     * 検索カーソルチェック & 検索位置情報初期化
     */
    ret = check_search_cursor(iwnn, cursor);
    if (ret != 0) {
        return ret;
    }

    return search_word(iwnn, cursor, comp_flg, exit_flag);
}


/**
 * 検索カーソルから、次候補情報を取得し、文字・候補フィルタリングを行う
 *
 * @param[in]     iwnn      解析情報クラス
 * @param[in,out] cursor    検索カーソル
 * @param[out]    result    検索結果
 * @param[in]     comp_flg  学習辞書、統合辞書の完全一致で複数キュー対象の有無
 *                           （1:対象、1以外:対象としない）
 *
 * @retval 0  次候補なし
 * @retval 1  次候補あり
 * @retval <0 異常終了
 */
NJ_INT16 njd_get_word(NJ_CLASS *iwnn, NJ_CURSOR *cursor, NJ_RESULT *result,
                      NJ_UINT8 comp_flg) {

    NJ_INT16    ret = -1;
    NJ_CHAR     tmp_stroke[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_CHAR     tmp_string[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_CHAR     tmp_additional[NJ_MAX_ADDITIONAL_LEN + NJ_TERM_LEN];
    NJ_UINT16   yclen;

    NJ_PHASE1_FILTER_IF phase1_filter_if;
    NJ_PHASE1_FILTER_MESSAGE ph1_filter_message;
    NJ_RESULT   tmp_result_now;
    NJ_RESULT   tmp_result_next;
    NJ_UINT8    relation_cnt;
    NJ_UINT8    cnt;
    NJ_DIC_FREQ dic_freq;
    NJ_UINT32   loop_count = NJD_GET_WORD_LOOP_LIMIT;
    NJ_UINT32   dic_type;


    /* 次候補が取得できる限りループを回し、フィルタ処理を行う */
    while ((ret = get_word_and_search_next_word(iwnn, cursor, result, comp_flg)) > 0) {
        if ((--loop_count) == 0) {
            /* 無限ループ防止カウンタが0になれば、強制的に次候補無し(ret = 0)で終了*/
            ret = 0; /*NCH*/
            break; /*NCH*/
        }

        /* 読み長指定予測入力の処理 */
        if (((cursor->cond.ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) &&
            (iwnn->njc_mode == 0) &&
            ((iwnn->environment.type == NJ_ANALYZE_NEXT_YOSOKU) ||
             (iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH) ||
             (iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI))) {
            /* 予測モードで前方一致検索実施時のみ、読み長制定予測入力をする */
            if ((cursor->cond.operation != NJ_CUR_OP_COMP_EXT) &&
                (cursor->cond.operation != NJ_CUR_OP_FORE_EXT)) {
                /* 読み文字列を取得 */
                ph1_filter_message.stroke_len = njd_get_stroke(iwnn, result, tmp_stroke, sizeof(tmp_stroke));
                if (ph1_filter_message.stroke_len <= 0) {
                    /* エラーの場合は、エラー値を返す */
                    return ph1_filter_message.stroke_len;
                }
            } else {
                /* 候補文字列を取得 */
                ph1_filter_message.stroke_len = njd_get_candidate(iwnn, result, tmp_stroke, sizeof(tmp_stroke));
                if (ph1_filter_message.stroke_len <= 0) {
                    /* エラーの場合は、エラー値を返す */
                    return ph1_filter_message.stroke_len;
                }
            }
            yclen = nj_charlen(tmp_stroke);
            if ((yclen < NJ_GET_FILTER_CHAR_MIN(iwnn)) ||
                (yclen > NJ_GET_FILTER_CHAR_MAX(iwnn)) ) {
                /* 文字数が制限内にない場合は候補を破棄する */
                continue;
            }
        } else {
            /* 読み長指定予測入力処理を行わない */
            ph1_filter_message.stroke_len = 0;
        }

        if (iwnn->option_data.phase1_filter == NULL) {
            /* フィルタリング関数が設定されていない場合、フィルタ処理終了。 */
            break;
        }

        /* フィルタメッセージの作成 */
        if (ph1_filter_message.stroke_len == 0) {
            /* 読み長指定予測入力処理で読みを取得していない場合、読みを取得する */
            ph1_filter_message.stroke_len = njd_get_stroke(iwnn, result, tmp_stroke, sizeof(tmp_stroke));
            if (ph1_filter_message.stroke_len <= 0) {
                /* エラーの場合は、エラー値を返す */
                return ph1_filter_message.stroke_len;
            }
        }
        ph1_filter_message.string_len = njd_get_candidate(iwnn, result, tmp_string, sizeof(tmp_string));
        if (ph1_filter_message.string_len <= 0) {
            /* エラーの場合は、エラー値を返す */
            return ph1_filter_message.string_len;
        }

        /* 付加情報文字列データの取得 */
        dic_type = NJ_GET_DIC_TYPE_EX(result->word.stem.loc.type, result->word.stem.loc.handle);
        if (HAS_ADDITIONAL_INFO((result->word), 0, dic_type)) {
            ph1_filter_message.additional_len = njd_get_additional_info(iwnn, result, (-1), tmp_additional, sizeof(tmp_additional));
            if (ph1_filter_message.additional_len < 0) {
                /* エラーの場合は、エラー値を返す */
                return (NJ_INT16)ph1_filter_message.additional_len; /*NCH_DEF*/
            }
        } else {
            ph1_filter_message.additional_len = 0;
            *tmp_additional = NJ_CHAR_NUL;
        }

        ph1_filter_message.stroke = &tmp_stroke[0];
        ph1_filter_message.string = &tmp_string[0];
        ph1_filter_message.additional = &tmp_additional[0];
        ph1_filter_message.result = result;
        ph1_filter_message.condition = &cursor->cond;
        ph1_filter_message.option = iwnn->option_data.phase1_option;

        /* 取得した候補に対してフィルタリングを実施する */
        phase1_filter_if = (NJ_PHASE1_FILTER_IF)(iwnn->option_data.phase1_filter);
        if ((*phase1_filter_if)(iwnn, &ph1_filter_message) == 0) {
            /* フィルタから0が返った場合は、候補を無視する */
            continue;
        }

        if (IS_COMPOUND_WORD(&(result->word))) {
            break;
        }

        if((GET_LOCATION_OPERATION(result->word.stem.loc.status) == NJ_CUR_OP_COMP) ||
           (GET_LOCATION_OPERATION(result->word.stem.loc.status) == NJ_CUR_OP_FORE)) {
            if ((relation_cnt = (NJ_UINT8)(result->word.stem.loc.current_info >> 4) & 0x0F) > 1) {
                /* ダミーの辞書頻度を設定しておく */
                dic_freq.high = 0;
                dic_freq.base = 0;
                tmp_result_now = *result;
                for (cnt = 1; cnt < relation_cnt; cnt++) {
                    ret = njd_get_relational_word(iwnn, &tmp_result_now, &tmp_result_next, &dic_freq);
                    if (ret < 0) {
                        return ret; /*NCH_FB*/
                    }
                    tmp_result_next.word.yomi = tmp_result_now.word.yomi + NJ_GET_YLEN_FROM_STEM(&tmp_result_now.word);
                    /* 連続文節をフィルタリングする */
                    ph1_filter_message.stroke_len =
                        njd_get_stroke(iwnn, &tmp_result_next, tmp_stroke, sizeof(tmp_stroke));
                    if (ph1_filter_message.stroke_len <= 0) {
                        return ph1_filter_message.stroke_len; /*NCH*/
                    }
                    ph1_filter_message.string_len =
                        njd_get_candidate(iwnn, &tmp_result_next, tmp_string, sizeof(tmp_string));
                    if (ph1_filter_message.string_len <= 0) {
                        return ph1_filter_message.string_len; /*NCH*/
                    }

                    /* 付加情報文字列データの取得 */
                    dic_type = NJ_GET_DIC_TYPE_EX(tmp_result_next.word.stem.loc.type, tmp_result_next.word.stem.loc.handle);
                    if (HAS_ADDITIONAL_INFO((tmp_result_next.word), 0, dic_type)) {
                        ph1_filter_message.additional_len = njd_get_additional_info(iwnn, &tmp_result_next, (-1), tmp_additional, sizeof(tmp_additional));
                        if (ph1_filter_message.additional_len < 0) {
                            /* エラーの場合は、エラー値を返す */
                            return (NJ_INT16)ph1_filter_message.additional_len; /*NCH_DEF*/
                        }
                    } else {
                        ph1_filter_message.additional_len = 0; /*NCH_DEF*/
                        *tmp_additional = NJ_CHAR_NUL; /*NCH_DEF*/
                    }

                    ph1_filter_message.stroke = &tmp_stroke[0];
                    ph1_filter_message.string = &tmp_string[0];
                    ph1_filter_message.additional = &tmp_additional[0];
                    ph1_filter_message.result = &tmp_result_next;
                    ph1_filter_message.condition = &cursor->cond;
                    ph1_filter_message.option = iwnn->option_data.phase1_option;

                    phase1_filter_if = (NJ_PHASE1_FILTER_IF)(iwnn->option_data.phase1_filter);
                    if ((*phase1_filter_if)(iwnn, &ph1_filter_message) == 0) {
                        result->word.stem.loc.current_info = (cnt << 4) & 0xF0;
                        break;
                    }
                    tmp_result_now = tmp_result_next;
                }
            }
        }
        if (ret) {
            /* 
             * フィルタ関数にて候補有効となった場合に、
             * 本ループ処理から抜ける
             */
             break;
        }
    }

    return ret;
}


/**
 * 単語検索njd_search_wordで取得した検索結果より、関連候補情報を取得する
 *
 * 検索カーソルは移動せず、現位置の関連候補を返す。
 *
 * @param[in]  iwnn     解析情報クラス
 * @param[in]  current  検索開始元単語情報
 * @param[out] next     検索した関連候補。領域はコール元で用意する必要がある(１個分)
 * @param[in]  dic_freq 辞書頻度情報用
 *
 * @retval 1   正常
 * @retval <0  エラー
 */
NJ_INT16 njd_get_relational_word(NJ_CLASS *iwnn, NJ_RESULT *current, NJ_RESULT *next, NJ_DIC_FREQ *dic_freq) {
    NJ_INT16 ret = 0;
    NJ_UINT8 num;
    NJ_UINT8 offset;
    NJ_UINT32 dictype;


    if (current->word.stem.loc.handle == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_RELATIONAL_WORD, NJ_ERR_INVALID_RESULT); /*NCH*/
    }

    /* 候補が持つ関連候補数よりも大きい取得位置を指定した場合 */
    num = (current->word.stem.loc.current_info >> 4) & 0x0f;
    offset = current->word.stem.loc.current_info & 0x0f;
    offset++;
    if (num <= offset) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_RELATIONAL_WORD, NJ_ERR_INVALID_RESULT); /*NCH*/
    }
    next->word.yomi = NULL;
    next->word.stem.info1 = 0;
    next->word.stem.info2 = 0;
    next->word.stem.hindo = 0;
    next->word.stem.type = 0;
    next->word.fzk.info1 = 0;
    next->word.fzk.info2 = 0;
    next->word.fzk.hindo = 0;
    next->word.stem.loc = current->word.stem.loc;
    next->word.stem.loc.current_info = (num << 4) | offset;

    /* 擬似辞書、通常辞書の切り替え */
    dictype = NJ_GET_DIC_TYPE_EX(next->word.stem.loc.type, current->word.stem.loc.handle);

    switch (dictype) {
    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書 */
    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書 */
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
        /* 統合辞書から関連候補情報を検索する */
        ret = njd_t_get_relational_word(&next->word.stem.loc, &next->word, dic_freq);
        break;
    case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
    case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
        /* 統合辞書から関連候補情報を検索する */
        ret = njd_s_get_relational_word(&next->word.stem.loc, &next->word, dic_freq);
        break;
    default:
        /* 統合辞書以外は学習辞書の場合しか処理が通らないので、
         * デフォルトを学習辞書にしておく */
        /* 学習辞書から関連候補情報を検索する   */
        ret = njd_l_get_relational_word(iwnn, &next->word.stem.loc, &next->word, dic_freq);
        break;
    }
    if (ret != 1) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_RELATIONAL_WORD, NJ_ERR_INVALID_RESULT); /*NCH*/
    }
    set_operation_id(&(next->word.stem.loc), 0, next);
    return 1;
}


/**
 * 読み文字列取得
 *
 * @param[in]  iwnn      解析情報クラス
 * @param[in]  result    処理結果
 * @param[out] stroke    読み文字列（領域は呼び元で用意する必要がある）
 * @param[in]  size      strokeのバイトサイズ
 *
 * @retval >=0 取得した読み文字列の文字配列長
 * @retval <0  エラー
 */
NJ_INT16 njd_get_stroke(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_CHAR *stroke, NJ_UINT16 size) {
    NJ_INT16 ret = 0;
    NJ_UINT16 len;
    NJ_UINT32 dictype;
    NJ_PROGRAM_DIC_IF program_dic_operation;
    NJ_PROGRAM_DIC_MESSAGE prog_msg;


    if (result->word.stem.loc.handle == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_STROKE, NJ_ERR_INVALID_RESULT);
    }

    /* 擬似辞書、通常辞書の切り替え */
    dictype = NJ_GET_DIC_TYPE_EX(result->word.stem.loc.type, result->word.stem.loc.handle);

    switch (dictype) {
    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書               */
    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書               */
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
        ret = njd_t_get_stroke(&result->word, stroke, size);
        break;

    case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
    case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
        ret = njd_s_get_stroke(&result->word, stroke, size);
        break;

    case NJ_DIC_TYPE_JIRITSU:                       /* 自立語辞書             */
    case NJ_DIC_TYPE_FZK:                           /* 付属語辞書             */
    case NJ_DIC_TYPE_TANKANJI:                      /* 単漢字辞書             */
    case NJ_DIC_TYPE_STDFORE:                       /* 標準予測辞書           */
    case NJ_DIC_TYPE_CUSTOM_COMPRESS:               /* 圧縮カスタマイズ辞書   */
    case NJ_DIC_TYPE_FORECONV:                      /* 予測変換辞書           */
        if (GET_LOCATION_OPERATION(result->word.stem.loc.status) != NJ_CUR_OP_COMP) {
            ret = njd_b_get_stroke(&result->word, stroke, size);
        } else {
            len = NJ_GET_YLEN_FROM_STEM(&result->word);
            if (size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_STROKE,
                                      NJ_ERR_BUFFER_NOT_ENOUGH);
            }
            if (len == 0) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_STROKE, /*NCH*/
                                      NJ_ERR_INVALID_RESULT);
            }
            nj_strncpy(stroke, result->word.yomi, len);
            *(stroke + len) = NJ_CHAR_NUL;
            return len;
        }
        break;

    case NJ_DIC_TYPE_LEARN:                         /* 学習辞書               */
    case NJ_DIC_TYPE_USER:                          /* ユーザ辞書             */
    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:             /* 非圧縮カスタマイズ辞書 */
    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:       /* 非圧縮カスタマイズ辞書(学習辞書変更) */
        ret = njd_l_get_stroke(iwnn, &result->word, stroke, size);
        break;

    case NJ_DIC_TYPE_YOMINASHI:                     /* 読み無し予測辞書       */
        ret = njd_f_get_stroke(&result->word, stroke, size);
        break;

    case NJ_DIC_TYPE_EXT_YOMINASI:                  /* 拡張読み無し予測辞書     */
        ret = njd_p_get_stroke(&result->word, stroke, size);
        break;

    case NJ_DIC_TYPE_PROGRAM:                       /* 擬似辞書       */
    case NJ_DIC_TYPE_PROGRAM_FZK:                   /* 擬似辞書-付属語-*/
        /* 擬似辞書呼出 */
        /* 関数ポインタの設定 */
        program_dic_operation = (NJ_PROGRAM_DIC_IF)(result->word.stem.loc.handle);
        /* 擬似辞書メッセージを設定 */
        njd_init_program_dic_message(&prog_msg);
        prog_msg.word = &result->word;
        prog_msg.stroke = stroke;
        prog_msg.stroke_size = size;
        ret = (*program_dic_operation)(iwnn, NJG_OP_GET_STROKE, &prog_msg);
        if (ret < 0) {
            return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, GET_ERR_FUNCVAL(ret));
        }
        break;

    default:
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_STROKE, NJ_ERR_DIC_TYPE_INVALID); /*NCH*/
    }
    /* 取得した読み長が０のとき、エラーで返す   */
    if (ret == 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_STROKE, NJ_ERR_INVALID_RESULT); /*NCH*/
    }
    return ret;
}


/**
 * 候補文字列取得
 *
 * @param[in]   iwnn      解析情報クラス
 * @param[in]   result    処理結果
 * @param[out]  candidate 候補文字列（領域は呼び元で用意する必要がある）
 * @param[in]   size      candidateのバイトサイズ
 *
 * @retval >0 取得した候補文字配列長
 * @retval <0 エラー
 */
NJ_INT16 njd_get_candidate(NJ_CLASS *iwnn, NJ_RESULT *result,
                           NJ_CHAR *candidate, NJ_UINT16 size) {
    NJ_INT16 ret = 0;
    NJ_UINT16 len;
    NJ_UINT32 dictype;
    NJ_PROGRAM_DIC_IF program_dic_operation;
    NJ_PROGRAM_DIC_MESSAGE prog_msg;


    if (result->word.stem.loc.handle == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_CANDIDATE, NJ_ERR_INVALID_RESULT); /*NCH*/
    }

    /* 擬似辞書、通常辞書の切り替え */
    dictype = NJ_GET_DIC_TYPE_EX(result->word.stem.loc.type, result->word.stem.loc.handle);

    switch (dictype) {
    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書               */
    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書               */
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
        if (GET_LOCATION_OPERATION(result->word.stem.loc.status) != NJ_CUR_OP_REV) {
            ret = njd_t_get_candidate(&result->word, candidate, size);
        } else {
            len = NJ_GET_YLEN_FROM_STEM(&result->word);
            if (size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_CANDIDATE,
                                      NJ_ERR_BUFFER_NOT_ENOUGH);
            }
            nj_strncpy(candidate, result->word.yomi, len);
            *(candidate + len) = NJ_CHAR_NUL;
            return len;
        }
        break;

    case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
    case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
        if (GET_LOCATION_OPERATION(result->word.stem.loc.status) != NJ_CUR_OP_REV) {
            ret = njd_s_get_candidate(&result->word, candidate, size);
        } else {
            len = NJ_GET_YLEN_FROM_STEM(&result->word);
            if (size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_CANDIDATE,
                                      NJ_ERR_BUFFER_NOT_ENOUGH);
            }
            nj_strncpy(candidate, result->word.yomi, len);
            *(candidate + len) = NJ_CHAR_NUL;
            return len;
        }
        break;

    case NJ_DIC_TYPE_JIRITSU:                   /* 自立語辞書             */
    case NJ_DIC_TYPE_FZK:                       /* 付属語辞書             */
    case NJ_DIC_TYPE_TANKANJI:                  /* 単漢字辞書             */
    case NJ_DIC_TYPE_STDFORE:                   /* 標準予測辞書           */
    case NJ_DIC_TYPE_CUSTOM_COMPRESS:           /* 圧縮カスタマイズ辞書   */
    case NJ_DIC_TYPE_FORECONV:                  /* 予測変換辞書           */
        if (GET_LOCATION_OPERATION(result->word.stem.loc.status) != NJ_CUR_OP_REV) {
            ret = njd_b_get_candidate(&result->word, candidate, size);
        } else {
            len = NJ_GET_YLEN_FROM_STEM(&result->word);
            if (size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_CANDIDATE,
                                      NJ_ERR_BUFFER_NOT_ENOUGH);
            }
            nj_strncpy(candidate, result->word.yomi, len);
            *(candidate + len) = NJ_CHAR_NUL;
            return len;
        }

        break;
    case NJ_DIC_TYPE_LEARN:                     /* 学習辞書               */
    case NJ_DIC_TYPE_USER:                      /* ユーザ辞書             */
    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:         /* 非圧縮カスタマイズ辞書 */
    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:   /* 非圧縮カスタマイズ辞書(学習辞書変更) */
        ret = njd_l_get_candidate(iwnn, &result->word, candidate, size);
        break;

    case NJ_DIC_TYPE_YOMINASHI:                 /* 読み無し予測辞書       */
        ret = njd_f_get_candidate(&result->word, candidate, size);
        break;

    case NJ_DIC_TYPE_EXT_YOMINASI:              /* 拡張読み無し予測辞書     */
        ret = njd_p_get_candidate(&result->word, candidate, size);
        break;

    case NJ_DIC_TYPE_PROGRAM:                   /* 擬似辞書       */
    case NJ_DIC_TYPE_PROGRAM_FZK:               /* 擬似辞書-付属語-*/
        /* 擬似辞書呼出*/
        /* 関数ポインタの設定 */
        program_dic_operation = (NJ_PROGRAM_DIC_IF)(result->word.stem.loc.handle);
        /* 擬似辞書メッセージを設定 */
        njd_init_program_dic_message(&prog_msg);
        prog_msg.word = &result->word;
        prog_msg.string = candidate;
        prog_msg.string_size = size;
        ret = (*program_dic_operation)(iwnn, NJG_OP_GET_STRING, &prog_msg);
        if (ret < 0) {
            return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, GET_ERR_FUNCVAL(ret));
        }
        break;

    default:
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_CANDIDATE, NJ_ERR_DIC_TYPE_INVALID); /*NCH*/
    }
    /* 取得した候補長が０のとき、エラーで返す   */
    if (ret == 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_CANDIDATE, NJ_ERR_INVALID_RESULT); /*NCH*/
    }
    return ret;
}


/**
 * 単語削除API
 *
 * 指定された単語を、辞書から削除する。
 *
 * @param[in,out]  iwnn      解析情報クラス
 * @param[in]      result    処理結果
 *
 * @retval 0  正常終了
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njx_delete_word(NJ_CLASS *iwnn, NJ_RESULT *result) {
    NJ_DIC_HANDLE handle;
    NJ_UINT32     dictype;
    NJ_INT16      ret = 0;
    NJ_PROGRAM_DIC_IF      program_dic_operation;
    NJ_PROGRAM_DIC_MESSAGE prog_msg;

    NJ_UINT16     functype;
    NJ_RESULT    *del_result;
    NJ_RESULT    *del_result2;
    NJ_INT16      i, j;
    NJ_UINT16     seg_cnt;
    NJ_UINT8      delskip_flg[NJ_MAX_PHRASE];
    NJ_UINT16     que_id, que_id2;
    NJ_UINT8      current_info, current_info2;
    NJ_DIC_HANDLE handle2;
    NJ_UINT32     dictype2;


    /* パラメータチェック       */
    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_DELETE_WORD, NJ_ERR_PARAM_ENV_NULL);
    }
    if (result == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_DELETE_WORD, NJ_ERR_PARAM_RESULT_NULL);
    }



    /* 連文節変換結果削除確認用バッファの初期化 */
    for (i = 0; i < NJ_MAX_PHRASE; i++) {
        delskip_flg[i] = NJ_FLAG_OFF;
    }

    /* 変換種別の取得 */
    functype = NJ_GET_RESULT_FUNC(result->operation_id);

    if (NJ_GET_RESULT_OP(result->operation_id) == NJ_OP_ANALYZE) {
        /* 削除文節数設定 */
        switch (functype) {
        case NJ_FUNC_CONVERT_MULTIPLE:    /* 連文節変換 */
            seg_cnt = iwnn->environment.conv_buf.multiple_keep_len;
            break;
        default:
            seg_cnt = NJ_NUM_ONE;
            break;
        }
    } else {
        seg_cnt = NJ_NUM_ONE;
    }

    /* 文節数分削除処理を実行 */
    for (i = 0; i < seg_cnt; i++) {
        if (NJ_GET_RESULT_OP(result->operation_id) == NJ_OP_ANALYZE) {
            /* 削除する処理結果を取得 */
            switch (functype) {
            case NJ_FUNC_CONVERT_MULTIPLE:    /* 連文節変換 */
                del_result = &iwnn->environment.conv_buf.multiple_keep[i];
                /* 辞書ハンドルを取得 */
                handle = del_result->word.stem.loc.handle;
                if (handle == NULL) {
                    continue; /*NCH*/
                }
                /* 辞書タイプを取得 */
                dictype = NJ_GET_DIC_TYPE_EX(del_result->word.stem.loc.type, handle);
                /* 学習辞書の場合のみ確認 */
                if ((dictype == NJ_DIC_TYPE_LEARN) && (!delskip_flg[i])) {
                    /* キューIDと文節情報を取得 */
                    que_id = (NJ_UINT16)(del_result->word.stem.loc.current >> NJ_NUM_BIT16);
                    current_info = del_result->word.stem.loc.current_info >> NJ_NUM_BIT4;
                    /* 削除対象文節以降の文節を確認 */
                    for (j = i + 1; j < seg_cnt; j++) {
                        /* 文節の処理結果を取得 */
                        del_result2 = &iwnn->environment.conv_buf.multiple_keep[j];
                        /* 辞書ハンドルを取得 */
                        handle2 = del_result2->word.stem.loc.handle;
                        if (handle2 == NULL) {
                            continue; /*NCH*/
                        }
                        /* 辞書タイプを取得 */
                        dictype2 = NJ_GET_DIC_TYPE_EX(del_result2->word.stem.loc.type, handle2);
                        /* 学習辞書の場合のみ確認 */
                        if (dictype2 == NJ_DIC_TYPE_LEARN) {
                            /* キューIDと文節情報を取得 */
                            que_id2 = (NJ_UINT16)(del_result2->word.stem.loc.current >> NJ_NUM_BIT16);
                            current_info2 = del_result2->word.stem.loc.current_info >> NJ_NUM_BIT4;
                            /* キューIDが一致した場合 */
                            if (que_id == que_id2) {
                                /* 文節数を確認 */
                                switch (current_info) {
                                case NJ_NUM_SEGMENT1:    /* 1文節 */
                                    /**
                                     * 比較元の文節が1文節の場合、
                                     * 後続文節は文節数に関わらず、SKIPフラグをONにする。
                                     * (比較元の候補を削除することで、対象キューの候補が削除される為。)
                                     */
                                    delskip_flg[j] = NJ_FLAG_ON;
                                    break;
                                case NJ_NUM_SEGMENT2:    /* 2文節 */
                                default:
                                    if (current_info2 == NJ_NUM_SEGMENT1) {
                                        /* 1文節の場合 */
                                        delskip_flg[i] = NJ_FLAG_ON;
                                    } else {
                                        /* 2文節の場合 */
                                        delskip_flg[j] = NJ_FLAG_ON;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            case NJ_FUNC_CONVERT_SINGLE:      /* 単文節変換 */
                del_result = &iwnn->environment.conv_buf.single_keep[i];
                break;
            default:
                del_result = result;
                break;
            }
        } else {
            del_result = result;
        }

        /* 辞書ハンドルもチェックする       */
        handle = del_result->word.stem.loc.handle;
        if (handle == NULL) {
            if (del_result->word.stem.type == 0) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_DELETE_WORD, NJ_ERR_DIC_HANDLE_NULL); /*NCH_FB*/
            } else {
                continue;
            }
        }

        /* 擬似辞書、通常辞書の判定 */
        dictype = NJ_GET_DIC_TYPE_EX(del_result->word.stem.loc.type, handle);

        switch (dictype) {
        case NJ_DIC_TYPE_LEARN:                      /* 学習辞書                             */
        case NJ_DIC_TYPE_USER:                       /* ユーザ辞書                           */
            /* 辞書引きもしくは予測結果以外の result が指定された場合はエラー                */
            if (!((NJ_GET_RESULT_OP(del_result->operation_id) == NJ_OP_SEARCH) ||
                  (NJ_GET_RESULT_OP(del_result->operation_id) == NJ_OP_CONVERT) ||
                  (NJ_GET_RESULT_OP(del_result->operation_id) == NJ_OP_ANALYZE))) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_DELETE_WORD, NJ_ERR_DIC_TYPE_INVALID);
            }
            if (!delskip_flg[i]) {
                ret = njd_l_delete_word(iwnn, del_result);
            }
            break;

        case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書                         */
        case NJ_DIC_TYPE_FUSION:                        /* 統合辞書                         */
        case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ)           */
            /* 予測候補の削除する処理を作成*/
            /* 予測結果以外の result が指定された場合はエラー */
            if (NJ_GET_RESULT_OP(del_result->operation_id) != NJ_OP_ANALYZE) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_DELETE_WORD, NJ_ERR_DIC_TYPE_INVALID);
            }
            if (del_result->word.stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL) {
                ret = njd_t_delete_word(iwnn, del_result);
            } else {
                /* 連文節変換・単文節変換結果以外の場合 */
                if ((functype != NJ_FUNC_CONVERT_MULTIPLE) && (functype != NJ_FUNC_CONVERT_SINGLE)) {
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_DELETE_WORD, NJ_ERR_DIC_TYPE_INVALID);
                } else {
                    /* 連文節変換・単文節変換の場合は何もしない */
                }
            }
            break;

        case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
        case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書)             */
        case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
            /* 予測候補の削除する処理を作成*/
            /* 予測結果以外の result が指定された場合はエラー */
            if (NJ_GET_RESULT_OP(del_result->operation_id) != NJ_OP_ANALYZE) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_DELETE_WORD, NJ_ERR_DIC_TYPE_INVALID);
            }
            if (del_result->word.stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL) {
                ret = njd_s_delete_word(iwnn, del_result);
            } else {
                /* 連文節変換・単文節変換結果以外の場合 */
                if ((functype != NJ_FUNC_CONVERT_MULTIPLE) && (functype != NJ_FUNC_CONVERT_SINGLE)) {
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_DELETE_WORD, NJ_ERR_DIC_TYPE_INVALID);
                } else {
                    /* 連文節変換・単文節変換の場合は何もしない */
                }
            }
            break;

        case NJ_DIC_TYPE_PROGRAM:                    /* 擬似辞書                             */
        case NJ_DIC_TYPE_PROGRAM_FZK:                /* 擬似辞書-付属語-                     */
            /* 擬似辞書呼出 */
            /* 関数ポインタの設定 */
            program_dic_operation = (NJ_PROGRAM_DIC_IF)(del_result->word.stem.loc.handle);

            /* 擬似辞書メッセージを設定 */
            njd_init_program_dic_message(&prog_msg);
            prog_msg.word = &del_result->word;
            ret = (*program_dic_operation)(iwnn, NJG_OP_DEL_WORD, &prog_msg);
            if (ret < 0) {
                return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, GET_ERR_FUNCVAL(ret));
            }
            break;
        default:
            /* 連文節変換・単文節変換結果以外の場合 */
            if ((functype != NJ_FUNC_CONVERT_MULTIPLE) && (functype != NJ_FUNC_CONVERT_SINGLE)) {
                /* 辞書種別エラー       */
                ret =  NJ_SET_ERR_VAL(NJ_FUNC_NJ_DELETE_WORD, NJ_ERR_DIC_TYPE_INVALID);
            } else {
                /* 連文節変換・単文節変換の場合は何もしない */
            }
            break;
        }
        if (ret < 0) {
            return ret;
        }
    }

    return ret;
}


/**
 * 辞書領域生成API
 *
 * @param[in]  iwnn     解析情報クラス
 * @param[out] handle   辞書ハンドル
 * @param[in]  type     辞書タイプ
 *                        0:ユーザ辞書
 *                        1:学習領域(AWnn互換)
 *                        2:学習領域(iWnn)
 *                        3:ユーザ辞書(付加情報付)
 *                        4:学習領域(iWnn-付加情報付-)
 * @param[in]  size     領域サイズ（ユーザ辞書の場合は無視）
 *                   
 * @retval 0  正常終了
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njx_create_dic(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_INT8 type, NJ_UINT32 size) {
    NJ_UINT32   cnt;
    NJ_UINT32   data_size;
    NJ_UINT8    *p;


    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CREATE_DIC, NJ_ERR_PARAM_ENV_NULL);
    }
    if (handle == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CREATE_DIC, NJ_ERR_DIC_HANDLE_NULL);
    }


    /* 辞書種別毎に最小サイズをチェックし、格納可能な単語数を計算する */
    if (type == CREATE_DIC_TYPE_LEARN_AWNN) {
        if (size < MIN_SIZE_OF_LEARN_DIC_AWNN) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CREATE_DIC, NJ_ERR_AREASIZE_INVALID); /*NCH_DEF*/
        }
        cnt = GET_MAX_WORD_NUM_IN_LEARN_DIC_AWNN(size);
    } else if (type == CREATE_DIC_TYPE_LEARN) {
        if (size < MIN_SIZE_OF_LEARN_DIC_IWNN) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CREATE_DIC, NJ_ERR_AREASIZE_INVALID);
        }
        cnt = GET_MAX_WORD_NUM_IN_LEARN_DIC_IWNN(size);
    } else if (type == CREATE_DIC_TYPE_LEARN2) {
        if (size < MIN_SIZE_OF_LEARN_DIC_IWNN2) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CREATE_DIC, NJ_ERR_AREASIZE_INVALID);
        }
        cnt = GET_MAX_WORD_NUM_IN_LEARN_DIC_IWNN2(size);
    } else if (type == CREATE_DIC_TYPE_USER) {
        if (size < MIN_SIZE_OF_USER_DIC) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CREATE_DIC, NJ_ERR_AREASIZE_INVALID);
        }
        cnt = GET_MAX_WORD_NUM_IN_USER_DIC(size);
    } else if (type == CREATE_DIC_TYPE_USER2) {
        if (size < MIN_SIZE_OF_USER2_DIC) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CREATE_DIC, NJ_ERR_AREASIZE_INVALID); /*NCH_DEF*/
        }
        cnt = GET_MAX_WORD_NUM_IN_USER2_DIC(size);
    } else {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CREATE_DIC, NJ_ERR_CREATE_TYPE_INVALID);
    }

    /* キューの数が32kを超えたらエラー  */
    if (cnt > 0x7FFF) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CREATE_DIC, NJ_ERR_AREASIZE_INVALID);
    }
    data_size = size - NJ_LEARN_DIC_HEADER_SIZE;

    /**
     * 辞書データ作成開始
     * 辞書作成が完了するまでは、電断対応のため、識別子を壊しておく。
     */
    p = (NJ_UINT8 *)handle;

    /* --- 共通データ --- */
    NJ_INT32_WRITE(p, 0L);
    p += sizeof(NJ_INT32);

    /* 辞書バージョン */
    if (type == CREATE_DIC_TYPE_LEARN2) {
        /**
         * 学習辞書 iWnnタイプ-付加情報付-
         * は、Ver.4とする
         */
        NJ_INT32_WRITE(p, NJ_DIC_VERSION4);
    } else if ((type == CREATE_DIC_TYPE_LEARN) ||
               (type == CREATE_DIC_TYPE_USER2)) {
        /**
         * 学習辞書 iWnnタイプ & ユーザ辞書(付加情報付)
         * は、Ver.3とする
         */
        NJ_INT32_WRITE(p, NJ_DIC_VERSION3);
    } else {
        NJ_INT32_WRITE(p, NJ_DIC_VERSION2);
    }
    p += sizeof(NJ_INT32);

    /* 辞書タイプ */
    if ((type == CREATE_DIC_TYPE_LEARN_AWNN) ||
        (type == CREATE_DIC_TYPE_LEARN) ||
        (type == CREATE_DIC_TYPE_LEARN2)) {
        NJ_INT32_WRITE(p, NJ_DIC_TYPE_LEARN);
    } else {
        NJ_INT32_WRITE(p, NJ_DIC_TYPE_USER);
    }
    p += sizeof(NJ_INT32);

    /* 辞書データサイズ */
    NJ_INT32_WRITE(p, data_size);
    p += sizeof(NJ_INT32);

    /* 辞書内拡張情報サイズ */
    NJ_INT32_WRITE(p, NJ_DIC_UNCOMP_EXT_HEADER_SIZE);
    p += sizeof(NJ_INT32);

    /* 辞書内最大読み文字列バイト長、辞書内候補最大候補文字列バイト長 */
    if ((type == CREATE_DIC_TYPE_LEARN_AWNN) ||
        (type == CREATE_DIC_TYPE_LEARN) ||
        (type == CREATE_DIC_TYPE_LEARN2)) {
        NJ_INT32_WRITE(p, NJ_MAX_LEN*sizeof(NJ_CHAR));
        p += sizeof(NJ_INT32);
        NJ_INT32_WRITE(p, NJ_MAX_RESULT_LEN*sizeof(NJ_CHAR));
    } else {
        NJ_INT32_WRITE(p, NJ_MAX_USER_LEN*sizeof(NJ_CHAR));
        p += sizeof(NJ_INT32);
        NJ_INT32_WRITE(p, NJ_MAX_USER_KOUHO_LEN*sizeof(NJ_CHAR));
    }
    p += sizeof(NJ_INT32);

    /* --- 拡張情報 --- */
    /* リザーブ */
    NJ_INT32_WRITE(p, 0L);
    p += sizeof(NJ_INT32);

    /* データ領域オフセット */
    NJ_INT32_WRITE(p, GET_DATA_AREA_OFFSET(cnt));
    p += sizeof(NJ_INT32);

    /* 登録語数 */
    NJ_INT32_WRITE(p, 0L);
    p += sizeof(NJ_INT32);

    /* 最大単語登録数 */
    NJ_INT32_WRITE(p, cnt);
    p += sizeof(NJ_INT32);

    /* 語彙データキューサイズ */
    if ((type == CREATE_DIC_TYPE_LEARN_AWNN) ||
        (type == CREATE_DIC_TYPE_LEARN) ||
        (type == CREATE_DIC_TYPE_LEARN2)) {
        NJ_INT32_WRITE(p, LEARN_DIC_QUE_SIZE);
    } else {
        NJ_INT32_WRITE(p, NJ_USER_QUE_SIZE);
    }
    p += sizeof(NJ_INT32);

    /* 次キュー追加位置 */
    NJ_INT32_WRITE(p, 0L);
    p += sizeof(NJ_INT32);

    /* 電断用エリア */
    NJ_INT32_WRITE(p, 0L);
    p += sizeof(NJ_INT32);

    /* 付加情報領域のオフセットを 設定 */
    if (type == CREATE_DIC_TYPE_LEARN2) {
        NJ_INT32_WRITE(p, GET_LEARN_ADDITIONAL_DATA_AREA_OFFSET(cnt));
    } else if (type == CREATE_DIC_TYPE_USER2) {
        NJ_INT32_WRITE(p, GET_USER_ADDITIONAL_DATA_AREA_OFFSET(cnt));
    } else {
        NJ_INT32_WRITE(p, 0L);
    }
    p += sizeof(NJ_INT32);

    /* インデックス領域オフセット   */
    NJ_INT32_WRITE(p, NJ_LEARN_DIC_HEADER_SIZE);
    p += sizeof(NJ_INT32);

    /* 表記順文字列インデックス領域オフセット   */
    NJ_INT32_WRITE(p, GET_HYOKI_INDEX_OFFSET(cnt));
    p += sizeof(NJ_INT32);

    /* Ver.2 は空き、Ver.3およびVer.4は拡張単語情報領域 */
    if ((type == CREATE_DIC_TYPE_LEARN) ||
        (type == CREATE_DIC_TYPE_LEARN2)) {
        NJ_INT32_WRITE(p, GET_EXT_DATA_AREA_OFFSET(cnt));
    } else {
        NJ_INT32_WRITE(p, 0L);
    }
    p += sizeof(NJ_INT32);

    /* 末尾の識別子をセットする                                 */
    /* データ領域に余りがあっても良い、渡されたサイズ分確保する */
    NJ_INT32_WRITE(handle + size - sizeof(NJ_UINT32), NJ_DIC_IDENTIFIER);
    /* 読み順インデックスをクリアし、キューを未使用状態にする   */
    njd_l_init_area(handle);

    /* 複合語予測情報バッファクリア */
    njd_l_init_cmpdg_info(iwnn);

    /* 電断対応のため、処理終了時に識別子をセットする   */
    NJ_INT32_WRITE(handle, NJ_DIC_IDENTIFIER);  /* 識別子           */

    return 0;
}


/**
 * 指定された辞書がエンジンで利用可能な辞書かチェックする
 *
 * @param[in]  iwnn      解析情報クラス
 * @param[in]  dic_type  辞書ハンドルタイプ
 * @param[in]  handle    辞書ハンドル
 * @param[in]  restore   自動復旧フラグ<br>
 *                        0：異常があっても自動復旧しない<br>
 *                        それ以外：異常があれば自動復旧を試みる
 *
 * @retval 0  正常終了
 * @retval <0 異常終了
 */
static NJ_INT16 njd_check_dic(NJ_CLASS *iwnn, NJ_UINT8 dic_type, NJ_DIC_HANDLE handle, NJ_UINT8 restore) {
    NJ_UINT8 *addr;
    NJ_UINT32 datasize, extsize;
    NJ_UINT32 version, id;
    NJ_UINT32 type;
    NJ_UINT8  buf[4];


    if (dic_type == NJ_DIC_H_TYPE_ON_STORAGE) {
        /* ストレージ辞書の場合 */
        addr = GET_STORAGE_CACHE_HEAD(handle);
    } else if (dic_type == NJ_DIC_H_TYPE_NORMAL) {
        addr = handle;
    } else {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID); /*NCH*/
    }

    /* 識別子 */
    if (NJ_INT32_READ(addr) != NJ_DIC_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID);
    }
    addr += sizeof(NJ_UINT32);

    /* 辞書フォーマット形式 */
    version = NJ_INT32_READ(addr);
    if ((version != NJ_DIC_VERSION1) && (version != NJ_DIC_VERSION2) && 
        (version != NJ_DIC_VERSION2_1) && (version != NJ_DIC_VERSION3) &&
        (version != NJ_DIC_VERSION4)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID);
    }
    addr += sizeof(NJ_UINT32);

    /* 辞書種別 */
    type = NJ_INT32_READ(addr);
    addr += sizeof(NJ_UINT32);

    /* 辞書データサイズ */
    datasize = NJ_INT32_READ(addr);
    addr += sizeof(NJ_UINT32);

    /* 拡張情報サイズ */
    extsize = NJ_INT32_READ(addr);
    addr += sizeof(NJ_UINT32);

    /* 辞書格納候補 最大読み長 */
    if (NJ_INT32_READ(addr) > (NJ_MAX_LEN * sizeof(NJ_CHAR))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID);
    }
    addr += sizeof(NJ_UINT32);

    /* 辞書格納候補 最大候補長 */
    if (NJ_INT32_READ(addr) > (NJ_MAX_RESULT_LEN * sizeof(NJ_CHAR))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID);
    }

    /* 後方の識別子チェック     */
    if (dic_type == NJ_DIC_H_TYPE_ON_STORAGE) {
        /* ストレージ辞書の場合 */
        if (njd_offset_fread((NJ_STORAGE_DIC_INFO *)handle, (NJ_DIC_COMMON_HEADER_SIZE + extsize + datasize - sizeof(NJ_UINT32)), sizeof(buf), (NJ_UINT8*)buf) < 0) {
            /* ファイルからの読み込み不可の場合、ID異常とする */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID); /*NCH_FB*/
        }
        id = NJ_INT32_READ(buf);
        if (id != NJ_DIC_IDENTIFIER) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID);
        }

    } else {
        addr += (extsize + datasize);
        if (NJ_INT32_READ(addr) != NJ_DIC_IDENTIFIER) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID);
        }
    }

    /* 辞書タイプ */
    switch (type) {
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ)               */
    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書-AWnnタイプ-                 */
    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書                             */
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)-AWnnタイプ- */
    case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書)             */
    case NJ_DIC_TYPE_JIRITSU:                       /* 自立語辞書                           */
    case NJ_DIC_TYPE_TANKANJI:                      /* 単漢字辞書                           */
    case NJ_DIC_TYPE_CUSTOM_COMPRESS:               /* カスタム辞書                         */
    case NJ_DIC_TYPE_STDFORE:                       /* 標準予測辞書                         */
    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:             /* 非圧縮カスタマイズ辞書               */
    case NJ_DIC_TYPE_FORECONV:                      /* 予測変換辞書                         */
        /* Ver.2 のみ許可 */
        if (version != (NJ_UINT32)NJ_DIC_VERSION2) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID);
        }
        break;

    case NJ_DIC_TYPE_FZK:                       /* 付属語辞書                           */
        /* Ver.2 または Ver.3 のみ許可 */
        if (!((version == (NJ_UINT32)NJ_DIC_VERSION2) || (version == (NJ_UINT32)NJ_DIC_VERSION3))) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID);
        }
        break;

    case NJ_DIC_TYPE_RULE:                      /* ルール辞書                           */
        /* Ver.2.1 または Ver.3 のみ許可 */
        if (!((version == (NJ_UINT32)NJ_DIC_VERSION2_1) || (version == (NJ_UINT32)NJ_DIC_VERSION3))) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID);
        }
        break;

    case NJ_DIC_TYPE_YOMINASHI:                 /* 読み無し予測辞書                     */
        /* Ver.1のみ許可 */
        if (version != (NJ_UINT32)NJ_DIC_VERSION1) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID);
        }
        break;

    case NJ_DIC_TYPE_EXT_YOMINASI:              /* 拡張読み無し予測辞書                 */
        /* Ver.1 または Ver.2 のみ許可 */
        if (!((version == (NJ_UINT32)NJ_DIC_VERSION1) || (version == (NJ_UINT32)NJ_DIC_VERSION2))) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID);
        }
        break;

    case NJ_DIC_TYPE_USER:                      /* ユーザ辞書                           */
        /* Ver.2 または Ver.3のみ許可 */
        if (!((version == (NJ_UINT32)NJ_DIC_VERSION2) || (version == (NJ_UINT32)NJ_DIC_VERSION3))) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID);
        }
        return njd_l_check_dic(iwnn, handle, restore);

    case NJ_DIC_TYPE_LEARN:                     /* 学習辞書                             */
    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:   /* 非圧縮カスタマイズ辞書(学習辞書変更) */
        /* Ver.2 または Ver.3 または Ver.4のみ許可 */
        if (!((version == (NJ_UINT32)NJ_DIC_VERSION2) || (version == (NJ_UINT32)NJ_DIC_VERSION3) || (version == (NJ_UINT32)NJ_DIC_VERSION4))) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_FORMAT_INVALID);
        }
        if (type == NJ_DIC_TYPE_LEARN) {
            /* 学習辞書の場合のみ、辞書内のインデックス等のチェックを行う */
            return njd_l_check_dic(iwnn, handle, restore);
        }
        break;

    default:
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_CHECK_DIC, NJ_ERR_DIC_TYPE_INVALID);
    }
    return 0;
}


/**
 * 単語検索API
 *
 * 指定された検索条件から、検索カーソルの作成をする。
 *
 * @param[in]      iwnn      解析情報クラス
 * @param[in,out]  cursor    検索カーソル
 *
 * @retval 0  検索候補なし
 * @retval 1  検索候補あり
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njx_search_word(NJ_CLASS *iwnn, NJ_CURSOR *cursor) {

    NJ_UINT8 exit_flag;                         /* ダミー */
    NJ_UINT8 cnt;
    NJ_DIC_HANDLE dhdl;
    NJ_UINT8 charset_flg = 0;
    NJ_UINT32 dictype;


    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SEARCH_WORD, NJ_ERR_PARAM_ENV_NULL);
    }
    if (cursor == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SEARCH_WORD, NJ_ERR_PARAM_CURSOR_NULL);
    }

    
    /* 検索条件の内部使用情報をクリア */
    cursor->cond.hinsi.fore = NULL;
    cursor->cond.hinsi.foreSize = 0;
    cursor->cond.hinsi.foreFlag = 0;
    cursor->cond.hinsi.rear = NULL;
    cursor->cond.hinsi.rearSize = 0;
    cursor->cond.hinsi.rearFlag = 0;
    cursor->cond.hinsi.yominasi_fore = NULL;
    cursor->cond.hinsi.prev_bpos = 0;
    cursor->cond.fzkconnect = 0;
    cursor->cond.ctrl_opt = 0;
    cursor->cond.attr = 0x00000000;

    /* 読み文字列長セット       */
    if (cursor->cond.yomi == NULL) {
        cursor->cond.ylen = 0;
        cursor->cond.yclen = 0;
    } else {
        cursor->cond.ylen = nj_strlen(cursor->cond.yomi);
        cursor->cond.yclen = nj_charlen(cursor->cond.yomi);
    }
    /* 辞書セット構造体がセットされていない場合は、NGとする */
    if (cursor->cond.ds == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_CHECK_SEARCH_CURSOR, NJ_ERR_PARAM_DIC_NULL);
    }
    /* キャッシュ検索モード無効にする。*/
    cursor->cond.ds->mode = NJ_CACHE_MODE_NONE;
    for (cnt = 0; cnt < NJ_MAX_DIC; cnt++) {
        if ((cursor->cond.ds->dic[cnt].handle != NULL) &&
            (cursor->cond.ds->dic[cnt].srhCache != NULL)) {

            dictype = NJ_GET_DIC_TYPE_EX(cursor->cond.ds->dic[cnt].type,
                                         cursor->cond.ds->dic[cnt].handle);
            switch (dictype) {
            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:
            case NJ_DIC_TYPE_FUSION_AWNN:
            case NJ_DIC_TYPE_FUSION:
            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:
            case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:
            case NJ_DIC_TYPE_FUSION_STORAGE:
                /* キャッシュ検索モード有効にする。*/
                cursor->cond.ds->mode = NJ_CACHE_MODE_VALID_FUSION;
                /* キャッシュ領域を初期化 */
                cursor->cond.ds->dic[cnt].srhCache->statusFlg = 0;
                cursor->cond.ds->dic[cnt].srhCache->viewCnt = 0;
                cursor->cond.ds->dic[cnt].srhCache->keyPtr[0] = 0;
                cursor->cond.ds->dic[cnt].srhCache->keyPtr[1] = 0;
                if (cursor->cond.yomi == NULL) {
                    INIT_KEYWORD_IN_NJ_DIC_SET(cursor->cond.ds); /*NCH_DEF*/
                } else {
                    nj_strcpy(cursor->cond.ds->keyword, cursor->cond.yomi);
                }
                charset_flg = 1;
                break;
            default:
                break;
            }
        }
    }
    /* あいまい文字セットの初期化 */
    if (charset_flg == 0) {
        cursor->cond.charset = NULL;
    }

    cursor->cond.ctrl_opt |= NJ_SEARCH_DISMANTLING_CONTROL; /* 検索制限を解除する */

    for (cnt = 0; cnt < NJ_MAX_DIC; cnt++) {
        dhdl = cursor->cond.ds->dic[cnt].handle;
        /* 指定辞書がある場合のみチェック対象とする */
        if (dhdl != NULL) {
            if ((cursor->cond.ds->dic[cnt].dic_freq[NJ_MODE_TYPE_HENKAN].base
                 > cursor->cond.ds->dic[cnt].dic_freq[NJ_MODE_TYPE_HENKAN].high)) {
                    return NJ_SET_ERR_VAL(NJ_FUNC_CHECK_SEARCH_CURSOR, NJ_ERR_DIC_FREQ_INVALID);
                }
        }
    }

    return njd_search_word(iwnn, cursor, 0, &exit_flag);
}


/**
 * 単語取得API
 *
 * 検索カーソルから、候補情報を取得する。
 *
 * @param[in]     iwnn      解析情報クラス
 * @param[in,out] cursor    検索カーソル
 *                          (内部処理用データが更新される)
 * @param[out]    result    次候補情報
 *
 * @retval 0  候補なし
 * @retval >0 候補あり
 * @retval <0 異常終了
 */
NJ_EXTERN NJ_INT16 njx_get_word(NJ_CLASS *iwnn, NJ_CURSOR *cursor, NJ_RESULT *result) {
    NJ_PHASE2_FILTER_IF phase2_filter_if;
    NJ_PHASE2_FILTER_MESSAGE ph2_filter_message;
    NJ_INT16  ret;


    /* パラメータチェック       */
    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_WORD, NJ_ERR_PARAM_ENV_NULL);
    }
    if (cursor == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_WORD, NJ_ERR_PARAM_CURSOR_NULL);
    }
    if (result == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_WORD, NJ_ERR_PARAM_RESULT_NULL);
    }


    if ((phase2_filter_if = (NJ_PHASE2_FILTER_IF)iwnn->option_data.phase2_filter) == NULL) {
        /* 候補フィルタが設定されていない場合、njd_get_word() を１回実行して終了する */
        ret = njd_get_word(iwnn, cursor, result, 0);
    } else {
        /* 候補フィルタが設定されている場合、フィルタが許可するまで njd_get_word() を繰り返す */
        ph2_filter_message.option = iwnn->option_data.phase2_option;
        while ((ret = njd_get_word(iwnn, cursor, result, 0)) > 0) {
            ph2_filter_message.result = result;
            if ((*phase2_filter_if)(iwnn, &ph2_filter_message) != 0) {
                break;
            }
        }
    }

    return ret;
}


/**
 * 単語登録API
 *
 * 指定された単語を、辞書に登録する。
 *
 * @param[in,out] iwnn      解析情報クラス
 * @param[in]     word      単語情報
 * @param[in]     type      登録先の辞書種別<br>
 *                           0(ADD_WORD_DIC_TYPE_USER)  : ユーザ辞書<br>
 *                           1(ADD_WORD_DIC_TYPE_LEARN) : 学習辞書<br>
 *                           2(ADD_WORD_DIC_TYPE_PROGRAM) : 擬似辞書
 * @param[in]     connect   ひとつ前に確定した候補との関係情報を作成するかのフラグ。<br>
 *                           0: 関係情報を作成しない<br>
 *                           1: 関係情報を作成する
 *
 * @retval >=0 正常終了
 * @retval <0  エラー
 */
NJ_EXTERN NJ_INT16 njx_add_word(NJ_CLASS *iwnn, NJ_WORD_INFO *word, NJ_UINT8 type, NJ_UINT8 connect) {
    NJ_LEARN_WORD_INFO lword;                           /* 学習単語情報 */
    NJ_UINT16 ylen, klen, alen;
    NJ_INT16 ret;
    NJ_UINT8 f_type, b_type;
    NJ_DIC_SET *dics;
    NJ_UINT16  f_cnt, b_cnt;


    /* 辞書セットチェック       */
    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_PARAM_ENV_NULL);
    }
    dics = &(iwnn->dic_set);

    /* ルール辞書チェック       */
    if (dics->rHandle[NJ_MODE_TYPE_HENKAN] == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_NO_RULEDIC);
    }
    /* 登録単語情報チェック     */
    if (word == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_WORD_INFO_NULL);
    }
    /* 登録先の辞書種別         */
    if ((type != ADD_WORD_DIC_TYPE_USER) &&
        (type != ADD_WORD_DIC_TYPE_LEARN) &&
        (type != ADD_WORD_DIC_TYPE_PROGRAM)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_DIC_TYPE_INVALID);
    }
    /* 関係学習フラグのチェック */
    if (connect > 1) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_INVALID_FLAG);
    }

    ylen = nj_strlen(word->yomi);
    klen = nj_strlen(word->kouho);
    alen = nj_strlen(word->additional);
    if (type == ADD_WORD_DIC_TYPE_USER) {
        /* ユーザ辞書   */
        if ((ylen == 0) || (ylen > NJ_MAX_USER_LEN)) {
            /* 読み異常(長さ０またはMAXオーバー)        */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_USER_YOMI_INVALID);
        }
        if ((klen == 0) || (klen > NJ_MAX_USER_KOUHO_LEN)) {
            /* 候補異常(長さ０またはMAXオーバー)        */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_USER_KOUHO_INVALID);
        }
        if (/*(alen > 0) && */(alen > NJ_MAX_USER_ADDITIONAL_LEN)) {
            /* 付加情報異常(長さ０またはMAXオーバー)        */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_USER_ADDITIONAL_INVALID);
        }
    } else {
        /* 学習辞書     */
        /* 擬似辞書     */
        if ((ylen == 0) || (ylen > NJ_MAX_LEN)) {
            /* 読み異常(長さ０またはMAXオーバー)        */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_USER_YOMI_INVALID);
        }
        if ((klen == 0) || (klen > NJ_MAX_RESULT_LEN)) {
            /* 候補異常(長さ０またはMAXオーバー)        */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_USER_KOUHO_INVALID);
        }
        if (/*(alen > 0) && */(alen > NJ_MAX_ADDITIONAL_LEN)) {
            /* 付加情報異常(長さ０またはMAXオーバー)        */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_USER_ADDITIONAL_INVALID); /*NCH_DEF*/
        }
    }
    nj_strcpy(lword.yomi, word->yomi);
    lword.yomi_len = (NJ_UINT8)(ylen & 0xff);
    nj_strcpy(lword.hyouki, word->kouho);
    lword.hyouki_len = (NJ_UINT8)(klen & 0xff);
    nj_strcpy(lword.additional, word->additional);
    lword.additional_len = (NJ_UINT8)(alen & 0xff);

    if (word->hinsi_group == NJ_HINSI_DETAIL) {
        /* wordの内部情報チェック */
        if (((word->stem.yomi_len + word->fzk.yomi_len) != ylen)
            || (word->fzk.yomi_len != word->fzk.kouho_len)) {
            /* stem,fzkの読み長合計がylenと違う場合 or fzkの読み長!=表記長の場合 */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_USER_YOMI_INVALID); /*NCH_FB*/
        }
        if ((word->stem.kouho_len + word->fzk.kouho_len) != klen) {
            /* stem,fzkの候補長合計がylenと違う場合 */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_USER_KOUHO_INVALID); /*NCH_FB*/
        }
        if (nj_strcmp(&(word->yomi[word->stem.yomi_len]),
                      &(word->kouho[word->stem.kouho_len])) != 0) {
            /* 付属語部分が 読み!=表記 であればエラー */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_USER_YOMI_INVALID); /*NCH_FB*/
        }

        /* "詳細" 品詞グループの場合の品詞情報および付属語読み長設定処理 */
        lword.attr = word->stem.attr;

        if (word->stem.yomi_len == 0) {
            lword.f_hinsi      =                      NJ_GET_FZK_F_HINSI_FROM_NJ_WORD_INFO(word);
            lword.b_hinsi      = lword.stem_b_hinsi = NJ_GET_FZK_B_HINSI_FROM_NJ_WORD_INFO(word);
        } else {
            lword.f_hinsi      =                      NJ_GET_STEM_F_HINSI_FROM_NJ_WORD_INFO(word);
            lword.b_hinsi      = lword.stem_b_hinsi = NJ_GET_STEM_B_HINSI_FROM_NJ_WORD_INFO(word);
            if(word->fzk.yomi_len != 0) {
                lword.b_hinsi  =                      NJ_GET_FZK_B_HINSI_FROM_NJ_WORD_INFO(word);
            }
        }
        lword.fzk_yomi_len = (NJ_UINT8)word->fzk.yomi_len;

        /* 有効な品詞番号でない場合、エラーとする。*/
        njd_r_get_count(dics->rHandle[NJ_MODE_TYPE_HENKAN], &f_cnt, &b_cnt);
        if ((lword.f_hinsi == 0) ||
            (lword.b_hinsi == 0) ||
            (lword.stem_b_hinsi == 0) ||
            (f_cnt < lword.f_hinsi) ||
            (b_cnt < lword.b_hinsi) ||
            (b_cnt < lword.stem_b_hinsi)) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_HINSI_GROUP_INVALID);
        }
    } else {
        /* "詳細" 以外の品詞グループの場合の品詞情報および付属語読み長設定処理 */
        lword.attr = 0L;

        /* 品詞グループに対応した変換用品詞を取得し、自立語品詞として設定する。付属語は無しとする */
        switch (word->hinsi_group) {
        case NJ_HINSI_MEISI:
            /* 一般名詞／固有名詞   */
            f_type = NJ_HINSI_MEISI_F;
            b_type = NJ_HINSI_MEISI_B;
            break;
        case NJ_HINSI_JINMEI:
            /* 人名                 */
            f_type = NJ_HINSI_JINMEI_F;
            b_type = NJ_HINSI_JINMEI_B;
            break;
        case NJ_HINSI_CHIMEI:
            /* 地名／駅名           */
            f_type = NJ_HINSI_CHIMEI_F;
            b_type = NJ_HINSI_CHIMEI_B;
            break;
        case NJ_HINSI_KIGOU:
            /* 記号                 */
            f_type = NJ_HINSI_KIGOU_F;
            b_type = NJ_HINSI_KIGOU_B;
            break;
        default:
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_HINSI_GROUP_INVALID);
        }
        lword.f_hinsi      = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], f_type);
        lword.b_hinsi      = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], b_type);
        lword.stem_b_hinsi = lword.b_hinsi;

        lword.fzk_yomi_len = 0;
    }

    /* ルール辞書から品詞番号を取得できなかった、起こらないはず */
    if ((lword.f_hinsi == 0) ||  (lword.b_hinsi == 0)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ADD_WORD, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
    }

    if (type == ADD_WORD_DIC_TYPE_USER) {
        /* ユーザ辞書へ登録 */
        /*
         * ユーザ辞書は関係学習を行なわないため、
         * 関係学習フラグは常に"0"を指定
         */
        ret = njd_l_add_word(iwnn, &lword, 0, 0, 0, type);
        if (ret < 0) {
            return ret;
        }
        /*
         * ユーザ辞書登録時は学習辞書にも登録する、undo = 1
         * 学習辞書には,関係学習フラグ(connect)を渡す
         */
        ret = njd_l_add_word(iwnn, &lword, connect, 0, 1, ADD_WORD_DIC_TYPE_LEARN);
        if (ret < 0) {
            /* 学習辞書が存在しない場合はエラー無視             */
            if (NJ_GET_ERR_CODE(ret) == NJ_ERR_DIC_NOT_FOUND) {
                return 0;
            } else {
                return ret; /*NCH_FB*/
            }
        }
    } else if (type == ADD_WORD_DIC_TYPE_LEARN) {
        /*
         * 学習辞書へ登録、undo = 1
         * 学習辞書には,関係学習フラグ(connect)を渡す
         */
        ret = njd_l_add_word(iwnn, &lword, connect, 0, 1, type);
        if (ret < 0) {
            return ret;
        }
    } else {
        /* 擬似辞書へ登録、undo = 1
         * 擬似辞書には,関係学習フラグ(connect)を渡す */
        ret = njd_add_word(iwnn, &lword, connect, 1);
        if (ret < 0) {
            return ret;
        }
    }

    if (iwnn->option_data.ext_mode & NJ_ADD_WORD_OPTIMIZE_OFF) {
        /* 辞書最適化処理OFFの場合 */
        return 0;
    }
    /* 同読みの学習情報が制限数を超えていたら削除する   */
    return njd_l_make_space(iwnn, NJ_MAX_PHRASE, 0);
}


/**
 * 辞書チェックAPI
 *
 * 指定された辞書のデータ整合性をチェックする。
 *
 * @param[in]      iwnn      解析情報クラス
 * @param[in]      dic_type  辞書ハンドルタイプ
 * @param[in,out]  handle    辞書ハンドル
 * @param[in]      restore   自動復旧フラグ<br>
 *                            ０：自動復旧しない<br>
 *                            その他：自動復旧する（学習辞書、ユーザ辞書のみ有効）
 * @param[in]      size      辞書ハンドルで指定された領域のサイズ<br>
 *                           ストレージ辞書機能が有効な場合は、ストレージ辞書キャッシュ領域のサイズ
 *
 * @retval 0  正常終了(正常データ)
 * @retval <0 異常終了
 */
NJ_EXTERN NJ_INT16 njx_check_dic(NJ_CLASS *iwnn, NJ_UINT8 dic_type, NJ_DIC_HANDLE handle, NJ_UINT8 restore, NJ_UINT32 size) {
    NJ_UINT32 dictype;
    NJ_STORAGE_DIC_INFO *p_storage_dic_info;


    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_DIC, NJ_ERR_PARAM_ENV_NULL);
    }

    if (handle == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_DIC, NJ_ERR_DIC_HANDLE_NULL);
    }

    if (restore > 1) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_DIC, NJ_ERR_INVALID_FLAG);
    }

#ifdef NJ_OPT_DIC_STORAGE
    if ((dic_type != NJ_DIC_H_TYPE_NORMAL) &&
        (dic_type != NJ_DIC_H_TYPE_ON_STORAGE)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_DIC, NJ_ERR_PARAM_TYPE_INVALID);
    }
#else  /* NJ_OPT_DIC_STORAGE */
    if (dic_type != NJ_DIC_H_TYPE_NORMAL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_DIC, NJ_ERR_PARAM_TYPE_INVALID);
    }
#endif /* NJ_OPT_DIC_STORAGE */

    /* 少なくとも共通ヘッダー分ないと次のチェックで     */
    /* 範囲外アクセスの怖れがあるので先にチェック       */
    if (size <= NJ_DIC_COMMON_HEADER_SIZE) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_DIC, NJ_ERR_AREASIZE_INVALID);
    }

    /* 辞書全体のサイズチェック */
    /* 共通ヘッダーサイズ＋辞書データサイズ＋拡張情報サイズの合計と比較 */
    if (dic_type == NJ_DIC_H_TYPE_ON_STORAGE) {

        p_storage_dic_info = (NJ_STORAGE_DIC_INFO*)handle;
        dictype = p_storage_dic_info->dictype;

        if ((dictype != NJ_DIC_TYPE_FUSION_AWNN_STORAGE) &&
            (dictype != NJ_DIC_TYPE_FUSION_STORAGE) &&
            (dictype != NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE)) {
            /* 統合辞書(ストレージ辞書)タイプ以外が指定された場合、異常とする */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_DIC, NJ_ERR_PARAM_TYPE_INVALID);
        }

        /* キャッシュサイズ確認 */
        if (size != (NJ_UINT32)njd_s_get_storage_dic_cache_size(iwnn, (dictype & 0x000FFFFF),
                                                                p_storage_dic_info->filestream, p_storage_dic_info->mode)) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_DIC, NJ_ERR_AREASIZE_INVALID);
        }

        /* 辞書データ確認 */
        if (p_storage_dic_info->dicsize != (NJ_DIC_COMMON_HEADER_SIZE
                     + NJ_INT32_READ(GET_STORAGE_CACHE_HEAD(handle) + NJ_DIC_POS_DATA_SIZE)
                     + NJ_INT32_READ(GET_STORAGE_CACHE_HEAD(handle) + NJ_DIC_POS_EXT_SIZE))) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_DIC, NJ_ERR_AREASIZE_INVALID);
        }
    } else {
        if (size != (NJ_DIC_COMMON_HEADER_SIZE
                     + NJ_INT32_READ(handle + NJ_DIC_POS_DATA_SIZE)
                     + NJ_INT32_READ(handle + NJ_DIC_POS_EXT_SIZE))) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_DIC, NJ_ERR_AREASIZE_INVALID);
        }
    }

    
    return njd_check_dic(iwnn, dic_type, handle, restore);
}


/**
 * 辞書ハンドル取得API
 * 
 * 指定した処理結果の辞書ハンドルを返す
 *
 * @param[in]  iwnn      解析情報クラス
 * @param[in]  result    辞書ハンドルを取得する処理結果
 *
 * @retval NULL  resultがNULLもしくは、エンジン内部で生成された擬似候補の場合。
 * @retval それ以外 指定した処理結果の辞書ハンドル
 */
NJ_EXTERN NJ_DIC_HANDLE njx_get_dic_handle(NJ_CLASS *iwnn, NJ_RESULT *result) {


    if (iwnn == NULL) {
        return NULL;
    }
    if (result == NULL) {
        return NULL;
    }

    /* 評価部の処理結果で、連文節変換もしくは１文節変換のとき   */
    /* ハンドルはNULLとする                                     */
    if (NJ_GET_RESULT_OP(result->operation_id) == NJ_OP_ANALYZE) {
        if ((NJ_GET_RESULT_FUNC(result->operation_id) == NJ_FUNC_CONVERT_MULTIPLE)
            || (NJ_GET_RESULT_FUNC(result->operation_id) == NJ_FUNC_CONVERT_SINGLE)) {
            return NULL;
        }
    }
    /* 評価部もしくは変換部の処理結果で擬似のとき               */
    /* ハンドルはNULLとする                                     */
    if (NJ_GET_RESULT_DIC(result->operation_id) == NJ_DIC_GIJI) {
        return NULL;
    }
    return result->word.stem.loc.handle;
}


/**
 * 辞書タイプ変更API
 *
 * 指定した辞書ハンドルの辞書タイプを変更する。
 *
 * @param[in]     iwnn      解析情報クラス
 * @param[in,out] handle    辞書ハンドル
 * @param[in]     direct    変更辞書タイプ<br>
 *                            0:学習辞書→非圧縮カスタマイズ辞書<br>
 *                            1:非圧縮カスタマイズ辞書→学習辞書
 *
 * @retval 0   正常終了
 * @retval <0  エラー
 */
NJ_EXTERN NJ_INT16 njx_change_dic_type(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 direct) {

    NJ_UINT32 type;
    NJ_UINT8    *p;


    /* パラメータチェック */
    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHANGE_DIC_TYPE, NJ_ERR_PARAM_ENV_NULL);
    }


    if (handle == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHANGE_DIC_TYPE, NJ_ERR_DIC_HANDLE_NULL);
    }

    p = (NJ_UINT8 *)handle;

    type = NJ_GET_DIC_TYPE(p);
    if ((type != NJ_DIC_TYPE_LEARN) && (type != NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN)) {
        /* 学習辞書か非圧縮カスタマイズ辞書(学習辞書変更)以外の場合、エラーとする。 */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHANGE_DIC_TYPE, NJ_ERR_DIC_TYPE_INVALID);
    }

    if ((NJ_GET_DIC_VER(p) != NJ_DIC_VERSION2) && (NJ_GET_DIC_VER(p) != NJ_DIC_VERSION3) && (NJ_GET_DIC_VER(p) != NJ_DIC_VERSION4)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHANGE_DIC_TYPE, NJ_ERR_DIC_VERSION_INVALID);
    }

    if (direct > 1) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHANGE_DIC_TYPE, NJ_ERR_INVALID_FLAG);
    }

    /*
     * 辞書バイナリの辞書タイプ状態と、変更辞書タイプが正しく指定されているかを
     * チェックする。
     */
    if (((type == NJ_DIC_TYPE_LEARN) && (direct == 1)) ||
        ((type == NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN) && (direct == 0))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHANGE_DIC_TYPE, NJ_ERR_INVALID_FLAG);
    }

    /*
     * 辞書バイナリの辞書タイプを変更辞書タイプに変更する。
     */
    if (direct == 0) {
        /* 非圧縮カスタマイズ辞書(学習辞書変更)へ変更 */
        NJ_INT32_WRITE(p + POS_DIC_TYPE_SIZE, NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN);
    } else {
        /* 学習辞書へ変更 */
        NJ_INT32_WRITE(p + POS_DIC_TYPE_SIZE, NJ_DIC_TYPE_LEARN);
    }

    return 0;
}


/**
 * 頻度学習領域サイズ取得API
 *
 * 頻度学習領域サイズを取得する。
 *
 * @attention    本関数は擬似辞書を許容しない
 *
 * @param[in] iwnn      解析情報クラス
 * @param[in] dic_type  辞書ハンドルタイプ
 * @param[in] handle    辞書ハンドル
 * @param[out] size     頻度学習領域サイズ
 *
 * @retval 1 取得成功
 * @retval 0 未取得
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njx_get_ext_area_size(NJ_CLASS *iwnn, NJ_UINT8 dic_type, NJ_DIC_HANDLE handle, NJ_UINT32 *size) 
{
    NJ_UINT16 ret;
    NJ_UINT32 dictype;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_EXT_AREA_SIZE, NJ_ERR_PARAM_ENV_NULL);
    }

    if (handle == NULL) {
        /* 第3引数(handle)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_EXT_AREA_SIZE, NJ_ERR_PARAM_DIC_NULL);
    }

    if (size == NULL) {
        /* 第4引数(size)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_EXT_AREA_SIZE, NJ_ERR_PARAM_SIZE_NULL); /*NCH_MB*/
    }

#ifdef NJ_OPT_DIC_STORAGE
    if (dic_type == NJ_DIC_H_TYPE_NORMAL) {
        dictype = NJ_GET_DIC_TYPE(handle);
    } else if (dic_type == NJ_DIC_H_TYPE_ON_STORAGE) {
        dictype = ((NJ_STORAGE_DIC_INFO*)handle)->dictype;
    } else {
        /* 第2引数(dic_type)がNJ_DIC_H_TYPE_NORMAL, NJ_DIC_H_TYPE_ON_STORAGE以外の場合は常に 0 を返す。*/
        *size = 0;
        return 0;
    }
#else  /* NJ_OPT_DIC_STORAGE */
    if (dic_type == NJ_DIC_H_TYPE_NORMAL) {
        dictype = NJ_GET_DIC_TYPE(handle);
    } else {
        /* 第2引数(dic_type)がNJ_DIC_H_TYPE_NORMAL以外の場合は常に 0 を返す。*/
        *size = 0;
        return 0;
    }
#endif /* NJ_OPT_DIC_STORAGE */


    /*
     * 頻度学習領域サイズ取得処理
     * 統合辞書の場合のみ、正しい値を返す。それ以外の辞書は頻度学習領域がないので常に 0 を返す。
     */
    switch (dictype) {
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書(AWnnタイプ) */
    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書 */
        ret = njd_t_get_ext_area_size(iwnn, handle, size);
        break;

    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
    case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
        ret = njd_s_get_ext_area_size(iwnn, handle, size);
        break;

    default:
        *size = 0;
        ret = 0;
        break;
    };

    return ret;
}


/**
 * 頻度学習領域初期化API
 *
 * 頻度学習領域を初期化する。
 *
 * @attention    本関数は擬似辞書を許容しない
 *
 * @param[in,out] iwnn  解析情報クラス
 * @param[in] dic_type  辞書ハンドルタイプ
 * @param[in] handle    辞書ハンドル
 * @param[out] ext_area 頻度学習領域
 * @param[in] size     頻度学習領域サイズ
 *
 * @retval 1 初期化成功
 * @retval 0 初期化不要
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njx_init_ext_area(NJ_CLASS *iwnn, NJ_UINT8 dic_type, NJ_DIC_HANDLE handle, NJ_VOID *ext_area, NJ_UINT32 size) {
    NJ_UINT16 ret;
    NJ_UINT32 dictype;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_INIT_EXT_AREA, NJ_ERR_PARAM_ENV_NULL);
    }

    if (handle == NULL) {
        /* 第3引数(handle)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_INIT_EXT_AREA, NJ_ERR_PARAM_DIC_NULL);
    }

    if (ext_area == NULL) {
        /* 第4引数(ext_area)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_INIT_EXT_AREA, NJ_ERR_PARAM_EXT_AREA_NULL);
    }

    if (size == 0) {
        /* 第5引数(size)が0の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_INIT_EXT_AREA, NJ_ERR_PARAM_EXT_AREA_INVALID_SIZE);
    }

#ifdef NJ_OPT_DIC_STORAGE
    if (dic_type == NJ_DIC_H_TYPE_NORMAL) {
        dictype = NJ_GET_DIC_TYPE(handle);
    } else if (dic_type == NJ_DIC_H_TYPE_ON_STORAGE) {
        dictype = ((NJ_STORAGE_DIC_INFO*)handle)->dictype;
    } else {
        /* 第2引数(dic_type)がNJ_DIC_H_TYPE_NORMAL, NJ_DIC_H_TYPE_ON_STORAGE以外の場合は初期化不要とする */
        return 0;
    }
#else  /* NJ_OPT_DIC_STORAGE */
    if (dic_type == NJ_DIC_H_TYPE_NORMAL) {
        dictype = NJ_GET_DIC_TYPE(handle);
    } else {
        /* 第2引数(dic_type)がNJ_DIC_H_TYPE_NORMAL以外の場合は初期化不要とする */
        return 0;
    }
#endif /* NJ_OPT_DIC_STORAGE */


    /*
     * 頻度学習領域初期化処理
     * 各辞書アダプタ毎に呼び出す
     */
    switch (dictype) {
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書(AWnnタイプ)  */
    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書              */
        ret = njd_t_init_ext_area(iwnn, handle, ext_area, size);
        break;

    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
    case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
        ret = njd_s_init_ext_area(iwnn, handle, ext_area, size);
        break;

    default:
        /* 対象辞書ではないため、初期化不要とする */
        ret =  0;
        break;
    };

    return ret;
}


/**
 * 頻度学習領域チェックAPI
 *
 * 頻度学習領域をチェックする。
 *
 * @attention 擬似辞書を指定しないこと
 *
 * @param[in,out] iwnn  解析情報クラス
 * @param[in] dic_type  辞書ハンドルタイプ
 * @param[in] handle    辞書ハンドル
 * @param[in] ext_area  頻度学習領域
 * @param[in] size      頻度学習領域サイズ
 * @param[in] index     拡張領域Index番号
 *
 * @retval 1 チェック成功
 * @retval 0 チェック不要
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njx_check_ext_area(NJ_CLASS *iwnn, NJ_UINT8 dic_type, NJ_DIC_HANDLE handle, NJ_VOID *ext_area, NJ_UINT32 size, NJ_UINT8 index) {
    NJ_UINT16 ret;
    NJ_UINT32 dictype;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_PARAM_ENV_NULL);
    }

    if (handle == NULL) {
        /* 第3引数(handle)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_PARAM_DIC_NULL);
    }

    if (ext_area == NULL) {
        /* 第4引数(ext_area)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_PARAM_EXT_AREA_NULL);
    }

    if (size == 0) {
        /* 第5引数(size)が0の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_PARAM_EXT_AREA_INVALID_SIZE);
    }

    if (index > NJ_TYPE_EXT_AREA_MORPHO) {
        /* 第6引数(index)がNJ_TYPE_EXT_AREA_MORPHOより大きい場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_PARAM_VALUE_INVALID);
    }

#ifdef NJ_OPT_DIC_STORAGE
    if (dic_type == NJ_DIC_H_TYPE_NORMAL) {
        dictype = NJ_GET_DIC_TYPE(handle);
    } else if (dic_type == NJ_DIC_H_TYPE_ON_STORAGE) {
        dictype = ((NJ_STORAGE_DIC_INFO*)handle)->dictype;
    } else {
        /* 第2引数(dic_type)がNJ_DIC_H_TYPE_NORMAL, NJ_DIC_H_TYPE_ON_STORAGE以外の場合はチェック不要 */
        return 0;
    }
#else  /* NJ_OPT_DIC_STORAGE */
    if (dic_type == NJ_DIC_H_TYPE_NORMAL) {
        dictype = NJ_GET_DIC_TYPE(handle);
    } else {
        /* 第2引数(dic_type)がNJ_DIC_H_TYPE_NORMAL以外の場合はチェック不要 */
        return 0;
    }
#endif /* NJ_OPT_DIC_STORAGE */


    /*
     * 頻度学習領域チェック処理
     * 各辞書アダプタ毎に呼び出す
     */
    switch (dictype) {
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書(AWnnタイプ)  */
    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書              */
        if (index == NJ_TYPE_EXT_AREA_DEFAULT) {
            /* 頻度学習領域チェック */
            ret = njd_t_check_ext_area(iwnn, handle, ext_area, size);
        } else {
            /* 拡張領域チェック */
            ret = njd_t_check_ext_area2(iwnn, handle, ext_area, size);
        }
        break;

    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
    case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
        if (index == NJ_TYPE_EXT_AREA_DEFAULT) {
            /* 頻度学習領域チェック */
            ret = njd_s_check_ext_area(iwnn, handle, ext_area, size);
        } else {
            /* 拡張領域チェック */
            ret = njd_s_check_ext_area2(iwnn, handle, ext_area, size);
        }
        break;

    default:
        /* チェック不要 */
        ret =  0;
        break;
    };

    return ret;
}


/**
 * 付加情報領域チェックAPI
 *
 * 付加情報領域をチェックする。
 *
 * @attention 擬似辞書を指定しないこと
 *
 * @param[in,out] iwnn  解析情報クラス
 * @param[in] dic_type  辞書ハンドルタイプ
 * @param[in] handle    辞書ハンドル
 * @param[in] add_info  付加情報領域
 * @param[in] size      付加情報領域サイズ
 *
 * @retval 1 チェック成功
 * @retval 0 チェック不要
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njx_check_additional_info(NJ_CLASS *iwnn, NJ_UINT8 dic_type, NJ_DIC_HANDLE handle, NJ_VOID *add_info, NJ_UINT32 size) {
    NJ_UINT16 ret;
    NJ_UINT32 dictype;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_ADD_INFO, NJ_ERR_PARAM_ENV_NULL);
    }

    if (handle == NULL) {
        /* 第3引数(handle)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_ADD_INFO, NJ_ERR_PARAM_DIC_NULL);
    }

    if (add_info == NULL) {
        /* 第4引数(add_info)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_ADD_INFO, NJ_ERR_PARAM_ADD_INFO_NULL);
    }

    if (size == 0) {
        /* 第5引数(size)が0の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_ADD_INFO, NJ_ERR_PARAM_ADD_INFO_INVALID_SIZE);
    }

#ifdef NJ_OPT_DIC_STORAGE
    if (dic_type == NJ_DIC_H_TYPE_ON_STORAGE) {
        dictype = ((NJ_STORAGE_DIC_INFO*)handle)->dictype;
    } else if (dic_type == NJ_DIC_H_TYPE_NORMAL) {
        dictype = NJ_GET_DIC_TYPE(handle);
    } else {
        /* 第2引数(dic_type)がNJ_DIC_H_TYPE_NORMAL, NJ_DIC_H_TYPE_ON_STORAGE以外の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_ADD_INFO, NJ_ERR_PARAM_TYPE_INVALID);
    }
#else  /* NJ_OPT_DIC_STORAGE */
    if (dic_type == NJ_DIC_H_TYPE_NORMAL) {
        dictype = NJ_GET_DIC_TYPE(handle);
    } else {
        /* 第2引数(dic_type)がNJ_DIC_H_TYPE_NORMAL, NJ_DIC_H_TYPE_ON_STORAGE以外の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_ADD_INFO, NJ_ERR_PARAM_TYPE_INVALID);
    }
#endif /* NJ_OPT_DIC_STORAGE */

    /* 付加情報領域チェック処理を記載 */

    /*
     * 頻度学習領域チェック処理
     * 各辞書アダプタ毎に呼び出す
     */
    switch (dictype) {
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書(AWnnタイプ)  */
    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書              */
        ret = njd_t_check_additional_info(iwnn, handle, add_info, size);
        break;

    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
    case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
        ret = njd_s_check_additional_info(iwnn, handle, add_info, size);
        break;

    case NJ_DIC_TYPE_EXT_YOMINASI:               /* 拡張読み無し予測辞書  */
        ret = njd_p_check_additional_info(iwnn, handle, add_info, size);
        break;

    default:
        /* チェック不要 */
        ret =  0;
        break;
    };

    return ret;
}


#ifdef NJ_OPT_DIC_STORAGE
/**
 * ストレージ辞書キャッシュサイズの取得
 *
 * @param[in]   iwnn        解析情報クラス
 * @param[in]   filestream  ファイルポインタ
 * @param[in]   mode        動作モード
 *
 * @retval >=0  キャッシュサイズ
 * @retval <0   エラー
 */
NJ_INT32 njx_get_storage_dic_cache_size(NJ_CLASS *iwnn, NJ_FILE* filestream, NJ_UINT32 mode) {
    NJ_INT32  ret;
    NJ_UINT8  buf[4];
    NJ_UINT32 dictype;

    /* パラメータチェック */
    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STORAGE_DIC_CACHE_SIZE, NJ_ERR_PARAM_ENV_NULL);
    }

    if (filestream == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STORAGE_DIC_CACHE_SIZE, NJ_ERR_PARAM_STREAM_NULL);
    }

    /* ストレージ辞書対象かどうか判定する。 */
    ret = nj_fseek(filestream, 8, NJ_FILE_IO_SEEK_SET);
    if (ret != 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STORAGE_DIC_CACHE_SIZE, NJ_ERR_STREAM_SEEK_ERR);
    }
    ret = nj_fread(buf, 4, filestream);
    if (ret == 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STORAGE_DIC_CACHE_SIZE, NJ_ERR_STREAM_READ_ERR);
    }

    dictype = NJ_INT32_READ(buf);

    switch (dictype) {
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ)               */
    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書(AWnnタイプ)                 */
    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書(iWnnタイプ)                 */
        ret = njd_s_get_storage_dic_cache_size(iwnn, dictype, filestream, mode);
        break;

    default:
        ret = NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STORAGE_DIC_CACHE_SIZE, NJ_ERR_PARAM_TYPE_INVALID);
        break;
    };

    return ret;
}


/**
 * ストレージ辞書情報の設定
 *
 * @param[in]   iwnn        解析情報クラス
 * @param[out]  fdicinfo    ストレージ辞書情報
 * @param[in]   filestream  ファイルポインタ
 * @param[in]   cache_area  キャッシュ領域
 * @param[in]   cache_size  キャッシュバッファサイズ
 * @param[in]   mode        動作モード
 *
 * @retval =0  正常
 * @retval !=0 エラー
 */
NJ_INT32 njx_set_storage_dic_info(NJ_CLASS *iwnn, NJ_STORAGE_DIC_INFO *fdicinfo, 
                                  NJ_FILE* filestream, NJ_UINT8 *cache_area, NJ_UINT32 cache_size, NJ_UINT32 mode) {
    NJ_INT32  ret;
    NJ_UINT8  buf[4];
    NJ_UINT32 dictype;
    NJ_INT32  size;

    /* パラメータチェック */
    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STORAGE_DIC_INFO, NJ_ERR_PARAM_ENV_NULL);
    }

    if (filestream == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STORAGE_DIC_INFO, NJ_ERR_PARAM_STREAM_NULL);
    }

    if (fdicinfo == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STORAGE_DIC_INFO, NJ_ERR_PARAM_NULL);
    }

    if (cache_area == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STORAGE_DIC_INFO, NJ_ERR_PARAM_NULL);
    }

    size = njx_get_storage_dic_cache_size(iwnn, filestream, mode);
    if (size < 0) {
        return size;
    }

    if (cache_size < (NJ_UINT32)size) {
        /* キャッシュサイズ不足 */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STORAGE_DIC_INFO, NJ_ERR_CACHE_NOT_ENOUGH);
    }

    /* ストレージ辞書対象かどうか判定する。 */
    ret = nj_fseek(filestream,  8, NJ_FILE_IO_SEEK_SET);
    if (ret != 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STORAGE_DIC_INFO, NJ_ERR_STREAM_SEEK_ERR);
    }
    ret = nj_fread(buf, 4, filestream);
    if (ret == 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STORAGE_DIC_INFO, NJ_ERR_STREAM_READ_ERR);
    }

    dictype = NJ_INT32_READ(buf);


    switch (dictype) {
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ)               */
    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書(AWnnタイプ)                 */
    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書(iWnnタイプ)                 */
        ret = njd_s_set_storage_dic_info(iwnn, fdicinfo, dictype, filestream, cache_area, cache_size, mode);
        break;

    default:
        ret = NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STORAGE_DIC_INFO, NJ_ERR_PARAM_TYPE_INVALID);
        break;
    };

    return ret;
}
#endif /* NJ_OPT_DIC_STORAGE */


/**
 * ユーザープロファイルデータサイズの取得
 *
 * @param[in]       iwnn        解析情報クラス
 * @param[in]       dicset      辞書セット情報
 * @param[in/out]   prof_info   ユーザープロファイル情報
 *
 * @retval   >=0   ユーザープロファイルデータサイズ
 * @retval   <0    エラー
 */
NJ_INT32 njx_get_user_prof_data_size(NJ_CLASS *iwnn, NJ_DIC_SET *dicset, NJ_USER_PROF_INFO *prof_info) {
    NJ_INT32 ret;
    NJ_UINT32 dictype;
    NJ_INT16 cnt;
    NJ_EXT_HINDO_INFO *p_ext_hindo_info;

    /* パラメータチェック */
    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_USER_PROF_DATA_SIZE, NJ_ERR_PARAM_ENV_NULL);
    }
    if (dicset == NULL) {
        /* 第2引数(dicset)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_USER_PROF_DATA_SIZE, NJ_ERR_PARAM_DIC_NULL);
    }
    if (prof_info == NULL) {
        /* 第3引数(prof_info)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_USER_PROF_DATA_SIZE, NJ_ERR_PARAM_NULL);
    }

    /* 頻度学習値情報構造体 初期化 */
    p_ext_hindo_info = &prof_info->ext_hindo_info;
    p_ext_hindo_info->size = 0;
    p_ext_hindo_info->imp_offset = 0x00000000;
    p_ext_hindo_info->block_cnt = 0;
    p_ext_hindo_info->imp_block = 0;
    p_ext_hindo_info->imp_hindo = 0;
    p_ext_hindo_info->word_cnt = 0;
    p_ext_hindo_info->status = NJ_OP_IMP_HINDO_INIT;
    for (cnt = 0; cnt < NJ_MAX_EXT_HINDO; cnt++) {
        p_ext_hindo_info->hindo_data[cnt].word_cnt = 0;
        p_ext_hindo_info->hindo_data[cnt].size = 0;
        p_ext_hindo_info->hindo_data[cnt].offset = 0;
        p_ext_hindo_info->hindo_data[cnt].exp_offset = 0;
    }
    p_ext_hindo_info->delete_data.word_cnt = 0;
    p_ext_hindo_info->delete_data.size = 0;
    p_ext_hindo_info->delete_data.offset = 0;
    p_ext_hindo_info->delete_data.exp_offset = 0;
    ret = 0;

    for (cnt = 0; cnt < NJ_MAX_DIC; cnt++) {

        if ((dicset->dic[cnt].handle != NULL) && (dicset->dic[cnt].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL)) {

            dictype = NJ_GET_DIC_TYPE_EX(dicset->dic[cnt].type, dicset->dic[cnt].handle);

            switch (dictype) {
            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ)               */
            case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書(AWnnタイプ)                 */
            case NJ_DIC_TYPE_FUSION:                        /* 統合辞書(iWnnタイプ)                 */
                ret = njd_t_get_user_prof_data_size(iwnn, dicset->dic[cnt].handle, dicset->dic[cnt].ext_area[NJ_TYPE_EXT_AREA_DEFAULT], p_ext_hindo_info);
                break;

            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
            case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
            case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書)(iWnnタイプ) */
                ret = njd_s_get_user_prof_data_size(iwnn, dicset->dic[cnt].handle, dicset->dic[cnt].ext_area[NJ_TYPE_EXT_AREA_DEFAULT], p_ext_hindo_info);
                break;

            default:
                ret = 0;
                break;
            }
            if (ret < 0) {
                return ret;
            }
        }
    }

    /* プロファイルデータのトータルサイズを算出する */
    for (cnt = 0; cnt < NJ_MAX_EXT_HINDO; cnt++) {
        /* データサイズを取得 */
        if (p_ext_hindo_info->hindo_data[cnt].size > 0) {
            /* データが存在するもののみ、サイズと単語数を取得 */
            p_ext_hindo_info->size += p_ext_hindo_info->hindo_data[cnt].size;
            p_ext_hindo_info->word_cnt += p_ext_hindo_info->hindo_data[cnt].word_cnt;
        }
    }
    /* 削除フラグONのデータが存在する場合 */
    if (p_ext_hindo_info->delete_data.size > 0) {
        /* サイズと単語数を取得 */
        p_ext_hindo_info->size += p_ext_hindo_info->delete_data.size;
        p_ext_hindo_info->word_cnt += p_ext_hindo_info->delete_data.word_cnt;
    }

    /* 頻度学習値データが存在する場合 */
    if (p_ext_hindo_info->word_cnt > 0) {
        /* 共通ヘッダ + 終端識別子のサイズを加算して、戻り値を設定する。 */
        ret = (p_ext_hindo_info->size + NJ_EXT_HINDO_COMMON_HEADER_SIZE + NJ_EXT_HINDO_IDENTIFIER_SIZE);
    } else {
        /* データが0の場合は、サイズ0で返す */
        ret = 0;
    }

    return ret;
}


/**
 * ユーザープロファイルデータエクスポート
 *
 * @param[in]      iwnn        解析情報クラス
 * @param[in]      dicset      辞書セット情報
 * @param[in/out]  *prof_info  ユーザープロファイル情報
 * @param[out]     *exp_data   エクスポート領域先頭アドレス
 * @param[in]      size        エクスポート領域サイズ
 * @param[in]      hindo       エクスポートする頻度学習値リミット
 *
 * @retval   1  正常終了
 * @retval  <0  エラー
 */
NJ_INT32 njx_export_user_prof_data(NJ_CLASS *iwnn, NJ_DIC_SET *dicset, NJ_USER_PROF_INFO *prof_info,
                                   NJ_VOID *exp_data, NJ_INT32 size, NJ_UINT8 hindo) {
    NJ_INT32  ret;
    NJ_UINT32 dictype;
    NJ_INT16 cnt;
    NJ_UINT8 *wk_exp_data;
    NJ_INT32 wk_size;
    NJ_INT32 chk_size;
    NJ_UINT32 wk_offset;
    NJ_EXT_HINDO_INFO *p_ext_hindo_info;
    NJ_INT16 s_pos;
    NJ_UINT32 wk_word_cnt;

    /* パラメータチェック */
    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_EXP_USER_PROF_DATA, NJ_ERR_PARAM_ENV_NULL);
    }
    if (dicset == NULL) {
        /* 第2引数(dicset)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_EXP_USER_PROF_DATA, NJ_ERR_PARAM_DIC_NULL);
    }
    if (prof_info == NULL) {
        /* 第3引数(prof_info)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_EXP_USER_PROF_DATA, NJ_ERR_PARAM_NULL);
    }
    p_ext_hindo_info = &prof_info->ext_hindo_info;
    if (p_ext_hindo_info->word_cnt == 0) {
        /* 第3引数(prof_info)のデータが不正な場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_EXP_USER_PROF_DATA, NJ_ERR_PARAM_VALUE_INVALID);
    }
    if (exp_data == NULL) {
        /* 第4引数(exp_data)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_EXP_USER_PROF_DATA, NJ_ERR_PARAM_NULL);
    }
    /* データ登録数、データサイズを算出する */
    wk_size = p_ext_hindo_info->delete_data.size;
    wk_word_cnt = p_ext_hindo_info->delete_data.word_cnt;
    if (hindo < NJ_MAX_EXT_HINDO) {
        for (cnt = (NJ_MAX_EXT_HINDO - 1); cnt >= hindo; cnt--) {
            wk_size += p_ext_hindo_info->hindo_data[cnt].size;
            wk_word_cnt += p_ext_hindo_info->hindo_data[cnt].word_cnt;
        }
    }
    chk_size = wk_size + NJ_EXT_HINDO_COMMON_HEADER_SIZE + NJ_EXT_HINDO_IDENTIFIER_SIZE;
    if (chk_size > size) {
       /* 第5引数(size)が必要な領域サイズより小さい場合はエラー */
       return NJ_SET_ERR_VAL(NJ_FUNC_NJ_EXP_USER_PROF_DATA, NJ_ERR_PARAM_VALUE_INVALID);
    }

    /* プロファイルデータ構造体の初期化 */
    if (p_ext_hindo_info->word_cnt > 0) {
        s_pos = 0;
        for (cnt = (NJ_MAX_EXT_HINDO - 1); cnt >= 0; cnt--) {
            if (p_ext_hindo_info->hindo_data[cnt].size > 0) {
                s_pos = cnt;
                break;
            }
        }

        /* プロファイルデータのオフセット位置を算出する */
        /* 削除フラグONのデータが存在する場合 */
        if (p_ext_hindo_info->delete_data.size > 0) {
            /* 削除データのオフセットを設定 */
            p_ext_hindo_info->delete_data.offset = NJ_EXT_HINDO_COMMON_HEADER_SIZE;
            p_ext_hindo_info->delete_data.exp_offset = p_ext_hindo_info->delete_data.offset;
            wk_offset = p_ext_hindo_info->delete_data.offset + p_ext_hindo_info->delete_data.size;
            if (p_ext_hindo_info->hindo_data[s_pos].size > 0) {
                p_ext_hindo_info->hindo_data[s_pos].offset = wk_offset;
                p_ext_hindo_info->hindo_data[s_pos].exp_offset = p_ext_hindo_info->hindo_data[s_pos].offset;
                wk_offset = p_ext_hindo_info->hindo_data[s_pos].offset + p_ext_hindo_info->hindo_data[s_pos].size;
            }
        } else {
            /* データ内で頻度学習値最大のオフセットを設定 */
            p_ext_hindo_info->hindo_data[s_pos].offset = NJ_EXT_HINDO_COMMON_HEADER_SIZE;
            p_ext_hindo_info->hindo_data[s_pos].exp_offset = p_ext_hindo_info->hindo_data[s_pos].offset;
            wk_offset = p_ext_hindo_info->hindo_data[s_pos].offset + p_ext_hindo_info->hindo_data[s_pos].size;
        }
        if (s_pos > 0) {
            for (cnt = (s_pos - 1); cnt >= 0; cnt--) {
                if (p_ext_hindo_info->hindo_data[cnt].size > 0) {
                    p_ext_hindo_info->hindo_data[cnt].offset = wk_offset;
                    p_ext_hindo_info->hindo_data[cnt].exp_offset = p_ext_hindo_info->hindo_data[cnt].offset;
                    wk_offset = p_ext_hindo_info->hindo_data[cnt].offset + p_ext_hindo_info->hindo_data[cnt].size;
                }
                if (cnt <= hindo) {
                    /* リミットの頻度値を越えた場合、そこまでで処理を抜ける */
                    break;
                }
            }
        }
    }

    ret = 0;
    for (cnt = 0; cnt < NJ_MAX_DIC; cnt++) {

        if ((dicset->dic[cnt].handle != NULL) && (dicset->dic[cnt].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL)) {

            dictype = NJ_GET_DIC_TYPE_EX(dicset->dic[cnt].type, dicset->dic[cnt].handle);

            switch (dictype) {
            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ)               */
            case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書(AWnnタイプ)                 */
            case NJ_DIC_TYPE_FUSION:                        /* 統合辞書(iWnnタイプ)                 */
                ret = njd_t_export_user_prof_data(iwnn, dicset->dic[cnt].handle,
                                                  dicset->dic[cnt].ext_area[NJ_TYPE_EXT_AREA_DEFAULT], exp_data, p_ext_hindo_info, hindo);
                break;

            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
            case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
            case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書)(iWnnタイプ) */
                ret = njd_s_export_user_prof_data(iwnn, dicset->dic[cnt].handle,
                                                  dicset->dic[cnt].ext_area[NJ_TYPE_EXT_AREA_DEFAULT], exp_data, p_ext_hindo_info, hindo);
                break;

            default:
                ret = 0;
                break;
            }
            if (ret < 0) {
                return ret;
            }
        }
    }

    /* 共通ヘッダ情報の書き込み */
    wk_exp_data = (NJ_UINT8 *)exp_data;
    /* 識別子(NJUP) */
    NJ_INT32_WRITE(wk_exp_data, NJ_EXT_HINDO_IDENTIFIER);
    wk_exp_data += sizeof(NJ_INT32);

    /* データバージョン */
    NJ_INT32_WRITE(wk_exp_data, NJ_EXT_HINDO_VERSION);
    wk_exp_data += sizeof(NJ_INT32);

    /* ユーザープロファイルデータサイズ */
    NJ_INT32_WRITE(wk_exp_data, chk_size);
    wk_exp_data += sizeof(NJ_INT32);

    /* 頻度学習値データオフセット */
    if (wk_word_cnt > 0) {
        wk_offset = NJ_EXT_HINDO_COMMON_HEADER_SIZE;
    } else {
        wk_offset = 0x00000000;
    }
    NJ_INT32_WRITE(wk_exp_data, wk_offset);
    wk_exp_data += sizeof(NJ_INT32);

    /* 頻度学習値データサイズ */
    NJ_INT32_WRITE(wk_exp_data, wk_size);
    wk_exp_data += sizeof(NJ_INT32);

    /* 頻度学習値データ登録数 */
    NJ_INT32_WRITE(wk_exp_data, wk_word_cnt);
    wk_exp_data += sizeof(NJ_INT32);

    /* リザーブ領域 */
    for (cnt = 0; cnt < 9; cnt++) {
        NJ_INT32_WRITE(wk_exp_data, NJ_EXT_HINDO_RESERVE);
        wk_exp_data += sizeof(NJ_INT32);
    }

    /* 頻度学習値データサイズオフセットを進める */
    wk_exp_data += wk_size;

    /* 識別子(NJUP) */
    NJ_INT32_WRITE(wk_exp_data, NJ_EXT_HINDO_IDENTIFIER);

    return ret;
}


/**
 * ユーザープロファイルデータインポート
 *
 * @param[in]      iwnn        解析情報クラス
 * @param[in/out]  dicset      辞書セット情報
 * @param[in/out]  *prof_info  ユーザープロファイル情報
 * @param[in]      *imp_data   インポート領域先頭アドレス
 * @param[in]      hindo       インポートする頻度学習値リミット
 *
 * @retval   1  データインポート継続可能
 * @retval   0  データインポート終了
 * @retval  <0  エラー
 */
NJ_INT32 njx_import_user_prof_data(NJ_CLASS *iwnn, NJ_DIC_SET *dicset, NJ_USER_PROF_INFO *prof_info,
                                   NJ_VOID *imp_data, NJ_UINT8 hindo) {
    NJ_INT32 ret;
    NJ_UINT32 dictype;
    NJ_INT16 cnt;
    NJ_WORD_INFO word_info;
    NJ_UINT8 yomi_b_len;
    NJ_UINT8 chk_b_len;
    NJ_UINT8 hyoki_b_len;
    NJ_UINT8 wk_hyoki_b_len;
    NJ_UINT8 yomi_c_len;
    NJ_UINT8 hyoki_c_len;
    NJ_UINT8 *p_data;
    NJ_UINT8 *p_yomi, *p_hyoki;
    NJ_UINT16 hinsi_info;
    NJ_CHAR tmp_buf[NJ_MAX_LEN + NJ_TERM_SIZE];
    NJ_INT16 i, j;
    NJ_UINT8 hindo_data;
    NJ_UINT32 data_size;
    NJ_UINT32 data_offset;
    NJ_UINT32 ext_data_size;
    NJ_UINT32 word_cnt;
    NJ_EXT_HINDO_INFO *p_ext_hindo_info;
    NJ_INT16 fhinsi;
    NJ_INT16 bhinsi;

    /* パラメータチェック */
    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_IMP_USER_PROF_DATA, NJ_ERR_PARAM_ENV_NULL);
    }
    if (dicset == NULL) {
        /* 第2引数(dicset)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_IMP_USER_PROF_DATA, NJ_ERR_PARAM_DIC_NULL);
    }
    if (prof_info == NULL) {
        /* 第3引数(prof_info)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_IMP_USER_PROF_DATA, NJ_ERR_PARAM_NULL);
    }
    if (imp_data == NULL) {
        /* 第4引数(imp_data)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_IMP_USER_PROF_DATA, NJ_ERR_PARAM_NULL);
    }

    p_ext_hindo_info = &prof_info->ext_hindo_info;

    /* インポートデータチェック */
    /* 識別子(NJUP)チェック */
    p_data = (NJ_UINT8 *)imp_data;
    if (NJ_INT32_READ(p_data) != NJ_EXT_HINDO_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_IMP_USER_PROF_DATA, NJ_ERR_PARAM_VALUE_INVALID);
    }
    p_data += sizeof(NJ_INT32);

    /* バージョン情報 */
    if (NJ_INT32_READ(p_data) != NJ_EXT_HINDO_VERSION) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_IMP_USER_PROF_DATA, NJ_ERR_PARAM_VALUE_INVALID);
    }
    p_data += sizeof(NJ_INT32);

    /* ユーザープロファイルデータ取得 */
    data_size = NJ_INT32_READ(p_data);
    p_data += sizeof(NJ_INT32);

    /* 頻度学習値データオフセット */
    data_offset = NJ_INT32_READ(p_data);
    p_data += sizeof(NJ_INT32);

    /* 頻度学習値データサイズ */
    ext_data_size = NJ_INT32_READ(p_data);
    p_data += sizeof(NJ_INT32);

    /* 頻度学習値データ登録数 */
    word_cnt = NJ_INT32_READ(p_data);

    /* 終端識別子(NJUP) */
    p_data = (NJ_UINT8 *)imp_data;
    if (NJ_INT32_READ((NJ_UINT8 *)p_data + data_size - NJ_EXT_HINDO_IDENTIFIER_SIZE) != NJ_EXT_HINDO_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_IMP_USER_PROF_DATA, NJ_ERR_PARAM_VALUE_INVALID);
    }

    if (p_ext_hindo_info->status != NJ_OP_IMP_HINDO_READY) {
        /* 頻度学習値データ領域の先頭へ移動する */
        p_ext_hindo_info->imp_offset = data_offset;
        p_ext_hindo_info->imp_block = 0;
        p_ext_hindo_info->imp_hindo = 0;
        p_ext_hindo_info->block_cnt = word_cnt / NJ_MAX_IMP_EXT_HINDO_CNT;
        p_ext_hindo_info->size = data_size;
        if (word_cnt % NJ_MAX_IMP_EXT_HINDO_CNT > 0) {
            p_ext_hindo_info->block_cnt++;
        }
    }

    for (i = 0; i < NJ_MAX_IMP_EXT_HINDO_CNT; i++) {
        /* インポートする単語情報を設定 */
        yomi_b_len = 0;
        hyoki_b_len = 0;
        chk_b_len = 0;
        hinsi_info = 0;
        hindo_data = 0;
        ret = njd_init_word_info(&word_info);
        p_data = (NJ_UINT8 *)imp_data + p_ext_hindo_info->imp_offset;

        /* 読みサイズを取得 */
        yomi_b_len = (*p_data) & 0x7F;
        yomi_c_len = yomi_b_len / sizeof(NJ_CHAR);
        p_data += NJ_EXT_HINDO_YOMI_DATA_SIZE;
        p_ext_hindo_info->imp_offset += NJ_EXT_HINDO_YOMI_DATA_SIZE;

        /* 読み文字列を取得 */
        p_yomi = p_data;
        p_data += yomi_b_len;
        p_ext_hindo_info->imp_offset += yomi_b_len;

        /* 表記サイズを取得 */
        hyoki_b_len = (*p_data) & 0x7F;
        chk_b_len = *p_data;
        if (hyoki_b_len == 0) {
            hyoki_c_len = yomi_c_len;
            wk_hyoki_b_len = yomi_b_len;
        } else {
            hyoki_c_len = hyoki_b_len / sizeof(NJ_CHAR);
            wk_hyoki_b_len = hyoki_b_len;
        }
        p_data += NJ_EXT_HINDO_HYOKI_DATA_SIZE;
        p_ext_hindo_info->imp_offset += NJ_EXT_HINDO_HYOKI_DATA_SIZE;

        /* 表記文字列を取得 */
        if (hyoki_b_len == 0) {
            p_hyoki = p_yomi;
        } else {
            p_hyoki = p_data;
        }
        p_data += hyoki_b_len;
        p_ext_hindo_info->imp_offset += hyoki_b_len;

        /* 前品詞番号を取得 */
        fhinsi = (NJ_UINT16)NJ_INT16_READ(p_data);
        p_data += NJ_EXT_HINDO_HINSI_DATA_SIZE;
        p_ext_hindo_info->imp_offset += NJ_EXT_HINDO_HINSI_DATA_SIZE;

        /* 後品詞番号を取得 */
        bhinsi = (NJ_UINT16)NJ_INT16_READ(p_data);
        p_data += NJ_EXT_HINDO_HINSI_DATA_SIZE;
        p_ext_hindo_info->imp_offset += NJ_EXT_HINDO_HINSI_DATA_SIZE;

        /* 頻度学習データ */
        hindo_data = *p_data;
        p_data += NJ_EXT_HINDO_HINDO_DATA_SIZE;
        p_ext_hindo_info->imp_offset += NJ_EXT_HINDO_HINDO_DATA_SIZE;

        /* 頻度データチェック */
        if (IS_HINDO_DATA_OVER(hindo, hindo_data)) {
            /* 強制的に最大値に設定し、処理を抜ける */
            ret = 0;
            p_ext_hindo_info->imp_offset = 0x00000000;
            p_ext_hindo_info->status = NJ_OP_IMP_HINDO_END;
            break;
        }

        /* インポート対象チェック */
        if ((yomi_c_len > NJ_MAX_LEN) || (hyoki_c_len > NJ_MAX_RESULT_LEN)) {
            if (IS_END_IMP_OFFSET(p_ext_hindo_info->imp_offset, ext_data_size)) {
                /* 強制的に最大値に設定し、処理を抜ける */
                ret = 0;
                p_ext_hindo_info->imp_offset = 0x00000000;
                p_ext_hindo_info->status = NJ_OP_IMP_HINDO_END;
                break;
            }
            ret = 0;
            continue;
        }

        /* 単語情報を設定 */
        nj_memcpy((NJ_UINT8 *)word_info.yomi, p_yomi, yomi_b_len);
        word_info.yomi[yomi_c_len] = NJ_CHAR_NUL;
        word_info.stem.yomi_len = yomi_c_len;
        if ((chk_b_len & 0x80) == 0) {
            /* カタカナ以外(ひらがな、漢字) */
            nj_memcpy((NJ_UINT8 *)word_info.kouho, p_hyoki, wk_hyoki_b_len);
        } else {
            /* カタカナ */
            for (j = 0; j < yomi_c_len; j++) {
                NJ_CHAR_COPY(tmp_buf + j, p_yomi);
                p_yomi += sizeof(NJ_CHAR);
            }
            tmp_buf[yomi_c_len] = NJ_CHAR_NUL;
            nje_convert_hira_to_kata(tmp_buf, word_info.kouho, yomi_c_len);
        }
        word_info.kouho[yomi_c_len] = NJ_CHAR_NUL;
        word_info.stem.kouho_len = hyoki_c_len;

        for (cnt = 0; cnt < NJ_MAX_DIC; cnt++) {

            if ((dicset->dic[cnt].handle != NULL) && (dicset->dic[cnt].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL)) {

                dictype = NJ_GET_DIC_TYPE_EX(dicset->dic[cnt].type, dicset->dic[cnt].handle);

                switch (dictype) {
                case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ)               */
                case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書(AWnnタイプ)                 */
                case NJ_DIC_TYPE_FUSION:                        /* 統合辞書(iWnnタイプ)                 */
                    ret = njd_t_import_user_prof_data(iwnn, &word_info, cnt, fhinsi, bhinsi, hindo_data);
                    break;

                case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
                case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
                case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書)(iWnnタイプ) */
                    ret = njd_s_import_user_prof_data(iwnn, &word_info, cnt, fhinsi, bhinsi, hindo_data);
                    break;

                default:
                    ret = 0;
                    break;
                };
            }
        }

        /* インポートデータ数チェック */
        /* インポート中のブロックが最終で終端の単語の場合 */
        if (!IS_END_IMP_OFFSET(p_ext_hindo_info->imp_offset, ext_data_size)) {
            /* インポート継続可能 */
            ret = 1;
            p_ext_hindo_info->status = NJ_OP_IMP_HINDO_READY;
        } else {
            /* インポート終了(強制的に最大値に設定し、処理を抜ける) */
            ret = 0;
            p_ext_hindo_info->imp_offset = 0x00000000;
            p_ext_hindo_info->status = NJ_OP_IMP_HINDO_END;
            break;
        }
    }
    /* インポート中のブロック数と頻度学習値を更新 */
    p_ext_hindo_info->imp_block++;
    p_ext_hindo_info->imp_hindo = (hindo_data & 0x3F);

    return ret;
}


/**
 * 処理結果情報から詳細な単語登録情報を取得する。
 *
 * @param[in] iwnn          解析情報クラス
 * @param[in] result        処理結果情報
 * @param[out] info         単語登録情報
 *
 * @retval >0  正常終了
 * @retval <0  エラー
 */
NJ_INT16 njd_get_word_info(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_WORD_INFO *info) {
    NJ_INT16  ret;
    NJ_UINT8  stem_len = 0;
    NJ_UINT8  fzk_len;
    NJ_INT32 ret32;
    NJ_UINT16 ylen, klen, fzlen;
    NJ_UINT16 stem_b_hinsi;


    /* infoの初期化 */
    info->hinsi_group    = NJ_HINSI_DETAIL; /* 品詞グループは詳細モード */
    info->yomi[0]        = NJ_CHAR_NUL;
    info->kouho[0]       = NJ_CHAR_NUL;
    info->additional[0]  = NJ_CHAR_NUL;
    info->stem.yomi_len  = 0;
    info->stem.kouho_len = 0;
    info->stem.hinsi     = 0;
    info->stem.freq      = 0;
    info->stem.attr      = 0;
    info->fzk.yomi_len   = 0;
    info->fzk.kouho_len  = 0;
    info->fzk.hinsi      = 0;
    info->fzk.freq       = 0;
    info->connect        = 0;


    ret32 = njx_get_additional_info(iwnn, result, (-1), info->additional, sizeof(info->additional));
    if (ret32 < 0) {
        return (NJ_INT16)ret32; /*NCH_FB*/
    }

    if (NJ_GET_RESULT_OP(result->operation_id) == NJ_OP_MORPHOLIZE) {
        if (NJ_GET_RESULT_FUNC(result->operation_id) == NJ_FUNC_SPLIT_WORD) {
            /* 分かち書きAPI処理結果の場合 */
            ret = mmx_get_info(iwnn, result, info->yomi, sizeof(info->yomi), &stem_len, NULL);
        } else {
            /* 形態素解析読み情報取得API処理結果の場合 */
            ret = njx_get_stroke(iwnn, result, info->yomi, sizeof(info->yomi));
        }
        if (ret < 0) {
            return ret; /*NCH_FB*/
        } else if (ret == 0) {
            /* 読みを取得できない場合、エラーとする */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_WORD_INFO, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
        }
        ret = njx_get_candidate(iwnn, result, info->kouho, sizeof(info->kouho));
        if (ret < 0) {
            return ret; /*NCH_FB*/
        }
        ylen = nj_strlen(info->yomi);
        klen = nj_strlen(info->kouho);
        fzlen= ylen - stem_len;

        info->stem.yomi_len  = stem_len;
        info->stem.kouho_len = klen - fzlen;
        info->stem.hinsi     = NJ_GET_STEM_HINSI_FROM_NJ_WORD(&result->word);
        info->stem.freq      = NJ_GET_FREQ_FROM_STEM(&result->word);
        info->stem.attr      = result->word.stem.loc.attr;

        info->fzk.yomi_len  = fzlen;
        info->fzk.kouho_len = fzlen;
        info->fzk.hinsi     = NJ_GET_FZK_HINSI_FROM_NJ_WORD(&result->word);
        info->fzk.freq      = NJ_GET_FREQ_FROM_FZK(&result->word);

        info->connect = 0;

    } else {
        /* 読み文字列・表記文字列を取得する */
        ret = njx_get_stroke(iwnn, result, info->yomi, sizeof(info->yomi));
        if (ret < 0) {
            return ret; /*NCH_FB*/
        } else {
            ylen = (NJ_UINT16)ret;
        }
        ret = njx_get_candidate(iwnn, result, info->kouho, sizeof(info->kouho));
        if (ret < 0) {
            return ret; /*NCH_FB*/
        } else {
            klen = (NJ_UINT16)ret;
        }

        info->fzk.yomi_len  = NJ_GET_YLEN_FROM_FZK(&result->word);
        info->fzk.kouho_len = NJ_GET_KLEN_FROM_FZK(&result->word);
        info->fzk.hinsi     = NJ_GET_FZK_HINSI_FROM_NJ_WORD(&result->word);
        info->fzk.freq      = NJ_GET_FREQ_FROM_FZK(&result->word);

        info->stem.yomi_len  = ylen - info->fzk.yomi_len;
        info->stem.kouho_len = klen - info->fzk.kouho_len;
        info->stem.hinsi     = NJ_GET_STEM_HINSI_FROM_NJ_WORD(&result->word);
        info->stem.freq      = NJ_GET_FREQ_FROM_STEM(&result->word);
        info->stem.attr      = result->word.stem.loc.attr;
        
        info->connect = 0;
        if (result->word.stem.loc.handle != NULL) {
            /* 通常辞書のみ接続フラグの処理を行う */
            if (NJ_GET_DIC_TYPE_EX(result->word.stem.loc.type,
                                   result->word.stem.loc.handle) == NJ_DIC_TYPE_LEARN) {
                /* 学習辞書のみ接続フラグの処理を行う */
                if ((NJ_GET_RESULT_OP(result->operation_id) == NJ_OP_SEARCH) ||
                    ((NJ_GET_RESULT_OP(result->operation_id) == NJ_OP_ANALYZE) &&
                     ((NJ_GET_RESULT_FUNC(result->operation_id) == NJ_FUNC_SEARCH) ||
                      (NJ_GET_RESULT_FUNC(result->operation_id) == NJ_FUNC_NEXT) ||
                      (NJ_GET_RESULT_FUNC(result->operation_id) == NJ_FUNC_RELATION)))) {
                    /*
                     * 以下の場合に、接続フラグの算出処理を行う
                     * ・辞書引き結果
                     * ・予測結果(予測＆読み無し予測＆絞込予測)
                     */
                    info->connect = njd_l_check_word_connect(iwnn, &result->word);
                }
                /* 学習辞書の自立語/付属語分割処理 */
                if (((result->word.stem.loc.current_info & 0xF0) == 0x10) && 
                    (result->word.stem.type == 0)) {
                    /* iWnnタイプの学習辞書の場合、拡張領域から情報を取得。 */
                    ret = njd_l_get_ext_word_data(iwnn, &result->word, &stem_b_hinsi, &fzk_len);
                    if ((ret) && (fzk_len > 0) &&
                        (fzk_len <= info->stem.yomi_len) && (fzk_len <= info->stem.kouho_len)) {
                        /* 品詞の変更 */
                        if ((info->stem.yomi_len == fzk_len) && (info->stem.kouho_len == fzk_len)) {
                            if (info->fzk.yomi_len == 0) {
                                info->fzk.hinsi = info->stem.hinsi;
                            } else {
                                info->fzk.hinsi =
                                    (info->stem.hinsi & 0xFFFF0000) | (info->fzk.hinsi & 0x0000FFFF);
                            }
                            info->stem.hinsi = 0x00000000;
                            info->fzk.freq = info->stem.freq;
                            info->stem.freq = 0;
                        } else {
                            if (info->fzk.yomi_len == 0) {
                                info->fzk.hinsi = (info->stem.hinsi & 0x0000FFFF) | 0x00010000;
                            } else {
                                info->fzk.hinsi = (info->fzk.hinsi & 0x0000FFFF) | 0x00010000; /*NCH_DEF*/
                            }
                            info->stem.hinsi = (info->stem.hinsi & 0xFFFF0000) | (NJ_UINT32)stem_b_hinsi;
                        }
                        /* 文字配列長の変更 */
                        info->stem.yomi_len  -= fzk_len;
                        info->stem.kouho_len -= fzk_len;
                        info->fzk.yomi_len   += fzk_len;
                        info->fzk.kouho_len  += fzk_len;
                    }
                }
            }
        }
    }

    return 1;
}


/**
 * 単語登録
 *
 * 各辞書に単語登録を行う
 *
 * @param[in,out] iwnn      解析情報クラス
 * @param[in]     lword     単語情報
 * @param[in]     connect   ひとつ前に確定した候補との関係情報を作成するかのフラグ。<br>
 *                           0: 関係情報を作成しない<br>
 *                           1: 関係情報を作成する
 * @param[in]     undo      アンドゥ指定<br>
 *                           0:アンドゥフラグ立てない<br>
 *                           1:アンドゥフラグ立てる
 *
 * @retval >=0 正常終了
 * @retval <0  エラー
 */
NJ_INT16 njd_add_word(NJ_CLASS *iwnn, NJ_LEARN_WORD_INFO *lword,
                      NJ_UINT8 connect, NJ_UINT8 undo) {
    NJ_DIC_INFO *dicinfo;
    NJ_INT16 ret;
    NJ_UINT16 i;
    NJ_UINT32 dictype;
    NJ_PROGRAM_DIC_IF program_dic_operation;
    NJ_PROGRAM_DIC_MESSAGE prog_msg;
    NJG_LEARN_WORD word_info;


    ret = 0;

    for (i = 0; i < NJ_MAX_DIC; i++) {
        /* 辞書情報を格納 */
        dicinfo = &(iwnn->dic_set.dic[i]);

        if (dicinfo->handle == NULL) {
            /* 辞書セットがマウントされていない */
            continue;
        }

        /* "擬似辞書"または"擬似辞書-付属語-"の場合だけ処理を実施（他は無視） */
        dictype = NJ_GET_DIC_TYPE_EX(NJ_GET_DIC_INFO(dicinfo), dicinfo->handle);
        if ((dictype == NJ_DIC_TYPE_PROGRAM) ||
            (dictype == NJ_DIC_TYPE_PROGRAM_FZK)) {
            /* 擬似辞書呼出 */
            /* 単語登録情報の設定 */
            word_info.f_hinsi = lword->f_hinsi;
            word_info.b_hinsi = lword->b_hinsi;
            word_info.connect = connect;
            word_info.undo    = undo;

            /* 関数ポインタの設定 */
            program_dic_operation = (NJ_PROGRAM_DIC_IF)(dicinfo->handle);

            /* 擬似辞書メッセージを設定 */
            njd_init_program_dic_message(&prog_msg);
            prog_msg.dicset = &(iwnn->dic_set);
            prog_msg.lword= &word_info;
            prog_msg.stroke = lword->yomi;
            prog_msg.string = lword->hyouki;
            prog_msg.additional = lword->additional;
            prog_msg.stroke_size = lword->yomi_len;             /* 変数名は size だが、NJG_OP_ADD_WORD の場合は文字配列長を扱う */
            prog_msg.string_size = lword->hyouki_len;           /* 変数名は size だが、NJG_OP_ADD_WORD の場合は文字配列長を扱う */
            prog_msg.additional_size = lword->additional_len;   /* 変数名は size だが、NJG_OP_ADD_WORD の場合は文字配列長を扱う */
            prog_msg.dic_idx = i;
            ret = (*program_dic_operation)(iwnn, NJG_OP_ADD_WORD, &prog_msg);
            if (ret < 0) {
                return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, GET_ERR_FUNCVAL(ret));
            }
        }

    }

    return 0;
}


/**
 * 学習通知
 *
 * 各辞書に学習通知を行う
 *
 * @param[in,out] iwnn      解析情報クラス
 * @param[in]     l_result  処理結果
 * @param[in]     lword     単語情報
 * @param[in]     connect   ひとつ前に確定した候補との関係情報を作成するかのフラグ。<br>
 *                           0: 関係情報を作成しない<br>
 *                           1: 関係情報を作成する
 * @param[in]     undo_flag アンドゥ指定<br>
 *                           0:アンドゥフラグ立てない<br>
 *                           1:アンドゥフラグ立てる
 * @param[in]     ext_hindo_flag 頻度学習フラグ
 *
 * @retval >=0 正常終了
 * @retval <0  エラー
 */
NJ_INT16 njd_learn_word(NJ_CLASS *iwnn, NJ_RESULT *l_result, NJ_LEARN_WORD_INFO *lword,
                        NJ_UINT8 connect, NJ_UINT8 undo_flag, NJ_UINT8 ext_hindo_flag) {
    NJ_INT16 ret;

    NJ_DIC_INFO *dicinfo;
    NJ_UINT16 i;
    NJ_UINT32 dictype;
    NJ_PROGRAM_DIC_IF program_dic_operation;
    NJ_PROGRAM_DIC_MESSAGE prog_msg;
    NJG_LEARN_WORD word_info;


    ret = 0;

    /* 単語登録情報の設定 */
    word_info.f_hinsi = lword->f_hinsi;
    word_info.b_hinsi = lword->b_hinsi;
    word_info.connect = connect;
    word_info.undo    = undo_flag;

    /* 辞書I/Fメッセージを設定 */
    njd_init_program_dic_message(&prog_msg);
    prog_msg.dicset = &(iwnn->dic_set);
    prog_msg.word = &l_result->word;
    prog_msg.lword= &word_info;
    prog_msg.stroke = lword->yomi;
    prog_msg.string = lword->hyouki;
    prog_msg.additional = lword->additional;
    prog_msg.stroke_size = lword->yomi_len;            /* 変数名は size だが、NJG_OP_LEARN の場合は文字配列長を扱う */
    prog_msg.string_size = lword->hyouki_len;          /* 変数名は size だが、NJG_OP_LEARN の場合は文字配列長を扱う */
    prog_msg.additional_size = lword->additional_len;  /* 変数名は size だが、NJG_OP_LEARN の場合は文字配列長を扱う */

    for (i = 0; i < NJ_MAX_DIC; i++) {
        /* 辞書情報を格納 */
        dicinfo = &(iwnn->dic_set.dic[i]);

        if (dicinfo->handle == NULL) {
            /* 辞書セットがマウントされていない */
            continue;
        }

        /* 擬似辞書、通常辞書の判定 */
        dictype = NJ_GET_DIC_TYPE_EX(NJ_GET_DIC_INFO(dicinfo), dicinfo->handle);

        switch (dictype) {
        case NJ_DIC_TYPE_PROGRAM:                    /* 擬似辞書              */
        case NJ_DIC_TYPE_PROGRAM_FZK:                /* 擬似辞書-付属語-      */
            /* 擬似辞書呼出 */
            /* 関数ポインタの設定 */
            program_dic_operation = (NJ_PROGRAM_DIC_IF)(dicinfo->handle);
            /* 辞書I/Fメッセージを設定 */
            prog_msg.dic_idx = i;
            ret = (*program_dic_operation)(iwnn, NJG_OP_LEARN, &prog_msg);
            if (ret < 0) {
                return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, GET_ERR_FUNCVAL(ret));
            }
            break;

        case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
        case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書(AWnnタイプ)  */
        case NJ_DIC_TYPE_FUSION:                        /* 統合辞書              */
            if (ext_hindo_flag) {
                /*
                 * 統合辞書の頻度学習処理を行うために統合辞書アダプタの呼び出しを行う。
                 */
                ret = njd_t_learn_word(iwnn, &l_result->word, lword, i);
            }
            break;

        case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
        case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
        case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
            if (ext_hindo_flag) {
                /*
                 * 統合辞書(ストレージ辞書)の頻度学習処理を行うために統合辞書(ストレージ辞書)アダプタの呼び出しを行う。
                 */
                ret = njd_s_learn_word(iwnn, &l_result->word, lword, i);
            }
            break;

        default:
            break;
        };

        /* 辞書セットを処理中にエラーが検出された場合、すぐにループを抜けてエラーを返す */
        if (ret < 0) {
            break;
        }
    }

    return ret;
}


/**
 * 頻度学習領域最適化関数
 *
 * 頻度学習領域を最適化する。
 *
 * @param[in,out] iwnn  解析情報クラス
 *
 * @retval 1 最適化成功
 * @retval 0 最適化不要
 * @retval <0 エラー
 */
NJ_INT16 njd_optimize_ext_area(NJ_CLASS *iwnn) {
    NJ_INT16 ret = 0;
    NJ_UINT32 dictype;
    NJ_UINT16 i;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_INIT_EXT_AREA, NJ_ERR_PARAM_ENV_NULL); /*NCH_FB*/
    }

    for (i = 0; i < NJ_MAX_DIC; i++) {
        /* 辞書がない、もしくは頻度学習領域がない場合は、スキップする */
        if ((iwnn->dic_set.dic[i].handle == NULL) || (iwnn->dic_set.dic[i].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] == NULL)) {
            continue;
        }

        /*
         * 頻度学習領域最適化処理
         * 統合辞書のみが対象となる
         */
        dictype = NJ_GET_DIC_TYPE_EX(iwnn->dic_set.dic[i].type, iwnn->dic_set.dic[i].handle);
        switch (dictype) {
        case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
        case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書(AWnnタイプ) */
        case NJ_DIC_TYPE_FUSION:                        /* 統合辞書 */
            ret = njd_t_optimize_ext_area(iwnn, iwnn->dic_set.dic[i].handle, iwnn->dic_set.dic[i].ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
            break;

        case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
        case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
        case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
            ret = njd_s_optimize_ext_area(iwnn, iwnn->dic_set.dic[i].handle, iwnn->dic_set.dic[i].ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
            break;

        default:
            /* 何もしない */
            break;
        }

        /* 辞書セットを処理中にエラーが検出された場合、その時点で処理を中断してループを抜ける */
        if (ret < 0) {
            break;
        }
    }

    return ret;
}


/**
 * 擬似辞書メッセージ構造体を初期化
 *
 * @param[out]  prog_msg : 擬似辞書メッセージ構造体
 *
 * @retval                 常に !0
 */
NJ_INT16 njd_init_program_dic_message(NJ_PROGRAM_DIC_MESSAGE* prog_msg) {

    prog_msg->condition   = NULL;
    prog_msg->location    = NULL;
    prog_msg->dicset      = NULL;
    prog_msg->word        = NULL;
    prog_msg->lword       = NULL;
    prog_msg->stroke      = NULL;
    prog_msg->string      = NULL;
    prog_msg->stroke_size = 0;
    prog_msg->string_size = 0;
    prog_msg->dic_idx     = 0;

    return 1;
}


/**
 * 検索単語情報構造体の初期化
 *
 * @param[out]  loctset : 検索単語情報構造体
 *
 * @retval      常に !0
 */
NJ_INT16 njd_init_search_location_set(NJ_SEARCH_LOCATION_SET* loctset) {
    NJ_INT16 i;

    loctset->cache_freq         = 0;
    loctset->dic_freq.base      = 0;
    loctset->dic_freq.high      = 0;
    loctset->loct.type          = NJ_DIC_H_TYPE_NORMAL;
    loctset->loct.handle        = NULL;
    for (i = 0; i < NJ_MAX_EXT_AREA; i++) {
        loctset->loct.ext_area[i] = NULL;
    }
    for (i = 0; i < NJ_MAX_ADDITIONAL_INFO; i++) {
        loctset->loct.add_info[i] = NULL;
    }
    loctset->loct.current_info  = 0x10;  /* num=1, offset = 0    */
    loctset->loct.current       = 0;
    loctset->loct.top           = 0;
    loctset->loct.bottom        = 0;
    loctset->loct.attr          = 0x00000000;
    loctset->loct.current_cache = 0;
    loctset->loct.status        = NJ_ST_SEARCH_NO_INIT;
    for (i = 0; i < NJ_MAX_PHR_CONNECT; i++) {
        loctset->loct.relation[i] = 0;
    }

    return 1;
}


/**
 * 辞書検索条件セット構造体の初期化
 *
 * @param[out]  condition : 辞書検索条件セット構造体
 *
 * @retval      常に !0
 */
NJ_INT16 njd_init_search_condition(NJ_SEARCH_CONDITION* condition) {

    condition->operation            = 0;
    condition->mode                 = 0;
    condition->ds                   = NULL;
    condition->hinsi.fore           = NULL;
    condition->hinsi.foreSize       = 0;
    condition->hinsi.foreFlag       = 0;
    condition->hinsi.rear           = NULL;
    condition->hinsi.yominasi_fore  = NULL;
    condition->hinsi.prev_bpos      = 0;
    condition->yomi                 = NULL;
    condition->ylen                 = 0;
    condition->yclen                = 0;
    condition->kanji                = NULL;
    condition->charset              = NULL;
    condition->fzkconnect           = 0;
    condition->ctrl_opt             = 0;
    condition->attr                 = 0x00000000;

    return 1;
}


/**
 * 単語情報構造体の初期化
 *
 * @param[out]  word : 単語情報構造体
 *
 * @retval      常に !0
 */
NJ_INT16 njd_init_word(NJ_WORD* word) {
    NJ_INT16 i;

    word->yomi                  = NULL;
    word->stem.info1            = 0;
    word->stem.info2            = 0;
    word->stem.hindo            = 0;
    word->stem.type             = NJ_TYPE_UNDEFINE;
    word->fzk.info1             = 0;
    word->fzk.info2             = 0;
    word->fzk.hindo             = 0;

    word->stem.loc.handle       = NULL;
    word->stem.loc.type         = NJ_DIC_H_TYPE_NORMAL;
    for (i = 0; i < NJ_MAX_EXT_AREA; i++) {
        word->stem.loc.ext_area[i] = NULL;
    }
    for (i = 0; i < NJ_MAX_ADDITIONAL_INFO; i++) {
        word->stem.loc.add_info[i] = NULL;
    }
    word->stem.loc.current      = 0;
    word->stem.loc.top          = 0;
    word->stem.loc.bottom       = 0;
    word->stem.loc.current_cache= 0;
    word->stem.loc.current_info = 0x10;  /* num=1, offset = 0    */
    word->stem.loc.status       = NJ_ST_SEARCH_NO_INIT;
    word->stem.loc.attr         = 0x00000000;
    for (i = 0; i < NJ_MAX_PHR_CONNECT; i++) {
        word->stem.loc.relation[i] = 0;
    }

    return 1;
}


/**
 * 辞書検索カーソルのステータスを強制的にNJ_ST_SEARCH_END_EXTにする
 *
 * @attention 不正な値(cursor=NULLなど)を指定した場合の動作は不定。
 *
 * @param[in,out] cursor   検索カーソル
 * @param[in]     dic_id   辞書ID
 *
 * @return 常に!0
 *
 */
NJ_INT16 njd_set_cursor_search_end(NJ_CURSOR *cursor, NJ_UINT16 dic_id) {
    if ((cursor != NULL) && (dic_id < NJ_MAX_DIC)) {
        cursor->loctset[dic_id].loct.status = NJ_ST_SEARCH_END_EXT;
    }
    return 1;
}


/**
 * NJ_DIC_INFO 構造体の全部メンバをクリアする
 *
 * @param[out] info     : NJ_DIC_INFO 構造体へのポインタ
 *
 * @return  常に!0
 */
NJ_INT16 njd_clear_dicinfo(NJ_DIC_INFO *info) {
    NJ_INT16 i;

    (info)->type                                = NJ_DIC_H_TYPE_NORMAL;
    (info)->limit                               = 0;
    (info)->dic_freq[NJ_MODE_TYPE_HENKAN].base  = 0;
    (info)->dic_freq[NJ_MODE_TYPE_HENKAN].high  = 0;
    (info)->dic_freq[NJ_MODE_TYPE_YOSOKU].base  = 0;
    (info)->dic_freq[NJ_MODE_TYPE_YOSOKU].high  = 0;
    (info)->dic_freq[NJ_MODE_TYPE_MORPHO].base  = 0;
    (info)->dic_freq[NJ_MODE_TYPE_MORPHO].high  = 0;
    (info)->handle                              = NULL;
    for (i = 0; i < NJ_MAX_EXT_AREA; i++) {
        (info)->ext_area[i] = NULL;
    }
    for (i = 0; i < NJ_MAX_ADDITIONAL_INFO; i++) {
        (info)->add_info[i] = NULL;
    }
    (info)->srhCache                            = NULL;

    return 1;
}


/**
 * NJ_DIC_INFO 構造体の設定内容をコピーする
 *
 * @param[out] dest_info    : NJ_DIC_INFO 構造体へのポインタ（コピー先）
 * @param[in] src_info      : NJ_DIC_INFO 構造体へのポインタ（コピー元）
 * @param[in] src_mode_type : コピー元の辞書頻度のタイプ
 *
 * @return  常に!0
 */
NJ_INT16 njd_copy_dicinfo(NJ_DIC_INFO *dest_info, NJ_DIC_INFO *src_info, NJ_INT16 src_mode_type) {
    NJ_INT16 i;

    (dest_info)->type                               = (src_info)->type;
    for (i = 0; i < NJ_MAX_EXT_AREA; i++) {
        (dest_info)->ext_area[i] = (src_info)->ext_area[i];
    }
    for (i = 0; i < NJ_MAX_ADDITIONAL_INFO; i++) {
        (dest_info)->add_info[i] = (src_info)->add_info[i];
    }
    (dest_info)->handle                             = (src_info)->handle;
    (dest_info)->dic_freq[NJ_MODE_TYPE_HENKAN].base = (src_info)->dic_freq[(src_mode_type)].base;
    (dest_info)->dic_freq[NJ_MODE_TYPE_HENKAN].high = (src_info)->dic_freq[(src_mode_type)].high;

    return 1;
}


/**
 * 付加情報文字列取得
 *
 * @param[in]   iwnn      解析情報クラス
 * @param[in]   result    処理結果
 * @param[in]   index     付加情報インデックス
 * @param[out]  add_info  文字列（領域は呼び元で用意する必要がある）
 * @param[in]   size      candidateのバイトサイズ
 *
 * @retval >0 取得した候補文字配列長
 * @retval <0 エラー
 */
NJ_INT32 njd_get_additional_info(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_INT8 index,
                                 NJ_CHAR *add_info, NJ_UINT32 size) {
    NJ_INT32  ret = 0, ret0;
    NJ_UINT32 dictype;
    NJ_UINT32 len1;
    NJ_UINT8  cont_num;
    NJ_RESULT now, next;
    NJ_PROGRAM_DIC_IF program_dic_operation;
    NJ_PROGRAM_DIC_MESSAGE prog_msg;
    NJ_DIC_FREQ dic_freq;
    NJ_UINT8   internal_idx;


    if (result->word.stem.loc.handle == NULL) {
        /* 
         * 本ロジックは通ることがない
         * NULL参照を防止のために本ロジックを入れる
         */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_ADDITIONAL_INFO, NJ_ERR_INVALID_RESULT); /*NCH*/
    }

    dic_freq.high = 0;
    dic_freq.base = 0;

    if (index == (-1)) {
        internal_idx = 0;
    } else {
        internal_idx = index;
    }

    /* 擬似辞書、通常辞書の切り替え */
    dictype = NJ_GET_DIC_TYPE_EX(result->word.stem.loc.type, result->word.stem.loc.handle);

    switch (dictype) {
    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書(AWnnタイプ)   */
    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書               */
        if (index == (-1)) {
            if (ADD_LEARN_FLG((NJ_UINT8*)result->word.stem.loc.add_info[0])) {
                /* 学習可能 */
            } else {
                /* 学習不可 */
                return 0; /*NCH_DEF*/
            }
        }
        ret = njd_t_get_additional_info(iwnn, &result->word, internal_idx, add_info, size);
        if (ret < 0) {
            return ret;
        }
        len1 = nj_strlen(add_info);
        if ((index != (-1)) && (NJ_GET_TYPE_FROM_STEM(&result->word) == NJ_TYPE_UNDEFINE) &&
            ((GET_LOCATION_OPERATION(result->word.stem.loc.status) == NJ_CUR_OP_COMP) ||
             (GET_LOCATION_OPERATION(result->word.stem.loc.status) == NJ_CUR_OP_FORE) ||
             (GET_LOCATION_OPERATION(result->word.stem.loc.status) == NJ_CUR_OP_COMP_EXT) ||
             (GET_LOCATION_OPERATION(result->word.stem.loc.status) == NJ_CUR_OP_FORE_EXT))) {
            cont_num = (NJ_UINT8)(result->word.stem.loc.current_info >> 4) & 0x0F;
            if (cont_num > 1) {
                now = *result; /*NCH_DEF*/
                while (--cont_num > 0) { /*NCH_DEF*/
                    ret0 = njd_get_relational_word(iwnn, &now, &next, &dic_freq); /*NCH_DEF*/
                    if (ret0 < 0) { /*NCH_DEF*/
                        return ret0; /*NCH_FB*/
                    }
                    
                    ret0 = njd_t_get_additional_info(iwnn, &next.word, internal_idx, add_info + len1, (NJ_UINT16)(size - len1*sizeof(NJ_CHAR))); /*NCH_DEF*/
                    if (ret0 < 0) { /*NCH_DEF*/
                        return ret0; /*NCH_FB*/
                    }
                    /*
                     * ２回目のnjd_get_candidate()の戻り値は
                     * 実際の格納した表記文字列長が返される。
                     */
                    len1 += ret0; /*NCH_DEF*/
                    now = next; /*NCH_DEF*/
                }
            }
            ret = len1;
        }
        break;

    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
    case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
    case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
        if (index == (-1)) {
            if (ADD_LEARN_FLG((NJ_UINT8*)result->word.stem.loc.add_info[0])) {
                /* 学習可能 */
            } else {
                /* 学習不可 */
                return 0; /*NCH_FB*/
            }
        }
        ret = njd_s_get_additional_info(iwnn, &result->word, internal_idx, add_info, size);
        if (ret < 0) {
            return ret;
        }
        len1 = nj_strlen(add_info);
        if ((index != (-1)) && (NJ_GET_TYPE_FROM_STEM(&result->word) == NJ_TYPE_UNDEFINE) &&
            ((GET_LOCATION_OPERATION(result->word.stem.loc.status) == NJ_CUR_OP_COMP) ||
             (GET_LOCATION_OPERATION(result->word.stem.loc.status) == NJ_CUR_OP_FORE) ||
             (GET_LOCATION_OPERATION(result->word.stem.loc.status) == NJ_CUR_OP_COMP_EXT) ||
             (GET_LOCATION_OPERATION(result->word.stem.loc.status) == NJ_CUR_OP_FORE_EXT))) {
            cont_num = (NJ_UINT8)(result->word.stem.loc.current_info >> 4) & 0x0F;
            if (cont_num > 1) {
                now = *result; /*NCH_FB*/
                while (--cont_num > 0) { /*NCH_FB*/
                    ret0 = njd_get_relational_word(iwnn, &now, &next, &dic_freq); /*NCH_FB*/
                    if (ret0 < 0) { /*NCH_FB*/
                        return ret0; /*NCH_FB*/
                    }
                    
                    ret0 = njd_s_get_additional_info(iwnn, &next.word, internal_idx, add_info + len1, (NJ_UINT16)(size - len1*sizeof(NJ_CHAR))); /*NCH_FB*/
                    if (ret0 < 0) { /*NCH_FB*/
                        return ret0; /*NCH_FB*/
                    }
                    /*
                     * ２回目のnjd_get_candidate()の戻り値は
                     * 実際の格納した表記文字列長が返される。
                     */
                    len1 += ret0; /*NCH_FB*/
                    now = next; /*NCH_FB*/
                }
            }
            ret = len1;
        }
        break;

    case NJ_DIC_TYPE_LEARN:                     /* 学習辞書               */
    case NJ_DIC_TYPE_USER:                      /* ユーザ辞書             */
        if (internal_idx != 0) {
            return 0;
        }
        ret = njd_l_get_additional_info(iwnn, &result->word, internal_idx, add_info, size);
        if (ret < 0) {
            return ret;
        }
        len1 = nj_strlen(add_info);
        if ((index != (-1)) && (NJ_GET_TYPE_FROM_STEM(&result->word) == NJ_TYPE_UNDEFINE) &&
            (GET_LOCATION_OPERATION(result->word.stem.loc.status) == NJ_CUR_OP_COMP)) {
            cont_num = (NJ_UINT8)(result->word.stem.loc.current_info >> 4) & 0x0F;
            if (cont_num > 1) {
                now = *result;
                while (--cont_num > 0) {
                    ret0 = njd_get_relational_word(iwnn, &now, &next, &dic_freq);
                    if (ret0 < 0) {
                        return ret0; /*NCH_FB*/
                    }
                    
                    ret0 = njd_l_get_additional_info(iwnn, &next.word, internal_idx, add_info + len1, (NJ_UINT16)(size - len1*sizeof(NJ_CHAR)));
                    if (ret0 < 0) {
                        return ret0; /*NCH_FB*/
                    }
                    /*
                     * ２回目のnjd_get_candidate()の戻り値は
                     * 実際の格納した表記文字列長が返される。
                     */
                    len1 += ret0;
                    now = next;
                }
            }
            ret = len1;
        }
        break;

    case NJ_DIC_TYPE_EXT_YOMINASI:              /* 拡張読み無し予測辞書     */
        if (index == (-1)) {
            if (ADD_LEARN_FLG((NJ_UINT8*)result->word.stem.loc.add_info[0])) { /*NCH_DEF*/
                /* 学習可能 */
            } else {
                /* 学習不可 */
                return 0; /*NCH_DEF*/
            }
        }
        ret = njd_p_get_additional_info(iwnn, &result->word, internal_idx, add_info, size);
        break;

    case NJ_DIC_TYPE_PROGRAM:                   /* 擬似辞書        */
    case NJ_DIC_TYPE_PROGRAM_FZK:               /* 擬似辞書-付属語-*/
        /* 擬似辞書呼出*/
        /* 関数ポインタの設定 */
        program_dic_operation = (NJ_PROGRAM_DIC_IF)(result->word.stem.loc.handle);
        /* 擬似辞書メッセージを設定 */
        njd_init_program_dic_message(&prog_msg);
        prog_msg.word = &result->word;
        prog_msg.additional = add_info;
        prog_msg.additional_size = size;
        prog_msg.dic_idx = (NJ_UINT16)internal_idx;
        ret = (*program_dic_operation)(iwnn, NJG_OP_GET_ADDITIONAL, &prog_msg);
        if (ret < 0) {
            return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, GET_ERR_FUNCVAL(ret)); /*NCH_DEF*/
        }
        break;


    default:
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_GET_ADDITIONAL_INFO, NJ_ERR_DIC_TYPE_INVALID); /*NCH*/
    }
    return ret;
}
