/**
 * @file
 *   同表記チェック処理
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2011 All Rights Reserved.
 */

/************************************************/
/*           インクルードファイル               */
/************************************************/
#include "nj_lib.h"
#include "nj_err.h"
#include "nj_dicif.h"
#include "njfilter.h"
#include "nj_ext.h"

/************************************************/
/*              define  宣  言                  */
/************************************************/

/************************************************/
/*              構 造 体 宣 言                  */
/************************************************/

/************************************************/
/*              prototype  宣  言               */
/************************************************/
/************************************************/
/*              extern  宣  言                  */
/************************************************/

/************************************************/
/*              static  変数宣言                */
/************************************************/

/**
 * 重複表記のチェックおよび同表記バッファへの格納
 *
 * 指定した処理結果を同表記バッファを用いて、同じ表記のものがあるかをチェックする。
 * また、処理結果のオペレーションIDの文字種を指定して返す
 *
 * @attention
 *  本関数での処理後に処理結果(result)のオペレーションIDの文字種が変更される。
 *
 * @param[in,out]  iwnn   解析情報クラス
 * @param[in,out]  result   処理結果
 * @param[in]      yomi_len resultを取得した際に指定した検索読み文字配列長<br>
 *  ０：繋がり検索、読み無し検索は０固定<br>
 *  Ｎ：完全一致検索、前方一致検索時の検索読み文字配列長
 * @param[out]     offset   重複が無い場合には、表記の格納位置。<br>
 *                          重複が有る場合には、重複表記の格納位置。
 *
 * @retval 1 重複候補あり
 * @retval 0 重複候補なし
 * @retval <0 エラー(バッファあふれ)
 */
NJ_INT16 nje_append_homonym(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_UINT16 yomi_len, NJ_INT16 *offset) {
    NJ_INT16 ret, type, len;
    NJ_CHAR *ptr, *p;


    /* バッファあふれチェック */
    if (iwnn->h_buf.workbuf.current >= NJ_MAX_CANDIDATE) {
        /* あふれた */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJE_APPEND_HOMONYM, NJ_ERR_BUFFER_NOT_ENOUGH);
    }

    /* 同表記バッファの未使用領域の先頭を探す */
    ptr = &(iwnn->h_buf.workbuf.work[iwnn->h_buf.workbuf.current * (NJ_MAX_RESULT_LEN + NJ_TERM_LEN)]);

    /* 処理結果から表記を取得する */
    ret = njx_get_candidate(iwnn, result, ptr, (NJ_MAX_RESULT_LEN + NJ_TERM_LEN) * sizeof(NJ_CHAR));
    if (ret < 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJE_APPEND_HOMONYM, NJ_ERR_INVALID_RESULT);
    }

    p = &(iwnn->h_buf.workbuf.work[0]);
    /*
     * retをループ変数に流用し、バッファ内から同表記文字列を
     * 検索する
     */
    for (ret = iwnn->h_buf.workbuf.current; ret > 0; ret--) {
#ifdef NJ_OPT_UTF16
        /*
         * 1文字目を比較することで、不一致項目を高速に省く
         */
        if (*p == *ptr) {
#else  /* NJ_OPT_UTF16 */
        if (*(p + 1) == *(ptr + 1)) {
#endif /* NJ_OPT_UTF16 */
            if (nj_strcmp(p, ptr) == 0) {
                /* 一致した */
                *offset = iwnn->h_buf.workbuf.current - ret;
                return 1;
            }
        }
        p += NJ_MAX_RESULT_LEN + NJ_TERM_LEN;
    }

    *offset = iwnn->h_buf.workbuf.current++;

    /* オペレーションIDの擬似フラグをマスクする */
    result->operation_id &= ~(NJ_TYPE_GIJI_MASK);
    /* オペレーションIDの未使用フラグ部分をマスクする */
    result->operation_id &= ~(NJ_TYPE_UNUSED_MASK);

    if (yomi_len == 0) {
        /*
         * 繋がり検索や読み無し検索で取得した、NJ_RESULTには
         * 以降の擬似候補判定処理は不要
         */
        return 0;
    }

    /* 指定された処理結果から読み文字列を取得する */
    len = NJ_GET_YLEN_FROM_STEM(&result->word) + NJ_GET_YLEN_FROM_FZK(&result->word);

    if (yomi_len != (NJ_UINT16)len) {
        /*
         * 処理結果の読み文字列長と入力文字列長が
         * 一致しなければ、擬似候補ではない。
         */
         return 0;
    }

    type = NJ_GET_TYPE_FROM_STEM(&result->word);
    if ( type != NJ_TYPE_UNDEFINE ) {
        /*
         * typeが、NJ_TYPE_UNDEFINE以外の場合は、
         * 擬似候補があると考える
         */
        result->operation_id |= NJ_TYPE_GIJI_BIT;
    }

    return 0;
}


/**
 * 同表記バッファをクリアする
 *
 * @param[in,out]  iwnn  解析情報クラス
 *
 * @return  常に0を返す
 */
NJ_UINT8 nje_clear_homonym_buf(NJ_CLASS *iwnn) {


    iwnn->h_buf.workbuf.current = 0;

    iwnn->h_buf.yomibuf.current = 0;
    return 0;
}


/**
 * 指定した文字列を同表記バッファへ追加する。
 *
 * @param[in,out] iwnn      解析情報クラス
 * @param[in]     str       追加する文字列
 *
 * @retval 1 重複候補あり
 * @retval 0 重複候補なし
 * @retval <0 エラー(バッファあふれ)
 */
NJ_INT16 nje_append_homonym_string(NJ_CLASS *iwnn, NJ_CHAR *str) {
    NJ_CHAR *p;
    NJ_INT16 i;
    

    /* バッファあふれチェック */
    if (iwnn->h_buf.yomibuf.current >= NJ_MAX_YOMI_STRING) {
        /* あふれた */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJE_APPEND_HOMONYM, NJ_ERR_BUFFER_NOT_ENOUGH);
    }

    p = &(iwnn->h_buf.yomibuf.work[0]);

    /*
     * バッファ内から同表記文字列を検索する
     */
    for (i = 0; i < iwnn->h_buf.yomibuf.current; i++) {
#ifdef NJ_OPT_UTF16
        /*
         * 1文字目を比較することで、不一致項目を高速に省く
         */
        if (*p == *str) {
#else  /* NJ_OPT_UTF16 */
        if (*(p + 1) == *(str + 1)) {
#endif /* NJ_OPT_UTF16 */
            if (nj_strcmp(p, str) == 0) {
                /* 一致した */
                return 1;
            }
        }
        p += NJ_MAX_LEN + NJ_TERM_LEN;
    }

    /* 同表記バッファの未使用領域の先頭を探す */
    p = &(iwnn->h_buf.yomibuf.work[iwnn->h_buf.yomibuf.current * (NJ_MAX_LEN + NJ_TERM_LEN)]);
    /* 同表記バッファへ、指定した文字列をコピーする */
    nj_strcpy(p, str);

    /* 格納数をincrement */
    iwnn->h_buf.yomibuf.current++;
    return 0;
}


/**
 * 指定したindexの位置にある文字列を返す。
 *
 * @param[in,out] iwnn  解析情報クラス
 * @param[in]     index   位置
 *
 * @retval NULL     候補なし(indexが格納量を超えている)
 * @retval NULL以外  文字列の先頭index
 */
NJ_CHAR *nje_get_homonym_string(NJ_CLASS *iwnn, NJ_UINT16 index) {


    /* 範囲チェック */
    if (index >= (NJ_UINT16)iwnn->h_buf.yomibuf.current) {
        /* その位置には候補がない */
        return NULL;
    }

    /* 同表記バッファの指定したindex位置を返す */
    return &(iwnn->h_buf.yomibuf.work[index * (NJ_MAX_LEN + NJ_TERM_LEN)]);
}
