LOCAL_PATH:= $(call my-dir)
#----------------------------------------------------------------------
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional 
LOCAL_MODULE:= libiwnn

IWNN_PATH := iwnn
IWNN_JNI_PATH := jni

IWNN_INCLUDE := $(LOCAL_PATH)/iwnn
IWNN_JNI_INCLUDE := $(LOCAL_PATH)/jni

# All of the source files that we will compile.
LOCAL_SRC_FILES:= \
    $(IWNN_PATH)/ex_filter.c \
    $(IWNN_PATH)/ex_giji.c \
    $(IWNN_PATH)/ex_cmpdg.c \
    $(IWNN_PATH)/ex_aipgiji.c \
    $(IWNN_PATH)/ex_hrlgiji.c \
    $(IWNN_PATH)/ex_nmcgiji.c \
    $(IWNN_PATH)/ex_nmscgiji.c \
    $(IWNN_PATH)/ex_nmfgiji.c \
    $(IWNN_PATH)/ex_predg.c \
    $(IWNN_PATH)/mmapi.c \
    $(IWNN_PATH)/ncapi.c \
    $(IWNN_PATH)/ncconv.c \
    $(IWNN_PATH)/ndapi.c \
    $(IWNN_PATH)/ndbdic.c \
    $(IWNN_PATH)/ndcommon.c \
    $(IWNN_PATH)/ndfdic.c \
    $(IWNN_PATH)/ndldic.c \
    $(IWNN_PATH)/ndpdic.c \
    $(IWNN_PATH)/ndrdic.c \
    $(IWNN_PATH)/ndtdic.c \
    $(IWNN_PATH)/neapi.c \
    $(IWNN_PATH)/necode.c \
    $(IWNN_PATH)/nehomo.c \
    $(IWNN_PATH)/nfapi.c \
    $(IWNN_PATH)/nj_str.c \
    $(IWNN_PATH)/demoji_api.c \
    $(IWNN_PATH)/demoji_giji.c \
    $(IWNN_PATH)/ndsdic.c \
    $(IWNN_PATH)/nj_fio.c \
    $(IWNN_JNI_PATH)/giji_qwerty.c \
    $(IWNN_JNI_PATH)/iwnn.c \
    $(IWNN_JNI_PATH)/iwnn_utils.c \
    $(IWNN_JNI_PATH)/uniconv.c

# All of the shared libraries we link against.
ifeq ($(TARGET_OS)-$(TARGET_SIMULATOR),linux-true)
LOCAL_LDLIBS += -lutils
endif
LOCAL_LDLIBS += -llog
ifneq ($(TARGET_SIMULATOR),true)
LOCAL_SHARED_LIBRARIES += libutils \
                          liblog \
                          libcutils
endif

# No static libraries.
LOCAL_STATIC_LIBRARIES :=

# Also need the JNI headers.
LOCAL_C_INCLUDES += \
    $(JNI_H_INCLUDE) $(IWNN_INCLUDE) $(IWNN_JNI_INCLUDE)

# No special compiler flags.
LOCAL_CFLAGS += \
    -I. \
    -DNJ_MAX_CHARSET=300 -DNJ_MAX_DIC=56 \
    -DNJ_OPT_CHARSET_2BYTE \
    -DNJ_SEARCH_CACHE_SIZE=1000 -DNJ_CACHE_VIEW_CNT=2 -DNJ_MAX_LEN=50 \
    -DNJ_MAX_RESULT_LEN=50 -DNJ_MAX_GET_RESULTS=32 \
    -DNJ_MAX_USER_LEN=50 -DNJ_MAX_USER_KOUHO_LEN=50 -DNJ_MAX_USER_COUNT=500 \
	-DNJ_MAX_CANDIDATE=300 -DNJ_ADD_STATE_TYPE2 -DNJ_OPT_UTF16 -DHAVE_CONFIG_H -O

# Don't prelink this library.  For more efficient code, you may want
# to add this library to the prelink map and set this to true. However,
# it's difficult to do this for applications that are not supplied as
# part of a system image.

LOCAL_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)
