/**
 * @file
 *   状況計算パラメータ定義（共通編）
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2009-2012 All Rights Reserved.
 */
#ifndef _NJX_STATE_H_
#define _NJX_STATE_H_

/************************************************/
/*           データ定義                         */
/************************************************/

/* 日本語定義 */
/* 状況設定カテゴリ */
#define NJ_CAT_FIELD_PERSON             0 /**< NJ_STATE::system 入力フィールド：人名 */
#define NJ_CAT_FIELD_HEAD               2 /**< NJ_STATE::system 入力フィールド：文頭 */
#define NJ_CAT_EXPRESSION_COLLOQUIAL    3 /**< NJ_STATE::system 表現の種類：柔らかい表現(話し言葉) */
#define NJ_CAT_EXPRESSION_WRITTEN       4 /**< NJ_STATE::system 表現の種類：固い表現(書き言葉) */
#define NJ_CAT_TIME_MORNING             5 /**< NJ_STATE::system 時間表現：朝 */
#define NJ_CAT_TIME_NOON                6 /**< NJ_STATE::system 時間表現：昼 */
#define NJ_CAT_TIME_NIGHT               7 /**< NJ_STATE::system 時間表現：晩 */
#define NJ_CAT_TIME_PAST                8 /**< NJ_STATE::system 時間表現：過去 */
#define NJ_CAT_TIME_FUTURE              9 /**< NJ_STATE::system 時間表現：未来 */
#define NJ_CAT_FEEL_PLUS                10/**< NJ_STATE::system 感情表現：プラスイメージ   */
#define NJ_CAT_FEEL_MINUS               11/**< NJ_STATE::system 感情表現：マイナスイメージ */
#define NJ_CAT_MONTH_JAN                12/**< NJ_STATE::system 月表現：１月 */
#define NJ_CAT_MONTH_FEB                13/**< NJ_STATE::system 月表現：２月 */
#define NJ_CAT_MONTH_MAR                14/**< NJ_STATE::system 月表現：３月 */
#define NJ_CAT_MONTH_APR                15/**< NJ_STATE::system 月表現：４月 */
#define NJ_CAT_MONTH_MAY                16/**< NJ_STATE::system 月表現：５月 */
#define NJ_CAT_MONTH_JUN                17/**< NJ_STATE::system 月表現：６月 */
#define NJ_CAT_MONTH_JUL                18/**< NJ_STATE::system 月表現：７月 */
#define NJ_CAT_MONTH_AUG                19/**< NJ_STATE::system 月表現：８月 */
#define NJ_CAT_MONTH_SEP                20/**< NJ_STATE::system 月表現：９月 */
#define NJ_CAT_MONTH_OCT                21/**< NJ_STATE::system 月表現：１０月 */
#define NJ_CAT_MONTH_NOV                22/**< NJ_STATE::system 月表現：１１月 */
#define NJ_CAT_MONTH_DEC                23/**< NJ_STATE::system 月表現：１２月 */
#define NJ_CAT_AREA_KANSAI              24/**< NJ_STATE::system 方言表現：関西弁 */
#define NJ_CAT_DAILY_PLACE              25/**< NJ_STATE::system 日常生活表現：場所 */
#define NJ_CAT_DAILY_RIDE               26/**< NJ_STATE::system 日常生活表現：乗り物 */
#define NJ_CAT_DAILY_FOOD               27/**< NJ_STATE::system 日常生活表現：食べ物 */
#define NJ_CAT_DAILY_DRINK              28/**< NJ_STATE::system 日常生活表現：飲み物 */
#define NJ_CAT_DAILY_WMEDIA             29/**< NJ_STATE::system 日常生活表現：メディア１ */
#define NJ_CAT_DAILY_RMEDIA             30/**< NJ_STATE::system 日常生活表現：メディア２ */
#define NJ_CAT_RESERVED_FUNC            1 /**< NJ_STATE::system 予約：機能 */
#define NJ_CAT_RESERVED_DIC             31/**< NJ_STATE::system 予約：辞書 */


/* 状況設定の加算値 */
#ifndef NJ_ADD_STATE_TYPE2
/** デフォルト状況操作設定 */
static const NJ_STATE_CALC_PARAMETER default_state_calc_parameter = {
/* 状況設定の上限値 */
    {NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS},
/* 状況設定の下限値 */
    {NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS},
/* 状況設定が <0 の場合、確定単語に関係なく加算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 状況設定が >1 の場合、確定単語に関係なく減算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 確定単語に一致する状況設定が 本値よりも小さい場合、状況設定を本値まで復帰させる値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 属性に応じた状況設定の可算値 */
    {{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
    },
/* 辞書設定辞書頻度最大値 */
    {NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_TERMINATE},
/* 辞書設定辞書頻度最小値 */
    {NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_TERMINATE}
};
#else /* !NJ_ADD_STATE_TYPE2 */
/** 
  * デフォルト状況操作設定
  *  @attension 20以下を必ず指定すること。
  */
static const NJ_STATE_CALC_PARAMETER default_state_calc_parameter = {
/* 状況設定の上限値 */
    {NJ_STATE_MAX_BIAS, 0, NJ_STATE_MAX_BIAS, /*人名, 予約：機能, 文頭*/
     30, 30, /*柔らか、硬い*/
     200, 200, 200, /*朝、昼、夜*/
     30, 30, /*過去、未来*/
     30, 30,  /*プラス、マイナス*/
     200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, /* 1月, 2月, 3月, 4月, 5月, 6月, 7月, 8月, 9月, 10月, 11月, 12月 */
     30, /* 関西弁 */
     0, 0, 0, 0, 0, 0, 0 /* 場所, 乗り物, 食べ物, 飲み物, メディア１, メディア２, 予約：辞書 */
    },
/* 状況設定の下限値 */
    {NJ_STATE_MIN_BIAS, 0, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, 0, 0, 0,
     0, 0, 0, 0},
/* 状況設定が <0 の場合、確定単語に関係なく加算する値 */
    {0, 0, 0, 10, 10, 0, 0, 0, 10, 10, 10, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 状況設定が >1 の場合、確定単語に関係なく減算する値 */
    {0, 0, 0, -2, -2, -1, -1, -1, -2, -2, -2, -2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0},
/* 確定単語に一致する状況設定が 本値よりも小さい場合、状況設定を本値まで復帰させる値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 属性に応じた状況設定の可算値 */
    {{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*人名*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*予約：機能*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*文頭*/
     {0, 0, 0, 10,-100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*柔らか*/
     {0, 0, 0,-100, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*硬い*/
     {0, 0, 0, 0, 0, 20,-10,-10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*朝*/
     {0, 0, 0, 0, 0,-10, 20,-10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*昼*/
     {0, 0, 0, 0, 0,-10,-10, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*晩*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, -800, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*過去*/
     {0, 0, 0, 0, 0, 0, 0, 0, -800, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*未来*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, -100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*プラス*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -100, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*マイナス*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*1月*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*2月*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*3月*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*4月*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*5月*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*6月*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*7月*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*8月*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*9月*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*10月*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*11月*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0}, /*12月*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0}, /*関西弁*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*場所*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*乗り物*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*食べ物*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*飲み物*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*メディア１*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*メディア２*/
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /*予約：辞書*/
    },
/* 辞書設定辞書頻度最大値 */
    {NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_TERMINATE},
/* 辞書設定辞書頻度最小値 */
    {NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_TERMINATE}
};
#endif /* !NJ_ADD_STATE_TYPE2 */



#ifdef NJ_OPT_EN_STATE
/* 英語版 状況適応予測定義 */
/* 状況設定カテゴリ */
#define NJ_CAT_EN_FEEL_PLUS              0 /**< NJ_STATE::system 感情表現：プラスイメージ   */
#define NJ_CAT_EN_FEEL_MINUS             1 /**< NJ_STATE::system 感情表現：マイナスイメージ */
#define NJ_CAT_EN_RESERVED               2 /**< NJ_STATE::system 予約カテゴリ */
#define NJ_CAT_EN_TIME_MORNING           3 /**< NJ_STATE::system 時間表現：朝 */
#define NJ_CAT_EN_TIME_NOON              4 /**< NJ_STATE::system 時間表現：昼 */
#define NJ_CAT_EN_TIME_NIGHT             5 /**< NJ_STATE::system 時間表現：晩 */
#define NJ_CAT_EN_SO_MONTH_JAN           6 /**< NJ_STATE::system 月表現-南半球-：１月 */
#define NJ_CAT_EN_SO_MONTH_FEB           7 /**< NJ_STATE::system 月表現-南半球-：２月 */
#define NJ_CAT_EN_SO_MONTH_MAR           8 /**< NJ_STATE::system 月表現-南半球-：３月 */
#define NJ_CAT_EN_SO_MONTH_APR           9 /**< NJ_STATE::system 月表現-南半球-：４月 */
#define NJ_CAT_EN_SO_MONTH_MAY          10 /**< NJ_STATE::system 月表現-南半球-：５月 */
#define NJ_CAT_EN_SO_MONTH_JUN          11 /**< NJ_STATE::system 月表現-南半球-：６月 */
#define NJ_CAT_EN_SO_MONTH_JUL          12 /**< NJ_STATE::system 月表現-南半球-：７月 */
#define NJ_CAT_EN_SO_MONTH_AUG          13 /**< NJ_STATE::system 月表現-南半球-：８月 */
#define NJ_CAT_EN_SO_MONTH_SEP          14 /**< NJ_STATE::system 月表現-南半球-：９月 */
#define NJ_CAT_EN_SO_MONTH_OCT          15 /**< NJ_STATE::system 月表現-南半球-：１０月 */
#define NJ_CAT_EN_SO_MONTH_NOV          16 /**< NJ_STATE::system 月表現-南半球-：１１月 */
#define NJ_CAT_EN_SO_MONTH_DEC          17 /**< NJ_STATE::system 月表現-南半球-：１２月 */
#define NJ_CAT_EN_NO_MONTH_JAN          18 /**< NJ_STATE::system 月表現-北半球-：１月 */
#define NJ_CAT_EN_NO_MONTH_FEB          19 /**< NJ_STATE::system 月表現-北半球-：２月 */
#define NJ_CAT_EN_NO_MONTH_MAR          20 /**< NJ_STATE::system 月表現-北半球-：３月 */
#define NJ_CAT_EN_NO_MONTH_APR          21 /**< NJ_STATE::system 月表現-北半球-：４月 */
#define NJ_CAT_EN_NO_MONTH_MAY          22 /**< NJ_STATE::system 月表現-北半球-：５月 */
#define NJ_CAT_EN_NO_MONTH_JUN          23 /**< NJ_STATE::system 月表現-北半球-：６月 */
#define NJ_CAT_EN_NO_MONTH_JUL          24 /**< NJ_STATE::system 月表現-北半球-：７月 */
#define NJ_CAT_EN_NO_MONTH_AUG          25 /**< NJ_STATE::system 月表現-北半球-：８月 */
#define NJ_CAT_EN_NO_MONTH_SEP          26 /**< NJ_STATE::system 月表現-北半球-：９月 */
#define NJ_CAT_EN_NO_MONTH_OCT          27 /**< NJ_STATE::system 月表現-北半球-：１０月 */
#define NJ_CAT_EN_NO_MONTH_NOV          28 /**< NJ_STATE::system 月表現-北半球-：１１月 */
#define NJ_CAT_EN_NO_MONTH_DEC          29 /**< NJ_STATE::system 月表現-北半球-：１２月 */

/* 状況設定の加算値 */
#ifndef NJ_ADD_STATE_TYPE2
/** デフォルト状況操作設定 */
static const NJ_STATE_CALC_PARAMETER default_state_calc_parameter_en_south = {
/* 状況設定の上限値 */
    {NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS},
/* 状況設定の下限値 */
    {NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS},
/* 状況設定が <0 の場合、確定単語に関係なく加算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 状況設定が >1 の場合、確定単語に関係なく減算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 確定単語に一致する状況設定が 本値よりも小さい場合、状況設定を本値まで復帰させる値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 属性に応じた状況設定の可算値 */
    {{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：プラスイメージ   */
     {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：マイナスイメージ */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予約カテゴリ */
     {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：朝 */
     {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：昼 */
     {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：晩 */
     {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
    },
/* 辞書設定辞書頻度最大値 */
    {NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_TERMINATE},
/* 辞書設定辞書頻度最小値 */
    {NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_TERMINATE}
};

static const NJ_STATE_CALC_PARAMETER default_state_calc_parameter_en_north = {
/* 状況設定の上限値 */
    {NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS},
/* 状況設定の下限値 */
    {NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS},
/* 状況設定が <0 の場合、確定単語に関係なく加算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 状況設定が >1 の場合、確定単語に関係なく減算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 確定単語に一致する状況設定が 本値よりも小さい場合、状況設定を本値まで復帰させる値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 属性に応じた状況設定の可算値 */
    {{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：プラスイメージ   */
     {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：マイナスイメージ */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予約カテゴリ */
     {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：朝 */
     {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：昼 */
     {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：晩 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, /* 月表現-北半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}, /* 月表現-北半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0}, /* 月表現-北半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0}, /* 月表現-北半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
    },
/* 辞書設定辞書頻度最大値 */
    {NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_TERMINATE},
/* 辞書設定辞書頻度最小値 */
    {NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_TERMINATE}
};
#else /* !NJ_ADD_STATE_TYPE2 */
static const NJ_STATE_CALC_PARAMETER default_state_calc_parameter_en_south = {
/* 状況設定の上限値 */
    {30, 30,                /* 感情表現：プラス、マイナス */
     NJ_STATE_MAX_BIAS,
     100, 100, 100,         /* 時間表現：朝、昼、晩       */
     100, 100, 100, 100,    /* 月表現-南半球- */
     100, 100, 100, 100,
     100, 100, 100, 100,
     100, 100, 100, 100,    /* 月表現-北半球- */
     100, 100, 100, 100,
     100, 100, 100, 100,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS },
/* 状況設定の下限値 */
    {NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS},
/* 状況設定が <0 の場合、確定単語に関係なく加算する値 */
    {10, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 状況設定が >1 の場合、確定単語に関係なく減算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 確定単語に一致する状況設定が 本値よりも小さい場合、状況設定を本値まで復帰させる値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 属性に応じた状況設定の可算値 */
    {{10,-100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：プラスイメージ   */
     {-100,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：マイナスイメージ */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予約カテゴリ */
     {0, 0, 0, 20,-10,-10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：朝 */
     {0, 0, 0,-10,20,-10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：昼 */
     {0, 0, 0,-10,-10,20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：晩 */
     {0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
    },
/* 辞書設定辞書頻度最大値 */
    {NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_TERMINATE},
/* 辞書設定辞書頻度最小値 */
    {NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_TERMINATE}
};

static const NJ_STATE_CALC_PARAMETER default_state_calc_parameter_en_north = {
/* 状況設定の上限値 */
    {30, 30,                /* 感情表現：プラス、マイナス */
     NJ_STATE_MAX_BIAS,
     100, 100, 100,         /* 時間表現：朝、昼、晩       */
     100, 100, 100, 100,    /* 月表現-南半球- */
     100, 100, 100, 100,
     100, 100, 100, 100,
     100, 100, 100, 100,    /* 月表現-北半球- */
     100, 100, 100, 100,
     100, 100, 100, 100,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS },
/* 状況設定の下限値 */
    {NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS},
/* 状況設定が <0 の場合、確定単語に関係なく加算する値 */
    {10, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 状況設定が >1 の場合、確定単語に関係なく減算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 確定単語に一致する状況設定が 本値よりも小さい場合、状況設定を本値まで復帰させる値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 属性に応じた状況設定の可算値 */
    {{10,-100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：プラスイメージ   */
     {-100,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：マイナスイメージ */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予約カテゴリ */
     {0, 0, 0, 20,-10,-10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：朝 */
     {0, 0, 0,-10,20,-10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：昼 */
     {0, 0, 0,-10,-10,20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：晩 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0}, /* 月表現-北半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0}, /* 月表現-北半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0}, /* 月表現-北半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0}, /* 月表現-北半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
    },
/* 辞書設定辞書頻度最大値 */
    {NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_TERMINATE},
/* 辞書設定辞書頻度最小値 */
    {NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_TERMINATE}
};
#endif /* !NJ_ADD_STATE_TYPE2 */

#endif /* NJ_OPT_EN_STATE */



#ifdef NJ_OPT_ZHCN_STATE
/* 中国語<簡体字>版 状況適応予測定義 */
/* 状況設定カテゴリ */
#define NJ_CAT_ZHCN_FEEL_PLUS              0 /**< NJ_STATE::system 感情表現：プラスイメージ   */
#define NJ_CAT_ZHCN_FEEL_MINUS             1 /**< NJ_STATE::system 感情表現：マイナスイメージ */
#define NJ_CAT_ZHCN_RESERVED               2 /**< NJ_STATE::system 予約カテゴリ */
#define NJ_CAT_ZHCN_TIME_MORNING           3 /**< NJ_STATE::system 時間表現：朝 */
#define NJ_CAT_ZHCN_TIME_NOON              4 /**< NJ_STATE::system 時間表現：昼 */
#define NJ_CAT_ZHCN_TIME_NIGHT             5 /**< NJ_STATE::system 時間表現：晩 */
#define NJ_CAT_ZHCN_MONTH_JAN              6 /**< NJ_STATE::system 月表現：１月 */
#define NJ_CAT_ZHCN_MONTH_FEB              7 /**< NJ_STATE::system 月表現：２月 */
#define NJ_CAT_ZHCN_MONTH_MAR              8 /**< NJ_STATE::system 月表現：３月 */
#define NJ_CAT_ZHCN_MONTH_APR              9 /**< NJ_STATE::system 月表現：４月 */
#define NJ_CAT_ZHCN_MONTH_MAY             10 /**< NJ_STATE::system 月表現：５月 */
#define NJ_CAT_ZHCN_MONTH_JUN             11 /**< NJ_STATE::system 月表現：６月 */
#define NJ_CAT_ZHCN_MONTH_JUL             12 /**< NJ_STATE::system 月表現：７月 */
#define NJ_CAT_ZHCN_MONTH_AUG             13 /**< NJ_STATE::system 月表現：８月 */
#define NJ_CAT_ZHCN_MONTH_SEP             14 /**< NJ_STATE::system 月表現：９月 */
#define NJ_CAT_ZHCN_MONTH_OCT             15 /**< NJ_STATE::system 月表現：１０月 */
#define NJ_CAT_ZHCN_MONTH_NOV             16 /**< NJ_STATE::system 月表現：１１月 */
#define NJ_CAT_ZHCN_MONTH_DEC             17 /**< NJ_STATE::system 月表現：１２月 */

/* 状況設定の加算値 */
#ifndef NJ_ADD_STATE_TYPE2
/** デフォルト状況操作設定 */
static const NJ_STATE_CALC_PARAMETER default_state_calc_parameter_zhcn = {
/* 状況設定の上限値 */
    {NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS},
/* 状況設定の下限値 */
    {NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS},
/* 状況設定が <0 の場合、確定単語に関係なく加算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 状況設定が >1 の場合、確定単語に関係なく減算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 確定単語に一致する状況設定が 本値よりも小さい場合、状況設定を本値まで復帰させる値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 属性に応じた状況設定の可算値 */
    {{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：プラスイメージ   */
     {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：マイナスイメージ */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予約カテゴリ */
     {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：朝 */
     {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：昼 */
     {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：晩 */
     {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：１月 */
     {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
    },
/* 辞書設定辞書頻度最大値 */
    {NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_TERMINATE},
/* 辞書設定辞書頻度最小値 */
    {NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_TERMINATE}
};
#else /* !NJ_ADD_STATE_TYPE2 */
static const NJ_STATE_CALC_PARAMETER default_state_calc_parameter_zhcn = {
/* 状況設定の上限値 */
    {30, 30,                /* 感情表現：プラス、マイナス */
     NJ_STATE_MAX_BIAS,
     100, 100, 100,         /* 時間表現：朝、昼、晩       */
     100, 100, 100, 100,    /* 月表現 */
     100, 100, 100, 100,
     100, 100, 100, 100,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS },
/* 状況設定の下限値 */
    {NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS},
/* 状況設定が <0 の場合、確定単語に関係なく加算する値 */
    {10, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 状況設定が >1 の場合、確定単語に関係なく減算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 確定単語に一致する状況設定が 本値よりも小さい場合、状況設定を本値まで復帰させる値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 属性に応じた状況設定の可算値 */
    {{10,-100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：プラスイメージ   */
     {-100,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：マイナスイメージ */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予約カテゴリ */
     {0, 0, 0, 20,-10,-10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：朝 */
     {0, 0, 0,-10,20,-10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：昼 */
     {0, 0, 0,-10,-10,20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：晩 */
     {0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：１月 */
     {0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
    },
/* 辞書設定辞書頻度最大値 */
    {NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_TERMINATE},
/* 辞書設定辞書頻度最小値 */
    {NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_TERMINATE}
};
#endif /* !NJ_ADD_STATE_TYPE2 */

#endif /* NJ_OPT_ZHCN_STATE */


#ifdef NJ_OPT_ES_STATE
/* スペイン語版 状況適応予測定義 */
/* 状況設定カテゴリ */
#define NJ_CAT_ES_FEEL_PLUS              0 /**< NJ_STATE::system 感情表現：プラスイメージ   */
#define NJ_CAT_ES_FEEL_MINUS             1 /**< NJ_STATE::system 感情表現：マイナスイメージ */
#define NJ_CAT_ES_RESERVED               2 /**< NJ_STATE::system 予約カテゴリ */
#define NJ_CAT_ES_TIME_MORNING           3 /**< NJ_STATE::system 時間表現：朝 */
#define NJ_CAT_ES_TIME_NOON              4 /**< NJ_STATE::system 時間表現：昼 */
#define NJ_CAT_ES_TIME_NIGHT             5 /**< NJ_STATE::system 時間表現：晩 */
#define NJ_CAT_ES_SO_MONTH_JAN           6 /**< NJ_STATE::system 月表現-南半球-：１月 */
#define NJ_CAT_ES_SO_MONTH_FEB           7 /**< NJ_STATE::system 月表現-南半球-：２月 */
#define NJ_CAT_ES_SO_MONTH_MAR           8 /**< NJ_STATE::system 月表現-南半球-：３月 */
#define NJ_CAT_ES_SO_MONTH_APR           9 /**< NJ_STATE::system 月表現-南半球-：４月 */
#define NJ_CAT_ES_SO_MONTH_MAY          10 /**< NJ_STATE::system 月表現-南半球-：５月 */
#define NJ_CAT_ES_SO_MONTH_JUN          11 /**< NJ_STATE::system 月表現-南半球-：６月 */
#define NJ_CAT_ES_SO_MONTH_JUL          12 /**< NJ_STATE::system 月表現-南半球-：７月 */
#define NJ_CAT_ES_SO_MONTH_AUG          13 /**< NJ_STATE::system 月表現-南半球-：８月 */
#define NJ_CAT_ES_SO_MONTH_SEP          14 /**< NJ_STATE::system 月表現-南半球-：９月 */
#define NJ_CAT_ES_SO_MONTH_OCT          15 /**< NJ_STATE::system 月表現-南半球-：１０月 */
#define NJ_CAT_ES_SO_MONTH_NOV          16 /**< NJ_STATE::system 月表現-南半球-：１１月 */
#define NJ_CAT_ES_SO_MONTH_DEC          17 /**< NJ_STATE::system 月表現-南半球-：１２月 */
#define NJ_CAT_ES_NO_MONTH_JAN          18 /**< NJ_STATE::system 月表現-北半球-：１月 */
#define NJ_CAT_ES_NO_MONTH_FEB          19 /**< NJ_STATE::system 月表現-北半球-：２月 */
#define NJ_CAT_ES_NO_MONTH_MAR          20 /**< NJ_STATE::system 月表現-北半球-：３月 */
#define NJ_CAT_ES_NO_MONTH_APR          21 /**< NJ_STATE::system 月表現-北半球-：４月 */
#define NJ_CAT_ES_NO_MONTH_MAY          22 /**< NJ_STATE::system 月表現-北半球-：５月 */
#define NJ_CAT_ES_NO_MONTH_JUN          23 /**< NJ_STATE::system 月表現-北半球-：６月 */
#define NJ_CAT_ES_NO_MONTH_JUL          24 /**< NJ_STATE::system 月表現-北半球-：７月 */
#define NJ_CAT_ES_NO_MONTH_AUG          25 /**< NJ_STATE::system 月表現-北半球-：８月 */
#define NJ_CAT_ES_NO_MONTH_SEP          26 /**< NJ_STATE::system 月表現-北半球-：９月 */
#define NJ_CAT_ES_NO_MONTH_OCT          27 /**< NJ_STATE::system 月表現-北半球-：１０月 */
#define NJ_CAT_ES_NO_MONTH_NOV          28 /**< NJ_STATE::system 月表現-北半球-：１１月 */
#define NJ_CAT_ES_NO_MONTH_DEC          29 /**< NJ_STATE::system 月表現-北半球-：１２月 */

/* 状況設定の加算値 */
#ifndef NJ_ADD_STATE_TYPE2
/** デフォルト状況操作設定 */
static const NJ_STATE_CALC_PARAMETER default_state_calc_parameter_es_south = {
/* 状況設定の上限値 */
    {NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS},
/* 状況設定の下限値 */
    {NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS},
/* 状況設定が <0 の場合、確定単語に関係なく加算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 状況設定が >1 の場合、確定単語に関係なく減算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 確定単語に一致する状況設定が 本値よりも小さい場合、状況設定を本値まで復帰させる値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 属性に応じた状況設定の可算値 */
    {{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：プラスイメージ   */
     {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：マイナスイメージ */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予約カテゴリ */
     {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：朝 */
     {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：昼 */
     {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：晩 */
     {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
    },
/* 辞書設定辞書頻度最大値 */
    {NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_TERMINATE},
/* 辞書設定辞書頻度最小値 */
    {NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_TERMINATE}
};

static const NJ_STATE_CALC_PARAMETER default_state_calc_parameter_es_north = {
/* 状況設定の上限値 */
    {NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS},
/* 状況設定の下限値 */
    {NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS},
/* 状況設定が <0 の場合、確定単語に関係なく加算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 状況設定が >1 の場合、確定単語に関係なく減算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 確定単語に一致する状況設定が 本値よりも小さい場合、状況設定を本値まで復帰させる値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 属性に応じた状況設定の可算値 */
    {{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：プラスイメージ   */
     {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：マイナスイメージ */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予約カテゴリ */
     {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：朝 */
     {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：昼 */
     {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：晩 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, /* 月表現-北半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}, /* 月表現-北半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0}, /* 月表現-北半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0}, /* 月表現-北半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
    },
/* 辞書設定辞書頻度最大値 */
    {NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_TERMINATE},
/* 辞書設定辞書頻度最小値 */
    {NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_TERMINATE}
};
#else /* !NJ_ADD_STATE_TYPE2 */
static const NJ_STATE_CALC_PARAMETER default_state_calc_parameter_es_south = {
/* 状況設定の上限値 */
    {30, 30,                /* 感情表現：プラス、マイナス */
     NJ_STATE_MAX_BIAS,
     100, 100, 100,         /* 時間表現：朝、昼、晩       */
     100, 100, 100, 100,    /* 月表現-南半球- */
     100, 100, 100, 100,
     100, 100, 100, 100,
     100, 100, 100, 100,    /* 月表現-北半球- */
     100, 100, 100, 100,
     100, 100, 100, 100,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS },
/* 状況設定の下限値 */
    {NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS},
/* 状況設定が <0 の場合、確定単語に関係なく加算する値 */
    {10, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 状況設定が >1 の場合、確定単語に関係なく減算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 確定単語に一致する状況設定が 本値よりも小さい場合、状況設定を本値まで復帰させる値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 属性に応じた状況設定の可算値 */
    {{10,-100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：プラスイメージ   */
     {-100,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：マイナスイメージ */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予約カテゴリ */
     {0, 0, 0, 20,-10,-10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：朝 */
     {0, 0, 0,-10,20,-10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：昼 */
     {0, 0, 0,-10,-10,20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：晩 */
     {0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
    },
/* 辞書設定辞書頻度最大値 */
    {NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_TERMINATE},
/* 辞書設定辞書頻度最小値 */
    {NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_TERMINATE}
};

static const NJ_STATE_CALC_PARAMETER default_state_calc_parameter_es_north = {
/* 状況設定の上限値 */
    {30, 30,                /* 感情表現：プラス、マイナス */
     NJ_STATE_MAX_BIAS,
     100, 100, 100,         /* 時間表現：朝、昼、晩       */
     100, 100, 100, 100,    /* 月表現-南半球- */
     100, 100, 100, 100,
     100, 100, 100, 100,
     100, 100, 100, 100,    /* 月表現-北半球- */
     100, 100, 100, 100,
     100, 100, 100, 100,
     NJ_STATE_MAX_BIAS, NJ_STATE_MAX_BIAS },
/* 状況設定の下限値 */
    {NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS,
     NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS, NJ_STATE_MIN_BIAS},
/* 状況設定が <0 の場合、確定単語に関係なく加算する値 */
    {10, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 状況設定が >1 の場合、確定単語に関係なく減算する値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 確定単語に一致する状況設定が 本値よりも小さい場合、状況設定を本値まで復帰させる値 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/* 属性に応じた状況設定の可算値 */
    {{10,-100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：プラスイメージ   */
     {-100,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 感情表現：マイナスイメージ */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予約カテゴリ */
     {0, 0, 0, 20,-10,-10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：朝 */
     {0, 0, 0,-10,20,-10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：昼 */
     {0, 0, 0,-10,-10,20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 時間表現：晩 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-南半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：３月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：４月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：５月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：６月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：７月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0, 0}, /* 月表現-北半球-：８月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0, 0}, /* 月表現-北半球-：９月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0, 0}, /* 月表現-北半球-：１０月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0, 0}, /* 月表現-北半球-：１１月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,10, 0, 0}, /* 月表現-北半球-：１２月 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* 予備 */
    },
/* 辞書設定辞書頻度最大値 */
    {NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ, NJ_STATE_MAX_FREQ,
     NJ_STATE_TERMINATE},
/* 辞書設定辞書頻度最小値 */
    {NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ, NJ_STATE_MIN_FREQ,
     NJ_STATE_TERMINATE}
};
#endif /* !NJ_ADD_STATE_TYPE2 */

#endif /* NJ_OPT_ES_STATE */



#endif /* _NJX_STATE_H_ */
