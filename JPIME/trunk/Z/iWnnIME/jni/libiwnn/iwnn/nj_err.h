/**
 * @file
 *   変換エンジンエラー定義（基本変数型宣言）
 *
 *   アプリケーションで使用する為に必要な定義を行う。
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2011 All Rights Reserved.
 */

#ifndef _NJ_ERR_H_
#define _NJ_ERR_H_

/************************************************/
/*                マクロ定義                    */
/************************************************/
/*
 * エラーコードアクセス マクロ定義
 */
#define NJ_ERR_CODE_MASK        (0x7F00)        /**< エラーコードマスク */
#define NJ_ERR_FUNC_MASK        (0x00FF)        /**< 関数コードマスク */

/**
 * エラーコード取得
 *
 * @param[in]  x : API の返値
 *
 * @return         エラーコード
 */
#define NJ_GET_ERR_CODE(x)      ((x) & NJ_ERR_CODE_MASK)
/**
 * エラー関数コード取得
 *
 * @param[in]  x : API の返値
 *
 * @return         エラー関数コード
 */
#define NJ_GET_ERR_FUNC(x)      ((x) & NJ_ERR_FUNC_MASK)

#define NJ_SET_ERR_VAL(x, y)    ((NJ_INT16)((x) | (y) | 0x8000))

/************************************************/
/*                 定数定義                     */
/************************************************/
/*
 * エラーコード
 */
#define NJ_ERR_PARAM_DIC_NULL                       (0x0000) /**< 辞書セットにNULLが指定されました   */
#define NJ_ERR_PARAM_YOMI_NULL                      (0x0100) /**< 読み文字列にNULLが指定されました   */
#define NJ_ERR_PARAM_YOMI_SIZE                      (0x0200) /**< 不正な読み文字列サイズが指定されました */
#define NJ_ERR_PARAM_KUGIRI                         (0x0300) /**< 不正な区切り位置が指定されました   */
#define NJ_ERR_PARAM_ILLEGAL_LEVEL                  (0x0400) /**< 不正な指定解析文節数が指定されました       */
#define NJ_ERR_PARAM_RESULT_NULL                    (0x0500) /**< 結果格納バッファにNULLが指定されました */
#define NJ_ERR_YOMI_TOO_LONG                        (0x0600) /**< NJ_MAX_LENを超える読み文字列が指定されました       */
#define NJ_ERR_NO_HINSI                             (0x0700) /**< 変換処理用品詞番号が取得できませんでした */
#define NJ_ERR_NO_RULEDIC                           (0x0800) /**< ルール辞書が指定されていません     */
#define NJ_ERR_PARAM_OPERATION                      (0x0900) /**< 不正な検索方法が指定されました     */
#define NJ_ERR_PARAM_MODE                           (0x0A00) /**< 不正な検索候補取得順が指定されました       */
#define NJ_ERR_PARAM_KANJI_NULL                     (0x0B00) /**< 漢字文字列にNULLが指定されました   */
#define NJ_ERR_CANDIDATE_TOO_LONG                   (0x0C00) /**< NJ_MAX_RESULT_LENよりも長い候補が存在します*/
#define NJ_ERR_PARAM_CURSOR_NULL                    (0x0D00) /**< カーソルにNULLが指定されました     */
#define NJ_ERR_DIC_TYPE_INVALID                     (0x0E00) /**< 不正な辞書タイプが指定されました   */
#define NJ_ERR_DIC_HANDLE_NULL                      (0x0F00) /**< 辞書ハンドルにNULLが指定されました */
#define NJ_ERR_FORMAT_INVALID                       (0x1000) /**< 辞書が壊れています                 */
#define NJ_ERR_NO_CANDIDATE_LIST                    (0x1100) /**< 全候補リストが存在しません         */
#define NJ_ERR_NOT_CONVERTED                        (0x1200) /**< 未変換中に全候補取得APIがコールされました */
#define NJ_ERR_AREASIZE_INVALID                     (0x1300) /**< 不正な辞書サイズが指定されました   */
#define NJ_ERR_BUFFER_NOT_ENOUGH                    (0x1400) /**< バッファサイズが不足しています     */
#define NJ_ERR_HINSI_GROUP_INVALID                  (0x1500) /**< 不正な品詞グループが指定されました */
#define NJ_ERR_CREATE_TYPE_INVALID                  (0x1600) /**< 不正な生成辞書タイプが指定されました       */
#define NJ_ERR_WORD_INFO_NULL                       (0x1700) /**< 登録単語情報にNULLが指定されました */
#define NJ_ERR_DIC_NOT_FOUND                        (0x1800) /**< 対象辞書が見つかりませんでした     */
#define NJ_ERR_CANNOT_GET_QUE                       (0x1900) /**< 単語情報が取得できませんでした     */
#define NJ_ERR_INVALID_FLAG                         (0x1A00) /**< 不正なフラグが指定されました       */
#define NJ_ERR_INVALID_RESULT                       (0x1B00) /**< 指定された処理結果が破壊されています       */
#define NJ_ERR_NOT_SELECT_YET                       (0x1C00) /**< 前確定情報がありません             */
#define NJ_ERR_INTERNAL                             (0x1D00) /**< 内部エラー(発生しない)             */
#define NJ_ERR_USER_YOMI_INVALID                    (0x1E00) /**< 登録単語時に不正な読み文字列が指定されました */
#define NJ_ERR_USER_KOUHO_INVALID                   (0x1F00) /**< 登録単語時に不正な候補文字列が指定されました */
#define NJ_ERR_USER_DIC_FULL                        (0x2000) /**< ユーザ辞書がいっぱいになりました   */
#define NJ_ERR_SAME_WORD                            (0x2100) /**< ユーザ辞書に同じ単語が登録されています */
#define NJ_ERR_DIC_BROKEN                           (0x2200) /**< 辞書が壊れています */
#define NJ_ERR_CANNOT_RESTORE                       (0x2300) /**< 辞書が復旧できませんでした */
#define NJ_ERR_WORD_NOT_FOUND                       (0x2400) /**< 削除しようとした単語が見つかりませんでした */
#define NJ_ERR_PARAM_NORMAL_NULL                    (0x2500) /**< 正規形検索結果格納バッファにNULLが指定されました */
#define NJ_ERR_PARAM_TYPE_INVALID                   (0x2600) /**< 不正な品詞属性が指定されました     */
#define NJ_ERR_PARAM_PROCESS_LEN_NULL               (0x2700) /**< 解析対象文字列長にNULLが指定されました */
#define NJ_ERR_CANNOT_USE_MORPHOLIZE                (0x2800) /**< 形態素解析結果では使えません  */
#define NJ_ERR_PARAM_ILLEGAL_LIMIT                  (0x2900) /**< 前方一致予測候補取得数不正な値が指定されました */
#define NJ_ERR_DIC_VERSION_INVALID                  (0x2A00) /**< 辞書バージョンが不正です */
#define NJ_ERR_DIC_FREQ_INVALID                     (0x2B00) /**< 辞書頻度設定が不正です */
#define NJ_ERR_CACHE_NOT_ENOUGH                     (0x2C00) /**< キャッシュサイズが不足しています   */
#define NJ_ERR_CACHE_BROKEN                         (0x2D00) /**< キャッシュが壊れています           */
#define NJ_ERR_PARAM_ENV_NULL                       (0x2E00) /**< 外部変数(NJ_CLASS)にNULLが指定されました   */
#define NJ_ERR_PARAM_EXT_AREA_NULL                  (0x2F00) /**< 頻度学習領域にNULLが指定されました */
#define NJ_ERR_PARAM_EXT_AREA_INVALID_SIZE          (0x3000) /**< 頻度学習領域サイズに0が指定されました      */
#define NJ_ERR_PARAM_ANALYZE_OPTION_NULL            (0x3100) /**< 予測オプションにNULLが指定されました       */
#define NJ_ERR_PARAM_ILLEGAL_CHAR_LEN               (0x3200) /**< 読み最大・最小文字数に不正な値が指定されました */
#define NJ_ERR_PARAM_STATE_NULL                     (0x3300) /**< 状況設定にNULLが指定されました */
#define NJ_ERR_PARAM_SIZE_NULL                      (0x3400) /**< 取得サイズにNULLが指定されました */
#define NJ_ERR_PARAM_INVALID_STATE                  (0x3500) /**< 状況設定に不正な値が設定されました。 */
#define NJ_ERR_PROTECTION_ERROR                     (0x3600) /**< 学習辞書保護範囲への操作を行おうとしました。*/
#define NJ_ERR_PARAM_INDEX_INVALID                  (0x3700) /**< 不正なインデックスが指定されました */
#define NJ_ERR_USER_ADDITIONAL_INVALID              (0x3800) /**< 登録単語時に不正な付加情報文字列が指定されました */
#define NJ_ERR_PARAM_ADD_INFO_NULL                  (0x3900) /**< 付加情報領域にNULLが指定されました */
#define NJ_ERR_PARAM_ADD_INFO_INVALID_SIZE          (0x3A00) /**< 付加情報領域サイズに0が指定されました      */
#define NJ_ERR_PARAM_STREAM_NULL                    (0x3B00) /**< ファイルストリームにNULLが指定されました */
#define NJ_ERR_STREAM_SEEK_ERR                      (0x3C00) /**< ファイルストリームに対するSEEKがエラーとなりました */
#define NJ_ERR_STREAM_READ_ERR                      (0x3D00) /**< ファイルストリームに対するREADがエラーとなりました */
#define NJ_ERR_PARAM_NULL                           (0x3E00) /**< 引数にNULLが指定されました */
#define NJ_ERR_PARAM_EXT_AREA_INVALID               (0x3F00) /**< 不正な頻度学習領域が指定されました */
#define NJ_ERR_PARAM_VALUE_INVALID                  (0x4000) /**< 不正な値の引数が指定されました */
#define NJ_ERR_OPTION_STORAGE_OFF                   (0x4100) /**< ストレージ辞書機能OFFの設定でストレージ辞書機能が使用されました */
#define NJ_ERR_CONJ_DATA_NULL                       (0x4200) /**< 活用形変形データ領域にNULLが指定されました */
#define NJ_ERR_PARAM_FZK_NULL                       (0x4300) /**< 付属語解析に必要な領域にNULLが指定されました */
#define NJ_ERR_FZK_TOO_LONG                         (0x4400) /**< 付属語の長さが長すぎる */
#define NJ_ERR_CONJ_DATA_INVALID                    (0x4500) /**< 不正な活用形変形データが指定されました */

/*
 * エラー関数コード
 */
/* 形態素解析部 */
#define NJ_FUNC_MM_SPLIT_WORD                       (0x0070)
#define NJ_FUNC_MM_GET_YOMI                         (0x0073)
#define NJ_FUNC_MM_GET_HINSI                        (0x0072)
#define NJ_FUNC_MM_SELECT                           (0x0071)

/* 変換部 */
#define NJ_FUNC_NJ_CONV                             (0x0000)
#define NJ_FUNC_NJ_ZENKOUHO                         (0x0001)
#define NJ_FUNC_CONV_MULTICONV                      (0x0002)
#define NJ_FUNC_CONV_STEMPROCESS                    (0x0003)
#define NJ_FUNC_CONV_FZKPROCESS                     (0x0004)
#define NJ_FUNC_CONV_ENV_SET                        (0x0005)
#define NJ_FUNC_CONV_GIJIPROCESS                    (0x0006)
#define NJ_FUNC_CAND_ENV_SET                        (0x0007)
#define NJ_FUNC_CAND_GETCANDIDATE                   (0x0008)
#define NJ_FUNC_GET_STROKE_WORD                     (0x0009)
#define NJ_FUNC_FZK_CONV                            (0x00B6)
#define NJ_FUNC_GET_CONJUGATION_TYPE                (0x00B7)
#define NJ_FUNC_CONJ_NJX_FZK_SPLIT_WORD             (0x00BC)

/* 辞書部 */
/* 圧縮辞書アダプタ */
#define NJ_FUNC_NJD_B_GET_CANDIDATE                 (0x0010)
#define NJ_FUNC_NJD_B_GET_STROKE                    (0x0061)
#define NJ_FUNC_NJD_B_SEARCH_WORD                   (0x0062)

/* 読無辞書アダプタ */
#define NJ_FUNC_NJD_F_GET_WORD                      (0x0011)
#define NJ_FUNC_NJD_F_GET_STROKE                    (0x0012)
#define NJ_FUNC_NJD_F_GET_CANDIDATE                 (0x0013)
/* 学習辞書アダプタ */
#define NJ_FUNC_NJD_L_DELETE_WORD                   (0x0014)
#define NJ_FUNC_NJD_L_ADD_WORD                      (0x0015)
#define NJ_FUNC_NJD_L_UNDO_LEARN                    (0x0016)
#define NJ_FUNC_DELETE_INDEX                        (0x0017)
#define NJ_FUNC_INSERT_INDEX                        (0x0018)
#define NJ_FUNC_QUE_STRCMP_COMPLETE_WITH_HYOUKI     (0x0019)
#define NJ_FUNC_NJD_L_GET_WORD                      (0x001B)
#define NJ_FUNC_NJD_L_GET_CANDIDATE                 (0x001C)
#define NJ_FUNC_NJD_L_GET_STROKE                    (0x001D)
#define NJ_FUNC_QUE_STRCMP_FORWARD                  (0x001E)
#define NJ_FUNC_NJD_L_CHECK_DIC                     (0x001F)
#define NJ_FUNC_SEARCH_RANGE_BY_YOMI                (0x0020)
#define NJ_FUNC_STR_QUE_CMP                         (0x0021)
#define NJ_FUNC_WRITE_LEARN_DATA                    (0x0022)
#define NJ_FUNC_NJD_L_GET_ADDITIONAL_INFO           (0x0096)
/* ルール辞書アダプタ */
#define NJ_FUNC_NJD_R_CHECK_GROUP                   (0x0064)

/* 統合辞書アダプタ */
#define NJ_FUNC_NJD_T_SEARCH_WORD                   (0x0080)
#define NJ_FUNC_NJD_T_GET_CANDIDATE                 (0x0081)
#define NJ_FUNC_NJD_T_GET_STROKE                    (0x0082)
#define NJ_FUNC_NJD_T_GET_RELATIONAL_WORD           (0x0085)
#define NJ_FUNC_NJD_T_GET_ADD_INFO                  (0x0098)
#define NJ_FUNC_NJD_T_INIT_EXT_AREA                 (0x008C)
#define NJ_FUNC_NJD_T_CHECK_EXT_AREA                (0x008F)
#define NJ_FUNC_NJD_T_CHECK_ADD_INFO                (0x0094)
#define NJ_FUNC_NJD_T_GET_USER_PROF_DATA_SIZE       (0x00AB)
#define NJ_FUNC_NJD_T_EXP_USER_PROF_DATA            (0x00AE)
#define NJ_FUNC_NJD_T_IMP_USER_PROF_DATA            (0x00BB)

/* 拡張読み無し予測辞書アダプタ */
#define NJ_FUNC_NJD_P_GET_CANDIDATE                 (0x0091)
#define NJ_FUNC_NJD_P_GET_STROKE                    (0x0092)
#define NJ_FUNC_NJD_P_GET_ADD_INFO                  (0x0099)
#define NJ_FUNC_NJD_P_CHECK_ADD_INFO                (0x0097)

/* ストレージ辞書アダプタ */
#define NJ_FUNC_NJD_S_SEARCH_WORD                   (0x00A0)
#define NJ_FUNC_NJD_S_GET_CANDIDATE                 (0x00A1)
#define NJ_FUNC_NJD_S_GET_STROKE                    (0x00A2)
#define NJ_FUNC_NJD_S_GET_RELATIONAL_WORD           (0x00A3)
#define NJ_FUNC_NJD_S_GET_ADD_INFO                  (0x00A4)
#define NJ_FUNC_NJD_S_INIT_EXT_AREA                 (0x00A5)
#define NJ_FUNC_NJD_S_CHECK_EXT_AREA                (0x00A6)
#define NJ_FUNC_NJD_S_CHECK_ADD_INFO                (0x00A7)
#define NJ_FUNC_NJD_S_GET_STORAGE_DIC_CACHE_SIZE    (0x00A8)
#define NJ_FUNC_NJD_S_SET_STORAGE_DIC_INFO          (0x00A9)
#define NJ_FUNC_NJD_S_GET_USER_PROF_DATA_SIZE       (0x00AC)
#define NJ_FUNC_NJD_S_EXP_USER_PROF_DATA            (0x00AF)
#define NJ_FUNC_NJD_S_IMP_USER_PROF_DATA            (0x00B1)
#define NJ_FUNC_NJD_S_DELETE_WORD_EXT               (0x00B2)
#define NJ_FUNC_NJD_S_LEARN_WORD                    (0x00B3)
#define NJ_FUNC_NJD_S_DELETE_WORD                   (0x00B4)
#define NJ_FUNC_NJD_S_GET_WORD                      (0x00B5)

/* 辞書引き部 API I/F部 */
#define NJ_FUNC_CHECK_SEARCH_CURSOR                 (0x0023)
#define NJ_FUNC_GET_WORD_AND_SEARCH_NEXT_WORD       (0x0024)

#define NJ_FUNC_NJD_GET_WORD_DATA                   (0x0025)
#define NJ_FUNC_NJD_GET_RELATIONAL_WORD             (0x0026)
#define NJ_FUNC_NJD_GET_WORD                        (0x0027)
#define NJ_FUNC_NJD_CHECK_DIC                       (0x0028)

#define NJ_FUNC_NJ_CREATE_DIC                       (0x0029)
#define NJ_FUNC_NJD_GET_STROKE                      (0x002A)
#define NJ_FUNC_NJD_GET_CANDIDATE                   (0x002B)
#define NJ_FUNC_NJ_SEARCH_WORD                      (0x002C)
#define NJ_FUNC_NJ_GET_WORD                         (0x002D)
#define NJ_FUNC_NJ_ADD_WORD                         (0x002E)
#define NJ_FUNC_NJ_DELETE_WORD                      (0x002F)
#define NJ_FUNC_NJ_CHECK_DIC                        (0x0030)
#define NJ_FUNC_NJD_L_MAKE_SPACE                    (0x0053)
#define NJ_FUNC_SEARCH_RANGE_BY_YOMI_MULTI          (0x0054)
#define NJ_FUNC_NJD_L_GET_RELATIONAL_WORD           (0x0055)
#define NJ_FUNC_QUE_STRCMP_INCLUDE                  (0x0056)
#define NJ_FUNC_IS_CONTINUED                        (0x0057)
#define NJ_FUNC_CONTINUE_CNT                        (0x0058)
#define NJ_FUNC_NJD_GET_ADDITIONAL_INFO             (0x0095)

#define NJ_FUNC_NJC_CONV                            (0x0031)
#define NJ_FUNC_NJC_ZENKOUHO                        (0x0032)
#define NJ_FUNC_NJC_ZENKOUHO1                       (0x0033)
#define NJ_FUNC_NJC_TOP_CONV                        (0x0034)
#define NJ_FUNC_NJC_GET_STROKE                      (0x0035)
#define NJ_FUNC_NJC_GET_CANDIDATE                   (0x0036)
#define NJ_FUNC_NJC_GET_GIJI_RESULT                 (0x0037)
#define NJ_FUNC_NJ_CHANGE_DIC_TYPE                  (0x0076)
#define NJ_FUNC_NJ_GET_EXT_AREA_SIZE                (0x0039)
#define NJ_FUNC_NJ_INIT_EXT_AREA                    (0x003A)
#define NJ_FUNC_NJ_CHECK_EXT_AREA                   (0x003B)
#define NJ_FUNC_SEARCH_WORD                         (0x003C)
#define NJ_FUNC_NJ_CHECK_ADD_INFO                   (0x003D)
#define NJ_FUNC_PROGRAM_DIC_OPERATION               (0x0090)
#define NJ_FUNC_NJ_GET_STORAGE_DIC_CACHE_SIZE       (0x009B)
#define NJ_FUNC_NJ_SET_STORAGE_DIC_INFO             (0x009C)

/* 共通部API I/F */
#define NJ_FUNC_NJ_SELECT                           (0x0040)
#define NJ_FUNC_NJ_INIT                             (0x0041)
#define NJ_FUNC_NJ_GET_CANDIDATE                    (0x0042)
#define NJ_FUNC_NJ_GET_STROKE                       (0x0043)
#define NJ_FUNC_NJ_GET_CHAR_TYPE                    (0x0074)
#define NJ_FUNC_NJ_GET_GIJI                         (0x0075)
#define NJ_FUNC_NJ_UNDO                             (0x0079)
#define NJ_FUNC_NJ_SET_GIJISET                      (0x0077)
#define NJ_FUNC_NJ_SET_OPTION                       (0x0083)
#define NJ_FUNC_NJ_SET_STATE                        (0x0086)
#define NJ_FUNC_NJ_GET_STATE                        (0x0087)
#define NJ_FUNC_NJ_GET_WORD_INFO                    (0x008D)
#define NJ_FUNC_NJ_MERGE_WORD_LIST                  (0x008E)
#define NJ_FUNC_NJ_GET_ADD_INFO                     (0x009A)
#define NJ_FUNC_NJ_GET_USER_PROF_DATA_SIZE          (0x00AA)
#define NJ_FUNC_NJ_EXP_USER_PROF_DATA               (0x00AD)
#define NJ_FUNC_NJ_IMP_USER_PROF_DATA               (0x00B0)

/* 評価部API I/F */
#define NJ_FUNC_NJ_ANALYZE                          (0x0050)
#define NJ_FUNC_ANALYZE_BODY                        (0x0051)
#define NJ_FUNC_ANALYZE_CONVERSION_SINGLE           (0x0052)
#define NJ_FUNC_ANALYZE_MODE_CHANGE                 (0x0084)

/* 共通部 nehomonym.c */
#define NJ_FUNC_NJE_APPEND_HOMONYM                  (0x0060)
#define NJ_FUNC_NJ_MANAGE_LEARNDIC                  (0x0093)

/* 共通部 nj_fio.c */
#define NJ_FUNC_NJD_OFFSET_FREAD                    (0x009F)
#define NJ_FUNC_NJ_FREAD                            (0x00B8)
#define NJ_FUNC_NJ_FSEEK                            (0x00B9)
#define NJ_FUNC_NJD_OFFSET_FREAD_CACHE              (0x00BA)

#endif /* _NJ_ERR_H_ */
