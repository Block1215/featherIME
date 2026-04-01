# natives/

このディレクトリにコンパイル済みのネイティブライブラリを配置してください。

## 配置するファイル

| OS            | ファイル名                          | ビルドスクリプト       |
|---------------|-------------------------------------|------------------------|
| Windows x64   | feathercaramel_win.dll              | build_Windows.sh       |
| macOS         | libfeathercaramel_darwin.dylib      | build_macOS.sh         |
| Linux X11     | libfeathercaramel_x11.so            | build_X11.sh           |
| Linux Wayland | libfeathercaramel_wl.so             | build_Wayland.sh       |

## ビルド手順

### Windows (Git Bash / MSYS2 推奨)
```bash
export JAVA_HOME="C:/Program Files/Microsoft/jdk-21.x.x.x-hotspot"
bash build_Windows.sh
```

### macOS
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
bash build_macOS.sh
```

### Linux X11
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
bash build_X11.sh
```

### Linux Wayland
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
bash build_Wayland.sh
```

ビルド後に `./gradlew build` を実行すると jar に natives が同梱されます。
