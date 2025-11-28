@echo off
REM Скрипт для сборки APK файла (debug версия, не требует подписи)

echo ========================================
echo Сборка APK файла LinkJob
echo ========================================
echo.

REM Проверяем наличие gradlew.bat
if exist "gradlew.bat" (
    set GRADLE_CMD=gradlew.bat
) else if exist "gradlew" (
    set GRADLE_CMD=gradlew
) else (
    echo ОШИБКА: Gradle wrapper не найден!
    echo Убедитесь, что вы находитесь в корне проекта
    echo Или используйте Android Studio для сборки
    pause
    exit /b 1
)

echo Очистка предыдущих сборок...
call %GRADLE_CMD% clean

echo.
echo Сборка Debug APK...
call %GRADLE_CMD% assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Сборка успешна!
    echo ========================================
    echo.
    
    REM Создаём папку releases, если её нет
    if not exist "releases" mkdir releases
    
    REM Копируем APK в папку releases
    set APK_PATH=app\build\outputs\apk\debug\app-debug.apk
    set RELEASE_PATH=releases\LinkJob-v1.0-debug.apk
    
    if exist "%APK_PATH%" (
        copy /Y "%APK_PATH%" "%RELEASE_PATH%"
        echo.
        echo APK файл скопирован в releases:
        echo %RELEASE_PATH%
        echo.
        echo Исходный файл:
        echo %APK_PATH%
        echo.
    ) else (
        echo.
        echo ВНИМАНИЕ: APK файл не найден по пути:
        echo %APK_PATH%
        echo.
    )
) else (
    echo.
    echo ОШИБКА при сборке!
    echo Проверьте ошибки выше
    echo.
)

pause

