package gallery.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/*
 Цифровая фотография с полной EXIF-информацией
 */
public class DigitalPhoto extends Photo {

    public DigitalPhoto(String fileName, byte[] content, PhotoMetadata metadata) {
        super(fileName, content, metadata);
    }

    public LocalDate getTakenDate() {
        if (!metadata.hasDate()) {
            return null;
        }
        return Instant.ofEpochSecond(metadata.takenAtEpochSec())
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
    }


    public Season getSeason() {
        LocalDate d = getTakenDate();
        return d == null ? null : Season.ofMonth(d.getMonthValue());
    }

    /*
     Похожесть двух цифровых фото: совпадение автора, камеры и близость времени съёмки.
     Используется для обнаружения серий/почти-дубликатов.
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
            if (diff <= 5) {
                score += 0.4;
            } else if (diff <= 3600) {
                score += 0.2;
            }
        }
        return score;
    }

    @Override
    public String describe() {
        LocalDate d = getTakenDate();
        return super.describe() + String.format(" дата: %s, камера: %s",
                d == null ? "неизвестно" : d,
                metadata.cameraModel().isEmpty() ? "нет данных" : metadata.cameraModel());
    }
}
