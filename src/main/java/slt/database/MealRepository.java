package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Meal;

import javax.transaction.Transactional;
import java.util.List;

interface MealCrudRepository extends CrudRepository<Meal, Long> {

    void deleteByUserIdAndId(Integer userId, Long mealId);

    void deleteByUserId(Integer userId);

    List<Meal> findByUserId(Integer userId);

    Meal findByUserIdAndName(Integer userId, String name);
}

@Repository
public class MealRepository {

    @Autowired
    MealCrudRepository mealCrudRepository;

    public Meal saveMeal(Integer userId, Meal meal) {
        meal.setUserId(userId);
        return  mealCrudRepository.save(meal);
    }

    public Meal findByName(Integer userId, String name) {
        return mealCrudRepository.findByUserIdAndName(userId,name);
    }

    @Transactional
    public void deleteMeal(Integer userId, Long mealId) {
        mealCrudRepository.deleteByUserIdAndId(userId, mealId);
    }

    public List<Meal> getAllMeals(Integer userId) {
        return mealCrudRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteAllForUser(Integer userId) {
        mealCrudRepository.deleteByUserId(userId);
    }

}