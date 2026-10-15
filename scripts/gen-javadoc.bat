@echo off
rem Генерация HTML-документации Javadoc в папку doc\javadoc
cd /d "%~dp0\.."
if exist doc\javadoc rmdir /s /q doc\javadoc
mkdir doc\javadoc
dir /s /b src\main\java\*.java > build\jdoc-files.txt
javadoc -encoding UTF-8 -charset UTF-8 -d doc\javadoc -sourcepath src\main\java @build\jdoc-files.txt
if %errorlevel%==0 (
  echo Готово. Откройте doc\javadoc\index.html в браузере.
) else (
  echo Ошибка генерации Javadoc.
)
pause
