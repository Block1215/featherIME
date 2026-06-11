#!/bin/bash
# Linux Wayland 用ネイティブライブラリをビルドして resources/native/ に配置する。

echo "=== Build libcaramelchatwl.so ==="
if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME is not set."
    exit 1
fi

mkdir -p build
cd src/wayland
make && make install
cd ../..

DEST="src/main/resources/native"
mkdir -p "$DEST"
cp build/libcaramelchatwl.so "$DEST/"
echo "Done → $DEST/libcaramelchatwl.so"
