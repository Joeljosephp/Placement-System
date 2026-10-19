#!/usr/bin/env bash
set -e

# Compile if classes do not exist
if [ ! -f "bin/com/placementsystem/Main.class" ]; then
    echo "Compiling Placement Management System..."
    mkdir -p bin
    javac -encoding UTF-8 -d bin $(find src -name "*.java")
fi

exec java -cp bin com.placementsystem.Main "$@"
