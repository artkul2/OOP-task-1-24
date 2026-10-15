package gallery.model;

/*
 Времена года - используются для распределения фотографий по каталогам.
 */
public enum Season {
    WINTER("Зима", new int[]{12, 1, 2}),
    SPRING("Весна", new int[]{3, 4, 5}),
    SUMMER("Лето", new int[]{6, 7, 8}),
    AUTUMN("Осень", new int[]{9, 10, 11});

    private final String displayName;
    private final int[] months;

    Season(String displayName, int[] months) {
        this.displayName = displayName;
        this.months = months;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int[] getMonths() {
        return months.clone();
    }

    /*
     Возвращает время года по номеру месяца (1-12).
     */
    public static Season ofMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Месяц должен быть в диапазоне 1..12, получено: " + month);
        }
        for (Season s : values()) {
            for (int m : s.months) {
                if (m == month) {
                    return s;
                }
            }
        }
        throw new IllegalStateException("Сезон не найден для месяца " + month);
    }
}
