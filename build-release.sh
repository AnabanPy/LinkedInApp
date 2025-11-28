#!/bin/bash
# Скрипт для сборки release версии приложения

echo "========================================"
echo "Сборка Release версии LinkJob"
echo "========================================"
echo ""

# Проверяем наличие keystore.properties
if [ ! -f "keystore.properties" ]; then
    echo "ВНИМАНИЕ: keystore.properties не найден!"
    echo "Запустите create-keystore.sh для создания keystore"
    echo "Или создайте keystore.properties вручную"
    echo ""
    exit 1
fi

echo "Очистка предыдущих сборок..."
./gradlew clean

echo ""
echo "Сборка Release AAB (Android App Bundle)..."
./gradlew bundleRelease

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "Сборка успешна!"
    echo "========================================"
    echo ""
    echo "Файл находится в:"
    echo "app/build/outputs/bundle/release/app-release.aab"
    echo ""
    echo "Следующий шаг: Загрузите этот файл в Google Play Console"
    echo ""
else
    echo ""
    echo "ОШИБКА при сборке!"
    echo "Проверьте настройки signing config в build.gradle.kts"
    echo ""
fi


