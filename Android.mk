LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files) $(call all-renderscript-files-under, src)

LOCAL_PACKAGE_NAME := Notepad
LOCAL_CERTIFICATE := shared

include $(BUILD_PACKAGE)
