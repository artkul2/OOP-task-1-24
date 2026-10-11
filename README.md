# Оптимизатор галереи фотографий (консольное приложение)

Учебный проект по дисциплине **«Объектно-ориентированное программирование»**.

Консольное приложение, которое умеет:
- **удалять одинаковые (дублирующиеся) фотографии** — по побайтовому совпадению содержимого (SHA-256);
- **распределять фотографии по каталогам** — по сезонам года, по месяцам или по форматам файлов (паттерн «Стратегия»);
- **обрабатывать метаданные** — EXIF-подобная информация: камера, автор, разрешение, дата съёмки.

## Технологии

- Java 17 (используются `record`, enhanced switch-синтаксис, Stream API)
- JUnit 5 для юнит-тестов (`junit-platform-console-standalone`)
- Сборка — напрямую `javac` (скрипты в `scripts/`), без Maven/Gradle

## Структура проекта

```
src/main/java/gallery/
├── model/                       # Модель предметной области
│   ├── PhotoFormat.java         # enum: форматы + распознавание по магическим байтам
│   ├── Season.java              # enum: времена года, ofMonth(1..12)
│   ├── PhotoMetadata.java       # record: неизменяемые метаданные фото
│   ├── Similarity.java          # interface: similarityTo / isDuplicateOf
│   ├── Photo.java               # abstract class (базовый класс), implements Similarity
│   ├── DigitalPhoto.java        # наследник Photo (цифровое фото с EXIF)
│   ├── ScannedPhoto.java        # наследник Photo (скан плёнки: DPI, глубина цвета)
│   └── Gallery.java             # агрегат фотографий
├── exceptions/
│   └── InvalidPhotoMetadataException.java   # СОБСТВЕННОЕ проверяемое исключение
├── service/
│   ├── GalleryOptimizer.java    # interface сервиса
│   ├── PhotoGalleryOptimizer.java  # реализация: дубликаты, каталоги, отчёт
│   ├── CatalogStrategy.java     # interface стратегии раскладки
│   ├── YearBasedCatalogStrategy.java # abstract: общие для «годовых» стратегий
│   ├── SeasonCatalogStrategy.java    # "2023/Лето"
│   ├── MonthCatalogStrategy.java     # "2023/07"
│   └── FormatCatalogStrategy.java    # "jpeg", "png"...
└── console/
    └── GalleryApp.java          # точка входа, демо ВСЕХ методов
src/test/java/gallery/service/
    └── PhotoGalleryOptimizerTest.java  # 26 юнит-тестов, включая краевые случаи
```

## Соответствие требованиям

| Требование | Реализация |
|---|---|
| Консольное приложение | `gallery.console.GalleryApp` |
| Наследование | `DigitalPhoto`, `ScannedPhoto` ← `Photo`; стратегии ← `YearBasedCatalogStrategy` |
| Имплементация интерфейса | `Photo implements Similarity`, `PhotoGalleryOptimizer implements GalleryOptimizer`, стратегии `implements CatalogStrategy` |
| Классы, enum, record модели | классы `Photo/Gallery/...`, enum `PhotoFormat/Season`, record `PhotoMetadata` |
| Собственное исключение | `InvalidPhotoMetadataException` (проверяемое, бросается при отсутствии даты съёмки) |
| Вывод всех методов в консоль | `bash scripts/run-demo.sh` |
| Юнит-тесты (≥15) | 26 тестов JUnit 5, включая исключения и особые случаи |
| Javadoc | документированы все классы и сложные методы |

## Сборка и запуск

```bash
# компиляция
bash scripts/build.sh

# консольное демо (показывает работу всех методов)
bash scripts/run-demo.sh

# юнит-тесты (нужен junit-platform-console-standalone.jar)
mkdir -p lib && curl -sSL -o lib/junit-platform-console-standalone.jar \
  https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar
bash scripts/run-tests.sh
```

## Пример вывода демо

```
[7] findDuplicateGroups:
    группа индексов [0, 1] -> [IMG_0001.JPG, IMG_0001_copy.JPG]

[8] removeDuplicates:
    было: 4, стало: 3

[9] Стратегии CatalogStrategy:
    [По годам и сезонам] {2023/Лето=[IMG_0001.JPG, IMG_0002.JPG], 1995/Зима=[scan_1995.png]}
    [По форматам файлов] {jpeg=[IMG_0001.JPG, IMG_0002.JPG], png=[scan_1995.png]}

[10] Собственное исключение InvalidPhotoMetadataException:
    Поймано: Некорректные метаданные у 'no_exif.jpg': отсутствует дата съёмки
```
