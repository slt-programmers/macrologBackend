package csl.dto;

import io.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;

/**
 * Class voor het bewaren van de macros
 */
public class AddLogEntryRequest {

    @ApiModelProperty(notes = "Defines what food has been eaten",required=true,example = "5")
    private Long foodId;
    @ApiModelProperty(notes = "MeasurementUnit ID. If left omitted the default measurement of the food will be used",required=false,example = "3")
    private Long aliasIdUsed;
    @ApiModelProperty(notes = "Multiplier of the measurement",required=true, example = "1.7")
    private Double multiplier;
    @ApiModelProperty(notes = "Time of log",required=true)
    private DateTime timestamp;


    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Long getAliasIdUsed() {
        return aliasIdUsed;
    }

    public void setAliasIdUsed(Long aliasIdUsed) {
        this.aliasIdUsed = aliasIdUsed;
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
}
