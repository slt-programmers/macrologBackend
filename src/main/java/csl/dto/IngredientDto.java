package csl.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

/**
 * Class voor het bewaren van de macros
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IngredientDto {

    private Long id;
    private Long foodId;
    private Long portionId;
    private Double multiplier;
    private FoodDto food;
    private PortionDto portion;

    public IngredientDto() {

    }

    public IngredientDto(Long id, Long foodId, Long portionId, Double multiplier, FoodDto food, PortionDto portion) {
        this.id = id;
        this.foodId = foodId;
        this.portionId = portionId;
        this.multiplier = multiplier;
        this.food = food;
        this.portion = portion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Long getPortionId() {
        return portionId;
    }

    public void setPortionId(Long portionId) {
        this.portionId = portionId;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }

    public FoodDto getFood() {
        return food;
    }

    public void setFood(FoodDto food) {
        this.food = food;
    }

    public PortionDto getPortion() {
        return portion;
    }

    public void setPortion(PortionDto portion) {
        this.portion = portion;
    }
}
