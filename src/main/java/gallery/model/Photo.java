package gallery.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Абстрактный базовый класс фотографии в галерее.
 * Хранит имя файла, бинарное содержимое, формат и метаданные.
 * Реализует интерфейс {@link Similarity} на уровне базовых общих операций.
 */
public abstract class Photo implements Similarity {

    protected final String fileName;
    protected final byte[] content;
    protected final PhotoFormat format;
    protected final PhotoMetadata metadata;

    /**
     * @param fileName имя файла (не пустое)
     * @param content  бинарное содержимое (не {@code null})
     * @param metadata метаданные (не {@code null})
     * @throws IllegalArgumentException если имя пустое или содержимое/metadata == null
     */
    protected Photo(String fileName, byte[] content, PhotoMetadata metadata) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }
        if (content == null) {
            throw new IllegalArgumentException("Содержимое файла не может быть null: " + fileName);
        }
        if (metadata == null) {
            throw new IllegalArgumentException("Метаданные не могут быть null: " + fileName);
        }
        this.fileName = fileName;
        // защищаемся от внешнего изменения массива
        this.content = Arrays.copyOf(content, content.length);
        this.metadata = metadata;
        // формат определяем по магическим байтам, при неудаче — по расширению
        PhotoFormat byMagic = PhotoFormat.fromMagicBytes(content);
        this.format = byMagic == PhotoFormat.UNKNOWN ? PhotoFormat.fromFileName(fileName) : byMagic;
    }

    public String getFileName() {
        return fileName;
    }

    /** Возвращает копию содержимого, чтобы класс оставался защищённым от мутаций. */
    public byte[] getContent() {
        return Arrays.copyOf(content, content.length);
    }

    public PhotoFormat getFormat() {
        return format;
    }

    public PhotoMetadata getMetadata() {
        return metadata;
    }

    /** Размер файла в байтах. */
    public long getSizeBytes() {
        return content.length;
    }

    /** SHA-256 хеш содержимого в hex-представлении — «отпечаток» файла. */
    public String sha256() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(content);
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 гарантированно есть в любой JRE
            throw new IllegalStateException("Алгоритм SHA-256 недоступен", e);
        }
    }

    /**
     * Краткое человекочитаемое описание фото для вывода в консоль.
     * По умолчанию — имя, формат, размер и разрешение; наследники расширяют описание.
     */
    public String describe() {
        return String.format("%s [%s, %d байт, %dx%d]",
                fileName, format, content.length, metadata.widthPx(), metadata.heightPx());
    }

    /** Дубликаты определяем по совпадению побайтового содержимого. */
    @Override
    public boolean isDuplicateOf(Similarity other) {
        return other instanceof Photo p && Arrays.equals(this.content, p.content);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Photo other)) return false;
        return fileName.equals(other.fileName) && Arrays.equals(content, other.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, Arrays.hashCode(content));
    }

    @Override
    public String toString() {
        return describe();
    }
}
