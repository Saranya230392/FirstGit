/**
 * @file
 *   Uicode Converter definition file.
 *
 * @author
 *   OMRON SOFTWARE Co., Ltd.
 *
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 * (C) Copyright OMRON SOFTWARE Co., Ltd. 2009 All Rights Reserved
 */
#ifndef _UNICONV_H_
#define _UNICONV_H_

extern unsigned char* UTF16BE_to_ModUTF8(unsigned char *src, unsigned char *dst, int dst_size);
extern unsigned char* UTF8_to_UTF16BE(unsigned char *src, unsigned char *dst, int dst_size);

#endif /* _UNICONV_H_ */
