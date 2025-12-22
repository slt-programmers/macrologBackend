package slt.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class LocalDateParser {

    private static final DateTimeFormatter standardFormat = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter shortFormat = DateTimeFormatter.ofPattern("yyyy-M-d");
    private static final DateTimeFormatter reversedFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter reversedShortFormat = DateTimeFormatter.ofPattern("d-M-yyyy");

    private LocalDateParser() {}

    public static LocalDate parse(String stringDate) {
        LocalDate date;
        try {
            date = LocalDate.parse(stringDate, standardFormat);
        } catch (DateTimeParseException ex) {
            try {
                date = LocalDate.parse(stringDate, shortFormat);
            } catch (DateTimeParseException ex2) {
                try {
                    date = LocalDate.parse(stringDate, reversedFormat);
                } catch (DateTimeParseException ex3) {
                    try {
                        date = LocalDate.parse(stringDate, reversedShortFormat);
                    } catch (DateTimeParseException ex4) {
                        log.error("Could not parse string [{}] to LocalDate.", stringDate);
                        throw ex4;
                    }
                }
            }
        }

        return date;
    }
}
