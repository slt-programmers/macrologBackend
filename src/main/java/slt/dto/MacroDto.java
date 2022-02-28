package slt.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MacroDto {

    @ApiModelProperty(notes = "Number of protein", required = true)
    private Double protein;

    @ApiModelProperty(notes = "Number of fat", required = true)
    private Double fat;

    @ApiModelProperty(notes = "Number of carbs", required = true)
    private Double carbs;

    @ApiModelProperty(notes = "Number of calories")
    private Integer calories;

    public Double getCalories() {
        return fat * 9 + carbs * 4 + protein * 4;
    }

    public MacroDto createCopy() {
        MacroDto clone = new MacroDto();
        clone.setFat(fat);
        clone.setCarbs(carbs);
        clone.setProtein(protein);
        return clone;
    }

}
