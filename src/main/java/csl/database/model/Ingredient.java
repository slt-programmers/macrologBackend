package csl.database.model;

public class Ingredient {

    private Long id;
    private Long mealId;
    private Long foodId;
    private Long portionId;
    private Double multiplier;


    public Ingredient() {
    }

    public Ingredient(Long id, Long mealId, Long foodId, Long portionId, Double multiplier) {
        this.id = id;
        this.mealId = mealId;
        this.foodId = foodId;
        this.portionId = portionId;
        this.multiplier = multiplier;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMealId() {
        return mealId;
    }

    public void setMealId(Long mealId) {
        this.mealId = mealId;
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Long getPortionId() {
        return portionId;
    }

    public void setPortionId(Long portionId) {
        this.portionId = portionId;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }
}
