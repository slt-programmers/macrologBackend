package slt.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class MealtimeDto {

    private Long id;

    private Meal meal;

    private Weekday weekday;

    @Builder.Default
    private List<IngredientDto> ingredients = new ArrayList<>();
}
