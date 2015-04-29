/**
 * @file
 *   辞書アダプタ：圧縮辞書アダプタ
 *
 *   圧縮辞書構造フォーマットへのアクセス関数を提供する。<br>
 *   辞書タイプ：
 *    - 自立語辞書
 *    - 付属語辞書
 *    - 単漢字辞書
 *    - 標準予測辞書
 *    - 圧縮カスタマイズ辞書
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2010 All Rights Reserved.
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

#define NODE_TERM(x) ((NJ_UINT8)(0x80 & (*(x))))
#define NODE_LEFT_EXIST(x) ((NJ_UINT8)(0x40 & (*(x))))
#define NODE_DATA_EXIST(x) ((NJ_UINT8)(0x20 & (*(x))))
#define NODE_IDX_EXIST(x) ((NJ_UINT8)(0x10 & (*(x))))
#define NODE_IDX_CNT(x) ((NJ_UINT8)((0x0f & (*(x))) + 2))

#define STEM_TERMINETER(x) ((NJ_UINT8)(0x80 & (*(x))))

#define STEM_NO_CONV_FLG(x) ((NJ_UINT8)(0x40 & (*(x))))

#define TERM_BIT (1)            /**< ターミネータフラグ ビット数 */
#define INDEX_BIT (8)           /**< 読みインデックス ビット数 */

#define APPEND_YOMI_FLG(h) ((NJ_UINT8)(0x80 & (*((h) + 0x1C))))
#define HINSI_NO_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x1D)))
#define FHINSI_NO_CNT(h) ((NJ_INT16)(NJ_INT16_READ((h) + 0x21)))
#define BHINSI_NO_CNT(h) ((NJ_INT16)(NJ_INT16_READ((h) + 0x23)))
#define HINSI_NO_BYTE(h) ((NJ_UINT8)(*((h) + 0x25)))
#define HINDO_NO_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x26)))
#define HINDO_NO_CNT(h) ((NJ_UINT8)(*((h) + 0x2A)))
#define STEM_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x2B)))
#define BIT_CANDIDATE_LEN(h) ((NJ_UINT8)(*((h) + 0x2F)))
#define BIT_FHINSI(h) ((NJ_UINT8)(*((h) + 0x30)))
#define BIT_BHINSI(h) ((NJ_UINT8)(*((h) + 0x31)))
#define BIT_HINDO_LEN(h) ((NJ_UINT8)(*((h) + 0x32)))
#define BIT_MUHENKAN_LEN(h) ((NJ_UINT8)(*((h) + 0x33)))
#define BIT_YOMI_LEN(h) ((NJ_UINT8)(*((h) + 0x35)))
#define YOMI_INDX_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x42)))
#define YOMI_INDX_CNT(h) ((NJ_INT16)(*((h) + 0x46)))
#define YOMI_INDX_SIZE(h) ((NJ_INT8)(*((h) + 0x47)))
#define NODE_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x48)))
#define BIT_NODE_AREA_DATA_LEN(h) ((NJ_UINT8)(*((h) + 0x4C)))
#define BIT_NODE_AREA_LEFT_LEN(h) ((NJ_UINT8)(*((h) + 0x4D)))
#define NODE_AREA_MID_ADDR(h) ((NJ_UINT32)(NJ_INT32_READ((h) + 0x4E)))
#define CAND_IDX_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x52)))
#define CAND_IDX_AREA_CNT(h) ((NJ_UINT32)(((NJ_INT32_READ((h) + 0x56)) >> 8) & 0x00FFFFFF))
#define CAND_IDX_AREA_SIZE(h) ((NJ_UINT8)(*((h) + 0x59)))

#define WORD_LEN(x) ((NJ_UINT16)(0x007F & (x)))

/**
 * 検索位置  設定情報
 */
#define CURRENT_INFO_SET ((NJ_UINT8)(0x10))

#define COMP_DIC_FREQ_DIV 63      /**< 圧縮辞書頻度段階 */

/**
 * 候補無し時のloct.currentの値
 */
#define LOC_CURRENT_NO_ENTRY  0xffffffffU

/************************************************/
/*              構 造 体 宣 言                  */
/************************************************/
/** 自立語データ格納構造体 */
typedef struct {
    NJ_UINT16 stem_size;        /**< STEMデータ領域バイト長 */
    NJ_UINT16 term;             /**< ターミネータ */
    NJ_UINT16 no_conv_flg;      /**< 無変換フラグ */
    NJ_HINDO hindo;             /**< 頻度(インデックス化) */
    NJ_INT16 hindo_jitu;        /**< 実頻度 */
    NJ_UINT16 candidate_size;   /**< 表記データバイト長 */
    NJ_UINT16 yomi_size;        /**< 読みデータバイト長 */
    NJ_UINT16 fhinsi;           /**< 前品詞(インデックス化) */
    NJ_UINT16 bhinsi;           /**< 後品詞(インデックス化) */
    NJ_UINT16 fhinsi_jitu;      /**< 実前品詞 */
    NJ_UINT16 bhinsi_jitu;      /**< 実後品詞 */
} STEM_DATA_SET;

/************************************************/
/*              prototype  宣  言               */
/************************************************/
static NJ_INT16 get_stem_next(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data);
static void get_stem_word(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data, STEM_DATA_SET *stem_set, NJ_UINT8 check);
static void get_stem_cand_data(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data, STEM_DATA_SET *stem_set);
static NJ_UINT16 get_stem_yomi_data(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data,STEM_DATA_SET *stem_set);
static NJ_UINT16 get_stem_yomi_size(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data, NJ_UINT16 yomi_size);
static NJ_UINT16 get_stem_yomi_string(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data, NJ_CHAR *yomi, NJ_UINT16 yomi_pos, NJ_UINT16 yomi_size, NJ_UINT16 size);
static NJ_INT16 search_node(NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset);
static NJ_INT16 bdic_search_data(NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset);
static NJ_INT16 bdic_search_fore_data(NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset);
static NJ_INT16 search_index(NJ_SEARCH_CONDITION *condition, 
                             NJ_SEARCH_LOCATION_SET *loctset);
static NJ_INT16 bdic_search_rev_data(NJ_SEARCH_CONDITION *condition, 
                                     NJ_SEARCH_LOCATION_SET *loctset);
static NJ_INT16 str_muhenkan_cmp(NJ_UINT8 *hdl, NJ_CHAR *key, 
                                 NJ_UINT8 key_len, NJ_UINT8 *ydata, 
                                 NJ_UINT8 yomi_size, NJ_UINT8 muhenkan,
                                 NJ_UINT16 *len);

static NJ_HINDO get_stem_hindo(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data);

static NJ_INT16 search_node2(NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset,
                             NJ_UINT16 hidx);
static NJ_INT16 bdic_search_fore_data2(NJ_SEARCH_CONDITION *condition,
                                       NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 hidx);
static NJ_INT16 search_yomi_node(NJ_UINT8 operation, NJ_UINT8 *node,
                                 NJ_UINT8 *now, NJ_UINT16 idx_no,
                                 NJ_CHAR  *yomi, NJ_UINT16 yomilen,
                                 NJ_UINT8 *root, NJ_UINT8 *node_mid,
                                 NJ_UINT16 bit_left, NJ_UINT16 bit_data,
                                 NJ_UINT8 *data_top,
                                 NJ_INT16 ytbl_cnt, NJ_UINT16 y,
                                 NJ_UINT8 *ytbl_top, NJ_CACHE_INFO *storebuf,
                                 NJ_UINT8 **con_node, NJ_UINT32 *data_offset);
static NJ_INT16 get_node_bottom(NJ_CHAR *yomi, NJ_UINT8 *now, NJ_UINT8 *node_mid,
                                NJ_UINT8 *data_top, NJ_UINT16 bit_left,
                                NJ_UINT16 bit_data, NJ_UINT32 top,
                                NJ_DIC_HANDLE handle, NJ_UINT32 *ret_bottom);
static NJ_INT16 bdic_get_next_data(NJ_UINT8 *data_top, NJ_UINT8 *data_end,
                                   NJ_SEARCH_LOCATION_SET *loctset,
                                   NJ_SEARCH_CACHE *psrhCache, NJ_UINT16 abIdx);
static NJ_INT16 bdic_get_word_freq(NJ_UINT8 *data_top, NJ_SEARCH_LOCATION_SET *loctset,
                                   NJ_SEARCH_CACHE *psrhCache, NJ_UINT16 abIdx);

/**
 * 圧縮辞書データ領域の単語頻度情報を取得
 *
 * @param[in]  hdl        辞書ハンドル
 * @param[in]  stem_data  STEMデータ領域
 *
 * @return 頻度
 */
static NJ_HINDO get_stem_hindo(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data) {
    NJ_UINT8 flg_bit;
    NJ_UINT16 data;
    NJ_UINT16 pos, j, bit_all;


    /* 無変換フラグ 設定 */
    flg_bit = BIT_MUHENKAN_LEN(hdl);
    if (NJ_GET_DIC_FMT(hdl) != NJ_DIC_FMT_KANAKAN) {
        flg_bit++;  /* 正規化フラグの分を加算 */
    }

    if (BIT_HINDO_LEN(hdl)) {
        /* 頻度位置(byte)算出 */
        bit_all = (NJ_UINT16)(TERM_BIT + flg_bit);
        pos = (NJ_UINT16)(bit_all >> 3);
        data = (NJ_UINT16)(NJ_INT16_READ(stem_data + pos));
        
        /* 頻度位置(bit)算出 */
        j = (NJ_UINT16)(bit_all & 0x0007);

        return GET_BITFIELD_16(data, j, BIT_HINDO_LEN(hdl));
    } else {
        /* 頻度ビット数 が 0 の場合 */
        return 0;
    }
}


/**
 * 圧縮辞書データ領域の次候補データへのオフセット取得
 *
 * @param[in]  hdl       辞書ハンドル
 * @param[in]  stem_data STEMデータ領域
 *
 * @return 次候補データへのバイトオフセット値
 */
static NJ_INT16 get_stem_next(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data) {
    NJ_UINT8 flg_bit;
    NJ_UINT16 data;
    NJ_UINT16 pos, j, bit_all;
    NJ_UINT16 stem_size, cand_bit, yomi_bit;
    NJ_UINT16 candidate_size, yomi_size;


    /* 辞書種別を見て処理分ける */
    flg_bit = BIT_MUHENKAN_LEN(hdl);
    if (NJ_GET_DIC_FMT(hdl) != NJ_DIC_FMT_KANAKAN) {
        flg_bit++;  /* 正規化フラグの分を加算 */
    }

    /* 表記バイト長設定 */
    /* 表記バイト長位置(byte)算出 */
    bit_all = (NJ_UINT16)(TERM_BIT + flg_bit + 
                          BIT_HINDO_LEN(hdl) + 
                          BIT_FHINSI(hdl) + 
                          BIT_BHINSI(hdl));
    pos = (NJ_UINT16)(bit_all >> 3);
    data = (NJ_UINT16)(NJ_INT16_READ(stem_data + pos));

    /* 表記バイト長位置(bit)算出 */
    j = (NJ_UINT16)(bit_all & 0x0007);
    cand_bit = BIT_CANDIDATE_LEN(hdl);
    /* 表記バイト長取得 */
    candidate_size = GET_BITFIELD_16(data, j, cand_bit);
    bit_all += cand_bit;

    /* 読みバイト長設定 */
    if (APPEND_YOMI_FLG(hdl) && STEM_TERMINETER(stem_data)) {
        /* 読みバイト長のデータが存在する場合 */
        /* 読みバイト長位置(byte)算出 */
        pos = (NJ_UINT16)(bit_all >> 3);
        data = (NJ_UINT16)(NJ_INT16_READ(stem_data + pos));

        /* 読みバイト長位置(bit)算出 */
        j = (NJ_UINT16)(bit_all & 0x0007);
        yomi_bit = BIT_YOMI_LEN(hdl);
        /* 読みバイト長取得 */
        yomi_size = GET_BITFIELD_16(data, j, yomi_bit);
        bit_all += yomi_bit;
    } else {
        yomi_size = 0;  /* 読みデータ存在なし時,読み長0 */
    }

    /* ここまでのビット数をバイトに補正する */
    stem_size = GET_BIT_TO_BYTE(bit_all);

    /* 表記文字列バイト長を追加 */
    stem_size += candidate_size;

    /* 読みデータ領域のデータバイト長を追加 */
    stem_size += yomi_size;

    /* 次候補データへのバイトオフセット返す */
    return stem_size;
}


/**
 * 圧縮辞書データ領域の単語情報を取得
 *
 * @param[in]  hdl       辞書ハンドル
 * @param[in]  stem_data STEMデータ領域
 * @param[out] stem_set  STEMデータ設定
 * @param[in]  check     読み長・表記長取得フラグ<br>
 *                        0: 読み長、表記長を取得する<br>
 *                        1: 読み長、表記長は取得しない<br>
 *                        2: 読み長は取得しないが、表記長は取得する
 *
 * @return なし
 */
static void get_stem_word(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data, STEM_DATA_SET *stem_set, NJ_UINT8 check) {
    NJ_UINT8 flg_bit;
    NJ_UINT16 data;
    NJ_UINT16 pos, j, bit_all = 0;
    NJ_UINT16 bit;
    NJ_UINT16 dpos = 0;
    NJ_INT16 next;
    NJ_UINT8 b;
    NJ_UINT8 *wkc;

   
    /* 辞書種別を見て処理分ける */
    flg_bit = BIT_MUHENKAN_LEN(hdl);
    if (NJ_GET_DIC_FMT(hdl) != NJ_DIC_FMT_KANAKAN) {
        flg_bit++;  /* 正規化フラグの分を加算 */
    }

    if (BIT_HINDO_LEN(hdl)) {
        /* 頻度位置(byte)算出 */
        bit_all = (NJ_UINT16)(TERM_BIT + flg_bit);
        pos = (NJ_UINT16)(bit_all >> 3);
        data = (NJ_UINT16)(NJ_INT16_READ(stem_data + pos));
        
        /* 頻度位置(bit)算出 */
        j = (NJ_UINT16)(bit_all & 0x0007);

        stem_set->hindo = GET_BITFIELD_16(data, j, BIT_HINDO_LEN(hdl));
    } else {
        /* 頻度ビット数 が 0 の場合 */
        stem_set->hindo = 0;
    }
    /* 実頻度値復元 */
    stem_set->hindo_jitu = (NJ_INT16)(*(HINDO_NO_TOP_ADDR(hdl) + stem_set->hindo));

    if (BIT_FHINSI(hdl)) {
        /* 前品詞 設定 */
        /* 前品詞位置(byte)算出 */
        bit_all = (NJ_UINT16)(TERM_BIT + flg_bit + BIT_HINDO_LEN(hdl));
        pos = (NJ_UINT16)(bit_all >> 3);
        data = (NJ_UINT16)(NJ_INT16_READ(stem_data + pos));
        
        /* 前品詞位置(bit)算出 */
        j = (NJ_UINT16)(bit_all & 0x0007);
        
        stem_set->fhinsi = GET_BITFIELD_16(data, j, BIT_FHINSI(hdl));
    } else {
        stem_set->fhinsi = 0;
    }

    /* 品詞番号変換用テーブルの該当位置 */
    b = HINSI_NO_BYTE(hdl);
    wkc = (NJ_UINT8*)(HINSI_NO_TOP_ADDR(hdl) + (b * (NJ_UINT16)(stem_set->fhinsi)));

    /* 実品詞への変換：品詞番号設定 */
    if (b == 2) {
        stem_set->fhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(wkc));
    } else {
        stem_set->fhinsi_jitu = (NJ_UINT16)*wkc;
    }

    if (BIT_BHINSI(hdl)) {
        /* 後品詞 設定 */
        /* 後品詞位置(byte)算出 */
        bit_all = (NJ_UINT16)(TERM_BIT + flg_bit + BIT_HINDO_LEN(hdl) + BIT_FHINSI(hdl));
        pos = (NJ_UINT16)(bit_all >> 3);
        data = (NJ_UINT16)(NJ_INT16_READ(stem_data + pos));

        /* 後品詞位置(bit)算出 */
        j = (NJ_UINT16)(bit_all & 0x0007);

        stem_set->bhinsi = GET_BITFIELD_16(data, j, BIT_BHINSI(hdl));
    } else {
        stem_set->bhinsi = 0;
    }
    /* 実品詞への変換：品詞番号変換用テーブルの該当位置 */
    wkc = (NJ_UINT8*)(HINSI_NO_TOP_ADDR(hdl)
                      + (b * (FHINSI_NO_CNT(hdl) + (NJ_UINT16)(stem_set->bhinsi))));
    /* 品詞番号設定 */
    if (b == 2) {
        stem_set->bhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(wkc));
    } else {
        stem_set->bhinsi_jitu = (NJ_UINT16)*wkc;
    }

    /* 表記バイト長チェック */
    if (check != 1) {
        /* 表記バイト長設定 */
        /* 表記バイト長位置(byte)算出 */
        bit_all = (NJ_UINT16)(TERM_BIT + flg_bit + 
                              BIT_HINDO_LEN(hdl) + 
                              BIT_FHINSI(hdl) + 
                              BIT_BHINSI(hdl));
        pos = (NJ_UINT16)(bit_all >> 3);
        data = (NJ_UINT16)(NJ_INT16_READ(stem_data + pos));

        /* 表記バイト長位置(bit)算出 */
        j = (NJ_UINT16)(bit_all & 0x0007);
        bit = BIT_CANDIDATE_LEN(hdl);
        /* 表記バイト長取得 */
        stem_set->candidate_size = GET_BITFIELD_16(data, j, bit);
        bit_all += bit;
    }
    /* 読みバイト長チェック */
    if (check == 0) {
        stem_set->yomi_size = 0;

        /* 読み取得可能辞書であれば、読みバイト長取得する */
        if (APPEND_YOMI_FLG(hdl) && STEM_TERMINETER(stem_data)) {
            /* append_yomi指定あり、ターミネータONの場合は、
             * 読みバイト長が格納されているので取得する */
            pos = (NJ_UINT16)(bit_all >> 3);
            data = (NJ_UINT16)(NJ_INT16_READ(stem_data + pos));

            /* 読みバイト長位置(bit)算出 */
            j = (NJ_UINT16)(bit_all & 0x0007);
            bit = BIT_YOMI_LEN(hdl);
            /* 読みバイト長取得 */
            stem_set->yomi_size = GET_BITFIELD_16(data, j, bit);
            bit_all += bit;

            /* 読みデータ領域までのバイト数を算出 */
            /* バイト補正した後,表記長のバイト数加算 */
            dpos = GET_BIT_TO_BYTE(bit_all);
            dpos += stem_set->candidate_size;
            
        } else if (APPEND_YOMI_FLG(hdl)) {
            /* append_yomi指定あり、ターミネータOFFの場合,
             * 読み長データを取得するため、ターミネータONの
             * 単語まで移動する */
            while (!(STEM_TERMINETER(stem_data))) {
                next = get_stem_next(hdl, stem_data);
                stem_data += next;
            }
            /* 読み長,読みデータ領域までのバイト数取得 */
            dpos = get_stem_yomi_data(hdl, stem_data, stem_set);
        }

        if (stem_set->yomi_size) {
           /* 読み文字列バイト長を取得 */
            stem_set->yomi_size = get_stem_yomi_size(hdl, stem_data + dpos, stem_set->yomi_size);
        }
    }
}


/**
 * 圧縮辞書データ領域の表記文字列情報を取得
 *
 * @param[in]  hdl       辞書ハンドル
 * @param[in]  stem_data STEMデータ領域
 * @param[out] stem_set  STEMデータ設定
 *
 * @return なし
 */
static void get_stem_cand_data(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data, STEM_DATA_SET *stem_set) {
    NJ_UINT8 flg_bit;
    NJ_UINT16 data;
    NJ_UINT16 pos, j, bit_all;
    NJ_UINT16 cand_bit, yomi_bit;


    /* 辞書種別を見て処理分ける */
    flg_bit = BIT_MUHENKAN_LEN(hdl);
    if (NJ_GET_DIC_FMT(hdl) != NJ_DIC_FMT_KANAKAN) {
        flg_bit++;  /* 正規化フラグの分を加算 */
    }

    /* 表記バイト長設定 */
    /* 表記バイト長位置(byte)算出 */
    bit_all = (NJ_UINT16)(TERM_BIT + flg_bit + 
                          BIT_HINDO_LEN(hdl) + 
                          BIT_FHINSI(hdl) + 
                          BIT_BHINSI(hdl));
    pos = (NJ_UINT16)(bit_all >> 3);
    data = (NJ_UINT16)(NJ_INT16_READ(stem_data + pos));

    /* 表記バイト長位置(bit)算出 */
    cand_bit = BIT_CANDIDATE_LEN(hdl);
    j = (NJ_UINT16)(bit_all & 0x0007);
    /* 表記バイト長取得 */
    stem_set->candidate_size = GET_BITFIELD_16(data, j, cand_bit);
    bit_all += cand_bit;

    /* 読みバイト長設定 */
    if (APPEND_YOMI_FLG(hdl) && STEM_TERMINETER(stem_data)) {
        /* 読みデータが存在する場合,読みバイト長ビットサイズを加算 */
        yomi_bit = BIT_YOMI_LEN(hdl);
        bit_all += yomi_bit;
    }

    /* STEM領域バイト長 設定 */
    stem_set->stem_size = GET_BIT_TO_BYTE(bit_all);
}


/**
 * 圧縮辞書データ領域の読み長,読みデータ領域までのバイト数取得
 *
 * @param[in]  hdl       辞書ハンドル
 * @param[in]  stem_data STEMデータ領域
 * @param[out] stem_set  STEMデータ設定
 *
 * @return  読みデータ領域までのバイト数
 */
static NJ_UINT16 get_stem_yomi_data(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data,STEM_DATA_SET *stem_set) {
    NJ_UINT16 flg_bit;
    NJ_UINT16 data;
    NJ_UINT16 cand_bit, yomi_bit;
    NJ_UINT16 pos, j, bit_all;
    NJ_UINT16 yomi_pos;
    NJ_UINT16 candidate_size;


    /* 辞書種別を見て処理分ける */
    flg_bit = BIT_MUHENKAN_LEN(hdl);
    if (NJ_GET_DIC_FMT(hdl) != NJ_DIC_FMT_KANAKAN) {
        flg_bit++;  /* 正規化フラグの分を加算 */
    }

    /* 表記バイト長算出 */
    /* 表記バイト長位置算出 */
    bit_all = (NJ_UINT16)(TERM_BIT + flg_bit + BIT_HINDO_LEN(hdl) + 
                          BIT_FHINSI(hdl) + BIT_BHINSI(hdl));
    pos = (NJ_UINT16)(bit_all >> 3);
    data = (NJ_UINT16)(NJ_INT16_READ(stem_data + pos));

    /* 表記バイト長位置(bit)算出 */
    j = (NJ_UINT16)(bit_all & 0x0007);

    cand_bit = BIT_CANDIDATE_LEN(hdl);
    candidate_size = GET_BITFIELD_16(data, j, cand_bit);

    /* 表記バイト長ビット分,加算 */
    bit_all += cand_bit;

    /* 読みバイト長ビット分,加算 */
    if (APPEND_YOMI_FLG(hdl) && STEM_TERMINETER(stem_data)) {
        /* 読みバイト長のデータが存在する場合 */
        /* 読みバイト長位置(byte)算出 */
        pos = (NJ_UINT16)(bit_all >> 3);
        data = (NJ_UINT16)(NJ_INT16_READ(stem_data + pos));

        /* 読みバイト長位置(bit)算出 */
        j = (NJ_UINT16)(bit_all & 0x0007);
        yomi_bit = BIT_YOMI_LEN(hdl);
        /* 読みバイト長取得 */
        stem_set->yomi_size = GET_BITFIELD_16(data, j, yomi_bit);
        bit_all += yomi_bit;
    } else {
        stem_set->yomi_size = 0;  /* 読みデータ存在なし時,読み長0 */ /*NCH_FB*/
    }

    /* 読みデータ領域までのバイト数を算出 */
    /* バイト補正した後,表記バイト長分を加算 */
    yomi_pos = GET_BIT_TO_BYTE(bit_all);
    yomi_pos += candidate_size;

    return yomi_pos;
}


/**
 * 圧縮辞書データ領域の読み文字列長取得
 *
 * @param[in]  hdl       辞書ハンドル
 * @param[in]  ydata     圧縮辞書データ領域 読みデータ領域
 * @param[in]  yomi_size  読みデータバイト長
 *
 * @return  読み文字列バイト長
 */
static NJ_UINT16 get_stem_yomi_size(NJ_DIC_HANDLE hdl, NJ_UINT8 *ydata, NJ_UINT16 yomi_size) {
    NJ_INT16 ytbl_cnt;
    NJ_INT8 ysize;
    NJ_UINT8 *ytbl_top;
    NJ_UINT8 *ytbl;
    NJ_UINT8 yidx;
    NJ_UINT16 i;
    NJ_UINT16 len;


    /* 読み変換テーブルのデータ取得 */
    ytbl_cnt = YOMI_INDX_CNT(hdl);

    if (ytbl_cnt) {
    ysize = YOMI_INDX_SIZE(hdl); /* 読み変換用テーブル長(byte)   */
    ytbl_top = YOMI_INDX_TOP_ADDR(hdl);

        len = 0;
        for (i = 0; i < yomi_size; i++) {
            if (ysize == 2) {
                /* 読みインデックス取得 */
                yidx = *(ydata+i);
                ytbl = ytbl_top + ((yidx-1) * ysize);
                len += UTL_CHAR(ytbl);
                
            } else {
                /* 1文字列長可算 (SJIS only) */
                len++;
            }
        }
        /* len には文字配列長が求まるので、バイト長に変換する */
        return len * sizeof(NJ_CHAR);
    } else {
        /* 読み変換テーブルがない場合は、読みデータバイト長そのままとなる */
        return yomi_size;
    }
}


/**
 * 圧縮辞書データ領域の読み文字列取得
 *
 * @param[in]  hdl        辞書ハンドル
 * @param[in]  stem_data  STEMデータ領域
 * @param[out] yomi       読み文字列
 * @param[in]  yomi_pos   読みデータまでのバイト数
 * @param[in]  yomi_size  読みデータバイト長
 * @param[in]  size       バッファサイズ
 *
 * @return 読み文字列長(読み文字列バイト長がバッファサイズを超える場合は、バッファサイズを返す)
 */
static NJ_UINT16 get_stem_yomi_string(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data, NJ_CHAR *yomi, NJ_UINT16 yomi_pos, NJ_UINT16 yomi_size, NJ_UINT16 size) {
    NJ_INT16 ytbl_cnt;
    NJ_INT8 ysize;
    NJ_UINT8 *ytbl_top, *ytbl;
    NJ_UINT8 *ydata;
    NJ_UINT8 yidx;
    NJ_UINT16 i;
    NJ_UINT16 copy_len;
    NJ_UINT16 char_len;


    /* 読み変換テーブルのデータ取得 */
    ytbl_cnt = YOMI_INDX_CNT(hdl);
    ysize    = YOMI_INDX_SIZE(hdl);      /* 読み変換用テーブル長(byte)   */
    ytbl_top = YOMI_INDX_TOP_ADDR(hdl);

    /* 読みデータ領域のバイト位置セット */
    ydata = stem_data + yomi_pos;

    if (ytbl_cnt) {
        copy_len = 0;
        for (i = 0; i < yomi_size; i++) {
            /* 読みインデックス取得 */
            yidx = *(ydata + i);
            ytbl = ytbl_top + ((yidx - 1) * ysize);
            if (ysize == 2) {
                /* 読み文字インデックス登録文字が2byteの場合 */
                char_len = UTL_CHAR(ytbl); /* １文字の文字配列要素数 */
                if (((copy_len + char_len + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) {
                    /* ターミネートを含み、サイズオーバーの場合、
                     * サイズ最大値を返しエラーとする */
                    return size;
                }
                while (char_len > 0) {
                    NJ_CHAR_COPY(yomi + copy_len, ytbl);
                    copy_len++;
                    char_len--;
                    ytbl += sizeof(NJ_CHAR);
                }
            } else {
                /* 読み文字インデックス登録文字が1byteの場合 (SJIS版のみ) */
                if (((copy_len + 1 + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) {
                    /* ターミネートを含み、サイズオーバーの場合、
                     * サイズ最大値を返しエラーとする */
                    return size; /*NCH_FB*/
                }
                /* 1バイトコピー */
                *(yomi + copy_len) = (NJ_CHAR)(*ytbl);
                copy_len++;
            }
        }
    } else {
        /*
         * 読み変換テーブルがない場合、そのままコピーする。
         */
        if ((yomi_size + (NJ_TERM_LEN * sizeof(NJ_CHAR))) > size) {
            /* ターミネートを含み、サイズオーバーの場合、
             * サイズ最大値を返しエラーとする */
            return size; /*NCH_FB*/
        }
        /* アライメントを考慮して、1byteずつコピー */
        nj_memcpy((NJ_UINT8*)yomi, ydata, yomi_size);
        copy_len = yomi_size / sizeof(NJ_CHAR);
    }

    /* 読み文字列を NULLでターミネートする */
    *(yomi + copy_len) = NJ_CHAR_NUL;

    /* 読み文字列長を返す */
    return copy_len;
}


/**
 * 検索文字列と無変換候補の候補文字列を比較し、比較結果を返す。
 *
 * 無変換候補の候補文字列長は引数 len にセットされる。
 *
 * @attention
 *   ret = 0 以外の時は、len の値は不定とする。
 *
 * @param[in]  hdl      辞書ハンドル
 * @param[in]  key      検索文字列
 * @param[in]  key_len  検索文字列長
 * @param[in]  ydata    無変換候補の読みデータ領域アドレス
 * @param[in]  yomi_size 無変換候補の読みバイト長
 * @param[in]  muhenkan 無変換候補の無変換フラグ
 * @param[out] len      無変換候補の候補文字配列長
 *
 * @retval 1  key > ydata
 * @retval 0  key = ydata
 * @retval -1 key < ydata
 */
static NJ_INT16 str_muhenkan_cmp(NJ_UINT8 *hdl,
                                 NJ_CHAR *key, NJ_UINT8 key_len,
                                 NJ_UINT8 *ydata, NJ_UINT8 yomi_size,
                                 NJ_UINT8 muhenkan, NJ_UINT16 *len) {
    NJ_INT16 ytbl_cnt;
    NJ_INT8  ysize;
    NJ_UINT8 *ytbl_top, *ytbl;
    NJ_UINT8 yidx;
    NJ_CHAR *wp;
    NJ_CHAR  kbuf[NJ_MAX_CHAR_LEN + NJ_TERM_LEN]; /* 1文字2バイト + NJ_TERM_LEN */
    NJ_CHAR  ybuf[NJ_MAX_CHAR_LEN + NJ_TERM_LEN]; /* 1文字2バイト + NJ_TERM_LEN */
    NJ_UINT16 cnt;
    NJ_UINT16 i, char_len;


    /* 読み変換テーブルのデータ取得 */
    ytbl_cnt = YOMI_INDX_CNT(hdl);
    ysize    = YOMI_INDX_SIZE(hdl);
    ytbl_top = YOMI_INDX_TOP_ADDR(hdl);

    cnt = 0;
    *len = 0;
    if (ytbl_cnt == 0) {
        /* 読み変換テーブルがない場合 */

        /* 検索文字列長分ループする */
        while (key_len > *len) { /*NCH_FB*/
            /* 比較したデータ数が、辞無変換候補の読み文字列長を
             * 越えた場合は、抜ける */
            if (cnt >= yomi_size) { /*NCH_FB*/
                break; /*NCH_FB*/
            }

            /* ydata から比較対象文字の先頭１文字を取得 */
            char_len = UTL_CHAR(ydata); /*NCH_DEF*/
            if (muhenkan) { /*NCH_DEF*/
                /* アライメントを考慮してbufに1文字コピー */
                for (i = 0; i < char_len; i++) { /*NCH_DEF*/
                    NJ_CHAR_COPY(&ybuf[i], &ydata[i * sizeof(NJ_CHAR)]); /*NCH_DEF*/
                }
                ybuf[i] = NJ_CHAR_NUL; /*NCH_DEF*/
                /* カタカナ候補ならカタカナに復元しておく */
                nje_convert_hira_to_kata(ybuf, kbuf, 1); /*NCH_DEF*/
                char_len = UTL_CHAR(kbuf); /*NCH_DEF*/
            } else {
                /* アライメントを考慮してbufに1文字コピー */
                for (i = 0; i < char_len; i++) { /*NCH_DEF*/
                    NJ_CHAR_COPY(&kbuf[i], &ydata[i * sizeof(NJ_CHAR)]); /*NCH_DEF*/
                }
                kbuf[i] = NJ_CHAR_NUL; /*NCH_DEF*/
            }

            /* key と１文字比較する */
            for (i = 0; i < char_len; i++) { /*NCH_DEF*/
                if (*key != kbuf[i]) { /*NCH_DEF*/
                    /* 一致しない場合は差分値を計算 */
                    return NJ_CHAR_DIFF(key, (kbuf + i)); /*NCH_DEF*/
                }
                key++; /*NCH_DEF*/
                cnt += sizeof(NJ_CHAR); /*NCH_DEF*/
                (*len)++; /*NCH_DEF*/
                if ((key_len <= *len) || (cnt >= yomi_size)) { /*NCH_DEF*/
                    break;
                }
            }
        }
    } else {
        /* 読み変換テーブルがある場合 */

        /* 検索文字列長分ループする */
        while (key_len > *len) {
            /* 比較したデータ数が、辞無変換候補の読み文字列長を越えた場合は、抜ける */
            if (cnt >= yomi_size) {
                break;
            }
            /* 読みインデックスから読み１文字取得 */
            yidx = *(ydata + cnt);
            ytbl = (ytbl_top + ((yidx-1) * ysize));

            if (ysize == 2) {
                /* 読み文字インデックス登録文字が2byteの場合 */
                char_len = UTL_CHAR(ytbl); /* １文字の文字配列要素数 */
                for (i = 0; i < char_len; i++) {
                    NJ_CHAR_COPY(&ybuf[i], &ytbl[i * sizeof(NJ_CHAR)]);
                }
                ybuf[i] = NJ_CHAR_NUL;
            } else {
                /* 読み文字インデックス登録文字が1byteの場合 (SJIS版のみ) */
                ybuf[0] = *ytbl;
                ybuf[1] = NJ_CHAR_NUL;
                char_len = 1;
            }
            /* 無変換フラグ On の場合,カタカナに変換 */
            if (muhenkan) {
                /* カタカナ候補ならカタカナに復元しておく */
                nje_convert_hira_to_kata(ybuf, kbuf, 1);
                char_len = UTL_CHAR(kbuf);
                wp = kbuf;
            } else {
                wp = ybuf;
            }
            /* key と１文字比較する */
            for (i = 0; i < char_len; i++) {
                if (*key != wp[i]) {
                    /* 一致しない場合は差分値を計算 */
                    return NJ_CHAR_DIFF(key, (wp + i));
                }
                key++;
                (*len)++;
                if (key_len <= *len) {
                    break;
                }
            }
            cnt++;
        }
    }
    
    if (key_len != *len) {
        /* 包含関係。検索文字列が無変換候補の候補文字列
         * より長い場合
         * 例) 検索文字「ほうほう」 無変換候補「ほう」 */
        return 1;
    }
    if (yomi_size != cnt) {
        /* 検索文字列が無変換候補の候補文字列より短い場合 */
        /* 例) 検索文字「アイ」 無変換候補「アイス」 */
        *len = key_len + 1;
        return 0;
    }
    return 0;
}


/**
 * 探索木から検索条件（読み）に該当するノードを検索し、該当する候補データのオフセットを取得
 *
 * @param[in] condition 検索条件
 * @param[in] loctset   検索位置
 *
 * @retval 0   検索候補なし
 * @retval 1   検索候補あり
 */
static NJ_INT16 search_node(NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset) {
    NJ_UINT8 *root, *now, *node, *node_mid;
    NJ_UINT8 index;
    NJ_UINT8 *byomi;
    NJ_UINT8 *wkc;
    NJ_UINT8 idx_no;
    NJ_INT16 idx;
    NJ_INT16 char_size;
    NJ_INT16 left, right, mid;  /* binary search用index */
    NJ_INT16 ytbl_cnt;
    NJ_UINT16 c, d;
    NJ_UINT8  c1 = 0, c2 = 0;
    NJ_UINT16 y;
    NJ_UINT16 ysize = (condition->ylen * sizeof(NJ_CHAR));
    NJ_UINT8 *ytbl_top;
    NJ_UINT16 idx_cnt;
    NJ_UINT16 nd_index;
    NJ_UINT16 bit_left, bit_data;
    NJ_UINT32 data_offset;
    NJ_UINT16 data;
    NJ_UINT16 pos, j, bit_all, bit_tmp, bit_idx;
    NJ_UINT32 data_l;
    NJ_UINT8 restart_flg = 0;
    NJ_UINT8 bottom_flg = 0;
    NJ_UINT8 *data_top, *stem_data;
    NJ_INT16 hindo, hindo_max;
    NJ_UINT32 current,hindo_max_data, bottom, next;


    node = NULL;        /* 検索されたノード */

    byomi = (NJ_UINT8*)(condition->yomi); /* 検索対象読み文字列 */

    /* TRIEのルートから検索を開始する */
    root = NODE_AREA_TOP_ADDR(loctset->loct.handle);

    /* TRIEの中間地点 */
    node_mid = root + NODE_AREA_MID_ADDR(loctset->loct.handle);
    now = node_mid;

    /* インデックスNO初期化 */
    idx_no = 0;
    idx_cnt = 1;

    /*
     * left, data ビット数取得
     */
    bit_left = BIT_NODE_AREA_LEFT_LEN(loctset->loct.handle);
    bit_data = BIT_NODE_AREA_DATA_LEN(loctset->loct.handle);

    /*
     * 読みインデックス
     */
    ytbl_cnt = YOMI_INDX_CNT(loctset->loct.handle);
    y = YOMI_INDX_SIZE(loctset->loct.handle);    /* 読み変換用テーブル長(byte)   */
    ytbl_top = YOMI_INDX_TOP_ADDR(loctset->loct.handle);
    
    /* 圧縮辞書インデックス領域の終端アドレスである
     * 圧縮辞書データ領域の先頭アドレスを取得 */
    data_top = STEM_AREA_TOP_ADDR(loctset->loct.handle);

    /* 前方一致検索で、空文字列指定時 */
    if ((condition->operation == NJ_CUR_OP_FORE) &&
        NJ_CHAR_STRLEN_IS_0(condition->yomi)) {

        /* ysize = 0 に設定し、以下の while (ysize > 0)の
         * ループを通らないようにする */
        ysize = 0;

        /* 検索位置をルートノードに設定しておく */
        node = root;
    }

    /* 検索対象読み文字列を全て検索する */
    while (ysize > 0) {
        if (ytbl_cnt != 0) {
            /*
             * 読みインデックスが存在する場合は、
             * 読み文字コードをインデックスに変換
             */
            char_size = UTL_CHAR(byomi) * sizeof(NJ_CHAR);

            if (char_size == 2) {        /* 読み文字：2byte */
                if (y == 1) {
                    /* 読み変換テーブル幅が 1byte ならば
                     * 読み文字 2byteの検索候補はない */
                    loctset->loct.status = NJ_ST_SEARCH_END_EXT; /*NCH_DEF*/
                    return 0; /*NCH_DEF*/   /* 検索候補無し */
                }
                c1 = *byomi;
                c2 = *(byomi + 1);
                c = (NJ_UINT16)((c1 << 8) | c2);
            } else {                    /* 読み文字：1byte */
                /* 1byte文字は前に詰めて比較する  (SJIS only) */
                c1 = *byomi;
                c2 = 0x00;
                c = (NJ_UINT16)(*byomi);
            }

            idx = -1;
            left = 0;                                   /* 読み変換用テーブル先頭位置   */
            right = ytbl_cnt;   /* 読み変換用テーブル最後尾位置 */

            if (y == 2) {
                while (left <= right) {
                    mid = (left + right) >> 1;
                    wkc = ytbl_top + (mid << 1);

                    if (c1 == *wkc) {
                        if (c2 == *(wkc + 1)) {
                            idx = (NJ_UINT16)(mid + 1);
                            break;
                        }
                        if (c2 < *(wkc + 1)) {
                            right = mid - 1;
                        } else {
                            left = mid + 1;
                        }
                    } else if (c1 < *wkc) {
                        right = mid - 1;
                    } else {
                        left = mid + 1;
                    }
                }
            } else {
                while (left <= right) {
                    mid = (left + right) >> 1;
                    wkc = (ytbl_top + (mid * y));
                    d = (NJ_UINT16)(*wkc);
                    if (c == d) {
                        idx = (NJ_UINT16)(mid + 1);
                        break;
                    }
                    if (c < d) {
                        right = mid - 1;
                    } else {
                        left = mid + 1;
                    }
                }
            }

            if (idx < 0) {
                loctset->loct.status = NJ_ST_SEARCH_END_EXT;
                return 0;       /* 検索候補無し */
            }
            index = (NJ_UINT8)idx;
        } else {
            index = *byomi;
            char_size = 1;       /* 1byte文字コードとみなす      */
        }

        byomi += char_size;       /* 次検索読み文字をセット       */
        ysize -= char_size;

        /*
         * 読み文字列から取得したインデックスと一致する
         * インデックスを持つノードを検索する。
         */
        while (now < data_top) {
            if (NODE_IDX_EXIST(now)) {
                bit_idx = 8;
                idx_cnt = NODE_IDX_CNT(now);
            } else {
                bit_idx = 4;
                idx_cnt = 1;
            }
            bit_all = bit_idx;

            /* left の bit数取得 */
            if (NODE_LEFT_EXIST(now)) {
                bit_all += bit_left;
            }

            /* data の bit数取得 */
            if (NODE_DATA_EXIST(now)) {
                bit_all += bit_data;
            }
            /* 後のノードサイズ算出用に退避しておく */
            bit_tmp = bit_all;

            /* 指定されたインデックス番号分ビット加算 (idx_no * 8) */
            bit_all += (NJ_UINT16)(idx_no << 3);

            /* index の 位置(byte)算出 */
            pos = (NJ_UINT16)(bit_all >> 3);
            /* 取得したいインデックスの手前の byte位置 */
            data = (NJ_UINT16)(NJ_INT16_READ(now + pos));

            /* index の 位置(bit)算出 */
            j = (NJ_UINT16)(bit_all & 0x0007);
            
            nd_index = GET_BITFIELD_16(data, j, INDEX_BIT);
            if (index == (NJ_UINT8)nd_index) {
                /* インデックス（文字）が一致していれば、次の文字の処理に移る */
                break;
            } else {
                if ((!NODE_TERM(now)) && (index > (NJ_UINT8)nd_index) && (idx_no == 0)) {
                    /* 次のノード（右ノード）へ移り、同じ文字で比較を継続する */
                    now += GET_BIT_TO_BYTE(bit_tmp + (idx_cnt * 8));
                    if (now == node_mid) {
                        loctset->loct.status = NJ_ST_SEARCH_END_EXT;
                        return 0;
                    }
                    continue;   /* 移動先のノードと比較 */
                } else {
                    if ((now == node_mid) && (restart_flg == 0) &&
                        (index < (NJ_UINT8)nd_index) && (idx_no == 0) &&
                        (root != node_mid)) {
                        now = root;
                        idx_no = 0;
                        restart_flg = 1;
                        continue;       /* 移動先のノードと比較 */
                    }
                    loctset->loct.status = NJ_ST_SEARCH_END_EXT;
                    return 0;
                }
            }
        }

        /*
         * 複数インデックスで、比較すべきインデックスがまだある場合は、次のインデックスへ移る
         * (idx_cnt が 1 の場合・・・インデックスがない場合）
         */
        if (/* NODE_IDX_EXIST(now) && */ (idx_cnt > (NJ_UINT16)(idx_no + 1))) {
            if (ysize == 0) {
                /*
                 * ysize が 0 になっているのに idx_no が idx_cnt を使い果たしていない場合は、
                 * 辞書の見出し語のほうが長い場合なので、「部分一致」という結果を返す
                 */
                if (condition->operation == NJ_CUR_OP_FORE) {
                    /* 前方一致検索の場合は該当単語となる */
                    node = now;
                    break;
                }
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;   /* 検索候補なし */
            }
            /*
             * ysize も残っている場合は、同一辞書インデックス上の次の文字に移行する
             */
            idx_no++;
            continue;
        }
        /*
         * インデックスを比較して一致した場合、左方向ノード（次の文字）に移動 
         */
        node = now;     /* 一致したノードを保存する */
        idx_no = 0;     /* インデックス番号クリア */

        if (ysize == 0) {
            /*
             * 全ての読み文字列を検索している場合は、
             * ノードの検索を完了とする。
             */
            break;
        } else {
            if (!(NODE_LEFT_EXIST(now))) {
                loctset->loct.status = NJ_ST_SEARCH_END_EXT;
                return 0;       /* 検索候補なし */
            }
        }

        /*
         * 左方向のノードを取得し、ノードの検索を継続する
         */
        if (NODE_IDX_EXIST(now)) {
            bit_idx = 8;
        } else {
            bit_idx = 4;
        }
        pos = (NJ_UINT16)(bit_idx >> 3);
        data_l = (NJ_UINT32)(NJ_INT32_READ(now + pos));

        /* index 位置(bit)算出 */
        j = (NJ_UINT16)(bit_idx & 0x0007);

        now += GET_BITFIELD_32(data_l, j, bit_left);
    }

    /* 検索位置を now に格納しておく */
    now = node; 

    /* node が NULL もしくは node にデータが存在しない場合 */
    if ((node == NULL) || !(NODE_DATA_EXIST(node))) {

        if ((condition->operation == NJ_CUR_OP_FORE) && 
            (node != NULL)) {
            /*
             * 前方一致検索の場合はデータポインタが存在する
             * 位置までノードを左方向に移動する
             */
            while (!NODE_DATA_EXIST(node)) {
                if (!(NODE_LEFT_EXIST(node))) {
                    /* 左方向のノードが存在しない場合は
                     *  検索候補なし */
                    loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_DEF*/
                    return 0; /*NCH_DEF*/   /* 検索候補なし */
                }
                
                /*
                 * 左方向のノードを取得し、ノードの検索を継続する
                 */
                if (NODE_IDX_EXIST(node)) {
                    bit_idx = 8;
                } else {
                    bit_idx = 4;
                }
                pos = (NJ_UINT16)(bit_idx >> 3);
                data_l = (NJ_UINT32)(NJ_INT32_READ(node + pos));
                
                /* index 位置(bit)算出 */
                j = (NJ_UINT16)(bit_idx & 0x0007);
                node += GET_BITFIELD_32(data_l, j, bit_left);
            }
        } else {
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;   /* 検索候補なし */
        }
    }

    /*
     * 全ての読み文字列に該当するノード列が見つかった場合、
     * ノード列の最終ノードのデータポインタを検査する。
     */
    if (NODE_IDX_EXIST(node)) {
        bit_idx = 8;
    } else {
        bit_idx = 4;
    }

    /* left フィールドのbit 数を加算 */
    if (NODE_LEFT_EXIST(node)) {
        bit_all = bit_idx + bit_left;
    } else {
        bit_all = bit_idx;
    }

    pos = (NJ_UINT16)(bit_all >> 3);
    data_l = (NJ_UINT32)(NJ_INT32_READ(node + pos));
    
    /* data の値を取得 */
    j = (NJ_UINT16)(bit_all & 0x0007);
    data_offset = GET_BITFIELD_32(data_l, j, bit_data);

    /*
     * データポインタ(同音異義語ブロックの先頭)を検索位置(loctset->loct.top)に設定し、
     * 先頭の同音異義語データを指すことを示すloctset->loct.current=0を設定する。
     */
    loctset->loct.top = data_offset;
    loctset->loct.current = 0;

    /*
     * 前方一致検索の場合は,検索候補の範囲(終端位置)を調べて
     * loctset->loct.bottom に 設定する
     */
    if (condition->operation == NJ_CUR_OP_FORE) {
        /* bottom を 検索候補の先頭位置(top)で初期化しておく */
        bottom = loctset->loct.top;

        if (NJ_CHAR_STRLEN_IS_0(condition->yomi)) {
            /* 検索文字列に空文字指定して前方一致検索の場合。
             * 中間ノード地点を起点とし、終端位置を探す */
            node = node_mid;

        } else {
            /* 検索文字列指定あり時の前方一致検索の場合 */
            node = now;
            if (NODE_LEFT_EXIST(node)) {
                /* まず一致したノード位置から １つleft方向
                 * に移動する 
                 */
                if (NODE_IDX_EXIST(node)) {
                    bit_all = 8;
                } else {
                    bit_all = 4;
                }
                
                pos = (NJ_UINT16)(bit_all >> 3);
                data_l = (NJ_UINT32)(NJ_INT32_READ(node + pos));
                
                /* left の値を取得 */
                j = (NJ_UINT16)(bit_all & 0x0007);
                node += GET_BITFIELD_32(data_l, j, bit_left);

            } else {
                /* 検索位置の left 方向にノードがない場合は、
                 * 先頭候補=最後候補となる */
                bottom_flg = 1;
            }
        }

        /* 上記処理で bottom が既に設定済みならば
         * 検索しない */
        if (!bottom_flg) {
            while (node < data_top) {
                /* ノードのターミネータがONかチェック */
                if (!NODE_TERM(node)) {
                    /* ターミネータ が OFF ならば 次ノードに移動する */
                    if (NODE_IDX_EXIST(node)) {
                        bit_all = 8; /*NCH_FB*/
                        idx_cnt = NODE_IDX_CNT(node); /*NCH_FB*/
                    } else {
                        bit_all = 4;
                        idx_cnt = 1;
                    }

                    /* left の bit数取得 */
                    if (NODE_LEFT_EXIST(node)) {
                        bit_all += bit_left;
                    }

                    /* data の bit数取得 */
                    if (NODE_DATA_EXIST(node)) {
                        bit_all += bit_data;
                    }

                    /* 次ノードに移動 */
                    node += GET_BIT_TO_BYTE(bit_all + (idx_cnt * 8));
                } else {
                    /* ターミネータがONならば left があるか見る */
                    if (!NODE_LEFT_EXIST(node)) {
                        /* left がない場合は、data があるか見る。 */
                        if (NODE_DATA_EXIST(node)) {
                            /* data はある場合は、そこが最終候補とする */
                            if (NODE_IDX_EXIST(node)) {
                                bit_all = 8;
                            } else {
                                bit_all = 4;
                            }

                            pos = (NJ_UINT16)(bit_all >> 3);
                            data_l = (NJ_UINT32)(NJ_INT32_READ(node + pos));
                            
                            /* data の値を取得 */
                            j = (NJ_UINT16)(bit_all & 0x0007);
                            data_offset = GET_BITFIELD_32(data_l, j, bit_data);
                            /* 最後の候補をセット */
                            bottom = data_offset;
                            break;
                        } else {
                            /* left も data もない場合は,あり得ないはず。
                             * エラーとする */
                            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_SEARCH_WORD, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
                        }

                    } else {
                        /* left があった場合,left 方向に移動する。*/
                        if (NODE_IDX_EXIST(node)) {
                            bit_all = 8;
                        } else {
                            bit_all = 4;
                        }

                        pos = (NJ_UINT16)(bit_all >> 3);
                        data_l = (NJ_UINT32)(NJ_INT32_READ(node + pos));

                        /* left の値を取得 */
                        j = (NJ_UINT16)(bit_all & 0x0007);

                        /* left 方向に移動 */
                        node += GET_BITFIELD_32(data_l, j, bit_left);
                    }
                }
            }
        }

        stem_data = data_top + bottom;

        /* 同音異議語がある場合に備えて,
         * 同音異議語終端(=ターミネータがON)まで移動。*/
        while (!(STEM_TERMINETER(stem_data))) {
            next = get_stem_next(loctset->loct.handle, stem_data);
            stem_data += next;
        }
        /* 終端位置(stem_data)の、先頭位置(data_top)からの
         * オフセットを bottom に設定する  */
        loctset->loct.bottom = (NJ_UINT32)(stem_data - data_top);

        /* top の頻度値取得 */
        stem_data = data_top + loctset->loct.top;

        hindo = (NJ_INT16) *((NJ_UINT8*)(HINDO_NO_TOP_ADDR(loctset->loct.handle)
                                          + get_stem_hindo(loctset->loct.handle, stem_data)));

        hindo_max = hindo;
        hindo_max_data = 0;

        if (condition->mode == NJ_CUR_MODE_FREQ) {

            /* 次のSTEMデータ設定 */
            j = get_stem_next(loctset->loct.handle, stem_data);
            current = j;
            stem_data += j;

            /* 
             * top 〜 bottom の範囲で最大頻度の候補を探す。
             */
            while (stem_data <= (data_top + loctset->loct.bottom)) {

                /* 頻度値取得 */
                hindo = (NJ_INT16) *((NJ_UINT8*)(HINDO_NO_TOP_ADDR(loctset->loct.handle)
                                                  + get_stem_hindo(loctset->loct.handle, stem_data)));

                /* 最大頻度値, 最大頻度データ位置 更新 */
                if (hindo > hindo_max) {
                    hindo_max = hindo;
                    hindo_max_data = current; 
                }

                /* 次のSTEMデータ設定 */
                j = get_stem_next(loctset->loct.handle, stem_data);
                current += j;
                stem_data += j;
            }
        }
        /* 辞書バージョンがver.3.0以外 */
        if (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION3) {
            loctset->cache_freq = CALCULATE_HINDO(hindo_max, loctset->dic_freq.base, 
                                                  loctset->dic_freq.high, COMP_DIC_FREQ_DIV);

        }
        loctset->loct.current = hindo_max_data;

    }

    return 1;   /* 検索候補あり */
}


/**
 * 次候補の検索を行う
 *
 * 次候補の情報はオフセットとして検索位置(loctset)に設定する
 *
 * @param[in]     condition 検索条件
 * @param[in,out] loctset   検索位置
 *
 * @retval 0   検索候補なし
 * @retval 1   検索候補あり
 */
static NJ_INT16 bdic_search_data(NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset) {
    NJ_UINT8 *data, *data_end;
    STEM_DATA_SET stem_set;
    NJ_INT16 i, current = 0;
    NJ_INT16 hindo;

    /* インライン展開用 */
    NJ_UINT8 flg_bit;
    NJ_UINT8 b;
    NJ_UINT16 pos, j, bit_all, hdata;
    NJ_UINT8 *wkc;


    data = STEM_AREA_TOP_ADDR(loctset->loct.handle);
    data += loctset->loct.top + loctset->loct.current;

    if (GET_LOCATION_STATUS(loctset->loct.status) != NJ_ST_SEARCH_NO_INIT) {
        /*
         * 初回の検索で無いとき、現カーソル位置で完全一致候補が
         * 終了かどうか検査
         */

        if (STEM_TERMINETER(data)) {
            /* 検索終了の属性を設定 */
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }

        /* STEMデータ設定 */
        i = get_stem_next(loctset->loct.handle, data);

        data += i;
        current += i;
    }

    if (NJ_GET_DIC_FMT(loctset->loct.handle) == NJ_DIC_FMT_KANAKAN) {
        data_end = loctset->loct.handle
            + NJ_DIC_COMMON_HEADER_SIZE 
            + NJ_INT32_READ(loctset->loct.handle + NJ_DIC_POS_DATA_SIZE)
            + NJ_INT32_READ(loctset->loct.handle + NJ_DIC_POS_EXT_SIZE) 
            - NJ_DIC_ID_LEN;
    } else {
        data_end = CAND_IDX_AREA_TOP_ADDR(loctset->loct.handle);
    }

    /*
     * ↓ get_stem_hinsi インライン展開 (前準備)
     */
    /* 共用エンジンの場合,辞書種別を見て処理分ける */
    flg_bit = BIT_MUHENKAN_LEN(loctset->loct.handle);
    if (NJ_GET_DIC_FMT(loctset->loct.handle) != NJ_DIC_FMT_KANAKAN) {
        flg_bit++;  /* 正規化フラグの分を加算 */
    }

    /* 品詞番号変換用テーブル上の１品詞のサイズ */
    b = HINSI_NO_BYTE(loctset->loct.handle);

    /*
     * ↑ get_stem_hinsi インライン展開 (前準備)
     */

    while (data < data_end) {
        /* 品詞情報を取得 */
        /*
         * ↓get_stem_hinsiのインライン展開 (本体)
         */
        if (condition->hinsi.fore != NULL) {
            if (BIT_FHINSI(loctset->loct.handle)) {
                /* 前品詞 設定 */
                /* 前品詞位置(byte)算出 */
                bit_all = (NJ_UINT16)(TERM_BIT + flg_bit + BIT_HINDO_LEN(loctset->loct.handle));
                pos = (NJ_UINT16)(bit_all >> 3);
                hdata = (NJ_UINT16)(NJ_INT16_READ(data + pos));

                /* 前品詞位置(bit)算出 */
                j = (NJ_UINT16)(bit_all & 0x0007);
                stem_set.fhinsi = GET_BITFIELD_16(hdata, j, BIT_FHINSI(loctset->loct.handle));
            } else {
                stem_set.fhinsi = 0;
            }

            /* 品詞番号変換用テーブルの該当位置 */
            wkc = (NJ_UINT8*)(HINSI_NO_TOP_ADDR(loctset->loct.handle) + (b * (NJ_UINT16)(stem_set.fhinsi)));
            /* 実品詞への変換：品詞番号設定 */
            if (b == 2) {
                stem_set.fhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(wkc));
            } else {
                stem_set.fhinsi_jitu = (NJ_UINT16)*wkc;
            }
        } else {
            stem_set.fhinsi = 0;
            stem_set.fhinsi_jitu = 0;
        }

        if (condition->hinsi.rear != NULL) {
            if (BIT_BHINSI(loctset->loct.handle)) {
                /* 後品詞 設定 */
                /* 後品詞位置(byte)算出 */
                bit_all = (NJ_UINT16)(TERM_BIT + flg_bit
                                      + BIT_HINDO_LEN(loctset->loct.handle)
                                      + BIT_FHINSI(loctset->loct.handle));
                pos = (NJ_UINT16)(bit_all >> 3);
                hdata = (NJ_UINT16)(NJ_INT16_READ(data + pos));

                /* 後品詞位置(bit)算出 */
                j = (NJ_UINT16)(bit_all & 0x0007);
                stem_set.bhinsi = GET_BITFIELD_16(hdata, j, BIT_BHINSI(loctset->loct.handle));
            } else {
                stem_set.bhinsi = 0;
            }

            /* 実品詞への変換：品詞番号変換用テーブルの該当位置 */
            wkc = (NJ_UINT8*)(HINSI_NO_TOP_ADDR(loctset->loct.handle)
                              + (b * (FHINSI_NO_CNT(loctset->loct.handle) + (NJ_UINT16)(stem_set.bhinsi))));
            /* 品詞番号設定 */
            if (b == 2) {
                stem_set.bhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(wkc));
            } else {
                stem_set.bhinsi_jitu = (NJ_UINT16)*wkc;
            }
        } else {
            stem_set.bhinsi = 0;
            stem_set.bhinsi_jitu = 0;
        }

        /*
         * ↑get_stem_hinsiのインライン展開 (本体)
         */

        /* 前品詞が検索条件と一致するかチェック */
        if (njd_connect_test(condition, stem_set.fhinsi_jitu, stem_set.bhinsi_jitu)) {
            /* 完全一致 */
            loctset->loct.status = NJ_ST_SEARCH_READY;
            loctset->loct.current += current;
            loctset->loct.current_info = CURRENT_INFO_SET;
            hindo = (NJ_INT16) *((NJ_UINT8*)(HINDO_NO_TOP_ADDR(loctset->loct.handle) + 
                                              get_stem_hindo(loctset->loct.handle, data)));
            /* 辞書バージョンがver.3.0以外 */
            if (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION3) {
                loctset->cache_freq = CALCULATE_HINDO(hindo, loctset->dic_freq.base, 
                                                      loctset->dic_freq.high, COMP_DIC_FREQ_DIV);

            /* 辞書バージョンがver.3.0 */
            } else {
                /* バイアス値CAND_FZK_HINDO_BIASを減算し、単語頻度として設定する */
                hindo -= CAND_FZK_HINDO_BIAS;
                loctset->cache_freq = CALCULATE_HINDO(hindo, loctset->dic_freq.base, 
                                                      loctset->dic_freq.high, COMP_DIC_FREQ_DIV);
                /* 辞書頻度ベース値より候補頻度が低い場合 */
                if (loctset->cache_freq < loctset->dic_freq.base) {
                    loctset->cache_freq = CALCULATE_HINDO_UNDER_BIAS(hindo, loctset->dic_freq.high - loctset->dic_freq.base);
                }
            }
            return 1;
        }
        if (STEM_TERMINETER(data)) {
            /* 次候補無し */
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }

        /* 次候補のアドレス取得 */
        i = get_stem_next(loctset->loct.handle, data);

        data += i;
        current += i;
    }
    /* 次候補無し */
    loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
    return 0; /*NCH_FB*/
}


/**
 * 前方一致検索における次候補検索を行う
 *
 * 次候補の情報はオフセットとして検索位置(loctset)に設定する。
 *
 * @param[in]     condition 検索条件
 * @param[in,out] loctset   検索位置
 *
 * @retval 0   検索候補なし
 * @retval 1   検索候補あり
 */
static NJ_INT16 bdic_search_fore_data(NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset) {
    NJ_UINT8 *data, *data_top, *bottom, *data_end;
    NJ_INT16 i = 0;
    NJ_INT16 hindo = 0;
    NJ_INT16 hindo_max = -1;
    NJ_UINT8 no_hit = 0;
    NJ_UINT32 current = loctset->loct.current;
    NJ_UINT8 *current_org;
    NJ_UINT32 hindo_data = 0;


    /* 初回検索時は そのまま抜ける */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        loctset->loct.status = NJ_ST_SEARCH_READY;
        loctset->loct.current_info = CURRENT_INFO_SET;
        return 1;
    }

    /* 圧縮辞書データ領域先頭取得 */
    data_top = STEM_AREA_TOP_ADDR(loctset->loct.handle);

    /* 検索単語の格納位置アドレスを取得 */
    data = data_top + loctset->loct.top + loctset->loct.current;

    /* 検索開始位置を退避しておく */
    current_org = data;

    /* 検索範囲終端のアドレスを取得。bottom に設定 */
    bottom = data_top + loctset->loct.bottom;

    if (NJ_GET_DIC_FMT(loctset->loct.handle) == NJ_DIC_FMT_KANAKAN) {
        data_end = loctset->loct.handle
            + NJ_DIC_COMMON_HEADER_SIZE 
            + NJ_INT32_READ(loctset->loct.handle + NJ_DIC_POS_DATA_SIZE)
            + NJ_INT32_READ(loctset->loct.handle + NJ_DIC_POS_EXT_SIZE) 
            - NJ_DIC_ID_LEN;
    } else {
        data_end = CAND_IDX_AREA_TOP_ADDR(loctset->loct.handle);
    }

    if (condition->mode == NJ_CUR_MODE_FREQ) {
        /* 頻度順検索の場合は、次に頻度が高い単語を取得する */

        /* 圧縮辞書データ領域の終端を超えた場合は抜ける */
        while (data < data_end) {
            /* 次のSTEMデータ設定 */
            i = get_stem_next(loctset->loct.handle, data);
            data += i;
            current += i;

            /* data 位置が bottom より大きい場合 */
            if (data > bottom) {
                /*
                 * 頻度値が0の状態で検索範囲の終端に達した場合,
                 * 検索終了とする */
                if (loctset->cache_freq == 0) {
                    /* 検索終了の属性を設定 */
                    loctset->loct.status = NJ_ST_SEARCH_END;
                    return 0;
                } else if (no_hit == 1) {
                    /* 検索終了の属性を設定 */
                    loctset->loct.status = NJ_ST_SEARCH_END;
                    return 0;
                }
                /* 頻度を -1 する */
                loctset->cache_freq -= 1;

                /* 検索候補の先頭に戻す */
                data = data_top + loctset->loct.top;
                current = 0;

                no_hit = 1;
            }

            /* 検索開始位置まで見終わった場合 */
            if ((hindo_max != -1) && (data == current_org)) {
                /* 頻度値が一致するものが無かったら     
                 * その頻度値以下で最大頻度の候補を返す */
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->loct.current = hindo_data;
                loctset->cache_freq = hindo_max;
                return 1;
            }

            /* 頻度値を取得する */
            hindo = (NJ_INT16) *((NJ_UINT8*)(HINDO_NO_TOP_ADDR(loctset->loct.handle) + get_stem_hindo(loctset->loct.handle, data)));
            /* 辞書バージョンがver.3.0以外 */
            if (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION3) {
                /* 辞書頻度を元に評価値を算出 */
                hindo = CALCULATE_HINDO(hindo, loctset->dic_freq.base, 
                                        loctset->dic_freq.high, COMP_DIC_FREQ_DIV);

            }

            /* 検索中頻度と等しい場合, その検索候補位置を返す */
            if (hindo == loctset->cache_freq) {
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->loct.current = current;
                return 1;
            }

            if (hindo < loctset->cache_freq) {
                if (((hindo == hindo_max) && (current < hindo_data)) || 
                    (hindo > hindo_max)) {
                    hindo_max = hindo;
                    hindo_data = current;
                }
            }
        }
    } else {
        /* 読み順検索の次のSTEMデータを取り出す。*/

        /* 次のSTEMデータ設定 */
        i = get_stem_next(loctset->loct.handle, data);
        data += i;
        current += i;

        /* data 位置が bottom より大きい場合 */
        if (data > bottom) {
            /* 検索終了の属性を設定 */
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }

        /* 頻度値を取得する */
        hindo = (NJ_INT16) *((NJ_UINT8*)(HINDO_NO_TOP_ADDR(loctset->loct.handle)
                                         + get_stem_hindo(loctset->loct.handle, data)));
        /* 辞書バージョンがver.3.0以外 */
        if (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION3) {
            loctset->cache_freq = CALCULATE_HINDO(hindo, loctset->dic_freq.base, 
                                                  loctset->dic_freq.high, COMP_DIC_FREQ_DIV);

        }
        loctset->loct.status = NJ_ST_SEARCH_READY;
        loctset->loct.current_info = CURRENT_INFO_SET;
        loctset->loct.current = current;
        return 1;
    }
    /* 検索終了の属性を設定 */
    loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
    return 0; /*NCH_FB*/
}


/**
 * 表記文字列インデックスから検索条件（候補）に該当する候補データのオフセットを取得
 *
 * @param[in]  condition   検索条件
 * @param[in]  loctset     検索位置
 *
 * @retval 0   検索候補なし
 * @retval 1   検索候補あり
 */
static NJ_INT16 search_index(NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset) {
    NJ_INT16 ret;
    NJ_UINT8 *data;
    NJ_UINT8 *data_org;
    NJ_CHAR  *hyouki;
    NJ_UINT8 *top;
    NJ_UINT8 *wkc;
    NJ_UINT8 cand_idx_size;
    NJ_UINT8 width;
    NJ_UINT8 hyouki_exist;
    NJ_INT16 next;
    NJ_UINT16 j, len;
    NJ_UINT16 yomi_pos;
    NJ_UINT32 cnt;
    NJ_UINT32 search_pos;
    NJ_UINT32 left, right;
    NJ_UINT32 mid;
    NJ_UINT32 mask;
    STEM_DATA_SET stem_set;
    NJ_UINT8 forward = 0;
    NJ_UINT8 *data_top;
    NJ_UINT8 dic_flg_bit;
    NJ_UINT16 dic_bit_all, dic_pos, dic_j, dic_cand_bit, dic_stem_blen, dic_data;
    NJ_INT8 bit_yomi_blen;
    NJ_UINT16 candidate_size;


    /* 必要な辞書ヘッダ情報を予め取得する （高速化）*/
    bit_yomi_blen = BIT_YOMI_LEN(loctset->loct.handle);

    /* 検索対象 表記文字列 */
    hyouki = condition->yomi;

    /* 表記文字列インデックス領域 バイト長 */
    cand_idx_size = CAND_IDX_AREA_SIZE(loctset->loct.handle);
    width = cand_idx_size * 8;

    /* 表記文字列インデックスの先頭アドレス, 登録数を取得 */
    top = CAND_IDX_AREA_TOP_ADDR(loctset->loct.handle);

    /* 表記文字列インデックス領域の登録数取得 */
    cnt = CAND_IDX_AREA_CNT(loctset->loct.handle);

    hyouki_exist = 0;
    search_pos = 0;
    left = 0;
    right = cnt - 1;

    /*
     * 二分検索の中で get_stem_cand_data を呼ぶと速度低下が著しいので、内容を展開する
     */

    /* 共用エンジンの場合,辞書種別を見て処理分ける */
    dic_flg_bit = BIT_MUHENKAN_LEN(loctset->loct.handle);
    if (NJ_GET_DIC_FMT(loctset->loct.handle) != NJ_DIC_FMT_KANAKAN) {
        dic_flg_bit++;  /* 正規化フラグの分を加算 */
    }

    dic_bit_all = (NJ_UINT16)(TERM_BIT + dic_flg_bit + 
                              BIT_HINDO_LEN(loctset->loct.handle) + 
                              BIT_FHINSI(loctset->loct.handle) + 
                              BIT_BHINSI(loctset->loct.handle));
    dic_pos = (NJ_UINT16)(dic_bit_all >> 3);
    dic_j = (NJ_UINT16)(dic_bit_all & 0x0007);
    dic_cand_bit = BIT_CANDIDATE_LEN(loctset->loct.handle);
    /*
     * ↑
     */

    /* 圧縮データ領域アドレス取得 */
    mask = ((NJ_UINT32)(0xFFFFFFFF) >> (32 - width));
    data_top = STEM_AREA_TOP_ADDR(loctset->loct.handle);

    /* 二分検索 */
    while (left <= right) {
        mid = (left + right) >> 1;

        /* 表記文字列インデックス領域に格納されている
         * 圧縮データ領域のバイトオフセット取得 */
        data = data_top + (((NJ_INT32_READ(top + (mid * cand_idx_size))) >> (32 - width)) & mask);

        /* 読み長設定 */
        if (STEM_TERMINETER(data)) {
            /* 読みデータが存在する場合,読み長ビットサイズを加算 */
            dic_stem_blen = dic_bit_all + bit_yomi_blen;
        } else {
            /* 読みデータが存在しない場合,読み長ビットサイズ無視 */
            dic_stem_blen = dic_bit_all;
        }
        dic_stem_blen += dic_cand_bit;

        /*
         * ↓get_stem_cand_data から手でインライン展開
         */
        /*
         * 辞書データを取り出す（表記文字配列長）
         */
        dic_data = (NJ_UINT16)(NJ_INT16_READ(data + dic_pos));
        len = GET_BITFIELD_16(dic_data, dic_j, dic_cand_bit) / sizeof(NJ_CHAR);
        /*
         * ↑
         */

        /* len = 0, つまり無変換候補の場合 */
        if (len == 0) {
            /* 無変換候補の場合は、読み文字列取得するため,
             * data のポインタをずらしていく。(+= next)
             * ここで元々の位置を保持しておく */
            data_org = data;


            /* 読み文字列がある単語(ターミネータON)に移動 */
            while (!(STEM_TERMINETER(data))) {
                /* 表記バイト長を取得する */
                wkc = data + (NJ_UINT16)(dic_bit_all >> 3);
                j = (NJ_UINT16)(NJ_INT16_READ(wkc));
                candidate_size = GET_BITFIELD_16(j, (NJ_UINT16)(dic_bit_all & 0x0007), dic_cand_bit);
                /* 表記ビット数を加算 */
                next = dic_bit_all + dic_cand_bit;

                /* バイト補正 */
                next = GET_BIT_TO_BYTE(next);

                /* 表記文字列長を加算 */
                next += candidate_size;

                /*
                 * ↑ get_stem_next をインライン展開
                 */
                data += next;
            }

            /* 読み長,読みデータ領域までのバイト数取得 */
            yomi_pos = get_stem_yomi_data(loctset->loct.handle, data, &stem_set);
            /* 無変換候補を比較する */
            ret = str_muhenkan_cmp(loctset->loct.handle, hyouki, 
                                   (NJ_UINT8)condition->ylen, 
                                   data + yomi_pos, 
                                   (NJ_UINT8)stem_set.yomi_size,
                                   STEM_NO_CONV_FLG(data_org),
                                   &len);

            /* data を、待避していたアドレスに戻しておく */
            data = data_org;

        } else {
            /* 無変換候補でない場合 */

            /*
             * 検索候補と比較する。
             * 検索読み長と、辞書候補長のうち短い長さを使ってstrncmpする
             */
            wkc = data + GET_BIT_TO_BYTE(dic_stem_blen);
            /* 先頭1文字だけ調べる(関数コールのオーバーヘッド軽減のため) */
            ret = NJ_CHAR_DIFF(hyouki, wkc);
            if (ret == 0) {
                /* 文字列長が短い方の文字列長で比較する */
                if (len >= condition->ylen) {
                    ret = nj_memcmp((NJ_UINT8*)hyouki, wkc, (NJ_UINT16)(condition->ylen * sizeof(NJ_CHAR)));
                } else {
                    ret = nj_memcmp((NJ_UINT8*)hyouki, wkc, (NJ_UINT16)(len * sizeof(NJ_CHAR)));
                    if (ret == 0) {
                        ret = 1;
                    }
                }
            }
        }

        if (ret == 0) {
            /*
             * 前方一致に該当する候補は存在する。
             */
            forward = 1;

            /* 検索文字列長と取得した候補の文字列長を比較 */
            if (condition->ylen == len) {
                /* 一致した場合 */
                hyouki_exist = 1;
                search_pos = mid;
            }

            right = mid - 1;
            if (!mid) {
                break;
            }
        } else if (ret < 0) {
            /* 先頭はこの場所より左 */
            right = mid - 1;
            if (!mid) {
                break;
            }
        } else {
            /* 先頭はこの場所より右 */
            left = mid + 1;
        }
    }

    /* 検索ヒットした候補無し */
    if (!hyouki_exist) {
        if (forward) {
            loctset->loct.status = NJ_ST_SEARCH_END;
        } else {
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
        }
        return 0;       /* 検索候補なし */
    }

    /* 検索で見つかった表記文字列インデックス位置を 
     * loctset->loct.current にセット */
    loctset->loct.current = (NJ_UINT32)(search_pos * cand_idx_size);
    loctset->loct.top = 0;
    return 1;   /* 検索候補あり */
}


/**
 * 逆引検索の次候補検索を行う
 *
 * 次候補の情報はオフセットとして検索位置(loctset)に設定する
 *
 * @param[in]     condition  検索条件
 * @param[in,out] loctset    検索位置
 *
 * @retval 0   検索候補なし
 * @retval 1   検索候補あり
 */
static NJ_INT16 bdic_search_rev_data(NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset) {
    NJ_UINT8 *data, *end;
    NJ_UINT8 *stem_data;
    NJ_UINT8 *stem_data_org;
    STEM_DATA_SET stem_set;
    NJ_INT32 current = 0;
    NJ_INT16 ret;
    NJ_INT16 next;
    NJ_CHAR  *hyouki;
    NJ_UINT8 *p;
    NJ_UINT16 len;
    NJ_UINT16 pos;
    NJ_UINT16 yomi_pos;
    NJ_INT16 hindo;
    NJ_UINT8 cand_idx_size;
    NJ_UINT8 width;
    NJ_UINT32 cnt;
    NJ_UINT32 mask;
    NJ_UINT8 flg_bit;
    NJ_UINT8 b;
    NJ_UINT16 j, bit_all, hdata;


    /* 表記インデックス領域 サイズ取得 */
    cand_idx_size = CAND_IDX_AREA_SIZE(loctset->loct.handle);
    width = cand_idx_size * 8;

    /* 共用辞書の場合 */
    data = CAND_IDX_AREA_TOP_ADDR(loctset->loct.handle);

    /* 表記文字列インデックスの末尾を取得 */
    cnt = CAND_IDX_AREA_CNT(loctset->loct.handle);
    end = data + (cand_idx_size * cnt);

    /* 検索された表記文字列インデックス位置を取得 */
    data += loctset->loct.current;

    /* 検索対象 表記文字列 */
    hyouki = condition->yomi;

    if (GET_LOCATION_STATUS(loctset->loct.status) != NJ_ST_SEARCH_NO_INIT) {
        /* 表記文字列インデックス領域のポインタ進める
         * 検索文字列と表記文字列が一致するかチェックする */
        data += cand_idx_size;
        current += cand_idx_size;

        if (data >= end) {
            /* 次候補無し */
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }

        /* 表記文字列インデックス領域から、圧縮データ領域
         * のアドレス取得 */
        stem_data = STEM_AREA_TOP_ADDR(loctset->loct.handle);

        /* 表記文字列インデックス領域に格納されている
         * 圧縮データ領域のバイトオフセット取得 */
        mask = ((NJ_UINT32)(0xFFFFFFFF) >> (32 - width));
        stem_data += ((NJ_INT32_READ(data)) >> (32 - width)) & mask;

        /* STEMデータより表記文字列に関するデータ取得 */
        get_stem_cand_data(loctset->loct.handle, stem_data, &stem_set);

        if (stem_set.candidate_size == 0) {
            stem_data_org = stem_data;

            /* 読み文字列がある単語(ターミネータON)に移動 */
            while (!(STEM_TERMINETER(stem_data))) {
                next = get_stem_next(loctset->loct.handle, stem_data);
                stem_data += next;
            }

            /* 読み長,読みデータ領域までのバイト数取得 */
            yomi_pos = get_stem_yomi_data(loctset->loct.handle, stem_data, 
                                          &stem_set);

            /* 無変換候補を比較する */
            ret = str_muhenkan_cmp(loctset->loct.handle, hyouki, 
                                   (NJ_UINT8)condition->ylen, 
                                   stem_data + yomi_pos, 
                                   (NJ_UINT8)stem_set.yomi_size,
                                   STEM_NO_CONV_FLG(stem_data_org),
                                   &len);

            if (ret != 0) {
                /* 候補が異なる場合,検索終了の属性を設定 */
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;
            }

            /* 候補長が異なる場合,検索終了の属性を設定 */
            if (condition->ylen != len) {
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;
            }

            /* stem_data を、待避していたアドレスに戻す */
            stem_data = stem_data_org;

        } else {
            /* 無変換候補でない場合 */
            /* 候補長が異なる場合,検索終了の属性を設定 */
            if ((condition->ylen * sizeof(NJ_CHAR)) != stem_set.candidate_size) {
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;
            }
            if (nj_memcmp((NJ_UINT8*)hyouki, stem_data + stem_set.stem_size,
                          stem_set.candidate_size) != 0) {
                /* 候補が異なる場合,検索終了の属性を設定 */
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;
            }
        }
    } else {
        /* 表記文字列インデックス領域から、圧縮データ領域
         * のアドレス取得 */
        stem_data = STEM_AREA_TOP_ADDR(loctset->loct.handle);

        /* 表記文字列インデックス領域に格納されている
         * 圧縮データ領域のバイトオフセット取得 */
        mask = ((NJ_UINT32)(0xFFFFFFFF) >> (32 - (cand_idx_size * 8)));
        stem_data += ((NJ_INT32_READ(data)) >> (32 - (cand_idx_size * 8))) & mask;
    }

    flg_bit = BIT_MUHENKAN_LEN(loctset->loct.handle);
    if (NJ_GET_DIC_FMT(loctset->loct.handle) != NJ_DIC_FMT_KANAKAN) {
        flg_bit++;  /* 正規化フラグの分を加算 */
    }

    /* 品詞番号変換用テーブル上の１品詞のサイズ */
    b = HINSI_NO_BYTE(loctset->loct.handle);

    /*
     * ↑ get_stem_hinsi インライン展開 (前準備)
     */
    while (data < end) {
        /* 検索文字列のポインタを先頭に戻す */
        hyouki = condition->yomi;

        /*
         * ↓get_stem_hinsiのインライン展開 (本体)
         */
        if (condition->hinsi.fore != NULL) {
            if (BIT_FHINSI(loctset->loct.handle)) {
                /* 前品詞 設定 */
                /* 前品詞位置(byte)算出 */
                bit_all = (NJ_UINT16)(TERM_BIT + flg_bit + BIT_HINDO_LEN(loctset->loct.handle));
                pos = (NJ_UINT16)(bit_all >> 3);
                hdata = (NJ_UINT16)(NJ_INT16_READ(stem_data + pos));

                /* 前品詞位置(bit)算出 */
                j = (NJ_UINT16)(bit_all & 0x0007);
                stem_set.fhinsi = GET_BITFIELD_16(hdata, j, BIT_FHINSI(loctset->loct.handle));
            } else {
                stem_set.fhinsi = 0;
            }

            /* 品詞番号変換用テーブルの該当位置 */
            p = (NJ_UINT8*)(HINSI_NO_TOP_ADDR(loctset->loct.handle) + (b * (NJ_UINT16)(stem_set.fhinsi)));
            /* 実品詞への変換：品詞番号設定 */
            if (b == 2) {
                stem_set.fhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(p));
            } else {
                stem_set.fhinsi_jitu = (NJ_UINT16)*p;
            }
        } else {
            stem_set.fhinsi = 0;
            stem_set.fhinsi_jitu = 0;
        }

        if (condition->hinsi.rear != NULL) {
            if (BIT_BHINSI(loctset->loct.handle)) {
                /* 後品詞 設定 */
                /* 後品詞位置(byte)算出 */
                bit_all = (NJ_UINT16)(TERM_BIT + flg_bit + BIT_HINDO_LEN(loctset->loct.handle) + BIT_FHINSI(loctset->loct.handle));
                pos = (NJ_UINT16)(bit_all >> 3);
                hdata = (NJ_UINT16)(NJ_INT16_READ(stem_data + pos));

                /* 後品詞位置(bit)算出 */
                j = (NJ_UINT16)(bit_all & 0x0007);
                stem_set.bhinsi = GET_BITFIELD_16(hdata, j, BIT_BHINSI(loctset->loct.handle));
            } else {
                stem_set.bhinsi = 0;
            }

            /* 実品詞への変換：品詞番号変換用テーブルの該当位置 */
            p = (NJ_UINT8*)(HINSI_NO_TOP_ADDR(loctset->loct.handle)
                            + (b * (FHINSI_NO_CNT(loctset->loct.handle) + (NJ_UINT16)(stem_set.bhinsi))));
            /* 品詞番号設定 */
            if (b == 2) {
                stem_set.bhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(p));
            } else {
                stem_set.bhinsi_jitu = (NJ_UINT16)*p;
            }
        } else {
            stem_set.bhinsi = 0;
            stem_set.bhinsi_jitu = 0;
        }

        /* 前品詞が検索条件と一致するかチェック */
        if (njd_connect_test(condition, stem_set.fhinsi_jitu, stem_set.bhinsi_jitu)) {

            /*
             * 初回検索時は、search_indexにて候補位置が決まっているため、
             * 現在のloctsetをそのまま返す。
             */
            loctset->loct.status = NJ_ST_SEARCH_READY;
            loctset->loct.current += current;
            loctset->loct.current_info = CURRENT_INFO_SET;
            hindo = (NJ_INT16) *((NJ_UINT8*)(HINDO_NO_TOP_ADDR(loctset->loct.handle)
                                              + get_stem_hindo(loctset->loct.handle, stem_data)));
            /* 辞書バージョンがver.3.0以外 */
            if (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION3) {
                loctset->cache_freq = CALCULATE_HINDO(hindo, loctset->dic_freq.base, 
                                                      loctset->dic_freq.high, COMP_DIC_FREQ_DIV);

            }
            return 1;
        }

        /* 表記インデックス進める */
        data += cand_idx_size;
        current += cand_idx_size;
        if (data >= end) {
            /* 次候補無し */
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }

        /* 表記文字列インデックス領域から、圧縮データ領域
         * のアドレス取得 */
        stem_data = STEM_AREA_TOP_ADDR(loctset->loct.handle);

        /* 表記文字列インデックス領域に格納されている
         * 圧縮データ領域のバイトオフセット取得 */
        mask = ((NJ_UINT32)(0xFFFFFFFF) >> (32 - (cand_idx_size * 8)));
        stem_data += ((NJ_INT32_READ(data)) >> (32 - (cand_idx_size * 8))) & mask;

        /* STEMデータより表記文字列に関するデータ取得 */
        get_stem_cand_data(loctset->loct.handle, stem_data, &stem_set);
        
        if (stem_set.candidate_size == 0) {
            stem_data_org = stem_data;

            /* 読み文字列がある単語(ターミネータON)に移動 */
            while (!(STEM_TERMINETER(stem_data))) {
                next = get_stem_next(loctset->loct.handle, stem_data);
                stem_data += next;
            }

            /* 読み長,読みデータ領域までのバイト数取得 */
            yomi_pos = get_stem_yomi_data(loctset->loct.handle, stem_data, 
                                          &stem_set);

            /* 無変換候補を比較する */
            ret = str_muhenkan_cmp(loctset->loct.handle, hyouki, 
                                   (NJ_UINT8)condition->ylen, 
                                   stem_data + yomi_pos, 
                                   (NJ_UINT8)stem_set.yomi_size,
                                   STEM_NO_CONV_FLG(stem_data_org),
                                   &len);

            if (ret != 0) {
                /* 候補が異なる場合,検索終了の属性を設定 */
                loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
                return 0; /*NCH_FB*/
            }

            /* 候補長が異なる場合,検索終了の属性を設定 */
            if (condition->ylen != len) {
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;
            }

            /* stem_data を、待避していたアドレスに戻す */
            stem_data = stem_data_org;

        } else {
            /* 無変換候補でない場合 */
            /* 候補長が異なる場合,検索終了の属性を設定 */
            if ((condition->ylen * sizeof(NJ_CHAR)) != stem_set.candidate_size) {
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;
            }
            if (nj_memcmp((NJ_UINT8*)hyouki, stem_data + stem_set.stem_size,
                          stem_set.candidate_size) != 0) {
                /* 候補が異なる場合,検索終了の属性を設定 */
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;
            }
        }
    }

    /* 候補が異なる場合,検索終了の属性を設定 */
    loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
    return 0; /*NCH_FB*/
}


/************************************************/
/* Global関数                                   */
/************************************************/
/**
 * 圧縮辞書アダプタ  単語検索
 *
 * @param[in]     con      検索条件
 * @param[in,out] loctset  検索位置
 *
 * @retval 0   検索候補なし
 * @retval 1   検索候補あり
 * @retval <0  エラー
 */
NJ_INT16 njd_b_search_word(NJ_SEARCH_CONDITION *con, NJ_SEARCH_LOCATION_SET *loctset) {
    NJ_INT16 ret;
    NJ_DIC_INFO *pdicinfo;
    NJ_UINT16 hIdx;


    /* 検索方法のチェック */
    switch (con->operation) {
    case NJ_CUR_OP_COMP:
        /* 検索候補取得順：頻度順(0)のみ有効 */
        if (con->mode != NJ_CUR_MODE_FREQ) {
            /* 該当候補なしとする */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        break;
    case NJ_CUR_OP_FORE:
        /* 検索候補取得順：登録順(2)は、無効な検索とする */
        if (con->mode == NJ_CUR_MODE_REGIST) {
            /* 該当候補なしとする */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        /*  辞書に読み取得情報がない場合、該当候補なしとする */
        if (APPEND_YOMI_FLG(loctset->loct.handle) == 0) {
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }

        /* 検索文字列が空文字列で、かつ辞書タイプが圧縮カスタマイズ辞書以外の場合、
         * 検索候補なしとする。 */
        if ((NJ_GET_DIC_TYPE_EX(loctset->loct.type, loctset->loct.handle) != NJ_DIC_TYPE_CUSTOM_COMPRESS)
            && NJ_CHAR_STRLEN_IS_0(con->yomi)) {
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        break;
    case NJ_CUR_OP_REV:
        if (NJ_GET_DIC_FMT(loctset->loct.handle) == NJ_DIC_FMT_KANAKAN) {
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        /* 検索候補取得順：頻度順(0)のみ有効 */
        if (con->mode != NJ_CUR_MODE_FREQ) {
            /* 該当候補なしとする */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        break;
    default:
        /* 対応しない検索操作が指定された場合、該当候補なしとする */
        loctset->loct.status = NJ_ST_SEARCH_END_EXT;
        return 0;
    }

    /* 検索文字列長(con->ylen) のチェック */
    if (con->operation == NJ_CUR_OP_REV) {
        /* 逆引検索の場合,最大候補長と比較 */
        if (con->ylen > NJ_GET_MAX_KLEN(loctset->loct.handle)) {
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
    } else {
        /* 正引き完全一致,前方一致検索の場合,最大読み長と比較 */
        if (con->ylen > NJ_GET_MAX_YLEN(loctset->loct.handle)) {
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
    }

    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        /* 検索状態：未初期化 */

        switch (con->operation) {
        case NJ_CUR_OP_COMP:
            /* 完全一致 */
            /* 探索木から、指定読みを元に該当ノードを検索する */
            ret = search_node(con, loctset);
            if (ret < 1) {
                return ret;
            }
            ret = bdic_search_data(con, loctset);
            if (ret < 1) {
                /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
                loctset->loct.status = NJ_ST_SEARCH_END;
            }
            break;
        case NJ_CUR_OP_FORE:
            /* 前方一致 */
            /* 探索木から、指定読みを元に該当ノードを検索する */
            pdicinfo = con->ds->dic;
            for (hIdx = 0; (hIdx < NJ_MAX_DIC) && (pdicinfo->handle != loctset->loct.handle); hIdx++) {
                pdicinfo++;
            }

            if (hIdx == NJ_MAX_DIC) {
                /* 辞書が存在しない為、検索終了の属性をカーソルに設定する */
                loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
                return 0; /*NCH_FB*/
            }

            /* キャッシュ領域がないか、または空文字列指定時 */
            /* キャッシュ検索不要モード時                   */
            if ((con->ds->dic[hIdx].srhCache == NULL) || (con->ylen == 0) ||
                !(con->ds->mode & 0x0001)) {
                ret = search_node(con, loctset);
                if (ret < 1) {
                    return ret;
                }
                ret = bdic_search_fore_data(con, loctset);
            } else {
                ret = search_node2(con, loctset, hIdx);
                if (ret == NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_SEARCH_WORD, NJ_ERR_CACHE_NOT_ENOUGH)) {
                    /* キャッシュ溢れが発生した場合は、もう一度同じ関数を呼ぶ */
                    NJ_SET_CACHEOVER_TO_SCACHE(con->ds->dic[hIdx].srhCache);
                    ret = search_node2(con, loctset, hIdx);
                }
                if (ret < 1) {
                    return ret;
                }
                ret = bdic_search_fore_data2(con, loctset, hIdx);
            }
            if (ret < 1) {
                /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
                loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
            }
            break;
        case NJ_CUR_OP_REV:
            /* 表記文字列インデックスを指定された候補で検索 */
            ret = search_index(con, loctset);
            if (ret < 1) {
                return ret;
            }

            /* 逆引き結果を取得 */
            ret = bdic_search_rev_data(con, loctset);
            if (ret < 1) {
                /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
                loctset->loct.status = NJ_ST_SEARCH_END;
            }
            break;
        default:
            loctset->loct.status = NJ_ST_SEARCH_END_EXT; /*NCH_FB*/
            return 0; /*NCH_FB*/
        }
    } else if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_READY) {

        switch (con->operation) {
        case NJ_CUR_OP_COMP:
            /* 完全一致 */
            /* 次候補となる候補データを検索する */
            ret = bdic_search_data(con, loctset);
            if (ret < 1) {
                /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
                loctset->loct.status = NJ_ST_SEARCH_END;
            }
            break;
        case NJ_CUR_OP_FORE:
            /* 前方一致 */
            /* 次候補となる候補データを検索する */
            pdicinfo = con->ds->dic;
            for (hIdx = 0; (hIdx < NJ_MAX_DIC) && (pdicinfo->handle != loctset->loct.handle); hIdx++) {
                pdicinfo++;
            }

            if (hIdx == NJ_MAX_DIC) {
                /* 辞書が存在しない為、検索終了の属性をカーソルに設定する */
                loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
                return 0; /*NCH_FB*/
            }

            /* キャッシュ領域がないか、または空文字列指定時 */
            /* キャッシュ検索不要モード時                   */
            if ((con->ds->dic[hIdx].srhCache == NULL) || (con->ylen == 0) ||
                !(con->ds->mode & 0x0001)) {
                ret = bdic_search_fore_data(con, loctset);
            } else {
                ret = bdic_search_fore_data2(con, loctset, hIdx);
            }
            if (ret < 1) {
                /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
                loctset->loct.status = NJ_ST_SEARCH_END;
            }
            break;
        case NJ_CUR_OP_REV:
            /* 逆引き結果を取得 */
            ret = bdic_search_rev_data(con, loctset);
            if (ret < 1) {
                /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
                loctset->loct.status = NJ_ST_SEARCH_END;
            }
            break;
        default:
            loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
            return 0; /*NCH_FB*/
        }
    } else {
        /* 検索状態：次候補なし */
        /* 検索終了              */
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
        return 0; /*NCH_FB*/
    }
    return ret;
}


/**
 * 圧縮辞書アダプタ  単語取得
 *
 * @attention
 *   読み、読み長は上位で設定する。
 *
 * @param[in]  loctset  検索位置
 * @param[out] word     単語情報
 *
 * @retval 0   正常終了
 * @retval <0  エラー
 */
NJ_INT16 njd_b_get_word(NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word) {
    NJ_UINT8 *data;
    STEM_DATA_SET stem_set;
    NJ_UINT8 *idx_data;
    NJ_UINT32 mask;
    NJ_UINT8 cand_idx_blen;
    NJ_UINT8 check;



    /* 検索状態：次候補なし  は、そのままリターン */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_END) {
        return 0; /*NCH_FB*/
    }

    if (GET_LOCATION_OPERATION(loctset->loct.status) == NJ_CUR_OP_REV) {
        /* 逆引検索の場合 */
        /* 該当候補の表記文字列インデックス領域のアドレスを取得する */
        idx_data = CAND_IDX_AREA_TOP_ADDR(loctset->loct.handle);
        idx_data += loctset->loct.current;

        /* 該当候補データのアドレスを取得する */
        data = STEM_AREA_TOP_ADDR(loctset->loct.handle);
        cand_idx_blen = CAND_IDX_AREA_SIZE(loctset->loct.handle);

        /* 表記文字列インデックス領域に格納されている
         * 圧縮データ領域のバイトオフセット取得 */
        mask = ((NJ_UINT32)(0xFFFFFFFF) >> (32 - (cand_idx_blen * 8)));
        data += ((NJ_INT32_READ(idx_data)) >> (32 - (cand_idx_blen * 8))) & mask;

        /* 該当候補データの情報を取得するフラグを設定：読み長・候補長取得せず */
        check = 1;

    } else if (GET_LOCATION_OPERATION(loctset->loct.status) == NJ_CUR_OP_FORE) {
        /* 前方検索の場合 */
        /* 該当候補データのアドレスを取得する */
        data = STEM_AREA_TOP_ADDR(loctset->loct.handle);
        data += loctset->loct.top + loctset->loct.current;

        /* 該当候補データの情報を取得するフラグを設定：読み長・候補長取得 */
        check = 0;
    } else {
        /* 正引き完全一致検索の場合 */

        /* 該当候補データのアドレスを取得する */
        data = STEM_AREA_TOP_ADDR(loctset->loct.handle);
        data += loctset->loct.top + loctset->loct.current;

        /* 該当候補データの情報を取得するフラグを設定：候補長のみ取得 */
        check = 2;
    }

    /* 該当候補データの情報を取得する */
    get_stem_word(loctset->loct.handle, data, &stem_set, check);

    if (GET_LOCATION_OPERATION(loctset->loct.status) == NJ_CUR_OP_FORE) {
        /* 前方一致検索の場合は、検索読み文字列と取得した読み文字列の長さが
         * 異なるため、圧縮データ領域から取得した読み文字列長をセットする */
        word->stem.info1 = (NJ_UINT16)(stem_set.yomi_size / sizeof(NJ_CHAR));
    }
    word->stem.info1 = WORD_LEN(word->stem.info1);              /* 前品詞ビットをクリア */
    word->stem.info1 |= (NJ_UINT16)(stem_set.fhinsi_jitu << 7); /* 前品詞 */

    if (check != 1) {
        if (stem_set.candidate_size == 0) {
            /* 無変換候補の場合,読み文字列長をコピー */
            if (GET_LOCATION_OPERATION(loctset->loct.status) == NJ_CUR_OP_FORE) {
                word->stem.info2 = (NJ_UINT16)(stem_set.yomi_size / sizeof(NJ_CHAR));
            } else {
                /* 無変換候補の場合,読み文字列長をコピー */
                word->stem.info2 = (NJ_UINT16)NJ_GET_YLEN_FROM_STEM(word);
            }
        } else {
            /* 無変換候補でない場合は、表記文字列長 */
            word->stem.info2 = (NJ_UINT16)(stem_set.candidate_size / sizeof(NJ_CHAR));
        }
    } else {
        /* 逆引検索結果の場合(check == 1)は、読み文字列長 */
        word->stem.info2 = (NJ_UINT16)NJ_GET_YLEN_FROM_STEM(word);
    }

    word->stem.info2 = WORD_LEN(word->stem.info2);                      /* 後品詞ビットをクリア */
    word->stem.info2 |= (NJ_UINT16)(stem_set.bhinsi_jitu << 7);         /* 後品詞 */
    /* 辞書バージョンがver.3.0以外 */
    if (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION3) {
        word->stem.hindo = CALCULATE_HINDO(stem_set.hindo_jitu, loctset->dic_freq.base, 
                                           loctset->dic_freq.high, COMP_DIC_FREQ_DIV); /* 実頻度値 */

    /* 辞書バージョンがver.3.0 */
    } else {
        /* バイアス値CAND_FZK_HINDO_BIASを減算し、単語頻度として設定する */
        stem_set.hindo_jitu -= CAND_FZK_HINDO_BIAS;
        word->stem.hindo = CALCULATE_HINDO(stem_set.hindo_jitu, loctset->dic_freq.base, 
                                           loctset->dic_freq.high, COMP_DIC_FREQ_DIV); /* 実頻度値 */
        /* 辞書頻度ベース値より候補頻度が低い場合 */
        if (word->stem.hindo < loctset->dic_freq.base) {
            word->stem.hindo = CALCULATE_HINDO_UNDER_BIAS(stem_set.hindo_jitu, loctset->dic_freq.high - loctset->dic_freq.base);
        }
    }
    word->stem.loc = loctset->loct;                                     /* 検索位置 */

    /* 擬似候補の種類をクリア   */
    word->stem.type = 0;
    return 1;
}


/**
 * 圧縮辞書アダプタ  候補文字列取得
 *
 * @param[in]   word       単語情報(NJ_RESULT->wordを指定)
 * @param[out]  candidate  候補文字列格納バッファ
 * @param[in]   size       候補文字列格納バッファサイズ(byte)
 *
 * @retval >0   取得文字列配列長(ヌル文字含まず)
 * @retval <0   エラー
 */
NJ_INT16 njd_b_get_candidate(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size) {
    NJ_SEARCH_LOCATION *loc;
    NJ_CHAR  *wkc, *cand;
    NJ_UINT8  *wkd;
    NJ_UINT8 *data;
    NJ_UINT8 *data_org;
    NJ_UINT16 len, j;
    STEM_DATA_SET stem_set;
    NJ_INT16  next;
    NJ_UINT16 yomi_pos;
    NJ_CHAR   ybuf[NJ_MAX_LEN + NJ_TERM_LEN];



    /* 正引き完全一致,前方一致検索時 */
    if ((GET_LOCATION_OPERATION(word->stem.loc.status) == NJ_CUR_OP_COMP) || 
        (GET_LOCATION_OPERATION(word->stem.loc.status) == NJ_CUR_OP_FORE)) {

        /* 該当候補データのアドレスを取得する */
        loc = &word->stem.loc;
        data = STEM_AREA_TOP_ADDR(loc->handle);
        data += loc->top + loc->current;

        /* 該当候補データの情報を取得する */
        get_stem_cand_data(loc->handle, data, &stem_set);
        len = stem_set.candidate_size / sizeof(NJ_CHAR);

    } else {
        /* 逆引き検索結果の場合はエラーとする。*/
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_GET_CANDIDATE, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
    }

    if (len == 0) {     /* 無変換候補 */
        data_org = data;

        if (GET_LOCATION_OPERATION(word->stem.loc.status) == NJ_CUR_OP_COMP) {
            /* 正引き検索結果の場合は、検索文字列から復元 */
            len = WORD_LEN(word->stem.info1);   /* 読み文字配列長 */
            if (size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_GET_CANDIDATE, NJ_ERR_BUFFER_NOT_ENOUGH);
            }
            wkc = word->yomi;
        } else {

            /* 前方一致検索結果の場合は、圧縮辞書データ領域から、
             * 読み文字列を取得する */


            /* 読み文字列がある単語(ターミネータON)に移動 */
            while (!(STEM_TERMINETER(data))) {
                next = get_stem_next(loc->handle, data);
                data += next;
            }

            /* 読み長,読みデータ領域までのバイト数取得 */
            yomi_pos = get_stem_yomi_data(loc->handle, data, &stem_set);

            /* 読み文字列を取得する */
            wkc = ybuf;
            len = get_stem_yomi_string(loc->handle, data, wkc, 
                                       yomi_pos, stem_set.yomi_size,
                                       size);

            /* サイズチェック */
            if (size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_GET_CANDIDATE, NJ_ERR_BUFFER_NOT_ENOUGH);
            }
        }

        if (STEM_NO_CONV_FLG(data_org) == 0) {  /* ひらがな設定 */
            cand = candidate;
            for (j = 0; j < len; j++) {
                *cand++ = *wkc++;
            }
            *cand = NJ_CHAR_NUL;
        } else {                                /* カタカナ設定 */
            nje_convert_hira_to_kata(wkc, candidate, len);
        }

    } else {            /* 表記あり(無変換候補でない)  */
        /* サイズチェック */
        if (size < (stem_set.candidate_size + (NJ_TERM_LEN*sizeof(NJ_CHAR)))) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_GET_CANDIDATE, NJ_ERR_BUFFER_NOT_ENOUGH);
        }
        wkc = candidate;
        wkd = data + stem_set.stem_size;
        for (j = 0; j < len; j++) {
            NJ_CHAR_COPY(wkc, wkd);
            wkd += sizeof(NJ_CHAR);
            wkc++;
        }
        *wkc = NJ_CHAR_NUL;
    }

    return len;
}


/**
 * 検索結果から読み文字列を取得する
 *
 * @param[in]  word      単語情報(NJ_RESULT->wordを指定)
 * @param[out] stroke    読み文字列格納バッファ
 * @param[in]  size      読み文字列格納バッファサイズ(byte)
 *
 * @retval >0  取得文字配列長(ヌル文字含まず)
 * @retval <0  エラー
 */
NJ_INT16 njd_b_get_stroke(NJ_WORD *word, NJ_CHAR *stroke, NJ_UINT16 size) {
    NJ_SEARCH_LOCATION *loc;
    NJ_UINT8 *data, *idx_data;
    NJ_UINT8 cand_idx_blen;
    NJ_INT16 len;
    NJ_INT16 next;
    NJ_UINT16 yomi_pos;
    NJ_UINT32 mask;
    STEM_DATA_SET stem_set;



    /* 前方一致検索 */
    if (GET_LOCATION_OPERATION(word->stem.loc.status) == NJ_CUR_OP_FORE) {
        if (NJ_GET_YLEN_FROM_STEM(word) == 0) {
            /* word の読み文字列長が 0 の場合,エラーとする */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_GET_STROKE, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
        }

        /* アドレス取得 */
        loc = &word->stem.loc;
        /* 該当候補データのアドレスを取得する */
        data = STEM_AREA_TOP_ADDR(loc->handle);
        data += loc->top + loc->current;

    } else if (GET_LOCATION_OPERATION(word->stem.loc.status) == NJ_CUR_OP_REV) {

        /* アドレス取得 */
        loc = &word->stem.loc;
        idx_data = CAND_IDX_AREA_TOP_ADDR(loc->handle);
        idx_data += loc->current;

        /* 該当候補データのアドレスを取得する */
        data = STEM_AREA_TOP_ADDR(loc->handle);
        cand_idx_blen = CAND_IDX_AREA_SIZE(loc->handle);

        /* 表記文字列インデックス領域に格納されている
         * 圧縮データ領域のバイトオフセット取得 */
        mask = ((NJ_UINT32)(0xFFFFFFFF) >> (32 - (cand_idx_blen * 8)));
        data += ((NJ_INT32_READ(idx_data)) >> (32 - (cand_idx_blen * 8))) & mask;

    } else {
        /* 上記,検索結果以外の場合はエラーとする。*/
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_GET_STROKE, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
    }

    /* 読み文字列がある単語(ターミネータON)に移動 */
    while (!(STEM_TERMINETER(data))) {
        next = get_stem_next(loc->handle, data);
        data += next;
    }

    /* 該当候補データの読み文字列情報を取得する */
    yomi_pos = get_stem_yomi_data(loc->handle, data, &stem_set);
    if (stem_set.yomi_size == 0) {
        /* 読み文字列格納辞書ではない */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_GET_STROKE, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
    }

    /* 読み文字列を取得する */
    len = get_stem_yomi_string(loc->handle, data, stroke, 
                               yomi_pos, stem_set.yomi_size,
                               size);

    /* サイズチェック */
    if (size < (NJ_UINT16)((len+NJ_TERM_LEN)*sizeof(NJ_CHAR))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_GET_STROKE, NJ_ERR_BUFFER_NOT_ENOUGH);
    }

    *(stroke + len) = NJ_CHAR_NUL;
    return len;
}


/**
 * 探索木から検索条件（読み）に該当するノードを検索し、該当する候補データのオフセットを取得
 *
 * @param[in]  condition  検索条件
 * @param[in]  loctset    検索位置
 * @param[in]  hidx       辞書のインデックス
 *
 * @retval 0  検索候補なし
 * @retval 1  検索候補あり
 */
static NJ_INT16 search_node2(NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 hidx) {
    NJ_UINT8 *root, *now, *node, *node_mid;
    NJ_CHAR  *yomi;

    NJ_INT16 ytbl_cnt;
    NJ_UINT16 y;
    NJ_UINT8 *ytbl_top;

    NJ_UINT16 bit_left, bit_data;
    NJ_UINT32 data_offset;
    NJ_UINT16 j;
    NJ_UINT8 *data_top, *stem_data;
    NJ_INT16 hindo, hindo_max, hindo_tmp;
    NJ_UINT32 current, hindo_max_data, hindo_tmp_data;

    /* 新検索 */
    NJ_SEARCH_CACHE *psrhCache = condition->ds->dic[hidx].srhCache;
    NJ_CHAR  *key;
    NJ_UINT8 cmpflg;
    NJ_UINT8 endflg;
    NJ_UINT16 abPtrIdx;
    NJ_UINT16 key_len;
    NJ_UINT16 i, l, m;
    NJ_UINT16 abIdx;
    NJ_UINT16 abIdx_current;
    NJ_UINT16 abIdx_old;
    NJ_UINT16 addcnt = 0;
    NJ_CHAR   char_tmp[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_UINT16 tmp_len;
    NJ_UINT16 endIdx;
    NJ_INT16 ret;
    NJ_UINT8 *con_node;
    NJ_UINT16 yomi_clen;
    NJ_UINT8 aimai_flg = 0x01;
    NJ_CHAR  key_tmp[NJ_MAX_CHAR_LEN + NJ_TERM_LEN];
    NJ_CACHE_INFO tmpbuff;
#ifdef NJ_OPT_CHARSET_2BYTE
    NJ_UINT16 start;
    NJ_UINT16 end;
#endif /* NJ_OPT_CHARSET_2BYTE */


    if (NJ_GET_CACHEOVER_FROM_SCACHE(psrhCache)) {
        aimai_flg = 0x00;
    }

    node = NULL;                /* 検索されたノード */

    yomi = condition->yomi;     /* 検索対象読み文字列 */

    /* TRIEのルートから検索を開始する */
    root = NODE_AREA_TOP_ADDR(loctset->loct.handle);

    /* TRIEの中間地点 */
    node_mid = root + NODE_AREA_MID_ADDR(loctset->loct.handle);
    now = node_mid;

    /*
     * left, data ビット数取得
     */
    bit_left = BIT_NODE_AREA_LEFT_LEN(loctset->loct.handle);
    bit_data = BIT_NODE_AREA_DATA_LEN(loctset->loct.handle);

    /*
     * 読みインデックス
     */
    ytbl_cnt = YOMI_INDX_CNT(loctset->loct.handle);
    y = YOMI_INDX_SIZE(loctset->loct.handle);    /* 読み変換用テーブル長(byte) */
    ytbl_top = YOMI_INDX_TOP_ADDR(loctset->loct.handle);

    /* 圧縮辞書インデックス領域の終端アドレスである
     * 圧縮辞書データ領域の先頭アドレスを取得 */
    data_top = STEM_AREA_TOP_ADDR(loctset->loct.handle);

    /* ループ処理前の初期化 */
    endflg = 0x00;
    cmpflg = 0x00;
    abPtrIdx = 0;
    key = condition->ds->keyword;

    /* 文字数分、曖昧検索を行う。*/
    yomi_clen = condition->yclen;
    for (i = 0; i < yomi_clen; i++) {
        /* ポインターインデックスの格納 */
        abPtrIdx = i;

        /* インデックス情報の構築 */
        if (!cmpflg) {  /* cmpflg == 0x00 */
            /* 一文字分を比較してOKだった。 */
            if (((abPtrIdx != 0) && (psrhCache->keyPtr[abPtrIdx] == 0))
                || (psrhCache->keyPtr[abPtrIdx + 1] == 0)) {
                /* インデックス情報がない */
                cmpflg = 0x01;
            } else {
                /* インデックス情報あり */
            }
        }

        addcnt = 0;
        if (cmpflg) {   /* cmpflg == 0x01 */
            /* インデックス情報なしと判定された場合 */
            if (abPtrIdx == 0) {
                /* 初回のインデックス情報を作成する。 */
                abIdx = 0;
                /* 一文字分をコピーする */
                nj_charncpy(key_tmp, yomi, 1);
                key_len = nj_strlen(key_tmp);

                node = NULL;
                now = node_mid;
                psrhCache->keyPtr[0] = 0;

                /* ノード位置を取得する。 */
                ret = search_yomi_node(condition->operation,
                                       node, now, 0, key_tmp, key_len,
                                       root, node_mid, bit_left, bit_data,
                                       data_top, ytbl_cnt, y, ytbl_top,
                                       &tmpbuff,
                                       &con_node, &data_offset);

                if (ret < 0) {
                    /* 存在しなかった場合 */
                } else {
                    /* 存在した場合 */

                    /* 一時バッファから記憶 */
                    psrhCache->storebuff[abIdx] = tmpbuff;

                    /* 最終ノード位置を記録する。 */
                    now = con_node;

                    /*
                     * データポインタ(同音異義語ブロックの先頭)を検索位置(loctset->loct.top)に設定し、
                     * 先頭の同音異義語データを指すことを示すloctset->loct.current=0を設定する。
                     */
                    psrhCache->storebuff[abIdx].top = data_offset;

                    /*
                     * 前方一致検索の場合は,検索候補の範囲(終端位置)を調べて
                     * loctset->loct.bottom に 設定する
                     */
                    if (condition->operation == NJ_CUR_OP_FORE) {
                        ret = get_node_bottom(key_tmp, now, node_mid, data_top,
                                              bit_left, bit_data,
                                              psrhCache->storebuff[abIdx].top,
                                              loctset->loct.handle,
                                              &(psrhCache->storebuff[abIdx].bottom));
                        if (ret < 0) {
                            /* 異常発生 */
                            return ret; /*NCH_FB*/
                        }
                    }
                    addcnt++;
                    abIdx++;
                }

                if ((condition->charset != NULL) && aimai_flg) {
                    /* 曖昧検索処理 */
#ifdef NJ_OPT_CHARSET_2BYTE
                    /* keyに一致するあいまい文字セットの範囲を２分検索する */
                    if (njd_search_charset_range(condition->charset, key, &start, &end) == 1) {
                        /* 範囲が見つかった */
                        for (l = start; l <= end; l++) {
#else /* NJ_OPT_CHARSET_2BYTE */
                    for (l = 0; l < condition->charset->charset_count; l++) {
                        /* 曖昧検索文字の抽出 */
                        if (nj_charncmp(key, condition->charset->from[l], 1) == 0) {
#endif /* NJ_OPT_CHARSET_2BYTE */
                            /* 一致しているなら */
                            nj_strcpy(char_tmp, condition->charset->to[l]);
                            tmp_len = nj_strlen(char_tmp);

                            node = NULL;
                            now = node_mid;

                            /* ノード位置を取得する。 */
                            ret = search_yomi_node(condition->operation,
                                                   node, now, 0, char_tmp, tmp_len,
                                                   root, node_mid, bit_left, bit_data,
                                                   data_top, ytbl_cnt, y, ytbl_top,
                                                   &tmpbuff,
                                                   &con_node, &data_offset);

                            if (ret < 0) {
                                /* 存在しなかった場合 */
                            } else {
                                /* 存在した場合 */

                                /* キャッシュ溢れ発生 */
                                if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                    psrhCache->keyPtr[abPtrIdx+1] = 0; /*NCH_DEF*/
                                    return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_SEARCH_WORD, NJ_ERR_CACHE_NOT_ENOUGH); /*NCH_DEF*/
                                }

                                /* 一時バッファから記憶 */
                                psrhCache->storebuff[abIdx] = tmpbuff;

                                /* 最終ノード位置を記録する。 */
                                now = con_node;

                                /*
                                 * データポインタ(同音異義語ブロックの先頭)を検索位置(loctset->loct.top)に設定し、
                                 * 先頭の同音異義語データを指すことを示すloctset->loct.current=0を設定する。
                                 */
                                psrhCache->storebuff[abIdx].top = data_offset;

                                /*
                                 * 前方一致検索の場合は,検索候補の範囲(終端位置)を調べて
                                 * loctset->loct.bottom に 設定する
                                 */
                                if (condition->operation == NJ_CUR_OP_FORE) {
                                    ret = get_node_bottom(key_tmp, now,
                                                          node_mid, data_top,
                                                          bit_left, bit_data,
                                                          psrhCache->storebuff[abIdx].top,
                                                          loctset->loct.handle,
                                                          &(psrhCache->storebuff[abIdx].bottom));
                                    if (ret < 0) {
                                        /* 異常発生 */
                                        return ret; /*NCH_FB*/
                                    }
                                }
                                addcnt++;
                                abIdx++;
                            }
                        }
                    }
                }
                psrhCache->keyPtr[abPtrIdx + 1] = abIdx;
            } else {
                /* 現在のインデックス情報から検索を行う。 */
                /* 一文字分をコピーする */
                nj_charncpy(key_tmp, yomi, 1); /* ToDo : 1byte対応済 */
                key_len = nj_strlen(key_tmp);

                if (psrhCache->keyPtr[abPtrIdx] == psrhCache->keyPtr[abPtrIdx - 1]) {
                    /* 検索結果が存在しない */
                    psrhCache->keyPtr[abPtrIdx+1] = psrhCache->keyPtr[abPtrIdx-1];
                    endflg = 0x01;
                } else {
                    /* 検索結果が存在する */
                    /* インデックス有効数を取得 */
                    endIdx = psrhCache->keyPtr[abPtrIdx];
                    abIdx_old = psrhCache->keyPtr[abPtrIdx - 1];

                    if (NJ_GET_CACHEOVER_FROM_SCACHE(psrhCache)) {
                        abIdx = psrhCache->keyPtr[abPtrIdx - 1];
                        psrhCache->keyPtr[abPtrIdx] = abIdx;
                    } else {
                        abIdx = psrhCache->keyPtr[abPtrIdx];
                    }

                    if ((abIdx > NJ_SEARCH_CACHE_SIZE) || (abIdx_old >= NJ_SEARCH_CACHE_SIZE)
                        || (endIdx > NJ_SEARCH_CACHE_SIZE)) {
                        /* キャッシュが破壊されている */
                        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_SEARCH_WORD, NJ_ERR_CACHE_BROKEN); /*NCH_FB*/
                    }

                    for (m = abIdx_old; m < endIdx; m++) {
                        node = psrhCache->storebuff[m].node;
                        now = psrhCache->storebuff[m].now;

                        if ((node == now) && (psrhCache->storebuff[m].idx_no == 0)) {
                            continue;
                        }

                        /* ノード位置を取得する。 */
                        ret = search_yomi_node(condition->operation,
                                               node, now, psrhCache->storebuff[m].idx_no,
                                               key_tmp, key_len, root,
                                               node_mid, bit_left, bit_data,
                                               data_top, ytbl_cnt, y, ytbl_top,
                                               &tmpbuff,
                                               &con_node, &data_offset);

                        if (ret < 0) {
                            /* 存在しなかった場合 */
                        } else {
                            /* 存在した場合 */

                            /* キャッシュ溢れ発生 */
                            if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                psrhCache->keyPtr[abPtrIdx+1] = 0;
                                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_SEARCH_WORD, NJ_ERR_CACHE_NOT_ENOUGH);
                            }

                            /* 一時バッファから記憶 */
                            psrhCache->storebuff[abIdx] = tmpbuff;
                            
                            /* 最終ノード位置を記録する。 */
                            now = con_node;

                            /*
                             * データポインタ(同音異義語ブロックの先頭)を検索位置(loctset->loct.top)に設定し、
                             * 先頭の同音異義語データを指すことを示すloctset->loct.current=0を設定する。
                             */
                            psrhCache->storebuff[abIdx].top = data_offset;

                            /*
                             * 前方一致検索の場合は,検索候補の範囲(終端位置)を調べて
                             * loctset->loct.bottom に 設定する
                             */
                            if (condition->operation == NJ_CUR_OP_FORE) {
                                ret = get_node_bottom(key_tmp, now, node_mid, data_top,
                                                      bit_left, bit_data,
                                                      psrhCache->storebuff[abIdx].top,
                                                      loctset->loct.handle,
                                                      &(psrhCache->storebuff[abIdx].bottom));

                                if (ret < 0) {
                                    /* 異常発生 */
                                    return ret; /*NCH_FB*/
                                }
                            }
                            addcnt++;
                            abIdx++;
                        }

                        if ((condition->charset != NULL) && aimai_flg) {
                            /* 曖昧検索処理 */
#ifdef NJ_OPT_CHARSET_2BYTE
                            /* keyに一致するあいまい文字セットの範囲を２分検索する */
                            if (njd_search_charset_range(condition->charset, key, &start, &end) == 1) {
                                /* 範囲が見つかった */
                                for (l = start; l <= end; l++) {
#else /* NJ_OPT_CHARSET_2BYTE */
                            for (l = 0; l < condition->charset->charset_count; l++) {
                                /* 曖昧検索文字の抽出 */
                                if (nj_charncmp(key, condition->charset->from[l], 1) == 0) {
#endif /* NJ_OPT_CHARSET_2BYTE */
                                    /* 一致しているなら */
                                    nj_strcpy(char_tmp, condition->charset->to[l]);

                                    tmp_len = nj_strlen(char_tmp);

                                    node = psrhCache->storebuff[m].node;
                                    now = psrhCache->storebuff[m].now;

                                    /* ノード位置を取得する。 */
                                    ret = search_yomi_node(condition->operation,
                                                           node, now,
                                                           psrhCache->storebuff[m].idx_no,
                                                           char_tmp, tmp_len,
                                                           root, node_mid,
                                                           bit_left, bit_data, data_top,
                                                           ytbl_cnt, y, ytbl_top,
                                                           &tmpbuff,
                                                           &con_node, &data_offset);

                                    if (ret < 0) {
                                        /* 存在しなかった場合 */
                                    } else {
                                        /* 存在した場合 */

                                        /* キャッシュ溢れ発生 */
                                        if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                            psrhCache->keyPtr[abPtrIdx+1] = 0; /*NCH_DEF*/
                                            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_SEARCH_WORD, NJ_ERR_CACHE_NOT_ENOUGH); /*NCH_DEF*/
                                        }

                                        /* 一時バッファから記憶 */
                                        psrhCache->storebuff[abIdx] = tmpbuff;
                                        
                                        /* 最終ノード位置を記録する。 */
                                        now = con_node;

                                        psrhCache->storebuff[abIdx].top = data_offset;

                                        /*
                                         * 前方一致検索の場合は,検索候補の範囲(終端位置)を調べて
                                         * loctset->loct.bottom に 設定する
                                         */
                                        if (condition->operation == NJ_CUR_OP_FORE) {
                                            ret = get_node_bottom(key_tmp, now, node_mid,
                                                                  data_top, bit_left, bit_data,
                                                                  psrhCache->storebuff[abIdx].top,
                                                                  loctset->loct.handle,
                                                                  &(psrhCache->storebuff[abIdx].bottom));
                                            if (ret < 0) {
                                                /* 異常発生 */
                                                return ret; /*NCH_FB*/
                                            }
                                        }
                                        addcnt++;
                                        abIdx++;
                                    }
                                }
                            }
                        }
                    }
                    psrhCache->keyPtr[abPtrIdx + 1] = abIdx;
                }
            }
        }
        yomi += UTL_CHAR(yomi);
        key  += UTL_CHAR(key);
    }
    /* 今回の検索結果件数が0かつキャッシュに候補がない（検索対象読み文字列長-1の開始位置と終了位置が同じ）場合に終了 */
    if ((addcnt == 0) && (psrhCache->keyPtr[yomi_clen - 1] == psrhCache->keyPtr[yomi_clen])) {
        endflg = 0x01;
    }

    if (endflg) {               /* endflg == 0x01 */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    loctset->loct.current = 0;

    /**************************************************/
    /* 頻度値の取得処理                               */
    /**************************************************/

    /* 有効インデックスを取得する */
    abPtrIdx = condition->yclen;

    /* 開始・終了インデックスを取得する */
    abIdx = psrhCache->keyPtr[abPtrIdx];
    abIdx_old = psrhCache->keyPtr[abPtrIdx - 1];
    if ((abIdx > NJ_SEARCH_CACHE_SIZE) || (abIdx_old >= NJ_SEARCH_CACHE_SIZE)) {
        /* キャッシュが破壊されている */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_SEARCH_WORD, NJ_ERR_CACHE_BROKEN); /*NCH_FB*/
    }

    if (condition->mode == NJ_CUR_MODE_FREQ) {
        hindo_max = 0;
        hindo_max_data = 0;
        abIdx_current = abIdx_old;

        /* top の頻度値取得 */
        stem_data = data_top + psrhCache->storebuff[abIdx_current].top;

        hindo = (NJ_INT16) *((NJ_UINT8 *)(HINDO_NO_TOP_ADDR(loctset->loct.handle) +
                                           get_stem_hindo(loctset->loct.handle, stem_data)));

        hindo_tmp = 0;
        hindo_tmp_data = 0;
        current = 0;

        /* top 〜 bottom の範囲で最大頻度の候補を探す。*/
        while (stem_data <= (data_top + psrhCache->storebuff[abIdx_current].bottom)) {
            /* 最大頻度値, 最大頻度データ位置 更新 */
            if (hindo > hindo_tmp) {
                hindo_tmp = hindo;
                hindo_tmp_data = current;
            }

            /* 次のSTEMデータ設定 */
            j = get_stem_next(loctset->loct.handle, stem_data);
            current += j;
            stem_data += j;

            /* 頻度値取得 */
            hindo = (NJ_INT16) *((NJ_UINT8 *) (HINDO_NO_TOP_ADDR(loctset->loct.handle) +
                                                get_stem_hindo(loctset->loct.handle, stem_data)));

        }

        /* 一番頻度が高い単語を記憶する。 */
        psrhCache->storebuff[abIdx_current].current = hindo_tmp_data;

        /* 最大頻度値、最大頻度データ位置 更新 */
        if (hindo_tmp > hindo_max) {
            hindo_max = hindo_tmp;
            hindo_max_data = hindo_tmp_data;
        }
    } else {
        /* 初回頻度値設定を行う */
        abIdx_current = abIdx_old; /*NCH_FB*/

        /* top の頻度値取得 */
        stem_data = data_top + psrhCache->storebuff[abIdx_current].top; /*NCH_FB*/

        hindo = (NJ_INT16) *((NJ_UINT8 *)(HINDO_NO_TOP_ADDR(loctset->loct.handle) /*NCH_FB*/
                                           + get_stem_hindo(loctset->loct.handle, stem_data)));

        hindo_max = hindo; /*NCH_FB*/
        hindo_max_data = 0; /*NCH_FB*/
    }

    /* 複数頻度がある場合は、最大値を設定する。 */
    loctset->loct.top = psrhCache->storebuff[abIdx_current].top;
    loctset->loct.bottom = psrhCache->storebuff[abIdx_current].bottom;

    /* 辞書バージョンがver.3.0以外 */
    if (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION3) {
        loctset->cache_freq = CALCULATE_HINDO(hindo_max, loctset->dic_freq.base,
                                              loctset->dic_freq.high, COMP_DIC_FREQ_DIV);

    }
    loctset->loct.current = hindo_max_data;
    loctset->loct.current_cache = abIdx_current;

    /* 曖昧検索用表示制御変数の初期化 */
    psrhCache->viewCnt = 1;
    NJ_SET_AIMAI_TO_SCACHE(psrhCache);

    return 1; /* 検索候補あり */
}


/**
 * ノード位置を取得
 *
 * @param[in]     operation      検索方法
 * @param[in,out] node           ノード情報の現在位置
 * @param[in,out] now            ノード情報の次位置
 * @param[in]     idx_no         ノード情報のインデックス位置
 * @param[in]     yomi           検索する単語の読み文字列
 * @param[in]     yomilen        検索する単語の読み文字列の文字配列長
 * @param[in]     root           ルートノード
 * @param[in]     node_mid       ノードの中間地点
 * @param[in]     bit_left       left ビット数
 * @param[in]     bit_data       data ビット数
 * @param[in]     data_top       圧縮辞書データ領域の先頭アドレス
 * @param[in]     ytbl_cnt       読み変換用テーブル  読み格納数
 * @param[in]     y              読み変換用テーブル  読みコード格納バイト数
 * @param[in]     ytbl_top       読み変換用テーブル  先頭アドレス
 * @param[out]    storebuf       キャッシュ情報領域
 * @param[in,out] con_node       検索位置
 * @param[in,out] data_offset    データオフセット
 *
 * @return  取得結果
 */
static NJ_INT16 search_yomi_node(NJ_UINT8 operation, NJ_UINT8 *node, NJ_UINT8 *now,
                                 NJ_UINT16 idx_no, NJ_CHAR  *yomi, NJ_UINT16 yomilen,
                                 NJ_UINT8 * root, NJ_UINT8 * node_mid,
                                 NJ_UINT16 bit_left, NJ_UINT16 bit_data,
                                 NJ_UINT8 * data_top,
                                 NJ_INT16 ytbl_cnt, NJ_UINT16 y, NJ_UINT8 * ytbl_top,
                                 NJ_CACHE_INFO * storebuf,
                                 NJ_UINT8 ** con_node,
                                 NJ_UINT32 * data_offset) {

    NJ_UINT8 index;
    NJ_UINT8 *wkc;
    NJ_UINT8 *byomi;
    NJ_INT16 idx;
    NJ_INT16 char_size;
    NJ_INT16 left, right, mid; /* binary search用index */
    NJ_UINT16 c, d;
    NJ_UINT8 c1 = 0, c2 = 0;
    NJ_UINT16 ysize = yomilen * sizeof(NJ_CHAR);
    NJ_UINT16 idx_cnt;
    NJ_UINT16 nd_index;
    NJ_UINT16 data;
    NJ_UINT16 pos, j, bit_all, bit_tmp, bit_idx;
    NJ_UINT32 data_l;
    NJ_UINT8 restart_flg = 0;


    *con_node = NULL;

    /* インデックスNO初期化 */
    idx_cnt = 1;
    storebuf->idx_no = 0;

    byomi = (NJ_UINT8*)yomi;

    /* 検索対象読み文字列を全て検索する */
    while (ysize > 0) {
        if (ytbl_cnt != 0) {
            /*
             * 読みインデックスが存在する場合は、
             * 読み文字コードをインデックスに変換
             */
            char_size = UTL_CHAR(byomi) * sizeof(NJ_CHAR);
            if (char_size > 2) {
                /* 本辞書は読み変換テーブル幅が最大2byte。
                 * 読み文字 3byte以上の場合、検索候補は無い。 */
                return -1; /*NCH_DEF*/  /* 検索候補無し */
            }


            /* 読み文字列をサイズに合わせて変更 */
            if (char_size == 2) { /* 読み文字：2byte */
                if (y == 1) {
                    /* 読み変換テーブル幅が 1byte ならば 読み文字
                     * 2byteの検索候補はない */
                    return -1;  /* 検索候補無し */ /*NCH_FB*/
                }
                c1 = *byomi;
                c2 = *(byomi + 1);
                c = (NJ_UINT16)((c1 << 8) | c2);
            } else {            /* 読み文字：1byte (SJIS only) */
                /* 1byte文字は前に詰めて比較する */
                c1 = *byomi;
                c2 = 0x00;
                c = (NJ_UINT16)(*byomi);
            }

            idx = -1;
            left = 0;           /* 読み変換用テーブル先頭位置 */
            right = ytbl_cnt;   /* 読み変換用テーブル最後尾位置 */

            if (y == 2) {
                while (left <= right) {
                    mid = (left + right) >> 1;
                    wkc = ytbl_top + (mid << 1);

                    if (c1 == *wkc) {
                        if (c2 == *(wkc + 1)) {
                            idx = (NJ_UINT16) (mid + 1);
                            break;
                        }
                        if (c2 < *(wkc + 1)) {
                            right = mid - 1;
                        } else {
                            left = mid + 1;
                        }
                    } else if (c1 < *wkc) {
                        right = mid - 1;
                    } else {
                        left = mid + 1;
                    }
                }
            } else {
                while (left <= right) {
                    mid = (left + right) >> 1;
                    wkc = ytbl_top + (mid * y);
                    d = (NJ_UINT16) (*wkc);
                    if (c == d) {
                        idx = (NJ_UINT16) (mid + 1);
                        break;
                    }
                    if (c < d) {
                        right = mid - 1;
                    } else {
                        left = mid + 1;
                    }
                }
            }

            if (idx < 0) {
                /*
                 * 指定された文字コードが読み変換用テーブルに無い
                 *  = 指定された辞書では、その読みコードが使われていない
                 */
                return -1;      /* 検索候補無し */
            }
            index = (NJ_UINT8) idx;
        } else {
            /*
             * 読みインデックスが存在しない場合は、
             * 読み文字列の先頭から1byte取り出し
             * インデックスとする
             */
            index = *byomi; /*NCH_DEF*/
            char_size = 1; /*NCH_DEF*/       /* 1byte文字コードとみなす */
        }

        byomi += char_size;       /* 次検索読み文字をセット */
        ysize -= char_size;

        /*
         * 読み文字列から取得したインデックスと一致する
         * インデックスを持つノードを検索する。
         */
        while (now < data_top) {
            if (NODE_IDX_EXIST(now)) {
                bit_idx = 8;
                idx_cnt = NODE_IDX_CNT(now);
            } else {
                bit_idx = 4;
                idx_cnt = 1;
            }
            bit_all = bit_idx;

            /* left の bit数取得 */
            if (NODE_LEFT_EXIST(now)) {
                bit_all += bit_left;
            }

            /* data の bit数取得 */
            if (NODE_DATA_EXIST(now)) {
                bit_all += bit_data;
            }
            /* 後のノードサイズ算出用に退避しておく */
            bit_tmp = bit_all;

            /* 指定されたインデックス番号分ビット加算 (idx_no * 8)*/
            bit_all += (NJ_UINT16) (idx_no << 3);

            /* index の 位置(byte)算出 */
            /* ÷8 を行う。            */
            pos = (NJ_UINT16) (bit_all >> 3);
            /* 取得したいインデックスの手前の byte位置 */
            data = (NJ_UINT16) (NJ_INT16_READ(now + pos));

            /* index の 位置(bit)算出 */
            /* %8 の算出を行う。       */
            j = (NJ_UINT16) (bit_all & 0x0007);

            nd_index = GET_BITFIELD_16(data, j, INDEX_BIT);
            if (index == (NJ_UINT8) nd_index) {
                /* インデックス（文字）が一致していれば、次の文字の処理に移る */
                break;
            } else {
                if ((!NODE_TERM(now)) && (index > (NJ_UINT8) nd_index) && (idx_no == 0)) {
                    /* 次のノード（右ノード）へ移り、同じ文字で比較を継続する */
                    now += GET_BIT_TO_BYTE(bit_tmp + (idx_cnt * 8));
                    if (now == node_mid) {
                        return -1; /*NCH_DEF*/
                    }
                    continue;   /* 移動先のノードと比較 */
                } else {
                    if ((now == node_mid) && (restart_flg == 0)
                        && (index < (NJ_UINT8) nd_index) && (idx_no == 0)
                        && (root != node_mid)) {
                        now = root;
                        idx_no = 0;
                        restart_flg = 1;
                        continue;       /* 移動先のノードと比較 */
                    }
                    return -1;
                }
            }
        }

        /*
         * 複数インデックスで、比較すべきインデックスがまだある場合は、次のインデックスへ移る
         * (idx_cnt が 1 の場合・・・インデックスがない場合）
         */
        if (/* NODE_IDX_EXIST(now) && */ (idx_cnt > (NJ_UINT16) (idx_no + 1))) {
            if (ysize == 0) {
                /*
                 * ysize が 0 になっているのに idx_no が idx_cnt を使い果たしていない場合は、
                 * 辞書の見出し語のほうが長い場合なので、「部分一致」という結果を返す
                 */
                if (operation == NJ_CUR_OP_FORE) {
                    /* 前方一致検索の場合は該当単語となる */
                    storebuf->node = now;
                    storebuf->now = now;
                    storebuf->idx_no = idx_no + 1;
                    node = now;
                    break;
                }
                return -2; /*NCH_DEF*/      /* 検索候補なし */
            }
            /*
             * ysize も残っている場合は、同一辞書インデックス上の次の文字に移行する
             */
            idx_no++;
            continue;
        }

        /*
         * インデックスを比較して一致した場合、左方向ノード（次の文字）に移動 
         */
        node = now;             /* 一致したノードを保存する */
        storebuf->node = now;
        idx_no = 0;             /* インデックス番号クリア */

        if (ysize == 0) {
            /*
             * 全ての読み文字列を検索している場合は、
             * ノードの検索を完了とする。
             */
            *con_node = now;
        } else {
            if (!(NODE_LEFT_EXIST(now))) {
                return -1; /*NCH_DEF*/ /* 検索候補なし */
            }
        }

        /*
         * 左方向のノードを取得し、ノードの検索を継続する
         */
        if (NODE_LEFT_EXIST(now)) {
            if (NODE_IDX_EXIST(now)) {
                bit_idx = 8;
            } else {
                bit_idx = 4;
            }
            pos = (NJ_UINT16) (bit_idx >> 3);
            data_l = (NJ_UINT32) (NJ_INT32_READ(now + pos));

            /* index 位置(bit)算出 */
            j = (NJ_UINT16) (bit_idx & 0x0007);

            now += GET_BITFIELD_32(data_l, j, bit_left);
            storebuf->now = now;
        } else {
            storebuf->now = now;
        }
    }


    /* 検索位置を now に格納しておく */
    if (*con_node == NULL) {
        *con_node = now;
    }

    /* node が NULL もしくは node にデータが存在しない場合 */
    if ((node == NULL) || !(NODE_DATA_EXIST(node))) {

        if ((operation == NJ_CUR_OP_FORE) && (node != NULL)) {
            /* 前方一致検索の場合はデータポインタが存在する
             * 位置までノードを左方向に移動する */
            while (!NODE_DATA_EXIST(node)) {
                if (!(NODE_LEFT_EXIST(node))) {
                    /* 左方向のノードが存在しない場合は 検索候補なし */
                    return -2; /*NCH_DEF*/  /* 検索候補なし */
                }

                /*
                 * 左方向のノードを取得し、ノードの検索を継続する
                 */
                if (NODE_IDX_EXIST(node)) {
                    bit_idx = 8;
                } else {
                    bit_idx = 4;
                }
                pos = (NJ_UINT16) (bit_idx >> 3);
                data_l = (NJ_UINT32) (NJ_INT32_READ(node + pos));

                /* index 位置(bit)算出 */
                j = (NJ_UINT16) (bit_idx & 0x0007);
                node += GET_BITFIELD_32(data_l, j, bit_left);
            }
        } else {
            return -2; /*NCH_DEF*/          /* 検索候補なし */
        }
    }

    /* 全ての読み文字列に該当するノード列が見つかった場合、
     * ノード列の最終ノードのデータポインタを検査する。*/
    if (NODE_IDX_EXIST(node)) {
        bit_idx = 8;
    } else {
        bit_idx = 4;
    }

    /* left フィールドのbit 数を加算 */
    if (NODE_LEFT_EXIST(node)) {
        bit_all = bit_idx + bit_left;
    } else {
        bit_all = bit_idx;
    }

    pos = (NJ_UINT16) (bit_all >> 3);
    data_l = (NJ_UINT32) (NJ_INT32_READ(node + pos));

    /* data の値を取得 */
    j = (NJ_UINT16) (bit_all & 0x0007);
    *data_offset = GET_BITFIELD_32(data_l, j, bit_data);

    return 1;
}


/**
 * 検索候補の範囲(終端位置)を調べる
 *
 * @param[in]  yomi       検索する単語の読み文字列
 * @param[in]  now        ノード情報の次位置
 * @param[in]  node_mid   ノードの中間地点
 * @param[in]  data_top   圧縮辞書データ領域の先頭アドレス
 * @param[in]  bit_left   left ビット数
 * @param[in]  bit_data   data ビット数
 * @param[in]  top        開始位置
 * @param[in]  handle     辞書ハンドル
 * @param[out] ret_bottom 終了位置
 *
 * @retval 1   正常
 * @retval <0  エラー
 */
static NJ_INT16 get_node_bottom(NJ_CHAR * yomi, NJ_UINT8 * now, NJ_UINT8 * node_mid,
                                NJ_UINT8 * data_top, NJ_UINT16 bit_left, NJ_UINT16 bit_data,
                                NJ_UINT32 top, NJ_DIC_HANDLE handle,
                                NJ_UINT32 * ret_bottom) {
    NJ_UINT8 *node;
    NJ_UINT16 idx_cnt;
    NJ_UINT32 data_offset;
    NJ_UINT16 pos, j, bit_all;
    NJ_UINT32 data_l;
    NJ_UINT8 bottom_flg = 0;
    NJ_UINT8 *stem_data;
    NJ_UINT32 bottom, next;


    /* bottom を 検索候補の先頭位置(top)で初期化しておく */
    bottom = top;

    if (NJ_CHAR_STRLEN_IS_0(yomi)) {
        /* 検索文字列に空文字指定して前方一致検索の場合。
         * 中間ノード地点を起点とし、終端位置を探す */
        node = node_mid; /*NCH_DEF*/

    } else {
        /* 検索文字列指定あり時の前方一致検索の場合 */
        node = now;
        if (NODE_LEFT_EXIST(node)) {
            /* まず一致したノード位置から １つleft方向 に移動する */
            if (NODE_IDX_EXIST(node)) {
                bit_all = 8;
            } else {
                bit_all = 4;
            }

            pos = (NJ_UINT16) (bit_all >> 3);
            data_l = (NJ_UINT32) (NJ_INT32_READ(node + pos));

            /* left の値を取得 */
            j = (NJ_UINT16) (bit_all & 0x0007);
            node += GET_BITFIELD_32(data_l, j, bit_left);

        } else {
            /* 検索位置の left 方向にノードがない場合は、
             * 先頭候補=最後候補となる */
            bottom_flg = 1;
        }
    }

    /* 上記処理で bottom が既に設定済みならば 検索しない */
    if (!bottom_flg) {
        while (node < data_top) {
            /* ノードのターミネータがONかチェック */
            if (!NODE_TERM(node)) {
                /* ターミネータ が OFF ならば 次ノードに移動する */
                if (NODE_IDX_EXIST(node)) {
                    bit_all = 8; /*NCH_DEF*/
                    idx_cnt = NODE_IDX_CNT(node); /*NCH_DEF*/
                } else {
                    bit_all = 4;
                    idx_cnt = 1;
                }

                /* left の bit数取得 */
                if (NODE_LEFT_EXIST(node)) {
                    bit_all += bit_left;
                }

                /* data の bit数取得 */
                if (NODE_DATA_EXIST(node)) {
                    bit_all += bit_data;
                }

                /* 次ノードに移動 */
                node += GET_BIT_TO_BYTE(bit_all + (idx_cnt * 8));
            } else {
                /* ターミネータがONならば left があるか見る */
                if (!NODE_LEFT_EXIST(node)) {
                    /* left がない場合は、data があるか見る。 */
                    if (NODE_DATA_EXIST(node)) {
                        /* data はある場合は、そこが最終候補とする */
                        if (NODE_IDX_EXIST(node)) {
                            bit_all = 8;
                        } else {
                            bit_all = 4;
                        }

                        pos = (NJ_UINT16) (bit_all >> 3);
                        data_l = (NJ_UINT32) (NJ_INT32_READ(node + pos));

                        /* data の値を取得 */
                        j = (NJ_UINT16) (bit_all & 0x0007);
                        data_offset = GET_BITFIELD_32(data_l, j, bit_data);
                        /* 最後の候補をセット */
                        bottom = data_offset;
                        break;
                    } else {
                        /* left も data もない場合は,あり得ないはず。
                         * エラーとする */
                        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_SEARCH_WORD, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
                    }

                } else {
                    /* left があった場合,left 方向に移動する。 */
                    if (NODE_IDX_EXIST(node)) {
                        bit_all = 8;
                    } else {
                        bit_all = 4;
                    }

                    pos = (NJ_UINT16) (bit_all >> 3);
                    data_l = (NJ_UINT32) (NJ_INT32_READ(node + pos));

                    /* left の値を取得 */
                    j = (NJ_UINT16) (bit_all & 0x0007);

                    /* left 方向に移動 */
                    node += GET_BITFIELD_32(data_l, j, bit_left);
                }
            }
        }
    }

    stem_data = data_top + bottom;

    /* 同音異議語がある場合に備えて,
     * 同音異議語終端(=ターミネータがON)まで移動。*/
    while (!(STEM_TERMINETER(stem_data))) {
        next = get_stem_next(handle, stem_data);
        stem_data += next;
    }
    /* 終端位置(stem_data)の、先頭位置(data_top)からの
     * オフセットを bottom に設定する */
    *ret_bottom = (NJ_UINT32) (stem_data - data_top);

    return 1;
}


/**
 * 前方一致検索における次候補検索を行う
 *
 * 次候補の情報はオフセットとして検索位置(loctset)に設定する
 *
 * @param[in]     condition  検索条件
 * @param[in,out] loctset    検索位置
 * @param[in]     hidx       辞書のインデックス
 *
 * @retval  0   検索候補なし
 * @retval  1   検索候補あり
 */
static NJ_INT16 bdic_search_fore_data2(NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 hidx) {
    NJ_UINT8 *data, *data_top, *bottom, *data_end;
    NJ_INT16 i = 0;
    NJ_INT16 hindo = 0;
    NJ_UINT32 current = loctset->loct.current;


    NJ_SEARCH_CACHE *psrhCache = condition->ds->dic[hidx].srhCache;

    NJ_UINT16 top_abIdx;
    NJ_UINT16 bottom_abIdx;
    NJ_UINT16 count_abIdx;
    NJ_UINT16 current_abIdx;
    NJ_UINT16 old_abIdx;
    NJ_UINT8 freq_flag = 0;
    NJ_INT16 save_hindo = 0;
    NJ_UINT16 save_abIdx = 0;
    NJ_UINT16 abPtrIdx;
    NJ_UINT16 m;
    NJ_INT16 ret;
    NJ_INT16 loop_check;

    NJ_UINT16 abIdx;
    NJ_UINT16 abIdx_old;
    NJ_INT16 hindo_max, hindo_tmp;
    NJ_UINT32 hindo_max_data, hindo_tmp_data;
    NJ_UINT16 abIdx_current;



    /* 初回検索時は そのまま抜ける */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        loctset->loct.status = NJ_ST_SEARCH_READY;
        loctset->loct.current_info = CURRENT_INFO_SET;
        return 1;
    }

    if (NJ_GET_AIMAI_FROM_SCACHE(psrhCache)) {
        NJ_UNSET_AIMAI_TO_SCACHE(psrhCache);
        /* 圧縮辞書データ領域先頭取得 */
        data_top = STEM_AREA_TOP_ADDR(loctset->loct.handle);
        if (condition->operation == NJ_CUR_OP_FORE) {
            if (condition->ylen) {              /* condition->ylen != 0 */
                /* 有効インデックスを取得する */
                abPtrIdx = condition->yclen;

                /* 開始・終了インデックスを取得する */
                abIdx = psrhCache->keyPtr[abPtrIdx];
                abIdx_old = psrhCache->keyPtr[abPtrIdx - 1];
                if ((abIdx > NJ_SEARCH_CACHE_SIZE) || (abIdx_old >= NJ_SEARCH_CACHE_SIZE)) {
                    /* キャッシュが破壊されている */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_SEARCH_WORD, NJ_ERR_CACHE_BROKEN); /*NCH_FB*/
                }

                if (condition->mode == NJ_CUR_MODE_FREQ) {
                    hindo_max = 0;
                    hindo_max_data = 0;
                    abIdx_current = abIdx_old;

                    for (m = abIdx_old; m < abIdx; m++) {
                        /* top の頻度値取得 */
                        data = data_top + psrhCache->storebuff[m].top;

                        hindo = (NJ_INT16) *((NJ_UINT8 *)(HINDO_NO_TOP_ADDR(loctset->loct.handle) +
                                                           get_stem_hindo(loctset->loct.handle, data)));

                        hindo_tmp = 0;
                        hindo_tmp_data = 0;
                        current = 0;

                        /* top 〜 bottom の範囲で最大頻度の候補を探す。*/
                        while (data <= (data_top + psrhCache->storebuff[m].bottom)) {
                            /* 最大頻度値, 最大頻度データ位置 更新 */
                            if (hindo > hindo_tmp) {
                                hindo_tmp = hindo;
                                hindo_tmp_data = current;
                            }

                            /* 次のSTEMデータ設定 */
                            i = get_stem_next(loctset->loct.handle, data);
                            current += i;
                            data += i;

                            /* 頻度値取得 */
                            hindo = (NJ_INT16) *((NJ_UINT8 *) (HINDO_NO_TOP_ADDR(loctset->loct.handle) +
                                                                get_stem_hindo(loctset->loct.handle, data)));

                        }

                        /* 一番頻度が高い単語を記憶する。 */
                        psrhCache->storebuff[m].current = hindo_tmp_data;

                        /* 最大頻度値、最大頻度データ位置 更新 */
                        if (hindo_tmp > hindo_max) {
                            hindo_max = hindo_tmp;
                            hindo_max_data = hindo_tmp_data;
                            abIdx_current = m;
                        }
                    }
                } else {
                    /* 初回頻度値設定を行う */
                    abIdx_current = abIdx_old; /*NCH_FB*/

                    /* top の頻度値取得 */
                    data = data_top + psrhCache->storebuff[abIdx_current].top; /*NCH_FB*/

                    hindo = (NJ_INT16) *((NJ_UINT8 *)(HINDO_NO_TOP_ADDR(loctset->loct.handle) /*NCH_FB*/
                                                       + get_stem_hindo(loctset->loct.handle, data)));

                    hindo_max = hindo; /*NCH_FB*/
                    hindo_max_data = 0; /*NCH_FB*/
                }

                /* 複数頻度がある場合は、最大値を設定する。 */
                loctset->loct.top = psrhCache->storebuff[abIdx_current].top;
                loctset->loct.bottom = psrhCache->storebuff[abIdx_current].bottom;

                /* 辞書バージョンがver.3.0以外 */
                if (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION3) {
                    loctset->cache_freq = CALCULATE_HINDO(hindo_max, loctset->dic_freq.base,
                                                          loctset->dic_freq.high, COMP_DIC_FREQ_DIV);

                }
                loctset->loct.current = hindo_max_data;
                loctset->loct.current_cache = abIdx_current;

                /* 曖昧検索用表示制御変数の初期化 */
                psrhCache->viewCnt = 1;
            } else {
                /* top の頻度値取得 */
                data = data_top + loctset->loct.top; /*NCH*/

                hindo = (NJ_INT16) *((NJ_UINT8 *)(HINDO_NO_TOP_ADDR(loctset->loct.handle) + /*NCH*/
                                                   get_stem_hindo(loctset->loct.handle, data)));

                hindo_max = hindo; /*NCH*/
                hindo_max_data = 0; /*NCH*/

                if (condition->mode == NJ_CUR_MODE_FREQ) { /*NCH*/

                    /* 次のSTEMデータ設定 */
                    i = get_stem_next(loctset->loct.handle, data); /*NCH*/
                    current = i; /*NCH*/
                    data += i; /*NCH*/

                    /* top 〜 bottom の範囲で最大頻度の候補を探す。*/
                    while (data <= (data_top + loctset->loct.bottom)) { /*NCH*/

                        /* 頻度値取得 */
                        hindo = (NJ_INT16)*((NJ_UINT8 *)(HINDO_NO_TOP_ADDR(loctset->loct.handle) + /*NCH*/
                                                          get_stem_hindo(loctset->loct.handle, data)));

                        /* 最大頻度値, 最大頻度データ位置 更新 */
                        if (hindo > hindo_max) { /*NCH*/
                            hindo_max = hindo; /*NCH*/
                            hindo_max_data = current; /*NCH*/
                        }

                        /* 次のSTEMデータ設定 */
                        i = get_stem_next(loctset->loct.handle, data); /*NCH*/
                        current += i; /*NCH*/
                        data += i; /*NCH*/
                    }
                }
                /* 辞書バージョンがver.3.0以外 */
                if (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION3) { /*NCH*/
                    loctset->cache_freq = CALCULATE_HINDO(hindo_max, /*NCH*/
                                                          loctset->dic_freq.base,
                                                          loctset->dic_freq.high, COMP_DIC_FREQ_DIV);
                }
                loctset->loct.current = hindo_max_data; /*NCH*/
            }
        }
        return 1;
    }

    /* 圧縮辞書データ領域先頭取得 */
    data_top = STEM_AREA_TOP_ADDR(loctset->loct.handle);

    /* 検索単語の格納位置アドレスを取得 */
    data = data_top + loctset->loct.top + loctset->loct.current;


    /* 検索範囲終端のアドレスを取得。bottom に設定 */
    bottom = data_top + loctset->loct.bottom;

    /*
     * 圧縮辞書データ領域の終端アドレスを取得 辞書ハンドルアドレス +
     * 共通ヘッダー + 辞書データサイズ + 拡張情報サイズ - 識別子 
     */
    if (NJ_GET_DIC_FMT(loctset->loct.handle) == NJ_DIC_FMT_KANAKAN) {
        data_end = loctset->loct.handle
            + NJ_DIC_COMMON_HEADER_SIZE
            + NJ_INT32_READ(loctset->loct.handle + NJ_DIC_POS_DATA_SIZE)
            + NJ_INT32_READ(loctset->loct.handle + NJ_DIC_POS_EXT_SIZE)
            - NJ_DIC_ID_LEN;
    } else {
        /* 逆引き完全一致圧縮辞書の場合は、 圧縮辞書データ領域の終端 =
         * 表記インデックスの 先頭アドレス */
        data_end = CAND_IDX_AREA_TOP_ADDR(loctset->loct.handle);
    }

    if (condition->mode == NJ_CUR_MODE_FREQ) {
        /* 頻度順検索の場合は、次に頻度が高い単語を取得する */
        /* 圧縮辞書データ領域の終端を超えた場合は抜ける */

        /* 有効インデックスを取得する */
        abPtrIdx = condition->yclen;

        /* 開始・終了インデックスを取得する */
        bottom_abIdx = psrhCache->keyPtr[abPtrIdx];
        top_abIdx = psrhCache->keyPtr[abPtrIdx - 1];
        if ((bottom_abIdx > NJ_SEARCH_CACHE_SIZE) || (top_abIdx >= NJ_SEARCH_CACHE_SIZE)) {
            /* キャッシュが破壊されている */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_B_SEARCH_WORD, NJ_ERR_CACHE_BROKEN); /*NCH_FB*/
        }

        /* インデックス数を割り出す */
        count_abIdx = bottom_abIdx - top_abIdx;
        if (!count_abIdx) {
            loctset->loct.status = NJ_ST_SEARCH_END; /*NCH*/
            return 0; /*NCH*/
        }

        old_abIdx = loctset->loct.current_cache;

        loop_check = 0;

        /* 次候補の取得を行う。 */
        ret = bdic_get_next_data(data_top, data_end, loctset, psrhCache, old_abIdx);

        if (ret == loctset->cache_freq) {
            /* 過去の頻度と同じ頻度だった場合。 */
            psrhCache->viewCnt++;
            if (psrhCache->viewCnt <= NJ_CACHE_VIEW_CNT) {
                /* 同一曖昧検索結果の抑制を行う。 */
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->loct.current = psrhCache->storebuff[old_abIdx].current;
                loctset->loct.current_cache = old_abIdx;
                return 1;
            } else {
                /* 曖昧検索チェック変数の初期化 */
                freq_flag = 1;
                psrhCache->viewCnt = 0;
            }
        } else {
            /* 過去の頻度と同じではなかった場合 */
            /* ここを通る場合は、過去の頻度より低いはずである。 */
            if (ret == -1) {
                /* 候補がなくなった場合 */
                loop_check++;
            }
            save_hindo = ret;
            save_abIdx = old_abIdx;
        }

        /* カレント以降を検索する。 */
        current_abIdx = old_abIdx + 1;
        if (current_abIdx >= bottom_abIdx) {
            /* 検索範囲を超えている場合、Topへ移動 */
            current_abIdx = top_abIdx;
        }

        while (loop_check != count_abIdx) {
            /* チェック変数がカウント数すべてを満たすまで終了しない。 */
            /* 終了条件を満たす＝候補０を示す。 */

            /* 指定候補の頻度を取得する */
            ret = bdic_get_word_freq(data_top, loctset, psrhCache, current_abIdx);

            if ((ret == loctset->cache_freq) &&
                (loctset->loct.top == psrhCache->storebuff[current_abIdx].top) &&
                (loctset->loct.current == psrhCache->storebuff[current_abIdx].current)) {
                /* 過去の頻度と同じ頻度でかつ辞書内の同一単語を指している場合 */
                /* 単語を１つ進ませる */
                ret = bdic_get_next_data(data_top, data_end, loctset, psrhCache, current_abIdx);
            }

            if (ret == loctset->cache_freq) {
                /* 過去の頻度と同じ頻度だった場合 */
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->loct.top = psrhCache->storebuff[current_abIdx].top;
                loctset->loct.bottom = psrhCache->storebuff[current_abIdx].bottom;
                loctset->loct.current = psrhCache->storebuff[current_abIdx].current;
                loctset->loct.current_cache = current_abIdx;
                psrhCache->viewCnt = 1;
                return 1;

            } else {
                /* 過去の頻度と同じではなかった場合 */
                /* ここを通る場合は、過去の頻度より低いはずである。 */
                if (ret == -1) {
                    /* 候補がなくなった場合 */
                    loop_check++;
                }
                if (save_hindo < ret) {
                    /* 保存頻度より大きければ頻度を保存 */
                    save_hindo = ret;
                    save_abIdx = current_abIdx;
                }
            }

            /* カレントを進める。 */
            current_abIdx++;
            if (current_abIdx >= bottom_abIdx) {
                /* 検索範囲を超えている場合、Topへ移動 */
                current_abIdx = top_abIdx;
            }

            /* 曖昧検索範囲をすべて検索完了 */
            if (current_abIdx == old_abIdx) {
                if (freq_flag == 1) {
                    /* ほかの候補内で同一候補が見つからなかった場合 */
                    loctset->loct.status = NJ_ST_SEARCH_READY; /*NCH_DEF*/
                    loctset->loct.current_info = CURRENT_INFO_SET; /*NCH_DEF*/
                    loctset->loct.top = psrhCache->storebuff[current_abIdx].top; /*NCH_DEF*/
                    loctset->loct.bottom = psrhCache->storebuff[current_abIdx].bottom; /*NCH_DEF*/
                    loctset->loct.current = psrhCache->storebuff[current_abIdx].current; /*NCH_DEF*/
                    loctset->loct.current_cache = current_abIdx; /*NCH_DEF*/
                    psrhCache->viewCnt = 1; /*NCH_DEF*/
                    return 1; /*NCH_DEF*/
                } else if (save_hindo != -1) {
                    /* 頻度を更新する。 */
                    loctset->cache_freq = save_hindo;
                    loctset->loct.status = NJ_ST_SEARCH_READY;
                    loctset->loct.current_info = CURRENT_INFO_SET;
                    loctset->loct.top = psrhCache->storebuff[save_abIdx].top;
                    loctset->loct.bottom = psrhCache->storebuff[save_abIdx].bottom;
                    loctset->loct.current = psrhCache->storebuff[save_abIdx].current;
                    loctset->loct.current_cache = save_abIdx;
                    psrhCache->viewCnt = 1;
                    return 1;
                }
            }
        }
    } else {
        /* 読み順検索の次のSTEMデータを取り出す。 */

        /* 次のSTEMデータ設定 */
        i = get_stem_next(loctset->loct.handle, data); /*NCH_FB*/
        data += i; /*NCH_FB*/
        current += i; /*NCH_FB*/

        /* data 位置が bottom より大きい場合 */
        if (data > bottom) { /*NCH_FB*/
            /* 検索終了の属性を設定 */
            loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
            return 0; /*NCH_FB*/
        }

        /* 頻度値を取得する */
        hindo = (NJ_INT16)*((NJ_UINT8 *)(HINDO_NO_TOP_ADDR(loctset->loct.handle) /*NCH_FB*/
                                         + get_stem_hindo(loctset->loct.handle, data)));
        /* 辞書バージョンがver.3.0以外 */
        if (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION3) { /*NCH_FB*/
            loctset->cache_freq = CALCULATE_HINDO(hindo, loctset->dic_freq.base, /*NCH_FB*/
                                                  loctset->dic_freq.high, COMP_DIC_FREQ_DIV);

        }
        loctset->loct.status = NJ_ST_SEARCH_READY; /*NCH_FB*/
        loctset->loct.current_info = CURRENT_INFO_SET; /*NCH_FB*/
        loctset->loct.current = current; /*NCH_FB*/
        return 1; /*NCH_FB*/
    }
    /* 検索終了の属性を設定 */
    loctset->loct.status = NJ_ST_SEARCH_END;
    return 0;
}


/**
 * 次候補の取得を行う
 *
 * @param[in]  data_top    圧縮辞書データ領域の先頭アドレス
 * @param[in]  data_end    圧縮辞書データ領域の終端アドレス
 * @param[in]  loctset     検索位置
 * @param[in]  psrhCache   キャッシュ管理領域
 * @param[in]  abIdx       キャッシュ情報領域のインデックス
 *
 * @retval >=0  頻度
 * @retval -1   検索候補なし
 */
static NJ_INT16 bdic_get_next_data(NJ_UINT8 *data_top, NJ_UINT8 *data_end,
                                   NJ_SEARCH_LOCATION_SET *loctset,
                                   NJ_SEARCH_CACHE *psrhCache,
                                   NJ_UINT16 abIdx) {
    NJ_UINT8 *data, *bottom;
    NJ_INT16 i = 0;
    NJ_INT16 hindo = 0;
    NJ_INT16 hindo_max = -1;
    NJ_UINT8 no_hit = 0;
    NJ_UINT32 current = psrhCache->storebuff[abIdx].current;
    NJ_UINT8 *current_org;
    NJ_UINT32 hindo_data = 0;
    NJ_INT16 freq_org = loctset->cache_freq;


    if (psrhCache->storebuff[abIdx].current == LOC_CURRENT_NO_ENTRY) {
        return (-1); /*NCH_FB*/
    }

    /* 検索単語の格納位置アドレスを取得 */
    data = data_top + psrhCache->storebuff[abIdx].top + psrhCache->storebuff[abIdx].current;

    /* 検索開始位置を退避しておく */
    current_org = data;

    /* 検索範囲終端のアドレスを取得。bottom に設定 */
    bottom = data_top + psrhCache->storebuff[abIdx].bottom;

    /* 頻度順検索の場合は、次に頻度が高い単語を取得する */

    /* 圧縮辞書データ領域の終端を超えた場合は抜ける */
    while (data < data_end) {
        /* 次のSTEMデータ設定 */
        i = get_stem_next(loctset->loct.handle, data);
        data += i;
        current += i;

        /* data 位置が bottom より大きい場合 */
        if (data > bottom) {
            /*
             * 頻度値が0の状態で検索範囲の終端に達した場合,
             * 検索終了とする */
            if ((freq_org == 0) || (no_hit == 1)) {
                /* 検索終了の属性を設定 */
                psrhCache->storebuff[abIdx].current = LOC_CURRENT_NO_ENTRY;
                return -1;
            }
            /* 頻度を -1 する */
            freq_org -= 1;

            /* 検索候補の先頭に戻す */
            data = data_top + psrhCache->storebuff[abIdx].top;
            current = 0;

            /* 次に bottom まで来たら頻度値と一致する
             * ものがなかった,という判断に活用。 ここでフラグ立てる */
            no_hit = 1;
        }

        /* 検索開始位置まで見終わった場合 */
        if ((hindo_max != -1) && (data == current_org)) {
            /* 頻度値が一致するものが無かったら
             * その頻度値以下で最大頻度の候補を返す */
            psrhCache->storebuff[abIdx].current = hindo_data;
            return hindo_max;
        }

        /* 頻度値を取得する */
        hindo = (NJ_INT16)*((NJ_UINT8 *)(HINDO_NO_TOP_ADDR(loctset->loct.handle)
                                         + get_stem_hindo(loctset->loct.handle, data)));
        /* 辞書バージョンがver.3.0以外 */
        if (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION3) {
            /* 辞書頻度を元に評価値を算出 */
            hindo = CALCULATE_HINDO(hindo, loctset->dic_freq.base, loctset->dic_freq.high, COMP_DIC_FREQ_DIV);

        }

        /* 検索中頻度と等しい場合, その検索候補位置を返す */
        if (hindo == freq_org) {
            psrhCache->storebuff[abIdx].current = current;
            return hindo;
        }

        /* 検索中の頻度と一致しない場合,
         * 最大頻度を持つ候補の頻度値,検索位置を 保持しておく。*/
        if (hindo < freq_org) {
            if ((hindo > hindo_max) || ((hindo == hindo_max) && (current < hindo_data))) {
                hindo_max = hindo;
                hindo_data = current;
            }
        }
    }

    /* 検索終了の属性を設定 */
    psrhCache->storebuff[abIdx].current = LOC_CURRENT_NO_ENTRY; /*NCH*/
    return -1; /*NCH*/
}


/**
 * 指定候補の頻度を取得する
 *
 * @param[in]  data_top    圧縮辞書データ領域の先頭アドレス
 * @param[in]  loctset     検索位置
 * @param[in]  psrhCache   キャッシュ管理領域
 * @param[in]  abIdx       キャッシュ情報領域のインデックス
 *
 * @retval >=0  頻度
 * @retval -1  検索候補なし
 */
static NJ_INT16 bdic_get_word_freq(NJ_UINT8 * data_top, NJ_SEARCH_LOCATION_SET * loctset,
                                   NJ_SEARCH_CACHE * psrhCache, NJ_UINT16 abIdx) {
    NJ_UINT8 *data;
    NJ_INT16 hindo = 0;


    if (psrhCache->storebuff[abIdx].current != LOC_CURRENT_NO_ENTRY) {
        /* 検索単語の格納位置アドレスを取得 */
        data = data_top + psrhCache->storebuff[abIdx].top + psrhCache->storebuff[abIdx].current;

        /* 頻度値を取得する */
        hindo = (NJ_INT16)*((NJ_UINT8 *)(HINDO_NO_TOP_ADDR(loctset->loct.handle)
                                         + get_stem_hindo(loctset->loct.handle, data)));
        /* 辞書バージョンがver.3.0以外 */
        if (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION3) {
            /* 辞書頻度を元に評価値を算出 */
            hindo = CALCULATE_HINDO(hindo, loctset->dic_freq.base, loctset->dic_freq.high, COMP_DIC_FREQ_DIV);

        }
        
    } else {
        /* 頻度がマイナス */
        hindo = -1;
    }

    return hindo;
}
