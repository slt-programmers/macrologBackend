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
public class Macro {

    @ApiModelProperty(notes = "Number of protein", required = true)
    private Double protein;
    @ApiModelProperty(notes = "Number of fat", required = true)
    private Double fat;
    @ApiModelProperty(notes = "Number of carbs", required = true)
    private Double carbs;

    public Double getCalories() {
        return fat * 9 + carbs * 4 + protein * 4;
    }

    public void multiply(Double multiplier) {
        this.protein = this.protein * multiplier;
        this.fat = this.fat * multiplier;
        this.carbs = this.carbs * multiplier;
    }

    public Macro createCopy() {
        Macro clone = new Macro();
        clone.setFat(fat);
        clone.setCarbs(carbs);
        clone.setProtein(protein);
        return clone;
    }

    public void combine(Macro other) {
        this.fat = this.fat + other.fat;
        this.carbs = this.carbs + other.carbs;
        this.protein = this.protein + other.protein;

    }


}
