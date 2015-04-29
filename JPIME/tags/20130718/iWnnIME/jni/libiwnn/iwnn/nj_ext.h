/**
 * @file
 *   iWnn内部用共通定義
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2012 All Rights Reserved.
 * 
 * @attention
 *   nj_lib.hの後にインクルードしてください。
 */
#ifndef _NJ_EXTERN_H_
#define _NJ_EXTERN_H_

/************************************************/
/*          iWnn内部用共通定義                  */
/************************************************/

#ifdef NJ_OPT_DIC_STORAGE
/*
 * ファイルポインタ位置の定義
 * ※システムにあわせて変更を行ってください。
 */
#define NJ_FILE_IO_SEEK_CUR             SEEK_CUR      /**< ファイルポインタ位置：現在位置 */
#define NJ_FILE_IO_SEEK_END             SEEK_END      /**< ファイルポインタ位置：終端 */
#define NJ_FILE_IO_SEEK_SET             SEEK_SET      /**< ファイルポインタ位置：先頭 */
#endif /* NJ_OPT_DIC_STORAGE */



#define NJ_MIN_FREQ_WITH_UNDER_BIAS (100)
#define NJ_MAX_FREQ_WITH_UNDER_BIAS (400)

/*
 * 文字コード依存処理吸収マクロ
 */
/**
 * 1文字の最大文字配列長
 *
 * @note (最大バイト長/sizeof(NJ_CHAR))の値
 */
#define NJ_MAX_CHAR_LEN  2

/**
 * 文字 a == b ?
 *
 * @param[in] a   文字a (NJ_UINT8* or NJ_CHAR*)
 * @param[in] b   文字b (NJ_UINT8* or NJ_CHAR*)
 *
 * @retval !0  a == b
 * @retval  0  a != b
 */
#ifdef NJ_OPT_UTF16
#define NJ_CHAR_IS_EQUAL(a, b) \
    ( (((NJ_UINT8*)(a))[0] == ((NJ_UINT8*)(b))[0]) && (((NJ_UINT8*)(a))[1] == ((NJ_UINT8*)(b))[1]) )
#else /* NJ_OPT_UTF16 */
#define NJ_CHAR_IS_EQUAL(a, b) \
    ( (((NJ_UINT8*)(a))[0] == ((NJ_UINT8*)(b))[0]) )
#endif /* NJ_OPT_UTF16 */

/**
 * 文字 a <= b ?
 *
 * @param[in] a   文字a (NJ_UINT8* or NJ_CHAR*)
 * @param[in] b   文字b (NJ_UINT8* or NJ_CHAR*)
 *
 * @retval !0  a <= b
 * @retval  0  a > b
 */
#ifdef NJ_OPT_UTF16
#define NJ_CHAR_IS_LESSEQ(a, b)                                         \
    ( (((NJ_UINT8*)(a))[0] < ((NJ_UINT8*)(b))[0]) ||                    \
      ((((NJ_UINT8*)(a))[0] == ((NJ_UINT8*)(b))[0]) && (((NJ_UINT8*)(a))[1] <= ((NJ_UINT8*)(b))[1])) )
#else /* NJ_OPT_UTF16 */
#define NJ_CHAR_IS_LESSEQ(a, b) \
    ( (((NJ_UINT8*)(a))[0] <= ((NJ_UINT8*)(b))[0]) )
#endif /* NJ_OPT_UTF16 */

/**
 * 文字 a >= b ?
 *
 * @param[in] a   文字a (NJ_UINT8* or NJ_CHAR*)
 * @param[in] b   文字b (NJ_UINT8* or NJ_CHAR*)
 *
 * @retval !0  a >= b
 * @retval  0  a < b
 */
#ifdef NJ_OPT_UTF16
#define NJ_CHAR_IS_MOREEQ(a, b)                                         \
    (  (((NJ_UINT8*)(a))[0] >  ((NJ_UINT8*)(b))[0]) ||                  \
      ((((NJ_UINT8*)(a))[0] == ((NJ_UINT8*)(b))[0]) && (((NJ_UINT8*)(a))[1] >= ((NJ_UINT8*)(b))[1])) )
#else /* NJ_OPT_UTF16 */
#define NJ_CHAR_IS_MOREEQ(a, b) \
    ( ((NJ_UINT8*)(a))[0] >= ((NJ_UINT8*)(b))[0] )
#endif /* NJ_OPT_UTF16 */

/**
 * 文字コードの差分(a - b)
 *
 * @param[in] a   文字a (NJ_UINT8*)
 * @param[in] b   文字b (NJ_UINT8*)
 *
 * @retval >0  a > b
 * @retval  0  a == b
 * @retval <0  a < b
 */
#define NJ_BYTE_DIFF(a, b)                                              \
        (NJ_INT16)(((NJ_UINT8*)(a))[0] - ((NJ_UINT8*)(b))[0])

/**
 * 文字コードの差分(a - b)
 *
 * @param[in] a   文字a (NJ_CHAR*)
 * @param[in] b   文字b (NJ_CHAR*)
 *
 * @retval >0  a > b
 * @retval  0  a == b
 * @retval <0  a < b
 */
#ifdef NJ_OPT_UTF16
#define NJ_CHAR_DIFF(a, b)                                              \
    ((NJ_INT16)                                                         \
     ( (((NJ_UINT8*)(a))[0] == ((NJ_UINT8*)(b))[0])                     \
       ? (((NJ_UINT8*)(a))[1] - ((NJ_UINT8*)(b))[1])                    \
       : (((NJ_UINT8*)(a))[0] - ((NJ_UINT8*)(b))[0]) )                  \
     )
#else /* NJ_OPT_UTF16 */
#define NJ_CHAR_DIFF(a, b)   ((NJ_INT16)(*(a) - *(b)))
#endif /* NJ_OPT_UTF16 */

/**
 * 1文字配列要素分をバイト単位でコピーする
 *
 * @param[in,out] dst   コピー先 (NJ_CHAR* or NJ_UINT8*)
 * @param[in]     src   コピー元 (NJ_CHAR* or NJ_UINT8*)
 */
#ifdef NJ_OPT_UTF16
#define NJ_CHAR_COPY(dst, src)                                          \
    {                                                                   \
        ((NJ_UINT8*)(dst))[0] = ((NJ_UINT8*)(src))[0];                  \
        ((NJ_UINT8*)(dst))[1] = ((NJ_UINT8*)(src))[1];                  \
    }
#else /* NJ_OPT_UTF16 */
#define NJ_CHAR_COPY(dst, src)                          \
    { ((NJ_UINT8*)(dst))[0] = ((NJ_UINT8*)(src))[0]; }
#endif /* NJ_OPT_UTF16 */

/**
 * 文字が半角カナ濁点か？
 *
 * @param[in] c     NJ_CHAR*
 *
 * @retval !0 半角カナ濁点
 * @retval  0 それ以外
 */
#ifdef NJ_OPT_UTF16
#define NJ_CHAR_IS_HANKANA_DAKUTEN(c)  NJ_CHAR_IS_EQUAL((c), "\xff\x9e")
#else /* NJ_OPT_UTF16 */
#define NJ_CHAR_IS_HANKANA_DAKUTEN(c)  (*(c) == 0xde)
#endif /* NJ_OPT_UTF16 */

/**
 * 半角カナ濁点が付けられる文字か？
 *
 * @param[in] c     NJ_CHAR*
 *
 * @retval !0 半角カナ濁点が付く文字
 * @retval  0 それ以外
 */
#ifdef NJ_OPT_UTF16
#define NJ_CHAR_IS_ENABLE_HANKANA_DAKUTEN(c)                            \
    ( (NJ_CHAR_IS_MOREEQ((c), "\xff\x76") && NJ_CHAR_IS_LESSEQ((c), "\xff\x84")) || \
      (NJ_CHAR_IS_MOREEQ((c), "\xff\x8a") && NJ_CHAR_IS_LESSEQ((c), "\xff\x8e")) )
#else /* NJ_OPT_UTF16 */
#define NJ_CHAR_IS_ENABLE_HANKANA_DAKUTEN(c)    \
    ( ((*(c) >= 0xb6) && (*(c) <= 0xc4)) ||     \
      ((*(c) >= 0xca) && (*(c) <= 0xce)) )
#endif /* NJ_OPT_UTF16 */

/**
 * 文字が半角カナ半濁点か？
 *
 * @param[in] c     NJ_CHAR*
 *
 * @retval !0 先頭文字が等しい
 * @retval  0 先頭文字が等しくない
 */
#ifdef NJ_OPT_UTF16
#define NJ_CHAR_IS_HANKANA_HANDAKUTEN(c)  NJ_CHAR_IS_EQUAL((c), "\xff\x9f")
#else /* NJ_OPT_UTF16 */
#define NJ_CHAR_IS_HANKANA_HANDAKUTEN(c)  (*(c) == 0xdf)
#endif /* NJ_OPT_UTF16 */

/**
 * 半角カナ濁点が付けられる文字か？
 *
 * @param[in] c     NJ_CHAR*
 *
 * @retval !0 半角カナ濁点が付く文字
 * @retval  0 それ以外
 */
#ifdef NJ_OPT_UTF16
#define NJ_CHAR_IS_ENABLE_HANKANA_HANDAKUTEN(c)                         \
    (NJ_CHAR_IS_MOREEQ((c), "\xff\x8a") && NJ_CHAR_IS_LESSEQ((c), "\xff\x8e"))
#else /* NJ_OPT_UTF16 */
#define NJ_CHAR_IS_ENABLE_HANKANA_HANDAKUTEN(c)   ((*(c) >= 0xca) && (*(c) <= 0xce))
#endif /* NJ_OPT_UTF16 */

/**
 * NJ_CHAR文字列長が0か？
 *
 * @param[in] c     NJ_CHAR*
 *
 * @retval !0 文字列長 == 0
 * @retval  0 文字列長 != 0
 */
#define NJ_CHAR_STRLEN_IS_0(c)   (*(c) == NJ_CHAR_NUL)

/**
 * 読み文字インデックスの格納バイト数が異常か？
 *
 * 読み文字インデックス対応の辞書フォーマットにおいて、
 * 辞書の読みコード格納バイト数情報が、文字コードの定義にマッチするかどうかを確認する。
 *
 * @param[in] size     辞書の読みコード格納バイト数
 *
 * @retval !0 異常(辞書使用不可)
 * @retval  0 正常(辞書使用可能)
 */
#ifdef NJ_OPT_UTF16
#define NJ_CHAR_ILLEGAL_DIC_YINDEX(size)   ((size) != 2)
#else  /* NJ_OPT_UTF16 */
#define NJ_CHAR_ILLEGAL_DIC_YINDEX(size)   ((size) > 2)
#endif /* NJ_OPT_UTF16 */


#ifdef NJ_OPT_UTF16
#define NJ_CHAR_LEN(s)                                                  \
    ( (NJ_CHAR_IS_MOREEQ((s), "\xD8\x00") && NJ_CHAR_IS_LESSEQ((s), "\xDB\xFF")) \
      ? ( (*((s)+1) == NJ_CHAR_NUL) ? 1 : 2)                            \
      : 1) /* サロゲートペア */
#else /* NJ_OPT_UTF16 */
#define NJ_CHAR_LEN(s)                                                     \
    ( (((*(s) >= 0x81) && (*(s) < 0xa0)) || ((*(s) > 0xdf) && (*(s) <= 0xfc))) \
      ? ((*((s) + 1) == NJ_CHAR_NUL) ? 1 : 2)                           \
      : 1 )
#endif /* NJ_OPT_UTF16 */

#ifdef NJ_OPT_UTF16
#define UTL_CHAR(s)  1
#else /* NJ_OPT_UTF16 */
#define UTL_CHAR(s)  NJ_CHAR_LEN(s)
#endif /* NJ_OPT_UTF16 */


#define NJ_GET_DIC_INFO(dicinfo) ((NJ_UINT8)((dicinfo)->type))

#ifdef NJ_OPT_DIC_STORAGE
#define NJ_GET_DIC_TYPE_EX(type, handle) \
            (  ((type) == NJ_DIC_H_TYPE_PROGRAM)     ? NJ_DIC_TYPE_PROGRAM :                        /* 擬似辞書 */ \
              (((type) == NJ_DIC_H_TYPE_PROGRAM_FZK) ? NJ_DIC_TYPE_PROGRAM_FZK :                    /* 擬似辞書-付属語- */ \
              (((type) == NJ_DIC_H_TYPE_ON_STORAGE)  ? ((NJ_STORAGE_DIC_INFO*)(handle))->dictype :  /* ストレージ辞書 */ \
                 NJ_GET_DIC_TYPE((handle)))) )                                                      /* 通常辞書 */
#else  /* NJ_OPT_DIC_STORAGE */
#define NJ_GET_DIC_TYPE_EX(type, handle) \
            (  ((type) == NJ_DIC_H_TYPE_PROGRAM)     ? NJ_DIC_TYPE_PROGRAM :      /* 擬似辞書 */ \
              (((type) == NJ_DIC_H_TYPE_PROGRAM_FZK) ? NJ_DIC_TYPE_PROGRAM_FZK :  /* 擬似辞書-付属語- */ \
                 NJ_GET_DIC_TYPE((handle))) )                                     /* 通常辞書 */
#endif /* NJ_OPT_DIC_STORAGE */

#define NJ_CLEAR_OTHERFREQ_IN_DICINFO(info) \
    { \
        (info)->dic_freq[NJ_MODE_TYPE_YOSOKU].base  = 0; \
        (info)->dic_freq[NJ_MODE_TYPE_YOSOKU].high  = 0; \
        (info)->dic_freq[NJ_MODE_TYPE_MORPHO].base  = 0; \
        (info)->dic_freq[NJ_MODE_TYPE_MORPHO].high = 0; \
    }


#define GET_STORAGE_CACHE_HEAD(h) ((NJ_UINT8*)(((NJ_STORAGE_DIC_INFO*)(h))->cache_area[NJ_STORAGE_DIC_HEAD]))

/*-----------------------------------------------------*/
/* 辞書アダプタ用マクロ                                */
/*-----------------------------------------------------*/


#define GET_BITFIELD_16(data, pos, width)                        \
    ((NJ_UINT16)(((NJ_UINT16)(data) >> (16 - (pos) - (width))) & \
                 ((NJ_UINT16)0xffff >> (16 - (width)       ))))

#define GET_BITFIELD_32(data, pos, width)       \
    ((NJ_UINT32)(((NJ_UINT32)(data) >> (32 - (pos) - (width))) & ((NJ_UINT32)0xffffffff >> (32 - (width)))))

#define GET_BIT_TO_BYTE(bit) ((NJ_UINT8)(((bit) + 7) >> 3))

#define V_ZOKUSEI_ADDR(h,x,y) (((h) + (NJ_UINT32)(((x) - 1) * (y))))

#define R_ZOKUSEI_ADDR(h,x,y) (((h) + (NJ_UINT32)(((x) - 1) * (y))))


#define INIT_KEYWORD_IN_NJ_DIC_SET(x) \
    { (x)->keyword[0] = NJ_CHAR_NUL; (x)->keyword[1] = NJ_CHAR_NUL; }

#define GET_ERR_FUNCVAL(errval) \
    ((NJ_UINT16)(((NJ_UINT16)(errval) & 0x007F) << 8))


#define RLT_GETREPRLEN(x) \
    (NJ_GET_KLEN_FROM_STEM(&((x)->word)) + NJ_GET_KLEN_FROM_FZK(&((x)->word)))

#define RLT_GETYOMILEN(x) \
    ((GET_LOCATION_OPERATION((x)->word.stem.loc.status) == NJ_CUR_OP_COMP_EXT) ? \
     ((NJ_UINT16)(RLT_GETREPRLEN((x)))) : \
     ((NJ_UINT16)(NJ_GET_YLEN_FROM_STEM(&((x)->word)) + NJ_GET_YLEN_FROM_FZK(&((x)->word)))))

#define RLT_HASFZK(x) ((NJ_UINT8)((NJ_GET_YLEN_FROM_FZK(&((x)->word))==0)?0:1))

#define RLT_GETLASTPOS(x) \
    ((NJ_UINT16)(RLT_HASFZK(x)?NJ_GET_BPOS_FROM_FZK(&((x)->word)):NJ_GET_BPOS_FROM_STEM(&((x)->word))))

#define RLT_GETSCORE_CONV(x) \
    ((NJ_INT32)((NJ_INT32)NJ_GET_FREQ_FROM_STEM(&((x)->word)) + (NJ_INT32)NJC_PHRASE_COST))
#define RLT_GETSCORE_CAND(x) (cand_getScore(x))

#define GIJI_GETSCORE(_len) \
    ((NJ_INT32)((NJ_INT16)(_len) * ((NJC_PHRASE_COST * (NJ_INT16)sizeof(NJ_CHAR)) / 2)) < NJ_NUM_MIN_INT16 ? NJ_NUM_MIN_GIJI_FREQ    \
     : (NJ_INT16)((NJ_INT16)(_len) * ((NJC_PHRASE_COST * (NJ_INT16)sizeof(NJ_CHAR)) / 2)))

#define RLT_GETFZKYOMILEN(x) (NJ_GET_YLEN_FROM_FZK(&((x)->word)))

#define WSENT_GETLASTYOMILEN(x) \
    ((NJ_UINT16)(((x)->phrNum==0)?0:RLT_GETYOMILEN(&((x)->phrases[(x)->phrNum-1]))))

#define WSENT_GETLASTPOS(x) \
    ((NJ_UINT16)(((x)->phrNum==0)?0:RLT_GETLASTPOS(&((x)->phrases[(x)->phrNum-1]))))


#define WSENT_GETINFO_HANDLE(x,index) \
    ((((index)<0)||((index)>=(x)->phrNum))?NULL:((x)->info[(index)].handle))
#define WSENT_GETINFO_ID(x,index) \
    ((((index)<0)||((index)>=(x)->phrNum))?0:((x)->info[(index)].id))
#define WSENT_SETINFO_LASTWORD(s,h,i) \
    (((s)->phrNum==0)?0:((s)->info[(s)->phrNum-1].handle = (h), (s)->info[(s)->phrNum-1].id = (i)))
#define WSENT_GETINFO_LPOS(x,index) \
    ((((index)<0)||((index)>=(x)->phrNum))?0:((x)->info[(index)].id))
#define WSENT_SETINFO_LASTLPOS(s,pos) \
    (((s)->phrNum==0)?0:((s)->info[(s)->phrNum-1].id = (pos)))
#define WSENT_GETINFO_LASTRPOS(x) \
    (((x)->phrNum==0)?0:((x)->info[(x)->phrNum-1].flag & NJC_WS_FLAG_RPOS_MASK))
#define WSENT_SETINFO_LASTRPOS(s,v) \
    (((s)->phrNum==0)?0:((s)->info[(s)->phrNum-1].flag &= ~NJC_WS_FLAG_RPOS_MASK, (s)->info[(s)->phrNum-1].flag |= (v)))
#define WSENT_GETFLAG(x,index) \
    ((((index)<0)||((index)>=(x)->phrNum))?0:((x)->info[(index)].flag & NJC_WS_FLAG_FUNCTION_MASK))
#define WSENT_GETLASTFLAG(x) \
    ((NJ_UINT32)(((x)->phrNum==0)?0:((x)->info[(x)->phrNum-1].flag & NJC_WS_FLAG_FUNCTION_MASK)))
#define WSENT_SETLASTFLAG(s,v) \
    (((s)->phrNum==0)?0:((s)->info[(s)->phrNum-1].flag &= ~NJC_WS_FLAG_FUNCTION_MASK, (s)->info[(s)->phrNum-1].flag |= (v)))

#define FZK_GET_YLEN(x) ((NJ_UINT16)((x)->info1 & 0x7f))
#define FZK_GET_FPOS(x) ((NJ_UINT16)((x)->info1 >> 7))
#define FZK_GET_TLEN(x) ((NJ_UINT16)((x)->info2 & 0x7f))
#define FZK_GET_BPOS(x) ((NJ_UINT16)((x)->info2 >> 7))
#define FZK_GET_FREQ(x) ((NJ_INT16)((x)->hindo))

#define FZK_SET_FPOS(s,v) ((s)->info1 = ((s)->info1 & 0x007F) | ((v) << 7))
#define FZK_SET_YLEN(s,v) ((s)->info1 = ((s)->info1 & 0xFF80) | ((v) & 0x7F))
#define FZK_SET_BPOS(s,v) ((s)->info2 = ((s)->info2 & 0x007F) | ((v) << 7))
#define FZK_SET_TLEN(s,v) ((s)->info2 = ((s)->info2 & 0xFF80) | ((v) & 0x7F))
#define FZK_SET_FREQ(s,v) ((s)->hindo = (NJ_HINDO)(v))
#define FZK_GET_SCORE(x) \
    ((NJ_INT32)((NJ_INT32)FZK_GET_FREQ(x) + (NJ_INT32)NJC_PHRASE_COST))

#define CNV_GET_OVERRUN(x)  ((NJ_UINT16)((x)->flags & 0x0001))
#define CNV_SET_OVERRUN(s,v) ((s)->flags = (v))


#define CONNECT_R(cf, posF)                                             \
    ((*((cf) + (((posF) - 1) / 8)) & (0x80 >> (((posF) - 1) % 8))) ? 1 : 0)
#define GET_CONNECT_EXT_R(cf, posF)                                             \
    ((*((cf) + (((posF) - 1) / 8)) >> (7-(((posF) - 1) % 8))) & 0x01)

/************************************************/
/*          iWnn内部用共通関数定義              */
/************************************************/

/*-----------------------------------------------------*/
/* ncconv.c                                            */
/*-----------------------------------------------------*/
extern NJ_INT16 njc_conv(NJ_CLASS *iwnn, NJ_DIC_SET *dic, NJ_CHAR *yomi,
                         NJ_UINT8 analyze_level, NJ_UINT8 devide_pos,
                         NJ_RESULT *results, NJ_UINT8 nogiji);
extern NJ_INT16 njc_zenkouho(NJ_CLASS *iwnn, NJ_RESULT *target,
                             NJ_UINT16 candidate_no, NJ_RESULT *result,
                             NJ_UINT8 top_set);
extern NJ_INT16 njc_zenkouho1(NJ_CLASS *iwnn, NJ_DIC_SET *dic, NJ_RESULT *target,
                             NJ_RESULT *result);
extern NJ_INT16 njc_top_conv(NJ_CLASS *iwnn, NJ_DIC_SET *dic, NJ_CHAR *yomi,
                             NJ_RESULT *results, NJ_UINT16 size);
extern NJ_UINT8 njc_init_conv(NJ_CLASS *iwnn);
extern NJ_INT16 njc_get_stroke(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_CHAR *stroke,
                               NJ_UINT16 size);
extern NJ_INT16 njc_get_candidate(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_CHAR *candidate,
                             NJ_UINT16 size);

/*-----------------------------------------------------*/
/* ndapi.c                                             */
/*-----------------------------------------------------*/
extern NJ_INT16 njd_get_word_data(NJ_CLASS *iwnn, NJ_DIC_SET *dicset, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 dic_idx, NJ_WORD *word);
extern NJ_INT16 njd_search_word(NJ_CLASS *iwnn, NJ_CURSOR *cursor, NJ_UINT8 comp_flg, NJ_UINT8 *exit_flag);
extern NJ_INT16 njd_get_word(NJ_CLASS *iwnn, NJ_CURSOR *cursor, NJ_RESULT *result, NJ_UINT8 comp_flg);
extern NJ_INT16 njd_get_relational_word(NJ_CLASS *iwnn, NJ_RESULT *current, NJ_RESULT *next,
                               NJ_DIC_FREQ *dic_freq);
extern NJ_INT16 njd_get_stroke(NJ_CLASS *iwnn, NJ_RESULT *result,
                               NJ_CHAR *stroke, NJ_UINT16 size);
extern NJ_INT16 njd_get_candidate(NJ_CLASS *iwnn, NJ_RESULT *result,
                               NJ_CHAR *candidate, NJ_UINT16 size);
extern NJ_INT16 njd_get_word_info(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_WORD_INFO *info);
extern NJ_INT16 njd_add_word(NJ_CLASS *iwnn, NJ_LEARN_WORD_INFO *lword,
                             NJ_UINT8 connect, NJ_UINT8 undo);
extern NJ_INT16 njd_learn_word(NJ_CLASS *iwnn, NJ_RESULT *l_result, NJ_LEARN_WORD_INFO *lword,
                               NJ_UINT8 connect, NJ_UINT8 undo_flag, NJ_UINT8 ext_hindo_flag);
extern NJ_INT16 njd_optimize_ext_area(NJ_CLASS *iwnn);
extern NJ_INT16 njd_init_program_dic_message(NJ_PROGRAM_DIC_MESSAGE* prog_msg);
extern NJ_INT16 njd_init_search_location_set(NJ_SEARCH_LOCATION_SET* loctset);
extern NJ_INT16 njd_init_search_condition(NJ_SEARCH_CONDITION* condition);
extern NJ_INT16 njd_init_word(NJ_WORD* word);
extern NJ_INT16 njd_set_cursor_search_end(NJ_CURSOR *cursor, NJ_UINT16 dic_id);
extern NJ_INT16 njd_clear_dicinfo(NJ_DIC_INFO *info);
extern NJ_INT16 njd_copy_dicinfo(NJ_DIC_INFO *dest_info, NJ_DIC_INFO *src_info, NJ_INT16 src_mode_type);
extern NJ_INT32 njd_get_additional_info(NJ_CLASS *iwnn, NJ_RESULT *result, NJ_INT8 index,
                                        NJ_CHAR *add_info, NJ_UINT32 size);


/*-----------------------------------------------------*/
/* ndbdic.c                                            */
/*-----------------------------------------------------*/
extern NJ_INT16 njd_b_search_word(NJ_SEARCH_CONDITION *con,
                                  NJ_SEARCH_LOCATION_SET *loctset);
extern NJ_INT16 njd_b_get_word(NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word);
extern NJ_INT16 njd_b_get_candidate(NJ_WORD *word, NJ_CHAR *candidate,
                                    NJ_UINT16 size);
extern NJ_INT16 njd_b_get_stroke(NJ_WORD *word, NJ_CHAR *stroke, NJ_UINT16 size);

/*-----------------------------------------------------*/
/* ndfdic.c                                            */
/*-----------------------------------------------------*/
extern NJ_INT16 njd_f_search_word(NJ_SEARCH_CONDITION *con,
                                  NJ_SEARCH_LOCATION_SET *loctset);
extern NJ_INT16 njd_f_get_word(NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word);
extern NJ_INT16 njd_f_get_stroke(NJ_WORD *word, NJ_CHAR *stroke,
                                 NJ_UINT16 size);
extern NJ_INT16 njd_f_get_candidate(NJ_WORD *word, NJ_CHAR *candidate,
                                    NJ_UINT16 size);

/*-----------------------------------------------------*/
/* ndpdic.c                                            */
/*-----------------------------------------------------*/
extern NJ_INT16 njd_p_search_word(NJ_CLASS * iwnn, NJ_SEARCH_CONDITION *con,
                                  NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 dic_idx);
extern NJ_INT16 njd_p_get_word(NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word);
extern NJ_INT16 njd_p_get_stroke(NJ_WORD *word, NJ_CHAR *stroke,
                                 NJ_UINT16 size);
extern NJ_INT16 njd_p_get_candidate(NJ_WORD *word, NJ_CHAR *candidate,
                                    NJ_UINT16 size);
extern NJ_INT16 njd_p_check_additional_info(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *add_info, NJ_UINT32 size);
extern NJ_INT32 njd_p_get_additional_info(NJ_CLASS *iwnn, NJ_WORD *word, NJ_UINT8 index, NJ_CHAR *add_info, NJ_UINT32 size);

/*-----------------------------------------------------*/
/* ndldic.c                                            */
/*-----------------------------------------------------*/
extern NJ_INT16 njd_l_search_word(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *con,
                                  NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 comp_flg);
extern NJ_INT16 njd_l_add_word(NJ_CLASS *iwnn, NJ_LEARN_WORD_INFO *word,
                                NJ_UINT8 connect, NJ_UINT8 type,
                                NJ_UINT8 undo, NJ_UINT8 dictype);

extern NJ_INT16 njd_l_delete_word(NJ_CLASS *iwnn, NJ_RESULT *result);
extern NJ_INT16 njd_l_get_word(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word);
extern NJ_INT16 njd_l_get_stroke(NJ_CLASS *iwnn, NJ_WORD *word,
                                 NJ_CHAR *stroke, NJ_UINT16 size);
extern NJ_INT16 njd_l_get_candidate(NJ_CLASS *iwnn, NJ_WORD *word,
                                 NJ_CHAR *candidate, NJ_UINT16 size);
extern NJ_INT16 njd_l_undo_learn(NJ_CLASS *iwnn, NJ_UINT16 undo_count);
extern NJ_INT16 njd_l_check_dic(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 restore);
extern NJ_INT16 njd_l_init_area(NJ_DIC_HANDLE handle);
extern NJ_INT16 njd_l_make_space(NJ_CLASS *iwnn, NJ_UINT16 count, NJ_UINT8 mode);
extern NJ_INT16 njd_l_get_relational_word(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION *loc,
                                 NJ_WORD *word, NJ_DIC_FREQ *mdic_freq);
extern NJ_INT16 njd_l_check_word_connect(NJ_CLASS *iwnn, NJ_WORD *word);
extern NJ_INT16 njd_l_get_ext_word_data(NJ_CLASS *iwnn, NJ_WORD *word, NJ_UINT16 *hinsi, NJ_UINT8 *len);
extern NJ_INT16 njd_l_mld_op_commit(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle);
extern NJ_INT16 njd_l_mld_op_commit_to_top(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle);
extern NJ_INT16 njd_l_mld_op_commit_cancel(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle);
extern NJ_INT16 njd_l_mld_op_get_space(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle);
extern NJ_INT32 njd_l_get_additional_info(NJ_CLASS *iwnn, NJ_WORD *word, NJ_UINT8 index, NJ_CHAR *add_info, NJ_UINT32 size);
extern NJ_INT16 njd_l_write_learn_data(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id,
                                 NJ_LEARN_WORD_INFO *word, NJ_UINT8 connect, NJ_UINT8 type, NJ_UINT8 undo,
                                 NJ_UINT8 muhenkan);
extern NJ_INT16 njd_l_delete_index(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
extern void njd_l_write_uint16_data(NJ_UINT8* ptr, NJ_UINT16 data);
extern NJ_WQUE *njd_l_get_que(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
extern NJ_CHAR *njd_l_get_string(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id, NJ_UINT8 *slen, NJ_UINT8 segment_num);
extern NJ_CHAR *njd_l_get_hyouki(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id, NJ_UINT8 *slen, NJ_UINT8 segment_num);
extern NJ_UINT16 njd_l_search_prev_que(NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
extern NJ_INT32 njd_l_get_attr_hindo(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset, NJ_DIC_HANDLE handle, NJ_UINT16 que_id, NJ_UINT32 *que_hindo);
extern NJ_UINT16 njd_l_search_next_que(NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
extern NJ_INT16 njd_l_init_cmpdg_info(NJ_CLASS* iwnn);

/*-----------------------------------------------------*/
/* ndtdic.c                                            */
/*-----------------------------------------------------*/
extern NJ_INT16 njd_t_search_word(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *con,
                                  NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 comp_flg, NJ_UINT16 dic_idx);
extern NJ_INT16 njd_t_get_word(NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word);
extern NJ_INT16 njd_t_get_candidate(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size);
extern NJ_INT16 njd_t_get_stroke(NJ_WORD *word, NJ_CHAR *stroke, NJ_UINT16 size);
extern NJ_INT16 njd_t_get_relational_word(NJ_SEARCH_LOCATION *loc,
                                          NJ_WORD *word, NJ_DIC_FREQ *mdic_freq );
extern NJ_INT16 njd_t_get_ext_area_size(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT32 *size);
extern NJ_INT16 njd_t_init_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_data, NJ_UINT32 size);
extern NJ_INT16 njd_t_check_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_data, NJ_UINT32 size);
extern NJ_INT16 njd_t_check_ext_area2(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_data, NJ_UINT32 size);
extern NJ_INT16 njd_t_delete_word(NJ_CLASS *iwnn, NJ_RESULT *result);
extern NJ_INT16 njd_t_learn_word(NJ_CLASS *iwnn, NJ_WORD *word, NJ_LEARN_WORD_INFO *lword, NJ_UINT16 idx);
extern NJ_INT16 njd_t_optimize_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_data);
extern NJ_UINT8 njd_t_calc_ext_hindo(NJ_CLASS *iwnn, NJ_UINT8 now);
extern NJ_INT16 njd_t_check_additional_info(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *add_info, NJ_UINT32 size);
extern NJ_INT32 njd_t_get_additional_info(NJ_CLASS *iwnn, NJ_WORD *word, NJ_UINT8 index, NJ_CHAR *add_info, NJ_UINT32 size);
extern NJ_INT16 njd_t_delete_word_ext(NJ_CLASS *iwnn, NJ_WORD_INFO *info, NJ_UINT16 idx);
extern NJ_INT32 njd_t_get_user_prof_data_size(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_area, NJ_EXT_HINDO_INFO *ext_hindo_info);
extern NJ_INT32 njd_t_export_user_prof_data(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                                            NJ_VOID *ext_area, NJ_VOID *exp_data, NJ_EXT_HINDO_INFO *ext_hindo_info, NJ_UINT8 hindo);
extern NJ_INT32 njd_t_import_user_prof_data(NJ_CLASS *iwnn, NJ_WORD_INFO *info, NJ_UINT16 idx,
                                            NJ_INT16 fhinsi, NJ_INT16 bhinsi, NJ_UINT8 hindo_data);

/*-----------------------------------------------------*/
/* ndrdic.c                                            */
/*-----------------------------------------------------*/
extern NJ_INT16 njd_r_get_hinsi(NJ_DIC_HANDLE rule, NJ_UINT8 type);
extern NJ_INT16 njd_r_get_connect(NJ_DIC_HANDLE rule,
                                  NJ_UINT16 hinsi, NJ_UINT8 type,
                                  NJ_UINT8 **connect);
extern NJ_INT16 njd_r_get_count(NJ_DIC_HANDLE rule,
                                NJ_UINT16 *fcount, NJ_UINT16 *rcount);
extern NJ_INT16 njd_r_check_group(NJ_DIC_HANDLE rule, NJ_UINT16 pos);
extern NJ_INT16 njd_r_get_connect_ext(NJ_DIC_HANDLE rule,
                                  NJ_UINT16 hinsi, NJ_UINT8 type,
                                  NJ_UINT8 **connect);

/*-----------------------------------------------------*/
/* ndsdic.c                                            */
/*-----------------------------------------------------*/
extern NJ_INT16 njd_s_search_word(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *con,
                                  NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 comp_flg, NJ_UINT16 dic_idx);
extern NJ_INT16 njd_s_get_word(NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word);
extern NJ_INT16 njd_s_get_candidate(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size);
extern NJ_INT16 njd_s_get_stroke(NJ_WORD *word, NJ_CHAR *stroke, NJ_UINT16 size);
extern NJ_INT16 njd_s_get_relational_word(NJ_SEARCH_LOCATION *loc,
                                          NJ_WORD *word, NJ_DIC_FREQ *mdic_freq );
extern NJ_INT16 njd_s_get_ext_area_size(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT32 *size);
extern NJ_INT16 njd_s_init_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_data, NJ_UINT32 size);
extern NJ_INT16 njd_s_check_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_data, NJ_UINT32 size);
extern NJ_INT16 njd_s_check_ext_area2(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_data, NJ_UINT32 size);
extern NJ_INT16 njd_s_delete_word(NJ_CLASS *iwnn, NJ_RESULT *result);
extern NJ_INT16 njd_s_learn_word(NJ_CLASS *iwnn, NJ_WORD *word, NJ_LEARN_WORD_INFO *lword, NJ_UINT16 idx);
extern NJ_INT16 njd_s_optimize_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_data);
extern NJ_UINT8 njd_s_calc_ext_hindo(NJ_CLASS *iwnn, NJ_UINT8 now);
extern NJ_INT16 njd_s_check_additional_info(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *add_info, NJ_UINT32 size);
extern NJ_INT32 njd_s_get_additional_info(NJ_CLASS *iwnn, NJ_WORD *word, NJ_UINT8 index, NJ_CHAR *add_info, NJ_UINT32 size);
extern NJ_INT16 njd_s_delete_word_ext(NJ_CLASS *iwnn, NJ_WORD_INFO *info, NJ_UINT16 idx);
extern NJ_INT32 njd_s_get_storage_dic_cache_size(NJ_CLASS *iwnn, NJ_UINT32 dictype, NJ_FILE* filestream, NJ_UINT32 mode);
extern NJ_INT32 njd_s_set_storage_dic_info(NJ_CLASS *iwnn, NJ_STORAGE_DIC_INFO *fdicinfo, NJ_UINT32 dictype,
                                           NJ_FILE* filestream, NJ_UINT8 *cache_area, NJ_UINT32 cache_size, NJ_UINT32 mode);
extern NJ_INT32 njd_s_get_user_prof_data_size(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_area, NJ_EXT_HINDO_INFO *ext_hindo_info);
extern NJ_INT32 njd_s_export_user_prof_data(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, 
                                            NJ_VOID *ext_area, NJ_VOID *exp_data, NJ_EXT_HINDO_INFO *ext_hindo_info, NJ_UINT8 hindo);
extern NJ_INT32 njd_s_import_user_prof_data(NJ_CLASS *iwnn, NJ_WORD_INFO *info, NJ_UINT16 idx,
                                            NJ_INT16 fhinsi, NJ_INT16 bhinsi, NJ_UINT8 hindo_data);

/*-----------------------------------------------------*/
/* neapi.c                                             */
/*-----------------------------------------------------*/
extern NJ_LEARN_WORD_INFO *nje_get_previous_selection(NJ_CLASS *iwnn, NJ_INT32 count, NJ_UINT8 mode);

/*-----------------------------------------------------*/
/* necode.c                                            */
/*-----------------------------------------------------*/
extern NJ_UINT16 nje_check_string(NJ_CHAR *s, NJ_UINT16 max_len);
extern NJ_UINT8 nje_get_top_char_type(NJ_CHAR *s);
extern NJ_INT16 nje_convert_kata_to_hira(NJ_CHAR *kata, NJ_CHAR *hira, NJ_UINT16 len, NJ_UINT16 max_len, NJ_UINT8 type);
extern NJ_INT16 nje_convert_hira_to_kata(NJ_CHAR *hira, NJ_CHAR *kata, NJ_UINT16 len);

/*-----------------------------------------------------*/
/* nchomo.c                                            */
/*-----------------------------------------------------*/
extern NJ_INT16 nje_append_homonym(NJ_CLASS *iwnn, NJ_RESULT *result,
                                   NJ_UINT16 yomiLen, NJ_INT16 *offset);
extern NJ_UINT8 nje_clear_homonym_buf(NJ_CLASS *iwnn);
extern NJ_INT16 nje_append_homonym_string(NJ_CLASS *iwnn, NJ_CHAR *str);
extern NJ_CHAR  *nje_get_homonym_string(NJ_CLASS *iwnn, NJ_UINT16 index);


/*-----------------------------------------------------*/
/* nfapi.c                                             */
/*-----------------------------------------------------*/
extern NJ_UINT8 njf_init_analyze(NJ_CLASS *iwnn);
extern NJ_UINT16 njf_get_keep_result(NJ_CLASS *iwnn, NJ_UINT8 type,
                                     NJ_RESULT **result);
extern NJ_INT16 njf_get_candidate(NJ_CLASS *iwnn, NJ_RESULT *result,
                                  NJ_CHAR *buf, NJ_UINT16 buf_size);
extern NJ_INT16 njf_get_stroke(NJ_CLASS *iwnn, NJ_RESULT *result,
                               NJ_CHAR *buf, NJ_UINT16 buf_size);

/*-----------------------------------------------------*/
/* nj_str.c                                            */
/*-----------------------------------------------------*/
extern NJ_CHAR  *nj_strcpy(NJ_CHAR *dst, NJ_CHAR *src);
extern NJ_CHAR  *nj_strncpy(NJ_CHAR *dst, NJ_CHAR *src, NJ_UINT16 n);
extern NJ_UINT16 nj_strlen(NJ_CHAR *c);
extern NJ_INT16  nj_strcmp(NJ_CHAR *s1, NJ_CHAR *s2);
extern NJ_INT16  nj_strncmp(NJ_CHAR *s1, NJ_CHAR *s2, NJ_UINT16 n);
extern NJ_UINT16 nj_charlen(NJ_CHAR *c);
extern NJ_INT16  nj_charncmp(NJ_CHAR *s1, NJ_CHAR *s2, NJ_UINT16 n);
extern NJ_CHAR  *nj_charncpy(NJ_CHAR *dst, NJ_CHAR *src, NJ_UINT16 n);
extern NJ_UINT8 *nj_memcpy(NJ_UINT8 *dst, NJ_UINT8 *src, NJ_UINT16 n);
extern NJ_INT16  nj_memcmp(NJ_UINT8 *s1,  NJ_UINT8 *s2,  NJ_UINT16 n);
extern NJ_UINT16 nj_strlen_limit(NJ_CHAR *c, NJ_UINT16 limit);

/*-----------------------------------------------------*/
/* ndcommon.c                                          */
/*-----------------------------------------------------*/
extern NJ_INT16 njd_connect_test(NJ_SEARCH_CONDITION *con, NJ_UINT16 hinsiF, NJ_UINT16 hinsiR);
extern NJ_INT32 njd_get_attr_bias(NJ_CLASS *iwnn, NJ_UINT32 attr_data);
extern NJ_INT16 njd_search_charset_range(NJ_CHARSET *charset, NJ_CHAR  *key, NJ_UINT16 *start, NJ_UINT16 *end);
extern NJ_INT16 njd_init_word_info(NJ_WORD_INFO *info);

/*-----------------------------------------------------*/
/* nj_fio.c                                            */
/*-----------------------------------------------------*/
extern NJ_INT32 nj_fread(NJ_UINT8 *buffer, NJ_UINT32 size, NJ_FILE *filestream);
extern NJ_INT32 nj_fseek(NJ_FILE *filestream, NJ_UINT32 offset, NJ_INT32 origin);
extern NJ_INT32 njd_offset_fread(NJ_STORAGE_DIC_INFO *fdicinfo, NJ_UINT32 offset, NJ_UINT32 size, NJ_UINT8 *dst);
extern NJ_INT32 njd_offset_fread_cache(NJ_STORAGE_DIC_INFO *fdicinfo, NJ_UINT32 index, NJ_UINT32 offset, 
                                       NJ_UINT32 size, NJ_UINT8 **dst);


#endif /* _NJ_EXTERN_H_ */
