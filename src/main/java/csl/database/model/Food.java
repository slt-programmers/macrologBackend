package csl.database.model;

/**
 * Created by Carmen on 18-3-2018.
 */
public class Food {

    private String name;
    private String unit;
    private Double protein;
    private Double fat;
    private Double carbs;

    public Food() {
    }

    public Food(String name,
                String unit,
                Double protein,
                Double fat,
                Double carbs) {
        this.name = name;
        this.unit = unit;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Food food = (Food) o;

        if (!name.equals(food.name)) return false;
        return unit.equals(food.unit);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + unit.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Food{" +
                "name='" + name + '\'' +
                '}';
    }
}
