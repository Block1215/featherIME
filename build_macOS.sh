#!/bin/bash
# macOS 用ネイティブライブラリをビルドして resources/native/ に配置する。

echo "=== Build libdarwincocoainput.dylib ==="
if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME is not set."
    echo "  Try: export JAVA_HOME=\$(/usr/libexec/java_home -v 21)"
    exit 1
fi

mkdir -p build
cd src/darwin/libcocoainput
make && make install
cd ../../..

DEST="src/main/resources/native"
mkdir -p "$DEST"
cp build/libdarwincocoainput.dylib "$DEST/"
echo "Done → $DEST/libdarwincocoainput.dylib"
