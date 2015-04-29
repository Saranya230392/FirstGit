/**
 * @file
 *   共通関数(文字列関数)
 *
 *   文字列アクセス関数を提供する。 
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2011 All Rights Reserved.
 */

/************************************************/
/*           インクルードファイル               */
/************************************************/
#include "nj_lib.h"
#include "nj_dicif.h"
#include "njfilter.h"
#include "nj_ext.h"

/************************************************/
/*              prototype  宣  言               */
/************************************************/

/************************************************/
/*              extern  宣  言                  */
/************************************************/

/**********************************************************************
 * function
 **********************************************************************/

/**
 * NJ_CHAR文字列をコピーする
 *
 * 標準関数strcpy()のNJ_CHAR版(NUL含む)
 *
 * @note      NJ_CHAR_NULを含む文字列コピーを行う。
 * @attention コピー先文字列は終端がNJ_CHAR_NUL文字で終了していること。
 * 
 * @param[out]   dst  コピー先文字列
 * @param[in]    src  コピー元文字列
 *
 * @return       dstのアドレス
 */
NJ_CHAR *nj_strcpy(NJ_CHAR *dst, NJ_CHAR *src) {

    NJ_CHAR *ret = dst;


    while (*src != NJ_CHAR_NUL) {
        *dst++ = *src++;
    }
    *dst = *src;
    return ret;
}


/**
 * NJ_CHAR文字列をコピーする（コピー文字列長指定）
 *
 * 標準関数strncpy()のNJ_CHAR版(NUL含む)
 *
 * @note      コピー元文字列がlenよりも短ければ、dstの残り部分をNJ_CHAR_NULで埋める。
 * @attention コピー元文字列がlenよりも長い場合、dstはNJ_CHAR_NULで終了しない。
 * 
 * @param[out]   dst  コピー先文字列
 * @param[in]    src  コピー元文字列
 * @param[in]    n    コピー文字配列長(NJ_CHAR型)
 *
 * @return       dstのアドレス
 */
NJ_CHAR *nj_strncpy(NJ_CHAR *dst, NJ_CHAR *src, NJ_UINT16 n) {

    NJ_CHAR *d = dst;


    while (n != 0) {
        if (*src == NJ_CHAR_NUL) {
            while (n != 0) {
                *d++ = NJ_CHAR_NUL;
                n--;
            }
            break;
        } else {
            *d++ = *src++;
        }
        n--;
    }
    return dst;
}


/**
 * NJ_CHAR文字列の長さをカウントする
 *
 * 標準関数strlen()のNJ_CHAR版
 *
 * @attention    入力文字列は、NJ_CHAR_NUL文字で終了していること。
 * 
 * @param[in]    c    入力文字列
 *
 * @return       文字配列長(NJ_CHAR型)
 */
NJ_UINT16 nj_strlen(NJ_CHAR *c) {

    NJ_UINT16 count = 0;


    while (*c++ != NJ_CHAR_NUL) {
        count++;
    }
    return count;
}


/**
 * NJ_CHAR文字列を比較する
 *
 * 標準関数strcmp()のNJ_CHAR版
 *
 * @attention    比較文字列Aは、NJ_CHAR_NUL文字で終了していること。
 *
 * @param[in]    s1    比較文字列A
 * @param[in]    s2    比較文字列B
 *
 * @retval >0    s1 > s2
 * @retval =0    s1 = s2
 * @retval <0    s1 < s2
 */
NJ_INT16 nj_strcmp(NJ_CHAR *s1, NJ_CHAR *s2) {

    while (*s1 == *s2) {
        if (*s1 == NJ_CHAR_NUL) {
            return (0);
        }
        s1++;
        s2++;
    }
    return NJ_CHAR_DIFF(s1, s2);
}


/**
 * NJ_CHAR文字列を比較する
 *
 * 標準関数strncmp()のNJ_CHAR版
 *
 * @attention    比較文字列Aは、NJ_CHAR_NUL文字で終了していること。
 *
 * @param[in]    s1    比較文字列A
 * @param[in]    s2    比較文字列B
 * @param[in]    n     比較文字配列数(NJ_CHAR型)
 *
 * @retval  >0     s1 > s2
 * @retval  =0     s1 = s2
 * @retval  <0     s1 < s2
 */
NJ_INT16 nj_strncmp(NJ_CHAR *s1, NJ_CHAR *s2, NJ_UINT16 n) {

    while (n != 0) {
        if (*s1 != *s2++) {
            return NJ_CHAR_DIFF(s1, (s2 - 1));
        }
        if (*s1++ == NJ_CHAR_NUL) {
            break; /*NCH_FB*/
        }
        n--;
    }
    return (0);
}


/**
 * NJ_CHAR文字列の文字数をカウントする
 *
 * @attention    入力文字列は、NJ_CHAR_NUL文字で終了していること。
 *
 * @param[in]    c     入力文字列
 *
 * @return       文字数
 */
NJ_UINT16 nj_charlen(NJ_CHAR *c) {

    NJ_UINT16 count = 0;
    

    while (*c != NJ_CHAR_NUL) {
        count++;
        c += NJ_CHAR_LEN(c);
    }
    return count;
}


/**
 * 文字列を比較する
 *
 * 指定文字数の文字列比較を行う。
 *
 * @attention    比較文字列Aは、NJ_CHAR_NUL文字で終了していること。
 *
 * @param[in]    s1    比較文字列A
 * @param[in]    s2    比較文字列B
 * @param[in]    n     比較文字数(NJ_CHAR型)
 *
 * @retval  >0     s1 > s2
 * @retval  =0     s1 = s2
 * @retval  <0     s1 < s2
 */
NJ_INT16 nj_charncmp(NJ_CHAR *s1, NJ_CHAR *s2, NJ_UINT16 n) {
    NJ_UINT16 i;


    while (n != 0) {
        for (i = NJ_CHAR_LEN(s1); i != 0; i--) {
            if (*s1 != *s2) {
                return NJ_CHAR_DIFF(s1, s2);
            }
            if (*s1 == NJ_CHAR_NUL) { /* *s1 == *s1 == NJ_CHAR_NUL */
                break; /*NCH_FB*/
            }
            s1++;
            s2++;
        }
        n--;
    }
    return (0);
}


/**
 * 文字列をコピーする
 *
 * 指定文字数分の文字列コピーを行う。
 *
 * @note      コピー元文字列がlenよりも短ければ、dstの文字列終端をNJ_CHAR_NULする。
 * @attention コピー元文字列がlenよりも長い場合、dstはNJ_CHAR_NULで終了しない。
 * 
 * @param[out]   dst  コピー先文字列
 * @param[in]    src  コピー元文字列
 * @param[in]    n    文字数
 *
 * @return       dstのアドレス
 */
NJ_CHAR *nj_charncpy(NJ_CHAR *dst, NJ_CHAR *src, NJ_UINT16 n) {

    NJ_CHAR *d = dst;
    NJ_UINT16 i;


    while (n != 0) {
        for (i = NJ_CHAR_LEN(src); i != 0; i--) {
            *d = *src;
            if (*src == NJ_CHAR_NUL) {
                return dst; /*NCH_FB*/
            }
            d++;
            src++;
        }
        n--;
    }
    *d = NJ_CHAR_NUL;
    return dst;
}


/**
 * メモリーのコピーする
 *
 * 標準関数memcpy()
 *
 * 
 * @param[out]   dst  コピー先アドレス
 * @param[in]    src  コピー元アドレス
 * @param[in]    n    コピーバイト数
 *
 * @return       dstのアドレス
 */
NJ_UINT8 *nj_memcpy(NJ_UINT8 *dst, NJ_UINT8 *src, NJ_UINT16 n) {

    NJ_UINT8 *d = dst;


    while (n != 0) {
        *d++ = *src++;
        n--;
    }
    return dst;
}


/**
 * メモリーを比較する
 *
 * 標準関数memcmp()
 *
 * @param[in]    s1    比較アドレスA
 * @param[in]    s2    比較アドレスB
 * @param[in]    n     比較バイト数
 *
 * @retval  >0     s1 > s2
 * @retval  =0     s1 = s2
 * @retval  <0     s1 < s2
 */
NJ_INT16 nj_memcmp(NJ_UINT8 *s1, NJ_UINT8 *s2, NJ_UINT16 n) { /*NCH_FB*/


    if (n == 0) {
        return 0; /*NCH_FB*/
    }
    do {
        if (*s1 != *s2++) {
            return (NJ_INT16)(*s1 - *(s2 - 1));
        }
        s1++;
    } while (--n != 0);

    return 0;
}


/**
 * NJ_CHAR文字列の長さをカウントする（Limit版）
 *
 * 標準関数strlen()のNJ_CHAR版
 * 最大でlimitまでの長さをカウントする
 *
 * @attention    入力文字列は、NJ_CHAR_NUL文字で終了していること。
 * 
 * @param[in]    c     入力文字列
 * @param[in]    limit 制限文字配列長
 *
 * @return       文字配列長(NJ_CHAR型)
 */
NJ_UINT16 nj_strlen_limit(NJ_CHAR *c, NJ_UINT16 limit) {

    NJ_UINT16 count = 0;


    while ((*c++ != NJ_CHAR_NUL) && (limit-- != 0)) {
        count++;
    }
    return count;
}
