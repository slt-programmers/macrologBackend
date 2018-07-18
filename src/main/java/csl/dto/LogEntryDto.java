package csl.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * Class voor het bewaren van de macros
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogEntryDto {

    private Long id;
    private FoodDto foodDto;
    @ApiModelProperty(notes = "PortionDto used.")
    private PortionDto portionDto;
    private Macro macrosCalculated;
    @ApiModelProperty(notes = "Multiplier of the measurement",required=true, example = "1.7")
    private Double multiplier;
    @ApiModelProperty(notes = "Day of log",required=true)
    private Date day;
    @ApiModelProperty(notes = "Meal. BREAKFAST, LUNCH, DINNER, SNACK",required=true, example="BREAKFAST")
    private String meal;


    public FoodDto getFoodDto() {
        return foodDto;
    }

    public void setFoodDto(FoodDto foodDto) {
        this.foodDto = foodDto;
    }

    public PortionDto getPortionDto() {
        return portionDto;
    }

    public void setPortionDto(PortionDto portionDto) {
        this.portionDto = portionDto;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
