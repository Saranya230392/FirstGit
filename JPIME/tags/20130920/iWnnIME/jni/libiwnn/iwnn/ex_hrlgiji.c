/**
 * @file
 *  [拡張] 品詞繋がり学習擬似候補作成辞書 (UTF16/SJIS版)
 *
 * 品詞繋がり学習擬似候補の作成を行う辞書。
 *
 * @author
 * (C) OMRON SOFTWARE Co., Ltd. 2009-2012 All Rights Reserved.
 */
#include "ex_hrlgiji.h"
#include "nj_ext.h"
#include "nj_dic.h"
#include "njd.h"
#include "nj_err.h"

/************************************************/
/*              define  宣  言                  */
/************************************************/
/** インデックス領域属性数 */
#define NJG_HRL_ATTR_CNT      32

/** 学習辞書データサイズ */
#define NJG_HRL_LEARN_DATA_SIZE (80 + (NJG_HRL_LEARN_COUNT * 42))

#define NJG_HRL_EXT_ID_LEN               (4)             /**< 辞書ヘッダ: 拡張情報データ識別子サイズ */
#define NJG_HRL_EXT_IDENTIFIER           0x4e4a4747      /**< 辞書ヘッダ: 拡張情報データ識別子       */
#define NJG_HRL_EXT_HEAD_SIZE            0x28            /**< 辞書ヘッダ: 拡張情報データヘッダサイズ */

/**
 * 拡張情報データ：インデックス領域オフセットの位置
 */
#define NJG_HRL_POS_INDEX_OFFSET             0x08
 /**
 * 拡張情報データ：インデックス領域後品詞登録数の位置
 */
#define NJG_HRL_POS_BPOS_INDEX_CNT           0x10
/**
 * 拡張情報データ：UNDO管理領域オフセットの位置
 */
#define NJG_HRL_POS_UNDO_OFFSET              0x14
/**
 * 拡張情報データ：学習辞書データオフセットの位置
 */
#define NJG_HRL_POS_LEARN_DATA_OFFSET        0x1C
 /**
 * 拡張情報内学習辞書データ：辞書ヘッダー内の格納位置
 */
#define NJG_HRL_POS_DATA_OFFSET              0x20
/*
 * 拡張情報内学習辞書データ：候補順インデックス登録数の位置
 */
#define NJG_HRL_POS_LEARN_WORD   0x24    /**< 単語登録数記録位置 */
#define NJG_HRL_POS_MAX_WORD     0x28    /**< 最大登録数記録位置 */
#define NJG_HRL_POS_QUE_SIZE     0x2C    /**< キューサイズ記録位置 */
#define NJG_HRL_POS_NEXT_QUE     0x30    /**< 次キュー記録位置 */
#define NJG_HRL_POS_WRITE_FLG    0x34    /**< 書き込みフラグ記録位置 */
/**
 * 拡張情報内学習辞書データ：データの読み文字列先頭オフセット
 */
#define NJG_HRL_LEARN_QUE_STRING_OFFSET 5
/**
 * 拡張情報内学習辞書データ：拡張キューのサイズ
 */
#define NJG_HRL_LEARN_DIC_EXT_QUE_SIZE    6
/**
 * 拡張情報内学習辞書データ：拡張単語情報領域オフセットの位置
 */
#define NJG_HRL_POS_EXT_DATA_OFFSET     0x44

/** UNDO管理領域UNDO非対象マスク */
#define NJG_HRL_NON_UNDO_MASK         0x8000


/************************************************/
/*              マ ク ロ 宣 言                  */
/************************************************/
#define GET_UINT16(ptr) ((((NJ_UINT16)(*(ptr))) << 8) | (*((ptr) + 1) & 0x00ff))
#define STATE_COPY(to, from)                                    \
    { ((NJ_UINT8*)(to))[0] = ((NJ_UINT8*)(from))[0];            \
        ((NJ_UINT8*)(to))[1] = ((NJ_UINT8*)(from))[1];          \
        ((NJ_UINT8*)(to))[2] = ((NJ_UINT8*)(from))[2];          \
        ((NJ_UINT8*)(to))[3] = ((NJ_UINT8*)(from))[3]; }
#define NJG_HRL_LEARN_DATA_ADDR(x) ((x) + (NJ_INT32_READ((x) + NJG_HRL_POS_LEARN_DATA_OFFSET)))
#define NJG_HRL_GET_BPOS_INDEX_CNT(x) (NJ_UINT16)(NJ_INT32_READ((x) + NJG_HRL_POS_BPOS_INDEX_CNT))
#define NJG_HRL_ATTR_INDEX_TOP_ADDR(x) ((x) + (NJ_INT32_READ((x) + NJG_HRL_POS_INDEX_OFFSET)))
#define NJG_HRL_UNDO_TOP_ADDR(x) ((x) + (NJ_INT32_READ((x) + NJG_HRL_POS_UNDO_OFFSET)))
#define NJG_HRL_GET_LEARN_MAX_WORD_COUNT(h) ((NJ_UINT16)NJ_INT32_READ((h) + NJG_HRL_POS_MAX_WORD))
#define NJG_HRL_GET_LEARN_WORD_COUNT(h) ((NJ_UINT16)NJ_INT32_READ((h) + NJG_HRL_POS_LEARN_WORD))
#define NJG_HRL_GET_LEARN_NEXT_WORD_POS(h) ((NJ_UINT16)NJ_INT32_READ((h) + NJG_HRL_POS_NEXT_QUE))
#define NJG_HRL_QUE_SIZE(h)     ((NJ_UINT16)NJ_INT32_READ((h) + NJG_HRL_POS_QUE_SIZE))
#define NJG_HRL_POS_TO_EXT_ADDRESS(x,pos)   (NJG_HRL_LEARN_EXT_DATA_TOP_ADDR(x) + NJG_HRL_LEARN_DIC_EXT_QUE_SIZE * (pos))
#define NJG_HRL_LEARN_EXT_DATA_TOP_ADDR(x)  ((x) + (NJ_INT32_READ((x) + NJG_HRL_POS_EXT_DATA_OFFSET)))
#define NJG_HRL_LEARN_DATA_TOP_ADDR(x)  ((x) + (NJ_INT32_READ((x) + NJG_HRL_POS_DATA_OFFSET)))
#define NJG_HRL_POS_TO_ADDRESS(x,pos)   (NJG_HRL_LEARN_DATA_TOP_ADDR(x) + NJG_HRL_QUE_SIZE(x) * (pos))
#define NJG_HRL_GET_UFLG_FROM_DATA(x) (*(x) >> 7)
/**
 * 学習辞書データ  検索位置からキューIDを取得
 *
 * @param[in]     x : current
 *
 * @return            キューID
 */
#define NJG_HRL_GET_QUE_ID_FROM_CURRENT(x) ((NJ_UINT16)((x) >> 16))
#define NJG_HRL_GET_TYPE_FROM_DATA(x) (*(x) & 0x03)
#define NJG_HRL_GET_PREV_POS(x, y) (NJ_UINT16)(((x) == 0) ? ((y) - 1) : ((x) - 1))
#define NJG_HRL_GET_NEXT_POS(x, y) (NJ_UINT16)(((x) >= ((y) - 1)) ? 0 : ((x) + 1))
#define NJG_HRL_GET_DATA(x, pos) (NJ_UINT16)(GET_UINT16((x) + (((pos) + 1) * NJ_INDEX_SIZE)))
#define NJG_HRL_GET_INDEX_TOP(x, y) (NJG_HRL_ATTR_INDEX_TOP_ADDR(x) + ((y) * NJ_INDEX_SIZE * (NJG_HRL_INDEX_CNT + 1)))


/***********************************************************************
 * 構造体宣言
 ***********************************************************************/

/***********************************************************************
 * 辞書インタフェース プロトタイプ宣言
 ***********************************************************************/

/***********************************************************************
 * 内部関数 プロトタイプ宣言
 ***********************************************************************/
static NJ_INT16 njg_hrl_search_word(NJ_CLASS *iwnn, NJ_UINT8 *ext_data, NJ_SEARCH_LOCATION_SET *loctset);
static NJ_INT16 njg_hrl_get_word(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word);
static NJ_INT16 njg_hrl_get_stroke(NJ_CLASS *iwnn, NJ_WORD *word, NJ_CHAR *stroke, NJ_UINT16 size);
static NJ_INT16 njg_hrl_get_candidate(NJ_CLASS *iwnn, NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size);
static NJ_INT16 njg_hrl_add_word(NJ_CLASS *iwnn, NJ_UINT8 *ext_data, NJ_LEARN_WORD_INFO *word, NJ_UINT8 connect, NJ_UINT16 *que_id);
static NJ_INT16 njg_hrl_learn_word(NJ_CLASS *iwnn, NJ_UINT8 *ext_data, NJ_LEARN_WORD_INFO *lword, NJ_UINT8 connect);
static NJ_INT16 njg_hrl_set_learn_word_info(NJ_CLASS *iwnn, NJ_LEARN_WORD_INFO *lword, NJ_PROGRAM_DIC_MESSAGE *message);
static NJ_INT16 njg_hrl_undo_learn(NJ_CLASS *iwnn, NJ_UINT8 *ext_data, NJ_UINT16 undo_count);

/***********************************************************************/

/**
 * 品詞繋がり学習用擬似辞書 辞書インタフェース
 *
 * @param[in,out] iwnn      iWnn内部情報(通常は参照のみ)
 * @param[in]     request   iWnnからの処理要求
 *                          - NJG_OP_SEARCH：初回検索
 *                          - NJG_OP_SEARCH_NEXT：次候補検索
 *                          - NJG_OP_GET_WORD_INFO：単語情報取得
 *                          - NJG_OP_GET_STROKE：読み文字列取得
 *                          - NJG_OP_LEARN：学習
 *                          - NJG_OP_LEARN_UNDO：学習UNDO
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
 *             [NJG_OP_GET_ADDITIONALの場合]<br>
 *                付加情報文字列長<br>
 *             [NJG_OP_LEARNの場合]<br>
 *                0:正常終了、<0:エラー
 *             [NJG_OP_UNDO_LEARNの場合]<br>
 *                0:正常終了、<0:エラー
 *
 * @retval <0  エラー
 */
NJ_EXTERN NJ_INT16 njex_hrl_giji_dic(NJ_CLASS *iwnn, NJ_UINT16 request, NJ_PROGRAM_DIC_MESSAGE *message) {
    NJ_UINT8 *ext_data = NULL;
    NJ_LEARN_WORD_INFO learn_word;
    NJ_LEARN_WORD_INFO *lword;
    NJ_UINT32 dic_type;
    NJ_UINT8 learn;

    if ((request != NJG_OP_GET_STROKE) && (request != NJG_OP_GET_STRING) && (request != NJG_OP_GET_ADDITIONAL)) {
        if (iwnn->dic_set.dic[message->dic_idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] == NULL) {
            /* 拡張領域がNULLの場合は、候補作成および学習不可とする */
            if ((request == NJG_OP_SEARCH) || (request == NJG_OP_SEARCH_NEXT)) {
                message->location->loct.status = NJ_ST_SEARCH_END_EXT;
            }
            return 0;
        } else {
            ext_data = (NJ_UINT8 *)iwnn->dic_set.dic[message->dic_idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT];
        }
    }

    switch (request) {
    case NJG_OP_SEARCH:
        if ((message->condition->operation != NJ_CUR_OP_LINK) ||
            (message->condition->mode != NJ_CUR_MODE_FREQ) ||
            (iwnn->njc_mode != NJC_MODE_NOT_CONVERTED) ||
            (iwnn->previous_selection.count == 0)) {
            /* つながり検索、頻度順、前確定候補ありのみ対応 */
            message->location->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }
        /* 拡張情報内を検索 */
        return njg_hrl_search_word(iwnn, ext_data, message->location);

    case NJG_OP_SEARCH_NEXT:
        /* 拡張情報内を検索 */
        return njg_hrl_search_word(iwnn, ext_data, message->location);

    case NJG_OP_GET_WORD_INFO:
        /* 単語を取得 */
        return njg_hrl_get_word(iwnn, message->location, message->word);

    case NJG_OP_GET_STROKE:
        /* 読み文字列を作成する */
        return njg_hrl_get_stroke(iwnn, message->word, message->stroke, message->stroke_size);

    case NJG_OP_GET_STRING:
        /* 表記文字列を作成する */
        return njg_hrl_get_candidate(iwnn, message->word, message->string, message->string_size);

    case NJG_OP_GET_ADDITIONAL:
        /* 付加情報文字列は対応しない */
        *(message->additional) = NJ_CHAR_NUL;
        message->additional_size = 0;
        return 0;

    case NJG_OP_LEARN:
        learn = 0;
        if ((iwnn->previous_selection.count != 0) && (message->word->stem.loc.handle != NULL)) {
            dic_type = NJ_GET_DIC_TYPE_EX(message->word->stem.loc.type, message->word->stem.loc.handle);
            if ((dic_type == NJ_DIC_TYPE_YOMINASHI) || (dic_type == NJ_DIC_TYPE_EXT_YOMINASI)) {
                /* 確定した候補の辞書が拡張読み無し予測辞書または読み無し予測辞書の場合 */
                learn = 1;
            }
            if ((learn == 0) && (message->word->stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT] != NULL)) {
                if (NJ_INT32_READ((NJ_UINT8 *)message->word->stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]) == NJG_HRL_EXT_IDENTIFIER) {
                    /* 確定した候補の辞書が品詞繋がり学習擬似辞書の場合 */
                    learn = 1;
                }
            }
        }
        if (learn != 0) {
            /* 学習対象 */
            lword = &learn_word;
            /* 学習単語情報を作成する */
            njg_hrl_set_learn_word_info(iwnn, &learn_word, message);
        } else {
            /* 学習非対象 */
            lword = NULL;
        }

        /* 学習を行う */
        return njg_hrl_learn_word(iwnn, ext_data, lword, (NJ_UINT8)message->lword->connect);

    case NJG_OP_UNDO_LEARN:
        /* 学習UNDOを行う */
        return njg_hrl_undo_learn(iwnn, ext_data, message->lword->undo);

    case NJG_OP_ADD_WORD:
    case NJG_OP_DEL_WORD:
        return 0;
    default:
        break;
    }
    return -1; /* エラー */
}

/**
 * 単語検索
 *
 * @param[in]     iwnn : 解析情報クラス
 * @param[in] ext_data : 拡張情報
 * @param[out] loctset : 検索位置
 *
 * @retval            0   検索候補なし
 * @retval            1   検索候補あり
 * @retval           <0   エラー
 */
static NJ_INT16 njg_hrl_search_word(NJ_CLASS *iwnn, NJ_UINT8 *ext_data, NJ_SEARCH_LOCATION_SET *loctset) {
    NJ_UINT16 i;
    NJ_UINT16 word_count;
    NJ_UINT16 que_id = 0;
    NJ_UINT16 cur_que_id = 0;
    NJ_UINT16 next_que_id = 0;
    NJ_UINT16 next_que_top = 0;
    NJ_UINT16 que_id_tmp;
    NJ_UINT32 current;
    NJ_UINT16 search_attr;
    NJ_UINT8  *ext_ptr;
    NJ_UINT32 attr_mask;
    NJ_UINT16 prev_idx;
    NJ_UINT8 *attr_tmp;
    NJ_UINT8 *learn_data_addr;
    NJ_UINT8 *index_top;
    NJ_UINT16 pos, next_pos, cur_pos;
    NJ_UINT8  found = 0;
    NJ_UINT16 search_type = 0;
    NJ_UINT16 count;

    learn_data_addr = NJG_HRL_LEARN_DATA_ADDR(ext_data);

    /* LOCATIONの現在状態の判別 */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT) {
        /* 初回検索 */
        word_count = NJG_HRL_GET_LEARN_WORD_COUNT(learn_data_addr);
        if (word_count == 0) {
            /* 条件に合う登録単語がない */
            loctset->loct.status = NJ_ST_SEARCH_END_EXT;
            return 0;
        }

        /* 前確定の属性と後品詞を取得 */
        prev_idx = iwnn->previous_selection.selection_now;
        attr_tmp = (NJ_UINT8*)&(iwnn->previous_selection.selection_data[prev_idx].attr);
        loctset->loct.top = NJ_INT32_READ(attr_tmp);
        loctset->loct.bottom = iwnn->previous_selection.selection_data[prev_idx].b_hinsi;

        if (loctset->loct.top & NJG_HRL_ATTR_MASK) {
            /* 検索開始を属性にする */
            search_attr = 1;

            /* 学習辞書データの「次キュー追加位置」を取得 */
            next_que_id = NJG_HRL_GET_LEARN_NEXT_WORD_POS(learn_data_addr);

            /* 最新のキューIDを取得する */
            cur_que_id = njd_l_search_prev_que(learn_data_addr, next_que_id);
            que_id = cur_que_id;

            /* 最新のキューIDの次の有効なキューIDを取得 */
            next_que_top = njd_l_search_next_que(learn_data_addr, cur_que_id);

            if (next_que_top == 0) {
                /* 検索順は最新のキューIDからキューID=0まで */
                search_type = 0;
                current = cur_que_id + 1;
            } else {
                /* 検索順は最新のキューIDから一回転して最新のキューIDのひとつ手前まで */
                search_type = 1;
                current = 0;
            }
        } else {
            /* 検索開始を後品詞にする */
            search_attr = 0;
            current = 0;
        }
    } else if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_READY) {
        /* 次回検索 */
        current = (NJ_UINT16)(loctset->loct.current & 0xFFFF);
        search_attr = (NJ_UINT16)(loctset->loct.current >> 16);
        if (search_attr != 0) {

            /* 学習辞書データの「次キュー追加位置」を取得 */
            next_que_id = NJG_HRL_GET_LEARN_NEXT_WORD_POS(learn_data_addr);

            /* 最新のキューIDを取得する */
            cur_que_id = njd_l_search_prev_que(learn_data_addr, next_que_id);

            /* 最新のキューIDの次の有効なキューIDを取得 */
            next_que_top = njd_l_search_next_que(learn_data_addr, cur_que_id);

            if (next_que_top == 0) {
                /* 検索順は最新のキューIDからキューID=0まで */
                search_type = 0;

                if ((current - 1) == 0) {
                    /* 前回検索位置がキューID=0の場合は、属性の検索を終了 */
                    search_attr = 0;
                    current = 0;
                } else {
                    /* 直前のキューIDを取得する */
                    que_id = njd_l_search_prev_que(learn_data_addr, loctset->loct.current_cache);
                    current = que_id + 1;
                }
            } else {
                /* 検索順は最新のキューIDから一回転して最新のキューIDのひとつ手前まで */
                search_type = 1;

                /* 直前のキューIDを取得する */
                que_id = njd_l_search_prev_que(learn_data_addr, loctset->loct.current_cache);
                current = que_id + 1;
            }
        }
    } else {
        /* 検索対象外 */
        loctset->loct.status = NJ_ST_SEARCH_END_EXT;
        return 0; /*NCH*/
    }

    if (search_attr != 0) {
        /* 属性インデックス領域を検索 */
        while (((search_type == 0) && (current > 0)) ||
               ((search_type != 0) && ((NJ_UINT32)(cur_que_id + 1) != current))) {
            if (current == 0) {
                current = que_id + 1;
            }
            found = 0;
            attr_mask = 0x80000000;
            for (i = 0; i < NJG_HRL_ATTR_CNT; i++, attr_mask >>= 1) {
                if (loctset->loct.top & attr_mask) {
                    /* 対象属性の場合 */
                    index_top = NJG_HRL_GET_INDEX_TOP(ext_data, i);

                    /* 次回書込位置を取得 */
                    next_pos = GET_UINT16(index_top);

                    /* 最新位置を取得 */
                    pos = NJG_HRL_GET_PREV_POS(next_pos, NJG_HRL_INDEX_CNT);

                    count = NJG_HRL_INDEX_CNT;
                    while (count > 0) {
                        que_id_tmp = NJG_HRL_GET_DATA(index_top, pos);
                        if (que_id_tmp == 0) {
                            break;
                        }
                        if ((que_id_tmp - 1) == que_id) {
                            /* 該当するキューIDが見つかった */
                            found = 1;
                            current = que_id + 1;
                            break;
                        }
                        /* 位置を１つ前にずらす */
                        pos = NJG_HRL_GET_PREV_POS(pos, NJG_HRL_INDEX_CNT);
                        count--;
                    }
                    if (found) {
                        break;
                    }
                }
            }
            if (found) {
                break;
            } else {
                if ((search_type == 0) && (que_id ==0)) {
                    /* 検索順がキューID=0までの場合はここで終了する */
                    current = 0;
                } else {
                    /* 直前のキューIDを取得する */
                    que_id = njd_l_search_prev_que(learn_data_addr, (NJ_UINT16)(current - 1));
                    current = que_id + 1;
                }
            }
        }
    }

    if (found == 0) {
        if (search_attr != 0) {
            /* 属性検索の直後の為、検索位置を初期化 */
            search_attr = 0;
            current = 0;
        }

        index_top = NJG_HRL_GET_INDEX_TOP(ext_data, ((NJG_HRL_ATTR_CNT - 1) + (loctset->loct.bottom - 1)));

        /* 次回書込位置を取得 */
        next_pos = GET_UINT16(index_top);

        /* 最新位置を取得 */
        cur_pos = NJG_HRL_GET_PREV_POS(next_pos, NJG_HRL_INDEX_CNT);

        if (current == 0) {
            /* 最新位置のキューIDを取得 */
            que_id_tmp = NJG_HRL_GET_DATA(index_top, cur_pos);
            if (que_id_tmp != 0) {
                current = cur_pos + 1;
                que_id = que_id_tmp - 1;
            }
        } else {
            /* currentのひとつ前のキューIDを取得 */
            pos = NJG_HRL_GET_PREV_POS((current - 1), NJG_HRL_INDEX_CNT);
            if (cur_pos == pos) {
                /* 一回転したので検索終了 */
                current = 0;
            } else {
                que_id_tmp = NJG_HRL_GET_DATA(index_top, pos);
                if (que_id_tmp == 0) {
                    current = 0;
                } else {
                    current = pos + 1;
                    que_id = que_id_tmp - 1;
                }

            }
        }
    }

    /* 対象はなかった */
    if (current == 0) {
        loctset->loct.status = NJ_ST_SEARCH_END_EXT;
        return 0;
    }

    /* 現在位置を設定 */
    loctset->loct.current = current;
    loctset->loct.current &= 0x0000ffff;
    loctset->loct.current |= ((NJ_UINT32)(search_attr) << 16);
    loctset->loct.current_cache = que_id;
    loctset->loct.current_info = 0x10;
    ext_ptr = NJG_HRL_POS_TO_EXT_ADDRESS(learn_data_addr, que_id);
    STATE_COPY(&loctset->loct.attr, ext_ptr);
    loctset->loct.status = NJ_ST_SEARCH_READY;
    loctset->cache_freq  = (NJ_HINDO)njd_l_get_attr_hindo(iwnn, loctset, learn_data_addr, que_id, &current);

    return 1;
}

/**
 * 検索位置の単語を取得する
 *
 * @param[in]     iwnn : 解析情報クラス
 * @param[in]  loctset : 検索位置
 * @param[out]    word : 単語情報
 *
 * @retval           0   正常終了
 * @retval          <0   エラー
 */
static NJ_INT16 njg_hrl_get_word(NJ_CLASS *iwnn, NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word) {
    NJ_WQUE *que;
    NJ_UINT16 que_id;

    que_id = loctset->loct.current_cache;

    que = njd_l_get_que(iwnn, NJG_HRL_LEARN_DATA_ADDR((NJ_UINT8*)loctset->loct.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]), que_id);
    if (que == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_WORD_INFO, NJ_ERR_CANNOT_GET_QUE); /*NCH_FB*/
    }

    word->stem.loc = loctset->loct;

    /* 各検索ロジックから算出された頻度をそのまま格納 */
    word->stem.hindo = loctset->cache_freq;

    NJ_SET_FPOS_TO_STEM(word, que->mae_hinsi);
    NJ_SET_YLEN_TO_STEM(word, que->yomi_len);
    NJ_SET_KLEN_TO_STEM(word, que->hyouki_len);
    NJ_SET_BPOS_TO_STEM(word, que->ato_hinsi);
    NJ_SET_EXT_YLEN_TO_STEM(word, 0);

    /* 擬似候補の種類をクリア */
    word->stem.type = 0;

    return 0;
}

/**
 * 読み文字列を取得する
 *
 * @param[in]    iwnn : 解析情報クラス
 * @param[in]    word : 単語情報
 * @param[out] stroke : 読み文字列
 * @param[in]    size : strokeのbyteサイズ
 *
 * @retval         >0   取得した文字配列長（ヌル文字含まず）
 * @retval         <0   エラー
 */
static NJ_INT16 njg_hrl_get_stroke(NJ_CLASS *iwnn, NJ_WORD *word, NJ_CHAR *stroke, NJ_UINT16 size) {
    NJ_UINT16 que_id;
    NJ_CHAR   *str;
    NJ_UINT8  slen;
    NJ_UINT8  ylen;

    que_id = word->stem.loc.current_cache;

    /* バッファサイズチェック */
    ylen = (NJ_UINT8)NJ_GET_YLEN_FROM_STEM(word);
    if ((NJ_UINT16)((ylen + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) {
        /* バッファサイズエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STROKE, NJ_ERR_BUFFER_NOT_ENOUGH);
    }
    if (ylen == 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STROKE, NJ_ERR_INVALID_RESULT); /*NCH_FB*/
    }
    str = njd_l_get_string(iwnn, NJG_HRL_LEARN_DATA_ADDR((NJ_UINT8*)word->stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]), que_id, &slen, NJ_NUM_SEGMENT1);

    /* 取得できなかった */
    if (str == NULL) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_STROKE, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
    }

    /* NJ_UINT8 型文字列をコピー */
    nj_strcpy(stroke, str);

    return slen;
}

/**
 * 表記文字列を取得する
 *
 * @param[in]       iwnn : 解析情報クラス
 * @param[in]       word : 単語情報
 * @param[out] candidate : 候補文字列
 * @param[in]       size : candidateのバイトサイズ
 *
 * @retval            >0   取得した文字配列長
 * @retval            <0   エラー
 */
static NJ_INT16 njg_hrl_get_candidate(NJ_CLASS *iwnn, NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size) {
    NJ_UINT16 que_id;
    NJ_CHAR   *str;
    NJ_UINT16 klen;
    NJ_UINT8  slen;

    que_id = word->stem.loc.current_cache;

    /* バッファサイズチェック */
    klen = NJ_GET_KLEN_FROM_STEM(word);
    if (size < ((klen + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
        /* バッファサイズエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_CANDIDATE, NJ_ERR_BUFFER_NOT_ENOUGH);
    }
    str = njd_l_get_hyouki(iwnn, NJG_HRL_LEARN_DATA_ADDR((NJ_UINT8*)word->stem.loc.ext_area[NJ_TYPE_EXT_AREA_DEFAULT]), que_id, &slen, NJ_NUM_SEGMENT1);

    if (str == NULL) {
        /* 表記取得エラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_CANDIDATE, NJ_ERR_DIC_BROKEN); /*NCH_FB*/
    }

    /* 文字列をコピー */
    nj_strcpy(candidate, str);

    return slen;
}

/**
 * 学習辞書データに登録する
 *
 * @param[in]     iwnn : 解析情報クラス
 * @param[in] ext_data : 拡張情報
 * @param[in]     word : 学習単語情報
 * @param[in]  connect : 関係学習（0:行わない、0以外:行う）
 * @param[out] *que_id : 追加したキューID
 *
 * @retval          0   正常終了
 * @retval         <0   エラー
 */
static NJ_INT16 njg_hrl_add_word(NJ_CLASS *iwnn, NJ_UINT8* ext_data, NJ_LEARN_WORD_INFO *word, NJ_UINT8 connect, NJ_UINT16 *que_id) {
    NJ_INT16 ret;
    NJ_UINT8 *learn_data_addr;

    learn_data_addr = NJG_HRL_LEARN_DATA_ADDR(ext_data);

    /* 登録位置は、辞書ヘッダの「次キュー追加位置」 */
    *que_id = NJG_HRL_GET_LEARN_NEXT_WORD_POS(learn_data_addr);

    /* インデックスから削除し、キューを未使用にする */
    ret = njd_l_delete_index(iwnn, learn_data_addr, *que_id);
    if (ret < 0) {
        /* インデックス削除時に何らかのエラーが発生した */
        return ret; /*NCH_FB*/
    }

    /* データ領域への追加 */
    ret = njd_l_write_learn_data(iwnn, learn_data_addr, *que_id, word, connect, 0, 1, 0);
    if (ret < 0) {
        return ret; /*NCH_FB*/
    }

    return 0;
}

/**
 * 学習通知から学習処理を行う。
 *
 * @param[in]  iwnn      解析情報クラス
 * @param[in]  ext_data  拡張情報
 * @param[in]  lword     単語登録情報（NULLの場合は学習対象外）
 * @param[in]  connect   関係学習（0:行わない、0以外:行う）
 *
 * @retval               0    正常終了
 * @retval              <0    エラー
 */
static NJ_INT16 njg_hrl_learn_word(NJ_CLASS *iwnn, NJ_UINT8* ext_data, NJ_LEARN_WORD_INFO *lword, NJ_UINT8 connect) {
    NJ_INT16 ret;
    NJ_UINT16 prev_idx;
    NJ_UINT32 prev_attr;
    NJ_UINT16 prev_bpos;
    NJ_UINT32 attr_mask;
    NJ_UINT8 *attr_tmp;
    NJ_UINT16 i;
    NJ_UINT8 *index_top;
    NJ_UINT16 que_id = 0;
    NJ_UINT16 que_id2, que_id_tmp;
    NJ_UINT16 next_que_id;
    NJ_UINT8 *learn_data_addr;
    NJ_UINT8 *ptr;
    NJ_UINT16 index_cnt;
    NJ_UINT16 pos, next_pos;
    NJ_UINT8 *undo_top;
    NJ_UINT16 undo_info_tmp;
    NJ_UINT16 count;
    NJ_UINT16 separate_flag = 0;

    if (lword != NULL) {
        learn_data_addr = NJG_HRL_LEARN_DATA_ADDR(ext_data);

        /* 学習辞書データを格納 */
        ret = njg_hrl_add_word(iwnn, ext_data, lword, connect, &que_id);
        if (ret < 0) {
            return ret; /*NCH_FB*/
        }

        /* インデックス登録数を取得 */
        index_cnt = NJG_HRL_ATTR_CNT + NJG_HRL_GET_BPOS_INDEX_CNT(ext_data);

        /* 学習辞書データの「次キュー追加位置」を取得 */
        next_que_id = NJG_HRL_GET_LEARN_NEXT_WORD_POS(learn_data_addr);

        if (que_id > next_que_id) {
            /* 書き込んだキューがMAX位置から先頭に跨っている */
            separate_flag = 1;
        }

        /* 既存のキューIDをインデックス領域から削除する */
        for (i = 0; i < index_cnt; i++) {
            /* 各インデックス領域の先頭アドレスを取得 */
            index_top = NJG_HRL_GET_INDEX_TOP(ext_data, i);

            /* 次回書込位置を取得 */
            next_pos = GET_UINT16(index_top);

            /* 最新位置を取得 */
            pos = NJG_HRL_GET_PREV_POS(next_pos, NJG_HRL_INDEX_CNT);

            /* 最新位置のキューIDを取得 */
            que_id_tmp = NJG_HRL_GET_DATA(index_top, pos);
            if (que_id_tmp == 0) {
                continue;
            }

            /* 最古位置を取得 */
            count = NJG_HRL_INDEX_CNT - 1;
            while (count > 0) {
                /* 位置を１つ前にずらす */
                pos = NJG_HRL_GET_PREV_POS(pos, NJG_HRL_INDEX_CNT);
                que_id_tmp = NJG_HRL_GET_DATA(index_top, pos);
                if (que_id_tmp == 0) {
                    /* １つ後ろが最古位置となる */
                    pos = NJG_HRL_GET_NEXT_POS(pos, NJG_HRL_INDEX_CNT);
                    break;
                }
                count--;
            }

            /* 最古位置のキューIDを取得 */
            que_id_tmp = NJG_HRL_GET_DATA(index_top, pos);
            if (que_id_tmp == 0) {
                continue;
            }
            que_id2 = que_id_tmp - 1;

            /* 今回上書きで削除されたキューIDが存在した場合0を書き込む */
            count = 0;
            while (((separate_flag != 0) && ((que_id2 >= que_id) || (que_id2 < next_que_id))) ||
                   ((separate_flag == 0) && ((que_id2 >= que_id) && (que_id2 < next_que_id)))) {
                ptr = index_top + ((pos + 1) * NJ_INDEX_SIZE);
                njd_l_write_uint16_data(ptr, 0);

                /* 位置を１つ後ろにずらす */
                pos = NJG_HRL_GET_NEXT_POS(pos, NJG_HRL_INDEX_CNT);

                /* キューIDを取得 */
                que_id_tmp = NJG_HRL_GET_DATA(index_top, pos);
                que_id2 = que_id_tmp - 1;
                
                if (count >= NJG_HRL_INDEX_CNT) {
                    break;
                }
                count++;
            }
        }

        /* 前確定の属性と後品詞を取得 */
        prev_idx = iwnn->previous_selection.selection_now;
        attr_tmp = (NJ_UINT8*)&(iwnn->previous_selection.selection_data[prev_idx].attr);
        prev_attr = NJ_INT32_READ(attr_tmp);
        prev_bpos = iwnn->previous_selection.selection_data[prev_idx].b_hinsi;

        if (NJ_INT32_READ(attr_tmp) & NJG_HRL_ATTR_MASK) {
            attr_mask = 0x80000000;
            for (i = 0; i < NJG_HRL_ATTR_CNT; i++, attr_mask >>= 1) {
                if (prev_attr & attr_mask) {
                    /* 属性インデックス領域の先頭アドレスを取得 */
                    index_top = NJG_HRL_GET_INDEX_TOP(ext_data, i);

                    /* 次回書込位置を取得 */
                    next_pos = GET_UINT16(index_top);

                    /* キューIDを書き込む */
                    njd_l_write_uint16_data(index_top + ((next_pos + 1) * NJ_INDEX_SIZE), (NJ_UINT16)(que_id + 1));

                    /* 次回書込位置を１つ後ろに更新 */
                    pos = NJG_HRL_GET_NEXT_POS(next_pos, NJG_HRL_INDEX_CNT);
                    njd_l_write_uint16_data(index_top, pos);
                }
            }
        }

        /* 後品詞インデックス領域の先頭アドレスを取得 */
        index_top = NJG_HRL_GET_INDEX_TOP(ext_data, ((NJG_HRL_ATTR_CNT - 1) + (prev_bpos - 1)));

        /* 次回書込位置を取得 */
        next_pos = GET_UINT16(index_top);

        /* キューIDを書き込む */
        njd_l_write_uint16_data(index_top + ((next_pos + 1) * NJ_INDEX_SIZE), (NJ_UINT16)(que_id + 1));

        /* 次回書込位置を１つ後ろに更新 */
        pos = NJG_HRL_GET_NEXT_POS(next_pos, NJG_HRL_INDEX_CNT);
        njd_l_write_uint16_data(index_top, pos);
    }

    /* UNDO管理領域を更新 */
    undo_top = NJG_HRL_UNDO_TOP_ADDR(ext_data);

    /* 次回書込位置を取得 */
    next_pos = GET_UINT16(undo_top);

    if (lword != NULL) {
        /* キューIDを書き込む */
        njd_l_write_uint16_data(undo_top + ((next_pos + 1) * NJ_INDEX_SIZE), (NJ_UINT16)(que_id + 1));

        /* 次回書込位置を１つ後ろに更新 */
        pos = NJG_HRL_GET_NEXT_POS(next_pos, NJG_HRL_UNDO_MAX);
        njd_l_write_uint16_data(undo_top, pos);
    } else {
        /* 最新位置を取得 */
        pos = NJG_HRL_GET_PREV_POS(next_pos, NJG_HRL_UNDO_MAX);

        /* 最新位置のUNDO情報を取得 */
        undo_info_tmp = NJG_HRL_GET_DATA(undo_top, pos);

        if (undo_info_tmp == 0) {
            /* 次回書込位置を１つ後ろに更新する */
            next_pos = NJG_HRL_GET_NEXT_POS(next_pos, NJG_HRL_UNDO_MAX);
            njd_l_write_uint16_data(undo_top, next_pos);
            pos = 0;
            undo_info_tmp = 0x8001;
        } else {
            if ((undo_info_tmp & 0x8000) && (undo_info_tmp != 0xFFFF)) {
                /* 最新位置がUNDO非対象の場合は連続回数をカウントアップする */
                undo_info_tmp++;
            } else {
                /* 最新位置がUNDO対象の場合は連続回数を1にする */
                undo_info_tmp = 0x8001;

                /* 次回書込位置を１つ後ろに更新する */
                next_pos = NJG_HRL_GET_NEXT_POS(next_pos, NJG_HRL_UNDO_MAX);
                njd_l_write_uint16_data(undo_top, next_pos);

                /* 書込位置を１つ後ろにする */
                pos = NJG_HRL_GET_NEXT_POS(pos, NJG_HRL_UNDO_MAX);
            }
        }
        /* UNDO連続回数を書き込む */
        njd_l_write_uint16_data(undo_top + ((pos + 1) * NJ_INDEX_SIZE), undo_info_tmp);
    }

    return 0;
}

/**
 * 擬似辞書メッセージ情報から学習単語情報を作成する
 *
 * @param[in,out] iwnn     解析情報クラス
 * @param[out]    lword    学習単語情報
 * @param[in]     message  擬似辞書メッセージ
 *
 * @return        常に0
 */
static NJ_INT16 njg_hrl_set_learn_word_info(NJ_CLASS *iwnn, NJ_LEARN_WORD_INFO *lword, NJ_PROGRAM_DIC_MESSAGE *message) {

    /* 付加情報文字列を lwordに追加する(対応なし) */
    *(lword->additional) = NJ_CHAR_NUL;
    lword->additional_len = 0;

    /* よみ文字列、表記文字列を lword に追加する */
    nj_strcpy(lword->yomi, message->stroke);
    lword->yomi_len = (NJ_UINT8)message->stroke_size;

    nj_strcpy(lword->hyouki, message->string);
    lword->hyouki_len = (NJ_UINT8)message->string_size;

    /* 属性データを格納 */
    lword->attr = message->word->stem.loc.attr;

    /* 前品詞、後品詞を lwordに追加する */
    lword->f_hinsi = message->lword->f_hinsi;
    lword->b_hinsi = message->lword->b_hinsi;

    /* 自立語の後品詞、付属語の読み長を lwordに追加する */
    lword->stem_b_hinsi = NJ_GET_BPOS_FROM_STEM(message->word);
    lword->fzk_yomi_len = NJ_GET_YLEN_FROM_FZK(message->word);

    return 0;
}

/**
 * 指定回数分だけ学習アンドゥを行う
 *
 * @param[in]       iwnn : 解析情報クラス
 * @param[in]   ext_data : 拡張情報
 * @param[in] undo_count : アンドゥ回数
 *
 * @retval            <0   エラー
 * @retval           >=0   アンドゥできた回数
 */
static NJ_INT16 njg_hrl_undo_learn(NJ_CLASS *iwnn, NJ_UINT8 *ext_data, NJ_UINT16 undo_count) {
    NJ_INT16 ret;
    NJ_INT16 ret_cnt = 0;                       /* アンドゥできた回数 */
    NJ_UINT16 que_id, que_id_tmp;
    NJ_UINT16 word_count;
    NJ_UINT16 i;
    NJ_UINT16 index_cnt;
    NJ_UINT16 pos, next_pos;
    NJ_UINT16 undo_info_tmp;
    NJ_UINT16 undo_next_pos, undo_cur_pos;
    NJ_UINT8 *ptr;
    NJ_UINT8 *learn_data_addr;
    NJ_UINT8 *undo_top;
    NJ_UINT8 *index_top;

    /* アンドゥ回数=0なら正常終了 */
    if (undo_count == 0) {
        return 0;
    }

    learn_data_addr = NJG_HRL_LEARN_DATA_ADDR(ext_data);

    /* 現在登録数を取得する */
    /* （UNDOなので登録語数で良い） */
    word_count = NJG_HRL_GET_LEARN_WORD_COUNT(learn_data_addr);
    if (word_count == 0) {
        /* 登録語数=0、 */
        /*（指定回数分アンドゥできなくてもエラー扱いにしない） */
        return 0;
    }

    /* UNDO管理領域の先頭アドレスを取得 */
    undo_top = NJG_HRL_UNDO_TOP_ADDR(ext_data);

    while (undo_count) {
        /* 次回書込位置を取得 */
        undo_next_pos = GET_UINT16(undo_top);

        /* 最新位置を取得 */
        undo_cur_pos = NJG_HRL_GET_PREV_POS(undo_next_pos, NJG_HRL_UNDO_MAX);

        /* 最新位置のUNDO情報を取得 */
        undo_info_tmp = NJG_HRL_GET_DATA(undo_top, undo_cur_pos);

        if (undo_info_tmp == 0) {
            /* UNDO情報なし */
            break;
        } else if (undo_info_tmp & NJG_HRL_NON_UNDO_MASK) {
            /* UNDO情報がUNDO非対象の場合 */
            undo_info_tmp &= ~NJG_HRL_NON_UNDO_MASK;
            if (undo_info_tmp <= undo_count) {
                undo_count -= undo_info_tmp;
                undo_info_tmp = 0;
            } else {
                undo_info_tmp -= undo_count;
                undo_info_tmp |= NJG_HRL_NON_UNDO_MASK;
                undo_count = 0;
            }

            if (undo_info_tmp == 0) {
                /* 次回書込位置を１つ前に更新する */
                njd_l_write_uint16_data(undo_top, undo_cur_pos);
            }

            /* UNDO情報を更新する */
            njd_l_write_uint16_data(undo_top + ((undo_cur_pos + 1) * NJ_INDEX_SIZE), undo_info_tmp);
        } else {
            /* 学習辞書データを削除する */
            que_id = undo_info_tmp - 1;

            /* カウントを減算 */
            undo_count--;
            ret_cnt++;

            /* インデックスから削除 */
            ret = njd_l_delete_index(iwnn, learn_data_addr, que_id);
            if (ret < 0) {
                /* インデックス削除時に何らかのエラーが発生した */
                return ret; /*NCH_FB*/
            }

            /* 次キュー追加位置更新 */
            njd_l_write_uint16_data(learn_data_addr + NJG_HRL_POS_NEXT_QUE + 2, que_id);

            /* 属性、後品詞インデックス領域から削除 */
            index_cnt = NJG_HRL_ATTR_CNT + NJG_HRL_GET_BPOS_INDEX_CNT(ext_data);
            for (i = 0; i < index_cnt; i++) {
                /* 各インデックス領域の先頭アドレスを取得 */
                index_top = NJG_HRL_GET_INDEX_TOP(ext_data, i);

                /* 次回書込位置を取得 */
                next_pos = GET_UINT16(index_top);

                /* 最新位置を取得 */
                pos = NJG_HRL_GET_PREV_POS(next_pos, NJG_HRL_INDEX_CNT);

                /* 最新位置のUNDO情報を取得 */
                que_id_tmp = NJG_HRL_GET_DATA(index_top, pos);

                if (que_id_tmp == 0) {
                    continue;
                }

                /* 対象のキューIDが存在した場合0を書き込み、次回書込位置を１つ前にする */
                if (que_id == (que_id_tmp - 1)) {
                    /* キューIDを削除する */
                    ptr = index_top + ((pos + 1) * NJ_INDEX_SIZE);
                    njd_l_write_uint16_data(ptr, 0);

                    /* 次回書込位置を１つ前に更新する */
                    njd_l_write_uint16_data(index_top, pos);
                }
            }

            /* UNDO情報を削除する */
            njd_l_write_uint16_data(undo_top + ((undo_cur_pos + 1) * NJ_INDEX_SIZE), 0);

            /* 次回書込位置を１つ前に更新する */
            njd_l_write_uint16_data(undo_top, undo_cur_pos);
        }
    }
    return ret_cnt;
}

/**
 * 品詞繋がり学習擬似辞書拡張情報データサイズ取得API
 *
 * 拡張情報データサイズを取得する。
 *
 * @param[in]  iwnn     解析情報クラス
 * @param[in]  rhandle  ルール辞書ハンドル
 * @param[out] size     拡張情報データサイズ
 *
 * @retval 0  正常終了
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njex_hrl_get_ext_area_size(NJ_CLASS *iwnn, NJ_DIC_HANDLE rhandle, NJ_UINT32 *size) {
    NJ_UINT32 ret_size;
    NJ_UINT16 fcnt;
    NJ_UINT16 bcnt;

    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_EXT_AREA_SIZE, NJ_ERR_PARAM_ENV_NULL);
    }

    if (rhandle == NULL) {
        /* 第2引数(handle)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_EXT_AREA_SIZE, NJ_ERR_PARAM_DIC_NULL);
    }

    if (size == NULL) {
        /* 第3引数(size)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_EXT_AREA_SIZE, NJ_ERR_PARAM_SIZE_NULL);
    }

    if (NJ_GET_DIC_TYPE(rhandle) != NJ_DIC_TYPE_RULE) {
        /* ルール辞書以外の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_GET_EXT_AREA_SIZE, NJ_ERR_NO_RULEDIC);
    }

    *size = 0;
    ret_size = 0;

    /* 拡張情報データ識別子*/
    ret_size += NJG_HRL_EXT_ID_LEN;

    /* 拡張情報データサイズ */
    ret_size += sizeof(NJ_UINT32);

    /* インデックス領域オフセット */
    ret_size += sizeof(NJ_UINT32);

    /* インデックス領域サイズ */
    ret_size += sizeof(NJ_UINT32);

    /* インデックス領域後品詞登録数 */
    ret_size += sizeof(NJ_UINT32);

    /* UNDO管理領域オフセット */
    ret_size += sizeof(NJ_UINT32);

    /* UNDO管理領域サイズ */
    ret_size += sizeof(NJ_UINT32);

    /* 学習辞書データオフセット */
    ret_size += sizeof(NJ_UINT32);

    /* 学習辞書データサイズ */
    ret_size += sizeof(NJ_UINT32);

    /* （空き）リザーブ */
    ret_size += sizeof(NJ_UINT32);

    /*
     * 登録されている前品詞数、後品詞数を取得
     */
    njd_r_get_count(rhandle, &fcnt, &bcnt);

    /* 属性インデックス領域サイズ (+1は次回書込位置分) */
    ret_size += NJG_HRL_ATTR_CNT * ((NJG_HRL_INDEX_CNT + 1) * NJ_INDEX_SIZE);

    /* 後品詞インデックス領域サイズ (+1は次回書込位置分) */
    ret_size += bcnt * ((NJG_HRL_INDEX_CNT + 1) * NJ_INDEX_SIZE);

    /* UNDO管理領域サイズ (+1は次回書込位置分) */
    ret_size += (NJG_HRL_UNDO_MAX + 1) * NJ_INDEX_SIZE;

    /* 学習領域サイズ */
    ret_size += NJG_HRL_LEARN_DATA_SIZE;

    /* 拡張情報データ識別子*/
    ret_size += NJG_HRL_EXT_ID_LEN;

    *size = ret_size;

    return 0;
}

/**
 * 品詞繋がり学習擬似辞書拡張情報データ初期化API
 *
 * @param[in]  iwnn      解析情報クラス
 * @param[in]  rhandle   ルール辞書ハンドル
 * @param[out] ext_area  拡張情報のアドレス
 * @param[in]  size      拡張情報データサイズ
 *
 * @retval 0  正常終了
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njex_hrl_init_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE rhandle, NJ_VOID *ext_area, NJ_UINT32 size) {
    NJ_INT16  ret;
    NJ_UINT32 dic_size;
    NJ_UINT8 *ext_tmp;
    NJ_UINT32 index_size;
    NJ_UINT32 undo_size;
    NJ_UINT16 fcnt;
    NJ_UINT16 bcnt;
    NJ_UINT32 bcnt32;
    NJ_UINT32 i;

    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_INIT_EXT_AREA, NJ_ERR_PARAM_ENV_NULL);
    }

    if (rhandle == NULL) {
        /* 第2引数(handle)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_INIT_EXT_AREA, NJ_ERR_PARAM_DIC_NULL);
    }

    if (ext_area == NULL) {
        /* 第3引数(ext_area)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_INIT_EXT_AREA, NJ_ERR_PARAM_EXT_AREA_NULL);
    }

    if (size == 0) {
        /* 第4引数(size)が0の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_INIT_EXT_AREA, NJ_ERR_PARAM_EXT_AREA_INVALID_SIZE);
    }

    if (NJ_GET_DIC_TYPE(rhandle) != NJ_DIC_TYPE_RULE) {
        /* ルール辞書以外の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_INIT_EXT_AREA, NJ_ERR_NO_RULEDIC);
    }

    njex_hrl_get_ext_area_size(iwnn, rhandle, &dic_size);
    if (size < dic_size) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_INIT_EXT_AREA, NJ_ERR_BUFFER_NOT_ENOUGH);
    }

    /*
     * 拡張情報データの初期化
     */
    ext_tmp = (NJ_UINT8*)ext_area;

    /* 拡張情報データ識別子*/
    NJ_INT32_WRITE(ext_tmp, NJG_HRL_EXT_IDENTIFIER);
    ext_tmp += NJG_HRL_EXT_ID_LEN;

    /* 拡張情報データサイズ */
    NJ_INT32_WRITE(ext_tmp, dic_size);
    ext_tmp += sizeof(NJ_UINT32);

    /* インデックス領域オフセット */
    NJ_INT32_WRITE(ext_tmp, NJG_HRL_EXT_HEAD_SIZE);
    ext_tmp += sizeof(NJ_UINT32);

    /*
     * 登録されている前品詞数、後品詞数を取得
     */
    njd_r_get_count(rhandle, &fcnt, &bcnt);

    index_size = 0;

    /* 属性インデックス領域サイズ (+1は次回書込位置分) */
    index_size += NJG_HRL_ATTR_CNT * ((NJG_HRL_INDEX_CNT + 1) * NJ_INDEX_SIZE);

    /* 後品詞インデックス領域サイズ (+1は次回書込位置分) */
    index_size += bcnt * ((NJG_HRL_INDEX_CNT + 1) * NJ_INDEX_SIZE);

    /* インデックス領域サイズ */
    NJ_INT32_WRITE(ext_tmp, index_size);
    ext_tmp += sizeof(NJ_UINT32);

    /* インデックス領域後品詞登録数 */
    bcnt32 = (NJ_UINT32)bcnt;
    NJ_INT32_WRITE(ext_tmp, bcnt32);
    ext_tmp += sizeof(NJ_UINT32);

    /* UNDO管理領域オフセット */
    NJ_INT32_WRITE(ext_tmp, (NJ_UINT32)(NJG_HRL_EXT_HEAD_SIZE + index_size));
    ext_tmp += sizeof(NJ_UINT32);

    /* UNDO管理領域サイズ (+1は次回書込位置分) */
    undo_size = (NJG_HRL_UNDO_MAX + 1) * NJ_INDEX_SIZE;
    NJ_INT32_WRITE(ext_tmp, undo_size);
    ext_tmp += sizeof(NJ_UINT32);

    /* 学習辞書データオフセット */
    NJ_INT32_WRITE(ext_tmp, (NJ_UINT32)(NJG_HRL_EXT_HEAD_SIZE + index_size + undo_size));
    ext_tmp += sizeof(NJ_UINT32);

    /* 学習辞書データサイズ */
    NJ_INT32_WRITE(ext_tmp, NJG_HRL_LEARN_DATA_SIZE);
    ext_tmp += sizeof(NJ_UINT32);

    /* 空き（リザーブ） */
    NJ_INT32_WRITE(ext_tmp, 0xFFFFFFFF);
    ext_tmp += sizeof(NJ_UINT32);

    /* インデックス領域をクリア */
    for (i = 0; i < index_size; i++) {
        *ext_tmp++ = 0x00;
    }

    /* UNDO管理領域をクリア */
    for (i = 0; i < undo_size; i++) {
        *ext_tmp++ = 0x00;
    }

    /* 学習辞書データを初期化 */
    ret = njx_create_dic(iwnn, ext_tmp, 2, NJG_HRL_LEARN_DATA_SIZE);
    if (ret < 0) {
        return ret; /*NCH*/
    }
    ext_tmp += NJG_HRL_LEARN_DATA_SIZE;

    /* 拡張情報データ識別子*/
    NJ_INT32_WRITE(ext_tmp, NJG_HRL_EXT_IDENTIFIER);
    ext_tmp += NJG_HRL_EXT_ID_LEN;

    return 0;
}

/**
 * 品詞繋がり学習擬似辞書拡張情報データチェックAPI
 *
 * @param[in] iwnn      解析情報クラス
 * @param[in] rhandle   ルール辞書ハンドル
 * @param[in] ext_area  拡張情報のアドレス
 * @param[in] size      拡張情報データサイズ
 *
 * @retval 0 チェックOK
 * @retval <0 エラー
 */
NJ_EXTERN NJ_INT16 njex_hrl_check_ext_area(NJ_CLASS *iwnn, NJ_DIC_HANDLE rhandle, NJ_VOID *ext_area, NJ_UINT32 size) {
    NJ_INT16  ret;
    NJ_UINT32 ext_size;
    NJ_UINT8 *ext_tmp;
    NJ_UINT32 data_size;
    NJ_UINT32 index_addr;
    NJ_UINT32 index_size;
    NJ_UINT32 learn_addr;
    NJ_UINT32 learn_size;
    NJ_UINT32 index_size_tmp;
    NJ_UINT32 undo_addr;
    NJ_UINT32 undo_size;
    NJ_UINT32 i;
    NJ_UINT16 fcnt;
    NJ_UINT16 bcnt;
    NJ_UINT16 bcnt_tmp;
    
    if (iwnn == NULL) {
        /* 第1引数(iwnn)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_PARAM_ENV_NULL);
    }

    if (rhandle == NULL) {
        /* 第2引数(handle)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_PARAM_DIC_NULL);
    }

    if (ext_area == NULL) {
        /* 第3引数(ext_area)がNULLの場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_PARAM_EXT_AREA_NULL);
    }

    if (size == 0) {
        /* 第4引数(size)が0の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_PARAM_EXT_AREA_INVALID_SIZE);
    }

    if (NJ_GET_DIC_TYPE(rhandle) != NJ_DIC_TYPE_RULE) {
        /* ルール辞書以外の場合はエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_NO_RULEDIC);
    }

    njex_hrl_get_ext_area_size(iwnn, rhandle, &ext_size);
    if (size < ext_size) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_BUFFER_NOT_ENOUGH);
    }

    /*
     * 拡張情報データのチェック
     */
    ext_tmp = (NJ_UINT8*)ext_area;

    /* 識別子 */
    if (NJ_INT32_READ(ext_tmp) != NJG_HRL_EXT_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_FORMAT_INVALID);
    }
    ext_tmp += sizeof(NJ_UINT32);

    /* 拡張情報データサイズ */
    data_size = NJ_INT32_READ(ext_tmp);
    ext_tmp += sizeof(NJ_UINT32);

    if (ext_size != data_size) {
        /* サイズエラー */
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_PARAM_EXT_AREA_INVALID_SIZE);
    }

    /* インデックス領域オフセットのアドレスを取得 */
    index_addr = NJ_INT32_READ(ext_tmp);
    ext_tmp += sizeof(NJ_UINT32);

    /* インデックス領域サイズを取得 */
    index_size = NJ_INT32_READ(ext_tmp);
    ext_tmp += sizeof(NJ_UINT32);

    /*
     * 登録されている前品詞数、後品詞数を取得
     */
    njd_r_get_count(rhandle, &fcnt, &bcnt);

    index_size_tmp = 0;

    /* 属性インデックス領域サイズ (+1は次回書込位置分) */
    index_size_tmp += NJG_HRL_ATTR_CNT * ((NJG_HRL_INDEX_CNT + 1) * NJ_INDEX_SIZE);

    /* 後品詞インデックス領域サイズ (+1は次回書込位置分) */
    index_size_tmp += bcnt * ((NJG_HRL_INDEX_CNT + 1) * NJ_INDEX_SIZE);

    if (index_size != index_size_tmp) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_FORMAT_INVALID);
    }

    /* インデックス領域後品詞登録数を取得 */
    bcnt_tmp = (NJ_UINT16)NJ_INT32_READ(ext_tmp);
    ext_tmp += sizeof(NJ_UINT32);

    if (bcnt != bcnt_tmp) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_FORMAT_INVALID);
    }

    /* UNDO管理領域オフセットのアドレスを取得 */
    undo_addr = NJ_INT32_READ(ext_tmp);
    ext_tmp += sizeof(NJ_UINT32);

    /* UNDO管理領域サイズを取得 */
    undo_size = NJ_INT32_READ(ext_tmp);
    ext_tmp += sizeof(NJ_UINT32);

    /* 学習辞書データオフセットのアドレスを取得 */
    learn_addr = NJ_INT32_READ(ext_tmp);
    ext_tmp += sizeof(NJ_UINT32);

    /* 学習辞書データサイズを取得 */
    learn_size = NJ_INT32_READ(ext_tmp);
    ext_tmp += sizeof(NJ_UINT32);

    /* UNDO管理領域をクリア */
    ext_tmp = (NJ_UINT8 *)ext_area + undo_addr;
    for (i = 0; i < undo_size; i++) {
        *ext_tmp++ = 0x00;
    }

    /* 学習辞書データをチェック */
    ret = njx_check_dic(iwnn, NJ_DIC_H_TYPE_NORMAL, ((NJ_UINT8 *)ext_area + learn_addr), 0, learn_size);
    if (ret < 0) {
        return ret;
    }

    /* 終端識別子 */
    if (NJ_INT32_READ((NJ_UINT8*)ext_area + data_size - NJG_HRL_EXT_ID_LEN) != NJG_HRL_EXT_IDENTIFIER) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJ_CHECK_EXT_AREA, NJ_ERR_FORMAT_INVALID);
    }

    return 0;
}
