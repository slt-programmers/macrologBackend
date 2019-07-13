package csl.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDateParserTest {

    @Test
    void parse() {

        assertThat(LocalDateParser.parse("2001-01-01")).isEqualTo(LocalDate.parse("2001-01-01"));
        assertThat(LocalDateParser.parse("2001-1-1")).isEqualTo(LocalDate.parse("2001-01-01"));
        assertThat(LocalDateParser.parse("1-01-2001")).isEqualTo(LocalDate.parse("2001-01-01"));
        assertThat(LocalDateParser.parse("1-1-2001")).isEqualTo(LocalDate.parse("2001-01-01"));

        assertThat(LocalDateParser.parse("2001-01-15")).isEqualTo(LocalDate.parse("2001-01-15"));
        assertThat(LocalDateParser.parse("2001-1-15")).isEqualTo(LocalDate.parse("2001-01-15"));
        assertThat(LocalDateParser.parse("15-01-2001")).isEqualTo(LocalDate.parse("2001-01-15"));
        assertThat(LocalDateParser.parse("15-1-2001")).isEqualTo(LocalDate.parse("2001-01-15"));

    }
}