package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class EntryDto {

    private Long id;
    private FoodDto food;
    private PortionDto portion;
    private MacroDto macrosCalculated;
    private Double multiplier;
    private Date day;
    private Meal meal;

}
