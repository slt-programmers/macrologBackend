package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntryDto {

    private Long id;

    private FoodDto food;

    private PortionDto portion;

    private MacroDto macrosCalculated = new MacroDto(0.0, 0.0, 0.0, 0);

    private Double multiplier;

    private Date day;

    private Meal meal;

}
