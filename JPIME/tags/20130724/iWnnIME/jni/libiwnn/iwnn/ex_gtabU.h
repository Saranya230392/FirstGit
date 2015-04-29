/**
 * @file
 *  [拡張] iWnn標準擬似候補作成辞書テーブル定義 (UTF16版)
 *
 * @author
 * (C) OMRON SOFTWARE Co., Ltd. 2008 All Rights Reserved.
 */
/************************************************/
/*         define  宣  言 (以下AWnnより流用)      */
/************************************************/
/*
 * 読み、擬似候補テーブル数定義
 */
#define NJ_YOMI_HIRA_MAX      83    /**< 読み文字テーブル(かな)数 */
#define NJ_YOMI_SYM_MAX       21    /**< 読み文字テーブル(記号)数 */
#define NJ_KURAI_TABLE_MAX     5    /**< 漢数字(位)テーブル数 */
#define NJ_KANSUUJI_TABLE_MAX 10    /**< 漢数字テーブル数 */
#define NJ_DAYOFWEEK_TABLE_MAX 7    /**< 曜日データテーブル数 */
#define NJ_ROMA_2TOUCH_YOMI_TABLE_MAX 156 /**< 読み文字テーブル(2タッチ入力)数 */
#define NJ_ROMA_2TOUCH_TABLE_MAX      156 /**< 文字テーブル(2タッチ入力)数 */
#define NJ_SUUJI_EMOJI_TABLE_MAX       10 /**< 数字絵文字テーブル数 */
#define NJ_SUUJI_KANJI_TABLE_MAX       10 /**< 数字漢数字テーブル数 */
#define NJ_SUUJI_MARU_TABLE_MAX        20 /**< 数字丸数字テーブル数 */
#define NJ_SUUJI_ROMA_TABLE_MAX        10 /**< 数字ローマ数字テーブル数 */
#define NJ_SUUJI_TABLE_MAX             10 /**< 数字テーブル数 */
#define NJ_SUUJI_KURAI_SEN_TABLE_MAX    4 /**< 漢数字(位)テーブル数(千の位まで) */
#define NJ_SUUJI_KURAI_TYOU_TABLE_MAX   3 /**< 漢数字(位)テーブル数(万・億・兆) */

#define NJ_NENGO_LIMIT_SHOWA_YEAR    1989 /**< 年号(昭和)の終端西暦年 */
#define NJ_NENGO_LIMIT_SHOWA_MONTH      1 /**< 年号(昭和)の終端月 */
#define NJ_NENGO_LIMIT_SHOWA_DAY        7 /**< 年号(昭和)の終端日 */

#define NJ_NENGO_BASE_SHOWA          1925 /**< 年号(昭和)算出基準年 */
#define NJ_NENGO_BASE_HEISEI         1988 /**< 年号(平成)算出基準年 */


/*
 * キーアサイン定義
 *   ひらがな文字が割り付けられているキーアサイン
 */
#define NJ_NUM_0        0           /**< キー定義：0キー */
#define NJ_NUM_1        1           /**< キー定義：1キー */
#define NJ_NUM_2        2           /**< キー定義：2キー */
#define NJ_NUM_3        3           /**< キー定義：3キー */
#define NJ_NUM_4        4           /**< キー定義：4キー */
#define NJ_NUM_5        5           /**< キー定義：5キー */
#define NJ_NUM_6        6           /**< キー定義：6キー */
#define NJ_NUM_7        7           /**< キー定義：7キー */
#define NJ_NUM_8        8           /**< キー定義：8キー */
#define NJ_NUM_9        9           /**< キー定義：9キー */
#define NJ_NUM_SHP     10           /**< キー定義：#キー */
#define NJ_NUM_NON (NJ_UINT16)(-1)  /**< キー定義：定義なし */

/*
 * 漢数字の桁定義
 */
#define NJ_KETA_JYUU  101   /**< 数字桁：10 */
#define NJ_KETA_HYAKU 102   /**< 数字桁：100 */
#define NJ_KETA_SEN   103   /**< 数字桁：1000 */
#define NJ_KETA_ZEN   104   /**< 数字桁：1000(読み：ぜん) */
#define NJ_KETA_MAN   1004  /**< 数字桁：10000 */
#define NJ_KETA_OKU   1008  /**< 数字桁：100000000 */
#define NJ_KETA_CHO   1012  /**< 数字桁：1000000000000 */
/* 十、百、千は、100を引くと桁数 それ以上は1000を引くと桁数 */
/** 桁数 = NJ_KETA_{JYUU|HYAKU|SEN} - NJG_SUB_CNT_JUUTOSEN */
#define NJG_SUB_CNT_JYUUTOSEN   100
/** 桁数 = NJ_KETA_{MAN|OKU|CHO} - NJG_SUB_CNT_MANTOCHO */
#define NJG_SUB_CNT_MANTOCHO   1000
#define NJG_FIND_OKUMANCHO     0x20  /**< 億万兆あり */
#define NJG_FIND_JYUUHYAKUSEN  0x21  /**< 十百千あり */


/************************************************/
/*                                              */
/* 時間・日付擬似候補定義                          */
/*                                              */
/************************************************/
/* giji_conv_table[] の要素定義 */
/* Time */
#define NJG_TBL_NO_THH        0  /**< giji_conv_table[] の要素: HH */
#define NJG_TBL_NO_TMM        1  /**< giji_conv_table[] の要素: MM */
#define NJG_TBL_NO_THM        2  /**< giji_conv_table[] の要素: HM */
#define NJG_TBL_NO_THMM       3  /**< giji_conv_table[] の要素: HMM */
#define NJG_TBL_NO_THHM       4  /**< giji_conv_table[] の要素: HHM */
#define NJG_TBL_NO_THHMM      5  /**< giji_conv_table[] の要素: HHMM */
#define NJG_TBL_NO_THMM_SYM   6  /**< giji_conv_table[] の要素: HMM_SYM */
#define NJG_TBL_NO_THHMM_SYM  7  /**< giji_conv_table[] の要素: HHMM_SYM */
/* Date */
#define NJG_TBL_NO_DYYYY      8  /**< giji_conv_table[] の要素: YYYY */
#define NJG_TBL_NO_DMM        9  /**< giji_conv_table[] の要素: MM */
#define NJG_TBL_NO_DDD        10  /**< giji_conv_table[] の要素: DDD */
#define NJG_TBL_NO_DMD        11  /**< giji_conv_table[] の要素: MD */
#define NJG_TBL_NO_DMDD       12  /**< giji_conv_table[] の要素: MDD */
#define NJG_TBL_NO_DMMD       13  /**< giji_conv_table[] の要素: MMD */
#define NJG_TBL_NO_DMMDD      14  /**< giji_conv_table[] の要素: MMDD */
#define NJG_TBL_NO_DMD_SYM    15  /**< giji_conv_table[] の要素: MD_SYM */
#define NJG_TBL_NO_DMDD_SYM   16  /**< giji_conv_table[] の要素: MDD_SYM */
#define NJG_TBL_NO_DMMD_SYM   17  /**< giji_conv_table[] の要素: MMD_SYM */
#define NJG_TBL_NO_DMMDD_SYM  18  /**< giji_conv_table[] の要素: MMDD_SYM */


/*******************************************
 * giji_suuji_tbl の定義
 *******************************************/
/* テーブルの配列要素定義 */
#define NJG_SUUJI_TBL_HAN          0  /**< テーブル配列要素:半角 */
#define NJG_SUUJI_TBL_ZEN          1  /**< テーブル配列要素:全角 */
#define NJG_SUUJI_TBL_KAN          2  /**< テーブル配列要素:漢字 */
#define NJG_SUUJI_TBL_KAN_DAIJI    3  /**< テーブル配列要素:漢字(大字) */
/** 数字文字の種類定義 */
#define NJG_SUUJI_TBL_NO      4
/** 数字文字数 */
#define NJG_SUUJI_REC        12

/** 数字候補出力桁数定義 */
#define NJG_KETA_SUUJI_DISABLE  0  /**< 数字出力桁数確認不要 */
#define NJG_KETA_SUUJI_EMOJI    1  /**< 数字絵文字擬似候補出力桁数 */
#define NJG_KETA_SUUJI_KANJI   13  /**< 数字漢数字擬似候補出力桁数 */
#define NJG_KETA_SUUJI_MARU     2  /**< 数字丸数字擬似候補出力桁数 */
#define NJG_KETA_SUUJI_ROMA     2  /**< 数字ローマ数字擬似候補出力桁数 */
#define NJG_KETA_SUUJI_COMMA   13  /**< 数字コンマ付き擬似候補出力桁数 */

/** 数字候補データ有効範囲定義 */
#define NJG_MIN_NUM_SUUJI_MARU   1                       /**< 数字丸数字候補変換最小値 */
#define NJG_MAX_NUM_SUUJI_MARU  NJ_SUUJI_MARU_TABLE_MAX  /**< 数字丸数字候補変換最大値 */
#define NJG_MIN_NUM_SUUJI_ROMA   1                       /**< 数字ローマ数字候補変換最小値 */
#define NJG_MAX_NUM_SUUJI_ROMA  NJ_SUUJI_TABLE_MAX       /**< 数字ローマ数字候補変換最大値 */


/*******************************************
 * 「ウ゛」→「ヴ」変換処理
 *******************************************/
/** ウ (UTF16 KATAKANA LETTER U) */
#define NJG_KATAKANA_U          0x30a6
/** ゛ (UTF16 KATAKANA-HIRAGANA VOICED SOUND MARK) */
#define NJG_KATAKANA_DAKUON     0x309b
/** ヴ (UTF16 KATAKANA-LETTER VU) */
#define NJG_KATAKANA_VU         0x30f4


/*******************************************
 * 半角数字カンマ区切り
 *******************************************/
/** カンマ記号 [,] (UTF16 COMMA) */
#define NJG_COMMA_MARK  0x002c
#define NJG_HAN_DAKUTEN_MARK     0xFF9E
#define NJG_HAN_HANDAKUTEN_MARK  0xFF9F

/***********************************************************************
 * 構造体宣言
 ***********************************************************************/
/**
 * 読み文字テーブルの要素定義
 */
typedef struct {
    NJ_UINT16  yomi;      /* 読み文字 */
    NJ_UINT16  num;       /* キーアサイン番号 */
    NJ_UINT16  toggle;    /* トグル数(1〜) */
} NJ_GIJI_YOMI_TABLE;

/**
 * 数字擬似テーブルの要素定義
 */
typedef struct {
    NJ_UINT16 num;       /**< 数値データ(Hex) */
    NJ_UINT16 half;      /**< 半角数字 */
    NJ_UINT16 full;      /**< 全角数字 */
    NJ_UINT16 kanji;     /**< 漢数字(全角) */
} NJ_GIJI_SUUJI_TABLE;

/**
 * 漢数字擬似テーブルの要素定義
 */
typedef struct  {
    NJ_UINT16 yomi[3];   /**< 漢数字よみ */
    NJ_INT16  len;       /**< 読み文字長 */
    NJ_INT16  ketapos;   /**< 対応する数（十以上は桁数を表す）*/
} NJ_SUUJIYOMITBL;

/**
 * 時間擬似、日付擬似の単位変換テーブルの定義
 */
typedef struct {
    NJ_INT16 f_digit;        /**< 途中で格納する表示コードの位置 */
    NJ_UINT16 f_code;        /**< 時、月表示用コード*/
    NJ_UINT16 s_code;        /**< 分、日表示用コード*/
    NJ_INT16 code_size;      /**< 表示用コードのバイト数 */
} NJ_CONVERT_TBL;

/************************************************/
/*                 テーブル定義                   */
/************************************************/
/*************************************************
 * 読み文字テーブル
 * 
 * ※文字コードをキーとした検索を行うため、
 *   文字コード順に並べるものとする。
 * 
 * ※このキーアサインを標準としている。
 *   このアサインのうち、記号とそれ以外にテーブルを分割している。
 * 
 *     <toggle> 1   2   3   4   5   6   7   8   9  10  11
 * <KeyAssign>
 *   <  1 >    あ  い  う  え  お  ぁ  ぃ  ぅ  ぇ  ぉ    
 *   <  2 >    か  き  く  け  こ
 *   <  3 >    さ  し  す  せ  そ
 *   <  4 >    た  ち  つ  て  と  っ
 *   <  5 >    な  に  ぬ  ね  の
 *   <  6 >    は  ひ  ふ  へ  ほ
 *   <  7 >    ま  み  む  め  も
 *   <  8 >    や  ゆ  よ  ゃ  ゅ  ょ
 *   <  9 >    ら  り  る  れ  ろ
 *   <  0 >    わ  を  ん  ゎ  ー
 *   <  # >    、  。  ？  ！  ・  SP
 * 
 *   <  2 >    が  ぎ  ぐ  げ  ご
 *   <  3 >    ざ  じ  ず  ぜ  ぞ
 *   <  4 >    だ  ぢ  づ  で  ど
 *   <  6 >    ば  び  ぶ  べ  ぼ
 *   <  6 >    ぱ  ぴ  ぷ  ぺ  ぽ
 * 
 *   <なし>    ．  ＠  ／  ：  −  ＿
 * 
 */


/**
 *  読み文字 ひらがな
 */
static const NJ_GIJI_YOMI_TABLE giji_yomi_hira_tbl[NJ_YOMI_HIRA_MAX] = {
/* ぁ あ          */{0x3041, NJ_NUM_1, 6}, {0x3042, NJ_NUM_1, 1},
/* ぃ い          */{0x3043, NJ_NUM_1, 7}, {0x3044, NJ_NUM_1, 2},
/* ぅ う          */{0x3045, NJ_NUM_1, 8}, {0x3046, NJ_NUM_1, 3},
/* ぇ え          */{0x3047, NJ_NUM_1, 9}, {0x3048, NJ_NUM_1, 4},
/* ぉ お          */{0x3049, NJ_NUM_1, 10}, {0x304a, NJ_NUM_1, 5},
/* か が          */{0x304b, NJ_NUM_2, 1}, {0x304c, NJ_NUM_NON, 0},
/* き ぎ          */{0x304d, NJ_NUM_2, 2}, {0x304e, NJ_NUM_NON, 0},
/* く ぐ          */{0x304f, NJ_NUM_2, 3}, {0x3050, NJ_NUM_NON, 0},
/* け げ          */{0x3051, NJ_NUM_2, 4}, {0x3052, NJ_NUM_NON, 0},
/* こ ご          */{0x3053, NJ_NUM_2, 5}, {0x3054, NJ_NUM_NON, 0},
/* さ ざ          */{0x3055, NJ_NUM_3, 1}, {0x3056, NJ_NUM_NON, 0},
/* し じ          */{0x3057, NJ_NUM_3, 2}, {0x3058, NJ_NUM_NON, 0},
/* す ず          */{0x3059, NJ_NUM_3, 3}, {0x305a, NJ_NUM_NON, 0},
/* せ ぜ          */{0x305b, NJ_NUM_3, 4}, {0x305c, NJ_NUM_NON, 0},
/* そ ぞ          */{0x305d, NJ_NUM_3, 5}, {0x305e, NJ_NUM_NON, 0},
/* た だ          */{0x305f, NJ_NUM_4, 1}, {0x3060, NJ_NUM_NON, 0},
/* ち ぢ          */{0x3061, NJ_NUM_4, 2}, {0x3062, NJ_NUM_NON, 0},
/* っ つ づ       */{0x3063, NJ_NUM_4, 6}, {0x3064, NJ_NUM_4, 3}, {0x3065, NJ_NUM_NON, 0},
/* て で          */{0x3066, NJ_NUM_4, 4}, {0x3067, NJ_NUM_NON, 0},
/* と ど          */{0x3068, NJ_NUM_4, 5}, {0x3069, NJ_NUM_NON, 0},
/* な に ぬ ね の */{0x306a, NJ_NUM_5, 1}, {0x306b, NJ_NUM_5, 2}, {0x306c, NJ_NUM_5, 3}, {0x306d, NJ_NUM_5, 4}, {0x306e, NJ_NUM_5, 5},
/* は ば ぱ       */{0x306f, NJ_NUM_6, 1}, {0x3070, NJ_NUM_NON, 0}, {0x3071, NJ_NUM_NON, 0},
/* ひ び ぴ       */{0x3072, NJ_NUM_6, 2}, {0x3073, NJ_NUM_NON, 0}, {0x3074, NJ_NUM_NON, 0},
/* ふ ぶ ぷ       */{0x3075, NJ_NUM_6, 3}, {0x3076, NJ_NUM_NON, 0}, {0x3077, NJ_NUM_NON, 0},
/* へ べ ぺ       */{0x3078, NJ_NUM_6, 4}, {0x3079, NJ_NUM_NON, 0}, {0x307a, NJ_NUM_NON, 0},
/* ほ ぼ ぽ       */{0x307b, NJ_NUM_6, 5}, {0x307c, NJ_NUM_NON, 0}, {0x307d, NJ_NUM_NON, 0},
/* ま み む め も */{0x307e, NJ_NUM_7, 1}, {0x307f, NJ_NUM_7, 2}, {0x3080, NJ_NUM_7, 3}, {0x3081, NJ_NUM_7, 4}, {0x3082, NJ_NUM_7, 5},
/* ゃ や          */{0x3083, NJ_NUM_8, 4}, {0x3084, NJ_NUM_8, 1},
/* ゅ ゆ          */{0x3085, NJ_NUM_8, 5}, {0x3086, NJ_NUM_8, 2},
/* ょ よ          */{0x3087, NJ_NUM_8, 6}, {0x3088, NJ_NUM_8, 3},
/* ら り る れ ろ */{0x3089, NJ_NUM_9, 1}, {0x308a, NJ_NUM_9, 2}, {0x308b, NJ_NUM_9, 3}, {0x308c, NJ_NUM_9, 4}, {0x308d, NJ_NUM_9, 5},
/* ゎ わ          */{0x308e, NJ_NUM_0, 4}, {0x308f, NJ_NUM_0, 1},
/* ゐ ゑ          */{0x0000, NJ_NUM_NON, 0},{0x0000, NJ_NUM_NON, 0},/* 未対応とする */
/* を ん          */{0x3092, NJ_NUM_0, 2}, {0x3093, NJ_NUM_0, 3}
};

/**
 * 読み文字 記号
 */
static const NJ_GIJI_YOMI_TABLE giji_yomi_sym_tbl[NJ_YOMI_SYM_MAX] = {
/* 、 */{0x3001, NJ_NUM_NON, 0},
/* 。 */{0x3002, NJ_NUM_NON, 0},
/* ー */{0x30fc, NJ_NUM_0, 5},
/* ・ */{0x30fb, NJ_NUM_NON, 0},
/* 〜 */{NJ_CHAR_WAVE_DASH_BIG, NJ_NUM_0, 9},
/* ！ */{0xff01, NJ_NUM_NON, 0},
/* ？ */{0xff1f, NJ_NUM_NON, 0},
/* − */{0xff0d, NJ_NUM_NON, 0},
/* ． */{0xff0e, NJ_NUM_NON, 0}, /**/
/* ／ */{0xff0f, NJ_NUM_NON, 0},
/* ： */{0xff1a, NJ_NUM_NON, 0},
/* ＠ */{0xff20, NJ_NUM_NON, 0},
/* ＿ */{0xff3f, NJ_NUM_NON, 0},
/* ， */{0xff0c, NJ_NUM_NON, 0},
/* ． */{0xff0e, NJ_NUM_NON, 0}, /**/
/* ゛ */{0x309b, NJ_NUM_NON, 0},
/* ゜ */{0x309c, NJ_NUM_NON, 0},
/* ＆ */{0xff06, NJ_NUM_NON, 0},
/* ＝ */{0xff1d, NJ_NUM_NON, 0},
/* ヴ */{0x30f4, NJ_NUM_NON, 0},
/* 　 */{0x3000, NJ_NUM_NON, 0}
};

/*******************************************
 * 擬似候補テーブル
 * 
 *   文字コード順ではなく、読み文字テーブル
 *   の順と一致するよう並べるものとします。
 */
/**
 * 全角カタカナ
 */
/*
 * 
 * ※このキーアサインを標準としている。
 *   このアサインのうち、記号とそれ以外にテーブルを分割している。
 * 
 *     <toggle> 1   2   3   4   5   6   7   8   9  10  11
 * <KeyAssign>
 *   <  1 >    ア  イ  ウ  エ  オ  ァ  ィ  ゥ  ェ  ォ  
 *   <  2 >    カ  キ  ク  ケ  コ
 *   <  3 >    サ  シ  ス  セ  ソ
 *   <  4 >    タ  チ  ツ  テ  ト  ッ
 *   <  5 >    ナ  ニ  ヌ  ネ  ノ
 *   <  6 >    ハ  ヒ  フ  ヘ  ホ
 *   <  7 >    マ  ミ  ム  メ  モ
 *   <  8 >    ヤ  ユ  ヨ  ャ  ュ  ョ
 *   <  9 >    ラ  リ  ル  レ  ロ
 *   <  0 >    ワ  ヲ  ン  ヮ  ー
 *   <  # >    、  。  ？  ！  ・  SP
 *   
 *   <  2 >    ガ  ギ  グ  ゲ  ゴ
 *   <  3 >    ザ  ジ  ズ  ゼ  ゾ
 *   <  4 >    ダ  ヂ  ヅ  デ  ド
 *   <  6 >    バ  ビ  ブ  ベ  ボ
 *   <  6 >    パ  ピ  プ  ペ  ポ
 * 
 *   <なし>    ．  ＠  ／  ：  −  ＿
 * 
 */
static const NJ_UINT16 giji_katakana_tbl[NJ_YOMI_HIRA_MAX] = {
/* ァ ア          */ 0x30a1, 0x30a2,
/* ィ イ          */ 0x30a3, 0x30a4,
/* ゥ ウ          */ 0x30a5, 0x30a6,
/* ェ エ          */ 0x30a7, 0x30a8,
/* ォ オ          */ 0x30a9, 0x30aa,
/* カ ガ          */ 0x30ab, 0x30ac,
/* キ ギ          */ 0x30ad, 0x30ae,
/* ク グ          */ 0x30af, 0x30b0,
/* ケ ゲ          */ 0x30b1, 0x30b2,
/* コ ゴ          */ 0x30b3, 0x30b4,
/* サ ザ          */ 0x30b5, 0x30b6,
/* シ ジ          */ 0x30b7, 0x30b8,
/* ス ズ          */ 0x30b9, 0x30ba,
/* セ ゼ          */ 0x30bb, 0x30bc,
/* ソ ゾ          */ 0x30bd, 0x30be,
/* タ ダ          */ 0x30bf, 0x30c0,
/* チ ヂ          */ 0x30c1, 0x30c2,
/* ッ ツ ヅ       */ 0x30c3, 0x30c4, 0x30c5,
/* テ デ          */ 0x30c6, 0x30c7,
/* ト ド          */ 0x30c8, 0x30c9,
/* ナ ニ ヌ ネ ノ */ 0x30ca, 0x30cb, 0x30cc, 0x30cd, 0x30ce,
/* ハ バ パ       */ 0x30cf, 0x30d0, 0x30d1,
/* ヒ ビ ピ       */ 0x30d2, 0x30d3, 0x30d4,
/* フ ブ プ       */ 0x30d5, 0x30d6, 0x30d7,
/* ヘ ベ ペ       */ 0x30d8, 0x30d9, 0x30da,
/* ホ ボ ポ       */ 0x30db, 0x30dc, 0x30dd,
/* マ ミ ム メ モ */ 0x30de, 0x30df, 0x30e0, 0x30e1, 0x30e2,
/* ャ ヤ          */ 0x30e3, 0x30e4,
/* ュ ユ          */ 0x30e5, 0x30e6,
/* ョ ヨ          */ 0x30e7, 0x30e8,
/* ラ リ ル レ ロ */ 0x30e9, 0x30ea, 0x30eb, 0x30ec, 0x30ed,
/* ヮ ワ          */ 0x30ee, 0x30ef,
/* ゐ ゑ          */ 0x0000, 0x0000, /* 未対応とする */
/* ヲ ン          */ 0x30f2, 0x30f3
};

/** 全角カタカナ 記号 */
static const NJ_UINT16 giji_katakana_sym_tbl[NJ_YOMI_SYM_MAX] = {
/* 、 */ 0x3001, 
/* 。 */ 0x3002, 
/* ー */ 0x30fc, 
/* ・ */ 0x30fb, 
/* 〜 */ NJ_CHAR_WAVE_DASH_BIG, 
/* ！ */ 0xff01, 
/* ？ */ 0xff1f, 
/* − */ 0xff0d, 
/* ． */ 0xff0e, /**/
/* ／ */ 0xff0f, 
/* ： */ 0xff1a, 
/* ＠ */ 0xff20, 
/* ＿ */ 0xff3f, 
/* ， */ 0xff0c, 
/* ． */ 0xff0e, /**/
/* ゛ */ 0x309b, 
/* ゜ */ 0x309c, 
/* ＆ */ 0xff06, 
/* ＝ */ 0xff1d,
/* ヴ */ 0x30f4,
/* 　 */ 0x3000
};

/**
 * 半角カタカナ
 */
/*
 * 
 * ※このキーアサインを標準としている。(実際は半角)
 *   このアサインのうち、記号とそれ以外にテーブルを分割している。
 * 
 *     <toggle> 1   2   3   4   5   6   7   8   9  10  11
 * <KeyAssign>
 *   <  1 >    ア  イ  ウ  エ  オ  ァ  ィ  ゥ  ェ  ォ
 *   <  2 >    カ  キ  ク  ケ  コ
 *   <  3 >    サ  シ  ス  セ  ソ
 *   <  4 >    タ  チ  ツ  テ  ト  ッ
 *   <  5 >    ナ  ニ  ヌ  ネ  ノ
 *   <  6 >    ハ  ヒ  フ  ヘ  ホ
 *   <  7 >    マ  ミ  ム  メ  モ
 *   <  8 >    ヤ  ユ  ヨ  ャ  ュ  ョ
 *   <  9 >    ラ  リ  ル  レ  ロ
 *   <  0 >    ワ  ヲ  ン  ヮ  ー
 *   <  # >    、  。  ？  ！  ・  SP
 *   
 *   <  2 >    ガ  ギ  グ  ゲ  ゴ
 *   <  3 >    ザ  ジ  ズ  ゼ  ゾ
 *   <  4 >    ダ  ヂ  ヅ  デ  ド
 *   <  6 >    バ  ビ  ブ  ベ  ボ
 *   <  6 >    パ  ピ  プ  ペ  ポ
 * 
 *   <なし>    ．  ＠  ／  ：  −  ＿
 * 
 */
static const NJ_UINT16 giji_hankata_tbl[NJ_YOMI_HIRA_MAX][2] = {
/* ァ ア          */ {0xff67, 0x0000}, {0xff71, 0x0000},
/* ィ イ          */ {0xff68, 0x0000}, {0xff72, 0x0000},
/* ゥ ウ          */ {0xff69, 0x0000}, {0xff73, 0x0000},
/* ェ エ          */ {0xff6a, 0x0000}, {0xff74, 0x0000},
/* ォ オ          */ {0xff6b, 0x0000}, {0xff75, 0x0000},
/* カ ガ          */ {0xff76, 0x0000}, {0xff76, 0xff9e},
/* キ ギ          */ {0xff77, 0x0000}, {0xff77, 0xff9e},
/* ク グ          */ {0xff78, 0x0000}, {0xff78, 0xff9e},
/* ケ ゲ          */ {0xff79, 0x0000}, {0xff79, 0xff9e},
/* コ ゴ          */ {0xff7a, 0x0000}, {0xff7a, 0xff9e},
/* サ ザ          */ {0xff7b, 0x0000}, {0xff7b, 0xff9e},
/* シ ジ          */ {0xff7c, 0x0000}, {0xff7c, 0xff9e},
/* ス ズ          */ {0xff7d, 0x0000}, {0xff7d, 0xff9e},
/* セ ゼ          */ {0xff7e, 0x0000}, {0xff7e, 0xff9e},
/* ソ ゾ          */ {0xff7f, 0x0000}, {0xff7f, 0xff9e},
/* タ ダ          */ {0xff80, 0x0000}, {0xff80, 0xff9e},
/* チ ヂ          */ {0xff81, 0x0000}, {0xff81, 0xff9e},
/* ッ ツ ヅ       */ {0xff6f, 0x0000}, {0xff82, 0x0000}, {0xff82, 0xff9e},
/* テ デ          */ {0xff83, 0x0000}, {0xff83, 0xff9e},
/* ト ド          */ {0xff84, 0x0000}, {0xff84, 0xff9e},
/* ナ ニ ヌ ネ ノ */ {0xff85, 0x0000}, {0xff86, 0x0000}, {0xff87, 0x0000}, {0xff88, 0x0000}, {0xff89, 0x0000},
/* ハ バ パ       */ {0xff8a, 0x0000}, {0xff8a, 0xff9e}, {0xff8a, 0xff9f},
/* ヒ ビ ピ       */ {0xff8b, 0x0000}, {0xff8b, 0xff9e}, {0xff8b, 0xff9f},
/* フ ブ プ       */ {0xff8c, 0x0000}, {0xff8c, 0xff9e}, {0xff8c, 0xff9f},
/* ヘ ベ ペ       */ {0xff8d, 0x0000}, {0xff8d, 0xff9e}, {0xff8d, 0xff9f},
/* ホ ボ ポ       */ {0xff8e, 0x0000}, {0xff8e, 0xff9e}, {0xff8e, 0xff9f},
/* マ ミ ム メ モ */ {0xff8f, 0x0000}, {0xff90, 0x0000}, {0xff91, 0x0000}, {0xff92, 0x0000}, {0xff93, 0x0000},
/* ャ ヤ          */ {0xff6c, 0x0000}, {0xff94, 0x0000},
/* ュ ユ          */ {0xff6d, 0x0000}, {0xff95, 0x0000},
/* ョ ヨ          */ {0xff6e, 0x0000}, {0xff96, 0x0000},
/* ラ リ ル レ ロ */ {0xff97, 0x0000}, {0xff98, 0x0000}, {0xff99, 0x0000}, {0xff9a, 0x0000}, {0xff9b, 0x0000},
/* ヮ ワ          */ {0xff9c, 0x0000}, {0xff9c, 0x0000},
/* ゐ ゑ          */ {0x0000, 0x0000}, {0x0000, 0x0000},
/* ヲ ン          */ {0xff66, 0x0000}, {0xff9d, 0x0000}
};

/** 半角カタカナ 記号 */
static const NJ_UINT16 giji_hankata_sym_tbl[NJ_YOMI_SYM_MAX][2] = {
/* ,  */ {0x002c, 0x0000},
/* .  */ {0x002e, 0x0000},
/* ー */ {0xff70, 0x0000},
/*    */ {0x0000, 0x0000},
/* 〜 */ {NJ_CHAR_WAVE_DASH_SMALL, 0x0000},
/* ！ */ {0x0021, 0x0000},
/* ？ */ {0x003f, 0x0000},
/* − */ {0x002d, 0x0000},
/* ． */ {0x002e, 0x0000},
/* ／ */ {0x002f, 0x0000},
/* ： */ {0x003a, 0x0000},
/* ＠ */ {0x0040, 0x0000},
/* ＿ */ {0x005f, 0x0000},
/* ， */ {0x002c, 0x0000},
/* ． */ {0x002e, 0x0000},
/* ゛ */ {0xff9e, 0x0000},
/* ゜ */ {0xff9f, 0x0000},
/* ＆ */ {0x0026, 0x0000},
/* ＝ */ {0x003d, 0x0000},
/* ヴ */ {0xff73, 0xff9e},
/*    */ {0x0020, 0x0000}
};

/**
 * 全角英字 大文字
 */
/*
 * 
 * ※大文字、小文字ともに、このキーアサインを標準としている。
 * 
 *     <toggle> 1   2   3   4   5   6   7   8   9  10  11
 * <KeyAssign>
 *   <  1 >    ．  ＠  −  ＿  ／  ：  〜
 *   <  2 >    ａ  ｂ  ｃ
 *   <  3 >    ｄ  ｅ  ｆ
 *   <  4 >    ｇ  ｈ  ｉ
 *   <  5 >    ｊ  ｋ  ｌ
 *   <  6 >    ｍ  ｎ  ｏ
 *   <  7 >    ｐ  ｑ  ｒ  ｓ
 *   <  8 >    ｔ  ｕ  ｖ
 *   <  9 >    ｗ  ｘ  ｙ  ｚ
 *   <  0 >    
 *   <  # >    ，  ．  ？  ！  ・  SP
 *   
 *   <  2 >    ａ  ｂ  ｃ
 *   <  3 >    ｄ  ｅ  ｆ
 *   <  4 >    ｇ  ｈ  ｉ
 *   <  6 >    ｍ  ｎ  ｏ
 *   <  6 >    ｍ  ｎ  ｏ
 * 
 */
static const NJ_UINT16 giji_zen_eiji_upper_tbl[NJ_YOMI_HIRA_MAX] = {
/* ぁ あ          *//* ： ．       */ 0xff1a, 0xff0e,
/* ぃ い          *//* 〜 ＠       */ NJ_CHAR_WAVE_DASH_BIG, 0xff20,
/* ぅ う          *//*    −       */ 0x0000, 0xff0d,
/* ぇ え          *//*    ＿       */ 0x0000, 0xff3f,
/* ぉ お          *//*    ／       */ 0x0000, 0xff0f,
/* か が          *//* Ａ Ａ       */ 0xff21, 0xff21,
/* き ぎ          *//* Ｂ Ｂ       */ 0xff22, 0xff22,
/* く ぐ          *//* Ｃ Ｃ       */ 0xff23, 0xff23,
/* け げ          *//*             */ 0x0000, 0x0000,
/* こ ご          *//*             */ 0x0000, 0x0000,
/* さ ざ          *//* Ｄ Ｄ       */ 0xff24, 0xff24,
/* し じ          *//* Ｅ Ｅ       */ 0xff25, 0xff25,
/* す ず          *//* Ｆ Ｆ       */ 0xff26, 0xff26,
/* せ ぜ          *//*             */ 0x0000, 0x0000,
/* そ ぞ          *//*             */ 0x0000, 0x0000,
/* た だ          *//* Ｇ Ｇ       */ 0xff27, 0xff27,
/* ち ぢ          *//* Ｈ Ｈ       */ 0xff28, 0xff28,
/* っ つ づ       *//*    Ｉ Ｉ    */ 0x0000, 0xff29, 0xff29,
/* て で          *//*             */ 0x0000, 0x0000,
/* と ど          *//*             */ 0x0000, 0x0000,
/* な に ぬ ね の *//* Ｊ Ｋ Ｌ    */ 0xff2a, 0xff2b, 0xff2c, 0x0000, 0x0000,
/* は ば ぱ       *//* Ｍ Ｍ Ｍ    */ 0xff2d, 0xff2d, 0xff2d,
/* ひ び ぴ       *//* Ｎ Ｎ Ｎ    */ 0xff2e, 0xff2e, 0xff2e,
/* ふ ぶ ぷ       *//* Ｏ Ｏ Ｏ    */ 0xff2f, 0xff2f, 0xff2f,
/* へ べ ぺ       *//*             */ 0x0000, 0x0000, 0x0000,
/* ほ ぼ ぽ       *//*             */ 0x0000, 0x0000, 0x0000,
/* ま み む め も *//* Ｐ Ｑ Ｒ Ｓ */ 0xff30, 0xff31, 0xff32, 0xff33, 0x0000,
/* ゃ や          *//*    Ｔ       */ 0x0000, 0xff34,
/* ゅ ゆ          *//*    Ｕ       */ 0x0000, 0xff35,
/* ょ よ          *//*    Ｖ       */ 0x0000, 0xff36,
/* ら り る れ ろ *//* Ｗ Ｘ Ｙ Ｚ */ 0xff37, 0xff38, 0xff39, 0xff3a, 0x0000,
/* ゎ わ          *//*             */ 0x0000, 0x0000,
/* ゐ ゑ          *//*             */ 0x0000, 0x0000,
/* を ん          *//*             */ 0x0000, 0x0000
};

/**
 * 全角英字 小文字
 */
static const NJ_UINT16 giji_zen_eiji_lower_tbl[NJ_YOMI_HIRA_MAX] = {
/* ぁ あ          *//* ： ．          */ 0xff1a, 0xff0e,
/* ぃ い          *//* 〜 ＠          */ NJ_CHAR_WAVE_DASH_BIG, 0xff20,
/* ぅ う          *//*    −          */ 0x0000, 0xff0d,
/* ぇ え          *//*    ＿          */ 0x0000, 0xff3f,
/* ぉ お          *//*    ／          */ 0x0000, 0xff0f,
/* か が          *//* ａ ａ          */ 0xff41, 0xff41,
/* き ぎ          *//* ｂ ｂ          */ 0xff42, 0xff42,
/* く ぐ          *//* ｃ ｃ          */ 0xff43, 0xff43,
/* け げ          *//*                */ 0x0000, 0x0000,
/* こ ご          *//*                */ 0x0000, 0x0000,
/* さ ざ          *//* ｄ ｄ          */ 0xff44, 0xff44,
/* し じ          *//* ｅ ｅ          */ 0xff45, 0xff45,
/* す ず          *//* ｆ ｆ          */ 0xff46, 0xff46,
/* せ ぜ          *//*                */ 0x0000, 0x0000,
/* そ ぞ          *//*                */ 0x0000, 0x0000,
/* た だ          *//* ｇ ｇ          */ 0xff47, 0xff47,
/* ち ぢ          *//* ｈ ｈ          */ 0xff48, 0xff48,
/* っ つ づ       *//*    ｉ ｉ       */ 0x0000, 0xff49, 0xff49,
/* て で          *//*                */ 0x0000, 0x0000,
/* と ど          *//*                */ 0x0000, 0x0000,
/* な に ぬ ね の *//* ｊ ｋ ｌ       */ 0xff4a, 0xff4b, 0xff4c, 0x0000, 0x0000,
/* は ば ぱ       *//* ｍ ｍ ｍ       */ 0xff4d, 0xff4d, 0xff4d,
/* ひ び ぴ       *//* ｎ ｎ ｎ       */ 0xff4e, 0xff4e, 0xff4e,
/* ふ ぶ ぷ       *//* ｏ ｏ ｏ       */ 0xff4f, 0xff4f, 0xff4f,
/* へ べ ぺ       *//*                */ 0x0000, 0x0000, 0x0000,
/* ほ ぼ ぽ       *//*                */ 0x0000, 0x0000, 0x0000,
/* ま み む め も *//* ｐ ｑ ｒ ｓ    */ 0xff50, 0xff51, 0xff52, 0xff53, 0x0000,
/* ゃ や          *//*    ｔ          */ 0x0000, 0xff54,
/* ゅ ゆ          *//*    ｕ          */ 0x0000, 0xff55,
/* ょ よ          *//*    ｖ          */ 0x0000, 0xff56,
/* ら り る れ ろ *//* ｗ ｘ ｙ ｚ    */ 0xff57, 0xff58, 0xff59, 0xff5a, 0x0000,
/* ゎ わ          *//*                */ 0x0000, 0x0000,
/* ゐ ゑ          *//*                */ 0x0000, 0x0000,
/* を ん          *//*                */ 0x0000, 0x0000
};

/**
 * 半角英字 大文字
 */
/*
 * 
 * ※大文字、小文字ともに、このキーアサインを標準としている。
 * 
 *     <toggle> 1   2   3   4   5   6   7   8   9  10  11
 * <KeyAssign>
 *   <  1 >     .   @   -   _   /   :   ~
 *   <  2 >     a   b   c
 *   <  3 >     d   e   f
 *   <  4 >     g   h   i
 *   <  5 >     j   k   l
 *   <  6 >     m   n   o
 *   <  7 >     p   q   r   s
 *   <  8 >     t   u   f
 *   <  9 >     w   x   y   z
 *   <  0 >    
 *   <  # >    ,  .  ？  ！  ・  SP
 *   
 *   <  2 >     a   b   c
 *   <  3 >     d   e   f
 *   <  4 >     g   h   i
 *   <  6 >     m   n   o
 *   <  6 >     m   n   o
 */
static const NJ_UINT16 giji_han_eiji_upper_tbl[NJ_YOMI_HIRA_MAX] = {
/* ぁ あ          *//* : .．     */ 0x003a, 0x002e,
/* ぃ い          *//* ~ @       */ NJ_CHAR_WAVE_DASH_SMALL, 0x0040,
/* ぅ う          *//*   -       */ 0x0000, 0x002d,
/* ぇ え          *//*   _       */ 0x0000, 0x005f,
/* ぉ お          *//*   /       */ 0x0000, 0x002f,
/* か が          *//* A A       */ 0x0041, 0x0041,
/* き ぎ          *//* B B       */ 0x0042, 0x0042,
/* く ぐ          *//* C C       */ 0x0043, 0x0043,
/* け げ          *//*           */ 0x0000, 0x0000,
/* こ ご          *//*           */ 0x0000, 0x0000,
/* さ ざ          *//* D D       */ 0x0044, 0x0044,
/* し じ          *//* E E       */ 0x0045, 0x0045,
/* す ず          *//* F F       */ 0x0046, 0x0046,
/* せ ぜ          *//*           */ 0x0000, 0x0000,
/* そ ぞ          *//*           */ 0x0000, 0x0000,
/* た だ          *//* G G       */ 0x0047, 0x0047,
/* ち ぢ          *//* H H       */ 0x0048, 0x0048,
/* っ つ づ       *//*    I I    */ 0x0000, 0x0049, 0x0049,
/* て で          *//*           */ 0x0000, 0x0000,
/* と ど          *//*           */ 0x0000, 0x0000,
/* な に ぬ ね の *//* J K L     */ 0x004a, 0x004b, 0x004c, 0x0000, 0x0000,
/* は ば ぱ       *//* M M M     */ 0x004d, 0x004d, 0x004d,
/* ひ び ぴ       *//* N N N     */ 0x004e, 0x004e, 0x004e,
/* ふ ぶ ぷ       *//* O O O     */ 0x004f, 0x004f, 0x004f,
/* へ べ ぺ       *//*           */ 0x0000, 0x0000, 0x0000,
/* ほ ぼ ぽ       *//*           */ 0x0000, 0x0000, 0x0000,
/* ま み む め も *//* P Q R S   */ 0x0050, 0x0051, 0x0052, 0x0053, 0x0000,
/* ゃ や          *//*   T       */ 0x0000, 0x0054,
/* ゅ ゆ          *//*   U       */ 0x0000, 0x0055,
/* ょ よ          *//*   V       */ 0x0000, 0x0056,
/* ら り る れ ろ *//* W X Y Z   */ 0x0057, 0x0058, 0x0059, 0x005a, 0x0000,
/* ゎ わ          *//*           */ 0x0000, 0x0000,
/* ゐ ゑ          *//*           */ 0x0000, 0x0000,
/* を ん          *//*           */ 0x0000, 0x0000
};

/**
 * 半角英字 小文字
 */
static const NJ_UINT16 giji_han_eiji_lower_tbl[NJ_YOMI_HIRA_MAX] = {
/* ぁ あ          *//* : .．     */ 0x003a, 0x002e,
/* ぃ い          *//* ~ @       */ NJ_CHAR_WAVE_DASH_SMALL, 0x0040,
/* ぅ う          *//*   -       */ 0x0000, 0x002d,
/* ぇ え          *//*   _       */ 0x0000, 0x005f,
/* ぉ お          *//*   /       */ 0x0000, 0x002f,
/* か が          *//* a a       */ 0x0061, 0x0061,
/* き ぎ          *//* b b       */ 0x0062, 0x0062,
/* く ぐ          *//* c c       */ 0x0063, 0x0063,
/* け げ          *//*           */ 0x0000, 0x0000,
/* こ ご          *//*           */ 0x0000, 0x0000,
/* さ ざ          *//* d d       */ 0x0064, 0x0064,
/* し じ          *//* e e       */ 0x0065, 0x0065,
/* す ず          *//* f f       */ 0x0066, 0x0066,
/* せ ぜ          *//*           */ 0x0000, 0x0000,
/* そ ぞ          *//*           */ 0x0000, 0x0000,
/* た だ          *//* g g       */ 0x0067, 0x0067,
/* ち ぢ          *//* h h       */ 0x0068, 0x0068,
/* っ つ づ       *//*    i i    */ 0x0000, 0x0069, 0x0069,
/* て で          *//*           */ 0x0000, 0x0000,
/* と ど          *//*           */ 0x0000, 0x0000,
/* な に ぬ ね の *//* j k l     */ 0x006a, 0x006b, 0x006c, 0x0000, 0x0000,
/* は ば ぱ       *//* m m m     */ 0x006d, 0x006d, 0x006d,
/* ひ び ぴ       *//* n n n     */ 0x006e, 0x006e, 0x006e,
/* ふ ぶ ぷ       *//* o o o     */ 0x006f, 0x006f, 0x006f,
/* へ べ ぺ       *//*           */ 0x0000, 0x0000, 0x0000,
/* ほ ぼ ぽ       *//*           */ 0x0000, 0x0000, 0x0000,
/* ま み む め も *//* p q r s   */ 0x0070, 0x0071, 0x0072, 0x0073, 0x0000,
/* ゃ や          *//*   t       */ 0x0000, 0x0074,
/* ゅ ゆ          *//*   u       */ 0x0000, 0x0075,
/* ょ よ          *//*   v       */ 0x0000, 0x0076,
/* ら り る れ ろ *//* w x y z   */ 0x0077, 0x0078, 0x0079, 0x007a, 0x0000,
/* ゎ わ          *//*           */ 0x0000, 0x0000,
/* ゐ ゑ          *//*           */ 0x0000, 0x0000,
/* を ん          *//*           */ 0x0000, 0x0000
};

/**
 * 半角数字、全角数字、漢数字(全角)
 */
static const NJ_UINT16 giji_suuji_tbl[NJG_SUUJI_TBL_NO][NJG_SUUJI_REC] = {
/* 半角数字 */
/* 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 */
{0x0030, 0x0031, 0x0032, 0x0033, 0x0034,
 0x0035, 0x0036, 0x0037, 0x0038, 0x0039,
 0x0000,
 0x0000},

/* 全角数字 */
/* ０, １, ２, ３, ４, ５, ６, ７, ８, ９ */
{0xff10, 0xff11, 0xff12, 0xff13, 0xff14,
 0xff15, 0xff16, 0xff17, 0xff18, 0xff19,
 0x0000,
 0x0000},

/* 漢数字(全角) 必ず2byte文字を指定すること */
{0x3007, 0x4e00, 0x4e8c, 0x4e09, 0x56db,
 0x4e94, 0x516d, 0x4e03, 0x516b, 0x4e5d,
 0x0000,
 0x0000},

/* 漢数字(大字) */
/* 零(未使用), 壱, 弐, 参, 四, 五, 六, 七, 八, 九 */
{0x96F6, 0x58F1, 0x5F10, 0x53C2, 0x56DB,
 0x4E94, 0x516D, 0x4E03, 0x516B, 0x4E5D,
 0x0000,
 0x0000}
};


/**
 * 漢数字(位)
 *
 * @attention 必ず2byte文字を指定すること
 */
static const NJ_UINT16 kurai_ksuuji_table[NJ_KURAI_TABLE_MAX] = {
/* 十 */ 0x5341,
/* 百 */ 0x767e,
/* 千 */ 0x5343,
/* 万 */ 0x4e07,
/* 億 */ 0x5104
};

/**
 * 漢数字
 *
 * 読み文字と漢数字を定義する。
 * @attention  必ず読みで降順ソートしておくこと。
 */
static const NJ_SUUJIYOMITBL giji_suuji_yomi_tbl[] = {
    {{ 0x308d, 0x3063, 0x0000 }, 2, 6},             /* ろっ */
    {{ 0x308d, 0x304f, 0x0000 }, 2, 6},             /* ろく */
    {{ 0x3088, 0x3093, 0x0000 }, 2, 4},             /* よん */
    {{ 0x307e, 0x3093, 0x0000 }, 2, NJ_KETA_MAN},   /* まん */
    {{ 0x3074, 0x3083, 0x304f }, 3, NJ_KETA_HYAKU}, /* ぴゃく */
    {{ 0x3073, 0x3083, 0x304f }, 3, NJ_KETA_HYAKU}, /* びゃく */
    {{ 0x3072, 0x3083, 0x3063 }, 3, NJ_KETA_HYAKU}, /* ひゃっ */
    {{ 0x3072, 0x3083, 0x304f }, 3, NJ_KETA_HYAKU}, /* ひゃく */
    {{ 0x306f, 0x3063, 0x0000 }, 2, 8},             /* はっ */
    {{ 0x306f, 0x3061, 0x0000 }, 2, 8},             /* はち */
    {{ 0x306b, 0x0000, 0x0000 }, 1, 2},             /* に */
    {{ 0x306a, 0x306a, 0x0000 }, 2, 7},             /* なな */

    {{ 0x305c, 0x3093, 0x0000 }, 2, NJ_KETA_ZEN},   /* ぜん */
    {{ 0x305b, 0x3093, 0x0000 }, 2, NJ_KETA_SEN},   /* せん */
    {{ 0x3058, 0x3085, 0x3063 }, 3, NJ_KETA_JYUU},  /* じゅっ */
    {{ 0x3058, 0x3085, 0x3046 }, 3, NJ_KETA_JYUU},  /* じゅう */
    {{ 0x3057, 0x3061, 0x0000 }, 2, 7},             /* しち */
    {{ 0x3055, 0x3093, 0x0000 }, 2, 3},             /* さん */
    {{ 0x3054, 0x0000, 0x0000 }, 1, 5},             /* ご */
    {{ 0x304d, 0x3085, 0x3046 }, 3, 9},             /* きゅう */
    {{ 0x304a, 0x304f, 0x0000 }, 2, NJ_KETA_OKU},   /* おく */
    {{ 0x3044, 0x3063, 0x0000 }, 2, 1},             /* いっ */
    {{ 0x3044, 0x3061, 0x0000 }, 2, 1},             /* いち */
    {{ 0x0000, 0x0000, 0x0000 }, 0, 0}
};

/**
 * 時間・日付擬似候補変換テーブル定義
 *
 *  - 時間、日付識別コード
 *  - 有効桁(文字)数 (0は無制限)
 *  - モード種別(チェックで使用する)
 *  - 擬似候補の途中に単位を格納する桁の位置
 *  - 単位文字コードの全バイト数
 *  - 擬似候補の途中に格納する単位の文字コード
 *  - 最後に格納する単位の文字コード
 */
static const NJ_CONVERT_TBL  giji_conv_table[] = {
    /* Time */
    { 0, 0x0000, 0x6642, 2 }, /* NJG_TBL_NO_THH *//* HH時 */
    { 0, 0x0000, 0x5206, 2 }, /* NJG_TBL_NO_TMM *//* MM分 */
    { 1, 0x6642, 0x5206, 4 }, /* NJG_TBL_NO_THM *//* H時M分 */
    { 1, 0x6642, 0x5206, 4 }, /* NJG_TBL_NO_THMM *//* H時MM分 */
    { 2, 0x6642, 0x5206, 4 }, /* NJG_TBL_NO_THHM *//* HH時M分 */
    { 2, 0x6642, 0x5206, 4 }, /* NJG_TBL_NO_THHMM *//* HH時MM分 */
    { 1, 0x003A, 0x0000, 2 }, /* NJG_TBL_NO_THMM_SYM *//* H:MM */
    { 2, 0x003A, 0x0000, 2 }, /* NJG_TBL_NO_THHMM_SYM *//* HH:MM */
    /* Date */
    { 0, 0x0000, 0x5e74, 2 }, /* NJG_TBL_NO_YYYY *//* YYYY年 */
    { 0, 0x0000, 0x6708, 2 }, /* NJG_TBL_NO_DMM *//* MM月 */
    { 0, 0x0000, 0x65E5, 2 }, /* NJG_TBL_NO_DDD *//* DD日 */
    { 1, 0x6708, 0x65E5, 4 }, /* NJG_TBL_NO_DMD *//* M月D日 */
    { 1, 0x6708, 0x65E5, 4 }, /* NJG_TBL_NO_DMDD *//* M月DD日 */
    { 2, 0x6708, 0x65E5, 4 }, /* NJG_TBL_NO_DMMD *//* MM月D日 */
    { 2, 0x6708, 0x65E5, 4 }, /* NJG_TBL_NO_DMMDD *//* MM月DD日 */
    { 1, 0x002F, 0x0000, 2 }, /* NJG_TBL_NO_DMD_SYM *//* M/D */
    { 1, 0x002F, 0x0000, 2 }, /* NJG_TBL_NO_DMDD_SYM *//* M/DD */
    { 2, 0x002F, 0x0000, 2 }, /* NJG_TBL_NO_DMMD_SYM *//* MM/D */
    { 2, 0x002F, 0x0000, 2 }, /* NJG_TBL_NO_DMMDD_SYM *//* MM/DD */
    {0,0,0,0}
};

/**
 * 月末日指定テーブル定義
 *
 *  - 各月の最後の日付をテーブルに格納
 *  - 先頭から順に、1月、2月、...、12月となっている(2月は29日を格納)
 */
static const NJ_UINT8 giji_last_day[12] = {
    31, /*  1月 */
    29, /*  2月 */
    31, /*  3月 */
    30, /*  4月 */
    31, /*  5月 */
    30, /*  6月 */
    31, /*  7月 */
    31, /*  8月 */
    30, /*  9月 */
    31, /* 10月 */
    30, /* 11月 */
    31  /* 12月 */
};


#define NJ_TYPE_INPUT_LEN                       12 /* 読み文字列長 */

/* 擬似候補作成分類定義*/
#define NJGC_TYPE_INIT                           0 /* 初期     */
#define NJGC_TYPE_YYYY                           1 /* 年表示   */
#define NJGC_TYPE_MM                             2 /* 月表示   */
#define NJGC_TYPE_MMDD                           3 /* 日付表示 */
#define NJGC_TYPE_HHMM                           4 /* 時間表示 */
#define NJGC_TYPE_WEEK                           5 /* 曜日表示 */

/* 変換対象文字列判定用テーブル */
typedef struct {
    NJ_UINT8  input[NJ_TYPE_INPUT_LEN]; /* 変換対象文字列        */
    NJ_UINT8  input_size;               /* 変換対象文字列長      */
    NJ_INT8   adjust;                   /* 調整時間              */
    NJ_UINT8  gctype;                   /* 擬似候補作成分類定義  */
} NJ_YOMI2TYPE_TBL;

/* 擬似候補作成テーブル */
typedef struct {
    NJ_UINT8  gtype;                    /* 擬似候補タイプ        */
    NJ_UINT8  gctype;                   /* 擬似候補作成分類定義  */
    NJ_UINT16 f_code;                   /* 時、月、年表示用コード*/
    NJ_INT16  f_code_size;              /* 表示用コードのバイト数*/
    NJ_UINT16 s_code;                   /* 分、日表示用コード    */
    NJ_INT16  s_code_size;              /* 表示用コードのバイト数*/
} NJ_TYPE2CONVERT_TBL;

static const NJ_YOMI2TYPE_TBL giji_yomi_2_type_tbl[] = {
/*     [ 0]  [ 1]  [ 2]  [ 3]  [ 4]  [ 5]  [ 6]  [ 7]  [ 8]  [ 9]  [10]  [11] */
    {{ 0x30, 0x4a, 0x30, 0x68, 0x30, 0x68, 0x30, 0x57, 0x00, 0x00, 0x00, 0x00 },  8,   -2, NJGC_TYPE_YYYY   },      /* おととし     */
    {{ 0x30, 0x4d, 0x30, 0x87, 0x30, 0x6d, 0x30, 0x93, 0x00, 0x00, 0x00, 0x00 },  8,   -1, NJGC_TYPE_YYYY   },      /* きょねん     */
    {{ 0x30, 0x53, 0x30, 0x68, 0x30, 0x57, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },  6,    0, NJGC_TYPE_YYYY   },      /* ことし       */
    {{ 0x30, 0x89, 0x30, 0x44, 0x30, 0x6d, 0x30, 0x93, 0x00, 0x00, 0x00, 0x00 },  8,    1, NJGC_TYPE_YYYY   },      /* らいねん     */
    {{ 0x30, 0x55, 0x30, 0x89, 0x30, 0x44, 0x30, 0x6d, 0x30, 0x93, 0x00, 0x00 }, 10,    2, NJGC_TYPE_YYYY   },      /* さらいねん   */
    {{ 0x30, 0x5b, 0x30, 0x93, 0x30, 0x5b, 0x30, 0x93, 0x30, 0x52, 0x30, 0x64 }, 12,   -2, NJGC_TYPE_MM     },      /* せんせんげつ */
    {{ 0x30, 0x5b, 0x30, 0x93, 0x30, 0x52, 0x30, 0x64, 0x00, 0x00, 0x00, 0x00 },  8,   -1, NJGC_TYPE_MM     },      /* せんげつ     */
    {{ 0x30, 0x53, 0x30, 0x93, 0x30, 0x52, 0x30, 0x64, 0x00, 0x00, 0x00, 0x00 },  8,    0, NJGC_TYPE_MM     },      /* こんげつ     */
    {{ 0x30, 0x89, 0x30, 0x44, 0x30, 0x52, 0x30, 0x64, 0x00, 0x00, 0x00, 0x00 },  8,    1, NJGC_TYPE_MM     },      /* らいげつ     */
    {{ 0x30, 0x55, 0x30, 0x89, 0x30, 0x44, 0x30, 0x52, 0x30, 0x64, 0x00, 0x00 }, 10,    2, NJGC_TYPE_MM     },      /* さらいげつ   */
    {{ 0x30, 0x55, 0x30, 0x4d, 0x30, 0x4a, 0x30, 0x68, 0x30, 0x64, 0x30, 0x44 }, 12,   -3, NJGC_TYPE_MMDD   },      /* さきおとつい */
    {{ 0x30, 0x55, 0x30, 0x4d, 0x30, 0x4a, 0x30, 0x68, 0x30, 0x68, 0x30, 0x44 }, 12,   -3, NJGC_TYPE_MMDD   },      /* さきおととい */
    {{ 0x30, 0x4a, 0x30, 0x68, 0x30, 0x64, 0x30, 0x44, 0x00, 0x00, 0x00, 0x00 },  8,   -2, NJGC_TYPE_MMDD   },      /* おとつい     */
    {{ 0x30, 0x4a, 0x30, 0x68, 0x30, 0x68, 0x30, 0x44, 0x00, 0x00, 0x00, 0x00 },  8,   -2, NJGC_TYPE_MMDD   },      /* おととい     */
    {{ 0x30, 0x4d, 0x30, 0x6e, 0x30, 0x46, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },  6,   -1, NJGC_TYPE_MMDD   },      /* きのう       */
    {{ 0x30, 0x4d, 0x30, 0x87, 0x30, 0x46, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },  6,    0, NJGC_TYPE_MMDD   },      /* きょう       */
    {{ 0x30, 0x72, 0x30, 0x65, 0x30, 0x51, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },  6,    0, NJGC_TYPE_MMDD   },      /* ひづけ       */
    {{ 0x30, 0x42, 0x30, 0x57, 0x30, 0x5f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },  6,    1, NJGC_TYPE_MMDD   },      /* あした       */
    {{ 0x30, 0x42, 0x30, 0x59, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },  4,    1, NJGC_TYPE_MMDD   },      /* あす         */
    {{ 0x30, 0x42, 0x30, 0x55, 0x30, 0x63, 0x30, 0x66, 0x00, 0x00, 0x00, 0x00 },  8,    2, NJGC_TYPE_MMDD   },      /* あさって     */
    {{ 0x30, 0x57, 0x30, 0x42, 0x30, 0x55, 0x30, 0x63, 0x30, 0x66, 0x00, 0x00 }, 10,    3, NJGC_TYPE_MMDD   },      /* しあさって   */
    {{ 0x30, 0x44, 0x30, 0x7e, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },  4,    0, NJGC_TYPE_HHMM   },      /* いま         */
    {{ 0x30, 0x58, 0x30, 0x4B, 0x30, 0x93, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },  6,    0, NJGC_TYPE_HHMM   },      /* じかん       */
    {{ 0x30, 0x88, 0x30, 0x46, 0x30, 0x73, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },  6,    0, NJGC_TYPE_WEEK   },      /* ようび       */
    {{ 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },  0xFF, 0, 0xFF             }       /* 終端         */
};

static const NJ_TYPE2CONVERT_TBL  giji_conv_tbl[] = {
    /* Time & Date */
    { NJ_TYPE_HAN_YOMI2DATE_YYYY,           NJGC_TYPE_YYYY, 0x5E74, 2, 0x0000, 0 }, /* YYYY年             */
    { NJ_TYPE_HAN_YOMI2DATE_NENGO,          NJGC_TYPE_YYYY, 0x5E74, 2, 0x0000, 0 }, /* 平成YY年           */
    { NJ_TYPE_HAN_YOMI2DATE_MM,             NJGC_TYPE_MM,   0x6708, 2, 0x0000, 0 }, /* MM月               */
    { NJ_TYPE_HAN_YOMI2DATE_MMDD,           NJGC_TYPE_MMDD, 0x6708, 2, 0x65E5, 2 }, /* MM月DD日           */
    { NJ_TYPE_HAN_YOMI2DATE_MMDD_WEEK,      NJGC_TYPE_MMDD, 0x6708, 2, 0x65E5, 2 }, /* MM月DD日(曜日)     */
    { NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM,       NJGC_TYPE_MMDD, 0x002F, 2, 0x0000, 0 }, /* MM/DD              */
    { NJ_TYPE_HAN_YOMI2DATE_MMDD_SYM_WEEK,  NJGC_TYPE_MMDD, 0x002F, 2, 0x0000, 0 }, /* MM/DD(曜日)        */
    { NJ_TYPE_HAN_YOMI2TIME_HHMM,           NJGC_TYPE_HHMM, 0x6642, 2, 0x5206, 2 }, /* HH時MM分           */
    { NJ_TYPE_HAN_YOMI2TIME_HHMM_12H,       NJGC_TYPE_HHMM, 0x6642, 2, 0x5206, 2 }, /* 午前(午後)HH時MM分 */
    { NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM,       NJGC_TYPE_HHMM, 0x003A, 2, 0x0000, 0 }, /* HH:MM              */
    { NJ_TYPE_HAN_YOMI2TIME_HHMM_SYM_AMPM,  NJGC_TYPE_HHMM, 0x003A, 2, 0x0000, 0 }, /* AM(PM)HH:MM        */
    { NJ_TYPE_DAY_OF_WEEK,                  NJGC_TYPE_WEEK, 0x0000, 0, 0x0000, 0 }, /* X曜日              */
    { NJ_TYPE_DAY_OF_WEEK_SYM,              NJGC_TYPE_WEEK, 0x0000, 0, 0x0000, 0 }, /* (曜日)             */
    { 0, 0xFF, 0,0,0,0}
};

/**
 * 月末日指定テーブル定義
 *
 *   ・各月の最後の日付をテーブルに格納
 *   ・先頭から順に、1月、2月、...、12月
 *     となっている
 *     (2月は29日を格納)
 */
static const NJ_UINT8 giji_month_days[12] = {
    31, /*  1月 */
    28, /*  2月 */
    31, /*  3月 */
    30, /*  4月 */
    31, /*  5月 */
    30, /*  6月 */
    31, /*  7月 */
    31, /*  8月 */
    30, /*  9月 */
    31, /* 10月 */
    30, /* 11月 */
    31  /* 12月 */
};

static const NJ_UINT16 giji_string_am_len = 4;
static const NJ_UINT8  giji_string_am[] = { 0x53, 0x48, 0x52, 0x4D };
static const NJ_UINT16 giji_string_pm_len = 4;
static const NJ_UINT8  giji_string_pm[] = { 0x53, 0x48, 0x5F, 0x8C };
static const NJ_UINT16 giji_string_am_en_len = 4;
static const NJ_UINT8  giji_string_am_en[] = { 0x00, 0x41, 0x00, 0x4D };
static const NJ_UINT16 giji_string_pm_en_len = 4;
static const NJ_UINT8  giji_string_pm_en[] = { 0x00, 0x50, 0x00, 0x4D };

static const NJ_UINT16 giji_string_showa_len = 4;
static const NJ_UINT8  giji_string_showa[] = { 0x66, 0x2d, 0x54, 0x8c };
static const NJ_UINT16 giji_string_heisei_len = 4;
static const NJ_UINT8  giji_string_heisei[] = { 0x5e, 0x73, 0x62, 0x10 };
static const NJ_UINT16 giji_string_gannen_len = 4;
static const NJ_UINT8  giji_string_gannen[] = { 0x51, 0x43, 0x5e, 0x74 };

#define GIJI_STRING_DAYOFWEEK_LEN    6    /* (曜日)配列要素長 */
static const NJ_UINT8 giji_string_dayofweek_tbl[NJ_DAYOFWEEK_TABLE_MAX][7] = {
/* (日) */ { 0x00, 0x28, 0x65, 0xE5, 0x00, 0x29, 0x00 },
/* (月) */ { 0x00, 0x28, 0x67, 0x08, 0x00, 0x29, 0x00 },
/* (火) */ { 0x00, 0x28, 0x70, 0x6B, 0x00, 0x29, 0x00 },
/* (水) */ { 0x00, 0x28, 0x6C, 0x34, 0x00, 0x29, 0x00 },
/* (木) */ { 0x00, 0x28, 0x67, 0x28, 0x00, 0x29, 0x00 },
/* (金) */ { 0x00, 0x28, 0x91, 0xD1, 0x00, 0x29, 0x00 },
/* (土) */ { 0x00, 0x28, 0x57, 0x1F, 0x00, 0x29, 0x00 }
};

#define GIJI_STRING_DAYOFWEEK_KANJI_LEN    6    /* X曜日配列要素長 */
static const NJ_UINT8 giji_string_dayofweek_kanji_tbl[NJ_DAYOFWEEK_TABLE_MAX][7] = {
/* 日曜日 */ { 0x65, 0xE5, 0x66, 0xDC, 0x65, 0xE5, 0x00 },
/* 月曜日 */ { 0x67, 0x08, 0x66, 0xDC, 0x65, 0xE5, 0x00 },
/* 火曜日 */ { 0x70, 0x6B, 0x66, 0xDC, 0x65, 0xE5, 0x00 },
/* 水曜日 */ { 0x6C, 0x34, 0x66, 0xDC, 0x65, 0xE5, 0x00 },
/* 木曜日 */ { 0x67, 0x28, 0x66, 0xDC, 0x65, 0xE5, 0x00 },
/* 金曜日 */ { 0x91, 0xD1, 0x66, 0xDC, 0x65, 0xE5, 0x00 },
/* 土曜日 */ { 0x57, 0x1F, 0x66, 0xDC, 0x65, 0xE5, 0x00 }
};

static const NJ_UINT16 giji_zen_2touch_yomi_tbl[NJ_ROMA_2TOUCH_YOMI_TABLE_MAX] = {
/* SP 、 。 ー ！ */ 0x3000, 0x3001, 0x3002, 0x30FC, 0xFF01,
/* ＃ ＆ ＊ ／ ０ */ 0xFF03, 0xFF06, 0xFF0A, 0xFF0F, 0xFF10,
/* １ ２ ３ ４ ５ */ 0xFF11, 0xFF12, 0xFF13, 0xFF14, 0xFF15,
/* ６ ７ ８ ９ ？ */ 0xFF16, 0xFF17, 0xFF18, 0xFF19, 0xFF1F,
/* Ａ Ｂ Ｃ Ｄ Ｅ */ 0xFF21, 0xFF22, 0xFF23, 0xFF24, 0xFF25,
/* Ｆ Ｇ Ｈ Ｉ Ｊ */ 0xFF26, 0xFF27, 0xFF28, 0xFF29, 0xFF2A,
/* Ｋ Ｌ Ｍ Ｎ Ｏ */ 0xFF2B, 0xFF2C, 0xFF2D, 0xFF2E, 0xFF2F,
/* Ｐ Ｑ Ｒ Ｓ Ｔ */ 0xFF30, 0xFF31, 0xFF32, 0xFF33, 0xFF34,
/* Ｕ Ｖ Ｗ Ｘ Ｙ */ 0xFF35, 0xFF36, 0xFF37, 0xFF38, 0xFF39,
/* Ｚ ａ ｂ ｃ ｄ */ 0xFF3A, 0xFF41, 0xFF42, 0xFF43, 0xFF44,
/* ｅ ｆ ｇ ｈ ｉ */ 0xFF45, 0xFF46, 0xFF47, 0xFF48, 0xFF49,
/* ｊ ｋ ｌ ｍ ｎ */ 0xFF4A, 0xFF4B, 0xFF4C, 0xFF4D, 0xFF4E,
/* ｏ ｐ ｑ ｒ ｓ */ 0xFF4F, 0xFF50, 0xFF51, 0xFF52, 0xFF53,
/* ｔ ｕ ｖ ｗ ｘ */ 0xFF54, 0xFF55, 0xFF56, 0xFF57, 0xFF58,
/* ｙ ｚ ¥       */ 0xFF59, 0xFF5A, 0xFFE5,
/* ぁ あ          */ 0x3041, 0x3042,
/* ぃ い          */ 0x3043, 0x3044,
/* ぅ う          */ 0x3045, 0x3046,
/* ぇ え          */ 0x3047, 0x3048,
/* ぉ お          */ 0x3049, 0x304a,
/* か が          */ 0x304b, 0x304c,
/* き ぎ          */ 0x304d, 0x304e,
/* く ぐ          */ 0x304f, 0x3050,
/* け げ          */ 0x3051, 0x3052,
/* こ ご          */ 0x3053, 0x3054,
/* さ ざ          */ 0x3055, 0x3056,
/* し じ          */ 0x3057, 0x3058,
/* す ず          */ 0x3059, 0x305a,
/* せ ぜ          */ 0x305b, 0x305c,
/* そ ぞ          */ 0x305d, 0x305e,
/* た だ          */ 0x305f, 0x3060,
/* ち ぢ          */ 0x3061, 0x3062,
/* っ つ づ       */ 0x3063, 0x3064, 0x3065,
/* て で          */ 0x3066, 0x3067,
/* と ど          */ 0x3068, 0x3069,
/* な に ぬ ね の */ 0x306a, 0x306b, 0x306c, 0x306d, 0x306e,
/* は ば ぱ       */ 0x306f, 0x3070, 0x3071,
/* ひ び ぴ       */ 0x3072, 0x3073, 0x3074,
/* ふ ぶ ぷ       */ 0x3075, 0x3076, 0x3077,
/* へ べ ぺ       */ 0x3078, 0x3079, 0x307a,
/* ほ ぼ ぽ       */ 0x307b, 0x307c, 0x307d,
/* ま み む め も */ 0x307e, 0x307f, 0x3080, 0x3081, 0x3082,
/* ゃ や          */ 0x3083, 0x3084,
/* ゅ ゆ          */ 0x3085, 0x3086,
/* ょ よ          */ 0x3087, 0x3088,
/* ら り る れ ろ */ 0x3089, 0x308a, 0x308b, 0x308c, 0x308d,
/* ゎ わ          */ 0x308e, 0x308f,
/* を ん          */ 0x3092, 0x3093,
/* ゛ ゜          */ 0x309B, 0x309C
};

static const NJ_UINT16 giji_zen_eiji_2touch_tbl[NJ_ROMA_2TOUCH_TABLE_MAX] = {
/* SP 、 。 ー ！ */ 0x3000, 0x3001, 0x3002, 0x30FC, 0xFF01,
/* ＃ ＆ ＊ ／ ０ */ 0xFF03, 0xFF06, 0xFF0A, 0xFF0F, 0xFF10,
/* １ ２ ３ ４ ５ */ 0xFF11, 0xFF12, 0xFF13, 0xFF14, 0xFF15,
/* ６ ７ ８ ９ ？ */ 0xFF16, 0xFF17, 0xFF18, 0xFF19, 0xFF1F,
/* Ａ Ｂ Ｃ Ｄ Ｅ */ 0xFF21, 0xFF22, 0xFF23, 0xFF24, 0xFF25,
/* Ｆ Ｇ Ｈ Ｉ Ｊ */ 0xFF26, 0xFF27, 0xFF28, 0xFF29, 0xFF2A,
/* Ｋ Ｌ Ｍ Ｎ Ｏ */ 0xFF2B, 0xFF2C, 0xFF2D, 0xFF2E, 0xFF2F,
/* Ｐ Ｑ Ｒ Ｓ Ｔ */ 0xFF30, 0xFF31, 0xFF32, 0xFF33, 0xFF34,
/* Ｕ Ｖ Ｗ Ｘ Ｙ */ 0xFF35, 0xFF36, 0xFF37, 0xFF38, 0xFF39,
/* Ｚ ａ ｂ ｃ ｄ */ 0xFF3A, 0xFF41, 0xFF42, 0xFF43, 0xFF44,
/* ｅ ｆ ｇ ｈ ｉ */ 0xFF45, 0xFF46, 0xFF47, 0xFF48, 0xFF49,
/* ｊ ｋ ｌ ｍ ｎ */ 0xFF4A, 0xFF4B, 0xFF4C, 0xFF4D, 0xFF4E,
/* ｏ ｐ ｑ ｒ ｓ */ 0xFF4F, 0xFF50, 0xFF51, 0xFF52, 0xFF53,
/* ｔ ｕ ｖ ｗ ｘ */ 0xFF54, 0xFF55, 0xFF56, 0xFF57, 0xFF58,
/* ｙ ｚ ¥       */ 0xFF59, 0xFF5A, 0xFFE5,
/* ァ ア          */ 0x30A1, 0x30A2,
/* ィ イ          */ 0x30A3, 0x30A4,
/* ゥ ウ          */ 0x30A5, 0x30A6,
/* ェ エ          */ 0x30A7, 0x30A8,
/* ォ オ          */ 0x30A9, 0x30AA,
/* カ ガ          */ 0x30AB, 0x30AC,
/* キ ギ          */ 0x30AD, 0x30AE,
/* ク グ          */ 0x30AF, 0x30B0,
/* ケ ゲ          */ 0x30B1, 0x30B2,
/* コ ゴ          */ 0x30B3, 0x30B4,
/* サ ザ          */ 0x30B5, 0x30B6,
/* シ ジ          */ 0x30B7, 0x30B8,
/* ス ズ          */ 0x30B9, 0x30BA,
/* セ ゼ          */ 0x30BB, 0x30BC,
/* ソ ゾ          */ 0x30BD, 0x30BE,
/* タ ダ          */ 0x30BF, 0x30C0,
/* チ ヂ          */ 0x30C1, 0x30C2,
/* ッ ツ ヅ       */ 0x30C3, 0x30C4, 0x30C5,
/* テ デ          */ 0x30C6, 0x30C7,
/* ト ド          */ 0x30C8, 0x30C9,
/* ナ ニ ヌ ネ ノ */ 0x30CA, 0x30CB, 0x30CC, 0x30cd, 0x30ce,
/* ハ バ パ       */ 0x30CF, 0x30D0, 0x30D1,
/* ヒ ビ ピ       */ 0x30D2, 0x30D3, 0x30D4,
/* フ ブ プ       */ 0x30D5, 0x30D6, 0x30D7,
/* ヘ ベ ペ       */ 0x30D8, 0x30D9, 0x30DA,
/* ホ ボ ポ       */ 0x30DB, 0x30DC, 0x30DD,
/* マ ミ ム メ モ */ 0x30DE, 0x30DF, 0x30E0, 0x30e1, 0x30e2,
/* ャ ヤ          */ 0x30E3, 0x30E4,
/* ュ ユ          */ 0x30E5, 0x30E6,
/* ョ ヨ          */ 0x30E7, 0x30E8,
/* ラ リ ル レ ロ */ 0x30E9, 0x30EA, 0x30EB, 0x30EC, 0x30ED,
/* ヮ ワ          */ 0x30EE, 0x30EF,
/* ヲ ン          */ 0x30F2, 0x30F3,
/* ゛ ゜          */ 0x309B, 0x309C
};

static const NJ_UINT16 giji_han_eiji_2touch_tbl[NJ_ROMA_2TOUCH_TABLE_MAX][2] = {
/* SP 、  。  ー  !  */ {0x0020, 0x0000}, {0xFF64, 0x0000}, {0xFF61, 0x0000}, {0xFF70, 0x0000}, {0x0021, 0x0000},
/* #  &  *  /  0  */ {0x0023, 0x0000}, {0x0026, 0x0000}, {0x002A, 0x0000}, {0x002F, 0x0000}, {0x0030, 0x0000},
/* 1  2  3  4  5  */ {0x0031, 0x0000}, {0x0032, 0x0000}, {0x0033, 0x0000}, {0x0034, 0x0000}, {0x0035, 0x0000},
/* 6  7  8  9  ?  */ {0x0036, 0x0000}, {0x0037, 0x0000}, {0x0038, 0x0000}, {0x0039, 0x0000}, {0x003F, 0x0000},
/* A  B  C  D  E  */ {0x0041, 0x0000}, {0x0042, 0x0000}, {0x0043, 0x0000}, {0x0044, 0x0000}, {0x0045, 0x0000},
/* F  G  H  I  J  */ {0x0046, 0x0000}, {0x0047, 0x0000}, {0x0048, 0x0000}, {0x0049, 0x0000}, {0x004A, 0x0000},
/* K  L  M  N  O  */ {0x004B, 0x0000}, {0x004C, 0x0000}, {0x004D, 0x0000}, {0x004E, 0x0000}, {0x004F, 0x0000},
/* P  Q  R  S  T  */ {0x0050, 0x0000}, {0x0051, 0x0000}, {0x0052, 0x0000}, {0x0053, 0x0000}, {0x0054, 0x0000},
/* U  V  W  X  Y  */ {0x0055, 0x0000}, {0x0056, 0x0000}, {0x0057, 0x0000}, {0x0058, 0x0000}, {0x0059, 0x0000},
/* Z  a  b  c  d  */ {0x005A, 0x0000}, {0x0061, 0x0000}, {0x0062, 0x0000}, {0x0063, 0x0000}, {0x0064, 0x0000},
/* e  f  g  h  i  */ {0x0065, 0x0000}, {0x0066, 0x0000}, {0x0067, 0x0000}, {0x0068, 0x0000}, {0x0069, 0x0000},
/* j  k  l  m  n  */ {0x006A, 0x0000}, {0x006B, 0x0000}, {0x006C, 0x0000}, {0x006D, 0x0000}, {0x006E, 0x0000},
/* o  p  q  r  s  */ {0x006F, 0x0000}, {0x0070, 0x0000}, {0x0071, 0x0000}, {0x0072, 0x0000}, {0x0073, 0x0000},
/* t  u  v  w  x  */ {0x0074, 0x0000}, {0x0075, 0x0000}, {0x0076, 0x0000}, {0x0077, 0x0000}, {0x0078, 0x0000},
/* y  z  \        */ {0x0079, 0x0000}, {0x007A, 0x0000}, {0x005C, 0x0000},
/* ァ ア          */ {0xFF67, 0x0000}, {0xFF71, 0x0000},
/* ィ イ          */ {0xFF68, 0x0000}, {0xFF72, 0x0000},
/* ゥ ウ          */ {0xFF69, 0x0000}, {0xFF73, 0x0000},
/* ェ エ          */ {0xFF6A, 0x0000}, {0xFF74, 0x0000},
/* ォ オ          */ {0xFF6B, 0x0000}, {0xFF75, 0x0000},
/* カ ガ          */ {0xFF76, 0x0000}, {0xFF76, 0xFF9E},
/* キ ギ          */ {0xFF77, 0x0000}, {0xFF77, 0xFF9E},
/* ク グ          */ {0xFF78, 0x0000}, {0xFF78, 0xFF9E},
/* ケ ゲ          */ {0xFF79, 0x0000}, {0xFF79, 0xFF9E},
/* コ ゴ          */ {0xFF7A, 0x0000}, {0xFF7A, 0xFF9E},
/* サ ザ          */ {0xFF7B, 0x0000}, {0xFF7B, 0xFF9E},
/* シ ジ          */ {0xFF7C, 0x0000}, {0xFF7C, 0xFF9E},
/* ス ズ          */ {0xFF7D, 0x0000}, {0xFF7D, 0xFF9E},
/* セ ゼ          */ {0xFF7E, 0x0000}, {0xFF7E, 0xFF9E},
/* ソ ゾ          */ {0xFF7F, 0x0000}, {0xFF7F, 0xFF9E},
/* タ ダ          */ {0xFF80, 0x0000}, {0xFF80, 0xFF9E},
/* チ ヂ          */ {0xFF81, 0x0000}, {0xFF81, 0xFF9E},
/* ッ ツ ヅ       */ {0xFF6f, 0x0000}, {0xFF82, 0x0000}, {0xFF82, 0xFF9E},
/* テ デ          */ {0xFF83, 0x0000}, {0xFF83, 0xFF9E},
/* ト ド          */ {0xFF84, 0x0000}, {0xFF84, 0xFF9E},
/* ナ ニ ヌ ネ ノ */ {0xFF85, 0x0000}, {0xFF86, 0x0000}, {0xFF87, 0x0000}, {0xFF88, 0x0000}, {0xFF89, 0x0000},
/* ハ バ パ       */ {0xFF8A, 0x0000}, {0xFF8A, 0xFF9E}, {0xFF8A, 0xFF9F},
/* ヒ ビ ピ       */ {0xFF8B, 0x0000}, {0xFF8B, 0xFF9E}, {0xFF8B, 0xFF9F},
/* フ ブ プ       */ {0xFF8C, 0x0000}, {0xFF8C, 0xFF9E}, {0xFF8C, 0xFF9F},
/* ヘ ベ ペ       */ {0xFF8D, 0x0000}, {0xFF8D, 0xFF9E}, {0xFF8D, 0xFF9F},
/* ホ ボ ポ       */ {0xFF8E, 0x0000}, {0xFF8E, 0xFF9E}, {0xFF8E, 0xFF9F},
/* マ ミ ム メ モ */ {0xFF8F, 0x0000}, {0xFF90, 0x0000}, {0xFF91, 0x0000}, {0xFF92, 0x0000}, {0xFF93, 0x0000},
/* ャ ヤ          */ {0xFF6C, 0x0000}, {0xFF94, 0x0000},
/* ュ ユ          */ {0xFF6D, 0x0000}, {0xFF95, 0x0000},
/* ョ ヨ          */ {0xFF6E, 0x0000}, {0xFF96, 0x0000},
/* ラ リ ル レ ロ */ {0xFF97, 0x0000}, {0xFF98, 0x0000}, {0xFF99, 0x0000}, {0xFF9A, 0x0000}, {0xFF9B, 0x0000},
/* ヮ ワ          */ {0xFF9C, 0x0000}, {0xFF9C, 0x0000},
/* ヲ ン          */ {0xFF66, 0x0000}, {0xFF9D, 0x0000},
/* ゛ ゜          */ {0xFF9E, 0x0000}, {0xFF9F, 0x0000}
};

static const NJ_UINT16 giji_zen_suuji_emoji_tbl[NJ_SUUJI_EMOJI_TABLE_MAX] = {
/* ０ １ ２ ３ ４ */ 0xE6EB, 0xE6E2, 0xE6E3, 0xE6E4, 0xE6E5,
/* ５ ６ ７ ８ ９ */ 0xE6E6, 0xE6E7, 0xE6E8, 0xE6E9, 0xE6EA
};

static const NJ_UINT16 giji_zen_suuji_maru_tbl[NJ_SUUJI_MARU_TABLE_MAX] = {
/* ① ② ③ ④ ⑤ */ 0x2460, 0x2461, 0x2462, 0x2463, 0x2464,
/* ⑥ ⑦ ⑧ ⑨ ⑩ */ 0x2465, 0x2466, 0x2467, 0x2468, 0x2469,
/* ⑪ ⑫ ⑬ ⑭ ⑮ */ 0x246A, 0x246B, 0x246C, 0x246D, 0x246E,
/* ⑯ ⑰ ⑱ ⑲ ⑳ */ 0x246F, 0x2470, 0x2471, 0x2472, 0x2473
};

static const NJ_UINT16 giji_zen_suuji_roma_tbl[NJ_SUUJI_ROMA_TABLE_MAX] = {
/* Ⅰ Ⅱ Ⅲ Ⅳ Ⅴ */ 0x2160, 0x2161, 0x2162, 0x2163, 0x2164,
/* Ⅵ Ⅶ Ⅷ Ⅸ Ⅹ */ 0x2165, 0x2166, 0x2167, 0x2168, 0x2169
};

static const NJ_UINT16 kurai_suuji_sen_table[NJ_SUUJI_KURAI_SEN_TABLE_MAX] = {
/* 千 (未使用) 十 百 */ 0x5343, 0x0000, 0x5341, 0x767E
};

static const NJ_UINT16 kurai_suuji_sen_daiji_table[NJ_SUUJI_KURAI_SEN_TABLE_MAX] = {
/* 千 (未使用) 拾 百 */ 0x5343, 0x0000, 0x62FE, 0x767E
};

static const NJ_UINT16 kurai_suuji_tyou_table[NJ_SUUJI_KURAI_TYOU_TABLE_MAX] = {
/* 兆 億 万 */  0x5146, 0x5104, 0x4E07
};

