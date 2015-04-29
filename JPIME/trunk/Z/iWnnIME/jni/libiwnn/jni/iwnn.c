/**
 * @file
 *   iWnn Interface for SWIG conversion.
 *
 * @author
 *   OMRON SOFTWARE Co., Ltd.
 *
 * (C) Copyright OMRON SOFTWARE Co., Ltd. 2009 All Rights Reserved
 */
#include <sys/types.h>
#include <sys/stat.h>

#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <libgen.h>
#include <errno.h>
#include <jni.h>

#ifndef _NJ_LIB_H_
#include "nj_lib.h"
#define _NJ_LIB_H_
#endif /* _NJ_LIB_H_ */
#include "ex_giji.h"
#ifdef NJ_OPT_UTF16
#include "uniconv.h"
#else
#include "nj_cc_lib.h"
#endif /*NJ_OPT_UTF16*/
#include "iwnn.h"

#include "nj_err.h"
#define LOG_TAG "iWnn"
#include "Log.h"

#include "nj_ext.h"
#include "njfilter.h"

#include "iwnn_wlf_sys.h"
#include "iwnn_wlf_com.h"

#include "iwnn_utils.h"
#include "giji_qwerty.h"
#include "demoji_giji.h"
#include "demoji_api.h"
#include "nj_dic.h"
#include "njd.h"
#include "ex_nmfgiji.h"


NJ_EXTERN NJ_INT16 njex_pred_giji_dic(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE *message);
NJ_EXTERN NJ_INT16 njex_aip_giji_dic(NJ_CLASS* iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE* message);
NJ_EXTERN NJ_INT16 njex_cmpdg_giji_dic(NJ_CLASS* iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE* message);
NJ_EXTERN NJ_INT16 njex_nmcgiji_dic(NJ_CLASS* iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE* message);
NJ_EXTERN NJ_INT16 njex_nmscgiji_dic(NJ_CLASS* iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE* message);
NJ_EXTERN NJ_INT16 njex_hrl_giji_dic(NJ_CLASS* iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE* message);
NJ_EXTERN NJ_INT16 njex_hrl_init_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE rhandle, NJ_VOID *ext_area, NJ_UINT32 size);
NJ_EXTERN NJ_INT16 njex_hrl_check_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE rhandle, NJ_VOID *ext_area, NJ_UINT32 size);
NJ_EXTERN NJ_INT16 njex_hrl_get_ext_area_size(NJ_CLASS *iwnn, NJ_DIC_HANDLE rhandle, NJ_UINT32 *size);


extern unsigned char iwnn_conv_table_CP932[];

static const int log_trace = 0;

/****************************************************************************/
/*                              DEFINES                                     */
/****************************************************************************/

/**
 * @brief Initialize Macro for WLF_PREDICT structure
 *
 * @param[in,out] pre  WLF_PREDICT structure
 *
 */
#define WLF_SET_INIT_PREDICT(pre)                                   \
           ((pre)->mode                 = 0,                        \
            (pre)->forecast_learn_limit = WLF_SET_FORECAST_LIMITMAX, \
            (pre)->forecast_limit       = WLF_SET_FORECAST_LIMITMAX, \
            (pre)->flexible_search      = WLF_FLEXIBLE_SEARCH_OFF,  \
            (pre)->stroke_min           = 0,                        \
            (pre)->stroke_max           = NJ_MAX_LEN,               \
            (pre)->in_divide_pos        = NJ_DEFAULT_IN_DIVIDE_POS, \
            (pre)->out_divide_pos       = NJ_DEFAULT_OUT_DIVIDE_POS)

/**
 * @brief Initialize Macro for WLF_CONV structure
 *
 * @param[in,out] conv WLF_CONV structure
 *
 */
#define WLF_SET_INIT_CONVERT(conv)         \
           ((conv)->devide_pos      = 0,   \
            (conv)->fix_seg_cnt     = -1,   \
            (conv)->fix_stroke_len = 0)

/**
 * @brief Initialize Macro for WLF_CAND structure
 *
 * @param[in,out] cand WLF_CAND structure
 *
 */
#define WLF_SET_INIT_CANDIDATE(cand)     \
           ((cand)->candidate_cnt = 0,   \
            (cand)->segment_cnt   = 0)

/**
 * Macro to compare characters a >= b ?
 *
 * @param[in] a   Character a (NJ_CHAR*)
 * @param[in] b   Character b (NJ_CHAR*)
 *
 * @retval !0  a >= b
 * @retval  0  a < b
 */
#define WLF_CHAR_IS_MOREEQ(a, b)                                         \
    (  (((WLF_UINT8*)(a))[0] >  ((WLF_UINT8*)(b))[0]) ||                  \
      ((((WLF_UINT8*)(a))[0] == ((WLF_UINT8*)(b))[0]) && (((WLF_UINT8*)(a))[1] >= ((WLF_UINT8*)(b))[1])) )

/**
 * Macro to compare characters a <= b ?
 *
 * @param[in] a   Character a (NJ_CHAR*)
 * @param[in] b   Character b (NJ_CHAR*)
 *
 * @retval !0  a <= b
 * @retval  0  a > b
 */
#define WLF_CHAR_IS_LESSEQ(a, b)                                         \
    ( (((WLF_UINT8*)(a))[0] < ((WLF_UINT8*)(b))[0]) ||                    \
      ((((WLF_UINT8*)(a))[0] == ((WLF_UINT8*)(b))[0]) && (((WLF_UINT8*)(a))[1] <= ((WLF_UINT8*)(b))[1])) )

/**
 * Macro to count string elements
 *
 * @param[in] s   String(NJ_CHAR*)
 *
 * @retval Number of elements
 */
#define WLF_CHAR_LEN(s)                                                  \
    ( (WLF_CHAR_IS_MOREEQ((s), "\xD8\x00") && WLF_CHAR_IS_LESSEQ((s), "\xDB\xFF")) \
      ? ( (*((s)+1) == WLF_CHAR_NUL) ? 1 : 2)                            \
      : 1) /* for Surrogate Pair */

/**
 * Get extendedWordInfoOffset
 *
 * @param[in]     cnt  dicWordCount
 *
 * @return             Offset(byte)
 */
#define GET_EXT_DATA_AREA_OFFSET(cnt)                                   \
    (NJ_LEARN_DIC_HEADER_SIZE + NJ_INDEX_SIZE * ((cnt)+1) * 2 + LEARN_DIC_QUE_SIZE * (cnt))

#define POS_MAX_WORD     0x28    /**< MaxWordCountOffset */

/** JNI interface method max number */
#define METHODS_MAX         61

/** Error code table max number */
#define IWNN_ERR_CODE_TABLES_MAX        55

/** Aimai character table max number */
#define AIMAI_MAX  362

/** Aimai character convert max number */
#define AIMAI_CONVERT_MAX 0x2E00

/** Use ext_input_area OFF=0 ON=1 */
#define USE_EXT_INPUT 1

/****************************************************************************/
/*                              STRUCTURES                                  */
/****************************************************************************/
/* Table for Japanese */
#include "iwnn_wlf_tbl_jp.h"

/****************************************************************************
 * debug
 ****************************************************************************/
/**
 * Error table
 */
typedef struct {
    NJ_INT16 code;
    char *name;
} IWNN_ERR_TABLE;

/** JNI interface */
static JNINativeMethod METHODS[METHODS_MAX] = {
  {"getInfo", "()I", (void*)iwnn_get_info },
  {"setActiveLang", "(II)I", (void*)iwnn_set_active_lang },
  {"setBookshelf", "(II)I", (void*)iwnn_set_bookshelf },
  {"unmountDics", "(I)I", (void*)iwnn_unmount_dics },
  {"destroy", "(I)V", (void*)iwnn_destroy },
  {"setInput", "(ILjava/lang/String;)I", (void*)iwnn_set_input },
  {"getInput", "(I)Ljava/lang/String;", (void*)iwnn_get_input },
  {"setdicByConf", "(ILjava/lang/String;I)I", (void*)iwnn_setdic_by_conf },
  {"init", "(ILjava/lang/String;)I", (void*)iwnn_init },
  {"getState", "(I)I", (void*)iwnn_get_state },
  {"setState", "(I)I", (void*)iwnn_set_state },
  {"setStateSystem", "(III)V", (void*)iwnn_set_state_system },
  {"forecast", "(IIII)I", (void*)iwnn_forecast },
  {"select", "(IIII)I", (void*)iwnn_select },
  {"searchWord", "(III)I", (void*)iwnn_search_word },
  {"getWord", "(III)Ljava/lang/String;", (void*)iwnn_get_word },
  {"addWord", "(ILjava/lang/String;Ljava/lang/String;III)I", (void*)iwnn_add_word },
  {"deleteWord", "(II)I", (void*)iwnn_delete_word },
  {"deleteSearchWord", "(II)I", (void*)iwnn_delete_search_word },
  {"conv", "(II)I", (void*)iwnn_conv },
  {"noconv", "(I)I", (void*)iwnn_noconv },
  {"getWordStroke", "(III)Ljava/lang/String;", (void*)iwnn_get_word_stroke },
  {"getWordString", "(III)Ljava/lang/String;", (void*)iwnn_get_word_string },
  {"getSegmentStroke", "(II)Ljava/lang/String;", (void*)iwnn_get_segment_stroke },
  {"getSegmentString", "(II)Ljava/lang/String;", (void*)iwnn_get_segment_string },
  {"deleteDictionary", "(IIII)I", (void*)iwnn_delete_dictionary },
  {"resetExtendedInfo", "(Ljava/lang/String;)I", (void*)iwnn_reset_extended_info },
  {"setFlexibleCharset", "(III)I", (void*)iwnn_set_flexible_charset },
  {"WriteOutDictionary", "(II)I", (void*)iwnn_write_out_dictionary },
  {"syncDictionary", "(II)I", (void*)iwnn_sync_dictionary },
  {"sync", "(I)V", (void*)iwnn_sync },
  {"isLearnDictionary", "(II)I", (void*)iwnn_is_learn_dictionary },
  {"isGijiResult", "(II)I", (void*)iwnn_is_giji_result },
  {"undo", "(II)I", (void*)iwnn_undo },
  {"emojiFilter", "(II)V", (void*)iwnn_emoji_filter },
  {"emailAddressFilter", "(II)V", (void*)iwnn_email_address_filter },
  {"splitWord", "(ILjava/lang/String;[I)V", (void*)iwnn_split_word},
  {"getMorphemeWord", "(II[Ljava/lang/String;)V", (void*)iwnn_get_morpheme_word },
  {"getMorphemeHinsi", "(II)S", (void*)iwnn_get_morpheme_hinsi},
  {"getMorphemeYomi", "(II[Ljava/lang/String;)V", (void*)iwnn_get_morpheme_yomi},
  {"createAdditionalDictionary", "(II)I", (void*)iwnn_create_additional_dictionary},
  {"deleteAdditionalDictionary", "(II)I", (void*)iwnn_delete_additional_dictionary},
  {"saveAdditionalDictionary", "(II)I", (void*)iwnn_save_additional_dictionary},
  {"createAutoLearningDictionary", "(II)I", (void*)iwnn_create_auto_learning_dictionary},
  {"deleteAutoLearningDictionary", "(II)I", (void*)iwnn_delete_auto_learning_dictionary},
  {"saveAutoLearningDictionary", "(II)I", (void*)iwnn_save_auto_learning_dictionary},
  {"setDownloadDictionary", "(IILjava/lang/String;Ljava/lang/String;IIIIIIZI)V",
        (void*)iwnn_set_download_dictionary},
  {"refreshConfFile", "(I)I", (void*)iwnn_refresh_conf_file},
  {"setServicePackageName", "(ILjava/lang/String;Ljava/lang/String;)I", (void*)iwnn_set_service_package_name},
  {"decoemojiFilter", "(II)V", (void*)iwnn_decoemoji_filter },
  {"controlDecoEmojiDictionary", "(ILjava/lang/String;Ljava/lang/String;II)V", (void*)iwnn_control_decoemoji_dictionary},
  {"checkDecoEmojiDictionary", "(I)I", (void*)iwnn_check_decoemoji_dictionary},
  {"resetDecoEmojiDictionary", "(I)I", (void*)iwnn_reset_decoemoji_dictionary},
  {"checkDecoemojiDicset", "(I)I", (void*)iwnn_check_decoemoji_dicset},
  {"getgijistr", "(III)I", (void*)iwnn_getgijistr},
  {"deleteLearnDicDecoEmojiWord", "(I)I", (void*)iwnn_delete_learndic_decoemojiword},
  {"setGijiFilter", "(I[I)I", (void*)iwnn_set_giji_filter},
  {"deleteDictionaryFile", "(Ljava/lang/String;)I", (void*)iwnn_delete_dictionary_file},
  {"getWordInfoStemAttribute", "(II)I", (void*)iwnn_get_word_info_stem_attribute },
  {"getSegmentStrokeLength", "(II)I", (void*)iwnn_get_segment_stroke_length},
  {"getWordStrokeLength", "(II)I", (void*)iwnn_get_word_stroke_length}
};

/** JNI class name */
static const char *CLASS_PATH_NAME = "jp/co/omronsoft/iwnnime/ml/iwnn/IWnnNative";

/**
 * Error code table
 */
static const IWNN_ERR_TABLE IWNN_ERR_CODE_TABLES[IWNN_ERR_CODE_TABLES_MAX] = {
    { NJ_ERR_PARAM_DIC_NULL,        "NJ_ERR_PARAM_DIC_NULL" },
    { NJ_ERR_PARAM_YOMI_NULL,       "NJ_ERR_PARAM_YOMI_NULL" },
    { NJ_ERR_PARAM_YOMI_SIZE,       "NJ_ERR_PARAM_YOMI_SIZE" },
    { NJ_ERR_PARAM_KUGIRI,          "NJ_ERR_PARAM_KUGIRI" },
    { NJ_ERR_PARAM_ILLEGAL_LEVEL,   "NJ_ERR_PARAM_ILLEGAL_LEVEL" },
    { NJ_ERR_PARAM_RESULT_NULL,     "NJ_ERR_PARAM_RESULT_NULL" },
    { NJ_ERR_YOMI_TOO_LONG,         "NJ_ERR_YOMI_TOO_LONG" },
    { NJ_ERR_NO_HINSI,              "NJ_ERR_NO_HINSI" },
    { NJ_ERR_NO_RULEDIC,            "NJ_ERR_NO_RULEDIC" },
    { NJ_ERR_PARAM_OPERATION,       "NJ_ERR_PARAM_OPERATION" },
    { NJ_ERR_PARAM_MODE,            "NJ_ERR_PARAM_MODE" },
    { NJ_ERR_PARAM_KANJI_NULL,      "NJ_ERR_PARAM_KANJI_NULL" },
    { NJ_ERR_CANDIDATE_TOO_LONG,    "NJ_ERR_CANDIDATE_TOO_LONG" },
    { NJ_ERR_PARAM_CURSOR_NULL,     "NJ_ERR_PARAM_CURSOR_NULL" },
    { NJ_ERR_DIC_TYPE_INVALID,      "NJ_ERR_DIC_TYPE_INVALID" },
    { NJ_ERR_DIC_HANDLE_NULL,       "NJ_ERR_DIC_HANDLE_NULL" },
    { NJ_ERR_FORMAT_INVALID,        "NJ_ERR_FORMAT_INVALID" },
    { NJ_ERR_NO_CANDIDATE_LIST,     "NJ_ERR_NO_CANDIDATE_LIST" },
    { NJ_ERR_NOT_CONVERTED,         "NJ_ERR_NOT_CONVERTED" },
    { NJ_ERR_AREASIZE_INVALID,      "NJ_ERR_AREASIZE_INVALID" },
    { NJ_ERR_BUFFER_NOT_ENOUGH,     "NJ_ERR_BUFFER_NOT_ENOUGH" },
    { NJ_ERR_HINSI_GROUP_INVALID,   "NJ_ERR_HINSI_GROUP_INVALID" },
    { NJ_ERR_CREATE_TYPE_INVALID,   "NJ_ERR_CREATE_TYPE_INVALID" },
    { NJ_ERR_WORD_INFO_NULL,        "NJ_ERR_WORD_INFO_NULL" },
    { NJ_ERR_DIC_NOT_FOUND,         "NJ_ERR_DIC_NOT_FOUND" },
    { NJ_ERR_CANNOT_GET_QUE,        "NJ_ERR_CANNOT_GET_QUE" },
    { NJ_ERR_INVALID_FLAG,          "NJ_ERR_INVALID_FLAG" },
    { NJ_ERR_INVALID_RESULT,        "NJ_ERR_INVALID_RESULT" },
    { NJ_ERR_NOT_SELECT_YET,        "NJ_ERR_NOT_SELECT_YET" },
    { NJ_ERR_INTERNAL,              "NJ_ERR_INTERNAL" },
    { NJ_ERR_USER_YOMI_INVALID,     "NJ_ERR_USER_YOMI_INVALID" },
    { NJ_ERR_USER_KOUHO_INVALID,    "NJ_ERR_USER_KOUHO_INVALID" },
    { NJ_ERR_USER_DIC_FULL,         "NJ_ERR_USER_DIC_FULL" },
    { NJ_ERR_SAME_WORD,             "NJ_ERR_SAME_WORD" },
    { NJ_ERR_DIC_BROKEN,            "NJ_ERR_DIC_BROKEN" },
    { NJ_ERR_CANNOT_RESTORE,        "NJ_ERR_CANNOT_RESTORE" },
    { NJ_ERR_WORD_NOT_FOUND,        "NJ_ERR_WORD_NOT_FOUND" },
    { NJ_ERR_PARAM_NORMAL_NULL,     "NJ_ERR_PARAM_NORMAL_NULL" },
    { NJ_ERR_PARAM_TYPE_INVALID,    "NJ_ERR_PARAM_TYPE_INVALID" },
    { NJ_ERR_PARAM_PROCESS_LEN_NULL,    "NJ_ERR_PARAM_PROCESS_LEN_NULL" },
    { NJ_ERR_CANNOT_USE_MORPHOLIZE, "NJ_ERR_CANNOT_USE_MORPHOLIZE" },
    { NJ_ERR_PARAM_ILLEGAL_LIMIT,   "NJ_ERR_PARAM_ILLEGAL_LIMIT" },
    { NJ_ERR_DIC_VERSION_INVALID,   "NJ_ERR_DIC_VERSION_INVALID" },
    { NJ_ERR_DIC_FREQ_INVALID,      "NJ_ERR_DIC_FREQ_INVALID" },
    { NJ_ERR_CACHE_NOT_ENOUGH,      "NJ_ERR_CACHE_NOT_ENOUGH" },
    { NJ_ERR_CACHE_BROKEN,          "NJ_ERR_CACHE_BROKEN" },
    { NJ_ERR_PARAM_ENV_NULL,        "NJ_ERR_PARAM_ENV_NULL" },
    { NJ_ERR_PARAM_EXT_AREA_NULL,   "NJ_ERR_PARAM_EXT_AREA_NULL" },
    { NJ_ERR_PARAM_EXT_AREA_INVALID_SIZE,   "NJ_ERR_PARAM_EXT_AREA_INVALID_SIZE" },
    { NJ_ERR_PARAM_ANALYZE_OPTION_NULL, "NJ_ERR_PARAM_ANALYZE_OPTION_NULL" },
    { NJ_ERR_PARAM_ILLEGAL_CHAR_LEN,    "NJ_ERR_PARAM_ILLEGAL_CHAR_LEN" },
    { NJ_ERR_PARAM_STATE_NULL,      "NJ_ERR_PARAM_STATE_NULL" },
    { NJ_ERR_PARAM_SIZE_NULL,       "NJ_ERR_PARAM_SIZE_NULL" },
    { NJ_ERR_PARAM_INVALID_STATE,   "NJ_ERR_PARAM_INVALID_STATE" },
    { NJ_ERR_PROTECTION_ERROR,      "NJ_ERR_PROTECTION_ERROR" }
};

/**
 * Number of error codes
 */
static const int IWNN_ERR_CODES = sizeof(IWNN_ERR_CODE_TABLES)/sizeof(IWNN_ERR_TABLE);

/**
 * Aimai character table
 */
static const short AIMAI_CHARACTER_TABLE[AIMAI_MAX][2] = {
    {0x0041/*A*/, 0x0061/*a*/},
    {0x0041/*A*/, 0x00c0/*À*/},
    {0x0041/*A*/, 0x00c1/*Á*/},
    {0x0041/*A*/, 0x00c2/*Â*/},
    {0x0041/*A*/, 0x00c3/*Ã*/},
    {0x0041/*A*/, 0x00c4/*Ä*/},
    {0x0041/*A*/, 0x00c5/*Å*/},
    {0x0041/*A*/, 0x00c6/*Æ*/},
    {0x0041/*A*/, 0x00e0/*à*/},
    {0x0041/*A*/, 0x00e1/*á*/},
    {0x0041/*A*/, 0x00e2/*â*/},
    {0x0041/*A*/, 0x00e3/*ã*/},
    {0x0041/*A*/, 0x00e4/*ä*/},
    {0x0041/*A*/, 0x00e5/*å*/},
    {0x0041/*A*/, 0x00e6/*æ*/},
    {0x0041/*A*/, 0x0104/*Ą*/},
    {0x0041/*A*/, 0x0105/*ą*/},
    {0x0042/*B*/, 0x0062/*b*/},
    {0x0043/*C*/, 0x0063/*c*/},
    {0x0043/*C*/, 0x00c7/*Ç*/},
    {0x0043/*C*/, 0x00e7/*ç*/},
    {0x0043/*C*/, 0x0106/*Ć*/},
    {0x0043/*C*/, 0x0107/*ć*/},
    {0x0043/*C*/, 0x010c/*Č*/},
    {0x0043/*C*/, 0x010d/*č*/},
    {0x0044/*D*/, 0x0064/*d*/},
    {0x0044/*D*/, 0x010e/*Ď*/},
    {0x0044/*D*/, 0x010f/*ď*/},
    {0x0044/*D*/, 0x0110/*Đ*/},
    {0x0044/*D*/, 0x0111/*đ*/},
    {0x0045/*E*/, 0x0065/*e*/},
    {0x0045/*E*/, 0x00c8/*È*/},
    {0x0045/*E*/, 0x00c9/*É*/},
    {0x0045/*E*/, 0x00ca/*Ê*/},
    {0x0045/*E*/, 0x00cb/*Ë*/},
    {0x0045/*E*/, 0x00e8/*è*/},
    {0x0045/*E*/, 0x00e9/*é*/},
    {0x0045/*E*/, 0x00ea/*ê*/},
    {0x0045/*E*/, 0x00eb/*ë*/},
    {0x0045/*E*/, 0x0118/*Ę*/},
    {0x0045/*E*/, 0x0119/*ę*/},
    {0x0045/*E*/, 0x011a/*Ě*/},
    {0x0045/*E*/, 0x011b/*ě*/},
    {0x0046/*F*/, 0x0066/*f*/},
    {0x0047/*G*/, 0x0067/*g*/},
    {0x0047/*G*/, 0x011e/*Ğ*/},
    {0x0047/*G*/, 0x011f/*ğ*/},
    {0x0048/*H*/, 0x0068/*h*/},
    {0x0049/*I*/, 0x0069/*i*/},
    {0x0049/*I*/, 0x00cc/*Ì*/},
    {0x0049/*I*/, 0x00cd/*Í*/},
    {0x0049/*I*/, 0x00ce/*Î*/},
    {0x0049/*I*/, 0x00cf/*Ï*/},
    {0x0049/*I*/, 0x00ec/*ì*/},
    {0x0049/*I*/, 0x00ed/*í*/},
    {0x0049/*I*/, 0x00ee/*î*/},
    {0x0049/*I*/, 0x00ef/*ï*/},
    {0x0049/*I*/, 0x0130/*İ*/},
    {0x0049/*I*/, 0x0131/*ı*/},
    {0x004a/*J*/, 0x006a/*j*/},
    {0x004b/*K*/, 0x006b/*k*/},
    {0x004c/*L*/, 0x006c/*l*/},
    {0x004c/*L*/, 0x0139/*Ĺ*/},
    {0x004c/*L*/, 0x013a/*ĺ*/},
    {0x004c/*L*/, 0x013d/*Ľ*/},
    {0x004c/*L*/, 0x013e/*ľ*/},
    {0x004c/*L*/, 0x0141/*Ł*/},
    {0x004c/*L*/, 0x0142/*ł*/},
    {0x004d/*M*/, 0x006d/*m*/},
    {0x004e/*N*/, 0x006e/*n*/},
    {0x004e/*N*/, 0x00d1/*Ñ*/},
    {0x004e/*N*/, 0x00f1/*ñ*/},
    {0x004e/*N*/, 0x0143/*Ń*/},
    {0x004e/*N*/, 0x0144/*ń*/},
    {0x004e/*N*/, 0x0147/*Ň*/},
    {0x004e/*N*/, 0x0148/*ň*/},
    {0x004f/*O*/, 0x006f/*o*/},
    {0x004f/*O*/, 0x00d2/*Ò*/},
    {0x004f/*O*/, 0x00d3/*Ó*/},
    {0x004f/*O*/, 0x00d4/*Ô*/},
    {0x004f/*O*/, 0x00d5/*Õ*/},
    {0x004f/*O*/, 0x00d6/*Ö*/},
    {0x004f/*O*/, 0x00d8/*Ø*/},
    {0x004f/*O*/, 0x00f2/*ò*/},
    {0x004f/*O*/, 0x00f3/*ó*/},
    {0x004f/*O*/, 0x00f4/*ô*/},
    {0x004f/*O*/, 0x00f5/*õ*/},
    {0x004f/*O*/, 0x00f6/*ö*/},
    {0x004f/*O*/, 0x00f8/*ø*/},
    {0x004f/*O*/, 0x0150/*Ő*/},
    {0x004f/*O*/, 0x0151/*ő*/},
    {0x004f/*O*/, 0x0152/*Œ*/},
    {0x004f/*O*/, 0x0153/*œ*/},
    {0x0050/*P*/, 0x0070/*p*/},
    {0x0051/*Q*/, 0x0071/*q*/},
    {0x0052/*R*/, 0x0072/*r*/},
    {0x0052/*R*/, 0x0154/*Ŕ*/},
    {0x0052/*R*/, 0x0155/*ŕ*/},
    {0x0052/*R*/, 0x0158/*Ř*/},
    {0x0052/*R*/, 0x0159/*ř*/},
    {0x0053/*S*/, 0x0073/*s*/},
    {0x0053/*S*/, 0x00df/*ß*/},
    {0x0053/*S*/, 0x015a/*Ś*/},
    {0x0053/*S*/, 0x015b/*ś*/},
    {0x0053/*S*/, 0x015e/*Ş*/},
    {0x0053/*S*/, 0x015f/*ş*/},
    {0x0053/*S*/, 0x0160/*Š*/},
    {0x0053/*S*/, 0x0161/*š*/},
    {0x0054/*T*/, 0x0074/*t*/},
    {0x0054/*T*/, 0x0164/*Ť*/},
    {0x0054/*T*/, 0x0165/*ť*/},
    {0x0055/*U*/, 0x0075/*u*/},
    {0x0055/*U*/, 0x00d9/*Ù*/},
    {0x0055/*U*/, 0x00da/*Ú*/},
    {0x0055/*U*/, 0x00db/*Û*/},
    {0x0055/*U*/, 0x00dc/*Ü*/},
    {0x0055/*U*/, 0x00f9/*ù*/},
    {0x0055/*U*/, 0x00fa/*ú*/},
    {0x0055/*U*/, 0x00fb/*û*/},
    {0x0055/*U*/, 0x00fc/*ü*/},
    {0x0055/*U*/, 0x016e/*Ů*/},
    {0x0055/*U*/, 0x016f/*ů*/},
    {0x0055/*U*/, 0x0170/*Ű*/},
    {0x0055/*U*/, 0x0171/*ű*/},
    {0x0056/*V*/, 0x0076/*v*/},
    {0x0057/*W*/, 0x0077/*w*/},
    {0x0058/*X*/, 0x0078/*x*/},
    {0x0059/*Y*/, 0x0079/*y*/},
    {0x0059/*Y*/, 0x00dd/*Ý*/},
    {0x0059/*Y*/, 0x00fd/*ý*/},
    {0x005a/*Z*/, 0x007a/*z*/},
    {0x005a/*Z*/, 0x0179/*Ź*/},
    {0x005a/*Z*/, 0x017a/*ź*/},
    {0x005a/*Z*/, 0x017b/*Ż*/},
    {0x005a/*Z*/, 0x017c/*ż*/},
    {0x005a/*Z*/, 0x017d/*Ž*/},
    {0x005a/*Z*/, 0x017e/*ž*/},
    {0x0061/*a*/, 0x00e0/*à*/},
    {0x0061/*a*/, 0x00e1/*á*/},
    {0x0061/*a*/, 0x00e2/*â*/},
    {0x0061/*a*/, 0x00e3/*ã*/},
    {0x0061/*a*/, 0x00e4/*ä*/},
    {0x0061/*a*/, 0x00e5/*å*/},
    {0x0061/*a*/, 0x00e6/*æ*/},
    {0x0061/*a*/, 0x0105/*ą*/},
    {0x0063/*c*/, 0x00e7/*ç*/},
    {0x0063/*c*/, 0x0107/*ć*/},
    {0x0063/*c*/, 0x010d/*č*/},
    {0x0064/*d*/, 0x010f/*ď*/},
    {0x0064/*d*/, 0x0111/*đ*/},
    {0x0065/*e*/, 0x00e8/*è*/},
    {0x0065/*e*/, 0x00e9/*é*/},
    {0x0065/*e*/, 0x00ea/*ê*/},
    {0x0065/*e*/, 0x00eb/*ë*/},
    {0x0065/*e*/, 0x0119/*ę*/},
    {0x0065/*e*/, 0x011b/*ě*/},
    {0x0067/*g*/, 0x011f/*ğ*/},
    {0x0069/*i*/, 0x00ec/*ì*/},
    {0x0069/*i*/, 0x00ed/*í*/},
    {0x0069/*i*/, 0x00ee/*î*/},
    {0x0069/*i*/, 0x00ef/*ï*/},
    {0x0069/*i*/, 0x0131/*ı*/},
    {0x006c/*l*/, 0x013a/*ĺ*/},
    {0x006c/*l*/, 0x013e/*ľ*/},
    {0x006c/*l*/, 0x0142/*ł*/},
    {0x006e/*n*/, 0x00f1/*ñ*/},
    {0x006e/*n*/, 0x0144/*ń*/},
    {0x006e/*n*/, 0x0148/*ň*/},
    {0x006f/*o*/, 0x00f2/*ò*/},
    {0x006f/*o*/, 0x00f3/*ó*/},
    {0x006f/*o*/, 0x00f4/*ô*/},
    {0x006f/*o*/, 0x00f5/*õ*/},
    {0x006f/*o*/, 0x00f6/*ö*/},
    {0x006f/*o*/, 0x00f8/*ø*/},
    {0x006f/*o*/, 0x0151/*ő*/},
    {0x006f/*o*/, 0x0153/*œ*/},
    {0x0072/*r*/, 0x0155/*ŕ*/},
    {0x0072/*r*/, 0x0159/*ř*/},
    {0x0073/*s*/, 0x00df/*ß*/},
    {0x0073/*s*/, 0x015b/*ś*/},
    {0x0073/*s*/, 0x015f/*ş*/},
    {0x0073/*s*/, 0x0161/*š*/},
    {0x0074/*t*/, 0x0165/*ť*/},
    {0x0075/*u*/, 0x00f9/*ù*/},
    {0x0075/*u*/, 0x00fa/*ú*/},
    {0x0075/*u*/, 0x00fb/*û*/},
    {0x0075/*u*/, 0x00fc/*ü*/},
    {0x0075/*u*/, 0x016f/*ů*/},
    {0x0075/*u*/, 0x0171/*ű*/},
    {0x0079/*y*/, 0x00fd/*ý*/},
    {0x007a/*z*/, 0x017a/*ź*/},
    {0x007a/*z*/, 0x017c/*ż*/},
    {0x007a/*z*/, 0x017e/*ž*/},
    {0x00c0/*À*/, 0x00e0/*à*/},
    {0x00c1/*Á*/, 0x00e1/*á*/},
    {0x00c2/*Â*/, 0x00e2/*â*/},
    {0x00c3/*Ã*/, 0x00e3/*ã*/},
    {0x00c4/*Ä*/, 0x00e4/*ä*/},
    {0x00c5/*Å*/, 0x00e5/*å*/},
    {0x00c6/*Æ*/, 0x00e6/*æ*/},
    {0x00c7/*Ç*/, 0x00e7/*ç*/},
    {0x00c8/*È*/, 0x00e8/*è*/},
    {0x00c9/*É*/, 0x00e9/*é*/},
    {0x00ca/*Ê*/, 0x00ea/*ê*/},
    {0x00cb/*Ë*/, 0x00eb/*ë*/},
    {0x00cc/*Ì*/, 0x00ec/*ì*/},
    {0x00cd/*Í*/, 0x00ed/*í*/},
    {0x00ce/*Î*/, 0x00ee/*î*/},
    {0x00cf/*Ï*/, 0x00ef/*ï*/},
    {0x00d1/*Ñ*/, 0x00f1/*ñ*/},
    {0x00d2/*Ò*/, 0x00f2/*ò*/},
    {0x00d3/*Ó*/, 0x00f3/*ó*/},
    {0x00d4/*Ô*/, 0x00f4/*ô*/},
    {0x00d5/*Õ*/, 0x00f5/*õ*/},
    {0x00d6/*Ö*/, 0x00f6/*ö*/},
    {0x00d8/*Ø*/, 0x00f8/*ø*/},
    {0x00d9/*Ù*/, 0x00f9/*ù*/},
    {0x00da/*Ú*/, 0x00fa/*ú*/},
    {0x00db/*Û*/, 0x00fb/*û*/},
    {0x00dc/*Ü*/, 0x00fc/*ü*/},
    {0x00dd/*Ý*/, 0x00fd/*ý*/},
    {0x0104/*Ą*/, 0x0105/*ą*/},
    {0x0106/*Ć*/, 0x0107/*ć*/},
    {0x010c/*Č*/, 0x010d/*č*/},
    {0x010e/*Ď*/, 0x010f/*ď*/},
    {0x0110/*Đ*/, 0x0111/*đ*/},
    {0x0118/*Ę*/, 0x0119/*ę*/},
    {0x011a/*Ě*/, 0x011b/*ě*/},
    {0x011e/*Ğ*/, 0x011f/*ğ*/},
    {0x0130/*İ*/, 0x0131/*ı*/},
    {0x0139/*Ĺ*/, 0x013a/*ĺ*/},
    {0x013d/*Ľ*/, 0x013e/*ľ*/},
    {0x0141/*Ł*/, 0x0142/*ł*/},
    {0x0143/*Ń*/, 0x0144/*ń*/},
    {0x0147/*Ň*/, 0x0148/*ň*/},
    {0x0150/*Ő*/, 0x0151/*ő*/},
    {0x0152/*Œ*/, 0x0153/*œ*/},
    {0x0154/*Ŕ*/, 0x0155/*ŕ*/},
    {0x0158/*Ř*/, 0x0159/*ř*/},
    {0x015a/*Ś*/, 0x015b/*ś*/},
    {0x015e/*Ş*/, 0x015f/*ş*/},
    {0x0160/*Š*/, 0x0161/*š*/},
    {0x0164/*Ť*/, 0x0165/*ť*/},
    {0x016e/*Ů*/, 0x016f/*ů*/},
    {0x0170/*Ű*/, 0x0171/*ű*/},
    {0x0179/*Ź*/, 0x017a/*ź*/},
    {0x017b/*Ż*/, 0x017c/*ż*/},
    {0x017d/*Ž*/, 0x017e/*ž*/},
    {0x0386/*Ά*/, 0x03ac/*ά*/},
    {0x0388/*Έ*/, 0x03ad/*έ*/},
    {0x0389/*Ή*/, 0x03ae/*ή*/},
    {0x038a/*Ί*/, 0x03af/*ί*/},
    {0x038c/*Ό*/, 0x03cc/*ό*/},
    {0x038e/*Ύ*/, 0x03cd/*ύ*/},
    {0x038f/*Ώ*/, 0x03ce/*ώ*/},
    {0x0391/*Α*/, 0x0386/*Ά*/},
    {0x0391/*Α*/, 0x03ac/*ά*/},
    {0x0391/*Α*/, 0x03b1/*α*/},
    {0x0392/*Β*/, 0x03b2/*β*/},
    {0x0393/*Γ*/, 0x03b3/*γ*/},
    {0x0394/*Δ*/, 0x03b4/*δ*/},
    {0x0395/*Ε*/, 0x0388/*Έ*/},
    {0x0395/*Ε*/, 0x03ad/*έ*/},
    {0x0395/*Ε*/, 0x03b5/*ε*/},
    {0x0396/*Ζ*/, 0x03b6/*ζ*/},
    {0x0397/*Η*/, 0x0389/*Ή*/},
    {0x0397/*Η*/, 0x03ae/*ή*/},
    {0x0397/*Η*/, 0x03b7/*η*/},
    {0x0398/*Θ*/, 0x03b8/*θ*/},
    {0x0399/*Ι*/, 0x038a/*Ί*/},
    {0x0399/*Ι*/, 0x0390/*ΐ*/},
    {0x0399/*Ι*/, 0x03aa/*Ϊ*/},
    {0x0399/*Ι*/, 0x03af/*ί*/},
    {0x0399/*Ι*/, 0x03b9/*ι*/},
    {0x0399/*Ι*/, 0x03ca/*ϊ*/},
    {0x039a/*Κ*/, 0x03ba/*κ*/},
    {0x039b/*Λ*/, 0x03bb/*λ*/},
    {0x039c/*Μ*/, 0x03bc/*μ*/},
    {0x039d/*Ν*/, 0x03bd/*ν*/},
    {0x039e/*Ξ*/, 0x03be/*ξ*/},
    {0x039f/*Ο*/, 0x038c/*Ό*/},
    {0x039f/*Ο*/, 0x03bf/*ο*/},
    {0x039f/*Ο*/, 0x03cc/*ό*/},
    {0x03a0/*Π*/, 0x03c0/*π*/},
    {0x03a1/*Ρ*/, 0x03c1/*ρ*/},
    {0x03a3/*Σ*/, 0x03c2/*ς*/},
    {0x03a3/*Σ*/, 0x03c3/*σ*/},
    {0x03a4/*Τ*/, 0x03c4/*τ*/},
    {0x03a5/*Υ*/, 0x038e/*Ύ*/},
    {0x03a5/*Υ*/, 0x03ab/*Ϋ*/},
    {0x03a5/*Υ*/, 0x03b0/*ΰ*/},
    {0x03a5/*Υ*/, 0x03c5/*υ*/},
    {0x03a5/*Υ*/, 0x03cb/*ϋ*/},
    {0x03a5/*Υ*/, 0x03cd/*ύ*/},
    {0x03a6/*Φ*/, 0x03c6/*φ*/},
    {0x03a7/*Χ*/, 0x03c7/*χ*/},
    {0x03a8/*Ψ*/, 0x03c8/*ψ*/},
    {0x03a9/*Ω*/, 0x038f/*Ώ*/},
    {0x03a9/*Ω*/, 0x03c9/*ω*/},
    {0x03a9/*Ω*/, 0x03ce/*ώ*/},
    {0x03aa/*Ϊ*/, 0x03ca/*ϊ*/},
    {0x03ab/*Ϋ*/, 0x03cb/*ϋ*/},
    {0x03b1/*α*/, 0x03ac/*ά*/},
    {0x03b5/*ε*/, 0x03ad/*έ*/},
    {0x03b7/*η*/, 0x03ae/*ή*/},
    {0x03b9/*ι*/, 0x0390/*ΐ*/},
    {0x03b9/*ι*/, 0x03af/*ί*/},
    {0x03b9/*ι*/, 0x03ca/*ϊ*/},
    {0x03bf/*ο*/, 0x03cc/*ό*/},
    {0x03c3/*σ*/, 0x03c2/*ς*/},
    {0x03c5/*υ*/, 0x03b0/*ΰ*/},
    {0x03c5/*υ*/, 0x03cb/*ϋ*/},
    {0x03c5/*υ*/, 0x03cd/*ύ*/},
    {0x03c9/*ω*/, 0x03ce/*ώ*/},
    {0x0401/*Ё*/, 0x0451/*ё*/},
    {0x0404/*Є*/, 0x0454/*є*/},
    {0x0406/*І*/, 0x0407/*Ї*/},
    {0x0406/*І*/, 0x0456/*і*/},
    {0x0406/*І*/, 0x0457/*ї*/},
    {0x0407/*Ї*/, 0x0457/*ї*/},
    {0x0410/*А*/, 0x0430/*а*/},
    {0x0411/*Б*/, 0x0431/*б*/},
    {0x0412/*В*/, 0x0432/*в*/},
    {0x0413/*Г*/, 0x0433/*г*/},
    {0x0413/*Г*/, 0x0490/*Ґ*/},
    {0x0414/*Д*/, 0x0434/*д*/},
    {0x0415/*Е*/, 0x0401/*Ё*/},
    {0x0415/*Е*/, 0x0435/*е*/},
    {0x0415/*Е*/, 0x0451/*ё*/},
    {0x0416/*Ж*/, 0x0436/*ж*/},
    {0x0417/*З*/, 0x0437/*з*/},
    {0x0418/*И*/, 0x0438/*и*/},
    {0x0419/*Й*/, 0x0439/*й*/},
    {0x041a/*К*/, 0x043a/*к*/},
    {0x041b/*Л*/, 0x043b/*л*/},
    {0x041c/*М*/, 0x043c/*м*/},
    {0x041d/*Н*/, 0x043d/*н*/},
    {0x041e/*О*/, 0x043e/*о*/},
    {0x041f/*П*/, 0x043f/*п*/},
    {0x0420/*Р*/, 0x0440/*р*/},
    {0x0421/*С*/, 0x0441/*с*/},
    {0x0422/*Т*/, 0x0442/*т*/},
    {0x0423/*У*/, 0x0443/*у*/},
    {0x0424/*Ф*/, 0x0444/*ф*/},
    {0x0425/*Х*/, 0x0445/*х*/},
    {0x0426/*Ц*/, 0x0446/*ц*/},
    {0x0427/*Ч*/, 0x0447/*ч*/},
    {0x0428/*Ш*/, 0x0448/*ш*/},
    {0x0429/*Щ*/, 0x0449/*щ*/},
    {0x042a/*Ъ*/, 0x044a/*ъ*/},
    {0x042b/*Ы*/, 0x044b/*ы*/},
    {0x042c/*Ь*/, 0x042a/*Ъ*/},
    {0x042c/*Ь*/, 0x044a/*ъ*/},
    {0x042c/*Ь*/, 0x044c/*ь*/},
    {0x042d/*Э*/, 0x044d/*э*/},
    {0x042e/*Ю*/, 0x044e/*ю*/},
    {0x042f/*Я*/, 0x044f/*я*/},
    {0x0433/*г*/, 0x0491/*ґ*/},
    {0x0435/*е*/, 0x0451/*ё*/},
    {0x044c/*ь*/, 0x044a/*ъ*/},
    {0x0456/*і*/, 0x0457/*ї*/},
    {0x0490/*Ґ*/, 0x0491/*ґ*/}
};

/***********************************************************************
 * static functions prototype
 ***********************************************************************/
static char *iwnn_make_message(const char *fmt, ...);
static char *iwnn_err_message(NJ_INT16 err_val);
static int read_binary(char *dicb, NJ_UINT8 **dicdata);
static int iwnn_wlfi_conf_read(WLF_CLASS *wlf, WLF_LANG_INFO *langinfo, const char *filename);

static int iwnn_set_input_i(iWnn_INFO* iwnn, char* str);
static int iwnn_setdic_by_conf_i(iWnn_INFO* iwnn, char* conf_file, int lang);
static int iwnn_add_word_i(iWnn_INFO* iwnn, char* yomi, char* repr, int group, int dtype, int con);

static WLF_UINT32 iwnn_wlf_get_init_size(void);
static WLF_INT16 iwnn_wlf_initialize(WLF_UINT8 *wlf_class, WLF_UINT8 endian);
static WLF_INT16 iwnn_wlf_load_lang(WLF_UINT8 *wlf_class, WLF_UINT8 lang, char *filename);
static WLF_INT16 iwnn_wlf_set_lang(WLF_UINT8 *wlf_class, WLF_UINT8 lang);

static void      iwnn_wlf_close_dictionary(WLF_CLASS *wlf);
static int       iwnn_wlf_reset_dictionary(WLF_CLASS *wlf, int dictype, int language, int dictionary);
static int       iwnn_wlf_reset_extended_info(char *file_name);

static WLF_INT16 iwnn_wlf_get_conversion(WLF_UINT8 *wlf_class, WLF_UINT8 mode, WLF_UINT8 *stroke,
                                         WLF_UINT8 devide_pos, WLF_INT32 stroke_min, WLF_INT32 stroke_max);
static WLF_INT16 iwnn_wlf_get_string(WLF_UINT8 *wlf_class, WLF_UINT16 seg_pos, WLF_UINT16 index, WLF_UINT8 *buf, WLF_UINT16 size);
static WLF_INT16 iwnn_wlf_get_stroke(WLF_UINT8 *wlf_class, WLF_UINT16 seg_pos, WLF_UINT16 index, WLF_UINT8 *buf, WLF_UINT16 size);
static WLF_INT16 iwnn_wlf_learn_string(WLF_UINT8 *wlf_class, WLF_INT32 seg_pos, WLF_INT32 index);
static int       iwnn_wlf_get_word(WLF_CLASS *wlf, int s_cnt);
static void      iwnn_wlfi_set_dicset(WLF_CLASS *wlf, NJ_UINT8 type);
static void      iwnn_wlfi_set_giji_table(WLF_CLASS *wlf);
static WLF_LANG_INFO* iwnn_wlfi_find_langinfo(WLF_CLASS *wlf, NJ_UINT8 lang);
static WLF_LANG_INFO* iwnn_wlfi_find_free_langinfo(WLF_CLASS *wlf);

static void      iwnn_wlfi_force_dicset(WLF_LANG_INFO *langinfo);
static void      iwnn_wlfi_force_dicinfo(WLF_LANG_INFO *langinfo, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop);
static int       iwnn_wlfi_write_out_dictionary(WLF_CLASS *wlf, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop);
static int       iwnn_wlfi_write_out_file(fmap_t *fmap, char *data_area_path, int dictype, char *package, char* password);
static int       iwnn_wlfi_reset_learn_dictionary(WLF_CLASS *wlf, int language);
static int       iwnn_wlfi_reset_user_dictionary(WLF_CLASS *wlf, int language, int dictionary);
static int       iwnn_wlfi_reset_hrlgiji_dictionary(WLF_CLASS *wlf, int language);
static int       iwnn_wlfi_reset_dictionary(WLF_CLASS *wlf, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop);
static int       iwnn_wlfi_reset_learning_extended_info(char *file_name);

static int iwnn_wlf_set_state(WLF_CLASS *wlf, NJ_STATE *state);
static int iwnn_wlf_get_state(WLF_CLASS *wlf, NJ_STATE *state);

static NJ_INT16  iwnn_wlfi_get_prediction(WLF_CLASS *wlf, NJ_UINT16 s_cnt);
static NJ_INT16  iwnn_wlfi_get_conv(WLF_CLASS *wlf);
static void      iwnn_wlfi_set_giji(WLF_CLASS *wlf, NJ_UINT8 type);
static NJ_INT16  iwnn_wlfi_get_zenkouho(WLF_CLASS *wlf, NJ_UINT16 s_cnt, NJ_UINT16 *t_cnt);
static void      iwnn_wlfi_set_flexible_charset(WLF_CLASS *wlf, int flexible_charset, int keytype);
static void      iwnn_wlfi_init_giji(WLF_CLASS *wlf);
static int       iwnn_wlfi_get_giji(WLF_CLASS *wlf, int s_cnt, int mode);
static NJ_INT16  iwnn_wlfi_select(WLF_CLASS *wlf, NJ_INT16 gidx, NJ_INT16 ridx, NJ_UINT16 type);
static WLF_INT32 iwnn_wlfi_convert_internal(WLF_CLASS *wlf, WLF_UINT8 *src, NJ_CHAR *dst, size_t size);
static WLF_INT32 iwnn_wlfi_convert_external(WLF_CLASS *wlf, NJ_CHAR *src, NJ_UINT8 *dst, size_t size);
static WLF_UINT8 iwnn_wlfi_calc_devide_pos(WLF_CLASS *wlf, WLF_UINT8 devide_pos);

static void      iwnn_wlf_set_hunhun(WLF_CLASS *wlf, WLF_INT32 stroke_min, WLF_INT32 stroke_max);
static void      iwnn_wlfi_set_hunhun(WLF_CLASS *wlf);

static void      iwnn_wlf_make_convert_giji_stroke(WLF_CLASS *wlf, NJ_UINT8 seg_pos);

static int       iwnn_dic_mount(WLF_CLASS *wlf, WLF_LANG_INFO *langinfo, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop, int cache);
static int       iwnn_dic_mount_easy(NJ_CLASS *iwnn, WLF_LANG_INFO *langinfo, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop);
static int       iwnn_dic_mount_ext(WLF_CLASS *wlf, WLF_LANG_INFO *langinfo, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop, char* package, char* password);
static int       iwnn_dic_mount_ext_open(NJ_CLASS *iwnn, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop, NJ_UINT32 size, const char *path);
static int       iwnn_dic_mount_ext_open_input(NJ_CLASS *iwnn, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop);
static int       iwnn_dic_init(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_area, NJ_UINT32 size, int dictype, int mode, int exttype);
static int       iwnn_learning_extended_info_reset(NJ_DIC_HANDLE handle);

static NJ_INT16  iwnn_phase1_filter(NJ_CLASS *iwnn, NJ_PHASE1_FILTER_MESSAGE *message);
static NJ_INT16  iwnn_check_emoji(NJ_CLASS *iwnn, NJ_PHASE1_FILTER_MESSAGE *message);
static NJ_INT16  iwnn_check_aimai(NJ_CLASS *iwnn, NJ_PHASE1_FILTER_MESSAGE *message);
static NJ_INT16 iwnn_is_contain_aimai_table(NJ_CHAR input, NJ_CHAR stroke);
static NJ_INT16 iwnn_is_contain_aimai_table_second_phase(NJ_CHAR input, NJ_CHAR stroke, int index);
static WLF_INT16 iwnn_wlfi_split_word(WLF_CLASS* wlf, WLF_UINT8* str, WLF_UINT8* process_len);
static void iwnn_set_stem_and_yomi(JNIEnv *env, jobjectArray dist, NJ_CHAR *text, int stem_len, iWnn_INFO *iwnn);
static WLF_INT16 iwnn_wlfi_get_hinsi(WLF_CLASS* wlf, WLF_INT16 index);
static void    iwnn_wlfi_conf_read_auto_learning_dictionary(WLF_CLASS *wlf, WLF_LANG_INFO *langinfo, NJ_DIC_SET *dicset,
        NJ_DIC_INFO *dicinfo_learndic, struct stat *al_dic_stat);
static void iwnn_wlfi_set_download_dicset(int index, WLF_CLASS *wlf, WLF_LANG_INFO *langinfo);
static void iwnn_wlfi_load_download_dic(WLF_CLASS *wlf);
static void iwnn_wlfi_close_download_dic(WLF_CLASS *wlf);
static int count_optional_dictionaries();
static int count_standard_dictionaries();
static int is_optional_dictionary(int type);

static NJ_INT16  iwnn_check_decoemoji(NJ_CLASS *iwnn, NJ_PHASE1_FILTER_MESSAGE *message);
static void iwnn_control_decoemoji_dictionary_i(iWnn_INFO* iwnn, char* id, char* yomi, int hinsi, int control_flag);
static void iwnn_add_decoemoji(WLF_CLASS *wlf, DL_UINT16* id, DL_UINT16* yomi, int hinsi);
static void iwnn_delete_decoemoji(WLF_CLASS *wlf, DL_UINT16* id, DL_UINT16* yomi, int hinsi);

/***********************************************************************
 * definitions
 ***********************************************************************/
/** Configuration file keywords */
#define NAME_DIC_CONF "[DIC_CONF]"                  /**< Dictionary Config                          */
#define NAME_DIC_MODE "[DIC_MODE]"                  /**< Dictionary Mode                            */
#define NAME_DIC_CHARSET_SIZE "[DIC_CHARSET_SIZE]"  /**< Flexible character set(SIZE)               */
#define NAME_DIC_CHARSET_FROM "[DIC_CHARSET_FROM]"  /**< Flexible character set(FROM)               */
#define NAME_DIC_CHARSET_TO   "[DIC_CHARSET_TO]"    /**< Flexible character set(TO)                 */
/** Analysis state of the configuration file */
#define PARSE_STATE_DIC_CONF (0)                    /**< Dictionary Config Analysis flag            */
#define PARSE_STATE_DIC_MODE (1)                    /**< Dictionary Mode Analysis flag              */
#define PARSE_STATE_DIC_CHARSET_SIZE (2)            /**< Flexible character set Analysis flag(SIZE) */
#define PARSE_STATE_DIC_CHARSET_FROM (3)            /**< Flexible character set Analysis flag(FROM) */
#define PARSE_STATE_DIC_CHARSET_TO   (4)            /**< Flexible character set Analysis flag(TO)   */
/** Flexible character Maximum String Size(Including the termination character) */
#define CHARSET_FROM_MAX (sizeof(NJ_CHAR)*2)        /**< Flexible character FROM Max String Size    */
#define CHARSET_TO_MAX   (sizeof(NJ_CHAR)*4)        /**< Flexible character TO Max String Size      */

/** Initialization flag */
#define IWNN_DELETE_LRN 1   /**< Initialize Learning Dictionary */
#define IWNN_DELETE_USR 2   /**< Initialize User Dictionary     */

#define WLF_GET_INIT_SIZE   iwnn_wlf_get_init_size
#define WLF_INITIALIZE      iwnn_wlf_initialize
#define WLF_SET_LANG        iwnn_wlf_set_lang
#define WLF_GET_CONVERSION  iwnn_wlf_get_conversion
#define WLF_GET_STRING      iwnn_wlf_get_string
#define WLF_GET_STROKE      iwnn_wlf_get_stroke
#define WLF_LEARN_STRING    iwnn_wlf_learn_string

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = (*env)->FindClass(env, className);
    if (clazz == NULL) {
        LOGE_IF(log_trace, "Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if ((*env)->RegisterNatives(env, clazz, gMethods, numMethods) < 0) {
        LOGE_IF(log_trace, "RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
static int registerNatives(JNIEnv* env)
{
    if (!registerNativeMethods(env, CLASS_PATH_NAME,
                               METHODS, sizeof(METHODS) / sizeof(METHODS[0]))) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * This is called by the VM when the shared library is first loaded.
 */
typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv* env = NULL;

    LOGI("JNI_OnLoad");

    if ((*vm)->GetEnv(vm, &uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        LOGE_IF(log_trace, "ERROR: GetEnv failed");
        goto bail;
    }
    env = uenv.env;

    if (registerNatives(env) != JNI_TRUE) {
        LOGE_IF(log_trace, "ERROR: registerNatives failed");
        goto bail;
    }

    result = JNI_VERSION_1_4;

bail:
    return result;
}


/***********************************************************************
 * functions
 ***********************************************************************/
/*
 *======================================================================
 * common functions
 *======================================================================
 */

/**
 * Return buffer which contains strings (like printf())
 *
 * After use, free memories by free()
 *
 * @param  fmt  format (same as printf())
 * @param  ...
 *
 * @retval  Not NULL   Result string
 * @retval  NULL       Lack of memory
 *
 */
static char *
iwnn_make_message(const char *fmt, ...)
{
    int n, size = 100;
    char *p, *np;
    va_list ap;

    if ((p = malloc(size)) == NULL)
        return NULL;

    while (1) {
        va_start(ap, fmt);
        n = vsnprintf(p, size, fmt, ap);
        va_end(ap);

        if (n > -1 && n < size)
            return p;

        if (n > -1)
            size = n+1;
        else
            size *= 2;

        if ((np = realloc(p, size)) == NULL) {
            free(p);
            return NULL;
        }
        else {
            p = np;
        }
    }
}

/**
 * Convert iWnn error value to Error code
 *
 * After use, free memories by free()
 *
 * @param  err_val  Error value on iWnn
 *
 * @retval  Not NULL  Result
 * @retval  NULL      Lack of memory
 */
static char *
iwnn_err_message(NJ_INT16 err_val)
{
    NJ_INT16 err_code;
    const IWNN_ERR_TABLE *cur = IWNN_ERR_CODE_TABLES;
    char *message = NULL;
    int i;

    err_code = NJ_GET_ERR_CODE(err_val);

    for (i = 0; i < IWNN_ERR_CODES; i++) {
        if (cur->code == err_code) {
            message = iwnn_make_message("%s[0x%02x]", cur->name, err_val);
            break;
        }
        cur++;
    }
    if (message == NULL) {
        message = iwnn_make_message("Unknown[0x%02x]", err_val);
    }
    return message;
}

/**
 * Read binary file onto heap memory
 *
 * @param [in]   dicb     file name
 * @param [out]  dicdata  loaded data
 *
 * @retval >=0 size of dicdata
 * @retval <0  error
 */
static int read_binary(char *dicb, NJ_UINT8 **dicdata)
{
    FILE *fp = NULL;
    NJ_UINT8 *ptr, buf;
    int fsize;

    /* open as a binary file */
    if ((fp = fopen(dicb, "rb")) == NULL) {
        LOGE_IF(log_trace, "read_binary() Input File %s file Cannot Open", dicb);
        return -1;
    }

    /* get the file size */
    if (fseek(fp, 0, SEEK_END) < 0) { /* goto the end of the file */
        LOGE_IF(log_trace, "read_binary() Seek Error %s", dicb);
        fclose(fp);
        return -1;
    }
    fsize = ftell(fp);   /* get the size */

    if (fsize <= 0) {
        fclose(fp);
        return -1;
    }

    /* allocate memory for loading the file */
    ptr = (NJ_UINT8 *)malloc(fsize);
    if (ptr == NULL) {
        LOGE_IF(log_trace, "read_binary() Memory Allocate Error");
        fclose(fp);
        return -1;
    }

    if (fseek(fp, 0, SEEK_SET) < 0) { /* move file cursor to the top */
        LOGE_IF(log_trace, "read_binary() Seek Error(2) %s", dicb);
        fclose(fp);
        free(ptr);
        return -1;
    }

    *dicdata = ptr;

    /* read the file */
    while(!feof(fp)) {
        if (fread(&buf, 1, 1, fp) <= 0) {
            break;
        }
        if (feof(fp) || ferror(fp)) {
            break;
        }
        *ptr = buf;
        ptr++;
    }
    fclose(fp);

    return(fsize);
}

/***********************************************************************
 * APIs
 **********************************************************************/
/**
 * Constructor
 *
 * @return instance of iWnn_INFO (NULL if error)
 */
jint iwnn_get_info(JNIEnv *env, jclass clazz)
{
    iWnn_INFO *     iwnn;
    WLF_UINT8 *     wlf;
    WLF_UINT32      size;
    WLF_INT16       result;

    iwnn = (iWnn_INFO *)malloc(sizeof(iWnn_INFO));
    if (iwnn == NULL) {
        return (jint)NULL;
    }

    size = WLF_GET_INIT_SIZE();

    wlf = (WLF_UINT8*)malloc(size);
    if (wlf == NULL) {
        free(iwnn);
        return (jint)NULL;
    }

    result = WLF_INITIALIZE(wlf, WLF_CHAR_TYPE_UTF16BE);
    if (result != 0) {
        if (((WLF_CLASS *)wlf)->option.aip_work != NULL) {
            free(((WLF_CLASS *)wlf)->option.aip_work);
            ((WLF_CLASS *)wlf)->option.aip_work = NULL;
        }
        free(wlf);
        free(iwnn);
        return (jint)NULL;
    }

    iwnn->body = (void *)wlf;

    ((WLF_CLASS *)wlf)->input_stroke[0] = NJ_CHAR_NUL;

    return (jint)iwnn;
}

/**
 * Set the active language
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     no    language number
 *
 * @retval >0 success
 * @retval  0  error
 */
jint iwnn_set_active_lang(JNIEnv *env, jclass clazz, jint iwnnp, jint no)
{
    WLF_CLASS *wlf;
    WLF_INT16 result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if (iwnn == NULL || iwnn->body == NULL) {
        return 0;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    LOGI_IF(log_trace, "iwnn_set_active_lang -- (%d)=>(%d)\n", wlf->lang, no);

    result = WLF_SET_LANG(iwnn->body, no);
    if (result < 0) {
        LOGE_IF(log_trace, "iwnn_set_active_lang -- failed\n");
        return 0;
    }

    return 1;
}

/**
 * Switch the type of dictionaries
 *
 * @param  env   JNI env
 * @param  clazz Class object
 * @param  iwnnp iWnn_INFO instance
 * @param  bookshelf  Type of dictionary
 *
 * @retval  1  success
 * @retval -1  error
 */
jint iwnn_set_bookshelf(JNIEnv *env, jclass clazz, jint iwnnp, jint bookshelf)
{
    WLF_CLASS       *wlf;
    WLF_LANG_INFO   *langinfo;
    WLF_SEARCH_INFO *search_info;
    int              i;
    iWnn_INFO       *iwnn = (iWnn_INFO*)iwnnp;

    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    /* Cast to WLF_CLASS */
    wlf = (WLF_CLASS *)iwnn->body;

    LOGI_IF(log_trace, "iwnn_set_bookshelf -- (%d)=>(%d) lang=%d\n", wlf->bookshelf, bookshelf, wlf->lang);

    langinfo = iwnn_wlfi_find_langinfo(wlf, wlf->lang);
    if (langinfo == NULL) {
        /* Case: try to use a unmounted dictionary */
        LOGI("iwnn_set_bookshelf -- failed lang=%d\n", wlf->lang);
        return -1;
    }

    /* Set a type of dictionaries */
    wlf->bookshelf = bookshelf;

    /* Copy a dictionary set for the language */
    wlf->dicset = langinfo->dicset;
    for (i = 0; i < NJ_MAX_DIC; i++) { // include Additional dictionary
        WLF_DIC_PROP *dicprop = &(langinfo->dicprops[i]);
        if (dicprop->mode[bookshelf] == 0) {
            wlf->dicset.dic[i].handle = NULL;
        }
    }

    /* Language changed, so download dictionary set to wlf->dicset */
    if ((bookshelf != WLF_DICSET_TYPE_USER_DIC) && (bookshelf != WLF_DICSET_TYPE_LEARN_DIC)) {    
        for (i = 0; i < IWNN_DL_DIC_MAX; i++) {
            iwnn_wlfi_set_download_dicset(i, wlf, langinfo);
        }
    }

    if (log_trace) {
        for (i = IWNN_ADD_DIC_DICSET_TOPINDEX; i < count_optional_dictionaries(); i++) {
            LOGD(" -- add[%d]=%x\n", i, (unsigned int)wlf->dicset.dic[i].handle);
        }
    }

    if (bookshelf < WLF_DICSET_TYPE_USER_DIC) {
        NJ_SET_ENV(wlf->iwnn, wlf->dicset);
    }
    else {
        search_info = &(wlf->search_info);
        search_info->dicset = wlf->dicset;
    }


    return 1;
}

/**
 * Unmount the dictionaries
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 *
 * @retval >0  success
 * @retval  0  error
 */
jint iwnn_unmount_dics(JNIEnv *env, jclass clazz, jint iwnnp)
{
    WLF_CLASS *wlf;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if (iwnn == NULL || iwnn->body == NULL) {
        return 0;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    iwnn_wlf_close_dictionary(wlf);

    return 1;
}

/**
 * Destractore
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 */
void iwnn_destroy(JNIEnv *env, jclass clazz, jint iwnnp)
{
    WLF_CLASS *wlf;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    WLF_INT16 result;

    if (iwnn != NULL) {
        wlf = (WLF_CLASS *)iwnn->body;
        if (wlf != NULL) {
            if (wlf->demoji_info.demojidic != NULL) {
                result = demoji_quit( &wlf->demoji_info );
                if( result != DL_ERROR_NONE ) {
                    LOGE_IF(log_trace, "iwnn_destroy() -- failed to demoji_quit() : result=%d\n", (int)result);
                }
            }
            if (wlf->option.aip_work != NULL) {
                free(wlf->option.aip_work);
                wlf->option.aip_work = NULL;
            }
            if (wlf->data_area_path != NULL) {
                free(wlf->data_area_path);
                wlf->data_area_path = NULL;
            }

            iwnn_wlf_close_dictionary(wlf);
            free(wlf);
        }
        free(iwnn);
    }
}

/*
 *======================================================================
 * kana-kanji conversion APIs
 *======================================================================
 */

/**
 * Set input string
 *
 * @param[in]     env     JNI env
 * @param[in]     clazz   Class object
 * @param[in,out] iwnnp   iWnn_INFO instance
 * @param[in]     string  input string
 *
 * @return byte length of str
 */
jint iwnn_set_input(JNIEnv *env, jclass clazz, jint iwnnp, jstring string)
{
    char *str = (char *)0;
    int ret;

    if (string) {
        str = (char *)(*env)->GetStringUTFChars(env, string, 0);
        if (!str) return 0;
    }

    ret = iwnn_set_input_i((iWnn_INFO *)iwnnp, str);

    if (str) (*env)->ReleaseStringUTFChars(env, string, (const char *)str);
    return ret;
}

/**
 * Set input string
 *
 * @param[in,out] iwnn  iWnn_INFO instance
 * @param[in]     str   input string
 *
 * @return byte length of str
 */
int iwnn_set_input_i(iWnn_INFO* iwnn, char* str)
{
    WLF_CLASS *wlf;
    WLF_INT32 result;

    if (iwnn == NULL || iwnn->body == NULL) {
        return 0;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    result = iwnn_wlfi_convert_internal(wlf, (WLF_UINT8*)str, wlf->input_stroke, sizeof(wlf->input_stroke));
    if (result <= 0) {
        if (result < 0) LOGE_IF(log_trace, "iwnn_set_input: str=0x%x,result=%d\n", (unsigned int)str, (int)result);
        LOGI_IF(log_trace, "iwnn_set_input(%p,%p)=0 result=%d\n", iwnn, str, (int)result);
        return 0;
    }

    LOGI_IF(log_trace, "iwnn_set_input(%p,%p)=%d\n", iwnn, str, (int)result);
    return (int)result;
}

/**
 * Get input string
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 *
 * @return input string (NULL if error)
 */
jstring iwnn_get_input(JNIEnv *env, jclass clazz, jint iwnnp)
{
    WLF_CLASS *wlf;
    WLF_INT32 result;
    char *ret_str;
    jstring jresult = 0;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if (iwnn == NULL || iwnn->body == NULL) {
        return NULL;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    result = iwnn_wlfi_convert_external(wlf, wlf->input_stroke, (WLF_UINT8*)iwnn->strbuf, sizeof(iwnn->strbuf));
    if (result <= 0) {
        if (result < 0) LOGE_IF(log_trace, "iwnn_get_input: result=%d\n", (int)result);
        LOGI_IF(log_trace, "iwnn_get_input(%p)=NULL result=%d\n", iwnn, (int)result);
        return NULL;
    }

    LOGI_IF(log_trace, "iwnn_get_input(%p)=%p result=%d \n", iwnn, iwnn->strbuf, (int)result);
    ret_str = iwnn->strbuf;
    if (ret_str) jresult = (*env)->NewStringUTF(env, (const char *)ret_str);
    return jresult;
}

/**
 * Get dictionary sets from the configuration file
 *
 * @param[in]     env     JNI env
 * @param[in]     clazz   Class object
 * @param[in,out] iwnnp   iWnn_INFO instance
 * @param[in]     file_name  Name of config file
 * @param[in]     lang    Index number of dictionaries
 *
 * @retval  1  success
 * @retval  0  fail
 */
jint iwnn_setdic_by_conf(JNIEnv *env, jclass clazz, jint iwnnp, jstring file_name, jint lang)
{
    char *str = (char *)0;
    int ret;

    if (file_name) {
        str = (char *)(*env)->GetStringUTFChars(env, file_name, 0);
    }

    if (!str) return 0;

    ret = iwnn_setdic_by_conf_i((iWnn_INFO *)iwnnp, str, lang);

    (*env)->ReleaseStringUTFChars(env, file_name, (const char *)str);
    return ret;
}

/**
 * Get dictionary sets from the configuration file
 *
 * @param  iwnn       iWnn_INFO instance
 * @param  conf_file  Configuration file
 * @param  lang       Number of language connected with dic sets
 *
 * @retval  1  success
 * @retval  0  fail
 */
int iwnn_setdic_by_conf_i(iWnn_INFO* iwnn, char* conf_file, int lang)
{
    WLF_INT16 result;
    WLF_CLASS *wlf;
    WLF_LANG_INFO *langinfo;
    NJ_DIC_SET *dicset;

    if (iwnn == NULL || iwnn->body == NULL) {
        return 0;
    }

    result = iwnn_wlf_load_lang(iwnn->body, (WLF_UINT8)lang, conf_file);
    if (result < 0) {
        LOGE_IF(log_trace, "iwnn_setdic_by_conf(%p,\"%s\",%d)=0 result=%d\n", iwnn, conf_file, lang, (int)result);
        return 0;
    }

    /* Cast to WLF_CLASS */
    wlf = (WLF_CLASS *)iwnn->body;

    WLF_SET_INIT_PREDICT(&(wlf->predict_info));
    if ((lang != WLF_LANG_TYPE_JP) && (lang != WLF_LANG_TYPE_ZHCN) && (lang != WLF_LANG_TYPE_ZHTW)) {
        wlf->predict_info.mode = NJ_NO_RENBUN;
        LOGI_IF(log_trace, "iwnn_setdic_by_conf() lang = [%d] predict_info.mode = NJ_NO_RENBUN\n",lang);
    }
    /* TANBUNSETSU HENKAN is not necessary in Chinese suggestion.   */
    /* Because the iWnn Engine behaves the following.               */
    /* 1. Input text is "qwerty"                                    */
    /* 2. Select this word.                                         */
    /* 3. Input "q", and then next suggestion becomes "qw"          */
    /* 4. Select this word.                                         */
    /* 5. Get suggesion is "erty".                                  */
    /*                                                              */
    /* "er" is able to convert to "��".                             */
    /* The iWnn Engine interpret the meaning of "qwerty"            */
    /* as split words ("qw" "er" "ty").                             */
    /* The engine will create suggestion as "qw" "erty",            */
    /* if no NJ_NO_TANBUN.                                          */

    /* Always no tanbun conversion */
    wlf->predict_info.mode |= NJ_NO_TANBUN;

    if (wlf->demoji_info.demojidic != NULL) {
        if (lang == WLF_LANG_TYPE_JP) {
            wlf->demoji_info.iwnn = (DL_UINT8 *)(&wlf->iwnn);
            wlf->demoji_info.basicdics_cnt = 0;
            langinfo = iwnn_wlfi_find_langinfo(wlf,(WLF_UINT8)lang);
            if (langinfo == NULL) {
                LOGE_IF(log_trace, "iwnn_setdic_by_conf() -- langinfo=NULL\n");
                return 0;
            }
            dicset = &langinfo->dicset;
            wlf->demoji_info.ruledic_h = (DL_UINT8*)dicset->rHandle[0];
            result = demoji_init(&(wlf->demoji_info));
            if((result != DL_ERROR_NONE) && (result != DL_ERROR_INITIALIZED)) {
                LOGE_IF(log_trace, "iwnn_setdic_by_conf() -- failed to demoji_init() : result=%d\n", (int)result);
                return 0;
            }
        }
    }

    LOGI_IF(log_trace, "iwnn_setdic_by_conf(%p,\"%s\",%d)=1\n", iwnn, conf_file, lang);
    return 1;
}

/**
 * Initialize
 *
 * This function invokes njx_init().
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     dir_path iWnn user data directory path
 *
 * @retval 1  success
 * @retval 0  fail
 */
jint iwnn_init(JNIEnv *env, jclass clazz, jint iwnnp, jstring dir_path)
{
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    WLF_CLASS *wlf;
    NJ_INT16 result;
    char *str;
    int dir_path_len;

    if (iwnn == NULL || iwnn->body == NULL || dir_path == NULL) {
        return 0;
    }
    
    wlf = (WLF_CLASS *)iwnn->body;

    wlf->mode = WLF_MODE_INIT;
    wlf->input_stroke[0] = NJ_CHAR_NUL;

    if (wlf->data_area_path) {
        free(wlf->data_area_path);
    }
    str = (char *)(*env)->GetStringUTFChars(env, dir_path, 0);
    if (str == NULL) {
        return 0;
    }
    dir_path_len = strlen(str) + 1;
    if (dir_path_len > WLF_FILE_PATH_LEN) {
        (*env)->ReleaseStringUTFChars(env, dir_path, (const char *)str);
        return 0;
    }
    wlf->data_area_path = (char*)malloc(dir_path_len);
    if (wlf->data_area_path == NULL) {
        (*env)->ReleaseStringUTFChars(env, dir_path, (const char *)str);
        return 0;
    }
    strncpy(wlf->data_area_path, str, dir_path_len);
    (*env)->ReleaseStringUTFChars(env, dir_path, (const char *)str);

    /* Initialize candidates */
    WLF_SET_INIT_CONVERT  (&wlf->conv_info);
    WLF_SET_INIT_CANDIDATE(&wlf->cand_info);

    /* Initialize a engine */
    result = njx_init(&(wlf->iwnn), &(wlf->option));
    if (result < 0) {
        char *err_msg;
        err_msg = iwnn_err_message(result);
        LOGE_IF(log_trace, "iwnn_init(%p)=0 result=%s\n", iwnn, err_msg);
        if (err_msg) free(err_msg);
        return 0;
    }
    LOGI_IF(log_trace, "iwnn_init(%p)=1 result=%d\n", iwnn, result);

    iwnn_wlf_set_state(wlf, NULL);

    return 1;
}

/**
 * Get status prediction settings
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 *
 * @retval  1  success
 * @retval -1  error
 */
jint iwnn_get_state(JNIEnv *env, jclass clazz, jint iwnnp)
{
    WLF_CLASS *wlf;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if ((iwnn == NULL) || (iwnn->body == NULL)) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    return iwnn_wlf_get_state(wlf, &(wlf->state));
}

/**
 * Set status prediction settings
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 *
 * @retval  0 success
 * @retval -1 error
 */
jint iwnn_set_state(JNIEnv *env, jclass clazz, jint iwnnp)
{
    WLF_CLASS *wlf;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if ((iwnn == NULL) || (iwnn->body == NULL)) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    return iwnn_wlf_set_state(wlf, &(wlf->state));
}

/**
 * Update situation predicting bias values
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp  iWnn_INFO instance
 * @param[in]     situation  Time, Season
 * @param[in]     bias  Bias value
 */
void iwnn_set_state_system(JNIEnv *env, jclass clazz, jint iwnnp, jint situation, jint bias)
{
    WLF_CLASS *wlf;
    NJ_STATE *state;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if ((iwnn == NULL) || (iwnn->body == NULL)) {
        return;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    state = &(wlf->state);

    state->system[situation] = (NJ_INT16)bias;
}

/**
 * Convert the input string
 *
 * This function invokes njx_analyze()
 *
 * @param[in]     env     JNI env
 * @param[in]     clazz   Class object
 * @param[in,out] iwnnp   iWnn_INFO instance
 * @param[in]     minLen  FunFun min limit ( 0 : no limit)
 * @param[in]     maxLen  FunFun max limit (-1 : no limit)
 * @param[in]     headConv  Head conversion flag (0 : don't head conversion 1 : do head conversion)
 *
 * @retval 1  success
 * @retval 0  fail
 */
jint iwnn_forecast(JNIEnv *env, jclass clazz, jint iwnnp, jint minLen, jint maxLen, jint headConv)
{
    WLF_CLASS *wlf;
    WLF_GIJI_INFO *giji_info;
    WLF_INT16 result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if (iwnn == NULL || iwnn->body == NULL) {
        return 0;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    giji_info = &(wlf->giji_info);

    memcpy(giji_info->giji_stroke, wlf->input_stroke, sizeof(giji_info->giji_stroke));
    if (0 <= maxLen) {
        giji_info->giji_stroke[maxLen] = 0;
    }

    wlf->headconv = headConv;
    result = WLF_GET_CONVERSION(iwnn->body, WLF_MODE_PREDICTION, NULL, 0, (WLF_INT32)minLen, (WLF_INT32)maxLen);
    if (result < 0) {
        LOGE_IF(log_trace, "iwnn_forecast: minLen=%d,maxLen=%d,result=%d\n", minLen, maxLen, (int)result);
        return 0;
    }
    LOGI_IF(log_trace, "iwnn_forecast(%p,%d,%d)=%d\n", iwnn, minLen, maxLen, (int)result);
    return (int)result;
}

/**
 * Convert a consecutive clause
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp       iWnn_INFO instance
 * @param[in]     devide_pos  Divide position of each clauses
 *
 * @return  Number of clauses
 */
jint iwnn_conv(JNIEnv *env, jclass clazz, jint iwnnp, jint devide_pos)
{
    WLF_CLASS *wlf;
    WLF_GIJI_INFO *giji_info;
    WLF_INT16 result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if (iwnn == NULL || iwnn->body == NULL) {
        return 0;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    giji_info = &(wlf->giji_info);

    memcpy(giji_info->giji_stroke, wlf->input_stroke, sizeof(giji_info->giji_stroke));
    if (0 < devide_pos) {
        giji_info->giji_stroke[devide_pos] = 0;
    }

    result = WLF_GET_CONVERSION(iwnn->body, WLF_MODE_CONVERSION, NULL, (WLF_UINT8)devide_pos, 0, -1);
    if (result <= 0) {
        LOGE_IF(log_trace, "iwnn_conv: devide_pos=%d,result=%d\n", devide_pos, (int)result);
        return 0;
    }
    LOGI_IF(log_trace, "iwnn_conv(%p,%d)=%d\n", iwnn, devide_pos, (int)result);
    return (int)result;
}

/**
 * Make no conversion candidate
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 *
 * @retval     1  Success
 * @retval  <  0  Failure
 */
jint iwnn_noconv(JNIEnv *env, jclass clazz, jint iwnnp)
{
    WLF_CLASS *wlf;
    int ret = 0;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if (iwnn == NULL || iwnn->body == NULL) {
        return 0;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    WLF_SET_INIT_CONVERT(&wlf->conv_info);
    WLF_SET_INIT_CANDIDATE(&wlf->cand_info);
    wlf->mode = WLF_MODE_CONVERSION;
    wlf->cand_info.segment_cnt = 1;

    ret = (int)njx_get_stroke_word(&(wlf->iwnn), wlf->input_stroke, wlf->cand_info.segment);
    if (ret < 1) {
        LOGE_IF(log_trace, "iwnn_noconv: result=%d\n", ret);
        return 0;
    }
    LOGI_IF(log_trace, "iwnn_noconv(%p)=%d\n", iwnn, ret);
    return ret;
}

/**
 * Get converted word string
 *
 * This function invokes njx_analyze(), njx_zenkouho() and njx_get_candidate().
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     segment  segment number
 * @param[in]     cand     candidate number
 *
 * @return word string (NULL if error)
 */
jstring iwnn_get_word_string(JNIEnv *env, jclass clazz, jint iwnnp, jint segment, jint cand)
{
    WLF_INT16 result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    char *ret_str;
    jstring jresult = 0;

    LOGI_IF(log_trace, "iwnn_get_word_string -- segment=%d,cand=%d\n", segment, cand);

    if (iwnn == NULL || iwnn->body == NULL) {
        return NULL;
    }

    result = WLF_GET_STRING(iwnn->body, (WLF_UINT16)segment, (WLF_UINT16)cand, (WLF_UINT8 *)iwnn->strbuf, sizeof(iwnn->strbuf));
    if (result <= 0) {
        if (result < 0) LOGE_IF(log_trace, "iwnn_get_word_string: segment=%d,cand=%d,result=%d\n", segment, cand, (int)result);
        LOGI_IF(log_trace, "iwnn_get_word_string(%p,%d,%d)=NULL result=%d\n", iwnn, segment, cand, (int)result);
        return NULL;
    }
    LOGI_IF(log_trace, "iwnn_get_word_string(%p,%d,%d)=%p\n", iwnn, segment, cand, iwnn->strbuf);
    ret_str = iwnn->strbuf;
    if (ret_str) jresult = (*env)->NewStringUTF(env, (const char *)ret_str);
    return jresult;
}

/**
 * Get converted word's stroke
 *
 * This function invokes njx_analyze(), njx_zenkouho() and njx_get_stroke().
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     segment  segment number
 * @param[in]     cand     candidate number
 *
 * @return word string (NULL if error)
 */
jstring iwnn_get_word_stroke(JNIEnv *env, jclass clazz, jint iwnnp, jint segment, jint cand)
{
    WLF_INT16 result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    char *ret_str;
    jstring jresult = 0;

    if (iwnn == NULL || iwnn->body == NULL) {
        return NULL;
    }

    result = WLF_GET_STROKE(iwnn->body, (WLF_UINT16)segment, (WLF_UINT16)cand, (WLF_UINT8 *)iwnn->strbuf, sizeof(iwnn->strbuf));
    if (result <= 0) {
        if (result < 0) LOGE_IF(log_trace, "iwnn_get_word_stroke: segment=%d,cand=%d,result=%d\n", segment, cand, (int)result);
        LOGI_IF(log_trace, "iwnn_get_word_stroke(%p,%d,%d)=NULL result=%d\n", iwnn, segment, cand, (int)result);
        return NULL;
    }
    LOGI_IF(log_trace, "iwnn_get_word_stroke(%p,%d,%d)=%p\n", iwnn, segment, cand, iwnn->strbuf);
    ret_str = iwnn->strbuf;
    if (ret_str) jresult = (*env)->NewStringUTF(env, (const char *)ret_str);
    return jresult;
}

/**
 * Get a candidate clause from consecutive clauses
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp    iWnn_INFO instance
 * @param[in]     segment  Position of a clause
 *
 * @retval  !NULL  Candidates exist
 * @retval   NULL  No candidate
 */
jstring iwnn_get_segment_string(JNIEnv *env, jclass clazz, jint iwnnp, jint segment)
{
    WLF_CLASS *wlf;
    NJ_INT16 ret;
    NJ_CHAR buf[NJ_MAX_LEN+NJ_TERM_LEN];
    WLF_INT32 result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    char *ret_str;
    jstring jresult = 0;

    if (iwnn == NULL || iwnn->body == NULL) {
        return NULL;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    if (wlf->mode != WLF_MODE_CONVERSION) {
        return NULL;
    }

    /* check the segment number */
    if (segment >= wlf->cand_info.segment_cnt) {
        return NULL;
    }

    ret = njx_get_candidate(&(wlf->iwnn), &(wlf->cand_info.segment[segment]), buf, sizeof(buf));
    if (ret <= 0) {
        return NULL;
    }

    result = iwnn_wlfi_convert_external(wlf, buf, (WLF_UINT8 *)iwnn->strbuf, sizeof(iwnn->strbuf));
    if (result <= 0) {
        return NULL;
    }

    ret_str = iwnn->strbuf;
    if (ret_str) jresult = (*env)->NewStringUTF(env, (const char *)ret_str);
    return jresult;
}

/**
 * Get a clause's reading string from consecutive clauses
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     segment  Position of a clause
 *
 * @retval  !NULL  Reading strings exist
 * @retval   NULL  No reading string
 */
jstring iwnn_get_segment_stroke(JNIEnv *env, jclass clazz, jint iwnnp, jint segment)
{
    WLF_CLASS *wlf;
    NJ_INT16 ret;
    NJ_CHAR buf[NJ_MAX_LEN+NJ_TERM_LEN];
    WLF_INT32 result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    char *ret_str;
    jstring jresult = 0;

    if (iwnn == NULL || iwnn->body == NULL) {
        return NULL;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    if (wlf->mode != WLF_MODE_CONVERSION) {
        return NULL;
    }

    /* check the segment number */
    if (segment >= wlf->cand_info.segment_cnt) {
        return NULL;
    }

    ret = njx_get_stroke(&(wlf->iwnn), &(wlf->cand_info.segment[segment]), buf, sizeof(buf));
    if (ret <= 0) {
        return NULL;
    }

    result = iwnn_wlfi_convert_external(wlf, buf, (WLF_UINT8 *)iwnn->strbuf, sizeof(iwnn->strbuf));
    if (result <= 0) {
        return NULL;
    }

    ret_str = iwnn->strbuf;
    if (ret_str) jresult = (*env)->NewStringUTF(env, (const char *)ret_str);
    return jresult;
}

/**
 * @brief Get a segment string length
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     segment  Position of a clause
 *
 * @return word's stroke length (-1 if error)
 */
jint iwnn_get_segment_stroke_length(JNIEnv *env, jclass clazz, jint iwnnp, jint segment)
{
    WLF_CLASS *wlf;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    WLF_INT16 result;

    if (iwnn == NULL || iwnn->body == NULL || segment < 0) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    if (segment < wlf->cand_info.segment_cnt) {
        result = NJ_GET_YLEN_FROM_STEM_EXT(&wlf->cand_info.segment[segment].word) 
                    + NJ_GET_YLEN_FROM_FZK(&wlf->cand_info.segment[segment].word);
    } else {
        result = -1;
    }

    if (result <= 0) {
        LOGI_IF(log_trace, "iwnn_get_segment_stroke_length: segment=%d,result=%d\n", segment, (int)result);
        return -1;
    }

    return result;
}

/**
 * @brief Get a reading string length
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     cand     candidate number
 *
 * @return word's stroke length (-1 if error)
 */
jint iwnn_get_word_stroke_length(JNIEnv *env, jclass clazz, jint iwnnp, jint cand)
{
    WLF_CLASS *wlf;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    WLF_INT16 result = 0;
    WLF_UINT16 candidate_cnt = 0;

    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    candidate_cnt = wlf->cand_info.candidate_cnt + wlf->giji_info.giji_cnt;

    /* argument check */
    if (cand < 0 || candidate_cnt <= cand) {
        return -1;
    }

    if (cand < wlf->cand_info.candidate_cnt) {
        if ((NJ_GET_RESULT_OP(wlf->cand_info.candidate[cand].operation_id) == NJ_OP_ANALYZE)
                && (NJ_GET_RESULT_FUNC(wlf->cand_info.candidate[cand].operation_id) == NJ_FUNC_ZENKOUHO_HEAD)) {
            result = NJ_GET_YLEN_FROM_STEM_EXT(&wlf->cand_info.candidate[cand].word) 
                    + NJ_GET_YLEN_FROM_FZK(&wlf->cand_info.candidate[cand].word);
        } else {
            result = nj_strlen(wlf->input_stroke);
        }
    } else {
        cand -= wlf->cand_info.candidate_cnt;
        if ((NJ_GET_RESULT_OP(wlf->giji_info.giji_candidate[cand].operation_id) == NJ_OP_ANALYZE)
                && (NJ_GET_RESULT_FUNC(wlf->giji_info.giji_candidate[cand].operation_id) == NJ_FUNC_ZENKOUHO_HEAD)) {
            result = NJ_GET_YLEN_FROM_STEM_EXT(&wlf->giji_info.giji_candidate[cand].word) 
                    + NJ_GET_YLEN_FROM_FZK(&wlf->giji_info.giji_candidate[cand].word);
        } else {
            result = nj_strlen(wlf->input_stroke);
        }
    }

    if (result < 0) {
        LOGE_IF(log_trace, "iwnn_get_word_stroke_length: result=%d\n", (int)result);
        return -1;
    }

    return result;
}

/**
 * Select a candidate
 *
 * This function invokes njx_select()
 *
 * @param[in]     env      JNI env
 * @param[in]     clazz    Class object
 * @param[in,out] iwnnp    iWnn_INFO instance
 * @param[in]     segment  segment number
 * @param[in]     cand     candidate number
 * @param[in]     mode     0x01:add to learn dict, 0x80:connect
 *
 * @retval  1  success
 * @retval  0  error
 */
jint iwnn_select(JNIEnv *env, jclass clazz, jint iwnnp, jint segment, jint cand, jint mode)
{
    WLF_CLASS *wlf;
    WLF_INT16 result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if (iwnn == NULL || iwnn->body == NULL) {
        return 0;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    wlf->connect = (mode & 0x80) ? 1 : 0;
    wlf->learn = (mode & 0x01) ? 1 : 0;

    result = WLF_LEARN_STRING(iwnn->body, (WLF_INT32)segment, (WLF_INT32)cand);
    if (result < 0) {
        LOGE_IF(log_trace, "iwnn_select(%p,%d,%d,0x%x)=0 result=%d\n", iwnn, segment, cand, mode, (int)result);
        return 0;
    }

    LOGI_IF(log_trace, "iwnn_select(%p,%d,%d,0x%x)=%d\n", iwnn, segment, cand, mode, (int)result);
    return 1;
}


/**
 * Search a word
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnn    iWnn_INFO instance
 * @param[in]     method  Searching method
 * @param[in]     order   Searching order
 *
 * @retval  0  No data
 * @retval  1  Data exist
 * @retval <0  error
 */
jint iwnn_search_word(JNIEnv *env, jclass clazz, jint iwnnp, jint method, jint order)
{
    WLF_CLASS * wlf;
    WLF_SEARCH_INFO *search_info;
    NJ_CURSOR * cursor;
    NJ_INT16    result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;


    if (iwnn == NULL || iwnn->body == NULL) {
        return WLF_ERR_INVALID_PARAM;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    search_info = &(wlf->search_info);

    search_info->iwnn = wlf->iwnn;
    search_info->iwnn.dic_set = search_info->dicset;

    cursor = &(search_info->cursor);

    search_info->search_cnt = 0;

    memset(&(search_info->cursor), 0, sizeof(search_info->cursor));
    cursor->cond.operation = (NJ_UINT8)method;
    cursor->cond.mode = (NJ_UINT8)order;
    cursor->cond.ds = &(search_info->dicset);
    cursor->cond.yomi = wlf->input_stroke;
    cursor->cond.kanji = NULL;
    cursor->cond.charset = NULL;

    result = njx_search_word(&(search_info->iwnn), cursor);
    if (result < 0) {
        char *err_msg;
        err_msg = iwnn_err_message(result);
        LOGE_IF(log_trace, "%s(%d,%d)=%s\n", __func__, method, order, err_msg);
        if (err_msg) free(err_msg);
        return -1;
    }

    return (int)result;
}

/**
 * Get a word
 *
 * @param[in] env    JNI env
 * @param[in] clazz  Class object
 * @param[in] iwnnp  iWnn_INFO instance
 * @param[in] index  Index of a candidate
 * @param[in] type   Reading(WLF_WORD_STROKE)/Display(WLF_WORD_CANDIDATE)
 *
 * @retval !NULL  Words exist
 * @retval  NULL  No word
 */
jstring iwnn_get_word(JNIEnv *env, jclass clazz, jint iwnnp, jint index, jint type)
{
    WLF_CLASS * wlf;
    WLF_SEARCH_INFO *search_info;
    int         s_cnt, r_cnt;
    NJ_CHAR     buf[NJ_MAX_LEN+NJ_TERM_SIZE];
    NJ_INT16    result;
    WLF_INT16   length;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    char *ret_str;
    jstring jresult = 0;

    if (iwnn == NULL || iwnn->body == NULL) {
        return NULL;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    search_info = &(wlf->search_info);

    if (search_info->search_cnt <= index) {
        s_cnt = (index + 1) - search_info->search_cnt;
        r_cnt = iwnn_wlf_get_word(wlf, s_cnt);
        if (r_cnt < 0) {
            return NULL;
        }
        if (s_cnt != r_cnt) {
            return NULL;
        }
    }

    if (index >= NJ_MAX_USER_COUNT) {
        return NULL;
    }

    if (type == WLF_WORD_STROKE) {
        result = njx_get_stroke(&(search_info->iwnn), &(search_info->search_word[index]), buf, sizeof(buf));
    }
    else {
        result = njx_get_candidate(&(search_info->iwnn), &(search_info->search_word[index]), buf, sizeof(buf));
    }

    if (result <= 0) {
        return NULL;
    }

    length = iwnn_wlfi_convert_external(wlf, buf, (WLF_UINT8 *)iwnn->strbuf, sizeof(iwnn->strbuf));
    if (length <= 0) {
        return NULL;
    }

    ret_str = iwnn->strbuf;
    if (ret_str) jresult = (*env)->NewStringUTF(env, (const char *)ret_str);
    return jresult;
}

/**
 * Regiter a word
 *
 * @param[in,out] iwnn     iWnn_INFO instance
 * @param[in]     yomi     String (Reading)
 * @param[in]     repr     String (Candidate)
 * @param[in]     group    Group number of lexical category
 * @param[in]     dtype    Dictionary type
 * @param[in]     con      Relational learning flag
 *
 * @retval        >=0      Success
 * @retval        <0       Failure
 */
jint iwnn_add_word(JNIEnv *env, jclass clazz, jint iwnnp, jstring yomi, jstring repr, jint group, jint dtype, jint con)
{
    char *yomi_str = (char *)0;
    char *repr_str = (char *)0;
    int ret;

    if (yomi) {
        yomi_str = (char *)(*env)->GetStringUTFChars(env, yomi, 0);
        if (!yomi_str) return 0;
    }

    if (repr) {
        repr_str = (char *)(*env)->GetStringUTFChars(env, repr, 0);
        if (!repr_str) {
            if (yomi_str) (*env)->ReleaseStringUTFChars(env, yomi, (const char *)yomi_str);
            return 0;
        }
    }

    ret = iwnn_add_word_i((iWnn_INFO *)iwnnp, yomi_str, repr_str, group, dtype, con);

    if (yomi_str) (*env)->ReleaseStringUTFChars(env, yomi, (const char *)yomi_str);
    if (repr_str) (*env)->ReleaseStringUTFChars(env, repr, (const char *)repr_str);
    return ret;
}

/**
 * Regiter a word
 *
 * @param[in,out] iwnn     iWnn_INFO instance
 * @param[in]     yomi     String (Reading)
 * @param[in]     repr     String (Candidate)
 * @param[in]     group    Group number of lexical category
 * @param[in]     dtype    Dictionary type
 * @param[in]     con      Relational learning flag
 *
 * @retval        >=0      Success
 * @retval        <0       Failure
 */
int iwnn_add_word_i(iWnn_INFO* iwnn, char* yomi, char* repr, int group, int dtype, int con)
{
    WLF_CLASS *wlf;
    WLF_SEARCH_INFO *search_info;
    NJ_WORD_INFO winfo;
    NJ_INT16 ret;

    if (iwnn == NULL) {
        LOGE_IF(log_trace, "iwnn_add_word() : param(iWnn_INFO) is not found.\n");
        return WLF_ERR_INVALID_PARAM;
    }

    if (yomi == NULL) {
        LOGE_IF(log_trace, "iwnn_add_word() : param(yomi) is not found.\n");
        return WLF_ERR_INVALID_PARAM;
    }

    if (repr == NULL) {
        LOGE_IF(log_trace, "iwnn_add_word() : param(repr) is not found.\n");
        return WLF_ERR_INVALID_PARAM;
    }

    memset(&winfo, 0x00, sizeof(NJ_WORD_INFO));

    wlf = (WLF_CLASS *)iwnn->body;
    search_info = &(wlf->search_info);

    search_info->iwnn = wlf->iwnn;

    if ((dtype == WLF_ADD_WORD_DIC_TYPE_LEARN) && (wlf->bookshelf != WLF_DICSET_TYPE_AUTOLEARN_DIC)) {
        /* Use the original dicset to register to a leaning dictionary */
    } else {
        search_info->iwnn.dic_set = search_info->dicset;
    }

    winfo.hinsi_group = (NJ_UINT8)group;
#ifdef NJ_OPT_UTF16
    UTF8_to_UTF16BE((unsigned char *)yomi, (unsigned char *)winfo.yomi, (NJ_MAX_LEN+NJ_TERM_SIZE) * sizeof(NJ_CHAR));
    UTF8_to_UTF16BE((unsigned char *)repr, (unsigned char *)winfo.kouho, (NJ_MAX_RESULT_LEN+NJ_TERM_SIZE) * sizeof(NJ_CHAR));
#endif
    ret = njx_add_word(&(search_info->iwnn), &winfo, dtype, con);
    if (ret < 0) {
        char *err_msg;
        err_msg = iwnn_err_message(ret);
        LOGE_IF(log_trace, "iwnn_add_word(%p)=0 result=%s\n", iwnn, err_msg);
        int err_code = NJ_GET_ERR_CODE(ret);
        switch (err_code) {
        case NJ_ERR_SAME_WORD:
            ret = WLF_ERR_SAME_WORD;
            break;
        case NJ_ERR_USER_DIC_FULL:
            ret = WLF_ERR_USER_DIC_FULL;
            break;
        default:
            /* DO NOTHING */
            break;
        }
        if (err_msg) free(err_msg);
    }

    return ret;
}

/**
 * Delete a word
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     index Index of candidates
 *
 * @retval  1 success
 * @retval -1 error
 */
jint iwnn_delete_word(JNIEnv *env, jclass clazz, jint iwnnp, jint index)
{
    WLF_CLASS *wlf;
    NJ_INT16 result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    WLF_CAND *pcand;

    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    pcand = &(wlf->cand_info);

    if (pcand->candidate_cnt <= index) {
        return -1;
    }

    result = njx_delete_word(&(wlf->iwnn), &(pcand->candidate[index]));
    if (result < 0) {
        char *err_msg;
        err_msg = iwnn_err_message(result);
        LOGE_IF(log_trace, "%s(%d)=%s\n", __func__, index, err_msg);
        if (err_msg) free(err_msg);
        return -1;
    }

    return 1;
}

/**
 * Delete a search word
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     index Index of candidates
 *
 * @retval  1 success
 * @retval -1 error
 */
jint iwnn_delete_search_word(JNIEnv *env, jclass clazz, jint iwnnp, jint index)
{
    WLF_CLASS *wlf;
    WLF_SEARCH_INFO *search_info;
    NJ_INT16 result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;


    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    search_info = &(wlf->search_info);

    if (search_info->search_cnt <= index) {
        return -1;
    }
    result = njx_delete_word(&(search_info->iwnn), &(search_info->search_word[index]));
    if (result < 0) {
        char *err_msg;
        err_msg = iwnn_err_message(result);
        LOGE_IF(log_trace, "%s(%d)=%s\n", __func__, index, err_msg);
        if (err_msg) free(err_msg);
        return -1;
    }

    return 1;
}

/**
 * Get a word
 *
 * @param  wlf  WLF_CLASS instance
 * @param  s_cnt  Get count
 *
 * @retval  0< Number of words
 * @retval -1  error
 */
static int iwnn_wlf_get_word(WLF_CLASS *wlf, int s_cnt)
{
    WLF_SEARCH_INFO *search_info;
    int cnt = 0;
    int i;
    NJ_INT16 result;


    search_info = &(wlf->search_info);

    for (i = 0; i < s_cnt; i++) {

        result = njx_get_word(&(search_info->iwnn), &(search_info->cursor), &(search_info->search_word[search_info->search_cnt]));
        if (result < 0) {
            char *err_msg;
            err_msg = iwnn_err_message(result);
            LOGE_IF(log_trace, "%s(%d)=%s\n", __func__, s_cnt, err_msg);
            if (err_msg) free(err_msg);
            return -1;
        }
        else if (result == 0) {
            break;
        }

        cnt++;
        search_info->search_cnt++;
    }
    return cnt;
}

/**
 * Update a Learning and an User dictionaries
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp  WLF_CLASS instance
 * @param[in]     type  Type of dictionaries
 *
 * @retval  1  success
 * @retval -1  error
 */
jint iwnn_write_out_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type)
{
    WLF_CLASS *wlf;
    WLF_LANG_INFO *langinfo;
    NJ_DIC_INFO *dicinfo;
    WLF_DIC_PROP *dicprop;
    NJ_INT16 dictype;
    int result = -1;
    int dicno;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;


    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    langinfo = iwnn_wlfi_find_langinfo(wlf, wlf->lang);
    if (langinfo == NULL) {
        return -1;
    }

    if (type == WLF_DIC_TYPE_LEARN) {
        for (dicno = 0; dicno < count_standard_dictionaries(); dicno++) {
            dicinfo = &(langinfo->dicset.dic[dicno]);
            dicprop = &(langinfo->dicprops[dicno]);
            if ((dicprop->type == WLF_DIC_TYPE_LEARN) || (dicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL)) {
                result = iwnn_wlfi_write_out_dictionary(wlf, dicinfo, dicprop);
                if (result < 0) {
                    break;
                }
            }
        }
    }
    else if (type == WLF_DIC_TYPE_USER) {
        for (dicno = 0; dicno < count_standard_dictionaries(); dicno++) {
            dicprop = &(langinfo->dicprops[dicno]);
            if (dicprop->type == WLF_DIC_TYPE_USER) {
                dicinfo = &(langinfo->dicset.dic[dicno]);
                result = iwnn_wlfi_write_out_dictionary(wlf, dicinfo, dicprop);
                break;
            }
        }
    }

ExitDone:
    if (result < 0) {
        result = -1;
    }

    return (int)result;
}

/**
 * Sync a Learning and an User dictionaries
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp  WLF_CLASS instance
 * @param[in]     type  Type of dictionaries
 *
 * @retval  1  success
 * @retval -1  error
 */
jint iwnn_sync_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type)
{
    WLF_CLASS *wlf;
    WLF_LANG_INFO *langinfo;
    NJ_DIC_INFO *dicinfo;
    WLF_DIC_PROP *dicprop;
    NJ_INT16 dictype;
    int result = -1;
    int dicno;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;


    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    langinfo = iwnn_wlfi_find_langinfo(wlf, wlf->lang);
    if (langinfo == NULL) {
        return -1;
    }

    if (type == WLF_DIC_TYPE_LEARN) {
        for (dicno = 0; dicno < count_standard_dictionaries(); dicno++) {
            dicinfo = &(langinfo->dicset.dic[dicno]);
            dicprop = &(langinfo->dicprops[dicno]);
            if ((dicprop->type == WLF_DIC_TYPE_LEARN) || (dicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL)) {
                result = fmap_sync(&(dicprop->fmap_handle), MS_SYNC);
                if (result < 0) {
                    break;
                }
            }
        }
    }
    else if (type == WLF_DIC_TYPE_USER) {
        for (dicno = 0; dicno < count_standard_dictionaries(); dicno++) {
            dicprop = &(langinfo->dicprops[dicno]);
            if (dicprop->type == WLF_DIC_TYPE_USER) {
                result = fmap_sync(&(dicprop->fmap_handle), MS_SYNC);
                break;
            }
        }
    }

ExitDone:
    if (result < 0) {
        result = -1;
    }

    return (int)result;
}

/**
 * Sync filesystem. 
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp  WLF_CLASS instance
 */
void iwnn_sync(JNIEnv *env, jclass clazz, jint iwnnp)
{
    sync();
}

/**
 * @brief Learn or User dictionary initialization
 *
 * <b>Content of processing</b>
 *  - Initialization for LearnDictinary or UserDictinary
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp  iWnnINFO Instance
 * @param[in]     type  LearnDictionary or UserDictionary
 * @param[in]     language Kind of Language
 * @param[in]     dictionary Type of Dictionary
 * @retval  0  Success
 * @retval -1  Failed
 */
jint iwnn_delete_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type, jint language, jint dictionary)
{
    WLF_CLASS     *wlf;
    int            dictype;
    int            result;
    iWnn_INFO     *iwnn     = (iWnn_INFO*)iwnnp;

    LOGI_IF(log_trace, "Hi! I'm iWnn Header!\n");

    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    if (type == IWNN_DELETE_LRN) {
        //Destroyed's LearnDictinary
        LOGI_IF(log_trace, "Destroyed's learning!\n");
        dictype = WLF_DIC_TYPE_LEARN;
    }
    else if (type == IWNN_DELETE_USR) {
        //Destroyed's dic to UserDictinary
        LOGI_IF(log_trace, "Destroyed's dic to user!\n");
        dictype = WLF_DIC_TYPE_USER;
    }
    else {
        return -1;
    }

    result = iwnn_wlf_reset_dictionary(wlf, dictype, language, dictionary);
    if (result < 0) {
        return -1;
    }

    return 0;
}
/**
 * @brief Learning Extended Info reset
 *
 * <b>Content of processing</b>
 *  - Reset for LearningDictinary of ExtendedInfo
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp  iWnnINFO Instance
 * @param[in]     language Kind of Language
 * @retval  0  Success
 * @retval -1  Failed
 */
jint iwnn_reset_extended_info(JNIEnv *env, jclass clazz, jstring fileName)
{
    int            result;
    char          *file_name;

    file_name = (char *)(*env)->GetStringUTFChars(env, fileName, 0);

    LOGI_IF(log_trace, "iwnn_reset_extended_info\n");

    if (file_name == NULL) {
        return -1; 
    }

    result = iwnn_wlf_reset_extended_info(file_name);

    (*env)->ReleaseStringUTFChars(env, fileName, (const char *)file_name);
    if (result < 0) {
        return -1;
    }

    return 0;
}
/**
 * @brief Flexible charset and Giji pack setting
 *
 * <b>Content Processing</b>
 *  -  Flexible charset and Giji pack setting
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp  iWnn_INFO Instance
 * @param[in]     flexible_charset  Flexible search
 * @param[in]     keytype  Keypad Type
 *
 * @retval  1  success
 * @retval  0  error
 */
jint iwnn_set_flexible_charset(JNIEnv *env, jclass clazz, jint iwnnp, jint flexible_charset, jint keytype)
{
    WLF_CLASS *wlf;
    WLF_PREDICT *predict;
    WLF_LANG_INFO *langinfo;
    int cnt;
    int offset;
    int max_cnt;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if (iwnn == NULL || iwnn->body == NULL) {
        return 0;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    predict = &(wlf->predict_info);

    langinfo = iwnn_wlfi_find_langinfo(wlf, wlf->lang);
    if (langinfo == NULL) {
        return 0;
    }

    iwnn_wlfi_set_flexible_charset(wlf, flexible_charset, keytype);
    iwnn_wlfi_set_giji_table(wlf);

    if (predict->flexible_search == WLF_FLEXIBLE_SEARCH_OFF) {
        offset = 0;
        max_cnt = langinfo->charset_off_count;
    } else {
        offset = langinfo->charset_off_count;
        max_cnt = langinfo->charset.charset_count - offset;
    }
    for (cnt = 0; cnt < max_cnt; cnt++) {
        wlf->charset.from[cnt] = langinfo->charset.from[cnt + offset];
        wlf->charset.to[cnt] = langinfo->charset.to[cnt + offset];
        //LOGI_IF(log_trace,"iwnn_set_flexible_charset from[%d][%04x][%04x]",cnt,wlf->charset.from[cnt][0],wlf->charset.from[cnt][1]);
        //LOGI_IF(log_trace,"iwnn_set_flexible_charset to[%d][%04x][%04x][%04x][%04x]",cnt,wlf->charset.to[cnt][0],wlf->charset.to[cnt][1],wlf->charset.to[cnt][2],wlf->charset.to[cnt][3]);
    }
    wlf->charset.charset_count = max_cnt;
    wlf->charset.from[max_cnt] = NULL;
    wlf->charset.to[max_cnt] = NULL;
    return 1;
}

/**
 * Flexible search and Keypad setting
 *
 * @param  wlf  WLF_CLASS Instance
 * @param  flexible_charset  Flexible search
 * @param  keytype  Keypad Type
 */
static void iwnn_wlfi_set_flexible_charset(WLF_CLASS *wlf, int flexible_charset, int keytype)
{
    WLF_PREDICT *predict;

    predict = &(wlf->predict_info);

    LOGI_IF(log_trace, "flexible_search (%d)=>(%d)\n", predict->flexible_search, flexible_charset);
    LOGI_IF(log_trace, "keytype (%d)=>(%d)\n", wlf->keytype, keytype);

    predict->flexible_search = flexible_charset;
    wlf->keytype = keytype;

}

/**
 * Judge the candidate if it's on a learning dictionary
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp  WLF_CLASS instance
 * @param[in]     index  Index of a candidate
 *
 * @retval  1  If it's on a learning dictionary
 * @retval  0  Unless
 */
jint iwnn_is_learn_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint index)
{
    WLF_CLASS *wlf;
    WLF_LANG_INFO *langinfo;
    WLF_CAND *pcand;
    int result = 0;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;


    if (iwnn == NULL || iwnn->body == NULL) {
        return 0;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    langinfo = iwnn_wlfi_find_langinfo(wlf, wlf->lang);
    if (langinfo == NULL) {
        return 0;
    }

    pcand = &(wlf->cand_info);
    if (pcand->candidate_cnt <= index) {
        return 0;
    }

    if (NJ_DIC_LEARN == NJ_GET_RESULT_DIC(pcand->candidate[index].operation_id)) {
        result = 1;
    }

    return result;
}

/**
 * Judge the candidate if it's a pseudo candidate
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     index Index of a candidate
 *
 * @retval  1  If it a pseudo candidate
 * @retval  0  Unless
 * @retval -1  Failure
 */
jint iwnn_is_giji_result(JNIEnv *env, jclass clazz, jint iwnnp, jint index)
{
    WLF_CLASS *wlf;
    WLF_CAND *cand_info;
    WLF_GIJI_INFO *giji_info;
    NJ_RESULT *result;
    NJ_UINT16 candidate_cnt;
    int giji_index;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;


    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    if (wlf->giji_mode != WLF_MODE_GIJI_ON) {
        return 0;
    }

    cand_info = &(wlf->cand_info);
    giji_info = &(wlf->giji_info);

    if (index < cand_info->candidate_cnt) {
        return 0;
    }

    candidate_cnt = cand_info->candidate_cnt + giji_info->giji_cnt;
    if (candidate_cnt <= index) {
        return 0;
    }

    giji_index = index - cand_info->candidate_cnt;

    result = &(giji_info->giji_candidate[giji_index]);

    if ((result->operation_id & NJ_TYPE_GIJI_BIT) != NJ_TYPE_GIJI_BIT) {
        return 0;
    }

    return 1;
}

/**
 * Undo learning
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     count  Count of Undo
 *
 * @retval  0  success
 * @retval -1  error
 */
jint iwnn_undo(JNIEnv *env, jclass clazz, jint iwnnp, jint count)
{
    WLF_CLASS *wlf;
    NJ_INT16 result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;


    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    result = njx_undo(&(wlf->iwnn), count);
    if (result < 0) {
        char *err_msg;
        err_msg = iwnn_err_message(result);
        LOGE_IF(log_trace, "iwnn_undo count=%d -- %s\n", count, err_msg);
        free(err_msg);
        return -1;
    }

    return 0;
}

#define WLF_EMOJI_FILTER_DISABLE    0   /**< Disable EMOJI filter */
#define WLF_EMOJI_FILTER_ENABLE     1   /**< Enable EMOJI filter  */
/**
 * Set an Emoji filter
 * (for dictionary searching)
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     enabled  Flag for filtering
 */
void iwnn_emoji_filter(JNIEnv *env, jclass clazz, jint iwnnp, jint enabled)
{
    WLF_CLASS *wlf;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if ((iwnn == NULL) || (iwnn->body == NULL)) {
        return;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    if (enabled == WLF_EMOJI_FILTER_ENABLE) {
        wlf->filter |= WLF_EMOJI_FILTER;
    }
    else {
        wlf->filter &= ~WLF_EMOJI_FILTER;
    }
}

#define WLF_EMAIL_ADDRESS_FILTER_DISABLE    0   /**< Disable Email address filter */
#define WLF_EMAIL_ADDRESS_FILTER_ENABLE     1   /**< Enable Email address filter  */
/**
 * Set an Email address filter
 * (for dictionary searching)
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     enabled  Flag for filtering
 */
void iwnn_email_address_filter(JNIEnv *env, jclass clazz, jint iwnnp, jint enabled)
{
    WLF_CLASS *wlf;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if ((iwnn == NULL) || (iwnn->body == NULL)) {
        return;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    if (enabled == WLF_EMAIL_ADDRESS_FILTER_ENABLE) {
        wlf->filter |= WLF_EMAIL_ADDRESS_FILTER;
    }
    else {
        wlf->filter &= ~WLF_EMAIL_ADDRESS_FILTER;
    }
}

#define WLF_DECOEMOJI_FILTER_DISABLE    0   /**< Disable DECOEMOJI filter */
#define WLF_DECOEMOJI_FILTER_ENABLE     1   /**< Enable DECOEMOJI filter  */
/**
 * Set an DecoEmoji filter
 * (for dictionary searching)
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     enabled  Flag for filtering
 */
void iwnn_decoemoji_filter(JNIEnv *env, jclass clazz, jint iwnnp, jint enabled)
{
    WLF_CLASS *wlf;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if ((iwnn == NULL) || (iwnn->body == NULL)) {
        return;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    if (enabled == WLF_DECOEMOJI_FILTER_ENABLE) {
        wlf->filter |= WLF_DECOEMOJI_FILTER;
    }
    else {
        wlf->filter &= ~WLF_DECOEMOJI_FILTER;
    }
}

/**
 * Get an available dictionary management information
 *
 * @param  langinfo  Language information
 *
 * @retval !NULL  Dictionary management information
 * @retval  NULL  None
 */
static fmap_t * iwnn_dic_new_reference(WLF_LANG_INFO *langinfo)
{
    fmap_t *reference;
    fmap_t *result = NULL;
    int cnt;

    for (cnt = 0; cnt < count_standard_dictionaries(); cnt++) {
        reference = &(langinfo->handle_reference[cnt]);
        if (reference->reference_count == 0) {
            result = reference;
            break;
        }
    }
    return result;
}

/**
 * Decreace a reference of the dictionary management information structure
 *
 * @param  langinfo  Language information
 * @param  path  Path to the dictionary
 *
 * @retval  1  No reference
 * @retval  0  There's some references
 */
static int iwnn_dic_decrement_reference(WLF_LANG_INFO *langinfo, const char *path)
{
    fmap_t *reference;
    int result = 0;
    int cnt;

    for (cnt = 0; cnt < count_standard_dictionaries(); cnt++) {
        reference = &(langinfo->handle_reference[cnt]);
        if (reference->reference_count == 0) {
            continue;
        }

        if (strcmp(path, reference->path) == 0) {
            reference->reference_count--;
            if (reference->reference_count == 0) {
                result = 1;
            }
            break;
        }
    }
    return result;
}

/**
 * Get a dictionary management information structure
 *
 * @param  langinfo  Language information
 * @param  path  Path to the dictionary
 *
 * @retval !NULL  Dictionary management information
 * @retval  NULL  No structure
 */
static fmap_t * iwnn_dic_search_reference(WLF_LANG_INFO *langinfo, const char *path)
{
    fmap_t *reference;
    fmap_t *result = NULL;
    int cnt;

    for (cnt = 0; cnt < count_standard_dictionaries(); cnt++) {
        reference = &(langinfo->handle_reference[cnt]);
        if (reference->reference_count == 0) {
            continue;
        }

        if (strcmp(path, reference->path) == 0) {
            result = reference;
            break;
        }
    }
    return result;
}

/**
 * Mount a dictionary
 *
 * @param  wlf  WLF_CLASS instance
 * @param  langinfo  Language information
 * @param  dicinfo  Dictionary set info
 * @param  dicprop  Dictionary management information
 * @param  cache  Management cache
 *
 * @retval  1  success
 * @retval  0  No mount
 * @retval -1  error
 */
static int iwnn_dic_mount(WLF_CLASS *wlf, WLF_LANG_INFO *langinfo, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop, int cache)
{
    int dictype = dicprop->type;


    LOGI_IF(log_trace, "iwnn_dic_mount -- path=\"%s\",type=%d,cache=%d\n", dicprop->name, dictype, cache);

    /* Do nothing and return if it's a rule dictionary */
    if (dictype == 1) {
        LOGI_IF(log_trace, "iwnn_dic_mount -- success\n");
        return 0;
    }

    if (cache) {
        if (dicinfo->srhCache) {
            free(dicinfo->srhCache);
            dicinfo->srhCache = NULL;
        }
        dicinfo->srhCache = (NJ_SEARCH_CACHE *)malloc(sizeof(NJ_SEARCH_CACHE));
        if (dicinfo->srhCache == NULL) {
            LOGE_IF(log_trace, "iwnn_dic_mount -- no memory\n");
            return -1;
        }
    }

    /* Return if it's a pseudo dictionary */
    if (dictype == 5 || dictype == 6 || dictype == 7 || dictype == 9 || dictype == 10 || dictype == 11 || dictype == 13) {
        LOGI_IF(log_trace, "iwnn_dic_mount -- success\n");
        return 1;
    }

    if (dictype == 12) {
        if (dicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT]) {
            free(dicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
            dicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT] = NULL;
        }
        dicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT] = malloc(sizeof(NJG_NMF_DIVINF));
        LOGI_IF(log_trace, "iwnn_dic_mount -- success\n");
        return 1;
    }

    if ((dictype != WLF_DIC_TYPE_USER) && (dictype != WLF_DIC_TYPE_LEARN) &&
        (dictype != WLF_DIC_TYPE_DECOEMOJI) && (dictype != WLF_DIC_TYPE_HRL_GIJI)) {
        if (iwnn_dic_mount_easy(&(wlf->iwnn), langinfo, dicinfo, dicprop) < 0) {
            free(dicinfo->srhCache);
            dicinfo->srhCache = NULL;
            LOGE_IF(log_trace, "iwnn_dic_mount -- failed to mount easy\n");
            return -1;
        }
    }

    if (iwnn_dic_mount_ext(wlf, langinfo, dicinfo, dicprop, wlf->package, wlf->password) < 0) {
        if ((dictype == WLF_DIC_TYPE_USER) || (dictype == WLF_DIC_TYPE_LEARN) ||
            (dictype == WLF_DIC_TYPE_DECOEMOJI) || (dictype == WLF_DIC_TYPE_HRL_GIJI)) {
            free(dicinfo->srhCache);
            dicinfo->srhCache = NULL;
            LOGE_IF(log_trace, "iwnn_dic_mount -- failed to mount ext\n");
            return -1;
        }
    }

    iwnn_dic_mount_ext_open_input(&(wlf->iwnn), dicinfo, dicprop);
    LOGI_IF(log_trace, "iwnn_dic_mount -- success\n");

    return 1;
}

/**
 * Mount a dictionary
 * (for a ROM dictionary)
 *
 * @param  iwnn  NJ_CLASS instance
 * @param  langinfo  Language information
 * @param  dicinfo  Dictionary setting info
 * @param  dicprop  Dictionary Management info
 *
 * @retval  0  success
 * @retval -1  error
 */
static int iwnn_dic_mount_easy(NJ_CLASS *iwnn, WLF_LANG_INFO *langinfo, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop)
{
    char path[WLF_FILE_PATH_LEN];
    struct stat sb;
    fmap_t *fmap;
    fmap_t *reference = NULL;
    int result;
    int i;


    LOGI_IF(log_trace, "iwnn_dic_mount_easy -- path=\"%s\"\n", dicprop->name);

    fmap = &(dicprop->fmap_handle);
    memcpy(path, dicprop->name, sizeof(path));

    SYSCALL_RETRY(result, stat(path, &sb));
    if (result < 0) {
        LOGE_IF(log_trace, "iwnn_dic_mount_easy -- failed to stat errno=%d\n", errno);
        return -1;
    }
    if ((reference = iwnn_dic_search_reference(langinfo, path)) != NULL) {
        reference->reference_count++;
        memcpy(fmap, reference, sizeof(fmap_t));
    }
    else {
        if (fmap_open(fmap, path, O_RDONLY, 0, (size_t)sb.st_size) < 0) {
            LOGE_IF(log_trace, "iwnn_dic_mount_easy -- failed to open errno=%d\n", errno);
            return -1;
        }

        if (iwnn_dic_init(iwnn, (NJ_DIC_HANDLE)fmap->addr, NULL, (NJ_UINT32)sb.st_size, dicprop->type, WLF_DIC_OPEN_HARD, -1) < 0) {
            (void)fmap_close(fmap);
            LOGE_IF(log_trace, "iwnn_dic_mount_easy -- failed to init errno=%d\n", errno);
            return -1;
        }

        if ((reference = iwnn_dic_new_reference(langinfo)) != NULL) {
            fmap->reference_count = 1;
            memcpy(reference, fmap, sizeof(fmap_t));
        }

    }

    dicinfo->handle = (NJ_DIC_HANDLE)fmap->addr;
    for (i = 0; i < NJ_MAX_EXT_AREA; i++) {
        dicinfo->ext_area[i] = NULL;
    }
    dicprop->size = (NJ_UINT32)sb.st_size;

    LOGI_IF(log_trace, "iwnn_dic_mount_easy -- success  handle=%p,ext_area=%p,size=%ld\n",
            dicinfo->handle, dicinfo->ext_area, dicprop->size);

    return 0;
}

#define IWNN_DICTIONARY_OPEN_RETRY_MAX 2    /**< Maximum number of recovering */
/**
 * Mount a dictionary
 * (for a rewritable dictionary)
 *
 * @param  wlf      WLF_CLASS Instance
 * @param  langinfo Language Information
 * @param  dicinfo  Dictionary set info
 * @param  dicprop  Dictionary Management
 * @param  package  Package name of client
 * @param  password Password of client
 *
 * @retval  0  success
 * @retval -1  error
 */
static int iwnn_dic_mount_ext(WLF_CLASS *wlf, WLF_LANG_INFO *langinfo, NJ_DIC_INFO *dicinfo,
                              WLF_DIC_PROP *dicprop, char* package, char* password)
{
    NJ_CLASS *iwnn = &(wlf->iwnn);
    char master_path[WLF_FILE_PATH_LEN];
    char *file_name;
    char tmp_path[WLF_FILE_PATH_LEN];
    NJ_DIC_HANDLE handle;
    NJ_VOID *ext_area;
    NJ_UINT32 size;
    NJ_INT16 result;
    fmap_t *fmap;
    int dictype, retval, retry;

    LOGI_IF(log_trace, "iwnn_dic_mount_ext -- path=\"%s\",type=%d\n", dicprop->name, dicprop->type);

    dictype = (int)dicprop->type;
    file_name = basename(dicprop->name);

    if (dictype == WLF_DIC_TYPE_USER) {
        size = NJ_USER_DIC_SIZE;
        if (package[0] == '\0') {
            snprintf(master_path, sizeof(master_path), IWNN_DICSET_BACKUP_MASTER_PATH, wlf->data_area_path, file_name);
            snprintf(tmp_path, sizeof(tmp_path), IWNN_DICSET_BACKUP_TMP_PATH, wlf->data_area_path, file_name);
        }
        else {
            snprintf(master_path, sizeof(master_path), IWNN_DICSET_BACKUP_SERVICE_MASTER_PATH, wlf->data_area_path, package, password, file_name);
            snprintf(tmp_path, sizeof(tmp_path), IWNN_DICSET_BACKUP_SERVICE_TMP_PATH, wlf->data_area_path, package, password, file_name);
        }
    }
    else if (dictype == WLF_DIC_TYPE_LEARN) {
        size = WLF_LEARN_DIC_SIZE;
        if (package[0] == '\0') {
            snprintf(master_path, sizeof(master_path), IWNN_DICSET_BACKUP_MASTER_PATH, wlf->data_area_path, file_name);
            snprintf(tmp_path, sizeof(tmp_path), IWNN_DICSET_BACKUP_TMP_PATH, wlf->data_area_path, file_name);
        }
        else {
            snprintf(master_path, sizeof(master_path), IWNN_DICSET_BACKUP_SERVICE_MASTER_PATH, wlf->data_area_path, package, password, file_name);
            snprintf(tmp_path, sizeof(tmp_path), IWNN_DICSET_BACKUP_SERVICE_TMP_PATH, wlf->data_area_path, package, password, file_name);
        }
    }
    else if (dictype == WLF_DIC_TYPE_DECOEMOJI) {
        size = DL_DEMOJIDIC_SIZE;
        snprintf(master_path, sizeof(master_path), IWNN_DICSET_BACKUP_MASTER_PATH, wlf->data_area_path, basename(dicprop->name));
        snprintf(tmp_path, sizeof(tmp_path), IWNN_DICSET_BACKUP_TMP_PATH, wlf->data_area_path, basename(dicprop->name));
    }
    else if (dictype == WLF_DIC_TYPE_HRL_GIJI) {
        iwnn->dic_set.rHandle[0] = langinfo->dicset.rHandle[0];
        result = njex_hrl_get_ext_area_size(iwnn, iwnn->dic_set.rHandle[0], &size);
        if (result < 0) {
            char *err_msg;
            err_msg = iwnn_err_message(result);
            LOGE_IF(log_trace, "iwnn_dic_mount_ext -- njex_hrl_get_ext_area_size =%s\n", err_msg);
            free(err_msg);
        } else {
            if (package[0] == '\0') {
                snprintf(master_path, sizeof(master_path), IWNN_DICSET_BACKUP_MASTER_PATH, wlf->data_area_path, file_name);
                snprintf(tmp_path, sizeof(tmp_path), IWNN_DICSET_BACKUP_TMP_PATH, wlf->data_area_path, file_name);
            } else {
                snprintf(master_path, sizeof(master_path), IWNN_DICSET_BACKUP_SERVICE_MASTER_PATH, wlf->data_area_path, package, password, file_name);
                snprintf(tmp_path, sizeof(tmp_path), IWNN_DICSET_BACKUP_SERVICE_TMP_PATH, wlf->data_area_path, package, password, file_name);
            }
        }
    }
    else {
        handle = dicinfo->handle;
        result = njx_get_ext_area_size(iwnn, NJ_DIC_H_TYPE_NORMAL, handle, &size);
        if (result < 0) {
            char *err_msg;
            err_msg = iwnn_err_message(result);
            LOGE_IF(log_trace, "iwnn_dic_mount_ext -- njx_get_ext_area_size(handle=%p)=%s\n", handle, err_msg);
            free(err_msg);
        }
        if (result <= 0) {
            LOGI_IF(log_trace, "iwnn_dic_mount_ext -- success to not mount\n");
            return 0;
        }
        fmap = &(dicprop->fmap_handle);
        if (package[0] == '\0') {
            snprintf(master_path, sizeof(master_path), IWNN_DICSET_BACKUP_REFERENCE_MASTER_PATH, wlf->data_area_path, langinfo->lang, fmap->reference_count, file_name);
            snprintf(tmp_path, sizeof(tmp_path), IWNN_DICSET_BACKUP_REFERENCE_TMP_PATH, wlf->data_area_path, langinfo->lang, fmap->reference_count, file_name);
        }
        else {
            snprintf(master_path, sizeof(master_path), IWNN_DICSET_BACKUP_REFERENCE_SERVICE_MASTER_PATH, wlf->data_area_path, package, password, langinfo->lang, fmap->reference_count, file_name);
            snprintf(tmp_path, sizeof(tmp_path), IWNN_DICSET_BACKUP_REFERENCE_SERVICE_TMP_PATH, wlf->data_area_path, package, password, langinfo->lang, fmap->reference_count, file_name);
        }
        LOGI_IF(log_trace, "iwnn_dic_mount_ext -- ext area size=%ld\n", size);
    }

    retry = 0;
    do {
        if (iwnn_dic_mount_ext_open(iwnn, dicinfo, dicprop, size, master_path) == 0) {
            LOGI_IF(log_trace, "iwnn_dic_mount_ext -- success  path=\"%s\",size=%ld\n", master_path, size);
            return 0;
        }

        if (fmap_recovery(master_path, tmp_path, size) < 0) {
            LOGE_IF(log_trace, "iwnn_dic_mount_ext -- failed to recovery  master=\"%s\",tmp=\"%s\",size=%ld\n",
                    master_path, tmp_path, size);
            break;
        }

        retry++;

    } while (retry < IWNN_DICTIONARY_OPEN_RETRY_MAX);

    SYSCALL_RETRY(retval, unlink(tmp_path));

    if (mkdirs(master_path, S_IRWXU) < 0) {
        LOGE_IF(log_trace, "iwnn_dic_mount_ext -- failed to mkdirs path=\"%s\",errno=%d\n", master_path, errno);
        return -1;
    }

    if ((dictype == WLF_DIC_TYPE_USER) || (dictype == WLF_DIC_TYPE_LEARN)) {
        fmap = &(dicprop->fmap_handle);
    }
    else {
        fmap = &(dicprop->fmap_ext_area);
    }

    if (fmap_open(fmap, master_path, O_CREAT|O_RDWR, S_IRUSR|S_IWUSR, size) < 0) {
        LOGE_IF(log_trace, "iwnn_dic_mount_ext -- failed to open\n");
        return -1;
    }

    if ((dictype == WLF_DIC_TYPE_USER) || (dictype == WLF_DIC_TYPE_LEARN)) {
        handle = (NJ_DIC_HANDLE)fmap->addr;
        ext_area = NULL;
    }
    else if (dictype == WLF_DIC_TYPE_DECOEMOJI) {
        handle = (NJ_DIC_HANDLE)fmap->addr;
        ext_area = (NJ_VOID *)fmap->addr;
    }
    else {
        handle = dicinfo->handle;
        ext_area = (NJ_VOID *)fmap->addr;
    }

    if (iwnn_dic_init(iwnn, handle, ext_area, size, dicprop->type, WLF_DIC_OPEN_CREAT, NJ_TYPE_EXT_AREA_DEFAULT) < 0) {
        (void)fmap_close(fmap);
        SYSCALL_RETRY(retval, unlink(master_path));
        LOGE_IF(log_trace, "iwnn_dic_mount_ext -- failed to init\n");
        return -1;
    }

    if (fmap_sync(fmap, MS_SYNC) < 0) {
        (void)fmap_close(fmap);
        SYSCALL_RETRY(retval, unlink(master_path));
        LOGE_IF(log_trace, "iwnn_dic_mount_ext -- failed to sync\n");
        return -1;
    }

    if ((dictype == WLF_DIC_TYPE_USER) || (dictype == WLF_DIC_TYPE_LEARN)) {
        dicinfo->handle = (NJ_DIC_HANDLE)fmap->addr;
    }
    else {
        dicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT] = (NJ_VOID *)fmap->addr;
    }

    LOGI_IF(log_trace, "iwnn_dic_mount_ext -- success\n");

    return 0;
}

/**
 * Mount a dictionary
 * (for a input dictionary)
 *
 * @param  iwnn  NJ_CLASS instance
 * @param  dicinfo  Dictionary set info
 * @param  dicprop  Dictionary management
 *
 * @retval  0  success
 * @retval -1  error
 */
static int iwnn_dic_mount_ext_open_input(NJ_CLASS *iwnn, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop)
{
    fmap_t *fmap;
    struct stat sb;
    int result;

    LOGI_IF(log_trace, "iwnn_dic_mount_ext_open_input\n");

    dicinfo->ext_area[NJ_TYPE_EXT_AREA_INPUT] = NULL;
    if (dicprop->ext_inputname[0] != '\0') {
        LOGI_IF(log_trace, "iwnn_dic_mount_ext_open_input -- path=\"%s\"\n", dicprop->ext_inputname);
        fmap = &(dicprop->fmap_ext_area_input);

        SYSCALL_RETRY(result, stat(dicprop->ext_inputname, &sb));
        if (result < 0) {
            LOGE_IF(log_trace, "iwnn_dic_mount_ext_open_input -- failed to stat errno=%d\n", errno);
            return -1;
        }

        if (fmap_open(fmap, dicprop->ext_inputname, O_RDONLY, 0, (size_t)sb.st_size) < 0) {
            LOGE_IF(log_trace, "iwnn_dic_mount_ext_open_input -- failed to open errno=%d\n", errno);
            return -1;
        }
        if (iwnn_dic_init(iwnn, dicinfo->handle, (NJ_VOID *)fmap->addr, (NJ_UINT32)sb.st_size, dicprop->type, WLF_DIC_OPEN_EXCL, NJ_TYPE_EXT_AREA_INPUT) < 0) {
            (void)fmap_close(fmap);
            LOGE_IF(log_trace, "iwnn_dic_mount_ext_open_input -- failed to init errno=%d\n", errno);
            return -1;
        }

        dicinfo->ext_area[NJ_TYPE_EXT_AREA_INPUT] = (NJ_VOID *)fmap->addr;
    }

    return 0;
}

/**
 * Mount a dictionary
 * (for a rewritable dictionary)
 *
 * @param  iwnn  NJ_CLASS instance
 * @param  dicinfo  Dictionary set info
 * @param  dicprop  Dictionary management
 * @param  size  Size of dictionary
 * @param  path  Path to a dictionary
 *
 * @retval  0  success
 * @retval -1  error
 */
static int iwnn_dic_mount_ext_open(NJ_CLASS *iwnn, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop, NJ_UINT32 size, const char *path)
{
    NJ_DIC_HANDLE handle;
    NJ_VOID *ext_area;
    fmap_t *fmap;
    int dictype;


    LOGI_IF(log_trace, "iwnn_dic_mount_ext_open -- path=\"%s\",size=%ld\n", path, size);

    if (fmap_check(path, size) < 0) {
        LOGE_IF(log_trace, "iwnn_dic_mount_ext_open -- failed to check\n");
        return -1;
    }

    dictype = (int)dicprop->type;

    if ((dictype == WLF_DIC_TYPE_USER) || (dictype == WLF_DIC_TYPE_LEARN)) {
        fmap = &(dicprop->fmap_handle);
    }
    else {
        fmap = &(dicprop->fmap_ext_area);
    }

    if (fmap_open(fmap, path, O_RDWR, S_IRUSR|S_IWUSR, size) < 0) {
        LOGE_IF(log_trace, "iwnn_dic_mount_ext_open -- failed to open\n");
        return -1;
    }

    if ((dictype == WLF_DIC_TYPE_USER) || (dictype == WLF_DIC_TYPE_LEARN)) {
        handle = (NJ_DIC_HANDLE)fmap->addr;
        ext_area = NULL;
    }
    else if (dictype == WLF_DIC_TYPE_DECOEMOJI) {
        handle = (NJ_DIC_HANDLE)fmap->addr;
        ext_area = (NJ_VOID *)fmap->addr;
    }
    else {
        handle = dicinfo->handle;
        ext_area = (NJ_VOID *)fmap->addr;
    }

    if (iwnn_dic_init(iwnn, handle, ext_area, size, dictype, WLF_DIC_OPEN_EXCL, NJ_TYPE_EXT_AREA_DEFAULT) < 0) {
        (void)fmap_close(fmap);
        LOGE_IF(log_trace, "iwnn_dic_mount_ext_open -- failed to init\n");
        return -1;
    }

    if ((dictype == WLF_DIC_TYPE_USER) || (dictype == WLF_DIC_TYPE_LEARN)) {
        dicinfo->handle = (NJ_DIC_HANDLE)fmap->addr;
    }
    else {
        dicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT] = (NJ_VOID *)fmap->addr;
    }

    LOGI_IF(log_trace, "iwnn_dic_mount_ext_open -- success\n");

    return 0;
}

/**
 * Unmount a dictionary
 *
 * @param  wlf  WLF_CLASS instance
 * @param  dicinfo  Dictionary set info
 * @param  dicprop  Dictionary management
 *
 * @retval  0  success
 */
static int iwnn_dic_unmount(WLF_CLASS *wlf, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop)
{
    LOGI_IF(log_trace, "iwnn_dic_unmount\n");
    fmap_close(&(dicprop->fmap_handle));
    fmap_close(&(dicprop->fmap_ext_area));
    fmap_close(&(dicprop->fmap_ext_area_input));
    LOGI_IF(log_trace, "iwnn_dic_unmount -- success\n");
    return 0;
}

/**
 * Initialize a dictionary
 *
 * @param  iwnn  NJ_CLASS instance
 * @param  handle  Dictionary handle
 * @param  ext_area  Extra dictionary handle
 * @param  size  Size of a dictionary
 * @param  dictype  Type of a dictionary
 * @param  mode  Initializing mode
 * @param  exttype  ext type
 *
 * @retval  0  success
 * @retval -1  error
 */
static int iwnn_dic_init(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_area, NJ_UINT32 size, int dictype, int mode, int exttype)
{
    NJ_INT16 result = 0;


    LOGI_IF(log_trace, "iwnn_dic_init -- handle=%p,ext_area=%p,size=%ld,type=%d,mode=%d\n",
            handle, ext_area, size, dictype, mode);

    if (mode == WLF_DIC_OPEN_CREAT) {
        if (is_optional_dictionary(dictype) || (dictype == WLF_DIC_TYPE_LEARN)) {
            NJ_INT8 type;

            if ((dictype != WLF_DIC_TYPE_LEARN) && (dictype != WLF_DIC_TYPE_AUTOLEARN)) {
                type = 0;
            }
            else {
                type = 2;
            }

            result = njx_create_dic(iwnn, handle, type, size);
            if (result < 0) {
                char *err_msg;
                err_msg = iwnn_err_message(result);
                LOGE_IF(log_trace, "iwnn_dic_init -- failed to create %s\n", err_msg);
                free(err_msg);
            }
        }
        else if(dictype == WLF_DIC_TYPE_DECOEMOJI) {
            result = demoji_create_dic((DL_UINT8 *)iwnn, (DL_UINT8 *)handle);
            if (result < 0) {
                LOGE_IF(log_trace, "iwnn_dic_init -- failed to demoji_create_dic() : iwnn=%p, handle=%p, result=%d\n", iwnn, handle, (int)result);
            }
        }
        else if (dictype == WLF_DIC_TYPE_HRL_GIJI) {
            result = njex_hrl_init_ext_area(iwnn, iwnn->dic_set.rHandle[0], ext_area, size);
            if (result < 0) {
                char *err_msg;
                err_msg = iwnn_err_message(result);
                LOGE_IF(log_trace, "iwnn_dic_init -- failed to njex_hrl_ext_create %s", err_msg);
                free(err_msg);
            }
        }
        else {
            result = njx_init_ext_area(iwnn, NJ_DIC_H_TYPE_NORMAL, handle, ext_area, size);
            if (result < 0) {
                char *err_msg;
                err_msg = iwnn_err_message(result);
                LOGE_IF(log_trace, "iwnn_dic_init -- failed to ext_create %s", err_msg);
                free(err_msg);
            }
            if (result == 0) {
                result = -1;
            }
        }

        if (result < 0) {
            LOGE_IF(log_trace, "iwnn_dic_init -- failed to init\n");
            return -1;
        }
    }

    if (is_optional_dictionary(dictype) || (dictype == WLF_DIC_TYPE_LEARN) || (mode == WLF_DIC_OPEN_HARD)) {
        NJ_UINT8 restore = 0;

        if (mode == WLF_DIC_OPEN_EXCL) {
            restore = 1;
        }

        result = njx_check_dic(iwnn, NJ_DIC_H_TYPE_NORMAL, handle, restore, size);
        if (result < 0) {
            char *err_msg;
            err_msg = iwnn_err_message(result);
            LOGE_IF(log_trace, "iwnn_dic_init -- failed to check handle=%p,restore=%d,size=%ld,%s\n", handle, restore, size, err_msg);
            free(err_msg);
        }
    }
    else if(dictype == WLF_DIC_TYPE_DECOEMOJI) {
        result = demoji_check_dic((DL_UINT8 *)iwnn, (DL_UINT8 *)handle);
        if (result < 0) {
            LOGE_IF(log_trace, "iwnn_dic_init -- failed to demoji_check_dic() : iwnn=%p, handle=%p, result=%d\n", iwnn, handle, (int)result);
        }
    }
    else if(dictype == WLF_DIC_TYPE_HRL_GIJI) {
        result = njex_hrl_check_ext_area(iwnn, iwnn->dic_set.rHandle[0], ext_area, size);
        if (result < 0) {
            char *err_msg;
            err_msg = iwnn_err_message(result);
            LOGE_IF(log_trace, "iwnn_dic_init -- failed to njex_hrl_ext_check handle=%p,ext_area=%p,size=%ld,%s\n", handle, ext_area, size, err_msg);
            free(err_msg);
        }
    }
    else {
        result = njx_check_ext_area(iwnn, NJ_DIC_H_TYPE_NORMAL, handle, ext_area, size, exttype);
        if (result < 0) {
            char *err_msg;
            err_msg = iwnn_err_message(result);
            LOGE_IF(log_trace, "iwnn_dic_init -- failed to ext_check handle=%p,ext_area=%p,size=%ld,%s\n", handle, ext_area, size, err_msg);
            free(err_msg);
        }
        if (result == 0) {
            result = -1;
        }
    }

    if (result < 0) {
        LOGE_IF(log_trace, "iwnn_dic_init -- failed to init\n");
        return -1;
    }

    LOGI_IF(log_trace, "iwnn_dic_init -- success");

    return 0;
}
/**
 * Reset a learning extended Info
 *
 * @param  handle  Dictionary handle
 *
 * @retval  0  success
 * @retval -1  error
 */
static int iwnn_learning_extended_info_reset(NJ_DIC_HANDLE handle)
{
    NJ_INT16 result = 0;
    NJ_UINT8    *p;
    NJ_UINT32   i,cnt,dictype;

    if (handle == NULL) {
        LOGE_IF(log_trace, "iwnn_learning_extended_info_reset -- handle = null");
        return -1;
    }

    LOGI_IF(log_trace, "iwnn_learning_extended_info_reset -- handle=%p\n",handle);

    /* Get DicType */
    dictype = NJ_GET_DIC_TYPE(handle);
    if (NJ_DIC_TYPE_LEARN != dictype) {
        LOGE_IF(log_trace, "iwnn_learning_extended_info_reset -- dictype != NJ_DIC_TYPE_LEARN");
        return -1;
    }

    p = (NJ_UINT8 *)handle;

    /* Get Max Word Count */
    cnt = NJ_INT32_READ(p + POS_MAX_WORD);

    p += GET_EXT_DATA_AREA_OFFSET(cnt);

    /* Reset Extended Word Info */
    for ( i = 0; i < cnt; i++) {
        NJ_INT32_WRITE(p, 0L);
        p += sizeof(NJ_INT32);
        p += sizeof(NJ_INT16);
    }

    LOGI_IF(log_trace, "iwnn_learning_extended_info_reset -- success");

    return 0;
}
/**
 * @brief Get the size of WLF_CLASS
 *
 * Description
 *  - Get the size on Memory of WLF_CLASS structure
 *
 * @return  Size of memory
 */
static WLF_UINT32 iwnn_wlf_get_init_size(void)
{
    return sizeof(WLF_CLASS);
}

/**
 * @brief Initialize
 *
 * Description
 *  - Initialize WLF_CLASS structure.
 *  - Mount a dictionary.
 *  - Set a pseudo searching setting.
 *  - Set a pseudo candidate setting.
 *  - Set iWnn Engine options.
 *  - Initialize predicting conditions.
 *  - Initialize the iWnn Engine.
 *
 * @param[in,out]  *wlf_class Pointer for WLF_CLASS
 * @param[in]      endian     Endian
 *
 * @retval  = 0  Success
 * @retval  < 0  Failure
 *
 * @note
 *  - Get the mount space for a dictionary, when initializing
 *    Must free the space when finalizing
 *
 */
static WLF_INT16 iwnn_wlf_initialize(WLF_UINT8 *wlf_class, WLF_UINT8 endian)
{
    WLF_CLASS   *wlf;
    NJ_UINT8    i, j;
    WLF_LANG_INFO *langinfo;
    NJ_UINT32 aip_worksize;
    int result = -1;

    if (wlf_class == NULL) {
        return WLF_ERR_INVALID_PARAM;
    }

    /* Cast to WLF_CLASS */
    wlf = (WLF_CLASS *)wlf_class;

    /* Initialize WLF_CLASS */
    memset(wlf, 0x00, sizeof(WLF_CLASS));

    /* Hold the endian */
    wlf->endian = endian;

    /* Initialize language settings */
    wlf->lang = WLF_LANG_TYPE_NONE;
    for (i = 0; i < WLF_LANG_TYPE_USE_MAX; i++) {
        langinfo = &(wlf->lang_info[i]);
        langinfo->lang = WLF_LANG_TYPE_NONE;
        memset(&(langinfo->dicset), 0, sizeof(NJ_DIC_SET));
        for (j = 0; j < NJ_MAX_DIC; j++) { // include Additional dictionary
            WLF_DIC_PROP *dicprop = &(langinfo->dicprops[j]);
            dicprop->type = -1;
        }
    }

    wlf->keytype = WLF_KEY_TYPE_KEYPAD12;

    /* Set the iWnn Engine option */
    /* (Number of automatic consecutive clause candidates) */
    wlf->option.autoconv_cnt = WLF_OPTION_RENBUN;

    if (wlf->option.aip_work) {
        free(wlf->option.aip_work);
    }
    wlf->option.aip_work = NULL;
    result = njex_aip_get_ext_area_size(&(wlf->iwnn),&aip_worksize);
    if (result < 0) {
        char *err_msg;
        err_msg = iwnn_err_message(result);
        LOGE_IF(log_trace, "iwnn_wlf_initialize -- njex_aip_get_ext_area_size=%s\n", err_msg);
        free(err_msg);
    }
    else{
        wlf->option.aip_work = malloc(aip_worksize);
        if (wlf->option.aip_work == NULL){
            LOGE_IF(log_trace, "iwnn_wlf_initialize -- no memory\n");
        }
    }
    wlf->option.ext_mode = 0x0000;
    wlf->option.ext_mode |= NJ_OPT_FORECAST_COMPOUND_WORD;
    /* Set learn dictionary setting */
    wlf->option.ext_mode |= NJ_OPT_CLEARED_EXTAREA_WORD;
    
    /* Set a filter for Dictionary searching */
    wlf->filter = 0;
    wlf->option.phase1_filter = iwnn_phase1_filter;
    wlf->option.phase1_option = (NJ_VOID *)wlf;

    /* Initialize prediction searching conditions */
    WLF_SET_INIT_PREDICT(&(wlf->predict_info));

    /* Initialize iWnn Engine */
    njx_init(&(wlf->iwnn), &(wlf->option));

    return 0;
}

/**
 * Load Language Settings
 *
 * Load a dictionary set from language settings
 *
 * @param  wlf_class  WLF_CLASS instance
 * @param  lang       Number of language
 * @param  filename   Configuration file name
 *
 * @retval   0  Success
 * @retval  -1  Failure
 */
static WLF_INT16 iwnn_wlf_load_lang(WLF_UINT8 *wlf_class, WLF_UINT8 lang, char *filename)
{
    WLF_CLASS       *wlf;
    WLF_UINT8       diccnt;
    WLF_LANG_INFO   *langinfo = NULL;
    WLF_INT16       ret;


    if (wlf_class == NULL) {
        return -1;
    }

    /* Cast to WLF_CLASS */
    wlf = (WLF_CLASS *)wlf_class;

    /* Check if a dictionary has been loaded */
    langinfo = iwnn_wlfi_find_langinfo(wlf, lang);
    if (langinfo != NULL) {
        /* Already done */
        return 0;
    }

    langinfo = iwnn_wlfi_find_free_langinfo(wlf);
    if (langinfo == NULL) {
        /* No area */
        return -1;
    }

    langinfo->lang = lang;

    ret = iwnn_wlfi_conf_read(wlf, langinfo, filename);
    if (ret < 0) {
        LOGE_IF(log_trace, "iwnn_wlf_load_lang: iwnn_wlfi_conf_read()=%d\n", ret);
        /* Free memory */
        //wlf_free_lang(WLF_CLASS *wlf, WLF_LANG_INFO *langinfo);
        langinfo->lang = WLF_LANG_TYPE_NONE;
        return -1;
    }

    return 0;
}

/**
 * Set language
 *
 * @param  wlf_class  WLF_CLASS instance
 * @param  lang  Type of language
 *
 * @retval  0  success
 * @retval -1  error
 */
static WLF_INT16 iwnn_wlf_set_lang(WLF_UINT8 *wlf_class, WLF_UINT8 lang)
{
    WLF_CLASS       *wlf;
    WLF_LANG_INFO   *langinfo;
    int i;

        LOGI_IF(log_trace, "iwnn_wlf_set_lang(%d)\n", lang);

    if (wlf_class == NULL) {
        return -1;
    }

    /* Cast to WLF_CLASS */
    wlf = (WLF_CLASS *)wlf_class;

    langinfo = iwnn_wlfi_find_langinfo(wlf, lang);
    if (langinfo == NULL) {
        /* Case: try to use language without mounting dictionary  */
        return -1;
    }

    /* Set language */
    wlf->lang = lang;

    /* Copy a dictionary set for the language */
    wlf->dicset = langinfo->dicset;

    /* Language changed, so download dictionary set to wlf->dicset */
    for (i = 0; i < IWNN_DL_DIC_MAX; i++) {
        iwnn_wlfi_set_download_dicset(i, wlf, langinfo);
    }
    NJ_SET_ENV(wlf->iwnn, wlf->dicset);

    /* Initialize iWnn Engine */
    njx_init(&(wlf->iwnn), &(wlf->option));

    /* Set a pseudo candidate table */
    iwnn_wlfi_set_giji_table(wlf);

        LOGI_IF(log_trace, "iwnn_wlf_set_lang(%d) return\n", lang);
    return 0;
}

/**
 * Release all dictionaries
 *
 * @param  wlf  WLF_CLASS Instance
 */
static void iwnn_wlf_close_dictionary(WLF_CLASS *wlf)
{
    WLF_LANG_INFO *langinfo;
    NJ_DIC_INFO *dicinfo;
    WLF_DIC_PROP *dicprop;
    int lang, dicno, i;

    LOGI_IF(log_trace, "iwnn_wlf_close_dictionary()\n");

    for (lang = 0; lang < WLF_LANG_TYPE_USE_MAX; lang++) {
        langinfo = &(wlf->lang_info[lang]);
        if (langinfo->lang == WLF_LANG_TYPE_NONE) {
            continue;
        }

        for (dicno = 0; dicno < count_standard_dictionaries(); dicno++) {
            dicinfo = &(langinfo->dicset.dic[dicno]);
            dicprop = &(langinfo->dicprops[dicno]);
            (void)iwnn_wlfi_write_out_dictionary(wlf, dicinfo, dicprop);
        }

        iwnn_wlfi_force_dicset(langinfo);
    }

    iwnn_wlfi_close_download_dic(wlf);

    memset(&(wlf->iwnn.dic_set), 0, sizeof(wlf->iwnn.dic_set));
    memset(&(wlf->dicset), 0, sizeof(wlf->dicset));
    memset(&(wlf->dicset_empty), 0, sizeof(wlf->dicset_empty));

    wlf->lang = WLF_LANG_TYPE_NONE;
    wlf->package[0] = '\0';
    wlf->password[0] = '\0';

    LOGI_IF(log_trace, "iwnn_wlf_close_dictionary() return\n");
}

/**
 * Release the specified dictionary language
 *
 * @param  langinfo  Language information structure
 */
static void iwnn_wlfi_force_dicset(WLF_LANG_INFO *langinfo)
{
    NJ_DIC_SET *dicset;
    WLF_DIC_PROP *dicprop;
    NJ_DIC_INFO *dicinfo;
    int dicno, i, j;

    LOGI_IF(log_trace, "iwnn_wlfi_force_dicset()\n");

    dicset = &langinfo->dicset;

    /* Dictionary release */
    for (dicno = 0; dicno < NJ_MAX_DIC; dicno++) { // include Additional dictionary.
        dicinfo = &(dicset->dic[dicno]);
        dicprop = &(langinfo->dicprops[dicno]);
        iwnn_wlfi_force_dicinfo(langinfo, dicinfo, dicprop);
    }

    /* Dictionary handle initialization */
    for (i = 0; i < NJ_MODE_TYPE_MAX; i++) {
        for (j = 0; j < NJ_MODE_TYPE_MAX; j++) {
            if (i == j) {
                continue;
            }
            if (dicset->rHandle[i] == dicset->rHandle[j]) {
                dicset->rHandle[j] = NULL;
            }
        }
        free(dicset->rHandle[i]);
        dicset->rHandle[i] = NULL;
    }

    /* Flexible character set release & initialization */
    for(i = 0; i < langinfo->charset.charset_count; i++) {
        free(langinfo->charset.from[i]);
        free(langinfo->charset.to[i]);
        langinfo->charset.from[i] = NULL;
        langinfo->charset.to[i] = NULL;
    }
    langinfo->charset.charset_count = 0;

    langinfo->lang = WLF_LANG_TYPE_NONE;

    LOGI_IF(log_trace, "iwnn_wlfi_force_dicset() return\n");
}

/**
 * Release the specified dictionary
 *
 * @param  langinfo Language information structure
 * @param  dicinfo  Dictionary management structure
 * @param  dicprop  Dictionary information structure
 */
static void iwnn_wlfi_force_dicinfo(WLF_LANG_INFO *langinfo, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop)
{
    fmap_t *fmap;
    int reference = 0;

    LOGI_IF(log_trace, "iwnn_wlfi_force_dicinfo()\n");

    if (dicinfo->srhCache) {
        free(dicinfo->srhCache);
        dicinfo->srhCache = NULL;
    }

    if ((dicinfo->type != NJ_DIC_H_TYPE_NORMAL) && (dicprop->type != WLF_DIC_TYPE_HRL_GIJI)) {
        goto Done;
    }

    if ((dicinfo->handle) && (dicprop->type != WLF_DIC_TYPE_HRL_GIJI)) {
        fmap = &(dicprop->fmap_handle);

        if ((fmap->addr != NULL) && (fmap->size != 0)) {
            reference = iwnn_dic_decrement_reference(langinfo, fmap->path);
        }

        if ((fmap->reference_count == 0) || (reference == 1)) {
            fmap_close(fmap);
        }
    }

    if (dicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT]) {
        fmap_close(&(dicprop->fmap_ext_area));
    }
    if (dicinfo->ext_area[NJ_TYPE_EXT_AREA_INPUT]) {
        fmap_close(&(dicprop->fmap_ext_area_input));
    }

Done:
    if (dicprop->type == 12 && dicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT]) {
        free(dicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
        dicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT] = NULL;
    }
    memset(dicinfo, 0, sizeof(NJ_DIC_INFO));
    memset(dicprop, 0, sizeof(WLF_DIC_PROP));
    dicprop->type = -1;

    LOGI_IF(log_trace, "iwnn_wlfi_force_dicinfo() return\n");
}

/**
 * @brief Learn or User dictionary initialization
 *
 * @param iwnn  iWnnINFO Instance
 * @param type  LearnDictionary or UserDictionary
 * @param language Kind of Language
 * @param dictionary Type of Dictionary
 * @retval  0  Success
 * @retval -1  Failed
 */
static int iwnn_wlf_reset_dictionary(WLF_CLASS *wlf, int dictype, int language, int dictionary)
{
    int result = -1;

    if (dictype == WLF_DIC_TYPE_LEARN) {
        result = iwnn_wlfi_reset_learn_dictionary(wlf, language);
        if (result < 0) {
            return result;
        }
        result = iwnn_wlfi_reset_hrlgiji_dictionary(wlf, language);
    } else if (dictype == WLF_DIC_TYPE_USER) {
        result = iwnn_wlfi_reset_user_dictionary(wlf, language, dictionary);
    }

    return result;
}
/**
 * @brief Extended Info reset
 *
 * @param wlf  iWnnINFO Instance
 * @param language Kind of Language
 * @retval  0  Success
 * @retval -1  Failed
 */
static int iwnn_wlf_reset_extended_info(char *file_name)
{
    int result = -1;
    int mode = PARSE_STATE_DIC_CONF;
    FILE *fp;
    char *cret;
    char buf[256] = {0};
    char fname[WLF_FILE_PATH_LEN] = {0};
    unsigned  q_high, q_base, cache_flg, stmoji, dictype;
    unsigned  f_high, f_base, m_high, m_base;

    fp = fopen(file_name, "rt");
    if (fp == NULL) {
        LOGE_IF(log_trace, "iwnn_wlf_reset_extended_info[fp == NULL] ret=(-1)");
        return -1;
    }

    cret = fgets(buf, sizeof(buf), fp);
    if (cret != NULL) {
        buf[strlen(buf) - 1] = '\0';
        /* check the conf's format type */
        if (strcmp(buf, NAME_DIC_CONF) == 0 ) {
            /* read configuration */
            while (1) {
                memset(buf, 0, sizeof(buf));
                if (fgets(buf, sizeof(buf), fp) == NULL) {
                    break;
                }
                if (buf[0] == ';') {
                    /* ignore a comment line */
                    LOGI_IF(log_trace,"iwnn_wlf_reset_extended_info[comment ';']");
                    continue;
                } else if((buf[0] == '\r' && buf[1] == '\n') || buf[0] == '\n') {
                    /* ignore a comment line */
                    LOGI_IF(log_trace,"iwnn_wlf_reset_extended_info[comment 'ret']");
                    continue;
                } else if (memcmp(buf, NAME_DIC_MODE, sizeof(NAME_DIC_MODE)-1) == 0) {
                    LOGI_IF(log_trace,"iwnn_wlf_reset_extended_info[PARSE_STATE_DIC_MODE]");
                    break;
                } else if (memcmp(buf, NAME_DIC_CHARSET_SIZE, sizeof(NAME_DIC_CHARSET_SIZE)-1) == 0) {
                    LOGI_IF(log_trace,"iwnn_wlf_reset_extended_info[PARSE_STATE_DIC_CHARSET_SIZE]");
                    break;
                } else if (memcmp(buf, NAME_DIC_CHARSET_FROM, sizeof(NAME_DIC_CHARSET_FROM)-1) == 0) {
                    LOGI_IF(log_trace,"iwnn_wlf_reset_extended_info[PARSE_STATE_DIC_CHARSET_FROM]");
                    break;
                } else if (memcmp(buf, NAME_DIC_CHARSET_TO, sizeof(NAME_DIC_CHARSET_TO)-1) == 0) {
                    LOGI_IF(log_trace,"iwnn_wlf_reset_extended_info[PARSE_STATE_DIC_CHARSET_TO]");
                    break;
                } else if (mode == PARSE_STATE_DIC_CONF) {
                    sscanf(buf, "%s%u%u%u%u%u%u%u%u%u", fname,  /* filename */
                           &q_high, &q_base, /* dict. freq. */
                           &f_high, &f_base,
                           &m_high, &m_base,
                           &cache_flg, /* ambiguous search cache ON/OFF */
                           &stmoji,    /* min. limit of number of charactors for searching dict. */
                           &dictype);  /* type of dict. */
                    if (WLF_DIC_TYPE_LEARN == dictype) {
                        result = iwnn_wlfi_reset_learning_extended_info(fname);
                        if (result < 0) {
                            LOGI_IF(log_trace,"iwnn_wlf_reset_extended_info -- fail");
                            break;
                        }
                    }
                }
            }
        }
    }
    fclose(fp);

    return result;
}
/**
 * Write out to dictionary files
 * (Learning Dictionary,Learning Dictionary,Frequency Dictionary)
 * (Redundant processing)
 *
 * @param  fmap     File identifier
 * @param  dir_path iWnn user data directory path
 * @param  dictype  Dictionary Type
 * @param  package  package
 * @param  password password
 *
 * @retval  0  success
 * @retval -1  error
 */
static int iwnn_wlfi_write_out_file(fmap_t *fmap, char *data_area_path, int dictype, char *package, char* password)
{
    char tmp_path[WLF_FILE_PATH_LEN] = {0};

    if (package[0] == '\0' || dictype == WLF_DIC_TYPE_DECOEMOJI) {
        snprintf(tmp_path, sizeof(tmp_path), IWNN_DICSET_BACKUP_TMP_PATH, data_area_path, basename(fmap->path));
    }
    else {
        snprintf(tmp_path, sizeof(tmp_path), IWNN_DICSET_BACKUP_SERVICE_TMP_PATH, data_area_path, package, password, basename(fmap->path));
    }

    if (fmap_backup(fmap, tmp_path) < 0) {
        return -1;
    }

    return 0;
}

/**
 * Write out to dictionary files
 *
 * @param  wlf  WLF_CLASS Instance
 * @param  dicinfo  Dictionary information structure
 * @param  dicprop  Dictionary management structure
 *
 * @retval  0  success
 * @retval -1  error
 */
static int iwnn_wlfi_write_out_dictionary(WLF_CLASS *wlf, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop)
{
    fmap_t *fmap = NULL;
    int result = 0;

    if ((dicinfo->type != NJ_DIC_H_TYPE_NORMAL) && (dicprop->type != WLF_DIC_TYPE_HRL_GIJI)) {
        goto ExitDone;
    }

    if (dicprop->type == WLF_DIC_TYPE_LEARN || dicprop->type == WLF_DIC_TYPE_USER) {
        fmap = &(dicprop->fmap_handle);
    }
    else {
        fmap = &(dicprop->fmap_ext_area);
    }

    if (!fmap || !(fmap->addr)) {
        goto ExitDone;
    }

    result = iwnn_wlfi_write_out_file(fmap, wlf->data_area_path, dicprop->type, wlf->package, wlf->password);

ExitDone:
    if ((result < 0) && (fmap != NULL)) {
        LOGE_IF(log_trace, "write out dictionary -- failed to backup \"%s\"\n", fmap->path);
        return -1;
    }
    return 0;
}

/**
 * Initialize a learning dictionary
 *
 * @param  wlf  WLF_CLASS instance
 * @param  language Initialized language
 *
 * @retval  1  success
 * @retval -1  Failed
 */
static int iwnn_wlfi_reset_learn_dictionary(WLF_CLASS *wlf, int language)
{
    WLF_LANG_INFO *langinfo;
    NJ_DIC_INFO *dicinfo;
    WLF_DIC_PROP *dicprop;
    int dicno;
    int result;

    langinfo = iwnn_wlfi_find_langinfo(wlf, language);
    if (langinfo == NULL) {
        return -1;
    }

    for (dicno = 0; dicno < count_standard_dictionaries(); dicno++) {
        dicinfo = &(langinfo->dicset.dic[dicno]);
        dicprop = &(langinfo->dicprops[dicno]);
        if ((dicprop->type == WLF_DIC_TYPE_LEARN) || (dicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL)) {
            result = iwnn_wlfi_reset_dictionary(wlf, dicinfo, dicprop);
            if (result < 0) {
                LOGE_IF(log_trace, "reset learn dictionary -- failed to reset lang=%d dicno=%d type=%d\n", language, dicno, dicprop->type);
            }

            if (dicprop->type == WLF_DIC_TYPE_LEARN) {
                langinfo->learn_handle = dicinfo->handle;
            }
        }
    }
    return 1;
}

 /**
 * @brief User dictionary initialization
 *
 * @param wlf  WLF_CLASS Instance
 * @param language Kind of Language
 * @param dictionary Type of Dictionary
 * @retval  0  Success
 * @retval -1  Failed
 */
static int iwnn_wlfi_reset_user_dictionary(WLF_CLASS *wlf, int language, int dictionary)
{
    WLF_LANG_INFO *langinfo;
    NJ_DIC_INFO  *dicinfo;
    WLF_DIC_PROP *dicprop;
    int dicno;
    int result;


    langinfo = iwnn_wlfi_find_langinfo(wlf, language);
    if (langinfo == NULL) {
        return -1;
    }

    for (dicno = 0; dicno < count_standard_dictionaries(); dicno++) {
        dicprop = &(langinfo->dicprops[dicno]);
        if ((dicprop->type == WLF_DIC_TYPE_USER) && (dicprop->mode[dictionary])) {
            dicinfo = &(langinfo->dicset.dic[dicno]);

            /* Reset a user dictionary */
            result = iwnn_wlfi_reset_dictionary(wlf, dicinfo, dicprop);
            if (result < 0) {
                LOGE_IF(log_trace, "reset user dictionary -- failed to reset\n");
            }
        }
    }

    return 1;
}

/**
 * Initialize a Part Of Speech Link learning dictionary
 *
 * @param  wlf  WLF_CLASS instance
 * @param  language Initialized language
 *
 * @retval  1  success
 * @retval -1  Failed
 */
static int iwnn_wlfi_reset_hrlgiji_dictionary(WLF_CLASS *wlf, int language)
{
    WLF_LANG_INFO *langinfo;
    NJ_DIC_INFO *dicinfo;
    WLF_DIC_PROP *dicprop;
    int dicno;
    int result;

    langinfo = iwnn_wlfi_find_langinfo(wlf, language);
    if (langinfo == NULL) {
        return -1;
    }

    for (dicno = 0; dicno < count_standard_dictionaries(); dicno++) {
        dicinfo = &(langinfo->dicset.dic[dicno]);
        dicprop = &(langinfo->dicprops[dicno]);
        if ((dicprop->type == WLF_DIC_TYPE_HRL_GIJI) && (dicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL)) {
            result = iwnn_wlfi_reset_dictionary(wlf, dicinfo, dicprop);
            if (result < 0) {
                LOGE_IF(log_trace, "reset hrlgiji dictionary -- failed to reset lang=%d dicno=%d type=%d\n", language, dicno, dicprop->type);
            }
        }
    }
    return 1;
}

/**
 * Initialize Learning, Use and Frequency dictionary
 *
 * @param  wlf  WLF_CLASS instance
 * @param  dicinfo  Dictionary info
 * @param  dicprop  Dictionary info
 *
 * @retval  0  success
 * @retval -1  error
 */
static int iwnn_wlfi_reset_dictionary(WLF_CLASS *wlf, NJ_DIC_INFO *dicinfo, WLF_DIC_PROP *dicprop)
{
    char tmp_path[WLF_FILE_PATH_LEN];
    fmap_t *fmap;
    NJ_DIC_HANDLE handle;
    NJ_VOID *ext_area;
    NJ_UINT32 size;
    int dictype, result;


    if (dicprop->type == WLF_DIC_TYPE_USER) {
        fmap = &(dicprop->fmap_handle);
        handle = (NJ_DIC_HANDLE)fmap->addr;
        ext_area = NULL;
        size = (NJ_UINT32)fmap->size;
        dictype = WLF_DIC_TYPE_USER;
    }
    else if (dicprop->type == WLF_DIC_TYPE_LEARN) {
        fmap = &(dicprop->fmap_handle);
        handle = (NJ_DIC_HANDLE)fmap->addr;
        ext_area = NULL;
        size = (NJ_UINT32)fmap->size;
        dictype = WLF_DIC_TYPE_LEARN;
    }
    else if (dicprop->type == WLF_DIC_TYPE_HRL_GIJI) {
        fmap = &(dicprop->fmap_ext_area);
        handle = dicinfo->handle;
        ext_area = (NJ_VOID *)fmap->addr;
        size = (NJ_UINT32)fmap->size;
        dictype = WLF_DIC_TYPE_HRL_GIJI;
    }
    else {
        fmap = &(dicprop->fmap_ext_area);
        handle = dicinfo->handle;
        ext_area = (NJ_VOID *)fmap->addr;
        size = (NJ_UINT32)fmap->size;
        dictype = WLF_DIC_TYPE_EXT_DIC;
    }

    if (wlf->package[0] == '\0' || dicprop->type == WLF_DIC_TYPE_DECOEMOJI) {
        snprintf(tmp_path, sizeof(tmp_path), IWNN_DICSET_BACKUP_TMP_PATH, wlf->data_area_path, basename(fmap->path));
    }
    else {
        snprintf(tmp_path, sizeof(tmp_path), IWNN_DICSET_BACKUP_SERVICE_TMP_PATH, wlf->data_area_path, wlf->package, wlf->password, basename(fmap->path));
    }

    (void)iwnn_dic_init(&(wlf->iwnn), handle, ext_area, size, dictype, WLF_DIC_OPEN_CREAT, NJ_TYPE_EXT_AREA_DEFAULT);
    result = fmap_backup(fmap, tmp_path);

    return result;
}
/**
 * Reset Learning extended Info
 *
 * @param  dicprop  Dictionary info
 *
 * @retval  0  success
 * @retval -1  error
 */
static int iwnn_wlfi_reset_learning_extended_info(char *file_name)
{
    fmap_t fmap;
    NJ_DIC_HANDLE handle;
    int result;
    struct stat sb;

     result = stat(file_name, &sb);
    if (result < 0) {
        LOGI_IF(log_trace,"iwnn_wlfi_reset_learning_extended_info -- file not found\n");
        return 0;
    }
   if (fmap_open(&fmap, file_name, O_RDWR, S_IRUSR|S_IWUSR, WLF_LEARN_DIC_SIZE) < 0) {
        LOGI_IF(log_trace,"iwnn_wlfi_reset_learning_extended_info -- failed to open\n");
        return -1;
    }
    handle = (NJ_DIC_HANDLE)(&fmap)->addr;

    result = iwnn_learning_extended_info_reset(handle);

    fmap_close(&fmap);

    return 0;
}
/**
 * Set status for iWnn
 *
 * @param  wlf  WLF_CLASS instance
 * @param  state  State settings
 *
 * @retval  1  success
 * @retval -1  error
 */
static int iwnn_wlf_set_state(WLF_CLASS *wlf, NJ_STATE *state)
{
    NJ_INT16 result;

    result = njx_set_state(&(wlf->iwnn), state);
    if (result < 0) {
        char *err_msg;
        err_msg = iwnn_err_message(result);
        LOGE_IF(log_trace, "njx_set_state()=%s\n", err_msg);
        if (err_msg) free(err_msg);
        return -1;
    }
    return 1;
}

/**
 * Get current status from iWnn
 *
 * @param  wlf  WLF_CLASS instance
 * @param  state  State settings
 *
 * @retval   1  success
 * @retval  -1  error
 */
static int iwnn_wlf_get_state(WLF_CLASS *wlf, NJ_STATE *state)
{
    NJ_INT16 result;

    result = njx_get_state(&(wlf->iwnn), state);
    if (result < 0) {
        char *err_msg;
        err_msg = iwnn_err_message(result);
        LOGE_IF(log_trace, "njx_get_state()=%s\n", err_msg);
        if (err_msg) free(err_msg);
        return -1;
    }
    return 1;
}

/**
 * Make index of a pseudo candidate table by a current conversion mode
 *
 * @param  mode  Conversion mode
 *
 * @return  Index of a pseudo candidate table
 */
static NJ_UINT8 iwnn_wlfi_get_gijiset_mode(WLF_UINT8 mode)
{
    NJ_UINT8 giji_mode;

    switch (mode) {
    case WLF_MODE_PREDICTION:
        giji_mode = WLF_GIJISET_PREDICT;
        break;

    case WLF_MODE_CONVERSION:
        giji_mode = WLF_GIJISET_CONVERT;
        break;

    case WLF_MODE_EISUKANA:
        giji_mode = WLF_GIJISET_EISUKANA;
        break;

    default:
        giji_mode = WLF_GIJISET_PREDICT;
        break;
    }

    return giji_mode;
}

/**
 * @brief Get a result of consecutive clause conversion
 *
 * @param[in,out] *wlf_class              Pointer for WLF_CLASS
 * @param[in]      mode                   Mode
 * @param[in]     *stroke                 Reading string
 * @param[in]      devide_pos             Dividing position for a clause(0-origin)
 *                                         0:    No dividing position
 *                                         Not 0:Dividing position(Number of characters)
 * @param[in]      stroke_min             Minimum length of a reading string
 * @param[in]      stroke_max             Maximum length of a reading string
 *
 * @retval  >= 0  Prediction, EISU-KANA:result of getting Conversion:number of clause
 * @retval  <  0  Failure
 */
static WLF_INT16 iwnn_wlf_get_conversion(WLF_UINT8 *wlf_class, WLF_UINT8 mode, WLF_UINT8 *stroke,
                                         WLF_UINT8 devide_pos, WLF_INT32 stroke_min, WLF_INT32 stroke_max)
{
    WLF_CLASS   *wlf;
    NJ_INT16    ret = 0;
    NJ_UINT8    gijiset_mode;
    NJ_CHAR     buf[NJ_MAX_LEN+NJ_TERM_SIZE];
    WLF_UINT16  s_cnt;      /* Searching count */
    WLF_INT16   r_cnt;      /* Get count */
    WLF_INT16   g_cnt;
    NJ_DIC_SET  dicset;
    WLF_PREDICT t_pred;     /* Hold a predicting condition */
    WLF_GIJI_INFO *giji_info;
    const WLF_GIJIPACK_INFO *gijipack;
    const NJ_GIJISET *gijiset;


    if (wlf_class == NULL) {
        /* Enough parameter */
        return WLF_ERR_INVALID_PARAM;
    }

    /* Cast to WLF_CLASS */
    wlf = (WLF_CLASS *)wlf_class;

    iwnn_wlfi_init_giji(wlf);

    if (wlf->bookshelf == WLF_DICSET_TYPE_EISUKANA) {
        mode = WLF_MODE_EISUKANA;
    }

    /* Mode */
    /* Return error, if it's an unexpected mode */
    wlf->mode = mode;

    iwnn_wlf_set_hunhun(wlf, stroke_min, stroke_max);

    /* Initialize a candidate info */
    WLF_SET_INIT_CONVERT  (&wlf->conv_info);
    WLF_SET_INIT_CANDIDATE(&wlf->cand_info);

    gijiset_mode = iwnn_wlfi_get_gijiset_mode(wlf->mode);

    /* Set a pseudo candidate setting */
    iwnn_wlfi_set_giji(wlf, gijiset_mode);

    switch (wlf->mode) {
    case WLF_MODE_PREDICTION :
    case WLF_MODE_EISUKANA :
        /* Set a pseudo candidate setting */
        //iwnn_wlfi_set_giji(wlf, WLF_GIJISET_PREDICT);

        if (stroke_min <= 0) {
            wlf->predict_info.stroke_min = 0;
        }
        else {
            wlf->predict_info.stroke_min = (NJ_UINT8)stroke_min;
        }

        if (stroke_max < 0) {
            wlf->predict_info.stroke_max = NJ_MAX_LEN;
        }
        else {
            wlf->predict_info.stroke_max = (NJ_UINT8)stroke_max;
        }

        s_cnt = 1;
        r_cnt = iwnn_wlfi_get_prediction(wlf, s_cnt);
        if (r_cnt < 0) {
            return r_cnt;
        }

        if (s_cnt != r_cnt) {
            /* If a Searching count and a Get count are different */
            if (wlf->bookshelf == WLF_DICSET_TYPE_KAOMOJI) {
                return 0;
            } else {
                wlf->giji_mode = WLF_MODE_GIJI_ON;
            }
        }

        if (wlf->giji_mode == WLF_MODE_GIJI_ON)  {
            g_cnt = iwnn_wlfi_get_giji(wlf, s_cnt - r_cnt, wlf->mode);
            if (s_cnt != (r_cnt + g_cnt)) {
                /* If a Searching count and a Get count are different */
                return 0;
            }
        }

        ret = 1;
        break;

    case WLF_MODE_CONVERSION :
        /* Set a pseudo candidate setting */
        //iwnn_wlfi_set_giji(wlf, WLF_GIJISET_CONVERT);

        /* Set a dividing position of clauses */
        wlf->conv_info.devide_pos = iwnn_wlfi_calc_devide_pos(wlf, devide_pos);

        /* Consecutive clause conversion */
        ret = iwnn_wlfi_get_conv(wlf);
        if (ret < 0) {
            /* Failure */
            return ret;
        } else if (ret == 0) {
            /* If there's no conversion candidate, Use pseudo candidates */

            /* Get a pseudo candidate */
            ret = njex_get_giji(&(wlf->iwnn),
                                &(wlf->time_info),
                                wlf->input_stroke,
                                NJ_TYPE_HIRAGANA,
                                wlf->cand_info.segment);
            if (ret <= 0) {
                /* Failure */
                return ret;
            }
            wlf->cand_info.segment_cnt = 1;  /* Success */
        } else {
            /* Conversion is succeeded */
            ;
        }

        /* Get a dividing position of clauses */
        ret = njx_get_stroke(&wlf->iwnn,
                             &wlf->cand_info.segment[0],
                             buf,
                             sizeof(buf));
        if (ret < 0) {
            /* Failure */
            return ret;
        }
        wlf->conv_info.devide_pos = nj_strlen(buf);

        ret = wlf->cand_info.segment_cnt;
        break;
#if 0
    case WLF_MODE_EISUKANA :
        /* Set a pseudo candidate setting */
        //iwnn_wlfi_set_giji(wlf, WLF_GIJISET_EISUKANA);

        /* Evacuate a prediction searching condition */
        memcpy(&t_pred, &wlf->predict_info, sizeof(WLF_PREDICT));

        /* Set a searching parameter for getting EISU-KANA candidates */
        wlf->predict_info.flexible_search = WLF_FLEXIBLE_SEARCH_OFF;
        wlf->predict_info.mode = (NJ_NO_LEARN | NJ_NO_YOSOKU | NJ_NO_RENBUN | NJ_NO_TANBUN);
        wlf->predict_info.forecast_learn_limit = 0;
        wlf->predict_info.forecast_limit = 0;
        wlf->predict_info.stroke_min = 0;
        wlf->predict_info.stroke_max = NJ_MAX_LEN;

        /* Start searching */
        giji_info = &(wlf->giji_info);
        gijipack = giji_info->gijipack;
        gijiset = gijipack->module[WLF_GIJISET_EISUKANA];

        ret = iwnn_wlfi_get_prediction(wlf, gijiset->count);

        /* Restore a searching parameter to be original */
        memcpy(&wlf->predict_info, &t_pred, sizeof(WLF_PREDICT));
        break;
#endif
    default :
        break;
    }

    return ret;
}

/**
 * @brief Get a candidate string
 *
 * Description
 *  - Check an excess between an index number and a count of getting candidate type.
 *  - Get a candidate from a candidate list info, when a getting candidate type is "a candidate lists"
 *  - Get a candidate from a clause info when a getting candidate type is "a clause"
 *  - Get a candidate from a word searching result when a getting candidate type is "a word"
 *
 * @param[in,out] *wlf_class Pointer for WLF_CLASS
 * @param[in]     seg_pos    Position of a clause(0-origin)
 * @param[in]     index      Index number(0-origin)
 * @param[out]    *buf       Output buffer
 * @param[in]     size       Size of buffer
 *
 * @retval  >= 0  Some candidates exist
 * @retval  =  0  No candidate
 * @retval  <  0  Failure
 */
static WLF_INT16 iwnn_wlf_get_string(WLF_UINT8 *wlf_class, WLF_UINT16 seg_pos, WLF_UINT16 index, WLF_UINT8 *buf, WLF_UINT16 size) {
    WLF_CLASS       *wlf;
    WLF_CAND        *pcand;
    WLF_CONV        *pconv;
    WLF_GIJI_INFO   *giji_info;
    WLF_UINT16      s_cnt = 0;  /* Searching count */
    WLF_UINT16      t_cnt;
    WLF_INT16       r_cnt = 0;  /* Get count */
    WLF_INT16       g_cnt = 0;  /* Getting pseudo candidate count */
    WLF_UINT16      candidate_cnt;
    NJ_CHAR         tmp[NJ_MAX_RESULT_LEN+NJ_TERM_SIZE];
    NJ_INT16        result;
    WLF_INT16       length;

    if ((wlf_class == NULL)  ||
        (buf       == NULL)) {
        /* Invalid parameter */
        return WLF_ERR_INVALID_PARAM;
    }

    /* Cast to WLF_CLASS */
    wlf = (WLF_CLASS *)wlf_class;
    pcand = &(wlf->cand_info);
    pconv = &(wlf->conv_info);
    giji_info = &(wlf->giji_info);

    switch (wlf->mode) {
    case WLF_MODE_PREDICTION :
    case WLF_MODE_EISUKANA :
        if (seg_pos != 0) {
            /* No clause in a prediction mode */
            return (-1);
        }

        candidate_cnt = pcand->candidate_cnt + giji_info->giji_cnt;
        if (candidate_cnt <= index) {
            /* Get a candidate if it's not got yet */
            s_cnt = (index + 1) - candidate_cnt;

            if (wlf->giji_mode != WLF_MODE_GIJI_ON) {
                r_cnt = iwnn_wlfi_get_prediction(wlf, s_cnt);
                if (r_cnt < 0) {
                    /* Failure */
                    return r_cnt;
                }
                if (s_cnt != r_cnt) {
                    /* If a Searching count and a Get count are different */
                    if (wlf->bookshelf == WLF_DICSET_TYPE_KAOMOJI) {
                        return 0;
                    } else {
                        wlf->giji_mode = WLF_MODE_GIJI_ON;
                    }
                }
            }

            if (wlf->giji_mode == WLF_MODE_GIJI_ON) {
                g_cnt = iwnn_wlfi_get_giji(wlf, s_cnt - r_cnt, wlf->mode);
                if (s_cnt != (r_cnt + g_cnt)) {
                     /* If a Searching count and a Get count are different */
                    return 0;
                }
            }
        }

        break;

    case WLF_MODE_CONVERSION :
        if (seg_pos >= pcand->segment_cnt) {
            /* Invalid clause number */
            return (0);
        }

        if (seg_pos != pconv->fix_seg_cnt) {
            /* A clause is different from a cached candidate */
            pconv->fix_seg_cnt = seg_pos;
            pcand->candidate_cnt = 0;
            iwnn_wlfi_init_giji(wlf);
            iwnn_wlf_make_convert_giji_stroke(wlf, seg_pos);
        }

        candidate_cnt = pcand->candidate_cnt + giji_info->giji_cnt;
        if (candidate_cnt <= index) {
            /* Get a candidate if it's not got yet */
            s_cnt = (index + 1) - candidate_cnt;

            if (wlf->giji_mode != WLF_MODE_GIJI_ON) {
                r_cnt = iwnn_wlfi_get_zenkouho(wlf, s_cnt, &t_cnt);
                if (r_cnt < 0) {
                    /* Failure */
                    return r_cnt;
                }
                if (s_cnt != r_cnt) {
                    /* If a Searching count and a Get count are different */
                    wlf->giji_mode = WLF_MODE_GIJI_ON;
                }
            }

            if (wlf->giji_mode == WLF_MODE_GIJI_ON) {
                g_cnt = iwnn_wlfi_get_giji(wlf, s_cnt - r_cnt, wlf->mode);
                if (s_cnt != (r_cnt + g_cnt)) {
                    /* If a Searching count and a Get count are different */
                    return 0;
                }
            }
        }

        break;
#if 0
    case WLF_MODE_EISUKANA :
        if (seg_pos != 0) {
            /* No combined clauses in EISU-KANA mode */
            return (-1);
        }

        if (index >= pcand->candidate_cnt) {
            /* Index is more than or equal to a number of candidates */
            return (0);
        }

        break;
#endif
    default:
        return (0);
    }

    if (index < pcand->candidate_cnt) {
        result = njx_get_candidate(&wlf->iwnn, &pcand->candidate[index], tmp, sizeof(tmp));
    }
    else {
        index -= pcand->candidate_cnt;
        result = njx_get_candidate(&(wlf->iwnn), &(giji_info->giji_candidate[index]), tmp, sizeof(tmp));
    }

    if (result <= 0) {
        return (-1);
    }

    /* Convert internal character code -> external character code */
    length = iwnn_wlfi_convert_external(wlf, tmp, buf, size);
    if (length <= 0) {
        return (-1);
    }

    return (1);
}


/**
 * @brief Get a reading string
 *
 * Description
 *  - Check an excess between an index number and a count of getting candidate type.
 *  - Get a string from a candidate list info, when a getting candidate type is "a candidate lists"
 *  - Get a string from a clause info when a getting candidate type is "a clause"
 *  - Get a string from a word searching result when a getting candidate type is "a word"
 *
 * @param[in,out] *wlf_class pointer for WLF_CLASS
 * @param[in]     seg_pos    Position of clauses(0-origin)
 * @param[in]     index      Index number(0-origin)
 * @param[out]    *buf       Output buffer
 * @param[in]     size       Size of buffer
 *
 * @retval  >= 0  Byte count of a reading string
 * @retval  <  0  Failure
 */
static WLF_INT16 iwnn_wlf_get_stroke(WLF_UINT8 *wlf_class, WLF_UINT16 seg_pos, WLF_UINT16 index, WLF_UINT8 *buf, WLF_UINT16 size) {
    WLF_CLASS       *wlf;
    WLF_CAND        *pcand;
    WLF_CONV        *pconv;
    WLF_GIJI_INFO   *giji_info;
    WLF_UINT16      s_cnt = 0;    /* Searching count */
    WLF_UINT16      t_cnt;
    WLF_INT16       r_cnt = 0;    /* Get count */
    WLF_INT16       g_cnt = 0;
    WLF_UINT16      candidate_cnt;
    NJ_CHAR         tmp[NJ_MAX_LEN+NJ_TERM_SIZE];
    NJ_INT16        result;
    WLF_INT16       length;

    if ((wlf_class == NULL)  ||
        (buf       == NULL)) {
        /* Invalid parameter */
        return WLF_ERR_INVALID_PARAM;
    }

    /* Cast to WLF_CLASS */
    wlf = (WLF_CLASS *)wlf_class;
    pcand = &(wlf->cand_info);
    pconv = &(wlf->conv_info);
    giji_info = &(wlf->giji_info);

    switch (wlf->mode) {
    case WLF_MODE_PREDICTION :
    case WLF_MODE_EISUKANA :
        if (seg_pos != 0) {
            /* No combined clauses in prediction mode */
            return (-1);
        }

        candidate_cnt = pcand->candidate_cnt + giji_info->giji_cnt;
        if (candidate_cnt <= index) {
            /* Get a candidate if it's not got yet */
            s_cnt = (index + 1) - candidate_cnt;

            if (wlf->giji_mode != WLF_MODE_GIJI_ON) {
                r_cnt = iwnn_wlfi_get_prediction(wlf, s_cnt);
                if (r_cnt < 0) {
                    /* Failure */
                    return r_cnt;
                }
                if (s_cnt != r_cnt) {
                    /* If a Searching count and a Get count are different */
                    if (wlf->bookshelf == WLF_DICSET_TYPE_KAOMOJI) {
                        return 0;
                    } else {
                        wlf->giji_mode = WLF_MODE_GIJI_ON;
                    }
                }
            }

            if (wlf->giji_mode == WLF_MODE_GIJI_ON) {
                g_cnt = iwnn_wlfi_get_giji(wlf, s_cnt - r_cnt, wlf->mode);
                if (s_cnt != (r_cnt + g_cnt)) {
                    /* If a Searching count and a Get count are different */
                    return 0;
                }
            }
        }

        break;

    case WLF_MODE_CONVERSION :
        if (seg_pos >= pcand->segment_cnt) {
            /* Invalid clause number */
            return (0);
        }

        if (seg_pos != pconv->fix_seg_cnt) {
            /* A clause is different from a cached candidate */
            pconv->fix_seg_cnt = seg_pos;
            pcand->candidate_cnt = 0;
            iwnn_wlfi_init_giji(wlf);
            iwnn_wlf_make_convert_giji_stroke(wlf, seg_pos);
        }

        candidate_cnt = pcand->candidate_cnt + giji_info->giji_cnt;
        if (candidate_cnt <= index) {
            /* Get a candidate if it's not got yet */
            s_cnt = (index + 1) - candidate_cnt;

            if (wlf->giji_mode != WLF_MODE_GIJI_ON) {
                r_cnt = iwnn_wlfi_get_zenkouho(wlf, s_cnt, &t_cnt);
                if (r_cnt < 0) {
                    /* Failure */
                    return r_cnt;
                }
                if (s_cnt != r_cnt) {
                    /* If a Searching count and a Get count are different */
                    wlf->giji_mode = WLF_MODE_GIJI_ON;
                }
            }

            if (wlf->giji_mode == WLF_MODE_GIJI_ON) {
                g_cnt = iwnn_wlfi_get_giji(wlf, s_cnt - r_cnt, wlf->mode);
                if (s_cnt != (r_cnt + g_cnt)) {
                    /* If a Searching count and a Get count are different */
                    return 0;
                }
            }
        }

        break;
#if 0
    case WLF_MODE_EISUKANA :
        if (seg_pos != 0) {
            /* No combined clauses in EISU-KANA mode */
            return (-1);
        }

        if (index >= pcand->candidate_cnt) {
            /* Index is more than or equal to a number of candidates */
            return (0);
        }

        break;
#endif

    default:
        return (0);
    }

    if (index < pcand->candidate_cnt) {
        result = njx_get_stroke(&wlf->iwnn, &pcand->candidate[index], tmp, sizeof(tmp));
    }
    else {
        index -= pcand->candidate_cnt;
        result = njx_get_stroke(&(wlf->iwnn), &(giji_info->giji_candidate[index]), tmp, sizeof(tmp));
    }

    if (result <= 0) {
        return (-1);
    }

    /* Convert internal character code -> external character code */
    length = iwnn_wlfi_convert_external(wlf, tmp, buf, size);
    if (length <= 0) {
        return (-1);
    }

    return (1);
}

/**
 * @brief Learn strings
 *
 * Desription
 *  - Check the result structure if it's a learning target.
 *  - Do learning for the result structure.
 *  - Validate connection learning and relational learning flags
 *  - When a candidate type is the "candidate list"
 *    - Clear the searching result.
 *  - When a candidate type is the "clause"
 *    - Count up a number of committed clauses
 *    - Get a reading string from committing candidates,
 *      update a reading string info.
 *
 * @param[in,out] *wlf_class Pointer for WLF_CLASS
 * @param[in]     seg_pos    Position of a clause(0-origin)
 * @param[in]     index      Index number(0-origin)
 *
 * @retval  >= 0  Rest number of clauses
 * @retval  <  0  Failure
 */
static WLF_INT16 iwnn_wlf_learn_string(WLF_UINT8 *wlf_class, WLF_INT32 seg_pos, WLF_INT32 index) {
    WLF_CLASS       *wlf;
    WLF_CAND        *pcand;
    WLF_CONV        *pconv;
    WLF_GIJI_INFO   *giji_info;
    NJ_RESULT       *result;
    NJ_UINT16       candidate_cnt;
    NJ_UINT16       learn = 0;
    NJ_UINT16       c_cnt;
    NJ_INT16        ret;
    NJ_CHAR         buf[NJ_MAX_LEN+NJ_TERM_SIZE];
    NJ_UINT8        type;

    if (wlf_class == NULL) {
        /* Invalid parameter */
        return WLF_ERR_INVALID_PARAM;
    }

    /* Cast to WLF_CLASS */
    wlf = (WLF_CLASS *)wlf_class;
    pcand = &(wlf->cand_info);
    pconv = &(wlf->conv_info);
    giji_info = &(wlf->giji_info);

    type = WLF_STR_TYPE_CAND;

    switch (wlf->mode) {
    case WLF_MODE_PREDICTION :
    case WLF_MODE_EISUKANA :
        LOGI_IF(log_trace, "iwnn_wlf_learn_string:PRE,EISU");
        if (seg_pos != 0) {
            return WLF_ERR_INVALID_PARAM;
        }
        break;

    case WLF_MODE_CONVERSION :
        LOGI_IF(log_trace, "iwnn_wlf_learn_string:CONV");
        if ((WLF_UINT16)seg_pos >= pcand->segment_cnt) {
            LOGI_IF(log_trace, "iwnn_wlf_learn_string: seg_pos(%d) >= cnt(%d)",
                    (int)seg_pos, pcand->segment_cnt);
            /* seg_pos is greater than the number of get results */
            return (-1);
        }

        if (index < 0) {
            index = seg_pos;
            type = WLF_STR_TYPE_SEG;
            break;
        }

        if ((WLF_UINT16)seg_pos != pconv->fix_seg_cnt) {
            LOGI_IF(log_trace, "iwnn_wlf_learn_string: unmatch pos");
            /* seg_pos is different from the cached clause */
            return (-1);
        }
        break;

    default :
        return (-1);
    }


    candidate_cnt = pcand->candidate_cnt + giji_info->giji_cnt;
    if (type == WLF_STR_TYPE_CAND && (WLF_UINT16)index >= candidate_cnt) {
         LOGI_IF(log_trace, "iwnn_wlf_learn_string: unmatch index");
        /* index is greater than the get result */
        return (-1);
    }

    learn = wlf->learn;

    /* Learning */
    if (learn != 0) {
        LOGI_IF(log_trace, "iwnn_wlf_learn_string: select: index");
        ret = iwnn_wlfi_select(wlf, (WLF_INT16)index, (WLF_INT16)index, type);
    } else {
        LOGI_IF(log_trace, "iwnn_wlf_learn_string: select: -1");
        ret = iwnn_wlfi_select(wlf, -1,    (WLF_INT16)index, type);
    }
    LOGI_IF(log_trace, "iwnn_wlf_learn_string: iwnn_wlfi_select()=%d\n", ret);
    if (ret < 0) {
        /* Failure */
        return ret;
    }

    /* Validate a connection learning and a relational seatching */
    wlf->connect  = WLF_CONNECT_ON;
    wlf->relation = WLF_RELATION_ON;

    /* Initialize a candidate info */
    //WLF_SET_INIT_CONVERT  (&(wlf->conv_info));
    //WLF_SET_INIT_CANDIDATE(&(wlf->cand_info));

    return (0);
}

/**
 * Check a wildcard prediction
 *
 * @param  wlf  WLF_CLASS instance
 * @param  stroke_min  Minimum number of reading characters
 * @param  stroke_max  Maximum number of reading characters
 */
static void iwnn_wlf_set_hunhun(WLF_CLASS *wlf, WLF_INT32 stroke_min, WLF_INT32 stroke_max)
{
    NJ_UINT16 stroke_len, input_len;

    wlf->hunhun_mode = WLF_MODE_HUNHUN_OFF;

    if (wlf->mode == WLF_MODE_PREDICTION) {

        if (0 <= stroke_min) {

            input_len = (NJ_UINT16)stroke_min;

            stroke_len = nj_strlen(wlf->input_stroke);

            if (stroke_len < input_len) {
                wlf->hunhun_mode = WLF_MODE_HUNHUN_ON;
            }
        }
    }

    iwnn_wlfi_set_hunhun(wlf);
}

/**
 * Get a reading string from the specific position of clauses and
 * Use it for converting a pseudo candidate.
 *
 * @param  wlf  WLF_CLASS instance
 * @param  seg_pos  Position of a clause
 */
static void iwnn_wlf_make_convert_giji_stroke(WLF_CLASS *wlf, NJ_UINT8 seg_pos)
{
    WLF_CAND *cand_info;
    WLF_CONV *conv_info;
    WLF_GIJI_INFO *giji_info;
    NJ_INT16 result;


    if (wlf->mode != WLF_MODE_CONVERSION) {
        return;
    }

    cand_info = &(wlf->cand_info);
    conv_info = &(wlf->conv_info);
    giji_info = &(wlf->giji_info);

    if (cand_info->segment_cnt <= seg_pos) {
        return;
    }

    result = njx_get_stroke(&(wlf->iwnn), &(wlf->cand_info.segment[seg_pos]), giji_info->giji_stroke, sizeof(giji_info->giji_stroke));
    if (result < 0) {
        char *err_msg;
        err_msg = iwnn_err_message(result);
        LOGE_IF(log_trace, "njx_get_stroke()=%s\n", err_msg);
        free(err_msg);
        giji_info->giji_stroke[0] = 0;
    }
}

/**
 * Disable to make pseudo candidates in a wildcard prediction
 *
 * @param  wlf  WLF_CLASS instance
 */
static void iwnn_wlfi_set_hunhun(WLF_CLASS *wlf)
{
    WLF_LANG_INFO *langinfo = NULL;
    WLF_DIC_PROP *dicprop = NULL;
    NJ_DIC_INFO *src_dicinfo = NULL;
    NJ_DIC_INFO *dst_dicinfo = NULL;
    int cnt;


    langinfo = iwnn_wlfi_find_langinfo(wlf, wlf->lang);
    if (langinfo == NULL) {
        return;
    }

    for (cnt = 0; cnt < count_standard_dictionaries(); cnt++) {
        dicprop = &(langinfo->dicprops[cnt]);
        if (dicprop->type == WLF_DIC_TYPE_GIJI) {
            src_dicinfo = &(wlf->dicset.dic[cnt]);
            break;
        }
    }

    dst_dicinfo = &(wlf->iwnn.dic_set.dic[cnt]);

    if (wlf->hunhun_mode == WLF_MODE_HUNHUN_OFF) {
        if (src_dicinfo != NULL) {
            dst_dicinfo->handle = src_dicinfo->handle;
        }
    }
    else {
        WLF_GIJI_INFO *giji_info;

        giji_info = &(wlf->giji_info);
        giji_info->giji_stroke[0] = 0;

        if (src_dicinfo != NULL) {
            dst_dicinfo->handle = NULL;
        }
    }

}

/**
 * @brief Set dictionary sets
 *
 * Description
 *  - When a dictionary set type is WLF_DICSET_SUB,
 *    Set an iWnn environment structure as a EMPTY.
 *  - When a dictionary set type is NOT WLF_DICSET_SUB,
 *    Set an iWnn environment structure as a prediction.
 *
 * @param[in,out]   *wlf     Framework class structure
 * @param[in]       type     Type of dictionary sets
 *
 * @retval     Void
 */
static void iwnn_wlfi_set_dicset(WLF_CLASS *wlf, NJ_UINT8 type)
{
    if (type == WLF_DICSET_SUB) {
        /* Restore a dictionary set to an original */
        wlf->dicset.mode = wlf->iwnn.dic_set.mode;
        memset((char *)(wlf->dicset.keyword), 0, NJ_MAX_KEYWORD);
        strncpy((char *)(wlf->dicset.keyword), (char *)(wlf->iwnn.dic_set.keyword), NJ_MAX_KEYWORD);

        NJ_SET_ENV(wlf->iwnn, wlf->dicset_empty);
    } else {
        NJ_SET_ENV(wlf->iwnn, wlf->dicset);
    }

    return;
}

/**
 * The dictionary is loaded from config File.
 *
 * @param wlf       WLF_CLASS Instance
 * @param langinfo  Language information structure
 * @param filename  Config File Name
 *
 * @return  >   0  dictionary Loading successful(Loaded number of dictionaries)
 * @return  <=  0  dictionary Loading failure
 */
static int iwnn_wlfi_conf_read(WLF_CLASS *wlf, WLF_LANG_INFO *langinfo, const char *filename)
{
    NJ_DIC_SET *dicset;
    FILE *fp;
    char buf[256] = {0};
    int mode = PARSE_STATE_DIC_CONF;
    NJ_INT16 ret = 0;
    char *cret;
    unsigned  q_high, q_base, cache_flg, stmoji, dictype;
    unsigned  f_high, f_base, m_high, m_base;
    char fname[WLF_FILE_PATH_LEN] = {0};
    char ext_inputfname[WLF_FILE_PATH_LEN] = {0};
    char fname_changedir[WLF_FILE_PATH_LEN] = {0};
    int  diccnt = 0;
    int  errflg = 0;
    int  charsetidx = 0;
    int  lineno = 0;
    NJ_INT32    size;
    NJ_DIC_INFO *dicinfo;
    WLF_DIC_PROP *dicprop;
    NJ_DIC_INFO *dicinfo_userdic = NULL;
    NJ_DIC_INFO *dicinfo_learndic = NULL;
    struct stat add_dic_stat;
    char add_dic_fname[WLF_FILE_PATH_LEN] = {0};
    int i,j;
    int add_dic_lang = 0;

    dicset = &langinfo->dicset;
    memset(dicset, 0x00, sizeof(NJ_DIC_SET));

    fp = fopen(filename, "rt");
    if (fp == NULL) {
        LOGE_IF(log_trace, "iwnn_wlfi_conf_read[fp == NULL] ret=(-1)");
        return -1;
    }

    cret = fgets(buf, sizeof(buf), fp);
    if (cret != NULL) {
        lineno++;
        buf[strlen(buf) - 1] = '\0';
        /* check the conf's format type */
        if (strcmp(buf, NAME_DIC_CONF) == 0 ) {
            /* load dictionaries */
            LOGI_IF(log_trace,"iwnn_wlfi_conf_read[NAME_DIC_CONF]");

            iwnn_wlfi_load_download_dic(wlf);

            diccnt = 0;
            errflg = 0;
            /* read configuration */
            while (1) {
                memset(buf, 0, sizeof(buf));
                if (fgets(buf, sizeof(buf), fp) == NULL) {
                    break;
                }
                lineno++;
                if (buf[0] == ';') {
                    /* ignore a comment line */
                    LOGI_IF(log_trace,"iwnn_wlfi_conf_read[comment ';']");
                    continue;
                } else if((buf[0] == '\r' && buf[1] == '\n') || buf[0] == '\n') {
                    /* ignore a comment line */
                    LOGI_IF(log_trace,"iwnn_wlfi_conf_read[comment 'ret']");
                    continue;
                } else if (memcmp(buf, NAME_DIC_MODE, sizeof(NAME_DIC_MODE)-1) == 0) {
                    mode = PARSE_STATE_DIC_MODE;
                    LOGI_IF(log_trace,"iwnn_wlfi_conf_read[PARSE_STATE_DIC_MODE]");
                    continue;
                } else if (memcmp(buf, NAME_DIC_CHARSET_SIZE, sizeof(NAME_DIC_CHARSET_SIZE)-1) == 0) {
                    mode = PARSE_STATE_DIC_CHARSET_SIZE;
                    LOGI_IF(log_trace,"iwnn_wlfi_conf_read[PARSE_STATE_DIC_CHARSET_SIZE]");
                    continue;
                } else if (memcmp(buf, NAME_DIC_CHARSET_FROM, sizeof(NAME_DIC_CHARSET_FROM)-1) == 0) {
                    mode = PARSE_STATE_DIC_CHARSET_FROM;
                    charsetidx = 0;
                    LOGI_IF(log_trace,"iwnn_wlfi_conf_read[PARSE_STATE_DIC_CHARSET_FROM]");
                    continue;
                } else if (memcmp(buf, NAME_DIC_CHARSET_TO, sizeof(NAME_DIC_CHARSET_TO)-1) == 0) {
                    mode = PARSE_STATE_DIC_CHARSET_TO;
                    charsetidx = 0;
                    LOGI_IF(log_trace,"iwnn_wlfi_conf_read[PARSE_STATE_DIC_CHARSET_TO]");
                    continue;
                } else if (mode == PARSE_STATE_DIC_CONF) {
                    ext_inputfname[0] = '\0';
                    sscanf(buf, "%s%u%u%u%u%u%u%u%u%u%s", fname,  /* filename */
                           &q_high, &q_base, /* dict. freq. */
                           &f_high, &f_base,
                           &m_high, &m_base,
                           &cache_flg, /* ambiguous search cache ON/OFF */
                           &stmoji,    /* min. limit of number of charactors for searching dict. */
                           &dictype,  /* type of dict. */
                           ext_inputfname); /* extfilename */

                    if (USE_EXT_INPUT == 0) {
                        ext_inputfname[0] = '\0';
                    }

                    dicprop = &(langinfo->dicprops[diccnt]);

                    switch (dictype) {
                    case 1: /* rule dictionary */
                        LOGI_IF(log_trace, "iwnn_wlfi_conf_read -- path=\"%s\",type=%d\n", fname, dictype);
                        if ( read_binary(fname, &(dicset->rHandle[0])) > 0 ) {
                            LOGI_IF(log_trace, "iwnn_wlfi_conf_readi ruledictionary mount OK ");
                            dicset->rHandle[1] = dicset->rHandle[0];
                            dicset->rHandle[2] = dicset->rHandle[0];
                        } else {
                            LOGE_IF(log_trace, "iwnn_wlfi_conf_read[rule dictionary parse NG][%d]",lineno);
                            errflg = 1;
                        }
                        break;
                    case 3: /* user dictionary */

                        dicinfo = &(dicset->dic[diccnt]);
                        dicinfo->type = NJ_DIC_H_TYPE_NORMAL;
                        dicinfo->dic_freq[NJ_MODE_TYPE_HENKAN].base = q_base;
                        dicinfo->dic_freq[NJ_MODE_TYPE_HENKAN].high = q_high;
                        dicinfo->dic_freq[NJ_MODE_TYPE_YOSOKU].base = f_base;
                        dicinfo->dic_freq[NJ_MODE_TYPE_YOSOKU].high = f_high;
                        dicinfo->dic_freq[NJ_MODE_TYPE_MORPHO].base = m_base;
                        dicinfo->dic_freq[NJ_MODE_TYPE_MORPHO].high = m_high;
                        dicinfo->limit = stmoji;

                        strncpy(fname_changedir, wlf->data_area_path, WLF_FILE_PATH_LEN);
                        strcat(fname_changedir, fname);
                        memcpy(dicprop->name, fname_changedir, sizeof(dicprop->name));
                        memcpy(dicprop->ext_inputname, ext_inputfname, sizeof(dicprop->ext_inputname));
                        dicprop->type = dictype;

                        if (iwnn_dic_mount(wlf, langinfo, dicinfo, dicprop, cache_flg) < 0) {
                            LOGE_IF(log_trace, "iwnn_wlfi_conf_read[user dictionary parse NG][%d]",lineno);
                            errflg = 1;
                        } else {
                            dicinfo_userdic = dicinfo;
                        }

                        LOGI_IF(log_trace, "iwnn_wlfi_conf_read[dictype/diccnt][%d/%d]",dictype,diccnt);
                        diccnt++;

                        break;
                    case 2: /* learning dictionary */

                        dicinfo = &(dicset->dic[diccnt]);
                        dicset->dic[diccnt].type = NJ_DIC_H_TYPE_NORMAL;
                        dicset->dic[diccnt].dic_freq[NJ_MODE_TYPE_HENKAN].base = q_base;
                        dicset->dic[diccnt].dic_freq[NJ_MODE_TYPE_HENKAN].high = q_high;
                        dicset->dic[diccnt].dic_freq[NJ_MODE_TYPE_YOSOKU].base = f_base;
                        dicset->dic[diccnt].dic_freq[NJ_MODE_TYPE_YOSOKU].high = f_high;
                        dicset->dic[diccnt].dic_freq[NJ_MODE_TYPE_MORPHO].base = m_base;
                        dicset->dic[diccnt].dic_freq[NJ_MODE_TYPE_MORPHO].high = m_high;
                        dicset->dic[diccnt].limit = stmoji;

                        strncpy(fname_changedir, wlf->data_area_path, WLF_FILE_PATH_LEN);
                        strcat(fname_changedir, fname);
                        memcpy(dicprop->name, fname_changedir, sizeof(dicprop->name));
                        memcpy(dicprop->ext_inputname, ext_inputfname, sizeof(dicprop->ext_inputname));
                        dicprop->type = dictype;

                        if (iwnn_dic_mount(wlf, langinfo, dicinfo, dicprop, cache_flg) < 0) {
                            LOGE_IF(log_trace, "iwnn_wlfi_conf_read[learning dictionary parse NG][%d]",lineno);
                            errflg = 1;
                        } else {
                            dicinfo_learndic = dicinfo;
                        }

                        langinfo->learn_handle = dicinfo->handle;

                        LOGI_IF(log_trace, "iwnn_wlfi_conf_read[dictype/diccnt][%d/%d]",dictype,diccnt);
                        diccnt++;

                        break;
                    case 4 : /* single-kanji dictionary */
                    case 5:  /* program dictionary (std giji) */
                    case 6:  /* program dictionary (querty giji) */
                    case 7:  /* program dictionary (predication giji) */
                    case 8:  /* decoemoji dictionary */
                    case 9:  /* program dictionary (ex_aip giji) */
                    case 10:  /* program dictionary (ex_cmpdg giji) */
                    case 11:  /* program dictionary (ex_nmc giji) */
                    case 12:  /* program dictionary (ex_nmf giji) */
                    case 13:  /* program dictionary (ex_nmsc giji) */
                    case WLF_DIC_TYPE_HRL_GIJI:  /* program dictionary (ex_hrl giji) */
                    default: /* etc. */

                        dicinfo = &(dicset->dic[diccnt]);

                        if (dictype == 5) {
                            dicinfo->type   = NJ_DIC_H_TYPE_PROGRAM;
                            dicinfo->handle = (NJ_VOID*)njex_giji_dic;
                            dicinfo->add_info[1] = (NJ_VOID*)&(wlf->time_info);
                        } else if (dictype == 6) {
                            dicinfo->type   = NJ_DIC_H_TYPE_PROGRAM;
                            dicinfo->handle = (NJ_VOID*)njex_qwerty_giji;
                        } else if (dictype == 7) {
                            dicinfo->type   = NJ_DIC_H_TYPE_PROGRAM;
                            dicinfo->handle = (NJ_VOID*)njex_pred_giji_dic;
                        } else if (dictype == 8) {
                            dicinfo->type   = NJ_DIC_H_TYPE_PROGRAM;
                            dicinfo->handle = (NJ_VOID*)requestEmojiDictionary;
                        } else if (dictype == 9) {
                            dicinfo->type   = NJ_DIC_H_TYPE_PROGRAM;
                            dicinfo->handle = (NJ_VOID*)njex_aip_giji_dic;
                        } else if (dictype == 10) {
                            dicinfo->type   = NJ_DIC_H_TYPE_PROGRAM;
                            dicinfo->handle = (NJ_VOID*)njex_cmpdg_giji_dic;
                        } else if (dictype == 11) {
                            dicinfo->type   = NJ_DIC_H_TYPE_PROGRAM;
                            dicinfo->handle = (NJ_VOID*)njex_nmcgiji_dic;
                        } else if (dictype == 12) {
                            dicinfo->type   = NJ_DIC_H_TYPE_PROGRAM;
                            dicinfo->handle = (NJ_VOID*)njex_nmfgiji_dic;
                        } else if (dictype == 13) {
                            dicinfo->type   = NJ_DIC_H_TYPE_PROGRAM;
                            dicinfo->handle = (NJ_VOID*)njex_nmscgiji_dic;
                        } else if (dictype == WLF_DIC_TYPE_HRL_GIJI) {
                            dicinfo->type   = NJ_DIC_H_TYPE_PROGRAM;
                            dicinfo->handle = (NJ_VOID*)njex_hrl_giji_dic;
                        } else {
                            dicinfo->type   = NJ_DIC_H_TYPE_NORMAL;
                        }

                        dicinfo->dic_freq[NJ_MODE_TYPE_HENKAN].base = q_base;
                        dicinfo->dic_freq[NJ_MODE_TYPE_HENKAN].high = q_high;
                        dicinfo->dic_freq[NJ_MODE_TYPE_YOSOKU].base = f_base;
                        dicinfo->dic_freq[NJ_MODE_TYPE_YOSOKU].high = f_high;
                        dicinfo->dic_freq[NJ_MODE_TYPE_MORPHO].base = m_base;
                        dicinfo->dic_freq[NJ_MODE_TYPE_MORPHO].high = m_high;
                        dicinfo->limit = stmoji;

                        memcpy(dicprop->name, fname, sizeof(dicprop->name));
                        memcpy(dicprop->ext_inputname, ext_inputfname, sizeof(dicprop->ext_inputname));
                        dicprop->type = dictype;

                        if (iwnn_dic_mount(wlf, langinfo, dicinfo, dicprop, cache_flg) < 0) {
                            LOGE_IF(log_trace, "iwnn_wlfi_conf_read[program dictionary parse NG][%d]",lineno);
                            errflg = 1;
                        }
                        if (dictype == 8) {
                            wlf->demoji_info.demojidic = (WLF_UINT8*)dicprop->fmap_ext_area.addr;
                        }
                        LOGI_IF(log_trace, "iwnn_wlfi_conf_read[dictype/diccnt][%d/%d]",dictype,diccnt);

                        diccnt++;
                        break;
                    }
                } else if (mode == PARSE_STATE_DIC_MODE) {
                    int i;
                    unsigned dicno;
                    unsigned dicmode[NJ_MAX_DIC];
                    int dicmode_cnt = 0;
                    memset(dicmode, 0, sizeof(dicmode));
                    buf[strlen(buf) - 1] = '\0';

                    sscanf(buf, "%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u%u", &dicno,
                        &dicmode[0], &dicmode[1], &dicmode[2], &dicmode[3], &dicmode[4],
                        &dicmode[5], &dicmode[6], &dicmode[7], &dicmode[8], &dicmode[9],
                        &dicmode[10], &dicmode[11], &dicmode[12], &dicmode[13], &dicmode[14],
                        &dicmode[15], &dicmode[16], &dicmode[17], &dicmode[18], &dicmode[19],
                        &dicmode[20], &dicmode[21], &dicmode[22], &dicmode[23], &dicmode[24],
                        &dicmode[25], &dicmode[26], &dicmode[27], &dicmode[28], &dicmode[29],
                        &dicmode[30], &dicmode[31], &dicmode[32], &dicmode[33], &dicmode[34]);

                    if (dicno >= NJ_MAX_DIC) {
                        LOGE_IF(log_trace, "iwnn_wlfi_conf_read() illegal[%d]",dicno);
                        errflg = 1;
                    } else {
                        for (i = 0; i < NJ_MAX_DIC; i++) { // include Additional dictionary
                            dicprop = &(langinfo->dicprops[i]);
                            dicprop->mode[dicno] = dicmode[i];
                            if (i >= IWNN_ADD_DIC_DICSET_TOPINDEX && dicmode_cnt > 1) {
                                dicprop->mode[dicno] = 1;
                            } else if (dicmode[i] != 0) {
                                dicmode_cnt++;
                            }
                        }
                    }
                } else if (mode == PARSE_STATE_DIC_CHARSET_SIZE) {
                    unsigned int charset_count_on = 0;
                    unsigned int charset_count_off = 0;
                    sscanf(buf, "%u%u" , &charset_count_on, &charset_count_off);
                    langinfo->charset.charset_count = (NJ_UINT16)charset_count_on;
                    langinfo->charset_off_count = (NJ_UINT16)charset_count_off;
                } else if (mode == PARSE_STATE_DIC_CHARSET_FROM) {
                    int from_str_buf[CHARSET_FROM_MAX] = {0};
                    NJ_CHAR *from_str = (NJ_CHAR *)malloc(CHARSET_FROM_MAX);
                    if (from_str == NULL) {
                        LOGE_IF(log_trace, "iwnn_wlfi_conf_read() from_str no memory[%d]",lineno);
                        errflg = 1;
                    } else {
                        sscanf(buf, "%x%x"
                            , &from_str_buf[0]
                            , &from_str_buf[1]
                          );
                        from_str[0] = (NJ_CHAR)from_str_buf[0];
                        from_str[1] = (NJ_CHAR)from_str_buf[1];
                        langinfo->charset.from[charsetidx] = from_str;
                        charsetidx++;
                    }
                } else if (mode == PARSE_STATE_DIC_CHARSET_TO) {
                    int to_str_buf[CHARSET_TO_MAX] = {0};
                    NJ_CHAR *to_str = (NJ_CHAR *)malloc(CHARSET_TO_MAX);
                    if (to_str == NULL) {
                        LOGE_IF(log_trace, "iwnn_wlfi_conf_read() to_str no memory[%d]",lineno);
                        errflg = 1;
                    } else {
                        sscanf(buf, "%x%x%x%x"
                            ,&to_str_buf[0]
                            ,&to_str_buf[1]
                            ,&to_str_buf[2]
                            ,&to_str_buf[3]
                            );
                        to_str[0] = (NJ_CHAR)to_str_buf[0];
                        to_str[1] = (NJ_CHAR)to_str_buf[1];
                        to_str[2] = (NJ_CHAR)to_str_buf[2];
                        to_str[3] = (NJ_CHAR)to_str_buf[3];
                        langinfo->charset.to[charsetidx] = to_str;
                        charsetidx++;
                    }
                }
                if (errflg) {
                    ret = -1;
                    LOGE_IF(log_trace, "iwnn_wlfi_conf_read[conf file parse NG]");
                    break;
                }
            }
            if (!errflg) {
                if(dicinfo_userdic != NULL) {
                    for (i = 0; i < IWNN_ADD_DIC_MAX; i++) {
                        dicprop = &(langinfo->dicprops[IWNN_ADD_DIC_DICSET_TOPINDEX + i]);
                        dicprop->type = WLF_DIC_TYPE_USER;
                        dicprop->mode[IWNN_ADD_DIC_DICSET_TOPINDEX + i] = 1;

                        dicinfo = &(dicset->dic[IWNN_ADD_DIC_DICSET_TOPINDEX + i]);

                        memcpy(dicinfo, dicinfo_userdic ,sizeof(NJ_DIC_INFO));
                        for (j = 0; j < NJ_MAX_EXT_AREA; j++) {
                            dicinfo->ext_area[j] = NULL;
                            }
                        dicinfo->srhCache = NULL;

                        if ((dicprop->fmap_handle.addr == NULL)
                                || (dicprop->fmap_handle.size == 0)) {
                            add_dic_lang = langinfo->lang;
                            if (langinfo->lang > WLF_LANG_TYPE_USUK) {
                               add_dic_lang = WLF_LANG_TYPE_JP;
                            }
                            snprintf(add_dic_fname, sizeof(add_dic_fname), IWNN_ADD_DIC_PATH, wlf->data_area_path, add_dic_lang, i);
                            if (stat(add_dic_fname, &add_dic_stat) == 0) {
                                if (fmap_open(&(dicprop->fmap_handle), add_dic_fname, O_CREAT|O_RDWR,
                                              S_IRUSR|S_IWUSR, NJ_USER_DIC_SIZE) < 0) {
                                    LOGE_IF(log_trace,
                                            "iwnn_wlfi_conf_read -- failed to open errno=%d\n", errno);
                                } else {
                                    dicinfo->srhCache = (NJ_SEARCH_CACHE *)malloc(sizeof(NJ_SEARCH_CACHE));
                                    if (dicinfo->srhCache == NULL) {
                                        LOGE_IF(log_trace, "iwnn_wlfi_conf_read() no memory");
                                        // "ret = -1" is unnecessary, because not fatal.
                                    }
                                }
                            }
                        }

                        dicinfo->handle = (NJ_DIC_HANDLE)dicprop->fmap_handle.addr;
                    }
                }

                if (dicinfo_learndic != NULL) {
                    iwnn_wlfi_conf_read_auto_learning_dictionary(wlf, langinfo, dicset,
                            dicinfo_learndic, &add_dic_stat);
                }
            }
        } else {
            ret = -1;
            LOGE_IF(log_trace, "iwnn_wlfi_conf_read[conf file format error]");
        }
    }
    fclose(fp);
    if (ret != -1) {
        ret = diccnt;
    }
    return ret;
}


/**
 * @brief Set pseudo candidate sets
 *
 * Description
 *  - Set a pseudo candidate set.
 *
 * @param[in,out]   *wlf  Framework class structure
 */
static void iwnn_wlfi_set_giji_table(WLF_CLASS *wlf)
{
    WLF_GIJI_INFO *giji_info;
    int lang = WLF_LANG_TYPE_JP;

    giji_info = &(wlf->giji_info);
    giji_info->gijipack = &WLF_GIJIPACK_NONE;

    lang = (wlf->lang == WLF_LANG_TYPE_JP) ? WLF_GIJI_JP : WLF_GIJI_OTHERS;

    if( (wlf->keytype != WLF_KEY_TYPE_KEYPAD12) &&
        (wlf->keytype != WLF_KEY_TYPE_FULL)){
         return;
     }

    giji_info->gijipack = WLF_GIJIPACK_TABLES[lang][wlf->keytype];

    LOGI_IF(log_trace, "iwnn_wlfi_set_giji_table(lang=%d,keytype=%d)=%p\n", lang, wlf->keytype, giji_info->gijipack);
}


/**
 * Get Language information
 *
 * @param  wlf   WLF_CLASS instance
 * @param  lang  Number of language
 *
 * @retval !NULL  Language info exist
 * @retval  NULL  No language info
 */
static WLF_LANG_INFO* iwnn_wlfi_find_langinfo(WLF_CLASS *wlf, NJ_UINT8 lang)
{
    WLF_UINT8 i;
    WLF_LANG_INFO *langinfo = NULL;

    if (lang == WLF_LANG_TYPE_NONE) {
        LOGE_IF(log_trace, "iwnn_wlfi_find_langinfo -- lang parameter err(WLF_LANG_TYPE_NONE) failed");
        return langinfo;
    }

    for (i = 0; i < WLF_LANG_TYPE_USE_MAX; i++) {
        if (wlf->lang_info[i].lang == lang) {
            langinfo = &(wlf->lang_info[i]);
            break;
        }
    }
    return langinfo;
}

/**
 * Get an available language info structure
 *
 * @param  wlf  WLF_CLASS instance
 *
 * @retval !NULL  Available structure
 * @retval  NULL  No available structure
 */
static WLF_LANG_INFO* iwnn_wlfi_find_free_langinfo(WLF_CLASS *wlf)
{
    WLF_UINT8 i;
    WLF_LANG_INFO *langinfo = NULL;

    for (i = 0; i < WLF_LANG_TYPE_USE_MAX; i++) {
        if (wlf->lang_info[i].lang == WLF_LANG_TYPE_NONE) {
            langinfo = &(wlf->lang_info[i]);
            break;
        }
    }
    return langinfo;
}

/**
 * @brief Get a prediction candidate
 *
 * Description
 *  - Judge if a flexible searching is enable or disable.
 *  - Judge if a relational searching is enable or disable.
 *  - Get a predicted candidate
 *
 * @param[in,out] *wlf    Framework class structure
 * @param[in]      s_cnt  Searching count
 *
 * @retval  >= 0    Number of candidates
 * @retval  <  0    Failure
 */
static NJ_INT16 iwnn_wlfi_get_prediction(WLF_CLASS *wlf, NJ_UINT16 s_cnt)
{
    NJ_INT16    ret = 0;
    NJ_INT16    cnt = 0;
    NJ_CHAR    *pstroke  = NULL;
    NJ_CHARSET *pcharset = NULL;
    NJ_UINT16   i;
    WLF_CAND   *pcand = &(wlf->cand_info);
    NJ_ANALYZE_OPTION ana_option;

    /* Set a flexible searching */
    if (wlf->charset.charset_count != 0) {
        pcharset = &(wlf->charset);
    }

    /* Set a reading string for searching */
    if (pcand->candidate_cnt == 0) {
        pstroke = wlf->input_stroke;
    }

    /* Set a relational searching condition */
    if (wlf->relation != 0) {
        wlf->predict_info.mode |= (NJ_RELATION_ON | NJ_YOMINASI_ON);
    } else {
        wlf->predict_info.mode &= ~NJ_RELATION_ON;
        wlf->predict_info.mode &= ~NJ_YOMINASI_ON;
    }

    /* Set a head conversion condition */
    if (wlf->headconv == WLF_HEAD_CONVERSION_OFF) {
        wlf->predict_info.mode &= ~NJ_HEAD_CONV_ON;
    } else {
        wlf->predict_info.mode |= NJ_HEAD_CONV_ON;
    }

    /* Set an option for a prediction */
    ana_option.mode                 = wlf->predict_info.mode;
    ana_option.forecast_learn_limit = wlf->predict_info.forecast_learn_limit;
    ana_option.forecast_limit       = wlf->predict_info.forecast_limit;
    ana_option.char_min             = wlf->predict_info.stroke_min;
    ana_option.char_max             = wlf->predict_info.stroke_max;
    ana_option.in_divide_pos        = NJ_DEFAULT_IN_DIVIDE_POS;
    ana_option.out_divide_pos       = NJ_DEFAULT_OUT_DIVIDE_POS;

    if (wlf->mode == WLF_MODE_EISUKANA) {
        /* Set a searching condition for EISU-KANA conversion candidate */
        pcharset = NULL;
        ana_option.mode = NJ_NO_LEARN | NJ_NO_YOSOKU | NJ_NO_RENBUN | NJ_NO_TANBUN | NJ_NO_ZEN;
        ana_option.forecast_learn_limit = 0;
        ana_option.forecast_limit = 0;
    }

    /* Start searching */
    for (i = 0; i < s_cnt; i++) {

        ret = njx_analyze( &(wlf->iwnn),
                           pcharset,
                           pstroke,
                           &(pcand->candidate[pcand->candidate_cnt]),
                           &ana_option);

        if (ret < 0) {
            /* Failure */
            char *err_msg;
            err_msg = iwnn_err_message(ret);
            LOGE_IF(log_trace, "njx_analyze()=%s\n", err_msg);
            if (err_msg) free(err_msg);
            return ret;
        } else if (ret == 0) {
            /* Complete getting candidates */
            break;
        } else {
        }
        cnt++;
        pcand->candidate_cnt++;
        pstroke = NULL;
    }
    LOGI_IF(log_trace, "iwnn_wlfi_get_prediction: cnt=%d,ret=%d\n", cnt, ret);
    return cnt;
}


/**
 * @brief Get a consecutive clause conversion
 *
 * Description
 *  - Convert a consecutive clause
 *
 * @param[in,out] *wlf  Framework class structure
 *
 * @retval  >  0  Number of clauses
 * @retval  <= 0  Failure
 */
static NJ_INT16 iwnn_wlfi_get_conv(WLF_CLASS *wlf)
{
    NJ_INT16 ret;

    /* Convert a consecutive clause */
    ret = njx_conv(&(wlf->iwnn),
                     wlf->input_stroke,
                     NJ_MAX_PHRASE,
                     wlf->conv_info.devide_pos,
                     wlf->cand_info.segment);
    if (ret > 0) {
        /* Success */
        wlf->cand_info.segment_cnt = ret;
    }

    if (ret < 0) {
        char *err_msg;
        err_msg = iwnn_err_message(ret);
        LOGE_IF(log_trace, "iwnn_wlfi_get_conv: njx_conv()=%s\n", err_msg);
        if (err_msg) free(err_msg);
    }

    return ret;
}

/**
 * @brief Set a pseudo candidate setting
 *
 * Description
 *  - Set a pseudo candidate type
 *
 * @param[in,out] *wlf   Framework class structure
 * @param[in]     type   Type of a pseudo candidate
 */
static void iwnn_wlfi_set_giji(WLF_CLASS *wlf, NJ_UINT8 type)
{
    WLF_LANG_INFO *langinfo;
    WLF_GIJI_INFO *giji_info;
    const WLF_GIJIPACK_INFO *gijipack;
    const NJ_GIJISET *gijiset;
    WLF_DIC_PROP *dicprop;
    NJ_DIC_SET *dst_dicset;
    NJ_DIC_SET *src_dicset;
    int i;
    int dicno = -1;

    langinfo = iwnn_wlfi_find_langinfo(wlf, wlf->lang);
    if (!langinfo) {
        return;
    }

    for (i = 0; i < count_standard_dictionaries(); i++) {
        dicprop = &(langinfo->dicprops[i]);
        if (dicprop->type == WLF_DIC_TYPE_GIJI) {
            dicno = i;
            break;
        }
    }

    if (dicno == -1) {
        return;
    }

    dst_dicset = &(wlf->iwnn.dic_set);
    src_dicset = &(wlf->dicset);
    memcpy(&(dst_dicset->dic[dicno]), &(src_dicset->dic[dicno]), sizeof(NJ_DIC_INFO));

    giji_info = &(wlf->giji_info);
    gijipack = giji_info->gijipack;
    gijiset = gijipack->module[type];
    if (gijiset->count) {
        dst_dicset->dic[dicno].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] = (NJ_GIJISET *)gijiset;
    }
    else {
        dst_dicset->dic[dicno].handle = NULL;
    }

    if (type != WLF_GIJISET_EISUKANA) {
        return;
    }

    dst_dicset->dic[dicno].dic_freq[NJ_MODE_TYPE_HENKAN].base = 0;
    dst_dicset->dic[dicno].dic_freq[NJ_MODE_TYPE_HENKAN].high = 10;
    dst_dicset->dic[dicno].dic_freq[NJ_MODE_TYPE_YOSOKU].base = 0;
    dst_dicset->dic[dicno].dic_freq[NJ_MODE_TYPE_YOSOKU].high = 10;
    dst_dicset->dic[dicno].dic_freq[NJ_MODE_TYPE_MORPHO].base = 10;
    dst_dicset->dic[dicno].dic_freq[NJ_MODE_TYPE_MORPHO].high = 0;

    dst_dicset->dic[dicno].limit = 0;
}

/**
 * @brief Get Homophones (get all candidates)
 *
 * Description
 *  - Get one candidate
 *  - Set NULL to a string and get all candidates
 *
 * @param[in,out] *wlf    Framework class structure
 * @param[in]     s_cnt   Searching count
 * @param[out]    *t_cnt  Number of all candidates
 *
 * @retval  >  0  Number of got candidates
 * @retval  <= 0  Failure
 */
static NJ_INT16 iwnn_wlfi_get_zenkouho(WLF_CLASS *wlf, NJ_UINT16 s_cnt, NJ_UINT16 *t_cnt)
{
    NJ_RESULT *presult = NULL;
    WLF_CAND  *pcand = &(wlf->cand_info);
    WLF_CONV  *pconv = &(wlf->conv_info);
    NJ_UINT16  cnt;
    NJ_UINT16  i;
    NJ_INT16   ret;

    /* Keep a number of candidate count before getting */
    cnt = pcand->candidate_cnt;

    if (pcand->candidate_cnt == 0) {
        /* First time */
        presult = &(pcand->segment[pconv->fix_seg_cnt]);
    }

    /* Get one candidate */
    ret = njx_zenkouho(&(wlf->iwnn),
                         presult,
                         pcand->candidate_cnt,
                       &(pcand->candidate[pcand->candidate_cnt]));
    if (ret <= 0) {
        if (ret < 0) {
            /* Failure */
            char *err_message;
            err_message = iwnn_err_message(ret);
            LOGE_IF(log_trace, "iwnn_wlfi_get_zenkouho: njx_zenkouho()=%s\n", err_message);
            if (err_message) free(err_message);
        }
        return ret;
    }

    /* Set a getting parameter */
    pcand->candidate_cnt++;
    *t_cnt = ret;
    presult = NULL;

    /* Get all candidates */
    for (i = 0; i < (s_cnt-1); i++) {

        ret = njx_zenkouho(&(wlf->iwnn),
                             presult,
                             pcand->candidate_cnt,
                           &(pcand->candidate[pcand->candidate_cnt]));
        if (ret <= 0) {
            if (ret < 0) {
                /* Failure */
                char *err_message;
                err_message = iwnn_err_message(ret);
                LOGE_IF(log_trace, "iwnn_wlfi_get_zenkouho: njx_zenkouho()=%s\n", err_message);
                if (err_message) free(err_message);
            }
            return ret;
        }
        pcand->candidate_cnt++;
    }

    /* Number of all candidates */
    return (pcand->candidate_cnt - cnt);
}

/**
 * Reset a pseudo candidate conversion
 *
 * @param  wlf  WLF_CLASS instance
 */
static void iwnn_wlfi_init_giji(WLF_CLASS *wlf)
{
    WLF_GIJI_INFO *giji_info;

    giji_info = &(wlf->giji_info);

    wlf->giji_mode = WLF_MODE_GIJI_OFF;
    giji_info->giji_cnt = 0;
    giji_info->type_cur = 0;
}

/**
 * Make a pseudo candidate
 *
 * @param  wlf   WLF_CLASS instance
 * @param  s_cnt Number of made candidates
 * @param  mode  Mode of conversion
 *
 * @retval  0< Number of result
 * @retval -1  Failure
 */
static int iwnn_wlfi_get_giji(WLF_CLASS *wlf, int s_cnt, int mode)
{
    WLF_CAND *cand_info;
    WLF_GIJI_INFO *giji_info;
    const WLF_GIJIPACK_INFO *gijipack;
    const NJ_GIJISET *gijiset;
    NJ_UINT16 candidate_cnt;
    NJ_INT16 result;
    int i, cnt = 0;
    const int giji_max_candidate = sizeof(giji_info->giji_candidate) / sizeof(NJ_RESULT);


    cand_info = &(wlf->cand_info);
    giji_info = &(wlf->giji_info);
    gijipack = giji_info->gijipack;
    gijiset = gijipack->fixed[mode];

    wlf->giji_mode = WLF_MODE_GIJI_ON;

    if (gijiset->count == 0) {
        return 0;
    }

    while (cnt < s_cnt) {

        if (giji_info->giji_stroke[0] == 0) {
            break;
        }

        if (giji_max_candidate <= giji_info->giji_cnt) {
            break;
        }

        if (gijiset->count <= giji_info->type_cur) {
            break;
        }

        /* Check filtering type */
        if (iwnn_check_giji_filter(gijiset->type[giji_info->type_cur], &(giji_info->giji_filter))) {
            giji_info->type_cur++;
            continue;
        }

        result = njex_get_giji(&(wlf->iwnn),
                               &(wlf->time_info),
                               giji_info->giji_stroke,
                               gijiset->type[giji_info->type_cur],
                               &(giji_info->giji_candidate[giji_info->giji_cnt]));

        LOGI_IF(log_trace, "njex_get_giji(type=%d,pos=%d)=%d\n", gijiset->type[giji_info->type_cur],
                                                                 giji_info->giji_cnt, result);

        if (result < 0) {
            char *err_msg;
            err_msg = iwnn_err_message(result);
            LOGE_IF(log_trace, "njex_get_giji(type=%d,pos=%d)=%s\n", gijiset->type[giji_info->type_cur],
                                                       giji_info->giji_cnt,
                                                       err_msg);
            free(err_msg);
            return -1;
        }
        else if (result == 0) {
            giji_info->type_cur++;
            continue;
        }

        cnt++;
        giji_info->giji_cnt++;
        giji_info->type_cur++;
    }

    LOGI_IF(log_trace, "iwnn_wlfi_get_giji()=%d\n", cnt);

    return cnt;
}

/**
 * @brief Learn a candidate
 *
 * Description
 *  - Learn a candidate, when a getting candidate type is "a candidate lists"
 *  - Learn a candidate, when a getting candidate type is "a clause"
 *
 * @param[in,out] *wlf  Framework class structure
 * @param[in]     gidx  Index for learning candidates
 * @param[in]     ridx  Index for connected candidates
 * @param[in]     type  Type of getting candidate(0:Candidate list, 1:Clauses)
 *
 * @retval  >= 0  Success
 * @retval  <  0  Failure
 */
static NJ_INT16 iwnn_wlfi_select(WLF_CLASS *wlf, NJ_INT16 gidx, NJ_INT16 ridx, NJ_UINT16 type)
{
    NJ_RESULT *gresult;
    NJ_RESULT *rresult;
    WLF_CAND  *pcand = &(wlf->cand_info);
    WLF_GIJI_INFO *giji_info = &(wlf->giji_info);
    NJ_INT16   ret;

    if(type == 0){
        if(gidx < 0){
            gresult = NULL;
        } else {
            if (gidx < pcand->candidate_cnt) {
                gresult = &pcand->candidate[gidx];
            }
            else {
                gidx -= pcand->candidate_cnt;
                gresult = &(giji_info->giji_candidate[gidx]);
            }
        }
        if(ridx < 0){
            rresult = NULL;
        } else {
            if (ridx < pcand->candidate_cnt) {
                rresult = &pcand->candidate[ridx];
            }
            else {
                ridx -= pcand->candidate_cnt;
                rresult = &(giji_info->giji_candidate[ridx]);
            }
        }
    } else {
        if(gidx < 0){
            gresult = NULL;
        } else {
            gresult = &pcand->segment[gidx];
        }
        if(ridx < 0){
            rresult = NULL;
        } else {
            rresult = &pcand->segment[ridx];
        }
    }
    ret = njx_select(&(wlf->iwnn), gresult, rresult, wlf->connect);
    LOGI_IF(log_trace, "njx_select(%p,%p,%p,0x%x)=%d\n", &(wlf->iwnn), gresult, rresult, wlf->connect, ret);
    return ret;
}

/**
 * Convert a character code from an input string to an internal code
 *
 * @param  wlf  WLF_CLASS instance
 * @param  in   Input string
 * @param  dst  Buffer for an internal character code
 * @param  size Size of the buffer
 *
 * @retval  >= 0 Output byte size
 * @retval   < 0 Failure
 *
 * @attension Must put NULL at the end of an input string.
 */
static WLF_INT32 iwnn_wlfi_convert_internal(WLF_CLASS *wlf, WLF_UINT8 *src, NJ_CHAR *dst, size_t size) {
    if (dst == NULL || size == 0) {
        return -1;
    }

    if (src == NULL) {
        dst[0] = NJ_CHAR_NUL;
        return 0;
    }
#ifdef NJ_OPT_UTF16
    UTF8_to_UTF16BE((unsigned char *)src, (unsigned char*)dst, size);
#else
    nj_code_convert(dst, (NJ_CC_UINT8*)src, size,
                    iwnn_conv_table_CP932, NJ_CC_UTF_8, NJ_CC_REV_CONVERT);
#endif
    return nj_strlen(dst);
}

/**
 * Convert a character code from an internal code to an output string
 *
 * @param  wlf  WLF_CLASS instance
 * @param  src  Internal character code
 * @param  dst  Buffer for an output string
 * @param  size Size of the buffer
 *
 * @retval  >= 0 Output byte size
 * @retval  < 0  Failure
 *
 * @attension Must put NULL at the end of an input string.
 */
static WLF_INT32 iwnn_wlfi_convert_external(WLF_CLASS *wlf, NJ_CHAR *src, NJ_UINT8 *dst, size_t size) {
    LOGI_IF(log_trace, "iwnn_wlfi_convert_external() start\n");
    if (src == NULL || dst == NULL || size == 0) {
        return -1;
    }

#ifdef NJ_OPT_UTF16
    /** convert from UTF-16BE to Modified UTF-8 */
    UTF16BE_to_ModUTF8((unsigned char*)src, (unsigned char*)dst, size);
#else
    nj_code_convert((NJ_CC_UINT8*)dst, src, size,
                    iwnn_conv_table_CP932, NJ_CC_UTF_8, NJ_CC_CONVERT);
#endif

    LOGI_IF(log_trace, "iwnn_wlfi_convert_external() end\n");
    return strlen((char*)dst);
}

/**
 * @brief Calculate a internal dividing position by a division position of clause.
 *
 * @param[in,out] *wlf        Framework class structure
 * @param[out]     devide_pos Dividing position of a clause
 *
 * @retval  >= 0 Output byte size
 * @retval  < 0  Failure
 *
 * @attension Must put NULL at the end of an input string.
 */
static WLF_UINT8 iwnn_wlfi_calc_devide_pos(WLF_CLASS *wlf, WLF_UINT8 devide_pos) {
    WLF_UINT8 i;
    WLF_UINT8 pos;

    if (devide_pos == 0) {
        /* Set 0 to a dividing position */
        return 0;
    }

    pos = 0;
    i = 0;
    while(wlf->input_stroke[i] != WLF_CHAR_NUL) {
        if (WLF_CHAR_LEN(&(wlf->input_stroke[i])) == 1) {
            i++;
            pos++;
        } else {
            i+=2;
            pos++;
        }
        if (pos == devide_pos) {
            /* Break When reached a dividing position of a clause */
            break;
        }
    }

    return i;
}

/** Range of EMOJI code */
#define WLF_EMOJI_HIGH_LOWER    0xDBB8
#define WLF_EMOJI_HIGH_HIGHER   0xDBBF
#define WLF_EMOJI_LOW_LOWER     0xDC00
#define WLF_EMOJI_LOW_HIGHER    0xDFFF

/* Range of email addresses */
#define WLF_EMAIL_ADDRESS_LOWER     0x0020
#define WLF_EMAIL_ADDRESS_HIGHER    0x007E

/** Range of a surrogate pair */
#define WLF_SURROGATE_HIGH_LOWER    0xD800
#define WLF_SURROGATE_HIGH_HIGHER   0xDBFF
#define WLF_SURROGATE_LOW_LOWER     0xDC00
#define WLF_SURROGATE_LOW_HIGHER    0xDFFF

/** Range of DECOEMOJI code */
#define WLF_DECOEMOJI_MARKER        0x001B

/** Convert from 2 byte data to UTF-16BE */
#define STR_TO_UINT16(s)    \
    (NJ_CHAR)(((*((NJ_UINT8*)(s))) << 8) | (*((NJ_UINT8*)(s)+1)))

/**
 * Set a filter for a searching dictionary
 *
 * @param  iwnn  iWnn_INFO instance
 * @param  message  Filter message of searching dictionary
 *
 * @retval  1  Include candidates
 * @retval  0  Exclude candidates
 */
static NJ_INT16 iwnn_phase1_filter(NJ_CLASS *iwnn, NJ_PHASE1_FILTER_MESSAGE *message)
{
    if ((iwnn == NULL) || (message == NULL)) {
        return 0;
    }

    if (message->string_len <= 0) {
        return 1;
    }

    if (iwnn_check_emoji(iwnn, message) && iwnn_check_aimai(iwnn, message)) {
        if (iwnn_check_decoemoji(iwnn, message)) {
            return 1;
        }
    }
    return 0;
}

/**
 * Check emoji character
 *
 * @param  iwnn  iWnn_INFO instance
 * @param  message  Filter message of searching dictionary
 *
 * @retval  1  Not filtering target
 * @retval  0  Filtering target
 */
static NJ_INT16 iwnn_check_emoji(NJ_CLASS *iwnn, NJ_PHASE1_FILTER_MESSAGE *message)
{
    WLF_CLASS *wlf;
    int size = 0;
    NJ_UINT8 *src;
    NJ_CHAR utf16, pare;
    
    if ((iwnn == NULL) || (message == NULL)) {
        return 1;
    }

    size = (int)message->string_len;
    src = (NJ_UINT8 *)message->string;
    wlf = (WLF_CLASS *)message->option;

    if (NJ_MAX_RESULT_LEN < size) {
        size = NJ_MAX_RESULT_LEN;
    }

    if (!wlf->filter) {
        return 1;
    }

    size = size * sizeof(NJ_CHAR);
    utf16 = STR_TO_UINT16(src);

    while (utf16 != 0) {

        if ((wlf->filter & WLF_EMOJI_FILTER) == WLF_EMOJI_FILTER) {
            if ((WLF_EMOJI_HIGH_LOWER <= utf16) && (utf16 <= WLF_EMOJI_HIGH_HIGHER)) {
                if ((size - 2) < 0) {
                    return 1;
                }
                pare = STR_TO_UINT16(src+2);
                if ((WLF_EMOJI_LOW_LOWER <= pare) && (pare <= WLF_EMOJI_LOW_HIGHER)) {
                    return 0;
                }
            }
        }

        if ((wlf->filter & WLF_EMAIL_ADDRESS_FILTER) == WLF_EMAIL_ADDRESS_FILTER) {
            if ((utf16 < WLF_EMAIL_ADDRESS_LOWER) || (WLF_EMAIL_ADDRESS_HIGHER < utf16)) {
                return 0;
            }
        }

        if ((WLF_SURROGATE_HIGH_LOWER <= utf16) && (utf16 <= WLF_SURROGATE_HIGH_HIGHER)) {
            if ((size - 2) < 0) {
                return 1;
            }
            pare = STR_TO_UINT16(src+2);
            if ((WLF_SURROGATE_LOW_LOWER <= pare) && (pare <= WLF_SURROGATE_LOW_HIGHER)) {
                size -= 4;
                if (size < 0) {
                    return 1;
                }
                src += 4;
                utf16 = STR_TO_UINT16(src);
                continue;
            }
        }

        size -= 2;
        if (size < 0) {
            return 1;
        }

        src += 2;
        utf16 = STR_TO_UINT16(src);

    }
    return 1;
}

/**
 * Check aimai character
 *
 * @param  iwnn  iWnn_INFO instance
 * @param  message  Filter message of searching dictionary
 *
 * @retval   1  Not filtering target
 * @retval   0  Filtering target
 */
static NJ_INT16 iwnn_check_aimai(NJ_CLASS *iwnn, NJ_PHASE1_FILTER_MESSAGE *message)
{
    WLF_CLASS *wlf;
    NJ_CHAR *temp;
    int message_stroke_len = 0;
    int input_len = 0;
    int loop_max = 0;
    int i = 0;
    int match_cnt = 0;
    NJ_UINT8 *stroke;
    NJ_UINT8 *input;
    NJ_CHAR utf16_stroke;
    NJ_CHAR utf16_input;

    if ((iwnn == NULL) || (message == NULL)) {
        return 1;
    }

    message_stroke_len = (int)message->stroke_len;
    stroke = (NJ_UINT8 *)message->stroke;
    wlf = (WLF_CLASS *)message->option;
    temp = wlf->input_stroke;
    input_len = (int)nj_strlen(temp);
    int stroke_len = (int)nj_strlen(message->stroke);
    input = (NJ_UINT8 *)temp;

    if (message->condition->charset == NULL) {
        return 1;
    }

    if (NJ_MAX_RESULT_LEN < message_stroke_len) {
        message_stroke_len = NJ_MAX_RESULT_LEN;
    }

    if (wlf->lang == WLF_LANG_TYPE_ZHCN) {
        return 1;
    }

    if (input_len <= 1) {
        return 1;
    }

    loop_max = stroke_len < input_len ? stroke_len : input_len;
    float num = (float)input_len / 2;

    for(; i < loop_max; i++) {
        utf16_input = STR_TO_UINT16(&input[i * 2]);
        utf16_stroke = STR_TO_UINT16(&stroke[i * 2]);
        
        if (utf16_input <= AIMAI_CONVERT_MAX) {
            if (utf16_input == utf16_stroke) {
                match_cnt++;
            } else {
                if (iwnn_is_contain_aimai_table(utf16_input, utf16_stroke)) {
                    match_cnt++;
                }
            }
        } else {
            return 1;
        }
    }

    if ((float)match_cnt >= num) {
        return 1;
    }
    return 0;
}

/**
 * Search aimai character
 *
 * @param  input  Input character
 * @param  stroke  Stroke character
 *
 * @retval  1  found
 * @retval  0  Not found
 */
static NJ_INT16 iwnn_is_contain_aimai_table(NJ_CHAR input, NJ_CHAR stroke)
{
    int start = 0;
    int end = AIMAI_MAX;
    int before_start = 0;
    int before_end = 0;
    int search_index = AIMAI_MAX / 2;
    int i = 0;

    for (;i < AIMAI_MAX; i++) {
        if (input == AIMAI_CHARACTER_TABLE[search_index][0]) {
            return iwnn_is_contain_aimai_table_second_phase(input, stroke, search_index);
        } else if (AIMAI_CHARACTER_TABLE[search_index][0] < input) {
            start = search_index;
            search_index = search_index + ((end - search_index) / 2);
        } else if (input < AIMAI_CHARACTER_TABLE[search_index][0]) {
            end = search_index;
            search_index = start + ((search_index - start) / 2);
        }

        if ((search_index < 0) || (AIMAI_MAX <= search_index)
                || ((start == before_start) && (end == before_end))) {
            return 0;
        }
        before_start = start;
        before_end = end;
    }
    return 0;
}

/**
 * Search aimai character around
 *
 * @param  input  Input character
 * @param  stroke  Stroke character
 * @param  index  Aimai table index
 *
 * @retval  1  found
 * @retval  0  Not found
 */
static NJ_INT16 iwnn_is_contain_aimai_table_second_phase(NJ_CHAR input, NJ_CHAR stroke, int index)
{
    int search_direction = 0;
    if (stroke > AIMAI_CHARACTER_TABLE[index][1]) {
        search_direction = 1;
    } else {
        search_direction = -1;
    }

    for (; (0 <= index) && (index < AIMAI_MAX); index += search_direction) {
        if ((AIMAI_CHARACTER_TABLE[index][0] != input)) {
            break;
        }

        if (AIMAI_CHARACTER_TABLE[index][1] == stroke) {
            return 1;
        }
    }
    return 0;
}

/**
 * Check decoemoji character
 *
 * @param  iwnn  iWnn_INFO instance
 * @param  message  Filter message of searching dictionary
 *
 * @retval  1  Not filtering target
 * @retval  0  Filtering target
 */
static NJ_INT16 iwnn_check_decoemoji(NJ_CLASS *iwnn, NJ_PHASE1_FILTER_MESSAGE *message)
{
    WLF_CLASS *wlf;
    NJ_UINT8 *src;
    NJ_CHAR utf16;
    int size = 0;

    if ((iwnn == NULL) || (message == NULL)) {
        return 1;
    }

    size = (int)message->string_len;
    wlf = (WLF_CLASS *)message->option;
    src = (NJ_UINT8 *)message->string;

    if (NJ_MAX_RESULT_LEN < size) {
        size = NJ_MAX_RESULT_LEN;
    }

    if (!wlf->filter) {
        return 1;
    }

    size = size * sizeof(NJ_CHAR);
    utf16 = STR_TO_UINT16(src);

    while (utf16 != 0) {
        if ((wlf->filter & WLF_DECOEMOJI_FILTER) == WLF_DECOEMOJI_FILTER) {
            if (utf16 == WLF_DECOEMOJI_MARKER) {
                return 0;
            }
        }

        size -= 2;
        if (size < 0) {
            return 1;
        }
        src += 2;
        utf16 = STR_TO_UINT16(src);
    }

    return 1;
}

/**
 * Do Morphological analysis
 *
 * @param[in]     env     JNI env
 * @param[in]     clazz   Class object
 * @param[in,out] iwnnp   iWnn_INFO instance
 * @param[in]     string  input string
 * @param[out]    result  Result [segments, process_len]
 */
void iwnn_split_word(JNIEnv *env, jclass clazz, jint iwnnp, jstring string, jintArray result)
{
    char process_len = 0;
    int  segment;
    char *str = (char *)0;
    WLF_CLASS *wlf;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    jint *presult;

    if (iwnn == NULL || iwnn->body == NULL) {
        return;
    }

    if (string) {
        str = (char *)(*env)->GetStringUTFChars(env, string, 0);
        if (!str) {
            LOGE_IF(log_trace, "iwnn_split_word: no str\n");
            return;
        }
    }

    wlf = (WLF_CLASS *)iwnn->body;
    segment = iwnn_wlfi_split_word(wlf, (WLF_UINT8*)str, (WLF_UINT8*)&process_len);

    if (str) (*env)->ReleaseStringUTFChars(env, string, (const char *)str);
    LOGI_IF(log_trace, "iwnn_split_word(%p,%p)=%d\n", iwnn, str, segment);

    presult = (*env)->GetIntArrayElements(env, result, NULL);
    if (presult) {
        presult[0] = segment;
        presult[1] = process_len;
        (*env)->ReleaseIntArrayElements(env, result, presult, 0);
    }
}

/**
 * @brief Do Morphological analysis
 *
 * @param[in,out] *wlf_class              Pointer for WLF_CLASS
 * @param[in]     *str                    input string
 * @param[out]    *process_len            position of finishing process.
 *
 * @retval  >= 0  Success
 * @retval  <  0  Failure
 */
static WLF_INT16 iwnn_wlfi_split_word(WLF_CLASS* wlf, WLF_UINT8* str, WLF_UINT8* process_len)
{
    WLF_CAND   *pcand;
    WLF_INT32  result;

    if (wlf == NULL) {
        /* Enough parameter */
        return WLF_ERR_INVALID_PARAM;
    }
    pcand = &(wlf->cand_info);
    result = iwnn_wlfi_convert_internal(wlf, (WLF_UINT8*)str, wlf->input_stroke, sizeof(wlf->input_stroke));
    if (result <= 0) {
        if (result < 0) LOGE_IF(log_trace, "iwnn_split_word: str=0x%x,result=%d\n", (unsigned int)str, (int)result);
        LOGI_IF(log_trace, "iwnn_split_word(%p,%p)=0 result=%d\n", wlf, str, (int)result);
        return 0;
    }

    return mmx_split_word(&(wlf->iwnn), wlf->input_stroke, process_len, &(pcand->candidate[0]));
}

/**
 * Set stem and yomi.
 *
 * @param[in]     env         JNI env
 * @param[out]    dist        Result
 * @param[in]     text        Text of stem + yomi
 * @param[in]     stem_len    Length of stem
 * @param[in]     iwnn        Info of iwnn
 */
static void iwnn_set_stem_and_yomi(JNIEnv *env, jobjectArray dist,
                                   NJ_CHAR *text, int stem_len, iWnn_INFO *iwnn) {
    NJ_CHAR   stem_last_char;
    WLF_INT16 length;
    WLF_CLASS *wlf;

    wlf = (WLF_CLASS *)iwnn->body;

    if (dist == NULL) {
        return;
    }
    (*env)->SetObjectArrayElement(env, dist, 0, NULL);
    (*env)->SetObjectArrayElement(env, dist, 1, NULL);

    if (text == NULL) {
        return;
    }

    if (0 < stem_len) {
        stem_last_char = text[stem_len];
        text[stem_len] = 0;

        length = iwnn_wlfi_convert_external(wlf, text, (WLF_UINT8 *)iwnn->strbuf, sizeof(iwnn->strbuf));
        if (0 < length) {
            (*env)->SetObjectArrayElement(env, dist, 0, 
                                          (*env)->NewStringUTF(env, iwnn->strbuf));
        }
        text[stem_len] = stem_last_char;
    }

    // Get fzk
    if (stem_len < nj_strlen(text)) {
        length = iwnn_wlfi_convert_external(wlf, &(text[stem_len]),
                                            (WLF_UINT8 *)iwnn->strbuf, sizeof(iwnn->strbuf));
        if (0 < length) {
            (*env)->SetObjectArrayElement(env, dist, 1, 
                                          (*env)->NewStringUTF(env, iwnn->strbuf));
        }
    }
}

/**
 * Get a word
 *
 * @param[in] env    JNI env
 * @param[in] clazz  Class object
 * @param[in] iwnnp  iWnn_INFO instance
 * @param[in] index  Index of a candidate
 * @param[out] word  Got word
 *
 * @retval !NULL  Words exist
 * @retval  NULL  No word
 */
void iwnn_get_morpheme_word(JNIEnv *env, jclass clazz, jint iwnnp, jint index, jobjectArray word)
{
    WLF_CLASS *wlf;
    NJ_CHAR    buf[MM_MAX_MORPHO_LEN+NJ_TERM_SIZE];
    NJ_INT16   result;
    iWnn_INFO *iwnn     = (iWnn_INFO*)iwnnp;
    NJ_RESULT *pcandidate;
    int        stem_len;

    if (iwnn == NULL || iwnn->body == NULL) {
        return;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    pcandidate = &(wlf->cand_info.candidate[index]);

    result = njx_get_candidate(&(wlf->iwnn), pcandidate, buf, sizeof(buf));
    if (result <= 0) {
        if (result < 0) LOGE_IF(log_trace, "iwnn_get_morpheme_word: result=%d\n", (int)result);
    } else {
        stem_len = MM_GET_STEM_LEN(pcandidate);
        iwnn_set_stem_and_yomi(env, word, buf, stem_len, iwnn);
    }
}

/**
 * Get parts of speech value.
 *
 * @param[in]     env     JNI env
 * @param[in]     clazz   Class object
 * @param[in,out] iwnnp   iWnn_INFO instance
 * @param[in]     index   Index of result
 *
 * @return parts of speech value
 */
jshort iwnn_get_morpheme_hinsi(JNIEnv *env, jclass clazz, jint iwnnp, jint index)
{
    WLF_CLASS *wlf;
    WLF_INT16 result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    if (iwnn == NULL || iwnn->body == NULL) {
        return 0;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    result = iwnn_wlfi_get_hinsi(wlf, index);
    if (result < 0) {
        LOGE_IF(log_trace, "iwnn_get_morpheme_hinsi: result=%d\n", (int)result);
        LOGI_IF(log_trace, "iwnn_get_morphmeme_hinsi(%p)=0 result=%d\n", iwnn, (int)result);
        return MM_HGROUP_OTHER;
    }

    LOGI_IF(log_trace, "iwnn_get_morpheme_hinsi(%p)=%d\n", iwnn, (int)result);
    return result;
}

/**
 * @brief Get parts of speech value.
 *
 * @param[in,out] *wlf_class  Pointer for WLF_CLASS
 * @param[in]     index       Index of result
 *
 * @retval  >= 0  Success
 * @retval  <  0  Failure
 */
static WLF_INT16 iwnn_wlfi_get_hinsi(WLF_CLASS* wlf, WLF_INT16 index)
{
    WLF_CAND   *pcand;

    if (wlf == NULL) {
        /* Enough parameter */
        return WLF_ERR_INVALID_PARAM;
    }

    pcand = &(wlf->cand_info);

    return mmx_get_hinsi(&(wlf->iwnn), &(pcand->candidate[index]));
}

/**
 * Get reading of yomi.
 *
 * @param[in,out] iwnn  iWnn_INFO instance
 * @param[in] clazz  Class object
 * @param[in] iwnnp  iWnn_INFO instance
 * @param[in] index  Index of a candidate
 * @param[out] word  Got yomi
 */
void iwnn_get_morpheme_yomi(JNIEnv *env, jclass clazz, jint iwnnp, jint index, jobjectArray yomi)
{
    WLF_CLASS *wlf;
    NJ_CHAR    buf[NJ_MAX_LEN+NJ_TERM_SIZE];
    WLF_INT16  result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    NJ_RESULT *pcandidate;
    WLF_UINT8  stem_len;

    if (iwnn == NULL || iwnn->body == NULL) {
        return;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    if (index < 0) {
        pcandidate = NULL;
    } else {
        pcandidate = &(wlf->cand_info.candidate[index]);
    }

    result = mmx_get_info(&(wlf->iwnn), pcandidate, buf, sizeof(buf), &stem_len, NULL);
    if (result <= 0) {
        if (result < 0) LOGE_IF(log_trace, "iwnn_get_morpheme_yomi: result=%d\n", (int)result);
        LOGI_IF(log_trace, "iwnn_get_morpheme_yomi(%p)=NULL result=%d\n",
                iwnn, (int)result);
    } else {
        iwnn_set_stem_and_yomi(env, yomi, buf, stem_len, iwnn);
    }
}

/**
 * Create an additional dictionary.
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in]     iwnnp iWnn_INFO instance
 * @param[in]     type  type of dictionary
 * @retval  0  Success
 * @retval -1  Failed
 */
jint iwnn_create_additional_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type)
{
    WLF_CLASS     *wlf;
    iWnn_INFO     *iwnn = (iWnn_INFO*)iwnnp;
    WLF_LANG_INFO *langinfo;
    fmap_t        *add_dic;
    char           add_dic_fname[WLF_FILE_PATH_LEN];
    int            add_dic_index;
    int            i;
    NJ_DIC_HANDLE  handle;

    LOGI_IF(log_trace, "iwnn_create_additional_dictionary(%d)\n", type);

    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    /* Cast to WLF_CLASS */
    wlf = (WLF_CLASS *)iwnn->body;

    langinfo = iwnn_wlfi_find_langinfo(wlf, wlf->lang);
    if (langinfo == NULL) {
        return -1;
    }

    add_dic_index = type - IWNN_ADD_DIC_DICSET_TOPINDEX;
    snprintf(add_dic_fname, sizeof(add_dic_fname), IWNN_ADD_DIC_PATH, wlf->data_area_path, wlf->lang, add_dic_index);
    mkdirs(add_dic_fname, S_IRWXU);
    add_dic = &(langinfo->dicprops[type].fmap_handle);
    if ((add_dic->addr == NULL) || (add_dic->size == 0)) {
        if (fmap_open(add_dic, add_dic_fname,
                      O_CREAT|O_RDWR, S_IRUSR|S_IWUSR, NJ_USER_DIC_SIZE) < 0) {
            LOGE_IF(log_trace, "iwnn_create_additional_dictionary -- failed to open errno=%d\n", errno);
            return -1;
        } else {
            if (langinfo->dicset.dic[type].srhCache == NULL) {
                langinfo->dicset.dic[type].srhCache = (NJ_SEARCH_CACHE *)malloc(sizeof(NJ_SEARCH_CACHE));
                if (langinfo->dicset.dic[type].srhCache == NULL) {
                    LOGE_IF(log_trace, "iwnn_create_additional_dictionary() no memory");
                    // "return -1" is unnecessary, because not fatal.
                }
            }
        }
    }

    if (iwnn_dic_init(&(wlf->iwnn), (NJ_DIC_HANDLE)add_dic->addr,
                      NULL, NJ_USER_DIC_SIZE, WLF_DIC_TYPE_USER, WLF_DIC_OPEN_CREAT, -1) < 0) {
        (void)fmap_close(add_dic);
        LOGE_IF(log_trace, "iwnn_create_additional_dictionary -- failed to init errno=%d\n", errno);
        return -1;
    }

    handle = (NJ_DIC_HANDLE)add_dic->addr;
    langinfo->dicset.dic[type].handle = handle;
    wlf->dicset.dic[type].handle = handle;
    wlf->dicset.dic[type].srhCache = langinfo->dicset.dic[type].srhCache;

    if (wlf->bookshelf < WLF_DICSET_TYPE_USER_DIC) {
        NJ_SET_ENV(wlf->iwnn, wlf->dicset);
    }
    else {
        wlf->search_info.dicset = wlf->dicset;
    }

    return 0;
}

/**
 * Delete an additional dictionary.
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in]     iwnnp iWnn_INFO instance
 * @param[in]     type  type of dictionary
 * @retval  0  Success
 * @retval -1  Failed
 */
jint iwnn_delete_additional_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type)
{
    WLF_CLASS     *wlf;
    iWnn_INFO     *iwnn     = (iWnn_INFO*)iwnnp;
    WLF_LANG_INFO *langinfo;
    fmap_t        *add_dic;
    char           add_dic_fname[WLF_FILE_PATH_LEN];
    int            add_dic_index;
    int            i;

    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    langinfo = iwnn_wlfi_find_langinfo(wlf, wlf->lang);
    if (langinfo == NULL) {
        return -1;
    }

    add_dic = &(langinfo->dicprops[type].fmap_handle);
    if ((add_dic->addr != NULL) && (add_dic->size != 0)) {

        langinfo->dicset.dic[type].handle = NULL;
        (void)fmap_close(add_dic);
        free(langinfo->dicset.dic[type].srhCache);
        langinfo->dicset.dic[type].srhCache = NULL;
        snprintf(add_dic_fname, sizeof(add_dic_fname), IWNN_ADD_DIC_PATH, wlf->data_area_path, wlf->lang, type - IWNN_ADD_DIC_DICSET_TOPINDEX);
        if (remove(add_dic_fname) != 0) {
            LOGE_IF(log_trace, "iwnn_delete_dictionary -- failed to remove errno=%d\n", errno);
        }
    }
    return 0;
}

/**
 * Delete an additional dictionary.
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in]     iwnnp iWnn_INFO instance
 * @param[in]     type  type of dictionary
 * @retval  0  Success
 * @retval -1  Failed
 */
jint iwnn_save_additional_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type)
{
    WLF_CLASS     *wlf;
    iWnn_INFO     *iwnn     = (iWnn_INFO*)iwnnp;
    WLF_LANG_INFO *langinfo;

    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    langinfo = iwnn_wlfi_find_langinfo(wlf, wlf->lang);
    if (langinfo == NULL) {
        return -1;
    }

    if ((type < IWNN_ADD_DIC_DICSET_TOPINDEX) || (NJ_MAX_DIC <= type)) {
        LOGE_IF(log_trace, "iwnn_save_additional_dictionary -- type err:%d\n", type);
        return -1;
    }

    if (fmap_sync(&(langinfo->dicprops[type].fmap_handle), MS_SYNC) < 0) {
        LOGE_IF(log_trace, "iwnn_save_additional_dictionary -- failed to sync\n");
        return -1;
    }
    return 0;
}

/**
 * Read conf file for auto learning dictionary.
 *
 * @param[in]  wlf               WLF_CLASS Instance
 * @param[in]  langinfo          Language information
 * @param[in]  dicset            Dictionary set
 * @param[in]  dicinfo_learndic   Dictionary set info of Learn Dictionary
 * @param[in]  al_dic_stat       Auto Learning Dictionary status
 */
void iwnn_wlfi_conf_read_auto_learning_dictionary(WLF_CLASS *wlf, WLF_LANG_INFO *langinfo, NJ_DIC_SET *dicset,
        NJ_DIC_INFO *dicinfo_learndic, struct stat *al_dic_stat)
{
    NJ_CLASS *iwnn;
    NJ_DIC_INFO *dicinfo;
    WLF_DIC_PROP *dicprop;
    char add_dic_fname[WLF_FILE_PATH_LEN];
    int i;

    iwnn = &(wlf->iwnn);
    dicprop = &(langinfo->dicprops[IWNN_AL_DIC_DICSET_TOPINDEX]);
    dicprop->type = WLF_DIC_TYPE_AUTOLEARN;
    dicprop->mode[IWNN_AL_DIC_DICSET_TOPINDEX] = 1;

    dicinfo = &(dicset->dic[IWNN_AL_DIC_DICSET_TOPINDEX]);

    memcpy(dicinfo, dicinfo_learndic ,sizeof(NJ_DIC_INFO));
    for (i = 0; i < NJ_MAX_EXT_AREA; i++) {
        dicinfo->ext_area[i] = NULL;
    }
    dicinfo->srhCache = NULL;

    if ((dicprop->fmap_handle.addr == NULL)
            || (dicprop->fmap_handle.size == 0)) {
        snprintf(add_dic_fname, sizeof(add_dic_fname), IWNN_AL_DIC_PATH, wlf->data_area_path, langinfo->lang, 0);
        if (stat(add_dic_fname, al_dic_stat) == 0) {
            if (fmap_open(&(dicprop->fmap_handle), add_dic_fname, O_CREAT|O_RDWR,
                          S_IRUSR|S_IWUSR, WLF_LEARN_DIC_SIZE) < 0) {
                LOGE_IF(log_trace,
                        "iwnn_wlfi_conf_read_auto_learning_dictionary -- failed to open errno=%d\n", errno);
            } else {
                dicinfo->srhCache = (NJ_SEARCH_CACHE *)malloc(sizeof(NJ_SEARCH_CACHE));
                if (dicinfo->srhCache == NULL) {
                    LOGE_IF(log_trace, "iwnn_wlfi_conf_read_auto_learning_dictionary() no memory");
                    // "ret = -1" is unnecessary, because not fatal.
                }
            }
        }
    }

    dicinfo->handle = (NJ_DIC_HANDLE)dicprop->fmap_handle.addr;
    memcpy(&(wlf->dicset.dic[IWNN_AL_DIC_DICSET_TOPINDEX]), dicinfo, sizeof(NJ_DIC_INFO));
    memcpy(&(iwnn->dic_set.dic[IWNN_AL_DIC_DICSET_TOPINDEX]), dicinfo, sizeof(NJ_DIC_INFO));
}

/**
 * Create an auto learning dictionary.
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in]     iwnnp iWnn_INFO instance
 * @param[in]     type  type of dictionary
 * @retval  0  Success
 * @retval -1  Failed
 */
jint iwnn_create_auto_learning_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type)
{
    WLF_CLASS     *wlf;
    iWnn_INFO     *iwnn = (iWnn_INFO*)iwnnp;
    WLF_LANG_INFO *langinfo;
    fmap_t        *al_dic;
    char           al_dic_fname[WLF_FILE_PATH_LEN];
    int            al_dic_index;
    int            i;
    NJ_DIC_HANDLE  handle;

    LOGI_IF(log_trace, "iwnn_create_auto_learning_dictionary(%d)\n", type);

    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    /* Cast to WLF_CLASS */
    wlf = (WLF_CLASS *)iwnn->body;

    langinfo = iwnn_wlfi_find_langinfo(wlf, wlf->lang);
    if (langinfo == NULL) {
        return -1;
    }

    al_dic_index = type - IWNN_AL_DIC_DICSET_TOPINDEX;
    snprintf(al_dic_fname, sizeof(al_dic_fname), IWNN_AL_DIC_PATH, wlf->data_area_path, wlf->lang, al_dic_index);
    mkdirs(al_dic_fname, S_IRWXU);
    al_dic = &(langinfo->dicprops[type].fmap_handle);
    if ((al_dic->addr == NULL) || (al_dic->size == 0)) {
        if (fmap_open(al_dic, al_dic_fname,
                      O_CREAT|O_RDWR, S_IRUSR|S_IWUSR, WLF_LEARN_DIC_SIZE) < 0) {
            LOGE_IF(log_trace, "iwnn_create_auto_learning_dictionary -- failed to open errno=%d\n", errno);
            return -1;
        } else {
            if (langinfo->dicset.dic[type].srhCache == NULL) {
                langinfo->dicset.dic[type].srhCache = (NJ_SEARCH_CACHE *)malloc(sizeof(NJ_SEARCH_CACHE));
                if (langinfo->dicset.dic[type].srhCache == NULL) {
                    LOGE_IF(log_trace, "iwnn_create_auto_learnin_dictionary() no memory");
                    // "return -1" is unnecessary, because not fatal.
                }
            }
        }
    }

    if (iwnn_dic_init(&(wlf->iwnn), (NJ_DIC_HANDLE)al_dic->addr,
                      NULL, WLF_LEARN_DIC_SIZE, WLF_DIC_TYPE_AUTOLEARN, WLF_DIC_OPEN_CREAT, -1) < 0) {
        (void)fmap_close(al_dic);
        LOGE_IF(log_trace, "iwnn_create_auto_learning_dictionary -- failed to init errno=%d\n", errno);
        return -1;
    }

    handle = (NJ_DIC_HANDLE)al_dic->addr;
    langinfo->dicset.dic[type].handle = handle;
    wlf->dicset.dic[type].handle = handle;
    wlf->dicset.dic[type].srhCache = langinfo->dicset.dic[type].srhCache;

    if (wlf->bookshelf < WLF_DICSET_TYPE_USER_DIC) {
        NJ_SET_ENV(wlf->iwnn, wlf->dicset);
    }
    else {
        wlf->search_info.dicset = wlf->dicset;
    }

    return 0;
}

/**
 * Delete an auto learning dictionary.
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in]     iwnnp iWnn_INFO instance
 * @param[in]     type  type of dictionary
 * @retval  0  Success
 * @retval -1  Failed
 */
jint iwnn_delete_auto_learning_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type)
{
    WLF_CLASS     *wlf;
    iWnn_INFO     *iwnn     = (iWnn_INFO*)iwnnp;
    WLF_LANG_INFO *langinfo;
    fmap_t        *al_dic;
    char           al_dic_fname[WLF_FILE_PATH_LEN];
    int            al_dic_index;
    int            i;

    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    langinfo = iwnn_wlfi_find_langinfo(wlf, wlf->lang);
    if (langinfo == NULL) {
        return -1;
    }

    al_dic = &(langinfo->dicprops[type].fmap_handle);
    if ((al_dic->addr != NULL) && (al_dic->size != 0)) {

        langinfo->dicset.dic[type].handle = NULL;
        (void)fmap_close(al_dic);
        free(langinfo->dicset.dic[type].srhCache);
        langinfo->dicset.dic[type].srhCache = NULL;
        snprintf(al_dic_fname, sizeof(al_dic_fname), IWNN_AL_DIC_PATH, wlf->data_area_path, wlf->lang, type - IWNN_AL_DIC_DICSET_TOPINDEX);
        if (remove(al_dic_fname) != 0) {
            LOGE_IF(log_trace, "iwnn_delete_dictionary -- failed to remove errno=%d\n", errno);
        }
    }

    // Setting handle pointer to iwnn.dic_set for receiving AutoLearning during IME showing
    wlf->iwnn.dic_set.dic[IWNN_AL_DIC_DICSET_TOPINDEX].handle
            = (NJ_DIC_HANDLE)langinfo->dicprops[IWNN_AL_DIC_DICSET_TOPINDEX].fmap_handle.addr;

    return 0;
}

/**
 * Save an auto learning dictionary.
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in]     iwnnp iWnn_INFO instance
 * @param[in]     type  type of dictionary
 * @retval  0  Success
 * @retval -1  Failed
 */
jint iwnn_save_auto_learning_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type)
{
    WLF_CLASS     *wlf;
    iWnn_INFO     *iwnn     = (iWnn_INFO*)iwnnp;
    WLF_LANG_INFO *langinfo;

    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    langinfo = iwnn_wlfi_find_langinfo(wlf, wlf->lang);
    if (langinfo == NULL) {
        return -1;
    }

    if ((type < IWNN_AL_DIC_DICSET_TOPINDEX) || (NJ_MAX_DIC <= type)) {
        LOGE_IF(log_trace, "iwnn_save_auto_learning_dictionary -- type err:%d\n", type);
        return -1;
    }

    if (fmap_sync(&(langinfo->dicprops[type].fmap_handle), MS_SYNC) < 0) {
        LOGE_IF(log_trace, "iwnn_save_auto_learning_dictionary -- failed to sync\n");
        return -1;
    }

    // Setting handle pointer to iwnn.dic_set for receiving AutoLearning during IME showing
    wlf->iwnn.dic_set.dic[IWNN_AL_DIC_DICSET_TOPINDEX].handle
            = (NJ_DIC_HANDLE)langinfo->dicprops[IWNN_AL_DIC_DICSET_TOPINDEX].fmap_handle.addr;

    return 0;
}

/**
 * Populate dl_dic_params that is temporary area of download dictionary properties from Java layer.
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in]     iwnnp iWnn_INFO instance
 * @param[in]     index index of download dictionary
 * @param[in]     name download dictionary name
 * @param[in]     file download dictionary file path
 * @param[in]     convert_high value of convert high
 * @param[in]     convert_base value of convert base
 * @param[in]     predict_high value of predict high
 * @param[in]     predict_base value of predict base
 * @param[in]     morpho_high value of morpho high
 * @param[in]     morpho_base value of morpho base
 * @param[in]     cache value of cache
 * @param[in]     limit value of limit
 */
void iwnn_set_download_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint index, jstring name,
        jstring file, jint convert_high, jint convert_base, jint predict_high, jint predict_base,
        jint morpho_high, jint morpho_base, jboolean cache, jint limit)
{ 
    WLF_CLASS *wlf;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    char *dl_dic_fname, *dl_dic_file;
    DL_DIC_PARAMS *dl_dic_params;

    if (iwnn == NULL || iwnn->body == NULL) {
        return;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    dl_dic_params = wlf->dl_dic_params;

    memset((void *)&dl_dic_params[index], 0, sizeof(dl_dic_params[index]));
    if (file != NULL) {

        dl_dic_fname = (char *)(*env)->GetStringUTFChars(env, name, 0);
        dl_dic_file = (char *)(*env)->GetStringUTFChars(env, file, 0);

        strncpy(dl_dic_params[index].name, dl_dic_fname, sizeof(dl_dic_params[index].name));
        dl_dic_params[index].name[sizeof(dl_dic_params[index].name) - 1] = '\0';
        strncpy(dl_dic_params[index].file, dl_dic_file, sizeof(dl_dic_params[index].file));
        dl_dic_params[index].file[sizeof(dl_dic_params[index].file) - 1] = '\0';
        dl_dic_params[index].convert_high = convert_high;
        dl_dic_params[index].convert_base = convert_base;
        dl_dic_params[index].predict_high = predict_high;
        dl_dic_params[index].predict_base = predict_base;
        dl_dic_params[index].morpho_high = morpho_high;
        dl_dic_params[index].morpho_base = morpho_base;
        dl_dic_params[index].cache = cache; 
        dl_dic_params[index].limit = limit;
    
        if (dl_dic_fname) (*env)->ReleaseStringUTFChars(env, name, (const char *)dl_dic_fname);
        if (dl_dic_file) (*env)->ReleaseStringUTFChars(env, file, (const char *)dl_dic_file);
    }

}

/**
 * Re-read conf file and refresh conf-settings.
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in]     iwnnp iWnn_INFO instance
 * @retval  0  Success
 * @retval -1  Failed
 */
jint iwnn_refresh_conf_file(JNIEnv *env, jclass clazz, jint iwnnp)
{
    WLF_CLASS     *wlf;
    iWnn_INFO     *iwnn     = (iWnn_INFO*)iwnnp;

    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    LOGI_IF(log_trace, "iwnn_refresh_conf_file()\n");

    iwnn_wlfi_close_download_dic(wlf);

    iwnn_wlfi_load_download_dic(wlf);
    return 0;
}

/**
 * Re-read conf file and refresh conf-settings.
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in]     iwnnp iWnn_INFO instance
 * @param[in]     package_name The service package name
 * @param[in]     password The service password
 * @retval  0  Success
 * @retval -1  Failed
 */
jint iwnn_set_service_package_name(JNIEnv *env, jclass clazz, jint iwnnp, jstring package_name, jstring password)
{ 
    WLF_CLASS     *wlf;
    iWnn_INFO     *iwnn     = (iWnn_INFO*)iwnnp;
    WLF_LANG_INFO *langinfo;
    char *package_chars= NULL;
    char *password_chars= NULL;
    int ret = -1;
    char converted_password[WLF_FILE_PATH_LEN];
    int i,j;

    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    LOGI_IF(log_trace, "iwnn_set_service_package_name()\n");

    wlf->password[0] = '\0';
    if (package_name) {
        package_chars = (char *)(*env)->GetStringUTFChars(env, package_name, 0);
        LOGI_IF(log_trace, "  package name = %s\n", package_chars);
        if (package_chars) {
            strncpy(wlf->package, package_chars, sizeof(wlf->package));
            wlf->package[sizeof(wlf->package) - 1] = '\0';
            (*env)->ReleaseStringUTFChars(env, package_name, package_chars);
            ret = 0;

            if (password) {
                password_chars = (char *)(*env)->GetStringUTFChars(env, password, 0);
                LOGI_IF(log_trace, "  password = %s\n", password_chars);
                if (password_chars) {
                    strncpy(wlf->password, password_chars, sizeof(wlf->password));
                    wlf->password[sizeof(wlf->password) - 1] = '\0';
                    (*env)->ReleaseStringUTFChars(env, password, password_chars);
                }
            }
        }
    } else {
        LOGI_IF(log_trace, "  package name = null\n");
        wlf->package[0] = '\0';
        ret = 0;
    }

    return ret;
}

/**
 * Copy download dictionary setting to wlf->dicset 
 * from dl_dic_params that is temporary area of download dictionary settings
 *
 * @param[in]  index     index of donwload dictionaries
 * @param[in]  wlf       WLF_CLASS instance
 * @param[in]  langinfo  Language information
 */
void iwnn_wlfi_set_download_dicset(int index, WLF_CLASS *wlf, WLF_LANG_INFO *langinfo) {
    WLF_DIC_PROP *dicprop;
    DL_DIC_PARAMS *dl_dic_params;

    dl_dic_params = wlf->dl_dic_params;

    dicprop = &(langinfo->dicprops[IWNN_DL_DIC_DICSET_TOPINDEX + index]);
    dicprop->type = WLF_DIC_TYPE_DOWNLOAD;
    dicprop->mode[IWNN_DL_DIC_DICSET_TOPINDEX + index] = 1;
    dicprop->fmap_handle = wlf->dl_dic_fmap[index];

    wlf->dicset.dic[IWNN_DL_DIC_DICSET_TOPINDEX + index].type = NJ_DIC_H_TYPE_NORMAL;
    wlf->dicset.dic[IWNN_DL_DIC_DICSET_TOPINDEX + index].dic_freq[NJ_MODE_TYPE_HENKAN].high
            = dl_dic_params[index].convert_high;
    wlf->dicset.dic[IWNN_DL_DIC_DICSET_TOPINDEX + index].dic_freq[NJ_MODE_TYPE_HENKAN].base
            = dl_dic_params[index].convert_base;
    wlf->dicset.dic[IWNN_DL_DIC_DICSET_TOPINDEX + index].dic_freq[NJ_MODE_TYPE_YOSOKU].high
            = dl_dic_params[index].predict_high;
    wlf->dicset.dic[IWNN_DL_DIC_DICSET_TOPINDEX + index].dic_freq[NJ_MODE_TYPE_YOSOKU].base
            = dl_dic_params[index].predict_base;
    wlf->dicset.dic[IWNN_DL_DIC_DICSET_TOPINDEX + index].dic_freq[NJ_MODE_TYPE_MORPHO].high
            = dl_dic_params[index].morpho_high;
    wlf->dicset.dic[IWNN_DL_DIC_DICSET_TOPINDEX + index].dic_freq[NJ_MODE_TYPE_MORPHO].base
            = dl_dic_params[index].morpho_base;
    wlf->dicset.dic[IWNN_DL_DIC_DICSET_TOPINDEX + index].limit = dl_dic_params[index].limit;

    size_t copy_size = sizeof(dicprop->name);
    size_t params_size = sizeof(dl_dic_params[index].name);
    if (copy_size > params_size) {
        copy_size = params_size;
    }
    memcpy(dicprop->name, dl_dic_params[index].name, copy_size);

    wlf->dicset.dic[IWNN_DL_DIC_DICSET_TOPINDEX + index].srhCache
            = wlf->dl_dic_srhcache[index];
    wlf->dicset.dic[IWNN_DL_DIC_DICSET_TOPINDEX + index].handle
            = (NJ_DIC_HANDLE)wlf->dl_dic_fmap[index].addr;
}

/**
 * Load download dictionary files.
 *
 * @param[in]  wlf       WLF_CLASS instance
 */
void iwnn_wlfi_load_download_dic(WLF_CLASS *wlf) {
    DL_DIC_PARAMS *dl_dic_params;
    int i;
    struct stat dl_dic_stat;
    char dl_dic_fname[WLF_FILE_PATH_LEN];

    /* open download dictionary file in order to fmap */
    dl_dic_params = wlf->dl_dic_params;
    for (i = 0; i < IWNN_DL_DIC_MAX; i++) {
        if ((wlf->dl_dic_fmap[i].addr == NULL) || (wlf->dl_dic_fmap[i].size == 0)) {
            if (stat(dl_dic_params[i].file, &dl_dic_stat) == 0) {
                if (fmap_open(&(wlf->dl_dic_fmap[i]), dl_dic_params[i].file, O_RDONLY,
                              0, (size_t)dl_dic_stat.st_size) < 0) {
                    LOGE_IF(log_trace, "iwnn_wlfi_conf_read -- failed to open errno=%d\n", errno);
                } else {
                    /* shift fmap pointer for header area of download dictionary */
                    wlf->dl_dic_fmap[i].addr += IWNN_DL_DIC_HEADER_SIZE;
                    wlf->dl_dic_fmap[i].size -= IWNN_DL_DIC_HEADER_SIZE;
                    if (dl_dic_params[i].cache) {
                        wlf->dl_dic_srhcache[i] = (NJ_SEARCH_CACHE *)malloc(sizeof(NJ_SEARCH_CACHE));
                        if (wlf->dl_dic_srhcache[i] == NULL) {
                            LOGE_IF(log_trace, "iwnn_wlfi_conf_read() no memory");
                            // "Ignore, because not fatal.
                        }
                    }
                }
            }
        }
    }
}

/**
 * Release download dictionaries.
 *
 * @param[in]  wlf       WLF_CLASS instance
 */
void iwnn_wlfi_close_download_dic(WLF_CLASS *wlf) {
    int i;
    int j;

    for (i = 0; i < IWNN_DL_DIC_MAX; i++) {
        if (wlf->dl_dic_fmap[i].addr != NULL) {

            for (j = 0; j < WLF_LANG_TYPE_USE_MAX; j++) {
                wlf->lang_info[j].dicset.dic[IWNN_DL_DIC_DICSET_TOPINDEX + i].handle = NULL;
            }

            /* shift fmap pointer for header area of download dictionary */
            wlf->dl_dic_fmap[i].addr -= IWNN_DL_DIC_HEADER_SIZE;
            wlf->dl_dic_fmap[i].size += IWNN_DL_DIC_HEADER_SIZE;

            /* release memory */
            (void)fmap_close(&(wlf->dl_dic_fmap[i]));
            free(wlf->dl_dic_srhcache[i]);
            wlf->dl_dic_srhcache[i] = NULL;
        }
    }
}

/**
 * Return count of all optional dictionaries. Optional dictionaries are blow.
 *  - Additional Dictionaries(10 dictionaries)
 *  - Autolearning Dictionaries(1 dicitonary)
 *  - Download Dictionaries(10 dicitonary)
 *
 * @return count of all optional dictionaries    
 */
int count_optional_dictionaries()
{
    return IWNN_ADD_DIC_DICSET_TOPINDEX + IWNN_ADD_DIC_MAX + IWNN_AL_DIC_MAX + IWNN_DL_DIC_MAX;
}

/**
 * Return count of all standard dictionaries. Standard dictionaries are all dictionaries exclude blow.
 *  - Additional Dictionaries(10 dictionaries)
 *  - Autolearning Dictionaries(1 dicitonary)
 *  - Download Dictionaries(10 dicitonary)
 *
 * @return count of all optional dictionaries    
 */
int count_standard_dictionaries()
{
    return NJ_MAX_DIC - IWNN_ADD_DIC_MAX - IWNN_AL_DIC_MAX - IWNN_DL_DIC_MAX;
}

/**
 * Return 1 if type is optional dictionary. Optional dictionaries are blow.
 *  - Additional Dictionaries
 *  - Autolearning Dictionaries
 *  - Download Dictionaries
 *
 * @param[in] type dictionary code
 * @return         count of all optional dictionaries    
 */
int is_optional_dictionary(int type)
{
    if((type == WLF_DIC_TYPE_USER) || (type == WLF_DIC_TYPE_AUTOLEARN) || (type == WLF_DIC_TYPE_DOWNLOAD)) {
        return 1;
    } else {
        return 0;
    }
}

#define BYTE_SWAP_BYTE1(x) (  x        & 0xFF )
#define BYTE_SWAP_BYTE2(x) ( (x >>  8) & 0xFF )
#define BYTE_SWAP_INT16(x) ( (DL_UINT16)(BYTE_SWAP_BYTE1(x)<<8 | BYTE_SWAP_BYTE2(x)) )
/**
 * Convert UTF-16BE to UTF-16LE
 *
 * @param[in] src           Source (UTF-16BE)
 * @param[in] dst           Destination (UTF-16LE)
 * @param[in] dst_size      Size of destination
 */
void UTF16BE_to_UTF16LE(DL_UINT16 *src, DL_UINT16 *dst, int dst_size)
{
    int i = 0;

    if ((src == NULL) || (dst == NULL)) {
        return;
    }

    for (i = 0; i < dst_size; i++ ) {
        dst[i] = BYTE_SWAP_INT16(src[i]);
    }
    return;
}

#define CONTROL_FLAG_ADD_DECOEMOJI    0  /* Regiter a DecoEmoji */
#define CONTROL_FLAG_DELETE_DECOEMOJI 2  /* Delete a decoemoji */
/**
 * Control a DecoEmojiDictionary
 *
 * @param[in,out] iwnn           iWnn_INFO instance
 * @param[in]     id             String (ID)
 * @param[in]     yomi           String (Reading)
 * @param[in]     hinsi          Number of Part
 * @param[in]     control_flag   Flag of Control
 */
void iwnn_control_decoemoji_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jstring id, jstring yomi, jint hinsi, jint control_flag)
{
    char *id_str = (char *)0;
    char *yomi_str = (char *)0;

    if ((control_flag != CONTROL_FLAG_ADD_DECOEMOJI) &&
        (control_flag != CONTROL_FLAG_DELETE_DECOEMOJI)) {
        return;
    }

    if (id) {
        id_str = (char *)(*env)->GetStringUTFChars(env, id, 0);
        if (!id_str) {
            return;
        }
    }

    if (yomi) {
        yomi_str = (char *)(*env)->GetStringUTFChars(env, yomi, 0);
        if (!yomi_str) {
            if (id_str) (*env)->ReleaseStringUTFChars(env, id, (const char *)id_str);
            return;
        }
    }

    iwnn_control_decoemoji_dictionary_i((iWnn_INFO *)iwnnp, id_str, yomi_str, hinsi, control_flag);

    if (id_str) (*env)->ReleaseStringUTFChars(env, id, (const char *)id_str);
    if (yomi_str) (*env)->ReleaseStringUTFChars(env, yomi, (const char *)yomi_str);

    return;
}

/**
 * Control a DecoEmojiDictionary
 *
 * @param[in,out] iwnn           iWnn_INFO instance
 * @param[in]     id             String (ID)
 * @param[in]     yomi           String (Reading)
 * @param[in]     hinsi          Number of Part
 * @param[in]     control_flag   Flag of Control
 */
void iwnn_control_decoemoji_dictionary_i(iWnn_INFO* iwnn, char* id, char* yomi, int hinsi, int control_flag)
{
    WLF_CLASS *wlf = NULL;
    DL_UINT16 add_id_src[DL_MAX_ID_LEN + 1] = {0};
    DL_UINT16 add_id_dst[DL_MAX_ID_LEN + 1] = {0};
    DL_UINT16 add_yomi_src[DL_MAX_YOMI_LEN + 1] = {0};
    DL_UINT16 add_yomi_dst[DL_MAX_YOMI_LEN + 1] = {0};
    DL_UINT16 *p_add_yomi_dst = add_yomi_dst;

    if (iwnn == NULL) {
        LOGE_IF(log_trace, "iwnn_control_decoemoji_dictionary_i() : param(iwnn) is not found.\n");
        return;
    }

    if (id == NULL) {
        LOGE_IF(log_trace, "iwnn_control_decoemoji_dictionary_i() : param(id) is not found.\n");
        return;
    }
    
    if ((control_flag != CONTROL_FLAG_ADD_DECOEMOJI) &&
        (control_flag != CONTROL_FLAG_DELETE_DECOEMOJI)) {
        return;
    }

    wlf = (WLF_CLASS *)iwnn->body;

#ifdef NJ_OPT_UTF16
    UTF8_to_UTF16BE((unsigned char *)id, (unsigned char *)add_id_src, (DL_MAX_ID_LEN * sizeof(DL_UINT16)));
    UTF16BE_to_UTF16LE(add_id_src, add_id_dst, DL_MAX_ID_LEN);

    if (yomi != NULL) {
        UTF8_to_UTF16BE((unsigned char *)yomi, (unsigned char *)add_yomi_src, (DL_MAX_YOMI_LEN * sizeof(DL_UINT16)));
        UTF16BE_to_UTF16LE(add_yomi_src, p_add_yomi_dst, DL_MAX_YOMI_LEN);
    }
    else {
        p_add_yomi_dst = NULL;
    }
#endif

    if (control_flag == CONTROL_FLAG_ADD_DECOEMOJI) {
        iwnn_add_decoemoji(wlf, add_id_dst, p_add_yomi_dst, hinsi);
    }
    else if (control_flag == CONTROL_FLAG_DELETE_DECOEMOJI) {
        iwnn_delete_decoemoji(wlf, add_id_dst, p_add_yomi_dst, hinsi);
    }

    return;
}

/**
 * Regiter a DecoEmoji
 *
 * @param[in,out] wlf      WLF_CLASS instance
 * @param[in]     id       String (ID)
 * @param[in]     yomi     String (Reading)
 * @param[in]     hinsi    Number of Part
 */
void iwnn_add_decoemoji(WLF_CLASS *wlf, DL_UINT16* id, DL_UINT16* yomi, int hinsi)
{
    DL_INT16 ret = 0;
    WLF_UINT8 lang_tmp = WLF_LANG_TYPE_JP;
    
    if (wlf == NULL) {
        return;
    }

    if (id == NULL) {
        return;
    }

    if (yomi == NULL) {
        return;
    }

    if ((hinsi < DL_PART_NONE) || (hinsi > DL_PART_CATEGORY_21)) {
        return;
    }

    if (wlf->lang != WLF_LANG_TYPE_JP) {
        lang_tmp = wlf->lang;
        ret = iwnn_wlf_set_lang((WLF_UINT8 *)wlf, WLF_LANG_TYPE_JP);
        if (ret != 0) {
            LOGE_IF(log_trace, "iwnn_add_decoemoji : iwnn_wlf_set_lang() Error = %d\n", (int)ret);
            return;
        }
    }

    ret = demoji_addToEmojiDictionary(&(wlf->demoji_info), id, yomi, (DL_UINT16)hinsi, DL_MM_OFF);
    if(ret != DL_ERROR_NONE) {
        LOGI_IF(log_trace, "iwnn_add_decoemoji : demoji_addToEmojiDictionary() Error = %d\n", (int)ret);
    }
    else {
        LOGI_IF(log_trace, "iwnn_add_decoemoji : demoji_addToEmojiDictionary() Success\n");
    }
    
    if (lang_tmp != WLF_LANG_TYPE_JP ) {
        ret = iwnn_wlf_set_lang((WLF_UINT8 *)wlf, lang_tmp);
        if (ret != 0) {
            LOGE_IF(log_trace, "iwnn_add_decoemoji : iwnn_wlf_set_lang() Error = %d\n", (int)ret);
        }
    }
    return;
}

/**
 * Delete a decoemoji
 *
 * @param[in,out] wlf      WLF_CLASS instance
 * @param[in]     id       String (ID)
 * @param[in]     yomi     String (Reading)
 * @param[in]     hinsi    Number of Part
 */
void iwnn_delete_decoemoji(WLF_CLASS *wlf, DL_UINT16* id, DL_UINT16* yomi, int hinsi)
{
    DL_INT16 ret = 0;
    WLF_UINT8 lang_tmp = 0;
    WLF_UINT8 filter_tmp = WLF_LANG_TYPE_JP;

    if (wlf == NULL) {
        return;
    }

    if (id == NULL) {
        return;
    }

    if (yomi == NULL) {
        hinsi = DL_PART_NONE;
    }

    if ((hinsi < DL_PART_NONE) || (hinsi > DL_PART_CATEGORY_21)) {
        return;
    }

    if (wlf->lang != WLF_LANG_TYPE_JP) {
        lang_tmp = wlf->lang;
        ret = iwnn_wlf_set_lang((WLF_UINT8 *)wlf, WLF_LANG_TYPE_JP);
        if (ret != 0) {
            LOGE_IF(log_trace, "iwnn_delete_decoemoji : iwnn_wlf_set_lang() Error = %d\n", (int)ret);
            return;
        }
    }
    
    filter_tmp = wlf->filter;
    wlf->filter &= ~WLF_DECOEMOJI_FILTER;

    ret = demoji_deleteFromEmojiDictionary(&(wlf->demoji_info), id, yomi, (DL_UINT16)hinsi);
    if (ret < 0) {
        LOGE_IF(log_trace, "iwnn_delete_decoemoji : demoji_deleteFromEmojiDictionary() Error = %d\n", (int)ret);
    }
    else {
        LOGI_IF(log_trace, "iwnn_delete_decoemoji : demoji_deleteFromEmojiDictionary() Success : ret = %d\n", ret);
    }
    
    wlf->filter = filter_tmp;
    
    if (lang_tmp != WLF_LANG_TYPE_JP ) {
        ret = iwnn_wlf_set_lang((WLF_UINT8 *)wlf, lang_tmp);
        if (ret != 0) {
            LOGE_IF(log_trace, "iwnn_delete_decoemoji : iwnn_wlf_set_lang() Error = %d\n", (int)ret);
        }
    }

    return;
}

/**
 * Check a DecoEmojiDictionary
 *
 * @param[in,out] iwnn           iWnn_INFO instance
 * @retval  0  Success
 * @retval -1  Failed
 */
jint iwnn_check_decoemoji_dictionary(JNIEnv *env, jclass clazz, jint iwnnp)
{
    iWnn_INFO *iwnn = NULL;
    WLF_CLASS *wlf = NULL;
    
    iwnn = (iWnn_INFO *)iwnnp;
    if (iwnn == NULL) {
        return -1;
    }
    
    wlf = (WLF_CLASS *)iwnn->body;
    if (wlf == NULL) {
        return -1;
    }
    else if ((wlf->demoji_info.iwnn == NULL) ||
             (wlf->demoji_info.demojidic == NULL)) {
        return -1;
    }
    else if (wlf->demoji_info.init_flg != DL_INIT_ON) {
        return -1;
    }

    return 0;
}

/**
 * Reset a DecoEmojiDictionary
 *
 * @param[in,out] iwnn           iWnn_INFO instance
 */
jint iwnn_reset_decoemoji_dictionary(JNIEnv *env, jclass clazz, jint iwnnp)
{
    iWnn_INFO *iwnn = NULL;
    WLF_CLASS *wlf = NULL;
    NJ_INT16 result = -1;
    
    iwnn = (iWnn_INFO *)iwnnp;
    if (iwnn == NULL) {
        LOGE_IF(log_trace, "iwnn_reset_decoemoji_dictionary() : param(iwnn) is not found.\n");
        return result;
    }
    
    wlf = (WLF_CLASS *)iwnn->body;
    if (wlf == NULL) {
        LOGE_IF(log_trace, "iwnn_reset_decoemoji_dictionary() : param(wlf) is not found.\n");
        return result;
    }
    else if (wlf->demoji_info.iwnn == NULL) {
        LOGE_IF(log_trace, "iwnn_reset_decoemoji_dictionary() : param(wlf->demoji_info.iwnn) is not found.\n");
        return result;
    }
    else if (wlf->demoji_info.demojidic == NULL) {
        LOGE_IF(log_trace, "iwnn_reset_decoemoji_dictionary() : param(wlf->demoji_info.demojidic) is not found.\n");
        return result;
    }

    // Regenerate dictionary
    result = demoji_create_dic(wlf->demoji_info.iwnn, wlf->demoji_info.demojidic);
    if (result < 0) {
        LOGE_IF(log_trace, "iwnn_reset_decoemoji_dictionary -- failed to demoji_create_dic() : result=%d\n", (int)result);
    }

    return result;
}

/**
 * Check a DecoEmojiDicset
 *
 * @param[in,out] iwnn           iWnn_INFO instance
 * @retval  0  Success
 * @retval -1  Failed
 */
jint iwnn_check_decoemoji_dicset(JNIEnv *env, jclass clazz, jint iwnnp)
{

    iWnn_INFO *iwnn = NULL;
    WLF_CLASS *wlf = NULL;
    NJ_INT16 result = -1;
    DL_DEMOJI_INFO* demojiInfo;
    NJ_CLASS* nj_class;
    int i = 0;

    iwnn = (iWnn_INFO *)iwnnp;
    if (iwnn == NULL) {
        LOGE_IF(log_trace, "iwnn_check_decoemoji_dicset() : param(iwnn) is not found.\n");
        return result;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    if (wlf == NULL) {
        LOGE_IF(log_trace, "iwnn_check_decoemoji_dicset() : param(wlf) is not found.\n");
        return result;
    }
    else if (wlf->demoji_info.iwnn == NULL) {
        LOGE_IF(log_trace, "iwnn_check_decoemoji_dicset() : param(wlf->demoji_info.iwnn) is not found.\n");
        return result;
    }
    else if (wlf->demoji_info.demojidic == NULL) {
        LOGE_IF(log_trace, "iwnn_check_decoemoji_dicset() : param(wlf->demoji_info.demojidic) is not found.\n");
        return result;
    }
    else if (wlf->demoji_info.init_flg != DL_INIT_ON) {
        LOGE_IF(log_trace, "iwnn_check_decoemoji_dicset() : wlf->demoji_info.init_flg is not init yet.\n");
        return result;
    }

    demojiInfo = &(wlf->demoji_info);
    nj_class = (NJ_CLASS *)demojiInfo->iwnn;
    for (i = 0; i < NJ_MAX_DIC; i++ ) {
        if (nj_class->dic_set.dic[i].handle == (NJ_VOID*)requestEmojiDictionary) {
            return 0;
        }
    }
    LOGI_IF(log_trace, "iwnn_check_decoemoji_dicset() : Error = %d\n", result);
    return result;
}

/**
 * To obtain the pseudo-potential given character types.
 *
 * @param[in]      env         JNI env
 * @param[in]      clazz       Class object
 * @param[in,out] iwnnp       iWnn_INFO instance
 * @param[in]      devide_pos Divide position of each clauses
 * @param[in]      type        By character type 
 *
 * @return  Number of clauses
 * Note the logic: iwnn_wlf_get_conversion()  case WLF_MODE_CONVERSION
 */
jint iwnn_getgijistr(JNIEnv *env, jclass clazz, jint iwnnp, jint devide_pos, jint type)
{
    WLF_CLASS      *wlf;
    WLF_GIJI_INFO  *giji_info;
    iWnn_INFO      *iwnn = (iWnn_INFO*)iwnnp;
    NJ_INT16       ret = 0;
    NJ_UINT8       gijiset_mode;
    NJ_CHAR        buf[NJ_MAX_LEN+NJ_TERM_SIZE];

    if (iwnn == NULL || iwnn->body == NULL) {
        LOGE_IF(log_trace, "iwnn_getgijistr(): null check err!!");
        return ret;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    giji_info = &(wlf->giji_info);

    memcpy(giji_info->giji_stroke, wlf->input_stroke, sizeof(giji_info->giji_stroke));
    if (0 < devide_pos) {
        giji_info->giji_stroke[devide_pos] = 0;
    }

    iwnn_wlfi_init_giji(wlf);

    /* Mode */
    /* Return error, if it's an unexpected mode */
    wlf->mode = WLF_MODE_CONVERSION;

    iwnn_wlf_set_hunhun(wlf, 0, -1);

    /* Initialize a candidate info */
    WLF_SET_INIT_CONVERT  (&wlf->conv_info);
    WLF_SET_INIT_CANDIDATE(&wlf->cand_info);

    gijiset_mode = iwnn_wlfi_get_gijiset_mode(wlf->mode);

    /* Set a pseudo candidate setting */
    iwnn_wlfi_set_giji(wlf, gijiset_mode);

    /* Set a dividing position of clauses */
    wlf->conv_info.devide_pos = iwnn_wlfi_calc_devide_pos(wlf, devide_pos);

    /* Get a pseudo candidate */
    ret = njex_get_giji(&(wlf->iwnn),
                        &(wlf->time_info),
                        wlf->input_stroke,
                        type,
                        wlf->cand_info.segment);
    if (ret <= 0) {
        /* Failure */
        LOGE_IF(log_trace, "iwnn_getgijistr(): njex_get_giji() ret err=%d\n", ret);
        return ret;
    }
    wlf->cand_info.segment_cnt = 1;  /* Success */

    /* Get a dividing position of clauses */
    ret = njx_get_stroke(&wlf->iwnn,
                         &wlf->cand_info.segment[0],
                         buf,
                         sizeof(buf));
    if (ret < 0) {
        /* Failure */
        LOGE_IF(log_trace, "iwnn_getgijistr(): njx_get_stroke() ret err=%d\n", ret);
        return ret;
    }
    wlf->conv_info.devide_pos = nj_strlen(buf);

    ret = wlf->cand_info.segment_cnt;
    return ret;
}

/**
 * Delete a search decoemoji word (LearnDictinary only)
 *
 * @param[in]      env   JNI env
 * @param[in]      clazz Class object
 * @param[in,out] iwnnp iWnn_INFO instance
 *
 * @retval  1 success (is delete words)
 * @retval  0 success (not delete words)
 * @retval -1 error
 */
jint iwnn_delete_learndic_decoemojiword(JNIEnv *env, jclass clazz, jint iwnnp) {

    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    WLF_CLASS * wlf;
    WLF_SEARCH_INFO * search_info;
    WLF_SEARCH_INFO* search_info_temp;
    NJ_VOID * filter_api_temp;
    NJ_VOID * option_temp;
    int  filter_temp;
    int  index = 0;
    int  result = 0;
    int  s_cnt, r_cnt;
    int  delete_result = 0;

    if (iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    // Evacuate the original data.
    search_info_temp = malloc(sizeof(WLF_SEARCH_INFO));
    memcpy(search_info_temp, &(wlf->search_info), sizeof(WLF_SEARCH_INFO));
    filter_temp = wlf->filter;
    filter_api_temp = wlf->option.phase1_filter;
    option_temp = wlf->option.phase1_option;

    // Because you do not want to filter the dictionary lookup, to temporarily disable the filtering function.
    // Such as editing user dictionary, and it takes the filter emoticons Deco, because you can not search.
    wlf->filter = 0;
    wlf->option.phase1_filter = NULL;
    wlf->option.phase1_option = NULL;

    // search word
    result = iwnn_search_word_decoemoji_learndic(env, clazz, iwnnp, NJ_CUR_OP_FORE_EXT, NJ_CUR_MODE_FREQ);
    if (result > 0) {
        // 0 is not an error, because neither be deleted, it is not necessary to further processing.
        // get word
        search_info = &(wlf->search_info);
        while(1) {
            s_cnt = (index + 1) - search_info->search_cnt;
            r_cnt = iwnn_wlf_get_word(wlf, s_cnt);
            if (r_cnt < 0) {
                break;
            }
            if (s_cnt != r_cnt) {
                break;
            }
            index++;
        }

        // delete word
        for (index = 0; index < search_info->search_cnt; index++) {
            delete_result = iwnn_delete_search_word(env, clazz, iwnnp, index);
            if (delete_result < 0) {
                result = delete_result;
            }
        }
    }
    // Back to the original data.
    memcpy(&(wlf->search_info), search_info_temp, sizeof(WLF_SEARCH_INFO));
    free(search_info_temp);
    wlf->filter = filter_temp;
    wlf->option.phase1_filter = filter_api_temp;
    wlf->option.phase1_option = option_temp;
    return result;
}

/**
 * Search a word (Decoemoji for learn dictionary)
 *
 * @param[in]      env   JNI env
 * @param[in]      clazz Class object
 * @param[in,out] iwnn    iWnn_INFO instance
 * @param[in]      method  Searching method
 * @param[in]      order   Searching order
 *
 * @retval  0  No data
 * @retval  1  Data exist
 * @retval <0  error
 */
int iwnn_search_word_decoemoji_learndic(JNIEnv *env, jclass clazz, jint iwnnp, jint method, jint order)
{
    WLF_CLASS * wlf;
    WLF_SEARCH_INFO *search_info;
    NJ_CURSOR * cursor;
    NJ_INT16    result;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    WLF_LANG_INFO *langinfo;
    int cnt;

    if (iwnn == NULL || iwnn->body == NULL) {
        return WLF_ERR_INVALID_PARAM;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    // Input Emoji, Japanese only.
    langinfo = iwnn_wlfi_find_langinfo(wlf, WLF_LANG_TYPE_JP);
    if (langinfo == NULL) {
        return -1;
    }

    search_info = &(wlf->search_info);
    search_info->iwnn = wlf->iwnn;
    search_info->iwnn.dic_set = langinfo->dicset;
    for (cnt = 0; cnt < NJ_MAX_DIC; cnt++) { // include LearnDictinary
        if (langinfo->dicprops[cnt].type == WLF_DIC_TYPE_LEARN) {
            search_info->iwnn.dic_set.dic[cnt].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] = search_info->iwnn.dic_set.dic[cnt].handle;
            search_info->iwnn.dic_set.dic[cnt].ext_area[NJ_TYPE_EXT_AREA_INPUT] = search_info->iwnn.dic_set.dic[cnt].handle;
        } else {
            search_info->iwnn.dic_set.dic[cnt].handle = NULL;
        }
    }
    search_info->dicset = search_info->iwnn.dic_set;

    cursor = &(search_info->cursor);

    search_info->search_cnt = 0;

    memset(&(search_info->cursor), 0, sizeof(search_info->cursor));
    cursor->cond.operation = (NJ_UINT8)method;
    cursor->cond.mode = (NJ_UINT8)order;
    cursor->cond.ds = &(search_info->dicset);
    cursor->cond.yomi = wlf->input_stroke;
    cursor->cond.kanji = NULL;
    cursor->cond.charset = NULL;

    result = njx_search_word(&(search_info->iwnn), cursor);

    if (result < 0) {
        char *err_msg;
        err_msg = iwnn_err_message(result);
        LOGE_IF(log_trace, "%s(%d,%d)=%s\n", __func__, method, order, err_msg);
        if (err_msg) free(err_msg);
        return -1;
    }

    return (int)result;
}

/**
 * Set a pseudo dictionary filter
 *
 * @param[in]     env   JNI       env
 * @param[in]     clazz Class     object
 * @param[in,out] iwnnp iWnn_INFO instance
 * @param[in]     type  jintArray filtering type
 *
 * @retval  1 success (set filtering type)
 * @retval  0 success (initialize filtering type)
 * @retval -1 error
 */
jint iwnn_set_giji_filter(JNIEnv *env, jclass clazz, jint iwnnp, jintArray type)
{
    WLF_CLASS *wlf;
    WLF_GIJI_INFO *giji_info;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;

    jint * tmp_type;
    int length = 0;
    int cnt = 0;
    int ret = 0;

    if (type == NULL || iwnn == NULL || iwnn->body == NULL) {
        return -1;
    }

    wlf = (WLF_CLASS *)iwnn->body;
    giji_info = &(wlf->giji_info);

    /* Initialize filtering type */
    giji_info->giji_filter.count = 0;
    memset(&(giji_info->giji_filter.type), 0, sizeof(giji_info->giji_filter.type));

    tmp_type = (*env)->GetIntArrayElements(env, type, NULL);
    if (tmp_type) {
        length = (*env)->GetArrayLength(env, type);
        if (length > 0 && length <= NJ_GIJISET_MAX) {
            if (*tmp_type != WLF_GIJI_FILTER_NONE) {
                /* Set filtering type */
                giji_info->giji_filter.count = length;
                for (cnt = 0; cnt < length; cnt++) {
                    *(giji_info->giji_filter.type + cnt)= *(tmp_type + cnt);
                }
                ret = 1;
            }
        }
        (*env)->ReleaseIntArrayElements(env, type, tmp_type, JNI_ABORT);
    }

    return ret;
}

/**
 * Check a pseudo dictionary filter
 *
 * @param type        filtering target
 * @param giji_filter filtering type
 *
 * @retval  1 Is Filtering target
 * @retval  0 Not filtering target
 */
int iwnn_check_giji_filter(int type, NJ_GIJISET* giji_filter) {

    int ret = 0;
    int cnt = 0;

    if (giji_filter == NULL) {
        return ret;
    }

    if (giji_filter->count <= 0) {
        return ret;
    }

    for (cnt = 0; cnt < giji_filter->count; cnt++) {
        if (giji_filter->type[cnt] == type) {
            ret = 1;
            break;
        }
    }

    return ret;
}

/**
 * Delete dictionary file
 *
 * @param[in,out] iwnn   iWnn_INFO instance
 * @param[in]      clazz  Class object
 * @param[in]      file   delete file path
 *
 * @retval  1 success
 * @retval -1 error
 */
jint iwnn_delete_dictionary_file(JNIEnv *env, jclass clazz, jstring file) {
    WLF_CLASS *wlf;
    char      *str;
    int       result = 0;

    if (file == NULL) {
        return -1;
    }

    str = (char *)(*env)->GetStringUTFChars(env, file, 0);
    if (str == NULL) {
        return -1;
    }

    result = remove(str);
    (*env)->ReleaseStringUTFChars(env, file, (const char *)str);
    if (result != 0) {
        LOGE_IF(log_trace, "iwnn_delete_dictionary_file delete failed '%s'", str);
        return -1;
    }

    return 1;
}

/**
 * Get the NJ_WORD_INFO.stem.attr
 *
 * @param[in]     env   JNI env
 * @param[in]     clazz Class object
 * @param[in]     iwnnp iWnn_INFO instance
 * @param[in]     index Index of a candidate
 *
 * @retval  NJ_WORD_INFO.stem.attr
 */
jint iwnn_get_word_info_stem_attribute(JNIEnv *env, jclass clazz, jint iwnnp, jint index)
{
    WLF_CLASS *wlf;
    WLF_SEARCH_INFO *search_info;
    NJ_INT16 ret;
    iWnn_INFO *iwnn = (iWnn_INFO*)iwnnp;
    NJ_WORD_INFO word_info;
    WLF_CAND* cand_info;

    if ((iwnn == NULL) || (iwnn->body == NULL)) {
        return 0;
    }

    wlf = (WLF_CLASS *)iwnn->body;

    search_info = &(wlf->search_info);
    cand_info = &(wlf->cand_info);

    if (cand_info->candidate_cnt <= index) {
        return 0;
    } else {
        ret = njx_get_word_info(&(search_info->iwnn), &(cand_info->candidate[index]), &word_info);

        if (ret < 0) {
            return 0;
        }
    }

    return word_info.stem.attr;
}
