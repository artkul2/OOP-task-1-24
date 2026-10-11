#!/usr/bin/env bash
# Компиляция основных классов проекта
set -e
cd "$(dirname "$0")/.."
mkdir -p build/classes
javac -encoding UTF-8 -d build/classes $(find src/main/java -name '*.java')
echo "Компиляция завершена успешно."
