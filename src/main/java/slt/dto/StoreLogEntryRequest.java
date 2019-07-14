package slt.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreLogEntryRequest {

    private Long id;
    @ApiModelProperty(notes = "Defines what food has been eaten", required = true, example = "5")
    private Long foodId;
    @ApiModelProperty(notes = "PortionDto used. If null default food entry has been used", example = "3")
    private Long portionId;
    @ApiModelProperty(notes = "Multiplier of the portion or 100 grams", required = true, example = "1.7")
    private Double multiplier;
    @ApiModelProperty(notes = "Time of log", required = true)
    private Date day;
    @ApiModelProperty(notes = "Meal. BREAKFAST, LUNCH, DINNER, SNACK", required = true, example = "BREAKFAST")
    private String meal;
}
