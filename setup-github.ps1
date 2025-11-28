# Скрипт для настройки Git репозитория и подключения к GitHub
# Запустите этот скрипт в PowerShell: .\setup-github.ps1

Write-Host "=== Настройка Git репозитория для LinkedInApp ===" -ForegroundColor Green
Write-Host ""

# Проверка установки Git
Write-Host "Проверка установки Git..." -ForegroundColor Yellow
try {
    $gitVersion = git --version
    Write-Host "✓ Git установлен: $gitVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Git не найден!" -ForegroundColor Red
    Write-Host "Пожалуйста, установите Git: https://git-scm.com/download/win" -ForegroundColor Yellow
    Write-Host "После установки перезапустите этот скрипт." -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Проверка, инициализирован ли уже репозиторий
if (Test-Path .git) {
    Write-Host "✓ Git репозиторий уже инициализирован" -ForegroundColor Green
} else {
    Write-Host "Инициализация Git репозитория..." -ForegroundColor Yellow
    git init
    Write-Host "✓ Репозиторий инициализирован" -ForegroundColor Green
}

Write-Host ""

# Проверка настроек пользователя
Write-Host "Проверка настроек Git..." -ForegroundColor Yellow
$userName = git config user.name
$userEmail = git config user.email

if ($userName -and $userEmail) {
    Write-Host "✓ Настроено имя: $userName" -ForegroundColor Green
    Write-Host "✓ Настроен email: $userEmail" -ForegroundColor Green
} else {
    Write-Host "⚠ Настройки пользователя не найдены" -ForegroundColor Yellow
    Write-Host "Выполните следующие команды:" -ForegroundColor Yellow
    Write-Host "  git config --global user.name `"Ваше Имя`"" -ForegroundColor Cyan
    Write-Host "  git config --global user.email `"ваш.email@example.com`"" -ForegroundColor Cyan
    Write-Host ""
    $continue = Read-Host "Продолжить без настройки? (y/n)"
    if ($continue -ne "y") {
        exit 0
    }
}

Write-Host ""

# Добавление файлов
Write-Host "Добавление файлов в репозиторий..." -ForegroundColor Yellow
git add .
Write-Host "✓ Файлы добавлены" -ForegroundColor Green

Write-Host ""

# Проверка статуса
Write-Host "Статус репозитория:" -ForegroundColor Yellow
git status --short

Write-Host ""

# Создание коммита
$hasCommits = git rev-parse --verify HEAD 2>$null
if ($hasCommits) {
    Write-Host "✓ Коммиты уже существуют" -ForegroundColor Green
} else {
    Write-Host "Создание первого коммита..." -ForegroundColor Yellow
    git commit -m "Initial commit"
    Write-Host "✓ Первый коммит создан" -ForegroundColor Green
}

Write-Host ""
Write-Host "=== Следующие шаги ===" -ForegroundColor Green
Write-Host ""
Write-Host "1. Создайте репозиторий на GitHub:" -ForegroundColor Yellow
Write-Host "   https://github.com/new" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. После создания репозитория выполните:" -ForegroundColor Yellow
Write-Host "   git remote add origin https://github.com/ВАШ_USERNAME/LinkedInApp.git" -ForegroundColor Cyan
Write-Host "   git branch -M main" -ForegroundColor Cyan
Write-Host "   git push -u origin main" -ForegroundColor Cyan
Write-Host ""
Write-Host "Подробная инструкция в файле GITHUB_SETUP.md" -ForegroundColor Yellow

