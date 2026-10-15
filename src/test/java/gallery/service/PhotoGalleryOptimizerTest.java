package gallery.service;

import gallery.exceptions.InvalidPhotoMetadataException;
import gallery.model.DigitalPhoto;
import gallery.model.Gallery;
import gallery.model.Photo;
import gallery.model.PhotoFormat;
import gallery.model.PhotoMetadata;
import gallery.model.ScannedPhoto;
import gallery.model.Season;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/*
 Юнит-тесты оптимизатора галереи
 */
class PhotoGalleryOptimizerTest {

    private static final PhotoGalleryOptimizer OPTIMIZER = new PhotoGalleryOptimizer();

    private static byte[] jpeg(String payload) {
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

    private static PhotoMetadata md(int w, int h, long t) {
        return new PhotoMetadata("Canon", "Анна", w, h, t);
    }

    // ==================== Модель ====================

    @Test
    void photoRejectsBlankFileName() {
        assertThrows(IllegalArgumentException.class,
                () -> new DigitalPhoto("   ", jpeg("x"), md(1, 1, 1)));
    }

    @Test
    void photoRejectsNullContentAndMetadata() {
        assertThrows(IllegalArgumentException.class,
                () -> new DigitalPhoto("a.jpg", null, md(1, 1, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> new DigitalPhoto("a.jpg", jpeg("x"), null));
    }

    @Test
    void recordRejectsNegativeDimensions() {
        assertThrows(IllegalArgumentException.class,
                () -> new PhotoMetadata("c", "p", -5, 10, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new PhotoMetadata("c", "p", 10, 10, -1));
    }

    @Test
    void formatDetectedByMagicBytesNotExtension() {
        // файл называется .png, но внутри JPEG — формат должен быть JPEG
        DigitalPhoto p = new DigitalPhoto("fake.png", jpeg("data"), md(1, 1, 1));
        assertEquals(PhotoFormat.JPEG, p.getFormat());
    }

    @Test
    void formatFallsBackToExtension() {
        // содержимое без магических байт, расширение .gif
        DigitalPhoto p = new DigitalPhoto("anim.gif", "random".getBytes(StandardCharsets.UTF_8), md(1, 1, 1));
        assertEquals(PhotoFormat.GIF, p.getFormat());
    }

    @Test
    void seasonOfMonthBoundaries() {
        assertEquals(Season.WINTER, Season.ofMonth(12));
        assertEquals(Season.WINTER, Season.ofMonth(1));
        assertEquals(Season.SPRING, Season.ofMonth(3));
        assertEquals(Season.AUTUMN, Season.ofMonth(11));
        assertThrows(IllegalArgumentException.class, () -> Season.ofMonth(0));
        assertThrows(IllegalArgumentException.class, () -> Season.ofMonth(13));
    }

    @Test
    void duplicateDetectionByContent() {
        DigitalPhoto a = new DigitalPhoto("a.jpg", jpeg("same"), md(10, 10, 100));
        DigitalPhoto b = new DigitalPhoto("b.jpg", jpeg("same"), md(20, 20, 200));
        DigitalPhoto c = new DigitalPhoto("c.jpg", jpeg("other"), md(10, 10, 100));
        assertTrue(a.isDuplicateOf(b), "одинаковое содержимое при разных именах/метаданных — дубликат");
        assertFalse(a.isDuplicateOf(c));
    }

    @Test
    void similarityAcrossDifferentSubtypesIsZero() {
        DigitalPhoto d = new DigitalPhoto("d.jpg", jpeg("x"), md(10, 10, 100));
        ScannedPhoto s = new ScannedPhoto("s.png", jpeg("x"), md(10, 10, 100), 300, 24);
        assertEquals(0.0, d.similarityTo(s));
        assertEquals(0.0, s.similarityTo(d));
    }

    @Test
    void scannedPhotoValidatesDpiAndDepth() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScannedPhoto("s.png", jpeg("x"), md(1, 1, 1), 0, 24));
        assertThrows(IllegalArgumentException.class,
                () -> new ScannedPhoto("s.png", jpeg("x"), md(1, 1, 1), 300, 49));
    }

    @Test
    void galleryRejectsNullNameAndNullPhoto() {
        Gallery g = new Gallery("test");
        assertThrows(IllegalArgumentException.class, () -> g.addPhoto(null));
    }

    @Test
    void galleryRejectsSameObjectTwice() {
        Gallery g = new Gallery("g");
        DigitalPhoto p = new DigitalPhoto("p.jpg", jpeg("x"), md(1, 1, 1));
        assertTrue(g.addPhoto(p));
        assertFalse(g.addPhoto(p), "повторное добавление того же объекта должно возвращать false");
        assertEquals(1, g.size());
    }

    // ==================== Оптимизатор ====================

    @Test
    void findDuplicateGroupsOnEmptyAndSingleLists() {
        assertTrue(OPTIMIZER.findDuplicateGroups(Collections.emptyList()).isEmpty());
        List<Photo> single = List.of(new DigitalPhoto("a.jpg", jpeg("x"), md(1, 1, 1)));
        assertTrue(OPTIMIZER.findDuplicateGroups(single).isEmpty(), "один файл — не группа");
        assertThrows(IllegalArgumentException.class, () -> OPTIMIZER.findDuplicateGroups(null));
    }

    @Test
    void findDuplicateGroupsDetectsTriples() {
        Photo a = new DigitalPhoto("a.jpg", jpeg("dup"), md(1, 1, 1));
        Photo b = new DigitalPhoto("b.jpg", jpeg("dup"), md(1, 1, 1));
        Photo c = new DigitalPhoto("c.jpg", jpeg("dup"), md(1, 1, 1));
        Photo d = new DigitalPhoto("d.jpg", jpeg("uniq"), md(1, 1, 1));
        List<List<Integer>> groups = OPTIMIZER.findDuplicateGroups(List.of(a, b, c, d));
        assertEquals(1, groups.size());
        assertEquals(List.of(0, 1, 2), groups.get(0));
    }

    @Test
    void removeDuplicatesKeepsFirstOccurrence() {
        Gallery g = new Gallery("t");
        Photo a = new DigitalPhoto("a.jpg", jpeg("dup"), md(1, 1, 1));
        Photo b = new DigitalPhoto("b.jpg", jpeg("dup"), md(1, 1, 1));
        Photo c = new DigitalPhoto("c.jpg", jpeg("keep"), md(1, 1, 1));
        g.addPhoto(a);
        g.addPhoto(b);
        g.addPhoto(c);
        Gallery cleaned = OPTIMIZER.removeDuplicates(g);
        assertEquals(2, cleaned.size());
        assertEquals(List.of("a.jpg", "c.jpg"),
                cleaned.getPhotos().stream().map(Photo::getFileName).toList());
        assertEquals(3, g.size(), "исходная галерея не должна изменяться");
        assertThrows(IllegalArgumentException.class, () -> OPTIMIZER.removeDuplicates(null));
    }

    @Test
    void removeDuplicatesOnEmptyGalleryReturnsEmpty() {
        Gallery cleaned = OPTIMIZER.removeDuplicates(new Gallery("empty"));
        assertTrue(cleaned.isEmpty());
    }

    @Test
    void buildCatalogMapBySeason() throws InvalidPhotoMetadataException {
        Gallery g = new Gallery("t");
        g.addPhoto(new DigitalPhoto("summer.jpg", jpeg("1"), md(1, 1, epoch(2023, 7, 1))));
        g.addPhoto(new DigitalPhoto("winter.jpg", jpeg("2"), md(1, 1, epoch(2023, 1, 15))));
        Map<String, List<String>> map = OPTIMIZER.buildCatalogMap(g, new SeasonCatalogStrategy());
        assertEquals(List.of("summer.jpg"), map.get("2023/Лето"));
        assertEquals(List.of("winter.jpg"), map.get("2023/Зима"));
    }

    @Test
    void buildCatalogMapByMonthAndFormat() throws InvalidPhotoMetadataException {
        Gallery g = new Gallery("t");
        g.addPhoto(new DigitalPhoto("photo.jpg", jpeg("1"), md(1, 1, epoch(2022, 12, 1))));
        assertEquals(List.of("photo.jpg"),
                OPTIMIZER.buildCatalogMap(g, new MonthCatalogStrategy()).get("2022/12"));
        assertEquals(List.of("photo.jpg"),
                OPTIMIZER.buildCatalogMap(g, new FormatCatalogStrategy()).get("jpeg"));
    }

    @Test
    void customExceptionThrownWhenDateMissingForSeasonStrategy() {
        Gallery g = new Gallery("t");
        g.addPhoto(new DigitalPhoto("no_date.jpg", jpeg("x"),
                PhotoMetadata.unknownDate("Sony", null, 800, 600)));
        InvalidPhotoMetadataException ex = assertThrows(InvalidPhotoMetadataException.class,
                () -> OPTIMIZER.buildCatalogMap(g, new SeasonCatalogStrategy()));
        assertEquals("no_date.jpg", ex.getPhotoFileName());
        assertTrue(ex.getMessage().contains("отсутствует дата съёмки"));
    }

    @Test
    void formatStrategyWorksWithoutDates() {
        Gallery g = new Gallery("t");
        g.addPhoto(new DigitalPhoto("x.jpg", jpeg("y"), PhotoMetadata.unknownDate("", "", 1, 1)));
        assertDoesNotThrow(() -> OPTIMIZER.buildCatalogMap(g, new FormatCatalogStrategy()));
    }

    @Test
    void buildCatalogMapRejectsNullArguments() {
        Gallery g = new Gallery("t");
        assertThrows(IllegalArgumentException.class,
                () -> OPTIMIZER.buildCatalogMap(null, new FormatCatalogStrategy()));
        assertThrows(IllegalArgumentException.class,
                () -> OPTIMIZER.buildCatalogMap(g, null));
    }

    @Test
    void generateReportContainsKeyInfo() {
        Gallery g = new Gallery("Отчёт");
        g.addPhoto(new DigitalPhoto("a.jpg", jpeg("dup"), md(10, 20, epoch(2023, 5, 1))));
        g.addPhoto(new DigitalPhoto("b.jpg", jpeg("dup"), md(10, 20, epoch(2023, 5, 1))));
        String report = OPTIMIZER.generateReport(g);
        assertTrue(report.contains("Отчёт"));
        assertTrue(report.contains("Фотографий: 2"));
        assertTrue(report.contains("Групп дубликатов: 1"));
        assertTrue(report.contains("a.jpg"));
    }

    @Test
    void generateReportOnEmptyGallery() {
        String report = OPTIMIZER.generateReport(new Gallery("Пустая"));
        assertTrue(report.contains("(галерея пуста)"));
        assertThrows(IllegalArgumentException.class, () -> OPTIMIZER.generateReport(null));
    }

    @Test
    void sha256IsStableAndContentSensitive() {
        DigitalPhoto a = new DigitalPhoto("a.jpg", jpeg("same"), md(1, 1, 1));
        DigitalPhoto b = new DigitalPhoto("b.jpg", jpeg("same"), md(2, 2, 2));
        DigitalPhoto c = new DigitalPhoto("c.jpg", jpeg("diff"), md(1, 1, 1));
        assertEquals(a.sha256(), b.sha256());
        assertNotEquals(a.sha256(), c.sha256());
        assertEquals(64, a.sha256().length());
    }

    @Test
    void digitalPhotoSimilarityBurstWindow() {
        // одинаковые камера/автор + съёмка в пределах 5 секунд => 0.3+0.3+0.4 = 1.0
        DigitalPhoto a = new DigitalPhoto("a.jpg", jpeg("one"), md(1, 1, 1000000));
        DigitalPhoto b = new DigitalPhoto("b.jpg", jpeg("two"), md(1, 1, 1000003));
        assertEquals(1.0, a.similarityTo(b), 1e-9);
        // съёмка через сутки => только камера+автор = 0.6
        DigitalPhoto c = new DigitalPhoto("c.jpg", jpeg("three"), md(1, 1, 1000000 + 86400));
        assertEquals(0.6, a.similarityTo(c), 1e-9);
    }

    @Test
    void scannedPhotoPrintWidth() {
        ScannedPhoto s = new ScannedPhoto("s.png", jpeg("x"), md(600, 400, 1), 600, 24);
        assertEquals(2.54, s.printWidthCm(), 1e-9);
        assertEquals(600, s.getDpi());
        assertEquals(24, s.getColorDepthBits());
    }
}
