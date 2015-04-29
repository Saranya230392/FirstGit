/**
 * @file
 *  [拡張] iWnn標準複合語予測検索用候補部定義
 *
 *   擬似候補取得を使用する為の外部関数宣言を行う
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2010 All Rights Reserved.
 */

/************************************************/
/*           インクルードファイル               */
/************************************************/
#ifndef _EX_CMPDG_H_
#define _EX_CMPDG_H_
#include "nj_lib.h"
#include "nj_dicif.h"

/************************************************/
/* 辞書頻度定義                                 */
/************************************************/
#define NJ_DEFAULT_FREQ_DIC_CMPDG_PROGRAM_H_HIGH    0     /**< デフォルト辞書頻度::複合語予測検索用擬似辞書[変換用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_CMPDG_PROGRAM_H_BASE    10    /**< デフォルト辞書頻度::複合語予測検索用擬似辞書[変換用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_CMPDG_PROGRAM_Y_HIGH    900   /**< デフォルト辞書頻度::複合語予測検索用擬似辞書[予測用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_CMPDG_PROGRAM_Y_BASE    401   /**< デフォルト辞書頻度::複合語予測検索用擬似辞書[予測用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_CMPDG_PROGRAM_M_HIGH    0     /**< デフォルト辞書頻度::複合語予測検索用擬似辞書[形態素解析用]-最高-   */
#define NJ_DEFAULT_FREQ_DIC_CMPDG_PROGRAM_M_BASE    10    /**< デフォルト辞書頻度::複合語予測検索用擬似辞書[形態素解析用]-底上げ- */

/***********************************************************************
 * 辞書インタフェース extern 宣言
 ***********************************************************************/
NJ_EXTERN NJ_INT16 njex_cmpdg_giji_dic(NJ_CLASS* iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE* message);


#endif /*_EX_CMPDG_H_*/
