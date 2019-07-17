package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Ingredient;

import javax.transaction.Transactional;
import java.util.List;

interface IngredientCrudRepository extends CrudRepository<Ingredient, Long> {
    void deleteByMealIdIn(List<Long> mealIds);

    void deleteByMealId(Long mealId);

    List<Ingredient> findByMealId(Long mealId);
}

@Repository
public class IngredientRepository {

    @Autowired
    IngredientCrudRepository ingredientCrudRepository;

    Ingredient saveIngredient(Ingredient ingredient) {
        return ingredientCrudRepository.save(ingredient);
    }

    @Transactional
    public void deleteAllForUser(List<Long> mealIds) {
        ingredientCrudRepository.deleteByMealIdIn(mealIds);
    }

    void updateIngredientsForMeal(Long mealId, List<Ingredient> newIngredients) {

        List<Ingredient> currentList = ingredientCrudRepository.findByMealId(mealId);

        for (Ingredient ingredient : newIngredients) {
            saveIngredient(ingredient);
        }

        for (Ingredient ingredient : currentList) {
            Long id = ingredient.getId();
            boolean found = false;
            for (Ingredient newIngredient : newIngredients) {
                if (newIngredient.getId().equals(id)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                deleteIngredient(ingredient.getId());
            }
        }
    }

    void deleteIngredientsForMeal(Long mealId) {
        ingredientCrudRepository.deleteByMealId(mealId);
    }

    @Transactional
    void deleteIngredient(Long ingredientId) {
        ingredientCrudRepository.deleteById(ingredientId);
    }


}