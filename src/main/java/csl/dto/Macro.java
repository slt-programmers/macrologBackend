package csl.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * Class voor het bewaren van de macros
 */
public class Macro {

    @ApiModelProperty(notes = "Number of proteins",required=true)
    private Double proteins;
    @ApiModelProperty(notes = "Number of fat",required=true)
    private Double fat;
    @ApiModelProperty(notes = "Number of carbs",required=true)
    private Double carbs;

    public Double getProteins() {
        return proteins;
    }

    public void setProteins(Double proteins) {
        this.proteins = proteins;
    }

    public Double getFat() {
        return fat;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public Double getCarbs() {
        return carbs;
    }

    public void setCarbs(Double carbs) {
        this.carbs = carbs;
    }
}
