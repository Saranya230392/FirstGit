/**
 * @file
 *   wlf_engine.h
 *
 * @brief  Wnn Language Framework Main Function Header
 *
 * @author
 *   OMRON SOFTWARE Co., Ltd.
 *
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 * (C) Copyright OMRON SOFTWARE Co., Ltd. 2009 All Rights Reserved
 */
#ifndef _WLF_ENGINE_H_
#define _WLF_ENGINE_H_

#ifdef __cplusplus
   extern "C" {
#endif


/***********************************************************/
/*                    DEFINES                              */
/***********************************************************/

/** Type definitions */
typedef signed char    WLF_INT8;
typedef signed short   WLF_INT16;
typedef signed long    WLF_INT32;

typedef unsigned char  WLF_UINT8;
typedef unsigned short WLF_UINT16;
typedef unsigned long  WLF_UINT32;

/** NULL character definition */
#define WLF_CHAR_NUL  0x0000

#define WLF_GIJI_JP         0       /**< Japanese           */
#define WLF_GIJI_OTHERS     1       /**< Others             */

/** Language type definitions */
#define WLF_LANG_TYPE_NONE    255       /**< None               */
#define WLF_LANG_TYPE_JP        0       /**< Japanese           */
#define WLF_LANG_TYPE_USUK      1       /**< English(USUK)      */
#define WLF_LANG_TYPE_ZHCN     14       /**< Chinese (PRC)      */
#define WLF_LANG_TYPE_ZHTW     15       /**< Chinese (TW)       */
#define WLF_LANG_TYPE_KOREAN   18       /**< Korean             */
#define WLF_LANG_TYPE_USE_MAX   3       /**< Maximum number of languages */

/** Dictionary type */
#define WLF_DICSET_TYPE_NONE       -1   /**< None           */
#define WLF_DICSET_TYPE_NORMAL      0   /**< Normal         */
#define WLF_DICSET_TYPE_EISUKANA    1   /**< EISU-KANA      */
#define WLF_DICSET_TYPE_KAOMOJI     2   /**< KAOMOJI        */
#define WLF_DICSET_TYPE_JINMEI      3   /**< Person's name  */
#define WLF_DICSET_TYPE_POSTAL_ADDRESS  4   /**< Postal code   */
#define WLF_DICSET_TYPE_EMAIL_ADDRESS   5   /**< Email address */
#define WLF_DICSET_TYPE_USER_DIC   10   /**< User           */
#define WLF_DICSET_TYPE_LEARN_DIC  11   /**< Learning       */
#define WLF_DICSET_TYPE_AUTOLEARN_DIC  45   /**< Auto Learning       */

/** Keypad Type */
#define WLF_KEY_TYPE_NONE     (255)  /**< None               */
#define WLF_KEY_TYPE_KEYPAD12 (0)    /**< 12 KeyPad          */
#define WLF_KEY_TYPE_FULL     (1)    /**< QWERTY Keypad      */

/** Dictionary type definitions */
#define WLF_DIC_TYPE_NONE     255    /**< None               */
#define WLF_DIC_TYPE_NORMAL     0    /**< Normal             */
#define WLF_DIC_TYPE_EISUKANA   1    /**< EISU-KANA          */
#define WLF_DIC_TYPE_KAOMOJI    2    /**< KAOMOJI            */

/** Character type definition */
#define WLF_CHAR_TYPE_UTF16LE    0   /**< UTF16LE            */
#define WLF_CHAR_TYPE_UTF16BE    1   /**< UTF16BE            */
#define WLF_CHAR_TYPE_UTF8       2   /**< UTF8               */

/** Mode definition */
#define WLF_MODE_INIT          255   /**< Initial            */
#define WLF_MODE_PREDICTION      0   /**< Prediction         */
#define WLF_MODE_CONVERSION      1   /**< Consecutive conv   */
#define WLF_MODE_EISUKANA        2   /**< EISU-KANA          */

/** File open mode */
#define WLF_DIC_OPEN_CREAT  0        /**< Create file (when it does not exist)       */
#define WLF_DIC_OPEN_EXCL   1        /**< Create file (when it would possibly exist) */
#define WLF_DIC_OPEN_HARD   2        /**< Open file (Read only)                      */

/** Dictionary type */
#define WLF_DIC_TYPE_LEARN         2    /**< Learning dictionary  */
#define WLF_DIC_TYPE_USER          3    /**< User dictionary      */
#define WLF_DIC_TYPE_GIJI          5    /**< Pseudo dictionary    */
#define WLF_DIC_TYPE_DECOEMOJI     8    /**< Decoemoji dictionary */
#define WLF_DIC_TYPE_HRL_GIJI     14    /**< Program dictionary(ex_hrl giji) */
#define WLF_DIC_TYPE_AUTOLEARN    31    /**< Auto Learning dictionary    */
#define WLF_DIC_TYPE_DOWNLOAD     32    /**< Download dictionary    */
#define WLF_DIC_TYPE_EXT_DIC     255    /**< Frequency dictionary */

#define WLF_MODE_GIJI_OFF   0        /**< Make pseudo candidates OFF */
#define WLF_MODE_GIJI_ON    1        /**< Make pseudo candidates ON  */

#define WLF_MODE_HUNHUN_OFF 0        /**< Wildcard suggestion ON  */
#define WLF_MODE_HUNHUN_ON  1        /**< Wildcard suggestion OFF */

/** Candidate type */
#define WLF_STR_TYPE_CAND   0        /**< Candidates list   */
#define WLF_STR_TYPE_SEG    1        /**< Clauses           */
#define WLF_STR_TYPE_WORD   2        /**< Words             */

/** (Lexical) category type */
#define WLF_HINSI_MEISI     0        /**< Noun               */
#define WLF_HINSI_JINMEI    1        /**< Name               */
#define WLF_HINSI_CHIMEI    2        /**< Place name         */
#define WLF_HINSI_KIGOU     3        /**< Symbol             */

/** Way to search */
#define WLF_CUR_OP_COMP     0        /**< Exact matching     */
#define WLF_CUR_OP_FORE     1        /**< Prefix search      */

/** Search mode */
#define WLF_CUR_MODE_FREQ   0        /**< by Frequency       */
#define WLF_CUR_MODE_YOMI   1        /**< by Reading         */

/** Display duplicate data */
#define WLF_DUPLICATE_OFF   0        /**< Hide               */
#define WLF_DUPLICATE_ON    1        /**< Show               */

/** Reconvert clause's length */
#define WLF_SEGMENT_SHORT   0        /**< Shorten a length   */
#define WLF_SEGMENT_EXTEND  1        /**< Lengthen a length  */

/** Flexible Search */
#define WLF_FLEXIBLE_SEARCH_OFF (0)  /**< Flexible Search Disabling */
#define WLF_FLEXIBLE_SEARCH_ON  (1)  /**< Flexible Search Enabling  */

/** Time structure */
typedef struct {
    WLF_UINT16 year;
    WLF_UINT8  month;
    WLF_UINT8  day;
    WLF_UINT8  hour;
    WLF_UINT8  min;
} WLF_TIME;

/***********************************************************/
/*                     extern                              */
/***********************************************************/
/**
 * Definition of externs
 */
#define WLF_EXTERN extern
/*
#define WLF_EXTERN extern __declspec(dllexport)
*/

/*
 * (Note) Use the definition below, when you make a Windows' DLL
 * #define WLF_EXTERN extern __declspec(dllexport)
 */

/** Get the size of WLF_CLASS */
WLF_EXTERN WLF_UINT32 wlf_get_init_size(void);

/** Initialize */
WLF_EXTERN WLF_INT16 wlf_initialize(WLF_UINT8 *wlf_class, WLF_UINT8 endian);

/** Finalize */
WLF_EXTERN void wlf_terminate(WLF_UINT8 *wlf_class);

/** Load Language Settings */
WLF_EXTERN WLF_INT16 wlf_load_lang(WLF_UINT8 *wlf_class, WLF_UINT8 lang);

/** Save language *//*Not Implemented*/
WLF_EXTERN WLF_INT16 wlf_save_lang(WLF_UINT8 *wlf_class, WLF_UINT8 lang);

/** Set language */
WLF_EXTERN WLF_INT16 wlf_set_lang(WLF_UINT8 *wlf_class, WLF_UINT8 lang);

/** Set time *//*Not Implemented*/
WLF_EXTERN WLF_INT16 wlf_set_time(WLF_UINT8 *wlf_class, WLF_TIME time);

/** Disable relational learning and advanced searching */
WLF_EXTERN WLF_INT16 wlf_clear_relation(WLF_UINT8 *wlf_class);

/** Set a flexible searching */
WLF_EXTERN WLF_INT16 wlf_set_flexible_search(WLF_UINT8 *wlf_class, WLF_UINT16 flexible_search);

/** Get a result of consecutive clause conversion */
WLF_EXTERN WLF_INT16 wlf_get_conversion(WLF_UINT8 *wlf_class, WLF_UINT8 mode, WLF_UINT8 *stroke, WLF_UINT8 devide_pos, WLF_UINT8 stroke_min, WLF_UINT8 stroke_max);

/** Get a candidate string */
WLF_EXTERN WLF_INT16 wlf_get_string(WLF_UINT8 *wlf_class, WLF_UINT16 seg_pos, WLF_UINT16 index, WLF_UINT8 *buf, WLF_UINT16 size);

/** Get a reading string */
WLF_INT16 wlf_get_stroke(WLF_UINT8  *wlf_class, WLF_UINT16 seg_pos, WLF_UINT16 index, WLF_UINT8 *buf, WLF_UINT16 size);

/** Clear leaning and user dictionary *//*Not Implemented*/
WLF_EXTERN WLF_INT16 wlf_clear_dic(WLF_UINT8 *, WLF_UINT16);

/** Save a dictionary *//*Not Implemented*/
WLF_EXTERN WLF_INT16 wlf_save_dic(WLF_UINT8 *, WLF_UINT16);

/** Learn strings */
WLF_EXTERN WLF_INT16 wlf_learn_string(WLF_UINT8 *, WLF_UINT16, WLF_UINT16);

/** Learn noconversion *//*Not Implemented*/
WLF_EXTERN WLF_INT16 wlf_learn_noconversion(WLF_UINT8 *, WLF_UINT8 *);

/** Search words *//*not Implemented*/
WLF_EXTERN WLF_INT16 wlf_search_word(WLF_UINT8 *, WLF_UINT8 *, WLF_UINT16, WLF_UINT16, WLF_UINT16, WLF_UINT16);

/** Add words *//*Not Implemented*/
WLF_EXTERN WLF_INT16 wlf_add_word(WLF_UINT8 *, WLF_UINT8 *, WLF_UINT8 *, WLF_UINT16, WLF_UINT16);

/** Delete words *//*Not Implemented*/
WLF_EXTERN WLF_INT16 wlf_delete_word(WLF_UINT8 *, WLF_UINT16, WLF_UINT16);

#ifdef __cplusplus
   }
#endif

#endif /* _WLF_ENGINE_H_ */
