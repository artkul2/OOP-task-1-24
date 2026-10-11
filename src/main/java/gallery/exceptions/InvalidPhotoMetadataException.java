package gallery.exceptions;

/**
 * Собственное проверяемое исключение проекта: возникает, когда с фотографией
 * невозможно выполнить операцию из-за некорректных/отсутствующих метаданных
 * (например, распределение по сезонам без даты съёмки).
 */
public class InvalidPhotoMetadataException extends Exception {

    private final String photoFileName;

    public InvalidPhotoMetadataException(String photoFileName, String message) {
        super(String.format("Некорректные метаданные у '%s': %s", photoFileName, message));
        this.photoFileName = photoFileName;
    }

    public InvalidPhotoMetadataException(String photoFileName, String message, Throwable cause) {
        super(String.format("Некорректные метаданные у '%s': %s", photoFileName, message), cause);
        this.photoFileName = photoFileName;
    }

    /** Имя файла фотографии, из-за которой возникла проблема. */
    public String getPhotoFileName() {
        return photoFileName;
    }
}
