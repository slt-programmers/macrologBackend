package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Ingredient;
import slt.database.entities.Meal;

import javax.transaction.Transactional;
import java.util.List;

interface MealCrudRepository extends CrudRepository<Meal, Long> {

    void deleteByUserIdAndId(Integer userId, Long mealId);

    void deleteByUserId(Integer userId);

    List<Meal> findByUserId(Integer userId);
}

@Repository
public class MealRepository {
    private static final IngredientRepository ingredientRepository = new IngredientRepository();

    @Autowired
    MealCrudRepository mealCrudRepository;

    @Autowired
    IngredientCrudRepository ingredientCrudRepository;

//    public Meal insertMeal(Integer userId, Meal meal, List<Ingredient> ingredients) {
//
//        meal.setUserId(userId);
//        Meal savedMeal = mealCrudRepository.save(meal);
//
//        for (Ingredient ingredient : ingredients) {
//            ingredient.setMealId(savedMeal.getId());
//            ingredientRepository.saveIngredient(ingredient);
//        }
//        return savedMeal;
//    }
//
//    public Meal updateMeal(Integer userId, Meal meal, List<Ingredient>ingredients ) {
//        meal.setUserId(userId);
//        Meal saved = mealCrudRepository.save(meal);
//        ingredientRepository.updateIngredientsForMeal(meal.getId(), ingredients);
//        return saved;
//    }

    @Transactional
    public void deleteMeal(Integer userId, Long mealId) {
        ingredientRepository.deleteIngredientsForMeal(mealId);
        mealCrudRepository.deleteByUserIdAndId(userId,mealId);
    }

    public List<Meal> getAllMeals(Integer userId) {
        return mealCrudRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteAllForUser(Integer userId) {
        mealCrudRepository.deleteByUserId(userId);
    }

}