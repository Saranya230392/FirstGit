/**
 * @file
 *   辞書アダプタ：ルール辞書アダプタ
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
#include "nj_dic.h"
#include "njd.h"

/************************************************/
/*              prototype  宣  言               */
/************************************************/

/************************************************/
/*              define  宣  言                  */
/************************************************/
#define DIC_VERSION(h) ((NJ_UINT32)(NJ_INT32_READ((h)+0x04)))
#define F_HINSI_SET_CNT(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x1C)))
#define B_HINSI_SET_CNT(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x1E)))
#define F_HINSI_TOP_ADDR(h) ((NJ_UINT8*)((h)+NJ_INT32_READ((h)+0x20)))
#define B_HINSI_TOP_ADDR(h) ((NJ_UINT8*)((h)+NJ_INT32_READ((h)+0x24)))
#define V2_F_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x28)))
#define BUN_B_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x2A)))
#define GIJI_F_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x2C)))
#define GIJI_B_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x2E)))
#define TAN_F_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x30)))
#define TAN_B_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x32)))
#define SUUJI_B_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x34)))
#define MEISI_F_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x36)))
#define MEISI_B_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x38)))
#define JINMEI_F_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x3A)))
#define JINMEI_B_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x3C)))
#define CHIMEI_F_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x3E)))
#define CHIMEI_B_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x40)))
#define KIGOU_F_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x42)))
#define KIGOU_B_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x44)))
#define V1_F_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x52)))
#define V3_F_HINSI(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x54)))

#define B_MEISI_GROUP_ADDR(h) ((NJ_UINT8 *)((h)+NJ_INT32_READ((h)+0x46)))
#define B_MEISI_GROUP_COUNT(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x4A)))

#define B_GIJI_GROUP_ADDR(h) ((NJ_UINT8 *)((h)+NJ_INT32_READ((h)+0x4C)))
#define B_GIJI_GROUP_COUNT(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x50)))
#define BIT_WIDTH_PER_ENTRY_HINSI_INFO_EXT(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x56)))
#define F_HINSI_EXT_TOP_ADDR(h) ((NJ_UINT8*)((h)+NJ_INT32_READ((h)+0x5A)))
#define B_HINSI_EXT_TOP_ADDR(h) ((NJ_UINT8*)((h)+NJ_INT32_READ((h)+0x5E)))

/**
 * ルール辞書アダプタ  指定品詞番号取得
 *
 * @param[in]  rule  ルール辞書ハンドル
 * @param[in]  type  品詞タイプ
 *
 * @retval >0 品詞番号
 * @retval 0  エラー
 */
NJ_INT16 njd_r_get_hinsi(NJ_DIC_HANDLE rule, NJ_UINT8 type) {


    /*
     * 指定した種類の品詞の品詞番号を取得する。
     * これらの品詞番号はルール辞書ヘッダー に格納されているので、これを返す。
     */

    /* 引数チェック */
    if (rule == NULL) {
        return 0; /*NCH_FB*/
    }

    switch (type) {
    case NJ_HINSI_V2_F :        /* V2の前品詞 */
        return V2_F_HINSI(rule);
    case NJ_HINSI_BUNTOU_B :    /* 文頭の後品詞 */
        return BUN_B_HINSI(rule);
    case NJ_HINSI_GIJI_F :      /* 擬似の前品詞 */
        return GIJI_F_HINSI(rule);
    case NJ_HINSI_GIJI_B :      /* 擬似の後品詞 */
        return GIJI_B_HINSI(rule);
    case NJ_HINSI_TANKANJI_F :  /* 単漢字の前品詞 */
        return TAN_F_HINSI(rule);
    case NJ_HINSI_TANKANJI_B :  /* 単漢字の後品詞 */
        return TAN_B_HINSI(rule);
    case NJ_HINSI_SUUJI_B:      /* 数字の後品詞 */
        return SUUJI_B_HINSI(rule);
    case NJ_HINSI_MEISI_F :     /* 一般名詞/固有名詞の前品詞 */
        return MEISI_F_HINSI(rule);
    case NJ_HINSI_MEISI_B :     /* 一般名詞/固有名詞の後品詞 */
        return MEISI_B_HINSI(rule);
    case NJ_HINSI_JINMEI_F :    /* 人名の前品詞 */
        return JINMEI_F_HINSI(rule);
    case NJ_HINSI_JINMEI_B :    /* 人名の後品詞 */
        return JINMEI_B_HINSI(rule);
    case NJ_HINSI_CHIMEI_F :    /* 地名/駅名の前品詞 */
        return CHIMEI_F_HINSI(rule);
    case NJ_HINSI_CHIMEI_B :    /* 地名/駅名の後品詞 */
        return CHIMEI_B_HINSI(rule);
    case NJ_HINSI_KIGOU_F :     /* 記号の前品詞 */
        return KIGOU_F_HINSI(rule);
    case NJ_HINSI_KIGOU_B :     /* 記号の後品詞 */
        return KIGOU_B_HINSI(rule);
    case NJ_HINSI_V1_F :        /* V1の前品詞 */
        return V1_F_HINSI(rule);
    case NJ_HINSI_V3_F :        /* V3の前品詞 */
        return V3_F_HINSI(rule);
    default:
    /* 引数チェック */
        return 0; /*NCH_FB*/
    }
}


/**
 * ルール辞書アダプタ  接続情報取得
 *
 * @param[in]  rule    ルール辞書ハンドル
 * @param[in]  hinsi   品詞番号(1以上)
 * @param[in]  type    品詞タイプ(0:後品詞,1:前品詞)
 * @param[out] connect 接続情報のポインタ
 *
 * @return 0
 */
NJ_INT16 njd_r_get_connect(NJ_DIC_HANDLE rule, NJ_UINT16 hinsi, NJ_UINT8 type, NJ_UINT8 **connect) {
    NJ_UINT16 i, rec_len;



    /* 引数チェック */
    if (rule == NULL) {
        return 0; /*NCH_FB*/
    }
    if (hinsi < 1) {
        return 0;
    }

    if (type == NJ_RULE_TYPE_BTOF) {    /* 後品詞が指定された */
        i = F_HINSI_SET_CNT(rule);      /* レコードサイズ算出 */
        rec_len = (NJ_UINT16)((i + 7) / 8);
                                        /* 接続情報ポインタ算出 */
        *connect = (NJ_UINT8*)(F_HINSI_TOP_ADDR(rule) + ((hinsi - 1) * rec_len));
    } else {                            /* 前品詞が指定された */
        i = B_HINSI_SET_CNT(rule);      /* レコードサイズ算出 */
        rec_len = (NJ_UINT16)((i + 7) / 8);
                                        /* 接続情報ポインタ算出 */
        *connect = (NJ_UINT8*)(B_HINSI_TOP_ADDR(rule) + ((hinsi - 1) * rec_len));
    }
    return 0;
}


/**
 * ルール辞書アダプタ  最大品詞数取得
 *
 *    Parameter : 
 * @param[in]   rule    ルール辞書ハンドル
 * @param[out]  fcount  前品詞最大品詞数
 * @param[out]  rcount  後品詞最大品詞数
 *
 * @return 0
 */
NJ_INT16 njd_r_get_count(NJ_DIC_HANDLE rule, NJ_UINT16 *fcount, NJ_UINT16 *rcount) {


    /*
     * ルール辞書が保持する前品詞、後品詞の最大品詞数をヘッダーから取得する。
     */

    /* 引数チェック */
    if (rule == NULL) {
        return 0; /*NCH_FB*/
    }

    *fcount = F_HINSI_SET_CNT(rule);
    *rcount = B_HINSI_SET_CNT(rule);

    return 0;
}


/**
 * 指定された品詞番号に対応する形態素解析用品詞グループを返す
 *
 * @param[in]  rule    ルール辞書ハンドル
 * @param[in]  pos     品詞番号
 *
 * @retval 0 品詞グループ該当なし
 * @retval 1 名詞グループ
 * @retval 2 擬似グループ
 * @retval <0 エラー
 */
NJ_INT16 njd_r_check_group(NJ_DIC_HANDLE rule, NJ_UINT16 pos) {
    NJ_INT16 i;
    NJ_UINT16 j;
    NJ_UINT8 *ptr = NULL;
    NJ_UINT16 count = 0;


    /* 後品詞   */
    for (i = 0; i < HGROUP_COUNT; i++) {
        switch (i) {
        case MM_HGROUP_MEISI:
            ptr = B_MEISI_GROUP_ADDR(rule);
            count = B_MEISI_GROUP_COUNT(rule);
            break;
        case MM_HGROUP_GIJI:
            ptr = B_GIJI_GROUP_ADDR(rule);
            count = B_GIJI_GROUP_COUNT(rule);
            break;
        default:
            return MM_HGROUP_OTHER; /* ここに来ることはない */ /*NCH*/
        }
        for (j = 0; j < count; j++) {
            if (NJ_INT16_READ(ptr) == pos) {
                /* テーブルに存在               */
                /* 品詞グループの番号を返す     */
                return i;
            }
            ptr += sizeof(NJ_UINT16);
        }
    }

    return MM_HGROUP_OTHER;
}


/**
 * ルール辞書アダプタ  接続情報（拡張）取得
 *
 * @param[in]  rule    ルール辞書ハンドル
 * @param[in]  hinsi   品詞番号(1以上)
 * @param[in]  type    品詞タイプ(0:後品詞,1:前品詞)
 * @param[out] connect 接続情報（拡張）のポインタ
 *
 * @return 0
 */
NJ_INT16 njd_r_get_connect_ext(NJ_DIC_HANDLE rule, NJ_UINT16 hinsi, NJ_UINT8 type, NJ_UINT8 **connect) {
    NJ_UINT16 i, rec_len;



    /* エラーのときに必ず *connect が NULL となるよう、あらかじめ NULL を代入しておく */
    *connect = NULL;

    /* 引数チェック */
    if (rule == NULL) {
        return 0; /*NCH_FB*/
    }
    if (hinsi < 1) {
        return 0;
    }

    /* 辞書バージョンチェック */
    if (DIC_VERSION(rule) != NJ_DIC_VERSION3) {
        /* 接続情報（拡張）が存在しない場合は、NULL を返す */
        return 0;
    }

    if (type == NJ_RULE_TYPE_BTOF) {    /* 後品詞が指定された */
        i = F_HINSI_SET_CNT(rule);      /* レコードサイズ算出 */
        rec_len = (NJ_UINT16)((i + 7) / 8);
                                        /* 接続情報ポインタ算出 */
        *connect = (NJ_UINT8*)(F_HINSI_EXT_TOP_ADDR(rule) + ((hinsi - 1) * rec_len));
    }
    return 0;
}
