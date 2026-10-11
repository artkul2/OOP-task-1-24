#!/usr/bin/env bash
# Компиляция и запуск JUnit 5 тестов.
# Требуется junit-platform-console-standalone.jar — путь задаётся переменной JUNIT_JAR,
# по умолчанию lib/junit-platform-console-standalone.jar
set -e
cd "$(dirname "$0")/.."
JUNIT_JAR="${JUNIT_JAR:-lib/junit-platform-console-standalone.jar}"
if [ ! -f "$JUNIT_JAR" ]; then
    echo "Не найден $JUNIT_JAR. Скачайте:"
    echo "  curl -sSL -o lib/junit-platform-console-standalone.jar \\"
    echo "    https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar"
    exit 1
fi
bash scripts/build.sh
mkdir -p build/test-classes
javac -encoding UTF-8 -cp "build/classes:$JUNIT_JAR" -d build/test-classes $(find src/test/java -name '*.java')
java -jar "$JUNIT_JAR" execute -cp "build/classes:build/test-classes" --scan-classpath --details=tree
