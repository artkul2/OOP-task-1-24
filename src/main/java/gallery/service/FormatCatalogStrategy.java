package gallery.service;

import gallery.model.Photo;

/*
 Раскладка по форматам: "jpeg", "png" и т.д.
 */
public class FormatCatalogStrategy implements CatalogStrategy {

    @Override
    public String getName() {
        return "По форматам файлов";
    }

    @Override
    public String resolveCatalog(Photo photo) {
        return photo.getFormat().name().toLowerCase();
    }
}
