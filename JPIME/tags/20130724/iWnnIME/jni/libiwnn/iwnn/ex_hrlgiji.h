/**
 * @file
 *  [拡張]品詞繋がり学習用擬似候補部定義
 *
 *   品詞繋がり学習擬似候補取得を使用する為の外部関数宣言を行う
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2009-2010 All Rights Reserved.
 */

/************************************************/
/*           インクルードファイル               */
/************************************************/
#ifndef _EX_HRLGIJI_H_
#define _EX_HRLGIJI_H_
#include "nj_lib.h"
#include "nj_dicif.h"


/************************************************/
/* 辞書頻度定義                                 */
/************************************************/
#define NJ_DEFAULT_FREQ_DIC_HRL_PROGRAM_H_HIGH    10    /**< デフォルト辞書頻度::品詞繋がり学習用擬似辞書[変換用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_HRL_PROGRAM_H_BASE    0     /**< デフォルト辞書頻度::品詞繋がり学習用擬似辞書[変換用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_HRL_PROGRAM_Y_HIGH    294   /**< デフォルト辞書頻度::品詞繋がり学習用擬似辞書[予測用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_HRL_PROGRAM_Y_BASE    245   /**< デフォルト辞書頻度::品詞繋がり学習用擬似辞書[予測用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_HRL_PROGRAM_M_HIGH    0     /**< デフォルト辞書頻度::品詞繋がり学習用擬似辞書[形態素解析用]-最高-   */
#define NJ_DEFAULT_FREQ_DIC_HRL_PROGRAM_M_BASE    10    /**< デフォルト辞書頻度::品詞繋がり学習用擬似辞書[形態素解析用]-底上げ- */


/**
 * 対象属性マスク
 *
 * 学習の対象とする属性をビットで指定する
 */
#ifndef NJG_HRL_ATTR_MASK
#define NJG_HRL_ATTR_MASK    0x80000000
#endif

/**
 * １品詞あたりの保存件数
 *
 * - 最大値：３０
 * - 最小値：１０
 * - 初期値：１５
 */
#ifndef NJG_HRL_INDEX_CNT
#define NJG_HRL_INDEX_CNT    15
#endif

/**
 * UNDO管理領域保存件数
 *
 * - 最大値：２０
 * - 最小値：５
 * - 初期値：７
 */
#ifndef NJG_HRL_UNDO_MAX
#define NJG_HRL_UNDO_MAX     7
#endif

/**
 * 学習辞書データ保存件数
 *
 * - 最大値：３０００
 * - 最小値：１００
 * - 初期値：１０００
 */
#ifndef NJG_HRL_LEARN_COUNT
#define NJG_HRL_LEARN_COUNT  1000
#endif

/***********************************************************************
 * 辞書インタフェース extern 宣言
 ***********************************************************************/
NJ_EXTERN NJ_INT16 njex_hrl_giji_dic(NJ_CLASS* iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE* message);
NJ_EXTERN NJ_INT16 njex_hrl_init_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE rhandle, NJ_VOID *ext_area, NJ_UINT32 size);
NJ_EXTERN NJ_INT16 njex_hrl_check_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE rhandle, NJ_VOID *ext_area, NJ_UINT32 size);
NJ_EXTERN NJ_INT16 njex_hrl_get_ext_area_size(NJ_CLASS *iwnn, NJ_DIC_HANDLE rhandle, NJ_UINT32 *size);

#endif /*_EX_HRLGIJI_H_*/
