#!/usr/bin/env bash
# Запуск консольного демо (показывает все методы проекта)
set -e
cd "$(dirname "$0")/.."
bash scripts/build.sh
java -cp build/classes gallery.console.GalleryApp
