package slt.database;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Mealplan;

import java.util.List;

interface MealplanCrudRepository extends CrudRepository<Mealplan, Long> {

    void deleteByUserIdAndId(Integer userId, Long dishId);

    void deleteByUserId(Integer userId);

    List<Mealplan> findByUserId(Integer userId);

}

@Repository
public class MealplanRepository {

    @Autowired
    MealplanCrudRepository mealplanCrudRepository;

    public Mealplan saveMealplan(Integer userId, Mealplan mealplan) {
        mealplan.setUserId(userId);
        return mealplanCrudRepository.save(mealplan);
    }

    @Transactional
    public void deleteMealplan(Integer userId, Long mealplanId) {
        mealplanCrudRepository.deleteByUserIdAndId(userId, mealplanId);
    }

    public List<Mealplan> getAllMealplans(Integer userId) {
        return mealplanCrudRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteAllForUser(Integer userId) {
        mealplanCrudRepository.deleteByUserId(userId);
    }
}
