/**
 * @file
 *   辞書アダプタ：拡張読み無し予測辞書アダプタ
 *
 *   拡張読み無し予測辞書へのアクセス関数を提供する
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
#define WORD_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x28)))
#define YOMI_HYOKI_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x2C)))
#define BPOS_IDX_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x30)))
#define SEARCH_IDX_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x34)))
#define ZOKUSEI_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x38)))
#define ZOKUSEI_AREA_LEN(h) ((NJ_INT32)(NJ_INT32_READ((h) + 0x3C)))
#define HINSI_NO_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x40)))
#define FHINSI_NO_CNT(h) ((NJ_INT16)(NJ_INT16_READ((h) + 0x44)))
#define FHINSI_NO_BYTE(h) ((NJ_INT8)(*((h) + 0x46)))
#define BIT_FHINSI(h) ((NJ_INT8)(*((h) + 0x47)))
#define BHINSI_NO_CNT(h) ((NJ_INT16)(NJ_INT16_READ((h) + 0x48)))
#define BHINSI_NO_BYTE(h) ((NJ_INT8)(*((h) + 0x4A)))
#define BIT_BHINSI(h) ((NJ_INT8)(*((h) + 0x4B)))
#define REAL_ZOKUSEI_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x4C)))
#define REAL_ZOKUSEI_AREA_CNT(h) ((NJ_INT32)(NJ_INT32_READ((h) + 0x50)))
#define REAL_ZOKUSEI_AREA_LEN(h) ((NJ_INT32)(NJ_INT32_READ((h) + 0x54)))

#define WORD_AREA_CNT(h) ((NJ_UINT32)(NJ_INT32_READ((h) + 0x1C)))
#define BPOS_IDX_AREA_CNT(h) ((NJ_INT32)(NJ_INT32_READ((h) + 0x20)))
#define SEARCH_IDX_AREA_CNT(h) ((NJ_INT32)(NJ_INT32_READ((h) + 0x24)))

#define BPOS_IDX_WORD_NO(h,x) ((NJ_UINT32)(NJ_INT32_READ((h) + (NJ_UINT32)(((x) - 1) * 0x04))))
#define SEARCH_IDX_START(x) ((NJ_UINT8)(0x80 & (*(x))))
#define SEARCH_IDX_WORD_NO(x) ((NJ_UINT32)(0x00FFFFFF & NJ_INT32_READ(x)))
#define SEARCH_IDX_HINDO(x) ((NJ_UINT8)(0x7F & (*(x))))
#define SEARCH_IDX_ADDR(h,x) (((h) + (NJ_UINT32)((x) * 0x04)))
#define WORD_IDX_ADDR(h,x) (((h) + (NJ_UINT32)(((x) - 1) * 0x0A)))

#define HINSI_DATA(h) (NJ_INT16_READ((h) + 0x08))
#define YOMI_LEN(h)  ((NJ_UINT8)((0x7F & *(h))/sizeof(NJ_CHAR)))
#define HYOKI_LEN(h) ((NJ_UINT8)((0x7F & *((h) + 0x04))/sizeof(NJ_CHAR)))
#define HYOKI_INFO(h) ((NJ_UINT8)(0x80 & *((h) + 0x04)))

#define YOMI_DATA_OFFSET(h) ((NJ_UINT32)(0x00FFFFFF & NJ_INT32_READ(h)))
#define HYOKI_DATA_OFFSET(h) ((NJ_UINT32)(0x00FFFFFF & NJ_INT32_READ((h) + 0x04)))

#define EXT_YOMINASI_DIC_FREQ_DIV 63  /**< 読みなし辞書頻度段階 */

#define ADD_LEARN_FLG(h) ((NJ_UINT32)(NJ_DIC_ADD_LEARN_FLG & NJ_INT32_READ((h) + 0x24)))
#define ADD_MAX_STRING_SIZE(h) ((NJ_UINT16)(NJ_INT32_READ((h) + 0x20)))
#define ADD_WORD_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x10)))
#define ADD_STRING_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x18)))
#define ADD_WORD_DATA_ADDR(h,x) (((h) + (NJ_UINT32)(((x) - 1) * ADD_WORD_DATA_AREA_SIZE)))
#define ADD_STRING_LEN(h) ((NJ_UINT16)(NJ_INT16_READ((h))/sizeof(NJ_CHAR)))
#define ADD_STRING_INFO(h) ((NJ_UINT16)(NJ_INT32_READ((h))))
#define ADD_STRING_DATA_OFFSET(h) ((NJ_UINT32)(NJ_INT32_READ((h) + 0x02)))

/**
 * 検索位置  設定情報
 */
#define CURRENT_INFO_SET       (NJ_UINT8)(0x10)
#define CURRENT_INFO_SET_STATE (NJ_UINT8)(0x90)

/**
 * 状況カテゴリ  設定情報
 */
#define SEARCH_CNT_DIC_VERSION1    (NJ_UINT8)(1)       /* 辞書バージョン Ver.1 の単語データ検索回数 */
#define SEARCH_CNT_DIC_VERSION2    (NJ_UINT8)(2)       /* 辞書バージョン Ver.2 の単語データ検索回数 */


/************************************************/
/*              prototype  宣  言               */
/************************************************/
static NJ_UINT16 search_data(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset);
static NJ_UINT16 search_data_next(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset);
static NJ_INT32  get_hindo(NJ_CLASS *iwnn, NJ_UINT32 current, NJ_INT16  base_hindo, NJ_SEARCH_LOCATION_SET *loctset);
static NJ_UINT32 get_attr(NJ_DIC_HANDLE handle, NJ_UINT32 current);


/**
 * 初回検索を行う
 *
 * 初回検索を行い、検索結果を検索位置(loctset)に設定する
 *
 * @param[in]     iwnn        解析情報クラス
 * @param[in]     condition   検索条件
 * @param[in,out] loctset     検索位置
 *
 * @retval                0   検索候補なし
 * @retval                1   検索候補あり
 */
static NJ_UINT16 search_data(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset) {
    NJ_DIC_HANDLE handle;

    NJ_UINT32 bpos_idx_cnt;
    NJ_UINT8 *bpos_idx_top;
    NJ_UINT32 search_idx_cnt;
    NJ_UINT8 *search_idx_top;
    NJ_UINT16 search_bpos;
    NJ_UINT32 search_idx_no;
    NJ_UINT32 search_cnt;
    NJ_UINT32 current;
    NJ_UINT8  *current_addr;
    NJ_INT32  hindo_max = INIT_HINDO;
    NJ_UINT32 current_max = 0;
    NJ_INT32  hindo_tmp;
    NJ_UINT8  findflg = 0;
    NJ_UINT8 *word_data_top;
    NJ_UINT32 word_data_current_no;
    NJ_UINT8 *word_data_current;

    NJ_UINT32 left;
    NJ_UINT32 right;
    NJ_UINT32 mid;
    NJ_UINT32 save_tmp = 0;
    NJ_CHAR  *yomi = condition->yomi;
    NJ_UINT16 yomi_len = (NJ_UINT16)(condition->ylen);
    NJ_UINT16 cmp_len;
    NJ_UINT8 *dic_yomi;
    NJ_UINT16 dic_yomi_len;
    NJ_INT16  check;

    NJ_UINT8 search_attr, loop_cnt = 0;
    NJ_UINT8 cnt = 1;
    NJ_UINT32 attr;
    NJ_UINT8 *p_attr;
    NJ_UINT8 skip_byte = 0;


    /* 辞書ハンドルを取得 */
    handle = loctset->loct.handle;
    search_bpos = condition->hinsi.prev_bpos;

    /* 後品詞インデックスを検索 */
    bpos_idx_cnt = BPOS_IDX_AREA_CNT(handle);
    bpos_idx_top = BPOS_IDX_TOP_ADDR(handle);

    if (bpos_idx_cnt < search_bpos) {
        /* 検索対象の後品詞がないため、検索終了とする。*/
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }
    /* 検索インデックスの先頭番号を取得 */
    search_idx_no = BPOS_IDX_WORD_NO(bpos_idx_top, search_bpos);
    if (search_idx_no == 0) {
        /* インデックス番号が 0 の場合は、検索対象候補なしのため、検索終了とする */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    /* 検索インデックスを検索 */
    search_idx_cnt = SEARCH_IDX_AREA_CNT(handle);
    search_idx_top = SEARCH_IDX_TOP_ADDR(handle);
    if (search_idx_cnt < search_idx_no) {
        /* 検索インデックスの範囲を超えているため、検索対象候補なしのため、検索終了とする*/
        /* ここは、辞書が壊れている場合のみ、通る */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    /* 検索インデックスの範囲を確定する */
    loctset->loct.top = search_idx_no;
    if (!SEARCH_IDX_START(SEARCH_IDX_ADDR(search_idx_top, (search_idx_no - 1)))) {
        /* 検索インデックスの先頭ではないため、検索終了とする*/
        /* ここは、辞書が壊れている場合のみ、通る */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }
    /* 検索インデックスの先頭は、検索インデックスの個数である。*/
    search_cnt =  SEARCH_IDX_WORD_NO(SEARCH_IDX_ADDR(search_idx_top, (search_idx_no - 1)));
    loctset->loct.bottom = search_idx_no + search_cnt - 1;

    if ((condition->operation == NJ_CUR_OP_FORE) &&
        (condition->ylen > 0)) {
        /* 読み文字列有り検索の場合、検索範囲の絞込を行う */
        word_data_top = WORD_TOP_ADDR(handle);
        left    = loctset->loct.top;
        right   = loctset->loct.bottom;
        findflg = 0;
        while (left <= right) {
            mid = (left + right) / 2;
            word_data_current_no = SEARCH_IDX_WORD_NO(SEARCH_IDX_ADDR(search_idx_top, mid));
            word_data_current = WORD_IDX_ADDR(word_data_top, word_data_current_no);
            
            /* 読み文字列を取得 */
            dic_yomi_len = (NJ_INT16)YOMI_LEN(word_data_current);
            dic_yomi     = YOMI_HYOKI_TOP_ADDR(handle) + (NJ_UINT32)YOMI_DATA_OFFSET(word_data_current);
            
            /* 文字列長が同じ分をまず比較する */
            if (yomi_len >= dic_yomi_len) {
                cmp_len = dic_yomi_len;
            } else {
                cmp_len = yomi_len;
            }
            check = nj_memcmp(dic_yomi, (NJ_UINT8*)yomi, (NJ_UINT16)(cmp_len * sizeof(NJ_CHAR)));
            if (check < 0) {
                /* 入力読みの方が大きい */
                left = mid + 1;
            } else if (check > 0) {
                /* 辞書内の読みの方が文字コードが大きい */
                right = mid - 1;
            } else {
                /* 文字列長が同じ分は一致 */
                if (dic_yomi_len >= yomi_len) {
                    /* 
                     * 検索文字列が前方一致している時のみ
                     * 検出フラグを立てる。
                     */
                    save_tmp = mid;
                    findflg = 1;
                }
                if (right == left) {
                    break;
                }
                if (yomi_len == dic_yomi_len) {
                    /* topを求める */
                    right = mid;
                } else if (yomi_len > dic_yomi_len) {
                    /* 入力読み長の方が長い */
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
            if ((right > loctset->loct.bottom) ||
                (left  < loctset->loct.top)) {
                /* 
                 * 検索範囲を超えて検索しようとしているため、
                 * ここで検索を終了する。
                 */
                break;
            }
        }
        if (findflg == 0) {
            /* 該当範囲に検索対象読み文字列が見つからなかった場合 */
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }
        /* 検索開始位置を補正 */
        loctset->loct.top = save_tmp;

        /*
         * 同読み文字の単語データが複数存在する可能性があるため、
         * 終端まで検索を行う。
         */
        left = save_tmp;
        right= loctset->loct.bottom;
        findflg = 0;
        while (left <= right) {
            mid = (left + right) / 2;
            word_data_current_no = SEARCH_IDX_WORD_NO(SEARCH_IDX_ADDR(search_idx_top, mid));
            word_data_current = WORD_IDX_ADDR(word_data_top, word_data_current_no);
            
            /* 読み文字列を取得 */
            dic_yomi_len = (NJ_INT16)YOMI_LEN(word_data_current);
            dic_yomi     = YOMI_HYOKI_TOP_ADDR(handle) + (NJ_UINT32)YOMI_DATA_OFFSET(word_data_current);
            
            /* 文字列長が同じ分をまず比較する */
            if (yomi_len >= dic_yomi_len) {
                cmp_len = dic_yomi_len;
            } else {
                cmp_len = yomi_len;
            }
            check = nj_memcmp(dic_yomi, (NJ_UINT8*)yomi, (NJ_UINT16)(cmp_len * sizeof(NJ_CHAR)));
            if (check < 0) {
                /* 入力読みの方が大きい */
                left = mid + 1; /*NCH_DEF*/
            } else if (check > 0) {
                /* 辞書内の読みの方が文字コードが大きい */
                right = mid - 1;
            } else {
                /* 文字列長が同じ分は一致 */
                save_tmp = mid;
                findflg = 1;
                if (yomi_len == dic_yomi_len) {
                    /* topを求める */
                    left = mid + 1;
                } else if (yomi_len > dic_yomi_len) {
                    /* 入力読み長の方が長い */
                    right = mid - 1; /*NCH_DEF*/
                } else {
                    left = mid + 1;
                }
            }
        }
        if (findflg == 0) {
            /* 該当範囲に検索対象読み文字列が見つからなかった場合 */
            loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_DEF*/
            return 0; /*NCH_DEF*/
        }
        /* 検索開始位置を補正 */
        loctset->loct.bottom = save_tmp;
    }


    /* 辞書バージョンチェック */
    if (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION2) {
        /* Ver.2 以外の場合 */

        /* 検索範囲から最高頻度を算出する */
        current = loctset->loct.top;
        while (current <= loctset->loct.bottom) {
            current_addr = SEARCH_IDX_ADDR(search_idx_top, current);

            /* 総合頻度を取得する */
            hindo_tmp = get_hindo(iwnn, SEARCH_IDX_WORD_NO(current_addr), SEARCH_IDX_HINDO(current_addr), loctset);

            /* 最高頻度を算出する */
            if (hindo_max < hindo_tmp) {
                hindo_max = hindo_tmp;
                current_max = current;
            }
            current++;
        }

    } else {
        /* Ver.2 の場合 */
        /* 最終文節の前確定情報から状況カテゴリ情報を取得 */
        attr = condition->attr;
        p_attr = (NJ_UINT8 *)&attr;
        if (*p_attr & DEFAULT_STATE_BIT) {
            loop_cnt = SEARCH_CNT_DIC_VERSION1;
        }
        /* カテゴリ先頭から3Byte移動する(指定Bitが25Bit目以降である為) */
        p_attr += CAT_POS_DAILY_LIFE;
        /* 指定の状況カテゴリが設定されているかを確認 */
        if (*p_attr & CHECK_DAILY_LIFE_BIT) {
            /* 指定がある場合、4Byte目の情報を取得 */
            search_attr = *p_attr;
            loop_cnt = SEARCH_CNT_DIC_VERSION2;
            skip_byte = CAT_POS_DAILY_LIFE;
        } else {
            /* 指定がない場合、既存データ情報を指定 */
            search_attr = DEFAULT_STATE_BIT;
            skip_byte = CAT_POS_RESERVE;
        }

        for (cnt = 0; cnt < loop_cnt; cnt++) {

            /* 検索範囲から最高頻度を算出する */
            current = loctset->loct.top;
            while (current <= loctset->loct.bottom) {
                current_addr = SEARCH_IDX_ADDR(search_idx_top, current);

                /* 総合頻度 & カテゴリ情報を取得する */
                hindo_tmp = get_hindo(iwnn, SEARCH_IDX_WORD_NO(current_addr), SEARCH_IDX_HINDO(current_addr), loctset);
                attr = get_attr(loctset->loct.handle, SEARCH_IDX_WORD_NO(current_addr));
                p_attr = (NJ_UINT8 *)&attr;
                /* カテゴリ先頭からカテゴリ確認該当Byte移動する */
                p_attr += skip_byte;

                /* 最高頻度を算出する */
                if ((*p_attr & search_attr) && (hindo_max < hindo_tmp)) {
                    hindo_max = hindo_tmp;
                    current_max = current;
                }
                current++;
            }

            /* 該当データが存在する場合 */
            if (hindo_max != INIT_HINDO) {
                /* 処理を抜ける */
                break;
            } else {
                /* 既存データ情報指定に変更 */
                search_attr = DEFAULT_STATE_BIT;
                skip_byte = CAT_POS_RESERVE;
            }
        }
    }
    /* 該当範囲に検索対象候補が見つからなかった場合 */
    if (hindo_max == INIT_HINDO) {
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    /* 検索候補あり */
    if ((NJ_GET_DIC_VER(loctset->loct.handle) == NJ_DIC_VERSION2) && (skip_byte > 0)) {
        /* 状況カテゴリ検索を指定 */
        loctset->loct.current_info = CURRENT_INFO_SET_STATE;
    } else {
        loctset->loct.current_info = CURRENT_INFO_SET;
    }
    loctset->loct.current = current_max;
    current_addr = SEARCH_IDX_ADDR(search_idx_top, current_max);
    loctset->loct.attr   = get_attr(loctset->loct.handle, SEARCH_IDX_WORD_NO(current_addr));
    loctset->cache_freq = CALCULATE_HINDO(hindo_max, loctset->dic_freq.base, 
                                          loctset->dic_freq.high, EXT_YOMINASI_DIC_FREQ_DIV);
    loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
    return 1;
}


/**
 * 次候補検索を行う
 *
 * 次候補検索を行い、検索結果を検索位置(loctset)に設定する
 *
 * @param[in]     iwnn        解析情報クラス
 * @param[in]     condition   検索条件
 * @param[in,out] loctset     検索位置
 *
 * @retval                0   検索候補なし
 * @retval                1   検索候補あり
 */
static NJ_UINT16 search_data_next(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset) {
    NJ_DIC_HANDLE handle;

    NJ_UINT8 *search_idx_top;
    NJ_UINT32 current;
    NJ_UINT8  *current_addr;
    NJ_INT32  hindo_max = INIT_HINDO;
    NJ_UINT32 current_max = 0;
    NJ_INT32  hindo_tmp;
    NJ_INT32  hindo_current;
    NJ_UINT32 bottom;
    NJ_UINT8  findflg = 0;
    NJ_UINT8  bottomflg;

    NJ_UINT8 search_attr, loop_cnt = 0;
    NJ_UINT8 cnt = 1;
    NJ_UINT32 attr;
    NJ_UINT8 *p_attr;
    NJ_UINT8 skip_byte = 0;


    /* 初回検索時は そのまま抜ける */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        loctset->loct.status = NJ_ST_SEARCH_READY;
        return 1;
    }

    /* 辞書ハンドルを取得 */
    handle = loctset->loct.handle;

    /* 現在位置を取得 */
    current = loctset->loct.current;

    /* 検索インデックスを検索 */
    search_idx_top = SEARCH_IDX_TOP_ADDR(handle);

    /* 現在位置の頻度を取得 */
    current_addr = SEARCH_IDX_ADDR(search_idx_top, current);

    /* 総合頻度を取得する */
    hindo_current = get_hindo(iwnn, SEARCH_IDX_WORD_NO(current_addr),
                              SEARCH_IDX_HINDO(current_addr), loctset);

    bottom = loctset->loct.bottom;
    if (current >= bottom) {
        current = loctset->loct.top; 
    } else {
        current += 1; 
    }

    /* 辞書バージョンチェック */
    if (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION2) {
        /* Ver.2 以外の場合 */
        bottomflg = 0;
        while (current <= bottom) {
            current_addr = SEARCH_IDX_ADDR(search_idx_top, current);

            /* 総合頻度を取得する */
            hindo_tmp = get_hindo(iwnn, SEARCH_IDX_WORD_NO(current_addr), 
                                  SEARCH_IDX_HINDO(current_addr), loctset);

            if ((current > loctset->loct.current) && (hindo_tmp == hindo_current)) {
                /* 検索中頻度と等しく、且つ検索候補がカレント位置以降の場合,
                 * その検索候補位置を返す */
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->loct.current = current;
                loctset->loct.attr   = get_attr(loctset->loct.handle, SEARCH_IDX_WORD_NO(current_addr));
                return 1;
            } else if (hindo_tmp < hindo_current) {
                /* カレントより頻度が低く、その中で最高頻度のものを取得 */
                /* 検索中の最高頻度と同じで、読みが先のものを取得 */
                if ((hindo_max < hindo_tmp) || ((current_max > current) && (hindo_max == hindo_tmp))) {
                    hindo_max = hindo_tmp;
                    current_max = current;
                    findflg = 1;
                }
            } else {}
            current++;
            if ((current > bottom) && (bottomflg == 0)) {
                bottomflg = 1;
                current = loctset->loct.top;
                bottom = loctset->loct.current - 1;
            }
        }

    } else {
        /* Ver.2 の場合 */
        if (loctset->loct.current_info != CURRENT_INFO_SET_STATE) {
            /* 通常情報 */
            search_attr = DEFAULT_STATE_BIT;
            loop_cnt = SEARCH_CNT_DIC_VERSION1;
            skip_byte = CAT_POS_RESERVE;
        } else {
            /* 状況カテゴリ情報 */
            /* 最終文節の前確定情報から状況カテゴリ情報を取得 */
            attr = condition->attr;
            p_attr = (NJ_UINT8 *)&attr;
            if (*p_attr & DEFAULT_STATE_BIT) {
                loop_cnt = SEARCH_CNT_DIC_VERSION1;
            }
            /* カテゴリ先頭から3Byte移動する(指定Bitが25Bit目以降である為) */
            p_attr += CAT_POS_DAILY_LIFE;
            /* 指定がある場合、4Byte目の情報を取得 */
            search_attr = *p_attr;
            loop_cnt += SEARCH_CNT_DIC_VERSION1;
            skip_byte = CAT_POS_DAILY_LIFE;
        }

        for (cnt = 0; cnt < loop_cnt; cnt++) {
            bottomflg = 0;
            while (current <= bottom) {
                current_addr = SEARCH_IDX_ADDR(search_idx_top, current);

                /* 総合頻度を取得する */
                hindo_tmp = get_hindo(iwnn, SEARCH_IDX_WORD_NO(current_addr), 
                                      SEARCH_IDX_HINDO(current_addr), loctset);
                attr = get_attr(loctset->loct.handle, SEARCH_IDX_WORD_NO(current_addr));
                p_attr = (NJ_UINT8 *)&attr;
                /* カテゴリ先頭からカテゴリ確認該当Byte移動する */
                p_attr += skip_byte;
                /* 状況カテゴリが一致 */
                if (*p_attr & search_attr) {
                    if ((current > loctset->loct.current) && (hindo_tmp == hindo_current)) {
                        /* 検索中頻度と等しく、且つ検索候補がカレント位置以降の場合,
                         * その検索候補位置を返す */
                        loctset->loct.status = NJ_ST_SEARCH_READY;
                        if (skip_byte > 0) {
                            loctset->loct.current_info = CURRENT_INFO_SET_STATE;
                        } else {
                            loctset->loct.current_info = CURRENT_INFO_SET;
                        }
                        loctset->loct.current = current;
                        loctset->loct.attr   = get_attr(loctset->loct.handle, SEARCH_IDX_WORD_NO(current_addr));
                        return 1;
                    } else if (hindo_tmp < hindo_current) {
                        /* カレントより頻度が低く、その中で最高頻度のものを取得 */
                        /* 検索中の最高頻度と同じで、読みが先のものを取得 */
                        if ((hindo_max < hindo_tmp) || ((current_max > current) && (hindo_max == hindo_tmp))) {
                            hindo_max = hindo_tmp;
                            current_max = current;
                            findflg = 1;
                        }
                    } else {}
                }
                current++;
                if ((current > bottom) && (bottomflg == 0)) {
                    bottomflg = 1;
                    current = loctset->loct.top;
                    bottom = loctset->loct.current - 1;
                }
            }
            /* 該当データが存在する場合 */
            if (hindo_max != INIT_HINDO) {
                /* 処理を抜ける */
                break;
            } else {
                /* 既存データ情報指定に変更 */
                search_attr = DEFAULT_STATE_BIT;
                skip_byte = CAT_POS_RESERVE;
                current = loctset->loct.top;
                bottom = loctset->loct.bottom;
                hindo_current = NJ_NUM_THOUSAND + 1;
            }
        }
    }
    if (findflg == 0) {
        /* 検索終了の属性を設定 */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    /* 検索候補あり */
    if ((NJ_GET_DIC_VER(loctset->loct.handle) == NJ_DIC_VERSION2) && (skip_byte > 0)) {
        /* 状況カテゴリ検索を指定 */
        loctset->loct.current_info = CURRENT_INFO_SET_STATE;
    } else {
        loctset->loct.current_info = CURRENT_INFO_SET;
    }
    loctset->loct.current = current_max;
    loctset->loct.status = NJ_ST_SEARCH_READY;
    current_addr = SEARCH_IDX_ADDR(search_idx_top, current_max);
    loctset->loct.attr   = get_attr(loctset->loct.handle, SEARCH_IDX_WORD_NO(current_addr));
    loctset->cache_freq = CALCULATE_HINDO(hindo_max, loctset->dic_freq.base, 
                                          loctset->dic_freq.high, EXT_YOMINASI_DIC_FREQ_DIV);
    loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
    return 1;
}

/************************************************/
/* Global関数                                   */
/************************************************/
/**
 * 拡張読み無し予測辞書アダプタ  単語検索
 *
 * @param[in,out]  iwnn     解析情報
 * @param[in]      con      検索条件
 * @param[in,out]  loctset  検索位置
 * @param[in]      dic_idx  辞書登録番号
 *
 * @retval               1  検索候補あり
 * @retval               0  検索候補なし
 * @retval              <0  エラー
 */
NJ_INT16 njd_p_search_word(NJ_CLASS * iwnn, NJ_SEARCH_CONDITION *con, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 dic_idx) {
    NJ_UINT16 ret = 0;


    switch (con->operation) {
    case NJ_CUR_OP_LINK:
        /* つながり検索 */
        /* 読み無し前品詞がない場合、候補なしを返す */
        if (con->hinsi.prev_bpos == 0) {
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }
        break;
    case NJ_CUR_OP_FORE:
        /* 前方一致 */
        /* 読みが空文字列の場合、候補なしを返す */
        if (nj_strlen(con->yomi) == 0) {
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }

        /* 読み無し前品詞がない場合、候補なしを返す */
        if (con->hinsi.prev_bpos == 0) {
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }
        break;
    default:
        /* つながり、前方一致以外は、候補なしを返す */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    } 


    /* 検索候補取得順：頻度順(0)のみ有効 */
    if (con->mode != NJ_CUR_MODE_FREQ) {
        /* 対応しない検索操作が指定された場合、該当候補なしとする */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    /* カーソルの初期化は上位で行っている*/
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        /* 初回検索 */
        ret = search_data(iwnn, con, loctset);
        if (ret < 1) {
            /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
            loctset->loct.status = NJ_ST_SEARCH_END;
            return ret;
        }
    }
    /* 次候補検索 */
    ret = search_data_next(iwnn, con, loctset);
    if (ret < 1) {
        /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
        loctset->loct.status = NJ_ST_SEARCH_END;
    }

    return ret;

}


/**
 * 拡張読み無し辞書アダプタ  単語取得
 *
 * @attention 読みは、上位で設定する。
 *
 * @param[in]   loctset   検索位置
 * @param[out]  word      単語情報
 *
 * @retval            1   候補あり
 * @retval            0   候補なし
 * @retval           <0   エラー
 */
NJ_INT16 njd_p_get_word(NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word) {
    NJ_UINT8 *word_data_top;
    NJ_UINT32 word_data_current_no;
    NJ_UINT8 *word_data_current;
    NJ_UINT8 *search_idx_top;
    NJ_UINT32 search_current;
    NJ_DIC_HANDLE handle;
    NJ_UINT16 hinsi_data;
    NJ_UINT16 vbpos;
    NJ_UINT16 vfpos;
    NJ_UINT16 rbpos;
    NJ_UINT16 rfpos;
    NJ_UINT8  fpos_byte;
    NJ_UINT8  bpos_byte;
    NJ_UINT8 *wpos;
    NJ_INT16 yomilen, kouholen;

    NJ_UINT8 *p_attr;


    /* 検索状態：次候補なし  は、そのままリターン */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_END) {
        return 0; /*NCH_FB*/
    }

    /* 辞書ハンドルを取得 */
    handle = loctset->loct.handle;

    /*
     * 検索インデックスから単語IDを算出する。
     *
     */
    /* 検索インデックスの先頭を取得 */
    search_idx_top = SEARCH_IDX_TOP_ADDR(handle);

    /* 現在の検索インデックス位置を取得 */
    search_current = loctset->loct.current;

    /* 単語IDを取得 */
    word_data_current_no =SEARCH_IDX_WORD_NO(SEARCH_IDX_ADDR(search_idx_top, search_current));


    /*
     * 単語IDをキーとして、単語情報の設定を行う
     *
     */
    /* 単語データの先頭を取得 */
    word_data_top = WORD_TOP_ADDR(handle);
    /* 単語IDの単語データ位置を取得 */
    word_data_current = WORD_IDX_ADDR(word_data_top, word_data_current_no);


    /*
     * 仮想品詞を実品詞に変換
     *
     */
    hinsi_data = HINSI_DATA(word_data_current);

    /* 仮想品詞の取得  */
    if (BIT_FHINSI(handle)) {
        vfpos = GET_BITFIELD_16(hinsi_data, 0, BIT_FHINSI(handle));
    } else {
        vfpos = 0; /*NCH_FB*/
    }
    if (BIT_BHINSI(handle)) {
        vbpos = GET_BITFIELD_16(hinsi_data, BIT_FHINSI(handle), BIT_BHINSI(handle));
    } else {
        vbpos = 0; /*NCH_FB*/
    }

    /* 実品詞の取得 */
    fpos_byte = FHINSI_NO_BYTE(handle);
    wpos = (HINSI_NO_AREA_TOP_ADDR(handle) + (NJ_UINT32)(fpos_byte * vfpos));
    if (fpos_byte == 2) {
        rfpos = (NJ_UINT16)(NJ_INT16_READ(wpos)); /*NCH_FB*/
    } else {
        rfpos = (NJ_UINT16)(*wpos);
    }

    bpos_byte = BHINSI_NO_BYTE(handle);
    wpos = (HINSI_NO_AREA_TOP_ADDR(handle)
            + (NJ_UINT32)((fpos_byte * (FHINSI_NO_CNT(handle))) + (bpos_byte * vbpos)));
    if (bpos_byte == 2) {
        rbpos = (NJ_UINT16)(NJ_INT16_READ(wpos));
    } else {
        rbpos = (NJ_UINT16)(*wpos);
    }

    /*
     * 読み、表記の文字列長を取得する。
     *
     */
    /* 読み文字列長を取得 */
    yomilen  = (NJ_UINT16)YOMI_LEN(word_data_current);

    /* 表記長取得 */
    kouholen = (NJ_UINT16)HYOKI_LEN(word_data_current);
    if (kouholen == 0) {
        /* 読み、表記と同じため、yomilenを使う */
        kouholen = yomilen;
    }

    /*
     * NJ_WORDに算出した値を設定する。
     *
     */
    NJ_SET_FPOS_TO_STEM(word, rfpos);    /* 前品詞       */
    NJ_SET_YLEN_TO_STEM(word, yomilen);  /* 読み文字列長 */
    NJ_SET_EXT_YLEN_TO_STEM(word, 0);    /* 拡張検索：読み文字列長 */
    NJ_SET_BPOS_TO_STEM(word, rbpos);    /* 後品詞       */
    NJ_SET_KLEN_TO_STEM(word, kouholen); /* 表記文字列長 */

    /* 算出済みの頻度を使用する */
    word->stem.hindo = loctset->cache_freq;
    /* 現在の検索位置を設定 */
    word->stem.loc = loctset->loct;
    if (NJ_GET_DIC_VER(loctset->loct.handle) == NJ_DIC_VERSION2) {
        p_attr = (NJ_UINT8 *)&word->stem.loc.attr;
        *p_attr &= MASK_RESERVE_BIT;
        /* カテゴリ先頭から3Byte移動する(指定Bitが25Bit目以降である為) */
        p_attr += CAT_POS_DAILY_LIFE;
        *p_attr &= MASK_DAILY_LIFE_BIT;
        word->stem.loc.current_info = CURRENT_INFO_SET;
    }

    /* 疑似候補の種類はクリア */
    word->stem.type = 0;

    return 1;
}


/**
 * 拡張読み無し辞書アダプタ  読み文字列取得
 *
 * @param[in]     word      単語情報(NJ_RESULT->wordを指定)
 * @param[out]    stroke    候補文字列格納バッファ
 * @param[in]     size      候補文字列格納バッファサイズ(byte)
 *
 * @retval            >0    取得文字列配列長(ヌル文字含まず)
 * @retval            <0    エラー
 */
NJ_INT16 njd_p_get_stroke(NJ_WORD *word, NJ_CHAR *stroke, NJ_UINT16 size) {
    NJ_UINT8     *search_idx_top;
    NJ_UINT32     current;
    NJ_UINT8     *current_addr;
    NJ_DIC_HANDLE handle;
    NJ_UINT8     *word_data_top;
    NJ_UINT32     word_data_current_no;
    NJ_UINT8     *word_data_current;
    NJ_UINT16     outlen;
    NJ_UINT8     *data_addr;


    if ((GET_LOCATION_OPERATION(word->stem.loc.status) != NJ_CUR_OP_FORE) &&
        (GET_LOCATION_OPERATION(word->stem.loc.status) != NJ_CUR_OP_LINK)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_GET_STROKE, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
    }

    if (NJ_GET_YLEN_FROM_STEM(word) == 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_GET_STROKE, NJ_ERR_INVALID_RESULT); /*NCH*/
    }

    /*
     * 単語情報内の検索語位置（word->loc.current）から読み文字列を返す。
     */
    /* 辞書ハンドルを取得 */
    handle  = word->stem.loc.handle;
    current = word->stem.loc.current;

    /* 検索インデックスの先頭を取得 */
    search_idx_top = SEARCH_IDX_TOP_ADDR(handle);

    /* 現在位置を取得 */
    current_addr = SEARCH_IDX_ADDR(search_idx_top, current);

    /* 単語IDを取得 */
    word_data_current_no = SEARCH_IDX_WORD_NO(current_addr);

    /* 単語データの先頭を取得 */
    word_data_top = WORD_TOP_ADDR(handle);
    /* 単語IDの単語データ位置を取得 */
    word_data_current = WORD_IDX_ADDR(word_data_top, word_data_current_no);

    /* 文字列の長さを取得 */
    outlen = (NJ_UINT16)(YOMI_LEN(word_data_current));

    /* サイズチェック */
    if (size < ((outlen + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_GET_STROKE, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH_FB*/
    }

    /* 読み領域のアドレスを取得する */
    data_addr = YOMI_HYOKI_TOP_ADDR(handle) + (NJ_UINT32)YOMI_DATA_OFFSET(word_data_current);
    
    /* 読み文字列を取得する */
    nj_memcpy((NJ_UINT8*)stroke, data_addr, (NJ_UINT16)(outlen * sizeof(NJ_CHAR)));
    *(stroke + outlen) = NJ_CHAR_NUL;

    return outlen;
}


/**
 * 拡張読み無し辞書アダプタ  候補文字列取得
 *
 * @param[in]   word      単語情報(NJ_RESULT->wordを指定)
 * @param[out]  candidate 候補文字列格納バッファ
 * @param[in]   size      候補文字列格納バッファサイズ(byte)
 *
 * @retval             >0 取得文字配列長(ヌル文字含まず)
 * @retval             <0 エラー
 */
NJ_INT16 njd_p_get_candidate(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size) {
    NJ_UINT8     *search_idx_top;
    NJ_UINT32     current;
    NJ_UINT8     *current_addr;
    NJ_DIC_HANDLE handle;
    NJ_UINT8     *word_data_top;
    NJ_UINT32     word_data_current_no;
    NJ_UINT8     *word_data_current;
    NJ_UINT16     outlen;
    NJ_UINT8     *data_addr;
    NJ_CHAR       ybuf[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];


    if ((GET_LOCATION_OPERATION(word->stem.loc.status) != NJ_CUR_OP_FORE) &&
        (GET_LOCATION_OPERATION(word->stem.loc.status) != NJ_CUR_OP_LINK)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_GET_CANDIDATE, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
    }

    if (NJ_GET_YLEN_FROM_STEM(word) == 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_GET_CANDIDATE, NJ_ERR_INVALID_RESULT); /*NCH*/
    }

    /*
     * 単語情報内の検索語位置（word->loc.current）から読み文字列を返す。
     */
    /* 辞書ハンドルを取得 */
    handle  = word->stem.loc.handle;
    current = word->stem.loc.current;

    /* 検索インデックスの先頭を取得 */
    search_idx_top = SEARCH_IDX_TOP_ADDR(handle);

    /* 現在位置の頻度を取得 */
    current_addr = SEARCH_IDX_ADDR(search_idx_top, current);

    /* 単語IDを取得 */
    word_data_current_no = SEARCH_IDX_WORD_NO(current_addr);

    /* 単語データの先頭を取得 */
    word_data_top = WORD_TOP_ADDR(handle);
    /* 単語IDの単語データ位置を取得 */
    word_data_current = WORD_IDX_ADDR(word_data_top, word_data_current_no);

    /* 文字列の長さを取得 */
    outlen = (NJ_UINT16)(HYOKI_LEN(word_data_current));
    
    if (outlen == 0) {
        /*
         * 無変換候補の場合、読み文字列を取得する
         * その後、表記バイト数ののカタカナフラグが設定されているかチェックする
         */
        outlen = (NJ_UINT16)(YOMI_LEN(word_data_current));
        /* サイズチェック */
        if (size < ((outlen + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_GET_CANDIDATE, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH_FB*/
        }

        /* 読みのアドレスを取得する */
        data_addr = YOMI_HYOKI_TOP_ADDR(handle) + (NJ_UINT32)YOMI_DATA_OFFSET(word_data_current);

        if (HYOKI_INFO(word_data_current) != 0) {
            /* カタカナ候補の場合 */
            /* いったん ybufに読み文字列をコピー */
            nj_memcpy((NJ_UINT8*)ybuf, data_addr, (NJ_UINT16)(outlen * sizeof(NJ_CHAR)));
            ybuf[outlen] = NJ_CHAR_NUL;
            /* カタカナに変換 */
            nje_convert_hira_to_kata(ybuf, candidate, outlen);
        } else {
            /* 平仮名をコピー */
            nj_memcpy((NJ_UINT8*)candidate, data_addr, (NJ_UINT16)(outlen * sizeof(NJ_CHAR)));
        }
    } else {
        /* サイズチェック */
        if (size < ((outlen + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_GET_CANDIDATE, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH_FB*/
        }

        /* 表記のアドレスを取得する */
        data_addr = YOMI_HYOKI_TOP_ADDR(handle) + (NJ_UINT32)HYOKI_DATA_OFFSET(word_data_current);

        /* 表記文字列をコピー */
        nj_memcpy((NJ_UINT8*)candidate, data_addr, (NJ_UINT16)(outlen * sizeof(NJ_CHAR)));
    }

    /* NUL文字でターミネートする */
    *(candidate + outlen) = NJ_CHAR_NUL;

    return outlen;
}


/**
 * 単語頻度取得関数
 *
 * @param[in]  iwnn       解析情報クラス
 * @param[in]  current    単語ID
 * @param[in]  base_hindo 初期単語頻度
 * @param[in]  loctset    検索位置
 *
 * @retval 単語頻度
 */
static NJ_INT32 get_hindo(NJ_CLASS *iwnn,
                          NJ_UINT32 current,
                          NJ_INT16  base_hindo,
                          NJ_SEARCH_LOCATION_SET *loctset) {

    NJ_DIC_HANDLE handle;
    NJ_INT32      ret_hindo = base_hindo;

    /* 属性データ用 */
    NJ_UINT32  attr_data;
    NJ_INT32   attr_bias;


    /* 辞書ハンドルの取得 */
    handle = loctset->loct.handle;

    attr_data = get_attr(handle, current);
    if (attr_data != 0x00000000) {
        /* 属性付き単語の場合、状況に応じて頻度加算・減算する */
        attr_bias = njd_get_attr_bias(iwnn, attr_data);
        ret_hindo += CALCULATE_ATTR_HINDO32(attr_bias,
                                            loctset->dic_freq.base,
                                            loctset->dic_freq.high, 
                                            EXT_YOMINASI_DIC_FREQ_DIV);
    }

    return ret_hindo;
}


/**
 * 属性データ取得関数
 *
 * @param[in]  handle     辞書ハンドル
 * @param[in]  current    単語ID
 *
 * @retval           0    属性データなし
 * @retval          !0    属性データ
 */
static NJ_UINT32 get_attr(NJ_DIC_HANDLE handle, NJ_UINT32 current) {
    NJ_UINT32 ret_attr = 0x00000000;
    NJ_UINT8  *tmp_attr;
    /* 属性データ用 */
    NJ_UINT8 *vattr_addr;
    NJ_UINT32 vattr_byte;
    NJ_UINT32 vattr_data;
    NJ_UINT8 *attr_tmp;

    NJ_UINT8 *rattr_addr;
    NJ_UINT32 rattr_cnt;
    NJ_UINT32 rattr_byte;
    NJ_UINT32  attr_i;


    /* 属性データ */
    vattr_addr = ZOKUSEI_AREA_TOP_ADDR(handle);
    vattr_byte = ZOKUSEI_AREA_LEN(handle);
    /* 実属性データ */
    rattr_addr = REAL_ZOKUSEI_AREA_TOP_ADDR(handle);
    rattr_cnt  = REAL_ZOKUSEI_AREA_CNT(handle);
    rattr_byte = REAL_ZOKUSEI_AREA_LEN(handle);
    if ((vattr_addr == NULL) ||
        (vattr_byte == 0) ||
        (rattr_addr == NULL) ||
        (rattr_cnt == 0) ||
        (rattr_byte == 0)) {
        /*
         * 属性データの一部が正しくない場合は、
         * 属性データが存在しないものとして扱う
         */
        /* 属性データ */
        vattr_addr = NULL;
        /* 実属性データ */
        rattr_addr = NULL;
    }

    /* 属性データを取得 */
    if (vattr_addr != NULL) {
        /* 仮想属性番号を取得 */
        attr_tmp = V_ZOKUSEI_ADDR(vattr_addr, current, vattr_byte);
        vattr_data= (NJ_UINT32)*attr_tmp;
        attr_tmp++;
        for (attr_i = 1; attr_i < vattr_byte; attr_i++, attr_tmp++) {
            vattr_data = (NJ_UINT32)(vattr_data << 8); /*NCH_DEF*/
            vattr_data += (NJ_UINT32)*attr_tmp; /*NCH_DEF*/
        }

        /* 実属性データを取得 */
        if (vattr_data != 0) {
            attr_tmp = R_ZOKUSEI_ADDR(rattr_addr, vattr_data, rattr_byte);
            tmp_attr = (NJ_UINT8*)&ret_attr;
            for (attr_i = 0; attr_i < rattr_byte; attr_i++) {
                *tmp_attr++ = *attr_tmp++;
            }
        }
    }

    return ret_attr;

}


/**
 * 付加情報領域チェック処理
 *
 * @param[in]     iwnn        解析情報クラス
 * @param[in]     handle      辞書ハンドル
 * @param[out]    add_info    付加情報領域
 * @param[in]     size        付加情報領域サイズ
 *
 * @retval               1    正常終了
 * @retval              <0    エラー
 */
NJ_INT16 njd_p_check_additional_info(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *add_info, NJ_UINT32 size) {
    NJ_INT16  ret;
    NJ_UINT8 *add_tmp;
    NJ_UINT32 top_addr;
    NJ_UINT32 data_size;
    NJ_UINT32 check_size;
    NJ_UINT8 *handle_tmp;
    NJ_UINT32 i;
    NJ_UINT32 info_data_size;
    NJ_UINT32 yomi_data_size;
    NJ_UINT32 max_yomi_len;
    NJ_UINT32 flag;


    ret = 1;
    
    /**
     * ヘッダ領域分のサイズがあるかチェック
     */
    if (size <= NJ_DIC_ADD_HEAD_SIZE) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_CHECK_ADD_INFO, NJ_ERR_BUFFER_NOT_ENOUGH);
    }

    /*
     * 付加情報領域の作成
     *
     */
    add_tmp    = (NJ_UINT8*)add_info;
    /* 識別子 */
    if (NJ_INT32_READ(add_tmp) != NJ_DIC_ADD_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_CHECK_ADD_INFO, NJ_ERR_FORMAT_INVALID);
    }
    add_tmp += sizeof(NJ_UINT32);

    /* 付加情報領域サイズ */
    data_size = NJ_INT32_READ(add_tmp);
    add_tmp += sizeof(NJ_UINT32);

    if (size != data_size) {
        /* サイズエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_CHECK_ADD_INFO, NJ_ERR_PARAM_ADD_INFO_INVALID_SIZE);
    }

    /* チェック用データオフセットのアドレスを取得 */
    top_addr = NJ_INT32_READ(add_tmp);
    add_tmp += sizeof(NJ_UINT32);

    /* チェック用データサイズを取得 */
    check_size = NJ_INT32_READ(add_tmp);
    add_tmp += sizeof(NJ_UINT32);

    /* 付加情報データサイズを取得 */
    add_tmp += sizeof(NJ_UINT32);
    info_data_size = NJ_INT32_READ(add_tmp);
    add_tmp += sizeof(NJ_UINT32);

    /* 読み・表記サイズを取得 */
    add_tmp += sizeof(NJ_UINT32);
    yomi_data_size = NJ_INT32_READ(add_tmp);
    add_tmp += sizeof(NJ_UINT32);

    /* 最大付加情報文字列を取得 */
    max_yomi_len = NJ_INT32_READ(add_tmp);
    add_tmp += sizeof(NJ_UINT32);

    /* オプションフラグを取得 */
    flag = NJ_INT32_READ(add_tmp);
    add_tmp += sizeof(NJ_UINT32);

    /* 文字列長のチェックを実施 */
    if (flag & NJ_DIC_ADD_LEARN_FLG) {
        /* 学習可能データ */
        if ((max_yomi_len / sizeof(NJ_CHAR)) > NJ_MAX_ADDITIONAL_LEN) {
            ret = NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_CHECK_ADD_INFO, NJ_ERR_FORMAT_INVALID);
        }
    }

    /* 
     * 辞書と頻度学習領域の共通データ、拡張情報をチェック
     */
    handle_tmp = handle;
    add_tmp = (NJ_UINT8*)(add_info);
    add_tmp += top_addr;
    for (i = 0; i < check_size; i++) {
        if (*add_tmp++ != *handle_tmp++) {
            ret = NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_CHECK_ADD_INFO, NJ_ERR_FORMAT_INVALID);
            break;
        }
    }

    /* 終端識別子 */
    if (NJ_INT32_READ((NJ_UINT8*)add_info + data_size - NJ_DIC_ADD_ID_LEN) != NJ_DIC_ADD_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_CHECK_ADD_INFO, NJ_ERR_FORMAT_INVALID);
    }

    return ret;

}


/**
 * 拡張読み無し辞書アダプタ  付加情報文字列取得
 *
 * @param[in]       iwnn : 解析情報クラス
 * @param[in]       word : 単語情報(NJ_RESULT->wordを指定)
 * @param[in]      index : 付加情報インデックス
 * @param[out]  add_info : 付加情報文字列格納バッファ
 * @param[in]       size : 付加情報文字列格納バッファサイズ(byte)
 *
 * @retval            <0   エラー
 * @retval           >=0   取得文字配列長(ヌル文字含まず)
 */
NJ_INT32 njd_p_get_additional_info(NJ_CLASS *iwnn, NJ_WORD *word, NJ_UINT8 index, NJ_CHAR *add_info, NJ_UINT32 size) {
    NJ_SEARCH_LOCATION *loc;
    NJ_UINT8 *data, *search_idx_top;
    NJ_UINT32 len = 0;
    NJ_UINT32 current;
    NJ_UINT8 *data_top_addr;
    NJ_UINT8 *word_top_addr;
    NJ_UINT8 *word_addr;



    if ((GET_LOCATION_OPERATION(word->stem.loc.status) != NJ_CUR_OP_FORE) &&
        (GET_LOCATION_OPERATION(word->stem.loc.status) != NJ_CUR_OP_LINK)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_GET_ADD_INFO, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
    }

    if (NJ_GET_YLEN_FROM_STEM(word) == 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_GET_ADD_INFO, NJ_ERR_INVALID_RESULT); /*NCH*/
    }

    loc = &word->stem.loc;
    search_idx_top = SEARCH_IDX_TOP_ADDR(loc->handle); /* 検索インデックスの先頭を取得 */
    current = SEARCH_IDX_WORD_NO(SEARCH_IDX_ADDR(search_idx_top, loc->current)); /* 単語IDを取得 */
    data_top_addr = loc->add_info[index];

    /* 付加情報データ＆文字列領域の先頭アドレスを取得 */
    word_top_addr = ADD_WORD_TOP_ADDR(data_top_addr);

    word_addr = ADD_WORD_DATA_ADDR(word_top_addr, current);

    len  = ADD_STRING_LEN(word_addr);

    if (size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_GET_ADD_INFO, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH_DEF*/
    }
    if ((index == 0) &&
        (ADD_LEARN_FLG(data_top_addr) == NJ_DIC_ADD_LEARN_FLG)) {
        if (len > NJ_MAX_ADDITIONAL_LEN) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_P_GET_ADD_INFO, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH_DEF*/
        }
    }

    data = ADD_STRING_TOP_ADDR(data_top_addr) + ADD_STRING_DATA_OFFSET(word_addr);

    /* 文字列を取得 */
    nj_memcpy((NJ_UINT8*)add_info, data, (NJ_UINT16)(len * sizeof(NJ_CHAR)));

    /* 読み文字列を NULLでターミネートする */
    *(add_info + len) = NJ_CHAR_NUL;

    return len;
}
