/**
 * @file
 *   辞書アダプタ： 学習辞書(非圧縮辞書)アダプタ
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
/*           define                             */
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

#define GET_CANDIDATE_BY_EVAL(iwnn, con, loctset, oper)                 \
    (((NJ_GET_DIC_VER((loctset)->loct.handle) == NJ_DIC_VERSION3) || (NJ_GET_DIC_VER((loctset)->loct.handle) == NJ_DIC_VERSION4)) ?      \
     get_cand_by_evaluate_state((iwnn), (con), (loctset), (oper)) :     \
     get_cand_by_evaluate((iwnn), (con), (loctset), (oper)))

#define GET_CANDIDATE_BY_EVAL2(iwnn, con, loctset, oper, idx)           \
    (((NJ_GET_DIC_VER((loctset)->loct.handle) == NJ_DIC_VERSION3) || (NJ_GET_DIC_VER((loctset)->loct.handle) == NJ_DIC_VERSION4)) ?      \
     get_cand_by_evaluate2_state((iwnn), (con), (loctset), (oper), (idx)) : \
     get_cand_by_evaluate2((iwnn), (con), (loctset), (oper), (idx)))

#define HAS_BUNTOU_ATTR(handle, que_id)                                 \
    ((POS_TO_EXT_ADDRESS((handle), (que_id)))[0] & (0x80U >> NJ_CAT_FIELD_HEAD))

#define IS_BUNTOU_LINK_SEARCH_MODE(iwnn)                                \
    ((iwnn)->environment.prev_hinsi ==                                  \
     njd_r_get_hinsi((iwnn)->dic_set.rHandle[NJ_MODE_TYPE_HENKAN], NJ_HINSI_BUNTOU_B))

#define USE_QUE_NUM(que_size, str_size)    \
    ( (((str_size) % ((que_size) - 1)) == 0)                           \
      ? ((str_size) / ((que_size) - 1))                                \
      : ((str_size) / ((que_size) - 1) + 1) )

#define NEXT_QUE(que, max)  ( ((que) < ((max) - 1)) ? ((que) + 1) : 0 )

#define PREV_QUE(que, max)  ( ((que) == 0) ? ((max) - 1) : ((que) - 1) )

#define COPY_QUE(handle, src, dst)                                      \
    nj_memcpy(POS_TO_ADDRESS((handle), (dst)), POS_TO_ADDRESS((handle), (src)), QUE_SIZE(handle))

#define COPY_QUE_EXT(handle, src, dst)                                  \
    nj_memcpy(POS_TO_EXT_ADDRESS((handle), (dst)), POS_TO_EXT_ADDRESS((handle), (src)), LEARN_DIC_EXT_QUE_SIZE)


/**
 * 候補無し時のloct.currentの値
 */
#define LOC_CURRENT_NO_ENTRY  0xffffffffU

#define IS_TARGET_COMPOUND_MODE(w, x, y, z)                                       \
    (((((w)->njc_mode == 0) &&                                                    \
        ((w)->environment.type == NJ_ANALYZE_FORWARD_SEARCH ||                    \
         (w)->environment.type == NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI)) &&    \
      (!((z)->ctrl_opt & NJ_SEARCH_DISMANTLING_CONTROL))  &&                      \
      (NJ_GET_DIC_TYPE_EX((x), (y)) == NJ_DIC_TYPE_LEARN) &&                      \
      ((z)->operation == NJ_CUR_OP_FORE)) ? 1 : 0)

#define IS_TARGET_COMPOUND_WORD(iwnn, type, handle, cond, que_id)       \
    (((IS_TARGET_COMPOUND_MODE((iwnn), (type), (handle), (cond))) &&    \
      (is_continued((iwnn), (handle), (que_id)) == 1)) ? 1 : 0)


/************************************************/
/*              構 造 体 宣 言                  */
/************************************************/

/************************************************/
/*        static 変数宣言                       */
/************************************************/



/************************************************/
/*              prototype  宣  言               */
/************************************************/
/* static 関数  */
static NJ_INT16 is_continued(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
static NJ_UINT8 *write_string(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 *dst, NJ_UINT8 *src, NJ_UINT8 size);
static NJ_INT16 que_strcmp_complete_with_hyouki(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id, NJ_CHAR *yomi, NJ_UINT16 yomi_len, NJ_CHAR *hyouki, NJ_UINT8 multi_flg);
static void shift_index(NJ_UINT8 *ptr, NJ_UINT8 direction, NJ_UINT16 shift_count);
static NJ_INT16 get_cand_by_sequential(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern, NJ_UINT8 comp_flg);
static NJ_INT16 get_cand_by_evaluate(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern);
static NJ_INT16 get_cand_by_evaluate2(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern, NJ_UINT16 hIdx);
static NJ_INT16 search_range_by_yomi(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 op, NJ_CHAR *yomi, NJ_UINT16 ylen, NJ_UINT16 *from, NJ_UINT16 *to, NJ_UINT8 *forward_flag);
static NJ_INT16 search_range_by_yomi2(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 op, NJ_CHAR *yomi, NJ_UINT16 ylen, NJ_UINT16 sfrom, NJ_UINT16 sto,
                                      NJ_UINT16 *from, NJ_UINT16 *to, NJ_UINT8 *forward_flag);
static NJ_INT16 search_range_by_yomi_multi(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_CHAR *yomi, NJ_UINT16 ylen, NJ_UINT16 *from, NJ_UINT16 *to);
static NJ_INT16 str_que_cmp(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_CHAR *yomi, NJ_UINT16 yomiLen, NJ_UINT16 que_id, NJ_UINT8 mode, NJ_UINT8 rev);
static NJ_WQUE *get_que_type_and_next(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
static NJ_WQUE *get_que_allHinsi(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
static NJ_INT16 search_empty_que(NJ_DIC_HANDLE handle, NJ_UINT16 *que_id);
static NJ_UINT8 check_muhenkan(NJ_CLASS *iwnn, NJ_CHAR *yomi, NJ_CHAR *hyouki, NJ_UINT8 *type);
static NJ_WQUE *get_que_yomiLen_and_hyoukiLen(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
static NJ_INT16 continue_cnt(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);

static NJ_UINT8 *get_search_index_address(NJ_DIC_HANDLE handle, NJ_UINT8 search_pattern);

static NJ_HINDO get_hindo(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern);

static NJ_HINDO calculate_hindo(NJ_DIC_HANDLE handle, NJ_INT32 freq, NJ_DIC_FREQ *dic_freq, NJ_INT16 freq_max, NJ_INT16 freq_min);
static NJ_INT16 search_regist_range(NJ_DIC_HANDLE handle, NJ_UINT16 *from, NJ_UINT16 *to, NJ_UINT16 *count);
static NJ_INT16 get_cand_by_evaluate_state(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern);
static NJ_INT16 get_cand_by_evaluate2_state(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern, NJ_UINT16 hIdx);
static NJ_INT16 get_candidate_buntou_link(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset);
static NJ_INT16 que_strcmp_include(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id, NJ_CHAR *yomi);
static NJ_INT16 njd_l_get_next_data(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond,
                                    NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern,
                                    NJ_DIC_HANDLE handle, NJ_UINT8 *ptr, NJ_UINT32 *current,
                                    NJ_UINT32 top, NJ_UINT32 bottom,
                                    NJ_INT32 *ret_hindo, NJ_UINT8 *current_info);
static NJ_INT16 is_commit_range(NJ_CLASS *iwnn, NJ_UINT16 que_id);
static void     set_cmpdg_mode(NJ_CLASS* iwnn, NJ_SEARCH_CONDITION* cond, NJ_SEARCH_LOCATION_SET* loctset, NJ_UINT16 que_id);
static NJ_UINT8 set_cmpdg_size_check(NJ_CLASS* iwnn, NJ_SEARCH_CONDITION* cond, NJ_SEARCH_LOCATION_SET* loctset, NJ_UINT16 que_id);
static NJ_INT32 get_additional_info_size(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id);
static NJ_INT16 njd_l_delete_same_word(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, 
                                       NJ_CHAR *yomi, NJ_UINT16 ylen, NJ_CHAR* hyoki, NJ_UINT16 hlen);
static NJ_INT16 create_word_info(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id, NJ_WORD_INFO *info);
static NJ_INT16 delete_word_ext_data(NJ_CLASS *iwnn, NJ_WORD_INFO *info);

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

/**
 * 検索すべきインデックスの先頭アドレスを返す
 *
 * @param[in]         handle : 辞書アドレス
 * @param[in] search_pattern : 検索方法
 *
 * @retval              NULL   許されていない検索
 * @retval             !NULL   インデックスの先頭アドレス
 */
static NJ_UINT8 *get_search_index_address(NJ_DIC_HANDLE handle, NJ_UINT8 search_pattern) {


    switch (search_pattern) {
    case NJ_CUR_OP_REV:         /* 逆引き完全一致検索     */
    case NJ_CUR_OP_COMP_EXT:    /* 正引き完全一致拡張検索 */
    case NJ_CUR_OP_FORE_EXT:    /* 逆引き前方一致拡張検索 */
        /* 候補順インデックスを指定 */
        return LEARN_INDEX_TOP_ADDR2(handle);
    default:
        /* 逆引き検索、正引き拡張検索以外なら読み順インデックスを指定 */
        return LEARN_INDEX_TOP_ADDR(handle);
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
void njd_l_write_uint16_data(NJ_UINT8* ptr, NJ_UINT16 data) {
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
static NJ_INT16 search_empty_que(NJ_DIC_HANDLE handle, NJ_UINT16 *que_id) {
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
    return -1; /*NCH_DEF*/
}


/**
 * 読みと表記を比較して無変換候補に該当するかどうかチェックする
 *
 * 無変換候補は以下の２種類
 *
 * １ 読み、表記がバイナリイメージで同じ場合
 * - 読み：あめりか    表記：あめりか
 * - 読み：アメリカ    表記：アメリカ
 * - 読み：ping/in     表記：ping/in
 * - 読み：12345       表記：12345
 *
 * ２ 読み ひらがな、表記 ひらがな→カタカナの場合
 * - 読み：あめりか    表記：アメリカ
 *
 * @attention 読みがすべてひらがなの場合のみ無変換候補とする
 *
 * @param[in]    iwnn : 解析情報クラス
 * @param[in]    yomi : 読み文字列
 * @param[in]  hyouki : 表記文字列
 * @param[out]   type : 無変換候補の種別を表す<br>
 *                      0 : 読み、表記文字列が等しい<br>
 *                      1 : 読みのひらがなをカタカナ変換すると、表記が同じ
 *
 * @retval          0   無変換候補でない
 * @retval          1   無変換候補である
 */
static NJ_UINT8 check_muhenkan(NJ_CLASS *iwnn, NJ_CHAR *yomi, NJ_CHAR *hyouki,
                               NJ_UINT8 *type) {
    NJ_UINT16 ylen, klen;


    /* type=0 で初期化しておく */
    *type = 0;

    /* 言語環境に関わらず、読みと表記文字列を比較する */
    if (nj_strcmp(yomi, hyouki) == 0) {
        /* 読み、表記文字列が等しい場合(type=0 を設定) */
        *type = 0;
        return 1;
    }

    /* 読み、候補長を比較して一致しなければ 0 を返す */
    ylen = nj_strlen(yomi);
    klen = nj_strlen(hyouki);
    if (ylen != klen) {
        return 0;
    }

#ifndef NJ_OPT_UTF16
    /* 読み文字列長が偶数でない場合、1byte コードが
     * 存在する。無変換候補としない */
    if (ylen & 0x0001) {
        return 0;
    }
#endif /* ! NJ_OPT_UTF16 */

    /* 読み文字列をカタカナに変換した文字列と表記文字列を比較 */
    nje_convert_hira_to_kata(yomi, &(iwnn->muhenkan_tmp[0]), ylen);
    if (nj_strcmp(hyouki, &(iwnn->muhenkan_tmp[0])) == 0) {
        /* 読みのカタカナ、表記文字列が等しい場合(type=1 を設定) */
        *type = 1;
        return 1;
    }
    return 0;
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
NJ_INT16 njd_l_search_word(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *con,
                           NJ_SEARCH_LOCATION_SET *loctset,
                           NJ_UINT8 comp_flg) {

    NJ_UINT16    word_count;
    NJ_UINT32    type;
    NJ_DIC_INFO *pdicinfo;
    NJ_UINT16    hIdx;
    NJ_INT16     ret;


    word_count = GET_LEARN_WORD_COUNT(loctset->loct.handle);
    if (word_count == 0) {
        /* 条件に合う登録単語がない     */
        loctset->loct.status = NJ_ST_SEARCH_END_EXT;
        return 0;
    }

    type = NJ_GET_DIC_TYPE_EX(loctset->loct.type, loctset->loct.handle);
    /* 非圧縮カスタマイズ辞書のみ,検索文字列長のチェックを行なう */
    if ((type == NJ_DIC_TYPE_CUSTOM_INCOMPRESS) ||
        (type == NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN)) {
        if ((con->operation == NJ_CUR_OP_REV) ||
            (con->operation == NJ_CUR_OP_REV_FORE) ||
            (con->operation == NJ_CUR_OP_COMP_EXT) ||
            (con->operation == NJ_CUR_OP_FORE_EXT)) {
            /* 逆引検索,拡張検索時は、最大候補長と比較   */
            if (con->ylen > NJ_GET_MAX_KLEN(loctset->loct.handle)) {
                loctset->loct.status = NJ_ST_SEARCH_END_EXT;
                return 0;
            }
        } else if ((con->operation == NJ_CUR_OP_COMP) ||
                   (con->operation == NJ_CUR_OP_FORE)){
            /* 正引き検索時は、最大読み長と比較 */
            if (con->ylen > NJ_GET_MAX_YLEN(loctset->loct.handle)) {
                loctset->loct.status = NJ_ST_SEARCH_END_EXT;
                return 0;
            }
        }
        /* 
         * 繋がり検索時は、最大読み長、最大候補長を超えて検索することが
         * あるため、チェックを行わない
         */
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
        return get_cand_by_sequential(iwnn, con, loctset, con->operation, comp_flg);

    case NJ_CUR_OP_FORE:
        /* 前方一致 */
        if (con->mode == NJ_CUR_MODE_REGIST) {
            /* 登録順 */
            if ((type != NJ_DIC_TYPE_USER) &&
                (type != NJ_DIC_TYPE_LEARN) &&
                (type != NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN)) {
                /* 学習辞書、ユーザ辞書、カスタマイズ辞書(学習辞書変更)以外は、検索対象外とする */
                loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
                return 0; /*NCH_FB*/
            }
            if (con->ylen > 0) {
                /* 読み長0以外では行わない */
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;
            }
            return get_cand_by_evaluate(iwnn, con, loctset, con->operation);
        } else if (con->mode == NJ_CUR_MODE_YOMI) {
            /* 読み順のとき */
            return get_cand_by_sequential(iwnn, con, loctset, con->operation, 0);
        } else {
            /* 頻度順のとき */
            /* 辞書ハンドル位置を特定する。 */
            pdicinfo = con->ds->dic;
            for (hIdx = 0; (hIdx < NJ_MAX_DIC) && (pdicinfo->handle != loctset->loct.handle); hIdx++) {
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

    case NJ_CUR_OP_LINK:
        /* つながり検索 */
        if (type == NJ_DIC_TYPE_USER) {
            /* ユーザ辞書でのつながり検索はない */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            break;
        }
        if (con->mode != NJ_CUR_MODE_FREQ) {
            /* つながり検索のときは頻度順のみ指定可能 */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            break;
        }
        /* 通常の繋がり検索 */
        if ((type == NJ_DIC_TYPE_USER)
            || (comp_flg == 0)) {
            /* ユーザ辞書 or 単語検索APIからの呼び出しの場合 */
            return get_cand_by_sequential(iwnn, con, loctset, con->operation, 0);
        } else {
            /* 文頭繋がり予測 */
            if (IS_BUNTOU_LINK_SEARCH_MODE(iwnn)) {
                return  get_candidate_buntou_link(iwnn, loctset);
            }
            /* 複数キュー繋がり対応 */
            return GET_CANDIDATE_BY_EVAL(iwnn, con, loctset, con->operation);
        }

    case NJ_CUR_OP_REV:
        /* 逆引き検索 */
        if (con->mode != NJ_CUR_MODE_FREQ) {
            /* 逆引検索のときは頻度順のみ指定可能 */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            break;
        }
        switch (type) {
        case NJ_DIC_TYPE_LEARN:
        case NJ_DIC_TYPE_USER:
            /* 学習辞書、ユーザ辞書
             * 逆引きの場合、状況適応には対応していないので、
             * GET_CANDIDATE_BY_EVALではなく、get_cand_by_evaluateを直接呼ぶ
             */
            return get_cand_by_evaluate(iwnn, con, loctset, con->operation);

        default:
            /* 非圧縮カスタマイズ辞書   */
            return get_cand_by_sequential(iwnn, con, loctset, con->operation, comp_flg);
        }

    case NJ_CUR_OP_COMP_EXT:
        /* 正引き完全一致拡張検索 */
        if (con->mode != NJ_CUR_MODE_FREQ) {
            /* 正引き完全一致拡張検索のときは頻度順のみ指定可能 */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            break;
        }
        /* 完全一致拡張 */
        /* 完全一致拡張の場合は頻度順であってもインデックス順に検索できる   */
        return get_cand_by_sequential(iwnn, con, loctset, con->operation, comp_flg);

    case NJ_CUR_OP_FORE_EXT:
        /* 正引き前方一致拡張検索 */
        if (con->mode == NJ_CUR_MODE_REGIST) {
            /* 登録順 */
            if ((type != NJ_DIC_TYPE_USER) &&
                (type != NJ_DIC_TYPE_LEARN) &&
                (type != NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN)) {
                /* 学習辞書以外は、検索対象外とする */
                loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
                return 0; /*NCH_FB*/
            }
            if (con->ylen > 0) {
                /* 読み長0以外では行わない */
                loctset->loct.status = NJ_ST_SEARCH_END;
                return 0;
            }
            return get_cand_by_evaluate(iwnn, con, loctset, con->operation);
        } else if (con->mode == NJ_CUR_MODE_YOMI) {
            /* 読み順のとき */
            return get_cand_by_sequential(iwnn, con, loctset, con->operation, 0);
        } else {
            /* 頻度順のとき */
            /* 辞書ハンドル位置を特定する。 */
            pdicinfo = con->ds->dic;
            for (hIdx = 0; (hIdx < NJ_MAX_DIC) && (pdicinfo->handle != loctset->loct.handle); hIdx++) {
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
                return get_cand_by_evaluate(iwnn, con, loctset, con->operation);
            } else {
                ret = get_cand_by_evaluate2(iwnn, con, loctset, con->operation, hIdx);
                if (ret == NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_NOT_ENOUGH)) {
                    /* キャッシュ溢れが発生した場合は、もう一度同じ関数を呼ぶ */
                    NJ_SET_CACHEOVER_TO_SCACHE(con->ds->dic[hIdx].srhCache);
                    ret = get_cand_by_evaluate2(iwnn, con, loctset, con->operation, hIdx);
                }
                return ret;
            }
        }

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
static NJ_WQUE *get_que_type_and_next(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
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
static NJ_WQUE *get_que_yomiLen_and_hyoukiLen(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                                              NJ_UINT16 que_id) {
    NJ_UINT8 *ptr;
    NJ_WQUE *que = &(iwnn->que_tmp);


    if (que_id >= GET_LEARN_MAX_WORD_COUNT(handle)) {
        return NULL; /*NCH_FB*/
    }

    ptr = POS_TO_ADDRESS(handle, que_id);

    que->type        = GET_TYPE_FROM_DATA(ptr);
    que->yomi_byte   = GET_YSIZE_FROM_DATA(ptr);
    que->yomi_len    = que->yomi_byte / sizeof(NJ_CHAR);
    que->hyouki_byte = GET_KSIZE_FROM_DATA(ptr);
    que->hyouki_len  = que->hyouki_byte / sizeof(NJ_CHAR);

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
static NJ_WQUE *get_que_allHinsi(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
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
NJ_WQUE *njd_l_get_que(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id) {
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
    que->yomi_len   = que->yomi_byte / sizeof(NJ_CHAR);
    que->hyouki_byte= GET_KSIZE_FROM_DATA(ptr);
    que->hyouki_len = que->hyouki_byte / sizeof(NJ_CHAR);
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
static NJ_INT16 is_continued(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id) {
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

        que = get_que_type_and_next(iwnn, handle, que_id);
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
static NJ_INT16 continue_cnt(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id) {
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

        que = get_que_type_and_next(iwnn, handle, que_id);
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
NJ_UINT16 njd_l_search_next_que(NJ_DIC_HANDLE handle, NJ_UINT16 que_id) {
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
NJ_UINT16 njd_l_search_prev_que(NJ_DIC_HANDLE handle, NJ_UINT16 que_id) {
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
NJ_INT16 njd_l_delete_index(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
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
            que_id2 = njd_l_search_next_que(handle, que_id);
            que = get_que_type_and_next(iwnn, handle, que_id2);
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
            njd_l_write_uint16_data(handle + POS_WRITE_FLG + 2, que_id);

            /* ---------------------- */
            /*   Lstat-18 (Ustat-15)  */
            /* ---------------------- */

            /* --------------------------------- */
            /*   登録語数待避 Ldel-03 (Udel-02)  */
            /* --------------------------------- */
            /* 電断用flg上位2byteに登録語数 - 1を待避   */
            njd_l_write_uint16_data(handle + POS_WRITE_FLG, (NJ_UINT16)(max - 1));

            /* ---------------------- */
            /*   Lstat-19 (Ustat-16)  */
            /* ---------------------- */

            /* ------------------------------------------- */
            /*   読み順インデックス削除 Ldel-04 (Udel-03)  */
            /*   書き込み途中は Lstat-20 (Ustat-17)        */
            /* ------------------------------------------- */
            /* 読み順インデックスシフト処理     */
            shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(max - i - 1));

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
            shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(max - i - 1));

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
static NJ_UINT8 *write_string(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 *dst,
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
            ret = njd_l_delete_index(iwnn, handle, (NJ_UINT16)ADDRESS_TO_POS(handle, dst)); /*NCH_FB*/
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
            ret = njd_l_delete_index(iwnn, handle, (NJ_UINT16)ADDRESS_TO_POS(handle, dst));
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
static NJ_INT16 que_strcmp_complete_with_hyouki(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, 
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
        str = njd_l_get_string(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
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

        str = njd_l_get_hyouki(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
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
        ret = is_continued(iwnn, handle, que_id);
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
        que_id = njd_l_search_next_que(handle, que_id);
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
static NJ_INT16 que_strcmp_include(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                                   NJ_UINT16 que_id, NJ_CHAR *yomi) {
    NJ_CHAR *str;
    NJ_UINT16 que_len;
    NJ_UINT16 yomi_len;
    NJ_INT16  ret;
    NJ_INT16  que_count = 1; /* 連続キューカウント数 */
    NJ_UINT16 i = 0;
    NJ_UINT8  slen;
    NJ_UINT8  *add_ptr;
    NJ_UINT32 dictype;
    NJ_UINT32 version;
    NJ_UINT16 addinfo_len;


    yomi_len = nj_strlen(yomi);
    if (yomi_len == 0) {
        return que_count;
    }
    /* かな漢字変換専用ロジック。NJ_CUR_OP_REVは来ない  */
    i = GET_LEARN_WORD_COUNT(handle);

    /* 付加情報が存在するのか判定するため、辞書タイプとバージョンを取得 */
    dictype = NJ_GET_DIC_TYPE(handle);
    version = NJ_GET_DIC_VER(handle);
    if ((dictype == NJ_DIC_TYPE_LEARN) && (version == NJ_DIC_VERSION4)) {
        /* 付加情報あり */
        add_ptr = POS_TO_ADD_ADDRESS(handle, que_id);
        addinfo_len = (NJ_UINT16)(*add_ptr / sizeof(NJ_CHAR));
    } else {
        /* 付加情報なし */
        add_ptr = NULL;
        addinfo_len = 0;
    }

    while (--i) {        /* 保険のため、現在登録されているキュー数以下のループとする */

        /* 次のキューがつながるか */
        ret = is_continued(iwnn, handle, que_id);
        if (ret < 0) {
            /* エラー   */
            return ret;
        } else if (ret == 0) {
            /* つながらない     */
            return que_count;
        }

        /* 次のキューを探す */
        que_id = njd_l_search_next_que(handle, que_id);

        if (add_ptr != NULL) {
            add_ptr = POS_TO_ADD_ADDRESS(handle, que_id);
            if (NJ_MAX_ADDITIONAL_LEN >= (addinfo_len + (NJ_UINT16)(*add_ptr / sizeof(NJ_CHAR)))) {
                addinfo_len += (NJ_UINT16)(*add_ptr / sizeof(NJ_CHAR));
            } else {
                /* 付加情報が溢れる場合は、納まる分までを文節とする */
                return que_count; /*NCH_DEF*/
            }
        }

        str = njd_l_get_string(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
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
 * @param[in]         iwnn : 解析情報クラス
 * @param[in]       handle : 辞書ハンドル
 * @param[in]       que_id : キューID
 * @param[out]        slen : 取得した文字配列長
 * @param[in]  segment_num : 読み文字列取得文節数
 *
 * @retval       NULL   エラー
 * @retval      !NULL   読み文字列
 */
NJ_CHAR *njd_l_get_string(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                           NJ_UINT16 que_id, NJ_UINT8 *slen, NJ_UINT8 segment_num) {
    NJ_UINT8 *src, *dst;
    NJ_UINT8 copy_size, size;
    NJ_UINT8 i;
    NJ_UINT8 *top_addr;
    NJ_UINT8 *bottom_addr;
    NJ_UINT16 que_size;
    NJ_UINT16 cnt = 0;


    *slen = 0;

    /* 初回のみ設定 */
    dst = (NJ_UINT8*)&(iwnn->learn_string_tmp[0]);

    for (cnt = 0; cnt < segment_num; cnt++) {
        if (cnt != 0) {
            que_id = njd_l_search_next_que(handle, que_id);
        }

        src = POS_TO_ADDRESS(handle, que_id);
        switch (GET_TYPE_FROM_DATA(src)) {
        case QUE_TYPE_JIRI:
        case QUE_TYPE_FZK:
            size =  GET_YSIZE_FROM_DATA(src);
            *slen += (NJ_UINT8)(size / sizeof(NJ_CHAR));
            break;

        default:
            return NULL;
        }
        /* 読み長をチェックする     */
        if (NJ_GET_DIC_TYPE(handle) == NJ_DIC_TYPE_USER) {
            if (*slen > NJ_MAX_USER_LEN) {
                return NULL; /*NCH*/
            }
        } else {
            if (*slen > NJ_MAX_LEN) {
                return NULL;
            }
        }

        /* 固定部分までポインタを進める     */
        src += LEARN_QUE_STRING_OFFSET;

        que_size = QUE_SIZE(handle);

        /* 最初のキューのコピー可能範囲 */
        copy_size = (NJ_UINT8)que_size - LEARN_QUE_STRING_OFFSET;
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
    }

    iwnn->learn_string_tmp[*slen] = NJ_CHAR_NUL;

    return &(iwnn->learn_string_tmp[0]);
}

/**
 * 学習領域データから、指定されたキューの候補文字列を取得する
 *
 * @param[in]         iwnn : 解析情報クラス
 * @param[in]       handle : 辞書ハンドル
 * @param[in]       que_id : キューID
 * @param[out]        slen : 取得した文字配列長
 * @param[in]  segment_num : 候補文字列取得文節数
 *
 * @retval       NULL   エラー
 * @retval      !NULL   候補文字列
 */
NJ_CHAR *njd_l_get_hyouki(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                           NJ_UINT16 que_id, NJ_UINT8 *slen, NJ_UINT8 segment_num) {
    NJ_UINT8 *src, *dst;
    NJ_WQUE *que;
    NJ_UINT8 copy_size, size;
    NJ_UINT8 i;
    NJ_UINT8 *top_addr;
    NJ_UINT8 *bottom_addr;
    NJ_CHAR  *hira;
    NJ_UINT16 que_size;
    NJ_UINT32 dictype;
    NJ_UINT16 cnt = 0;
    NJ_UINT8 tmp_slen = 0;


    *slen = 0;

    /* 初回のみ設定 */
    dst = (NJ_UINT8*)&(iwnn->muhenkan_tmp[0]);

    for (cnt = 0; cnt < segment_num; cnt++) {
        if (cnt != 0) {
            que_id = njd_l_search_next_que(handle, que_id);
        }

        que = get_que_yomiLen_and_hyoukiLen(iwnn, handle, que_id);
        if (que == NULL) {
            return NULL;
        }
        /* 読み長、候補長をチェックする     */
        dictype = NJ_GET_DIC_TYPE(handle);
        if (dictype == NJ_DIC_TYPE_USER) {
            if (que->yomi_len > NJ_MAX_USER_LEN) {
                return NULL; /*NCH*/
            }
            if (que->hyouki_len > NJ_MAX_USER_KOUHO_LEN) {
                return NULL; /*NCH*/
            }
        } else {
            if (que->yomi_len > NJ_MAX_LEN) {
                return NULL; /*NCH*/
            }
            if (que->hyouki_len > NJ_MAX_RESULT_LEN) {
                return NULL;
            }
        }

        src = POS_TO_ADDRESS(handle, que_id);
        /* 表記長が格納されていなければ無変換候補   */
        if (que->hyouki_len == 0) {
            hira = njd_l_get_string(iwnn, handle, que_id, &tmp_slen, NJ_NUM_SEGMENT1);
            if (hira == NULL) {
                return NULL; /*NCH*/
            }
            /* 無変換フラグONならカタカナ候補にする */
            if (GET_MFLG_FROM_DATA(src) != 0) {
                /* カタカナ候補の場合 */
                *slen += (NJ_UINT8)nje_convert_hira_to_kata(hira, &(iwnn->muhenkan_tmp[*slen]), (NJ_UINT16)tmp_slen);
                if (cnt == (segment_num - 1)) {
                    /* 終端の文節の場合 */
                    return &(iwnn->muhenkan_tmp[0]);
                } else {
                    /* 取得文節が残っているので上位に戻る */
                    dst += (tmp_slen * sizeof(NJ_CHAR));
                    continue;
                }
            } else {
                /* ひらがな候補の場合 */
                if (cnt == (segment_num - 1)) {
                    /* 終端の文節の場合 */
                    if (segment_num < NJ_NUM_SEGMENT2) {
                        *slen += tmp_slen;
                        return hira;
                    } else {
                        nj_strcpy(&iwnn->muhenkan_tmp[*slen], hira);
                        *slen += tmp_slen;
                        return &(iwnn->muhenkan_tmp[0]);
                    }
                } else {
                    /* 取得文節が残っているのでひらがな文字情報上位に戻る */
                    nj_strcpy(&iwnn->muhenkan_tmp[*slen], hira);
                    *slen += tmp_slen;
                    dst += (tmp_slen * sizeof(NJ_CHAR));
                    continue;
                }
            }
        }
        /* 読み先頭までポインタを進める     */
        src += LEARN_QUE_STRING_OFFSET;

        que_size = QUE_SIZE(handle);

        /* 読み文字数を進める */
        size = que->yomi_byte;
        copy_size = (NJ_UINT8)que_size - LEARN_QUE_STRING_OFFSET;
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
                src = top_addr; /*NCH_DEF*/
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
        *slen += que->hyouki_len;
    }

    iwnn->muhenkan_tmp[*slen] = NJ_CHAR_NUL;

    return &(iwnn->muhenkan_tmp[0]);
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
NJ_INT16 njd_l_write_learn_data(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id,
                                 NJ_LEARN_WORD_INFO *word, NJ_UINT8 connect, NJ_UINT8 type,
                                 NJ_UINT8 undo, NJ_UINT8 muhenkan) {
    NJ_UINT8  *ptr, *index_ptr;
    NJ_UINT16 next_que_id;
    NJ_UINT16 i, max;
    NJ_CHAR   *str;
    NJ_UINT16 que_id2;
    NJ_UINT8  slen;
    NJ_INT16  ret = 0;
    NJ_UINT32 dictype;
    NJ_UINT16 top, bottom, mid;
    NJ_UINT32 version;
    NJ_UINT8  *ext_ptr;


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

    dictype = NJ_GET_DIC_TYPE(handle);

    /* 語彙種別と語彙情報分進める       */
    ptr += LEARN_QUE_STRING_OFFSET;
    /* 読み、表記を書き出す     */
    ptr = write_string(iwnn, handle, ptr, (NJ_UINT8*)word->yomi, (NJ_UINT8)(word->yomi_len*sizeof(NJ_CHAR)));
    if (ptr == NULL) {
        /* write_string内部でdelete_indexに失敗 */
        return NJ_SET_ERR_VAL(NJ_FUNC_WRITE_LEARN_DATA, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
    }
    ptr = write_string(iwnn, handle, ptr, (NJ_UINT8*)word->hyouki, (NJ_UINT8)(word->hyouki_len*sizeof(NJ_CHAR)));
    if (ptr == NULL) {
        /* write_string内部でdelete_indexに失敗 */
        return NJ_SET_ERR_VAL(NJ_FUNC_WRITE_LEARN_DATA, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
    }

    /*
     * 拡張単語情報領域に書き込みを行う
     * iWnnタイプの学習辞書のみ
     */
    version = NJ_GET_DIC_VER(handle);
    if (((dictype == NJ_DIC_TYPE_LEARN) && (version == NJ_DIC_VERSION4)) ||
        ((dictype == NJ_DIC_TYPE_LEARN) && (version == NJ_DIC_VERSION3))) {
        ext_ptr = POS_TO_EXT_ADDRESS(handle, que_id);
        /* 単語の属性データを保存 */
        STATE_COPY(ext_ptr, (NJ_UINT8*)&word->attr);
        ext_ptr += sizeof(NJ_UINT32);
        /* 単語の自立語後品詞(9bit)、付属語読みバイト数(7bit)を書き込み */
        SET_BPOS_AND_YSIZE(ext_ptr, word->stem_b_hinsi, word->fzk_yomi_len*sizeof(NJ_CHAR));
    }

    /**
     * 付加情報領域に書き込みを行う。
     * 付加情報付の学習辞書＆ユーザ辞書を対象とする。
     */
    if (((dictype == NJ_DIC_TYPE_LEARN) && (version == NJ_DIC_VERSION4)) ||
        ((dictype == NJ_DIC_TYPE_USER) && (version == NJ_DIC_VERSION3))) {
        ext_ptr = POS_TO_ADD_ADDRESS(handle, que_id);
        /* 付加情報文字列長を格納する */
        *ext_ptr = (NJ_UINT8)(word->additional_len * sizeof(NJ_CHAR));
        ext_ptr++;
        *ext_ptr = 0x00; /* リザーブ */
        ext_ptr++; /* リザーブ */

        /* 付加情報文字列を格納する */
        nj_memcpy((NJ_UINT8*)ext_ptr, (NJ_UINT8*)word->additional, (NJ_UINT16)(word->additional_len * sizeof(NJ_CHAR)));
    }


    /* ---------------------- */
    /*   Lstat-03 (Ustat-03)  */
    /* ---------------------- */

    /* ------------------------------ */
    /*   復旧位置格納処理（Ladd-02）  */
    /* ------------------------------ */
    /* 電断用flgの下位2byteに追加対象キューIDを待避する */
    njd_l_write_uint16_data(handle + POS_WRITE_FLG + 2, que_id);

    /* ---------------------- */
    /*   Lstat-04 (Ustat-04)  */
    /* ---------------------- */

    if (dictype == NJ_DIC_TYPE_LEARN) {
        /* 学習辞書 ----- */
        next_que_id = ADDRESS_TO_POS(handle, ptr - 1) + 1;
        if (next_que_id >= GET_LEARN_MAX_WORD_COUNT(handle)) {
            next_que_id = 0;
        }

        /* ----------------------------------- */
        /*   次キュー追加位置更新処理 Ladd-03  */
        /* ----------------------------------- */
        njd_l_write_uint16_data(handle + POS_NEXT_QUE + 2, next_que_id);

        /* ------------ */
        /*   Lstat-05   */
        /* ------------ */
    }

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
        str = njd_l_get_string(iwnn, handle, que_id2, &slen, NJ_NUM_SEGMENT1);
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
            if (dictype == NJ_DIC_TYPE_USER) {
                str = njd_l_get_hyouki(iwnn, handle, que_id2, &slen, NJ_NUM_SEGMENT1);
                if (str == NULL) {
                    return NJ_SET_ERR_VAL(NJ_FUNC_WRITE_LEARN_DATA, NJ_ERR_DIC_BROKEN); /*NCH*/
                }
                ret = nj_strcmp(word->hyouki, str);
                if (ret <= 0) {
                    bottom = mid;
                } else {
                    top = mid + 1;
                }
            } else {
                bottom = mid;
            }
        }
    }
    index_ptr += (NJ_INDEX_SIZE * bottom);
    i = bottom;

    /* 電断用flgの上位2byteに登録語数 + 1 を待避する    */
    njd_l_write_uint16_data(handle + POS_WRITE_FLG, (NJ_UINT16)(max + 1));

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
        shift_index(index_ptr, SHIFT_RIGHT, (NJ_UINT16)(max - i));
    }
    /* ---------------------- */
    /*   Lstat-09 (Ustat-08)  */
    /* ---------------------- */

    /* --------------------------------- */
    /*   キューID挿入 Ladd-07 (Uadd-06)  */
    /* --------------------------------- */
    njd_l_write_uint16_data(index_ptr, que_id);

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
        str = njd_l_get_hyouki(iwnn, handle, que_id2, &slen, NJ_NUM_SEGMENT1);
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
        shift_index(index_ptr, SHIFT_RIGHT, (NJ_UINT16)(max - i));
    }
    /* ---------------------- */
    /*   Lstat-12 (Ustat-11)  */
    /* ---------------------- */

    /* --------------------------------- */
    /*   キューID挿入 Ladd-09 (Uadd-08)  */
    /* --------------------------------- */
    njd_l_write_uint16_data(index_ptr, que_id);

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
static void shift_index(NJ_UINT8 *ptr,
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
 * 学習辞書またはユーザ辞書に学習する
 *
 * @param[in]    iwnn : 解析情報クラス
 * @param[in]    word : 学習単語情報
 * @param[in] connect : 関係学習（0:行わない、0以外:行う）
 * @param[in]    type : 単語種別（0:自立語、0以外:付属語）
 * @param[in]    undo : アンドゥ指定（0:アンドゥフラグ立てない、1:アンドゥフラグ立てる）
 * @param[in] dictype : 登録する辞書種別<br>
 *                      0(ADD_WORD_DIC_TYPE_USER)  : ユーザ辞書<br>
 *                      1(ADD_WORD_DIC_TYPE_LEARN) : 学習辞書
 *
 * @retval         <0   エラー
 * @retval        >=0   正常終了
 */
NJ_INT16 njd_l_add_word(NJ_CLASS *iwnn, NJ_LEARN_WORD_INFO *word,
                        NJ_UINT8 connect, NJ_UINT8 type, NJ_UINT8 undo, NJ_UINT8 dictype) {

    NJ_UINT16 que_id;
    NJ_INT16 ret;
    NJ_UINT16 word_count;
    NJ_INT16 i;
    NJ_DIC_HANDLE handle = NULL;
    NJ_UINT16 from, to, id;
    NJ_UINT16 max_count;
    NJ_WQUE *que;
    NJ_UINT8 *p;
    NJ_UINT8 forward_flag = 0;                          /* ダミー       */
    NJ_UINT8 muhenkan;
    NJ_DIC_SET *dics = &(iwnn->dic_set);
    NJ_UINT16 que_size;
    NJ_UINT16 que_byte;
    NJ_UINT16 tmp_que_id;


    if (dictype == ADD_WORD_DIC_TYPE_LEARN) {
        /* 辞書セットから学習辞書を探す */
        for (i = 0; i < NJ_MAX_DIC; i++) {
            if (dics->dic[i].handle == NULL) {
                continue;
            }
            if (NJ_GET_DIC_TYPE_EX(dics->dic[i].type, dics->dic[i].handle) == NJ_DIC_TYPE_LEARN) {
                handle = dics->dic[i].handle;
                break;
            }
        }
    } else {
        /* 辞書セットからユーザ辞書を探す       */
        for (i = 0; i < NJ_MAX_DIC; i++) {
            if (dics->dic[i].handle == NULL) {
                continue;
            }
            if (NJ_GET_DIC_TYPE_EX(dics->dic[i].type, dics->dic[i].handle) == NJ_DIC_TYPE_USER) {
                handle = dics->dic[i].handle;
                break;
            }
        }
    }
    if (handle == NULL) {
        /* 指定辞書無し */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_ADD_WORD, NJ_ERR_DIC_NOT_FOUND);
    }

    /* 現在登録数を取得する             */
    /* （追加なので登録語数で良い）     */
    word_count = GET_LEARN_WORD_COUNT(handle);

    if (dictype == ADD_WORD_DIC_TYPE_LEARN) {
        /* 学習辞書  */
        /* 登録位置は、辞書ヘッダの「次キュー追加位置」 */
        que_id = GET_LEARN_NEXT_WORD_POS(handle);

        /* 登録に必要なquesizeを計算する */
        que_byte = LEARN_QUE_STRING_OFFSET;
        que_byte += word->hyouki_len * sizeof(NJ_CHAR);
        que_byte += word->yomi_len * sizeof(NJ_CHAR);
        if (que_byte > QUE_SIZE(handle)) {
            /* 1queのサイズで収まらない場合 */
            que_size = 1;
            que_byte -= QUE_SIZE(handle);
            que_size += que_byte / (QUE_SIZE(handle)-1);
            if ((que_byte % (QUE_SIZE(handle)-1)) != 0) {
                que_size++;
            }
        } else {
            /* 1queのサイズで収まる場合 */
            que_size = 1;
        }
        tmp_que_id = que_id;

        for (i = 0; i < que_size; i++) {
            if (is_commit_range(iwnn, tmp_que_id)) {
                /* 学習辞書コミット範囲のため、学習辞書操作がエラーとなる。*/
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_MAKE_SPACE, NJ_ERR_PROTECTION_ERROR);
            }
            tmp_que_id++;
            if (tmp_que_id == GET_LEARN_MAX_WORD_COUNT(handle)) {
                tmp_que_id = 0;
            }
        }
    } else {
        /* ユーザ辞書 */
        max_count = GET_LEARN_MAX_WORD_COUNT(handle);           /* 最大単語登録数 */
        if (word_count >= max_count) {
            /* 辞書が一杯だった */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_ADD_WORD, NJ_ERR_USER_DIC_FULL);
        }
        /* 空きキューを探す */
        que_id = 0;
        if (search_empty_que(handle, &que_id) < 0) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_ADD_WORD, NJ_ERR_DIC_BROKEN); /*NCH*/
        };

        if (word_count > 0) {
            /* 読みで二分検索する       */
            ret = search_range_by_yomi(iwnn, handle, NJ_CUR_OP_COMP,
                                       word->yomi, nj_strlen(word->yomi), &from, &to, &forward_flag);
            if (ret < 0) {
                return ret; /*NCH_FB*/
            }
            if (ret > 0) {
                p = LEARN_INDEX_TOP_ADDR(handle);
                p += (from * NJ_INDEX_SIZE);
                while (from <= to) {
                    id = GET_UINT16(p);
                    ret = que_strcmp_complete_with_hyouki(iwnn, handle, id, 
                                                          word->yomi, nj_strlen(word->yomi), word->hyouki, 0);
                    if (ret < 0) {
                        return ret; /*NCH_FB*/
                    }
                    if (ret == 1) {
                        que = get_que_allHinsi(iwnn, handle, id);
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
    }

    if (dictype == ADD_WORD_DIC_TYPE_LEARN) {
        /* インデックスから削除し、キューを未使用にする */
        ret = njd_l_delete_index(iwnn, handle, que_id);
        if (ret < 0) {
            /* インデックス削除時に何らかのエラーが発生した */
            return ret;
        }
    }

    ret = check_muhenkan(iwnn, word->yomi, word->hyouki, &muhenkan);
    if (ret == 1) {
        /* 無変換候補の場合は表記長に 0 を設定する */
        word->hyouki_len = 0;
    }

    /* データ領域への追加 */
    ret = njd_l_write_learn_data(iwnn, handle, que_id, word, connect, type, undo, muhenkan);
    if (ret < 0) {
        return ret;
    }

    /* 複合語予測情報バッファクリア */
    njd_l_init_cmpdg_info(iwnn);

    if (NJ_GET_DIC_TYPE(handle) == NJ_DIC_TYPE_LEARN) {
        /* 学習時は学習辞書のキャッシュ管理領域をクリアする */
        if (dics->dic[i].srhCache != NULL) {
            dics->dic[i].srhCache->statusFlg = 0;
            dics->dic[i].srhCache->viewCnt = 0;
            dics->dic[i].srhCache->keyPtr[0] = 0;
            dics->dic[i].srhCache->keyPtr[1] = 0;
        } /* else {} */
    }

    return 0;
}


/**
 * 学習辞書から削除する
 *
 * @param[in]      iwnn : 解析情報クラス
 * @param[in]    result : 処理結果
 *
 * @retval      <0   エラー
 * @retval     >=0   正常終了
 */
NJ_INT16 njd_l_delete_word(NJ_CLASS *iwnn, NJ_RESULT *result) {
    NJ_INT16 ret;
    NJ_UINT16 que_id;
    NJ_INT16 i, j;
    NJ_WORD_INFO info;
    NJ_UINT8 current_info;
    NJ_UINT8 seg_cnt;
    NJ_CHAR del_yomi[NJ_MAX_LEN + NJ_TERM_SIZE];
    NJ_CHAR del_hyoki[NJ_MAX_RESULT_LEN + NJ_TERM_SIZE];
    NJ_CHAR *dst_pyomi, *src_pyomi;
    NJ_CHAR *dst_phyoki, *src_phyoki;
    NJ_UINT16 dst_ylen, dst_hlen;
    NJ_SEARCH_LOCATION *loc;
    NJ_UINT16 opttype;
    NJ_UINT16 del_que_id[NJ_MAX_PHRASE];
    NJ_WQUE *que;


    dst_pyomi = del_yomi;
    dst_phyoki = del_hyoki;
    dst_ylen = 0;
    dst_hlen = 0;
    loc = &result->word.stem.loc;
    que_id = (NJ_UINT16)(loc->current >> NJ_NUM_BIT16);

    /*
     * 削除対象候補が学習辞書の候補であった場合、
     * 統合辞書の頻度学習値をクリアする為に必要な単語情報を取得しておく
     */
    if (NJ_GET_DIC_TYPE(loc->handle) == NJ_DIC_TYPE_LEARN) {
        if ((NJ_GET_RESULT_OP(result->operation_id) != NJ_OP_SEARCH) &&
            (NJ_GET_RESULT_FUNC(result->operation_id) != NJ_FUNC_NEXT)) {
            current_info = loc->current_info >> NJ_NUM_BIT4;
        } else {
            current_info = 1;
        }
        del_que_id[0] = que_id;
        seg_cnt = 0;
        /* 文節数処理を実施 */
        while (seg_cnt < current_info) {
            if (is_commit_range(iwnn, que_id)) {
                /* 学習辞書コミット範囲のため、学習辞書操作がエラーとなる。*/
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_MAKE_SPACE, NJ_ERR_PROTECTION_ERROR);
            }
            del_que_id[seg_cnt] = que_id;
            /* 確認文節数をインクリメント */
            seg_cnt++;

            /* 確認したい文節数分回れば、処理を抜ける */
            if (seg_cnt == current_info) {
                break;
            }

            /* 接続する候補があるかを確認する */
            ret = is_continued(iwnn, loc->handle, que_id);
            if (ret < 0) {
                /* エラー */
                return ret; /*NCH*/
            } else if (ret == 0) {
                /* つながらないことはないはずであるが、念のため。 */
                break; /*NCH*/
            }
            /* 次のキューを検索する */
            que_id = njd_l_search_next_que(loc->handle, que_id);
        }

        for (j = 0; j < seg_cnt; j++) {
            /* 削除対象のqueが存在するかを確認 */
            que = njd_l_get_que(iwnn, loc->handle, del_que_id[j]);
            if (que == NULL) {
                /* 削除対象のque_idが存在しなかった     */
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_DELETE_WORD,
                                      NJ_ERR_WORD_NOT_FOUND);
            }
            /**
             * que_idを基に読み文字列、読み長、候補文字列、候補長を取得し、
             * NJ_WORD_INFOに取得した情報を設定する。
             */
            ret = create_word_info(iwnn, loc->handle, del_que_id[j], &info);
            if (ret < 0) {
                return ret; /*NCH_FB*/
            }

            /* 読み文字列を取得 */
            src_pyomi = &info.yomi[0];
            for (i = 0; i < info.stem.yomi_len; i++) {
                *(dst_pyomi + dst_ylen) = *src_pyomi;
                dst_ylen++;
                src_pyomi++;
            }
            *(dst_pyomi + dst_ylen) = NJ_CHAR_NUL;

            /* 表記文字列を取得 */
            src_phyoki = &info.kouho[0];
            for (i = 0; i < info.stem.kouho_len; i++) {
                *(dst_phyoki + dst_hlen) = *src_phyoki;
                dst_hlen++;
                src_phyoki++;
            }
            *(dst_phyoki + dst_hlen) = NJ_CHAR_NUL;

            /* 単語削除実行 */
            ret = njd_l_delete_index(iwnn, loc->handle, del_que_id[j]);
            if (ret < 0) {
                return ret; /*NCH_FB*/
            }
            if (ret == 0) {
                /* 削除対象のque_idが存在しなかった     */
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_DELETE_WORD, /*NCH_FB*/
                                      NJ_ERR_WORD_NOT_FOUND);
            }

            if (iwnn->option_data.ext_mode & NJ_OPT_CLEARED_EXTAREA_WORD) {
                /*
                 * 統合辞書の頻度学習値のクリアする。
                 */
                ret = delete_word_ext_data(iwnn, &info);
                /* エラーが発生した場合、エラーを返す */
                if (ret < 0) {
                    return ret;
                }
            }
        }

        opttype = NJ_GET_RESULT_OP(result->operation_id);
        if(((opttype == NJ_OP_CONVERT) || (opttype == NJ_OP_ANALYZE)) &&
           (GET_LEARN_WORD_COUNT(loc->handle) > 0)) {
            /* 辞書内から同読み・同表記候補を削除する */
            ret = njd_l_delete_same_word(iwnn, loc->handle, dst_pyomi, dst_ylen, dst_phyoki, dst_hlen);
            if (ret < 0) {
                return ret; /*NCH_FB*/
            }
        }
        /* 複合語予測情報バッファクリア */
        njd_l_init_cmpdg_info(iwnn);

    } else {
        if (is_commit_range(iwnn, que_id)) {
            /* 学習辞書コミット範囲のため、学習辞書操作がエラーとなる。*/
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_MAKE_SPACE, NJ_ERR_PROTECTION_ERROR); /*NCH_DEF*/
        }

        ret = njd_l_delete_index(iwnn, loc->handle, que_id);
        if (ret < 0) {
            return ret; /*NCH_FB*/
        }
        if (ret == 0) {
            /* 削除対象のque_idが存在しなかった     */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_DELETE_WORD,
                                  NJ_ERR_WORD_NOT_FOUND);
        }
    }
    return 0;
}


/**
 * キューの順番に候補を探す（currentから格納順）
 *
 * この関数が呼ばれるのは
 * - 完全一致検索(NJ_CUR_OP_COMP)のとき
 * - 前方一致検索(NJ_CUR_OP_FORE)で、かつ、候補取得順が読み順(NJ_CUR_MODE_YOMI)のとき
 * - つながり検索(NJ_CUR_OP_LINK)で、かつ、（学習辞書、かつ内部I/F）以外のとき<br>
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
static NJ_INT16 get_cand_by_sequential(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond,
                                       NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern,
                                       NJ_UINT8 comp_flg) {
    NJ_UINT16 current, from, to;
    NJ_UINT16 que_id;
    NJ_UINT8  *ptr, *p;
    NJ_INT16 ret, num_count;
    NJ_CHAR  *yomi;
    NJ_WQUE  *que;
    NJ_UINT8 forward_flag = 0;
    NJ_UINT32 version;
    NJ_UINT8  *ext_ptr;


    /* LOCATIONの現在状態の判別 */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        /* 複数キューは対象としない     */
        ret = search_range_by_yomi(iwnn, loctset->loct.handle, search_pattern,
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
    /* 辞書のバージョンを取得 */
    version = NJ_GET_DIC_VER(loctset->loct.handle);

    /* 検索するインデックスの先頭アドレスを取得 */
    ptr = get_search_index_address(loctset->loct.handle, cond->operation);
    p = ptr + (current * NJ_INDEX_SIZE);

    while (current <= loctset->loct.bottom) {
        que_id = GET_UINT16(p);
        if (search_pattern == NJ_CUR_OP_COMP) {
            /* 完全一致検索     */
            ret = str_que_cmp(iwnn, loctset->loct.handle, cond->yomi, cond->ylen, que_id, 1, 0);
            /* str_que_cmpは正常の戻り値は0, 1, 2で     */
            /* 一致した場合 ret = 1                     */
            if (ret == 2) {
                ret = 0; /*NCH*/
            }
        } else if (search_pattern == NJ_CUR_OP_FORE) {
            /* 前方一致検索     */
            ret = str_que_cmp(iwnn, loctset->loct.handle, cond->yomi, cond->ylen, que_id, 2, 0);
            /* str_que_cmpは正常の戻り値は0, 1, 2で     */
            /* 一致した場合 ret = 1                     */
            if (ret == 2) {
                ret = 0; /*NCH*/
            }
        } else if (search_pattern == NJ_CUR_OP_LINK) {
            /* つながり検索     */
            /* 読みと候補の完全一致比較         */
            /* 複数キューは対象としない         */
            ret = que_strcmp_complete_with_hyouki(iwnn, loctset->loct.handle, que_id,
                                                  cond->yomi, cond->ylen, cond->kanji, 0);
        } else if (search_pattern == NJ_CUR_OP_FORE_EXT) {
            /* 正引き前方一致拡張検索 */
            ret = str_que_cmp(iwnn, loctset->loct.handle, cond->yomi, cond->ylen, que_id, 2, 1);
            /* str_que_cmpは正常の戻り値は0, 1, 2で一致した場合 ret = 1 */
            if (ret == 2) {
                ret = 0; /*NCH*/
            }
        } else {
            /* 逆引き検索 or 正引き完全一致拡張検索 */
            ret = str_que_cmp(iwnn, loctset->loct.handle, cond->yomi, cond->ylen, que_id, 1, 1);
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
            if (search_pattern == NJ_CUR_OP_LINK) {
                /* つながり検索                 */
                /* つながりの候補があるか探す   */
                num_count = continue_cnt(iwnn, loctset->loct.handle, que_id);
                if (num_count < 0) {
                    /* 下位のエラーをそのまま返す       */
                    return num_count; /*NCH_FB*/
                }
                /* retは完全一致したキューの数                          */
                /* num_count + 1は先頭を含み、つながっているキュー数    */
                if (num_count >= ret) {
                    /* つながり検索対象のキューがあった         */
                    loctset->loct.current_info = (NJ_UINT8)(((num_count + 1) << 4) | ret);
                    loctset->loct.current = current;
                    loctset->loct.status = NJ_ST_SEARCH_READY;
                    loctset->cache_freq = get_hindo(iwnn, loctset, search_pattern);
                    if ((version == NJ_DIC_VERSION3) || (version == NJ_DIC_VERSION4)) {
                        ext_ptr = POS_TO_EXT_ADDRESS(loctset->loct.handle, que_id);
                        STATE_COPY((NJ_UINT8*)&loctset->loct.attr, ext_ptr);
                    }
                    return 1;
                }
            } else if ((search_pattern == NJ_CUR_OP_REV) ||
                       (search_pattern == NJ_CUR_OP_COMP_EXT) ||
                       (search_pattern == NJ_CUR_OP_FORE_EXT)) {

                /* 品詞チェック */
                que = get_que_allHinsi(iwnn, loctset->loct.handle, que_id);
                if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                    /* 逆引き検索のとき */
                    loctset->loct.current = current;
                    loctset->loct.status = NJ_ST_SEARCH_READY;
                    /* 複数キューは見ていないのでcurrent_infoは固定     */
                    loctset->loct.current_info = (NJ_UINT8)0x10;
                    loctset->cache_freq = get_hindo(iwnn, loctset, search_pattern);
                    if ((version == NJ_DIC_VERSION3) || (version == NJ_DIC_VERSION4)) {
                        ext_ptr = POS_TO_EXT_ADDRESS(loctset->loct.handle, que_id);
                        STATE_COPY((NJ_UINT8*)&loctset->loct.attr, ext_ptr);
                    }
                    return 1;
                }
            } else {
                /* 完全一致     */
                /* 前方一致     */

                /* 品詞チェック */
                /* str_que_cmp or que_strcmp_complete_with_hyoukiが     */
                /* 成功しているのでここではエラーは返らない             */
                que = get_que_allHinsi(iwnn, loctset->loct.handle, que_id);
                if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {

                    /* 学習辞書、非圧縮カスタマイズ辞書を検索対象とする */
                    switch (NJ_GET_DIC_TYPE_EX(loctset->loct.type, loctset->loct.handle)) {
                    case NJ_DIC_TYPE_LEARN:
                    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS:
                    case NJ_DIC_TYPE_CUSTOM_INCOMPRESS_LEARN:
                        if ((search_pattern == NJ_CUR_OP_COMP) && (comp_flg == 1)) {
                            /* 学習辞書、完全一致、内部I/Fのときのみ
                             * 連続しているキューが包含関係にあるかチェック */
                            yomi = cond->yomi + cond->ylen;
                            ret = que_strcmp_include(iwnn, loctset->loct.handle, que_id, yomi);
                            if (ret < 0) {
                                return ret;
                            }
                        }
                        break;
                    default:
                        break;
                    }
                    loctset->loct.current = current;
                    loctset->loct.status = NJ_ST_SEARCH_READY;
                    /* 連続しているキューの数を格納     */
                    loctset->loct.current_info = (ret & 0x0f) << 4;
                    loctset->cache_freq = get_hindo(iwnn, loctset, search_pattern);
                    if ((version == NJ_DIC_VERSION3) || (version == NJ_DIC_VERSION4)) {
                        ext_ptr = POS_TO_EXT_ADDRESS(loctset->loct.handle, que_id);
                        STATE_COPY((NJ_UINT8*)&loctset->loct.attr, ext_ptr);
                    }
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
 * - つながり検索(NJ_CUR_OP_LINK)で、学習辞書かつ内部I/Fから呼ばれたとき
 * - 逆引き検索(NJ_CUR_OP_REV)で、学習辞書またはユーザ辞書のとき
 * - 正引き完全一致拡張検索(NJ_CUR_OP_COMP_EXT)で、学習辞書のとき
 * - 正引き前方一致拡張検索(NJ_CUR_OP_FORE_EXT)で、学習辞書のとき
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
static NJ_INT16 get_cand_by_evaluate(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond,
                                     NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 search_pattern) {
    NJ_UINT16 from, to, i;
    NJ_UINT16 que_id, oldest;
    NJ_UINT32 max_value, eval, current;
    NJ_UINT8  *ptr, *p;
    NJ_WQUE  *que;
    NJ_INT16 ret, num_count;
    NJ_INT32 found = 0;
    NJ_UINT8 forward_flag = 0;
    NJ_UINT32 version;
    NJ_UINT8  *ext_ptr;
    NJ_INT32 is_first_search, is_better_freq;
    NJ_UINT16 que_id_tmp = 0xffff;


    /* 検索するインデックスの先頭アドレスを取得 */
    ptr = get_search_index_address(loctset->loct.handle, cond->operation);

    /* 最も古いque_idを取得する */
    oldest = GET_LEARN_NEXT_WORD_POS(loctset->loct.handle);
    /* 辞書のバージョンを取得 */
    version = NJ_GET_DIC_VER(loctset->loct.handle);

    /* LOCATIONの現在状態の判別 */
    current = 0;

    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        loctset->loct.current_info = 0x10;
    }

    if (IS_TARGET_COMPOUND_MODE(iwnn, loctset->loct.type, loctset->loct.handle, cond) &&
        ((loctset->loct.current_info & CMPDG_BIT_ON) != 0)) {
        if((loctset->loct.current_info & CMPDG_CURRENT_NUM) != 0) {
            /* 初期化 */
            loctset->loct.current_info = 0x10;
        } else {
            /* 複合語接続bit */
            loctset->loct.current_info = CMPDG_CURRENT_NUM;
            /* 複合語接続状態bit */
            loctset->loct.current_info |= CMPDG_BIT_ON;
            return 1;
        }
    }

    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        if (search_pattern == NJ_CUR_OP_LINK) {
            /* つながり検索                     */
            /* ここを通るのは学習辞書、内部I/Fのときのみ        */
            /* 複数キューを検索対象とする                       */
            ret = search_range_by_yomi_multi(iwnn, loctset->loct.handle,
                                             cond->yomi, cond->ylen, &from, &to);
        } else {
            /* 複数キューは対象としない */
            /* 前方一致検索、逆引き検索 */
            ret = search_range_by_yomi(iwnn, loctset->loct.handle, search_pattern,
                                       cond->yomi, cond->ylen, &from, &to, &forward_flag);
        }
        if (ret <= 0) {
            loctset->loct.status = NJ_ST_SEARCH_END;
            if ((ret == 0) &&
                ((search_pattern == NJ_CUR_OP_REV) ||
                 (search_pattern == NJ_CUR_OP_COMP_EXT))) {
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
            current += GET_LEARN_MAX_WORD_COUNT(loctset->loct.handle);
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
        max_value = oldest + GET_LEARN_MAX_WORD_COUNT(loctset->loct.handle);
    }

    p = ptr + (loctset->loct.top * NJ_INDEX_SIZE);
    eval = current;
    que_id = 0;
    for (i = (NJ_UINT16)loctset->loct.top; i <= (NJ_UINT16)loctset->loct.bottom; i++) {
        que_id = GET_UINT16(p);
        if (que_id < oldest) {
            eval = que_id + GET_LEARN_MAX_WORD_COUNT(loctset->loct.handle);
        } else {
            eval = que_id;
        }
        /* 頻度値のチェック */
        if (cond->mode != NJ_CUR_MODE_REGIST) {
            is_better_freq = ((eval >= max_value) && ((is_first_search) || (eval < current))) ? 1 : 0;
        } else {
            is_better_freq = ((eval <= max_value) && ((is_first_search) || (eval > current))) ? 1 : 0;
        }
        if (is_better_freq) {
            /* 条件内で最大と思しき候補を読みでチェック */
            if (search_pattern == NJ_CUR_OP_LINK) {
                /* つながり検索で複数キューを対象とする */
                ret = que_strcmp_complete_with_hyouki(iwnn, loctset->loct.handle, que_id,
                                                          cond->yomi, cond->ylen, cond->kanji, 1);
            } else if ((search_pattern == NJ_CUR_OP_REV) ||
                       (search_pattern == NJ_CUR_OP_COMP_EXT)) {
                /* 逆引き（完全一致）or 正引き完全一致拡張検索 */
                ret = str_que_cmp(iwnn, loctset->loct.handle, cond->yomi, cond->ylen, que_id, 1, 1);
                /* str_que_cmpは正常の戻り値は0, 1, 2で一致した場合 ret = 1 */
                if (ret == 2) {
                    ret = 0; /*NCH*/
                }
            } else if (search_pattern == NJ_CUR_OP_FORE_EXT) {
                /* 正引き前方一致拡張検索 */
                ret = str_que_cmp(iwnn, loctset->loct.handle, cond->yomi, cond->ylen, que_id, 2, 1);
                /* str_que_cmpは正常の戻り値は0, 1, 2で一致した場合 ret = 1 */
                if (ret == 2) {
                    ret = 0; /*NCH*/
                }
            } else {
                /* 前方一致検索     */
                ret = str_que_cmp(iwnn, loctset->loct.handle, cond->yomi, cond->ylen, que_id, 2, 0);
                /* str_que_cmpは正常の戻り値は0, 1, 2で一致した場合 ret = 1 */
                if (ret == 2) {
                    ret = 0; /*NCH*/
                }
            }
            if (ret < 0) {
                return ret; /*NCH_FB*/
            }
            if (ret >= 1) {
                if (search_pattern == NJ_CUR_OP_LINK) {
                    /* つながり検索                 */
                    /* つながりの候補があるか探す   */
                    num_count = continue_cnt(iwnn, loctset->loct.handle, que_id);
                    if (num_count < 0) {
                        /* 下位のエラーをそのまま返す       */
                        return num_count; /*NCH_FB*/
                    }
                    /* retは完全一致したキューの数                          */
                    /* num_count + 1は先頭を含み、つながっているキュー数    */
                    if (num_count >= ret) {
                        /* つながり検索対象のキューがあった         */
                        loctset->loct.current_info = (NJ_UINT8)(((num_count + 1) << 4) | ret);
                        loctset->loct.current = i;
                        if ((version == NJ_DIC_VERSION3) || (version == NJ_DIC_VERSION4)) {
                            ext_ptr = POS_TO_EXT_ADDRESS(loctset->loct.handle, que_id); /*NCH_DEF*/
                            STATE_COPY((NJ_UINT8*)&loctset->loct.attr, ext_ptr); /*NCH_DEF*/
                        }
                        max_value = eval;
                        found = 1;
                    }
                } else if ((search_pattern == NJ_CUR_OP_REV) ||
                           (search_pattern == NJ_CUR_OP_COMP_EXT) ||
                           (search_pattern == NJ_CUR_OP_FORE_EXT)) {

                    /* 品詞チェック */
                    que = get_que_allHinsi(iwnn, loctset->loct.handle, que_id);
                    if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                        /* 逆引き */
                        /* 複数キューは見ていないのでcurrent_infoは固定     */
                        loctset->loct.current_info = (NJ_UINT8)0x10;
                        loctset->loct.current = i;
                        if ((version == NJ_DIC_VERSION3) || (version == NJ_DIC_VERSION4)) {
                            ext_ptr = POS_TO_EXT_ADDRESS(loctset->loct.handle, que_id);
                            STATE_COPY((NJ_UINT8*)&loctset->loct.attr, ext_ptr);
                        }
                        max_value = eval;
                        found = 1;
                    }
                } else {
                    /* 前方一致     */

                    /* 品詞チェック */
                    /* str_que_cmp or que_strcmp_complete_with_hyoukiが     */
                    /* 成功しているのでここではエラーは返らない             */
                    que = get_que_allHinsi(iwnn, loctset->loct.handle, que_id);
                    if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                        /* 複数キューは見ていないのでcurrent_infoは固定     */
                        loctset->loct.current_info = (NJ_UINT8)0x10;
                        loctset->loct.current = i;
                        if ((version == NJ_DIC_VERSION3) || (version == NJ_DIC_VERSION4)) {
                            ext_ptr = POS_TO_EXT_ADDRESS(loctset->loct.handle, que_id);
                            STATE_COPY((NJ_UINT8*)&loctset->loct.attr, ext_ptr);
                        }
                        max_value = eval;
                        found = 1;
                        que_id_tmp = que_id;
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
        /* 複合語予測学習機能実行 */
        if ((iwnn->option_data.ext_mode & NJ_OPT_FORECAST_COMPOUND_WORD) &&
            (que_id_tmp != 0xffff)) {
            set_cmpdg_mode(iwnn, cond, loctset, que_id_tmp);
        }
        loctset->loct.status = NJ_ST_SEARCH_READY;
        loctset->cache_freq = get_hindo(iwnn, loctset, search_pattern);
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
static NJ_INT16 search_range_by_yomi(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 op, 
                                     NJ_CHAR  *yomi, NJ_UINT16 len, NJ_UINT16 *from, NJ_UINT16 *to,
                                     NJ_UINT8 *forward_flag) {
    NJ_INT16 right, mid = 0, left, max;        /* バイナリサーチ用 */
    NJ_UINT16 que_id;
    NJ_UINT8  *ptr, *p;
    NJ_CHAR  *str;
    NJ_INT16 ret = 0;
    NJ_INT32 found = 0;
    NJ_UINT8 slen;
    NJ_INT32 cmp;


    /* 検索するインデックスの先頭アドレスを取得 */
    ptr = get_search_index_address(handle, op);
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
    case NJ_CUR_OP_COMP_EXT:
    case NJ_CUR_OP_FORE_EXT:
        /* 読みの完全一致検索、または */
        /* 読み・表記の完全一致検索時 */
        /* または逆引き検索時         */
        /* 前方一致検索時             */
        /* 正引き完全一致拡張検索時   */
        /* 正引き前方一致拡張検索時   */
        break;
    default:
        return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_PARAM_OPERATION); /*NCH_FB*/
    }

    while (left <= right) {
        mid = left + ((right - left) / 2);
        p = ptr + (mid * NJ_INDEX_SIZE);
        que_id = GET_UINT16(p);
        switch (op) {
        case NJ_CUR_OP_REV:         /* 逆引き完全一致検索     */
        case NJ_CUR_OP_COMP_EXT:    /* 正引き完全一致拡張検索 */
        case NJ_CUR_OP_FORE_EXT:    /* 逆引き前方一致拡張検索 */
            str = njd_l_get_hyouki(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
            break;
        default:
            str = njd_l_get_string(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
            break;
        }
        if (str == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN);
        }

        ret = nj_strncmp(yomi, str, len);
        if ((op != NJ_CUR_OP_FORE) &&
            (op != NJ_CUR_OP_FORE_EXT)) {
            /* 読みの完全一致検索、または */
            /* 読み・表記の完全一致検索時 */
            /* または逆引き検索時         */
            if (ret == 0) {
                if ((*forward_flag == 0) && (len <= (NJ_UINT16)slen)) {
                    /* 少なくとも前方一致はしている     */
                    *forward_flag = 1;
                }
                if (len > (NJ_UINT16)slen) {
                    ret = 1; /*NCH_DEF*/
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
            if (mid <= 0) {
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

    if (mid <= 0) {
        mid = 0;
        *from = mid;
    } else {
        /* midの一つ前から読み順インデックスを線形検索 */
        p = ((mid - 1) * NJ_INDEX_SIZE) + ptr;
        /* leftの代わりにNJ_INT32のcmpを使っている      */
        for (cmp = mid - 1; cmp >= 0; cmp--) {
            que_id = GET_UINT16(p);
            switch (op) {
            case NJ_CUR_OP_REV:         /* 逆引き完全一致検索     */
            case NJ_CUR_OP_COMP_EXT:    /* 正引き完全一致拡張検索 */
            case NJ_CUR_OP_FORE_EXT:    /* 逆引き前方一致拡張検索 */
                str = njd_l_get_hyouki(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
                break;
            default:
                str = njd_l_get_string(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
                break;
            }
            if (str == NULL) {
                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN);
            }

            if ((op != NJ_CUR_OP_FORE) &&
                (op != NJ_CUR_OP_FORE_EXT)) {
                ret = nj_strncmp(yomi, str, len);
                if (ret == 0) {
                    if (len > (NJ_UINT16)slen) {
                        ret = 1; /*NCH_DEF*/
                    } else if (len < (NJ_UINT16)slen) {
                        ret = -1; /*NCH_DEF*/
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
            switch (op) {
            case NJ_CUR_OP_REV:         /* 逆引き完全一致検索     */
            case NJ_CUR_OP_COMP_EXT:    /* 正引き完全一致拡張検索 */
            case NJ_CUR_OP_FORE_EXT:    /* 逆引き前方一致拡張検索 */
                str = njd_l_get_hyouki(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
                break;
            default:
                str = njd_l_get_string(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
                break;
            }
            if (str == NULL) {
                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN);
            }

            if ((op != NJ_CUR_OP_FORE) &&
                (op != NJ_CUR_OP_FORE_EXT)) {
                ret = nj_strncmp(yomi, str, len);
                if (ret == 0) {
                    if (len > (NJ_UINT16)slen) {
                        ret = 1; /*NCH_DEF*/
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
 * 学習辞書の検索範囲を返す。
 *
 * この関数は内部I/F(njd_search_word)から
 * -学習辞書の完全一致検索
 * -つながり検索
 * を指定されたときのみ呼ばれる
 *                   
 * @param[in]    iwnn : 解析情報クラス
 * @param[in]  handle : 辞書ハンドル
 * @param[in]    yomi : 読み文字列
 * @param[in]     len : 検索対象の読み文字配列長
 * @param[out]   from : 検索範囲先頭
 * @param[out]     to : 検索範囲末尾
 *
 * @retval         <0   エラー
 * @retval          0   見付からなかった
 * @retval          1   見付かった
 */
static NJ_INT16 search_range_by_yomi_multi(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle,
                                           NJ_CHAR *yomi, NJ_UINT16 len, NJ_UINT16 *from, NJ_UINT16 *to) {
    NJ_INT16 right, mid = 0, left, max = 0;    /* バイナリサーチ用 */
    NJ_UINT16 que_id;
    NJ_UINT8  *ptr, *p;
    NJ_INT16 ret = 0;
    NJ_UINT16 comp_len;
    NJ_UINT16 i, char_len;
    NJ_INT32 found = 0;
    NJ_INT32 cmp;
    NJ_CHAR  comp_yomi[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_CHAR  *pYomi;


    /* インデックス領域の先頭アドレスを取得する         */
    /* （つながり検索なので常に読み順インデックスで良い）       */
    ptr = LEARN_INDEX_TOP_ADDR(handle);

    /* 学習領域に登録されている語彙数を取得する */
    max = GET_LEARN_WORD_COUNT(handle);


    comp_len = 0;
    pYomi = yomi;
    while (comp_len < len) {
        /* 最初は１文字目の完全一致で検索し、           */
        /* 見つからなければ、１文字ずつ伸ばして検索する */
        char_len = NJ_CHAR_LEN(pYomi);
        for (i = 0; i < char_len; i++) {
            *(comp_yomi + comp_len) = *pYomi;
            comp_len++;
            pYomi++;
        }
        *(comp_yomi + comp_len) = NJ_CHAR_NUL;

        right = max - 1;
        left = 0;
        while (left <= right) {
            mid = left + ((right - left) / 2);
            p = ptr + (mid * NJ_INDEX_SIZE);
            que_id = GET_UINT16(p);

            /* comp_len分（１〜ｎ文字目）完全一致検索   */
            ret = str_que_cmp(iwnn, handle, comp_yomi, comp_len, que_id, 1, 0);
            if (ret < 0) {
                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI_MULTI, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
            }

            if (ret == 1) {
                /* 先頭はこの場所より左にある */
                found = 1;
                break;
            } else if (ret == 0) {
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
        /* 完全一致したら抜ける */
        if (found) {
            break;
        }
    }

    if (!found) {
        /* 候補なし     */
        return 0;
    }

    /* 完全一致したので線形検索で先頭を探す     */
    if (mid == 0) {
        *from = mid;
    } else {
        /* midの一つ前から読み順インデックスを線形検索 */
        p = ((mid - 1) * NJ_INDEX_SIZE) + ptr;
        /* leftの代わりにNJ_INT32のcmpを使っている      */
        for (cmp = mid - 1; cmp >= 0; cmp--) {
            que_id = GET_UINT16(p);
            ret = str_que_cmp(iwnn, handle, comp_yomi, comp_len, que_id, 1, 0);
            if (ret < 0) {
                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI_MULTI, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
            }
            if (ret == 2) {
                break;
            }
            p -= NJ_INDEX_SIZE;
        }
        if (cmp < 0) {
            *from = 0;
        } else {
            *from = (NJ_UINT16)cmp + 1;
        }
    }


    /* 読み全体の完全一致検索で末尾を探す       */
    if ((mid + 1) >= max) {
        *to = mid;
    } else {
        /* index中の位置をpに入れる */
        p = ((mid + 1) * NJ_INDEX_SIZE) + ptr;
        /* 線形検索で一致しなくなるまで探す */
        for (right = mid + 1; right < max; right++) {
            que_id = GET_UINT16(p);
            ret = str_que_cmp(iwnn, handle, yomi, len, que_id, 1, 0);
            if (ret < 0) {
                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI_MULTI, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
            }
            if (ret == 0) {
                break;
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
static NJ_INT16 str_que_cmp(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_CHAR *yomi,
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
        hira = njd_l_get_string(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
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
    yomiByte = yomiLen * sizeof(NJ_CHAR);

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
            return 2; /*NCH_DEF*/
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
static NJ_HINDO calculate_hindo(NJ_DIC_HANDLE handle, NJ_INT32 freq, NJ_DIC_FREQ *dic_freq, NJ_INT16 freq_max, NJ_INT16 freq_min) {
    NJ_UINT16 max;
    NJ_HINDO  hindo;


    max = GET_LEARN_MAX_WORD_COUNT(handle);

    if (NJ_GET_DIC_TYPE(handle) == NJ_DIC_TYPE_LEARN) {
        /* 学習辞書 */
        if (max > 1) {
            /* 最大頻度から底上げ頻度の間で正規化 */
            hindo = CALCULATE_HINDO(freq, dic_freq->base, dic_freq->high, (max-1));
        } else {
            /* 一語のみの辞書は最大頻度固定 */
            hindo = (NJ_INT16)dic_freq->high;
        }
    } else if (NJ_GET_DIC_TYPE(handle) == NJ_DIC_TYPE_USER) {
        /* ユーザ辞書 底上げ頻度を設定 */
        hindo = (NJ_INT16)dic_freq->base;
    } else {
        /* 非圧縮カスタマイズ辞書 */
        if (max > 1) {
            /* 最大頻度から底上げ頻度の間で正規化 */
            hindo = CALCULATE_HINDO(freq, dic_freq->base, dic_freq->high, (max-1));
        } else {
            /* 一語のみの辞書は最大頻度固定 */
            hindo = (NJ_INT16)dic_freq->high;
        }
    }
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
static NJ_HINDO get_hindo(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset,
                          NJ_UINT8 search_pattern) {
    NJ_WQUE   *que;
    NJ_UINT16 que_id, oldest;
    NJ_UINT8  offset;
    NJ_INT32  dic_freq;
    NJ_UINT16 max;
    NJ_UINT8  *learn_index_top_addr;


    /* 検索するインデックスの先頭アドレスを取得 */
    learn_index_top_addr = get_search_index_address(loctset->loct.handle, search_pattern);

    que_id = (NJ_UINT16)GET_UINT16(learn_index_top_addr +
                                   ((loctset->loct.current & 0xffffU) * NJ_INDEX_SIZE));
    oldest = GET_LEARN_NEXT_WORD_POS(loctset->loct.handle);

    offset = (loctset->loct.current_info & 0x0f);
    while (offset--) {
        que_id = njd_l_search_next_que(loctset->loct.handle, que_id);
    }

    que = njd_l_get_que(iwnn, loctset->loct.handle, que_id);
    if (que == NULL) {
        return INIT_HINDO; /*NCH_FB*/
    }

    max = GET_LEARN_MAX_WORD_COUNT(loctset->loct.handle);
    if (que_id >= oldest) {
        dic_freq = que_id - oldest;
    } else {
        dic_freq = que_id - oldest + max;
    }

    /* キュー格納位置、辞書頻度から頻度を算出する */
    return calculate_hindo(loctset->loct.handle, dic_freq, &(loctset->dic_freq), loctset->dic_freq_max, loctset->dic_freq_min);
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
NJ_INT16 njd_l_get_word(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word) {
    NJ_WQUE *que;
    NJ_UINT16 que_id;
    NJ_UINT16 que_id_bak;
    NJ_UINT8 offset;
    NJ_UINT8 *learn_index_top_addr;
    NJ_INT8   i,j;        /* カウンタ */
    NJ_INT16  yomi_len;   /* queのyomi_len */


    /* 検索するインデックスの先頭アドレスを取得 */
    learn_index_top_addr = get_search_index_address(loctset->loct.handle, GET_LOCATION_OPERATION(loctset->loct.status));

    que_id = (NJ_UINT16)GET_UINT16(learn_index_top_addr +
                                   ((loctset->loct.current & 0xffff) * NJ_INDEX_SIZE));

    offset = (loctset->loct.current_info & 0x0f);
    while (offset--) {
        que_id = njd_l_search_next_que(loctset->loct.handle, que_id);
    }

    que = njd_l_get_que(iwnn, loctset->loct.handle, que_id);
    if (que == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_GET_WORD, NJ_ERR_CANNOT_GET_QUE); /*NCH_FB*/
    }
    yomi_len = que->yomi_len;
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

    if ((GET_LOCATION_OPERATION(loctset->loct.status) == NJ_CUR_OP_COMP_EXT) ||
        (GET_LOCATION_OPERATION(loctset->loct.status) == NJ_CUR_OP_FORE_EXT)) {
        /* 拡張検索時は、拡張検索用読み文字列長に読み文字列長をセット */
        NJ_SET_EXT_YLEN_TO_STEM(word, que->yomi_len);
    } else {
        NJ_SET_EXT_YLEN_TO_STEM(word, 0);
    }

    /* 擬似候補の種類をクリア   */
    word->stem.type = 0;

    if(!HAS_COMPOUND_STATUS(word)) {
        /* 複合語となり得る候補ではない為、処理完了とする */
        return 1;
    }

    if (((word->stem.loc.current_info & CMPDG_BIT_ON) != 0) && 
        ((word->stem.loc.current_info & CMPDG_CURRENT_NUM) != 0)) {
        /* current_infoを複合語用→通常用に設定 */
        word->stem.loc.current_info = CMPDG_CURRENT_NUM;

        /* 結合する後部語句のqueを取得 */
        que_id_bak = que_id;
        que_id = njd_l_search_next_que(word->stem.loc.handle, que_id);
        que = njd_l_get_que(iwnn, word->stem.loc.handle, que_id);
        if (que == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_GET_WORD, NJ_ERR_CANNOT_GET_QUE); /*NCH_FB*/
        }

        /* 複合語の後部語句の後品詞を設定 */
        NJ_SET_BPOS_TO_STEM(word, que->ato_hinsi);

        /* 複合語の後部語句の読み長、表記長を設定 */
        NJ_SET_YLEN_TO_STEM(word, (NJ_GET_YLEN_FROM_STEM(word) + que->yomi_len));
        if (que->hyouki_len > 0) {
            NJ_SET_KLEN_TO_STEM(word, (NJ_GET_KLEN_FROM_STEM(word) + que->hyouki_len));
        } else {
            /* 無変換候補の場合は読みの候補数   */
            NJ_SET_KLEN_TO_STEM(word, (NJ_GET_KLEN_FROM_STEM(word) + que->yomi_len));
        }

        /* 複合語予測情報バッファ空き領域確認 */
        if (iwnn->cmpdg_info.add_count >= NJ_MAX_NEXT_CMPDG_RESULTS) {
            /* 追加用バッファはMAXの為、追加不要 */
            return 1;
        }

        /* 複合語予測結果保持バッファ空き確認 */
        for (i = 0; i < NJ_MAX_CMPDG_RESULTS; i++) {
            if (iwnn->cmpdg_info.cmpdg_data[i].index == NJ_CMPDG_BLANK_INDEX) {
                /* 空きが確認できた場合、処理を抜ける */
                break;
            }
        }

        if (i != NJ_MAX_CMPDG_RESULTS) {
            /* 複合語予測結果保持情報の重複確認 */
            for (j = 0; j < NJ_MAX_CMPDG_RESULTS; j++) {
                /* BLANKとなっていない保持領域全てから(next/refer共に同じque_idがないか確認) */
                if (iwnn->cmpdg_info.cmpdg_data[j].index != NJ_CMPDG_BLANK_INDEX) {
                    que_id = (NJ_UINT16)((iwnn->cmpdg_info.cmpdg_data[j].result.word.stem.loc.current >> 16) & (0x0000ffff));
                    if (que_id == que_id_bak) {
                        /* 同一のresultが既に格納済みの為、追加なし */
                        return 1;
                    }
                }
            }

            /* 追加可能な場合、データを登録 */
            iwnn->cmpdg_info.cmpdg_data[i].yomi_toplen = (NJ_UINT8)yomi_len;
            iwnn->cmpdg_info.cmpdg_data[i].yomi_len = NJ_GET_YLEN_FROM_STEM(word);
            iwnn->cmpdg_info.cmpdg_data[i].result.word = *word;
            iwnn->cmpdg_info.add_count++;
            iwnn->cmpdg_info.cmpdg_data[i].index = (NJ_UINT8)iwnn->cmpdg_info.add_count;
            iwnn->cmpdg_info.cmpdg_data[i].status = NJ_CMPDG_STATUS_ADD;
        }
    } else if ((word->stem.loc.current_info & CMPDG_BIT_ON) != 0) {
        /* current_infoを複合語用→通常用に設定 */
        word->stem.loc.current_info = 0x10;
    }

    return 1;
}


/**
 * 単語検索njd_search_wordで取得した検索結果から、関連候補情報を取得する。
 *
 * @note 検索カーソルは移動せず、現位置の関連候補を返す
 *
 * @param[in]       iwnn : 解析情報クラス
 * @param[in]        loc : 検索位置
 * @param[out]      word : 単語情報
 * @param[in]  mdic_freq : 辞書頻度情報
 *
 * @retval             1   正常
 * @retval            <0   エラー
 */
NJ_INT16 njd_l_get_relational_word(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION *loc,
                                   NJ_WORD *word, NJ_DIC_FREQ *mdic_freq) {
    NJ_INT32 dic_freq = 0;
    NJ_UINT16 max;
    NJ_UINT16 que_id, oldest;
    NJ_WQUE *que;


    oldest = GET_LEARN_NEXT_WORD_POS(loc->handle);

    /* 現在のque_id取得         */
    que_id = (NJ_UINT16)((word->stem.loc.current >> 16) & (0x0000ffff));
    /* que_idを一つ進める       */
    que_id = njd_l_search_next_que(loc->handle, que_id);
    que = njd_l_get_que(iwnn, loc->handle, que_id);
    if (que == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_GET_RELATIONAL_WORD, /*NCH_FB*/
                              NJ_ERR_CANNOT_GET_QUE);
    }

    /* que_idを更新                             */
    /* offsetのチェックと更新は上位で実施済み   */
    word->stem.loc.current &= 0x0000ffff;
    word->stem.loc.current |= ((NJ_UINT32)que_id << 16);
    
    NJ_SET_FPOS_TO_STEM(word, que->mae_hinsi);
    NJ_SET_BPOS_TO_STEM(word, que->ato_hinsi);
    max = GET_LEARN_MAX_WORD_COUNT(loc->handle);
    if (que_id >= oldest) {
        dic_freq = que_id - oldest;
    } else {
        dic_freq = que_id - oldest + max;
    }

    /* キュー格納位置、辞書頻度から評価値を算出する */
    word->stem.hindo = (NJ_INT16)calculate_hindo(loc->handle, dic_freq, mdic_freq, NJ_STATE_MAX_FREQ, NJ_STATE_MIN_FREQ);

    /* 読み長のみ格納する       */
    /* 読みは格納しない         */
    NJ_SET_YLEN_TO_STEM(word, que->yomi_len);

    /* 表記長のみ格納する       */
    if (que->hyouki_len == 0) {
        /* 無変換候補なので、読み長を候補長とする */
        NJ_SET_KLEN_TO_STEM(word, que->yomi_len);
    } else {
        NJ_SET_KLEN_TO_STEM(word, que->hyouki_len);
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
NJ_INT16 njd_l_get_stroke(NJ_CLASS *iwnn, NJ_WORD *word, NJ_CHAR *stroke, NJ_UINT16 size) {
    NJ_UINT16 que_id;
    NJ_CHAR   *str;
    NJ_UINT8  slen;
    NJ_UINT8  ylen;
    NJ_UINT8  segment_num;



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


    /* 複合語結合を行う状態かかどうか判定 */
    if (IS_COMPOUND_WORD(word)) {
        segment_num = NJ_NUM_SEGMENT2;
    } else {
        segment_num = NJ_NUM_SEGMENT1;
    }

    str = njd_l_get_string(iwnn, word->stem.loc.handle, que_id, &slen, segment_num);

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
NJ_INT16 njd_l_get_candidate(NJ_CLASS *iwnn, NJ_WORD *word,
                             NJ_CHAR *candidate, NJ_UINT16 size) {
    NJ_UINT16 que_id;
    NJ_CHAR   *str;
    NJ_UINT16 klen;
    NJ_UINT8  slen;
    NJ_UINT8 segment_num;



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

    /* 複合語結合を行う状態かかどうか判定 */
    if (IS_COMPOUND_WORD(word)) {
        segment_num = NJ_NUM_SEGMENT2;
    } else {
        segment_num = NJ_NUM_SEGMENT1;
    }

    str = njd_l_get_hyouki(iwnn, word->stem.loc.handle, que_id, &slen, segment_num);

    if (str == NULL) {
        /* 表記取得エラー       */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_GET_CANDIDATE, NJ_ERR_DIC_BROKEN);
    }

    /* 文字列をコピー  */
    nj_strcpy(candidate, str);

    return klen;
}


/**
 *      指定回数分だけ学習アンドゥを行う
 *
 * @param[in]       iwnn : 解析情報クラス
 * @param[in] undo_count : アンドゥ回数
 *
 * @retval            <0   エラー
 * @retval           >=0   アンドゥできた回数
 */
NJ_INT16 njd_l_undo_learn(NJ_CLASS *iwnn, NJ_UINT16 undo_count) {
    NJ_UINT16 que_id;
    NJ_UINT16 word_count;
    NJ_INT16 ret;
    NJ_UINT8 *ptr;
    NJ_INT16 i;
    NJ_DIC_HANDLE handle = NULL;
    NJ_INT16 ret_cnt = 0;                       /* アンドゥできた回数   */
    NJ_DIC_SET *dics;


    /* アンドゥ回数=0なら正常終了       */
    if (undo_count == 0) {
        return 0;
    }
    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_UNDO_LEARN, NJ_ERR_PARAM_ENV_NULL); /*NCH_FB*/
    }
    dics = &(iwnn->dic_set);

    /* 辞書セットから学習辞書を探す     */
    for (i=0; i<NJ_MAX_DIC; i++) {
        if (dics->dic[i].handle == NULL) {
            continue;
        }
        if (NJ_GET_DIC_TYPE_EX(dics->dic[i].type, dics->dic[i].handle) == NJ_DIC_TYPE_LEARN) {
            handle = dics->dic[i].handle;
            break;
        }
    }
    if (handle == NULL) {
        /* 学習辞書無し */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_UNDO_LEARN, NJ_ERR_DIC_NOT_FOUND); /*NCH_FB*/
    }

    /* 現在登録数を取得する             */
    /* （UNDOなので登録語数で良い）     */
    word_count = GET_LEARN_WORD_COUNT(handle);
    if (word_count == 0) {
        /* 登録語数=0、                                                 */
        /*（指定回数分アンドゥできなくてもエラー扱いにしない）          */
        return 0;
    }
    /* 辞書ヘッダの「次キュー追加位置」 */
    que_id = GET_LEARN_NEXT_WORD_POS(handle);
    while (undo_count) {
        que_id = njd_l_search_prev_que(handle, que_id);
        ptr = POS_TO_ADDRESS(handle, que_id);
        /* アンドゥフラグが見付かったらカウントを減算   */
        if (GET_UFLG_FROM_DATA(ptr)) {
            undo_count--;
            ret_cnt++;
        }
        /* インデックスから削除 */
        ret = njd_l_delete_index(iwnn, handle, que_id);
        if (ret < 0) {
            /* インデックス削除時に何らかのエラーが発生した */
            return ret; /*NCH_FB*/
        }
        /* 次キュー追加位置更新 */
        njd_l_write_uint16_data(handle + POS_NEXT_QUE + 2, que_id);
        word_count--;
        if (word_count == 0) {
            break;
        }
    }
    return ret_cnt;
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
NJ_INT16 njd_l_check_dic(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT8 restore) {
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
        if (restore == 0) { /*NCH_DEF*/
            /* 復旧指定がされてなければreturn   */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC, NJ_ERR_DIC_BROKEN); /*NCH_DEF*/
        }
        bk_point = 0xffff; /*NCH_DEF*/
        /* 読み順インデックス領域の先頭アドレスを取得する */
        ptr = LEARN_INDEX_TOP_ADDR(handle); /*NCH_DEF*/
        for (i=0; i < word_cnt; i++, ptr += NJ_INDEX_SIZE) { /*NCH_DEF*/
            id2 = GET_UINT16(ptr); /*NCH_DEF*/
            /* 挿入対象キューがあった   */
            if (id2 == target_id) { /*NCH_DEF*/
                /* 表記文字列インデックスチェックへ */
                finish_flg = 1; /*NCH_DEF*/
                break; /*NCH_DEF*/
            }
            if ((i > 0) && (id1 == id2)) { /*NCH_DEF*/
                /* 重複IDがあった       */
                bk_point = i - 1; /*NCH_DEF*/
                break; /*NCH_DEF*/
            }
            id1 = id2; /*NCH_DEF*/
        }

        /* 挿入処理が終了していないとき */
        if (finish_flg == 0) { /*NCH_DEF*/
            if (bk_point != 0xffff) { /*NCH_DEF*/
                /* 重複IDがあった                                               */
                /* 重複位置以降のデータ（登録語数＋１まで）を一つ前に詰める     */
                shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(word_cnt - bk_point)); /*NCH_DEF*/
            } else {

                /* 表記文字列インデックスの削除対象位置を検索 */
                ptr = LEARN_INDEX_TOP_ADDR2(handle); /*NCH_DEF*/
                for (i=0; i < word_cnt; i++, ptr += NJ_INDEX_SIZE) { /*NCH_DEF*/
                    id2 = GET_UINT16(ptr); /*NCH_DEF*/
                    /* 挿入対象キューがあった   */
                    if (id2 == target_id) { /*NCH_DEF*/
                        shift_index((ptr + NJ_INDEX_SIZE), SHIFT_LEFT, (NJ_UINT16)(word_cnt - i)); /*NCH_DEF*/
                        finish_flg = 1; /*NCH_DEF*/ /* 挿入処理が終了している       */
                        break; /*NCH_DEF*/
                    }
                    if ((i > 0) && (id1 == id2)) { /*NCH_DEF*/
                        /* 重複IDがあった       */
                        bk_point = i - 1; /*NCH_DEF*/
                        break; /*NCH_DEF*/
                    }
                    id1 = id2; /*NCH_DEF*/
                }

                /* 表記インデックスに挿入対象キューが存在しないとき */
                if (finish_flg == 0) { /*NCH_DEF*/
                    if (bk_point != 0xffff) { /*NCH_DEF*/
                        /* 重複IDがあった                                               */
                        /* 重複位置以降のデータ（登録語数＋１まで）を一つ前に詰める     */
                        shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(word_cnt - bk_point)); /*NCH_DEF*/
                    }
                }
            }

            /* 挿入対象キューを未使用にする     */
            ptr = POS_TO_ADDRESS(handle, target_id); /*NCH_DEF*/
            *ptr = QUE_TYPE_EMPTY; /*NCH_DEF*/
            /* 登録語数を電断用flgの上位2byteにコピーする       */
            COPY_UINT16(handle + POS_WRITE_FLG, /*NCH_DEF*/
                        handle + POS_LEARN_WORD + 2); /*NCH_DEF*/
        } else {
            bk_point = 0xffff; /*NCH_DEF*/
            finish_flg = 0; /*NCH_DEF*/
            /* 表記文字列インデックス領域の先頭アドレスを取得する */
            ptr = LEARN_INDEX_TOP_ADDR2(handle); /*NCH_DEF*/
            for (i=0; i < word_cnt; i++, ptr += NJ_INDEX_SIZE) { /*NCH_DEF*/
                id2 = GET_UINT16(ptr); /*NCH_DEF*/
                /* 挿入対象キューがあった       */
                if (id2 == target_id) { /*NCH_DEF*/
                    /* ロールフォワード                                 */
                    /* 登録語数更新                                     */
                    /* 登録語数に電断用flgの上位2byteをコピーする       */
                    COPY_UINT16(handle + POS_LEARN_WORD + 2, /*NCH_DEF*/
                                handle + POS_WRITE_FLG); /*NCH_DEF*/
                    finish_flg = 1; /*NCH_DEF*/     /* 挿入処理が終了している       */
                    break; /*NCH_DEF*/
                }
                if ((i > 0) && (id1 == id2)) { /*NCH_DEF*/
                    /* 重複IDがあった   */
                    bk_point = i - 1; /*NCH_DEF*/
                    break; /*NCH_DEF*/
                }
                id1 = id2; /*NCH_DEF*/
            }

            /* 挿入処理が終了していないとき */
            if (finish_flg == 0) { /*NCH_DEF*/
                if (bk_point != 0xffff) { /*NCH_DEF*/
                    /* 重複IDがあった                                           */
                    /* 重複位置以降のデータ（登録語数＋１まで）を一つ前に詰める */
                    shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(word_cnt - bk_point)); /*NCH_DEF*/
                }

                /* 読み文字列インデックスの削除対象位置を検索 */
                ptr = LEARN_INDEX_TOP_ADDR(handle); /*NCH_DEF*/
                for (i=0; i < word_cnt; i++) { /*NCH_DEF*/
                    id2 = GET_UINT16(ptr); /*NCH_DEF*/
                    ptr += NJ_INDEX_SIZE; /*NCH_DEF*/
                    /* キューが一致 */
                    if (id2 == target_id) { /*NCH_DEF*/
                        shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(word_cnt - i)); /*NCH_DEF*/
                    }
                }

                /* 挿入対象キューを未使用にする */
                ptr = POS_TO_ADDRESS(handle, target_id); /*NCH_DEF*/
                *ptr = QUE_TYPE_EMPTY; /*NCH_DEF*/
                /* 登録語数を電断用flgの上位2byteにコピーする   */
                COPY_UINT16(handle + POS_WRITE_FLG, /*NCH_DEF*/
                            handle + POS_LEARN_WORD + 2); /*NCH_DEF*/
            }
        }
    } else if (flg == (word_cnt - 1)) { /*NCH_DEF*/
        /*
         * 削除処理中に電断したと見なして
         * 復旧処理を行う
         */
        if (restore == 0) { /*NCH_DEF*/
            /* 復旧指定がされてなければreturn   */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_CHECK_DIC, NJ_ERR_DIC_BROKEN); /*NCH_DEF*/
        }
        bk_point = 0xffff; /*NCH_DEF*/
        /* 読み順インデックス領域の先頭アドレスを取得する */
        ptr = LEARN_INDEX_TOP_ADDR(handle); /*NCH_DEF*/
        for (i = 0; i < word_cnt; i++, ptr += NJ_INDEX_SIZE) { /*NCH_DEF*/
            id2 = GET_UINT16(ptr); /*NCH_DEF*/
            if (id2 == target_id) { /*NCH_DEF*/
                /* 削除対象キューがあった       */
                bk_point = i; /*NCH_DEF*/
                ptr += NJ_INDEX_SIZE; /*NCH_DEF*/
                break; /*NCH_DEF*/
            }
            if ((i > 0) && (id1 == id2)) { /*NCH_DEF*/
                /* 重複IDがあった       */
                bk_point = i - 1; /*NCH_DEF*/
                break; /*NCH_DEF*/
            }
            id1 = id2; /*NCH_DEF*/
        }
        if (bk_point != 0xffff) { /*NCH_DEF*/
            shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(word_cnt - bk_point - 1)); /*NCH_DEF*/
        }

        bk_point = 0xffff; /*NCH_DEF*/
        /* 表記文字列インデックス領域の先頭アドレスを取得する */
        ptr = LEARN_INDEX_TOP_ADDR2(handle); /*NCH_DEF*/
        for (i = 0; i < word_cnt; i++, ptr += NJ_INDEX_SIZE) { /*NCH_DEF*/
            id2 = GET_UINT16(ptr); /*NCH_DEF*/
            if (id2 == target_id) { /*NCH_DEF*/
                /* 削除対象キューがあった       */
                bk_point = i; /*NCH_DEF*/
                ptr += NJ_INDEX_SIZE; /*NCH_DEF*/
                break; /*NCH_DEF*/
            }
            if ((i > 0) && (id1 == id2)) { /*NCH_DEF*/
                /* 重複IDがあった       */
                bk_point = i - 1; /*NCH_DEF*/
                break; /*NCH_DEF*/
            }
            id1 = id2; /*NCH_DEF*/
        }
        if (bk_point != 0xffff) { /*NCH_DEF*/
            shift_index(ptr, SHIFT_LEFT, (NJ_UINT16)(word_cnt - bk_point - 1)); /*NCH_DEF*/
        }

        /* 削除対象キューを未使用にする */
        ptr = POS_TO_ADDRESS(handle, target_id); /*NCH_DEF*/
        *ptr = QUE_TYPE_EMPTY; /*NCH_DEF*/
        /* 登録語数更新                                 */
        /* 登録語数に電断用flgの下位2byteをコピーする   */
        COPY_UINT16(handle + POS_LEARN_WORD + 2, /*NCH_DEF*/
                    handle + POS_WRITE_FLG); /*NCH_DEF*/
    }

    word_cnt = GET_LEARN_WORD_COUNT(handle);

    /* 復旧後チェック                   */
    /* 読み順インデックスに格納されているキューIDの
     * 読みと候補が取得できるかチェックする     */
    ptr = LEARN_INDEX_TOP_ADDR(handle);
    for (i = 0; i < word_cnt; i++) {
        id1 = GET_UINT16(ptr);
        if (njd_l_get_hyouki(iwnn, handle, id1, &slen, NJ_NUM_SEGMENT1) == NULL) {
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
NJ_INT16 njd_l_init_area(NJ_DIC_HANDLE handle) {
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
 * 学習辞書の空き領域を確保する
 *
 * (1) count数分の空き領域を確保する
 * (2) 制限値を超える同読みの学習を削除する
 *
 * @param[in] iwnn  : 解析情報クラス
 * @param[in] count : 必要なキューの数
 * @param[in] mode  : 0:上記(2)の処理のみ行う<br>
 *                    1:上記(1)(2)の処理を両方行う
 *
 * @retval        0   正常終了
 * @retval       <0   エラー
 */
NJ_INT16 njd_l_make_space(NJ_CLASS *iwnn, NJ_UINT16 count, NJ_UINT8 mode) {
    NJ_DIC_HANDLE handle = NULL;
    NJ_INT16 i;
    NJ_UINT16 j;
    NJ_UINT16 max;
    NJ_UINT16 que_id;
    NJ_INT16 ret;
    NJ_UINT8 *ptr;
    NJ_UINT8 *index_ptr;
    NJ_UINT16 word_cnt;
    NJ_UINT8 slen;
    NJ_INT16 same_cnt = 0;
    NJ_CHAR  *str;
    NJ_CHAR  str1[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_DIC_SET *dics = &(iwnn->dic_set);


    /* 辞書セットから学習辞書を探す     */
    for (i = 0; i < NJ_MAX_DIC; i++) {
        if (dics->dic[i].handle == NULL) {
            continue;
        }
        if (NJ_GET_DIC_TYPE_EX(dics->dic[i].type, dics->dic[i].handle) == NJ_DIC_TYPE_LEARN) {
            handle = dics->dic[i].handle;
            break;
        }
    }
    /* 学習辞書がないときは正常終了とする       */
    if (handle == NULL) {
        return 0;
    }

    /* 登録語数0ならreturn                      */
    /* （学習辞書限定なので登録語数で良い）     */
    if (GET_LEARN_WORD_COUNT(handle) == 0) {
        return 0;
    }

    if (mode == 1) {
        /*
         * 最も古いキューを削除する
         */
        max = GET_LEARN_MAX_WORD_COUNT(handle);
        /* 次キュー追加位置から消す     */
        que_id = GET_LEARN_NEXT_WORD_POS(handle);

        for (j = 0; j < count; j++) {

            if (is_commit_range(iwnn, que_id)) {
                /* 学習辞書コミット範囲のため、学習辞書操作がエラーとなる。*/
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_MAKE_SPACE, NJ_ERR_PROTECTION_ERROR); /*NCH_DEF*/
            }

            ptr = POS_TO_ADDRESS(handle, que_id);
            if (GET_TYPE_FROM_DATA(ptr) != QUE_TYPE_EMPTY) {
                ret = njd_l_delete_index(iwnn, handle, que_id);
                if (ret < 0) {
                    return ret; /*NCH_FB*/
                }
            }
            que_id++;
            if (que_id >= max) {
                que_id = 0;
            }
        }
    }

    /* 
     * 同じ読みが制限個数を超えているものを削除する
     */
    /* 登録語数                                 */
    /* （学習辞書限定なので登録語数で良い）     */
    word_cnt = GET_LEARN_WORD_COUNT(handle);
    /* 読み順インデックス領域の先頭アドレスを取得する           */
    /* （読みのチェックなので常に読み順インデックスで良い）     */
    index_ptr = LEARN_INDEX_TOP_ADDR(handle);
    str1[0] = NJ_CHAR_NUL;

    /* インデックス順に登録語数分見る   */
    for (j = 0; j < word_cnt; j++) {
        str = njd_l_get_string(iwnn, handle, (NJ_UINT16)GET_UINT16(index_ptr), &slen, NJ_NUM_SEGMENT1);
        if (str == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_MAKE_SPACE, NJ_ERR_DIC_BROKEN);
        }
        if (nj_strcmp(str, str1) == 0) {
            same_cnt++;
            if (same_cnt > NJD_SAME_INDEX_LIMIT) {
                que_id = (NJ_UINT16)GET_UINT16(index_ptr);
                if (is_commit_range(iwnn, que_id)) {
                    /* 学習辞書コミット範囲のため、学習辞書操作がエラーとなる。*/
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_MAKE_SPACE, NJ_ERR_PROTECTION_ERROR); /*NCH_DEF*/
                }

                ret = njd_l_delete_index(iwnn, handle, que_id);
                if (ret < 0) {
                    return ret; /*NCH_FB*/
                }
                continue;
            }
        } else {
            same_cnt = 1;
            nj_strcpy(str1, str);
        }
        index_ptr += NJ_INDEX_SIZE;
    }

    return 0;
}




/**
 * 評価値順に候補を探す(キャッシュ検索用)
 *
 * この関数が呼ばれるのは
 * - 前方一致検索(NJ_CUR_OP_FORE)で、かつ、候補取得順が頻度順(NJ_CUR_MODE_FREQ)のとき
 * - 正引き前方一致拡張検索(NJ_CUR_OP_FORE_EXT)で、かつ、候補取得順が頻度順(NJ_CUR_MODE_FREQ)のとき
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
static NJ_INT16 get_cand_by_evaluate2(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond,
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
    NJ_UINT16 que_id_tmp  = 0xffff;
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
    NJ_UINT32               version;
    NJ_UINT8               *ext_ptr;
#ifdef NJ_OPT_CHARSET_2BYTE
    NJ_UINT16 start;
    NJ_UINT16 end;
#endif /* NJ_OPT_CHARSET_2BYTE */


    if (NJ_GET_CACHEOVER_FROM_SCACHE(psrhCache)) {
        aimai_flg = 0x00;
    }

    /* 検索するインデックスの先頭アドレスを取得 */
    ptr = get_search_index_address(loctset->loct.handle, cond->operation);

    /* 辞書のバージョンを取得 */
    version = NJ_GET_DIC_VER(loctset->loct.handle);

    /* 最も古いque_idを取得する */
    oldest = GET_LEARN_NEXT_WORD_POS(loctset->loct.handle);
    max_value = oldest;

    /* LOCATIONの現在状態の判別 */
    current = 0;

    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        loctset->loct.current_info = 0x10;
    }

    if (IS_TARGET_COMPOUND_MODE(iwnn, loctset->loct.type, loctset->loct.handle, cond) &&
        ((loctset->loct.current_info & CMPDG_BIT_ON) != 0)) {
        if ((loctset->loct.current_info & CMPDG_CURRENT_NUM) != 0) {
            /* 初期化 */
            loctset->loct.current_info = 0x10;
        } else {
            /* 複合語接続bit */
            loctset->loct.current_info = CMPDG_CURRENT_NUM;
            /* 複合語接続状態bit */
            loctset->loct.current_info |= CMPDG_BIT_ON;
            return 1;
        }
    }

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
                    ret = search_range_by_yomi(iwnn, loctset->loct.handle, search_pattern,
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
                                ret = search_range_by_yomi(iwnn, loctset->loct.handle, search_pattern,
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
                                        psrhCache->keyPtr[j+1] = 0; /*NCH_DEF*/
                                        return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_NOT_ENOUGH); /*NCH_DEF*/
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
                            if (search_pattern != NJ_CUR_OP_FORE_EXT) {
                                str = njd_l_get_string(iwnn, loctset->loct.handle, que_id, &slen, NJ_NUM_SEGMENT1);
                            } else {
                                str = njd_l_get_hyouki(iwnn, loctset->loct.handle, que_id, &slen, NJ_NUM_SEGMENT1);
                            }

                            if (str == NULL) {
                                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
                            }

                            /* 必要読み文字列数分取得する */
                            nj_strncpy(char_tmp, str, psrhCache->storebuff[m].idx_no);
                            char_tmp[psrhCache->storebuff[m].idx_no] = NJ_CHAR_NUL;

                            pchar_tmp = &char_tmp[psrhCache->storebuff[m].idx_no];
                            nj_charncpy(pchar_tmp, yomi, 1);
                            tmp_len = nj_strlen(char_tmp);


                            ret = search_range_by_yomi2(iwnn, loctset, search_pattern,
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
                                        ret = search_range_by_yomi2(iwnn, loctset, search_pattern,
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
                                                psrhCache->keyPtr[j+1] = 0; /*NCH_DEF*/
                                                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_NOT_ENOUGH); /*NCH_DEF*/
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
            current += GET_LEARN_MAX_WORD_COUNT(loctset->loct.handle);
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
                    eval = que_id + GET_LEARN_MAX_WORD_COUNT(loctset->loct.handle);
                } else {
                    eval = que_id;
                }
                if (eval >= max_value) {
                    if ((GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT)
                        || ((GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_READY)
                            && (NJ_GET_AIMAI_FROM_SCACHE(psrhCache)))
                        || (eval < current)) {

                        /* QueIdから文字列を取得する */
                        /* Queのエラーチェックのため、ダミーで文字列取得処理を行う */
                        if (search_pattern != NJ_CUR_OP_FORE_EXT) {
                            str = njd_l_get_string(iwnn, loctset->loct.handle, que_id, &slen, NJ_NUM_SEGMENT1);
                        } else {
                            str = njd_l_get_hyouki(iwnn, loctset->loct.handle, que_id, &slen, NJ_NUM_SEGMENT1);
                        }
                        if (str == NULL) {
                            return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
                        }

                        /* 前方一致検索     */
                        /* 品詞チェック */
                        que = get_que_allHinsi(iwnn, loctset->loct.handle, que_id);
                        if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                            /* 複数キューは見ていないのでcurrent_infoは固定     */
                            loctset->loct.current_info = (NJ_UINT8)0x10;
                            loctset->loct.current = i;
                            if ((version == NJ_DIC_VERSION3) || (version == NJ_DIC_VERSION4)) {
                                ext_ptr = POS_TO_EXT_ADDRESS(loctset->loct.handle, que_id);
                                STATE_COPY((NJ_UINT8*)&loctset->loct.attr, ext_ptr);
                            }
                            max_value = eval;
                            found = 1;
                            que_id_tmp = que_id;
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
        /* あいまい検索合致時は複合語処理はスキップ */
        if ((GET_LOCATION_STATUS(loctset->loct.status) != NJ_ST_SEARCH_NO_INIT) &&
            (iwnn->option_data.ext_mode & NJ_OPT_FORECAST_COMPOUND_WORD)) {
            /* 複合語予測学習機能実行 */
            if (que_id_tmp != 0xffff) {
                set_cmpdg_mode(iwnn, cond, loctset, que_id_tmp);
            }
        }
        loctset->loct.status = NJ_ST_SEARCH_READY;
        loctset->cache_freq = get_hindo(iwnn, loctset, search_pattern);
        return 1;
    }
}


/**
 *      検索条件により指定された読みと一致する範囲を2分検索する。
 *                   
 * @param[in]          iwnn : 解析情報クラス
 * @param[in]       loctset : 検索位置
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
static NJ_INT16 search_range_by_yomi2(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset, NJ_UINT8 op, 
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
    NJ_DIC_HANDLE handle = loctset->loct.handle;


    /* 検索するインデックスの先頭アドレスを取得 */
    ptr = get_search_index_address(handle, op);
    /* 学習領域に登録されている語彙数を取得する */
    max = GET_LEARN_WORD_COUNT(handle);

    right = sto;
    left = sfrom;


    *forward_flag = 0;

    while (left <= right) {
        mid = left + ((right - left) / 2);
        p = ptr + (mid * NJ_INDEX_SIZE);
        que_id = GET_UINT16(p);
        if (op != NJ_CUR_OP_FORE_EXT) {
            str = njd_l_get_string(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
        } else {
            str = njd_l_get_hyouki(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
        }
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
        /* midの一つ前から読み順インデックスを線形検索(拡張検索の場合は、候補順インデックス) */
        p = ((mid - 1) * NJ_INDEX_SIZE) + ptr;
        /* leftの代わりにNJ_INT32のcmpを使っている      */
        for (cmp = mid - 1; cmp >= 0; cmp--) {
            que_id = GET_UINT16(p);
            if (op != NJ_CUR_OP_FORE_EXT) {
                str = njd_l_get_string(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
            } else {
                str = njd_l_get_hyouki(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
            }
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
            if (op != NJ_CUR_OP_FORE_EXT) {
                str = njd_l_get_string(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
            } else {
                str = njd_l_get_hyouki(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
            }
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
 * 繋がり状態チェック関数
 *
 * 処理結果から前文節との繋がり関係があるかを判断する。
 *
 * @param[in] iwnn          解析情報クラス
 * @param[in] word          単語情報
 *
 * @retval 1  接続有
 * @retval 0  接続無
 */
NJ_INT16 njd_l_check_word_connect(NJ_CLASS *iwnn, NJ_WORD *word) {
    NJ_DIC_HANDLE handle;
    NJ_UINT16     que_id;
    NJ_UINT8     *ptr;
    NJ_INT16      ret;
    NJ_UINT16     prev_que;
    NJ_UINT8     *prev_ptr;
    NJ_UINT32     size;
    NJ_UINT32     use_que_cnt;
    NJ_UINT32     need_que_cnt;
    NJ_UINT16     max_cnt;


    ret = 0;

    /* 辞書ハンドルを取得する */
    handle = word->stem.loc.handle;

    /* 現在que_idを取得する */
    que_id = (NJ_UINT16)(word->stem.loc.current >> 16);

    ptr = POS_TO_ADDRESS(handle, que_id);
    /* １つ前の語彙との接続状態をチェック */
    if (GET_FFLG_FROM_DATA(ptr)) {
        /* 接続状態 */
        prev_que = njd_l_search_prev_que(handle, que_id);
        prev_ptr = POS_TO_ADDRESS(handle, prev_que);
        if (GET_TYPE_FROM_DATA(prev_ptr) != QUE_TYPE_EMPTY) {
            /* 格納サイズを算出する */
            size = LEARN_QUE_STRING_OFFSET - 1 /* 単語情報サイズ(= ヘッダサイズ - 管理情報1byte) */
                + GET_YSIZE_FROM_DATA(prev_ptr) + GET_KSIZE_FROM_DATA(prev_ptr);
            /* 必要キュー数を算出する */
            use_que_cnt = USE_QUE_NUM(LEARN_DIC_QUE_SIZE, size);

            /* 現在キューと１つ前のキューの差分を算出 */
            max_cnt = GET_LEARN_MAX_WORD_COUNT(handle);
            need_que_cnt = 0;
            if (que_id < prev_que) {
                need_que_cnt = max_cnt - prev_que; /*NCH_DEF*/
                need_que_cnt += que_id; /*NCH_DEF*/
            } else {
                need_que_cnt = que_id - prev_que;
            }

            /* 接続関係があるかチェックする */
            if (need_que_cnt == use_que_cnt) {
                /* 接続関係有 */
                ret = 1;
            }
        }
    }

    return ret;
}


/**
 * 拡張単語情報取得関数
 *
 * 辞書ハンドルとque_idから単語情報を取得する。
 * 
 * @attention iWnnタイプの学習辞書のみ対応
 * @attention return 0 の場合は、引数hinsi,lenが不定値となるため、呼び出し側でエラー処理が必要
 *
 * @param[in] iwnn          解析情報クラス
 * @param[in] word          単語情報
 * @param[out] hinsi        自立語後品詞
 * @param[out] len          付属語文字配列長
 *
 * @retval 1  取得成功
 * @retval 0  取得なし
 */
NJ_INT16 njd_l_get_ext_word_data(NJ_CLASS *iwnn, NJ_WORD *word, NJ_UINT16 *hinsi, NJ_UINT8 *len) {
    NJ_DIC_HANDLE handle;
    NJ_UINT16     que_id;
    NJ_UINT8      *ptr;


    /* 辞書ハンドルを取得する */
    handle = word->stem.loc.handle;

    if ((NJ_GET_DIC_VER(handle) != NJ_DIC_VERSION3) && (NJ_GET_DIC_VER(handle) != NJ_DIC_VERSION4)) {
        return 0;
    }

    /* 現在que_idを取得する */
    que_id = (NJ_UINT16)(word->stem.loc.current >> 16);

    ptr = POS_TO_EXT_ADDRESS(handle, que_id);

    /* 単語の属性データを読み飛ばす */
    ptr += sizeof(NJ_UINT32);
    /* 単語の自立語後品詞(9bit)、付属語読みバイト数(7bit)を読み込む */
    *hinsi = GET_BPOS_FROM_EXT_DATA(ptr);
    *len   = GET_YSIZE_FROM_EXT_DATA(ptr)/sizeof(NJ_CHAR);

    return 1;
}


/**
 * 辞書内の登録範囲を調べる(登録順)
 *
 * @param[in] handle        辞書ハンドル
 * @param[out] from         登録範囲先頭
 * @param[out] to           登録範囲末尾
 * @param[out] count        登録件数
 *
 * @retval 1  候補あり
 * @retval 0  候補なし
 * @retval <0  エラー
 */
static NJ_INT16 search_regist_range(NJ_DIC_HANDLE handle, NJ_UINT16 *from, NJ_UINT16 *to, NJ_UINT16 *count) {
    NJ_UINT16 regist_count;
    NJ_UINT16 max_count;
    NJ_UINT16 i;
    NJ_UINT16 pos;


    /* 学習領域に登録されている語彙数を取得する */
    regist_count = GET_LEARN_WORD_COUNT(handle);
    if (regist_count == 0) {
        return 0;
    }
    *count = regist_count;

    /* 辞書最大登録数を取得 */
    max_count = GET_LEARN_MAX_WORD_COUNT(handle);
    /* 次キュー追加位置を取得 */
    pos = GET_LEARN_NEXT_WORD_POS(handle);

    /* to を求める */
    *to = PREV_QUE(pos, max_count);

    /* from を求める */
    for (i = 0; i < regist_count; i++) {
        pos = njd_l_search_prev_que(handle, pos);
    }
    *from = pos;

    return 1;
}


/**
 * 辞書内の登録範囲を記憶する
 *
 * @param[in] iwnn          解析情報クラス
 * @param[in] handle        辞書ハンドル
 *
 * @retval 1   記憶範囲あり
 * @retval 0   記憶範囲なし
 * @retval <0  エラー
 */
NJ_INT16 njd_l_mld_op_commit(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle) {
    NJ_INT16 ret = 0;
    NJ_UINT16 from, to;
    NJ_UINT16 count;


    if (search_regist_range(handle, &from, &to, &count) == 0) {
        iwnn->learndic_status.commit_status = 0;
        ret = 0;
    } else {
        ret = 1;
        iwnn->learndic_status.commit_status = 1;
        iwnn->learndic_status.save_top = to;
        iwnn->learndic_status.save_bottom = from;
        iwnn->learndic_status.save_count  = count;
    }

    return ret;
}


/**
 * 辞書内の記憶範囲外の登録単語を記憶範囲後に移動する
 *
 * 歯抜け状態の部分はつめて移動する。
 *
 * @param[in] iwnn          解析情報クラス
 * @param[in] handle        辞書ハンドル
 *
 * @retval 0   正常終了
 * @retval <0  エラー
 */
NJ_INT16 njd_l_mld_op_commit_to_top(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle) {
    NJ_UINT8  *ptr;
    NJ_UINT16 j;
    NJ_UINT16 que_id;
    NJ_UINT16 que_num;
    NJ_UINT16 que_word_top;
    NJ_UINT16 que_move_from, que_move_to;
    NJ_UINT8  move_stroke_len, move_string_len;
    NJ_CHAR   move_stroke[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_CHAR   move_string[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    NJ_CHAR   *tmp_str;
    NJ_UINT8  tmp_len;
    NJ_UINT16 max_count;
    NJ_UINT16 idx_size;
    NJ_UINT16 que_size;


    if (iwnn->learndic_status.commit_status == 0) {
        /* 未コミット状態の場合、何もしない。 */
        return 0; /*NCH_DEF*/
    }
    /* 辞書最大登録数を取得 */
    max_count = GET_LEARN_MAX_WORD_COUNT(handle);

    /* キューサイズ */
    que_size = QUE_SIZE(handle);
    /* 移動元キュー：初期値 次単語登録先キュー */
    que_move_from = GET_LEARN_NEXT_WORD_POS(handle);
    /* 移動元単語開始キュー：初期値 移動範囲の末尾の単語の開始キュー */
    que_word_top  = njd_l_search_prev_que(handle, que_move_from);
    /* 移動先キュー：初期値 コミット範囲先頭キュー */
    que_move_to = iwnn->learndic_status.save_bottom;

    /*
     * コミット範囲外(=移動元範囲)の末尾から順次移動していく
     */
    while (!is_commit_range(iwnn, que_word_top)) {
        /* que_word_topのキュー情報を取得する */
        ptr = POS_TO_ADDRESS(handle, que_word_top);
        if ((tmp_str = njd_l_get_string(iwnn, handle, que_word_top, &move_stroke_len, NJ_NUM_SEGMENT1)) == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MANAGE_LEARNDIC, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
        }
        nj_strcpy(&move_stroke[0], tmp_str); /* 読み文字列 */
        if ((tmp_str = njd_l_get_hyouki(iwnn, handle, que_word_top, &move_string_len, NJ_NUM_SEGMENT1)) == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MANAGE_LEARNDIC, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
        }
        nj_strcpy(&move_string[0], tmp_str); /* 表記文字列 */

        /* 格納サイズを算出する
         * = 単語情報サイズ(= ヘッダサイズ - 管理情報1byte) + 読みbyte長 + 表記byte長 */
        que_num = LEARN_QUE_STRING_OFFSET - 1 + (move_stroke_len + move_string_len) * sizeof(NJ_CHAR);
        /* 消費するキュー数を算出する */
        que_num = USE_QUE_NUM(que_size, que_num);

        /* 
         * 書き込み元キュー位置 = 単語の開始キュー位置 + キュー消費サイズ - 1
         */
        que_move_from = que_word_top;
        for (j = 1; j < que_num; j++) {
            que_move_from = NEXT_QUE(que_move_from, max_count);
        }
        /* 書き込み先キュー位置 = 最終書き込み位置 - 1 */
        que_move_to = PREV_QUE(que_move_to, max_count);

        /*
         * 末尾から先頭に向かって順次ポインタを移動しつつ、
         * キューのコピー処理を行う。
         */
        if (que_move_to != que_move_from) {
            /* 移動元と先が異なる場合は、キューの複製処理を行う */
            for (j = 0; j < que_num; j++) {
                COPY_QUE(handle, que_move_from, que_move_to);
                if ((NJ_GET_DIC_VER(handle) == NJ_DIC_VERSION3) || (NJ_GET_DIC_VER(handle) == NJ_DIC_VERSION4)) {
                    COPY_QUE_EXT(handle, que_move_from, que_move_to);
                }
                que_move_from = PREV_QUE(que_move_from, max_count);
                que_move_to   = PREV_QUE(que_move_to,   max_count);
            }
            /* コピー元キューの削除
             */
            ptr = POS_TO_ADDRESS(handle, que_word_top);
            *ptr = QUE_TYPE_EMPTY;
            que_move_to = NEXT_QUE(que_move_to, max_count);
        } else {
            /* 移動元と先が同じ場合は、ポインタのみ変更 */
            que_move_from = que_word_top;
            que_move_to   = que_word_top;
        }

        /*
         * インデックス領域の更新
         */
        /* 読みインデックスの更新 */
        ptr = LEARN_INDEX_TOP_ADDR(handle);
        idx_size = GET_LEARN_WORD_COUNT(handle);
        for (j = 0; j < idx_size; j++, ptr += NJ_INDEX_SIZE) {
            if (que_word_top != GET_UINT16(ptr)) {
                /* キューIDが一致するまでポインタを進める */
                continue;
            }
            /* キューIDが一致した場合、インデックス更新する */
            NJ_INT16_WRITE(ptr, que_move_to);
            /* 
             * 移動する単語は、同読みの中で最小頻度になるため、
             * 同読みが、インデックスに存在しないかチェックする。
             */
            j++;
            ptr += NJ_INDEX_SIZE;
            for (; j < idx_size; j++, ptr += NJ_INDEX_SIZE) {
                tmp_len = GET_YSIZE_FROM_DATA(ptr) / sizeof(NJ_CHAR);
                if (tmp_len != move_stroke_len) {
                    /* 読み長が同じでない(=読みが同じでない)場合は終了 */
                    break;
                }
                /* 
                 * 移動キューと同じ読みのキューが存在する可能性があるため、
                 * 文字列を取得して比較を行う。
                 */
                que_id = GET_UINT16(ptr); /*NCH_DEF*/
                if ((tmp_str = njd_l_get_string(iwnn, handle, que_id, &tmp_len, NJ_NUM_SEGMENT1)) == NULL) { /*NCH_DEF*/
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MANAGE_LEARNDIC, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
                }
                if (nj_strcmp(&move_stroke[0], tmp_str) == 0) { /*NCH_DEF*/
                    /* 移動キューとキューの読みが完全一致した場合、キューIDの入替を行う。*/
                    NJ_INT16_WRITE(ptr - NJ_INDEX_SIZE, que_id); /*NCH_DEF*/
                    NJ_INT16_WRITE(ptr, que_move_to); /*NCH_DEF*/
                }
            }
            break;
        }

        /* 表記インデックスの更新 */
        ptr = LEARN_INDEX_TOP_ADDR2(handle);
        for (j = 0; j < idx_size; j++, ptr += NJ_INDEX_SIZE) {
            if (que_word_top != GET_UINT16(ptr)) {
                /* キューIDが一致するまでポインタを進める */
                continue;
            }
            /* キューIDが一致した場合、インデックス更新する */
            NJ_INT16_WRITE(ptr, que_move_to);
            /* 
             * 移動する単語は、同読みの中で最小頻度になるため、
             * 同表記が、インデックスに存在しないかチェックする。
             */
            j++;
            ptr += NJ_INDEX_SIZE;
            for (; j < idx_size; j++, ptr += NJ_INDEX_SIZE) {
                tmp_len = GET_KSIZE_FROM_DATA(ptr) / sizeof(NJ_CHAR);
                if (tmp_len != move_string_len) {
                    /* 表記長が同じでない(=表記が同じでない)場合は終了 */
                    break;
                }
                /* 
                 * 移動キューと同じ読みのキューが存在する可能性があるため、
                 * 文字列を取得して比較を行う。
                 */
                que_id = GET_UINT16(ptr); /*NCH_DEF*/
                if ((tmp_str = njd_l_get_hyouki(iwnn, handle, que_id, &tmp_len, NJ_NUM_SEGMENT1)) == NULL) { /*NCH_DEF*/
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_MANAGE_LEARNDIC, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
                }
                if (nj_strcmp(&move_string[0], tmp_str) == 0) { /*NCH_DEF*/
                    /* 移動キューとキューの表記が完全一致した場合、キューIDの入替を行う。*/
                    NJ_INT16_WRITE(ptr - NJ_INDEX_SIZE, que_id); /*NCH_DEF*/
                    NJ_INT16_WRITE(ptr, que_move_to); /*NCH_DEF*/
                }
            }
            break;
        }

        /* 次の移動対象単語の開始キューを求める */
        que_word_top  = njd_l_search_prev_que(handle, que_word_top);
    }

    /* コミット範囲を更新する */
    iwnn->learndic_status.save_bottom = que_move_to;
    iwnn->learndic_status.save_count =  GET_LEARN_WORD_COUNT(handle);
    SET_LEARN_NEXT_WORD_POS(handle, NEXT_QUE(iwnn->learndic_status.save_top, max_count));

    return 0;
}


/**
 * 辞書内の記憶範囲外の登録単語を削除する。
 *
 * ただし、未コミット状態の時は何もしない。
 *
 * @param[in] iwnn          解析情報クラス
 * @param[in] handle        辞書ハンドル
 *
 * @retval 0   正常終了
 * @retval <0  エラー
 */
NJ_INT16 njd_l_mld_op_commit_cancel(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle) {
    NJ_UINT16 i;
    NJ_UINT16 queid;
    NJ_INT16  ret;


    /* 追加された語数を算出 */
    if (iwnn->learndic_status.commit_status == 0) {
        /* 未コミット状態の場合、何もしない。 */
        return 0; /*NCH*/
    }

    /* コミット範囲の末尾から次キューを検索し、コミット範囲外のキューを削除していく */
    queid = njd_l_search_next_que(handle, iwnn->learndic_status.save_top);
    i = GET_LEARN_MAX_WORD_COUNT(handle); /* ループ上限リミット = 最大登録数 */
    while (!is_commit_range(iwnn, queid)) {
        ret = njd_l_delete_index(iwnn, handle, queid);
        if (ret < 0) {
            return ret; /*NCH_FB*/
        }
        if ((--i) == 0) {
            /* 最大登録数回ループした場合は、強制的に終了する */
            break;
        }
        queid = njd_l_search_next_que(handle, queid); /* 次キュー検索 */
    }
    iwnn->learndic_status.commit_status = 0;

    return 0;
}


/**
 * 辞書内の学習可能最大候補数を取得する。
 *
 * @param[in] iwnn          解析情報クラス
 * @param[in] handle        辞書ハンドル
 *
 * @retval 0   正常終了
 * @retval <0  エラー
 */
NJ_INT16 njd_l_mld_op_get_space(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle) {
    NJ_INT16 ret = 0;
    NJ_UINT16 from, to, count;
    NJ_UINT16 max_count;


    /* 辞書最大登録数を取得 */
    max_count = GET_LEARN_MAX_WORD_COUNT(handle);

    if (search_regist_range(handle, &from, &to, &count) == 0) {
        /* 登録候補が存在しない */
        ret = max_count;
    } else {
        /* すでに登録されている範囲を減算 */
        if (from <= to) {
            ret = max_count - (to - from + 1);
        } else {
            ret = from - to - 1;
        }
    }

    return ret;
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
static NJ_INT16 get_cand_by_evaluate_state(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond,
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
    NJ_INT32  que_hindo_max = INIT_HINDO;
    NJ_INT32  que_hindo_tmp;
    NJ_INT16  relation_cnt;
    NJ_UINT8  current_info = 0x10;


    /* 検索するインデックスの先頭アドレスを取得 */
    ptr = get_search_index_address(loctset->loct.handle, cond->operation);
    if (ptr == NULL) {
        /* 指定された方法で検索できない場合、"候補なし" を返す 08.02.03 */
        return 0; /*NCH_FB*/
    }

    /* LOCATIONの現在状態の判別 */
    current = 0;

    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        loctset->loct.current_info = 0x10;
    }

    if (IS_TARGET_COMPOUND_MODE(iwnn, loctset->loct.type, loctset->loct.handle, cond) &&
        ((loctset->loct.current_info & CMPDG_BIT_ON) != 0)) {
        if ((loctset->loct.current_info & CMPDG_CURRENT_NUM) != 0) {
            /* 初期化 */
            loctset->loct.current_info = 0x10;
        } else {
            /* 複合語接続bit */
            loctset->loct.current_info = CMPDG_CURRENT_NUM;
            /* 複合語接続状態bit */
            loctset->loct.current_info |= CMPDG_BIT_ON;
            return 1;
        }
    }

    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        if (search_pattern == NJ_CUR_OP_LINK) {
            /* つながり検索 */
            ret = search_range_by_yomi_multi(iwnn, loctset->loct.handle, 
                                             cond->yomi, cond->ylen, &from, &to);
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
                /* つながり検索 */
                ret = que_strcmp_complete_with_hyouki(iwnn, loctset->loct.handle, que_id,
                                                      cond->yomi, cond->ylen, cond->kanji, 1);
                if (ret < 0) {
                    return ret; /*NCH_FB*/
                }
                if (ret >= 1) {
                    /* 一致した場合のみ頻度を取得し、最高頻度を算出する。*/
                    
                    /* つながりの候補があるかを探す */
                    relation_cnt = continue_cnt(iwnn, loctset->loct.handle, que_id);
                    if (relation_cnt < 0) {
                        /* 下位のエラーをそのまま返す */
                        return relation_cnt; /*NCH_FB*/
                    }
                    /*
                     * retは、完全一致した単語の数
                     * relation_cnt + 1は、先頭を含む繋がっている単語の数
                     */
                    if (relation_cnt >= ret) {
                        for (j = 0; j < ret; j++) {
                            /*
                             * ここで指しているque_idは、繋がり元のque_idなので
                             * 返す候補のque_idまで進める
                             */
                            que_id = njd_l_search_next_que(loctset->loct.handle, que_id);
                        }
                        
                        /* 単語頻度を取得*/
                        hindo_tmp = njd_l_get_attr_hindo(iwnn, loctset, loctset->loct.handle, que_id, (NJ_UINT32*)&que_hindo_tmp);
                        if ((hindo_tmp > hindo_max) || ((hindo_tmp == hindo_max) && (que_hindo_tmp > que_hindo_max))) {
                            que = get_que_allHinsi(iwnn, loctset->loct.handle, que_id);
                            if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                                found        = 1;
                                hindo_max    = hindo_tmp;
                                que_hindo_max= que_hindo_tmp;
                                current      = i;
                                current_info = (NJ_UINT8)(((relation_cnt + 1) << 4) | ret);
                            }
                        }
                    }
                }
            }
            /* 現在位置を設定 */
            loctset->loct.current = current;
        } else {
            /* 前方一致検索 */
            ret = search_range_by_yomi(iwnn, loctset->loct.handle, search_pattern,
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
                hindo_tmp = njd_l_get_attr_hindo(iwnn, loctset, loctset->loct.handle, que_id, (NJ_UINT32*)&que_hindo_tmp);

                if ((hindo_tmp > hindo_max) || ((hindo_tmp == hindo_max) && (que_hindo_tmp > que_hindo_max))) {
                    que = get_que_allHinsi(iwnn, loctset->loct.handle, que_id);
                    if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                        found = 1;
                        hindo_max = hindo_tmp;
                        que_hindo_max= que_hindo_tmp;
                        current   = i;
                        current_info = 0x10;
                    }
                }
            }
            /* 現在位置を設定 */
            loctset->loct.current = current;
        }

    } else if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_READY) {
        /* 次候補検索を行う */
        current_info = loctset->loct.current_info;
        found = njd_l_get_next_data(iwnn, cond, loctset, search_pattern, loctset->loct.handle,
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
        que_id = njd_l_search_next_que(loctset->loct.handle, que_id);
    }

    /* 複合語予測学習機能実行 */
    if (iwnn->option_data.ext_mode & NJ_OPT_FORECAST_COMPOUND_WORD) {
        set_cmpdg_mode(iwnn, cond, loctset, que_id);
    }

    ext_ptr = POS_TO_EXT_ADDRESS(loctset->loct.handle, que_id);
    STATE_COPY((NJ_UINT8*)&loctset->loct.attr, ext_ptr);

    loctset->loct.status = NJ_ST_SEARCH_READY;
    loctset->cache_freq  = calculate_hindo(loctset->loct.handle, hindo_max, &(loctset->dic_freq), loctset->dic_freq_max, loctset->dic_freq_min);
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
static NJ_INT16 get_cand_by_evaluate2_state(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond,
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
    NJ_INT32        que_hindo_max = INIT_HINDO;
    NJ_INT32        que_hindo_tmp;
    NJ_UINT16       current_abIdx = 0;
    NJ_UINT16       old_cache_idx;
    NJ_INT32        save_hindo;
    NJ_UINT16       save_idx;
    NJ_UINT8        current_info = 0x10; /* 複合文節でない */

#ifdef NJ_OPT_CHARSET_2BYTE
    NJ_UINT16 start;
    NJ_UINT16 end;
#endif /* NJ_OPT_CHARSET_2BYTE */


    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        loctset->loct.current_info = 0x10;
    }

    if (NJ_GET_CACHEOVER_FROM_SCACHE(psrhCache)) {
        aimai_flg = 0x00;
    }

    /* 検索するインデックスの先頭アドレスを取得 */
    ptr = get_search_index_address(loctset->loct.handle, cond->operation);
    if (ptr == NULL) {
        /* 指定された方法で検索できない場合、"候補なし" を返す 08.02.03 */
        return 0; /*NCH_FB*/
    }

    /* LOCATIONの現在状態の判別 */
    current = 0;

    if (IS_TARGET_COMPOUND_MODE(iwnn, loctset->loct.type, loctset->loct.handle, cond) &&
        ((loctset->loct.current_info & CMPDG_BIT_ON) != 0)) {
        if ((loctset->loct.current_info & CMPDG_CURRENT_NUM) != 0) {
            /* 初期化 */
            loctset->loct.current_info = 0x10;
        } else {
            /* 複合語接続bit */
            loctset->loct.current_info = CMPDG_CURRENT_NUM;
            /* 複合語接続状態bit */
            loctset->loct.current_info |= CMPDG_BIT_ON;
            return 1;
        }
    }

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
                    ret = search_range_by_yomi(iwnn, loctset->loct.handle, search_pattern,
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
                                ret = search_range_by_yomi(iwnn, loctset->loct.handle, search_pattern,
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
                                        psrhCache->keyPtr[j+1] = 0; /*NCH_DEF*/
                                        return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_NOT_ENOUGH); /*NCH_DEF*/
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
                            str = njd_l_get_string(iwnn, loctset->loct.handle, que_id, &slen, NJ_NUM_SEGMENT1);

                            if (str == NULL) {
                                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
                            }

                            /* 必要読み文字列数分取得する */
                            nj_strncpy(char_tmp, str, psrhCache->storebuff[m].idx_no);
                            char_tmp[psrhCache->storebuff[m].idx_no] = NJ_CHAR_NUL;

                            pchar_tmp = &char_tmp[psrhCache->storebuff[m].idx_no];
                            nj_charncpy(pchar_tmp, yomi, 1);
                            tmp_len = nj_strlen(char_tmp);


                            ret = search_range_by_yomi2(iwnn, loctset, search_pattern,
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
                                        ret = search_range_by_yomi2(iwnn, loctset, search_pattern,
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
                                                psrhCache->keyPtr[j+1] = 0; /*NCH_DEF*/
                                                return NJ_SET_ERR_VAL(NJ_FUNC_SEARCH_RANGE_BY_YOMI, NJ_ERR_CACHE_NOT_ENOUGH); /*NCH_DEF*/
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
            NJ_UNSET_AIMAI_TO_SCACHE(psrhCache);
        } else {
            old_cache_idx = loctset->loct.current_cache;
            /* 次候補検索を行う */
            found = njd_l_get_next_data(iwnn, cond, loctset, search_pattern, loctset->loct.handle, ptr,
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
                que_hindo_max = INIT_HINDO;
                found     = 0;

                /* 辞書内の最大頻度の候補を算出 */
                for (i = from; i <= to; i++) {

                    /* QueueIDを取得 */
                    p = ptr + (i * NJ_INDEX_SIZE);
                    que_id = GET_UINT16(p);

                    /* 単語頻度を取得*/
                    hindo_tmp = njd_l_get_attr_hindo(iwnn, loctset, loctset->loct.handle, que_id, (NJ_UINT32*)&que_hindo_tmp);

                    if ((hindo_tmp > hindo_max) || ((hindo_tmp == hindo_max) && (que_hindo_tmp > que_hindo_max))) {
                        /* 品詞チェック */
                        /*
                         * str_que_cmp or que_strcmp_complete_with_hyoukiが
                         * 成功しているのでここではエラーは返らない
                         */
                        que = get_que_allHinsi(iwnn, loctset->loct.handle, que_id);
                        if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                            found      = 1;
                            hindo_max  = hindo_tmp;
                            que_hindo_max = que_hindo_tmp;
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
            que_hindo_max = INIT_HINDO;
            current_abIdx = 0;

            for (m = abIdx; m < endIdx; m++) {
                if (psrhCache->storebuff[m].current == LOC_CURRENT_NO_ENTRY) {
                    /* 検索終了の検索キャッシュの場合は、参照しない */
                    continue;
                }
                i = (NJ_UINT16)psrhCache->storebuff[m].current;
                p = ptr + (i * NJ_INDEX_SIZE);
                que_id = GET_UINT16(p);
                hindo_tmp = njd_l_get_attr_hindo(iwnn, loctset, loctset->loct.handle, que_id, (NJ_UINT32*)&que_hindo_tmp);

                if ((hindo_tmp > hindo_max) || ((hindo_tmp == hindo_max) && (que_hindo_tmp > que_hindo_max))) {
                    /* 最高の頻度の算出を行う */
                    hindo_max     = hindo_tmp;
                    que_hindo_max = que_hindo_tmp;
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

    /* あいまい検索合致時は複合語処理はスキップ */
    if ((GET_LOCATION_STATUS(loctset->loct.status) != NJ_ST_SEARCH_NO_INIT) && 
        (iwnn->option_data.ext_mode & NJ_OPT_FORECAST_COMPOUND_WORD)) {
        /* 複合語予測学習機能実行 */
        set_cmpdg_mode(iwnn, cond, loctset, que_id);
    }
    ext_ptr = POS_TO_EXT_ADDRESS(loctset->loct.handle, que_id);
    STATE_COPY((NJ_UINT8*)&loctset->loct.attr, ext_ptr);
    loctset->loct.current_cache = current_abIdx;
    loctset->loct.status = NJ_ST_SEARCH_READY;
    loctset->cache_freq  = calculate_hindo(loctset->loct.handle, hindo_max, &(loctset->dic_freq), loctset->dic_freq_max, loctset->dic_freq_min);

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
 * @param[out] que_hindo      キュー頻度
 *
 * @retval 単語頻度
 */
NJ_INT32 njd_l_get_attr_hindo(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset, NJ_DIC_HANDLE handle, NJ_UINT16 que_id, NJ_UINT32 *que_hindo) {
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
    *que_hindo = ret_hindo;

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
 * 文頭繋がり候補を検索する
 *
 * @param[in] iwnn           解析情報クラス
 * @param[in] loctset        検索位置
 *
 * @retval 1 検索一致あり
 * @retval 0 検索一致なし
 */
static NJ_INT16 get_candidate_buntou_link(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset) {

    NJ_UINT16 current;
    NJ_UINT16 found = 0;
    NJ_UINT16 que_id;
    NJ_UINT8  *ptr;
    NJ_INT32  freq;
    NJ_INT32  freq_max  = INIT_HINDO;
    NJ_INT32  freq_init = 0x7FFFFFFF; /* 内部頻度上限値よりも上の数値 */
    NJ_INT32  que_freq;
    NJ_INT32  que_freq_max  = INIT_HINDO;
    NJ_INT32  que_freq_init = 0x7FFFFFFF; /* 内部頻度上限値よりも上の数値 */



    if (GET_LEARN_WORD_COUNT(loctset->loct.handle) <= 0) {
        /* 登録候補数が０の場合、検索一致無しで終了 */
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
        return 0; /*NCH_FB*/
    }
    if ((NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION3) && (NJ_GET_DIC_VER(loctset->loct.handle) != NJ_DIC_VERSION4)) {
        /* 属性あり(iWnn版)でない場合は非対応。検索一致無しで終了 */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    /* カーソル位置のインデックスアドレスを取得 */
    ptr = LEARN_INDEX_TOP_ADDR(loctset->loct.handle);

    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        /* 初回検索時 */
        /* 全範囲を検索対象とする */
        loctset->loct.top    = 0;
        loctset->loct.bottom = GET_LEARN_WORD_COUNT(loctset->loct.handle) - 1;
        /* 読みインデックスの先頭の候補から検索 */
        current = 0;

    } else if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_READY) {
        /* 検索継続中 次候補取得 */
        /* 読みインデックスの先頭の候補から検索 */
        current = 0;
        /* 頻度最大値の初期値(現在のカーソル位置の単語の頻度値) */
        freq_init = njd_l_get_attr_hindo(iwnn, loctset, loctset->loct.handle,
                                   (NJ_UINT16)(GET_UINT16(ptr + (loctset->loct.current * NJ_INDEX_SIZE))), (NJ_UINT32*)&que_freq_init);

    } else {
        /* その他のモードの時は、検索一致無しで返す */
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
        return 0; /*NCH_FB*/
    }

    /* 属性値が文頭の候補を探す */
    while (current <= loctset->loct.bottom) {
        /* Queue番号の取得 */
        que_id = GET_UINT16(ptr + (current * NJ_INDEX_SIZE));

        if (HAS_BUNTOU_ATTR(loctset->loct.handle, que_id)) {
            /* 文頭属性がONの場合 */
            freq = njd_l_get_attr_hindo(iwnn, loctset, loctset->loct.handle, que_id, (NJ_UINT32*)&que_freq);
            if ((freq > freq_max)
                && ((freq < freq_init)
                    || ((freq == freq_init) && (que_freq_init > que_freq)))) {
                freq_max = freq;
                que_freq_max = que_freq;
                found = current;
            }
        }
        current++;
    }

    if (freq_max == INIT_HINDO) {
        /* 頻度最大値に変更がない = 候補が見つからなかった場合 */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    /* 候補が見つかった場合は、その情報を返す */
    loctset->loct.status = NJ_ST_SEARCH_READY;
    loctset->loct.current = found;
    loctset->loct.current_info = (NJ_UINT8)0x10;
    loctset->cache_freq = calculate_hindo(loctset->loct.handle, freq_max, &(loctset->dic_freq), loctset->dic_freq_max, loctset->dic_freq_min);
    if ((NJ_GET_DIC_VER(loctset->loct.handle) == NJ_DIC_VERSION3) || (NJ_GET_DIC_VER(loctset->loct.handle) == NJ_DIC_VERSION4)) {
        STATE_COPY((NJ_UINT8*)&loctset->loct.attr,
                   POS_TO_EXT_ADDRESS(loctset->loct.handle, GET_UINT16(ptr + (found * NJ_INDEX_SIZE))));
    }
    loctset->loct.relation[0] = 0;
    return 1;
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
static NJ_INT16 njd_l_get_next_data(NJ_CLASS *iwnn, NJ_SEARCH_CONDITION *cond,
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
    NJ_INT32  current_que_hindo;
    NJ_INT32  tmp_que_hindo = 0;
    NJ_INT32  max_que_hindo = INIT_HINDO;
    NJ_UINT32 found = 0;
    NJ_UINT8  *p;
    NJ_INT16  ret, j;
    NJ_INT16  relation_cnt;


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
        que_id = njd_l_search_next_que(loctset->loct.handle, que_id);
    }
    current_hindo = njd_l_get_attr_hindo(iwnn, loctset, handle, que_id, (NJ_UINT32*)&current_que_hindo);
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

        if (search_pattern == NJ_CUR_OP_LINK) {
            /* つながり検索 */
            ret = que_strcmp_complete_with_hyouki(iwnn, handle, que_id,
                                                  cond->yomi, cond->ylen, cond->kanji, 1);
            if (ret < 0) {
                return ret; /*NCH_FB*/
            }
            if (ret >= 1) {
                /* 一致した場合のみ頻度を取得し、最高頻度を算出する。*/
                /* つながりの候補があるかを探す */
                relation_cnt = continue_cnt(iwnn, handle, que_id);
                if (relation_cnt < 0) {
                    /* 下位のエラーをそのまま返す */
                    return relation_cnt; /*NCH_FB*/
                }
                /*
                 * retは、完全一致したキューの数
                 * relation_cnt + 1は、先頭を含む繋がっているキューの数
                 */
                if (relation_cnt >= ret) {
                    for (j = 0; j < ret; j++) {
                        /*
                         * ここで指しているque_idは、繋がり元のque_idなので
                         * 返す候補のque_idまで進める
                         */
                        que_id = njd_l_search_next_que(handle, que_id);
                    }

                    /* 単語頻度を取得*/
                    tmp_hindo = njd_l_get_attr_hindo(iwnn, loctset, handle, que_id, (NJ_UINT32*)&tmp_que_hindo);

                    if ((tmp_hindo < current_hindo) || ((tmp_hindo == current_hindo) && (tmp_que_hindo < current_que_hindo))) {
                        /*
                         * カレントより頻度が低く、その中で最高頻度のものを取得
                         * 検索中の最高頻度と同じで、読みが先のものを取得
                         */
                        if ((max_hindo < tmp_hindo) || ((tmp_hindo == max_hindo) && (tmp_que_hindo > max_que_hindo))) {
                            que = get_que_allHinsi(iwnn, handle, que_id);
                            if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                                max_hindo       = tmp_hindo;
                                max_que_hindo   = tmp_que_hindo;
                                max_hindo_idx   = i;
                                found           = 1;
                                *current_info   = (NJ_UINT8)(((relation_cnt + 1) << 4) | ret);
                            }
                        }
                    }
                }
            }
        } else {
            /* 前方一致検索 */
            /* 単語頻度を取得*/
            tmp_hindo = njd_l_get_attr_hindo(iwnn, loctset, handle, que_id, (NJ_UINT32*)&tmp_que_hindo);

            if ((tmp_hindo < current_hindo) || ((tmp_hindo == current_hindo) && (tmp_que_hindo < current_que_hindo))) {
                /* カレントより頻度が低く、その中で最高頻度のものを取得 */
                /* 検索中の最高頻度と同じで、読みが先のものを取得 */
                if ((max_hindo < tmp_hindo) || ((tmp_hindo == max_hindo) && (tmp_que_hindo > max_que_hindo))) {
                    /*
                     * 品詞チェック
                     * str_que_cmpが
                     * 成功しているのでここではエラーは返らない
                     */
                    que = get_que_allHinsi(iwnn, handle, que_id);
                    if (njd_connect_test(cond, que->mae_hinsi, que->ato_hinsi)) {
                        max_hindo       = tmp_hindo;
                        max_que_hindo   = tmp_que_hindo;
                        max_hindo_idx   = i;
                        found           = 1;
                    }
                }
            }
        }
        i++;
        if ((i > bottom) && (bottomflg == 0)) {
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
 * 学習辞書が特定キューに対して操作可能かを判定する。
 *
 * @param[in,out]      iwnn  解析情報クラス
 * @param[in]        que_id  キューID
 *
 * @retval              1   操作不可
 * @retval              0   操作可能
 */
static NJ_INT16 is_commit_range(NJ_CLASS *iwnn, NJ_UINT16 que_id) {

    if (iwnn->learndic_status.commit_status) {
        /* 学習辞書操作コミット中のため、コミット範囲への操作はNG */
        if (iwnn->learndic_status.save_top >= iwnn->learndic_status.save_bottom) {
            if ((iwnn->learndic_status.save_top    >= que_id) &&
                (iwnn->learndic_status.save_bottom <= que_id)) {
                return 1;
            }
        } else {
            if ((iwnn->learndic_status.save_top    >= que_id) ||
                (iwnn->learndic_status.save_bottom <= que_id)) {
                return 1;
            }
        }
    }
    return 0;
}


/**
 * 付加情報文字列を取得する
 *                   
 * @param[in]       iwnn : 解析情報クラス
 * @param[in]       word : 単語情報
 * @param[in]      index : 付加情報インデックス
 * @param[out]  add_info : 付加情報文字列
 * @param[in]       size : add_infoのバイトサイズ
 *
 * @retval            >0   取得した文字配列長
 * @retval            <0   エラー
 */
NJ_INT32 njd_l_get_additional_info(NJ_CLASS *iwnn, NJ_WORD *word, NJ_UINT8 index,
                                 NJ_CHAR *add_info, NJ_UINT32 size) {
    NJ_UINT16 que_id;
    NJ_UINT16 len, total_len;
    NJ_UINT8  *add_ptr;
    NJ_UINT32 dictype;
    NJ_UINT32 version;
    NJ_UINT8  segment_num;
    NJ_UINT16 cnt = 0;


    dictype = NJ_GET_DIC_TYPE(word->stem.loc.handle);
    version = NJ_GET_DIC_VER(word->stem.loc.handle);
    if (((dictype == NJ_DIC_TYPE_LEARN) && (version != NJ_DIC_VERSION4)) ||
        ((dictype == NJ_DIC_TYPE_USER) && (version != NJ_DIC_VERSION3))) {
        return 0;
    }

    que_id = (NJ_UINT16)(word->stem.loc.current >> 16);

    /* 複合語結合を行う状態かかどうか判定 */
    if (IS_COMPOUND_WORD(word)) {
        segment_num = NJ_NUM_SEGMENT2;
    } else {
        segment_num = NJ_NUM_SEGMENT1;
    }

    total_len = 0;
    for (cnt = 0; cnt < segment_num; cnt++) {

        add_ptr = POS_TO_ADD_ADDRESS(word->stem.loc.handle, que_id);

        /* 付加情報文字列バイト数を取得 */
        len = *add_ptr;
        add_ptr++;
        add_ptr++; /* リザーブをスキップ */

        if (size < (len + (NJ_TERM_LEN*sizeof(NJ_CHAR)))) {
            /* バッファサイズエラー         */
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_GET_ADDITIONAL_INFO, NJ_ERR_BUFFER_NOT_ENOUGH);
        }
        /* 文字列を取得 */
        nj_memcpy((NJ_UINT8*)add_info, add_ptr, len);
        add_info  += (len / sizeof(NJ_CHAR));
        total_len += (len / sizeof(NJ_CHAR));
        size      -= len;
        
        /* 次の文節のque_idを取得 */
        que_id = njd_l_search_next_que(word->stem.loc.handle, que_id);
    }

    /* 付加情報文字列を NULLでターミネートする */
    *(add_info) = NJ_CHAR_NUL;

    return (NJ_INT16)(total_len);
}


/**
 * 複合語予測情報初期化処理
 *                   
 * @param[in]       iwnn : 複合語予測情報
 *
 * @retval          0   初期化なし
 * @retval          1   正常
 */
NJ_INT16 njd_l_init_cmpdg_info(NJ_CLASS* iwnn) {

    NJ_UINT32 i;

    /* 複合語予測学習機能オプション判定 */
    if (!(iwnn->option_data.ext_mode & NJ_OPT_FORECAST_COMPOUND_WORD)) {
        /* 複合語予測学習なし */
        return 0;
    }

    /* 複合語予測情報バッファクリア */
    for (i = 0; i < NJ_MAX_CMPDG_RESULTS; i++) {
        iwnn->cmpdg_info.cmpdg_data[i].yomi_toplen = 0;
        iwnn->cmpdg_info.cmpdg_data[i].yomi_len = 0;
        iwnn->cmpdg_info.cmpdg_data[i].index = NJ_CMPDG_BLANK_INDEX;
        iwnn->cmpdg_info.cmpdg_data[i].status = NJ_CMPDG_STATUS_ADD;
    }
    iwnn->cmpdg_info.refer_count = 0;
    iwnn->cmpdg_info.add_count  = 0;

    return 1;
}


/**
 * 複合語予測学習機能実行
 *                   
 * @param[in]       iwnn    複合語予測情報
 * @param[in]       cond    検索条件
 * @param[in,out]   loctset 検索位置
 * @param[in]       que_id  キューID
 */
static void set_cmpdg_mode(NJ_CLASS* iwnn, NJ_SEARCH_CONDITION* cond, NJ_SEARCH_LOCATION_SET* loctset, NJ_UINT16 que_id) {

    NJ_UINT8        ret;
#ifndef NJ_OPT_FORECAST_COMPOUND_VER143
    NJ_WQUE         *que;
    NJ_UINT8        *src;
#endif /* NJ_OPT_FORECAST_COMPOUND_VER143 */

    /* 複合語予測学習機能実行判定 */
    ret = (NJ_UINT8)(IS_TARGET_COMPOUND_WORD(iwnn, loctset->loct.type, loctset->loct.handle, cond, que_id));
    if (ret) {
#ifndef NJ_OPT_FORECAST_COMPOUND_VER143
        /* 複合語予測生成有無の判定 */
        que = get_que_yomiLen_and_hyoukiLen(iwnn, loctset->loct.handle, que_id);
        if (que == NULL) {
            return;
        }
        if (que->hyouki_len == 0) {
            /* 表記文字列長が存在しない場合 */
            src = POS_TO_ADDRESS(loctset->loct.handle, que_id);
            if (GET_MFLG_FROM_DATA(src) == 0) {
                /* queが無変換候補(表記：ひらがな)の場合、複合語予測候補を生成しない */
                return;
            }
        }
#endif /* !NJ_OPT_FORECAST_COMPOUND_VER143 */
        /* 複合語サイズ確認 */
        if (set_cmpdg_size_check(iwnn, cond, loctset, que_id)) {
            /* 複合語可能 */
            loctset->loct.current_info = (NJ_UINT8)0x10;
            /* 複合語接続状態bit */
            loctset->loct.current_info |= CMPDG_BIT_ON;
        }
    }
    return;
}


/**
 * 付加情報の配列長を取得する
 *
 * @param[in]       iwnn : 解析情報クラス
 * @param[in]     handle : 単語情報
 * @param[in]     que_id : 付加情報インデックス
 *
 * @retval            >0   取得したサイズ
 */
static NJ_INT32 get_additional_info_size(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id) {
    NJ_UINT16 size = 0;
    NJ_UINT8  *add_ptr;
    NJ_UINT32 dictype;
    NJ_UINT32 version;


    dictype = NJ_GET_DIC_TYPE(handle);
    version = NJ_GET_DIC_VER(handle);
    if ((dictype == NJ_DIC_TYPE_LEARN) && (version != NJ_DIC_VERSION4)) {
        return 0;
    }

    add_ptr = POS_TO_ADD_ADDRESS(handle, que_id);

    /* 付加情報文字列バイト数を取得 */
    size = *add_ptr;

    /* 付加情報の文字配列長にして返す */
    return (NJ_INT16)(size / sizeof(NJ_CHAR));
}


/**
 * 複合語サイズ確認
 *
 * 読み長、表記長、付加情報長を複合化した際に最大バッファを越えないか確認
 *
 * @param[in]       iwnn    複合語予測情報
 * @param[in]       cond    検索条件
 * @param[in,out]   loctset 検索位置
 * @param[in]       que_id  キューID
 *
 * @retval            =1    複合可能
 * @retval            =0    複合不可

 */
static NJ_UINT8 set_cmpdg_size_check(NJ_CLASS* iwnn, NJ_SEARCH_CONDITION* cond, NJ_SEARCH_LOCATION_SET* loctset, NJ_UINT16 que_id) {
    NJ_CHAR*  str;
    NJ_UINT8  slen;
    NJ_UINT8  slen2;
    NJ_UINT16 que_id_next;
    NJ_UINT16 letters;
    NJ_UINT32 dictype;
    NJ_UINT32 version;

    /* 複合語読み文字列長確認 */
    str = njd_l_get_string(iwnn, loctset->loct.handle, que_id, &slen, NJ_NUM_SEGMENT1);
    if (str == NULL) {
        /* 読み文字列が取得できなかった場合、複合語候補作成不可とする */
        return 0; /*NCH*/
    }
    /* 複合語生成は、入力された読み文字数と複合語の語句１の読み文字数が一致 */
    if (slen != cond->ylen) {
        /* 複合語候補作成不可 */
        return 0;
    }

    /* 複合語予測最大文字数確認 */
    letters = nj_charlen(str);
    que_id_next = njd_l_search_next_que(loctset->loct.handle, que_id);
    str = njd_l_get_string(iwnn, loctset->loct.handle, que_id_next, &slen2, NJ_NUM_SEGMENT1);
    if (str == NULL) {
        /* 読み文字列が取得できなかった場合、複合語候補作成不可とする */
        return 0; /*NCH*/
    }
    letters += nj_charlen(str);

    /* 最大読み文字列長オーバーの場合 */
    if (((slen + slen2) > NJ_MAX_LEN)) {
        /* 複合語候補作成不可 */
        return 0;
    }

    /* 予測文字数範囲外の場合 */
    if ((letters < iwnn->environment.option.char_min) ||
        (letters > iwnn->environment.option.char_max)) {
        /* 複合語候補作成不可 */
        return 0;
    }

    /* 複合語表記文字列長確認 */
    slen  = 0;
    slen2 = 0;
    str = njd_l_get_hyouki(iwnn, loctset->loct.handle, que_id, &slen, NJ_NUM_SEGMENT1);
    if (str == NULL) {
        /* 候補文字列が取得できなかった場合、複合語候補作成不可とする */
        return 0; /*NCH*/
    }
    str = njd_l_get_hyouki(iwnn, loctset->loct.handle, que_id_next, &slen2, NJ_NUM_SEGMENT1);
    if (str == NULL) {
        /* 候補文字列が取得できなかった場合、複合語候補作成不可とする */
        return 0; /*NCH*/
    }

    /* 最大表記文字列長オーバーの場合 */
    if (((slen + slen2) > NJ_MAX_RESULT_LEN)) {
        /* 複合語候補作成不可 */
        return 0;
    }

    /* 複合語付加情報サイズ長確認 */
    dictype = NJ_GET_DIC_TYPE(loctset->loct.handle);
    version = NJ_GET_DIC_VER(loctset->loct.handle);
    if ((dictype == NJ_DIC_TYPE_LEARN) && (version != NJ_DIC_VERSION4)) {
        /* 付加情報対象外の為、判定せずに作成可とする */
        return 1;
    }

    slen = (NJ_UINT8)get_additional_info_size(iwnn, loctset->loct.handle, que_id);
    slen += (NJ_UINT8)get_additional_info_size(iwnn, loctset->loct.handle, que_id_next);
    /* 付加情報サイズオーバーの場合 */
    if ((slen > (NJ_MAX_ADDITIONAL_LEN))) {
        /* 複合語候補作成不可 */
        return 0;
    }

    /* 複合語可能 */
    return 1;
}


/**
 * 学習辞書から同読み・同表記の候補を検索し、削除する
 *
 * @param[in]      iwnn    解析情報クラス
 * @param[in]    handle    辞書ハンドル
 * @param[in]      yomi    読み文字列
 * @param[in]      ylen    読み文字列長
 * @param[in]     hyoki    表記文字列
 * @param[in]      hlen    表記文字列長
 *
 * @retval     0   正常終了
 * @retval    <0   エラー
 */
static NJ_INT16 njd_l_delete_same_word(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, 
                                       NJ_CHAR *yomi, NJ_UINT16 ylen, NJ_CHAR* hyoki, NJ_UINT16 hlen) {
    NJ_INT16 ret;
    NJ_INT16 j, k, l;
    NJ_UINT16 from;
    NJ_UINT16 to;
    NJ_UINT8 *index_ptr, *ptr;
    NJ_UINT16 que_id;
    NJ_CHAR *stroke, *string;
    NJ_UINT8 oke_len, ing_len;
    NJ_UINT16 del_que_id[NJD_SAME_INDEX_LIMIT][NJ_MAX_PHRASE];
    NJ_UINT16 del_que_cnt[NJD_SAME_INDEX_LIMIT];
    NJ_UINT16 del_word_cnt;
    NJ_CHAR *tmp_yomi, *tmp_hyoki;
    NJ_UINT16 tmp_ylen, tmp_hlen;
    NJ_WORD_INFO info;
    NJ_UINT8 seg_flg;
    NJ_CHAR *pyomi;
    NJ_UINT8 pyomi_len;
    NJ_UINT16 del_cnt;
    NJ_UINT8 forward_flag = 0;



    /* 引数チェック */
    if ((handle == NULL) || (yomi == NULL) || (ylen > NJ_MAX_LEN) ||
        (hyoki == NULL) || (hlen > NJ_MAX_RESULT_LEN)) {
        return 0; /*NCH*/
    }

    pyomi = yomi;
    pyomi_len = 0;
    del_cnt = 0;
    /**
     * 1文字目から読み文字列長まで読みを1文字ずつ
     * 伸ばして一致する候補が存在するかを確認する。
     */
    while (pyomi_len < ylen) {
        from = 0;
        to = 0;
        del_word_cnt = 0;

        /* 検索範囲取得に必要な文字配列長を取得 */
        pyomi = yomi + pyomi_len;
        pyomi_len += NJ_CHAR_LEN(pyomi);

        /* 削除対象の検索範囲を取得 */
        ret = search_range_by_yomi(iwnn, handle, NJ_CUR_OP_COMP, yomi, pyomi_len, &from, &to, &forward_flag);
        if (ret <= 0) {
            /**
             * 読みに対する検索対象範囲が見つからなかった場合、
             * 読み文字列を1文字伸ばす。
             */
            continue;
        }

        /* 読み順インデックス領域の先頭アドレスを取得する */
        index_ptr = LEARN_INDEX_TOP_ADDR(handle);

        del_que_cnt[del_word_cnt] = 0;
        for (j = from; j <= to; j++) {
            ptr = index_ptr + (j * NJ_INDEX_SIZE);
            que_id = GET_UINT16(ptr);
            del_que_cnt[del_word_cnt] = 0;
            tmp_ylen = 0;
            tmp_hlen = 0;
            tmp_yomi = yomi;
            tmp_hyoki = hyoki;
            seg_flg = 1;

            /* 接続する文節があるまで確認を実施する */
            while (seg_flg) {

                /* que_idに対応する読み文字列を取得 */
                stroke = njd_l_get_string(iwnn, handle, que_id, &oke_len, NJ_NUM_SEGMENT1);
                if (stroke == NULL) {
                    /* 読み文字列が取得できなかった場合、次の候補へ移動 */
                    seg_flg = 0;
                    break;
                }
                /* 削除対象の読み文字列と比較 */
                if (nj_strncmp(tmp_yomi, stroke, oke_len) != 0) {
                    /* 一致しない場合、次の候補へ移動 */
                    seg_flg = 0;
                    break;
                }
                /* que_idに対応する候補文字列を取得 */
                string = njd_l_get_hyouki(iwnn, handle, que_id, &ing_len, NJ_NUM_SEGMENT1);
                if (string == NULL) {
                    /* 表記文字列が取得できなかった場合、次の候補へ移動 */
                    seg_flg = 0;
                    break;
                }
                /* 削除対象の候補文字列と比較 */
                if (nj_strncmp(tmp_hyoki, string, ing_len) != 0) {
                    /* 一致しない場合、次の候補へ移動 */
                    seg_flg = 0;
                    break;
                }

                /* 比較元データを比較した読み・表記文字列長移動 */
                tmp_yomi += oke_len;
                tmp_ylen += oke_len;
                tmp_hyoki += ing_len;
                tmp_hlen += ing_len;

                /* 削除対象となるキュー番号(文節)情報を保持 */
                del_que_id[del_word_cnt][del_que_cnt[del_word_cnt]] = que_id;
                del_que_cnt[del_word_cnt]++;

                /**
                 * 削除対象の読み・表記文字列長と
                 * 比較対象の読み・表記文字列長が一致した場合
                 */
                if ((ylen == tmp_ylen) && (hlen == tmp_hlen)) {
                    /* 同読み候補は50個までなので、それ以内の場合のみ追加 */
                    if (del_word_cnt < NJD_SAME_INDEX_LIMIT) {
                        del_word_cnt++;
                    }
                    break;
                } else {
                    /* 一致した場合は、次の接続キュー情報を取得 */
                    ret = is_continued(iwnn, handle, que_id);
                    if (ret <= 0) {
                        /* 接続がなかった場合 */
                        seg_flg = 0;
                        break;
                    }
                    que_id = njd_l_search_next_que(handle, que_id);
                }
            } /* while */
        } /* for */

        /* 削除対象の該当個数分処理を実行 */
        for (l = 0; l < del_word_cnt; l++) {
            seg_flg = 1;
            /* 削除対象候補が学習辞書コミット範囲内でないかを確認 */
            for (k = 0; k < del_que_cnt[l]; k++) {
                if (is_commit_range(iwnn, del_que_id[l][k])) {
                    /* 学習辞書コミット範囲のため、処理を抜ける */
                    seg_flg = 0;
                    break;
                }
            }
            /* 学習辞書コミット範囲の場合は、削除せず本候補をSKIPする */
            if (!seg_flg) {
                continue;
            }
            /* 取得接続文節数分、候補削除処理を実行 */
            for (k = 0; k < del_que_cnt[l]; k++) {

                if (iwnn->option_data.ext_mode & NJ_OPT_CLEARED_EXTAREA_WORD) {
                    /**
                     * que_idを基に読み文字列、読み長、候補文字列、候補長を取得し、
                     * NJ_WORD_INFOに取得した情報を設定する。
                     */
                    ret = create_word_info(iwnn, handle, del_que_id[l][k], &info);
                    if (ret < 0) {
                        /* SKIPし、次候補を削除する */
                        continue;
                    }
                }

                /* 単語削除実行 */
                ret = njd_l_delete_index(iwnn, handle, del_que_id[l][k]);
                if (ret <= 0) {
                    /* SKIPし、次候補を削除する */
                    continue;
                }

                if (iwnn->option_data.ext_mode & NJ_OPT_CLEARED_EXTAREA_WORD) {
                    /*
                     * 統合辞書の頻度学習値のクリアする。
                     */
                    ret = delete_word_ext_data(iwnn, &info);
                    /* エラーが発生した場合、SKIPする */
                    if (ret < 0) {
                        /* SKIPし、次候補を削除する */
                        continue;
                    }
                } /* if */
                del_cnt++;
            } /* for(del_que_cnt[l]) */
        } /* for(del_word_cnt) */
    } /* while */
    return del_cnt;
}


/**
 * 単語登録情報を設定する
 *
 * @param[in]       iwnn    解析情報クラス
 * @param[in]     handle    辞書ハンドル
 * @param[in]     que_id    キュー番号
 * @param[out]      info    単語登録情報アドレス
 *
 * @retval     0    設定完了(正常終了)
 * @retval    <0    エラー
 */
static NJ_INT16 create_word_info(NJ_CLASS *iwnn, NJ_DIC_HANDLE handle, NJ_UINT16 que_id, NJ_WORD_INFO *info) {
    NJ_INT16 ret;
    NJ_CHAR *str;
    NJ_UINT8 slen;



    /* 単語登録情報を初期化 */
    ret = njd_init_word_info(info);

    /* 読み文字列情報(読み文字列・読み長)を取得する */
    str = njd_l_get_string(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
    if (str == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_DELETE_WORD, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
    }
    nj_strcpy(info->yomi, str);
    info->stem.yomi_len = slen;

    /* 候補文字列情報(候補文字列・候補長)を取得する */
    str = njd_l_get_hyouki(iwnn, handle, que_id, &slen, NJ_NUM_SEGMENT1);
    if (str == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_L_DELETE_WORD, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
    }
    nj_strcpy(info->kouho, str);
    info->stem.kouho_len = slen;

    return 0;
}


/**
 * 削除した単語情報から統合辞書の統合辞書の頻度学習値を確認し、クリアする
 *
 * @param[in]     iwnn    解析情報クラス
 * @param[out]    info    単語登録情報アドレス
 *
 * @retval     0    クリア完了(正常終了)
 * @retval    <0    エラー
 */
static NJ_INT16 delete_word_ext_data(NJ_CLASS *iwnn, NJ_WORD_INFO *info) {
    NJ_INT16 ret = 0;
    NJ_INT16 i;
    NJ_UINT32 dictype;
    NJ_DIC_INFO *dicinfo;



    /*
     * 統合辞書の頻度学習値のクリアする。
     */
    for (i = 0; i < NJ_MAX_DIC; i++) {
        /* 辞書情報を格納 */
        dicinfo = &(iwnn->dic_set.dic[i]);

        if (dicinfo->handle == NULL) {
            /* 辞書セットがマウントされていない */
            continue;
        }

        /* 擬似辞書、通常辞書の判定 */
        dictype = NJ_GET_DIC_TYPE_EX(NJ_GET_DIC_INFO(dicinfo), dicinfo->handle);

        switch (dictype) {
        case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS:          /* 統合辞書(高圧縮タイプ)               */
        case NJ_DIC_TYPE_FUSION_AWNN:                   /* 統合辞書(AWnnタイプ)                 */
        case NJ_DIC_TYPE_FUSION:                        /* 統合辞書(iWnnタイプ)                 */
            /*
             * 統合辞書の頻度学習値クリアを行うために統合辞書アダプタの呼び出しを行う。
             */
            ret = njd_t_delete_word_ext(iwnn, info, i);
            break;
        case NJ_DIC_TYPE_FUSION_HIGH_COMPRESS_STORAGE:  /* 統合辞書(ストレージ辞書)(iWnnタイプ) */
        case NJ_DIC_TYPE_FUSION_AWNN_STORAGE:           /* 統合辞書(ストレージ辞書)(AWnnタイプ) */
        case NJ_DIC_TYPE_FUSION_STORAGE:                /* 統合辞書(ストレージ辞書)(iWnnタイプ) */
            /*
             * 統合辞書(ストレージ辞書)の頻度学習値クリアを行うために統合辞書(ストレージ辞書)アダプタの呼び出しを行う。
             */
            ret = njd_s_delete_word_ext(iwnn, info, i);
            break;
        default:
            ret = 0;
            break;
        };

        /* 辞書セットを処理中にエラーが検出された場合、すぐにループを抜けてエラーを返す */
        if (ret < 0) {
            break;
        }
    }
    /* エラーが発生した場合、エラーを返す */
    if (ret < 0) {
        return ret;
    }

    return 0;
}
