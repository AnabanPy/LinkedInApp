@echo off
REM Скрипт для создания keystore для подписи release сборки
REM Для публикации в Google Play

echo ========================================
echo Создание Keystore для LinkJob
echo ========================================
echo.

REM Запрашиваем данные
set /p KEYSTORE_NAME="Имя keystore файла [linkjob-release.keystore]: "
if "%KEYSTORE_NAME%"=="" set KEYSTORE_NAME=linkjob-release.keystore

set /p KEY_ALIAS="Alias ключа [linkjob]: "
if "%KEY_ALIAS%"=="" set KEY_ALIAS=linkjob

echo.
echo Создаю keystore...
echo ВАЖНО: Запишите все пароли в безопасном месте!
echo.

keytool -genkey -v -keystore %KEYSTORE_NAME% -alias %KEY_ALIAS% -keyalg RSA -keysize 2048 -validity 10000

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Keystore успешно создан!
    echo ========================================
    echo.
    echo Следующие шаги:
    echo 1. Создайте keystore.properties в корне проекта
    echo 2. Заполните его данными (см. keystore.properties.example)
    echo 3. Раскомментируйте signing config в app/build.gradle.kts
    echo.
    echo ВАЖНО: Сохраните %KEYSTORE_NAME% и пароли в безопасном месте!
    echo.
) else (
    echo.
    echo ОШИБКА: Не удалось создать keystore
    echo.
)

pause


