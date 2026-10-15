package gallery.console;

import gallery.exceptions.InvalidPhotoMetadataException;
import gallery.model.DigitalPhoto;
import gallery.model.Gallery;
import gallery.model.Photo;
import gallery.model.PhotoFormat;
import gallery.model.PhotoMetadata;
import gallery.model.ScannedPhoto;
import gallery.service.FormatCatalogStrategy;
import gallery.service.MonthCatalogStrategy;
import gallery.service.PhotoGalleryOptimizer;
import gallery.service.SeasonCatalogStrategy;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;


public final class GalleryApp {

    private GalleryApp() {
    }

    /*
     Метод генерации файлов jpeg из текста
     */
    private static byte[] jpeg(String payload) {
        // заголовок JPEG + полезная нагрузка — чтобы формат определялся по магическим байтам
        byte[] head = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        byte[] body = payload.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[head.length + body.length];
        System.arraycopy(head, 0, out, 0, head.length);
        System.arraycopy(body, 0, out, head.length, body.length);
        return out;
    }

    private static long epoch(int y, int m, int d) {
        return LocalDate.of(y, m, d).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
    }

    public static void main(String[] args) {
        System.out.println("############################################################");
        System.out.println("#        ОПТИМИЗАТОР ГАЛЕРЕИ ФОТОГРАФИЙ — ДЕМО             #");
        System.out.println("############################################################\n");

        // --- 1/2. Перечисления модели ---
        System.out.println("[1] Перечисление PhotoFormat:");
        for (PhotoFormat f : PhotoFormat.values()) {
            System.out.printf("    %-7s ext=%-5s mime=%s%n", f, f.getExtension(), f.getMimeType());
        }
        System.out.println("    fromFileName(\"photo.PNG\") -> " + PhotoFormat.fromFileName("photo.PNG"));
        System.out.println("    fromFileName(\"noext\")     -> " + PhotoFormat.fromFileName("noext"));
        System.out.println("    fromMagicBytes(jpeg)      -> " + PhotoFormat.fromMagicBytes(jpeg("x")));

        System.out.println("\n[2] Перечисление Season:");
        for (int m = 1; m <= 12; m++) {
            System.out.printf("    месяц %2d -> %s (%s)%n", m, SeasonHelper.name(m), SeasonHelper.months(m));
        }

        // --- 3. Record метаданных ---
        System.out.println("\n[3] Record PhotoMetadata:");
        PhotoMetadata md1 = new PhotoMetadata("Canon EOS R", "Анна", 4000, 3000, epoch(2023, 7, 15));
        PhotoMetadata md2 = PhotoMetadata.unknownDate("Nikon Z6", "Борис", 1000, 800);
        System.out.println("    md1 = " + md1);
        System.out.println("    md2 = " + md2);
        System.out.println("    md1.hasDate()=" + md1.hasDate() + ", md2.hasDate()=" + md2.hasDate());
        System.out.printf("    md1.megaPixels()=%.1f%n", md1.megaPixels());

        // --- 4. Классы DigitalPhoto и ScannedPhoto модели ---
        System.out.println("\n[4] DigitalPhoto и ScannedPhoto:");
        DigitalPhoto d1 = new DigitalPhoto("IMG_0001.JPG", jpeg("sunset"), md1);
        DigitalPhoto d2 = new DigitalPhoto("IMG_0002.JPG", jpeg("beach"),
                new PhotoMetadata("Canon EOS R", "Анна", 4000, 3000, epoch(2023, 7, 15)));
        DigitalPhoto d1copy = new DigitalPhoto("IMG_0001_copy.JPG", jpeg("sunset"), md1);
        ScannedPhoto s1 = new ScannedPhoto("scan_1995.png", jpeg("axe"),
                new PhotoMetadata("", "неизвестен", 1200, 900, epoch(1995, 12, 20)), 600, 24);
        System.out.println("    d1 = " + d1.describe());
        System.out.println("    d2 = " + d2.describe());
        System.out.println("    s1 = " + s1.describe());
        System.out.println("    d1.sha256() = " + d1.sha256().substring(0, 16) + "...");
        System.out.println("    d1.getTakenDate() = " + d1.getTakenDate() + ", сезон = " + d1.getSeason());

        // --- 5. Интерфейс Similarity ---
        System.out.println("\n[5] Интерфейс Similarity:");
        System.out.println("    d1.isDuplicateOf(d1copy) = " + d1.isDuplicateOf(d1copy));
        System.out.println("    d1.isDuplicateOf(d2)     = " + d1.isDuplicateOf(d2));
        System.out.printf("    d1.similarityTo(d2)      = %.2f (одинаковая камера/автор/время)%n", d1.similarityTo(d2));
        System.out.printf("    d1.similarityTo(s1)      = %.2f (разные подтипы)%n", d1.similarityTo(s1));

        // --- 6. Галерея ---
        Gallery gallery = new Gallery("Отпуск");
        gallery.addPhoto(d1);
        gallery.addPhoto(d1copy);
        gallery.addPhoto(d2);
        gallery.addPhoto(s1);
        System.out.println("\n[6] Gallery '" + gallery.getName() + "': size=" + gallery.size()
                + ", total=" + gallery.totalSizeBytes() + " байт");
        System.out.println("    findByFileName(\"IMG_0001.JPG\") присутствует: "
                + gallery.findByFileName("IMG_0001.JPG").isPresent());

        // --- 7/8. Сервис-оптимизатор ---
        PhotoGalleryOptimizer optimizer = new PhotoGalleryOptimizer();
        List<Photo> all = gallery.getPhotos();

        System.out.println("\n[7] findDuplicateGroups:");
        List<List<Integer>> groups = optimizer.findDuplicateGroups(all);
        for (List<Integer> g : groups) {
            System.out.println("    группа индексов " + g + " -> "
                    + g.stream().map(i -> all.get(i).getFileName()).toList());
        }

        System.out.println("\n[8] removeDuplicates:");
        Gallery cleaned = optimizer.removeDuplicates(gallery);
        System.out.println("    было: " + gallery.size() + ", стало: " + cleaned.size());
        for (Photo p : cleaned.getPhotos()) {
            System.out.println("    сохранилось: " + p.getFileName());
        }

        // --- 9. Стратегии распределения по каталогам ---
        System.out.println("\n[9] Стратегии CatalogStrategy:");
        try {
            Map<String, List<String>> bySeason = optimizer.buildCatalogMap(cleaned, new SeasonCatalogStrategy());
            System.out.println("    [" + seasonName() + "] " + bySeason);
            Map<String, List<String>> byMonth = optimizer.buildCatalogMap(cleaned, new MonthCatalogStrategy());
            System.out.println("    [" + monthName() + "] " + byMonth);
            Map<String, List<String>> byFormat = optimizer.buildCatalogMap(cleaned, new FormatCatalogStrategy());
            System.out.println("    [" + formatName() + "] " + byFormat);
        } catch (InvalidPhotoMetadataException e) {
            System.err.println("    Неожиданно: " + e.getMessage());
        }

        // --- 10. Собственное исключение ---
        System.out.println("\n[10] Собственное исключение InvalidPhotoMetadataException:");
        Gallery noDate = new Gallery("Без даты");
        noDate.addPhoto(new DigitalPhoto("no_exif.jpg", jpeg("??"),
                PhotoMetadata.unknownDate("Sony", null, 800, 600)));
        try {
            optimizer.buildCatalogMap(noDate, new SeasonCatalogStrategy());
            System.out.println("    Исключение НЕ возникло (ошибка!)");
        } catch (InvalidPhotoMetadataException e) {
            System.out.println("    Поймано: " + e.getMessage());
            System.out.println("    getPhotoFileName() = " + e.getPhotoFileName());
        }

        // --- 11. Отчёт ---
        System.out.println("\n[11] generateReport:");
        System.out.println(optimizer.generateReport(gallery));

        System.out.println("Демо завершено успешно.");
    }

    private static String seasonName() {
        return new SeasonCatalogStrategy().getName();
    }

    private static String monthName() {
        return new MonthCatalogStrategy().getName();
    }

    private static String formatName() {
        return new FormatCatalogStrategy().getName();
    }

    /* Вспомогательный класс для красивого вывода перечисления Season. */
    static final class SeasonHelper {
        static String name(int month) {
            return gallery.model.Season.ofMonth(month).getDisplayName();
        }

        static String months(int month) {
            return java.util.Arrays.toString(gallery.model.Season.ofMonth(month).getMonths());
        }
    }
}
