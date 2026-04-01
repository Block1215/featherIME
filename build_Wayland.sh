#!/bin/bash
# Linux Wayland 用ネイティブライブラリをビルドして resources/natives/ に配置する。

echo "=== Build libfeathercaramel_wl.so ==="
if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME is not set."
    exit 1
fi

mkdir -p build
cd src/wayland
make && make install
cd ../..

DEST="src/main/resources/natives"
mkdir -p "$DEST"
cp build/libfeathercaramel_wl.so "$DEST/"
echo "Done → $DEST/libfeathercaramel_wl.so"
