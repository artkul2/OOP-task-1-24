package gallery.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Цифровая фотография с полной EXIF-информацией.
 * Наследник {@link Photo}; похожая считается по дате съёмки и автору.
 */
public class DigitalPhoto extends Photo {

    private static final long SIMILAR_TIME_WINDOW_SEC = 5; // снимки в серии — ±5 секунд

    public DigitalPhoto(String fileName, byte[] content, PhotoMetadata metadata) {
        super(fileName, content, metadata);
    }

    /** Дата съёмки (null, если дата неизвестна). */
    public LocalDate getTakenDate() {
        if (!metadata.hasDate()) {
            return null;
        }
        return Instant.ofEpochSecond(metadata.takenAtEpochSec())
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
    }

    /** Сезон, в котором был сделан снимок; UNKNOWN-сезона нет — при отсутствии даты бросаем исключение сервиса выше. */
    public Season getSeason() {
        LocalDate d = getTakenDate();
        return d == null ? null : Season.ofMonth(d.getMonthValue());
    }

    /**
     * Похожесть двух цифровых фото: совпадение автора, камеры и близость времени съёмки.
     * Используется для обнаружения серий/почти-дубликатов.
     */
    @Override
    public double similarityTo(Similarity other) {
        if (!(other instanceof DigitalPhoto p)) {
            return 0.0;
        }
        if (isDuplicateOf(other)) {
            return 1.0;
        }
        double score = 0.0;
        if (metadata.cameraModel().equals(p.metadata.cameraModel()) && !metadata.cameraModel().isEmpty()) {
            score += 0.3;
        }
        if (metadata.photographer().equals(p.metadata.photographer()) && !metadata.photographer().isEmpty()) {
            score += 0.3;
        }
        if (metadata.hasDate() && p.metadata.hasDate()) {
            long diff = Math.abs(metadata.takenAtEpochSec() - p.metadata.takenAtEpochSec());
            if (diff <= SIMILAR_TIME_WINDOW_SEC) {
                score += 0.4;
            } else if (diff <= 3600) {
                score += 0.2;
            }
        }
        return Math.min(score, 1.0);
    }

    @Override
    public String describe() {
        LocalDate d = getTakenDate();
        return super.describe() + String.format(" снято: %s, камера: %s",
                d == null ? "неизвестно" : d,
                metadata.cameraModel().isEmpty() ? "нет данных" : metadata.cameraModel());
    }
}
