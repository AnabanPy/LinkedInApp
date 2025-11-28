# Пошаговая инструкция по изменению Application ID

## ⚠️ ВАЖНО: Сначала выберите новый Application ID!

Примеры:
- `com.yourname.linkjob`
- `ru.yourname.linkjob` 
- `io.yourname.linkjob`
- `app.yourname.linkjob`

**Выбранный ID:** `________________________` (запишите его здесь)

---

## Шаг 1: Обновить build.gradle.kts

В файле `app/build.gradle.kts` замените `com.yourname.linkjob` на ваш реальный Application ID в строках 10 и 14.

---

## Шаг 2: Переименовать Package в Android Studio

### Вариант А: Через меню Refactor (Рекомендуется)

1. **В Project View** (слева) найдите папку:
   ```
   app/src/main/java/com/example/linkedinapp
   ```

2. **Правой кнопкой мыши** на папке `com.example.linkedinapp`

3. Выберите **"Refactor" → "Rename"** (или нажмите `Shift+F6`)

4. В диалоговом окне:
   - Введите новый package name (например: `com.yourname.linkjob`)
   - ✅ **Убедитесь, что выбрано "Rename package"**
   - ✅ Отметьте все опции:
     - [x] Search in comments and strings
     - [x] Search for text occurrences
     - [x] Rename directory

5. Нажмите **"Refactor"**

6. Android Studio покажет предпросмотр изменений - нажмите **"Do Refactor"**

### Вариант Б: Через Search & Replace

Если Refactor не сработал, используйте:
1. **Edit → Find → Replace in Files** (`Ctrl+Shift+R`)
2. Find: `com.example.linkedinapp`
3. Replace: `com.yourname.linkjob` (ваш новый ID)
4. Scope: "Project Files"
5. Нажмите "Replace All"

**Затем вручную переименуйте папки:**
- `com/example/linkedinapp` → `com/yourname/linkjob`

---

## Шаг 3: Проверить AndroidManifest.xml

Откройте `app/src/main/AndroidManifest.xml` и убедитесь, что:
- Используются относительные пути (`.MainActivity` вместо полного пути)
- Нет хардкода старого package name

**Должно быть:**
```xml
<activity android:name=".MainActivity" ...>
```

---

## Шаг 4: Обновить Firebase (ОБЯЗАТЕЛЬНО!)

### 4.1. Firebase Console

1. Перейдите: https://console.firebase.google.com/
2. Выберите проект: **analog-linkedin**
3. Нажмите ⚙️ (Settings) → **Project settings**
4. Прокрутите вниз до **"Your apps"**
5. Найдите ваше Android приложение
6. Два варианта:

   **Вариант А (Рекомендуется): Добавить новое приложение**
   - Нажмите **"Add app"** → выберите Android
   - Введите новый Application ID
   - Скачайте новый `google-services.json`
   - Замените старый файл в `app/google-services.json`

   **Вариант Б: Изменить существующее**
   - НЕ РЕКОМЕНДУЕТСЯ - лучше создать новое

### 4.2. Обновить SHA-1/SHA-256

**После создания keystore** выполните:
```bash
keytool -list -v -keystore linkjob-release.keystore -alias linkjob
```

Скопируйте SHA-1 и SHA-256, добавьте в Firebase Console:
- Project Settings → Your apps → Android app
- Scroll to "SHA certificate fingerprints"
- Add fingerprint

---

## Шаг 5: Синхронизировать проект

В Android Studio:
1. Нажмите **"Sync Project with Gradle Files"** (🔄 иконка вверху)
2. Или: **File → Sync Project with Gradle Files**

---

## Шаг 6: Проверить сборку

```bash
./gradlew clean
./gradlew assembleDebug
```

Если нет ошибок - всё готово! ✅

---

## ✅ Чеклист

- [ ] Выбран новый Application ID
- [ ] Обновлен `build.gradle.kts` (namespace и applicationId)
- [ ] Переименован package через Refactor
- [ ] Проверен AndroidManifest.xml
- [ ] Обновлен Firebase Console
- [ ] Заменен `google-services.json`
- [ ] Синхронизирован проект
- [ ] Проект успешно собирается

---

## ⚠️ Если возникли проблемы

1. **Ошибка "Package not found"**
   - Убедитесь, что папки переименованы
   - Сделайте Clean Project (Build → Clean Project)

2. **Ошибка Firebase**
   - Проверьте `google-services.json` - там должен быть новый package_name
   - Убедитесь, что файл в правильной папке: `app/google-services.json`

3. **Ошибки компиляции**
   - File → Invalidate Caches → Invalidate and Restart
   - Build → Rebuild Project


