package csl.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * Class voor het bewaren van de macros
 */
public class Macro {

    @ApiModelProperty(notes = "Number of protein", required = true)
    private Double protein;
    @ApiModelProperty(notes = "Number of fat", required = true)
    private Double fat;
    @ApiModelProperty(notes = "Number of carbs", required = true)
    private Double carbs;
    private Double calories;

    public Macro(Double protein, Double fat, Double carbs) {
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
    }

    public Macro() {
    }

    public Double getProtein() {
        return protein;
    }

    public void setProtein(Double protein) {
        this.protein = protein;
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

    public Double getCalories() {
        return fat * 9 + carbs * 4 + protein * 4;
    }

    public void setCarbs(Double carbs) {
        this.carbs = carbs;
    }

    public void multiply(Double multiplier) {
        this.protein = this.protein * multiplier;
        this.fat = this.fat * multiplier;
        this.carbs = this.carbs * multiplier;
    }

    public Macro clone() {
        Macro clone = new Macro();
        clone.setFat(fat);
        clone.setCarbs(carbs);
        clone.setProtein(protein);

        return clone;
    }

    public void combine(Macro other) {
        this.fat = this.fat + other.fat;
        this.carbs = this.carbs + other.carbs;
        this.protein = this.protein + other.protein;

    }


}
