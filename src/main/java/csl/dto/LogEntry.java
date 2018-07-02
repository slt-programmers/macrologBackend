package csl.dto;

import io.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;

import java.util.Date;

/**
 * Class voor het bewaren van de macros
 */
public class LogEntry {

    private FoodMacros food;
    private FoodAlias foodAlias;

    @ApiModelProperty(notes = "Multiplier of the measurement",required=true, example = "1.7")
    private Double multiplier;
    @ApiModelProperty(notes = "Time of log",required=true)
    private Date day;
    @ApiModelProperty(notes = "Meal. BREAKFAST, LUNCH, DINNER, SNACK",required=true, example="BREAKFAST")
    private String meal;



    public FoodMacros getFood() {
        return food;
    }

    public void setFood(FoodMacros food) {
        this.food = food;
    }

    public FoodAlias getFoodAlias() {
        return foodAlias;
    }

    public void setFoodAlias(FoodAlias foodAlias) {
        this.foodAlias = foodAlias;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public String getMeal() {
        return meal;
    }

    public void setMeal(String meal) {
        this.meal = meal;
    }
}
