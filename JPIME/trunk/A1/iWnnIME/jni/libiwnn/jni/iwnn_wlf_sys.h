/**
 * @file
 *   wlf_sys.h
 *
 * @brief  Wnn Language Framework System Function Header
 *
 * @author
 *   OMRON SOFTWARE Co., Ltd.
 *
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 * (C) Copyright OMRON SOFTWARE Co., Ltd. 2009 All Rights Reserved
 */
#ifndef _WLF_SYS_H_
#define _WLF_SYS_H_

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "iwnn_wlf_engine.h"

#ifdef __cplusplus
   extern "C" {
#endif

/****************************************************************************/
/*                           DEFINES                                        */
/****************************************************************************/

/** Typedefine */
typedef FILE    WLF_FILE;


#define AFW_FILENAME_MAX        256               /**< Maximum file name length */

/* File position */
#define WLF_SEEK_END            SEEK_END          /**< End                 */
#define WLF_SEEK_CUR            SEEK_CUR          /**< Current             */
#define WLF_SEEK_SET            SEEK_SET          /**< Top                 */



/****************************************************************************/
/*                         Prototypes                                       */
/****************************************************************************/

/* Memory Operation */
void        wlf_memset(void *, WLF_INT32, WLF_UINT32);
void*       wlf_memcpy(void *, void *, WLF_UINT32);
void*       wlf_malloc(WLF_UINT32);
void        wlf_free(void *);

/* File Operation */
WLF_FILE*   wlf_fopen(const WLF_INT8 *, const WLF_INT8 *);
WLF_INT32   wlf_fseek(WLF_FILE *, WLF_INT32, WLF_INT32);
WLF_INT32   wlf_ftell(WLF_FILE *);
WLF_INT32   wlf_fread(void *, WLF_INT32, WLF_INT32, WLF_FILE *);
WLF_INT32   wlf_fwrite(void *, WLF_INT32, WLF_INT32, WLF_FILE *);
void        wlf_fclose(WLF_FILE *);

/* String Operation */
WLF_INT8*   wlf_strcpy(WLF_INT8 *, const WLF_INT8 *);
WLF_INT8*   wlf_strcat(WLF_INT8 *, const WLF_INT8 *);
WLF_INT32   wlf_strlen(WLF_INT8 *);
WLF_INT32   wlf_strcmp(WLF_INT8 *s1, WLF_INT8 *s2);

#ifdef __cplusplus
   }
#endif


#endif /* _WLF_SYS_H_ */
