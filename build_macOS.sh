#!/bin/bash
# macOS 用ネイティブライブラリをビルドして resources/natives/ に配置する。

echo "=== Build libfeathercaramel_darwin.dylib ==="
if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME is not set."
    echo "  Try: export JAVA_HOME=\$(/usr/libexec/java_home -v 21)"
    exit 1
fi

mkdir -p build
cd src/darwin/libcocoainput
make && make install
cd ../../..

DEST="src/main/resources/natives"
mkdir -p "$DEST"
cp build/libfeathercaramel_darwin.dylib "$DEST/"
echo "Done → $DEST/libfeathercaramel_darwin.dylib"
