package slt.dto;

import lombok.*;

import java.util.Date;

@Getter
@Builder
public class DayMacroDto {

    private Date day;
    private MacroDto macros;

}
