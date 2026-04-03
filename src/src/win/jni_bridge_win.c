/*
 * jni_bridge_win.c
 *
 * Windows 用 JNI ブリッジ。
 * 既存の libwincocoainput.c の関数を呼び出し、
 * Java (NativeIMEBridge) と C の間を接続する。
 *
 * コンパイル: src/win/Makefile で自動的にビルドされる。
 * 生成物: feathercaramel_win.dll
 *         → src/main/resources/natives/feathercaramel_win.dll に配置すること。
 */

#include <jni.h>
#include <windows.h>
#include "libwincocoainput.h"

/* ---- グローバル: JVM と NativeIMEBridge クラス参照 ---- */
static JavaVM  *g_jvm       = NULL;
static jclass   g_bridgeCls = NULL;

/* ---- JNI_OnLoad : JVM ポインタを取得 ---- */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    g_jvm = vm;
    return JNI_VERSION_1_8;
}

/* ---- ヘルパ: 現在スレッドの JNIEnv を取得 ---- */
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

/* ---- C → Java コールバック: draw (preedit) ---- */
static int *c_draw(wchar_t *text, int cursor, int selectedLength) {
    if (!g_jvm || !g_bridgeCls) return NULL;

    int detach = 0;
    JNIEnv *env = getEnv(&detach);
    if (!env) return NULL;

    /* wchar_t → Java String */
    jstring jtext = NULL;
    if (text) {
        /* UTF-16 に変換して NewString で作成 */
        int len = wcslen(text);
        jtext = (*env)->NewString(env, (const jchar *)text, len);
    } else {
        jtext = (*env)->NewStringUTF(env, "");
    }

    jmethodID mid = (*env)->GetStaticMethodID(env, g_bridgeCls,
        "onPreeditText", "(Ljava/lang/String;II)V");
    if (mid) {
        (*env)->CallStaticVoidMethod(env, g_bridgeCls, mid,
            jtext, (jint)cursor, (jint)selectedLength);
    }
    if (jtext) (*env)->DeleteLocalRef(env, jtext);

    /* rect は onRequestRect で別途取得 */
    static int dummy[4] = {0, 0, 0, 0};

    jmethodID rectMid = (*env)->GetStaticMethodID(env, g_bridgeCls,
        "onRequestRect", "()[F");
    if (rectMid) {
        jfloatArray arr = (jfloatArray)(*env)->CallStaticObjectMethod(
            env, g_bridgeCls, rectMid);
        if (arr) {
            jfloat *elems = (*env)->GetFloatArrayElements(env, arr, NULL);
            if (elems) {
                dummy[0] = (int)elems[0];
                dummy[1] = (int)elems[1];
                dummy[2] = (int)(elems[0] + elems[2]);
                dummy[3] = (int)(elems[1] + elems[3]);
                (*env)->ReleaseFloatArrayElements(env, arr, elems, JNI_ABORT);
            }
            (*env)->DeleteLocalRef(env, arr);
        }
    }

    if (detach) (*g_jvm)->DetachCurrentThread(g_jvm);
    return dummy;
}

/* ---- C → Java コールバック: done (commit) ---- */
static void c_done(wchar_t *text) {
    if (!g_jvm || !g_bridgeCls) return;

    int detach = 0;
    JNIEnv *env = getEnv(&detach);
    if (!env) return;

    jstring jtext = NULL;
    if (text && wcslen(text) > 0) {
        jtext = (*env)->NewString(env, (const jchar *)text, wcslen(text));
    } else {
        jtext = (*env)->NewStringUTF(env, "");
    }

    jmethodID mid = (*env)->GetStaticMethodID(env, g_bridgeCls,
        "onCommitText", "(Ljava/lang/String;)V");
    if (mid) {
        (*env)->CallStaticVoidMethod(env, g_bridgeCls, mid, jtext);
    }
    if (jtext) (*env)->DeleteLocalRef(env, jtext);
    if (detach) (*g_jvm)->DetachCurrentThread(g_jvm);
}

/* ---- C → Java コールバック: rect (候補ウィンドウ位置) ---- */
static int c_rect(float *rect) {
    if (!g_jvm || !g_bridgeCls || !rect) return 1;

    int detach = 0;
    JNIEnv *env = getEnv(&detach);
    if (!env) return 1;

    jmethodID mid = (*env)->GetStaticMethodID(env, g_bridgeCls,
        "onRequestRect", "()[F");
    int result = 1;
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
                result = 0;
                (*env)->ReleaseFloatArrayElements(env, arr, elems, JNI_ABORT);
            }
            (*env)->DeleteLocalRef(env, arr);
        }
    }

    if (detach) (*g_jvm)->DetachCurrentThread(g_jvm);
    return result;
}

/* ---- Logger コールバック (C → Java ログ) ---- */
static void jni_log(const char *msg) {
    /* 省略: 必要なら同様に JNI で LOGGER.info を呼ぶ */
}

/* ============================================================
 *  Java native メソッド実装
 * ============================================================ */

/*
 * dev.bl.feathercaramel.ime.NativeIMEBridge#winInitialize(J)V
 */
JNIEXPORT void JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_winInitialize(
        JNIEnv *env, jclass cls, jlong hwnd) {

    /* NativeIMEBridge クラスをグローバル参照として保持 */
    if (!g_bridgeCls) {
        g_bridgeCls = (jclass)(*env)->NewGlobalRef(env, cls);
    }

    initialize((long)hwnd, c_draw, c_done, c_rect,
               jni_log, jni_log, jni_log);
}

/*
 * dev.bl.feathercaramel.ime.NativeIMEBridge#winSetFocus(Z)V
 */
JNIEXPORT void JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_winSetFocus(
        JNIEnv *env, jclass cls, jboolean focused) {
    set_focus(focused ? 1 : 0);
}

/*
 * dev.bl.feathercaramel.ime.NativeIMEBridge#winGetKeyboardLayout()I
 */
JNIEXPORT jint JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_winGetKeyboardLayout(
        JNIEnv *env, jclass cls) {
    return (jint)getKeyboardLayout();
}

/*
 * dev.bl.feathercaramel.ime.NativeIMEBridge#winGetIMEStatus()Z
 */
JNIEXPORT jboolean JNICALL
Java_dev_bl_feathercaramel_ime_NativeIMEBridge_winGetIMEStatus(
        JNIEnv *env, jclass cls) {
    return (jboolean)(getStatus() != 0);
}
