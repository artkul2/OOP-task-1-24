package gallery.model;

import java.util.Objects;

/**
 * Метаданные фотографии (EXIF-подобные).
 * Неизменяемый record.
 *
 * @param cameraModel    модель камеры (может быть {@code null}, если не задана)
 * @param photographer   автор снимка (может быть {@code null})
 * @param widthPx        ширина в пикселях, >= 0
 * @param heightPx       высота в пикселях, >= 0
 * @param takenAtEpochSec unix-время съёмки в секундах; 0 означает «дата неизвестна»
 */
public record PhotoMetadata(String cameraModel,
                            String photographer,
                            int widthPx,
                            int heightPx,
                            long takenAtEpochSec) {

    /**
     * Компактный конструктор проверяет инварианты записи.
     *
     * @throws IllegalArgumentException если размеры отрицательные или время съёмки отрицательное
     */
    public PhotoMetadata {
        if (widthPx < 0 || heightPx < 0) {
            throw new IllegalArgumentException("Размеры фото не могут быть отрицательными: "
                    + widthPx + "x" + heightPx);
        }
        if (takenAtEpochSec < 0) {
            throw new IllegalArgumentException("Время съёмки не может быть отрицательным: " + takenAtEpochSec);
        }
        cameraModel = cameraModel == null ? "" : cameraModel.trim();
        photographer = photographer == null ? "" : photographer.trim();
    }

    /** Метод-фабрика для метаданных без даты съёмки. */
    public static PhotoMetadata unknownDate(String cameraModel, String photographer, int w, int h) {
        return new PhotoMetadata(cameraModel, photographer, w, h, 0L);
    }

    /** Есть ли известная дата съёмки. */
    public boolean hasDate() {
        return takenAtEpochSec > 0;
    }

    /** Количество мегапикселей. */
    public double megaPixels() {
        return (widthPx * (double) heightPx) / 1_000_000.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhotoMetadata other)) return false;
        return widthPx == other.widthPx
                && heightPx == other.heightPx
                && takenAtEpochSec == other.takenAtEpochSec
                && Objects.equals(cameraModel, other.cameraModel)
                && Objects.equals(photographer, other.photographer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cameraModel, photographer, widthPx, heightPx, takenAtEpochSec);
    }
}
