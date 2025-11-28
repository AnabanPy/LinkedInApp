@echo off
REM Скрипт для сборки release версии приложения

echo ========================================
echo Сборка Release версии LinkJob
echo ========================================
echo.

REM Проверяем наличие keystore.properties
if not exist "keystore.properties" (
    echo ВНИМАНИЕ: keystore.properties не найден!
    echo Запустите create-keystore.bat для создания keystore
    echo Или создайте keystore.properties вручную
    echo.
    pause
    exit /b 1
)

echo Очистка предыдущих сборок...
call gradlew.bat clean

echo.
echo Сборка Release AAB (Android App Bundle)...
call gradlew.bat bundleRelease

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Сборка успешна!
    echo ========================================
    echo.
    echo Файл находится в:
    echo app\build\outputs\bundle\release\app-release.aab
    echo.
    echo Следующий шаг: Загрузите этот файл в Google Play Console
    echo.
) else (
    echo.
    echo ОШИБКА при сборке!
    echo Проверьте настройки signing config в build.gradle.kts
    echo.
)

pause


