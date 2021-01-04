package slt.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DishRequest {

    private Long id;
    @ApiModelProperty(notes = "Name of dish", required = true)
    private String name;
    @ApiModelProperty(notes = "List of ingredients", required = true)
    private List<AddDishIngredientDto> ingredients = new ArrayList<>();

}
