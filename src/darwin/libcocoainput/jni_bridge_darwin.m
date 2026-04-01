/*
 * jni_bridge_darwin.m
 *
 * macOS (Objective-C) 用 JNI ブリッジ。
 * 既存の libcocoainput (cocoainput.m / DataManager.m など) を呼び出し、
 * Java (NativeIMEBridge) と Objective-C の間を接続する。
 *
 * コンパイル: src/darwin/libcocoainput/Makefile で自動的にビルドされる。
 * 生成物: libfeathercaramel_darwin.dylib
 *         → src/main/resources/natives/ に配置すること。
 */

#import <Foundation/Foundation.h>
#import <jni.h>
#import "cocoainput.h"

/* ---- グローバル ---- */
static JavaVM *g_jvm       = NULL;
static jclass  g_bridgeCls = NULL;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    g_jvm = vm;
    return JNI_VERSION_1_8;
}

/* ---- ヘルパ ---- */
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

/* ---- logger stub ---- */
static void jni_log(const char *msg)   { /* no-op; Java 側でログ */ }

/* ---- C コールバック: insertText ---- */
static void c_insertText(const char *text, const int replacementStart, const int replacementLength) {
    if (!g_jvm || !g_bridgeCls || !text) return;
    int detach = 0;
    JNIEnv *env = getEnv(&detach);
    if (!env) return;

    jstring jtext = (*env)->NewStringUTF(env, text);
    jmethodID mid = (*env)->GetStaticMethodID(env, g_bridgeCls,
        "macOnInsertText", "(Ljava/lang/String;II)V");
    if (mid) {
        (*env)->CallStaticVoidMethod(env, g_bridgeCls, mid,
            jtext, (jint)replacementStart, (jint)replacementLength);
    }
    (*env)->DeleteLocalRef(env, jtext);
    if (detach) (*g_jvm)->DetachCurrentThread(g_jvm);
}

/* ---- C コールバック: setMarkedText ---- */
static void c_setMarkedText(const char *text,
                             const int selStart, const int selEnd,
                             const int replacementStart, const int replacementLength) {
    if (!g_jvm || !g_bridgeCls) return;
    int detach = 0;
    JNIEnv *env = getEnv(&detach);
    if (!env) return;

    jstring jtext = (*env)->NewStringUTF(env, text ? text : "");
    jmethodID mid = (*env)->GetStaticMethodID(env, g_bridgeCls,
        "macOnSetMarkedText", "(Ljava/lang/String;IIII)V");
    if (mid) {
        (*env)->CallStaticVoidMethod(env, g_bridgeCls, mid,
            jtext, (jint)selStart, (jint)selEnd,
            (jint)replacementStart, (jint)replacementLength);
    }
    (*env)->DeleteLocalRef(env, jtext);
    if (detach) (*g_jvm)->DetachCurrentThread(g_jvm);
}

/* ---- C コールバック: firstRectForCharacterRange ---- */
static void c_firstRectForCharacterRange(const float *outRect) {
    if (!g_jvm || !g_bridgeCls || !outRect) return;
    int detach = 0;
    JNIEnv *env = getEnv(&detach);
    if (!env) return;

    /* Java 側に float[4] を渡して書き込んでもらう */
    jfloatArray arr = (*env)->NewFloatArray(env, 4);
    if (arr) {
        jmethodID mid = (*env)->GetStaticMethodID(env, g_bridgeCls,
            "macOnFirstRectForCharacterRange", "([F)V");
        if (mid) {
            (*env)->CallStaticVoidMethod(env, g_bridgeCls, mid, arr);
        }
        jfloat *elems = (*env)->GetFloatArrayElements(env, arr, NULL);
        if (elems) {
            /* const を外して書き込む (呼び出し側は書き込みを期待している) */
            float *w = (float *)outRect;
            w[0] = elems[0]; w[1] = elems[1];
            w[2] = elems[2]; w[3] = elems[3];
            (*env)->ReleaseFloatArrayElements(env, arr, elems, JNI_ABORT);
        }
        (*env)->DeleteLocalRef(env, arr);
    }
    if (detach) (*g_jvm)->DetachCurrentThread(g_jvm);
}

/* ============================================================
 *  Java native メソッド実装
 * ============================================================ */

JNIEXPORT void JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_macInitialize(
        JNIEnv *env, jclass cls) {
    if (!g_bridgeCls) {
        g_bridgeCls = (jclass)(*env)->NewGlobalRef(env, cls);
    }
    initialize(jni_log, jni_log, jni_log);
}

JNIEXPORT void JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_macAddInstance(
        JNIEnv *env, jclass cls, jstring uuid) {
    const char *u = (*env)->GetStringUTFChars(env, uuid, NULL);
    addInstance(u, c_insertText, c_setMarkedText, c_firstRectForCharacterRange);
    (*env)->ReleaseStringUTFChars(env, uuid, u);
}

JNIEXPORT void JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_macRemoveInstance(
        JNIEnv *env, jclass cls, jstring uuid) {
    const char *u = (*env)->GetStringUTFChars(env, uuid, NULL);
    removeInstance(u);
    (*env)->ReleaseStringUTFChars(env, uuid, u);
}

JNIEXPORT void JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_macSetIfReceiveEvent(
        JNIEnv *env, jclass cls, jstring uuid, jboolean active) {
    const char *u = (*env)->GetStringUTFChars(env, uuid, NULL);
    setIfReceiveEvent(u, active ? 1 : 0);
    (*env)->ReleaseStringUTFChars(env, uuid, u);
}

JNIEXPORT void JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_macDiscardMarkedText(
        JNIEnv *env, jclass cls, jstring uuid) {
    const char *u = (*env)->GetStringUTFChars(env, uuid, NULL);
    discardMarkedText(u);
    (*env)->ReleaseStringUTFChars(env, uuid, u);
}

JNIEXPORT jstring JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_macGetStatus(
        JNIEnv *env, jclass cls) {
    const char *s = getStatus();
    return s ? (*env)->NewStringUTF(env, s) : NULL;
}

JNIEXPORT void JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_macRefreshInstance(
        JNIEnv *env, jclass cls) {
    refreshInstance();
}
