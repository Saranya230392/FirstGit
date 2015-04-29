/**
 * @file
 *   辞書引き部 共通定義
 *
 *   辞書引き部に関してエンジン内で公開する事項をまとめる
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2011 All Rights Reserved.
 */

#ifndef _NJ_DIC_H_
#define _NJ_DIC_H_

/************************************************/
/*            定数定義                          */
/************************************************/
#define NJ_DIC_TYPE_JIRITSU                         0x00000000      /**< 辞書種別 自立語辞書 */
#define NJ_DIC_TYPE_FZK                             0x00000001      /**< 辞書種別 付属語辞書 */
#define NJ_DIC_TYPE_TANKANJI                        0x00000002      /**< 辞書種別 単漢字辞書 */
#define NJ_DIC_TYPE_CUSTOM_COMPRESS                 0x00000003      /**< 辞書種別 圧縮カスタマイズ辞書 */
#define NJ_DIC_TYPE_STDFORE                         0x00000004      /**< 辞書種別 標準予測辞書 */
#define NJ_DIC_TYPE_FORECONV                        0x00000005      /**< 辞書種別 予測変換辞書 */
#define NJ_DIC_TYPE_YOMINASHI                       0x00010000      /**< 辞書種別 読み無し予測辞書 */
#define NJ_DIC_TYPE_LEARN                           0x80020000      /**< 辞書種別 学習辞書 */
#define NJ_DIC_TYPE_CUSTOM_INCOMPRESS               0x00020002      /**< 辞書種別 非圧縮カスタマイズ辞書 */
#define NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN         0x00020003      /**< 辞書種別 非圧縮カスタマイズ辞書(学習辞書変更) */
#define NJ_DIC_TYPE_USER                            0x80030000      /**< 辞書種別 ユーザ辞書 */
#define NJ_DIC_TYPE_RULE                            0x000F0000      /**< 辞書種別 ルール辞書 */
#define NJ_DIC_TYPE_EXT_YOMINASI                    0x00040000      /**< 辞書種別 拡張読み無し予測辞書 */
#define NJ_DIC_TYPE_FUSION_AWNN                     0x00000006      /**< 辞書種別 統合辞書(AWnnタイプ) */
#define NJ_DIC_TYPE_FUSION                          0x00050000      /**< 辞書種別 統合辞書 */
#define NJ_DIC_TYPE_PROGRAM                         0x000E0000      /**< 辞書種別 擬似辞書 */
#define NJ_DIC_TYPE_PROGRAM_FZK                     0x000E0001      /**< 辞書種別 擬似辞書-付属語- */
#define NJ_DIC_TYPE_FUSION_AWNN_STORAGE             0x40000006      /**< 辞書種別 統合辞書(ストレージ辞書)(AWnnタイプ) */
#define NJ_DIC_TYPE_FUSION_STORAGE                  0x40050000      /**< 辞書種別 統合辞書(ストレージ辞書) */
#define NJ_DIC_TYPE_FUSION_HIGH_COMPRESS            0x00050001      /**< 辞書種別 統合辞書(高圧縮タイプ) */
#define NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE    0x40050001      /**< 辞書種別 統合辞書(ストレージ辞書)(高圧縮タイプ) */


/* 変換部等で必要な品詞種別 */
#define NJ_HINSI_V2_F            0      /**< 品詞種別 文節末 */
#define NJ_HINSI_GIJI_F          1      /**< 品詞種別 擬似（前） */
#define NJ_HINSI_GIJI_B          2      /**< 品詞種別 擬似（後） */
#define NJ_HINSI_SUUJI_B        14      /**< 品詞種別 数字（後） */
#define NJ_HINSI_BUNTOU_B        3      /**< 品詞種別 文頭 */
/* 評価部等で必要な品詞種別 */
#define NJ_HINSI_TANKANJI_F      4      /**< 品詞種別 単漢字（前） */
#define NJ_HINSI_TANKANJI_B      5      /**< 品詞種別 単漢字（後） */
/* 品詞グループ */
#define NJ_HINSI_MEISI_F         6      /**< 品詞種別 名詞（前） */
#define NJ_HINSI_MEISI_B         7      /**< 品詞種別 名詞（後） */
#define NJ_HINSI_JINMEI_F        8      /**< 品詞種別 人名（前） */
#define NJ_HINSI_JINMEI_B        9      /**< 品詞種別 人名（後） */
#define NJ_HINSI_MEISI_NO_CONJ_F 10     /**< 品詞種別 名詞(スル活用なし) */
#define NJ_HINSI_MEISI_NO_CONJ_B 11     /**< 品詞種別 名詞(スル活用なし) */
#define NJ_HINSI_CHIMEI_F       10      /**< 品詞種別 地名（前） */
#define NJ_HINSI_CHIMEI_B       11      /**< 品詞種別 地名（後） */
#define NJ_HINSI_KIGOU_F        12      /**< 品詞種別 記号（前） */
#define NJ_HINSI_KIGOU_B        13      /**< 品詞種別 記号（後） */
#define NJ_HINSI_V1_F           15      /**< 品詞種別 文末 */
#define NJ_HINSI_V3_F           16      /**< 品詞種別 文中文節末 */

#define NJ_RULE_TYPE_BTOF       0
#define NJ_RULE_TYPE_FTOB       1

#define NJD_SAME_INDEX_LIMIT    50

#define HGROUP_COUNT            2

#define DIC_FREQ_BASE 0                                 /**< 辞書頻度下限 */
#define DIC_FREQ_HIGH NJ_NUM_THOUSAND                   /**< 辞書頻度上限 */

#define CAND_FZK_HINDO_BIAS      32                     /**< 不要付属語判定 - バイアス値     */
#define CAND_FZK_CAST_FREQ       (DIC_FREQ_HIGH + 1)    /**< 不要付属語判定 - 減算候補頻度値 */

#define NJ_FZK_CONJ_IDENTIFIER    0x434f4e4a    /**< データヘッダ: 活用形変形データ識別子 */


/************************************************/
/*           マクロ定義                         */
/************************************************/
#define NJ_INT16_READ(in)                                               \
    (((((NJ_INT16)((in)[0])) << 8) & 0xff00U) + ((in)[1] & 0xffU))

#define NJ_INT32_READ(in)                                               \
    (((((NJ_INT32)((in)[0])) << 24) & 0xff000000) |                     \
     ((((NJ_INT32)((in)[1])) << 16) &   0xff0000) |                     \
     ((((NJ_INT32)((in)[2])) <<  8) &     0xff00) |                     \
     ((((NJ_INT32)((in)[3]))      ) &       0xff))

#define NJ_INT24_READ(in)                          \
    (((((NJ_INT32)((in)[0])) << 16) & 0xff0000) |  \
     ((((NJ_INT32)((in)[1])) <<  8) &   0xff00) |  \
     ((((NJ_INT32)((in)[2]))      ) &     0xff))

#define NJ_INT32_WRITE(to, from)\
        {(to)[0]=(NJ_UINT8)(((from)>>24) & 0x000000ff);\
         (to)[1]=(NJ_UINT8)(((from)>>16) & 0x000000ff);\
         (to)[2]=(NJ_UINT8)(((from)>>8) & 0x000000ff);\
         (to)[3]=(NJ_UINT8)((from) & 0x000000ff);}

#define NJ_INT16_WRITE(to, from)\
        {(to)[0]=(NJ_UINT8)(((from)>>8) & 0x00ff);\
         (to)[1]=(NJ_UINT8)((from) & 0x00ff);}

#define NJ_GET_MAX_YLEN(h) ((NJ_INT16)(NJ_INT16_READ((h)+0x16)/sizeof(NJ_CHAR)))

#define NJ_GET_MAX_KLEN(h) ((NJ_INT16)(NJ_INT16_READ((h)+0x1A)/sizeof(NJ_CHAR)))

#define NJ_GET_DIC_TYPE(h) ((NJ_UINT32)(NJ_INT32_READ((h)+8)))

#define NJ_CHECK_USE_DIC_FREQ(df) (((df)->high < (df)->base) ? 0 : 1)

/************************************************/
/*           データ構造定義                     */
/************************************************/

#endif /* _NJ_DIC_H_ */
