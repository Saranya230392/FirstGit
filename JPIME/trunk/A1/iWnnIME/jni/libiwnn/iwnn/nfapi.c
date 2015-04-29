/**
 * @file
 *   評価部 API部
 *
 *   入力された文字列に対し、予測もしくは変換を行い最適な解を返す。
 *   直前に確定された語彙に続く語彙検索も行う。
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2013 All Rights Reserved.
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
/** [予測機能] 学習解析制限条件 最大値 */
#define MAX_FORECAST_LEARN_LIMIT        NJ_MAX_CANDIDATE

/** [予測機能] 前方一致解析制限条件 最大値 */
#define MAX_FORECAST_LIMIT      NJ_MAX_CANDIDATE

/** njx_analyzeでのループリミッター (無限ループ防止) */
#define NJX_ANALYZE_LOOP_LIMIT  20000

/** 複合語予測 頻度順ソート時の参照複合語加算値 */
#define CMPDG_REF_SORT_COUNT  200

/** 複合語予測 頻度順ソート時の追加複合語加算値 */
#define CMPDG_ADD_SORT_COUNT  100


/************************************************/
/*              マ ク ロ 宣 言                  */
/************************************************/

/************************************************/
/*              extern  宣  言                  */
/************************************************/

/************************************************/
/*              構 造 体 宣 言                  */
/************************************************/

/************************************************/
/*              prototype  宣  言               */
/************************************************/
static void set_search_dic(NJ_CLASS *iwnn, NJ_DIC_SET *search_dics, NJ_DIC_SET *dics,
                           NJ_UINT16 op, NJ_UINT16 yomi_len, NJ_ANALYZE_OPTION *option);
static NJ_INT16 clear_flg(NJ_DIC_SET *dics, NJ_UINT16 mode);

static NJ_INT16 analyze_forward(NJ_CLASS *iwnn, NJ_RESULT *result);
static NJ_INT16 analyze_ai_yosoku(NJ_CLASS *iwnn, NJ_RESULT *result);
static NJ_INT16 analyze_next_yosoku(NJ_CLASS *iwnn, NJ_RESULT *result);
static NJ_INT16 analyze_conversion_multiple(NJ_CLASS *iwnn, NJ_RESULT *result);
static NJ_INT16 analyze_conversion_single(NJ_CLASS *iwnn, NJ_RESULT *result);
static NJ_INT16 analyze_complete(NJ_CLASS *iwnn, NJ_RESULT *result);
static NJ_INT16 analyze_complete_head(NJ_CLASS *iwnn, NJ_RESULT *result);
static NJ_INT16 analyze_complete_giji(NJ_CLASS *iwnn, NJ_RESULT *result);
static NJ_INT16 analyze_body(NJ_CLASS *iwnn, NJ_RESULT *result);
static NJ_INT16 analyze_mode_change(NJ_CLASS *iwnn);
static NJ_INT16 cmpdg_info_update(NJ_CLASS *iwnn);

/************************************************/
/*              外  部  変  数                  */
/************************************************/

/************************************************/
/*              static  変数宣言                */
/************************************************/

/**
 * 辞書セットから指定された解析条件にあった辞書のみ検索条件に設定する
 *
 * @param[in,out] iwnn      解析情報クラス
 * @param[out]    search_dics 辞書検索条件
 * @param[in]     dics        辞書セット
 * @param[in]     op          解析条件
 * @param[in]     yomi_clen   解析文字列の読み文字数(FunFun入力時は読み最小制限文字数)
 * @param[in]     option      予測オプション
 */
static void set_search_dic(NJ_CLASS *iwnn,
                           NJ_DIC_SET *search_dics,
                           NJ_DIC_SET *dics,
                           NJ_UINT16  op,
                           NJ_UINT16  yomi_clen,
                           NJ_ANALYZE_OPTION *option) {
    NJ_UINT32 dic_type;
    NJ_DIC_HANDLE *handle;
    NJ_UINT8 i;
    NJ_DIC_FREQ *rf;
    NJ_INT32 dic_freq_type;
    NJ_INT32 rdic_freq_type;


    /* 検索辞書情報のコピー */
    *search_dics = *dics;

    /* 使用する辞書頻度のモードを取得する */
    switch (op) {
    case NJ_ANALYZE_AI_YOSOKU:                     /* AI予測                   */
    case NJ_ANALYZE_NEXT_YOSOKU:                   /* 読みなし予測(繋がり予測) */
    case NJ_ANALYZE_FORWARD_SEARCH:                /* 前方一致解析     */
    case NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI:  /* 前方一致解析(読み無し予測含む) */
        dic_freq_type = rdic_freq_type = NJ_MODE_TYPE_YOSOKU;
        break;
    case NJ_ANALYZE_CONVERSION_SINGLE:             /* １文節解析 */
    case NJ_ANALYZE_CONVERSION_MULTIPLE:           /* 連文節解析 */
    case NJ_ANALYZE_COMPLETE_HEAD:                 /* 先頭文節全候補解析 */
        dic_freq_type = rdic_freq_type = NJ_MODE_TYPE_HENKAN;
        break;

    default:                                    /* 全候補解析,解析終了など */
        dic_freq_type = NJ_MODE_TYPE_HENKAN;
        /* 未然形など標準では使えない終了形を使うため、予測用を用いる。 */
        rdic_freq_type = NJ_MODE_TYPE_YOSOKU;
        break;
    }

    /* op に合致していないものは削除 */
    for (i = 0; i < NJ_MAX_DIC; i++) {
        handle = &(search_dics->dic[i].handle);
        if (*handle == NULL) {
            /* handle が NULLなら何もしない */
            continue;
        }
        if (NJ_CHECK_USE_DIC_FREQ(&search_dics->dic[i].dic_freq[dic_freq_type]) == 0) {
            /* 辞書頻度の設定で未使用の場合 */
            *handle = NULL;
            continue;
        }

        /* 擬似辞書と通常辞書の切り分け処理 */
        dic_type = NJ_GET_DIC_TYPE_EX(search_dics->dic[i].type, *handle);

        /* 通常予測・読みなし予測の時は、予測検索制限をチェックする */
        if ((op == NJ_ANALYZE_AI_YOSOKU)
            || (op == NJ_ANALYZE_NEXT_YOSOKU)
            || (op == NJ_ANALYZE_FORWARD_SEARCH)
            || (op == NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI)) {
            if (search_dics->dic[i].limit > yomi_clen) {
                /*
                 * 入力文字数が、検索開始文字数に満たない場合は、検索対象にしない。
                 */
                *handle = NULL;
                continue;
            }
            if (dic_type == NJ_DIC_TYPE_LEARN) {
                /* 学習辞書の場合 */
                if ((((option->mode & NJ_NO_LEARN) != 0) ||
                     (option->forecast_learn_limit == 0) ||
                     (option->forecast_limit == 0))) {
                    *handle = NULL;
                    continue;
                }
            } else {
                /* 学習辞書以外の場合 */
                if (((option->mode & NJ_NO_YOSOKU) != 0) || (option->forecast_limit == 0)) {
                    *handle = NULL;
                    continue;
                }

                if ((dic_type == NJ_DIC_TYPE_YOMINASHI) || 
                    ((dic_type == NJ_DIC_TYPE_EXT_YOMINASI) && (NJ_GET_DIC_VER(*handle) == NJ_DIC_VERSION1))) {
                    /* 読みなし予測辞書 or 拡張読み無し予測辞書Ver.1の場合 */
                    if ((op == NJ_ANALYZE_FORWARD_SEARCH) ||
                        (((op == NJ_ANALYZE_AI_YOSOKU) || (op == NJ_ANALYZE_NEXT_YOSOKU)) &&
                         (iwnn->environment.previous_selection_cnt != 0))) {
                        *handle = NULL;
                        continue;
                    }
                }
                if ((dic_type == NJ_DIC_TYPE_EXT_YOMINASI) && (NJ_GET_DIC_VER(*handle) == NJ_DIC_VERSION2)) {
                    /* 読みなし予測辞書の場合 */
                    if ((op == NJ_ANALYZE_FORWARD_SEARCH) ||
                        (((op == NJ_ANALYZE_AI_YOSOKU) || (op == NJ_ANALYZE_NEXT_YOSOKU)) &&
                         (iwnn->environment.previous_selection_cnt > 1))) {
                        *handle = NULL;
                        continue;
                    }
                }
            }
        }

        /* 辞書の使用可能・不可能チェック */
        switch (dic_type) {
        case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:
        case NJ_DIC_TYPE_FUSION_AWNN:
        case NJ_DIC_TYPE_FUSION:
        case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:
        case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:
        case NJ_DIC_TYPE_FUSION_STORAGE:
        case NJ_DIC_TYPE_LEARN:
            if (op == NJ_ANALYZE_COMPLETE_GIJI) {
                /* 全候補取得(擬似候補)時は使用しない */
                *handle = NULL;
            }
            break;

        case NJ_DIC_TYPE_PROGRAM:
        case NJ_DIC_TYPE_PROGRAM_FZK:
            /* すべてのオペレーションで使用する */
            break;

        case NJ_DIC_TYPE_YOMINASHI:
        case NJ_DIC_TYPE_EXT_YOMINASI:
            if ((op != NJ_ANALYZE_NEXT_YOSOKU) && (op != NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI)) {
                /* 読みなし予測/読みなし絞り込み時以外は使用しない */
                *handle = NULL;
            }
            break;

        case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:
        case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:
            if ((op == NJ_ANALYZE_CONVERSION_SINGLE) || (op == NJ_ANALYZE_CONVERSION_MULTIPLE)) {
                /* 高速化のため、連文節・単文節変換では使わない */
                *handle = NULL;
            }
            break;

        case NJ_DIC_TYPE_USER:
            if ((op == NJ_ANALYZE_AI_YOSOKU) || (op == NJ_ANALYZE_NEXT_YOSOKU)) {
                /* 繋がり予測では使わない */
                *handle = NULL;
            }
            break;

        case NJ_DIC_TYPE_CUSTOM_COMPRESS:
            if ((op != NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI) && (op != NJ_ANALYZE_FORWARD_SEARCH)
                && (op != NJ_ANALYZE_COMPLETE) && (op != NJ_ANALYZE_COMPLETE_HEAD)) {
                /* 予測・全候補取得、先頭文節全候補取得以外では使わない */
                *handle = NULL;
            }
            break;

        case NJ_DIC_TYPE_STDFORE:
            if ((op != NJ_ANALYZE_FORWARD_SEARCH) && (op != NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI)) {
                /* 通常予測以外では使わない */
                *handle = NULL;
            }
            break;

        case NJ_DIC_TYPE_FORECONV:
            if ((op != NJ_ANALYZE_FORWARD_SEARCH) && (op != NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI)
                && (op != NJ_ANALYZE_CONVERSION_SINGLE) && (op != NJ_ANALYZE_CONVERSION_MULTIPLE)
                && (op != NJ_ANALYZE_COMPLETE) && (op != NJ_ANALYZE_COMPLETE_HEAD)) {
                /* 通常予測、連文節・単文節変換、全候補取得、先頭文節全候補取得以外では使わない */
                *handle = NULL; /*NCH*/
            }
            break;

        case NJ_DIC_TYPE_JIRITSU:
        case NJ_DIC_TYPE_FZK:
            if ((op != NJ_ANALYZE_CONVERSION_SINGLE) && (op != NJ_ANALYZE_CONVERSION_MULTIPLE)
                && (op != NJ_ANALYZE_COMPLETE) && (op != NJ_ANALYZE_COMPLETE_HEAD)) {
                /* 連文節・単文節変換、全候補取得、先頭文節全候補取得以外では使わない */
                *handle = NULL;
            }
            break;

        case NJ_DIC_TYPE_TANKANJI:
            if ((op != NJ_ANALYZE_COMPLETE) && (op != NJ_ANALYZE_COMPLETE_HEAD)) {
                /* 全候補以外では使わない */
                *handle = NULL;
            }
            break;

        default:
            *handle = NULL; /*NCH*/
        }

        if (*handle != NULL) {
            /* 辞書を利用する場合、辞書頻度の値をモードにあわせて設定する */
            rf = &search_dics->dic[i].dic_freq[NJ_MODE_TYPE_HENKAN];
            rf->base = search_dics->dic[i].dic_freq[dic_freq_type].base;
            rf->high = search_dics->dic[i].dic_freq[dic_freq_type].high;
            NJ_CLEAR_OTHERFREQ_IN_DICINFO(&(search_dics->dic[i]));
        }
    }

    /* 検索条件に合わせてルール辞書を切り替える */
    search_dics->rHandle[NJ_MODE_TYPE_HENKAN] = dics->rHandle[rdic_freq_type];
    search_dics->rHandle[NJ_MODE_TYPE_YOSOKU] = NULL;
    search_dics->rHandle[NJ_MODE_TYPE_MORPHO] = NULL;
}


/**
 * 前方一致検索を行う
 *
 * @param[in,out] iwnn   解析情報クラス
 * @param[out]    result   候補(1つ)
 *
 * @retval >0 抽出候補(NJ_RESULT)の数(1)
 * @retval =0 候補なし
 * @retval <0 エラー(下位のエラーが返る)
 */
static NJ_INT16 analyze_forward(NJ_CLASS *iwnn, NJ_RESULT *result) {
    NJ_INT16 ret;
    NJ_UINT8 dummy;
    NJ_ANALYZE_ENV *env = &(iwnn->environment);


    /* 絞込検索対象候補確認フラグの初期化 */
    iwnn->relation_cand_flg = 0;

    if (env->level == 0) {
        /* 読み無し予測用前品詞 yominasi_fore に前品詞情報を設定する */
        njd_r_get_connect(env->dics->rHandle[NJ_MODE_TYPE_YOSOKU], env->prev_hinsi,
                          0, &(env->cursor.cond.hinsi.yominasi_fore));
        njd_r_get_count(env->dics->rHandle[NJ_MODE_TYPE_YOSOKU],
                        &(env->cursor.cond.hinsi.foreSize), &(env->cursor.cond.hinsi.rearSize));

        /* 拡張読み無し予測用接続後品詞を設定 */
        env->cursor.cond.hinsi.prev_bpos = env->prev_hinsi;

        /* 解析対象辞書設定 */
        if (env->cursor.cond.yclen > env->option.char_min) {
            /* 読み長指定予測でないとき */
            /* 入力文字数をもとに辞書セットを作成する */
            set_search_dic(iwnn, &(env->search_dics), env->dics,
                           env->type, env->cursor.cond.yclen, &(env->option));
        } else {
            /* 読み長指定予測のとき */
            /* 最小読み文字数をもとに辞書セットを作成する */
            set_search_dic(iwnn, &(env->search_dics), env->dics,
                           env->type, env->option.char_min, &(env->option));
        }
        env->cursor.cond.ds = &(env->search_dics);

        /* 検索条件から検索カーソルを作成する */
        ret = njd_search_word(iwnn, &(env->cursor), 1, &dummy);
        nj_strcpy(env->dics->keyword, env->cursor.cond.ds->keyword);

        if (ret <= 0) {
            /* 辞書引きでエラーもしくは候補無しの場合 */
            return ret;
        }
    }

    ret = njd_get_word(iwnn, &(env->cursor), result, 1);
    if (ret <= 0) {
        /* エラーの場合 */
        return ret;
    }

    env->level++;

    result->operation_id &= ~(NJ_OP_MASK | NJ_FUNC_MASK);
    result->operation_id |= (NJ_OP_ANALYZE | NJ_FUNC_SEARCH);

    /* 絞込検索候補であった場合 */
    if (iwnn->relation_cand_flg == 1) {
        /* 絞込検索候補フラグの立てる */
        result->operation_id |= NJ_FUNC_RELATION;
    }

    return ret;
}


/**
 * 標準予測辞書およびカスタマイズ辞書(非圧縮)からAI予測の取得を行う
 *
 * @param[in,out] iwnn    解析情報クラス
 * @param[out]    result    候補(1つ)
 *
 * @retval >0 抽出候補(NJ_RESULT)の数(=1)
 * @retval =0 候補なし
 * @retval <0 エラー(下位のエラーが返る)
 */
static NJ_INT16 analyze_ai_yosoku(NJ_CLASS *iwnn, NJ_RESULT *result) {
    NJ_INT16 ret = 0;
    NJ_ANALYZE_ENV *env = &(iwnn->environment);
    NJ_LEARN_WORD_INFO *lword;
    NJD_AIP_WORK *aipwork;
    NJ_INT32 cnt;
    NJ_UINT8 dummy;
    NJ_UINT8 i;



    /* AI予測用ワークエリアが存在するかにより、動作有無を確定させる。*/
    if (iwnn->option_data.aip_work == NULL) {
        return 0;
    } else {
        aipwork = (NJD_AIP_WORK*)iwnn->option_data.aip_work;
    }

    /* 読み無し予測用前品詞 yominasi_fore に前品詞情報を設定する */
    njd_r_get_connect(env->dics->rHandle[NJ_MODE_TYPE_YOSOKU], env->prev_hinsi,
                      0, &(env->cursor.cond.hinsi.yominasi_fore));
    njd_r_get_count(env->dics->rHandle[NJ_MODE_TYPE_YOSOKU],
                    &(env->cursor.cond.hinsi.foreSize), &(env->cursor.cond.hinsi.rearSize));

    /* 拡張読み無し予測用接続後品詞を設定 */
    env->cursor.cond.hinsi.prev_bpos = env->prev_hinsi;

    /* 初回設定 */
    set_search_dic(iwnn, &(env->search_dics), env->dics,
                   NJ_ANALYZE_AI_YOSOKU, env->cursor.cond.yclen, &(env->option));
    env->cursor.cond.ds = &(env->search_dics);

    aipwork->save_cnt = 0;
    cnt = 0;
    for (i = 1; (i <= iwnn->previous_selection.count) && (i <= NJD_AIP_PERMISSION_WORD); i++) {
        /*
         * 前確定情報からAI予測用前確定情報を作成する。
         */
        lword = NULL;
        lword = nje_get_previous_selection(iwnn, i, NJ_PREVIOUS_SELECTION_AI);
        if (lword != NULL) {
            /* 前確定情報の品詞条件を保存する */
            env->prev_hinsi = lword->b_hinsi;

            /* 検索条件(読み、表記)を設定する */
            env->cursor.cond.yomi  = lword->yomi;
            env->cursor.cond.ylen  = lword->yomi_len;
            env->cursor.cond.kanji = lword->hyouki;

            /* 検索条件から検索カーソルを作成する */
            ret = njd_search_word(iwnn, &(env->cursor), 1, &dummy);
            if (ret < 0) {
                /* 辞書引きでエラーの場合 */
                return ret;
            }
            if (ret == 0) {
                continue;
            }

            while((ret = njd_get_word(iwnn, &(env->cursor), &(aipwork->aip_dat[cnt].result), 1)) > 0) {
                /* 検索結果ありの場合、検索結果を記憶する。*/
                aipwork->aip_dat[cnt].result.operation_id &= ~(NJ_OP_MASK | NJ_FUNC_MASK);
                aipwork->aip_dat[cnt].result.operation_id |= NJ_OP_ANALYZE | NJ_FUNC_NEXT;
                cnt++;
                if (cnt >= NJD_AIP_WORK_CNT) {
                    break;
                }
            }
            if (ret < 0) {
                /* エラーの場合 */
                return ret;
            }
        } else {
            /* AI予測用前確定情報を生成できない場合は、次の検索モードへ遷移 */
            break; /*NCH*/
        }

        if (cnt != 0) {
            /* AI予測用の検索結果格納候補数を更新する。*/
            aipwork->condition = env->cursor.cond;
            aipwork->save_cnt = cnt;
            break;
        }
    }

    /* 次の検索モード用に前確定情報を更新しておく */
    lword = NULL;
    env->previous_selection_cnt = iwnn->previous_selection.count;
    while (env->previous_selection_cnt > 0) {
        /* 読みなし予測： 直前の njx_select 結果を用いる */
        lword = nje_get_previous_selection(iwnn, env->previous_selection_cnt, NJ_PREVIOUS_SELECTION_NORMAL);
        env->previous_selection_cnt--;
        if (lword != NULL) {
            /* 前確定情報が生成できた場合 */

            /* 前確定情報の品詞条件を保存する */
            env->prev_hinsi = lword->b_hinsi;

            /* 検索条件(読み、表記)を設定する */
            env->cursor.cond.yomi  = lword->yomi;
            env->cursor.cond.ylen  = lword->yomi_len;
            env->cursor.cond.kanji = lword->hyouki;
            break;
        }
    }

    return 0;
}


/**
 * 標準予測辞書およびカスタマイズ辞書(非圧縮)から読み無し予測(繋がり予測)を行う
 *
 * @param[in,out] iwnn    解析情報クラス
 * @param[out]    result    候補(1つ)
 *
 * @retval >0 抽出候補(NJ_RESULT)の数(=1)
 * @retval =0 候補なし
 * @retval <0 エラー(下位のエラーが返る)
 */
static NJ_INT16 analyze_next_yosoku(NJ_CLASS *iwnn, NJ_RESULT *result) {
    NJ_INT16 ret = 0;
    NJ_UINT8 dummy;
    NJ_ANALYZE_ENV *env = &(iwnn->environment);
    NJ_LEARN_WORD_INFO *lword;


    while (env->previous_selection_cnt >= 0) {
        /* 絞込検索対象候補確認フラグの初期化 */
        iwnn->relation_cand_flg = 0;

        if (env->level == 0) {
            /* 読み無し予測用前品詞 yominasi_fore に前品詞情報を設定する */
            njd_r_get_connect(env->dics->rHandle[NJ_MODE_TYPE_YOSOKU], env->prev_hinsi,
                              0, &(env->cursor.cond.hinsi.yominasi_fore));
            njd_r_get_count(env->dics->rHandle[NJ_MODE_TYPE_YOSOKU],
                            &(env->cursor.cond.hinsi.foreSize), &(env->cursor.cond.hinsi.rearSize));

            /* 拡張読み無し予測用接続後品詞を設定 */
            env->cursor.cond.hinsi.prev_bpos = env->prev_hinsi;

            /* 初回設定 */
            set_search_dic(iwnn, &(env->search_dics), env->dics,
                           NJ_ANALYZE_NEXT_YOSOKU, env->cursor.cond.yclen, &(env->option));
            env->cursor.cond.ds = &(env->search_dics);

            /* 検索条件から検索カーソルを作成する */
            ret = njd_search_word(iwnn, &(env->cursor), 1, &dummy);
            if (ret < 0) {
                /* 辞書引きでエラーの場合 */
                return ret;
            }
        } else {
            /* 検索済みのため、以降の候補取得を動かす */
            ret = 1;
        }

        if (ret > 0) {
            ret = njd_get_word(iwnn, &(env->cursor), result, 1);
            if (ret < 0) {
                /* エラーの場合 */
                return ret;
            } else if (ret > 0) {
                env->level++;

                result->operation_id &= ~(NJ_OP_MASK | NJ_FUNC_MASK);
                result->operation_id |= NJ_OP_ANALYZE | NJ_FUNC_NEXT;
                /* 絞込検索候補であった場合 */
                if (iwnn->relation_cand_flg == 1) {
                    /* 絞込検索候補フラグの立てる */
                    result->operation_id |= NJ_FUNC_RELATION;/*NCH_FB*/
                }
                return ret;
            }
        }

        if (env->previous_selection_cnt == 0) {
            env->previous_selection_cnt--; 
        }

        /* 
         * 上記検索で候補なしの場合、前確定情報を更新する
         */
        env->level = 0;
        lword = NULL;
        while (env->previous_selection_cnt > 0) {
            /* 読みなし予測： 直前の njx_select 結果を用いる */
            lword = nje_get_previous_selection(iwnn, env->previous_selection_cnt, NJ_PREVIOUS_SELECTION_NORMAL);
            env->previous_selection_cnt--;
            if (lword != NULL) {
                /* 前確定情報の品詞条件を保存する */
                env->prev_hinsi = lword->b_hinsi;
                
                /* 検索条件(読み、表記)を設定する */
                env->cursor.cond.yomi  = lword->yomi;
                env->cursor.cond.ylen  = lword->yomi_len;
                env->cursor.cond.kanji = lword->hyouki;
                /* 前確定情報が1文節 or 2文節の場合 */
                if (env->previous_selection_cnt < 2) {
                    /* 状況カテゴリ情報を設定する */
                    env->cursor.cond.attr = lword->attr;
                }
                break;
            }
        }
    }

    return ret;
}


/**
 * 連文節解析を行う。(analyze_bodyの子供)
 *
 * @param[in,out] iwnn     解析情報クラス
 * @param[out]    result     候補(2つ)
 *
 * @retval >0 抽出候補(NJ_RESULT)の数(=文節数)
 * @retval =0 候補なし
 * @retval <0 エラー(下位のエラーが返る)
 */
static NJ_INT16 analyze_conversion_multiple(NJ_CLASS *iwnn, NJ_RESULT *result) {
    NJ_INT16 i, ret;
    NJ_ANALYZE_ENV *env = &(iwnn->environment);
    NJ_ANALYZE_CONV_BUF *buf = &(env->conv_buf);
    NJ_RESULT *tmp = &(buf->multiple_keep[0]);
    NJ_UINT8 dictypeflg = 0;
    NJ_UINT8 divide_pos;
    NJ_UINT8 analyze_level;
    NJ_UINT8 zen_flg = 0;
    NJ_UINT16 len = 0;
    NJ_WORD *tmp_word;


    set_search_dic(iwnn, &(env->search_dics), env->dics,
                   NJ_ANALYZE_CONVERSION_MULTIPLE, env->cursor.cond.yclen, &(env->option));

    /* 文節切り位置指定 */
    divide_pos = 0;
    if ((env->option.mode & NJ_HEAD_CONV_ON) ||
        (env->option.mode & NJ_HEAD_CONV2_ON)) {
        /**
         * 逐次単文節変換の場合、
         * 予測オプションに指定された文節区切り位置を設定
         */
        if (env->option.in_divide_pos != env->cursor.cond.ylen) {
            divide_pos = env->option.in_divide_pos;
            analyze_level = NJ_MAX_PHRASE;
        } else {
            analyze_level = 1;
            zen_flg = 1;
        }
    } else {
        /* 文節区切り位置指定無しを設定 */
        analyze_level = NJ_MAX_PHRASE;
    }

    /* 連文節変換 */
    ret = njc_conv(iwnn, &(env->search_dics), env->yomi, analyze_level, divide_pos, tmp, 0);
    if (ret <= 0) {
        return ret;
    }

    /* 逐次単文節変換ON時のみ、文節区切り位置を登録 */
    if ((env->option.mode & NJ_HEAD_CONV_ON) ||
        (env->option.mode & NJ_HEAD_CONV2_ON)) {
        if ((NJ_GET_RESULT_DIC((tmp)->operation_id) == NJ_DIC_LEARN) &&
            ((tmp->word.stem.loc.current_info >> 4) & 0x0F) > 1) {
            /* 複合学習候補の場合、結合文節の１文節目の読みを取得する。*/
            env->option.out_divide_pos = (NJ_UINT8)njd_l_get_stroke(iwnn, &(tmp->word), iwnn->muhenkan_tmp, sizeof(iwnn->muhenkan_tmp));
        } else {
            env->option.out_divide_pos = (NJ_UINT8)(NJ_GET_YLEN_FROM_STEM_EXT(&(tmp->word)) + NJ_GET_YLEN_FROM_FZK(&(tmp->word)));
        }
    }

    /* オペレーションIDの付加 */
    for (i = 0; i < ret; i++) {
        (tmp + i)->operation_id &= ~(NJ_OP_MASK | NJ_FUNC_MASK);
        (tmp + i)->operation_id |= NJ_OP_ANALYZE | NJ_FUNC_CONVERT_MULTIPLE;
        if (!(dictypeflg) &&
            (NJ_GET_RESULT_DIC((tmp + i)->operation_id) == NJ_DIC_LEARN)) {
            dictypeflg = NJ_FLAG_ON;
        }
        if ((zen_flg) && !((tmp + i)->operation_id & NJ_TYPE_GIJI_MASK)) {
            (tmp + i)->operation_id &= ~(NJ_FUNC_CONVERT_MULTIPLE);
            (tmp + i)->operation_id |= NJ_FUNC_ZENKOUHO;
        }

        tmp_word = &((tmp + i)->word);
        if (((((tmp_word->stem.loc.status) >> 4) & 0x0F) != NJ_CUR_OP_COMP_EXT) &&
            ((((tmp_word->stem.loc.status) >> 4) & 0x0F) != NJ_CUR_OP_FORE_EXT)) {
            /* 読み文字列長を設定 */
        len = (NJ_UINT8)(len + (NJ_GET_YLEN_FROM_STEM(&((tmp + i)->word)) + NJ_GET_YLEN_FROM_FZK(&((tmp + i)->word))));

        } else {
            /* 読み文字列長を設定 */
            len += (NJ_UINT8)(NJ_GET_EXT_YLEN_FROM_STEM(&((tmp + i)->word)) + NJ_GET_YLEN_FROM_FZK(&((tmp + i)->word)));

        }
    }
    if (len > NJ_MAX_LEN) {
        /*
         * 最大読み文字配列長を超えた場合は、無効とする。
         */
        env->option.out_divide_pos = 0;
        return 0;
    }
    env->level++;

    /* conv_bufへ、解析結果をバックアップする */
    if (dictypeflg) {
        tmp->operation_id = (tmp->operation_id & 0x0FFF) | NJ_DIC_LEARN;
    }
    *result = *tmp;
    buf->multiple_keep_len = ret;
    return ret;
}


/**
 * 1文節解析を行う。(analyze_bodyの子供)
 *
 * @param[in,out] iwnn     解析情報クラス
 * @param[out]    result     候補(2つ)
 *
 * @retval >0 抽出候補(NJ_RESULT)の数(=1 or 2)
 * @retval =0 候補なし
 * @retval <0 エラー(下位のエラーが返る)
 */
static NJ_INT16 analyze_conversion_single(NJ_CLASS *iwnn, NJ_RESULT *result) {
    NJ_INT16 ret;
    NJ_UINT16 len, ylen;
    NJ_ANALYZE_ENV *env = &(iwnn->environment);
    NJ_ANALYZE_CONV_BUF *buf = &(env->conv_buf);
    NJ_RESULT *tmp = &(buf->single_keep[0]);
    NJ_UINT16 tmp_len = 0;


    set_search_dic(iwnn, &(env->search_dics), env->dics,
                   NJ_ANALYZE_CONVERSION_SINGLE, env->cursor.cond.yclen, &(env->option));

    /* 文節切り位置指定無しで１文節変換 */
    ret = njc_top_conv(iwnn, &(env->search_dics), env->yomi, tmp, 1);
    if (ret <= 0) {
        return ret;
    }

    ylen = nj_strlen(env->yomi);
    len = NJ_GET_YLEN_FROM_STEM_EXT(&(tmp->word)) + NJ_GET_YLEN_FROM_FZK(&(tmp->word));

    if (ylen < len) {
        /* 1文節解析結果のよみ文字列長が、入力したよみ文字列長を超えた */
        /* 通常発生しない */
        return 0;
    }

    env->level++;

    /* オペレーションIDを設定する */
    tmp->operation_id &= ~(NJ_OP_MASK | NJ_FUNC_MASK);
    tmp->operation_id |= (NJ_OP_ANALYZE | NJ_FUNC_CONVERT_SINGLE); 

    *result = *tmp;

    if ((ylen == len)
        && (((env->option.mode & NJ_NO_RENBUN) == 0) || ((env->option.mode & NJ_NO_ZEN) == 0))) {
        return 0;
    } else if (ylen == len) {
        buf->single_keep_len = 1;
        return 1;
    }
    if ((NJ_GET_KLEN_FROM_STEM(&tmp->word) + NJ_GET_KLEN_FROM_FZK(&tmp->word) + (ylen - len))
        > NJ_MAX_RESULT_LEN) {
        /*
         * 最大表記出力長を超えた場合は、無効とする。
         */
        return 0; /*NCH*/
    }
    if (((((tmp->word.stem.loc.status) >> 4) & 0x0F) != NJ_CUR_OP_COMP_EXT) &&
        ((((tmp->word.stem.loc.status) >> 4) & 0x0F) != NJ_CUR_OP_FORE_EXT)) {
        /* 読み文字列長を設定 */
        tmp_len = (NJ_UINT8)NJ_GET_YLEN_FROM_STEM(&tmp->word) + NJ_GET_YLEN_FROM_FZK(&tmp->word) + (ylen - len);

    } else {
        /* 読み文字列長を設定 */
        tmp_len = (NJ_UINT8)NJ_GET_EXT_YLEN_FROM_STEM(&tmp->word) + NJ_GET_YLEN_FROM_FZK(&tmp->word) + (ylen - len);

    }
    if (tmp_len > NJ_MAX_LEN) {
        /*
         * 最大読み文字配列長を超えた場合は、無効とする。
         */
        return 0;
    }

    tmp++;
    /* 解析あまり部分に入力文字列をそのまま追加 */

    tmp->operation_id = NJ_OP_ANALYZE | NJ_FUNC_CONVERT_SINGLE | NJ_DIC_GIJI;

    njd_init_word(&(tmp->word));
    tmp->word.yomi = (env->yomi + len);
    NJ_SET_YLEN_TO_STEM(&tmp->word, (NJ_UINT16)(ylen - len));
    NJ_SET_KLEN_TO_STEM(&tmp->word, (NJ_UINT16)(ylen - len));

    /* 前品詞 = 擬似、後品詞 = 擬似 */
    NJ_SET_FPOS_TO_STEM(&tmp->word,
                        njd_r_get_hinsi(env->dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_GIJI_F));
    NJ_SET_BPOS_TO_STEM(&tmp->word,
                        njd_r_get_hinsi(env->dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_GIJI_B));

    NJ_SET_TYPE_TO_STEM(&tmp->word, NJ_TYPE_HIRAGANA);
    NJ_SET_FREQ_TO_STEM(&tmp->word, GIJI_GETSCORE(ylen - len));

    /* conv_bufへ、解析結果をバックアップする */
    buf->single_keep_len = 2;
    return 2;
}


/**
 * 全候補取得を行う。(analyze_bodyの子供)
 *
 * @param[in,out] iwnn     解析情報クラス
 * @param[out]    result     候補(1つ)
 *
 * @retval >0 抽出候補(NJ_RESULT)の数(=1)
 * @retval =0 候補なし
 * @retval <0 エラー(下位のエラーが返る)
 */
static NJ_INT16 analyze_complete(NJ_CLASS *iwnn, NJ_RESULT *result) {

    NJ_INT16 ret;
    NJ_RESULT target;
    NJ_ANALYZE_ENV *env = &(iwnn->environment);


    if (env->level == 0) {
        set_search_dic(iwnn, &(env->search_dics), env->dics,
                       NJ_ANALYZE_COMPLETE, env->cursor.cond.yclen, &(env->option));

        target.operation_id = 0;

        njd_init_word(&(target.word));
        target.word.yomi = env->yomi;
        target.word.stem.type = NJ_TYPE_UNDEFINE;
        NJ_SET_YLEN_TO_STEM(&target.word, nj_strlen(env->yomi));
        ret = njc_zenkouho1(iwnn, &(env->search_dics), &target, result);

    } else {
        ret = njc_zenkouho1(iwnn, &(env->search_dics), NULL, result);
    }

    if (ret <= 0) {
        return ret;
    }

    env->level++;
    result->operation_id &= ~(NJ_OP_MASK | NJ_FUNC_MASK);
    result->operation_id |= (NJ_OP_ANALYZE | NJ_FUNC_ZENKOUHO);
    return 1;
}


/**
 * 先頭文節全候補取得を行う。(analyze_bodyの子供)
 *
 * @param[in,out]  iwnn    解析情報クラス
 * @param[out]     result  候補(1つ)
 *
 * @retval  >0  抽出候補(NJ_RESULT)の数(=1)
 * @retval  =0  候補なし
 * @retval  <0  エラー(下位のエラーが返る)
 */
static NJ_INT16 analyze_complete_head(NJ_CLASS *iwnn, NJ_RESULT *result) {

    NJ_INT16 ret;
    NJ_INT16 offset;
    NJ_RESULT target;
    NJ_ANALYZE_ENV *env = &(iwnn->environment);
    NJ_CHAR swap;
    NJ_UINT32 type;


    if (env->level == 0) {
        if ((NJ_GET_RESULT_DIC(env->conv_buf.multiple_keep[0].operation_id) == NJ_DIC_LEARN) &&
            (((env->conv_buf.multiple_keep[0].word.stem.loc.current_info >> 4) & 0x0F) > 1)) {
            /* 複合語文節の場合、先頭文節を切り出すことができないため、
             * 先頭文節の全候補処理に遷移させる。
             */
            env->level++;
        } else {
            /* 
             * 連文節変換結果の先頭文節を強制的に出力を行う。
             * また、候補は、連文節変換結果保持用バッファから候補を生成する。
             */
            result->operation_id = 0;
            njd_init_word(&(result->word));

            /* 読み文字列を第一文節部分でNULLターミネート */
            swap = *(env->yomi + env->option.out_divide_pos);
            *(env->yomi + env->option.out_divide_pos) = NJ_CHAR_NUL;
            if (nj_charlen(env->yomi) < env->option.char_min) {
                *(env->yomi + env->option.out_divide_pos) = swap; /*NCH*/
                return 0; /*NCH*/
            } else {
                *result = env->conv_buf.multiple_keep[0];
                result->operation_id &= ~(NJ_OP_MASK | NJ_FUNC_MASK);
                result->operation_id |= (NJ_OP_ANALYZE | NJ_FUNC_ZENKOUHO_HEAD);

                if (result->word.stem.loc.handle != NULL) {
                    type = NJ_GET_DIC_TYPE_EX(result->word.stem.loc.type, result->word.stem.loc.handle);

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
                    case NJ_DIC_TYPE_EXT_YOMINASI:                  /* 拡張読み無し予測辞書         */
                    case NJ_DIC_TYPE_FORECONV:                      /* 予測変換辞書                 */
                        result->operation_id &= 0x0FFF;
                        result->operation_id |= NJ_DIC_STATIC;
                        break;

                    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:             /* 非圧縮カスタマイズ辞書       */
                    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:       /* 非圧縮カスタマイズ辞書(学習辞書変更) */
                    case NJ_DIC_TYPE_CUSTOM_COMPRESS:               /* 圧縮カスタマイズ辞書         */
                        result->operation_id &= 0x0FFF;
                        result->operation_id |= NJ_DIC_CUSTOMIZE;
                        break;

                    case NJ_DIC_TYPE_LEARN:                         /* 学習辞書                     */
                        result->operation_id &= 0x0FFF;
                        result->operation_id |= NJ_DIC_LEARN;
                        break;

                    case NJ_DIC_TYPE_USER:                          /* ユーザ辞書                   */
                        result->operation_id &= 0x0FFF;
                        result->operation_id |= NJ_DIC_USER;
                        break;

                    case NJ_DIC_TYPE_PROGRAM:                       /* 擬似辞書                     */
                    case NJ_DIC_TYPE_PROGRAM_FZK:                   /* 擬似辞書-付属語-             */
                        result->operation_id &= 0x0FFF;
                        result->operation_id |= NJ_DIC_PROGRAM;
                        break;

                    default:
                        /* ここに来ることはない */
                        result->operation_id &= 0x0FFF;
                        result->operation_id |= NJ_DIC_STATIC; /*NCH*/
                    }
                }
                ret = 1;
            }
            /* 読み文字列のNULLターミネートを解除 */
            *(env->yomi + env->option.out_divide_pos) = swap;

            if (!((env->option.in_divide_pos != 0) &&
                  (env->conv_buf.multiple_keep_len > 1))) {
                /* 全候補処理は、重複候補削除処理が行われないため、同表記バッファに追加を行う。 */
                nje_append_homonym(iwnn, result, (NJ_INT16)(NJ_GET_YLEN_FROM_STEM(&result->word) + NJ_GET_YLEN_FROM_FZK(&result->word)), &offset);
            }
        }
        env->divipos = env->option.out_divide_pos;
    }
    
    while (env->divipos > 0) {
        if (env->level == 1) {
            set_search_dic(iwnn, &(env->search_dics), env->dics,
                           NJ_ANALYZE_COMPLETE_HEAD, env->cursor.cond.yclen, &(env->option));

            target.operation_id = 0;

            njd_init_word(&(target.word));
            target.word.yomi = env->yomi;
            target.word.stem.type = NJ_TYPE_UNDEFINE;
            /* 読み文字列を第一文節部分でNULLターミネート */
            swap = *(target.word.yomi + env->divipos);
            *(target.word.yomi + env->divipos) = NJ_CHAR_NUL;

            if (nj_charlen(target.word.yomi) < env->option.char_min) {
                /* 読み文字列のNULLターミネートを解除 */
                *(target.word.yomi + env->divipos) = swap; /*NCH*/
                return 0; /*NCH*/
            }
            NJ_SET_YLEN_TO_STEM(&target.word, nj_strlen(env->yomi));

            /* 全候補取得処理 */
            ret = njc_zenkouho1(iwnn, &(env->search_dics), &target, result);

            /* 読み文字列のNULLターミネートを解除 */
            *(target.word.yomi + env->divipos) = swap;

        } else {
            if (env->level != 0) {
                ret = njc_zenkouho1(iwnn, &(env->search_dics), NULL, result);
            }
        }
        if (ret == 1) {
            break;
        }
        if (ret == 0) {
            if ((env->option.mode & NJ_HEAD_CONV2_ON) == 0) {
                /* NJ_HEAD_CONV2_ONが指定されていない場合は、NJ_HEAD_CONV_ONとして動作する。*/
                break;
            }
            env->level = 1;
            if (env->divipos >= 2) {
                /* 文字数を考慮して縮小処理を行う。 */
                if (NJ_CHAR_LEN(env->yomi + env->divipos - 2) == 2) {
                    /* SJIS 2byte もしくは、UTF16サロゲートペア */
                    env->divipos = env->divipos - 2;
                } else {
                    env->divipos--;
                }
                
            } else {
                /* 縮小処理が終わるため、終了条件を設定 */
                env->divipos = 0;
            }
            continue;
        }
        if (ret < 0) {
            return ret;
        }
    }

    if (ret <= 0) {
        return ret;
    }

    env->level++;
    result->operation_id &= ~(NJ_OP_MASK | NJ_FUNC_MASK);
    result->operation_id |= (NJ_OP_ANALYZE | NJ_FUNC_ZENKOUHO_HEAD);
    return 1;
}

/**
 * 全候補取得(擬似候補)を行う。(analyze_bodyの子供)
 *
 * @param[in,out]  iwnn    解析情報クラス
 * @param[out]     result  候補(1つ)
 *
 * @retval  >0  抽出候補(NJ_RESULT)の数(=1)
 * @retval  =0  候補なし
 * @retval  <0  エラー(下位のエラーが返る)
 */
static NJ_INT16 analyze_complete_giji(NJ_CLASS *iwnn, NJ_RESULT *result) {

    NJ_INT16 ret;
    NJ_RESULT target;
    NJ_ANALYZE_ENV *env = &(iwnn->environment);


    if (env->level == 0) {
        set_search_dic(iwnn, &(env->search_dics), env->dics,
                       NJ_ANALYZE_COMPLETE_GIJI, env->cursor.cond.yclen, &(env->option));

        target.operation_id = 0;

        njd_init_word(&(target.word));
        target.word.yomi = env->yomi;
        target.word.stem.type = NJ_TYPE_UNDEFINE;
        NJ_SET_YLEN_TO_STEM(&target.word, nj_strlen(env->yomi));
        ret = njc_zenkouho1(iwnn, &(env->search_dics), &target, result);

    } else {
        ret = njc_zenkouho1(iwnn, &(env->search_dics), NULL, result);
    }

    if (ret <= 0) {
        return ret;
    }

    env->level++;
    result->operation_id &= ~(NJ_OP_MASK | NJ_FUNC_MASK);
    result->operation_id |= (NJ_OP_ANALYZE | NJ_FUNC_ZENKOUHO);
    return 1;
}

/**
 * 評価部解析処理本体。各解析処理の流れを制御する。
 *
 * @param[in,out] iwnn   解析情報クラス
 * @param[out]    result   候補(2つ)
 *
 * @retval >0 候補に格納した数
 * @retval =0 候補無し(終了)
 * @retval <0 エラー(発生しない)
 */
static NJ_INT16 analyze_body(NJ_CLASS *iwnn, NJ_RESULT *result) {

    NJ_INT16 ret;
    NJ_ANALYZE_ENV *env = &(iwnn->environment);


    switch (env->type) {
    case NJ_ANALYZE_AI_YOSOKU:   /* AI予測 */
        ret = analyze_ai_yosoku(iwnn, result);
        if (ret != 0) {
            return ret;
        }

        env->level = -1;
        break;

    case NJ_ANALYZE_NEXT_YOSOKU:   /* 読み無し予測(予測つながり)     */
        ret = analyze_next_yosoku(iwnn, result);
        if (ret != 0) {
            return ret;
        }

        env->level = -1;
        break;


    case NJ_ANALYZE_FORWARD_SEARCH:        /* 前方一致解析   */
    case NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI: /* 前方一致解析(読み無し予測含む) */

        ret = analyze_forward(iwnn, result);
        if (ret != 0) {
            return ret;
        }
        env->level = -1;
        break;

    case NJ_ANALYZE_CONVERSION_MULTIPLE:   /* 連文節解析 */

        if (env->level == 0) {
            ret = analyze_conversion_multiple(iwnn, result);

            if (ret != 0) {
                return ret;
            }
        }

        env->level = -1;
        break;


    case NJ_ANALYZE_CONVERSION_SINGLE:     /* 1文節解析 */

        if (env->level == 0) {
            ret = analyze_conversion_single(iwnn, result);
            if (ret != 0) {
                return ret;
            }
        }

        env->level = -1;
        break;

    case NJ_ANALYZE_COMPLETE:      /* 全候補解析 */
        ret = analyze_complete(iwnn, result);
        if (ret != 0) {
            return ret;
        }

        env->level = -1;
        break;

    case NJ_ANALYZE_COMPLETE_HEAD:      /* 先頭文節全候補解析 */
        ret = analyze_complete_head(iwnn, result);
        if (ret != 0) {
            return ret;
        }

        env->level = -1;
        break;

    case NJ_ANALYZE_COMPLETE_GIJI:      /* 全候補解析(擬似候補) */
        ret = analyze_complete_giji(iwnn, result);
        if (ret != 0) {
            return ret;
        }

        env->level = -1;
        break;

    case NJ_ANALYZE_END:

        /* 終了 */
        env->level = 0;
        break;

    default:
        /* この部分は通常おこらない */
        return NJ_SET_ERR_VAL(NJ_FUNC_ANALYZE_BODY, NJ_ERR_PARAM_OPERATION); /*NCH*/
    }

    /* 次検索解析処理 */
    ret = analyze_mode_change(iwnn);

    return 0;
}


/************************************************/
/* Global関数                                   */
/************************************************/

/**
 * 予測候補取得API
 *
 * 指定した辞書セット、よみを用いて予測候補を含む候補を１つ抽出する。
 * よみがNULLの場合は、次候補を返す。
 * よみが、空文字列("")であれば、前に確定された情報に繋がる候補を返す。
 *
 * @param[in,out] iwnn   解析情報クラス
 * @param[in] charset    あいまい定義文字
 * @param[in] yomi       よみ文字列。<br>
 *                          NULLなら前解析の次候補、空文字列("")なら次候補を返す。
 * @param[out] result    処理結果。バッファはアプリで用意する(1つ分)
 * @param[in] option     予測オプション
 *
 * @retval >0  候補に格納した数(=1)
 * @retval =0  候補無し(終了)
 * @retval <0  エラー
 */
NJ_EXTERN NJ_INT16 njx_analyze(NJ_CLASS *iwnn, NJ_CHARSET *charset,
                               NJ_CHAR  *yomi, NJ_RESULT *result,
                               NJ_ANALYZE_OPTION *option) {

    NJ_LEARN_WORD_INFO *lword;
    NJ_INT16 ret;
    NJ_ANALYZE_ENV *env;
    NJ_DIC_SET *dics;
    NJ_SEARCH_CONDITION *cond;
    NJ_INT16 offset;
    NJ_UINT16 yomiLen = 0;

    NJ_SEARCH_CACHE     *pCache;
    NJ_UINT16           kw_len;
    NJ_UINT16           initst, inited;
    NJ_UINT16           clrcnt, diccnt;
    NJ_CHAR             *p_yomi, *p_key;
    NJ_UINT16           cacheOverKeyPtr;
    NJ_PHASE2_FILTER_IF phase2_filter_if;
    NJ_PHASE2_FILTER_MESSAGE ph2_filter_message;
    NJ_DIC_INFO *dic_info;
    NJ_UINT32 i, loop_count;


    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ANALYZE, NJ_ERR_PARAM_ENV_NULL);
    }
    env = &(iwnn->environment);
    dics = &(iwnn->dic_set);

    if (dics->rHandle[NJ_MODE_TYPE_HENKAN] == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ANALYZE, NJ_ERR_NO_RULEDIC);
    }
    if (dics->rHandle[NJ_MODE_TYPE_YOSOKU] == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ANALYZE, NJ_ERR_NO_RULEDIC);
    }
    if (result == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ANALYZE, NJ_ERR_PARAM_RESULT_NULL);
    }


    /*
     * 検索前の設定を行う。
     */
    if (yomi != NULL) {
        /*
         * 新規解析なので解析環境を初期化する
         */
        if (option != NULL) {
            /* 予測候補数上限値チェック */
            if (option->forecast_learn_limit > MAX_FORECAST_LEARN_LIMIT) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ANALYZE, NJ_ERR_PARAM_ILLEGAL_LIMIT);
            }
            if (option->forecast_limit > MAX_FORECAST_LIMIT) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ANALYZE, NJ_ERR_PARAM_ILLEGAL_LIMIT);
            }
            if (option->forecast_learn_limit > option->forecast_limit) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ANALYZE, NJ_ERR_PARAM_ILLEGAL_LIMIT);
            }
            /* 読み最小文字数、最大文字数のチェック */
            if (option->char_min > option->char_max) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ANALYZE, NJ_ERR_PARAM_ILLEGAL_CHAR_LEN);
            }
            /* 読み最小文字数、最大文字数のチェック */
            if ((option->char_min > NJ_MAX_LEN) ||
                (option->char_max > NJ_MAX_LEN)) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ANALYZE, NJ_ERR_PARAM_ILLEGAL_CHAR_LEN);
            }
            /* 逐次単文節変換指定区切り位置のチェック */
            if (option->in_divide_pos > NJ_MAX_LEN) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ANALYZE, NJ_ERR_PARAM_VALUE_INVALID);
            }
            env->option = *option;
        } else {
            /* 予測オプションが設定されていない場合は、デフォルト値で動作 */
            env->option.forecast_learn_limit = NJ_DEFAULT_FORECAST_LEARN_LIMIT;
            env->option.forecast_limit       = NJ_DEFAULT_FORECAST_LIMIT;
            env->option.char_min             = NJ_DEFAULT_CHAR_MIN;
            env->option.char_max             = NJ_DEFAULT_CHAR_MAX;
            env->option.mode                 = NJ_DEFAULT_MODE;
            env->option.in_divide_pos        = NJ_DEFAULT_IN_DIVIDE_POS;
            env->option.out_divide_pos       = NJ_DEFAULT_OUT_DIVIDE_POS;
        }


        /* 入力文字配列長のチェックを行う */
        yomiLen = nj_strlen(yomi);
        if (yomiLen > NJ_MAX_LEN) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ANALYZE, NJ_ERR_PARAM_YOMI_SIZE);
        }

        /* 辞書セットが変更された場合はキャッシュ管理領域とキャッシュ文字列をクリアする */
        if (env->dics != dics) {
            for (diccnt = 0; diccnt < NJ_MAX_DIC; diccnt++) {
                dic_info = &(dics->dic[diccnt]);
                if ((dic_info->handle != NULL) && (dic_info->srhCache != NULL)) {
                    dic_info->srhCache->statusFlg = 0;
                    dic_info->srhCache->viewCnt = 0;
                    dic_info->srhCache->keyPtr[0] = 0;
                    dic_info->srhCache->keyPtr[1] = 0;
                }
            }
            INIT_KEYWORD_IN_NJ_DIC_SET(dics);
        }

        /* 解析環境(辞書,表記など) */
        env->dics                   = dics;
        cond                        = &(env->cursor.cond);
        njd_init_search_condition(cond);
        cond->yomi                  = yomi;
        cond->ylen                  = yomiLen;
        cond->charset               = charset;
        cond->mode                  = NJ_CUR_MODE_FREQ; /* 評価部で利用するときは頻度順のみ */

        /* 解析状況 */
        env->type                   = NJ_ANALYZE_INITIAL;
        env->yomi                   = yomi;
        env->yomi_len               = yomiLen;
        env->level                  = 0;
        env->no                     = 0;
        env->learn_cnt              = 0;
        env->previous_selection_cnt = iwnn->previous_selection.count; /* 前確定情報 保持数を格納 */

        dics->mode = NJ_CACHE_MODE_VALID; /* キャッシュ検索モードを有効にする */

        /*
         * 入力文字列とキャッシュ検索用キーワード文字列の比較を行い、
         * 検索キャッシュの初期化処理を行う
         */
        p_yomi = cond->yomi;
        p_key  = dics->keyword;
        initst = 0;
        cond->yclen = nj_charlen(cond->yomi);
        for (clrcnt = 0; clrcnt < cond->yclen; clrcnt++) {
            if (nj_charncmp(p_yomi, p_key, 1) != 0) {
                break;
            }
            p_yomi += NJ_CHAR_LEN(p_yomi);
            p_key  += NJ_CHAR_LEN(p_key);
        }

        if (clrcnt != 0) {
            initst = clrcnt + 1;
        } else {
            initst = 0;
        }

        kw_len = nj_charlen(dics->keyword);
        if (kw_len >= cond->yclen) {
          inited = kw_len + 1;
        } else {
          inited = cond->yclen + 1;
        }
        for (diccnt = 0; diccnt < NJ_MAX_DIC; diccnt++) {
            dic_info = &(dics->dic[diccnt]);
            pCache = dic_info->srhCache;
            if ((dic_info->handle != NULL) && (pCache != NULL)) {
                /* キャッシュ溢れ発生判定 */
                if (NJ_GET_CACHEOVER_FROM_SCACHE(pCache)) {
                    /* キャッシュ溢れ発生位置を探す */
                    for (cacheOverKeyPtr = 0; cacheOverKeyPtr < kw_len; cacheOverKeyPtr++) {
                        if (pCache->keyPtr[cacheOverKeyPtr] == pCache->keyPtr[cacheOverKeyPtr + 1] ) {
                            break;
                        }
                    }
                    cacheOverKeyPtr++;

                    /* キャッシュ溢れ発生位置と不一致文字の位置を比較しより小さい方からクリアを開始する */
                    if (cacheOverKeyPtr < initst) {
                        clrcnt = cacheOverKeyPtr;/*NCH_FB*/
                    } else {
                        clrcnt = initst;
                    }
                    for (; clrcnt < inited; clrcnt++) {
                        pCache->keyPtr[clrcnt] = 0x0000;
                    }
                    /* 検索キャッシュの整合性チェック */
                    for (clrcnt = 1; clrcnt < inited; clrcnt++ ) {
                        if ((pCache->keyPtr[clrcnt - 1] > pCache->keyPtr[clrcnt]) &&
                            (pCache->keyPtr[clrcnt] != 0)) {
                            clear_flg(dics, env->option.mode); /*NCH_FB*/
                            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ANALYZE, NJ_ERR_CACHE_BROKEN); /*NCH_FB*/
                        }
                    }
                    NJ_UNSET_CACHEOVER_TO_SCACHE(pCache);
                } else {
                    for (clrcnt = initst; clrcnt < inited; clrcnt++) {
                        pCache->keyPtr[clrcnt] = 0x0000;
                    }
                    /* 検索キャッシュの整合性チェック */
                    for (clrcnt = 1; clrcnt < inited; clrcnt++ ) {
                        if ((pCache->keyPtr[clrcnt - 1] > pCache->keyPtr[clrcnt]) &&
                            (pCache->keyPtr[clrcnt] != 0)) {
                            clear_flg(dics, env->option.mode);
                            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ANALYZE, NJ_ERR_CACHE_BROKEN);
                        }
                    }
                }
                if ((env->option.mode & NJ_NO_CLEAR_LEARN_CACHE) == 0) {
                    /* 学習辞書検索キャッシュクリア要の場合 */
                    if (NJ_GET_DIC_TYPE_EX(dic_info->type, dic_info->handle) == NJ_DIC_TYPE_LEARN) {
                        /*
                         * キャッシュエリア先頭にフラグを書く
                         * フラグを書くことにより、本検索キャッシュ内をクリアする処理を行う
                         */
                        pCache->keyPtr[0] = 0xFFFF;
                    }
                }
            } /* else {} */
        }

        /* 入力文字列をキャッシュ検索用キーワードに保存する */
        nj_strcpy(dics->keyword, cond->yomi);

        /* 同表記チェックバッファ */
        ret = nje_clear_homonym_buf(iwnn);      /* 常に0を返す */

        /*
         * 予測結果を変換部の全候補取得で使用禁止とする為に、
         * 変換部揮発領域のクリアを行う。
         */
        ret = njc_init_conv(iwnn);  /* 常に0を返す */

        /* 解析条件を初期化する */
        /* この時点で yomi == NULL はない */
        if (yomiLen == 0) {
            /* 読み無し予測 */
            /* 繋がり予測結果保持情報をクリアする */
            iwnn->relation_results_count = 0;

            lword = NULL;
            while (env->previous_selection_cnt > 0) {
                /* 読みなし予測： 直前の njx_select 結果を用いる */
                lword = nje_get_previous_selection(iwnn, env->previous_selection_cnt, NJ_PREVIOUS_SELECTION_NORMAL);
                env->previous_selection_cnt--;
                if (lword != NULL) {
                    /* 前確定情報が生成できた場合 */
                    break;
                }
            }
            if (lword == NULL) {
                if (iwnn->state.system[NJ_CAT_FIELD_HEAD] <= 0) {
                    /* 前確定情報無し ＆ 状況設定：文頭がOFFの場合 */
                    env->type  = NJ_ANALYZE_END;
                    env->level = -1;
                    env->no    = 0;
                    clear_flg(dics, env->option.mode);
                    return 0;
                } else {
                    /* 
                     * 前確定情報がない場合でも、状況設定：文頭ON時は、
                     * 文頭の読み無し予測を行う
                     */
                    env->prev_hinsi = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_YOSOKU], NJ_HINSI_BUNTOU_B);
                    env->cursor.cond.kanji = env->cursor.cond.yomi;
                }
            } else {
                /* 前確定情報の品詞条件を保存する */
                env->prev_hinsi = lword->b_hinsi;
                /* 検索条件(読み、表記)を設定する */
                env->cursor.cond.yomi  = lword->yomi;
                env->cursor.cond.ylen  = lword->yomi_len;
                env->cursor.cond.kanji = lword->hyouki;
                /* 状況カテゴリ情報を保存する */
                env->cursor.cond.attr  = lword->attr;
            }
            /* 検索方法：繋がり検索を設定 */
            env->search_op  = NJ_CUR_OP_LINK;

            /* 学習準備を行うため、学習辞書に必要領域の確保する。*/
            ret = njd_l_make_space(iwnn, NJ_MAX_PHRASE, 1);
            if (ret < 0) {
                clear_flg(dics, env->option.mode); /*NCH_FB*/
                return ret; /*NCH_FB*/
            }

        } else {
            /* 通常予測 */
            /* 直前の njx_select 結果を用いる */
            lword = nje_get_previous_selection(iwnn, 0, NJ_PREVIOUS_SELECTION_NORMAL);
            if (lword == NULL) {
                /* 前確定情報がない */
                if (((env->option.mode & NJ_YOMINASI_ON) != 0) &&
                    (iwnn->state.system[NJ_CAT_FIELD_HEAD] > 0)) {
                    env->prev_hinsi = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_YOSOKU], NJ_HINSI_BUNTOU_B);
                } else {
                    env->prev_hinsi = NULL;
                    env->option.mode &= ~NJ_YOMINASI_ON;
                }
            } else {
                /* 前確定情報の品詞条件を保存する */
                env->prev_hinsi = lword->b_hinsi;
                /* 状況カテゴリ情報を保存する */
                env->cursor.cond.attr = lword->attr;
            }
            /* 検索方法：前方一致検索を設定 */
            env->search_op  = NJ_CUR_OP_FORE;

            /* 学習準備を行うため、学習辞書に必要領域の確保する。*/
            ret = njd_l_make_space(iwnn, NJ_MAX_PHRASE, 1);
            if (ret < 0) {
                clear_flg(dics, env->option.mode);
                return ret;
            }
        }

        /* 抽出方法を検索条件に設定する */
        cond->operation = env->search_op;

        /* 検索状態を変更する */
        ret = analyze_mode_change(iwnn);

        /* 前方一致検索数も最小値(0)の場合、検索条件を変更 */
        if (((env->type == NJ_ANALYZE_AI_YOSOKU) || (env->type == NJ_ANALYZE_NEXT_YOSOKU) ||
             (env->type == NJ_ANALYZE_FORWARD_SEARCH) || (env->type == NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI)) 
              && (env->option.forecast_limit == 0)) {
            /* 検索状態を変更する */
            ret = analyze_mode_change(iwnn);
            env->level = 0;
        }

        /* 複合語予測領域更新処理 */
        if (iwnn->option_data.ext_mode & NJ_OPT_FORECAST_COMPOUND_WORD) {
            /* オプション設定に複合語予測学習機能ONされている場合 */
            ret = cmpdg_info_update(iwnn);
            if (ret < 0) {
                return ret; /*NCH_FB*/
            }
        }

    } else {
        /*
         * 新規解析後なので、新規解析時の解析環境を使用する
         */
        if (env->yomi == NULL) {
            clear_flg(dics, env->option.mode);
            /* njf_init_analyze後に、前条件がなかった */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_ANALYZE, NJ_ERR_NOT_SELECT_YET);
        }
        /* 読み文字列配列長を再設定する */
        yomiLen = nj_strlen(env->yomi);
    }

    /*
     * 検索処理を行う
     */
    loop_count = 0;
    while ((++loop_count) <= NJX_ANALYZE_LOOP_LIMIT) {
        ret = analyze_body(iwnn, result);

        /* analyze_bodyからの、再解析要求(boomerang状態) */
        if (env->level == -1) {
            env->level = 0;
            continue;
        }

        if (ret <= 0) {
            /* エラー発生 or 候補なし */
            clear_flg(dics, env->option.mode);
            return ret;
        }

        if ((NJ_GET_RESULT_FUNC(result->operation_id) == NJ_FUNC_ZENKOUHO) ||
            (NJ_GET_RESULT_FUNC(result->operation_id) == NJ_FUNC_ZENKOUHO_HEAD)) {
            env->no++;
            break;
        }


        if (iwnn->option_data.phase2_filter != NULL) {
            /* 候補フィルタリングが設定されている場合のみ動作 */
            ph2_filter_message.result = result;
            ph2_filter_message.option = iwnn->option_data.phase2_option;
            phase2_filter_if = (NJ_PHASE2_FILTER_IF)(iwnn->option_data.phase2_filter);
            if ((*phase2_filter_if)(iwnn, &ph2_filter_message) == 0 ) {
                continue;
            }
        }

        /* 重複候補を削除する */
        ret = nje_append_homonym(iwnn, result, yomiLen, &offset);
        if (ret == 0) { /* 同表記なし */
            /* 候補番号・文節番号を付加する */
            env->no++;
            break;
        } else if (ret < 0) {
            clear_flg(dics, env->option.mode);
            /* エラー発生 */
            if (NJ_GET_ERR_CODE(ret) == NJ_ERR_BUFFER_NOT_ENOUGH) {
                /* 同表記バッファ溢れ時は、これ以上候補を返せない */
                return 0; /*NCH*/
            }
            return ret;
        }
    }

    if (loop_count > NJX_ANALYZE_LOOP_LIMIT) {
        /* ループ回数がリミットを超えたとき
         * 「候補無し」扱いにする。
         */
        clear_flg(dics, env->option.mode); /*NCH*/
        return 0; /*NCH*/
    }

    /* 読みなし予測絞り込みバッファリング処理 */
    if ((env->search_op == NJ_CUR_OP_LINK) &&
        (iwnn->relation_results_count < NJ_MAX_RELATION_RESULTS)) {
        /* 読み無し予測の場合、絞込予測検索用に候補をバッファリングする */
        iwnn->relation_results[iwnn->relation_results_count] = *result;
        iwnn->relation_results_count++;
    }

    /* 予測入力時の処理 */
    if ((env->type == NJ_ANALYZE_NEXT_YOSOKU) ||
        (env->type == NJ_ANALYZE_FORWARD_SEARCH) ||
        (env->type == NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI)) {

        /* 学習辞書の候補の場合の処理 */
        if ((result->word.stem.loc.handle != NULL) &&
            (NJ_GET_DIC_TYPE_EX(result->word.stem.loc.type, result->word.stem.loc.handle)
             == NJ_DIC_TYPE_LEARN)) {
            /* 学習辞書からの予測候補の場合、候補数を加算する処理を行う */
            env->learn_cnt++;
            if (env->learn_cnt >= env->option.forecast_learn_limit) {
                /* すでに学習辞書から取得する予測候補数を満たしている場合は、
                 * 内部辞書セットの学習辞書handleをNULLにする。
                 */
                for (i = 0; i < NJ_MAX_DIC; i++) {
                    dic_info = &(env->search_dics.dic[i]);
                    if (dic_info->handle != NULL) {
                        if (NJ_GET_DIC_TYPE_EX(dic_info->type, dic_info->handle) == NJ_DIC_TYPE_LEARN) {
                            /* 学習辞書の検索カーソルを強制的に検索終了にする */
                            njd_set_cursor_search_end(&(env->cursor), (NJ_UINT16)i);
                        }
                    }
                }
            }
        }

        /* 予測候補の制限条件：NJ_FORECAST_LIMIT の判定 */
        if (env->no >= env->option.forecast_limit) {
            /* 候補が制限以上になったとき、次の検索状態に変更する */
            ret = analyze_mode_change(iwnn);
            env->level = 0;
        }
    }

    /* 連文節変換結果の場合、out_divide_posを更新する。 */
    if ((option != NULL) &&
        (((env->option.mode & NJ_HEAD_CONV_ON) != 0) || ((env->option.mode & NJ_HEAD_CONV2_ON) != 0) )&& 
        (env->type >= NJ_ANALYZE_CONVERSION_MULTIPLE) ) {
        option->out_divide_pos = env->option.out_divide_pos;
    }

    clear_flg(dics, env->option.mode);
    return 1;
}


/**
 * 評価部の揮発領域を初期化する。
 *
 * @param[in,out] iwnn  解析情報クラス
 *
 * @return 必ず0が返る
 */
NJ_UINT8 njf_init_analyze(NJ_CLASS *iwnn) {
    NJ_INT16 i;
    NJ_ANALYZE_ENV *env = &iwnn->environment;
    NJD_AIP_WORK *aipwork = (NJD_AIP_WORK*)iwnn->option_data.aip_work;



    if (aipwork != NULL) {
        /* AI予測用ワーク領域の初期化 */
        aipwork->save_cnt = 0;
        aipwork->status = 0;
    }

    /* アプリから指定された辞書セット*/
    env->dics = NULL;

    /* 検索用のテンポラリ辞書セット & 辞書引き時の検索カーソル */
    env->search_dics.rHandle[NJ_MODE_TYPE_HENKAN] = NULL;
    env->search_dics.rHandle[NJ_MODE_TYPE_YOSOKU] = NULL;
    env->search_dics.rHandle[NJ_MODE_TYPE_MORPHO] = NULL;
    INIT_KEYWORD_IN_NJ_DIC_SET(&(env->search_dics));
    env->search_dics.mode = 0;
    njd_init_search_condition(&(env->cursor.cond));
    for (i = 0; i < NJ_MAX_DIC; i++) {
        njd_clear_dicinfo(&(env->search_dics.dic[i]));
        njd_init_search_location_set(&(env->cursor.loctset[i]));
    }

    /* 辞書引き時の検索条件 */
    env->search_op = 0;

    /* アプリから指定されたよみ */
    env->yomi = NULL;

    /* 前確定候補の後品詞 */
    env->prev_hinsi = 0;

    /* 現状の解析状態 */
    env->type = NJ_ANALYZE_END;
    env->level = 0;
    env->no = 0;

    /* 1文節変換結果保持用 */
    for (i = 0; i < 2; i++) {
        env->conv_buf.single_keep[i].operation_id = 0;
        njd_init_word(&(env->conv_buf.single_keep[i].word));
    }
    env->conv_buf.single_keep_len = 0;

    /* 連文節変換結果保持用 */
    for (i = 0; i < NJ_MAX_PHRASE; i++) {
        env->conv_buf.multiple_keep[i].operation_id = 0;
        njd_init_word(&(env->conv_buf.multiple_keep[i].word));
    }
    env->conv_buf.multiple_keep_len = 0;

    return 0;
}


/**
 * 評価部で保持している連文節変換、1文節変換の処理結果を取得する
 *
 * 未取得状態でも何らかの結果が返る。
 *
 * @param[in,out] iwnn    解析情報クラス
 * @param[in]     type      1文節変換結果なら1, それ以外なら連文節変換結果
 * @param[out]    result    処理結果格納アドレス
 *
 * @return  処理結果数。1以上が必ず返る
 */
NJ_UINT16 njf_get_keep_result(NJ_CLASS *iwnn, NJ_UINT8 type, NJ_RESULT **result) {


    if (type == 1) {
        *result = &(iwnn->environment.conv_buf.single_keep[0]);
        return iwnn->environment.conv_buf.single_keep_len;
    }

    *result = &(iwnn->environment.conv_buf.multiple_keep[0]);
    return iwnn->environment.conv_buf.multiple_keep_len;
}


/**
 * 指定した処理結果の候補文字列を取得する
 *
 * @param[in,out] iwnn      解析情報クラス
 * @param[in]     result      処理結果
 * @param[out]    buf         候補文字列の出力先
 * @param[in]     buf_size    bufのbyteサイズ
 *
 * @retval >0 取得できた文字配列長を返す
 * @retval <0 エラー(バッファ不足)
 */
NJ_INT16 njf_get_candidate(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_CHAR *buf,
                           NJ_UINT16 buf_size) {
    NJ_INT16 len;
    NJ_UINT16 total_len;
    NJ_UINT16 result_num;
    NJ_UINT16 func_type;


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
        switch (func_type) {
        case NJ_FUNC_SEARCH:
        case NJ_FUNC_NEXT:
        case NJ_FUNC_RELATION:
            /* 辞書引き部に問い合わせる */
            len = njd_get_candidate(iwnn, result, buf, buf_size);
            break;
        default:
            /* 変換部に問い合わせる */
            len = njc_get_candidate(iwnn, result, buf, buf_size);
            break;
        }

        if (len < 0) {
            return len;
        }

        total_len += len;
        buf_size  -= (len * sizeof(NJ_CHAR));
        buf       += len;
        result++;
    }
    return total_len;
}


/**
 * 指定した処理結果のよみ文字列を取得する(エンジン内部向け)
 *
 * @attention エラーチェックはない。
 *
 * @param[in,out] iwnn      解析情報クラス
 * @param[in]     result      処理結果
 * @param[out]    buf         よみ文字列の出力先
 * @param[in]     buf_size    bufのbyteサイズ
 *
 * @retval >0 取得できた文字配列長
 * @retval <0 エラー(バッファ不足)
 */
NJ_INT16 njf_get_stroke(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_CHAR *buf,
                        NJ_UINT16 buf_size) {
    NJ_INT16 len;
    NJ_UINT16 total_len;
    NJ_UINT16 result_num;
    NJ_UINT16 func_type;


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
        switch (func_type) {
        case NJ_FUNC_SEARCH:
        case NJ_FUNC_NEXT:
        case NJ_FUNC_RELATION:
            /* 辞書引き部に問い合わせる */
            len = njd_get_stroke(iwnn, result, buf, buf_size);
            break;
        default:
            /* 変換部に問い合わせる */
            len = njc_get_stroke(iwnn, result, buf, buf_size);
            break;
        }

        if (len < 0) {
            return len;/*NCH*/
        }

        total_len += len;
        buf_size  -= (len * sizeof(NJ_CHAR));
        buf       += len;
        result++;
    }

    return total_len;
}


/**
 * 学習辞書の検索キャッシュをクリアする
 *
 * @param[in,out]  dics   辞書セット
 * @param[in]      mode   解析制限
 *
 * @retval 0 フラグクリア無し
 * @retval 1 フラグクリア有り
 */
static NJ_INT16 clear_flg(NJ_DIC_SET *dics, NJ_UINT16 mode) {
    NJ_INT16            ret = 0;
    NJ_SEARCH_CACHE     *pCache;
    NJ_UINT16           i;


    if (mode & NJ_NO_CLEAR_LEARN_CACHE) {
        /* 学習辞書検索キャッシュクリア不要の場合 */
        return ret;
    }

    /* 学習辞書検索キャッシュクリア要の場合 */
    for (i = 0; i < NJ_MAX_DIC; i++) {
        if (dics->dic[i].handle != NULL) {
            pCache = dics->dic[i].srhCache;
            if (pCache != NULL) {
                if ((NJ_GET_DIC_TYPE_EX(dics->dic[i].type, dics->dic[i].handle) == NJ_DIC_TYPE_LEARN) &&
                    (pCache->keyPtr[0] == 0xFFFF)) {
                    /* 辞書が学習辞書で、かつフラグON状態の場合 */
                    /* キャッシュエリア先頭のフラグをクリアする */
                    pCache->keyPtr[0] = 0x0000;
                    ret = 1;
                }
            }
        }
    }

    return ret;
}


/**
 * 各解析処理の流れを判定、設定する。
 *
 * @param[in,out] iwnn  解析情報クラス
 *
 * @retval 1 正常終了
 * @retval 0 設定エラー
 */
static NJ_INT16 analyze_mode_change(NJ_CLASS *iwnn) {
    NJ_ANALYZE_ENV   *env = &(iwnn->environment);
    NJD_AIP_WORK *aipwork = (NJD_AIP_WORK*)iwnn->option_data.aip_work;
    NJ_UINT16        tmp_type = env->type;


    /* 終了の場合 */
    if (tmp_type == NJ_ANALYZE_END) {
        /* 終 了 */
        return 1;
    }

    /* AI予測確認 */
    if ((tmp_type == NJ_ANALYZE_INITIAL) && (env->search_op == NJ_CUR_OP_LINK) && (env->yomi_len == 0)) {
        if (aipwork != NULL) {
            /* AI予測 */
            env->type = NJ_ANALYZE_AI_YOSOKU;
        } else {
            /* 読みなし予測： 読みなし予測(予測繋がり) */
            env->type = NJ_ANALYZE_NEXT_YOSOKU;
        }
        return 1;
    }

    /* 繋がり検索確認 */
    if (tmp_type == NJ_ANALYZE_AI_YOSOKU) {
        /* 読みなし予測： 読みなし予測(予測繋がり) */
        env->type = NJ_ANALYZE_NEXT_YOSOKU;
        return 1;
    }

    /* 圧縮(予測)辞書制御確認 */
    if (tmp_type == NJ_ANALYZE_INITIAL) {
        /* 次解析判定 */
        if (env->option.mode & NJ_YOMINASI_ON) {
            /* 次は前方一致検索(読み無し予測含む) */
            env->type = NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI;
        } else {
            /* 次は前方一致検索 */
            env->type = NJ_ANALYZE_FORWARD_SEARCH;
        }
        return 1;
    } else {
        /* 判定条件を前方一致検索に変更 */
        tmp_type = NJ_ANALYZE_FORWARD_SEARCH;
    }

    /* 読み長指定予測入力の制御処理 */
    if ((env->cursor.cond.yclen < env->option.char_min) ||
        (env->cursor.cond.yclen > env->option.char_max) ) {
        /* 
         * 読み長指定予測入力時に以降の変換処理が不要な場合に、
         * 本処理にて処理を抜けるようにする
         */
        /* 検索終了を設定 */
        env->type = NJ_ANALYZE_END;
        return 1;
    }

    /* 入力文字と逐次単文節変換指定文節区切り位置チェック */
    if (((env->option.mode & NJ_HEAD_CONV_ON) || (env->option.mode & NJ_HEAD_CONV2_ON)) &&
        (env->yomi_len < env->option.in_divide_pos)) {
        /**
         * 入力文字よりも逐次単文節変換指定文節区切り位置が大きい場合、
         * 解析処理を終了するようにする。(検索終了を設定)
         */
        env->type = NJ_ANALYZE_END;
        return 1;
    }

    /* 連文節制御確認 */
    if (((env->type <= NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI) && (env->type != NJ_ANALYZE_NEXT_YOSOKU)) &&
        ((tmp_type == NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI) || (tmp_type == NJ_ANALYZE_FORWARD_SEARCH)) &&
        (((env->option.mode & NJ_HEAD_CONV_ON) != 0) || ((env->option.mode & NJ_HEAD_CONV2_ON) != 0) || ((env->option.mode & NJ_NO_RENBUN) == 0)) && (env->yomi_len != 0)) {
        /* 予測候補数確認 */
        /* 設定された連文節変換自動開始候補数が入力文字列に対する予測候補数を
         * 下回った場合に連文節変換を行う
         * ただし、逐次単文節変換有効時で文節区切り位置指定がある場合は、
         * 連文節変換を行う。
         */
        if ((iwnn->option_data.autoconv_cnt >= env->no) ||
            ((((env->option.mode & NJ_HEAD_CONV_ON) != 0) || ((env->option.mode & NJ_HEAD_CONV2_ON) != 0)) && (env->option.in_divide_pos != 0))) {
            /* 次は連文節変換 */
            env->type = NJ_ANALYZE_CONVERSION_MULTIPLE;
            return 1;
        } else {
            if (((env->option.mode & NJ_HEAD_CONV_ON) != 0) || ((env->option.mode & NJ_HEAD_CONV2_ON) != 0)) {
                /* 逐次単文節変換有効時に連文節変換をしない場合、
                 * 入力文字列長を文節区切り位置として設定する。
                 */
                env->option.out_divide_pos = (NJ_UINT8)env->yomi_len;
            }
            /* 判定条件を連文節変換に変更 */
            tmp_type = NJ_ANALYZE_CONVERSION_MULTIPLE;
        }
    } else {
        /* 判定条件を連文節変換に変更 */
        tmp_type = NJ_ANALYZE_CONVERSION_MULTIPLE;
    }

    /* 単文節制御確認 */
    if (((env->type <= NJ_ANALYZE_CONVERSION_MULTIPLE) && (env->type != NJ_ANALYZE_NEXT_YOSOKU)) &&
        (tmp_type == NJ_ANALYZE_CONVERSION_MULTIPLE) && ((env->option.mode & NJ_NO_TANBUN) == 0) && (env->yomi_len != 0)) {
        /* 次は単文節変換 */
        env->type = NJ_ANALYZE_CONVERSION_SINGLE;
        return 1;
    } else {
        /* 判定条件を単文節変換に変更 */
        tmp_type = NJ_ANALYZE_CONVERSION_SINGLE;
    }

    /* 全候補制御確認 */
    if (((env->type <= NJ_ANALYZE_CONVERSION_SINGLE) && (env->type != NJ_ANALYZE_NEXT_YOSOKU)) &&
        (tmp_type == NJ_ANALYZE_CONVERSION_SINGLE) && ((env->option.mode & NJ_NO_ZEN) == 0) && (env->yomi_len != 0)) {
        /* 次は全候補取得 */
        env->type = NJ_ANALYZE_COMPLETE;
        return 1;
    } else {
        /* 判定条件を全候補に変更 */
        tmp_type = NJ_ANALYZE_COMPLETE;
    }

    if (((env->option.mode & NJ_HEAD_CONV_ON) || (env->option.mode & NJ_HEAD_CONV2_ON)) && ((env->option.mode & NJ_NO_RENBUN) == 0)) {
        /* 先頭文節全候補制御確認 */
        if (((env->type <= NJ_ANALYZE_COMPLETE) && (env->type != NJ_ANALYZE_NEXT_YOSOKU)) &&
            (tmp_type == NJ_ANALYZE_COMPLETE) && (env->yomi_len != 0) &&
            (env->option.out_divide_pos != 0) &&
            (env->option.out_divide_pos >= env->option.char_min) &&
            (((env->option.mode & NJ_HEAD_CONV_ON) && (env->option.out_divide_pos < env->yomi_len)) ||
             ((env->option.mode & NJ_HEAD_CONV2_ON) && (env->option.out_divide_pos <= env->yomi_len)))) {
            /* 次は先頭文節全候補取得 */
            env->type = NJ_ANALYZE_COMPLETE_HEAD;
            return 1;
        } else {
            /* 判定条件を先頭文節全候補取得に変更 */
            tmp_type = NJ_ANALYZE_COMPLETE_HEAD;
        }
    } else {
        /* 判定条件を先頭文節全候補取得に変更 */
        tmp_type = NJ_ANALYZE_COMPLETE_HEAD;
    }

    /* 全候補(擬似候補)制御確認 */
    if (((env->type <= NJ_ANALYZE_COMPLETE_HEAD) && (env->type != NJ_ANALYZE_NEXT_YOSOKU)) &&
        (tmp_type == NJ_ANALYZE_COMPLETE_HEAD) && ((env->option.mode & NJ_NO_ZEN) == 0) && (env->yomi_len != 0)) {
        /* 次は全候補(擬似候補)取得 */
        env->type = NJ_ANALYZE_COMPLETE_GIJI;
        return 1;
    } else {
        /* 判定条件を全候補(擬似候補)に変更 */
        tmp_type = NJ_ANALYZE_COMPLETE_GIJI;
    }

    /* 終了確認 */
    if ((tmp_type == NJ_ANALYZE_COMPLETE) ||
        (tmp_type == NJ_ANALYZE_NEXT_YOSOKU) ||
        (tmp_type == NJ_ANALYZE_COMPLETE_HEAD) ||
        (tmp_type == NJ_ANALYZE_COMPLETE_GIJI)) {
        /* 検索終了を設定 */
        env->type = NJ_ANALYZE_END;
        return 1;
    }

    return NJ_SET_ERR_VAL(NJ_FUNC_ANALYZE_MODE_CHANGE, NJ_ERR_PARAM_OPERATION); /*NCH*/

}


/**
 * 複合語予測情報更新処理
 *
 * @param[in,out] iwnn  解析情報クラス
 *
 * @retval 1  更新あり
 * @retval 0  更新なし
 * @retval <0 エラー
 */
static NJ_INT16 cmpdg_info_update(NJ_CLASS *iwnn) {

    NJ_INT16       ret = 0;
    NJ_UINT8       i, j;
    NJ_UINT8       add_cnt, add_idx, add_result;
    NJ_UINT8       ref_cnt, ref_idx, ref_result, work_idx;
    NJ_CMPDG_INFO *cmpdg_info = &(iwnn->cmpdg_info);

    if (cmpdg_info->add_count == 0) {
        /* 追加複合語が存在しないため、バッファの更新は行わない。 */
        return ret;
    }

    if ((iwnn->environment.option.char_min == NJ_DEFAULT_CHAR_MIN) &&
        (iwnn->environment.option.char_max == NJ_DEFAULT_CHAR_MAX)) {
        /* 
         * ワイルドカード予測実施時以外の場合に、
         * トグル時の動作を考慮し、入力文字配列長の変化をチェックする
         */
        for (i = 0; i < NJ_MAX_CMPDG_RESULTS; i++) {
            if ((cmpdg_info->cmpdg_data[i].status == NJ_CMPDG_STATUS_ADD) &&
                (cmpdg_info->cmpdg_data[i].index  != NJ_CMPDG_BLANK_INDEX)) {
                if (iwnn->environment.cursor.cond.ylen == cmpdg_info->cmpdg_data[i].yomi_toplen) {
                    /* 入力文字配列長に変化がないため、更新処理は行わない */
                    
                    /* 追加複合語を検出した位置から後方の配列を初期化する */
                    iwnn->cmpdg_info.add_count = 0;
                    for (j = i; j < NJ_MAX_CMPDG_RESULTS; j++) {
                        if (cmpdg_info->cmpdg_data[j].status != NJ_CMPDG_STATUS_REFER) {
                            iwnn->cmpdg_info.cmpdg_data[j].index = NJ_CMPDG_BLANK_INDEX;
                        }
                    }
                    return 0; /* 更新なし */
                } else {
                    /* 入力文字配列長に変化があるため、更新処理を継続する。 */
                    ret = 1;
                    break;
                }
            }
        }
    } else {
        /* ワイルドカード予測実施時は、更新処理を継続する */
        ret = 1;
    }

    /* 
     * 更新処理の前処理実施
     * - 追加複合語を参照複合語に状態変更
     */
    add_cnt = 0;
    ref_cnt = 0;
    for (i = 0; i < NJ_MAX_CMPDG_RESULTS; i++) {
        if (cmpdg_info->cmpdg_data[i].index != NJ_CMPDG_BLANK_INDEX) {
            /* 更新処理用にインデックスを更新する */
            if (cmpdg_info->cmpdg_data[i].status == NJ_CMPDG_STATUS_REFER) {
                /* 参照複合語の場合 */
                cmpdg_info->cmpdg_data[i].index = (NJ_UINT8)(cmpdg_info->cmpdg_data[i].index + CMPDG_REF_SORT_COUNT);
                ref_cnt++;
            } else {
                /* 追加複合語の場合 */
                cmpdg_info->cmpdg_data[i].index = (NJ_UINT8)(cmpdg_info->cmpdg_data[i].index + CMPDG_ADD_SORT_COUNT);
                add_cnt++;
            }
        }
    }


    /* 不整合発生時の補正処理 */
    if (cmpdg_info->add_count != add_cnt) {
        cmpdg_info->add_count = add_cnt; /*NCH_FB*/
    }
    if (cmpdg_info->refer_count != ref_cnt) {
        cmpdg_info->refer_count = ref_cnt; /*NCH_FB*/
    }

    /* 更新処理 頻度順の上位20候補(NJ_MAX_REFER_CMPDG_RESULTS)に絞込みを行う */
    ref_cnt = 0;
    ref_result = 0;
    ref_idx = CMPDG_REF_SORT_COUNT;
    add_cnt = 0;
    add_result = 0;
    add_idx = CMPDG_ADD_SORT_COUNT;
    for (i = 0; i < NJ_MAX_REFER_CMPDG_RESULTS; i++) {
        /* 参照複合語の参照位置を更新 */
        if (ref_idx != 0xFF) {
            if ((i == 0) ||
                ((i != 0) && (cmpdg_info->cmpdg_data[ref_result].index != ref_idx))) {
                /* 初回もしくは、*/
                work_idx = 0xFF;
                for (j = 0; j < NJ_MAX_CMPDG_RESULTS; j++) {
                    if ((cmpdg_info->cmpdg_data[j].status == NJ_CMPDG_STATUS_REFER) &&
                        (cmpdg_info->cmpdg_data[j].index  != NJ_CMPDG_BLANK_INDEX)) {
                        if ((cmpdg_info->cmpdg_data[j].index < work_idx) &&
                            (cmpdg_info->cmpdg_data[j].index > ref_idx)) {
                            work_idx   = cmpdg_info->cmpdg_data[j].index;
                            ref_result = j;
                        }
                    }
                }
                ref_idx = work_idx;
                if (ref_idx != 0xFF) {
                    ref_cnt++;
                }
            }
        }

        /* 追加複合語の参照位置を更新 */
        if (add_idx != 0xFF) {
            if ((i == 0) ||
                ((i != 0) && (cmpdg_info->cmpdg_data[add_result].index != add_idx))) {
                /* 初回もしくは、*/
                work_idx = 0xFF;
                for (j = 0; j < NJ_MAX_CMPDG_RESULTS; j++) {
                    if ((cmpdg_info->cmpdg_data[j].status == NJ_CMPDG_STATUS_ADD) &&
                        (cmpdg_info->cmpdg_data[j].index  != NJ_CMPDG_BLANK_INDEX)) {
                        if ((cmpdg_info->cmpdg_data[j].index < work_idx) &&
                            (cmpdg_info->cmpdg_data[j].index > add_idx)) {
                            work_idx   = cmpdg_info->cmpdg_data[j].index;
                            add_result = j;
                        }
                    }
                }
                add_idx = work_idx;
                if (add_idx != 0xFF) {
                    add_cnt++;
                }
            }
        }

        /* 追加複合語、参照複合語の頻度を比較する */
        if ((ref_idx == 0xFF) && (add_idx == 0xFF)) {
            /* 両複合語から参照位置を取得できない場合は、ループを終了する */
            break;
        } else if (ref_idx == 0xFF) {
            /* 追加複合語を追加する。*/
            cmpdg_info->cmpdg_data[add_result].status = NJ_CMPDG_STATUS_REFER;
            cmpdg_info->cmpdg_data[add_result].index  = i + 1;
        } else if (add_idx == 0xFF) {
            /* 参照複合語を追加する。*/
            cmpdg_info->cmpdg_data[ref_result].status = NJ_CMPDG_STATUS_REFER;
            cmpdg_info->cmpdg_data[ref_result].index  = i + 1;
        } else {
            /* 
             * 頻度値比較を行い、頻度が高い複合語を優先する。
             * ただし、同頻度の場合は、追加複合語を優先する。
             */
            if (cmpdg_info->cmpdg_data[add_result].result.word.stem.hindo >=
                cmpdg_info->cmpdg_data[ref_result].result.word.stem.hindo) {
                /* 追加複合語を追加する。*/
                cmpdg_info->cmpdg_data[add_result].status = NJ_CMPDG_STATUS_REFER;
                cmpdg_info->cmpdg_data[add_result].index  = i + 1;
            } else {
                /* 参照複合語を追加する。*/
                cmpdg_info->cmpdg_data[ref_result].status = NJ_CMPDG_STATUS_REFER;
                cmpdg_info->cmpdg_data[ref_result].index  = i + 1;
            }
        }
    }
    cmpdg_info->refer_count = i;

    /* 絞込み対象にならなかった候補を削除する */
    for (i = 0; i < NJ_MAX_CMPDG_RESULTS; i++) {
        if (cmpdg_info->cmpdg_data[i].index > NJ_MAX_REFER_CMPDG_RESULTS) {
            cmpdg_info->cmpdg_data[i].status = NJ_CMPDG_STATUS_ADD;
            cmpdg_info->cmpdg_data[i].index  = NJ_CMPDG_BLANK_INDEX;
        }
    }
    cmpdg_info->add_count = 0;

    return ret;
}
