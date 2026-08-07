package ge.tsepesh.motorental.util;

import java.util.Locale;

public class DurationFormatUtil {
    private DurationFormatUtil() {}
    /**
     * Форматирует период в минутах для отображения пользователю.
     * <= 60 мин: "30 мин."
     * > 60 мин:  "1 ч. 30 мин." (если минут = 0, то "2 ч.")
     */
    public static String formatMinutes(int totalMinutes, Locale locale) {
        boolean en = "en".equalsIgnoreCase(locale.getLanguage());
        if (totalMinutes <= 60) {
            return en
                    ? totalMinutes + " min"
                    : totalMinutes + " мин";
        }
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        if (minutes == 0) {
            return en ? hours + " h" : hours + " ч";
        }
        return en
                ? hours + " h " + minutes + " min"
                : hours + " ч " + minutes + " мин";
    }
}
