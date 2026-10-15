package gallery.exceptions;

/*
 Исключение возникает, когда с фотографией
 невозможно выполнить операцию из-за некорректных/отсутствующих метаданных
 */

public class InvalidPhotoMetadataException extends Exception {

    private final String photoFileName;

    public InvalidPhotoMetadataException(String photoFileName, String message) {
        super(String.format("Некорректные метаданные у '%s': %s", photoFileName, message));
        this.photoFileName = photoFileName;
    }

    public String getPhotoFileName() {
        return photoFileName;
    } // Выводит имя файла, вызвавшего ошибку
}
