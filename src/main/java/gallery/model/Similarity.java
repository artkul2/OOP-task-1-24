package gallery.model;

/*
 Интерфейс для сущностей, которые можно сравнить на «похожесть»
 */
public interface Similarity {
    /*
     Оценивает степень похожести двух объектов в диапазоне [0;1],
     где 1 — абсолютное совпадение
     */
    double similarityTo(Similarity other);

    /*
     Является ли объект полным дубликатом другого
     */
    boolean isDuplicateOf(Similarity other);
}
