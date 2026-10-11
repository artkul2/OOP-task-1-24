package gallery.model;

/**
 * Отсканированная плёночная фотография. Не имеет EXIF-камеры,
 * но имеет разрешение сканера (DPI) и глубину цвета.
 * Демонстрирует наследование: переопределяет {@code describe()} и {@code similarityTo()}.
 */
public class ScannedPhoto extends Photo {

    private final int dpi;
    private final int colorDepthBits;

    public ScannedPhoto(String fileName, byte[] content, PhotoMetadata metadata, int dpi, int colorDepthBits) {
        super(fileName, content, metadata);
        if (dpi <= 0) {
            throw new IllegalArgumentException("DPI сканера должен быть положительным: " + dpi);
        }
        if (colorDepthBits < 1 || colorDepthBits > 48) {
            throw new IllegalArgumentException("Глубина цвета вне диапазона 1..48 бит: " + colorDepthBits);
        }
        this.dpi = dpi;
        this.colorDepthBits = colorDepthBits;
    }

    public int getDpi() {
        return dpi;
    }

    public int getColorDepthBits() {
        return colorDepthBits;
    }

    /** Физический размер отпечатка в сантиметрах (ширина), исходя из пикселей и DPI. */
    public double printWidthCm() {
        return metadata.widthPx() / (double) dpi * 2.54;
    }

    /**
     * Похожесть сканов: побайтовое совпадение даёт 1.0; иначе совпадение разрешения и глубины цвета.
     */
    @Override
    public double similarityTo(Similarity other) {
        if (!(other instanceof ScannedPhoto p)) {
            return 0.0;
        }
        if (isDuplicateOf(other)) {
            return 1.0;
        }
        double score = 0.0;
        if (metadata.widthPx() == p.metadata.widthPx() && metadata.heightPx() == p.metadata.heightPx()) {
            score += 0.5;
        }
        if (dpi == p.dpi && colorDepthBits == p.colorDepthBits) {
            score += 0.5;
        }
        return score;
    }

    @Override
    public String describe() {
        return super.describe() + String.format(" скан: %d dpi, %d бит, ~%.1f см", dpi, colorDepthBits, printWidthCm());
    }
}
