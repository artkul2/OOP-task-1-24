package gallery.service;

import gallery.model.Photo;

/**
 * Интерфейс сервиса-оптимизатора: задаёт контракт базовых операций
 * над галереей (поиск дубликатов, распределение по каталогам, отчёт).
 */
public interface GalleryOptimizer {
    /** Находит индексы групп полностью одинаковых фото. */
    java.util.List<java.util.List<Integer>> findDuplicateGroups(java.util.List<Photo> photos);

    /** Удаляет дубликаты, оставляя в каждой группе первое фото. */
    gallery.model.Gallery removeDuplicates(gallery.model.Gallery gallery);

    /** Формирует карту «каталог -> список имён файлов» по выбранной стратегии. */
    java.util.Map<String, java.util.List<String>> buildCatalogMap(
            gallery.model.Gallery gallery, CatalogStrategy strategy)
            throws gallery.exceptions.InvalidPhotoMetadataException;

    /** Текстовый отчёт о состоянии галереи. */
    String generateReport(gallery.model.Gallery gallery);
}
