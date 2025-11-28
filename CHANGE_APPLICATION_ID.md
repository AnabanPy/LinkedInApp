# Инструкция по изменению Application ID

## ⚠️ ВАЖНО

Текущий Application ID `com.example.linkedinapp` **запрещен** для публикации в Google Play.

## Шаг 1: Выбрать новый Application ID

Выберите уникальный ID в формате: `com.вашедоменноеимя.linkjob`

Примеры:
- `com.yourcompany.linkjob`
- `ru.yourname.linkjob`
- `io.yourdomain.linkjob`
- `app.linkjob`

**Рекомендация:** Используйте обратный DNS формат с вашим доменом или именем.

## Шаг 2: Обновить в коде

### 2.1. build.gradle.kts

Изменить:
```kotlin
namespace = "com.example.linkedinapp"
applicationId = "com.example.linkedinapp"
```

На:
```kotlin
namespace = "com.вашедоменноеимя.linkjob"
applicationId = "com.вашедоменноеимя.linkjob"
```

### 2.2. Переименовать package

В Android Studio:
1. Правой кнопкой на `com.example.linkedinapp`
2. Refactor → Rename
3. Выберите "Rename package"
4. Введите новый package name
5. Выберите все опции для обновления ссылок

### 2.3. Обновить AndroidManifest.xml

Проверить, что нет хардкода старого package name.

## Шаг 3: Обновить Firebase

### 3.1. Firebase Console

1. Перейдите в [Firebase Console](https://console.firebase.google.com/)
2. Выберите проект `analog-linkedin`
3. Project Settings → Your apps → Android app
4. Нажмите "Add app" или удалите старый и добавьте новый
5. Введите новый Application ID
6. Скачайте новый `google-services.json`
7. Замените старый файл `app/google-services.json`

### 3.2. Обновить SHA-1/SHA-256 (для Auth)

1. Получите SHA-1/SHA-256 вашего release keystore:
```bash
keytool -list -v -keystore linkjob-release.keystore -alias linkjob
```

2. Добавьте в Firebase Console:
   - Project Settings → Your apps → Android app
   - Scroll to "SHA certificate fingerprints"
   - Add fingerprint

## Шаг 4: Проверить все ссылки

Убедитесь, что везде обновился package name:
- Все импорты в Kotlin файлах
- AndroidManifest.xml
- Build файлы

## Шаг 5: Пересобрать

```bash
./gradlew clean
./gradlew assembleRelease
```

## После изменения

**⚠️ НЕ публикуйте приложение со старым ID, если уже начали процесс!**
Новый Application ID = новое приложение в Google Play.

