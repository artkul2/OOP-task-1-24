package gallery.service;

import gallery.model.Photo;

/**
 * Стратегия распределения фотографий по каталогам.
 */
public interface CatalogStrategy {
    /** Имя стратегии для вывода в консоль. */
    String getName();

    /**
     * Возвращает относительный путь каталога, куда следует поместить фото.
     *
     * @param photo фотография
     * @return путь вида "2023/summer" или "png"
     * @throws gallery.exceptions.InvalidPhotoMetadataException если для этой стратегии
     *         метаданные фотографии недостаточны (например, нет даты съёмки)
     */
    String resolveCatalog(Photo photo) throws gallery.exceptions.InvalidPhotoMetadataException;
}
