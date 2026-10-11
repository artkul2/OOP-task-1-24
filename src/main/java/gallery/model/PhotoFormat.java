package gallery.model;

/**
 * Форматы файлов, которые умеет распознавать оптимизатор галереи.
 */
public enum PhotoFormat {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    GIF("gif", "image/gif"),
    BMP("bmp", "image/bmp"),
    WEBP("webp", "image/webp"),
    TIFF("tiff", "image/tiff"),
    UNKNOWN("dat", "application/octet-stream");

    private final String extension;
    private final String mimeType;

    PhotoFormat(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    /**
     * Определяет формат по магическим байтам заголовка файла.
     *
     * @param header первые байты файла (можно передать меньше — метод устойчив к короткому массиву)
     * @return найденный формат или {@link #UNKNOWN}
     */
    public static PhotoFormat fromMagicBytes(byte[] header) {
        if (header == null || header.length < 2) {
            return UNKNOWN;
        }
        // JPEG: FF D8 FF
        if (header.length >= 3 && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return JPEG;
        }
        // PNG: 89 50 4E 47
        if (header.length >= 4 && (header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
            return PNG;
        }
        // GIF: "GIF8"
        if (header.length >= 4 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F' && header[3] == '8') {
            return GIF;
        }
        // BMP: "BM"
        if (header[0] == 'B' && header[1] == 'M') {
            return BMP;
        }
        // TIFF: II*\0 или MM\0*
        if (header.length >= 4
                && ((header[0] == 'I' && header[1] == 'I' && header[2] == 42 && header[3] == 0)
                || (header[0] == 'M' && header[1] == 'M' && header[2] == 0 && header[3] == 42))) {
            return TIFF;
        }
        // WEBP: "RIFF" .... "WEBP"
        if (header.length >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return WEBP;
        }
        return UNKNOWN;
    }

    /**
     * Определяет формат по расширению имени файла (без учёта регистра).
     *
     * @param fileName имя файла
     * @return найденный формат или {@link #UNKNOWN}
     */
    public static PhotoFormat fromFileName(String fileName) {
        if (fileName == null) {
            return UNKNOWN;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return UNKNOWN;
        }
        String ext = fileName.substring(dot + 1).toLowerCase();
        for (PhotoFormat f : values()) {
            if (f.extension.equals(ext)) {
                return f;
            }
        }
        return UNKNOWN;
    }
}
