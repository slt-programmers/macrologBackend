package slt.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import slt.exceptions.InvalidDateException;

import java.time.LocalDate;

@Slf4j
class LocalDateParserTest {

    @Test
    void parse() {
        Assertions.assertEquals(LocalDate.parse("2001-01-01"), LocalDateParser.parse("2001-01-01"));
        Assertions.assertEquals(LocalDate.parse("2001-01-01"), LocalDateParser.parse("2001-1-1"));
        Assertions.assertEquals(LocalDate.parse("2001-01-01"), LocalDateParser.parse("1-01-2001"));
        Assertions.assertEquals(LocalDate.parse("2001-01-01"), LocalDateParser.parse("1-1-2001"));

        Assertions.assertThrows(InvalidDateException.class, () -> LocalDateParser.parse("32-02-2001")
        );

    }
}