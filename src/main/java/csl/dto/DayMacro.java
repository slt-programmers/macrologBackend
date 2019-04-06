package csl.dto;

import java.util.Date;

/**
 * Class voor het bewaren van de macros
 */
public class DayMacro {

    private Date day;
    private Macro macro;

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public Macro getMacro() {
        return macro;
    }

    public void setMacro(Macro macro) {
        this.macro = macro;
    }
}
