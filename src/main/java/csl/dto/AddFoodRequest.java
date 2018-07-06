package csl.dto;
import csl.database.model.Portion;
import java.util.List;

public class AddFoodRequest {

    private String name;
    private String measurementUnit;
    private String unitName;
    private Double unitGrams;
    private Double protein;
    private Double fat;
    private Double carbs;
    private List<Portion> portions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(String unit) {
        this.measurementUnit = unit;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public Double getUnitGrams() {
        return unitGrams;
    }

    public void setUnitGrams(Double unitGrams) {
        this.unitGrams = unitGrams;
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

    public void setCarbs(Double carbs) {
        this.carbs = carbs;
    }

    public List<Portion> getPortions() {
        return portions;
    }

    public void setPortions(List<Portion> portions) {
        this.portions = portions;
    }
}
