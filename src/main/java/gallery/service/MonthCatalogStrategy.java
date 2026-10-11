package gallery.service;

import gallery.model.Photo;

import java.time.LocalDate;

/**
 * Каталог вида "год/месяц", например "2023/07".
 * Наследник {@link YearBasedCatalogStrategy}.
 */
public class MonthCatalogStrategy extends YearBasedCatalogStrategy {

    @Override
    public String getName() {
        return "По годам и месяцам";
    }

    @Override
    protected String subFolder(Photo photo, LocalDate date) {
        return String.format("%02d", date.getMonthValue());
    }
}
