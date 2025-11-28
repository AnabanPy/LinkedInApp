# Быстрый старт для публикации в Google Play

## 🔴 Критически важно (сделать в первую очередь)

### 1. Изменить Application ID
**Действие:** Откройте `CHANGE_APPLICATION_ID.md` и следуйте инструкциям

**Важно:** `com.example.linkedinapp` запрещен для публикации!

---

### 2. Создать Keystore

**Windows:**
```bash
create-keystore.bat
```

**Linux/Mac:**
```bash
chmod +x create-keystore.sh
./create-keystore.sh
```

**Или вручную:**
```bash
keytool -genkey -v -keystore linkjob-release.keystore -alias linkjob -keyalg RSA -keysize 2048 -validity 10000
```

**Затем:**
1. Создайте `keystore.properties` (скопируйте из `keystore.properties.example`)
2. Заполните пароли
3. Раскомментируйте signing config в `app/build.gradle.kts` (строки 42-54 и 35)

---

### 3. Privacy Policy

1. Откройте `PRIVACY_POLICY_TEMPLATE.md`
2. Заполните шаблон своими данными
3. Разместите на веб-сайте (GitHub Pages, Firebase Hosting и т.д.)
4. Получите публичную ссылку
5. Добавьте ссылку в Google Play Console

---

## ✅ Что уже готово

- ✅ ProGuard включен
- ✅ Target SDK = 35
- ✅ Release конфигурация настроена
- ✅ ProGuard rules добавлены
- ✅ .gitignore настроен
- ✅ Backup rules оптимизированы

---

## 📦 Сборка Release

**Windows:**
```bash
build-release.bat
```

**Linux/Mac:**
```bash
chmod +x build-release.sh
./build-release.sh
```

**Или вручную:**
```bash
./gradlew bundleRelease
```

Файл будет в: `app/build/outputs/bundle/release/app-release.aab`

---

## 📋 Полный чеклист

Смотрите `GOOGLE_PLAY_CHECKLIST.md` для полного списка всех требований.

---

## ⚠️ Важные напоминания

1. **Сохраните keystore и пароли** в безопасном месте (менеджер паролей)
2. **Не коммитьте** `keystore.properties` и `*.keystore` в Git
3. **Обновите Firebase** после изменения Application ID
4. **Протестируйте** release сборку перед загрузкой

---

## 🆘 Проблемы?

- Ошибка signing: проверьте `keystore.properties` и пути в `build.gradle.kts`
- Ошибка сборки: убедитесь, что все шаги выполнены
- Firebase ошибки: проверьте `google-services.json` после смены Application ID


