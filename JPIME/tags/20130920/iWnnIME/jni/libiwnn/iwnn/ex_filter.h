/**
 * @file
 *  [拡張]フィルタリング定義
 *
 *   フィルタリング関数を使用する為の外部関数宣言を行う。
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2012 All Rights Reserved.
 */
#ifndef _EX_FILTER_H_
#define _EX_FILTER_H_
#include "nj_lib.h"
#include "njfilter.h"

/************************************************/
/*           インクルードファイル                  */
/************************************************/

/***********************************************************************
 * フィルタリングインタフェース プロトタイプ宣言
 ***********************************************************************/
NJ_EXTERN NJ_INT16 njex_phase1_filter(NJ_CLASS * iwnn, NJ_PHASE1_FILTER_MESSAGE *message);
NJ_EXTERN NJ_INT16 njex_phase2_filter(NJ_CLASS * iwnn, NJ_PHASE2_FILTER_MESSAGE *message);
NJ_EXTERN NJ_INT16 njex_phase3_filter(NJ_CLASS * iwnn, NJ_PHASE3_FILTER_MESSAGE *message);



#endif /*_EX_GIJI_H_*/
