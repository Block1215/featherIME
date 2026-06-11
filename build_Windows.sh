#!/bin/bash
# Windows 用ネイティブライブラリをビルドして resources/native/ に配置する。
# JAVA_HOME が設定されていることを確認してから実行すること。
#   例: export JAVA_HOME=/c/Program\ Files/Microsoft/jdk-21.0.x.x-hotspot

echo "=== Build libwincocoainput.dll ==="
if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME is not set."
    exit 1
fi

mkdir -p build
cd src/win
make && make install
cd ../..

DEST="src/main/resources/native"
mkdir -p "$DEST"
cp build/libwincocoainput.dll "$DEST/"
echo "Done → $DEST/libwincocoainput.dll"
