/**
 * @file
 *  [拡張] AI予測用擬似候補作成辞書 (UTF16/SJIS版)
 *
 * AI予測用擬似候補の作成を行う辞書。
 *
 * @author
 * (C) OMRON SOFTWARE Co., Ltd. 2010 All Rights Reserved.
 */
#include "ex_aipgiji.h"
#include "nj_err.h"
#include "nj_ext.h"
#include "njd.h"
#ifdef NJ_OPT_UTF16
#include "ex_aiptabU.h"
#else /* NJ_OPT_UTF16 */
#include "ex_aiptab.h"
#endif /* NJ_OPT_UTF16 */


/************************************************/
/*         define  宣  言                       */
/************************************************/
/*
 * 文字種ごとの文字長定義
 */
#ifdef NJ_OPT_UTF16
/** 半角文字長 (UTF16) */
#define NJG_AIP_HAN_MOJI_LEN  1
/** 全角文字長 (UTF16) */
#define NJG_AIP_ZEN_MOJI_LEN  1

#else /* NJ_OPT_UTF16 */
/** 半角文字長 (SJIS) */
#define NJG_AIP_HAN_MOJI_LEN  1
/** 全角文字長 (SJIS) */
#define NJG_AIP_ZEN_MOJI_LEN  2
#endif /* NJ_OPT_UTF16 */

#define AIP_DIC_FREQ_DIV 100 /**< AI予測擬似辞書頻度段階 */

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
#define NJG_AIP_CONV_TO_WCHAR(x)                                            \
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
#define NJG_AIP_IS_WCHAR(x)  1
#else /* NJ_OPT_UTF16 */
/** SJIS版 */
#define NJG_AIP_IS_WCHAR(x)                                                 \
    (((((x) >= 0x81) && ((x) <= 0x9f)) || (((x) >= 0xe0) && ((x) <= 0xfc))) ? 1 : 0)
#endif/* NJ_OPT_UTF16 */

#define RESTORE_HINDO(freq, base, high, div) \
    ((NJ_HINDO)(((base) >= (high)) ? 0 : ((((freq) - (base)) * (div)) / ((high) - (base)))))

/***********************************************************************
 * 内部関数 プロトタイプ宣言
 ***********************************************************************/
static NJ_INT16 search_word(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE *message, NJD_AIP_WORK *aipwork, NJ_DIC_HANDLE fzk_dic_table);
static NJ_INT16 get_word_string(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE *message, NJD_AIP_WORK *aipwork, NJ_DIC_HANDLE fzk_dic_table, NJ_CHAR *string, NJ_INT16 size);

/***********************************************************************/

/**
 * AI予測用擬似辞書 辞書インタフェース
 *
 * @param[in,out] iwnn      iWnn内部情報(通常は参照のみ)
 * @param[in]     request   iWnnからの処理要求
 *                          - NJG_OP_SEARCH：初回検索
 *                          - NJG_OP_SEARCH_NEXT：次候補検索
 *                          - NJG_OP_GET_WORD_INFO：単語情報取得
 *                          - NJG_OP_GET_STROKE：読み文字列取得
 *                          - NJG_OP_GET_STRING：表記文字列取得
 *                          - NJG_OP_GET_ADDITIONAL：付加情報文字列取得
 *                          - NJG_OP_LEARN：学習
 *                          - NJG_OP_LEARN_UNDO：学習UNDO
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
 *                付加情報文字列長<br>
 *             [NJG_OP_LEARNの場合]<br>
 *                0:正常終了、<0:エラー
 *             [NJG_OP_UNDO_LEARNの場合]<br>
 *                0:正常終了、<0:エラー
 *
 * @retval <0  エラー
 */
NJ_EXTERN NJ_INT16 njex_aip_giji_dic(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE *message) {
    NJD_AIP_WORK  *aipwork;
    NJD_AIP_DATA  *aipdat;
    NJ_DIC_HANDLE fzk_dic_table;
    NJ_UINT8 i;

    if ((iwnn == NULL) ||
        (message == NULL)) {
        /* エンジン内部からの呼び出しのため、
           本箇所を通ることはない */
        return 0; /*NCH*/
    }
    
    /* AI予測用ワーク領域がない場合は、動作しない。 */
    if (iwnn->option_data.aip_work == NULL) {
        if ((request == NJG_OP_SEARCH) || (request == NJG_OP_SEARCH_NEXT)) {
            message->location->loct.status = NJ_ST_SEARCH_END_EXT;
        }
        if ((request == NJG_OP_GET_STROKE) ||
            (request == NJG_OP_GET_STRING) ||
            (request == NJG_OP_GET_ADDITIONAL)) {
            return (-1);
        }
        return 0;
    } else {
        aipwork = (NJD_AIP_WORK*)iwnn->option_data.aip_work;
    }

    /* AI予測用付属語テーブルを設定する */
    switch (request) {
    case NJG_OP_SEARCH:
    case NJG_OP_SEARCH_NEXT:
    case NJG_OP_GET_WORD_INFO:
        if ((message->dic_idx < NJ_MAX_DIC) &&
            (iwnn->dic_set.dic[message->dic_idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] == NULL)) {
            /*
             * 頻度学習領域がNULLの場合は、デフォルトの付属語テーブルを使う。
             */
            fzk_dic_table = (NJ_DIC_HANDLE)&giji_fzk_tbl[0];
        } else {
            /* 頻度学習領域に設定されたものを付属語テーブルとして扱う */
            fzk_dic_table = (NJ_DIC_HANDLE)(iwnn->dic_set.dic[message->dic_idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
        }
        break;

    case NJG_OP_GET_STROKE:
    case NJG_OP_GET_STRING:
    case NJG_OP_GET_ADDITIONAL:
    case NJG_OP_LEARN:
    case NJG_OP_DEL_WORD:
        /* 付属語テーブルは、message->word->stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]を設定 */
        fzk_dic_table = message->word->stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT];
        break;

    case NJG_OP_UNDO_LEARN:
    case NJG_OP_ADD_WORD:
    default:
        /* 付属語テーブルを使用しないため、NULLを設定 */
        fzk_dic_table = NULL;
        break;
    }

    switch (request) {
    case NJG_OP_SEARCH:
    case NJG_OP_SEARCH_NEXT:
        /* 検索条件にマッチするかを判定する */
        if ((aipwork->save_cnt == 0) ||
            ((message->condition->ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) != 0x00) ||
            ((iwnn->environment.option.mode & NJ_RELATION_ON) == 0) ||
            (message->condition->mode != NJ_CUR_MODE_FREQ) ||
            ((message->condition->operation != NJ_CUR_OP_FORE) && (message->condition->operation != NJ_CUR_OP_COMP))) {
            /* 
             * 以下のいずれかの条件では、検索を実施しない。
             * - AI予測用候補保持なし
             * - 辞書引きAPIから呼び出し
             * - 予測オプション：解析制限の繋がり予測絞込検索なし
             * - 取得順が頻度順ではない場合
             * - 検索方法が前方一致検索、完全一致検索ではない場合
             */
            message->location->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        if ( !(((iwnn->njc_mode == NJC_MODE_NOT_CONVERTED) &&
               ((iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH) ||
                (iwnn->environment.type == NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI) ||
                (iwnn->environment.type == NJ_ANALYZE_CONVERSION_MULTIPLE) ||
                (iwnn->environment.type == NJ_ANALYZE_CONVERSION_SINGLE) ||
                (iwnn->environment.type == NJ_ANALYZE_COMPLETE))) ||
               (iwnn->njc_mode == NJC_MODE_CONVERTED) ||
               (iwnn->njc_mode == NJC_MODE_ZENKOUHO)) ) {
            /*
             * 以下のいずれかの条件に当てはまらない場合は、検索を実施しない。
             * - 予測候補取得API からの呼び出しで
             *     かつ、前方一致、連文節変換、単文節変換、全候補の検索状態の時
             * - かな漢字変換API からの呼び出し
             * - 全候補取得API からの呼び出し
             */
            message->location->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        
        /* 検索処理を行う */
        return search_word(iwnn, request, message, aipwork, fzk_dic_table);

    case NJG_OP_GET_WORD_INFO:
        if (message->location->loct.current > message->location->loct.bottom) {
            /* 念のため保持候補の範囲をチェック。ココを通ることはない */
            return -1; /*NCH*/
        }

        /* 単語情報を作成 */
        aipdat = &(aipwork->aip_dat[message->location->loct.current]);
        *(message->word) = aipdat->result.word;
        (*(message->word)).stem.loc.status      = SET_LOCATION_OPERATION(GET_LOCATION_OPERATION(message->location->loct.status)) |
                                                  GET_LOCATION_STATUS(aipdat->result.word.stem.loc.status);
        (*(message->word)).stem.loc.type        = NJ_DIC_H_TYPE_PROGRAM;
        (*(message->word)).stem.loc.handle      = (NJ_DIC_HANDLE)njex_aip_giji_dic;
        for (i = 0; i < NJ_MAX_EXT_AREA; i++) {
            (*(message->word)).stem.loc.ext_area[i] = NULL;
        }
        (*(message->word)).stem.loc.current     = message->location->loct.current;
        (*(message->word)).stem.loc.current_info= message->location->loct.current_info;

        /* 検索方法の固有となる情報を設定する */
        if (GET_LOCATION_OPERATION(message->location->loct.status) == NJ_CUR_OP_FORE) {
            /* 前方一致検索結果 */
            if (aipdat->loctset.loct.status == NJ_ST_SEARCH_END) {
                /* 候補(語幹部分)を返す場合 */
            } else {
                /* 候補(語幹部分)＋接続候補(付属語部分)を返す場合 */
                (*(message->word)).stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT] = (NJ_VOID*)fzk_dic_table;
                (*(message->word)).stem.loc.relation[0] = aipdat->word.stem.loc.current;
                NJ_SET_BPOS_TO_STEM(message->word, NJ_GET_BPOS_FROM_STEM(&(aipdat->word)));
                NJ_SET_YLEN_TO_STEM(message->word, (NJ_UINT8)(NJ_GET_YLEN_FROM_STEM(&(aipdat->result.word)) + NJ_GET_YLEN_FROM_STEM(&(aipdat->word))));
                NJ_SET_KLEN_TO_STEM(message->word, (NJ_UINT8)(NJ_GET_KLEN_FROM_STEM(&(aipdat->result.word)) + NJ_GET_KLEN_FROM_STEM(&(aipdat->word))));
            }
        } else if (GET_LOCATION_OPERATION(message->location->loct.status) == NJ_CUR_OP_COMP) {
            /* 完全一致検索結果 */
            (*(message->word)).stem.hindo = message->location->cache_freq;
        } else {
        }
        return 0;

    case NJG_OP_GET_STROKE:
        return get_word_string(iwnn, request, message, aipwork, fzk_dic_table, message->stroke, message->stroke_size);

    case NJG_OP_GET_STRING:
        return get_word_string(iwnn, request, message, aipwork, fzk_dic_table, message->string, message->string_size);

    case NJG_OP_GET_ADDITIONAL:
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
 * AI予測用擬似辞書拡張情報データサイズ取得API
 *
 * 拡張情報データサイズを取得する。
 *
 * @param[in]  iwnn     解析情報クラス
 * @param[out] size     拡張情報データサイズ
 *
 * @retval 0  正常終了
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njex_aip_get_ext_area_size(NJ_CLASS *iwnn, NJ_UINT32 *size) {
    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_EXT_AREA_SIZE, NJ_ERR_PARAM_ENV_NULL);
    }

    if (size == NULL) {
        /* 第3引数(size)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_EXT_AREA_SIZE, NJ_ERR_PARAM_SIZE_NULL);
    }

    *size = sizeof(NJD_AIP_WORK);

    return 0;
}


/**
 * AI辞書データの検索処理
 *
 * @param[in,out] iwnn          iWnn内部情報(通常は参照のみ)
 * @param[in]     request       iWnnからの処理要求
 *                              - NJG_OP_SEARCH：初回検索
 *                              - NJG_OP_SEARCH_NEXT：次候補検索
 * @param[in,out] message       iWnn←→擬似辞書間でやり取りする情報
 * @param[in,out] aipwork       ワーク領域
 * @param[in]     fzk_dic_table 付属語テーブル
 *
 * @retval >=0 [NJG_OP_SEARCHの場合]<br>
 *                1:候補あり、0:候補なし<br>
 *             [NJG_OP_SEARCH_NEXTの場合]<br>
 *                1:候補あり、0:候補なし<br>
 *
 * @retval <0  エラー
 */
static NJ_INT16 search_word(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE *message, NJD_AIP_WORK *aipwork, NJ_DIC_HANDLE fzk_dic_table) {
    NJ_INT16      ret, base_len, last_len, ylen, cmplen, lastcmplen;
    NJ_UINT32     i, j, loop_start;
    NJ_CHAR       yomi_buff[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_CHAR      *yomi;
    NJ_HINDO      word_hindo;
    NJD_AIP_DATA  *aipdat;
    
    if (message->location->loct.status == NJ_ST_SEARCH_NO_INIT) {
        /* 初回検索時のみ、以下の処理を実施する。*/

        /* 検索対象範囲の設定 */
        message->location->loct.top = 0;
        message->location->loct.bottom = aipwork->save_cnt - 1;
        aipwork->status = 0xFFFFFFFF;
        loop_start = 0;
    } else {
        /* 各検索モードに合わせて、ループの初期値を設定 */
        if (message->condition->operation == NJ_CUR_OP_FORE) {
            loop_start = aipwork->status;
        } else {
            loop_start = message->location->loct.current + 1;
        }

    }
    
    /* 検索読み文字列の設定 */
    yomi = message->condition->yomi;
    ylen = message->condition->ylen;
    
    if (message->condition->operation == NJ_CUR_OP_FORE) {
        /* 前方一致検索時 */
        
        for (i = loop_start; i <= message->location->loct.bottom; i++) {
            /* 候補毎(語幹)のAI予測データを設定 */
            aipdat = &(aipwork->aip_dat[i]);

            if (message->location->loct.status == NJ_ST_SEARCH_NO_INIT) {
                /* 初回検索時 */
                njd_init_word(&(aipdat->word));
                njd_init_search_location_set(&(aipdat->loctset));

                /* カーソルの位置情報の初期設定 */
                aipdat->loctset.loct.handle        = fzk_dic_table;
                aipdat->loctset.loct.type          = NJ_DIC_H_TYPE_NORMAL;
                for (j = 0; j < NJ_MAX_EXT_AREA; j++) {
                    aipdat->loctset.loct.ext_area[j] = NULL;
                }
                for (j = 0; j < NJ_MAX_ADDITIONAL_INFO; j++) {
                    aipdat->loctset.loct.add_info[j] = NULL;
                }
                aipdat->loctset.loct.current_info  = 0x10;  /* num=1, offset = 0    */
                aipdat->loctset.loct.status        = NJ_ST_SEARCH_NO_INIT;
                aipdat->loctset.dic_freq           = message->location->dic_freq;
                aipdat->loctset.dic_freq_max       = message->location->dic_freq_max;
                aipdat->loctset.dic_freq_min       = message->location->dic_freq_min;

            } else {
                /* 次候補検索時 */
                if ((i == loop_start) &&
                    (aipdat->loctset.loct.status == NJ_ST_SEARCH_END)) {
                    /* 語幹部分のみの候補返却後、検索状態を終了へ遷移させる */
                    aipdat->loctset.loct.status = NJ_ST_SEARCH_END_EXT;
                }
                if (aipdat->loctset.loct.status == NJ_ST_SEARCH_END_EXT) {
                    /* 
                     * 候補毎(語幹)のAI予測データに対する検索が終了したため、
                     * 次の候補へ遷移する。
                     */
                    continue;
                }
                if (aipwork->status != i) {
                    /* 
                     * 候補毎(語幹)の切替が発生した場合は、
                     * ステータス情報を更新し、すでに検索済みの別候補(語幹)を
                     * 利用するため、ループを抜ける
                     */
                    aipwork->status = i;
                    break;
                }
            }

            /* 候補(語幹部分)の読み文字列を取得する */
            base_len = njx_get_stroke(iwnn, &(aipdat->result), &yomi_buff[0], sizeof(yomi_buff));
            if (base_len < 0) {
                /* エラーが返ってきた場合は、検索対象としない。 */
                aipdat->loctset.loct.status = NJ_ST_SEARCH_END_EXT;
                continue;
            }

            /* 
             * 入力文字列長と候補(語幹部分)文字列長を比較し、
             * 前方一致するか、まず判定を行う。
             */
            if (ylen >= base_len) {
                cmplen     = base_len;
                last_len   = (NJ_INT16)(ylen - base_len);
            } else {
                cmplen     = ylen;
                last_len   = 0;
            }

            if ((nj_strncmp(yomi, yomi_buff, cmplen) == 0)) {
                /* 候補（語幹部分）が完全一致したので、残り部分の検索を行う。*/

                /* 候補の後品詞を設定することにより、
                 * 後品詞に対する接続候補(付属語部分)を取得する */
                aipwork->condition.hinsi.prev_bpos = NJ_GET_BPOS_FROM_STEM(&aipdat->result.word);
                while ((ret = njd_p_search_word(iwnn, &(aipwork->condition), &(aipdat->loctset), 0)) > 0) {
                    ret = njd_p_get_word(&(aipdat->loctset), &(aipdat->word));
                    if (ret <= 0) {
                        /* 候補なし、エラーが返ってきた場合は、検索対象としない。 */
                        break;
                    } else if ((NJ_INT16)(base_len + NJ_GET_YLEN_FROM_STEM(&(aipdat->word))) > NJ_MAX_LEN) {
                        /* 語幹部分＋付属語部分を合わせると、NJ_MAX_LENを越える場合は、
                         * データとして採用をしない。
                         */
                        continue;
                    } else if ((NJ_GET_KLEN_FROM_STEM(&(aipdat->result.word)) + NJ_GET_KLEN_FROM_STEM(&(aipdat->word))) > NJ_MAX_RESULT_LEN) {
                        /* 語幹部分＋付属語部分を合わせると、NJ_MAX_RESULT_LENを越える場合は、
                         * データとして採用をしない。
                         */
                        continue;
                    } else {
                    }

                    /* 検索状態を前方一致に変更し、接続候補(付属語部分)を取得する。 */
                    aipdat->word.stem.loc.status = SET_LOCATION_OPERATION(NJ_CUR_OP_FORE) | GET_LOCATION_STATUS(aipdat->word.stem.loc.status);
                    if (last_len != 0) {
                        /* 比較対象の入力文字列が残っている場合に比較処理を行う。*/
                        ret = njd_p_get_stroke(&(aipdat->word), &yomi_buff[0], sizeof(yomi_buff));
                        if (ret < 0) {
                            /* エラーの場合は、データとして採用をしない。*/
                            continue;
                        }
                        /* 残り部分が前方一致するか、判定を行う。 */
                        if (last_len == ret) {
                            lastcmplen     = ret;
                        } else if (last_len > ret) {
                            /* 入力文字列長よりも、文字列長が長いため、採用しない */
                            continue;
                        } else {
                            lastcmplen     = last_len;
                        }
                        if ((nj_strncmp((yomi + cmplen), yomi_buff, lastcmplen) == 0)) {
                            /* 検索一致した候補を見つけた場合 */
                            break;
                        }
                    } else {
                        /* 検索一致した候補を見つけた場合 */
                        break;
                    }
                }
                if (ret <= 0) {
                    /* 候補なし、エラーが返ってきた場合は、検索対象としない。 */
                    if (last_len == 0) {
                        /* ただし、入力文字列に対する残り部分がない場合は、
                         * 候補（語幹部分）のみを候補として返す
                         */
                        aipdat->loctset.loct.status = NJ_ST_SEARCH_END;
                        ret = 1;
                    } else {
                        /* 候補なし */
                        aipdat->loctset.loct.status = NJ_ST_SEARCH_END_EXT;
                        continue;
                    }
                }
            } else {
                /* 候補（語幹部分）が入力文字列と不一致のため候補としては利用しない */
                aipdat->loctset.loct.status = NJ_ST_SEARCH_END_EXT;
                continue;
            }
            if (aipwork->status == 0xFFFFFFFF) {
                /* ステータス状態に初期値から変化があったため、更新する */
                aipwork->status = i;
            }
            if ((message->location->loct.status != NJ_ST_SEARCH_NO_INIT) &&
                (ret > 0)) {
                /* 初回検索時以外で、検索候補ありの場合 */
                break;
            }
        }
        if ((message->location->loct.status != NJ_ST_SEARCH_NO_INIT) &&
            (i > message->location->loct.bottom)) {
            /* 保持している候補（語幹部分）の数を越えたため、次候補なし */
            message->location->loct.status = NJ_ST_SEARCH_END_EXT;
        } else if (aipwork->status != 0xFFFFFFFF) {
            /* ステータス状態が初期値でない場合は、候補ありとする */
            message->location->loct.current      = aipwork->status;
            message->location->loct.current_info = 0x10;
            message->location->cache_freq        = aipwork->aip_dat[aipwork->status].result.word.stem.hindo;
            message->location->loct.status       = NJ_ST_SEARCH_READY;
            return 1;
        }

    } else if (message->condition->operation == NJ_CUR_OP_COMP) {
        /* 完全一致検索時 */

        for (i = loop_start; i <= message->location->loct.bottom; i++) {
            ret = njx_get_stroke(iwnn, &(aipwork->aip_dat[i].result), &yomi_buff[0], sizeof(yomi_buff));
            if (ret < 0) {
                /* エラーが返ってきた場合は、検索対象としない。 */
                continue;
            }
            if ((ylen == ret) &&
                (nj_strncmp(yomi, yomi_buff, ylen) == 0)) {
                /* 検索読みと完全一致した場合に候補として利用する。*/
                message->location->loct.current = i;
                message->location->loct.current_info = 0x10;
                aipwork->status = i;
                /* 予測頻度から単語頻度へ復元を行う。*/
                word_hindo = RESTORE_HINDO(aipwork->aip_dat[i].result.word.stem.hindo,
                                           iwnn->dic_set.dic[message->dic_idx].dic_freq[NJ_MODE_TYPE_YOSOKU].base,
                                           iwnn->dic_set.dic[message->dic_idx].dic_freq[NJ_MODE_TYPE_YOSOKU].high,
                                           AIP_DIC_FREQ_DIV);
                /* 復元した単語頻度から、変換頻度を生成する。*/
                message->location->cache_freq = NORMALIZE_HINDO(
                                                 CALCULATE_HINDO(word_hindo, message->location->dic_freq.base, message->location->dic_freq.high, AIP_DIC_FREQ_DIV),
                                                 message->location->dic_freq_max, message->location->dic_freq_min);
                message->location->loct.status = NJ_ST_SEARCH_READY;
                return 1;
            }
        }
    } else {
    }
    /* 候補作成不可 */
    message->location->loct.status = NJ_ST_SEARCH_END_EXT;
    return 0;
}

/**
 * AI辞書データの文字列処理
 *
 * @param[in,out] iwnn          iWnn内部情報(通常は参照のみ)
 * @param[in]     request       iWnnからの処理要求
 *                               - NJG_OP_GET_STROKE：読み文字列取得
 *                               - NJG_OP_GET_STRING：表記文字列取得
 * @param[in,out] message       iWnn←→擬似辞書間でやり取りする情報
 * @param[in,out] aipwork       ワーク領域
 * @param[in]     fzk_dic_table 付属語テーブル
 * @param[out]    string        文字列取得バッファ
 * @param[in]     size          文字列取得バッファサイズ
 *
 * @retval >=0 [NJG_OP_GET_STROKEの場合]<br>
 *                読み文字列長<br>
 *             [NJG_OP_GET_STRINGの場合]<br>
 *                表記文字列長<br>
 *
 * @retval <0  エラー
 */
static NJ_INT16 get_word_string(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE *message, NJD_AIP_WORK *aipwork, NJ_DIC_HANDLE fzk_dic_table, NJ_CHAR *string, NJ_INT16 size) {
    NJ_INT16      base_len, last_len, cmplen;
    NJD_AIP_DATA  *aipdat;
    NJ_WORD       word_dat;
    
    if (request == NJG_OP_GET_STRING) {
        /* 表記文字列の長さを取得 */
        cmplen = NJ_GET_KLEN_FROM_STEM(message->word);
    } else {
        /* 読み文字列の長さを取得 */
        cmplen = NJ_GET_YLEN_FROM_STEM(message->word);
    }
    if (size < (NJ_INT16)((cmplen + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
        /* 格納バッファ不足の場合はエラー */
        return -1;
    }
    aipdat = &(aipwork->aip_dat[message->word->stem.loc.current]);

    if (request == NJG_OP_GET_STRING) {
        /* 表記文字列(語幹部分)を取得 */
        base_len = njx_get_candidate(iwnn, &(aipdat->result), string, size);
    } else {
        /* 読み文字列(語幹部分)を取得 */
        base_len = njx_get_stroke(iwnn, &(aipdat->result), string, size);
    }
    if (base_len < 0) {
        /* エラーが返ってきた場合は、検索対象としない。 */
        return -1;
    }
    last_len = 0;
    if (fzk_dic_table != NULL) {
        word_dat = aipdat->word;
        word_dat.stem.loc.status  = SET_LOCATION_OPERATION(NJ_CUR_OP_LINK) | GET_LOCATION_STATUS(word_dat.stem.loc.status);
        word_dat.stem.loc.handle  = fzk_dic_table;
        word_dat.stem.loc.current = message->word->stem.loc.relation[0];

        if (request == NJG_OP_GET_STRING) {
            /* 表記文字列(付属語部分)を取得 */
            last_len = njd_p_get_candidate(&(word_dat), (string + base_len), (NJ_UINT16)(size - (base_len * sizeof(NJ_CHAR))));
        } else {
            /* 読み文字列(付属語部分)を取得 */
            last_len = njd_p_get_stroke(&(word_dat), (string + base_len), (NJ_UINT16)(size - (base_len * sizeof(NJ_CHAR))));
        }
        if (last_len < 0) {
            /* エラーが返ってきた場合は、検索対象としない。 */
            return -1;
        }
    }
    return (NJ_INT16)(base_len + last_len);
}
