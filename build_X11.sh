#!/bin/bash
# Linux X11 用ネイティブライブラリをビルドして resources/natives/ に配置する。

echo "=== Build libfeathercaramel_x11.so ==="
if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME is not set."
    exit 1
fi

mkdir -p build
cd src/x11
make && make install
cd ../..

DEST="src/main/resources/natives"
mkdir -p "$DEST"
cp build/libfeathercaramel_x11.so "$DEST/"
echo "Done → $DEST/libfeathercaramel_x11.so"
