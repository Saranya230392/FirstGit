/**
 * @file
 *   iWnn Utilities.
 *
 * @author
 *   OMRON SOFTWARE Co., Ltd.
 *
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 * (C) Copyright OMRON SOFTWARE Co., Ltd. 2009 All Rights Reserved
 */
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>

#include "iwnn_utils.h"

#define LOG_TAG "iWnn"
#include "Log.h"


static int log_trace = 0;

static void *mmap_retry(void *addr, size_t length, int prot, int flags, int fd, off_t offset);


/**
 * Make a directory
 *
 * @param  path: Path of the directory
 * @param  mode: Permissions
 *
 * @retval  0  success
 * @retval -1  error
 */
int mkdirs(const char *path, mode_t mode)
{
    char parent_path[FMAP_MAX_PATH];
    char *pos;


    strncpy(parent_path, path, FMAP_MAX_PATH);
    parent_path[FMAP_MAX_PATH - 1] = '\0';

    pos = strrchr(parent_path, '/');
    if (pos == NULL) {
        return -1;
    }

    *pos = '\0';

    if (mkdir(parent_path, mode) < 0) {
        if (errno == EEXIST) {
            return 0;
        }
        else if (errno == ENOENT) {
            if (mkdirs(parent_path, mode) < 0) {
                return -1;
            }

            if (mkdir(parent_path, mode) < 0) {
                return -1;
            }
        }
        else {
            return -1;
        }
    }
    return 0;
}

/**
 * Recover a file from backup files
 *
 * @param  master_path: File to recover
 * @param  tmp_path   : Backup file path
 * @param  size       : Size of a file
 *
 * @retval  0  success
 * @retval -1  error
 */
int fmap_recovery(const char *master_path, const char *tmp_path, size_t size)
{
    fmap_t tfmap;
    int result;

    SYSCALL_RETRY(result, unlink(master_path));

    if (fmap_check(tmp_path, size) < 0) {
        return -1;
    }

    if (fmap_open(&tfmap, tmp_path, O_RDONLY, S_IRUSR|S_IWUSR, size) < 0) {
        return -1;
    }

    if (fmap_save(tfmap.addr, tfmap.size, master_path) < 0) {
        (void)fmap_close(&tfmap);
        return -1;
    }

    (void)fmap_close(&tfmap);
    SYSCALL_RETRY(result, unlink(tmp_path));

    return 0;
}

/**
 * Backup a file
 *
 * @param  mfmap: File map control structure
 * @param  path : Backup file path
 *
 * @retval  0  success
 * @retval -1  error
 */
int fmap_backup(fmap_t *mfmap, const char *path)
{
    fmap_t fmap;
    int result;

    if (fmap_save(mfmap->addr, mfmap->size, path) < 0) {
        LOGE_IF(log_trace, "fmap_backup -- failed to save path=\"%s\" errno=%d\n", path, errno);
    }

    if (fmap_sync(mfmap, MS_SYNC) < 0) {
        LOGE_IF(log_trace, "fmap_backup -- failed to sync path=\"%s\" errno=%d\n", path, errno);
        return -1;
    }

    SYSCALL_RETRY(result, unlink(path));

    return 0;
}

/** Retry count for backup */
#define IWNN_BACKUP_RETRY_MAX 2
/**
 * Save a buffer to the file
 *
 * @param  buffer: Buffer
 * @param  size  : Size
 * @param  path  : Saving file path
 *
 * @retval  0  success
 * @retval -1  error
 */
int fmap_save(char *buffer, size_t size, const char *path)
{
    int retry;
    int result;

    retry = 0;
    do {
        result = fmap_copy(buffer, size, path);
        if (result == 0) {
            break;
        }

        retry++;

    } while (retry < IWNN_BACKUP_RETRY_MAX);

    return result;
}

/**
 * Check a file size if it matches
 *
 * @param  path: File path
 * @param  size: File size
 *
 * @retval  0  success
 * @retval -1  error
 */
int fmap_check(const char *path, size_t size)
{
    struct stat sb;
    int result;

    SYSCALL_RETRY(result, stat(path, &sb));
    if (result < 0) {
        result = errno;
        LOGE_IF(log_trace, "fmap_check -- failed to stat  path=\"%s\" errno=%d\n", path, errno);
        errno = result;
        return -1;
    }

    if (sb.st_size != size) {
        LOGE_IF(log_trace, "fmap_check -- failed to size  path=\"%s\" (%lld) != (%d)\n", path, sb.st_size, size);
        errno = EINVAL;
        return -1;
    }

    return 0;
}

/**
 * Copy a buffer to the file
 *
 * @param  buffer: Buffer
 * @param  size  : Size
 * @param  path  : Saving file path
 *
 * @retval  0  success
 * @retval -1  error
 */
int fmap_copy(char *buffer, size_t size, const char *path)
{
    fmap_t fmap;
    int result;
    int error = 0;


    SYSCALL_RETRY(result, unlink(path));
    if (result < 0) {
        if (errno != ENOENT) {
            error = errno;
            LOGE_IF(log_trace, "fmap_copy -- failed to unlink  path=\"%s\" errno=%d\n", path, errno);
            errno = error;
            return -1;
        }
    }

    mkdirs(path, S_IRWXU);

    if (fmap_open(&fmap, path, O_CREAT|O_RDWR, S_IRUSR|S_IWUSR, size) < 0) {
        return -1;
    }

    memcpy(fmap.addr, buffer, fmap.size);

    if (fmap_sync(&fmap, MS_SYNC) < 0) {
        error = errno;
        fmap_close(&fmap);
        SYSCALL_RETRY(result, unlink(path));
        errno = error;
        return -1;
    }

    if (fmap_close(&fmap) < 0) {
        error = errno;
        SYSCALL_RETRY(result, unlink(path));
        errno = error;
        return -1;
    }

    if (fmap_check(path, size) < 0) {
        error = errno;
        SYSCALL_RETRY(result, unlink(path));
        errno = error;
        return -1;
    }

    return 0;
}

/**
 * Open a file
 *
 * @param  fmap : File map control structure
 * @param  path : File path
 * @param  flags: Open flags
 * @param  mode : Open mode
 * @param  size : File size
 *
 * @retval  0  success
 * @retval -1  error
 */
int fmap_open(fmap_t *fmap, const char *path, int flags, mode_t mode, size_t size)
{
    int fd;
    struct stat sb;
    size_t length = 0;
    int prot = 0;
    char *p;
    int shared = MAP_PRIVATE;
    int result;
    int error = 0;


    LOGI_IF(log_trace, "fmap_open(\"%s\",flags=0x%x,mode=0x%x,size=%d)\n", path, flags, mode, size);

    if (fmap == NULL) {
        errno = EINVAL;
        return -1;
    }

    if (size == 0) {
        errno = EINVAL;
        return -1;
    }

    SYSCALL_RETRY(fd, open(path, flags, mode));
    if (fd < 0) {
        error = errno;
        LOGE_IF(log_trace, "  -- faild to open %d\n", error);
        errno = error;
        return -1;
    }

    if ((flags & O_CREAT) != 0) {
        char c;
        off_t offset;
        ssize_t written_size;

        length = size;
        SYSCALL_RETRY(offset, lseek(fd, (length - 1), SEEK_SET));
        if (offset < 0) {
            error = errno;
            SYSCALL_RETRY(result, close(fd));
            LOGE_IF(log_trace, "  -- failed to lseek %d\n", error);
            errno = error;
            return -1;
        }

        c = 0;
        SYSCALL_RETRY(written_size, write(fd, &c, sizeof(char)));
        if (written_size < 0) {
            error = errno;
            SYSCALL_RETRY(result, close(fd));
            LOGE_IF(log_trace, "  -- failed to write %d\n", error);
            errno = error;
            return -1;
        }
    } else {
        SYSCALL_RETRY(error, fstat(fd, &sb));
        if (error < 0) {
            error = errno;
            SYSCALL_RETRY(result, close(fd));
            LOGE_IF(log_trace, "  -- failed to fstat %d\n", error);
            errno = error;
            return -1;
        }
        length = sb.st_size;
    }

    switch(flags & O_ACCMODE) {
        case O_RDONLY: /* Map the file as read-only. The mapped data can be modified, but it is not reflected to the file. */
            prot   = PROT_READ;
            shared = MAP_PRIVATE;
            break;
        case O_WRONLY: /* Map the file as write-only. The mapped data can be modified, and it is reflected to the file. */
            prot   = PROT_WRITE;
            shared = MAP_SHARED;
            break;
        case O_RDWR:   /* Map the file for read and write. The mapped data can be modified, and it is reflected to the file. */
            prot   = PROT_READ | PROT_WRITE;
            shared = MAP_SHARED;
            break;
        default:
            break;    /* unreachable */
    }

    p = mmap_retry(0, length, prot, shared, fd, 0);
    if (p == MAP_FAILED) {
        error = errno;
        SYSCALL_RETRY(result, close(fd));
        LOGE_IF(log_trace, "  -- failed to mmap %d\n", error);
        errno = error;
        return -1;
    }

    SYSCALL_RETRY(result, close(fd));
    if (result < 0) {
        LOGE_IF(log_trace, "fmap_open -- failed to close\n");
    }

    fd = -1;
    fmap->fd = fd;
    fmap->addr = p;
    fmap->size = length;
    fmap->flags = flags;
    fmap->mode = mode;

    size_t copy_size = sizeof(fmap->path);
    size_t path_size = strlen(path);
    if (copy_size > path_size) {
        copy_size = path_size;
    }

    memcpy(fmap->path, path, copy_size);

    if ((flags & O_CREAT) == O_CREAT) {
        (void)fmap_sync(fmap, MS_SYNC);
    }

    LOGI_IF(log_trace, "fmap_open() fd=%d addr=%p size=%d flags=0x%x mode=0x%x\n", 
            fmap->fd, fmap->addr, fmap->size, fmap->flags, fmap->mode);

    return 0;
}

/**
 * Close a file
 *
 * @param  fmap: File map control structure
 *
 * @retval  0  success
 * @retval -1  error
 */
int fmap_close(fmap_t *fmap)
{
    char *addr;
    size_t size;
    int result;


    if (fmap == NULL) {
        errno = EINVAL;
        return -1;
    }

    LOGI_IF(log_trace, "fmap_close -- path=\"%s\"\n", fmap->path);

    addr = fmap->addr;
    size = fmap->size;

    fmap->path[0] = 0;
    fmap->addr = NULL;
    fmap->size = 0;
    fmap->flags = 0;
    fmap->mode = 0;

    if ((addr == NULL) || (size == 0)) {
        LOGE_IF(log_trace, "fmap_close -- fmap not found\n");
        return 0;
    }

    SYSCALL_RETRY(result, munmap(addr, size));
    if (result < 0) {
        result = errno;
        LOGE_IF(log_trace, "munmup - failed %d\n", errno);
        errno = result;
        return -1;
    }

    LOGI_IF(log_trace, "fmap_close -- success\n");

    return 0;
}

/**
 * Synchronize a file and mapped memory
 *   It's reflected immediately when mapping files only with PROT_WRITE and MAP_SHARED
 *
 * @param  fmap : File map control structure
 * @param  flags: msync flag
 *
 * @retval  0  success
 * @retval -1  error
 */
int fmap_sync(fmap_t *fmap, int flags)
{
    int result;

    if (fmap == NULL) {
        errno = EINVAL;
        return -1;
    }

    SYSCALL_RETRY(result, msync(fmap->addr, fmap->size, flags));
    if (result < 0) {
        result = errno;
        LOGE_IF(log_trace, "msync - failed %d\n", errno);
        errno = result;
        return -1;
    }

    LOGI_IF(log_trace, "fmap_sync -- success  path=\"%s\" flags=%d\n", fmap->path, flags);

    return 0;
}

/**
 * give advice about use of memory
 *
 * @param  fmap  : File map control structure
 * @param  advise: advise  
 *
 * @retval  0  success
 * @retval -1  error
 */
int fmap_advise(fmap_t *fmap, int advise)
{
    int result;

    if (fmap == NULL) {
        errno = EINVAL;
        return -1;
    }

    SYSCALL_RETRY(result, madvise(fmap->addr, fmap->size, advise));
    if (result < 0) {
        result = errno;
        LOGE_IF(log_trace, "madvise - failed %d\n", errno);
        errno = result;
        return -1;
    }

    LOGI_IF(log_trace, "fmap_advise -- success  path=\"%s\" advise=%d\n", fmap->path, advise);

    return 0;
}

/**
 * Wrapper function for mmap
 *   for EINTR error
 */
static void *mmap_retry(void *addr, size_t length, int prot, int flags, int fd, off_t offset)
{
    void *result = MAP_FAILED;
    int retry = 0;

    do {
        if (retry == 5) {
            LOGE_IF(log_trace, "mmap - failed ENOMEM\n");
            errno = ENOMEM;
            break;
        }
        result = mmap(addr, length, prot, flags, fd, offset);
        if ((result == MAP_FAILED) && (errno == EINTR)) {
            errno = 0;
            retry++;
        }
        else {
            if (result == MAP_FAILED) {
                retry = errno;
                LOGE_IF(log_trace, "mmap - failed %d\n", errno);
                errno = retry;
            }
            break;
        }
    } while (1);

    return result;
}


