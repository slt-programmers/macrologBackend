package slt.database;


import lombok.AllArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Ingredient;

import java.util.List;

interface IngredientCrudRepository extends CrudRepository<Ingredient, Long> { }

@Repository
@AllArgsConstructor
public class IngredientRepository {

    private IngredientCrudRepository ingredientCrudRepository;

    public List<Ingredient> getAllIngredients() {
        return (List<Ingredient>) ingredientCrudRepository.findAll();
    }
}
