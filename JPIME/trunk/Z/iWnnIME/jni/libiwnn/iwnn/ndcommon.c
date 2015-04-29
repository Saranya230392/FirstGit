/**
 * @file
 *   辞書アダプタ：共通関数
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
/*              extern  宣  言                  */
/************************************************/

/************************************************/
/*              define  宣  言                  */
/************************************************/
/** njd_search_charset_rangeでのループリミッター (無限ループ防止) */
#define NJX_BINARY_SEARCH_LOOP_LIMIT  2000

/************************************************/
/*              prototype  宣  言               */
/************************************************/


/**
 * 指定された品詞条件に合致（接続可能）するか検査する
 *
 * @param[in]  con     前・後品詞条件
 * @param[in]  hinsiF  接続チェックする前品詞番号
 * @param[in]  hinsiR  接続チェックする後品詞番号
 *
 * @retval  1  接続
 * @retval  0  非接続
 */
NJ_INT16 njd_connect_test(NJ_SEARCH_CONDITION *con, NJ_UINT16 hinsiF, NJ_UINT16 hinsiR) {

    /* 前品詞チェック */
    if (con->hinsi.fore != NULL) {
        if (hinsiF == 0) {
            return 0; /*NCH_FB*/
        }

        hinsiF--;
        if (hinsiF >= con->hinsi.foreSize) {
            return 0; /*NCH_FB*/
        }
        if (*(con->hinsi.fore + (hinsiF / 8)) & (0x80 >> (hinsiF % 8))) {
            /* 接続可能の場合 */
            if (con->hinsi.foreFlag != 0) {
                /* 反転比較モードの場合は非接続(0) */
                return 0; /*NCH_FB*/
            }
        } else {
            /* 接続不可の場合 */
            if (con->hinsi.foreFlag == 0) {
                /* 通常比較モードの場合は非接続(0) */
                return 0;
            }
        }
    }

    /* 後品詞チェック */
    if (con->hinsi.rear != NULL) {
        if (hinsiR == 0) {
            return 0; /*NCH_FB*/
        }

        hinsiR--;
        if (hinsiR >= con->hinsi.rearSize) {
            return 0; /*NCH_FB*/
        }
        if (*(con->hinsi.rear + (hinsiR / 8)) & (0x80 >> (hinsiR % 8))) {
            /* 接続可能の場合 */
            if (con->hinsi.rearFlag != 0) {
                /* 反転比較モードの場合は非接続(0) */
                return 0; /*NCH_FB*/
            }
        } else {
            /* 接続不可の場合 */
            if (con->hinsi.rearFlag == 0) {
                /* 通常比較モードの場合は非接続(0) */
                return 0;
            }
        }
    }

    return 1;
}


/**
 * 属性値と状況設定値から頻度のバイアス値(-100〜100)を求める
 *
 * @param[in]  iwnn       解析情報クラス
 * @param[in]  attr_data  属性値
 *
 * @return  バイアス値
 */
NJ_INT32 njd_get_attr_bias(NJ_CLASS *iwnn, NJ_UINT32 attr_data) {
    /* 属性データ用 */
    NJ_UINT8  attr_l;
    NJ_UINT8  attr_m;
    NJ_UINT8  attr_idx;
    NJ_UINT8 *attr_tmp;
    NJ_INT16  state_system;
    NJ_UINT8  bitmask;
    NJ_INT32  bias_max = NJ_STATE_MIN_BIAS - 1;
    NJ_INT32  bias_min = NJ_STATE_MAX_BIAS + 1;

    /* 属性データの最高頻度を算出 */
    attr_tmp = (NJ_UINT8*)&attr_data;
    /* バイト単位でループ処理を行う */
    for (attr_l = 0; attr_l < sizeof(NJ_UINT32); attr_l++, attr_tmp++) {
        if (*attr_tmp == 0x00) {
            /* 0x00の場合は、本バイトはスキップする */
            continue;
        }
        bitmask = 0x80;
        attr_idx = attr_l * 8;
        /* ビット単位でループを行う */
        for (attr_m = 0; (attr_m < 8) && (attr_idx < NJ_MAX_STATE); attr_m++, attr_idx++) {
            state_system = iwnn->state.system[attr_idx];
            if ((state_system != 0) && ((*attr_tmp & bitmask) != 0)) {
                if (bias_max < state_system) {
                    /* 属性データに一致する状況設定の最高バイアス値を求める */
                    bias_max = state_system;
                }
                if (bias_min > state_system) {
                    /* 属性データに一致する状況設定の最高バイアス値を求める */
                    bias_min = state_system;
                }
            }
            bitmask >>= 1;
        }
    }
    if (bias_max < 0) {
        /* max < 0 の場合は 0 にする */
        bias_max = 0;
    }
    if (bias_min > 0) {
        /* min > 0 の場合は 0 にする */
        bias_min = 0;
    }
    return (bias_max + bias_min);
}


/**
 * あいまい文字セットのfromと指定したkeyと一致する範囲を2分検索する。
 *
 * @param[in]   charset   あいまい文字セット
 * @param[in]   key       検索対象key
 * @param[out]  start     検索範囲先頭
 * @param[out]  end       検索範囲末尾
 *
 * @retval      0   見付からなかった
 * @retval      1   見付かった
 */
NJ_INT16 njd_search_charset_range(NJ_CHARSET *charset, NJ_CHAR *key, NJ_UINT16 *start, NJ_UINT16 *end) {

    NJ_UINT16 left;
    NJ_UINT16 right;
    NJ_UINT16 mid = 0;
    NJ_INT16  cmp;
    NJ_INT16  found = 0;
    NJ_UINT16 index  = 0;
    NJ_UINT16 index2 = charset->charset_count - 1;
    NJ_UINT16 loop_count = 0;

    if(charset->charset_count == 0) {
        return 0;
    }

    /* keyと一致する検索範囲先頭を2分検索する */
    right = charset->charset_count - 1;
    left = 0;
    while ((left <= right) && ((++loop_count) <= NJX_BINARY_SEARCH_LOOP_LIMIT)) {
        mid = left + ((right - left) / 2);
        cmp = nj_charncmp(key, charset->from[mid], 1);
        if (cmp < 0) {
            /* この場所より左にある */
            right = mid - 1;
        } else if (cmp > 0) {
            /* この場所より右にある */
            left = mid + 1;
        } else {
            /* 見つかった */
            found = 1;
            index = mid;
            right = mid - 1;
        }
        if (((mid <= 0) && (cmp <= 0)) ||
            ((mid >= (charset->charset_count - 1)) && (cmp > 0))) {
            /* あいまい文字セット先頭より前、末尾より後を検索 */
            break;
        }
    }
    if ( found == 0 ) {
        /* 見つからなかった */
        return 0;
    }

    /* keyと一致する検索範囲末尾を2分検索する */
    left = index;
    right = index2;
    loop_count = 0;
    while ((left <= right) && ((++loop_count) <= NJX_BINARY_SEARCH_LOOP_LIMIT)) {
        mid = left + ((right - left) / 2);
        cmp = nj_charncmp(key, charset->from[mid], 1);
        if (cmp < 0) {
            /* この場所より左にある */
            right = mid - 1;
        } else if (cmp > 0) {
            /* この場所より右にある */
            left = mid + 1; /*NCH*/
        } else {
            /* 見つかった */
            index2 = mid;
            left = mid + 1;
        }
        if (((mid <= index) && (cmp < 0)) ||
            ((mid >= (charset->charset_count - 1)) && (cmp >= 0))) {
            /* 範囲先頭より前又は、末尾より後を検索 */
            break;
        }
    }

    *start = index;
    *end = index2;

    return 1;
}


/**
 * 単語登録情報構造体を初期化する
 *
 * @param[in,out] info : 単語登録情報
 *
 * @retval  1  初期化完了
 * @retval  0  初期化未完了
 */
NJ_INT16 njd_init_word_info(NJ_WORD_INFO *info) {

    /* 引数チェック */
    if (info == NULL) {
        /* 初期化未完了として返す */
        return 0; /*NCH*/
    }

    info->hinsi_group = 0;                /* 品詞グループ   */
    info->yomi[0] = NJ_CHAR_NUL;          /* 登録読み文字列 */
    info->kouho[0] = NJ_CHAR_NUL;         /* 登録候補文字列 */
    info->additional[0] = NJ_CHAR_NUL;    /* 付加情報文字列 */

    /** 自立語情報 */
    info->stem.yomi_len = 0;              /* 読み文字配列長 */
    info->stem.kouho_len = 0;             /* 候補文字配列長 */
    info->stem.hinsi = 0;                 /* 品詞           */
    info->stem.attr = 0;                  /* 属性データ     */
    info->stem.freq = 0;                  /* 頻度           */

    /** 付属語情報 */
    info->fzk.yomi_len = 0;               /* 読み文字配列長 */
    info->fzk.kouho_len = 0;              /* 候補文字配列長 */
    info->fzk.hinsi = 0;                  /* 品詞           */
    info->fzk.freq = 0;                   /* 頻度           */

    info->connect = 0;                    /* 接続フラグ     */

    /* 初期化完了 */
    return 1;
}
