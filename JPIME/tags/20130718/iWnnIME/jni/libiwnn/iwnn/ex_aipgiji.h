/**
 * @file
 *  [拡張]AI予測用擬似候補部定義
 *
 *   AI予測用擬似候補取得を使用する為の外部関数宣言を行う
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2010 All Rights Reserved.
 */
#ifndef _EX_AIPGIJI_H_
#define _EX_AIPGIJI_H_
#include "nj_lib.h"
#include "nj_dicif.h"

/************************************************/
/*           インクルードファイル               */
/************************************************/
/************************************************/
/* 辞書頻度定義                                 */
/************************************************/
#define NJ_DEFAULT_FREQ_DIC_AIP_PROGRAM_H_HIGH    550   /**< デフォルト辞書頻度::AI予測用擬似辞書[変換用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_AIP_PROGRAM_H_BASE    400   /**< デフォルト辞書頻度::AI予測用擬似辞書[変換用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_AIP_PROGRAM_Y_HIGH    560   /**< デフォルト辞書頻度::AI予測用擬似辞書[予測用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_AIP_PROGRAM_Y_BASE    100   /**< デフォルト辞書頻度::AI予測用擬似辞書[予測用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_AIP_PROGRAM_M_HIGH    0     /**< デフォルト辞書頻度::AI予測用擬似辞書[形態素解析用]-最高-   */
#define NJ_DEFAULT_FREQ_DIC_AIP_PROGRAM_M_BASE    10    /**< デフォルト辞書頻度::AI予測用擬似辞書[形態素解析用]-底上げ- */

/***********************************************************************
 * 辞書インタフェース extern 宣言
 ***********************************************************************/
NJ_EXTERN NJ_INT16 njex_aip_giji_dic(NJ_CLASS* iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE* message);
NJ_EXTERN NJ_INT16 njex_aip_get_ext_area_size(NJ_CLASS *iwnn, NJ_UINT32 *size);

#endif /*_EX_AIPGIJI_H_*/
