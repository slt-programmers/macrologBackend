package slt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MacroDto {

    private Double protein;

    private Double fat;

    private Double carbs;

    private Integer calories;

    public Double getCalories() {
        return fat * 9 + carbs * 4 + protein * 4;
    }

    public MacroDto createCopy() {
        final var clone = new MacroDto();
        clone.setFat(fat);
        clone.setCarbs(carbs);
        clone.setProtein(protein);
        return clone;
    }

}
