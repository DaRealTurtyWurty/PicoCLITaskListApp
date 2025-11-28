package dev.turtywurty.tasklistcli.util;

import java.time.LocalDate;

public class TimeUtils {
    public static LocalDate parseOrNull(String dateStr) {
        if(dateStr == null || dateStr.isEmpty())
            return null;

        try {
            return LocalDate.parse(dateStr);
        } catch (Exception ignored) {
            return null;
        }
    }
}
