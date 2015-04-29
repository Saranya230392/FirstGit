/**
 * @file
 *   デコメ絵文字読み入力定義（共通編）
 *
 *   アプリケーションで使用する為に必要な定義を行う。
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2010 All Rights Reserved.
 */
#ifndef _DEMOJI_LIB_H_
#define _DEMOJI_LIB_H_

#ifdef    __cplusplus
extern "C" {
#endif    /* __cplusplus */

/************************************************/
/*                 typedef 宣 言                */
/************************************************/
/**
 * 数字型
 */
typedef unsigned char     DL_UINT8;     /**<  8bit 符号なし整数 */
typedef unsigned short    DL_UINT16;    /**< 16bit 符号なし整数 */
typedef unsigned long     DL_UINT32;    /**< 32bit 符号なし整数 */
typedef signed char       DL_INT8;      /**<  8bit 符号あり整数 */
typedef signed short      DL_INT16;     /**< 16bit 符号あり整数 */
typedef signed long       DL_INT32;     /**< 32bit 符号あり整数 */

/**
 * VOID型
 */
typedef void              DL_VOID;

/************************************************/
/*                 define 宣 言                 */
/************************************************/
/**
 * APIの戻り値
 */
#define DL_ERROR_NONE               ((DL_INT16)0)         /**< 正常終了 */
#define DL_ERROR_PARAM              ((DL_INT16)-1)        /**< エラー:引数エラー */
#define DL_ERROR_INITIALIZED        ((DL_INT16)-2)        /**< エラー:既に初期化されている */
#define DL_ERROR_NOT_INITIALIZED    ((DL_INT16)-3)        /**< エラー:初期化されていない */
#define DL_ERROR_SAME_WORD          ((DL_INT16)-4)        /**< エラー:登録する内容が重複している */
#define DL_ERROR_DIC_FULL           ((DL_INT16)-5)        /**< エラー:最大件数登録されている */
#define DL_ERROR_MM_FAIL            ((DL_INT16)-6)        /**< エラー:形態素解析に失敗した */
#define DL_ERROR_INTERNAL           ((DL_INT16)-99)       /**< エラー:内部エラー */

/**
 * 品詞値
 */
#define DL_PART_MAX                 21                    /**< 品詞最大値 */
#define DL_PART_CNT                 22                    /**< 品詞定義数 */
#define DL_PART_NONE                ((DL_UINT16)0)        /**< 品詞指定なし */
#define DL_PART_CATEGORY_1          ((DL_UINT16)1)        /**< 名詞 */
#define DL_PART_CATEGORY_2          ((DL_UINT16)2)        /**< 名詞-固有名詞 */
#define DL_PART_CATEGORY_3          ((DL_UINT16)3)        /**< 名詞-代名詞 */
#define DL_PART_CATEGORY_4          ((DL_UINT16)4)        /**< 名詞-副詞可能 */
#define DL_PART_CATEGORY_5          ((DL_UINT16)5)        /**< 名詞-サ変接続 */
#define DL_PART_CATEGORY_6          ((DL_UINT16)6)        /**< 名詞-形容動詞語幹 */
#define DL_PART_CATEGORY_7          ((DL_UINT16)7)        /**< 名詞-ナイ形容詞語幹 */
#define DL_PART_CATEGORY_8          ((DL_UINT16)8)        /**< 名詞-数 */
#define DL_PART_CATEGORY_9          ((DL_UINT16)9)        /**< 名詞-非自立 */
#define DL_PART_CATEGORY_10         ((DL_UINT16)10)       /**< 名詞-特殊 */
#define DL_PART_CATEGORY_11         ((DL_UINT16)11)       /**< 名詞-接尾 */
#define DL_PART_CATEGORY_12         ((DL_UINT16)12)       /**< 名詞-接続詞的 */
#define DL_PART_CATEGORY_13         ((DL_UINT16)13)       /**< 名詞-動詞的 */
#define DL_PART_CATEGORY_14         ((DL_UINT16)14)       /**< 接頭詞 */
#define DL_PART_CATEGORY_15         ((DL_UINT16)15)       /**< 動詞 */
#define DL_PART_CATEGORY_16         ((DL_UINT16)16)       /**< 形容詞 */
#define DL_PART_CATEGORY_17         ((DL_UINT16)17)       /**< 副詞 */
#define DL_PART_CATEGORY_18         ((DL_UINT16)18)       /**< 連体詞 */
#define DL_PART_CATEGORY_19         ((DL_UINT16)19)       /**< 接続詞 */
#define DL_PART_CATEGORY_20         ((DL_UINT16)20)       /**< 感動詞 */
#define DL_PART_CATEGORY_21         ((DL_UINT16)21)       /**< 記号 */

/**
 * サイズ値
 */
#define DL_DEMOJIDIC_MAX_COUNT      20000                 /**< 絵文字辞書最大単語登録数 */
#define DL_MAX_ID_LEN               12                    /**< 絵文字IDの最大配列長 */
#define DL_MAX_YOMI_LEN             24                    /**< 絵文字読みの最大文字長 */
#define DL_MAX_KOUHO_LEN            20
#define DL_DEMOJIDIC_SIZE           (80 + ((DL_MAX_YOMI_LEN * 2) + (DL_MAX_KOUHO_LEN * 2) + 9) * DL_DEMOJIDIC_MAX_COUNT)  /**< 絵文字辞書サイズ */
#define DL_MAX_DIC                  ((DL_UINT8)10)        /**< 基本辞書の最大使用数 */

/**
 * 形態素解析用辞書推奨頻度値
 */
#define DL_UBASE_DIC_FREQ_M_HIGH    500                   /**< 統合辞書[形態素解析用]-最高頻度値-            */
#define DL_UBASE_DIC_FREQ_M_BASE    400                   /**< 統合辞書[形態素解析用]-底上げ頻度値-          */

#define DL_TAN_DIC_FREQ_M_HIGH       10                   /**< 単漢字辞書[形態素解析用]-最高頻度値-          */
#define DL_TAN_DIC_FREQ_M_BASE        0                   /**< 単漢字辞書[形態素解析用]-底上げ頻度値-        */

#define DL_FZK_DIC_FREQ_M_HIGH      500                   /**< 付属語辞書[形態素解析用]-最高頻度値-          */
#define DL_FZK_DIC_FREQ_M_BASE      400                   /**< 付属語辞書[形態素解析用]-底上げ頻度値-        */

#define DL_USER_DIC_FREQ_M_HIGH       0                   /**< ユーザ辞書[形態素解析用]-最高頻度値-          */
#define DL_USER_DIC_FREQ_M_BASE      10                   /**< ユーザ辞書[形態素解析用]-底上げ頻度値-        */

#define DL_CUSTOM_DIC_FREQ_M_HIGH   400                   /**< カスタマイズ辞書[形態素解析用]-最高頻度値-    */
#define DL_CUSTOM_DIC_FREQ_M_BASE     0                   /**< カスタマイズ辞書[形態素解析用]-底上げ頻度値-  */


/**
 * 形態素解析実行フラグ
 */
#define DL_MM_ON                    1                     /**< 形態素解析実行ON  */
#define DL_MM_OFF                   0                     /**< 形態素解析実行OFF */

/**
 * iWnn解析クラスのサイズ取得マクロ
 */
#define DL_GET_IWNN_CLASS_SIZE() demoji_getIwnnClassSize()

/************************************************/
/*                構 造 体 定 義                */
/************************************************/
/**
 * 逆引き用基本辞書情報
 */
typedef struct {
    DL_UINT8 *handle;       /**< 辞書ハンドル     */
    DL_UINT16 freq_high_m;  /**< 形態素解析用辞書頻度-最高-   */
    DL_UINT16 freq_base_m;  /**< 形態素解析用辞書頻度-底上げ- */
} DL_BASIC_DIC_INFO;

/**
 * デコレ絵文字辞書情報構造体
 */
typedef struct {
    DL_UINT8*    iwnn;                         /**< iWnn解析クラスバッファ   */
    DL_BASIC_DIC_INFO  basicdics[DL_MAX_DIC];  /**< 基本辞書バッファ         */
    DL_UINT8     basicdics_cnt;                /**< 基本辞書使用数           */
    DL_UINT8*    ruledic_h;                    /**< 変換用ルール辞書バッファ */
    DL_UINT8*    ruledic_m;                    /**< 形態素解析用ルール辞書バッファ */
    DL_UINT8*    demojidic;                    /**< 絵文字辞書バッファ       */
    DL_UINT8     init_flg;                     /**< 初期化フラグ             */
} DL_DEMOJI_INFO;


/************************************************/
/*                 extern 宣 言                 */
/************************************************/
/**
 * extern 宣言用の定義
 */
#define DL_EXTERN    extern

/* 初期化処理 */
DL_EXTERN DL_INT16 demoji_init(DL_DEMOJI_INFO *demoji);

/* 終了処理 */
DL_EXTERN DL_INT16 demoji_quit(DL_DEMOJI_INFO *demoji);

/* 絵文字辞書領域作成 */
DL_EXTERN DL_INT16 demoji_create_dic(DL_UINT8* iwnn, DL_UINT8* handle);

/* 絵文字辞書チェック */
DL_EXTERN DL_INT16 demoji_check_dic(DL_UINT8* iwnn, DL_UINT8* handle);

/* 絵文字登録 */
DL_EXTERN DL_INT16 demoji_addToEmojiDictionary(DL_DEMOJI_INFO *demoji, DL_UINT16 *id, DL_UINT16 *yomi, DL_UINT16 hinsi, DL_UINT16 mm_flag);

/* 絵文字辞書更新 */
DL_EXTERN DL_INT16 demoji_updateEmojiDictionary(DL_DEMOJI_INFO *demoji, DL_UINT16 *id, DL_UINT16 *yomi, DL_UINT16 hinsi, DL_UINT16 *newYomi, DL_UINT16 newHinsi, DL_UINT16 mm_flag);

/* 絵文字削除 */
DL_EXTERN DL_INT16 demoji_deleteFromEmojiDictionary(DL_DEMOJI_INFO *demoji, DL_UINT16 *id, DL_UINT16 *yomi, DL_UINT16 hinsi);

/* iWnn解析クラスのサイズ取得 */
DL_EXTERN DL_UINT32 demoji_getIwnnClassSize(DL_VOID);

#ifdef __cplusplus
   }
#endif /* __cplusplus */

#endif /* _DEMOJI_LIB_H_ */
