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
    private FoodDto food;
    @ApiModelProperty(notes = "PortionDto used.")
    private PortionDto portion;
    private Macro macrosCalculated;
    @ApiModelProperty(notes = "Multiplier of the measurement",required=true, example = "1.7")
    private Double multiplier;
    @ApiModelProperty(notes = "Day of log",required=true)
    private Date day;
    @ApiModelProperty(notes = "Meal. BREAKFAST, LUNCH, DINNER, SNACK",required=true, example="BREAKFAST")
    private String meal;

    public LogEntryDto() {
        macrosCalculated = new Macro(0.0,0.0,0.0);
    }

    public FoodDto getFood() {
        return food;
    }

    public void setFood(FoodDto foodDto) {
        this.food = foodDto;
    }

    public PortionDto getPortion() {
        return portion;
    }

    public void setPortion(PortionDto portion) {
        this.portion = portion;
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
