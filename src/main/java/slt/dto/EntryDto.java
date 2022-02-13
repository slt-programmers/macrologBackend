package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(notes = "PortionDto used.")
    private PortionDto portion;

    private Macro macrosCalculated = new Macro(0.0, 0.0, 0.0);

    @ApiModelProperty(notes = "Multiplier of the measurement", required = true, example = "1.7")
    private Double multiplier;

    @ApiModelProperty(notes = "Day of log", required = true)
    private Date day;

    @ApiModelProperty(notes = "Meal. BREAKFAST, LUNCH, DINNER, SNACK", required = true, example = "BREAKFAST")
    private String meal;

}
