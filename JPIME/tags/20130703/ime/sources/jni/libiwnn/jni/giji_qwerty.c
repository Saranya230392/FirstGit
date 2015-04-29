/**
 * @file
 *   giji_qwerty.c
 *
 * @author
 *   OMRON SOFTWARE Co., Ltd.
 *
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 * (C) Copyright OMRON SOFTWARE Co., Ltd. 2009 All Rights Reserved
 */
#include "nj_lib.h"
#include "nj_dicif.h"
#include "nj_ext.h"
#include "nj_dic.h"
#include "njd.h"
#include "nj_err.h"
#include "giji_qwerty.h"

#define EXGQWERTY_NO_CONV       0
#define EXGQWERTY_LOWER         1
#define EXGQWERTY_TOP_UPPER     2
#define EXGQWERTY_UPPER         3
#define EXGQWERTY_MMDD_SLASH    4
#define EXGQWERTY_HHMM_COLON    5
#define EXGQWERTY_DOLLER_YEN_J  6
#define EXGQWERTY_NUM           9

static NJ_INT16 can_make_giji(NJ_CHAR* str, NJ_UINT16 len, NJ_UINT16 type,
                              NJ_UINT32* locTop, NJ_UINT32* locBottom);
static NJ_INT16 set_word_info(NJ_CHAR* yomi, NJ_UINT16 len,
                              NJ_SEARCH_LOCATION *loct, NJ_DIC_HANDLE rule, NJ_RESULT *giji);
static NJ_INT16 get_giji_candidate(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 olen);
static NJ_UINT32 ex_atod(NJ_CHAR *str, NJ_UINT16 len);


/**
 * Pseudo Dictionary: Dictionary Interface (SJIS only)
 *
 * @param[in,out] iwnn      Private information for iWnn (usually only referenced)
 * @param[in]     request   Request from iWnn
 *                          - NJG_OP_SEARCH: First time searching
 *                          - NJG_OP_SEARCH_NEXT: Next candidate searching
 *                          - NJG_OP_GET_WORD_INFO: Get word info
 *                          - NJG_OP_GET_STROKE: Get reading strings
 *                          - NJG_OP_GET_STRING: Get candidate strings
 *                          - NJG_OP_LEARN: Lean a word
 * @param[in,out] message   info; between iWnn and the pseudo dictionary
 *
 * @retval >=0 Success (depends on the kind of requests)
 * @retval <0  Failure
 */
NJ_EXTERN NJ_INT16 njex_qwerty_giji(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE *message) {

    NJ_INT16 ret = 0;
    NJ_UINT16 i;
    NJ_RESULT rlt;
    NJ_UINT16 len;
    NJ_CHAR  *nj_strncpy_ret;

    if(message == NULL) {
        return -1;    
    }

    switch (request) {
    case NJG_OP_SEARCH:
        if ((message->condition->operation != NJ_CUR_OP_COMP)
            || (message->condition->mode != NJ_CUR_MODE_FREQ)) {
            /* Not supported, except exact match lookup as frequency */
            message->location->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }

        for (i = 0; i < EXGQWERTY_NUM; i++) {
            ret = can_make_giji(message->condition->yomi, message->condition->ylen, i,
                                &message->location->loct.top, &message->location->loct.bottom);
            if (ret > 0) {
                break;
            }
        }
        if (ret > 0) {
            /* Ready to make a pseudo candidate */
            message->location->loct.current = i;
            message->location->loct.current_info = 0x10;
            message->location->cache_freq = 0;
            message->location->loct.status = NJ_ST_SEARCH_READY;
        } else {
            /* Unready to make a candidate */
            message->location->loct.status = NJ_ST_SEARCH_END;
        }
        return ret;

    case NJG_OP_SEARCH_NEXT:
        /* Lookup the next candidate */
        for (i = (NJ_UINT16)(message->location->loct.current + 1); i < EXGQWERTY_NUM; i++) {
            ret = can_make_giji(message->condition->yomi, message->condition->ylen, i,
                                &message->location->loct.top, &message->location->loct.bottom);
            if (ret > 0) {
                break;
            }
        }
        if (ret > 0) {
            /* Ready to make a pseudo candidate */
            message->location->loct.current = i;
            message->location->loct.current_info = 0x10;
            message->location->cache_freq = 0;
            message->location->loct.status = NJ_ST_SEARCH_READY;
        } else {
            /* Unready to make a candidate */
            message->location->loct.status = NJ_ST_SEARCH_END;
        }
        return ret;

    case NJG_OP_GET_WORD_INFO:
        /* Make a pseudo candidate by getting the type of a candidate */
        if (message->location->loct.current >= EXGQWERTY_NUM) {
            /* Unused root */
            /* Check the range of candidates as a precaution */
            return -1; /*NCH*/
        }

        ret = set_word_info(message->word->yomi, NJ_GET_YLEN_FROM_STEM(message->word),
                            &(message->location->loct), message->dicset->rHandle[0], &rlt);
        if (ret > 0) {
            /* Possible to make a pseudo candidate */
            *(message->word) = rlt.word;
            return 0;
        } else {
            /* Never be through here, but check errors as a precaution */
            return -1; /*NCH*/
        }

    case NJG_OP_GET_STROKE:
        /* Make a reading string */
        len = NJ_GET_YLEN_FROM_STEM(message->word);
        if (message->stroke_size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
            /* Return error, if there's no buffer */
            return -1;
        }
        nj_strncpy_ret = nj_strncpy(message->stroke, message->word->yomi, len);
        if(nj_strncpy_ret == NULL) {
            return -1;
        }
        message->stroke[len] = NJ_CHAR_NUL;
        return (NJ_INT16)len;

    case NJG_OP_GET_STRING:
        /* Make strings to display */
        ret = get_giji_candidate(message->word, message->string, (NJ_UINT16)(message->string_size / sizeof(NJ_CHAR)));
        return ret;

    case NJG_OP_GET_ADDITIONAL:
    case NJG_OP_LEARN:
    case NJG_OP_UNDO_LEARN:
    case NJG_OP_ADD_WORD:
    case NJG_OP_DEL_WORD:
        return 0;
    default:
        break;
    }
    return -1; /* Error */ /*NCH*/
}

static NJ_INT16 can_make_giji(NJ_CHAR* str, NJ_UINT16 len, NJ_UINT16 type,
                              NJ_UINT32* locTop, NJ_UINT32* locBottom) {
    NJ_CHAR   c;
    NJ_INT16  ret;
    NJ_UINT32 tmp;
    NJ_UINT16 i;
    NJ_UINT8  *byte;

    if( (str == NULL) || (locTop == NULL) || (locBottom == NULL) ) {
        return -1;
    }

    switch (type) {
    case EXGQWERTY_NO_CONV:
    case EXGQWERTY_LOWER:
    case EXGQWERTY_TOP_UPPER:
    case EXGQWERTY_UPPER:
        ret = 1;
        for (i = 0; i < len; i++) {
            c = str[i];
            byte = (NJ_UINT8*)&c;
            if ( (byte[0] != 0) ||
                 (((byte[1] < '0') || (byte[1] > '9')) && ((byte[1] < 'A') || (byte[1] > 'z')) &&
                   (byte[1] != '$') && (byte[1] != ' ') && (byte[1] != '.') && (byte[1] != ',') &&
                   (byte[1] != '-'))) {
                /* Invalid if a reading string contains non-alphanumeric */
                ret = 0;
                break;
            } 
        }
        return ret;

    case EXGQWERTY_MMDD_SLASH:
        if (len != 4) {
            /* Invalid unless a string length is 4 */
            return 0;
        }
        ret = 0;
        /* check month */
        byte = (NJ_UINT8*)&str[0];
        if ((byte[0] == 0) && (byte[1] == '1')) {
            byte = (NJ_UINT8*)&str[1];
            if ((byte[0] == 0) && (byte[1] >= '0') && (byte[1] <= '2')) {
                ret = (NJ_INT16)(10 + byte[1] - '0');
            } 
        } else if ((byte[0] == 0) && (byte[1] == '0')) {
            byte = (NJ_UINT8*)&str[1];
            if ((byte[0] == 0) && (byte[1] >= '1') && (byte[1] <= '9')) {
                ret = (NJ_INT16)(byte[1] - '0');
            } 
        } else {
            /* Do nothing */
        }

        if (ret == 0) {
            return 0;
        }
        *locTop = ret; /* Save the month info to loct.from */
        /* check day */
        byte = (NJ_UINT8*)&str[2];
        if ((byte[0] == 0) && (byte[1] >= '0') && (byte[1] <= '2')) {
            /* Return ok if the day is less than or equal to 29th */
            byte = (NJ_UINT8*)&str[3];
            if ((byte[0] == 0) && (byte[1] >= '1') && (byte[1] <= '9')) {
                /* Save the day info to loct.to */
                *locBottom = byte[1] - '0';
                byte = (NJ_UINT8*)&str[2];
                *locBottom += (byte[1] - '0') * 10;
                return 1;
            }
        } else if ((byte[0] == 0) && (byte[1] == '3') && (ret != 2)) {
            /* Check the day (greater than 29th), except Feb. */
            byte = (NJ_UINT8*)&str[3];
            if ((byte[0] == 0) && (byte[1] == '0')) {
                /* Check 30th, return OK unless it's Feb. */
                *locBottom = 30; /* Save the day info to loct.to */
                return 1;
            } else if ((byte[0] == 0) && (byte[1] == '1')) {
                if ((ret == 1) || (ret == 3) || (ret == 5) ||
                    (ret == 7) || (ret == 8) || (ret == 10) ||
                    (ret == 12)) {
                    /* Check 31th, return OK except Feb, Apr, Jun, Sep, and Nov. */
                    *locBottom = 31; /* Save the day info to loct.to */
                    return 1;
                }
            } else {
                /* Do nothing */
            }
        } else {
            /* Do nothing */
        }
        return 0;

    case EXGQWERTY_HHMM_COLON:
        if (len != 4) {
            /* Invalid unless a string length is 4 */
            return 0;
        }
        ret = 0;
        /* check hour */
        byte = (NJ_UINT8*)&str[0];
        if ((byte[0] == 0) && (byte[1] == '1')) {
            byte = (NJ_UINT8*)&str[1];
            if ((byte[0] == 0) && (byte[1] >= '0') && (byte[1] <= '9')) {
                ret = (NJ_INT16)(10 + byte[1] - '0');
            } 
        } else if ((byte[0] == 0) && (byte[1] == '0')) {
            byte = (NJ_UINT8*)&str[1];
            if ((byte[0] == 0) && (byte[1] >= '1') && (byte[1] <= '9')) {
                ret = (NJ_INT16)(byte[1] - '0');
            } 
        } else if ((byte[0] == 0) && (byte[1] == '2')) {
            byte = (NJ_UINT8*)&str[1];
            if ((byte[0] == 0) && (byte[1] >= '0') && (byte[1] <= '3')) {
                ret = (NJ_INT16)(20 + byte[1] - '0');
            } 
        } else {
            /* Do nothing */
        }

        if (ret == 0) {
            return 0;
        }
        *locTop = ret; /* Save the hour info to loct.from */
        /* check min */
        byte = (NJ_UINT8*)&str[2];
        if ((byte[0] == 0) && (byte[1] >= '0') && (byte[1] <= '5')) {
            /* Return ok if the min is less than or equal 59 */
            byte = (NJ_UINT8*)&str[3];
            if ((byte[0] == 0) && (byte[1] >= '0') && (byte[1] <= '9')) {
                *locBottom = byte[1] - '0';
                byte = (NJ_UINT8*)&str[2];
                *locBottom += (byte[1] - '0') * 10;
                return 1;
            }
        }
        return 0;

    case EXGQWERTY_DOLLER_YEN_J:
        byte = (NJ_UINT8*)&str[0];
        if ((byte[0] != 0) || (byte[1] != '$') || (len < 2)) {
            return 0;
        }
        if ((tmp = ex_atod(&str[1], (NJ_UINT16)(len - 1))) > 0) {
            tmp *= 102;
            *locTop = tmp;
            i = 0;
            while (tmp > 0) {
                i++;
                tmp /= 10;
            }
            *locBottom = i;
            return 1;
        }
        return 0;
        
    default:
        break;
    }
    return 0; /*NCH*/
}

static NJ_INT16 set_word_info(NJ_CHAR* yomi, NJ_UINT16 ylen,
                              NJ_SEARCH_LOCATION* loct, NJ_DIC_HANDLE rule, NJ_RESULT *giji) {
    NJ_UINT16 fpos, bpos;
    NJ_UINT16 len;

    if( (loct == NULL) || (giji== NULL) ) {
        return 0;
    }

    /* Initialize NJ_RESULT */
    giji->operation_id = (NJ_OP_CONVERT | NJ_FUNC_SEARCH);
    giji->word.yomi                  = NULL;
    giji->word.stem.info1            = 0;
    giji->word.stem.info2            = 0;
    giji->word.stem.hindo            = 0;
    giji->word.stem.type             = NJ_TYPE_UNDEFINE;
    giji->word.fzk.info1             = 0;
    giji->word.fzk.info2             = 0;
    giji->word.fzk.hindo             = 0;

    giji->word.stem.loc = *loct;

    giji->word.yomi = yomi;
    NJ_SET_YLEN_TO_STEM(&giji->word, ylen);
    NJ_SET_FREQ_TO_STEM(&giji->word, 10);

    /* Forward lexical category: get the number of lexical category for a pseudo candidate */
    fpos = njd_r_get_hinsi(rule, NJ_HINSI_GIJI_F);
    /* Backword lexical category: get the number of lexical category for a pseudo candidate */
    bpos = njd_r_get_hinsi(rule, NJ_HINSI_GIJI_B);

    switch (loct->current) {
    case EXGQWERTY_NO_CONV:
    case EXGQWERTY_LOWER:
    case EXGQWERTY_TOP_UPPER:
    case EXGQWERTY_UPPER:
        NJ_SET_KLEN_TO_STEM(&giji->word, ylen);
        NJ_SET_FPOS_TO_STEM(&giji->word, fpos);
        NJ_SET_BPOS_TO_STEM(&giji->word, bpos);
        return 1;

    case EXGQWERTY_MMDD_SLASH:
        len = (NJ_UINT16)(ylen + 1); /* The number of slashes + 1 */
        if (loct->top < 10) {
            /* When the month is less than 10 */
            len--;
        }
        if (loct->bottom < 10) {
            /* When the day is less than 10 */
            len--;
        }
        NJ_SET_KLEN_TO_STEM(&giji->word, len);
        NJ_SET_FPOS_TO_STEM(&giji->word, fpos);
        NJ_SET_BPOS_TO_STEM(&giji->word, bpos);
        return 1;

    case EXGQWERTY_HHMM_COLON:
        len = (NJ_UINT16)(ylen + 1); /* The number of colons + 1 */
        if (loct->top < 10) {
            /* When the hour is less than 10 */
            len--;
        }
        NJ_SET_KLEN_TO_STEM(&giji->word, len);
        NJ_SET_FPOS_TO_STEM(&giji->word, fpos);
        NJ_SET_BPOS_TO_STEM(&giji->word, bpos);
        return 1;
        
    case EXGQWERTY_DOLLER_YEN_J:
        len = (NJ_UINT16)loct->bottom;
        NJ_SET_KLEN_TO_STEM(&giji->word, len + 1); /* The number of YEN + 1 */
        NJ_SET_FPOS_TO_STEM(&giji->word, fpos);
        NJ_SET_BPOS_TO_STEM(&giji->word, bpos);
        NJ_SET_FREQ_TO_STEM(&giji->word, 0);
        return 1;

    default:
        break;
    }
    return 0; /*NCH*/
}

static NJ_INT16 get_giji_candidate(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 olen) {
    NJ_UINT16 len;
    NJ_UINT16 i;
    NJ_CHAR   c;
    NJ_CHAR   pc;
    NJ_UINT16 type;
    NJ_UINT32 tmp;
    NJ_UINT8 *byte;
    NJ_CHAR  *nj_strncpy_ret;

    if( (word == NULL) || (candidate == NULL) ) {
        return -1;
    }

    type = (NJ_UINT16)(word->stem.loc.current);
    switch (type) {
    case EXGQWERTY_NO_CONV:
        len = NJ_GET_YLEN_FROM_STEM(word);
        if ((len + NJ_TERM_LEN) > olen) {
            return -1;
        }
        nj_strncpy_ret = nj_strncpy(candidate, word->yomi, len);
        if(nj_strncpy_ret == NULL) {
            return -1;
        }

        *(candidate + len) = NJ_CHAR_NUL;
        return len;

    case EXGQWERTY_LOWER:
        len = NJ_GET_YLEN_FROM_STEM(word);
        if ((len + NJ_TERM_LEN) > olen) {
            return -1;
        }
        for (i = 0; i < len; i++) {
            c = word->yomi[i];
            byte = (NJ_UINT8*)&c;
            if ((byte[0] == 0) && (byte[1] >= 'A') && (byte[1] <= 'Z')) {
                byte[1] = (NJ_UINT8)(byte[1] - 'A' + 'a');
            }
            candidate[i] = c;
        }
        candidate[len] = NJ_CHAR_NUL;
        return len;

    case EXGQWERTY_TOP_UPPER:
        len = NJ_GET_YLEN_FROM_STEM(word);
        if ((len + NJ_TERM_LEN) > olen) {
            return -1;
        }
        pc = ' ';
        for (i = 0; i < len; i++) {
            c = word->yomi[i];
            byte = (NJ_UINT8*)&c;
            if (pc == ' ') {
                if ((byte[0] == 0) && (byte[1] >= 'a') && (byte[1] <= 'z')) {
                    byte[1] = (NJ_UINT8)(byte[1] - 'a' + 'A');
                }
            } else {
                if ((byte[0] == 0) && (byte[1] >= 'A') && (byte[1] <= 'Z')) {
                    byte[1] = (NJ_UINT8)(byte[1] - 'A' + 'a');
                }
            }
            candidate[i] = c;
            pc = c;
        }
        candidate[len] = NJ_CHAR_NUL;
        return len;
        
    case EXGQWERTY_UPPER:
        len = NJ_GET_YLEN_FROM_STEM(word);
        if ((len + NJ_TERM_LEN) > olen) {
            return -1;
        }
        for (i = 0; i < len; i++) {
            c = word->yomi[i];
            byte = (NJ_UINT8*)&c;
            if ((byte[0] == 0) && (byte[1] >= 'a') && (byte[1] <= 'z')) {
                byte[1] = (NJ_UINT8)(byte[1] - 'a' + 'A');
            }
            candidate[i] = c;
        }
        candidate[len] = NJ_CHAR_NUL;
        return len;
        
    case EXGQWERTY_MMDD_SLASH:
        len = NJ_GET_KLEN_FROM_STEM(word);
        if ((NJ_GET_YLEN_FROM_STEM(word) != 4) || ((len + NJ_TERM_LEN) > olen)) {
            return -1;
        }
        i = 0;
        byte = (NJ_UINT8*)&word->yomi[0];
        if ((byte[0] == 0) && (byte[1] == '0')) {
            candidate[i] = word->yomi[1]; i++;
        } else {
            candidate[i] = word->yomi[0]; i++;
            candidate[i] = word->yomi[1]; i++;
        }
        byte = (NJ_UINT8*)&candidate[i];
        byte[0] = 0x00;
        byte[1] = '/';
        i++;
        byte = (NJ_UINT8*)&word->yomi[2];
        if ((byte[0] == 0) && (byte[1] == '0')) {
            candidate[i] = word->yomi[3]; i++;
        } else {
            candidate[i] = word->yomi[2]; i++;
            candidate[i] = word->yomi[3]; i++;
        }
        candidate[i] = NJ_CHAR_NUL;
        return len;

    case EXGQWERTY_HHMM_COLON:
        len = NJ_GET_KLEN_FROM_STEM(word);
        if ((NJ_GET_YLEN_FROM_STEM(word) != 4) || ((len + NJ_TERM_LEN) > olen)) {
            return -1;
        }
        i = 0;
        byte = (NJ_UINT8*)&word->yomi[0];
        if ((byte[0] == 0) && (byte[1] == '0')) {
            candidate[i] = word->yomi[1]; i++;
        } else {
            candidate[i] = word->yomi[0]; i++;
            candidate[i] = word->yomi[1]; i++;
        }
        byte = (NJ_UINT8*)&candidate[i];
        byte[0] = 0x00;
        byte[1] = ':';
        i++;
        candidate[i] = word->yomi[2]; i++;
        candidate[i] = word->yomi[3]; i++;
        candidate[i] = NJ_CHAR_NUL;
        return len;

    case EXGQWERTY_DOLLER_YEN_J:
        len = NJ_GET_KLEN_FROM_STEM(word);
        if ((len + NJ_TERM_LEN) > olen) {
            return -1;
        }
        tmp = word->stem.loc.top;
        for (i = (NJ_UINT16)word->stem.loc.bottom; i > 0; i--) {
            byte = (NJ_UINT8*)&candidate[i - 1];
            byte[0] = 0;
            byte[1] = (NJ_UINT8)('0' + (tmp % 10));
            tmp /= 10;
        }
        byte = (NJ_UINT8*)&candidate[word->stem.loc.bottom];
        byte[0] = 0x51;
        byte[1] = 0x86;
        candidate[word->stem.loc.bottom + 1] = NJ_CHAR_NUL;

        return len;

    default:
        break;
    }

    return -1;
}

static NJ_UINT32 ex_atod(NJ_CHAR *str, NJ_UINT16 len)
{
    NJ_UINT16 i;
    NJ_UINT32 ret = 0;
    NJ_UINT8 *byte;

    if(str == NULL) {
        return 0;
    }

    for (i = 0; i < len; i++) {
        byte = (NJ_UINT8*)&str[i];
        if ((byte[0] == 0) && (byte[1] >= '0') && (byte[1] <= '9')) {
            ret *= 10;
            ret += byte[1] - '0';
        } else {
            return 0;
        }
    }
    return ret;
}
