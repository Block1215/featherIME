#!/bin/bash
# Linux X11 用ネイティブライブラリをビルドして resources/native/ に配置する。

echo "=== Build libx11cocoainput.so ==="
if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME is not set."
    exit 1
fi

mkdir -p build
cd src/x11
make && make install
cd ../..

DEST="src/main/resources/native"
mkdir -p "$DEST"
cp build/libx11cocoainput.so "$DEST/"
echo "Done → $DEST/libx11cocoainput.so"
