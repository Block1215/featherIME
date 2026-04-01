#!/bin/bash
# Windows 用ネイティブライブラリをビルドして resources/natives/ に配置する。
# JAVA_HOME が設定されていることを確認してから実行すること。
#   例: export JAVA_HOME=/c/Program\ Files/Microsoft/jdk-21.0.x.x-hotspot

echo "=== Build feathercaramel_win.dll ==="
if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME is not set."
    exit 1
fi

mkdir -p build
cd src/win
make && make install
cd ../..

DEST="src/main/resources/natives"
mkdir -p "$DEST"
cp build/feathercaramel_win.dll "$DEST/"
echo "Done → $DEST/feathercaramel_win.dll"
