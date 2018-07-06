package csl.database.model;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Carmen on 6-7-2018.
 */
public class Portion {

    @ApiModelProperty(notes = "Als je op food niveau grams hebt gekozen dan is dit de hoeveelheid gram",required=false, example = "100.0")
    private Double grams;
    @ApiModelProperty(notes = "als je op food niveau unit hebt gekozen. dan is dit aantal malen dat food",required=false, example = "1.2")
    private Double unit;
    @ApiModelProperty(notes = "bord oid",required=true, example = "bord")
    private String description;

    public Double getGrams() {
        return grams;
    }

    public void setGrams(Double grams) {
        this.grams = grams;
    }

    public Double getUnit() {
        return unit;
    }

    public void setUnit(Double unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
