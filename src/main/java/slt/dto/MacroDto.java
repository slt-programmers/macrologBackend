package slt.dto;

import lombok.*;

@Getter
@Builder
public class MacroDto {

    private Double protein;
    private Double fat;
    private Double carbs;
    @Setter
    private Integer calories;

}
