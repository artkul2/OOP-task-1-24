package gallery.service;

import gallery.model.Photo;

/*
 Стратегия распределения фотографий по каталогам
 */
public interface CatalogStrategy {
    String getName(); // выводит название стратегии

    /*
     Выводит параметр фотографии photo, по которому проходит распределение
     */
    String resolveCatalog(Photo photo) throws gallery.exceptions.InvalidPhotoMetadataException;
}
