/**
 * @file
 *   iwnn_utils.h
 *
 * @author
 *   OMRON SOFTWARE Co., Ltd.
 *
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 * (C) Copyright OMRON SOFTWARE Co., Ltd. 2009 All Rights Reserved
 */
#ifndef __IWNN_UTILS_H__
#define __IWNN_UTILS_H__

#include <sys/mman.h>

/**
 * Wrapper Macro for System Calls
 *
 *   Return EINTR Error, if catches Signal before the System Call is accepted.
 *   * Retry to request System Calls when it fails
 */
#define SYSCALL_RETRY3(result, expr, check)     \
    {                                           \
        int retry = 0;                          \
        do {                                    \
            if (retry == 5) {                   \
                break;                          \
            }                                   \
            (result) = (expr);                  \
            if ((check) && (errno == EINTR)) {  \
                errno = 0;                      \
                retry++;                        \
            }                                   \
            else {                              \
                break;                          \
            }                                   \
        } while (1);                            \
    }

#define SYSCALL_RETRY(result, expr) \
    SYSCALL_RETRY3(result, expr, (result < 0))

/** Length of a file path */
#define FMAP_MAX_PATH  256
/** Path to the learning, user and frequency dictionary */
#define IWNN_DICSET_BACKUP_MASTER_PATH "%s/dicset/master/%s"
/** Backup path to the learning, user and frequency dictionary */
#define IWNN_DICSET_BACKUP_TMP_PATH "%s/dicset/tmp/%s"
#define IWNN_DICSET_BACKUP_REFERENCE_MASTER_PATH "%s/dicset/master/%03d_%03d_%s"
#define IWNN_DICSET_BACKUP_REFERENCE_TMP_PATH "%s/dicset/tmp/%03d_%03d_%s"
/** Path to the learning, user and frequency dictionary for Service */
#define IWNN_DICSET_BACKUP_SERVICE_MASTER_PATH "%s/dicset/service/%s_%s/%s"
#define IWNN_DICSET_BACKUP_SERVICE_TMP_PATH "%s/dicset/servicetmp/%s_%s/%s"
/** Backup path to the learning, user and frequency dictionary for Service*/
#define IWNN_DICSET_BACKUP_REFERENCE_SERVICE_MASTER_PATH "%s/dicset/service/%s_%s/%03d_%03d_%s"
#define IWNN_DICSET_BACKUP_REFERENCE_SERVICE_TMP_PATH "%s/dicset/servicetmp/%s_%s/%03d_%03d_%s"

/** File map control structure */
typedef struct {
    char path[FMAP_MAX_PATH];   /**< file name         */
    int fd;                     /**< file descriptor   */
    char *addr;                 /**< pointer           */
    size_t size;                /**< size              */
    int flags;                  /**< open flags        */
    int mode;                   /**< open mode         */
    int reference_count;        /**< reference counter */
} fmap_t;

extern int mkdirs(const char *path, mode_t mode);
extern int fmap_recovery(const char *master_path, const char *tmp_path, size_t size);
extern int fmap_backup(fmap_t *mfmap, const char *path);
extern int fmap_save(char *buffer, size_t size, const char *path);
extern int fmap_check(const char *path, size_t size);
extern int fmap_copy(char *buffer, size_t size, const char *path);
extern int fmap_open(fmap_t *fmap, const char *path, int flags, mode_t mode, size_t size);
extern int fmap_close(fmap_t *fmap);
extern int fmap_sync(fmap_t *fmap, int flags);
extern int fmap_advise(fmap_t *fmap, int advise);

#endif /* __IWNN_UTILS_H__ */

