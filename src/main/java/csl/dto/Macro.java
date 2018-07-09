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

    public Macro(Double proteins, Double fat, Double carbs) {
        this.proteins = proteins;
        this.fat = fat;
        this.carbs = carbs;
    }

    public Macro() {
    }

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

    public void multiply(Double multiplier) {
        this.proteins = this.proteins * multiplier;
        this.fat = this.fat * multiplier;
        this.carbs = this.carbs * multiplier;
    }
    public Macro clone(){
        Macro clone = new Macro();
        clone.setFat(fat);
        clone.setCarbs(carbs);
        clone.setProteins(proteins);

        return clone;
    }
}
