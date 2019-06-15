package csl.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MealDto {

    private Long id;
    @ApiModelProperty(notes = "Name of meal", required = true)
    private String name;
    @ApiModelProperty(notes = "List of ingredients", required = true)
    private List<IngredientDto> ingredients;

    public MealDto() {

    }

    public MealDto(Long id, String name, List<IngredientDto> ingredients) {
        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
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

    public List<IngredientDto> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientDto> ingredients) {
        this.ingredients = ingredients;
    }
}
