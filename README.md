# FeatherCaramelChat

Feather Client 対応の IME 改善 Minecraft Fabric Mod (1.21.11)。
CocoaInput-lib をベースに、Architectury を使わない純粋 Fabric 構成で実装しています。

## caramelChat との違い

| 項目 | caramelChat | FeatherCaramelChat |
|------|-------------|-------------------|
| ローダー対応 | Fabric / Forge / NeoForge (Architectury) | **Fabric のみ** (Architectury なし) |
| Feather 互換 | ❌ 起動不可 | ✅ 起動可能 |
| Mixin 要求度 | required: true | **required: false** (失敗してもクラッシュしない) |
| プリエディット描画 | Mixin 内で描画 | **HudRenderCallback** (Mixin 外) |

## ビルド手順

### 1. ネイティブライブラリのビルド

使用 OS に応じてビルドスクリプトを実行してください。
**実行前に JAVA_HOME を設定すること。**

```bash
# Windows (Git Bash / MSYS2)
export JAVA_HOME="C:/Program Files/Microsoft/jdk-21.x.x.x-hotspot"
bash build_Windows.sh

# macOS
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
bash build_macOS.sh

# Linux X11
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
bash build_X11.sh

# Linux Wayland
bash build_Wayland.sh
```

ビルドに成功すると `src/main/resources/natives/` にライブラリが配置されます。

### 2. Gradle ビルド

```bash
./gradlew build
```

`build/libs/feather-caramel-chat-1.0.0.jar` が生成されます。

## ファイル構成

```
CocoaInput-lib-master/
├── build.gradle                          ← Fabric 1.21.11 ビルド設定
├── gradle.properties                     ← mod ID: feather-caramel-chat
├── settings.gradle
├── build_Windows.sh / build_macOS.sh ... ← ネイティブビルド + natives/ 配置
├── src/
│   ├── main/
│   │   ├── java/dev/bl/feathercaramel/
│   │   │   ├── FeatherCaramelChatClient.java  ← エントリーポイント
│   │   │   ├── ime/
│   │   │   │   ├── Platform.java             ← OS 検出
│   │   │   │   ├── NativeLibraryLoader.java  ← jar 内から .dll/.so を展開してロード
│   │   │   │   ├── NativeIMEBridge.java      ← native メソッド宣言 + C→Java CB
│   │   │   │   └── IMEManager.java           ← IME 状態管理シングルトン
│   │   │   └── mixin/
│   │   │       ├── ChatScreenMixin.java      ← チャット開閉で IME フォーカス制御
│   │   │       └── EditBoxMixin.java         ← commit 挿入・charTyped 抑制
│   │   └── resources/
│   │       ├── fabric.mod.json
│   │       ├── feather-caramel-chat.mixins.json
│   │       └── natives/                      ← ビルド後にここに .dll/.so を置く
│   ├── win/
│   │   ├── jni_bridge_win.c                  ← Windows JNI ブリッジ (新規)
│   │   └── Makefile                          ← 更新済み
│   ├── darwin/libcocoainput/
│   │   ├── jni_bridge_darwin.m               ← macOS JNI ブリッジ (新規)
│   │   └── Makefile                          ← 更新済み
│   ├── x11/
│   │   ├── jni_bridge_x11.c                  ← X11 JNI ブリッジ (新規)
│   │   └── Makefile                          ← 更新済み
│   └── wayland/
│       ├── jni_bridge_wl.c                   ← Wayland JNI ブリッジ (新規)
│       └── Makefile                          ← 更新済み
```

## ライセンス

LGPL-3.0-only (CocoaInput-lib に準拠)
