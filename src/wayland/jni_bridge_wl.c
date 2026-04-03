/*
 * jni_bridge_wl.c
 *
 * Linux Wayland 用 JNI ブリッジ。
 * 既存の libcaramelchatwl.c の関数を呼び出し、
 * Java (NativeIMEBridge) と C の間を接続する。
 *
 * 生成物: libfeathercaramel_wl.so
 *         → src/main/resources/natives/ に配置すること。
 */

#include <jni.h>
#include <wchar.h>
#include <stdlib.h>
#include "libcaramelchatwl.h"

static JavaVM *g_jvm       = NULL;
static jclass  g_bridgeCls = NULL;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    g_jvm = vm;
    return JNI_VERSION_1_8;
}

static JNIEnv *getEnv(int *needDetach) {
    JNIEnv *env = NULL;
    jint res = (*g_jvm)->GetEnv(g_jvm, (void **)&env, JNI_VERSION_1_8);
    if (res == JNI_EDETACHED) {
        (*g_jvm)->AttachCurrentThread(g_jvm, (void **)&env, NULL);
        *needDetach = 1;
    } else {
        *needDetach = 0;
    }
    return env;
}

static void jni_log(const char *msg) { /* no-op */ }

/* ---- wchar_t → jstring ---- */
static jstring wcharToJString(JNIEnv *env, wchar_t *text) {
    if (!text) return (*env)->NewStringUTF(env, "");
    size_t len = wcslen(text);
    char *buf = (char *)malloc(len * 4 + 1);
    if (!buf) return (*env)->NewStringUTF(env, "");
    wcstombs(buf, text, len * 4 + 1);
    jstring js = (*env)->NewStringUTF(env, buf);
    free(buf);
    return js;
}

/* ---- C コールバック: preedit ---- */
static void c_preedit(wchar_t *text) {
    if (!g_jvm || !g_bridgeCls) return;
    int detach = 0;
    JNIEnv *env = getEnv(&detach);
    if (!env) return;

    jstring jtext = wcharToJString(env, text);
    jmethodID mid = (*env)->GetStaticMethodID(env, g_bridgeCls,
        "onPreeditText", "(Ljava/lang/String;II)V");
    if (mid) {
        (*env)->CallStaticVoidMethod(env, g_bridgeCls, mid, jtext, 0, 0);
    }
    (*env)->DeleteLocalRef(env, jtext);
    if (detach) (*g_jvm)->DetachCurrentThread(g_jvm);
}

/* ---- C コールバック: preeditNull (Wayland 専用) ---- */
static void c_preeditNull() {
    if (!g_jvm || !g_bridgeCls) return;
    int detach = 0;
    JNIEnv *env = getEnv(&detach);
    if (!env) return;

    jmethodID mid = (*env)->GetStaticMethodID(env, g_bridgeCls,
        "onPreeditNull", "()V");
    if (mid) {
        (*env)->CallStaticVoidMethod(env, g_bridgeCls, mid);
    }
    if (detach) (*g_jvm)->DetachCurrentThread(g_jvm);
}

/* ---- C コールバック: commit ---- */
static void c_done(wchar_t *text) {
    if (!g_jvm || !g_bridgeCls) return;
    int detach = 0;
    JNIEnv *env = getEnv(&detach);
    if (!env) return;

    jstring jtext = wcharToJString(env, text);
    jmethodID mid = (*env)->GetStaticMethodID(env, g_bridgeCls,
        "onCommitText", "(Ljava/lang/String;)V");
    if (mid) {
        (*env)->CallStaticVoidMethod(env, g_bridgeCls, mid, jtext);
    }
    (*env)->DeleteLocalRef(env, jtext);
    if (detach) (*g_jvm)->DetachCurrentThread(g_jvm);
}

/* ---- C コールバック: rect ---- */
static bool c_rect(float *rect) {
    if (!g_jvm || !g_bridgeCls || !rect) return true;
    int detach = 0;
    JNIEnv *env = getEnv(&detach);
    if (!env) return true;

    bool result = true;
    jmethodID mid = (*env)->GetStaticMethodID(env, g_bridgeCls,
        "onRequestRect", "()[F");
    if (mid) {
        jfloatArray arr = (jfloatArray)(*env)->CallStaticObjectMethod(
            env, g_bridgeCls, mid);
        if (arr) {
            jfloat *elems = (*env)->GetFloatArrayElements(env, arr, NULL);
            if (elems) {
                rect[0] = elems[0];
                rect[1] = elems[1];
                rect[2] = elems[2];
                rect[3] = elems[3];
                result = false;
                (*env)->ReleaseFloatArrayElements(env, arr, elems, JNI_ABORT);
            }
            (*env)->DeleteLocalRef(env, arr);
        }
    }

    if (detach) (*g_jvm)->DetachCurrentThread(g_jvm);
    return result;
}

/* ============================================================
 *  Java native メソッド実装
 * ============================================================ */

JNIEXPORT void JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_wlInitialize(
        JNIEnv *env, jclass cls, jlong displayPtr) {
    if (!g_bridgeCls) {
        g_bridgeCls = (jclass)(*env)->NewGlobalRef(env, cls);
    }
    initialize((struct wl_display *)displayPtr,
               c_preedit, c_preeditNull, c_done, c_rect,
               jni_log, jni_log, jni_log);
}

JNIEXPORT void JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_wlSetFocus(
        JNIEnv *env, jclass cls, jboolean focused) {
    setFocus((bool)focused);
}
