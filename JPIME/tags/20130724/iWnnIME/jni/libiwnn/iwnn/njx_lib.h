/**
 * @file
 *   マルチスレッド版変換エンジン定義定義（共通編）
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2012 All Rights Reserved.
 */
#ifndef _NJX_LIB_H_
#define _NJX_LIB_H_

/************************************************/
/*              define  宣  言                  */
/************************************************/
/** 形態素解析結果読み取得時の最大候補数 */
#define NJ_MAX_YOMI_STRING 50

#define NJD_MAX_CONNECT_CNT     6

/************************************************/
/*           インクルードファイル               */
/************************************************/
#include "njc.h"

/**********************************************/
/* 学習辞書系構造体宣言                       */
/**********************************************/
/**
 * 登録単語情報
 */
typedef struct {
    NJ_UINT16 f_hinsi;                                  /**< 単語の前品詞番号 */
    NJ_UINT16 b_hinsi;                                  /**< 単語の後品詞番号 */
    NJ_UINT8  yomi_len;                                 /**< 単語の読み文字配列要素数 */
    NJ_UINT8  hyouki_len;                               /**< 単語の候補文字配列要素数 */
    NJ_UINT8  additional_len;                           /**< 単語の付加情報文字配列要素数 */
    NJ_CHAR   yomi[NJ_MAX_LEN +NJ_TERM_LEN];            /**< 単語の読み文字列 */
    NJ_CHAR   hyouki[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];  /**< 単語の候補文字列 */
    NJ_CHAR   additional[NJ_MAX_ADDITIONAL_LEN + NJ_TERM_LEN];  /**< 単語の付加情報文字列 */
    NJ_UINT32 attr;                                     /**< 単語の状況属性フラグ */
    NJ_UINT16 stem_b_hinsi;                             /**< 単語の自立語部分の後品詞番号 */
    NJ_UINT8  fzk_yomi_len;                             /**< 単語の付属語部分の読み文字列配列要素数 */
} NJ_LEARN_WORD_INFO;


/**
 * 学習辞書キュー構造
 */
typedef struct word_que {
    NJ_UINT16  entry;           /**< キューエントリ番号 */
    NJ_UINT8   type;            /**< キューのタイプ。QUE_TYPE_xxxx*/
    NJ_UINT16  mae_hinsi;       /**< 前品詞 */
    NJ_UINT16  ato_hinsi;       /**< 後品詞 */
    NJ_UINT8   yomi_len;        /**< 読みの文字配列要素数 */
    NJ_UINT8   hyouki_len;      /**< 表記の文字配列要素数 */
    NJ_UINT8   yomi_byte;       /**< 読みのバイト数 */
    NJ_UINT8   hyouki_byte;     /**< 表記のバイト数 */
    NJ_UINT8   next_flag;       /* 前のキューからの続きかどうか */
} NJ_WQUE;

/**********************************************/
/* 同表記処理系構造体宣言                     */
/**********************************************/
/**
 * 同表記削除用バッファ管理(全候補取得用)
 */
typedef struct {
    /** ワーク領域 */
    NJ_CHAR work[(NJ_MAX_RESULT_LEN + NJ_TERM_LEN) * (NJ_MAX_CANDIDATE + 1)];
    /** 現在位置 */
    NJ_INT16 current;
} NJ_WORK_BUF;

/**
 * 同よみ削除用バッファ管理(読み取得用)
 */
typedef struct {
    /** ワーク領域 */
    NJ_CHAR work[(NJ_MAX_LEN + NJ_TERM_LEN) * (NJ_MAX_YOMI_STRING + 1)];
    /** 現在位置 */
    NJ_INT16 current;
} NJ_YOMI_BUF;

/**********************************************/
/* 解析処理系定数・構造体宣言                 */
/**********************************************/
/**
 * 解析処理ステータス
 */
/* 予測系 */
#define NJ_ANALYZE_INITIAL                      0x0000 /**< 初期設定状態                   */
#define NJ_ANALYZE_AI_YOSOKU                    0x0001 /**< AI予測                         */
#define NJ_ANALYZE_NEXT_YOSOKU                  0x0002 /**< 読みなし予測(繋がり予測)       */
#define NJ_ANALYZE_FORWARD_SEARCH               0x0003 /**< 前方一致解析                   */
#define NJ_ANALYZE_FORWARD_SEARCH_WITH_YOMINASI 0x0004 /**< 前方一致解析(読み無し予測含む) */
/* 変換系 */
#define NJ_ANALYZE_CONVERSION_MULTIPLE          0x0005 /**< 連文節解析                     */
#define NJ_ANALYZE_CONVERSION_SINGLE            0x0006 /**< 1文節解析                      */
#define NJ_ANALYZE_COMPLETE                     0x0007 /**< 全候補解析                     */
#define NJ_ANALYZE_COMPLETE_HEAD                0x0008 /**< 先頭文節全候補解析             */
#define NJ_ANALYZE_COMPLETE_GIJI                0x0009 /**< 全候補解析(擬似候補)           */
#define NJ_ANALYZE_END                          0x000A /**< 解析終了                       */

/**
 * 変換用バッファ
 */
typedef struct {
    NJ_RESULT   single_keep[2];               /**< 1文節変換結果保持用バッファ */
    NJ_UINT16   single_keep_len;              /**< 1文節変換結果保持長 */
    NJ_RESULT   multiple_keep[NJ_MAX_PHRASE]; /**< 連文節変換結果保持用バッファ */
    NJ_UINT16   multiple_keep_len;            /**< 連文節変換結果保持長 */
} NJ_ANALYZE_CONV_BUF;

/**
 * 予測変換環境設定
 */
typedef struct {
    /* 解析環境 */
    NJ_DIC_SET  *dics;                  /**< アプリから指定された辞書セット */
    NJ_DIC_SET  search_dics;            /**< 検索用のテンポラリ辞書セット */
    NJ_UINT8    search_op;              /**< 辞書引き時の検索条件 */
    NJ_CURSOR   cursor;                 /**< 辞書引き時の検索カーソル */
    NJ_CHAR     *yomi;                  /**< アプリから指定された読み */
    NJ_UINT16   prev_hinsi;             /**< 前確定候補の後品詞 */
    /* 現状の解析状態 */
    NJ_UINT16   type;                   /**< 解析種別 */
    NJ_INT16    level;                  /**< 解析回数(解析種別が変わるごと 0 になる) */
    NJ_UINT16   no;                     /**< 解析結果の候補番号 */
    NJ_ANALYZE_OPTION option;           /**< 予測オプション */
    /* 全候補検索用バッファ */
    NJ_ANALYZE_CONV_BUF conv_buf;       /**< 変換バッファ */
    NJ_UINT16   yomi_len;               /**< 読み長 */
    NJ_UINT16   learn_cnt;              /**< 学習辞書からの候補数 */
    NJ_INT16    previous_selection_cnt; /**< 検索対象の前確定情報件数 */
    NJ_UINT16   divipos;                /**< 逐次単文節変換：文節区切り位置 */
} NJ_ANALYZE_ENV;


/**
 * 前確定情報構造体
 */
typedef struct {
    NJ_LEARN_WORD_INFO  selection_data[NJ_MAX_RELATION_SEGMENT];    /**< 保持する前確定情報データ */
    NJ_LEARN_WORD_INFO  next_prediction_lword;                      /**< 作業用前確定情報データ */
    NJ_UINT8            selection_now;                              /**< 直前の前確定情報の位置 */
    NJ_UINT8            count;                                      /**< 保持している前確定情報の件数 */
} NJ_PREVIOUS_SELECTION_INFO;

/**
 * 複合語予測結果保持情報
 */
typedef struct {
    NJ_RESULT result;                        /**< 複合語スタック領域 */
    NJ_UINT8  yomi_toplen;                   /**< 複合語第１語句読み文字列長 */
    NJ_UINT8  yomi_len;                      /**< 複合語読み文字列長 */
    NJ_UINT8  index;                         /**< 複合語のバッファのindex */
#define NJ_CMPDG_BLANK_INDEX          0x00   /**< NJ_CMPDG_RESULT::index 空バッファ */
    NJ_UINT8  status;                        /**< 複合語のバッファステータス */
#define NJ_CMPDG_STATUS_ADD           0x00   /**< NJ_CMPDG_RESULT::status 追加領域 */
#define NJ_CMPDG_STATUS_REFER         0x01   /**< NJ_CMPDG_RESULT::status 参照領域 */
} NJ_CMPDG_RESULT;

/**
 * 複合語予測情報
 */
typedef struct {
    NJ_CMPDG_RESULT cmpdg_data[NJ_MAX_CMPDG_RESULTS];     /**< 複合語予測結果保持情報 */
    NJ_UINT32       refer_count;                          /**< 参照複合語のスタック数 */
    NJ_UINT32       add_count;                            /**< 追加複合語のスタック数 */
} NJ_CMPDG_INFO;

/**
 *  iWnn解析情報クラス
 */
typedef struct {
    /**
     *  頻度学習カウンタ
     */
    NJ_UINT8 iwnn_now;
    
    /**********************************************/
    /*  mmapi.c                                   */
    /**********************************************/
    /**
     *  読み取得時のフラグ
     *
     * - 0   : 未取得
     * - 1〜 : 取得回数
     * - nj_get_wordが正常終了すれば、1になる
     */
    NJ_UINT8 mm_yomi_search_index;

    /** mm_get_yomi API 保持付属語文字配列長 */
    NJ_UINT8 mm_yomi_fzk_len;

    /**********************************************/
    /*  ncconv.c                                  */
    /**********************************************/
    /**
     *  変換モード
     *
     * - 0:初期状態
     * - 1:変換中
     * - 2:全候補中
     * -3:形態素解析中
     */
    NJ_UINT8    njc_mode;

    /** 連文節変換処理結果保持領域 */
    NJC_SENTENCE        m_ConvResult;

    /** 変換処理ワーク領域 */
    union {
        NJC_CONV        Conv;           /**< 連文節変換処理ワーク */
        NJC_CANDIDATE   Cand;           /**< 全候補処理結果保持領域 */
    } m_Buf;

    NJC_CANDIDATE_WORK m_cwork;                     /**< 自立語解析用ワーク領域 */
    NJ_CURSOR m_ccur[NJC_MAX_STEM_CANDIDATE];       /**< 辞書検索カーソル */

    /**********************************************/
    /*  ndldic.c                                  */
    /**********************************************/
    /** テンポラリqueue */
    NJ_WQUE que_tmp;

    /**
     * 複合語予測結果保持情報
     *
     */
    NJ_CMPDG_INFO cmpdg_info;

    /**********************************************/
    /*  ndtdic.c                                  */
    /**********************************************/
    /** 学習文字列バッファテンポラリ */
    NJ_CHAR learn_string_tmp[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];
    /** 無変換文字列バッファテンポラリ */
    NJ_CHAR muhenkan_tmp[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];

    /**********************************************/
    /*  neapi.c                                   */
    /**********************************************/
    /** 前確定情報 */
    NJ_PREVIOUS_SELECTION_INFO previous_selection;

    NJ_UINT32 prev_search_range[NJ_MAX_DIC][2];   /**< 繋がりインデックス検索範囲 */
#define NJ_SEARCH_RANGE_TOP     0                 /**< prev_search_range: 検索範囲開始位置 */
#define NJ_SEARCH_RANGE_BOTTOM  1                 /**< prev_search_range: 検索範囲終了位置 */

    /**
     * 繋がり予測結果保持情報
     *
     */
    NJ_RESULT relation_results[NJ_MAX_RELATION_RESULTS];
    NJ_UINT32 relation_results_count;

    /**********************************************/
    /*  nehomo.c                                  */
    /**********************************************/
    /** 重複候補削除用バッファ */
    union {
        NJ_WORK_BUF workbuf;  /** 表記チェック用 */
        NJ_YOMI_BUF yomibuf;  /** 読みチェック用 */
    } h_buf;

    /**********************************************/
    /*  nfapi.c                                   */
    /**********************************************/
    /** 解析環境設定 */
    NJ_ANALYZE_ENV environment;

    /**
     * 絞込検索対象候補確認フラグ
     *
     * - 0 : 非絞込検索対象候補
     * - 1 : 絞込検索対象候補
     */
    NJ_UINT8 relation_cand_flg;

    /**
     * 各辞書用絞込検索対象候補確認フラグ
     *
     * - 0 : 非絞込検索対象候補
     * - 1 : 絞込検索対象候補
     */
    NJ_UINT8 wk_relation_cand_flg[NJ_MAX_DIC];

    /** オプション設定 */
    NJ_OPTION option_data;

    /**********************************************/
    /*  nj_lib.h                                  */
    /**********************************************/
    NJ_DIC_SET dic_set;         /**< 辞書セット構造体 */
    NJ_DIC_SET tmp_dic_set;     /**< テンポラリ用辞書セット */

    NJ_STATE   state;           /**< 状況適応予測用状況設定 */

    struct {
        /**
         * 学習辞書操作中状態フラグ
         * - 0 : 非操作中
         * - 1 : 操作中
         */
        NJ_UINT8   commit_status;
        /**
         * 学習辞書操作中状態 -登録範囲先頭-
         */
        NJ_UINT16  save_top;
        /**
         * 学習辞書操作中状態 -登録範囲末尾-
         */
        NJ_UINT16  save_bottom;
        /**
         * 学習辞書操作中状態 -登録件数-
         */
        NJ_UINT16  save_count;
    } learndic_status;

    /** 汎用ワーク領域 */
    NJ_CHAR tmp[NJ_MAX_RESULT_LEN + NJ_TERM_LEN];


} NJ_CLASS;

#define NJ_SET_ENV(x,y) ((x).dic_set = (y))


#define NJ_PUT_DIC_KEYWORD(x,y) nj_strcpy(&(x).keyword[0], &(y).dic_set.keyword[0])

#endif /* _NJX_LIB_H_ */
