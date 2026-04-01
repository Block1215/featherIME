/*
 * jni_bridge_x11.c
 *
 * Linux X11 用 JNI ブリッジ。
 * 既存の libx11cocoainput.c の関数を呼び出し、
 * Java (NativeIMEBridge) と C の間を接続する。
 *
 * 生成物: libfeathercaramel_x11.so
 *         → src/main/resources/natives/ に配置すること。
 */

#include <jni.h>
#include <wchar.h>
#include <stdlib.h>
#include "libx11cocoainput.h"

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

/* ---- wchar_t を Java String に変換 ---- */
static jstring wcharToJString(JNIEnv *env, wchar_t *text) {
    if (!text) return (*env)->NewStringUTF(env, "");
    /* wchar_t (4 byte on Linux) → UTF-8 */
    size_t len = wcslen(text);
    char *buf = (char *)malloc(len * 4 + 1);
    if (!buf) return (*env)->NewStringUTF(env, "");
    wcstombs(buf, text, len * 4 + 1);
    jstring js = (*env)->NewStringUTF(env, buf);
    free(buf);
    return js;
}

/* ---- C コールバック: draw (preedit) ---- */
static int *c_draw(int caret, int chg_first, int chg_length,
                   short textlen, int is_wchar,
                   char *mb_text, wchar_t *wc_text,
                   int dummy, int secondary, int sec_end) {
    if (!g_jvm || !g_bridgeCls) return NULL;
    int detach = 0;
    JNIEnv *env = getEnv(&detach);
    if (!env) return NULL;

    jstring jtext;
    if (is_wchar) {
        jtext = wcharToJString(env, wc_text);
    } else {
        jtext = (*env)->NewStringUTF(env, mb_text ? mb_text : "");
    }

    int selectedLen = (sec_end > secondary) ? (sec_end - secondary) : 0;

    jmethodID mid = (*env)->GetStaticMethodID(env, g_bridgeCls,
        "onPreeditText", "(Ljava/lang/String;II)V");
    if (mid) {
        (*env)->CallStaticVoidMethod(env, g_bridgeCls, mid,
            jtext, (jint)caret, (jint)selectedLen);
    }
    (*env)->DeleteLocalRef(env, jtext);

    /* カーソル位置を返す */
    static int pos[2] = {0, 0};
    jmethodID rectMid = (*env)->GetStaticMethodID(env, g_bridgeCls,
        "onRequestRect", "()[F");
    if (rectMid) {
        jfloatArray arr = (jfloatArray)(*env)->CallStaticObjectMethod(
            env, g_bridgeCls, rectMid);
        if (arr) {
            jfloat *elems = (*env)->GetFloatArrayElements(env, arr, NULL);
            if (elems) {
                pos[0] = (int)elems[0];
                pos[1] = (int)elems[1];
                (*env)->ReleaseFloatArrayElements(env, arr, elems, JNI_ABORT);
            }
            (*env)->DeleteLocalRef(env, arr);
        }
    }

    if (detach) (*g_jvm)->DetachCurrentThread(g_jvm);
    return pos;
}

/* ---- C コールバック: done (commit / preedit clear) ---- */
static void c_done() {
    if (!g_jvm || !g_bridgeCls) return;
    int detach = 0;
    JNIEnv *env = getEnv(&detach);
    if (!env) return;

    /* X11 では done がプリエディット終了を意味する。
       確定テキストは draw の最終呼び出しで渡される仕様のため、
       ここでは preedit をクリアするだけ。 */
    jmethodID mid = (*env)->GetStaticMethodID(env, g_bridgeCls,
        "onPreeditText", "(Ljava/lang/String;II)V");
    if (mid) {
        jstring empty = (*env)->NewStringUTF(env, "");
        (*env)->CallStaticVoidMethod(env, g_bridgeCls, mid, empty, 0, 0);
        (*env)->DeleteLocalRef(env, empty);
    }

    if (detach) (*g_jvm)->DetachCurrentThread(g_jvm);
}

/* ============================================================
 *  Java native メソッド実装
 * ============================================================ */

JNIEXPORT void JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_x11Initialize(
        JNIEnv *env, jclass cls, jlong glfwWindowPtr, jlong x11Window) {
    if (!g_bridgeCls) {
        g_bridgeCls = (jclass)(*env)->NewGlobalRef(env, cls);
    }
    initialize((long)glfwWindowPtr, (long)x11Window,
               c_draw, c_done,
               jni_log, jni_log, jni_log);
}

JNIEXPORT void JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_x11SetFocus(
        JNIEnv *env, jclass cls, jboolean focused) {
    set_focus(focused ? 1 : 0);
}
