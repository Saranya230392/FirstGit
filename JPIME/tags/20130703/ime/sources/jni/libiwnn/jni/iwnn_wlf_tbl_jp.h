/**
 * @file
 *   wlf_tbl_jp.h
 *
 * @brief  Wnn Language Framework Definitions for Japanese
 *
 * @author
 *   OMRON SOFTWARE Co., Ltd.
 *
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 * (C) Copyright OMRON SOFTWARE Co., Ltd. 2009 All Rights Reserved
 */
#define WLF_GIJI_PREDICT_PROGRAM_CNT_JP_10      4
#define WLF_GIJI_PREDICT_FIXED_CNT_JP_10       43
#define WLF_GIJI_EISUKANA_FIXED_CNT_JP_10      31
#define WLF_GIJI_PREDICT_FIXED_CNT_JP_FULL     15
#define WLF_GIJI_PREDICT_FIXED_CNT_LATIN_FULL   1
#define WLF_GIJI_GIJISET_NONE_CNT               0

#define WLF_GIJI_CONVERT_PROGRAM_JP_10      WLF_GIJI_PREDICT_PROGRAM_JP_10
#define WLF_GIJI_EISUKANA_PROGRAM_JP_10     WLF_GIJI_GIJISET_NONE
#define WLF_GIJI_CONVERT_FIXED_JP_10        WLF_GIJI_PREDICT_FIXED_JP_10
#define WLF_GIJI_PREDICT_PROGRAM_JP_FULL    WLF_GIJI_PREDICT_PROGRAM_JP_10
#define WLF_GIJI_CONVERT_PROGRAM_JP_FULL    WLF_GIJI_CONVERT_PROGRAM_JP_10
#define WLF_GIJI_EISUKANA_PROGRAM_JP_FULL   WLF_GIJI_GIJISET_NONE
#define WLF_GIJI_CONVERT_FIXED_JP_FULL      WLF_GIJI_PREDICT_FIXED_JP_FULL
#define WLF_GIJI_EISUKANA_FIXED_JP_FULL     WLF_GIJI_GIJISET_NONE
#define WLF_GIJI_PREDICT_PROGRAM_LATIN_10   WLF_GIJI_GIJISET_NONE
#define WLF_GIJI_CONVERT_PROGRAM_LATIN_10   WLF_GIJI_GIJISET_NONE
#define WLF_GIJI_EISUKANA_PROGRAM_LATIN_10  WLF_GIJI_GIJISET_NONE
#define WLF_GIJI_PREDICT_FIXED_LATIN_10     WLF_GIJI_PREDICT_FIXED_EN_FULL
#define WLF_GIJI_CONVERT_FIXED_LATIN_10     WLF_GIJI_GIJISET_NONE
#define WLF_GIJI_EISUKANA_FIXED_LATIN_10    WLF_GIJI_GIJISET_NONE
#define WLF_GIJI_PREDICT_PROGRAM_LATIN_FULL WLF_GIJI_GIJISET_NONE
#define WLF_GIJI_CONVERT_PROGRAM_LATIN_FULL WLF_GIJI_GIJISET_NONE
#define WLF_GIJI_EISUKANA_PROGRAM_LATIN_FULL WLF_GIJI_GIJISET_NONE
#define WLF_GIJI_PREDICT_FIXED_LATIN_FULL   WLF_GIJI_PREDICT_FIXED_EN_FULL
#define WLF_GIJI_CONVERT_FIXED_LATIN_FULL   WLF_GIJI_GIJISET_NONE
#define WLF_GIJI_EISUKANA_FIXED_LATIN_FULL  WLF_GIJI_GIJISET_NONE


/** Pseudo prediction dictionary for Japanese 10 keyboard */
static const NJ_GIJISET  WLF_GIJI_PREDICT_PROGRAM_JP_10 = {
    WLF_GIJI_PREDICT_PROGRAM_CNT_JP_10,
    {
        NJ_TYPE_HAN_SUUJI_YOMI,               /**< Pseudo candidate type: Reading string of Half-width number -> Number                          */
        NJ_TYPE_ZEN_SUUJI_YOMI,               /**< Pseudo candidate type: Reading string of Full-width number -> Number                          */
        NJ_TYPE_ZEN_KANSUUJI_YOMI,            /**< Pseudo candidate type: Reading string of Full-width number -> Kan-Suji                        */
        NJ_TYPE_ZEN_KANSUUJI_KURAI_YOMI,      /**< Pseudo candidate type: Reading string of Full-width number -> Kan-Suji(numeral)               */
    }
};

/** No pseudo dictionary setting */
static const NJ_GIJISET  WLF_GIJI_GIJISET_NONE = {
    WLF_GIJI_GIJISET_NONE_CNT,
    {
    }
};

/** No pseudo prediction setting */
static const NJ_GIJISET  WLF_GIJI_PREDICT_FIXED_EN_FULL = {
    WLF_GIJI_PREDICT_FIXED_CNT_LATIN_FULL,
    {
        NJ_TYPE_HIRAGANA            /**< Full-width Hiragana (Input string) */
    }
};

/** Definition of pseudo predicted candidates for Japanese 10 keyboard */
static const NJ_GIJISET  WLF_GIJI_PREDICT_FIXED_JP_10 = {
    WLF_GIJI_PREDICT_FIXED_CNT_JP_10,
    {
        NJ_TYPE_HAN_YOMI2DATE_INIT,           /**< Pseudo candidate type: Reading string of Half-width number -> Date/Time Initialize            */
        NJ_TYPE_HAN_YOMI2DATE_YYYY,           /**< Pseudo candidate type: Reading string of Half-width number -> Date(Format : YYYY年)           */
        NJ_TYPE_HAN_YOMI2DATE_NENGO,          /**< Pseudo candidate type: Reading string of Half-width number -> Date(Format : 平成(昭和)YY年)   */
        NJ_TYPE_HAN_YOMI2DATE_MM,             /**< Pseudo candidate type: Reading string of Half-width number -> Date(Format : MM月)             */
        NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM,       /**< Pseudo candidate type: Reading string of Half-width number -> Date(Format : MM/DD)            */
        NJ_TYPE_HAN_YOMI2DATE_MMDD,           /**< Pseudo candidate type: Reading string of Half-width number -> Date(Format : MM月DD日)         */
        NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM_WEEK,  /**< Pseudo candidate type: Reading string of Half-width number -> Date(Format : MM/DD(曜日))      */
        NJ_TYPE_HAN_YOMI2DATE_MMDD_WEEK,      /**< Pseudo candidate type: Reading string of Half-width number -> Date(Format : MM月DD日(曜日))   */
        NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM,       /**< Pseudo candidate type: Reading string of Half-width number -> Time(Format : HH:MM)            */
        NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM_AMPM,  /**< Pseudo candidate type: Reading string of Half-width number -> Time(Format : AM(PM)HH:MM)      */
        NJ_TYPE_HAN_YOMI2TIME_HHMM,           /**< Pseudo candidate type: Reading string of Half-width number -> Time(Format : HH時MM分)         */
        NJ_TYPE_HAN_YOMI2TIME_HHMM_12H,       /**< Pseudo candidate type: Reading string of Half-width number -> Time(Format : 午前(後)HH時MM分) */
        NJ_TYPE_HIRAGANA,                     /**< Full-width Hiragana                 */
        NJ_TYPE_KATAKANA,                     /**< Full-width Katakana                 */
        NJ_TYPE_HANKATA,                      /**< Half-width Katakana                 */
        NJ_TYPE_HAN_EIJI_LOWER,               /**< Half-width English characters(All small letters)  */
        NJ_TYPE_ZEN_EIJI_LOWER,               /**< Full-width English characters(All small letters)  */
        NJ_TYPE_HAN_EIJI_UPPER,               /**< Half-width English characters(All capital letters)*/
        NJ_TYPE_ZEN_EIJI_UPPER,               /**< Full-width English characters(All capital letters)*/
        NJ_TYPE_HAN_EIJI_CAP,                 /**< Half-width English characters(with capital on top)*/
        NJ_TYPE_ZEN_EIJI_CAP,                 /**< Full-width English characters(with capital on top)*/
        NJ_TYPE_HAN_SUUJI_COMMA,              /**< Half-width Number(Comma)            */
        NJ_TYPE_HAN_SUUJI,                    /**< Half-width Number                   */
        NJ_TYPE_ZEN_SUUJI,                    /**< Full-width Number                   */
        NJ_TYPE_HAN_TIME_HH,                  /**< Half-width Time(H hour)             */
        NJ_TYPE_HAN_TIME_MM,                  /**< Half-width Time(M minute)           */
        NJ_TYPE_HAN_TIME_HM,                  /**< Half-width Time(H hour & M minute)  */
        NJ_TYPE_HAN_TIME_HMM,                 /**< Half-width Time(H hour & MM minute) */
        NJ_TYPE_HAN_TIME_HHM,                 /**< Half-width Time(HH hour & M minute) */
        NJ_TYPE_HAN_TIME_HHMM,                /**< Half-width Time(HH hour & MM minute)*/
        NJ_TYPE_HAN_TIME_HMM_SYM,             /**< Half-width Time(H:MM)               */
        NJ_TYPE_HAN_TIME_HHMM_SYM,            /**< Half-width Time(HH:MM)              */
        NJ_TYPE_HAN_DATE_YYYY,                /**< Half-width Date(YYYY year)          */
        NJ_TYPE_HAN_DATE_MM,                  /**< Half-width Date(M month)            */
        NJ_TYPE_HAN_DATE_DD,                  /**< Half-width Date(D day)              */
        NJ_TYPE_HAN_DATE_MD,                  /**< Half-width Date(M month D day)      */
        NJ_TYPE_HAN_DATE_MDD,                 /**< Half-width Date(M month DD day)     */
        NJ_TYPE_HAN_DATE_MMD,                 /**< Half-width Date(MM month D day)     */
        NJ_TYPE_HAN_DATE_MMDD,                /**< Half-width Date(MM month DD day)    */
        NJ_TYPE_HAN_DATE_MD_SYM,              /**< Half-width Date(M/D)                */
        NJ_TYPE_HAN_DATE_MDD_SYM,             /**< Half-width Date(M/DD)               */
        NJ_TYPE_HAN_DATE_MMD_SYM,             /**< Half-width Date(MM/D)               */
        NJ_TYPE_HAN_DATE_MMDD_SYM             /**< Half-width Date(MM/DD)              */
    }
};

/** Definition of pseudo predicted candidates for Japanese 10 keyboard */
static const NJ_GIJISET  WLF_GIJI_EISUKANA_FIXED_JP_10 = {
    WLF_GIJI_EISUKANA_FIXED_CNT_JP_10,
    {
        NJ_TYPE_HIRAGANA,           /**< Full-width Hiragana                 */
        NJ_TYPE_KATAKANA,           /**< Full-width Katakana                 */
        NJ_TYPE_HANKATA,            /**< Half-width Katakana                 */
        NJ_TYPE_HAN_EIJI_LOWER,     /**< Half-width English characters(All small letters)  */
        NJ_TYPE_ZEN_EIJI_LOWER,     /**< Full-width English characters(All small letters)  */
        NJ_TYPE_HAN_EIJI_UPPER,     /**< Half-width English characters(All capital letters)*/
        NJ_TYPE_ZEN_EIJI_UPPER,     /**< Full-width English characters(All capital letters)*/
        NJ_TYPE_HAN_EIJI_CAP,       /**< Half-width English characters(with capital on top)*/
        NJ_TYPE_ZEN_EIJI_CAP,       /**< Full-width English characters(with capital on top)*/
        NJ_TYPE_HAN_SUUJI_COMMA,    /**< Half-width Number(Comma)            */
        NJ_TYPE_HAN_SUUJI,          /**< Half-width Number                   */
        NJ_TYPE_ZEN_SUUJI,          /**< Full-width Number                   */
        NJ_TYPE_HAN_TIME_HH,        /**< Half-width Time(H hour)             */
        NJ_TYPE_HAN_TIME_MM,        /**< Half-width Time(M minute)           */
        NJ_TYPE_HAN_TIME_HM,        /**< Half-width Time(H hour & M minute)  */
        NJ_TYPE_HAN_TIME_HMM,       /**< Half-width Time(H hour & MM minute) */
        NJ_TYPE_HAN_TIME_HHM,       /**< Half-width Time(HH hour & M minute) */
        NJ_TYPE_HAN_TIME_HHMM,      /**< Half-width Time(HH hour & MM minute)*/
        NJ_TYPE_HAN_TIME_HMM_SYM,   /**< Half-width Time(H:MM)               */
        NJ_TYPE_HAN_TIME_HHMM_SYM,  /**< Half-width Time(HH:MM)              */
        NJ_TYPE_HAN_DATE_YYYY,      /**< Half-width Date(YYYY year)          */
        NJ_TYPE_HAN_DATE_MM,        /**< Half-width Date(M month)            */
        NJ_TYPE_HAN_DATE_DD,        /**< Half-width Date(D day)              */
        NJ_TYPE_HAN_DATE_MD,        /**< Half-width Date(M month D day)      */
        NJ_TYPE_HAN_DATE_MDD,       /**< Half-width Date(M month DD day)     */
        NJ_TYPE_HAN_DATE_MMD,       /**< Half-width Date(MM month D day)     */
        NJ_TYPE_HAN_DATE_MMDD,      /**< Half-width Date(MM month DD day)    */
        NJ_TYPE_HAN_DATE_MD_SYM,    /**< Half-width Date(M/D)                */
        NJ_TYPE_HAN_DATE_MDD_SYM,   /**< Half-width Date(M/DD)               */
        NJ_TYPE_HAN_DATE_MMD_SYM,   /**< Half-width Date(MM/D)               */
        NJ_TYPE_HAN_DATE_MMDD_SYM   /**< Half-width Date(MM/DD)              */
    }
};


/** Pseudo prediction dictionary for Japanese QWERTY keyboard */
static const NJ_GIJISET  WLF_GIJI_PREDICT_FIXED_JP_FULL = {
    WLF_GIJI_PREDICT_FIXED_CNT_JP_FULL,
    {
        NJ_TYPE_HAN_YOMI2DATE_INIT,           /**< Pseudo candidate type: Reading string of Half-width number -> Date/Time Initialize            */
        NJ_TYPE_HAN_YOMI2DATE_YYYY,           /**< Pseudo candidate type: Reading string of Half-width number -> Date(Format : YYYY年)           */
        NJ_TYPE_HAN_YOMI2DATE_NENGO,          /**< Pseudo candidate type: Reading string of Half-width number -> Date(Format : 平成(昭和)YY年)   */
        NJ_TYPE_HAN_YOMI2DATE_MM,             /**< Pseudo candidate type: Reading string of Half-width number -> Date(Format : MM月)             */
        NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM,       /**< Pseudo candidate type: Reading string of Half-width number -> Date(Format : MM/DD)            */
        NJ_TYPE_HAN_YOMI2DATE_MMDD,           /**< Pseudo candidate type: Reading string of Half-width number -> Date(Format : MM月DD日)         */
        NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM_WEEK,  /**< Pseudo candidate type: Reading string of Half-width number -> Date(Format : MM/DD(曜日))      */
        NJ_TYPE_HAN_YOMI2DATE_MMDD_WEEK,      /**< Pseudo candidate type: Reading string of Half-width number -> Date(Format : MM月DD日(曜日))   */
        NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM,       /**< Pseudo candidate type: Reading string of Half-width number -> Time(Format : HH:MM)            */
        NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM_AMPM,  /**< Pseudo candidate type: Reading string of Half-width number -> Time(Format : AM(PM)HH:MM)      */
        NJ_TYPE_HAN_YOMI2TIME_HHMM,           /**< Pseudo candidate type: Reading string of Half-width number -> Time(Format : HH時MM分)         */
        NJ_TYPE_HAN_YOMI2TIME_HHMM_12H,       /**< Pseudo candidate type: Reading string of Half-width number -> Time(Format : 午前(後)HH時MM分) */
        NJ_TYPE_HIRAGANA,                     /**< Full-width Hiragana        */
        NJ_TYPE_KATAKANA,                     /**< Full-width Katakana        */
        NJ_TYPE_HANKATA,                      /**< Half-width Katakana        */
    }
};

/** 12 Japanese keypad pseudo(GIJI) candidate definition */
static const WLF_GIJIPACK_INFO  WLF_GIJIPACK_JP_10 = {
    .module =
    {
        &WLF_GIJI_PREDICT_PROGRAM_JP_10,
        &WLF_GIJI_CONVERT_PROGRAM_JP_10,
        &WLF_GIJI_EISUKANA_PROGRAM_JP_10
    },
    .fixed =
    {
        &WLF_GIJI_PREDICT_FIXED_JP_10,
        &WLF_GIJI_CONVERT_FIXED_JP_10,
        &WLF_GIJI_EISUKANA_FIXED_JP_10
    }
};

/** Japanese QWERTY keypad pseudo(GIJI) candidate definition */
static const WLF_GIJIPACK_INFO  WLF_GIJIPACK_JP_FULL = {
    .module =
    {
        &WLF_GIJI_PREDICT_PROGRAM_JP_FULL,
        &WLF_GIJI_CONVERT_PROGRAM_JP_FULL,
        &WLF_GIJI_EISUKANA_PROGRAM_JP_FULL
    },
    .fixed =
    {
        &WLF_GIJI_PREDICT_FIXED_JP_FULL,
        &WLF_GIJI_CONVERT_FIXED_JP_FULL,
        &WLF_GIJI_EISUKANA_FIXED_JP_FULL
    }
};

/** 12 Latin keypad pseudo(GIJI) candidate definition */
static const WLF_GIJIPACK_INFO  WLF_GIJIPACK_LATIN_10 = {
    .module =
    {
        &WLF_GIJI_PREDICT_PROGRAM_LATIN_10,
        &WLF_GIJI_CONVERT_PROGRAM_LATIN_10,
        &WLF_GIJI_EISUKANA_PROGRAM_LATIN_10
    },
    .fixed =
    {
        &WLF_GIJI_PREDICT_FIXED_LATIN_10,
        &WLF_GIJI_CONVERT_FIXED_LATIN_10,
        &WLF_GIJI_EISUKANA_FIXED_LATIN_10
    }
};

/** Latin QWERTY keypad pseudo(GIJI) candidate definition */
static const WLF_GIJIPACK_INFO  WLF_GIJIPACK_LATIN_FULL = {
    .module =
    {
        &WLF_GIJI_PREDICT_PROGRAM_LATIN_FULL,
        &WLF_GIJI_CONVERT_PROGRAM_LATIN_FULL,
        &WLF_GIJI_EISUKANA_PROGRAM_LATIN_FULL
    },
    .fixed =
    {
        &WLF_GIJI_PREDICT_FIXED_LATIN_FULL,
        &WLF_GIJI_CONVERT_FIXED_LATIN_FULL,
        &WLF_GIJI_EISUKANA_FIXED_LATIN_FULL
    }
};

/** No pseudo candidate definition */
static const WLF_GIJIPACK_INFO  WLF_GIJIPACK_NONE = {
    .module =
    {
        &WLF_GIJI_GIJISET_NONE,
        &WLF_GIJI_GIJISET_NONE,
        &WLF_GIJI_GIJISET_NONE,
    },
    .fixed =
    {
        &WLF_GIJI_GIJISET_NONE,
        &WLF_GIJI_GIJISET_NONE,
        &WLF_GIJI_GIJISET_NONE,
    }
};


#define WLF_GIJIPACK_MAX_LANG       2   /**< Number of maximum languages        */
#define WLF_GIJIPACK_MAX_KEYTYPE    2   /**< Number of maximum keyboard types   */

static const WLF_GIJIPACK_INFO  *WLF_GIJIPACK_TABLES[WLF_GIJIPACK_MAX_LANG][WLF_GIJIPACK_MAX_KEYTYPE] = {
    { &WLF_GIJIPACK_JP_10, &WLF_GIJIPACK_JP_FULL },
    { &WLF_GIJIPACK_LATIN_10, &WLF_GIJIPACK_LATIN_FULL },
};


