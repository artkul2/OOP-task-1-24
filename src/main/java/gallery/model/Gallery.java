package gallery.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Галерея — агрегат фотографий (модель предметной области).
 */
public class Gallery {

    private final String name;
    private final List<Photo> photos = new ArrayList<>();

    public Gallery(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя галереи не может быть пустым");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** @return неизменяемый «снимок» списка фотографий */
    public List<Photo> getPhotos() {
        return Collections.unmodifiableList(photos);
    }

    public int size() {
        return photos.size();
    }

    public boolean isEmpty() {
        return photos.isEmpty();
    }

    /**
     * Добавляет фото, запрещая дублирование одного и того же объекта дважды.
     *
     * @return {@code true}, если фото добавлено
     */
    public boolean addPhoto(Photo photo) {
        if (photo == null) {
            throw new IllegalArgumentException("Нельзя добавить null-фото в галерею");
        }
        for (Photo p : photos) {
            if (p == photo) {
                return false;
            }
        }
        return photos.add(photo);
    }

    public boolean removePhoto(Photo photo) {
        return photos.remove(photo);
    }

    public Optional<Photo> findByFileName(String fileName) {
        return photos.stream().filter(p -> p.getFileName().equals(fileName)).findFirst();
    }

    /** Суммарный размер всех фото в байтах. */
    public long totalSizeBytes() {
        return photos.stream().mapToLong(Photo::getSizeBytes).sum();
    }
}
