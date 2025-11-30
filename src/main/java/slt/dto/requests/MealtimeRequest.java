package slt.dto.requests;

import lombok.Builder;
import lombok.Getter;
import slt.dto.Meal;
import slt.dto.Weekday;

import java.util.List;

@Builder
@Getter
public class MealtimeRequest {

    private Long id;

    private Meal meal;

    private Weekday weekday;

    private List<IngredientRequest> ingredients;
}
