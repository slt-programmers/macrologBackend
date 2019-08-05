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

    private Long foodId;
    private Long portionId;
    private Double multiplier;
}
