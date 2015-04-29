/**
 * @file
 *  [拡張]擬似候補部定義
 *
 *   擬似候補取得を使用する為の外部関数宣言を行う
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2010 All Rights Reserved.
 */

/************************************************/
/*           インクルードファイル               */
/************************************************/
#ifndef _EX_PREDG_H_
#define _EX_PREDG_H_
#include "nj_lib.h"
#include "nj_dicif.h"

/************************************************/
/* 辞書頻度定義                                 */
/************************************************/
#define NJ_DEFAULT_FREQ_DIC_LIMIT_PRED_PROGRAM_H_HIGH    0     /**< デフォルト辞書頻度::絞込予測用擬似辞書[変換用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_LIMIT_PRED_PROGRAM_H_BASE    10    /**< デフォルト辞書頻度::絞込予測用擬似辞書[変換用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_LIMIT_PRED_PROGRAM_Y_HIGH    1000  /**< デフォルト辞書頻度::絞込予測用擬似辞書[予測用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_LIMIT_PRED_PROGRAM_Y_BASE    950   /**< デフォルト辞書頻度::絞込予測用擬似辞書[予測用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_LIMIT_PRED_PROGRAM_M_HIGH    0     /**< デフォルト辞書頻度::絞込予測用擬似辞書[形態素解析用]-最高-   */
#define NJ_DEFAULT_FREQ_DIC_LIMIT_PRED_PROGRAM_M_BASE    10    /**< デフォルト辞書頻度::絞込予測用擬似辞書[形態素解析用]-底上げ- */

/***********************************************************************
 * 辞書インタフェース extern 宣言
 ***********************************************************************/
NJ_EXTERN NJ_INT16 njex_pred_giji_dic(NJ_CLASS* iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE* message);


#endif /*_EX_PREDG_H_*/
