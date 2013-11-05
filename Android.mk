LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := bouncycastle telephony-common telephony-msim
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 android-support-v13 jsr305 libGoogleAnalyticsV2

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res

LOCAL_SRC_FILES += $(call all-java-files-under, ../PerformanceControl/src)
LOCAL_RESOURCE_DIR += packages/apps/PerformanceControl/res
LOCAL_ASSET_DIR += packages/apps/PerformanceControl/assets

LOCAL_AAPT_FLAGS := --extra-packages com.brewcrewfoo.performance --auto-add-overlay

LOCAL_PACKAGE_NAME := Settings
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
