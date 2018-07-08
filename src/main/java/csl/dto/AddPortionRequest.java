package csl.dto;

public class AddPortionRequest {

   String description;
   Double grams;
   String unitMultiplier;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getGrams() {
        return grams;
    }

    public void setGrams(Double grams) {
        this.grams = grams;
    }

    public String getUnitMultiplier() {
        return unitMultiplier;
    }

    public void setUnitMultiplier(String unitMultiplier) {
        this.unitMultiplier = unitMultiplier;
    }
}
