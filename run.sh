#!/usr/bin/env bash
set -euo pipefail

# Direktori output kelas
OUT_DIR=bin

# Bersihkan build sebelumnya
echo "Cleaning previous build..."
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

# Compile semua kode utama
echo "Compiling main sources..."
javac -d "$OUT_DIR" \
    src/backend/model/*.java \
    src/backend/exception/*.java \
    src/backend/algorithm/*.java \
    src/backend/util/*.java
    # src/main/app/*.java \

# Compile test driver
echo "Compiling TestDriver..."
javac -d "$OUT_DIR" \
    -cp "$OUT_DIR" \
    src/test/backend/TestDriverUCS.java 

# Jalankan test driver
echo "Running tests..."
java -cp "$OUT_DIR" test.backend.TestDriverUCS

echo "Build & tests completed successfully."
