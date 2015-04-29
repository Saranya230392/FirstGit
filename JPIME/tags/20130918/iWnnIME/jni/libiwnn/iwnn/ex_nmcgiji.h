/**
 * @file
 *  [拡張]数字交じり変換候補部定義
 *
 *   数字交じり変換候補取得を使用する為の外部関数宣言を行う
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2009-2010 All Rights Reserved.
 */

/************************************************/
/*           インクルードファイル               */
/************************************************/
#ifndef _EX_NMCGIJI_H_
#define _EX_NMCGIJI_H_
#include "nj_lib.h"
#include "nj_dicif.h"

/************************************************/
/* 辞書頻度定義                                 */
/************************************************/
#define NJ_DEFAULT_FREQ_DIC_NMC_PROGRAM_H_HIGH    10    /**< デフォルト辞書頻度::数字交じり変換擬似辞書[変換用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_NMC_PROGRAM_H_BASE    0     /**< デフォルト辞書頻度::数字交じり変換擬似辞書[変換用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_NMC_PROGRAM_Y_HIGH    0     /**< デフォルト辞書頻度::数字交じり変換擬似辞書[予測用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_NMC_PROGRAM_Y_BASE    10    /**< デフォルト辞書頻度::数字交じり変換擬似辞書[予測用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_NMC_PROGRAM_M_HIGH    0     /**< デフォルト辞書頻度::数字交じり変換擬似辞書[形態素解析用]-最高-   */
#define NJ_DEFAULT_FREQ_DIC_NMC_PROGRAM_M_BASE    10    /**< デフォルト辞書頻度::数字交じり変換擬似辞書[形態素解析用]-底上げ- */

/***********************************************************************
 * 辞書インタフェース extern 宣言
 ***********************************************************************/
NJ_EXTERN NJ_INT16 njex_nmcgiji_dic(NJ_CLASS* iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE* message);

#endif /*_EX_NMCGIJI_H_*/
