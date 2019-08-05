package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class AddDishIngredientDto {

    private FoodDto food;
    private PortionDto portion;
    private Double multiplier;
}
