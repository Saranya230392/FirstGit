LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_MODULE_TAGS := optional

LOCAL_PACKAGE_NAME := LGHWIME
LOCAL_CERTIFICATE := shared
LOCAL_REQUIRED_MODULES := libMyScriptEngine libMyScriptHWR libvoim

LOCAL_STATIC_JAVA_LIBRARIES := libvisionobjectsimvp
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libvisionobjectsimvp:libs/com.visionobjects.im.jar
include $(BUILD_PACKAGE)
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := libMyScriptEngine
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := libs/armeabi/$(LOCAL_MODULE).so
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := libMyScriptHWR
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := libs/armeabi/$(LOCAL_MODULE).so
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := libvoim
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := libs/armeabi/$(LOCAL_MODULE).so
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
include $(BUILD_PREBUILT)

PROGUARD_ENABLED := disabled