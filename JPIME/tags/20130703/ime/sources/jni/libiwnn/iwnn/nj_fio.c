/**
 * @file
 *   共通関数(ファイルアクセス関数)
 *
 *   ファイルアクセス関数を提供する。 
 *
 * @attention
 *   関数群は、エンジンが動作するシステムにあわせてカスタマイズをする必要がある。
 *
 * @author
 *   Copyright (C) OMRON SOFTWARE Co., Ltd. 2008-2011 All Rights Reserved.
 */

/************************************************/
/*           インクルードファイル               */
/************************************************/
#ifdef NJ_OPT_DIC_STORAGE
#include <stdio.h>
#endif /* NJ_OPT_DIC_STORAGE */
#include "nj_lib.h"
#include "nj_err.h"
#include "nj_dic.h"
#include "nj_dicif.h"
#include "njfilter.h"
#include "nj_ext.h"
#include "njd.h"

/************************************************/
/*              prototype  宣  言               */
/************************************************/

/************************************************/
/*              extern  宣  言                  */
/************************************************/

/**********************************************************************
 * function
 **********************************************************************/

/**
 * ファイルポインタからデータを読み込む
 *
 * 標準関数fread()
 *
 * @param[out]   buffer     格納バッファ
 * @param[in]    size       読み込みサイズ(byte)
 * @param[in]    filestream ファイルポインタ
 *
 * @retval   >0 正常終了
 * @retval   =0 異常終了
 */
NJ_INT32 nj_fread(NJ_UINT8 *buffer, NJ_UINT32 size, NJ_FILE *filestream) {


#ifdef NJ_OPT_DIC_STORAGE
    return (NJ_INT32)fread(buffer, size, 1, (FILE*)filestream);
#else  /* NJ_OPT_DIC_STORAGE */
    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_FREAD, NJ_ERR_OPTION_STORAGE_OFF);
#endif /* NJ_OPT_DIC_STORAGE */
}


/**
 * 指定された位置へファイルポインタを移動する
 *
 * 標準関数fseek()
 *
 * @param[in]    filestream ファイルポインタ
 * @param[in]    offset     originからの移動バイト数
 * @param[in]    origin     初期位置
 *
 * @retval =0    正常
 * @retval !=0   エラー
 */
NJ_INT32 nj_fseek(NJ_FILE *filestream, NJ_UINT32 offset, NJ_INT32 origin) {


#ifdef NJ_OPT_DIC_STORAGE
    return (NJ_INT32)fseek((FILE*)filestream, offset, origin);
#else  /* NJ_OPT_DIC_STORAGE */
    return NJ_SET_ERR_VAL(NJ_FUNC_NJ_FSEEK, NJ_ERR_OPTION_STORAGE_OFF);
#endif /* NJ_OPT_DIC_STORAGE */
}


/**
 * 指定された位置から指定バイト数のデータを読み込む
 *
 *
 * @param[in]    fdicinfo   ストレージ辞書情報
 * @param[in]    offset     originからの移動バイト数
 * @param[in]    size       offsetからの読み込みバイト数
 * @param[out]   dst        出力領域
 *
 * @retval >=0  正常
 * @retval <0   エラー
 */
NJ_INT32 njd_offset_fread(NJ_STORAGE_DIC_INFO *fdicinfo, NJ_UINT32 offset, NJ_UINT32 size, NJ_UINT8 *dst) {


#ifdef NJ_OPT_DIC_STORAGE
    /* 先頭から指定offset分、ファイルポインタをシークする */
    if (nj_fseek(fdicinfo->filestream, offset, NJ_FILE_IO_SEEK_SET) != 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_OFFSET_FREAD, NJ_ERR_STREAM_SEEK_ERR);    /* NCH_FB */
    }

    /* 指定offsetから指定バイト数分データを読み込む */
    if (nj_fread(dst, size, fdicinfo->filestream) == 0) {
        return NJ_SET_ERR_VAL(NJ_FUNC_NJD_OFFSET_FREAD, NJ_ERR_STREAM_READ_ERR);    /* NCH_FB */
    }

    return 1;
#else  /* NJ_OPT_DIC_STORAGE */
    return NJ_SET_ERR_VAL(NJ_FUNC_NJD_OFFSET_FREAD, NJ_ERR_OPTION_STORAGE_OFF);
#endif /* NJ_OPT_DIC_STORAGE */
}


/**
 * 指定された位置から指定バイト数のデータを読み込む
 *
 * @attention
 *   本関数では、fdicinfoに用意している一時キャッシュ領域にデータを読み込み、
 *   一時キャッシュ領域のポインタをdstに格納して返します。
 *
 * @param[in]    fdicinfo   ストレージ辞書情報
 * @param[in]    index      キャッシュインデックス
 * @param[in]    offset     originからの移動バイト数
 * @param[in]    size       offsetからの読み込みバイト数
 * @param[out]   dst        出力領域
 *
 * @retval >=0  正常
 * @retval <0   エラー
 */
NJ_INT32 njd_offset_fread_cache(NJ_STORAGE_DIC_INFO *fdicinfo, NJ_UINT32 index, NJ_UINT32 offset, NJ_UINT32 size, NJ_UINT8 **dst) {
#ifdef NJ_OPT_DIC_STORAGE
    NJ_STORAGE_DIC_CACHE_HYOKI_INDEX  *cache_hyoki_index;
    NJ_STORAGE_DIC_CACHE_WORD_DATA    *cache_word_data;
    NJ_STORAGE_DIC_CACHE_YOMI_HYOKI   *cache_yomi_hyoki;
    NJ_UINT8 *ptr;
    NJ_UINT8  hash_idx;
    NJ_UINT32 offset_tmp;
    NJ_UINT32 offset_diff;


    /* 各領域毎に読み込むバッファを設定する */
    /* 単語データ領域の場合 */
    if (index == NJ_SDIC_STORAGE_WORD_DATA) {
        if ((fdicinfo->mode & NJ_STORAGE_MODE_ONMEMORY_WORD_DATA) != 0) {
            /* オンメモリの場合は、メモリを使用する */
            offset_diff = 0;
            ptr         = (NJ_UINT8*)(fdicinfo->cache_area[index]);

            /* 辞書先頭からのオフセットを取り出し */
            offset_tmp  = NJ_INT32_READ(ptr);
            ptr += sizeof(NJ_UINT32);

            ptr += offset - offset_tmp;
        } else {
            if (size < (NJ_SDIC_HASH_WORD_DATA_SIZE / 2)) {
                offset_diff = offset % (NJ_SDIC_HASH_WORD_DATA_SIZE / 2);
                offset_tmp = offset - offset_diff;
            } else {
                offset_diff = 0;        /* NCH_FB */
                offset_tmp = offset;    /* NCH_FB */
            }

            /* offsetからバッファのハッシュIndexを取得する */
            hash_idx = (NJ_UINT8)((NJ_SDIC_HASH_SIZE - 1) & (offset_tmp ^ (offset_tmp >> 8) ^ (offset_tmp >> 16) ^ (offset_tmp >> 24)));

            /* 単語データ領域 */
            cache_word_data = (NJ_STORAGE_DIC_CACHE_WORD_DATA*)fdicinfo->cache_area[index];

            /* 読み込み領域を指定 */
            ptr = &(cache_word_data->hash_data[hash_idx].data[0]);

            if (offset_tmp != cache_word_data->hash_data[hash_idx].offset) {
                /* 先頭から指定offset分、ファイルポインタをシークする */
                if (nj_fseek(fdicinfo->filestream, offset_tmp, NJ_FILE_IO_SEEK_SET) != 0) {
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJD_OFFSET_FREAD, NJ_ERR_STREAM_SEEK_ERR);    /* NCH_FB */
                }

                /* 指定offsetから指定バイト数分データを読み込む */
                if (nj_fread(ptr,
                             ((offset_tmp +  NJ_SDIC_HASH_WORD_DATA_SIZE) > fdicinfo->dicsize) ? (fdicinfo->dicsize - offset_tmp) : NJ_SDIC_HASH_WORD_DATA_SIZE,
                             fdicinfo->filestream) == 0) {
                    /* ハッシュが破壊された可能性があるのでoffsetを初期値に設定する */
                    cache_word_data->hash_data[hash_idx].offset = 0;                            /* NCH_FB */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJD_OFFSET_FREAD, NJ_ERR_STREAM_READ_ERR);    /* NCH_FB */
                } else {
                    cache_word_data->hash_data[hash_idx].offset = offset_tmp;
                }
            }
        }

    /* 読み・表記領域の場合 */
    } else if (index == NJ_SDIC_STORAGE_YOMI_HYOKI) {
        if ((fdicinfo->mode & NJ_STORAGE_MODE_ONMEMORY_STRING_DATA) != 0) {
            offset_diff = 0;
            ptr         = (NJ_UINT8*)(fdicinfo->cache_area[index]);

            /* 辞書先頭からのオフセットを取り出し */
            offset_tmp  = NJ_INT32_READ(ptr);
            ptr += sizeof(NJ_UINT32);

            ptr += offset - offset_tmp;
        } else {
            if (size < (NJ_SDIC_HASH_YOMI_HYOKI_SIZE / 2)) {
                offset_diff = offset % (NJ_SDIC_HASH_YOMI_HYOKI_SIZE / 2);
                offset_tmp = offset - offset_diff;
            } else {
                offset_diff = 0;        /* NCH_FB */
                offset_tmp = offset;    /* NCH_FB */
            }

            /* offsetからバッファのハッシュIndexを取得する */
            hash_idx = (NJ_UINT8)((NJ_SDIC_HASH_SIZE - 1) & (offset_tmp ^ (offset_tmp >> 8) ^ (offset_tmp >> 16) ^ (offset_tmp >> 24)));

            /* 読み表記領域 */
            cache_yomi_hyoki = (NJ_STORAGE_DIC_CACHE_YOMI_HYOKI*)fdicinfo->cache_area[index];

            /* 読み込み領域を指定 */
            ptr  = &(cache_yomi_hyoki->hash_data[hash_idx].data[0]);

            if (offset_tmp != cache_yomi_hyoki->hash_data[hash_idx].offset) {
                /* 先頭から指定offset分、ファイルポインタをシークする */
                if (nj_fseek(fdicinfo->filestream, offset_tmp, NJ_FILE_IO_SEEK_SET) != 0) {
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJD_OFFSET_FREAD, NJ_ERR_STREAM_SEEK_ERR);    /* NCH_FB */
                }

                /* 指定offsetから指定バイト数分データを読み込む */
                if (nj_fread(ptr,
                             ((offset_tmp + NJ_SDIC_HASH_YOMI_HYOKI_SIZE) > fdicinfo->dicsize) ? (fdicinfo->dicsize - offset_tmp) : NJ_SDIC_HASH_YOMI_HYOKI_SIZE,
                             fdicinfo->filestream) == 0) {
                    /* ハッシュが破壊された可能性があるのでoffsetを初期値に設定する */
                    cache_yomi_hyoki->hash_data[hash_idx].offset = 0;                           /* NCH_FB */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJD_OFFSET_FREAD, NJ_ERR_STREAM_READ_ERR);    /* NCH_FB */
                } else {
                    cache_yomi_hyoki->hash_data[hash_idx].offset = offset_tmp;
                }
            }
        }

    /* 表記文字列インデックス領域の場合 */
    } else if (index == NJ_SDIC_STORAGE_HYOKI_INDEX) {
        /* 領域が存在しない場合があるため、チェックする */
        if (fdicinfo->cache_area[index] == NULL) {
            return NJ_SET_ERR_VAL(NJ_FUNC_NJD_OFFSET_FREAD, NJ_ERR_FORMAT_INVALID);    /* NCH_FB */
        }

        if ((fdicinfo->mode & NJ_STORAGE_MODE_ONMEMORY_MORPHO_DATA) != 0) {
            offset_diff = 0;
            ptr         = (NJ_UINT8*)(fdicinfo->cache_area[index]);

            /* 辞書先頭からのオフセットを取り出し */
            offset_tmp  = NJ_INT32_READ(ptr);
            ptr += sizeof(NJ_UINT32);

            ptr += offset - offset_tmp;
        } else {
            if (size < (NJ_SDIC_HASH_HYOKI_INDEX_SIZE / 2)) {                                    /* NCH_FB */
                offset_diff = offset % (NJ_SDIC_HASH_HYOKI_INDEX_SIZE / 2);                      /* NCH_FB */
                offset_tmp = offset - offset_diff;                                               /* NCH_FB */
            } else {
                offset_diff = 0;                                                                 /* NCH_FB */
                offset_tmp = offset;                                                             /* NCH_FB */
            }

            /* offsetからバッファのハッシュIndexを取得する */
            hash_idx = (NJ_UINT8)((NJ_SDIC_HASH_SIZE - 1) & (offset_tmp ^ (offset_tmp >> 8) ^ (offset_tmp >> 16) ^ (offset_tmp >> 24)));    /* NCH_FB */

            /* 表記文字列インデックス */
            cache_hyoki_index = (NJ_STORAGE_DIC_CACHE_HYOKI_INDEX*)fdicinfo->cache_area[index];    /* NCH_FB */

            /* 読み込み領域を指定 */
            ptr = &(cache_hyoki_index->hash_data[hash_idx].data[0]);                             /* NCH_FB */

            if (offset_tmp != cache_hyoki_index->hash_data[hash_idx].offset) {                   /* NCH_FB */
                /* 先頭から指定offset分、ファイルポインタをシークする */
                if (nj_fseek(fdicinfo->filestream, offset_tmp, NJ_FILE_IO_SEEK_SET) != 0) {      /* NCH_FB */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJD_OFFSET_FREAD, NJ_ERR_STREAM_SEEK_ERR);     /* NCH_FB */
                }

                /* 指定offsetから指定バイト数分データを読み込む */
                if (nj_fread(ptr,
                             ((offset_tmp + NJ_SDIC_HASH_HYOKI_INDEX_SIZE) > fdicinfo->dicsize) ? (fdicinfo->dicsize - offset_tmp) : NJ_SDIC_HASH_HYOKI_INDEX_SIZE,
                             fdicinfo->filestream) == 0) {                                       /* NCH_FB */
                    /* ハッシュが破壊された可能性があるのでoffsetを初期値に設定する */
                    cache_hyoki_index->hash_data[hash_idx].offset = 0;                           /* NCH_FB */
                    return NJ_SET_ERR_VAL(NJ_FUNC_NJD_OFFSET_FREAD, NJ_ERR_STREAM_READ_ERR);     /* NCH_FB */
                } else {
                    cache_hyoki_index->hash_data[hash_idx].offset = offset_tmp;                  /* NCH_FB */
                }
            }
        }
    }

    *dst = ptr + offset_diff;

    return 1;
#else  /* NJ_OPT_DIC_STORAGE */
    return NJ_SET_ERR_VAL(NJ_FUNC_NJD_OFFSET_FREAD_CACHE, NJ_ERR_OPTION_STORAGE_OFF);
#endif /* NJ_OPT_DIC_STORAGE */
}
