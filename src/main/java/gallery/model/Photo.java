package gallery.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;


public abstract class Photo implements Similarity {

    protected final String fileName;
    protected final byte[] content;
    protected final PhotoFormat format;
    protected final PhotoMetadata metadata;


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

        this.content = Arrays.copyOf(content, content.length);

        this.metadata = metadata;

        PhotoFormat byMagic = PhotoFormat.fromMagicBytes(content);
        this.format = byMagic == PhotoFormat.UNKNOWN ? PhotoFormat.fromFileName(fileName) : byMagic;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return Arrays.copyOf(content, content.length);
    }

    public PhotoFormat getFormat() {
        return format;
    }

    public PhotoMetadata getMetadata() {
        return metadata;
    }

    public long getSizeBytes() {
        return content.length;
    }

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
            throw new IllegalStateException("Алгоритм SHA-256 недоступен", e);
        }
    }

    /*
    Краткое описание фото, для вывода в консоль
     */
    public String describe() {
        return String.format("%s [%s, %d байт, %dx%d]",
                fileName, format, content.length, metadata.widthPx(), metadata.heightPx());
    }

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
