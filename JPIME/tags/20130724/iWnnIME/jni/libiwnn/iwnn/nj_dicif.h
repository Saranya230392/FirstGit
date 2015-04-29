/**
 * @file
 *   辞書インターフェース 定義ファイル
 * 
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2010 All Rights Reserved.
 */
#ifndef _NJ_DICIF_H_
#define _NJ_DICIF_H_

#define NJG_OP_SEARCH           0x0001 /**< 初回検索            */
#define NJG_OP_SEARCH_NEXT      0x0002 /**< 次候補検索          */
#define NJG_OP_GET_WORD_INFO    0x0003 /**< 単語情報取得        */
#define NJG_OP_GET_STROKE       0x0004 /**< 読み文字列取得      */
#define NJG_OP_GET_STRING       0x0005 /**< 表記文字列取得      */
#define NJG_OP_GET_ADDITIONAL   0x000A /**< 付加情報文字列取得  */
#define NJG_OP_LEARN            0x0006 /**< 単語学習            */
#define NJG_OP_UNDO_LEARN       0x0007 /**< 単語学習UNDO        */
#define NJG_OP_ADD_WORD         0x0008 /**< 単語登録            */
#define NJG_OP_DEL_WORD         0x0009 /**< 単語削除            */

/**
 * 擬似辞書 単語登録情報定義
 */
typedef struct {
    NJ_UINT16                   f_hinsi;        /**< 前品詞             */
    NJ_UINT16                   b_hinsi;        /**< 後品詞             */
    NJ_UINT16                   connect;        /**< 接続フラグ         */
    NJ_UINT16                   undo;           /**< アンドゥフラグ     */
} NJG_LEARN_WORD;

/**
 * 擬似辞書メッセージ定義
 */
typedef struct {
    NJ_SEARCH_CONDITION *       condition;      /**< 検索条件                                                     */
    NJ_SEARCH_LOCATION_SET *    location;       /**< 検索カーソル                                                 */
    NJ_DIC_SET *                dicset;         /**< 検索辞書セット                                               */
    NJ_WORD *                   word;           /**< 単語情報                                                     */
    NJG_LEARN_WORD *            lword;          /**< 単語登録情報                                                 */
    NJ_CHAR *                   stroke;         /**< 読み文字列取得先                                             */
    NJ_CHAR *                   string;         /**< 表記文字列取得先                                             */
    NJ_CHAR *                   additional;     /**< 付加情報文字列取得先                                         */
    NJ_UINT16                   stroke_size;    /**< strokeのサイズ（メッセージにより byte単位／文字単位が変わる）*/
    NJ_UINT16                   string_size;    /**< stringのサイズ（メッセージにより byte単位／文字単位が変わる）*/
    NJ_UINT32                   additional_size;/**< additionalのサイズ（メッセージにより byte単位／文字単位が変わる）*/
    NJ_UINT16                   dic_idx;        /**< 辞書マウント位置                                             */
} NJ_PROGRAM_DIC_MESSAGE;

/**
 * 擬似辞書インタフェース定義
 *
 * [検索処理時]
 * @param[in,out] iwnn                  解析情報クラス
 * @param[in]     mode                  処理モード(=NJG_OP_SEARCH, NJG_OP_SEARCH_NEXT)
 * @param[in,out] message->condition    検索条件
 * @param[in,out] message->location     検索カーソル
 * @param[in]     message->dicset       検索辞書セット
 * @param[in]     message->word         未使用(=NULL)
 * @param[in]     message->lword        未使用(=NULL)
 * @param[in]     message->stroke       未使用(=NULL)
 * @param[in]     message->string       未使用(=NULL)
 * @param[in]     message->stroke_size  未使用(=0)
 * @param[in]     message->string_size  未使用(=0)
 * @param[in]     message->dic_idx      辞書マウント位置
 *
 * @retval 1 候補有
 * @retval 0 候補無
 * @retval 負数 エラー
 *
 * [候補取得処理時]
 * @param[in,out] iwnn                  解析情報クラス
 * @param[in]     mode                  処理モード(=NJG_OP_GET_WORD_INFO)
 * @param[in]     message->condition    未使用(=NULL)
 * @param[in]     message->location     検索カーソル
 * @param[in]     message->dicset       検索辞書セット
 * @param[out]    message->word         単語情報(検索結果を格納)
 * @param[in]     message->lword        未使用(=NULL)
 * @param[in]     message->stroke       未使用(=NULL)
 * @param[in]     message->string       未使用(=NULL)
 * @param[in]     message->stroke_size  未使用(=0)
 * @param[in]     message->string_size  未使用(=0)
 * @param[in]     message->dic_idx      辞書マウント位置
 *
 * @retval 0 正常
 * @retval 負数 エラー
 *
 * [読み/表記取得処理時]
 * @param[in,out] iwnn                  解析情報クラス
 * @param[in]     mode                  処理モード(=NJG_OP_GET_STROKE or NJG_OP_GET_STRING)
 * @param[in]     message->condition    未使用(=NULL)
 * @param[in]     message->location     未使用(=NULL)
 * @param[in]     message->dicset       未使用(=NULL)
 * @param[in]     message->word         単語情報(読み表記取得対象の単語)
 * @param[in]     message->lword        未使用(=NULL)
 * @param[out]    message->stroke       読み文字列取得先
 * @param[out]    message->string       表記文字列取得先
 * @param[in]     message->stroke_size  strokeのbyteサイズ
 * @param[in]     message->string_size  stringのbyteサイズ
 * @param[in]     message->dic_idx      未使用(=0)
 *
 * @return 格納文字長（エラーの場合は負数）
 *
 * [学習処理時]
 * @param[in,out] iwnn                  解析情報クラス
 * @param[in]     mode                  処理モード(=NJG_OP_LEARN)
 * @param[in]     message->condition    未使用(=NULL)
 * @param[in]     message->location     未使用(=NULL)
 * @param[in]     message->dicset       検索辞書セット
 * @param[in]     message->word         単語情報(読み表記取得対象の単語)
 * @param[in]     message->lword        単語登録情報
 * @param[in]     message->stroke       読み文字列取得先
 * @param[in]     message->string       表記文字列取得先
 * @param[in]     message->stroke_size  strokeの文字配列長
 * @param[in]     message->string_size  stringの文字配列長
 * @param[in]     message->dic_idx      辞書マウント位置
 *
 * @retval 0 正常
 * @retval 負数 エラー
 *
 * [学習UNDO処理時]
 * @param[in,out] iwnn                  解析情報クラス
 * @param[in]     mode                  処理モード(=NJG_OP_UNDO_LEARN)
 * @param[in]     message->condition    未使用(=NULL)
 * @param[in]     message->location     未使用(=NULL)
 * @param[in]     message->dicset       検索辞書セット
 * @param[in]     message->word         未使用(=NULL)
 * @param[in]     message->lword        単語登録情報
 * @param[in]     message->stroke       未使用(=NULL)
 * @param[in]     message->string       未使用(=NULL)
 * @param[in]     message->stroke_size  未使用(=0)
 * @param[in]     message->string_size  未使用(=0)
 * @param[in]     message->dic_idx      辞書マウント位置
 *
 * @retval 0 正常
 * @retval 負数 エラー
 *
 * [単語登録処理時]
 * @param[in,out] iwnn                  解析情報クラス
 * @param[in]     mode                  処理モード(=NJG_OP_ADD_WORD)
 * @param[in]     message->condition    未使用(=NULL)
 * @param[in]     message->location     未使用(=NULL)
 * @param[in]     message->dicset       検索辞書セット
 * @param[in]     message->word         未使用(=NULL)
 * @param[in]     message->lword        単語登録情報
 * @param[in]     message->stroke       読み文字列取得先
 * @param[in]     message->string       表記文字列取得先
 * @param[in]     message->stroke_size  strokeの文字配列長
 * @param[in]     message->string_size  stringの文字配列長
 * @param[in]     message->dic_idx      辞書マウント位置
 *
 * @retval 0 正常
 * @retval 負数 エラー
 *
 * [単語削除処理時]
 * @param[in,out] iwnn                  解析情報クラス
 * @param[in]     mode                  処理モード(=NJG_OP_DEL_WORD)
 * @param[in]     message->condition    未使用(=NULL)
 * @param[in]     message->location     未使用(=NULL)
 * @param[in]     message->dicset       未使用(=NULL)
 * @param[in]     message->word         単語情報(削除対象の単語)
 * @param[in]     message->lword        未使用(=NULL)
 * @param[in]     message->stroke       未使用(=NULL)
 * @param[in]     message->string       未使用(=NULL)
 * @param[in]     message->stroke_size  未使用(=NULL)
 * @param[in]     message->string_size  未使用(=NULL)
 * @param[in]     message->dic_idx      未使用(=NULL)
 *
 * @retval 0 正常
 * @retval 負数 エラー
 */
typedef NJ_INT16(*NJ_PROGRAM_DIC_IF)(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE *message);




#endif /* _NJ_DICIF_H_ */
