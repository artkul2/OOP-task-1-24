@echo off
rem Компиляция и запуск JUnit 5 тестов. Windows-версия.
rem Требуется junit-platform-console-standalone.jar в папке lib\
cd /d "%~dp0\.."
if not exist lib\junit-platform-console-standalone.jar (
    echo Не найден lib\junit-platform-console-standalone.jar. Скачайте его командой:
    echo   curl -sSL -o lib\junit-platform-console-standalone.jar https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar
    echo (если curl нет, просто откройте ссылку в браузере и сохраните файл в папку lib\)
    exit /b 1
)
if not exist build\classes mkdir build\classes
dir /s /b src\main\java\*.java > build\sources.txt
javac -encoding UTF-8 -d build\classes @build\sources.txt || exit /b 1
if not exist build\test-classes mkdir build\test-classes
dir /s /b src\test\java\*.java > build\test-sources.txt
javac -encoding UTF-8 -cp "build\classes;lib\junit-platform-console-standalone.jar" -d build\test-classes @build\test-sources.txt || exit /b 1
java -jar lib\junit-platform-console-standalone.jar execute -cp "build\classes;build\test-classes" --scan-classpath --details=tree
