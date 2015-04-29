/**
 * @file
 *   Uicode Converter.
 *
 * @author
 *   OMRON SOFTWARE Co., Ltd.
 *
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 * (C) Copyright OMRON SOFTWARE Co., Ltd. 2009 All Rights Reserved
 */
#include "uniconv.h"

#define NULL (void*)0

#define STR_TO_UINT16(s)                                         \
    (unsigned int)(((*(s)) << 8) | (*((s)+1)))


#define UTF16_TO_STR(utf16, str)                                 \
    {                                                            \
        (str)[0] = (unsigned char)(((utf16) >>  8) & 0xff);      \
        (str)[1] = (unsigned char)(((utf16)      ) & 0xff);      \
    }

#define UTF8_TO_STR(utf8, str)                                   \
    {                                                            \
        (str)[0] = (unsigned char)(((utf8)      ) & 0xff);       \
    }


unsigned char* UTF16BE_to_ModUTF8(unsigned char *src, unsigned char *dst, int dst_size)
{
    unsigned int utf8;
    unsigned int utf16;

    /* null check */
    if ((src == NULL) || (dst == NULL)) {
        return NULL;
    }

    /*
     * convert utf16 string to Modified utf8 string
     */
    utf16 = STR_TO_UINT16(src);
    while (utf16 != 0) {
        if (utf16 <= 0x0000007F) {
            /* 1 byte char */
            /* check size */
            dst_size -= 1;
            if (dst_size < 0) {
                /* error if the buffer overrun */
                return NULL;
            }
            utf8 = utf16;
            UTF8_TO_STR(utf8, dst);
            dst++;
        } else if (utf16 <= 0x000007FF) {
            /* 2 byte char */
            /* check size */
            dst_size -= 2;
            if (dst_size < 0) {
                /* error if the buffer overrun */
                return NULL;
            }
            utf8 = ((utf16 >> 6) & 0x1F) | 0xC0;
            UTF8_TO_STR(utf8, dst);
            dst++;
            utf8 = ((utf16     ) & 0x3F) | 0x80;
            UTF8_TO_STR(utf8, dst);
            dst++;
        } else if (utf16 <= 0x0000FFFF) {
            /* 3 byte char */
            /* check size */
            dst_size -= 3;
            if (dst_size < 0) {
                /* error if the buffer overrun */
                return NULL;
            }
            utf8 = ((utf16 >> 12) & 0x0F) | 0xE0;
            UTF8_TO_STR(utf8, dst);
            dst++;
            utf8 = ((utf16 >>  6) & 0x3F) | 0x80;
            UTF8_TO_STR(utf8, dst);
            dst++;
            utf8 = ((utf16      ) & 0x3F) | 0x80;
            UTF8_TO_STR(utf8, dst);
            dst++;
        } else {
            /* Do nothing */
        }
        /* move the src pointer & get utf16 value */
        src += 2;
        utf16 = STR_TO_UINT16(src);
    }

    /* check size */
    dst_size -= 1;
    if (dst_size < 0) {
        /* error if the buffer overrun */
        return NULL;
    }
    /* write a terminator */
    UTF8_TO_STR(0x00, dst);

    return dst;
}

unsigned char* UTF8_to_UTF16BE(unsigned char *src, unsigned char *dst, int dst_size)
{
    unsigned char* utf8;
    unsigned int   utf16 = 0;

    /* null check */
    if ((src == NULL) || (dst == NULL)) {
        return NULL;
    }

    /*
     * convert Modified utf8 string to utf16 string
     */
    utf8 = src;
    while (*utf8 != 0x00) {
        if ((*utf8 & 0x80) != 0) {
            if ((*utf8 & 0xE0) == 0xC0) {
                /* 2 bytes char */
                utf16 = (unsigned int)((*utf8 & 0x1F) << 6);
                utf8++;
                utf16 |= (*utf8 & 0x3F);
                utf8++;
            } else if ((*utf8 & 0xF0) == 0xE0) {
                /* 3 bytes char */
                utf16 = (unsigned int)((*utf8 & 0x0F) << 12);
                utf8++;
                utf16 |= (unsigned int)((*utf8 & 0x3F) << 6);
                utf8++;
                utf16 |= (*utf8 & 0x3F);
                utf8++;
            } else {
                /* Do nothing */
            }
        } else {
            /* 1 byte char */
            utf16 = (*utf8 & 0x7F);
            utf8++;
        }

        /* check size */
        dst_size -= 2;
        if (dst_size < 0) {
            /* error if the buffer overrun */
            return NULL;
        }
        /* write a utf16 character & move the dst pointer */
        UTF16_TO_STR(utf16, dst);
        dst += 2;
    }

    /* check size */
    dst_size -= 2;
    if (dst_size < 0) {
        /* error if the buffer overrun */
        return NULL;
    }
    /* write a terminator */
    UTF16_TO_STR(0x0000, dst);

    return dst;
}
