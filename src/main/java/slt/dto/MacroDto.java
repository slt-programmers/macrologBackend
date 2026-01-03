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

}
