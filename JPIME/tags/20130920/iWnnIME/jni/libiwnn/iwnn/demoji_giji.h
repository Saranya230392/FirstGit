/**
 * @file
 *  [拡張]デコメ絵文字辞書操作用擬似辞書定義
 *
 *   デコメ絵文字辞書を操作する為の外部関数宣言を行う
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2010 All Rights Reserved.
 */
#ifndef _DEMOJI_GIJI_H_
#define _DEMOJI_GIJI_H_

#include "nj_lib.h"
#include "nj_dicif.h"

#ifdef    __cplusplus
extern "C" {
#endif    /* __cplusplus */

/************************************************/
/*                 define 宣 言                 */
/************************************************/
/**
 * [デコレ絵文字辞書] 最大単語登録数
 *
 * - 最大値：２００００
 * - 最小値：１０
 * - 初期値：２００００
 */
#ifndef NJ_MAX_EMOJI_COUNT
#define NJ_MAX_EMOJI_COUNT       20000
#endif /* NJ_MAX_EMOJI_COUNT */

/**
 * [デコレ絵文字辞書] 読み文字配列最大要素数
 *
 * - 最大値：NJ_MAX_LEN
 * - 最小値：２０
 * - 初期値：４０
 *
 * @attention
 *   ヌル文字を含まない長さ
 */
#ifndef NJ_MAX_EMOJI_LEN
#define NJ_MAX_EMOJI_LEN         24
#endif /* NJ_MAX_EMOJI_LEN */

/**
 * [デコレ絵文字辞書] 候補文字配列最大要素数
 *
 * - 最大値：NJ_MAX_RESULT_LEN
 * - 最小値：２０
 * - 初期値：４０
 *
 * @attention
 *   ヌル文字を含まない長さ
 */
#ifndef NJ_MAX_EMOJI_KOUHO_LEN
#define NJ_MAX_EMOJI_KOUHO_LEN   20
#endif /* NJ_MAX_EMOJI_KOUHO_LEN */

/**
 * [デコレ絵文字辞書] 絵文字辞書サイズ（変更不可）
 *
 * NJ_EMOJI_DIC_SIZE
 */
#define NJ_EMOJI_QUE_SIZE    (((NJ_MAX_EMOJI_LEN + NJ_MAX_EMOJI_KOUHO_LEN) * sizeof(NJ_CHAR)) + 5)
#define NJ_EMOJI_DIC_SIZE    ((NJ_EMOJI_QUE_SIZE + NJ_INDEX_SIZE + NJ_INDEX_SIZE) * NJ_MAX_EMOJI_COUNT + NJ_INDEX_SIZE  + NJ_INDEX_SIZE + NJ_LEARN_DIC_HEADER_SIZE + 4)

/************************************************/
/*                 extern 宣 言                 */
/************************************************/
NJ_EXTERN NJ_INT16 requestEmojiDictionary(NJ_CLASS* iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE* message);
NJ_EXTERN NJ_INT16 createEmojiDictionary(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT32 size);
NJ_EXTERN NJ_INT16 checkEmojiDictionary(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT32 size);

#ifdef __cplusplus
   }
#endif /* __cplusplus */

#endif /*_DEMOJI_GIJI_H_*/
