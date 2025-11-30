package slt.dto.requests;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class IngredientRequest {

    private Long id;
    private Double multiplier;
    private Long foodId;
    private Long portionId;

}
