/**
 * @file
 *  [拡張] デコメ絵文字辞書操作用擬似辞書
 *
 * デコメ絵文字辞書の操作を行う擬似辞書
 *
 * @author
 * (C) OMRON SOFTWARE Co., Ltd. 2010 All Rights Reserved.
 */
#include "demoji_giji.h"
#include "nj_lib.h"
#include "nj_err.h"
#include "nj_dicif.h"
#include "njfilter.h"
#include "nj_ext.h"
#include "nj_dic.h"
#include "njd.h"

/************************************************/
/*                 define 宣 言                 */
/************************************************/
/*
 * QUE_TYPE で取得した結果
 */
#define QUE_TYPE_EMPTY  0   /**< QUE_TYPE: 空 */
#define QUE_TYPE_NEXT   0   /**< QUE_TYPE: 前キューの続き */
#define QUE_TYPE_JIRI   1   /**< QUE_TYPE: 自立語 */
#define QUE_TYPE_FZK    2   /**< QUE_TYPE: 付属語 */
/*
 * インデックス情報のシフト方法
 */
#define SHIFT_LEFT      0   /**< インデックスのシフト方法：左シフト */
#define SHIFT_RIGHT     1   /**< インデックスのシフト方法：右シフト */
/**
 * 辞書ヘッダー内の格納位置
 */
#define POS_DATA_OFFSET  0x20
/*
 * 候補順インデックス登録数の位置
 */
#define POS_LEARN_WORD   0x24    /**< 単語登録数記録位置 */
#define POS_MAX_WORD     0x28    /**< 最大登録数記録位置 */
#define POS_QUE_SIZE     0x2C    /**< キューサイズ記録位置 */
#define POS_NEXT_QUE     0x30    /**< 次キュー記録位置 */
#define POS_WRITE_FLG    0x34    /**< 書き込みフラグ記録位置 */
/**
 * 読み順インデックスオフセットの位置
 */
#define POS_INDEX_OFFSET        0x3C
/**
 * 候補順インデックスオフセットの位置
 */
#define POS_INDEX_OFFSET2       0x40
/**
 * 拡張単語情報領域オフセットの位置
 */
#define POS_EXT_DATA_OFFSET     0x44

/**
 * 付加情報領域オフセットの位置
 */
#define POS_ADDITIONAL_DATA_OFFSET  0x38
/**
 * 付加情報文字列バイト数のサイズ
 */
#define ADDITIONAL_DATA_SIZE  1

#define LEARN_INDEX_TOP_ADDR(x) ((x) + (NJ_INT32_READ((x) + POS_INDEX_OFFSET)))
#define LEARN_INDEX_TOP_ADDR2(x) ((x) + (NJ_INT32_READ((x) + POS_INDEX_OFFSET2)))
#define LEARN_DATA_TOP_ADDR(x)  ((x) + (NJ_INT32_READ((x) + POS_DATA_OFFSET)))
#define LEARN_EXT_DATA_TOP_ADDR(x)  ((x) + (NJ_INT32_READ((x) + POS_EXT_DATA_OFFSET)))

#define LEARN_INDEX_BOTTOM_ADDR(x) (LEARN_DATA_TOP_ADDR(x) - 1)

/* 学習領域データの読み文字列先頭オフセット */
#define LEARN_QUE_STRING_OFFSET 5

#define ADDRESS_TO_POS(x,adr)   (((adr) - LEARN_DATA_TOP_ADDR(x)) / QUE_SIZE(x))
#define POS_TO_ADDRESS(x,pos)   (LEARN_DATA_TOP_ADDR(x) + QUE_SIZE(x) * (pos))

#define POS_TO_EXT_ADDRESS(x,pos)   (LEARN_EXT_DATA_TOP_ADDR(x) + LEARN_DIC_EXT_QUE_SIZE * (pos))

#define ADDITIONAL_DATA_TOP_ADDR(x)  ((x) + (NJ_INT32_READ((x) + POS_ADDITIONAL_DATA_OFFSET)))

#define POS_TO_ADD_ADDRESS(x,pos)   (ADDITIONAL_DATA_TOP_ADDR(x) + ((ADDITIONAL_DATA_SIZE + 1 + (NJ_MAX_ADDITIONAL_LEN * sizeof(NJ_CHAR))) * (pos)))

#define GET_UINT16(ptr) ((((NJ_UINT16)(*(ptr))) << 8) | (*((ptr) + 1) & 0x00ff))

#define GET_FPOS_FROM_DATA(x) ((NJ_UINT16)NJ_INT16_READ((x)+1) >> 7)
#define GET_YSIZE_FROM_DATA(x) ((NJ_UINT8)((NJ_UINT16)NJ_INT16_READ((x)+1) & 0x7F))
#define GET_BPOS_FROM_DATA(x) ((NJ_UINT16)NJ_INT16_READ((x)+3) >> 7)
#define GET_KSIZE_FROM_DATA(x) ((NJ_UINT8)((NJ_UINT16)NJ_INT16_READ((x)+3) & 0x7F))
#define GET_BPOS_FROM_EXT_DATA(x) ((NJ_UINT16)NJ_INT16_READ(x) >> 7)
#define GET_YSIZE_FROM_EXT_DATA(x) ((NJ_UINT8)((NJ_UINT16)NJ_INT16_READ(x) & 0x7F))

#define SET_BPOS_AND_YSIZE(x,bpos,ysize)                                \
    NJ_INT16_WRITE((x), ((NJ_UINT16)((bpos) << 7) | ((ysize) & 0x7F)))
#define SET_FPOS_AND_YSIZE(x,fpos,ysize)                                \
    NJ_INT16_WRITE(((x)+1), ((NJ_UINT16)((fpos) << 7) | ((ysize) & 0x7F)))
#define SET_BPOS_AND_KSIZE(x,bpos,ksize)                                \
    NJ_INT16_WRITE(((x)+3), ((NJ_UINT16)((bpos) << 7) | ((ksize) & 0x7F)))

#define GET_TYPE_FROM_DATA(x) (*(x) & 0x03)
#define GET_UFLG_FROM_DATA(x) (*(x) >> 7)
#define GET_FFLG_FROM_DATA(x) ((*(x) >> 6) & 0x01)
#define GET_MFLG_FROM_DATA(x) (*(x) & 0x10)

#define SET_TYPE_UFLG_FFLG(x,type,u,f)                                  \
    (*(x) = (NJ_UINT8)(((type) & 0x03) |                                \
                       (((u) & 0x01) << 7) | (((f) & 0x01) << 6)))
#define SET_TYPE_ALLFLG(x,type,u,f,m)                                   \
    (*(x) = (NJ_UINT8)(((type) & 0x03) |                                \
                       (((u) & 0x01) << 7) | (((f) & 0x01) << 6) | (((m) & 0x01) << 4)))

#define RESET_FFLG(x) (*(x) &= 0xbf)

#define STATE_COPY(to, from)                                    \
    { ((NJ_UINT8*)(to))[0] = ((NJ_UINT8*)(from))[0];            \
        ((NJ_UINT8*)(to))[1] = ((NJ_UINT8*)(from))[1];          \
        ((NJ_UINT8*)(to))[2] = ((NJ_UINT8*)(from))[2];          \
        ((NJ_UINT8*)(to))[3] = ((NJ_UINT8*)(from))[3]; }

/**
 * 頻度順に候補を取得する関数の使用切り分けマクロ
 *
 * 辞書のバージョンに応じて、状況適応対応版(get_cand_by_evaluate_state)と
 * 非対応版(get_cand_by_evaluate)を切り分けて呼び出す。
 *
 * @param[in]               iwnn : 解析情報クラス
 * @param[in]                con : 検索条件
 * @param[in,out]        loctset : 検索位置
 * @param[in]               oper : 検索方法
 *
 * @retval                    <0   エラー
 * @retval                     0   候補なし
 * @retval                     1   候補あり
 */
#define GET_CANDIDATE_BY_EVAL(iwnn, con, loctset, oper)                 \
    (((NJ_GET_DIC_VER((NJ_DIC_HANDLE)((loctset)->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT])) == NJ_DIC_VERSION3) || (NJ_GET_DIC_VER((NJ_DIC_HANDLE)((loctset)->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT])) == NJ_DIC_VERSION4)) ?      \
     degiji_get_cand_by_evaluate_state((iwnn), (con), (loctset), (oper)) :     \
     degiji_get_cand_by_evaluate((iwnn), (con), (loctset), (oper)))

/**
 * 頻度順に候補を取得する関数(曖昧検索)の使用切り分けマクロ
 *
 * 辞書のバージョンに応じて、状況適応対応版(get_cand_by_evaluate2_state)と
 * 非対応版(get_cand_by_evaluate2)を切り分けて呼び出す。
 *
 * @param[in]               iwnn : 解析情報クラス
 * @param[in]                con : 検索条件
 * @param[in,out]        loctset : 検索位置
 * @param[in]               oper : 検索方法
 * @param[in]                idx : 辞書のインデックス
 *
 * @retval                    <0   エラー
 * @retval                     0   候補なし
 * @retval                     1   候補あり
 */
#define GET_CANDIDATE_BY_EVAL2(iwnn, con, loctset, oper, idx)           \
    (((NJ_GET_DIC_VER((NJ_DIC_HANDLE)((loctset)->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT])) == NJ_DIC_VERSION3) || (NJ_GET_DIC_VER((NJ_DIC_HANDLE)((loctset)->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT])) == NJ_DIC_VERSION4)) ?      \
     degiji_get_cand_by_evaluate2_state((iwnn), (con), (loctset), (oper), (idx)) : \
     degiji_get_cand_by_evaluate2((iwnn), (con), (loctset), (oper), (idx)))

/**
 * 文頭属性を含むかどうか？
 *
 * @attention NJ_CAT_FIELD_HEADの定義が変わった場合は変更が必要！
 *
 * @param[in]   handle   辞書ハンドル
 * @param[in]   que_id   キュー番号
 *
 * @retval  0    含まない
 * @retval  !0   含む
 */
#define HAS_BUNTOU_ATTR(handle, que_id)                                 \
    ((POS_TO_EXT_ADDRESS((handle), (que_id)))[0] & (0x80U >> NJ_CAT_FIELD_HEAD))

/**
 * 文頭繋がり検索モードか？
 *
 * @param[in]   iwnn   解析情報クラス
 *
 * @retval  !0   文頭繋がり検索モード
 * @retval  0    文頭繋がり検索モード以外
 */
#define IS_BUNTOU_LINK_SEARCH_MODE(iwnn)                                \
    ((iwnn)->environment.prev_hinsi ==                                  \
     njd_r_get_hinsi((iwnn)->dic_set.rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_BUNTOU_B))

/**
 * 一単語が使用するキューの数を求める
 *
 * @param[in]   que_size   １キューのサイズ
 * @param[in]   str_size   読み＋表記＋単語情報バイトサイズ
 *
 * @return 使用キュー数
 */
#define USE_QUE_NUM(que_size, str_size)    \
    ( (((str_size) % ((que_size) - 1)) == 0)                           \
      ? ((str_size) / ((que_size) - 1))                                \
      : ((str_size) / ((que_size) - 1) + 1) )

/**
 * 循環を考慮して次のキューを求める
 *
 * @param[in]   que   現在のキューID
 * @param[in]   max   最大キュー登録数
 *
 * @return 次キューID
 */
#define NEXT_QUE(que, max)  ( ((que) < ((max) - 1)) ? ((que) + 1) : 0 )

/**
 * 循環を考慮して前のキューを求める
 *
 * @param[in]   que   現在のキューID
 * @param[in]   max   最大キュー登録数
 *
 * @return 前キューID
 */
#define PREV_QUE(que, max)  ( ((que) == 0) ? ((max) - 1) : ((que) - 1) )

/**
 * キューをコピーする
 *
 * @param[in]   handle 辞書ハンドル
 * @param[in]   src    コピー元キューID
 * @param[in]   dst    コピー先キューID
 *
 * @return nj_memcpyと同じ
 */
#define COPY_QUE(handle, src, dst)                                      \
    nj_memcpy(POS_TO_ADDRESS((handle), (dst)), POS_TO_ADDRESS((handle), (src)), QUE_SIZE(handle))


/**
 * キュー拡張情報をコピーする
 *
 * @param[in]   handle 辞書ハンドル
 * @param[in]   src    コピー元キューID
 * @param[in]   dst    コピー先キューID
 *
 * @return nj_memcpyと同じ
 */
#define COPY_QUE_EXT(handle, src, dst)                                  \
    nj_memcpy(POS_TO_EXT_ADDRESS((handle), (dst)), POS_TO_EXT_ADDRESS((handle), (src)), LEARN_DIC_EXT_QUE_SIZE)


/**
 * 初期化頻度値
 */
#define INIT_HINDO          (-10000)

/**
 * 候補無し時のloct.currentの値
 */
#define LOC_CURRENT_NO_ENTRY  0xffffffffU

/**
 * 絵文字辞書の最小サイズ
 *  ＝ 辞書ヘッダー ＋ キュー１個 ＋ インデックス２個分×２セット（共用辞書の場合）＋ 末尾識別子分
 */
#define MIN_SIZE_OF_EMOJI_DIC                                            \
    (NJ_LEARN_DIC_HEADER_SIZE + NJ_EMOJI_QUE_SIZE + 2 * (NJ_INDEX_SIZE * (1+1)) + 4)

/**
 * 絵文字辞書に格納できる最大単語数を得る
 *
 * @param[in]     size 辞書サイズ
 *
 * @return             単語数
 */
#define GET_MAX_WORD_NUM_IN_EMOJI_DIC(size)                              \
    (((size) - NJ_LEARN_DIC_HEADER_SIZE - (2 * NJ_INDEX_SIZE) - 4)      \
     / (NJ_EMOJI_QUE_SIZE + 2 * NJ_INDEX_SIZE))

#define NJ_DIC_UNCOMP_EXT_HEADER_SIZE   0x002C      /**< 辞書ヘッダ: 拡張情報サイズ-非圧縮辞書- */

/**
 * 絵文字辞書のデータ領域オフセットを取得する
 *
 * @param[in]     cnt  辞書単語数
 *
 * @return             オフセット(byte単位)
 */
/* "* 2" としているのは、「インデックス領域」と「表記文字列インデックス領域」の２つが存在するため */
#define GET_DATA_AREA_OFFSET(cnt)                               \
    (NJ_LEARN_DIC_HEADER_SIZE + NJ_INDEX_SIZE * ((cnt)+1) * 2)

/**
 * 絵文字辞書の表記文字列インデックス領域オフセットを取得する
 *
 * @param[in]     cnt  辞書単語数
 *
 * @return             オフセット(byte単位)
 */
#define GET_HYOKI_INDEX_OFFSET(cnt)                             \
    (NJ_LEARN_DIC_HEADER_SIZE + NJ_INDEX_SIZE * ((cnt)+1))

#define GET_LEARN_MAX_WORD_COUNT(h) ((NJ_UINT16)NJ_INT32_READ((h) + POS_MAX_WORD))

#define GET_LEARN_WORD_COUNT(h)                         \
    ((NJ_UINT16)NJ_INT32_READ((h) + POS_LEARN_WORD))
#define SET_LEARN_WORD_COUNT(h, n)                      \
    NJ_INT32_WRITE((h)+POS_LEARN_WORD, (NJ_UINT32)(n))
#define GET_LEARN_NEXT_WORD_POS(h)                      \
    ((NJ_UINT16)NJ_INT32_READ((h) + POS_NEXT_QUE))
#define SET_LEARN_NEXT_WORD_POS(h, id)                  \
    NJ_INT32_WRITE((h)+POS_NEXT_QUE, (NJ_UINT32)(id))
#define QUE_SIZE(h)     ((NJ_UINT16)NJ_INT32_READ((h) + POS_QUE_SIZE))

#define COPY_UINT16(dst,src)    (*(NJ_UINT16 *)(dst) = *(NJ_UINT16 *)(src))

#define SET_GIJI_ERR(x) ((NJ_INT16)((NJ_GET_ERR_CODE(x) >> 8) | 0x8000))

/************************************************/
/*           プ ロ ト タ イ プ 宣 言            */
/************************************************/
static NJ_INT16 degiji_is_continued(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
static NJ_UINT8 *degiji_write_string(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 *dst, NJ_UINT8 *src, NJ_UINT8 size);
static NJ_INT16 degiji_que_strcmp_complete_with_hyouki(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id, NJ_CHAR *yomi, NJ_UINT16 yomi_len, NJ_CHAR *hyouki, NJ_UINT8 multi_flg);
static void degiji_shift_index(NJ_UINT8 *ptr, NJ_UINT8 direction, NJ_UINT16 shift_count);
static NJ_INT16 degiji_get_cand_by_sequential(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern, NJ_UINT8 comp_flg);
static NJ_INT16 degiji_get_cand_by_evaluate(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern);
static NJ_INT16 degiji_get_cand_by_evaluate2(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern, NJ_UINT16 hIdx);
static NJ_INT16 degiji_search_range_by_yomi(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 op, NJ_CHAR *yomi, NJ_UINT16 ylen, NJ_UINT16 *from, NJ_UINT16 *to, NJ_UINT8 *forward_flag);
static NJ_INT16 degiji_search_range_by_yomi2(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 op, NJ_CHAR *yomi, NJ_UINT16 ylen, NJ_UINT16 sfrom, NJ_UINT16 sto, NJ_UINT16 *from, NJ_UINT16 *to,
                                             NJ_UINT8 *forward_flag);
static NJ_INT16 degiji_str_que_cmp(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_CHAR *yomi, NJ_UINT16 yomiLen, NJ_UINT16 que_id, NJ_UINT8 mode, NJ_UINT8 rev);
static NJ_WQUE *degiji_get_que_type_and_next(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
static NJ_WQUE *degiji_get_que_allHinsi(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
static NJ_INT16 degiji_search_empty_que(NJ_DIC_HANDLE handle, NJ_UINT16 *que_id);
static NJ_WQUE *degiji_get_que_yomiLen_and_hyoukiLen(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
static NJ_INT16 degiji_continue_cnt(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
static NJ_UINT8 *degiji_get_search_index_address(NJ_DIC_HANDLE handle, NJ_UINT8 search_pattern);
static NJ_HINDO degiji_get_hindo(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern);
static NJ_HINDO degiji_calculate_hindo(NJ_DIC_HANDLE handle, NJ_INT32 freq, NJ_DIC_FREQ *dic_freq, NJ_INT16 freq_max, NJ_INT16 freq_min);
static NJ_INT16 degiji_get_cand_by_evaluate_state(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern);
static NJ_INT16 degiji_get_cand_by_evaluate2_state(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern, NJ_UINT16 hIdx);
static NJ_INT16 degiji_que_strcmp_include(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id, NJ_CHAR *yomi);
static NJ_INT16 degiji_get_next_data(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond,
                                     NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern,
                                     NJ_DIC_HANDLE handle, NJ_UINT8 *ptr, NJ_UINT32 *current,
                                     NJ_UINT32 top, NJ_UINT32 bottom,
                                     NJ_INT32 *ret_hindo, NJ_UINT8 *current_info);
static NJ_INT16 degiji_search_word(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *con,
                                   NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 comp_flg);
static NJ_INT16 degiji_add_word(NJ_CLASS *iwnn, NJ_LEARN_WORD_INFO *word, NJ_UINT8 *demojidic);
static NJ_INT16 degiji_delete_word(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION *loc);
static NJ_INT16 degiji_get_word(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word);
static NJ_INT16 degiji_get_stroke(NJ_CLASS *iwnn, NJ_WORD *word,
                                  NJ_CHAR *stroke, NJ_UINT16 size);
static NJ_INT16 degiji_get_candidate(NJ_CLASS *iwnn, NJ_WORD *word,
                                     NJ_CHAR *candidate, NJ_UINT16 size);
static NJ_INT16 degiji_check_dic(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 restore);
static NJ_INT16 degiji_init_area(NJ_DIC_HANDLE handle);
static NJ_INT16 degiji_write_learn_data(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id,
                                        NJ_LEARN_WORD_INFO *word, NJ_UINT8 connect, NJ_UINT8 type, NJ_UINT8 undo,
                                        NJ_UINT8 muhenkan);
static NJ_INT16 degiji_delete_index(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
static void degiji_write_uint16_data(NJ_UINT8* ptr, NJ_UINT16 data);
static NJ_WQUE *degiji_get_que(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
static NJ_CHAR *degiji_get_string(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id, NJ_UINT8 *slen);
static NJ_CHAR *degiji_get_hyouki(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id, NJ_UINT8 *slen);
static NJ_UINT16 degiji_search_prev_que(NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
static NJ_INT32 degiji_get_attr_hindo(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
static NJ_UINT16 degiji_search_next_que(NJ_DIC_HANDLE handle, NJ_UINT16 que_id);

/**
 * デコレ絵文字擬似辞書 辞書インタフェース
 *
 * @param[in,out] iwnn      iWnn内部情報(通常は参照のみ)
 * @param[in]     request   iWnnからの処理要求
 *                          - NJG_OP_SEARCH：初回検索
 *                          - NJG_OP_SEARCH_NEXT：次候補検索
 *                          - NJG_OP_GET_WORD_INFO：単語情報取得
 *                          - NJG_OP_GET_STROKE：読み文字列取得
 *                          - NJG_OP_ADD_WORD：単語登録
 *                          - NJG_OP_DEL_WORD：単語削除
 * @param[in,out] message   iWnn←→擬似辞書間でやり取りする情報
 *
 * @retval >=0 [NJG_OP_SEARCHの場合]<br>
 *                1:候補あり、0:候補なし<br>
 *             [NJG_OP_SEARCH_NEXTの場合]<br>
 *                1:候補あり、0:候補なし<br>
 *             [NJG_OP_GET_WORD_INFOの場合]<br>
 *                0:正常終了<br>
 *             [NJG_OP_GET_STROKEの場合]<br>
 *                読み文字列長<br>
 *             [NJG_OP_GET_STRINGの場合]<br>
 *                表記文字列長<br>
 *             [NJG_OP_ADD_WORDの場合]<br>
 *                0:正常終了<br>
 *             [NJG_OP_DEL_WORDの場合]<br>
 *                0:正常終了<br>
 *
 * @retval <0  エラー
 */
NJ_EXTERN NJ_INT16 requestEmojiDictionary(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE *message) {
    NJ_LEARN_WORD_INFO word;
    NJ_INT16 ret;

    switch (request) {
    case NJG_OP_SEARCH:
        if (message->location->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT] == NULL) {
            /* 拡張領域がNULLの場合は、候補作成不可とする */
            message->location->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        /* 絵文字辞書を検索 */
        ret = degiji_search_word(iwnn, message->condition, message->location, 0);
        if (ret < 0) {
            ret = SET_GIJI_ERR(ret);
        }
        return ret;
    case NJG_OP_SEARCH_NEXT:
        /* 絵文字辞書を検索 */
        ret = degiji_search_word(iwnn, message->condition, message->location, 0);
        if (ret < 0) {
            ret = SET_GIJI_ERR(ret);
        }
        return ret;
    case NJG_OP_GET_WORD_INFO:
        /* 単語を取得 */
        ret = degiji_get_word(iwnn, message->location, message->word);
        if (ret < 0) {
            ret = SET_GIJI_ERR(ret);
        }
        return ret;
    case NJG_OP_GET_STROKE:
        /* 読み文字列を作成する */
        ret = degiji_get_stroke(iwnn, message->word, message->stroke, message->stroke_size);
        if (ret < 0) {
            ret = SET_GIJI_ERR(ret);
        }
        return ret;
    case NJG_OP_GET_STRING:
        /* 表記文字列を作成する */
        ret = degiji_get_candidate(iwnn, message->word, message->string, message->string_size);
        if (ret < 0) {
            ret = SET_GIJI_ERR(ret);
        }
        return ret;
    case NJG_OP_ADD_WORD:
        if (message->dicset->dic[message->dic_idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] == NULL) {
            /* 拡張領域がNULLの場合は、エラーとする */
            return SET_GIJI_ERR(NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_ADD_WORD, NJ_ERR_DIC_BROKEN));
        }

        /* 登録情報を作成する */
        nj_strcpy(word.yomi, message->stroke);
        word.yomi_len = (NJ_UINT8)message->stroke_size;
        nj_strcpy(word.hyouki, message->string);
        word.hyouki_len = (NJ_UINT8)message->string_size;
        word.f_hinsi = message->lword->f_hinsi;
        word.b_hinsi = message->lword->b_hinsi;

        /* 単語を登録 */
        ret = degiji_add_word(iwnn, &word, (NJ_UINT8 *)message->dicset->dic[message->dic_idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
        if (ret < 0) {
            ret = SET_GIJI_ERR(ret);
        }
        return ret;
    case NJG_OP_DEL_WORD:
        /* 単語を削除 */
        ret = degiji_delete_word(iwnn, &(message->word->stem.loc));
        if (ret < 0) {
            ret = SET_GIJI_ERR(ret);
        }
        return ret;
    case NJG_OP_GET_ADDITIONAL:
    case NJG_OP_LEARN:
    case NJG_OP_UNDO_LEARN:
        return 0;
    default:
        break;
    }
    return -1; /* エラー */
}

/**
 * 検索すべきインデックスの先頭アドレスを返す
 *
 * @param[in]         handle : 辞書アドレス
 * @param[in] search_pattern : 検索方法
 *
 * @retval              NULL   許されていない検索
 * @retval             !NULL   インデックスの先頭アドレス
 */
static NJ_UINT8 *degiji_get_search_index_address(NJ_DIC_HANDLE handle, NJ_UINT8 search_pattern) {

    if (search_pattern != NJ_CUR_OP_REV) {
        /* 逆引き検索以外なら読み順インデックス */
        return LEARN_INDEX_TOP_ADDR(handle);
    } else {
        /* 逆引き検索のときは候補順インデックス */
        return LEARN_INDEX_TOP_ADDR2(handle);
    }
}

/**
 * 電断を考慮して2byte書き込む
 *
 * @param[in]  ptr : 格納アドレス
 * @param[in] data : 格納データ
 *
 * @return           なし
 */
static void degiji_write_uint16_data(NJ_UINT8* ptr, NJ_UINT16 data) {
    NJ_UINT16 from;
    NJ_UINT8  *tmp;

    tmp = (NJ_UINT8*)&from;
    tmp[0] = (NJ_UINT8)(data >> 8);
    tmp[1] = (NJ_UINT8)(data & 0x00ff);
    COPY_UINT16(ptr, &from);
    return;
}

/**
 * 空き領域を探す
 *
 * @param[in]  handle : 辞書ハンドル
 * @param[out] que_id : キューID
 *
 * @retval          0   正常終了
 * @retval         -1   空きキューなし
 */
static NJ_INT16 degiji_search_empty_que(NJ_DIC_HANDLE handle, NJ_UINT16 *que_id) {
    NJ_UINT16 max;
    NJ_UINT16 id;

    max = GET_LEARN_MAX_WORD_COUNT(handle);             /* 最大単語登録数 */

    for (id = 0; id < max; id++) {
        if (GET_TYPE_FROM_DATA(POS_TO_ADDRESS(handle, id)) == QUE_TYPE_EMPTY) {
            /* 空きキュー */
            *que_id = id;
            return 0;
        }
    }
    /* 空きキューなし   */
    return -1;
}

/**
 * 単語検索
 *
 * @param[in]      iwnn : 解析情報クラス
 * @param[in]       con : 検索条件
 * @param[out]  loctset : 検索位置
 * @param[in]  comp_flg : 完全一致検索: 一致した候補に対するつながり検索の有無(1:有り、1以外:無し)<br>
 *                        繋がり検索: 複数キュー繋がり検索の有無（1:有り、1以外:なし）
 * @retval            0   検索候補なし
 * @retval            1   検索候補あり
 * @retval           <0   エラー
 */
static NJ_INT16 degiji_search_word(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *con,
                                   NJ_SEARCH_LOCATION_SET *loctset,
                                   NJ_UINT8 comp_flg) {

    NJ_UINT16    word_count;
    NJ_DIC_INFO *pdicinfo;
    NJ_UINT16    hIdx;
    NJ_INT16     ret;

    word_count = GET_LEARN_WORD_COUNT((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
    if (word_count == 0) {
        /* 条件に合う登録単語がない     */
        loctset->loct.status = NJ_ST_SEARCH_END_EXT;
        return 0;
    }

    /* 検索方法 */
    switch (con->operation) {
    case NJ_CUR_OP_COMP:
        if (con->mode != NJ_CUR_MODE_FREQ) {
            /* 完全一致検索のときは頻度順のみ指定可能 */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            break;
        }
        /* 完全一致 */
        /* 完全一致の場合は頻度順であってもインデックス順に検索できる   */
        return degiji_get_cand_by_sequential(iwnn, con, loctset, con->operation, comp_flg);

    case NJ_CUR_OP_FORE:
        /* 前方一致 */
        if (con->mode == NJ_CUR_MODE_REGIST) {
            /* 登録順 */
            if (con->ylen > 0) {
                /* 読み長0以外では行わない */
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;
            }
            return degiji_get_cand_by_evaluate(iwnn, con, loctset, con->operation);
        } else if (con->mode == NJ_CUR_MODE_YOMI) {
            /* 読み順のとき */
            return degiji_get_cand_by_sequential(iwnn, con, loctset, con->operation, 0);
        } else {
            /* 頻度順のとき */
            /* 辞書ハンドル位置を特定する。 */
            pdicinfo = con->ds->dic;
            for (hIdx = 0; (hIdx < NJ_MAX_DIC) && (pdicinfo->ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]); hIdx++) {
                pdicinfo++;
            }

            if (hIdx == NJ_MAX_DIC) {
                /* 辞書が存在しない為、検索終了の属性をカーソルに設定する */
                loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
                return 0; /*NCH_FB*/
            }

            /* キャッシュ領域がないか、または空文字列指定時 */
            /* キャッシュ検索不要モード時                   */
            if ((con->ds->dic[hIdx].srhCache == NULL) || (con->ylen == 0) ||
                !(con->ds->mode & 0x0001)) {
                return GET_CANDIDATE_BY_EVAL(iwnn, con, loctset, con->operation);
            } else {
                ret = GET_CANDIDATE_BY_EVAL2(iwnn, con, loctset, con->operation, hIdx);
                if (ret == NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_NOT_ENOUGH)) {
                    /* キャッシュ溢れが発生した場合は、もう一度同じ関数を呼ぶ */
                    NJ_SET_CACHEOVER_TO_SCACHE(con->ds->dic[hIdx].srhCache);
                    ret = GET_CANDIDATE_BY_EVAL2(iwnn, con, loctset, con->operation, hIdx);
                }
                return ret;
            }
        }

    case NJ_CUR_OP_REV:
        /* 逆引き検索 */
        if (con->mode != NJ_CUR_MODE_FREQ) {
            /* 逆引検索のときは頻度順のみ指定可能 */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            break;
        }
        return degiji_get_cand_by_evaluate(iwnn, con, loctset, con->operation);

    default:
        loctset->loct.status = NJ_ST_SEARCH_END_EXT;
    }

    return 0;
}

/**
 * 学習領域データから、指定されたキューの語彙種別と接続情報を取得する
 *
 * @attention 空きキュー（QUE_TYPE_EMPTY）は正常扱い
 *
 * @param[out]   iwnn : 解析情報クラス
 * @param[in]  handle : 辞書ハンドル
 * @param[in]  que_id : キューID
 *
 * @retval       NULL   取得できなかった
 * @retval      !NULL   キュー構造体
 */
static NJ_WQUE *degiji_get_que_type_and_next(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                                             NJ_UINT16 que_id) {
    NJ_UINT8 *ptr;
    NJ_WQUE *que = &(iwnn->que_tmp);

    if (que_id >= GET_LEARN_MAX_WORD_COUNT(handle)) {
        return NULL; /*NCH_FB*/
    }

    ptr = POS_TO_ADDRESS(handle, que_id);

    que->type = GET_TYPE_FROM_DATA(ptr);
    que->next_flag  = GET_FFLG_FROM_DATA(ptr);

    switch (que->type) {
    case QUE_TYPE_EMPTY:
    case QUE_TYPE_JIRI:
    case QUE_TYPE_FZK:
        return que;
    default:
        break;
    }
    return NULL; /*NCH_FB*/
}

/**
 * 学習領域データから、指定されたキューの読み文字列長と候補文字列長を取得する
 *
 * @attention 空きキュー（QUE_TYPE_EMPTY）はエラー
 *
 * @param[out]   iwnn : 解析情報クラス
 * @param[in]  handle : 辞書ハンドル
 * @param[in]  que_id : キューID
 *
 * @retval        NULL   取得できなかった
 * @retval       !NULL   キュー構造体
 */
static NJ_WQUE *degiji_get_que_yomiLen_and_hyoukiLen(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                                                     NJ_UINT16 que_id) {
    NJ_UINT8 *ptr;
    NJ_WQUE *que = &(iwnn->que_tmp);

    if (que_id >= GET_LEARN_MAX_WORD_COUNT(handle)) {
        return NULL; /*NCH_FB*/
    }

    ptr = POS_TO_ADDRESS(handle, que_id);

    que->type        = GET_TYPE_FROM_DATA(ptr);
    que->yomi_byte   = GET_YSIZE_FROM_DATA(ptr);
    que->yomi_len    = (NJ_UINT8)(que->yomi_byte / sizeof(NJ_CHAR));
    que->hyouki_byte = GET_KSIZE_FROM_DATA(ptr);
    que->hyouki_len  = (NJ_UINT8)(que->hyouki_byte / sizeof(NJ_CHAR));

    switch (que->type) {
    case QUE_TYPE_JIRI:
    case QUE_TYPE_FZK:
        return que;
    default:
        break;
    }
    return NULL;
}

/**
 * 学習領域データから、指定されたキューの前品詞と後品詞を取得する
 *
 * @attention 空きキュー（QUE_TYPE_EMPTY）はエラー
 *
 * @param[out]   iwnn : 解析情報クラス
 * @param[in]  handle : 辞書ハンドル
 * @param[in]  que_id : キューID
 *
 * @retval       NULL   取得できなかった
 * @retval      !NULL   キュー構造体
 */
static NJ_WQUE *degiji_get_que_allHinsi(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                                        NJ_UINT16 que_id) {
    NJ_UINT8 *ptr;
    NJ_WQUE *que = &(iwnn->que_tmp);

    if (que_id >= GET_LEARN_MAX_WORD_COUNT(handle)) {
        return NULL; /*NCH_FB*/
    }

    ptr = POS_TO_ADDRESS(handle, que_id);

    que->type      = GET_TYPE_FROM_DATA(ptr);
    que->mae_hinsi = GET_FPOS_FROM_DATA(ptr);
    que->ato_hinsi = GET_BPOS_FROM_DATA(ptr);

    switch (que->type) {
    case QUE_TYPE_JIRI:
    case QUE_TYPE_FZK:
        return que;
    default:
        break;
    }
    return NULL; /*NCH_FB*/
}

/**
 * 学習領域データから、指定されたキューを取得する
 *
 * @attention 空きキュー（QUE_TYPE_EMPTY）はエラー
 *
 * @param[out]   iwnn : 解析情報クラス
 * @param[in]  handle : 辞書ハンドル
 * @param[in]  que_id : キューID
 *
 * @retval       NULL   取得できなかった
 * @retval      !NULL   キュー構造体
 */
static NJ_WQUE *degiji_get_que(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id) {
    NJ_UINT8 *ptr;
    NJ_WQUE *que = &(iwnn->que_tmp);

    if (que_id >= GET_LEARN_MAX_WORD_COUNT(handle)) {
        return NULL; /*NCH_FB*/
    }

    ptr = POS_TO_ADDRESS(handle, que_id);

    que->entry      = que_id;
    que->type       = GET_TYPE_FROM_DATA(ptr);
    que->mae_hinsi  = GET_FPOS_FROM_DATA(ptr);
    que->ato_hinsi  = GET_BPOS_FROM_DATA(ptr);
    que->yomi_byte  = GET_YSIZE_FROM_DATA(ptr);
    que->yomi_len   = (NJ_UINT8)(que->yomi_byte / sizeof(NJ_CHAR));
    que->hyouki_byte= GET_KSIZE_FROM_DATA(ptr);
    que->hyouki_len = (NJ_UINT8)(que->hyouki_byte / sizeof(NJ_CHAR));
    que->next_flag  = GET_FFLG_FROM_DATA(ptr);

    switch (que->type) {
    case QUE_TYPE_JIRI:
    case QUE_TYPE_FZK:
        return que;
    default:
        break;
    }
    return NULL; /*NCH_FB*/
}

/**
 * 指定されたキューに接続するキューがあるか調べる
 *
 * @param[in]   iwnn : 解析情報クラス
 * @param[in] handle : 辞書ハンドル
 * @param[in] que_id : キューID
 *
 * @retval         0   接続しない
 * @retval         1   接続する
 * @retval        <0   エラー
 */
static NJ_INT16 degiji_is_continued(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id) {
    NJ_WQUE *que;
    NJ_UINT16 i;
    NJ_UINT16 max, end;

    max = GET_LEARN_MAX_WORD_COUNT(handle);             /* 最大登録数 */
    end = GET_LEARN_NEXT_WORD_POS(handle);

    for (i = 0; i < max; i++) {
        que_id++;
        if (que_id >= GET_LEARN_MAX_WORD_COUNT(handle)) {
            /* 最大値を超えたら先頭に */
            que_id = 0;
        }

        /* 一番古い位置にきた */
        if (que_id == end) {
            /* 次情報がなかった＝接続しない */
            return 0;
        }

        que = degiji_get_que_type_and_next(iwnn, handle, que_id);
        if (que == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_IS_CONTINUED, NJ_ERR_DIC_BROKEN);
        }
        if (que->type != QUE_TYPE_EMPTY) {
            /* キューの開始だった */
            if (que->next_flag != 0) {
                /* 前接続情報が立っていた */
                return 1;
            } else {
                /* 前接続情報が立ってなかった */
                return 0;
            }
        }
    }

    /* 何も登録されていない場合などはここに来る */
    return 0; /*NCH_FB*/
}

/**
 * 指定されたキューに接続するキューがいくつあるか調べる
 *
 *    Parameter    :
 * @param[in]   iwnn : 解析情報クラス
 * @param[in] handle : 辞書ハンドル
 * @param[in] que_id : キューID
 *
 * @retval       >=0   接続数
 * @retval        <0   エラー
 */
static NJ_INT16 degiji_continue_cnt(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id) {
    NJ_WQUE *que;
    NJ_UINT16 i;
    NJ_UINT16 max, end;
    NJ_INT16 cnt = 0;

    max = GET_LEARN_MAX_WORD_COUNT(handle);             /* 最大登録語数 */
    end = GET_LEARN_NEXT_WORD_POS(handle);

    for (i = 0; i < max; i++) {
        que_id++;
        if (que_id >= max) {
            /* 最大値を超えたら先頭に */
            que_id = 0;
        }

        /* 一番古い位置にきた */
        if (que_id == end) {
            /* 次情報がなかった＝接続しない */
            return cnt;
        }

        que = degiji_get_que_type_and_next(iwnn, handle, que_id);
        if (que == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_CONTINUE_CNT, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
        }
        if (que->type != QUE_TYPE_EMPTY) {
            /* キューの開始だった */
            if (que->next_flag != 0) {
                /* 前接続情報が立っていた */
                cnt++;
                /* 最大接続数チェック、                 */
                /* つながり検索なので最大接続数-1以下   */
                if (cnt >= (NJD_MAX_CONNECT_CNT - 1)) {
                    return cnt;
                }
            } else {
                /* 前接続情報が立ってなかった */
                return cnt;
            }
        }
    }

    /* 何も登録されていない場合などはここに来る */
    return 0; /*NCH_FB*/
}

/**
 * 次の単語のキュー番号を返す。
 *
 * @attention 一回転する可能性もあるので注意。
 * @attention 学習領域に何も登録されていない場合は0を返すため、キューIDと区別がつかない。要注意。
 *
 * @param[in] handle : 辞書ハンドル
 * @param[in] que_id : キュー番号
 *
 * @return 次のキュー番号 (学習領域に何も登録されていない場合は0を返す)
 */
static NJ_UINT16 degiji_search_next_que(NJ_DIC_HANDLE handle, NJ_UINT16 que_id) {
    NJ_UINT16 max;
    NJ_UINT16 i;

    max = GET_LEARN_MAX_WORD_COUNT(handle);             /* 最大登録語数 */

    for (i = 0; i < max; i++) {
        que_id++;
        if (que_id >= max) {
            /* 最大値を超えたら先頭に */
            que_id = 0;
        }

        if (GET_TYPE_FROM_DATA(POS_TO_ADDRESS(handle, que_id)) != QUE_TYPE_EMPTY) {
            /* キューの開始だった */
            return que_id;
        }
    }

    /* 何も登録されていない場合などはここに来る */
    return 0; /*NCH_FB*/
}

/**
 * 前の単語のキュー番号を返す。<br>
 *                   一回転する可能性もあるので注意。
 *
 * @param[in] handle : 辞書ハンドル
 * @param[in] que_id : キュー番号
 *
 * @retval         0    学習領域に何も登録されていない
 * @retval        !0    前のキュー番号
 */
static NJ_UINT16 degiji_search_prev_que(NJ_DIC_HANDLE handle, NJ_UINT16 que_id) {
    NJ_UINT16 max;
    NJ_UINT16 i;

    max = GET_LEARN_MAX_WORD_COUNT(handle);             /* 最大登録語数 */

    for (i = 0; i < max; i++) {
        if (que_id == 0) {
            que_id = max - 1;
        } else {
            que_id--;
        }

        if (GET_TYPE_FROM_DATA(POS_TO_ADDRESS(handle, que_id)) != QUE_TYPE_EMPTY) {
            return que_id;
        }
    }

    /* 何も登録されていない場合などはここに来る */
    return 0; /*NCH_FB*/
}

/**
 * 指定したキューをindexから削除する
 *
 * @param[in]   iwnn : 解析情報クラス
 * @param[in] handle : 辞書ハンドル
 * @param[in] que_id : 削除するque_id
 *
 * @retval        <0   エラー
 * @retval         0   指定したキューで削除対象するものはなかった
 * @retval        >0   指定したキューを削除した
 */
static NJ_INT16 degiji_delete_index(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                                    NJ_UINT16 que_id) {
    NJ_UINT16 max, que_id2;
    NJ_UINT16 i;
    NJ_UINT8  *ptr;
    NJ_WQUE *que;
    NJ_INT16 removed = 0;

    /**
     * 規定外の que_id を削除しようとしてないかをチェックする。
     */
    if (que_id >= GET_LEARN_MAX_WORD_COUNT(handle)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_DELETE_INDEX, NJ_ERR_DIC_BROKEN);
    }

    /* 学習領域に登録されている語彙数を取得する */
    /* （削除なので登録語数）                   */
    max = GET_LEARN_WORD_COUNT(handle);

    /* 一つも入ってないときは正常で返す */
    if (max == 0) {
        return 0;
    }

    /* 念のため最大登録数チェック */
    if (max > GET_LEARN_MAX_WORD_COUNT(handle)) {
        return NJ_SET_ERR_VAL(NJ_FUNC_DELETE_INDEX, NJ_ERR_DIC_BROKEN);
    }

    /* ---------------------- */
    /*   Lstat-15 (Ustat-14)  */
    /* ---------------------- */

    if (NJ_GET_DIC_TYPE(handle) == NJ_DIC_TYPE_LEARN) {
        /* ----------------------------------- */
        /*   つながり情報削除処理 Ldel-01      */
        /*   書き込み途中は Lstat-16           */
        /* ----------------------------------- */
        if (max > 1) {
            que_id2 = degiji_search_next_que(handle, que_id);
            que = degiji_get_que_type_and_next(iwnn, handle, que_id2);
            if (que == NULL) {
                return NJ_SET_ERR_VAL(NJ_FUNC_DELETE_INDEX, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
            }
            if (que->next_flag != 0) {
                /* つながり情報を削除する       */
                ptr = POS_TO_ADDRESS(handle, que_id2);
                RESET_FFLG(ptr);
            }
        }

        /* ------------ */
        /*   Lstat-17   */
        /* ------------ */
    }

    /* インデックス領域の先頭アドレスを取得する         */
    /* （最初は通常のインデックス）                     */
    ptr = LEARN_INDEX_TOP_ADDR(handle);

    /* index内で該当のキューを探す */
    for (i = 0; i < max; i++) {
        que_id2 = GET_UINT16(ptr);
        ptr += NJ_INDEX_SIZE;

        if (que_id2 == que_id) {
            /* --------------------------------- */
            /*   復旧位置格納 Ldel-02 (Udel-01)  */
            /* --------------------------------- */
            /* 電断用flg下位2byteに削除対象キューIDを待避       */
            degiji_write_uint16_data(handle + POS_WRITE_FLG + 2, que_id);

            /* ---------------------- */
            /*   Lstat-18 (Ustat-15)  */
            /* ---------------------- */

            /* --------------------------------- */
            /*   登録語数待避 Ldel-03 (Udel-02)  */
            /* --------------------------------- */
            /* 電断用flg上位2byteに登録語数 - 1を待避   */
            degiji_write_uint16_data(handle + POS_WRITE_FLG, (NJ_UINT16)(max - 1));

            /* ---------------------- */
            /*   Lstat-19 (Ustat-16)  */
            /* ---------------------- */

            /* ------------------------------------------- */
            /*   読み順インデックス削除 Ldel-04 (Udel-03)  */
            /*   書き込み途中は Lstat-20 (Ustat-17)        */
            /* ------------------------------------------- */
            /* 読み順インデックスシフト処理     */
            degiji_shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(max - i - 1));

            /* ---------------------- */
            /*   Lstat-21 (Ustat-18)  */
            /* ---------------------- */

            removed++;
            break;
        }
    }

    /* インデックス領域の先頭アドレスを取得する         */
    /* （表記文字列のインデックス）                     */
    ptr = LEARN_INDEX_TOP_ADDR2(handle);

    /* index内で該当のキューを探す */
    for (i = 0; i < max; i++) {
        que_id2 = GET_UINT16(ptr);
        ptr += NJ_INDEX_SIZE;

        if (que_id2 == que_id) {

            /* ----------------------------------------------- */
            /*   表記文字列インデックス削除 Ldel-05 (Udel-04)  */
            /*   書き込み途中は Lstat-22 (Ustat-19)            */
            /* ----------------------------------------------- */
            /* 表記文字列インデックスシフト処理 */
            degiji_shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(max - i - 1));

            /* ---------------------- */
            /*   Lstat-23 (Ustat-20)  */
            /* ---------------------- */


            /* --------------------------------------------- */
            /*   キューを未使用状態にする Ldel-06 (Udel-05)  */
            /* --------------------------------------------- */
            /* 削除対象キューを未使用にする     */
            ptr = POS_TO_ADDRESS(handle, que_id);
            *ptr = QUE_TYPE_EMPTY;

            /* ---------------------- */
            /*   Lstat-24 (Ustat-21)  */
            /* ---------------------- */

            /* -------------------------------------- */
            /*  登録語数を更新する Ldel-07 (Udel-06)  */
            /* -------------------------------------- */
            /* 登録語数を1減らす（登録語数に電断用flgをコピー） */
            COPY_UINT16(handle + POS_LEARN_WORD + 2,
                        handle + POS_WRITE_FLG);

            /* ---------------------- */
            /*   Lstat-25 (Ustat-22)  */
            /* ---------------------- */

            removed++;
            break;
        }
    }

    if (removed == 1) {
        /* 読み／表記インデックスのいずれか片方にしか削除対象の
         * キューIDが存在しなかった場合、辞書が壊れている。 */
        return NJ_SET_ERR_VAL(NJ_FUNC_DELETE_INDEX, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
    }
    return removed;
}

/**
 * よみ、表記を固定長キューで分割されたデータに格納する
 *
 * @param[in]    iwnn : 解析情報クラス
 * @param[in]  handle : 辞書ハンドル
 * @param[out]    dst : コピー先アドレス
 * @param[in]     src : コピー元アドレス
 * @param[in]    size : 文字列のバイト長
 *
 * @return              書き込んだ次のアドレス
 *
 *:::DOC_END
 */
static NJ_UINT8 *degiji_write_string(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 *dst,
                                     NJ_UINT8 *src, NJ_UINT8 size) {
    /*
     *                       + dstアドレス
     *   +--+----------------+------------------------+
     *   |1 | 固定データ(他) |  コピー元文字列        |
     *   +--+-----------------------------------------+
     *   |0 | コピー元文字列(続き)              |     |
     *   +--+-----------------------------------+-----+
     *   <--------------- QUE_SIZE ------------------->
     */

    NJ_UINT8 copy_size;
    NJ_INT16 ret;
    NJ_UINT8 *bottom_addr;

    if (size == 0) {
        /* コピー先アドレスをそのまま返す */
        return dst;
    }

    /* dstがキューの先頭なら QUE_TYPE_NEXT を設定する */
    if (((dst - LEARN_DATA_TOP_ADDR(handle)) % QUE_SIZE(handle)) == 0) {
        /* キューアドレスチェック */
        bottom_addr = LEARN_DATA_TOP_ADDR(handle);
        bottom_addr += QUE_SIZE(handle) * GET_LEARN_MAX_WORD_COUNT(handle) - 1;
        if (dst >= bottom_addr) {
            dst = LEARN_DATA_TOP_ADDR(handle); /*NCH_FB*/
        }

        if (*dst != QUE_TYPE_EMPTY) {
            ret = degiji_delete_index(iwnn, handle, (NJ_UINT16)ADDRESS_TO_POS(handle, dst)); /*NCH_FB*/
            if (ret < 0) { /*NCH_FB*/
                return NULL; /*NCH_FB*/
            }
        }
        *dst++ = QUE_TYPE_NEXT;
    }

    /* 最初のキューのコピー可能範囲 */
    copy_size = (NJ_UINT8)QUE_SIZE(handle);
    copy_size -= (NJ_UINT8)((dst - LEARN_DATA_TOP_ADDR(handle)) % QUE_SIZE(handle));
    if (copy_size > size) {
        /* 最初のキューのみコピー */
        copy_size = size;
    }
    nj_memcpy(dst, src, copy_size);
    dst += copy_size;
    src += copy_size;

    while (size -= copy_size) {

        /* 次キュー検索 */
        bottom_addr = LEARN_DATA_TOP_ADDR(handle);
        bottom_addr += QUE_SIZE(handle) * GET_LEARN_MAX_WORD_COUNT(handle) - 1;
        if (dst >= bottom_addr) {
            dst = LEARN_DATA_TOP_ADDR(handle);
        }

        if (*dst != QUE_TYPE_EMPTY) {
            ret = degiji_delete_index(iwnn, handle, (NJ_UINT16)ADDRESS_TO_POS(handle, dst));
            if (ret < 0) {
                return NULL; /*NCH_FB*/
            }
        }

        /* キューの種別を更新する */
        *dst++ = QUE_TYPE_NEXT;

        if (size <= (QUE_SIZE(handle) - 1)) {
            /* 次のキューで終了 */
            copy_size = size;
        } else {
            copy_size = (NJ_UINT8)(QUE_SIZE(handle) - 1);
        }
        nj_memcpy(dst, src, copy_size);
        dst += copy_size;
        src += copy_size;
    }

    return dst;
}

/**
 * 読みと候補文字列の完全一致検索
 *
 * 複数キューを対象にするか否かをフラグで指定する
 *
 * @param[in]      iwnn : 解析情報クラス
 * @param[in]    handle : 辞書ハンドル
 * @param[in]    que_id : キューID
 * @param[in]      yomi : 読み
 * @param[in]  yomi_len : 検索対象の読み文字配列長
 * @param[in]    hyouki : 表記
 * @param[in] multi_flg : 複数キュー対象フラグ
 *                           （0:対象外、0以外:対象とする）
 *
 * @retval           >0   一致したキューの連続数
 * @retval            0   一致しなかった
 * @retval           <0   エラー
 */
static NJ_INT16 degiji_que_strcmp_complete_with_hyouki(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                                                       NJ_UINT16 que_id, NJ_CHAR *yomi, NJ_UINT16 yomi_len, NJ_CHAR *hyouki,
                                                       NJ_UINT8 multi_flg) {
    NJ_CHAR *str;
    NJ_INT16 ret;
    NJ_UINT8 slen;
    NJ_UINT16 hyouki_len;
    NJ_UINT16 que_yomilen, que_hyoukilen;
    NJ_INT16 que_count = 1;
    NJ_INT16 cnt = 0;

    /* 比較する候補の長さ       */
    hyouki_len = nj_strlen(hyouki);

    if (multi_flg == 0) {
        /* 複数キューを対象としない     */
        cnt = 1;
    } else {
        /* 複数キューを対象とする       */
        /* 最大で登録語数分だけ回す     */
        cnt = GET_LEARN_WORD_COUNT(handle);
    }

    while (cnt--) {
        str = degiji_get_string(iwnn, handle, que_id, &slen);
        if (str == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_QUE_STRCMP_COMPLETE_WITH_HYOUKI, /*NCH_FB*/
                                  NJ_ERR_DIC_BROKEN);
        }
        que_yomilen = slen;
        /* 読みの一致チェック */
        ret = nj_strncmp(yomi, str, que_yomilen);
        if (ret != 0) {
            /* 不一致           */
            return 0;
        }

        str = degiji_get_hyouki(iwnn, handle, que_id, &slen);
        if (str == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_QUE_STRCMP_COMPLETE_WITH_HYOUKI, /*NCH_FB*/
                                  NJ_ERR_DIC_BROKEN);
        }
        que_hyoukilen = slen;
        /* 候補の一致チェック   */
        ret = nj_strncmp(hyouki, str, que_hyoukilen);
        if (ret != 0) {
            /* 不一致           */
            return 0;
        }
        /* 読みと候補が完全一致 */
        if ((yomi_len == que_yomilen) &&
            (hyouki_len == que_hyoukilen)) {
            /* 接続数を返す     */
            return que_count;
        }
        /* キューの読み長または候補長の方が長い */
        if ((que_yomilen > yomi_len) ||
            (que_hyoukilen > hyouki_len)) {
            /* 不一致           */
            return 0; /*NCH_FB*/
        }
        /* 次のキューにつながるかチェック       */
        ret = degiji_is_continued(iwnn, handle, que_id);
        if (ret <= 0) {
            /* つながっていない、またはエラー   */
            return ret;
        }
        /* 最大接続数チェック、                 */
        /* つながり検索なので最大接続数-1以下   */
        if (que_count >= (NJD_MAX_CONNECT_CNT - 1)) {
            /* 不一致   */
            return 0;
        }
        /* 次に比較する部分までポインタを進める */
        yomi_len -= que_yomilen;
        yomi     += que_yomilen;

        hyouki_len -= que_hyoukilen;
        hyouki     += que_hyoukilen;

        /* 次のキューを探す     */
        que_id = degiji_search_next_que(handle, que_id);
        que_count++;
    }
    return 0; /*NCH_FB*/
}

/**
 * 指定されたキューに接続するキューを対象として包含で検索する
 *
 * @param[in]     iwnn : 解析情報クラス
 * @param[in]   handle : 辞書ハンドル
 * @param[in]   que_id : 一致しているキューID
 * @param[in]     yomi : 比較する読み文字列
 *
 * @retval        >0   包含関係にあるキュー数<br>
 *                    （指定キューは既にチェック済みなので１以上）
 * @retval        <0   エラー
 */
static NJ_INT16 degiji_que_strcmp_include(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                                          NJ_UINT16 que_id, NJ_CHAR *yomi) {
    NJ_CHAR *str;
    NJ_UINT16 que_len;
    NJ_UINT16 yomi_len;
    NJ_INT16  ret;
    NJ_INT16  que_count = 1; /* 連続キューカウント数 */
    NJ_UINT16 i = 0;
    NJ_UINT8  slen;
    NJ_UINT8  *add_ptr;
    NJ_UINT16 addinfo_len;

    yomi_len = nj_strlen(yomi);
    if (yomi_len == 0) {
        return que_count;
    }
    /* かな漢字変換専用ロジック。NJ_CUR_OP_REVは来ない  */
    i = GET_LEARN_WORD_COUNT(handle);

    /* 付加情報なし */
    add_ptr = NULL;
    addinfo_len = 0;

    while (--i) {        /* 保険のため、現在登録されているキュー数以下のループとする */

        /* 次のキューがつながるか */
        ret = degiji_is_continued(iwnn, handle, que_id);
        if (ret < 0) {
            /* エラー   */
            return ret;
        } else if (ret == 0) {
            /* つながらない     */
            return que_count;
        }

        /* 次のキューを探す */
        que_id = degiji_search_next_que(handle, que_id);

        if (add_ptr != NULL) {
            add_ptr = POS_TO_ADD_ADDRESS(handle, que_id);
            if (NJ_MAX_ADDITIONAL_LEN >= (addinfo_len + (NJ_UINT16)(*add_ptr / sizeof(NJ_CHAR)))) {
                addinfo_len += (NJ_UINT16)(*add_ptr / sizeof(NJ_CHAR));
            } else {
                /* 付加情報が溢れる場合は、納まる分までを文節とする */
                return que_count;
            }
        }

        str = degiji_get_string(iwnn, handle, que_id, &slen);
        if (str == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_QUE_STRCMP_INCLUDE, NJ_ERR_DIC_BROKEN);
        }
        que_len = slen;

        /* queの文字配列長が大きい : 包含一致しない */
        if (que_len > yomi_len) {
            return que_count;
        }

        /* queの文字配列数分だけstrcmpする */
        ret = nj_strncmp(yomi, str, que_len);
        if (ret != 0) {
            /* 不一致   */
            return que_count;
        }

        /* 読み長が一致 : 完全一致している */
        if (que_len == yomi_len) {
            return (que_count + 1);
        }

        que_count++;
        if (que_count >= NJD_MAX_CONNECT_CNT) {
            /* 最大接続数を超えたら不一致       */
            return que_count;
        }

        /* チェック済み部分をskipする */
        yomi_len -= que_len;
        yomi     += que_len;
    }

    return que_count;
}

/**
 * 学習領域データから、指定されたキューの読みを取得する
 *
 * @param[out]   iwnn : 解析情報クラス
 * @param[in]  handle : 辞書ハンドル
 * @param[in]  que_id : キューID
 * @param[out]   slen : 取得した文字配列長
 *
 * @retval       NULL   エラー
 * @retval      !NULL   読み文字列
 */
static NJ_CHAR *degiji_get_string(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                                  NJ_UINT16 que_id, NJ_UINT8 *slen) {
    NJ_UINT8 *src, *dst;
    NJ_UINT8 copy_size, size;
    NJ_UINT8 i;
    NJ_UINT8 *top_addr;
    NJ_UINT8 *bottom_addr;
    NJ_UINT16 que_size;

    src = POS_TO_ADDRESS(handle, que_id);
    switch (GET_TYPE_FROM_DATA(src)) {
    case QUE_TYPE_JIRI:
    case QUE_TYPE_FZK:
        size =  GET_YSIZE_FROM_DATA(src);
        *slen = (NJ_UINT8)(size / sizeof(NJ_CHAR));
        break;

    default:
        return NULL;
    }
    /* 読み長をチェックする     */
    if (*slen > NJ_MAX_EMOJI_LEN) {
        return NULL; /*NCH*/
    }

    /* 固定部分までポインタを進める     */
    src += LEARN_QUE_STRING_OFFSET;

    que_size = QUE_SIZE(handle);

    /* 最初のキューのコピー可能範囲 */
    copy_size = (NJ_UINT8)que_size - LEARN_QUE_STRING_OFFSET;
    dst = (NJ_UINT8*)&(iwnn->learn_string_tmp[0]);
    if (copy_size > size) {
        /* 最初のキューのみコピー */
        copy_size = size;
    }
    for (i = 0; i < copy_size; i++) {
        *dst++ = *src++;
    }

    /* 次キュー検索 */
    top_addr = LEARN_DATA_TOP_ADDR(handle);
    bottom_addr = top_addr;
    bottom_addr += que_size * GET_LEARN_MAX_WORD_COUNT(handle) - 1;

    while (size -= copy_size) {

        if (src >= bottom_addr) {
            src = top_addr;
        }

        /* キューの種別をチェックする */
        if (*src != QUE_TYPE_NEXT) {
            return NULL;        /* キューが破損している */ /*NCH_FB*/
        }

        src++;  /* 次キューの先頭(種別)をskipする */
        if (size < que_size) {
            /* 次のキューで終了 */
            copy_size = size;
        } else {
            copy_size = (NJ_UINT8)(que_size - 1);
        }
        for (i = 0; i < copy_size; i++) {
            *dst++ = *src++;
        }
    }
    iwnn->learn_string_tmp[*slen] = NJ_CHAR_NUL;

    return &(iwnn->learn_string_tmp[0]);
}

/**
 * 学習領域データから、指定されたキューの候補文字列を取得する
 *
 * @param[in]    iwnn : 解析情報クラス
 * @param[in]  handle : 辞書ハンドル
 * @param[in]  que_id : キューID
 * @param[out]   slen : 取得した文字配列長
 *
 * @retval       NULL   エラー
 * @retval      !NULL   読み文字列
 */
static NJ_CHAR *degiji_get_hyouki(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                                  NJ_UINT16 que_id, NJ_UINT8 *slen) {
    NJ_UINT8 *src, *dst;
    NJ_WQUE *que;
    NJ_UINT8 copy_size, size;
    NJ_UINT8 i;
    NJ_UINT8 *top_addr;
    NJ_UINT8 *bottom_addr;
    NJ_CHAR  *hira;
    NJ_UINT16 que_size;

    que = degiji_get_que_yomiLen_and_hyoukiLen(iwnn, handle, que_id);
    if (que == NULL) {
        return NULL;
    }
    /* 読み長、候補長をチェックする     */
    if (que->yomi_len > NJ_MAX_EMOJI_LEN) {
        return NULL; /*NCH*/
    }
    if (que->hyouki_len > NJ_MAX_EMOJI_KOUHO_LEN) {
        return NULL; /*NCH*/
    }

    src = POS_TO_ADDRESS(handle, que_id);
    /* 表記長が格納されていなければ無変換候補   */
    if (que->hyouki_len == 0) {
        hira = degiji_get_string(iwnn, handle, que_id, slen);
        if (hira == NULL) {
            return NULL; /*NCH*/
        }
        /* 無変換フラグONならカタカナ候補にする */
        if (GET_MFLG_FROM_DATA(src) != 0) {
            *slen = (NJ_UINT8)nje_convert_hira_to_kata(hira, &(iwnn->muhenkan_tmp[0]), *slen);
            return &(iwnn->muhenkan_tmp[0]);
        } else {
            return hira;
        }
    }
    /* 読み先頭までポインタを進める     */
    src += LEARN_QUE_STRING_OFFSET;

    que_size = QUE_SIZE(handle);

    /* 読み文字数を進める */
    size = que->yomi_byte;
    copy_size = (NJ_UINT8)que_size - LEARN_QUE_STRING_OFFSET;
    dst = (NJ_UINT8*)&(iwnn->learn_string_tmp[0]);
    if (copy_size > size) {
        /* 最初のキューのみコピー */
        copy_size = size;
    }

    /* 学習辞書の先頭アドレス退避 */
    top_addr = LEARN_DATA_TOP_ADDR(handle);
    bottom_addr = top_addr;
    bottom_addr += que_size * GET_LEARN_MAX_WORD_COUNT(handle) - 1;

    src += copy_size;
    while (size -= copy_size) {

        /* 次キュー検索 */
        if (src >= bottom_addr) {
            src = top_addr;
        }

        /* キューの種別をチェックする */
        if (*src != QUE_TYPE_NEXT) {
            return NULL;        /* キューが破損している */ /*NCH_FB*/
        }

        src++;  /* 次キューの先頭(種別)をskipする */
        if (size < que_size) {
            /* 次のキューで終了 */
            copy_size = size;
        } else {
            copy_size = (NJ_UINT8)(que_size - 1);
        }
        src += copy_size;
    }


    /* srcがキューの先頭なら キュー種別をチェックし、スキップする */
    if (((src - top_addr) % que_size) == 0) {

        if (src >= bottom_addr) {
            src = top_addr;
        }

        if (*src++ != QUE_TYPE_NEXT) {
            return NULL; /*NCH_FB*/
        }
    }

    size = que->hyouki_byte;

    /* 最初のキューのコピー可能範囲 */
    copy_size = (NJ_UINT8)(que_size);
    copy_size -= (NJ_UINT8)((src - top_addr) % que_size);
    if (copy_size > size) {
        /* 最初のキューのみコピー */
        copy_size = size;
    }
    for (i = 0; i < copy_size; i++) {
        *dst++ = *src++;
    }

    while (size -= copy_size) {

        /* 次キュー検索 */
        if (src >= bottom_addr) {
            src = top_addr;
        }

        /* キューの種別をチェックする */
        if (*src != QUE_TYPE_NEXT) {
            return NULL;        /* キューが破損している */
        }

        src++;  /* 次キューの先頭(種別)をskipする */
        if (size < que_size) {
            /* 次のキューで終了 */
            copy_size = size;
        } else {
            copy_size = (NJ_UINT8)(que_size - 1);
        }

        for (i = 0; i < copy_size; i++) {
            *dst++ = *src++;
        }
    }

    *slen = que->hyouki_len;
    iwnn->learn_string_tmp[*slen] = NJ_CHAR_NUL;

    return &(iwnn->learn_string_tmp[0]);
}

/**
 * 学習領域に追加する
 *
 * @param[in]     iwnn : 解析情報クラス
 * @param[in]   handle : 辞書ハンドル
 * @param[in]   que_id : キューID
 * @param[in]     word : 学習単語情報
 * @param[in]  connect : 接続情報
 * @param[in]     type : 自立語／付属語種別
 * @param[in]     undo : アンドゥフラグ
 * @param[in] muhenkan : 無変換候補 種別フラグ
 *
 * @retval           0   正常終了
 * @retval          <0   エラー
 */
static NJ_INT16 degiji_write_learn_data(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id,
                                        NJ_LEARN_WORD_INFO *word, NJ_UINT8 connect, NJ_UINT8 type,
                                        NJ_UINT8 undo, NJ_UINT8 muhenkan) {
    NJ_UINT8  *ptr, *index_ptr;
    NJ_UINT16 i, max;
    NJ_CHAR   *str;
    NJ_UINT16 que_id2;
    NJ_UINT8  slen;
    NJ_INT16  ret = 0;
    NJ_UINT16 top, bottom, mid;

    /* 登録語彙数                       */
    /* （追加なので登録語数で良い）     */
    max = GET_LEARN_WORD_COUNT(handle);
    /* 挿入可能かチェックする */
    if (max >= GET_LEARN_MAX_WORD_COUNT(handle)) {
        /* いっぱいいっぱいだった */
        return NJ_SET_ERR_VAL(NJ_FUNC_WRITE_LEARN_DATA, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
    }

    /* 読み順インデックス領域の先頭アドレスを取得する */
    index_ptr = LEARN_INDEX_TOP_ADDR(handle);

    /* ---------------------- */
    /*   Lstat-01 (Ustat-01)  */
    /* ---------------------- */

    /* --------------------------------------- */
    /*   品詞等書き込み処理 Ladd-01 (Uadd-01)  */
    /*   書き込み途中は Lstat-02 (Ustat-02)    */
    /* --------------------------------------- */
    ptr = POS_TO_ADDRESS(handle, que_id);
    /* 前品詞と読みバイト長、後品詞と候補バイト長を書き込む */
    SET_FPOS_AND_YSIZE(ptr, word->f_hinsi, word->yomi_len*sizeof(NJ_CHAR));
    SET_BPOS_AND_KSIZE(ptr, word->b_hinsi, word->hyouki_len*sizeof(NJ_CHAR));

    /* 語彙種別と語彙情報分進める       */
    ptr += LEARN_QUE_STRING_OFFSET;
    /* 読み、表記を書き出す     */
    ptr =
    degiji_write_string(iwnn, handle, ptr, (NJ_UINT8*)word->yomi, (NJ_UINT8)(word->yomi_len*sizeof(NJ_CHAR)));
    if (ptr == NULL) {
        /* write_string内部でdelete_indexに失敗 */
        return NJ_SET_ERR_VAL(NJ_FUNC_WRITE_LEARN_DATA, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
    }
    ptr = degiji_write_string(iwnn, handle, ptr, (NJ_UINT8*)word->hyouki, (NJ_UINT8)(word->hyouki_len*sizeof(NJ_CHAR)));
    if (ptr == NULL) {
        /* write_string内部でdelete_indexに失敗 */
        return NJ_SET_ERR_VAL(NJ_FUNC_WRITE_LEARN_DATA, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
    }


    /* ---------------------- */
    /*   Lstat-03 (Ustat-03)  */
    /* ---------------------- */

    /* ------------------------------ */
    /*   復旧位置格納処理（Ladd-02）  */
    /* ------------------------------ */
    /* 電断用flgの下位2byteに追加対象キューIDを待避する */
    degiji_write_uint16_data(handle + POS_WRITE_FLG + 2, que_id);

    /* ---------------------- */
    /*   Lstat-04 (Ustat-04)  */
    /* ---------------------- */

    /* ------------------------------------- */
    /*   登録語数待避処理 Ladd-04 (Uadd-03)  */
    /* ------------------------------------- */
    /* 削除が発生している可能性があるので登録語数を取得しなおす */
    /* （追加なので登録語数で良い）                             */
    max = GET_LEARN_WORD_COUNT(handle);

    /* 読み順インデックスの挿入位置を探す */
    /* 二分検索を行う                     */
    top = 0;
    bottom = max;
    while (top < bottom) {
        mid = top + ((bottom - top) / 2);

        que_id2 = GET_UINT16(index_ptr + (NJ_INDEX_SIZE * mid));
        str = degiji_get_string(iwnn, handle, que_id2, &slen);
        if (str == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_WRITE_LEARN_DATA, NJ_ERR_DIC_BROKEN);
        }
        ret = nj_strcmp(word->yomi, str);

        if (ret < 0) {
            bottom = mid;
        } else if (ret > 0) {
            top = mid + 1;
        } else {
            /* 単語登録時には候補も比較する     */
            str = degiji_get_hyouki(iwnn, handle, que_id2, &slen);
            if (str == NULL) {
                return NJ_SET_ERR_VAL(NJ_FUNC_WRITE_LEARN_DATA, NJ_ERR_DIC_BROKEN); /*NCH*/
            }
            ret = nj_strcmp(word->hyouki, str);
            if (ret <= 0) {
                bottom = mid;
            } else {
                top = mid + 1;
            }
        }
    }
    index_ptr += (NJ_INDEX_SIZE * bottom);
    i = bottom;

    /* 電断用flgの上位2byteに登録語数 + 1 を待避する    */
    degiji_write_uint16_data(handle + POS_WRITE_FLG, (NJ_UINT16)(max + 1));

    /* ---------------------- */
    /*   Lstat-06 (Ustat-05)  */
    /* ---------------------- */

    /* ------------------------------------------- */
    /*   キューを使用状態にする Ladd-05 (Uadd-04)  */
    /* ------------------------------------------- */
    /* 語彙情報格納、キューは使用状態になる     */
    ptr = POS_TO_ADDRESS(handle, que_id);
    SET_TYPE_ALLFLG(ptr,
                    (type == 0)? QUE_TYPE_JIRI: QUE_TYPE_FZK,
                    undo,
                    (connect == 1)? 1: 0,
                    muhenkan);          /* 無変換フラグ */

    /* ---------------------- */
    /*   Lstat-07 (Ustat-06)  */
    /* ---------------------- */

    if (i < max) {
        /* ------------------------------------- */
        /*   挿入位置空ける Ladd-06 (Uadd-05)    */
        /*   書き込み途中は Lstat-08 (Ustat-07)  */
        /* ------------------------------------- */
        degiji_shift_index(index_ptr, SHIFT_RIGHT, (NJ_UINT16)(max - i));
    }
    /* ---------------------- */
    /*   Lstat-09 (Ustat-08)  */
    /* ---------------------- */

    /* --------------------------------- */
    /*   キューID挿入 Ladd-07 (Uadd-06)  */
    /* --------------------------------- */
    degiji_write_uint16_data(index_ptr, que_id);

    /* ---------------------- */
    /*   Lstat-10 (Ustat-09)  */
    /* ---------------------- */

    /* 表記順インデックスの先頭アドレス         */
    index_ptr = LEARN_INDEX_TOP_ADDR2(handle);
    /* 表記順インデックスの挿入位置を探す */
    /* 二分検索を行う                     */
    top = 0;
    bottom = max;
    while (top < bottom) {
        mid = top + ((bottom - top) / 2);

        que_id2 = GET_UINT16(index_ptr + (NJ_INDEX_SIZE * mid));
        str = degiji_get_hyouki(iwnn, handle, que_id2, &slen);
        if (str == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_WRITE_LEARN_DATA, NJ_ERR_DIC_BROKEN);
        }
        ret = nj_strcmp(word->hyouki, str);
        if (ret <= 0) {
            bottom = mid;
        } else {
            top = mid + 1;
        }
    }
    index_ptr += (NJ_INDEX_SIZE * bottom);
    i = bottom;

    if (i < max) {
        /* ------------------------------------- */
        /*   挿入位置空ける Ladd-08 (Uadd-07)    */
        /*   書き込み途中は Lstat-11 (Ustat-10)  */
        /* ------------------------------------- */
        /* ---------------------- */
        /*   Lstat-11 (Ustat-10)  */
        /* ---------------------- */
        degiji_shift_index(index_ptr, SHIFT_RIGHT, (NJ_UINT16)(max - i));
    }
    /* ---------------------- */
    /*   Lstat-12 (Ustat-11)  */
    /* ---------------------- */

    /* --------------------------------- */
    /*   キューID挿入 Ladd-09 (Uadd-08)  */
    /* --------------------------------- */
    degiji_write_uint16_data(index_ptr, que_id);

    /* ---------------------- */
    /*   Lstat-13 (Ustat-12)  */
    /* ---------------------- */

    /* --------------------------------- */
    /*   登録語数更新 Ladd-10 (Uadd-09)  */
    /* --------------------------------- */
    /* 登録語数を1増やす（登録語数に電断用flg上位2byteをコピー） */
    COPY_UINT16(handle + POS_LEARN_WORD + 2,
                handle + POS_WRITE_FLG);

    /* ---------------------- */
    /*   Lstat-14 (Ustat-13)  */
    /* ---------------------- */

    return 0;
}

/**
 * 指定されたインデックスデータをshift_count分、direction方向にシフトする
 *
 * @attention ptrを内部でNJ_UINT16*にキャストしている。そのため、ptrは偶数番地を指す必要がある。
 *
 * @param[in]         ptr : 移動開始位置
 * @param[in]   direction : 方向<br>
 *                          SHIFT_LEFT  : 左<br>
 *                          SHIFT_RIGHT : 右
 * @param[in] shift_count : 移動するデータの個数
 *
 * @return                  なし
 */
static void degiji_shift_index(NJ_UINT8 *ptr,
                               NJ_UINT8 direction, NJ_UINT16 shift_count) {
    NJ_UINT16 i;
    NJ_UINT16 *src, *dst;

    switch (direction) {
    case SHIFT_LEFT:
        src = (NJ_UINT16 *)ptr;
        dst = src - 1;
        for (i = 0; i < shift_count; i++) {
            *(dst++) = *(src++);
        }
        break;

    case SHIFT_RIGHT:
        src = (NJ_UINT16 *)ptr + shift_count - 1;
        dst = src + 1;
        for (i = 0; i < shift_count; i++) {
            *(dst--) = *(src--);
        }
        break;

    default:
        /* directionが異常だが、起こらないハズ */
        break;
    }

    return;
}

/**
 * デコメ絵文字辞書に登録する
 *
 * @param[in]      iwnn : 解析情報クラス
 * @param[in]      word : 学習単語情報
 * @param[in] demojidic : 絵文字辞書ハンドル
 *
 * @retval         <0   エラー
 * @retval        >=0   正常終了
 */
static NJ_INT16 degiji_add_word(NJ_CLASS *iwnn, NJ_LEARN_WORD_INFO *word, NJ_UINT8 *demojidic) {

    NJ_UINT16 que_id;
    NJ_INT16 ret;
    NJ_UINT16 word_count;
    NJ_DIC_HANDLE handle = NULL;
    NJ_UINT16 from, to, id;
    NJ_UINT16 max_count;
    NJ_WQUE *que;
    NJ_UINT8 *p;
    NJ_UINT8 forward_flag = 0;                          /* ダミー       */

    handle = demojidic;

    /* 現在登録数を取得する             */
    /* （追加なので登録語数で良い）     */
    word_count = GET_LEARN_WORD_COUNT(handle);

    /* ユーザ辞書 */
    max_count = GET_LEARN_MAX_WORD_COUNT(handle);           /* 最大単語登録数 */
    if (word_count >= max_count) {
        /* 辞書が一杯だった */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_ADD_WORD, NJ_ERR_USER_DIC_FULL);
    }
    /* 空きキューを探す */
    que_id = 0;
    if (degiji_search_empty_que(handle, &que_id) < 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_ADD_WORD, NJ_ERR_DIC_BROKEN); /*NCH*/
    };

    if (word_count > 0) {
        /* 読みで二分検索する       */
        ret = degiji_search_range_by_yomi(iwnn, handle, NJ_CUR_OP_COMP,
                                   word->yomi, nj_strlen(word->yomi), &from, &to, &forward_flag);
        if (ret < 0) {
            return ret; /*NCH_FB*/
        }
        if (ret > 0) {
            /* 線形検索で読みと候補が同じものを検索し       */
            /* 一致すれば前品詞と後品詞も比べる             */
            /* （登録なので読み順インデックスで良い）       */
            p = LEARN_INDEX_TOP_ADDR(handle);
            p += (from * NJ_INDEX_SIZE);
            while (from <= to) {
                id = GET_UINT16(p);
                ret = degiji_que_strcmp_complete_with_hyouki(iwnn, handle, id,
                                                      word->yomi, nj_strlen(word->yomi), word->hyouki, 0);
                if (ret < 0) {
                    return ret; /*NCH_FB*/
                }
                if (ret == 1) {
                    que = degiji_get_que_allHinsi(iwnn, handle, id);
                    if (que == NULL) {
                        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_ADD_WORD, /*NCH*/
                                              NJ_ERR_DIC_BROKEN);
                    }
                    if ((word->f_hinsi == que->mae_hinsi) &&
                        (word->b_hinsi == que->ato_hinsi)) {
                        /* 既に同じ単語が登録されていた     */
                        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_ADD_WORD,
                                              NJ_ERR_SAME_WORD);
                    }
                }
                from++;
                p += NJ_INDEX_SIZE;
            }
        }
    }

    /* データ領域への追加 */
    ret = degiji_write_learn_data(iwnn, handle, que_id, word, 0, 0, 0, 0);
    if (ret < 0) {
        return ret;
    }

    return 0;
}

/**
 * 学習辞書から削除する
 *
 * @param[in] iwnn : 解析情報クラス
 * @param[in]  loc : 検索位置
 *
 * @retval      <0   エラー
 * @retval     >=0   正常終了
 */
static NJ_INT16 degiji_delete_word(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION *loc) {
    NJ_INT16 ret;
    NJ_UINT16 que_id;

    que_id = (NJ_UINT16)(loc->current >> 16);

    ret = degiji_delete_index(iwnn, (NJ_DIC_HANDLE)loc->ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
    if (ret < 0) {
        return ret; /*NCH_FB*/
    }
    if (ret == 0) {
        /* 削除対象のque_idが存在しなかった     */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_DELETE_WORD,
                              NJ_ERR_WORD_NOT_FOUND);
    }
    return 0;
}

/**
 * キューの順番に候補を探す（currentから格納順）
 *
 * この関数が呼ばれるのは
 * - 完全一致検索(NJ_CUR_OP_COMP)のとき
 * - 前方一致検索(NJ_CUR_OP_FORE)で、かつ、候補取得順が読み順(NJ_CUR_MODE_YOMI)のとき
 * - 逆引き(NJ_CUR_OP_REV)で、かつ、非圧縮カスタマイズ辞書のとき
 *
 * @param[in]               iwnn : 解析情報クラス
 * @param[in]               cond : 検索条件
 * @param[in,out]        loctset : 検索位置
 * @param[in]     search_pattern : 検索方法
 * @param[in]           comp_flg : 学習辞書の完全一致検索で一致した候補に対するつながり検索の有無<br>
 *                                （1:有り、1以外:無し）
 *
 * @retval                    <0   エラー
 * @retval                     0   候補なし
 * @retval                     1   候補あり
 */
static NJ_INT16 degiji_get_cand_by_sequential(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond,
                                              NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern,
                                              NJ_UINT8 comp_flg) {
    NJ_UINT16 current, from, to;
    NJ_UINT16 que_id;
    NJ_UINT8  *ptr, *p;
    NJ_INT16 ret;
    NJ_WQUE  *que;
    NJ_UINT8 forward_flag = 0;

    /* LOCATIONの現在状態の判別 */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        /* 複数キューは対象としない     */
        ret = degiji_search_range_by_yomi(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], search_pattern,
                                   cond->yomi, cond->ylen, &from, &to, &forward_flag);
        if (ret < 0) {
            return ret;
        }
        if (ret == 0) {
            if (forward_flag) {
                loctset->loct.status = NJ_ST_SEARCH_END;
            } else {
                loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            }
            return 0;
        }
        loctset->loct.top = from;
        loctset->loct.bottom = to;
        current = from;
    } else if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_READY) {
        /* sequentialなのでcurrentを一つ進める  */
        current = (NJ_UINT16)(loctset->loct.current + 1);
    } else {
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
        return 0; /*NCH_FB*/
    }

    /* 検索するインデックスの先頭アドレスを取得 */
    ptr = degiji_get_search_index_address((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], cond->operation);
    p = ptr + (current * NJ_INDEX_SIZE);

    while (current <= loctset->loct.bottom) {
        que_id = GET_UINT16(p);
        if (search_pattern == NJ_CUR_OP_COMP) {
            /* 完全一致検索     */
            ret = degiji_str_que_cmp(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], cond->yomi, cond->ylen, que_id, 1, 0);
            /* str_que_cmpは正常の戻り値は0, 1, 2で     */
            /* 一致した場合 ret = 1                     */
            if (ret == 2) {
                ret = 0; /*NCH*/
            }
        } else if (search_pattern == NJ_CUR_OP_FORE) {
            /* 前方一致検索     */
            ret = degiji_str_que_cmp(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], cond->yomi, cond->ylen, que_id, 2, 0);
            /* str_que_cmpは正常の戻り値は0, 1, 2で     */
            /* 一致した場合 ret = 1                     */
            if (ret == 2) {
                ret = 0; /*NCH*/
            }
        } else {
            /* 逆引き検索       */
            ret = degiji_str_que_cmp(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], cond->yomi, cond->ylen, que_id, 1, 1);
            /* str_que_cmpは正常の戻り値は0, 1, 2で     */
            /* 一致した場合 ret = 1                     */
            if (ret == 2) {
                ret = 0; /*NCH*/
            }
        }
        /* 下位のエラーをそのまま返す   */
        if (ret < 0) {
            return ret;
        }
        if (ret > 0) {
            if (search_pattern == NJ_CUR_OP_REV) {

                /* 品詞チェック */
                que = degiji_get_que_allHinsi(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
                if (que == NULL) {
                    return NJ_SET_ERR_VAL(NJ_FUNC_IS_CONTINUED, NJ_ERR_DIC_BROKEN);
                }
                if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                    /* 逆引き検索のとき */
                    loctset->loct.current = current;
                    loctset->loct.status = NJ_ST_SEARCH_READY;
                    /* 複数キューは見ていないのでcurrent_infoは固定     */
                    loctset->loct.current_info = (NJ_UINT8)0x10;
                    loctset->cache_freq = degiji_get_hindo(iwnn, loctset, search_pattern);
                    return 1;
                }
            } else {
                /* 完全一致     */
                /* 前方一致     */

                /* 品詞チェック */
                /* str_que_cmp or que_strcmp_complete_with_hyoukiが     */
                /* 成功しているのでここではエラーは返らない             */
                que = degiji_get_que_allHinsi(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
                if (que == NULL) {
                    return NJ_SET_ERR_VAL(NJ_FUNC_IS_CONTINUED, NJ_ERR_DIC_BROKEN);
                }
                if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                    loctset->loct.current = current;
                    loctset->loct.status = NJ_ST_SEARCH_READY;
                    /* 連続しているキューの数を格納     */
                    loctset->loct.current_info = (ret & 0x0f) << 4;
                    loctset->cache_freq = degiji_get_hindo(iwnn, loctset, search_pattern);
                    return 1;
                }
            }
        }
        p += NJ_INDEX_SIZE;
        current++;
    }

    /* 対象はなかった */
    loctset->loct.status = NJ_ST_SEARCH_END;
    return 0;
}

/**
 * 評価値順に候補を探す
 *
 * この関数が呼ばれるのは
 * - 前方一致検索(NJ_CUR_OP_FORE)で、
 *   かつ、候補取得順が頻度順(NJ_CUR_MODE_FREQ)or登録順(NJ_CUR_MODE_REGIST)のとき
 * - 逆引き検索(NJ_CUR_OP_REV)で、学習辞書またはユーザ辞書のとき
 *
 * @param[in]               iwnn : 解析情報クラス
 * @param[in]               cond : 検索条件
 * @param[in,out]        loctset : 検索位置
 * @param[in]     search_pattern : 検索方法
 *
 * @retval                    <0   エラー
 * @retval                     0   候補なし
 * @retval                     1   候補あり
 */
static NJ_INT16 degiji_get_cand_by_evaluate(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond,
                                            NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern) {
    NJ_UINT16 from, to, i;
    NJ_UINT16 que_id, oldest;
    NJ_UINT32 max_value, eval, current;
    NJ_UINT8  *ptr, *p;
    NJ_WQUE  *que;
    NJ_INT16 ret;
    NJ_INT32 found = 0;
    NJ_UINT8 forward_flag = 0;
    NJ_INT32 is_first_search, is_better_freq;

    /* 検索するインデックスの先頭アドレスを取得 */
    ptr = degiji_get_search_index_address((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], cond->operation);

    /* 最も古いque_idを取得する */
    oldest = GET_LEARN_NEXT_WORD_POS((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);

    /* LOCATIONの現在状態の判別 */
    current = 0;
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        /* 前方一致検索、逆引き検索 */
        ret = degiji_search_range_by_yomi(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], search_pattern,
                                          cond->yomi, cond->ylen, &from, &to, &forward_flag);
        if (ret <= 0) {
            loctset->loct.status = NJ_ST_SEARCH_END;
            if ((ret == 0) && (search_pattern == NJ_CUR_OP_REV)) {
                if (!forward_flag) {
                    /* 前方一致語もない */
                    loctset->loct.status = NJ_ST_SEARCH_END_EXT;
                }
            }
            return ret;
        }
        loctset->loct.top = from;
        loctset->loct.bottom = to;
        is_first_search = 1;
    } else if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_READY) {
        current = GET_UINT16(ptr + (loctset->loct.current * NJ_INDEX_SIZE));
        if (current < oldest) {
            /* 現在位置より新しいもの(上にあるもの)は、仮の最大値を
               加算する*/
            current += GET_LEARN_MAX_WORD_COUNT((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
        }
        is_first_search = 0;
    } else {
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
        return 0; /*NCH_FB*/
    }

    if (cond->mode != NJ_CUR_MODE_REGIST) {
        /* 頻度順の場合： max_valueを最低頻度(=oldest)で初期化する */
        max_value = oldest;
    } else {
        /* 頻度順の場合： max_valueを最高頻度(=oldest + 最大登録キュー数)で初期化する
         * 最大登録数が0x7FFFであるため、桁溢れすることはない。
         */
        max_value = oldest + GET_LEARN_MAX_WORD_COUNT((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
    }

    p = ptr + (loctset->loct.top * NJ_INDEX_SIZE);
    eval = current;
    for (i = (NJ_UINT16)loctset->loct.top; i <= (NJ_UINT16)loctset->loct.bottom; i++) {
        que_id = GET_UINT16(p);
        if (que_id < oldest) {
            eval = que_id + GET_LEARN_MAX_WORD_COUNT((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
        } else {
            eval = que_id;
        }
        /* 頻度値のチェック */
        if (cond->mode != NJ_CUR_MODE_REGIST) {
            /* 頻度順の場合
             * (1)頻度値がmax_value以上
             * かつ
             * (2)最初の一回目 または 2回目以降ならcurrentより小さいものを取得
             */
            is_better_freq = ((eval >= max_value) && ((is_first_search) || (eval < current))) ? 1 : 0;
        } else {
            /* 登録順の場合
             * (1)頻度値がmax_value以下
             * かつ
             * (2)最初の一回目 または 2回目以降ならcurrentより大きいものを取得
             */
            is_better_freq = ((eval <= max_value) && ((is_first_search) || (eval > current))) ? 1 : 0;
        }
        if (is_better_freq) {
            /* 条件内で最大と思しき候補を読みでチェック */
            if (search_pattern == NJ_CUR_OP_REV) {
                /* 逆引き（完全一致）*/
                ret = degiji_str_que_cmp(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], cond->yomi, cond->ylen, que_id, 1, 1);
                /* str_que_cmpは正常の戻り値は0, 1, 2で一致した場合 ret = 1 */
                if (ret == 2) {
                    ret = 0; /*NCH*/
                }
            } else {
                /* 前方一致検索     */
                ret = degiji_str_que_cmp(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], cond->yomi, cond->ylen, que_id, 2, 0);
                /* str_que_cmpは正常の戻り値は0, 1, 2で一致した場合 ret = 1 */
                if (ret == 2) {
                    ret = 0; /*NCH*/
                }
            }
            if (ret < 0) {
                return ret; /*NCH_FB*/
            }
            if (ret >= 1) {
                if (search_pattern == NJ_CUR_OP_REV) {

                    /* 品詞チェック */
                    que = degiji_get_que_allHinsi(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
                    if (que == NULL) {
                        return NJ_SET_ERR_VAL(NJ_FUNC_IS_CONTINUED, NJ_ERR_DIC_BROKEN);
                    }
                    if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                        /* 逆引き */
                        /* 複数キューは見ていないのでcurrent_infoは固定     */
                        loctset->loct.current_info = (NJ_UINT8)0x10;
                        loctset->loct.current = i;
                        max_value = eval;
                        found = 1;
                    }
                } else {
                    /* 前方一致     */

                    /* 品詞チェック */
                    /* str_que_cmp or que_strcmp_complete_with_hyoukiが     */
                    /* 成功しているのでここではエラーは返らない             */
                    que = degiji_get_que_allHinsi(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
                    if (que == NULL) {
                        return NJ_SET_ERR_VAL(NJ_FUNC_IS_CONTINUED, NJ_ERR_DIC_BROKEN);
                    }
                    if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                        /* 複数キューは見ていないのでcurrent_infoは固定     */
                        loctset->loct.current_info = (NJ_UINT8)0x10;
                        loctset->loct.current = i;
                        max_value = eval;
                        found = 1;
                    }
                }
            }
        }
        p += NJ_INDEX_SIZE;
    }

    /* 対象はなかった */
    if (found == 0) {
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    } else {
        loctset->loct.status = NJ_ST_SEARCH_READY;
        loctset->cache_freq = degiji_get_hindo(iwnn, loctset, search_pattern);
        return 1;
    }

}

/**
 * 検索条件により指定された読みと一致する範囲を2分検索する。
 *
 * @note 呼ばれるのは、search時の最初の1回目。
 *
 * @param[in]          iwnn : 解析情報クラス
 * @param[in]        handle : 辞書ハンドル
 * @param[in]            op : 検索方法
 * @param[in]          yomi : 読み文字列
 * @param[in]           len : 検索対象の読み文字配列長
 * @param[out]         from : 検索範囲先頭
 * @param[out]           to : 検索範囲末尾
 * @param[out] forward_flag : 前方一致フラグ（0:前方一致候補なし、1:前方一致候補あり）
 *
 * @retval               <0   エラー
 * @retval                0   見付からなかった
 * @retval                1   見付かった
 */
static NJ_INT16 degiji_search_range_by_yomi(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 op,
                                            NJ_CHAR  *yomi, NJ_UINT16 len, NJ_UINT16 *from, NJ_UINT16 *to,
                                            NJ_UINT8 *forward_flag) {
    NJ_UINT16 right, mid = 0, left, max;        /* バイナリサーチ用 */
    NJ_UINT16 que_id;
    NJ_UINT8  *ptr, *p;
    NJ_CHAR  *str;
    NJ_INT16 ret = 0;
    NJ_INT32 found = 0;
    NJ_UINT8 slen;
    NJ_INT32 cmp;

    /* 検索するインデックスの先頭アドレスを取得 */
    ptr = degiji_get_search_index_address(handle, op);
    /* 学習領域に登録されている語彙数を取得する */
    max = GET_LEARN_WORD_COUNT(handle);

    right = max - 1;
    left = 0;


    *forward_flag = 0;

    /* Operation IDのチェック */
    switch (op) {
    case NJ_CUR_OP_COMP:
    case NJ_CUR_OP_LINK:
    case NJ_CUR_OP_REV:
    case NJ_CUR_OP_FORE:
        /* 読みの完全一致検索、または */
        /* 読み・表記の完全一致検索時 */
        /* または逆引き検索時         */
        /* 前方一致検索時 */
        break;
    default:
        return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_PARAM_OPERATION); /*NCH_FB*/
    }

    while (left <= right) {
        mid = left + ((right - left) / 2);
        p = ptr + (mid * NJ_INDEX_SIZE);
        que_id = GET_UINT16(p);
        if (op == NJ_CUR_OP_REV) {
            str = degiji_get_hyouki(iwnn, handle, que_id, &slen);
        } else {
            str = degiji_get_string(iwnn, handle, que_id, &slen);
        }
        if (str == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN);
        }

        ret = nj_strncmp(yomi, str, len);
        if (op != NJ_CUR_OP_FORE) {
            /* 読みの完全一致検索、または */
            /* 読み・表記の完全一致検索時 */
            /* または逆引き検索時         */
            if (ret == 0) {
                if ((*forward_flag == 0) && (len <= (NJ_UINT16)slen)) {
                    /* 少なくとも前方一致はしている     */
                    *forward_flag = 1;
                }
                if (len > (NJ_UINT16)slen) {
                    ret = 1;
                } else if (len < (NJ_UINT16)slen) {
                    ret = -1;
                }
            }
        }
        if (ret == 0) {
            /* 先頭はこの場所より左にある */
            found = 1;
            break;
        } else if (ret < 0) {
            /* この場所より左にある */
            right = mid - 1;
            if (mid == 0) {
                break;
            }
        } else {
            /* この場所より右にある */
            left = mid + 1;
        }
    }

    if (!found) {
        return 0;
    }

    if (mid == 0) {
        *from = mid;
    } else {
        /* midの一つ前から読み順インデックスを線形検索 */
        p = ((mid - 1) * NJ_INDEX_SIZE) + ptr;
        /* leftの代わりにNJ_INT32のcmpを使っている      */
        for (cmp = mid - 1; cmp >= 0; cmp--) {
            que_id = GET_UINT16(p);
            if (op == NJ_CUR_OP_REV) {
                str = degiji_get_hyouki(iwnn, handle, que_id, &slen);
            } else {
                str = degiji_get_string(iwnn, handle, que_id, &slen);
            }
            if (str == NULL) {
                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN);
            }

            if (op != NJ_CUR_OP_FORE) {
                ret = nj_strncmp(yomi, str, len);
                if (ret == 0) {
                    if (len > (NJ_UINT16)slen) {
                        ret = 1;
                    } else if (len < (NJ_UINT16)slen) {
                        ret = -1;
                    }
                }
                if (ret > 0) {
                    /* この条件後には完全一致・包含関係で一致するものはない */
                    break;
                }
            } else {
                /* 前方一致時の処理 */
                if (nj_strncmp(yomi, str, len) != 0) {
                    break;      /* forループから抜ける */
                }
            }
            p -= NJ_INDEX_SIZE;
        }
        if (cmp < 0) {
            *from = 0;
        } else {
            *from = (NJ_UINT16)cmp + 1;
        }
    }


    if ((mid + 1) >= max) {
        *to = mid;
    } else {
        /* index中の位置をpに入れる */
        p = ((mid + 1) * NJ_INDEX_SIZE) + ptr;
        /* 線形検索で一致しなくなるまで探す */
        for (right = mid + 1; right < max; right++) {
            que_id = GET_UINT16(p);
            if (op == NJ_CUR_OP_REV) {
                str = degiji_get_hyouki(iwnn, handle, que_id, &slen);
            } else {
                str = degiji_get_string(iwnn, handle, que_id, &slen);
            }
            if (str == NULL) {
                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN);
            }

            if (op != NJ_CUR_OP_FORE) {
                ret = nj_strncmp(yomi, str, len);
                if (ret == 0) {
                    if (len > (NJ_UINT16)slen) {
                        ret = 1;
                    } else if (len < (NJ_UINT16)slen) {
                        ret = -1;
                    }
                }
                if (ret < 0) {
                    /* この条件後には完全一致・包含関係で一致するものはない */
                    break;
                }
            } else {
                /* 前方一致時の処理 */
                if (nj_strncmp(yomi, str, len) != 0) {
                    break;      /* forループから抜ける */
                }
            }
            p += NJ_INDEX_SIZE;
        }
        *to = right - 1;
    }

    return 1;
}

/**
 *      指定の読みとキューの読みの比較を行う
 *
 * @param[in]    iwnn : 解析情報クラス
 * @param[in]  handle : 辞書ハンドル
 * @param[in]    yomi : 読み文字列
 * @param[in] yomiLen : 読み文字配列長
 * @param[in]  que_id : キューID
 * @param[in]    mode : 0:包含、1:完全一致、2:前方一致
 * @param[in]     rev : 0:通常、1:逆引き
 *
 * @retval         <0   エラー
 * @retval          0   キューの読みの方が大きい
 * @retval          1   一致
 * @retval          2   指定読みの方が大きい
 */
static NJ_INT16 degiji_str_que_cmp(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_CHAR *yomi,
                                   NJ_UINT16 yomiLen, NJ_UINT16 que_id, NJ_UINT8 mode, NJ_UINT8 rev) {
    NJ_UINT8  *queYomi;
    NJ_UINT8  *yomiPtr;                 /* キュー内の文字列と byte 単位で比較するために必要な NJ_UINT8 ポインタ */
    NJ_UINT16 yomiByte;
    NJ_UINT16 yomiPos;
    NJ_UINT8  queYomiByte, queKouhoByte;
    NJ_UINT8  queYomiPos, queYomiSearchArea;
    NJ_INT16  complete;
    NJ_UINT8  *top_addr;
    NJ_UINT8  *bottom_addr;
    NJ_CHAR   *hira;                    /* 無変換候補取得用の読み文字列         */
    NJ_UINT8  slen;                     /* 無変換候補の読み取得用 読み長さ      */
    NJ_INT16  ret;                      /* 無変換候補比較の戻り値               */
    NJ_UINT16 que_size;

    if (que_id >= GET_LEARN_MAX_WORD_COUNT(handle)) {
        /* 辞書が壊れている     */
        return NJ_SET_ERR_VAL(NJ_FUNC_STR_QUE_CMP, NJ_ERR_DIC_BROKEN);
    }

    queYomi = POS_TO_ADDRESS(handle, que_id);
    switch (GET_TYPE_FROM_DATA(queYomi)) {
    case QUE_TYPE_EMPTY:
    case QUE_TYPE_JIRI:
    case QUE_TYPE_FZK:
        break;
    default:
        /* 辞書が壊れている     */
        return NJ_SET_ERR_VAL(NJ_FUNC_STR_QUE_CMP, NJ_ERR_DIC_BROKEN);
    }

    /* 指定読みが空文字列だったら前方一致   */
    if ((mode == 2) && (yomiLen == 0)) {
        return 1;
    }

    /* 読み長を取得、読みデータの先頭位置にポインタを進める */
    queYomiByte = GET_YSIZE_FROM_DATA(queYomi);
    queKouhoByte= GET_KSIZE_FROM_DATA(queYomi);

    if ((rev == 1) && (queKouhoByte == 0) && (GET_MFLG_FROM_DATA(queYomi) != 0)) {
        /* 逆引き検索で無変換候補かつカタカナ候補だったとき     */
        hira = degiji_get_string(iwnn, handle, que_id, &slen);
        if (hira == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_STR_QUE_CMP, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
        }

        /* カタカナ無変換候補を取得     */
        nje_convert_hira_to_kata(hira, &(iwnn->muhenkan_tmp[0]), slen);
        /* カタカナ無変換候補と比較する */
        ret = nj_strncmp(yomi, iwnn->muhenkan_tmp, yomiLen);
        if (ret < 0) {
            /* キューの方が大きい       */
            return 0; /*NCH_FB*/
        } else if (ret == 0) {
            /* 等しい                   */
            return 1;
        } else {
            /* 指定読みの方が大きい     */
            return 2; /*NCH_FB*/
        }
    }
    top_addr = LEARN_DATA_TOP_ADDR(handle);
    que_size = QUE_SIZE(handle);

    /* 読み先頭までポインタを進める     */
    queYomi += LEARN_QUE_STRING_OFFSET;                 /* 固定部分を進める */
    queYomiSearchArea = (NJ_UINT8)(QUE_SIZE(handle) - LEARN_QUE_STRING_OFFSET);
    if (rev == 1) {
        if (queKouhoByte > 0) {
            /* 逆引きのときはポインタは表記の先頭、queYomiByteに表記長を入れる   */
            /* ただし、無変換候補のときは読みを対象                             */
            bottom_addr = top_addr;
            bottom_addr += que_size * GET_LEARN_MAX_WORD_COUNT(handle) - 1;
            /*
             * 読みが最初のキューの範囲を超えていたら
             * 識別子をチェックしながらポインタを進める
             */
            while (queYomiSearchArea <= queYomiByte) {

                /* 次キューの先頭に移動         */
                queYomi += queYomiSearchArea;
                /* 最終キューの次は先頭キュー   */
                if (queYomi >= bottom_addr) {
                    queYomi = top_addr;
                }
                /* 次キューに接続しているので識別子チェック     */
                if (*queYomi++ != QUE_TYPE_NEXT) {
                    /* データ連続の識別子でない場合 */
                    return NJ_SET_ERR_VAL(NJ_FUNC_STR_QUE_CMP, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
                }
                queYomiByte -= queYomiSearchArea;
                queYomiSearchArea = (NJ_UINT8)(que_size - 1);
            }
            /* 残った読みの長さを検索範囲から減算       */
            queYomiSearchArea -= queYomiByte;
            /* 候補の先頭までポインタを進める           */
            queYomi += queYomiByte;
            /* queYomiByteを検索対象の候補の長さに入れ替える     */
            queYomiByte = queKouhoByte;
        }
    }

    complete = 0;
    yomiPos = 0; queYomiPos = 0;
    yomiPtr  = (NJ_UINT8*)yomi;
    yomiByte = (NJ_UINT16)(yomiLen * sizeof(NJ_CHAR));

    /* byte単位で文字列を比較 */
    while ((complete = (*yomiPtr - *queYomi)) == 0) {
        yomiPos++; queYomiPos++;
        /* キュー読みの最後     */
        if (queYomiPos >= queYomiByte) {
            if (queYomiByte == yomiByte) {
                /* 完全一致     */
                return 1;
            } else if (mode == 2) {
                /* 前方一致で候補なし   */
                return 2; /*NCH_FB*/
            } else {
                /* 包含一致 */
                return (mode + 1);
            }
        }
        if (yomiPos >= yomiByte) {
            /* 検索可能範囲は全て完全一致 */
            break;
        } else {
            yomiPtr++; queYomi++;
            if (queYomiPos >= queYomiSearchArea) {
                /* 次キュー検索 */
                bottom_addr = top_addr;
                bottom_addr += que_size * GET_LEARN_MAX_WORD_COUNT(handle) - 1;
                if (queYomi >= bottom_addr) {
                    queYomi = top_addr;
                }
                /* 次のキューにデータが格納されている場合 */
                /* 接続語彙情報の1byte進める            */
                if (*queYomi++ != QUE_TYPE_NEXT) {
                    /* データ連続の識別子でない場合 */
                    return NJ_SET_ERR_VAL(NJ_FUNC_STR_QUE_CMP, NJ_ERR_DIC_BROKEN);
                }
                queYomiSearchArea += (NJ_UINT8)(que_size - 1);
            }
        }
    }
    if (complete == 0) {
        if (yomiByte < queYomiByte) {
            /* 前方一致 */
            if (mode == 2) {
                return 1;
            }
            /* キューの読みの方が大きい */
            return 0;
        } else {
            /* 指定読みの方が大きい     */
            return 2;
        }
    } else if (complete < 0) {
        /* キューの読みの方が大きい     */
        return 0;
    } else {
        /* 指定読みの方が大きい */
        return 2;
    }
}

/**
 * キュー格納位置、辞書頻度から頻度を算出する
 *
 * @param[in]   handle : 辞書ハンドル
 * @param[in]     freq : 単語の頻度
 * @param[in] dic_freq : 辞書頻度情報
 * @param[in] freq_max : 辞書頻度上限値
 * @param[in] freq_min : 辞書頻度下限値
 *
 * @return  頻度 (エラー時:INIT_HINDO)
 */
static NJ_HINDO degiji_calculate_hindo(NJ_DIC_HANDLE handle, NJ_INT32 freq, NJ_DIC_FREQ *dic_freq, NJ_INT16 freq_max, NJ_INT16 freq_min) {
    NJ_HINDO  hindo;

    /* 整数値の演算のため先に積算を行っている、したがって       */
    /* 一旦NJ_INT32で計算してNJ_INT16に代入する。               */
    /* 最終結果はNJ_INT16の範囲内である                         */
    /* ユーザ辞書 底上げ頻度を設定 */
    hindo = (NJ_INT16)dic_freq->base;

    return NORMALIZE_HINDO(hindo, freq_max, freq_min);
}

/**
 * 検索位置の単語情報（頻度）を取得する
 *
 * @param[in]           iwnn : 解析情報クラス
 * @param[in]        loctset : 検索位置
 * @param[in] search_pattern : 検索方法
 *
 * @return 頻度 (エラー時:INIT_HINDO)
 */
static NJ_HINDO degiji_get_hindo(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset,
                                 NJ_UINT8 search_pattern) {
    NJ_WQUE   *que;
    NJ_UINT16 que_id, oldest;
    NJ_UINT8  offset;
    NJ_INT32  dic_freq;
    NJ_UINT16 max;
    NJ_UINT8  *learn_index_top_addr;

    /* 検索するインデックスの先頭アドレスを取得 */
    learn_index_top_addr = degiji_get_search_index_address((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], search_pattern);

    que_id = (NJ_UINT16)GET_UINT16(learn_index_top_addr +
                                   ((loctset->loct.current & 0xffffU) * NJ_INDEX_SIZE));
    oldest = GET_LEARN_NEXT_WORD_POS((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);

    offset = (loctset->loct.current_info & 0x0f);
    while (offset--) {
        que_id = degiji_search_next_que((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
    }

    que = degiji_get_que(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
    if (que == NULL) {
        return INIT_HINDO; /*NCH_FB*/
    }

    max = GET_LEARN_MAX_WORD_COUNT((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
    if (que_id >= oldest) {
        dic_freq = que_id - oldest;
    } else {
        dic_freq = que_id - oldest + max;
    }

    /* キュー格納位置、辞書頻度から頻度を算出する */
    return degiji_calculate_hindo((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], dic_freq, &(loctset->dic_freq), loctset->dic_freq_max, loctset->dic_freq_min);
}

/**
 * 検索位置の単語を取得する
 *
 * @param[in]     iwnn : 解析情報クラス
 * @param[in]  loctset : 検索位置
 * @param[out]    word : 単語情報
 *
 * @retval           0   候補なし
 * @retval           1   候補あり
 * @retval          <0   エラー
 */
static NJ_INT16 degiji_get_word(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word) {
    NJ_WQUE *que;
    NJ_UINT16 que_id;
    NJ_UINT8 offset;
    NJ_UINT8 *learn_index_top_addr;

    /* 検索するインデックスの先頭アドレスを取得 */
    learn_index_top_addr = degiji_get_search_index_address((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], GET_LOCATION_OPERATION(loctset->loct.status));

    que_id = (NJ_UINT16)GET_UINT16(learn_index_top_addr +
                                   ((loctset->loct.current & 0xffff) * NJ_INDEX_SIZE));

    offset = (loctset->loct.current_info & 0x0f);
    while (offset--) {
        que_id = degiji_search_next_que((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
    }

    que = degiji_get_que(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
    if (que == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_GET_WORD, NJ_ERR_CANNOT_GET_QUE); /*NCH_FB*/
    }

    word->stem.loc = loctset->loct;

    word->stem.loc.current &= 0x0000ffff;
    word->stem.loc.current |= ((NJ_UINT32)que_id << 16);

    /* 各検索ロジックから算出された頻度をそのまま格納 */
    word->stem.hindo = loctset->cache_freq;

    NJ_SET_FPOS_TO_STEM(word, que->mae_hinsi);
    /* 逆引き結果の場合は、info1/info2の値を逆転        */
    if (GET_LOCATION_OPERATION(loctset->loct.status) == NJ_CUR_OP_REV) {
        NJ_SET_KLEN_TO_STEM(word, que->yomi_len);
        if (que->hyouki_len > 0) {
            NJ_SET_YLEN_TO_STEM(word, que->hyouki_len);
        } else {
            /* 無変換候補の場合は読みの候補数   */
            NJ_SET_YLEN_TO_STEM(word, que->yomi_len);
        }
        NJ_SET_BPOS_TO_STEM(word, que->ato_hinsi);
    } else {
        NJ_SET_YLEN_TO_STEM(word, que->yomi_len);
        if (que->hyouki_len > 0) {
            NJ_SET_KLEN_TO_STEM(word, que->hyouki_len);
        } else {
            /* 無変換候補の場合は読みの候補数   */
            NJ_SET_KLEN_TO_STEM(word, que->yomi_len);
        }
        NJ_SET_BPOS_TO_STEM(word, que->ato_hinsi);
    }

    /* 擬似候補の種類をクリア   */
    word->stem.type = 0;

    return 1;
}

/**
 *      読み文字列を取得する
 *
 * @param[in]    iwnn : 解析情報クラス
 * @param[in]    word : 単語情報
 * @param[out] stroke : 読み文字列
 * @param[in]    size : strokeのbyteサイズ
 *
 * @retval         >0   取得した文字配列長（ヌル文字含まず）
 * @retval         <0   エラー
 */
static NJ_INT16 degiji_get_stroke(NJ_CLASS *iwnn, NJ_WORD *word, NJ_CHAR *stroke, NJ_UINT16 size) {
    NJ_UINT16 que_id;
    NJ_CHAR   *str;
    NJ_UINT8  slen;
    NJ_UINT8  ylen;

    que_id = (NJ_UINT16)(word->stem.loc.current >> 16);

    /* バッファサイズチェック   */
    if (GET_LOCATION_OPERATION(word->stem.loc.status) == NJ_CUR_OP_REV) {       /* 逆引きの場合 */
        ylen = (NJ_UINT8)NJ_GET_KLEN_FROM_STEM(word);
    } else {
        ylen = (NJ_UINT8)NJ_GET_YLEN_FROM_STEM(word);
    }
    if ((NJ_UINT16)((ylen+ NJ_TERM_LEN)*sizeof(NJ_CHAR)) > size) {
        /* バッファサイズエラー         */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_GET_STROKE, NJ_ERR_BUFFER_NOT_ENOUGH);
    }
    if (ylen == 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_GET_STROKE, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
    }
    str = degiji_get_string(iwnn, (NJ_DIC_HANDLE)word->stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id, &slen);

    /* 取得できなかった         */
    if (str == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_GET_STROKE, NJ_ERR_DIC_BROKEN);
    }

    /* NJ_UINT8 型文字列をコピー  */
    nj_strcpy(stroke, str);

    return slen;
}

/**
 *      読み文字列を取得する
 *
 * @param[in]       iwnn : 解析情報クラス
 * @param[in]       word : 単語情報
 * @param[out] candidate : 候補文字列
 * @param[in]       size : candidateのバイトサイズ
 *
 * @retval            >0   取得した文字配列長
 * @retval            <0   エラー
 */
static NJ_INT16 degiji_get_candidate(NJ_CLASS *iwnn, NJ_WORD *word,
                                     NJ_CHAR *candidate, NJ_UINT16 size) {
    NJ_UINT16 que_id;
    NJ_CHAR   *str;
    NJ_UINT16 klen;
    NJ_UINT8  slen;

    que_id = (NJ_UINT16)(word->stem.loc.current >> 16);

    /* バッファサイズチェック   */
    if (GET_LOCATION_OPERATION(word->stem.loc.status) == NJ_CUR_OP_REV) {
        /* 逆引き検索のときは読み文字列長をセット       */
        klen = NJ_GET_YLEN_FROM_STEM(word);
    } else {
        klen = NJ_GET_KLEN_FROM_STEM(word);
    }
    if (size < ((klen+NJ_TERM_LEN)*sizeof(NJ_CHAR))) {
        /* バッファサイズエラー         */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_GET_CANDIDATE, NJ_ERR_BUFFER_NOT_ENOUGH);
    }
    str = degiji_get_hyouki(iwnn, (NJ_DIC_HANDLE)word->stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id, &slen);
    if (str == NULL) {
        /* 表記取得エラー       */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_GET_CANDIDATE, NJ_ERR_DIC_BROKEN);
    }

    /* 文字列をコピー  */
    nj_strcpy(candidate, str);

    return klen;
}

/**
 * 学習辞書・ユーザ辞書のチェックを行う<br>
 *      自動復旧フラグが指定されていた場合は復旧処理を行う
 *
 * @param[in]    iwnn : 解析情報クラス
 * @param[in]  handle : 辞書ハンドル
 * @param[in] restore : 自動復旧フラグ
 *                      0    ：復旧を行わない
 *                      0以外：復旧処理を行う
 *
 * @retval         <0   エラー
 *                      NJ_ERR_DIC_BROKEN     : 辞書が壊れていた
 *                      NJ_ERR_CANNOT_RESTORE : 復旧できなかった
 * @retval        >=    正常終了
 */
static NJ_INT16 degiji_check_dic(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 restore) {
    NJ_UINT16 flg;
    NJ_UINT16 word_cnt, max;
    NJ_UINT8 *ptr;
    NJ_UINT16 bk_point;
    NJ_UINT16 target_id;
    NJ_UINT16 i;
    NJ_UINT16 id1 = 0, id2 = 0;
    NJ_UINT8 slen;
    NJ_INT16 finish_flg = 0;

    /* 対象は学習辞書かユーザ辞書       */
    if ((NJ_GET_DIC_TYPE(handle) != NJ_DIC_TYPE_LEARN)
        && (NJ_GET_DIC_TYPE(handle) != NJ_DIC_TYPE_USER)) {
        /* 辞書タイプ不正       */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC, NJ_ERR_DIC_TYPE_INVALID); /*NCH_FB*/
    }

    /* 最初に登録語数とインデックスの範囲のみのチェックを行う   */
    /* このチェックすらNGだった場合復旧不可と見なす             */
    /* （学習辞書orユーザ辞書限定なので登録語数で良い）         */
    word_cnt = GET_LEARN_WORD_COUNT(handle);
    max = GET_LEARN_MAX_WORD_COUNT(handle);
    if (word_cnt > max) {
        /* 最大登録語数オーバー */
        if (restore == 0) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC,
                                  NJ_ERR_DIC_BROKEN);
        } else {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC,
                                  NJ_ERR_CANNOT_RESTORE);
        }
    }
    /* 読み順インデックス領域の先頭アドレスを取得する */
    ptr = LEARN_INDEX_TOP_ADDR(handle);
    for (i = 0; i < word_cnt; i++) {
        id1 = GET_UINT16(ptr);
        /* キューIDが範囲外     */
        if (id1 >= max) {
            if (restore == 0) { /*NCH_FB*/
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC, /*NCH_FB*/
                                      NJ_ERR_DIC_BROKEN);
            } else {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC, /*NCH_FB*/
                                      NJ_ERR_CANNOT_RESTORE);
            }
        }
        ptr += NJ_INDEX_SIZE;
    }

    /* 表記文字列インデックス領域の先頭アドレスを取得する */
    ptr = LEARN_INDEX_TOP_ADDR2(handle);
    for (i = 0; i < word_cnt; i++) {
        id1 = GET_UINT16(ptr);
        /* キューIDが範囲外     */
        if (id1 >= max) {
            if (restore == 0) { /*NCH_FB*/
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC, /*NCH_FB*/
                                      NJ_ERR_DIC_BROKEN);
            } else {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC, /*NCH_FB*/
                                      NJ_ERR_CANNOT_RESTORE);
            }
        }
        ptr += NJ_INDEX_SIZE;
    }

    /* 待避した更新後の登録語数 */
    flg = GET_UINT16(handle + POS_WRITE_FLG);
    /* 挿入or削除対象キュー     */
    target_id = GET_UINT16(handle + POS_WRITE_FLG + 2);

    /* 待避した登録語数とtarget_idの範囲外チェック      */
    /* どちらかが範囲外であれば復旧不可とみなす         */
    if (((flg != word_cnt) && (flg != (word_cnt + 1)) && (flg != (word_cnt - 1))) ||
        (target_id >= max)) {
        if (restore == 0) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC,
                                  NJ_ERR_DIC_BROKEN);
        } else {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC,
                                  NJ_ERR_CANNOT_RESTORE);
        }
    }

    /* 待避した登録語数と現在の登録語数が等しければ復旧処理は行わない   */
    /* この場合はすぐに復旧後チェックを行う                             */
    if (flg == (word_cnt + 1)) {
        /*
         * 追加処理中に電断したと見なして
         * 復旧処理を行う
         */
        if (restore == 0) {
            /* 復旧指定がされてなければreturn   */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC, NJ_ERR_DIC_BROKEN);
        }
        bk_point = 0xffff;
        /* 読み順インデックス領域の先頭アドレスを取得する */
        ptr = LEARN_INDEX_TOP_ADDR(handle);
        for (i=0; i < word_cnt; i++, ptr += NJ_INDEX_SIZE) {
            id2 = GET_UINT16(ptr);
            /* 挿入対象キューがあった   */
            if (id2 == target_id) {
                /* 表記文字列インデックスチェックへ */
                finish_flg = 1;
                break;
            }
            if ((i > 0) && (id1 == id2)) {
                /* 重複IDがあった       */
                bk_point = i - 1;
                break;
            }
            id1 = id2;
        }

        /* 挿入処理が終了していないとき */
        if (finish_flg == 0) {
            if (bk_point != 0xffff) {
                /* 重複IDがあった                                               */
                /* 重複位置以降のデータ（登録語数＋１まで）を一つ前に詰める     */
                degiji_shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(word_cnt - bk_point));
            } else {

                /* 表記文字列インデックスの削除対象位置を検索 */
                ptr = LEARN_INDEX_TOP_ADDR2(handle);
                for (i=0; i < word_cnt; i++, ptr += NJ_INDEX_SIZE) {
                    id2 = GET_UINT16(ptr);
                    /* 挿入対象キューがあった   */
                    if (id2 == target_id) {
                        /*
                         * 挿入対象キューIDを削除する。
                         * 挿入対象キューID以降(ptr+NJ_INDEX_SIZE)のデータを
                         * 前に詰める
                         */
                        degiji_shift_index((ptr + NJ_INDEX_SIZE), SHIFT_LEFT, (NJ_UINT16)(word_cnt - i));
                        finish_flg = 1; /* 挿入処理が終了している       */
                        break;
                    }
                    if ((i > 0) && (id1 == id2)) {
                        /* 重複IDがあった       */
                        bk_point = i - 1;
                        break;
                    }
                    id1 = id2;
                }

                /* 表記インデックスに挿入対象キューが存在しないとき */
                if (finish_flg == 0) {
                    if (bk_point != 0xffff) {
                        /* 重複IDがあった                                               */
                        /* 重複位置以降のデータ（登録語数＋１まで）を一つ前に詰める     */
                        degiji_shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(word_cnt - bk_point));
                    }
                }
            }

            /* 挿入対象キューを未使用にする     */
            ptr = POS_TO_ADDRESS(handle, target_id);
            *ptr = QUE_TYPE_EMPTY;
            /* 登録語数を電断用flgの上位2byteにコピーする       */
            COPY_UINT16(handle + POS_WRITE_FLG,
                        handle + POS_LEARN_WORD + 2);
        } else {
            bk_point = 0xffff;
            finish_flg = 0;
            /* 表記文字列インデックス領域の先頭アドレスを取得する */
            ptr = LEARN_INDEX_TOP_ADDR2(handle);
            for (i=0; i < word_cnt; i++, ptr += NJ_INDEX_SIZE) {
                id2 = GET_UINT16(ptr);
                /* 挿入対象キューがあった       */
                if (id2 == target_id) {
                    /* ロールフォワード                                 */
                    /* 登録語数更新                                     */
                    /* 登録語数に電断用flgの上位2byteをコピーする       */
                    COPY_UINT16(handle + POS_LEARN_WORD + 2,
                                handle + POS_WRITE_FLG);
                    finish_flg = 1;     /* 挿入処理が終了している       */
                    break;
                }
                if ((i > 0) && (id1 == id2)) {
                    /* 重複IDがあった   */
                    bk_point = i - 1;
                    break;
                }
                id1 = id2;
            }

            /* 挿入処理が終了していないとき */
            if (finish_flg == 0) {
                if (bk_point != 0xffff) {
                    /* 重複IDがあった                                           */
                    /* 重複位置以降のデータ（登録語数＋１まで）を一つ前に詰める */
                    degiji_shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(word_cnt - bk_point));
                }

                /* 読み文字列インデックスの削除対象位置を検索 */
                ptr = LEARN_INDEX_TOP_ADDR(handle);
                for (i=0; i < word_cnt; i++) {
                    id2 = GET_UINT16(ptr);
                    ptr += NJ_INDEX_SIZE;
                    /* キューが一致 */
                    if (id2 == target_id) {
                        /*
                         * 挿入対象キューIDを削除する。
                         * 挿入対象キューID以降(ptr+NJ_INDEX_SIZE)のデータを
                         * 前に詰める
                         */
                        degiji_shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(word_cnt - i));
                    }
                }

                /* 挿入対象キューを未使用にする */
                ptr = POS_TO_ADDRESS(handle, target_id);
                *ptr = QUE_TYPE_EMPTY;
                /* 登録語数を電断用flgの上位2byteにコピーする   */
                COPY_UINT16(handle + POS_WRITE_FLG,
                            handle + POS_LEARN_WORD + 2);
            }
        }
    } else if (flg == (word_cnt - 1)) {
        /*
         * 削除処理中に電断したと見なして
         * 復旧処理を行う
         */
        if (restore == 0) {
            /* 復旧指定がされてなければreturn   */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC, NJ_ERR_DIC_BROKEN);
        }
        bk_point = 0xffff;
        /* 読み順インデックス領域の先頭アドレスを取得する */
        ptr = LEARN_INDEX_TOP_ADDR(handle);
        for (i = 0; i < word_cnt; i++, ptr += NJ_INDEX_SIZE) {
            id2 = GET_UINT16(ptr);
            if (id2 == target_id) {
                /* 削除対象キューがあった       */
                bk_point = i;
                ptr += NJ_INDEX_SIZE;
                break;
            }
            if ((i > 0) && (id1 == id2)) {
                /* 重複IDがあった       */
                bk_point = i - 1;
                break;
            }
            id1 = id2;
        }
        if (bk_point != 0xffff) {
            /* 重複IDがあった                                           */
            /* 重複位置以降のデータ（登録語数まで）を一つ前に詰める     */
            /* 削除対象キュー以降データ（登録語数まで）を一つ前に詰める */
            degiji_shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(word_cnt - bk_point - 1));
        }

        bk_point = 0xffff;
        /* 表記文字列インデックス領域の先頭アドレスを取得する */
        ptr = LEARN_INDEX_TOP_ADDR2(handle);
        for (i = 0; i < word_cnt; i++, ptr += NJ_INDEX_SIZE) {
            id2 = GET_UINT16(ptr);
            if (id2 == target_id) {
                /* 削除対象キューがあった       */
                bk_point = i;
                ptr += NJ_INDEX_SIZE;
                break;
            }
            if ((i > 0) && (id1 == id2)) {
                /* 重複IDがあった       */
                bk_point = i - 1;
                break;
            }
            id1 = id2;
        }
        if (bk_point != 0xffff) {
            /* 重複IDがあった                                           */
            /* 重複位置以降のデータ（登録語数まで）を一つ前に詰める     */
            /* 削除対象キュー以降データ（登録語数まで）を一つ前に詰める */
            degiji_shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(word_cnt - bk_point - 1));
        }

        /* 削除対象キューを未使用にする */
        ptr = POS_TO_ADDRESS(handle, target_id);
        *ptr = QUE_TYPE_EMPTY;
        /* 登録語数更新                                 */
        /* 登録語数に電断用flgの下位2byteをコピーする   */
        COPY_UINT16(handle + POS_LEARN_WORD + 2,
                    handle + POS_WRITE_FLG);
    }

    word_cnt = GET_LEARN_WORD_COUNT(handle);

    /* 復旧後チェック                   */
    /* 読み順インデックスに格納されているキューIDの
     * 読みと候補が取得できるかチェックする     */
    ptr = LEARN_INDEX_TOP_ADDR(handle);
    for (i = 0; i < word_cnt; i++) {
        id1 = GET_UINT16(ptr);
        if (degiji_get_hyouki(iwnn, handle, id1, &slen) == NULL) {
            if (restore == 0) {
                /* 辞書が壊れている     */
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC,
                                      NJ_ERR_DIC_BROKEN);
            } else {
                /* 復旧できなかった     */
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC,
                                      NJ_ERR_CANNOT_RESTORE);
            }
        }
        ptr += NJ_INDEX_SIZE;
    }

    /* キューIDから読み、候補が取得できるかどうかは上記処理で
     * チェック済なので、表記インデックスについては、範囲外の
     * キューIDが格納されていない点のみチェックする。*/
    ptr = LEARN_INDEX_TOP_ADDR2(handle);
    for (i = 0; i < word_cnt; i++) {
        id1 = GET_UINT16(ptr);
        /* キューIDが範囲外     */
        if (id1 >= max) {
            if (restore == 0) { /*NCH_FB*/
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC, /*NCH_FB*/
                                      NJ_ERR_DIC_BROKEN);
            } else {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC, /*NCH_FB*/
                                      NJ_ERR_CANNOT_RESTORE);
            }
        }
        ptr += NJ_INDEX_SIZE;
    }

    return 0;
}

/**
 * 学習辞書、ユーザ辞書の読み順インデックス領域をクリアし、全キューを未使用状態にする
 *
 * @param[in]     handle : 辞書ハンドル
 *
 * @retval             0   正常終了 ※ 正常終了以外の戻り値は無い
 *
 *:::DOC_END
 */
static NJ_INT16 degiji_init_area(NJ_DIC_HANDLE handle) {
    NJ_UINT8 *ptr;
    NJ_UINT16 cnt, *p;
    NJ_UINT32 i;

    /* 読み順インデックスの先頭アドレス */
    ptr = LEARN_INDEX_TOP_ADDR(handle);
    p = (NJ_UINT16 *)ptr;

    /* 読み順インデックスをクリア       */
    cnt = GET_LEARN_MAX_WORD_COUNT(handle);
    for (i = 0; i <= cnt; i++) {
        *p++ = 0x0000;
    }
    /* 表記順インデックスの先頭アドレス */
    ptr = LEARN_INDEX_TOP_ADDR2(handle);
    p = (NJ_UINT16 *)ptr;

    /* 表記順インデックスをクリア       */
    cnt = GET_LEARN_MAX_WORD_COUNT(handle);
    for (i = 0; i <= cnt; i++) {
        *p++ = 0x0000;
    }

    /* キューを未使用状態にする         */
    ptr = POS_TO_ADDRESS(handle, 0);
    for (i = 0; i < cnt; i++) {
        *ptr = QUE_TYPE_EMPTY;
        ptr += QUE_SIZE(handle);
    }
    return 0;
}

/**
 * 評価値順に候補を探す(キャッシュ検索用)
 *
 * この関数が呼ばれるのは
 * - 前方一致検索(NJ_CUR_OP_FORE)で、かつ、候補取得順が頻度順(NJ_CUR_MODE_FREQ)のとき
 *
 * @param[in]               iwnn : 解析情報クラス
 * @param[in]               cond : 検索条件
 * @param[in,out]        loctset : 検索位置
 * @param[in]     search_pattern : 検索方法
 * @param[in]                idx : 辞書のインデックス
 *
 * @retval                    <0   エラー
 * @retval                     0   候補なし
 * @retval                     1   候補あり
 */
static NJ_INT16 degiji_get_cand_by_evaluate2(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond,
                                             NJ_SEARCH_LOCATION_SET *loctset,
                                             NJ_UINT8 search_pattern,
                                             NJ_UINT16 idx) {
    NJ_UINT16 from, to, i;
    NJ_UINT16 que_id, oldest;
    NJ_UINT32 max_value, eval, current;
    NJ_UINT8  *ptr, *p;
    NJ_WQUE *que;
    NJ_INT16 ret = 0;
    NJ_INT32 found = 0;
    NJ_UINT8 forward_flag = 0;

    /* 新検索 */
    NJ_UINT16               abIdx;
    NJ_UINT16               abIdx_old;
    NJ_UINT16               tmp_len;
    NJ_UINT16               yomi_clen;
    NJ_UINT16               j,l,m;
    NJ_UINT8                cmpflg;
    NJ_UINT8                endflg = 0;
    NJ_CHAR                 *str;
    NJ_CHAR                 *key;
    NJ_CHAR                 char_tmp[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_CHAR                 *pchar_tmp;
    NJ_SEARCH_CACHE         *psrhCache = cond->ds->dic[idx].srhCache;
    NJ_UINT16               endIdx;
    NJ_UINT8                slen;
    NJ_UINT16               addcnt = 0;
    NJ_CHAR                 *yomi;
    NJ_UINT8                aimai_flg = 0x01;
    NJ_CHARSET              *pCharset = cond->charset;
#ifdef NJ_OPT_CHARSET_2BYTE
    NJ_UINT16 start;
    NJ_UINT16 end;
#endif /* NJ_OPT_CHARSET_2BYTE */

    if (NJ_GET_CACHEOVER_FROM_SCACHE(psrhCache)) {
        aimai_flg = 0x00;
    }

    /* 検索するインデックスの先頭アドレスを取得 */
    ptr = degiji_get_search_index_address((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], cond->operation);

    /* 最も古いque_idを取得する */
    oldest = GET_LEARN_NEXT_WORD_POS((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
    max_value = oldest;

    /* LOCATIONの現在状態の判別 */
    current = 0;
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        /* 複数キューは対象としない */
        /* 読み文字列の状態を調べる */
        key       = cond->ds->keyword;
        yomi      = cond->yomi;
        yomi_clen = cond->yclen;

        /* 読み文字列の比較チェック */
        endflg = 0x00;

        /* キャッシュエリアの先頭が0xFFFFなら、
         * cmpflgに1をセットする(キャッシュを作成し直す)
         */
        if (psrhCache->keyPtr[0] == 0xFFFF) {
            cmpflg = 0x01;
            psrhCache->keyPtr[0] = 0x0000;
        } else {
            cmpflg = 0x00;
        }

        if (yomi_clen >= NJ_MAX_KEYWORD) {
            return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_YOMI_TOO_LONG);
        }

        for (i = 0; i < yomi_clen; i++) {
            j = i;

            /* インデックス情報の構築。 */
            if (!cmpflg) { /* cmpflg == 0x00 */
                /* 一文字分を比較してOKだった。 */
                if (((j != 0) && (psrhCache->keyPtr[j] == 0)) || (psrhCache->keyPtr[j+1] == 0)) {
                    /* インデックス情報がない */
                    cmpflg = 0x01;
                } else {
                    /* インデックス情報あり */
                }
            }

            if (cmpflg) { /* cmpflg == 0x01 */
                /* インデックス情報なしと判定された場合 */
                if (!j) { /* j == 0 */
                    /* 初回のインデックス情報を作成する。 */
                    abIdx = 0;
                    addcnt = 0;
                    nj_charncpy(char_tmp, yomi, 1);
                    tmp_len = nj_strlen(char_tmp);
                    ret = degiji_search_range_by_yomi(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], search_pattern,
                                               char_tmp, tmp_len, &from,
                                               &to, &forward_flag);
                    if (ret < 0) {
                        /* 異常発生 */
                        /* 現在位置を次文字の書き込み位置に格納する。 */
                        psrhCache->keyPtr[j+1] = abIdx; /*NCH_FB*/
                        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
                        return ret; /*NCH_FB*/
                    } else if (ret > 0) {
                        /* 存在した場合。 */
                        psrhCache->storebuff[abIdx].top    = from;
                        psrhCache->storebuff[abIdx].bottom = to;
                        psrhCache->storebuff[abIdx].idx_no = (NJ_INT8)tmp_len;
                        addcnt++;
                        abIdx++;
                        psrhCache->keyPtr[j+1] = abIdx;
                    } else {
                        psrhCache->keyPtr[j+1] = abIdx;
                    }

                    if ((!endflg) && (pCharset != NULL) && aimai_flg) {
                        /* 曖昧検索処理 */
#ifdef NJ_OPT_CHARSET_2BYTE
                    /* keyに一致するあいまい文字セットの範囲を２分検索する */
                    if (njd_search_charset_range(pCharset, key, &start, &end) == 1) {
                        /* 範囲が見つかった */
                        for (l = start; l <= end; l++) {
#else /* NJ_OPT_CHARSET_2BYTE */
                        for (l = 0; l < pCharset->charset_count; l++) {
                            /* 曖昧検索分の検出 */
                            if (nj_charncmp(yomi, pCharset->from[l], 1) == 0) {
#endif /* NJ_OPT_CHARSET_2BYTE */
                                /* 一致しているなら */
                                nj_strcpy(char_tmp, pCharset->to[l]);
                                tmp_len = nj_strlen(char_tmp);
                                ret = degiji_search_range_by_yomi(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], search_pattern,
                                                           char_tmp, tmp_len, &from, &to, &forward_flag);
                                if (ret < 0) {
                                    /* 異常発生 */
                                    /* 現在位置を次文字の書き込み位置に格納する。 */
                                    psrhCache->keyPtr[j+1] = abIdx; /*NCH_FB*/
                                    loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
                                    return ret; /*NCH_FB*/
                                } else if (ret > 0) {
                                    /* 存在した場合。 */
                                    /* キャッシュ溢れ発生 */
                                    if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                        psrhCache->keyPtr[j+1] = 0;
                                        return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_NOT_ENOUGH);
                                    }
                                    psrhCache->storebuff[abIdx].top    = from;
                                    psrhCache->storebuff[abIdx].bottom = to;
                                    psrhCache->storebuff[abIdx].idx_no = (NJ_INT8)tmp_len;
                                    if (addcnt == 0) {
                                        psrhCache->keyPtr[j] = abIdx;
                                    }
                                    abIdx++;
                                    addcnt++;
                                    psrhCache->keyPtr[j+1] = abIdx;
                                } else {
                                    psrhCache->keyPtr[j+1] = abIdx;
                                }
                            } /* else {} */
                        } /* for */
                    } /* if */
                } else {
                    /* 現在のインデックス情報を取得する */
                    if (psrhCache->keyPtr[j] == psrhCache->keyPtr[j-1]) {
                        /* 検索結果が存在しない */
                        psrhCache->keyPtr[j+1] = psrhCache->keyPtr[j-1];
                        endflg = 0x01;
                    } else {
                        /* インデックス有効数を取得 */
                        endIdx = psrhCache->keyPtr[j];
                        abIdx_old = psrhCache->keyPtr[j-1];

                        if (NJ_GET_CACHEOVER_FROM_SCACHE(psrhCache)) {
                            abIdx = psrhCache->keyPtr[j - 1];
                            psrhCache->keyPtr[j] = abIdx;
                        } else {
                            abIdx = psrhCache->keyPtr[j];
                        }
                        addcnt = 0;

                        if ((abIdx > NJ_SEARCH_CACHE_SIZE) || (abIdx_old >= NJ_SEARCH_CACHE_SIZE) ||
                            (endIdx > NJ_SEARCH_CACHE_SIZE)) {
                            /* キャッシュが破壊されている */
                            return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_BROKEN); /*NCH_FB*/
                        }
                        for (m = abIdx_old; m < endIdx; m++) {
                            /* QueIdを取得する */
                            p = ptr + (psrhCache->storebuff[m].top * NJ_INDEX_SIZE);
                            que_id = GET_UINT16(p);

                            /* QueIdから文字列を取得する */
                            str = degiji_get_string(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id, &slen);

                            if (str == NULL) {
                                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
                            }

                            /* 必要読み文字列数分取得する */
                            nj_strncpy(char_tmp, str, psrhCache->storebuff[m].idx_no);
                            char_tmp[psrhCache->storebuff[m].idx_no] = NJ_CHAR_NUL;

                            pchar_tmp = &char_tmp[psrhCache->storebuff[m].idx_no];
                            nj_charncpy(pchar_tmp, yomi, 1);
                            tmp_len = nj_strlen(char_tmp);


                            ret = degiji_search_range_by_yomi2(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], search_pattern,
                                                        char_tmp, tmp_len,
                                                        (NJ_UINT16)(psrhCache->storebuff[m].top),
                                                        (NJ_UINT16)(psrhCache->storebuff[m].bottom),
                                                        &from, &to, &forward_flag);
                            if (ret < 0) {
                                /* 異常発生 */
                                /* 現在位置を次文字の書き込み位置に格納する。 */
                                psrhCache->keyPtr[j+1] = abIdx; /*NCH_FB*/
                                loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
                                return ret; /*NCH_FB*/
                            } else if (ret > 0) {
                                /* 存在した場合。 */
                                /* キャッシュ溢れ発生 */
                                if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                    psrhCache->keyPtr[j+1] = 0;
                                    return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_NOT_ENOUGH);
                                }
                                psrhCache->storebuff[abIdx].top    = from;
                                psrhCache->storebuff[abIdx].bottom = to;
                                psrhCache->storebuff[abIdx].idx_no = (NJ_INT8)tmp_len;
                                if (addcnt == 0) {
                                    psrhCache->keyPtr[j] = abIdx;
                                }
                                abIdx++;
                                addcnt++;
                                psrhCache->keyPtr[j+1] = abIdx;
                            } else {
                                psrhCache->keyPtr[j+1] = abIdx;
                            }

                            if ((!endflg) && (pCharset != NULL) && aimai_flg) {
                                /* 曖昧検索処理 */
#ifdef NJ_OPT_CHARSET_2BYTE
                                /* keyに一致するあいまい文字セットの範囲を２分検索する */
                                if (njd_search_charset_range(pCharset, key, &start, &end) == 1) {
                                    /* 範囲が見つかった */
                                    for (l = start; l <= end; l++) {
#else /* NJ_OPT_CHARSET_2BYTE */
                                for (l = 0; l < pCharset->charset_count; l++) {
                                    /* 曖昧検索分の検出 */
                                    if (nj_charncmp(yomi, pCharset->from[l], 1) == 0) {
#endif /* NJ_OPT_CHARSET_2BYTE */
                                        /* 一致しているなら */
                                        tmp_len = nj_strlen(pCharset->to[l]);

                                        nj_strncpy(pchar_tmp, pCharset->to[l], tmp_len);
                                        *(pchar_tmp + tmp_len) = NJ_CHAR_NUL;
                                        tmp_len = nj_strlen(char_tmp);
                                        ret = degiji_search_range_by_yomi2(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], search_pattern,
                                                                    char_tmp, tmp_len,
                                                                    (NJ_UINT16)(psrhCache->storebuff[m].top),
                                                                    (NJ_UINT16)(psrhCache->storebuff[m].bottom),
                                                                    &from, &to, &forward_flag);
                                        if (ret < 0) {
                                            /* 異常発生 */
                                            /* 現在位置を次文字の書き込み位置に格納する。 */
                                            psrhCache->keyPtr[j+1] = abIdx; /*NCH_FB*/
                                            loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
                                            return ret; /*NCH_FB*/
                                        } else if (ret > 0) {
                                            /* 存在した場合。 */
                                            /* キャッシュ溢れ発生 */
                                            if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                                psrhCache->keyPtr[j+1] = 0;
                                                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_NOT_ENOUGH);
                                            }
                                            psrhCache->storebuff[abIdx].top    = from;
                                            psrhCache->storebuff[abIdx].bottom = to;
                                            psrhCache->storebuff[abIdx].idx_no = (NJ_INT8)tmp_len;
                                            abIdx++;
                                            addcnt++;
                                            psrhCache->keyPtr[j+1] = abIdx;
                                        } else {
                                            psrhCache->keyPtr[j+1] = abIdx;
                                        }
                                    } /* else {} */
                                } /* for */
                            } /* if */
                        } /* for */
                    } /* if */
                }
            }
            yomi += UTL_CHAR(yomi);
            key  += UTL_CHAR(key);
        }

        /* 今回の検索結果件数が0かつキャッシュに候補がない（検索対象読み文字列長-1の開始位置と終了位置が同じ）場合に終了 */
        if ((addcnt == 0) && (psrhCache->keyPtr[yomi_clen - 1] == psrhCache->keyPtr[yomi_clen])) {
            endflg = 0x01;
        }

        if (endflg) {           /* endflg == 0x01 */
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }
    } else if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_READY) {
        current = GET_UINT16(ptr + (loctset->loct.current * NJ_INDEX_SIZE));
        if (current < oldest) {
            /* 現在位置より新しいもの(上にあるもの)は、仮の最大値を
               加算する*/
            current += GET_LEARN_MAX_WORD_COUNT((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
        }
    } else {
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
        return 0; /*NCH_FB*/
    }


    j = cond->yclen - 1;

    abIdx = psrhCache->keyPtr[j];
    abIdx_old = psrhCache->keyPtr[j+1];
    /* インデックス有効数を取得 */
    endIdx = abIdx_old;
    if ((abIdx >= NJ_SEARCH_CACHE_SIZE) || (abIdx_old > NJ_SEARCH_CACHE_SIZE)) {
        /* キャッシュが破壊されている */
        return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_BROKEN); /*NCH_FB*/
    }
    p = ptr + (psrhCache->storebuff[abIdx].top * NJ_INDEX_SIZE);
    que_id = GET_UINT16(p);
    eval = current;

    /* 複数キャッシュから候補を選択する。 */

    if (psrhCache->keyPtr[j] < psrhCache->keyPtr[j + 1]) {
        if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
            /*
             * あいまい検索有効時
             *   初回時    ：入力文字列に近いキャッシュから頻度最大値を無条件で取得。
             * あいまい検索無効時
             *   初回時    ：頻度最大値を無条件で取得。
             */
            endIdx = abIdx + 1;
            NJ_SET_AIMAI_TO_SCACHE(psrhCache);
        }

        for (m = abIdx; m < endIdx; m++) {
            p = ptr + (psrhCache->storebuff[m].top * NJ_INDEX_SIZE);
            que_id = GET_UINT16(p);
            eval = current;

            for (i = (NJ_UINT16)psrhCache->storebuff[m].top; i <= (NJ_UINT16)psrhCache->storebuff[m].bottom; i++) {
                que_id = GET_UINT16(p);
                if (que_id < oldest) {
                    eval = que_id + GET_LEARN_MAX_WORD_COUNT((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]);
                } else {
                    eval = que_id;
                }
                if (eval >= max_value) {
                    /*
                     * あいまい検索有効時
                     *   初回時    ：入力文字列に近いキャッシュから頻度最大値を無条件で取得。
                     *   ２回目    ：全キャッシュから頻度最大値を無条件で取得。
                     *   ３回目以降：全キャッシュからcurrentより小さい最大値を取得。
                     * あいまい検索無効時
                     *   初回時    ：頻度最大値を無条件で取得。
                     *   ２回目以降：currentより小さい最大値を取得。
                     */
                    if ((GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT)
                        || ((GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_READY)
                            && (NJ_GET_AIMAI_FROM_SCACHE(psrhCache)))
                        || (eval < current)) {

                        /* QueIdから文字列を取得する */
                        /* Queのエラーチェックのため、ダミーで文字列取得処理を行う */
                        str = degiji_get_string(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id, &slen);
                        if (str == NULL) {
                            return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
                        }

                        /* 前方一致検索     */
                        /* 品詞チェック */
                        que = degiji_get_que_allHinsi(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
                        if (que == NULL) {
                            return NJ_SET_ERR_VAL(NJ_FUNC_IS_CONTINUED, NJ_ERR_DIC_BROKEN);
                        }
                        if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                            /* 複数キューは見ていないのでcurrent_infoは固定     */
                            loctset->loct.current_info = (NJ_UINT8)0x10;
                            loctset->loct.current = i;
                            max_value = eval;
                            found = 1;
                        }
                    }
                }
                p += NJ_INDEX_SIZE;
            }
        }
    }

    if (GET_LOCATION_STATUS(loctset->loct.status) != NJ_ST_SEARCH_NO_INIT) {
        NJ_UNSET_AIMAI_TO_SCACHE(psrhCache);
    }

    /* 対象はなかった */
    if (found == 0) {
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    } else {
        loctset->loct.status = NJ_ST_SEARCH_READY;
        loctset->cache_freq = degiji_get_hindo(iwnn, loctset, search_pattern);
        return 1;
    }
}

/**
 *      検索条件により指定された読みと一致する範囲を2分検索する。
 *
 * @param[in]          iwnn : 解析情報クラス
 * @param[in]        handle : 辞書ハンドル
 * @param[in]            op : 検索方法
 * @param[in]          yomi : 読み文字列
 * @param[in]           len : 検索対象の読み文字配列長
 * @param[in]         sfrom : 検索範囲先頭
 * @param[in]           sto : 検索範囲末尾
 * @param[out]         from : 検索範囲先頭
 * @param[out]           to : 検索範囲末尾
 * @param[out] forward_flag : 前方一致フラグ（0:前方一致候補なし、1:前方一致候補あり）
 *
 * @retval               <0   エラー
 * @retval                0   見付からなかった
 * @retval                1   見付かった
 */
static NJ_INT16 degiji_search_range_by_yomi2(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 op,
                                             NJ_CHAR  *yomi, NJ_UINT16 len,
                                             NJ_UINT16 sfrom, NJ_UINT16 sto,
                                             NJ_UINT16 *from, NJ_UINT16 *to,
                                             NJ_UINT8 *forward_flag) {
    NJ_UINT16 right, mid = 0, left, max;        /* バイナリサーチ用 */
    NJ_UINT16 que_id;
    NJ_UINT8  *ptr, *p;
    NJ_CHAR  *str;
    NJ_INT16 ret = 0;
    NJ_INT32 found = 0;
    NJ_UINT8 slen;
    NJ_INT32 cmp;

    /* 検索するインデックスの先頭アドレスを取得 */
    ptr = degiji_get_search_index_address(handle, op);
    /* 学習領域に登録されている語彙数を取得する */
    max = GET_LEARN_WORD_COUNT(handle);

    right = sto;
    left = sfrom;


    *forward_flag = 0;

    while (left <= right) {
        mid = left + ((right - left) / 2);
        p = ptr + (mid * NJ_INDEX_SIZE);
        que_id = GET_UINT16(p);
        str = degiji_get_string(iwnn, handle, que_id, &slen);
        if (str == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
        }

        /* キューに登録されている文字列で考える */
        ret = nj_strncmp(yomi, str, len);

        if (ret == 0) {
            /* 先頭はこの場所より左にある */
            found = 1;
            break;
        } else if (ret < 0) {
            /* この場所より左にある */
            right = mid - 1;
            if (mid == 0) {
                break;
            }
        } else {
            /* この場所より右にある */
            left = mid + 1;
        }
    }

    if (!found) {
        return 0;
    }

    if (mid == 0) {
        *from = mid;
    } else {
        /* midの一つ前から読み順インデックスを線形検索 */
        p = ((mid - 1) * NJ_INDEX_SIZE) + ptr;
        /* leftの代わりにNJ_INT32のcmpを使っている      */
        for (cmp = mid - 1; cmp >= 0; cmp--) {
            que_id = GET_UINT16(p);
            str = degiji_get_string(iwnn, handle, que_id, &slen);
            if (str == NULL) {
                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
            }

            /* 前方一致時の処理 */
            if (nj_strncmp(yomi, str, len) != 0) {
                break;      /* forループから抜ける */
            }
            p -= NJ_INDEX_SIZE;
        }
        if (cmp < 0) {
            *from = 0;
        } else {
            *from = (NJ_UINT16)cmp + 1;
        }
    }


    if ((mid + 1) >= max) {
        *to = mid;
    } else {
        /* index中の位置をpに入れる */
        p = ((mid + 1) * NJ_INDEX_SIZE) + ptr;
        /* 線形検索で一致しなくなるまで探す */
        for (right = mid + 1; right < max; right++) {
            que_id = GET_UINT16(p);
            str = degiji_get_string(iwnn, handle, que_id, &slen);
            if (str == NULL) {
                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
            }

            /* 前方一致時の処理 */
            if (nj_strncmp(yomi, str, len) != 0) {
                break;      /* forループから抜ける */
            }
            p += NJ_INDEX_SIZE;
        }
        *to = right - 1;
    }

    return 1;
}

/**
 * 前方一致検索、繋がり検索の頻度順(状況適応予測)検索を行う
 *
 * @param[in] iwnn           解析情報クラス
 * @param[in] cond           検索条件
 * @param[in,out] loctset    検索位置
 * @param[in] search_pattern 検索方法
 *
 * @retval 1   候補あり
 * @retval 0   候補なし
 * @retval <0  エラー
 */
static NJ_INT16 degiji_get_cand_by_evaluate_state(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond,
                                                  NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern) {
    NJ_UINT16 from, to, i, j;
    NJ_UINT16 que_id;
    NJ_UINT32 current;
    NJ_UINT8  *ptr, *p;
    NJ_WQUE   *que;
    NJ_INT16  ret;
    NJ_INT16  found = 0;
    NJ_UINT8  forward_flag = 0;
    NJ_UINT8  *ext_ptr;
    NJ_INT32  hindo_max = INIT_HINDO;
    NJ_INT32  hindo_tmp;
    NJ_UINT8  current_info = 0x10;

    /* 検索するインデックスの先頭アドレスを取得 */
    ptr = degiji_get_search_index_address((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], cond->operation);
    if (ptr == NULL) {
        /* 指定された方法で検索できない場合、"候補なし" を返す 08.02.03 */
        return 0; /*NCH_FB*/
    }

    /* LOCATIONの現在状態の判別 */
    current = 0;
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        /* 前方一致検索 */
        ret = degiji_search_range_by_yomi(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], search_pattern,
                                   cond->yomi, cond->ylen, &from, &to, &forward_flag);
        if (ret <= 0) {
            loctset->loct.status = NJ_ST_SEARCH_END;
            return ret;
        }
        loctset->loct.top = from;
        loctset->loct.bottom = to;
        /* 辞書内の最大頻度の候補を算出 */
        for (i = from; i <= to; i++) {
            /* QueueIDを取得 */
            p = ptr + (i * NJ_INDEX_SIZE);
            que_id = GET_UINT16(p);
            /* 一致した場合のみ頻度を取得し、最高頻度を算出する。*/
            /* 単語頻度を取得*/
            hindo_tmp = degiji_get_attr_hindo(iwnn, loctset, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);

            if (hindo_tmp > hindo_max) {
                /*
                 * 品詞チェック
                 * str_que_cmpが
                 * 成功しているのでここではエラーは返らない
                 */
                que = degiji_get_que_allHinsi(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
                if (que == NULL) {
                    return NJ_SET_ERR_VAL(NJ_FUNC_IS_CONTINUED, NJ_ERR_DIC_BROKEN);
                }
                if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                    found = 1;
                    hindo_max = hindo_tmp;
                    current   = i;
                    current_info = 0x10;
                }
            }
        }
        /* 現在位置を設定 */
        loctset->loct.current = current;

    } else if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_READY) {
        /* 次候補検索を行う */
        current_info = loctset->loct.current_info;
        found = degiji_get_next_data(iwnn, cond, loctset, search_pattern, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT],
                                    ptr, &(loctset->loct.current),
                                    loctset->loct.top, loctset->loct.bottom,
                                    &hindo_max, &(current_info));
    }

    /* 対象はなかった */
    if (found == 0) {
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    loctset->loct.current_info = current_info;

    /* QUE_IDの算出処理を行う */
    p = ptr + ((loctset->loct.current) * NJ_INDEX_SIZE);
    que_id = GET_UINT16(p);
    ret = (current_info & 0x0f);
    for (j = 0; j < ret; j++) {
        /*
         * ここで指しているque_idは、繋がり元のque_idなので
         * 返す候補のque_idまで進める
         */
        que_id = degiji_search_next_que((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
    }

    ext_ptr = POS_TO_EXT_ADDRESS((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
    STATE_COPY((NJ_UINT8*)&loctset->loct.attr, ext_ptr);

    loctset->loct.status = NJ_ST_SEARCH_READY;
    loctset->cache_freq  = degiji_calculate_hindo((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], hindo_max, &(loctset->dic_freq), loctset->dic_freq_max, loctset->dic_freq_min);
    return 1;
}

/**
 * 前方一致検索、頻度順の検索を行う(キャッシュ検索用)
 *
 * @param[in] iwnn           解析情報クラス
 * @param[in] cond           検索条件
 * @param[in,out] loctset    検索位置
 * @param[in] search_pattern 検索方法
 * @param[in] idx            辞書のインデックス
 *
 * @retval 1   候補あり
 * @retval 0   候補なし
 * @retval <0  エラー
 */
static NJ_INT16 degiji_get_cand_by_evaluate2_state(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond,
                                                   NJ_SEARCH_LOCATION_SET *loctset,
                                                   NJ_UINT8 search_pattern,
                                                   NJ_UINT16 idx) {
    NJ_UINT16 from, to, i;
    NJ_UINT16 que_id;
    NJ_UINT32 current;
    NJ_UINT8  *ptr, *p;
    NJ_WQUE *que;
    NJ_INT16 ret = 0;
    NJ_INT32 found = 0;
    NJ_UINT8 forward_flag = 0;

    /* 新検索 */
    NJ_UINT16       abIdx;
    NJ_UINT16       abIdx_old;
    NJ_UINT16       tmp_len;
    NJ_UINT16       yomi_len;
    NJ_UINT16       j,l,m;
    NJ_UINT8        cmpflg;
    NJ_UINT8        endflg = 0;
    NJ_CHAR         *str;
    NJ_CHAR         *key;
    NJ_CHAR         char_tmp[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_CHAR         *pchar_tmp;
    NJ_SEARCH_CACHE *psrhCache = cond->ds->dic[idx].srhCache;
    NJ_UINT16       endIdx;
    NJ_UINT8        slen;
    NJ_UINT16       addcnt = 0;
    NJ_CHAR         *yomi;
    NJ_UINT8        aimai_flg = 0x01;
    NJ_CHARSET      *pCharset = cond->charset;
    NJ_UINT8        *ext_ptr;
    NJ_INT32        hindo_max = INIT_HINDO;
    NJ_INT32        hindo_tmp;
    NJ_INT32        hindo_current;
    NJ_UINT16       current_abIdx = 0;
    NJ_UINT16       old_cache_idx;
    NJ_INT32        save_hindo;
    NJ_UINT16       save_idx;
    NJ_UINT8        current_info = 0x10; /* 複合文節でない */
#ifdef NJ_OPT_CHARSET_2BYTE
    NJ_UINT16 start;
    NJ_UINT16 end;
#endif /* NJ_OPT_CHARSET_2BYTE */

    if (NJ_GET_CACHEOVER_FROM_SCACHE(psrhCache)) {
        aimai_flg = 0x00;
    }

    /* 検索するインデックスの先頭アドレスを取得 */
    ptr = degiji_get_search_index_address((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], cond->operation);
    if (ptr == NULL) {
        /* 指定された方法で検索できない場合、"候補なし" を返す 08.02.03 */
        return 0; /*NCH_FB*/
    }

    /* LOCATIONの現在状態の判別 */
    current = 0;
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        /* 複数キューは対象としない */
        /* 読み文字列の状態を調べる */
        key      = cond->ds->keyword;
        yomi     = cond->yomi;
        yomi_len = cond->yclen;

        /* 読み文字列の比較チェック */
        endflg = 0x00;

        /* キャッシュエリアの先頭が0xFFFFなら、
         * cmpflgに1をセットする(キャッシュを作成し直す)
         */
        if (psrhCache->keyPtr[0] == 0xFFFF) {
            cmpflg = 0x01;
            psrhCache->keyPtr[0] = 0x0000;
        } else {
            cmpflg = 0x00;
        }

        if (yomi_len >= NJ_MAX_KEYWORD) {
            return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_YOMI_TOO_LONG);
        }

        for (i = 0; i < yomi_len; i++) {
            j = i;

            /* インデックス情報の構築。 */
            if (!cmpflg) { /* cmpflg == 0x00 */
                /* 一文字分を比較してOKだった。 */
                if (((j != 0) && (psrhCache->keyPtr[j] == 0)) || (psrhCache->keyPtr[j+1] == 0)) {
                    /* インデックス情報がない */
                    cmpflg = 0x01;
                } else {
                    /* インデックス情報あり */
                }
            }

            if (cmpflg) { /* cmpflg == 0x01 */
                /* インデックス情報なしと判定された場合 */
                if (!j) { /* j == 0 */
                    /* 初回のインデックス情報を作成する。 */
                    abIdx = 0;
                    addcnt = 0;
                    nj_charncpy(char_tmp, yomi, 1);
                    tmp_len = nj_strlen(char_tmp);
                    ret = degiji_search_range_by_yomi(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], search_pattern,
                                               char_tmp, tmp_len, &from,
                                               &to, &forward_flag);
                    if (ret < 0) {
                        /* 異常発生 */
                        /* 現在位置を次文字の書き込み位置に格納する。 */
                        psrhCache->keyPtr[j+1] = abIdx; /*NCH_FB*/
                        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
                        return ret; /*NCH_FB*/
                    } else if (ret > 0) {
                        /* 存在した場合。 */
                        psrhCache->storebuff[abIdx].top    = from;
                        psrhCache->storebuff[abIdx].bottom = to;
                        psrhCache->storebuff[abIdx].idx_no = (NJ_INT8)tmp_len;
                        addcnt++;
                        abIdx++;
                        psrhCache->keyPtr[j+1] = abIdx;
                    } else {
                        psrhCache->keyPtr[j+1] = abIdx;
                    }

                    if ((!endflg) && (pCharset != NULL) && aimai_flg) {
                        /* 曖昧検索処理 */
#ifdef NJ_OPT_CHARSET_2BYTE
                        /* keyに一致するあいまい文字セットの範囲を２分検索する */
                        if (njd_search_charset_range(pCharset, key, &start, &end) == 1) {
                            /* 範囲が見つかった */
                            for (l = start; l <= end; l++) {
#else /* NJ_OPT_CHARSET_2BYTE */
                        for (l = 0; l < pCharset->charset_count; l++) {
                            /* 曖昧検索分の検出 */
                            if (nj_charncmp(yomi, pCharset->from[l], 1) == 0) {
#endif /* NJ_OPT_CHARSET_2BYTE */
                                /* 一致しているなら */
                                nj_strcpy(char_tmp, pCharset->to[l]);
                                tmp_len = nj_strlen(char_tmp);
                                ret = degiji_search_range_by_yomi(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], search_pattern,
                                                           char_tmp, tmp_len, &from, &to, &forward_flag);
                                if (ret < 0) {
                                    /* 異常発生 */
                                    /* 現在位置を次文字の書き込み位置に格納する。 */
                                    psrhCache->keyPtr[j+1] = abIdx; /*NCH_FB*/
                                    loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
                                    return ret; /*NCH_FB*/
                                } else if (ret > 0) {
                                    /* 存在した場合。 */
                                    /* キャッシュ溢れ発生 */
                                    if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                        psrhCache->keyPtr[j+1] = 0;
                                        return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_NOT_ENOUGH);
                                    }
                                    psrhCache->storebuff[abIdx].top    = from;
                                    psrhCache->storebuff[abIdx].bottom = to;
                                    psrhCache->storebuff[abIdx].idx_no = (NJ_INT8)tmp_len;
                                    if (addcnt == 0) {
                                        psrhCache->keyPtr[j] = abIdx;
                                    }
                                    abIdx++;
                                    addcnt++;
                                    psrhCache->keyPtr[j+1] = abIdx;
                                } else {
                                    psrhCache->keyPtr[j+1] = abIdx;
                                }
                            } /* else {} */
                        } /* for */
                    } /* if */
                } else {
                    /* 現在のインデックス情報を取得する */
                    if (psrhCache->keyPtr[j] == psrhCache->keyPtr[j-1]) {
                        /* 検索結果が存在しない */
                        psrhCache->keyPtr[j+1] = psrhCache->keyPtr[j-1];
                        endflg = 0x01;
                    } else {
                        /* インデックス有効数を取得 */
                        endIdx = psrhCache->keyPtr[j];
                        abIdx_old = psrhCache->keyPtr[j-1];

                        if (NJ_GET_CACHEOVER_FROM_SCACHE(psrhCache)) {
                            abIdx = psrhCache->keyPtr[j - 1];
                            psrhCache->keyPtr[j] = abIdx;
                        } else {
                            abIdx = psrhCache->keyPtr[j];
                        }
                        addcnt = 0;

                        if ((abIdx > NJ_SEARCH_CACHE_SIZE) || (abIdx_old >= NJ_SEARCH_CACHE_SIZE) ||
                            (endIdx > NJ_SEARCH_CACHE_SIZE)) {
                            /* キャッシュが破壊されている */
                            return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_BROKEN); /*NCH_FB*/
                        }
                        for (m = abIdx_old; m < endIdx; m++) {
                            /* QueIdを取得する */
                            p = ptr + (psrhCache->storebuff[m].top * NJ_INDEX_SIZE);
                            que_id = GET_UINT16(p);

                            /* QueIdから文字列を取得する */
                            str = degiji_get_string(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id, &slen);

                            if (str == NULL) {
                                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
                            }

                            /* 必要読み文字列数分取得する */
                            nj_strncpy(char_tmp, str, psrhCache->storebuff[m].idx_no);
                            char_tmp[psrhCache->storebuff[m].idx_no] = NJ_CHAR_NUL;

                            pchar_tmp = &char_tmp[psrhCache->storebuff[m].idx_no];
                            nj_charncpy(pchar_tmp, yomi, 1);
                            tmp_len = nj_strlen(char_tmp);


                            ret = degiji_search_range_by_yomi2(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], search_pattern,
                                                        char_tmp, tmp_len,
                                                        (NJ_UINT16)(psrhCache->storebuff[m].top),
                                                        (NJ_UINT16)(psrhCache->storebuff[m].bottom),
                                                        &from, &to, &forward_flag);
                            if (ret < 0) {
                                /* 異常発生 */
                                /* 現在位置を次文字の書き込み位置に格納する。 */
                                psrhCache->keyPtr[j+1] = abIdx; /*NCH_FB*/
                                loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
                                return ret; /*NCH_FB*/
                            } else if (ret > 0) {
                                /* 存在した場合。 */
                                /* キャッシュ溢れ発生 */
                                if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                    psrhCache->keyPtr[j+1] = 0;
                                    return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_NOT_ENOUGH);
                                }
                                psrhCache->storebuff[abIdx].top    = from;
                                psrhCache->storebuff[abIdx].bottom = to;
                                psrhCache->storebuff[abIdx].idx_no = (NJ_INT8)tmp_len;
                                if (addcnt == 0) {
                                    psrhCache->keyPtr[j] = abIdx;
                                }
                                abIdx++;
                                addcnt++;
                                psrhCache->keyPtr[j+1] = abIdx;
                            } else {
                                psrhCache->keyPtr[j+1] = abIdx;
                            }

                            if ((!endflg) && (pCharset != NULL) && aimai_flg) {
                                /* 曖昧検索処理 */
#ifdef NJ_OPT_CHARSET_2BYTE
                                /* keyに一致するあいまい文字セットの範囲を２分検索する */
                                if (njd_search_charset_range(pCharset, key, &start, &end) == 1) {
                                    /* 範囲が見つかった */
                                    for (l = start; l <= end; l++) {
#else /* NJ_OPT_CHARSET_2BYTE */
                                for (l = 0; l < pCharset->charset_count; l++) {
                                    /* 曖昧検索分の検出 */
                                    if (nj_charncmp(yomi, pCharset->from[l], 1) == 0) {
#endif /* NJ_OPT_CHARSET_2BYTE */
                                        /* 一致しているなら */
                                        tmp_len = nj_strlen(pCharset->to[l]);

                                        nj_strncpy(pchar_tmp, pCharset->to[l], tmp_len);
                                        *(pchar_tmp + tmp_len) = NJ_CHAR_NUL;
                                        tmp_len = nj_strlen(char_tmp);
                                        ret = degiji_search_range_by_yomi2(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], search_pattern,
                                                                    char_tmp, tmp_len,
                                                                    (NJ_UINT16)(psrhCache->storebuff[m].top),
                                                                    (NJ_UINT16)(psrhCache->storebuff[m].bottom),
                                                                    &from, &to, &forward_flag);
                                        if (ret < 0) {
                                            /* 異常発生 */
                                            /* 現在位置を次文字の書き込み位置に格納する。 */
                                            psrhCache->keyPtr[j+1] = abIdx; /*NCH_FB*/
                                            loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
                                            return ret; /*NCH_FB*/
                                        } else if (ret > 0) {
                                            /* 存在した場合。 */
                                            /* キャッシュ溢れ発生 */
                                            if (abIdx >= NJ_SEARCH_CACHE_SIZE) {
                                                psrhCache->keyPtr[j+1] = 0;
                                                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_NOT_ENOUGH);
                                            }
                                            psrhCache->storebuff[abIdx].top    = from;
                                            psrhCache->storebuff[abIdx].bottom = to;
                                            psrhCache->storebuff[abIdx].idx_no = (NJ_INT8)tmp_len;
                                            abIdx++;
                                            addcnt++;
                                            psrhCache->keyPtr[j+1] = abIdx;
                                        } else {
                                            psrhCache->keyPtr[j+1] = abIdx;
                                        }
                                    } /* else {} */
                                } /* for */
                            } /* if */
                        } /* for */
                    } /* if */
                }
            }
            yomi += UTL_CHAR(yomi);
            key  += UTL_CHAR(key);
        }

        /* 今回の検索結果件数が0かつキャッシュに候補がない（検索対象読み文字列長-1の開始位置と終了位置が同じ）場合に終了 */
        if ((addcnt == 0) && (psrhCache->keyPtr[yomi_len - 1] == psrhCache->keyPtr[yomi_len])) {
            endflg = 0x01;
        }

        if (endflg) {           /* endflg == 0x01 */
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }

    } else if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_READY) {
        if (NJ_GET_AIMAI_FROM_SCACHE(psrhCache)) {
            /*
             * 曖昧検索状態フラグをアンセットする。
             * あいまい度合い順→頻度順へ遷移
             * 現在位置は、現在のままにしておく。
             */
            NJ_UNSET_AIMAI_TO_SCACHE(psrhCache);
        } else {
            old_cache_idx = loctset->loct.current_cache;
            /* 次候補検索を行う */
            found = degiji_get_next_data(iwnn, cond, loctset, search_pattern, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], ptr,
                                        &(psrhCache->storebuff[old_cache_idx].current),
                                        psrhCache->storebuff[old_cache_idx].top,
                                        psrhCache->storebuff[old_cache_idx].bottom,
                                        &hindo_max, &current_info);
        }
    } else {
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
        return 0; /*NCH_FB*/
    }


    j = cond->yclen - 1;

    abIdx     = psrhCache->keyPtr[j];
    abIdx_old = psrhCache->keyPtr[j+1];
    /* インデックス有効数を取得 */
    endIdx    = abIdx_old;
    if ((abIdx >= NJ_SEARCH_CACHE_SIZE) || (abIdx_old > NJ_SEARCH_CACHE_SIZE)) {
        /* キャッシュが破壊されている */
        return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_BROKEN); /*NCH_FB*/
    }

    if (psrhCache->keyPtr[j] < psrhCache->keyPtr[j + 1]) {
        if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
            /*
             * 初回検索時は、検索キャッシュ毎に最高頻度を算出を行う。
             * 算出を行った上で、入力文字列に近い検索キャッシュを使うようにする。
             */
            save_hindo = INIT_HINDO;
            save_idx   = 0;
            for (m = abIdx; m < endIdx; m++) {
                /* 検索キャッシュ毎に最高頻度を求める */
                from      = (NJ_UINT16)psrhCache->storebuff[m].top;
                to        = (NJ_UINT16)psrhCache->storebuff[m].bottom;
                hindo_max = INIT_HINDO;
                found     = 0;

                /* 辞書内の最大頻度の候補を算出 */
                for (i = from; i <= to; i++) {

                    /* QueueIDを取得 */
                    p = ptr + (i * NJ_INDEX_SIZE);
                    que_id = GET_UINT16(p);

                    /* 単語頻度を取得*/
                    hindo_tmp = degiji_get_attr_hindo(iwnn, loctset, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);

                    if (hindo_tmp > hindo_max) {
                        /* 品詞チェック */
                        /*
                         * str_que_cmp or que_strcmp_complete_with_hyoukiが
                         * 成功しているのでここではエラーは返らない
                         */
                        que = degiji_get_que_allHinsi(iwnn, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
                        if (que == NULL) {
                            return NJ_SET_ERR_VAL(NJ_FUNC_IS_CONTINUED, NJ_ERR_DIC_BROKEN);
                        }
                        if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                            found      = 1;
                            hindo_max  = hindo_tmp;
                            psrhCache->storebuff[m].current = i;
                        }
                    }
                }
                if (found == 0) {
                    /* 対象はなかった */
                    psrhCache->storebuff[m].current = LOC_CURRENT_NO_ENTRY; /*NCH_FB*/
                }
                if (save_hindo == INIT_HINDO) {
                    /* 入力文字列に近いキャッシュ位置と頻度を記憶する。*/
                    save_idx   = m;
                    save_hindo = hindo_max;
                }
            }

            /* 初回検索時は、入力文字列に近いキャッシュから頻度最大値を無条件で取得 */
            if (save_hindo == INIT_HINDO) {
                loctset->loct.status = NJ_ST_SEARCH_END; /*NCH*/
                return 0; /*NCH*/
            }

            /* 入力文字列に近い有効な検索キャッシュのデータをセットする */
            current       = psrhCache->storebuff[save_idx].current;
            current_abIdx = save_idx;
            hindo_max     = save_hindo;
            found         = 1;
            /* 検索候補有り状態の場合は、曖昧キャッシュフラグを設定する */
            NJ_SET_AIMAI_TO_SCACHE(psrhCache);
        } else {
            /*
             * 次候補検索処理、頻度順に候補を返す
             */
            found         = 0;
            hindo_max     = INIT_HINDO;
            current_abIdx = 0;

            for (m = abIdx; m < endIdx; m++) {
                if (psrhCache->storebuff[m].current == LOC_CURRENT_NO_ENTRY) {
                    /* 検索終了の検索キャッシュの場合は、参照しない */
                    continue;
                }
                i = (NJ_UINT16)psrhCache->storebuff[m].current;
                p = ptr + (i * NJ_INDEX_SIZE);
                que_id = GET_UINT16(p);
                hindo_current = degiji_get_attr_hindo(iwnn, loctset, (NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);

                if (hindo_current > hindo_max) {
                    /* 最高の頻度の算出を行う */
                    hindo_max     = hindo_current;
                    current_abIdx = m;
                    current       = i;
                    found         = 1;
                }
            }
        }

    }

    /* 対象はなかった */
    if (found == 0) {
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    loctset->loct.current_info = (NJ_UINT8)0x10;
    loctset->loct.current = current;

    /* QUE_IDの算出処理を行う */
    p = ptr + (current * NJ_INDEX_SIZE);
    que_id = GET_UINT16(p);
    ext_ptr = POS_TO_EXT_ADDRESS((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
    STATE_COPY((NJ_UINT8*)&loctset->loct.attr, ext_ptr);
    loctset->loct.current_cache = current_abIdx;
    loctset->loct.status = NJ_ST_SEARCH_READY;
    loctset->cache_freq  = degiji_calculate_hindo((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], hindo_max, &(loctset->dic_freq), loctset->dic_freq_max, loctset->dic_freq_min);
    return 1;
}

/**
 * 単語の頻度処理
 *
 * @attention Version3の辞書のみ対応
 *
 * @param[in]  iwnn           解析情報クラス
 * @param[in]  loctset        検索位置
 * @param[in]  handle         辞書ハンドル
 * @param[in]  que_id         キューID
 *
 * @retval 単語頻度
 */
static NJ_INT32 degiji_get_attr_hindo(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset, NJ_DIC_HANDLE handle, NJ_UINT16 que_id) {
    NJ_UINT16 oldest;
    NJ_INT32  ret_hindo;
    NJ_UINT16 max;
    NJ_UINT8 *ext_ptr;
    /* 属性データ用 */
    NJ_UINT32 attr_data;
    NJ_INT32  attr_bias;

    oldest = GET_LEARN_NEXT_WORD_POS(handle);

    max = GET_LEARN_MAX_WORD_COUNT(handle);

    if (que_id >= oldest) {
        ret_hindo = que_id - oldest;
    } else {
        ret_hindo = que_id - oldest + max;
    }
    ext_ptr = POS_TO_EXT_ADDRESS(handle, que_id);

    STATE_COPY((NJ_UINT8*)&attr_data , ext_ptr);

    if ((attr_data != 0x00000000) && (max > 0)) {
        /* 属性付き単語の場合、状況に応じて頻度加算・減算する */
        attr_bias = njd_get_attr_bias(iwnn, attr_data);
        ret_hindo += CALCULATE_ATTR_HINDO32(attr_bias,
                                            loctset->dic_freq.base,
                                            loctset->dic_freq.high,
                                            max);
    }

    return ret_hindo;
}

/**
 * 正引き前方一致検索、繋がり検索時の次候補検索処理(状況適応対応版)
 *
 * @param[in,out]       iwnn  解析情報クラス
 * @param[in]           cond  検索条件
 * @param[in]        loctset  検索位置
 * @param[in] search_pattern  検索方法
 * @param[in]         handle  辞書ハンドル
 * @param[in]            ptr  インデックス先頭アドレス
 * @param[in,out]    current  現在位置
 * @param[in]            top  検索範囲-開始位置-
 * @param[in]         bottom  検索範囲-終了位置-
 * @param[out]     ret_hindo  次候補の頻度値
 * @param[out]  current_info  結合文節数
 *
 * @retval              1   候補有
 * @retval              0   候補無
 */
static NJ_INT16 degiji_get_next_data(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond,
                                     NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern,
                                     NJ_DIC_HANDLE handle, NJ_UINT8 *ptr,
                                     NJ_UINT32 *current, NJ_UINT32 top, NJ_UINT32 bottom,
                                     NJ_INT32 *ret_hindo, NJ_UINT8 *current_info) {
    NJ_UINT32 i;
    NJ_UINT16 que_id;
    NJ_UINT32 bottomflg;
    NJ_WQUE   *que;
    NJ_INT32  current_hindo;
    NJ_INT32  tmp_hindo;
    NJ_INT32  max_hindo     = INIT_HINDO;
    NJ_INT32  max_hindo_idx = 0x7FFFFFFF; /* INT32最大値 */
    NJ_UINT32 found = 0;
    NJ_UINT8  *p;
    NJ_INT16  ret, j;

    if (*current == LOC_CURRENT_NO_ENTRY) {
        /* 検索終了状態の場合は、候補無しとする */
        return 0; /*NCH_FB*/
    }

    /* 現在の頻度を取得する */
    i      = *current;
    p      = ptr + (i * NJ_INDEX_SIZE);
    que_id = GET_UINT16(p);
    ret    = (*current_info & 0x0f);
    for (j = 0; j < ret; j++) {
        /*
         * ここで指しているque_idは、繋がり元のque_idなので
         * 返す候補のque_idまで進める
         */
        que_id = degiji_search_next_que((NJ_DIC_HANDLE)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT], que_id);
    }
    current_hindo = degiji_get_attr_hindo(iwnn, loctset, handle, que_id);
    if (i >= bottom) {
        /* 終端まで検索している場合は、topから検索を行う */
        bottomflg = 1;
        i = top;
    } else {
        bottomflg = 0;
        i++;
    }

    /* 結合文節数は常に0x10で初期化する */
    *current_info = 0x10;

    /* 次候補の算出を行う */
    while (i <= bottom) {
        que_id = GET_UINT16(ptr + (i * NJ_INDEX_SIZE));

        /* 前方一致検索 */
        /* 単語頻度を取得*/
        tmp_hindo = degiji_get_attr_hindo(iwnn, loctset, handle, que_id);

        if ((i > *current) && (tmp_hindo == current_hindo)) {
            /*
             * 検索中頻度と等しく、且つ検索候補がカレント位置以降の場合,
             * その検索候補位置を返す
             * 品詞チェック
             * str_que_cmpが
             * 成功しているのでここではエラーは返らない
             */
            que = degiji_get_que_allHinsi(iwnn, handle, que_id);
            if (que == NULL) {
                return NJ_SET_ERR_VAL(NJ_FUNC_IS_CONTINUED, NJ_ERR_DIC_BROKEN);
            }
            if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                max_hindo       = tmp_hindo;
                max_hindo_idx   = i;
                found           = 1;
            }
            break;
        } else if (tmp_hindo < current_hindo) {
            /* カレントより頻度が低く、その中で最高頻度のものを取得 */
            /* 検索中の最高頻度と同じで、読みが先のものを取得 */
            if ((max_hindo < tmp_hindo) || (((NJ_UINT32)max_hindo_idx > i) && (max_hindo == tmp_hindo))) {
                /*
                 * 品詞チェック
                 * str_que_cmpが
                 * 成功しているのでここではエラーは返らない
                 */
                que = degiji_get_que_allHinsi(iwnn, handle, que_id);
                if (que == NULL) {
                    return NJ_SET_ERR_VAL(NJ_FUNC_IS_CONTINUED, NJ_ERR_DIC_BROKEN);
                }
                if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                    max_hindo       = tmp_hindo;
                    max_hindo_idx   = i;
                    found           = 1;
                }
            }
        }

        i++;
        if ((i > bottom) && (bottomflg == 0)) {
            /*
             * 範囲の終端まで検索したと判定し、
             * currentを先頭まで、bottomをcurrent - 1までに置き換えて
             * 検索処理を続ける
             */
            bottomflg = 1;
            i = top;
            if (*current == top) {
                /* currentが先頭のため、これ以上候補を検索することができないため、抜ける */
                break;
            } else {
                bottom = *current - 1;
            }
        }
    }

    if (found == 0) {
        /* 対象はなかった */
        *current = LOC_CURRENT_NO_ENTRY;
        return 0;
    }

    *current   = max_hindo_idx;
    *ret_hindo = max_hindo;
    return 1;
}

/**
 * 辞書領域生成API
 *
 * @param[in]  iwnn     解析情報クラス
 * @param[out] handle   辞書ハンドル
 * @param[in]  size     領域サイズ
 *
 * @retval 0  正常終了
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 createEmojiDictionary(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT32 size) {
    NJ_UINT32   cnt;
    NJ_UINT32   data_size;
    NJ_UINT8    *p;

    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, NJ_ERR_PARAM_ENV_NULL);;
    }
    if (handle == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, NJ_ERR_DIC_HANDLE_NULL);;
    }

    /* 辞書種別毎に最小サイズをチェックし、格納可能な単語数を計算する */
    if (size < MIN_SIZE_OF_EMOJI_DIC) {
        return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, NJ_ERR_AREASIZE_INVALID);
    }
    cnt = GET_MAX_WORD_NUM_IN_EMOJI_DIC(size);

    /* キューの数が32kを超えたらエラー  */
    if (cnt > 0x7FFF) {
        return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, NJ_ERR_AREASIZE_INVALID);
    }
    /* 読み順インデックスは１個余分に必要、データサイズには     */
    /* 末尾識別子分が含まれる                                   */
    /* 渡されたサイズからヘッダーサイズを引いた値を設定する     */
    data_size = size - NJ_LEARN_DIC_HEADER_SIZE;

    /**
     * 辞書データ作成開始
     * 辞書作成が完了するまでは、電断対応のため、識別子を壊しておく。
     */
    p = (NJ_UINT8 *)handle;

    /* --- 共通データ --- */
    NJ_INT32_WRITE(p, 0L);
    p += sizeof(NJ_INT32);

    /* 辞書バージョン */
    NJ_INT32_WRITE(p, NJ_DIC_VERSION2);
    p += sizeof(NJ_INT32);

    /* 辞書タイプ */
    NJ_INT32_WRITE(p, NJ_DIC_TYPE_USER);
    p += sizeof(NJ_INT32);

    /* 辞書データサイズ */
    NJ_INT32_WRITE(p, data_size);
    p += sizeof(NJ_INT32);

    /* 辞書内拡張情報サイズ */
    NJ_INT32_WRITE(p, NJ_DIC_UNCOMP_EXT_HEADER_SIZE);
    p += sizeof(NJ_INT32);

    /* 辞書内最大読み文字列バイト長、辞書内候補最大候補文字列バイト長 */
    NJ_INT32_WRITE(p, NJ_MAX_EMOJI_LEN*sizeof(NJ_CHAR));
    p += sizeof(NJ_INT32);
    NJ_INT32_WRITE(p, NJ_MAX_EMOJI_KOUHO_LEN*sizeof(NJ_CHAR));
    p += sizeof(NJ_INT32);

    /* --- 拡張情報 --- */
    /* リザーブ */
    NJ_INT32_WRITE(p, 0L);
    p += sizeof(NJ_INT32);

    /* データ領域オフセット */
    NJ_INT32_WRITE(p, GET_DATA_AREA_OFFSET(cnt));
    p += sizeof(NJ_INT32);

    /* 登録語数 */
    NJ_INT32_WRITE(p, 0L);
    p += sizeof(NJ_INT32);

    /* 最大単語登録数 */
    NJ_INT32_WRITE(p, cnt);
    p += sizeof(NJ_INT32);

    /* 語彙データキューサイズ */
    NJ_INT32_WRITE(p, NJ_EMOJI_QUE_SIZE);
    p += sizeof(NJ_INT32);

    /* 次キュー追加位置 */
    NJ_INT32_WRITE(p, 0L);
    p += sizeof(NJ_INT32);

    /* 電断用エリア */
    NJ_INT32_WRITE(p, 0L);
    p += sizeof(NJ_INT32);

    /* 付加情報領域のオフセットを 設定 */
    NJ_INT32_WRITE(p, 0L);
    p += sizeof(NJ_INT32);

    /* インデックス領域オフセット   */
    NJ_INT32_WRITE(p, NJ_LEARN_DIC_HEADER_SIZE);
    p += sizeof(NJ_INT32);

    /* 表記順文字列インデックス領域オフセット   */
    NJ_INT32_WRITE(p, GET_HYOKI_INDEX_OFFSET(cnt));
    p += sizeof(NJ_INT32);

    /* Ver.2 は空き、Ver.3およびVer.4は拡張単語情報領域 */
    NJ_INT32_WRITE(p, 0L);
    p += sizeof(NJ_INT32);

    /* 末尾の識別子をセットする                                 */
    /* データ領域に余りがあっても良い、渡されたサイズ分確保する */
    NJ_INT32_WRITE(handle + size - sizeof(NJ_UINT32), NJ_DIC_IDENTIFIER);
    /* 読み順インデックスをクリアし、キューを未使用状態にする   */
    njd_l_init_area(handle);

    /* 電断対応のため、処理終了時に識別子をセットする   */
    NJ_INT32_WRITE(handle, NJ_DIC_IDENTIFIER);  /* 識別子           */

    return 0;
}

/**
 * 辞書チェックAPI
 *
 * 指定された辞書のデータ整合性をチェックする。
 *
 * @param[in]      iwnn      解析情報クラス
 * @param[in,out]  handle    辞書ハンドル
 * @param[in]      size      領域サイズ
 *
 * @retval 0  正常終了(正常データ)
 * @retval <0 異常終了
 */
NJ_EXTERN NJ_INT16 checkEmojiDictionary(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT32 size) {
    NJ_UINT8 *addr;
    NJ_UINT32 datasize, extsize;
    NJ_UINT32 version;
    NJ_UINT32 type;

    if (iwnn == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, NJ_ERR_PARAM_ENV_NULL);
    }

    if (handle == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, NJ_ERR_DIC_HANDLE_NULL);
    }

    /* 少なくとも共通ヘッダー分ないと次のチェックで     */
    /* 範囲外アクセスの怖れがあるので先にチェック       */
    if (size <= NJ_DIC_COMMON_HEADER_SIZE) {
        return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, NJ_ERR_AREASIZE_INVALID);
    }

    /* 辞書全体のサイズチェック */
    /* 共通ヘッダーサイズ＋辞書データサイズ＋拡張情報サイズの合計と比較 */
    if (size != (NJ_DIC_COMMON_HEADER_SIZE
                 + NJ_INT32_READ(handle + NJ_DIC_POS_DATA_SIZE)
                 + NJ_INT32_READ(handle + NJ_DIC_POS_EXT_SIZE))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, NJ_ERR_AREASIZE_INVALID);
    }

    addr = handle;

    /* 識別子 */
    if (NJ_INT32_READ(addr) != NJ_DIC_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, NJ_ERR_FORMAT_INVALID);
    }
    addr += sizeof(NJ_UINT32);

    /* 辞書フォーマット形式 */
    version = NJ_INT32_READ(addr);
    if (version != NJ_DIC_VERSION2) {
        return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, NJ_ERR_FORMAT_INVALID);
    }
    addr += sizeof(NJ_UINT32);

    /* 辞書種別 */
    type = NJ_INT32_READ(addr);
    addr += sizeof(NJ_UINT32);

    /* 辞書データサイズ */
    datasize = NJ_INT32_READ(addr);
    addr += sizeof(NJ_UINT32);

    /* 拡張情報サイズ */
    extsize = NJ_INT32_READ(addr);
    addr += sizeof(NJ_UINT32);

    /* 辞書格納候補 最大読み長 */
    if (NJ_INT32_READ(addr) > (NJ_MAX_LEN * sizeof(NJ_CHAR))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, NJ_ERR_FORMAT_INVALID);
    }
    addr += sizeof(NJ_UINT32);

    /* 辞書格納候補 最大候補長 */
    if (NJ_INT32_READ(addr) > (NJ_MAX_RESULT_LEN * sizeof(NJ_CHAR))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, NJ_ERR_FORMAT_INVALID);
    }

    /* 後方の識別子チェック     */
    addr += (extsize + datasize);
    if (NJ_INT32_READ(addr) != NJ_DIC_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_PROGRAM_DIC_OPERATION, NJ_ERR_FORMAT_INVALID);
    }

    return degiji_check_dic(iwnn, handle, 0);
}