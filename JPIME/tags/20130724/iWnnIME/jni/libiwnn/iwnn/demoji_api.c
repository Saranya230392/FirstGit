/**
 * @file
 *   デコメ絵文字読み入力 API部
 *
 *   アプリケーションで使用する為に必要な定義を行う。
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2010 All Rights Reserved.
 */
#include "nj_lib.h"
#include "nj_err.h"
#include "nj_dicif.h"
#include "nj_ext.h"
#include "ex_giji.h"
#include "demoji_lib.h"
#include "demoji_giji.h"
#include "demoji_api.h"

/************************************************/
/*           プ ロ ト タ イ プ 宣 言            */
/************************************************/
static DL_VOID init_dicset(NJ_DIC_SET *dicset);
static DL_VOID set_demoji_giji(NJ_DIC_SET *dicset, DL_UINT16 idx, DL_UINT8 *demojidic);
static DL_VOID clear_memory(DL_UINT8 *buf, DL_UINT32 size);
static DL_VOID conv_utf16be(DL_UINT16 *dst, DL_UINT16 *src, DL_UINT16 src_len);
static DL_VOID conv_iwnn_hinsi(DL_UINT16 hinsi, DL_UINT16 *f_hinsi, DL_UINT16 *b_hinsi);
static DL_INT16 set_word_info(NJ_CLASS *iwnn, NJ_WORD_INFO *word, DL_UINT16 *yomi, DL_UINT16 *id, DL_UINT16 hinsi, DL_UINT16 mm_flag);
static DL_INT16 check_word_match(NJ_CLASS *iwnn, NJ_RESULT *result, DL_UINT16 *id, DL_UINT16 id_len, DL_UINT16 f_hinsi, DL_UINT16 b_hinsi, DL_UINT8 hinsi_check);
static DL_INT16 isAllHiragana(DL_UINT16 *yomi);

/************************************************/
/*              外 部 関 数 定 義               */
/************************************************/
/**
 * 初期化処理
 *
 * <b>処理概要</b>
 *  - パラメータチェックを行う。
 *  - iWnn解析クラスの辞書セットに絵文字擬似辞書、形態素解析用基本辞書、ルール辞書をセットする。
 *  - iWnnエンジンの初期化を行う。
 *  - 絵文字辞書情報の初期化フラグをONにする。
 *
 * @param[in,out]  demoji    絵文字辞書情報
 *
 * @retval  DL_ERROR_NONE         正常終了
 * @retval  DL_ERROR_PARAM        引数エラー
 * @retval  DL_ERROR_INITIALIZED  既に初期化されている
 * @retval  DL_ERROR_INTERNAL     API内部エラー
 */
DL_EXTERN DL_INT16 demoji_init(DL_DEMOJI_INFO *demoji) {
    NJ_CLASS   *nj_class;
    DL_INT16    ret;
    DL_UINT8    i,j;

    if (demoji == NULL) {
        /* パラメータ不正 */
        return DL_ERROR_PARAM;
    }

    if ((demoji->iwnn == NULL) || (demoji->demojidic == NULL)) {
        /* パラメータ不正 */
        return DL_ERROR_PARAM;
    }

    if (demoji->basicdics_cnt > DL_MAX_DIC) {
        /* パラメータ不正 */
        return DL_ERROR_PARAM;
    }

    for (i = 0; i < demoji->basicdics_cnt; i++) {
        if (demoji->basicdics[i].handle == NULL) {
            /* パラメータ不正 */
            return DL_ERROR_PARAM;
        }
    }

    if ((demoji->ruledic_h == NULL) ||
        ((demoji->basicdics_cnt > 0) && (demoji->ruledic_m == NULL))) {
        /* パラメータ不正 */
        return DL_ERROR_PARAM;
    }

    if (demoji->init_flg == DL_INIT_ON) {
        /* 既に初期化されている */
        return DL_ERROR_INITIALIZED;
    }

    /* NJ_CLASS型にキャスト */
    nj_class = (NJ_CLASS *)demoji->iwnn;

    /* 辞書セットの初期化 */
    init_dicset(&nj_class->dic_set);

    /* 基本辞書マウント */
    for (i = 0; i < demoji->basicdics_cnt; i++) {
        nj_class->dic_set.dic[i].type = NJ_DIC_H_TYPE_NORMAL;
        for (j = 0; j < NJ_MAX_EXT_AREA; j++) {
            nj_class->dic_set.dic[i].ext_area[j] = NULL;
        }
        nj_class->dic_set.dic[i].add_info[0] = NULL;
        nj_class->dic_set.dic[i].add_info[1] = NULL;
        nj_class->dic_set.dic[i].handle = demoji->basicdics[i].handle;
        nj_class->dic_set.dic[i].dic_freq[NJ_MODE_TYPE_MORPHO].base = demoji->basicdics[i].freq_base_m;
        nj_class->dic_set.dic[i].dic_freq[NJ_MODE_TYPE_MORPHO].high = demoji->basicdics[i].freq_high_m;
        nj_class->dic_set.dic[i].srhCache = NULL;
        nj_class->dic_set.dic[i].limit = 0;
    }

    /* 絵文字擬似辞書マウント */
    set_demoji_giji(&(nj_class->dic_set), i, demoji->demojidic);

    /* ルール辞書マウント */
    nj_class->dic_set.rHandle[NJ_MODE_TYPE_HENKAN] = demoji->ruledic_h;
    nj_class->dic_set.rHandle[NJ_MODE_TYPE_MORPHO] = demoji->ruledic_m;

    /* iWnnエンジン初期化 */
    ret = njx_init(nj_class, NULL);
    if (ret < 0) {
        return DL_ERROR_INTERNAL;
    }

    /* 初期化フラグをONにする */
    demoji->init_flg = DL_INIT_ON;

    return DL_ERROR_NONE;
}

/**
 * 終了処理
 *
 * <b>処理概要</b>
 *  - パラメータチェックを行う。
 *  - 絵文字辞書情報の初期化フラグをOFFにする。
 *
 * @param[in,out]  demoji    絵文字辞書情報
 *
 * @retval  DL_ERROR_NONE      正常終了
 * @retval  DL_ERROR_PARAM     引数エラー
 * @retval  DL_ERROR_INTERNAL  API内部エラー
 */
DL_EXTERN DL_INT16 demoji_quit(DL_DEMOJI_INFO *demoji) {

    if (demoji == NULL) {
        /* パラメータ不正 */
        return DL_ERROR_PARAM;
    }

    /* 初期化フラグをOFFにする */
    demoji->init_flg = DL_INIT_OFF;

    return DL_ERROR_NONE;
}

/**
 * 絵文字辞書領域作成
 *
 * <b>処理概要</b>
 *  - パラメータチェックを行う。
 *  - 絵文字辞書領域の初期化を行う。
 *
 * @param[in,out]  iwnn      iWnn解析情報クラス
 * @param[in,out]  handle    絵文字辞書ハンドル
 *
 * @retval  DL_ERROR_NONE      正常終了
 * @retval  DL_ERROR_PARAM     引数エラー
 * @retval  DL_ERROR_INTERNAL  API内部エラー
 */
DL_EXTERN DL_INT16 demoji_create_dic(DL_UINT8* iwnn, DL_UINT8* handle) {
    NJ_CLASS      *nj_class;
    NJ_DIC_HANDLE  demojidic;
    DL_INT16       ret;

    if ((iwnn == NULL) || (handle == NULL)) {
        /* パラメータ不正 */
        return DL_ERROR_PARAM;
    }

    /* NJ_CLASS型にキャスト */
    nj_class = (NJ_CLASS *)iwnn;

    /* NJ_DIC_HANDLE型にキャスト */
    demojidic = (NJ_DIC_HANDLE)handle;

    /* 絵文字辞書領域を初期化 */
    ret = createEmojiDictionary(nj_class, demojidic, DL_DEMOJIDIC_SIZE);
    if (ret < 0) {
        return DL_ERROR_INTERNAL;
    }

    return DL_ERROR_NONE;
}

/**
 * 絵文字辞書チェック
 *
 * <b>処理概要</b>
 *  - パラメータチェックを行う。
 *  - 絵文字辞書領域の辞書チェックを行う。
 *
 * @param[in,out]  iwnn      iWnn解析情報クラス
 * @param[in]      handle    絵文字辞書ハンドル
 *
 * @retval 0                         正常データ
 * @retval DL_ERROR_PARAM            引数エラー
 * @retval DL_ERROR_PARAM以外の負数  異常データ
 */
DL_EXTERN DL_INT16 demoji_check_dic(DL_UINT8* iwnn, DL_UINT8* handle) {
    NJ_CLASS      *nj_class;
    NJ_DIC_HANDLE  demojidic;
    DL_INT16       ret;

    if ((iwnn == NULL) || (handle == NULL)) {
        /* パラメータ不正 */
        return DL_ERROR_PARAM;
    }

    /* NJ_CLASS型にキャスト */
    nj_class = (NJ_CLASS *)iwnn;

    /* NJ_DIC_HANDLE型にキャスト */
    demojidic = (NJ_DIC_HANDLE)handle;

    /* 絵文字辞書チェック */
    ret = checkEmojiDictionary(nj_class, demojidic, DL_DEMOJIDIC_SIZE);
    if (ret < 0) {
        return ret;
    }

    return 0;
}

/**
 * 絵文字辞書登録
 *
 * <b>処理概要</b>
 *  - パラメータチェックを行う。
 *  - パラメータで指定された内容で単語登録情報を設定する。
 *  - 単語登録を行う。
 *
 * @param[in,out]  demoji     絵文字辞書情報
 * @param[in]      id         絵文字ID
 * @param[in]      yomi       読み/表記
 * @param[in]      hinsi      品詞
 * @param[in]      mm_flag    形態素解析実行フラグ(DL_MM_ON:実行する、DL_MM_OFF:実行しない)
 *
 * @retval  DL_ERROR_NONE             正常終了
 * @retval  DL_ERROR_NOT_INITIALIZED  初期化されていない
 * @retval  DL_ERROR_PARAM            引数エラー
 * @retval  DL_ERROR_SAME_WORD        登録する内容が重複している
 * @retval  DL_ERROR_DIC_FULL         最大件数登録されている
 * @retval  DL_ERROR_MM_FAIL          形態素解析に失敗した
 * @retval  DL_ERROR_INTERNAL         API内部エラー
 */
DL_EXTERN DL_INT16 demoji_addToEmojiDictionary(DL_DEMOJI_INFO *demoji, DL_UINT16 *id, DL_UINT16 *yomi, DL_UINT16 hinsi, DL_UINT16 mm_flag) {
    NJ_CLASS       *nj_class;
    NJ_INT16        ret;
    NJ_WORD_INFO    word;
    NJ_UINT16       yomi_len;
    NJ_UINT16       id_len;
    NJ_CHAR         yomi_tmp[NJ_MAX_EMOJI_LEN+NJ_TERM_SIZE];
    NJ_CHAR         id_tmp[NJ_MAX_EMOJI_KOUHO_LEN+NJ_TERM_SIZE];

    if ((demoji == NULL) || (id == NULL) || (yomi == NULL) || (hinsi > DL_PART_MAX) || (mm_flag > 1)) {
        /* 引数エラー */
        return DL_ERROR_PARAM;
    }

    if ((demoji->iwnn == NULL) || (demoji->demojidic == NULL)) {
        /* 引数エラー */
        return DL_ERROR_PARAM;
    }

    yomi_len = nj_strlen(yomi);
    id_len = nj_strlen(id);
    if ((yomi_len > DL_MAX_YOMI_LEN) || (id_len > DL_MAX_ID_LEN)) {
        /* 引数エラー */
        return DL_ERROR_PARAM;
    }

    if (demoji->init_flg != DL_INIT_ON) {
        /* 初期化されていない */
        return DL_ERROR_NOT_INITIALIZED;
    }

    /* NJ_CLASS型にキャスト */
    nj_class = (NJ_CLASS *)demoji->iwnn;

    /* 読みをUTF16BEに変換 */
    conv_utf16be(yomi_tmp, yomi, yomi_len);

    /* 絵文字IDをUTF16BEに変換 */
    conv_utf16be(id_tmp, id, id_len);

    /* 単語登録情報の初期化  */
    clear_memory((DL_UINT8*)&word, sizeof(NJ_WORD_INFO));

    /* 単語登録情報の設定 */
    ret = set_word_info(nj_class, &word, yomi_tmp, id_tmp, hinsi, mm_flag);
    if (ret == 2) {
        return DL_ERROR_MM_FAIL;
    } else if (ret < 0) {
        return DL_ERROR_INTERNAL;
    }

    /* 単語登録 */
    ret = njx_add_word(nj_class, &word, 2, 0);
    if (NJ_GET_ERR_CODE(ret) == NJ_ERR_SAME_WORD) {
        /* 登録する内容が重複している */
        return DL_ERROR_SAME_WORD;
    } else if (NJ_GET_ERR_CODE(ret) == NJ_ERR_USER_DIC_FULL) {
        /* 最大件数登録されている */
        return DL_ERROR_DIC_FULL;
    } else if (ret < 0) {
        /* API内部エラー */
        return DL_ERROR_INTERNAL;
    }

    return DL_ERROR_NONE;
}

/**
 * 絵文字辞書更新
 *
 * <b>処理概要</b>
 *  - パラメータチェックを行う。
 *  - 更新後の単語が既に登録されているか検索する。
 *  - 更新条件の単語を検索し、見つかれば単語削除する。
 *  - 更新後の単語登録を行う。
 *
 * @param[in,out]  demoji      絵文字辞書情報
 * @param[in]      id          絵文字ID
 * @param[in]      yomi        読み/表記
 * @param[in]      hinsi       品詞
 * @param[in]      newYomi     更新読み/表記
 * @param[in]      newHinsi    更新品詞
 * @param[in]      mm_flag     形態素解析実行フラグ(DL_MM_ON:実行する、DL_MM_OFF:実行しない)
 *
 * @retval  DL_ERROR_NONE             正常終了
 * @retval  DL_ERROR_NOT_INITIALIZED  初期化されていない
 * @retval  DL_ERROR_PARAM            引数エラー
 * @retval  DL_ERROR_SAME_WORD        登録する内容が重複している
 * @retval  DL_ERROR_MM_FAIL          形態素解析に失敗した
 * @retval  DL_ERROR_INTERNAL         API内部エラー
 */
DL_EXTERN DL_INT16 demoji_updateEmojiDictionary(DL_DEMOJI_INFO *demoji, DL_UINT16 *id, DL_UINT16 *yomi, DL_UINT16 hinsi, DL_UINT16 *newYomi, DL_UINT16 newHinsi, DL_UINT16 mm_flag) {
    NJ_CLASS       *nj_class;
    NJ_INT16        ret;
    NJ_CURSOR       cursor;
    NJ_DIC_SET      dicset;
    NJ_UINT16       yomi_len;
    NJ_UINT16       new_yomi_len;
    NJ_UINT16       id_len;
    NJ_RESULT       result;
    NJ_CHAR         yomi_tmp[NJ_MAX_EMOJI_LEN+NJ_TERM_SIZE];
    NJ_CHAR         new_yomi_tmp[NJ_MAX_EMOJI_LEN+NJ_TERM_SIZE];
    NJ_CHAR         id_tmp[NJ_MAX_EMOJI_KOUHO_LEN+NJ_TERM_SIZE];
    NJ_UINT16       del_flag = 0;
    NJ_WORD_INFO    b_word;
    NJ_WORD_INFO    a_word;
    NJ_UINT16       f_hinsi;
    NJ_UINT16       b_hinsi;

    if ((demoji == NULL) || (id == NULL) || (yomi == NULL) || (newYomi == NULL) ||
        (hinsi > DL_PART_MAX) || (newHinsi > DL_PART_MAX) || (mm_flag > 1)) {
        /* 引数エラー */
        return DL_ERROR_PARAM;
    }

    if ((demoji->iwnn == NULL) || (demoji->demojidic == NULL)) {
        /* 引数エラー */
        return DL_ERROR_PARAM;
    }

    id_len = nj_strlen(id);
    yomi_len = nj_strlen(yomi);
    new_yomi_len = nj_strlen(newYomi);
    if ((id_len > DL_MAX_ID_LEN) || (yomi_len > DL_MAX_YOMI_LEN) || (new_yomi_len > DL_MAX_YOMI_LEN)) {
        /* 引数エラー */
        return DL_ERROR_PARAM;
    }

    if (demoji->init_flg != DL_INIT_ON) {
        /* 初期化されていない */
        return DL_ERROR_NOT_INITIALIZED;
    }

    /* NJ_CLASS型にキャスト */
    nj_class = (NJ_CLASS *)demoji->iwnn;

    /* 読みをUTF16BEに変換 */
    conv_utf16be(yomi_tmp, yomi, yomi_len);

    /* 絵文字IDをUTF16BEに変換 */
    conv_utf16be(id_tmp, id, id_len);

    /* 更新後の読みをUTF16BEに変換 */
    conv_utf16be(new_yomi_tmp, newYomi, new_yomi_len);

    /* 単語登録情報の初期化  */
    clear_memory((DL_UINT8*)&a_word, sizeof(NJ_WORD_INFO));

    /* 単語登録情報に更新後情報を設定 */
    ret = set_word_info(nj_class, &a_word, new_yomi_tmp, id_tmp, newHinsi, mm_flag);
    if (ret == 2) {
        return DL_ERROR_MM_FAIL;
    } else if (ret < 0) {
        return DL_ERROR_INTERNAL;
    }

    /* 辞書検索カーソルの初期化  */
    clear_memory((DL_UINT8*)&cursor, sizeof(NJ_CURSOR));

    /* 辞書セットの初期化 */
    init_dicset(&dicset);

    /* 辞書セットに絵文字擬似辞書を設定 */
    set_demoji_giji(&dicset, 0, demoji->demojidic);

    /* 検索条件を設定 */
    cursor.cond.operation = NJ_CUR_OP_COMP;
    cursor.cond.ds = &dicset;
    cursor.cond.mode = NJ_CUR_MODE_FREQ;
    cursor.cond.yomi = a_word.yomi;

    /* 更新する単語が登録済みでないかチェックする */
    ret = njx_search_word(nj_class, &cursor);
    if (ret < 0) {
        /* API内部エラー */
        return DL_ERROR_INTERNAL;
    } else if (ret > 0) {
        /* 単語取得 */
        while(njx_get_word(nj_class, &cursor, &result) > 0) {
            f_hinsi = (NJ_UINT16)((a_word.stem.hinsi & 0xFFFF0000) >> 16);
            b_hinsi = (NJ_UINT16)((a_word.stem.hinsi & 0x0000FFFF));

            /* 更新後の内容と一致するかチェックする */
            ret = check_word_match(nj_class, &result, id_tmp, id_len, f_hinsi, b_hinsi, 1);
            if (ret == 1) {
                /* 登録する内容が重複している */
                return DL_ERROR_SAME_WORD;
            } else if (ret < 0) {
                /* API内部エラー */
                return DL_ERROR_INTERNAL;
            }
        }
    }

    /* 更新前単語検索 */
    cursor.cond.yomi = yomi_tmp;
    ret = njx_search_word(nj_class, &cursor);
    if (ret == 0) {
        /* 正常終了 */
        return DL_ERROR_NONE;
    } else if (ret < 0) {
        /* API内部エラー */
        return DL_ERROR_INTERNAL;
    }

    /* 更新前単語取得 */
    while(njx_get_word(nj_class, &cursor, &result) > 0) {
        if (hinsi == DL_PART_NONE) {
            /* 単語登録情報の初期化  */
            clear_memory((DL_UINT8*)&b_word, sizeof(NJ_WORD_INFO));

            /* 単語登録情報に更新後情報を設定 */
            ret = set_word_info(nj_class, &b_word, yomi_tmp, id_tmp, hinsi, DL_MM_OFF);
            if (ret < 0) {
                return DL_ERROR_INTERNAL;
            }

            /* 形態素解析で取得した品詞を設定する */
            f_hinsi = (NJ_UINT16)((b_word.stem.hinsi >> 16) & (0x0000FFFF));
            b_hinsi = (NJ_UINT16)(b_word.stem.hinsi & 0x0000FFFF);
        } else {
            /* 品詞をiWnn品詞に置き換える */
            conv_iwnn_hinsi(hinsi, &f_hinsi, &b_hinsi);
        }

        /* 更新前の内容と一致するかチェックする */
        ret = check_word_match(nj_class, &result, id_tmp, id_len, f_hinsi, b_hinsi, 1);
        if (ret == 1) {
            /* 更新前単語削除 */
            ret = njx_delete_word(nj_class, &result);
            if (ret < 0) {
                /* API内部エラー */
                return DL_ERROR_INTERNAL;
            }

            del_flag = 1;
            break;
        } else if (ret < 0) {
            /* API内部エラー */
            return DL_ERROR_INTERNAL;
        }
    }

    if (del_flag) {
        /* 単語登録 */
        ret = njx_add_word(nj_class, &a_word, 2, 0);
        if (ret < 0) {
            /* API内部エラー */
            return DL_ERROR_INTERNAL;
        }
    }

    return DL_ERROR_NONE;
}

/**
 * 絵文字辞書削除
 *
 * <b>処理概要</b>
 *  - パラメータチェックを行う。
 *  - 削除条件の単語を検索し、見つかれば単語削除する。
 *
 * @param[in,out]  demoji     絵文字辞書情報
 * @param[in]      id         絵文字ID
 * @param[in]      yomi       読み/表記
 * @param[in]      hinsi      品詞
 *
 * @retval  0以上                     正常終了(削除語彙数)
 * @retval  DL_ERROR_NOT_INITIALIZED  初期化されていない
 * @retval  DL_ERROR_PARAM            引数エラー
 * @retval  DL_ERROR_INTERNAL         API内部エラー
 */
DL_EXTERN DL_INT16 demoji_deleteFromEmojiDictionary(DL_DEMOJI_INFO *demoji, DL_UINT16 *id, DL_UINT16 *yomi, DL_UINT16 hinsi) {
    NJ_CLASS   *nj_class;
    NJ_INT16    ret;
    NJ_CURSOR   cursor;
    NJ_DIC_SET  dicset;
    NJ_UINT16   yomi_len;
    NJ_UINT16   id_len;
    NJ_RESULT   result;
    NJ_CHAR     yomi_tmp[NJ_MAX_EMOJI_LEN+NJ_TERM_SIZE];
    NJ_CHAR     id_tmp[NJ_MAX_EMOJI_KOUHO_LEN+NJ_TERM_SIZE];
    NJ_UINT16   del_cnt = 0;
    NJ_UINT16   f_hinsi;
    NJ_UINT16   b_hinsi;
    NJ_UINT8    hinsi_check;

    if ((demoji == NULL) || (id == NULL) || (hinsi > DL_PART_MAX)) {
        /* 引数エラー */
        return DL_ERROR_PARAM;
    }

    if ((demoji->iwnn == NULL) || (demoji->demojidic == NULL)) {
        /* 引数エラー */
        return DL_ERROR_PARAM;
    }

    id_len = nj_strlen(id);
    if (id_len > DL_MAX_ID_LEN) {
        /* 引数エラー */
        return DL_ERROR_PARAM;
    }

    if (yomi != NULL) {
        yomi_len = nj_strlen(yomi);
        if (yomi_len > DL_MAX_YOMI_LEN) {
            /* 引数エラー */
            return DL_ERROR_PARAM;
        }
    } else {
        yomi_len = 0;
    }

    if (demoji->init_flg != DL_INIT_ON) {
        /* 初期化されていない */
        return DL_ERROR_NOT_INITIALIZED;
    }

    /* NJ_CLASS型にキャスト */
    nj_class = (NJ_CLASS *)demoji->iwnn;

    /* 読みをUTF16BEに変換 */
    conv_utf16be(yomi_tmp, yomi, yomi_len);

    /* 絵文字IDをUTF16BEに変換 */
    conv_utf16be(id_tmp, id, id_len);

    /* 辞書検索カーソルの初期化  */
    clear_memory((DL_UINT8*)&cursor, sizeof(NJ_CURSOR));

    /* 辞書セットの初期化 */
    init_dicset(&dicset);

    /* 辞書セットに絵文字擬似辞書を設定 */
    set_demoji_giji(&dicset, 0, demoji->demojidic);

    /* 検索条件を設定 */
    cursor.cond.ds = &dicset;
    if (yomi_len == 0) {
        /* 読みがNULLの場合は、逆引き完全一致検索 */
        cursor.cond.operation = NJ_CUR_OP_REV;  /* 逆引き完全一致検索 */
        cursor.cond.mode = NJ_CUR_MODE_FREQ;    /* 頻度順 */
        cursor.cond.yomi = id_tmp;
    } else {
        /* 読みが指定されている場合は、正引き完全一致検索 */
        cursor.cond.operation = NJ_CUR_OP_COMP;
        cursor.cond.mode = NJ_CUR_MODE_FREQ;
        cursor.cond.yomi = yomi_tmp;
    }

    /* 単語検索 */
    ret = njx_search_word(nj_class, &cursor);
    if (ret == 0) {
        /* 削除条件に該当する単語がない */
        return 0;
    } else if (ret < 0) {
        /* API内部エラー */
        return DL_ERROR_INTERNAL;
    }

    /* 単語取得 */
    while(njx_get_word(nj_class, &cursor, &result) > 0) {
        /* 品詞をiWnn品詞に置き換える */
        conv_iwnn_hinsi(hinsi, &f_hinsi, &b_hinsi);
        
        if (hinsi == DL_PART_NONE) {
            hinsi_check = 0;
        } else {
            hinsi_check = 1;
        }

        /* 削除条件と一致するかチェックする */
        ret = check_word_match(nj_class, &result, id_tmp, id_len, f_hinsi, b_hinsi, hinsi_check);
        if (ret == 1) {
            /* 単語削除 */
            ret = njx_delete_word(nj_class, &result);
            if (ret < 0) {
                /* API内部エラー */
                return DL_ERROR_INTERNAL;
            }

            del_cnt++;

            /* 単語検索 */
            ret = njx_search_word(nj_class, &cursor);
            if (ret == 0) {
                break;
            } else if (ret < 0) {
                  /* API内部エラー */
                return DL_ERROR_INTERNAL;
            }
        } else if (ret < 0) {
              /* API内部エラー */
            return DL_ERROR_INTERNAL;
        }
    }

    return del_cnt;
}

/**
 * 解析情報サイズ取得
 *
 * <b>処理概要</b>
 *  - NJ_CLASS構造体のサイズを取得する。
 *
 * @retval iWnn解析クラスサイズ
 */
DL_EXTERN DL_UINT32 demoji_getIwnnClassSize(DL_VOID) {
    return sizeof(NJ_CLASS);
}

/************************************************/
/*              内 部 関 数 定 義               */
/************************************************/
/**
 * 辞書セット初期化
 *
 * <b>処理概要</b>
 *  - 辞書セットの初期化を行う。
 *
 * @param[in,out]  dicset       辞書セット
 *
 */
static DL_VOID init_dicset(NJ_DIC_SET *dicset) {
    DL_UINT16 i,j;

    /* 辞書セットの初期化 */
    for (i = 0; i < NJ_MAX_DIC; i++) {
        dicset->dic[i].type = NJ_DIC_H_TYPE_NORMAL;
        dicset->dic[i].limit = 0;
        dicset->dic[i].handle = NULL;
        for (j = 0; j < NJ_MAX_EXT_AREA; j++) {
            dicset->dic[i].ext_area[j] = NULL;
        }
        dicset->dic[i].dic_freq[NJ_MODE_TYPE_HENKAN].base = 10;
        dicset->dic[i].dic_freq[NJ_MODE_TYPE_HENKAN].high = 0;
        dicset->dic[i].dic_freq[NJ_MODE_TYPE_YOSOKU].base = 10;
        dicset->dic[i].dic_freq[NJ_MODE_TYPE_YOSOKU].high = 0;
        dicset->dic[i].dic_freq[NJ_MODE_TYPE_MORPHO].base = 10;
        dicset->dic[i].dic_freq[NJ_MODE_TYPE_MORPHO].high = 0;
        dicset->dic[i].srhCache = NULL;
    }
    dicset->keyword[0] = '\0';
    dicset->keyword[1] = '\0';
    dicset->rHandle[NJ_MODE_TYPE_HENKAN] = NULL;
    dicset->rHandle[NJ_MODE_TYPE_YOSOKU] = NULL;
    dicset->rHandle[NJ_MODE_TYPE_MORPHO] = NULL;

    return;
}

/**
 * 絵文字擬似辞書設定
 *
 * <b>処理概要</b>
 *  - 辞書セットの指定した位置に絵文字擬似辞書をセットする。
 *
 * @param[in,out]  dicset       辞書セット
 * @param[in]      idx          辞書セット位置
 * @param[in]      demojidic    絵文字辞書ハンドル
 *
 */
static DL_VOID set_demoji_giji(NJ_DIC_SET *dicset, DL_UINT16 idx, DL_UINT8 *demojidic) {

    dicset->dic[idx].ext_area[NJ_TYPE_EXT_AREA_DEFAULT] = demojidic;
    dicset->dic[idx].type = NJ_DIC_H_TYPE_PROGRAM;
    dicset->dic[idx].add_info[0] = NULL;
    dicset->dic[idx].add_info[1] = NULL;
    dicset->dic[idx].handle = (NJ_UINT8*)requestEmojiDictionary;
    dicset->dic[idx].dic_freq[NJ_MODE_TYPE_HENKAN].base = NJ_DEFAULT_FREQ_DIC_PROGRAM_H_BASE;
    dicset->dic[idx].dic_freq[NJ_MODE_TYPE_HENKAN].high = NJ_DEFAULT_FREQ_DIC_PROGRAM_H_HIGH;
    dicset->dic[idx].dic_freq[NJ_MODE_TYPE_YOSOKU].base = NJ_DEFAULT_FREQ_DIC_PROGRAM_Y_BASE;
    dicset->dic[idx].dic_freq[NJ_MODE_TYPE_YOSOKU].high = NJ_DEFAULT_FREQ_DIC_PROGRAM_Y_HIGH;
    dicset->dic[idx].dic_freq[NJ_MODE_TYPE_MORPHO].base = NJ_DEFAULT_FREQ_DIC_PROGRAM_M_BASE;
    dicset->dic[idx].dic_freq[NJ_MODE_TYPE_MORPHO].high = NJ_DEFAULT_FREQ_DIC_PROGRAM_M_HIGH;
    dicset->dic[idx].srhCache = NULL;
    dicset->dic[idx].limit = 0;

    return;
}

/**
 * メモリクリア
 *
 * <b>処理概要</b>
 *  - メモリアドレスからサイズ分0クリアする。
 *
 * @param[in,out]  buf    メモリアドレス
 * @param[in]      size   メモリサイズ(バイト)
 *
 */
static DL_VOID clear_memory(DL_UINT8 *buf, DL_UINT32 size) {
    DL_UINT8    *ptr;
    DL_UINT16    i;

    ptr = buf;
    for (i = 0; i < size; i++) {
        *ptr = 0x00;
        ptr++;
    }

    return;
}

/**
 * UTF16BE変換
 *
 * <b>処理概要</b>
 *  - 変換元アドレスから変換文字長分のデータをUTF16BE形式で変換先に格納する。
 *
 * @param[in,out]  dst        変換先アドレス
 * @param[in,out]  src        変換元アドレス
 * @param[in,out]  src_len    変換元文字長
 *
 */
static DL_VOID conv_utf16be(DL_UINT16 *dst, DL_UINT16 *src, DL_UINT16 src_len) {
    DL_UINT16 *ptr;
    DL_UINT16    i;

    ptr = dst;
    for (i = 0; i < src_len; i++) {
        DL_COPY_INT16_TO_CHAR(ptr, src[i]);
        ptr++;
    }
    *ptr = NJ_CHAR_NUL;

    return;
}

/**
 * iWnn品詞変換
 *
 * <b>処理概要</b>
 *  - 品詞をiWnnの前品詞、後品詞に変換する。
 *
 * @param[in]      hinsi      品詞
 * @param[in,out]  f_hinsi    前品詞
 * @param[in,out]  b_hinsi    後品詞
 *
 */
static DL_VOID conv_iwnn_hinsi(DL_UINT16 hinsi, DL_UINT16 *f_hinsi, DL_UINT16 *b_hinsi) {
    NJ_UINT16 i;
    
    for (i = 0; i < DL_PART_CNT; i++) {
        if (DL_HINSI_TBL[i][0] == hinsi) {
            *f_hinsi = DL_HINSI_TBL[i][1];
            *b_hinsi = DL_HINSI_TBL[i][2];
            break;
        }
    }

    return;
}

/**
 * 単語登録情報設定
 *
 * <b>処理概要</b>
 *  - 単語登録情報に登録内容を設定する。
 *
 * @param[in,out]  iwnn       解析クラス
 * @param[in,out]  word       単語登録情報
 * @param[in]      yomi       読み
 * @param[in]      id         絵文字ID
 * @param[in]      hinsi      品詞
 * @param[in]      mm_flag    形態素解析フラグ(0:OFF、1:ON)
 *
 * @retval   0    正常終了
 * @retval  <0    異常終了
 * @retval   2    形態素解析失敗
 */
static DL_INT16 set_word_info(NJ_CLASS *iwnn, NJ_WORD_INFO *word, DL_UINT16 *yomi, DL_UINT16 *id, DL_UINT16 hinsi, DL_UINT16 mm_flag) {
    NJ_INT16    ret;
    NJ_INT16    split_num;
    NJ_INT16    yomi_len;
    NJ_UINT16   id_len;
    NJ_CHAR     mm_yomi[NJ_MAX_LEN+NJ_TERM_SIZE];
    NJ_CHAR     mm_yomi_tmp[NJ_MAX_LEN+NJ_TERM_SIZE];
    NJ_UINT16   mm_yomi_len = 0;
    NJ_UINT16   f_hinsi;
    NJ_UINT16   b_hinsi;
    NJ_UINT8    set_yomi_flag = 0;
    NJ_UINT8    set_hinsi_flag = 0;
    NJ_UINT8    process_len;
    NJ_RESULT   mm_result[MM_MAX_MORPHO_LEN];
    NJ_UINT8    stem_len;
    NJ_CHAR*    pY;
    NJ_UINT8    i;
    static NJ_WORD_INFO  wordInfo;

    /* 全てひらがななら何もしない */
    if ((mm_flag == DL_MM_ON) && (hinsi != DL_PART_NONE)) {
        if (isAllHiragana(yomi)) {
            goto DO_NOTHING;
        }
    }

    if ((mm_flag == DL_MM_ON) || (hinsi == DL_PART_NONE)) {
        /* 形態素解析を行う */
        split_num = mmx_split_word(iwnn, yomi, &process_len, &mm_result[0]);
        if (split_num < 0) {
            /* 異常終了 */
            return -1;
        } else if (split_num > 0) {
            pY = mm_yomi;
            for (i = 0; i < split_num; i++) {
                /* 形態素解析読みを取得 */
                if (mm_flag == DL_MM_ON) {
                    yomi_len = mmx_get_info(iwnn, &mm_result[i], mm_yomi_tmp, sizeof(mm_yomi_tmp), &stem_len, NULL);
                    if (yomi_len < 0) {
                        /* 異常終了 */
                        return -1;
                    } else if (yomi_len == 0) {
                        /* 文節1つでも形態素解析失敗→登録失敗とする */
                        return 2;
                    }
                    mm_yomi_len += yomi_len;
                    if (mm_yomi_len > DL_MAX_YOMI_LEN) {
                        break;
                    }
                    nj_strncpy(pY, mm_yomi_tmp, yomi_len);
                    pY += yomi_len;
                }

                if (hinsi == DL_PART_NONE) {
                    /* 最後の自立語の語彙の品詞を設定 */
                    ret = njx_get_word_info(iwnn, &mm_result[i], &wordInfo);
                    if (ret >= 0 && wordInfo.stem.hinsi != 0) {
                        /* iWnn品詞をIPA品詞に丸める */
                        /*
                         * ※ここで、指定されたIPA品詞に含まれるiWnn品詞であれば
                         * 丸めずにそのiWnn品詞をそのまま適用することも考えられる。
                         */
                        hinsi = hinsiTblFromIWnn[wordInfo.stem.hinsi & 0x0000FFFF];

                        /* 品詞をiWnn品詞に置き換える */
                        conv_iwnn_hinsi(hinsi, &f_hinsi, &b_hinsi);
                        set_hinsi_flag = 1;
                    }
                }
            } /* end of loop for split words */

            *pY = NJ_CHAR_NUL;

            if (mm_flag == DL_MM_ON) {
                if ((mm_yomi_len > 0) && (mm_yomi_len <= DL_MAX_YOMI_LEN)) {
                    if(nj_strcpy(word->yomi, mm_yomi) == NULL) {
                        /* 異常終了 */
                        return -1;
                    }
                    word->stem.yomi_len = nj_strlen(mm_yomi);

                    set_yomi_flag = 1;
                } else {
                    /* 形態素解析結果の読み文字長が許容範囲外→登録失敗とする */
                    return 2;
                }
            }
        } else {
            if (mm_flag == DL_MM_ON) {
                /* 形態素解析失敗→登録失敗とする */
                return 2;
            }
        }
    }

DO_NOTHING:
    if (set_yomi_flag != 1) {
        /* 読みを設定 */
        if(nj_strcpy(word->yomi, yomi) == NULL) {
            /* 異常終了 */
            return -1;
        }
        word->stem.yomi_len = nj_strlen(yomi);
    }

    if (set_hinsi_flag != 1) {
        if (hinsi == DL_PART_NONE) {
            hinsi = DL_PART_CATEGORY_1;
        }

        /* 品詞をiWnn品詞に置き換える */
        conv_iwnn_hinsi(hinsi, &f_hinsi, &b_hinsi);
    }

    /* 品詞グループを詳細に設定 */
    word->hinsi_group = (DL_UINT8)NJ_HINSI_DETAIL;

    /* 品詞を設定 */
    word->stem.hinsi = (word->stem.hinsi & 0x0000FFFF) | (NJ_UINT32)((f_hinsi << 16) & 0xFFFFFFFF);
    word->stem.hinsi = (word->stem.hinsi & 0xFFFF0000) | (NJ_UINT32)b_hinsi;

    /* 候補に絵文字IDを設定 */
    if (nj_strcpy(word->kouho, id) == NULL) {
        /* 異常終了 */
        return -1;
    }
    id_len = nj_strlen(id);
    word->stem.kouho_len = id_len;

    return 0;
}

/**
 * 単語取得処理結果チェック
 *
 * <b>処理概要</b>
 *  - 単語取得した処理結果がパラメータの絵文字ID、品詞と一致するかチェックする。
 *
 * @param[in,out]  iwnn         解析クラス
 * @param[in]      result       単語取得処理結果
 * @param[in]      id           絵文字ID
 * @param[in]      id_len       絵文字ID文字長
 * @param[in]      f_hinsi      前品詞
 * @param[in]      b_hinsi      後品詞
 * @param[in]      hinsi_check  品詞チェック(0:チェックなし、1:チェックあり)
 *
 * @retval   1    条件に一致する
 * @retval   0    条件に一致しない
 * @retval  <0    異常終了
 */
static DL_INT16 check_word_match(NJ_CLASS *iwnn, NJ_RESULT *result, DL_UINT16 *id, DL_UINT16 id_len, DL_UINT16 f_hinsi, DL_UINT16 b_hinsi, DL_UINT8 hinsi_check) {
    NJ_INT16    hlen;
    NJ_CHAR     cand[NJ_MAX_RESULT_LEN + NJ_TERM_SIZE];
    NJ_UINT32   fpos;
    NJ_UINT32   bpos;

    /* 候補文字列取得 */
    hlen = njx_get_candidate(iwnn, result, cand, (NJ_UINT16)sizeof(cand));
    if (hlen < 0) {
        /* 異常終了 */
        return -1;
    }
    if (nj_strncmp(cand, (NJ_CHAR*)id, id_len) == 0) {
        /* 条件の絵文字IDと一致した */
        if (hinsi_check == 0) {
            /* 条件に一致する */
            return 1;
        }

        /* 条件の品詞と一致するかチェックする */
        fpos = NJ_GET_FPOS_FROM_STEM(&(result->word));
        bpos = NJ_GET_BPOS_FROM_STEM(&(result->word));
        if (fpos == f_hinsi && bpos == b_hinsi) {
            /* 条件に一致する */
            return 1;
        }
    }

    return 0;
}

/**
 * ひらがな判定
 *
 * <b>処理概要</b>
 *  - パラメータの読み文字がひらがなのみかを判定する
 *
 * @param[in]      yomi       読み
 *
 * @retval   1    ひらがなのみ
 * @retval   0    ひらがな以外が含まれる
 */
static DL_INT16 isAllHiragana(DL_UINT16 *yomi) {
    DL_UINT8 *cp;
    DL_UINT16 aChar;
    DL_INT16 ret = 1;

    cp = (DL_UINT8*)yomi;

    aChar = (cp[0] << 8) | (cp[1] & 0x00FF);

    while (aChar != 0x0000) {
        /* ひらがな以外か */
        /* 10キーで入力できる文字で、読みにふさわしい文字。数字、alphabetは除く */
        if ((aChar < 0x3041) || (aChar > 0x3094)) {
            ret = 0;
            break;
        }
        cp += 2;
        aChar = (cp[0] << 8) | (cp[1] & 0x00FF);
    }
    return ret;
}

