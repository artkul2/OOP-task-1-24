@echo off
rem Запуск консольного демо (показывает все методы проекта). Windows-версия.
cd /d "%~dp0\.."
if not exist build\classes mkdir build\classes
where javac >nul 2>nul || (echo Не найден javac. Установите JDK 17: https://adoptium.net & exit /b 1)
dir /s /b src\main\java\*.java > build\sources.txt
javac -encoding UTF-8 -d build\classes @build\sources.txt || exit /b 1
java -cp build\classes gallery.console.GalleryApp
