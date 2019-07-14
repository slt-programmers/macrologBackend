package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealDto {

    private Long id;
    @ApiModelProperty(notes = "Name of meal", required = true)
    private String name;
    @ApiModelProperty(notes = "List of ingredients", required = true)
    private List<IngredientDto> ingredients;

}
