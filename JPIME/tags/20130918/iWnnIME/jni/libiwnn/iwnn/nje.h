/**
 * @file
 *   共通部 定義ファイル
 *
 *   共通部での各種定義
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008 All Rights Reserved.
 */

#ifndef _NJE_H_
#define _NJE_H_

/************************************************/
/*             定数定義                         */
/************************************************/
/*
 * nje_get_top_char_type の戻り値定義(NJ_UINT8)
 */
#define NJ_CHAR_TYPE_ZEN_SUUJI 0        /**< 文字種: 全角数字   */
#define NJ_CHAR_TYPE_ZEN_SUUJI_SIG 1    /**< 文字種: 全角数字記号       */

#define NJ_CHAR_TYPE_HAN_SUUJI 2        /**< 文字種: 半角数字   */
#define NJ_CHAR_TYPE_HAN_SUUJI_SIG 3    /**< 文字種: 半角数字記号       */

#define NJ_CHAR_TYPE_HIRAGANA  4        /**< 文字種: ひらがな   */
#define NJ_CHAR_TYPE_KATAKANA  5        /**< 文字種: 全角カナ   */
#define NJ_CHAR_TYPE_KANA_SIG  6        /**< 文字種: かな記号   */

#define NJ_CHAR_TYPE_HANKATA   7        /**< 文字種: 半角カナ   */
#define NJ_CHAR_TYPE_HKATA_SIG 8        /**< 文字種: 半角カナ記号 */

#define NJ_CHAR_TYPE_HAN_UNDEFINE  9    /**< 文字種: その他(半角)       */
#define NJ_CHAR_TYPE_ZEN_UNDEFINE  10   /**< 文字種: その他(全角)       */


#endif /* _NJE_H_ */
