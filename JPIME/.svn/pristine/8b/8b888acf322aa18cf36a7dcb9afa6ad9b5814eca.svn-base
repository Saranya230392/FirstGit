/**
 * @file
 *   デコメ絵文字読み入力定義（内部編）
 *
 *   API処理内部用に必要な定義を行う。
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2010 All Rights Reserved.
 */
#ifndef _DEMOJI_API_H_
#define _DEMOJI_API_H_

/************************************************/
/*                 define 宣 言                 */
/************************************************/
/**
 * 初期化フラグ定義
 */
#define DL_INIT_OFF         ((DL_UINT8)0)       /**< 初期化フラグOFF */
#define DL_INIT_ON          ((DL_UINT8)1)       /**< 初期化フラグON  */

/**
 * NJ_CHARにテーブルに定義された16bit文字をコピーする。
 *
 * @note Big Endianで2byte格納する。
 *
 * @param[in] to    コピー先 (NJ_CHAR*)
 * @param[in] from  コピー元 (NJ_UINT16/NJ_INT16)
 */
#define DL_COPY_INT16_TO_CHAR(to, from)                           \
    { ((NJ_UINT8*)(to))[0] = (NJ_UINT8)(((from) >> 8) & 0x00ff);  \
        ((NJ_UINT8*)(to))[1] = (NJ_UINT8)((from) & 0x00ff); }

/************************************************/
/*              static 変 数 宣 言              */
/************************************************/
/**
 * 品詞変換テーブル
 */
static const DL_UINT16 DL_HINSI_TBL[DL_PART_CNT][3] = {
    /* ライブラリ品詞,     iWnn前品詞番号,     iWnn後品詞番号 */
    { DL_PART_NONE,        (DL_UINT16)0x00BE,  (DL_UINT16)0x0025 },
    { DL_PART_CATEGORY_1,  (DL_UINT16)0x00BE,  (DL_UINT16)0x0025 },
    { DL_PART_CATEGORY_2,  (DL_UINT16)0x00BE,  (DL_UINT16)0x002D },
    { DL_PART_CATEGORY_3,  (DL_UINT16)0x00BE,  (DL_UINT16)0x0026 },
    { DL_PART_CATEGORY_4,  (DL_UINT16)0x00BE,  (DL_UINT16)0x003F },
    { DL_PART_CATEGORY_5,  (DL_UINT16)0x00BE,  (DL_UINT16)0x0065 },
    { DL_PART_CATEGORY_6,  (DL_UINT16)0x00BE,  (DL_UINT16)0x0078 },
    { DL_PART_CATEGORY_7,  (DL_UINT16)0x00BE,  (DL_UINT16)0x004C },
    { DL_PART_CATEGORY_8,  (DL_UINT16)0x00BE,  (DL_UINT16)0x0002 },
    { DL_PART_CATEGORY_9,  (DL_UINT16)0x00BE,  (DL_UINT16)0x0025 },
    { DL_PART_CATEGORY_10, (DL_UINT16)0x00BE,  (DL_UINT16)0x0025 },
    { DL_PART_CATEGORY_11, (DL_UINT16)0x00BE,  (DL_UINT16)0x0027 },
    { DL_PART_CATEGORY_12, (DL_UINT16)0x00BE,  (DL_UINT16)0x0025 },
    { DL_PART_CATEGORY_13, (DL_UINT16)0x00BE,  (DL_UINT16)0x0025 },
    { DL_PART_CATEGORY_14, (DL_UINT16)0x00BE,  (DL_UINT16)0x0044 },
    { DL_PART_CATEGORY_15, (DL_UINT16)0x00BE,  (DL_UINT16)0x0065 },
    { DL_PART_CATEGORY_16, (DL_UINT16)0x00BE,  (DL_UINT16)0x0078 },
    { DL_PART_CATEGORY_17, (DL_UINT16)0x00BE,  (DL_UINT16)0x002E },
    { DL_PART_CATEGORY_18, (DL_UINT16)0x00BE,  (DL_UINT16)0x0040 },
    { DL_PART_CATEGORY_19, (DL_UINT16)0x00BE,  (DL_UINT16)0x0043 },
    { DL_PART_CATEGORY_20, (DL_UINT16)0x00BE,  (DL_UINT16)0x0042 },
    { DL_PART_CATEGORY_21, (DL_UINT16)0x00BE,  (DL_UINT16)0x0021 }
};

/**
 * 品詞逆変換テーブル
 */
static const DL_UINT16 hinsiTblFromIWnn[295] = {
    0, 1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1, 1, 21, 21, 21, 1, 1, 3, 11,
    2, 1, 3, 3, 3, 2, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17,
    17, 17, 17, 17, 18, 18, 20, 19, 1, 14, 14, 14, 14, 14, 14, 1, 6, 6, 6, 11,
    8, 14, 14, 8, 8, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
    5, 5, 5, 1, 5, 1, 1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
    6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 1, 4, 4, 4, 4, 20, 20,
    5, 5, 6, 6, 5, 1, 1, 1, 1, 1, 20, 20, 6, 6, 6, 6, 6, 6, 20, 1,
    5, 1, 5, 5, 5, 5, 5, 5, 5, 5, 6, 20, 6, 6, 6, 6, 6, 6, 6, 5,
    5, 5, 5, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1, 1, 1, 12, 12, 12, 12, 12,
    12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
    12, 12, 12, 20, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
    12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
    12, 12, 12, 12, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 12, 12, 12, 12,
    12, 12, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 21
};


#endif /* _DEMOJI_API_H_ */
