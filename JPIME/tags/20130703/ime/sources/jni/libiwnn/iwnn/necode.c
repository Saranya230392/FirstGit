/**
 * @file
 *   文字コード依存処理
 *
 *   文字コードに依存する処理はこのファイルにまとめる。
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2011 All Rights Reserved.
 */

/************************************************/
/*           インクルードファイル               */
/************************************************/
#include "nj_lib.h"
#include "nje.h"
#include "nj_dicif.h"
#include "njfilter.h"
#include "nj_ext.h"

/************************************************/
/*              define  宣  言                  */
/************************************************/

/** 文字種：ひらがな */
#define HIRAGANA        (NJ_UINT16)(0x0001)
/** 文字種：カタカナ */
#define KATAKANA        (NJ_UINT16)(0x0002)
/** 文字種：半角カタカナ */
#define HANKATA         (NJ_UINT16)(0x0004)
/** 文字種：半角数字 */
#define HAN_SUUJI       (NJ_UINT16)(0x0008)
/** 文字種：全角数字 */
#define ZEN_SUUJI       (NJ_UINT16)(0x0010)
/** 文字種：全角カタカナ記号 */
#define HANKATA_SYM     (NJ_UINT16)(0x0020)

/** 記号表のサイズ */
#define KIGOU_TABLE_SIZE        7
/** 全角ひらがな表のサイズ */
#define HIRA_ZENKAKU_TABLE_SIZE 63

/*
 * カタカナ変換コード
 */
#ifdef NJ_OPT_UTF16
#define HIRA_KATA_OFFSET (0x0060)    /**< カタカナ変換用オフセット(UTF16) */
#define HAN_KANA_TOP     (0xff61)    /**< 半角カナ開始コード(UTF16) */
#define KATAKANA_VU      (0x30f4)    /**< 「ヴ」(UTF16) */
#define KATAKANA_XKA     (0x30f5)    /**< 「ヵ」(UTF16) */
#define KATAKANA_XKE     (0x30f6)    /**< 「ヶ」(UTF16) */
#define HIRAGANA_U       (0x3046)    /**< 「う」(UTF16) */
#define HIRAGANA_DAKUTEN (0x309b)    /**< 「゛」(UTF16) */
#define HIRAGANA_XKA     (0x304b)    /**< 「か」(UTF16) */
#define HIRAGANA_XKE     (0x3051)    /**< 「け」(UTF16) */
#define HAN_KANA_DAKUTEN    (0xff9e)    /**< 「゛」(UTF16) */
#define HAN_KANA_HANDAKUTEN (0xff9f)    /**< 「゜」(UTF16) */
#else /* NJ_OPT_UTF16 */
#define HIRA_KATA_OFFSET (0x00A1)    /**< カタカナ変換用オフセット(SJIS) */
#define HAN_KANA_TOP     (0x00A1)    /**< 半角カナ開始コード(SJIS) */
#define KATAKANA_MU      (0x8380)    /**< 「ム」(SJIS) 以降はOFFSETが0xA2になる */
#define HIRAGANA_MU      (0x82DE)    /**< 「む」(SJIS) 以降はOFFSETが0xA2になる */
#define KATAKANA_VU      (0x8394)    /**< 「ヴ」(SJIS) */
#define KATAKANA_XKA     (0x8395)    /**< 「ヵ」(SJIS) */
#define KATAKANA_XKE     (0x8396)    /**< 「ヶ」(SJIS) */
#define HIRAGANA_U       (0x82a4)    /**< 「う」(SJIS) */
#define HIRAGANA_DAKUTEN (0x814a)    /**< 「゛」(SJIS) */
#define HIRAGANA_XKA     (0x82a9)    /**< 「か」(SJIS) */
#define HIRAGANA_XKE     (0x82af)    /**< 「け」(SJIS) */
#define HAN_KANA_DAKUTEN    (0x00de)    /**< 「゛」(SJIS) */
#define HAN_KANA_HANDAKUTEN (0x00df)    /**< 「゜」(SJIS) */
#endif /* NJ_OPT_UTF16 */

#ifdef NJ_OPT_UTF16
#define HAN_CHAR_LEN  1  /**< 半角文字１文字の配列長(UTF16) */
#define ZEN_CHAR_LEN  1  /**< 全角文字１文字の配列長(UTF16) */
#else  /* NJ_OPT_UTF16 */
#define HAN_CHAR_LEN  1  /**< 半角文字１文字の配列長(SJIS) */
#define ZEN_CHAR_LEN  2  /**< 全角文字１文字の配列長(SJIS) */
#endif /* NJ_OPT_UTF16 */

#define CHAR_TO_WCHAR(s)                                        \
    ((NJ_UINT16)( (((NJ_UINT8*)(s))[0] << 8) | ((NJ_UINT8*)(s))[1] ))

#define SET_WCHAR_TO_CHAR(s, c)                                \
    {                                                          \
        ((NJ_UINT8*)(s))[0] = (NJ_UINT8)(((c) >> 8) & 0x00ff); \
        ((NJ_UINT8*)(s))[1] = (NJ_UINT8)(((c))      & 0x00ff); \
    }

#ifdef NJ_OPT_UTF16
#define IS_HAN_WCHAR(c)   \
    ( (                   ((c) <= 0x00ff)) ||   \
      (((c) >= 0xff61) && ((c) <= 0xff9f)) )
#else  /* NJ_OPT_UTF16 */
#define IS_HAN_WCHAR(c)   \
    ((c) <= 0x00ff)
#endif /* NJ_OPT_UTF16 */

#define IS_HAN_NUM_WCHAR(c)   ( ((c) >= 0x0030) && ((c) <= 0x0039) )

#define IS_HAN_NUM_SYM_WCHAR(c)   ( ((c) == 0x002c) || ((c) == 0x002e) )

#ifdef NJ_OPT_UTF16
/* -------------------------------------------------- */
/* 0x0021 0x0026 0x002c 0x002e 0x003d 0x003f          */
/*      !      &      ,    .      =      ?            */
/* 0x007e 0xff61 0xff64 0xff65 0xff70 0xff9e 0xff9f   */
/*      ~      。      、    ・    ー     ゛     ゜     */
/* -------------------------------------------------- */
#define IS_HAN_KANA_SYM_WCHAR(c)            \
    ( ((c) == 0x0021) || ((c) == 0x0026) || \
      ((c) == 0x002c) || \
      ((c) == 0x002e) || ((c) == 0x003d) || \
      ((c) == 0x003f) || ((c) == NJ_CHAR_WAVE_DASH_SMALL) || \
      ((c) == 0xff61) || ((c) == 0xff64) || \
      ((c) == 0xff65) || ((c) == 0xff70) || \
      ((c) == 0xff9e) || ((c) == 0xff9f) )
#else  /* NJ_OPT_UTF16 */
/* ------------------------------------ */
/* 0x21 0x26 0x2c 0x2e 0x3d 0x3f        */
/*    !    &    ,   .    =    ?         */
/* 0x7e 0xa1 0xa4 0xa5 0xb0 0xde 0xdf   */
/*    ~    。   、  ・   ー   ゛    ゜  */
/* ------------------------------------ */
#define IS_HAN_KANA_SYM_WCHAR(c)                        \
    ( ((c) == 0x0021) || ((c) == 0x0026) ||             \
      ((c) == 0x002c) || \
      ((c) == 0x002e) || ((c) == 0x003d) ||             \
      ((c) == 0x003f) || ((c) == 0x007e) ||             \
      ((c) == 0x00a1) || ((c) == 0x00a4) ||             \
      ((c) == 0x00a5) || ((c) == 0x00b0) ||             \
      ((c) == 0x00de) || ((c) == 0x00df) )
#endif /* NJ_OPT_UTF16 */

#ifdef NJ_OPT_UTF16
/* -------------------------------------------------- */
/* 0x0021 0x0026 0x002c 0x002d 0x002e 0x003d 0x003f   */
/*      !      &      ,      -      .      =      ?   */
/* 0x007e 0xff61 0xff64 0xff65 0xff70 0xff9e 0xff9f   */
/*      ~      。      、    ・    ー     ゛     ゜     */
/* -------------------------------------------------- */
#define IS_HAN_KANA_SYM_PLUS_WCHAR(c)       \
    ( ((c) == 0x0021) || ((c) == 0x0026) || \
      ((c) == 0x002c) || ((c) == 0x002d) || \
      ((c) == 0x002e) || ((c) == 0x003d) || \
      ((c) == 0x003f) || ((c) == NJ_CHAR_WAVE_DASH_SMALL) || \
      ((c) == 0xff61) || ((c) == 0xff64) || \
      ((c) == 0xff65) || ((c) == 0xff70) || \
      ((c) == 0xff9e) || ((c) == 0xff9f) )
#else  /* NJ_OPT_UTF16 */
/* ------------------------------------ */
/* 0x21 0x26 0x2c 0x2d 0x2e 0x3d 0x3f   */
/*    !    &    ,    -    .    =    ?   */
/* 0x7e 0xa1 0xa4 0xa5 0xb0 0xde 0xdf   */
/*    ~    。   、  ・   ー   ゛    ゜  */
/* ------------------------------------ */
#define IS_HAN_KANA_SYM_PLUS_WCHAR(c)                   \
    ( ((c) == 0x0021) || ((c) == 0x0026) ||             \
      ((c) == 0x002c) || ((c) == 0x002d) ||             \
      ((c) == 0x002e) || ((c) == 0x003d) ||             \
      ((c) == 0x003f) || ((c) == 0x007e) ||             \
      ((c) == 0x00a1) || ((c) == 0x00a4) ||             \
      ((c) == 0x00a5) || ((c) == 0x00b0) ||             \
      ((c) == 0x00de) || ((c) == 0x00df) )
#endif /* NJ_OPT_UTF16 */

#ifdef NJ_OPT_UTF16
/* 0xff61, 0xff64, [0xff66, 0xff9f] */
#define IS_HAN_KANA_WCHAR(c)   (((c) >= 0xff66) && ((c) <= 0xff9f))
#else  /* NJ_OPT_UTF16 */
/* ------------------------------------ */
/* 0xa1 0xa4 0xa6 ... 0xdf              */
/*    。  、  ヲ        ゜              */
/* ------------------------------------ */
#define IS_HAN_KANA_WCHAR(c)   (((c) >= 0x00a6) && ((c) <= 0x00df))
#endif /* NJ_OPT_UTF16 */

#ifdef NJ_OPT_UTF16
#define IS_HAN_KANA_ON_TABLE_WCHAR(c)   \
    (((c) == 0xff61) || (((c) >= 0xff64) && ((c) <= 0xff9f)))
#else  /* NJ_OPT_UTF16 */
#define IS_HAN_KANA_ON_TABLE_WCHAR(c)   \
    (((c) == 0x00a1) || (((c) >= 0x00a4) && ((c) <= 0x00df)))
#endif /* NJ_OPT_UTF16 */

#ifdef NJ_OPT_UTF16
#define IS_HAN_KANA_ENABLE_DAKU_WCHAR(c)   \
    ( (((c) >= 0xff76) && ((c) <= 0xff84)) || /* カ、サ、タ行 */ \
      (((c) >= 0xff8a) && ((c) <= 0xff8e)) )  /* ハ行 */
#else  /* NJ_OPT_UTF16 */
#define IS_HAN_KANA_ENABLE_DAKU_WCHAR(c)   \
    ( (((c) >= 0x00b6) && ((c) <= 0x00c4)) || /* カ、サ、タ行 */ \
      (((c) >= 0x00ca) && ((c) <= 0x00ce)) )  /* ハ行 */
#endif /* NJ_OPT_UTF16 */

#ifdef NJ_OPT_UTF16
#define IS_HAN_KANA_ENABLE_HANDAKU_WCHAR(c)   \
    (((c) >= 0xff8a) && ((c) <= 0xff8e))  /* ハ行 */
#else  /* NJ_OPT_UTF16 */
#define IS_HAN_KANA_ENABLE_HANDAKU_WCHAR(c)   \
    (((c) >= 0x00ca) && ((c) <= 0x00ce))  /* ハ行 */
#endif /* NJ_OPT_UTF16 */

#ifdef NJ_OPT_UTF16
#define IS_HAN_KANA_PROLONG_WCHAR(c)   ((c) == 0xff70)
#else  /* NJ_OPT_UTF16 */
#define IS_HAN_KANA_PROLONG_WCHAR(c)   ((c) == 0x00b0)
#endif /* NJ_OPT_UTF16 */

#ifdef NJ_OPT_UTF16
#define IS_HIRAGANA_WCHAR(c)  ( ((c) >= 0x3041) && ((c) <= 0x3093) )
#else  /* NJ_OPT_UTF16 */
#define IS_HIRAGANA_WCHAR(c)  ( ((c) >= 0x829f) && ((c) <= 0x82f1) )
#endif /* NJ_OPT_UTF16 */

#ifdef NJ_OPT_UTF16
#define IS_KATAKANA_WCHAR(c)  ( ((c) >= 0x30a1) && ((c) <= 0x30f6) )
#else  /* NJ_OPT_UTF16 */
#define IS_KATAKANA_WCHAR(c)  ( ((c) >= 0x8340) && ((c) <= 0x8396) )
#endif /* NJ_OPT_UTF16 */

#ifdef NJ_OPT_UTF16
#define IS_NUM_WCHAR(c)  ( ((c) >= 0xff10) && ((c) <= 0xff19) )
#else  /* NJ_OPT_UTF16 */
#define IS_NUM_WCHAR(c)  ( ((c) >= 0x824f) && ((c) <= 0x8258) )
#endif /* NJ_OPT_UTF16 */

#ifdef NJ_OPT_UTF16
#define IS_COMMA_PERIOD_WCHAR(c)  ( ((c) == 0xff0c) || ((c) == 0xff0e) )
#else  /* NJ_OPT_UTF16 */
#define IS_COMMA_PERIOD_WCHAR(c)  ( ((c) == 0x8143) || ((c) == 0x8144) )
#endif /* NJ_OPT_UTF16 */

#ifdef NJ_OPT_UTF16
/* ------------------------------------ */
/* 0x3001 02 .. 1c .. 9b 9c .. fb fc    */
/*     、 。    〜    ゛ ゜    ・ ー    */
/* 0xFF01 .. 06 .. 0c 0e .. 1d .. 1f    */
/*    ！     ＆    ， ．    ＝    ？    */
/* ------------------------------------ */
#define IS_KANA_SYM_WCHAR(c)                            \
    ( ((c) == 0x30fc) || ((c) == NJ_CHAR_WAVE_DASH_BIG) ||      \
      ((c) == 0x3001) || ((c) == 0x3002) ||             \
      ((c) == 0xff0c) || ((c) == 0xff0e) ||             \
      ((c) == 0xff1f) || ((c) == 0xff01) ||             \
      ((c) == 0x30fb) || ((c) == 0xff1d) ||             \
      ((c) == 0xff06) || ((c) == 0x309b) ||             \
      ((c) == 0x309c) )
#else  /* NJ_OPT_UTF16 */
#define IS_KANA_SYM_WCHAR(c)                    \
    ( ((c) == 0x815b) || ((c) == 0x8160) ||     \
      ((c) == 0x8181) || ((c) == 0x8195) ||     \
      (((c) >= 0x8141) && ((c) <=0x8145)) ||    \
      (((c) >= 0x8148) && ((c) <=0x814b)) )
#endif /* NJ_OPT_UTF16 */


/************************************************/
/*              外  部  変  数                  */
/************************************************/

/************************************************/
/*              static  変数宣言                */
/************************************************/

#ifdef NJ_OPT_UTF16
static const NJ_UINT16 kigou_table[KIGOU_TABLE_SIZE * 2] = {
    /* 変換前 */        /* 変換後 */    
    0x0021, /* ! */     0xff01, /* ！ */
    0x0026, /* & */     0xff06, /* ＆ */
    0x002c, /* , */     0xff0c, /* ， */
    0x002e, /* . */     0xff0e, /* ． */
    0x003d, /* = */     0xff1d, /* ＝ */
    0x003f, /* ? */     0xff1f, /* ？ */
    NJ_CHAR_WAVE_DASH_SMALL, /* ~ */     NJ_CHAR_WAVE_DASH_BIG  /* 〜 */
};

/* 清音 & 拗音 & 撥音 & 促音 */
static const NJ_UINT16 hira_zenkaku_table[HIRA_ZENKAKU_TABLE_SIZE] = {
    /* カナ記号 */
    /*。*/  /*「*/  /*」*/  /*、*/  /*・*/ 
    0x3002, 0x300c, 0x300d, 0x3001, 0x30fb,

    /* カナ50音 */
    /*を*/  /*ぁ*/  /*ぃ*/  /*ぅ*/  /*ぇ*/
    0x3092, 0x3041, 0x3043, 0x3045, 0x3047,
    /*ぉ*/  /*ゃ*/  /*ゅ*/  /*ょ*/  /*っ*/
    0x3049, 0x3083, 0x3085, 0x3087, 0x3063,
    /*ー*/  /*あ*/  /*い*/  /*う*/  /*え*/
    0x30fc, 0x3042, 0x3044, 0x3046, 0x3048,
    /*お*/  /*か*/  /*き*/  /*く*/  /*け*/
    0x304a, 0x304b, 0x304d, 0x304f, 0x3051,
    /*こ*/  /*さ*/  /*し*/  /*す*/  /*せ*/
    0x3053, 0x3055, 0x3057, 0x3059, 0x305b,
    /*そ*/  /*た*/  /*ち*/  /*つ*/  /*て*/
    0x305d, 0x305f, 0x3061, 0x3064, 0x3066,
    /*と*/  /*な*/  /*に*/  /*ぬ*/  /*ね*/
    0x3068, 0x306a, 0x306b, 0x306c, 0x306d,
    /*の*/  /*は*/  /*ひ*/  /*ふ*/  /*へ*/
    0x306e, 0x306f, 0x3072, 0x3075, 0x3078,
    /*ほ*/  /*ま*/  /*み*/  /*む*/  /*め*/
    0x307b, 0x307e, 0x307f, 0x3080, 0x3081,
    /*も*/  /*や*/  /*ゆ*/  /*よ*/  /*ら*/
    0x3082, 0x3084, 0x3086, 0x3088, 0x3089,
    /*り*/  /*る*/  /*れ*/  /*ろ*/  /*わ*/
    0x308a, 0x308b, 0x308c, 0x308d, 0x308f,
    /*ん*/ 
    0x3093,

    /* 濁点、半濁点 */
    /*゛*/  /*゜*/ 
    0x309b, 0x309c
};

#else /* NJ_OPT_UTF16 */

static const NJ_UINT16 kigou_table[KIGOU_TABLE_SIZE * 2] = {
    /* 変換前 */        /* 変換後 */    
    0x0021, /* ! */     0x8149, /* ！ */
    0x0026, /* & */     0x8195, /* ＆ */
    0x002c, /* , */     0x8143, /* ， */
    0x002e, /* . */     0x8144, /* ． */
    0x003d, /* = */     0x8181, /* ＝ */
    0x003f, /* ? */     0x8148, /* ？ */
    0x007e, /* ~ */     0x8160  /* 〜 */
};

static const NJ_UINT16 hira_zenkaku_table[HIRA_ZENKAKU_TABLE_SIZE] = {
    /* カナ記号 */
    /*。*/  /*「*/  /*」*/  /*、*/  /*・*/ 
    0x8142, 0x8175, 0x8176, 0x8141, 0x8145,

    /* カナ50音 */
    /*を*/  /*ぁ*/  /*ぃ*/  /*ぅ*/  /*ぇ*/
    0x82f0, 0x829f, 0x82a1, 0x82a3, 0x82a5,
    /*ぉ*/  /*ゃ*/  /*ゅ*/  /*ょ*/  /*っ*/
    0x82a7, 0x82e1, 0x82e3, 0x82e5, 0x82c1,
    /*ー*/  /*あ*/  /*い*/  /*う*/  /*え*/
    0x815b, 0x82a0, 0x82a2, 0x82a4, 0x82a6,
    /*お*/  /*か*/  /*き*/  /*く*/  /*け*/
    0x82a8, 0x82a9, 0x82ab, 0x82ad, 0x82af,
    /*こ*/  /*さ*/  /*し*/  /*す*/  /*せ*/
    0x82b1, 0x82b3, 0x82b5, 0x82b7, 0x82b9,
    /*そ*/  /*た*/  /*ち*/  /*つ*/  /*て*/
    0x82bb, 0x82bd, 0x82bf, 0x82c2, 0x82c4,
    /*と*/  /*な*/  /*に*/  /*ぬ*/  /*ね*/
    0x82c6, 0x82c8, 0x82c9, 0x82ca, 0x82cb,
    /*の*/  /*は*/  /*ひ*/  /*ふ*/  /*へ*/
    0x82cc, 0x82cd, 0x82d0, 0x82d3, 0x82d6,
    /*ほ*/  /*ま*/  /*み*/  /*む*/  /*め*/
    0x82d9, 0x82dc, 0x82dd, 0x82de, 0x82df,
    /*も*/  /*や*/  /*ゆ*/  /*よ*/  /*ら*/
    0x82e0, 0x82e2, 0x82e4, 0x82e6, 0x82e7,
    /*り*/  /*る*/  /*れ*/  /*ろ*/  /*わ*/
    0x82e8, 0x82e9, 0x82ea, 0x82eb, 0x82ed,
    /*ん*/ 
    0x82f1,

    /* 濁点、半濁点 */
    /*゛*/  /*゜*/ 
    0x814a, 0x814b
};
#endif /* NJ_OPT_UTF16 */

/************************************************/
/*              extern  宣  言                  */
/************************************************/

/************************************************/
/*              prototype  宣  言               */
/************************************************/

/**
 * 指定された文字列がどの文字種であるかを調べ、オペレーションIDの文字種(NJ_TYPE_**)で返す。
 *
 * @attention
 *   Unicodeには、EUC/SJISに含まれないコードが 多数存在する。
 *   EUC/SJISに共通する文字のみマッピングされていると仮定した実装をしている。
 *   仕様時は十分に検討する必要がある。
 *
 * @param[in]  s        文字種を調べる対象の文字列
 * @param[in]  max_len  sの検索文字配列長
 *
 * @return
 *   文字種(NJ_TYPE_**)。エラーは発生しない。
 */
NJ_UINT16 nje_check_string(NJ_CHAR *s, NJ_UINT16 max_len) {
    NJ_UINT16 wchar;
    NJ_UINT16 wchar_len = 1;
    NJ_UINT16 flag = 0xffff;


    while ((flag != 0) && (max_len > 0)) {
        /* 文字列終端：検査完了 */
        if (*s == NJ_CHAR_NUL) {
            break;
        }
#ifdef NJ_OPT_UTF16
        wchar = CHAR_TO_WCHAR(s);
#else  /* NJ_OPT_UTF16 */
        if (NJ_CHAR_LEN(s) == 2) {
            wchar = CHAR_TO_WCHAR(s);
            wchar_len = 2;
        } else {
            wchar = (NJ_UINT16)(*s);
            wchar_len = 1;
        }
#endif /* NJ_OPT_UTF16 */
        
        max_len -= wchar_len;
        if (IS_HAN_NUM_WCHAR(wchar)) {
            /* 半角数字文字群 */
            flag &= HAN_SUUJI;
        } else if (IS_HAN_NUM_SYM_WCHAR(wchar)) {
            /* 半角数字記号群、半角カナ記号群で重なっている物「，」「．」 */
            if (((flag & HAN_SUUJI) != 0) &&
                ((max_len == 0) || (*(s + HAN_CHAR_LEN) == NJ_CHAR_NUL))) {
                /* 半角数字文字列処理中に、末尾が半角数字記号である場合は、数字属性とはみなさない */
                flag &= (HANKATA | HANKATA_SYM);
            } else {
                flag &= (HAN_SUUJI | HANKATA | HANKATA_SYM);
            }
        } else if (IS_HIRAGANA_WCHAR(wchar)) {
            /* ひらがな群 */
            flag &= HIRAGANA;
        } else if (IS_KATAKANA_WCHAR(wchar)) {
            /* カタカナ群 */
            flag &= KATAKANA;
        } else if (IS_NUM_WCHAR(wchar)) {
            /* 全角数字群 */
            flag &= ZEN_SUUJI;
        } else if (IS_COMMA_PERIOD_WCHAR(wchar)) {
            /* 全角数字記号群、かな記号群で重なっている物「，」「．」 */
            if (((flag & ZEN_SUUJI) != 0) &&
                ((max_len == 0) || (*s == NJ_CHAR_NUL))) {
                /* 全角数字文字列処理中に、末尾が全角数字記号である場合は、数字属性とはみなさない */
                flag &= (HIRAGANA | KATAKANA);
            } else {
                /* 全角数字記号群 */
                flag &= (ZEN_SUUJI | HIRAGANA | KATAKANA);
            }
        } else if (IS_KANA_SYM_WCHAR(wchar)) {
            /* かな記号群 */
            flag &= (KATAKANA | HIRAGANA);
        } else if (IS_HAN_KANA_SYM_PLUS_WCHAR(wchar)) {
            /* 半角カナ記号 */
            flag &= (HANKATA | HANKATA_SYM);
        } else if (IS_HAN_KANA_WCHAR(wchar)) {
            /* 半角カナ群 */
            flag &= HANKATA;
        } else {
            flag = 0;
            break;
        }
        s += wchar_len;
    }

    if (flag == 0) {
        return NJ_TYPE_UNDEFINE;
    }

    switch (flag) {
    case HIRAGANA:
        return NJ_TYPE_HIRAGANA;
    case KATAKANA:
        return NJ_TYPE_KATAKANA;
    case HAN_SUUJI:
        return NJ_TYPE_HAN_SUUJI;
    case ZEN_SUUJI:
        return NJ_TYPE_ZEN_SUUJI;
    case HANKATA:
        return NJ_TYPE_HANKATA;
    default:
        /* たとえば、「長音(ー)」だけや、句読点だけの場合 */
        return NJ_TYPE_UNDEFINE;
    }
}


/**
 * 先頭の文字種を判定する
 *
 * @param[in]  s  入力文字列
 *
 * @return  文字種(nje.hのNJ_CHAR_TYPE*)
 */
NJ_UINT8 nje_get_top_char_type(NJ_CHAR *s) {

    NJ_UINT16 wchar;


    /* 2バイト文字コード作成 */
#ifdef NJ_OPT_UTF16
    wchar = CHAR_TO_WCHAR(s);
#else  /* NJ_OPT_UTF16 */
    if (NJ_CHAR_LEN(s) == 2) {
        wchar = CHAR_TO_WCHAR(s);
    } else {
        wchar = (NJ_UINT16)(*s);
    }
#endif /* NJ_OPT_UTF16 */

    if (!IS_HAN_WCHAR(wchar)) {
        /* 全角文字のチェック */
        if (IS_NUM_WCHAR(wchar)) { /* 全角数字 */
            return NJ_CHAR_TYPE_ZEN_SUUJI;
        } else if (IS_COMMA_PERIOD_WCHAR(wchar)) { /* 全角数字記号 */
            return NJ_CHAR_TYPE_ZEN_SUUJI_SIG;
        } else if (IS_HIRAGANA_WCHAR(wchar)) { /* ひらがな */
            return NJ_CHAR_TYPE_HIRAGANA;
        } else if (IS_KATAKANA_WCHAR(wchar)) { /* 全角カナ */
            return NJ_CHAR_TYPE_KATAKANA;
        } else if (IS_KANA_SYM_WCHAR(wchar)) { /* かな記号 */
            return NJ_CHAR_TYPE_KANA_SIG;
        }
        /* その他の全角文字 */
        return NJ_CHAR_TYPE_ZEN_UNDEFINE;
    } else {
        /* 半角文字のチェック */
        if (IS_HAN_NUM_WCHAR(wchar)) { /* 半角数字 */
            return NJ_CHAR_TYPE_HAN_SUUJI;
        } else if (IS_HAN_NUM_SYM_WCHAR(wchar)) { /* 半角数字記号 */
            return NJ_CHAR_TYPE_HAN_SUUJI_SIG;
        } else if (IS_HAN_KANA_WCHAR(wchar) &&
                   !IS_HAN_KANA_PROLONG_WCHAR(wchar)) { /* 半角カナ(長音除く) */
            return NJ_CHAR_TYPE_HANKATA;
        } else if (IS_HAN_KANA_SYM_WCHAR(wchar)) { /* 半角カナ記号 */
            return NJ_CHAR_TYPE_HKATA_SIG;
        }
        /* その他の半角文字 */
        return NJ_CHAR_TYPE_HAN_UNDEFINE;
    }
}


/**
 * 全角または半角カタカナをひらがなに変換する
 *
 * @param[in]  kata     カタカナ文字列(変換元)
 * @param[out] hira     ひらがな文字列(変換先)-文字列終端にはNUL文字
 * @param[in]  len      変換文字配列長
 * @param[in]  max_len  hiraバッファの最大文字配列長(終端記号分除く)
 * @param[in]  type     変換種別<br>
 *                      NJ_TYPE_KATAKANA : 全角カタカナをひらがなへ変換<br>
 *                      NJ_TYPE_HANKATA  : 半角カタカナをひらがなへ変換
 *
 * @retval >=0  hiraに書き込んだ文字配列長
 * @retval <0   エラー
 *
 * @attention
 *  len未満でkataにterminatorが来ても正常終了になる。
 */
NJ_INT16 nje_convert_kata_to_hira(NJ_CHAR *kata, NJ_CHAR *hira,
                                  NJ_UINT16 len, NJ_UINT16 max_len, NJ_UINT8 type) {

    NJ_UINT16 wchar, wchar2;
    NJ_UINT16 prev_wchar;
    NJ_INT16 ret = 0;
    NJ_INT16 i;


    /*
     * 第5引数(type)が規定外の場合、変換しない
     */
    if ((type != NJ_TYPE_KATAKANA) && (type != NJ_TYPE_HANKATA)) {
        return -1; /*NCH_FB*/
    }

    if (type == NJ_TYPE_KATAKANA) {
        /*
         * 全角カタカナからひらがなへの変換
         */
        while (len > 0) {
            if (*kata == NJ_CHAR_NUL) {
                /* NUL文字が見つかった時点で処理終了 */
                break; /*NCH_FB*/
            }
            
            /* 文字コード取得 */
#ifdef NJ_OPT_UTF16
            wchar = CHAR_TO_WCHAR(kata);
            kata++;
            len--;
#else  /* NJ_OPT_UTF16 */
            if (NJ_CHAR_LEN(kata) == 2) {
                wchar = CHAR_TO_WCHAR(kata);
                kata += 2;
                len  -= 2;
            } else {
                /* 半角文字は対象外 */
                return -1;
            }
#endif /* NJ_OPT_UTF16 */

            if (max_len < (NJ_UINT16)(ret + ZEN_CHAR_LEN)) {
                /* ひらがな文字列格納領域に空きが無い場合は変換不可 */
                return -1; /*NCH_FB*/
            }

            if (IS_KATAKANA_WCHAR(wchar)) {
                if (wchar == KATAKANA_VU) {
                    /* カタカナ(ヴ)の場合 */
                    if (max_len < (NJ_UINT16)(ret + (ZEN_CHAR_LEN * 2))) {
                        /* ひらがな文字列格納領域に空きが無い場合は変換不可 */
                        return -1; /*NCH_FB*/
                    }
                    /* 「う」 */
                    SET_WCHAR_TO_CHAR(hira, HIRAGANA_U);
                    hira += ZEN_CHAR_LEN;
                    ret  += ZEN_CHAR_LEN;
                    /* 「゛」 */
                    SET_WCHAR_TO_CHAR(hira, HIRAGANA_DAKUTEN);
                    hira += ZEN_CHAR_LEN;
                    ret  += ZEN_CHAR_LEN;
                } else if (wchar == KATAKANA_XKA) {
                    /* カタカナ(ヵ)の場合 */
                    /* 「か」 */
                    SET_WCHAR_TO_CHAR(hira, HIRAGANA_XKA);
                    hira += ZEN_CHAR_LEN;
                    ret  += ZEN_CHAR_LEN;
                } else if (wchar == KATAKANA_XKE) {
                    /* カタカナ(ヶ)の場合 */
                    /* 「け」 */
                    SET_WCHAR_TO_CHAR(hira, HIRAGANA_XKE);
                    hira += ZEN_CHAR_LEN;
                    ret  += ZEN_CHAR_LEN;
                } else {
                    /* カタカナ(ァ〜ン)の場合 */
#ifndef NJ_OPT_UTF16
                    /* SJISの場合、ミとムの間にギャップがあるため補正する */
                    if (wchar >= KATAKANA_MU) {
                        wchar--;
                    }
#endif /* !NJ_OPT_UTF16 */
                    SET_WCHAR_TO_CHAR(hira, wchar - HIRA_KATA_OFFSET);
                    hira += ZEN_CHAR_LEN;
                    ret  += ZEN_CHAR_LEN;
                }
            } else if (IS_KANA_SYM_WCHAR(wchar)) {
                /* カナ記号の場合、そのままコピー */
                SET_WCHAR_TO_CHAR(hira, wchar);
                hira += ZEN_CHAR_LEN;
                ret  += ZEN_CHAR_LEN;
            } else {
                /* 上記以外なら異常終了 */
                return -1; /*NCH_FB*/
            }
        }

    } else if (type == NJ_TYPE_HANKATA) {
        /*
         * 半角カタカナからひらがなへの変換
         */
        prev_wchar = 0;
        while (len > 0) {
            if (*kata == NJ_CHAR_NUL) {
                /* NUL文字が見つかった時点で処理終了 */
                break; /*NCH_FB*/
            }

            /* 文字コード取得 */
#ifdef NJ_OPT_UTF16
            wchar = CHAR_TO_WCHAR(kata);
            kata++;
            len--;
#else  /* NJ_OPT_UTF16 */
            if (NJ_CHAR_LEN(kata) == 1) {
                wchar = (NJ_UINT16)(*kata);
                kata++;
                len--;
            } else {
                /* 全角文字は対象外 */
                return -1;
            }
#endif /* NJ_OPT_UTF16 */

            /* 半角カナ・半角カナ記号を平仮名に変換する */
            if (IS_HAN_KANA_ON_TABLE_WCHAR(wchar)) {
                wchar2 = hira_zenkaku_table[wchar - HAN_KANA_TOP];

                /* 濁音・半濁音の処理を行う */
                if (prev_wchar != 0) {
                    if (wchar == HAN_KANA_DAKUTEN) {
                        /* 処理している文字が濁点 */
                        if (IS_HAN_KANA_ENABLE_DAKU_WCHAR(prev_wchar)) {
                            /* 直前の文字が濁点が付きうる文字 */
                            /* 清音コード+1=濁音コード */
                            wchar2 = hira_zenkaku_table[prev_wchar - HAN_KANA_TOP] + 1;
                            hira -= ZEN_CHAR_LEN;
                            ret  -= ZEN_CHAR_LEN;
                        }
                    } else if (wchar == HAN_KANA_HANDAKUTEN) {
                        /* 処理している文字が半濁点 */
                        if (IS_HAN_KANA_ENABLE_HANDAKU_WCHAR(prev_wchar)) {
                            /* 直前の文字が半濁点が付きうる文字 */
                            /* 清音コード+2=半濁音コード */
                            wchar2 = hira_zenkaku_table[prev_wchar - HAN_KANA_TOP] + 2;
                            hira -= ZEN_CHAR_LEN;
                            ret  -= ZEN_CHAR_LEN;
                        }
                    }
                }
            } else {
                /*
                 * 半角カナ記号をかな記号に変換する.
                 */
                for (i = 0; i < KIGOU_TABLE_SIZE; i++) {
                    /* kigou_tableより、かな記号を取得 */
                    if (wchar == kigou_table[i * 2]) {
                        break;
                    }
                }
                if (i == KIGOU_TABLE_SIZE) {
                    /*
                     * 半角カナ文字でも半角カナ記号でもない
                     * 変換対象外の文字のため、変換不可として、
                     * (-1)リターン
                     */
                    return -1; /*NCH_FB*/
                }
                wchar2 = kigou_table[i * 2 + 1];
            }
            /*
             * ひらがな文字列格納領域に空きが無い場合は
             * 変換不可として、(-1)リターン
             */
            if (max_len < (NJ_UINT16)(ret + ZEN_CHAR_LEN)) {
                return -1; /*NCH_FB*/
            }
            SET_WCHAR_TO_CHAR(hira, wchar2);
            hira += ZEN_CHAR_LEN;
            ret  += ZEN_CHAR_LEN;
            prev_wchar = wchar;
        }
    }

    /* 変換が正常終了した、NUL文字を設定しリターン */
    *hira = NJ_CHAR_NUL;
    return ret;
}


/**
 * ひらがなをカタカナに変換する
 *
 * @param[in]  hira  ひらがな文字列 (変換元)
 * @param[out] kata  カタカナ文字列 (変換先)-文字列終端にはNUL文字
 * @param[in]  len   変換文字配列長
 *
 * @retval >=0  kataに書き込んだ文字配列長
 * @retval <0   エラー
 * 
 * @attention
 *   len未満でhiraにterminatorが来ても正常終了になる
 */
NJ_INT16 nje_convert_hira_to_kata(NJ_CHAR *hira, NJ_CHAR *kata, NJ_UINT16 len) {
    NJ_UINT16 pnt;
    NJ_UINT16 wchar;


    pnt = 0;
    while (pnt < len) {
        if (*hira == NJ_CHAR_NUL) {
            /* ひらがな文字列にNUL文字が見つかった時点で処理終了 */
            return pnt; /*NCH_FB*/
        }

        /* 文字コード取得 */
#ifdef NJ_OPT_UTF16
        wchar = CHAR_TO_WCHAR(hira);
        hira++;
        pnt++;
#else  /* NJ_OPT_UTF16 */
        if (NJ_CHAR_LEN(hira) == 2) {
            wchar = CHAR_TO_WCHAR(hira);
            hira += 2;
            pnt  += 2;
        } else {
            /* 1byte コードの時は無変換 */
            *kata = *hira;
            kata++;
            hira++;
            pnt++;
            continue;
        }
#endif /* NJ_OPT_UTF16 */

        if (IS_HIRAGANA_WCHAR(wchar)) {
            /* ひらがなの場合、カタカナに変換 */
#ifndef NJ_OPT_UTF16
            /* SJISの場合「ミ」と「ム」の間にギャップがあるので補正する */
            if (wchar >= HIRAGANA_MU) {
                wchar++;
            }
#endif /*!NJ_OPT_UTF16*/
            SET_WCHAR_TO_CHAR(kata, wchar + HIRA_KATA_OFFSET);
            kata += ZEN_CHAR_LEN;
        } else {
            SET_WCHAR_TO_CHAR(kata, wchar);
            kata += ZEN_CHAR_LEN;
        }
    }

    /* 変換が正常終了した、NUL文字を設定しリターン */
    *kata = NJ_CHAR_NUL;
    return pnt;
}
