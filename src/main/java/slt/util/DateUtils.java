package slt.util;

import lombok.experimental.UtilityClass;
import slt.exceptions.InvalidDateException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@UtilityClass
public class DateUtils {

    public static void validateDateFormat(final String date) {
        if (date != null) {
            try {
                final var dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setLenient(false);
                dateFormat.parse(date);
            } catch (ParseException pe) {
                throw new InvalidDateException("Date format is not valid.");
            }
        }
    }
}
