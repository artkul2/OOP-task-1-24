package gallery.service;

import gallery.exceptions.InvalidPhotoMetadataException;
import gallery.model.DigitalPhoto;
import gallery.model.Photo;
import gallery.model.Season;

import java.time.LocalDate;

/*
 Стратегия распределения по годам, реализует методы, которые наследуют распределения по месяцам и сезонам
 */
public abstract class YearBasedCatalogStrategy implements CatalogStrategy {

    @Override
    public String resolveCatalog(Photo photo) throws InvalidPhotoMetadataException {
        LocalDate date = extractDate(photo);
        return String.format("%d/%s", date.getYear(), subFolder(photo, date));
    }

    /*
     Извлекает дату съёмки
     */
    protected LocalDate extractDate(Photo photo) throws InvalidPhotoMetadataException {
        long epoch = photo.getMetadata().takenAtEpochSec();
        if (epoch == 0) {
            throw new InvalidPhotoMetadataException(
                    photo.getFileName(), "отсутствует дата съёмки");
        }
        if (photo instanceof DigitalPhoto dp) {
            return dp.getTakenDate();
        }
        return java.time.Instant.ofEpochSecond(epoch)
                .atZone(java.time.ZoneOffset.UTC).toLocalDate();
    }

    /*
     Формирует дополнительные параметры, которые образуются в наследуемых классах
     */
    protected abstract String subFolder(Photo photo, LocalDate date);
}
