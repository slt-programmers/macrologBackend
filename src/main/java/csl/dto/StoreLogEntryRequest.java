package csl.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * Class voor het bewaren van de macros
 */
public class StoreLogEntryRequest {

    private Long id;
    @ApiModelProperty(notes = "Defines what food has been eaten",required=true,example = "5")
    private Long foodId;
    @ApiModelProperty(notes = "Portion used. If null default food entry has been used",required=false,example = "3")
    private Long portionId;
    @ApiModelProperty(notes = "Multiplier of the measurement",required=true, example = "1.7")
    private Double multiplier;
    @ApiModelProperty(notes = "Time of log",required=true)
    private Date day;
    @ApiModelProperty(notes = "Meal. BREAKFAST, LUNCH, DINNER, SNACK",required=true, example="BREAKFAST")
    private String meal;


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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
