package gallery.model;

/**
 * Интерфейс для сущностей, которые можно сравнить на «похожесть»
 * (например, две фотографии могут быть дубликатами).
 */
public interface Similarity {
    /**
     * Оценивает степень похожести двух объектов в диапазоне [0..1],
     * где 1 — абсолютное совпадение.
     *
     * @param other объект для сравнения
     * @return оценка похожести
     */
    double similarityTo(Similarity other);

    /**
     * Является ли объект полным дубликатом другого.
     *
     * @param other объект для сравнения
     * @return {@code true}, если объекты неотличимы
     */
    boolean isDuplicateOf(Similarity other);
}
