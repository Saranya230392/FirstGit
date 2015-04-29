/**
 * @file
 *   変換部 変換処理
 *
 *   単文節、連文節変換を行う。
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
#include "nje.h"
#include "njd.h"

/************************************************/
/*        static 変数宣言                       */
/************************************************/

/************************************************/
/*              define  宣  言                  */
/************************************************/
/*
 * 全候補解析 状態遷移種別
 */
/** 全候補：初期状態 */
#define ZEN_MODE_INIT 0
/** 全候補：学習辞書検索状態 */
#define ZEN_MODE_LEARN_PROC 1
/** 全候補：辞書検索準備状態 */
#define ZEN_MODE_PRE_PROC 2
/** 全候補：辞書検索状態 */
#define ZEN_MODE_NORMAL_PROC 3
/** 全候補：擬似候補検索状態-iWnnでは未使用- */
#define ZEN_MODE_GIJI_PROC 4
/** 全候補：完了状態 */
#define ZEN_MODE_END 5

/**
 * カタカナ語連結ヒューリスティックの強度を決める定数
 * （値の意味はソースコード参照）
 */
#define NJ_KANA_CONNECT_LEN     3

/**
 * 文末終端ベクトルのチェック
 *
 * @param  mode     変換モード(NJ_CLASS::njc_mode)
 * @param  con      接続条件(NJC_WORK_SENTENCE::connect)
 * @param  hinsi    品詞情報(NJC_HINSI_INFO)
 */
#define CONNECT_R_SENTENCE_END(mode, con, hinsi)                        \
    ((((mode) != NJC_MODE_MORPHO) && CONNECT_R((con), (hinsi).f_v1)) || \
     (((mode) == NJC_MODE_MORPHO) && CONNECT_R((con), (hinsi).f_v2)))    


/**
 * (文末を除く)文節末終端ベクトルのチェック
 *
 * @param  mode     変換モード(NJ_CLASS::njc_mode)
 * @param  con      接続条件(NJC_WORK_SENTENCE::connect)
 * @param  hinsi    品詞情報(NJC_HINSI_INFO)
 */
#define CONNECT_R_PHRASE_END(mode, con, hinsi)                          \
    ((((mode) != NJC_MODE_MORPHO) && CONNECT_R((con), (hinsi).f_v3)) || \
     (((mode) == NJC_MODE_MORPHO) && CONNECT_R((con), (hinsi).f_v2)))    

/**
 * 候補文字列配列長のチェック
 *
 * 残り読み文字列を考慮して、残り読み文字列で擬似候補が生成可能であることを条件とする。
 *
 * @param  mode     iwnn->njc_mode
 * @param  njcconv  解析に使用中のNJC_CONV
 * @param  decided  連文節変換途中で確定済みの候補文字配列長
 * @param  reprLen  追加しようとしている候補文字配列長
 * @param  yomiLen  追加しようとしている読み文字配列長
 *
 * @retval =0  候補文字列配列長が範囲内
 * @retval !=0 候補文字列配列長が最大値を超えている
 */
#define REPR_LEN_EXCEED(mode, njcconv, decided, reprLen, yomiLen)       \
    ( ((mode) != NJC_MODE_MORPHO)                                       \
      ? (((decided) + (reprLen)) > ((njcconv)->maxRltLen - (njcconv)->restYomiLen + (yomiLen))) \
      : (((decided) + (reprLen)) > ((njcconv)->maxRltLen)) )            \

/**
 * 単語IDを取得する
 *
 * @param[in] x  単語情報（NJ_WORD*)
 *
 * @return       単語ID
 */
#define WORD_GET_ID(x)       ((x)->stem.loc.top + (x)->stem.loc.current)

/************************************************/
/*              prototype  宣  言               */
/************************************************/
/* 共通 */

/* 変換関連 */
static NJ_INT16 conv_multiConv(NJ_CLASS *iwnn, NJC_CONV *work,
                               NJC_SENTENCE *result);
static NJ_INT16 conv_stemProcess(NJ_CLASS *iwnn, NJC_CONV *x,
                                 NJ_UINT16 decideLen);
static NJ_INT16 conv_fzkProcess(NJ_CLASS *iwnn, NJC_CONV *x,
                                NJ_UINT16 decideLen);
static NJ_INT16 conv_gijiProcess(NJ_CLASS *iwnn, NJC_CONV *x, NJ_UINT16 decideLen,
                                 NJ_UINT8 first);
static NJ_INT16 conv_replaceSentence(NJC_CONV *x);
static void conv_sortArea(NJC_CONV *x, NJ_INT16 sentID);
static void conv_removeSent(NJC_CONV *x, NJ_INT16 N);
static NJ_INT16 conv_getShortestLen(NJC_CONV *x, NJ_INT16 N, NJ_INT16 *topID);
static void sent_init(NJC_SENTENCE *s);
static void conv_init(NJC_CONV *work);
static void rlt_init(NJ_RESULT *rlt);
static void info_init(NJC_PHRASE_INFO *info);
static void wsent_init(NJC_WORK_SENTENCE *ws);
static NJ_INT16 conv_env_set(NJ_CLASS *iwnn, NJC_CONV *work);
static NJ_INT16 wsent_pushPhrase(NJ_CLASS *iwnn, NJC_CONV *x, NJC_WORK_SENTENCE *ws,
                                 NJ_RESULT *phr, NJC_PHRASE_INFO* info);
static NJ_RESULT *wsent_popPhrase(NJ_CLASS *iwnn, NJC_CONV *x,
                                  NJC_WORK_SENTENCE *ws, NJC_PHRASE_INFO** info);
static void rlt_setHiraGiji(NJ_RESULT *r, NJ_UINT16 len,
                            NJ_UINT16 f_giji, NJ_UINT16 r_giji);
static NJ_UINT16 sent_copyPhrase(NJ_CLASS *iwnn, NJC_CONV *xx, NJC_SENTENCE *x,
                                 NJC_WORK_SENTENCE *sent, NJ_INT16 num);
static void wsent_reducePhrases(NJ_CLASS *iwnn, NJC_WORK_SENTENCE *ws);

/* 全候補取得関連 */
static NJ_UINT8 connect(NJ_DIC_HANDLE rule, NJC_HINSI_INFO *info,
                        NJ_UINT16 posB, NJ_UINT16 posF);
static void fzk_rlt_init(NJC_FZK_RESULT *rlt);
static void cand_init(NJ_CLASS *iwnn, NJC_CANDIDATE *x);
static NJ_INT16 cand_env_set(NJC_CANDIDATE *x);
static NJ_INT16 cand_getCandidate(NJ_CLASS *iwnn, NJC_CANDIDATE *x, NJ_RESULT *result);
static NJ_INT16 cand_fzkProcess(NJ_CLASS *iwnn, NJC_CANDIDATE *x);
static NJ_UINT16 cand_dummyProcess(NJC_CANDIDATE *x);
static NJ_INT16 cand_replaceDummyPhrase(NJC_CANDIDATE *x, NJ_UINT8 overwrite,
                                        NJ_UINT8 fzkcheck);
static NJ_INT16 cand_getEqualDummyPhrase(NJC_CANDIDATE *x, NJ_UINT16 phr_off,
                                         NJ_UINT8 poscheck);
static void cand_sortDummyArea(NJC_CANDIDATE *x, NJ_INT16 phrID);
static NJ_INT16 cand_replacePhrase(NJ_CLASS *iwnn, NJC_CANDIDATE *x, NJ_UINT8 overwrite, NJ_INT16* phrID_after);
static NJ_INT16 cand_getEqualPhrase(NJ_CLASS *iwnn, NJC_CANDIDATE *x, NJ_UINT16 phr_off);
static NJ_INT16 cand_sortArea(NJ_CLASS *iwnn, NJC_CANDIDATE *x, NJ_INT16 phrID);
static NJ_INT16 cand_getShortestLen(NJC_CANDIDATE *x, NJ_INT16 *N,
                                    NJ_INT16 *topID);
static void cand_removeCandidate(NJC_CANDIDATE *x, NJ_UINT16 idx);
static NJ_INT16 cand_stemProcess(NJ_CLASS *iwnn, NJC_CANDIDATE *x, NJ_RESULT *result);
static NJ_INT32 cand_getScore(NJ_RESULT* result);

/**********************************************************************
 * function
 **********************************************************************/


/**
 * 変換処理
 *
 * @param[in,out] iwnn        解析情報クラス
 * @param[in]     dics          変換で利用対象となる辞書セット
 * @param[in]     yomi          変換を行う文字列(NUL止め)
 * @param[in]     analyze_level 解析文節数(1:単文節変換、1<:連文節変換)
 * @param[in]     devide_pos    文節区切り位置(0:指定なし)
 * @param[in]     results       処理結果格納バッファ<br>
 *                              analyze_level分の領域を呼出元で用意すること。
 * @param[in]     nogiji        0:変換解析過程で擬似候補生成許可、1:生成禁止
 *
 * @retval >0 resultsへ格納した処理結果数(=文節数)
 * @retval <0 異常終了
 */
NJ_INT16 njc_conv(NJ_CLASS *iwnn, NJ_DIC_SET *dics, NJ_CHAR  *yomi, NJ_UINT8 analyze_level,
                  NJ_UINT8 devide_pos, NJ_RESULT *results, NJ_UINT8 nogiji) {

    NJ_INT16 ret = 0;           /* 戻り値   */
    NJ_RESULT target;           /* 文節情報 */
    NJC_CONV *convBuf;          /* 連文節変換処理ワーク */
    NJC_SENTENCE *convResult;   /* 連文節変換処理結果保持領域 */
    NJ_INT16 i;
    NJ_UINT16 len;              /* 変換対象読み文字列 */
    NJ_CHAR  swap;              /* NULL設定用読み文字退避エリア */
    NJ_INT16 gfpos, gbpos;      /* 擬似品詞番号 */
    NJ_RESULT *target_ptr;


    /*
     * 引数チェック
     */

    /*
     * 第1引数(iwnn)がNULLであれば、異常とする。
     */
    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_CONV, NJ_ERR_PARAM_ENV_NULL); /*NCH_FB*/
    }

    /*
     * 処理結果格納バッファがNULL(未指定)の場合は異常
     */
    if (results == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_CONV, NJ_ERR_PARAM_RESULT_NULL);
    }

    /*
     * 読み文字列のチェック
     */
    if (yomi == NULL) {
        /*
         * 変換を行う読み文字列がNULL(未指定)の場合は異常
         */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_CONV, NJ_ERR_PARAM_YOMI_NULL);
    } else {

        if (iwnn->njc_mode == NJC_MODE_MORPHO) {
            if (NJ_CHAR_STRLEN_IS_0(yomi)) {
                return 0;
            }
            if (devide_pos != 0) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJC_CONV, NJ_ERR_PARAM_KUGIRI);/*NCH*/
            }
        } else {
            len = nj_strlen(yomi);

            if ((len == 0) || (len > NJ_MAX_LEN)) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJC_CONV, NJ_ERR_PARAM_YOMI_SIZE);
            }

            /*
             * 文節区切り位置が、読み文字配列長−１よりも大きい場合は異常
             */
            if (len < (NJ_UINT16)devide_pos) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJC_CONV, NJ_ERR_PARAM_KUGIRI);
            }
        }

    }
    /* キャッシュ検索モード無効にする。*/
    dics->mode = 0x0000;
    /*
     * 解析文節数が規定(1以上NJ_MAX_PHRASE以下)の場合は異常
     */
    /*
     * ただし、形態素解析ではチェックしない
     */
    if (iwnn->njc_mode != NJC_MODE_MORPHO) {
        if ((analyze_level < 1) || (analyze_level > NJ_MAX_PHRASE)) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJC_CONV, NJ_ERR_PARAM_ILLEGAL_LEVEL); /*NCH_FB*/
        }
    }

    if (analyze_level == 1) {
        /*
         * [単文節変換]
         */
        /* 全候補取得条件設定 */
        rlt_init(&target);
        target.word.yomi = yomi;
        NJ_SET_YLEN_TO_STEM(&target.word, nj_strlen(yomi));

        /* 全候補取得を行い、その第１候補を単文節変換結果とする */
        ret = njc_zenkouho1(iwnn, dics, &target, &results[0]);
        if (ret == 0) {
            /*
             * 単文節変換結果の取得に失敗した場合は、
             * 擬似候補を返す。
             */
            gfpos = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_GIJI_F);
            gbpos = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_GIJI_B);
            if ((gfpos == 0) || (gbpos == 0)) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJC_CONV, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
            }
            rlt_setHiraGiji(&results[0], (NJ_UINT16)nj_strlen(yomi), gfpos, gbpos);
            results[0].word.yomi = target.word.yomi;

            /* 戻り値に１を格納 */
            ret = 1;
        }
        if (ret > 0) {
            /* 正常に取得できた場合は、リターン値に候補数１を設定 */
            ret = 1;
            /* オペレーションIDを単文節変換の結果に変更する */
            results[0].operation_id &= ~NJ_FUNC_MASK;
            results[0].operation_id |= NJ_FUNC_ZENKOUHO;
            results[0].operation_id &= ~NJ_OP_MASK;
            results[0].operation_id |= NJ_OP_CONVERT;
        } /* その他は異常 */

    } else {
        /*
         * [連文節変換-文節区切位置指定あり]
         */
        if (devide_pos != 0) {
            convResult = NULL;
            if (nj_strlen(yomi) > devide_pos) {
                /* 区切り位置後方の読みを連文節変換 */

                /* 連文節変換処理結果保持領域 初期化 */
                convResult = &(iwnn->m_ConvResult);
                sent_init(convResult);
                if (&results[1] != NULL) {
                    convResult->phrases = &results[1];
                }
                convResult->phrMax = analyze_level - 1;

                /* 連文節変換処理ワーク 初期化 */
                convBuf = &(iwnn->m_Buf.Conv);
                conv_init(convBuf);

                /* 連文節解析条件設定 */
                convResult->yomi = yomi + devide_pos;
                convBuf->cond.nogiji = 0; /* 固定：擬似候補生成可能 */
                convBuf->cond.level = analyze_level - 1;
                convBuf->cond.ds = dics;
                convBuf->maxRltLen = NJ_MAX_RESULT_LEN;

                /* 連文節解析実行 */
                ret = conv_multiConv(iwnn, convBuf, convResult);
                if (ret == 0) {
                    /*
                     * 連文節変換結果の取得に失敗した場合は、
                     * 擬似候補を返す。
                     */
                    gfpos = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_GIJI_F); /*NCH_FB*/
                    gbpos = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_GIJI_B); /*NCH_FB*/
                    if ((gfpos == 0) || (gbpos == 0)) { /*NCH_FB*/
                        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_CONV, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
                    }
                    rlt_setHiraGiji(&results[1], /*NCH_FB*/
                                    (NJ_UINT16)nj_strlen(yomi + devide_pos), gfpos, gbpos);
                    results[1].word.yomi = yomi + devide_pos; /*NCH_FB*/

                    /* 戻り値に1を格納する */
                    ret = 1; /*NCH_FB*/
                }
                if (ret < 0) {
                    /* 連文節変換処理で異常が発生した */
                    return ret; /*NCH_FB*/
                }
            }

            /* 区切り位置前方の読みを単文節変換 */
            rlt_init(&target);
            target.word.yomi = yomi;
            NJ_SET_YLEN_TO_STEM(&target.word, devide_pos);

            /* 読み文字列をNULLターミネートする */
            swap = *(target.word.yomi + devide_pos);
            *(target.word.yomi + devide_pos) = NJ_CHAR_NUL;

            if (convResult == NULL) {  /* 連文節変換未実行 */
                len = 0;
            } else {
                len = convResult->reprLen;
            }
            target_ptr = &target;
            while ((ret = njc_zenkouho1(iwnn, dics, target_ptr, &results[0])) > 0) {
                target_ptr = NULL;
                if ((RLT_GETREPRLEN(&results[0]) + len) <= NJ_MAX_RESULT_LEN) {
                    /*
                     * 全候補結果と連文節変換結果の総表記長が最大長を超えていなければ
                     * その全候補結果を単文節変換結果として、確定する。
                     */
                    break;
                }
            }

            /* 読み文字列をNULLターミネートを解除する */
            *(target.word.yomi + devide_pos) = swap;

            if (ret > 0) { /* 全候補取得正常 */
                /* 戻り値に取得した文節数を格納する */
                if (convResult == NULL) {  /* 連文節変換未実行時(=読み文字列最終に区切り位置) */
                    ret = 1;
                } else {
                    ret = convResult->phrNum + 1;
                }
            } else if (ret == 0) {
                gfpos = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_GIJI_F);
                gbpos = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_GIJI_B);
                if ((gfpos == 0) || (gbpos == 0)) {
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJC_CONV, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
                }
                rlt_setHiraGiji(&results[0], (NJ_UINT16)devide_pos, gfpos, gbpos);
                results[0].word.yomi = target.word.yomi;
                
                /* ２文節目移行の処理 */
                if (nj_strlen(target.word.yomi + devide_pos) == 0) {
                    /* 入力文字列長と文節区切りが同じ場合は、一文節で返す。*/
                    ret = 1;
                } else {
                    /* 先頭文節の長さ(devide_pos)＋２文節目以降の長さ(len)をチェック */
                    if ((convResult != NULL) && ((devide_pos + len) <= NJ_MAX_RESULT_LEN)) {
                        /* 連文節変換実行済みで最大変換候補長を超えない場合、その連文節変換結果を利用する。*/
                        ret = convResult->phrNum + 1;
                    } else {
                        /* 先頭文節以降を１文節の平仮名擬似候補にする */
                        rlt_setHiraGiji(&results[1], nj_strlen(target.word.yomi + devide_pos), gfpos, gbpos);
                        results[1].word.yomi = target.word.yomi + devide_pos;

                        /* 戻り値に取得した文節数(擬似候補文節数２)を格納する */
                        ret = 2;
                    }

                }
            }

            /* オペレーションIDを連文節変換の結果に変更する */
            for (i = 0; (i < ret) && (i < analyze_level); i++) {
                /* オペレーションIDを連文節変換の結果に変更する */
                results[i].operation_id &= ~NJ_FUNC_MASK;
                results[i].operation_id |= NJ_FUNC_CONVERT_MULTIPLE;
                results[i].operation_id &= ~NJ_OP_MASK;
                results[i].operation_id |= NJ_OP_CONVERT;
            }

        } else {
            /*
             * [連文節変換-文節区切位置指定なし]
             */

            /* 連文節変換処理結果保持領域 初期化 */
            convResult = &(iwnn->m_ConvResult);
            sent_init(convResult);
            convResult->phrases = results; /* 結果格納領域割り当て */
            convResult->phrMax = analyze_level;

            /* 連文節変換処理ワーク 初期化 */
            convBuf = &(iwnn->m_Buf.Conv);
            conv_init(convBuf);

            /* 連文節解析条件設定 */
            convResult->yomi = yomi;
            convBuf->cond.nogiji = (NJ_UINT8)(nogiji ? 1 : 0);
            convBuf->cond.level = analyze_level;
            convBuf->cond.ds = dics;
            if (iwnn->njc_mode == NJC_MODE_MORPHO) {
                convBuf->maxRltLen = MM_MAX_MORPHO_LEN;
            } else {
                convBuf->maxRltLen = NJ_MAX_RESULT_LEN;
            }

            /* 連文節解析実行 */
            ret = conv_multiConv(iwnn, convBuf, convResult);
            if (ret == 0) {
                /* 候補なし */
                if (nogiji == 1) { /*NCH_FB*/
                    ret = 0; /*NCH_FB*/
                } else {
                    /*
                     * 連文節変換結果の取得に失敗した場合は、
                     * 擬似候補を返す。
                     */
                    gfpos = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_GIJI_F); /*NCH_FB*/
                    gbpos = njd_r_get_hinsi(dics->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_GIJI_B); /*NCH_FB*/
                    if ((gfpos == 0) || (gbpos == 0)) { /*NCH_FB*/
                        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_CONV, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
                    }
                    
                    if (iwnn->njc_mode == NJC_MODE_MORPHO) {
                        len = (NJ_UINT16)nj_strlen_limit(yomi, MM_MAX_MORPHO_LEN);
                    } else {
                        len = (NJ_UINT16)nj_strlen(yomi); /*NCH_FB*/
                    }
                    rlt_setHiraGiji(&results[0], len, gfpos, gbpos); /*NCH_FB*/
                    results[0].word.yomi = yomi; /*NCH_FB*/

                    /* 戻り値に1を格納する */
                    ret = 1; /*NCH_FB*/
                }

            }

            if (ret > 0) {
                /* 取得した候補を連文節変換結果として格納する */
                for (i = 0; (i < ret) && (i < analyze_level); i++) {
                    /* オペレーションIDを連文節変換の結果に変更する */
                    results[i].operation_id &= ~NJ_FUNC_MASK;
                    results[i].operation_id &= ~NJ_OP_MASK;
                    if (iwnn->njc_mode == NJC_MODE_MORPHO) {
                        results[i].operation_id |= NJ_FUNC_SPLIT_WORD;
                        results[i].operation_id |= NJ_OP_MORPHOLIZE;
                    } else {
                        results[i].operation_id |= NJ_FUNC_CONVERT_MULTIPLE;
                        results[i].operation_id |= NJ_OP_CONVERT;
                    }
                }
                ret = i;
            }
        }
    }

    return ret;
}


/**
 * 連文節変換処理
 *
 * @param[in,out] iwnn   解析情報クラス
 * @param[in]     work     連文節変換処理ワーク
 * @param[out]    result   連文節変換処理結果保持領域
 *
 * @retval >0 resultsへ格納した処理結果数(=文節数)<br>
 *        (work->cond.nogiji(1)以外、０は返らない)
 * @retval <0 異常終了
 */
static NJ_INT16 conv_multiConv(NJ_CLASS *iwnn, NJC_CONV *work,
                               NJC_SENTENCE *result) {

    NJ_UINT8 giji_last;                 /* 擬似候補作成フラグ */
    NJC_WORK_SENTENCE *sent, *ss;
    NJ_INT16 add;                       /* 解析時の追加文数   */
    NJ_INT16 ret;                       /* 戻り値             */
    NJ_INT16 i, j;
    NJ_UINT16 len;                      /* 読み文字列長       */
    NJ_RESULT *phr;
    NJ_UINT8 decidePhraseNumber;        /* 最大評価分文節保持数  */
    NJ_UINT8 first;
    NJ_UINT16 limit;
    NJ_UINT16 tmpReprLen;
    NJ_UINT8 flag_enable_short_top_sentence;
    /* 最大評価文が１文節でも存在を許容するためのフラグ */
    NJ_UINT8  force = 0;                 /* 強制解析続行フラグ */
    NJC_PHRASE_INFO info;


    /* 連文節解析用環境 初期化 */
    if ((ret = conv_env_set(iwnn, work)) < 0) {
        return ret;
    }

    /* 最大評価文が１文節であることを許容するのは、ループの初回実行時のみ */
    flag_enable_short_top_sentence = 1;

    sent = work->sentence;

    work->pyomi = result->yomi;
    work->restYomiLen = nj_strlen(result->yomi);


    /* 出力長上限到達フラグを寝かせておく（conv_*Process の中で立てられる）*/
    CNV_SET_OVERRUN(work, 0);

    while (result->phrNum < work->cond.level) {
        /*
         * giji_last : 以下のループ内で擬似候補を追加した後で、
         * ループを脱出する場合にONする
         */
        giji_last = 0;

        add = -1; /* 初回のみ */
        first = 0;
        limit = 0;

        /* 最大評価文が２文節で構成されるまで解析する */
        while (sent->phrNum < NJC_MAX_WORK_PHRASE) {

            if (limit++ > work->restYomiLen) {
                giji_last = 2;
                break;
            }

            if ((force == 0) && (work->restYomiLen == sent->yomiLen)) {
                /* 最大評価文において、解析が完了している場合は */
                /* 解析を終了する                               */
                break;
            }
            force = 0; /* 強制解析続行フラグをリセット */


            /*
             * 確定文節が既に最大表示出力長になっている場合
             * 解析不能と判断し、解析を中断する。
             */
            if (result->reprLen > work->maxRltLen) {
                giji_last = 2; /*NCH_FB*/
                break; /*NCH_FB*/
            }

            /*
             * 擬似候補入り文節を作成し、その後の自立語解析で
             * 新しい文が追加されない場合、付属語解析はスキップ
             */
            if (add != 0) {
                /* 自立語解析を行う */
                add = conv_stemProcess(iwnn, work, result->reprLen);
                if (add < 0) {
                    /* 自立語解析処理異常 */
                    return add;
                }

                if ((add == 1) && (work->sentNum == 1) && (work->sentence[0].reprLen == 0) &&
                    (work->sentence[0].yomiLen == 0) ) {
                    limit--;
                }

                /* 出力長が上限に達したものがあれば、解析を終了 */
                if (CNV_GET_OVERRUN(work) && (iwnn->njc_mode == NJC_MODE_MORPHO)) {
                    break;
                }
            }

            /*
             * 追加された自立語がない場合、もしくは最大評価文に自立語が生成されなかった場合は
             * 疑似候補生成ロジックを実行し、疑似候補をくっつける
             */
            if ((add == 0) ||
                ((!flag_enable_short_top_sentence)
                 && ((work->sentence[0].phrNum + work->sentence[0].vphrNum) < NJC_MAX_WORK_PHRASE)) ) {

                if (work->cond.nogiji == 1) {
                    /* 擬似候補作成禁止モード */
                    if (work->restYomiLen == sent->yomiLen) { /*NCH_FB*/
                        /* ただし、既に解析が完了している場合は */
                        /* 確定する                             */
                        break;/*NCH_FB*/
                    }
                    return 0;/*NCH_FB*/
                }

                /* 未解析の読みに対して、擬似候補を作成 */
                if ((ret = conv_gijiProcess(iwnn, work, result->reprLen, first)) < 0) {
                    /* 擬似候補作成異常 */
                    return ret;  /*NCH*/
                }
                /* 出力長が上限に達したものがあれば、解析を終了 */
                if (CNV_GET_OVERRUN(work) && (iwnn->njc_mode == NJC_MODE_MORPHO)) {
                    break;
                }

                first = 1;

                if (ret == 0) {
                    /*
                     * 擬似候補追加不可の状態は、これ以上解析不可である
                     * 状態を指すので、解析を中断する。
                     */
                    giji_last = 2; /*NCH*/
                    break; /*NCH*/
                }

                /* 付属語解析を行う */
                if ((ret = conv_fzkProcess(iwnn, work, result->reprLen)) < 0) {
                    /* 付属語解析処理異常 */
                    return ret; /*NCH*/
                }
                /* 出力長が上限に達したものがあれば、解析を終了 */
                if (CNV_GET_OVERRUN(work) && (iwnn->njc_mode == NJC_MODE_MORPHO)) {
                    break;
                }

                /*
                 * 最大評価文が２文節解析まで完了していた場合、
                 * 一旦解析を中断し、先頭文節を確定する
                 */
                if (sent->phrNum >= NJC_MAX_WORK_PHRASE) {
                    giji_last = 1; /* 擬似候補を追加した直後 */
                    break;
                }

                /* 擬似候補(+付属語)文節の後から，自立語解析を行う */
                add = conv_stemProcess(iwnn, work, result->reprLen);
                if (add < 0) {
                    /* 自立語解析処理異常 */
                    return add; /*NCH*/
                }
                /* 出力長が上限に達したものがあれば、解析を終了 */
                if (CNV_GET_OVERRUN(work) && (iwnn->njc_mode == NJC_MODE_MORPHO)) {
                    break;
                }
            }

            /*
             * 擬似候補入り文節を作成し、その後の自立語解析で
             * 新しい文が追加されない場合、付属語解析はスキップ
             */
            if (add != 0) {
                /* 付属語解析を行う */
                if ((ret = conv_fzkProcess(iwnn, work, result->reprLen)) < 0) {
                    /* 付属語解析処理異常 */
                    return ret; /*NCH*/
                }
                add += ret;
                /* 出力長が上限に達したものがあれば、解析を終了 */
                if (CNV_GET_OVERRUN(work) && (iwnn->njc_mode == NJC_MODE_MORPHO)) {
                    break;
                }
            }

            /* 最大評価文が１文節であることを許容するのは、ループの初回実行時のみ */
            flag_enable_short_top_sentence = 0;
        }

        /* njc_top_convからのコール時、解析を中断する */
        if (work->cond.top_conv == 1) {
            return 0;
        }

        /* 出力長が上限に達したものがあれば、解析を終了 */
        if (CNV_GET_OVERRUN(work) && (iwnn->njc_mode == NJC_MODE_MORPHO)) {
            tmpReprLen = RLT_GETYOMILEN(&(sent->phrases[0]));
            if ((sent->phrNum == NJC_MAX_WORK_PHRASE) &&
                ((result->reprLen + tmpReprLen) <= work->maxRltLen)) {
                (void)sent_copyPhrase(iwnn, work, result, sent, 1);
                break;
            }

            if ((sent->phrNum == 1) &&
                (NJ_GET_FPOS_FROM_STEM(&(sent->phrases[0].word)) == work->hinsi.f_giji) &&
                (NJ_GET_BPOS_FROM_STEM(&(sent->phrases[0].word)) == work->hinsi.r_giji) &&
                (RLT_GETYOMILEN(&(sent->phrases[0])) >= (MM_MAX_MORPHO_LEN / 2))) {
                (void)sent_copyPhrase(iwnn, work, result, sent, 1);
                break;
            }

            if ((result->phrNum == 0) && (sent->phrNum == 1) && RLT_HASFZK(&sent->phrases[0])) {
                for (i = 0; i < work->sentNum; i++) {
                    if (CONNECT_R(work->sentence[i].connect, work->hinsi.f_v2)) {
                        (void)sent_copyPhrase(iwnn, work, result, &work->sentence[i], 1);
                        break;
                    }
                }
                if (i >= work->sentNum) {
                    /*
                     * 終止形で完了する解析文がない場合、最高評価文(sent)の自立語部分が
                     * 終止形であることを条件として、自立語のみを確定する。
                     */
                    if (NJ_GET_KLEN_FROM_STEM(&sent->phrases[0].word)) { /*NCH_FB*/
                        if (connect(work->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], &work->hinsi, /*NCH_FB*/
                                    NJ_GET_BPOS_FROM_STEM(&sent->phrases[0].word), work->hinsi.f_v2)) { /*NCH_FB*/
                            /* 付属語情報クリア */
                            sent->phrases[0].word.fzk.info1 = 0; /*NCH_FB*/
                            sent->phrases[0].word.fzk.info2 = 0; /*NCH_FB*/
                            sent->phrases[0].word.fzk.hindo = 0; /*NCH_FB*/
                            (void)sent_copyPhrase(iwnn, work, result, sent, 1); /*NCH_FB*/
                            break; /*NCH_FB*/
                        }
                    }

                    /*
                     * その他は、形態素解析不能である為、全てを擬似候補として
                     * 確定する。(現文法上存在しないと予想される。)
                     */
                    NJ_SET_YLEN_TO_STEM(&sent->phrases[0].word, /*NCH_FB*/
                                        NJ_GET_YLEN_FROM_STEM_EXT(&sent->phrases[0].word) +
                                        NJ_GET_YLEN_FROM_FZK(&sent->phrases[0].word));
                    NJ_SET_KLEN_TO_STEM(&sent->phrases[0].word, /*NCH_FB*/
                                        NJ_GET_KLEN_FROM_STEM(&sent->phrases[0].word) +
                                        NJ_GET_KLEN_FROM_FZK(&sent->phrases[0].word));
                    /* 付属語情報クリア */
                    sent->phrases[0].word.fzk.info1 = 0; /*NCH_FB*/
                    sent->phrases[0].word.fzk.info2 = 0; /*NCH_FB*/
                    sent->phrases[0].word.fzk.hindo = 0; /*NCH_FB*/
                    /* 擬似品詞設定 */
                    NJ_SET_FPOS_TO_STEM(&sent->phrases[0].word, work->hinsi.f_giji); /*NCH_FB*/
                    NJ_SET_BPOS_TO_STEM(&sent->phrases[0].word, work->hinsi.r_giji); /*NCH_FB*/
                    (void)sent_copyPhrase(iwnn, work, result, sent, 1); /*NCH_FB*/
                    break; /*NCH_FB*/
                }
            }
            break;
        }


        /* 解析不可状態発生 */
        if (giji_last == 2) {
            if (iwnn->njc_mode != NJC_MODE_MORPHO) {
                /*
                 * 最大候補出力長(work->maxRltLen)を超過した場合は、
                 * 全読みを擬似候補として文節を最適解とする
                 */
                result->phrNum = 1;
                phr = result->phrases;
                rlt_setHiraGiji(phr, nj_strlen(result->yomi), work->hinsi.f_giji, work->hinsi.r_giji);
                phr->word.yomi = result->yomi;
                result->reprLen = nj_strlen(result->yomi);
            } else {
                if (result->phrNum == 0) {
                    (void)sent_copyPhrase(iwnn, work, result, sent, 1);
                }
            }
            break;
        }

        if ((!giji_last) && (work->restYomiLen <= sent->yomiLen)) {
            /*
             * 終止形でない場合は、次に評価の高い候補を確定する
             * ただし、他に候補が無い場合は、最終文節を擬似候補と
             * して確定する。
             */
            j = 0; /* 最適解検出フラグとして使用(0:未検出,1:検出) */
            for (i = 0; (i < work->sentNum) && (j == 0); i++) {
                /* 全ての読みが解析されていて、かつ終止形の解析文を探す */
                if (work->restYomiLen == work->sentence[i].yomiLen) {
                    /* ここでは、文末である文節に対しての終端接続チェックを行う。*/
                    if (CONNECT_R_SENTENCE_END(iwnn->njc_mode, work->sentence[i].connect, work->hinsi)) {
                        if (sent_copyPhrase(iwnn, work, result, &work->sentence[i], (NJ_INT16)work->sentence[i].phrNum) >=
                            work->sentence[i].phrNum) {
                            j = 1;
                            break;      /* 最適解確定 */
                        }
                    }
                } else {
                    break;
                }
            }
            if (j) {
                break;  /* 最適解確定 */
            }

            /*
             * 最適解がまだ決まらない場合
             *  + 全入力読みが解析されているが、全て非終止形の場合
             */
            if (work->restYomiLen == sent->yomiLen) {
                if (sent->phrNum < NJC_MAX_WORK_PHRASE) {
                    force = 1;
                    continue;
                }
            }
        }

        ss = work->sentence;
        for (i = 0; i < work->sentNum; i++, ss++) {
            if ((CONNECT_R_PHRASE_END(iwnn->njc_mode, ss->connect, work->hinsi) &&
                 (WSENT_GETLASTYOMILEN(ss) != 0)) ||
                ((WSENT_GETLASTFLAG(ss) & NJC_WS_FLAG_ASSUME_TWO_PHRASES) != 0) ) {
                if ((ss->yomiLen < work->restYomiLen) &&
                    REPR_LEN_EXCEED(iwnn->njc_mode, work, result->reprLen, ss->reprLen, ss->yomiLen)) {
                    ss->invalid = 1; /*NCH_FB*/
                    continue; /*NCH_FB*/
                }
                break;
            }
        }

        if (i == work->sentNum) {
            ss = sent;
        }


        if (sent_copyPhrase(iwnn, work, result, ss,
                            (NJ_INT16)(ss->phrNum - 1)) < (NJ_UINT16)(ss->phrNum - 1)) {
            return NJ_SET_ERR_VAL(NJ_FUNC_CONV_MULTICONV, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
        }
        decidePhraseNumber = ss->phrNum;

        /* 結果にコピーした文節の読み長を取得 */
        len = ss->yomiLen - RLT_GETYOMILEN(&(ss->phrases[ss->phrNum - 1]));

        /* 未解析読み文字列情報更新 */
        work->pyomi += len;
        work->restYomiLen -= len;

        /* 先頭文節の文節付加情報を取得（最大評価文自体も以下のループ内で reduce するので、先に情報をコピー） */
        info = ss->info[0];

        for (i = 0; (i < work->sentNum) && (i <= NJC_MAX_WORK_CANDIDATE); i++) {
            ss = &(work->sentence[i]);
            if ((ss->phrNum != decidePhraseNumber) ||
                (len != (ss->yomiLen - RLT_GETYOMILEN(&(ss->phrases[ss->phrNum-1])))) ||
                (work->sentence[i].invalid == 1) ||
                (ss->info[0].flag != info.flag) ) {
                for (j = i + 1; j < work->sentNum; j++) {
                    if (j != (i + 1)) {
                        ss++;
                    }
                    *ss = *(ss + 1);
                }
                work->sentNum--;
                i--;
                continue;
            }
            /*
             * 残す解析文の先頭文節を破棄し、２文節目の文節を
             * 先頭文節に移動する
             */
            wsent_reducePhrases(iwnn, ss);
        }

    }

    return result->phrNum;
}


/**
 * NJC_WORK_SENTENCEクラスから先頭 num 個の文節を取り出してコピー(NJC_SENTENCE の末尾に追加)する
 *
 * @param[in]  iwnn   解析情報クラス
 * @param[in]  xx     連文節変換処理ワーク
 * @param[out] x      連文節変換結果格納領域
 * @param[in]  sent   作業用文情報保持領域
 * @param[in]  num    コピー文節数
 *
 * @return コピーした文節数
 */
static NJ_UINT16 sent_copyPhrase(NJ_CLASS *iwnn, NJC_CONV *xx, NJC_SENTENCE *x,
                                 NJC_WORK_SENTENCE *sent, NJ_INT16 num) {

    struct NJ_FZK fzk;
    NJ_SEARCH_LOCATION_SET loctset;
    NJ_DIC_FREQ dic_freq = {0, 0};
    NJ_RESULT *now, *next, *target_phr;
    NJ_INT16 ret, i;
    NJ_UINT16 j;
    NJ_UINT16 len, total_len;
    NJ_UINT16 total_phrase;
    NJ_UINT8 cont_num;

    NJ_CHAR  *yomi, *div_yomi;
    NJ_UINT8 char_type, lastchar_type, char_follow, char_change;
    NJ_UINT16 conn_phrase;
    NJ_CHAR  *conn_yomi;
    NJ_UINT8  rlt_f_len, rlt_r_len;
    NJ_RESULT *rlt_f, *rlt_r;
    loctset.dic_freq_max = NJ_STATE_MAX_FREQ;
    loctset.dic_freq_min = NJ_STATE_MIN_FREQ;
    total_len    = x->reprLen;  /* 候補文字列長 */
    total_phrase = x->phrNum;   /* 文節数       */
    conn_phrase  = 0;           /* ヒューリスティックで連結した文節数 */
    target_phr = NULL;

    for (i = 0; i < num; i++) {

        /* 候補文字列長のチェック */
        if (iwnn->njc_mode == NJC_MODE_MORPHO) {
            len = RLT_GETYOMILEN((&sent->phrases[i]));
        } else {
            len = RLT_GETREPRLEN((&sent->phrases[i]));
        }
        if ((len + total_len) > xx->maxRltLen) {
            /* 最大表記長を超える場合は追加しない */
            return 0; /*NCH_FB*/
        }
        total_len += len;

        /* 処理結果(NJ_RESULT) をチェックして cont_num を適切に設定する */
        now = &(sent->phrases[i]);
        if (now->word.stem.loc.handle != NULL) {
            if((x->flag_latest & NJC_WS_FLAG_ASSUME_TWO_PHRASES) != 0) {
                x->flag_latest = NJC_WS_FLAG_DEFAULT;   /* NCH_FB */
                return 0; /*NCH_FB*/
            }
            cont_num = (NJ_UINT8)(now->word.stem.loc.current_info >> 4) & 0x0F;
            if (cont_num == 0) {
                cont_num = 1; /*NCH_FB*/
            }
        } else {
            if((x->flag_latest & NJC_WS_FLAG_ASSUME_TWO_PHRASES) != 0) {
                conn_phrase++;
                total_phrase--;
                target_phr = &(x->phrases[total_phrase]);

                /*
                 * 直前の文節（target_phr）に対象文節（now）の付属語を結合する（conv_fzkProcess を参照のこと）。
                 */
                if (!RLT_HASFZK(target_phr)) {
                    NJ_SET_FPOS_TO_FZK(&target_phr->word, NJ_GET_FPOS_FROM_FZK(&now->word));
                }
                NJ_SET_BPOS_TO_FZK(&target_phr->word, NJ_GET_BPOS_FROM_FZK(&now->word));

                NJ_SET_YLEN_TO_FZK(&target_phr->word, NJ_GET_YLEN_FROM_FZK(&target_phr->word) + NJ_GET_YLEN_FROM_FZK(&now->word));
                NJ_SET_KLEN_TO_FZK(&target_phr->word, NJ_GET_YLEN_FROM_FZK(&target_phr->word));

                if (NJ_GET_FREQ_FROM_FZK(&target_phr->word) < NJ_GET_FREQ_FROM_FZK(&now->word)) {
                    NJ_SET_FREQ_TO_FZK(&target_phr->word, (NJ_GET_FREQ_FROM_FZK(&now->word)));
                }

                if (target_phr->word.stem.loc.handle != NULL) {
                    cont_num = (NJ_UINT8)(target_phr->word.stem.loc.current_info >> 4) & 0x0F;
                    if (cont_num == 0) {
                        cont_num = 1; /*NCH_FB*/
                    }
                } else {
                    /* 付属語のみの文節の場合、current_infoは不定値である */
                    cont_num = 1;
                }
                now = NULL;
            } else {
                /* 付属語のみの文節の場合、current_infoは不定値である */
                cont_num = 1;
            }
        }

        if ((total_phrase + cont_num) > x->phrMax) {
            /* 最大文節数を超える場合は追加しない */
            return 0; /*NCH_FB*/
        }

        if ((WSENT_GETFLAG(sent,i) & NJC_WS_FLAG_ASSUME_TWO_PHRASES) != 0) {
            cont_num = 0;
        }

        /* 文節情報とフラグをコピーする(仮) */
        if (now != NULL) {
            x->phrases[total_phrase] = *now;
        } else {
            if (target_phr != NULL) {
                now = target_phr;
                target_phr = NULL;
            }
        }
        x->flag_latest = WSENT_GETFLAG(sent,i);

        if (cont_num == 1) {

            /*
             * 形態素解析処理の場合、疑似候補を字種毎にバラバラにする
             */
            if ((iwnn->njc_mode == NJC_MODE_MORPHO) && (now->word.stem.loc.handle == NULL) &&
                (NJ_GET_YLEN_FROM_STEM_EXT(&(now->word)) > 0)) {
                /*
                 * 完全なる疑似候補のみ、疑似候補分割ヒューリスティックを適用。
                 * cont_num が 1 以外の場合、辞書候補のはずなので、適用しない。
                 */

                /*
                 * 付属語は分割対象とならないため、ループを手前で止めるようにする
                 */
                len -= NJ_GET_YLEN_FROM_FZK(&(now->word));

                /*
                 * 文節を最初の文字からざっとなめて
                 * 字種が変わるところで、後ろの x->phrases に入れていく
                 */
                yomi = now->word.yomi;
                div_yomi = yomi;

                lastchar_type = nje_get_top_char_type(yomi);
                char_follow = lastchar_type;
                len  -= NJ_CHAR_LEN(yomi);
                yomi += NJ_CHAR_LEN(yomi);
                total_phrase++;

                while (len > 0) {
                    char_change = 1;
                    char_type = nje_get_top_char_type(yomi);

                    /*
                     * 擬似候補の文字種変化点を補正する
                     */
                    if (char_type != lastchar_type) {
                        /*
                         * 解析文字列属性(char_follow)から
                         * 文字種の変化点かを判定
                         */
                        switch (char_follow) {
                        case NJ_CHAR_TYPE_ZEN_SUUJI:
                            if ((char_type == NJ_CHAR_TYPE_ZEN_SUUJI) ||
                                (char_type == NJ_CHAR_TYPE_ZEN_SUUJI_SIG)) {
                                /*
                                 * 全角数字の擬似候補判別中
                                 */
                                char_change = 0;
                            }
                            break;
                        case NJ_CHAR_TYPE_ZEN_SUUJI_SIG:
                            if ((char_type == NJ_CHAR_TYPE_ZEN_SUUJI) ||
                                (char_type == NJ_CHAR_TYPE_ZEN_SUUJI_SIG)) {
                                /*
                                 * 全角数字の擬似候補判別中
                                 */
                                char_follow = NJ_CHAR_TYPE_ZEN_SUUJI;
                                char_change = 0;
                            } else if (char_type == NJ_CHAR_TYPE_HIRAGANA) {
                                /*
                                 * ひらがなの擬似候補判別中
                                 */
                                char_follow = NJ_CHAR_TYPE_HIRAGANA;
                                char_change = 0;
                            } else if (char_type == NJ_CHAR_TYPE_KATAKANA) {
                                /*
                                 * 全角カナの擬似候補判別中
                                 */
                                char_follow = NJ_CHAR_TYPE_KATAKANA;
                                char_change = 0;
                            } else if (char_type == NJ_CHAR_TYPE_KANA_SIG) {
                                /*
                                 * ひらがな、また全角カナの擬似候補判別中
                                 */
                                char_follow = NJ_CHAR_TYPE_KANA_SIG;
                                char_change = 0;
                            } else if (char_type == NJ_CHAR_TYPE_ZEN_UNDEFINE) {
                                /*
                                 * 未定義全角文字の擬似候補判別中
                                 */
                                char_follow = NJ_CHAR_TYPE_ZEN_UNDEFINE;
                                char_change = 0;
                            }
                            break;
                        case NJ_CHAR_TYPE_HAN_SUUJI:
                            if ((char_type == NJ_CHAR_TYPE_HAN_SUUJI) ||
                                (char_type == NJ_CHAR_TYPE_HAN_SUUJI_SIG)) {
                                /*
                                 * 半角数字の擬似候補判別中
                                 */
                                char_change = 0;
                            }
                            break;
                        case NJ_CHAR_TYPE_HAN_SUUJI_SIG:
                            if ((char_type == NJ_CHAR_TYPE_HAN_SUUJI) ||
                                (char_type == NJ_CHAR_TYPE_HAN_SUUJI_SIG)) {
                                /*
                                 * 半角数字の擬似候補判別中
                                 */
                                char_follow = NJ_CHAR_TYPE_HAN_SUUJI;
                                char_change = 0;
                            } else if (char_type == NJ_CHAR_TYPE_HANKATA) {
                                /*
                                 * 半角カナの擬似候補判別中
                                 */
                                char_follow = NJ_CHAR_TYPE_HANKATA;
                                char_change = 0;
                            } else if (char_type == NJ_CHAR_TYPE_HKATA_SIG) {
                                /*
                                 * 半角カナの擬似候補判別中
                                 */
                                char_follow = NJ_CHAR_TYPE_HKATA_SIG;
                                char_change = 0;
                            } else if (char_type == NJ_CHAR_TYPE_HAN_UNDEFINE) {
                                /*
                                 * 未定義半角文字の擬似候補判別中
                                 */
                                char_follow = NJ_CHAR_TYPE_HAN_UNDEFINE;
                                char_change = 0;
                            }
                            break;
                        case NJ_CHAR_TYPE_HIRAGANA:
                            if ((char_type == NJ_CHAR_TYPE_HIRAGANA) ||
                                (char_type == NJ_CHAR_TYPE_ZEN_SUUJI_SIG) ||
                                (char_type == NJ_CHAR_TYPE_KANA_SIG)) {
                                /*
                                 * ひらがなの擬似候補判別中
                                 */
                                char_change = 0;
                            }
                            break;
                        case NJ_CHAR_TYPE_KATAKANA:
                            if ((char_type == NJ_CHAR_TYPE_KATAKANA) ||
                                (char_type == NJ_CHAR_TYPE_ZEN_SUUJI_SIG) ||
                                (char_type == NJ_CHAR_TYPE_KANA_SIG)) {
                                /*
                                 * 全角カナの擬似候補判別中
                                 */
                                char_change = 0;
                            }
                            break;
                        case NJ_CHAR_TYPE_KANA_SIG:
                            if (char_type == NJ_CHAR_TYPE_HIRAGANA) {
                                /*
                                 * ひらがなの擬似候補判別中
                                 */
                                char_follow = NJ_CHAR_TYPE_HIRAGANA;
                                char_change = 0;

                            } else if (char_type == NJ_CHAR_TYPE_KATAKANA) {
                                /*
                                 * 全角カナの擬似候補判別中
                                 */
                                char_follow = NJ_CHAR_TYPE_KATAKANA;
                                char_change = 0;
                            } else if (char_type == NJ_CHAR_TYPE_ZEN_UNDEFINE) {
                                /*
                                 * 未定義全角文字の擬似候補判別中
                                 */
                                char_follow = NJ_CHAR_TYPE_ZEN_UNDEFINE;
                                char_change = 0;
                            }
                            break;
                        case NJ_CHAR_TYPE_HANKATA:
                            if ((char_type == NJ_CHAR_TYPE_HANKATA) ||
                                (char_type == NJ_CHAR_TYPE_HAN_SUUJI_SIG) ||
                                (char_type == NJ_CHAR_TYPE_HKATA_SIG)) {
                                /*
                                 * 半角カナの擬似候補判別中
                                 */
                                char_change = 0;
                            }
                            break;
                        case NJ_CHAR_TYPE_HKATA_SIG:
                            if (char_type == NJ_CHAR_TYPE_HANKATA) {
                                /*
                                 * 半角カナの擬似候補判別中
                                 */
                                char_follow = NJ_CHAR_TYPE_HANKATA;
                                char_change = 0;
                            } else if (char_type == NJ_CHAR_TYPE_HAN_UNDEFINE) {
                                /*
                                 * 未定義半角文字の擬似候補判別中
                                 */
                                char_follow = NJ_CHAR_TYPE_HAN_UNDEFINE;
                                char_change = 0;
                            }
                            break;

                        default:
                            break;
                        }
                    } else {
                        /*
                         * 文字種の変化なし
                         */
                        char_change = 0;
                    }

                    /*
                     * 文字種の変化点を見つけたので、その手前までを１つの文節とする
                     */
                    if (char_change) {
                        if (char_follow == NJ_CHAR_TYPE_ZEN_SUUJI) {
                            if (lastchar_type != NJ_CHAR_TYPE_ZEN_SUUJI_SIG) {
                                NJ_SET_BPOS_TO_STEM(&(x->phrases[total_phrase - 1].word), xx->hinsi.r_ngiji);
                            }
                        } else if (char_follow == NJ_CHAR_TYPE_HAN_SUUJI) {
                            if (lastchar_type != NJ_CHAR_TYPE_HAN_SUUJI_SIG) {
                                NJ_SET_BPOS_TO_STEM(&(x->phrases[total_phrase - 1].word), xx->hinsi.r_ngiji);
                            }
                        }

                        NJ_SET_YLEN_TO_STEM(&(x->phrases[total_phrase - 1].word), yomi - div_yomi);
                        NJ_SET_YLEN_TO_FZK(&(x->phrases[total_phrase - 1].word), 0);

                        if (total_phrase < MM_MAX_MORPHO_LEN) {
                            x->phrases[total_phrase] = *now;
                            x->phrases[total_phrase].word.yomi = yomi;
                            NJ_SET_YLEN_TO_STEM(&(x->phrases[total_phrase].word), len);
                            total_phrase++;
                        } else {
                            /*
                             * 仕様上、ここにはこない
                             */
                            return 0; /* 全くコピーできなかったこととする */ /*NCH*/
                        }

                        div_yomi = yomi;
                        char_follow = char_type; /* 解析文字列属性を更新 */
                    }
                    lastchar_type = char_type;

                    len  -= NJ_CHAR_LEN(yomi);
                    yomi += NJ_CHAR_LEN(yomi);
                }
                if (char_follow == NJ_CHAR_TYPE_ZEN_SUUJI) {
                    if (lastchar_type != NJ_CHAR_TYPE_ZEN_SUUJI_SIG) {
                        NJ_SET_BPOS_TO_STEM(&(x->phrases[total_phrase - 1].word), xx->hinsi.r_ngiji);
                    }
                } else if (char_follow == NJ_CHAR_TYPE_HAN_SUUJI) {
                    if (lastchar_type != NJ_CHAR_TYPE_HAN_SUUJI_SIG) {
                        NJ_SET_BPOS_TO_STEM(&(x->phrases[total_phrase - 1].word), xx->hinsi.r_ngiji);
                    }
                }
                
            } else {
                if ((x->phrases[total_phrase].word.stem.loc.handle == NULL) &&
                    (NJ_GET_YLEN_FROM_STEM_EXT(&(x->phrases[total_phrase].word)) > 0)) {
                    j = nje_check_string(x->phrases[total_phrase].word.yomi,
                                         (NJ_UINT16)NJ_GET_YLEN_FROM_STEM_EXT(&(x->phrases[total_phrase].word)));
                    if ((j == NJ_TYPE_HAN_SUUJI) || (j == NJ_TYPE_ZEN_SUUJI)) {
                        NJ_SET_BPOS_TO_STEM(&(x->phrases[total_phrase].word), xx->hinsi.r_ngiji);
                    }
                }


                total_phrase++;
            }

            if ((iwnn->njc_mode == NJC_MODE_MORPHO) && (total_phrase > 1)) {
                rlt_f = &(x->phrases[total_phrase - 2]);
                rlt_r = &(x->phrases[total_phrase - 1]);
                rlt_f_len = NJ_GET_YLEN_FROM_STEM_EXT(&(rlt_f->word));
                rlt_r_len = NJ_GET_YLEN_FROM_STEM_EXT(&(rlt_r->word));

                if ((rlt_r_len < (NJ_KANA_CONNECT_LEN * (2 / sizeof(NJ_CHAR)))) &&
                    (NJ_GET_YLEN_FROM_FZK(&(rlt_f->word)) == 0) &&
                    (nje_check_string(rlt_r->word.yomi, rlt_r_len) == NJ_TYPE_KATAKANA) &&
                    (nje_check_string(rlt_f->word.yomi, rlt_f_len) == NJ_TYPE_KATAKANA) ) {
                    /*
                     * １つ前にあるカタカナ語文節（疑似／辞書語問わず）に連結する
                     * 連結されたカタカナ語は、強制的に疑似候補となる
                     */
                    conn_yomi = rlt_f->word.yomi;
                    rlt_setHiraGiji(rlt_f, (NJ_UINT16)(rlt_f_len + rlt_r_len), xx->hinsi.f_giji, xx->hinsi.r_giji);
                    rlt_f->word.yomi = conn_yomi;

                    /* 連結する語の付属語を、連結される語の付属語にくっつける */
                    rlt_f->word.fzk.hindo = rlt_r->word.fzk.hindo;
                    rlt_f->word.fzk.info1 = rlt_r->word.fzk.info1;
                    rlt_f->word.fzk.info2 = rlt_r->word.fzk.info2;

                    total_phrase--;
                    conn_phrase++;
                }
            }
        } else if (cont_num > 1) {
            /*
             * 合成されている文節から付属語を取り出す
             */
            fzk = now->word.fzk;
            
            /*
             * 合成されている文節は、合成された読み長、頻度などが入って
             * いる。このため、元のデータを取り戻す必要がある
             */
            loctset.cache_freq = now->word.stem.hindo;
            loctset.loct = now->word.stem.loc;

            for (j = 0; j < NJ_MAX_DIC; j++) {
                if (xx->cond.ds->dic[j].handle == now->word.stem.loc.handle) {
                    loctset.dic_freq = xx->cond.ds->dic[j].dic_freq[NJ_MODE_TYPE_HENKAN];
                    dic_freq = xx->cond.ds->dic[j].dic_freq[NJ_MODE_TYPE_HENKAN];
                    break;
                }
            }

            ret = njd_get_word_data(iwnn, xx->cond.ds, &loctset, j, &now->word);

            if (ret < 0) {
                return 0; /*NCH*/
            }

            now->word.stem.loc = loctset.loct;

            if (iwnn->njc_mode != NJC_MODE_MORPHO) { /* 形態素解析以外 */
                if ((now->word.stem.loc.handle != NULL) &&
                    (NJ_GET_DIC_TYPE_EX(now->word.stem.loc.type,
                                       now->word.stem.loc.handle) == NJ_DIC_TYPE_LEARN)) {
                    if ((NJ_UINT16)now->word.stem.hindo >
                        (((dic_freq.high - dic_freq.base) * 8) / 10 + dic_freq.base)) {
                        total_phrase++;
                        continue;
                    }
                }
            }

            /* 付属語情報クリア */
            now->word.fzk.info1 = 0;
            now->word.fzk.info2 = 0;
            now->word.fzk.hindo = 0;

            /* 合成されている文節の先頭をコピー */
            x->phrases[total_phrase] = *now;
            cont_num--;

            /* current_infoをクリアするため、nowを再設定 */
            now = &(x->phrases[total_phrase++]);

            /* 複数文節の合成結果であった */
            while (cont_num-- > 0) {
                next = &(x->phrases[total_phrase++]);
                ret = njd_get_relational_word(iwnn, now, next, &dic_freq);
                if (ret < 0) {
                    return 0; /* 全くコピーできなかったこととする */ /*NCH*/
                }

                /* よみを設定する(nowの付属語長は加算不要) */
                next->word.yomi = now->word.yomi
                    + NJ_GET_YLEN_FROM_STEM_EXT(&now->word);

                /* 複数文節の情報を削除 */
                now->word.stem.loc.current_info &= 0x0f;

                /* 次文節の付属語情報クリア(rlt_initの代用) */
                next->word.fzk.info1 = 0;
                next->word.fzk.info2 = 0;
                next->word.fzk.hindo = 0;

                now = next;
            }

            /* 保管していた付属語情報を復元する */
            now->word.fzk = fzk;
            now->word.stem.loc.current_info &= 0x0f;
        } else {
            total_phrase++;
        }
    }
    ret = total_phrase - x->phrNum + conn_phrase;
    x->reprLen = total_len;
    x->phrNum  = total_phrase;


    return ret;
}


/**
 * 連文節変換処理結果保持領域 初期化
 *
 * @param[out] s  連文節変換処理結果保持領域
 *
 * @return なし
 */
static void sent_init(NJC_SENTENCE *s) {


    s->phrases = NULL;
    s->phrMax = 0;
    s->phrNum  = 0;
    s->reprLen  = 0;

    s->flag_latest = NJC_WS_FLAG_DEFAULT;
}


/**
 * 文節情報 初期化
 *
 * @param[out] r   文節情報
 *
 * @return なし
 */
static void rlt_init(NJ_RESULT *r) {


    r->operation_id = NJ_OP_CONVERT;

    /* NJ_WORDの初期化関数を呼び出す */
    njd_init_word(&(r->word));
}


/**
 * 文節情報 付加情報 初期化
 *
 * @param[out] info   文節情報 付加情報
 *
 * @return なし
 */
static void info_init(NJC_PHRASE_INFO *info) {


    info->flag      = NJC_WS_FLAG_DEFAULT;
    info->handle    = NULL;
    info->id        = 0;
}


/**
 * 文節情報 初期化
 *
 * @param[out] r  文節情報
 *
 * @return なし
 */
static void fzk_rlt_init(NJC_FZK_RESULT *r) {


    r->info1 = 0;
    r->info2 = 0;
    r->hindo = 0;
}


/**
 * 連文節変換処理ワークを初期化する。
 *
 * @param[out]  work  連文節変換処理ワーク
 *
 * @return なし
 */
static void conv_init(NJC_CONV *work) {

    NJC_WORK_SENTENCE *p;
    NJ_INT16 i;


    p = work->sentence;
    for (i = 0; i <= NJC_MAX_WORK_CANDIDATE; i++, p++) {
        wsent_init(p);
    }
    work->sentNum = 0;
    for (i = 0; i < NJ_MAX_DIC; i++) {
        work->jds.dic[i].srhCache = NULL;
        work->jds.dic[i].limit = 0;
        work->fds.dic[i].srhCache = NULL;
        work->fds.dic[i].limit = 0;
        work->jds.dic[i].handle = NULL;
        work->fds.dic[i].handle = NULL;
    }
    INIT_KEYWORD_IN_NJ_DIC_SET(&(work->jds));
    work->jds.mode = 0;
    INIT_KEYWORD_IN_NJ_DIC_SET(&(work->fds));
    work->fds.mode = 0;
    work->jds.rHandle[NJ_MODE_TYPE_HENKAN] = NULL;
    work->fds.rHandle[NJ_MODE_TYPE_HENKAN] = NULL;
    work->jds.rHandle[NJ_MODE_TYPE_YOSOKU] = NULL;
    work->fds.rHandle[NJ_MODE_TYPE_YOSOKU] = NULL;
    work->jds.rHandle[NJ_MODE_TYPE_MORPHO] = NULL;
    work->fds.rHandle[NJ_MODE_TYPE_MORPHO] = NULL;

    work->restYomiLen = 0;
    work->pyomi = NULL;
    work->cond.ds = NULL;
    work->cond.nogiji = 0;
    work->cond.top_conv = 0;
    work->cond.level = 0;
}


/**
 * 作業用文情報保持領域を初期化する。
 *
 * @param[out]  ws 作業用文情報保持領域
 *
 * @return なし
 */
static void wsent_init(NJC_WORK_SENTENCE *ws) {

    NJ_INT16 i;
    NJ_RESULT *p;
    NJC_PHRASE_INFO *info;


    ws->score = 0;
    ws->yomiLen = 0;
    ws->reprLen = 0;
    ws->phrNum = 0;
    ws->vphrNum = 0;
    ws->invalid = 0;

    p = ws->phrases;
    info = ws->info;
    for (i = NJC_MAX_WORK_PHRASE ; i > 0 ; i--, p++, info++) {
        rlt_init(p);
        info_init(info);
    }
}


/**
 * 連文節変換処理ワーク 環境設定
 *
 * @param[in]  iwnn  解析情報クラス
 * @param[out] work    連文節変換処理ワーク
 *
 * @retval 0  正常
 * @retval <0 異常
 */
static NJ_INT16 conv_env_set(NJ_CLASS *iwnn, NJC_CONV *work) {

    NJ_DIC_INFO   *pstem, *pfzk, *ptarget;
    NJ_UINT16 i;
    NJ_UINT32 type;


    /* ルール辞書アドレス 設定 */
    work->jds.rHandle[NJ_MODE_TYPE_HENKAN] = work->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN];
    work->fds.rHandle[NJ_MODE_TYPE_HENKAN] = work->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN];
    
    for (i = 0; i < NJ_MAX_DIC; i++, pstem++, pfzk++, ptarget++) {
        /* 辞書情報アドレス 設定 */
        pstem  = &(work->jds.dic[i]);   /* 自立語解析用 */
        pfzk   = &(work->fds.dic[i]);   /* 付属語解析用 */
        ptarget= &(work->cond.ds->dic[i]);/* 解析対象辞書 */

        njd_clear_dicinfo(pstem);
        njd_clear_dicinfo(pfzk);

        /* 辞書が存在し、辞書頻度の high, base が正しい場合のみ解析用辞書情報を設定する */
        if((ptarget->handle != NULL) &&
           (NJ_CHECK_USE_DIC_FREQ(ptarget->dic_freq) != 0)) {

            type = NJ_GET_DIC_TYPE_EX(ptarget->type, ptarget->handle);
            switch (type) {
            case NJ_DIC_TYPE_JIRITSU:                       /* 自立語辞書 */
            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
            case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書 */
            case NJ_DIC_TYPE_FUSION:                        /* 統合辞書 */
            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
            case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
            case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
            case NJ_DIC_TYPE_CUSTOM_COMPRESS:               /* 圧縮カスタマイズ辞書 */
            case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:             /* 非圧縮カスタマイズ辞書 */
            case NJ_DIC_TYPE_USER:                          /* ユーザ辞書 */
            case NJ_DIC_TYPE_FORECONV:                      /* 予測変換辞書(iWnnでは未使用)  */
            case NJ_DIC_TYPE_PROGRAM:                       /* 擬似辞書 */
                /* 自立語解析に使用 */
                njd_copy_dicinfo(pstem, ptarget, NJ_MODE_TYPE_HENKAN);
                break;

            case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:       /* 非圧縮カスタマイズ辞書(学習辞書変更) */
            case NJ_DIC_TYPE_LEARN:                         /* 学習辞書 */
                /* 形態素解析時以外の場合のみ、自立語解析に使用 */
                if (iwnn->njc_mode != NJC_MODE_MORPHO) {
                    njd_copy_dicinfo(pstem, ptarget, NJ_MODE_TYPE_HENKAN);
                }
                break;

            case NJ_DIC_TYPE_FZK:                           /* 付属語辞書 */
            case NJ_DIC_TYPE_PROGRAM_FZK:                   /* 擬似辞書-付属語- */
                /* 付属語解析に使用 */
                njd_copy_dicinfo(pfzk, ptarget, NJ_MODE_TYPE_HENKAN);
                break;

            case NJ_DIC_TYPE_TANKANJI:                      /* 単漢字辞書 */
                /* 形態素解析時のみ、自立語解析に使用 */
                if (iwnn->njc_mode == NJC_MODE_MORPHO) {
                    njd_copy_dicinfo(pstem, ptarget, NJ_MODE_TYPE_MORPHO);
                }
                break;

            default:
                /* 解析には使用しない */
                break;
            }
        }
    }


    if (work->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN] == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_CONV_ENV_SET, NJ_ERR_NO_RULEDIC);  /*NCH_FB*/
    }
    work->hinsi.f_v1 = njd_r_get_hinsi(work->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_V1_F);
    work->hinsi.f_v2 = njd_r_get_hinsi(work->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_V2_F);
    work->hinsi.f_v3 = njd_r_get_hinsi(work->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_V3_F);
    work->hinsi.f_giji = njd_r_get_hinsi(work->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_GIJI_F);
    work->hinsi.r_giji = njd_r_get_hinsi(work->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_GIJI_B);
    work->hinsi.r_ngiji = njd_r_get_hinsi(work->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_SUUJI_B);
    work->hinsi.r_bunto = njd_r_get_hinsi(work->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_BUNTOU_B);
    if ((work->hinsi.f_v1 == 0) || (work->hinsi.f_v2 == 0) || (work->hinsi.f_v3 == 0) ||
        (work->hinsi.f_giji == 0) || (work->hinsi.r_giji == 0) ||
        (work->hinsi.r_ngiji == 0) || (work->hinsi.r_bunto == 0)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_CONV_ENV_SET, NJ_ERR_NO_HINSI);
    }

    /*
     * 登録されている前品詞数、後品詞数を取得
     */
    njd_r_get_count(work->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], &work->hinsi.fcnt, &work->hinsi.bcnt);

    return 0;
}


/**
 * 自立語解析
 *
 * @param[in]     iwnn      解析情報クラス
 * @param[in,out] x           連文節変換処理ワーク
 * @param[in]     decideLen   解析済み文の表記文字列長
 *
 * @retval >=0 連文節変換処理ワークへ新規に追加された自立語の個数
 * @retval <0  異常
 */
static NJ_INT16 conv_stemProcess(NJ_CLASS *iwnn, NJC_CONV *x,
                                 NJ_UINT16 decideLen) {

    NJ_UINT16 id;       /* 変換解析バッファの現処理位置 */
    NJ_INT16 ret;       /* 戻り値 */
    NJ_UINT16 slen;     /* 解析済み読み文字NJ_UINT8長 */
    NJ_CHAR  *yomi_top; /* 解析開始読み位置 */
    NJ_UINT16 i, j;     /* カウンタ */
    NJ_CURSOR cur;      /* 辞書検索カーソル */
    NJ_RESULT result;   /* 辞書検索単語 */
    NJ_UINT8 cont_num;  /* 繋がり文節数 */
    NJ_UINT8 cont_num2; /* 繋がり文節数 */
    NJC_WORK_SENTENCE *new_sent;
    NJ_RESULT next;     /* 繋がり単語取得結果 */
    NJ_RESULT phr;
    NJ_UINT16 yomi_len;
    NJ_UINT16 hyouki_len;
    NJ_INT16 ret_count; /* 追加した自立語個数 */
    NJ_UINT8 gcnt;      /* 辞書引き取得単語数 */
    NJ_UINT8 exit_flag;
    NJ_UINT16 tmpYomiLen;       /* 読み長 */
    NJ_UINT16 tmpReprLen;       /* 候補長 */
    NJ_DIC_FREQ dic_freq;       /* 辞書頻度 */
    NJ_UINT8 sent_check;        /* 解析不能文検出用 */



    ret_count = 0;
    id = 0;     /* 変換解析バッファ先頭から解析 */
    while ((id < (NJ_UINT16)(x->sentNum)) || (x->sentNum == 0)) {
        sent_check = 0;

        if (x->sentNum == 0) {
            /* １文節も格納されていない場合 */
            slen = 0;

            /*
             * 自立語長０(擬似候補)の文節を作成する。
             * 付属語のみの文節も有効にする為。
             */
            new_sent = &(x->sentence[x->sentNum]);
            wsent_init(new_sent);
            rlt_init(&result);
            NJ_SET_FPOS_TO_STEM(&result.word, x->hinsi.f_giji);
            NJ_SET_BPOS_TO_STEM(&result.word, x->hinsi.r_giji);
            
            result.word.yomi = x->pyomi;
            /* 候補フィルタリング */
            /* 文情報へ文節情報を追加する */
            ret = wsent_pushPhrase(iwnn, x, new_sent, &result, NULL);
            if (conv_replaceSentence(x) > 0) {
                id++;
                ret_count++;
            }

        } else {
            /*
             * 文節分割されている文節が対象の場合、次文節の自立語長は 0 になるので、
             * 自立語長０の文節を押し込み、付属語作成処理に移る。
             */

           if ((WSENT_GETLASTFLAG(&(x->sentence[id])) & NJC_WS_FLAG_ASSUME_TWO_PHRASES) != 0) {
                new_sent = &(x->sentence[id]);

                rlt_init(&result);
                slen = x->sentence[id].yomiLen;
                result.word.yomi = &(x->pyomi[slen]);

                NJ_SET_FPOS_TO_STEM(&result.word, 0);
                NJ_SET_BPOS_TO_STEM(&result.word, WSENT_GETINFO_LASTRPOS(new_sent));
                NJ_SET_FREQ_TO_STEM(&result.word, 0);

                ret = wsent_pushPhrase(iwnn, x, new_sent, &result, NULL);

                id++;
                ret_count++;
                continue;
            }

            /* 終端ベクトルチェックを行う */
            if (!CONNECT_R_PHRASE_END(iwnn->njc_mode, x->sentence[id].connect, x->hinsi)) {
                /* 前文節が終止形でない場合、自立語の追加は行わない */
                x->sentence[id].invalid = 1; /* 疑似候補も追加しない */
                id++;
                continue;
            }
            slen = x->sentence[id].yomiLen;
        }

        if (slen == x->restYomiLen) {
            if ((x->sentence[id].phrNum + x->sentence[id].vphrNum) < NJC_MAX_WORK_PHRASE) {
                x->sentence[id].vphrNum++;
            }
            id++;
            continue;
        }

        /* 解析不能と判断された解析文はスキップする */
        if (x->sentence[id].invalid) {
            id++; /*NCH_FB*/
            continue; /*NCH_FB*/
        } else {
            /*
             * 解析可能であっても、確定文節候補長＋解析候補長が、最大出力候補長
             * である場合は、解析不能とする
             */
            if (REPR_LEN_EXCEED(iwnn->njc_mode, x, decideLen,
                                x->sentence[id].reprLen, x->sentence[id].yomiLen)) {
                x->sentence[id].invalid = 1;
                id++;
                /* 出力長上限到達フラグを立てる */
                CNV_SET_OVERRUN(x, 1);
                continue;
            }
        }

        /* 解析開始 読み位置を取得 */
        yomi_top = &(x->pyomi[slen]);

        /*
         * 辞書検索カーソル 検索条件(読み以外)設定
         */
        njd_init_search_condition(&(cur.cond));
        cur.cond.operation = (iwnn->njc_mode != NJC_MODE_MORPHO) ? NJ_CUR_OP_COMP : NJ_CUR_OP_REV;
        cur.cond.mode = NJ_CUR_MODE_FREQ;
        cur.cond.ds = &x->jds;
        njd_r_get_connect(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], x->hinsi.r_bunto, 0, &cur.cond.hinsi.fore);
        cur.cond.hinsi.foreSize = x->hinsi.fcnt;

        /* その直後で自立語を検索する */
        i = slen;
        i += NJ_CHAR_LEN(&(x->pyomi[i]));
        for ( ; i <= x->restYomiLen; i += NJ_CHAR_LEN(&(x->pyomi[i]))) {

            /* 正引き完全一致拡張検索の場合 */
            if (cur.cond.operation == NJ_CUR_OP_COMP_EXT) {
                /* 正引き完全一致検索に切替 */
                cur.cond.operation = NJ_CUR_OP_COMP;
            }

            /* 辞書検索カーソル 検索条件(読み)設定 */
            cur.cond.yomi = yomi_top;
            cur.cond.ylen = i - slen; /* 検索読み文字列長設定 */

            /*
             * 単語検索 実行
             */
            ret = njd_search_word(iwnn, &cur, 1, &exit_flag);

            if (ret == 0) {
                /* 候補が無い */
                if (exit_flag == 1) {
                    /* 前方一致候補も存在しないので      */
                    /* これ以上検索しても意味なし        */
                    break;
                }
                continue;
            } else if (ret < 0) {
                if (NJ_GET_ERR_CODE(ret) == NJ_ERR_YOMI_TOO_LONG) {
                    /* 辞書候補なし */
                    break;
                }
                /* 単語検索処理で異常発生 */
                return ret;
            }

            /*
             * 単語取得 全ての単語を取得
             */
            gcnt = 0;
            while ((ret = njd_get_word(iwnn, &cur, &result, 1)) > 0) {

                if (++gcnt > NJC_MAX_GET_RESULTS) {
                    /* 同読みに対する辞書引き回数制限 */
                    break;
                }

                /* 基本辞書以外の辞書引き結果では、検索対象 */
                /* 読み文字列は格納されていない。               */
                result.word.yomi = yomi_top;

                /* 繋がり文節数を取得 */
                cont_num = (NJ_UINT8)(result.word.stem.loc.current_info >> 4) & 0x0F;

                /*
                 * 読み長 slen の評価値最高の文に文節を加える
                 */
                new_sent = &(x->sentence[x->sentNum]);
                if (slen == 0) {
                    wsent_init(new_sent);
                } else {
                    *new_sent = x->sentence[id];
                }

                /* 繋がり文節（文節切り学習情報）を取得 */
                phr = result;

                if (iwnn->njc_mode == NJC_MODE_MORPHO) {
                    tmpReprLen = RLT_GETYOMILEN(&phr);
                    tmpYomiLen = RLT_GETREPRLEN(&phr);
                } else {
                    tmpReprLen = RLT_GETREPRLEN(&phr);
                    tmpYomiLen = RLT_GETYOMILEN(&phr);
                }

                if (REPR_LEN_EXCEED(iwnn->njc_mode, x, decideLen,
                                    (new_sent->reprLen + tmpReprLen),
                                    (new_sent->yomiLen + tmpYomiLen))) {
                    /* 候補文字列表記長超過した場合は無視 */
                    /* 出力長上限到達フラグを立てる */
                    CNV_SET_OVERRUN(x, 1);
                    continue;
                }
                cont_num--;

                sent_check = 1;

                for (j = 0; j < NJ_MAX_DIC; j++) {
                    if (x->cond.ds->dic[j].handle == result.word.stem.loc.handle) {
                        dic_freq = x->cond.ds->dic[j].dic_freq[NJ_MODE_TYPE_HENKAN];
                        break;
                    }
                }

                while (cont_num--) {
                    ret = njd_get_relational_word(iwnn, &result, &next, &dic_freq);
                    if (ret < 0) {
                        return ret; /*NCH*/
                    }
                    /* 繋がり文節を作成 */
                    hyouki_len = NJ_GET_KLEN_FROM_STEM(&next.word);
                    yomi_len = NJ_GET_YLEN_FROM_STEM_EXT(&next.word);
                    if (iwnn->njc_mode == NJC_MODE_MORPHO) {
                        tmpReprLen = RLT_GETYOMILEN(&phr); /*NCH_FB*/
                        tmpYomiLen = RLT_GETREPRLEN(&phr); /*NCH_FB*/
                    } else {
                        tmpReprLen = RLT_GETREPRLEN(&phr);
                        tmpYomiLen = RLT_GETYOMILEN(&phr);
                    }
                    if (REPR_LEN_EXCEED(iwnn->njc_mode, x, decideLen,
                                        (new_sent->reprLen + tmpReprLen + hyouki_len),
                                        (new_sent->yomiLen + tmpYomiLen + yomi_len))) {
                        /*
                         * 表記文字列最大長を超えないところまで結合する
                         */
                        /* 結合文節数の更新 */
                        cont_num2 = (phr.word.stem.loc.current_info >> 4) & 0x0F;
                        cont_num2 -= (cont_num + 1);
                        phr.word.stem.loc.current_info = (cont_num2 << 4) & 0xF0;
                        break;
                    }
                    NJ_SET_YLEN_TO_STEM(&phr.word, (NJ_GET_YLEN_FROM_STEM_EXT(&phr.word) + NJ_GET_YLEN_FROM_STEM_EXT(&next.word)));
                    NJ_SET_KLEN_TO_STEM(&phr.word, (NJ_GET_KLEN_FROM_STEM(&phr.word) + hyouki_len));
                    NJ_SET_BPOS_TO_STEM(&phr.word, NJ_GET_BPOS_FROM_STEM(&next.word));
                    if (NJ_GET_FREQ_FROM_STEM(&phr.word) < NJ_GET_FREQ_FROM_STEM(&next.word)) {
                        NJ_SET_FREQ_TO_STEM(&phr.word, NJ_GET_FREQ_FROM_STEM(&next.word));
                    }
                    result = next;
                }

                /* 候補フィルタリング */
                /* 解析文情報に文節情報を追加 */
                if (wsent_pushPhrase(iwnn, x, new_sent, &phr, NULL) == 0) {
                    /* 追加に失敗した場合は、次解析文の解析にうつる */
                    break;
                }

                /* 解析文配列への組み込み */
                if (conv_replaceSentence(x) > 0) {
                    ret_count++;
                    id++;
                    if (slen == 0) {
                        if (id > (NJ_UINT16)(x->sentNum)) {
                            id--; /*NCH_FB*/
                        }
                    } else {
                        if (id >= (NJ_UINT16)(x->sentNum)) {
                            break;
                        }
                    }
                }
            }
            if ((ret == 0) &&
                (gcnt == 0)){
                /* 候補が無い */
                if (exit_flag == 1) {
                    /* 前方一致候補も存在しないので      */
                    /* これ以上検索しても意味なし        */
                    break;
                }
                continue;
            }


            if (ret < 0) {
                /* 単語取得処理で異常発生 */
                return ret;
            }

            if ((slen != 0) && (id >= (NJ_UINT16)(x->sentNum))) {
                /*
                 * 自立語解析処理自体を完了する。
                 */
                break;
            }
        }

        if (slen != 0) {
            /*
             * 同読み長の文候補は、評価値の最も高い候補のみに対し
             * 自立語解析を行う.その他の候補に関しては解析は行わない.
             */
            if (sent_check == 0) {
                id++;

            } else {
                while ((id < (NJ_UINT16)(x->sentNum)) && (slen == x->sentence[id].yomiLen) &&
                       ((WSENT_GETLASTFLAG(&(x->sentence[id])) & NJC_WS_FLAG_ASSUME_TWO_PHRASES) == 0) ) {
                    id++;
                }
            }
        } else {
            break;
        }
    }


    /*
     * 最大評価文の文節数が最大に達している場合のみ、それ以下の文節数の文情報を削除
     */
    conv_removeSent(x, (NJ_INT16)(x->sentence[0].phrNum + x->sentence[0].vphrNum));


    return ret_count;
}


/**
 * 文情報に文節情報を追加する
 *
 * @param[in] iwnn    解析情報クラス
 * @param[in] x         連文節変換処理ワーク
 * @param[in] ws        作業用文情報保持領域
 * @param[in] phr       文節情報
 * @param[in] info      文節付加情報
 *
 * @return 追加文節数（1: 追加、0: 追加不可）
 */
static NJ_INT16 wsent_pushPhrase(NJ_CLASS *iwnn, NJC_CONV *x, NJC_WORK_SENTENCE *ws,
                                 NJ_RESULT *phr, NJC_PHRASE_INFO* info) {

    NJ_UINT16 len;
    NJ_UINT16 posR;             /* 右品詞番号           */


    if (ws->phrNum >= NJC_MAX_WORK_PHRASE) {
        /* 登録文節数超過の為、追加できない */
        return 0;
    }

    if (iwnn->njc_mode == NJC_MODE_MORPHO) {
        len = RLT_GETYOMILEN(phr);
    } else {
        len = RLT_GETREPRLEN(phr);
    }

    if ((ws->reprLen + len) > x->maxRltLen) {
        /*
         * 最大表示サイズを超えた
         */
        return 0; /*NCH_FB*/
    }

    if (info == NULL) {
        /* デフォルト値を設定する */
        info_init(&(ws->info[ws->phrNum]));
    } else if (&(ws->info[ws->phrNum]) != info) {
        /* 同じ実体を指す場合以外はコピーする */
        /*  cf) 付属語解析時、popしてpushする時実体は同じ */
        ws->info[ws->phrNum] = *info; /*NCH_FB*/
    }

    if (&(ws->phrases[ws->phrNum]) != phr) {
        /* 同じ実体を指す場合以外はコピーする */
        /*  cf) 付属語解析時、popしてpushする時実体は同じ */
        ws->phrases[ws->phrNum] = *phr;
    }
    ws->phrNum++;

    ws->reprLen += len;

    ws->yomiLen += RLT_GETYOMILEN(phr);

    ws->score += RLT_GETSCORE_CONV(phr);

    posR = RLT_GETLASTPOS(phr);

    njd_r_get_connect(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], posR, 0, &(ws->connect));
    njd_r_get_connect_ext(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], posR, 0, &(ws->connect_ext));

    return 1;
}


/**
 * 文末尾の文節を取り出す(ポップする)
 *
 * @param[in]     iwnn     解析情報クラス
 * @param[in]     x        評価対象文バッファ
 * @param[in,out] ws       作業用文情報保持領域
 * @param[out]    info     文節付加情報
 *
 * @return 文末尾の文節情報
 *
 * @attention
 * 返値は，NJC_WORK_SENTENCE内部に元々あったポップ対象の
 * NJ_RESULTインスタンス領域を指す。
 * これは、領域のコピー回数を減らし、効率化するためである。<br>
 * この仕様のため、ポップした文節情報を使用中に、取り出し元の 
 * NJC_WORK_SENTENCEインスタンスを操作しないように注意が必要である。
 * 操作が必要な場合は、ポップ後、取り出し側でコピー処理をすること。
 */
static NJ_RESULT *wsent_popPhrase(NJ_CLASS *iwnn, NJC_CONV *x,
                                  NJC_WORK_SENTENCE *ws, NJC_PHRASE_INFO** info) {

    NJ_RESULT *phr;
    NJ_UINT16 posR;             /* 右品詞番号           */


    if (ws->phrNum == 0) {
        return NULL; /*NCH_FB*/
    }

    ws->phrNum--;

    /* フラグ取得 */
    if (info != NULL) {
        *info = &(ws->info[ws->phrNum]);
    }

    /* 文節情報 */
    phr = &(ws->phrases[ws->phrNum]);

    /* 表記長の変更 */
    if (iwnn->njc_mode == NJC_MODE_MORPHO) {
        ws->reprLen -= RLT_GETYOMILEN(phr);
    } else {
        ws->reprLen -= RLT_GETREPRLEN(phr);
    }

    /* 読み長の変更 */
    ws->yomiLen -= RLT_GETYOMILEN(phr);

    /* 評価値の変更 */
    ws->score -= RLT_GETSCORE_CONV(phr);

    /* 接続情報の再設定 */
    posR = (ws->phrNum == 0) ?
        0 : RLT_GETLASTPOS(&(ws->phrases[ws->phrNum-1]));

    njd_r_get_connect(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], posR, 0, &(ws->connect));
    njd_r_get_connect_ext(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], posR, 0, &(ws->connect_ext));

    return phr;
}


/**
 * 作業領域(sentence[sentNum])にある文を文配列の中に挿入する
 *
 * 等価文があるときは、評価値の高い方を残す。
 *
 * @param[in] x          連文節変換処理ワーク
 *
 * @retval 1 新規追加
 * @retval 0 追加なし
 */
static NJ_INT16 conv_replaceSentence(NJC_CONV *x) {

    NJ_INT16 sameID;
    NJC_WORK_SENTENCE *new_sent;
    NJC_WORK_SENTENCE *sent;
    NJ_INT16 i;


    if (x->sentNum > NJC_MAX_WORK_CANDIDATE) {
        return 0;
    }

    new_sent = &(x->sentence[x->sentNum]);
    if (x->sentNum > 0) {
        sent = &(x->sentence[0]);
        if ((new_sent->phrNum == NJC_MAX_WORK_PHRASE)  &&
            (new_sent->yomiLen < (sent->yomiLen / NJC_CUTOFF_LENGTH_THRESHOLD))) {
            return 0;
        }
    }

    sameID = -1;
    sent = x->sentence;
    for (i = 0; i < x->sentNum; i++, sent++) {
        if (sent->yomiLen == new_sent->yomiLen) {
            if (WSENT_GETLASTPOS(sent) == WSENT_GETLASTPOS(new_sent) &&
                sent->info[sent->phrNum-1].flag   == new_sent->info[new_sent->phrNum-1].flag &&
                sent->info[sent->phrNum-1].handle == new_sent->info[new_sent->phrNum-1].handle &&
                sent->info[sent->phrNum-1].id     == new_sent->info[new_sent->phrNum-1].id ) {
                sameID = i;
                break;
            }
        } else if (sent->yomiLen < new_sent->yomiLen) {
            /* ソートされているので，これ以上調べる必要なし */
            break;
        }
    }


    if (sameID < 0) {
        /* 等価文が無ければ，新規追加 */
        x->sentNum++;
        conv_sortArea(x, (NJ_INT16)(x->sentNum - 1));

        /* 保持上限を超えた場合 */
        if (x->sentNum >= NJC_MAX_WORK_CANDIDATE) {
            x->sentNum--;
        }

        /* 新規追加されたときは 1 を返す */
        return 1;

    } else if (x->sentence[sameID].score < new_sent->score) {
        /*
         * 等価文があれば，評価値の高い方を残す
         */
        /* 追加した文節を示すフラグを落とす（通常は OFF、conv_fzkProcess でのみ ON されている場合あり）*/
        new_sent->invalid &= 0x7f;

        x->sentence[sameID] = *new_sent;
        conv_sortArea(x, sameID);

    } else if (x->sentence[sameID].score == new_sent->score) {
        /*
         * 等価文であり、評価値が同じであれば、
         * 最終文節の読み長の長い方を残す
         */

        if (RLT_GETYOMILEN(&x->sentence[sameID].phrases[x->sentence[sameID].phrNum - 1]) < 
            RLT_GETYOMILEN(&new_sent->phrases[new_sent->phrNum - 1])) {

            /* 追加した文節を示すフラグを落とす（通常は OFF、conv_fzkProcess でのみ ON されている場合あり）*/
            new_sent->invalid &= 0x7f;
            x->sentence[sameID] = *new_sent;
            conv_sortArea(x, sameID);
        }
    }

    return 0;
}


/**
 * 文列をソートする
 *
 * 指定ID の文の位置のみを変更する。
 * 指定ID 以外の部分は既にソートされていることが前提。<br>
 * ソート順は、読み長降順、読み長が同じ場合は評価値降順、評価値も同じ場合はフラグ値昇順。<br>
 * 
 *
 * @param[in] x      連文節変換処理ワーク
 * @param[in] sentID ソート対象文ID
 *
 * @return なし
 */
static void conv_sortArea(NJC_CONV *x, NJ_INT16 sentID) {

    NJ_INT16 index;
    NJC_WORK_SENTENCE tmp;
    NJC_WORK_SENTENCE *p;
    NJ_UINT16 len1, len2;
    NJ_UINT32 flag1, flag2;


    tmp = x->sentence[sentID];

    if (sentID > 0) {
        p = &(x->sentence[sentID - 1]);

        len1 = tmp.yomiLen;
        len2 = p->yomiLen;
        flag1 = WSENT_GETLASTFLAG(&tmp);
        flag2 = WSENT_GETLASTFLAG(p);

        if ((tmp.invalid & 0x7f) == 1) {
            len1 = 0; /*NCH_FB*/
            flag1 = 0; /*NCH_FB*/
        } else if ((p->invalid & 0x7f) == 1) {
            len2 = 0;
            flag2 = 0;
        }
    } else {
        len1 = 0;
        len2 = 1;
        flag1 = 0;
        flag2 = 0;
        p = &tmp;
    }

    if ((sentID == 0) ||
         (len1 <  len2) ||
        ((len1 == len2) && ( ((flag1 <= flag2) && (tmp.score < p->score)) ||
                              (flag1 >  flag2) )) ) {
        /* 現在位置よりも下に移動させる */
        index = sentID + 1;
        p = &(x->sentence[index]);
        while (index < x->sentNum) {
            len1 = tmp.yomiLen;
            len2 = p->yomiLen;
            flag1 = WSENT_GETLASTFLAG(&tmp);
            flag2 = WSENT_GETLASTFLAG(p);

            if ((tmp.invalid & 0x7f) == 1) {
                len1 = 0;         /* NCH_FB */
                flag1 = 0;        /* NCH_FB */
            } else if ((p->invalid & 0x7f) == 1) {
                len2 = 0;
                flag2 = 0;
            }

            if ( (len1 >  len2) ||
                ((len1 == len2) && ( ((flag1 >= flag2) && (tmp.score > p->score)) ||
                                      (flag1 <  flag2) )) ){
                /* 落ち付き先発見 */
                *(p - 1) = tmp;
                break;
            }
            *(p - 1) = *p;
            index++; p++;
        }
        if (index >= x->sentNum) {
            /* 最後尾に落ち付く場合 */
            x->sentence[x->sentNum - 1] = tmp;
        }
    } else {
        /* 現在位置よりも上に移動させる */
        index = sentID - 1;
        p = &(x->sentence[index]);
        while (index >= 0) {
            len1 = tmp.yomiLen;
            len2 = p->yomiLen;
            flag1 = WSENT_GETLASTFLAG(&tmp);
            flag2 = WSENT_GETLASTFLAG(p);

            if ((tmp.invalid & 0x7f) == 1) {
                len1 = 0; /*NCH_FB*/
                flag1 = 0; /*NCH_FB*/
            } else if ((p->invalid & 0x7f) == 1) {
                len2 = 0;
                flag2 = 0;
            }

            if ((len1 < len2) ||
                ((len1 == len2) && ( ((flag1 <= flag2) && (tmp.score <= p->score)) ||
                                      (flag1 >  flag2) )) ) {
                /* 落ち付き先発見 */
                *(p + 1) = tmp;
                break;
            }
            *(p + 1) = *p;
            index--; p--;
        }
        if (index < 0) {
            /* 先頭に落ち付く場合 */
            x->sentence[0] = tmp;
        }
    }
}


/**
 * N個未満の文節を持つ解析文を削除
 *
 * @param[in] x   連文節変換処理ワーク
 * @param[in] N   削除対象 文節数閾値
 *
 * @return なし
 */
static void conv_removeSent(NJC_CONV *x, NJ_INT16 N) {

    NJ_INT16 i, sentNum;
    NJC_WORK_SENTENCE *p0;
    NJC_WORK_SENTENCE *p1;


    p0 = x->sentence;
    p1 = x->sentence;
    for (i = 0, sentNum = 0; i < x->sentNum; i++, p0++) {
        if ((p0->phrNum + p0->vphrNum) >= N) {
            if (p0 != p1) {
                /* 移動先、移動元のアドレスが異なる場合、移動 */
                *p1 = *p0;
            }
            p1++;
            sentNum++;
        }
    }
    x->sentNum = sentNum;
}


/**
 * 付属語解析
 *
 * @param[in]     iwnn    解析情報クラス
 * @param[in,out] x         連文節変換処理ワーク
 * @param[in]     decideLen 解析済み文の表記文字列長
 *
 * @retval >=0 連文節変換処理ワークへ新規に追加された付属語の個数
 * @retval <0 異常
 *
 * @attention 本関数内で、sentences[*]->invalid の最上位ビットを利用している。
 */
static NJ_INT16 conv_fzkProcess(NJ_CLASS *iwnn, NJC_CONV *x,
                                NJ_UINT16 decideLen) {

    NJ_CURSOR cur;      /* 辞書検索カーソル */
    NJ_INT16 N;
    NJ_INT16 id;
    NJ_UINT16 slen;
    NJ_INT16 sslen;
    NJ_CHAR  *yomi_top;
    NJ_INT16 ret;       /* 戻り値 */
    NJ_INT16 k;
    NJ_UINT16 i, ylen;
    NJC_WORK_SENTENCE *sent;
    NJ_CURSOR ctmp;
    NJ_RESULT result;
    NJC_WORK_SENTENCE *new_sent;
    NJ_RESULT *phr;
    NJ_INT16 ret_count;     /* 追加した付属語個数 */
    NJ_UINT8 exit_flag;
    NJ_UINT16 tmpYomiLen;
    NJ_UINT16 tmpReprLen;
    NJ_INT16 sentNum;
    NJC_WORK_SENTENCE *p0, *p1;
    NJ_UINT8 yomiLenLimit, yomiLenWork;
    NJ_INT16 sentCnt;
    NJC_PHRASE_INFO* info;
    NJ_UINT32 flag;


    p0 = x->sentence;
    for (sentCnt = 0 ; sentCnt < x->sentNum ; sentCnt++, p0++) {
        p0->invalid &= 0x7f;
    }

    /* 読み長が短かい順に処理する */
    ret_count = 0;
    N = 0;

    /*
     * 辞書検索カーソル 検索条件(読み以外)設定
     */
    njd_init_search_condition(&(cur.cond));
    cur.cond.operation = NJ_CUR_OP_COMP;    /* 形態素解析でも正引きする */
    cur.cond.mode = NJ_CUR_MODE_FREQ;
    cur.cond.ds = &x->fds;
    cur.cond.hinsi.foreSize = x->hinsi.fcnt;
    cur.cond.hinsi.foreFlag = 1;

    while ((sslen = conv_getShortestLen(x, N, &id)) >= 0) {

        slen = (NJ_UINT16)sslen;
        yomi_top = &(x->pyomi[slen]);

        /* その直後で付属語を検索する */
        i = slen;
        i += NJ_CHAR_LEN(&(x->pyomi[i]));
        for ( ; i <= x->restYomiLen; i += NJ_CHAR_LEN(&(x->pyomi[i]))) {

            /* 辞書検索カーソル 検索条件(読み)設定 */
            cur.cond.yomi = yomi_top;
            ylen = i - slen;
            cur.cond.ylen = ylen;

            /*
             * 単語検索 実行
             */
            ret = njd_search_word(iwnn, &cur, 1, &exit_flag);
            if (ret == 0) {
                /* 候補が無い */
                if (exit_flag == 1) {
                    /* 前方一致候補も存在しないので      */
                    /* これ以上検索しても意味なし        */
                    break;
                }
                continue;
            } else if (ret < 0) {
                /* 単語検索処理で異常発生 */
                return ret; /*NCH_FB*/
            }

            /* N番目の最短読み長の文についてループ */
            for (k = id; k < x->sentNum; k++) {

                sent = &(x->sentence[k]);
                if (sent->yomiLen != slen) {
                    /* N番目の最短読み長でない */
                    break;
                }

                /* 解析不能と判断された解析文はスキップする */
                if (x->sentence[k].invalid & 0x7f) {
                    continue;
                    /*
                     * 解析可能であっても、確定文節候補長＋解析候補長が、最大出力候補長
                     * である場合は、解析不能とする
                     */
                } else {
                    if (REPR_LEN_EXCEED(iwnn->njc_mode, x, decideLen,
                                        x->sentence[k].reprLen, x->sentence[k].yomiLen)) {
                        x->sentence[k].invalid = (x->sentence[k].invalid & 0x7f) | 0x01; /*NCH_FB*/
                        /* 出力長上限到達フラグを立てる */
                        CNV_SET_OVERRUN(x, 1); /*NCH_FB*/
                        continue; /*NCH_FB*/
                    }
                }

                if ((WSENT_GETLASTFLAG(sent) & NJC_WS_FLAG_ASSUME_TWO_PHRASES) != 0) {
                    continue;
                }

                /* 全辞書検索一致語についてループ */
                ctmp = cur;
                /* 検索条件に品詞を追加してnjd_get_word()する */
                ctmp.cond.hinsi.fore = sent->connect;
                ctmp.cond.hinsi.foreSize = x->hinsi.fcnt;
                ctmp.cond.hinsi.foreFlag = 0;
                while ((ret = njd_get_word(iwnn, &ctmp, &result, 1)) > 0) {

                    if (!CONNECT_R(sent->connect, NJ_GET_FPOS_FROM_STEM(&result.word))) {
                        /* 繋がらければ無視*/
                        continue;
                    }

                    if (sent->phrNum > 1 && RLT_GETYOMILEN(&(sent->phrases[sent->phrNum-1])) == 0 &&
                        (WSENT_GETFLAG(sent,sent->phrNum-2) & NJC_WS_FLAG_ASSUME_TWO_PHRASES) != 0 ) {

                        if ((WSENT_GETFLAG(sent,sent->phrNum-2) & NJC_WS_FLAG_DIVIDE_FOR_WORD) != 0) {
                            if (!(WSENT_GETINFO_HANDLE(sent,sent->phrNum-2) == result.word.stem.loc.handle &&
                                  WSENT_GETINFO_ID(sent,sent->phrNum-2) == WORD_GET_ID(&result.word))) {
                            /* 最後尾の１つ手前の文節に品詞指定があれば、それ品詞以外の単語は繋がらないようにする */
                                continue; /*NCH_DEF*/
                            }
                        } else {
                            if (WSENT_GETINFO_LPOS(sent,sent->phrNum-2) != NJ_GET_FPOS_FROM_STEM(&result.word)) {
                            /* 最後尾の１つ手前の文節に品詞指定があれば、それ品詞以外の単語は繋がらないようにする */
                                continue;
                            }
                        }
                    }


                    /* 追加文を作成 */
                    new_sent = &(x->sentence[x->sentNum]);
                    *new_sent = *sent;

                    if ((NJ_GET_YLEN_FROM_STEM_EXT(&(new_sent->phrases[new_sent->phrNum-1].word)) != 0) &&
                        ((NJ_GET_FREQ_FROM_STEM(&result.word) < 0) ||
                         (new_sent->connect_ext != NULL && ((GET_CONNECT_EXT_R(new_sent->connect_ext, NJ_GET_FPOS_FROM_STEM(&result.word)) & 0x01) != 0))) ) {

                        /* 文節分割後の連結に必要な情報をフラグに設定する */
                        flag = NJC_WS_FLAG_ASSUME_TWO_PHRASES;

                        phr = &(new_sent->phrases[new_sent->phrNum-1]);
                        if (!RLT_HASFZK(phr)) {
                            WSENT_SETINFO_LASTRPOS(new_sent, NJ_GET_BPOS_FROM_STEM(&phr->word));
                        } else {
                            WSENT_SETINFO_LASTRPOS(new_sent, NJ_GET_BPOS_FROM_FZK(&phr->word));
                        }

                        if ((NJ_GET_FREQ_FROM_STEM(&result.word) < 0)) {
                            /* 単語での分割なので、接続側の単語情報を保持する */
                            flag |= NJC_WS_FLAG_DIVIDE_FOR_WORD;
                            WSENT_SETINFO_LASTWORD(new_sent, result.word.stem.loc.handle, WORD_GET_ID(&result.word));
                        } else {
                            /* 品詞接続での分割なので、接続側の前品詞情報を保持する */
                            WSENT_SETINFO_LASTLPOS(new_sent, NJ_GET_FPOS_FROM_STEM(&result.word));
                        }

                        WSENT_SETLASTFLAG(new_sent, flag);
                    } else {

                        if ((phr = wsent_popPhrase(iwnn, x, new_sent, &info)) == NULL) {
                            continue;
                        }

                        /* 文節情報を変更 */
                        if (!RLT_HASFZK(phr)) {
                            NJ_SET_FPOS_TO_FZK(&phr->word, NJ_GET_FPOS_FROM_STEM(&result.word));
                        }
                        NJ_SET_YLEN_TO_FZK(&phr->word, (NJ_GET_YLEN_FROM_FZK(&phr->word) + ylen));
                        NJ_SET_BPOS_TO_FZK(&phr->word, NJ_GET_BPOS_FROM_STEM(&result.word));
                        NJ_SET_KLEN_TO_FZK(&phr->word, (NJ_GET_YLEN_FROM_FZK(&phr->word)));
                        if (NJ_GET_FREQ_FROM_FZK(&phr->word) < NJ_GET_FREQ_FROM_STEM(&result.word)) {
                            NJ_SET_FREQ_TO_FZK(&phr->word, (NJ_GET_FREQ_FROM_STEM(&result.word)));
                        }

                        if (iwnn->njc_mode == NJC_MODE_MORPHO) {
                            tmpReprLen = RLT_GETYOMILEN(phr);
                            tmpYomiLen = RLT_GETREPRLEN(phr);
                        } else {
                            tmpReprLen = RLT_GETREPRLEN(phr);
                            tmpYomiLen = RLT_GETYOMILEN(phr);
                        }

                        if (REPR_LEN_EXCEED(iwnn->njc_mode, x, decideLen, 
                                            (new_sent->reprLen + tmpReprLen), 
                                            (new_sent->yomiLen + tmpYomiLen))) {
                            /* 最大出力表記文字列長を超えた */
                            /* 出力長上限到達フラグを立てる */
                            CNV_SET_OVERRUN(x, 1);
                            continue;
                        }

                        /* 候補フィルタリング                           */
                        /* 付属語解析のため候補フィルタリングは行わない */
                        /* 文節情報が解析文に追加できない場合はスキップ */
                        if (wsent_pushPhrase(iwnn, x, new_sent, phr, info) == 0) {
                            continue;
                        }
                    }

                    /*
                     * 追加した文節情報であることを示すフラグを立てる
                     * 新規追加でなければ conv_replaceSentence( ) の中でフラグが落とされる
                     */
                    new_sent->invalid |= 0x80;


                    /* 文配列への組み込み */
                    if (conv_replaceSentence(x) > 0) {
                        /* 新規追加の場合
                         * 追加されたときは，長さが元よりも大きくなる．
                         * したがって，ソート時に必ず上に移動する．
                         * そのぶん，k, id をずらす
                         */
                        id++;
                        ret_count++;
                        if (++k >= x->sentNum) {
                            /*
                             * 解析バッファをすべて処理した場合は
                             * 自立語解析処理を完了する。
                             */
                            break;
                        }
                        sent = &(x->sentence[k]);
                    }
                }
                if (ret < 0) {
                    /* 単語取得処理で異常発生 */
                    return ret; /*NCH_FB*/
                }
            }
        }

        N++; /* 次に長い読みを持つ文候補に対して、付属語解析を行う */
    }

    p0 = x->sentence;
    p1 = x->sentence;

    sent = &(x->sentence[0]);
    yomiLenLimit = NJ_GET_YLEN_FROM_STEM_EXT(&(sent->phrases[sent->phrNum - 1].word)) +
        NJ_GET_YLEN_FROM_FZK(&(sent->phrases[sent->phrNum - 1].word));
    yomiLenLimit /= NJC_CUTOFF_LENGTH_THRESHOLD;

    for (sentCnt = 0, sentNum = 0; sentCnt < x->sentNum; sentCnt++, p0++) {
        yomiLenWork = NJ_GET_YLEN_FROM_STEM_EXT(&(p0->phrases[p0->phrNum - 1].word)) +
            NJ_GET_YLEN_FROM_FZK(&(p0->phrases[p0->phrNum - 1].word));

        if ((sent->phrNum < p0->phrNum) ||
            ((NJ_GET_TYPE_FROM_STEM(&(p0->phrases[p0->phrNum - 1].word)) != NJ_TYPE_UNDEFINE) &&
             (NJ_GET_YLEN_FROM_FZK(&(p0->phrases[p0->phrNum - 1].word)) == 0)) ||
            (yomiLenWork == 0) || 
            (yomiLenWork >= yomiLenLimit)) {
            if (p0 != p1) {
                /* 移動先、移動元のアドレスが異なる場合、移動 */
                *p1 = *p0;
            }
            p1++;
            sentNum++;
        } else {
            /*
             * この関数内で追加された文節が削除対象となる場合、ret_count を減算する
             */
            if (p0->invalid & 0x80) {
                ret_count--;
            }
        }
    }
    x->sentNum = sentNum;

    /*
     * ret_count 引き落としフラグを落とす（他の処理に悪影響を与えないよう）
     */
    p0 = x->sentence;
    for (sentCnt = 0 ; sentCnt < x->sentNum ; sentCnt++, p0++ ) {
        p0->invalid &= 0x7f;
    }

    /*
     * 初回の自立語解析で追加された長さ０の擬似候補が
     * あれば、削除する
     */
    if (x->sentence[x->sentNum - 1].yomiLen == 0) {
        x->sentNum--;
    }


    return ret_count;
}


/**
 * N番目に短かい文を取得する
 *
 * @param[in,out] x     連文節変換処理ワーク
 * @param[in]     N     N番目に短かい文節の読み長を求める
 * @param[out]    topID N番目に短かい文の先頭ID
 *
 * @return N番目に短かい文の読み長
 */
static NJ_INT16 conv_getShortestLen(NJC_CONV *x, NJ_INT16 N, NJ_INT16 *topID) {

    NJ_INT16 len;


    if (x->sentNum <= 0) {
        /* 文節がなければ 0 を返す */
        *topID = 0;  /*NCH_FB*/
        return 0; /*NCH_FB*/
    }

    *topID = x->sentNum - 1;
    len = x->sentence[*topID].yomiLen;

    while (N >= 0) {
        while ((*topID >= 0) && (x->sentence[*topID].yomiLen == (NJ_UINT16)len)) {
            (*topID)--;
        }

        if (*topID < 0) {
            /* 先頭まで来たとき
             *
             * N == 0 の場合は，*topID(==0) が先頭．
             * それ以外のときは，0 番目を超えた場合なので，
             * 負数を返す．
             */
            *topID = 0;
            return (N == 0)? len : -1;
        }

        len = x->sentence[*topID].yomiLen;
        N--;
    }

    /* この時点で，*topID は N番目に短かい文の(先頭ID-1)を指す */
    (*topID)++;
    len = x->sentence[*topID].yomiLen;

    return len;
}


/**
 * 自立語に擬似候補を設定する
 *
 * @param[in]     iwnn    解析情報クラス
 * @param[in,out] x         連文節変換処理ワーク
 * @param[in]     decideLen 解析済み文の表記文字列長
 * @param[in]     first     0: 候補長１文字の疑似候補文節を作成<br>
 *                          1: 上記を作成しない
 *
 * @return 追加した擬似候補文節数
 */
static NJ_INT16 conv_gijiProcess(NJ_CLASS *iwnn, NJC_CONV *x, NJ_UINT16 decideLen,
                                 NJ_UINT8 first) {

    NJ_INT16 id;
    NJ_UINT16 slen;
    NJ_CHAR  *yomi_top;
    NJ_UINT16 ylen;
    NJC_WORK_SENTENCE *new_sent;
    NJ_RESULT phr, *pp;
    NJ_UINT16 ret;
    NJC_WORK_SENTENCE *p0;
    NJC_WORK_SENTENCE *p1;
    NJ_INT16 sentNum;
    NJ_UINT16 tmpReprLen;


    /*
     * x->sentence[x->sentNum]は、擬似候補を追加する際に
     * 利用する。その為、該当領域をクリアしておく。
     */
    if (x->sentNum <= NJC_MAX_WORK_CANDIDATE) {
        wsent_init(&(x->sentence[x->sentNum]));
    }

    ret = 0;
    for (id = 0; id <= x->sentNum; id++) {
        if (id == x->sentNum) {
            slen = 0;
        } else {
            slen = x->sentence[id].yomiLen;
        }

        /* 無効な解析文は処理しない */
        if (x->sentence[id].invalid == 1) {
            continue;
        }

        /* 候補長１文字の疑似候補文節を作成しない */
        if ((slen == 0) && first) {
            continue;
        }
        /* 既に全ての読みを解析しているsentenceの場合 */
        if (slen == x->restYomiLen) {
            if ((x->sentence[id].phrNum + x->sentence[id].vphrNum) < NJC_MAX_WORK_PHRASE) {
                x->sentence[id].vphrNum++; /*NCH_FB*/
            }
            continue;
        }
        yomi_top = &(x->pyomi[slen]);

        /* 2byte文字の場合は，途中で区切らないようにする */
        ylen = NJ_CHAR_LEN(yomi_top);

        /* 擬似文節の追加 */
        if (slen == 0) {
            new_sent = &(x->sentence[x->sentNum]);
            wsent_init(new_sent);
            rlt_setHiraGiji(&phr, ylen, x->hinsi.f_giji, x->hinsi.r_giji);
            phr.word.yomi = yomi_top;
            if (iwnn->njc_mode == NJC_MODE_MORPHO) {
                tmpReprLen = RLT_GETYOMILEN(&phr);
            } else {
                tmpReprLen = RLT_GETREPRLEN(&phr);
            }

            if ((decideLen + new_sent->reprLen + tmpReprLen) > x->maxRltLen) {
                /* 最大出力表記文字列長を超えた */
                /* 出力長上限到達フラグを立てる */
                CNV_SET_OVERRUN(x, 1); /*NCH_FB*/
                continue; /*NCH_FB*/
            }
            /* 候補フィルタリング */
            if (wsent_pushPhrase(iwnn, x, new_sent, &phr, NULL) == 0) {
                /* 登録できない場合は、次解析文の処理へ */
                continue;
            }
        } else {
            new_sent = &(x->sentence[x->sentNum]);
            *new_sent = x->sentence[id];

            if (WSENT_GETLASTPOS(new_sent) == x->hinsi.r_giji) {
                /*
                 * 直前の文節も擬似文節の場合、直前の文節を伸ばす
                 * ※擬似文節は 読み長 == 表記長 であることが前提
                 */
                pp = wsent_popPhrase(iwnn, x, new_sent, NULL);
                if (pp == NULL) {
                    return NJ_SET_ERR_VAL(NJ_FUNC_CONV_GIJIPROCESS, NJ_ERR_INVALID_RESULT); /*NCH*/
                }
                if (iwnn->njc_mode == NJC_MODE_MORPHO) {
                    tmpReprLen = RLT_GETYOMILEN(pp);
                } else {
                    tmpReprLen = RLT_GETREPRLEN(pp);
                }

                if ((decideLen + new_sent->reprLen + tmpReprLen + ylen) > x->maxRltLen) {
                    /* 最大出力表記文字列長を超えた */
                    /* 出力長上限到達フラグを立てる */
                    CNV_SET_OVERRUN(x, 1);
                    continue;
                }
                /*
                 * 付属語が無く、自立語のタイプがNJ_TYPE_HIRAGANA(ひらがな)で
                 * あれば、連文節処理中に発生した疑似候補である。
                 */
                if ((RLT_HASFZK(pp) == 0) && (pp->word.stem.type == NJ_TYPE_HIRAGANA) &&
                    (pp->word.stem.loc.handle == NULL)) {
                    yomi_top = pp->word.yomi; /* 前回の読み先頭アドレス保存 */
                    rlt_setHiraGiji(pp, (NJ_UINT16)(ylen + RLT_GETYOMILEN(pp)), x->hinsi.f_giji, x->hinsi.r_giji);
                    pp->word.yomi = yomi_top;
                    /*
                     * 作成もとの疑似文節は不要(次回以降の解析に必要がない)なので
                     * 解析対象外の解析文とする。
                     */
                    x->sentence[id].invalid = 2;
                }
                /* 候補フィルタリング */
                if (wsent_pushPhrase(iwnn, x, new_sent, pp, NULL) == 0) {
                    /* 登録できない場合は、次解析文の処理へ */
                    continue;
                }
            }

            /*
             * 既存の擬似文節に擬似文字追加が行われていない時
             */
            if (x->sentence[id].invalid == 0) {
                /* 終端ベクトルチェックを行う */
                if (!CONNECT_R_PHRASE_END(iwnn->njc_mode, x->sentence[id].connect, x->hinsi)) {
                    /* 前文節が終止形でない場合、擬似候補の追加は行わない */
                    continue;
                }
                rlt_setHiraGiji(&phr, ylen, x->hinsi.f_giji, x->hinsi.r_giji);
                phr.word.yomi = yomi_top;
                if (iwnn->njc_mode == NJC_MODE_MORPHO) {
                    tmpReprLen = RLT_GETYOMILEN(&phr);
                } else {
                    tmpReprLen = RLT_GETREPRLEN(&phr);
                }

                if ((decideLen + new_sent->reprLen + tmpReprLen) > x->maxRltLen) {
                    /* 最大出力表記文字列長を超えた */
                    /* 出力長上限到達フラグを立てる */
                    CNV_SET_OVERRUN(x, 1);
                    continue;
                }
                /* 候補フィルタリング */
                if (wsent_pushPhrase(iwnn, x, new_sent, &phr, NULL) == 0) {
                    /* 登録できない場合は、次解析文の処理へ */
                    continue;
                }
            }
        }

        /* 文配列への組み込み */
        if (conv_replaceSentence(x) > 0) {
            id++;
            ret++;
        }
    }

    /*
     * 疑似候補作成中に作成された解析不要文を削除する
     */
    p0 = x->sentence;
    p1 = x->sentence;
    for (id = sentNum = 0; id < x->sentNum; id++, p0++) {
        if (p0->invalid != 2) {
            if (p0 != p1) {
                /* 移動先、移動元のアドレスが異なる場合、移動 */
                *p1 = *p0;
            }
            p1++;
            sentNum++;
        }
    }
    x->sentNum = sentNum;

    /* 最大評価文の文節数以下の文節を持つ文情報を削除する */
    conv_removeSent(x, (NJ_INT16)(x->sentence[0].phrNum + x->sentence[0].vphrNum));


    return ret;
}


/**
 * 擬似候補情報に設定(初期化)する
 *
 * @param[in] r      文節情報
 * @param[in] len    擬似候補長
 * @param[in] f_giji 擬似品詞 前品詞番号
 * @param[in] r_giji 擬似品詞 後品詞番号
 *
 * @return なし
 */
static void rlt_setHiraGiji(NJ_RESULT *r, NJ_UINT16 len, NJ_UINT16 f_giji,
                            NJ_UINT16 r_giji) {


    rlt_init(r);
    r->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH | NJ_DIC_GIJI | NJ_TYPE_GIJI_BIT);
    NJ_SET_YLEN_TO_STEM(&r->word, len);
    NJ_SET_KLEN_TO_STEM(&r->word, len);
    NJ_SET_FPOS_TO_STEM(&r->word, f_giji);
    NJ_SET_BPOS_TO_STEM(&r->word, r_giji);
    NJ_SET_FREQ_TO_STEM(&r->word, GIJI_GETSCORE(len));
    NJ_SET_TYPE_TO_STEM(&r->word, NJ_TYPE_HIRAGANA);
}


/**
 * 最後尾の文節を残して文節を削除する
 *
 * 一つしか文節を含まない場合は、何もしない。
 *
 * @param[in] iwnn 解析情報クラス
 * @param[in] ws     作業用文情報保持領域
 *
 * @return なし
 */
static void wsent_reducePhrases(NJ_CLASS *iwnn, NJC_WORK_SENTENCE *ws) {

    NJ_UINT8 i;
    NJ_RESULT *phr;
    NJC_PHRASE_INFO *info;


    /* 一つしか文節を含まない場合は，何もしない */
    if (ws->phrNum == 1) {
        return;
    }

    /* 末尾を先頭にコピー */
    phr = &(ws->phrases[0]);
    *phr = ws->phrases[ws->phrNum - 1];

    info = &(ws->info[0]);
    *info = ws->info[ws->phrNum - 1];

    for (i = 1; i < ws->phrNum; i++) {
        phr = &(ws->phrases[i]);
        ws->score -= RLT_GETSCORE_CONV(phr);

        /* 情報を初期化 */
        rlt_init(phr);
        info_init(&(ws->info[i]));
    }

    phr = &(ws->phrases[0]);
    ws->score = RLT_GETSCORE_CONV(phr);

    /* 表記文字配列長の変更 */
    if (iwnn->njc_mode == NJC_MODE_MORPHO) {
        ws->reprLen = RLT_GETYOMILEN(phr);
    } else {
        ws->reprLen = RLT_GETREPRLEN(phr);
    }

    /* 読み文字配列長の変更 */
    ws->yomiLen = RLT_GETYOMILEN(phr);

    /* 文節数の設定 */
    ws->phrNum = 1;
    ws->vphrNum = 0;


}


/**
 * 全候補取得時の候補頻度取得
 *
 * @param[in]  result   候補
 *
 * @return              頻度値
 */
static NJ_INT32 cand_getScore(NJ_RESULT* result) {
    NJ_INT32    freq_stem_fzk;


    freq_stem_fzk = NJ_GET_FREQ_FROM_FZK(&(result->word));

    if (freq_stem_fzk >= 0) {
        freq_stem_fzk = NJ_GET_FREQ_FROM_STEM(&(result->word));
    } else {
        freq_stem_fzk += NJ_GET_FREQ_FROM_STEM(&(result->word));

        if (freq_stem_fzk < NJ_MIN_FREQ_WITH_UNDER_BIAS) {
            freq_stem_fzk = NJ_MIN_FREQ_WITH_UNDER_BIAS;
        } else if (freq_stem_fzk > NJ_MAX_FREQ_WITH_UNDER_BIAS) {
            freq_stem_fzk = NJ_MAX_FREQ_WITH_UNDER_BIAS;
        }
    }
    return freq_stem_fzk + NJC_PHRASE_COST;
}


/**
 * 全候補取得処理（１候補の解析が完了次第結果を返す）
 *
 * @param[in]  iwnn   解析情報クラス
 * @param[in]  dics     変換で利用対象となる辞書セット
 * @param[in]  target   njx_convで返された処理結果内で、全候補取得を対象とする
 *                      文節位置の処理結果
 * @param[out] result   全候補結果格納バッファ<br>
 *                      １候補分の領域を呼出元で用意すること
 *
 * @retval >=0 全候補の候補数（０：候補なし）
 * @retval <0  異常終了
 */
NJ_INT16 njc_zenkouho1(NJ_CLASS *iwnn, NJ_DIC_SET *dics, NJ_RESULT *target,
                       NJ_RESULT *result) {

    NJC_CANDIDATE *cbuf;
    NJ_INT16 ret;

        
    /*
     * 引数チェック
     */
    if (dics == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_ZENKOUHO1, NJ_ERR_PARAM_DIC_NULL); /*NCH_FB*/
    }
    if (result == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_ZENKOUHO1, NJ_ERR_PARAM_RESULT_NULL); /*NCH_FB*/
    }

    cbuf = &(iwnn->m_Buf.Cand);
    
    if (target == NULL) {
        /*
         * 次候補取得
         */
        ret = cand_getCandidate(iwnn, cbuf, result);    /* 全候補取得処理継続   */
    } else {
        /*
         * 新規で全候補を取得する
         */
        cand_init(iwnn, cbuf);          /* ワ−クバッファ初期化 */
        cbuf->cond.ds = dics;           /* 検索対象辞書セット設定       */
        cbuf->yomi = target->word.yomi; /* 検索対象読み文字列先頭アドレス設定 */
        cbuf->yomiLen = RLT_GETYOMILEN(target); /* 検索対象読み文字配列長設定 */
        cbuf->cond.top_set = 0;         /* 先頭候補指定フラグOFF        */
        ret = cand_getCandidate(iwnn, cbuf, result);    /* 全候補取得処理実行   */
    }

    if (ret < 0) {                      /* 異常処理発生         */
        return ret;                     /* 返された異常値でリタ−ン */ /*NCH_FB*/
    }

    return ret; /* 1: 候補あり、0: 候補なし */
}


/**
 * 全候補取得処理（全ての解析を行う）
 *
 * @param[in]  iwnn        解析情報クラス
 * @param[in]  target        njx_conv処理結果内の、全候補取得対象文節位置の処理結果
 * @param[in]  candidate_no  全候補 候補番号(0 origin)
 * @param[out] result        全候補結果格納バッファ<br>
 *                           １候補分の領域を呼出元で用意すること
 * @param[in]  top_set       0: targetを全候補リストの先頭に追加しない<br>
 *                           1: targetを全候補リストの先頭に追加する
 *
 * @retval >=0  全候補の候補数（０：候補なし）
 * @retval <0   異常終了
 */
NJ_INT16 njc_zenkouho(NJ_CLASS *iwnn, NJ_RESULT *target,
                      NJ_UINT16 candidate_no, NJ_RESULT *result, 
                      NJ_UINT8 top_set) {

    NJC_CANDIDATE *cbuf;
    NJ_UINT16 ylen;
    NJ_INT16 ret;


    /*
     * 引数チェック
     */
    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_ZENKOUHO, NJ_ERR_PARAM_ENV_NULL); /*NCH_FB*/
    }
    if (result == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_ZENKOUHO, NJ_ERR_PARAM_RESULT_NULL);
    }

    cbuf = &(iwnn->m_Buf.Cand);
    
    if (target != NULL) {
        /*
         * 同読みでの全候補リストが存在した場合でも、辞書を変えて
         * 引き直すことが想定される為、全候補を取得し直す
         */
        cand_init(iwnn, cbuf);
        cbuf->cond.ds = &(iwnn->dic_set);
        ylen = RLT_GETYOMILEN(target);
        cbuf->yomi = target->word.yomi;
        cbuf->yomiLen = ylen;
        if (top_set == 1) {
            cbuf->cond.top_set = 1;
            cbuf->cond.target = target;
        }
        ret = cand_getCandidate(iwnn, cbuf, NULL);
        if (ret < 0) {
            return ret;
        }
    }

    if (candidate_no >= cbuf->phrNum) {
        /* 指定された候補番号が範囲外 */
        return 0;
    }

    *result = cbuf->phrases[candidate_no];

    return cbuf->phrNum;
}


/**
 * 全候補処理結果保持領域 初期化
 *
 * @param[in]  iwnn  解析情報クラス
 * @param[out] x       全候補処理結果保持領域
 *
 * @return なし
 */
static void cand_init(NJ_CLASS *iwnn, NJC_CANDIDATE *x) {

    NJ_INT16 i;


    x->yomi = NULL;
    x->yomiLen = 0;
    for (i = 0; i <= NJC_MAX_FZK_CANDIDATE; i++) {
        fzk_rlt_init(&(x->fzk_phrs[i]));
    }
    x->fzkPhrNum = 0;
    for (i = 0; i < NJ_MAX_CANDIDATE; i++) {
        rlt_init(&(x->phrases[i]));
    }
    x->phrNum = 0;
    for (i = 0; i < NJ_MAX_DIC; i++) {
        x->jds.dic[i].srhCache  = NULL;
        x->jds.dic[i].limit     = 0;
        x->fds.dic[i].srhCache  = NULL;
        x->fds.dic[i].limit     = 0;
        x->jds2.dic[i].srhCache = NULL;
        x->jds2.dic[i].limit    = 0;
        x->jds2.dic[i].handle   = NULL;
        x->jds.dic[i].handle    = NULL;
        x->fds.dic[i].handle    = NULL;
    }
    INIT_KEYWORD_IN_NJ_DIC_SET(&(x->jds));
    x->jds.mode = 0;
    INIT_KEYWORD_IN_NJ_DIC_SET(&(x->fds));
    x->fds.mode = 0;
    INIT_KEYWORD_IN_NJ_DIC_SET(&(x->jds2));
    x->jds2.mode = 0;
    x->jds2.rHandle[NJ_MODE_TYPE_HENKAN] = NULL;
    x->jds.rHandle[NJ_MODE_TYPE_HENKAN] = NULL;
    x->fds.rHandle[NJ_MODE_TYPE_HENKAN] = NULL;
    x->jds2.rHandle[NJ_MODE_TYPE_YOSOKU] = NULL;
    x->jds.rHandle[NJ_MODE_TYPE_YOSOKU] = NULL;
    x->fds.rHandle[NJ_MODE_TYPE_YOSOKU] = NULL;
    x->jds2.rHandle[NJ_MODE_TYPE_MORPHO] = NULL;
    x->jds.rHandle[NJ_MODE_TYPE_MORPHO] = NULL;
    x->fds.rHandle[NJ_MODE_TYPE_MORPHO] = NULL;

    x->cond.ds = NULL;
    x->cond.target = NULL;
    x->cond.top_set = 0;
    x->cond.mode = ZEN_MODE_INIT;

    x->jwork = &(iwnn->m_cwork);

    for (i = 0; i < NJC_MAX_STEM_CANDIDATE; i++) {
        x->jwork->list[i].cur = &(iwnn->m_ccur[i]);
    }
}

/**
 * 全候補取得処理ワーク 環境設定
 *
 * @param[out] x   全候補処理結果保持領域
 *
 * @retval 0  正常
 * @retval <0 異常
 */
static NJ_INT16 cand_env_set(NJC_CANDIDATE *x) {

    NJ_DIC_INFO   *pstem, *pfzk, *ptarget, *pstem_learn;
    NJ_UINT16 i;
    NJ_UINT32 type;


    /* ルール辞書アドレス 設定 */
    x->jds.rHandle[NJ_MODE_TYPE_HENKAN] = x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN];
    x->jds2.rHandle[NJ_MODE_TYPE_HENKAN] = x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN];
    x->fds.rHandle[NJ_MODE_TYPE_HENKAN] = x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN];
    

    for (i = 0; i < NJ_MAX_DIC; i++) {
        /* 辞書情報アドレス 設定 */
        pstem       = &(x->jds.dic[i]);         /* 自立語解析用          */
        pstem_learn = &(x->jds2.dic[i]);        /* 自立語解析用(学習辞書)*/
        pfzk        = &(x->fds.dic[i]);         /* 付属語解析用          */
        ptarget     = &(x->cond.ds->dic[i]);    /* 解析対象辞書          */

        njd_clear_dicinfo(pstem);
        njd_clear_dicinfo(pstem_learn);
        njd_clear_dicinfo(pfzk);

        /* 辞書が存在し、辞書頻度の high, base が正しい場合のみ解析用辞書情報を設定する */
        if((ptarget->handle != NULL) &&
           (NJ_CHECK_USE_DIC_FREQ(ptarget->dic_freq) != 0)) {

            type = NJ_GET_DIC_TYPE_EX(ptarget->type, ptarget->handle);
            switch (type) {
            case NJ_DIC_TYPE_JIRITSU:                       /* 自立語辞書 */
            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ) */
            case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書 */
            case NJ_DIC_TYPE_FUSION:                        /* 統合辞書 */
            case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(高圧縮タイプ) */
            case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
            case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書) */
            case NJ_DIC_TYPE_TANKANJI:                      /* 単漢字辞書 */
            case NJ_DIC_TYPE_CUSTOM_COMPRESS:               /* 圧縮カスタマイズ辞書 */
            case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:             /* 非圧縮カスタマイズ辞書 */
            case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:       /* 非圧縮カスタマイズ辞書(学習辞書変更) */
            case NJ_DIC_TYPE_USER:                          /* ユーザ辞書 */
            case NJ_DIC_TYPE_FORECONV:                      /* 予測変換辞書（iWnnでは未使用）  */
            case NJ_DIC_TYPE_PROGRAM:                       /* 擬似辞書  */
                /* 自立語解析に使用 */
                njd_copy_dicinfo(pstem, ptarget, NJ_MODE_TYPE_HENKAN);
                break;

            case NJ_DIC_TYPE_LEARN:                         /* 学習辞書 */
                /* 自立語解析(学習辞書)に使用 */
                njd_copy_dicinfo(pstem_learn, ptarget, NJ_MODE_TYPE_HENKAN);
                break;

            case NJ_DIC_TYPE_FZK:                           /* 付属語辞書 */
            case NJ_DIC_TYPE_PROGRAM_FZK:                   /* 擬似辞書-付属語-  */
                /* 付属語解析に使用 */
                njd_copy_dicinfo(pfzk, ptarget, NJ_MODE_TYPE_HENKAN);
                break;
            default:
                /* 解析には使用しない */
                break;
            }
        }
    }
    if (x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN] == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_CAND_ENV_SET, NJ_ERR_NO_RULEDIC);
    }
    x->hinsi.f_v2 = njd_r_get_hinsi(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_V2_F);
    x->hinsi.f_giji = njd_r_get_hinsi(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_GIJI_F);
    x->hinsi.r_giji = njd_r_get_hinsi(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_GIJI_B);
    x->hinsi.r_ngiji = njd_r_get_hinsi(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_SUUJI_B);
    x->hinsi.r_bunto = njd_r_get_hinsi(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_BUNTOU_B);
    if ((x->hinsi.f_v2 == 0) || (x->hinsi.f_giji == 0) ||
        (x->hinsi.r_giji == 0) || (x->hinsi.r_ngiji == 0) || (x->hinsi.r_bunto == 0)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_CAND_ENV_SET, NJ_ERR_NO_HINSI);
    }

    /*
     * 登録されている前品詞数、後品詞数を取得
     */
    njd_r_get_count(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], &x->hinsi.fcnt, &x->hinsi.bcnt);

    return 0;
}


/**
 * 全候補解析
 *
 * @param[in]     iwnn    解析情報クラス
 * @param[in,out] x         全候補処理結果保持領域
 * @param[out]    result    全候補取得結果（１結果）<br>
 *                          NULLのときは、全候補の全てを解析する。
 *
 * @retval 0 正常
 * @retval <0 エラー
 */
static NJ_INT16 cand_getCandidate(NJ_CLASS *iwnn, NJC_CANDIDATE *x, NJ_RESULT *result) {

    NJ_INT16 ret;
    NJ_RESULT *new_phr;
    

    /*
     * 初回実行時は、付属語解析を行う */
    if (x->cond.mode == ZEN_MODE_INIT) {
        /* 解析用環境 初期化 */
        if ((ret = cand_env_set(x)) < 0) {
            return ret;
        }
        /* ダミーの自立語を生成する */
        cand_dummyProcess(x);

        /* ダミーの自立語を持つ付属語文節を生成する */
        ret = cand_fzkProcess(iwnn, x);
        if (ret < 0) {
            return ret; /*NCH_FB*/
        }
    }

    /* 全候補リスト先頭に候補追加要求がある場合追加する。*/
    if (x->cond.top_set && (result == NULL)) {
        new_phr = &(x->phrases[0]);
        *new_phr = *x->cond.target;
        /* オペレーションIDを設定 */
        new_phr->operation_id &= ~NJ_OP_MASK;
        new_phr->operation_id |= NJ_OP_CONVERT;
        new_phr->operation_id &= ~NJ_FUNC_MASK;
        new_phr->operation_id |= NJ_FUNC_ZENKOUHO;
        x->phrNum = 1;
        /* 同表記バッファに追加 */
        ret = nje_append_homonym(iwnn, new_phr, x->yomiLen, &x->offset[0]);
        if (ret < 0) {
            /* 同表記バッファ登録で異常発生 */
            return ret; /*NCH*/
        }
    }

    /* 自立語を検索し付属語を繋げて文節を追加する */
    ret = cand_stemProcess(iwnn, x, result);

    return ret;
}


/**
 * 付属語解析
 *
 * @param[in]  iwnn   解析情報クラス
 * @param[out] x        全候補処理結果保持領域
 *
 * @retval >=0 作業バッファ登録文節数
 * @retval <0  エラー
 */
static NJ_INT16 cand_fzkProcess(NJ_CLASS *iwnn, NJC_CANDIDATE *x) {

    NJ_CURSOR cur, ctmp;
    NJC_FZK_RESULT *phr, *new_phr;
    NJ_RESULT result;
    NJ_INT16 N;
    NJ_INT16 id;
    NJ_UINT16 dlen, ylen, reprLen, i, k;
    NJ_CHAR  *yomi_top;
    NJ_INT16 num;
    NJ_INT16 ret = 0;
    NJ_UINT16 slen;
    NJ_INT16 sslen;
    NJ_UINT8 exit_flag;


    /* 読み長が短かい順に処理する */
    N = 0;

    /****************************
     形態素解析で全候補取得を使う場合は、operation を変更する必要あり。
    ****************************/
    njd_init_search_condition(&(cur.cond));
    cur.cond.operation = NJ_CUR_OP_COMP;
    cur.cond.mode = NJ_CUR_MODE_FREQ;
    cur.cond.ds = &x->fds;
    cur.cond.hinsi.foreSize = x->hinsi.fcnt;
    cur.cond.hinsi.foreFlag = 1;

    while ((sslen = cand_getShortestLen(x, &N, &id)) > 0) {
        slen = (NJ_UINT16)sslen;
        yomi_top = &(x->yomi[slen]);
        /* その直後で付属語を検索する */
        for (i = slen; i <= x->yomiLen; i += NJ_CHAR_LEN(&(x->yomi[i]))) {

            if (i == slen) {
                continue;
            }
            /* 辞書検索 */
            ylen = i - slen;
            cur.cond.yomi = yomi_top;
            cur.cond.ylen = ylen;

            num = njd_search_word(iwnn, &cur, 1, &exit_flag);
            if (num == 0) {
                /* 候補なし */
                if (exit_flag == 1) {
                    /* 前方一致候補も存在しないので      */
                    /* これ以上検索しても意味なし        */
                    break;
                }
                continue;
            } else if (num < 0) {
                /* 単語検索処理で異常発生 */
                return num; /*NCH*/
            }

            /* N番目の最短読み長の文についてループ */
            for (k = id; k < x->fzkPhrNum; k++) {
                if (FZK_GET_TLEN(&(x->fzk_phrs[k])) != slen) {
                    /* N番目の最短読み長でない */
                    break;
                }
                /* 全辞書検索一致語についてループ */
                ctmp = cur;
                while ((ret = njd_get_word(iwnn, &ctmp, &result, 1)) > 0) {
                    
                    /*
                     * 接続チェック
                     *   ダミー自立語しかない場合はチェックしない。
                     */
                    phr = &(x->fzk_phrs[k]);
                    if ((FZK_GET_YLEN(phr) > 0) &&
                        (!connect(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], &x->hinsi,
                                  FZK_GET_BPOS(phr), NJ_GET_FPOS_FROM_STEM(&result.word)))) {
                        /* 繋がらければ無視 */
                        continue;
                    }


                    /* 辞書検索一致語の表記の長さ */
                    dlen = NJ_GET_KLEN_FROM_STEM(&result.word);
                    reprLen  = FZK_GET_TLEN(phr);

                    if ((reprLen + dlen) > NJ_MAX_RESULT_LEN) {
                        /* 表記長が上限を超えた場合，無視する */
                        continue;
                    }

                    /* 追加文節を作成 */
                    new_phr = &(x->fzk_phrs[x->fzkPhrNum]);
                    *new_phr = *phr;

                    if (FZK_GET_YLEN(phr) == 0) {
                        FZK_SET_FPOS(new_phr, NJ_GET_FPOS_FROM_STEM(&result.word));
                    }
                    FZK_SET_YLEN(new_phr, (FZK_GET_YLEN(new_phr) + ylen));
                    FZK_SET_TLEN(new_phr, (FZK_GET_TLEN(new_phr) + ylen));
                    FZK_SET_BPOS(new_phr, NJ_GET_BPOS_FROM_STEM(&result.word));
                    FZK_SET_FREQ(new_phr, FZK_GET_FREQ(new_phr) + NJ_GET_FREQ_FROM_STEM(&result.word));

                    /* 文配列への組み込み */
                    if (cand_replaceDummyPhrase(x, 0, 1) > 0) {
                        k++;
                        id++;
                        if (k >= x->fzkPhrNum) {
                            /*
                             * 検索範囲が解析バッファフルによって
                             * 既に消去されている。
                             */
                            break;
                        }
                    }
                }
                if (ret < 0) {
                    /* 単語取得処理で異常発生 */
                    return ret; /*NCH*/
                }
            }
        }
    }

    phr = x->fzk_phrs;
    ret = 0;
    while (FZK_GET_TLEN(phr) == x->yomiLen) {
        if ((FZK_GET_YLEN(phr) != 0) && 
            (!connect(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], &x->hinsi, FZK_GET_BPOS(phr), x->hinsi.f_v2))) {
            /* 終端ベクトルチェック */
            /* 繋がらければ削除 */
            cand_removeCandidate(x, ret);
        } else {
            ret++;
            phr++;
        }
    }

    x->fzkPhrNum = ret;
    return ret;
}


/**
 * ダミー自立語作成処理
 *
 * @param[out] x    全候補処理結果保持領域
 *
 * @return ダミー自立語作成数
 */
static NJ_UINT16 cand_dummyProcess(NJC_CANDIDATE *x) {

    NJ_UINT16 ylen, ret;
    NJC_FZK_RESULT *phr;
    NJ_INT16 mcnt, clen;


    /* 文字数取得 */
    ylen = 0;
    clen = 0;
    while (ylen < x->yomiLen) {
        ylen += NJ_CHAR_LEN(&(x->yomi[ylen])); /* 現処理文字バイト長 加算 */
        clen++;
    }
    mcnt = 0;       /* ダミー自立語 スキップ回数 */
    if (clen > NJC_MAX_FZK_LEN) {
        mcnt = clen - NJC_MAX_FZK_LEN - 1;
    }

    ylen = NJ_CHAR_LEN(&(x->yomi[0]));
    ret = 0;

    while (ylen <= x->yomiLen) {
        if (mcnt == 0) {
            phr = &(x->fzk_phrs[x->fzkPhrNum]);
            fzk_rlt_init(phr);

            /*
             * ダミー自立語定義
             *   読み長＝表記長
             *   前品詞：０（ワイルドカード）
             *   後品詞：０（ワイルドカード）
             *   文節情報として付属語はなし
             *      読み長、前品詞、後品詞ともに０
             */
            FZK_SET_TLEN(phr, ylen);

            /* 文節配列への組み込み */
            if (cand_replaceDummyPhrase(x, 1, 0) > 0) {
                ret++;
            }

        } else {
            mcnt--;
        }
        ylen += NJ_CHAR_LEN(&(x->yomi[ylen]));
    }

    return ret;
}


/**
 * ダミー解析文節情報の追加
 *
 * @param[in,out] x         全候補処理結果保持領域
 * @param[in]     overwrite 評価値が同じ場合に<br>
 *                             1 .. 後から追加した方を優先<br>
 *                             0 .. 元にある方を優先
 * @param[in]     fzkcheck  付属語品詞チェック<br>
 *                             1...チェックする<br>
 *                             0...チェックしない
 *
 * @retval 0 新規追加なし
 * @retval 1 新規追加あり
 */
static NJ_INT16 cand_replaceDummyPhrase(NJC_CANDIDATE *x, NJ_UINT8 overwrite,
                                        NJ_UINT8 fzkcheck) {

    NJC_FZK_RESULT *new_phr;
    NJ_INT16 ret;
    NJ_INT16 sameID;
    NJ_INT32 score1, score2;


    new_phr = &(x->fzk_phrs[x->fzkPhrNum]);

    ret = 0;
    sameID = cand_getEqualDummyPhrase(x, x->fzkPhrNum, fzkcheck);
    if (sameID < 0) {
        /* 等価文が無ければ，新規追加 */
        x->fzkPhrNum++;
        cand_sortDummyArea(x, (NJ_INT16)(x->fzkPhrNum - 1));

        /* 保持上限を超えた場合 */
        if (x->fzkPhrNum > NJC_MAX_FZK_CANDIDATE) {
            x->fzkPhrNum = NJC_MAX_FZK_CANDIDATE;
        }

        /* 新規追加されたときは 1 を返す */
        ret = 1;
    } else {
        score1 = FZK_GET_SCORE(&(x->fzk_phrs[sameID]));
        score2 = FZK_GET_SCORE(new_phr);

        if ((score1 < score2)  || (overwrite && (score1 == score2))) {
            /*
             * 等価文があれば，評価値の高い方を残す
             */
            x->fzk_phrs[sameID] = *new_phr;
            cand_sortDummyArea(x, sameID);
        }
    }


    return ret;
}


/**
 * 作業バッファ内に同表記文節情報がないか検索する
 *
 * @param[in,out] x         全候補処理結果保持領域
 * @param[in]     phr_off   検索対象 文節位置
 * @param[in]     poscheck  1: 付属語品詞の比較をする、0:比較しない
 *
 * @retval >=0 同表記文節情報格納位置
 * @retval -1  同表記情報なし
 */
static NJ_INT16 cand_getEqualDummyPhrase(NJC_CANDIDATE *x, NJ_UINT16 phr_off,
                                         NJ_UINT8 poscheck) {

    NJC_FZK_RESULT *phr0, *phr;
    NJ_UINT16 i;
    NJ_UINT16 ylen0, ylen;


    phr = &(x->fzk_phrs[phr_off]);
    ylen = FZK_GET_TLEN(phr);

    phr0 = x->fzk_phrs;
    for (i = 0; i < x->fzkPhrNum; i++, phr0++) {
        ylen0 = FZK_GET_TLEN(phr0);
        if (ylen0 == ylen) {    /* 文節の読み長が一致している場合 */
            if (poscheck) {
                /* 付属語の前品詞、後品詞が同じでない場合は不一致と判定（付属語解析時のみ） */
                if ((FZK_GET_FPOS(phr0) != FZK_GET_FPOS(phr)) || 
                    (FZK_GET_BPOS(phr0) != FZK_GET_BPOS(phr))) {
                    continue;
                }
            }
            if (FZK_GET_YLEN(phr0) == FZK_GET_YLEN(phr)) {
                /* 付属語の表記長で同表記チェックとする */
                return i;
            }
        } else if (ylen0 < ylen) {
            /* ソートされているので，これ以上調べる必要なし */
            break;
        }
    }
    return -1;
}


/**
 * 文節列をソートする
 *
 *  指定ID の文の位置のみを変更する。指定ID 以外の部分は、既にソートされていることが前提。
 *  ソート順は、読み長降順、読み長が同じ場合は評価値降順。
 *
 *  文を追加したときに使用する。
 *
 * @param[out] x      全候補処理結果保持領域
 * @param[in]  phrID  作業バッファ内更新候補情報位置(ID)
 *
 * @return なし
 */
static void cand_sortDummyArea(NJC_CANDIDATE *x, NJ_INT16 phrID) {

    NJ_INT16 index;
    NJ_UINT16 ylen1, ylen2;
    NJ_INT32 score1, score2;
    NJC_FZK_RESULT tmp;
    NJC_FZK_RESULT *phr;


    tmp = x->fzk_phrs[phrID];
    ylen1  = FZK_GET_TLEN(&tmp);
    score1 = FZK_GET_SCORE(&tmp);

    phr = &(x->fzk_phrs[((phrID == 0)? 0 : (phrID - 1))]);
    ylen2  = FZK_GET_TLEN(phr);
    score2 = FZK_GET_SCORE(phr);

    if ((phrID == 0) || (ylen1 < ylen2) ||
        ((ylen1 == ylen2) && (score1 < score2))) {
        /* 現在位置よりも下に移動させる */
        index = phrID + 1;
        while ((NJ_UINT16)index < x->fzkPhrNum) {
            phr = &(x->fzk_phrs[index]); /*NCH_FB*/
            ylen2  = FZK_GET_TLEN(phr); /*NCH_FB*/
            score2 = FZK_GET_SCORE(phr); /*NCH_FB*/
            if ((ylen1 > ylen2) || ((ylen1 == ylen2) && (score1 > score2))) { /*NCH_FB*/
                /* 落ち付き先発見 */
                x->fzk_phrs[index - 1] = tmp; /*NCH_FB*/
                break; /*NCH_FB*/
            }
            x->fzk_phrs[index - 1] = x->fzk_phrs[index]; /*NCH_FB*/
            index++; /*NCH_FB*/
        }
        if ((NJ_UINT16)index >= x->fzkPhrNum) {
            /* 最後尾に落ち付く場合 */
            x->fzk_phrs[x->fzkPhrNum - 1] = tmp;
        }

    } else {
        /* 現在位置よりも上に移動させる */
        index = phrID - 1;
        while (index >= 0) {
            phr = &(x->fzk_phrs[index]);
            ylen2  = FZK_GET_TLEN(phr);
            score2 = FZK_GET_SCORE(phr);
            if ((ylen1 < ylen2) || ((ylen1 == ylen2) && (score1 <= score2))) {
                /* 落ち付き先発見 */
                x->fzk_phrs[index + 1] = tmp;
                break;
            }
            x->fzk_phrs[index + 1] = x->fzk_phrs[index];
            index--;
        }
        if (index < 0) {
            /* 先頭に落ち付く場合 */
            x->fzk_phrs[0] = tmp;
        }
    }
}


/**
 * 解析文節情報の追加
 *
 * @param[in]     iwnn        解析情報クラス
 * @param[in,out] x           全候補処理結果保持領域
 * @param[in]     overwrite   評価値が同じ場合に<br>
 *                               1 .. 後から追加した方を優先<br>
 *                               0 .. 元にある方を優先
 * @param[in]     phrID_after 追加文節の落ち着き先のID
 *
 * @retval 0 新規追加なし
 * @retval 1 新規追加あり
 */
static NJ_INT16 cand_replacePhrase(NJ_CLASS *iwnn, NJC_CANDIDATE *x, NJ_UINT8 overwrite, NJ_INT16* phrID_after) {

    NJ_RESULT *new_phr;
    NJ_INT16 ret;
    NJ_INT16 sameID;
    NJ_INT32 score1, score2;
    NJ_UINT16 ope_id;


    new_phr = &(x->phrases[x->phrNum]);
    
    /* オペレーションIDは、ここでまとめて設定 */
    new_phr->operation_id &= ~NJ_OP_MASK;
    new_phr->operation_id |= NJ_OP_CONVERT;
    new_phr->operation_id &= ~NJ_FUNC_MASK;
    new_phr->operation_id |= NJ_FUNC_ZENKOUHO;

    ret = 0;
    *phrID_after = -1;

    sameID = cand_getEqualPhrase(iwnn, x, x->phrNum);
    if (sameID == -2) {
        /* 同表記バッファ溢れ発生 */
        return 0; /* 追加なし */
    }
    if (sameID < 0) {
        /* 等価文が無ければ，新規追加 */
        x->phrNum++;
        *phrID_after = cand_sortArea(iwnn, x, (NJ_INT16)(x->phrNum - 1));
        
        /* 保持上限を超えた場合 */
        if (x->phrNum > NJ_MAX_CANDIDATE) {
            x->phrNum = NJ_MAX_CANDIDATE; /*NCH_FB*/
        }
        
        /* 新規追加されたときは 1 を返す */
        ret = 1;
    } else {
        score1 = RLT_GETSCORE_CAND(&(x->phrases[sameID]));
        score2 = RLT_GETSCORE_CAND(new_phr);

        if ((score1 < score2) || (overwrite && (score1 == score2))) {
            /*
             * 等価文があれば，評価値の高い方を残す
             */
            ope_id = x->phrases[sameID].operation_id; /* 擬似情報は継承する */
            ope_id = NJ_GET_GIJI_BIT(ope_id);
            new_phr->operation_id &= ~NJ_TYPE_GIJI_BIT;
            new_phr->operation_id |= ope_id;
            
            x->phrases[sameID] = *new_phr;
            *phrID_after = cand_sortArea(iwnn, x, sameID);
        }
    }

    return ret;
}


/**
 * 作業バッファ内に同表記文節情報がないか検索する
 *
 * @param[in]     iwnn   解析情報クラス
 * @param[in,out] x        全候補処理結果保持領域
 * @param[in]     phr_off  検索対象 文節位置
 *
 * @retval >=0 同表記文節情報格納位置
 * @retval -1  同表記情報なし
 * @retval -2  全候補登録禁止
 */
static NJ_INT16 cand_getEqualPhrase(NJ_CLASS *iwnn, NJC_CANDIDATE *x, NJ_UINT16 phr_off) {

    NJ_RESULT *phr;
    NJ_UINT16 j;
    NJ_INT16 ret;
    NJ_INT16 offset;


    phr = &x->phrases[phr_off];
    /* 候補文字列の同表記チェックを行う */
    ret = nje_append_homonym(iwnn, phr, x->yomiLen, &offset);
    if (ret < 0) {
        /* 同表記バッファが溢れた */
        return -2;
    } else if (ret == 1) {
        /* 同表記が存在 */
        for (j = 0; j < x->phrNum; j++) {
            if (x->offset[j] == offset) {
                return j;
            }
        }
        return -2;
    }

    /* 同表記が存在しないので、処理中断 */
    x->offset[phr_off] = offset;
    return -1;
}


/**
 * 文節列をソートする
 *
 * 指定ID の文の位置のみを変更する。
 * 指定ID 以外の部分は、既にソートされていることが前提。
 * ソート順は、読み長降順、読み長が同じ場合は評価値降順。
 *
 * 文を追加したときに使用する。
 *
 * @param[in]  iwnn  解析情報クラス
 * @param[out] x       全候補処理結果保持領域
 * @param[in]  phrID   作業バッファ内更新候補情報位置(ID)
 *
 * @return     指定IDの文の移動後の位置
 */
static NJ_INT16 cand_sortArea(NJ_CLASS *iwnn, NJC_CANDIDATE *x, NJ_INT16 phrID) {

    NJ_INT16 index;
    NJ_UINT16 ylen1, ylen2;
    NJ_INT32 score1, score2;
    NJ_RESULT tmp;
    NJ_RESULT *phr;
    NJ_INT16 tmpoff;


    if ((x->cond.top_set == 1) && (phrID == 0)) {
        /* njx_zenkouho実行時は、先頭候補を固定 */
        return 0;
    }

    tmp = x->phrases[phrID];
    tmpoff = x->offset[phrID];
    ylen1  = RLT_GETYOMILEN(&tmp);
    score1 = RLT_GETSCORE_CAND(&tmp);

    phr = &(x->phrases[((phrID == 0)? 0 : (phrID - 1))]);
    ylen2  = RLT_GETYOMILEN(phr);
    score2 = RLT_GETSCORE_CAND(phr);

    if ((phrID == 0) || (ylen1 < ylen2) ||
        ((ylen1 == ylen2) && (score1 < score2))) {
        /* 現在位置よりも下に移動させる */
        index = phrID + 1;
        while ((NJ_UINT16)index < x->phrNum) {
            phr = &(x->phrases[index]); /*NCH_FB*/
            ylen2  = RLT_GETYOMILEN(phr); /*NCH_FB*/
            score2 = RLT_GETSCORE_CAND(phr); /*NCH_FB*/
            if ((ylen1 > ylen2) || ((ylen1 == ylen2) && (score1 > score2))) { /*NCH_FB*/
                /* 落ち付き先発見 */
                x->phrases[index - 1] = tmp; /*NCH_FB*/
                x->offset[index - 1] = tmpoff; /*NCH_FB*/
                break; /*NCH_FB*/
            }
            x->phrases[index - 1] = x->phrases[index]; /*NCH_FB*/
            x->offset[index - 1] = x->offset[index]; /*NCH_FB*/
            index++; /*NCH_FB*/
        }
        if ((NJ_UINT16)index >= x->phrNum) {
            /* 最後尾に落ち付く場合 */
            x->phrases[x->phrNum - 1] = tmp;
            x->offset[x->phrNum - 1] = tmpoff;
            index = x->phrNum - 1;
        } else {
            index = index - 1;
        }

    } else {
        /* 現在位置よりも上に移動させる */
        index = phrID - 1;
        /* njx_zenkouho実行時は、先頭候補固定 */
        while (((index >= 0) && (x->cond.top_set == 0)) ||
               ((index >  0) && (x->cond.top_set == 1))) {
            phr = &(x->phrases[index]);
            ylen2  = RLT_GETYOMILEN(phr);
            score2 = RLT_GETSCORE_CAND(phr);
            if ((ylen1 < ylen2) || ((ylen1 == ylen2) && (score1 <= score2))) {
                /* 落ち付き先発見 */
                x->phrases[index + 1] = tmp;
                x->offset[index + 1] = tmpoff;
                break;
            }
            x->phrases[index + 1] = x->phrases[index];
            x->offset[index + 1] = x->offset[index];
            index--;
        }
        /* njx_zenkouho非実行時 */
        if ((index < 0) && (x->cond.top_set == 0)) {
            /* 先頭候補に落ち付く場合 */
            x->phrases[0] = tmp; /*NCH_FB*/
            x->offset[0] = tmpoff; /*NCH_FB*/
            index = 0;

            /* njx_zenkouho実行時 */
        } else if ((index <= 0) && (x->cond.top_set == 1)) {
            /* 第２候補に落ち付く場合 */
            x->phrases[1] = tmp;
            x->offset[1] = tmpoff;
            index = 1;
        } else {
            index = index + 1;
        }
    }

    return index;
}


/**
 * ダミー文節作業バッファの中で、Nバイトより長い文のIDを返す
 *
 * @param[out]    x     全候補処理結果保持領域
 * @param[in,out] N     Nバイトよりも長い文を探す<br>
 *                      topID候補の文の長さ(=戻り値)に書き換えられる
 * @param[out]    topID Nバイトより長い文のID格納場所
 *
 * @return topID候補の文の長さ
 */
static NJ_INT16 cand_getShortestLen(NJC_CANDIDATE *x, NJ_INT16 *N,
                                    NJ_INT16 *topID) {

    NJ_INT16 len;
    NJC_FZK_RESULT *frp;


    if (x->fzkPhrNum == 0) {
        /* 文節がなければ 0 を返す */
        *topID = 0; /*NCH_FB*/
        return 0; /*NCH_FB*/
    }

    len = -1;
    frp = &(x->fzk_phrs[x->fzkPhrNum - 1]);
    for (*topID = x->fzkPhrNum - 1; *topID >= 0; (*topID)--, frp--) {
        /*
         * 前回取得時の次に短い候補を探す。
         */
        if (len == -1) {
            if ((NJ_INT16)FZK_GET_TLEN(frp) > *N) {
                len = FZK_GET_TLEN(frp);
            }
        } else {
            if ((NJ_INT16)FZK_GET_TLEN(frp) > len) {
                /*
                 * 目的位置-1の位置を検出した
                 */
                break;
            }
        }
    }
    if (*topID < 0) {
        /*
         * 解析バッファの先頭まで、走査した場合
         */
        *topID = 0;
        return -1;
    }

    /* この時点で，*topID は N番目に短かい文の(先頭ID-1)を指す */
    (*topID)++;
    *N = (NJ_INT16)FZK_GET_TLEN(&(x->fzk_phrs[*topID]));

    return *N;
}


/**
 * 指定された後品詞と前品詞が接続するかチェック
 *
 * @param[in] rule   ルール辞書ハンドル
 * @param[in] info   品詞情報構造体
 * @param[in] posB   後品詞番号
 * @param[in] posF   前品詞番号
 *
 * @retval 0 非接続
 * @retval 1 接続
 */
static NJ_UINT8 connect(NJ_DIC_HANDLE rule, NJC_HINSI_INFO *info,
                        NJ_UINT16 posB, NJ_UINT16 posF) {

    NJ_UINT8 *cf;


    /*
     * 引数チェック
     */
    if ((rule == NULL) || (info == NULL) ||
        (posB == 0) || (posF == 0)) {
        return 0; /*NCH*/
    }

    /*
     * 品詞番号チェック
     */
    if ((info->fcnt < posF) || (info->bcnt < posB)) {
        return 0; /*NCH*/
    }

    /*
     * 後品詞に対する前品詞接続情報を取得
     */
    njd_r_get_connect(rule, posB, 0, &cf);

    posF--;
    
    return (*(cf + (posF / 8)) & (0x80 >> (posF % 8))) ? 1 : 0;
}


/**
 * idx 番目の候補をダミー文節作業バッファから削除する
 *
 * @param[out] x   全候補処理結果保持領域
 * @param[in]  idx 削除バッファ内削除対象ID
 *
 * @return なし
 */
static void cand_removeCandidate(NJC_CANDIDATE *x, NJ_UINT16 idx) {

    NJ_UINT16 i;


    for (i = idx + 1; i < x->fzkPhrNum; i++, idx++) {
        x->fzk_phrs[idx] = x->fzk_phrs[i];
    }
    x->fzkPhrNum = idx;
}


/**
 * 自立語解析
 *
 * @param[in]   iwnn     解析情報クラス
 * @param[out]  x          全候補処理結果保持領域
 * @param[out]  ret_result 全候補取得結果（１結果）
 *
 * @retval >=0 [全候補を一度にすべて取得するモード(ret_result==NULL)の場合]
 *                作業バッファ登録文節数(全候補数)<br>
 *             [全候補を一つずつ取得するモード(ret_result!=NULL)の場合]
 *                1:候補あり、0:候補なし
 * @retval <0 エラー
 */
static NJ_INT16 cand_stemProcess(NJ_CLASS *iwnn, NJC_CANDIDATE *x, NJ_RESULT *ret_result) {

    NJ_UINT16 i, j;
    NJC_CURSOR_LIST *pl;
    NJ_INT16 ret, ret0, entry;
    NJ_RESULT result, next, *rlt;
    NJ_UINT8 cont_num;
    NJ_UINT16 cont_len;
    NJ_UINT8 empty;
    NJ_INT32 maxeval;
    NJ_UINT16 svofs;
    NJC_FZK_RESULT *fzk;
    NJ_UINT16 ylen, fpos;
    NJ_RESULT *cand;
    NJ_UINT8 *hinsi;
    NJ_UINT8 exit_flag;
    NJ_DIC_FREQ dic_freq;
    NJ_PHASE2_FILTER_IF phase2_filter_if;
    NJ_PHASE2_FILTER_MESSAGE ph2_filter_message;
    NJ_INT16 newID;


    entry = 0;  /* 全候補数クリア */

    /*
     * 学習辞書検索カーソルリストを作成する
     */
    if (x->cond.mode == ZEN_MODE_INIT) {        /* 全候補解析 初期状態 */
        pl = x->jwork->list;    /* リスト先頭アドレス取得 */
        x->jwork->listNum = 0;  /* リスト登録数初期化 */

        /* 後品詞：文頭 に接続する前品詞情報を取得 */
        njd_r_get_connect(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], x->hinsi.r_bunto, 0, &hinsi);

        /* すべての読みの組み合わせでリストを作成 */
        for (i = NJ_CHAR_LEN(x->yomi); i <= x->yomiLen; i += NJ_CHAR_LEN(&x->yomi[i])) {
            /* 辞書検索カーソルの初期化 */
            pl->cur->cond.operation = NJ_CUR_OP_COMP;
            pl->cur->cond.mode = NJ_CUR_MODE_FREQ;
            pl->cur->cond.ds = &x->jds2;                /* 学習辞書のみ */
            pl->cur->cond.hinsi.fore = hinsi;
            pl->cur->cond.hinsi.foreSize = x->hinsi.fcnt;
            pl->cur->cond.hinsi.foreFlag = 0;
            pl->cur->cond.hinsi.rear = NULL;
            pl->cur->cond.hinsi.rearSize = x->hinsi.bcnt;
            pl->cur->cond.hinsi.rearFlag = 0;
            pl->cur->cond.hinsi.yominasi_fore = NULL;
            pl->cur->cond.hinsi.prev_bpos = 0;
            pl->cur->cond.yomi = x->yomi;
            pl->cur->cond.ylen = i;
            pl->cur->cond.kanji = NULL;
            pl->cur->cond.charset = NULL;
            pl->cur->cond.fzkconnect = 1;

            /* 単語検索実行 */
            ret = njd_search_word(iwnn, pl->cur, 1, &exit_flag);
            if (ret < 0) {
                /* 単語検索処理で異常発生 */
                return ret; /*NCH*/
            }

            /* 検索語有無設定 */
            pl->status = (ret ? 1 : 0);

            if (pl->status == 0) {
                /* 次カーソル作成へ */
                if (exit_flag == 1) {
                    /* 前方一致候補も存在しないので      */
                    /* これ以上検索しても意味なし        */
                    break;
                }
                continue;
            }

            /* 単語取得実行 */
            while ((ret = njd_get_word(iwnn, pl->cur, &result, 1)) > 0) {
                /* 先頭文節 保存 */
                pl->rlt[0] = result;
                /* 候補文字列長を取得する */
                cont_len = NJ_GET_KLEN_FROM_STEM(&result.word);
                if (cont_len > NJ_MAX_RESULT_LEN) {
                    /* 最大出力候補長 超過 */
                    continue;
                }

                for (j = 0; j < NJ_MAX_DIC; j++) {
                    if (x->cond.ds->dic[j].handle == result.word.stem.loc.handle) {
                        dic_freq = x->cond.ds->dic[j].dic_freq[NJ_MODE_TYPE_HENKAN];
                        break;
                    }
                }

                /* 連続文節数を取得する */
                pl->rltcnt = (NJ_UINT8)(result.word.stem.loc.current_info >> 4) & 0x0F;
                /* 連続文節を取得する */
                for (cont_num = 1; cont_num < pl->rltcnt; cont_num++) {
                    ret0 = njd_get_relational_word(iwnn, &result, &next, &dic_freq);
                    if (ret0 < 0) {
                        return ret0; /*NCH*/
                    }
                    cont_len += NJ_GET_KLEN_FROM_STEM(&next.word);
                    if (cont_len > NJ_MAX_RESULT_LEN) {
                        /* 連続した場合、最大表示出力長を超えないところまでの繋がり文節を取得 */
                        pl->rltcnt = cont_num;
                        /* 結合文節数の更新 */
                        pl->rlt[0].word.stem.loc.current_info = (cont_num << 4) & 0xF0;
                        break;
                    }

                    /* 文節情報 保存 */
                    pl->rlt[cont_num] = next;
                    result = next;
                }
                if (cont_num == pl->rltcnt) {
                    /* 最後まで、連続文節の取得が完了 */
                    break; /*NCH_FB*/
                }
            }
            if (ret < 0) {
                /* 単語検索処理で異常発生 */
                return ret; /*NCH*/
            }
            if (ret == 0) {
                /* 有効な検索語が存在しなかった */
                continue;
            }

            /* カーソルリスト１レコード作成完了 */
            pl++;
            x->jwork->listNum++;
        }
        x->cond.mode = ZEN_MODE_LEARN_PROC;     /* 全候補解析 辞書検索状態へ遷移 */

        x->cond.low_prio_offset = 0;            /* 低頻度付属語溜まりオフセットを初期化する */
    }

    /*
     * 辞書検索カーソルと付属語バッファの候補組み合わせから
     * 学習辞書を対象とした全候補用候補を作成する
     */
    if (x->cond.mode == ZEN_MODE_LEARN_PROC) {  /* 全候補解析 辞書検索 */
        empty = 0;
        while (empty == 0) {
            /*
             * 全候補を１つずつ返す場合
             * 既に、全候補リストに候補が格納してある候補を返す
             */
            if ((ret_result != NULL) && (x->phrNum > x->cond.low_prio_offset)) {
                *ret_result = x->phrases[0];
                /* 全候補リストから、上位に返す候補を削除 */
                for (i = 1; i < x->phrNum; i++) {
                    x->phrases[i-1] = x->phrases[i];
                    x->offset[i-1] = x->offset[i];
                }
                x->phrNum--;
                return 1;       /* 候補数１ */
            }

            /* 辞書検索カーソルリストから、最高頻度の候補を検索 */
            svofs = 0;
            pl = x->jwork->list;        /* リスト先頭アドレス取得 */
            empty = 1;
            maxeval = NJC_PHRASE_COST;
            for (i = 0; i < x->jwork->listNum; i++, pl++) {
                if (pl->status == 0) {
                    /* 全ての候補を引き尽くしたカーソル */
                    continue;
                }
                if (empty || (maxeval < RLT_GETSCORE_CAND(&pl->rlt[pl->rltcnt - 1]))) {
                    /* 最大頻度と予想される候補格納位置を保存 */
                    maxeval = RLT_GETSCORE_CAND(&pl->rlt[pl->rltcnt - 1]);
                    svofs = i;
                    empty = 0;
                }
            }
            if (empty) {
                /* 辞書候補からの全候補作成処理完了 */
                continue;
            }

            /* 付属語バッファ内から接続する付属語を検索 */
            fzk = x->fzk_phrs;
            for (i = 0; i < x->fzkPhrNum; i++, fzk++) {
                ylen = FZK_GET_TLEN(fzk) - FZK_GET_YLEN(fzk);/* 自立語読み長取得 */
                /* ylen と辞書検索カーソルリストの自立語読み長が一致するかチェック */
                rlt = x->jwork->list[svofs].rlt;
                cont_num = x->jwork->list[svofs].rltcnt;
                cont_len = 0;
                for (j = 0; j < cont_num; j++, rlt++) {
                    cont_len += NJ_GET_YLEN_FROM_STEM_EXT(&rlt->word);
                    if (cont_len == ylen) {
                        /* 包含関係また完全一致 */
                        break;
                    }
                }
                if (cont_len != ylen) {
                    /* 一致しない */
                    continue;
                }

                /* 連続文節の結合数を保存 */
                cont_num = (NJ_UINT8)(j + 1);

                /* 品詞接続するかチェックする */
                fpos = FZK_GET_FPOS(fzk);
                if (fpos == 0) {
                    /* 付属語が無い場合は、V2接続するかチェック */
                    if (!connect(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], &x->hinsi, NJ_GET_BPOS_FROM_STEM(&rlt->word), x->hinsi.f_v2)) {
                        /* 接続しない */
                        continue;
                    }
                } else {
                    /* 付属語が有る場合は、その付属語に接続する自立語を取得 */
                    if (!connect(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], &x->hinsi, NJ_GET_BPOS_FROM_STEM(&rlt->word), fpos)) {
                        /* 接続しない */
                        continue;
                    }
                }

                /* 自立語バッファに登録する */
                cand = &(x->phrases[x->phrNum]);
                rlt = x->jwork->list[svofs].rlt;
                /* 自立語のoperation_idをつける(ただし、NJ_OP_CONVERTとする) */
                cand->operation_id = (rlt->operation_id & ~NJ_OP_MASK) | NJ_OP_CONVERT;
                cand->word.yomi = x->yomi;
                cand->word.stem = rlt->word.stem;       /* 辞書引き結果(自立語)の取得 */
                cand->word.stem.loc.current_info = (cont_num << 4) & 0xF0; /* 連続文節数の更新 */
                NJ_SET_YLEN_TO_STEM(&cand->word, ylen);
                NJ_SET_FREQ_TO_STEM(&cand->word, NJ_GET_FREQ_FROM_STEM(&rlt->word));
                NJ_SET_YLEN_TO_FZK(&cand->word, FZK_GET_YLEN(fzk));
                NJ_SET_KLEN_TO_FZK(&cand->word, FZK_GET_YLEN(fzk));
                NJ_SET_FPOS_TO_FZK(&cand->word, FZK_GET_FPOS(fzk));
                NJ_SET_BPOS_TO_FZK(&cand->word, FZK_GET_BPOS(fzk));
                NJ_SET_FREQ_TO_FZK(&cand->word, FZK_GET_FREQ(fzk));

                /* 連続文節を結合する */
                cont_len = 0;
                cont_num--;
                while (cont_num--) {
                    ++rlt;
                    cont_len += NJ_GET_KLEN_FROM_STEM(&rlt->word);
                    if (NJ_GET_FREQ_FROM_STEM(&cand->word) < NJ_GET_FREQ_FROM_STEM(&rlt->word)) {
                        NJ_SET_FREQ_TO_STEM(&cand->word, NJ_GET_FREQ_FROM_STEM(&rlt->word));
                    }
                }
                NJ_SET_KLEN_TO_STEM(&cand->word, NJ_GET_KLEN_FROM_STEM(&cand->word) + cont_len);
                NJ_SET_BPOS_TO_STEM(&cand->word, NJ_GET_BPOS_FROM_STEM(&rlt->word));

                /* 表記長チェック */
                if (RLT_GETREPRLEN(cand) > NJ_MAX_RESULT_LEN) {
                    continue;
                }

                if (iwnn->option_data.phase2_filter != NULL) {
                    /* 候補フィルタリングが設定されている場合のみ動作 */
                    ph2_filter_message.result = cand;
                    ph2_filter_message.option = iwnn->option_data.phase2_option;
                    /* 関数ポインタの呼出 */
                    phase2_filter_if = (NJ_PHASE2_FILTER_IF)(iwnn->option_data.phase2_filter);
                    if ((*phase2_filter_if)(iwnn, &ph2_filter_message) == 0 ) {
                        continue;
                    }
                }
                /* 文節配列への組み込み */
                if (cand_replacePhrase(iwnn, x, 0, &newID) > 0) {
                    entry++;    /* 全候補候補数＋１ */

                    if (ret_result != NULL) {
                        if (FZK_GET_FREQ(fzk) < 0) {
                            x->cond.low_prio_offset++;
                        } else if (newID >= x->phrNum - x->cond.low_prio_offset) {
                            x->cond.low_prio_offset = (x->phrNum-1) - newID;
                        }
                    }
                }
            }
            /* ここまでの処理で使用した自立語候補は不要となるので破棄する */
            x->jwork->list[svofs].rltcnt = 0;
            /* 破棄した候補を取得した辞書検索カーソルから次候補を取得する */
            pl = &x->jwork->list[svofs];
            while ((ret = njd_get_word(iwnn, pl->cur, &result, 1)) > 0) {
                /* 先頭文節 保存 */
                pl->rlt[0] = result;
                /* 候補文字列長を取得する */
                cont_len = NJ_GET_KLEN_FROM_STEM(&result.word);
                if (cont_len > NJ_MAX_RESULT_LEN) {
                    /* 最大出力候補長 超過 */
                    continue;
                }

                for (j = 0; j < NJ_MAX_DIC; j++) {
                    if (x->cond.ds->dic[j].handle == result.word.stem.loc.handle) {
                        dic_freq = x->cond.ds->dic[j].dic_freq[NJ_MODE_TYPE_HENKAN];
                        break;
                    }
                }

                /* 連続文節数を取得する */
                pl->rltcnt = (NJ_UINT8)(result.word.stem.loc.current_info >> 4) & 0x0F;
                /* 連続文節を取得する */
                for (cont_num = 1; cont_num < pl->rltcnt; cont_num++) {
                    ret0 = njd_get_relational_word(iwnn, &result, &next, &dic_freq);
                    if (ret0 < 0) {
                        return ret0; /*NCH*/
                    }
                    cont_len += NJ_GET_KLEN_FROM_STEM(&next.word);
                    if (cont_len > NJ_MAX_RESULT_LEN) {
                        /* 連続した場合、最大表示出力長を超えないところまでの繋がり文節を取得 */
                        pl->rltcnt = cont_num;
                        /* 結合文節数の更新 */
                        pl->rlt[0].word.stem.loc.current_info = (cont_num << 4) & 0xF0;
                        break;
                    }
                    /* 文節情報 保存 */
                    pl->rlt[cont_num] = next;
                    result = next;
                }
                if (cont_num == pl->rltcnt) {
                    /* 最後まで、連続文節の取得が完了 */
                    break; /*NCH_FB*/
                }
            }
            if (ret < 0) {
                /* 単語検索処理で異常発生 */
                return ret; /*NCH*/
            }
            if (ret == 0) {
                /* 有効な検索語が存在しなかった */
                pl->status = 0;
            }
        }
        x->cond.mode = ZEN_MODE_PRE_PROC;       /* 全候補解析 疑似候補検索状態へ */
    }

    /*
     * 辞書検索カーソルリストを作成する
     */
    if (x->cond.mode == ZEN_MODE_PRE_PROC) {    /* 全候補解析 辞書検索 */
        pl = x->jwork->list;    /* リスト先頭アドレス取得 */
        x->jwork->listNum = 0;  /* リスト登録数初期化 */

        /* 後品詞：文頭 に接続する前品詞情報を取得 */
        njd_r_get_connect(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], x->hinsi.r_bunto, 0, &hinsi);

        fzk = x->fzk_phrs;
        for (i = 0; i < x->fzkPhrNum; i++, fzk++) {
            ylen = FZK_GET_TLEN(fzk) - FZK_GET_YLEN(fzk);/* 自立語読み長取得 */
            fpos = FZK_GET_FPOS(fzk);

            /* 辞書検索カーソルの初期化 */
            /****************************
             *  形態素解析で全候補取得を使う場合は、operation を変更する必要あり。
             ****************************/
            pl->cur->cond.operation = NJ_CUR_OP_COMP;
            pl->cur->cond.mode = NJ_CUR_MODE_FREQ;
            pl->cur->cond.ds = &x->jds;         /* 学習辞書以外 */
            pl->cur->cond.hinsi.fore = hinsi;
            pl->cur->cond.hinsi.foreSize = x->hinsi.fcnt;
            pl->cur->cond.hinsi.foreFlag = 0;
            if (fpos == 0) {
                njd_r_get_connect(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], x->hinsi.f_v2, 1, &pl->cur->cond.hinsi.rear);
            } else {
                njd_r_get_connect(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], fpos, 1, &pl->cur->cond.hinsi.rear);
            }
            pl->cur->cond.hinsi.rearSize = x->hinsi.bcnt;
            pl->cur->cond.hinsi.rearFlag = 0;
            pl->cur->cond.hinsi.yominasi_fore = NULL;
            pl->cur->cond.hinsi.prev_bpos = 0;
            pl->cur->cond.yomi = x->yomi;
            pl->cur->cond.ylen = ylen;
            pl->cur->cond.kanji = NULL;
            pl->cur->cond.charset = NULL;
            if (FZK_GET_YLEN(fzk) > 0) {
                /* 付属語が存在する   */
                pl->cur->cond.fzkconnect = 1;
            } else {
                /* 付属語が存在しない */
                pl->cur->cond.fzkconnect = 0;
            }

            /* 単語検索実行 */
            ret = njd_search_word(iwnn, pl->cur, 1, &exit_flag);
            if (ret < 0) {
                /* 単語検索処理で異常発生 */
                return ret; /*NCH*/
            }

            /* 検索語有無設定 */
            pl->status = (ret ? 1 : 0);

            if (pl->status == 0) {
                /* 次カーソル作成へ */
                pl++;
                x->jwork->listNum++;
                continue;
            }

            /* 単語取得実行 */
            while ((ret = njd_get_word(iwnn, pl->cur, &result, 1)) > 0) {
                /* 先頭文節 保存 */
                pl->rlt[0] = result;
                /* 候補文字列長を取得する */
                if (NJ_GET_KLEN_FROM_STEM(&result.word) > NJ_MAX_RESULT_LEN) {
                    /* 最大出力候補長 超過 */
                    continue;   /* 無視する */
                }

                /* 付属語有りの場合は、付属語との接続チェックを行う */
                if (pl->cur->cond.fzkconnect) {
                    if (!connect(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], &x->hinsi,
                                 NJ_GET_BPOS_FROM_STEM(&(result.word)), fpos)) {
                        /* 付属語が存在し、付属語の前品詞と後品詞が接続しない場合は作成しない */
                        continue;
                    }
                }

                /* 全候補処理において複数文節結果を利用しないため、
                 * 強制的に文節数を1にする（現仕様）
                 */
                result.word.stem.loc.current_info = (NJ_UINT8)0x10;
                pl->rltcnt = 1;
                break;  /* 有効な検索語確定 */
            }
            if (ret < 0) {
                /* 単語検索処理で異常発生 */
                return ret; /*NCH*/
            }
            if (ret == 0) {
                /* 有効な検索語が存在しなかった */
                pl->status = 0;
            }

            /* カーソルリスト１レコード作成完了 */
            pl++;
            x->jwork->listNum++;
        }
        x->cond.mode = ZEN_MODE_NORMAL_PROC;    /* 全候補解析 辞書検索状態へ遷移 */
    }

    /*
     * 辞書検索カーソルと付属語バッファの候補組み合わせから
     * 学習辞書以外を対象とした全候補用候補を作成する
     */
    if (x->cond.mode == ZEN_MODE_NORMAL_PROC) { /* 全候補解析 辞書検索 */
        empty = 0;
        while (empty == 0) {
            /*
             * 全候補を１つずつ返す場合
             * 既に、全候補リストに候補が格納してある候補を返す
             */
            if ((ret_result != NULL) && (x->phrNum > x->cond.low_prio_offset)) {
                *ret_result = x->phrases[0];
                /* 全候補リストから、上位に返す候補を削除 */
                for (i = 1; i < x->phrNum; i++) {
                    x->phrases[i-1] = x->phrases[i];
                    x->offset[i-1] = x->offset[i];
                }
                x->phrNum--;
                return 1;       /* 候補数１ */
            }

            /* 辞書検索カーソルリストから、最高頻度の候補を検索 */
            svofs = 0;
            pl = x->jwork->list;        /* リスト先頭アドレス取得 */
            empty = 1;
            maxeval = NJC_PHRASE_COST;
            for (i = 0; i < x->jwork->listNum; i++, pl++) {
                if (pl->status == 0) {
                    /* 全ての候補を引き尽くしたカーソル */
                    continue;
                }
                if (empty || (maxeval < RLT_GETSCORE_CAND(&pl->rlt[pl->rltcnt - 1]))) {
                    /* 最大頻度と予想される候補格納位置を保存 */
                    maxeval = RLT_GETSCORE_CAND(&pl->rlt[pl->rltcnt - 1]);
                    svofs = i;
                    empty = 0;
                }
            }
            if (empty) {
                /* 辞書候補からの全候補作成処理完了 */
                continue;
            }

            /* 付属語バッファ内から接続する付属語を検索 */
            fzk = &x->fzk_phrs[svofs];
            /* 自立語バッファに登録する */
            cand = &(x->phrases[x->phrNum]);
            rlt = x->jwork->list[svofs].rlt;
            /* 自立語のoperation_idをつける(ただし、NJ_OP_CONVERTとする) */
            cand->operation_id = (rlt->operation_id & ~NJ_OP_MASK) | NJ_OP_CONVERT;
            cand->word.yomi = x->yomi;
            cand->word.stem = rlt->word.stem;   /* 辞書引き結果(自立語)の取得 */
            NJ_SET_YLEN_TO_FZK(&cand->word, FZK_GET_YLEN(fzk));
            NJ_SET_KLEN_TO_FZK(&cand->word, FZK_GET_YLEN(fzk));
            NJ_SET_FPOS_TO_FZK(&cand->word, FZK_GET_FPOS(fzk));
            NJ_SET_BPOS_TO_FZK(&cand->word, FZK_GET_BPOS(fzk));
            NJ_SET_FREQ_TO_FZK(&cand->word, FZK_GET_FREQ(fzk));

            /* 表記長を超えていない場合、候補として確定する */
            if (RLT_GETREPRLEN(cand) <= NJ_MAX_RESULT_LEN) {
                if (iwnn->option_data.phase2_filter != NULL) {
                    /* 候補フィルタリングが設定されている場合のみ動作 */
                    ph2_filter_message.result = cand;
                    ph2_filter_message.option = iwnn->option_data.phase2_option;
                    /* 関数ポインタの呼出 */
                    phase2_filter_if = (NJ_PHASE2_FILTER_IF)(iwnn->option_data.phase2_filter);
                    if ((*phase2_filter_if)(iwnn, &ph2_filter_message) != 0 ) {
                        /* 文節配列への組み込み */
                        if (cand_replacePhrase(iwnn, x, 0, &newID) > 0) {
                            entry++;    /* 全候補候補数＋１ */

                            if (ret_result != NULL) {
                                if (FZK_GET_FREQ(fzk) < 0) {
                                    x->cond.low_prio_offset++;
                                } else if (newID >= x->phrNum - x->cond.low_prio_offset) {
                                    x->cond.low_prio_offset = (x->phrNum-1) - newID;
                                }
                            }
                        }
                    }
                } else {
                    /* 文節配列への組み込み */
                    if (cand_replacePhrase(iwnn, x, 0, &newID) > 0) {
                        entry++;    /* 全候補候補数＋１ */

                        if (ret_result != NULL) {
                            if (FZK_GET_FREQ(fzk) < 0) {
                                x->cond.low_prio_offset++;
                            } else if (newID >= x->phrNum - x->cond.low_prio_offset) {
                                x->cond.low_prio_offset = (x->phrNum-1) - newID;
                            }
                        }
                    }
                }
            }

            /* ここまでの処理で使用した自立語候補は不要となるので破棄する */
            x->jwork->list[svofs].rltcnt = 0;
            /* 破棄した候補を取得した辞書検索カーソルから次候補を取得する */
            pl = &x->jwork->list[svofs];
            /* 単語取得実行 */
            while ((ret = njd_get_word(iwnn, pl->cur, &result, 1)) > 0) {
                /* 先頭文節 保存 */
                pl->rlt[0] = result;
                /* 候補文字列長を取得する */
                if (NJ_GET_KLEN_FROM_STEM(&result.word) > NJ_MAX_RESULT_LEN) {
                    /* 最大出力候補長 超過 */
                    continue;   /* 無視する */
                }
                /* 付属語有りの場合は、付属語との接続チェックを行う */
                if (pl->cur->cond.fzkconnect) {
                    if (!connect(x->cond.ds->rHandle[NJ_MODE_TYPE_HENKAN], &x->hinsi,
                                 NJ_GET_BPOS_FROM_STEM(&(result.word)), FZK_GET_FPOS(fzk))) {
                        /* 付属語が存在し、付属語の前品詞と後品詞が接続しない場合は作成しない */
                        continue;
                    }
                }
                /* 全候補処理において複数文節結果を利用しないため、
                 * 強制的に文節数を1にする（現仕様）
                 */
                result.word.stem.loc.current_info = (NJ_UINT8)0x10;
                pl->rltcnt = 1;
                break;  /* 有効な検索語確定 */
            }
            if (ret < 0) {
                /* 単語検索処理で異常発生 */
                return ret; /*NCH*/
            }
            if (ret == 0) {
                /* 有効な検索語が存在しなかった */
                pl->status = 0;
            }
        }
        x->cond.mode = ZEN_MODE_END;      /* 全候補解析 完了状態へ */
    }

    if ((ret_result != NULL) && (x->phrNum > 0)) {
        *ret_result = x->phrases[0];
        /* 全候補リストから、上位に返す候補を削除 */
        for (i = 1; i < x->phrNum; i++) {
            x->phrases[i-1] = x->phrases[i];
            x->offset[i-1] = x->offset[i];
        }
        x->phrNum--;
        return 1;       /* 候補数１ */
    }

    return entry;       /* 全候補数 */
}


/**
 * ２文節解析固定で、連文節変換を行う。
 *
 * 先頭文節の解析バッファの内容を評価の高い順に返す。
 *
 * @param[in]  iwnn     解析情報クラス
 * @param[in]  dics       連文節変換の対象となる辞書セット
 * @param[in]  yomi       連文節変換対象読み文字列(NUL止め)
 * @param[out] results    処理結果格納バッファ<br>
 *                        領域を呼出元で用意すること
 * @param[in]  size       処理結果取得候補数(resultsの個数)
 *
 * @retval >=0 resultsへ格納した処理結果数(=文節数)
 * @retval <0 負数
 */
NJ_INT16 njc_top_conv(NJ_CLASS *iwnn, NJ_DIC_SET *dics, NJ_CHAR  *yomi,
                      NJ_RESULT *results, NJ_UINT16 size) {

    NJ_INT16 ret;
    NJ_UINT16 i;
    NJC_WORK_SENTENCE *p;
    NJC_CONV *convBuf;
    NJC_SENTENCE *convResult;


    /*
     * 引数チェック
     */
    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_TOP_CONV, NJ_ERR_PARAM_ENV_NULL); /*NCH_FB*/
    }
    if (dics == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_TOP_CONV, NJ_ERR_PARAM_DIC_NULL); /*NCH_FB*/
    }
    if (yomi == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_TOP_CONV, NJ_ERR_PARAM_YOMI_NULL); /*NCH_FB*/
    } else {
        if (nj_strlen(yomi) > NJ_MAX_LEN) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJC_TOP_CONV, NJ_ERR_YOMI_TOO_LONG); /*NCH_FB*/
        }
    }
    if (results == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_TOP_CONV, NJ_ERR_PARAM_RESULT_NULL); /*NCH_FB*/
    }

    convResult = &(iwnn->m_ConvResult); /* 変換結果格納領域設定 */
    sent_init(convResult);
    convResult->phrMax = size;
    convResult->phrases = results;

    /* 連文節変換処理ワーク 初期化 */
    convBuf = &(iwnn->m_Buf.Conv); /* 変換バッファ設定 */
    conv_init(convBuf);

    convResult->yomi = yomi;
    convBuf->cond.level = 2; /* 2文節解析 */
    convBuf->cond.top_conv = 1;
    convBuf->cond.ds = dics;
    convBuf->maxRltLen = NJ_MAX_RESULT_LEN;

    ret = conv_multiConv(iwnn, convBuf, convResult);
    if (ret < 0) {
        return ret; /*NCH_FB*/
    }
    
    p = iwnn->m_Buf.Conv.sentence;
    for (i = 0; (i < (NJ_UINT16)iwnn->m_Buf.Conv.sentNum) && (i < size); i++, p++) {
        *results++ = p->phrases[0];
    }

    return (NJ_INT16)i;
}


/**
 * 保持データをクリアする
 *
 * @param[out] iwnn  解析情報クラス
 *
 * @return 必ず0
 */
NJ_UINT8 njc_init_conv(NJ_CLASS *iwnn) {


    iwnn->njc_mode = NJC_MODE_NOT_CONVERTED;
    return 0;
}


/**
 * 読み文字列を取得する
 *
 * @param[in]    iwnn     解析情報クラス
 * @param[in]    result   変換結果
 * @param[out]   stroke   読み文字列格納バッファ
 * @param[in]    size     strokeバイトサイズ
 *
 * @retval >=0 読み文字配列長
 * @retval <0  エラー
 */
NJ_INT16 njc_get_stroke(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_CHAR  *stroke, NJ_UINT16 size) {
    NJ_UINT16 len1, len2;
    NJ_INT16 ret, ret0;
    NJ_UINT8 cont_num;
    NJ_RESULT now, next;
    NJ_DIC_FREQ dic_freq;
    NJ_PROGRAM_DIC_IF program_dic_operation;
    NJ_PROGRAM_DIC_MESSAGE prog_msg;
    NJ_INT16 ret_len;


    /*
     * 引数チェック
     */
    len1 = RLT_GETYOMILEN(result);
    if (NJ_GET_RESULT_OP(result->operation_id) == NJ_OP_MORPHOLIZE) {
        if (len1 > MM_MAX_MORPHO_LEN) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_STROKE, NJ_ERR_YOMI_TOO_LONG); /*NCH_FB*/
        }
    } else {
        if (len1 > NJ_MAX_LEN) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_STROKE, NJ_ERR_YOMI_TOO_LONG);
        }
    }
    if (len1 == 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_STROKE, NJ_ERR_INVALID_RESULT);
    }

    if (size < ((len1 + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_STROKE, NJ_ERR_BUFFER_NOT_ENOUGH);
    }

    dic_freq.high = 0;
    dic_freq.base = 0;

    if (NJ_GET_YLEN_FROM_STEM_EXT(&result->word) > 0) {  /* 自立語が存在する場合 */
        if ((NJ_GET_RESULT_OP(result->operation_id) == NJ_OP_MORPHOLIZE) ||
            (NJ_GET_TYPE_FROM_STEM(&result->word) == NJ_TYPE_HIRAGANA)) {
            /* 逆引き、擬似候補 */
            len1 = NJ_GET_YLEN_FROM_STEM_EXT(&result->word);
            nj_strncpy(stroke, result->word.yomi, len1);
            *(stroke + len1) = NJ_CHAR_NUL;
            ret = len1;
        } else if (NJ_GET_TYPE_FROM_STEM(&result->word) == NJ_TYPE_UNDEFINE) {
            /*
             * 複数文節情報から１文節(result)に合成している場合は、
             * それぞれの候補文字列を取り出し、連結する。
             */
            ret = njd_get_stroke(iwnn, result, stroke, size);
            if (ret < 0) {
                return ret; /*NCH_FB*/
            }

            len1 = nj_strlen(stroke);

            cont_num = (NJ_UINT8)(result->word.stem.loc.current_info >> 4) & 0x0F;
            if (cont_num > 1) {
                now = *result;
                while (--cont_num > 0) {
                    ret0 = njd_get_relational_word(iwnn, &now, &next, &dic_freq);
                    if (ret0 < 0) {
                        return ret0; /*NCH_FB*/
                    }

                    ret0 = njd_get_stroke(iwnn, &next, stroke + len1, (NJ_UINT16)(size - len1 * sizeof(NJ_CHAR)));
                    if (ret0 < 0) {
                        return ret0; /*NCH_FB*/
                    }
                    /*
                     * ２回目のnjd_get_stroke()の戻り値は
                     * 実際の格納した表記文字列長が返される。
                     */
                    len1 += ret0;
                    now = next;
                }
            }
            switch (GET_LOCATION_OPERATION(result->word.stem.loc.status)) {
            case NJ_CUR_OP_COMP_EXT:
            case NJ_CUR_OP_FORE_EXT:
                /*
                 * 拡張入力検索の場合、retを更新せずにそのまま抜ける
                 * njd_get_stroke()で取得した読み文字数をそのまま使用したい為。
                 */
                break;
            default:
                ret = NJ_GET_YLEN_FROM_STEM_EXT(&result->word);
            }
        } else {
            /* 擬似辞書呼出*/
            /* 関数ポインタの設定 */
            program_dic_operation = (NJ_PROGRAM_DIC_IF)(result->word.stem.loc.handle);
            /* 擬似辞書メッセージを設定 */
            njd_init_program_dic_message(&prog_msg);
            prog_msg.word = &result->word;
            prog_msg.stroke = stroke;
            prog_msg.stroke_size = size;
            ret = (*program_dic_operation)(iwnn, NJG_OP_GET_STROKE, &prog_msg);
            if (ret < 0) {
                return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, GET_ERR_FUNCVAL(ret)); /*NCH_FB*/
            }
        }
    } else {
        /* 自立語が存在しない＝付属語のみで構成された文節 */
        ret = 0;
    }

    /* 付属語については、読み文字列を使用する */
    len1 = NJ_GET_YLEN_FROM_STEM_EXT(&result->word);
    len2 = NJ_GET_YLEN_FROM_FZK(&result->word);
    if (len2 > 0) { /* 付属語が存在 */
        nj_strncpy(stroke + ret, result->word.yomi + len1, len2);
    }
    *(stroke + ret + len2) = NJ_CHAR_NUL;

    /* 読み文字配列長を設定 */
    switch (GET_LOCATION_OPERATION(result->word.stem.loc.status)) {
    case NJ_CUR_OP_COMP_EXT:
    case NJ_CUR_OP_FORE_EXT:
        /* 自立語と付属語の読み文字配列長を設定 */
        ret_len = ret + len2;
        break;
    default:
        ret_len = (NJ_INT16)(RLT_GETYOMILEN(result));
    }

    return ret_len;
}


/**
 * 候補文字列を取得する
 *
 * @param[in]   iwnn    解析情報クラス
 * @param[in]   result    変換結果
 * @param[out]  candidate 候補文字列格納バッファ
 * @param[in]   size      candidateバイトサイズ
 *
 * @retval >=0 候補文字配列長
 * @retval <0  エラー
 */
NJ_INT16 njc_get_candidate(NJ_CLASS *iwnn, NJ_RESULT *result,
                           NJ_CHAR  *candidate, NJ_UINT16 size) {

    NJ_UINT16 len1, len2;
    NJ_INT16 ret, ret0;
    NJ_UINT8 cont_num;
    NJ_RESULT now, next;
    NJ_DIC_FREQ dic_freq;
    NJ_PROGRAM_DIC_IF program_dic_operation;
    NJ_PROGRAM_DIC_MESSAGE prog_msg;


    /*
     * 引数チェック
     */
    len1 = RLT_GETREPRLEN(result);
    if (len1 > NJ_MAX_RESULT_LEN) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_CANDIDATE, NJ_ERR_CANDIDATE_TOO_LONG);
    }
    if (len1 == 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_CANDIDATE, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
    }

    if (size < ((len1 + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJC_GET_CANDIDATE, NJ_ERR_BUFFER_NOT_ENOUGH);
    }


    dic_freq.high = 0;
    dic_freq.base = 0;

    if (NJ_GET_KLEN_FROM_STEM(&result->word) > 0) { /* 自立語が存在する場合 */
        if (NJ_GET_TYPE_FROM_STEM(&result->word) == NJ_TYPE_UNDEFINE) {
            /* 複数文節情報から１文節(result)に合成している場合は、 */
            /* それぞれの候補文字列を取り出し、連結する。               */

            ret = njd_get_candidate(iwnn, result, candidate, size);
            if (ret < 0) {
                return ret; /*NCH_FB*/
            }

            len1 = nj_strlen(candidate);

            cont_num = (NJ_UINT8)(result->word.stem.loc.current_info >> 4) & 0x0F;
            if (cont_num > 1) {
                now = *result;
                while (--cont_num > 0) {
                    ret0 = njd_get_relational_word(iwnn, &now, &next, &dic_freq);
                    if (ret0 < 0) {
                        return ret0; /*NCH_FB*/
                    }
                    
                    ret0 = njd_get_candidate(iwnn, &next, candidate + len1, (NJ_UINT16)(size - len1*sizeof(NJ_CHAR)));
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
            ret = NJ_GET_KLEN_FROM_STEM(&result->word);
        } else if (NJ_GET_TYPE_FROM_STEM(&result->word) == NJ_TYPE_HIRAGANA) {
            /* 擬似候補 */
            len1 = NJ_GET_KLEN_FROM_STEM(&result->word);
            nj_strncpy(candidate, result->word.yomi, len1);
            *(candidate + len1) = NJ_CHAR_NUL;
            ret = len1;
        } else {
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
        }
    } else {
        /* 自立語が存在しない＝付属語のみで構成された文節 */
        ret = 0;
    }

    /* 付属語については、読み文字列を使用する */
    len1 = NJ_GET_YLEN_FROM_STEM_EXT(&result->word);
    len2 = NJ_GET_YLEN_FROM_FZK(&result->word);
    if (len2 > 0) { /* 付属語が存在 */
        nj_strncpy(candidate + ret, result->word.yomi + len1, len2);
    }
    *(candidate + ret + len2) = NJ_CHAR_NUL;
    return (NJ_INT16)(RLT_GETREPRLEN(result));
}
