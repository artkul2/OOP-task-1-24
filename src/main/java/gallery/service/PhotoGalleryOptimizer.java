package gallery.service;

import gallery.exceptions.InvalidPhotoMetadataException;
import gallery.model.Gallery;
import gallery.model.Photo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 Оптимизирует галерею, удаляя совпавшие фотографии
 */
public class PhotoGalleryOptimizer implements GalleryOptimizer {

    /*
     Группирует индексы фото с одинаковым SHA-256
     Возвращает только группы размера >= 2 (только дублирующиеся фотографии)
     */
    @Override
    public List<List<Integer>> findDuplicateGroups(List<Photo> photos) {
        if (photos == null) {
            throw new IllegalArgumentException("Список фото не может быть null");
        }
        Map<String, List<Integer>> byHash = new LinkedHashMap<>();
        for (int i = 0; i < photos.size(); i++) {
            byHash.computeIfAbsent(photos.get(i).sha256(), k -> new ArrayList<>()).add(i);
        }
        List<List<Integer>> groups = new ArrayList<>();
        for (List<Integer> g : byHash.values()) {
            if (g.size() >= 2) {
                groups.add(g);
            }
        }
        return groups;
    }

    /*
     Удаляет из галереи полностью одинаковые фото, оставляя первое из них
     */
    public Gallery removeDuplicates(Gallery gallery) {
        if (gallery == null) {
            throw new IllegalArgumentException("Галерея не может быть null");
        }
        Gallery cleaned = new Gallery(gallery.getName() + " (очищенная)");
        List<Photo> photos = new ArrayList<>(gallery.getPhotos());
        List<List<Integer>> dupGroups = findDuplicateGroups(photos);
        List<Integer> toRemove = new ArrayList<>();
        for (List<Integer> group : dupGroups) {
            for (int idx = 1; idx < group.size(); idx++) { // первое оставляем, остальные удаляются
                toRemove.add(group.get(idx));
            }
        }
        for (int i = 0; i < photos.size(); i++) {
            if (!toRemove.contains(i)) {
                cleaned.addPhoto(photos.get(i));
            }
        }
        return cleaned;
    }

    /*
     Распределяет фото по каталогам согласно выбранной стратегии
     */
    @Override
    public Map<String, List<String>> buildCatalogMap(Gallery gallery, CatalogStrategy strategy)
            throws InvalidPhotoMetadataException {
        if (gallery == null || strategy == null) {
            throw new IllegalArgumentException("Галерея и стратегия не могут быть null");
        }
        Map<String, List<String>> catalog = new LinkedHashMap<>();
        for (Photo photo : gallery.getPhotos()) {
            String dir = strategy.resolveCatalog(photo); // позволяет делить по форматам, годам, месяцам и сезонам в зависимости от стратегии
            catalog.computeIfAbsent(dir, k -> new ArrayList<>()).add(photo.getFileName());
        }
        return catalog;
    }

    /*
     Не краткое описание галереи, для вывода в консоль
     */
    @Override
    public String generateReport(Gallery gallery) {
        if (gallery == null) {
            throw new IllegalArgumentException("Галерея не может быть null");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("=== Отчёт по галерее '").append(gallery.getName()).append("' ===\n");
        sb.append("Фотографий: ").append(gallery.size()).append('\n');
        sb.append("Суммарный размер: ").append(gallery.totalSizeBytes()).append(" байт\n");
        if (gallery.isEmpty()) {
            sb.append("(галерея пуста)\n");
            return sb.toString();
        }
        List<Photo> photos = new ArrayList<>(gallery.getPhotos());
        List<List<Integer>> dups = findDuplicateGroups(photos);
        sb.append("Групп дубликатов: ").append(dups.size()).append('\n');
        for (List<Integer> g : dups) {
            sb.append("  - ").append(g.stream().map(i -> photos.get(i).getFileName())
                    .toList()).append('\n');
        }
        for (Photo p : photos) {
            sb.append("  * ").append(p.describe()).append('\n');
        }
        return sb.toString();
    }
}
