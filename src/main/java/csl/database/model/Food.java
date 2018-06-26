package csl.database.model;

/**
 * Created by Carmen on 18-3-2018.
 */
public class Food {

    private String name;
    private Double amountNumber;
    private String amountUnit;
    private Double protein;
    private Double fat;
    private Double carbs;

    public Food() {
    }

    public Food(String name,
                Double amountNumber,
                String amountUnit,
                Double protein,
                Double fat,
                Double carbs) {
        this.name = name;
        this.amountNumber = amountNumber;
        this.amountUnit = amountUnit;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getAmountUnit() {
        return amountUnit;
    }

    public void setAmountUnit(String amountUnit) {
        this.amountUnit = amountUnit;
    }

    public Double getAmountNumber() {
        return amountNumber;
    }

    public void setAmountNumber(Double amountNumber) {
        this.amountNumber = amountNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Food food = (Food) o;

        if (!name.equals(food.name)) return false;
        if (!amountUnit.equals(food.amountUnit)) return false;
        return amountNumber.equals(food.amountNumber);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + amountUnit.hashCode();
        result = 31 * result + amountNumber.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Food{" +
                "name='" + name + '\'' +
                '}';
    }
}
