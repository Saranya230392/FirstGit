/**
 * @file
 *  [拡張]数字繋がり予測候補部定義
 *
 *   数字繋がり予測候補取得を使用する為の外部関数宣言を行う
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2009-2010 All Rights Reserved.
 */

/************************************************/
/*           インクルードファイル               */
/************************************************/
#ifndef _EX_NMFGIJI_H_
#define _EX_NMFGIJI_H_
#include "nj_lib.h"
#include "nj_dicif.h"


/************************************************/
/* 辞書頻度定義                                 */
/************************************************/
#define NJ_DEFAULT_FREQ_DIC_NMF_PROGRAM_H_HIGH    0     /**< デフォルト辞書頻度::数字繋がり予測擬似辞書[変換用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_NMF_PROGRAM_H_BASE    10    /**< デフォルト辞書頻度::数字繋がり予測擬似辞書[変換用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_NMF_PROGRAM_Y_HIGH    294   /**< デフォルト辞書頻度::数字繋がり予測擬似辞書[予測用]-最高-         */
#define NJ_DEFAULT_FREQ_DIC_NMF_PROGRAM_Y_BASE    245   /**< デフォルト辞書頻度::数字繋がり予測擬似辞書[予測用]-底上げ-       */
#define NJ_DEFAULT_FREQ_DIC_NMF_PROGRAM_M_HIGH    0     /**< デフォルト辞書頻度::数字繋がり予測擬似辞書[形態素解析用]-最高-   */
#define NJ_DEFAULT_FREQ_DIC_NMF_PROGRAM_M_BASE    10    /**< デフォルト辞書頻度::数字繋がり予測擬似辞書[形態素解析用]-底上げ- */


/************************************************/
/* 数字繋がり予測候補部定義                        */
/************************************************/
/**
 * 数字繋がり予測拡張領域構造体
 */
typedef struct {
    NJ_UINT32 bnum;        /* 作成候補直前数値 */
    NJ_UINT32 fnum;        /* 前候補直前数値 */
    NJ_UINT16 divnum;      /* 分割数 */
    NJ_UINT16 fhyouki_len; /* 前候補表記文字長 */
    NJ_CHAR   fhyouki[NJ_MAX_RESULT_LEN + NJ_TERM_SIZE]; /* 前候補表記文字 */
} NJG_NMF_DIVINF;


/***********************************************************************
 * 辞書インタフェース extern 宣言
 ***********************************************************************/
NJ_EXTERN NJ_INT16 njex_nmfgiji_dic(NJ_CLASS* iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE* message);

#endif /*_EX_NMFGIJI_H_*/
