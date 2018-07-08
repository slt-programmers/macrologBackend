package csl.database.model;

import csl.enums.MeasurementUnit;

/**
 * Created by Carmen on 18-3-2018.
 */
public class Food {

    private Long id;
    private String name;
    private MeasurementUnit measurementUnit;
    private Double protein;
    private Double fat;
    private Double carbs;
    private String unitName;
    private Double unitGrams;

    public Food() {}

    public Food(Long id,
                String name,
                MeasurementUnit measurementUnit,
                Double protein,
                Double fat,
                Double carbs,
                String unitName,
                Double unitGrams) {
        this.id = id;
        this.name = name;
        this.measurementUnit = measurementUnit;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.unitName = unitName;
        this.unitGrams = unitGrams;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MeasurementUnit getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(MeasurementUnit measurementUnit) {
        this.measurementUnit = measurementUnit;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Food food = (Food) o;

        if (!id.equals(food.id)) return false;
        if (!name.equals(food.name)) return false;
        return measurementUnit == food.measurementUnit;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + measurementUnit.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Food{" +
                "name='" + name + '\'' +
                '}';
    }
}
