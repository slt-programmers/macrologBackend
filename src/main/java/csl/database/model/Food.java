package csl.database.model;

/**
 * Created by Carmen on 18-3-2018.
 */
public class Food {

    private String name;
    private String unit;
    private String unitName;
    private Integer optionalGrams;
    private Integer protein;
    private Integer fat;
    private Integer carbs;

    public Food(String name,
                String unit,
                String unitName,
                Integer optionalGrams,
                Integer protein,
                Integer fat,
                Integer carbs) {
        this.name = name;
        this.unit = unit;
        this.unitName = unitName;
        this.optionalGrams = optionalGrams;
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

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public Integer getOptionalGrams() {
        return optionalGrams;
    }

    public void setOptionalGrams(Integer optionalGrams) {
        this.optionalGrams = optionalGrams;
    }

    public Integer getProtein() {
        return protein;
    }

    public void setProtein(Integer protein) {
        this.protein = protein;
    }

    public Integer getFat() {
        return fat;
    }

    public void setFat(Integer fat) {
        this.fat = fat;
    }

    public Integer getCarbs() {
        return carbs;
    }

    public void setCarbs(Integer carbs) {
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
