#!/bin/bash
# Скрипт для сборки APK файла (debug версия, не требует подписи)

echo "========================================"
echo "Сборка APK файла LinkJob"
echo "========================================"
echo ""

# Проверяем наличие gradlew
if [ -f "gradlew" ]; then
    GRADLE_CMD="./gradlew"
elif [ -f "gradlew.bat" ]; then
    GRADLE_CMD="./gradlew.bat"
else
    echo "ОШИБКА: Gradle wrapper не найден!"
    echo "Убедитесь, что вы находитесь в корне проекта"
    echo "Или используйте Android Studio для сборки"
    exit 1
fi

echo "Очистка предыдущих сборок..."
$GRADLE_CMD clean

echo ""
echo "Сборка Debug APK..."
$GRADLE_CMD assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "Сборка успешна!"
    echo "========================================"
    echo ""
    
    # Создаём папку releases, если её нет
    mkdir -p releases
    
    # Копируем APK в папку releases
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    RELEASE_PATH="releases/LinkJob-v1.0-debug.apk"
    
    if [ -f "$APK_PATH" ]; then
        cp "$APK_PATH" "$RELEASE_PATH"
        echo ""
        echo "APK файл скопирован в releases:"
        echo "$RELEASE_PATH"
        echo ""
        echo "Исходный файл:"
        echo "$APK_PATH"
        echo ""
    else
        echo ""
        echo "ВНИМАНИЕ: APK файл не найден по пути:"
        echo "$APK_PATH"
        echo ""
    fi
else
    echo ""
    echo "ОШИБКА при сборке!"
    echo "Проверьте ошибки выше"
    echo ""
fi

