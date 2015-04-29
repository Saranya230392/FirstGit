/**
 * @file
 *   辞書引き部 内部定義
 *
 *   辞書引き部内部の共通定義
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2011 All Rights Reserved.
 */

#ifndef _NJD_H_
#define _NJD_H_

/************************************************/
/*             定数定義                         */
/************************************************/
/*
 * status定義(内部使用)
 */
#define NJ_ST_SEARCH_NO_INIT    1       /**< status定義:未初期化   */
#define NJ_ST_SEARCH_READY      2       /**< status定義:初期化済   */
#define NJ_ST_SEARCH_END        3       /**< status定義:次候補なし */
#define NJ_ST_SEARCH_END_EXT    4       /**< status定義:次候補なし(前方一致候補もなし) */

/*
 * 辞書種別の定義
 */
#define NJ_DIC_FMT_KANAKAN              0x0     /**< 辞書種別: かな漢字用辞書   */
#define NJ_DIC_FMT_KYOUYO               0x02    /**< 辞書種別: 共用辞書         */

/*
 * 辞書ヘッダー部
 */
#define NJ_DIC_ID_LEN                   (4)             /**< 辞書ヘッダ: 識別子サイズ */
#define NJ_DIC_IDENTIFIER               0x4e4a4443      /**< 辞書ヘッダ: 辞書識別子     */
#define NJ_DIC_COMMON_HEADER_SIZE       0x001C          /**< 辞書ヘッダ: 共通ヘッダーサイズ */
#define NJ_DIC_POS_DATA_SIZE            0x0c            /**< 辞書ヘッダ: 辞書データサイズ位置 */
#define NJ_DIC_POS_EXT_SIZE             0x10            /**< 辞書ヘッダ: 拡張情報サイズ位置 */
/* 辞書バージョンは基本的にはVer.2、読み無し予測辞書のみはVer.1 */
#define NJ_DIC_VERSION1                 0x00010000      /**< 辞書ヘッダ: バージョン-辞書Ver.1   */
#define NJ_DIC_VERSION2                 0x00020000      /**< 辞書ヘッダ: バージョン-辞書Ver.2   */
#define NJ_DIC_VERSION3                 0x00030000      /**< 辞書ヘッダ: バージョン-辞書Ver.3   */
#define NJ_DIC_VERSION4                 0x00040000      /**< 辞書ヘッダ: バージョン-辞書Ver.4   */
#define NJ_DIC_VERSION2_1               0x00020001      /**< 辞書ヘッダ: バージョン-辞書Ver.2.1(ルール辞書) */

/*
 * 単語登録に関する定義
 */
#define ADD_WORD_DIC_TYPE_USER  0       /**< 単語登録: ユーザ辞書に登録 */
#define ADD_WORD_DIC_TYPE_LEARN 1       /**< 単語登録: 学習辞書に登録 */
#define ADD_WORD_DIC_TYPE_PROGRAM 2     /**< 単語登録: 擬似辞書に登録 */

#ifndef STATE_WEIGHT
#define STATE_WEIGHT 10      /**< 属性データの重み付け */
#endif /* STATE_WEIGHT */

/*
 * 前確定情報生成の定義
 */
#define NJ_PREVIOUS_SELECTION_NORMAL 0    /**< 通常予測用前確定情報   */
#define NJ_PREVIOUS_SELECTION_AI     1    /**< AI予測用前確定情報     */

/**
 * 複合語判定current_info判定定義
 */
#define CMPDG_BIT_ON     0x80    /**< 複合語判定Bit ON  */
#define CMPDG_BIT_OFF    0x7F    /**< 複合語判定Bit OFF */

#define NJ_SDIC_HASH_SIZE               (0xFF + 1)                       /**< ストレージ辞書一時キャッシュハッシュ件数 */
#define NJ_SDIC_HASH_BLOCK_SIZE         32                               /**< ストレージ辞書一時キャッシュサイズ */
#define NJ_SDIC_HASH_HYOKI_INDEX_SIZE   (NJ_SDIC_HASH_BLOCK_SIZE)        /**< ストレージ辞書一時キャッシュサイズ:表記文字列インデックス */
#define NJ_SDIC_HASH_WORD_DATA_SIZE     (NJ_SDIC_HASH_BLOCK_SIZE)        /**< ストレージ辞書一時キャッシュサイズ:単語データ */
#define NJ_SDIC_HASH_YOMI_HYOKI_SIZE    (NJ_SDIC_HASH_BLOCK_SIZE * 4)    /**< ストレージ辞書一時キャッシュサイズ:読み・表記領域 */

#define NJ_SDIC_STORAGE_YOMI_INDEX        1       /**< キャッシュ番号：読み文字列インデックス領域 */
#define NJ_SDIC_STORAGE_TUNAGARI_INDEX    2       /**< キャッシュ番号：つながりインデックス領域 */
#define NJ_SDIC_STORAGE_HYOKI_INDEX       3       /**< キャッシュ番号：表記文字列インデックス領域 */
#define NJ_SDIC_STORAGE_HINSI_NO          4       /**< キャッシュ番号：品詞番号変換データ領域 */
#define NJ_SDIC_STORAGE_WORD_DATA         5       /**< キャッシュ番号：単語データ領域 */
#define NJ_SDIC_STORAGE_REAL_ZOKUSEI      6       /**< キャッシュ番号：実属性変換データ領域 */
#define NJ_SDIC_STORAGE_ZOKUSEI           7       /**< キャッシュ番号：属性データ領域 */
#define NJ_SDIC_STORAGE_JITU_MOJI         8       /**< キャッシュ番号：実文字コード変換データ領域 */
#define NJ_SDIC_STORAGE_TUNAGARI          9       /**< キャッシュ番号：つながりデータ領域 */
#define NJ_SDIC_STORAGE_YOMI_HYOKI       10       /**< キャッシュ番号：読み表記領域 */
#define NJ_SDIC_STORAGE_HINDO            11       /**< キャッシュ番号：頻度データ領域 */

#define NJ_SDIC_STORAGE_TYPE_NONE        0       /**< インデックス番号：指定なし */
#define NJ_SDIC_STORAGE_TYPE_YOMI        1       /**< インデックス番号：読み領域 */
#define NJ_SDIC_STORAGE_TYPE_HYOKI       2       /**< インデックス番号：表記領域 */


#define NJ_EXT_HINDO_YOMI_DATA_SIZE        0x01    /**< 頻度学習値データ：読みサイズ */
#define NJ_EXT_HINDO_HYOKI_DATA_SIZE       0x01    /**< 頻度学習値データ：表記サイズ */
#define NJ_EXT_HINDO_HINSI_DATA_SIZE       0x02    /**< 頻度学習値データ：前品詞・後品詞サイズ */
#define NJ_EXT_HINDO_HINDO_DATA_SIZE       0x01    /**< 頻度学習値データ：頻度学習データサイズ */

#define NJ_EXT_HINDO_IDENTIFIER    0x4E4A5550    /**< 頻度学習領域入出力データ：識別子(NJUP) */
#define NJ_EXT_HINDO_VERSION       0x00000001    /**< 頻度学習領域入出力データ：バージョン */
#define NJ_EXT_HINDO_RESERVE       0x00000000    /**< 頻度学習領域入出力データ：リザーブ領域(空き) */


#ifndef EXT_AREA_THRESHOLD
#define EXT_AREA_THRESHOLD 5 /**< 頻度学習：予測候補への引き上げ閾値 */ 
#endif /* EXT_AREA_THRESHOLD */

/** １単語の頻度学習サイズ */
#define EXT_AREA_WORD_SIZE  1
/** 頻度学習予測利用フラグ */
#define EXT_AREA_WORD_YOSOKU      0x80
/** 頻度学習削除フラグ */
#define EXT_AREA_WORD_DELETE      0x40
/** 頻度学習最適化フラグ */
#define EXT_AREA_OPTIMIZE         0x80
/** 頻度学習値インポートマスクフラグ */
#define EXT_AREA_WORD_FLAG_MASK   (EXT_AREA_WORD_DELETE | EXT_AREA_WORD_YOSOKU)


#define HASH_INDEX_CNT                257        /**< ハッシュインデックス数 */
#define WORD_DATA_AREA_SIZE           10         /**< 単語データ領域 1データバイト数 */
#define WORD_DATA_YOMI_BYTE_SIZE      1          /**< 単語データ領域 データサイズ -読みバイトサイズ- */
#define WORD_DATA_YOMI_DATA_SIZE      4          /**< 単語データ領域 データサイズ -読みバイト + 読み文字列位置サイズ- */
#define FUSION_DIC_FREQ_DIV           100        /**< 統合辞書頻度段階 */
#define FUSION_DIC_MAX_FREQ           100        /**< 統合辞書内単語最大頻度 */
#define INIT_HINDO                    (-10000)   /**< 初期化頻度値 */
#define HYOKI_INDEX_DATA_AREA_SIZE    4          /**< 表記文字列インデックスデータ領域 1データバイト数 */
#define WORD_DATA_HIGH_COMPRESS_AREA_SIZE   6    /**< 単語データ領域 1データバイト数 =>高圧縮辞書用 */
#define NJD_MAX_CHARACTER_LEN         110        /**< 最大文字配列要素数 */

#define NJ_DIC_ADD_ID_LEN          (4)           /**< 辞書ヘッダ: 付加情報領域識別子サイズ */
#define NJ_DIC_ADD_IDENTIFIER      0x4e4a4144    /**< 辞書ヘッダ: 付加情報領域識別子       */
#define NJ_DIC_ADD_HEAD_SIZE       0x28          /**< 辞書ヘッダ: 付加情報領域ヘッダサイズ */
#define NJ_DIC_ADD_LEARN_FLG       0x00000080    /**< 辞書ヘッダ: 学習有無フラグ           */
#define ADD_WORD_DATA_AREA_SIZE    6             /**< 付加情報データ領域 1データバイト数   */

#define NJ_DIC_EXT_ID_LEN            (4)           /**< 辞書ヘッダ: 頻度学習領域識別子サイズ */
#define NJ_DIC_EXT_IDENTIFIER        0x4e4a4558    /**< 辞書ヘッダ: 頻度学習領域識別子       */
#define NJ_DIC_EXT_HEAD_SIZE         0x20          /**< 辞書ヘッダ: 頻度学習領域ヘッダサイズ */
#define NJ_DIC_EXT_AREA_T_HINDO      0x3F          /**< 統合辞書：頻度学習最大頻度           */
#define NJ_DIC_EXT_INPUT_IDENTIFIER  0x4e4a4549    /**< 辞書ヘッダ: 頻度領域識別子           */


/**
 * 状況カテゴリ  設定情報
 */
#define DEFAULT_STATE_BIT          (NJ_UINT8)(0x40)    /* カテゴリ：予約：機能 */
#define MASK_RESERVE_BIT           (NJ_UINT8)(0xBF)    /* カテゴリマスク用：予約：機能 */
#define CHECK_DAILY_LIFE_BIT       (NJ_UINT8)(0x7E)    /* カテゴリ：日常生活表現：場所、乗り物、食べ物、飲み物、メディア1、メディア2 */
#define MASK_DAILY_LIFE_BIT        (NJ_UINT8)(0x81)    /* カテゴリマスク用：日常生活表現：場所、乗り物、食べ物、飲み物、メディア1、メディア2 */

#define CAT_POS_RESERVE            (NJ_UINT8)(0)       /* 予約：機能カテゴリデータまでの先頭からのByte数 */
#define CAT_POS_DAILY_LIFE         (NJ_UINT8)(3)       /* 日常生活表現カテゴリデータまでの先頭からのByte数 */


/********************************************************
 *  変更不可な値
 */
/** 学習辞書: キューのサイズ */
#define LEARN_DIC_QUE_SIZE        32
/** 学習辞書: 拡張キューのサイズ */
#define LEARN_DIC_EXT_QUE_SIZE    6
/** 学習辞書: 付加情報キューのヘッダサイズ */
#define LEARN_DIC_ADDITIONAL_QUE_HEAD_SIZE    2

/************************************************/
/*           マクロ定義                         */
/************************************************/


#define GET_LOCATION_STATUS(x) ((NJ_UINT8)((x)&0x0f))

#define GET_LOCATION_OPERATION(x) ((NJ_UINT8)(((x) >> 4)&0x0f))

#define SET_LOCATION_OPERATION(ope) ((NJ_UINT16)((ope) << 4))


#define NJ_GET_DIC_FMT(h) ((NJ_UINT8)((*((h)+0x1C)) & 0x03))

#define NJ_GET_DIC_VER(h) NJ_INT32_READ((h)+4)


#define CALCULATE_HINDO(freq, base, high, div) \
    ((NJ_HINDO)((((freq) * ((high) - (base))) / (div)) + (base)))

#define CALCULATE_HINDO_UNDER_BIAS(freq, width) \
    ((NJ_HINDO)((((freq) * (width)) / CAND_FZK_HINDO_BIAS) - (width)))

#define NORMALIZE_HINDO(freq, max, min) \
    (((freq) < (min)) ? (min) : (((freq) > (max)) ? (max) : (freq)))

#define CALCULATE_ATTR_HINDO32(freq, base, high, div)                   \
    ( ((high) > (base))                                                 \
      ? ((NJ_INT32)(((freq) * (div)) / ((high) - (base)))) \
      : ((NJ_INT32)((freq))) )

#define HAS_ADDITIONAL_INFO(word, index, dictype)       \
    ((((word).stem.loc.add_info[(index)] != NULL) ||    \
     (((dictype) == NJ_DIC_TYPE_LEARN) ||               \
      ((dictype) == NJ_DIC_TYPE_USER) ||                \
      ((dictype) == NJ_DIC_TYPE_PROGRAM) ||             \
      ((dictype) == NJ_DIC_TYPE_PROGRAM_FZK))) ? 1 : 0)


#define CMPDG_CURRENT_NUM (NJ_UINT8)(NJ_NUM_SEGMENT2 << 4)

#define HAS_COMPOUND_STATUS(word)                        \
    ((((NJ_GET_DIC_TYPE_EX((word)->stem.loc.type, (word)->stem.loc.handle) == NJ_DIC_TYPE_LEARN) &&     \
     (((word)->stem.loc.status >> 4) == 0x01))) ? 1 : 0) /* 0x01: 前方一致検索 */

#define IS_COMPOUND_WORD(word)                        \
    ((HAS_COMPOUND_STATUS(word) &&             \
     (((word)->stem.loc.current_info) == CMPDG_CURRENT_NUM)) ? 1 : 0)

#define NJ_SDIC_GET_STORAGE_MODE(x) ((NJ_UINT32)((x) & NJ_STORAGE_MODE_ONMEMORY))



/************************************************/
/*           データ構造定義                     */
/************************************************/
#define NJD_AIP_WORK_CNT        10      /**< AI予測用処理結果保持件数         */
#define NJD_AIP_TOP_CODE        0x02    /**< AI予測データ検索用先頭文字コード */
#define NJD_AIP_PERMISSION_WORD 2       /**< AI予測時前確定許容単語数         */
/**
 * AI予測データ記憶領域
 */
typedef struct {
    NJ_RESULT              result;      /**< 検索結果保存エリア */
    NJ_SEARCH_LOCATION_SET loctset;     /**< 辞書毎検索単語情報 */
    NJ_WORD                word;        /**< 単語情報           */
} NJD_AIP_DATA;


/**
 * AI予測用ワーク領域
 */
typedef struct {
    NJ_UINT32           save_cnt;                   /**< 検索結果保存件数   */
    NJ_UINT32           status;                     /**< ステータス情報     */
    NJ_SEARCH_CONDITION condition;                  /**< 辞書検索条件セット */
    NJ_DIC_SET          dicset;                     /**< 辞書セット         */
    NJD_AIP_DATA        aip_dat[NJD_AIP_WORK_CNT];  /**< AI予測データ       */
} NJD_AIP_WORK;


/**
 * ストレージ辞書一時キャッシュ定義：表記文字列インデックス
 */
typedef struct {
    struct {
        NJ_UINT32   offset;
        NJ_UINT32   dummy[(NJ_SDIC_HASH_BLOCK_SIZE / 4) - 1];
        NJ_UINT8    data[NJ_SDIC_HASH_HYOKI_INDEX_SIZE];
    } hash_data[NJ_SDIC_HASH_SIZE];
} NJ_STORAGE_DIC_CACHE_HYOKI_INDEX;

/**
 * ストレージ辞書一時キャッシュ定義：単語データ
 */
typedef struct {
    struct {
        NJ_UINT32   offset;
        NJ_UINT32   dummy[(NJ_SDIC_HASH_BLOCK_SIZE / 4) - 1];
        NJ_UINT8    data[NJ_SDIC_HASH_WORD_DATA_SIZE];
    } hash_data[NJ_SDIC_HASH_SIZE];
} NJ_STORAGE_DIC_CACHE_WORD_DATA;

/**
 * ストレージ辞書一時キャッシュ定義：読み・表記領域
 */
typedef struct {
    struct {
        NJ_UINT32   offset;
        NJ_UINT32   dummy[(NJ_SDIC_HASH_BLOCK_SIZE / 4) - 1];
        NJ_UINT8    data[NJ_SDIC_HASH_YOMI_HYOKI_SIZE];
    } hash_data[NJ_SDIC_HASH_SIZE];
} NJ_STORAGE_DIC_CACHE_YOMI_HYOKI;

/************************************************/
/*           プロトタイプ宣言                   */
/************************************************/
#endif /* _NJD_H_ */
