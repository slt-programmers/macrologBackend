package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IngredientDto {

    private Long id;
    private Long foodId;
    private Long portionId;
    private Double multiplier;
    private FoodDto food;
    private PortionDto portion;
}
