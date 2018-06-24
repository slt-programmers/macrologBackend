package csl.dto;

import io.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;

/**
 * Class voor het bewaren van de macros
 */
public class LogEntry {

    @ApiModelProperty(notes = "Defines what food has been eaten",required=true,example = "yoghurt")
    private String foodMacroName;
    @ApiModelProperty(notes = "MeasurementUnit. If left omitted the default measurement of the food will be used",required=false,example = "cup")
    private String measurementUnit;
    @ApiModelProperty(notes = "Multiplier of the measurement",required=true, example = "1.7")
    private Double multiplier;
    @ApiModelProperty(notes = "Time of log",required=true)
    private DateTime timestamp;


    public String getFoodMacroName() {
        return foodMacroName;
    }

    public void setFoodMacroName(String foodMacroName) {
        this.foodMacroName = foodMacroName;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(String measurementUnit) {
        this.measurementUnit = measurementUnit;
    }
}
