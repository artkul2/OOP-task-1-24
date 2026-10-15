#!/usr/bin/env bash
# Генерация HTML-документации Javadoc в папку doc/javadoc
set -e
cd "$(dirname "$0")/.."
rm -rf doc/javadoc
mkdir -p doc/javadoc
javadoc -encoding UTF-8 -charset UTF-8 -d doc/javadoc -sourcepath src/main/java $(find src/main/java -name '*.java')
echo "Готово. Откройте doc/javadoc/index.html в браузере."
