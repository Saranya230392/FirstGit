/**
 * @file
 *   辞書アダプタ：統合辞書アダプタ
 *
 *   統合辞書フォーマットへのアクセス関数を提供する
 *
 * @author
 *   Copyright(C) OMRON SOFTWARE Co., Ltd. 2008-2012 All Rights Reserved.
 */

/************************************************/
/*           インクルードファイル               */
/************************************************/
#include "nj_lib.h"
#include "nj_err.h"
#include "nj_dicif.h"
#include "njfilter.h"
#include "nj_ext.h"
#include "nj_dic.h"
#include "njd.h"

/************************************************/
/*        static 変数宣言                       */
/************************************************/

/************************************************/
/*              define  宣  言                  */
/************************************************/
#define APPEND_YOMI_FLG(h) ((NJ_UINT32)(0x80000000 & NJ_INT32_READ((h) + 0x1C)))
#define WORD_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x20)))
#define YOMI_HYOKI_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x24)))
#define HINDO_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x28)))
#define TSUNAGARI_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x2C)))
#define YOMI_IDX_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x30)))
#define TSUNAGARI_IDX_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x34)))
#define HYOKI_IDX_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x38)))
#define ZOKUSEI_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x3C)))
#define ZOKUSEI_AREA_SIZE(h) ((NJ_INT32)(NJ_INT32_READ((h) + 0x40)))
#define HINSI_NO_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x44)))
#define FHINSI_NO_CNT(h) ((NJ_INT16)(NJ_INT16_READ((h) + 0x48)))
#define FHINSI_NO_BYTE(h) ((NJ_INT8)(*((h) + 0x4A)))
#define BIT_FHINSI(h) ((NJ_UINT8)(*((h) + 0x4B)))
#define BHINSI_NO_CNT(h) ((NJ_INT16)(NJ_INT16_READ((h) + 0x4C)))
#define BHINSI_NO_BYTE(h) ((NJ_INT8)(*((h) + 0x4E)))
#define BIT_BHINSI(h) ((NJ_UINT8)(*((h) + 0x4F)))
#define WORD_AREA_CNT(h) ((NJ_UINT32)(NJ_INT32_READ((h) + 0x50)))
#define TSUNAGARI_AREA_CNT(h) ((NJ_UINT32)(NJ_INT32_READ((h) + 0x54)))
#define YOMI_IDX_AREA_CNT(h) ((NJ_INT32)(NJ_INT32_READ((h) + 0x58)))
#define TSUNAGARI_IDX_AREA_CNT(h) ((NJ_INT32)(NJ_INT32_READ((h) + 0x5C)))
#define HYOKI_IDX_AREA_CNT(h) ((NJ_INT32)(NJ_INT32_READ((h) + 0x60)))
#define REAL_ZOKUSEI_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x64)))
#define REAL_ZOKUSEI_AREA_CNT(h) ((NJ_INT32)(NJ_INT32_READ((h) + 0x68)))
#define REAL_ZOKUSEI_AREA_SIZE(h) ((NJ_INT32)(NJ_INT32_READ((h) + 0x6C)))

#define JITU_MOJI_HENKAN_DATA_AREA_TOP_ADDR(h)  ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x70)))
#define JITU_MOJI_HENKAN_DATA_AREA_CNT(h) ((NJ_INT16)(NJ_INT16_READ((h) + 0x74)))
#define JITU_MOJI_HENKAN_DATA_AREA_BYTE_SIZE(h) ((NJ_INT16)(NJ_INT16_READ((h) + 0x76)))

#define STEM_TERMINATOR(x) ((NJ_UINT8)(0x80 & (*(x))))
#define WORD_SIZE(x)  ((NJ_UINT16)(0x007F & (x)))
#define YOMI_LEN(h)  ((NJ_UINT8)((0x7F & *(h))/sizeof(NJ_CHAR)))
#define HYOKI_LEN(h) ((NJ_UINT8)((0x7F & *((h) + 0x04))/sizeof(NJ_CHAR)))
#define HYOKI_INFO(h) ((NJ_UINT8)(0x80 & *((h) + 0x04)))
#define YOMI_DATA_OFFSET(h) ((NJ_UINT32)(0x00FFFFFF & NJ_INT32_READ(h)))
#define HYOKI_DATA_OFFSET(h) ((NJ_UINT32)(0x00FFFFFF & NJ_INT32_READ((h) + 0x04)))
#define WORD_DATA_ADDR_HIGH_COMPRESS(h,x) ((h) + (NJ_UINT32)(((x) - 1) * WORD_DATA_HIGH_COMPRESS_AREA_SIZE))
#define WORD_DATA_ADDR(h,x) ((h) + (NJ_UINT32)(((x) - 1) * WORD_DATA_AREA_SIZE))
#define HINDO_HENKAN_ADDR(h,x) (((h) + (NJ_UINT32)(((x) - 1) * 0x02)))
#define HINDO_YOSOKU_ADDR(h,x) (((h) + (NJ_UINT32)((((x) - 1) * 0x02) + 0x01)))
#define HINSI_DATA_HIGH_COMPRESS(h) (NJ_INT16_READ((h) + 0x04))
#define HINSI_DATA(h) (NJ_INT16_READ((h) + 0x08))
#define HINDO_HENKAN_ON(h) ((NJ_UINT8)(0x40 & *(h)))
#define HINDO_TSUNAGARI_WORD(h) ((NJ_UINT8)(0x80 & *(h)))
#define HINDO_YOSOKU_1WORD(h) ((0xC0 & (*(h))) == 0x40)
#define HINDO_YOSOKU_2WORD(h) (((0xC0 & (*(h))) != 0xC0) && ((0xC0 & (*(h))) != 0x00))
#define HINDO_YOSOKU_3WORD(h) ((0xC0 & (*(h))) != 0x00)
#define HINDO_YOSOKU_WORD(h) ((0xC0 & (*(h))))

#define TSUNAGARI_SRC_NO(h,x) ((NJ_UINT32)(((0xFFFF & (NJ_INT16_READ((h) + (NJ_UINT32)(((x) - 1) * 0x06)))) << 8) + \
                                           *((h) + ((NJ_UINT32)(((x) - 1) * 0x06) + 0x02))))
#define TSUNAGARI_DST_NO(h,x) ((NJ_UINT32)(((*((h) + (NJ_UINT32)(((x) - 1) * 0x06) + 0x03)) << 16) + \
                                           (0xFFFF & (NJ_INT16_READ((h) + (NJ_UINT32)(((x) - 1) * 0x06) + 0x04)))))
#define TSUNAGARI_WORD_DATA_NO(h) ((NJ_UINT32)(0x00FFFFFF & NJ_INT32_READ(h)))
#define HYOKI_INDEX_ADDR(h,x) (((h) + (NJ_UINT32)(((x) - 1) * 0x04)))
#define HYOKI_INDEX_WORD_NO(h) ((NJ_UINT32)(0x00FFFFFF & NJ_INT32_READ(h)))
#define HINDO_MORPHO(h) ((NJ_INT8)(0x7F & *(h)))
#define HINDO_DATA(h) ((NJ_INT16)(0x3F & *(h)))
#define HINDO_TSUNAGARI_YOSOKU(h) ((NJ_UINT8)(0x40 & *(h)))
#define TSUNAGARI_DATA_ADDR(h,x) (((h) + (NJ_UINT32)(((x) - 1) * 0x04)))
#define TSUNAGARI_HINDO_DATA(h) ((NJ_INT16)(0x3F & *(h)))
#define TSUNAGARI_YOSOKU_ON(h) ((NJ_UINT8)(0x40 & *(h)))
#define TSUNAGARI_HENKAN_ON(h) ((NJ_UINT8)(0x80 & *(h)))
#define YOMI_IDX_DATA_ADDR(h,x) (((h) + (NJ_UINT32)(((x) - 1) * ((HASH_INDEX_CNT + 1) * 4))))


#define EXT_AREA_CHECK_DATA_TOP_ADDR(h) ((NJ_UINT8*)(NJ_INT32_READ((h) + 0x04)))
#define EXT_AREA_TOP_ADDR(h, dich)                                     \
    ((NJ_UINT8*)((h) + NJ_DIC_EXT_HEAD_SIZE + NJ_DIC_COMMON_HEADER_SIZE \
                 + NJ_INT32_READ((dich) + NJ_DIC_POS_EXT_SIZE)))
#define EXT_AREA_SIZE(h) ((NJ_UINT32)(NJ_INT32_READ((h) + 0x14)))
#define EXT_AREA_WORD_ADDR(h,x) ((NJ_UINT8*)((h) + (NJ_UINT32)(((x)-1) * EXT_AREA_WORD_SIZE)))
#define HINDO_EXT_AREA(h) ((NJ_UINT8)(*(h)) & NJ_DIC_EXT_AREA_T_HINDO)
#define HINDO_EXT_AREA_YOSOKU(h) ((0x80 & (*(h))) != 0x00)
#define HINDO_EXT_AREA_DELETE(h) ((0x40 & (*(h))) != 0x00)
#define HINDO_EXT_AREA_OPTION(h) ((0xC0 & (*(h))))


#define ADD_LEARN_FLG(h) ((NJ_UINT32)(NJ_DIC_ADD_LEARN_FLG & NJ_INT32_READ((h) + 0x24)))
#define ADD_MAX_STRING_SIZE(h) ((NJ_UINT16)(NJ_INT32_READ((h) + 0x20)))
#define ADD_WORD_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x10)))
#define ADD_STRING_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x18)))
#define ADD_WORD_DATA_ADDR(h,x) (((h) + (NJ_UINT32)(((x) - 1) * ADD_WORD_DATA_AREA_SIZE)))
#define ADD_STRING_LEN(h) ((NJ_UINT16)(NJ_INT16_READ((h))/sizeof(NJ_CHAR)))
#define ADD_STRING_INFO(h) ((NJ_UINT16)(NJ_INT32_READ((h))))
#define ADD_STRING_DATA_OFFSET(h) ((NJ_UINT32)(NJ_INT32_READ((h) + 0x02)))


#define EXT_AREA_GET_OPTIMIZE_FLAG(h)   (*((NJ_UINT8*)((h) + 0x18)) & 0x80)
#define EXT_AREA_GET_OPTION_ADDR(h)  ((NJ_UINT8*)((h) + 0x18))

/**
 * 検索位置  設定情報
 */
#define CURRENT_INFO_SET (NJ_UINT8)(0x10)

/**
 * 候補無し時のloct.currentの値
 */
#define LOC_CURRENT_NO_ENTRY  0xffffffffU

#define HAS_MORE_ENTRY(wid)           \
    (!((/* 候補無し */(wid) == LOC_CURRENT_NO_ENTRY) || \
       (/* 初期値   */(wid) == 0)) )

#define YOMI_LEN_BYTE(h)  ((NJ_UINT8)(0x7F & *(h)))
#define HYOKI_LEN_BYTE(h) ((NJ_UINT8)(0x7F & *((h) + 0x04)))
#define HYOKI_LEN_BYTE_INFO(h) ((NJ_UINT8)(*((h) + 0x04)))

#define IS_USED_FOR_FORECAST(wid, hdtbl_addr, ext_addr, clen)           \
    (/* 頻度学習がある場合でDELETEフラグが立っていない */               \
     (((ext_addr) == NULL) || (!HINDO_EXT_AREA_DELETE(EXT_AREA_WORD_ADDR((ext_addr), (wid))))) \
     &&                                                                 \
     /* 頻度学習で予測使用ON または 各文字数に応じた予測フラグON */     \
     ((((ext_addr) != NULL) && HINDO_EXT_AREA_YOSOKU(EXT_AREA_WORD_ADDR((ext_addr), (wid)))) \
      || (((clen) == 1) && HINDO_YOSOKU_1WORD(HINDO_YOSOKU_ADDR((hdtbl_addr), (wid)))) \
      || (((clen) == 2) && HINDO_YOSOKU_2WORD(HINDO_YOSOKU_ADDR((hdtbl_addr), (wid)))) \
      || (((clen) >= 3) && HINDO_YOSOKU_3WORD(HINDO_YOSOKU_ADDR((hdtbl_addr), (wid))))) )

#define EXT_HYOKI_IDX_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h) + NJ_INT32_READ((h) + 0x10)))
#define EXT_HYOKI_IDX_AREA_CNT(h) ((NJ_INT32)(NJ_INT32_READ((h) + 0x18)))

#define YOMI_LEN_FROM_DIC_TYPE(h,x) (!(h) ? YOMI_LEN_BYTE((x)) : YOMI_LEN((x)))
#define HYOKI_LEN_FROM_DIC_TYPE(h,x)    (!(h) ? YOMI_LEN_BYTE((x)) : HYOKI_LEN((x)))
#define WORD_DATA_ADDR_FROM_DIC_TYPE(h,x,d) (!(h) ? WORD_DATA_ADDR_HIGH_COMPRESS((x),(d)) : WORD_DATA_ADDR((x),(d)))
#define HYOKI_LEN_BYTE_FROM_DIC_TYPE(h,x)   (!(h) ? YOMI_LEN_BYTE((x)) : HYOKI_LEN_BYTE((x)))
#define HYOKI_DATA_OFFSET_FROM_DIC_TYPE(h,x)    (!(h) ? YOMI_DATA_OFFSET((x)) : HYOKI_DATA_OFFSET((x)))
#define HINSI_DATA_FROM_DIC_TYPE(h,x) (!(h) ? HINSI_DATA_HIGH_COMPRESS((x)) : HINSI_DATA((x)))

#define IS_FUNFUN_OK_BY_HICOMP_TYPE(is_hicomp, handle, cursor, min, max)    \
        ((!(is_hicomp))                                                     \
        ? is_funfun_ok_high_compress((handle), (cursor), (min), (max))      \
        : is_funfun_ok((handle), (cursor), (min), (max)))

#define SEARCH_WORD_NO_BY_HICOMP_TYPE(is_hicomp, handle, area, mode, src_topno, src_bottomno        \
                            , yomi, yomi_len, startpos, check, dst_topno, dst_bottomno, word_clen)  \
        ((!(is_hicomp))                                                                             \
        ? search_word_no_high_compress((handle), (area), (mode), (src_topno), (src_bottomno)        \
            , (yomi), (yomi_len), (startpos), (check), (dst_topno), (dst_bottomno), (word_clen))    \
        : search_word_no((handle), (area), (mode), (src_topno), (src_bottomno), (yomi), (yomi_len)  \
            , (startpos), (check), (dst_topno), (dst_bottomno), (word_clen)))


/************************************************/
/*              構 造 体 宣 言                  */
/************************************************/
typedef struct {
    NJ_UINT16 stem_size;        /* STEMデータ領域文字配列長 */
    NJ_UINT16 term;             /* ターミネータ */
    NJ_UINT16 no_conv_flg;      /* 無変換フラグ */
    NJ_HINDO  hindo;            /* 頻度(インデックス化) */
    NJ_UINT16 hindo_jitu;       /* 実頻度 */
    NJ_UINT16 candidate_len;    /* 表記データ文字配列長 */
    NJ_UINT16 yomi_len;         /* 読みデータ文字配列長 */
    NJ_UINT16 fhinsi;           /* 前品詞(インデックス化) */
    NJ_UINT16 bhinsi;           /* 後品詞(インデックス化) */
    NJ_UINT16 fhinsi_jitu;      /* 実前品詞 */
    NJ_UINT16 bhinsi_jitu;      /* 実後品詞 */
} STEM_DATA_SET;

/************************************************/
/*              prototype  宣  言               */
/************************************************/
static void get_stem_word(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data, STEM_DATA_SET *stem_set, NJ_UINT8 check, NJ_UINT32 current);
static void get_stem_cand_data(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data, STEM_DATA_SET *stem_set);
static NJ_UINT16 get_stem_yomi_string(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data, NJ_CHAR *yomi, NJ_UINT16 yomi_len, NJ_UINT16 size);
static void get_jitu_hinsi_no(NJ_DIC_HANDLE handle, NJ_UINT8 *word_data, NJ_UINT16 *fhinsi_jitu, NJ_UINT16 *bhinsi_jitu);
static NJ_INT16 search_node(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 dic_idx);
static NJ_INT16 search_node_rev(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset);
static NJ_INT16 search_node_yominashi(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 hidx);
static NJ_INT16 tdic_search_data(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset);
static NJ_INT16 tdic_search_data_fore(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 dic_idx);
static NJ_INT16 tdic_search_data_rev(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset);
static NJ_INT16 tdic_search_data_yominashi(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 hidx);
static NJ_INT16 search_node_aimai(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition,
                                  NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 hidx);
static NJ_INT16 search_node_rev_aimai(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset,
                                      NJ_UINT16 hidx);
static NJ_INT16 tdic_search_data_fore_aimai(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition,
                                            NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 hidx);
static NJ_INT16 tdic_search_data_rev_aimai(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition,
                                           NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 hidx);
static NJ_INT16 search_hash_idx(NJ_DIC_HANDLE hdl, NJ_CHAR *yomi, NJ_INT16 ylen, NJ_UINT32 *topno, NJ_UINT32 *bottomno);
static NJ_INT16 search_word_no(NJ_DIC_HANDLE hdl, NJ_VOID *ext_area, NJ_UINT8 mode,
                               NJ_UINT32 src_topno, NJ_UINT32 src_bottomno,
                               NJ_CHAR *yomi, NJ_UINT16 yomilen,
                               NJ_INT16 startpos, NJ_INT16 check,
                               NJ_UINT32 *dst_topno, NJ_UINT32 *dst_bottomno, NJ_INT32 *word_len);
static NJ_INT16 search_word_no_high_compress(NJ_DIC_HANDLE hdl, NJ_VOID *ext_area, NJ_UINT8 mode,
                               NJ_UINT32 src_topno, NJ_UINT32 src_bottomno,
                               NJ_CHAR *yomi, NJ_UINT16 yomilen,
                               NJ_INT16 startpos, NJ_INT16 check,
                               NJ_UINT32 *dst_topno, NJ_UINT32 *dst_bottomno, NJ_INT32 *word_len);
static NJ_INT16 search_rev_idx(NJ_SEARCH_LOCATION_SET *loctset, NJ_SEARCH_CONDITION *condition, 
                               NJ_CHAR  *hyoki, NJ_UINT16 hyokilen, 
                               NJ_UINT32 hyoki_top, NJ_UINT32 hyoki_bottom, 
                               NJ_UINT16 startpos, NJ_UINT32 *topno, NJ_UINT32 *bottomno);
static NJ_INT32 tdic_get_next_data(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset,
                                   NJ_SEARCH_CACHE *psrhCache, NJ_UINT16 abIdx, NJ_UINT16 yomi_len,
                                   NJ_UINT16 dic_idx, NJ_UINT8 mode);
static NJ_INT32 tdic_get_next_data_rev(NJ_SEARCH_LOCATION_SET *loctset,
                                       NJ_SEARCH_CACHE *psrhCache,
                                       NJ_UINT16 abIdx);
static NJ_INT32 tdic_get_next_data_fore_ext(NJ_CLASS *iwnn,
                                            NJ_SEARCH_CONDITION *condition,
                                            NJ_SEARCH_LOCATION_SET *loctset,
                                            NJ_SEARCH_CACHE *psrhCache,
                                            NJ_UINT16 abIdx);
static NJ_INT32 tdic_get_word_freq(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset,
                                   NJ_SEARCH_CACHE *psrhCache, NJ_UINT16 abIdx, NJ_UINT16 dic_idx);
static NJ_INT32 tdic_get_word_freq_rev(NJ_SEARCH_LOCATION_SET *loctset,
                                       NJ_SEARCH_CACHE *psrhCache, NJ_UINT16 abIdx);
static NJ_INT32 tdic_get_word_freq_fore_ext(NJ_SEARCH_LOCATION_SET *loctset,
                                            NJ_SEARCH_CACHE *psrhCache, NJ_UINT16 abIdx);
static NJ_INT32 get_tsunagari_hindo(NJ_UINT32 top, NJ_UINT32 bottom, NJ_UINT8* handle,
                                    NJ_UINT8* hdtbl_tmp, NJ_UINT32 word_no);
static NJ_INT16 search_connect_word(NJ_DIC_HANDLE handle,
                                    NJ_UINT32 wordno, NJ_CHAR *yomi,
                                    NJ_UINT16 ylen,
                                    NJ_SEARCH_LOCATION_SET *loctset);
static NJ_INT32 get_hindo(NJ_CLASS *iwnn, NJ_UINT32 current, NJ_INT16 base_hindo,
                          NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8  *chk_flg, NJ_UINT16 dic_idx );
static NJ_UINT32 get_attr(NJ_DIC_HANDLE handle, NJ_UINT32 current);
static NJ_UINT8 get_candidate_string(NJ_DIC_HANDLE handle, NJ_UINT8 *word, NJ_UINT8 *dic_str_area, NJ_CHAR* str);
static NJ_UINT8 is_funfun_ok(NJ_DIC_HANDLE handle, NJ_UINT32 word_id, NJ_UINT8 clen_min, NJ_UINT8 clen_max);
static NJ_UINT8 is_funfun_ok_high_compress(NJ_DIC_HANDLE handle, NJ_UINT32 word_id, NJ_UINT8 clen_min, NJ_UINT8 clen_max);
static NJ_UINT8 is_funfun_ok_ext(NJ_DIC_HANDLE handle, NJ_UINT32 word_id, NJ_UINT8 clen_min, NJ_UINT8 clen_max);
static NJ_INT16 convert_str_real_to_virtual(NJ_DIC_HANDLE handle, NJ_CHAR* real_yomi, NJ_UINT16 real_yomi_len, NJ_UINT8* conv_yomi, NJ_UINT16 conv_yomi_size);
static NJ_INT16 convert_str_virtual_to_real(NJ_DIC_HANDLE handle, NJ_UINT8* v_yomi, NJ_UINT16 v_yomi_len, NJ_CHAR* conv_yomi, NJ_UINT16 conv_yomi_size);
static NJ_INT16 is_tdic_filtering(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset);

/**
 * 表記文字列の取得
 *
 * @attention 出力バッファサイズチェックを行わないので、呼び出し側で十分確保すること。
 *
 * @param[in]   word            辞書単語情報の先頭ポインタ
 * @param[in]   dic_str_area    辞書読み・表記データエリア
 * @param[out]  str             表記文字列出力先
 *
 * @return 表記文字列長
 */
static NJ_UINT8 get_candidate_string(NJ_DIC_HANDLE handle, NJ_UINT8 *word, NJ_UINT8 *dic_str_area, NJ_CHAR* str) {
    NJ_UINT32 i;
    NJ_UINT8 *ptr;
    NJ_CHAR yomi_buff[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_UINT8 len, conv_len;
    NJ_UINT8 is_hicomp = 1;

    if (NJ_GET_DIC_TYPE(handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /* 表記文字列 */
    len = HYOKI_LEN_FROM_DIC_TYPE(is_hicomp, word);
    if (len == 0) {

        /* 表記文字列長が0の時 */
        ptr = dic_str_area + YOMI_DATA_OFFSET(word);
        len = YOMI_LEN(word);
        if (HYOKI_INFO(word) == 0) {
            /* ひらがな(読み＝表記)設定 */
            for (i = len; i > 0; i--) {
                NJ_CHAR_COPY(str, ptr);
                ptr += sizeof(NJ_CHAR);
                str++;
            }
            *str = NJ_CHAR_NUL;
        } else {
            for (i = 0; i < len; i++) {
                NJ_CHAR_COPY(yomi_buff + i, ptr);
                ptr += sizeof(NJ_CHAR);
            }
            yomi_buff[len] = NJ_CHAR_NUL;
            nje_convert_hira_to_kata(yomi_buff, str, len);
            str[len] = NJ_CHAR_NUL;
            }
    } else {
        /* 表記文字列長が>0の時は、辞書から表記文字列データを取得 */
        if (!is_hicomp) {
            ptr = dic_str_area + YOMI_DATA_OFFSET(word);

            /* 仮想文字コード(実文字コードに変換)設定 */
            conv_len = (NJ_UINT8)convert_str_virtual_to_real(handle, ptr, len, str, ((NJ_MAX_RESULT_LEN + NJ_TERM_LEN) * sizeof(NJ_CHAR)));
            len = conv_len;
            *(str + len) = NJ_CHAR_NUL;
        } else {
            ptr = dic_str_area + HYOKI_DATA_OFFSET(word);

            for (i = len; i > 0; i--) {
                NJ_CHAR_COPY(str, ptr);
                ptr += sizeof(NJ_CHAR);
                str++;
            }
            *str = NJ_CHAR_NUL;
        }
    }
    return len;
}


/**
 * FunFun入力高速化用ロジック：文字数制限条件を満たすかどうか判断
 *
 * @param[in]  handle  辞書ハンドル
 * @param[in]  word_id 単語番号
 * @param[in]  clen_min 最小文字数制限
 * @param[in]  clen_max 最大文字数制限
 *
 * @retval 1 読み文字数が文字数制限範囲内
 * @retval 0 読み文字数が文字数制限範囲外
 */
static NJ_UINT8 is_funfun_ok(NJ_DIC_HANDLE handle, NJ_UINT32 word_id, NJ_UINT8 clen_min, NJ_UINT8 clen_max) {
    NJ_UINT8 *word_data;
    NJ_UINT8 *yomi_ptr;
    NJ_UINT8 yomi_len;
    NJ_UINT8 yomi_clen;
    NJ_UINT8 char_len;
    NJ_UINT8 i;


    if ((clen_min == 0) &&
        (clen_max == NJ_MAX_LEN)) {
        /*
         * maxがNJ_MAX_LENで、
         * minが0なら必ず条件を満たす。
         */
        return 1;
    }

    /* 単語データのアドレス */
    word_data = WORD_DATA_ADDR(WORD_TOP_ADDR(handle), word_id); 
    /* 読み文字配列長 */
    yomi_len  = YOMI_LEN(word_data);

    if (clen_min > yomi_len) {
        return 0;
    }

    /* 高速化を図るため、最小文字数制限により処理の切替を行う。*/
    if (clen_min >= 4) {
        if ((clen_min * NJ_MAX_CHAR_LEN) <= yomi_len) {
            /* 読み文字配列長 * NJ_MAX_CHAR_LEN (SJIS/UTF16は最大2要素で１文字)よりも
             * yomi_lenが大きい場合、必ずOK。
             */
            return 1;
        }

        /* 読み文字列の先頭ポインタ */
        yomi_ptr  = YOMI_HYOKI_TOP_ADDR(handle) + YOMI_DATA_OFFSET(word_data);

        /* 単語の読み文字数と制限文字数を比較する */
        yomi_clen = 0;
        i = 0;
        while (i < yomi_len) {
            yomi_clen++;
            if (yomi_clen >= clen_min) {
                /* min を超えたら即OK*/
                return 1;
            }
            char_len = NJ_CHAR_LEN(yomi_ptr);
            yomi_ptr += (char_len * sizeof(NJ_CHAR));
            i += char_len;
        }
    } else {
        if (((clen_min * NJ_MAX_CHAR_LEN) <= yomi_len) &&
             (clen_max >= yomi_len)) {
            /* 
             * 最小文字数制限 * NJ_MAX_CHAR_LEN (SJIS/UTF16は最大2要素で１文字)よりもyomi_lenが大きく、
             * 最大文字数制限より、yomi_lenが小さい場合、必ずOK。
             */
            return 1;
        }
        if ((clen_max * NJ_MAX_CHAR_LEN) < yomi_len) {
            /* 最大文字数制限 * NJ_MAX_CHAR_LEN (SJIS/UTF16は最大2要素で１文字)よりも
             * yomi_lenが大きい場合、必ずNG。
             */
            return 0;
        }

        /* 読み文字列の先頭ポインタ */
        yomi_ptr  = YOMI_HYOKI_TOP_ADDR(handle) + YOMI_DATA_OFFSET(word_data);


        /* 最小文字数制限の判定処理 */
        yomi_clen = 0;
        i = 0;
        while (i < yomi_len) {
            yomi_clen++;
            if (yomi_clen >= clen_min) {
                if (clen_max >= yomi_len) {
                    /* 
                     * 最小文字数制限を算出中の文字数(yomi_clen)が越え、
                     * 最大文字数制限より、yomi_lenが小さい場合、OKとする
                     */
                    return 1;
                }
                char_len = NJ_CHAR_LEN(yomi_ptr);
                yomi_ptr += (char_len * sizeof(NJ_CHAR));
                i += char_len;
                break;
            }
            char_len = NJ_CHAR_LEN(yomi_ptr);
            yomi_ptr += (char_len * sizeof(NJ_CHAR));
            i += char_len;
        }

        /* 最大文字数制限の判定処理
         * 単語の文字数を最後まで算出する。 */
        while (i < yomi_len) {
            yomi_clen++;
            char_len = NJ_CHAR_LEN(yomi_ptr);
            yomi_ptr += (char_len * sizeof(NJ_CHAR));
            i += char_len;
        }

        if (clen_max >= yomi_clen) {
            /* 最大文字数制限以下であれば、OKとする。*/
            return 1;
        }
    }

    return 0;
}


/**
 * FunFun入力高速化用ロジック：文字数制限条件を満たすかどうか判断(高圧縮タイプ専用)
 *
 * @param[in]  handle  辞書ハンドル
 * @param[in]  word_id 単語番号
 * @param[in]  clen_min 最小文字数制限
 * @param[in]  clen_max 最大文字数制限
 *
 * @retval 1 読み文字数が文字数制限範囲内
 * @retval 0 読み文字数が文字数制限範囲外
 */
static NJ_UINT8 is_funfun_ok_high_compress(NJ_DIC_HANDLE handle, NJ_UINT32 word_id, NJ_UINT8 clen_min, NJ_UINT8 clen_max) {
    NJ_UINT8 *word_data;
    NJ_UINT8 yomi_len;


    if ((clen_min == 0) &&
        (clen_max == NJ_MAX_LEN)) {
        /*
         * maxがNJ_MAX_LENで、
         * minが0なら必ず条件を満たす。
         */
        return 1;
    }

    /* 単語データのアドレス */
    word_data = WORD_DATA_ADDR_HIGH_COMPRESS(WORD_TOP_ADDR(handle), word_id); 
    /* 読み文字配列長 */
    yomi_len  = YOMI_LEN_BYTE(word_data);

    if ((yomi_len >= clen_min) &&
        (yomi_len <= clen_max)) {
        return 1;
    }

    return 0;
}


/**
 * FunFun入力高速化用ロジック(拡張入力用)：文字数制限条件を満たすかどうか判断
 *
 * @param[in]  handle  辞書ハンドル
 * @param[in]  word_id 単語番号
 * @param[in]  clen_min 最小文字数制限
 * @param[in]  clen_max 最大文字数制限
 *
 * @retval 1 候補文字数が文字数制限範囲内
 * @retval 0 候補文字数が文字数制限範囲外
 */
static NJ_UINT8 is_funfun_ok_ext(NJ_DIC_HANDLE handle, NJ_UINT32 word_id, NJ_UINT8 clen_min, NJ_UINT8 clen_max) {
    NJ_UINT8 *word_data;
    NJ_UINT8 *hyoki_ptr;
    NJ_UINT8 hyoki_len;
    NJ_UINT8 hyoki_clen;
    NJ_UINT8 char_len;
    NJ_UINT8 i;


    if ((clen_min == 0) &&
        (clen_max == NJ_MAX_LEN)) {
        /*
         * maxがNJ_MAX_LENで、
         * minが0なら必ず条件を満たす。
         */
        return 1;
    }

    /* 単語データのアドレス */
    word_data = WORD_DATA_ADDR(WORD_TOP_ADDR(handle), word_id);
    /* 候補文字配列長 */
    hyoki_len = HYOKI_LEN(word_data);
    /* 候補文字列の先頭ポインタ */
    hyoki_ptr = YOMI_HYOKI_TOP_ADDR(handle) + HYOKI_DATA_OFFSET(word_data);

    /* 候補文字配列長が0の場合 */
    if (hyoki_len == 0) {
        /* 読み文字配列長を取得する */
        hyoki_len = YOMI_LEN(word_data);
        /* 読み文字列の先頭ポインタ */
        hyoki_ptr = YOMI_HYOKI_TOP_ADDR(handle) + YOMI_DATA_OFFSET(word_data);
    }

    if (clen_min > hyoki_len) {
        return 0;
    }

    /* 高速化を図るため、最小文字数制限により処理の切替を行う。*/
    if (clen_min >= 4) {
        if ((clen_min * NJ_MAX_CHAR_LEN) <= hyoki_len) {
            /* 読み文字配列長 * NJ_MAX_CHAR_LEN (SJIS/UTF16は最大2要素で１文字)よりも
             * hyoki_lenが大きい場合、必ずOK。
             */
            return 1;
        }

        /* 単語の表記文字数と制限文字数を比較する */
        hyoki_clen = 0;
        i = 0;
        while (i < hyoki_len) {
            hyoki_clen++;
            if (hyoki_clen >= clen_min) {
                /* min を超えたら即OK*/
                return 1;
            }
            char_len = NJ_CHAR_LEN(hyoki_ptr);
            hyoki_ptr += (char_len * sizeof(NJ_CHAR));
            i += char_len;
        }
    } else {
        if (((clen_min * NJ_MAX_CHAR_LEN) <= hyoki_len) &&
             (clen_max >= hyoki_len)) {
            /* 
             * 最小文字数制限 * NJ_MAX_CHAR_LEN (SJIS/UTF16は最大2要素で１文字)よりもhyoki_lenが大きく、
             * 最大文字数制限より、hyoki_lenが小さい場合、必ずOK。
             */
            return 1;
        }
        if ((clen_max * NJ_MAX_CHAR_LEN) < hyoki_len) {
            /* 最大文字数制限 * NJ_MAX_CHAR_LEN (SJIS/UTF16は最大2要素で１文字)よりも
             * hyoki_lenが大きい場合、必ずNG。
             */
            return 0;
        }


        /* 最小文字数制限の判定処理 */
        hyoki_clen = 0;
        i = 0;
        while (i < hyoki_len) {
            hyoki_clen++;
            if (hyoki_clen >= clen_min) {
                if (clen_max >= hyoki_len) {
                    /* 
                     * 最小文字数制限を算出中の文字数(hyoki_clen)が越え、
                     * 最大文字数制限より、hyoki_lenが小さい場合、OKとする
                     */
                    return 1;
                }
                char_len = NJ_CHAR_LEN(hyoki_ptr);
                hyoki_ptr += (char_len * sizeof(NJ_CHAR));
                i += char_len;
                break;
            }
            char_len = NJ_CHAR_LEN(hyoki_ptr);
            hyoki_ptr += (char_len * sizeof(NJ_CHAR));
            i += char_len;
        }

        /* 最大文字数制限の判定処理
         * 単語の文字数を最後まで算出する。 */
        while (i < hyoki_len) {
            hyoki_clen++;
            char_len = NJ_CHAR_LEN(hyoki_ptr);
            hyoki_ptr += (char_len * sizeof(NJ_CHAR));
            i += char_len;
        }

        if (clen_max >= hyoki_clen) {
            /* 最大文字数制限以下であれば、OKとする。*/
            return 1;
        }
    }

    return 0; /*NCH_FB*/
}


/**
 *      統合辞書データ領域の単語情報を取得
 *
 * @param[in]        hdl : 辞書ハンドル
 * @param[in]  stem_data : STEMデータ領域
 * @param[out]  stem_set : STEMデータ設定
 * @param[in]      check : 検索方法
 * @param[in]    current : カレント単語番号
 *
 * @return                 なし
 */
static void get_stem_word(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data, STEM_DATA_SET *stem_set, NJ_UINT8 check, NJ_UINT32 current)
{
    NJ_UINT16 data;
    NJ_UINT16 len;
    NJ_UINT8 fb, bb;
    NJ_UINT8 *wkc;
    NJ_UINT8 *htbl_top, *htbl_tmp;
    NJ_UINT8 *yhtbl_top;
    NJ_CHAR  tmp_buf[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_UINT8 is_hicomp = 1;


    /* 頻度データ領域の先頭アドレスを取得 */
    htbl_top = HINDO_TOP_ADDR(hdl);

    if (NJ_GET_DIC_TYPE(hdl) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    switch (check) {
        /* 正引き完全一致, 正引き前方一致検索の時 */
    case NJ_CUR_OP_COMP:
        /* 正引き完全一致 */
        htbl_tmp = HINDO_HENKAN_ADDR(htbl_top, current);
        stem_set->hindo_jitu = HINDO_DATA(htbl_tmp);
        break;
    case NJ_CUR_OP_FORE:
        /* 正引き前方一致 */
        htbl_tmp = HINDO_YOSOKU_ADDR(htbl_top, current);
        stem_set->hindo_jitu = HINDO_DATA(htbl_tmp);
        break;
    default:
        break;
    }

    data = HINSI_DATA_FROM_DIC_TYPE(is_hicomp, stem_data);
    if (BIT_FHINSI(hdl)) {
        /* 前品詞 設定 */
        stem_set->fhinsi = GET_BITFIELD_16(data, 0, BIT_FHINSI(hdl));
    } else {
        stem_set->fhinsi = 0; /*NCH*/
    }

    /* 品詞番号変換用テーブルの該当位置 */
    fb = FHINSI_NO_BYTE(hdl);
    wkc = (HINSI_NO_AREA_TOP_ADDR(hdl) + (NJ_UINT32)(fb * stem_set->fhinsi));

    /* 実品詞への変換：品詞番号設定 */
    if (fb == 2) {
        stem_set->fhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(wkc)); /*NCH_FB*/
    } else {
        stem_set->fhinsi_jitu = (NJ_UINT16)*wkc;
    }

    if (BIT_BHINSI(hdl)) {
        /* 後品詞 設定 */
        stem_set->bhinsi = GET_BITFIELD_16(data, BIT_FHINSI(hdl), BIT_BHINSI(hdl));
    } else {
        stem_set->bhinsi = 0; /*NCH*/
    }
    /* 実品詞への変換：品詞番号変換用テーブルの該当位置 */
    bb = BHINSI_NO_BYTE(hdl);
    wkc = (HINSI_NO_AREA_TOP_ADDR(hdl)
           + (NJ_UINT32)((fb * (FHINSI_NO_CNT(hdl))) + (bb * stem_set->bhinsi)));
    /* 品詞番号設定 */
    if (bb == 2) {
        stem_set->bhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(wkc));
    } else {
        stem_set->bhinsi_jitu = (NJ_UINT16)*wkc;
    }

    /* 表記長／読み文字列長を取得 */
    if (!is_hicomp) {
        yhtbl_top = YOMI_HYOKI_TOP_ADDR(hdl);
        wkc = yhtbl_top + YOMI_DATA_OFFSET(stem_data);
        len = convert_str_virtual_to_real(hdl, wkc, YOMI_LEN_BYTE(stem_data), tmp_buf, sizeof(tmp_buf));

        stem_set->candidate_len = len;
        stem_set->yomi_len = len;
    } else {
        stem_set->candidate_len = (NJ_UINT16)HYOKI_LEN(stem_data);
        stem_set->yomi_len = (NJ_UINT16)YOMI_LEN(stem_data);
    }

}


/**
 *      統合辞書データ領域の表記文字列情報を取得
 *
 * @param[in]  stem_data : STEMデータ領域
 * @param[out]  stem_set : STEMデータ設定
 *
 * @return                 なし
 */
static void get_stem_cand_data(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data, STEM_DATA_SET *stem_set) {
    NJ_UINT8 *wkc;
    NJ_UINT8 *yhtbl_top;
    NJ_UINT16 len;
    NJ_CHAR  tmp_buf[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_UINT8 is_hicomp = 1;

    if (NJ_GET_DIC_TYPE(hdl) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /* 表記長 設定 */
    if (!is_hicomp) {
        yhtbl_top = YOMI_HYOKI_TOP_ADDR(hdl);
        wkc = yhtbl_top + YOMI_DATA_OFFSET(stem_data);
        len = convert_str_virtual_to_real(hdl, wkc, YOMI_LEN_BYTE(stem_data), tmp_buf, sizeof(tmp_buf));

        /* 表記長取得 */
        stem_set->candidate_len = len;
        /* STEM領域長 設定 */
        stem_set->stem_size = WORD_DATA_HIGH_COMPRESS_AREA_SIZE;
    } else {
        /* 表記長取得 */
        stem_set->candidate_len = (NJ_UINT16)HYOKI_LEN(stem_data);

        /* STEM領域長 設定 */
        stem_set->stem_size = WORD_DATA_AREA_SIZE;
    }
}


/**
 *      読み・表記データ領域の読み文字列取得
 *
 * @param[in]        hdl : 辞書ハンドル
 * @param[in]  stem_data : STEMデータ領域
 * @param[out]      yomi : 読み文字列
 * @param[in]   yomi_len : 読みデータ文字配列長
 * @param[in]       size : バッファサイズ
 *
 * @return                 読み文字配列長（ターミネータを含まない）
 *                         ただし、ターミネータを含む読み文字配列長がバッファサイズを超える場合
 *                         読み文字列は取得せず、上位関数にエラーとしてトラップされる値を返す。
 */
static NJ_UINT16 get_stem_yomi_string(NJ_DIC_HANDLE hdl, NJ_UINT8 *stem_data, NJ_CHAR *yomi, NJ_UINT16 yomi_len, NJ_UINT16 size) {
    NJ_UINT8  *data;
    NJ_INT16 conv_len;
    NJ_UINT8 is_hicomp = 1;


    if (NJ_GET_DIC_TYPE(hdl) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /* ターミネートを含み、サイズオーバー */
    if (((yomi_len + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) {
        /* バッファサイズよりも長い文字列長を返し、上位関数にてエラーと判断してもらう */
        return size / sizeof(NJ_CHAR);
    }

    /* 読みの先頭アドレスを取得 */
    data = YOMI_HYOKI_TOP_ADDR(hdl) + YOMI_DATA_OFFSET(stem_data);

    if (!is_hicomp) {
        /* 仮想文字コードより実文字コードへ変換 */
        conv_len = convert_str_virtual_to_real(hdl, data, yomi_len, yomi, size);
        yomi_len = conv_len;
    } else {
        /* 読み文字列を取得 */
        nj_memcpy((NJ_UINT8*)yomi, data, (NJ_UINT16)(yomi_len * sizeof(NJ_CHAR)));
    }

    /* 読み文字列を NULLでターミネートする */
    *(yomi + yomi_len) = NJ_CHAR_NUL;

    /* 読み文字列長を返す */
    return yomi_len;
}


/**
 * 前品詞・後品詞の実品詞番号を取得する
 *
 * @param[in]   handle       辞書ハンドル
 * @param[in]   word_data    単語データ領域(該当単語データ)
 * @param[out]  fhinsi_jitu  格納前品詞番号
 * @param[out]  bhinsi_jitu  格納後品詞番号
 *
 * @return  なし
 */
static void get_jitu_hinsi_no(NJ_DIC_HANDLE handle, NJ_UINT8 *word_data, NJ_UINT16 *fhinsi_jitu, NJ_UINT16 *bhinsi_jitu) {
    NJ_UINT8 *wkc;
    NJ_UINT16 data;
    NJ_UINT16 fhinsi;
    NJ_UINT16 bhinsi;
    NJ_UINT8 fb, bb;
    NJ_UINT8 is_hicomp = 1;


    if (NJ_GET_DIC_TYPE(handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    data = HINSI_DATA_FROM_DIC_TYPE(is_hicomp, word_data);
    if (BIT_FHINSI(handle)) {
        /* 前品詞 設定 */
        fhinsi = GET_BITFIELD_16(data, 0, BIT_FHINSI(handle));
    } else {
        fhinsi = 0; /*NCH*/
    }
    /* 品詞番号変換用テーブルの該当位置 */
    fb = FHINSI_NO_BYTE(handle);
    wkc = (HINSI_NO_AREA_TOP_ADDR(handle) + (NJ_UINT32)(fb * fhinsi));

    /* 実品詞への変換：品詞番号設定 */
    if (fb == 2) {
        *fhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(wkc)); /*NCH_FB*/
    } else {
        *fhinsi_jitu = (NJ_UINT16)*wkc;
    }

    if (BIT_BHINSI(handle)) {
        /* 後品詞 設定 */
        bhinsi = GET_BITFIELD_16(data, BIT_FHINSI(handle), BIT_BHINSI(handle));
    } else {
        bhinsi = 0; /*NCH*/
    }
    /* 実品詞への変換：品詞番号変換用テーブルの該当位置 */
    bb = BHINSI_NO_BYTE(handle);
    wkc = (HINSI_NO_AREA_TOP_ADDR(handle)
           + (NJ_UINT32)((fb * (FHINSI_NO_CNT(handle))) + (bb * bhinsi)));

    /* 品詞番号設定 */
    if (bb == 2) {
        *bhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(wkc));
    } else {
        *bhinsi_jitu = (NJ_UINT16)*wkc;
    }
}


/**
 * 正引き完全一致,正引き前方一致検索の時、統合辞書から検索条件（読み）に該当する単語を検索し、該当する候補データの情報を取得
 *
 * @param[in,out]      iwnn : 解析情報クラス
 * @param[in]     condition : 検索条件
 * @param[in,out]   loctset : 検索位置
 * @param[in]       dic_idx : 辞書インデックス番号
 *
 * @retval                0   検索候補なし
 * @retval                1   検索候補あり
 */
static NJ_INT16 search_node(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition,
                            NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 dic_idx) {
    NJ_UINT32 word_top, word_end;                       /* 検索対象単語番号情報 */
    NJ_UINT32 yomi_top, yomi_end;                       /* 検索一致した単語データ先頭,終端番号 */
    NJ_UINT8 *hdtbl_addr, *hdtbl_tmp;                   /* 頻度データ領域操作用 */
    NJ_INT32 hindo_top;                                 /* 最大頻度位置 */
    NJ_INT32 hindo_tmp, hindo_chk;                      /* 内部頻度情報格納用 */
    NJ_UINT32 cnt;                                      /* ループカウンター */
    NJ_INT32 word_cnt = 0;                              /* 検索対象文字列の文字数 */
    NJ_INT32 word_cnt_funfun;
    NJ_INT32 hindo_chk_flg = 0;                          /* 頻度確認用フラグ */
    NJ_INT16 ret = 0;                                   /* 関数戻り値 */
    NJ_INT16 ylen = condition->ylen;

    NJ_INT16 ss_rtn = 0;                                /* 関数戻り値 */
    NJ_SEARCH_LOCATION_SET *loctset_tmp = loctset;      /* つながり情報取得用 */
    NJ_UINT8 relation_chk_flg = 0;                      /* 絞込検索確認用フラグ */
    NJ_UINT8 *ext_top;
    NJ_UINT8 no_used_flg;
    NJ_UINT8 is_hicomp = 1;
    NJ_SEARCH_LOCATION_SET tmp_loctset;


    if (NJ_GET_DIC_TYPE(loctset->loct.handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /* 読みがある場合は、ハッシュインデックスから検索対象範囲を決定する */
    if (ylen > 0) {

        /* 読み文字列インデックステーブルから、
         * 対象読み文字列の先頭文字を検索する。
         */
        ret = search_hash_idx(loctset->loct.handle, condition->yomi, condition->ylen, &word_top, &word_end);

        /* 検索対象読み文字列が読み文字列インデックステーブルに存在しない場合 */
        if (ret < 0) {
            /**
             * 指定された文字コードが読み文字列インデックステーブルに無い
             *  = 指定された辞書では、その読み文字列が使われていないとする。
             */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;        /* 検索候補無し */
        }

        if ((condition->operation == NJ_CUR_OP_FORE) && 
            (iwnn->previous_selection.count != 0) && 
            ((iwnn->environment.option.mode & NJ_RELATION_ON) != 0)) {

            /* 前確定情報につながるデータがあるか確認する */
            ss_rtn = search_node_yominashi(iwnn, condition, loctset_tmp, dic_idx);
            if (ss_rtn > 0) {
                /* 前確定情報をセット */
                iwnn->prev_search_range[dic_idx][NJ_SEARCH_RANGE_TOP] = loctset_tmp->loct.top;
                iwnn->prev_search_range[dic_idx][NJ_SEARCH_RANGE_BOTTOM] = loctset_tmp->loct.bottom;
            } else {
                /* 前確定情報がない場合、確定情報をクリア */
                iwnn->prev_search_range[dic_idx][NJ_SEARCH_RANGE_TOP] = 0;
                iwnn->prev_search_range[dic_idx][NJ_SEARCH_RANGE_BOTTOM] = 0;
            }
            loctset_tmp->loct.status = NJ_ST_SEARCH_NO_INIT;
        } else {
            /* 前確定情報がない場合、確定情報をクリア */
            iwnn->prev_search_range[dic_idx][NJ_SEARCH_RANGE_TOP] = 0;
            iwnn->prev_search_range[dic_idx][NJ_SEARCH_RANGE_BOTTOM] = 0;
        }

        /* 検索対象読み文字列から単語データ領域を検索し、
         * 該当する単語データ先頭、終端番号を取得する */
        ret = SEARCH_WORD_NO_BY_HICOMP_TYPE(is_hicomp, loctset->loct.handle, loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], condition->operation,
                                 word_top, word_end, condition->yomi, ylen, 0, (-1), &yomi_top, &yomi_end, &word_cnt);

        /* 検索対象読み文字列が単語データ領域に存在しない場合 */
        if (ret < 0) {
            /* 検索候補なしとする */
            if (condition->operation == NJ_CUR_OP_COMP) {
                if (ret == (-2)) {
                    /* 検索候補なし(前方一致有) */
                    loctset->loct.status = NJ_ST_SEARCH_END;
                } else {
                    /* 検索候補なし */
                    loctset->loct.status = NJ_ST_SEARCH_END_EXT;
                }
            } else {
                loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            }
            return 0;        /* 検索候補無し */
        }
    } else {
        /* 読みが無い場合は、全ての単語範囲を検索範囲とする */
        yomi_top = 1; /*NCH*/
        yomi_end = WORD_AREA_CNT(loctset->loct.handle); /*NCH*/
        word_cnt = 3; /*NCH*/
    }

    /* 頻度学習値利用に必要なデータを取得する */
    if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL) {
        ext_top = EXT_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], loctset->loct.handle);
    } else {
        ext_top = NULL;
    }


    /* 読み先頭と読み終端をセット */
    cnt = yomi_top;
    hdtbl_addr = HINDO_TOP_ADDR(loctset->loct.handle);

    hindo_tmp = INIT_HINDO;
    hindo_top = yomi_top;
    switch (condition->operation) {
    case NJ_CUR_OP_COMP:
        /* 正引き完全一致検索 */
        /* 読み先頭から読み終端まで検索する */
        while (cnt <= yomi_end) {
            hdtbl_tmp = HINDO_HENKAN_ADDR(hdtbl_addr, cnt);
            if (HINDO_HENKAN_ON(hdtbl_tmp)) {
                /* 最高頻度値の単語番号、頻度値を取得 */
                hindo_tmp = HINDO_DATA(hdtbl_tmp);
                hindo_top = cnt;
                hindo_chk_flg = 1;
                break;
            }
            cnt++;
        }
        break;
    case NJ_CUR_OP_FORE:
        /* 正引き前方一致検索 */
        if (condition->mode == NJ_CUR_MODE_FREQ) {
            /* 頻度順検索の時 */

            if (word_cnt >= iwnn->environment.option.char_min) {
                word_cnt_funfun = word_cnt;
            } else {
                word_cnt_funfun = iwnn->environment.option.char_min;
            }

            /* 読み先頭から読み終端まで検索する */
            while (cnt <= yomi_end) {

                /* 検索制限の解除フラグをチェックする */
                no_used_flg = 0;
                if ((condition->ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) {
                    /* 単語使用有無の確認 */
                    if (!IS_USED_FOR_FORECAST(cnt, hdtbl_addr, ext_top, word_cnt_funfun)) {
                        no_used_flg = 1;
                    }
                } else {
                    /* 辞書引きAPIからのみ通る */
                    /* 単語使用有無の確認 必ず３文字以上インデックスを使う */
                    if (!IS_USED_FOR_FORECAST(cnt, hdtbl_addr, ext_top, 3)) {
                        no_used_flg = 1;
                    }
                }
                if (no_used_flg) {
                    cnt++;
                    continue;
                }
                /* 予測総合頻度を取得する */
                hdtbl_tmp = HINDO_YOSOKU_ADDR(hdtbl_addr, cnt);
                hindo_chk = get_hindo(iwnn, cnt, HINDO_DATA(hdtbl_tmp), loctset, &relation_chk_flg, dic_idx);

                /* 最高頻度値の単語番号、頻度値を取得 */
                if (hindo_tmp < hindo_chk) {

                    /* 該当候補に対して統合辞書フィルターを実行 */
                    if (iwnn->option_data.phase3_filter != NULL) {
                        tmp_loctset = *loctset;
                        tmp_loctset.loct.current = cnt;
                        tmp_loctset.loct.attr = get_attr(loctset->loct.handle, hindo_chk);
                        tmp_loctset.loct.top = yomi_top;
                        tmp_loctset.loct.bottom = yomi_end;
                        tmp_loctset.cache_freq = CALCULATE_HINDO(hindo_chk, loctset->dic_freq.base, 
                                                                 loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
                        tmp_loctset.cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);

                        /* フィルターチェック処理を実行 */
                        if (!(is_tdic_filtering(iwnn, &tmp_loctset))) {
                            /* 候補使用なしの場合、次候補検索へ移る */
                            cnt++;
                            continue;
                        }
                    }

                    hindo_tmp = hindo_chk;
                    hindo_top = cnt;
                    /* 取得した候補が絞込検索候補であった場合 */
                    if (relation_chk_flg == 1) {
                        /* 絞込検索候補フラグの立てる */
                        iwnn->wk_relation_cand_flg[dic_idx] = 1;
                    } else {
                        /* 絞込検索候補フラグの落とす */
                        iwnn->wk_relation_cand_flg[dic_idx] = 0;
                    }
                    hindo_chk_flg = 1;
                }
                cnt++;
            }
        } else {
            /* 読み先頭から読み終端まで検索する */
            while (cnt <= yomi_end) {
                /* 読み順検索の時 */

                /* 検索制限の解除フラグをチェックする */
                no_used_flg = 0;
                if ((condition->ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) {
                    /* 単語使用有無の確認 */
                    if (!IS_USED_FOR_FORECAST(cnt, hdtbl_addr, ext_top, word_cnt)) {
                        no_used_flg = 1;
                    }
                } else {
                    /* 辞書引きAPIを使った時だけ通る */
                    /* 単語使用有無の確認 必ず３文字以上インデックスを使う */
                    if (!IS_USED_FOR_FORECAST(cnt, hdtbl_addr, ext_top, 3)) {
                        no_used_flg = 1;
                    }
                }
                if (no_used_flg) {
                    cnt++;
                    continue;
                }

                /* 予測頻度を取得する */
                hdtbl_tmp = HINDO_YOSOKU_ADDR(hdtbl_addr, cnt);
                hindo_tmp = HINDO_DATA(hdtbl_tmp);
                hindo_top = cnt;
                /* 該当候補に対して統合辞書フィルターを実行 */
                if (iwnn->option_data.phase3_filter != NULL) {
                    tmp_loctset = *loctset;
                    tmp_loctset.loct.current = cnt;
                    tmp_loctset.loct.attr = get_attr(loctset->loct.handle, hindo_top);
                    tmp_loctset.loct.top = yomi_top;
                    tmp_loctset.loct.bottom = yomi_end;
                    tmp_loctset.cache_freq = CALCULATE_HINDO(hindo_tmp, loctset->dic_freq.base, 
                                                             loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
                    tmp_loctset.cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);

                    /* フィルターチェック処理を実行 */
                    if (!(is_tdic_filtering(iwnn, &tmp_loctset))) {
                        /* 候補使用なしの場合、次候補検索へ移る */
                        cnt++;
                        continue;
                    }
                }
                hindo_chk_flg = 1;
                break;
            }
        }
        break;
    default:
        /* 逆引き完全一致検索,逆引き前方一致検索,それ以外 */
        /* ここにくることはないが、念のため入ったときの場合は
         * 検索候補なしとする */
        loctset->loct.status = NJ_ST_SEARCH_END_EXT; /*NCH*/
        return 0;        /* 検索候補無し */ /*NCH*/
    }

    if (hindo_chk_flg == 0) {
        /* 頻度値が更新されていなかった場合、検索候補なしとする */
        if (condition->operation == NJ_CUR_OP_COMP) {
            loctset->loct.status = NJ_ST_SEARCH_END;
        } else {
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
        }
        return 0;        /* 検索候補無し */
    }

    /* 検索位置、評価値を更新する */
    loctset->loct.current = hindo_top;
    loctset->loct.attr   = get_attr(loctset->loct.handle, hindo_top);
    loctset->loct.top = yomi_top;
    loctset->loct.bottom = yomi_end;
    loctset->cache_freq = CALCULATE_HINDO(hindo_tmp, loctset->dic_freq.base, 
                                          loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
    loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);

    return 1;        /* 検索候補あり */
}


/**
 * 正引き完全一致検索の時の次候補を検索する（次候補の情報は検索位置(loctset)に設定する）
 *
 * @param[in]          iwnn : 解析情報クラス
 * @param[in]     condition : 検索条件
 * @param[in,out]   loctset : 検索位置
 *
 * @retval                0   検索候補なし
 * @retval                1   検索候補あり
 */
static NJ_INT16 tdic_search_data(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset) {
    NJ_UINT8 *wtbl_addr, *hdtbl_addr;              /* データ領域先頭アドレス */
    NJ_UINT8 *data_cur, *hindo_cur;                /* カーソル位置データアドレス */
    STEM_DATA_SET stem_set;                        /* カーソル位置データ情報 */

    /* インライン展開用 */
    NJ_UINT8 fb, bb;
    NJ_UINT16 hdata;
    NJ_UINT32 cnt;
    NJ_UINT8 *wkc;
    NJ_UINT8 is_hicomp = 1;

    NJ_SEARCH_LOCATION_SET tmp_loctset;


    if (NJ_GET_DIC_TYPE(loctset->loct.handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /* 現カーソル位置単語データの先頭アドレスを取得 */
    wtbl_addr = WORD_TOP_ADDR(loctset->loct.handle);
    hdtbl_addr = HINDO_TOP_ADDR(loctset->loct.handle);
    data_cur = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, wtbl_addr, loctset->loct.current);
    hindo_cur = HINDO_HENKAN_ADDR(hdtbl_addr, loctset->loct.current);
    cnt = loctset->loct.current;

    if (GET_LOCATION_STATUS(loctset->loct.status) != NJ_ST_SEARCH_NO_INIT) {
        /**
         * 初回の検索で無いとき、現カーソル位置で完全一致候補が
         * 終了かどうか検査
         */

        if (loctset->loct.current == loctset->loct.bottom) {
            /* 検索終了の属性を設定 */
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }

        /* STEMデータ設定 */
        cnt = loctset->loct.current + 1;
        data_cur = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, wtbl_addr, cnt);
        hindo_cur = HINDO_HENKAN_ADDR(hdtbl_addr, cnt);
    }

    /* 品詞番号変換用テーブル上の１品詞のサイズ */
    fb = FHINSI_NO_BYTE(loctset->loct.handle);
    bb = BHINSI_NO_BYTE(loctset->loct.handle);

    while (cnt <= loctset->loct.bottom) {
        if (HINDO_HENKAN_ON(hindo_cur)) {
            /* 品詞情報を取得 */
            /**
             * ↓get_stem_hinsiのインライン展開 (本体)
             */
            if (condition->hinsi.fore != NULL) {
                if (BIT_FHINSI(loctset->loct.handle)) {
                    /* 前品詞 設定 */
                    hdata = HINSI_DATA_FROM_DIC_TYPE(is_hicomp, data_cur);
                    stem_set.fhinsi = GET_BITFIELD_16(hdata, 0, BIT_FHINSI(loctset->loct.handle));
                } else {
                    stem_set.fhinsi = 0; /*NCH*/
                }

                /* 品詞番号変換データ領域の該当位置 */
                wkc = (HINSI_NO_AREA_TOP_ADDR(loctset->loct.handle) + (NJ_UINT32)(fb * stem_set.fhinsi));
                /* 実品詞への変換：品詞番号設定 */
                if (fb == 2) {
                    stem_set.fhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(wkc)); /*NCH_FB*/
                } else {
                    stem_set.fhinsi_jitu = (NJ_UINT16)*wkc;
                }
            } else {
                stem_set.fhinsi = 0;
                stem_set.fhinsi_jitu = 0;
            }

            if (condition->hinsi.rear != NULL) {
                if (BIT_BHINSI(loctset->loct.handle)) {
                    /* 後品詞 設定 */
                    hdata = HINSI_DATA_FROM_DIC_TYPE(is_hicomp, data_cur);
                    stem_set.bhinsi = GET_BITFIELD_16(hdata, BIT_FHINSI(loctset->loct.handle), BIT_BHINSI(loctset->loct.handle));
                } else {
                    stem_set.bhinsi = 0; /*NCH*/
                }

                /* 品詞番号変換データ領域の該当位置 */
                wkc = (HINSI_NO_AREA_TOP_ADDR(loctset->loct.handle)
                       + (NJ_UINT32)((fb * (FHINSI_NO_CNT(loctset->loct.handle))) + (bb * stem_set.bhinsi)));
                /* 実品詞への変換：品詞番号設定 */
                if (bb == 2) {
                    stem_set.bhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(wkc));
                } else {
                    stem_set.bhinsi_jitu = (NJ_UINT16)*wkc;
                }
            } else {
                stem_set.bhinsi = 0;
                stem_set.bhinsi_jitu = 0;
            }
            /**
             * ↑get_stem_hinsiのインライン展開 (本体)
             */

            /* 前品詞が検索条件と一致するかチェック */
            if (njd_connect_test(condition, stem_set.fhinsi_jitu, stem_set.bhinsi_jitu)) {
                /* 完全一致 */
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current = cnt;
                loctset->loct.attr   = get_attr(loctset->loct.handle, cnt);
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->cache_freq = CALCULATE_HINDO(HINDO_DATA(hindo_cur), loctset->dic_freq.base, 
                                                      loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
                loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);

                /* 取得候補に対して統合辞書フィルターを実行 */
                if (iwnn->option_data.phase3_filter != NULL) {
                    /* フィルターチェック処理を実行 */
                    tmp_loctset = *loctset;
                    if (is_tdic_filtering(iwnn, &tmp_loctset)) {
                        /*
                         * フィルターチェックにて候補を使用すると判断した場合、
                         * 検索候補ありとする。
                         */
                        return 1;
                    }
                } else {
                    /* 検索候補あり */
                    return 1;
                }
            }
            if (STEM_TERMINATOR(wtbl_addr)) {
                /* 次候補無し */
                loctset->loct.status = NJ_ST_SEARCH_END; /*NCH*/
                return 0; /*NCH*/
            }
        }
        /* 次候補のアドレス取得 */
        cnt++;
        data_cur = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, wtbl_addr, cnt);
        hindo_cur = HINDO_HENKAN_ADDR(hdtbl_addr, cnt);
    }
    /* 次候補無し */
    loctset->loct.status = NJ_ST_SEARCH_END;
    return 0;
}


/**
 * 正引き前方一致検索の時の次候補を検索する（次候補の情報を検索位置(loctset)に設定する）
 *
 * @param[in,out]      iwnn : 解析情報クラス
 * @param[in]     condition : 検索条件
 * @param[in,out]   loctset : 検索位置
 * @param[in]       dic_idx : 辞書インデックス番号
 *
 * @retval                0   検索候補なし
 * @retval                1   検索候補あり
 */
static NJ_INT16 tdic_search_data_fore(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition,
                                      NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 dic_idx) {

    NJ_UINT32 bottom;
    NJ_UINT32 cursor = loctset->loct.current;
    NJ_UINT8 *hindo_cur;
    NJ_UINT8 *hdtbl_addr;
    NJ_UINT16 yomi_clen;
    NJ_UINT16 yomi_clen_funfun;
    NJ_INT32 hindo_tmp = INIT_HINDO;
    NJ_INT32 hindo_chk = INIT_HINDO;
    NJ_INT32 hindo_current = INIT_HINDO;
    NJ_UINT32 word_next = 0;
    NJ_UINT8 bottomflg = 0;

    NJ_UINT8 relation_chk_flg = 0;

    NJ_UINT8 *ext_top;

    NJ_DIC_HANDLE handle;
    NJ_UINT8 no_used_flg;
    NJ_UINT8 is_hicomp = 1;

    NJ_SEARCH_LOCATION_SET tmp_loctset;
    NJ_INT16 ret;



    /* 初回検索時は そのまま抜ける */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        loctset->loct.status = NJ_ST_SEARCH_READY;
        loctset->loct.current_info = CURRENT_INFO_SET;
        return 1;
    }

    handle = loctset->loct.handle;

    if (NJ_GET_DIC_TYPE(handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /* 頻度学習値利用に必要なデータを取得する */
    if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL) {
        ext_top = EXT_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], loctset->loct.handle);
    } else {
        ext_top = NULL;
    }

    hdtbl_addr = HINDO_TOP_ADDR(handle);

    if (condition->mode == NJ_CUR_MODE_FREQ) {
        /* 頻度順検索の場合は、次に頻度が高い単語を取得する */

        hindo_cur = HINDO_YOSOKU_ADDR(hdtbl_addr, loctset->loct.current);
        hindo_current = get_hindo(iwnn, loctset->loct.current, HINDO_DATA(hindo_cur), loctset,
                                  &relation_chk_flg, dic_idx);
        
        bottom = loctset->loct.bottom;
        if (cursor >= bottom) {
            cursor = loctset->loct.top;
        } else {
            cursor += 1;
        }

        yomi_clen = condition->yclen;
        if (yomi_clen >= iwnn->environment.option.char_min) {
            yomi_clen_funfun = yomi_clen;
        } else {
            yomi_clen_funfun = iwnn->environment.option.char_min;
        }

        /* 統合辞書データ領域の終端を超えた場合は抜ける */
        while (cursor <= bottom) {

            /* 検索制限の解除フラグをチェックする */
            no_used_flg = 0;
            if ((condition->ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) {
                /* 単語使用有無の確認 */
                if ((!IS_USED_FOR_FORECAST(cursor, hdtbl_addr, ext_top, yomi_clen_funfun)) ||
                    (!IS_FUNFUN_OK_BY_HICOMP_TYPE(is_hicomp, loctset->loct.handle, cursor, iwnn->environment.option.char_min, iwnn->environment.option.char_max))) {
                    no_used_flg = 1;
                }
            } else {
                /* 単語検索APIの時のみ通る */
                /* 単語使用有無の確認 必ず３文字以上インデックスを使う */
                if (!IS_USED_FOR_FORECAST(cursor, hdtbl_addr, ext_top, 3)) {
                    no_used_flg = 1;
                }
            }
            if (no_used_flg) {
                cursor++;
                if ((cursor > bottom) && (bottomflg == 0)) {
                    bottomflg = 1;
                    cursor = loctset->loct.top;
                    bottom = loctset->loct.current;
                }
                continue;
            }

            /* 予測総合頻度を取得する */
            hindo_cur = HINDO_YOSOKU_ADDR(hdtbl_addr, cursor);
            hindo_tmp = get_hindo(iwnn, cursor, HINDO_DATA(hindo_cur), loctset, &relation_chk_flg, dic_idx);

            if ((cursor > loctset->loct.current) && (hindo_tmp == hindo_current)) {
                /* 検索中頻度と等しく、且つ検索候補がカレント位置以降の場合,
                 * その検索候補位置を返す */
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->loct.current = cursor;
                loctset->loct.attr   = get_attr(loctset->loct.handle, cursor);
                /* 取得した候補が絞込検索候補であった場合 */
                if (relation_chk_flg == 1) {
                    /* 絞込検索候補フラグの立てる */
                    iwnn->wk_relation_cand_flg[dic_idx] = 1;
                } else {
                    /* 絞込検索候補フラグの落とす */
                    iwnn->wk_relation_cand_flg[dic_idx] = 0;
                }

                /* 取得候補に対して統合辞書フィルターを実行 */
                if (iwnn->option_data.phase3_filter != NULL) {
                    /* フィルターチェック処理を実行 */
                    tmp_loctset = *loctset;
                    if (is_tdic_filtering(iwnn, &tmp_loctset)) {
                        /*
                         * フィルターチェックにて候補を使用すると判断した場合、
                         * 検索候補ありとする。
                         */
                        return 1;
                    }
                } else {
                    /* 検索候補あり */
                    return 1;
                }

            } else if (hindo_tmp < hindo_current) {
                /* カレントより頻度が低く、その中で最高頻度のものを取得 */
                /* 検索中の最高頻度と同じで、読みが先のものを取得 */
                if ((hindo_chk < hindo_tmp) || ((cursor < word_next) && (hindo_chk == hindo_tmp))) {

                    /* 該当候補に対して統合辞書フィルターを実行 */
                    if (iwnn->option_data.phase3_filter != NULL) {
                        /* フィルターチェック処理を実行 */
                        tmp_loctset = *loctset;
                        tmp_loctset.loct.status = NJ_ST_SEARCH_READY;
                        tmp_loctset.loct.current_info = CURRENT_INFO_SET;
                        tmp_loctset.loct.current = cursor;
                        tmp_loctset.loct.attr   = get_attr(loctset->loct.handle, cursor);
                        tmp_loctset.loct.current = cursor;
                        ret = is_tdic_filtering(iwnn, &tmp_loctset);
                    } else {
                        /* フィルターを使用しない場合、無条件に候補使用ありを設定 */
                        ret = 1;
                    }
                    if (ret) {
                        hindo_chk = hindo_tmp;
                        word_next = cursor;
                        /* 取得した候補が絞込検索候補であった場合 */
                        if (relation_chk_flg == 1) {
                            /* 絞込検索候補フラグの立てる */
                            iwnn->wk_relation_cand_flg[dic_idx] = 1;
                        } else {
                            /* 絞込検索候補フラグの落とす */
                            iwnn->wk_relation_cand_flg[dic_idx] = 0;
                        }
                    }
                }
            } else {
            }
            cursor++;
            if ((cursor > bottom) && (bottomflg == 0)) {
                bottomflg = 1;
                cursor = loctset->loct.top;
                bottom = loctset->loct.current;
            }
        }

        /* 検索中の頻度と一致しない場合,
         * 最大頻度を持つ候補の頻度値,単語データ番号を返す。*/
        if (word_next > 0) {
            loctset->cache_freq = CALCULATE_HINDO(hindo_chk, loctset->dic_freq.base,
                                                  loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
            loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
            loctset->loct.status = NJ_ST_SEARCH_READY;
            loctset->loct.current_info = CURRENT_INFO_SET;
            loctset->loct.current = word_next;
            loctset->loct.attr   = get_attr(loctset->loct.handle, word_next);
            /* 取得した候補が絞込検索候補であった場合 */
            if (relation_chk_flg == 1) {
                /* 絞込検索候補フラグの立てる */
                iwnn->wk_relation_cand_flg[dic_idx] = 1;
            } else {
                /* 絞込検索候補フラグの落とす */
                iwnn->wk_relation_cand_flg[dic_idx] = 0;
            }
            return 1;
        }

    } else {
        /* 読み順検索の次のデータを取り出す。*/
        /* 検索条件の文字数 */
        yomi_clen = condition->yclen;
        /* 次のデータ設定 */
        cursor = loctset->loct.current + 1;

        while (cursor <= loctset->loct.bottom) {
            /* 検索制限の解除フラグをチェックする */
            if ((condition->ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) {
                /* 単語使用有無の確認 */
                if (IS_USED_FOR_FORECAST(cursor, hdtbl_addr, ext_top, yomi_clen)) {
                    break;
                }
            } else {
                /* 単語検索APIの時のみ通る */
                /* 単語使用有無の確認 必ず３文字以上インデックスを使う */
                if (IS_USED_FOR_FORECAST(cursor, hdtbl_addr, ext_top, 3)) {
                    break;
                }
            }
            cursor++; /*NCH_DEF*/
        }

        if (cursor <= loctset->loct.bottom) {
            /* 検索領域の終端までに検索一致した時 */

            /* 予測頻度を取得する */
            hindo_cur = HINDO_YOSOKU_ADDR(hdtbl_addr, cursor);
            loctset->cache_freq = CALCULATE_HINDO(HINDO_DATA(hindo_cur),
                                                  loctset->dic_freq.base, loctset->dic_freq.high,
                                                  FUSION_DIC_FREQ_DIV);
            loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
            loctset->loct.status = NJ_ST_SEARCH_READY;
            loctset->loct.current_info = CURRENT_INFO_SET;
            loctset->loct.current = cursor;
            loctset->loct.attr   = get_attr(loctset->loct.handle, cursor);
            return 1;
        }
    }
    /* 検索終了の属性を設定 */
    loctset->loct.status = NJ_ST_SEARCH_END;
    return 0;
}


/************************************************/
/* Global関数                                   */
/************************************************/

/**
 *      統合辞書アダプタ  単語検索
 *
 * @param[in,out]     iwnn : 解析情報クラス
 * @param[in]          con : 検索条件
 * @param[in,out]  loctset : 検索位置
 * @param[in]     comp_flg : 統合辞書の完全一致で変換補正対象の有無<br>
 *                          （1:対象、1以外:対象としない）
 * @param[in]     dic_idx  : 辞書インデックス番号
 *
 * @retval              <0   エラー
 * @retval               0   検索候補なし
 * @retval               1   検索候補あり
 */
NJ_INT16 njd_t_search_word(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *con,
                           NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 comp_flg,
                           NJ_UINT16 dic_idx) {
    NJ_INT16 ret;

    /* 検索方法のチェック */
    switch (con->operation) {
    case NJ_CUR_OP_COMP:
        /* 正引き完全一致検索 */
        if ((con->mode != NJ_CUR_MODE_FREQ) ||
            (con->ylen <= 0) ||
            (con->ylen > NJ_GET_MAX_YLEN(loctset->loct.handle))) {
            /* 該当候補なしとする */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
            /* 検索状態：未初期化 */
            /* 指定読みを元に該当単語データを検索する */
            ret = search_node(iwnn, con, loctset, dic_idx);
            if (ret < 1) {
                return ret;
            }
        }
        ret = tdic_search_data(iwnn, con, loctset);
        if (ret < 1) {
            /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
            loctset->loct.status = NJ_ST_SEARCH_END;
        }
        /* 変換補正(変換補正対象の場合のみ) */
        if ((ret > 0) && (comp_flg == 1) && (*(con->yomi + con->ylen) != NJ_CHAR_NUL) ) {
            /* 変換補正処理を呼ぶ */
            ret = search_connect_word(loctset->loct.handle,
                                      loctset->loct.current, con->yomi, con->ylen,
                                      loctset);
            if (ret < 0) {
                return ret; /*NCH*/
            }
        }
        break;

    case NJ_CUR_OP_FORE:
        /* 正引き前方一致検索 */
        if ((con->mode == NJ_CUR_MODE_REGIST) ||
            (con->ylen <= 0) || 
            (con->ylen > NJ_GET_MAX_YLEN(loctset->loct.handle))) {
            /* 該当候補なしとする */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        /* キャッシュ領域がないか、または空文字列指定時 */
        /* キャッシュ検索不要モード時 */
        if ((con->ds->dic[dic_idx].srhCache == NULL) || (con->ylen == 0) ||
            ((con->ds->mode & (NJ_CACHE_MODE_VALID | NJ_CACHE_MODE_VALID_FUSION)) == 0) || 
            (con->mode != NJ_CUR_MODE_FREQ)) {
            if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
                /* 検索状態：未初期化 */
                ret = search_node(iwnn, con, loctset, dic_idx);
                if (ret < 1) {
                    return ret;
                }
            }
            ret = tdic_search_data_fore(iwnn, con, loctset, dic_idx);
        } else {
            if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
                /* 検索状態：未初期化 */
                ret = search_node_aimai(iwnn, con, loctset, dic_idx);
                if (ret == NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_NOT_ENOUGH)) {
                    /* キャッシュ溢れが発生した場合は、もう一度同じ関数を呼ぶ */
                    NJ_SET_CACHEOVER_TO_SCACHE(con->ds->dic[dic_idx].srhCache);
                    ret = search_node_aimai(iwnn, con, loctset, dic_idx);
                }
                if (ret < 1) {
                    return ret;
                }
            }
            ret = tdic_search_data_fore_aimai(iwnn, con, loctset, dic_idx);
        }
        if (ret < 1) {
            /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
            loctset->loct.status = NJ_ST_SEARCH_END;
        }
        break;

    case NJ_CUR_OP_REV:
        /* 逆引き完全一致検索 */
        if ((con->mode != NJ_CUR_MODE_FREQ) ||
            (con->ylen <= 0) ||
            (con->ylen > NJ_GET_MAX_KLEN(loctset->loct.handle))) {
            /* 該当候補なしとする */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
            /* 検索状態：未初期化 */
            /* 表記文字列インデックスを指定された候補で検索 */
            ret = search_node_rev(iwnn, con, loctset);
            if (ret < 1) {
                return ret;
            }
        }
        /* 逆引き結果を取得 */
        ret = tdic_search_data_rev(iwnn, con, loctset);
        if (ret < 1) {
            /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
            loctset->loct.status = NJ_ST_SEARCH_END;
        }
        break;

    case NJ_CUR_OP_REV_FORE:
        /* 逆引き前方一致検索 */
        if ((con->ylen <= 0) ||
            (con->ylen > NJ_GET_MAX_KLEN(loctset->loct.handle))) {
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        if ((con->ds->dic[dic_idx].srhCache == NULL) || (con->ylen == 0) ||
            ((con->ds->mode & NJ_CACHE_MODE_VALID_FUSION) == 0) || 
            (con->mode != NJ_CUR_MODE_FREQ)) {
            /* キャッシュ領域がないか、または空文字列指定時 */
            /* キャッシュ検索不要モード時                   */
            if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
                /* 検索状態：未初期化 */
                ret = search_node_rev(iwnn, con, loctset);
                if (ret < 1) {
                    return ret;
                }
            }
            /* 逆引き結果を取得 */
            /* 表記文字列インデックスを指定された候補で検索 */
            ret = tdic_search_data_rev(iwnn, con, loctset);
            if (ret < 1) {
                /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
                loctset->loct.status = NJ_ST_SEARCH_END;
            }
        } else {
            /* あいまい検索モード時 */
            if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
                ret = search_node_rev_aimai(iwnn, con, loctset, dic_idx);
                if (ret == NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_NOT_ENOUGH)) {
                    /* キャッシュ溢れが発生した場合は、もう一度同じ関数を呼ぶ */
                    NJ_SET_CACHEOVER_TO_SCACHE(con->ds->dic[dic_idx].srhCache);
                    ret = search_node_rev_aimai(iwnn, con, loctset, dic_idx);
                }
                if (ret < 1) {
                    return ret;
                }
            }
            ret = tdic_search_data_rev_aimai(iwnn, con, loctset, dic_idx);
        }
        break;

    case NJ_CUR_OP_LINK:
        /* 読みなし(つながり予測)検索 */
        /* 以下の場合、候補無しとする
         *  検索候補取得順：頻度順(0)以外
         */
        if (con->mode != NJ_CUR_MODE_FREQ) {
            /* 該当候補なしとする */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
            /* 検索状態：未初期化 */
            ret = search_node_yominashi(iwnn, con, loctset, dic_idx);
            if (ret < 1) {
                return ret;
            }
        }
        ret = tdic_search_data_yominashi(iwnn, con, loctset, dic_idx);
        if (ret < 1) {
            /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
            loctset->loct.status = NJ_ST_SEARCH_END;
        }
        break;

    case NJ_CUR_OP_COMP_EXT:
        /* 正引き完全一致拡張検索 */
        if ((con->mode != NJ_CUR_MODE_FREQ) ||
            (con->ylen <= 0) ||
            (con->ylen > NJ_GET_MAX_KLEN(loctset->loct.handle)) ||
            (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] == NULL)) {
            /* 該当候補なしとする */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
            /* 検索状態：未初期化 */
            /* 拡張領域(表記文字列インデックス)を指定された候補で検索 */
            ret = search_node_rev(iwnn, con, loctset);
            if (ret < 1) {
                return ret;
            }
        }
        /* 正引き拡張検索結果を取得(逆引き検索を使用する) */
        ret = tdic_search_data_rev(iwnn, con, loctset);
        if (ret < 1) {
            /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
            loctset->loct.status = NJ_ST_SEARCH_END;
        }
        break;

    case NJ_CUR_OP_FORE_EXT:
        /* 正引き前方一致拡張検索 */
        if ((con->mode == NJ_CUR_MODE_REGIST) ||
            (con->ylen <= 0) || 
            (con->ylen > NJ_GET_MAX_KLEN(loctset->loct.handle)) ||
            (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] == NULL)) {
            /* 該当候補なしとする */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        /* キャッシュ領域がないか、または空文字列指定時 */
        /* キャッシュ検索不要モード時 */
        if ((con->ds->dic[dic_idx].srhCache == NULL) || (con->ylen == 0) ||
            ((con->ds->mode & (NJ_CACHE_MODE_VALID | NJ_CACHE_MODE_VALID_FUSION)) == 0) || 
            (con->mode != NJ_CUR_MODE_FREQ)) {
            if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
                /* 検索状態：未初期化 */
                ret = search_node_rev(iwnn, con, loctset);
                if (ret < 1) {
                    return ret;
                }
            }
            /* 逆引き結果を取得 */
            /* 表記文字列インデックスを指定された候補で検索 */
            ret = tdic_search_data_rev(iwnn, con, loctset);
            if (ret < 1) {
                /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
                loctset->loct.status = NJ_ST_SEARCH_END;
            }
        } else {
            /* あいまい検索モード時 */
            if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
                ret = search_node_rev_aimai(iwnn, con, loctset, dic_idx);
                if (ret == NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_NOT_ENOUGH)) {
                    /* キャッシュ溢れが発生した場合は、もう一度同じ関数を呼ぶ */
                    NJ_SET_CACHEOVER_TO_SCACHE(con->ds->dic[dic_idx].srhCache);
                    ret = search_node_rev_aimai(iwnn, con, loctset, dic_idx);
                }
                if (ret < 1) {
                    return ret;
                }
            }
            ret = tdic_search_data_rev_aimai(iwnn, con, loctset, dic_idx);
        }
        if (ret < 1) {
            /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
            loctset->loct.status = NJ_ST_SEARCH_END;
        }
        break;

    default:
        /* 対応しない検索操作が指定された場合、該当候補なしとする */
        loctset->loct.status = NJ_ST_SEARCH_END_EXT; /*NCH_FB*/
        return 0; /*NCH_FB*/
    }

    return ret;
}


/**
 * 統合辞書アダプタ  単語取得（※ 読み、読み長は上位で設定する）
 *
 * @param[in]  loctset : 検索位置
 * @param[out]    word : 単語情報
 *
 * @retval          <0   エラー
 * @retval           0   正常終了
 */
NJ_INT16 njd_t_get_word(NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word) {
    NJ_UINT8 *data;
    STEM_DATA_SET stem_set;
    NJ_UINT8 check;

    NJ_UINT8 *tdtbl_addr, *tdtbl_tmp;
    NJ_UINT8 *word_addr;
    NJ_UINT32 current;
    NJ_UINT8 *hitbl_addr, *hitbl_tmp;
    NJ_UINT8 is_hicomp = 1;



    /* 検索状態：次候補なし  は、そのままリターン */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_END) {
        return 0; /*NCH_FB*/
    }

    if (NJ_GET_DIC_TYPE(loctset->loct.handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /* 単語データ領域の先頭アドレスを取得 */
    word_addr = WORD_TOP_ADDR(loctset->loct.handle);

    /* 検索方法を取得する */
    check = GET_LOCATION_OPERATION(loctset->loct.status);

    if ((check == NJ_CUR_OP_REV) ||
        (check == NJ_CUR_OP_REV_FORE)) {
        /* 逆引き完全一致、前方一致検索の場合 */

        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO] != NULL) {
            /* 拡張領域(形態素用)表記文字列インデックス領域の先頭アドレスを取得 */
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO]);
            /* 表記文字列インデックス番号から単語データ番号、頻度を取得 */
            hitbl_tmp = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
            current = HYOKI_INDEX_WORD_NO(hitbl_tmp);
            stem_set.hindo_jitu = (NJ_INT16)HINDO_DATA(hitbl_tmp);
            data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, word_addr, current);
        } else {
            /* 表記文字列インデックス領域の先頭アドレスを取得 */
            hitbl_addr = HYOKI_IDX_AREA_TOP_ADDR(loctset->loct.handle);
            /* 表記文字列インデックス番号から単語データ番号、頻度を取得 */
            hitbl_tmp = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
            current = HYOKI_INDEX_WORD_NO(hitbl_tmp);
            stem_set.hindo_jitu = (NJ_INT16)HINDO_MORPHO(hitbl_tmp);
            data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, word_addr, current);
        }

    } else if (check == NJ_CUR_OP_LINK) {
        /* つながり予測検索の場合 */

        /* つながりデータ領域の先頭アドレスを取得 */
        tdtbl_addr = TSUNAGARI_TOP_ADDR(loctset->loct.handle);
        /* つながりデータ番号から単語データ番号、頻度を取得 */
        tdtbl_tmp = TSUNAGARI_DATA_ADDR(tdtbl_addr, loctset->loct.current);
        current = TSUNAGARI_WORD_DATA_NO(tdtbl_tmp);
        stem_set.hindo_jitu = TSUNAGARI_HINDO_DATA(tdtbl_tmp);
        data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, word_addr, current);

    } else if ((check == NJ_CUR_OP_COMP_EXT) ||
               (check == NJ_CUR_OP_FORE_EXT)) {
        /* 正引き完全一致拡張, 正引き前方一致拡張検索の場合 */
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
            /* 表記文字列インデックス領域の先頭アドレスを取得 */
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT]);
            /* 表記文字列インデックス番号から単語データ番号、頻度を取得 */
            hitbl_tmp = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
            current = HYOKI_INDEX_WORD_NO(hitbl_tmp);
            stem_set.hindo_jitu = (NJ_INT16)HINDO_DATA(hitbl_tmp);
            data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, word_addr, current);
        } else {
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }
    } else {
        /* 正引き完全一致、正引き前方一致検索の場合 */
        /* 該当候補データのアドレスを取得する */
        current = loctset->loct.current;
        stem_set.hindo_jitu = 0; /* 念のため仮初期化。実値はget_stem_wordで入る。*/
        data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, word_addr, current);
    }

    /* 該当候補データの情報を取得する */
    get_stem_word(loctset->loct.handle, data, &stem_set, check, current);

    /* 読み長、表記長の格納を行う。*/
    switch (check) {
    case NJ_CUR_OP_COMP:
    case NJ_CUR_OP_FORE:
    case NJ_CUR_OP_LINK:
        /**
         * 正引き完全一致、前方一致検索、つながり検索の場合は、
         * 単語データ領域から取得した読み長、表記長をセットする
         */
        word->stem.info1 = stem_set.yomi_len;

        if (stem_set.candidate_len == 0) {
            /* 表記無し候補の場合 */
            word->stem.info2 = stem_set.yomi_len;
        } else {
            word->stem.info2 = stem_set.candidate_len;
        }
        break;

    case NJ_CUR_OP_COMP_EXT:
    case NJ_CUR_OP_FORE_EXT:
        if (stem_set.candidate_len == 0) {
            /* 表記無し候補の場合 */
            word->stem.info1 = stem_set.yomi_len;
            word->stem.info2 = stem_set.yomi_len;
        } else {
            word->stem.info1 = stem_set.candidate_len;
            word->stem.info2 = stem_set.candidate_len;
        }
        break;

    default:
        /**
         * 逆引き完全一致、前方一致検索の場合は、
         * 単語データ領域から取得した読み長、表記長を逆セットする
         */
        if (stem_set.candidate_len == 0) {
            /* 表記無し候補の場合 */
            word->stem.info1 = stem_set.yomi_len;
        } else {
            word->stem.info1 = stem_set.candidate_len;
        }

        word->stem.info2 = stem_set.yomi_len;
    }

    /* 前品詞の格納を行う */
    word->stem.info1 = WORD_SIZE(word->stem.info1);                  /* 前品詞ビットをクリア */
    word->stem.info1 |= (NJ_UINT16)(stem_set.fhinsi_jitu << 7);      /* 前品詞 */

    /* 後品詞の格納を行う */
    word->stem.info2 = WORD_SIZE(word->stem.info2);                  /* 後品詞ビットをクリア */
    word->stem.info2 |= (NJ_UINT16)(stem_set.bhinsi_jitu << 7);      /* 後品詞 */

    if ((check == NJ_CUR_OP_FORE) ||
        (check == NJ_CUR_OP_LINK)) {
        word->stem.hindo = loctset->cache_freq;                     /* 実頻度値 */
    } else {
        word->stem.hindo = CALCULATE_HINDO(stem_set.hindo_jitu, loctset->dic_freq.base, 
                                           loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);     /* 実頻度値 */
        word->stem.hindo = NORMALIZE_HINDO(word->stem.hindo, loctset->dic_freq_max, loctset->dic_freq_min);
    }

    word->stem.loc = loctset->loct;                                     /* 検索位置 */

    /* 擬似候補の種類をクリア */
    word->stem.type = 0;

    return 1;
}


/**
 *      統合辞書アダプタ  候補文字列取得
 *
 * @param[in]       word : 単語情報(NJ_RESULT->wordを指定)
 * @param[out] candidate : 候補文字列格納バッファ
 * @param[in]       size : 候補文字列格納バッファサイズ(byte)
 *
 * @retval            <0   エラー
 * @retval            >0   取得文字配列長(ヌル文字含まず)
 */
NJ_INT16 njd_t_get_candidate(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size) {
    NJ_SEARCH_LOCATION *loc;
    NJ_UINT8 *data;
    NJ_UINT16 len;
    STEM_DATA_SET stem_set;
    NJ_UINT32 current;
    NJ_UINT8 is_hicomp = 1;


    if (NJ_GET_DIC_TYPE(word->stem.loc.handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    switch (GET_LOCATION_OPERATION(word->stem.loc.status)) {
    case NJ_CUR_OP_COMP:
    case NJ_CUR_OP_FORE:
        /* 正引き完全一致検索時 */
        /* 正引き前方一致検索時 */
        /* 該当候補データのアドレスを取得する */
        loc = &word->stem.loc;
        /* 単語データ領域のアドレスを取得 */
        data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, WORD_TOP_ADDR(loc->handle), loc->current);

        /* 該当候補データの情報を取得する */
        get_stem_cand_data(loc->handle, data, &stem_set);
        len = stem_set.candidate_len;
        break;

    case NJ_CUR_OP_LINK:
        /* つながり予測検索時 */
        /* 該当候補データのアドレスを取得する */
        loc = &word->stem.loc;
        /* つながりデータ番号から単語データ番号、頻度を取得 */
        current = TSUNAGARI_WORD_DATA_NO(TSUNAGARI_DATA_ADDR(TSUNAGARI_TOP_ADDR(loc->handle), loc->current));
        /* 単語データ領域のアドレスを取得 */
        data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, WORD_TOP_ADDR(loc->handle), current);

        /* 該当候補データの情報を取得する */
        get_stem_cand_data(loc->handle, data, &stem_set);
        len = stem_set.candidate_len;
        break;

    case NJ_CUR_OP_REV_FORE:
        /* 逆引き前方一致検索時 */
        /* 該当候補データのアドレスを取得する */
        loc = &word->stem.loc;
        if (loc->ext_area[NJ_TYPE_EXT_AREA_MORPHO] != NULL) {
            /* 拡張領域(形態素用)表記文字列インデックス番号から単語データ番号を取得 */
            current = HYOKI_INDEX_WORD_NO(HYOKI_INDEX_ADDR(EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loc->ext_area[NJ_TYPE_EXT_AREA_MORPHO]), loc->current));
        } else {
            /* 表記文字列インデックス番号から単語データ番号を取得 */
            current = HYOKI_INDEX_WORD_NO(HYOKI_INDEX_ADDR(HYOKI_IDX_AREA_TOP_ADDR(loc->handle), loc->current));
        }
        /* 単語データ領域のアドレスを取得 */
        data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, WORD_TOP_ADDR(loc->handle), current);

        /* 該当候補データの情報を取得する */
        get_stem_cand_data(loc->handle, data, &stem_set);
        len = stem_set.candidate_len;
        break;

    case NJ_CUR_OP_COMP_EXT:
    case NJ_CUR_OP_FORE_EXT:
        /* 正引き完全一致拡張, 正引き前方一致拡張検索時 */
        /* 該当候補データのアドレスを取得する */
        loc = &word->stem.loc;
        if (loc->ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
            /* 表記文字列インデックス番号から単語データ番号を取得 */
            current = HYOKI_INDEX_WORD_NO(HYOKI_INDEX_ADDR(EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loc->ext_area[NJ_TYPE_EXT_AREA_INPUT]), loc->current));
            /* 単語データ領域のアドレスを取得 */
            data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, WORD_TOP_ADDR(loc->handle), current);

            /* 該当候補データの情報を取得する */
            get_stem_cand_data(loc->handle, data, &stem_set);
            len = stem_set.candidate_len;
        } else {
            len = 0;
            return len;
        }
        break;

    default:
        /* 逆引き完全一致検索結果の場合はエラーとする。*/
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_GET_CANDIDATE, NJ_ERR_INVALID_RESULT); /*NCH*/
    }

    if (len == 0) {
        len = YOMI_LEN_FROM_DIC_TYPE(is_hicomp, data);
    }
    if (size < (NJ_UINT16)((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_GET_CANDIDATE, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH*/
    }
    len = get_candidate_string(loc->handle, data, YOMI_HYOKI_TOP_ADDR(loc->handle), candidate);

    return len;
}


/**
 *      検索結果から読み文字列を取得する
 *
 * @param[in]    word : 単語情報(NJ_RESULT->wordを指定)
 * @param[out] stroke : 読み文字列格納バッファ
 * @param[in]    size : 読み文字列格納バッファサイズ(byte)
 *
 * @retval         <0   エラー
 * @retval         >0   取得文字配列長(ヌル文字含まず)
 */
NJ_INT16 njd_t_get_stroke(NJ_WORD *word, NJ_CHAR *stroke, NJ_UINT16 size) {
    NJ_SEARCH_LOCATION *loc;
    NJ_UINT8 *data;
    NJ_INT16 len;

    NJ_UINT16 yomi_len;
    NJ_UINT8 *word_addr;
    NJ_UINT8 *tdtbl_addr, *tdtbl_tmp;
    NJ_UINT32 current;
    NJ_UINT8 *hitbl_addr, *hitbl_tmp;
    NJ_UINT8 is_hicomp = 1;



    if (NJ_GET_DIC_TYPE(word->stem.loc.handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    switch (GET_LOCATION_OPERATION(word->stem.loc.status)) {
    case NJ_CUR_OP_COMP:
    case NJ_CUR_OP_FORE:
        /* 正引き完全一致、正引き前方一致検索 */
        if (NJ_GET_YLEN_FROM_STEM(word) == 0) {
            /* word の読み文字列長が 0 の場合,エラーとする */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_GET_STROKE, NJ_ERR_INVALID_RESULT); /*NCH*/
        }
        /* アドレス取得 */
        loc = &word->stem.loc;
        /* 単語データ領域の先頭アドレスを取得 */
        word_addr = WORD_TOP_ADDR(loc->handle);
        /* 該当候補データのアドレスを取得する */
        data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, word_addr, loc->current);
        break;

    case NJ_CUR_OP_LINK:
        /* つながり予測検索 */
        /* アドレス取得 */
        loc = &word->stem.loc;
        /* 単語データ領域の先頭アドレスを取得 */
        word_addr = WORD_TOP_ADDR(loc->handle);

        /* つながりデータ領域の先頭アドレスを取得 */
        tdtbl_addr = TSUNAGARI_TOP_ADDR(loc->handle);
        /* つながりデータ番号から単語データ番号、頻度を取得 */
        tdtbl_tmp = TSUNAGARI_DATA_ADDR(tdtbl_addr, loc->current);
        current = TSUNAGARI_WORD_DATA_NO(tdtbl_tmp);
        data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, word_addr, current);
        break;

    case NJ_CUR_OP_REV:
    case NJ_CUR_OP_REV_FORE:
        /* アドレス取得 */
        loc = &word->stem.loc;

        /* 単語データ領域の先頭アドレスを取得 */
        word_addr = WORD_TOP_ADDR(loc->handle);

        if (loc->ext_area[NJ_TYPE_EXT_AREA_MORPHO] != NULL) {
            /* 拡張領域(形態素用)表記文字列インデックス領域の先頭アドレスを取得 */
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loc->ext_area[NJ_TYPE_EXT_AREA_MORPHO]);
        } else {
            /* 表記文字列インデックス領域の先頭アドレスを取得 */
            hitbl_addr = HYOKI_IDX_AREA_TOP_ADDR(loc->handle);
        }
        /* 表記文字列インデックス番号から単語データ番号を取得 */
        hitbl_tmp = HYOKI_INDEX_ADDR(hitbl_addr, loc->current);
        current = HYOKI_INDEX_WORD_NO(hitbl_tmp);
        data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, word_addr, current);
        break;

    case NJ_CUR_OP_COMP_EXT:
    case NJ_CUR_OP_FORE_EXT:
        /* 正引き完全一致拡張, 正引き前方一致拡張検索 */
        /* アドレス取得 */
        loc = &word->stem.loc;

        if (loc->ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
            /* 単語データ領域の先頭アドレスを取得 */
            word_addr = WORD_TOP_ADDR(loc->handle);

            /* 表記文字列インデックス領域の先頭アドレスを取得 */
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loc->ext_area[NJ_TYPE_EXT_AREA_INPUT]);
            /* 表記文字列インデックス番号から単語データ番号を取得 */
            hitbl_tmp = HYOKI_INDEX_ADDR(hitbl_addr, loc->current);
            current = HYOKI_INDEX_WORD_NO(hitbl_tmp);
            data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, word_addr, current);
        } else {
            len = 0;
            return len;
        }
        break;

    default:
        /* 上記,検索結果以外の場合はエラーとする。*/
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_GET_STROKE, NJ_ERR_INVALID_RESULT); /*NCH*/
    }

    /* 読み文字列を取得する */
    /* 該当する単語の読み文字列を取得する */
    yomi_len = YOMI_LEN_FROM_DIC_TYPE(is_hicomp, data);
    len = get_stem_yomi_string(loc->handle, data, stroke, yomi_len, size);

    /* サイズチェック */
    if (size < (NJ_UINT16)((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_GET_STROKE, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH*/
    }

    *(stroke + len) = NJ_CHAR_NUL;
    return len;
}


/**
 * 正引き前方一致検索−あいまい予測時、統合辞書から検索条件（読み）に該当する単語を検索し、該当する候補データの情報を取得
 *
 * @param[in,out]     iwnn : 解析情報クラス
 * @param[in]    condition : 検索条件
 * @param[in,out]  loctset : 検索位置
 * @param[in]         hidx : 辞書のインデックス
 *
 * @retval               0   検索候補なし
 * @retval               1   検索候補あり
 */
static NJ_INT16 search_node_aimai(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 hidx) {
    NJ_CHAR *yomi;
    NJ_UINT8 *stem_data;
    NJ_INT32 hindo_tmp, hindo_chk;
    NJ_UINT32 current, hindo_tmp_data;

    /* 新検索 */
    NJ_SEARCH_CACHE *psrhCache = condition->ds->dic[hidx].srhCache;
    NJ_CHAR  *key;
    NJ_UINT8 cmpflg;
    NJ_UINT8 endflg;
    NJ_UINT16 abPtrIdx;
    NJ_UINT16 key_len;
    NJ_UINT16 i, l, m;
    NJ_UINT16 abIdx;
    NJ_UINT16 abIdx_old;
    NJ_UINT16 addcnt = 0;
    NJ_CHAR   char_tmp[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_UINT16 tmp_len;
    NJ_UINT16 endIdx;
    NJ_INT16 ret = 0;
    NJ_UINT16 yomi_clen;
    NJ_UINT16 yomi_clen_funfun;
    NJ_UINT8 aimai_flg = 0x01;
    NJ_CHAR  key_tmp[NJ_MAX_CHAR_LEN + NJ_TERM_LEN];

    NJ_UINT32 word_top = 0, word_bottom = 0;
    NJ_UINT8 *hdtbl_addr;

    NJ_SEARCH_LOCATION_SET *loctset_tmp = loctset;
    NJ_INT32 word_clen = 0;
    NJ_CACHE_INFO tmpbuff;


    NJ_INT16 ss_rtn = 0;
    NJ_UINT8 relation_chk_flg = 0;

    NJ_UINT8 *ext_top;
    NJ_UINT8 no_used_flg;
    NJ_UINT8 is_hicomp = 1;

    NJ_SEARCH_LOCATION_SET tmp_loctset;

#ifdef NJ_OPT_CHARSET_2BYTE
    NJ_UINT16 start;
    NJ_UINT16 end;
#endif /* NJ_OPT_CHARSET_2BYTE */


    /* 曖昧検索は、頻度順検索のみの為、
     * 読み順検索(頻度順検索以外)の場合は検索候補なしとする */
    if (condition->mode != NJ_CUR_MODE_FREQ) {
        /* 検索候補なし */
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH*/
        return 0; /*NCH*/
    }

    if (NJ_GET_DIC_TYPE(loctset->loct.handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    if (NJ_GET_CACHEOVER_FROM_SCACHE(psrhCache) != 0) {
        aimai_flg = 0x00;
    }

    /* 検索対象読み文字列 */
    yomi = condition->yomi;

    /* 辞書情報を取得 */
    hdtbl_addr = HINDO_TOP_ADDR(loctset->loct.handle);

    /* ループ処理前の初期化 */
    endflg = 0x00;
    cmpflg = 0x00;
    abPtrIdx = 0;
    key = condition->ds->keyword;

    /* 文字数分、曖昧検索を行う。*/
    yomi_clen = condition->yclen;
    for (i = 0; i < yomi_clen; i++) {
        /* ポインターインデックスの格納 */
        abPtrIdx = i;

        /* インデックス情報の構築 */
        if (!cmpflg) {    /* cmpflg == 0x00 */
            /* 一文字分を比較してOKだった。 */
            if (((abPtrIdx != 0) && (psrhCache->keyPtr[abPtrIdx] == 0))
                || (psrhCache->keyPtr[abPtrIdx + 1] == 0)) {
                /* インデックス情報がない */
                cmpflg = 0x01;
            } else {
                /* インデックス情報あり */
            }
        }

        addcnt = 0;
        if (cmpflg) {    /* cmpflg == 0x01 */
            /* インデックス情報なしと判定された場合 */
            if (abPtrIdx == 0) {
                /* 初回のインデックス情報を作成する。 */
                abIdx = 0;
                /* 一文字分をコピーする */
                nj_charncpy(key_tmp, yomi, 1);
                key_len = nj_strlen(key_tmp);

                psrhCache->keyPtr[0] = 0;

                /* 読み文字からハッシュインデックスを検索し、
                 * 該当する単語データ番号の先頭と終端を検索する */
                ret = search_hash_idx(loctset->loct.handle, key_tmp, key_len, &word_top, &word_bottom);

                if (ret < 0) {
                    /* 存在しなかった場合 */
                } else {
                    /* 存在した場合 */

                    /* 対象読み文字列から単語データ先頭,終端番号を取得する */
                    ret = SEARCH_WORD_NO_BY_HICOMP_TYPE(is_hicomp, loctset->loct.handle, loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], condition->operation,
                                         word_top, word_bottom,
                                         key_tmp, key_len, 0, NJ_MODE_TYPE_YOSOKU,
                                         &tmpbuff.top, &tmpbuff.bottom,
                                         &word_clen);

                    if (ret < 0) {
                        /* 存在しなかった場合 */
                    } else {
                        /* 単語データ番号の先頭,終端番号をセット */
                        psrhCache->storebuff[abIdx].top    = tmpbuff.top;
                        psrhCache->storebuff[abIdx].bottom = tmpbuff.bottom;
                        psrhCache->storebuff[abIdx].idx_no = (NJ_INT8)key_len;
                        addcnt++;
                        abIdx++;
                    }
                }

                if ((condition->charset != NULL) && aimai_flg) {
                    /* 曖昧検索処理 */
#ifdef NJ_OPT_CHARSET_2BYTE
                    /* keyに一致するあいまい文字セットの範囲を２分検索する */
                    if (njd_search_charset_range(condition->charset, key, &start, &end) == 1) {
                        /* 範囲が見つかった */
                        for (l = start; l <= end; l++) {
#else /* NJ_OPT_CHARSET_2BYTE */
                    for (l = 0; l < condition->charset->charset_count; l++) {
                        /* 曖昧検索文字の抽出 */
                        if (nj_charncmp(key, condition->charset->from[l], 1) == 0) {
#endif /* NJ_OPT_CHARSET_2BYTE */
                            /* 一致しているなら */
                            nj_strcpy(char_tmp, condition->charset->to[l]);
                            tmp_len = nj_strlen(char_tmp);

                            /* 読み文字からハッシュインデックスを検索し、
                             * 該当する単語データ番号の先頭と終端を検索する */
                            ret = search_hash_idx(loctset->loct.handle, char_tmp, tmp_len,
                                                  &word_top, &word_bottom);
                            if (ret < 0) {
                                /* 存在しなかった場合 */
                            } else {
                                /* 存在した場合 */

                                /* 対象読み文字列から単語データ先頭,終端番号を取得する */
                                ret = SEARCH_WORD_NO_BY_HICOMP_TYPE(is_hicomp, loctset->loct.handle, loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT],
                                                     condition->operation, word_top, word_bottom,
                                                     char_tmp, tmp_len, 0, NJ_MODE_TYPE_YOSOKU,
                                                     &tmpbuff.top, &tmpbuff.bottom,
                                                     &word_clen);

                                if (ret < 0) {
                                    /* 存在しなかった場合 */
                                } else {
                                    /* 存在した場合 */
                                    /* キャッシュ溢れ発生 */
                                    if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                        psrhCache->keyPtr[abPtrIdx+1] = 0; /*NCH*/
                                        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_NOT_ENOUGH); /*NCH*/
                                    }

                                    /* 単語データ番号の先頭,終端番号をセット */
                                    psrhCache->storebuff[abIdx].top    = tmpbuff.top;
                                    psrhCache->storebuff[abIdx].bottom = tmpbuff.bottom;
                                    psrhCache->storebuff[abIdx].idx_no = (NJ_INT8)tmp_len;

                                    addcnt++;
                                    abIdx++;
                                }
                            }
                        }
                    }
                }
                psrhCache->keyPtr[abPtrIdx + 1] = abIdx;
            } else {
                /* 現在のインデックス情報から検索を行う。 */
                /* 一文字分をコピーする */
                nj_charncpy(key_tmp, yomi, 1);
                key_len = nj_strlen(key_tmp);

                if (psrhCache->keyPtr[abPtrIdx] == psrhCache->keyPtr[abPtrIdx - 1]) {
                    /* 検索結果が存在しない */
                    psrhCache->keyPtr[abPtrIdx+1] = psrhCache->keyPtr[abPtrIdx-1];
                    endflg = 0x01;
                } else {
                    /* 検索結果が存在する */
                    /* インデックス有効数を取得 */
                    endIdx = psrhCache->keyPtr[abPtrIdx];
                    abIdx_old = psrhCache->keyPtr[abPtrIdx - 1];

                    if (NJ_GET_CACHEOVER_FROM_SCACHE(psrhCache) != 0) {
                        abIdx = psrhCache->keyPtr[abPtrIdx - 1];
                        psrhCache->keyPtr[abPtrIdx] = abIdx;
                    } else {
                        abIdx = psrhCache->keyPtr[abPtrIdx];
                    }

                    if ((abIdx > NJ_SEARCH_CACHE_SIZE) || (abIdx_old >= NJ_SEARCH_CACHE_SIZE) ||
                        (endIdx > NJ_SEARCH_CACHE_SIZE)) {
                        /* キャッシュが破壊されている */
                        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_BROKEN); /*NCH_FB*/
                    }

                    for (m = abIdx_old; m < endIdx; m++) {
                        /* 対象読み文字列から単語データ先頭,終端番号を取得する */
                        ret = SEARCH_WORD_NO_BY_HICOMP_TYPE(is_hicomp, loctset->loct.handle, loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], condition->operation,
                                             psrhCache->storebuff[m].top, psrhCache->storebuff[m].bottom,
                                             key_tmp, key_len, psrhCache->storebuff[m].idx_no, NJ_MODE_TYPE_YOSOKU,
                                             &tmpbuff.top, &tmpbuff.bottom, &word_clen);

                        if (ret < 0) {
                            /* 存在しなかった場合 */
                        } else {
                            /* 存在した場合 */
                            /* キャッシュ溢れ発生 */
                            if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                psrhCache->keyPtr[abPtrIdx+1] = 0;
                                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_NOT_ENOUGH);
                            }

                            /* 単語データ番号の先頭,終端番号をセット */
                            psrhCache->storebuff[abIdx].top    = tmpbuff.top;
                            psrhCache->storebuff[abIdx].bottom = tmpbuff.bottom;
                            psrhCache->storebuff[abIdx].idx_no = psrhCache->storebuff[m].idx_no + key_len;
                            addcnt++;
                            abIdx++;
                        }

                        if ((condition->charset != NULL) && aimai_flg) {
                            /* 曖昧検索処理 */
#ifdef NJ_OPT_CHARSET_2BYTE
                            /* keyに一致するあいまい文字セットの範囲を２分検索する */
                            if (njd_search_charset_range(condition->charset, key, &start, &end) == 1) {
                                /* 範囲が見つかった */
                                for (l = start; l <= end; l++) {
#else /* NJ_OPT_CHARSET_2BYTE */
                            for (l = 0; l < condition->charset->charset_count; l++) {
                                /* 曖昧検索文字の抽出 */
                                if (nj_charncmp(key, condition->charset->from[l], 1) == 0) {
#endif /* NJ_OPT_CHARSET_2BYTE */
                                    /* 一致しているなら */
                                    nj_strcpy(char_tmp, condition->charset->to[l]);

                                    tmp_len = nj_strlen(char_tmp);

                                    /* 対象読み文字列から単語データ先頭,終端番号を取得する */
                                    ret = SEARCH_WORD_NO_BY_HICOMP_TYPE(is_hicomp, loctset->loct.handle, loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT],
                                                         condition->operation, psrhCache->storebuff[m].top,
                                                         psrhCache->storebuff[m].bottom, char_tmp, tmp_len,
                                                         psrhCache->storebuff[m].idx_no, NJ_MODE_TYPE_YOSOKU,
                                                         &tmpbuff.top, &tmpbuff.bottom, &word_clen);

                                    if (ret < 0) {
                                        /* 存在しなかった場合 */
                                    } else {
                                        /* 存在した場合 */
                                        /* キャッシュ溢れ発生 */
                                        if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                            psrhCache->keyPtr[abPtrIdx+1] = 0; /*NCH_DEF*/
                                            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_NOT_ENOUGH); /*NCH_DEF*/
                                        }

                                        /* 単語データ番号の先頭,終端番号をセット */
                                        psrhCache->storebuff[abIdx].top    = tmpbuff.top;
                                        psrhCache->storebuff[abIdx].bottom = tmpbuff.bottom;
                                        psrhCache->storebuff[abIdx].idx_no = psrhCache->storebuff[m].idx_no + tmp_len;
                                        addcnt++;
                                        abIdx++;
                                    }
                                }
                            }
                        }
                    }
                    psrhCache->keyPtr[abPtrIdx + 1] = abIdx;
                }
            }
        }
        yomi += NJ_CHAR_LEN(yomi);
        key  += NJ_CHAR_LEN(key);
    }
    /* 今回の検索結果件数が0かつキャッシュに候補がない（検索対象読み文字列長-1の開始位置と終了位置が同じ）場合に終了 */
    if ((addcnt == 0) && (psrhCache->keyPtr[yomi_clen - 1] == psrhCache->keyPtr[yomi_clen])) {
        endflg = 0x01;
    }

    if (endflg) {        /* endflg == 0x01 */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    loctset->loct.current = 0;

    /**************************************************/
    /* 頻度値の取得処理                               */
    /**************************************************/

    /* 有効インデックスを取得する */
    abPtrIdx = condition->yclen;

    /* 開始・終了インデックスを取得する */
    abIdx = psrhCache->keyPtr[abPtrIdx];
    abIdx_old = psrhCache->keyPtr[abPtrIdx - 1];
    if ((abIdx > NJ_SEARCH_CACHE_SIZE) || (abIdx_old >= NJ_SEARCH_CACHE_SIZE)) {
        /* キャッシュが破壊されている */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_BROKEN); /*NCH*/
    }

    /* 頻度学習値利用に必要なデータを取得する */
    if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL) {
        ext_top = EXT_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], loctset->loct.handle);
    } else {
        ext_top = NULL;
    }

    /* 正引き前方一致検索で前確定情報がある場合、
     * 前確定単語のつながり情報を取得する。 */
    if ((condition->operation == NJ_CUR_OP_FORE) && 
        (iwnn->previous_selection.count != 0)) {

        /* 前確定情報につながるデータがあるか確認する */
        ss_rtn = search_node_yominashi(iwnn, condition, loctset_tmp, hidx);
        if (ss_rtn > 0) {
            /* 前確定情報をセット */
            iwnn->prev_search_range[hidx][NJ_SEARCH_RANGE_TOP] = loctset_tmp->loct.top;
            iwnn->prev_search_range[hidx][NJ_SEARCH_RANGE_BOTTOM] = loctset_tmp->loct.bottom;
        } else {
            /* 前確定情報がない場合、確定情報をクリア */
            iwnn->prev_search_range[hidx][NJ_SEARCH_RANGE_TOP] = 0;
            iwnn->prev_search_range[hidx][NJ_SEARCH_RANGE_BOTTOM] = 0;
        }
    } else {
        /* 前確定情報がない場合、確定情報をクリア */
        iwnn->prev_search_range[hidx][NJ_SEARCH_RANGE_TOP] = 0;
        iwnn->prev_search_range[hidx][NJ_SEARCH_RANGE_BOTTOM] = 0;
    }
    loctset_tmp->loct.status = NJ_ST_SEARCH_NO_INIT;

    /* 入力文字列に近く、１番頻度の高い候補を第一候補とする */
    hindo_tmp = INIT_HINDO;
    hindo_chk = INIT_HINDO;
    hindo_tmp_data = 0;
    current = 0;
    if (yomi_clen >= iwnn->environment.option.char_min) {
        yomi_clen_funfun = yomi_clen;
    } else {
        yomi_clen_funfun = iwnn->environment.option.char_min;
    }
    for (m = abIdx_old; m < abIdx; m++) {
        current = psrhCache->storebuff[m].top;
        /* top 〜 bottom の範囲で最大頻度の候補を探す。*/
        while (current <= psrhCache->storebuff[m].bottom) {

            /* 検索制限の解除フラグをチェックする */
            no_used_flg = 0;
            if ((condition->ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) {
                /* 単語使用有無の確認 */
                if (!IS_USED_FOR_FORECAST(current, hdtbl_addr, ext_top, yomi_clen_funfun)) {
                    no_used_flg = 1;
                }
            } else {
                /* 単語検索APIの時のみ通る */
                /* 単語使用有無の確認 必ず３文字以上インデックスを使う */
                if (!IS_USED_FOR_FORECAST(current, hdtbl_addr, ext_top, 3)) {
                    no_used_flg = 1;
                }
            }
            if (no_used_flg) {
                current++;
                continue;
            }

            /* 予測総合頻度を取得する */
            stem_data = HINDO_YOSOKU_ADDR(hdtbl_addr, current);
            hindo_chk = get_hindo(iwnn, current, HINDO_DATA(stem_data), loctset, &relation_chk_flg, hidx);

            /* 最高頻度値の単語番号、頻度値を取得 */
            if (hindo_tmp < hindo_chk) {

                /* 該当候補に対して統合辞書フィルターを実行 */
                if (iwnn->option_data.phase3_filter != NULL) {
                    tmp_loctset = *loctset;
                    tmp_loctset.loct.top = psrhCache->storebuff[m].top;
                    tmp_loctset.loct.bottom = psrhCache->storebuff[m].bottom;
                    tmp_loctset.cache_freq = CALCULATE_HINDO(hindo_chk, loctset->dic_freq.base,
                                                             loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
                    tmp_loctset.cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
                    tmp_loctset.loct.current = current;
                    tmp_loctset.loct.attr = get_attr(loctset->loct.handle, current);
                    tmp_loctset.loct.current_cache = m;

                    /* フィルターチェック処理を実行 */
                    if (!(is_tdic_filtering(iwnn, &tmp_loctset))) {
                        /* 候補使用なしの場合、次候補検索へ移る */
                        current++;
                        continue;
                    }
                }
                hindo_tmp = hindo_chk;
                hindo_tmp_data = current;
                /* 取得した候補が絞込検索候補であった場合 */
                if (relation_chk_flg == 1) {
                    /* 絞込検索候補フラグの立てる */
                    iwnn->wk_relation_cand_flg[hidx] = 1;
                } else {
                    /* 絞込検索候補フラグの落とす */
                    iwnn->wk_relation_cand_flg[hidx] = 0;
                }
            }
            current++;
        }

        if (hindo_tmp <= INIT_HINDO) {
            /* 候補なしの場合は、次のキャッシュを検索する。 */
            psrhCache->storebuff[m].current = LOC_CURRENT_NO_ENTRY;
        } else {
            /* 候補ありの場合は、有りになって時点でループを抜ける */
            psrhCache->storebuff[m].current = hindo_tmp_data;
            break;
        }
    }

    /* 全検索キャッシュをチェック候補が見つからなかった場合 */
    if (hindo_tmp <= INIT_HINDO) {
        /* 候補なしとする */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    /* 複数頻度がある場合は、最大値を設定する。 */
    loctset->loct.top = psrhCache->storebuff[m].top;
    loctset->loct.bottom = psrhCache->storebuff[m].bottom;
    loctset->cache_freq = CALCULATE_HINDO(hindo_tmp, loctset->dic_freq.base,
                                          loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
    loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
    loctset->loct.current = hindo_tmp_data;
    loctset->loct.attr   = get_attr(loctset->loct.handle, hindo_tmp_data);
    loctset->loct.current_cache = m;

    /* 曖昧検索用表示制御変数の初期化 */
    psrhCache->viewCnt = 1;
    NJ_SET_AIMAI_TO_SCACHE(psrhCache);

    return 1; /* 検索候補あり */
}


/**
 * 検索対象文字をハッシュインデックスから検索し、該当する単語データの先頭,終端番号を取得する。
 *
 * @param[in]       hdl : 辞書ハンドル
 * @param[in]      yomi : 検索する単語の読み文字
 * @param[in]      ylen : 検索する単語の読み文字配列長
 * @param[out]    topno : 該当単語データ先頭番号
 * @param[out] bottomno : 該当単語データ終端番号
 *
 * @retval           -1   該当データなし
 * @retval          >0    検索する単語の読みバイト長
 */
static NJ_INT16 search_hash_idx(NJ_DIC_HANDLE hdl, NJ_CHAR *yomi, NJ_INT16 ylen, NJ_UINT32 *topno, NJ_UINT32 *bottomno) {
    NJ_UINT8 *yitbl_addr;
    NJ_INT32  yitbl_cnt;
    NJ_UINT32 word_top = 0, word_bottom = 0;
    NJ_INT32  cnt = 0;
    NJ_UINT8  *char_tmp;
    NJ_UINT8 *h_char, *in_char;
    NJ_INT16  ysize_cnt = 0, ysize = 0;
    NJ_UINT16 hash_idx_st;
    NJ_UINT16 hash_idx_ed;
    NJ_INT8   next;


    /* 初期化 */
    h_char = NULL;
    in_char= (NJ_UINT8*)yomi;

    /* 読み文字列インデックステーブル情報を取得する
     * ( データ登録数、先頭アドレス )
     */
    yitbl_addr = YOMI_IDX_AREA_TOP_ADDR(hdl);
    yitbl_cnt  = YOMI_IDX_AREA_CNT(hdl);
    char_tmp   = yitbl_addr;

    /* ylen をバイト単位に直す（以降の処理はバイト単位で行っているため）*/
    ysize = ylen * sizeof(NJ_CHAR);

    /* 読み文字列インデックステーブル分ループを行う。 */
    next = -1;
    for (cnt = 1; cnt <= yitbl_cnt; cnt++) {
        h_char   = char_tmp;
        in_char  = (NJ_UINT8*)yomi;
        ysize_cnt = 0;
        next     = -1;
        /* 読み先頭共通文字列判定処理 */
        while ((*h_char != 0x00) && (ysize_cnt < ysize)) {
            if ( *h_char < *in_char ) {
                /* 読み先頭共通文字列に含まれないため抜ける */
                next = -1;
                break;
            } else if ( *h_char > *in_char ) {
                /* 読み先頭共通文字列の方が大きい */
                next = 1;
                break;
            } else {
                /* 一致した場合 */
                h_char++;
                in_char++;
                ysize_cnt++;
                next = 0;
            }
            if ((*h_char != 0x00) && (ysize_cnt >= ysize)) {
                /* 入力文字列より長い読み先頭共通文字列を持つ場合は利用しない */
                next = 1;
                break;
            }
            
        }
        if ( next == -1 ) {
            /* 0x00000000のハッシュインデックスを見る場合 */
            break;
        } else if ( next == 0 ) {
            /* 一致する読み先頭共通文字列を見つけた場合 */
            break;
        }
        /* 読み先頭共通文字列 + ハッシュインデックスインクリメント */
        /* 読み先頭共通文字列(4バイト) + ハッシュインデックス(4 * 257バイト) */
        char_tmp += 4;
        char_tmp += (NJ_UINT32)(HASH_INDEX_CNT * 4);
    }

    if (next == -1) {
        /* 0x00000000のハッシュインデックス有無判定処理 */
        char_tmp = YOMI_IDX_DATA_ADDR(yitbl_addr, yitbl_cnt);
        /* 読み共通文字列が0x00000000の場合のみ、以下の処理を行う */
        if (NJ_INT32_READ(char_tmp) != 0x00000000 ) {
            /**
             * 指定された文字コードが読み文字列インデックステーブルに無い
             *  = 指定された辞書では、その読み文字列が使われていないとする。
             */
            return -1;        /* 検索候補無し */
        }
#ifdef NJ_OPT_UTF16
        ysize_cnt++;
        in_char++;
#else /*NJ_OPT_UTF16*/
        ysize_cnt = 0;
        in_char  = (NJ_UINT8*)yomi;
#endif /*NJ_OPT_UTF16*/
    } else if (next == 1) {
        return -1;
    }

    /* ハッシュインデックスの読込 */
    if (ysize_cnt >= ysize) {
        /* 読み共通文字列で完全一致した場合 */
        hash_idx_st = 0;
        hash_idx_ed = HASH_INDEX_CNT - 1;
    } else {
        /* 次の1バイト情報を取得する */
        hash_idx_st = *in_char;
        hash_idx_ed = hash_idx_st + 1;
        ysize_cnt++;
    }

    /* 読み共通文字列分(４バイト)進める */
    char_tmp += 4;
    /* ハッシュインデックスから対象の単語番号を取得する */
    word_top = (NJ_UINT32)NJ_INT32_READ((char_tmp + ((hash_idx_st) * 4)));
    /* ストッパーとしてハッシュインデックスから
     * 次ハッシュインデックスの先頭単語の単語番号を取得する */
    word_bottom = (NJ_UINT32)NJ_INT32_READ((char_tmp + ((hash_idx_ed) * 4)));

    /* 単語終端番号をセットする為、
     * 先頭番号よりも大きい場合は、次ハッシュの先頭番号を-1する */
    if (word_top < word_bottom) {
        word_bottom--;
    } else if (word_top == word_bottom) {
        /* 単語先頭と次ハッシュの先頭番号がイコールの場合は、
         * 検索候補がないので、検索候補無しで返す。 */
        return -1;
    }

    /* 検索対象文字の該当する単語データ領域先頭、終端単語データ番号をセット */
    *topno    = word_top;
    *bottomno = word_bottom;
    
    return (ysize_cnt);
}


/**
 * 検索対象文字を対象単語データ領域中から検索し、該当する単語データの先頭,終端番号を取得する。
 *
 * @attention 前方一致検索の時は、yomiを１文字ずつ指定して、startposをずらしながら複数回検索する。
 *
 * @param[in]           hdl  辞書ハンドル
 * @param[in]      ext_area  頻度学習領域
 * @param[in]          mode  検索条件
 * @param[in]     src_topno  検索対象単語データ先頭番号
 * @param[in]  src_bottomno  検索対象単語データ終端番号
 * @param[in]          yomi  検索する単語の読み文字
 * @param[in]       yomilen  検索する単語の読み文字配列長
 * @param[in]      startpos  単語データ検索開始位置(文字配列要素番号)
 * @param[in]         check  検索範囲制限
 * @param[out]    dst_topno  取得単語データ先頭番号
 * @param[out] dst_bottomno  取得単語データ終端番号
 * @param[out]    word_clen  検索対象文字数
 *
 * @retval               -1   該当データなし
 * @retval               -2   該当データなし(前方一致有)
 * @retval              >=0   検索する単語の読み文字の読み文字配列長
 */
static NJ_INT16 search_word_no(NJ_DIC_HANDLE hdl, NJ_VOID *ext_area, NJ_UINT8 mode,
                               NJ_UINT32 src_topno, NJ_UINT32 src_bottomno,
                               NJ_CHAR  *yomi, NJ_UINT16 yomilen,
                               NJ_INT16 startpos, NJ_INT16 check,
                               NJ_UINT32 *dst_topno, NJ_UINT32 *dst_bottomno,
                               NJ_INT32 *word_clen) {

    NJ_INT16 char_len;                                  /* 文字Length格納用 */
    NJ_UINT16 ylen;                                     /* 検索対象文字列長操作用 */
    NJ_CHAR  *yomi_tmp;                                 /* 読み文字列操作用 */
    NJ_UINT32 left, right, mid, mid_bak;                /* binary search用index */
    NJ_UINT8 *wdtbl_addr, *wdtbl_tmp;                   /* 単語データ領域操作用 */
    NJ_INT16 rtn;                                       /* 関数戻り値 */
    NJ_UINT16 wkc_len;                                  /* 単語データ */
    NJ_UINT8 *yhtbl_addr;                               /* 読み・表記データ領域先頭アドレス */
    NJ_UINT8  *wkc;                                     /* 辞書登録語の読み */
    NJ_INT32 top_tmp = src_topno;                       /* 単語データ */
    NJ_INT32 bottom_tmp = src_bottomno;                 /* 検索文字End値取得用 */
    NJ_INT32 lop;                                      /* ループカウンター */
    NJ_UINT8 findflg;
    NJ_UINT8 char_match;
    NJ_UINT8 *ext_top;
    NJ_INT16 diff;
    

    if ((src_topno == 0) || (src_bottomno == 0) || (src_topno > src_bottomno)) {
        /* 検索対象なしとする */
        *dst_topno = 0; /*NCH*/
        *dst_bottomno = 0; /*NCH*/
        return -1; /*NCH*/
    }

    /* 単語データ領域の先頭アドレスを取得する */
    wdtbl_addr = WORD_TOP_ADDR(hdl);
    yhtbl_addr = YOMI_HYOKI_TOP_ADDR(hdl);

    /* 初期情報設定 */
    *word_clen = 0;
    yomi_tmp = yomi;
    rtn   = startpos;
    left  = src_topno;
    right = src_bottomno;

    if (mode == NJ_CUR_OP_FORE) {
        /* 前方一致検索 */
        char_len = 1;
        for (ylen = 0; ylen < yomilen; ylen += char_len) {
            /* yomi_top にある１文字の文字配列要素数 */
            char_len = NJ_CHAR_LEN(yomi_tmp);

            /* 検索対象読み文字列を検索し、topを取得する */
            findflg = 0;
            while (left <= right) {
                mid = left + ((right - left) / 2);
                wdtbl_tmp = WORD_DATA_ADDR(wdtbl_addr, mid);
                /* 読み文字配列長を取得する */
                wkc_len = (NJ_INT16)YOMI_LEN(wdtbl_tmp);
                /* 取得した読み文字配列長が、検索開始位置より大きい場合、
                 * 確認処理を行う。 */
                if (wkc_len > rtn) {
                    /* 読み文字列位置を取得する(unsigned char*) */
                    wkc = yhtbl_addr + YOMI_DATA_OFFSET(wdtbl_tmp) + (rtn * sizeof(NJ_CHAR));
                    /* 文字列の一致を調べる */
                    char_match = 1;
                    for (lop = 0; lop < char_len; lop++, wkc += sizeof(NJ_CHAR)) {
                        diff = NJ_CHAR_DIFF(yomi_tmp + lop, wkc);
                        if (diff < 0) {
                            right = mid - 1;
                            char_match = 0;
                            break;
                        } else if (diff > 0) {
                            left = mid + 1;
                            char_match = 0;
                            break;
                        }
                    }
                    if (char_match) {
                        findflg = 1;
                        top_tmp = mid;
                        right = mid - 1;
                    }
                } else {
                    left = mid + 1;
                }
            }
            /* 該当範囲に検索対象読み文字列が見つからなかった場合 */
            if (findflg == 0) {
                /* 検索対象なしとする */
                return -1;
            }

            /* 同表記文字の単語データがある為、終端を検索し、
             * その表記インデックステーブルデータ番号を取得するものとする。 */
            left = top_tmp;
            right = bottom_tmp;
            findflg = 0;
            while (left <= right) {
                mid = left + ((right - left) / 2);
                wdtbl_tmp = WORD_DATA_ADDR(wdtbl_addr, mid);
                /* 読み文字配列長を取得する */
                wkc_len = (NJ_INT16)YOMI_LEN(wdtbl_tmp);
                /* 取得した読み文字配列長が、検索開始位置より大きい場合、
                 * 確認処理を行う。 */
                if (wkc_len > rtn) {
                    /* 読み文字列位置を取得する */
                    wkc = yhtbl_addr + YOMI_DATA_OFFSET(wdtbl_tmp) + (rtn * sizeof(NJ_CHAR));
                    /* 文字列の一致を調べる */
                    char_match = 1;
                    for (lop = 0; lop < char_len; lop++, wkc += sizeof(NJ_CHAR)) {
                        diff = NJ_CHAR_DIFF(yomi_tmp + lop, wkc);
                        if (diff < 0) {
                            right = mid - 1;
                            char_match = 0;
                            break;
                        } else if (diff > 0) {
                            left = mid + 1; /*NCH_DEF*/
                            char_match = 0; /*NCH_DEF*/
                            break; /*NCH_DEF*/
                        }
                    }
                    if (char_match) {
                        findflg = 1;
                        bottom_tmp = mid;
                        left = mid + 1;
                    }
                } else {
                    right = mid - 1; /*NCH_DEF*/
                }
            }
            /* 該当範囲に検索対象読み文字列が見つからなかった場合 */
            if (findflg == 0) {
                /* 検索対象なしとする */
                return -1; /*NCH_DEF*/
            }

            /* 次検索読み文字情報をセット */
            yomi_tmp += char_len;        /* 読み文字 */
            rtn += char_len;             /* 検索文字位置 */
            (*word_clen)++;              /* 検索対象文字数 */
            left = top_tmp;
            right = bottom_tmp;
        }
    } else {
        /* 正引き完全一致検索 */

        /* 検索対象読み文字列を検索し、topを取得する */
        mid_bak = 0;
        findflg = 0;
        while (left <= right) {
            mid = left + ((right - left) / 2);

            /* 対象の読み文字列を取得する */
            wdtbl_tmp = WORD_DATA_ADDR(wdtbl_addr, mid);
            wkc       = yhtbl_addr + YOMI_DATA_OFFSET(wdtbl_tmp);
            wkc_len   = YOMI_LEN(wdtbl_tmp);

            /* 文字列の比較処理 */
            diff = NJ_CHAR_DIFF(yomi, wkc);
            if (diff == 0) {
                if (wkc_len < yomilen) {
                    /*検索キーの方が長い場合*/
                    diff = nj_memcmp((NJ_UINT8*)yomi, wkc, (NJ_UINT16)(wkc_len * sizeof(NJ_CHAR)));
                    if (diff == 0) {
                        diff = 1;
                    }
                } else {
                    /*検索キーの方が短い or 同じ長さの場合*/
                    diff = nj_memcmp((NJ_UINT8*)yomi, wkc, (NJ_UINT16)(yomilen * sizeof(NJ_CHAR)));
                }
            }
            /* 二分検索範囲更新 */
            if (diff == 0) {
                if (wkc_len == yomilen) {
                    /* 完全一致の場合、該当の単語データ番号を保存 */
                    top_tmp = mid;
                    if (mid_bak < mid) {
                        mid_bak = mid;
                        bottom_tmp = right;
                    }
                }
                findflg = 1;
                right = mid - 1;
            } else if (diff < 0) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        /* 該当範囲に検索対象読み文字列が見つからなかった場合 */
        if (mid_bak == 0) {
            if (findflg) {
                /* 検索対象なし(前方一致有)とする */
                return -2;
            } else {
                if (left > src_bottomno) {
                    /* leftが検索範囲終端を越えている場合 */
                    /* 検索対象なしとする */
                    return -1;
                } else {
                    /* leftが前方一致していないかチェック */

                    /* 対象の読み文字列を取得する */
                    wdtbl_tmp = WORD_DATA_ADDR(wdtbl_addr, left);
                    wkc       = yhtbl_addr + YOMI_DATA_OFFSET(wdtbl_tmp);
                    wkc_len   = YOMI_LEN(wdtbl_tmp);

                    if (wkc_len >= yomilen) {
                        if (nj_memcmp((NJ_UINT8*)yomi, wkc, (NJ_UINT16)(yomilen * sizeof(NJ_CHAR))) == 0) {
                            /* 検索対象なし(前方一致有)とする */
                            return -2; /*NCH_DEF*/
                        }
                    }
                    /* 検索対象なしとする */
                    return -1;
                }
            }
        }

        /* 同表記文字の単語データがある為、終端を検索し、
         * その表記インデックステーブルデータ番号を取得するものとする。 */
        left  = mid_bak;
        right = bottom_tmp;
        bottom_tmp = 0;

        while (left <= right) {
            mid = left + ((right - left) / 2);

            /* 対象の読み文字列を取得する */
            wdtbl_tmp = WORD_DATA_ADDR(wdtbl_addr, mid);
            wkc       = yhtbl_addr + YOMI_DATA_OFFSET(wdtbl_tmp) + startpos;
            wkc_len   = YOMI_LEN(wdtbl_tmp);

            /* 文字列の比較処理 */
            diff = NJ_CHAR_DIFF(yomi, wkc);
            if (diff == 0) {
                if (wkc_len < yomilen) {
                    /*検索キーの方が長い場合*/
                    diff = nj_memcmp((NJ_UINT8*)yomi, wkc, (NJ_UINT16)(wkc_len * sizeof(NJ_CHAR)));
                    if (diff == 0) {
                        diff = 1; /*NCH_DEF*/
                    }
                } else {
                    /*検索キーの方が短い or 同じ長さの場合*/
                    diff = nj_memcmp((NJ_UINT8*)yomi, wkc, (NJ_UINT16)(yomilen * sizeof(NJ_CHAR)));
                }
            }

            /* 二分検索範囲の更新 */
            if (diff == 0) {
                /* 該当の単語データ番号を保存 */
                if (wkc_len == yomilen) {
                    bottom_tmp = mid;
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            } else if (diff < 0) {
                right = mid - 1;
            } else {
                left = mid + 1; /*NCH_DEF*/
            }
        }

        /* 該当範囲に検索対象読み文字列が見つからなかった場合 */
        if (bottom_tmp == 0) {
            /* 検索対象なしとする */
            return -1; /*NCH_DEF*/
        } else {
            rtn = yomilen;
        }
    }

    /* 辞書情報を取得 */
    wdtbl_addr = HINDO_TOP_ADDR(hdl);

    /* 検索範囲制限のチェック処理 */
    if (check == (-1)) {
        /* 制限なし */
        findflg = 1;
    } else if (check == NJ_MODE_TYPE_YOSOKU) {
        /* 頻度学習値利用に必要なデータを取得する */
        if (ext_area != NULL) {
            ext_top = EXT_AREA_TOP_ADDR((NJ_UINT8 *)ext_area, hdl);
        } else {
            ext_top = NULL;
        }

        /* 予測のみ */
        findflg = 0;
        for (lop = top_tmp; lop <= bottom_tmp; lop++) {
            if (IS_USED_FOR_FORECAST(lop, wdtbl_addr, ext_top, 3)) {
                /* 予測利用語彙が含まれた場合、ループを抜ける */
                findflg = 1;
                break;
            }
        }
    } else {
        /* 変換のみ */
        /* 本処理は、形態素解析では通らない */
        findflg = 0; /*NCH*/
        for (lop = top_tmp; lop <= bottom_tmp; lop++) { /*NCH*/
            wdtbl_tmp = HINDO_HENKAN_ADDR(wdtbl_addr, lop); /*NCH*/
            if (HINDO_HENKAN_ON(wdtbl_tmp)) { /*NCH*/
                /* 変換利用語彙が含まれた場合、ループを抜ける */
                findflg = 1; /*NCH*/
                break; /*NCH*/
            }
        }
    }

    if (findflg) {
        /* 単語データ番号の先頭と終端をセット */
        *dst_topno = top_tmp;
        *dst_bottomno = bottom_tmp;
        
        /* 候補有り */
        return rtn;
    }

    /* 候補なし */
    return -1;
}


/**
 * 検索対象文字を対象単語データ領域中から検索し、該当する単語データの先頭,終端番号を取得する。
 *
 * @attention 前方一致検索の時は、yomiを１文字ずつ指定して、startposをずらしながら複数回検索する。
 *
 * @param[in]           hdl  辞書ハンドル
 * @param[in]      ext_area  頻度学習領域
 * @param[in]          mode  検索条件
 * @param[in]     src_topno  検索対象単語データ先頭番号
 * @param[in]  src_bottomno  検索対象単語データ終端番号
 * @param[in]          yomi  検索する単語の読み文字
 * @param[in]       yomilen  検索する単語の読み文字配列長
 * @param[in]      startpos  単語データ検索開始位置(文字配列要素番号)
 * @param[in]         check  検索範囲制限
 * @param[out]    dst_topno  取得単語データ先頭番号
 * @param[out] dst_bottomno  取得単語データ終端番号
 * @param[out]    word_clen  検索対象文字数
 *
 * @retval               -1   該当データなし
 * @retval               -2   該当データなし(前方一致有)
 * @retval              >=0   検索する単語の読み文字の読み文字配列長
 */
static NJ_INT16 search_word_no_high_compress(NJ_DIC_HANDLE hdl, NJ_VOID *ext_area, NJ_UINT8 mode,
                               NJ_UINT32 src_topno, NJ_UINT32 src_bottomno,
                               NJ_CHAR  *yomi, NJ_UINT16 yomilen,
                               NJ_INT16 startpos, NJ_INT16 check,
                               NJ_UINT32 *dst_topno, NJ_UINT32 *dst_bottomno,
                               NJ_INT32 *word_clen) {

    NJ_INT16 char_len;                                  /* 文字Length格納用 */
    NJ_UINT16 ylen;                                     /* 検索対象文字列長操作用 */
    NJ_CHAR  *yomi_tmp;                                 /* 読み文字列操作用 */
    NJ_UINT32 left, right, mid, mid_bak;                /* binary search用index */
    NJ_UINT8 *wdtbl_addr, *wdtbl_tmp;                   /* 単語データ領域操作用 */
    NJ_INT16 rtn;                                       /* 関数戻り値 */
    NJ_UINT16 wkc_len;                                  /* 単語データ */
    NJ_UINT8 *yhtbl_addr;                               /* 読み・表記データ領域先頭アドレス */
    NJ_UINT8  *wkc;                                     /* 辞書登録語の読み */
    NJ_INT32 top_tmp = src_topno;                       /* 単語データ */
    NJ_INT32 bottom_tmp = src_bottomno;                 /* 検索文字End値取得用 */
    NJ_INT32 lop;                                      /* ループカウンター */
    NJ_UINT8 *ext_top;
    NJ_UINT8 *yomi_str = NULL;                          /* 検索する単語の読み文字 */
    NJ_UINT8 v_yomi[NJ_MAX_LEN + NJ_TERM_LEN];          /* 検索する単語の読み文字配列 */
    NJ_INT16 diff, conv_proc_len;
    NJ_UINT8 findflg;
    NJ_UINT8 char_match;


    if ((src_topno == 0) || (src_bottomno == 0) || (src_topno > src_bottomno)) {
        /* 検索対象なしとする */
        *dst_topno = 0; /*NCH*/
        *dst_bottomno = 0; /*NCH*/
        return -1; /*NCH*/
    }

    /* 単語データ領域の先頭アドレスを取得する */
    wdtbl_addr = WORD_TOP_ADDR(hdl);
    yhtbl_addr = YOMI_HYOKI_TOP_ADDR(hdl);

    /* 初期情報設定 */
    *word_clen = 0;
    yomi_tmp = yomi;
    rtn   = startpos;
    left  = src_topno;
    right = src_bottomno;

    /* 仮想文字コードに変換 */
    if ((conv_proc_len = convert_str_real_to_virtual(hdl, yomi, yomilen, v_yomi, sizeof(v_yomi))) == -1) {
        /*
         *  変換文字が辞書に存在しないため
         *  無条件で検索対象なしとする
        */
        return -1;
    }
    yomi_str = v_yomi;
    /* yomi_top にある１文字の文字配列要素数 */
    char_len = sizeof(NJ_UINT8);
    if (conv_proc_len != yomilen) {
        /* 変換後の仮想文字列配列長に変更する */
        yomilen = conv_proc_len;
    }

    if (mode == NJ_CUR_OP_FORE) {
        /* 前方一致検索 */
        for (ylen = 0; ylen < yomilen; ylen += char_len) {

            /* 検索対象読み文字列を検索し、topを取得する */
            findflg = 0;
            while (left <= right) {
                mid = left + ((right - left) / 2);
                wdtbl_tmp = WORD_DATA_ADDR_HIGH_COMPRESS(wdtbl_addr, mid);
                /* 読み文字配列長を取得する */
                wkc_len = (NJ_INT16)YOMI_LEN_BYTE(wdtbl_tmp);
                /* 取得した読み文字配列長が、検索開始位置より大きい場合、
                 * 確認処理を行う。 */
                if (wkc_len > rtn) {
                    /* 読み文字列位置を取得する(unsigned char*) */
                    wkc = yhtbl_addr + YOMI_DATA_OFFSET(wdtbl_tmp) + rtn;
                    /* 文字列の一致を調べる */
                    char_match = 1;
                    diff = NJ_BYTE_DIFF(yomi_str, wkc);
                    if (diff < 0) {
                        right = mid - 1;
                        char_match = 0;
                    } else if (diff > 0) {
                        left = mid + 1;
                        char_match = 0;
                    }
                    if (char_match) {
                        findflg = 1;
                        top_tmp = mid;
                        right = mid - 1;
                    }
                } else {
                    left = mid + 1;
                }
            }
            /* 該当範囲に検索対象読み文字列が見つからなかった場合 */
            if (findflg == 0) {
                /* 検索対象なしとする */
                return -1;
            }

            /* 同表記文字の単語データがある為、終端を検索し、
             * その表記インデックステーブルデータ番号を取得するものとする。 */
            left = top_tmp;
            right = bottom_tmp;
            findflg = 0;
            while (left <= right) {
                mid = left + ((right - left) / 2);
                wdtbl_tmp = WORD_DATA_ADDR_HIGH_COMPRESS(wdtbl_addr, mid);
                /* 読み文字配列長を取得する */
                wkc_len = (NJ_INT16)YOMI_LEN_BYTE(wdtbl_tmp);
                /* 取得した読み文字配列長が、検索開始位置より大きい場合、
                 * 確認処理を行う。 */
                if (wkc_len > rtn) {
                    /* 読み文字列位置を取得する */
                    wkc = yhtbl_addr + YOMI_DATA_OFFSET(wdtbl_tmp) + rtn;
                    /* 文字列の一致を調べる */
                    char_match = 1;
                    diff = NJ_BYTE_DIFF(yomi_str, wkc);
                    if (diff < 0) {
                        right = mid - 1;
                        char_match = 0;
                    } else if (diff > 0) {
                        left = mid + 1; /*NCH_DEF*/
                        char_match = 0; /*NCH_DEF*/
                    }
                    if (char_match) {
                        findflg = 1;
                        bottom_tmp = mid;
                        left = mid + 1;
                    }
                } else {
                    right = mid - 1; /*NCH_DEF*/
                }
            }
            /* 該当範囲に検索対象読み文字列が見つからなかった場合 */
            if (findflg == 0) {
                /* 検索対象なしとする */
                return -1; /*NCH_DEF*/
            }

            /* 次検索読み文字情報をセット */
            yomi_str += char_len;        /* 読み文字 */
            rtn += char_len;             /* 検索文字位置 */
            (*word_clen)++;              /* 検索対象文字数 */
            left = top_tmp;
            right = bottom_tmp;
        }
    } else {
        /* 正引き完全一致検索 */

        /* 検索対象読み文字列を検索し、topを取得する */
        mid_bak = 0;
        findflg = 0;
        while (left <= right) {
            mid = left + ((right - left) / 2);

            /* 対象の読み文字列を取得する */
            wdtbl_tmp = WORD_DATA_ADDR_HIGH_COMPRESS(wdtbl_addr, mid);
            wkc       = yhtbl_addr + YOMI_DATA_OFFSET(wdtbl_tmp);
            wkc_len   = YOMI_LEN_BYTE(wdtbl_tmp);

            /* 文字列の比較処理 */
            diff = NJ_BYTE_DIFF(yomi_str, wkc);
            if (diff == 0) {
                if (wkc_len < yomilen) {
                    /*検索キーの方が長い場合*/
                    diff = nj_memcmp((NJ_UINT8*)yomi_str, wkc, wkc_len);
                    if (diff == 0) {
                        diff = 1;
                    }
                } else {
                    /*検索キーの方が短い or 同じ長さの場合*/
                    diff = nj_memcmp((NJ_UINT8*)yomi_str, wkc, yomilen);
                }
            }

            /* 二分検索範囲更新 */
            if (diff == 0) {
                if (wkc_len == yomilen) {
                    /* 完全一致の場合、該当の単語データ番号を保存 */
                    top_tmp = mid;
                    if (mid_bak < mid) {
                        mid_bak = mid;
                        bottom_tmp = right;
                    }
                }
                findflg = 1;
                right = mid - 1;
            } else if (diff < 0) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        /* 該当範囲に検索対象読み文字列が見つからなかった場合 */
        if (mid_bak == 0) {
            if (findflg) {
                /* 検索対象なし(前方一致有)とする */
                return -2;
            } else {
                if (left > src_bottomno) {
                    /* leftが検索範囲終端を越えている場合 */
                    /* 検索対象なしとする */
                    return -1; /*NCH_DEF*/
                } else {
                    /* leftが前方一致していないかチェック */

                    /* 対象の読み文字列を取得する */
                    wdtbl_tmp = WORD_DATA_ADDR_HIGH_COMPRESS(wdtbl_addr, left);
                    wkc       = yhtbl_addr + YOMI_DATA_OFFSET(wdtbl_tmp);
                    wkc_len   = YOMI_LEN_BYTE(wdtbl_tmp);

                    if (wkc_len >= yomilen) {
                        if (nj_memcmp((NJ_UINT8*)yomi_str, wkc, (NJ_UINT16)(yomilen * sizeof(NJ_UINT8))) == 0) {
                            /* 検索対象なし(前方一致有)とする */
                            return -2;
                        }
                    }
                    /* 検索対象なしとする */
                    return -1;
                }
            }
        }

        /* 同表記文字の単語データがある為、終端を検索し、
         * その表記インデックステーブルデータ番号を取得するものとする。 */
        left  = mid_bak;
        right = bottom_tmp;
        bottom_tmp = 0;

        while (left <= right) {
            mid = left + ((right - left) / 2);

            /* 対象の読み文字列を取得する */
            wdtbl_tmp = WORD_DATA_ADDR_HIGH_COMPRESS(wdtbl_addr, mid);
            wkc       = yhtbl_addr + YOMI_DATA_OFFSET(wdtbl_tmp) + startpos;
            wkc_len   = YOMI_LEN_BYTE(wdtbl_tmp);

            /* 文字列の比較処理 */
            diff = NJ_BYTE_DIFF(yomi_str, wkc);
            if (diff == 0) {
                if (wkc_len < yomilen) {
                    /*検索キーの方が長い場合*/
                    diff = nj_memcmp((NJ_UINT8*)yomi_str, wkc, wkc_len);
                    if (diff == 0) {
                        diff = 1; /*NCH_DEF*/
                    }
                } else {
                    /*検索キーの方が短い or 同じ長さの場合*/
                    diff = nj_memcmp((NJ_UINT8*)yomi_str, wkc, yomilen);
                }
            }

            /* 二分検索範囲の更新 */
            if (diff == 0) {
                /* 該当の単語データ番号を保存 */
                if (wkc_len == yomilen) {
                    bottom_tmp = mid;
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            } else if (diff < 0) {
                right = mid - 1;
            } else {
                left = mid + 1; /*NCH_DEF*/
            }
        }

        /* 該当範囲に検索対象読み文字列が見つからなかった場合 */
        if (bottom_tmp == 0) {
            /* 検索対象なしとする */
            return -1; /*NCH_DEF*/
        } else {
            rtn = yomilen;
        }
    }

    /* 辞書情報を取得 */
    wdtbl_addr = HINDO_TOP_ADDR(hdl);

    /* 検索範囲制限のチェック処理 */
    if (check == (-1)) {
        /* 制限なし */
        findflg = 1;
    } else if (check == NJ_MODE_TYPE_YOSOKU) {
        /* 頻度学習値利用に必要なデータを取得する */
        if (ext_area != NULL) {
            ext_top = EXT_AREA_TOP_ADDR((NJ_UINT8 *)ext_area, hdl);
        } else {
            ext_top = NULL;
        }

        /* 予測のみ */
        findflg = 0;
        for (lop = top_tmp; lop <= bottom_tmp; lop++) {
            if (IS_USED_FOR_FORECAST(lop, wdtbl_addr, ext_top, 3)) {
                /* 予測利用語彙が含まれた場合、ループを抜ける */
                findflg = 1;
                break;
            }
        }
    } else {
        /* 変換のみ */
        /* 本処理は、形態素解析では通らない */
        findflg = 0; /*NCH*/
        for (lop = top_tmp; lop <= bottom_tmp; lop++) { /*NCH*/
            wdtbl_tmp = HINDO_HENKAN_ADDR(wdtbl_addr, lop); /*NCH*/
            if (HINDO_HENKAN_ON(wdtbl_tmp)) { /*NCH*/
                /* 変換利用語彙が含まれた場合、ループを抜ける */
                findflg = 1; /*NCH*/
                break; /*NCH*/
            }
        }
    }

    if (findflg) {
        /* 単語データ番号の先頭と終端をセット */
        *dst_topno = top_tmp;
        *dst_bottomno = bottom_tmp;
        
        /* 候補有り */
        return rtn;
    }

    /* 候補なし */
    return -1; /*NCH_DEF*/
}


/**
 * 逆引き前方一致検索−あいまい予測時統合辞書から検索条件（表記）に該当する単語を検索し、該当する候補データの情報を取得
 *
 * @param[in]          iwnn : 解析情報クラス
 * @param[in]     condition : 検索条件
 * @param[in,out]   loctset : 検索位置
 * @param[in]          hidx : 辞書のインデックス
 *
 * @retval                0   検索候補なし
 *                        1   検索候補あり
 */
static NJ_INT16 search_node_rev_aimai(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 hidx) {
    NJ_CHAR  *yomi;
    NJ_SEARCH_CACHE *psrhCache = condition->ds->dic[hidx].srhCache;
    NJ_CHAR  *key;
    NJ_UINT8 cmpflg;
    NJ_UINT8 endflg;
    NJ_UINT16 abPtrIdx;
    NJ_UINT16 key_len;
    NJ_UINT16 i, l, m, cur_tmp = 0;
    NJ_UINT16 abIdx;
    NJ_UINT16 abIdx_old;
    NJ_UINT16 addcnt = 0;
    NJ_CHAR   char_tmp[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_UINT16 tmp_len;
    NJ_UINT16 endIdx = 0;
    NJ_INT16 ret = 0;
    NJ_UINT16 yomi_clen;
    NJ_UINT8 aimai_flg = 0x01;
    NJ_CHAR   key_tmp[NJ_MAX_CHAR_LEN + NJ_TERM_LEN];

    NJ_UINT32 word_top = 0, word_bottom = 0;
    NJ_UINT8 *hidx_cur;
    NJ_UINT8 *hitbl_addr;
    NJ_UINT32 search_top, search_bottom;
    NJ_INT32 hindo_max, hindo_tmp, hindo_cur;
    NJ_UINT8 no_used_flg;
#ifdef NJ_OPT_CHARSET_2BYTE
    NJ_UINT16 start;
    NJ_UINT16 end;
#endif /* NJ_OPT_CHARSET_2BYTE */


    /* 曖昧検索は、頻度順検索のみの為、
     * 表記順検索(頻度順検索以外)の場合は検索候補なしとする */
    if (condition->mode != NJ_CUR_MODE_FREQ) {
        /* 検索候補なし */
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_DEF*/
        return 0; /*NCH_DEF*/
    }

    if (NJ_GET_CACHEOVER_FROM_SCACHE(psrhCache) != 0) {
        aimai_flg = 0x00;
    }

    /* 検索対象読み文字列 */
    yomi = condition->yomi;

    /* ループ処理前の初期化 */
    endflg = 0x00;
    cmpflg = 0x00;
    abPtrIdx = 0;
    key = condition->ds->keyword;

    /* 文字数分、曖昧検索を行う。*/
    yomi_clen = condition->yclen;
    for (i = 0; i < yomi_clen; i++) {
        /* ポインターインデックスの格納 */
        abPtrIdx = i;

        /* インデックス情報の構築 */
        if (!cmpflg) {    /* cmpflg == 0x00 */
            /* 一文字分を比較してOKだった。 */
            if (((abPtrIdx != 0) && (psrhCache->keyPtr[abPtrIdx] == 0))
                || (psrhCache->keyPtr[abPtrIdx + 1] == 0)) {
                /* インデックス情報がない */
                cmpflg = 0x01;
            } else {
                /* インデックス情報あり */
            }
        }

        addcnt = 0;
        if (cmpflg) {    /* cmpflg == 0x01 */
            /* インデックス情報なしと判定された場合 */
            if (abPtrIdx == 0) {
                /* 初回のインデックス情報を作成する。 */
                abIdx = 0;
                /* 一文字分をコピーする */
                nj_charncpy(key_tmp, yomi, 1);
                key_len = nj_strlen(key_tmp);

                psrhCache->keyPtr[0] = 0;
                if (condition->operation == NJ_CUR_OP_FORE_EXT) {
                    /* 正引き前方一致拡張検索の場合 */
                    if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
                        search_top = 1;
                        search_bottom = (NJ_UINT32)EXT_HYOKI_IDX_AREA_CNT((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT]);
                    } else {
                        /* 検索候補なし */
                        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH*/
                        return 0; /*NCH*/
                    }
                } else {
                    /* 逆引き前方一致検索の場合 */
                    if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO] != NULL) {
                        search_top = 1;
                        search_bottom = (NJ_UINT32)EXT_HYOKI_IDX_AREA_CNT((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO]);
                    } else {
                        search_top = 1;
                        search_bottom = (NJ_UINT32)(HYOKI_IDX_AREA_CNT(loctset->loct.handle));
                    }
                }

                /* 表記文字列から表記文字列インデックステーブルを検索し、
                 * 該当する単語データ番号の先頭と終端を検索する */
                ret = search_rev_idx(loctset, condition, key_tmp, key_len, 
                                     search_top, search_bottom, 
                                     0, &word_top, &word_bottom);

                if (ret < 0) {
                    /* 存在しなかった場合 */
                } else {
                    /* 存在した場合 */

                    /* 単語データ番号の先頭,終端番号をセット */
                    psrhCache->storebuff[abIdx].top = word_top;
                    psrhCache->storebuff[abIdx].bottom = word_bottom;
                    psrhCache->storebuff[abIdx].idx_no = key_len;
                    addcnt++;
                    abIdx++;
                }

                if ((condition->charset != NULL) && aimai_flg) {
                    /* 曖昧検索処理 */
#ifdef NJ_OPT_CHARSET_2BYTE
                    /* keyに一致するあいまい文字セットの範囲を２分検索する */
                    if (njd_search_charset_range(condition->charset, key, &start, &end) == 1) {
                        /* 範囲が見つかった */
                        for (l = start; l <= end; l++) {
#else /* NJ_OPT_CHARSET_2BYTE */
                    for (l = 0; l < condition->charset->charset_count; l++) {
                        /* 曖昧検索文字の抽出 */
                        if (nj_charncmp(key, condition->charset->from[l], 1) == 0) {
#endif /* NJ_OPT_CHARSET_2BYTE */
                            /* 一致しているなら */
                            nj_strcpy(char_tmp, condition->charset->to[l]);
                            tmp_len = nj_strlen(char_tmp);

                            if (condition->operation == NJ_CUR_OP_FORE_EXT) {
                                /* 正引き前方一致拡張検索の場合 */
                                if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
                                    search_top = 1;
                                    search_bottom = (NJ_UINT32)EXT_HYOKI_IDX_AREA_CNT((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT]);
                                } else {
                                    search_top = 1; /*NCH_DEF*/
                                    search_bottom = (NJ_UINT32)(HYOKI_IDX_AREA_CNT(loctset->loct.handle)); /*NCH_DEF*/
                                }
                            } else {
                                /* 逆引き前方一致検索の場合 */
                                if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO] != NULL) {
                                    search_top = 1; /*NCH_DEF*/
                                    search_bottom = (NJ_UINT32)EXT_HYOKI_IDX_AREA_CNT((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO]); /*NCH_DEF*/
                                } else {
                                    search_top = 1;
                                    search_bottom = (NJ_UINT32)(HYOKI_IDX_AREA_CNT(loctset->loct.handle));
                                }
                            }

                            /* 表記文字列から表記文字列インデックステーブルを検索し、
                             * 該当する単語データ番号の先頭と終端を検索する */
                            ret = search_rev_idx(loctset, condition, char_tmp, tmp_len,
                                                 search_top, search_bottom, 
                                                 0, &word_top, &word_bottom);

                            if (ret < 0) {
                                /* 存在しなかった場合 */
                            } else {
                                /* 存在した場合 */

                                /* 単語データ番号の先頭,終端番号をセット */
                                psrhCache->storebuff[abIdx].top = word_top;
                                psrhCache->storebuff[abIdx].bottom = word_bottom;
                                psrhCache->storebuff[abIdx].idx_no = tmp_len;

                                /* キャッシュ溢れ発生 */
                                if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                    psrhCache->keyPtr[abPtrIdx+1] = 0; /*NCH_DEF*/
                                    return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_NOT_ENOUGH); /*NCH_DEF*/
                                }
                                addcnt++;
                                abIdx++;
                            }
                        }
                    }
                }
                psrhCache->keyPtr[abPtrIdx + 1] = abIdx;
            } else {
                /* 現在のインデックス情報から検索を行う。 */
                /* 一文字分をコピーする */
                nj_charncpy(key_tmp, yomi, 1);
                key_len = nj_strlen(key_tmp);

                if (psrhCache->keyPtr[abPtrIdx] == psrhCache->keyPtr[abPtrIdx - 1]) {
                    /* 検索結果が存在しない */
                    psrhCache->keyPtr[abPtrIdx+1] = psrhCache->keyPtr[abPtrIdx-1]; /*NCH_FB*/
                    endflg = 0x01; /*NCH_FB*/
                } else {
                    /* 検索結果が存在する */
                    /* インデックス有効数を取得 */
                    endIdx = psrhCache->keyPtr[abPtrIdx];
                    abIdx_old = psrhCache->keyPtr[abPtrIdx - 1];

                    if (NJ_GET_CACHEOVER_FROM_SCACHE(psrhCache) != 0) {
                        abIdx = psrhCache->keyPtr[abPtrIdx - 1];
                        psrhCache->keyPtr[abPtrIdx] = abIdx;
                    } else {
                        abIdx = psrhCache->keyPtr[abPtrIdx];
                    }

                    if ((abIdx > NJ_SEARCH_CACHE_SIZE) || (abIdx_old >= NJ_SEARCH_CACHE_SIZE) ||
                        (endIdx > NJ_SEARCH_CACHE_SIZE)) {
                        /* キャッシュが破壊されている */
                        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_BROKEN); /*NCH_FB*/
                    }

                    for (m = abIdx_old; m < endIdx; m++) {
                        search_top = psrhCache->storebuff[m].top;
                        search_bottom = psrhCache->storebuff[m].bottom;

                        /* 表記文字列から表記文字列インデックステーブルを検索し、
                         * 該当する単語データ番号の先頭と終端を検索する */
                        ret = search_rev_idx(loctset, condition, key_tmp, key_len,
                                             search_top, search_bottom, 
                                             psrhCache->storebuff[m].idx_no, &word_top, &word_bottom);

                        if (ret < 0) {
                            /* 存在しなかった場合 */
                        } else {
                            /* 存在した場合 */
                            /* キャッシュ溢れ発生 */
                            if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                psrhCache->keyPtr[abPtrIdx+1] = 0;
                                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_NOT_ENOUGH);
                            }

                            /* 単語データ番号の先頭,終端番号をセット */
                            psrhCache->storebuff[abIdx].top = word_top;
                            psrhCache->storebuff[abIdx].bottom = word_bottom;
                            psrhCache->storebuff[abIdx].idx_no = psrhCache->storebuff[m].idx_no + key_len;
                            addcnt++;
                            abIdx++;
                        }

                        if ((condition->charset != NULL) && aimai_flg) {
                            /* 曖昧検索処理 */
#ifdef NJ_OPT_CHARSET_2BYTE
                            /* keyに一致するあいまい文字セットの範囲を２分検索する */
                            if (njd_search_charset_range(condition->charset, key, &start, &end) == 1) {
                                /* 範囲が見つかった */
                                for (l = start; l <= end; l++) {
#else /* NJ_OPT_CHARSET_2BYTE */
                            for (l = 0; l < condition->charset->charset_count; l++) {
                                /* 曖昧検索文字の抽出 */
                                if (nj_charncmp(key, condition->charset->from[l], 1) == 0) {
#endif /* NJ_OPT_CHARSET_2BYTE */
                                    /* 一致しているなら */
                                    nj_strcpy(char_tmp, condition->charset->to[l]);
                                    tmp_len = nj_strlen(char_tmp);

                                    search_top = psrhCache->storebuff[m].top;
                                    search_bottom = psrhCache->storebuff[m].bottom;

                                    /* 表記文字列から表記文字列インデックステーブルを検索し、
                                     * 該当する単語データ番号の先頭と終端を検索する */
                                    ret = search_rev_idx(loctset, condition, char_tmp, tmp_len,
                                                         search_top, search_bottom, 
                                                         psrhCache->storebuff[m].idx_no, &word_top, &word_bottom);

                                    if (ret < 0) {
                                        /* 存在しなかった場合 */
                                    } else {
                                        /* 存在した場合 */
                                        /* キャッシュ溢れ発生 */
                                        if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                            psrhCache->keyPtr[abPtrIdx+1] = 0; /*NCH_DEF*/
                                            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_NOT_ENOUGH); /*NCH_DEF*/
                                        }

                                        /* 単語データ番号の先頭,終端番号をセット */
                                        psrhCache->storebuff[abIdx].top = word_top;
                                        psrhCache->storebuff[abIdx].bottom = word_bottom;
                                        psrhCache->storebuff[abIdx].idx_no = psrhCache->storebuff[m].idx_no + tmp_len;

                                        addcnt++;
                                        abIdx++;
                                    }
                                }
                            }
                        }
                    }
                    psrhCache->keyPtr[abPtrIdx + 1] = abIdx;
                }
            }
        }
        yomi += NJ_CHAR_LEN(yomi);
        key  += NJ_CHAR_LEN(key);
    }
    /* 今回の検索結果件数が0かつキャッシュに候補がない（検索対象読み文字列長-1の開始位置と終了位置が同じ）場合に終了 */
    if ((addcnt == 0) && (psrhCache->keyPtr[yomi_clen - 1] == psrhCache->keyPtr[yomi_clen])) {
        endflg = 0x01;
    }

    if (endflg) {        /* endflg == 0x01 */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    loctset->loct.current = 0;

    /**************************************************/
    /* 頻度値の取得処理                               */
    /**************************************************/

    /* 有効インデックスを取得する */
    abPtrIdx = condition->yclen;

    /* 開始・終了インデックスを取得する */
    abIdx = psrhCache->keyPtr[abPtrIdx];
    abIdx_old = psrhCache->keyPtr[abPtrIdx - 1];
    if (condition->operation == NJ_CUR_OP_FORE_EXT) {
        /* 正引き前方一致拡張検索の場合 */
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT]);
        } else {
            loctset->loct.status = NJ_ST_SEARCH_END;    /*NCH*/
            return 0;                                   /*NCH*/
        }
    } else {
        /* 逆引き前方一致検索の場合 */
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO] != NULL) {
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO]);
        } else {
            hitbl_addr = HYOKI_IDX_AREA_TOP_ADDR(loctset->loct.handle);
        }
    }

    if ((abIdx > NJ_SEARCH_CACHE_SIZE) || (abIdx_old >= NJ_SEARCH_CACHE_SIZE)) {
        /* キャッシュが破壊されている */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_BROKEN); /*NCH_FB*/
    }

    hindo_max = INIT_HINDO;
    hindo_tmp = INIT_HINDO;
    cur_tmp = 0;
    /* 取得した候補の中から最高頻度の候補を探す。 */
    switch (condition->operation) {
    case NJ_CUR_OP_FORE_EXT:
        /* 正引き前方一致拡張検索の場合 */
        for (m = abIdx_old; m < abIdx; m++) {
            /* 一番頻度が高い単語を記憶する。(予測利用ONの単語を検索) */
            search_top    = psrhCache->storebuff[m].top;
            search_bottom = psrhCache->storebuff[m].bottom;
            hindo_tmp = INIT_HINDO;
            while (search_top <= search_bottom) {
                hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, search_top);
                no_used_flg = 0;
                if ((condition->ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) {
                    /* 単語使用有無の確認 */
                    if ((HINDO_EXT_AREA_YOSOKU(hidx_cur)) &&
                        (is_funfun_ok_ext(loctset->loct.handle, HYOKI_INDEX_WORD_NO(hidx_cur), iwnn->environment.option.char_min, iwnn->environment.option.char_max))) {
                        no_used_flg = 1;
                    }
                } else {
                    /* 辞書引きAPIからのみ通る */
                    no_used_flg = 1;
                }
                if (!no_used_flg) {
                    search_top++;
                    continue;
                }

                hindo_cur = HINDO_DATA(hidx_cur);
                if (hindo_tmp < hindo_cur) {
                    hindo_tmp = hindo_cur;
                    psrhCache->storebuff[m].current = search_top;
                }
                search_top++;
            }

            /* 最大頻度値、最大頻度データ位置 更新 */
            if (hindo_tmp > hindo_max) {
                cur_tmp = m;
                hindo_max = hindo_tmp;
            }
        }
        break;

    default:
        /* 逆引き完全一致, 逆引き前方一致検索の場合 */
        for (m = abIdx_old; m < abIdx; m++) {
            /* 一番頻度が高い単語を記憶する。 */
            psrhCache->storebuff[m].current = psrhCache->storebuff[m].top;
            hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, psrhCache->storebuff[m].current);
            hindo_tmp = HINDO_MORPHO(hidx_cur);
            if (condition->operation == NJ_CUR_OP_REV_FORE) {
                search_top    = psrhCache->storebuff[m].top + 1;
                search_bottom = psrhCache->storebuff[m].bottom;
                while (search_top <= search_bottom) {
                    hindo_cur = HINDO_MORPHO(HYOKI_INDEX_ADDR(hitbl_addr, search_top));
                    if (hindo_tmp < hindo_cur) {
                        hindo_tmp = hindo_cur;
                        psrhCache->storebuff[m].current = search_top;
                    }
                    search_top++;
                }
            }

            /* 最大頻度値、最大頻度データ位置 更新 */
            if (hindo_tmp > hindo_max) {
                cur_tmp = m;
                hindo_max = hindo_tmp;
            }
        }
    }

    /* 候補が見つからなかった場合 */
    if (hindo_max <= INIT_HINDO) {
        /* 候補なしとする */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    /* 複数頻度がある場合は、最大値を設定する。 */
    loctset->loct.top = psrhCache->storebuff[cur_tmp].top;
    loctset->loct.bottom = psrhCache->storebuff[cur_tmp].bottom;
    loctset->cache_freq = CALCULATE_HINDO(hindo_max, loctset->dic_freq.base,
                                          loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
    loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
    loctset->loct.current = psrhCache->storebuff[cur_tmp].current;
    loctset->loct.current_cache = cur_tmp;

    /* 曖昧検索用表示制御変数の初期化 */
    psrhCache->viewCnt = 1;

    /* 逆引きは頻度順に取得する為、制御をアンセットする。 */
    NJ_UNSET_AIMAI_TO_SCACHE(psrhCache);

    return 1; /* 検索候補あり */
}


/**
 * 検索対象文字を表記文字列インデックステーブルから検索し、該当する単語データの先頭,終端番号を取得する。
 *
 * @param[in]       loctset : 検索位置
 * @param[in]     condition : 検索条件
 * @param[in]         hyoki : 検索する単語の表記文字
 * @param[in]      hyokilen : 検索する単語の表記文字配列長
 * @param[in]     hyoki_top : 検索する単語先頭データ番号
 * @param[in]  hyoki_bottom : 検索する単語終端データ番号
 * @param[in]      startpos : 単語データ検索開始位置(文字配列要素番号)
 * @param[out]        topno : 単語データ先頭番号
 * @param[out]     bottomno : 単語データ終端番号
 *
 * @retval               -1   該当データなし
 * @retval               -2   該当データなし(前方一致有)
 * @retval              >=0   検索する単語の読み文字配列長
 *
 *:::DOC_END
 */
static NJ_INT16 search_rev_idx(NJ_SEARCH_LOCATION_SET *loctset, NJ_SEARCH_CONDITION *condition,
                               NJ_CHAR  *hyoki, NJ_UINT16 hyokilen, 
                               NJ_UINT32 hyoki_top, NJ_UINT32 hyoki_bottom, 
                               NJ_UINT16 startpos, NJ_UINT32 *topno, NJ_UINT32 *bottomno) {

    NJ_INT16  char_len;
    NJ_INT16  char_pos;
    NJ_UINT8  *hitbl_addr;
    NJ_UINT8  *wtbl_addr, *yhtbl_addr;
    NJ_UINT32 left, right, mid, mid_bak;
    NJ_INT32  word_no;
    NJ_UINT8  *word;
    NJ_CHAR   hyoki_buff[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_UINT8  hyoki_buff_len;
    NJ_INT16  diff;
    NJ_UINT32 index  = hyoki_top;
    NJ_UINT32 index2 = hyoki_bottom;
    NJ_UINT8  findflg;
    NJ_UINT8 is_hicomp = 1;


    if (NJ_GET_DIC_TYPE(loctset->loct.handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /* 単語データ領域・表記文字列インデックス領域の先頭アドレス取得 */
    wtbl_addr  = WORD_TOP_ADDR(loctset->loct.handle);
    yhtbl_addr = YOMI_HYOKI_TOP_ADDR(loctset->loct.handle);
    switch (condition->operation) {
    case NJ_CUR_OP_COMP_EXT:
    case NJ_CUR_OP_FORE_EXT:
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT]);
        } else {
            /* 検索対象なしとする */
            return -1;  /*NCH*/
        }
        break;
    default:
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO] != NULL) {
            /* 拡張領域(形態素用)にデータが設定されている場合は拡張領域を参照する */
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO]);
        } else {
            hitbl_addr = HYOKI_IDX_AREA_TOP_ADDR(loctset->loct.handle);
        }
        break;
    }

    /* 単語データ領域から表記文字列インデックスデータ登録数を取得する */
    left    = hyoki_top;
    right   = hyoki_bottom;

    if ((condition->operation == NJ_CUR_OP_REV_FORE) ||
        (condition->operation == NJ_CUR_OP_FORE_EXT)) {
        /* 逆引き前方一致, 正引き前方一致拡張検索の時 */

        /* 検索対象読み文字列を検索し、topを取得する */
        char_pos = startpos;
        while (hyokilen > 0) {
            /* 対象文字の文字コード判定を行う */
            char_len = NJ_CHAR_LEN(hyoki);

            /* 表記文字列インデックステーブルから検索対象文字列が存在するか２分検索を行う */
            findflg = 0;
            while (left <= right) {
                mid = left + ((right - left) / 2);
                word_no = HYOKI_INDEX_WORD_NO(HYOKI_INDEX_ADDR(hitbl_addr, mid));
                word    = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, wtbl_addr, word_no);

                /* 表記文字列 */
                hyoki_buff_len = get_candidate_string(loctset->loct.handle, word, yhtbl_addr, hyoki_buff);
                
                /* 対象の読み文字列とデータ領域の読み・表記文字列を比較 */
                if (hyoki_buff_len > char_pos) {
                    diff = nj_strncmp(hyoki, &hyoki_buff[char_pos], char_len);
                    if (diff == 0) {
                        /* 該当の単語データ番号を保存 */
                        findflg = 1;
                        index = mid;
                        right = mid - 1;
                    } else if (diff < 0) {
                        right = mid - 1;
                    } else {
                        left = mid + 1;
                    }
                } else {
                    left = mid + 1;
                }
            }
            /* 該当範囲に検索対象読み文字列が見つからなかった場合 */
            if (findflg == 0) {
                /* 検索対象なしとする */
                return -1;
            }

            /* 同表記文字の単語データがある為、終端を検索し、
             * その表記インデックステーブルデータ番号を取得するものとする。 */
            left = index;
            right = index2;
            findflg = 0;
            while (left <= right) {
                mid = left + ((right - left) / 2);
                word_no = HYOKI_INDEX_WORD_NO(HYOKI_INDEX_ADDR(hitbl_addr, mid));
                word    = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, wtbl_addr, word_no);

                /* 表記文字列 */
                hyoki_buff_len = get_candidate_string(loctset->loct.handle, word, yhtbl_addr, hyoki_buff);

                if (hyoki_buff_len > char_pos) {
                    /* 対象の読み文字列とデータ領域の読み・表記文字列を比較 */
                    diff = nj_strncmp(hyoki, &hyoki_buff[char_pos], char_len);
                    /* 逆引き前方一致 */
                    if (diff == 0) {
                        /* 該当の単語データ番号を保存 */
                        findflg = 1;
                        index2 = mid;
                        left = mid + 1;
                    } else if (diff < 0) {
                        right = mid - 1;
                    } else {
                        left = mid + 1; /*NCH_DEF*/
                    }
                } else {
                    right = mid - 1; /*NCH_DEF*/
                }
            }
            /* 該当範囲に検索対象読み文字列が見つからなかった場合 */
            if (findflg == 0) {
                /* 検索対象なしとする */
                return -1; /*NCH_DEF*/
            }

            /* 次検索読み文字情報をセット */
            hyoki    += char_len;       /* 読み文字 */
            hyokilen -= char_len;       /* 読み文字長 */
            char_pos += char_len;
            left = index;
            right = index2;
        }

    } else {
        /* 逆引き完全一致, 正引き完全一致拡張検索の時 */
        /* 完全一致検索のため、startposは無視する */

        /* 表記インデックスを２分検索する */
        mid_bak = 0;
        char_pos = hyokilen;
        findflg = 0;
        while (left <= right) {
            mid = left + ((right - left) / 2);

            /* 表記インデックスの位置を取得 */
            word_no = HYOKI_INDEX_WORD_NO(HYOKI_INDEX_ADDR(hitbl_addr, mid));
            word    = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, wtbl_addr, word_no);

            /* 表記文字列を取得 */
            hyoki_buff_len = get_candidate_string(loctset->loct.handle, word, yhtbl_addr, hyoki_buff);

            diff = NJ_CHAR_DIFF(hyoki, hyoki_buff);
            if (diff == 0) {
                if (hyoki_buff_len < hyokilen) {
                    diff = nj_strncmp(hyoki + 1, &hyoki_buff[1], (NJ_UINT16)(hyoki_buff_len - 1));
                    if (diff == 0) {
                        /* 辞書単語文字列の方が短い場合、
                         * 部分文字列が一致しても、キーの方が大きいと判断 */
                        diff = 1;
                    }
                } else {
                    diff = nj_strncmp(hyoki + 1, &hyoki_buff[1], (NJ_UINT16)(hyokilen - 1));
                }
            }
            if (diff == 0) {
                /* 前方一致or完全一致 */
                if (hyoki_buff_len == hyokilen) {
                    /* 完全一致の場合、該当の単語データ番号を保存 */
                    index = mid;
                    if (mid_bak < mid) {
                        mid_bak = mid;
                        index2  = right;
                    }
                }
                findflg= 1;
                right = mid - 1;
            } else if (diff < 0) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        /* 対象の表記文字列が見つからなかった場合は、検索候補なしとする。 */
        if (mid_bak == 0) {
            if (findflg) {
                return -2;        /* 検索候補無し(前方一致有) */
            } else {
                if (left > hyoki_bottom) {
                    /* leftが検索範囲終端を越えている場合 */
                    return -1;        /* 検索候補無し */
                } else {
                    /* 終端が前方一致していないかチェックする */

                    /* 表記インデックスの位置を取得 */
                    word_no  = HYOKI_INDEX_WORD_NO(HYOKI_INDEX_ADDR(hitbl_addr, left));
                    word     = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, wtbl_addr, word_no);

                    /* 表記文字列を取得 */
                    hyoki_buff_len = get_candidate_string(loctset->loct.handle, word, yhtbl_addr, hyoki_buff);

                    if (hyoki_buff_len >= hyokilen) {
                        if (nj_strncmp(hyoki, hyoki_buff, (NJ_UINT16)hyokilen) == 0) {
                            return -2; /*NCH_DEF*/        /* 検索候補無し(前方一致有) */
                        }
                    }
                    return -1;        /* 検索候補無し */
                }
            }
        }

        /* 同表記文字の単語データがある為、終端を検索し、
         * その表記インデックステーブルデータ番号を取得するものとする。 */
        left  = mid_bak; /* 過去に完全一致してる箇所から開始 */
        right = index2;
        if (left == right) {
            /* 検索対象文字の該当する単語データ領域先頭、終端単語データ番号をセット */
            *topno    = index;
            *bottomno = index2;
            return char_pos;
        } 
        index2 = 0;
        while (left <= right) {
            mid = left + ((right - left) / 2);

            /* 表記インデックスの位置を取得 */
            word_no  = HYOKI_INDEX_WORD_NO(HYOKI_INDEX_ADDR(hitbl_addr, mid));
            word     = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, wtbl_addr, word_no);

            /* 表記文字列 */
            hyoki_buff_len = get_candidate_string(loctset->loct.handle, word, yhtbl_addr, hyoki_buff);

            diff = NJ_CHAR_DIFF(hyoki, hyoki_buff);
            if (diff == 0) {
                if (hyoki_buff_len < hyokilen) {
                    diff = nj_strncmp(hyoki + 1, &hyoki_buff[1], (NJ_UINT16)(hyoki_buff_len - 1));
                    if (diff == 0) {
                        /* 辞書単語文字列の方が短い場合、
                         * 部分文字列が一致しても、キーの方が大きいと判断 */
                        diff = 1; /*NCH_DEF*/
                    }
                } else {
                    diff = nj_strncmp(hyoki + 1, &hyoki_buff[1], (NJ_UINT16)(hyokilen - 1));
                }
            }
            if (diff == 0) {
                if (hyoki_buff_len == hyokilen) {
                    /* 完全一致の場合、該当の単語データ番号を保存 */
                    index2 = mid;
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            } else if (diff < 0) {
                right = mid - 1;
            } else {
                left = mid + 1; /*NCH_DEF*/
            }
        }
        /* 対象の表記文字列が見つからなかった場合は、検索候補なしとする。 */
        if (index2 == 0) {
            return -1; /*NCH_DEF*/        /* 検索候補無し */
        }

    }

    /* 検索対象文字の該当する単語データ領域先頭、終端単語データ番号をセット */
    *topno = index;
    *bottomno = index2;

    return char_pos;
}


/**
 * 正引き前方一致検索−あいまい検索の時の次候補を検索する
 *
 * @note 次候補の情報はオフセットとして検索位置(loctset)に設定する
 *
 * @param[in,out]      iwnn : 解析情報クラス
 * @param[in]     condition : 検索条件
 * @param[in,out]   loctset : 検索位置
 * @param[in]          hidx : 辞書のインデックス
 *
 * @retval                0   検索候補なし
 * @retval                1   検索候補あり
 */
static NJ_INT16 tdic_search_data_fore_aimai(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition,
                                            NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 hidx) {
    NJ_UINT8 *data;
    NJ_INT32 hindo = INIT_HINDO;
    NJ_UINT32 current = loctset->loct.current;
    NJ_SEARCH_CACHE *psrhCache = condition->ds->dic[hidx].srhCache;

    NJ_UINT16 top_abIdx;
    NJ_UINT16 bottom_abIdx;
    NJ_UINT16 count_abIdx;
    NJ_UINT16 current_abIdx;
    NJ_UINT16 old_abIdx;
    NJ_UINT8 freq_flag = 0;
    NJ_INT32 save_hindo = INIT_HINDO;
    NJ_UINT16 save_abIdx = 0;
    NJ_UINT16 abPtrIdx;
    NJ_UINT16 m;
    NJ_INT32  ret;
    NJ_UINT16 loop_check;

    NJ_UINT16 abIdx;
    NJ_UINT16 abIdx_old;
    NJ_INT32 hindo_max;
    NJ_INT32 hindo_tmp;
    NJ_UINT32 hindo_max_data, hindo_tmp_data;
    NJ_UINT16 abIdx_current;
    NJ_UINT8 *hdtbl_addr;
    NJ_UINT16 yomi_clen = condition->yclen;
    NJ_UINT16 yomi_clen_funfun;
    NJ_UINT32 bottom;
    NJ_UINT8 bottomflg = 0;

    NJ_UINT8 relation_chk_flg = 0;
    NJ_UINT8 relation_chk_flg2 = 0;

    NJ_UINT8 *ext_top;
    NJ_UINT8 no_used_flg;
    NJ_UINT8 is_hicomp = 1;

    NJ_INT16 ret_16;
    NJ_SEARCH_LOCATION_SET tmp_loctset;



    /* 曖昧検索は、頻度順検索のみの為、
     * 読み順検索(頻度順検索以外)の場合は検索候補なしとする */
    if (condition->mode != NJ_CUR_MODE_FREQ) {
        /* 検索候補なし */
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_DEF*/
        return 0; /*NCH_DEF*/
    }

    /* 初回検索時は そのまま抜ける */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        loctset->loct.status = NJ_ST_SEARCH_READY;
        loctset->loct.current_info = CURRENT_INFO_SET;
        return 1;
    }

    if (NJ_GET_DIC_TYPE(loctset->loct.handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /* 頻度学習値利用に必要なデータを取得する */
    if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL) {
        ext_top = EXT_AREA_TOP_ADDR((NJ_UINT8*)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], loctset->loct.handle);
    } else {
        ext_top = NULL;
    }

    if (NJ_GET_AIMAI_FROM_SCACHE(psrhCache) != 0) {
        NJ_UNSET_AIMAI_TO_SCACHE(psrhCache);
        /* 頻度データ領域先頭アドレス取得 */
        hdtbl_addr = HINDO_TOP_ADDR(loctset->loct.handle);
        if (condition->operation == NJ_CUR_OP_FORE) {
            if (condition->ylen != 0) {
                /* 有効インデックスを取得する */
                abPtrIdx = condition->yclen;

                /* 開始・終了インデックスを取得する */
                abIdx = psrhCache->keyPtr[abPtrIdx];
                abIdx_old = psrhCache->keyPtr[abPtrIdx - 1];
                if ((abIdx > NJ_SEARCH_CACHE_SIZE) || (abIdx_old >= NJ_SEARCH_CACHE_SIZE)) {
                    /* キャッシュが破壊されている */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_BROKEN); /*NCH_FB*/
                }

                /* 頻度順検索の場合 １番頻度が高い候補を
                 * loctset->loct.current にセットする */
                hindo_max = INIT_HINDO;
                hindo_max_data = 0;
                abIdx_current = abIdx_old;

                if (condition->yclen >= iwnn->environment.option.char_min) {
                    yomi_clen_funfun = condition->yclen;
                } else {
                    yomi_clen_funfun = iwnn->environment.option.char_min;
                }

                for (m = abIdx_old; m < abIdx; m++) {
                    /* top の頻度値取得 */
                    hindo_tmp = INIT_HINDO;
                    hindo_tmp_data = 0;
                    current = psrhCache->storebuff[m].top;
                    bottom = psrhCache->storebuff[m].bottom;
                    /* 絞込検索対象候補確認フラグの初期化 */
                    relation_chk_flg2 = 0;

                    /* top 〜 bottom の範囲で最大頻度の候補を探す。*/
                    while (current <= bottom) {

                        /* 検索制限の解除フラグをチェックする */
                        no_used_flg = 0;
                        if ((condition->ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) {
                            /* 単語の予測使用有無の確認 */
                            if (!IS_USED_FOR_FORECAST(current, hdtbl_addr, ext_top, yomi_clen_funfun)) {
                                no_used_flg = 1;
                            }
                        } else {
                            /* 単語検索APIの時のみ通る */
                            /* 単語の予測使用有無の確認 必ず３文字以上インデックスを使う */
                            if (!IS_USED_FOR_FORECAST(current, hdtbl_addr, ext_top, 3)) {
                                no_used_flg = 1;
                            }
                        }
                        if (no_used_flg) {
                            current++;
                            continue;
                        }

                        /* 予測総合頻度を取得する */
                        data = HINDO_YOSOKU_ADDR(hdtbl_addr, current);
                        hindo = get_hindo(iwnn, current, HINDO_DATA(data), loctset, &relation_chk_flg, hidx);

                        /* 最高頻度値の単語番号、頻度値を取得 */
                        if (hindo_tmp < hindo) {

                            /* 該当候補に対して統合辞書フィルターを実行 */
                            if (iwnn->option_data.phase3_filter != NULL) {
                                tmp_loctset = *loctset;
                                tmp_loctset.loct.top = psrhCache->storebuff[m].top;
                                tmp_loctset.loct.bottom = psrhCache->storebuff[m].bottom;
                                tmp_loctset.cache_freq = CALCULATE_HINDO(hindo, loctset->dic_freq.base,
                                                                         loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
                                tmp_loctset.cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
                                tmp_loctset.loct.current = current;
                                tmp_loctset.loct.attr = get_attr(loctset->loct.handle, current);
                                tmp_loctset.loct.current_cache = m;

                                /* フィルターチェック処理を実行 */
                                if (!(is_tdic_filtering(iwnn, &tmp_loctset))) {
                                    /* 候補使用なしの場合、次候補検索へ移る */
                                    current++;
                                    continue;
                                }
                            }
                            hindo_tmp = hindo;
                            hindo_tmp_data = current;
                            /* 取得した候補が絞込検索候補であった場合 */
                            if (relation_chk_flg == 1) {
                                /* 絞込検索候補フラグの立てる */
                                relation_chk_flg2 = 1;
                            } else {
                                /* 絞込検索候補フラグの落とす */
                                relation_chk_flg2 = 0;
                            }
                        }
                        current++;

                    }

                    /* 一番頻度が高い単語を記憶する。 */
                    if (hindo_tmp_data != 0) {
                        /* 検索候補が有った場合のみ更新をする */
                        psrhCache->storebuff[m].current = hindo_tmp_data;
                        /* 最大頻度値、最大頻度データ位置 更新 */
                        if (hindo_tmp > hindo_max) {
                            hindo_max = hindo_tmp;
                            hindo_max_data = hindo_tmp_data;
                            abIdx_current = m;
                            /* 取得した候補が絞込検索候補であった場合 */
                            if (relation_chk_flg2 == 1) {
                                /* 絞込検索候補フラグの立てる */
                                iwnn->wk_relation_cand_flg[hidx] = 1;
                            } else {
                                /* 絞込検索候補フラグの落とす */
                                iwnn->wk_relation_cand_flg[hidx] = 0;
                            }
                        }
                    } else {
                        /* 検索候補が無かった場合 */
                        psrhCache->storebuff[m].current = LOC_CURRENT_NO_ENTRY;
                    }
                    
                }

                /* 複数頻度がある場合は、最大値を設定する。 */
                loctset->loct.top = psrhCache->storebuff[abIdx_current].top;
                loctset->loct.bottom = psrhCache->storebuff[abIdx_current].bottom;
                loctset->cache_freq = CALCULATE_HINDO(hindo_max, loctset->dic_freq.base,
                                                      loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
                loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
                loctset->loct.current = hindo_max_data;
                loctset->loct.attr   = get_attr(loctset->loct.handle, hindo_max_data);
                loctset->loct.current_cache = abIdx_current;

                /* 曖昧検索用表示制御変数の初期化 */
                psrhCache->viewCnt = 1;
            } else {
                /* top の頻度値取得 */
                hindo_max = INIT_HINDO; /*NCH_FB*/
                hindo_max_data = 0; /*NCH_FB*/
                hindo_tmp = INIT_HINDO; /*NCH_FB*/
                hindo_tmp_data = 0; /*NCH_FB*/

                current = loctset->loct.current; /*NCH_FB*/
                bottom = loctset->loct.bottom; /*NCH_FB*/
                if (current >= bottom) { /*NCH_FB*/
                    current = loctset->loct.top; /*NCH_FB*/
                } else {
                    current += 1; /*NCH_FB*/
                }
                /* top 〜 bottom の範囲で最大頻度の候補を探す。*/
                while (current <= loctset->loct.bottom) { /*NCH_FB*/
                    /* 検索制限の解除フラグをチェックする */
                    no_used_flg = 0; /*NCH_FB*/
                    if ((condition->ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) { /*NCH_FB*/
                        /* 単語使用有無の確認 */
                        if (!IS_USED_FOR_FORECAST(current, hdtbl_addr, ext_top, condition->yclen)) { /*NCH_FB*/
                            no_used_flg = 1; /*NCH_FB*/
                        }
                    } else {
                        /* 単語検索APIの時のみ通る */
                        /* 単語使用有無の確認 必ず３文字以上インデックスを使う */
                        if (!IS_USED_FOR_FORECAST(current, hdtbl_addr, ext_top, 3)) { /*NCH_FB*/
                            no_used_flg = 1; /*NCH_FB*/
                        }
                    }
                    if (no_used_flg) { /*NCH_FB*/
                        current++; /*NCH_FB*/
                        if ((current > bottom) && (bottomflg == 0)) { /*NCH_FB*/
                            bottomflg = 1; /*NCH_FB*/
                            current = loctset->loct.top; /*NCH_FB*/
                            bottom = loctset->loct.current; /*NCH_FB*/
                        }
                        continue; /*NCH_FB*/
                    }

                    /* 予測総合頻度を取得する */
                    data = HINDO_YOSOKU_ADDR(hdtbl_addr, current); /*NCH_FB*/
                    hindo = get_hindo(iwnn, current, HINDO_DATA(data), loctset, &relation_chk_flg, hidx); /*NCH_FB*/

                    /* 最大頻度値, 最大頻度データ位置 更新 */
                    if (hindo > hindo_max) { /*NCH_FB*/

                        /* 該当候補に対して統合辞書フィルターを実行 */
                        if (iwnn->option_data.phase3_filter != NULL) { /*NCH_FB*/
                            tmp_loctset = *loctset; /*NCH_FB*/
                            tmp_loctset.cache_freq = CALCULATE_HINDO(hindo, /*NCH_FB*/
                                                                     loctset->dic_freq.base,
                                                                     loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
                            tmp_loctset.cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min); /*NCH_FB*/
                            tmp_loctset.loct.current = current; /*NCH_FB*/
                            tmp_loctset.loct.attr = get_attr(loctset->loct.handle, hindo); /*NCH_FB*/

                            /* フィルターチェック処理を実行 */
                            ret_16 = is_tdic_filtering(iwnn, &tmp_loctset); /*NCH_FB*/
                        } else {
                            ret_16 = 1; /*NCH_FB*/
                        }

                        if (ret_16) {
                            hindo_max = hindo; /*NCH_FB*/
                            hindo_max_data = current; /*NCH_FB*/
                            /* 取得した候補が絞込検索候補であった場合 */
                            if (relation_chk_flg == 1) { /*NCH_FB*/
                                /* 絞込検索候補フラグの立てる */
                                iwnn->wk_relation_cand_flg[hidx] = 1; /*NCH_FB*/
                            } else {
                                /* 絞込検索候補フラグの落とす */
                                iwnn->wk_relation_cand_flg[hidx] = 0; /*NCH_FB*/
                            }
                        }
                    }
                    current++; /*NCH_FB*/
                    if ((current > bottom) && (bottomflg == 0)) { /*NCH_FB*/
                        bottomflg = 1; /*NCH_FB*/
                        current = loctset->loct.top; /*NCH_FB*/
                        bottom = loctset->loct.current; /*NCH_FB*/
                    }
                }
                loctset->cache_freq = CALCULATE_HINDO(hindo_max, /*NCH_FB*/
                                                      loctset->dic_freq.base,
                                                      loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
                loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min); /*NCH_FB*/
                loctset->loct.current = hindo_max_data; /*NCH_FB*/
                loctset->loct.attr   = get_attr(loctset->loct.handle, hindo_max_data); /*NCH_FB*/
            }
        }
        return 1;
    }

    /* 頻度データ領域先頭アドレス取得 */
    hdtbl_addr = HINDO_TOP_ADDR(loctset->loct.handle);

    /* 頻度順検索の場合は、次に頻度が高い単語を取得する */
    /* 検索対象の単語データ領域の終端を超えた場合は抜ける */

    /* 有効インデックスを取得する */
    abPtrIdx = condition->yclen;

    /* 開始・終了インデックスを取得する */
    bottom_abIdx = psrhCache->keyPtr[abPtrIdx];
    top_abIdx = psrhCache->keyPtr[abPtrIdx - 1];
    if ((bottom_abIdx > NJ_SEARCH_CACHE_SIZE) || (top_abIdx >= NJ_SEARCH_CACHE_SIZE)) {
        /* キャッシュが破壊されている */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_BROKEN); /*NCH_FB*/
    }

    /* インデックス数を割り出す */
    count_abIdx = (NJ_UINT16)(bottom_abIdx - top_abIdx);
    if (count_abIdx == 0) {
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
        return 0; /*NCH_FB*/
    }

    /* 検索キャッシュ位置を設定 */
    if (loctset->loct.current_cache < top_abIdx) {
        old_abIdx = top_abIdx; /*NCH_DEF*/
    } else {
        old_abIdx = loctset->loct.current_cache;
    }

    loop_check = 0;

    /* 次候補の取得を行う。 */
    ret = tdic_get_next_data(iwnn, loctset, psrhCache, old_abIdx, yomi_clen, hidx, condition->ctrl_opt);
    /* 現在の候補の頻度を取得する */
    data = HINDO_YOSOKU_ADDR(hdtbl_addr, loctset->loct.current);
    hindo = get_hindo(iwnn, loctset->loct.current, HINDO_DATA(data), loctset, NULL, hidx);

    if (ret >= hindo) {
        /* 過去の頻度と同じ頻度だった場合。
         * ret > hindoとなるケースはないが、念のため。
         */
        psrhCache->viewCnt++;
        if (psrhCache->viewCnt <= NJ_CACHE_VIEW_CNT) {
            /* 同一曖昧検索結果の抑制を行う。 */
            loctset->loct.status = NJ_ST_SEARCH_READY;
            loctset->loct.current_info = CURRENT_INFO_SET;
            loctset->loct.current = psrhCache->storebuff[old_abIdx].current;
            loctset->loct.attr   = get_attr(loctset->loct.handle, psrhCache->storebuff[old_abIdx].current);
            loctset->loct.current_cache = old_abIdx;
            return 1;
        } else {
            /* 曖昧検索チェック変数の初期化 */
            freq_flag = 1;
            psrhCache->viewCnt = 0;
        }
    } else {
        /* 過去の頻度と同じではなかった場合 */
        /* ここを通る場合は、過去の頻度より低いはずである。 */
        if (ret <= INIT_HINDO) {
            /* 候補がなくなった場合 */
            loop_check++;
        }
        save_hindo = ret;
        save_abIdx = old_abIdx;
    }

    /* カレント以降を検索する。 */
    current_abIdx = (NJ_UINT16)(old_abIdx + 1);
    if (current_abIdx >= bottom_abIdx) {
        /* 検索範囲を超えている場合、Topへ移動 */
        current_abIdx = top_abIdx;
    }

    while (loop_check != count_abIdx) {
        /* チェック変数がカウント数すべてを満たすまで終了しない。 */
        /* 終了条件を満たす＝候補０を示す。 */

        /* 指定候補の頻度を取得する */
        ret = tdic_get_word_freq(iwnn, loctset, psrhCache, current_abIdx, hidx);

        if ((ret == hindo) &&
            (loctset->loct.top == psrhCache->storebuff[current_abIdx].top) &&
            (loctset->loct.current == psrhCache->storebuff[current_abIdx].current)) {
            /* 過去の頻度と同じ頻度でかつ辞書内の同一単語を指している場合 */
            /* 単語を１つ進ませる */
            ret = tdic_get_next_data(iwnn, loctset, psrhCache, current_abIdx, yomi_clen, hidx, condition->ctrl_opt);
        }

        if (ret == hindo) {
            /* 過去の頻度と同じ頻度だった場合 */
            loctset->loct.status = NJ_ST_SEARCH_READY;
            loctset->loct.current_info = CURRENT_INFO_SET;
            loctset->loct.top = psrhCache->storebuff[current_abIdx].top;
            loctset->loct.bottom = psrhCache->storebuff[current_abIdx].bottom;
            loctset->loct.current = psrhCache->storebuff[current_abIdx].current;
            loctset->loct.attr   = get_attr(loctset->loct.handle, psrhCache->storebuff[current_abIdx].current);
            loctset->loct.current_cache = current_abIdx;
            psrhCache->viewCnt = 1;
            return 1;

        } else {
            /* 過去の頻度と同じではなかった場合 */
            /* ここを通る場合は、過去の頻度より低いはずである。 */
            if (ret <= INIT_HINDO) {
                /* 候補がなくなった場合 */
                loop_check++;
            }
            if (save_hindo < ret) {
                /* 保存頻度より大きければ頻度を保存 */
                save_hindo = ret;
                save_abIdx = current_abIdx;
            }
        }

        /* カレントを進める。 */
        current_abIdx++;
        if (current_abIdx >= bottom_abIdx) {
            /* 検索範囲を超えている場合、Topへ移動 */
            current_abIdx = top_abIdx;
        }

        /* 曖昧検索範囲をすべて検索完了 */
        if (current_abIdx == old_abIdx) {
            if (freq_flag == 1) {
                /* ほかの候補内で同一候補が見つからなかった場合 */
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->loct.top = psrhCache->storebuff[current_abIdx].top;
                loctset->loct.bottom = psrhCache->storebuff[current_abIdx].bottom;
                loctset->loct.current = psrhCache->storebuff[current_abIdx].current;
                loctset->loct.attr   = get_attr(loctset->loct.handle, psrhCache->storebuff[current_abIdx].current);
                loctset->loct.current_cache = current_abIdx;
                psrhCache->viewCnt = 1;
                return 1;
            } else if (save_hindo != INIT_HINDO) {
                /* 頻度を更新する。 */
                loctset->cache_freq = CALCULATE_HINDO(save_hindo, loctset->dic_freq.base, 
                                                      loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
                loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->loct.top = psrhCache->storebuff[save_abIdx].top;
                loctset->loct.bottom = psrhCache->storebuff[save_abIdx].bottom;
                loctset->loct.current = psrhCache->storebuff[save_abIdx].current;
                loctset->loct.attr   = get_attr(loctset->loct.handle, psrhCache->storebuff[save_abIdx].current);
                loctset->loct.current_cache = save_abIdx;
                psrhCache->viewCnt = 1;
                return 1;
            } else {
            }
        }
    }

    /* 検索終了の属性を設定 */
    loctset->loct.status = NJ_ST_SEARCH_END;
    return 0;
}


/**
 * 逆引き前方一致, 正引き前方一致拡張検索−あいまい検索の時の次候補を検索する（次候補の情報はオフセットとして検索位置(loctset)に設定する）
 *
 * @param[in]          iwnn : 解析情報クラス
 * @param[in]     condition : 検索条件
 * @param[in,out]   loctset : 検索位置
 * @param[in]          hidx : 辞書のインデックス
 *
 * @retval                0   検索候補なし
 * @retval                1   検索候補あり
 */
static NJ_INT16 tdic_search_data_rev_aimai(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 hidx) {
    NJ_INT32 hindo = INIT_HINDO;
    NJ_SEARCH_CACHE *psrhCache = condition->ds->dic[hidx].srhCache;

    NJ_UINT16 top_abIdx;
    NJ_UINT16 bottom_abIdx;
    NJ_UINT16 count_abIdx;
    NJ_UINT16 current_abIdx;
    NJ_UINT16 old_abIdx;
    NJ_UINT8  freq_flag = 0;
    NJ_INT32  save_hindo = INIT_HINDO;
    NJ_UINT16 save_abIdx = 0;
    NJ_UINT16 abPtrIdx;
    NJ_INT32  ret;
    NJ_UINT16 loop_check;

    NJ_UINT8 *hitbl_addr;
    NJ_UINT8 *hidx_cur;


    /* 曖昧検索は、頻度順検索のみの為、
     * 表記順検索(頻度順検索以外)の場合は検索候補なしとする */
    if (condition->mode != NJ_CUR_MODE_FREQ) {
        /* 検索候補なし */
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_DEF*/
        return 0; /*NCH_DEF*/
    }

    /* 初回検索時は そのまま抜ける */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        loctset->loct.status = NJ_ST_SEARCH_READY;
        loctset->loct.current_info = CURRENT_INFO_SET;
        return 1;
    }

    /* 曖昧検索は、頻度順で検索するため、曖昧度合いの低い設定できた場合は、
     * 検索候補無しとする */
    if (NJ_GET_AIMAI_FROM_SCACHE(psrhCache) != 0) {
        /* 検索候補なし */
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_DEF*/
        return 0; /*NCH_DEF*/
    }

    if (condition->operation == NJ_CUR_OP_FORE_EXT) {
        /* 正引き前方一致拡張検索の場合 */
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT]);
        } else {
            /* 検索候補なし */
            loctset->loct.status = NJ_ST_SEARCH_END;    /*NCH*/
            return 0;                                   /*NCH*/
        }
    } else {
        /* 逆引き前方一致検索の場合 */
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO] != NULL) {
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO]);
        } else {
            hitbl_addr = HYOKI_IDX_AREA_TOP_ADDR(loctset->loct.handle);
        }
    }

    abPtrIdx = condition->yclen;

    /* 開始・終了インデックスを取得する */
    bottom_abIdx = psrhCache->keyPtr[abPtrIdx];
    top_abIdx = psrhCache->keyPtr[abPtrIdx - 1];
    if ((bottom_abIdx > NJ_SEARCH_CACHE_SIZE) || (top_abIdx >= NJ_SEARCH_CACHE_SIZE)) {
        /* キャッシュが破壊されている */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_SEARCH_WORD, NJ_ERR_CACHE_BROKEN); /*NCH*/
    }

    /* インデックス数を割り出す */
    count_abIdx = (NJ_UINT16)(bottom_abIdx - top_abIdx);
    if (count_abIdx == 0) {
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
        return 0; /*NCH_FB*/
    }

    /* 検索キャッシュ位置を設定 */
    if (loctset->loct.current_cache < top_abIdx) {
        old_abIdx = top_abIdx; /*NCH_DEF*/
    } else {
        old_abIdx = loctset->loct.current_cache;
    }

    loop_check = 0;

    /* 次候補の取得を行う。 */
    if (condition->operation == NJ_CUR_OP_FORE_EXT) {
        /* 正引き前方一致拡張検索の場合 */
        ret = tdic_get_next_data_fore_ext(iwnn, condition, loctset, psrhCache, old_abIdx);
        hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
        hindo = HINDO_DATA(hidx_cur);
    } else {
        /* 逆引き前方一致検索の場合 */
        ret = tdic_get_next_data_rev(loctset, psrhCache, old_abIdx);
        hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
        hindo = HINDO_MORPHO(hidx_cur);
    }

    if (ret == hindo) {
        /* 過去の頻度と同じ頻度だった場合。 */
        psrhCache->viewCnt++;
        if (psrhCache->viewCnt <= NJ_CACHE_VIEW_CNT) {
            /* 同一曖昧検索結果の抑制を行う。 */
            loctset->loct.status = NJ_ST_SEARCH_READY;
            loctset->loct.current_info = CURRENT_INFO_SET;
            loctset->loct.current = psrhCache->storebuff[old_abIdx].current;
            loctset->loct.current_cache = old_abIdx;
            hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
            loctset->loct.attr   = get_attr(loctset->loct.handle, HYOKI_INDEX_WORD_NO(hidx_cur));
            return 1;
        } else {
            /* 曖昧検索チェック変数の初期化 */
            freq_flag = 1;
            psrhCache->viewCnt = 0;
        }
    } else {
        /* 過去の頻度と同じではなかった場合 */
        /* ここを通る場合は、過去の頻度より低いはずである。 */
        if (ret <= INIT_HINDO) {
            /* 候補がなくなった場合 */
            loop_check++;
        }
        save_hindo = ret;
        save_abIdx = old_abIdx;
    }

    /* カレント以降を検索する。 */
    current_abIdx = (NJ_UINT16)(old_abIdx + 1);
    if (current_abIdx >= bottom_abIdx) {
        /* 検索範囲を超えている場合、Topへ移動 */
        current_abIdx = top_abIdx;
    }

    while (loop_check != count_abIdx) {
        /* チェック変数がカウント数すべてを満たすまで終了しない。 */
        /* 終了条件を満たす＝候補０を示す。 */

        /* 指定候補の頻度を取得する */
        if (condition->operation == NJ_CUR_OP_FORE_EXT) {
            /* 正引き前方一致拡張検索の場合 */
            ret = tdic_get_word_freq_fore_ext(loctset, psrhCache, current_abIdx);
        } else {
            /* 逆引き前方一致検索の場合 */
            ret = tdic_get_word_freq_rev(loctset, psrhCache, current_abIdx);
        }

        if ((ret == hindo) &&
            (loctset->loct.top == psrhCache->storebuff[current_abIdx].top) &&
            (loctset->loct.current == psrhCache->storebuff[current_abIdx].current)) {
            /* 過去の頻度と同じ頻度でかつ辞書内の同一単語を指している場合 */
            /* 単語を１つ進ませる */
            ret = tdic_get_next_data_rev(loctset, psrhCache, current_abIdx); /*NCH_FB*/
        }

        if (ret == hindo) {
            /* 過去の頻度と同じ頻度だった場合 */
            loctset->loct.status = NJ_ST_SEARCH_READY;
            loctset->loct.current_info = CURRENT_INFO_SET;
            loctset->loct.top = psrhCache->storebuff[current_abIdx].top;
            loctset->loct.bottom = psrhCache->storebuff[current_abIdx].bottom;
            loctset->loct.current = psrhCache->storebuff[current_abIdx].current;
            loctset->loct.current_cache = current_abIdx;
            hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
            loctset->loct.attr   = get_attr(loctset->loct.handle, HYOKI_INDEX_WORD_NO(hidx_cur));
            psrhCache->viewCnt = 1;
            return 1;

        } else {
            /* 過去の頻度と同じではなかった場合 */
            /* ここを通る場合は、過去の頻度より低いはずである。 */
            if (ret <= INIT_HINDO) {
                /* 候補がなくなった場合 */
                loop_check++;
            }
            if (save_hindo < ret) {
                /* 保存頻度より大きければ頻度を保存 */
                save_hindo = ret;
                save_abIdx = current_abIdx;
            }
        }

        /* カレントを進める。 */
        current_abIdx++;
        if (current_abIdx >= bottom_abIdx) {
            /* 検索範囲を超えている場合、Topへ移動 */
            current_abIdx = top_abIdx;
        }

        /* 曖昧検索範囲をすべて検索完了 */
        if (current_abIdx == old_abIdx) {
            if (freq_flag == 1) {
                /* ほかの候補内で同一候補が見つからなかった場合 */
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->loct.top = psrhCache->storebuff[current_abIdx].top;
                loctset->loct.bottom = psrhCache->storebuff[current_abIdx].bottom;
                loctset->loct.current = psrhCache->storebuff[current_abIdx].current;
                loctset->loct.current_cache = current_abIdx;
                hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
                loctset->loct.attr   = get_attr(loctset->loct.handle, HYOKI_INDEX_WORD_NO(hidx_cur));
                psrhCache->viewCnt = 1;
                return 1;
            } else if (save_hindo != INIT_HINDO) {
                /* 頻度を更新する。 */
                loctset->cache_freq = CALCULATE_HINDO(save_hindo, loctset->dic_freq.base,
                                                      loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
                loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->loct.top = psrhCache->storebuff[save_abIdx].top;
                loctset->loct.bottom = psrhCache->storebuff[save_abIdx].bottom;
                loctset->loct.current = psrhCache->storebuff[save_abIdx].current;
                loctset->loct.current_cache = save_abIdx;
                hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
                loctset->loct.attr   = get_attr(loctset->loct.handle, HYOKI_INDEX_WORD_NO(hidx_cur));
                psrhCache->viewCnt = 1;
                return 1;
            } else {
            }
        }
    }
    /* 検索終了の属性を設定 */
    loctset->loct.status = NJ_ST_SEARCH_END;
    return 0;
}


/**
 * 逆引き完全一致,逆引き前方一致,正引き完全一致拡張,正引き前方一致拡張検索の時、<br>
 * 次候補を検索する（次候補の情報はオフセットとして検索位置(loctset)に設定する）
 *
 * @param[in]          iwnn : 解析情報クラス
 * @param[in]     condition : 検索条件
 * @param[in,out]   loctset : 検索位置
 *
 * @retval                0   検索候補なし
 * @retval                1   検索候補あり
 */
static NJ_INT16 tdic_search_data_rev(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset) {
    NJ_UINT8 *data;
    NJ_UINT8 *data_cur, *hidx_cur;
    STEM_DATA_SET stem_set;
    NJ_INT32 word_no = 0;
    NJ_UINT8 *hitbl_addr;
    NJ_UINT8 *hitbl_tmp;
    NJ_INT32 hindo = INIT_HINDO;
    NJ_UINT32 current = loctset->loct.current;
    NJ_INT32 hindo_chk = INIT_HINDO;
    NJ_UINT32 word_next = 0;
    NJ_INT32 hindo_current = INIT_HINDO;
    NJ_INT16 hindo_d;

    /* インライン展開用 */
    NJ_UINT8 fb, bb;
    NJ_UINT16 hdata;
    NJ_UINT32 cnt;
    NJ_UINT8 *wkc;
    NJ_UINT8 is_hicomp = 1;
    NJ_UINT8 no_used_flg;


    if (NJ_GET_DIC_TYPE(loctset->loct.handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /* 現カーソル位置単語データの先頭アドレスを取得 */
    data = WORD_TOP_ADDR(loctset->loct.handle);
    if ((condition->operation == NJ_CUR_OP_REV) ||
        (condition->operation == NJ_CUR_OP_REV_FORE)) {
        /* 逆引き完全一致, 逆引き前方一致検索の場合 */
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO] != NULL) {
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO]);
        } else {
            hitbl_addr = HYOKI_IDX_AREA_TOP_ADDR(loctset->loct.handle);
        }
    } else {
        /* 正引き完全一致拡張, 正引き前方一致拡張の場合 */
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT]);
        } else {
            /* 検索終了の属性を設定 */
            loctset->loct.status = NJ_ST_SEARCH_END;    /*NCH*/
            return 0;                                   /*NCH*/
        }
    }
    hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
    word_no = HYOKI_INDEX_WORD_NO(hidx_cur);
    data_cur = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, data, word_no);
    cnt = loctset->loct.current;

    if ((condition->operation == NJ_CUR_OP_REV) ||
        (condition->operation == NJ_CUR_OP_COMP_EXT)) {
        /*
         * 逆引き完全一致, 正引き完全一致拡張検索の場合、
         * 品詞情報を取得し、品詞関係が一致するものを候補とする。
         */

        if (GET_LOCATION_STATUS(loctset->loct.status) != NJ_ST_SEARCH_NO_INIT) {
            /**
             * 初回の検索で無いとき、現カーソル位置で逆引き完全一致候補が
             * 終了かどうか検査
             */
            if (loctset->loct.current == loctset->loct.bottom) {
                /* 検索終了の属性を設定 */
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;
            }

            if (condition->mode == NJ_CUR_MODE_FREQ) {
                /* 頻度順検索の時 */
                /* STEMデータ設定 */
                cnt = loctset->loct.current + 1;
                if (cnt > loctset->loct.bottom) {
                    /* 次候補無し */
                    loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_DEF*/
                    return 0; /*NCH_DEF*/
                }
                hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, cnt);
                word_no = HYOKI_INDEX_WORD_NO(hidx_cur);
                data_cur = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, data, word_no);
            }
        }

        /* 品詞番号変換用テーブル上の１品詞のサイズ */
        fb = FHINSI_NO_BYTE(loctset->loct.handle);
        bb = BHINSI_NO_BYTE(loctset->loct.handle);

        while (cnt <= loctset->loct.bottom) {
            /* 品詞情報を取得 */
            /**
             * ↓get_stem_hinsiのインライン展開 (本体)
             */
            if (condition->hinsi.fore != NULL) {
                if (BIT_FHINSI(loctset->loct.handle)) {
                    /* 前品詞 設定 */
                    hdata = HINSI_DATA_FROM_DIC_TYPE(is_hicomp, data_cur);
                    stem_set.fhinsi = GET_BITFIELD_16(hdata, 0, BIT_FHINSI(loctset->loct.handle));
                } else {
                    stem_set.fhinsi = 0; /*NCH_FB*/
                }

                /* 品詞番号変換データ領域の該当位置 */
                wkc = (HINSI_NO_AREA_TOP_ADDR(loctset->loct.handle) + (NJ_UINT32)(fb * stem_set.fhinsi));
                /* 実品詞への変換：品詞番号設定 */
                if (fb == 2) {
                    stem_set.fhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(wkc)); /*NCH_FB*/
                } else {
                    stem_set.fhinsi_jitu = (NJ_UINT16)*wkc;
                }
            } else {
                stem_set.fhinsi = 0;
                stem_set.fhinsi_jitu = 0;
            }

            if (condition->hinsi.rear != NULL) {
                if (BIT_BHINSI(loctset->loct.handle)) {
                    /* 後品詞 設定 */
                    hdata = HINSI_DATA_FROM_DIC_TYPE(is_hicomp, data_cur);
                    stem_set.bhinsi = GET_BITFIELD_16(hdata, BIT_FHINSI(loctset->loct.handle), BIT_BHINSI(loctset->loct.handle));
                } else {
                    stem_set.bhinsi = 0; /*NCH_FB*/
                }

                /* 品詞番号変換データ領域の該当位置 */
                wkc = (HINSI_NO_AREA_TOP_ADDR(loctset->loct.handle)
                       + (NJ_UINT32)((fb * (FHINSI_NO_CNT(loctset->loct.handle))) + (bb * stem_set.bhinsi)));
                /* 実品詞への変換：品詞番号設定 */
                if (bb == 2) {
                    stem_set.bhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(wkc));
                } else {
                    stem_set.bhinsi_jitu = (NJ_UINT16)*wkc;
                }
            } else {
                stem_set.bhinsi = 0;
                stem_set.bhinsi_jitu = 0;
            }

            /**
             * ↑get_stem_hinsiのインライン展開 (本体)
             */

            /* 前品詞が検索条件と一致するかチェック */
            if (njd_connect_test(condition, stem_set.fhinsi_jitu, stem_set.bhinsi_jitu)) {
                /* 完全一致 */
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current = cnt;
                hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
                loctset->loct.attr   = get_attr(loctset->loct.handle, HYOKI_INDEX_WORD_NO(hidx_cur));
                loctset->loct.current_info = CURRENT_INFO_SET;
                if (condition->operation == NJ_CUR_OP_COMP_EXT) {
                    hindo_d = (NJ_INT16)HINDO_DATA(hidx_cur);
                } else {
                    hindo_d = (NJ_INT16)HINDO_MORPHO(hidx_cur);
                }
                loctset->cache_freq = CALCULATE_HINDO(hindo_d, loctset->dic_freq.base, 
                                                      loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
                loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
                return 1;
            }
            if (STEM_TERMINATOR(data)) {
                /* 次候補無し */
                loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_DEF*/
                return 0; /*NCH_DEF*/
            }

            /* 次候補のアドレス取得 */
            cnt++;
            if (cnt > loctset->loct.bottom) {
                /* 次候補無し */
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;
            }
            hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, cnt);
            word_no  = HYOKI_INDEX_WORD_NO(hidx_cur);
            data_cur = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, data, word_no);
        }
    } else {

        /* 初回検索時は そのまま抜ける */
        if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
            loctset->loct.status = NJ_ST_SEARCH_READY;
            loctset->loct.current_info = CURRENT_INFO_SET;
            return 1;
        }

        /* 逆引き前方一致検索, 正引き前方一致拡張検索の時 */
        if (condition->mode == NJ_CUR_MODE_FREQ) {
            /* 頻度順検索の場合は、検索対象のtopからbottomを検索し、
             * 次に頻度が高いものを取得する。 */
            /* 判定するカレント情報を取得 */
            hitbl_tmp = HYOKI_INDEX_ADDR(hitbl_addr, current);
            if (condition->operation == NJ_CUR_OP_FORE_EXT) {
                /* 正引き前方一致拡張検索の場合 */
                hindo_current = (NJ_INT16)HINDO_DATA(hitbl_tmp);
            } else {
                /* 逆引き前方一致検索の場合 */
                hindo_current = (NJ_INT16)HINDO_MORPHO(hitbl_tmp);
            }
            cnt = loctset->loct.top;
            hindo = INIT_HINDO;
            hindo_chk = INIT_HINDO;
            word_next = 0;
            if (condition->operation == NJ_CUR_OP_FORE_EXT) {
                /* 正引き前方一致拡張検索の場合 */
                while (cnt <= loctset->loct.bottom) {
                    hitbl_tmp = HYOKI_INDEX_ADDR(hitbl_addr, cnt);
                    no_used_flg = 0;
                    if ((condition->ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) {
                        /* 単語使用有無の確認 */
                        if ((HINDO_EXT_AREA_YOSOKU(hitbl_tmp)) &&
                            (is_funfun_ok_ext(loctset->loct.handle, HYOKI_INDEX_WORD_NO(hitbl_tmp), iwnn->environment.option.char_min, iwnn->environment.option.char_max))) {
                            no_used_flg = 1;
                        }
                    } else {
                        /* 辞書引きAPIからのみ通る */
                        no_used_flg = 1;
                    }
                    if (!no_used_flg) {
                        cnt++;
                        continue;
                    }

                    hindo = (NJ_INT16)HINDO_DATA(hitbl_tmp);

                    if ((cnt > current) && (hindo == hindo_current)) {
                        /* 検索中頻度と等しく、且つ検索候補がカレント位置以降の場合,
                         * その検索候補位置を返す */
                        loctset->loct.status = NJ_ST_SEARCH_READY;
                        loctset->loct.current_info = CURRENT_INFO_SET;
                        loctset->loct.current = cnt;
                        hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
                        loctset->loct.attr   = get_attr(loctset->loct.handle, HYOKI_INDEX_WORD_NO(hidx_cur));
                        return 1;
                    } else if ((hindo_chk < hindo) && (hindo < hindo_current)) {
                        hindo_chk = hindo;
                        word_next = cnt;
                    } else {
                    }
                    cnt++;
                }
            } else {
                /* 逆引き前方一致検索の場合 */
                while (cnt <= loctset->loct.bottom) {
                    hitbl_tmp = HYOKI_INDEX_ADDR(hitbl_addr, cnt);
                    hindo = (NJ_INT16)HINDO_MORPHO(hitbl_tmp);

                    if ((cnt > current) && (hindo == hindo_current)) {
                        /* 検索中頻度と等しく、且つ検索候補がカレント位置以降の場合,
                         * その検索候補位置を返す */
                        loctset->loct.status = NJ_ST_SEARCH_READY;
                        loctset->loct.current_info = CURRENT_INFO_SET;
                        loctset->loct.current = cnt;
                        hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
                        loctset->loct.attr   = get_attr(loctset->loct.handle, HYOKI_INDEX_WORD_NO(hidx_cur));
                        return 1;
                    } else if ((hindo_chk < hindo) && (hindo < hindo_current)) {
                        hindo_chk = hindo;
                        word_next = cnt;
                    } else {
                    }
                    cnt++;
                }
            }

            /* 検索中の頻度と一致しない場合,
             * 最大頻度を持つ候補の頻度値,単語データ番号を返す。*/
            if (word_next > 0) {
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current = word_next;
                hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
                loctset->loct.attr   = get_attr(loctset->loct.handle, HYOKI_INDEX_WORD_NO(hidx_cur));
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->cache_freq = CALCULATE_HINDO(hindo_chk, loctset->dic_freq.base, 
                                                      loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
                loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
                return 1;
            } else {
                /* 次候補無し */
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;
            }
        } else {

            if (loctset->loct.current == loctset->loct.bottom) {
                /* 検索終了の属性を設定 */
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;
            }

            if (condition->operation == NJ_CUR_OP_FORE_EXT) {
                /* 正引き前方一致拡張検索の場合 */
                cnt = loctset->loct.current + 1;
                hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, cnt);
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current = cnt;
                hindo_d = (NJ_INT16)HINDO_DATA(hidx_cur);
                loctset->loct.attr = get_attr(loctset->loct.handle, HYOKI_INDEX_WORD_NO(hidx_cur));
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->cache_freq = CALCULATE_HINDO(hindo_d, loctset->dic_freq.base, 
                                                      loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
                loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
                return 1;
            } else {
                /* 逆引き前方一致検索の場合 */
                cnt = loctset->loct.current + 1;
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current = cnt;
                hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
                hindo_d = (NJ_INT16)HINDO_MORPHO(hidx_cur);
                loctset->loct.attr = get_attr(loctset->loct.handle, HYOKI_INDEX_WORD_NO(hidx_cur));
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->cache_freq = CALCULATE_HINDO(hindo_d, loctset->dic_freq.base, 
                                                      loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
                loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
                return 1;
            }
        }
    }
    /* 次候補無し */
    loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
    return 0; /*NCH_FB*/

}


/**
 * 読みなし予測の時の次候補を検索する（次候補の情報は検索位置(loctset)に設定する）
 *
 * @param[in,out]  iwnn      解析情報クラス
 * @param[in]      condition 検索条件
 * @param[in,out]  loctset   検索位置
 * @param[in]      hidx      辞書マウント位置
 *
 * @retval                0   検索候補なし
 * @retval                1   検索候補あり
 */
static NJ_INT16 tdic_search_data_yominashi(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 hidx) {

    NJ_UINT8 *tdtbl_addr, *tdtbl_tmp;
    NJ_UINT32 current, word_next = 0;
    NJ_UINT32 bottom = loctset->loct.bottom;
    NJ_INT32 hindo = INIT_HINDO;
    NJ_INT32 hindo_cur = INIT_HINDO;
    NJ_INT32 hindo_chk = INIT_HINDO;
    NJ_UINT8 bottomflg = 0;


    /* 初回検索時は そのまま抜ける */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        loctset->loct.status = NJ_ST_SEARCH_READY;
        loctset->loct.current_info = CURRENT_INFO_SET;
        return 1;
    }

    /* 読みなし予測は頻度順検索のみ対応の為、
     * 頻度順以外の場合は、検索候補なしとする */
    if (condition->mode != NJ_CUR_MODE_FREQ) {
        loctset->loct.status = NJ_ST_SEARCH_END_EXT; /*NCH_DEF*/
        return 0; /*NCH_DEF*/        /* 検索候補なし */
    }

    /* つながりデータ領域の先頭アドレスを取得 */
    tdtbl_addr = TSUNAGARI_TOP_ADDR(loctset->loct.handle);
    /* つながり頻度を取得する */
    tdtbl_tmp = TSUNAGARI_DATA_ADDR(tdtbl_addr, loctset->loct.current);

    /* 頻度学習＋属性データを加味した頻度へ変換 */
    hindo_cur = get_hindo(iwnn, TSUNAGARI_WORD_DATA_NO(tdtbl_tmp), 
                          TSUNAGARI_HINDO_DATA(tdtbl_tmp), loctset, NULL, hidx);

    current = loctset->loct.current;
    if (current >= bottom) {
        current = loctset->loct.top;
    } else {
        current += 1;
    }

    /* 現在位置から終端位置までデータを検索 */
    while (current <= bottom) {
        tdtbl_tmp = TSUNAGARI_DATA_ADDR(tdtbl_addr, current);
        if (HINDO_TSUNAGARI_YOSOKU(tdtbl_tmp)) {        /* 予測で使用 */
            /* 頻度学習＋属性データを加味した頻度へ変換 */
            hindo = get_hindo(iwnn, TSUNAGARI_WORD_DATA_NO(tdtbl_tmp),
                              TSUNAGARI_HINDO_DATA(tdtbl_tmp), loctset, NULL, hidx);

            if ((current > loctset->loct.current) && (hindo == hindo_cur)) {
                /* 検索中頻度と等しく、且つ検索候補がカレント位置以降の場合,
                 * その検索候補位置を返す */
                loctset->loct.status = NJ_ST_SEARCH_READY;
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->loct.current = current;
                tdtbl_tmp = TSUNAGARI_DATA_ADDR(tdtbl_addr, loctset->loct.current);
                loctset->loct.attr   = get_attr(loctset->loct.handle, TSUNAGARI_WORD_DATA_NO(tdtbl_tmp));
                return 1;
            } else if (hindo < hindo_cur) {
                /* カレントより頻度が低く、その中で最高頻度のものを取得 */
                /* 検索中の最高頻度と同じで、単語IDが先のものを取得     */
                if ((hindo_chk < hindo) || ((current < word_next) && (hindo_chk == hindo))) {
                    hindo_chk = hindo;
                    word_next = current;
                }
            } else {
            }
        }
        current++;
        if ((current > bottom) && (bottomflg == 0)) {
            bottomflg = 1;
            current = loctset->loct.top;
            bottom = loctset->loct.current;
        }
    }

    /* つながりデータのが見つかった場合 */
    if (word_next > 0) {
        /* 検索候補位置情報を更新 */
        /* 辞書頻度を元に評価値を算出 */
        loctset->cache_freq = CALCULATE_HINDO(hindo_chk, loctset->dic_freq.base, 
                                              loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
        loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
        loctset->loct.status = NJ_ST_SEARCH_READY;
        loctset->loct.current_info = CURRENT_INFO_SET;
        loctset->loct.current = word_next;
        tdtbl_tmp = TSUNAGARI_DATA_ADDR(tdtbl_addr, loctset->loct.current);
        loctset->loct.attr   = get_attr(loctset->loct.handle, TSUNAGARI_WORD_DATA_NO(tdtbl_tmp));
        return 1;
    }

    /* 検索終了の属性を設定 */
    loctset->loct.status = NJ_ST_SEARCH_END_EXT;
    return 0;

}


/**
 * 正引き前方一致検索−あいまい検索時、次候補を取得する
 *
 * @param[in,out]      iwnn  解析情報クラス
 * @param[in]       loctset  検索位置
 * @param[in]     psrhCache  キャッシュ管理領域
 * @param[in]         abIdx  キャッシュ情報領域のインデックス
 * @param[in]     yomi_clen  検索対象読み文字列長(文字数)
 * @param[in]       dic_idx  辞書のインデックス
 * @param[in]          mode  検索制限の使用有無 (NJ_SEARCH_DISMANTLING_CONTROL参照)
 *
 * @retval       INIT_HINDO   検索候補なし
 * @retval              >=0   頻度
 */
static NJ_INT32 tdic_get_next_data(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset,
                                   NJ_SEARCH_CACHE *psrhCache,
                                   NJ_UINT16 abIdx, NJ_UINT16 yomi_clen, NJ_UINT16 dic_idx,
                                   NJ_UINT8 mode) {
    NJ_UINT8 *data;
    NJ_INT32 hindo = INIT_HINDO;
    NJ_INT32 hindo_max = INIT_HINDO;
    NJ_UINT32 cursor;
    NJ_UINT32 hindo_data = 0;
    NJ_UINT8 *hdtbl_addr;
    NJ_UINT32 bottom;
    NJ_UINT8 bottomflg = 0;
    NJ_INT32 hindo_cur = INIT_HINDO;

    NJ_UINT8 relation_chk_flg = 0;

    NJ_UINT8 *ext_top;

    NJ_UINT8 no_used_flg;
    NJ_UINT16 yomi_clen_funfun;
    NJ_UINT8 is_hicomp = 1;

    NJ_INT16 ret;
    NJ_SEARCH_LOCATION_SET tmp_loctset;


    if (HAS_MORE_ENTRY(psrhCache->storebuff[abIdx].current)) {
        /* 頻度学習値利用に必要なデータを取得する */
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL) {
            ext_top = EXT_AREA_TOP_ADDR((NJ_UINT8*)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], loctset->loct.handle);
        } else {
            ext_top = NULL;
        }

        /* 頻度データ領域の先頭アドレスを取得 */
        hdtbl_addr = HINDO_TOP_ADDR(loctset->loct.handle);
        data = HINDO_YOSOKU_ADDR(hdtbl_addr, psrhCache->storebuff[abIdx].current);
        hindo_cur = get_hindo(iwnn, psrhCache->storebuff[abIdx].current,
                              HINDO_DATA(data), loctset, &relation_chk_flg, dic_idx);

        if (NJ_GET_DIC_TYPE(loctset->loct.handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
            /* 高圧縮タイプ判定用 */
            is_hicomp = 0;
        }

        cursor = psrhCache->storebuff[abIdx].current;
        bottom = psrhCache->storebuff[abIdx].bottom;
        if (cursor >= bottom) {
            cursor = psrhCache->storebuff[abIdx].top;
        } else {
            cursor += 1;
        }
        /* 該当頻度データ領域の終端を超えた場合は抜ける */
        if (yomi_clen >= iwnn->environment.option.char_min) {
            yomi_clen_funfun = yomi_clen;
        } else {
            yomi_clen_funfun = iwnn->environment.option.char_min;
        }
        while (cursor <= bottom) {

            /* 検索制限の解除フラグをチェックする */
            no_used_flg = 0;
            if ((mode & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) {
                /* 単語使用有無の確認 */
                if ((!IS_USED_FOR_FORECAST(cursor, hdtbl_addr, ext_top, yomi_clen_funfun)) ||
                    (!IS_FUNFUN_OK_BY_HICOMP_TYPE(is_hicomp, loctset->loct.handle, cursor, iwnn->environment.option.char_min, iwnn->environment.option.char_max))) {
                    no_used_flg = 1;
                }
            } else {
                /* 単語検索の時のみ通る */
                /* 単語使用有無の確認 */
                if (!IS_USED_FOR_FORECAST(cursor, hdtbl_addr, ext_top, 3)) {
                    no_used_flg = 1;
                } 
            }
            if (no_used_flg) {
                cursor++;
                if ((cursor > bottom) && (bottomflg == 0)) {
                    bottomflg = 1;
                    cursor = psrhCache->storebuff[abIdx].top;
                    bottom = psrhCache->storebuff[abIdx].current;
                }
                continue;
            }

            /* 辞書頻度を元に評価値を算出 */
            data = HINDO_YOSOKU_ADDR(hdtbl_addr, cursor);
            hindo = get_hindo(iwnn, cursor, HINDO_DATA(data), loctset, &relation_chk_flg, dic_idx);

            /* 検索中頻度と等しく、且つ検索候補がカレント位置以降の場合,
             * その検索候補位置を返す */
            if ((hindo == hindo_cur) && (psrhCache->storebuff[abIdx].current < cursor)) {
                /* 該当候補に対して統合辞書フィルターを実行 */
                if (iwnn->option_data.phase3_filter != NULL) {
                    tmp_loctset = *loctset;
                    tmp_loctset.loct.current = cursor;
                    /* フィルターチェック処理を実行 */
                    ret = is_tdic_filtering(iwnn, &tmp_loctset);
                } else {
                    ret =1;
                }
                if (ret) {
                    psrhCache->storebuff[abIdx].current = cursor;
                    /* 取得した候補が絞込検索候補であった場合 */
                    if (relation_chk_flg == 1) {
                        /* 絞込検索候補フラグの立てる */
                        iwnn->wk_relation_cand_flg[dic_idx] = 1;
                    } else {
                        /* 絞込検索候補フラグの落とす */
                        iwnn->wk_relation_cand_flg[dic_idx] = 0;
                    }
                    return hindo;
                }
            }
            /* 検索中の頻度と一致しない場合,
             * 最大頻度を持つ候補の頻度値,検索位置を 保持しておく。*/
            if (hindo < hindo_cur) {
                if (((hindo == hindo_max) && (cursor < hindo_data)) || (hindo > hindo_max)) {
                    /* 該当候補に対して統合辞書フィルターを実行 */
                    if (iwnn->option_data.phase3_filter != NULL) {
                        tmp_loctset = *loctset;
                        tmp_loctset.loct.current = cursor;
                        /* フィルターチェック処理を実行 */
                        ret = is_tdic_filtering(iwnn, &tmp_loctset);
                    } else {
                        ret =1;
                    }
                    if (ret) {
                        hindo_max = hindo;
                        hindo_data = cursor;
                        /* 取得した候補が絞込検索候補であった場合 */
                        if (relation_chk_flg == 1) {
                            /* 絞込検索候補フラグの立てる */
                            iwnn->wk_relation_cand_flg[dic_idx] = 1;
                        } else {
                            /* 絞込検索候補フラグの落とす */
                            iwnn->wk_relation_cand_flg[dic_idx] = 0;
                        }
                    }
                }
            }
            cursor++;
            if ((cursor > bottom) && (bottomflg == 0)) {
                bottomflg = 1;
                cursor = psrhCache->storebuff[abIdx].top;
                bottom = psrhCache->storebuff[abIdx].current;
            }
        }

        /* 検索終端位置まで見終わった場合 */
        if (hindo_max != INIT_HINDO) {
            /* 頻度値が一致するものが無かったら
             * その頻度値以下で最大頻度の候補を返す */
            psrhCache->storebuff[abIdx].current = hindo_data;
            return hindo_max;
        }
    }

    /* 検索終了の属性を設定 */
    psrhCache->storebuff[abIdx].current = LOC_CURRENT_NO_ENTRY;
    return INIT_HINDO;
}


/**
 * 逆引き前方一致検索−あいまい検索時、次候補を取得する
 *
 * @param[in]   loctset : 検索位置
 * @param[in] psrhCache : キャッシュ管理領域
 * @param[in]     abIdx : キャッシュ情報領域のインデックス
 *
 * @return  頻度(検索候補なし:INIT_HINDO)
 */
static NJ_INT32 tdic_get_next_data_rev(NJ_SEARCH_LOCATION_SET *loctset,
                                       NJ_SEARCH_CACHE *psrhCache,
                                       NJ_UINT16 abIdx) {

    NJ_UINT8 *hitbl_addr;
    NJ_UINT8 *hidx_cur;
    NJ_INT32 hindo;
    NJ_UINT32 cursor;
    NJ_INT32 hindo_max = INIT_HINDO;
    NJ_UINT32 hindo_data = 0;
    NJ_INT32 hindo_cur = 0;


    if (HAS_MORE_ENTRY(psrhCache->storebuff[abIdx].current)) {
        /* 表記文字列インデックス領域の先頭アドレスを取得 */
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO] != NULL) {
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO]);
        } else {
            hitbl_addr = HYOKI_IDX_AREA_TOP_ADDR(loctset->loct.handle);
        }
        hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, psrhCache->storebuff[abIdx].current);
        hindo_cur = (NJ_INT16)HINDO_MORPHO(hidx_cur);
        cursor = psrhCache->storebuff[abIdx].top;
        /* 該当頻度データ領域の終端を超えた場合は抜ける */
        while (cursor <= psrhCache->storebuff[abIdx].bottom) {
            /* 該当する表記文字列情報を取得する */
            /* 該当する表記文字列の頻度を取得する */
            hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, cursor);
            hindo = (NJ_INT16)HINDO_MORPHO(hidx_cur);

            /* 検索中頻度と等しく、且つ検索候補がカレント位置以降の場合,
             * その検索候補位置を返す */
            if ((hindo == hindo_cur) && (psrhCache->storebuff[abIdx].current < cursor)) {
                psrhCache->storebuff[abIdx].current = cursor;
                return hindo;
            }
            /* 検索中の頻度と一致しない場合,
             * 最大頻度を持つ候補の頻度値,検索位置を 保持しておく。*/
            if ((hindo < hindo_cur) && (hindo_max < hindo)) {
                hindo_max = hindo;
                hindo_data = cursor;
            }
            cursor++;
        }

        /* 検索終端位置まで見終わった場合 */
        if (hindo_max != INIT_HINDO) {
            /* 頻度値が一致するものが無かったら
             * その頻度値以下で最大頻度の候補を返す */
            psrhCache->storebuff[abIdx].current = hindo_data;
            return hindo_max;
        }
    }

    /* 検索終了の属性を設定 */
    psrhCache->storebuff[abIdx].current = LOC_CURRENT_NO_ENTRY;
    return INIT_HINDO;

}


/**
 * 正引き前方一致拡張検索−あいまい検索時、次候補を取得する
 *
 * @param[in]      iwnn : 解析情報クラス
 * @param[in] condition : 検索条件
 * @param[in]   loctset : 検索位置
 * @param[in] psrhCache : キャッシュ管理領域
 * @param[in]     abIdx : キャッシュ情報領域のインデックス
 *
 * @return  頻度(検索候補なし:INIT_HINDO)
 */
static NJ_INT32 tdic_get_next_data_fore_ext(NJ_CLASS *iwnn,
                                            NJ_SEARCH_CONDITION *condition,
                                            NJ_SEARCH_LOCATION_SET *loctset,
                                            NJ_SEARCH_CACHE *psrhCache,
                                            NJ_UINT16 abIdx) {

    NJ_UINT8 *hitbl_addr;
    NJ_UINT8 *hidx_cur;
    NJ_INT32 hindo;
    NJ_UINT32 cursor;
    NJ_INT32 hindo_max = INIT_HINDO;
    NJ_UINT32 hindo_data = 0;
    NJ_INT32 hindo_cur = 0;
    NJ_UINT8 no_used_flg;


    if (HAS_MORE_ENTRY(psrhCache->storebuff[abIdx].current)) {
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
            /* 表記文字列インデックス領域の先頭アドレスを取得 */
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT]);
            hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, psrhCache->storebuff[abIdx].current);
            hindo_cur = (NJ_INT16)HINDO_DATA(hidx_cur);
            cursor = psrhCache->storebuff[abIdx].top;
        } else {
            /* 検索終了の属性を設定 */
            psrhCache->storebuff[abIdx].current = LOC_CURRENT_NO_ENTRY; /*NCH*/
            return INIT_HINDO;                                          /*NCH*/
        }
        /* 該当頻度データ領域の終端を超えた場合は抜ける */
        while (cursor <= psrhCache->storebuff[abIdx].bottom) {
            /* 該当する表記文字列情報を取得する */
            /* 該当する表記文字列の頻度を取得する */
            hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, cursor);
            no_used_flg = 0;
            if ((condition->ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) {
                /* 単語使用有無の確認 */
                if ((HINDO_EXT_AREA_YOSOKU(hidx_cur)) &&
                    (is_funfun_ok_ext(loctset->loct.handle, HYOKI_INDEX_WORD_NO(hidx_cur), iwnn->environment.option.char_min, iwnn->environment.option.char_max))) {
                    no_used_flg = 1;
                }
            } else {
                /* 辞書引きAPIからのみ通る */
                no_used_flg = 1;
            }
            if (!no_used_flg) {
                cursor++;
                continue;
            }

            hindo = (NJ_INT16)HINDO_DATA(hidx_cur);

            /* 検索中頻度と等しく、且つ検索候補がカレント位置以降の場合,
             * その検索候補位置を返す */
            if ((hindo == hindo_cur) && (psrhCache->storebuff[abIdx].current < cursor)) {
                psrhCache->storebuff[abIdx].current = cursor;
                return hindo;
            }
            /* 検索中の頻度と一致しない場合,
             * 最大頻度を持つ候補の頻度値,検索位置を 保持しておく。*/
            if ((hindo < hindo_cur) && (hindo_max < hindo)) {
                hindo_max = hindo;
                hindo_data = cursor;
            }
            cursor++;
        }

        /* 検索終端位置まで見終わった場合 */
        if (hindo_max != INIT_HINDO) {
            /* 頻度値が一致するものが無かったら
             * その頻度値以下で最大頻度の候補を返す */
            psrhCache->storebuff[abIdx].current = hindo_data;
            return hindo_max;
        }
    }

    /* 検索終了の属性を設定 */
    psrhCache->storebuff[abIdx].current = LOC_CURRENT_NO_ENTRY;
    return INIT_HINDO;

}


/**
 * 正引き前方一致検索−あいまい検索時、指定候補の頻度を取得する
 *
 * @param[in,out]      iwnn  解析情報クラス
 * @param[in]       loctset  検索位置
 * @param[in]     psrhCache  キャッシュ管理領域
 * @param[in]         abIdx  キャッシュ情報領域のインデックス
 * @param[in]       dic_idx  辞書のインデックス
 *
 * @retval       INIT_HINDO   検索候補なし
 * @retval               >0   頻度
 * @retval               <0   頻度
 */
static NJ_INT32 tdic_get_word_freq(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset,
                                   NJ_SEARCH_CACHE *psrhCache, NJ_UINT16 abIdx, NJ_UINT16 dic_idx) {
    NJ_UINT8 *data;
    NJ_UINT8 *hdtbl_addr;
    NJ_INT32 hindo = 0;


    if (psrhCache->storebuff[abIdx].current != LOC_CURRENT_NO_ENTRY) {
        /* 検索単語の格納位置アドレスを取得 */
        hdtbl_addr = HINDO_TOP_ADDR(loctset->loct.handle);
        data = HINDO_YOSOKU_ADDR(hdtbl_addr, psrhCache->storebuff[abIdx].current);
        /* 頻度値を取得する */
        hindo = get_hindo(iwnn, psrhCache->storebuff[abIdx].current, HINDO_DATA(data),
                          loctset, &iwnn->wk_relation_cand_flg[dic_idx], dic_idx);
    } else {
        /* 検索候補なし */
        hindo = INIT_HINDO;
    }

    return hindo;
}


/**
 * 逆引き前方一致検索−あいまい検索時、指定候補の頻度を取得する
 *
 * @param[in]    loctset : 検索位置
 * @param[in]  psrhCache : キャッシュ管理領域
 * @param[in]      abIdx : キャッシュ情報領域のインデックス
 *
 * @retval    INIT_HINDO   検索候補なし
 * @retval            >0   頻度
 * @retval            <0   頻度
 */
static NJ_INT32 tdic_get_word_freq_rev(NJ_SEARCH_LOCATION_SET *loctset,
                                       NJ_SEARCH_CACHE *psrhCache, NJ_UINT16 abIdx) {
    NJ_UINT8 *hidx_cur;
    NJ_UINT8 *hitbl_addr;
    NJ_INT32 hindo = INIT_HINDO;


    if (HAS_MORE_ENTRY(psrhCache->storebuff[abIdx].current)) {
        /* 表記文字列インデックス領域の先頭アドレスを取得 */
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO] != NULL) {
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO]);
        } else {
            hitbl_addr = HYOKI_IDX_AREA_TOP_ADDR(loctset->loct.handle);
        }
        hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, psrhCache->storebuff[abIdx].current);
        hindo = (NJ_INT16)HINDO_MORPHO(hidx_cur);


    } else {
        /* 検索候補なし */
        hindo = INIT_HINDO; /*NCH*/
    }

    return hindo;
}


/**
 * 正引き前方一致拡張検索−あいまい検索時、指定候補の頻度を取得する
 *
 * @param[in]    loctset : 検索位置
 * @param[in]  psrhCache : キャッシュ管理領域
 * @param[in]      abIdx : キャッシュ情報領域のインデックス
 *
 * @retval    INIT_HINDO   検索候補なし
 * @retval            >0   頻度
 * @retval            <0   頻度
 */
static NJ_INT32 tdic_get_word_freq_fore_ext(NJ_SEARCH_LOCATION_SET *loctset,
                                            NJ_SEARCH_CACHE *psrhCache, NJ_UINT16 abIdx) {
    NJ_UINT8 *hidx_cur;
    NJ_UINT8 *hitbl_addr;
    NJ_INT32 hindo = INIT_HINDO;


    if (HAS_MORE_ENTRY(psrhCache->storebuff[abIdx].current)) {
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
            /* 表記文字列インデックス領域の先頭アドレスを取得 */
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT]);
            hidx_cur = HYOKI_INDEX_ADDR(hitbl_addr, psrhCache->storebuff[abIdx].current);
            hindo = (NJ_INT16)HINDO_DATA(hidx_cur);
        } else {
            /* 検索候補なし */
            hindo = INIT_HINDO; /*NCH*/
        }
    } else {
        /* 検索候補なし */
        hindo = INIT_HINDO;
    }

    return hindo;
}


/**
 * 読みなし予測時、統合辞書から検索条件（前確定情報）に該当する単語データを検索し、該当する候補データを取得
 *
 * @param[in]          iwnn  解析情報クラス
 * @param[in]     condition  検索条件
 * @param[in,out]   loctset  検索位置
 * @param[in]          hidx  辞書のインデックス
 *
 * @retval                0   検索候補なし
 * @retval                1   検索候補あり
 */
static NJ_INT16 search_node_yominashi(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition,
                                      NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT16 hidx) {

    NJ_INT32 lop;                                              /* ループカウンター */
    NJ_UINT8 *word_addr;                                       /* 単語データ領域先頭アドレス */
    NJ_UINT32 left, right, mid = 0;                            /* ２分検索用index */
    NJ_CHAR  *yomi;                                            /* 読み文字列 */
    NJ_CHAR  *hyoki;                                           /* 表記文字列 */
    NJ_CHAR  *yomi_prev;                                       /* 前確定の読み文字列 */
    NJ_CHAR  *hyoki_prev;                                      /* 前確定の表記文字列 */
    NJ_UINT8 *word_d;                                          /* 単語データ領域操作用 */
    NJ_CHAR  yomi_buff[NJ_MAX_LEN + NJ_TERM_LEN];              /* 単語の読み文字列 */
    NJ_CHAR  hyoki_buff[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];      /* 単語の候補文字列 */
    NJ_UINT8 *yhtbl_addr;                                      /* 読み・表記領域操作用 */
    NJ_UINT8 *yomi_d, *hyoki_d;
    NJ_UINT8 yomi_d_len, hyoki_d_len;
    NJ_INT16 diff;                                              /* 文字列比較判定結果 */
    NJ_UINT32 index, index_max;                                 /* 単語データ番号 */
    NJ_UINT8 *titbl_addr;                                       /* つながりインデックス領域操作用 */
    NJ_UINT8 *tdtbl_tmp;                                        /* つながりデータ領域操作用 */
    NJ_LEARN_WORD_INFO *lword;
    NJ_INT32 hindo, hindo_max;
    NJ_INT32 cur, cur_max, cur_start;
    NJ_UINT8 is_hicomp = 1;


    if (NJ_GET_DIC_TYPE(loctset->loct.handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /* 前方一致検索又はつながり予測で、空文字列指定時 */
    if (condition->operation == NJ_CUR_OP_FORE) {
        /* 前確定情報(読み・表記)を取得する */
        if (iwnn->previous_selection.count != 0) {
            lword = nje_get_previous_selection(iwnn, 0, NJ_PREVIOUS_SELECTION_NORMAL);
            yomi_prev  = lword->yomi;
            hyoki_prev = lword->hyouki;
        } else {
            /* 取得失敗時は、検索候補なしとする。*/
            loctset->loct.status = NJ_ST_SEARCH_END_EXT; /*NCH_FB*/
            return 0;   /* 検索候補無し */ /*NCH_FB*/
        }
    } else if (condition->operation == NJ_CUR_OP_LINK) {
        /* 検索条件を取得する */
        if ((condition->yomi != NULL) &&
            (condition->kanji != NULL) && (condition->ylen > 0)) {
            yomi_prev  = condition->yomi;
            hyoki_prev = condition->kanji;
        } else {
            /* 取得失敗時は、検索候補なしとする。*/
            loctset->loct.status = NJ_ST_SEARCH_END_EXT; /*NCH_DEF*/
            return 0; /*NCH_DEF*/   /* 検索候補無し */
        }
    } else {
        /*
         * 上位で切り分けを行っている為、処理が通ることはないが、
         * 念のため処理を入れておく。(候補無しを返す)
         */
        loctset->loct.status = NJ_ST_SEARCH_END_EXT; /*NCH*/
        return 0;       /* 検索候補無し */ /*NCH*/
    }

    /* 単語データ領域先頭アドレスを取得 */
    word_addr = WORD_TOP_ADDR(loctset->loct.handle);
    yhtbl_addr = YOMI_HYOKI_TOP_ADDR(loctset->loct.handle);

    /* つながりインデックステーブル先頭アドレスを取得 */
    titbl_addr = TSUNAGARI_IDX_AREA_TOP_ADDR(loctset->loct.handle);

    /* 単語データ領域から前確定の単語データ番号を取得する */
    left = 1;
    right = TSUNAGARI_IDX_AREA_CNT(loctset->loct.handle);
    index = 0;
    while (left <= right) {
        mid = left + ((right - left) / 2);
        word_d = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, word_addr, TSUNAGARI_SRC_NO(titbl_addr, mid));
        yomi_d     = yhtbl_addr + YOMI_DATA_OFFSET(word_d);

        if (!is_hicomp) {
            /* 読み文字列 */
            yomi_d_len = (NJ_UINT8)convert_str_virtual_to_real(loctset->loct.handle, yomi_d, YOMI_LEN_BYTE(word_d), yomi_buff, sizeof(yomi_buff));
            yomi = yomi_buff;

            /* 表記文字列 */
            hyoki_d_len = yomi_d_len;
            hyoki = yomi_buff;

        } else {
            /* 読み文字列 */
            yomi_d_len = YOMI_LEN(word_d);
            for (lop = 0, yomi = yomi_buff; lop < yomi_d_len; lop++) {
                NJ_CHAR_COPY(yomi, yomi_d);
                yomi_d += sizeof(NJ_CHAR);
                yomi++;
            }
            *yomi = NJ_CHAR_NUL;
            yomi = yomi_buff;

            /* 表記文字列 */
            hyoki_d_len = HYOKI_LEN(word_d);
            if (hyoki_d_len == 0) {
                /* 表記文字列長が0の時 */
                if (HYOKI_INFO(word_d) == 0) {
                    /* ひらがな(読み＝表記)設定 */
                    hyoki = yomi_buff;
                } else {
                    /* カタカナ(読みをカタカナに変換)設定 */
                    nje_convert_hira_to_kata(yomi_buff, hyoki_buff, yomi_d_len);
                    hyoki = hyoki_buff;
                }
            } else {
                /* 表記文字列長が>0の時は、辞書から表記文字列データを取得 */
                hyoki_d = yhtbl_addr + HYOKI_DATA_OFFSET(word_d);
                for (lop = 0, hyoki = hyoki_buff; lop < hyoki_d_len; lop++) {
                    NJ_CHAR_COPY(hyoki, hyoki_d);
                    hyoki_d += sizeof(NJ_CHAR);
                    hyoki++;
                }
                *hyoki = NJ_CHAR_NUL;
                hyoki = hyoki_buff;
            }
        }
        /* 対象の読み文字列とデータ領域の読み・表記文字列を比較 */
        diff = nj_strcmp(yomi_prev, yomi);
        if (diff == 0) {
            diff = nj_strcmp(hyoki_prev, hyoki);
            if (diff == 0) {
                /* 該当の単語データ番号を取得 */
                index = mid;
                right = mid - 1;
            } else if (diff < 0) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        } else if (diff < 0) {
            right = mid - 1;
        } else {
            left = mid + 1;
        }
    }
    /* 一致した前確定単語のIDをチェック */
    if (index == 0) {
        /* 一致無しの場合、
         * どの確定文字列につながるか判らないので、検索候補なしとする。*/
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;       /* 検索候補無し */
    }

    /* つながりデータの対象位置から終端まで検索し、終端位置を取得 */
    cur = cur_max = cur_start = TSUNAGARI_DST_NO(titbl_addr, index);
    hindo_max = INIT_HINDO;
    index_max = index;
    for (tdtbl_tmp = TSUNAGARI_DATA_ADDR(TSUNAGARI_TOP_ADDR(loctset->loct.handle), cur);
         (index = TSUNAGARI_WORD_DATA_NO(tdtbl_tmp)) != 0;
         tdtbl_tmp += 4) {
        /* 最高頻度のものを検索する */
        if (HINDO_TSUNAGARI_YOSOKU(tdtbl_tmp)) {
            /* 頻度学習＋属性データを加味した頻度へ変換 */
            hindo = get_hindo(iwnn, index, TSUNAGARI_HINDO_DATA(tdtbl_tmp), loctset, NULL, hidx);
            if (hindo_max < hindo) {
                hindo_max = hindo;
                cur_max = cur;
                index_max = index;
            }
        }
        cur++;
    }
    if (hindo_max == INIT_HINDO) {
        /* 繋がり候補が見つからなかったとき */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;       /* 検索候補無し */
    }

    /* 検索位置、評価値を更新 */
    /* 読みなし予測の場合は、つながりデータのデータ番号を基準とする。 */
    loctset->loct.current = cur_max;
    loctset->loct.attr    = get_attr(loctset->loct.handle, index_max);
    loctset->loct.top     = cur_start;
    loctset->loct.bottom  = cur - 1;
    loctset->cache_freq = CALCULATE_HINDO(hindo_max, loctset->dic_freq.base,
                                          loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
    loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);

    return 1;   /* 検索候補あり */
}


/**
 * 逆引き完全一致,逆引き前方一致,正引き完全一致拡張,正引き前方一致拡張検索の時、<br>
 * 統合辞書から検索条件（表記）に該当する単語を検索し、該当する候補データの情報を取得
 *
 * @param[in]          iwnn : 解析情報クラス
 * @param[in]     condition : 検索条件
 * @param[in,out]   loctset : 検索位置
 *
 * @retval                0   検索候補なし
 * @retval                1   検索候補あり
 */
static NJ_INT16 search_node_rev(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset) {

    NJ_UINT8 *hitbl_addr;                                       /* 表記文字列インデックス領域先頭アドレス */
    NJ_UINT8 *hidx_tmp;                                         /* 表記文字列インデックス領域操作用 */
    NJ_UINT32 lop;                                              /* ループカウンター */
    NJ_UINT32 bottom;
    NJ_INT16 ret = 0;
    NJ_UINT32 hyoki_top = 0, hyoki_bottom = 0;
    NJ_INT32 hindo_tmp = INIT_HINDO;
    NJ_INT32 hindo_max = INIT_HINDO;
    NJ_UINT32 hindo_top = 0;
    NJ_INT32 hindo_chk_flg = 0;
    NJ_INT32 hyoki_cnt;
    NJ_UINT8 no_used_flg;


    /* 逆引き完全一致,逆引き前方一致,正引き完全一致拡張,正引き前方一致拡張検索以外の場合 */
    if ((condition->operation != NJ_CUR_OP_REV) &&
        (condition->operation != NJ_CUR_OP_REV_FORE) &&
        (condition->operation != NJ_CUR_OP_COMP_EXT) &&
        (condition->operation != NJ_CUR_OP_FORE_EXT)) {
        loctset->loct.status = NJ_ST_SEARCH_END_EXT; /*NCH*/
        return 0;       /* 検索候補無し */ /*NCH*/
    }

    /* 正引き完全一致拡張, 正引き前方一致拡張検索の場合 */
    if ((condition->operation == NJ_CUR_OP_COMP_EXT) ||
        (condition->operation == NJ_CUR_OP_FORE_EXT)) {
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
            /* 拡張インデックスデータ 表記文字列インデックス領域の先頭アドレス取得 */
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT]);
            hyoki_cnt = EXT_HYOKI_IDX_AREA_CNT((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_INPUT]);
        } else {
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;    /*NCH*/
            return 0;       /* 検索候補無し */              /*NCH*/
        }
    } else {
        /* 拡張領域(形態素用)にデータの指定がある場合 */
        if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO] != NULL) {
            /* 拡張インデックスデータ(形態素用) 表記文字列インデックス領域の先頭アドレス取得 */
            hitbl_addr = EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO]);
            hyoki_cnt = EXT_HYOKI_IDX_AREA_CNT((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_MORPHO]);
        } else {
            /* 表記文字列インデックス領域の先頭アドレス取得 */
            hitbl_addr = HYOKI_IDX_AREA_TOP_ADDR(loctset->loct.handle);
            hyoki_cnt = HYOKI_IDX_AREA_CNT(loctset->loct.handle);
        }
    }

    /* 読みがある場合 */
    if (condition->ylen > 0) {
        /* 表記文字列から表記文字列インデックステーブルを検索し、
         * 該当する単語データ番号の先頭と終端を検索する */
        ret = search_rev_idx(loctset, condition, condition->yomi, condition->ylen,
                             1, hyoki_cnt,
                             0, &hyoki_top, &hyoki_bottom);

    } else {
        /* 読みが無い場合は、全ての表記インデックス登録単語範囲を
         * 検索範囲とする */
        hyoki_top = 1; /*NCH_FB*/
        hyoki_bottom = hyoki_cnt; /*NCH_FB*/
        if (hyoki_top > hyoki_bottom) { /*NCH_FB*/
            ret = -1; /*NCH_FB*/
        }
    }

    if (ret < 0) {
        /**
         * 逆引き検索, 正引き拡張検索対象ではないと判断し、
         * 検索候補なしとする。
         */
        if ((condition->operation == NJ_CUR_OP_REV) ||
            (condition->operation == NJ_CUR_OP_COMP_EXT)) {
            if (ret == (-2)) {
                /* 候補なし(前方一致有)*/
                loctset->loct.status = NJ_ST_SEARCH_END;
            } else {
                /* 候補なし */
                loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            }
        } else {
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
        }
        return 0;       /* 検索候補無し */
    }

    /* 検索位置を更新する */
    /**
     * 逆引き完全一致,逆引き前方一致,正引き完全一致拡張,正引き前方一致拡張検索の時、
     * 表記文字列インデックステーブルのデータ番号を基準とする
     */
    if ((condition->mode == NJ_CUR_MODE_FREQ) &&
        ((condition->operation == NJ_CUR_OP_REV_FORE) ||
         (condition->operation == NJ_CUR_OP_FORE_EXT))) {
        /* 逆引き前方一致, 正引き前方一致拡張検索時のみ */
        /* 頻度順の場合は、検索対象となるtopからbottomの中で
         * １番頻度の高いものを取得する */
        lop = hyoki_top;
        bottom = hyoki_bottom;
        hindo_tmp = INIT_HINDO;
        hindo_max = INIT_HINDO;
        hindo_top = 0;
        /* 該当する表記文字列インデックステーブルの開始から終端までを検索し、
         * 検索中の単語データの中で最も小さい単語データ番号を取得する */
        if (condition->operation == NJ_CUR_OP_REV_FORE) {
            /* 逆引き前方一致検索の場合 */
            while (lop <= bottom) {
                hidx_tmp = HYOKI_INDEX_ADDR(hitbl_addr, lop);
                hindo_tmp = (NJ_INT16)HINDO_MORPHO(hidx_tmp);

                /* 最高頻度値の単語番号、頻度値を取得 */
                if (hindo_max < hindo_tmp) {
                    hindo_max = hindo_tmp;
                    hindo_top = lop;
                    hindo_chk_flg = 1;
                }
                lop++;
            }
        } else {
            /* 正引き前方一致拡張検索の場合 */
            while (lop <= bottom) {
                no_used_flg = 0;
                if ((condition->ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL) == 0x00) {
                    /* 単語使用有無の確認 */
                    /* 予測利用ONのデータのみデータ確認 */
                    if ((HINDO_EXT_AREA_YOSOKU(HYOKI_INDEX_ADDR(hitbl_addr, lop))) &&
                        (is_funfun_ok_ext(loctset->loct.handle, HYOKI_INDEX_WORD_NO(HYOKI_INDEX_ADDR(hitbl_addr, lop)), iwnn->environment.option.char_min, iwnn->environment.option.char_max))) {
                        no_used_flg = 1;
                    }
                } else {
                    no_used_flg = 1;
                }
                if (!no_used_flg) {
                    lop++;
                    continue;
                }

                hidx_tmp = HYOKI_INDEX_ADDR(hitbl_addr, lop);
                hindo_tmp = (NJ_INT16)HINDO_DATA(hidx_tmp);

                /* 最高頻度値の単語番号、頻度値を取得 */
                if (hindo_max < hindo_tmp) {
                    hindo_max = hindo_tmp;
                    hindo_top = lop;
                    hindo_chk_flg = 1;
                }
                lop++;
            }
        }

        if (hindo_chk_flg == 0) {
            /* 頻度値が更新されていなかった場合、検索候補なしとする */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT; /*NCH*/
            return 0;        /* 検索候補無し */ /*NCH*/
        }

    } else {
        /* 頻度順の場合は、逆引き完全一致, 正引き完全一致拡張検索時のみ */
        switch (condition->operation) {
        case NJ_CUR_OP_COMP_EXT:    /* 正引き完全一致拡張検索 */
        case NJ_CUR_OP_FORE_EXT:    /* 正引き前方一致拡張検索 */
            hindo_top = hyoki_top;
            hidx_tmp = HYOKI_INDEX_ADDR(hitbl_addr, hyoki_top);
            hindo_max = (NJ_INT16)HINDO_DATA(hidx_tmp);
            break;
        default:
            hindo_top = hyoki_top;
            hidx_tmp = HYOKI_INDEX_ADDR(hitbl_addr, hyoki_top);
            hindo_max = (NJ_INT16)HINDO_MORPHO(hidx_tmp);
            break;
        }
    }
    loctset->loct.current = hindo_top;
    hidx_tmp = HYOKI_INDEX_ADDR(hitbl_addr, loctset->loct.current);
    loctset->loct.attr   = get_attr(loctset->loct.handle, HYOKI_INDEX_WORD_NO(hidx_tmp));
    loctset->loct.top = hyoki_top;
    loctset->loct.bottom = hyoki_bottom;
    loctset->cache_freq = CALCULATE_HINDO(hindo_max, loctset->dic_freq.base,
                                          loctset->dic_freq.high, FUSION_DIC_FREQ_DIV);
    loctset->cache_freq = NORMALIZE_HINDO(loctset->cache_freq, loctset->dic_freq_max, loctset->dic_freq_min);
    return 1;   /* 検索候補あり */

}


/**
 * 繋がりデータ領域から、該当する候補データの繋がり頻度値を取得
 *
 * @param[in]       top : 検索先頭つながりデータ番号
 * @param[in]    bottom : 検索終端つながりデータ番号
 * @param[in]    handle : 辞書データ情報
 * @param[in] hdtbl_tmp : 該当単語データ頻度領域先頭アドレス
 * @param[in]   word_no : 該当する単語データ番号
 *
 * @return  該当つながり頻度値 (繋がりなし:INIT_HINDO)
 */
static NJ_INT32 get_tsunagari_hindo(NJ_UINT32 top, NJ_UINT32 bottom, NJ_UINT8 *handle,
                                    NJ_UINT8 *hdtbl_tmp, NJ_UINT32 word_no) {
    NJ_INT32 hindo = INIT_HINDO;
    NJ_UINT8 *tdtbl_addr, *tdtbl_tmp;
    NJ_UINT32 tmp_wordno = 0;
    NJ_UINT32 left, right, mid = 0;


    /* topかbottomのどちらかが0の場合、
     * つながり情報がないので、検索せずに終了する。 */
    if ((top == 0) || (bottom == 0)) {
        return INIT_HINDO;
    }

    /* つながりデータ領域の先頭アドレスを取得 */
    tdtbl_addr = TSUNAGARI_TOP_ADDR(handle);

    /* つながりデータ領域に
     * 現在の単語データ番号が存在するか検索する */
    if (HINDO_TSUNAGARI_WORD(hdtbl_tmp)) {
        left = top;
        right = bottom;
        while (left <= right) {
            mid = (left + right) / 2;
            tdtbl_tmp = TSUNAGARI_DATA_ADDR(tdtbl_addr, mid);
            tmp_wordno = TSUNAGARI_WORD_DATA_NO(tdtbl_tmp);
            if (word_no == tmp_wordno) {
                /* つながり頻度を取得する */
                hindo = TSUNAGARI_HINDO_DATA(tdtbl_tmp);
                break;
            } else if (word_no < tmp_wordno) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
    } else {
        /* つながり語彙で無い場合は、処理を終了する */
        return INIT_HINDO;
    }
    return hindo;
}


/**
 * 正引き完全一致検索で２文節以上の場合、つながり情報を利用し、変換補正を行う。
 *
 * @param[in]   handle : 辞書ハンドル
 * @param[in]   wordno : 一致している単語データ番号
 * @param[in]     yomi : 読み文字列
 * @param[in]     ylen : 解析終了文字配列長
 * @param[out] loctset : 検索位置
 *
 * @retval          >0   繋がり文節数
 * @retval           0   候補無し
 */
static NJ_INT16 search_connect_word(NJ_DIC_HANDLE handle,
                                    NJ_UINT32 wordno, NJ_CHAR *yomi,
                                    NJ_UINT16 ylen,
                                    NJ_SEARCH_LOCATION_SET *loctset ) {

    NJ_UINT8 *titbl_addr;       /* つながりインデックス先頭   */
    NJ_INT32  titbl_cnt = 0;    /* つながりインデックス登録数 */
    NJ_UINT8 *tdtbl_addr;       /* つながりデータ先頭         */
    NJ_UINT8 *wtbl_addr;        /* 単語データ先頭             */
    NJ_UINT8 *yhtbl_addr;       /* 読み・表記データ先頭       */
    NJ_INT16 phr_cnt = 1;       /* 接続フレーズ数             */
    NJ_INT16  complete_ylen;    /* 解析完了文字列長          */
    NJ_UINT32 complete_wordno;  /* 解析完了単語番号          */
    NJ_CHAR  *complete_yomi;    /* 解析完了読み文字列        */
    NJ_UINT32 left, right, mid; /* 二分検索用領域            */
    NJ_UINT32 t_src_no;         /* つながり元単語番号        */
    NJ_UINT32 t_dst_no;         /* つながりデータ番号        */
    NJ_UINT8 *data;             /* 単語データ                */
    NJ_UINT8 *wkc;              /* 単語データ読み文字列 */
    NJ_INT16 wkc_len;           /* 単語データ読み文字列長 */
    NJ_INT16 ret = 0;
    NJ_INT32 lop;
    NJ_UINT32 phr_lop;
    NJ_UINT32 cursor;
    NJ_UINT8  *tdtbl_cursor;
    NJ_UINT16 yomi_len;
    NJ_INT32  tmp_hindo;
    NJ_UINT16  chk_len;
    NJ_INT32  chk_hindo;
    NJ_UINT32 chk_cursor;
    NJ_INT32  chk_wordno;
    NJ_CHAR tmp_buf[NJ_MAX_LEN + NJ_TERM_LEN];  /* 高圧縮タイプ仮想文字一時変換用 */
    NJ_INT16 cnv_len;               /* 高圧縮タイプ仮想文字一時変換用 */
    NJ_UINT8 is_hicomp = 1;


    if (NJ_GET_DIC_TYPE(handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /* つながりインデックスの有無をチェック */
    titbl_cnt  = TSUNAGARI_IDX_AREA_CNT(handle);      /* つながりインデックス登録数   */
    if (titbl_cnt == 0) {
        /* つながりインデックスがないため、以下の検索を行わない */
        return phr_cnt;
    }

    /* 検索対象の読み文字列長を取得する */
    yomi_len = nj_strlen(yomi + ylen);
    if (yomi_len == 0) {
        return phr_cnt; /*NCH_FB*/
    }
    
    /* 検索に必要な初期設定情報を取得 */
    titbl_addr = TSUNAGARI_IDX_AREA_TOP_ADDR(handle); /* つながりインデックス先頭取得 */
    tdtbl_addr = TSUNAGARI_TOP_ADDR(handle);          /* つながりデータ先頭取得       */
    wtbl_addr  = WORD_TOP_ADDR(handle);               /* 単語データ先頭取得           */
    yhtbl_addr = YOMI_HYOKI_TOP_ADDR(handle);         /* 読み・表記データ先頭取得     */

    /* 解析完了読み文字列長 */
    complete_ylen   = ylen;
    complete_wordno = wordno;
    complete_yomi   = yomi;

    /* 繋がり情報をクリア */
    for (phr_lop = 0; phr_lop < NJ_MAX_PHR_CONNECT; phr_lop++) {
        loctset->loct.relation[phr_lop] = 0;
    }

    /* つながり情報の検索処理 */
    for (phr_lop = 0; phr_lop < NJ_MAX_PHR_CONNECT; phr_lop++) {

        /****************************************************/
        /* つながりインデックスからつながり元単語番号を検索 */
        /****************************************************/
        left  = 1;
        mid   = 1;
        right = titbl_cnt;
        t_dst_no = 0;
        while (left <= right) {
            /* 中間位置を取得 */
            mid = left + ((right - left) / 2);

            /* 検索位置のつながり元単語番号を取得する。*/
            t_src_no = TSUNAGARI_SRC_NO(titbl_addr, mid);

            if (t_src_no == complete_wordno) {
                /* つながり元単語番号が一致するため、つながりデータ先頭を取得して抜ける */
                t_dst_no = TSUNAGARI_DST_NO(titbl_addr, mid);
                break;
            }

            /* 単語番号から単語データ位置を取得 */
            data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, wtbl_addr, t_src_no);

            /* 読み文字長を取得する */
            wkc_len = YOMI_LEN_FROM_DIC_TYPE(is_hicomp, data);
            if ((wkc_len == complete_ylen) &&
                (t_src_no == complete_wordno)) {
                /* つながり元単語番号が一致するため、つながりデータ先頭を取得して抜ける */
                t_dst_no = TSUNAGARI_DST_NO(titbl_addr, mid); /*NCH_DEF*/
                break; /*NCH_DEF*/
            }

            /* 読み文字列を取得する */
            if (!is_hicomp) {
                cnv_len = get_stem_yomi_string(handle, data, tmp_buf, wkc_len, sizeof(tmp_buf));
                wkc = (NJ_UINT8*)tmp_buf;
                wkc_len = cnv_len;
            } else {
                wkc = yhtbl_addr + YOMI_DATA_OFFSET(data);
            }
            if (wkc_len < complete_ylen) {
                ret = nj_memcmp(wkc, (NJ_UINT8*)complete_yomi, (NJ_UINT16)(wkc_len * sizeof(NJ_CHAR)));
                if (ret == 0) {
                    ret = -1;
                }
            } else {
                ret = nj_memcmp(wkc, (NJ_UINT8*)complete_yomi, (NJ_UINT16)(complete_ylen * sizeof(NJ_CHAR)));
            }
            if (ret >= 0) {
                right = mid - 1;
            } else {
                left  = mid + 1;
            }
        }

        if (t_dst_no == 0) {
            for (lop = left; lop < titbl_cnt; lop++) {
                t_src_no = TSUNAGARI_SRC_NO(titbl_addr, lop);
                if (t_src_no == complete_wordno) {
                    /* つながり元単語番号が一致するため、つながりデータ先頭を取得して抜ける */
                    t_dst_no = TSUNAGARI_DST_NO(titbl_addr, lop); /*NCH_DEF*/
                    break; /*NCH_DEF*/
                }
                /* 単語番号から単語データ位置を取得 */
                data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, wtbl_addr, t_src_no);

                /* 読み文字列を取得する */
                wkc_len = YOMI_LEN_FROM_DIC_TYPE(is_hicomp, data);
                if (wkc_len != complete_ylen) {
                    break;
                }
            }
        }

        /* つながりインデックステーブルに
         * 一致している単語データ番号が存在しなかった場合 */
        if (t_dst_no == 0) {
            break;
        }

        /* 繋がり検索を実施する読みに変更する */
        complete_yomi += complete_ylen;

        /****************************************************/
        /* つながりデータの検索を行う                       */
        /****************************************************/
        chk_len    = 0;
        chk_hindo  = INIT_HINDO;
        chk_cursor = 0;
        chk_wordno = 0;

        /* 繋がりデータ位置を取得 */
        tdtbl_cursor = TSUNAGARI_DATA_ADDR(tdtbl_addr, t_dst_no);
        /* つながりデータの終端になるまでループ */
        while ((cursor = TSUNAGARI_WORD_DATA_NO(tdtbl_cursor)) != 0) { /* 単語データ番号を取得 */
            /* つながりデータを変換で利用しない場合は、次のデータに移る */
            if (TSUNAGARI_HENKAN_ON(tdtbl_cursor) == 0) {
                t_dst_no++;
                tdtbl_cursor = TSUNAGARI_DATA_ADDR(tdtbl_addr, t_dst_no);
                continue;
            }

            /* つながりデータの読み文字列を取得 */
            data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, wtbl_addr, cursor);
            /* データの読み長取得 */
            wkc_len = YOMI_LEN_FROM_DIC_TYPE(is_hicomp, data);
            if (yomi_len < wkc_len) {
                /* つながりデータの読み文字列が検索対象の読み文字列長より
                 * 長い場合は、完全一致しないので次のデータに移る */
                t_dst_no++;
                tdtbl_cursor = TSUNAGARI_DATA_ADDR(tdtbl_addr, t_dst_no);
                continue;
            }
            /* データの読み先頭ポインタ */
            if (!is_hicomp) {
                cnv_len = get_stem_yomi_string(handle, data, tmp_buf, wkc_len, sizeof(tmp_buf));
                wkc = (NJ_UINT8*)tmp_buf;
                wkc_len = cnv_len;
            } else {
                wkc = yhtbl_addr + YOMI_DATA_OFFSET(data);
            }

            /* 比較処理 */
            if (nj_memcmp((NJ_UINT8*)complete_yomi, wkc, (NJ_UINT16)(wkc_len * sizeof(NJ_CHAR))) == 0) {
                /* 変換の頻度を取得 */
                /* つながりデータから頻度値を取得 */
                tmp_hindo = TSUNAGARI_HINDO_DATA(tdtbl_cursor);

                if ((chk_len < wkc_len) || ((chk_len == wkc_len) && (chk_hindo < tmp_hindo))) {
                    /* 一致する文字数が保持しているデータの文字数より大きい場合、
                     * 又は文字数が同じでも、頻度が高い場合、
                     * 一致した単語データ番号を保存しておく
                     */
                    chk_hindo  = tmp_hindo;
                    chk_wordno = cursor;
                    chk_len    = wkc_len;
                    /* つながりデータ番号を記憶する */
                    chk_cursor = t_dst_no;
                    if (yomi_len == wkc_len) {
                        break;
                    }
                }
            }
            t_dst_no++;
            tdtbl_cursor = TSUNAGARI_DATA_ADDR(tdtbl_addr, t_dst_no);
        }

        /* 一致するデータが見つからなかった場合 */
        if (chk_wordno == 0) {
            break;
        }
        
        /* 連続している文節を格納する */
        loctset->loct.relation[phr_cnt-1]   = chk_cursor;

        /* 一致する文節数をインクリメント */
        phr_cnt++;  
        
        /* 再検索を行うために必要な情報を更新する。*/
        complete_ylen   = chk_len;
        complete_wordno = chk_wordno;

        /* 検索対象の読み文字列長を取得する */
        yomi_len = nj_strlen(complete_yomi + complete_ylen);
        if (yomi_len == 0) {
            break;
        }
    }

    if ( phr_cnt > 1 ) {
        /* 一致文節数を格納する */
        loctset->loct.current_info = (phr_cnt << 4) | 0;
    }

    return phr_cnt;
}


/**
 * 該当する単語データ番号に対して、
 * 統合辞書フィルターインターフェースを使用し、該当候補の使用有無を判定する。
 *
 * @param[in]  *iwnn     解析情報クラス
 * @param[in]  *loctset  候補情報 辞書内格納位置
 *
 * @retval   1  候補使用する
 * @retval   0  候補使用しない
 */
static NJ_INT16 is_tdic_filtering(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset) {
    NJ_INT16 ret = 0;
    NJ_INT32 ret32 = 0;

    NJ_WORD tmp_word;
    NJ_CHAR tmp_stroke[NJ_MAX_LEN + NJ_TERM_SIZE];
    NJ_CHAR tmp_string[NJ_MAX_RESULT_LEN + NJ_TERM_SIZE];
    NJ_CHAR tmp_additional[NJ_MAX_ADDITIONAL_LEN + NJ_TERM_SIZE];

    NJ_PHASE3_FILTER_IF phase3_filter_if;
    NJ_PHASE3_FILTER_MESSAGE ph3_filter_message;



    /* 単語情報取得 */
    ret = njd_t_get_word(loctset, &tmp_word);
    if (ret < 0) {
        /*
         * エラーが発生した場合、
         * その候補は使用しない扱いとする。
         */
        return 0; /*NCH*/
    }

    /* 読み文字列取得、設定 */
    ret = njd_t_get_stroke(&tmp_word, tmp_stroke, sizeof(tmp_stroke));
    if (ret < 0) {
        /*
         * エラーが発生した場合、
         * その候補は使用しない扱いとする。
         */
        return 0; /*NCH*/
    }
    ph3_filter_message.stroke_len = ret;
    ph3_filter_message.stroke = &tmp_stroke[0];

    /* 候補文字列取得、設定 */
    ret = njd_t_get_candidate(&tmp_word, tmp_string, sizeof(tmp_string));
    if (ret < 0) {
        /*
         * エラーが発生した場合、
         * その候補は使用しない扱いとする。
         */
        return 0; /*NCH*/
    }
    ph3_filter_message.string_len = ret;
    ph3_filter_message.string = &tmp_string[0];

    /* 付加情報取得、設定 */
    if (HAS_ADDITIONAL_INFO(tmp_word, 0, NJ_GET_DIC_TYPE_EX(tmp_word.stem.loc.type, tmp_word.stem.loc.handle))) {
        /* 付加情報データが存在する場合 */
        ret32 = njd_t_get_additional_info(iwnn, &tmp_word, 0, tmp_additional, sizeof(tmp_additional));
        if (ret32 < 0) {
            /*
             * エラーが発生した場合、
             * その候補は使用しない扱いとする。
             */
            return 0; /*NCH*/
        }
        ph3_filter_message.additional_len = ret;
        ph3_filter_message.additional = &tmp_additional[0];
    } else {
        /* 付加情報データが存在しない場合 */
        ph3_filter_message.additional_len = 0;
        ph3_filter_message.additional = NULL;
    }

    /* オプション設定 */
    ph3_filter_message.option = iwnn->option_data.phase3_option;

    /* 統合辞書フィルタインターフェース呼び出し */
    phase3_filter_if = (NJ_PHASE3_FILTER_IF)(iwnn->option_data.phase3_filter);
    ret = (*phase3_filter_if)(iwnn, &ph3_filter_message);

    return ret;
}


/**
 * 単語検索njd_search_wordで取得した検索結果より、関連候補情報を取得する（検索カーソルは移動せず、現位置の関連候補を返す）
 *
 * @param[in]        loc : 検索位置
 * @param[out]      word : 単語情報
 * @param[in]  mdic_freq : 辞書頻度情報
 *
 * @retval             1   正常
 * @retval            <0   エラー
 */
NJ_INT16 njd_t_get_relational_word(NJ_SEARCH_LOCATION *loc,
                                   NJ_WORD *word, NJ_DIC_FREQ *mdic_freq ) {
    NJ_UINT8 fb, bb;
    NJ_UINT16 hdata;
    NJ_UINT8 *wkc;
    NJ_UINT8 *wtbl_addr;
    NJ_UINT8 *data;
    NJ_UINT32 hindo;
    NJ_UINT16 fhinsi;
    NJ_UINT16 bhinsi;
    NJ_UINT16 fhinsi_jitu = 0;
    NJ_UINT16 bhinsi_jitu = 0;
    NJ_INT16 len = 0;
    NJ_UINT8 lop;
    NJ_UINT32 cursor;
    NJ_UINT8 *tdtbl_addr;
    NJ_UINT8  *tdtbl_cursor;
    NJ_UINT8 is_hicomp = 1;


    /* 変換補正の次候補が0の場合 */
    if (word->stem.loc.relation[0] == 0) {
        /* エラーとする */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_GET_RELATIONAL_WORD, /*NCH_FB*/
                              NJ_ERR_CANNOT_GET_QUE);
    }

    if (NJ_GET_DIC_TYPE(loc->handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /**
     * 変換補正に対する単語番号は、NJ_WORD内に格納しているため、
     * 格納情報の復元を行う
     */
    tdtbl_addr   = TSUNAGARI_TOP_ADDR(loc->handle);          /* つながりデータ先頭取得       */
    tdtbl_cursor = TSUNAGARI_DATA_ADDR(tdtbl_addr, (NJ_UINT32)word->stem.loc.relation[0]);
    cursor       = TSUNAGARI_WORD_DATA_NO(tdtbl_cursor);

    word->stem.loc.current = cursor;
    word->stem.loc.top     = cursor;
    word->stem.loc.bottom  = cursor;
    
    for (lop = 0; lop < (NJ_MAX_PHR_CONNECT-1); lop++) {
        word->stem.loc.relation[lop] = word->stem.loc.relation[lop+1];
    }
    word->stem.loc.relation[NJ_MAX_PHR_CONNECT-1] = 0;

    /* 単語情報取得に必要な辞書フォーマット情報を取得する */
    wtbl_addr = WORD_TOP_ADDR(loc->handle);
    data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, wtbl_addr, word->stem.loc.current);
    /**
     * 変換補正の場合は、頻度値を0にする。
     * hindo = HINDO_DATA(hdtbl_tmp);
     */
    hindo = 0;

    /* 前品詞と後品詞情報を取得する */
    /* 前品詞 設定 */
    fb = FHINSI_NO_BYTE(loc->handle);
    if (BIT_FHINSI(loc->handle)) {
        hdata = HINSI_DATA_FROM_DIC_TYPE(is_hicomp, data);
        fhinsi = GET_BITFIELD_16(hdata, 0, BIT_FHINSI(loc->handle));
    } else {
        fhinsi = 0; /*NCH_FB*/
    }
    /* 品詞番号変換データ領域の該当位置 */
    wkc = (HINSI_NO_AREA_TOP_ADDR(loc->handle) + (NJ_UINT32)(fb * fhinsi));
    /* 実品詞への変換：品詞番号設定 */
    if (fb == 2) {
        fhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(wkc)); /*NCH_FB*/
    } else {
        fhinsi_jitu = (NJ_UINT16)*wkc;
    }
    NJ_SET_FPOS_TO_STEM(word, fhinsi_jitu);


    /* 後品詞 設定 */
    bb = BHINSI_NO_BYTE(loc->handle);
    if (BIT_BHINSI(loc->handle)) {
        hdata = HINSI_DATA_FROM_DIC_TYPE(is_hicomp, data);
        bhinsi = GET_BITFIELD_16(hdata, BIT_FHINSI(loc->handle), BIT_BHINSI(loc->handle));
    } else {
        bhinsi = 0; /*NCH_FB*/
    }
    wkc = (HINSI_NO_AREA_TOP_ADDR(loc->handle)
           + (NJ_UINT32)((fb * (FHINSI_NO_CNT(loc->handle))) + (bb * bhinsi)));
    /* 実品詞への変換：品詞番号設定 */
    if (bb == 2) {
        bhinsi_jitu = (NJ_UINT16)(NJ_INT16_READ(wkc));
    } else {
        bhinsi_jitu = (NJ_UINT16)*wkc;
    }
    NJ_SET_BPOS_TO_STEM(word, bhinsi_jitu);

    /* 辞書頻度から評価値を算出する */
    word->stem.hindo = CALCULATE_HINDO(hindo, mdic_freq->base, mdic_freq->high, FUSION_DIC_FREQ_DIV);
    word->stem.hindo = NORMALIZE_HINDO(word->stem.hindo, NJ_STATE_MAX_FREQ, NJ_STATE_MIN_FREQ);

    /* 読み長のみ格納する */
    /* 読みは格納しない */
    len = YOMI_LEN_FROM_DIC_TYPE(is_hicomp, data);
    NJ_SET_YLEN_TO_STEM(word, len);

    /* 表記長のみ格納する */
    len = (NJ_UINT16)HYOKI_LEN_FROM_DIC_TYPE(is_hicomp, data);
    if (len == 0) {
        /* 無変換候補なので、読み長を候補長とする */
        len = (NJ_UINT16)YOMI_LEN_FROM_DIC_TYPE(is_hicomp, data); /*NCH_FB*/
    }
    NJ_SET_KLEN_TO_STEM(word, len);

    /* 擬似候補の種類をクリア   */
    word->stem.type = 0;

    return 1;
}


/**
 * 頻度学習領域サイズ取得処理
 *
 * @param[in]     iwnn        解析情報クラス
 * @param[in]     handle      辞書ハンドル
 * @param[out]    size        頻度学習領域サイズ
 *
 * @retval  1    正常終了     ※ 値は1のみ
 */
NJ_INT16 njd_t_get_ext_area_size(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT32 *size) {
    NJ_INT16  ret;
    NJ_UINT32 ret_size;
    NJ_UINT32 word_cnt;


    ret = 1;
    ret_size = 0;

    /* 頻度学習領域識別子*/
    ret_size += NJ_DIC_EXT_ID_LEN;

    /* 頻度学習領域サイズ */
    ret_size += sizeof(NJ_UINT32);

    /* チェック用データオフセット */
    ret_size += sizeof(NJ_UINT32);

    /* チェック用データサイズ */
    ret_size += sizeof(NJ_UINT32);

    /* 頻度データオフセット */
    ret_size += sizeof(NJ_UINT32);

    /* 頻度データサイズ */
    ret_size += sizeof(NJ_UINT32);

    /* （空き）リザーブ */
    ret_size += sizeof(NJ_UINT32);

    /* （空き）リザーブ */
    ret_size += sizeof(NJ_UINT32);

    /* 
     * チェック用データ：共通データサイズ、拡張情報サイズを加算
     */
    ret_size += NJ_DIC_COMMON_HEADER_SIZE;

    ret_size += NJ_INT32_READ(handle + NJ_DIC_POS_EXT_SIZE);
    
    /*
     * 頻度学習領域サイズ
     *
     */
    /* 辞書の単語登録数を取得 */
    word_cnt = WORD_AREA_CNT(handle);

    /* 単語データ数分だけの領域を確保*/
    ret_size += (NJ_UINT32)(word_cnt * 1);
    
    /* 頻度学習領域識別子*/
    ret_size += NJ_DIC_EXT_ID_LEN;

    *size = ret_size;
    
    return ret;

}


/**
 * 頻度学習領域初期化処理
 *
 * @param[in]     iwnn        解析情報クラス
 * @param[in]     handle      辞書ハンドル
 * @param[out]    ext_data    頻度学習領域
 * @param[in]     size        頻度学習領域サイズ
 *
 * @retval              1     正常終了
 * @retval             <0     エラー
 */
NJ_INT16 njd_t_init_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_data, NJ_UINT32 size) {
    NJ_INT16  ret;
    NJ_UINT32 dic_size;
    NJ_UINT8 *ext_tmp;
    NJ_UINT32 use_size;
    NJ_UINT32 word_cnt;
    NJ_UINT8 *handle_tmp;
    NJ_UINT32 i;


    ret = 1;

    njd_t_get_ext_area_size(iwnn, handle, &dic_size);
    if (size < dic_size) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_INIT_EXT_AREA, NJ_ERR_BUFFER_NOT_ENOUGH);
    }

    /*
     * 頻度学習領域の作成
     *
     */
    ext_tmp = (NJ_UINT8*)ext_data;

    /* 頻度学習領域識別子*/
    NJ_INT32_WRITE(ext_tmp, NJ_DIC_EXT_IDENTIFIER);
    ext_tmp += NJ_DIC_EXT_ID_LEN;

    /* 頻度学習領域サイズ */
    NJ_INT32_WRITE(ext_tmp, dic_size);
    ext_tmp += sizeof(NJ_UINT32);

    /* チェック用データオフセット */
    NJ_INT32_WRITE(ext_tmp, NJ_DIC_EXT_HEAD_SIZE);
    ext_tmp += sizeof(NJ_UINT32);

    /* チェック用データサイズ */
    use_size = (NJ_DIC_COMMON_HEADER_SIZE + NJ_INT32_READ(handle + NJ_DIC_POS_EXT_SIZE));
    NJ_INT32_WRITE(ext_tmp, use_size);
    ext_tmp += sizeof(NJ_UINT32);

    /* 頻度データオフセット */
    NJ_INT32_WRITE(ext_tmp, (NJ_DIC_EXT_HEAD_SIZE + use_size));
    ext_tmp += sizeof(NJ_UINT32);

    /* 辞書の単語登録数を取得 */
    word_cnt = WORD_AREA_CNT(handle);

    /* 頻度データサイズ */
    NJ_INT32_WRITE(ext_tmp, word_cnt);
    ext_tmp += sizeof(NJ_UINT32);

    /* オプションエリア */
    NJ_INT32_WRITE(ext_tmp, 0x00000000);
    ext_tmp += sizeof(NJ_UINT32);

    /* 空き（リザーブ） */
    NJ_INT32_WRITE(ext_tmp, 0xFFFFFFFF);
    ext_tmp += sizeof(NJ_UINT32);

    /* 
     * チェック用データ：共通データ、拡張情報をコピー
     */
    handle_tmp = handle;
    for (i = 0; i < use_size; i++) {
        *ext_tmp++ = *handle_tmp++;
    }

    /*
     * 頻度学習領域
     */
    for (i = 0; i < word_cnt; i++) {
        *ext_tmp++ = 0x00;
    }

    /* 頻度学習領域識別子*/
    NJ_INT32_WRITE(ext_tmp, NJ_DIC_EXT_IDENTIFIER);
    ext_tmp += NJ_DIC_EXT_ID_LEN;

    return ret;

}


/**
 * 頻度学習領域チェック処理
 *
 * @param[in]     iwnn        解析情報クラス
 * @param[in]     handle      辞書ハンドル
 * @param[out]    ext_data    頻度学習領域
 * @param[in]     size        頻度学習領域サイズ
 *
 * @retval               1    正常終了
 * @retval              <0    エラー
 */
NJ_INT16 njd_t_check_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_data, NJ_UINT32 size) {
    NJ_INT16  ret;
    NJ_UINT32 ext_size;
    NJ_UINT8 *ext_tmp;
    NJ_UINT32 top_addr;
    NJ_UINT32 data_size;
    NJ_UINT32 check_size;
    NJ_UINT8 *handle_tmp;
    NJ_UINT32 i;


    ret = 1;

    njd_t_get_ext_area_size(iwnn, handle, &ext_size);
    if (size < ext_size) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_EXT_AREA, NJ_ERR_BUFFER_NOT_ENOUGH);
    }

    /*
     * 頻度学習領域の作成
     *
     */
    ext_tmp = (NJ_UINT8*)ext_data;

    /* 識別子 */
    if (NJ_INT32_READ(ext_tmp) != NJ_DIC_EXT_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_EXT_AREA, NJ_ERR_FORMAT_INVALID);
    }
    ext_tmp += sizeof(NJ_UINT32);

    /* 頻度学習領域サイズ */
    data_size = NJ_INT32_READ(ext_tmp);
    ext_tmp += sizeof(NJ_UINT32);

    if (ext_size != data_size) {
        /* サイズエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_EXT_AREA, NJ_ERR_PARAM_EXT_AREA_INVALID_SIZE);
    }

    /* チェック用データオフセットのアドレスを取得 */
    top_addr = NJ_INT32_READ(ext_tmp);
    ext_tmp += sizeof(NJ_UINT32);

    /* チェック用データサイズを取得 */
    check_size = NJ_INT32_READ(ext_tmp);
    ext_tmp += sizeof(NJ_UINT32);

    /*
     * 辞書と頻度学習領域の共通データ、拡張情報をチェック
     */
    handle_tmp = handle;
    ext_tmp = (NJ_UINT8*)(ext_data);
    ext_tmp += top_addr;
    for (i = 0; i < check_size; i++) {
        if (*ext_tmp++ != *handle_tmp++) {
            ret = NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_EXT_AREA, NJ_ERR_FORMAT_INVALID);
            break;
        }
    }

    /* 終端識別子 */
    if (NJ_INT32_READ((NJ_UINT8*)ext_data + data_size - NJ_DIC_EXT_ID_LEN) != NJ_DIC_EXT_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_EXT_AREA, NJ_ERR_FORMAT_INVALID);
    }

    return ret;

}


/**
 * 拡張インデックスデータチェック処理
 *
 * @param[in]     iwnn        解析情報クラス
 * @param[in]     handle      辞書ハンドル
 * @param[out]    ext_data    拡張領域
 * @param[in]     size        拡張領域サイズ
 *
 * @retval               1    正常終了
 * @retval              <0    エラー
 */
NJ_INT16 njd_t_check_ext_area2(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_data, NJ_UINT32 size) {
    NJ_INT16  ret;
    NJ_UINT8 *ext_tmp;
    NJ_UINT32 top_addr;
    NJ_UINT32 data_size;
    NJ_UINT32 check_size;
    NJ_UINT8 *handle_tmp;
    NJ_UINT32 i;


    ret = 1;

    /*
     * 拡張インデックスデータチェック
     *
     */
    ext_tmp = (NJ_UINT8*)ext_data;

    /* 識別子 */
    if (NJ_INT32_READ(ext_tmp) != NJ_DIC_EXT_INPUT_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_EXT_AREA, NJ_ERR_FORMAT_INVALID);
    }
    ext_tmp += sizeof(NJ_UINT32);

    /* 拡張インデックスデータサイズ */
    data_size = NJ_INT32_READ(ext_tmp);
    ext_tmp += sizeof(NJ_UINT32);

    if (size < data_size) {
        /* サイズエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_EXT_AREA, NJ_ERR_PARAM_EXT_AREA_INVALID_SIZE);
    }

    /* 拡張インデックスチェックデータ オフセット */
    top_addr = NJ_INT32_READ(ext_tmp);
    ext_tmp += sizeof(NJ_UINT32);

    /* 拡張インデックスチェックデータ サイズ */
    check_size = NJ_INT32_READ(ext_tmp);
    ext_tmp += sizeof(NJ_UINT32);

    /* 表記文字列インデックスデータ オフセット */
    ext_tmp += sizeof(NJ_UINT32);
    /* 表記文字列インデックスデータ サイズ */
    ext_tmp += sizeof(NJ_UINT32);
    /* 表記文字列インデックスデータ登録数 */
    ext_tmp += sizeof(NJ_UINT32);
    /* (空き)リザーブ */
    ext_tmp += sizeof(NJ_UINT32);

    /*
     * 辞書と拡張領域の共通データ、拡張情報をチェック
     */
    handle_tmp = handle;
    ext_tmp = (NJ_UINT8*)(ext_data);
    ext_tmp += top_addr;
    for (i = 0; i < check_size; i++) {
        if (*ext_tmp++ != *handle_tmp++) {
            ret = NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_EXT_AREA, NJ_ERR_FORMAT_INVALID);
            break;
        }
    }

    /* 終端識別子 */
    if (NJ_INT32_READ((NJ_UINT8*)ext_data + data_size - NJ_DIC_EXT_ID_LEN) != NJ_DIC_EXT_INPUT_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_EXT_AREA, NJ_ERR_FORMAT_INVALID);
    }

    return ret;

}


/**
 * 予測候補から単語を削除する
 *
 * @param[in]     iwnn        解析情報クラス
 * @param[in]     result      処理結果
 *
 * @retval           1        正常終了
 * @retval          <0        エラー
 */
NJ_INT16 njd_t_delete_word(NJ_CLASS *iwnn, NJ_RESULT *result) {
    NJ_INT16 ret = 1;
    NJ_UINT8 *ext_top;
    NJ_UINT8 *ext_current;
    NJ_UINT16 functype;


    /* 変換種別を取得 */
    functype = NJ_GET_RESULT_FUNC(result->operation_id);

    /* 頻度学習領域の先頭アドレスを取得 */
    ext_top = EXT_AREA_TOP_ADDR((NJ_UINT8*)result->word.stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], result->word.stem.loc.handle);

    ext_current = EXT_AREA_WORD_ADDR(ext_top, result->word.stem.loc.current);

    /* 辞書引き検索 or 絞込検索で頻度学習クリアオプションが設定されていない場合 */
    if (((functype == NJ_FUNC_SEARCH) || (functype == NJ_FUNC_RELATION)) &&
        !(iwnn->option_data.ext_mode & NJ_OPT_CLEARED_EXTAREA_WORD)) {
        /**
         * 頻度学習値の削除を行う。(削除フラグをONにする)
         * 指定単語データの頻度値のみ抽出する
         */
        *ext_current = (NJ_UINT8)(EXT_AREA_WORD_DELETE);

    } else {
        *ext_current = ((*ext_current) & EXT_AREA_WORD_DELETE);

    }

    return ret;
}


/**
 * 学習通知から頻度学習処理を行う。
 *
 * @param[in]     iwnn        解析情報クラス
 * @param[in]     word        単語情報
 * @param[in]     lword       単語登録情報
 * @param[in]     idx         辞書登録番号
 *
 * @retval               0    正常終了
 * @retval              <0    エラー
 */
NJ_INT16 njd_t_learn_word(NJ_CLASS *iwnn, NJ_WORD *word, NJ_LEARN_WORD_INFO *lword, NJ_UINT16 idx) {
    NJ_INT16  ret;
    NJ_UINT8 *ext_tmp;
    NJ_UINT8 *ext_top;
    NJ_UINT8 *ext_current;
    NJ_UINT8  ext_data;
    NJ_UINT32 word_id;
    NJ_UINT32 word_cnt;

    NJ_DIC_HANDLE handle;
    NJ_UINT32 word_top, word_end;                       /* 検索対象単語番号情報 */
    NJ_UINT32 yomi_top, yomi_end;                       /* 検索一致した単語データ先頭,終端番号 */
    NJ_INT32  word_clen = 0;                            /* 検索対象文字列の文字数 */
    NJ_UINT32 word_idx;
    NJ_UINT8 *data;
    NJ_UINT8 *word_addr;
    STEM_DATA_SET stem_set;
    NJ_UINT8 *option_data;
    NJ_CHAR   candidate[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_WORD   word_tmp;
    NJ_UINT16 len;
    NJ_UINT8 *tdtbl_addr, *tdtbl_tmp;
    NJ_UINT8 *hdtbl_addr;
    NJ_UINT16 check_len;
    NJ_UINT16 candidate_len;
    NJ_UINT8 is_hicomp = 1;


    ret = 0;

    /* 頻度学習領域が設定されていない場合は、即正常終了とする。*/
    if (iwnn->dic_set.dic[idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] == NULL) {
        return ret;
    }
    /* 辞書ハンドルをセーブ */
    handle = iwnn->dic_set.dic[idx].handle;
    word_id = 0;
    ext_tmp = NULL;
    
    if (NJ_GET_DIC_TYPE(handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    if ((word->stem.loc.handle == handle) &&
        (word->stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL)) {
        /*
         * 学習単語が、現在対象となっている統合辞書のものだった場合
         * ダイレクトに学習することが可能である。
         */

        /* 頻度学習領域を取得する    */
        ext_tmp = word->stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT];
        /* 検索方法にて取得した候補かを判定し、単語IDを生成する */
        switch (GET_LOCATION_OPERATION(word->stem.loc.status)) {
        case NJ_CUR_OP_COMP:
        case NJ_CUR_OP_FORE:
        case NJ_CUR_OP_COMP_EXT:
        case NJ_CUR_OP_FORE_EXT:
            /* 正引き完全一致検索時 または 正引き前方一致検索時 */
            /* 正引き完全一致拡張検索時 または 正引き前方一致拡張検索時 */
            word_id = word->stem.loc.current;
            break;

        case NJ_CUR_OP_LINK:
            /* つながり予測検索時 */
            /* つながりデータ領域の先頭アドレスを取得 */
            tdtbl_addr = TSUNAGARI_TOP_ADDR(word->stem.loc.handle);
            /* つながりデータ番号から単語データ番号、頻度を取得 */
            tdtbl_tmp  = TSUNAGARI_DATA_ADDR(tdtbl_addr, word->stem.loc.current);
            word_id    = TSUNAGARI_WORD_DATA_NO(tdtbl_tmp);
            break;

        default:
            /* 上記以外、頻度学習の対象外とする。 */
            return 0; /*NCH_FB*/
        }
        
    } else if (iwnn->dic_set.dic[idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL) {
        /*
         * 学習単語が、現在対象となっている統合辞書とは異なる場合に、
         * 統合辞書内を検索し、該当単語が存在した場合に頻度学習を行う。
         */
        ext_tmp = iwnn->dic_set.dic[idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT];
        if (ext_tmp != NULL) {
            /* 
             * 読み文字列インデックステーブルから、対象読み文字列の先頭文字を検索する。
             */
            len = (NJ_UINT16)(lword->yomi_len - lword->fzk_yomi_len);
            ret = search_hash_idx(handle, lword->yomi, len, &word_top, &word_end);

            /* 検索対象読み文字列が読み文字列インデックステーブルに存在しない場合 */
            if (ret < 0) {
                /**
                 * 指定された文字コードが読み文字列インデックステーブルに無い
                 *  = 指定された辞書では、その読み文字列が使われていないとする。
                 */
                return 0;        /* 検索候補無し */
            }

            /* 検索対象読み文字列から単語データ領域を検索し、
             * 該当する単語データ先頭、終端番号を取得する */
            ret = SEARCH_WORD_NO_BY_HICOMP_TYPE(is_hicomp, handle, iwnn->dic_set.dic[idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT], NJ_CUR_OP_COMP,
                                 word_top, word_end, lword->yomi,
                                 len, 0, (-1), &yomi_top, &yomi_end, &word_clen);
            /* 検索対象読み文字列が単語データ領域に存在しない場合 */
            if (ret < 0) {
                return 0;        /* 検索候補無し */
            }

            /* 検索範囲の候補から、前品詞、後品詞が一致することをを検索 */
            word_idx = yomi_top;
            /* 単語データ領域の先頭アドレスを取得 */
            word_addr = WORD_TOP_ADDR(handle);
            ret = 0;
            if (lword->hyouki_len != 0) {
                check_len = lword->hyouki_len - lword->fzk_yomi_len;
            } else {
                /* 読み表記同一、表記カタカナ */
                check_len = lword->yomi_len - lword->fzk_yomi_len; /*NCH_DEF*/
            }

            while (word_idx <= yomi_end) {
                /* 単語の情報を取得する */
                data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, word_addr, word_idx);

                get_stem_word(handle, data, &stem_set, NJ_CUR_OP_COMP, word_idx);
                if (stem_set.candidate_len != 0) {
                    candidate_len = stem_set.candidate_len;
                } else {
                    /* 読み表記同一、表記カタカナ */
                    candidate_len = stem_set.yomi_len;
                }

                if (check_len != candidate_len) {
                    /* 表記文字列の長さが異なる */
                    word_idx++;
                    continue;
                }

                /* 表記文字列を取得する */
                word_tmp.stem.loc.status = NJ_CUR_OP_COMP;
                word_tmp.stem.loc.handle = handle;
                word_tmp.stem.loc.current = word_idx;
                if (njd_t_get_candidate(&word_tmp, candidate, sizeof(candidate)) < 0) {
                    /* エラーのため読み飛ばす */
                    word_idx++; /*NCH_FB*/
                    continue; /*NCH_FB*/
                }
                if (nj_strncmp(lword->hyouki, candidate, candidate_len) != 0) {
                    /* 表記文字列の内容が異なる */
                    word_idx++;
                    continue;
                }

                /* 前品詞 後品詞のチェック */
                if ((lword->f_hinsi == stem_set.fhinsi_jitu) &&
                    (lword->stem_b_hinsi == stem_set.bhinsi_jitu)) {
                    
                    /* 単語ID 格納する。*/
                    word_id = word_idx;
                    break;
                }
                word_idx++;
            }
        }
    }

    if ((ext_tmp != NULL) &&
        (word_id != 0)) {

        /* 単語数を取得する。        */
        word_cnt= WORD_AREA_CNT(ext_tmp + NJ_DIC_EXT_HEAD_SIZE);

        /* 頻度学習領域の先頭を取得 */
        ext_top = EXT_AREA_TOP_ADDR(ext_tmp, handle);
        
        if (word_id > word_cnt) {
            /* 学習対象の単語より単語数が少ない場合 */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_INIT_EXT_AREA, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH_FB*/
        }
        ext_current = EXT_AREA_WORD_ADDR(ext_top, word_id);

        /* 頻度学習値の計算を行う。*/
        ext_data = njd_t_calc_ext_hindo(iwnn, (NJ_UINT8)HINDO_EXT_AREA(ext_current));

        if (ext_data >= EXT_AREA_THRESHOLD) {
            /*
             * 以下の条件の単語を、予測候補として使用する。
             * ・頻度学習値が閾値以上になった単語
             */
            *ext_current = ext_data | EXT_AREA_WORD_YOSOKU;
        } else {
            /* 統合辞書側の頻度データ領域先頭を取得 */
            hdtbl_addr = HINDO_TOP_ADDR(handle);

            if ((HINDO_YOSOKU_WORD(HINDO_YOSOKU_ADDR(hdtbl_addr, word_id))) &&
                !(HINDO_EXT_AREA_DELETE(ext_current))) {
                *ext_current = ext_data | EXT_AREA_WORD_YOSOKU;
            } else {
                *ext_current = ext_data | HINDO_EXT_AREA_OPTION(ext_current);
            }
        }

        if (ext_data >= NJ_DIC_EXT_AREA_T_HINDO) {
            /* 頻度学習で最高頻度になった場合は、最適化フラグを立てる */
            option_data = EXT_AREA_GET_OPTION_ADDR(ext_tmp);
            *option_data |=  EXT_AREA_OPTIMIZE;
        }
    }


    return ret;
}


/**
 * 単語頻度取得関数
 *
 * @param[in]  iwnn       解析情報クラス
 * @param[in]  current    単語ID
 * @param[in]  base_hindo 初期単語頻度
 * @param[in]  loctset    検索位置
 * @param[out] chk_flg    繋がり予測チェックフラグ
 * @param[in]  dic_idx    辞書マウント位置
 *
 * @return                単語頻度
 */
static NJ_INT32 get_hindo(NJ_CLASS *iwnn,
                          NJ_UINT32 current,
                          NJ_INT16  base_hindo,
                          NJ_SEARCH_LOCATION_SET *loctset,
                          NJ_UINT8  *chk_flg,
                          NJ_UINT16 dic_idx ) {
    NJ_DIC_HANDLE handle;
    NJ_INT32      ret_hindo = base_hindo;

    /* 頻度学習用 */
    NJ_UINT8 *ext_top;
    NJ_UINT8 *ext_current;

    /* 絞込検索の加算 */
    NJ_UINT8 *hdtbl_addr;
    NJ_UINT8 *hindo_cur;
    NJ_INT32  hindo_tmp = INIT_HINDO;
    /* 属性データ用 */
    NJ_UINT32 attr_data;
    NJ_INT32  attr_bias;


    /* 辞書ハンドルの取得 */
    handle = loctset->loct.handle;


    /* 頻度学習値利用に必要なデータを取得する */
    if (loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL) {
        ext_top = EXT_AREA_TOP_ADDR((NJ_UINT8 *)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], loctset->loct.handle);
    } else {
        ext_top = NULL;
    }
    ext_current = NULL;

    /* 頻度学習値を取得 */
    if (ext_top != NULL) {
        ext_current = EXT_AREA_WORD_ADDR(ext_top, current);
        ret_hindo  += HINDO_EXT_AREA(ext_current);
        if (ret_hindo > FUSION_DIC_MAX_FREQ) {
            /* 辞書内の最大頻度を超えている場合は、最大頻度に修正 */
            ret_hindo = FUSION_DIC_MAX_FREQ;
        }
    }

    if (chk_flg != NULL) {
        hdtbl_addr = HINDO_TOP_ADDR(handle);
        *chk_flg = 0;

        /* 前確定情報がある時、
         * 且つ絞り込みありが設定された場合 */
        if ((iwnn->previous_selection.count != 0) &&
            ((iwnn->environment.option.mode & NJ_RELATION_ON) != 0)) {
            /* 繋がり語彙の場合のみ、繋がり頻度を取得する */
            hindo_cur = HINDO_HENKAN_ADDR(hdtbl_addr, current);
            hindo_tmp = get_tsunagari_hindo(iwnn->prev_search_range[dic_idx][NJ_SEARCH_RANGE_TOP],
                                            iwnn->prev_search_range[dic_idx][NJ_SEARCH_RANGE_BOTTOM],
                                            handle, hindo_cur, current);
            if (hindo_tmp != INIT_HINDO) {
                ret_hindo += hindo_tmp;
                *chk_flg = 1;
            }
        }
    }

    attr_data = get_attr(handle, current);
    if (attr_data != 0x00000000) {
        /* 属性付き単語の場合、状況に応じて頻度加算・減算する */
        attr_bias = njd_get_attr_bias(iwnn, attr_data);
        ret_hindo += CALCULATE_ATTR_HINDO32(attr_bias,
                                            loctset->dic_freq.base,
                                            loctset->dic_freq.high,
                                            FUSION_DIC_FREQ_DIV);
    }

    return ret_hindo;
}


/**
 * 属性データ取得関数
 *
 * @param[in]  handle     辞書ハンドル
 * @param[in]  current    単語ID
 *
 * @retval           0    属性データなし
 * @retval          !0    属性データ
 */
static NJ_UINT32 get_attr(NJ_DIC_HANDLE handle, NJ_UINT32 current) {
    NJ_UINT32 ret_attr = 0x00000000;
    NJ_UINT8  *tmp_attr;
    /* 属性データ用 */
    NJ_UINT8 *vattr_addr;
    NJ_UINT32 vattr_byte;
    NJ_UINT32 vattr_data;
    NJ_UINT8 *attr_tmp;

    NJ_UINT8 *rattr_addr;
    NJ_UINT32 rattr_cnt;
    NJ_UINT32 rattr_byte;
    NJ_UINT32 attr_i;


    if ((NJ_GET_DIC_TYPE(handle) != NJ_DIC_TYPE_FUSION) &&
        (NJ_GET_DIC_TYPE(handle) != NJ_DIC_TYPE_FUSION_HIGH_COMPRESS)) {
        /* AWNNタイプは、属性データが存在しないものとする */
        return ret_attr;
    }

    /* 属性データの取得 */

    /* 属性データ */
    vattr_addr = ZOKUSEI_AREA_TOP_ADDR(handle);
    vattr_byte = ZOKUSEI_AREA_SIZE(handle);
    /* 実属性データ */
    rattr_addr = REAL_ZOKUSEI_AREA_TOP_ADDR(handle);
    rattr_cnt  = REAL_ZOKUSEI_AREA_CNT(handle);
    rattr_byte = REAL_ZOKUSEI_AREA_SIZE(handle);
    if ((vattr_addr == NULL) || (vattr_byte == 0) ||
        (rattr_addr == NULL) || (rattr_byte == 0) || (rattr_cnt == 0)) {
        /* 属性データの一部が正しくない場合は、属性データが存在しないものとして扱う */
        return ret_attr;
    }

    /* 仮想属性番号を取得 */
    attr_tmp = V_ZOKUSEI_ADDR(vattr_addr, current, vattr_byte);
    vattr_data= (NJ_UINT32)*attr_tmp;
    attr_tmp++;
    for (attr_i = vattr_byte; attr_i > 1; attr_i--, attr_tmp++) {
        vattr_data = (NJ_UINT32)(vattr_data << 8); /*NCH_DEF*/
        vattr_data += (NJ_UINT32)*attr_tmp; /*NCH_DEF*/
    }

    /* 実属性データを取得 */
    if (vattr_data != 0) {
        attr_tmp = R_ZOKUSEI_ADDR(rattr_addr, vattr_data, rattr_byte);
        tmp_attr = (NJ_UINT8*)&ret_attr;
        for (attr_i = rattr_byte; attr_i > 0; attr_i--) {
            *tmp_attr++ = *attr_tmp++;
        }
    }

    return ret_attr;
}


/**
 * 頻度学習領域最適化処理
 *
 * @param[in]     iwnn        解析情報クラス
 * @param[in]     handle      辞書ハンドル
 * @param[out]    ext_data    頻度学習領域
 *
 * @retval               1    正常終了
 * @retval               0    最適化不要
 * @retval              <0    エラー
 */
NJ_INT16 njd_t_optimize_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_data) {
    NJ_INT16  ret;
    NJ_UINT8 *ext_tmp;
    NJ_UINT8 *ext_top;
    NJ_UINT32 i;
    NJ_UINT8  hindo;
    NJ_UINT8 *hindo_top_addr;
    NJ_UINT32 hindo_size;


    if (EXT_AREA_GET_OPTIMIZE_FLAG((NJ_UINT8*)ext_data) == 0) {
        /* 最適化フラグが立っていないため、最適化不要*/
        return 0;
    }

    ret = 1;
    /*
     * 頻度学習領域の作成
     *
     */
    ext_top = (NJ_UINT8*)ext_data;

    /* 頻度データオフセットのアドレスを取得 */
    hindo_top_addr = EXT_AREA_TOP_ADDR(ext_top, handle);

    /* 頻度データサイズを取得 */
    hindo_size = EXT_AREA_SIZE(ext_top);

    /*
     * 頻度学習領域の最適化
     */
    ext_tmp = hindo_top_addr;
    for (i = 0; i < hindo_size; i++) {
        hindo = HINDO_EXT_AREA(ext_tmp);
        if ((hindo > 0) ||
            ((hindo == 0) && (HINDO_EXT_AREA_OPTION(ext_tmp) != 0x00))) {
            hindo = (NJ_UINT8)(hindo / 2);
            if (hindo < EXT_AREA_THRESHOLD) {
                *ext_tmp = hindo | EXT_AREA_WORD_DELETE;
            } else {
                *ext_tmp = hindo | HINDO_EXT_AREA_OPTION(ext_tmp);
            }
        }
        ext_tmp++;
    }

    /*
     * 頻度学習最適化フラグをクリアする。
     */
    ext_tmp = EXT_AREA_GET_OPTION_ADDR((NJ_UINT8*)ext_data);
    *ext_tmp= 0x00;

    return ret;

}


/**
 * 頻度学習値を算出する
 *
 * @param[in]  iwnn   解析情報クラス
 * @param[in]  now    現在の頻度値
 *
 * @return            確定頻度値
 */
NJ_UINT8 njd_t_calc_ext_hindo(NJ_CLASS *iwnn, NJ_UINT8 now) {


    if (NJ_DIC_EXT_AREA_T_HINDO <= now) {
        /* 最大の頻度値であれば、これ以上学習しない */
        return now;
    }

    /* 乱数の算出処理 */
    iwnn->iwnn_now = (NJ_UINT8)(((iwnn->iwnn_now * 37) % 251) + 1);

    if ((iwnn->iwnn_now % NJ_DIC_EXT_AREA_T_HINDO) >= now) {
        /* 学習で頻度値を上げる */
        now++;
    }

    return now;
}


/**
 * 付加情報領域チェック処理
 *
 * @param[in]     iwnn        解析情報クラス
 * @param[in]     handle      辞書ハンドル
 * @param[out]    add_info    付加情報領域
 * @param[in]     size        付加情報領域サイズ
 *
 * @retval               1    正常終了
 * @retval              <0    エラー
 */
NJ_INT16 njd_t_check_additional_info(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *add_info, NJ_UINT32 size) {
    NJ_INT16  ret;
    NJ_UINT8 *add_tmp;
    NJ_UINT32 top_addr;
    NJ_UINT32 data_size;
    NJ_UINT32 check_size;
    NJ_UINT8 *handle_tmp;
    NJ_UINT32 i;
    NJ_UINT32 info_data_size;
    NJ_UINT32 yomi_data_size;
    NJ_UINT32 max_yomi_len;
    NJ_UINT32 flag;


    ret = 1;

    /**
     * ヘッダ領域分のサイズがあるかチェック
     */
    if (size <= NJ_DIC_ADD_HEAD_SIZE) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_ADD_INFO, NJ_ERR_BUFFER_NOT_ENOUGH);
    }

    /*
     * 付加情報領域の作成
     *
     */
    add_tmp    = (NJ_UINT8*)add_info;
    /* 識別子 */
    if (NJ_INT32_READ(add_tmp) != NJ_DIC_ADD_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_ADD_INFO, NJ_ERR_FORMAT_INVALID);
    }
    add_tmp += sizeof(NJ_UINT32);

    /* 付加情報領域サイズ */
    data_size = NJ_INT32_READ(add_tmp);
    add_tmp += sizeof(NJ_UINT32);

    if (size != data_size) {
        /* サイズエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_ADD_INFO, NJ_ERR_PARAM_ADD_INFO_INVALID_SIZE);
    }

    /* チェック用データオフセットのアドレスを取得 */
    top_addr = NJ_INT32_READ(add_tmp);
    add_tmp += sizeof(NJ_UINT32);

    /* チェック用データサイズを取得 */
    check_size = NJ_INT32_READ(add_tmp);
    add_tmp += sizeof(NJ_UINT32);

    /* 付加情報データサイズを取得 */
    add_tmp += sizeof(NJ_UINT32);
    info_data_size = NJ_INT32_READ(add_tmp);
    add_tmp += sizeof(NJ_UINT32);

    /* 読み・表記サイズを取得 */
    add_tmp += sizeof(NJ_UINT32);
    yomi_data_size = NJ_INT32_READ(add_tmp);
    add_tmp += sizeof(NJ_UINT32);

    /* 最大付加情報文字列を取得 */
    max_yomi_len = NJ_INT32_READ(add_tmp);
    add_tmp += sizeof(NJ_UINT32);

    /* オプションフラグを取得 */
    flag = NJ_INT32_READ(add_tmp);
    add_tmp += sizeof(NJ_UINT32);

    /* 文字列長のチェックを実施 */
    if (flag & NJ_DIC_ADD_LEARN_FLG) {
        /* 学習可能データ */
        if ((max_yomi_len / sizeof(NJ_CHAR)) > NJ_MAX_ADDITIONAL_LEN) {
            ret = NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_ADD_INFO, NJ_ERR_FORMAT_INVALID);
        }
    }

    /* 
     * 辞書と頻度学習領域の共通データ、拡張情報をチェック
     */
    handle_tmp = handle;
    add_tmp = (NJ_UINT8*)(add_info);
    add_tmp += top_addr;
    for (i = 0; i < check_size; i++) {
        if (*add_tmp++ != *handle_tmp++) {
            ret = NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_ADD_INFO, NJ_ERR_FORMAT_INVALID);
            break;
        }
    }

    /* 終端識別子 */
    if (NJ_INT32_READ((NJ_UINT8*)add_info + data_size - NJ_DIC_ADD_ID_LEN) != NJ_DIC_ADD_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_CHECK_ADD_INFO, NJ_ERR_FORMAT_INVALID);
    }

    return ret;

}


/**
 * 統合辞書アダプタ  付加情報文字列取得
 *
 * @param[in]       iwnn : 解析情報クラス
 * @param[in]       word : 単語情報(NJ_RESULT->wordを指定)
 * @param[in]      index : 付加情報インデックス
 * @param[out]  add_info : 付加情報文字列格納バッファ
 * @param[in]       size : 付加情報文字列格納バッファサイズ(byte)
 *
 * @retval            <0   エラー
 * @retval           >=0   取得文字配列長(ヌル文字含まず)
 */
NJ_INT32 njd_t_get_additional_info(NJ_CLASS *iwnn, NJ_WORD *word, NJ_UINT8 index, NJ_CHAR *add_info, NJ_UINT32 size) {
    NJ_SEARCH_LOCATION *loc;
    NJ_UINT8 *data;
    NJ_UINT32 len = 0;
    NJ_UINT32 current;
    NJ_UINT8 *data_top_addr;
    NJ_UINT8 *word_top_addr;
    NJ_UINT8 *word_addr;



    /* 該当候補データのアドレスを取得する */
    loc = &word->stem.loc;

    switch (GET_LOCATION_OPERATION(word->stem.loc.status)) {
    case NJ_CUR_OP_COMP:
    case NJ_CUR_OP_FORE:
        /* 正引き完全一致検索時 */
        /* 正引き前方一致検索時 */
        current = loc->current;
        break;

    case NJ_CUR_OP_LINK:
        /* つながり予測検索時 */
        /* つながりデータ番号から単語データ番号、頻度を取得 */
        current = TSUNAGARI_WORD_DATA_NO(TSUNAGARI_DATA_ADDR(TSUNAGARI_TOP_ADDR(loc->handle), loc->current)); /*NCH_DEF*/
        break; /*NCH_DEF*/

    case NJ_CUR_OP_REV:
    case NJ_CUR_OP_REV_FORE:
        /* 逆引き前方一致検索時 */
        /* 表記文字列インデックス番号から単語データ番号を取得 */
        if (loc->ext_area[NJ_TYPE_EXT_AREA_MORPHO] != NULL) {
            current = HYOKI_INDEX_WORD_NO(HYOKI_INDEX_ADDR(EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loc->ext_area[NJ_TYPE_EXT_AREA_MORPHO]), loc->current));
        } else {
            current = HYOKI_INDEX_WORD_NO(HYOKI_INDEX_ADDR(HYOKI_IDX_AREA_TOP_ADDR(loc->handle), loc->current));
        }
        break;

    case NJ_CUR_OP_COMP_EXT:
    case NJ_CUR_OP_FORE_EXT:
        /* 正引き完全一致拡張検索時 */
        /* 正引き前方一致拡張検索時 */
        /* 拡張領域にデータの指定がある場合 */
        if (loc->ext_area[NJ_TYPE_EXT_AREA_INPUT] != NULL) {
            /* 表記文字列インデックス番号から単語データ番号を取得 */
            current = HYOKI_INDEX_WORD_NO(HYOKI_INDEX_ADDR(EXT_HYOKI_IDX_AREA_TOP_ADDR((NJ_UINT8 *)loc->ext_area[NJ_TYPE_EXT_AREA_INPUT]), loc->current));
        } else {
            /* 付加情報文字列長0で返す */
            return len;
        }
        break;

    default:
        /* 上記以外の検索結果の場合はエラーとする。*/
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_GET_CANDIDATE, NJ_ERR_INVALID_RESULT); /*NCH*/
    }

    data_top_addr = loc->add_info[index];
    /* 付加情報データ＆文字列領域の先頭アドレスを取得 */
    word_top_addr = ADD_WORD_TOP_ADDR(data_top_addr);
    word_addr = ADD_WORD_DATA_ADDR(word_top_addr, current);

    len  = ADD_STRING_LEN(word_addr);

    if (size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_GET_ADD_INFO, NJ_ERR_BUFFER_NOT_ENOUGH);
    }
    if ((index == 0) &&
        (ADD_LEARN_FLG(data_top_addr) == NJ_DIC_ADD_LEARN_FLG)) {
        if (len > NJ_MAX_ADDITIONAL_LEN) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_GET_ADD_INFO, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH_DEF*/
        }
    }

    data = ADD_STRING_TOP_ADDR(data_top_addr) + ADD_STRING_DATA_OFFSET(word_addr);

    /* 文字列を取得 */
    nj_memcpy((NJ_UINT8*)add_info, data, (NJ_UINT16)(len * sizeof(NJ_CHAR)));

    /* 読み文字列を NULLでターミネートする */
    *(add_info + len) = NJ_CHAR_NUL;

    return len;
}


/**
 * 単語削除通知から頻度学習クリア処理を行う。
 *
 * @param[in,out] iwnn : 解析情報クラス
 * @param[in]     info : 単語登録情報
 * @param[in]      idx : 辞書登録番号
 *
 * @retval               0    正常終了
 * @retval              <0    エラー
 */
NJ_INT16 njd_t_delete_word_ext(NJ_CLASS *iwnn, NJ_WORD_INFO *info, NJ_UINT16 idx) {
    NJ_INT16  ret;
    NJ_UINT8 *ext_tmp;
    NJ_UINT8 *ext_top;
    NJ_UINT8 *ext_current;
    NJ_UINT32 word_id;
    NJ_UINT32 word_cnt;

    NJ_DIC_HANDLE handle;
    NJ_UINT32 word_top, word_end;                       /* 検索対象単語番号情報 */
    NJ_UINT32 yomi_top, yomi_end;                       /* 検索一致した単語データ先頭,終端番号 */
    NJ_INT32  word_clen = 0;                            /* 検索対象文字列の文字数 */
    NJ_UINT32 word_idx;
    NJ_UINT8 *data;
    NJ_UINT8 *word_addr;
    STEM_DATA_SET stem_set;
    NJ_CHAR   candidate[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_WORD   word_tmp;
    NJ_UINT16 len;
    NJ_UINT16 check_len;
    NJ_UINT16 candidate_len;
    NJ_UINT8 is_hicomp = 1;


    ret = 0;

    /* 頻度学習領域が設定されていない場合は、即正常終了とする。*/
    if (iwnn->dic_set.dic[idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] == NULL) {
        return ret;

    } else {
        /*
         * 学習単語が、現在対象となっている統合辞書とは異なる場合に、
         * 統合辞書内を検索し、該当単語が存在した場合に頻度学習クリアを行う。
         */
        /* 辞書ハンドルをセーブ */
        handle = iwnn->dic_set.dic[idx].handle;
        word_id = 0;
        ext_tmp = iwnn->dic_set.dic[idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT];
        if (NJ_GET_DIC_TYPE(handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
            /* 高圧縮タイプ判定用 */
            is_hicomp = 0;
        }
        if (ext_tmp != NULL) {
            /* 
             * 読み文字列インデックステーブルから、対象読み文字列の先頭文字を検索する。
             */
            len = (NJ_UINT16)(info->stem.yomi_len - info->fzk.yomi_len);
            ret = search_hash_idx(handle, info->yomi, len, &word_top, &word_end);

            /* 検索対象読み文字列が読み文字列インデックステーブルに存在しない場合 */
            if (ret < 0) {
                /**
                 * 指定された文字コードが読み文字列インデックステーブルに無い
                 *  = 指定された辞書では、その読み文字列が使われていないとする。
                 */
                return 0;        /* 検索候補無し */
            }

            /*
             * 検索対象読み文字列から単語データ領域を検索し、
             * 該当する単語データ先頭、終端番号を取得する
             */
            ret = SEARCH_WORD_NO_BY_HICOMP_TYPE(is_hicomp, handle, iwnn->dic_set.dic[idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT], NJ_CUR_OP_COMP,
                                 word_top, word_end, info->yomi,
                                 len, 0, (-1), &yomi_top, &yomi_end, &word_clen);
            /* 検索対象読み文字列が単語データ領域に存在しない場合 */
            if (ret < 0) {
                return 0;        /* 検索候補無し */
            }

            /* 検索範囲の候補から読み・表記情報が一致することを検索 */
            word_idx = yomi_top;
            /* 単語データ領域の先頭アドレスを取得 */
            word_addr = WORD_TOP_ADDR(handle);
            ret = 0;
            if (info->stem.kouho_len != 0) {
                check_len = info->stem.kouho_len - info->fzk.yomi_len;
            } else {
                /* 読み表記同一、表記カタカナ */
                check_len = info->stem.yomi_len - info->fzk.yomi_len; /*NCH_FB*/
            }

            while (word_idx <= yomi_end) {
                /* 単語の情報を取得する */
                data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, word_addr, word_idx);

                get_stem_word(handle, data, &stem_set, NJ_CUR_OP_COMP, word_idx);
                if (stem_set.candidate_len != 0) {
                    candidate_len = stem_set.candidate_len;
                } else {
                    /* 読み表記同一、表記カタカナ */
                    candidate_len = stem_set.yomi_len;
                }

                if (check_len != candidate_len) {
                    /* 表記文字列の長さが異なる */
                    word_idx++;
                    continue;
                }

                /* 表記文字列を取得する */
                word_tmp.stem.loc.status = NJ_CUR_OP_COMP;
                word_tmp.stem.loc.handle = handle;
                word_tmp.stem.loc.current = word_idx;
                if (njd_t_get_candidate(&word_tmp, candidate, sizeof(candidate)) < 0) {
                    /* エラーのため読み飛ばす */
                    word_idx++; /*NCH_FB*/
                    continue; /*NCH_FB*/
                }
                if (nj_strncmp(info->kouho, candidate, candidate_len) != 0) {
                    /* 表記文字列の内容が異なる */
                    word_idx++;
                    continue;
                }

                /* 単語ID 格納して処理を抜ける */
                word_id = word_idx;

                if (word_id != 0) {

                    /* 単語数を取得する。       */
                    word_cnt= WORD_AREA_CNT(ext_tmp + NJ_DIC_EXT_HEAD_SIZE);

                    /* 頻度学習領域の先頭を取得 */
                    ext_top = EXT_AREA_TOP_ADDR(ext_tmp, handle);

                    if (word_id > word_cnt) {
                        /* 学習対象の単語より単語数が少ない場合 */
                        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_INIT_EXT_AREA, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH_FB*/
                    }
                    ext_current = EXT_AREA_WORD_ADDR(ext_top, word_id);

                    *ext_current = ((*ext_current) & EXT_AREA_WORD_DELETE);
                    /* 単語番号をクリア */
                    word_id = 0;
                    word_idx++;
                }
            }
        }
    }

    return ret;
}

/**
 * ユーザープロファイルデータサイズ取得を行う。
 *
 * @param[in]   iwnn            解析情報クラス
 * @param[in]   handle          辞書ハンドル
 * @param[in]   ext_area        頻度学習領域
 * @param[out]  ext_hindo_info  頻度学習値情報構造体
 *
 * @retval   0  正常終了
 * @retval  <0  エラー
 */
NJ_INT32 njd_t_get_user_prof_data_size(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_VOID *ext_area, NJ_EXT_HINDO_INFO *ext_hindo_info) {
    NJ_INT32 ret = 0;
    NJ_UINT32 word_cnt = 0;
    NJ_UINT32 chk_size;
    NJ_UINT32 cnt;
    NJ_UINT32 data_size;
    NJ_UINT8 *ext_top;
    NJ_UINT8 *ext_cur;
    NJ_UINT8 *wtbl_addr;
    NJ_UINT8 *data_cur;
    NJ_UINT8 *yhdata_addr;
    NJ_CHAR tmp_buff[(NJD_MAX_CHARACTER_LEN / sizeof(NJ_CHAR)) + NJ_TERM_LEN];
    NJ_INT16 ryomi_len;
    NJ_UINT8 hindo_idx;
    NJ_UINT8 is_hicomp = 1;


    if (NJ_GET_DIC_TYPE(handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /* 辞書データより、単語登録数を取得 */
    word_cnt = WORD_AREA_CNT(handle);

    /* 頻度学習データより、頻度学習データ数を取得 */
    chk_size = EXT_AREA_SIZE((NJ_UINT8*)ext_area);

    /*
     * 頻度学習データは単語登録数 × 1byte である為、
     * 比較すれば、辞書データの単語登録数と一致するはずである。
     */
    if (word_cnt != chk_size) {
        /* 辞書データと頻度学習データが一致しない場合 */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_GET_USER_PROF_DATA_SIZE, NJ_ERR_PARAM_EXT_AREA_INVALID);
    }

    /* 単語データ領域先頭アドレスを取得 */
    wtbl_addr = WORD_TOP_ADDR(handle);

    /* 頻度学習データの先頭アドレスを取得 */
    ext_top = EXT_AREA_TOP_ADDR((NJ_UINT8 *)ext_area, handle);
    ext_cur = ext_top;

    /* 単語登録個数ループ */
    for (cnt = 1; cnt <= word_cnt; cnt++) {
        data_size = 0;
        if (*ext_cur != 0) {
            data_cur = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, wtbl_addr, cnt);

            /* 読みサイズ(1byte)を加算 */
            data_size += NJ_EXT_HINDO_YOMI_DATA_SIZE;

            /* 表記サイズ(1byte)を加算 */
            data_size += NJ_EXT_HINDO_HYOKI_DATA_SIZE;

            /* 高圧縮タイプは表記サイズを加算しない */
            if (!is_hicomp) {
                yhdata_addr = YOMI_HYOKI_TOP_ADDR(handle) + YOMI_DATA_OFFSET(data_cur);
                ryomi_len = convert_str_virtual_to_real(handle, yhdata_addr, YOMI_LEN_BYTE(data_cur), tmp_buff, sizeof(tmp_buff));

                /* 読み文字列データサイズを加算 */
                data_size += (ryomi_len * sizeof(NJ_CHAR));
            } else {
                /* 読み文字列データサイズを加算 */
                data_size += YOMI_LEN_BYTE(data_cur);

                /* 表記文字列データサイズを加算 */
                data_size += HYOKI_LEN_BYTE(data_cur);
            }

            /* 前品詞番号(2byte)を加算 */
            data_size += NJ_EXT_HINDO_HINSI_DATA_SIZE;

            /* 後品詞番号(2byte)を加算 */
            data_size += NJ_EXT_HINDO_HINSI_DATA_SIZE;

            /* 頻度学習データ(1byte)を加算 */
            data_size += NJ_EXT_HINDO_HINDO_DATA_SIZE;

            if (HINDO_EXT_AREA_DELETE(ext_cur)) {
                /* 削除フラグが立っている場合 */
                ext_hindo_info->delete_data.size += data_size;
                ext_hindo_info->delete_data.word_cnt++;
            } else {
                /* 頻度情報が更新されている場合 */
                hindo_idx = HINDO_EXT_AREA(ext_cur);
                ext_hindo_info->hindo_data[hindo_idx].size += data_size;
                ext_hindo_info->hindo_data[hindo_idx].word_cnt++;
            }
        }
        ext_cur++;
    }

    return ret;
}


/**
 * ユーザープロファイルデータのデータエクスポートを行う。
 *
 * @param[in]   iwnn            解析情報クラス
 * @param[in]   handle          辞書ハンドル
 * @param[in]   ext_area        頻度学習領域
 * @param[out]  exp_area        エクスポートデータ先頭アドレス
 * @param[in]   ext_hindo_info  頻度学習値情報構造体
 * @param[in]   hindo           エクスポートする頻度学習値リミット
 *
 * @retval   0  正常終了
 * @retval  <0  エラー
 */
NJ_INT32 njd_t_export_user_prof_data(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                                     NJ_VOID *ext_area, NJ_VOID *exp_data, NJ_EXT_HINDO_INFO *ext_hindo_info, NJ_UINT8 hindo) {
    NJ_EXT_HINDO_DATA *prof_data;
    NJ_INT32 ret = 0;
    NJ_UINT32 word_cnt = 0;
    NJ_UINT32 chk_size;
    NJ_UINT32 cnt;
    NJ_UINT32 data_size;
    NJ_UINT8 *ext_top;
    NJ_UINT8 *ext_cur;
    NJ_UINT8 *wtbl_addr;
    NJ_UINT8 *data_cur;
    NJ_CHAR* cp_ptr;
    NJ_UINT8 *wk_data;
    NJ_CHAR tmp_buff[(NJD_MAX_CHARACTER_LEN / sizeof(NJ_CHAR)) + NJ_TERM_LEN];
    NJ_INT16 wk_len, wk_size;
    NJ_UINT16 fhinsi_jitu;
    NJ_UINT16 bhinsi_jitu;
    NJ_INT16 i;
    NJ_UINT8 wk_len_size;
    NJ_UINT8 hindo_idx;
    NJ_UINT8 is_hicomp = 1;


    if (NJ_GET_DIC_TYPE(handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }

    /* 辞書データより、単語登録数を取得 */
    word_cnt = WORD_AREA_CNT(handle);

    /* 頻度学習データより、頻度学習データ数を取得 */
    chk_size = EXT_AREA_SIZE((NJ_UINT8*)ext_area);

    /*
     * 頻度学習データは単語登録数 × 1byte である為、
     * 比較すれば、辞書データの単語登録数と一致するはずである。
     */
    if (word_cnt != chk_size) {
        /* 辞書データと頻度学習データが一致しない場合 */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_EXP_USER_PROF_DATA, NJ_ERR_PARAM_EXT_AREA_INVALID);
    }

    /* 単語データ領域先頭アドレスを取得 */
    wtbl_addr = WORD_TOP_ADDR(handle);

    /* 頻度学習データの先頭アドレスを取得 */
    ext_top = EXT_AREA_TOP_ADDR((NJ_UINT8 *)ext_area, handle);
    ext_cur = ext_top;

    /* 単語登録個数ループ */
    for (cnt = 1; cnt <= word_cnt; cnt++) {
        if ((*ext_cur != 0) && 
            ((HINDO_EXT_AREA_DELETE(ext_cur)) || (HINDO_DATA(ext_cur) >= hindo))) {
            data_size = 0;
            if (HINDO_EXT_AREA_DELETE(ext_cur)) {
                prof_data = &ext_hindo_info->delete_data;
            } else {
                hindo_idx = HINDO_EXT_AREA(ext_cur);
                prof_data = &ext_hindo_info->hindo_data[hindo_idx];
            }

            /* データエクスポート位置を取得 */
            wk_data = (NJ_UINT8 *)exp_data + prof_data->exp_offset;
            data_cur = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, wtbl_addr, cnt);

            /* 高圧縮タイプ */
            if (!is_hicomp) {
                /* バイト長を取得 */
                wk_len = YOMI_LEN_FROM_DIC_TYPE(is_hicomp, data_cur);
                /* バイト長を文字列長として実文字変換後の文字配列長を取得 */
                wk_size = get_stem_yomi_string(handle, data_cur, tmp_buff, wk_len, (NJD_MAX_CHARACTER_LEN / sizeof(NJ_CHAR)));

                /* 読みサイズ(1byte)を加算 */
                wk_len_size = (wk_size * sizeof(NJ_CHAR));
                *wk_data = wk_len_size;
                data_size += NJ_EXT_HINDO_YOMI_DATA_SIZE;
                wk_data += NJ_EXT_HINDO_YOMI_DATA_SIZE;

                /* 読み文字列データサイズを加算 */
                cp_ptr = tmp_buff;
                i = 0;
                while (i < wk_len_size) {
                    NJ_CHAR_COPY(wk_data, cp_ptr);
                    cp_ptr += 1;
                    wk_data += sizeof(NJ_CHAR);
                    i += sizeof(NJ_CHAR);
                }
                data_size += wk_len_size;

                /* 表記サイズ(1byte)を加算 */
                *wk_data = 0x00;
                data_size += NJ_EXT_HINDO_HYOKI_DATA_SIZE;
                wk_data += NJ_EXT_HINDO_HYOKI_DATA_SIZE;
            } else {
                /* 読みサイズ(1byte)を加算 */
                wk_len_size = YOMI_LEN_BYTE(data_cur);
                *wk_data = wk_len_size;
                data_size += NJ_EXT_HINDO_YOMI_DATA_SIZE;
                wk_data += NJ_EXT_HINDO_YOMI_DATA_SIZE;

                /* 読み文字列データサイズを加算 */
                wk_len = YOMI_LEN_FROM_DIC_TYPE(is_hicomp, data_cur);
                wk_size = get_stem_yomi_string(handle, data_cur, tmp_buff, wk_len, (NJD_MAX_CHARACTER_LEN / sizeof(NJ_CHAR)));
                cp_ptr = tmp_buff;
                i = 0;
                while (i < wk_len_size) {
                    NJ_CHAR_COPY(wk_data, cp_ptr);
                    cp_ptr += 1;
                    wk_data += sizeof(NJ_CHAR);
                    i += sizeof(NJ_CHAR);
                }
                data_size += wk_len_size;

                /* 表記サイズ(1byte)を加算 */
                wk_len_size = HYOKI_LEN_BYTE_FROM_DIC_TYPE(is_hicomp, data_cur);
                *wk_data = HYOKI_LEN_BYTE_INFO(data_cur);
                data_size += NJ_EXT_HINDO_HYOKI_DATA_SIZE;
                wk_data += NJ_EXT_HINDO_HYOKI_DATA_SIZE;

                /* 表記文字列データサイズを加算 */
                wk_len = HYOKI_LEN_FROM_DIC_TYPE(is_hicomp, data_cur);
                if (wk_len > 0) {
                    wk_size = get_candidate_string(handle, data_cur, YOMI_HYOKI_TOP_ADDR(handle), tmp_buff);
                    cp_ptr = tmp_buff;
                    i = 0;
                    while (i < wk_len_size) {
                        NJ_CHAR_COPY(wk_data, cp_ptr);
                        cp_ptr += 1;
                        wk_data += sizeof(NJ_CHAR);
                        i += sizeof(NJ_CHAR);
                    }
                    data_size += wk_len_size;
                }
            }

            /* 前品詞番号・後品詞番号(2byte + 2byte)を加算 */
            fhinsi_jitu = 0;
            bhinsi_jitu = 0;
            get_jitu_hinsi_no(handle, data_cur, &fhinsi_jitu, &bhinsi_jitu);
            NJ_INT16_WRITE(wk_data, fhinsi_jitu);
            data_size += NJ_EXT_HINDO_HINSI_DATA_SIZE;
            wk_data += NJ_EXT_HINDO_HINSI_DATA_SIZE;
            NJ_INT16_WRITE(wk_data, bhinsi_jitu);
            data_size += NJ_EXT_HINDO_HINSI_DATA_SIZE;
            wk_data += NJ_EXT_HINDO_HINSI_DATA_SIZE;

            /* 頻度学習データ(1byte)を加算 */
            *wk_data = *ext_cur;
            data_size += NJ_EXT_HINDO_HINDO_DATA_SIZE;
            wk_data += NJ_EXT_HINDO_HINDO_DATA_SIZE;

            /* オフセット位置をずらす */
            prof_data->exp_offset += data_size;

        }
        ext_cur++;
    }

    return ret;
}


/**
 * ユーザープロファイルデータのデータインポートを行う。
 *
 * @param[in/out]  iwnn        解析情報クラス
 * @param[in]      info        単語情報
 * @param[in]      idx         辞書インデックス番号
 * @param[in]      fhinsi      前品詞番号
 * @param[in]      bhinsi      後品詞番号
 * @param[in]      hindo_data  頻度学習値
 *
 * @retval   0  正常終了
 * @retval  <0  エラー
 */
NJ_INT32 njd_t_import_user_prof_data(NJ_CLASS *iwnn, NJ_WORD_INFO *info, NJ_UINT16 idx,
                                     NJ_INT16 fhinsi, NJ_INT16 bhinsi, NJ_UINT8 hindo_data) {
    NJ_INT16 ret = 0;
    NJ_UINT8 *ext_tmp;
    NJ_UINT8 *ext_top;
    NJ_UINT8 *ext_current;
    NJ_UINT32 word_id;
    NJ_UINT32 word_cnt;

    NJ_DIC_HANDLE handle;
    NJ_UINT32 word_top, word_end;
    NJ_UINT32 yomi_top, yomi_end;
    NJ_INT32 word_clen = 0;
    NJ_UINT32 word_idx;
    NJ_UINT8 *data;
    NJ_UINT8 *word_addr;
    STEM_DATA_SET stem_set;
    NJ_CHAR candidate[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_WORD word_tmp;
    NJ_UINT16 len;
    NJ_UINT16 check_len;
    NJ_UINT16 candidate_len;
    NJ_UINT16 fhinsi_jitu;
    NJ_UINT16 bhinsi_jitu;
    NJ_UINT8 is_hicomp = 1;


    /*
     * 学習単語が、現在対象となっている統合辞書とは異なる場合に、
     * 統合辞書内を検索し、該当単語が存在した場合に頻度学習の更新を行う。
     */
    /* 辞書ハンドルをセーブ */
    handle = iwnn->dic_set.dic[idx].handle;
    word_id = 0;
    ext_tmp = iwnn->dic_set.dic[idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT];
    if (NJ_GET_DIC_TYPE(handle) == NJ_DIC_TYPE_FUSION_HIGH_COMPRESS) {
        /* 高圧縮タイプ判定用 */
        is_hicomp = 0;
    }
    if (ext_tmp != NULL) {
        /* 
         * 読み文字列インデックステーブルから、対象読み文字列の先頭文字を検索する。
         */
        len = (NJ_UINT16)info->stem.yomi_len;
        ret = search_hash_idx(handle, info->yomi, len, &word_top, &word_end);

        /* 検索対象読み文字列が読み文字列インデックステーブルに存在しない場合 */
        if (ret < 0) {
            /**
             * 指定された文字コードが読み文字列インデックステーブルに無い
             *  = 指定された辞書では、その読み文字列が使われていないとする。
             */
            return 0;        /* 検索候補無し */
        }

        /*
         * 検索対象読み文字列から単語データ領域を検索し、
         * 該当する単語データ先頭、終端番号を取得する
         */
        ret = SEARCH_WORD_NO_BY_HICOMP_TYPE(is_hicomp, handle, iwnn->dic_set.dic[idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT], NJ_CUR_OP_COMP,
                             word_top, word_end, info->yomi,
                             len, 0, (-1), &yomi_top, &yomi_end, &word_clen);
        /* 検索対象読み文字列が単語データ領域に存在しない場合 */
        if (ret < 0) {
            return 0;        /* 検索候補無し */
        }

        /* 検索範囲の候補から読み・表記情報が一致することを検索 */
        word_idx = yomi_top;
        /* 単語データ領域の先頭アドレスを取得 */
        word_addr = WORD_TOP_ADDR(handle);
        ret = 0;
        if (info->stem.kouho_len != 0) {
            check_len = info->stem.kouho_len - info->fzk.yomi_len;
        } else {
            /* 読み表記同一、表記カタカナ */
            check_len = info->stem.yomi_len - info->fzk.yomi_len; /*NCH_FB*/
        }

        while (word_idx <= yomi_end) {
            /* 単語の情報を取得する */
            data = WORD_DATA_ADDR_FROM_DIC_TYPE(is_hicomp, word_addr, word_idx);

            get_stem_word(handle, data, &stem_set, NJ_CUR_OP_COMP, word_idx);
            if (stem_set.candidate_len != 0) {
                candidate_len = stem_set.candidate_len;
            } else {
                /* 読み表記同一、表記カタカナ */
                candidate_len = stem_set.yomi_len;
            }

            if (check_len != candidate_len) {
                /* 表記文字列の長さが異なる */
                word_idx++;
                continue;
            }

            /* 表記文字列を取得する */
            word_tmp.stem.loc.status = NJ_CUR_OP_COMP;
            word_tmp.stem.loc.handle = handle;
            word_tmp.stem.loc.current = word_idx;
            if (njd_t_get_candidate(&word_tmp, candidate, sizeof(candidate)) < 0) {
                /* エラーのため読み飛ばす */
                word_idx++; /*NCH_FB*/
                continue; /*NCH_FB*/
            }
            if (nj_strncmp(info->kouho, candidate, candidate_len) != 0) {
                /* 表記文字列の内容が異なる */
                word_idx++;
                continue;
            }

            /* 品詞情報を取得し、確認 */
            fhinsi_jitu = 0;
            bhinsi_jitu = 0;
            get_jitu_hinsi_no(handle, data, &fhinsi_jitu, &bhinsi_jitu);
            if ((fhinsi != fhinsi_jitu) || (bhinsi != bhinsi_jitu)) {
                word_idx++;
                continue;
            }

            /* 単語ID 格納して処理を抜ける */
            word_id = word_idx;

            if (word_id != 0) {

                /* 単語数を取得する。       */
                word_cnt= WORD_AREA_CNT(ext_tmp + NJ_DIC_EXT_HEAD_SIZE);

                /* 頻度学習領域の先頭を取得 */
                ext_top = EXT_AREA_TOP_ADDR(ext_tmp, handle);

                if (word_id > word_cnt) {
                    /* 学習対象の単語より単語数が少ない場合 */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJD_T_IMP_USER_PROF_DATA, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH_FB*/
                }
                ext_current = EXT_AREA_WORD_ADDR(ext_top, word_id);

                /* インポートデータの頻度学習削除フラグが立っている場合 */
                if (HINDO_EXT_AREA_DELETE(&hindo_data)) {
                    /* 頻度学習データの削除フラグを立てる */
                    *ext_current |= EXT_AREA_WORD_DELETE;
                }
                /* 頻度学習データの頻度学習削除フラグが立っている場合 */
                if (HINDO_EXT_AREA_DELETE(ext_current)) {
                    /* 頻度学習データの予測利用フラグを落とす */
                    *ext_current &= 0x7F;
                } else {
                    /* インポートデータの予測利用フラグが立っている場合 */
                    if (HINDO_EXT_AREA_YOSOKU(&hindo_data)) {
                        /* 頻度学習データの予測利用フラグを立てる */
                        *ext_current |= EXT_AREA_WORD_YOSOKU;
                    }
                }
                if (HINDO_DATA(ext_current) < HINDO_DATA(&hindo_data)) {
                    /* 予測利用・削除フラグのみ一旦マスクする */
                    *ext_current &= EXT_AREA_WORD_FLAG_MASK;
                    /* 頻度値エリア(下位6bit)を更新する */
                    *ext_current |= (NJ_UINT8)HINDO_DATA(&hindo_data);
                }

                /* 単語番号をクリア */
                word_id = 0;
                break;
            }
        }
    }

    return ret;
}


/**
 * 実文字コードより仮想文字コードへの変換関数(高圧縮タイプ用)
 *
 * @attention 実文字コードとして完全一致するデータがなければ、該当候補なしとする。(SJIS非対応)
 *
 * @param[in]   handle          辞書ハンドル
 * @param[in]   real_yomi       実文字コード読み文字列
 * @param[in]   real_yomi_len   実文字コード読み配列長
 * @param[out]  conv_yomi       仮想文字コードへの変換後文字列出力バッファ
 * @param[in]   conv_yomi_size  仮想コードへの変換後文字列出力バッファ配列長(=byte長)
 *
 * @return      >=0             読み文字列バイト配列長(=文字配列長)
 * @retval       -1             該当候補なし
 */
static NJ_INT16 convert_str_real_to_virtual(NJ_DIC_HANDLE handle, NJ_CHAR* real_yomi, NJ_UINT16 real_yomi_len, NJ_UINT8* conv_yomi, NJ_UINT16 conv_yomi_size) {
    NJ_UINT32   left, right, mid;           /* 分岐検索用 */
    NJ_CHAR     *r_char, *vtbl_char;        /* 比較文字コード r:実文字 vtbl:仮想 */
    NJ_INT16    yomi_loop;                  /* 実文字配列数処理用 */
    NJ_UINT16   max_cnt, data_size;         /* 実文字コード変換データ情報 */
    NJ_UINT16   conv_proc_size = 0;         /* 処理バイト数 */
    NJ_INT16    diff;                       /* 文字コード比較結果 */
    NJ_UINT8    *vtbl_addr;                 /* 実文字コード変換データエリア */
    NJ_UINT8    diff_loop, real_char_len;   /* 1文字分のデータ処理用 */


    /* [実文字コード変換データ領域]登録数 取得 */
    max_cnt = JITU_MOJI_HENKAN_DATA_AREA_CNT(handle);
    /* [実文字コード変換データ領域]実文字格納バイト数 取得 */
    data_size = JITU_MOJI_HENKAN_DATA_AREA_BYTE_SIZE(handle);
    /* [実文字コード変換データ領域]先頭アドレス 取得 */
    vtbl_addr = JITU_MOJI_HENKAN_DATA_AREA_TOP_ADDR(handle);

    /* INの読み文字列数処理 */
    for (yomi_loop = 0; yomi_loop < real_yomi_len; yomi_loop++) {

        /* NULL終端を加えると変換先バッファサイズに到達する場合 */
        /* または実文字データがNULLになれば処理を抜ける */
        if ((*real_yomi == NJ_CHAR_NUL) ||
            ((conv_proc_size + NJ_TERM_LEN) >= conv_yomi_size)) {
            break;
        }

        /* 比較対象文字のバイト長を取得(1:通常文字コード、2:サロゲートペア) */
        real_char_len = NJ_CHAR_LEN(real_yomi);

        /* 仮想文字テーブル内から比較一致文字コードを二分検索 */
        left = 1;
        right = max_cnt - 1;
        while (left <= right) {
            mid = (left + right) / 2;

            diff = 1;
            /* 1文字ずつNJ_CHAR単位で比較する */
            for (diff_loop = 0; diff_loop < real_char_len; diff_loop++) {
                r_char = (real_yomi + diff_loop);
                vtbl_char = (NJ_CHAR*)(vtbl_addr + (mid * data_size) + (diff_loop * real_char_len));

                diff = NJ_CHAR_DIFF(r_char, vtbl_char);
                if (diff > 0) {
                    left = mid + 1;
                    break;
                } else if (diff < 0) {
                    right = mid - 1;
                    break;
                }
            }

            if (!diff) {
                /* 仮想テーブル文字コードとINの読み文字コードが等しい場合   */
                /* 現インデックス(mid)を仮想文字コードとする                */
                *conv_yomi = (NJ_UINT8)mid;
                conv_yomi++;
                conv_proc_size++;
                break;
            }
        }

        /* 検索文字コードが存在しなければ該当候補なしとして終了する */
        if (left > right) {
            return -1;
        }
        /* 実文字コード参照位置を進める */
        real_yomi += real_char_len;
        /* サロゲートペア時は実読み配列位置も一つ進める */
        if (real_char_len > 1) {
            yomi_loop++;
        }
    }

    /* 最終位置に1byte NULLを設定 */
    *conv_yomi = NJ_BYTE_NUL;

    return conv_proc_size;
}


/**
 * 仮想文字コードより実文字コードへの変換関数(高圧縮タイプ用)
 *
 * @attention 有効インデックスを超える場合は、該当候補なしとする。(SJIS非対応)
 *
 * @param[in]   handle          辞書ハンドル
 * @param[in]   v_yomi          仮想文字コード読み文字列
 * @param[in]   v_yomi_len      仮想文字コード読み配列長
 * @param[out]  conv_yomi       実文字コードへの変換後文字列出力バッファ
 * @param[in]   conv_yomi_size  実文字コードへの変換後文字列出力バッファバイト長
 *
 * @return      >=0             読み文字列バイト配列長
 */
static NJ_INT16 convert_str_virtual_to_real(NJ_DIC_HANDLE handle, NJ_UINT8* v_yomi, NJ_UINT16 v_yomi_len, NJ_CHAR* conv_yomi, NJ_UINT16 conv_yomi_size) {
    NJ_UINT16   yomi_loop;
    NJ_UINT16   max_cnt, data_size;
    NJ_UINT16   conv_proc_size = 0;           /* 処理バイト数 */
    NJ_CHAR     *offset_pos_char;
    NJ_UINT8    *vtbl_addr;
    NJ_UINT8    v_data_len, v_data_size;


    /* [実文字コード変換データ領域]登録数 取得 */
    max_cnt = JITU_MOJI_HENKAN_DATA_AREA_CNT(handle);
    /* [実文字コード変換データ領域]実文字格納バイト数 取得 */
    data_size = JITU_MOJI_HENKAN_DATA_AREA_BYTE_SIZE(handle);
    /* [実文字コード変換データ領域]先頭アドレス 取得 */
    vtbl_addr = JITU_MOJI_HENKAN_DATA_AREA_TOP_ADDR(handle);

    /* INの読み文字列数処理 */
    for (yomi_loop = 0; yomi_loop < v_yomi_len; yomi_loop++) {
        offset_pos_char = (NJ_CHAR*)(vtbl_addr + (*(v_yomi) * data_size));
        /* 仮想テーブルインデックスのデータ配列長を取得(1:非サロゲートペア、2：サロゲートペア) */
        v_data_len = NJ_CHAR_LEN(offset_pos_char);
        v_data_size = (NJ_UINT8)(v_data_len * sizeof(NJ_CHAR));

        if ((conv_proc_size + v_data_size + NJ_TERM_LEN) > conv_yomi_size) {
            /* 変換処理後にNULL終端を加えるとオーバーフローしてしまう場合は処理せず抜ける */
            break;  /*NCH_FB*/
        }

        /* データ長分だけ出力バッファへコピー */
        nj_memcpy((NJ_UINT8*)conv_yomi, (NJ_UINT8*)offset_pos_char, v_data_size);

        /* 参照位置、代入位置、変換サイズを更新 */
        v_yomi++;
        conv_yomi += v_data_len;
        conv_proc_size += v_data_size;
    }

    *(conv_yomi) = NJ_CHAR_NUL;

    return (conv_proc_size / sizeof(NJ_CHAR));
}


