/**
 * @file
 *   wlf_com.h
 *
 * @brief  Wnn Language Framework Common Header file
 *
 * @author
 *   OMRON SOFTWARE Co., Ltd.
 *
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 * (C) Copyright OMRON SOFTWARE Co., Ltd. 2009 All Rights Reserved
 */
#ifndef _WLF_COM_H_
#define _WLF_COM_H_

#ifndef _NJ_LIB_H_
#include "nj_lib.h"
#define _NJ_LIB_H_
#endif /* _NJ_LIB_H_ */
#include "ex_giji.h"
#include "ex_predg.h"

#include "iwnn_utils.h"
#include "demoji_lib.h"

/****************************************************************************/
/*                           DEFINES                                        */
/****************************************************************************/
#define WLF_LEARN_MAX_CNT   2000     /**< Maximum number of learning dictionaries */
#define WLF_LEARN_AREA_SIZE   42     /**< Learning area size by a word */

#define WLF_LEARN_DIC_SIZE ( NJ_LEARN_DIC_HEADER_SIZE +    \
                             ( WLF_LEARN_AREA_SIZE *       \
                               WLF_LEARN_MAX_CNT ) )
                                     /**< Size of a learning dictionary */

#define WLF_DICSET_MAIN       0      /**< Dictionary set for prediction */
#define WLF_DICSET_SUB        1      /**< The others                    */

/* Definition for pseudo candidates  */
#define WLF_GIJISET_PREDICT   0      /**< Pseudo set for prediction     */
#define WLF_GIJISET_CONVERT   1      /**< Pseudo set for conversion     */
#define WLF_GIJISET_EISUKANA  2      /**< Pseudo set for EISU-KANA      */
#define WLF_GIJISET_MAX       3      /**< Maximum number of pseudo set  */

/* Pseudo dictionary filter */
#define WLF_GIJI_FILTER_NONE  0      /**< None (Initialize)             */

/* Option setting for iWnn Engine */
#define WLF_OPTION_RENBUN     10     /**< Threshold value for candidates*/
                                     /**< to start the consecutive clause conversion automatically */

/* Definition for dictionary files */
#define WLF_DIC_DIR         "dic"    /**< Dictionary folder             */
#define WLF_LANG_DIR_JP     "JP"     /**< Japanese                      */
#define WLF_LANG_DIR_EN     "EN"     /**< English                       */
#define WLF_LANG_DIR_CN     "CN"     /**< Chinese(Simplified)           */
#define WLF_LANG_DIR_TW     "TW"     /**< Chinese(Traditional)          */
#define WLF_LANG_DIR_KO     "KO"     /**< Korean                        */

/* Definition for dictionary file names */
#define WLF_RULE_DIC_FNAME      "njcon.a"     /**< Rule dictionary      */
#define WLF_UBASE1_DIC_FNAME    "njubase1.a"  /**< ubase1 dictionary    */
#define WLF_UBASE2_DIC_FNAME    "njubase2.a"  /**< ubase2 dictionary    */
#define WLF_FUZOKU_DIC_FNAME    "njfzk.a"     /**< fzk dictionary       */
#define WLF_TANKAN_DIC_FNAME    "njtan.a"     /**< Simple Kanji dictionary */
#define WLF_NOYOMI_DIC_FNAME    "njyomi.a"    /**< Prediction without readings dictionary */
#define WLF_LEARN_DIC_FNAME     "njuserl.a"   /**< Learning dictionary  */
#define WLF_USER_DIC_FNAME      "njuserw.a"   /**< User dictionary      */

/* Definition for the automatic dictionary restoration */
#define WLF_DIC_RESTORE_OFF     0    /**< OFF                           */
#define WLF_DIC_RESTORE_ON      1    /**< ON                            */

/* Definition for caches */
#define WLF_DIC_SRHCACHE_OFF    0    /**< No cache                      */
#define WLF_DIC_SRHCACHE_ON     1    /**< Some cache in use             */

/* Definition for conditions of the prediction */
#define WLF_SET_FORECAST_LEARN    15   /**< Maximum number of candidates for the leaning dictionary */
#define WLF_SET_FORECAST_LIMIT    50   /**< Maximum number of prediction candidates */
#define WLF_SET_FORECAST_LIMITMAX 100  /**< Maximum number of prediction candidates */

/* Definition for connection leaning flag */
#define WLF_CONNECT_OFF         0    /**< OFF                           */
#define WLF_CONNECT_ON          1    /**< ON                            */

/* Definition for relational searching flag */
#define WLF_RELATION_OFF        0    /**< OFF                           */
#define WLF_RELATION_ON         1    /**< ON                            */

/* Definition for Morphological Analysis */
#define WLF_MM_SELECT_SEGMENT   0    /**< Learn selected segments       */
#define WLF_MM_SELECT_STEM      1    /**< Learn selected word stems     */

/* Definition for head conversion flag */
#define WLF_HEAD_CONVERSION_OFF 0    /**< OFF                           */
#define WLF_HEAD_CONVERSION_ON  1    /**< ON                            */

#define WLF_GIJI_MAX_CANDIDATE  30   /**< Maximum number of pseudo candidates */

#define WLF_WORD_STROKE         0    /**< Reading of a word             */
#define WLF_WORD_CANDIDATE      1    /**< Candidate of a word           */

/*
 * Definition for registering words to a dictionary
 */
#define WLF_ADD_WORD_DIC_TYPE_USER  0       /**< for a User dictionary  */
#define WLF_ADD_WORD_DIC_TYPE_LEARN 1       /**< for a learning dictionary */
#define WLF_ADD_WORD_DIC_TYPE_PROGRAM 2     /**< for a pseudo dictionary */
#define WLF_ADD_WORD_DIC_TYPE_ADDITIONAL 20    /**< for a additional dictionary */

#define WLF_CAT_TIME_NONE       0
#define WLF_CAT_TIME_MORNING    1
#define WLF_CAT_TIME_NOON       2
#define WLF_CAT_TIME_NIGHT      3

#define WLF_CAT_SEASON_NONE     0
#define WLF_CAT_SEASON_SPRING   1
#define WLF_CAT_SEASON_SUMMER   2
#define WLF_CAT_SEASON_AUTUMN   3
#define WLF_CAT_SEASON_WINTER   4

#define WLF_FILE_PATH_LEN     256   /**< Maximum length of a file path  */

#define WLF_EMOJI_FILTER            0x01    /* Filter for EMOJI         */
#define WLF_EMAIL_ADDRESS_FILTER    0x02    /* Filter for email address */
#define WLF_DECOEMOJI_FILTER        0x04    /* Filter for DECOEMOJI     */

/* Definition for Errors */
#define WLF_ERR_INVALID_PARAM     -1   /**< Invalid parameter           */
#define WLF_ERR_NO_MEMORY         -2   /**< No memory                   */
#define WLF_ERR_FAILED_FILE_OPEN  -3   /**< Open file failure           */
#define WLF_ERR_FAILED_FILE_SEEK  -4   /**< Seek file failure           */
#define WLF_ERR_FAILED_FILE_READ  -5   /**< Read file failure           */
#define WLF_ERR_FAILED_FILE_WRITE -6   /**< Write file failure          */
#define WLF_ERR_FAILED_DIC_MOUNT  -7   /**< Mount dictionary failure    */
#define WLF_ERR_DIC_NO_MOUNT      -8   /**< No dictionary               */
#define WLF_ERR_INVALID_RESULT    -9   /**< Invalid data                */
#define WLF_ERR_NO_DATA           -10  /**< No data                     */
#define WLF_ERR_SAME_WORD         -11  /**< Duplicate words             */
#define WLF_ERR_USER_DIC_FULL     -12  /**< Dictionary full             */

#define IWNN_ADD_DIC_DICSET_TOPINDEX 35
#define IWNN_ADD_DIC_MAX 10
#define IWNN_ADD_DIC_PATH "%s/dicset/additional/add_dic_%03d_%03d.so"
#define IWNN_AL_DIC_PATH "%s/dicset/autolearning/al_dic_%03d_%03d.so"

#define IWNN_AL_DIC_DICSET_TOPINDEX 45
#define IWNN_AL_DIC_MAX 1
#define IWNN_DL_DIC_DICSET_TOPINDEX 46
#define IWNN_DL_DIC_MAX 10
#define IWNN_DL_DIC_HEADER_SIZE 128

/****************************************************************************/
/* Definition for structures                                                */
/****************************************************************************/


/**
 * @brief  Prediction Information
 */
typedef struct {
    NJ_UINT16    mode;                  /**< Prediction mode            */
    NJ_UINT16    forecast_learn_limit;  /**< Number of prediction candidates by a learning dictionary   */
    NJ_UINT16    forecast_limit;        /**< Number of prediction candidates by a compressed dictionary */
    NJ_UINT16    flexible_search;       /**< Flag for flexible searching */
    NJ_UINT8     stroke_min;            /**< Minimum length of a reading string */
    NJ_UINT8     stroke_max;            /**< Maximum length of a reading string */
    NJ_UINT8     in_divide_pos;         /**< Position of specified dividing clause */
    NJ_UINT8     out_divide_pos;        /**< Position result of dividing clause */
} WLF_PREDICT;

/**
 * @brief  Conversion Information
 */
typedef struct {
    NJ_UINT8     devide_pos;            /**< Position of dividing clause */
    NJ_UINT8     fix_seg_cnt;           /**< Number of committed clauses */
    NJ_UINT8     fix_stroke_len;        /**< Number of committed characters */
} WLF_CONV;

/**
 * @brief  Candidate's Information
 */
typedef struct {
    NJ_RESULT    candidate[NJ_MAX_CANDIDATE]; /**< Result structure (for candidate lists) */
    NJ_UINT16    candidate_cnt;               /**< Number of Result structures (for candidate lists) */
    NJ_RESULT    segment[NJ_MAX_PHRASE];      /**< Result structure (for clauses) */
    NJ_UINT16    segment_cnt;                 /**< Number of Result structures (for clauses) */
} WLF_CAND;

/**
 * Dictionary property
 */
typedef struct {
    char        name[WLF_FILE_PATH_LEN];    /**< File name              */
    char        ext_inputname[WLF_FILE_PATH_LEN];    /**< File name              */
    NJ_UINT32   size;                       /**< Size                   */
    fmap_t      fmap_handle;                /**< File map descriptor    */
    fmap_t      fmap_ext_area;              /**< ext_area file descriptor */
    fmap_t      fmap_ext_area_input;              /**< ext_area file descriptor */
    NJ_INT16    type;                       /**< Dictionary type        */
    NJ_INT16    mode[NJ_MAX_DIC];           /**< Dictionary mode        */
} WLF_DIC_PROP;

/**
 * @brief  Language information structure
 */
typedef struct {
    NJ_UINT8        lang;                           /**< Language                        */
    NJ_DIC_SET      dicset;                         /**< Dictionary Set                  */
    NJ_DIC_HANDLE   learn_handle;                   /**< Dictionary handle               */
    NJ_CHARSET      charset;                        /**< Flexible character set          */
    NJ_UINT16       charset_off_count;              /**< Count of NJ_CHARSET.charset_count when off */
    WLF_DIC_PROP    dicprops[NJ_MAX_DIC];           /**< Dictionary management           */
    fmap_t          handle_reference[NJ_MAX_DIC];   /**< Dictionary reference identifier */
} WLF_LANG_INFO;

/**
 * Search information
 */
typedef struct {
    NJ_CLASS        iwnn;           /**< iWnn instance                  */
    NJ_DIC_SET      dicset;         /**< Dictionary set                 */
    NJ_CURSOR       cursor;         /**< Cursor for searching dictionary */
    NJ_RESULT       search_word[NJ_MAX_USER_COUNT]; /**< Search word information */
    NJ_INT16        search_cnt;     /**< Number of search words         */
} WLF_SEARCH_INFO;

/**
 * Pseudo candidate definition table
 */
typedef struct {
    const NJ_GIJISET    *module[WLF_GIJISET_MAX];   /**< Type of pseudo dictionaries */
    const NJ_GIJISET    *fixed[WLF_GIJISET_MAX];    /**< Type of the fixed pseudo dictionary */
} WLF_GIJIPACK_INFO;

/**
 * Pseudo candidate information
 */
typedef struct {
    NJ_UINT16       giji_cnt;          /**< Number of pseudo candidates */
    NJ_INT16        type_cur;          /**< Type of a pseudo candidate  */
    NJ_RESULT       giji_candidate[WLF_GIJI_MAX_CANDIDATE]; /**< Pseudo candidate */
    const WLF_GIJIPACK_INFO *gijipack;/**< Pseudo candidate definition table */
    NJ_CHAR         giji_stroke[NJ_MAX_LEN+NJ_TERM_SIZE]; /**< Reading  */
    NJ_GIJISET      giji_filter;       /**< Pseudo dictionary filter */
} WLF_GIJI_INFO;

/**
 * Download dictionaries' information
 */
typedef struct {
    char name[50];
    char file[1024];
    int convert_high;
    int convert_base;
    int predict_high;
    int predict_base;
    int morpho_high;
    int morpho_base;
    int cache;
    int limit;
} DL_DIC_PARAMS;

/**
 * @brief  Wnn Language Framework class
 */
typedef struct {
    WLF_UINT8       mode;                                   /**< Mode                     */
    WLF_UINT8       giji_mode;                              /**< Pseudo candidate mode    */
    WLF_UINT8       hunhun_mode;                            /**< Wildcard prediction mode */
    WLF_UINT8       filter;                                 /**< Filter type              */
    WLF_UINT8       lang;                                   /**< Language                 */
    WLF_UINT8       keytype;                                /**< Keyboard type            */
    WLF_UINT8       bookshelf;                              /**< Dictionary type          */
    WLF_UINT8       endian;                                 /**< Endian                   */
    NJ_CLASS        iwnn;                                   /**< iWnn analyzing class info*/
    WLF_LANG_INFO   lang_info[WLF_LANG_TYPE_USE_MAX];       /**< Language information     */
    NJ_DIC_SET      dicset;                                 /**< Dictionary set           */
    NJ_DIC_SET      dicset_empty;                           /**< for switching dictionary <empty> */
    NJ_OPTION       option;                                 /**< Option                   */
    NJ_STATE        state;                                  /**< State information        */
    NJ_CHAR         input_stroke[NJ_MAX_LEN+NJ_TERM_SIZE];  /**< Reading string           */
    NJ_CHARSET      charset;                                /**< Flexible character set   */
    NJ_UINT8        connect;                                /**< Connection learning flag */
    NJ_UINT8        relation;                               /**< Relational searching flag*/
    NJ_UINT8        learn;                                  /**< Learning flag            */
    NJ_UINT8        headconv;                               /**< Head conversion flag     */
    WLF_PREDICT     predict_info;                           /**< Prediction information   */
    WLF_CONV        conv_info;                              /**< Conversion information   */
    WLF_CAND        cand_info;                              /**< Prediction candidate info*/
    WLF_SEARCH_INFO search_info;                            /**< Searching info           */
    WLF_GIJI_INFO   giji_info;                              /**< Pseudo candidate info    */
    fmap_t          dl_dic_fmap[IWNN_DL_DIC_MAX];           /**< File map descriptor for download dictionary */
    NJ_SEARCH_CACHE *dl_dic_srhcache[IWNN_DL_DIC_MAX];      /**< Search cache for download dictionary */
    DL_DIC_PARAMS   dl_dic_params[IWNN_DL_DIC_MAX];         /**< Download dictionaries' information */
    char            package[WLF_FILE_PATH_LEN];             /**< Package name of the service client */
    char            password[WLF_FILE_PATH_LEN];            /**< Package password of the service client */
    NJ_TIME_ST      time_info;                              /**< System time info         */
    DL_DEMOJI_INFO demoji_info;                             /**< Decoemoji information    */
    char            *data_area_path;                        /**< Data area path           */
} WLF_CLASS;


#endif /* _WLF_COM_H_ */
