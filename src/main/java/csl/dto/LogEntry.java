package csl.dto;

import io.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;

import java.util.Date;

/**
 * Class voor het bewaren van de macros
 */
public class LogEntry {

    private String foodName;
    private Long foodId;
    private FoodAlias foodAlias;
    private Macro macrosCalculated;
    @ApiModelProperty(notes = "Multiplier of the measurement",required=true, example = "1.7")
    private Double multiplier;
    @ApiModelProperty(notes = "Day of log",required=true)
    private Date day;
    @ApiModelProperty(notes = "Meal. BREAKFAST, LUNCH, DINNER, SNACK",required=true, example="BREAKFAST")
    private String meal;


    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
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

    public Macro getMacrosCalculated() {
        return macrosCalculated;
    }

    public void setMacrosCalculated(Macro macrosCalculated) {
        this.macrosCalculated = macrosCalculated;
    }
}
