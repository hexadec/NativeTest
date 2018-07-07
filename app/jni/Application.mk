APP_ABI := armeabi-v7a arm64-v8a
APP_CPPFLAGS += -std=c++11 -fexceptions
APP_STL := gnustl_static
APP_OPTIM := release
APP_PLATFORM := android-16
# GCC 4.9 Toolchain - requires NDK r10
# NDK_TOOLCHAIN_VERSION = 4.9
APP_BUILD_SCRIPT := jni/Android.mk