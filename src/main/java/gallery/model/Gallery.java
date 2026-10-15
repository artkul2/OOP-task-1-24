package gallery.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Gallery {

    private final String name;
    private final List<Photo> photos = new ArrayList<>();

    public Gallery(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Photo> getPhotos() {
        return Collections.unmodifiableList(photos);
    }

    public int size() {
        return photos.size();
    }

    public boolean isEmpty() {
        return photos.isEmpty();
    }

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

        for (int i = 0; i < photos.size(); i++){
            if (photos.get(i).fileName == fileName){

            }
        }
        return photos.stream().filter(p -> p.getFileName().equals(fileName)).findFirst();
    }

    /*
    Суммарный размер всех фото в байтах.
    */
    public long totalSizeBytes() {
        return photos.stream().mapToLong(Photo::getSizeBytes).sum();
    }
}
