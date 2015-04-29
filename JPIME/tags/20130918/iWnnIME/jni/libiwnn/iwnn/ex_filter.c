/**
 * @file
 *  [拡張]フィルタリング関数
 *
 *   フィルタリング処理を実現するためのフィルタリング関数群
 *
 * @author
 * (C) OMRON SOFTWARE CO.,Ltd. 2008-2012 All Rights Reserved
 */
#include "ex_filter.h"

/**
 * 辞書引きフィルタリング インターフェース
 *
 * @param[in]     iwnn     : iWnn内部情報(通常は参照のみ)
 * @param[in,out] message  : iWnn←→フィルタリング関数間でやり取りする情報
 *
 * @retval 1 使用
 * @retval 0 未使用
 *
 */
NJ_EXTERN NJ_INT16 njex_phase1_filter(NJ_CLASS * iwnn, NJ_PHASE1_FILTER_MESSAGE *message)
{

    return 1;
}

/**
 * 候補フィルタリング インターフェース
 *
 * @param[in]     iwnn     : iWnn内部情報(通常は参照のみ)
 * @param[in,out] message  : iWnn←→フィルタリング関数間でやり取りする情報
 *
 * @retval 1 使用
 * @retval 0 未使用
 *
 */
NJ_EXTERN NJ_INT16 njex_phase2_filter(NJ_CLASS * iwnn, NJ_PHASE2_FILTER_MESSAGE *message)
{

    return 1;
}

/**
 * 統合辞書フィルタリング インターフェース
 *
 * @param[in]     iwnn     : iWnn内部情報(通常は参照のみ)
 * @param[in,out] message  : iWnn←→フィルタリング関数間でやり取りする情報
 *
 * @retval 1 使用
 * @retval 0 未使用
 *
 */
NJ_EXTERN NJ_INT16 njex_phase3_filter(NJ_CLASS * iwnn, NJ_PHASE3_FILTER_MESSAGE *message)
{

    return 1;
}
