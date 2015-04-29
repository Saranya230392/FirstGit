/**
 * @file
 *   フィルターインターフェース 定義ファイル
 * 
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2012 All Rights Reserved.
 */
#ifndef _NJ_FILTER_H_
#define _NJ_FILTER_H_

/**
 * 辞書引きフィルターメッセージ定義
 */
typedef struct {
    NJ_SEARCH_CONDITION * condition; /**< 検索条件 */
    NJ_CHAR *   stroke;         /**< 読み文字列バッファ         */
    NJ_CHAR *   string;         /**< 表記文字列バッファ         */
    NJ_CHAR *   additional;     /**< 付加情報バッファ           */
    NJ_INT16    stroke_len;     /**< 読み文字配列長             */
    NJ_INT16    string_len;     /**< 表記文字配列長             */
    NJ_INT32    additional_len; /**< 付加情報文字配列長         */
    NJ_RESULT * result;         /**< 単語情報                   */
    NJ_VOID *   option;         /**< フィルタオプション         */
} NJ_PHASE1_FILTER_MESSAGE;


/**
 * 辞書引きフィルターインタフェース定義
 *
 * @param[in]     iwnn                  解析情報クラス
 * @param[in]     message->condition    検索条件
 * @param[in]     message->stroke       読み文字列バッファ
 * @param[in]     message->string       表記文字列バッファ
 * @param[in]     message->stroke_len   読み文字配列長
 * @param[in]     message->string_len   表記文字配列長
 * @param[in]     message->result       未使用
 *
 * @retval !=0 使用
 * @retval 0 未使用
 */
typedef NJ_INT16(*NJ_PHASE1_FILTER_IF)(NJ_CLASS * iwnn, NJ_PHASE1_FILTER_MESSAGE *message);

/**
 * 候補フィルターメッセージ定義
 */
typedef struct {
    NJ_RESULT * result;         /**< 単語情報                   */
    NJ_VOID *   option;         /**< フィルタオプション          */
} NJ_PHASE2_FILTER_MESSAGE;

/**
 * 外部変数から読み最大文字配列長を取得
 *
 * @param[in]  iwnn  外部変数(NJ_CLASS)
 *
 * @retval           読み最大文字配列長
 */
#define NJ_GET_FILTER_CHAR_MAX(iwnn) ((iwnn)->environment.option.char_max)

/**
 * 外部変数から読み最小文字配列長を取得
 *
 * @param[in]  iwnn  外部変数(NJ_CLASS)
 *
 * @retval           読み最大文字配列長
 */
#define NJ_GET_FILTER_CHAR_MIN(iwnn) ((iwnn)->environment.option.char_min)

/**
 * 候補フィルターインタフェース定義
 *
 * @param[in]     iwnn                  解析情報クラス
 * @param[in]     message->result       単語情報
 *
 * @retval !=0 使用
 * @retval 0 未使用
 */
typedef NJ_INT16(*NJ_PHASE2_FILTER_IF)(NJ_CLASS * iwnn, NJ_PHASE2_FILTER_MESSAGE *message);

/**
 * 統合辞書フィルターメッセージ定義
 */
typedef struct {
    NJ_CHAR *   stroke;         /**< 読み文字列バッファ         */
    NJ_CHAR *   string;         /**< 表記文字列バッファ         */
    NJ_CHAR *   additional;     /**< 付加情報バッファ           */
    NJ_INT16    stroke_len;     /**< 読み文字配列長             */
    NJ_INT16    string_len;     /**< 表記文字配列長             */
    NJ_INT32    additional_len; /**< 付加情報文字配列長         */
    NJ_VOID *   option;         /**< フィルタオプション         */
} NJ_PHASE3_FILTER_MESSAGE;


/**
 * 統合辞書フィルターインタフェース定義
 *
 * @param[in]     iwnn                  解析情報クラス
 * @param[in]     message->condition    検索条件
 * @param[in]     message->stroke       読み文字列バッファ
 * @param[in]     message->string       表記文字列バッファ
 * @param[in]     message->stroke_len   読み文字配列長
 * @param[in]     message->string_len   表記文字配列長
 * @param[in]     message->result       未使用
 *
 * @retval !=0 使用
 * @retval 0 未使用
 */
typedef NJ_INT16(*NJ_PHASE3_FILTER_IF)(NJ_CLASS * iwnn, NJ_PHASE3_FILTER_MESSAGE *message);


#endif /* _NJ_FILTER_H_ */
