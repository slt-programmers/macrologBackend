package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class DishDto {

    private Long id;

    private String name;

    @Builder.Default
    private List<IngredientDto> ingredients = new ArrayList<>();

    @Builder.Default
    private MacroDto macrosCalculated = new MacroDto(0.0, 0.0, 0.0, 0);

}
