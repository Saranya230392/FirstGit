/**
 * @file
 *   iWnn Interface for SWIG definition file.
 *
 * @author
 *   OMRON SOFTWARE Co., Ltd.
 *
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 * (C) Copyright OMRON SOFTWARE Co., Ltd. 2009 All Rights Reserved
 */
#ifndef __IWNN_H__
#define __IWNN_H__

/***********************************************************************
 * definitions
 ***********************************************************************/
/**
 * iWnn data structure
 */
typedef struct {
    /** string buffer */
    char     strbuf[1024];
    /** body of iWnn data */
    void    *body;
} iWnn_INFO;

/***********************************************************************
 * APIs
 ***********************************************************************/
extern jint    iwnn_get_info(JNIEnv *env, jclass clazz);
extern jint    iwnn_set_active_lang(JNIEnv *env, jclass clazz, jint iwnnp, jint no);
extern jint    iwnn_set_bookshelf(JNIEnv *env, jclass clazz, jint iwnnp, jint bookshelf);
extern jint    iwnn_unmount_dics(JNIEnv *env, jclass clazz, jint iwnnp);
extern void    iwnn_destroy(JNIEnv *env, jclass clazz, jint iwnnp);
extern jint    iwnn_set_input(JNIEnv *env, jclass clazz, jint iwnnp, jstring string);
extern jstring iwnn_get_input(JNIEnv *env, jclass clazz, jint iwnnp);
extern jint    iwnn_setdic_by_conf(JNIEnv *env, jclass clazz, jint iwnnp, jstring file_name, jint lang);
extern jint    iwnn_init(JNIEnv *env, jclass clazz, jint iwnnp, jstring dir_path);
extern jint    iwnn_get_state(JNIEnv *env, jclass clazz, jint iwnnp);
extern jint    iwnn_set_state(JNIEnv *env, jclass clazz, jint iwnnp);
extern void    iwnn_set_state_system(JNIEnv *env, jclass clazz, jint iwnnp, jint situation, jint bias);
extern jint    iwnn_forecast(JNIEnv *env, jclass clazz, jint iwnnp, jint minLen, jint maxLen, jint headConv);
extern jint    iwnn_select(JNIEnv *env, jclass clazz, jint iwnnp, jint segment, jint cand, jint mode);
extern jint    iwnn_search_word(JNIEnv *env, jclass clazz, jint iwnnp, jint method, jint order);
extern jstring iwnn_get_word(JNIEnv *env, jclass clazz, jint iwnnp, jint index, jint type);
extern jint    iwnn_add_word(JNIEnv *env, jclass clazz, jint iwnnp, jstring yomi, jstring repr, jint group, jint dtype, jint con);
extern jint    iwnn_delete_word(JNIEnv *env, jclass clazz, jint iwnnp, jint index);
extern jint    iwnn_delete_search_word(JNIEnv *env, jclass clazz, jint iwnnp, jint index);
extern jint    iwnn_conv(JNIEnv *env, jclass clazz, jint iwnnp, jint devide_pos);
extern jint    iwnn_noconv(JNIEnv *env, jclass clazz, jint iwnnp);
extern jstring iwnn_get_word_stroke(JNIEnv *env, jclass clazz, jint iwnnp, jint segment, jint cand);
extern jstring iwnn_get_word_string(JNIEnv *env, jclass clazz, jint iwnnp, jint segment, jint cand);
extern jstring iwnn_get_segment_stroke(JNIEnv *env, jclass clazz, jint iwnnp, jint segment);
extern jstring iwnn_get_segment_string(JNIEnv *env, jclass clazz, jint iwnnp, jint segment);
extern jint    iwnn_delete_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type, jint language, jint dictionary);
extern jint    iwnn_reset_extended_info(JNIEnv *env, jclass clazz, jstring fileName);
extern jint    iwnn_set_flexible_charset(JNIEnv *env, jclass clazz, jint iwnnp, jint flexible_charset, jint keytype);
extern jint    iwnn_write_out_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type);
extern jint    iwnn_sync_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type);
extern void    iwnn_sync(JNIEnv *env, jclass clazz, jint iwnnp);
extern jint    iwnn_is_learn_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint index);
extern jint    iwnn_is_giji_result(JNIEnv *env, jclass clazz, jint iwnnp, jint index);
extern jint    iwnn_undo(JNIEnv *env, jclass clazz, jint iwnnp, jint count);
extern void    iwnn_emoji_filter(JNIEnv *env, jclass clazz, jint iwnnp, jint enabled);
extern void    iwnn_email_address_filter(JNIEnv *env, jclass clazz, jint iwnnp, jint enabled);
extern void    iwnn_split_word(JNIEnv *env, jclass clazz, jint iwnnp, jstring string, jintArray result);
extern void    iwnn_get_morpheme_word(JNIEnv *env, jclass clazz, jint iwnnp, jint index, jobjectArray word);
extern jshort  iwnn_get_morpheme_hinsi(JNIEnv *env, jclass clazz, jint iwnnp, jint index);
extern void    iwnn_get_morpheme_yomi(JNIEnv *env, jclass clazz, jint iwnnp, jint index, jobjectArray yomi);
extern jint iwnn_get_morpheme_stemlen(JNIEnv *env, jclass clazz, jint iwnnp, jint index);
extern jint    iwnn_create_additional_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type);
extern jint    iwnn_delete_additional_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type);
extern jint    iwnn_save_additional_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type);
extern jint    iwnn_create_auto_learning_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type);
extern jint    iwnn_delete_auto_learning_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type);
extern jint    iwnn_save_auto_learning_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint type);
extern void    iwnn_set_download_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jint index, jstring name,
        jstring file, jint convert_high, jint convert_base, jint predict_high, jint predict_base,
        jint morpho_high, jint morpho_base, jboolean cache, jint limit);
extern jint    iwnn_set_service_package_name(JNIEnv *env, jclass clazz, jint iwnnp, jstring package_name, jstring password);
extern jint    iwnn_refresh_conf_file(JNIEnv *env, jclass clazz, jint iwnnp);

extern void iwnn_decoemoji_filter(JNIEnv *env, jclass clazz, jint iwnnp, jint enabled);
extern void iwnn_control_decoemoji_dictionary(JNIEnv *env, jclass clazz, jint iwnnp, jstring id, jstring yomi, jint hinsi, jint control_flag);
extern jint iwnn_check_decoemoji_dictionary(JNIEnv *env, jclass clazz, jint iwnnp);
extern jint iwnn_reset_decoemoji_dictionary(JNIEnv *env, jclass clazz, jint iwnnp);
extern jint iwnn_check_decoemoji_dicset(JNIEnv *env, jclass clazz, jint iwnnp);
extern jint    iwnn_getgijistr(JNIEnv *env, jclass clazz, jint iwnnp, jint devide_pos, jint type);
extern jint iwnn_delete_learndic_decoemojiword(JNIEnv *env, jclass clazz, jint iwnnp);
extern jint iwnn_set_giji_filter(JNIEnv *env, jclass clazz, jint iwnnp, jintArray type);
extern jint iwnn_delete_dictionary_file(JNIEnv *env, jclass clazz, jstring file);
extern jint iwnn_get_word_info_stem_attribute(JNIEnv *env, jclass clazz, jint iwnnp, jint index);
#endif /*__IWNN_H_*/
