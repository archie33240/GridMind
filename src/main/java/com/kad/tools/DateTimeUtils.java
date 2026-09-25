package com.kad.tools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtils {

    // Formatteurs pour les dates/heures
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Convertit une date et une heure en ZonedDateTime.
     * @param date La date (LocalDate).
     * @param time L'heure au format "HH:mm".
     * @return ZonedDateTime combiné.
     * @throws DateTimeParseException Si le format de l'heure est invalide.
     */
    public static ZonedDateTime combineDateAndTime(LocalDate date, String time) throws DateTimeParseException {
        LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);
        return ZonedDateTime.of(date, localTime, ZoneId.systemDefault());
    }

    /**
     * Valide le format d'une heure (HH:mm).
     * @param time L'heure à valider.
     * @return true si le format est valide, false sinon.
     */
    public static boolean isValidTimeFormat(String time) {
        try {
            LocalTime.parse(time, TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Convertit un ZonedDateTime en chaîne formatée (dd/MM/yyyy HH:mm).
     */
    public static String formatDateTime(ZonedDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Convertit une chaîne "dd/MM/yyyy HH:mm" en ZonedDateTime.
     */
    public static ZonedDateTime parseDateTime(String dateTimeStr) throws DateTimeParseException {
        return ZonedDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
    }

    /**
     * Convertit un java.sql.Timestamp en ZonedDateTime.
     */
    public static ZonedDateTime toZonedDateTime(java.sql.Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime().atZone(ZoneId.systemDefault()) : null;
    }

    /**
     * Convertit un ZonedDateTime en java.sql.Timestamp.
     */
    public static java.sql.Timestamp toTimestamp(ZonedDateTime zonedDateTime) {
        return zonedDateTime != null ? java.sql.Timestamp.from(zonedDateTime.toInstant()) : null;
    }
}
