
@echo off

set projectPathTest="C:\Users\user\Downloads\test5"

cd /d "%projectPathScanner%"

gradle build

:: Возвращаемся в исходный каталог
cd /d "%~dp0"

echo Тестовый проект собран успешно!
pause