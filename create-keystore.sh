#!/bin/bash
# Скрипт для создания keystore для подписи release сборки
# Для публикации в Google Play

echo "========================================"
echo "Создание Keystore для LinkJob"
echo "========================================"
echo ""

# Запрашиваем данные
read -p "Имя keystore файла [linkjob-release.keystore]: " KEYSTORE_NAME
KEYSTORE_NAME=${KEYSTORE_NAME:-linkjob-release.keystore}

read -p "Alias ключа [linkjob]: " KEY_ALIAS
KEY_ALIAS=${KEY_ALIAS:-linkjob}

echo ""
echo "Создаю keystore..."
echo "ВАЖНО: Запишите все пароли в безопасном месте!"
echo ""

keytool -genkey -v -keystore "$KEYSTORE_NAME" -alias "$KEY_ALIAS" -keyalg RSA -keysize 2048 -validity 10000

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "Keystore успешно создан!"
    echo "========================================"
    echo ""
    echo "Следующие шаги:"
    echo "1. Создайте keystore.properties в корне проекта"
    echo "2. Заполните его данными (см. keystore.properties.example)"
    echo "3. Раскомментируйте signing config в app/build.gradle.kts"
    echo ""
    echo "ВАЖНО: Сохраните $KEYSTORE_NAME и пароли в безопасном месте!"
    echo ""
else
    echo ""
    echo "ОШИБКА: Не удалось создать keystore"
    echo ""
fi

