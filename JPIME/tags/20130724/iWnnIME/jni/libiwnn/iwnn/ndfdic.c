/**
 * @file
 *   辞書アダプタ：読み無し辞書アダプタ
 *
 *   読み無し予測辞書へのアクセス関数を提供する
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2011 All Rights Reserved.
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
/*              define  宣  言                  */
/************************************************/
/**
 * １候補データのバイトサイズ
 */
#define DATA_SIZE (10)
#define DATA_OFFSET_FHINSI          (0) /**< 前品詞 [9Bit] */
#define DATA_OFFSET_BHINSI          (1) /**< 後品詞 [9Bit] */
#define DATA_OFFSET_HINDO           (2) /**< 初期頻度 [6Bit] */
#define DATA_OFFSET_CANDIDATE       (3) /**< 表記オフセット[20Bit] */
#define DATA_OFFSET_CANDIDATE_LEN   (5) /**< 表記長 [8Bit] */
#define DATA_OFFSET_YOMI            (6) /**< 読みオフセット[20Bit] */
#define DATA_OFFSET_YOMI_LEN        (9) /**< 読み長 [8Bit] */

#define YOMINASI_DIC_FREQ_DIV 63  /**< 読みなし辞書頻度段階 */

#define DATA_FHINSI(x)                                                  \
    ( (NJ_UINT16)(0x01FF &                                              \
                  (((NJ_UINT16)*((x)+DATA_OFFSET_FHINSI  ) << 1) |      \
                   (           *((x)+DATA_OFFSET_FHINSI+1) >> 7))) )
#define DATA_BHINSI(x)                                                  \
    ( (NJ_UINT16)(0x01FF &                                              \
                  (((NJ_UINT16)*((x)+DATA_OFFSET_BHINSI  ) << 2) |      \
                   (           *((x)+DATA_OFFSET_BHINSI+1) >> 6))) )
#define DATA_HINDO(x)                                                   \
    ((NJ_HINDO)(0x003F & ((NJ_UINT16)*((x)+DATA_OFFSET_HINDO))))
#define DATA_CANDIDATE(x)                                               \
    ((NJ_UINT32)(0x000FFFFF &                                           \
                 (((NJ_UINT32)*((x)+DATA_OFFSET_CANDIDATE)   << 12) |   \
                  ((NJ_UINT32)*((x)+DATA_OFFSET_CANDIDATE+1) <<  4) |   \
                  (           *((x)+DATA_OFFSET_CANDIDATE+2) >>  4))))
#define DATA_CANDIDATE_SIZE(x)                                          \
    ((NJ_UINT8)((*((x)+DATA_OFFSET_CANDIDATE_LEN)   << 4) |             \
                (*((x)+DATA_OFFSET_CANDIDATE_LEN+1) >> 4)))
#define DATA_YOMI(x) \
    ((NJ_UINT32)(0x000FFFFF &                                           \
                 (((NJ_UINT32)*((x)+DATA_OFFSET_YOMI)   << 16) |        \
                  ((NJ_UINT32)*((x)+DATA_OFFSET_YOMI+1) <<  8) |        \
                  (           *((x)+DATA_OFFSET_YOMI+2)      ))))
#define DATA_YOMI_SIZE(x)                       \
    ((NJ_UINT8)((*((x)+DATA_OFFSET_YOMI_LEN))))

#define YOMI_INDX_TOP_ADDR(h) ((NJ_UINT8*)((h)+NJ_INT32_READ((h)+0x1C)))
#define YOMI_INDX_CNT(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x20)))
#define YOMI_INDX_BYTE(h) ((NJ_UINT16)(NJ_INT16_READ((h)+0x22)))
#define STEM_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h)+NJ_INT32_READ((h)+0x24)))
#define STRS_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h)+NJ_INT32_READ((h)+0x28)))
#define YOMI_AREA_TOP_ADDR(h) ((NJ_UINT8*)((h)+NJ_INT32_READ((h)+0x2C)))

/**
 * 無変換フラグ
 */
#define NO_CONV_FLG ((NJ_UINT32) 0x00080000L)

/** NJ_WORD構造体 品詞情報オフセット */
#define HINSI_OFFSET (7)

/**
 * 検索位置  設定情報
 */
#define CURRENT_INFO_SET (NJ_UINT8)(0x10)

/************************************************/
/*              prototype  宣  言               */
/************************************************/
static NJ_UINT16 search_data(NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset);
static NJ_UINT16 convert_to_yomi(NJ_DIC_HANDLE hdl, NJ_UINT8 *index, NJ_UINT16 len, NJ_CHAR *yomi, NJ_UINT16 size);
static NJ_UINT16 yomi_strcmp_forward(NJ_DIC_HANDLE hdl, NJ_UINT8 *data, NJ_CHAR *yomi);

/**
 * 次候補の検索を行う
 *
 * 次候補の情報はオフセットとして検索位置(loctset)に設定する
 *
 * @param[in]     condition   検索条件
 * @param[in,out] loctset     検索位置
 *
 * @retval  0   検索候補なし
 * @retval  1   検索候補あり
 */
static NJ_UINT16 search_data(NJ_SEARCH_CONDITION *condition, NJ_SEARCH_LOCATION_SET *loctset) {
    NJ_UINT32 offset;
    NJ_UINT8 *data;
    NJ_UINT16 i, j;
    NJ_UINT16 hindo;
    NJ_UINT8 hit_flg;
    NJ_UINT8 *tmp_hinsi = NULL;


    offset = loctset->loct.current;
    data = STEM_AREA_TOP_ADDR(loctset->loct.handle) + offset;

    if (GET_LOCATION_STATUS(loctset->loct.status) != NJ_ST_SEARCH_NO_INIT) {
        /*
         * 初回でない（２回目以降の検索）のとき
         */
        data += DATA_SIZE;
        offset += DATA_SIZE;

        /* 表記領域オフセットまで探索したら終了 */
        if (data >= STRS_AREA_TOP_ADDR(loctset->loct.handle)) {
            /* 見つからなかった */
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }
    }

    /* 前方一致検索時、前品詞情報は yominasi_fore を利用。 */
    tmp_hinsi = condition->hinsi.fore;
    condition->hinsi.fore = condition->hinsi.yominasi_fore;
    /* オフセットアドレスからテーブル数を算出 */
    i = (STRS_AREA_TOP_ADDR(loctset->loct.handle) - data) / DATA_SIZE;
    for (j = 0; j < i; j++) {
        /* 前品詞が検索条件と一致するかチェック */
        if (njd_connect_test(condition, DATA_FHINSI(data), DATA_BHINSI(data))) {
            /* 検索候補あり/なしフラグ 初期化 */
            hit_flg = 0;

            if (condition->operation == NJ_CUR_OP_LINK) {
                /* つながり検索の場合は無条件で検索候補あり */
                hit_flg = 1;
            } else {
                /* 前方一致の場合は読み文字列と前方一致するか確認 */

                /* 読み文字列比較 */
                if (yomi_strcmp_forward(loctset->loct.handle, data, condition->yomi)) {
                    /* 検索候補あり */
                    hit_flg = 1;
                }
            }

            if (hit_flg) {
                /* 検索候補あり */
                loctset->loct.current_info = CURRENT_INFO_SET;
                loctset->loct.current = offset;
                loctset->loct.status = NJ_ST_SEARCH_READY;
                hindo = DATA_HINDO(STEM_AREA_TOP_ADDR(loctset->loct.handle) + loctset->loct.current);
                loctset->cache_freq = CALCULATE_HINDO(hindo, loctset->dic_freq.base, 
                                                      loctset->dic_freq.high, YOMINASI_DIC_FREQ_DIV);

                /* 前品詞情報を元に戻す */
                condition->hinsi.fore = tmp_hinsi;
                return 1;
            }
        }
        /* 次候補のアドレス取得 */
        data += DATA_SIZE;
        offset += DATA_SIZE;
    }
    /* 見つからなかった場合 */
    loctset->loct.status = NJ_ST_SEARCH_END;
    /* 前品詞情報を元に戻す */
    condition->hinsi.fore = tmp_hinsi;
    return 0;
}


/**
 * 読みの内部コードを復元する
 *
 * @param[in]   hdl      辞書ハンドル
 * @param[in]   index    変換元内部コード（インデックス）
 * @param[in]   len      変換元文字配列長
 * @param[out]  yomi     読み
 * @param[in]   size     バッファサイズ
 *
 * @return 復元読み文字配列長 (エラー時：ret * sizeof(NJ_CHAR) >= size となる値)
 */
static NJ_UINT16 convert_to_yomi(NJ_DIC_HANDLE hdl, NJ_UINT8 *index, NJ_UINT16 len, NJ_CHAR *yomi, NJ_UINT16 size) {
    NJ_UINT8  *wkc;
    NJ_CHAR   *wky;
    NJ_UINT16 i, idx, yib, ret;
    NJ_UINT16 j, char_len;


    /* 読み変換用テーブルを取得する */
    wkc = YOMI_INDX_TOP_ADDR(hdl);

    /* 読み変換用テーブル  読みコード格納バイト数 */
    yib = YOMI_INDX_BYTE(hdl);

    /* 文字コード的に使用可能なバイト数か確認する */
    if (NJ_CHAR_ILLEGAL_DIC_YINDEX(yib)) {
        /* 不正な場合、読み文字変換を行わず、変換結果を 0 文字として終了する。*/
        return 0; /*NCH_DEF*/
    }

    /* 変換元の長さ分、復元する */
    ret = 0;
    wky = yomi;
    for (i = 0; i < len; i++) {
        idx = (NJ_UINT16)((*index - 1) * yib);  /* 読み領域のオフセットサイズ算出 */
        if (yib == 2) {         /* 読み変換用テーブル  格納バイト数:2byte */
            char_len = UTL_CHAR(wkc + idx);
            /* ターミネートを含み、サイズオーバーの場合、サイズ最大値を返しエラーとする */
            if (((ret + char_len + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) {
                return (size / sizeof(NJ_CHAR));
            }
            for (j = 0; j < char_len; j++) {
                NJ_CHAR_COPY(wky, wkc + idx + j);
                wky++;
                ret++;
            }
        } else {                /* 読み変換用テーブル  格納バイト数:1byte (SJIS only) */
            /* ターミネートを含み、サイズオーバーの場合、サイズ最大値を返しエラーとする */
            if (((ret + 1 + NJ_TERM_LEN) * sizeof(NJ_CHAR)) > size) { /*NCH_FB*/
                return (size / sizeof(NJ_CHAR)); /*NCH_FB*/
            }
            *wky++ = (NJ_CHAR)(*(wkc + idx));  /*NCH_FB*/
            ret++; /*NCH_FB*/
        }
        index++;
    }
    *wky = NJ_CHAR_NUL;
    return ret;
}

/**
 * 該当候補データの読みと指定した読み文字列(yomi)が前方一致するか比較
 *
 * @param[in]  hdl     辞書ハンドル
 * @param[in]  data    該当候補データのアドレス
 * @param[in]  yomi    読み文字列
 *
 * @retval  0  前方一致しない
 * @retval  1  前方一致する
 */
static NJ_UINT16 yomi_strcmp_forward(NJ_DIC_HANDLE hdl, NJ_UINT8 *data, NJ_CHAR *yomi) {
    NJ_UINT8 *area;
    NJ_CHAR  *stroke;
    NJ_CHAR   buf[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_UINT16 ylen, dic_ylen, j, size;


    /* 該当候補 読み文字列格納バッファ */
    size = sizeof(buf);
    stroke = buf;

    /* 該当候補 読み文字列格納位置を取得 */
    area = YOMI_AREA_TOP_ADDR(hdl) + DATA_YOMI(data);

    if (YOMI_INDX_CNT(hdl) == 0) {      /* 読み格納数が0の時はIndex化されていない */
        /* Index 化されていないときは、読み長＝読み文字列の byte 長なので、文字配列数に変換する */
        dic_ylen = DATA_YOMI_SIZE(data) / sizeof(NJ_CHAR);

        /* サイズチェック */
        if (size < ((dic_ylen + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
            /* 読み文字列格納バッファ(NJ_MAX_LEN)を越える場合は、
             * 前方一致なし */
            return 0;
        }
        for (j = 0; j < dic_ylen; j++) {
            NJ_CHAR_COPY(stroke, area); /* そのままのコードを設定 */
            stroke++;
            area += sizeof(NJ_CHAR);
        }
        *stroke = NJ_CHAR_NUL;
    } else {                            /* Indexを読み文字列に変換 */
        /* Index 化されているときは、Index は１文字1byte なので、読み長＝読み文字列の文字配列数となる */
        dic_ylen = convert_to_yomi(hdl, area, DATA_YOMI_SIZE(data), stroke, size);

        /* サイズチェック */
        if (size < ((dic_ylen + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
            /* 読み文字列格納バッファ(NJ_MAX_LEN)を越える場合は、
             * 前方一致なし */
            return 0;
        }
    }

    /* 検索読み文字列長取得 */
    ylen = nj_strlen(yomi);

    /* 該当候補の読みと検索読みの文字列長を比較 */
    if (dic_ylen < ylen) {
        /* 該当候補の読み文字列長の方が小さい場合、前方一致なし */
        return 0;
    }

    /* 該当候補の読みと検索読みを比較 */
    if (nj_strncmp(yomi, buf, ylen) == 0) {
        /* 前方一致あり */
        return 1;
    }
    return 0;
}

/************************************************/
/* Global関数                                   */
/************************************************/
/**
 * 読み無し辞書アダプタ  単語検索
 *
 * @param[in]      con      検索条件
 * @param[in,out]  loctset  検索位置
 *
 * @retval 1  検索候補あり
 * @retval 0  検索候補なし
 * @retval <0 エラー
 */
NJ_INT16 njd_f_search_word(NJ_SEARCH_CONDITION *con, NJ_SEARCH_LOCATION_SET *loctset) {
    NJ_UINT16 ret;


    switch (con->operation) {
    case NJ_CUR_OP_LINK:
        /* つながり */
        /* 読み無し前品詞がない場合、候補なしを返す */
        if ((con->hinsi.yominasi_fore == NULL) ||
            (con->hinsi.foreSize == 0)) {
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }
        break;
    case NJ_CUR_OP_FORE:
        /* 前方一致 */
        /* 読みが空文字列の場合、候補なしを返す */
        if (NJ_CHAR_STRLEN_IS_0(con->yomi)) {
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }

        /* 読み無し前品詞がない場合、候補なしを返す */
        if ((con->hinsi.yominasi_fore == NULL) ||
            (con->hinsi.foreSize == 0)) {
            loctset->loct.status = NJ_ST_SEARCH_END;
            return 0;
        }
        break;
    default:
        /* つながり、前方一致以外は、候補なしを返す */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    } 


    /* 検索候補取得順：頻度順(0)のみ有効 */
    if (con->mode != NJ_CUR_MODE_FREQ) {
        /* 対応しない検索操作が指定された場合、該当候補なしとする */
        loctset->loct.status = NJ_ST_SEARCH_END;
        return 0;
    }

    /* カーソルの初期化は上位で行っている*/
    if ((GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_NO_INIT)
        || (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_READY)) {
        /* 候補データを検索する（頻度値も取得して設定する） */
        ret = search_data(con, loctset);
        if (ret < 1) {
            /* 該当候補データが無い為、検索終了の属性をカーソルに設定する */
            loctset->loct.status = NJ_ST_SEARCH_END;
        }
        return ret;
    } else {
        /* NJ_ST_SEARCH_END     */
        loctset->loct.status = NJ_ST_SEARCH_END; /*NCH_FB*/
        return 0; /*NCH_FB*/
    }
}


/**
 * 読み無し辞書アダプタ  単語取得
 *
 * @param[in]   loctset   検索位置
 * @param[out]  word      単語情報
 *
 * @retval 1 候補あり
 * @retval 0 候補なし
 * @retval <0 エラー
 */
NJ_INT16 njd_f_get_word(NJ_SEARCH_LOCATION_SET *loctset, NJ_WORD *word) {
    NJ_UINT8 *data;
    NJ_CHAR  stroke[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_INT16 yomilen, kouholen;


    /* 検索状態：次候補なし  は、そのままリターン */
    if (GET_LOCATION_STATUS(loctset->loct.status) == NJ_ST_SEARCH_END) {
        return 0; /*NCH_FB*/
    }

    /*
     * 検索位置（loc）のloc->currentから検索結果を返す。
     */
    /* 該当候補データのアドレスを取得する */
    data = STEM_AREA_TOP_ADDR(loctset->loct.handle) + loctset->loct.current;

    NJ_SET_YLEN_TO_STEM(word, 1);

    /* 該当候補データの情報を設定する */
    word->stem.loc = loctset->loct;                                     /* 検索位置 */
    yomilen = njd_f_get_stroke(word, stroke, sizeof(stroke));
    if (yomilen <= 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_F_GET_WORD, NJ_ERR_INVALID_RESULT); /*NCH*/
    }
    word->stem.info1 = yomilen;
    word->stem.info1 |= (NJ_UINT16)(DATA_FHINSI(data) << HINSI_OFFSET); /* 前品詞 */
    word->stem.info2 = (NJ_UINT16)(DATA_BHINSI(data) << HINSI_OFFSET);  /* 後品詞 */
    kouholen = (NJ_UINT16)DATA_CANDIDATE_SIZE(data)/sizeof(NJ_CHAR);
    if (kouholen == 0) {
        /* 無変換       */
        kouholen = yomilen;
    }
    word->stem.info2 |= kouholen;                                       /* 候補長 */
    word->stem.hindo = CALCULATE_HINDO(DATA_HINDO(data), loctset->dic_freq.base, 
                                       loctset->dic_freq.high, YOMINASI_DIC_FREQ_DIV); /* 頻度値 */

    /* 擬似候補の種類クリア     */
    word->stem.type = 0;

    return 1;
}


/**
 * 読み無し辞書アダプタ  読み文字列取得
 *
 * @param[in]     word      単語情報(NJ_RESULT->wordを指定)
 * @param[out]    stroke    候補文字列格納バッファ
 * @param[in]     size      候補文字列格納バッファサイズ(byte)
 *
 * @retval >0 取得文字配列長(ヌル文字含まず)
 * @retval <0 エラー
 */
NJ_INT16 njd_f_get_stroke(NJ_WORD *word, NJ_CHAR *stroke, NJ_UINT16 size) {
    NJ_SEARCH_LOCATION *loc;
    NJ_UINT8 *area, *data;
    NJ_UINT16 len;
    NJ_UINT32 j;

    if (NJ_GET_YLEN_FROM_STEM(word) == 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_F_GET_STROKE, NJ_ERR_INVALID_RESULT); /*NCH*/
    }

    /*
     * 単語情報内の検索語位置（word->loc.current）から読み文字列を返す。
     */

    /* 該当候補データのアドレスを取得する */
    loc = &word->stem.loc;
    data = STEM_AREA_TOP_ADDR(loc->handle) + loc->current;

    /* 指定候補読み文字列格納位置を取得する */
    area = YOMI_AREA_TOP_ADDR(loc->handle) + DATA_YOMI(data);

    if (YOMI_INDX_CNT(loc->handle) == 0) {      /* 読み格納数が0の時はIndex化されていない */
        /* Index 化されていないときは、読み長＝読み文字列の byte 長なので、文字配列数に変換する */
        len = DATA_YOMI_SIZE(data)/sizeof(NJ_CHAR);

        /* サイズチェック */
        if (size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_F_GET_STROKE, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH_FB*/
        }

        for (j = 0; j < len; j++) {
            NJ_CHAR_COPY(stroke, area); /* そのままのコードを設定 */
            stroke++;
            area += sizeof(NJ_CHAR);
        }
        *stroke = NJ_CHAR_NUL;
    } else {                                    /* Indexを読み文字列に変換 */
        /* Index 化されているときは、Index は１文字1byte なので、読み長＝読み文字列の文字配列数となる */
        len = convert_to_yomi(loc->handle, area, DATA_YOMI_SIZE(data), stroke, size);

        /* サイズチェック */
        if (size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_F_GET_STROKE, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH_FB*/
        }
    }
    return len;
}


/**
 * 読み無し辞書アダプタ  候補文字列取得
 *
 * @param[in]   word      単語情報(NJ_RESULT->wordを指定)
 * @param[out]  candidate 候補文字列格納バッファ
 * @param[in]   size      候補文字列格納バッファサイズ(byte)
 *
 * @retval >0 取得文字列配列長(ヌル文字含まず)
 * @retval <0 エラー
 */
NJ_INT16 njd_f_get_candidate(NJ_WORD *word, NJ_CHAR *candidate, NJ_UINT16 size) {
    NJ_SEARCH_LOCATION *loc;
    NJ_UINT8 *data, *area;
    NJ_CHAR   work[NJ_MAX_LEN + NJ_TERM_LEN];
    NJ_UINT16 len, j;



    /* 該当候補データのアドレスを取得する */
    loc = &word->stem.loc;
    data = STEM_AREA_TOP_ADDR(loc->handle) + loc->current;

    /* 表記文字列を取得する */
    len = DATA_CANDIDATE_SIZE(data)/sizeof(NJ_CHAR);
    if (size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_F_GET_CANDIDATE, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH_FB*/
    }

    /* 表記長=0 は無変換候補 */
    if (len == 0) {     /* 無変換候補 */
        /* 指定候補読み文字列格納位置を取得する */
        area = YOMI_AREA_TOP_ADDR(loc->handle) + DATA_YOMI(data);
        if (YOMI_INDX_CNT(loc->handle) == 0) {  /* 読み格納数が0の時はIndex化されていない */
            /* Index 化されていないときは、読み長＝読み文字列の byte 長なので、文字配列数に変換する */
            len = DATA_YOMI_SIZE(data)/sizeof(NJ_CHAR);

            /* サイズチェック */
            if (size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_F_GET_STROKE, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH_FB*/
            }
            for (j = 0; j < len; j++) {
                NJ_CHAR_COPY(candidate + j, area);   /* そのままのコードを設定 */
                area += sizeof(NJ_CHAR);
            }
            candidate[len] = NJ_CHAR_NUL;
            return len;
        } else {                                        /* Indexを読み文字列に変換 */
            /* Index 化されているときは、Index は１文字1byte なので、読み長＝読み文字列の文字配列数となる */
            len = convert_to_yomi(loc->handle, area, DATA_YOMI_SIZE(data), work, size);

            /* サイズチェック */
            if (size < ((len + NJ_TERM_LEN) * sizeof(NJ_CHAR))) {
                return NJ_SET_ERR_VAL(NJ_FUNC_NJD_F_GET_CANDIDATE, NJ_ERR_BUFFER_NOT_ENOUGH); /*NCH_FB*/
            }
        }

        if (DATA_CANDIDATE(data) & NO_CONV_FLG) {       /* カタカナ設定 */
            nje_convert_hira_to_kata(work, candidate, len);
        } else {                                        /* ひらがな設定 */
            for (j = 0; j < len; j++) {
                candidate[j] = work[j];
            }
        }
    } else {            /* 表記あり */
        /* 指定候補表記文字列格納位置を取得する */
        area = STRS_AREA_TOP_ADDR(loc->handle) + DATA_CANDIDATE(data);
        for (j = 0; j < len; j++) {
            NJ_CHAR_COPY(candidate + j, area);
            area += sizeof(NJ_CHAR);
        }
    }

    candidate[len] = NJ_CHAR_NUL;
    return len;
}
