# Инструкция по настройке Release Signing

## Шаг 1: Создать Keystore

Выполните команду в корне проекта:

```bash
keytool -genkey -v -keystore linkjob-release.keystore -alias linkjob -keyalg RSA -keysize 2048 -validity 10000
```

**Важно:** Запишите все данные:
- **Store Password** - пароль для keystore
- **Key Password** - пароль для ключа (можно тот же)
- **Alias** - имя ключа (в примере: `linkjob`)

## Шаг 2: Создать keystore.properties

1. Скопируйте `keystore.properties.example` в `keystore.properties`
2. Заполните данные:

```properties
storeFile=linkjob-release.keystore
storePassword=ВАШ_ПАРОЛЬ_STORE
keyAlias=linkjob
keyPassword=ВАШ_ПАРОЛЬ_KEY
```

## Шаг 3: Добавить в .gitignore

Убедитесь, что в `.gitignore` есть:
```
keystore.properties
*.keystore
linkjob-release.keystore
```

## Шаг 4: Активировать в build.gradle.kts

1. Раскомментируйте блок `signingConfigs` в `app/build.gradle.kts`
2. Раскомментируйте строку `signingConfig = signingConfigs.getByName("release")` в `buildTypes.release`

## Шаг 5: Собрать Release APK/AAB

```bash
./gradlew bundleRelease
```

Результат будет в: `app/build/outputs/bundle/release/app-release.aab`

---

## ⚠️ КРИТИЧЕСКИ ВАЖНО

**Сохраните keystore и пароли в БЕЗОПАСНОМ месте!**
- Без keystore вы НЕ сможете обновлять приложение в Google Play
- Google не может восстановить потерянный keystore
- Рекомендуется хранить в менеджере паролей (1Password, LastPass, Bitwarden)

## Резервное копирование

1. Сохраните `linkjob-release.keystore` в нескольких местах
2. Сохраните пароли в менеджере паролей
3. Запишите alias (имя ключа)

