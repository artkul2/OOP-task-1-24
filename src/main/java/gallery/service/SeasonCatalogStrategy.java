package gallery.service;

import gallery.model.Photo;
import gallery.model.Season;

import java.time.LocalDate;

/*
 Каталог вида "год/сезон", например "2023/Лето"
 */
public class SeasonCatalogStrategy extends YearBasedCatalogStrategy {

    @Override
    public String getName() {
        return "По годам и сезонам";
    }

    @Override
    protected String subFolder(Photo photo, LocalDate date) {
        return Season.ofMonth(date.getMonthValue()).getDisplayName();
    }
}
