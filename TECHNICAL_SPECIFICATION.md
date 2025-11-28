# Техническое задание
## Мобильное приложение LinkJob

**Версия:** 1.0  
**Дата:** 2024

---

## 1. Общие сведения

**LinkJob** — мобильное приложение для поиска работы и размещения вакансий на платформе Android.

**Целевая аудитория:**
- Соискатели — пользователи, ищущие работу
- Работодатели — пользователи, размещающие вакансии

**Платформа:** Android 7.0 (API 24) — Android 14 (API 35)

---

## 2. Технические требования

**Язык:** Kotlin

**UI:** Jetpack Compose, Material Design 3

**Архитектура:** MVVM, Repository Pattern

**База данных:**
- Room — локальная БД (офлайн)
- Firebase Firestore — облачная БД (синхронизация)

**Сервисы:**
- Firebase Authentication — аутентификация
- Firebase Storage — хранение файлов
- WorkManager — фоновые задачи

**Библиотеки:**
- Coil — загрузка изображений
- Jetpack Navigation Compose — навигация

**Разрешения:**
- INTERNET, ACCESS_NETWORK_STATE
- READ_EXTERNAL_STORAGE, CAMERA (опционально)

---

## 3. Функциональные требования

### 3.1. Аутентификация
- Регистрация (имя, фамилия, email, телефон, username, пароль)
- Вход по email/паролю
- Гостевой режим (просмотр вакансий)

### 3.2. Профиль пользователя
- Просмотр и редактирование данных
- Загрузка фото профиля (аватары или загрузка)
- Просмотр профилей других пользователей

### 3.3. Вакансии
- **Просмотр:** лента вакансий, детальная информация
- **Поиск:** по названию, фильтры (опыт, город, зарплата)
- **Создание:** форма с полями (название, зарплата, опыт, описание, город и др.)
- **Редактирование:** изменение своих вакансий
- **Управление:** список своих вакансий, редактирование, удаление

### 3.4. Сообщения
- Список переписок
- Чат с пользователем
- Уведомления о новых сообщениях

---

## 4. Структура данных

### User (Пользователь)
- id, firstName, lastName, middleName, email, phone, username, password
- profilePhotoId, profilePhotoUrl

### Job (Вакансия)
- id, title, salaryFrom, salaryTo, salaryCurrency, experience
- resume, city, aboutUs, requiredQualities, weOffer, keySkills
- employerId, createdAt

### Message (Сообщение)
- id, senderId, receiverId, text, timestamp

---

## 5. Экраны приложения

**Аутентификация:**
- WelcomeScreen — приветственный экран
- LoginScreen — вход
- RegistrationScreen — регистрация

**Основные:**
- MainScreen — главный экран с навигацией
- FeedScreen — лента вакансий
- JobDetailScreen — детали вакансии
- SearchScreen — поиск вакансий
- CreateJobScreen — создание вакансии
- EditJobScreen — редактирование вакансии
- MyJobsScreen — мои вакансии
- ProfileScreen — профиль пользователя
- MessagesScreen — список переписок
- ChatScreen — чат
- SettingsScreen — настройки

---

## 6. Архитектура

**Слои:**
- **Presentation:** Composable UI, ViewModel, Navigation
- **Domain:** Repository (бизнес-логика)
- **Data:** Room Database, Firebase Firestore, DAO

**Паттерны:**
- MVVM — разделение UI и логики
- Repository — единая точка доступа к данным
- Observer (Flow, State) — реактивное программирование

**Структура проекта:**
```
app/src/main/java/com/example/linkedinapp/
├── data/          # Модели, DAO, Database
├── repository/    # Репозитории
├── viewmodel/     # ViewModel
├── ui/
│   ├── screens/   # Экраны
│   ├── components/# Компоненты
│   └── theme/    # Тема
├── navigation/    # Навигация
├── util/         # Утилиты
└── worker/       # Фоновые задачи
```

---

## 7. Синхронизация данных

**Онлайн:** Firestore → Room (кэширование)  
**Офлайн:** Room → очередь изменений → синхронизация при подключении

**ID:** Firestore ID (String) → хеш → Long ID для Room

---

## 8. Безопасность

- Хеширование паролей (Firebase Authentication)
- Валидация входных данных
- Правила безопасности Firestore
- Безопасное хранение сессий

---

## 9. Производительность

- Lazy loading списков
- Кэширование изображений
- Пагинация данных
- Оптимизация запросов к БД

---

## 10. Развертывание

- Сборка: Debug/Release
- Подпись приложения (keystore)
- Публикация в Google Play Store
- Настройка Firebase (Firestore, Authentication, Storage)

---

## 11. Планируемые улучшения

- Push-уведомления (FCM)
- Избранные вакансии
- Резюме соискателей
- Рейтинги и отзывы
- Многоязычность

---

**Конец документа**
