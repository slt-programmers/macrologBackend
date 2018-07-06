package csl.database.model;

/**
 * Created by Carmen on 6-7-2018.
 */
public class Portion {

    private Double grams;
    private Double unit;
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
