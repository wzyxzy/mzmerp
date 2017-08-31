LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := librarys
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_SRC_FILES := \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\Android.mk \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\Application.mk \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\Android.mk \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lapi.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lauxlib.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lbaselib.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lcode.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\ldblib.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\ldebug.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\ldo.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\ldump.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lfunc.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lgc.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\linit.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\liolib.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\llex.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lmathlib.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lmem.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\loadlib.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lobject.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lopcodes.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\loslib.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lparser.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lstate.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lstring.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lstrlib.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\ltable.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\ltablib.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\ltm.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lundump.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lvm.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\lua\lzio.c \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\luajava\Android.mk \
	D:\hejia\HJme\HJme\svn\librarys\src\main\jni\luajava\luajava.c \

LOCAL_C_INCLUDES += D:\hejia\HJme\HJme\svn\librarys\src\main\jni
LOCAL_C_INCLUDES += D:\hejia\HJme\HJme\svn\librarys\src\release\jni

include $(BUILD_SHARED_LIBRARY)
