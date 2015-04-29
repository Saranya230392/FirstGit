/**
 * @file
 *   形態素解析部 API部
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
#include "njc.h"

/************************************************/
/*              define  宣  言                  */
/************************************************/

/************************************************/
/*              extern  宣  言                  */
/************************************************/

/************************************************/
/*              prototype  宣  言               */
/************************************************/

/************************************************/
/*              構 造 体 宣 言                  */
/************************************************/

/************************************************/
/*              外  部  変  数                  */
/************************************************/

/************************************************/
/*              static  変数宣言                */
/************************************************/
static void rlt_setMorphoGiji(NJ_RESULT *r, NJ_UINT16 len, NJ_UINT16 f_giji,
                              NJ_UINT16 r_giji, NJ_INT16 type);


/**
 * 擬似候補情報に設定(初期化)する
 *
 * @param[in] r      文節情報
 * @param[in] len    擬似候補長
 * @param[in] f_giji 擬似品詞 前品詞番号
 * @param[in] r_giji 擬似品詞 後品詞番号
 * @param[in] type   擬似候補タイプ
 *
 * @return なし
 */
static void rlt_setMorphoGiji(NJ_RESULT *r, NJ_UINT16 len, NJ_UINT16 f_giji,
                              NJ_UINT16 r_giji, NJ_INT16 type) {


    njd_init_word(&(r->word));
    r->operation_id &= ~NJ_FUNC_MASK;
    r->operation_id = (NJ_OP_MORPHOLIZE | NJ_FUNC_ZENKOUHO | NJ_DIC_GIJI | NJ_TYPE_GIJI_BIT);
    NJ_SET_YLEN_TO_STEM(&r->word, len);
    NJ_SET_KLEN_TO_STEM(&r->word, len);
    NJ_SET_FPOS_TO_STEM(&r->word, f_giji);
    NJ_SET_BPOS_TO_STEM(&r->word, r_giji);
    NJ_SET_FREQ_TO_STEM(&r->word, GIJI_GETSCORE(len));
    NJ_SET_TYPE_TO_STEM(&r->word, (NJ_UINT8)type);
}


/**
 * 形態素解析 分かち書き機能API
 *
 * 入力文字列に対して分かち書きを行う。
 *
 * @attention    入力文字列は終端がNJ_CHAR_NUL文字で終了していること。
 * @attention    解析結果格納バッファは、MM_MAX_MORPHO_LEN分の領域を呼出元で用意すること。
 * 
 * @param[in,out] iwnn        解析情報クラス
 * @param[in]     input       入力文字列
 * @param[out]    process_len 解析結果文字配列長
 * @param[out]    results     解析結果格納バッファ
 *
 * @retval  >0    resultsへ格納した解析結果数(=文節数)
 * @retval  0     inputが空文字列の時
 * @retval  <0    異常終了
 */
NJ_EXTERN NJ_INT16 mmx_split_word(NJ_CLASS *iwnn, NJ_CHAR *input,
                                  NJ_UINT8 *process_len, NJ_RESULT *results) {

    NJ_INT16 ret, i;
    NJ_RESULT *rptr;
    NJ_CHAR *cptr;
    NJ_UINT8 len;
#ifndef NJ_OPT_UTF16
    NJ_UINT8 cnt, tmp;
#endif /* NJ_OPT_UTF16 */
    NJ_UINT32 type;


    /*
     * 引数チェック
     *  ※ 下記以外の引数チェックは、njc_conv()で実施する
     */
    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_SPLIT_WORD, NJ_ERR_PARAM_ENV_NULL);
    }

    if (process_len == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_SPLIT_WORD, NJ_ERR_PARAM_PROCESS_LEN_NULL);
    } else {
        /* process_len を 0に初期化する */
        *process_len = 0;
    }

    /* 辞書セット内ルール辞書がNULLの場合はエラーとする */
    if (iwnn->dic_set.rHandle[NJ_MODE_TYPE_MORPHO] == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_SPLIT_WORD, NJ_ERR_NO_RULEDIC);
    }

    /*
     * 形態素解析用辞書セット構造体の作成
     */
    iwnn->tmp_dic_set.rHandle[NJ_MODE_TYPE_HENKAN] = iwnn->dic_set.rHandle[NJ_MODE_TYPE_MORPHO];
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
            if (iwnn->dic_set.dic[i].dic_freq[NJ_MODE_TYPE_MORPHO].high >=
                iwnn->dic_set.dic[i].dic_freq[NJ_MODE_TYPE_MORPHO].base ) {
                /* 使用辞書のみ設定する。使用しない辞書はすべて初期化 */
                type = NJ_GET_DIC_TYPE_EX(iwnn->dic_set.dic[i].type, iwnn->dic_set.dic[i].handle);
                switch (type) {
                case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
                case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書               */
                case NJ_DIC_TYPE_FUSION:                        /* 統合辞書               */
                case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
                case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
                case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
                case NJ_DIC_TYPE_JIRITSU:                       /* 自立語辞書             */
                case NJ_DIC_TYPE_FZK:                           /* 付属語辞書             */
                case NJ_DIC_TYPE_TANKANJI:                      /* 単漢字辞書             */
                case NJ_DIC_TYPE_CUSTOM_COMPRESS:               /* 圧縮カスタマイズ辞書   */
                case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:             /* 非圧縮カスタマイズ辞書 */
                case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:       /* 非圧縮カスタマイズ辞書(学習辞書変更) */
                case NJ_DIC_TYPE_USER:                          /* ユーザ辞書 */
                case NJ_DIC_TYPE_FORECONV:                      /* 予測変換辞書           */
                case NJ_DIC_TYPE_PROGRAM:                       /* 擬似辞書               */
                case NJ_DIC_TYPE_PROGRAM_FZK:                   /* 擬似辞書-付属語-       */
                    njd_copy_dicinfo(&(iwnn->tmp_dic_set.dic[i]), &(iwnn->dic_set.dic[i]), NJ_MODE_TYPE_MORPHO);
                    break;
                default:
                    break;
                }
            }
        }
    }


    /*
     * 解析実行
     */
    iwnn->njc_mode = NJC_MODE_MORPHO;   /* 3:形態素解析中 */

    ret = njc_conv(iwnn, &(iwnn->tmp_dic_set), input, MM_MAX_MORPHO_LEN, 0, results, 0);
    if (ret < 0) {
        iwnn->njc_mode = NJC_MODE_NOT_CONVERTED;        /* 0:初期状態 */
    } else if (ret > 0) {
        *process_len = 0;
        rptr = results;
        for (i = 0; i < ret; i++, rptr++) {
            *process_len += NJ_GET_YLEN_FROM_STEM(&(rptr->word)) +
                NJ_GET_YLEN_FROM_FZK(&(rptr->word));
        }
        /*
         * 半角カナの濁音と半濁音が分断されていた場合、
         * 清音部分を最終文節から削除する。
         */
        rptr = &results[ret - 1];
        if (NJ_GET_RESULT_DIC(rptr->operation_id) == NJ_DIC_GIJI) {
            /* 擬似文節 */
            if (NJ_GET_YLEN_FROM_FZK(&rptr->word) == 0) {
                /* 自立語のみ */

                /*
                 * 未解析の入力文字列先頭に、半角かなの濁音・半濁音記号が
                 * 存在するかチェックする
                 */
                len = NJ_GET_YLEN_FROM_STEM(&rptr->word);
                cptr = rptr->word.yomi + len;
                if (NJ_CHAR_IS_HANKANA_DAKUTEN(cptr)) {
                    /* 半角カナ 濁音記号 検出 */
#ifdef NJ_OPT_UTF16
                    cptr--;  /* 清音部分検査位置へ移動 */
#else /* NJ_OPT_UTF16 */
                    cptr = rptr->word.yomi;
                    tmp = 0;
                    cnt = 0;
                    /* 最終文節先頭候補文字列から検査し、最終文字の文字幅を取得 */
                    while (cnt < len) {
                        tmp = NJ_CHAR_LEN(cptr); /* 現在の文字のバイト数保存     */
                        cnt += tmp;              /* ループカウンタ更新           */
                        cptr += tmp;             /* 検査位置文字先頭位置設定     */
                    }
                    cptr -= tmp;
#endif /* NJ_OPT_UTF16 */
                    if (NJ_CHAR_IS_ENABLE_HANKANA_DAKUTEN(cptr)) {
                        /* 濁音として判定可能な清音検出 (カ/サ/タ/ハ行)*/
                        len--;
                        if (len) {
                            /* 最終文節から清音1文字分削除 */
                            NJ_SET_YLEN_TO_STEM(&rptr->word, len);
                            NJ_SET_KLEN_TO_STEM(&rptr->word, len);
                        } else {
                            /* １文節消滅 */
                            ret--;
                        }
                        *process_len -= 1; /* 解析長から1文字減算 */
                    }
                } else if (NJ_CHAR_IS_HANKANA_HANDAKUTEN(cptr)) {
                    /* 半角カナ 半濁音記号 検出 */
#ifdef NJ_OPT_UTF16
                    cptr--;  /* 清音部分検査位置へ移動 */
#else /* NJ_OPT_UTF16 */
                    cptr = rptr->word.yomi;
                    tmp = 0;
                    cnt = 0;
                    /* 最終文節先頭候補文字列から検査し、最終文字の文字幅を取得 */
                    while (cnt < len) {
                        tmp = NJ_CHAR_LEN(cptr); /* 現在の文字のバイト数保存     */
                        cnt += tmp;              /* ループカウンタ更新           */
                        cptr += tmp;             /* 検査位置文字先頭位置設定     */
                    }
                    cptr -= tmp;
#endif /* NJ_OPT_UTF16 */
                    if (NJ_CHAR_IS_ENABLE_HANKANA_HANDAKUTEN(cptr)) {
                        /* 半濁音として判定可能な清音検出 (ハ行)*/
                        len--;
                        if (len) {
                            /* 最終文節から清音1文字分削除 */
                            NJ_SET_YLEN_TO_STEM(&rptr->word, len);
                            NJ_SET_KLEN_TO_STEM(&rptr->word, len);
                        } else {
                            /* １文節消滅 */
                            ret--;
                        }
                        *process_len -= 1; /* 解析長から1文字減算 */
                    }
                }
            }
        }
    }
    return ret;
}


/**
 * 形態素解析 品詞グループ取得API
 *
 * 指定した NJ_RESULT の品詞グループを返す。
 *
 * @param[in,out]  iwnn    解析情報クラス
 * @param[in]      result    解析結果
 *
 * @retval >=0 品詞グループ番号 (MM_HGROUP_*)
 * @retval <0  エラー(resultがNULLの場合を含む)
 */
NJ_EXTERN NJ_INT16 mmx_get_hinsi(NJ_CLASS *iwnn, NJ_RESULT *result) {


    /* 処理結果がNULLの場合はエラーとする */
    if (result == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_GET_HINSI, NJ_ERR_PARAM_RESULT_NULL);
    }

    /* 第1引数(iwnn)がNULLの場合はエラーとする */
    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_GET_HINSI, NJ_ERR_PARAM_ENV_NULL);
    }

    /* 辞書セット内ルール辞書がNULLの場合はエラーとする */
    if (iwnn->dic_set.rHandle[NJ_MODE_TYPE_MORPHO] == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_GET_HINSI, NJ_ERR_NO_RULEDIC);
    }

    /* 形態素解析結果以外はエラーとする */
    if ((NJ_GET_RESULT_FUNC(result->operation_id) != NJ_FUNC_SPLIT_WORD) ||
        (NJ_GET_RESULT_OP(result->operation_id) != NJ_OP_MORPHOLIZE)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_GET_HINSI, NJ_ERR_INVALID_RESULT);
    }

    return njd_r_check_group(iwnn->dic_set.rHandle[NJ_MODE_TYPE_MORPHO], NJ_GET_BPOS_FROM_STEM(&(result->word)));
}


/**
 * 形態素解析 読み文字列情報取得API
 *
 * 指定した単語の読み・処理結果を取得する
 *
 * @param[in,out]  iwnn       解析情報クラス
 * @param[in]      target     対象文節の形態素結果
 * @param[out]     yomi       読み文字列格納バッファ
 * @param[in]      yomi_size  読み文字列格納バッファのバイトサイズ
 * @param[out]     stem_len   読み文字列格納バッファ中の自立語文字配列長
 * @param[out]     result     取得候補格納バッファ
 *
 * @retval  >0  yomiに格納したバイト数
 * @retval  =0  候補なし
 * @retval  <0  エラー
 */
NJ_EXTERN NJ_INT16 mmx_get_info(NJ_CLASS *iwnn,
                                NJ_RESULT *target, 
                                NJ_CHAR *yomi, NJ_UINT16 yomi_size,
                                NJ_UINT8 *stem_len, NJ_RESULT *result) {

    NJ_INT16 ret, ret2;
    NJ_CHAR tmp[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_CHAR candidate[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_UINT8 exit_flag;
    NJ_UINT16 tmp_hinsi;
    NJ_CURSOR yomi_cur;
    NJ_UINT16 len = 0;
    NJ_CHAR *ptr;
    NJ_DIC_SET tmpds;
    NJ_UINT16 i;
    NJ_DIC_SET *dics;
    NJ_UINT32 type;
    NJC_CANDIDATE *tmp_cand;
    NJ_INT16 gfpos, gbpos;
    NJ_INT16 giji_type;


    /*
     * 入力引数チェック
     */
    /* 第1引数(iwnn)がNULLの場合はエラーとする */
    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_GET_YOMI, NJ_ERR_PARAM_ENV_NULL);
    }
    dics = &(iwnn->dic_set);
    tmp_cand = &(iwnn->m_Buf.Cand);

    /* 処理結果がNULL以外の場合 */
    if (result != NULL) {
        /*
         * 以下の場合、エラーとする
         * 読み文字列バッファ：NULL、読み文字列バッファサイズ：1以上
         * 読み文字列バッファ：NULL以外、読み文字列バッファサイズ：0
         */
        if (((yomi == NULL) && (yomi_size > 0)) ||
            ((yomi != NULL) && (yomi_size == 0))) {
            return NJ_SET_ERR_VAL(NJ_FUNC_MM_GET_YOMI, NJ_ERR_BUFFER_NOT_ENOUGH);
        }
    /* 処理結果がNULLの場合 */
    } else {
        /*
         * 読み文字列バッファがNULL、
         * もしくは読み文字列バッファサイズが0の場合はエラーとする
         */
        if ((yomi == NULL) || (yomi_size == 0)) {
            return NJ_SET_ERR_VAL(NJ_FUNC_MM_GET_YOMI, NJ_ERR_BUFFER_NOT_ENOUGH);
        }
    }

    /* バッファが(NJ_MAX_LEN + NJ_TERM_LEN)未満の場合エラーとする */
    if ((yomi != NULL) && (yomi_size < ((NJ_MAX_LEN + NJ_TERM_LEN) * sizeof(NJ_CHAR)))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_GET_YOMI, NJ_ERR_BUFFER_NOT_ENOUGH);
    }

    /*
     * 読み文字列格納バッファ中の自立語バイト数を初期化
     */
    if (stem_len != NULL) {
        *stem_len = 0;
    }

    if (target != NULL) {
        /* 新規検索の開始 */

        /* 前回検索結果は強制的にクリアする */
        iwnn->mm_yomi_search_index = 0; /* 0:未検索、1〜:検索後 */
        nje_clear_homonym_buf(iwnn);    /* 同表記バッファクリア */

        /* 全候補取得用バッファをクリアする */
        for (i = 0; i < NJ_MAX_CANDIDATE; i++) {
            njd_init_word(&(tmp_cand->phrases[i].word));
        }
        tmp_cand->phrNum = 0;

        /* 辞書セット内ルール辞書がNULLの場合はエラーとする */
        if (dics->rHandle[NJ_MODE_TYPE_MORPHO] == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_MM_GET_YOMI, NJ_ERR_NO_RULEDIC);
        }

        /* 形態素解析結果以外はエラーとする */
        if ((NJ_GET_RESULT_FUNC(target->operation_id) != NJ_FUNC_SPLIT_WORD) ||
            (NJ_GET_RESULT_OP(target->operation_id) != NJ_OP_MORPHOLIZE)) {
            return NJ_SET_ERR_VAL(NJ_FUNC_MM_GET_YOMI, NJ_ERR_INVALID_RESULT);
        }

        /* 自立語長を取得する */
        len = NJ_GET_YLEN_FROM_STEM(&target->word);
        /* 付属語長を取得する */
        iwnn->mm_yomi_fzk_len = (NJ_UINT8)NJ_GET_YLEN_FROM_FZK(&target->word);

        if ((len + iwnn->mm_yomi_fzk_len) > NJ_MAX_RESULT_LEN) {
            /*
             * 形態素解析結果の候補文字列がNJ_MAX_RESULT_LENを超過
             * した場合は、読み文字列を取得しない。
             */
            return 0;
        }

        INIT_KEYWORD_IN_NJ_DIC_SET(&tmpds);
        tmpds.mode = NJ_CACHE_MODE_NONE;
        /*
         * 使用辞書のフィルタリングを行う
         */
        for (i = 0; i < NJ_MAX_DIC; i++) {
            njd_clear_dicinfo(&(tmpds.dic[i]));
            if (dics->dic[i].handle != NULL) {
                if ( dics->dic[i].dic_freq[NJ_MODE_TYPE_MORPHO].high >=
                     dics->dic[i].dic_freq[NJ_MODE_TYPE_MORPHO].base ) {

                    type = NJ_GET_DIC_TYPE_EX(dics->dic[i].type, dics->dic[i].handle);
                    switch (type) {
                    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
                    case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書               */
                    case NJ_DIC_TYPE_FUSION:                        /* 統合辞書               */
                    case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
                    case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
                    case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
                    case NJ_DIC_TYPE_JIRITSU:                       /* 自立語辞書             */
                    case NJ_DIC_TYPE_TANKANJI:                      /* 単漢字辞書             */
                    case NJ_DIC_TYPE_CUSTOM_COMPRESS:               /* 圧縮カスタマイズ辞書   */
                    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:             /* 非圧縮カスタマイズ辞書 */
                    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:       /* 非圧縮カスタマイズ辞書(学習辞書変更) */
                    case NJ_DIC_TYPE_USER:                          /* ユーザ辞書 */
                    case NJ_DIC_TYPE_FORECONV:                      /* 予測変換辞書           */
                    case NJ_DIC_TYPE_PROGRAM:                       /* 擬似辞書               */
                    case NJ_DIC_TYPE_PROGRAM_FZK:                   /* 擬似辞書-付属語-       */
                        njd_copy_dicinfo(&(tmpds.dic[i]), &(dics->dic[i]), NJ_MODE_TYPE_MORPHO);
                        break;
                    default:
                        break;
                    }
                }
            }
        }
        tmpds.rHandle[NJ_MODE_TYPE_HENKAN] = dics->rHandle[NJ_MODE_TYPE_MORPHO];        /* ルール辞書のコピー */
        tmpds.rHandle[NJ_MODE_TYPE_YOSOKU] = NULL;
        tmpds.rHandle[NJ_MODE_TYPE_MORPHO] = NULL;

        /*
         * 候補文字列が同じ・品詞に合致する ものを検索する。
         */

        ret = njc_get_stroke(iwnn, target, candidate, sizeof(candidate));
        if (ret < 0) {
            return ret;
        }

        /* 自立語がない場合は、付属語のみyomiへ格納して終了する */
        if (len == 0) {

            iwnn->mm_yomi_search_index = 1;         /* 検索後 */

            if (yomi_size >= (NJ_UINT16)((ret + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
                nj_strcpy(yomi, candidate);
                return ret;
            } else {
                /* バッファに入らないので候補なし */
                return 0; /*NCH*/
            }
        }

        /* 検索条件を作成する */
        yomi_cur.cond.operation = NJ_CUR_OP_REV;        /* 逆引き */
        yomi_cur.cond.mode      = NJ_CUR_MODE_FREQ;     /* 頻度順 */
        yomi_cur.cond.ds        = &tmpds;       /* フィルタ後辞書セット */
        yomi_cur.cond.yomi      = candidate;    /* 自立語の候補文字列   */
        yomi_cur.cond.ylen      = len;          /* 自立語長             */
        yomi_cur.cond.kanji     = NULL;
        yomi_cur.cond.charset   = NULL;

        /* 品詞数設定 */
        njd_r_get_count(dics->rHandle[NJ_MODE_TYPE_MORPHO], &(yomi_cur.cond.hinsi.foreSize),
                        &(yomi_cur.cond.hinsi.rearSize));

        /*
         * 前品詞条件設定
         */
        /* "文頭" 品詞番号取得 */
        tmp_hinsi = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_MORPHO], NJ_HINSI_BUNTOU_B);
        if (tmp_hinsi == 0) {
            /* 指定した品詞が正常取得できない場合 */
            return NJ_SET_ERR_VAL(NJ_FUNC_MM_GET_YOMI, NJ_ERR_NO_HINSI);
        }
        /* 接続情報取得 */
        njd_r_get_connect(dics->rHandle[NJ_MODE_TYPE_MORPHO], tmp_hinsi,
                          0, &(yomi_cur.cond.hinsi.fore));
        /* 接続する単語を取得 */
        yomi_cur.cond.hinsi.foreFlag = 0;
        /* 読み無し予測用の前品詞情報初期化 */
        yomi_cur.cond.hinsi.yominasi_fore = NULL;
        yomi_cur.cond.hinsi.prev_bpos = 0;

        /*
         * 後品詞条件設定
         */
        if (iwnn->mm_yomi_fzk_len == 0) {
            /* 付属語がない場合は、V2を使用する */
            tmp_hinsi = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_MORPHO], NJ_HINSI_V2_F);
        } else {
            /* 付属語がある場合は、付属語の前品詞を使用する */
            tmp_hinsi = NJ_GET_FPOS_FROM_FZK(&(target->word));
        }
        if (tmp_hinsi == 0) {
            /* 指定した品詞が正常取得できない場合 */
            return NJ_SET_ERR_VAL(NJ_FUNC_MM_GET_YOMI, NJ_ERR_NO_HINSI);
        }
        /* 接続情報取得 */
        njd_r_get_connect(dics->rHandle[NJ_MODE_TYPE_MORPHO], tmp_hinsi,
                          1, &(yomi_cur.cond.hinsi.rear));
        /* 接続する単語を取得 */
        yomi_cur.cond.hinsi.rearFlag = 0;

        /* 表記・品詞を条件として自立語の検索を開始する */
        ret = njd_search_word(iwnn, &yomi_cur, 0, &exit_flag);
        if (ret < 0) {
            return ret;
        }

        /* 同じ候補文字列を持つ単語を取得する */
        while ((ret = njd_get_word(iwnn, &yomi_cur, &(tmp_cand->phrases[tmp_cand->phrNum]), 0)) > 0) {

            /*
             * tmp に読み文字列を展開する
             */
            ret2 = njd_get_stroke(iwnn, &(tmp_cand->phrases[tmp_cand->phrNum]), tmp, sizeof(tmp));
            if (ret2 < 0) {
                /* njd_get_strokeでエラー発生 */
                return ret2; /*NCH_FB*/
            }

            if ((iwnn->mm_yomi_fzk_len + ret2) > NJ_MAX_LEN) {
                continue;
            }

            /* 取得した自立語の読みに付属語を追加する */
            nj_strcpy(&tmp[ret2], &candidate[len]);
            /* 同表記バッファに格納する*/
            ret2 = nje_append_homonym_string(iwnn, tmp);
            if (ret2 == 0) {
                /* 内部保持取得候補バッファの内容を更新する */
                tmp_cand->phrases[tmp_cand->phrNum].operation_id &= ~NJ_FUNC_MASK;
                tmp_cand->phrases[tmp_cand->phrNum].operation_id |= NJ_FUNC_ZENKOUHO;
                tmp_cand->phrases[tmp_cand->phrNum].operation_id |= NJ_OP_MORPHOLIZE;
                tmp_cand->phrases[tmp_cand->phrNum].word.yomi = target->word.yomi;
                tmp_cand->phrases[tmp_cand->phrNum].word.fzk.info1 = target->word.fzk.info1;
                tmp_cand->phrases[tmp_cand->phrNum].word.fzk.info2 = target->word.fzk.info2;
                tmp_cand->phrases[tmp_cand->phrNum].word.fzk.hindo = target->word.fzk.hindo;
                tmp_cand->phrNum++;
            }
        }
        if (ret < 0) {
            /* njd_get_wordでエラー発生 */
            return ret; /*NCH_FB*/
        }

        /*
         * 自立語の文字種を取得する
         */
        giji_type = (NJ_INT16)nje_check_string(candidate, (NJ_UINT16)len);
        switch (giji_type) {
        case NJ_TYPE_HIRAGANA:
            if (nj_strlen(candidate) <= NJ_MAX_LEN) {
                ret2 = nje_append_homonym_string(iwnn, candidate);
                if (ret2 == 0) {
                    /* 品詞「疑似」の品詞番号を取得する */
                    gfpos = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_MORPHO], NJ_HINSI_GIJI_F);
                    gbpos = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_MORPHO], NJ_HINSI_GIJI_B);
                    rlt_setMorphoGiji(&tmp_cand->phrases[tmp_cand->phrNum], (NJ_UINT16)len, gfpos, gbpos, giji_type);
                    tmp_cand->phrases[tmp_cand->phrNum].word.yomi = target->word.yomi;
                    tmp_cand->phrases[tmp_cand->phrNum].word.fzk.info1 = target->word.fzk.info1;
                    tmp_cand->phrases[tmp_cand->phrNum].word.fzk.info2 = target->word.fzk.info2;
                    tmp_cand->phrases[tmp_cand->phrNum].word.fzk.hindo = target->word.fzk.hindo;
                    tmp_cand->phrNum++;
                }
            }
            break;

        case NJ_TYPE_KATAKANA:
        case NJ_TYPE_HANKATA:
            /*
             * 自立語候補文字列が全てひらがな文字でない場合
             * 自立語を、全角・半角カタカナ→ひらがな変換を行う
             */
            ret = nje_convert_kata_to_hira(candidate, tmp,
                                           len, NJ_MAX_LEN, (NJ_UINT8)giji_type);
            if ((ret > 0) && ((ret + iwnn->mm_yomi_fzk_len) <= NJ_MAX_LEN)) {
                /*
                 * 全角・半角カタカナ→ひらがなが正常変換
                 * 付属語を付加する
                 */
                nj_strcpy(&tmp[ret], &candidate[len]);
                ret2 = nje_append_homonym_string(iwnn, tmp);
                if (ret2 == 0) {
                    /* 品詞「疑似」の品詞番号を取得する */
                    gfpos = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_MORPHO], NJ_HINSI_GIJI_F);
                    gbpos = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_MORPHO], NJ_HINSI_GIJI_B);
                    rlt_setMorphoGiji(&tmp_cand->phrases[tmp_cand->phrNum], (NJ_UINT16)len, gfpos, gbpos, giji_type);
                    tmp_cand->phrases[tmp_cand->phrNum].word.yomi = target->word.yomi;
                    tmp_cand->phrases[tmp_cand->phrNum].word.fzk.info1 = target->word.fzk.info1;
                    tmp_cand->phrases[tmp_cand->phrNum].word.fzk.info2 = target->word.fzk.info2;
                    tmp_cand->phrases[tmp_cand->phrNum].word.fzk.hindo = target->word.fzk.hindo;
                    tmp_cand->phrNum++;
                }
            }
            break;
            
        default:
            /*
             * その他の文字種は、読み文字列を作成しない
             */
            break;
        }

        /* iwnn->mm_yomi_search_index は1-originとする */
        iwnn->mm_yomi_search_index = 1; /* 0:未検索、1〜:検索後 */

    } else {
        /* targetがNULLの場合は前の続きを行う */
        if (iwnn->mm_yomi_search_index == 0) {
            /* njx_init以降、mmx_get_yomiの初回処理だった */
            return NJ_SET_ERR_VAL(NJ_FUNC_MM_GET_YOMI, NJ_ERR_NO_CANDIDATE_LIST);
        }
    }

    /* 次の同表記語を取得する */
    while ((ptr = nje_get_homonym_string(iwnn, (NJ_UINT16)(iwnn->mm_yomi_search_index - 1))) != NULL) {
        /* 次読み文字列候補位置 更新 */
        iwnn->mm_yomi_search_index++;

        /*
         * 指定されたバッファに格納できれば、
         * 上位に返す文字列確定
         */
        len = nj_strlen(ptr);
        if ((yomi == NULL) ||
            ((yomi != NULL) && (yomi_size >= ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))))) {
            break;
        }
    }
    if (ptr == NULL) {
        /* 候補なし */
        return 0;
    }

    /* 読み文字列バッファがNULL以外の場合 */
    if (yomi != NULL) {
        /* ptr位置のデータを、yomiバッファにコピーする */
        nj_strcpy(yomi, ptr);
    }

    /* 取得候補格納バッファがNULL以外の場合 */
    if (result != NULL) {
        *result = tmp_cand->phrases[iwnn->mm_yomi_search_index - 2];
    }

    /* 自立語長を、stem_lenへ返す */
    if (stem_len != NULL) {
        *stem_len = (NJ_UINT8)len - iwnn->mm_yomi_fzk_len;
    }

    return (NJ_INT16)len;
}


/**
 * 形態素解析 学習API
 *
 * 指定した単語の読みを取得する。
 *
 * @param[in,out]  iwnn   解析情報クラス
 * @param[in]      result   処理結果
 * @param[in]      yomi     読み文字列格納バッファ
 * @param[in]      nofzk    0:指定文節学習、1:指定文節から自立語部分のみを学習
 * @param[in]      connect  ひとつ前に選択した候補との関係情報を{0:作成しない,1:作成する}
 *
 * @retval  >=0 正常終了
 * @retval  <0  エラー
 */
NJ_EXTERN NJ_INT16 mmx_select(NJ_CLASS *iwnn, NJ_RESULT *result,
                              NJ_CHAR *yomi, NJ_UINT8 nofzk, NJ_UINT8 connect) {

    NJ_LEARN_WORD_INFO lwinfo;
    NJ_LEARN_WORD_INFO save_lwinfo;
    NJ_CHAR            kouho[MM_MAX_MORPHO_LEN + NJ_TERM_LEN];
    NJ_UINT16          ylen, klen;
    NJ_INT16           ret;


    /*
     * 入力パラメータチェック
     */
    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_SELECT, NJ_ERR_PARAM_ENV_NULL);
    }

    if (iwnn->dic_set.rHandle[NJ_MODE_TYPE_MORPHO] == NULL) {
        /* 第1引数(iwnn)でルール辞書ハンドルがNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_SELECT, NJ_ERR_NO_RULEDIC);
    }
    if (result == NULL) {
        /* 第2引数(result)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_SELECT, NJ_ERR_PARAM_RESULT_NULL);
    }
    if (((NJ_GET_RESULT_FUNC(result->operation_id) != NJ_FUNC_SPLIT_WORD) &&
         (NJ_GET_RESULT_FUNC(result->operation_id) != NJ_FUNC_ZENKOUHO)) ||
        (NJ_GET_RESULT_OP(result->operation_id) != NJ_OP_MORPHOLIZE)) {
        /* 第2引数(result)が形態素解析結果以外はエラーとする */
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_SELECT, NJ_ERR_INVALID_RESULT);
    }
    if (yomi == NULL) {
        /* 第3引数(yomi)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_SELECT, NJ_ERR_PARAM_YOMI_NULL);
    }
    ylen = nj_strlen(yomi);
    if (ylen == 0) {
        /* 第3引数(yomi)が空文字列の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_SELECT, NJ_ERR_PARAM_YOMI_NULL);
    }
    if (ylen > NJ_MAX_LEN) {
        /* 第3引数(yomi)の読み文字列長が、NJ_MAX_LENを超過している場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_SELECT, NJ_ERR_YOMI_TOO_LONG);
    }
    if ((nofzk != 0) && (nofzk != 1)) {
        /* 第4引数(nofzk)が、規定(0 or 1)以外の値が設定されている場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_SELECT, NJ_ERR_INVALID_FLAG);
    }
    if ((connect != 0) && (connect != 1)) {
        /* 第5引数(connect)が、規定(0 or 1)以外の値が設定されている場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_SELECT, NJ_ERR_INVALID_FLAG);
    }


    if (nofzk == 0) {
        klen = NJ_GET_YLEN_FROM_STEM(&(result->word)) + NJ_GET_YLEN_FROM_FZK(&(result->word));
    } else {
        klen = NJ_GET_YLEN_FROM_STEM(&(result->word));
    }
    if (klen > NJ_MAX_RESULT_LEN) {
        /*
         * 第2引数(result)の学習対象の候補文字列バイト長が
         * NJ_MAX_RUSULTを超過した場合エラー
         */
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_SELECT, NJ_ERR_CANDIDATE_TOO_LONG);
    }
    if (klen == 0) {
        /*
         * nofzk=1 の時に、NJ_RESULTが付属語のみであれば
         * 学習する候補文字列が無くなるので異常とする
         */
        return NJ_SET_ERR_VAL(NJ_FUNC_MM_SELECT, NJ_ERR_INVALID_RESULT);
    }

    ret = njc_get_stroke(iwnn, result, kouho, sizeof(kouho));
    if (ret < 0) {
        return ret; /*NCH*/
    }

    /* 学習情報の作成 */
    if (NJ_GET_YLEN_FROM_STEM(&(result->word)) != 0) {
        lwinfo.f_hinsi      = NJ_GET_FPOS_FROM_STEM(&(result->word));
        lwinfo.stem_b_hinsi = NJ_GET_BPOS_FROM_STEM(&(result->word));
    } else {
        lwinfo.f_hinsi      = NJ_GET_FPOS_FROM_FZK(&(result->word));
        lwinfo.stem_b_hinsi = NJ_GET_BPOS_FROM_FZK(&(result->word));
    }

    if ((nofzk == 0) && (NJ_GET_YLEN_FROM_FZK(&(result->word)) != 0)) {
        lwinfo.b_hinsi      = NJ_GET_BPOS_FROM_FZK(&(result->word));
        lwinfo.fzk_yomi_len = NJ_GET_YLEN_FROM_FZK(&(result->word));
    } else {
        lwinfo.b_hinsi      = NJ_GET_BPOS_FROM_STEM(&(result->word));
        lwinfo.fzk_yomi_len = 0;
    }

    lwinfo.attr    = result->word.stem.loc.attr;    /* 単語の属性情報 */

    lwinfo.yomi_len = (NJ_UINT8)ylen;               /* 単語の読み文字列長 */
    lwinfo.hyouki_len = (NJ_UINT8)klen;             /* 単語の候補文字列長 */

    nj_strcpy(lwinfo.yomi, yomi);                   /* 単語の読み文字列 */
    nj_strncpy(lwinfo.hyouki, kouho, klen);         /* 単語の候補文字列 */
    lwinfo.hyouki[klen] = NJ_CHAR_NUL;
    lwinfo.additional_len = 0;                                 /* 単語の付加情報文字列長 */
    lwinfo.additional[lwinfo.additional_len] = NJ_CHAR_NUL;    /* 単語の付加情報文字列 */

    /* 学習辞書への登録処理にて変更されるため、コピーを取る */
    save_lwinfo = lwinfo;

    ret = njd_l_add_word(iwnn, &lwinfo, connect, 0, 1, 1);
    if ((ret < 0) &&
        (NJ_GET_ERR_CODE(ret) != NJ_ERR_DIC_NOT_FOUND)) {
        /* エラー発生のため、そのままエラーを返す */
        return ret;
    }

    /* 学習辞書以外の学習処理を行う。 */
    ret = njd_learn_word(iwnn, result, &save_lwinfo, connect, 1, 0);
    if (ret < 0) {
        /* エラー発生のため、そのままエラーを返す */
        return ret; /*NCH*/
    }

    return ret;
}
