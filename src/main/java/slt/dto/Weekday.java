package slt.dto;

import lombok.Getter;

@Getter
public enum Weekday {

    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    final String label;

    Weekday(final String label) {
        this.label = label;
    }

}
