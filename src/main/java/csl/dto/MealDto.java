package csl.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Class voor het bewaren van de macros
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MealDto {

    private Long id;
    @ApiModelProperty(notes = "Name of meal", required = true)
    private String name;
    @ApiModelProperty(notes = "List of ingredients", required = true)
    private List<IngredientDto> ingredientDtos;

    public MealDto() {

    }

    public MealDto(Long id, String name, List<IngredientDto> ingredientDtos) {
        this.id = id;
        this.name = name;
        this.ingredientDtos = ingredientDtos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<IngredientDto> getIngredientDtos() {
        return ingredientDtos;
    }

    public void setIngredientDtos(List<IngredientDto> ingredientDtos) {
        this.ingredientDtos = ingredientDtos;
    }
}
