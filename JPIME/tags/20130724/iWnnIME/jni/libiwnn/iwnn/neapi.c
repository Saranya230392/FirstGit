/**
 * @file
 *   共通部 API部
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

/************************************************/
/*              define  宣  言                  */
/************************************************/
#define NJ_DEFAULT_STATE_CALC_PARAMETER default_state_calc_parameter;

/************************************************/
/*              extern  宣  言                  */
/************************************************/

/************************************************/
/*              prototype  宣  言               */
/************************************************/
static NJ_INT16 set_previous_selection(NJ_CLASS *iwnn, NJ_RESULT *result);
static NJ_INT16 set_learn_word_info(NJ_CLASS *iwnn, NJ_LEARN_WORD_INFO *lword, NJ_RESULT *result);
static NJ_INT16 nje_change_state(NJ_CLASS *iwnn, NJ_UINT32 state);
static NJ_INT16 check_state_calc_parameter(NJ_STATE_CALC_PARAMETER *calc_parameter);

/************************************************/
/*              構 造 体 宣 言                  */
/************************************************/

/************************************************/
/*              外  部  変  数                  */
/************************************************/

/************************************************/
/*              static  変数宣言                */
/************************************************/

/**
 * 学習API
 *
 * 候補選択・学習を行う
 *
 * @param[in,out]  iwnn      解析情報クラス
 * @param[in]      l_result  選択された候補(学習辞書への学習用)
 * @param[in]      r_result  選択された候補(前確定情報設定用)
 * @param[in]      connect   ひとつ前に選択した候補との関係情報を作成するかのフラグ。<br>
 *                            0: 関係情報を作成しない<br>
 *                            1: 関係情報を作成する
 *
 * @retval >=0 正常終了
 * @retval <0  エラー
 *
 * @attention  エラーの場合は、確定情報は保持されない。
 */
NJ_EXTERN NJ_INT16 njx_select(NJ_CLASS *iwnn, NJ_RESULT *l_result, NJ_RESULT *r_result, NJ_UINT8 connect) {
    NJ_INT16 ret;
    NJ_UINT16 size;     /* njf_get_keep_result()の戻り値として使用 */
    NJ_LEARN_WORD_INFO *lword;
    NJ_LEARN_WORD_INFO learn_word;
    NJ_LEARN_WORD_INFO save_learn_word;
    NJ_UINT8 undo_flag = 1;
    NJ_UINT16 operation_id;
    NJ_DIC_SET *dics;
    NJ_UINT16 func_type;
    NJ_UINT8 *attr;
    NJ_WORD_INFO word_info;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SELECT, NJ_ERR_PARAM_ENV_NULL);
    }
    dics = &(iwnn->dic_set);

    if (dics->rHandle[NJ_MODE_TYPE_HENKAN] == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SELECT, NJ_ERR_NO_RULEDIC);
    }
    if (dics->rHandle[NJ_MODE_TYPE_YOSOKU] == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SELECT, NJ_ERR_NO_RULEDIC); /*NCH_MB*/
    }
    if (connect > 1) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SELECT, NJ_ERR_INVALID_FLAG);
    }

    if ((l_result != NULL) && (NJ_GET_RESULT_OP(l_result->operation_id) == NJ_OP_MORPHOLIZE)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SELECT, NJ_ERR_CANNOT_USE_MORPHOLIZE);
    }

    if ((r_result != NULL) && (NJ_GET_RESULT_OP(r_result->operation_id) == NJ_OP_MORPHOLIZE)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SELECT, NJ_ERR_CANNOT_USE_MORPHOLIZE); /*NCH_MB*/
    }
    /* 頻度学習カウンタ のカウントアップ */
    iwnn->iwnn_now++;

    if (connect == 0) {
        /* 前確定候補との関係性がない場合は、前確定情報をクリアする。 */
        set_previous_selection(iwnn, NULL);
    }

    /* 頻度学習の最適化チェック */
    ret = njd_optimize_ext_area(iwnn);
    if (ret< 0) {
        return ret; /*NCH*/
    }

    /* 学習辞書への学習用の候補が設定されている時 */
    if ( l_result != NULL ) {
        size = 1;        /* 普通は、処理結果は1つ */
        /* operation_idを待避しておく   */
        operation_id = l_result->operation_id;

        /* 複数文節処理結果なら、その実体を取得する */
        if (NJ_GET_RESULT_OP(l_result->operation_id) == NJ_OP_ANALYZE) {
            func_type = NJ_GET_RESULT_FUNC(l_result->operation_id);
            if ((func_type == NJ_FUNC_CONVERT_MULTIPLE) ||
                (func_type == NJ_FUNC_CONVERT_SINGLE)) {
                if (func_type == NJ_FUNC_CONVERT_MULTIPLE) {
                    size = njf_get_keep_result(iwnn, 0, &l_result);
                } else {
                    size = njf_get_keep_result(iwnn, 1, &l_result);
                }

                for (ret = 0; ret < (NJ_INT16)size; ret++) {
                    (l_result + ret)->operation_id &= ~ NJ_OP_MASK;
                    (l_result + ret)->operation_id |= NJ_OP_CONVERT;
                }
            }
        }

        /* 学習に必要なため、学習辞書用エリアを使用する */
        lword = &(learn_word);
        while (size-- > 0) {

            /* 活用形変形データが設定されている場合のみ実行 */
            if (iwnn->option_data.conjugation_data != NULL) {
                /* NJ_WORD_INFOを作成し、活用形変形タイプの過去形情報を取得する */
                ret = njd_get_word_info(iwnn, l_result, &word_info);
                word_info.connect = connect;
                if (njx_get_conjugation_type(iwnn, &word_info) == NJ_CONJ_TYPE_PAST) {
                    /* エンディアン依存しないように1byte毎判定を行う */
                    attr = (NJ_UINT8 *)(&(l_result->word.stem.loc.attr));
                    attr++;
                    *attr |= 0x80;
                }
            }

            /* lword(学習辞書用エリア)にresultの情報を設定する */
            ret = set_learn_word_info(iwnn, lword, l_result);
            if (ret < 0) {
                return ret; /*NCH_FB*/
            }
            attr = (NJ_UINT8 *)(&(lword->attr));
            if (iwnn->state.system[NJ_CAT_FIELD_HEAD] > 0) {
                /* 文頭候補の場合、文頭属性をONにする */
                *attr |= 0x20; /* = (0x80U >> NJ_CAT_FIELD_HEAD) */
            } else {
                /* 文頭候補でない場合、文頭属性をOFFにする */
                *attr &= 0xDF; /* = ~(0x80U >> NJ_CAT_FIELD_HEAD); */
            }

            /* 学習辞書への登録処理にて変更されるため、コピーを取る */
            save_learn_word = learn_word;

            /* 学習辞書に対して自立語固定(0)で学習する */
            ret = njd_l_add_word(iwnn, lword, connect, 0, undo_flag, 1);

            if ((ret < 0) &&
                (NJ_GET_ERR_CODE(ret) != NJ_ERR_DIC_NOT_FOUND)) {
                return ret;
            }

            /* 学習辞書以外の学習処理を行う。 */
            ret = njd_learn_word(iwnn, l_result, &save_learn_word, connect, undo_flag, 1);
            if (ret < 0) {
                return ret;
            }

            /* 学習単語により状況設定値を変更 */
            ret = nje_change_state(iwnn, lword->attr);
            if (ret < 0) {
                return ret; /*NCH*/
            }

            undo_flag = 0;
            connect = 1; /* 複数文節結果がある時は繋げて学習させる */
            l_result++;
        }

        if (NJ_GET_RESULT_OP(operation_id) == NJ_OP_SEARCH) {
            /* 辞書引き部のresultだった場合 */
            /* 同読みの学習情報が制限数を超えていたら削除する */
            ret = njd_l_make_space(iwnn, NJ_MAX_PHRASE, 0);
            if (ret < 0) {
                return ret; /*NCH_FB*/
            }
        }
    }

    /* 前確定情報設定用の候補が設定されている時 */
    if ( r_result != NULL ) {
        size = 1;        /* 普通は、処理結果は1つ */

        /* 複数文節処理結果なら、その実体を取得する */
        /* 複数文節処理結果なら、その実体を取得する */
        if (NJ_GET_RESULT_OP(r_result->operation_id) == NJ_OP_ANALYZE) {
            func_type = NJ_GET_RESULT_FUNC(r_result->operation_id);
            if ((func_type == NJ_FUNC_CONVERT_MULTIPLE) ||
                (func_type == NJ_FUNC_CONVERT_SINGLE)) {
                if (func_type == NJ_FUNC_CONVERT_MULTIPLE) {
                    size = njf_get_keep_result(iwnn, 0, &r_result);
                } else {
                    size = njf_get_keep_result(iwnn, 1, &r_result);
                }

                /* ループカウンタとして ret を流用し、
                   評価部の処理結果であったことを変換部の処理結果に
                   変更する */
                for (ret = 0; ret < (NJ_INT16)size; ret++) {
                    (r_result + ret)->operation_id &= ~ NJ_OP_MASK;
                    (r_result + ret)->operation_id |= NJ_OP_CONVERT;
                }
            }
        }

        while (size-- > 0) {

            /* 前確定情報にresultの情報を設定する */
            ret = set_previous_selection(iwnn, r_result);
            if (ret < 0) {
                return ret; /*NCH_FB*/
            }
            r_result++;
        }
    } else {
        /* 前確定情報をクリア */
        set_previous_selection(iwnn, NULL);
    }

    /* 複合語擬似辞書バッファ初期化処理 */
    njd_l_init_cmpdg_info(iwnn);

    return 0;   /* 正常終了 */
}


/**
 * 初期化API 
 *
 * 変換エンジンの揮発領域の初期化を行う。
 *
 * @param[in,out] iwnn    解析情報クラス
 * @param[in]     option  オプション設定情報
 *
 * @retval             0  正常終了
 * @retval            <0  エラー
 */
NJ_EXTERN NJ_INT16 njx_init(NJ_CLASS *iwnn, NJ_OPTION *option) {
    NJ_INT16 ret;
    NJ_INT16 i;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラーとする */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_INIT, NJ_ERR_PARAM_ENV_NULL);
    }


    /***********************************/
    /*         揮発領域の初期化        */
    /***********************************/
    /* オプション設定の初期化 */
    ret = njx_set_option(iwnn, option);
    if (ret< 0) {
        return ret; /*NCH*/
    }

    /* 前確定情報の初期化 */
    set_previous_selection(iwnn, NULL);

    /* 複合語擬似辞書バッファ初期化処理 */
    njd_l_init_cmpdg_info(iwnn);

    /* 読み取得情報の初期化 */
    iwnn->mm_yomi_search_index = 0;
    iwnn->mm_yomi_fzk_len = 0;

    /* 評価部：揮発領域の初期化 : 必ず0が返るので戻り値は無視 */
    njf_init_analyze(iwnn);

    /* 変換部：揮発領域の初期化 : 必ず0が返るので戻り値は無視 */
    njc_init_conv(iwnn);

    /* 学習辞書操作中状態を非操作中へ */
    iwnn->learndic_status.commit_status = 0;
    /* 状況設定値を初期化 */
    for (i = 0; i < NJ_MAX_STATE; i++) {
        iwnn->state.system[i] = 0;
    }
    for (i = 0; i < NJ_MAX_EXT_STATE; i++) {
        iwnn->state.extension[i] = 0;
    }
    iwnn->state.calc_parameter = (NJ_STATE_CALC_PARAMETER*)&default_state_calc_parameter;

    /* 状況設定パラメータのターミネーターチェック */
    ret = check_state_calc_parameter(iwnn->state.calc_parameter);
    if (ret < 0) {
        return ret;
    }

    return 0;
}


/**
 * 学習アンドゥAPI
 *
 * 直前に選択された undo_count 分の候補の学習を取りやめる。
 *
 * @attention
 *   njx_selectでの学習時に上書きされた古い情報の回復は保証されない。
 *
 * @param[in,out]  iwnn        解析情報クラス
 * @param[in]      undo_count  学習を取りやめる回数。
 *
 * @retval >=0 アンドゥした回数
 * @retval <0  エラー
 */
NJ_EXTERN NJ_INT16 njx_undo(NJ_CLASS *iwnn, NJ_UINT16 undo_count) {
    NJ_DIC_INFO *dicinfo;
    NJ_INT16 ret;
    NJ_INT16 done_count = 0;
    NJ_UINT16 i;
    NJ_PROGRAM_DIC_IF      program_dic_operation;
    NJ_PROGRAM_DIC_MESSAGE prog_msg;
    NJG_LEARN_WORD word_info;
    NJ_UINT32 dic_type;
    NJ_UINT8 processed_learndic, processed_programdic;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_UNDO, NJ_ERR_PARAM_ENV_NULL);
    }


    /* Undoを実施した場合は、前確定情報もクリアする */
    set_previous_selection(iwnn, NULL);

    /* 複合語擬似辞書バッファ初期化処理 */
    njd_l_init_cmpdg_info(iwnn);

    ret = 0;
    processed_learndic = processed_programdic = 0;

    for (i = 0; i < NJ_MAX_DIC; i++) {
        /* 辞書情報を格納 */
        dicinfo = &(iwnn->dic_set.dic[i]);

        if (dicinfo->handle == NULL) {
            /* 辞書セットがマウントされていない */
            continue;
        }

        dic_type = NJ_GET_DIC_TYPE_EX(NJ_GET_DIC_INFO(dicinfo), dicinfo->handle);
        switch (dic_type) {
        case NJ_DIC_TYPE_PROGRAM:                    /* 擬似辞書              */
        case NJ_DIC_TYPE_PROGRAM_FZK:                /* 擬似辞書-付属語-      */
            /* 擬似辞書呼出 */
            /* 単語登録情報の設定 */
            word_info.f_hinsi = 0;
            word_info.b_hinsi = 0;
            word_info.connect = 0;
            word_info.undo    = undo_count;
            
            /* 関数ポインタの設定 */
            program_dic_operation = (NJ_PROGRAM_DIC_IF)(dicinfo->handle);

            /* 擬似辞書メッセージを設定 */
            njd_init_program_dic_message(&prog_msg);
            prog_msg.dicset = &(iwnn->dic_set);
            prog_msg.lword= &word_info;
            prog_msg.dic_idx = i;
            ret = (*program_dic_operation)(iwnn, NJG_OP_UNDO_LEARN, &prog_msg);
            if (ret < 0) {
                return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, GET_ERR_FUNCVAL(ret));
            }
            processed_programdic = !0;
            break;

        case NJ_DIC_TYPE_LEARN:                      /* 学習辞書              */
            if (!processed_learndic) {
                /* 学習辞書については、辞書リスト内で最初に現れた辞書だけを対象とする */
                ret = njd_l_undo_learn(iwnn, undo_count);
                if (ret >= 0) {
                    /* 擬似辞書がマウントされている場合retが上書きされるので
                       undoした回数を記憶しておく */
                    done_count = ret;
                }
                processed_learndic = !0;
            }
            break;

        default:
            /* undo できない辞書は無視する */
            break;
        }

        /* 辞書セット内の辞書を処理中にエラーが検出された場合は、即座にエラーリターンする */
        if (ret < 0) {
            break; /*NCH_FB*/
        }
    }

    /* 学習辞書または擬似辞書をまったく処理しなかった（＝辞書セットに設定されていなかった）場合はエラーとなる */
    if ((processed_learndic == 0) && (processed_programdic == 0)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_UNDO, NJ_ERR_DIC_NOT_FOUND);
    }

    /* 学習辞書と擬似辞書の両方がマウントされている際は学習辞書のundo回数を返す */
    return (ret < 0) ? ret : done_count;
}


/**
 * 指定された前確定情報を返す。
 *
 * 前確定情報が存在しない場合は、NULLが返る。
 * 結合前確定情報が生成できない場合は、NULLが返る
 * 引数が不正だった場合は、NULLが返る
 *
 * @param[in]  iwnn      解析情報クラス
 * @param[in]  count     結合前確定情報数
 * @param[in]  mode      前確定作成モード
 *
 * @retval !=NULL   前確定情報 (NJ_LEAEN_WORD_INFOへのポインタ)。
 * @retval NULL     前確定情報がない場合は、NULLが返る。
 */
NJ_LEARN_WORD_INFO *nje_get_previous_selection(NJ_CLASS *iwnn, NJ_INT32 count, NJ_UINT8 mode) {
    NJ_PREVIOUS_SELECTION_INFO *prev_info = &(iwnn->previous_selection);
    NJ_LEARN_WORD_INFO *ret_lword = &(iwnn->previous_selection.next_prediction_lword);
    NJ_LEARN_WORD_INFO *lword;
    NJ_INT32  i;
    NJ_INT32  prev_idx;
    NJ_INT16  ailen;
    NJ_UINT8  *ptr;
    NJ_UINT8 *p_attr;

    /* 引数countのチェック */
    if (count < 0) {
        /* countに負数が指定された場合 */
        return NULL; /*NCH*/
    }
    if (mode == NJ_PREVIOUS_SELECTION_AI) {
        ailen = 1;
    } else {
        ailen = 0;
    }
    /* 引数prev_info->countのチェック */
    if ((prev_info->count < count) ||
        (prev_info->count == 0) ||
        (prev_info->count > NJ_MAX_RELATION_SEGMENT)) {
        /* 保持している前確定情報件数より大きい値が設定された場合 */
        return NULL;
    }

    /* 直前の前確定情報位置を取得 */
    prev_idx = prev_info->selection_now;

    if (count > 0) {
        /*
         * countに0より大きい値が設定された場合
         * すなわち、複数の前確定情報を結合する必要がある場合
         */

        /* 添え字の位置を複数の前確定情報の先頭へ移動する */
        count--;
        if (count > prev_idx) {
            i = count - prev_idx;
            prev_idx = NJ_MAX_RELATION_SEGMENT - i;
        } else {
            prev_idx -= count;
        }

        /* 結合を行う先頭の前確定情報を代入する */
        *ret_lword = prev_info->selection_data[prev_idx];
        if (mode == NJ_PREVIOUS_SELECTION_AI) {
            /* 読み文字列の更新 */
            ptr = (NJ_UINT8*)&ret_lword->yomi[0];
#ifdef NJ_OPT_UTF16
            *ptr = 0x00;
            ptr++;
#endif /* NJ_OPT_UTF16 */
            *ptr= NJD_AIP_TOP_CODE;
            nj_strncpy(&(ret_lword->yomi[ailen]), prev_info->selection_data[prev_idx].yomi, prev_info->selection_data[prev_idx].yomi_len);
            ret_lword->yomi_len += ailen;
            ret_lword->yomi[ret_lword->yomi_len] = NJ_CHAR_NUL;
        }

        /* 前確定情報が1文節 or 2文節の場合 */
        if (count == 1) {
            /* 状況カテゴリ情報を設定する */
            ret_lword->attr = prev_info->selection_data[prev_idx].attr;
        } else if (count == 0) {
            /* 状況カテゴリ情報を設定する */
            ret_lword->attr = prev_info->selection_data[prev_idx].attr;
            p_attr = (NJ_UINT8 *)(&ret_lword->attr);
            *p_attr |= DEFAULT_STATE_BIT;
        }

        /* ２つ目以降の前確定情報の結合処理を行う */
        for (i = 0; i < count; i++) {
            /* 参照する前確定情報の添え字に位置を移動する */
            prev_idx++;
            if (prev_idx >= NJ_MAX_RELATION_SEGMENT) {
                prev_idx = 0;
            }

            lword = &(prev_info->selection_data[prev_idx]);
            /* 読み文字列長のチェックを行う */
            if ((ret_lword->yomi_len + lword->yomi_len) > NJ_MAX_LEN) {
                /* 読み文字列長オーバー */
                return NULL;
            }

            /* 表記文字列長のチェックを行う */
            if ((ret_lword->hyouki_len + lword->hyouki_len) > NJ_MAX_RESULT_LEN) {
                /* 表記文字列長オーバー */
                return NULL;
            }

            /* 後品詞の更新 */
            ret_lword->b_hinsi = lword->b_hinsi;

            /* 読み文字列の更新 */
            nj_strncpy(&(ret_lword->yomi[ret_lword->yomi_len]), lword->yomi, lword->yomi_len);
            ret_lword->yomi_len += lword->yomi_len;
            ret_lword->yomi[ret_lword->yomi_len] = NJ_CHAR_NUL;

            /* 表記文字列の更新 */
            nj_strncpy(&(ret_lword->hyouki[ret_lword->hyouki_len]), lword->hyouki, lword->hyouki_len);
            ret_lword->hyouki_len += lword->hyouki_len;
            ret_lword->hyouki[ret_lword->hyouki_len] = NJ_CHAR_NUL;

            /* 前確定情報が2文節の場合 */
            if (count == 1) {
                /*
                 * 直近の前確定情報状況カテゴリデータ4Byte目のデータ(01111110)を確認し、
                 * カテゴリが付与されている場合は、直近の前確定情報データへ置き換える。
                 */
                p_attr = (NJ_UINT8 *)&prev_info->selection_data[prev_idx].attr;
                p_attr += CAT_POS_DAILY_LIFE;
                if ((*p_attr & CHECK_DAILY_LIFE_BIT)) {
                    ret_lword->attr = 0;
                }
            }
        }
    } else {
        /*
         * count == 0 の場合
         * 直前の確定情報をコピーして渡す。
         */
        *ret_lword = prev_info->selection_data[prev_idx];
        /* 状況カテゴリ情報を設定する */
        ret_lword->attr = prev_info->selection_data[prev_idx].attr;
        p_attr = (NJ_UINT8 *)(&ret_lword->attr);
        *p_attr |= DEFAULT_STATE_BIT;
    }

    /* 読み文字列長のチェックを行う */
    if ((ret_lword->yomi_len) > NJ_MAX_LEN) {
        /* 読み文字列長オーバー */
        return NULL; /*NCH*/
    }

    /* 表記文字列長のチェックを行う */
    if ((ret_lword->hyouki_len) > NJ_MAX_RESULT_LEN) {
        /* 表記文字列長オーバー */
        return NULL; /*NCH*/
    }

    return ret_lword;
}


/**
 * 候補文字列取得API
 *
 * 指定した処理結果の候補文字列を取得する
 *
 * @param[in]  iwnn       解析情報クラス
 * @param[in]  result     処理結果
 * @param[out] buf        候補文字列の出力先
 * @param[in]  buf_size   bufのサイズ
 *
 * @retval >=0 取得できた文字配列長
 * @retval <0  エラー(resultがNULLの場合を含む)
 */
NJ_EXTERN NJ_INT16 njx_get_candidate(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_CHAR *buf, NJ_UINT16 buf_size) {
    NJ_INT16 ret;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラーとする */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_CANDIDATE, NJ_ERR_PARAM_ENV_NULL);
    }
    if (result == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_CANDIDATE, NJ_ERR_PARAM_RESULT_NULL);
    }

    if ((buf == NULL) || (buf_size == 0)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_CANDIDATE, NJ_ERR_BUFFER_NOT_ENOUGH);
    }


    switch (NJ_GET_RESULT_OP(result->operation_id)) {
    case NJ_OP_SEARCH:
        ret = njd_get_candidate(iwnn, result, buf, buf_size);
        break;

    case NJ_OP_CONVERT:
    case NJ_OP_ENV:
        ret = njc_get_candidate(iwnn, result, buf, buf_size);
        break;

    case NJ_OP_ANALYZE:
        ret = njf_get_candidate(iwnn, result, buf, buf_size);
        break;

    case NJ_OP_MORPHOLIZE:
        ret = njc_get_stroke(iwnn, result, buf, buf_size);
        break;

    default:
        /* オペレーションが異常 */
        ret = NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_CANDIDATE, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
        break;
    }

    return ret;
}


/**
 * 読み文字列取得API
 *
 * 指定した処理結果のよみ文字列を取得する
 *
 * @param[in]   iwnn      解析情報クラス
 * @param[in]   result    処理結果
 * @param[out]  buf       よみ文字列の出力先
 * @param[in]   buf_size  bufのサイズ（byte数）
 *
 * @retval >=0 取得できた文字の文字配列長
 * @retval <0  エラー(resultがNULLの場合を含む)
 */
NJ_EXTERN NJ_INT16 njx_get_stroke(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_CHAR *buf, NJ_UINT16 buf_size) {
    NJ_INT16 ret;
    NJ_UINT16 len1, len2;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラーとする */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STROKE, NJ_ERR_PARAM_ENV_NULL);
    }
    if (result == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STROKE, NJ_ERR_PARAM_RESULT_NULL);
    }

    if ((buf == NULL) || (buf_size == 0)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STROKE, NJ_ERR_BUFFER_NOT_ENOUGH);
    }


    switch (NJ_GET_RESULT_OP(result->operation_id)) {
    case NJ_OP_SEARCH:
        ret = njd_get_stroke(iwnn, result, buf, buf_size);
        break;

    case NJ_OP_CONVERT:
    case NJ_OP_ENV:
        ret = njc_get_stroke(iwnn, result, buf, buf_size);
        break;

    case NJ_OP_ANALYZE:
        ret = njf_get_stroke(iwnn, result, buf, buf_size);
        break;

    case NJ_OP_MORPHOLIZE:
        if (NJ_GET_RESULT_FUNC(result->operation_id) != NJ_FUNC_ZENKOUHO) {
            /* 全候補解析以外の場合、エラーとする */
            ret = NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STROKE, NJ_ERR_CANNOT_USE_MORPHOLIZE);
        } else {
            /* 全候補解析の場合 */
            if (NJ_GET_GIJI_BIT(result->operation_id)) {
                /* 擬似bitが付与されている場合 */
                /* ひらがなの場合 */
                if (result->word.stem.type == NJ_TYPE_HIRAGANA) {
                    len1 = NJ_GET_YLEN_FROM_STEM_EXT(&result->word);
                    nj_strncpy(buf, result->word.yomi, len1);
                    *(buf + len1) = NJ_CHAR_NUL;
                    ret = len1;

                /* 半角・全角カタカナの場合 */
                } else {
                    ret = nje_convert_kata_to_hira(result->word.yomi, buf,
                                                   (NJ_UINT16)NJ_GET_YLEN_FROM_STEM_EXT(&result->word), NJ_MAX_LEN, result->word.stem.type);
                }
            } else {
                /* 自立語の読み文字列を取得 */
                ret = njd_get_stroke(iwnn, result, buf, buf_size);
            }
            if (ret < 0) {
                /* エラー発生時は処理を抜ける */
                break; /*NCH*/
            }
            /* 付属語については、読み文字列を使用する */
            len1 = NJ_GET_YLEN_FROM_STEM_EXT(&result->word);
            len2 = NJ_GET_YLEN_FROM_FZK(&result->word);
            if (len2 > 0) { /* 付属語が存在 */
                nj_strncpy(buf + ret, result->word.yomi + len1, len2);
            }
            *(buf + ret + len2) = NJ_CHAR_NUL;
            ret += len2;
        }
        break;

    default:
        /* オペレーションが異常 */
        ret = NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STROKE, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
        break;
    }
    return ret;
}


/**
 * 文字種取得API
 *
 * 指定した単語の自立語・付属語の文字種を返す
 *
 * @param[in]  iwnn       解析情報クラス
 * @param[in]  result     処理結果
 * @param[out] stem_type  自立語文字種
 * @param[out] fzk_type   付属語文字種
 *
 * @retval 0  正常終了
 * @retval <0 エラー(resultがNULLの場合を含む)
 */
NJ_EXTERN NJ_INT16 njx_get_char_type(NJ_CLASS *iwnn, NJ_RESULT *result,
                                     NJ_UINT8 *stem_type, NJ_UINT8 *fzk_type) {

    NJ_INT16 ret;       /* 戻り値 */

    /* 候補の表記文字列取得用バッファ */
    NJ_CHAR   buf[MM_MAX_MORPHO_LEN + NJ_TERM_LEN];  /* 形態素解析結果用     */
    NJ_CHAR   buf2[NJ_MAX_RESULT_LEN + NJ_TERM_LEN]; /* 形態素解析以外結果用 */
    NJ_CHAR   *pbuf;    /* 表記文字列取得用バッファ先頭アドレス */
    NJ_UINT16 buf_size; /* 表記文字列取得用バッファバイトサイズ */

    NJ_UINT16 stem_len; /* 自立語文字配列長 */


    /*
     * 入力パラメータ チェック
     */
    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラーとする */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_CHAR_TYPE, NJ_ERR_PARAM_ENV_NULL);
    }
    if (result == NULL) {
        /* 第2引数(result)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_CHAR_TYPE, NJ_ERR_PARAM_RESULT_NULL); /*NCH_MB*/
    }


    /*
     * 表記文字列取得用バッファの決定
     */
    if (((NJ_GET_RESULT_FUNC(result->operation_id) == NJ_FUNC_SPLIT_WORD) ||
         (NJ_GET_RESULT_FUNC(result->operation_id) == NJ_FUNC_ZENKOUHO)) &&
        (NJ_GET_RESULT_OP(result->operation_id) == NJ_OP_MORPHOLIZE)) {
        /* 形態素解析の処理結果は、MM_MAX_MORPHO_LEN分の領域を使用 */
        pbuf = buf;
        buf_size = (MM_MAX_MORPHO_LEN + NJ_TERM_LEN)*sizeof(NJ_CHAR);
    } else {
        /* 形態素解析以外の処理結果は、MM_MAX_RESULT_LEN分の領域を使用 */
        pbuf = buf2;
        buf_size = (NJ_MAX_RESULT_LEN + NJ_TERM_LEN)*sizeof(NJ_CHAR);
    }

    /*
     * 処理結果の候補表記文字列を取得
     */
    ret = njx_get_candidate(iwnn, result, pbuf, buf_size);
    if (ret < 0) {
        /* njx_get_candidate 異常終了 */
        return ret; /*NCH_FB*/
    }

    if (NJ_GET_RESULT_OP(result->operation_id) == NJ_OP_ANALYZE) {
        /* 候補の全表記を自立語表記とする */
        stem_len = ret; /* 自立語文字配列長取得 */

    } else if ((NJ_GET_RESULT_FUNC(result->operation_id) == NJ_FUNC_SEARCH_R) ||
               (NJ_GET_RESULT_FUNC(result->operation_id) == NJ_FUNC_SPLIT_WORD) ||
               ((NJ_GET_RESULT_OP(result->operation_id) == NJ_OP_MORPHOLIZE) &&
                (NJ_GET_RESULT_FUNC(result->operation_id) == NJ_FUNC_ZENKOUHO))) {
        /*
         * 辞書引き（逆引き）と分かち書きの処理結果 
         * 処理結果(NJ_RESULT)から、自立語文字配列長を取得
         */
        stem_len = NJ_GET_YLEN_FROM_STEM(&(result->word));

    } else {
        /*
         * 上記以外の処理結果
         * 処理結果(NJ_RESULT)から、自立語文字配列長を取得
         */
        stem_len = NJ_GET_KLEN_FROM_STEM(&(result->word));
    }

    if (stem_type != NULL) {

        if (stem_len == 0) {
            /* 付属語が無い場合、NJ_TYPE_NONEとする */
            *stem_type = (NJ_UINT8)NJ_TYPE_NONE;
        } else {
            /**
             * 自立語の末尾をNULターミネートする。
             * 付属語の先頭文字をNUL文字とスワップする。
             */

            /* 自立語の文字種取得 */
            *stem_type = (NJ_UINT8)nje_check_string(pbuf, stem_len);

        }
    }

    if (fzk_type != NULL) {
        if (stem_len == (NJ_UINT16)ret) {
            /* 付属語が無い場合、NJ_TYPE_NONEとする */
            *fzk_type = (NJ_UINT8)NJ_TYPE_NONE;
        } else {
            /* 付属語の文字種取得 */
            *fzk_type = (NJ_UINT8)nje_check_string(pbuf + stem_len,
                                                   (NJ_UINT16)(ret - stem_len));
        }
    }

    /* 正常終了 */
    return 0;
}


/**
 * 選択された候補から前確定情報をセットする
 *
 * resultの情報をNJ_LEARN_WORD_INFOに設定する
 * resultがNULLの場合は、前確定情報をクリアする。
 *
 * @param[in,out] iwnn    解析情報クラス
 * @param[in]     result  選択された候補
 *
 * @retval >=0 正常終了
 * @retval <0  エラー
 *
 * @attention
 *  エラーの場合は、確定情報は保持されない。
 */
static NJ_INT16 set_previous_selection(NJ_CLASS *iwnn, NJ_RESULT *result) {
    NJ_UINT16  i;
    NJ_INT16   ret;
    NJ_PREVIOUS_SELECTION_INFO *prev_info = &(iwnn->previous_selection);
    NJD_AIP_WORK *aipwork;


    if (result == NULL) {
        prev_info->count = 0;
        prev_info->selection_now = (NJ_UINT8)(NJ_MAX_RELATION_SEGMENT - 1);

        for (i = 0; i < NJ_MAX_DIC; i++) {
            iwnn->prev_search_range[i][NJ_SEARCH_RANGE_TOP] = 0;
            iwnn->prev_search_range[i][NJ_SEARCH_RANGE_BOTTOM] = 0;
        }
        iwnn->relation_results_count = 0;

        aipwork = (NJD_AIP_WORK*)iwnn->option_data.aip_work;
        if (aipwork != NULL) {
            /* AI予測用ワーク領域の初期化 */
            aipwork->save_cnt = 0;
            aipwork->status = 0;
        }
    } else {
        /*
         * resultの中の情報を、前確定情報に格納する
         */
        /* 直前の前確定情報位置を進める */
        prev_info->selection_now++;
        if (prev_info->selection_now >= NJ_MAX_RELATION_SEGMENT) {
            /* 前確定情報位置が配列の終端を指している場合は、先頭を指すように修正 */
            prev_info->selection_now = 0;
        }
        /* resultの内容を、前確定情報に格納する */
        ret = set_learn_word_info(iwnn, &(prev_info->selection_data[prev_info->selection_now]), result);
        if (ret < 0) {
            /* エラー発生 */
            return ret; /*NCH_FB*/
        }

        /* 前確定情報の保持件数を更新する */
        if (prev_info->count < NJ_MAX_RELATION_SEGMENT) {
            /* 最大保持件数に達していない場合のみ更新を行う */
            prev_info->count++;
        }
    }

    return 0;
}


/**
 * 選択された候補から学習単語情報を作成する
 *
 * resultの情報をNJ_LEARN_WORD_INFOに設定する
 *
 * @param[in,out] iwnn    解析情報クラス
 * @param[out]    lword   学習単語情報
 * @param[in]     result  選択された候補
 *
 * @retval >=0 正常終了
 * @retval <0  エラー
 *
 * @attention
 *  エラーの場合は、学習単語情報は保持されない。
 */
static NJ_INT16 set_learn_word_info(NJ_CLASS *iwnn, NJ_LEARN_WORD_INFO *lword, NJ_RESULT *result) {
    NJ_INT16 ret;
    NJ_INT32 ret32;
    NJ_DIC_SET *dics = &(iwnn->dic_set);
    NJ_UINT8  fzk_len;



    /* 付加情報文字列を lwordに追加する */
    ret32 = njx_get_additional_info(iwnn, result, (-1), lword->additional, sizeof(lword->additional));
    if (ret32 < 0) {
        return (NJ_INT16)ret32; /*NCH_FB*/
    }
    lword->additional_len = (NJ_UINT8)ret32;

    /* よみ文字列、表記文字列を lword に追加する */
    ret = njx_get_stroke(iwnn, result, lword->yomi, sizeof(lword->yomi));
    if (ret < 0) {
        return ret; /*NCH_FB*/
    }
    lword->yomi_len = (NJ_UINT8)ret;
    ret = njx_get_candidate(iwnn, result, lword->hyouki, sizeof(lword->hyouki));
    if (ret < 0) {
        return ret; /*NCH_FB*/
    }
    lword->hyouki_len = (NJ_UINT8)ret;

    /* 属性データを格納 */
    lword->attr = result->word.stem.loc.attr;

    lword->f_hinsi = NJ_GET_FPOS_FROM_STEM(&(result->word));
    lword->stem_b_hinsi = NJ_GET_BPOS_FROM_STEM(&(result->word));

    if ((lword->fzk_yomi_len = NJ_GET_YLEN_FROM_FZK(&(result->word))) == 0) {
        /* 付属語無し */
        lword->b_hinsi = NJ_GET_BPOS_FROM_STEM(&(result->word));
    } else {
        lword->b_hinsi = NJ_GET_BPOS_FROM_FZK(&(result->word));
    }

    /* 前品詞が単漢字であれば、普通名詞(NJ_HINSI_CHIMEI_F)に変更する */
    ret = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_TANKANJI_F);
    if ((ret != 0) && (lword->f_hinsi == (NJ_UINT16)ret)) {
        ret = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_CHIMEI_F);
        if (ret != 0) {
            lword->f_hinsi = (NJ_UINT16)ret;
        }
    }

    /* 後品詞が単漢字であれば、普通名詞(NJ_HINSI_CHIMEI_B)に変更する */
    ret = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_TANKANJI_B);
    if ((ret != 0) && (lword->b_hinsi == (NJ_UINT16)ret)) {
        ret = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_CHIMEI_B);
        if (ret != 0) {
            lword->b_hinsi = (NJ_UINT16)ret;
        }
    }

    /* 自立語部分の後品詞が単漢字であれば、普通名詞(NJ_HINSI_CHIMEI_B)に変更する */
    ret = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_TANKANJI_B);
    if ((ret != 0) && (lword->stem_b_hinsi == (NJ_UINT16)ret)) {
        ret = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_CHIMEI_B);
        if (ret != 0) {
            lword->stem_b_hinsi = (NJ_UINT16)ret;
        }
    }

    if (((result->word.stem.loc.current_info & 0xF0) == 0x10) &&
        (result->word.stem.type == 0) &&
        (result->word.stem.loc.handle != NULL) &&
        (NJ_GET_DIC_TYPE_EX(result->word.stem.loc.type,
                            result->word.stem.loc.handle) == NJ_DIC_TYPE_LEARN)) {
        /* iWnnタイプの学習辞書のみ情報の補正を行う。*/
        if (njd_l_get_ext_word_data(iwnn, &result->word, &lword->stem_b_hinsi, &fzk_len)) {
            lword->fzk_yomi_len += fzk_len;
        }
    }

    return 0;

}


/**
 * オプション設定API
 *
 * オプション設定を設定する。
 *
 * @param[in,out] iwnn    解析情報クラス
 * @param[in] option      オプション設定構造体
 *
 * @retval 1 設定成功
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njx_set_option(NJ_CLASS *iwnn, NJ_OPTION *option) {


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_OPTION, NJ_ERR_PARAM_ENV_NULL);
    }

    if (option == NULL) {
        /* デフォルト値を設定 */
        /* 連文節変換自動開始候補数 */
        iwnn->option_data.autoconv_cnt   = NJ_OPT_AUTOCONV_CNT;
        iwnn->option_data.ext_mode       = 0x0000;
        iwnn->option_data.phase3_filter  = NULL;
        iwnn->option_data.phase3_option  = NULL;
        iwnn->option_data.phase2_filter  = NULL;
        iwnn->option_data.phase2_option  = NULL;
        iwnn->option_data.phase1_filter  = NULL;
        iwnn->option_data.phase1_option  = NULL;
        iwnn->option_data.aip_work   = NULL;
        
    } else {
        /* オプション設定の更新 */
        iwnn->option_data              = *option;
        if (/*(iwnn->option_data.autoconv_cnt < 0) && */
            (iwnn->option_data.autoconv_cnt > NJ_MAX_CANDIDATE)) {
            /* autoconv_cnt は、NJ_UINT16型のためマイナス値にはならない */
            /* 連文節変換自動開始候補数をセット */
            iwnn->option_data.autoconv_cnt = NJ_OPT_AUTOCONV_CNT;
        }
    }


    return 1;

}


/**
 * 状況設定API
 *
 * 状況設定をセットする。
 *
 * @param[in,out] iwnn     解析情報クラス
 * @param[in] state        状況設定構造体
 *
 * @retval 1 設定成功
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njx_set_state(NJ_CLASS *iwnn, NJ_STATE *state) {
    NJ_UINT32 i, j;
    NJ_INT16 ret;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STATE, NJ_ERR_PARAM_ENV_NULL);
    }

    if (state != NULL) {
        for (i = 0; i < NJ_MAX_STATE; i++) {
            if ((state->system[i] < NJ_STATE_MIN_BIAS) ||
                (state->system[i] > NJ_STATE_MAX_BIAS)) {
                /* 第2引数(state)の設定値が不正の場合はエラー */
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STATE, NJ_ERR_PARAM_INVALID_STATE);
            }
        }
        for (i = 0; i < NJ_MAX_EXT_STATE; i++) {
            if ((state->extension[i] < NJ_STATE_MIN_BIAS) ||
                (state->extension[i] > NJ_STATE_MAX_BIAS)) {
                /* 第2引数(state)の設定値が不正の場合はエラー */
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STATE, NJ_ERR_PARAM_INVALID_STATE);
            }
        }
        /* 状況操作設定のチェック */
        if ((state->calc_parameter) != NULL) {
            for (i = 0; i < NJ_MAX_STATE; i++) {
                if ((state->calc_parameter->system_max_bias[i] < NJ_STATE_MIN_BIAS) ||
                    (state->calc_parameter->system_max_bias[i] > NJ_STATE_MAX_BIAS)) {
                    /* 第2引数(state)の設定値が不正の場合はエラー */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STATE, NJ_ERR_PARAM_INVALID_STATE);
                }
                if ((state->calc_parameter->system_min_bias[i] < NJ_STATE_MIN_BIAS) ||
                    (state->calc_parameter->system_min_bias[i] > NJ_STATE_MAX_BIAS)) {
                    /* 第2引数(state)の設定値が不正の場合はエラー */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STATE, NJ_ERR_PARAM_INVALID_STATE);
                }
                if (state->calc_parameter->system_min_bias[i] > state->calc_parameter->system_max_bias[i]) {
                    /* 第2引数(state)の設定値が不正の場合はエラー */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STATE, NJ_ERR_PARAM_INVALID_STATE); /*NCH_MB*/
                }
                if ((state->calc_parameter->system_add_bias[i] < NJ_STATE_MIN_BIAS) ||
                    (state->calc_parameter->system_add_bias[i] > NJ_STATE_MAX_BIAS)) {
                    /* 第2引数(state)の設定値が不正の場合はエラー */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STATE, NJ_ERR_PARAM_INVALID_STATE);
                }
                if ((state->calc_parameter->system_subtract_bias[i] < NJ_STATE_MIN_BIAS) ||
                    (state->calc_parameter->system_subtract_bias[i] > NJ_STATE_MAX_BIAS)) {
                    /* 第2引数(state)の設定値が不正の場合はエラー */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STATE, NJ_ERR_PARAM_INVALID_STATE);
                }
                if ((state->calc_parameter->system_base_bias[i] < NJ_STATE_MIN_BIAS) ||
                    (state->calc_parameter->system_base_bias[i] > NJ_STATE_MAX_BIAS)) {
                    /* 第2引数(state)の設定値が不正の場合はエラー */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STATE, NJ_ERR_PARAM_INVALID_STATE);
                }
                for (j = 0; j < NJ_MAX_STATE; j++) {
                    if ((state->calc_parameter->system_change_bias[i][j] < NJ_STATE_MIN_BIAS) ||
                        (state->calc_parameter->system_change_bias[i][j] > NJ_STATE_MAX_BIAS)) {
                        /* 第2引数(state)の設定値が不正の場合はエラー */
                        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STATE, NJ_ERR_PARAM_INVALID_STATE);
                    }
                }
            }
            for (i = 0; i < NJ_MAX_DIC; i++) {
                if ((state->calc_parameter->dicinfo_max[i] < NJ_STATE_MIN_FREQ) ||
                    (state->calc_parameter->dicinfo_max[i] > NJ_STATE_MAX_FREQ)) {
                    /* 第2引数(state)の設定値が不正の場合はエラー */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STATE, NJ_ERR_PARAM_INVALID_STATE);
                }
                if ((state->calc_parameter->dicinfo_min[i] < NJ_STATE_MIN_FREQ) ||
                    (state->calc_parameter->dicinfo_min[i] > NJ_STATE_MAX_FREQ)) {
                    /* 第2引数(state)の設定値が不正の場合はエラー */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STATE, NJ_ERR_PARAM_INVALID_STATE);
                }
            }
        }

        /* 状況設定の更新 */
        iwnn->state = *state;

        if ((state->calc_parameter) == NULL) {
            /* 状況操作設定がNULLの場合は、デフォルト値を設定 */
            iwnn->state.calc_parameter = (NJ_STATE_CALC_PARAMETER*)&default_state_calc_parameter;
        }
    } else {
        /*
         * NULL指定の場合は、初期化を行う
         */
        for (i = 0; i < NJ_MAX_STATE; i++) {
            iwnn->state.system[i] = 0;
        }
        for (i = 0; i < NJ_MAX_EXT_STATE; i++) {
            iwnn->state.extension[i] = 0;
        }
        iwnn->state.calc_parameter = (NJ_STATE_CALC_PARAMETER*)&default_state_calc_parameter;
    }

    /* 状況設定パラメータのターミネーターチェック */
    ret = check_state_calc_parameter(iwnn->state.calc_parameter);
    if (ret < 0) {
        return ret;
    }

    return 1;
}


/**
 * 状況設定取得API
 *
 * 状況設定を取得する。
 *
 * @param[in,out] iwnn     解析情報クラス
 * @param[out] state       状況設定構造体
 *
 * @retval 1 設定成功
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njx_get_state(NJ_CLASS *iwnn, NJ_STATE *state) {


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STATE, NJ_ERR_PARAM_ENV_NULL);
    }

    if (state == NULL) {
        /* 第2引数(state)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STATE, NJ_ERR_PARAM_STATE_NULL);
    }


    /* 状況設定のコピー */
    *state = iwnn->state;

    return 1;
}


/**
 * 候補リストマージAPI
 *
 * 複数の候補リストをまとめて、１つの候補リストを作成する。
 *
 * @param[in,out] word_list 候補リスト
 * @param[in] list_max      最大格納候補数
 * @param[in] mode          マージ方法
 * @param[in] iwnn          解析情報クラス
 * @param[in] result        マージする候補配列
 * @param[in] num           マージする候補数
 *
 * @retval >=0 格納候補数
 * @retval <0  エラー
 */
NJ_EXTERN NJ_INT16 njx_merge_word_list(NJ_M_RESULT *word_list, NJ_INT32 list_max, NJ_UINT32 mode,
                                       NJ_CLASS *iwnn, NJ_RESULT *result, NJ_INT32 num) {
    NJ_INT32 phase;
    NJ_INT32 i, j;
    NJ_INT32 total, total0;
    NJ_CHAR repr[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_CHAR *repr_list;
    NJ_M_RESULT *listp;
    NJ_M_RESULT tmp;
    NJ_INT16 ret;
    NJ_INT32 found;
    NJ_INT32 count;


    /* check arguments */
    if ((word_list == NULL) || (result == NULL)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MERGE_WORD_LIST, NJ_ERR_PARAM_RESULT_NULL);
    }
    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MERGE_WORD_LIST, NJ_ERR_PARAM_ENV_NULL);
    }
    if (list_max > NJ_MAX_CANDIDATE) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MERGE_WORD_LIST, NJ_ERR_BUFFER_NOT_ENOUGH);
    }
    if (num > list_max) {
        num = list_max;
    }

    if (mode == NJ_MERGE_INIT) {
        /*
         * word_listの先頭から、result[]で上書きし、残りをクリアする
         */
        total = 0;
        for (i = 0; i < num; i++) {
            word_list[i].iwnn   = iwnn;
            word_list[i].result = &result[i];
            total++;
        }
        for (i = num; i < list_max; i++) {
            word_list[i].iwnn   = NULL;
            word_list[i].result = NULL;
        }
    } else if ((mode == NJ_MERGE_NOT_EXIST) || (mode == NJ_MERGE_NOT_EXIST_FORCE)) {
        /*
         * word_listにないものだけ追加する
         */
        /* word_listの全表記をiwnn->h_buf.workbuf[]に作成 */
        total = 0;
        for (i = 0, listp = word_list; (i < list_max) && (listp->result != NULL); i++, listp++) {
            ret = njx_get_candidate(listp->iwnn, listp->result,
                                    &(iwnn->h_buf.workbuf.work[(NJ_MAX_RESULT_LEN + NJ_TERM_LEN) * i]),
                                    (NJ_MAX_RESULT_LEN + NJ_TERM_LEN) * sizeof(NJ_CHAR));
            if (ret < 0) {
                /* 表記文字列が取得できない場合 */
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MERGE_WORD_LIST, NJ_ERR_INVALID_RESULT);
            }
            total++;
        }
        total0 = total; /* リストにあらかじめあった候補数 */
        count = 0; /* 登録しきれなかった数 */
        /*
         * 強制追加モードを実現するため、同様の処理を２回ループする。
         * ループ不要の場合は、breakで抜ける。
         */
        for (phase = 0; phase < 2; phase++) {
            for (i = 0; i < num; i++) {
                ret = njx_get_candidate(iwnn, &result[i], repr, sizeof(repr));
                if (ret < 0) {
                    /* 表記文字列が取得できない場合 */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MERGE_WORD_LIST, NJ_ERR_INVALID_RESULT);
                }
                repr_list = iwnn->h_buf.workbuf.work;
                listp = word_list;
                found = 0;
                for (j = 0; (j < total0) && (listp->result != NULL); j++) {
                    /* 既存リストの候補と表記が一致するか確認する */
                    if (nj_strcmp(repr_list, repr) == 0) {
                        if (mode == NJ_MERGE_NOT_EXIST_FORCE) {
                            /* 強制追加の場合は、既にあった候補にマークを付ける */
                            listp->result->operation_id |= NJ_TYPE_MERGE_TMP_BIT;
                        }
                        found = 1;
                        break;
                    }
                    repr_list += (NJ_MAX_RESULT_LEN + NJ_TERM_LEN);
                    listp++;
                }
                if (!found) {
                    if (total >= list_max) {
                        /* リスト最大登録数を超えた場合 */
                        if ((mode == NJ_MERGE_NOT_EXIST) || (phase > 0)) {
                            /* 強制追加でない or phase>0なら終了 */
                            break;
                        } else {
                            /* 強制追加の場合は追加できなかった個数を数える */
                            count++;
                        }
                    } else {
                        /* リストに追加する */
                        word_list[total].iwnn = iwnn;
                        word_list[total].result = &result[i];
                        total++;
                    }
                }
            }
            if ((count > 0) && (mode == NJ_MERGE_NOT_EXIST_FORCE)) {
                /*
                 * 追加できなかった候補の登録処理(phase=1)に移る準備
                 */
                for (total0--; total0 >= 0; total0--) {
                    if (word_list[total0].result->operation_id & NJ_TYPE_MERGE_TMP_BIT) {
                        /*
                         * 重複マークがあった場合
                         * リストからresultにアクセスできなくなる前に重複マークを消しておく。
                         */
                        word_list[total0].result->operation_id &= (~NJ_TYPE_MERGE_TMP_BIT);
                    } else {
                        /* 重複マークが無かった場合 */
                        count--;
                        if (count <= 0) {
                            break;
                        }
                    }
                }
                total = total0;
            } else {
                /* 全部追加できた場合 or NJ_MERGE_NOT_EXISTモードのときは、次フェーズ不要 */
                break;
            }
        }

        /* 後処理 */
        if (mode == NJ_MERGE_NOT_EXIST_FORCE) {
            /* 重複チェック用マークを消す */
            for (i = 0, listp = word_list;
                 (i < list_max) && (listp->result != NULL); i++, listp++) {
                listp->result->operation_id &= (~NJ_TYPE_MERGE_TMP_BIT);
            }
        }

    } else if (mode == NJ_MERGE_FORCE) {
        /*
         * 必ず末尾に追加する
         */
        tmp.iwnn = NULL;
        tmp.result = NULL;
        /* result[]の全表記をiwnn->h_buf.workbuf.work[]に作成 */
        for (i = 0; i < num; i++) {
            j = (NJ_MAX_RESULT_LEN + NJ_TERM_LEN) * i;
            ret = njx_get_candidate(iwnn, &result[i],
                                    &(iwnn->h_buf.workbuf.work[j]),
                                    (NJ_MAX_RESULT_LEN + NJ_TERM_LEN) * sizeof(NJ_CHAR));
            if (ret < 0) {
                /* 表記文字列が取得できない場合 */
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MERGE_WORD_LIST, NJ_ERR_INVALID_RESULT);
            }
        }
        count = 0; /* 後ろに移動した数 */
        i = 0;
        listp = word_list;
        while ((i < (list_max - count)) && (listp->result != NULL)) {
            ret = njx_get_candidate(listp->iwnn, listp->result,
                                    repr, sizeof(repr));
            if (ret < 0) {
                /* 表記文字列が取得できない場合 */
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MERGE_WORD_LIST, NJ_ERR_INVALID_RESULT);
            }
            repr_list = iwnn->h_buf.workbuf.work;
            found = 0;
            for (j = 0; j < num; j++) {
                if (nj_strcmp(repr, repr_list) == 0) {
                    found = 1;
                    break;
                }
                repr_list += (NJ_MAX_RESULT_LEN + NJ_TERM_LEN);
            }
            if (found) {
                /* result[]と同じものがあれば、リストの末尾に移動(並び替え) */
                tmp = *listp;
                for (j = i + 1; j < list_max; j++) {
                    word_list[j-1] = word_list[j];
                }
                word_list[list_max - 1] = tmp;
                /* 並び替えで次候補位置がずれるのを補正 */
                count++;
            } else {
                i++;
                listp++;
            }
        }
        total = 0;
        for (i = 0, listp = word_list;
             (i < (list_max - num)) && (listp->result != NULL); i++, listp++) {
            total++;
        }

        /* 空白の先頭 or list_max - numから追加していく */
        repr_list = iwnn->h_buf.workbuf.work;
        for (i = 0; i < num; i++) {
            found = 0;
            for (j = 0; j < count; j++) {
                if (found) {
                    /*
                     * 一度見つかれば、移動した分を削除する処理を行う
                     * 現在位置の候補で、直後の候補を上書きする。
                     */
                    word_list[list_max - j] = word_list[list_max - 1 - j]; /*NCH*/
                } else {
                    ret = njx_get_candidate(word_list[list_max - 1 - j].iwnn,
                                            word_list[list_max - 1 - j].result,
                                            repr, sizeof(repr));
                    if (ret < 0) {
                        /* 表記文字列が取得できない場合 */
                        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MERGE_WORD_LIST,
                                              NJ_ERR_INVALID_RESULT);
                    }
                    if (nj_strcmp(repr, repr_list) == 0) {
                        /*
                         * 追加対象と表記が同じものがあった場合
                         * データを一時退避
                         */
                        tmp = word_list[list_max - 1 - j];
                        found = 1;
                    }
                }
            }
            if (found) {
                /* まず、現在位置をクリア */
                word_list[list_max - j].iwnn  = NULL;
                word_list[list_max - j].result = NULL;
                /* 退避していたデータを追加 */
                word_list[total] = tmp;
                total++;
                /* 移動した候補数の調整 */
                count--;
            } else {
                /* resultをそのまま追加 */
                word_list[total].iwnn   = iwnn;
                word_list[total].result = &result[i];
                total++;
            }
            repr_list += (NJ_MAX_RESULT_LEN + NJ_TERM_LEN);
        }
        for (i = total; i < list_max; i++) {
            word_list[i].iwnn   = NULL;
            word_list[i].result  = NULL;
        }

    } else {
        /* 動作モード不正 */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MERGE_WORD_LIST, NJ_ERR_PARAM_MODE);
    }

    return (NJ_INT16)total;
}


/**
 * 単語登録情報取得API
 *
 * 詳細な単語登録情報を取得する。
 *
 * @param[in] iwnn          解析情報クラス
 * @param[in] result        処理結果情報
 * @param[out] info         単語登録情報
 *
 * @retval >=0 格納候補数
 * @retval <0  エラー
 */
NJ_EXTERN NJ_INT16 njx_get_word_info(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_WORD_INFO *info) {

    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_WORD_INFO, NJ_ERR_PARAM_ENV_NULL);
    }
    if (result == NULL) {
        /* 第2引数(result)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_WORD_INFO, NJ_ERR_PARAM_RESULT_NULL);
    }
    if (info == NULL) {
        /* 第3引数(info)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_WORD_INFO, NJ_ERR_WORD_INFO_NULL);
    }


    return njd_get_word_info(iwnn, result, info);
}


/**
 * 学習辞書操作API
 *
 * 学習辞書内、登録単語情報を操作する。
 *
 * @param[in] iwnn          解析情報クラス
 * @param[in] operation     操作オペレーション
 *
 * @retval >=0 残り登録キューサイズ（operation に NJ_MLD_OP_GET_SPACE を指定した場合）
 * @retval 0   正常終了（operation に NJ_MLD_OP_GET_SPACE 以外を指定した場合）
 * @retval <0  エラー
 */
NJ_EXTERN NJ_INT16 njx_manage_learndic(NJ_CLASS *iwnn, NJ_UINT32 operation) {
    NJ_INT16  ret;
    NJ_UINT16 i;
    NJ_UINT32 dic_type;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MANAGE_LEARNDIC, NJ_ERR_PARAM_ENV_NULL);
    }

    if (!((operation == NJ_MLD_OP_COMMIT) ||
          (operation == NJ_MLD_OP_COMMIT_TO_TOP) ||
          (operation == NJ_MLD_OP_COMMIT_CANCEL) ||
          (operation == NJ_MLD_OP_GET_SPACE)) ) {
        /* 第2引数(operation) が範囲外の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MANAGE_LEARNDIC, NJ_ERR_PARAM_MODE);
    }

    for (i = 0; i < NJ_MAX_DIC; i++) {
        if (iwnn->dic_set.dic[i].handle != NULL) {
            dic_type = NJ_GET_DIC_TYPE_EX(iwnn->dic_set.dic[i].type, iwnn->dic_set.dic[i].handle);
            if (dic_type == NJ_DIC_TYPE_LEARN) {
                /* 辞書セット内に最初に存在する学習辞書だけを対象に、処理を実行する */
                switch (operation) {
                case NJ_MLD_OP_COMMIT:         /* 学習辞書操作OP：操作位置記憶   */
                    ret = njd_l_mld_op_commit(iwnn, iwnn->dic_set.dic[i].handle);
                    if (ret < 0) {
                        return ret; /*NCH*/
                    }
                    return 0;   /* njd_l_mld_op_commit が 0 か 1 を返すときは正常動作 */

                case NJ_MLD_OP_COMMIT_TO_TOP:  /* 学習辞書操作OP：学習情報移動   */
                    if (iwnn->learndic_status.commit_status) {
                        return njd_l_mld_op_commit_to_top(iwnn, iwnn->dic_set.dic[i].handle);
                    }
                    /* 操作位置記憶前に移動を指示された場合、代わりに操作位置記憶動作を行う */
                    ret = njd_l_mld_op_commit(iwnn, iwnn->dic_set.dic[i].handle);
                    if (ret < 0) {
                        return ret; /*NCH*/
                    }
                    return 0;   /* njd_l_mld_op_commit が 0 か 1 を返すときは正常動作 */

                case NJ_MLD_OP_COMMIT_CANCEL:  /* 学習辞書操作OP：操作処理中止   */
                    if (iwnn->learndic_status.commit_status) {
                        return njd_l_mld_op_commit_cancel(iwnn, iwnn->dic_set.dic[i].handle);
                    }
                    /* 操作位置記憶を行っていない状態で操作中止を行った場合、何も行わない（0 は正常終了値） */
                    return 0;

                case NJ_MLD_OP_GET_SPACE:  /* 学習辞書操作OP：登録可能数取得 */
                    return njd_l_mld_op_get_space(iwnn, iwnn->dic_set.dic[i].handle);

                default:
                    break;
                }

                /* 先にパラメータの範囲チェックをしているので、ここには来ない。来たら内部エラー */
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MANAGE_LEARNDIC, NJ_ERR_INTERNAL); /*NCH*/
            }
        }
    }

    /* 辞書セット内に学習辞書が見つからない場合はエラーとなる */
    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MANAGE_LEARNDIC, NJ_ERR_DIC_NOT_FOUND);
}


/**
 * 属性データから状況設定値を変更する
 *
 * @param[in,out] iwnn    解析情報クラス
 * @param[in]     attr    属性データ
 *
 * @retval >=0 正常終了
 * @retval <0  エラー
 *
 */
static NJ_INT16 nje_change_state(NJ_CLASS *iwnn, NJ_UINT32 attr) {
    NJ_STATE_CALC_PARAMETER *calc_parameter;
    /* 属性データ用 */
    NJ_UINT8 *attr_tmp;
    NJ_INT32  state_system;
    NJ_INT32  i, j;
    NJ_INT16  ret = 0;
    NJ_UINT8  attr_l;
    NJ_UINT8  attr_m;
    NJ_UINT8  attr_idx;
    NJ_UINT8  bitmask;


    calc_parameter = iwnn->state.calc_parameter;

    for (i = 0; i < NJ_MAX_STATE; i++) {
        /* １文書入力中に変化する属性値を <0 から 0 に近づける処理 */
        if (calc_parameter->system_add_bias[i] != 0) {
            /* 加算値が定義されている場合 */
            state_system = iwnn->state.system[i]; /*NCH_V1*/
            if (state_system < 0) { /*NCH_V1*/
                /* 状況設定が <0 であれば 0 を超えない範囲で可算 */
                state_system += calc_parameter->system_add_bias[i]; /*NCH_V1*/
                if (state_system > 0) { /*NCH_V1*/
                    state_system = 0; /*NCH_V1*/
                }
                iwnn->state.system[i] = (NJ_INT16)state_system; /*NCH_V1*/
            }
        }
    }

    /* 属性データの最高頻度を算出 */
    attr_tmp = (NJ_UINT8*)&attr;
    j = 0;
    /* バイト単位でループ処理を行う */
    for (attr_l = 0; attr_l < sizeof(NJ_UINT32); attr_l++, attr_tmp++) {
        bitmask = 0x80;
        attr_idx = attr_l * 8;
        /* ビット単位でループを行う */
        for (attr_m = 0; (attr_m < 8) && (attr_idx < NJ_MAX_STATE); attr_m++, attr_idx++) {
            if ((*attr_tmp & bitmask) != 0) {
                for (i = 0; i < NJ_MAX_STATE; i++) {
                    state_system = iwnn->state.system[i];
#ifdef NJ_ADD_STATE_TYPE2
                    if ((i == attr_idx) && (state_system < calc_parameter->system_base_bias[i])) {
                        state_system = calc_parameter->system_base_bias[i];
                    }
#endif /* NJ_ADD_STATE_TYPE2 */
                    state_system += calc_parameter->system_change_bias[attr_idx][i];
                    if (state_system > calc_parameter->system_max_bias[i]) {
                        state_system = calc_parameter->system_max_bias[i];
                    } else if (state_system < calc_parameter->system_min_bias[i]) {
                        state_system = calc_parameter->system_min_bias[i]; /*NCH_V1*/
                    }
                    iwnn->state.system[i] = (NJ_INT16)state_system;
                }
            } else {
                /* １文書入力中に変化する属性値を >0 から 0 に近づける処理 */
                if (calc_parameter->system_subtract_bias[j] != 0) {
                    /* 減算値が定義されている場合 */
                    state_system = iwnn->state.system[j];
                    if (state_system > 0) {
                        /* 状況設定が >0 であれば 0 を超えない範囲で減算 */
                        state_system += calc_parameter->system_subtract_bias[j];
                        if (state_system < 0) {
                            state_system = 0;
                        }
                        iwnn->state.system[j] = (NJ_INT16)state_system;
                    }
                }
            }
            /* 1bitシフトさせる */
            bitmask >>= 1;
            j++;
        }
    }

    /* 文頭ビットは常に落とす */
    iwnn->state.system[NJ_CAT_FIELD_HEAD] = 0;

    return ret;
}


/**
 * 付加情報文字列取得API
 *
 * 指定した処理結果の付加情報文字列を取得する
 *
 * @param[in]  iwnn       解析情報クラス
 * @param[in]  result     処理結果
 * @param[in]  index      取得付加情報インデックス
 * @param[out] buf        付加情報文字列の出力先
 * @param[in]  buf_size   bufのサイズ
 *
 * @retval >=0 取得できた文字配列長
 * @retval <0  エラー(resultがNULLの場合を含む)
 */
NJ_EXTERN NJ_INT32 njx_get_additional_info(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_INT8 index, NJ_CHAR *buf, NJ_UINT32 buf_size) {
    NJ_UINT32 dic_type;
    NJ_INT32  ret;
    NJ_INT32  total_len;
    NJ_UINT16 result_num;
    NJ_UINT16 func_type;
    NJ_UINT8  internal_idx;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラーとする */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_ADD_INFO, NJ_ERR_PARAM_ENV_NULL);
    }
    if (result == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_ADD_INFO, NJ_ERR_PARAM_RESULT_NULL);
    }

    if (index >= NJ_MAX_ADDITIONAL_INFO) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_ADD_INFO, NJ_ERR_PARAM_INDEX_INVALID);
    }

    if ((buf == NULL) || (buf_size == 0)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_ADD_INFO, NJ_ERR_BUFFER_NOT_ENOUGH);
    }

    if (index == (-1)) {
        internal_idx = 0;
    } else {
        internal_idx = index;
    }
    if (result->word.stem.loc.handle != NULL) {
        dic_type = NJ_GET_DIC_TYPE_EX(result->word.stem.loc.type, result->word.stem.loc.handle);
        if ((!HAS_ADDITIONAL_INFO((result->word), internal_idx, dic_type))) {
            *buf = NJ_CHAR_NUL;
            return 0;
        }
    } else {
        /* handle がNULLの場合は候補を生成しない */
        *buf = NJ_CHAR_NUL;
        return 0;
    }

    result_num = 1;
    func_type = NJ_GET_RESULT_FUNC(result->operation_id);
    if (NJ_GET_RESULT_OP(result->operation_id) == NJ_OP_ANALYZE) {
        if (func_type == NJ_FUNC_CONVERT_MULTIPLE) {
            result_num = iwnn->environment.conv_buf.multiple_keep_len;
            result     = &(iwnn->environment.conv_buf.multiple_keep[0]);
        }
        else if (func_type == NJ_FUNC_CONVERT_SINGLE) {
            result_num = iwnn->environment.conv_buf.single_keep_len;
            result     = &(iwnn->environment.conv_buf.single_keep[0]);
        }
    }

    total_len = 0;
        
    while (result_num-- > 0) {
        if (result->word.stem.loc.handle != NULL) {
            dic_type = NJ_GET_DIC_TYPE_EX(result->word.stem.loc.type, result->word.stem.loc.handle);
            if ((!HAS_ADDITIONAL_INFO((result->word), internal_idx, dic_type))) {
                *buf = NJ_CHAR_NUL;
                result++;
                continue;
            }
        } else {
            /* handleがNULLの場合は、文字列を生成しない */
            *buf = NJ_CHAR_NUL;
            result++;
            continue;
        }

        ret = njd_get_additional_info(iwnn, result, index, buf, buf_size);
        if (ret < 0) {
            return ret;
        }

        total_len += ret;
        buf_size  -= (ret * sizeof(NJ_CHAR));
        buf       += ret;
        result++;
    }

    return total_len;
}


/**
 * 状況計算パラメータ設定をチェックする
 *
 * @param[in] iwnn    解析情報クラス
 *
 * @retval >=0 正常終了
 * @retval <0  エラー
 *
 */
static NJ_INT16 check_state_calc_parameter(NJ_STATE_CALC_PARAMETER *calc_parameter) {
    NJ_INT16 i;

    /* 辞書頻度値設定領域のターミネーターチェック */
    for (i = 0; i < NJ_MAX_DIC; i++) {
        /* NJ_MAX_DICまでは、ターミネーターが設定されているとエラー */
        if ((calc_parameter->dicinfo_max[i] == (NJ_INT16)NJ_STATE_TERMINATE) ||
            (calc_parameter->dicinfo_min[i] == (NJ_INT16)NJ_STATE_TERMINATE)) {
            /* 第2引数(state)の設定値が不正の場合はエラー */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STATE, NJ_ERR_PARAM_INVALID_STATE); /*NCH_FB*/
        }
    }
    /* NJ_MAX_DIC + 1は、ターミネーターが設定されていないとエラー */
    if ((calc_parameter->dicinfo_max[i] != (NJ_INT16)NJ_STATE_TERMINATE) ||
        (calc_parameter->dicinfo_min[i] != (NJ_INT16)NJ_STATE_TERMINATE)) {
        /* 第2引数(state)の設定値が不正の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_SET_STATE, NJ_ERR_PARAM_INVALID_STATE);
    }

    return 0;
}
